package tests.targets.security;

import dalvik.annotation.TestTargetClass;

import javax.crypto.spec.DHPrivateKeySpec;
import javax.crypto.spec.DHPublicKeySpec;

@TestTargetClass(targets.KeyFactories.DH.class)
public class KeyFactoryTestDH extends KeyFactoryTest<DHPublicKeySpec, DHPrivateKeySpec> {

    public KeyFactoryTestDH() {
        super("DH", new KeyAgreementHelper("DH"), DHPublicKeySpec.class, DHPrivateKeySpec.class);
    }

}
