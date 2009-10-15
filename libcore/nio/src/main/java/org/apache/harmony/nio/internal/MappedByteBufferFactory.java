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

package org.apache.harmony.nio.internal;

import java.lang.reflect.Constructor;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.harmony.luni.platform.PlatformAddress;

class MappedByteBufferFactory {

    static final Constructor<?> constructor;

    static {
        constructor = AccessController
                .doPrivileged(new PrivilegedAction<Constructor<?>>() {
                    public Constructor<?> run() {
                        try {
                            Class<?> wrapperClazz = ClassLoader
                                    .getSystemClassLoader().loadClass(
                                            "java.nio.MappedByteBufferAdapter"); //$NON-NLS-1$
                            Constructor<?> result = wrapperClazz
                                    .getConstructor(new Class[] {
                                            PlatformAddress.class, int.class,
                                            int.class, int.class });
                            result.setAccessible(true);
                            return result;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    static MappedByteBuffer getBuffer(PlatformAddress addr, int mapmode,
            long size, int offset) throws Exception {
        /*
         * Spec points out explicitly that the size of map should be no greater
         * than Integer.MAX_VALUE, so long to int cast is safe here.
         */
        return (MappedByteBuffer) constructor.newInstance(new Object[] { addr,
                new Integer((int) size), new Integer(offset),
                new Integer(mapmode) });
    }
}
