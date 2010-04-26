package org.bouncycastle.x509;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AttCertIssuer;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.bouncycastle.asn1.x509.V2AttributeCertificateInfoGenerator;
import org.bouncycastle.asn1.x509.AttributeCertificateInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;

/**
 * class to produce an X.509 Version 2 AttributeCertificate.
 */
public class X509V2AttributeCertificateGenerator
{
    private V2AttributeCertificateInfoGenerator   acInfoGen;
    private DERObjectIdentifier         sigOID;
    private AlgorithmIdentifier         sigAlgId;
    private String                      signatureAlgorithm;
    private Hashtable                   extensions = null;
    private Vector                      extOrdering = null;

    public X509V2AttributeCertificateGenerator()
    {
        acInfoGen = new V2AttributeCertificateInfoGenerator();
    }

    /**
     * reset the generator
     */
    public void reset()
    {
        acInfoGen = new V2AttributeCertificateInfoGenerator();
        extensions = null;
        extOrdering = null;
    }

    /**
     * Set the Holder of this Attribute Certificate
     */
    public void setHolder(
        AttributeCertificateHolder     holder)
    {
        acInfoGen.setHolder(holder.holder);
    }

    /**
     * Set the issuer
     */
    public void setIssuer(
        AttributeCertificateIssuer  issuer)
    {
        acInfoGen.setIssuer(AttCertIssuer.getInstance(issuer.form));
    }

    /**
     * set the serial number for the certificate.
     */
    public void setSerialNumber(
        BigInteger      serialNumber)
    {
        acInfoGen.setSerialNumber(new DERInteger(serialNumber));
    }

    public void setNotBefore(
        Date    date)
    {
        acInfoGen.setStartDate(new DERGeneralizedTime(date));
    }

    public void setNotAfter(
        Date    date)
    {
        acInfoGen.setEndDate(new DERGeneralizedTime(date));
    }

    /**
     * Set the signature algorithm. This can be either a name or an OID, names
     * are treated as case insensitive.
     * 
     * @param signatureAlgorithm string representation of the algorithm name.
     */
    public void setSignatureAlgorithm(
        String  signatureAlgorithm)
    {
        this.signatureAlgorithm = signatureAlgorithm;

        try
        {
            sigOID = X509Util.getAlgorithmOID(signatureAlgorithm);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Unknown signature type requested");
        }

        // BEGIN android-changed
        sigAlgId = new AlgorithmIdentifier(this.sigOID, DERNull.THE_ONE);
        // END android-changed

        acInfoGen.setSignature(sigAlgId);
    }
    
    /**
     * add an attribute
     */
    public void addAttribute(
        X509Attribute       attribute)
    {
        acInfoGen.addAttribute(Attribute.getInstance(attribute.toASN1Object()));
    }

    public void setIssuerUniqueId(
        boolean[] iui)
    {
        // [TODO] convert boolean array to bit string
        //acInfoGen.setIssuerUniqueID(iui);
        throw new RuntimeException("not implemented (yet)");
    }
     
    /**
     * add a given extension field for the standard extensions tag
     * @throws IOException
     */
    public void addExtension(
        String          OID,
        boolean         critical,
        ASN1Encodable   value)
        throws IOException
    {
        this.addExtension(OID, critical, value.getEncoded());
    }

    /**
     * add a given extension field for the standard extensions tag
     * The value parameter becomes the contents of the octet string associated
     * with the extension.
     */
    public void addExtension(
        String          OID,
        boolean         critical,
        byte[]          value)
    {
        if (extensions == null)
        {
            extensions = new Hashtable();
            extOrdering = new Vector();
        }

        DERObjectIdentifier oid = new DERObjectIdentifier(OID);
        
        extensions.put(oid, new X509Extension(critical, new DEROctetString(value)));
        extOrdering.addElement(oid);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing.
     */
    public X509AttributeCertificate generateCertificate(
        PrivateKey      key,
        String          provider)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        return generateCertificate(key, provider, null);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing and the supplied source
     * of randomness, if required.
     */
    public X509AttributeCertificate generateCertificate(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        Signature sig = null;

        if (sigOID == null)
        {
            throw new IllegalStateException("no signature algorithm specified");
        }

        try
        {
            sig = Signature.getInstance(sigOID.getId(), provider);
        }
        catch (NoSuchAlgorithmException ex)
        {
            try
            {
                sig = Signature.getInstance(signatureAlgorithm, provider);
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new SecurityException("exception creating signature: " + e.toString());
            }
        }

        if (random != null)
        {
            sig.initSign(key, random);
        }
        else
        {
            sig.initSign(key);
        }

        if (extensions != null)
        {
            acInfoGen.setExtensions(new X509Extensions(extOrdering, extensions));
        }

        AttributeCertificateInfo acInfo = acInfoGen.generateAttributeCertificateInfo();

        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);

            dOut.writeObject(acInfo);

            sig.update(bOut.toByteArray());
        }
        catch (Exception e)
        {
            throw new SecurityException("exception encoding Attribute cert - " + e);
        }

        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(acInfo);
        v.add(sigAlgId);
        v.add(new DERBitString(sig.sign()));

        try
        {
            return new X509V2AttributeCertificate(new AttributeCertificate(new DERSequence(v)));
        }
        catch (IOException e)
        {
            throw new RuntimeException("constructed invalid certificate!");
        }
    }
    
    /**
     * Return an iterator of the signature names supported by the generator.
     * 
     * @return an iterator containing recognised names.
     */
    public Iterator getSignatureAlgNames()
    {
        return X509Util.getAlgNames();
    }
}
