package org.bouncycastle.jce;

import java.io.*;
import java.security.cert.*;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x509.*;

/**
 * a utility class that will extract X509Principal objects from X.509 certificates.
 * <p>
 * Use this in preference to trying to recreate a principal from a String, not all
 * DNs are what they should be, so it's best to leave them encoded where they
 * can be.
 */
public class PrincipalUtil
{
    /**
     * return the issuer of the given cert as an X509PrincipalObject.
     */
    public static X509Principal getIssuerX509Principal(
        X509Certificate cert)
        throws CertificateEncodingException
    {
        try
        {
            ByteArrayInputStream    bIn = new ByteArrayInputStream(
                cert.getTBSCertificate());
            ASN1InputStream         aIn = new ASN1InputStream(bIn);
            TBSCertificateStructure tbsCert = new TBSCertificateStructure(
                                            (ASN1Sequence)aIn.readObject());

            return new X509Principal(tbsCert.getIssuer());
        }
        catch (IOException e)
        {
            throw new CertificateEncodingException(e.toString());
        }
    }

    /**
     * return the subject of the given cert as an X509PrincipalObject.
     */
    public static X509Principal getSubjectX509Principal(
        X509Certificate cert)
        throws CertificateEncodingException
    {
        try
        {
            ByteArrayInputStream    bIn = new ByteArrayInputStream(
                cert.getTBSCertificate());
            ASN1InputStream         aIn = new ASN1InputStream(bIn);
            TBSCertificateStructure tbsCert = new TBSCertificateStructure(
                                            (ASN1Sequence)aIn.readObject());

            return new X509Principal(tbsCert.getSubject());
        }
        catch (IOException e)
        {
            throw new CertificateEncodingException(e.toString());
        }
    }
    
    /**
     * return the issuer of the given CRL as an X509PrincipalObject.
     */
    public static X509Principal getIssuerX509Principal(
        X509CRL crl)
        throws CRLException
    {
        try
        {
            ByteArrayInputStream    bIn = new ByteArrayInputStream(
                crl.getTBSCertList());
            ASN1InputStream         aIn = new ASN1InputStream(bIn);
            TBSCertList tbsCertList = new TBSCertList(
                                            (ASN1Sequence)aIn.readObject());

            return new X509Principal(tbsCertList.getIssuer());
        }
        catch (IOException e)
        {
            throw new CRLException(e.toString());
        }
    }
}
