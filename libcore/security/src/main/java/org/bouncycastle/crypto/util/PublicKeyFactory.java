package org.bouncycastle.crypto.util;

import java.io.IOException;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
// BEGIN android-removed
// import org.bouncycastle.asn1.oiw.ElGamalParameter;
// END android-removed
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
// BEGIN android-removed
// import org.bouncycastle.asn1.x9.X962NamedCurves;
// import org.bouncycastle.asn1.x9.X962Parameters;
// import org.bouncycastle.asn1.x9.X9ECParameters;
// import org.bouncycastle.asn1.x9.X9ECPoint;
// END android-removed
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
// BEGIN android-removed
// import org.bouncycastle.crypto.params.ECDomainParameters;
// import org.bouncycastle.crypto.params.ECPublicKeyParameters;
// import org.bouncycastle.crypto.params.ElGamalParameters;
// import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
// END android-removed
import org.bouncycastle.crypto.params.RSAKeyParameters;

/**
 * Factory to create asymmetric public key parameters for asymmetric ciphers
 * from range of ASN.1 encoded SubjectPublicKeyInfo objects.
 */
public class PublicKeyFactory
{
    /**
     * Create a public key from the passed in SubjectPublicKeyInfo
     * 
     * @param keyInfo the SubjectPublicKeyInfo containing the key data
     * @return the appropriate key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(
        SubjectPublicKeyInfo    keyInfo)
        throws IOException
    {
        AlgorithmIdentifier     algId = keyInfo.getAlgorithmId();
        
        if (algId.getObjectId().equals(PKCSObjectIdentifiers.rsaEncryption)
            || algId.getObjectId().equals(X509ObjectIdentifiers.id_ea_rsa))
        {
            RSAPublicKeyStructure   pubKey = new RSAPublicKeyStructure((ASN1Sequence)keyInfo.getPublicKey());

            return new RSAKeyParameters(false, pubKey.getModulus(), pubKey.getPublicExponent());
        }
        else if (algId.getObjectId().equals(PKCSObjectIdentifiers.dhKeyAgreement)
                 || algId.getObjectId().equals(X9ObjectIdentifiers.dhpublicnumber))
        {
            DHParameter params = new DHParameter((ASN1Sequence)keyInfo.getAlgorithmId().getParameters());
            DERInteger  derY = (DERInteger)keyInfo.getPublicKey();
            
            return new DHPublicKeyParameters(derY.getValue(), new DHParameters(params.getP(), params.getG()));
        }
        // BEGIN android-removed
        // else if (algId.getObjectId().equals(OIWObjectIdentifiers.elGamalAlgorithm))
        // {
        //     ElGamalParameter    params = new ElGamalParameter((ASN1Sequence)keyInfo.getAlgorithmId().getParameters());
        //     DERInteger          derY = (DERInteger)keyInfo.getPublicKey();
        //
        //     return new ElGamalPublicKeyParameters(derY.getValue(), new ElGamalParameters(params.getP(), params.getG()));
        // }
        // END android-removed
        else if (algId.getObjectId().equals(X9ObjectIdentifiers.id_dsa)
                 || algId.getObjectId().equals(OIWObjectIdentifiers.dsaWithSHA1))
        {
            DSAParameter    params = new DSAParameter((ASN1Sequence)keyInfo.getAlgorithmId().getParameters());
            DERInteger      derY = (DERInteger)keyInfo.getPublicKey();

            return new DSAPublicKeyParameters(derY.getValue(), new DSAParameters(params.getP(), params.getQ(), params.getG()));
        }
        // BEGIN android-removed
        // else if (algId.getObjectId().equals(X9ObjectIdentifiers.id_ecPublicKey))
        // {
        //     X962Parameters      params = new X962Parameters((DERObject)keyInfo.getAlgorithmId().getParameters());
        //     ECDomainParameters  dParams = null;
        //     
        //     if (params.isNamedCurve())
        //     {
        //         DERObjectIdentifier oid = (DERObjectIdentifier)params.getParameters();
        //         X9ECParameters      ecP = X962NamedCurves.getByOID(oid);
        //
        //         dParams = new ECDomainParameters(
        //                                     ecP.getCurve(),
        //                                     ecP.getG(),
        //                                     ecP.getN(),
        //                                     ecP.getH(),
        //                                     ecP.getSeed());
        //     }
        //     else
        //     {
        //         X9ECParameters ecP = new X9ECParameters(
        //                     (ASN1Sequence)params.getParameters());
        //         dParams = new ECDomainParameters(
        //                                     ecP.getCurve(),
        //                                     ecP.getG(),
        //                                     ecP.getN(),
        //                                     ecP.getH(),
        //                                     ecP.getSeed());
        //     }
        //
        //     DERBitString    bits = keyInfo.getPublicKeyData();
        //     byte[]          data = bits.getBytes();
        //     ASN1OctetString key = new DEROctetString(data);
        //
        //     X9ECPoint       derQ = new X9ECPoint(dParams.getCurve(), key);
        //    
        //     return new ECPublicKeyParameters(derQ.getPoint(), dParams);
        // }
        // BEGIN android-removed
        else
        {
            throw new RuntimeException("algorithm identifier in key not recognised");
        }
    }
}
