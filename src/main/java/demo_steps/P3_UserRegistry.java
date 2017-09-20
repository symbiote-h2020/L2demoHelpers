package demo_steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static helpers.Constants.*;
import static helpers.UserRegistrationHelper.registerUser;

public class P3_UserRegistry {
    public static void main(String[] args) {
        Log log = LogFactory.getLog(P3_UserRegistry.class);

        try {
            registerUser(AAMOwnerUsername,
                    AAMOwnerPassword,
                    username,
                    password,
                    federatedId3,
                    recoveryMail3,
                    platform1AAMServerAddress
            );
            log.info("User registration done");
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(e.getCause());
        }


    }
}
