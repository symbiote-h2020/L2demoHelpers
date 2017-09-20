package demo_steps;

import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.commons.exceptions.custom.NotExistingUserException;
import eu.h2020.symbiote.security.commons.exceptions.custom.ValidationException;
import eu.h2020.symbiote.security.commons.exceptions.custom.WrongCredentialsException;
import eu.h2020.symbiote.security.helpers.ECDSAHelper;
import eu.h2020.symbiote.security.helpers.PlatformAAMCertificateKeyStoreFactory;
import helpers.Constants;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;


public class P2_Platform1AAMKeyStoreGeneration {

    public static void main(String[] args) throws IOException, CertificateException, NoSuchAlgorithmException, ValidationException, InvalidArgumentsException, InvalidAlgorithmParameterException, NotExistingUserException, NoSuchProviderException, WrongCredentialsException, KeyStoreException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        ECDSAHelper.enableECDSAProvider();

        PlatformAAMCertificateKeyStoreFactory.getPlatformAAMKeystore(
                Constants.coreAAMServerAddress,
                Constants.platformOwnerUsername,
                Constants.platformOwnerPassword,
                Constants.platformId,
                Constants.KEY_STORE_PATH,
                Constants.KEY_STORE_PASSWORD,
                Constants.rootCACertificateAlias,
                Constants.aamCertificateAlias,
                Constants.PV_KEY_PASSWORD);

    }
}
