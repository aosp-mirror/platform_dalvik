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

package org.apache.harmony.security.tests.java.security;

import java.security.Identity;
import java.security.IdentityScope;
import java.security.KeyManagementException;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.harmony.security.tests.java.security.Identity2Test.IdentitySubclass;
@SuppressWarnings("deprecation")
public class IdentityScope2Test extends junit.framework.TestCase {

    static PublicKey pubKey;
    static {
        try {
            pubKey = KeyPairGenerator.getInstance("DSA").genKeyPair().getPublic();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public static class IdentityScopeSubclass extends IdentityScope {
        Hashtable<Identity, Identity> identities;

        public IdentityScopeSubclass(String name, PublicKey pk) {
            super(name);
            try {
                setPublicKey(pk);
            } catch (KeyManagementException e) {
            }
            identities = new Hashtable<Identity, Identity>();
        }

        public IdentityScopeSubclass() {
            super();
            identities = new Hashtable<Identity, Identity>();
        }

        public IdentityScopeSubclass(String name) {
            super(name);
            identities = new Hashtable<Identity, Identity>();
        }

        public IdentityScopeSubclass(String name, IdentityScope scope)
                throws KeyManagementException {
            super(name, scope);
            identities = new Hashtable<Identity, Identity>();
        }

        public int size() {
            return identities.size();
        }

        public Identity getIdentity(String name) {
            Enumeration en = identities();
            while (en.hasMoreElements()) {
                Identity current = (Identity) en.nextElement();
                if (current.getName().equals(name))
                    return current;
            }
            return null;
        }

        public Identity getIdentity(PublicKey pk) {
            Enumeration en = identities();
            while (en.hasMoreElements()) {
                Identity current = (Identity) en.nextElement();
                if (current.getPublicKey() == pk)
                    return current;
            }
            return null;
        }

        public Enumeration<Identity> identities() {
            return identities.elements();
        }

        public void addIdentity(Identity id) throws KeyManagementException {
            if (identities.containsKey(id))
                throw new KeyManagementException(
                        "This Identity is already contained in the scope");
            if (getIdentity(id.getPublicKey()) != null)
                throw new KeyManagementException(
                        "This Identity's public key already exists in the scope");
            identities.put(id, id);
        }

        public void removeIdentity(Identity id) throws KeyManagementException {
            if (!identities.containsKey(id))
                throw new KeyManagementException(
                        "This Identity is not contained in the scope");
            identities.remove(id);
        }
    }

    /**
     * @tests java.security.IdentityScope#IdentityScope()
     */
    public void test_Constructor() {
        new IdentityScopeSubclass();
    }

    /**
     * @tests java.security.IdentityScope#IdentityScope(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        new IdentityScopeSubclass("test");
    }

    /**
     * @tests java.security.IdentityScope#IdentityScope(java.lang.String,
     *        java.security.IdentityScope)
     */
    public void test_ConstructorLjava_lang_StringLjava_security_IdentityScope() throws Exception {
        new IdentityScopeSubclass("test", new IdentityScopeSubclass());
    }

    /**
     * @tests java.security.IdentityScope#addIdentity(java.security.Identity)
     */
    public void test_addIdentityLjava_security_Identity() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass("id1");
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               try {
                   Identity id2 = new IdentitySubclass("id2");
                   id2.setPublicKey(pubKey);
                   sub.addIdentity(id2);
                   fail("KeyManagementException should have been thrown");
               } catch (KeyManagementException e) {
                   // Expected
               }
    }

    /**
     * @tests java.security.IdentityScope#removeIdentity(java.security.Identity)
     */
    public void test_removeIdentityLjava_security_Identity() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass();
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               sub.removeIdentity(id);
               try {
                   sub.removeIdentity(id);
                   fail("KeyManagementException should have been thrown");
               } catch (KeyManagementException e) {
                   // expected
               }
    }

    /**
     * @tests java.security.IdentityScope#identities()
     */
    public void test_identities() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass();
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               Enumeration en = sub.identities();
               assertTrue("Wrong object contained in identities", en.nextElement()
                       .equals(id));
               assertTrue("Contains too many elements", !en.hasMoreElements());
    }

    /**
     * @tests java.security.IdentityScope#getIdentity(java.security.Principal)
     */
    public void test_getIdentityLjava_security_Principal() throws Exception {
               Identity id = new IdentitySubclass("principal name");
               id.setPublicKey(pubKey);
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               sub.addIdentity(id);
               Identity returnedId = sub.getIdentity(id);
               assertEquals("Returned Identity not the same as the added one", id,
                       returnedId);
    }

    /**
     * @tests java.security.IdentityScope#getIdentity(java.security.PublicKey)
     */
    public void test_getIdentityLjava_security_PublicKey() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass();
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               Identity returnedId = sub.getIdentity(pubKey);
               assertEquals("Returned Identity not the same as the added one", id,
                       returnedId);
    }

    /**
     * @tests java.security.IdentityScope#getIdentity(java.lang.String)
     */
    public void test_getIdentityLjava_lang_String() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass("test");
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               Identity returnedId = sub.getIdentity("test");
               assertEquals("Returned Identity not the same as the added one", id,
                       returnedId);
    }

    /**
     * @tests java.security.IdentityScope#size()
     */
    public void test_size() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass();
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               assertEquals("Wrong size", 1, sub.size());
    }

    /**
     * @tests java.security.IdentityScope#toString()
     */
    public void test_toString() throws Exception {
            IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                    new IdentityScopeSubclass());
            Identity id = new IdentitySubclass();
            id.setPublicKey(pubKey);
            sub.addIdentity(id);
            assertNotNull("toString returned a null", sub.toString());
            assertTrue("Not a valid String ", sub.toString().length() > 0);
    }

    public void test_getIdentity() throws Exception {
        //Regression for HARMONY-1173
        IdentityScope scope = IdentityScope.getSystemScope(); 
        try {
            scope.getIdentity((String) null);
            fail("NPE expected");
        } catch (NullPointerException npe) {}
    }
}
