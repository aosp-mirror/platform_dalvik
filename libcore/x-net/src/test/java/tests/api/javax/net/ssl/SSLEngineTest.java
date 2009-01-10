/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests.api.javax.net.ssl;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.AndroidOnly;

import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLEngineResult;
import java.nio.ReadOnlyBufferException;

import junit.framework.TestCase;


/**
 * Tests for SSLEngine class
 * 
 */
@TestTargetClass(SSLEngine.class) 
public class SSLEngineTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SSLEngineTest.class);
    }

    /**
     * Test for <code>SSLEngine()</code> constructor Assertion: creates
     * SSLEngine object with null host and -1 port
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLEngine",
        args = {}
    )
    public void test_Constructor() {
        SSLEngine e = new mySSLEngine();
        assertNull(e.getPeerHost());
        assertEquals(-1, e.getPeerPort());
        String[] suites = { "a", "b", "c" };
        e.setEnabledCipherSuites(suites);
        assertEquals(e.getEnabledCipherSuites().length, suites.length);
    }

    /**
     * Test for <code>SSLEngine(String host, int port)</code> constructor
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verification with incorrect parameters missed",
        method = "SSLEngine",
        args = {java.lang.String.class, int.class}
    )
    public void test_ConstructorLjava_lang_StringI01() throws SSLException {
        int port = 1010;
        SSLEngine e = new mySSLEngine(null, port);
        assertNull(e.getPeerHost());
        assertEquals(e.getPeerPort(), port);
        try {
            e.beginHandshake();
        } catch (SSLException ex) {
        }
    }

    /**
     * Test for <code>SSLEngine(String host, int port)</code> constructor
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verification with incorrect parameters missed",
        method = "SSLEngine",
        args = {java.lang.String.class, int.class}
    )
    public void test_ConstructorLjava_lang_StringI02() {
        String host = "new host";
        int port = 8080;
        SSLEngine e = new mySSLEngine(host, port);
        assertEquals(e.getPeerHost(), host);
        assertEquals(e.getPeerPort(), port);
        String[] suites = { "a", "b", "c" };
        e.setEnabledCipherSuites(suites);
        assertEquals(e.getEnabledCipherSuites().length, suites.length);
        e.setUseClientMode(true);
        assertTrue(e.getUseClientMode());
    }

    /**
     * Test for <code>getPeerHost()</code> method
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPeerHost",
        args = {}
    )
    public void test_getPeerHost() {
        SSLEngine e = new mySSLEngine();
        assertNull(e.getPeerHost());
        e = new mySSLEngine("www.fortify.net", 80);
        assertEquals("Incorrect host name", "www.fortify.net", e.getPeerHost());
    }
    
    /**
     * Test for <code>getPeerPort()</code> method
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPeerPort",
        args = {}
    )
    public void test_getPeerPort() {
        SSLEngine e = new mySSLEngine();
        assertEquals("Incorrect default value of peer port",
                -1 ,e.getPeerPort());
        e = new mySSLEngine("www.fortify.net", 80);
        assertEquals("Incorrect peer port", 80, e.getPeerPort());
    }

    /**
     * @tests javax.net.ssl.SSLEngine#getSupportedProtocols()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedProtocols",
        args = {}
    )
    public void test_getSupportedProtocols() {
        mySSLEngine sse = new mySSLEngine();
        try {
            String[] res = sse.getSupportedProtocols();
            assertNotNull(res);
            assertEquals(res.length, 0);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#setEnabledProtocols(String[] protocols)
     * @tests javax.net.ssl.SSLEngine#getEnabledProtocols()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getEnabledProtocols",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setEnabledProtocols",
            args = {String[].class}
        )
    })
    public void test_EnabledProtocols() {
        mySSLEngine sse = new mySSLEngine();
        String[] pr = {"Protocil_01", "Protocol_02"};
        try {
            sse.setEnabledProtocols(pr);
            String[] res = sse.getEnabledProtocols();
            assertNotNull("Null array was returned", res);
            assertEquals("Incorrect array length", res.length, pr.length);
            assertEquals("Incorrect array was returned", res, pr);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        try {
            sse.setEnabledProtocols(null);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#getSupportedCipherSuites()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedCipherSuites",
        args = {}
    )
    public void test_getSupportedCipherSuites() {
        mySSLEngine sse = new mySSLEngine();
        try {
            String[] res = sse.getSupportedCipherSuites();
            assertNotNull(res);
            assertEquals(res.length, 0);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#setEnabledCipherSuites(String[] suites)
     * @tests javax.net.ssl.SSLEngine#getEnabledCipherSuites()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setEnabledCipherSuites",
            args = {String[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getEnabledCipherSuites",
            args = {}
        )
    })
    public void test_EnabledCipherSuites() {
        mySSLEngine sse = new mySSLEngine();
        String[] st = {"Suite_01", "Suite_02"};
        try {
            sse.setEnabledCipherSuites(st);
            String[] res = sse.getEnabledCipherSuites();
            assertNotNull("Null array was returned", res);
            assertEquals("Incorrect array length", res.length, st.length);
            assertEquals("Incorrect array was returned", res, st);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        try {
            sse.setEnabledCipherSuites(null);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#setEnableSessionCreation(boolean flag)
     * @tests javax.net.ssl.SSLEngine#getEnableSessionCreation()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setEnableSessionCreation",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getEnableSessionCreation",
            args = {}
        )
    })
    public void test_EnableSessionCreation() {
        mySSLEngine sse = new mySSLEngine();
        try {
            assertTrue(sse.getEnableSessionCreation());
            sse.setEnableSessionCreation(false);
            assertFalse(sse.getEnableSessionCreation());
            sse.setEnableSessionCreation(true);
            assertTrue(sse.getEnableSessionCreation());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#setNeedClientAuth(boolean need)
     * @tests javax.net.ssl.SSLEngine#getNeedClientAuth()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setNeedClientAuth",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getNeedClientAuth",
            args = {}
        )
    })
    public void test_NeedClientAuth() {
        mySSLEngine sse = new mySSLEngine();
        try {
            sse.setNeedClientAuth(false);
            assertFalse(sse.getNeedClientAuth());
            sse.setNeedClientAuth(true);
            assertTrue(sse.getNeedClientAuth());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#setWantClientAuth(boolean want)
     * @tests javax.net.ssl.SSLEngine#getWantClientAuth()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setWantClientAuth",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getWantClientAuth",
            args = {}
        )
    })
    public void test_WantClientAuth() {
        mySSLEngine sse = new mySSLEngine();
        try {
            sse.setWantClientAuth(false);
            assertFalse(sse.getWantClientAuth());
            sse.setWantClientAuth(true);
            assertTrue(sse.getWantClientAuth());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#beginHandshake()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "beginHandshake",
        args = {}
    )
    public void test_beginHandshake() {
        mySSLEngine sse = new mySSLEngine();
        try {
            sse.beginHandshake();
            fail("SSLException wasn't thrown");
        } catch (SSLException se) {
            //expected
        }
        sse = new mySSLEngine("new host", 1080);
        try {
            sse.beginHandshake();
            fail("IllegalStateException wasn't thrown");
        } catch (IllegalStateException ise) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalStateException");
        }
        try {
            sse.setUseClientMode(true);
            System.out.println("<--- Client mode was set");
            sse.beginHandshake();
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#setUseClientMode(boolean mode)
     * @tests javax.net.ssl.SSLEngine#getUseClientMode()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setUseClientMode",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getUseClientMode",
            args = {}
        )
    })
    public void test_UseClientMode() {
        mySSLEngine sse = new mySSLEngine();
        try {
            sse.setUseClientMode(false);
            assertFalse(sse.getUseClientMode());
            sse.setUseClientMode(true);
            assertTrue(sse.getUseClientMode());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        
        try {
            sse = new mySSLEngine("new host", 1080);
            sse.setUseClientMode(true);
            sse.beginHandshake();
            try {
                sse.setUseClientMode(false);
                fail("IllegalArgumentException was not thrown");
            } catch (IllegalArgumentException iae) {
                //expected
            }
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#getSession()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSession",
        args = {}
    )
    public void test_getSession() {
        mySSLEngine sse = new mySSLEngine();
        try {
            assertNull(sse.getSession());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#getHandshakeStatus()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getHandshakeStatus",
        args = {}
    )
    public void test_getHandshakeStatus() {
        mySSLEngine sse = new mySSLEngine();
        try {
            assertEquals(sse.getHandshakeStatus().toString(), "FINISHED");
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#getDelegatedTask()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDelegatedTask",
        args = {}
    )
    public void test_getDelegatedTask() {
        mySSLEngine sse = new mySSLEngine();
        try {
            assertNull(sse.getDelegatedTask());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts,
     *                                       int offset, int length)
     * Exception case: SSLException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class, int.class, int.class}
    )
    public void test_unwrap_01() {
        ByteBuffer bbs = ByteBuffer.allocate(100);
        ByteBuffer bbd = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1();
        sse.setUseClientMode(true);
        
        try {
            sse.unwrap(bbs, new ByteBuffer[] { bbd }, 0, 10);
            fail("SSLException wasn't thrown");
        } catch (SSLException ex) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts,
     *                                       int offset, int length)
     * Exception case: IndexOutOfBoundsException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class, int.class, int.class}
    )
    public void test_unwrap_02() throws SSLException {
        String host = "new host";
        int port = 8080;
        ByteBuffer[] bbA = { ByteBuffer.allocate(100), ByteBuffer.allocate(10), ByteBuffer.allocate(100) };

        ByteBuffer bb = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.unwrap(bb, bbA, -1, 3);
            fail("IndexOutOfBoundsException wasn't thrown");
        } catch (IndexOutOfBoundsException iobe) {
            //expected
        }
        try {
            SSLEngineResult res = sse.unwrap(bb, bbA, 0, -3);
            fail("IndexOutOfBoundsException wasn't thrown");
        } catch (IndexOutOfBoundsException iobe) {
            //expected
        }
        try {
            SSLEngineResult res = sse.unwrap(bb, bbA, bbA.length + 1, bbA.length);
            fail("IndexOutOfBoundsException wasn't thrown");
        } catch (IndexOutOfBoundsException iobe) {
            //expected
        }
        try {
            SSLEngineResult res = sse.unwrap(bb, bbA, 0, bbA.length + 1);
            fail("IndexOutOfBoundsException wasn't thrown");
        } catch (IndexOutOfBoundsException iobe) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts,
     *                                       int offset, int length)
     * Exception case: ReadOnlyBufferException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class, int.class, int.class}
    )
    public void test_unwrap_03() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbR = ByteBuffer.allocate(100).asReadOnlyBuffer();
        ByteBuffer[] bbA = { bbR, ByteBuffer.allocate(10), ByteBuffer.allocate(100) };

        ByteBuffer bb = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.unwrap(bb, bbA, 0, bbA.length);
            fail("ReadOnlyBufferException wasn't thrown");
        } catch (ReadOnlyBufferException iobe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of ReadOnlyBufferException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts,
     *                                       int offset, int length)
     * Exception case: IllegalArgumentException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IllegalArgumentException should be thrown",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class, int.class, int.class}
    )
    @AndroidOnly("NullPointerException was thrown instead of IllegalArgumentException for null parameter.")
    public void test_unwrap_04() {
        String host = "new host";
        int port = 8080;
        ByteBuffer[] bbA = {ByteBuffer.allocate(100), ByteBuffer.allocate(10), ByteBuffer.allocate(100)};
        ByteBuffer[] bbAN = {ByteBuffer.allocate(100), null, ByteBuffer.allocate(100)};
        ByteBuffer[] bbN = null;
        ByteBuffer bb = ByteBuffer.allocate(10);
        ByteBuffer bN = null;
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.unwrap(bN, bbA, 0, 3);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        try {
            SSLEngineResult res = sse.unwrap(bb, bbAN, 0, 3);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        try {
            SSLEngineResult res = sse.unwrap(bb, bbN, 0, 0);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        try {
            SSLEngineResult res = sse.unwrap(bN, bbN, 0, 0);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }

    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts,
     *                                       int offset, int length)
     * Exception case: IllegalStateException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class, int.class, int.class}
    )
    public void test_unwrap_05() {
        String host = "new host";
        int port = 8080;
        ByteBuffer[] bbA = { ByteBuffer.allocate(100), ByteBuffer.allocate(10), ByteBuffer.allocate(100) };

        ByteBuffer bb = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1(host, port);
        
        try {
            SSLEngineResult res = sse.unwrap(bb, bbA, 0, bbA.length);
            fail("IllegalStateException wasn't thrown");
        } catch (IllegalStateException iobe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalStateException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts,
     *                                       int offset, int length)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class, int.class, int.class}
    )
    public void test_unwrap_06() {
        String host = "new host";
        int port = 8080;
        ByteBuffer[] bbA = { ByteBuffer.allocate(100), ByteBuffer.allocate(10), ByteBuffer.allocate(100) };

        ByteBuffer bb = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.unwrap(bb, bbA, 0, bbA.length);
            assertEquals(1, res.bytesConsumed());
            assertEquals(2, res.bytesProduced());
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, int offset,
     *                                     int length, ByteBuffer dst)
     * Exception case: SSLException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer[].class, int.class, int.class, ByteBuffer.class}
    )
    public void test_wrap_01() {
        ByteBuffer bbs = ByteBuffer.allocate(100);
        ByteBuffer bbd = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1();
        sse.setUseClientMode(true);
        
        try {
            sse.wrap(new ByteBuffer[] { bbs }, 0, 100, bbd);
            fail("SSLException wasn't thrown");
        } catch (SSLException ex) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, int offset,
     *                                     int length, ByteBuffer dst)
     * Exception case: IndexOutOfBoundsException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer[].class, int.class, int.class, ByteBuffer.class}
    )
    public void test_wrap_02() throws SSLException {
        String host = "new host";
        int port = 8080;
        ByteBuffer bb = ByteBuffer.allocate(10);
        ByteBuffer[] bbA = {ByteBuffer.allocate(5), ByteBuffer.allocate(10), ByteBuffer.allocate(5)};
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.wrap(bbA, -1, 3, bb);
            fail("IndexOutOfBoundsException wasn't thrown");
        } catch (IndexOutOfBoundsException iobe) {
            //expected
        }
        try {
            SSLEngineResult res = sse.wrap(bbA, 0, -3, bb);
            fail("IndexOutOfBoundsException wasn't thrown");
        } catch (IndexOutOfBoundsException iobe) {
            //expected
        }
        try {
            SSLEngineResult res = sse.wrap(bbA, bbA.length + 1, bbA.length, bb);
            fail("IndexOutOfBoundsException wasn't thrown");
        } catch (IndexOutOfBoundsException iobe) {
            //expected
        }
        try {
            SSLEngineResult res = sse.wrap(bbA, 0, bbA.length + 1, bb);
            fail("IndexOutOfBoundsException wasn't thrown");
        } catch (IndexOutOfBoundsException iobe) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, int offset,
     *                                     int length, ByteBuffer dst)
     * Exception case: ReadOnlyBufferException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer[].class, int.class, int.class, ByteBuffer.class}
    )
    public void test_wrap_03() throws SSLException {
        String host = "new host";
        int port = 8080;
        ByteBuffer bb = ByteBuffer.allocate(10).asReadOnlyBuffer();
        ByteBuffer[] bbA = {ByteBuffer.allocate(5), ByteBuffer.allocate(10), ByteBuffer.allocate(5)};
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.wrap(bbA, 0, bbA.length, bb);
            fail("ReadOnlyBufferException wasn't thrown");
        } catch (ReadOnlyBufferException iobe) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, int offset,
     *                                     int length, ByteBuffer dst)
     * Exception case: IllegalArgumentException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IllegalArgumentException must be thrown",
        method = "wrap",
        args = {ByteBuffer[].class, int.class, int.class, ByteBuffer.class}
    )
    @AndroidOnly("NullPointerException was thrown instead of IllegalArgumentException for null parameter.")
    public void test_wrap_04() {
        String host = "new host";
        int port = 8080;
        ByteBuffer[] bbA = {ByteBuffer.allocate(100), ByteBuffer.allocate(10), ByteBuffer.allocate(100)};
        ByteBuffer[] bbAN = {ByteBuffer.allocate(100), null, ByteBuffer.allocate(100)};
        ByteBuffer[] bbN = null;
        ByteBuffer bb = ByteBuffer.allocate(10);
        ByteBuffer bN = null;
        SSLEngine e = new mySSLEngine1(host, port);
        e.setUseClientMode(true);
        
        try {
            e.wrap(bbA, 0, 3, bN);
            fail("IllegalArgumentException must be thrown for null srcs byte buffer array");
        } catch (NullPointerException npe) {
        } catch (IllegalArgumentException ex) {
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IllegalArgumentException");
        }
        
        try {
            e.wrap(bbN, 0, 0, bN);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException ex) {
        } catch (NullPointerException npe) {
        } catch (Exception ex) {
            fail(ex + " was thrown instead of IllegalArgumentException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, int offset,
     *                                     int length, ByteBuffer dst)
     * Exception case: IllegalStateException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer[].class, int.class, int.class, ByteBuffer.class}
    )
    public void test_wrap_05() throws SSLException {
        String host = "new host";
        int port = 8080;
        ByteBuffer bb = ByteBuffer.allocate(10);
        ByteBuffer[] bbA = {ByteBuffer.allocate(5), ByteBuffer.allocate(10), ByteBuffer.allocate(5)};
        SSLEngine sse = new mySSLEngine1(host, port);
        
        try {
            SSLEngineResult res = sse.wrap(bbA, 0, bbA.length, bb);
            fail("IllegalStateException wasn't thrown");
        } catch (IllegalStateException iobe) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, int offset,
     *                                     int length, ByteBuffer dst)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer[].class, int.class, int.class, ByteBuffer.class}
    )
    public void test_wrap_06() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bb = ByteBuffer.allocate(10);
        ByteBuffer[] bbA = {ByteBuffer.allocate(5), ByteBuffer.allocate(10), ByteBuffer.allocate(5)};
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);        
        
        try {
            SSLEngineResult res = sse.wrap(bbA, 0, bbA.length, bb);
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#closeOutbound()
     * @tests javax.net.ssl.SSLEngine#isOutboundDone()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "closeOutbound",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isOutboundDone",
            args = {}
        )
    })
    public void test_closeOutbound() {
        SSLEngine sse = new mySSLEngine();
        
        try {
            assertFalse(sse.isOutboundDone());
            sse.closeOutbound();
            assertTrue(sse.isOutboundDone());
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#closeInbound()
     * @tests javax.net.ssl.SSLEngine#isInboundDone()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "closeInbound",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isInboundDone",
            args = {}
        )
    })
    public void test_closeInbound() {
        SSLEngine sse = new mySSLEngine();
 
        try {
            assertFalse(sse.isInboundDone());
            sse.closeInbound();
            assertTrue(sse.isInboundDone());
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer dst)
     * SSLException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer.class}
    )
    public void test_unwrap_ByteBuffer_ByteBuffer_01() {
        ByteBuffer bbs = ByteBuffer.allocate(100);
        ByteBuffer bbd = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1();
        sse.setUseClientMode(true);
        
        try {
            sse.unwrap(bbs, bbd);
            fail("SSLException wasn't thrown");
        } catch (SSLException ex) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer dst)
     * ReadOnlyBufferException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer.class}
    )
    public void test_unwrap_ByteBuffer_ByteBuffer_02() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbs = ByteBuffer.allocate(10);
        ByteBuffer bbd = ByteBuffer.allocate(100).asReadOnlyBuffer();
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.unwrap(bbs, bbd);
            fail("ReadOnlyBufferException wasn't thrown");
        } catch (ReadOnlyBufferException iobe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of ReadOnlyBufferException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer dst)
     * IllegalArgumentException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer.class}
    )
    @AndroidOnly("NullPointerException was thrown instead of IllegalArgumentException for null parameter.")
    public void test_unwrap_ByteBuffer_ByteBuffer_03() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbsN = null;
        ByteBuffer bbdN = null;
        ByteBuffer bbs = ByteBuffer.allocate(10);
        ByteBuffer bbd = ByteBuffer.allocate(100);
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.unwrap(bbsN, bbd);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        
        try {
            SSLEngineResult res = sse.unwrap(bbs, bbdN);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        
        try {
            SSLEngineResult res = sse.unwrap(bbsN, bbdN);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer dst)
     * IllegalStateException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer.class}
    )
    public void test_unwrap_ByteBuffer_ByteBuffer_04() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbs = ByteBuffer.allocate(10);
        ByteBuffer bbd = ByteBuffer.allocate(100);
        SSLEngine sse = new mySSLEngine1(host, port);
        
        try {
            SSLEngineResult res = sse.unwrap(bbs, bbd);
            fail("IllegalStateException wasn't thrown");
        } catch (IllegalStateException iobe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalStateException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer dst)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer.class}
    )
    public void test_unwrap_ByteBuffer_ByteBuffer_05() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbs = ByteBuffer.allocate(10);
        ByteBuffer bbd = ByteBuffer.allocate(100);
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.unwrap(bbs, bbd);
            assertEquals(1, res.bytesConsumed());
            assertEquals(2, res.bytesProduced());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts)
     * SSLException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class}
    )
    public void test_unwrap_ByteBuffer$ByteBuffer_01() {
        ByteBuffer bbs = ByteBuffer.allocate(100);
        ByteBuffer bbd = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1();
        sse.setUseClientMode(true);
        
        try {
            sse.unwrap(bbs, new ByteBuffer[] { bbd });
            fail("SSLException wasn't thrown");
        } catch (SSLException ex) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts)
     * ReadOnlyBufferException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class}
    )
    public void test_unwrap_ByteBuffer$ByteBuffer_02() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbs = ByteBuffer.allocate(10);
        ByteBuffer bbR = ByteBuffer.allocate(100).asReadOnlyBuffer();
        ByteBuffer[] bbA = { bbR, ByteBuffer.allocate(10), ByteBuffer.allocate(100) };
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.unwrap(bbs, bbA);
            fail("ReadOnlyBufferException wasn't thrown");
        } catch (ReadOnlyBufferException iobe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of ReadOnlyBufferException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts)
     * IllegalArgumentException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class}
    )
    @AndroidOnly("NullPointerException was thrown instead of IllegalArgumentException for null parameter.")
    public void test_unwrap_ByteBuffer$ByteBuffer_03() {
        String host = "new host";
        int port = 8080;
        ByteBuffer[] bbA = { ByteBuffer.allocate(100), ByteBuffer.allocate(10), ByteBuffer.allocate(100) };
        ByteBuffer[] bbN = { ByteBuffer.allocate(100), null, ByteBuffer.allocate(100) };
        ByteBuffer[] bbAN = null;
        ByteBuffer bb = ByteBuffer.allocate(10);
        ByteBuffer bN = null;
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.unwrap(bN, bbA);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        
        try {
            SSLEngineResult res = sse.unwrap(bb, bbAN);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        
        try {
            SSLEngineResult res = sse.unwrap(bb, bbN);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        
        try {
            SSLEngineResult res = sse.unwrap(bN, bbAN);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts)
     * IllegalStateException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class}
    )
    public void test_unwrap_ByteBuffer$ByteBuffer_04() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbs = ByteBuffer.allocate(10);
        ByteBuffer[] bbd = {ByteBuffer.allocate(100), ByteBuffer.allocate(10), ByteBuffer.allocate(100) };
        SSLEngine sse = new mySSLEngine1(host, port);
        
        try {
            SSLEngineResult res = sse.unwrap(bbs, bbd);
            fail("IllegalStateException wasn't thrown");
        } catch (IllegalStateException iobe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalStateException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#unwrap(ByteBuffer src, ByteBuffer[] dsts)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "unwrap",
        args = {ByteBuffer.class, ByteBuffer[].class}
    )
    public void test_unwrap_ByteBuffer$ByteBuffer_05() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbs = ByteBuffer.allocate(10);
        ByteBuffer[] bbd = {ByteBuffer.allocate(100), ByteBuffer.allocate(10), ByteBuffer.allocate(100) };
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.unwrap(bbs, bbd);
            assertEquals(1, res.bytesConsumed());
            assertEquals(2, res.bytesProduced());
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer src, ByteBuffer dst)
     * SSLException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer.class, ByteBuffer.class}
    )
    public void test_wrap_ByteBuffer_ByteBuffer_01() {
        ByteBuffer bbs = ByteBuffer.allocate(100);
        ByteBuffer bbd = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1();
        sse.setUseClientMode(true);
        
        try {
            sse.wrap(bbs, bbd);
            fail("SSLException wasn't thrown");
        } catch (SSLException ex) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer src, ByteBuffer dst)
     * ReadOnlyBufferException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer.class, ByteBuffer.class}
    )
    public void test_wrap_ByteBuffer_ByteBuffer_02() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbs = ByteBuffer.allocate(10);
        ByteBuffer bbd = ByteBuffer.allocate(100).asReadOnlyBuffer();
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.wrap(bbs, bbd);
            fail("ReadOnlyBufferException wasn't thrown");
        } catch (ReadOnlyBufferException iobe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of ReadOnlyBufferException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer src, ByteBuffer dst)
     * IllegalArgumentException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer.class, ByteBuffer.class}
    )
    @AndroidOnly("NullPointerException was thrown instead of IllegalArgumentException for null parameter.")
    public void test_wrap_ByteBuffer_ByteBuffer_03() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbsN = null;
        ByteBuffer bbdN = null;
        ByteBuffer bbs = ByteBuffer.allocate(10);
        ByteBuffer bbd = ByteBuffer.allocate(100);
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.wrap(bbsN, bbd);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        
        try {
            SSLEngineResult res = sse.wrap(bbs, bbdN);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        
        try {
            SSLEngineResult res = sse.wrap(bbsN, bbdN);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer src, ByteBuffer dst)
     * IllegalStateException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer.class, ByteBuffer.class}
    )
    public void test_wrap_ByteBuffer_ByteBuffer_04() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bbs = ByteBuffer.allocate(10);
        ByteBuffer bbd = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1(host, port);
        
        try {
            SSLEngineResult res = sse.wrap(bbs, bbd);
            fail("IllegalStateException wasn't thrown");
        } catch (IllegalStateException iobe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalStateException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer src, ByteBuffer dst)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer.class, ByteBuffer.class}
    )
    public void test_wrap_ByteBuffer_ByteBuffer_05() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bb = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.wrap(bb, ByteBuffer.allocate(10));
            assertEquals(10, res.bytesConsumed());
            assertEquals(20, res.bytesProduced());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, ByteBuffer dst)
     * SSLException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer[].class, ByteBuffer.class}
    )
    public void test_wrap_ByteBuffer$ByteBuffer_01() {
        ByteBuffer bbs = ByteBuffer.allocate(100);
        ByteBuffer bbd = ByteBuffer.allocate(10);
        SSLEngine sse = new mySSLEngine1();
        sse.setUseClientMode(true);
        
        try {
            sse.wrap(new ByteBuffer[] { bbs }, bbd);
            fail("SSLException wasn't thrown");
        } catch (SSLException ex) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, ByteBuffer dst)
     * ReadOnlyBufferException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer[].class, ByteBuffer.class}
    )
    public void test_wrap_ByteBuffer$ByteBuffer_02() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bb = ByteBuffer.allocate(10).asReadOnlyBuffer();
        ByteBuffer[] bbA = {ByteBuffer.allocate(5), ByteBuffer.allocate(10), ByteBuffer.allocate(5)};
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.wrap(bbA, bb);
            fail("ReadOnlyBufferException wasn't thrown");
        } catch (ReadOnlyBufferException iobe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of ReadOnlyBufferException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, ByteBuffer dst)
     * IllegalArgumentException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer[].class, ByteBuffer.class}
    )
    @AndroidOnly("NullPointerException was thrown instead of IllegalArgumentException for null parameter.")
    public void test_wrap_ByteBuffer$ByteBuffer_03() {
        String host = "new host";
        int port = 8080;
        ByteBuffer[] bbA = {ByteBuffer.allocate(100), ByteBuffer.allocate(10), ByteBuffer.allocate(100)};
        ByteBuffer[] bbN = {ByteBuffer.allocate(100), null, ByteBuffer.allocate(100)};
        ByteBuffer[] bbAN = null;
        ByteBuffer bb = ByteBuffer.allocate(10);
        ByteBuffer bN = null;
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.wrap(bbA, bN);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        
        try {
            SSLEngineResult res = sse.wrap(bbAN, bb);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
        
        try {
            SSLEngineResult res = sse.wrap(bbAN, bN);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iobe) {
            //expected
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, ByteBuffer dst)
     * IllegalStateException should be thrown.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer[].class, ByteBuffer.class}
    )
    public void test_wrap_ByteBuffer$ByteBuffer_04() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bb = ByteBuffer.allocate(10);
        ByteBuffer[] bbA = { ByteBuffer.allocate(5), ByteBuffer.allocate(10), ByteBuffer.allocate(5) };
        SSLEngine sse = new mySSLEngine1(host, port);
        
        try {
            SSLEngineResult res = sse.wrap(bbA, bb);
            fail("IllegalStateException wasn't thrown");
        } catch (IllegalStateException iobe) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalStateException");
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLEngine#wrap(ByteBuffer[] srcs, ByteBuffer dst)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "wrap",
        args = {ByteBuffer[].class, ByteBuffer.class}
    )
    public void test_wrap_ByteBuffer$ByteBuffer_05() {
        String host = "new host";
        int port = 8080;
        ByteBuffer bb = ByteBuffer.allocate(10);
        ByteBuffer[] bbA = { ByteBuffer.allocate(5), ByteBuffer.allocate(10), ByteBuffer.allocate(5) };
        SSLEngine sse = new mySSLEngine1(host, port);
        sse.setUseClientMode(true);
        
        try {
            SSLEngineResult res = sse.wrap(bbA, bb);
            assertEquals(10, res.bytesConsumed());
            assertEquals(20, res.bytesProduced());
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }
}

/*
 * Additional class for verification SSLEngine constructors
 */

