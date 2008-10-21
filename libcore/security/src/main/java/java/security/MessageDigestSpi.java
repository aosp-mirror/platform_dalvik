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

import org.apache.harmony.security.internal.nls.Messages;


/**
 * This class is a Service Provider Interface (therefore the Spi suffix) for
 * digest algorithms to be supplied by providers. Examples of digest algorithms
 * are MD5 and SHA.
 * 
 * A digest is a secure hash function for a stream of bytes, like a fingerprint
 * for the stream of bytes.
 * 
 */
public abstract class MessageDigestSpi {
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected int engineGetDigestLength() {
        return 0;
    }
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract void engineUpdate(byte input);
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract void engineUpdate(byte[] input, int offset, int len);
    
    /**
     * @com.intel.drl.spec_ref
     * 
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
            engineUpdate(tmp, offset+position, limit - position);
            input.position(limit);
        } else {
            tmp = new byte[input.limit() - input.position()];
            input.get(tmp);
            engineUpdate(tmp, 0, tmp.length);
        }    
    }
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract byte[] engineDigest();
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected int engineDigest(byte[] buf, int offset, int len)
                    throws DigestException {
        if (len < engineGetDigestLength()) {
            engineReset();
            throw new DigestException(Messages.getString("security.1B"));  //$NON-NLS-1$
        }
        if (offset < 0) {
            engineReset();
            throw new DigestException(Messages.getString("security.1C")); //$NON-NLS-1$
        }
        if (offset + len > buf.length) {
            engineReset();
            throw new DigestException(Messages.getString("security.1D")); //$NON-NLS-1$
        }
        byte tmp[] = engineDigest();
        if (len < tmp.length) {
            throw new DigestException(Messages.getString("security.1B")); //$NON-NLS-1$
        }
        System.arraycopy(tmp, 0, buf, offset, tmp.length);
        return tmp.length;            
    }
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected abstract void engineReset();
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
