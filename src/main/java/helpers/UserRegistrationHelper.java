package helpers;

import eu.h2020.symbiote.security.commons.enums.OperationType;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.commons.exceptions.custom.WrongCredentialsException;
import eu.h2020.symbiote.security.communication.payloads.Credentials;
import eu.h2020.symbiote.security.communication.payloads.UserDetails;
import eu.h2020.symbiote.security.communication.payloads.UserManagementRequest;
import eu.h2020.symbiote.security.helpers.PlatformAAMCertificateKeyStoreFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class UserRegistrationHelper {

    private static Log log = LogFactory.getLog(PlatformAAMCertificateKeyStoreFactory.class);

    public static void main(String[] args) {
        String AAMOwnerUsername = "";
        String AAMOwnerPassword = "";
        String username = "";
        String password = "";
        String federatedId = "";
        String recoveryMail = "";
    }


    public void registerUser(String AAMOwnerUsername, String AAMOwnerPassword, String username, String password,
                             String federatedId, String recoveryMail) throws
            IOException, TimeoutException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException, InvalidArgumentsException, KeyStoreException,
            InvalidAlgorithmParameterException, NoSuchProviderException, OperatorCreationException,
            WrongCredentialsException {


        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put("testKey", "testAttribute");
        // issue app registration over AMQP
        byte[] response = appManagementClient.primitiveCall(mapper.writeValueAsString(new
                UserManagementRequest(new
                Credentials(AAMOwnerUsername, AAMOwnerPassword), new Credentials(),
                new UserDetails(new Credentials(username, password), federatedId, recoveryMail, UserRole.USER, attributesMap, new HashMap<>()),
                OperationType.CREATE)).getBytes());
    }
}