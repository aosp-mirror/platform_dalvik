package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.KeyPairGenerators.DH.class)
public class KeyPairGeneratorTestDH extends KeyPairGeneratorTest {

    public KeyPairGeneratorTestDH() {
        super("DH", new KeyAgreementHelper("DH"));
    }

}
