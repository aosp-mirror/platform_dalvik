package org.bouncycastle.crypto.params;

public class DHValidationParameters
{
    private byte[]  seed;
    private int     counter;

    public DHValidationParameters(
        byte[]  seed,
        int     counter)
    {
        this.seed = seed;
        this.counter = counter;
    }

    public boolean equals(
        Object o)
    {
        if (!(o instanceof DHValidationParameters))
        {
            return false;
        }

        DHValidationParameters  other = (DHValidationParameters)o;

        if (other.counter != this.counter)
        {
            return false;
        }

        if (other.seed.length != this.seed.length)
        {
            return false;
        }

        for (int i = 0; i != other.seed.length; i++)
        {
            if (other.seed[i] != this.seed[i])
            {
                return false;
            }
        }

        return true;
    }
    
    public int hashCode()
    {
        int code = counter;
        
        for (int i = 0; i != seed.length; i++)
        {
            code ^= (seed[i] & 0xff) << (i % 4);
        }
        
        return code;
    }
}
