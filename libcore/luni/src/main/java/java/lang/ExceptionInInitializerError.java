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

/**
 * This error is thrown when an exception occurs during class initialization.
 */
public class ExceptionInInitializerError extends LinkageError {

    private static final long serialVersionUID = 1521711792217232256L;

    private Throwable exception;

    /**
     * Constructs a new instance of this class with its walkback filled in.
     */
    public ExceptionInInitializerError() {
        super();
        initCause(null);
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param detailMessage
     *            String The detail message for the exception.
     */
    public ExceptionInInitializerError(String detailMessage) {
        super(detailMessage);
        initCause(null);
    }

    /**
     * Constructs a new instance of this class with its walkback and exception
     * filled in. The exception should be the one which originally occurred in
     * the class initialization code.
     * 
     * @param exception
     *            Throwable The exception which caused the problem.
     */
    public ExceptionInInitializerError(Throwable exception) {
        super();
        this.exception = exception;
        initCause(exception);
    }

    /**
     * Returns the exception which was passed in when the instance was created.
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Returns the cause of this Throwable, or null if there is no cause.
     * 
     * @return Throwable The receiver's cause.
     */
    @Override
    public Throwable getCause() {
        return exception;
    }
}
