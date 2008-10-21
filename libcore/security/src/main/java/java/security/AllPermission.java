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

package java.security;


/**
 * Subclass of Permission whose instances imply all other permissions. Granting
 * this permission is equivalent to disabling security.
 * 
 */
public final class AllPermission extends Permission {

    /**
     * @serial
     */
    private static final long serialVersionUID = -2916474571451318075L;

    // Permission name
    private static final String ALL_PERMISSIONS = "<all permissions>"; //$NON-NLS-1$

    // Actions name
    private static final String ALL_ACTIONS = "<all actions>"; //$NON-NLS-1$

    /**
     * Constructs a new instance of this class. The two argument version is
     * provided for class <code>Policy</code> so that it has a consistent call
     * pattern across all Permissions. The name and action list are both
     * ignored.
     * 
     * @param name
     *            java.lang.String ignored.
     * @param actions
     *            java.lang.String ignored.
     */
    public AllPermission(String name, String actions) {
        super(ALL_PERMISSIONS);
    }

    /**
     * Constructs a new instance of this class.
     */
    public AllPermission() {
        super(ALL_PERMISSIONS);
    }

    /**
     * Compares the argument to the receiver, and returns true if they represent
     * the <em>same</em> object using a class specific comparison. All
     * AllPermissions are equal to each other.
     * 
     * @param obj
     *            the object to compare with this object
     * @return <code>true</code> if the object is the same as this object
     *         <code>false</code> if it is different from this object
     * @see #hashCode
     */
    public boolean equals(Object obj) {
        return (obj instanceof AllPermission);
    }

    /**
     * Returns an integer hash code for the receiver. Any two objects which
     * answer <code>true</code> when passed to <code>equals</code> must
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    public int hashCode() {
        return 1;
    }

    /**
     * Returns the actions associated with the receiver. Since AllPermission
     * objects allow all actions, answer with the string "<all actions>".
     * 
     * @return String the actions associated with the receiver.
     */
    public String getActions() {
        return ALL_ACTIONS;
    }

    /**
     * Indicates whether the argument permission is implied by the receiver.
     * AllPermission objects imply all other permissions.
     * 
     * @return boolean <code>true</code> if the argument permission is implied
     *         by the receiver, and <code>false</code> if it is not.
     * @param permission
     *            java.security.Permission the permission to check
     */
    public boolean implies(Permission permission) {
        return true;
    }

    /**
     * Returns a new PermissionCollection for holding permissions of this class.
     * Answer null if any permission collection can be used.
     * 
     * @return a new PermissionCollection or null
     * 
     * @see java.security.BasicPermissionCollection
     */
    public PermissionCollection newPermissionCollection() {
        return new AllPermissionCollection();
    }
}
