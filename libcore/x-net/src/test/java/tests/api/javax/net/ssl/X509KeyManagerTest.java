package tests.api.javax.net.ssl;

import dalvik.annotation.TestTargetClass; 
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import javax.net.ssl.X509KeyManager;
import java.security.cert.X509Certificate;
import java.security.Principal;
import java.security.PrivateKey;
import java.net.Socket;

import junit.framework.TestCase;

import org.apache.harmony.xnet.tests.support.X509KeyManagerImpl;

/**
 * Tests for <code>X509KeyManager</code> class constructors and methods.
 */
@TestTargetClass(X509KeyManager.class) 
public class X509KeyManagerTest extends TestCase {
    
    /**
     * @tests X509KeyManager#getClientAliases(String keyType, Principal[] issuers) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getClientAliases",
        args = {java.lang.String.class, java.security.Principal[].class}
    )
    public void test_getClientAliases() {
        try {
            X509KeyManagerImpl xkm = new X509KeyManagerImpl("CLIENT");
            assertNull(xkm.getClientAliases(null, null));
            assertNull(xkm.getClientAliases("", null));
            String[] resArray = xkm.getClientAliases("CLIENT", null);
            assertTrue("Incorrect result", compareC(resArray));
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests X509KeyManager#chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "chooseClientAlias",
        args = {java.lang.String[].class, java.security.Principal[].class, java.net.Socket.class}
    )
    public void test_chooseClientAlias() {
        try {
            String[] ar = {"CLIENT"};
            X509KeyManagerImpl xkm = new X509KeyManagerImpl("CLIENT");
            assertNull(xkm.chooseClientAlias(null, null, new Socket()));
            assertNull(xkm.chooseClientAlias(new String[0], null, new Socket()));
            String res = xkm.chooseClientAlias(ar, null, new Socket());
            assertEquals(res, "clientalias_03");
            res = xkm.chooseClientAlias(ar, null, null);
            assertEquals(res, "clientalias_02");
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests X509KeyManager#getServerAliases(String keyType, Principal[] issuers) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getServerAliases",
        args = {java.lang.String.class, java.security.Principal[].class}
    )
    public void test_getServerAliases() {
        try {
            X509KeyManagerImpl xkm = new X509KeyManagerImpl("SERVER");
            assertNull(xkm.getServerAliases(null, null));
            assertNull(xkm.getServerAliases("", null));
            String[] resArray = xkm.getServerAliases("SERVER", null);
            assertEquals("Incorrect length", resArray.length, 1);
            assertEquals("Incorrect aliase", resArray[0], "serveralias_00");
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests X509KeyManager#chooseServerAlias(String keyType, Principal[] issuers, Socket socket) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "chooseServerAlias",
        args = {java.lang.String.class, java.security.Principal[].class, java.net.Socket.class}
    )
    public void test_chooseServerAlias() {
        try {
            X509KeyManagerImpl xkm = new X509KeyManagerImpl("SERVER");
            assertNull(xkm.chooseServerAlias(null, null, new Socket()));
            assertNull(xkm.chooseServerAlias("", null, new Socket()));
            assertNull(xkm.chooseServerAlias("SERVER", null, null));
            String res = xkm.chooseServerAlias("SERVER", null, new Socket());
            assertEquals(res, "serveralias_00");
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
   
    /**
     * @tests X509KeyManager#getCertificateChain(String alias) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificateChain",
        args = {java.lang.String.class}
    )
    public void test_getCertificateChain() {
        try {
            X509KeyManagerImpl xkm = new X509KeyManagerImpl("SERVER");
            assertNull("Not NULL for NULL parameter", xkm.getCertificateChain(null));
            assertNull("Not NULL for empty parameter",xkm.getCertificateChain(""));
            assertNull("Not NULL for clientAlias_01 parameter", xkm.getCertificateChain("clientAlias_01"));
            assertNull("Not NULL for serverAlias_00 parameter", xkm.getCertificateChain("serverAlias_00"));
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests X509KeyManager#getPrivateKey(String alias) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrivateKey",
        args = {java.lang.String.class}
    )
    public void test_getPrivateKey() {
        try {
            X509KeyManagerImpl xkm = new X509KeyManagerImpl("CLIENT");
            assertNull("Not NULL for NULL parameter", xkm.getPrivateKey(null));
            assertNull("Not NULL for serverAlias_00 parameter", xkm.getPrivateKey("serverAlias_00"));
            assertNull("Not NULL for clientAlias_02 parameter", xkm.getPrivateKey("clientAlias_02"));
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    
    private boolean compareC(String[] ar) {
        if (ar.length != 3) {
            return false;
        }
        for (int i = 0; i < ar.length; i++) {
            if (ar[i] != "clientalias_01" && ar[i] != "clientalias_02" && ar[i] != "clientalias_03") {
                return false;
            }
        }
        return true;
    }
}

