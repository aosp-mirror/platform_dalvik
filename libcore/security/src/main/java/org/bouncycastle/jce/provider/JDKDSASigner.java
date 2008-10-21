package org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.DSAKey;
import java.security.spec.AlgorithmParameterSpec;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DSA;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.DSASigner;
// BEGIN android-removed
// import org.bouncycastle.crypto.signers.ECDSASigner;
// import org.bouncycastle.crypto.signers.ECNRSigner;
// import org.bouncycastle.jce.interfaces.ECKey;
// import org.bouncycastle.jce.interfaces.ECPublicKey;
// import org.bouncycastle.jce.interfaces.GOST3410Key;
// END android-removed

public class JDKDSASigner
    extends Signature implements PKCSObjectIdentifiers, X509ObjectIdentifiers
{
    private Digest                  digest;
    private DSA                     signer;
    private SecureRandom            random;

    protected JDKDSASigner(
        String                  name,
        Digest                  digest,
        DSA                     signer)
    {
        super(name);

        this.digest = digest;
        this.signer = signer;
    }

    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        CipherParameters    param = null;

        // BEGIN android-removed
        // if (publicKey instanceof ECPublicKey)
        // {
        //     param = ECUtil.generatePublicKeyParameter(publicKey);
        // }
        // else if (publicKey instanceof GOST3410Key)
        // {
        //     param = GOST3410Util.generatePublicKeyParameter(publicKey);
        // }
        // else if (publicKey instanceof DSAKey)
        // END android-removed
        // BEGIN android-added
        if (publicKey instanceof DSAKey)
        // END android-added
        {
            param = DSAUtil.generatePublicKeyParameter(publicKey);
        }
        else
        {
            try
            {
                byte[]  bytes = publicKey.getEncoded();

                publicKey = JDKKeyFactory.createPublicKeyFromDERStream(bytes);

                // BEGIN android-removed
                // if (publicKey instanceof ECPublicKey)
                // {
                //     param = ECUtil.generatePublicKeyParameter(publicKey);
                // }
                // else if (publicKey instanceof DSAKey)
                // END android-removed
                // BEGIN android-added
                if (publicKey instanceof DSAKey)
                // END android-added
                {
                    param = DSAUtil.generatePublicKeyParameter(publicKey);
                }
                else
                {
                    throw new InvalidKeyException("can't recognise key type in DSA based signer");
                }
            }
            catch (Exception e)
            {
                throw new InvalidKeyException("can't recognise key type in DSA based signer");
            }
        }

        digest.reset();
        signer.init(false, param);
    }

    protected void engineInitSign(
        PrivateKey      privateKey,
        SecureRandom    random)
        throws InvalidKeyException
    {
        this.random = random;
        engineInitSign(privateKey);
    }

    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        CipherParameters    param = null;

        // BEGIN android-removed
        // if (privateKey instanceof ECKey)
        // {
        //     param = ECUtil.generatePrivateKeyParameter(privateKey);
        // }
        // else if (privateKey instanceof GOST3410Key)
        // {
        //     param = GOST3410Util.generatePrivateKeyParameter(privateKey);
        // }
        // else
        // {
        // END android-removed
            param = DSAUtil.generatePrivateKeyParameter(privateKey);
        // BEGIN android-removed
        // }
        // END android-removed

        digest.reset();

        if (random != null)
        {
            signer.init(true, new ParametersWithRandom(param, random));
        }
        else
        {
            signer.init(true, param);
        }
    }

    protected void engineUpdate(
        byte    b)
        throws SignatureException
    {
        digest.update(b);
    }

    protected void engineUpdate(
        byte[]  b,
        int     off,
        int     len) 
        throws SignatureException
    {
        digest.update(b, off, len);
    }

    protected byte[] engineSign()
        throws SignatureException
    {
        byte[]  hash = new byte[digest.getDigestSize()];

        digest.doFinal(hash, 0);

        try
        {
            BigInteger[]    sig = signer.generateSignature(hash);

            return derEncode(sig[0], sig[1]);
        }
        catch (Exception e)
        {
            throw new SignatureException(e.toString());
        }
    }

    protected boolean engineVerify(
        byte[]  sigBytes) 
        throws SignatureException
    {
        byte[]  hash = new byte[digest.getDigestSize()];

        digest.doFinal(hash, 0);

        BigInteger[]    sig;

        try
        {
            sig = derDecode(sigBytes);
        }
        catch (Exception e)
        {
            throw new SignatureException("error decoding signature bytes.");
        }

        return signer.verifySignature(hash, sig[0], sig[1]);
    }

    protected void engineSetParameter(
        AlgorithmParameterSpec params)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /**
     * @deprecated replaced with <a href = "#engineSetParameter(java.security.spec.AlgorithmParameterSpec)">
     */
    protected void engineSetParameter(
        String  param,
        Object  value)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /**
     * @deprecated
     */
    protected Object engineGetParameter(
        String      param)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    private byte[] derEncode(
        BigInteger  r,
        BigInteger  s)
        throws IOException
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);
        ASN1EncodableVector     v = new ASN1EncodableVector();

        v.add(new DERInteger(r));
        v.add(new DERInteger(s));

        dOut.writeObject(new DERSequence(v));

        return bOut.toByteArray();
    }

    private BigInteger[] derDecode(
        byte[]  encoding)
        throws IOException
    {
        ASN1InputStream         aIn = new ASN1InputStream(encoding);
        ASN1Sequence            s = (ASN1Sequence)aIn.readObject();

        BigInteger[]            sig = new BigInteger[2];

        sig[0] = ((DERInteger)s.getObjectAt(0)).getValue();
        sig[1] = ((DERInteger)s.getObjectAt(1)).getValue();

        return sig;
    }

    static public class stdDSA
        extends JDKDSASigner
    {
        public stdDSA()
        {
            super("SHA1withDSA", new SHA1Digest(), new DSASigner());
        }
    }

    static public class noneDSA
        extends JDKDSASigner
    {
        public noneDSA()
        {
            super("NONEwithDSA", new NullDigest(), new DSASigner());
        }
    }
    
