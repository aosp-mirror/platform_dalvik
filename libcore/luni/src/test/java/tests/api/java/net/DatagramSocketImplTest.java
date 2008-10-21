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

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;

public class DatagramSocketImplTest extends junit.framework.TestCase {
    /**
     * @tests java.net.DatagramSocketImpl#DatagramSocketImpl()
     */
    public void test_Constructor() throws Exception {
        // regression test for Harmony-1117
        MockDatagramSocketImpl impl = new MockDatagramSocketImpl();
        assertNull(impl.getFileDescriptor());
    }
}

class MockDatagramSocketImpl extends DatagramSocketImpl {

    @Override
    public FileDescriptor getFileDescriptor() {
        return super.getFileDescriptor();
    }

    @Override
    protected void bind(int port, InetAddress addr) throws SocketException {
        // empty
    }

    @Override
    protected void close() {
        // empty
    }

    @Override
    protected void create() throws SocketException {
        // empty
    }

    @Override
    public Object getOption(int optID) throws SocketException {
        return null;
    }

    @Override
    protected byte getTTL() throws IOException {
        return 0;
    }

    @Override
    protected int getTimeToLive() throws IOException {
        return 0;
    }

    @Override
    protected void join(InetAddress addr) throws IOException {
        // empty
    }

    @Override
    protected void joinGroup(SocketAddress addr, NetworkInterface netInterface)
            throws IOException {
        // empty
    }

    @Override
    protected void leave(InetAddress addr) throws IOException {
        // empty
    }

    @Override
    protected void leaveGroup(SocketAddress addr, NetworkInterface netInterface)
            throws IOException {
        // empty
    }

    @Override
    protected int peek(InetAddress sender) throws IOException {
        return 0;
    }

    @Override
    protected int peekData(DatagramPacket pack) throws IOException {
        return 0;
    }

    @Override
    protected void receive(DatagramPacket pack) throws IOException {
        // empty
    }

    @Override
    protected void send(DatagramPacket pack) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOption(int optID, Object val) throws SocketException {
        // empty
    }

    @Override
    protected void setTTL(byte ttl) throws IOException {
        // empty
    }

    @Override
    protected void setTimeToLive(int ttl) throws IOException {
        // empty
    }

}
