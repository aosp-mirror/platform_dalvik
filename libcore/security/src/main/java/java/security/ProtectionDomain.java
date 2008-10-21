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
 * This class represents a domain in which classes from the same source (URL)
 * and signed by the same keys are stored. All the classes inside are given the
 * same permissions.
 * <p>
 * Note: a class can only belong to one and only one protection domain.
 */
public class ProtectionDomain {

    // CodeSource for this ProtectionDomain
    private CodeSource codeSource;

    // Static permissions for this ProtectionDomain
    private PermissionCollection permissions;

    // ClassLoader
    private ClassLoader classLoader;

    // Set of principals associated with this ProtectionDomain
    private Principal[] principals;

    // false if this ProtectionDomain was constructed with static 
    // permissions, true otherwise. 
    private boolean dynamicPerms;

    /**
     * Constructs a protection domain from the given code source and the
     * permissions that that should be granted to the classes which are
     * encapsulated in it.
     * @param cs 
     * @param permissions 
     */
    public ProtectionDomain(CodeSource cs, PermissionCollection permissions) {
        this.codeSource = cs;
        if (permissions != null) {
            permissions.setReadOnly();
        }
        this.permissions = permissions;
        //this.classLoader = null;
        //this.principals = null;
        //dynamicPerms = false;
    }

    /**
     * Constructs a protection domain from the given code source and the
     * permissions that that should be granted to the classes which are
     * encapsulated in it. 
     * 
     * This constructor also allows the association of a ClassLoader and group
     * of Principals.
     * 
     * @param cs
     *            the CodeSource associated with this domain
     * @param permissions
     *            the Permissions associated with this domain
     * @param cl
     *            the ClassLoader associated with this domain
     * @param principals
     *            the Principals associated with this domain
     */
    public ProtectionDomain(CodeSource cs, PermissionCollection permissions,
            ClassLoader cl, Principal[] principals) {
        this.codeSource = cs;
        if (permissions != null) {
            permissions.setReadOnly();
        }
        this.permissions = permissions;
        this.classLoader = cl;
        if (principals != null) {
            this.principals = new Principal[principals.length];
            System.arraycopy(principals, 0, this.principals, 0,
                    this.principals.length);
        }
        dynamicPerms = true;
    }

    /**
     * Returns the ClassLoader associated with the ProtectionDomain
     * 
     * @return ClassLoader associated ClassLoader
     */
    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the code source of this domain.
     * 
     * @return java.security.CodeSource the code source of this domain
     */
    public final CodeSource getCodeSource() {
        return codeSource;
    }

    /**
     * Returns the permissions that should be granted to the classes which are
     * encapsulated in this domain.
     * 
     * @return java.security.PermissionCollection collection of permissions
     *         associated with this domain.
     */
    public final PermissionCollection getPermissions() {
        return permissions;
    }

    /**
     * Returns the Principals associated with this ProtectionDomain. A change to
     * the returned array will not impact the ProtectionDomain.
     * 
     * @return Principals[] Principals associated with the ProtectionDomain.
     */
    public final Principal[] getPrincipals() {
        if( principals == null ) {
            return new Principal[0];
        }
        Principal[] tmp = new Principal[principals.length];
        System.arraycopy(principals, 0, tmp, 0, tmp.length);
        return tmp;
    }

    /**
     * Determines whether the permission collection of this domain implies the
     * argument permission.
     * 
     * 
     * @return boolean true if this permission collection implies the argument
     *         and false otherwise.
     * @param permission
     *            java.security.Permission the permission to check.
     */
    public boolean implies(Permission permission) {
        // First, test with the Policy, as the default Policy.implies() 
        // checks for both dynamic and static collections of the 
        // ProtectionDomain passed...
        if (dynamicPerms
                && Policy.getAccessiblePolicy().implies(this, permission)) {
            return true;
        }

        // ... and we get here if 
        // either the permissions are static
        // or Policy.implies() did not check for static permissions
        // or the permission is not implied
        return permissions == null ? false : permissions.implies(permission);
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return String a printable representation for the receiver.
     */
    public String toString() {
        //FIXME: 1.5 use StreamBuilder here
        StringBuffer buf = new StringBuffer(200);
        buf.append("ProtectionDomain\n"); //$NON-NLS-1$
        buf.append("CodeSource=").append( //$NON-NLS-1$
                codeSource == null ? "<null>" : codeSource.toString()).append( //$NON-NLS-1$
                "\n"); //$NON-NLS-1$
        buf.append("ClassLoader=").append( //$NON-NLS-1$
                classLoader == null ? "<null>" : classLoader.toString()) //$NON-NLS-1$
                .append("\n"); //$NON-NLS-1$
        if (principals == null || principals.length == 0) {
            buf.append("<no principals>\n"); //$NON-NLS-1$
        } else {
            buf.append("Principals: <\n"); //$NON-NLS-1$
            for (int i = 0; i < principals.length; i++) {
                buf.append("\t").append( //$NON-NLS-1$
                        principals[i] == null ? "<null>" : principals[i] //$NON-NLS-1$
                                .toString()).append("\n"); //$NON-NLS-1$
            }
            buf.append(">"); //$NON-NLS-1$
        }

        //permissions here
        buf.append("Permissions:\n"); //$NON-NLS-1$
        if (permissions == null) {
            buf.append("\t\t<no static permissions>\n"); //$NON-NLS-1$
        } else {
            buf.append("\t\tstatic: ").append(permissions.toString()).append( //$NON-NLS-1$
                    "\n"); //$NON-NLS-1$
        }

        if (dynamicPerms) {
            if (Policy.isSet()) {
                PermissionCollection perms;
                perms = Policy.getAccessiblePolicy().getPermissions(this);
                if (perms == null) {
                    buf.append("\t\t<no dynamic permissions>\n"); //$NON-NLS-1$
                } else {
                    buf.append("\t\tdynamic: ").append(perms.toString()) //$NON-NLS-1$
                            .append("\n"); //$NON-NLS-1$
                }
            } else {
                buf.append("\t\t<no dynamic permissions>\n"); //$NON-NLS-1$
            }
        }
        return buf.toString();
    }
}
