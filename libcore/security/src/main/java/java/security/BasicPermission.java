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
 * {@code BasicPermission} is the common base class of all permissions which
 * have a name but no action lists. A {@code BasicPermission} is granted or it
 * is not.
 * <p>
 * Names of a BasicPermission follow the dot separated, hierarchical property
 * naming convention. Asterisk '*' can be used as wildcards. Either by itself,
 * matching anything, or at the end of the name, immediately preceded by a '.'.
 * For example:
 * 
 * <pre>
 * com.google.android.*  grants all permissions under the com.google.android permission hierarchy
 * *                     grants all permissions
 * </pre>
 * </p><p>
 * While this class ignores the action list in the
 * {@link #BasicPermission(String, String)} constructor, subclasses may
 * implement actions on top of this class.
 * </p>
 */
public abstract class BasicPermission extends Permission implements
    Serializable {

    private static final long serialVersionUID = 6279438298436773498L;

    /**
     * Constructs a new instance of {@code BasicPermission} with the specified
     * name.
     * 
     * @param name
     *            the name of the permission.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws IllegalArgumentException if {@code name.length() == 0}.
     * @since Android 1.0
     */
    public BasicPermission(String name) {
        super(name);
        checkName(name);
    }

    /**
     * Constructs a new instance of {@code BasicPermission} with the specified
     * name. The {@code action} parameter is ignored.
     * 
     * @param name
     *            the name of the permission.
     * @param action
     *            is ignored.
     * @throws NullPointerException
     *             if {@code name} is {@code null}.
     * @throws IllegalArgumentException
     *             if {@code name.length() == 0}.
     * @since Android 1.0
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
     * Compares the specified object with this {@code BasicPermission} for
     * equality. Returns {@code true} if the specified object has the same class
     * and the two {@code Permissions}s have the same name.
     * <p>
     * The {@link #implies(Permission)} method should be used for making access
     * control checks.
     * </p>
     * 
     * @param obj
     *            object to be compared for equality with this {@code
     *            BasicPermission}.
     * @return {@code true} if the specified object is equal to this {@code
     *         BasicPermission}, otherwise {@code false}.
     * @since Android 1.0
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
     * Returns the hash code value for this {@code BasicPermission}. Returns the
     * same hash code for {@code BasicPermission}s that are equal to each other
     * as required by the general contract of {@link Object#hashCode}.
     * 
     * @return the hash code value for this {@code BasicPermission}.
     * @see Object#equals(Object)
     * @see BasicPermission#equals(Object)
     * @since Android 1.0
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Returns the actions associated with this permission. Since {@code
     * BasicPermission} instances have no actions, an empty string is returned.
     * 
     * @return an empty string.
     * @since Android 1.0
     */
    public String getActions() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Indicates whether the specified permission is implied by this permission.
     * 
     * @param permission
     *            the permission to check against this permission.
     * @return {@code true} if the specified permission is implied by this
     *         permission, {@code false} otherwise.
     * @since Android 1.0
     */
    public boolean implies(Permission permission) {
        if (permission != null && permission.getClass() == this.getClass()) {
            return nameImplies(getName(), permission.getName());
        }
        return false;
    }

    /**
     * Checks if {@code thisName} implies {@code thatName},
     * accordingly to hierarchical property naming convention.
     * It is assumed that names cannot be {@code null} or empty.
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
     * Returns an empty {@link PermissionCollection} for holding permissions.
     * <p>
     * For {@code PermissionCollection} (and subclasses which do not override
     * this method), the collection which is returned does <em>not</em> invoke
     * the {@link #implies(Permission)} method of the permissions which are
     * stored in it when checking if the collection implies a permission.
     * Instead, it assumes that if the type of the permission is correct, and
     * the name of the permission is correct, there is a match.
     * </p>
     * 
     * @return an empty {@link PermissionCollection} for holding permissions.
     * @see BasicPermissionCollection
     * @since Android 1.0
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