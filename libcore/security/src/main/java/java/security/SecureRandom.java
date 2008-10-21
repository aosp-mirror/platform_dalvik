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

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.apache.harmony.security.fortress.Engine;
import org.apache.harmony.security.fortress.Services;
import org.apache.harmony.security.internal.nls.Messages;

import org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl;

/**
 * @com.intel.drl.spec_ref
 * 
 */

public class SecureRandom extends Random {
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    private static final long serialVersionUID = 4940670005562187L;
    
    // The service name.
    private static final transient String SERVICE = "SecureRandom"; //$NON-NLS-1$
    
    // Used to access common engine functionality
    private static transient Engine engine = new Engine(SERVICE);
    
    /**
     * @com.intel.drl.spec_ref
     */
    private Provider provider;
    
    /**
     * @com.intel.drl.spec_ref
     */
    private SecureRandomSpi secureRandomSpi; 
    
    /**
     * @com.intel.drl.spec_ref
     */
    private String algorithm;
    
    /**
     * @com.intel.drl.spec_ref
     */
    private byte[] state;
    
    /**
     * @com.intel.drl.spec_ref
     */
    private byte[] randomBytes;
    
    /**
     * @com.intel.drl.spec_ref
     */
    private int randomBytesUsed;
    
    /**
     * @com.intel.drl.spec_ref
     */
    private long counter;
    
    // Internal SecureRandom used for getSeed(int)
    private static transient SecureRandom internalSecureRandom;
    
    /**
     * Constructs a new instance of this class. Users are encouraged to use
     * <code>getInstance()</code> instead.
     * 
     * An implementation for the highest-priority provider is returned. The
     * instance returned will not have been seeded.
     */
    public SecureRandom() {
        super(0);
        Provider.Service service = findService();
        if (service == null) {
            this.provider = null;
            this.secureRandomSpi = new SHA1PRNG_SecureRandomImpl();
            this.algorithm = "SHA1PRNG"; //$NON-NLS-1$
        } else {
            try {
                this.provider = service.getProvider();
                this.secureRandomSpi = (SecureRandomSpi)service.newInstance(null);
                this.algorithm = service.getAlgorithm();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }            
        }    
    }

    /**
     * Constructs a new instance of this class. Users are encouraged to use
     * <code>getInstance()</code> instead.
     * 
     * An implementation for the highest-priority provider is returned. The
     * instance returned will be seeded with the parameter.
     * 
     * @param seed
     *            bytes forming the seed for this generator.
     */
    public SecureRandom(byte[] seed) {
        this();
        setSeed(seed);
    }
    
    //Find SecureRandom service.
    private Provider.Service findService() {
        Set s;
        Provider.Service service;
        for (Iterator it1 = Services.getProvidersList().iterator(); it1.hasNext();) {
            service = ((Provider)it1.next()).getService("SecureRandom"); //$NON-NLS-1$
            if (service != null) {
                return service;
            }
        }
        return null;
    }
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected SecureRandom(SecureRandomSpi secureRandomSpi,
                           Provider provider) {
        this(secureRandomSpi, provider, "unknown"); //$NON-NLS-1$
    }
    
    // Constructor
    private SecureRandom(SecureRandomSpi secureRandomSpi,
                         Provider provider,
                         String algorithm) {
        super(0);
        this.provider = provider;
        this.algorithm = algorithm;
        this.secureRandomSpi = secureRandomSpi;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public static SecureRandom getInstance(String algorithm)
                                throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); //$NON-NLS-1$
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new SecureRandom((SecureRandomSpi)engine.spi, engine.provider, algorithm);
        }
    }

    /**
     * Returns a new SecureRandom which is capable of running the algorithm
     * described by the argument. The result will be an instance of a subclass
     * of SecureRandomSpi which implements that algorithm.
     * 
     * @param algorithm
     *            java.lang.String Name of the algorithm desired
     * @param provider
     *            java.security.Provider Provider which has to implement the
     *            algorithm
     * @return SecureRandom a concrete implementation for the algorithm desired.
     * 
     * @exception NoSuchAlgorithmException
     *                If the algorithm cannot be found
     */
    public static SecureRandom getInstance(String algorithm, String provider)
                                throws NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(
                    Messages.getString("security.02")); //$NON-NLS-1$
        }
        Provider p = Security.getProvider(provider);
        if (p == null) {
            throw new NoSuchProviderException(Messages.getString("security.03", provider));  //$NON-NLS-1$
        }
        return getInstance(algorithm, p);    
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public static SecureRandom getInstance(String algorithm, Provider provider)
                                throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); //$NON-NLS-1$
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); //$NON-NLS-1$
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new SecureRandom((SecureRandomSpi)engine.spi, provider, algorithm);
        }
    }

    /**
     * Returns the Provider of the secure random represented by the receiver.
     * 
     * @return Provider an instance of a subclass of java.security.Provider
     */
    public final Provider getProvider() {
        return provider;
    }
    
    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public synchronized void setSeed(byte[] seed) {
        secureRandomSpi.engineSetSeed(seed);
    }

    /**
     * Reseeds this random object with the eight bytes described by the
     * representation of the long provided.
     * 
     * 
     * @param seed
     *            long Number whose representation to use to reseed the
     *            receiver.
     */
    public void setSeed(long seed) {
        if (seed == 0) {    // skip call from Random
            return;
        }
        byte[] byteSeed = {
                (byte)((seed >> 56) & 0xFF),
                (byte)((seed >> 48) & 0xFF),
                (byte)((seed >> 40) & 0xFF),
                (byte)((seed >> 32) & 0xFF),
                (byte)((seed >> 24) & 0xFF),
                (byte)((seed >> 16) & 0xFF),
                (byte)((seed >> 8) & 0xFF),
                (byte)((seed) & 0xFF)
        };
        setSeed(byteSeed);
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public synchronized void nextBytes(byte[] bytes) {
        secureRandomSpi.engineNextBytes(bytes);
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected final int next(int numBits) {
        if (numBits < 0) {
            numBits = 0;
        } else {
            if (numBits > 32) {
                numBits = 32;
            }
        }
        int bytes = (numBits+7)/8;
        byte[] next = new byte[bytes];
        int ret = 0;
         
        nextBytes(next);
        for (int i = 0; i < bytes; i++) {
            ret = (next[i] & 0xFF) | (ret << 8);
        }    
        ret = ret >>> (bytes*8 - numBits);
        return ret;
    }

    /**
     * Returns the given number of seed bytes, computed using the seed
     * generation algorithm used by this class.
     * 
     * @param numBytes
     *            int the given number of seed bytes
     * @return byte[] The seed bytes generated
     */
    public static byte[] getSeed(int numBytes) {
        if (internalSecureRandom == null) {
            internalSecureRandom = new SecureRandom();
        }
        return internalSecureRandom.generateSeed(numBytes);
    }

    /**
     * Generates a certain number of seed bytes
     * 
     * 
     * @param numBytes
     *            int Number of seed bytes to generate
     * @return byte[] The seed bytes generated
     */
    public byte[] generateSeed(int numBytes) {
        return secureRandomSpi.engineGenerateSeed(numBytes);
    }
    
}
