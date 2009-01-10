package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.Signatures.MD2withRSA.class)
public class SignatureTestMD2withRSA extends SignatureTest {

    public SignatureTestMD2withRSA() {
        super("MD2withRSA", "RSA");
    }
}
