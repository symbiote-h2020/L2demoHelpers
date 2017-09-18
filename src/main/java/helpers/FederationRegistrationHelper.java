package helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.RpcClient;
import eu.h2020.symbiote.security.communication.payloads.Credentials;
import eu.h2020.symbiote.security.communication.payloads.FederationRule;
import eu.h2020.symbiote.security.communication.payloads.FederationRuleManagementRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class FederationRegistrationHelper {

    private static Log log = LogFactory.getLog(FederationRegistrationHelper.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        //TODO: fill all the proper fields
        String federationId = "";
        Set<String> platformsIds = new HashSet<>();
        platformsIds.add("");

        String AAMOwnerUsername = "";
        String AAMOwnerPassword = "";

        String rabbitHost = "";
        String rabbitUsername = "";
        String rabbitPassword = "";
        String federationRuleManagementRequestQueue = "";


        try {
            registerFederation(
                    federationId,
                    platformsIds,
                    AAMOwnerUsername,
                    AAMOwnerPassword,
                    rabbitHost,
                    rabbitUsername,
                    rabbitPassword,
                    federationRuleManagementRequestQueue);
            log.info("OK");
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(e.getCause());
        }


    }

    public static Map<String, FederationRule> registerFederation(String federationId, Set<String> platformsIds, String aamOwnerUsername, String aamOwnerPassword, String rabbitHost, String rabbitUsername, String rabbitPassword, String federationRuleManagementRequestQueue) throws IOException, TimeoutException {
        Connection connection = getConnection(rabbitHost, rabbitUsername, rabbitPassword);
        RpcClient federationRuleManagementOverAMQPClient = new RpcClient(connection.createChannel(), "",
                federationRuleManagementRequestQueue, 5000);

        log.info("Connection over AMQP established.");
        FederationRuleManagementRequest federationRuleManagementRequest = new FederationRuleManagementRequest(
                new Credentials(aamOwnerUsername, aamOwnerPassword),
                federationId,
                platformsIds,
                FederationRuleManagementRequest.OperationType.CREATE);
        log.info("FederationRuleManagementRequest constructed.");

        byte[] response = federationRuleManagementOverAMQPClient.primitiveCall(mapper.writeValueAsString
                (federationRuleManagementRequest).getBytes());

        log.info("Response from AAM acquired.");

        HashMap<String, FederationRule> responseMap = mapper.readValue(response, new TypeReference<HashMap<String, FederationRule>>() {
        });
        if (responseMap == null
                || responseMap.get(federationId) == null) {
            throw new SecurityException("Federation was not registered. Please, try again.");
        }
        log.info("Federation registered.");
        return responseMap;
    }

    private static Connection getConnection(String rabbitHost, String rabbitUsername, String rabbitPassword) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setUsername(rabbitUsername);
        factory.setPassword(rabbitPassword);
        return factory.newConnection();
    }

}
