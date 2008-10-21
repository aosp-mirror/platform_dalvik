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

package java.math;

import org.apache.harmony.math.internal.nls.Messages;
import org.openssl.NativeBN;

import java.util.Random;

/**
 * In contrast to BigIntegers this class doesn't fake two's complement representation.
 * Any Bit-Operations, including Shifting, solely regard the unsigned magnitude.
 * Moreover BigInt objects are mutable and offer efficient in-place-operations.
 **/

class BigInt
// extends Number
// implements Comparable<BigInt>,
//        Serializable
{

    class Context {
        int bnctx;
        Context() {
            bnctx = NativeBN.BN_CTX_new();
        }
    }
    static BigInt dummy;
    static Context defaultContext;

    static {
        dummy = new BigInt();
        defaultContext = dummy.new Context();
    }

    static int getCtx (Context t) {
        return (t != null) ? t.bnctx : defaultContext.bnctx;
    }

    /** This is the serialVersionUID used by the sun implementation */
    private static final long serialVersionUID = -8287574255936472291L;

    /* Fields used for the internal representation. */
    transient int bignum = 0;


    public void dispose() {
        if (this.bignum != 0) {
            NativeBN.BN_free(this.bignum);
            this.bignum = 0;
        }
    }

    @Override
    protected void finalize() {
        dispose();
    }

    @Override
    public String toString() {
        return this.decString();
    }

    public int getNativeBIGNUM() {
        return this.bignum;
    }

    public static int consumeErrors(StringBuilder sb) {
        int cnt = 0;
        int e, reason;
        boolean first = true;
        while ((e = NativeBN.ERR_get_error()) != 0) {
            reason = e & 255;
            if (reason == 103) {
                // math.17=BigInteger divide by zero
                throw new ArithmeticException(Messages.getString("math.17")); //$NON-NLS-1$
            }
            if (reason == 108) {
                // math.19=BigInteger not invertible.
                throw new ArithmeticException(Messages.getString("math.19")); //$NON-NLS-1$
            }
            if (reason == 65) {
                throw new OutOfMemoryError();
            }
            if (!first) { sb.append(" *** "); first = false; }
            sb.append(e).append(": ");
            String s = NativeBN.ERR_error_string(e);
            sb.append(s);
            cnt++;
        }
        return cnt;
    }

    private static void Check(boolean success) {
        if (!success) {
            StringBuilder sb = new StringBuilder("(openssl)ERR: ");
            int cnt = consumeErrors(sb);
            if (cnt > 0)
                throw new ArithmeticException(sb.toString());
        }
    }


    private void makeValid() {
        if (this.bignum == 0) {
            this.bignum = NativeBN.BN_new();
            Check(this.bignum != 0);
        }
    }

    private static BigInt newBigInt() {
        BigInt bi = new BigInt();
        bi.bignum = NativeBN.BN_new();
        Check(bi.bignum != 0);
        return bi;
    }


    public static int cmp(BigInt a, BigInt b) {
        return NativeBN.BN_cmp(a.bignum, b.bignum);
    }


    public void putCopy(BigInt from) {
        this.makeValid();
        Check(NativeBN.BN_copy(this.bignum, from.bignum));
    }

    public BigInt copy() {
        BigInt bi = new BigInt();
        bi.putCopy(this);
        return bi;
    }


    public void putLongInt(long val) {
        this.makeValid();
        Check(NativeBN.putLongInt(this.bignum, val));
    }

    public void putULongInt(long val, boolean neg) {
        this.makeValid();
        Check(NativeBN.putULongInt(this.bignum, val, neg));
    }

    public void putDecString(String str) {
        if (str == null) throw new NullPointerException();
        if (str.length() == 0) {
            // math.12=Zero length BigInteger
            throw new NumberFormatException(Messages.getString("math.12")); //$NON-NLS-1$
        }
        this.makeValid();
        int usedLen = NativeBN.BN_dec2bn(this.bignum, str);
        Check((usedLen > 0));
        if (usedLen < str.length()) {
            throw new NumberFormatException(str);
        }
    }

    public void putHexString(String str) {
        if (str == null) throw new NullPointerException();
        if (str.length() == 0) {
            // math.12=Zero length BigInteger
            throw new NumberFormatException(Messages.getString("math.12")); //$NON-NLS-1$
        }
        this.makeValid();
        int usedLen = NativeBN.BN_hex2bn(this.bignum, str);
        Check((usedLen > 0));
        if (usedLen < str.length()) {
            throw new NumberFormatException(str);
        }
    }

    public void putBigEndian(byte[] a, boolean neg) {
        this.makeValid();
        Check(NativeBN.BN_bin2bn(a, a.length, neg, this.bignum));
    }

    public void putLittleEndianInts(int[] a, boolean neg) {
        this.makeValid();
        Check(NativeBN.litEndInts2bn(a, a.length, neg, this.bignum));
    }

    public void putBigEndianTwosComplement(byte[] a) {
        this.makeValid();
        Check(NativeBN.twosComp2bn(a, a.length, this.bignum));
    }


    public long longInt() {
        return NativeBN.longInt(this.bignum);
    }

    public String decString() {
        String str = NativeBN.BN_bn2dec(this.bignum);
        return str;
    }

    public String hexString() {
        String str = NativeBN.BN_bn2hex(this.bignum);
        return str;
    }

    public byte[] bigEndianMagnitude() {
        byte[] a = NativeBN.BN_bn2bin(this.bignum, null);
        return a;
    }

    public int[] littleEndianIntsMagnitude() {
        int[] a = NativeBN.bn2litEndInts(this.bignum, null);
        return a;
    }

    public byte[] bigEndianTwosComplement() {
        byte[] a = NativeBN.bn2twosComp(this.bignum, null);
        return a;
    }


    public int sign() {
        return NativeBN.sign(this.bignum);
    }

    public void setSign(int val) {
        if (val > 0) NativeBN.BN_set_negative(this.bignum, 0);
        else if (val < 0) NativeBN.BN_set_negative(this.bignum, 1);
    }


    public boolean twosCompFitsIntoBytes(int byteCnt) {
        return NativeBN.twosCompFitsIntoBytes(this.bignum, byteCnt);
    }

    public int bitLength() {
        return NativeBN.bitLength(this.bignum);
    }

    public boolean isBitSet(int n) {
        return NativeBN.BN_is_bit_set(this.bignum, n);
    }

    public void modifyBit(int n, int op) {
        // op: 0 = reset; 1 = set; -1 = flip
        Check(NativeBN.modifyBit(this.bignum, n, op));
    }


    // n > 0: shift left (multiply)
    public static BigInt shift(BigInt a, int n) {
        BigInt r = newBigInt();
        Check(NativeBN.BN_lshift(r.bignum, a.bignum, n));
        return r;
    }

    public void shift(int n) {
        Check(NativeBN.BN_lshift(this.bignum, this.bignum, n));
    }

    public void addPositiveInt(int w) {
        Check(NativeBN.BN_add_word(this.bignum, w));
    }

    public void subtractPositiveInt(int w) {
        Check(NativeBN.BN_sub_word(this.bignum, w));
    }

    public void multiplyByPositiveInt(int w) {
        Check(NativeBN.BN_mul_word(this.bignum, w));
    }

    public int divideByPositiveInt(int w) {
        int rem = NativeBN.BN_div_word(this.bignum, w);
        Check(rem != -1);
        return rem;
    }

    public static int remainderByPositiveInt(BigInt a, int w) {
        int rem = NativeBN.BN_mod_word(a.bignum, w);
        Check(rem != -1);
        return rem;
    }

    public static BigInt addition(BigInt a, BigInt b) {
        BigInt r = newBigInt();
        Check(NativeBN.BN_add(r.bignum, a.bignum, b.bignum));
        return r;
    }

    public void add(BigInt a) {
        Check(NativeBN.BN_add(this.bignum, this.bignum, a.bignum));
    }

    public static BigInt subtraction(BigInt a, BigInt b) {
        BigInt r = newBigInt();
        Check(NativeBN.BN_sub(r.bignum, a.bignum, b.bignum));
        return r;
    }


    public static BigInt gcd(BigInt a, BigInt b, Context t) {
        BigInt r = newBigInt();
        Check(NativeBN.BN_gcd(r.bignum, a.bignum, b.bignum, getCtx(t)));
        return r;
    }

    public static BigInt product(BigInt a, BigInt b, Context t) {
        BigInt r = newBigInt();
        Check(NativeBN.BN_mul(r.bignum, a.bignum, b.bignum, getCtx(t)));
        return r;
    }

    public void multiplyBy(BigInt a, Context t) {
        Check(NativeBN.BN_mul(this.bignum, this.bignum, a.bignum, getCtx(t)));
    }

    public static BigInt bigExp(BigInt a, BigInt p, Context t) {
        // Sign of p is ignored!
        BigInt r = newBigInt();
        Check(NativeBN.BN_exp(r.bignum, a.bignum, p.bignum, getCtx(t)));
        return r;
    }

    public static BigInt exp(BigInt a, int p, Context t) {
        // Sign of p is ignored!
        BigInt power = new BigInt();
        power.putLongInt(p);
        return bigExp(a, power, t);
        // OPTIONAL:
//      public int BN_sqr(BigInteger r, BigInteger a, BN_CTX ctx);
      // int BN_sqr(BIGNUM *r, const BIGNUM *a,BN_CTX *ctx);
    }

    public static void division(BigInt dividend, BigInt divisor, Context t,
            BigInt quotient, BigInt remainder) {
        int quot, rem;
        if (quotient != null) {
            quotient.makeValid();
            quot = quotient.bignum;
        }
        else quot = 0;
        if (remainder != null) {
            remainder.makeValid();
            rem = remainder.bignum;
        }
        else rem = 0;
        Check(NativeBN.BN_div(quot, rem, dividend.bignum, divisor.bignum, getCtx(t)));
    }

    public static BigInt modulus(BigInt a, BigInt m, Context t) {
        // Sign of p is ignored! ?
        BigInt r = newBigInt();
        Check(NativeBN.BN_nnmod(r.bignum, a.bignum, m.bignum, getCtx(t)));
        return r;
    }

    public static BigInt modExp(BigInt a, BigInt p, BigInt m, Context t) {
        // Sign of p is ignored!
        BigInt r = newBigInt();
        Check(NativeBN.BN_mod_exp(r.bignum, a.bignum, p.bignum, m.bignum, getCtx(t)));

        // OPTIONAL:
        // int BN_mod_sqr(BIGNUM *r, const BIGNUM *a, const BIGNUM *m, BN_CTX *ctx);
        return r;
    }


    public static BigInt modInverse(BigInt a, BigInt m, Context t) {
        BigInt r = newBigInt();
        Check(NativeBN.BN_mod_inverse(r.bignum, a.bignum, m.bignum, getCtx(t)));
        return r;
    }


    public static BigInt generatePrimeDefault(int bitLength, Random rnd, Context t) {
        BigInt r = newBigInt();
        Check(NativeBN.BN_generate_prime_ex(r.bignum, bitLength, false, 0, 0, 0));
        return r;
    }

    public boolean isPrime(int certainty, Random rnd, Context t) {
        return NativeBN.BN_is_prime_ex(bignum, certainty, getCtx(t), 0);
    }
}
