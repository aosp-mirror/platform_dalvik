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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package java.security;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.spec.SecretKeySpec;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class KeyRep implements Serializable {
    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = -4757683898830641853L;
    // Key type
    private final Type type;
    // Key algorithm name
    private final String algorithm;
    // Key encoding format
    private final String format;
    // Key encoding
    private byte[] encoded;

    /**
     * @com.intel.drl.spec_ref
     */
    public KeyRep(Type type,
            String algorithm, String format, byte[] encoded) {
        this.type = type;
        this.algorithm = algorithm;
        this.format = format;
        this.encoded = encoded;
        if(this.type == null) {
            throw new NullPointerException(Messages.getString("security.07")); //$NON-NLS-1$
        }
        if(this.algorithm == null) {
            throw new NullPointerException(Messages.getString("security.08")); //$NON-NLS-1$
        }
        if(this.format == null) {
            throw new NullPointerException(Messages.getString("security.09")); //$NON-NLS-1$
        }
        if(this.encoded == null) {
            throw new NullPointerException(Messages.getString("security.0A")); //$NON-NLS-1$
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected Object readResolve() throws ObjectStreamException {
        switch (type) {
        case SECRET:
            if ("RAW".equals(format)) { //$NON-NLS-1$
                try {
                    return new SecretKeySpec(encoded, algorithm);
                } catch (IllegalArgumentException e) {
                    throw new NotSerializableException(
                            Messages.getString("security.0B", e)); //$NON-NLS-1$
                }
            }
            throw new NotSerializableException(
                Messages.getString("security.0C", type, format)); //$NON-NLS-1$
        case PUBLIC:
            if ("X.509".equals(format)) { //$NON-NLS-1$
                try {
                    KeyFactory kf = KeyFactory.getInstance(algorithm);
                    return kf.generatePublic(new X509EncodedKeySpec(encoded));
                } catch (NoSuchAlgorithmException e) {
                    throw new NotSerializableException(
                            Messages.getString("security.0D", e)); //$NON-NLS-1$
                }
                catch (InvalidKeySpecException e) {
                    throw new NotSerializableException(
                            Messages.getString("security.0D", e)); //$NON-NLS-1$
                }
            }
            throw new NotSerializableException(
                Messages.getString("security.0C", type, format)); //$NON-NLS-1$
        case PRIVATE:
            if ("PKCS#8".equals(format)) { //$NON-NLS-1$
                try {
                    KeyFactory kf = KeyFactory.getInstance(algorithm);
                    return kf.generatePrivate(new PKCS8EncodedKeySpec(encoded));
                } catch (NoSuchAlgorithmException e) {
                    throw new NotSerializableException(
                            Messages.getString("security.0D", e)); //$NON-NLS-1$
                }
                catch (InvalidKeySpecException e) {
                    throw new NotSerializableException(
                            Messages.getString("security.0D", e)); //$NON-NLS-1$
                }
            }
            throw new NotSerializableException(
                Messages.getString("security.0C", type, format)); //$NON-NLS-1$
        }
        throw new NotSerializableException(Messages.getString("security.0E", type)); //$NON-NLS-1$
    }

    // Makes defensive copy of key encoding
    private void readObject(ObjectInputStream is)
        throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        byte[] new_encoded = new byte[encoded.length];
        System.arraycopy(encoded, 0, new_encoded, 0, new_encoded.length);
        encoded = new_encoded;    
    }

    /**
     * Supported key types
     */
    public static enum Type {
        SECRET,
        PUBLIC,
        PRIVATE
    }
}
