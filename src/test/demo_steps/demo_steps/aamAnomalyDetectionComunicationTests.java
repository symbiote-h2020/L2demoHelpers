package demo_steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.security.ClientSecurityHandlerFactory;
import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.L2DemoHelper;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.Token;
import eu.h2020.symbiote.security.commons.enums.ManagementStatus;
import eu.h2020.symbiote.security.commons.enums.OperationType;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.commons.enums.ValidationStatus;
import eu.h2020.symbiote.security.commons.exceptions.SecurityException;
import eu.h2020.symbiote.security.commons.exceptions.custom.AAMException;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.commons.exceptions.custom.ValidationException;
import eu.h2020.symbiote.security.communication.AAMClient;
import eu.h2020.symbiote.security.communication.IAAMClient;
import eu.h2020.symbiote.security.communication.payloads.*;
import eu.h2020.symbiote.security.handler.IAnomalyListenerSecurity;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import eu.h2020.symbiote.security.handler.ISecurityHandler;
import eu.h2020.symbiote.security.helpers.ECDSAHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Optional;

import static eu.h2020.symbiote.security.helpers.Constants.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = L2DemoHelper.class)
public class aamAnomalyDetectionComunicationTests {

    private static final Log log = LogFactory.getLog(aamAnomalyDetectionComunicationTests.class);
    @Value("${anomaly.verbosity.level}")
    private String rabbitPassword;
    @Value("${rabbit.queue.get.user.details}")
    private String getUserDetailsQueue;
    @Value("${rabbit.queue.manage.revocation.request}")
    private String revocationQueue;
    private IAAMClient aamClient;

