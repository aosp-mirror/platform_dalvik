package org.bouncycastle.asn1;

import java.io.IOException;

/**
 * A NULL object.
 */
public abstract class ASN1Null
    extends DERObject
{
    // BEGIN android-changed
    /*package*/ ASN1Null()
    {
    }
    // END android-changed

    public int hashCode()
    {
        return 0;
    }

    public boolean equals(
        Object o)
    {
        if ((o == null) || !(o instanceof ASN1Null))
        {
            return false;
        }
        
        return true;
    }

    abstract void encode(DEROutputStream out)
        throws IOException;

    public String toString()
    {
      return "NULL";
    }
}
