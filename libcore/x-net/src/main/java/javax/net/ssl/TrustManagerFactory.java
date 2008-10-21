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

import org.apache.harmony.security.fortress.Engine;


/**
 * @com.intel.drl.spec_ref
 * 
 */

public class TrustManagerFactory {
    // Store TrustManager service name
    private static final String SERVICE = "TrustManagerFactory";

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    // Store default property name
    private static final String PROPERTYNAME = "ssl.TrustManagerFactory.algorithm";

    // Store used provider
    private final Provider provider;

    // Storeused TrustManagerFactorySpi implementation
    private final TrustManagerFactorySpi spiImpl;

    // Store used algorithm
    private final String algorithm;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected TrustManagerFactory(TrustManagerFactorySpi factorySpi,
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
    public static final TrustManagerFactory getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new TrustManagerFactory((TrustManagerFactorySpi) engine.spi,
                    engine.provider, algorithm);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if algorithm is null (instead of
     * NoSuchAlgorithmException as in 1.4 release)
     */
    public static final TrustManagerFactory getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException("Provider is null oe empty");
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
    public static final TrustManagerFactory getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new TrustManagerFactory((TrustManagerFactorySpi) engine.spi,
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
    public final void init(KeyStore ks) throws KeyStoreException {
        spiImpl.engineInit(ks);
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
    public final TrustManager[] getTrustManagers() {
        return spiImpl.engineGetTrustManagers();
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public static final String getDefaultAlgorithm() {
        return AccessController
                .doPrivileged(new java.security.PrivilegedAction<String>() {
                    public String run() {
                        return Security.getProperty(PROPERTYNAME);
                    }
                });
    }
}