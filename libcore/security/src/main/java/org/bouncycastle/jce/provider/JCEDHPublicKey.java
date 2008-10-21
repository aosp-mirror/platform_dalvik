package org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;

public class JCEDHPublicKey
    implements DHPublicKey
{
    private BigInteger              y;
    private DHParameterSpec         dhSpec;

    JCEDHPublicKey(
        DHPublicKeySpec    spec)
    {
        this.y = spec.getY();
        this.dhSpec = new DHParameterSpec(spec.getP(), spec.getG());
    }

    JCEDHPublicKey(
        DHPublicKey    key)
    {
        this.y = key.getY();
        this.dhSpec = key.getParams();
    }

    JCEDHPublicKey(
        DHPublicKeyParameters  params)
    {
        this.y = params.getY();
        this.dhSpec = new DHParameterSpec(params.getParameters().getP(), params.getParameters().getG(), 0);
    }

    JCEDHPublicKey(
        BigInteger        y,
        DHParameterSpec   dhSpec)
    {
        this.y = y;
        this.dhSpec = dhSpec;
    }

    JCEDHPublicKey(
        SubjectPublicKeyInfo    info)
    {
        DHParameter             params = new DHParameter((ASN1Sequence)info.getAlgorithmId().getParameters());
        DERInteger              derY = null;

        try
        {
            derY = (DERInteger)info.getPublicKey();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("invalid info structure in DH public key");
        }

        this.y = derY.getValue();
        if (params.getL() != null)
        {
            this.dhSpec = new DHParameterSpec(params.getP(), params.getG(), params.getL().intValue());
        }
        else
        {
            this.dhSpec = new DHParameterSpec(params.getP(), params.getG());
        }
    }

    public String getAlgorithm()
    {
        return "DH";
    }

    public String getFormat()
    {
        return "X.509";
    }

    public byte[] getEncoded()
    {
        SubjectPublicKeyInfo    info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.dhpublicnumber, new DHParameter(dhSpec.getP(), dhSpec.getG(), dhSpec.getL()).getDERObject()), new DERInteger(y));

        return info.getDEREncoded();
    }

    public DHParameterSpec getParams()
    {
        return dhSpec;
    }

    public BigInteger getY()
    {
        return y;
    }

    private void readObject(
        ObjectInputStream   in)
        throws IOException, ClassNotFoundException
    {
        this.y = (BigInteger)in.readObject();
        this.dhSpec = new DHParameterSpec((BigInteger)in.readObject(), (BigInteger)in.readObject(), in.readInt());
    }

    private void writeObject(
        ObjectOutputStream  out)
        throws IOException
    {
        out.writeObject(this.getY());
        out.writeObject(dhSpec.getP());
        out.writeObject(dhSpec.getG());
        out.writeInt(dhSpec.getL());
    }
}
