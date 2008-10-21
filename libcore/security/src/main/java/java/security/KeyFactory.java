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

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import org.apache.harmony.security.fortress.Engine;
import org.apache.harmony.security.internal.nls.Messages;


/**
 * @com.intel.drl.spec_ref
 * 
 */

public class KeyFactory {
    // The service name.
    private static final String SERVICE = "KeyFactory"; //$NON-NLS-1$
    
    // The provider
    private Provider provider;
    
    
    // Used to access common engine functionality
    static private Engine engine = new Engine(SERVICE);
    
    // The SPI implementation.
    private KeyFactorySpi spiImpl; 
    
    // The algorithm.
    private String algorithm;

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected KeyFactory(KeyFactorySpi keyFacSpi, 
                         Provider provider,
                         String algorithm) {
        this.provider = provider;
        this. algorithm = algorithm;
        this.spiImpl = keyFacSpi;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public static KeyFactory getInstance(String algorithm)
                                throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); //$NON-NLS-1$
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new KeyFactory((KeyFactorySpi)engine.spi, engine.provider, algorithm);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public static KeyFactory getInstance(String algorithm, String provider)
                                throws NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.02")); //$NON-NLS-1$
        }
        Provider p = Security.getProvider(provider);
        if (p == null) {
            throw new NoSuchProviderException(Messages.getString("security.03", provider));  //$NON-NLS-1$ //$NON-NLS-2$
        }
        return getInstance(algorithm, p);    
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public static KeyFactory getInstance(String algorithm, Provider provider)
                                 throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); //$NON-NLS-1$
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); //$NON-NLS-1$
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new KeyFactory((KeyFactorySpi)engine.spi, provider, algorithm);
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
    public final String getAlgorithm() {
        return algorithm;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public final PublicKey generatePublic(KeySpec keySpec)
                                throws InvalidKeySpecException {
        return spiImpl.engineGeneratePublic(keySpec);
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public final PrivateKey generatePrivate(KeySpec keySpec)
                                throws InvalidKeySpecException {
        return spiImpl.engineGeneratePrivate(keySpec);
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public final <T extends KeySpec> T getKeySpec(Key key,
                                    Class<T> keySpec)
                            throws InvalidKeySpecException {
        return spiImpl.engineGetKeySpec(key, keySpec);
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    public final Key translateKey(Key key)
                        throws InvalidKeyException {
        return spiImpl.engineTranslateKey(key);
    }
}
