package tests.api.javax.net.ssl;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.net.ssl.SSLSessionContext;
import org.apache.harmony.xnet.tests.support.SSLSessionContextImpl;
   
/**
 * Tests for <code>SSLSessionContext</code> class constructors and methods.
 */
@TestTargetClass(SSLSessionContext.class) 
public class SSLSessionContextTest extends TestCase {
    
    /**
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
    public final void test_sessionCacheSize() {
        SSLSessionContextImpl sc = new SSLSessionContextImpl();
        try {
            assertEquals("0 wasn't returned", 0, sc.getSessionCacheSize());
            sc.setSessionCacheSize(10);
            assertEquals("10 wasn't returned", 10, sc.getSessionCacheSize());
            sc.setSessionCacheSize(5);
            assertEquals("5 wasn't returned", 5, sc.getSessionCacheSize());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
        
        try {
            sc.setSessionCacheSize(-1);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    /**
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
    public final void test_sessionTimeout() {
        SSLSessionContextImpl sc = new SSLSessionContextImpl();
        try {
            assertEquals("0 wasn't returned", 0, sc.getSessionTimeout());
            sc.setSessionTimeout(100);
            assertEquals("100 wasn't returned", 100, sc.getSessionTimeout());
            sc.setSessionTimeout(5000);
            assertEquals("5000 wasn't returned", 5000, sc.getSessionTimeout());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
        
        try {
            sc.setSessionTimeout(-1);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSessionContex#getSession(byte[] sessionId)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSession",
        args = {byte[].class}
    )
    public final void test_getSession() {
        SSLSessionContextImpl sc = new SSLSessionContextImpl();
        try {
            assertNull(sc.getSession(null));
            assertNull(sc.getSession(new byte[5]));
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSessionContex#getIds()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIds",
        args = {}
    )
    public final void test_getIds() {
        SSLSessionContextImpl sc = new SSLSessionContextImpl();
        try {
            assertNull(sc.getIds());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

}