package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.Signatures.SHA256withRSA.class)
public class SignatureTestSHA256withRSA extends SignatureTest {

    public SignatureTestSHA256withRSA() {
        super("SHA256withRSA", "RSA");
    }
}
