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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

@TestTargetClass(LogRecord.class)
public class LogRecordTest extends TestCase {

    static final String MSG = "test msg, pls. ignore itb";

    private LogRecord lr;

    private static String className = LogRecordTest.class.getName();

    protected void setUp() throws Exception {
        super.setUp();
        lr = new LogRecord(Level.CONFIG, MSG);
    }

    @TestInfo(level = TestLevel.COMPLETE, purpose = "", 
            targets = {
            @TestTarget(methodName = "LogRecord", methodArgs = {
                    Level.class, String.class})
    })
    public void testLogRecordWithNullPointers() {
        try {
            new LogRecord(null, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            new LogRecord(null, MSG);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        LogRecord r = new LogRecord(Level.WARNING, null);
        assertSame(r.getLevel(), Level.WARNING);
        assertNull(r.getMessage());
    }

    @TestInfo(level = TestLevel.COMPLETE, purpose = "normally set/get don't need to be tested", targets = {
            @TestTarget(methodName = "getLoggerName", methodArgs = {}),
            @TestTarget(methodName = "setLoggerName", methodArgs = {String.class})
    })
    public void testGetSetLoggerName() {
        assertNull(lr.getLoggerName());
        lr.setLoggerName(null);
        assertNull(lr.getLoggerName());
        lr.setLoggerName("test logger name");
        assertEquals("test logger name", lr.getLoggerName());
    }


    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getResourceBundle", methodArgs = {}),
            @TestTarget(methodName = "setResourceBundle", methodArgs = {ResourceBundle.class})
    })
    public void testGetSetResourceBundle() {
        assertNull(lr.getResourceBundleName());
        assertNull(lr.getResourceBundle());

        lr.setResourceBundle(null);
        assertNull(lr.getResourceBundle());

        lr.setResourceBundleName("bundles/java/util/logging/res");
        assertNull(lr.getResourceBundle());

        lr.setResourceBundleName(null);
        ResourceBundle rb = ResourceBundle
                .getBundle("bundles/java/util/logging/res");
        lr.setResourceBundle(rb);
        assertEquals(rb, lr.getResourceBundle());
        assertNull(lr.getResourceBundleName());
    }

    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getResourceBundleName", methodArgs = {}),
            @TestTarget(methodName = "setResourceBundleName", methodArgs = {String.class})
    })
    public void testGetSetResourceBundleName() {
        assertNull(lr.getResourceBundleName());
        lr.setResourceBundleName(null);
        assertNull(lr.getResourceBundleName());
        lr.setResourceBundleName("test");
        assertEquals("test", lr.getResourceBundleName());
    }
    
    @TestInfo(level = TestLevel.COMPLETE, purpose = "normal behavior of getter/setter don't need to be tested (normally)", targets = {
            @TestTarget(methodName = "getLevel", methodArgs = {}),
            @TestTarget(methodName = "setLevel", methodArgs = {Level.class})
    })
    public void testGetSetLevelNormal() {           
        assertSame(lr.getLevel(), Level.CONFIG);
        lr.setLevel(Level.ALL);
        assertSame(lr.getLevel(), Level.ALL);
        lr.setLevel(Level.FINEST);
        assertSame(lr.getLevel(), Level.FINEST);
    }

    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getLevel", methodArgs = {}),
            @TestTarget(methodName = "setLevel", methodArgs = {Level.class})
    })
    public void testGetSetLevelNullPointerException() {
        try {
            lr.setLevel(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        assertSame(lr.getLevel(), Level.CONFIG);
    }

    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getSequenceNumber", methodArgs = {}),
            @TestTarget(methodName = "setSequenceNumber", methodArgs = {long.class})
    })
    public void testGetSetSequenceNumber() {
        long l = lr.getSequenceNumber();
        lr.setSequenceNumber(-111);
        assertEquals(lr.getSequenceNumber(), -111L);
        lr.setSequenceNumber(0);
        assertEquals(lr.getSequenceNumber(), 0L);
        lr = new LogRecord(Level.ALL, null); //sequenceNumber is updated to a private static counter
        assertEquals(lr.getSequenceNumber(), l + 1);
    }

    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getSourceClassName", methodArgs = {}),
            @TestTarget(methodName = "setSourceClassName", methodArgs = {String.class})
    })
    public void testGetSetSourceClassName() {
        lr.setSourceClassName(null);
        assertNull(lr.getSourceClassName());
        lr.setSourceClassName("bad class name");
        assertEquals("bad class name", lr.getSourceClassName());
        lr.setSourceClassName(this.getClass().getName());
        assertEquals(this.getClass().getName(), lr.getSourceClassName());
    }

    @TestInfo(level = TestLevel.PARTIAL_OK, purpose = "", targets = {
            @TestTarget(methodName = "getSourceMethodName", methodArgs = {}),
            @TestTarget(methodName = "setSourceMethodName", methodArgs = {String.class})
    })
    public void testGetSetSourceMethodName() {
        lr.setSourceMethodName(null);
        assertNull(lr.getSourceMethodName());
        lr.setSourceMethodName("bad class name");
        assertEquals("bad class name", lr.getSourceMethodName());
        lr.setSourceMethodName(this.getClass().getName());
        assertEquals(this.getClass().getName(), lr.getSourceMethodName());
    }

    @TestInfo(level = TestLevel.PARTIAL_OK, purpose = "", targets = {
            @TestTarget(methodName = "getSourceMethodName", methodArgs = {}),
            @TestTarget(methodName = "setSourceMethodName", methodArgs = {String.class}),
            @TestTarget(methodName = "getSourceClassName", methodArgs = {}),
            @TestTarget(methodName = "setSourceClassName", methodArgs = {String.class})
    })
    public void testGetSourceDefaultValue() {
        assertNull(lr.getSourceMethodName());
        assertNull(lr.getSourceClassName());

        // find class and method who called logger
        Logger logger = Logger.global;
        MockHandler handler = new MockHandler();
        logger.addHandler(handler);
        logger.log(Level.SEVERE, MSG);
        assertEquals(this.getClass().getName(), handler.getSourceClassName());
        assertEquals("testGetSourceDefaultValue", handler.getSourceMethodName());

        // only set source method to null
        lr = new LogRecord(Level.SEVERE, MSG);
        lr.setSourceMethodName(null);
        logger.log(lr);
        assertNull(handler.getSourceClassName());
        assertNull(handler.getSourceMethodName());

        // only set source class to null
        lr = new LogRecord(Level.SEVERE, MSG);
        lr.setSourceClassName(null);
        logger.log(lr);
        assertNull(handler.getSourceClassName());
        assertNull(handler.getSourceMethodName());

        // set both
        lr = new LogRecord(Level.SEVERE, MSG);
        lr.setSourceClassName("className");
        lr.setSourceMethodName("methodName");
        logger.log(lr);
        assertEquals("className", handler.getSourceClassName());
        assertEquals("methodName", handler.getSourceMethodName());

        // test if LogRecord is constructed in another class, and is published
        // by Logger
        logger.log(RecordFactory.getDefaultRecord());
        assertEquals(this.getClass().getName(), handler.getSourceClassName());
        assertEquals("testGetSourceDefaultValue", handler.getSourceMethodName());

        lr = RecordFactory.getDefaultRecord();
        RecordFactory.log(logger, lr);
        assertEquals(RecordFactory.class.getName(), handler
                .getSourceClassName());
        assertEquals("log", handler.getSourceMethodName());

        // only try once to get the default value
        lr = RecordFactory.getDefaultRecord();
        assertNull(lr.getSourceClassName());
        assertNull(lr.getSourceMethodName());
        RecordFactory.log(logger, lr);
        assertNull(handler.getSourceClassName());
        assertNull(handler.getSourceMethodName());

        MockLogger ml = new MockLogger("foo", null);
        ml.addHandler(handler);
        ml.info(MSG);
        assertEquals(className + "$MockLogger", handler.getSourceClassName());
        assertEquals("info", handler.getSourceMethodName());

        ml = new MockLogger("foo", null);
        ml.addHandler(handler);
        ml.log(Level.SEVERE, MSG);
        assertNull(handler.getSourceClassName());
        assertNull(handler.getSourceMethodName());

        lr = new LogRecord(Level.SEVERE, MSG);
        handler.publish(lr);
        assertNull(handler.getSourceClassName());
        assertNull(handler.getSourceMethodName());
        logger.removeHandler(handler);
    }

    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getMessage", methodArgs = {}),
            @TestTarget(methodName = "setMessage", methodArgs = {String.class})
    })
    public void testGetSetMessage() {
        assertEquals(MSG, lr.getMessage());
        lr.setMessage(null);
        assertNull(lr.getMessage());
        lr.setMessage("");
        assertEquals("", lr.getMessage());
    }

    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getParameters", methodArgs = {}),
            @TestTarget(methodName = "setParameters", methodArgs = {Object[].class})
    })
    public void testGetSetParameters() {
        assertNull(lr.getParameters());
        lr.setParameters(null);
        assertNull(lr.getParameters());
        Object[] oa = new Object[0];
        lr.setParameters(oa);
        assertEquals(oa, lr.getParameters());
        oa = new Object[] {new Object(), new Object()};
        lr.setParameters(oa);
        assertSame(oa, lr.getParameters());
    }

    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getMillis", methodArgs = {}),
            @TestTarget(methodName = "setMillis", methodArgs = {long.class})
    })
    public void testGetSetMillis() {
        long milli = lr.getMillis();
        assertTrue(milli > 0);
        lr.setMillis(-1);
        assertEquals(-1, lr.getMillis());
        lr.setMillis(0);
        assertEquals(0, lr.getMillis());
    }
    
    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getMillis", methodArgs = {}),
            @TestTarget(methodName = "setMillis", methodArgs = {long.class})
    })
    public void testGetSetTimeCheck() {
        long before = lr.getMillis();
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogRecord lr2 = new LogRecord(Level.CONFIG, "MSG2");
        long after = lr2.getMillis();
        assertTrue(after-before>0);    
    }
    


    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getThreadID", methodArgs = {}),
            @TestTarget(methodName = "setThreadID", methodArgs = {int.class})
    })
    public void testGetSetThreadID() {
        int id = lr.getThreadID();
        lr = new LogRecord(Level.ALL, "a1");
        assertEquals(id, lr.getThreadID());
        lr.setThreadID(id + 10);
        assertEquals(id + 10, lr.getThreadID());
        lr = new LogRecord(Level.ALL, "a1");
        assertEquals(id, lr.getThreadID());
    }
    
    /*
     * Check threadID are different
     */
    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getThreadID", methodArgs = {}),
            @TestTarget(methodName = "setThreadID", methodArgs = {int.class})
    })
    public void testGetSetThreadID_DifferentThread() {
        int id = lr.getThreadID();
        // Create and start the thread
        MockThread thread = new MockThread();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }  
        // Create and start the thread2
        MockThread thread2 = new MockThread();
        thread2.start();
        try {
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //All threadID must be different, based on the ThreadLocal.java ID
        assertTrue(lr.getThreadID() != thread.lr.getThreadID());
        assertTrue(lr.getThreadID() != thread2.lr.getThreadID());
        assertTrue(thread.lr.getThreadID() != thread2.lr.getThreadID());
    }


    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "getThrown", methodArgs = {}),
            @TestTarget(methodName = "setThrown", methodArgs = {Throwable.class})})
    public void testGetSetThrown() {
        assertNull(lr.getThrown());
        lr.setThrown(null);
        assertNull(lr.getThrown());
        Throwable e = new Exception();
        lr.setThrown(e);
        assertEquals(e, lr.getThrown());
        Throwable n = new NullPointerException();
        lr.setThrown(n);
        assertEquals(n, lr.getThrown());
    }

    // comparator for LogRecord objects
    private static final SerializableAssert LOGRECORD_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            LogRecord init = (LogRecord) initial;
            LogRecord dser = (LogRecord) deserialized;

            assertEquals("Class", init.getClass(), dser.getClass());
            assertEquals("Level", init.getLevel(), dser.getLevel());
            assertEquals("LoggerName", init.getLoggerName(), dser
                    .getLoggerName());
            assertEquals("Message", init.getMessage(), dser.getMessage());
            assertEquals("Millis", init.getMillis(), dser.getMillis());

            // compare parameters
            Object[] paramInit = init.getParameters();
            Object[] paramDser = dser.getParameters();
            assertEquals("Parameters length", paramInit.length,
                    paramDser.length);
            for (int i = 0; i < paramInit.length; i++) {
                assertEquals("Param: " + i, paramInit[i].toString(),
                        paramDser[i]);
            }

            // don't check ResourceBundle object
            // verify only bundle's name
            assertEquals("ResourceBundleName", init.getResourceBundleName(),
                    dser.getResourceBundleName());
            assertEquals("SequenceNumber", init.getSequenceNumber(), dser
                    .getSequenceNumber());
            assertEquals("SourceClassName", init.getSourceClassName(), dser
                    .getSourceClassName());
            assertEquals("SourceMethodName", init.getSourceMethodName(), dser
                    .getSourceMethodName());
            assertEquals("ThreadID", init.getThreadID(), dser.getThreadID());

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(init
                    .getThrown(), dser.getThrown());
        }
    };

    /**
     * @tests serialization/deserialization compatibility.
     */
    @TestInfo(level = TestLevel.COMPLETE, purpose = "serialisation/deserialization", targets = {
            @TestTarget(methodName = "!SerializationSelf", methodArgs = {})})
    public void testSerializationSelf() throws Exception {
        LogRecord r = new LogRecord(Level.ALL, "msg");
        r.setLoggerName("LoggerName");
        r.setMillis(123456789);
        r.setResourceBundleName("ResourceBundleName");
        r.setSequenceNumber(987654321);
        r.setSourceClassName("SourceClassName");
        r.setSourceMethodName("SourceMethodName");
        r.setParameters(new Object[] {"test string", new Exception("ex-msg")});
        r.setThreadID(3232);
        r.setThrown(new Exception("ExceptionMessage"));

        SerializationTest.verifySelf(r, LOGRECORD_COMPARATOR);
    }

    /**
     * @tests resolution of resource bundle for serialization/deserialization.
     */
    @TestInfo(
        level = TestLevel.COMPLETE, 
        purpose = "tests resolution of resource bundle during deserialization", 
        targets = {
                @TestTarget(methodName = "!Serialization", methodArgs = {})})
    public void testSerializationResourceBundle() throws Exception {

        // test case: valid resource bundle name
        lr.setResourceBundleName("bundles/java/util/logging/res2");
        lr.setResourceBundle(ResourceBundle.getBundle(
                "bundles/java/util/logging/res", Locale.US));

        LogRecord result = (LogRecord) SerializationTest.copySerializable(lr);
        assertNotNull(result.getResourceBundle());

        // test case: invalid resource bundle name, it is not resolved during
        // deserialization LogRecord object so check for returned null value
        lr.setResourceBundleName("bad bundle name");
        lr.setResourceBundle(ResourceBundle.getBundle(
                "bundles/java/util/logging/res", Locale.US));

        result = (LogRecord) SerializationTest.copySerializable(lr);
        assertNull(result.getResourceBundle());
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestInfo(level = TestLevel.COMPLETE, purpose = "", targets = {
            @TestTarget(methodName = "!SerializationGolden", methodArgs = {})})
    public void testSerializationCompatibility() throws Exception {
        LogRecord r = new LogRecord(Level.ALL, "msg");
        r.setLoggerName("LoggerName");
        r.setMillis(123456789);
        r.setResourceBundleName("ResourceBundleName");
        r.setSequenceNumber(987654321);
        r.setSourceClassName("SourceClassName");
        r.setSourceMethodName("SourceMethodName");
        r.setParameters(new Object[] {"test string", new Exception("ex-msg")});
        r.setThreadID(3232);
        r.setThrown(new Exception("ExceptionMessage"));

        SerializationTest.verifyGolden(this, r, LOGRECORD_COMPARATOR);
    }

    public static class MockHandler extends Handler {
        private String className;

        private String methodName;

        public void close() {
        }

        public void flush() {
        }

        public void publish(LogRecord record) {
            className = record.getSourceClassName();
            methodName = record.getSourceMethodName();
        }

        public String getSourceMethodName() {
            return methodName;
        }

        public String getSourceClassName() {
            return className;
        }
    }

    // mock class, try to test when the sourceclass and sourcemethod of
    // LogRecord is initiated
    public static class RecordFactory {

        public static LogRecord getDefaultRecord() {
            return new LogRecord(Level.SEVERE, MSG);
        }

        public static void log(Logger logger, LogRecord lr) {
            logger.log(lr);
        }
    }

    public static class MockLogger extends Logger {

        /**
         * @param name
         * @param resourceBundleName
         */
        public MockLogger(String name, String resourceBundleName) {
            super(name, resourceBundleName);
        }

        public void log(Level l, String s) {
            this.log(new LogRecord(l, s));
        }

        public void info(String s) {
            super.info(s);
        }

        public void log(LogRecord record) {
            if (isLoggable(record.getLevel())) {
                // call the handlers of this logger
                // TODO: What if an exception occurred in handler?
                Handler[] ha = this.getHandlers();
                for (int i = 0; i < ha.length; i++) {
                    ha[i].publish(record);
                }
                // call the parent's handlers if set useParentHandlers
                if (getUseParentHandlers()) {
                    Logger anyParent = this.getParent();
                    while (null != anyParent) {
                        ha = anyParent.getHandlers();
                        for (int i = 0; i < ha.length; i++) {
                            ha[i].publish(record);
                        }
                        if (anyParent.getUseParentHandlers()) {
                            anyParent = anyParent.getParent();
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public class MockThread extends Thread {

        public LogRecord lr = null; //will be update by the thread
        
        public MockThread(){
            super();
        }
        
        public void run() {
            update();
        }
        
        public synchronized void update(){
            lr = new LogRecord(Level.CONFIG, "msg thread");
        }

    }
}
