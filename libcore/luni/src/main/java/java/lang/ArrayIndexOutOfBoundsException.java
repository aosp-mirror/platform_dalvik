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

package java.lang;

import org.apache.harmony.luni.util.Msg;

/**
 * Thrown when the an array is indexed with a value less than zero, or greater
 * than or equal to the size of the array.
 * 
 * @since Android 1.0
 */
public class ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException {

    private static final long serialVersionUID = -5116101128118950844L;

    /**
     * Constructs a new {@code ArrayIndexOutOfBoundsException} that includes the
     * current stack trace.
     * 
     * @since Android 1.0
     */
    public ArrayIndexOutOfBoundsException() {
        super();
    }

    /**
     * Constructs a new {@code ArrayIndexOutOfBoundsException} with the current
     * stack trace and a detail message that is based on the specified invalid
     * {@code index}.
     * 
     * @param index
     *            the invalid index.
     * @since Android 1.0
     */
    public ArrayIndexOutOfBoundsException(int index) {
        super(Msg.getString("K0052", index)); //$NON-NLS-1$
    }

    /**
     * Constructs a new {@code ArrayIndexOutOfBoundsException} with the current
     * stack trace and the specified detail message.
     * 
     * @param detailMessage
     *            the detail message for this exception.
     * @since Android 1.0
     */
    public ArrayIndexOutOfBoundsException(String detailMessage) {
        super(detailMessage);
    }
}
