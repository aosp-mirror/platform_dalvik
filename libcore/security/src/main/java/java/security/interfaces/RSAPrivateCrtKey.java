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

package java.security.interfaces;

import java.math.BigInteger;

/**
 * The interface for a PKCS#1 RSA private key using CRT information values.
 * 
 * @since Android 1.0
 */
public interface RSAPrivateCrtKey extends RSAPrivateKey {

    /**
     * The serial version identifier.
     * 
     * @since Android 1.0
     */
    public static final long serialVersionUID = -5682214253527700368L;

    /**
     * Returns the CRT coefficient, {@code q^-1 mod p}.
     * 
     * @return the CRT coefficient.
     * @since Android 1.0
     */
    public BigInteger getCrtCoefficient();

    /**
     * Returns the prime factor {@code p} of {@code n}.
     * 
     * @return the prime factor {@code p} of {@code n}.
     * @since Android 1.0
     */
    public BigInteger getPrimeP();

    /**
     * Returns the prime factor {@code q} of {@code n}.
     * 
     * @return the prime factor {@code q} of {@code n}.
     * @since Android 1.0
     */
    public BigInteger getPrimeQ();

    /**
     * Returns the CRT exponent of the primet {@code p}.
     * 
     * @return the CRT exponent of the prime {@code p}.
     * @since Android 1.0
     */
    public BigInteger getPrimeExponentP();

    /**
     * Returns the CRT exponent of the prime {@code q}.
     * 
     * @return the CRT exponent of the prime {@code q}.
     * @since Android 1.0
     */
    public BigInteger getPrimeExponentQ();

    /**
     * Returns the public exponent {@code e}.
     * 
     * @return the public exponent {@code e}.
     * @since Android 1.0
     */
    public BigInteger getPublicExponent();
}