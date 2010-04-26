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

import java.util.Enumeration;

import org.apache.harmony.security.fortress.DefaultPolicy;
import org.apache.harmony.security.fortress.PolicyUtils;


/**
 * {@code Policy} is the common super type of classes which represent a system
 * security policy. The {@code Policy} specifies which permissions apply to
 * which code sources.
 * <p>
 * The system policy can be changed by setting the {@code 'policy.provider'}
 * property in the file named {@code JAVA_HOME/lib/security/java.security} to
 * the fully qualified class name of the desired {@code Policy}.
 * <p>
 * Only one instance of a {@code Policy} is active at any time.
 */
public abstract class Policy {
    
    // Key to security properties, defining default policy provider.
    private static final String POLICY_PROVIDER = "policy.provider"; //$NON-NLS-1$

    // The SecurityPermission required to set custom Policy.
    private static final SecurityPermission SET_POLICY = new SecurityPermission(
            "setPolicy"); //$NON-NLS-1$

    // The SecurityPermission required to get current Policy.
    private static final SecurityPermission GET_POLICY = new SecurityPermission(
            "getPolicy"); //$NON-NLS-1$

    // The policy currently in effect. 
    private static Policy activePolicy;

    /**
     * Returns a {@code PermissionCollection} describing what permissions are
     * allowed for the specified {@code CodeSource} based on the current
     * security policy.
     * <p>
     * Note that this method is not called for classes which are in the system
     * domain (i.e. system classes). System classes are always given
     * full permissions (i.e. AllPermission). This can not be changed by
     * installing a new policy.
     *
     * @param cs
     *            the {@code CodeSource} to compute the permissions for.
     * @return the permissions that are granted to the specified {@code
     *         CodeSource}.
     */
    public abstract PermissionCollection getPermissions(CodeSource cs);

    /**
     * Reloads the policy configuration for this {@code Policy} instance.
     */
    public abstract void refresh();

    /**
     * Returns a {@code PermissionCollection} describing what permissions are
     * allowed for the specified {@code ProtectionDomain} (more specifically,
     * its {@code CodeSource}) based on the current security policy.
     * <p>
     * Note that this method is not< called for classes which are in the
     * system domain (i.e. system classes). System classes are always
     * given full permissions (i.e. AllPermission). This can not be changed by
     * installing a new policy.
     *
     * @param domain
     *            the {@code ProtectionDomain} to compute the permissions for.
     * @return the permissions that are granted to the specified {@code
     *         CodeSource}.
     */
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        if (domain != null) {
            return getPermissions(domain.getCodeSource());
        }
        return new Permissions();
    }

    /**
     * Indicates whether the specified {@code Permission} is implied by the
     * {@code PermissionCollection} of the specified {@code ProtectionDomain}.
     *
     * @param domain
     *            the {@code ProtectionDomain} for which the permission should
     *            be granted.
     * @param permission
     *            the {@code Permission} for which authorization is to be
     *            verified.
     * @return {@code true} if the {@code Permission} is implied by the {@code
     *         ProtectionDomain}, {@code false} otherwise.
     */
    public boolean implies(ProtectionDomain domain, Permission permission) {
        if (domain != null) {
            PermissionCollection total = getPermissions(domain);
            PermissionCollection inherent = domain.getPermissions();
            if (total == null) {
                total = inherent;
            } else if (inherent != null) {
                for (Enumeration<Permission> en = inherent.elements(); en.hasMoreElements();) {
                    total.add(en.nextElement());
                }
            }
            if (total != null && total.implies(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the current system security policy. If no policy has been
     * instantiated then this is done using the security property {@code
     * "policy.provider"}.
     * <p>
     * If a {@code SecurityManager} is installed, code calling this method needs
     * the {@code SecurityPermission} {@code getPolicy} to be granted, otherwise
     * a {@code SecurityException} will be thrown.
     *
     * @return the current system security policy.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and the caller does
     *             not have permission to invoke this method.
     */
    public static Policy getPolicy() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(GET_POLICY);
        }
        return getAccessiblePolicy();
    }

     // Reads name of default policy provider from security.properties,
     // loads the class and instantiates the provider.<br> 
     // In case of any error, including undefined provider name, 
     // returns new instance of org.apache.harmony.security.FilePolicy provider. 
    private static Policy getDefaultProvider() {
        final String defaultClass = AccessController
                .doPrivileged(new PolicyUtils.SecurityPropertyAccessor(
                        POLICY_PROVIDER));
        if (defaultClass == null) {
            //TODO log warning
            //System.err.println("No policy provider specified. Loading the " 
            //           + DefaultPolicy.class.getName());
            return new DefaultPolicy();
        }

        // TODO accurate classloading
        return AccessController.doPrivileged(new PrivilegedAction<Policy>() {

            public Policy run() {
                try {
                    return (Policy) Class.forName(defaultClass, true,
                            ClassLoader.getSystemClassLoader()).newInstance();
                }
                catch (Exception e) {
                    //TODO log error 
                    //System.err.println("Error loading policy provider <" 
                    //                 + defaultClass + "> : " + e 
                    //                 + "\nSwitching to the default " 
                    //                 + DefaultPolicy.class.getName());
                    return new DefaultPolicy();
                }
            }
        });

    }
    
    /**
     * Returns {@code true} if system policy provider is instantiated.
     */
    static boolean isSet() {
        return activePolicy != null;
    }

    /**
     * Shortcut accessor for friendly classes, to skip security checks.
     * If active policy was set to <code>null</code>, loads default provider, 
     * so this method never returns <code>null</code>. <br>
     * This method is synchronized with setPolicy()
     */
    static Policy getAccessiblePolicy() {
        Policy current = activePolicy;
        if (current == null) {
            synchronized (Policy.class) {
                // double check in case value has been reassigned 
                // while we've been awaiting monitor
                if (activePolicy == null) {
                    activePolicy = getDefaultProvider();
                }
                return activePolicy;
            }
        }
        return current;
    }

    /**
     * Sets the system wide policy.
     * <p>
     * If a {@code SecurityManager} is installed, code calling this method needs
     * the {@code SecurityPermission} {@code setPolicy} to be granted, otherwise
     * a {@code SecurityException} will be thrown.
     *
     * @param policy
     *            the {@code Policy} to set.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and the caller does
     *             not have permission to invoke this method.
     */
    public static void setPolicy(Policy policy) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SET_POLICY);
        }
        synchronized (Policy.class) {
            activePolicy = policy;
        }
    }
}
