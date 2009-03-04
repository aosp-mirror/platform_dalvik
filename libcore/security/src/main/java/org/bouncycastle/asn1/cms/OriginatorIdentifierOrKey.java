package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERTaggedObject;

public class OriginatorIdentifierOrKey
    extends ASN1Encodable
{
    private DEREncodable id;
    
    public OriginatorIdentifierOrKey(
        IssuerAndSerialNumber id)
    {
        this.id = id;
    }
    
    public OriginatorIdentifierOrKey(
        ASN1OctetString id)
    {
        this.id = new DERTaggedObject(false, 0, id);
    }
    
    public OriginatorIdentifierOrKey(
        OriginatorPublicKey id)
    {
        this.id = new DERTaggedObject(false, 1, id);
    }
    
    public OriginatorIdentifierOrKey(
        DERObject id)
    {
        this.id = id;
    }
    
    /**
     * return an OriginatorIdentifierOrKey object from a tagged object.
     *
     * @param o the tagged object holding the object we want.
     * @param explicit true if the object is meant to be explicitly
     *              tagged false otherwise.
     * @exception IllegalArgumentException if the object held by the
     *          tagged object cannot be converted.
     */
    public static OriginatorIdentifierOrKey getInstance(
        ASN1TaggedObject    o,
        boolean             explicit)
    {
        if (!explicit)
        {
            throw new IllegalArgumentException(
                    "Can't implicitly tag OriginatorIdentifierOrKey");
        }

        return getInstance(o.getObject());
    }
    
    /**
     * return an OriginatorIdentifierOrKey object from the given object.
     *
     * @param o the object we want converted.
     * @exception IllegalArgumentException if the object cannot be converted.
     */
    public static OriginatorIdentifierOrKey getInstance(
        Object o)
    {
        if (o == null || o instanceof OriginatorIdentifierOrKey)
        {
            return (OriginatorIdentifierOrKey)o;
        }
        
        if (o instanceof DERObject)
        {
            return new OriginatorIdentifierOrKey((DERObject)o);
        }
        
        throw new IllegalArgumentException("Invalid OriginatorIdentifierOrKey: " + o.getClass().getName());
    } 

    public DEREncodable getId()
    {
        return id;
    }
    
    /** 
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     * OriginatorIdentifierOrKey ::= CHOICE {
     *     issuerAndSerialNumber IssuerAndSerialNumber,
     *     subjectKeyIdentifier [0] SubjectKeyIdentifier,
     *     originatorKey [1] OriginatorPublicKey 
     * }
     *
     * SubjectKeyIdentifier ::= OCTET STRING
     * </pre>
     */
    public DERObject toASN1Object()
    {
        return id.getDERObject();
    }
}
