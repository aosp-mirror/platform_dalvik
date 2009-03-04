package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;

public class CCMParameters
    implements CipherParameters
{
    private byte[] associatedText;
    private byte[] nonce;
    private KeyParameter key;
    private int macSize;

    /**
     * Base constructor.
     * 
     * @param key key to be used by underlying cipher
     * @param macSize macSize in bits
     * @param nonce nonce to be used
     * @param associatedText associated text, if any
     */
    public CCMParameters(KeyParameter key, int macSize, byte[] nonce, byte[] associatedText)
    {
        this.key = key;
        this.nonce = nonce;
        this.macSize = macSize;
        this.associatedText = associatedText;
    }
    
    public KeyParameter getKey()
    {
        return key;
    }
    
    public int getMacSize()
    {
        return macSize;
    }
    
    public byte[] getAssociatedText()
    {
        return associatedText;
    }
    
    public byte[] getNonce()
    {
        return nonce;
    }
}
