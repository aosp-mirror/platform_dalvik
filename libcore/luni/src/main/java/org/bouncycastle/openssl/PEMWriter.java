package org.bouncycastle.openssl;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.OpenSSLPBEParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.x509.X509AttributeCertificate;
import org.bouncycastle.x509.X509V2AttributeCertificate;

/**
 * General purpose writer for OpenSSL PEM objects.
 */
public class PEMWriter
    extends BufferedWriter
{

    /**
     * Base constructor.
     * 
     * @param out output stream to use.
     */
    public PEMWriter(Writer out)
    {
        super(out);
    }
    
    private void writeHexEncoded(byte[] bytes)
        throws IOException
    {
        bytes = Hex.encode(bytes);
        
        for (int i = 0; i != bytes.length; i++)
        {
            this.write((char)bytes[i]);
        }
    }

    private void writeEncoded(byte[] bytes) 
        throws IOException
    {
        char[]  buf = new char[64];
        
        bytes = Base64.encode(bytes);
        
        for (int i = 0; i < bytes.length; i += buf.length)
        {
            int index = 0;
            
            while (index != buf.length)
            {
                if ((i + index) >= bytes.length)
                {
                    break;
                }
                buf[index] = (char)bytes[i + index];
                index++;
            }
            this.write(buf, 0, index);
            this.newLine();
        }
    }
    
    public void writeObject(
        Object  o) 
        throws IOException
    {
        String  type;
        byte[]  encoding;
        
        if (o instanceof X509Certificate)
        {
            type = "CERTIFICATE";
            try
            {
                encoding = ((X509Certificate)o).getEncoded();
            }
            catch (CertificateEncodingException e)
            {
                throw new IOException("Cannot encode object: " + e.toString());
            }
        }
        else if (o instanceof X509CRL)
        {
            type = "X509 CRL";
            try
            {
                encoding = ((X509CRL)o).getEncoded();
            }
            catch (CRLException e)
            {
                throw new IOException("Cannot encode object: " + e.toString());
            }
        }
        else if (o instanceof KeyPair)
        {
            writeObject(((KeyPair)o).getPrivate());
            return;
        }
        else if (o instanceof PrivateKey)
        {
            ByteArrayInputStream    bIn = new ByteArrayInputStream(((Key)o).getEncoded());
            ASN1InputStream         aIn = new ASN1InputStream(bIn);
            
            PrivateKeyInfo          info = new PrivateKeyInfo((ASN1Sequence)aIn.readObject());
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
            
            if (o instanceof RSAPrivateKey)
            {
                type = "RSA PRIVATE KEY";
                
                aOut.writeObject(info.getPrivateKey());
            }
            else if (o instanceof DSAPrivateKey)
            {
                type = "DSA PRIVATE KEY";
                
                DSAParameter        p = DSAParameter.getInstance(info.getAlgorithmId().getParameters());
                ASN1EncodableVector v = new ASN1EncodableVector();
                
                v.add(new DERInteger(0));
                v.add(new DERInteger(p.getP()));
                v.add(new DERInteger(p.getQ()));
                v.add(new DERInteger(p.getG()));
                
                BigInteger x = ((DSAPrivateKey)o).getX();
                BigInteger y = p.getG().modPow(x, p.getP());
                
                v.add(new DERInteger(y));
                v.add(new DERInteger(x));
                
                aOut.writeObject(new DERSequence(v));
            }
            else
            {
                throw new IOException("Cannot identify private key");
            }
            
            encoding = bOut.toByteArray();
        }
        else if (o instanceof PublicKey)
        {
            type = "PUBLIC KEY";
            
            encoding = ((PublicKey)o).getEncoded();
        }
        else if (o instanceof X509AttributeCertificate)
        {
            type = "ATTRIBUTE CERTIFICATE";
            encoding = ((X509V2AttributeCertificate)o).getEncoded();
        }
        else if (o instanceof PKCS10CertificationRequest)
        {
            type = "CERTIFICATE REQUEST";
            encoding = ((PKCS10CertificationRequest)o).getEncoded();
        }
        else if (o instanceof ContentInfo)
        {
            type = "PKCS7";
            encoding = ((ContentInfo)o).getEncoded();
        }
        else
        {
            throw new IOException("unknown object passed - can't encode.");
        }
        
        this.write("-----BEGIN " + type + "-----");
        this.newLine();
        
        writeEncoded(encoding);
        
        this.write("-----END " + type + "-----");
        this.newLine();
    }
    
    public void writeObject(
        Object       o,
        String       algorithm,
        char[]       password,
        SecureRandom random)
        throws IOException
    {
        byte[] salt = new byte[8];
        
        random.nextBytes(salt);
       
        OpenSSLPBEParametersGenerator pGen = new OpenSSLPBEParametersGenerator();
        
        pGen.init(PBEParametersGenerator.PKCS5PasswordToBytes(password), salt);
        
        SecretKey secretKey = null;
        
        if (algorithm.equalsIgnoreCase("DESEDE"))
        {
            // generate key
            int keyLength = 24;

            secretKey = new SecretKeySpec(((KeyParameter)pGen.generateDerivedParameters(keyLength * 8)).getKey(), algorithm);
        }
        else
        {
            throw new IOException("unknown algorithm in writeObject");
        }
        
        byte[] keyData = null;
        
        if (o instanceof RSAPrivateCrtKey)
        {
            RSAPrivateCrtKey k = (RSAPrivateCrtKey)o;

            RSAPrivateKeyStructure keyStruct = new RSAPrivateKeyStructure(
                k.getModulus(),
                k.getPublicExponent(),
                k.getPrivateExponent(),
                k.getPrimeP(),
                k.getPrimeQ(),
                k.getPrimeExponentP(),
                k.getPrimeExponentQ(),
                k.getCrtCoefficient());
       
            // convert to bytearray
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            ASN1OutputStream      aOut = new ASN1OutputStream(bOut);
            
            aOut.writeObject(keyStruct);
            aOut.close();
            
            keyData = bOut.toByteArray();
        }
       
        byte[]  encData = null;
        
        // cipher  
        try
        {
            Cipher  c = Cipher.getInstance("DESede/CBC/PKCS5Padding", "BC");
            c.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(salt));
        
            encData = c.doFinal(keyData);
        }
        catch (Exception e)
        {
            throw new IOException("exception using cipher: " + e.toString());
        }
       
        // write the data
        this.write("-----BEGIN RSA PRIVATE KEY-----");
        this.newLine();
        this.write("Proc-Type: 4,ENCRYPTED");
        this.newLine();
        this.write("DEK-Info: DES-EDE3-CBC,");
        this.writeHexEncoded(salt);
        this.newLine();
        this.newLine();
        
        this.writeEncoded(encData);
        this.write("-----END RSA PRIVATE KEY-----");   
    }
}
