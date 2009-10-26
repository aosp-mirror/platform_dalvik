/* Licensed to the Apache Software Foundation (ASF) under one or more
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

/*
 * Android Notice
 * In this class the address length was changed from long to int.
 * This is due to performance optimizations for the device.
 */

package org.apache.harmony.nio;

import java.io.FileDescriptor;
import java.nio.Buffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.harmony.luni.platform.FileDescriptorHandler;
import org.apache.harmony.nio.internal.DirectBuffer;
import org.apache.harmony.nio.internal.FileChannelImpl;

public class AddressUtil {

    /**
     * Gets the start address of a direct buffer.
     * <p>
     * This method corresponds to the JNI function:
     * 
     * <pre>
     *    void* GetDirectBufferAddress(JNIEnv* env, jobject buf);
     * </pre>
     * 
     * @param buf
     *            the direct buffer whose address shall be returned must not be
     *            <code>null</code>.
     * @return the address of the buffer given, or zero if the buffer is not a
     *         direct Buffer.
     */
    public static int getDirectBufferAddress(Buffer buf) {
        if (!(buf instanceof DirectBuffer)) {
            return 0;
        }
        return ((DirectBuffer) buf).getEffectiveAddress().toInt();
    }

    // BEGIN android-removed: dead code (the native side of which was scary!)
    //public static int getChannelAddress(Channel channel);
    //private static native int getFDAddress(FileDescriptor fd);
    // END android-removed
}
