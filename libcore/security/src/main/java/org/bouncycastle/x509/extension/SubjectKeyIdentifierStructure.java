package org.bouncycastle.x509.extension;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateParsingException;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

/**
 * A high level subject key identifier.
 */
public class SubjectKeyIdentifierStructure
    extends SubjectKeyIdentifier
{
    private AuthorityKeyIdentifier authKeyID;
    
    /**
     * Constructor which will take the byte[] returned from getExtensionValue()
     * 
     * @param encodedValue a DER octet encoded string with the extension structure in it.
     * @throws IOException on parsing errors.
     */
    public SubjectKeyIdentifierStructure(
        byte[]  encodedValue)
        throws IOException
    {
        super((ASN1OctetString)X509ExtensionUtil.fromExtensionValue(encodedValue));
    }
    
    private static ASN1OctetString fromPublicKey(
        PublicKey pubKey)
        throws CertificateParsingException
    {
        try
        {
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                (ASN1Sequence)new ASN1InputStream(pubKey.getEncoded()).readObject());

            return (ASN1OctetString)(new SubjectKeyIdentifier(info).toASN1Object());
        }
        catch (Exception e)
        {
            throw new CertificateParsingException("Exception extracting certificate details: " + e.toString());
        }
    }
    
    public SubjectKeyIdentifierStructure(
        PublicKey pubKey)
        throws CertificateParsingException
    {
        super(fromPublicKey(pubKey));
    }
}
