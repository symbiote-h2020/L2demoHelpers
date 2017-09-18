package helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.RpcClient;
import eu.h2020.symbiote.security.communication.payloads.Credentials;
import eu.h2020.symbiote.security.communication.payloads.FederationRule;
import eu.h2020.symbiote.security.communication.payloads.FederationRuleManagementRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class FederationRegistrationHelper {

    private static Log log = LogFactory.getLog(FederationRegistrationHelper.class);
    protected static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args){
        String federationId = "";
        Set<String> platformsIds = new HashSet<>();
        platformsIds.add("");

        String AAMOwnerUsername = "";
        String AAMOwnerPassword = "";

        String rabbitHost = "";
        String rabbitUsername = "";
        String rabbitPassword = "";
        String federationRuleManagementRequestQueue = "";

        Connection connection = null;
        RpcClient federationRuleManagementOverAMQPClient = null;
        try {
             connection = getConnection(rabbitHost,rabbitUsername,rabbitPassword);
             federationRuleManagementOverAMQPClient = new RpcClient(connection.createChannel(), "",
                    federationRuleManagementRequestQueue, 5000);
        } catch (IOException | TimeoutException e) {
            log.error("Failed to open connection.");
        }


        FederationRuleManagementRequest federationRuleManagementRequest = new FederationRuleManagementRequest(
                new Credentials(AAMOwnerUsername, AAMOwnerPassword),
                federationId,
                platformsIds,
                FederationRuleManagementRequest.OperationType.CREATE);

        try {
            byte[] response = federationRuleManagementOverAMQPClient.primitiveCall(mapper.writeValueAsString
                    (federationRuleManagementRequest).getBytes());
        } catch (IOException | TimeoutException e) {
            log.error("Fail during communication occured.");
        }



    }

    public static Connection getConnection(String rabbitHost, String rabbitUsername, String rabbitPassword) throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitHost);
            factory.setUsername(rabbitUsername);
            factory.setPassword(rabbitPassword);
            return factory.newConnection();
    }

}
