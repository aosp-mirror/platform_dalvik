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

/**
 * @com.intel.drl.spec_ref
 * 
 * @deprecated Replaced by behavior in
 *             {@link java.security.cert java.security.cert} package and
 *             {@link java.security.Principal Principal}
 */

public abstract class Signer extends Identity {

    /**
     * @com.intel.drl.spec_ref 
     */
    private static final long serialVersionUID = -1763464102261361480L;

    /**
     * @com.intel.drl.spec_ref 
     */
    private PrivateKey privateKey;

    /**
     * @com.intel.drl.spec_ref 
     */
    protected Signer() {
        super();
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public Signer(String name) {
        super(name);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public Signer(String name, IdentityScope scope)
            throws KeyManagementException {
        super(name, scope);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public PrivateKey getPrivateKey() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("getSignerPrivateKey"); //$NON-NLS-1$
        }

        return privateKey;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public final void setKeyPair(KeyPair pair)
            throws InvalidParameterException, KeyException {
        
        if (pair == null) {
            throw new NullPointerException();
        }

        if ((pair.getPrivate() == null) || (pair.getPublic() == null)) {
            throw new InvalidParameterException();
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("setSignerKeyPair"); //$NON-NLS-1$
        }
        final PublicKey pk = pair.getPublic();
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws KeyManagementException {
                    setPublicKey(pk);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new KeyException(e.getException());
        }
        this.privateKey = pair.getPrivate();
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public String toString() {
        String s = "[Signer]" + getName(); //$NON-NLS-1$
        if (getScope() != null) {
            s = s + '[' + getScope().toString() + ']';
        }
        return s;
    }
}
