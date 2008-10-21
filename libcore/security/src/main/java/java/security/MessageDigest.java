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

import org.apache.harmony.security.fortress.Engine;
import org.apache.harmony.security.internal.nls.Messages;


/**
 * @com.intel.drl.spec_ref
 * 
 */

public abstract class MessageDigest extends MessageDigestSpi {
    
    // The service name
    private static final String SERVICE = "MessageDigest"; //$NON-NLS-1$

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    // The provider
    private Provider provider;

    // The algorithm.
    private String algorithm;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected MessageDigest(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public static MessageDigest getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); //$NON-NLS-1$
        }
        MessageDigest result;
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            if (engine.spi instanceof MessageDigest) {
                result = (MessageDigest) engine.spi;
                result.algorithm = algorithm;
                result.provider = engine.provider;
                return result;
            } else {
                result = new MessageDigestImpl((MessageDigestSpi) engine.spi,
                        engine.provider, algorithm);
                return result;
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public static MessageDigest getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.02")); //$NON-NLS-1$
        }
        Provider p = Security.getProvider(provider);
        if (p == null) {
            throw new NoSuchProviderException(Messages.getString("security.03", provider)); //$NON-NLS-1$
        }
        return getInstance(algorithm, p);
    }

    /**
     * Returns a new MessageDigest which is capable of running the algorithm
     * described by the argument. The result will be an instance of a subclass
     * of MessageDigest which implements that algorithm.
     * 
     * 
     * @param algorithm
     *            java.lang.String Name of the algorithm desired
     * @param provider
     *            Provider Provider which has to implement the algorithm
     * @return MessageDigest a concrete implementation for the algorithm
     *         desired.
     * 
     * @exception NoSuchAlgorithmException
     *                If the algorithm cannot be found
     */
    public static MessageDigest getInstance(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); //$NON-NLS-1$
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); //$NON-NLS-1$
        }
        MessageDigest result;
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            if (engine.spi instanceof MessageDigest) {
                result = (MessageDigest) engine.spi;
                result.algorithm = algorithm;
                result.provider = provider;
                return result;
            } else {
                result = new MessageDigestImpl((MessageDigestSpi) engine.spi,
                        provider, algorithm);
                return result;
            }
        }
    }

    // BEGIN android-note
    // Removed @see tag that didn't seem to actually refer to anything.
    // END android-note
    
    /**
     * Puts the receiver back in an initial state, such that it is ready to
     * compute a new hash.
     */
    public void reset() {
        engineReset();
    }

    /**
     * Includes the argument in the hash value computed
     * by the receiver.
     *
     * @param arg0 byte
     *             the byte to feed to the hash algorithm
     *
     * @see #reset()
     */
    public void update(byte arg0) {
        engineUpdate(arg0);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void update(byte[] input, int offset, int len) {
        if (input == null ||
                // offset < 0 || len < 0 ||
                // checks for negative values are commented out intentionally
                // see HARMONY-1120 for details
                (long) offset + (long) len > input.length) {
            throw new IllegalArgumentException(Messages
                    .getString("security.05")); //$NON-NLS-1$
        }
        engineUpdate(input, offset, len);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void update(byte[] input) {
        if (input == null) {
            throw new NullPointerException(Messages.getString("security.06")); //$NON-NLS-1$
        }
        engineUpdate(input, 0, input.length);
    }

    /**
     * Computes and returns the final hash value that the receiver represents.
     * After the digest is computed the receiver is reset.
     * 
     * @return the hash the receiver computed
     * 
     * @see #reset
     */
    public byte[] digest() {
        return engineDigest();
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public int digest(byte[] buf, int offset, int len) throws DigestException {
        if (buf == null ||
                // offset < 0 || len < 0 ||
                // checks for negative values are commented out intentionally
                // see HARMONY-1148 for details
                (long) offset + (long) len > buf.length) {
            throw new IllegalArgumentException(Messages
                    .getString("security.05")); //$NON-NLS-1$
        }
        return engineDigest(buf, offset, len);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public byte[] digest(byte[] input) {
        update(input);
        return digest();
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a printable representation for the receiver.
     */
    public String toString() {
        return "MESSAGE DIGEST " + algorithm; //$NON-NLS-1$
    }

    /**
     * Does a simply byte-per-byte compare of the two digests.
     * 
     * @param digesta
     *            One of the digests to compare
     * @param digestb
     *            The digest to compare to
     * 
     * @return <code>true</code> if the two hashes are equal
     *         <code>false</code> if the two hashes are not equal
     */
    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        if (digesta.length != digestb.length) {
            return false;
        }
        for (int i = 0; i < digesta.length; i++) {
            if (digesta[i] != digestb[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the standard Java Security name for the algorithm being used by
     * the receiver.
     * 
     * @return String the name of the algorithm
     */
    public final String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the Provider of the digest represented by the receiver.
     * 
     * @return Provider an instance of a subclass of java.security.Provider
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Return the engine digest length in bytes. Default is 0.
     * 
     * @return int the engine digest length in bytes
     * 
     */
    public final int getDigestLength() {
        int l = engineGetDigestLength();
        if (l != 0) {
            return l;
        }
        if (!(this instanceof Cloneable)) {
            return 0;
        }
        try {
            MessageDigest md = (MessageDigest) clone();
            return md.digest().length;
        } catch (CloneNotSupportedException e) {
            return 0;
        }
    }

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

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final void update(ByteBuffer input) {
        engineUpdate(input);
    }

    /**
     * 
     * The internal MessageDigest implementation
     * 
     */
    private static class MessageDigestImpl extends MessageDigest {
        
        // MessageDigestSpi implementation
        private MessageDigestSpi spiImpl;

        // MessageDigestImpl ctor
        private MessageDigestImpl(MessageDigestSpi messageDigestSpi,
                Provider provider, String algorithm) {
            super(algorithm);
            super.provider = provider;
            spiImpl = messageDigestSpi;
        }

        // engineReset() implementation
        protected void engineReset() {
            spiImpl.engineReset();
        }

        // engineDigest() implementation
        protected byte[] engineDigest() {
            return spiImpl.engineDigest();
        }

        // engineGetDigestLength() implementation
        protected int engineGetDigestLength() {
            return spiImpl.engineGetDigestLength();
        }

        // engineUpdate() implementation
        protected void engineUpdate(byte arg0) {
            spiImpl.engineUpdate(arg0);
        }

        // engineUpdate() implementation
        protected void engineUpdate(byte[] arg0, int arg1, int arg2) {
            spiImpl.engineUpdate(arg0, arg1, arg2);
        }

        // Returns a clone if the spiImpl is cloneable
        public Object clone() throws CloneNotSupportedException {
            if (spiImpl instanceof Cloneable) {
                MessageDigestSpi spi = (MessageDigestSpi) spiImpl.clone();
                return new MessageDigestImpl(spi, getProvider(), getAlgorithm());
            } else {
                throw new CloneNotSupportedException();
            }
        }
    }
}
