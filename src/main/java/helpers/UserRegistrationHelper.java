package helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.security.commons.enums.ManagementStatus;
import eu.h2020.symbiote.security.commons.enums.OperationType;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.commons.exceptions.custom.AAMException;
import eu.h2020.symbiote.security.communication.AAMClient;
import eu.h2020.symbiote.security.communication.IAAMClient;
import eu.h2020.symbiote.security.communication.payloads.Credentials;
import eu.h2020.symbiote.security.communication.payloads.UserDetails;
import eu.h2020.symbiote.security.communication.payloads.UserManagementRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static helpers.Constants.platform1AAMServerAddress;

public class UserRegistrationHelper {

    private static Log log = LogFactory.getLog(UserRegistrationHelper.class);
    protected static ObjectMapper mapper = new ObjectMapper();


    public static void main(String[] args) {
        //TODO: fill all the proper fields
        String AAMOwnerUsername = Constants.AAMOwnerUsername;
        String AAMOwnerPassword = Constants.AAMOwnerPassword;
        String username = Constants.username;
        String password = Constants.password;
        String federatedId = Constants.federatedId3;
        String recoveryMail = Constants.recoveryMail3;

        try {
            registerUser(AAMOwnerUsername,
                    AAMOwnerPassword,
                    username,
                    password,
                    federatedId,
                    recoveryMail,
                    platform1AAMServerAddress);
            log.info("Done");
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(e.getCause());
        }
    }


    public static ResponseEntity<ManagementStatus> registerUser(String AAMOwnerUsername, String AAMOwnerPassword, String username, String password,
                                                                String federatedId, String recoveryMail, String serverAddress) throws IOException, TimeoutException {
        IAAMClient aamClient = new AAMClient(serverAddress);

        UserManagementRequest userManagementRequest = new UserManagementRequest(new
                Credentials(AAMOwnerUsername, AAMOwnerPassword), new Credentials(),
                new UserDetails(new Credentials(username, password), federatedId, recoveryMail, UserRole.USER, new HashMap<>(), new HashMap<>()),
                OperationType.CREATE);

        try {
            ManagementStatus managementStatus = aamClient.manageUser(userManagementRequest);
            log.info("User registration done");
            return ResponseEntity.status(HttpStatus.OK).body(managementStatus);
        } catch (AAMException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ManagementStatus.ERROR);
        }
    }
}