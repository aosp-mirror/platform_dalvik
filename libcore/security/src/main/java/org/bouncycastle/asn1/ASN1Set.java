package org.bouncycastle.asn1;

// BEGIN android-removed
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
// END android-removed
import java.util.Enumeration;
// BEGIN android-removed
//import java.util.Vector;
// END android-removed;

// BEGIN android-note
// Changed inheritence of class.
// END android-note

abstract public class ASN1Set
    extends ASN1Collection
{
    // BEGIN android-removed
    //protected Vector set = new Vector();
    // END android-removed

    /**
     * return an ASN1Set from the given object.
     *
     * @param obj the object we want converted.
     * @exception IllegalArgumentException if the object cannot be converted.
     */
    public static ASN1Set getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof ASN1Set)
        {
            return (ASN1Set)obj;
        }

        throw new IllegalArgumentException("unknown object in getInstance");
    }

    /**
     * Return an ASN1 set from a tagged object. There is a special
     * case here, if an object appears to have been explicitly tagged on 
     * reading but we were expecting it to be implictly tagged in the 
     * normal course of events it indicates that we lost the surrounding
     * set - so we need to add it back (this will happen if the tagged
     * object is a sequence that contains other sequences). If you are
     * dealing with implicitly tagged sets you really <b>should</b>
     * be using this method.
     *
     * @param obj the tagged object.
     * @param explicit true if the object is meant to be explicitly tagged
     *          false otherwise.
     * @exception IllegalArgumentException if the tagged object cannot
     *          be converted.
     */
    public static ASN1Set getInstance(
        ASN1TaggedObject    obj,
        boolean             explicit)
    {
        if (explicit)
        {
            if (!obj.isExplicit())
            {
                throw new IllegalArgumentException("object implicit - explicit expected.");
            }

            return (ASN1Set)obj.getObject();
        }
        else
        {
            //
            // constructed object which appears to be explicitly tagged
            // and it's really implicit means we have to add the
            // surrounding sequence.
            //
            if (obj.isExplicit())
            {
                ASN1Set    set = new DERSet(obj.getObject());

                return set;
            }
            else
            {
                if (obj.getObject() instanceof ASN1Set)
                {
                    return (ASN1Set)obj.getObject();
                }

                //
                // in this case the parser returns a sequence, convert it
                // into a set.
                //
                ASN1EncodableVector  v = new ASN1EncodableVector();

                if (obj.getObject() instanceof ASN1Sequence)
                {
                    ASN1Sequence s = (ASN1Sequence)obj.getObject();
                    Enumeration e = s.getObjects();

                    while (e.hasMoreElements())
                    {
                        v.add((DEREncodable)e.nextElement());
                    }

                    return new DERSet(v, false);
                }
            }
        }

        throw new IllegalArgumentException(
                    "unknown object in getInstanceFromTagged");
    }

    public ASN1Set()
    {
    }

    // BEGIN android-removed
    //public Enumeration getObjects()
    //{
    //    return set.elements();
    //}

    ///**
    // * return the object at the set postion indicated by index.
    // *
    // * @param index the set number (starting at zero) of the object
    // * @return the object at the set postion indicated by index.
    // */
    //public DEREncodable getObjectAt(
    //    int index)
    //{
    //    return (DEREncodable)set.elementAt(index);
    //}

    ///**
    // * return the number of objects in this set.
    // *
    // * @return the number of objects in this set.
    // */
    //public int size()
    //{
    //    return set.size();
    //}

    //public int hashCode()
    //{
    //    Enumeration             e = this.getObjects();
    //    int                     hashCode = 0;

    //    while (e.hasMoreElements())
    //    {
    //        hashCode ^= e.nextElement().hashCode();
    //    }

    //    return hashCode;
    //}
    // END android-removed

    public boolean equals(
        Object  o)
    {
        if (o == this)
        {
            return true;
        }
        
        if (!(o instanceof DEREncodable))
        {
            return false;
        }

        DERObject      dObj = ((DEREncodable)o).getDERObject();
        
        if (!(dObj instanceof ASN1Set))
        {
            return false;
        }

        ASN1Set   other = (ASN1Set)dObj;

        if (this.size() != other.size())
        {
            return false;
        }

        Enumeration s1 = this.getObjects();
        Enumeration s2 = other.getObjects();

        while (s1.hasMoreElements())
        {
            if (!s1.nextElement().equals(s2.nextElement()))
            {
                return false;
            }
        }

        return true;
    }

    // BEGIN android-removed
    ///**
    // * return true if a <= b (arrays are assumed padded with zeros).
    // */
    //private boolean lessThanOrEqual(
    //     byte[] a,
    //     byte[] b)
    //{
    //     if (a.length <= b.length)
    //     {
    //         for (int i = 0; i != a.length; i++)
    //         {
    //             int    l = a[i] & 0xff;
    //             int    r = b[i] & 0xff;
    //             
    //             if (r > l)
    //             {
    //                 return true;
    //             }
    //             else if (l > r)
    //             {
    //                 return false;
    //             }
    //         }

    //         return true;
    //     }
    //     else
    //     {
    //         for (int i = 0; i != b.length; i++)
    //         {
    //             int    l = a[i] & 0xff;
    //             int    r = b[i] & 0xff;
    //             
    //             if (r > l)
    //             {
    //                 return true;
    //             }
    //             else if (l > r)
    //             {
    //                 return false;
    //             }
    //         }

    //         return false;
    //     }
    //}

    //private byte[] getEncoded(
    //    DEREncodable obj)
    //{
    //    ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
    //    ASN1OutputStream        aOut = new ASN1OutputStream(bOut);

    //    try
    //    {
    //        aOut.writeObject(obj);
    //    }
    //    catch (IOException e)
    //    {
    //        throw new IllegalArgumentException("cannot encode object added to SET");
    //    }

    //    return bOut.toByteArray();
    //}

    //protected void sort()
    //{
    //    if (set.size() > 1)
    //    {
    //        boolean    swapped = true;

    //        while (swapped)
    //        {
    //            int    index = 0;
    //            byte[] a = getEncoded((DEREncodable)set.elementAt(0));
    //            
    //            swapped = false;
    //            
    //            while (index != set.size() - 1)
    //            {
    //                byte[] b = getEncoded((DEREncodable)set.elementAt(index + 1));

    //                if (lessThanOrEqual(a, b))
    //                {
    //                    a = b;
    //                }
    //                else
    //                {
    //                    Object  o = set.elementAt(index);

    //                    set.setElementAt(set.elementAt(index + 1), index);
    //                    set.setElementAt(o, index + 1);

    //                    swapped = true;
    //                }

    //                index++;
    //            }
    //        }
    //    }
    //}

    //protected void addObject(
    //    DEREncodable obj)
    //{
    //    set.addElement(obj);
    //}

    //abstract void encode(DEROutputStream out)
    //        throws IOException;

    //public String toString() 
    //{
    //  return set.toString();
    //}
    // END android-removed
}
