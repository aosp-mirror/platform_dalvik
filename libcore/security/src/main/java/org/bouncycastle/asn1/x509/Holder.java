package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * The Holder object.
 * <pre>
 *  Holder ::= SEQUENCE {
 *        baseCertificateID   [0] IssuerSerial OPTIONAL,
 *                 -- the issuer and serial number of
 *                 -- the holder's Public Key Certificate
 *        entityName          [1] GeneralNames OPTIONAL,
 *                 -- the name of the claimant or role
 *        objectDigestInfo    [2] ObjectDigestInfo OPTIONAL
 *                 -- used to directly authenticate the holder,
 *                 -- for example, an executable
 *  }
 * </pre>
 */
public class Holder
    extends ASN1Encodable
{
    IssuerSerial        baseCertificateID;
    GeneralNames        entityName;
    ObjectDigestInfo    objectDigestInfo;

    public static Holder getInstance(
        Object  obj)
    {
        if (obj instanceof Holder)
        {
            return (Holder)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new Holder((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    public Holder(
        ASN1Sequence    seq)
    {
        if (seq.size() > 3)
        {
            throw new IllegalArgumentException("Bad sequence size: "
                    + seq.size());
        }

        for (int i = 0; i != seq.size(); i++)
        {
            ASN1TaggedObject    tObj = ASN1TaggedObject.getInstance(seq.getObjectAt(i));
            
            switch (tObj.getTagNo())
            {
            case 0:
                baseCertificateID = IssuerSerial.getInstance(tObj, false);
                break;
            case 1:
                entityName = GeneralNames.getInstance(tObj, false);
                break;
            case 2:
                objectDigestInfo = ObjectDigestInfo.getInstance(tObj, false);
                break;
            default:
                throw new IllegalArgumentException("unknown tag in Holder");
            }
        }
    }
    
    public Holder(
        IssuerSerial    baseCertificateID)
    {
        this.baseCertificateID = baseCertificateID;
    }
    
    public Holder(
        GeneralNames    entityName)
    {
        this.entityName = entityName;
    }
    
    public IssuerSerial getBaseCertificateID()
    {
        return baseCertificateID;
    }
    
    public GeneralNames getEntityName()
    {
        return entityName;
    }
    
    public ObjectDigestInfo getObjectDigestInfo()
    {
        return objectDigestInfo;
    }
    
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        if (baseCertificateID != null)
        {
            v.add(new DERTaggedObject(false, 0, baseCertificateID));
        }

        if (entityName != null)
        {
            v.add(new DERTaggedObject(false, 1, entityName));
        }

        if (objectDigestInfo != null)
        {
            v.add(new DERTaggedObject(false, 2, objectDigestInfo));
        }

        return new DERSequence(v);
    }
}
