package org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPrivateKeySpec;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;

public class JCEDHPrivateKey
    implements DHPrivateKey, PKCS12BagAttributeCarrier
{
    BigInteger      x;

    DHParameterSpec dhSpec;

    private Hashtable   pkcs12Attributes = new Hashtable();
    private Vector      pkcs12Ordering = new Vector();

    protected JCEDHPrivateKey()
    {
    }

    JCEDHPrivateKey(
        DHPrivateKey    key)
    {
        this.x = key.getX();
        this.dhSpec = key.getParams();
    }

    JCEDHPrivateKey(
        DHPrivateKeySpec    spec)
    {
        this.x = spec.getX();
        this.dhSpec = new DHParameterSpec(spec.getP(), spec.getG());
    }

    JCEDHPrivateKey(
        PrivateKeyInfo  info)
    {
        DHParameter     params = new DHParameter((ASN1Sequence)info.getAlgorithmId().getParameters());
        DERInteger      derX = (DERInteger)info.getPrivateKey();

        this.x = derX.getValue();
        if (params.getL() != null)
        {
            this.dhSpec = new DHParameterSpec(params.getP(), params.getG(), params.getL().intValue());
        }
        else
        {
            this.dhSpec = new DHParameterSpec(params.getP(), params.getG());
        }
    }

    JCEDHPrivateKey(
        DHPrivateKeyParameters  params)
    {
        this.x = params.getX();
        this.dhSpec = new DHParameterSpec(params.getParameters().getP(), params.getParameters().getG());
    }

    public String getAlgorithm()
    {
        return "DH";
    }

    /**
     * return the encoding format we produce in getEncoded().
     *
     * @return the string "PKCS#8"
     */
    public String getFormat()
    {
        return "PKCS#8";
    }

    /**
     * Return a PKCS8 representation of the key. The sequence returned
     * represents a full PrivateKeyInfo object.
     *
     * @return a PKCS8 representation of the key.
     */
    public byte[] getEncoded()
    {
        PrivateKeyInfo          info = new PrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.dhKeyAgreement, new DHParameter(dhSpec.getP(), dhSpec.getG(), dhSpec.getL()).getDERObject()), new DERInteger(getX()));

        return info.getDEREncoded();
    }

    public DHParameterSpec getParams()
    {
        return dhSpec;
    }

    public BigInteger getX()
    {
        return x;
    }

    private void readObject(
        ObjectInputStream   in)
        throws IOException, ClassNotFoundException
    {
        x = (BigInteger)in.readObject();

        this.dhSpec = new DHParameterSpec((BigInteger)in.readObject(), (BigInteger)in.readObject(), in.readInt());
    }

    private void writeObject(
        ObjectOutputStream  out)
        throws IOException
    {
        out.writeObject(this.getX());
        out.writeObject(dhSpec.getP());
        out.writeObject(dhSpec.getG());
        out.writeInt(dhSpec.getL());
    }

    public void setBagAttribute(
        DERObjectIdentifier oid,
        DEREncodable        attribute)
    {
        pkcs12Attributes.put(oid, attribute);
        pkcs12Ordering.addElement(oid);
    }

    public DEREncodable getBagAttribute(
        DERObjectIdentifier oid)
    {
        return (DEREncodable)pkcs12Attributes.get(oid);
    }

    public Enumeration getBagAttributeKeys()
    {
        return pkcs12Ordering.elements();
    }
}
