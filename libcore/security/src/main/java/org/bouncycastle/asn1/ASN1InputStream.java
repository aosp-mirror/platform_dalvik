package org.bouncycastle.asn1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * a general purpose ASN.1 decoder - note: this class differs from the
 * others in that it returns null after it has read the last object in
 * the stream. If an ASN.1 NULL is encountered a DER/BER Null object is
 * returned.
 */
public class ASN1InputStream
    extends FilterInputStream
    implements DERTags
{
    private DERObject END_OF_STREAM = new DERObject()
    {
        void encode(
            DEROutputStream out)
        throws IOException
        {
            throw new IOException("Eeek!");
        }
        public int hashCode()
        {
            return 0;
        }
        public boolean equals(
            Object o) 
        {
            return o == this;
        }
    };
    
    boolean eofFound = false;
    int     limit = Integer.MAX_VALUE;

    public ASN1InputStream(
        InputStream is)
    {
        super(is);
    }

    /**
     * Create an ASN1InputStream based on the input byte array. The length of DER objects in
     * the stream is automatically limited to the length of the input array.
     * 
     * @param input array containing ASN.1 encoded data.
     */
    public ASN1InputStream(
        byte[] input)
    {
        this(new ByteArrayInputStream(input), input.length);
    }
    
    /**
     * Create an ASN1InputStream where no DER object will be longer than limit.
     * 
     * @param input stream containing ASN.1 encoded data.
     * @param limit maximum size of a DER encoded object.
     */
    public ASN1InputStream(
        InputStream input,
        int         limit)
    {
        super(input);
        this.limit = limit;
    }
    
    protected int readLength()
        throws IOException
    {
        int length = read();
        if (length < 0)
        {
            throw new IOException("EOF found when length expected");
        }

        if (length == 0x80)
        {
            return -1;      // indefinite-length encoding
        }

        if (length > 127)
        {
            int size = length & 0x7f;

            if (size > 4)
            {
                throw new IOException("DER length more than 4 bytes");
            }
            
            length = 0;
            for (int i = 0; i < size; i++)
            {
                int next = read();

                if (next < 0)
                {
                    throw new IOException("EOF found reading length");
                }

                length = (length << 8) + next;
            }
            
            if (length < 0)
            {
                throw new IOException("corrupted steam - negative length found");
            }
            
            if (length >= limit)   // after all we must have read at least 1 byte
            {
                throw new IOException("corrupted steam - out of bounds length found");
            }
        }

        return length;
    }

    protected void readFully(
        byte[]  bytes)
        throws IOException
    {
        int     left = bytes.length;
        int     len;

        if (left == 0)
        {
            return;
        }

        while ((len = read(bytes, bytes.length - left, left)) > 0)
        {
            if ((left -= len) == 0)
            {
                return;
            }
        }

        if (left != 0)
        {
            throw new EOFException("EOF encountered in middle of object");
        }
    }

    /**
     * build an object given its tag and a byte stream to construct it
     * from.
     */
    protected DERObject buildObject(
        int       tag,
        int       tagNo,
        byte[]    bytes)
        throws IOException
    {
        if ((tag & APPLICATION) != 0)
        {
            return new DERApplicationSpecific(tag, bytes);
        }
        
        switch (tag)
        {
        case NULL:
            // BEGIN android-changed
            return DERNull.THE_ONE;   
            //END android-changed
        case SEQUENCE | CONSTRUCTED:
            ASN1InputStream         aIn = new ASN1InputStream(bytes);
            ASN1EncodableVector     v = new ASN1EncodableVector();

            DERObject   obj = aIn.readObject();

            while (obj != null)
            {
                v.add(obj);
                obj = aIn.readObject();
            }

            return new DERSequence(v);
        case SET | CONSTRUCTED:
            aIn = new ASN1InputStream(bytes);
            v = new ASN1EncodableVector();

            obj = aIn.readObject();

            while (obj != null)
            {
                v.add(obj);
                obj = aIn.readObject();
            }

            return new DERSet(v, false);
        case BOOLEAN:
            // BEGIN android-changed
            return DERBoolean.getInstance(bytes);
            // END android-changed
        case INTEGER:
            return new DERInteger(bytes);
        case ENUMERATED:
            return new DEREnumerated(bytes);
        case OBJECT_IDENTIFIER:
            return new DERObjectIdentifier(bytes);
        case BIT_STRING:
            int     padBits = bytes[0];
            byte[]  data = new byte[bytes.length - 1];

            System.arraycopy(bytes, 1, data, 0, bytes.length - 1);

            return new DERBitString(data, padBits);
        case NUMERIC_STRING:
            return new DERNumericString(bytes);
        case UTF8_STRING:
            return new DERUTF8String(bytes);
        case PRINTABLE_STRING:
            return new DERPrintableString(bytes);
        case IA5_STRING:
            return new DERIA5String(bytes);
        case T61_STRING:
            return new DERT61String(bytes);
        case VISIBLE_STRING:
            return new DERVisibleString(bytes);
        case GENERAL_STRING:
            return new DERGeneralString(bytes);
        case UNIVERSAL_STRING:
            return new DERUniversalString(bytes);
        case BMP_STRING:
            return new DERBMPString(bytes);
        case OCTET_STRING:
            return new DEROctetString(bytes);
        case OCTET_STRING | CONSTRUCTED:
            return buildDerConstructedOctetString(bytes);
        case UTC_TIME:
            return new DERUTCTime(bytes);
        case GENERALIZED_TIME:
            return new DERGeneralizedTime(bytes);
        default:
            //
            // with tagged object tag number is bottom 5 bits
            //
            
            if ((tag & TAGGED) != 0)  
            {
                if (bytes.length == 0)        // empty tag!
                {
                    if ((tag & CONSTRUCTED) == 0)
                    {
                        // BEGIN android-changed
                        return new DERTaggedObject(false, tagNo, DERNull.THE_ONE);
                        // END android-changed
                    }
                    else
                    {
                        return new DERTaggedObject(false, tagNo, new DERSequence());
                    }
                }

                //
                // simple type - implicit... return an octet string
                //
                if ((tag & CONSTRUCTED) == 0)
                {
                    return new DERTaggedObject(false, tagNo, new DEROctetString(bytes));
                }

                aIn = new ASN1InputStream(bytes);

                DEREncodable dObj = aIn.readObject();

                //
                // explicitly tagged (probably!) - if it isn't we'd have to
                // tell from the context
                //
                if (aIn.available() == 0)
                {
                    return new DERTaggedObject(tagNo, dObj);
                }

                //
                // another implicit object, we'll create a sequence...
                //
                v = new ASN1EncodableVector();

                while (dObj != null)
                {
                    v.add(dObj);
                    dObj = aIn.readObject();
                }

                return new DERTaggedObject(false, tagNo, new DERSequence(v));
            }

            return new DERUnknownTag(tag, bytes);
        }
    }

    /**
     * read a string of bytes representing an indefinite length object.
     */
    private byte[] readIndefiniteLengthFully()
        throws IOException
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        int                     b, b1;

        b1 = read();

        while ((b = read()) >= 0)
        {
            if (b1 == 0 && b == 0)
            {
                break;
            }

            bOut.write(b1);
            b1 = b;
        }

        return bOut.toByteArray();
    }

    private BERConstructedOctetString buildConstructedOctetString()
        throws IOException
    {
        Vector               octs = new Vector();

        for (;;)
        {
            DERObject        o = readObject();

            if (o == END_OF_STREAM)
            {
                break;
            }

            octs.addElement(o);
        }

        return new BERConstructedOctetString(octs);
    }
    
    //
    // yes, people actually do this...
    //
    private BERConstructedOctetString buildDerConstructedOctetString(byte[] input)
        throws IOException
    {
        Vector               octs = new Vector();
        ASN1InputStream      aIn = new ASN1InputStream(input);
        DERObject            o;
        
        while ((o = aIn.readObject()) != null)
        {
            octs.addElement(o);
        }
    
        return new BERConstructedOctetString(octs);
    }

    public DERObject readObject()
        throws IOException
    {
        int tag = read();
        if (tag == -1)
        {
            if (eofFound)
            {
                throw new EOFException("attempt to read past end of file.");
            }

            eofFound = true;

            return null;
        }
    
        int tagNo = 0;
        
        if ((tag & TAGGED) != 0)  
        {
            tagNo = readTagNumber(tag);
        }
        
        int     length = readLength();

        if (length < 0)    // indefinite length method
        {
            switch (tag)
            {
            case NULL:
                // BEGIN android-changed
                return BERNull.THE_ONE;
                // END android-changed
            case SEQUENCE | CONSTRUCTED:
                ASN1EncodableVector  v = new ASN1EncodableVector();
    
                for (;;)
                {
                    DERObject   obj = readObject();

                    if (obj == END_OF_STREAM)
                    {
                        break;
                    }

                    v.add(obj);
                }
                return new BERSequence(v);
            case SET | CONSTRUCTED:
                v = new ASN1EncodableVector();
    
                for (;;)
                {
                    DERObject   obj = readObject();

                    if (obj == END_OF_STREAM)
                    {
                        break;
                    }

                    v.add(obj);
                }
                return new BERSet(v, false);
            case OCTET_STRING | CONSTRUCTED:
                return buildConstructedOctetString();
            default:
                //
                // with tagged object tag number is bottom 5 bits
                //
                if ((tag & TAGGED) != 0)  
                {
                    //
                    // simple type - implicit... return an octet string
                    //
                    if ((tag & CONSTRUCTED) == 0)
                    {
                        byte[]  bytes = readIndefiniteLengthFully();

                        return new BERTaggedObject(false, tagNo, new DEROctetString(bytes));
                    }

                    //
                    // either constructed or explicitly tagged
                    //
                    DERObject        dObj = readObject();

                    if (dObj == END_OF_STREAM)     // empty tag!
                    {
                        return new DERTaggedObject(tagNo);
                    }

                    DERObject       next = readObject();

                    //
                    // explicitly tagged (probably!) - if it isn't we'd have to
                    // tell from the context
                    //
                    if (next == END_OF_STREAM)
                    {
                        return new BERTaggedObject(tagNo, dObj);
                    }

                    //
                    // another implicit object, we'll create a sequence...
                    //
                    v = new ASN1EncodableVector();

                    v.add(dObj);

                    do
                    {
                        v.add(next);
                        next = readObject();
                    }
                    while (next != END_OF_STREAM);

                    return new BERTaggedObject(false, tagNo, new BERSequence(v));
                }

                throw new IOException("unknown BER object encountered");
            }
        }
        else
        {
            if (tag == 0 && length == 0)    // end of contents marker.
            {
                return END_OF_STREAM;
            }

            byte[]  bytes = new byte[length];
    
            readFully(bytes);
    
            return buildObject(tag, tagNo, bytes);
        }
    }

    private int readTagNumber(int tag) 
        throws IOException
    {
        int tagNo = tag & 0x1f;

        if (tagNo == 0x1f)
        {
            int b = read();

            tagNo = 0;

            while ((b >= 0) && ((b & 0x80) != 0))
            {
                tagNo |= (b & 0x7f);
                tagNo <<= 7;
                b = read();
            }

            if (b < 0)
            {
                eofFound = true;
                throw new EOFException("EOF found inside tag value.");
            }
            
            tagNo |= (b & 0x7f);
        }
        
        return tagNo;
    }
}

