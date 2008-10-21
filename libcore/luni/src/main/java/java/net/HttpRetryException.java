/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.net;

import java.io.IOException;

/**
 * The exception to be thrown when a request cannot be retried.
 */
public class HttpRetryException extends IOException {

    private static final long serialVersionUID = -9186022286469111381L;

    private int responseCode;

    private String location = null;

    /**
     * new a HttpRetryException by given detail message and responseCode
     * 
     * @param detail
     *            detail for this exception
     * @param code
     *            http response code to return
     */
    public HttpRetryException(String detail, int code) {
        super(detail);
        responseCode = code;
    }

    /**
     * new a HttpRetryException by given detail message, responseCode and the
     * Location response header
     * 
     * @param detail
     *            detail for this exception
     * @param code
     *            http response code to return
     * @param location
     *            the error resulted from redirection, the Location header can
     *            be recorded
     */
    public HttpRetryException(String detail, int code, String location) {
        super(detail);
        responseCode = code;
        this.location = location;
    }

    /**
     * @return the Location header recorded
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return the detail reason for this exception
     */
    public String getReason() {
        return getMessage();
    }

    /**
     * @return a http response code
     */
    public int responseCode() {
        return responseCode;
    }
}
