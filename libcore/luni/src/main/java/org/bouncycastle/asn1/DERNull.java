package org.bouncycastle.asn1;

import java.io.IOException;

/**
 * A NULL object.
 */
public class DERNull
    extends ASN1Null
{
    // BEGIN android-added
    /** non-null; unique instance of this class */
    static public final DERNull THE_ONE = new DERNull();
    // END android-added

    // BEGIN android-changed
    private static final byte[]  zeroBytes = new byte[0];

    /*package*/ DERNull()
    {
    }
    // END android-changed

    void encode(
        DEROutputStream  out)
        throws IOException
    {
        out.writeEncoded(NULL, zeroBytes);
    }
    
    public boolean equals(
        Object o)
    {
        if ((o == null) || !(o instanceof DERNull))
        {
            return false;
        }
        
        return true;
    }
    
    public int hashCode()
    {
        return 0;
    }
}
