package org.bouncycastle.jce;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.IssuerAndSerialNumber;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.SignedData;
import org.bouncycastle.asn1.pkcs.SignerInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.X509CRLObject;
import org.bouncycastle.jce.provider.X509CertificateObject;

/**
 * Represents a PKCS#7 object - specifically the "Signed Data"
 * type.
 * <p>
 * How to use it? To verify a signature, do:
 * <pre>
 * PKCS7SignedData pkcs7 = new PKCS7SignedData(der_bytes);        // Create it
 * pkcs7.update(bytes, 0, bytes.length);                          // Update checksum
 * boolean verified = pkcs7.verify();                             // Does it add up?
 *
 * To sign, do this:
 * PKCS7SignedData pkcs7 = new PKCS7SignedData(privKey, certChain, "MD5");
 * pkcs7.update(bytes, 0, bytes.length);                          // Update checksum
 * pkcs7.sign();                                                  // Create digest
 *
 * bytes = pkcs7.getEncoded();                                    // Write it somewhere
 * </pre>
 * <p>
 * This class is pretty close to obsolete, for a much better (and more complete)
 * implementation of PKCS7 have a look at the org.bouncycastle.cms package.
 * @deprecated this class really is obsolete - use the CMS package.
 */
public class PKCS7SignedData
    implements PKCSObjectIdentifiers
{
    private int version, signerversion;
    private Set digestalgos;
    private Collection certs, crls;
    private X509Certificate signCert;
    private byte[] digest;
    private String digestAlgorithm, digestEncryptionAlgorithm;
    private Signature sig;
    private transient PrivateKey privKey;

    private final String ID_PKCS7_DATA = "1.2.840.113549.1.7.1";
    private final String ID_PKCS7_SIGNED_DATA = "1.2.840.113549.1.7.2";
    private final String ID_MD5 = "1.2.840.113549.2.5";
    private final String ID_MD2 = "1.2.840.113549.2.2";
    private final String ID_SHA1 = "1.3.14.3.2.26";
    private final String ID_RSA = "1.2.840.113549.1.1.1";
    private final String ID_DSA = "1.2.840.10040.4.1";

    /**
     * Read an existing PKCS#7 object from a DER encoded byte array using
     * the BC provider.
     */
    public PKCS7SignedData(
        byte[]  in)
        throws SecurityException, CRLException, InvalidKeyException,
        NoSuchProviderException, NoSuchAlgorithmException
    {
        this(in, "BC");
    }

    /**
     * Read an existing PKCS#7 object from a DER encoded byte array 
     */
    public PKCS7SignedData(
        byte[]  in,
        String  provider)
        throws SecurityException, CRLException, InvalidKeyException,
        NoSuchProviderException, NoSuchAlgorithmException
    {
        ASN1InputStream din = new ASN1InputStream(new ByteArrayInputStream(in));

        //
        // Basic checks to make sure it's a PKCS#7 SignedData Object
        //
        DERObject pkcs;

        try
        {
            pkcs = din.readObject();
        }
        catch (IOException e)
        {
            throw new SecurityException("can't decode PKCS7SignedData object");
        }

        if (!(pkcs instanceof ASN1Sequence))
        {
            throw new SecurityException("Not a valid PKCS#7 object - not a sequence");
        }

        ContentInfo content = ContentInfo.getInstance(pkcs);

        if (!content.getContentType().equals(signedData))
        {
            throw new SecurityException("Not a valid PKCS#7 signed-data object - wrong header " + content.getContentType().getId());
        }


        SignedData  data = SignedData.getInstance(content.getContent());

        certs = new ArrayList();

        if (data.getCertificates() != null)
        {
            Enumeration ec = ASN1Set.getInstance(data.getCertificates()).getObjects();

            while (ec.hasMoreElements())
            {
                certs.add(new X509CertificateObject(X509CertificateStructure.getInstance(ec.nextElement())));
            }
        }

        crls = new ArrayList();

        if (data.getCRLs() != null)
        {
            Enumeration ec = ASN1Set.getInstance(data.getCRLs()).getObjects();
            while (ec.hasMoreElements())
            {
                crls.add(new X509CRLObject(CertificateList.getInstance(ec.nextElement())));
            }
        }

        version = data.getVersion().getValue().intValue();

        //
        // Get the digest algorithm
        //
        digestalgos = new HashSet();
        Enumeration e = data.getDigestAlgorithms().getObjects();

        while (e.hasMoreElements())
        {
            ASN1Sequence s = (ASN1Sequence)e.nextElement();
            DERObjectIdentifier o = (DERObjectIdentifier)s.getObjectAt(0);
            digestalgos.add(o.getId());
        }

        //
        // Get the SignerInfo
        //
        ASN1Set signerinfos = data.getSignerInfos();
        if (signerinfos.size() != 1)
        {
            throw new SecurityException("This PKCS#7 object has multiple SignerInfos - only one is supported at this time");
        }

        SignerInfo signerInfo = SignerInfo.getInstance(signerinfos.getObjectAt(0));

        signerversion = signerInfo.getVersion().getValue().intValue();

        IssuerAndSerialNumber isAnds = signerInfo.getIssuerAndSerialNumber();

        //
        // Get the signing certificate
        //
        BigInteger      serialNumber = isAnds.getCertificateSerialNumber().getValue();
        X509Principal   issuer = new X509Principal(isAnds.getName());

        for (Iterator i = certs.iterator();i.hasNext();)
        {
            X509Certificate cert = (X509Certificate)i.next();
            if (serialNumber.equals(cert.getSerialNumber())
                    && issuer.equals(cert.getIssuerDN()))
            {
                signCert = cert;
                break;
            }
        }

        if (signCert == null)
        {
            throw new SecurityException("Can't find signing certificate with serial "+serialNumber.toString(16)); 
        }

        digestAlgorithm = signerInfo.getDigestAlgorithm().getObjectId().getId();

        digest = signerInfo.getEncryptedDigest().getOctets();
        digestEncryptionAlgorithm = signerInfo.getDigestEncryptionAlgorithm().getObjectId().getId();

        sig = Signature.getInstance(getDigestAlgorithm(), provider);

        sig.initVerify(signCert.getPublicKey());
    }

    /**
     * Create a new PKCS#7 object from the specified key using the BC provider.
     *
     * @param privKey the private key to be used for signing.
     * @param certChain the certificate chain associated with the private key.
     * @param hashAlgorithm the hashing algorithm used to compute the message digest. Must be "MD5", "MD2", "SHA1" or "SHA"
     */
    public PKCS7SignedData(
        PrivateKey      privKey,
        Certificate[]   certChain,
        String          hashAlgorithm)
        throws SecurityException, InvalidKeyException,
        NoSuchProviderException, NoSuchAlgorithmException
    {
        this(privKey, certChain, hashAlgorithm, "BC");
    }

    /**
     * Create a new PKCS#7 object from the specified key.
     *
     * @param privKey the private key to be used for signing.
     * @param certChain the certificate chain associated with the private key.
     * @param hashAlgorithm the hashing algorithm used to compute the message digest. Must be "MD5", "MD2", "SHA1" or "SHA"
     * @param provider the provider to use.
     */
    public PKCS7SignedData(
        PrivateKey      privKey,
        Certificate[]   certChain,
        String          hashAlgorithm,
        String          provider)
        throws SecurityException, InvalidKeyException,
        NoSuchProviderException, NoSuchAlgorithmException
    {
        this(privKey, certChain, null, hashAlgorithm, provider);
    }

    /**
     * Create a new PKCS#7 object from the specified key.
     *
     * @param privKey the private key to be used for signing.
     * @param certChain the certificate chain associated with the private key.
     * @param crlList the crl list associated with the private key.
     * @param hashAlgorithm the hashing algorithm used to compute the message digest. Must be "MD5", "MD2", "SHA1" or "SHA"
     * @param provider the provider to use.
     */
    public PKCS7SignedData(
        PrivateKey      privKey,
        Certificate[]   certChain,
        CRL[]           crlList,
        String          hashAlgorithm,
        String          provider)
        throws SecurityException, InvalidKeyException,
        NoSuchProviderException, NoSuchAlgorithmException
    {
        this.privKey = privKey;

        if (hashAlgorithm.equals("MD5"))
        {
            digestAlgorithm = ID_MD5;
        }
        else if (hashAlgorithm.equals("MD2"))
        {
            digestAlgorithm = ID_MD2;
        }
        else if (hashAlgorithm.equals("SHA"))
        {
            digestAlgorithm = ID_SHA1;
        }
        else if (hashAlgorithm.equals("SHA1"))
        {
            digestAlgorithm = ID_SHA1;
        }
        else
        {
            throw new NoSuchAlgorithmException("Unknown Hash Algorithm "+hashAlgorithm);
        }

        version = signerversion = 1;
        certs = new ArrayList();
        crls = new ArrayList();
        digestalgos = new HashSet();
        digestalgos.add(digestAlgorithm);

        //
        // Copy in the certificates and crls used to sign the private key.
        //
        signCert = (X509Certificate)certChain[0];
        for (int i = 0;i < certChain.length;i++)
        {
            certs.add(certChain[i]);
        }

        if (crlList != null)
        {
            for (int i = 0;i < crlList.length;i++)
            {
                crls.add(crlList[i]);
            }
        }

        //
        // Now we have private key, find out what the digestEncryptionAlgorithm is.
        //
        digestEncryptionAlgorithm = privKey.getAlgorithm();
        if (digestEncryptionAlgorithm.equals("RSA"))
        {
            digestEncryptionAlgorithm = ID_RSA;
        }
        else if (digestEncryptionAlgorithm.equals("DSA"))
        {
            digestEncryptionAlgorithm = ID_DSA;
        }
        else
        {
            throw new NoSuchAlgorithmException("Unknown Key Algorithm "+digestEncryptionAlgorithm);
        }

        sig = Signature.getInstance(getDigestAlgorithm(), provider);

        sig.initSign(privKey);
    }

    /**
     * Get the algorithm used to calculate the message digest
     */
    public String getDigestAlgorithm()
    {
        String da = digestAlgorithm;
        String dea = digestEncryptionAlgorithm;

        if (digestAlgorithm.equals(ID_MD5))
        {
            da = "MD5";
        }
        else if (digestAlgorithm.equals(ID_MD2))
        {
            da = "MD2";
        }
        else if (digestAlgorithm.equals(ID_SHA1))
        {
            da = "SHA1";
        }

        if (digestEncryptionAlgorithm.equals(ID_RSA))
        {
            dea = "RSA";
        }
        else if (digestEncryptionAlgorithm.equals(ID_DSA))
        {
            dea = "DSA";
        }

        return da + "with" + dea;
    }

    /**
     * Resets the PKCS7SignedData object to it's initial state, ready
     * to sign or verify a new buffer.
     */
    public void reset()
    {
        try
        {
            if (privKey==null)
            {
                sig.initVerify(signCert.getPublicKey());
            }
            else
            {
                sig.initSign(privKey);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Get the X.509 certificates associated with this PKCS#7 object
     */
    public Certificate[] getCertificates()
    {
        return (X509Certificate[])certs.toArray(new X509Certificate[certs.size()]);
    }

    /**
     * Get the X.509 certificate revocation lists associated with this PKCS#7 object
     */
    public Collection getCRLs()
    {
        return crls;
    }
    
    /**
     * Get the X.509 certificate actually used to sign the digest.
     */
    public X509Certificate getSigningCertificate()
    {
        return signCert;
    }

    /**
     * Get the version of the PKCS#7 object. Always 1
     */
    public int getVersion()
    {
        return version;
    }

    /**
     * Get the version of the PKCS#7 "SignerInfo" object. Always 1
     */
    public int getSigningInfoVersion()
    {
        return signerversion;
    }

    /**
     * Update the digest with the specified byte. This method is used both for signing and verifying
     */
    public void update(byte buf)
        throws SignatureException
    {
        sig.update(buf);
    }

    /**
     * Update the digest with the specified bytes. This method is used both for signing and verifying
     */
    public void update(byte[] buf, int off, int len)
        throws SignatureException
    {
        sig.update(buf, off, len);
    }

    /**
     * Verify the digest
     */
    public boolean verify()
        throws SignatureException
    {
        return sig.verify(digest);
    }

    /**
     * Get the "issuer" from the TBSCertificate bytes that are passed in
     */
    private DERObject getIssuer(byte[] enc)
    {
        try
        {
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(enc));
            ASN1Sequence seq = (ASN1Sequence)in.readObject();
            return (DERObject)seq.getObjectAt(seq.getObjectAt(0) instanceof DERTaggedObject ? 3 : 2);
        }
        catch (IOException e)
        {
            throw new Error("IOException reading from ByteArray: "+e);
        }
    }

    /**
     * return the bytes for the PKCS7SignedData object.
     */
    public byte[] getEncoded()
    {
        try
        {
        
            digest = sig.sign();

            // Create the set of Hash algorithms. I've assumed this is the
            // set of all hash agorithms used to created the digest in the
            // "signerInfo" structure. I may be wrong.
            //
            ASN1EncodableVector v = new ASN1EncodableVector();
            for (Iterator i = digestalgos.iterator(); i.hasNext();)
            {
                AlgorithmIdentifier a = new AlgorithmIdentifier(
                            new DERObjectIdentifier((String)i.next()),
                            null);
                
                v.add(a);
            }

            DERSet algos = new DERSet(v);

            // Create the contentInfo. Empty, I didn't implement this bit
            //
            DERSequence contentinfo = new DERSequence(
                                        new DERObjectIdentifier(ID_PKCS7_DATA));

            // Get all the certificates
            //
            v = new ASN1EncodableVector();
            for (Iterator i = certs.iterator();i.hasNext();)
            {
                ASN1InputStream tempstream = new ASN1InputStream(new ByteArrayInputStream(((X509Certificate)i.next()).getEncoded()));
                v.add(tempstream.readObject());
            }

            DERSet dercertificates = new DERSet(v);

            // Create signerinfo structure.
            //
            ASN1EncodableVector signerinfo = new ASN1EncodableVector();

            // Add the signerInfo version
            //
            signerinfo.add(new DERInteger(signerversion));

            IssuerAndSerialNumber isAnds = new IssuerAndSerialNumber(
                        new X509Name((ASN1Sequence)getIssuer(signCert.getTBSCertificate())),
                        new DERInteger(signCert.getSerialNumber()));
            signerinfo.add(isAnds);

            // Add the digestAlgorithm
            //
            // BEGIN android-changed
            signerinfo.add(new AlgorithmIdentifier(
                                new DERObjectIdentifier(digestAlgorithm),
                                DERNull.THE_ONE));

            //
            // Add the digestEncryptionAlgorithm
            //
            signerinfo.add(new AlgorithmIdentifier(
                                new DERObjectIdentifier(digestEncryptionAlgorithm),
                                DERNull.THE_ONE));
            // END android-changed

            //
            // Add the digest
            //
            signerinfo.add(new DEROctetString(digest));


            //
            // Finally build the body out of all the components above
            //
            ASN1EncodableVector body = new ASN1EncodableVector();
            body.add(new DERInteger(version));
            body.add(algos);
            body.add(contentinfo);
            body.add(new DERTaggedObject(false, 0, dercertificates));

            if (crls.size()>0)
            {
                v = new ASN1EncodableVector();
                for (Iterator i = crls.iterator();i.hasNext();)
                {
                    ASN1InputStream t = new ASN1InputStream(new ByteArrayInputStream(((X509CRL)i.next()).getEncoded()));
                    v.add(t.readObject());
                }
                DERSet dercrls = new DERSet(v);
                body.add(new DERTaggedObject(false, 1, dercrls));
            }

            // Only allow one signerInfo
            //
            body.add(new DERSet(new DERSequence(signerinfo)));

            // Now we have the body, wrap it in it's PKCS7Signed shell
            // and return it
            //
            ASN1EncodableVector whole = new ASN1EncodableVector();
            whole.add(new DERObjectIdentifier(ID_PKCS7_SIGNED_DATA));
            whole.add(new DERTaggedObject(0, new DERSequence(body)));

            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();

            DEROutputStream dout = new DEROutputStream(bOut);
            dout.writeObject(new DERSequence(whole));
            dout.close();

            return bOut.toByteArray();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.toString());
        }
    }
}
