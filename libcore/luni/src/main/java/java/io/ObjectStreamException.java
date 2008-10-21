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

package java.io;

/**
 * This is the superclass of all exceptions that can happen when serializing or
 * deserialing objects. The state of the stream is unknown when one of these
 * serialization-related exceptions are thrown.
 * 
 * @see InvalidObjectException
 * @see NotActiveException
 * @see NotSerializableException
 * @see OptionalDataException
 * @see StreamCorruptedException
 * @see WriteAbortedException
 */
public abstract class ObjectStreamException extends IOException {

    private static final long serialVersionUID = 7260898174833392607L;

    /**
     * Constructs a new instance of this class with its walkback filled in.
     */
    protected ObjectStreamException() {
        super();
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param detailMessage
     *            The detail message for the exception.
     */
    protected ObjectStreamException(String detailMessage) {
        super(detailMessage);
    }
}