class mySSLEngine extends SSLEngine {

    private boolean useClientMode;

    private boolean needClientAuth;

    private boolean enableSessionCreation = true;

    private boolean wantClientAuth;

    private String[] enabledProtocols;

    private String[] enabledCipherSuites;
    
    public int mode = -1;
    private boolean init = false;
    private boolean inboundDone = false;
    private boolean outboundDone = false;

    public mySSLEngine() {
        super();
    }

    protected mySSLEngine(String host, int port) {
        super(host, port);
    }

    public void beginHandshake() throws SSLException {
        String host = super.getPeerHost();
        if ((host == null) || (host.length() == 0)) {
            throw new SSLException("");
        }
        if (mode == -1) {
            throw new IllegalStateException();
        }
        init = true;
    }

    public void closeInbound() throws SSLException {
        inboundDone = true;
    }

    public void closeOutbound() {
        outboundDone = true;
    }

    public Runnable getDelegatedTask() {
        return null;
    }

    public String[] getEnabledCipherSuites() {
        return enabledCipherSuites;
    }

    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }

    public boolean getEnableSessionCreation() {
        return enableSessionCreation;
    }

    public SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        return SSLEngineResult.HandshakeStatus.FINISHED;
    }

    public boolean getNeedClientAuth() {
        return needClientAuth;
    }

    public SSLSession getSession() {
        return null;
    }

    public String[] getSupportedCipherSuites() {
        return new String[0];
    }

    public String[] getSupportedProtocols() {
        return new String[0];
    }

    public boolean getUseClientMode() {
        return useClientMode;
    }

    public boolean getWantClientAuth() {
        return wantClientAuth;
    }

    public boolean isInboundDone() {
        return inboundDone;
    }

    public boolean isOutboundDone() {
        return outboundDone;
    }

    public void setEnabledCipherSuites(String[] suites) {
        if (suites == null) {
            throw new IllegalArgumentException();
        }
        enabledCipherSuites = suites;
    }

    public void setEnabledProtocols(String[] protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException();
        }
        enabledProtocols = protocols;
    }

    public void setEnableSessionCreation(boolean flag) {
        enableSessionCreation = flag;
    }

    public void setNeedClientAuth(boolean need) {
        needClientAuth = need;
    }

    public void setUseClientMode(boolean mode) {
        if (init) {
            throw new IllegalArgumentException();
        }
        useClientMode = mode;
        if (useClientMode) {
            this.mode = 1;
        } else {
            this.mode = 0;
        }
    }

    public void setWantClientAuth(boolean want) {
        wantClientAuth = want;
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts,
            int offset, int length) throws SSLException {
        return new SSLEngineResult(SSLEngineResult.Status.OK,
                SSLEngineResult.HandshakeStatus.FINISHED, 1, 2);
    }

    public SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length,
            ByteBuffer dst) throws SSLException {
        return new SSLEngineResult(SSLEngineResult.Status.OK,
                SSLEngineResult.HandshakeStatus.FINISHED, 10, 20);
    }
}

