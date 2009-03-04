package tests.targets.security.cert;

import dalvik.annotation.TestTargetClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

@TestTargetClass(targets.CertPathBuilders.PKIX.class)
public class CertPathBuilderTestPKIX extends CertPathBuilderTest {

    public CertPathBuilderTestPKIX() {
        super("PKIX");
    }

    @Override
    CertPathParameters getCertPathParameters() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            fail(e.getMessage());
        }

        try {
            keyStore.load(null, null);
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        } catch (CertificateException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        CertificateFactory certificateFactory = null;
        try {
            certificateFactory = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            fail(e.getMessage());
        }

        X509Certificate selfSignedcertificate = null;
        try {
            selfSignedcertificate = (X509Certificate) certificateFactory
                    .generateCertificate(new ByteArrayInputStream(
                            selfSignedCert.getBytes()));
        } catch (CertificateException e) {
            fail(e.getMessage());
        }

        try {
            keyStore.setCertificateEntry("selfSignedCert",
                    selfSignedcertificate);
        } catch (KeyStoreException e) {
            fail(e.getMessage());
        }

        X509CertSelector targetConstraints = new X509CertSelector();
        targetConstraints.setCertificate(selfSignedcertificate);

        List<Certificate> certList = new ArrayList<Certificate>();
        certList.add(selfSignedcertificate);
        CertStoreParameters storeParams = new CollectionCertStoreParameters(
                certList);


        CertStore certStore = null;
        try {
            certStore = CertStore.getInstance("Collection", storeParams);
        } catch (InvalidAlgorithmParameterException e) {
            fail(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }


        try {
            PKIXBuilderParameters parameters = new PKIXBuilderParameters(
                    keyStore, targetConstraints);
            parameters.addCertStore(certStore);
            parameters.setRevocationEnabled(false);
            return parameters;
        } catch (KeyStoreException e) {
            fail(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            fail(e.getMessage());
        }
        return null;
    }

    @Override
    void validateCertPath(CertPath path) {
        List<? extends Certificate> certificates = path.getCertificates();
        Certificate certificate = certificates.get(0);

        assertEquals("unexpected certificate type", "X.509", certificate
                .getType());

        X509Certificate x509Certificate = (X509Certificate) certificate;
        X500Principal subjectX500Principal = x509Certificate
                .getSubjectX500Principal();

        X500Principal expectedPrincipal = new X500Principal(
                "CN=Android CTS, OU=Android, O=Android, L=Android, ST=Android, C=AN");

        assertEquals("unexpected principal", expectedPrincipal,
                subjectX500Principal);
    }

    private String selfSignedCert = "-----BEGIN CERTIFICATE-----\n"
    + "MIICSDCCAbECBEk2ZvswDQYJKoZIhvcNAQEEBQAwazELMAkGA1UEBhMCQU4xEDAOBgNVBAgTB0Fu\n"
    + "ZHJvaWQxEDAOBgNVBAcTB0FuZHJvaWQxEDAOBgNVBAoTB0FuZHJvaWQxEDAOBgNVBAsTB0FuZHJv\n"
    + "aWQxFDASBgNVBAMTC0FuZHJvaWQgQ1RTMB4XDTA4MTIwMzExMDExNVoXDTM2MDQyMDExMDExNVow\n"
    + "azELMAkGA1UEBhMCQU4xEDAOBgNVBAgTB0FuZHJvaWQxEDAOBgNVBAcTB0FuZHJvaWQxEDAOBgNV\n"
    + "BAoTB0FuZHJvaWQxEDAOBgNVBAsTB0FuZHJvaWQxFDASBgNVBAMTC0FuZHJvaWQgQ1RTMIGfMA0G\n"
    + "CSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAMd+N1Bu2eiI4kukOLvFlpTSEHTGplN2vvw76T7jSZinx\n"
    + "WcrtLe6qH1uPffbVNW4/BRn6OywbcynazEdqEUa09hWtHYmUsXpRPyGUBScNnyF751SGA2JIQUfg\n"
    + "3gi3gT3h32Z64AIHnn5gsGDJkeWOHx6/uVOV7iqr7cwPdLp03QIDAQABMA0GCSqGSIb3DQEBBAUA\n"
    + "A4GBAGG46Udsh6U7bSkJsyPPmSCCEkGr14L8F431UuaWbLvQVDtyPv8vtdJilyUTVnlWM6JNGV/q\n"
    + "bgHuLbohkVXn9l68GtgQ7QDexHJE5hEDG/S7cYNi9GhrCfzAjEed13VMntZHZ0XQ4E7jBOmhcMAY\n"
    + "DC9BBx1sVKoji17RP4R8CTf1\n" + "-----END CERTIFICATE-----";
}
