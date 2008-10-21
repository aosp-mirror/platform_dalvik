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
* @author Alexey V. Varlamov
* @version $Revision$
*/

package java.security;

import java.io.IOException;
import java.io.Serializable;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * Superclass of permissions which have names but no action lists.
 * 
 */

public abstract class BasicPermission extends Permission implements
    Serializable {

    private static final long serialVersionUID = 6279438298436773498L;

    /**
     * Creates an instance of this class with the given name and action list.
     * 
     * @param name
     *            String the name of the new permission.
     */
    public BasicPermission(String name) {
        super(name);
        checkName(name);
    }

    /**
     * Creates an instance of this class with the given name and action list.
     * The action list is ignored.
     * 
     * @param name
     *            String the name of the new permission.
     * @param action
     *            String ignored.
     */
    public BasicPermission(String name, String action) {
        super(name);
        checkName(name);
    }

    /**
     * Checks name parameter
     */ 
    private final void checkName(String name) {
        if (name == null) {
            throw new NullPointerException(Messages.getString("security.28")); //$NON-NLS-1$
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException(Messages.getString("security.29")); //$NON-NLS-1$
        }
    }

    /**
     * Compares the argument to the receiver, and returns true if they represent
     * the <em>same</em> object using a class specific comparison. In this
     * case, the receiver and the object must have the same class and name.
     * 
     * @param obj
     *            the object to compare with this object
     * @return <code>true</code> if the object is the same as this object
     *         <code>false</code> if it is different from this object
     * @see #hashCode
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj != null && obj.getClass() == this.getClass()) {
            return this.getName().equals(((Permission)obj).getName());
        }
        return false;
    }

    /**
     * Returns an integer hash code for the receiver. Any two objects which
     * answer <code>true</code> when passed to <code>equals</code> must
     * answer the same value for this method.
     * 
     * @return int the receiver's hash
     * 
     * @see #equals
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Returns the actions associated with the receiver. BasicPermission objects
     * have no actions, so answer the empty string.
     * 
     * @return String the actions associated with the receiver.
     */
    public String getActions() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Indicates whether the argument permission is implied by the receiver.
     * 
     * @return boolean <code>true</code> if the argument permission is implied
     *         by the receiver, and <code>false</code> if it is not.
     * @param permission
     *            java.security.Permission the permission to check
     */
    public boolean implies(Permission permission) {
        if (permission != null && permission.getClass() == this.getClass()) {
            return nameImplies(getName(), permission.getName());
        }
        return false;
    }

    /**
     * Checks if <code>thisName</code> implies <code>thatName</code>,
     * accordingly to hierarchical property naming convention.
     * It is assumed that names cannot be null or empty.
     */
    static boolean nameImplies(String thisName, String thatName) {
        if (thisName == thatName) {
            return true;
        }
        int end = thisName.length();
        if (end > thatName.length()) {
            return false;
        }
        if (thisName.charAt(--end) == '*'
            && (end == 0 || thisName.charAt(end - 1) == '.')) {
            //wildcard found
            end--;
        } else if (end != (thatName.length()-1)) {
            //names are not equal
            return false;
        }
        for (int i = end; i >= 0; i--) {
            if (thisName.charAt(i) != thatName.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a new PermissionCollection for holding permissions of this class.
     * Answer null if any permission collection can be used.
     * <p>
     * Note: For BasicPermission (and subclasses which do not override this
     * method), the collection which is returned does <em>not</em> invoke the
     * .implies method of the permissions which are stored in it when checking
     * if the collection implies a permission. Instead, it assumes that if the
     * type of the permission is correct, and the name of the permission is
     * correct, there is a match.
     * 
     * @return a new PermissionCollection or null
     * 
     * @see java.security.BasicPermissionCollection
     */
    public PermissionCollection newPermissionCollection() {
        return new BasicPermissionCollection();
    }

    /**
     * Checks name after default deserialization.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        in.defaultReadObject();
        checkName(this.getName());
    }
}