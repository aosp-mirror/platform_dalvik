package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.params.CCMParameters;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Implements the Counter with Cipher Block Chaining mode (CCM) detailed in
 * NIST Special Publication 800-38C.
 * <p>
 * <b>Note</b>: this mode is a packet mode - it needs all the data up front.
 */
public class CCMBlockCipher
{
    private BlockCipher     cipher = null;
    private int             blockSize;
    private boolean         forEncryption;
    private CCMParameters   params;
    private byte[]          macBlock;


    /**
     * Basic constructor.
     *
     * @param c the block cipher to be used.
     */
    public CCMBlockCipher(BlockCipher c)
    {
        this.cipher = c;
        this.blockSize = c.getBlockSize();
        this.macBlock = new byte[blockSize];
        
        if (blockSize != 16)
        {
            throw new IllegalArgumentException("cipher required with a block size of 16.");
        }
    }

    /**
     * return the underlying block cipher that we are wrapping.
     *
     * @return the underlying block cipher that we are wrapping.
     */
    public BlockCipher getUnderlyingCipher()
    {
        return cipher;
    }


    public void init(boolean forEncryption, CipherParameters params)
          throws IllegalArgumentException
    {
        if (!(params instanceof CCMParameters))
        {
            throw new IllegalArgumentException("parameters need to be CCMParameters");
        }
        
        this.forEncryption = forEncryption;
        this.params = (CCMParameters)params;
    }

    public String getAlgorithmName()
    {
        return cipher.getAlgorithmName() + "/CCM";
    }
    
    /**
     * Returns a byte array containing the mac calculated as part of the
     * last encrypt or decrypt operation.
     * 
     * @return the last mac calculated.
     */
    public byte[] getMac()
    {
        byte[] mac = new byte[params.getMacSize() / 8];
        
        System.arraycopy(macBlock, 0, mac, 0, mac.length);
        
        return mac;
    }

    public byte[] processPacket(byte[] in, int inOff, int inLen)
        throws IllegalStateException, InvalidCipherTextException
    {
        if (params == null)
        {
            throw new IllegalStateException("CCM cipher unitialized.");
        }
        
        BlockCipher ctrCipher = new SICBlockCipher(cipher);
        byte[] iv = new byte[blockSize];
        byte[] nonce = params.getNonce();
        int    macSize = params.getMacSize() / 8;
        byte[] out;

        iv[0] = (byte)(((15 - nonce.length) - 1) & 0x7);
        
        System.arraycopy(nonce, 0, iv, 1, nonce.length);
        
        ctrCipher.init(forEncryption, new ParametersWithIV(params.getKey(), iv));
        
        if (forEncryption)
        {
            int index = inOff;
            int outOff = 0;
            
            out = new byte[inLen + macSize];
            
            calculateMac(in, inOff, inLen, macBlock);
            
            ctrCipher.processBlock(macBlock, 0, macBlock, 0);   // S0
            
            while (index < inLen - blockSize)                   // S1...
            {
                ctrCipher.processBlock(in, index, out, outOff);
                outOff += blockSize;
                index += blockSize;
            }
            
            byte[] block = new byte[blockSize];
            
            System.arraycopy(in, index, block, 0, inLen - index);
            
            ctrCipher.processBlock(block, 0, block, 0);
            
            System.arraycopy(block, 0, out, outOff, inLen - index);
            
            outOff += inLen - index;

            System.arraycopy(macBlock, 0, out, outOff, out.length - outOff);
        }
        else
        {
            int index = inOff;
            int outOff = 0;
            
            out = new byte[inLen - macSize];
            
            System.arraycopy(in, inOff + inLen - macSize, macBlock, 0, macSize);
            
            ctrCipher.processBlock(macBlock, 0, macBlock, 0);
            
            for (int i = macSize; i != macBlock.length; i++)
            {
                macBlock[i] = 0;
            }
            
            while (outOff < out.length - blockSize)
            {
                ctrCipher.processBlock(in, index, out, outOff);
                outOff += blockSize;
                index += blockSize;
            }
            
            byte[] block = new byte[blockSize];
            
            System.arraycopy(in, index, block, 0, out.length - outOff);
            
            ctrCipher.processBlock(block, 0, block, 0);
            
            System.arraycopy(block, 0, out, outOff, out.length - outOff);
            
            byte[] calculatedMacBlock = new byte[blockSize];
            
            calculateMac(out, 0, out.length, calculatedMacBlock);
            
            if (!areEqual(macBlock, calculatedMacBlock))
            {
                throw new InvalidCipherTextException("mac check in CCM failed");
            }
        }
        
        return out;
    }
    
    private int calculateMac(byte[] data, int dataOff, int dataLen, byte[] macBlock)
    {
        Mac    cMac = new CBCBlockCipherMac(cipher, params.getMacSize());

        byte[] nonce = params.getNonce();
        byte[] associatedText = params.getAssociatedText();
        
        cMac.init(params.getKey());

        //
        // build b0
        //
        byte[] b0 = new byte[16];
    
        if (associatedText != null && associatedText.length != 0)
        {
            b0[0] |= 0x40;
        }
        
        b0[0] |= (((cMac.getMacSize() - 2) / 2) & 0x7) << 3;

        b0[0] |= ((15 - nonce.length) - 1) & 0x7;
        
        System.arraycopy(nonce, 0, b0, 1, nonce.length);
        
        int q = dataLen;
        int count = 1;
        while (q > 0)
        {
            b0[b0.length - count] = (byte)(q & 0xff);
            q >>>= 8;
            count++;
        }
        
        cMac.update(b0, 0, b0.length);
        
        //
        // process associated text
        //
        if (associatedText != null)
        {
            int extra;
            
            if (associatedText.length < ((1 << 16) - (1 << 8)))
            {
                cMac.update((byte)(associatedText.length >> 8));
                cMac.update((byte)associatedText.length);
                
                extra = 2;
            }
            else // can't go any higher than 2^32
            {
                cMac.update((byte)0xff);
                cMac.update((byte)0xfe);
                cMac.update((byte)(associatedText.length >> 24));
                cMac.update((byte)(associatedText.length >> 16));
                cMac.update((byte)(associatedText.length >> 8));
                cMac.update((byte)associatedText.length);
                
                extra = 6;
            }
            
            cMac.update(associatedText, 0, associatedText.length);
            
            extra = (extra + associatedText.length) % 16;
            if (extra != 0)
            {
                for (int i = 0; i != 16 - extra; i++)
                {
                    cMac.update((byte)0x00);
                }
            }
        }
        
        //
        // add the text
        //
        cMac.update(data, dataOff, dataLen);

        return cMac.doFinal(macBlock, 0);
    }
    
    /**
     * compare two byte arrays.
     */
    private boolean areEqual(
        byte[]    a,
        byte[]    b)
    {
        if (a.length != b.length)
        {
            return false;
        }
        
        for (int i = 0; i != b.length; i++)
        {
            if (a[i] != b[i])
            {
                return false;
            }
        }
        
        return true;
    }
}
