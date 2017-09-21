package demo_steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.RpcClient;
import eu.h2020.symbiote.security.commons.enums.ManagementStatus;
import eu.h2020.symbiote.security.commons.enums.OperationType;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.communication.payloads.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static demo_steps.P3_FederationInitialization.getConnection;
import static helpers.Constants.*;

public class P1_PlatformsRegistrationInCoreAAM {

    private static Log log = LogFactory.getLog(P1_PlatformsRegistrationInCoreAAM.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws
            IOException {

        Connection connection = null;
        try {
            connection =  getConnection(rabbitHost, rabbitUsername, rabbitPassword);
            registerPlatformOwner(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername, platformOwnerPassword, federatedId, recoveryMail,
                    connection, userManagementRequestQueue);

            registerPlatformOwner(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername2, platformOwnerPassword2, federatedId2, recoveryMail2,
                    connection, userManagementRequestQueue);

            registerPlatform(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername, platformOwnerPassword, platformInstanceFriendlyName,
                    platformInterworkingInterfaceAddress, platformId, connection, platformManagementRequestQueue);

            registerPlatform(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername2, platformOwnerPassword2, platformInstanceFriendlyName2,
                    platformInterworkingInterfaceAddress2, platformId2, connection , platformManagementRequestQueue);
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(e.getCause());
        } finally{
            if (connection!=null){
                connection.close();
            }

        }
    }


    private static PlatformManagementResponse registerPlatform(String AAMOwnerUsername, String AAMOwnerPassword, String platformOwnerUsername, String platformOwnerPassword,
                                                               String platformInstanceFriendlyName, String platformInterworkingInterfaceAddress, String platformId,
                                                               Connection connection, String platformManagementRequestQueue) throws
            IOException,
            TimeoutException {
        RpcClient platformManagementOverAMQPClient = null;
        try {
            platformManagementOverAMQPClient = new RpcClient(connection.createChannel(), "",
                    platformManagementRequestQueue, 5000);
        } catch (IOException e) {
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
        log.info("platform registration done");
        return platformRegistrationOverAMQPResponse;
    }

    private static ManagementStatus registerPlatformOwner(String AAMOwnerUsername, String AAMOwnerPassword, String platformOwnerUsername, String platformOwnerPassword,
                                                          String federatedId, String recoveryMail, Connection connection, String userManagementRequestQueue) throws
            IOException,
            TimeoutException {

        RpcClient userManagementOverAMQPClient = null;
        try {
            userManagementOverAMQPClient = new RpcClient(connection.createChannel(), "",
                    userManagementRequestQueue, 5000);
        } catch (IOException e) {
            log.error("Failed to open connection.");
        }

        UserManagementRequest userManagementRequest = new UserManagementRequest(new
                Credentials(AAMOwnerUsername, AAMOwnerPassword), new Credentials(),
                new UserDetails(new Credentials(platformOwnerUsername, platformOwnerPassword), federatedId, recoveryMail,
                        UserRole.PLATFORM_OWNER, new HashMap<>(), new HashMap<>()), OperationType.CREATE);

        byte[] response = userManagementOverAMQPClient.primitiveCall(mapper.writeValueAsString
                (userManagementRequest).getBytes());

        ManagementStatus managementStatus = mapper.readValue(response, ManagementStatus.class);
        log.info("Platform owner registration done");
        return managementStatus;
    }
}
