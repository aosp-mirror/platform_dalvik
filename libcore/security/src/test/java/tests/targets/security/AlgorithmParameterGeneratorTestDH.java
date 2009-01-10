package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.AlgorithmParameterGenerators.DH.class)
public class AlgorithmParameterGeneratorTestDH extends
        AlgorithmParameterGeneratorTest {

    public AlgorithmParameterGeneratorTestDH() {
        super("DH", new AlgorithmParameterKeyAgreementHelper("DH"));
    }

}
