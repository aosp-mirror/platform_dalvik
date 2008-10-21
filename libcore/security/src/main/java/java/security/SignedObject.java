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

package java.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public final class SignedObject implements Serializable {

    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = 720502720485447167L;

    /**
     * @com.intel.drl.spec_ref
     */
    private byte[] content;

    /**
     * @com.intel.drl.spec_ref
     */
    private byte[] signature;

    /**
     * @com.intel.drl.spec_ref
     */
    private String thealgorithm;

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {

        s.defaultReadObject();
        byte[] tmp = new byte[content.length];
        System.arraycopy(content, 0, tmp, 0, content.length);
        content = tmp;
        tmp = new byte[signature.length];
        System.arraycopy(signature, 0, tmp, 0, signature.length);
        signature = tmp;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public SignedObject(Serializable object, PrivateKey signingKey,
            Signature signingEngine) throws IOException, InvalidKeyException,
            SignatureException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        try {
            // Serialize
            oos.writeObject(object);
            oos.flush();
        } finally {
            oos.close();
        }
        content = baos.toByteArray();
        signingEngine.initSign(signingKey);
        thealgorithm = signingEngine.getAlgorithm();
        signingEngine.update(content);
        signature = signingEngine.sign();
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public Object getObject() throws IOException, ClassNotFoundException {
        // deserialize our object
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                content));
        try {
            return ois.readObject();
        } finally {
            ois.close();
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public byte[] getSignature() {
        byte[] sig = new byte[signature.length];
        System.arraycopy(signature, 0, sig, 0, signature.length);
        return sig;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public String getAlgorithm() {
        return thealgorithm;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public boolean verify(PublicKey verificationKey,
            Signature verificationEngine) throws InvalidKeyException,
            SignatureException {

        verificationEngine.initVerify(verificationKey);
        verificationEngine.update(content);
        return verificationEngine.verify(signature);
    }

}