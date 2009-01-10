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

package tests.api.java.net;

import dalvik.annotation.TestTargetClass; 
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
/*
* SocketImpl functionality is untestable because of 
* Socket.setSocketImplFactory method can be specified only once.
*/

@TestTargetClass(value = SocketImpl.class, 
                 untestedMethods = {
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "accept",
                        args = {SocketImpl.class}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "available",
                        args = {}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "bind",
                        args = {InetAddress.class, int.class}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "close",
                        args = {}
                    ),   
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "connect",
                        args = {String.class, int.class}
                    ),       
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "connect",
                        args = {InetAddress.class, int.class}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "connect",
                        args = {SocketAddress.class, int.class}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "create",
                        args = {boolean.class}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "getFileDescriptor",
                        args = {}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "getInetAddress",
                        args = {}
                    ),     
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "getInputStream",
                        args = {}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "getLocalPort",
                        args = {}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "getOption",
                        args = {int.class}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "getOutputStream",
                        args = {}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "getPort",
                        args = {}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "listen",
                        args = {int.class}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "sendUrgentData",
                        args = {int.class}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "setOption",
                        args = {int.class, Object.class}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "setPerformancePreferences",
                        args = {int.class, int.class, int.class}
                    ),     
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "shutdownInput",
                        args = {}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "shutdownOutput",
                        args = {}
                    ),
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "supportsUrgentData",
                        args = {}
                    ),                    
                    @TestTargetNew(
                        level = TestLevel.NOT_FEASIBLE,
                        notes = "",
                        method = "toString",
                        args = {}
                    )                    
                }) 
public class SocketImplTest extends junit.framework.TestCase {

    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Regression test.",
        method = "SocketImpl",
        args = {}
    )
    public void test_Constructor_fd() throws Exception {
        // regression test for Harmony-1117
        MockSocketImpl mockSocketImpl = new MockSocketImpl();
        assertNull(mockSocketImpl.getFileDescriptor());
    }
    
    // the mock class for test, leave all method empty
    class MockSocketImpl extends SocketImpl{
        
        protected void accept(SocketImpl newSocket) throws IOException {
        }

        protected int available() throws IOException {
            return 0;
        }

        protected void bind(InetAddress address, int port) throws IOException {
        }

        protected void close() throws IOException {
        }

        protected void connect(String host, int port) throws IOException {
        }

        protected void connect(InetAddress address, int port) throws IOException {
        }

        protected void create(boolean isStreaming) throws IOException {
        }

        protected InputStream getInputStream() throws IOException {
            return null;
        }

        public Object getOption(int optID) throws SocketException {
            return null;
        }

        protected OutputStream getOutputStream() throws IOException {
            return null;
        }

        protected void listen(int backlog) throws IOException {
        }

        public void setOption(int optID, Object val) throws SocketException {
        }

        protected void connect(SocketAddress remoteAddr, int timeout) throws IOException {
        }

        protected void sendUrgentData(int value) throws IOException {
        }
        
        public void setPerformancePreference(int connectionTime,
                int latency,
                int bandwidth){
            super.setPerformancePreferences(connectionTime, latency, bandwidth);
        }

        public FileDescriptor getFileDescriptor() {
            return super.getFileDescriptor();
        }
        
        public void shutdownOutput() throws IOException {
            super.shutdownOutput();
        }
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    protected void doneSuite() {
    }
}