// BEGIN android-removed
//    static public class ecDSA
//        extends JDKDSASigner
//    {
//        public ecDSA()
//        {
//            super("SHA1withECDSA", new SHA1Digest(), new ECDSASigner());
//        }
//    }
//    
//    static public class ecDSA224
//        extends JDKDSASigner
//    {
//        public ecDSA224()
//        {
//            super("SHA224withECDSA", new SHA224Digest(), new ECDSASigner());
//        }
//    }
//    
//    static public class ecDSA256
//        extends JDKDSASigner
//    {
//        public ecDSA256()
//        {
//            super("SHA256withECDSA", new SHA256Digest(), new ECDSASigner());
//        }
//    }
//    
//    static public class ecDSA384
//        extends JDKDSASigner
//    {
//        public ecDSA384()
//        {
//            super("SHA384withECDSA", new SHA384Digest(), new ECDSASigner());
//        }
//    }
//    
//    static public class ecDSA512
//        extends JDKDSASigner
//    {
//        public ecDSA512()
//        {
//            super("SHA512withECDSA", new SHA512Digest(), new ECDSASigner());
//        }
//    }
//    
//
//    static public class ecNR 
//        extends JDKDSASigner
//    {
//        public ecNR()
//        {
//            super("SHA1withECNR", new SHA1Digest(), new ECNRSigner());
//        }
//    }
//
//    static public class ecNR224 
//        extends JDKDSASigner
//    {
//        public ecNR224()
//        {
//            super("SHA224withECNR", new SHA224Digest(), new ECNRSigner());
//        }
//    }
//
//    static public class ecNR256 
//        extends JDKDSASigner
//    {
//        public ecNR256()
//        {
//            super("SHA256withECNR", new SHA256Digest(), new ECNRSigner());
//        }
//    }
//
//    static public class ecNR384 
//        extends JDKDSASigner
//    {
//        public ecNR384()
//        {
//            super("SHA384withECNR", new SHA384Digest(), new ECNRSigner());
//        }
//    }
//
//    static public class ecNR512 
//        extends JDKDSASigner
//    {
//        public ecNR512()
//        {
//            super("SHA512withECNR", new SHA512Digest(), new ECNRSigner());
//        }
//    }
// END android-removed
    
    private static class NullDigest
        implements Digest
    {
        private ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        
        public String getAlgorithmName()
        {
            return "NULL";
        }
    
        public int getDigestSize()
        {
            return bOut.size();
        }
    
        public void update(byte in)
        {
            bOut.write(in);
        }
    
        public void update(byte[] in, int inOff, int len)
        {
            bOut.write(in, inOff, len);
        }
    
        public int doFinal(byte[] out, int outOff)
        {
            byte[] res = bOut.toByteArray();
            
            System.arraycopy(res, 0, out, outOff, res.length);
            
            return res.length;
        }
    
        public void reset()
        {
            bOut.reset();
        }
    }
}
