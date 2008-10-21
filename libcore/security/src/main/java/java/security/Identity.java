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

import java.io.Serializable;
import java.util.Vector;
import java.util.Arrays;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * 
 * @deprecated
 */
public abstract class Identity implements Principal, Serializable {
    private static final long serialVersionUID = 3609922007826600659L;

    private String name;

    private PublicKey publicKey;

    private String info = "no additional info"; //$NON-NLS-1$

    private IdentityScope scope;

    private Vector<Certificate> certificates;

    protected Identity() {
    }

    public Identity(String name) {
        this.name = name;
    }

    public Identity(String name, IdentityScope scope)
            throws KeyManagementException {
        this(name);
        if (scope != null) {
            scope.addIdentity(this);
            this.scope = scope;
        }
    }

    public void addCertificate(Certificate certificate)
            throws KeyManagementException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("addIdentityCertificate"); //$NON-NLS-1$
        }
        PublicKey certPK = certificate.getPublicKey();
        if (publicKey != null) {
            if (!checkKeysEqual(publicKey, certPK)) {
                throw new KeyManagementException(Messages.getString("security.13")); //$NON-NLS-1$
            }
        } else {
            publicKey = certPK;
        }
        if (certificates == null) {
            certificates = new Vector<Certificate>();
        }
        certificates.add(certificate);
    }


      

    private static boolean checkKeysEqual(PublicKey pk1, PublicKey pk2) {
        // first, they should have the same format
        // second, their encoded form must be the same

        // assert(pk1 != null);
        // assert(pk2 != null);

        String format1 = pk1.getFormat();
        String format2;
        if ((pk2 == null)
                || (((format2 = pk2.getFormat()) != null) ^ (format1 != null))
                || ((format1 != null) && !format1.equals(format2))) {
            return false;
        }

        return Arrays.equals(pk1.getEncoded(), pk2.getEncoded());
    }


      

    public void removeCertificate(Certificate certificate)
            throws KeyManagementException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("removeIdentityCertificate"); //$NON-NLS-1$
        }
        if (certificates != null) {
            certificates.removeElement(certificate);
        }
    }


      

    public Certificate[] certificates() {
        if (certificates == null) {
            return new Certificate[0];
        }
        Certificate[] ret = new Certificate[certificates.size()];
        certificates.copyInto(ret);
        return ret;
    }


      

    protected boolean identityEquals(Identity identity) {
        if (!name.equals(identity.name)) {
            return false;
        }

        if (publicKey == null) {
            return (identity.publicKey == null);
        }

        return checkKeysEqual(publicKey, identity.publicKey);
    }


      

    public String toString(boolean detailed) {
        String s = toString();
        if (detailed) {
            s += " " + info; //$NON-NLS-1$
        }
        return s;
    }


      

    public final IdentityScope getScope() {
        return scope;
    }


      

    public void setPublicKey(PublicKey key) throws KeyManagementException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("setIdentityPublicKey"); //$NON-NLS-1$
        }
        // this check does not always work  
        if ((scope != null) && (key != null)) {
            Identity i = scope.getIdentity(key);
            //System.out.println("###DEBUG## Identity: "+i);
            if ((i != null) && (i != this)) {
                throw new KeyManagementException(Messages.getString("security.14")); //$NON-NLS-1$
            }
        }
        this.publicKey = key;
        certificates = null;
    }


      

    public PublicKey getPublicKey() {
        return publicKey;
    }


      

    public void setInfo(String info) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("setIdentityInfo"); //$NON-NLS-1$
        }
        this.info = info;
    }


      

    public String getInfo() {
        return info;
    }


      

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Identity)) {
            return false;
        }
        Identity i = (Identity) obj;
        if ((name == i.name || (name != null && name.equals(i.name)))
                && (scope == i.scope || (scope != null && scope.equals(i.scope)))) {
            return true;
        }
        return identityEquals(i);
    }


      

    public final String getName() {
        return name;
    }


      

    public int hashCode() {
        int hash = 0;
        if (name != null) {
            hash += name.hashCode();
        }
        if (scope != null) {
            hash += scope.hashCode();
        }
        return hash;
    }


      

    public String toString() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("printIdentity"); //$NON-NLS-1$
        }
        String s = (this.name == null? "" : this.name);
        if (scope != null) {
            s += " [" + scope.getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return s;
    }
}