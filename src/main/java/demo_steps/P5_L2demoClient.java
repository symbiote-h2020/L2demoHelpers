package demo_steps;

import eu.h2020.symbiote.security.ClientSecurityHandlerFactory;
import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.accesspolicies.common.SingleTokenAccessPolicyFactory;
import eu.h2020.symbiote.security.accesspolicies.common.singletoken.SingleTokenAccessPolicySpecifier;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.Token;
import eu.h2020.symbiote.security.commons.credentials.AuthorizationCredentials;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.commons.exceptions.custom.ValidationException;
import eu.h2020.symbiote.security.communication.payloads.AAM;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import eu.h2020.symbiote.security.handler.ISecurityHandler;
import eu.h2020.symbiote.security.helpers.ECDSAHelper;
import eu.h2020.symbiote.security.helpers.MutualAuthenticationHelper;
import helpers.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class P5_L2demoClient {

    private static final Log log = LogFactory.getLog(P5_L2demoClient.class);

    public static void main(String[] args) throws
            SecurityHandlerException,
            InvalidArgumentsException,
            ValidationException,
            NoSuchAlgorithmException,
            KeyManagementException,
            IOException {


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

        log.info("Initializing application security handler");
        // generating the SH
        ISecurityHandler clientSH = ClientSecurityHandlerFactory.getSecurityHandler(
                Constants.coreAAMServerAddress,
                Constants.KEY_STORE_PATH,
                Constants.KEY_STORE_PASSWORD,
                Constants.userId
        );
        AAM coreAAM = clientSH.getCoreAAMInstance();
        AAM platform1 = clientSH.getAvailableAAMs().get(Constants.platformId);


        log.info("Acquiring application certificate");
        clientSH.getCertificate(platform1, Constants.username, Constants.password, Constants.userId);
        log.info("Acquiring application HOME token from " + Constants.platformId);
        Token token = clientSH.login(platform1);

        Set<AuthorizationCredentials> authorizationCredentialsSet = new HashSet<>();
        authorizationCredentialsSet.add(new AuthorizationCredentials(token, platform1, clientSH.getAcquiredCredentials().get(platform1.getAamInstanceId()).homeCredentials));
        SecurityRequest securityRequest = MutualAuthenticationHelper.getSecurityRequest(authorizationCredentialsSet, false);

        //rap platform
        String rapKey = "rap";
        String rapComponentId = rapKey + "@" + Constants.platformId2;
        // generating the CSH
        IComponentSecurityHandler rapCSH = ComponentSecurityHandlerFactory.getComponentSecurityHandler(
                Constants.coreAAMServerAddress,
                Constants.KEY_STORE_PATH,
                Constants.KEY_STORE_PASSWORD,
                rapComponentId,
                Constants.coreAAMServerAddress,
                false,
                Constants.platformOwnerUsername2,
                Constants.platformOwnerPassword2
        );
        // adding policy to platform 2 'dummy' rap
        Map<String, IAccessPolicy> testAP = new HashMap<>();
        Set<String> federationMembers = new HashSet<>();
        federationMembers.add(Constants.platformId);
        federationMembers.add(Constants.platformId2);
        // for the demo the core is also in federation as the arbiter
        federationMembers.add(SecurityConstants.CORE_AAM_INSTANCE_ID);
        String goodResourceId = "resourceId";

        SingleTokenAccessPolicySpecifier testPolicySpecifier = new SingleTokenAccessPolicySpecifier(federationMembers, Constants.platformId2, Constants.federationId);
        testAP.put(goodResourceId, SingleTokenAccessPolicyFactory.getSingleTokenAccessPolicy(testPolicySpecifier));

        if (!rapCSH.getSatisfiedPoliciesIdentifiers(testAP, securityRequest).isEmpty()) {
            String m = "Access to federated resource using Platform1 HOME token passed Access Policy. It should not.";
            log.error(m);
            System.exit(1);
        }
        log.info("Access to federated resource using Platform1 HOME token was rejected");

        //get foreign token from core aam using homeToken from platform 1
        List<AAM> aamList = new ArrayList<>();
        aamList.add(coreAAM);

        log.info("Attempting to acquire FOREIGN token from Core AAM");
        Map<AAM, Token> foreignTokens = new HashMap<>();
        try {
            foreignTokens = clientSH.login(aamList, token.toString());
            log.info("Foreign token acquired");
        } catch (SecurityHandlerException | NullPointerException e) {
            log.error("Failed to acquire foreign token");
            System.exit(1);
        }

        authorizationCredentialsSet = new HashSet<>();
        authorizationCredentialsSet.add(new AuthorizationCredentials(foreignTokens.get(coreAAM), coreAAM, clientSH.getAcquiredCredentials().get(platform1.getAamInstanceId()).homeCredentials));
        SecurityRequest federatedSecurityRequest = MutualAuthenticationHelper.getSecurityRequest(authorizationCredentialsSet, false);

        if (rapCSH.getSatisfiedPoliciesIdentifiers(testAP, federatedSecurityRequest).isEmpty()) {
            String m = "Access to federated resource using FOREIGN token from CoreAAM didn't pass Access Policy. It should.";
            log.error(m);
            System.exit(1);
        }
        log.info("Access to federated resource using FOREIGN token from CoreAAM was GRANTED");

        log.info("Waiting for operator to update the federation");
        System.in.read();

        log.info("Trying to access the resource again using the cached foreign token");

        if (!rapCSH.getSatisfiedPoliciesIdentifiers(testAP, federatedSecurityRequest).isEmpty()) {
            log.error("Access to federated resource using the cached FOREIGN token from CoreAAM was GRANTED. It should not.");
            System.exit(1);
        }
        log.info("Access to federated resource using the cached FOREIGN token was denied as the token was revoked by the issuer due to platform1 no longer being in the federation");
    }
}
