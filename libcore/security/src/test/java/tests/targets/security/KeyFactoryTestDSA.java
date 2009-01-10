package tests.targets.security;

import dalvik.annotation.TestTargetClass;

import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;

@TestTargetClass(targets.KeyFactories.DSA.class)
public class KeyFactoryTestDSA extends
        KeyFactoryTest<DSAPublicKeySpec, DSAPrivateKeySpec> {

    public KeyFactoryTestDSA() {
        super("DSA", new SignatureHelper("DSA"), DSAPublicKeySpec.class, DSAPrivateKeySpec.class);
    }

}
