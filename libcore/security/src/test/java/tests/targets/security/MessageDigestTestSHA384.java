package tests.targets.security;

import dalvik.annotation.TestTargetClass;


@TestTargetClass(value=targets.MessageDigests.SHA_384.class)
public class MessageDigestTestSHA384 extends MessageDigestTest {

    public MessageDigestTestSHA384() {
        super("SHA-384");
    }



}
