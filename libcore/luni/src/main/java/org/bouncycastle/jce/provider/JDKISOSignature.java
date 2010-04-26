package org.bouncycastle.jce.provider;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
// BEGIN android-removed
// import org.bouncycastle.crypto.digests.RIPEMD160Digest;
// END android-removed
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.signers.ISO9796d2Signer;

public class JDKISOSignature
    extends Signature
{
    private ISO9796d2Signer         signer;

    protected JDKISOSignature(
        String                  name,
        Digest                  digest,
        AsymmetricBlockCipher   cipher)
    {
        super(name);

        signer = new ISO9796d2Signer(cipher, digest, true);
    }

    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        CipherParameters    param = RSAUtil.generatePublicKeyParameter((RSAPublicKey)publicKey);

        signer.init(false, param);
    }

    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        CipherParameters    param = RSAUtil.generatePrivateKeyParameter((RSAPrivateKey)privateKey);

        signer.init(true, param);
    }

    protected void engineUpdate(
        byte    b)
        throws SignatureException
    {
        signer.update(b);
    }

    protected void engineUpdate(
        byte[]  b,
        int     off,
        int     len) 
        throws SignatureException
    {
        signer.update(b, off, len);
    }

    protected byte[] engineSign()
        throws SignatureException
    {
        try
        {
            byte[]  sig = signer.generateSignature();

            return sig;
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
        boolean yes = signer.verifySignature(sigBytes);

        return yes;
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

    static public class SHA1WithRSAEncryption
        extends JDKISOSignature
    {
        public SHA1WithRSAEncryption()
        {
            super("SHA1withRSA/ISO9796-2", new SHA1Digest(), new RSAEngine());
        }
    }

    static public class MD5WithRSAEncryption
        extends JDKISOSignature
    {
        public MD5WithRSAEncryption()
        {
            super("MD5withRSA/ISO9796-2", new MD5Digest(), new RSAEngine());
        }
    }

// BEGIN android-removed
//    static public class RIPEMD160WithRSAEncryption
//        extends JDKISOSignature
//    {
//        public RIPEMD160WithRSAEncryption()
//        {
//            super("RIPEMD160withRSA/ISO9796-2", new RIPEMD160Digest(), new RSAEngine());
//        }
//    }
// END android-removed
}
