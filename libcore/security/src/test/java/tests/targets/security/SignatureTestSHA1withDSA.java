package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.Signatures.SHA1withDSA.class)
public class SignatureTestSHA1withDSA extends SignatureTest {

    public SignatureTestSHA1withDSA() {
        super("SHA1withDSA", "DSA");
    }
}
