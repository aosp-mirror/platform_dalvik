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

package org.apache.harmony.security;

import java.security.CodeSource;
import java.security.Permission;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;

import org.apache.harmony.security.fortress.PolicyUtils;


/**
 * This class represents an elementary block of a security policy. It associates
 * a CodeSource of an executable code, Principals allowed to execute the code,
 * and a set of granted Permissions.
 * 
 * @see org.apache.harmony.security.fortress.DefaultPolicy
 */
public class PolicyEntry {

    // Store CodeSource
    private final CodeSource cs;

    // Array of principals 
    private final Principal[] principals;

    // Permissions collection
    private final Collection<Permission> permissions;

    /**
     * Constructor with initialization parameters. Passed collections are not
     * referenced directly, but copied.
     */
    public PolicyEntry(CodeSource cs, Collection<? extends Principal> prs,
            Collection<? extends Permission> permissions) {
        this.cs = cs;
        this.principals = (prs == null || prs.isEmpty()) ? null
                : (Principal[]) prs.toArray(new Principal[prs.size()]);
        this.permissions = (permissions == null || permissions.isEmpty()) ? null
                : Collections.unmodifiableCollection(permissions);
    }

    /**
     * Checks if passed CodeSource matches this PolicyEntry. Null CodeSource of
     * PolicyEntry implies any CodeSource; non-null CodeSource forwards to its
     * imply() method.
     */
    public boolean impliesCodeSource(CodeSource codeSource) {
        return (cs == null) ? true : cs.implies(codeSource);
    }

    /**
     * Checks if specified Principals match this PolicyEntry. Null or empty set
     * of Principals of PolicyEntry implies any Principals; otherwise specified
     * array must contain all Principals of this PolicyEntry.
     */
    public boolean impliesPrincipals(Principal[] prs) {
        return PolicyUtils.matchSubset(principals, prs);
    }

    /**
     * Returns unmodifiable collection of permissions defined by this
     * PolicyEntry, may be <code>null</code>.
     */
    public Collection<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Returns true if this PolicyEntry defines no Permissions, false otherwise.
     */
    public boolean isVoid() {
        return permissions == null || permissions.size() == 0;
    }
}
