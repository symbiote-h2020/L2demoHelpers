package helpers;

import eu.h2020.symbiote.security.commons.enums.OperationType;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.commons.exceptions.custom.NotExistingUserException;
import eu.h2020.symbiote.security.commons.exceptions.custom.ValidationException;
import eu.h2020.symbiote.security.commons.exceptions.custom.WrongCredentialsException;
import eu.h2020.symbiote.security.communication.payloads.Credentials;
import eu.h2020.symbiote.security.communication.payloads.PlatformManagementRequest;
import eu.h2020.symbiote.security.helpers.PlatformAAMCertificateKeyStoreFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;

public class PlatformRegistrationHelper {

    private static Log log = LogFactory.getLog(PlatformAAMCertificateKeyStoreFactory.class);

    public static void main(String[] args) {
        String coreAAMAddress = "";
        String platformOwnerUsername = "";
        String platformOwnerPassword = "";
        String platformId = "";
        String keyStorePath = "";
        String keyStorePassword = "";
        String privateKeyPassword = "";
        String aamCertificateAlias = "";
        String rootCACertificateAlias = "";

        try {
            PlatformAAMCertificateKeyStoreFactory.getPlatformAAMKeystore(
                    coreAAMAddress,
                    platformOwnerUsername,
                    platformOwnerPassword,
                    platformId,
                    keyStorePath,
                    keyStorePassword,
                    rootCACertificateAlias,
                    aamCertificateAlias,
                    privateKeyPassword
            );
            log.info("OK");
        } catch (WrongCredentialsException | ValidationException | KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | InvalidArgumentsException | InvalidAlgorithmParameterException | NoSuchProviderException | NotExistingUserException e) {
            log.error(e.getMessage());
            log.error(e.getCause());
        }
    }

    public void registerPlatform(String coreAAMAddress, String platformOwnerUsername, String platformOwnerPassword, String platformId)
            throws IOException, TimeoutException {
        PlatformManagementRequest platformRegistrationOverAMQPRequest = new PlatformManagementRequest(new Credentials(AAMOwnerUsername,
                AAMOwnerPassword), new Credentials(platformOwnerUsername, platformOwnerPassword), platformInterworkingInterfaceAddress,
                platformInstanceFriendlyName, platformId, OperationType.CREATE);

        byte[] response = platformManagementOverAMQPClient.primitiveCall(mapper.writeValueAsString
                (platformRegistrationOverAMQPRequest).getBytes());

    }


}
