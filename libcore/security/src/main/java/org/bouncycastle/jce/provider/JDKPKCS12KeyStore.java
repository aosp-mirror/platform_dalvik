package org.bouncycastle.jce.provider;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.BERConstructedOctetString;
import org.bouncycastle.asn1.BEROutputStream;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.AuthenticatedSafe;
import org.bouncycastle.asn1.pkcs.CertBag;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.EncryptedData;
import org.bouncycastle.asn1.pkcs.MacData;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.Pfx;
import org.bouncycastle.asn1.pkcs.SafeBag;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.jce.interfaces.BCKeyStore;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.util.encoders.Hex;

public class JDKPKCS12KeyStore
    extends KeyStoreSpi
    implements PKCSObjectIdentifiers, X509ObjectIdentifiers, BCKeyStore
{
    private static final int    SALT_SIZE = 20;
    private static final int    MIN_ITERATIONS = 100;
    
    //
    // SHA-1 and 3-key-triple DES.
    //
    private static final String KEY_ALGORITHM = "1.2.840.113549.1.12.1.3";

    //
    // SHA-1 and 40 bit RC2.
    //
    private static final String CERT_ALGORITHM = "1.2.840.113549.1.12.1.6";

    private Hashtable                       keys = new Hashtable();
    private Hashtable                       localIds = new Hashtable();
    private Hashtable                       certs = new Hashtable();
    private Hashtable                       chainCerts = new Hashtable();
    private Hashtable                       keyCerts = new Hashtable();

    //
    // generic object types
    //
    static final int NULL           = 0;
    static final int CERTIFICATE    = 1;
    static final int KEY            = 2;
    static final int SECRET         = 3;
    static final int SEALED         = 4;

    //
    // key types
    //
    static final int    KEY_PRIVATE = 0;
    static final int    KEY_PUBLIC  = 1;
    static final int    KEY_SECRET  = 2;

    protected SecureRandom      random = new SecureRandom();

    private CertificateFactory  certFact = null;

    private class CertId
    {
        byte[]  id;

        CertId(
            PublicKey  key)
        {
            this.id = createSubjectKeyId(key).getKeyIdentifier();
        }

        CertId(
            byte[]  id)
        {
            this.id = id;
        }

        public int hashCode()
        {
            int hash = id[0] & 0xff;

            for (int i = 1; i != id.length - 4; i++)
            {
                hash ^= ((id[i] & 0xff) << 24) | ((id[i + 1] & 0xff) << 16)
                          | ((id[i + 2] & 0xff) << 8) | (id[i + 3] & 0xff);
            }

            return hash;
        }

        public boolean equals(
            Object  o)
        {
            if (!(o instanceof CertId))
            {
                return false;
            }

            CertId  cId = (CertId)o;

            if (cId.id.length != id.length)
            {
                return false;
            }

            for (int i = 0; i != id.length; i++)
            {
                if (cId.id[i] != id[i])
                {
                    return false;
                }
            }

            return true;
        }
    }

    public JDKPKCS12KeyStore(
        String provider)
    {
        try
        {
            if (provider != null)
            {
                certFact = CertificateFactory.getInstance("X.509", provider);
            }
            else
            {
                certFact = CertificateFactory.getInstance("X.509");
            }
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("can't create cert factory - " + e.toString());
        }
    }

    private SubjectKeyIdentifier createSubjectKeyId(
        PublicKey   pubKey)
    {
        try
        {
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                (ASN1Sequence)new ASN1InputStream(pubKey.getEncoded()).readObject());

            return new SubjectKeyIdentifier(info);
        }
        catch (Exception e)
        {
            throw new RuntimeException("error creating key");
        }
    }

    public void setRandom(
        SecureRandom    rand)
    {
        this.random = rand;
    }

    public Enumeration engineAliases() 
    {
        Hashtable  tab = new Hashtable();

        Enumeration e = certs.keys();
        while (e.hasMoreElements())
        {
            tab.put(e.nextElement(), "cert");
        }

        e = keys.keys();
        while (e.hasMoreElements())
        {
            String  a = (String)e.nextElement();
            if (tab.get(a) == null)
            {
                tab.put(a, "key");
            }
        }

        return tab.keys();
    }

    public boolean engineContainsAlias(
        String  alias) 
    {
        return (certs.get(alias) != null || keys.get(alias) != null);
    }

    /**
     * this is quite complete - we should follow up on the chain, a bit
     * tricky if a certificate appears in more than one chain...
     */
    public void engineDeleteEntry(
        String  alias) 
        throws KeyStoreException
    {
        Key k = (Key)keys.remove(alias);

        Certificate c = (Certificate)certs.remove(alias);

        if (c != null)
        {
            chainCerts.remove(new CertId(c.getPublicKey()));
        }

        if (k != null)
        {
            String  id = (String)localIds.remove(alias);
            if (id != null)
            {
                c = (Certificate)keyCerts.remove(id);
            }
            if (c != null)
            {
                chainCerts.remove(new CertId(c.getPublicKey()));
            }
        }

        if (c == null && k == null)
        {
            throw new KeyStoreException("no such entry as " + alias);
        }
    }

    /**
     * simply return the cert for the private key
     */
    public Certificate engineGetCertificate(
        String alias) 
    {
        if (alias == null)
        {
            throw new IllegalArgumentException("null alias passed to getCertificate.");
        }
        
        Certificate c = (Certificate)certs.get(alias);

        //
        // look up the key table - and try the local key id
        //
        if (c == null)
        {
            String  id = (String)localIds.get(alias);
            if (id != null)
            {
                c = (Certificate)keyCerts.get(id);
            }
            else
            {
                c = (Certificate)keyCerts.get(alias);
            }
        }

        return c;
    }

    public String engineGetCertificateAlias(
        Certificate cert) 
    {
        Enumeration c = certs.elements();
        Enumeration k = certs.keys();

        while (c.hasMoreElements())
        {
            Certificate tc = (Certificate)c.nextElement();
            String      ta = (String)k.nextElement();

            if (tc.equals(cert))
            {
                return ta;
            }
        }

        c = keyCerts.elements();
        k = keyCerts.keys();

        while (c.hasMoreElements())
        {
            Certificate tc = (Certificate)c.nextElement();
            String      ta = (String)k.nextElement();

            if (tc.equals(cert))
            {
                return ta;
            }
        }
        
        return null;
    }
    
    public Certificate[] engineGetCertificateChain(
        String alias) 
    {
        if (alias == null)
        {
            throw new IllegalArgumentException("null alias passed to getCertificateChain.");
        }
        
        if (!engineIsKeyEntry(alias))
        {
            return null;
        }
        
        Certificate c = engineGetCertificate(alias);

        if (c != null)
        {
            Vector  cs = new Vector();

            while (c != null)
            {
                X509Certificate     x509c = (X509Certificate)c;
                Certificate         nextC = null;

                byte[]  bytes = x509c.getExtensionValue(X509Extensions.AuthorityKeyIdentifier.getId());
                if (bytes != null)
                {
                    try
                    {
                        ASN1InputStream         aIn = new ASN1InputStream(bytes);

                        byte[] authBytes = ((ASN1OctetString)aIn.readObject()).getOctets();
                        aIn = new ASN1InputStream(authBytes);

                        AuthorityKeyIdentifier id = new AuthorityKeyIdentifier((ASN1Sequence)aIn.readObject());
                        if (id.getKeyIdentifier() != null)
                        {
                            nextC = (Certificate)chainCerts.get(new CertId(id.getKeyIdentifier()));
                        }
                        
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e.toString());
                    }
                }

                if (nextC == null)
                {
                    //
                    // no authority key id, try the Issuer DN
                    //
                    Principal  i = x509c.getIssuerDN();
                    Principal  s = x509c.getSubjectDN();

                    if (!i.equals(s))
                    {
                        Enumeration e = chainCerts.keys();

                        while (e.hasMoreElements())
                        {
                            X509Certificate crt = (X509Certificate)chainCerts.get(e.nextElement());
                            Principal  sub = crt.getSubjectDN();
                            if (sub.equals(i))
                            {
                                try
                                {
                                    x509c.verify(crt.getPublicKey());
                                    nextC = crt;
                                    break;
                                }
                                catch (Exception ex)
                                {
                                    // continue
                                }
                            }
                        }
                    }
                }

                cs.addElement(c);
                if (nextC != c)     // self signed - end of the chain
                {
                    c = nextC;
                }
                else
                {
                    c = null;
                }
            }

            Certificate[]   certChain = new Certificate[cs.size()];

            for (int i = 0; i != certChain.length; i++)
            {
                certChain[i] = (Certificate)cs.elementAt(i);
            }

            return certChain;
        }

        return null;
    }
    
    public Date engineGetCreationDate(String alias) 
    {
        return new Date();
    }

    public Key engineGetKey(
        String alias,
        char[] password) 
        throws NoSuchAlgorithmException, UnrecoverableKeyException
    {
        if (alias == null)
        {
            throw new IllegalArgumentException("null alias passed to getKey.");
        }
        
        return (Key)keys.get(alias);
    }

    public boolean engineIsCertificateEntry(
        String alias) 
    {
        return (certs.get(alias) != null && keys.get(alias) == null);
    }

    public boolean engineIsKeyEntry(
        String alias) 
    {
        return (keys.get(alias) != null);
    }

    public void engineSetCertificateEntry(
        String      alias,
        Certificate cert) 
        throws KeyStoreException
    {
        if (certs.get(alias) != null)
        {
            throw new KeyStoreException("There is already a certificate with the name " + alias + ".");
        }

        certs.put(alias, cert);
        chainCerts.put(new CertId(cert.getPublicKey()), cert);
    }

    public void engineSetKeyEntry(
        String alias,
        byte[] key,
        Certificate[] chain) 
        throws KeyStoreException
    {
        throw new RuntimeException("operation not supported");
    }

    public void engineSetKeyEntry(
        String          alias,
        Key             key,
        char[]          password,
        Certificate[]   chain) 
        throws KeyStoreException
    {
        if ((key instanceof PrivateKey) && (chain == null))
        {
            throw new KeyStoreException("no certificate chain for private key");
        }

        if (keys.get(alias) != null && !key.equals(keys.get(alias)))
        {
            throw new KeyStoreException("There is already a key with the name " + alias + ".");
        }

        keys.put(alias, key);
        certs.put(alias, chain[0]);

        for (int i = 0; i != chain.length; i++)
        {
            chainCerts.put(new CertId(chain[i].getPublicKey()), chain[i]);
        }
    }

    public int engineSize() 
    {
        Hashtable  tab = new Hashtable();

        Enumeration e = certs.keys();
        while (e.hasMoreElements())
        {
            tab.put(e.nextElement(), "cert");
        }

        e = keys.keys();
        while (e.hasMoreElements())
        {
            String  a = (String)e.nextElement();
            if (tab.get(a) == null)
            {
                tab.put(a, "key");
            }
        }

        return tab.size();
    }

    protected PrivateKey unwrapKey(
        AlgorithmIdentifier   algId,
        byte[]                data,
        char[]                password,
        boolean               wrongPKCS12Zero)
        throws IOException
    {
        String              algorithm = algId.getObjectId().getId();
        PKCS12PBEParams     pbeParams = new PKCS12PBEParams((ASN1Sequence)algId.getParameters());

        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        PrivateKey          out = null;

        try
        {
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                algorithm, "BC");
            PBEParameterSpec    defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

            SecretKey           k = keyFact.generateSecret(pbeSpec);
            
            ((JCEPBEKey)k).setTryWrongPKCS12Zero(wrongPKCS12Zero);

            Cipher cipher = Cipher.getInstance(algorithm, "BC");

            cipher.init(Cipher.UNWRAP_MODE, k, defParams);

            // we pass "" as the key algorithm type as it is unknown at this point
            out = (PrivateKey)cipher.unwrap(data, "", Cipher.PRIVATE_KEY);
        }
        catch (Exception e)
        {
            throw new IOException("exception unwrapping private key - " + e.toString());
        }

        return out;
    }

    protected byte[] wrapKey(
        String                  algorithm,
        Key                     key,
        PKCS12PBEParams         pbeParams,
        char[]                  password)
        throws IOException
    {
        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        byte[]              out;

        try
        {
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                algorithm, "BC");
            PBEParameterSpec    defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

            Cipher cipher = Cipher.getInstance(algorithm, "BC");

            cipher.init(Cipher.WRAP_MODE, keyFact.generateSecret(pbeSpec), defParams);

            out = cipher.wrap(key);
        }
        catch (Exception e)
        {
            throw new IOException("exception encrypting data - " + e.toString());
        }

        return out;
    }

    protected ASN1Sequence decryptData(
        AlgorithmIdentifier   algId,
        byte[]                data,
        char[]                password,
        boolean               wrongPKCS12Zero)
        throws IOException
    {
        String              algorithm = algId.getObjectId().getId();
        PKCS12PBEParams     pbeParams = new PKCS12PBEParams((ASN1Sequence)algId.getParameters());

        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        byte[]              out = null;

        try
        {
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                algorithm, "BC");
            PBEParameterSpec    defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());
            SecretKey           k = keyFact.generateSecret(pbeSpec);
            
            ((JCEPBEKey)k).setTryWrongPKCS12Zero(wrongPKCS12Zero);

            Cipher cipher = Cipher.getInstance(algorithm, "BC");

            cipher.init(Cipher.DECRYPT_MODE, k, defParams);

            out = cipher.doFinal(data);
        }
        catch (Exception e)
        {
            throw new IOException("exception decrypting data - " + e.toString());
        }

        ASN1InputStream  aIn = new ASN1InputStream(out);

        return (ASN1Sequence)aIn.readObject();
    }

    protected byte[] encryptData(
        String                  algorithm,
        byte[]                  data,
        PKCS12PBEParams         pbeParams,
        char[]                  password)
        throws IOException
    {
        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        byte[]              out;

        try
        {
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                algorithm, "BC");
            PBEParameterSpec    defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

            Cipher cipher = Cipher.getInstance(algorithm, "BC");

            cipher.init(Cipher.ENCRYPT_MODE, keyFact.generateSecret(pbeSpec), defParams);

            out = cipher.doFinal(data);
        }
        catch (Exception e)
        {
            throw new IOException("exception encrypting data - " + e.toString());
        }

        return out;
    }

    public void engineLoad(
        InputStream stream,
        char[]      password) 
        throws IOException
    {
        if (stream == null)     // just initialising
        {
            return;
        }

        if (password == null)
        {
            throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
        }

        // BEGIN android-modified
        BufferedInputStream             bufIn = new BufferedInputStream(stream, 8192);
        // END android-modified

        bufIn.mark(10);

        int head = bufIn.read();

        if (head != 0x30)
        {
            throw new IOException("stream does not represent a PKCS12 key store");
        }

        bufIn.reset();

        ASN1InputStream bIn = new ASN1InputStream(bufIn);
        ASN1Sequence    obj = (ASN1Sequence)bIn.readObject();
        Pfx             bag = new Pfx(obj);
        ContentInfo     info = bag.getAuthSafe();
        Vector          chain = new Vector();
        boolean         unmarkedKey = false;
        boolean         wrongPKCS12Zero = false;

        if (bag.getMacData() != null)           // check the mac code
        {
            ByteArrayOutputStream       bOut = new ByteArrayOutputStream();
            BEROutputStream             berOut = new BEROutputStream(bOut);
            MacData                     mData = bag.getMacData();
            DigestInfo                  dInfo = mData.getMac();
            AlgorithmIdentifier         algId = dInfo.getAlgorithmId();
            byte[]                      salt = mData.getSalt();
            int                         itCount = mData.getIterationCount().intValue();
        
            berOut.writeObject(info);

            byte[]  data = ((ASN1OctetString)info.getContent()).getOctets();

            try
            {
                Mac                 mac = Mac.getInstance(algId.getObjectId().getId(), "BC");
                SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(algId.getObjectId().getId(), "BC");
                PBEParameterSpec    defParams = new PBEParameterSpec(salt, itCount);
                PBEKeySpec          pbeSpec = new PBEKeySpec(password);

                mac.init(keyFact.generateSecret(pbeSpec), defParams);

                mac.update(data);

                byte[]  res = mac.doFinal();
                byte[]  dig = dInfo.getDigest();

                if (res.length != dInfo.getDigest().length)
                {
                    throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                }

                boolean okay = true;
                
                for (int i = 0; i != res.length; i++)
                {
                    if (res[i] != dig[i])
                    {
                        if (password.length != 0)  // may be dodgey zero password
                        {
                            throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                        }
                        else
                        {
                            okay = false;
                            break;
                        }
                    }
                }
                
                //
                // may be incorrect zero length password
                //
                if (!okay)
                {
                    SecretKey k = keyFact.generateSecret(pbeSpec);
                    
                    ((JCEPBEKey)k).setTryWrongPKCS12Zero(true);
                    
                    mac.init(k, defParams);
    
                    mac.update(data);
    
                    res = mac.doFinal();
                    dig = dInfo.getDigest();
                    
                    for (int i = 0; i != res.length; i++)
                    {
                        if (res[i] != dig[i])
                        {
                           throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                        }
                    }
                    
                    wrongPKCS12Zero = true;
                }
            }
            catch (IOException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new IOException("error constructing MAC: " + e.toString());
            }
        }

        keys = new Hashtable();
        localIds = new Hashtable();

        if (info.getContentType().equals(data))
        {
            bIn = new ASN1InputStream(((ASN1OctetString)info.getContent()).getOctets());

            AuthenticatedSafe   authSafe = new AuthenticatedSafe((ASN1Sequence)bIn.readObject());
            ContentInfo[]       c = authSafe.getContentInfo();

            for (int i = 0; i != c.length; i++)
            {
                if (c[i].getContentType().equals(data))
                {
                    ASN1InputStream dIn = new ASN1InputStream(((ASN1OctetString)c[i].getContent()).getOctets());
                    ASN1Sequence    seq = (ASN1Sequence)dIn.readObject();

                    for (int j = 0; j != seq.size(); j++)
                    {
                        SafeBag b = new SafeBag((ASN1Sequence)seq.getObjectAt(j));
                        if (b.getBagId().equals(pkcs8ShroudedKeyBag))
                        {
                            org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo eIn = new org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo((ASN1Sequence)b.getBagValue());
                            PrivateKey              privKey = unwrapKey(eIn.getEncryptionAlgorithm(), eIn.getEncryptedData(), password, wrongPKCS12Zero);

                            //
                            // set the attributes on the key
                            //
                            PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)privKey;
                            String                                   alias = null;
                            ASN1OctetString                   localId = null;

                            if (b.getBagAttributes() != null)
                            {
                                Enumeration e = b.getBagAttributes().getObjects();
                                while (e.hasMoreElements())
                                {
                                    ASN1Sequence  sq = (ASN1Sequence)e.nextElement();
                                    DERObjectIdentifier     aOid = (DERObjectIdentifier)sq.getObjectAt(0);
                                    ASN1Set                 attrSet = (ASN1Set)sq.getObjectAt(1);
                                    DERObject               attr = null;
    
                                    if (attrSet.size() > 0)
                                    {
                                        attr = (DERObject)attrSet.getObjectAt(0);
    
                                        bagAttr.setBagAttribute(aOid, attr);
                                    }
    
                                    if (aOid.equals(pkcs_9_at_friendlyName))
                                    {
                                        alias = ((DERBMPString)attr).getString();
                                        keys.put(alias, privKey);
                                    }
                                    else if (aOid.equals(pkcs_9_at_localKeyId))
                                    {
                                        localId = (ASN1OctetString)attr;
                                    }
                                }
                            }
                        
                            if (localId != null)
                            {
                                String name = new String(Hex.encode(localId.getOctets()));
    
                                if (alias == null)
                                {
                                    keys.put(name, privKey);
                                }
                                else
                                {
                                    localIds.put(alias, name);
                                }
                             }
                             else
                             {
                                 unmarkedKey = true;
                                 keys.put("unmarked", privKey);
                             }
                        }
                        else if (b.getBagId().equals(certBag))
                        {
                            chain.addElement(b);
                        }
                        else
                        {
                            System.out.println("extra in data " + b.getBagId());
                            System.out.println(ASN1Dump.dumpAsString(b));
                        }
                    }
                }
                else if (c[i].getContentType().equals(encryptedData))
                {
                    EncryptedData d = new EncryptedData((ASN1Sequence)c[i].getContent());
                    ASN1Sequence seq = decryptData(d.getEncryptionAlgorithm(), d.getContent().getOctets(), password, wrongPKCS12Zero);

                    for (int j = 0; j != seq.size(); j++)
                    {
                        SafeBag b = new SafeBag((ASN1Sequence)seq.getObjectAt(j));
                        
                        if (b.getBagId().equals(certBag))
                        {
                            chain.addElement(b);
                        }
                        else if (b.getBagId().equals(pkcs8ShroudedKeyBag))
                        {
                            org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo eIn = new org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo((ASN1Sequence)b.getBagValue());
                            PrivateKey              privKey = unwrapKey(eIn.getEncryptionAlgorithm(), eIn.getEncryptedData(), password, wrongPKCS12Zero);

                            //
                            // set the attributes on the key
                            //
                            PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)privKey;
                            String                      alias = null;
                            ASN1OctetString              localId = null;

                            Enumeration e = b.getBagAttributes().getObjects();
                            while (e.hasMoreElements())
                            {
                                ASN1Sequence  sq = (ASN1Sequence)e.nextElement();
                                DERObjectIdentifier     aOid = (DERObjectIdentifier)sq.getObjectAt(0);
                                ASN1Set                 attrSet= (ASN1Set)sq.getObjectAt(1);
                                DERObject               attr = null;

                                if (attrSet.size() > 0)
                                {
                                    attr = (DERObject)attrSet.getObjectAt(0);

                                    bagAttr.setBagAttribute(aOid, attr);
                                }

                                if (aOid.equals(pkcs_9_at_friendlyName))
                                {
                                    alias = ((DERBMPString)attr).getString();
                                    keys.put(alias, privKey);
                                }
                                else if (aOid.equals(pkcs_9_at_localKeyId))
                                {
                                    localId = (ASN1OctetString)attr;
                                }
                            }

                            String name = new String(Hex.encode(localId.getOctets()));

                            if (alias == null)
                            {
                                keys.put(name, privKey);
                            }
                            else
                            {
                                localIds.put(alias, name);
                            }
                        }
                        else if (b.getBagId().equals(keyBag))
                        {
                            org.bouncycastle.asn1.pkcs.PrivateKeyInfo pIn = new org.bouncycastle.asn1.pkcs.PrivateKeyInfo((ASN1Sequence)b.getBagValue());
                            PrivateKey              privKey = JDKKeyFactory.createPrivateKeyFromPrivateKeyInfo(pIn);

                            //
                            // set the attributes on the key
                            //
                            PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)privKey;
                            String                      alias = null;
                            ASN1OctetString             localId = null;

                            Enumeration e = b.getBagAttributes().getObjects();
                            while (e.hasMoreElements())
                            {
                                ASN1Sequence  sq = (ASN1Sequence)e.nextElement();
                                DERObjectIdentifier     aOid = (DERObjectIdentifier)sq.getObjectAt(0);
                                ASN1Set                 attrSet = (ASN1Set)sq.getObjectAt(1);
                                DERObject   attr = null;

                                if (attrSet.size() > 0)
                                {
                                    attr = (DERObject)attrSet.getObjectAt(0);

                                    bagAttr.setBagAttribute(aOid, attr);
                                }

                                if (aOid.equals(pkcs_9_at_friendlyName))
                                {
                                    alias = ((DERBMPString)attr).getString();
                                    keys.put(alias, privKey);
                                }
                                else if (aOid.equals(pkcs_9_at_localKeyId))
                                {
                                    localId = (ASN1OctetString)attr;
                                }
                            }

                            String name = new String(Hex.encode(localId.getOctets()));

                            if (alias == null)
                            {
                                keys.put(name, privKey);
                            }
                            else
                            {
                                localIds.put(alias, name);
                            }
                        }
                        else
                        {
                            System.out.println("extra in encryptedData " + b.getBagId());
                            System.out.println(ASN1Dump.dumpAsString(b));
                        }
                    }
                }
                else
                {
                    System.out.println("extra " + c[i].getContentType().getId());
                    System.out.println("extra " + ASN1Dump.dumpAsString(c[i].getContent()));
                }
            }
        }

        certs = new Hashtable();
        chainCerts = new Hashtable();
        keyCerts = new Hashtable();

        for (int i = 0; i != chain.size(); i++)
        {
            SafeBag     b = (SafeBag)chain.elementAt(i);
            CertBag     cb = new CertBag((ASN1Sequence)b.getBagValue());
            Certificate cert = null;

            try
            {
                ByteArrayInputStream  cIn = new ByteArrayInputStream(
                                ((ASN1OctetString)cb.getCertValue()).getOctets());
                cert = certFact.generateCertificate(cIn);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.toString());
            }


            //
            // set the attributes
            //
            ASN1OctetString              localId = null;
            String                      alias = null;

            if (b.getBagAttributes() != null)
            {
                Enumeration e = b.getBagAttributes().getObjects();
                while (e.hasMoreElements())
                {
                    ASN1Sequence  sq = (ASN1Sequence)e.nextElement();
                    DERObjectIdentifier     oid = (DERObjectIdentifier)sq.getObjectAt(0);
                    DERObject               attr = (DERObject)((ASN1Set)sq.getObjectAt(1)).getObjectAt(0);

                    if (cert instanceof PKCS12BagAttributeCarrier)
                    {
                        PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)cert;
                        bagAttr.setBagAttribute(oid, attr);
                    }

                    if (oid.equals(pkcs_9_at_friendlyName))
                    {
                        alias = ((DERBMPString)attr).getString();
                    }
                    else if (oid.equals(pkcs_9_at_localKeyId))
                    {
                        localId = (ASN1OctetString)attr;
                    }
                }
            }

            chainCerts.put(new CertId(cert.getPublicKey()), cert);

            if (unmarkedKey)
            {
                if (keyCerts.isEmpty())
                {
                    String    name = new String(Hex.encode(createSubjectKeyId(cert.getPublicKey()).getKeyIdentifier()));
                    
                    keyCerts.put(name, cert);
                    keys.put(name, keys.remove("unmarked"));
                }
            }
            else
            {
                //
                // the local key id needs to override the friendly name
                //
                if (localId != null)
                {
                    String name = new String(Hex.encode(localId.getOctets()));

                    keyCerts.put(name, cert);
                }
                if (alias != null)
                {
                    certs.put(alias, cert);
                }
            }
        }
    }

    public void engineStore(OutputStream stream, char[] password) 
        throws IOException
    {
        if (password == null)
        {
            throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
        }

        ContentInfo[]   c = new ContentInfo[2];


        //
        // handle the key
        //
        ASN1EncodableVector  keyS = new ASN1EncodableVector();


        Enumeration ks = keys.keys();

        while (ks.hasMoreElements())
        {
            byte[]                  kSalt = new byte[SALT_SIZE];

            random.nextBytes(kSalt);

            String                  name = (String)ks.nextElement();
            PrivateKey              privKey = (PrivateKey)keys.get(name);
            PKCS12PBEParams         kParams = new PKCS12PBEParams(kSalt, MIN_ITERATIONS);
            byte[]                  kBytes = wrapKey(KEY_ALGORITHM, privKey, kParams, password);
            AlgorithmIdentifier     kAlgId = new AlgorithmIdentifier(new DERObjectIdentifier(KEY_ALGORITHM), kParams.getDERObject());
            org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo kInfo = new org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo(kAlgId, kBytes);
            boolean                 attrSet = false;
            ASN1EncodableVector     kName = new ASN1EncodableVector();

            if (privKey instanceof PKCS12BagAttributeCarrier)
            {
                PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)privKey;
                //
                // make sure we are using the local alias on store
                //
                DERBMPString    nm = (DERBMPString)bagAttrs.getBagAttribute(pkcs_9_at_friendlyName);
                if (nm == null || !nm.getString().equals(name))
                {
                    bagAttrs.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(name));
                }

                //
                // make sure we have a local key-id
                //
                if (bagAttrs.getBagAttribute(pkcs_9_at_localKeyId) == null)
                {
                    Certificate             ct = engineGetCertificate(name);

                    bagAttrs.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(ct.getPublicKey()));
                }

                Enumeration e = bagAttrs.getBagAttributeKeys();

                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    ASN1EncodableVector  kSeq = new ASN1EncodableVector();

                    kSeq.add(oid);
                    kSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));

                    attrSet = true;

                    kName.add(new DERSequence(kSeq));
                }
            }

            if (!attrSet)
            {
                //
                // set a default friendly name (from the key id) and local id
                //
                ASN1EncodableVector     kSeq = new ASN1EncodableVector();
                Certificate             ct = engineGetCertificate(name);

                kSeq.add(pkcs_9_at_localKeyId);
                kSeq.add(new DERSet(createSubjectKeyId(ct.getPublicKey())));

                kName.add(new DERSequence(kSeq));

                kSeq = new ASN1EncodableVector();

                kSeq.add(pkcs_9_at_friendlyName);
                kSeq.add(new DERSet(new DERBMPString(name)));

                kName.add(new DERSequence(kSeq));
            }

            SafeBag                 kBag = new SafeBag(pkcs8ShroudedKeyBag, kInfo.getDERObject(), new DERSet(kName));
            keyS.add(kBag);
        }

        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);

        dOut.writeObject(new DERSequence(keyS));

        BERConstructedOctetString          keyString = new BERConstructedOctetString(bOut.toByteArray());

        //
        // certficate processing
        //
        byte[]                  cSalt = new byte[SALT_SIZE];

        random.nextBytes(cSalt);

        ASN1EncodableVector  certSeq = new ASN1EncodableVector();
        PKCS12PBEParams         cParams = new PKCS12PBEParams(cSalt, MIN_ITERATIONS);
        AlgorithmIdentifier     cAlgId = new AlgorithmIdentifier(new DERObjectIdentifier(CERT_ALGORITHM), cParams.getDERObject());
        Hashtable               doneCerts = new Hashtable();

        Enumeration cs = keys.keys();
        while (cs.hasMoreElements())
        {
            try
            {
                String              name = (String)cs.nextElement();
                Certificate         cert = engineGetCertificate(name);
                boolean             cAttrSet = false;
                CertBag             cBag = new CertBag(
                                        x509certType,
                                        new DEROctetString(cert.getEncoded()));
                ASN1EncodableVector fName = new ASN1EncodableVector();

                if (cert instanceof PKCS12BagAttributeCarrier)
                {
                    PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)cert;
                    //
                    // make sure we are using the local alias on store
                    //
                    DERBMPString    nm = (DERBMPString)bagAttrs.getBagAttribute(pkcs_9_at_friendlyName);
                    if (nm == null || !nm.getString().equals(name))
                    {
                        bagAttrs.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(name));
                    }

                    //
                    // make sure we have a local key-id
                    //
                    if (bagAttrs.getBagAttribute(pkcs_9_at_localKeyId) == null)
                    {
                        bagAttrs.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(cert.getPublicKey()));
                    }

                    Enumeration e = bagAttrs.getBagAttributeKeys();

                    while (e.hasMoreElements())
                    {
                        DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                        ASN1EncodableVector fSeq = new ASN1EncodableVector();

                        fSeq.add(oid);
                        fSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                        fName.add(new DERSequence(fSeq));

                        cAttrSet = true;
                    }
                }

                if (!cAttrSet)
                {
                    ASN1EncodableVector  fSeq = new ASN1EncodableVector();

                    fSeq.add(pkcs_9_at_localKeyId);
                    fSeq.add(new DERSet(createSubjectKeyId(cert.getPublicKey())));
                    fName.add(new DERSequence(fSeq));

                    fSeq = new ASN1EncodableVector();

                    fSeq.add(pkcs_9_at_friendlyName);
                    fSeq.add(new DERSet(new DERBMPString(name)));

                    fName.add(new DERSequence(fSeq));
                }

                SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), new DERSet(fName));

                certSeq.add(sBag);

                doneCerts.put(cert, cert);
            }
            catch (CertificateEncodingException e)
            {
                throw new IOException("Error encoding certificate: " + e.toString());
            }
        }

        cs = certs.keys();
        while (cs.hasMoreElements())
        {
            try
            {
                String              certId = (String)cs.nextElement();
                Certificate         cert = (Certificate)certs.get(certId);
                boolean             cAttrSet = false;

                if (keys.get(certId) != null)
                {
                    continue;
                }

                CertBag             cBag = new CertBag(
                                        x509certType,
                                        new DEROctetString(cert.getEncoded()));
                ASN1EncodableVector fName = new ASN1EncodableVector();

                if (cert instanceof PKCS12BagAttributeCarrier)
                {
                    PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)cert;
                    //
                    // make sure we are using the local alias on store
                    //
                    DERBMPString    nm = (DERBMPString)bagAttrs.getBagAttribute(pkcs_9_at_friendlyName);
                    if (nm == null || !nm.getString().equals(certId))
                    {
                        bagAttrs.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(certId));
                    }

                    Enumeration e = bagAttrs.getBagAttributeKeys();

                    while (e.hasMoreElements())
                    {
                        DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                        ASN1EncodableVector fSeq = new ASN1EncodableVector();

                        fSeq.add(oid);
                        fSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                        fName.add(new DERSequence(fSeq));

                        cAttrSet = true;
                    }
                }

                if (!cAttrSet)
                {
                    ASN1EncodableVector  fSeq = new ASN1EncodableVector();

                    fSeq.add(pkcs_9_at_friendlyName);
                    fSeq.add(new DERSet(new DERBMPString(certId)));

                    fName.add(new DERSequence(fSeq));
                }

                SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), new DERSet(fName));

                certSeq.add(sBag);

                doneCerts.put(cert, cert);
            }
            catch (CertificateEncodingException e)
            {
                throw new IOException("Error encoding certificate: " + e.toString());
            }
        }

        cs = chainCerts.keys();
        while (cs.hasMoreElements())
        {
            try
            {
                CertId              certId = (CertId)cs.nextElement();
                Certificate         cert = (Certificate)chainCerts.get(certId);

                if (doneCerts.get(cert) != null)
                {
                    continue;
                }

                CertBag             cBag = new CertBag(
                                        x509certType,
                                        new DEROctetString(cert.getEncoded()));
                ASN1EncodableVector fName = new ASN1EncodableVector();

                if (cert instanceof PKCS12BagAttributeCarrier)
                {
                    PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)cert;
                    Enumeration e = bagAttrs.getBagAttributeKeys();

                    while (e.hasMoreElements())
                    {
                        DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                        ASN1EncodableVector fSeq = new ASN1EncodableVector();

                        fSeq.add(oid);
                        fSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                        fName.add(new DERSequence(fSeq));
                    }
                }

                SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), new DERSet(fName));

                certSeq.add(sBag);
            }
            catch (CertificateEncodingException e)
            {
                throw new IOException("Error encoding certificate: " + e.toString());
            }
        }

        bOut.reset();

        dOut = new DEROutputStream(bOut);

        dOut.writeObject(new DERSequence(certSeq));

        dOut.close();

        byte[]                  certBytes = encryptData(CERT_ALGORITHM, bOut.toByteArray(), cParams, password);
        EncryptedData           cInfo = new EncryptedData(data, cAlgId, new BERConstructedOctetString(certBytes));

        c[0] = new ContentInfo(data, keyString);

        c[1] = new ContentInfo(encryptedData, cInfo.getDERObject());

        AuthenticatedSafe   auth = new AuthenticatedSafe(c);

        bOut.reset();

        BEROutputStream         berOut = new BEROutputStream(bOut);

        berOut.writeObject(auth);

        byte[]              pkg = bOut.toByteArray();

        ContentInfo         mainInfo = new ContentInfo(data, new BERConstructedOctetString(pkg));

        //
        // create the mac
        //
        byte[]                      mSalt = new byte[20];
        int                         itCount = MIN_ITERATIONS;

        random.nextBytes(mSalt);
    
        byte[]  data = ((ASN1OctetString)mainInfo.getContent()).getOctets();

        MacData                 mData = null;

        try
        {
            Mac                 mac = Mac.getInstance(id_SHA1.getId(), "BC");
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(id_SHA1.getId(), "BC");
            PBEParameterSpec    defParams = new PBEParameterSpec(mSalt, itCount);
            PBEKeySpec          pbeSpec = new PBEKeySpec(password);

            mac.init(keyFact.generateSecret(pbeSpec), defParams);

            mac.update(data);

            byte[]      res = mac.doFinal();

            // BEGIN android-changed
            AlgorithmIdentifier     algId = new AlgorithmIdentifier(id_SHA1, DERNull.THE_ONE);
            // END android-changed
            DigestInfo              dInfo = new DigestInfo(algId, res);

            mData = new MacData(dInfo, mSalt, itCount);
        }
        catch (Exception e)
        {
            throw new IOException("error constructing MAC: " + e.toString());
        }
        
        //
        // output the Pfx
        //
        Pfx                 pfx = new Pfx(mainInfo, mData);

        berOut = new BEROutputStream(stream);

        berOut.writeObject(pfx);
    }

    public static class BCPKCS12KeyStore
        extends JDKPKCS12KeyStore
    {
        public BCPKCS12KeyStore()
        {
            super("BC");
        }
    }

    public static class DefPKCS12KeyStore
        extends JDKPKCS12KeyStore
    {
        public DefPKCS12KeyStore()
        {
            super(null);
        }
    }
}
