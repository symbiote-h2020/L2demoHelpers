package helpers;

import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.accesspolicies.common.SingleTokenAccessPolicyFactory;
import eu.h2020.symbiote.security.accesspolicies.common.singletoken.SingleTokenAccessPolicySpecifier;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.Token;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.payloads.AAM;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import io.jsonwebtoken.Claims;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class L2demoClient {

    public static void main(String[] args) throws SecurityHandlerException, InvalidArgumentsException {

        Log log = LogFactory.getLog(L2demoClient.class);

        String serverAddress = "";
        String KEY_STORE_PATH = "";
        String KEY_STORE_PASSWORD = "";
        String platformId = "";
        String rapPlatformId = "";
        String platformOwnerUsername = "";
        String platformOwnerPassword = "";

        String rapPlatformOwnerUsername = "";
        String rapPlatformOwnerPassword = "";

        SecurityRequest platformSecurityRequest = null;

        //TODO create and register platform owners and 2 platforms
        //TODO Acquire home token from platform 1;


        String rapKey = "rap";
        String rapComponentId = rapKey + "@" + rapPlatformId;
        // generating the CSH
        //TODO configure rapCSH
        IComponentSecurityHandler rapCSH = ComponentSecurityHandlerFactory.getComponentSecurityHandler(
                serverAddress,
                KEY_STORE_PATH,
                KEY_STORE_PASSWORD,
                rapComponentId,
                serverAddress,
                false,
                rapPlatformOwnerUsername,
                rapPlatformOwnerPassword
        );
        //TODO add policy to platform 2
        Map<String, IAccessPolicy> testAP = new HashMap<>();
        String testPolicyId = "testPolicyId";
        Map<String, String> requiredClaims = new HashMap<>();
        requiredClaims.put(Claims.ISSUER, SecurityConstants.CORE_AAM_INSTANCE_ID);
        SingleTokenAccessPolicySpecifier testPolicySpecifier =
                new SingleTokenAccessPolicySpecifier(
                        SingleTokenAccessPolicySpecifier.SingleTokenAccessPolicyType.SLHTIBAP,
                        requiredClaims);
        testAP.put(testPolicyId, SingleTokenAccessPolicyFactory.getSingleTokenAccessPolicy(testPolicySpecifier));



        //TODO Try to acquire access to resouce in platform 2 using platform1 home token - not possible
        if (!rapCSH.getSatisfiedPoliciesIdentifiers(testAP, platformSecurityRequest).isEmpty()){
            log.error("Platform 1 SecurityRequest passed the rule. It should not.");
        };


        //TODO make federation rule in core for platform 1
        //TODO get foreign token from core aam using homeToken from platform 1

        SecurityRequest federatedSecurityRequest = null;
        if (rapCSH.getSatisfiedPoliciesIdentifiers(testAP, federatedSecurityRequest).isEmpty()){
            log.error("Platform 1 federated SecurityRequest didn't pass the rule. It should.");
        };
    }

}
