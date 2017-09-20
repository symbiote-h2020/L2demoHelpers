package helpers;

import eu.h2020.symbiote.security.ClientSecurityHandlerFactory;
import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.accesspolicies.common.SingleTokenAccessPolicyFactory;
import eu.h2020.symbiote.security.accesspolicies.common.singletoken.SingleTokenAccessPolicySpecifier;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.Token;
import eu.h2020.symbiote.security.commons.credentials.AuthorizationCredentials;
import eu.h2020.symbiote.security.commons.exceptions.custom.*;
import eu.h2020.symbiote.security.communication.payloads.AAM;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import eu.h2020.symbiote.security.handler.ISecurityHandler;
import eu.h2020.symbiote.security.helpers.MutualAuthenticationHelper;
import io.jsonwebtoken.Claims;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;


public class L2demoClient {

    public static void main(String[] args) throws SecurityHandlerException, InvalidArgumentsException, ValidationException, NoSuchAlgorithmException, KeyManagementException, CertificateException, WrongCredentialsException, NotExistingUserException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {

        Log log = LogFactory.getLog(L2demoClient.class);

        String platform1Username = "testUser";
        String platform1Password = "testPassword";

        // generating the CSH
        ISecurityHandler clientSH = ClientSecurityHandlerFactory.getSecurityHandler(
                Constants.coreAAMServerAddress,
                Constants.KEY_STORE_PATH,
                Constants.KEY_STORE_PASSWORD,
                Constants.userId
        );
        AAM coreAAM = clientSH.getCoreAAMInstance();
        AAM platform1 = clientSH.getAvailableAAMs().get(Constants.platformId);

        clientSH.getCertificate(platform1, platform1Username, platform1Password, Constants.userId );
        Token token = clientSH.login(platform1);

        Set<AuthorizationCredentials> authorizationCredentialsSet=new HashSet<>();
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

        //get foreign token from core aam using homeToken from platform 1
        List <AAM> aamList = new ArrayList<>();
        aamList.add(coreAAM);
        Map<AAM, Token> foreignTokens = clientSH.login(aamList, token.toString());
        authorizationCredentialsSet=new HashSet<>();
        authorizationCredentialsSet.add(new AuthorizationCredentials(foreignTokens.get(coreAAM), coreAAM, clientSH.getAcquiredCredentials().get(coreAAM.getAamInstanceId()).homeCredentials));
        SecurityRequest federatedSecurityRequest = MutualAuthenticationHelper.getSecurityRequest(authorizationCredentialsSet, false);

        if (rapCSH.getSatisfiedPoliciesIdentifiers(testAP, federatedSecurityRequest).isEmpty()){
            log.error("SecurityRequest using federated token didn't pass Access Policy. It should.");
        }
        log.info("SecurityRequest using federated token passed Access Policy.");
    }

}
