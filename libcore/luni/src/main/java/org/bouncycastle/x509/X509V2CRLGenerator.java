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
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.asn1.x509.TBSCertList;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.asn1.x509.V2TBSCertListGenerator;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.X509CRLObject;

/**
 * class to produce an X.509 Version 2 CRL.
 */
public class X509V2CRLGenerator
{
    private SimpleDateFormat            dateF = new SimpleDateFormat("yyMMddHHmmss");
    private SimpleTimeZone              tz = new SimpleTimeZone(0, "Z");
    private V2TBSCertListGenerator      tbsGen;
    private DERObjectIdentifier         sigOID;
    private AlgorithmIdentifier         sigAlgId;
    private String                      signatureAlgorithm;
    private Hashtable                   extensions = null;
    private Vector                      extOrdering = null;

    public X509V2CRLGenerator()
    {
        dateF.setTimeZone(tz);

        tbsGen = new V2TBSCertListGenerator();
    }

    /**
     * reset the generator
     */
    public void reset()
    {
        tbsGen = new V2TBSCertListGenerator();
    }

    /**
     * Set the issuer distinguished name - the issuer is the entity whose private key is used to sign the
     * certificate.
     */
    public void setIssuerDN(
        X500Principal   issuer)
    {
        try
        {
            tbsGen.setIssuer(new X509Principal(issuer.getEncoded()));
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("can't process principal: " + e);
        }
    }

    /**
     * Set the issuer distinguished name - the issuer is the entity whose private key is used to sign the
     * certificate.
     */
    public void setIssuerDN(
        X509Name   issuer)
    {
        tbsGen.setIssuer(issuer);
    }

    public void setThisUpdate(
        Date    date)
    {
        tbsGen.setThisUpdate(new Time(date));
    }

    public void setNextUpdate(
        Date    date)
    {
        tbsGen.setNextUpdate(new Time(date));
    }

    /**
     * Reason being as indicated by ReasonFlags, i.e. ReasonFlags.keyCompromise
     * or 0 if ReasonFlags are not to be used
     **/
    public void addCRLEntry(BigInteger userCertificate, Date revocationDate, int reason)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificate), new Time(revocationDate), reason);
    }

    /**
     * Add a CRL entry with an Invalidity Date extension as well as a CRLReason extension.
     * Reason being as indicated by ReasonFlags, i.e. ReasonFlags.keyCompromise
     * or 0 if ReasonFlags are not to be used
     **/
    public void addCRLEntry(BigInteger userCertificate, Date revocationDate, int reason, Date invalidityDate)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificate), new Time(revocationDate), reason, new DERGeneralizedTime(invalidityDate));
    }
   
    /**
     * Add a CRL entry with extensions.
     **/
    public void addCRLEntry(BigInteger userCertificate, Date revocationDate, X509Extensions extensions)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificate), new Time(revocationDate), extensions);
    }
    
    /**
     * Add the CRLEntry objects contained in a previous CRL.
     * 
     * @param other the X509CRL to source the other entries from. 
     */
    public void addCRL(X509CRL other)
        throws CRLException
    {
        Set revocations = other.getRevokedCertificates();
        
        Iterator it = revocations.iterator();
        while (it.hasNext())
        {
            X509CRLEntry entry = (X509CRLEntry)it.next();
            
            ASN1InputStream aIn = new ASN1InputStream(entry.getEncoded());
            
            try
            {
                tbsGen.addCRLEntry(ASN1Sequence.getInstance(aIn.readObject()));
            }
            catch (IOException e)
            {
                throw new CRLException("exception processing encoding of CRL: " + e.toString());
            }
        }
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

        sigAlgId = X509Util.getSigAlgID(sigOID);

        tbsGen.setSignature(sigAlgId);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        String          OID,
        boolean         critical,
        DEREncodable    value)
    {
        this.addExtension(new DERObjectIdentifier(OID), critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        DERObjectIdentifier OID,
        boolean             critical,
        DEREncodable        value)
    {
        if (extensions == null)
        {
            extensions = new Hashtable();
            extOrdering = new Vector();
        }

        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);

        try
        {
            dOut.writeObject(value);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("error encoding value: " + e);
        }

        this.addExtension(OID, critical, bOut.toByteArray());
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        String          OID,
        boolean         critical,
        byte[]          value)
    {
        this.addExtension(new DERObjectIdentifier(OID), critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        DERObjectIdentifier OID,
        boolean             critical,
        byte[]              value)
    {
        if (extensions == null)
        {
            extensions = new Hashtable();
            extOrdering = new Vector();
        }

        extensions.put(OID, new X509Extension(critical, new DEROctetString(value)));
        extOrdering.addElement(OID);
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject
     * using the default provider "BC".
     */
    public X509CRL generateX509CRL(
        PrivateKey      key)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509CRL(key, "BC", null);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject
     * using the default provider "BC" and an user defined SecureRandom object as
     * source of randomness.
     */
    public X509CRL generateX509CRL(
        PrivateKey      key,
        SecureRandom    random)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509CRL(key, "BC", random);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject
     * using the passed in provider for the signing.
     */
    public X509CRL generateX509CRL(
        PrivateKey      key,
        String          provider)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        return generateX509CRL(key, provider, null);
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject,
     * using the passed in provider for the signing.
     */
    public X509CRL generateX509CRL(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        Signature sig = null;

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
            tbsGen.setExtensions(new X509Extensions(extOrdering, extensions));
        }

        TBSCertList tbsCrl = tbsGen.generateTBSCertList();

        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);

            dOut.writeObject(tbsCrl);

            sig.update(bOut.toByteArray());
        }
        catch (Exception e)
        {
            throw new SecurityException("exception encoding TBS cert - " + e);
        }

        // Construct the CRL
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(tbsCrl);
        v.add(sigAlgId);
        v.add(new DERBitString(sig.sign()));

        try
        {
            return new X509CRLObject(new CertificateList(new DERSequence(v)));
        }
        catch (CRLException e)
        {
            throw new SecurityException("exception creating CRL: " + e.getMessage());
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
