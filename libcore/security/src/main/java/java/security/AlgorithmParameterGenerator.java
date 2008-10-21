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
 * @com.intel.drl.spec_ref
 * 
 */

public class AlgorithmParameterGenerator {

    // Store spi service name
    private static final String SERVICE = "AlgorithmParameterGenerator"; //$NON-NLS-1$

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    // Store SecureRandom
    private static SecureRandom randm = new SecureRandom();

    // Store used provider
    private final Provider provider;

    // Store used AlgorithmParameterGeneratorSpi implementation
    private final AlgorithmParameterGeneratorSpi spiImpl;

    //Store used algorithm
    private final String algorithm;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected AlgorithmParameterGenerator(
            AlgorithmParameterGeneratorSpi paramGenSpi, Provider provider,
            String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = paramGenSpi;
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
     * throws NullPointerException when algorithm is null
     */
    public static AlgorithmParameterGenerator getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); //$NON-NLS-1$
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new AlgorithmParameterGenerator(
                    (AlgorithmParameterGeneratorSpi) engine.spi, engine.provider,
                    algorithm);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if algorithm is null (instead of
     * NoSuchAlgorithmException) as in 1.4 release
     */
    public static AlgorithmParameterGenerator getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
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
    public static AlgorithmParameterGenerator getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); //$NON-NLS-1$
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); //$NON-NLS-1$
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new AlgorithmParameterGenerator(
                    (AlgorithmParameterGeneratorSpi) engine.spi, provider,
                    algorithm);
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
    public final void init(int size) {
        spiImpl.engineInit(size, randm);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final void init(int size, SecureRandom random) {
        spiImpl.engineInit(size, random);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final void init(AlgorithmParameterSpec genParamSpec)
            throws InvalidAlgorithmParameterException {
        spiImpl.engineInit(genParamSpec, randm);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final void init(AlgorithmParameterSpec genParamSpec,
            SecureRandom random) throws InvalidAlgorithmParameterException {
        spiImpl.engineInit(genParamSpec, random);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final AlgorithmParameters generateParameters() {
        return spiImpl.engineGenerateParameters();
    }
}