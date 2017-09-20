import eu.h2020.symbiote.security.commons.enums.ManagementStatus;
import eu.h2020.symbiote.security.communication.payloads.FederationRule;
import eu.h2020.symbiote.security.communication.payloads.PlatformManagementResponse;
import helpers.FederationRegistrationHelper;
import helpers.PlatformRegistrationHelper;
import helpers.UserRegistrationHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class HelpersTests {

    Log log = LogFactory.getLog(HelpersTests.class);

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
        String username = "testUsername";
        String password = "testPassword";
        String federatedId = "testFederatedId";
        String recoveryMail = "null@dev.null";

        ResponseEntity<ManagementStatus> response = UserRegistrationHelper.registerUser(
                AAMOwnerUsername,
                AAMOwnerPassword,
                username,
                password,
                federatedId,
                recoveryMail);
        assertEquals(ManagementStatus.OK, response.getBody());
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

        String federatedId = "testFederatedId";
        String recoveryMail = "null@dev.null";
        String userManagementRequestQueue = "symbIoTe-AuthenticationAuthorizationManager-manage_user_request";

        String rabbitHost = "localhost";
        String rabbitUsername = "guest";
        String rabbitPassword = "guest";
        String platformManagementRequestQueue = "symbIoTe-AuthenticationAuthorizationManager-manage_platform_request";

        ManagementStatus userResponse = PlatformRegistrationHelper.registerPlatformOwner(
                AAMOwnerUsername,
                AAMOwnerPassword,
                platformOwnerUsername,
                platformOwnerPassword,
                federatedId,
                recoveryMail,
                rabbitHost,
                rabbitUsername,
                rabbitPassword,
                userManagementRequestQueue
        );

        assertEquals(ManagementStatus.OK, userResponse);

        PlatformManagementResponse response = PlatformRegistrationHelper.registerPlatform(
                AAMOwnerUsername,
                AAMOwnerPassword,
                platformOwnerUsername,
                platformOwnerPassword,
                platformInstanceFriendlyName,
                platformInterworkingInterfaceAddress,
                platformId,
                rabbitHost,
                rabbitUsername,
                rabbitPassword,
                platformManagementRequestQueue);


        assertEquals(ManagementStatus.OK, response.getRegistrationStatus());
        assertEquals(platformId, response.getPlatformId());
    }


}
