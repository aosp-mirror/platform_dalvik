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
* @author Aleksei Y. Semenov
* @version $Revision$
*/

package java.security;

import java.util.Enumeration;

import org.apache.harmony.security.SystemScope;


/**
 * @com.intel.drl.spec_ref 
 * @deprecated
 */
public abstract class IdentityScope extends Identity {

    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = -2337346281189773310L;

    // systemScope holds reference to the current system scope
    private static IdentityScope systemScope;

    /**
     * @com.intel.drl.spec_ref 
     */
    protected IdentityScope() {
        super();
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public IdentityScope(String name) {
        super(name);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public IdentityScope(String name, IdentityScope scope)
            throws KeyManagementException {
        super(name, scope);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public static IdentityScope getSystemScope() {
        /* 
         * Test shows that the implementation class name is read from security property
         * "system.scope", and the class is only loaded from boot classpath. No default
         * implementation as fallback, i.e., return null if fails to init an instance. 
         */
        if (systemScope == null) {
            String className = AccessController.doPrivileged(new PrivilegedAction<String>(){
                public String run() {
                    return Security.getProperty("system.scope"); //$NON-NLS-1$
                }
            });
            if(className != null){
                try {
                    systemScope = (IdentityScope) Class.forName(className).newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return systemScope;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    protected static void setSystemScope(IdentityScope scope) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("setSystemScope"); //$NON-NLS-1$
        }
        systemScope = scope;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public abstract int size();

    /**
     * @com.intel.drl.spec_ref 
     */
    public abstract Identity getIdentity(String name);

    /**
     * @com.intel.drl.spec_ref 
     */
    public Identity getIdentity(Principal principal) {
        return getIdentity(principal.getName());
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public abstract Identity getIdentity(PublicKey key);

    /**
     * @com.intel.drl.spec_ref 
     */
    public abstract void addIdentity(Identity identity)
            throws KeyManagementException;

    /**
     * @com.intel.drl.spec_ref 
     */
    public abstract void removeIdentity(Identity identity)
            throws KeyManagementException;

    /**
     * @com.intel.drl.spec_ref 
     */
    public abstract Enumeration<Identity> identities();

    /**
     * @com.intel.drl.spec_ref 
     */
    public String toString() {
        return new StringBuffer(super.toString())
                .append("[").append(size()).append("]").toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }
}