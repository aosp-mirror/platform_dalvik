package tests.api.javax.net.ssl;

import dalvik.annotation.BrokenTest;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSessionContext;

import java.security.NoSuchAlgorithmException;
   
/**
 * Tests for <code>SSLSessionContext</code> class constructors and methods.
 */
@TestTargetClass(SSLSessionContext.class) 
public class SSLSessionContextTest extends TestCase {
    
    /**
     * @throws NoSuchAlgorithmException 
     * @tests javax.net.ssl.SSLSessionContex#getSessionCacheSize()
     * @tests javax.net.ssl.SSLSessionContex#setSessionCacheSize(int size)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSessionCacheSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSessionCacheSize",
            args = {int.class}
        )
    })
    @BrokenTest("getClientSessionContext returns null on android but does not on RI")
    public final void test_sessionCacheSize() throws NoSuchAlgorithmException {
        SSLSessionContext sc = SSLContext.getInstance("TLS")
                .getClientSessionContext();
        sc.setSessionCacheSize(10);
        assertEquals("10 wasn't returned", 10, sc.getSessionCacheSize());
        sc.setSessionCacheSize(5);
        assertEquals("5 wasn't returned", 5, sc.getSessionCacheSize());
        
        try {
            sc.setSessionCacheSize(-1);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    /**
     * @throws NoSuchAlgorithmException 
     * @tests javax.net.ssl.SSLSessionContex#getSessionTimeout()
     * @tests javax.net.ssl.SSLSessionContex#setSessionTimeout(int seconds)
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSessionTimeout",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSessionTimeout",
            args = {int.class}
        )
    })
    @BrokenTest("getClientSessionContext returns null on android but does not on RI")
    public final void test_sessionTimeout() throws NoSuchAlgorithmException {
        SSLSessionContext sc = SSLContext.getInstance("TLS")
                .getClientSessionContext();
        sc.setSessionTimeout(100);
        assertEquals("100 wasn't returned", 100, sc.getSessionTimeout());
        sc.setSessionTimeout(5000);
        assertEquals("5000 wasn't returned", 5000, sc.getSessionTimeout());
        
        try {
            sc.setSessionTimeout(-1);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    /**
     * @throws NoSuchAlgorithmException 
     * @tests javax.net.ssl.SSLSessionContex#getSession(byte[] sessionId)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSession",
        args = {byte[].class}
    )
    @BrokenTest("getClientSessionContext returns null on android but does not on RI")
    public final void test_getSession() throws NoSuchAlgorithmException {
        SSLSessionContext sc = SSLContext.getInstance("TLS")
                .getClientSessionContext();
        try {
            sc.getSession(null);
        } catch (NullPointerException e) {
            // expected
        }
        assertNull(sc.getSession(new byte[5]));
    }
    
    /**
     * @throws NoSuchAlgorithmException 
     * @tests javax.net.ssl.SSLSessionContex#getIds()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIds",
        args = {}
    )
    @BrokenTest("getClientSessionContext returns null on android but does not on RI")
    public final void test_getIds() throws NoSuchAlgorithmException {
        SSLSessionContext sc = SSLContext.getInstance("TLS")
                .getClientSessionContext();
        assertFalse(sc.getIds().hasMoreElements());
    }

}