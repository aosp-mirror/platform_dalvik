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

/**
* @author Alexander V. Astapchuk
* @version $Revision$
*/

package java.security;

/**
 * This runtime exception is thrown when an access control check indicates that
 * access should not be granted.
 * 
 */
public class AccessControlException extends SecurityException {

    private static final long serialVersionUID = 5138225684096988535L;

    /**
     * @com.intel.drl.spec_ref 
     */
    private Permission perm; // Named as demanded by Serialized Form.

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * 
     * @param message
     *            String The detail message for the exception.
     */
    public AccessControlException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of this class with its walkback, message and
     * associated permission all filled in.
     * 
     * 
     * @param message
     *            String The detail message for the exception.
     * @param perm
     *            Permission The failed permission.
     */
    public AccessControlException(String message, Permission perm) {
        super(message);
        this.perm = perm;
    }

    /**
     * Returns the receiver's permission.
     * 
     * 
     * @return Permission the receiver's permission
     */
    public Permission getPermission() {
        return perm;
    }
}
