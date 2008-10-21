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
 * This runtime exception is thrown when the an array is indexed with a value
 * less than zero, or greater than or equal to the size of the array.
 */
public class ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException {

    private static final long serialVersionUID = -5116101128118950844L;

    /**
     * Constructs a new instance of this class with its walkback filled in.
     */
    public ArrayIndexOutOfBoundsException() {
        super();
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * (which is based on the argument which is the index which failed) filled
     * in.
     * 
     * @param index
     *            int the offending index.
     */
    public ArrayIndexOutOfBoundsException(int index) {
        super(Msg.getString("K0052", index)); //$NON-NLS-1$
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param detailMessage
     *            String The detail message for the exception.
     */
    public ArrayIndexOutOfBoundsException(String detailMessage) {
        super(detailMessage);
    }
}
