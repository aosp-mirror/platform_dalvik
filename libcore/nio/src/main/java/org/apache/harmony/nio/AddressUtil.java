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

/*
 * Android Notice 
 * In this class the address length was changed from long to int.
 * This is due to performance optimizations for the device.
 */

package org.apache.harmony.nio;

import java.io.FileDescriptor;
import java.nio.Buffer;
import java.nio.channels.Channel;

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
    
    /**
     * Gets the address of native resource held by the given channel, if has
     * any.
     * 
     * For network related channel, including SocketChannel, ServerSocketChannel
     * and DatagramChannel, this method returns a int of Socket handler in Linux
     * while returns a SOCKET (UINT_PTR) in windows.
     * 
     * For FileChannel, this method returns the native file descriptor.
     * 
     * For other channels, this method return 0, which means unsupported
     * operation.
     * 
     * @param channel
     *            the given channel which may holds a native resource address
     * @return the address of native resource held by the given channel, if any,
     *         otherwise return 0
     */
    public static int getChannelAddress(Channel channel){
        if(channel instanceof FileDescriptorHandler){
            return getFDAddress(((FileDescriptorHandler) channel).getFD());    
        }else if(channel instanceof FileChannelImpl){
            return ((FileChannelImpl) channel).getHandle();    
        }
        return 0;
    }

    private static native int getFDAddress(FileDescriptor fd);
}