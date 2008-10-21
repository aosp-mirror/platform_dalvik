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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Permission;
import java.util.Properties;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.LoggingPermission;
import java.util.logging.SocketHandler;
import java.util.logging.XMLFormatter;

import junit.framework.TestCase;

import org.apache.harmony.logging.tests.java.util.logging.HandlerTest.NullOutputStream;
import org.apache.harmony.logging.tests.java.util.logging.util.EnvironmentHelper;

import tests.util.CallVerificationStack;

/**
 * Test class java.util.logging.ConsoleHandler
 */
public class SocketHandlerTest extends TestCase {

    private static final LogManager LOG_MANAGER = LogManager.getLogManager();

    private final static String INVALID_LEVEL = "impossible_level";
    
    private final PrintStream err = System.err;

    private OutputStream errSubstituteStream = null;    
    
    private static String className = SocketHandlerTest.class.getName();

    private SocketHandler h = null;

    private Properties props;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        errSubstituteStream = new NullOutputStream();
        System.setErr(new PrintStream(errSubstituteStream));  
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        initProps();
        LOG_MANAGER.reset();
        LOG_MANAGER.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        CallVerificationStack.getInstance().clear();
        if (null != h) {
            h.close();
            h = null;
        }
        System.setErr(err);
        super.tearDown();
    }
    

    private void initProps() throws Exception {
        props = new Properties();
        props.put("handlers", className + "$MockHandler " + className
                + "$MockHandler");
        props.put("java.util.logging.FileHandler.pattern", "%h/java%u.log");
        props.put("java.util.logging.FileHandler.limit", "50000");
        props.put("java.util.logging.FileHandler.count", "5");
        props.put("java.util.logging.FileHandler.formatter",
                "java.util.logging.XMLFormatter");
        props.put(".level", "FINE");
        props.put("java.util.logging.ConsoleHandler.level", "OFF");
        props.put("java.util.logging.ConsoleHandler.formatter",
                "java.util.logging.SimpleFormatter");
        props.put("foo.handlers", "java.util.logging.ConsoleHandler");
        props.put("foo.level", "WARNING");
        props.put("com.xyz.foo.level", "SEVERE");
    }

    /*
     * Test the constructor with no relevant log manager properties are set.
     */
    public void testConstructor_NoProperties() throws Exception {
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.level"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.filter"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.formatter"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.encoding"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.host"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.port"));

        try {
            h = new SocketHandler();
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }

        try {
            h = new SocketHandler(null, 0);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }

        try {
            h = new SocketHandler("", 0);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }

        try {
            h = new SocketHandler("127.0.0.1", -1);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }

        try {
            h = new SocketHandler("127.0.0.1", Integer.MAX_VALUE);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }

        try {
            h = new SocketHandler("127.0.0.1", 66666);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }

        try {
            h = new SocketHandler("127.0.0.1", 0);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);

        h = new SocketHandler("127.0.0.1", 6666);
        assertSame(h.getLevel(), Level.ALL);
        assertTrue(h.getFormatter() instanceof XMLFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        h.close();
        // ensure the thread exits and the port becomes available again
        thread.getReadString();
    }

    /*
     * Test the constructor with no relevant log manager properties are set
     * except host and port.
     */
    public void testConstructor_NoBasicProperties() throws Exception {
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.level"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.filter"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.formatter"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.encoding"));
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);

        h = new SocketHandler();
        assertSame(h.getLevel(), Level.ALL);
        assertTrue(h.getFormatter() instanceof XMLFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        h.close();
        // ensure the thread exits and the port becomes available again
        thread.getReadString();

        try {
            h = new SocketHandler("127.0.sdfcdsfsa%%&&^0.1", 6665);
            fail("Should throw IOException!");
        } catch (IOException e) {
        }
    }

    /*
     * Test the constructor with insufficient privilege for connection.
     */
    public void testConstructor_InsufficientPrivilege() throws Exception {
        SecurityManager oldMan = null;
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", "FINE");
        p.put("java.util.logging.SocketHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.encoding", "utf-8");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockNoSocketSecurityManager());
        try {
            new SocketHandler();
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        System.setSecurityManager(new MockNoSocketSecurityManager());
        try {
            new SocketHandler("127.0.0.1", 6666);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }

    /*
     * Test the constructor with valid relevant log manager properties are set.
     */
    public void testConstructor_ValidProperties() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", "FINE");
        p.put("java.util.logging.SocketHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.encoding", "iso-8859-1");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);

        h = new SocketHandler();
        assertSame(h.getLevel(), Level.parse("FINE"));
        assertTrue(h.getFormatter() instanceof MockFormatter);
        assertTrue(h.getFilter() instanceof MockFilter);
        assertEquals(h.getEncoding(), "iso-8859-1");
        h.close();
        // ensure the thread exits and the port becomes available again
        thread.getReadString();

        // start the server to be ready to accept log messages
        thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);

        h = new SocketHandler("127.0.0.1", 6666);
        assertSame(h.getLevel(), Level.parse("FINE"));
        assertTrue(h.getFormatter() instanceof MockFormatter);
        assertTrue(h.getFilter() instanceof MockFilter);
        assertEquals(h.getEncoding(), "iso-8859-1");
        h.close();
        // ensure the thread exits and the port becomes available again
        thread.getReadString();
    }

    /*
     * Test the constructor with invalid relevant log manager properties are set
     * except host and port.
     */
    public void testConstructor_InvalidBasicProperties() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", INVALID_LEVEL);
        p.put("java.util.logging.SocketHandler.filter", className + "");
        p.put("java.util.logging.SocketHandler.formatter", className + "");
        p.put("java.util.logging.SocketHandler.encoding", "XXXX");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);

        h = new SocketHandler();
        assertSame(h.getLevel(), Level.ALL);
        assertTrue(h.getFormatter() instanceof XMLFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        h.publish(new LogRecord(Level.SEVERE, "test"));
        assertNull(h.getEncoding());
        h.close();
        // ensure the thread exits and the port becomes available again
        thread.getReadString();

        // start the server to be ready to accept log messages
        thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);

        h = new SocketHandler("127.0.0.1", 6666);
        assertSame(h.getLevel(), Level.ALL);
        assertTrue(h.getFormatter() instanceof XMLFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        h.publish(new LogRecord(Level.SEVERE, "test"));
        assertNull(h.getEncoding());
        h.close();
        // ensure the thread exits and the port becomes available again
        thread.getReadString();
    }

    /*
     * Test the constructor with valid relevant log manager properties are set
     * except port.
     */
    public void testConstructor_InvalidPort() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", "FINE");
        p.put("java.util.logging.SocketHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.encoding", "iso-8859-1");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666i");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        try {
            h = new SocketHandler();
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {

        }
    }

    /*
     * Test the constructor with valid relevant log manager properties are set,
     * but the port is not open.
     */
    public void testConstructor_NotOpenPort() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", "FINE");
        p.put("java.util.logging.SocketHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.encoding", "iso-8859-1");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6665");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        try {
            h = new SocketHandler();
            fail("Should throw IOException!");
        } catch (IOException e) {

        }

        try {
            h = new SocketHandler("127.0.0.1", 6665);
            fail("Should throw IOException!");
        } catch (IOException e) {

        }
    }

    /*
     * Test the constructor with valid relevant log manager properties are set
     * except port.
     */
    public void testConstructor_InvalidHost() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", "FINE");
        p.put("java.util.logging.SocketHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.encoding", "iso-8859-1");
        p.put("java.util.logging.SocketHandler.host", " 34345 #$#%$%$");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        try {
            h = new SocketHandler();
            fail("Should throw IOException!");
        } catch (IOException e) {

        }

        try {
            h = new SocketHandler(" 34345 #$#%$%$", 6666);
            fail("Should throw IOException!");
        } catch (IOException e) {

        }
    }

    /*
     * Test close() when having sufficient privilege, and a record has been
     * written to the output stream.
     */
    public void testClose_SufficientPrivilege_NormalClose() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);

        h = new SocketHandler();
        h.publish(new LogRecord(Level.SEVERE,
                "testClose_SufficientPrivilege_NormalClose msg"));
        h.close();
        assertEquals("MockFormatter_Head"
                + "testClose_SufficientPrivilege_NormalClose msg"
                + "MockFormatter_Tail", thread.getReadString());
        h.close();
    }

    /*
     * Test close() when having sufficient privilege, and no record has been
     * written to the output stream.
     */
    public void testClose_SufficientPrivilege_DirectClose() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);

        h = new SocketHandler();
        h.setLevel(Level.INFO);

        h.close();
        assertEquals("MockFormatter_Head" + "MockFormatter_Tail", thread
                .getReadString());
    }

    /*
     * Test close() when having insufficient privilege.
     */
    public void testClose_InsufficientPrivilege() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);

        h = new SocketHandler();
        h.setLevel(Level.INFO);

        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            h.close();
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
            h.close();
            // ensure the thread exits and the port becomes available again
            thread.getReadString();
        }
    }

    /*
     * Test publish(), use no filter, having output stream, normal log record.
     */
    public void testPublish_NoFilter() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);

        h = new SocketHandler();
        h.setLevel(Level.INFO);

        LogRecord r = new LogRecord(Level.INFO, "testPublish_NoFilter");
        h.setLevel(Level.INFO);
        h.publish(r);

        h.setLevel(Level.WARNING);
        h.publish(r);

        h.setLevel(Level.CONFIG);
        h.publish(r);

        r.setLevel(Level.OFF);
        h.setLevel(Level.OFF);
        h.publish(r);
        h.close();
        assertEquals("MockFormatter_Head" + "testPublish_NoFilter"
                + "testPublish_NoFilter" + "MockFormatter_Tail", thread
                .getReadString());
    }

    /*
     * Test publish(), use a filter, having output stream, normal log record.
     */
    public void testPublish_WithFilter() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        h.setFilter(new MockFilter());

        System.setErr(new PrintStream(new ByteArrayOutputStream()));

        LogRecord r = new LogRecord(Level.INFO, "testPublish_WithFilter");
        h.setLevel(Level.INFO);
        h.publish(r);
        h.close();
        assertEquals("MockFormatter_Head" + "MockFormatter_Tail", thread
                .getReadString());
    }

    /*
     * Test publish(), null log record, having output stream
     */
    public void testPublish_Null() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        try {
            h.publish(null);
        } finally {
            h.close();
            // ensure the thread exits and the port becomes available again
            thread.getReadString();
        }
    }

    /*
     * Test publish(), a log record with empty msg, having output stream
     */
    public void testPublish_EmptyMsg() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        LogRecord r = new LogRecord(Level.INFO, "");
        h.publish(r);
        h.close();
        assertEquals("MockFormatter_Head" + "MockFormatter_Tail", thread
                .getReadString());
    }

    /*
     * Test publish(), a log record with null msg, having output stream
     */
    public void testPublish_NullMsg() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        LogRecord r = new LogRecord(Level.INFO, null);
        h.publish(r);
        h.close();
        assertEquals("MockFormatter_Head" + "MockFormatter_Tail", thread
                .getReadString());
    }

    /*
     * Test publish(), after close.
     */
    public void testPublish_AfterClose() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));

        // start the server to be ready to accept log messages
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.FINE);

        assertSame(h.getLevel(), Level.FINE);
        LogRecord r = new LogRecord(Level.INFO, "testPublish_NoFormatter");
        assertTrue(h.isLoggable(r));
        h.close();
        // ensure the thread exits and the port becomes available again
        thread.getReadString();
        // assertFalse(h.isLoggable(r));
        h.publish(r);
        h.flush();
        // assertEquals("MockFormatter_Head",
        // this.errSubstituteStream.toString());
    }

    /*
     * A mock filter, always return false.
     */
    public static class MockFilter implements Filter {

        public boolean isLoggable(LogRecord record) {
            CallVerificationStack.getInstance().push(record);
            // System.out.println("filter called...");
            return false;
        }
    }

    /*
     * A mock formatter.
     */
    public static class MockFormatter extends Formatter {
        public String format(LogRecord r) {
            // System.out.println("formatter called...");
            return super.formatMessage(r);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.logging.Formatter#getHead(java.util.logging.Handler)
         */
        public String getHead(Handler h) {
            return "MockFormatter_Head";
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.logging.Formatter#getTail(java.util.logging.Handler)
         */
        public String getTail(Handler h) {
            return "MockFormatter_Tail";
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
     * Used to grant all permissions except logging control.
     */
    public static class MockNoSocketSecurityManager extends SecurityManager {

        public MockNoSocketSecurityManager() {
        }

        public void checkPermission(Permission perm) {
        }

        public void checkPermission(Permission perm, Object context) {
        }

        public void checkConnect(String host, int port) {
            throw new SecurityException();
        }
    }

    /*
     * A mock stream handler, expose setOutputStream.
     */
    public static class MockSocketHandler extends SocketHandler {
        public MockSocketHandler() throws Exception {
            super();
        }

        public void setOutputStream(OutputStream out) {
            super.setOutputStream(out);
        }

        public boolean isLoggable(LogRecord r) {
            CallVerificationStack.getInstance().push(r);
            return super.isLoggable(r);
        }
    }

    /*
     * A server thread that accepts an incoming connection request and reads any
     * incoming data into an byte array.
     */
    public static class ServerThread extends Thread {

        private volatile StringBuffer sb = new StringBuffer();

        private volatile boolean finished = false;

        public boolean finished() {
            return this.finished;
        }

        public String getReadString() throws Exception {
            int i = 0;
            while (!this.finished) {
                sleep(100);
                if (++i > 100) {
                    // connect to port 6666 to stop the listening.
                    try {
                        Socket s = new Socket("127.0.0.1", 6666);
                        OutputStream os = s.getOutputStream();
                        os.write(1);
                        os.close();
                        s.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            return this.sb.toString();
        }

        public void run() {
            ServerSocket ss = null;
            Socket s = null;
            InputStreamReader reader = null;
            try {
                char[] buffer = new char[32];
                ss = new ServerSocket(6666);
                s = ss.accept();
                reader = new InputStreamReader(s.getInputStream());
                while (true) {
                    int length = reader.read(buffer);
                    if (-1 == length) {
                        break;
                    }
                    this.sb.append(buffer, 0, length);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            } finally {
                try {
                    if (null != reader) {
                        reader.close();
                        s.close();
                        ss.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                this.finished = true;
            }
        }
    }

}
