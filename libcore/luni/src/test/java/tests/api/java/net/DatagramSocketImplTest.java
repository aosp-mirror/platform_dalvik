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
import java.net.DatagramPacket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;

/*
 * DatagramSocketImplFactory can be specified only once, 
 * therefore we can't check DatagramSocketImpl functionality.
 */

@TestTargetClass(value = DatagramSocketImpl.class,
                 untestedMethods = {
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "bind",
                     args = {int.class, InetAddress.class}
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
                     method = "create",
                     args = {}
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "getTimeToLive",
                     args = {}
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "getTTL",
                     args = {}
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "join",
                     args = {InetAddress.class}
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "joinGroup",
                     args = { SocketAddress.class, NetworkInterface.class }
                 ),  
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "leave",
                     args = { InetAddress.class }
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "leaveGroup",
                     args = { SocketAddress.class, NetworkInterface.class }
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "peek",
                     args = { InetAddress.class }
                 ),     
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "peekData",
                     args = { DatagramPacket.class }
                 ),    
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "receive",
                     args = { DatagramPacket.class }
                 ), 
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "send",
                     args = { DatagramPacket.class }
                 ),    
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "setTimeToLive",
                     args = { int.class }
                 ),     
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "setTTL",
                     args = { byte.class }
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "setOption",
                     args = { int.class, Object.class }
                 ),
                 @TestTargetNew(
                     level = TestLevel.NOT_FEASIBLE,
                     notes = "",
                     method = "getOption",
                     args = { int.class }
                 )                 
             }) 
public class DatagramSocketImplTest extends junit.framework.TestCase {
    
    MockDatagramSocketImpl ds;
    
    public void setUp() {
        ds = new MockDatagramSocketImpl();
    }
    
    public void tearDown() {
        ds.close();
        ds = null;
    }
    /**
     * @tests java.net.DatagramSocketImpl#DatagramSocketImpl()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DatagramSocketImpl",
        args = {}
    )
    public void test_Constructor() throws Exception {
        // regression test for Harmony-1117
        MockDatagramSocketImpl impl = new MockDatagramSocketImpl();
        assertNull(impl.getFileDescriptor());
    }
    
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SocketException is not checked.",
        method = "connect",
        args = {java.net.InetAddress.class, int.class}
    )
    public void test_connect() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            //int port = ds.getLocalPort();
            ds.connect(localHost, 0);
            DatagramPacket send = new DatagramPacket(new byte[10], 10,
                    localHost, 0);
            ds.send(send);
        } catch (IOException e) {
            fail("Unexpected IOException : " + e.getMessage());
        } finally {
            ds.close();
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "disconnect",
        args = {}
    )
    public void test_disconnect() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            //int port = ds.getLocalPort();
            ds.connect(localHost, 0);
            DatagramPacket send = new DatagramPacket(new byte[10], 10,
                    localHost, 0);
            ds.send(send);
            ds.disconnect();
        } catch (IOException e) {
            fail("Unexpected IOException : " + e.getMessage());
        } finally {
            ds.close();
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFileDescriptor",
        args = {}
    )
    public void test_getFileDescriptor() {
        assertNull(ds.getFileDescriptor());
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalPort",
        args = {}
    )
    public void test_getLocalPort() {
        // RI fails here. RI returns 0. the spec doesn't say what the value for
        // an unbound DatagramSocket should be. The same difference can be seen
        // for Socket.getLocalPort. But there the spec says that unbound
        // Sockets return -1. And for that method also the RI returns 0 which
        // goes against the spec.
        assertEquals(-1, ds.getLocalPort());
    }
}

class MockDatagramSocketImpl extends DatagramSocketImpl {

    @Override
    public FileDescriptor getFileDescriptor() {
        return super.getFileDescriptor();
    }

    @Override
    public void bind(int port, InetAddress addr) throws SocketException {
        // empty
    }

    @Override
    public void close() {
        // empty
    }

    @Override
    public void create() throws SocketException {
        // empty
    }


    @Override
    public byte getTTL() throws IOException {
        return 0;
    }

    @Override
    public int getTimeToLive() throws IOException {
        return 0;
    }

    @Override
    public void join(InetAddress addr) throws IOException {
        // empty
    }

    @Override
    public void joinGroup(SocketAddress addr, NetworkInterface netInterface)
            throws IOException {
        // empty
    }

    @Override
    public void leave(InetAddress addr) throws IOException {
        // empty
    }

    @Override
    public void leaveGroup(SocketAddress addr, NetworkInterface netInterface)
            throws IOException {
        // empty
    }

    @Override
    public int peek(InetAddress sender) throws IOException {
        return 0;
    }

    @Override
    public int peekData(DatagramPacket pack) throws IOException {
        return 0;
    }

    @Override
    public void receive(DatagramPacket pack) throws IOException {
        // empty
    }

    @Override
    public void send(DatagramPacket pack) throws IOException {
        // TODO Auto-generated method stub

    }


    @Override
    public void setTTL(byte ttl) throws IOException {
        // empty
    }

    @Override
    public void setTimeToLive(int ttl) throws IOException {
        // empty
    }

    public Object getOption(int optID) throws SocketException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setOption(int optID, Object value) throws SocketException {
        // TODO Auto-generated method stub
        
    }

    public void connect(InetAddress address, int port) throws SocketException {
        super.connect(address, port);
    }
    
    public void disconnect() {
        super.disconnect();
    }
    
    public int getLocalPort() {
        return super.getLocalPort();
    }
}
