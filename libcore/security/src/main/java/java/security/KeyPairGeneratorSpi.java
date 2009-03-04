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

import org.apache.harmony.security.internal.nls.Messages;

/**
 * {@code KeyPairGeneratorSpi} is the Service Provider Interface (SPI)
 * definition for {@link KeyPairGenerator}.
 * 
 * @see KeyPairGenerator
 * @since Android 1.0
 */
public abstract class KeyPairGeneratorSpi {
    
    /**
     * Constructs a new instance of {@code KeyPairGeneratorSpi}.
     * 
     * @since Android 1.0
     */
    public KeyPairGeneratorSpi() {
    }

    /**
     * Computes and returns a new unique {@code KeyPair} each time this method
     * is called.
     * 
     * @return a new unique {@code KeyPair} each time this method is called.
     * @since Android 1.0
     */
    public abstract KeyPair generateKeyPair();

    /**
     * Initializes this {@code KeyPairGeneratorSpi} with the given key size and
     * the given {@code SecureRandom}. The default parameter set will be used.
     * 
     * @param keysize
     *            the key size (number of bits).
     * @param random
     *            the source of randomness.
     * @since Android 1.0
     */
    public abstract void initialize(int keysize, SecureRandom random);

    /**
     * Initializes this {@code KeyPairGeneratorSpi} with the given {@code
     * AlgorithmParameterSpec} and the given {@code SecureRandom}.
     * 
     * @param params
     *            the parameters to use.
     * @param random
     *            the source of randomness.
     * @throws InvalidAlgorithmParameterException
     *             if the specified parameters are not supported.
     * @since Android 1.0
     */
    public void initialize(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException(Messages.getString("security.2E")); //$NON-NLS-1$
    }
}