/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio;

import org.apache.harmony.luni.platform.PlatformAddress;
import org.apache.harmony.nio.internal.DirectBuffer;

/**
 * This class is used via JNI by code in frameworks/base/.
 */
class NIOAccess {

    /**
     * Returns the underlying native pointer to the data of the given
     * Buffer starting at the Buffer's current position, or 0 if the
     * Buffer is not backed by native heap storage. Note that this is
     * different than what the Harmony implementation calls a "base
     * address."
     *
     * @param Buffer b the Buffer to be queried
     * @return the native pointer to the Buffer's data at its current
     * position, or 0 if there is none
     */
    static long getBasePointer(Buffer b) {
        if (b instanceof DirectBuffer) {
            PlatformAddress address = ((DirectBuffer) b).getEffectiveAddress();
            if (address == null) {
                return 0L;
            }
            return address.toInt() + (b.position() << b._elementSizeShift);
        }
        return 0L;
    }

    /**
     * Returns the number of bytes remaining in the given Buffer. That is,
     * this scales <code>remaining()</code> by the byte-size of elements
     * of this Buffer.
     *
     * @param Buffer b the Buffer to be queried
     * @return the number of remaining bytes
     */
    static int getRemainingBytes(Buffer b) {
        return (b.limit - b.position) << b._elementSizeShift;
    }

    /**
     * Returns the underlying Java array containing the data of the
     * given Buffer, or null if the Buffer is not backed by a Java array.
     *
     * @param Buffer b the Buffer to be queried
     * @return the Java array containing the Buffer's data, or null if
     * there is none
     */
    static Object getBaseArray(Buffer b) {
        return b.hasArray() ? b.array() : null;
    }

    /**
     * Returns the offset in bytes from the start of the underlying
     * Java array object containing the data of the given Buffer to
     * the actual start of the data. This method is only meaningful if
     * getBaseArray() returns non-null.
     *
     * @param Buffer b the Buffer to be queried
     * @return the data offset in bytes to the start of this Buffer's data
     */
    static int getBaseArrayOffset(Buffer b) {
        return b.hasArray() ? (b.arrayOffset() << b._elementSizeShift) : 0;
    }
}
