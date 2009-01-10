package tests.targets.security;

import dalvik.annotation.TestTargetClass;

import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

@TestTargetClass(targets.KeyFactories.RSA.class)
public class KeyFactoryTestRSA extends
        KeyFactoryTest<RSAPublicKeySpec, RSAPrivateKeySpec> {

    @SuppressWarnings("unchecked")
    public KeyFactoryTestRSA() {
        super("RSA", new CipherAsymmetricCryptHelper("RSA"), RSAPublicKeySpec.class, RSAPrivateKeySpec.class);
    }

}
