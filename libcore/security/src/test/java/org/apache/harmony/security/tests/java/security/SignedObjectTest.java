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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

import org.apache.harmony.security.tests.support.TestKeyPair;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;
@TestTargetClass(SignedObject.class)
/**
 * Tests for <code>SignedObject</code> constructor and methods
 * 
 */
public class SignedObjectTest extends TestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "SignedObject",
            args = {java.io.Serializable.class, java.security.PrivateKey.class, java.security.Signature.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getAlgorithm",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getObject",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSignature",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "verify",
            args = {java.security.PublicKey.class, java.security.Signature.class}
        )
    })
    public void testSignedObject() {
        Signature sig = null;
        TestKeyPair tkp = null;
        Properties prop;
        
        try {
            sig = Signature.getInstance("SHA1withDSA");        
        } catch (NoSuchAlgorithmException e) {
            fail(e.toString());
        }
        
        try {
            tkp = new TestKeyPair("DSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        prop = new Properties();
        prop.put("aaa", "bbb");
        SignedObject so = null;
        try {
            so = new SignedObject(prop, tkp.getPrivate(), sig);
        } catch (IOException e) {
               fail(e.toString());  
        } catch (SignatureException e) {   
               fail(e.toString());  
        } catch (InvalidKeyException e) {
               fail(e.toString());  
        } catch (InvalidKeySpecException e) {
              fail(e.toString());
        }

        assertEquals("SHA1withDSA", so.getAlgorithm());
 
        try {
            assertEquals(so.getObject(), prop);          
        } catch (ClassNotFoundException e) {
               fail(e.toString());  
        } catch (IOException e) {
               fail(e.toString());  
        }
        try {
            if (!so.verify(tkp.getPublic(), sig)) {
                fail("verify() failed");
            }    
        } catch (SignatureException e) {
            fail(e.toString());          
        } catch (InvalidKeyException e) {
               fail(e.toString());             
        } catch (InvalidKeySpecException e) {
               fail(e.toString()); 
        }
        
        if (so.getSignature() == null) {
            fail("signature is null");
        }
        
        try {
            TestKeyPair tkp2 = new TestKeyPair("DH");
            so = new SignedObject(prop, tkp2.getPrivate(), sig);
        } catch(InvalidKeyException e) {
            // ok
        } catch (NoSuchAlgorithmException e) {
            fail(e.toString()); 
        } catch (SignatureException e) {
            fail(e.toString()); 
        } catch (InvalidKeySpecException e) {
            fail(e.toString()); 
        } catch (IOException e) {
            fail(e.toString()); 
        }
        
        try {
            new SignedObject(new Serializable() {
                private void writeObject(ObjectOutputStream out) throws IOException {
                    throw new IOException();
                }
            }, tkp.getPrivate(), sig);
        } catch(InvalidKeyException e) {
            fail(e.toString()); 
        } catch (SignatureException e) {
            fail(e.toString()); 
        } catch (InvalidKeySpecException e) {
            fail(e.toString()); 
        } catch (IOException e) {
            // ok 
        } 

        
        try {
            new SignedObject(prop, tkp.getPrivate(), new Signature("TST") {
            
                @Override
                protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
                    throw new SignatureException();
                }
            
                @Override
                protected void engineUpdate(byte[] b, int off, int len)
                        throws SignatureException {
                    throw new SignatureException();
                }
            
                @Override
                protected void engineUpdate(byte b) throws SignatureException {
                    throw new SignatureException();
                }
            
                @Override
                protected byte[] engineSign() throws SignatureException {
                    throw new SignatureException();
                }
            
                @Override
                protected void engineSetParameter(String param, Object value)
                        throws InvalidParameterException {
            
                }
            
                @Override
                protected void engineInitVerify(PublicKey publicKey)
                        throws InvalidKeyException {
            
                }
            
                @Override
                protected void engineInitSign(PrivateKey privateKey)
                        throws InvalidKeyException {
            
                }
            
                @Override
                protected Object engineGetParameter(String param)
                        throws InvalidParameterException {
                    return null;
                }
            });
        } catch(InvalidKeyException e) {
            fail(e.toString()); 
        } catch (SignatureException e) {
            // ok 
        } catch (InvalidKeySpecException e) {
            fail(e.toString()); 
        } catch (IOException e) {
            fail(e.toString()); 
        } 
        
    }
}
