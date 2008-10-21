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
 * This class is a data structure that contains the username and password.
 */
public final class PasswordAuthentication {

    private String userName;

    private char[] password;

    /**
     * Creates an instance of a password authentication with a username and
     * password.
     * 
     * @param userName
     *            java.lang.String the username
     * @param password
     *            char[] the password
     */
    public PasswordAuthentication(String userName, char[] password) {
        this.userName = userName;
        this.password = password.clone();
    }

    /**
     * Returns the reference of the password of this class.
     * 
     * @return char[] the reference of the password
     */
    public char[] getPassword() {
        return password.clone();
    }

    /**
     * Returns the username of this class.
     * 
     * @return java.lang.String the username of this class
     */
    public String getUserName() {
        return userName;
    }
}
