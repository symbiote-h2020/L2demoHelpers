package helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.RpcClient;
import eu.h2020.symbiote.security.commons.enums.OperationType;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.communication.payloads.Credentials;
import eu.h2020.symbiote.security.communication.payloads.UserDetails;
import eu.h2020.symbiote.security.communication.payloads.UserManagementRequest;
import eu.h2020.symbiote.security.helpers.PlatformAAMCertificateKeyStoreFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static helpers.FederationRegistrationHelper.getConnection;

public class UserRegistrationHelper {

    private static Log log = LogFactory.getLog(PlatformAAMCertificateKeyStoreFactory.class);
    protected static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        String AAMOwnerUsername = "";
        String AAMOwnerPassword = "";
        String username = "";
        String password = "";
        String federatedId = "";
        String recoveryMail = "";

        String rabbitHost = "";
        String rabbitUsername = "";
        String rabbitPassword = "";
        String userManagementRequestQueue = "";

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
                new UserDetails(new Credentials(username, password), federatedId, recoveryMail, UserRole.USER, new HashMap<>(), new HashMap<>()),
                OperationType.CREATE);

        try {
            byte[] response = userManagementOverAMQPClient.primitiveCall(mapper.writeValueAsString
                    (userManagementRequest).getBytes());
        } catch (IOException | TimeoutException e) {
            log.error("Communication failed");
        }
    }
}