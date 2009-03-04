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

package org.apache.harmony.luni.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;

/**
 * This class was added so we can create sockets without options that were
 * needed for server sockets. It just overrides create so that we call new
 * natives which only set the options required for plain sockets. In order to
 * preserve behaviour of older versions the create PlainSocketImpl was left as
 * is and this new class was added. For newer versions an instance of this class
 * is used, for earlier versions the original PlainSocketImpl is used.
 */
class PlainSocketImpl2 extends PlainSocketImpl {

    public PlainSocketImpl2(FileDescriptor fd, int localport, InetAddress addr, int port) {
        super();
        super.fd = fd;
        super.localport = localport;
        super.address = addr;
        super.port = port;
    }

    public PlainSocketImpl2() {
        super();
    }

    /**
     * creates an instance with specified proxy.
     */
    public PlainSocketImpl2(Proxy proxy) {
        super();
        this.proxy = proxy;
    }

    @Override
    protected void create(boolean isStreaming) throws IOException {
        streaming = isStreaming;
        if (isStreaming) {
            netImpl.createSocket(fd, NetUtil.preferIPv4Stack());
        } else {
            netImpl.createDatagramSocket(fd, NetUtil.preferIPv4Stack());
        }
    }
}
