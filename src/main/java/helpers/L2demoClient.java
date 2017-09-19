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
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.commons.exceptions.custom.ValidationException;
import eu.h2020.symbiote.security.communication.payloads.AAM;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import eu.h2020.symbiote.security.handler.ISecurityHandler;
import eu.h2020.symbiote.security.helpers.MutualAuthenticationHelper;
import io.jsonwebtoken.Claims;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.NoSuchAlgorithmException;
import java.util.*;

public class L2demoClient {

    public static void main(String[] args) throws SecurityHandlerException, InvalidArgumentsException, ValidationException, NoSuchAlgorithmException {

        Log log = LogFactory.getLog(L2demoClient.class);

        String coreAAMServerAddress = "";
        String KEY_STORE_PATH = "";
        String KEY_STORE_PASSWORD = "";
        String userPlatformId = "";
        String rapPlatformId = "";
        String platformOwnerUsername = "";
        String platformOwnerPassword = "";

        String rapPlatformOwnerUsername = "";
        String rapPlatformOwnerPassword = "";
        String userId = "clientPlatformId";


        //TODO create and register platform owners and 2 platforms

        //Acquire home token from platform 1;

        // generating the CSH
        ISecurityHandler clientSH = ClientSecurityHandlerFactory.getSecurityHandler(
                coreAAMServerAddress,
                KEY_STORE_PATH,
                KEY_STORE_PASSWORD,
                userId
        );
        AAM coreAAM = clientSH.getCoreAAMInstance();
        AAM platform1 = clientSH.getAvailableAAMs().get(userPlatformId);
        String username = "testUser";
        String password = "testPassword";

        Certificate cert = clientSH.getCertificate(platform1, username, password, userId );
        //TODO get private key
        Token token = clientSH.login(platform1);

        clientSH.getAcquiredCredentials();
        Set<AuthorizationCredentials> authorizationCredentialsSet=new HashSet<>();
        //TODO put private key
        authorizationCredentialsSet.add(new AuthorizationCredentials(token, platform1, new HomeCredentials(platform1, username, userId, cert, null)));

        SecurityRequest securityRequest = MutualAuthenticationHelper.getSecurityRequest(authorizationCredentialsSet, false);

        String rapKey = "rap";
        String rapComponentId = rapKey + "@" + rapPlatformId;
        // generating the CSH
        //TODO configure rapCSH
        IComponentSecurityHandler rapCSH = ComponentSecurityHandlerFactory.getComponentSecurityHandler(
                coreAAMServerAddress,
                KEY_STORE_PATH,
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
