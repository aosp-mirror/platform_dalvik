package org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Base class for an application specific object
 */
public class DERApplicationSpecific 
    extends DERObject
{
    private int       tag;
    private byte[]    octets;
    
    public DERApplicationSpecific(
        int        tag,
        byte[]    octets)
    {
        this.tag = tag;
        this.octets = octets;
    }
    
    public DERApplicationSpecific(
        int                  tag, 
        DEREncodable         object) 
        throws IOException 
    {
        this.tag = tag | DERTags.CONSTRUCTED;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DEROutputStream dos = new DEROutputStream(baos);
        
        dos.writeObject(object);
        
        this.octets = baos.toByteArray();
    }
    
    public boolean isConstructed()
    {
        return (tag & DERTags.CONSTRUCTED) != 0;
    }
    
    public byte[] getContents()
    {
        return octets;
    }
    
    public int getApplicationTag() 
    {
        return tag & 0x1F;
    }
     
    public DERObject getObject() 
        throws IOException 
    {
        return new ASN1InputStream(getContents()).readObject();
    }
    
    /* (non-Javadoc)
     * @see org.bouncycastle.asn1.DERObject#encode(org.bouncycastle.asn1.DEROutputStream)
     */
    void encode(DEROutputStream out) throws IOException
    {
        out.writeEncoded(DERTags.APPLICATION | tag, octets);
    }
    
    public boolean equals(
            Object o)
    {
        if ((o == null) || !(o instanceof DERApplicationSpecific))
        {
            return false;
        }
        
        DERApplicationSpecific other = (DERApplicationSpecific)o;
        
        if (tag != other.tag)
        {
            return false;
        }
        
        if (octets.length != other.octets.length)
        {
            return false;
        }
        
        for (int i = 0; i < octets.length; i++) 
        {
            if (octets[i] != other.octets[i])
            {
                return false;
            }
        }
        
        return true;
    }
    
    public int hashCode()
    {
        byte[]  b = this.getContents();
        int     value = 0;

        for (int i = 0; i != b.length; i++)
        {
            value ^= (b[i] & 0xff) << (i % 4);
        }

        return value ^ this.getApplicationTag();
    }
}
