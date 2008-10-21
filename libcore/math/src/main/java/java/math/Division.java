/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/*
 * Since the original Harmony Code of the BigInteger class was strongly modified,
 * in order to use the more efficient OpenSSL BIGNUM implementation,
 * no android-modification-tags were placed, at all.
 */

package java.math;


/**
 * Static library that provides all operations related with division and modular
 * arithmetic to {@link BigInteger}. Some methods are provided in both mutable
 * and immutable way. There are several variants provided listed below:
 * 
 * <ul type="circle">
 * <li> <b>Division</b>
 * <ul type="circle">
 * <li>{@link BigInteger} division and remainder by {@link BigInteger}.</li>
 * <li>{@link BigInteger} division and remainder by {@code int}.</li>
 * <li><i>gcd</i> between {@link BigInteger} numbers.</li>
 * </ul>
 * </li>
 * <li> <b>Modular arithmetic </b>
 * <ul type="circle">
 * <li>Modular exponentiation between {@link BigInteger} numbers.</li>
 * <li>Modular inverse of a {@link BigInteger} numbers.</li>
 * </ul>
 * </li>
 *</ul>
 * 
 * @author Intel Middleware Product Division
 * @author Instituto Tecnologico de Cordoba
 */
class Division {

    /**
     * Divides an array by an integer value. Implements the Knuth's division
     * algorithm. See D. Knuth, The Art of Computer Programming, vol. 2.
     * 
     * @param dest the quotient
     * @param src the dividend
     * @param srcLength the length of the dividend
     * @param divisor the divisor
     * @return remainder
     */
    static int divideArrayByInt(int dest[], int src[], final int srcLength,
            final int divisor) {

        long rem = 0;
        long bLong = divisor & 0xffffffffL;

        for (int i = srcLength - 1; i >= 0; i--) {
            long temp = (rem << 32) | (src[i] & 0xffffffffL);
            long quot;
            if (temp >= 0) {
                quot = (temp / bLong);
                rem = (temp % bLong);
            } else {
                /*
                 * make the dividend positive shifting it right by 1 bit then
                 * get the quotient an remainder and correct them properly
                 */
                long aPos = temp >>> 1;
                long bPos = divisor >>> 1;
                quot = aPos / bPos;
                rem = aPos % bPos;
                // double the remainder and add 1 if a is odd
                rem = (rem << 1) + (temp & 1);
                if ((divisor & 1) != 0) {
                    // the divisor is odd
                    if (quot <= rem) {
                        rem -= quot;
                    } else {
                        if (quot - rem <= bLong) {
                            rem += bLong - quot;
                            quot -= 1;
                        } else {
                            rem += (bLong << 1) - quot;
                            quot -= 2;
                        }
                    }
                }
            }
            dest[i] = (int) (quot & 0xffffffffL);
        }
        return (int) rem;
    }

    /**
     * Divides an array by an integer value. Implements the Knuth's division
     * algorithm. See D. Knuth, The Art of Computer Programming, vol. 2.
     * 
     * @param src the dividend
     * @param srcLength the length of the dividend
     * @param divisor the divisor
     * @return remainder
     */
    static int remainderArrayByInt(int src[], final int srcLength,
            final int divisor) {

        long result = 0;

        for (int i = srcLength - 1; i >= 0; i--) {
            long temp = (result << 32) + (src[i] & 0xffffffffL);
            long res = divideLongByInt(temp, divisor);
            result = (int) (res >> 32);
        }
        return (int) result;
    }

    /**
     * Divides a <code>BigInteger</code> by a signed <code>int</code> and
     * returns the remainder.
     * 
     * @param dividend the BigInteger to be divided. Must be non-negative.
     * @param divisor a signed int
     * @return divide % divisor
     */
    static int remainder(BigInteger dividend, int divisor) {
        dividend.establishOldRepresentation("Division.remainder");
        return remainderArrayByInt(dividend.digits, dividend.numberLength,
                divisor);
    }

    /**
     * Divides an unsigned long a by an unsigned int b. It is supposed that the
     * most significant bit of b is set to 1, i.e. b < 0
     * 
     * @param a the dividend
     * @param b the divisor
     * @return the long value containing the unsigned integer remainder in the
     *         left half and the unsigned integer quotient in the right half
     */
    static long divideLongByInt(long a, int b) {
        long quot;
        long rem;
        long bLong = b & 0xffffffffL;

        if (a >= 0) {
            quot = (a / bLong);
            rem = (a % bLong);
        } else {
            /*
             * Make the dividend positive shifting it right by 1 bit then get
             * the quotient an remainder and correct them properly
             */
            long aPos = a >>> 1;
            long bPos = b >>> 1;
            quot = aPos / bPos;
            rem = aPos % bPos;
            // double the remainder and add 1 if a is odd
            rem = (rem << 1) + (a & 1);
            if ((b & 1) != 0) { // the divisor is odd
                if (quot <= rem) {
                    rem -= quot;
                } else {
                    if (quot - rem <= bLong) {
                        rem += bLong - quot;
                        quot -= 1;
                    } else {
                        rem += (bLong << 1) - quot;
                        quot -= 2;
                    }
                }
            }
        }
        return (rem << 32) | (quot & 0xffffffffL);
    }

    /**
     * Computes the quotient and the remainder after a division by an {@code int}
     * number.
     * 
     * @return an array of the form {@code [quotient, remainder]}.
     */
    static BigInteger[] divideAndRemainderByInteger(BigInteger val,
            int divisor, int divisorSign) {
        val.establishOldRepresentation("Division.divideAndRemainderByInteger");
        // res[0] is a quotient and res[1] is a remainder:
        int[] valDigits = val.digits;
        int valLen = val.numberLength;
        int valSign = val.sign;
        if (valLen == 1) {
            long a = (valDigits[0] & 0xffffffffL);
            long b = (divisor & 0xffffffffL);
            long quo = a / b;
            long rem = a % b;
            if (valSign != divisorSign) {
                quo = -quo;
            }
            if (valSign < 0) {
                rem = -rem;
            }
            return new BigInteger[] { BigInteger.valueOf(quo),
                    BigInteger.valueOf(rem) };
        }
        int quotientLength = valLen;
        int quotientSign = ((valSign == divisorSign) ? 1 : -1);
        int quotientDigits[] = new int[quotientLength];
        int remainderDigits[];
        remainderDigits = new int[] { Division.divideArrayByInt(
                quotientDigits, valDigits, valLen, divisor) };
        BigInteger result0 = new BigInteger(quotientSign, quotientLength,
                quotientDigits);
        BigInteger result1 = new BigInteger(valSign, 1, remainderDigits);
        result0.cutOffLeadingZeroes();
        result1.cutOffLeadingZeroes();
        return new BigInteger[] { result0, result1 };
    }

}
