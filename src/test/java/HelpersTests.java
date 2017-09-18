import eu.h2020.symbiote.security.commons.enums.ManagementStatus;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.communication.payloads.FederationRule;
import eu.h2020.symbiote.security.communication.payloads.PlatformManagementResponse;
import helpers.FederationRegistrationHelper;
import helpers.PlatformRegistrationHelper;
import helpers.UserRegistrationHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class HelpersTests {

    @Before
    public void setUp() {
    }

    @Test
    public void FederationRegistrationHelperTest() throws IOException, TimeoutException {

        String federationId = "federationTest5";
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

    @Test
    public void UserRegistrationHelperTest() throws IOException, TimeoutException {

        String AAMOwnerUsername = "AAMOwner";
        String AAMOwnerPassword = "AAMPassword";
        String username = "testPOUsername";
        String password = "testPOPassword";
        String federatedId = "testFederatedId";
        String recoveryMail = "null@dev.null";

        String rabbitHost = "localhost";
        String rabbitUsername = "guest";
        String rabbitPassword = "guest";
        String userManagementRequestQueue = "symbIoTe-AuthenticationAuthorizationManager-manage_user_request";

        ManagementStatus response = UserRegistrationHelper.registerUser(
                AAMOwnerUsername,
                AAMOwnerPassword,
                username,
                password,
                federatedId,
                recoveryMail,
                rabbitHost,
                rabbitUsername,
                rabbitPassword,
                userManagementRequestQueue);

        assertEquals(ManagementStatus.OK, response);
    }

    @Test
    public void PlatformRegistrationHelperTest() throws IOException, TimeoutException {

        String AAMOwnerUsername = "AAMOwner";
        String AAMOwnerPassword = "AAMPassword";
        String platformId = "testPlatformId";
        String platformOwnerUsername = "testPOUsername";
        String platformOwnerPassword = "testPOPassword";
        String platformInstanceFriendlyName = "testPlatformInstanceFriendlyName";
        String platformInterworkingInterfaceAddress = "testPlatformInterworkingInterfaceAddress";

        String rabbitHost = "localhost";
        String rabbitUsername = "guest";
        String rabbitPassword = "guest";
        String platformManagementRequestQueue = "symbIoTe-AuthenticationAuthorizationManager-manage_platform_request";

        PlatformManagementResponse response = PlatformRegistrationHelper.registerPlatform(
                AAMOwnerUsername,
                AAMOwnerPassword,
                platformId,
                platformOwnerUsername,
                platformOwnerPassword,
                platformInstanceFriendlyName,
                platformInterworkingInterfaceAddress,
                rabbitHost,
                rabbitUsername,
                rabbitPassword,
                platformManagementRequestQueue);

        assertEquals(ManagementStatus.OK, response.getRegistrationStatus());
        assertEquals(platformId, response.getPlatformId());
    }


}
