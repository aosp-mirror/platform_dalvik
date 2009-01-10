package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.KeyPairGenerators.DSA.class)
public class KeyPairGeneratorTestDSA extends KeyPairGeneratorTest {

    public KeyPairGeneratorTestDSA() {
        super("DSA", new SignatureHelper("DSA"));
    }

}
