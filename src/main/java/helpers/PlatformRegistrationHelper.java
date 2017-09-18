package helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.RpcClient;
import eu.h2020.symbiote.security.commons.enums.OperationType;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.communication.payloads.*;
import eu.h2020.symbiote.security.helpers.PlatformAAMCertificateKeyStoreFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static helpers.FederationRegistrationHelper.getConnection;

public class PlatformRegistrationHelper {

    private static Log log = LogFactory.getLog(PlatformAAMCertificateKeyStoreFactory.class);
    protected static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        //TODO: fill all the proper fields

        registerPlatformOwner();

        String AAMOwnerUsername = "";
        String AAMOwnerPassword = "";
        String platformId = "";
        String platformOwnerUsername = "";
        String platformOwnerPassword = "";
        String platformInstanceFriendlyName = "";
        String platformInterworkingInterfaceAddress = "";

        String rabbitHost = "";
        String rabbitUsername = "";
        String rabbitPassword = "";
        String platformManagementRequestQueue = "";

        try {
            registerPlatform(AAMOwnerUsername,
                    AAMOwnerPassword,
                    platformId,
                    platformOwnerUsername,
                    platformOwnerPassword,
                    platformInstanceFriendlyName,
                    platformInterworkingInterfaceAddress,
                    rabbitHost,
                    rabbitUsername,
                    rabbitPassword,
                    platformManagementRequestQueue
            );
            log.info("Done");
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(e.getCause());
        }


    }

    public static PlatformManagementResponse registerPlatform(String AAMOwnerUsername, String AAMOwnerPassword, String platformOwnerUsername, String platformOwnerPassword,
                                                              String platformId, String platformInstanceFriendlyName, String platformInterworkingInterfaceAddress,
                                                              String rabbitHost, String rabbitUsername, String rabbitPassword, String platformManagementRequestQueue) throws IOException, TimeoutException {
        Connection connection = null;
        RpcClient platformManagementOverAMQPClient = null;
        try {
            connection = getConnection(rabbitHost, rabbitUsername, rabbitPassword);
            platformManagementOverAMQPClient = new RpcClient(connection.createChannel(), "",
                    platformManagementRequestQueue, 5000);
        } catch (IOException | TimeoutException e) {
            log.error("Failed to open connection.");
        }

        PlatformManagementRequest platformManagementRequest = new PlatformManagementRequest(new Credentials(AAMOwnerUsername,
                AAMOwnerPassword), new Credentials(platformOwnerUsername, platformOwnerPassword), platformInterworkingInterfaceAddress,
                platformInstanceFriendlyName, platformId, OperationType.CREATE);

        byte[] response = platformManagementOverAMQPClient.primitiveCall(mapper.writeValueAsString
                (platformManagementRequest).getBytes());
        if (response == null) {
            throw new SecurityException("Platform not registered.");
        }
        PlatformManagementResponse platformRegistrationOverAMQPResponse = mapper.readValue(response,
                PlatformManagementResponse.class);

        return platformRegistrationOverAMQPResponse;
    }

    public static void registerPlatformOwner() {
        String AAMOwnerUsername = "AAMOwner";
        String AAMOwnerPassword = "AAMOwner";
        String username = "testPOUsername";
        String password = "testPOpassword";
        String federatedId = "testPOFederatedId";
        String recoveryMail = "testRecoveryMail";

        String rabbitHost = "localhost";
        String rabbitUsername = "guest";
        String rabbitPassword = "guest";
        String userManagementRequestQueue = "symbIoTe-AuthenticationAuthorizationManager-manage_user_request";

        Connection connection = null;
        RpcClient userManagementOverAMQPClient = null;
        try {
            connection = getConnection(rabbitHost, rabbitUsername, rabbitPassword);
            userManagementOverAMQPClient = new RpcClient(connection.createChannel(), "",
                    userManagementRequestQueue, 5000);
        } catch (IOException | TimeoutException e) {
            log.error("Failed to open connection.");
        }

        UserManagementRequest userManagementRequest = new UserManagementRequest(new
                Credentials(AAMOwnerUsername, AAMOwnerPassword), new Credentials(),
                new UserDetails(new Credentials(username, password), federatedId, recoveryMail,
                        UserRole.PLATFORM_OWNER, new HashMap<>(), new HashMap<>()), OperationType.CREATE);

        try {
            byte[] response = userManagementOverAMQPClient.primitiveCall(mapper.writeValueAsString
                    (userManagementRequest).getBytes());
        } catch (IOException | TimeoutException e) {
            log.error("Communication failed");
        }
    }


}
