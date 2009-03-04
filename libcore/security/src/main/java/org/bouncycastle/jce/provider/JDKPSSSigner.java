package org.bouncycastle.jce.provider;

import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.PSSSigner;

public class JDKPSSSigner
    extends Signature
{
    private AlgorithmParameters    engineParams;
    private PSSParameterSpec       paramSpec;
    private PSSParameterSpec       originalSpec;
    private AsymmetricBlockCipher  signer;
    private Digest digest;
    private int saltLength;
    private byte trailer;

    private PSSSigner pss;

    private byte getTrailer(
        int trailerField)
    {
        if (trailerField == 1)
        {
            return PSSSigner.TRAILER_IMPLICIT;
        }
        
        throw new IllegalArgumentException("unknown trailer field");
    }
    
    protected JDKPSSSigner(
        String                name,
        AsymmetricBlockCipher signer,
        PSSParameterSpec      paramSpec)
    {
        super(name);

        this.signer = signer;
        
        if (paramSpec == null)
        {
            originalSpec = null;
            paramSpec = PSSParameterSpec.DEFAULT;
        }
        else
        {
            originalSpec = paramSpec;
            this.paramSpec = paramSpec;
        }
        
        this.digest = JCEDigestUtil.getDigest(paramSpec.getDigestAlgorithm());
        this.saltLength = paramSpec.getSaltLength();
        this.trailer = getTrailer(paramSpec.getTrailerField());
    }
    
    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        if (!(publicKey instanceof RSAPublicKey))
        {
            throw new InvalidKeyException("Supplied key is not a RSAPublicKey instance");
        }

        pss = new PSSSigner(signer, digest, saltLength);
        pss.init(false,
            RSAUtil.generatePublicKeyParameter((RSAPublicKey)publicKey));
    }

    protected void engineInitSign(
        PrivateKey      privateKey,
        SecureRandom    random)
        throws InvalidKeyException
    {
        if (!(privateKey instanceof RSAPrivateKey))
        {
            throw new InvalidKeyException("Supplied key is not a RSAPrivateKey instance");
        }

        pss = new PSSSigner(signer, digest, saltLength, trailer);
        pss.init(true, new ParametersWithRandom(RSAUtil.generatePrivateKeyParameter((RSAPrivateKey)privateKey), random));
    }

    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        if (!(privateKey instanceof RSAPrivateKey))
        {
            throw new InvalidKeyException("Supplied key is not a RSAPrivateKey instance");
        }

        pss = new PSSSigner(signer, digest, saltLength, trailer);
        pss.init(true, RSAUtil.generatePrivateKeyParameter((RSAPrivateKey)privateKey));
    }

    protected void engineUpdate(
        byte    b)
        throws SignatureException
    {
        pss.update(b);
    }

    protected void engineUpdate(
        byte[]  b,
        int     off,
        int     len) 
        throws SignatureException
    {
        pss.update(b, off, len);
    }

    protected byte[] engineSign()
        throws SignatureException
    {
        try
        {
            return pss.generateSignature();
        }
        catch (CryptoException e)
        {
            throw new SignatureException(e.getMessage());
        }
    }

    protected boolean engineVerify(
        byte[]  sigBytes) 
        throws SignatureException
    {
        return pss.verifySignature(sigBytes);
    }

    protected void engineSetParameter(
        AlgorithmParameterSpec params)
        throws InvalidParameterException
    {
        if (params instanceof PSSParameterSpec)
        {
            paramSpec = (PSSParameterSpec)params;
            
            if (originalSpec != null)
            {
                if (!JCEDigestUtil.isSameDigest(originalSpec.getDigestAlgorithm(), paramSpec.getDigestAlgorithm()))
                {
                    throw new InvalidParameterException("parameter must be using " + originalSpec.getDigestAlgorithm());
                }
            }
            if (!paramSpec.getMGFAlgorithm().equalsIgnoreCase("MGF1") && !paramSpec.getMGFAlgorithm().equals(PKCSObjectIdentifiers.id_mgf1.getId()))
            {
                throw new InvalidParameterException("unknown mask generation function specified");
            }
            
            if (!(paramSpec.getMGFParameters() instanceof MGF1ParameterSpec))
            {
                throw new InvalidParameterException("unkown MGF parameters");
            }
            
            MGF1ParameterSpec   mgfParams = (MGF1ParameterSpec)paramSpec.getMGFParameters();
            
            if (!JCEDigestUtil.isSameDigest(mgfParams.getDigestAlgorithm(), paramSpec.getDigestAlgorithm()))
            {
                throw new InvalidParameterException("digest algorithm for MGF should be the same as for PSS parameters.");
            }
            
            digest = JCEDigestUtil.getDigest(mgfParams.getDigestAlgorithm());
            
            if (digest == null)
            {
                throw new InvalidParameterException("no match on MGF digest algorithm: "+ mgfParams.getDigestAlgorithm());
            }
            
            this.saltLength = paramSpec.getSaltLength();
            this.trailer = getTrailer(paramSpec.getTrailerField());
        }
        else
        {
            throw new InvalidParameterException("Only PSSParameterSpec supported");
        }
    }

    protected AlgorithmParameters engineGetParameters() 
    {
        if (engineParams == null)
        {
            if (paramSpec != null)
            {
                try
                {
                    engineParams = AlgorithmParameters.getInstance("PSS", "BC");
                    engineParams.init(paramSpec);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e.toString());
                }
            }
        }

        return engineParams;
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
    
    protected Object engineGetParameter(
        String param)
    {
        throw new UnsupportedOperationException("engineGetParameter unsupported");
    }

    static public class PSSwithRSA
        extends JDKPSSSigner
    {
        public PSSwithRSA()
        {
            super("SHA1withRSAandMGF1", new RSAEngine(), null);
        }
    }
    
    static public class SHA1withRSA
        extends JDKPSSSigner
    {
        public SHA1withRSA()
        {
            super("SHA1withRSAandMGF1", new RSAEngine(), PSSParameterSpec.DEFAULT);
        }
    }

    static public class SHA224withRSA
        extends JDKPSSSigner
    {
        public SHA224withRSA()
        {
            super("SHA2224withRSAandMGF1", new RSAEngine(), new PSSParameterSpec("SHA-224", "MGF1", new MGF1ParameterSpec("SHA-224"), 28, 1));
        }
    }
    
    static public class SHA256withRSA
        extends JDKPSSSigner
    {
        public SHA256withRSA()
        {
            super("SHA256withRSAandMGF1", new RSAEngine(), new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1));
        }
    }

    static public class SHA384withRSA
        extends JDKPSSSigner
    {
        public SHA384withRSA()
        {
            super("SHA384withRSAandMGF1", new RSAEngine(), new PSSParameterSpec("SHA-384", "MGF1", new MGF1ParameterSpec("SHA-384"), 48, 1));
        }
    }

    static public class SHA512withRSA
        extends JDKPSSSigner
    {
        public SHA512withRSA()
        {
            super("SHA512withRSAandMGF1", new RSAEngine(), new PSSParameterSpec("SHA-512", "MGF1", new MGF1ParameterSpec("SHA-512"), 64, 1));
        }
    }
}
