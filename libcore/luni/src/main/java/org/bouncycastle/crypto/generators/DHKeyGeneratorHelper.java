package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import java.security.SecureRandom;

class DHKeyGeneratorHelper
{
    private static final int MAX_ITERATIONS = 1000;

    static final DHKeyGeneratorHelper INSTANCE = new DHKeyGeneratorHelper();
    
    private static BigInteger ZERO = BigInteger.valueOf(0);
    private static BigInteger TWO = BigInteger.valueOf(2);
    
    private DHKeyGeneratorHelper()
    {
    }
    
    BigInteger calculatePrivate(BigInteger p, SecureRandom random, int limit)
    {
        //
        // calculate the private key
        //
        BigInteger pSub2 = p.subtract(TWO);
        BigInteger x;
        
        if (limit == 0)
        {
            x = createInRange(pSub2, random);
        }
        else
        {
            do
            {
                x = new BigInteger(limit, random);
            }
            while (x.equals(ZERO));
        }
        
        return x;
    }

    private BigInteger createInRange(BigInteger max, SecureRandom random)
    {
        BigInteger x;
        int maxLength = max.bitLength();
        int count = 0;
        
        do
        {
            x = new BigInteger(maxLength, random);
            count++;
        }
        while ((x.equals(ZERO) || x.compareTo(max) > 0) && count != MAX_ITERATIONS);
        
        if (count == MAX_ITERATIONS)  // fall back to a faster (restricted) method
        {
            return new BigInteger(maxLength - 1, random).setBit(0);
        }
        
        return x;
    }
    
    BigInteger calculatePublic(BigInteger p, BigInteger g, BigInteger x)
    {
        return g.modPow(x, p);
    }
}
