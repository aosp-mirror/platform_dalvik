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

import java.net.SocketException;

import org.apache.harmony.luni.platform.Platform;

/**
 * This class was added so we can create sockets with options that are needed
 * for server sockets. It just overrides create so that we call new natives
 * which only set the options required for server sockets. In order to preserve
 * behaviour of older versions the create PlainSocketImpl was left as is and
 * this new class was added. For newer versions an instance of this class is
 * used, for earlier versions the original PlainSocketImpl is used.
 */
class PlainMulticastSocketImpl extends PlainDatagramSocketImpl {

    @Override
    public void create() throws SocketException {
        Platform.getNetworkSystem().createMulticastSocket(fd, NetUtil.preferIPv4Stack());
    }
}