    @Autowired
    private IAnomalyListenerSecurity anomaliesHelper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void before() throws
            NoSuchAlgorithmException,
            KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        ECDSAHelper.enableECDSAProvider();
        //cleanup
        this.aamClient = new AAMClient(coreAAMServerAddress);
        manageUser(aamClient, OperationType.DELETE, username, password);
        manageUser(aamClient, OperationType.DELETE, username2, password2);
        anomaliesHelper.clearBlockedActions();

    }

    @Test
    public void userBlockedDuringHomeTokenAcquisition() throws
            SecurityHandlerException,
            ValidationException,
            InterruptedException {

        // creating user in DB
        assertTrue(manageUser(aamClient, OperationType.CREATE, username, password));
        // generating the SH
        ISecurityHandler clientSH = ClientSecurityHandlerFactory.getSecurityHandler(
                coreAAMServerAddress,
                KEY_STORE_PATH,
                KEY_STORE_PASSWORD
        );
        AAM coreAAM = clientSH.getCoreAAMInstance();

        log.info("Acquiring application certificate");
        clientSH.getCertificate(coreAAM, username, password, userId);
        log.info("Unregister user in AAM - certificate is not valid anymore");
        assertTrue(manageUser(aamClient, OperationType.DELETE, username, password));

        for (int i = 0; i < 4; i++) {
            try {
                log.info("Acquiring application HOME token from " + coreAAMServerAddress);
                clientSH.login(coreAAM);
            } catch (SecurityHandlerException e) {
                log.info("As expected: caught error: " + e.getMessage());
            }
        }

        log.info("Waiting for ADM sending message about anomaly");
        Thread.sleep(1000);
        try {
            log.info("Acquiring application HOME token from " + coreAAMServerAddress);
            clientSH.login(coreAAM);
        } catch (SecurityHandlerException e) {
            assertTrue(e.getMessage().contains("User was blocked"));
            log.info("Anomaly was detected, error: " + e.getErrorMessage());
        }
    }

    @Test
    public void componentBlockedDuringHomeTokenAcquisition() throws
            SecurityHandlerException,
            JsonProcessingException,
            ValidationException,
            InterruptedException {

        // generating the SH
        String crmKey = "crm";
        String crmComponentId = crmKey + "@" + SecurityConstants.CORE_AAM_INSTANCE_ID;
        assertNotNull(anomaliesHelper);
        IComponentSecurityHandler componentSH = ComponentSecurityHandlerFactory.getComponentSecurityHandler(
                KEY_STORE_PATH,
                KEY_STORE_PASSWORD,
                crmComponentId,
                coreAAMServerAddress,
                AAMOwnerUsername,
                AAMOwnerPassword,
                Optional.of(anomaliesHelper));

        AAM coreAAM = componentSH.getSecurityHandler().getCoreAAMInstance();
        componentSH.generateSecurityRequestUsingLocalCredentials();

        //deleting component from aam database - it's certificate is not longer valid.
        RevocationRequest revocationRequest = new RevocationRequest();
        revocationRequest.setCredentials(new Credentials(AAMOwnerUsername, AAMOwnerPassword));
        revocationRequest.setCredentialType(RevocationRequest.CredentialType.ADMIN);
        revocationRequest.setCertificateCommonName(crmComponentId);

        Object response = rabbitTemplate.convertSendAndReceive(revocationQueue, mapper.writeValueAsString
                (revocationRequest).getBytes());
        RevocationResponse revocationResponse = mapper.convertValue(response,
                RevocationResponse.class);

        assertTrue(revocationResponse.isRevoked());
        componentSH.getSecurityHandler().clearCachedTokens();

        for (int i = 0; i < 4; i++) {
            try {
                log.info("Acquiring application HOME token from " + coreAAMServerAddress);
                componentSH.getSecurityHandler().login(coreAAM);
            } catch (SecurityHandlerException e) {
                log.info("As expected: caught error: " + e.getMessage());
            }
        }

        log.info("Waiting for ADM sending message about anomaly");
        Thread.sleep(1000);
        try {
            log.info("Acquiring application HOME token from " + coreAAMServerAddress);
            componentSH.getSecurityHandler().login(coreAAM);
        } catch (SecurityHandlerException e) {
            assertTrue(e.getMessage().contains("User was blocked"));
            log.info("Anomaly was detected, error: " + e.getErrorMessage());
        }
    }

    @Test
    public void userBlockedDuringDetailsAcquisition() throws
            InterruptedException {

        //register user
        assertTrue(manageUser(aamClient, OperationType.CREATE, username, password));

        for (int i = 0; i < 4; i++) {
            try {
                log.info("Acquiring user details using wrong password from " + coreAAMServerAddress);
                aamClient.getUserDetails(new Credentials(username, "Wrong password"));
            } catch (SecurityException e) {
                log.info("As expected: caught error: " + e.getMessage());
            }
        }
        log.info("Waiting for ADM sending message about anomaly");
        Thread.sleep(1000);
        try {
            log.info("Acquiring user details using wrong password from " + coreAAMServerAddress);
            aamClient.getUserDetails(new Credentials(username, "Wrong password"));
        } catch (SecurityException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
            log.info("Anomaly was detected, error: " + e.getErrorMessage());
        }
    }

    @Test
    public void tokenBlockedDuringValidation() throws
            InterruptedException,
            SecurityHandlerException,
            ValidationException,
            AAMException {

        //register user
        assertTrue(manageUser(aamClient, OperationType.CREATE, username2, password2));
        // generating the SH
        ISecurityHandler clientSH = ClientSecurityHandlerFactory.getSecurityHandler(
                coreAAMServerAddress,
                KEY_STORE_PATH,
                KEY_STORE_PASSWORD
        );
        AAM coreAAM = clientSH.getCoreAAMInstance();

        log.info("Acquiring application certificate");
        clientSH.getCertificate(coreAAM, username2, password2, userId);
        log.info("Acquiring HOME token");
        Token token = clientSH.login(coreAAM);
        assertTrue(manageUser(aamClient, OperationType.DELETE, username2, password2));
        for (int i = 0; i < 4; i++) {

            log.info("Validating token in core AAM");
            ValidationStatus validationStatus = aamClient.validateCredentials(token.getToken(), Optional.empty(), Optional.empty(), Optional.empty());
            assertEquals(ValidationStatus.REVOKED_SPK, validationStatus);
        }
        log.info("Waiting for ADM sending message about anomaly");
        Thread.sleep(1000);

        log.info("Validating token in core AAM");
        ValidationStatus validationStatus = aamClient.validateCredentials(token.getToken(), Optional.empty(), Optional.empty(), Optional.empty());
        assertEquals(ValidationStatus.BLOCKED, validationStatus);

    }

    public boolean manageUser(IAAMClient aamClient, OperationType operationType, String username, String password) {
        UserManagementRequest userManagementRequest = new UserManagementRequest(new
                Credentials(AAMOwnerUsername, AAMOwnerPassword), new Credentials(username, password),
                new UserDetails(new Credentials(username, password), federatedId, recoveryMail, UserRole.USER, new HashMap<>(), new HashMap<>()),
                operationType);
        try {
            ManagementStatus managementStatus = aamClient.manageUser(userManagementRequest);
            if (managementStatus != ManagementStatus.ERROR) {
                return true;
            }
        } catch (AAMException e) {
            return false;
        }
        return false;
    }
}