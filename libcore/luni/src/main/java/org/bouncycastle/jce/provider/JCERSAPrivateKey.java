package org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;

public class JCERSAPrivateKey
    implements RSAPrivateKey, PKCS12BagAttributeCarrier
{
    protected BigInteger modulus;
    protected BigInteger privateExponent;

    private Hashtable   pkcs12Attributes = new Hashtable();
    private Vector      pkcs12Ordering = new Vector();

    protected JCERSAPrivateKey()
    {
    }

    JCERSAPrivateKey(
        RSAKeyParameters key)
    {
        this.modulus = key.getModulus();
        this.privateExponent = key.getExponent();
    }

    JCERSAPrivateKey(
        RSAPrivateKeySpec spec)
    {
        this.modulus = spec.getModulus();
        this.privateExponent = spec.getPrivateExponent();
    }

    JCERSAPrivateKey(
        RSAPrivateKey key)
    {
        this.modulus = key.getModulus();
        this.privateExponent = key.getPrivateExponent();
    }

    public BigInteger getModulus()
    {
        return modulus;
    }

    public BigInteger getPrivateExponent()
    {
        return privateExponent;
    }

    public String getAlgorithm()
    {
        return "RSA";
    }

    public String getFormat()
    {
        return "NULL";
    }

    public byte[] getEncoded()
    {
        return null;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof RSAPrivateKey))
        {
            return false;
        }

        if (o == this)
        {
            return true;
        }

        RSAPrivateKey key = (RSAPrivateKey)o;

        return getModulus().equals(key.getModulus())
            && getPrivateExponent().equals(key.getPrivateExponent());
    }

    public int hashCode()
    {
        return getModulus().hashCode() ^ getPrivateExponent().hashCode();
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

    private void readObject(
        ObjectInputStream   in)
        throws IOException, ClassNotFoundException
    {
        this.modulus = (BigInteger)in.readObject();

        Object  obj = in.readObject();

        if (obj instanceof Hashtable)
        {
            this.pkcs12Attributes = (Hashtable)obj;
            this.pkcs12Ordering = (Vector)in.readObject();
        }
        else
        {
            this.pkcs12Attributes = new Hashtable();
            this.pkcs12Ordering = new Vector();

            ASN1InputStream         aIn = new ASN1InputStream((byte[])obj);

            DERObjectIdentifier    oid;

            while ((oid = (DERObjectIdentifier)aIn.readObject()) != null)
            {
                this.setBagAttribute(oid, aIn.readObject());
            }
        }

        this.privateExponent = (BigInteger)in.readObject();
    }

    private void writeObject(
        ObjectOutputStream  out)
        throws IOException
    {
        out.writeObject(modulus);

        if (pkcs12Ordering.size() == 0)
        {
            out.writeObject(pkcs12Attributes);
            out.writeObject(pkcs12Ordering);
        }
        else
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);

            Enumeration             e = this.getBagAttributeKeys();

            while (e.hasMoreElements())
            {
                DEREncodable    oid = (DEREncodable)e.nextElement();

                aOut.writeObject(oid);
                aOut.writeObject(pkcs12Attributes.get(oid));
            }

            out.writeObject(bOut.toByteArray());
        }

        out.writeObject(privateExponent);
    }
}
