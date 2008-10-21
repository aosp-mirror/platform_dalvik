package org.apache.harmony.security.tests.java.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureSpi;

import junit.framework.TestCase;

public class SignatureSpiTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testClone() {
        MySignatureSpi1 ss1 = new MySignatureSpi1();
        try {
            MySignatureSpi1 ssc1 = (MySignatureSpi1) ss1.clone();
            assertTrue(ss1 != ssc1);
        } catch (CloneNotSupportedException e) {
            fail("Unexpected CloneNotSupportedException " + e.getMessage());
        }

        
        MySignatureSpi2 ss2 = new MySignatureSpi2();
        try {
            ss2.clone();
            fail("CloneNotSupportedException expected ");
        } catch (CloneNotSupportedException e) {
            // expected
        }
    }

    class MySignatureSpi1 extends SignatureSpi implements Cloneable {
        public Object engineGetParameter(String param) {
            return null;
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
        
        public void engineInitSign(PrivateKey privateKey) {
        }

        public void engineInitVerify(PublicKey publicKey) {
        }

        public void engineSetParameter(String param, Object value) {
        }

        public byte[] engineSign() {
            return null;
        }

        public void engineUpdate(byte b) {
        }

        public void engineUpdate(byte[] b, int off, int len) {
        }

        public boolean engineVerify(byte[] sigBytes) {
            return false;
        }
    }

    class MySignatureSpi2 extends SignatureSpi {
        public Object engineGetParameter(String param) {
            return null;
        }
        
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
        
        public void engineInitSign(PrivateKey privateKey) {
        }

        public void engineInitVerify(PublicKey publicKey) {
        }

        public void engineSetParameter(String param, Object value) {
        }

        public byte[] engineSign() {
            return null;
        }

        public void engineUpdate(byte b) {
        }

        public void engineUpdate(byte[] b, int off, int len) {
        }

        public boolean engineVerify(byte[] sigBytes) {
            return false;
        }
    }
}
