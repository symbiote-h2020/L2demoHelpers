package demo_steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.RpcClient;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.communication.payloads.Credentials;
import eu.h2020.symbiote.security.communication.payloads.FederationRule;
import eu.h2020.symbiote.security.communication.payloads.FederationRuleManagementRequest;
import helpers.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class P3_FederationInitialization {

    private static Log log = LogFactory.getLog(P3_FederationInitialization.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        Set<String> platformsIds = new HashSet<>();
        platformsIds.add(SecurityConstants.CORE_AAM_INSTANCE_ID);
        platformsIds.add(Constants.platformId);
        platformsIds.add(Constants.platformId2);
        try {
            deleteFederation(
                Constants.federationId,
                Constants.AAMOwnerUsername,
                Constants.AAMOwnerPassword,
                Constants.rabbitHost,
                Constants.rabbitUsername,
                Constants.rabbitPassword,
                Constants.federationRuleManagementRequestQueue);
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(e.getCause());
        }
        try {
            registerFederation(
                    Constants.federationId,
                    platformsIds,
                    Constants.AAMOwnerUsername,
                    Constants.AAMOwnerPassword,
                    Constants.rabbitHost,
                    Constants.rabbitUsername,
                    Constants.rabbitPassword,
                    Constants.federationRuleManagementRequestQueue);
            log.info("OK");
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(e.getCause());
        }


    }
    private static Map<String, FederationRule> deleteFederation (String federationId,
                                                                 String aamOwnerUsername,
                                                                 String aamOwnerPassword,
                                                                 String rabbitHost,
                                                                 String rabbitUsername,
                                                                 String rabbitPassword,
                                                                 String federationRuleManagementRequestQueue)
            throws IOException, TimeoutException {

        Connection connection = getConnection(rabbitHost, rabbitUsername, rabbitPassword);
        RpcClient federationRuleManagementOverAMQPClient = new RpcClient(connection.createChannel(), "",
                federationRuleManagementRequestQueue, 5000);

        FederationRuleManagementRequest federationRuleManagementRequest = new FederationRuleManagementRequest(
                new Credentials(aamOwnerUsername, aamOwnerPassword),
                federationId,
                new HashSet<>(),
                FederationRuleManagementRequest.OperationType.DELETE);

        byte[] response = federationRuleManagementOverAMQPClient.primitiveCall(mapper.writeValueAsString
                (federationRuleManagementRequest).getBytes());
        HashMap<String, FederationRule> responseMap = mapper.readValue(response, new TypeReference<HashMap<String, FederationRule>>() {
        });
        log.info("Database with federations cleared.");
        connection.close();
        return responseMap;
    }

    private static Map<String, FederationRule> registerFederation(String federationId,
                                                                  Set<String> platformsIds,
                                                                  String aamOwnerUsername,
                                                                  String aamOwnerPassword,
                                                                  String rabbitHost,
                                                                  String rabbitUsername,
                                                                  String rabbitPassword,
                                                                  String federationRuleManagementRequestQueue)
            throws IOException,
            TimeoutException {
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
        connection.close();
        if (responseMap == null
                || responseMap.get(federationId) == null) {
            throw new SecurityException("Federation was not registered. Please, try again.");
        }
        log.info("Federation registered.");

        return responseMap;
    }

    static Connection getConnection(String rabbitHost, String rabbitUsername, String rabbitPassword) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setUsername(rabbitUsername);
        factory.setPassword(rabbitPassword);
        return factory.newConnection();
    }
}
