package tests.targets.security.cert;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;

public abstract class CertPathValidatorTest extends TestCase {

    private final String algorithmName;


    public CertPathValidatorTest(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    abstract CertPathParameters getParams();
    abstract CertPath getCertPath();
    abstract void validateResult(CertPathValidatorResult validatorResult);

    @TestTargets({
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="getInstance",
                args={String.class}
        ),
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="validate",
                args={CertPath.class, CertPathParameters.class}
        ),
        @TestTargetNew(
                level=TestLevel.COMPLETE,
                method="method",
                args={}
        )
    })
    public void testCertPathValidator() {
        CertPathValidator certPathValidator = null;
        try {
            certPathValidator = CertPathValidator.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }

        CertPathValidatorResult validatorResult = null;
        try {
            validatorResult = certPathValidator.validate(getCertPath(),
                    getParams());
        } catch (CertPathValidatorException e) {
            fail(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            fail(e.getMessage());
        }

        validateResult(validatorResult);
    }


}
