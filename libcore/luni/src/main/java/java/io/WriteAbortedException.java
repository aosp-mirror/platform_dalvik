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
 * This type of exception is thrown by readObject() when it detects an exception
 * marker in the input stream. This marker indicates that when the object was
 * being serialized, an exception happened and this marker was inserted instead
 * of the original object. It is a way to "propagate" an exception from the code
 * that attempted to write the object to the code that is attempting to read the
 * object.
 * 
 * @see ObjectInputStream#readObject()
 */
public class WriteAbortedException extends ObjectStreamException {

    private static final long serialVersionUID = -3326426625597282442L;

    /**
     * The exception that was caused when writeObject() was attempting to
     * serialize the object
     */
    public Exception detail;

    /**
     * Constructs a new instance of this class with its walkback, message and
     * the exception which caused the underlying problem when serializing the
     * object filled in.
     * 
     * @param detailMessage
     *            the detail message for the exception.
     * @param rootCause
     *            exception that caused the problem when serializing the object.
     */
    public WriteAbortedException(String detailMessage, Exception rootCause) {
        super(detailMessage);
        detail = rootCause;
        initCause(rootCause);
    }

    /**
     * Returns the extra information message which was provided when the
     * throwable was created. If no message was provided at creation time, then
     * answer null.
     * 
     * @return the receiver's message.
     */
    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (detail != null) {
            msg = msg + "; " + detail.toString(); //$NON-NLS-1$
        }
        return msg;
    }

    /**
     * Returns the cause of this Throwable, or null if there is no cause.
     * 
     * @return the receiver's cause.
     */
    @Override
    public Throwable getCause() {
        return detail;
    }
}
