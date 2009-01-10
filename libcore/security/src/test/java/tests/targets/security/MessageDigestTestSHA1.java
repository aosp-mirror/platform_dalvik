package tests.targets.security;

import dalvik.annotation.TestTargetClass;


@TestTargetClass(value=targets.MessageDigests.SHA_1.class)
public class MessageDigestTestSHA1 extends MessageDigestTest {

    public MessageDigestTestSHA1() {
        super("SHA-1");
    }



}
