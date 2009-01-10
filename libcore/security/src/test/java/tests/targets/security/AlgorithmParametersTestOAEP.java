package tests.targets.security;

import dalvik.annotation.TestTargetClass;

import java.security.spec.MGF1ParameterSpec;

import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

@TestTargetClass(targets.AlgorithmParameters.OAEP.class)
public class AlgorithmParametersTestOAEP extends AlgorithmParametersTest {

    public AlgorithmParametersTestOAEP() {
        super("OAEP", new AlgorithmParameterAsymmetricHelper("RSA"), new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
    }

}
