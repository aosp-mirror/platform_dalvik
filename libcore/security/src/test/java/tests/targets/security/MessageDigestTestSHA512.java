package tests.targets.security;

import dalvik.annotation.TestTargetClass;


@TestTargetClass(value=targets.MessageDigests.SHA_512.class)
public class MessageDigestTestSHA512 extends MessageDigestTest {

    public MessageDigestTestSHA512() {
        super("SHA-512");
    }



}
