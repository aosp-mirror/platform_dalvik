package org.bouncycastle.asn1;

import java.io.IOException;

/**
 * A BER NULL object.
 */
public class BERNull
    extends DERNull
{
    // BEGIN android-added
    /** non-null; unique instance of this class */
    static public final BERNull THE_ONE = new BERNull();
    // END android-added

    // BEGIN android-changed
    private BERNull()
    {
    }
    // END android-changed

    void encode(
        DEROutputStream  out)
        throws IOException
    {
        if (out instanceof ASN1OutputStream || out instanceof BEROutputStream)
        {
            out.write(NULL);
        }
        else
        {
            super.encode(out);
        }
    }
}
