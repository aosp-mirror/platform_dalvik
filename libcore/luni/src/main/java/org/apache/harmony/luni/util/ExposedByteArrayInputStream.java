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

/**
 * The extension of <code>ByteArrayInputStream</code> which exposes an
 * underlying buffer.
 */
public class ExposedByteArrayInputStream extends ByteArrayInputStream {

    /**
     * @see java.io.ByteArrayInputStream(byte[])
     */
    public ExposedByteArrayInputStream(byte buf[]) {
        super(buf);
    }

    /**
     * @see java.io.ByteArrayInputStream(byte[], int, int)
     */
    public ExposedByteArrayInputStream(byte buf[], int offset, int length) {
        super(buf, offset, length);
    }

    /**
     * Reads the whole stream and returns the stream snapshot.
     */
    public synchronized byte[] expose() {
        if (pos == 0 && count == buf.length) {
            skip(count);
            return buf;
        }

        final int available = available();
        final byte[] buffer = new byte[available];
        System.arraycopy(buf, pos, buffer, 0, available);
        skip(available);
        return buffer;
    }
}
