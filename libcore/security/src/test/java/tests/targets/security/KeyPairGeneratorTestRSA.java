package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.KeyPairGenerators.RSA.class)
public class KeyPairGeneratorTestRSA extends KeyPairGeneratorTest {

    @SuppressWarnings("unchecked")
    public KeyPairGeneratorTestRSA() {
        super("RSA", new CipherAsymmetricCryptHelper("RSA"));
    }

}

