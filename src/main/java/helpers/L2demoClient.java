package helpers;

import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.Token;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.payloads.AAM;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import io.jsonwebtoken.Claims;

import java.util.HashMap;
import java.util.Map;

public class L2demoClient {

    public static void main(String[] args) throws SecurityHandlerException {
        String serverAddress = "";
        String KEY_STORE_PATH = "";
        String KEY_STORE_PASSWORD = "";
        String platformId = "";
        String rapPlatformId = "";
        String platformOwnerUsername = "";
        String platformOwnerPassword = "";

        String rapPlatformOwnerUsername = "";
        String rapPlatformOwnerPassword = "";



        //TODO create and register platform owners and 2 platforms

        String crmKey = "crm";
        String crmComponentId = crmKey + "@" + platformId;
        // generating the CSH
        IComponentSecurityHandler crmCSH = ComponentSecurityHandlerFactory.getComponentSecurityHandler(
                serverAddress,
                KEY_STORE_PATH,
                KEY_STORE_PASSWORD,
                crmComponentId,
                serverAddress,
                false,
                platformOwnerUsername,
                platformOwnerPassword
        );
        System.out.println(crmCSH.getSecurityHandler().getAvailableAAMs().values());
        //TODO get proper AAM
        //Token token = crmCSH.getSecurityHandler().login();

        String rapKey = "rap";
        String rapComponentId = rapKey + "@" + rapPlatformId;
        // generating the CSH
        //TODO configure rapCSH
        IComponentSecurityHandler rapCSH = ComponentSecurityHandlerFactory.getComponentSecurityHandler(
                serverAddress,
                KEY_STORE_PATH,
                KEY_STORE_PASSWORD,
                crmComponentId,
                serverAddress,
                false,
                rapPlatformOwnerUsername,
                rapPlatformOwnerPassword
        );
        //TODO add policy
        Map<String, IAccessPolicy> testAP = new HashMap<>();
        String testPolicyId = "testPolicyId";
        Map<String, String> requiredClaims = new HashMap<>();
        requiredClaims.put(Claims.ISSUER, SecurityConstants.CORE_AAM_INSTANCE_ID);
       // rapCSH.getSatisfiedPoliciesIdentifiers();

        //TODO check if policy is satisfied - shouldn't be using SecRequest from platform1

        //TODO make federation rule for platform 1
        //TODO get foreign token from core aam
        //TODO check if policy is satisfied - it should be
    }

}
