package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERTaggedObject;

public class RecipientInfo
    extends ASN1Encodable
{
    DEREncodable    info;

    public RecipientInfo(
        KeyTransRecipientInfo info)
    {
        this.info = info;
    }

    public RecipientInfo(
        KeyAgreeRecipientInfo info)
    {
        this.info = new DERTaggedObject(true, 1, info);
    }

    public RecipientInfo(
        KEKRecipientInfo info)
    {
        this.info = new DERTaggedObject(true, 2, info);
    }

    public RecipientInfo(
        PasswordRecipientInfo info)
    {
        this.info = new DERTaggedObject(true, 3, info);
    }

    public RecipientInfo(
        OtherRecipientInfo info)
    {
        this.info = new DERTaggedObject(true, 4, info);
    }

    public RecipientInfo(
        DERObject   info)
    {
        this.info = info;
    }

    public static RecipientInfo getInstance(
        Object  o)
    {
        if (o == null || o instanceof RecipientInfo)
        {
            return (RecipientInfo)o;
        }
        else if (o instanceof ASN1Sequence)
        {
            return new RecipientInfo((ASN1Sequence)o);
        }
        else if (o instanceof ASN1TaggedObject)
        {
            return new RecipientInfo((ASN1TaggedObject)o);
        }

        throw new IllegalArgumentException("unknown object in factory: "
                                                    + o.getClass().getName());
    }

    public DERInteger getVersion()
    {
        if (info instanceof ASN1TaggedObject)
        {
            ASN1TaggedObject o = (ASN1TaggedObject)info;

            switch (o.getTagNo())
            {
            case 1:
                return KeyAgreeRecipientInfo.getInstance(o, true).getVersion();
            case 2:
                return KEKRecipientInfo.getInstance(o, true).getVersion();
            case 3:
                return PasswordRecipientInfo.getInstance(o, true).getVersion();
            case 4:
                return new DERInteger(0);    // no syntax version for OtherRecipientInfo
            default:
                throw new IllegalStateException("unknown tag");
            }
        }

        return KeyTransRecipientInfo.getInstance(info).getVersion();
    }

    public boolean isTagged()
    {
        return (info instanceof ASN1TaggedObject);
    }

    public DEREncodable getInfo()
    {
        if (info instanceof ASN1TaggedObject)
        {
            ASN1TaggedObject o = (ASN1TaggedObject)info;

            switch (o.getTagNo())
            {
            case 1:
                return KeyAgreeRecipientInfo.getInstance(o, true);
            case 2:
                return KEKRecipientInfo.getInstance(o, true);
            case 3:
                return PasswordRecipientInfo.getInstance(o, true);
            case 4:
                return OtherRecipientInfo.getInstance(o, true);
            default:
                throw new IllegalStateException("unknown tag");
            }
        }

        return KeyTransRecipientInfo.getInstance(info);
    }

    /** 
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     * RecipientInfo ::= CHOICE {
     *     ktri KeyTransRecipientInfo,
     *     kari [1] KeyAgreeRecipientInfo,
     *     kekri [2] KEKRecipientInfo,
     *     pwri [3] PasswordRecipientInfo,
     *     ori [4] OtherRecipientInfo }
     * </pre>
     */
    public DERObject toASN1Object()
    {
        return info.getDERObject();
    }
}
