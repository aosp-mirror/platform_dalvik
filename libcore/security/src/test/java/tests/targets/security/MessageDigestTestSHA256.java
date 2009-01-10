package tests.targets.security;

import dalvik.annotation.TestTargetClass;


@TestTargetClass(value=targets.MessageDigests.SHA_256.class)
public class MessageDigestTestSHA256 extends MessageDigestTest {

    public MessageDigestTestSHA256() {
        super("SHA-256");
    }



}
