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
* @version $Revision: 1.1.2.2.4.3 $
*/

package java.security;

import java.util.ArrayList;
import org.apache.harmony.security.fortress.PolicyUtils;

/**
 * @com.intel.drl.spec_ref 
 */
public final class AccessControlContext {

    // List of ProtectionDomains wrapped by the AccessControlContext
    // It has the following characteristics:
    //     - 'context' can not be null
    //     - never contains null(s)
    //     - all elements are uniq (no dups)
    ProtectionDomain[] context;

    DomainCombiner combiner;

    // An AccessControlContext inherited by the current thread from its parent
    private AccessControlContext inherited;

    /**
     * @com.intel.drl.spec_ref 
     */
    public AccessControlContext(AccessControlContext acc,
            DomainCombiner combiner) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission(
                    "createAccessControlContext"));
        }
        // no need to clone() here as ACC is immutable
        this.context = acc.context;
        this.combiner = combiner;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public AccessControlContext(ProtectionDomain[] context) {
        if (context == null) {
            throw new NullPointerException("context can not be null");
        }
        if (context.length != 0) {
            // remove dup entries
            ArrayList<ProtectionDomain> a = new ArrayList<ProtectionDomain>();
            for (int i = 0; i < context.length; i++) {
                if (context[i] != null && !a.contains(context[i])) {
                    a.add(context[i]);
                }
            }
            if (a.size() != 0) {
                this.context = new ProtectionDomain[a.size()];
                a.toArray(this.context);
            }
        }
        if (this.context == null) {
            // Prevent numerous checks for 'context==null' 
            this.context = new ProtectionDomain[0];
        }
    }

    /**
     * Package-level ctor which is used in AccessController.<br>
     * ProtectionDomains passed as <code>stack</code> is then passed into 
     * {@link #AccessControlContext(ProtectionDomain[])}, therefore:<br>
     * <il>
     * <li>it must not be null
     * <li>duplicates will be removed
     * <li>null-s will be removed
     * </li>
     *   
     * @param stack - array of ProtectionDomains
     * @param inherited - inherited context, which may be null
     */
    AccessControlContext(ProtectionDomain[] stack,
            AccessControlContext inherited) {
        this(stack); // removes dups, removes nulls, checks for stack==null
        this.inherited = inherited;
    }

    /**
     * Package-level ctor which is used in AccessController.<br>
     * ProtectionDomains passed as <code>stack</code> is then passed into 
     * {@link #AccessControlContext(ProtectionDomain[])}, therefore:<br>
     * <il>
     * <li>it must not be null
     * <li>duplicates will be removed
     * <li>null-s will be removed
     * </li>
     *   
     * @param stack - array of ProtectionDomains
     * @param inherited - inherited context, which may be null
     */
    AccessControlContext(ProtectionDomain[] stack,
            DomainCombiner combiner) {
        this(stack); // removes dups, removes nulls, checks for stack==null
        this.combiner = combiner;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public void checkPermission(Permission perm) throws AccessControlException {
        if (perm == null) {
            throw new NullPointerException("Permission cannot be null");
        }
        for (int i = 0; i < context.length; i++) {
            if (!context[i].implies(perm)) {
                throw new AccessControlException("Permission check failed "
                        + perm, perm);
            }
        }
        if (inherited != null) {
            inherited.checkPermission(perm);
        }
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AccessControlContext) {
            AccessControlContext that = (AccessControlContext) obj;
            if (!(PolicyUtils.matchSubset(context, that.context) && PolicyUtils
                    .matchSubset(that.context, context))) {
                return false;
            }
            // 'combiner' is not taken into account - see the test 
            // AccessControllerTest.testEqualsObject_01
            return true;
        }
        return false;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public DomainCombiner getDomainCombiner() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("getDomainCombiner"));
        }
        return combiner;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < context.length; i++) {
            hash ^= context[i].hashCode();
        }
        return hash;
    }

}
