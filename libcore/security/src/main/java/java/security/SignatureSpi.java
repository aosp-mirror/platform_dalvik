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

import java.nio.ByteBuffer;
import java.security.spec.AlgorithmParameterSpec;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref
 * 
 */

public abstract class SignatureSpi {

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected SecureRandom appRandom;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected abstract void engineInitVerify(PublicKey publicKey)
            throws InvalidKeyException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected abstract void engineInitSign(PrivateKey privateKey)
            throws InvalidKeyException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random)
            throws InvalidKeyException {
        appRandom = random;
        engineInitSign(privateKey);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected abstract void engineUpdate(byte b) throws SignatureException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected abstract void engineUpdate(byte[] b, int off, int len)
            throws SignatureException;

    /**
     * @com.intel.drl.spec_ref
     * 
     * The SignatureException is not specified for this method. 
     * So throw RuntimeException if underlying engineUpdate(byte[] b, int off, int len)
     * throws SignatureException.
     */
    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        byte[] tmp;
        if (input.hasArray()) {
            tmp = input.array();
            int offset = input.arrayOffset();
            int position = input.position();
            int limit = input.limit();
            try {
                engineUpdate(tmp, offset + position, limit - position);
            } catch (SignatureException e) { 
                throw new RuntimeException(e); //Wrap SignatureException
            }
            input.position(limit);
        } else {
            tmp = new byte[input.limit() - input.position()];
            input.get(tmp);
            try {
                engineUpdate(tmp, 0, tmp.length);
            } catch (SignatureException e) {
                throw new RuntimeException(e); //Wrap SignatureException
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected abstract byte[] engineSign() throws SignatureException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected int engineSign(byte[] outbuf, int offset, int len)
            throws SignatureException {
        byte tmp[] = engineSign();
        if (tmp == null) {
            return 0;
        }
        if (len < tmp.length) {
            throw new SignatureException(Messages.getString("security.2D")); //$NON-NLS-1$
        }
        if (offset < 0) {
            throw new SignatureException(Messages.getString("security.1C")); //$NON-NLS-1$
        }
        if (offset + len > outbuf.length) {
            throw new SignatureException(Messages.getString("security.05")); //$NON-NLS-1$
        }
        System.arraycopy(tmp, 0, outbuf, offset, tmp.length);
        return tmp.length;
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected abstract boolean engineVerify(byte[] sigBytes)
            throws SignatureException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected boolean engineVerify(byte[] sigBytes, int offset, int length)
            throws SignatureException {
        byte tmp[] = new byte[length];
        System.arraycopy(sigBytes, offset, tmp, 0, length);
        return engineVerify(tmp);
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * @deprecated Use
     *             {@link SignatureSpi#engineSetParameter(AlgorithmParameterSpec) engineSetParameter}
     */
    protected abstract void engineSetParameter(String param, Object value)
            throws InvalidParameterException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected void engineSetParameter(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException();
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected AlgorithmParameters engineGetParameters() {
        throw new UnsupportedOperationException();
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * @deprecated There is no generally accepted parameter naming convention.
     */
    protected abstract Object engineGetParameter(String param)
            throws InvalidParameterException;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }
}