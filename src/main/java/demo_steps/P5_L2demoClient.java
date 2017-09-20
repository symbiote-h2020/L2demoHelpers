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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class P5_L2demoClient {

    private static final Log log = LogFactory.getLog(P5_L2demoClient.class);

    public static void main(String[] args) throws
            SecurityHandlerException,
            InvalidArgumentsException, ValidationException, NoSuchAlgorithmException, KeyManagementException {


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
                Constants.coreAAMServerAddress,
                Constants.KEY_STORE_PATH,
                Constants.KEY_STORE_PASSWORD,
                Constants.userId
        );
        AAM coreAAM = clientSH.getCoreAAMInstance();
        AAM platform1 = clientSH.getAvailableAAMs().get(Constants.platformId);

        //TODO change coreAAM to platform1
        clientSH.getCertificate(platform1, Constants.username, Constants.password, Constants.userId);
        Token token = clientSH.login(platform1);

        Set<AuthorizationCredentials> authorizationCredentialsSet = new HashSet<>();
        //TODO check this
        authorizationCredentialsSet.add(new AuthorizationCredentials(token, platform1, clientSH.getAcquiredCredentials().get(platform1.getAamInstanceId()).homeCredentials));
        SecurityRequest securityRequest = MutualAuthenticationHelper.getSecurityRequest(authorizationCredentialsSet, false);

        //rap platform
        String rapKey = "rap";
        String rapComponentId = rapKey + "@" + Constants.platformId2;
        // generating the CSH
        //TODO configure rapCSH
        IComponentSecurityHandler rapCSH = ComponentSecurityHandlerFactory.getComponentSecurityHandler(
                Constants.coreAAMServerAddress,
                Constants.KEY_STORE_PATH,
                Constants.KEY_STORE_PASSWORD,
                rapComponentId,
                Constants.rapAAMServerAddress,
                false,
                Constants.platformOwnerUsername2,
                Constants.platformOwnerPassword2
        );
        //adding policy to platform 2
        Map<String, IAccessPolicy> testAP = new HashMap<>();
        Set<String> federationMembers = new HashSet<>();
        federationMembers.add(platform1.getAamInstanceId());
        federationMembers.add(rapComponentId);
        // for the demo the core is also in federation as the arbiter
        federationMembers.add(SecurityConstants.CORE_AAM_INSTANCE_ID);
        String goodResourceId = "resourceId";

        SingleTokenAccessPolicySpecifier testPolicySpecifier = new SingleTokenAccessPolicySpecifier(federationMembers, rapComponentId, Constants.federationId);
        testAP.put(goodResourceId, SingleTokenAccessPolicyFactory.getSingleTokenAccessPolicy(testPolicySpecifier));

        //TODO change those ifs, they are stupid
        if (!rapCSH.getSatisfiedPoliciesIdentifiers(testAP, securityRequest).isEmpty()) {
            log.error("SecurityRequest using Platform1 home token passed Access Policy. It should not.");
            throw new SecurityException("SecurityRequest using Platform1 home token passed Access Policy. It should not.");
        }
        log.info("SecurityRequest using Platform1 home token not passed Access Policy");
        //TODO make pause after every operation (checking satisfied policies, generating foreignToken)

        //get foreign token from core aam using homeToken from platform 1
        List<AAM> aamList = new ArrayList<>();
        aamList.add(coreAAM);

        Map<AAM, Token> foreignTokens = clientSH.login(aamList, token.toString());
        log.info("Foreign token acquired");
        authorizationCredentialsSet = new HashSet<>();
        authorizationCredentialsSet.add(new AuthorizationCredentials(foreignTokens.get(coreAAM), coreAAM, clientSH.getAcquiredCredentials().get(platform1.getAamInstanceId()).homeCredentials));
        SecurityRequest federatedSecurityRequest = MutualAuthenticationHelper.getSecurityRequest(authorizationCredentialsSet, false);

        if (rapCSH.getSatisfiedPoliciesIdentifiers(testAP, federatedSecurityRequest).isEmpty()) {
            log.error("SecurityRequest using federated token didn't pass Access Policy. It should.");
            throw new SecurityException("SecurityRequest using federated token didn't pass Access Policy. It should.");
        }
        log.info("SecurityRequest using federated token passed Access Policy.");
    }
}
