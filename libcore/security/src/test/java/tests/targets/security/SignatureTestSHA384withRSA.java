package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.Signatures.SHA384withRSA.class)
public class SignatureTestSHA384withRSA extends SignatureTest {

    public SignatureTestSHA384withRSA() {
        super("SHA384withRSA", "RSA");
    }
}
