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
 * A problem was found with the class of one of the objects being serialized or
 * deserialized. These can be
 * <ul>
 * <li>The SUIDs of the class loaded by the VM and the serialized class info do
 * not match</li>
 * <li>A serializable or externalizable object cannot be instantiated (when
 * deserializing) because the empty constructor that needs to be run is not
 * visible or fails.</li>
 * </ul>
 * 
 * @see ObjectInputStream #readObject()
 * @see ObjectInputValidation#validateObject()
 */
public class InvalidClassException extends ObjectStreamException {

    private static final long serialVersionUID = -4333316296251054416L;

    /**
     * The fully qualified name of the class that caused the problem
     */
    public String classname;

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param detailMessage
     *            The detail message for the exception.
     */
    public InvalidClassException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new instance of this class with its walkback, message and
     * the fully qualified name of the class which caused the exception filled
     * in.
     * 
     * @param className
     *            The detail message for the exception.
     * @param detailMessage
     *            The detail message for the exception.
     */
    public InvalidClassException(String className, String detailMessage) {
        super(detailMessage);
        this.classname = className;
    }

    /**
     * Returns the extra information message which was provided when the
     * exception was created. If no message was provided at creation time, then
     * answer null. If a message was provided and a class name which caused the
     * exception, the values are concatenated and returned.
     * 
     * @return The receiver's message, possibly concatenated with the name of
     *         the class that caused the problem.
     */
    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (classname != null) {
            msg = classname + "; " + msg; //$NON-NLS-1$
        }
        return msg;
    }
}
