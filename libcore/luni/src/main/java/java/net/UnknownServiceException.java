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

import java.io.IOException;

/**
 * This UnknownServiceException is thrown when a particular service requested
 * isn't support by the URL. Examples are attempts to read from an URL via an
 * <code>InputStream</code> or write to an URL via an
 * <code>OutputStream</code>
 */
public class UnknownServiceException extends IOException {

    private static final long serialVersionUID = -4169033248853639508L;

    /**
     * Constructs a new instance of this class with its walkback filled in.
     */
    public UnknownServiceException() {
        super();
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param detailMessage
     *            String The detail message for the exception.
     */
    public UnknownServiceException(String detailMessage) {
        super(detailMessage);
    }
}
