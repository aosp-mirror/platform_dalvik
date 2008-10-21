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
 * When an implementation of ObjectOutput.writeObject() is passed an object that
 * is not serializable, it will throw this type of exception. This can happen if
 * the object does not implement Serializable or Externalizable, or if it is
 * Serializable but it overrides writeObject(ObjectOutputStream) and explicitely
 * decides it wants to prevent serialization, by throwing this type of
 * exception.
 * 
 * @see ObjectOutputStream#writeObject(Object)
 * @see ObjectOutput#writeObject(Object)
 */
public class NotSerializableException extends ObjectStreamException {

    private static final long serialVersionUID = 2906642554793891381L;

    /**
     * Constructs a new instance of this class with its walkback filled in.
     * 
     */
    public NotSerializableException() {
        super();
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param detailMessage
     *            The detail message for the exception.
     */
    public NotSerializableException(String detailMessage) {
        super(detailMessage);
    }
}
