package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.AlgorithmParameterGenerators.AES.class)
public class AlgorithmParameterGeneratorTestAES extends
        AlgorithmParameterGeneratorTest {

    public AlgorithmParameterGeneratorTestAES() {
        super("AES", new AlgorithmParameterSymmetricHelper("AES", "CBC/PKCS5PADDING", 128));
    }

}
