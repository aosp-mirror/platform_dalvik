package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.Signatures.MD5withRSA.class)
public class SignatureTestMD5withRSA extends SignatureTest {

    public SignatureTestMD5withRSA() {
        super("MD5withRSA", "RSA");
    }
}
