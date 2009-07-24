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

package org.apache.harmony.luni.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * The class contains static {@link java.io.InputStream} utilities.
 */
public class InputStreamExposer {

    /**
     * Provides access to a protected underlying buffer of
     * <code>ByteArrayInputStream</code>.
     */
    private static final Field BAIS_BUF;

    /**
     * Provides access to a protected position in the underlying buffer of
     * <code>ByteArrayInputStream</code>.
     */
    private static final Field BAIS_POS;

    static {
        final Field[] f = new Field[2];
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    f[0] = ByteArrayInputStream.class.getDeclaredField("buf");
                    f[0].setAccessible(true);
                    f[1] = ByteArrayInputStream.class.getDeclaredField("pos");
                    f[1].setAccessible(true);
                } catch (NoSuchFieldException nsfe) {
                    throw new InternalError(nsfe.getLocalizedMessage());
                }
                return null;
            }
        });
        BAIS_BUF = f[0];
        BAIS_POS = f[1];
    }

    /**
     * Reads all bytes from {@link java.io.ByteArrayInputStream} using its
     * underlying buffer directly.
     *
     * @return an underlying buffer, if a current position is at the buffer
     *         beginning, and an end position is at the buffer end, or a copy of
     *         the underlying buffer part.
     */
    private static byte[] expose(ByteArrayInputStream bais) {
        byte[] buffer, buf;
        int pos;
        synchronized (bais) {
            int available = bais.available();
            try {
                buf = (byte[]) BAIS_BUF.get(bais);
                pos = BAIS_POS.getInt(bais);
            } catch (IllegalAccessException iae) {
                throw new InternalError(iae.getLocalizedMessage());
            }
            if (pos == 0 && available == buf.length) {
                buffer = buf;
            } else {
                buffer = new byte[available];
                System.arraycopy(buf, pos, buffer, 0, available);
            }
            bais.skip(available);
        }
        return buffer;
    }

    /**
     * The utility method for reading the whole input stream into a snapshot
     * buffer. To speed up the access it works with an underlying buffer for a
     * given {@link java.io.ByteArrayInputStream}.
     *
     * @param is
     *            the stream to be read.
     * @return the snapshot wrapping the buffer where the bytes are read to.
     * @throws UnsupportedOperationException if the input stream data cannot be exposed
     */
    public static byte[] expose(InputStream is) throws IOException, UnsupportedOperationException {
        // BEGIN android-changed
        // if (is instanceof ExposedByteArrayInputStream) {
        //     return ((ExposedByteArrayInputStream) is).expose();
        // }
        // END android-changed

        if (is.getClass().equals(ByteArrayInputStream.class)) {
            return expose((ByteArrayInputStream) is);
        }

        // We don't know how to do this
        throw new UnsupportedOperationException();
    }
}
