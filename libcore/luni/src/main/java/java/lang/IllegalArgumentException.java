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
 * This runtime exception is thrown when a method is invoked with an argument
 * which it can not reasonably deal with.
 */
public class IllegalArgumentException extends RuntimeException {

    private static final long serialVersionUID = -5365630128856068164L;

    /**
     * Constructs a new instance of this class with its walkback filled in.
     */
    public IllegalArgumentException() {
        super();
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param detailMessage
     *            String The detail message for the exception.
     */
    public IllegalArgumentException(String detailMessage) {
        super(detailMessage);
    }
    
    /**
     * <p>Constructs a new instance with a message and cause.</p>
     * @param message The message to assign to this exception.
     * @param cause The optional cause of this exception; may be <code>null</code>.
     * @since 1.5
     */
    public IllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * <p>Constructs a new instance with a cause.</p>
     * @param cause The optional cause of this exception; may be <code>null</code>.
     * @since 1.5
     */
    public IllegalArgumentException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }
}
