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

import java.io.Serializable;

/**
 * Abstract superclass of all classes which represent permission to access
 * system resources.
 * 
 */
public abstract class Permission implements Guard, Serializable {

    /** 
     * @com.intel.drl.spec_ref 
     */
    private static final long serialVersionUID = -5636570222231596674L;

    private final String name;

    /** 
     * @com.intel.drl.spec_ref 
     */
    public abstract boolean equals(Object obj);

    /**
     * Returns an integer hash code for the receiver. Any two objects which
     * answer <code>true</code> when passed to <code>.equals</code> must
     * answer the same value for this method.
     * 
     * 
     * @return int the receiver's hash.
     * 
     * @see #equals
     */
    public abstract int hashCode();

    /**
     * Returns the actions associated with the receiver. Subclasses should
     * return their actions in canonical form. If no actions are associated with
     * the receiver, the empty string should be returned.
     * 
     * 
     * @return String the receiver's actions.
     */
    public abstract String getActions();

    /**
     * Indicates whether the argument permission is implied by the receiver.
     * 
     * 
     * @return boolean <code>true</code> if the argument permission is implied
     *         by the receiver, and <code>false</code> if it is not.
     * @param permission
     *            Permission the permission to check.
     */
    public abstract boolean implies(Permission permission);

    /**
     * Constructs a new instance of this class with its name set to the
     * argument.
     * 
     * 
     * @param name
     *            String the name of the permission.
     */
    public Permission(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the receiver.
     * 
     * 
     * @return String the receiver's name.
     */
    public final String getName() {
        return name;
    }

    /** 
     * @com.intel.drl.spec_ref 
     */
    public void checkGuard(Object obj) throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(this);
        }
    }

    /**
     * Returns a new PermissionCollection for holding permissions of this class.
     * Answer null if any permission collection can be used.
     * 
     * 
     * @return PermissionCollection or null a suitable permission collection for
     *         instances of the class of the receiver.
     */
    public PermissionCollection newPermissionCollection() {
        return null;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * 
     * @return String a printable representation for the receiver.
     */
    public String toString() {
        String actions = getActions();
        actions = (actions == null || actions.length() == 0) ? "" : " " //$NON-NLS-1$ //$NON-NLS-2$
                + getActions();
        return "(" + getClass().getName() + " " + getName() + actions + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
