package org.bouncycastle.jce.provider;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.RC5ParameterSpec;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.CTSBlockCipher;
import org.bouncycastle.crypto.modes.GOFBBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;
// BEGIN android-removed
// import org.bouncycastle.crypto.modes.OpenPGPCFBBlockCipher;
// import org.bouncycastle.crypto.modes.PGPCFBBlockCipher;
// END android-removed
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.paddings.ISO10126d2Padding;
import org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.TBCPadding;
import org.bouncycastle.crypto.paddings.X923Padding;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.ParametersWithSBox;
import org.bouncycastle.crypto.params.RC2Parameters;
import org.bouncycastle.crypto.params.RC5Parameters;
// BEGIN android-removed
// import org.bouncycastle.jce.spec.GOST28147ParameterSpec;
// END android-removed
import org.bouncycastle.util.Strings;

public class JCEBlockCipher extends WrapCipherSpi
    implements PBE
{
    //
    // specs we can handle.
    //
    private Class[]                 availableSpecs =
                                    {
                                        RC2ParameterSpec.class,
                                        RC5ParameterSpec.class,
                                        IvParameterSpec.class,
                                        PBEParameterSpec.class,
                                        //GOST28147ParameterSpec.class
                                    };
 
    private BlockCipher             baseEngine;
    private BufferedBlockCipher     cipher;
    private ParametersWithIV        ivParam;

    private int                     ivLength = 0;

    private boolean                 padded = true;
    
    private PBEParameterSpec        pbeSpec = null;
    private String                  pbeAlgorithm = null;
    
    private String                  modeName = null;

    protected JCEBlockCipher(
        BlockCipher engine)
    {
        baseEngine = engine;

        cipher = new PaddedBufferedBlockCipher(engine);
    }
        
    protected JCEBlockCipher(
        BlockCipher engine,
        int         ivLength)
    {
        baseEngine = engine;

        this.cipher = new PaddedBufferedBlockCipher(engine);
        this.ivLength = ivLength / 8;
    }

    protected int engineGetBlockSize() 
    {
        return baseEngine.getBlockSize();
    }

    protected byte[] engineGetIV() 
    {
        return (ivParam != null) ? ivParam.getIV() : null;
    }

    protected int engineGetKeySize(
        Key     key) 
    {
        return key.getEncoded().length * 8;
    }

    protected int engineGetOutputSize(
        int     inputLen) 
    {
        return cipher.getOutputSize(inputLen);
    }

    protected AlgorithmParameters engineGetParameters() 
    {
        if (engineParams == null)
        {
            if (pbeSpec != null)
            {
                try
                {
                    engineParams = AlgorithmParameters.getInstance(pbeAlgorithm, "BC");
                    engineParams.init(pbeSpec);
                }
                catch (Exception e)
                {
                    return null;
                }
            }
            else if (ivParam != null)
            {
                String  name = cipher.getUnderlyingCipher().getAlgorithmName();

                if (name.indexOf('/') >= 0)
                {
                    name = name.substring(0, name.indexOf('/'));
                }

                try
                {
                    engineParams = AlgorithmParameters.getInstance(name, "BC");
                    engineParams.init(ivParam.getIV());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e.toString());
                }
            }
        }

        return engineParams;
    }

    protected void engineSetMode(
        String  mode)
        throws NoSuchAlgorithmException
    {
        modeName = Strings.toUpperCase(mode);

        if (modeName.equals("ECB"))
        {
            ivLength = 0;
            cipher = new PaddedBufferedBlockCipher(baseEngine);
        }
        else if (modeName.equals("CBC"))
        {
            ivLength = baseEngine.getBlockSize();
            cipher = new PaddedBufferedBlockCipher(
                            new CBCBlockCipher(baseEngine));
        }
        else if (modeName.startsWith("OFB"))
        {
            ivLength = baseEngine.getBlockSize();
            if (modeName.length() != 3)
            {
                int wordSize = Integer.parseInt(modeName.substring(3));

                cipher = new PaddedBufferedBlockCipher(
                                new OFBBlockCipher(baseEngine, wordSize));
            }
            else
            {
                cipher = new PaddedBufferedBlockCipher(
                        new OFBBlockCipher(baseEngine, 8 * baseEngine.getBlockSize()));
            }
        }
        else if (modeName.startsWith("CFB"))
        {
            ivLength = baseEngine.getBlockSize();
            if (modeName.length() != 3)
            {
                int wordSize = Integer.parseInt(modeName.substring(3));

                cipher = new PaddedBufferedBlockCipher(
                                new CFBBlockCipher(baseEngine, wordSize));
            }
            else
            {
                cipher = new PaddedBufferedBlockCipher(
                        new CFBBlockCipher(baseEngine, 8 * baseEngine.getBlockSize()));
            }
        }
        // BEGIN android-removed
        // else if (modeName.startsWith("PGP"))
        // {
        //     if (modeName.equalsIgnoreCase("PGPCFBwithIV"))
        //     {
        //         ivLength = baseEngine.getBlockSize();
        //         cipher = new PaddedBufferedBlockCipher(
        //             new PGPCFBBlockCipher(baseEngine, true));
        //     }
        //     else
        //     {
        //         ivLength = baseEngine.getBlockSize();
        //         cipher = new PaddedBufferedBlockCipher(
        //             new PGPCFBBlockCipher(baseEngine, false));
        //     }
        // }
        // else if (modeName.equalsIgnoreCase("OpenPGPCFB"))
        // {
        //     ivLength = 0;
        //     cipher = new PaddedBufferedBlockCipher(
        //         new OpenPGPCFBBlockCipher(baseEngine));
        // }
        // END android-removed
        else if (modeName.startsWith("SIC"))
        {
            ivLength = baseEngine.getBlockSize();
            if (ivLength < 16)
            {
                throw new IllegalArgumentException("Warning: SIC-Mode can become a twotime-pad if the blocksize of the cipher is too small. Use a cipher with a block size of at least 128 bits (e.g. AES)");
            }
            cipher = new BufferedBlockCipher(
                        new SICBlockCipher(baseEngine));
        }
        else if (modeName.startsWith("CTR"))
        {
            ivLength = baseEngine.getBlockSize();
            cipher = new BufferedBlockCipher(
                        new SICBlockCipher(baseEngine));
        }
        else if (modeName.startsWith("GOFB"))
        {
            ivLength = baseEngine.getBlockSize();
            cipher = new BufferedBlockCipher(
                        new GOFBBlockCipher(baseEngine));
        }
        else if (modeName.startsWith("CTS"))
        {
            ivLength = baseEngine.getBlockSize();
            cipher = new CTSBlockCipher(new CBCBlockCipher(baseEngine));
        }
        else
        {
            throw new NoSuchAlgorithmException("can't support mode " + mode);
        }
    }

    protected void engineSetPadding(
        String  padding) 
    throws NoSuchPaddingException
    {
        String  paddingName = Strings.toUpperCase(padding);

        if (paddingName.equals("NOPADDING"))
        {
            padded = false;
            
            if (!(cipher instanceof CTSBlockCipher))
            {
                cipher = new BufferedBlockCipher(cipher.getUnderlyingCipher());
            }
        }
        else if (paddingName.equals("PKCS5PADDING") || paddingName.equals("PKCS7PADDING"))
        {
            cipher = new PaddedBufferedBlockCipher(cipher.getUnderlyingCipher());
        }
        else if (paddingName.equals("ZEROBYTEPADDING"))
        {
            cipher = new PaddedBufferedBlockCipher(cipher.getUnderlyingCipher(), new ZeroBytePadding());
        }
        else if (paddingName.equals("ISO10126PADDING") || paddingName.equals("ISO10126-2PADDING"))
        {
            cipher = new PaddedBufferedBlockCipher(cipher.getUnderlyingCipher(), new ISO10126d2Padding());
        }
        else if (paddingName.equals("X9.23PADDING") || paddingName.equals("X923PADDING"))
        {
            cipher = new PaddedBufferedBlockCipher(cipher.getUnderlyingCipher(), new X923Padding());
        }
        else if (paddingName.equals("ISO7816-4PADDING") || paddingName.equals("ISO9797-1PADDING"))
        {
            cipher = new PaddedBufferedBlockCipher(cipher.getUnderlyingCipher(), new ISO7816d4Padding());
        }
        else if (paddingName.equals("TBCPADDING"))
        {
            cipher = new PaddedBufferedBlockCipher(cipher.getUnderlyingCipher(), new TBCPadding());
        }
        else if (paddingName.equals("WITHCTS"))
        {
            padded = false;
            cipher = new CTSBlockCipher(cipher.getUnderlyingCipher());
        }
        else
        {
            throw new NoSuchPaddingException("Padding " + padding + " unknown.");
        }
    }

    protected void engineInit(
        int                     opmode,
        Key                     key,
        AlgorithmParameterSpec  params,
        SecureRandom            random) 
        throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        CipherParameters        param;
        
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.engineParams = null;
        
        //
        // basic key check
        //
        if (!(key instanceof SecretKey))
        {
            throw new InvalidKeyException("Key for algorithm " + key.getAlgorithm() + " not suitable for symmetric enryption.");
        }
        
        //
        // for RC5-64 we must have some default parameters
        //
        if (params == null && baseEngine.getAlgorithmName().startsWith("RC5-64"))
        {
            throw new InvalidAlgorithmParameterException("RC5 requires an RC5ParametersSpec to be passed in.");
        }

        //
        // a note on iv's - if ivLength is zero the IV gets ignored (we don't use it).
        //
        if (key instanceof JCEPBEKey)
        {
            JCEPBEKey   k = (JCEPBEKey)key;
            
            if (k.getOID() != null)
            {
                pbeAlgorithm = k.getOID().getId();
            }
            else
            {
                pbeAlgorithm = k.getAlgorithm();
            }
            
            if (k.getParam() != null)
            {
                param = k.getParam();
                pbeSpec = new PBEParameterSpec(k.getSalt(), k.getIterationCount());
            }
            else if (params instanceof PBEParameterSpec)
            {
                pbeSpec = (PBEParameterSpec)params;
                param = PBE.Util.makePBEParameters(k, params, cipher.getUnderlyingCipher().getAlgorithmName());
            }
            else
            {
                throw new InvalidAlgorithmParameterException("PBE requires PBE parameters to be set.");
            }

            if (param instanceof ParametersWithIV)
            {
                ivParam = (ParametersWithIV)param;
            }
        }
        else if (params == null)
        {
            param = new KeyParameter(key.getEncoded());
        }
        else if (params instanceof IvParameterSpec)
        {
            if (ivLength != 0)
            {
                IvParameterSpec p = (IvParameterSpec)params;

                if (p.getIV().length != ivLength)
                {
                    throw new InvalidAlgorithmParameterException("IV must be " + ivLength + " bytes long.");
                }

                param = new ParametersWithIV(new KeyParameter(key.getEncoded()), p.getIV());
                ivParam = (ParametersWithIV)param;
            }
            else
            {
                if (modeName != null && modeName.equals("ECB"))
                {
                    throw new InvalidAlgorithmParameterException("ECB mode does not use an IV");
                }
                
                param = new KeyParameter(key.getEncoded());
            }
        }
        // BEGIN android-removed
        // else if (params instanceof GOST28147ParameterSpec)
        // {
        //     GOST28147ParameterSpec    gost28147Param = (GOST28147ParameterSpec)params;
        //
        //     param = new ParametersWithSBox(
        //                new KeyParameter(key.getEncoded()), ((GOST28147ParameterSpec)params).getSbox());
        //
        //     if (gost28147Param.getIV() != null && ivLength != 0)
        //     {
        //         param = new ParametersWithIV(param, gost28147Param.getIV());
        //         ivParam = (ParametersWithIV)param;
        //     }
        // }
        // END android-removed
        else if (params instanceof RC2ParameterSpec)
        {
            RC2ParameterSpec    rc2Param = (RC2ParameterSpec)params;

            param = new RC2Parameters(key.getEncoded(), ((RC2ParameterSpec)params).getEffectiveKeyBits());

            if (rc2Param.getIV() != null && ivLength != 0)
            {
                param = new ParametersWithIV(param, rc2Param.getIV());
                ivParam = (ParametersWithIV)param;
            }
        }
        else if (params instanceof RC5ParameterSpec)
        {
            RC5ParameterSpec    rc5Param = (RC5ParameterSpec)params;

            param = new RC5Parameters(key.getEncoded(), ((RC5ParameterSpec)params).getRounds());
            if (baseEngine.getAlgorithmName().startsWith("RC5"))
            {
                if (baseEngine.getAlgorithmName().equals("RC5-32"))
                {
                    if (rc5Param.getWordSize() != 32)
                    {
                        throw new InvalidAlgorithmParameterException("RC5 already set up for a word size of 32 not " + rc5Param.getWordSize() + ".");
                    }
                }
                else if (baseEngine.getAlgorithmName().equals("RC5-64"))
                {
                    if (rc5Param.getWordSize() != 64)
                    {
                        throw new InvalidAlgorithmParameterException("RC5 already set up for a word size of 64 not " + rc5Param.getWordSize() + ".");
                    }
                }
            }
            else
            {
                throw new InvalidAlgorithmParameterException("RC5 parameters passed to a cipher that is not RC5.");
            }
            if ((rc5Param.getIV() != null) && (ivLength != 0))
            {
                param = new ParametersWithIV(param, rc5Param.getIV());
                ivParam = (ParametersWithIV)param;
            }
        }
        else
        {
            throw new InvalidAlgorithmParameterException("unknown parameter type.");
        }

        if ((ivLength != 0) && !(param instanceof ParametersWithIV))
        {
            SecureRandom    ivRandom = random;

            if (ivRandom == null)
            {
                ivRandom = new SecureRandom();
            }

            if ((opmode == Cipher.ENCRYPT_MODE) || (opmode == Cipher.WRAP_MODE))
            {
                byte[]  iv = new byte[ivLength];

                ivRandom.nextBytes(iv);
                param = new ParametersWithIV(param, iv);
                ivParam = (ParametersWithIV)param;
            }
            else if (cipher.getUnderlyingCipher().getAlgorithmName().indexOf("PGPCFB") < 0)
            {
                throw new InvalidAlgorithmParameterException("no IV set when one expected");
            }
        }

        if (random != null && padded)
        {
            param = new ParametersWithRandom(param, random);
        }

        try
        {
            switch (opmode)
            {
            case Cipher.ENCRYPT_MODE:
            case Cipher.WRAP_MODE:
                cipher.init(true, param);
                break;
            case Cipher.DECRYPT_MODE:
            case Cipher.UNWRAP_MODE:
                cipher.init(false, param);
                break;
            default:
                throw new InvalidParameterException("unknown opmode " + opmode + " passed");
            }
        }
        catch (Exception e)
        {
            throw new InvalidKeyException(e.getMessage());
        }
    }

    protected void engineInit(
        int                 opmode,
        Key                 key,
        AlgorithmParameters params,
        SecureRandom        random) 
    throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        AlgorithmParameterSpec  paramSpec = null;

        if (params != null)
        {
            for (int i = 0; i != availableSpecs.length; i++)
            {
                try
                {
                    paramSpec = params.getParameterSpec(availableSpecs[i]);
                    break;
                }
                catch (Exception e)
                {
                    continue;
                }
            }

            if (paramSpec == null)
            {
                throw new InvalidAlgorithmParameterException("can't handle parameter " + params.toString());
            }
        }

        engineInit(opmode, key, paramSpec, random);
        
        engineParams = params;
    }

    protected void engineInit(
        int                 opmode,
        Key                 key,
        SecureRandom        random) 
        throws InvalidKeyException
    {
        try
        {
            engineInit(opmode, key, (AlgorithmParameterSpec)null, random);
        }
        catch (InvalidAlgorithmParameterException e)
        {
            throw new InvalidKeyException(e.getMessage());
        }
    }

    protected byte[] engineUpdate(
        byte[]  input,
        int     inputOffset,
        int     inputLen) 
    {
        int     length = cipher.getUpdateOutputSize(inputLen);

        if (length > 0)
        {
                byte[]  out = new byte[length];

                int len = cipher.processBytes(input, inputOffset, inputLen, out, 0);

                if (len == 0)
                {
                    return null;
                }
                else if (len != out.length)
                {
                    byte[]  tmp = new byte[len];

                    System.arraycopy(out, 0, tmp, 0, len);

                    return tmp;
                }

                return out;
        }

        cipher.processBytes(input, inputOffset, inputLen, null, 0);

        return null;
    }

    protected int engineUpdate(
        byte[]  input,
        int     inputOffset,
        int     inputLen,
        byte[]  output,
        int     outputOffset)
        throws ShortBufferException
    {
        try
        {
            return cipher.processBytes(input, inputOffset, inputLen, output, outputOffset);
        }
        catch (DataLengthException e)
        {
            throw new ShortBufferException(e.getMessage());
        }
    }

    protected byte[] engineDoFinal(
        byte[]  input,
        int     inputOffset,
        int     inputLen) 
        throws IllegalBlockSizeException, BadPaddingException
    {
        int     len = 0;
        byte[]  tmp = new byte[engineGetOutputSize(inputLen)];

        if (inputLen != 0)
        {
            len = cipher.processBytes(input, inputOffset, inputLen, tmp, 0);
        }

        try
        {
            len += cipher.doFinal(tmp, len);
        }
        catch (DataLengthException e)
        {
            throw new IllegalBlockSizeException(e.getMessage());
        }
        catch (InvalidCipherTextException e)
        {
            throw new BadPaddingException(e.getMessage());
        }

        byte[]  out = new byte[len];

        System.arraycopy(tmp, 0, out, 0, len);

        return out;
    }

    protected int engineDoFinal(
        byte[]  input,
        int     inputOffset,
        int     inputLen,
        byte[]  output,
        int     outputOffset) 
        throws IllegalBlockSizeException, BadPaddingException
    {
        int     len = 0;

        if (inputLen != 0)
        {
                len = cipher.processBytes(input, inputOffset, inputLen, output, outputOffset);
        }

        try
        {
            return (len + cipher.doFinal(output, outputOffset + len));
        }
        catch (DataLengthException e)
        {
            throw new IllegalBlockSizeException(e.getMessage());
        }
        catch (InvalidCipherTextException e)
        {
            throw new BadPaddingException(e.getMessage());
        }
    }

    /*
     * The ciphers that inherit from us.
     */

    /**
     * DES
     */
    static public class DES
        extends JCEBlockCipher
    {
        public DES()
        {
            super(new DESEngine());
        }
    }

    /**
     * DESCBC
     */
    static public class DESCBC
        extends JCEBlockCipher
    {
        public DESCBC()
        {
            super(new CBCBlockCipher(new DESEngine()), 64);
        }
    }

    /**
     * DESede
     */
    static public class DESede
        extends JCEBlockCipher
    {
        public DESede()
        {
            super(new DESedeEngine());
        }
    }

    /**
     * DESedeCBC
     */
    static public class DESedeCBC
        extends JCEBlockCipher
    {
        public DESedeCBC()
        {
            super(new CBCBlockCipher(new DESedeEngine()), 64);
        }
    }

    /**
     *  GOST28147
     */
    // BEGIN android-removed
    // static public class GOST28147
    //     extends JCEBlockCipher
    // {
    //     public GOST28147()
    //     {
    //         super(new GOST28147Engine());
    //     }
    // }
    //
    // static public class GOST28147cbc
    //     extends JCEBlockCipher
    // {
    //     public GOST28147cbc()
    //     {
    //         super(new CBCBlockCipher(new GOST28147Engine()), 64);
    //     }
    // }
    // END android-removed

    /**
     * AES
     */
    static public class AES
        extends JCEBlockCipher
    {
        public AES()
        {
            super(new AESFastEngine());
        }
    }

    /**
     * AESCBC
     */
    static public class AESCBC
        extends JCEBlockCipher
    {
        public AESCBC()
        {
            super(new CBCBlockCipher(new AESFastEngine()), 128);
        }
    }

    /**
     * AESCFB
     */
    static public class AESCFB
        extends JCEBlockCipher
    {
        public AESCFB()
        {
            super(new CFBBlockCipher(new AESFastEngine(), 128), 128);
        }
    }
    
    /**
     * AESOFB
     */
    static public class AESOFB
        extends JCEBlockCipher
    {
        public AESOFB()
        {
            super(new OFBBlockCipher(new AESFastEngine(), 128), 128);
        }
    }

    /**
     * Camellia
     */
    // BEGIN android-removed
    // static public class Camellia
    //     extends JCEBlockCipher
    // {
    //     public Camellia()
    //     {
    //         super(new CamelliaEngine());
    //     }
    // }
    // END android-removed
    
    /**
     * CAST5
     */
    // BEGIN android-removed
    // static public class CAST5
    //     extends JCEBlockCipher
    // {
    //     public CAST5()
    //     {
    //         super(new CAST5Engine());
    //     }
    // }
    // END android-removed

    /**
     * CAST5 CBC
     */
    // BEGIN android-removed
    // static public class CAST5CBC
    //     extends JCEBlockCipher
    // {
    //     public CAST5CBC()
    //     {
    //         super(new CBCBlockCipher(new CAST5Engine()), 64);
    //     }
    // }
    // END android-removed

    /**
     * CAST6
     */
    // BEGIN android-removed
    // static public class CAST6
    //     extends JCEBlockCipher
    // {
    //     public CAST6()
    //     {
    //         super(new CAST6Engine());
    //     }
    // }
    // BEGIN android-removed

    /**
     * PBEWithMD5AndDES
     */
    static public class PBEWithMD5AndDES
        extends JCEBlockCipher
    {
        public PBEWithMD5AndDES()
        {
            super(new CBCBlockCipher(new DESEngine()));
        }
    }

    /**
     * PBEWithSHA1AndDES
     */
    static public class PBEWithSHA1AndDES
        extends JCEBlockCipher
    {
        public PBEWithSHA1AndDES()
        {
            super(new CBCBlockCipher(new DESEngine()));
        }
    }

    /**
     * PBEWithSHAAnd3-KeyTripleDES-CBC
     */
    static public class PBEWithSHAAndDES3Key
        extends JCEBlockCipher
    {
        public PBEWithSHAAndDES3Key()
        {
            super(new CBCBlockCipher(new DESedeEngine()));
        }
    }

    /**
     * PBEWithSHAAnd2-KeyTripleDES-CBC
     */
    static public class PBEWithSHAAndDES2Key
        extends JCEBlockCipher
    {
        public PBEWithSHAAndDES2Key()
        {
            super(new CBCBlockCipher(new DESedeEngine()));
        }
    }
    
    /**
     * PBEWithAES-CBC
     */
    static public class PBEWithAESCBC
        extends JCEBlockCipher
    {
        public PBEWithAESCBC()
        {
            super(new CBCBlockCipher(new AESFastEngine()));
        }
    }
}
