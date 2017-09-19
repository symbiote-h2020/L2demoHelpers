package helpers;

import eu.h2020.symbiote.security.commons.exceptions.custom.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;

import static helpers.Constants.*;
import static helpers.PlatformRegistrationHelper.registerPlatform;
import static helpers.PlatformRegistrationHelper.registerPlatformOwner;

public class L2demoRegistry {
    public static void main(String[] args) throws SecurityHandlerException, InvalidArgumentsException, ValidationException, NoSuchAlgorithmException, KeyManagementException, CertificateException, WrongCredentialsException, NotExistingUserException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {

        Log log = LogFactory.getLog(L2demoClient.class);

        try {
            registerPlatformOwner(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername, platformOwnerPassword, federatedId, recoveryMail,
                    rabbitHost, rabbitUsername, rabbitPassword, userManagementRequestQueue);

            registerPlatformOwner(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername2, platformOwnerPassword2, federatedId2, recoveryMail2,
                    rabbitHost, rabbitUsername, rabbitPassword, userManagementRequestQueue);

            registerPlatform(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername, platformOwnerPassword, platformInstanceFriendlyName,
                    platformInterworkingInterfaceAddress, platformId, rabbitHost, rabbitUsername, rabbitPassword, platformManagementRequestQueue);

            registerPlatform(AAMOwnerUsername, AAMOwnerPassword, platformOwnerUsername2, platformOwnerPassword2, platformInstanceFriendlyName2,
                    platformInterworkingInterfaceAddress2, platformId2, rabbitHost, rabbitUsername, rabbitPassword, platformManagementRequestQueue);
        } catch (IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(e.getCause());
        }
    }
}