/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.security;

import org.apache.harmony.security.fortress.PolicyUtils;

import java.util.ArrayList;

/**
 * {@code AccessControlContext} encapsulates the {@code ProtectionDomain}s on
 * which access control decisions are based.
 */
public final class AccessControlContext {

    // List of ProtectionDomains wrapped by the AccessControlContext
    // It has the following characteristics:
    //     - 'context' can not be null
    //     - never contains null(s)
    //     - all elements are unique (no dups)
    ProtectionDomain[] context;

    DomainCombiner combiner;

    // An AccessControlContext inherited by the current thread from its parent
    private AccessControlContext inherited;

    /**
     * Constructs a new instance of {@code AccessControlContext} with the
     * specified {@code AccessControlContext} and {@code DomainCombiner}.
     * <p>
     * If a {@code SecurityManager} is installed, code calling this constructor
     * need the {@code SecurityPermission} {@code createAccessControlContext} to
     * be granted, otherwise a {@code SecurityException} will be thrown.
     * 
     * @param acc
     *            the {@code AccessControlContext} related to the given {@code
     *            DomainCombiner}
     * @param combiner
     *            the {@code DomainCombiner} related to the given {@code
     *            AccessControlContext}
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and the caller does
     *             not have permission to invoke this constructor
     * @throws NullPointerException
     *             if {@code acc} is {@code null}
     * @since Android 1.0
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
     * Constructs a new instance of {@code AccessControlContext} with the
     * specified array of {@code ProtectionDomain}s.
     * 
     * @param context
     *            the {@code ProtectionDomain}s that are used to perform access
     *            checks in the context of this {@code AccessControlContext}
     * @throws NullPointerException
     *             if {@code context} is {@code null}
     * @since Android 1.0
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
     * @param combiner - combiner
     */
    AccessControlContext(ProtectionDomain[] stack,
            DomainCombiner combiner) {
        this(stack); // removes dups, removes nulls, checks for stack==null
        this.combiner = combiner;
    }

    /**
     * Checks the specified permission against the vm's current security policy.
     * The check is based on this {@code AccessControlContext} as opposed to the
     * {@link AccessController#checkPermission(Permission)} method which
     * performs access checks based on the context of the current thread. This
     * method returns silently if the permission is granted, otherwise an
     * {@code AccessControlException} is thrown.
     * <p>
     * A permission is considered granted if every {@link ProtectionDomain} in
     * this context has been granted the specified permission.
     * <p>
     * If privileged operations are on the call stack, only the {@code
     * ProtectionDomain}s from the last privileged operation are taken into
     * account.
     * <p>
     * If inherited methods are on the call stack, the protection domains of the
     * declaring classes are checked, not the protection domains of the classes
     * on which the method is invoked.
     * 
     * @param perm
     *            the permission to check against the policy
     * @throws AccessControlException
     *             if the specified permission is not granted
     * @throws NullPointerException
     *             if the specified permission is {@code null}
     * @see AccessController#checkPermission(Permission)
     * @since Android 1.0
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
     * Compares the specified object with this {@code AccessControlContext} for
     * equality. Returns {@code true} if the specified object is also an
     * instance of {@code AccessControlContext}, and the two contexts
     * encapsulate the same {@code ProtectionDomain}s. The order of the {@code
     * ProtectionDomain}s is ignored by this method.
     * 
     * @param obj
     *            object to be compared for equality with this {@code
     *            AccessControlContext}
     * @return {@code true} if the specified object is equal to this {@code
     *         AccessControlContext}, otherwise {@code false}
     * @since Android 1.0
     */
    @Override
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
            // BEGIN android-changed
            if(combiner != null) {
                return combiner.equals(that.combiner);
            }
            return that.combiner == null;
            // END android-changed
        }
        return false;
    }

    /**
     * Returns the {@code DomainCombiner} associated with this {@code
     * AccessControlContext}.
     * <p>
     * If a {@code SecurityManager} is installed, code calling this method needs
     * the {@code SecurityPermission} {@code getDomainCombiner} to be granted,
     * otherwise a {@code SecurityException} will be thrown.
     * 
     * @return the {@code DomainCombiner} associated with this {@code
     *         AccessControlContext}
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and the caller does
     *             not have permission to invoke this method
     * @since Android 1.0
     */
    public DomainCombiner getDomainCombiner() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("getDomainCombiner"));
        }
        return combiner;
    }


    /**
     * Returns the hash code value for this {@code AccessControlContext}.
     * Returns the same hash code for {@code AccessControlContext}s that are
     * equal to each other as required by the general contract of
     * {@link Object#hashCode}.
     * 
     * @return the hash code value for this {@code AccessControlContext}
     * @see Object#equals(Object)
     * @see AccessControlContext#equals(Object)
     * @since Android 1.0
     */
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < context.length; i++) {
            hash ^= context[i].hashCode();
        }
        return hash;
    }

}
