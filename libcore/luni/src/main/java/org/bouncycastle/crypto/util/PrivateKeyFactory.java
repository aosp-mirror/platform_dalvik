package org.bouncycastle.crypto.util;

import java.io.IOException;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
// BEGIN android-removed
// import org.bouncycastle.asn1.oiw.ElGamalParameter;
// END android-removed
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
// BEGIN android-removed
// import org.bouncycastle.asn1.sec.ECPrivateKeyStructure;
// END android-removed
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
// BEGIN android-removed
// import org.bouncycastle.asn1.x9.X962NamedCurves;
// import org.bouncycastle.asn1.x9.X962Parameters;
// import org.bouncycastle.asn1.x9.X9ECParameters;
// END android-removed
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
// BEGIN android-removed
// import org.bouncycastle.crypto.params.ECDomainParameters;
// import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
// import org.bouncycastle.crypto.params.ElGamalParameters;
// import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
//END android-removed
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

/**
 * Factory for creating private key objects from PKCS8 PrivateKeyInfo objects.
 */
public class PrivateKeyFactory
{
    /**
     * Create a private key parameter from the passed in PKCS8 PrivateKeyInfo object.
     * 
     * @param keyInfo the PrivateKeyInfo object containing the key material
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(
        PrivateKeyInfo    keyInfo)
        throws IOException
    {
        AlgorithmIdentifier     algId = keyInfo.getAlgorithmId();
        
        if (algId.getObjectId().equals(PKCSObjectIdentifiers.rsaEncryption))
        {
            RSAPrivateKeyStructure  keyStructure = new RSAPrivateKeyStructure((ASN1Sequence)keyInfo.getPrivateKey());

            return new RSAPrivateCrtKeyParameters(
                                        keyStructure.getModulus(),
                                        keyStructure.getPublicExponent(),
                                        keyStructure.getPrivateExponent(),
                                        keyStructure.getPrime1(),
                                        keyStructure.getPrime2(),
                                        keyStructure.getExponent1(),
                                        keyStructure.getExponent2(),
                                        keyStructure.getCoefficient());
        }
        else if (algId.getObjectId().equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
            DHParameter     params = new DHParameter((ASN1Sequence)keyInfo.getAlgorithmId().getParameters());
            DERInteger      derX = (DERInteger)keyInfo.getPrivateKey();

            return new DHPrivateKeyParameters(derX.getValue(), new DHParameters(params.getP(), params.getG()));
        }
        // BEGIN android-removed
        // else if (algId.getObjectId().equals(OIWObjectIdentifiers.elGamalAlgorithm))
        // {
        //     ElGamalParameter    params = new ElGamalParameter((ASN1Sequence)keyInfo.getAlgorithmId().getParameters());
        //     DERInteger          derX = (DERInteger)keyInfo.getPrivateKey();
        //
        //     return new ElGamalPrivateKeyParameters(derX.getValue(), new ElGamalParameters(params.getP(), params.getG()));
        // }
        // END android-removed
        else if (algId.getObjectId().equals(X9ObjectIdentifiers.id_dsa))
        {
            DSAParameter    params = new DSAParameter((ASN1Sequence)keyInfo.getAlgorithmId().getParameters());
            DERInteger      derX = (DERInteger)keyInfo.getPrivateKey();

            return new DSAPrivateKeyParameters(derX.getValue(), new DSAParameters(params.getP(), params.getQ(), params.getG()));
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
        //     ECPrivateKeyStructure   ec = new ECPrivateKeyStructure((ASN1Sequence)keyInfo.getPrivateKey());
        //
        //     return new ECPrivateKeyParameters(ec.getKey(), dParams);
        // }
        // END android-removed
        else
        {
            throw new RuntimeException("algorithm identifier in key not recognised");
        }
    }
}
