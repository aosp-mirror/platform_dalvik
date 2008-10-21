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

package java.lang.reflect;

/**
 * This class provides a wrapper for an exception thrown by a Method or
 * Constructor invocation.
 * 
 * @see Method#invoke
 * @see Constructor#newInstance
 */
public class InvocationTargetException extends Exception {

    private static final long serialVersionUID = 4085088731926701167L;

    private Throwable target;

    /**
     * Constructs a new instance of this class with its walkback filled in.
     */
    protected InvocationTargetException() {
        super((Throwable) null);
    }

    /**
     * Constructs a new instance of this class with its walkback and target
     * exception filled in.
     * 
     * @param exception
     *            Throwable The exception which occurred while running the
     *            Method or Constructor.
     */
    public InvocationTargetException(Throwable exception) {
        super(null, exception);
        target = exception;
    }

    /**
     * Constructs a new instance of this class with its walkback, target
     * exception and message filled in.
     * 
     * @param detailMessage
     *            String The detail message for the exception.
     * @param exception
     *            Throwable The exception which occurred while running the
     *            Method or Constructor.
     */
    public InvocationTargetException(Throwable exception, String detailMessage) {
        super(detailMessage, exception);
        target = exception;
    }

    /**
     * Returns the exception which caused the receiver to be thrown.
     */
    public Throwable getTargetException() {
        return target;
    }

    /**
     * Returns the cause of this Throwable, or null if there is no cause.
     * 
     * @return Throwable The receiver's cause.
     */
    @Override
    public Throwable getCause() {
        return target;
    }
}
