package helpers;

import eu.h2020.symbiote.security.ClientSecurityHandlerFactory;
import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.accesspolicies.common.SingleTokenAccessPolicyFactory;
import eu.h2020.symbiote.security.accesspolicies.common.singletoken.SingleTokenAccessPolicySpecifier;
import eu.h2020.symbiote.security.commons.Certificate;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.Token;
import eu.h2020.symbiote.security.commons.credentials.AuthorizationCredentials;
import eu.h2020.symbiote.security.commons.credentials.HomeCredentials;
import eu.h2020.symbiote.security.commons.exceptions.custom.*;
import eu.h2020.symbiote.security.communication.payloads.AAM;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import eu.h2020.symbiote.security.handler.ISecurityHandler;
import eu.h2020.symbiote.security.helpers.ECDSAHelper;
import eu.h2020.symbiote.security.helpers.MutualAuthenticationHelper;
import eu.h2020.symbiote.security.helpers.PlatformAAMCertificateKeyStoreFactory;
import io.jsonwebtoken.Claims;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static helpers.Constants.*;
import static helpers.PlatformRegistrationHelper.registerPlatform;
import static helpers.PlatformRegistrationHelper.registerPlatformOwner;

public class L2demoClient {

    public static void main(String[] args) throws SecurityHandlerException, InvalidArgumentsException, ValidationException, NoSuchAlgorithmException, KeyManagementException, CertificateException, WrongCredentialsException, NotExistingUserException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {

        Log log = LogFactory.getLog(L2demoClient.class);

        try {
            registerPlatformOwner(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername, platformOwnerPassword, federatedId, recoveryMail,
                    rabbitHost, rabbitUsername, rabbitPassword, userManagementRequestQueue);

            registerPlatformOwner(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername2, platformOwnerPassword2, federatedId2, recoveryMail2,
                    rabbitHost, rabbitUsername, rabbitPassword, userManagementRequestQueue);

            registerPlatform(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername, platformOwnerPassword, platformInstanceFriendlyName,
                    platformInterworkingInterfaceAddress, platformId, rabbitHost, rabbitUsername, rabbitPassword, platformManagementRequestQueue);

            registerPlatform(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername2, platformOwnerPassword2, platformInstanceFriendlyName2,
                    platformInterworkingInterfaceAddress2, platformId2, rabbitHost, rabbitUsername, rabbitPassword, platformManagementRequestQueue);
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(e.getCause());
        }


        String coreAAMServerAddress = "https://localhost:8080";
        String DE = "core.p12";
        String KEY_STORE_PASSWORD = "1234567";
        String userPlatformId = "";
        String rapPlatformId = "";

        String rapPlatformOwnerUsername = "";
        String rapPlatformOwnerPassword = "";
        String userId = "clientPlatformId";

        //Acquire home token from platform 1;
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
        // generating the CSH
        ISecurityHandler clientSH = ClientSecurityHandlerFactory.getSecurityHandler(
                coreAAMServerAddress,
                DE,
                KEY_STORE_PASSWORD,
                userId
        );
        AAM coreAAM = clientSH.getCoreAAMInstance();
        AAM platform1 = clientSH.getAvailableAAMs().get(platformId);
        String username = "testUser";
        String password = "testPassword";
        String KEY_STORE_PATH = "./src/main/resources/new.p12";
        String PV_KEY_PASSWORD = "1234567";
        /*PlatformAAMCertificateKeyStoreFactory.getPlatformAAMKeystore(
                coreAAMServerAddress, platformOwnerUsername, platformOwnerPassword, platformId, KEY_STORE_PATH,
                KEY_STORE_PASSWORD,
                "root_cert", "aam_cert", PV_KEY_PASSWORD
        );
        //keyStore checking if proper Certificates exists
        KeyStore trustStore = KeyStore.getInstance("PKCS12", "BC");
        try (
            FileInputStream fIn = new FileInputStream(KEY_STORE_PATH)) {
            trustStore.load(fIn, KEY_STORE_PASSWORD.toCharArray());
            fIn.close();
        }
*/


        Certificate cert = clientSH.getCertificate(platform1, platformOwnerUsername, platformOwnerPassword, "L2DemoApp" );


        //TODO get private key
        Token token = clientSH.login(platform1);

        clientSH.getAcquiredCredentials();
        Set<AuthorizationCredentials> authorizationCredentialsSet=new HashSet<>();
        //TODO put private key
        //Certificate cert = null;
        authorizationCredentialsSet.add(new AuthorizationCredentials(token, platform1, new HomeCredentials(platform1, username, userId, cert, null)));

        SecurityRequest securityRequest = MutualAuthenticationHelper.getSecurityRequest(authorizationCredentialsSet, false);

        String rapKey = "rap";
        String rapComponentId = rapKey + "@" + rapPlatformId;
        // generating the CSH
        //TODO configure rapCSH
        IComponentSecurityHandler rapCSH = ComponentSecurityHandlerFactory.getComponentSecurityHandler(
                coreAAMServerAddress,
                DE,
                KEY_STORE_PASSWORD,
                rapComponentId,
                coreAAMServerAddress,
                false,
                rapPlatformOwnerUsername,
                rapPlatformOwnerPassword
        );
        //adding policy to platform 2
        Map<String, IAccessPolicy> testAP = new HashMap<>();
        String testPolicyId = "testPolicyId";
        Map<String, String> requiredClaims = new HashMap<>();
        requiredClaims.put(Claims.ISSUER, SecurityConstants.CORE_AAM_INSTANCE_ID);
        SingleTokenAccessPolicySpecifier testPolicySpecifier =
                new SingleTokenAccessPolicySpecifier(
                        SingleTokenAccessPolicySpecifier.SingleTokenAccessPolicyType.SLHTIBAP,
                        requiredClaims);
        testAP.put(testPolicyId, SingleTokenAccessPolicyFactory.getSingleTokenAccessPolicy(testPolicySpecifier));

        //TODO change those ifs, they are stupid
        if (!rapCSH.getSatisfiedPoliciesIdentifiers(testAP, securityRequest).isEmpty()){
            log.error("SecurityRequest using Platform1 home token passed Access Policy. It should not.");
        }
        log.info("SecurityRequest using Platform1 home token not passed Access Policy");
        //TODO make pause after every operation (checking satisfied policies, generating foreignToken)
        log.info("SecurityRequest using Platform1 home token not passed Access Policy");
        //TODO make federation rule in core for platform 1

        //TODO get foreign token from core aam using homeToken from platform 1
        List <AAM> aamList = new ArrayList<>();
        aamList.add(coreAAM);
        Map<AAM, Token> foreignTokens = clientSH.login(aamList, token.toString());
        authorizationCredentialsSet=new HashSet<>();
        //TODO put private key
        authorizationCredentialsSet.add(new AuthorizationCredentials(foreignTokens.get(coreAAM), coreAAM, new HomeCredentials(platform1, username, userId, cert, null)));
        securityRequest = MutualAuthenticationHelper.getSecurityRequest(authorizationCredentialsSet, false);


        SecurityRequest federatedSecurityRequest = null;
        if (rapCSH.getSatisfiedPoliciesIdentifiers(testAP, federatedSecurityRequest).isEmpty()){
            log.error("SecurityRequest using federated token didn't pass Access Policy. It should.");
        }
        log.info("SecurityRequest using federated token passed Access Policy.");
    }

}