class mySSLEngine1 extends mySSLEngine {
    
    public mySSLEngine1() {
    }

    public mySSLEngine1(String host, int port) {
        super(host, port);
    }
    
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst)
            throws SSLException {
        if (src.limit() > dst.limit()) {
            throw new SSLException("incorrect limits");
        }
        return super.unwrap(src, dst);
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts)
            throws SSLException {
        if (src.limit() > dsts[0].limit()) {
            throw new SSLException("incorrect limits");
        }
        return super.unwrap(src, dsts);
    }

    public SSLEngineResult wrap(ByteBuffer[] srcs, ByteBuffer dst)
            throws SSLException {
        if (srcs[0].limit() > dst.limit()) {
            throw new SSLException("incorrect limits");
        }
        return super.wrap(srcs, dst);
    }

    public SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst)
            throws SSLException {
        if (src.limit() > dst.limit()) {
            throw new SSLException("incorrect limits");
        }
        return super.wrap(src, dst);
    }
    
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts,
            int offset, int length) throws SSLException {
        if (super.mode == -1) {
            throw new IllegalStateException("client/server mode has not yet been set");
        }
        if (src.limit() > dsts[0].limit()) {
            throw new SSLException("incorrect limits");
        }
        if (offset < 0 || length < 0) {
            throw new IndexOutOfBoundsException("negative parameter");
        }
        if (offset > length || length > dsts.length - offset) {
            throw new IndexOutOfBoundsException("negative parameter");
        }
        if (src.equals(null) || dsts.equals(null)) {
            throw new IllegalArgumentException("null parameter");
        }
        for (int i = 0; i < dsts.length; i++) {
            if (dsts[i].isReadOnly()) {
                throw new ReadOnlyBufferException();
            } else if (dsts[i].equals(null)) {
                throw new IllegalArgumentException("null parameter");
            }
        }
        return super.unwrap(src, dsts, offset, length);
    }
    
    public SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst)
                                throws SSLException {
        if (super.mode == -1) {
            throw new IllegalStateException("client/server mode has not yet been set");
        }
        if (srcs[0].limit() > dst.limit()) {
            throw new SSLException("incorrect limits");
        }
        if (offset < 0 || length < 0) {
            throw new IndexOutOfBoundsException("negative parameter");
        }
        if (offset > length || length > srcs.length - offset) {
            throw new IndexOutOfBoundsException("negative parameter");
        }
        if (srcs.equals(null) || dst.equals(null)) {
            throw new IllegalArgumentException("null parameter");
        }
        for (int i = 0; i < srcs.length; i++) {
            if (srcs[i].equals(null)) {
                throw new IllegalArgumentException("null parameter");
            }
        }
        if (dst.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        return super.wrap(srcs, offset, length, dst);
    }
}