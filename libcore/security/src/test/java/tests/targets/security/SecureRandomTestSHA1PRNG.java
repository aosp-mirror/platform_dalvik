package tests.targets.security;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.SecureRandoms.SHAPRNG1.class)
public class SecureRandomTestSHA1PRNG extends SecureRandomTest {

    public SecureRandomTestSHA1PRNG() {
        super("SHA1PRNG");
    }

}
