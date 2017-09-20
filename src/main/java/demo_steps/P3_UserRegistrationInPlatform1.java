package demo_steps;

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

import java.util.HashMap;

import static helpers.Constants.*;

public class P3_UserRegistrationInPlatform1 {
    public static void main(String[] args) {
        Log log = LogFactory.getLog(P3_UserRegistrationInPlatform1.class);

        IAAMClient aamClient = new AAMClient(platform1AAMServerAddress);

        UserManagementRequest userManagementRequest = new UserManagementRequest(new
                Credentials(AAMOwnerUsername, AAMOwnerPassword), new Credentials(),
                new UserDetails(new Credentials(username, password), federatedId, recoveryMail, UserRole.USER, new HashMap<>(), new HashMap<>()),
                OperationType.CREATE);

        try {
            aamClient.manageUser(userManagementRequest);
            log.info("User registration done");
        } catch (AAMException e) {
            log.error(e);
        }


    }
}
