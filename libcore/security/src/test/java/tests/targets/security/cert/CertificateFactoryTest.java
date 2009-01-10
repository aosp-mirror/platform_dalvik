package tests.targets.security.cert;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public abstract class CertificateFactoryTest extends TestCase {

    private final String algorithmName;
    private final byte[] certificateData;


    public CertificateFactoryTest(String algorithmName, byte[] certificateData) {
        this.algorithmName = algorithmName;
        this.certificateData = certificateData;
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    @TestTargets({
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="getInstance",
                args={String.class}
        ),
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="generateCertificate",
                args={InputStream.class}
        ),
        @TestTargetNew(
                level=TestLevel.COMPLETE,
                method="method",
                args={}
        )
    })
    public void testCertificateFactory() {
        CertificateFactory certificateFactory = null;
        try {
            certificateFactory = CertificateFactory.getInstance(algorithmName);
        } catch (CertificateException e) {
            fail(e.getMessage());
        }

        Certificate certificate = null;
        try {
            certificate = certificateFactory
                    .generateCertificate(new ByteArrayInputStream(
                            certificateData));
        } catch (CertificateException e) {
            fail(e.getMessage());
        }

        assertNotNull(certificate);
    }
}
