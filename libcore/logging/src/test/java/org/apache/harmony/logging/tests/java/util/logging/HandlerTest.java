/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.logging.tests.java.util.logging;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestLevel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Permission;
import java.util.EmptyStackException;
import java.util.Properties;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.LoggingPermission;
import java.util.logging.SimpleFormatter;

import junit.framework.TestCase;
import org.apache.harmony.logging.tests.java.util.logging.util.EnvironmentHelper;
import tests.util.CallVerificationStack;

/**
 * Test suite for the class java.util.logging.Handler.
 * 
 */
@TestTargetClass(Handler.class) 
public class HandlerTest extends TestCase {
    private static String className = HandlerTest.class.getName();

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        CallVerificationStack.getInstance().clear();
    }

    /**
     * Constructor for HandlerTest.
     * 
     * @param arg0
     */
    public HandlerTest(String arg0) {
        super(arg0);
    }

    /*
     * Test the constructor.
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "Handler",
          methodArgs = {}
        )
    })
    public void testConstructor() {
        MockHandler h = new MockHandler();
        assertSame(h.getLevel(), Level.ALL);
        assertNull(h.getFormatter());
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        assertTrue(h.getErrorManager() instanceof ErrorManager);
    }

    /*
     * Test the constructor, with properties set
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "Handler",
          methodArgs = {}
        )
    })
    public void testConstructor_Properties() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.MockHandler.level", "FINE");
        p.put("java.util.logging.MockHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.Handler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.MockHandler.encoding", "utf-8");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        assertEquals(LogManager.getLogManager().getProperty(
        "java.util.logging.MockHandler.level"), "FINE");
        assertEquals(LogManager.getLogManager().getProperty(
        "java.util.logging.MockHandler.encoding"), "utf-8");
        MockHandler h = new MockHandler();
        assertSame(h.getLevel(), Level.ALL);
        assertNull(h.getFormatter());
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        assertTrue(h.getErrorManager() instanceof ErrorManager);
        LogManager.getLogManager().reset();
    }

    /*
     * Abstract method, no test needed.
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Abstract method.",
      targets = {
        @TestTarget(
          methodName = "close",
          methodArgs = {}
        )
    })
    public void testClose() {
        MockHandler h = new MockHandler();
        h.close();
    }

    /*
     * Abstract method, no test needed.
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Abstract method.",
      targets = {
        @TestTarget(
          methodName = "flush",
          methodArgs = {}
        )
    })
    public void testFlush() {
        MockHandler h = new MockHandler();
        h.flush();
    }

    /*
     * Abstract method, no test needed.
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Abstract method.",
      targets = {
        @TestTarget(
          methodName = "publish",
          methodArgs = {java.util.logging.LogRecord.class}
        )
    })
    public void testPublish() {
        MockHandler h = new MockHandler();
        h.publish(null);
    }

    /*
     * Test getEncoding & setEncoding methods with supported encoding.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Doesn't verify exceptions.",
      targets = {
        @TestTarget(
          methodName = "getEncoding",
          methodArgs = {}
        ),
        @TestTarget(
          methodName = "setEncoding",
          methodArgs = {java.lang.String.class}
        )
    })
    public void testGetSetEncoding_Normal() throws Exception {
        MockHandler h = new MockHandler();
        h.setEncoding("iso-8859-1");
        assertEquals("iso-8859-1", h.getEncoding());
    }

    /*
     * Test getEncoding & setEncoding methods with null.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies null as a parameter.",
      targets = {
        @TestTarget(
          methodName = "getEncoding",
          methodArgs = {}
        )
    })
    public void testGetSetEncoding_Null() throws Exception {
        MockHandler h = new MockHandler();
        h.setEncoding(null);
        assertNull(h.getEncoding());
    }

    /*
     * Test getEncoding & setEncoding methods with unsupported encoding.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies UnsupportedEncodingException.",
      targets = {
        @TestTarget(
          methodName = "setEncoding",
          methodArgs = {java.lang.String.class}
        )
    })
    public void testGetSetEncoding_Unsupported() {
        MockHandler h = new MockHandler();
        try {
            h.setEncoding("impossible");
            fail("Should throw UnsupportedEncodingException!");
        } catch (UnsupportedEncodingException e) {
        }
        assertNull(h.getEncoding());
    }

    /*
     * Test setEncoding with insufficient privilege.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Doesn't verify UnsupportedEncodingException.",
      targets = {
        @TestTarget(
          methodName = "setEncoding",
          methodArgs = {java.lang.String.class}
        )
    })
    public void testSetEncoding_InsufficientPrivilege() throws Exception {
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        // set a normal value
        try {
            h.setEncoding("iso-8859-1");
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        assertNull(h.getEncoding());
        System.setSecurityManager(new MockSecurityManager());
        // set an invalid value
        try {

            h.setEncoding("impossible");
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        assertNull(h.getEncoding());
    }

    /*
     * Test getErrorManager & setErrorManager methods with non-null value.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "setErrorManager",
          methodArgs = {java.util.logging.ErrorManager.class}
        ),
        @TestTarget(
          methodName = "getErrorManager",
          methodArgs = {}
        )
    })
    public void testGetSetErrorManager_Normal() throws Exception {
        MockHandler h = new MockHandler();
        ErrorManager man = new ErrorManager();
        h.setErrorManager(man);
        assertSame(man, h.getErrorManager());
    }

    /*
     * Test getErrorManager & setErrorManager methods with null.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies null as a parameter.",
      targets = {
        @TestTarget(
          methodName = "getErrorManager",
          methodArgs = {}
        ),
        @TestTarget(
          methodName = "setErrorManager",
          methodArgs = {java.util.logging.ErrorManager.class}
        )
    })
    public void testGetSetErrorManager_Null() throws Exception {
        MockHandler h = new MockHandler();
        // test set null
        try {
            h.setErrorManager(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }

        // test reset null
        try {
            h.setErrorManager(new ErrorManager());
            h.setErrorManager(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }

    /*
     * Test getErrorManager with insufficient privilege.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies SecurityException.",
      targets = {
        @TestTarget(
          methodName = "getErrorManager",
          methodArgs = {}
        )
    })
    public void testGetErrorManager_InsufficientPrivilege() throws Exception {
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());

        try {
            h.getErrorManager();
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }

    /*
     * Test setErrorManager with insufficient privilege.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies setErrorManager with insufficient privilege.",
      targets = {
        @TestTarget(
          methodName = "setErrorManager",
          methodArgs = {java.util.logging.ErrorManager.class}
        )
    })
    public void testSetErrorManager_InsufficientPrivilege() throws Exception {
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());

        // set null
        try {

            h.setErrorManager(null);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        // set a normal value
        System.setSecurityManager(new MockSecurityManager());
        try {

            h.setErrorManager(new ErrorManager());
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }

    /*
     * Test getFilter & setFilter methods with non-null value.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Doesn't verify SecurityException.",
      targets = {
        @TestTarget(
          methodName = "setFilter",
          methodArgs = {java.util.logging.Filter.class}
        ),
        @TestTarget(
          methodName = "getFilter",
          methodArgs = {}
        )
    })
    public void testGetSetFilter_Normal() throws Exception {
        MockHandler h = new MockHandler();
        Filter f = new MockFilter();
        h.setFilter(f);
        assertSame(f, h.getFilter());
    }

    /*
     * Test getFilter & setFilter methods with null.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies null as a parameter.",
      targets = {
        @TestTarget(
          methodName = "getFilter",
          methodArgs = {}
        ),
        @TestTarget(
          methodName = "setFilter",
          methodArgs = {java.util.logging.Filter.class}
        )
    })
    public void testGetSetFilter_Null() throws Exception {
        MockHandler h = new MockHandler();
        // test set null
        h.setFilter(null);

        // test reset null
        h.setFilter(new MockFilter());
        h.setFilter(null);
    }

    /*
     * Test setFilter with insufficient privilege.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies SecurityException.",
      targets = {
        @TestTarget(
          methodName = "setFilter",
          methodArgs = {java.util.logging.Filter.class}
        )
    })
    public void testSetFilter_InsufficientPrivilege() throws Exception {
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());

        // set null
        try {

            h.setFilter(null);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        // set a normal value
        System.setSecurityManager(new MockSecurityManager());
        try {

            h.setFilter(new MockFilter());
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }

    /*
     * Test getFormatter & setFormatter methods with non-null value.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Doesn't verify SecurityException.",
      targets = {
        @TestTarget(
          methodName = "getFormatter",
          methodArgs = {}
        ),
        @TestTarget(
          methodName = "setFormatter",
          methodArgs = {java.util.logging.Formatter.class}
        )
    })
    public void testGetSetFormatter_Normal() throws Exception {
        MockHandler h = new MockHandler();
        Formatter f = new SimpleFormatter();
        h.setFormatter(f);
        assertSame(f, h.getFormatter());
    }

    /*
     * Test getFormatter & setFormatter methods with null.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies null as a parameter.",
      targets = {
        @TestTarget(
          methodName = "getFormatter",
          methodArgs = {}
        ),
        @TestTarget(
          methodName = "setFormatter",
          methodArgs = {java.util.logging.Formatter.class}
        )
    })
    public void testGetSetFormatter_Null() throws Exception {
        MockHandler h = new MockHandler();
        // test set null
        try {
            h.setFormatter(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }

        // test reset null
        try {
            h.setFormatter(new SimpleFormatter());
            h.setFormatter(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }

    /*
     * Test setFormatter with insufficient privilege.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies SecurityException.",
      targets = {
        @TestTarget(
          methodName = "getFormatter",
          methodArgs = {}
        )
    })
    public void testSetFormatter_InsufficientPrivilege() throws Exception {
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());

        // set null
        try {

            h.setFormatter(null);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        // set a normal value
        System.setSecurityManager(new MockSecurityManager());
        try {

            h.setFormatter(new SimpleFormatter());
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }

    /*
     * Test getLevel & setLevel methods with non-null value.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Doesn't verify SecurityException.",
      targets = {
        @TestTarget(
          methodName = "getLevel",
          methodArgs = {}
        ),
        @TestTarget(
          methodName = "setLevel",
          methodArgs = {java.util.logging.Level.class}
        )
    })
    public void testGetSetLevel_Normal() throws Exception {
        MockHandler h = new MockHandler();
        Level f = Level.CONFIG;
        h.setLevel(f);
        assertSame(f, h.getLevel());
    }

    /*
     * Test getLevel & setLevel methods with null.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies getLevel & setLevel methods with null.",
      targets = {
        @TestTarget(
          methodName = "getLevel",
          methodArgs = {}
        ),
        @TestTarget(
          methodName = "setLevel",
          methodArgs = {java.util.logging.Level.class}
        )
    })
    public void testGetSetLevel_Null() throws Exception {
        MockHandler h = new MockHandler();
        // test set null
        try {
            h.setLevel(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }

        // test reset null
        try {
            h.setLevel(Level.CONFIG);
            h.setLevel(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }

    /*
     * Test setLevel with insufficient privilege.
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies  NullPointerException, SecurityException.",
      targets = {
        @TestTarget(
          methodName = "setLevel",
          methodArgs = {java.util.logging.Level.class}
        )
    })
    public void testSetLevel_InsufficientPrivilege() throws Exception {
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());

        // set null
        try {

            h.setLevel(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        // set a normal value
        System.setSecurityManager(new MockSecurityManager());
        try {

            h.setLevel(Level.CONFIG);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }

    /*
     * Use no filter
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "isLoggable",
          methodArgs = {java.util.logging.LogRecord.class}
        )
    })
    public void testIsLoggable_NoFilter() {
        MockHandler h = new MockHandler();
        LogRecord r = new LogRecord(Level.CONFIG, null);
        assertTrue(h.isLoggable(r));

        h.setLevel(Level.CONFIG);
        assertTrue(h.isLoggable(r));

        h.setLevel(Level.SEVERE);
        assertFalse(h.isLoggable(r));

        r.setLevel(Level.OFF);
        h.setLevel(Level.OFF);
        assertFalse(h.isLoggable(r));
    }

    /*
     * Use a filter
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Verifies isLoggable method with filter.",
      targets = {
        @TestTarget(
          methodName = "isLoggable",
          methodArgs = {java.util.logging.LogRecord.class}
        )
    })
    public void testIsLoggable_WithFilter() {
        MockHandler h = new MockHandler();
        LogRecord r = new LogRecord(Level.CONFIG, null);
        LogRecord r1 = new LogRecord(Level.CONFIG, null);
        LogRecord r2 = new LogRecord(Level.CONFIG, null);
        
        h.setFilter(new MockFilter());
        assertFalse(h.isLoggable(r));
        assertSame(r,CallVerificationStack.getInstance().pop());
        
        h.setLevel(Level.CONFIG);
        assertFalse(h.isLoggable(r1));
        assertSame(r1, CallVerificationStack.getInstance().pop());

        h.setLevel(Level.SEVERE);
        assertFalse(h.isLoggable(r2));
       
        try{
            CallVerificationStack.getInstance().pop();
        }catch(EmptyStackException e){
            //normal
        }
    }

    /**
     * @tests java.util.logging.Handler#isLoggable(LogRecord)
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies null as a parameter.",
      targets = {
        @TestTarget(
          methodName = "isLoggable",
          methodArgs = {java.util.logging.LogRecord.class}
        )
    })
    public void testIsLoggable_Null() {
        MockHandler h = new MockHandler();
        try {
            h.isLoggable(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Test whether the error manager is actually called with expected
     * parameters.
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "reportError",
          methodArgs = {java.lang.String.class, java.lang.Exception.class, int.class}
        )
    })
    public void testReportError() {
        MockHandler h = new MockHandler();
        h.setErrorManager(new MockErrorManager());
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());

        try {
            Exception ex = new Exception("test exception");
            // with non-null parameters
            h.reportError("test msg", ex, -1);
            assertEquals(-1, CallVerificationStack.getInstance().popInt());
            assertSame(ex, CallVerificationStack.getInstance().pop());
            assertEquals("test msg", CallVerificationStack.getInstance().pop());
            // with null parameters
            h.reportError(null, null, 0);
            assertEquals(0, CallVerificationStack.getInstance().popInt());
            assertSame(null, CallVerificationStack.getInstance().pop());
            assertNull(CallVerificationStack.getInstance().pop());
        } catch (SecurityException e) {
            fail("Should not throw SecurityException!");
        } finally {
            System.setSecurityManager(oldMan);
        }
    }

    /*
     * Used to enable the testing of Handler because Handler is an abstract
     * class.
     */
    public static class MockHandler extends Handler {

        public void close() {
        }

        public void flush() {
        }

        public void publish(LogRecord record) {
        }

        public void reportError(String msg, Exception ex, int code) {
            super.reportError(msg, ex, code);
        }
    }

    /*
     * Used to grant all permissions except logging control.
     */
    public static class MockSecurityManager extends SecurityManager {

        public MockSecurityManager() {
        }

        public void checkPermission(Permission perm) {
            // grant all permissions except logging control
            if (perm instanceof LoggingPermission) {
                throw new SecurityException();
            }
        }

        public void checkPermission(Permission perm, Object context) {
            // grant all permissions except logging control
            if (perm instanceof LoggingPermission) {
                throw new SecurityException();
            }
        }
    }

    /*
     * A mock filter, always return false.
     */
    public static class MockFilter implements Filter {

        public boolean isLoggable(LogRecord record) {
            CallVerificationStack.getInstance().push(record);
            return false;
        }
    }

    /*
     * A mock error manager, used to validate the expected method is called with
     * the expected parameters.
     */
    public static class MockErrorManager extends ErrorManager {

        public void error(String msg, Exception ex, int errorCode) {
            CallVerificationStack.getInstance().push(msg);
            CallVerificationStack.getInstance().push(ex);
            CallVerificationStack.getInstance().push(errorCode);
        }
    }
    
    public static class NullOutputStream extends OutputStream{
        @Override
        public void write(int arg0) throws IOException {
        }
    }

}
