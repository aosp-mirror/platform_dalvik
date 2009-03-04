package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.Signatures.NONEwithDSA.class)
public class SignatureTestNONEwithDSA extends SignatureTest {

    public SignatureTestNONEwithDSA() {
        super("NONEwithDSA", "DSA");
    }
}
