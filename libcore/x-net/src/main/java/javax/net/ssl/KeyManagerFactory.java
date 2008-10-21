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

package javax.net.ssl;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;

import org.apache.harmony.security.fortress.Engine;


/**
 * @com.intel.drl.spec_ref
 * 
 */

public class KeyManagerFactory {
    // Store KeyManagerFactory service name
    private static final String SERVICE = "KeyManagerFactory";

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    // Store default property name
    private static final String PROPERTY_NAME = "ssl.KeyManagerFactory.algorithm";

    // Store used provider
    private final Provider provider;

    // Store used KeyManagerFactorySpi implementation
    private final KeyManagerFactorySpi spiImpl;

    // Store used algorithm
    private final String algorithm;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected KeyManagerFactory(KeyManagerFactorySpi factorySpi,
            Provider provider, String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = factorySpi;
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
     * throws NullPointerException if algorithm is null (instead of
     * NoSuchAlgorithmException as in 1.4 release)
     */
    public static final KeyManagerFactory getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorith is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new KeyManagerFactory((KeyManagerFactorySpi) engine.spi,
                    engine.provider, algorithm);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if algorithm is null (instead of
     * NoSuchAlgorithmException as in 1.4 release)
     */
    public static final KeyManagerFactory getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException("Provider is null or empty");
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
     * NoSuchAlgorithmException as in 1.4 release)
     */
    public static final KeyManagerFactory getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorith is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new KeyManagerFactory((KeyManagerFactorySpi) engine.spi,
                    provider, algorithm);
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
    public final void init(KeyStore ks, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException {
        spiImpl.engineInit(ks, password);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final void init(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException {
        spiImpl.engineInit(spec);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final KeyManager[] getKeyManagers() {
        return spiImpl.engineGetKeyManagers();
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public static final String getDefaultAlgorithm() {
        return AccessController
                .doPrivileged(new java.security.PrivilegedAction<String>() {
                    public String run() {
                        return Security.getProperty(PROPERTY_NAME);
                    }
                });
    }
}