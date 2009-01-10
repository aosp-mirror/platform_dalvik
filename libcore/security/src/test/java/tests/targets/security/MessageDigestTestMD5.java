package tests.targets.security;

import dalvik.annotation.TestTargetClass;


@TestTargetClass(value=targets.MessageDigests.MD5.class)
public class MessageDigestTestMD5 extends MessageDigestTest {

    public MessageDigestTestMD5() {
        super("MD5");
    }



}
