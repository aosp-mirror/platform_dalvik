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
 * Some methods in ObjectInputStream and ObjectOutputStream can only be called
 * from a nested call to readObject() or writeObject(). Any attempt to call them
 * from another context will cause this exception to be thrown. The list of
 * methods that are protected this way is:
 * <ul>
 * <li>ObjectInputStream.defaultReadObject()</li>
 * <li>ObjectInputStream.registerValidation()</li>
 * <li>ObjectOutputStream.defaultWriteObject()</li>
 * </ul>
 * 
 * @see ObjectInputStream#defaultReadObject()
 * @see ObjectInputStream#registerValidation(ObjectInputValidation, int)
 * @see ObjectOutputStream#defaultWriteObject()
 */
public class NotActiveException extends ObjectStreamException {

    private static final long serialVersionUID = -3893467273049808895L;

    /**
     * Constructs a new instance of this class with its walkback filled in.
     */
    public NotActiveException() {
        super();
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param detailMessage
     *            The detail message for the exception.
     */
    public NotActiveException(String detailMessage) {
        super(detailMessage);
    }
}
