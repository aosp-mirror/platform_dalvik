package tests.targets.security;

import dalvik.annotation.TestTargetClass;

import java.security.spec.DSAParameterSpec;

@TestTargetClass(targets.AlgorithmParameterGenerators.DSA.class)
public class AlgorithmParameterGeneratorTestDSA extends
        AlgorithmParameterGeneratorTest {

    public AlgorithmParameterGeneratorTestDSA() {
        super("DSA", new AlgorithmParameterSignatureHelper<DSAParameterSpec>("DSA", DSAParameterSpec.class));
    }

    
}
