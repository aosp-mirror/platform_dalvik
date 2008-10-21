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

package java.net;

/**
 * An attempt to connect to a socket of the wrong type (stream or nonstream)
 * will trigger this exception. An invalid operation applied on the protocol
 * that doesn't support it will also throw this exception.
 * 
 * @see URL
 */
public class ProtocolException extends java.io.IOException {

    private static final long serialVersionUID = -6098449442062388080L;

    /**
     * Constructs a new instance of this class with its walkback filled in.
     */
    public ProtocolException() {
        super();
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param detailMessage
     *            String The detail message for the exception.
     */
    public ProtocolException(String detailMessage) {
        super(detailMessage);
    }
}
