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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package java.security;

import java.security.spec.AlgorithmParameterSpec;

import org.apache.harmony.security.fortress.Engine;
import org.apache.harmony.security.internal.nls.Messages;


/**
 * 
 * @com.intel.drl.spec_ref
 * 
 */
public abstract class KeyPairGenerator extends KeyPairGeneratorSpi {

    // Store KeyPairGenerator SERVICE name
    private static final String SERVICE = "KeyPairGenerator"; //$NON-NLS-1$

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    // Store SecureRandom
    private static SecureRandom random = new SecureRandom();

    // Store used provider
    private Provider provider;

    // Store used algorithm
    private String algorithm;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected KeyPairGenerator(String algorithm) {
        this.algorithm = algorithm;
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
     * throws NullPointerException when algorithm is null
     */
    public static KeyPairGenerator getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); //$NON-NLS-1$
        }
        KeyPairGenerator result;
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            if (engine.spi instanceof KeyPairGenerator) {
                result = (KeyPairGenerator) engine.spi;
                result.algorithm = algorithm;
                result.provider = engine.provider;
                return result;
            } else {
                result = new KeyPairGeneratorImpl((KeyPairGeneratorSpi) engine.spi,
                        engine.provider, algorithm);
                return result;
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if algorithm is null (instead of
     * NoSuchAlgorithmException) as in 1.4 release
     */
    public static KeyPairGenerator getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(
                    Messages.getString("security.02")); //$NON-NLS-1$
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(algorithm, impProvider);
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if algorithm is null (instead of
     * NoSuchAlgorithmException) as in 1.4 release
     */
    public static KeyPairGenerator getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); //$NON-NLS-1$
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); //$NON-NLS-1$
        }
        KeyPairGenerator result;
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            if (engine.spi instanceof KeyPairGenerator) {
                result = (KeyPairGenerator) engine.spi;
                result.algorithm = algorithm;
                result.provider = provider;
                return result;
            } else {
                result = new KeyPairGeneratorImpl((KeyPairGeneratorSpi) engine.spi,
                        provider, algorithm);
                return result;
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void initialize(int keysize) {
        initialize(keysize, random);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void initialize(AlgorithmParameterSpec param)
            throws InvalidAlgorithmParameterException {
        initialize(param, random);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final KeyPair genKeyPair() {
        return generateKeyPair();
    }

    public KeyPair generateKeyPair() {
        return null;
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void initialize(int keysize, SecureRandom random) {
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public void initialize(AlgorithmParameterSpec param, SecureRandom random)
            throws InvalidAlgorithmParameterException {
    }

    /**
     * 
     * Internal class: KeyPairGenerator implementation
     * 
     */
    private static class KeyPairGeneratorImpl extends KeyPairGenerator {
        // Save KeyPairGeneratorSpi
        private KeyPairGeneratorSpi spiImpl;

        // Implementation of KeyPaiGenerator constructor
        // 
        // @param KeyPairGeneratorSpi
        // @param provider
        // @param algorithm
        private KeyPairGeneratorImpl(KeyPairGeneratorSpi keyPairGeneratorSpi,
                Provider provider, String algorithm) {
            super(algorithm);
            super.provider = provider;
            spiImpl = keyPairGeneratorSpi;
        }

        // implementation of initialize(int keysize, SecureRandom random)
        // using corresponding spi initialize() method
        public void initialize(int keysize, SecureRandom random) {
            spiImpl.initialize(keysize, random);
        }

        // implementation of generateKeyPair()
        // using corresponding spi generateKeyPair() method
        public KeyPair generateKeyPair() {
            return spiImpl.generateKeyPair();
        }

        // implementation of initialize(int keysize, SecureRandom random)
        // using corresponding spi initialize() method
        public void initialize(AlgorithmParameterSpec param, SecureRandom random)
                throws InvalidAlgorithmParameterException {
            spiImpl.initialize(param, random);
        }

    }

}