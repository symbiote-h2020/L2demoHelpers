import eu.h2020.symbiote.security.communication.payloads.FederationRule;
import helpers.FederationRegistrationHelper;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertTrue;


public class HelpersTests {

    @Test
    public void FederationRegistrationHelperTest() throws IOException, TimeoutException {

        String federationId = "federationTest";
        Set<String> platformsIds = new HashSet<>();
        platformsIds.add("testPlatform");

        String AAMOwnerUsername = "AAMOwner";
        String AAMOwnerPassword = "AAMPassword";

        String rabbitHost = "localhost";
        String rabbitUsername = "guest";
        String rabbitPassword = "guest";
        String federationRuleManagementRequestQueue = "symbIoTe-AuthenticationAuthorizationManager-federation_rule_manage_request";

        Map<String, FederationRule> responseMap = FederationRegistrationHelper.registerFederation(
                federationId,
                platformsIds,
                AAMOwnerUsername,
                AAMOwnerPassword,
                rabbitHost,
                rabbitUsername,
                rabbitPassword,
                federationRuleManagementRequestQueue);

        assertTrue(responseMap.containsKey(federationId));
    }
}
