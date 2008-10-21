package org.bouncycastle.asn1.x509;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.Strings;

// BEGIN android-note
// Changes to this class now limit X509Names to 32 components. We have
// never observed an instance created with more than 10.
// END android-note

/**
 * <pre>
 *     RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
 *
 *     RelativeDistinguishedName ::= SET SIZE (1..MAX) OF AttributeTypeAndValue
 *
 *     AttributeTypeAndValue ::= SEQUENCE {
 *                                   type  OBJECT IDENTIFIER,
 *                                   value ANY }
 * </pre>
 */
public class X509Name
    extends ASN1Encodable
{
    /**
     * country code - StringType(SIZE(2))
     */
    public static final DERObjectIdentifier C = new DERObjectIdentifier("2.5.4.6");

    /**
     * organization - StringType(SIZE(1..64))
     */
    public static final DERObjectIdentifier O = new DERObjectIdentifier("2.5.4.10");

    /**
     * organizational unit name - StringType(SIZE(1..64))
     */
    public static final DERObjectIdentifier OU = new DERObjectIdentifier("2.5.4.11");

    /**
     * Title
     */
    public static final DERObjectIdentifier T = new DERObjectIdentifier("2.5.4.12");

    /**
     * common name - StringType(SIZE(1..64))
     */
    public static final DERObjectIdentifier CN = new DERObjectIdentifier("2.5.4.3");

    /**
     * device serial number name - StringType(SIZE(1..64))
     */
    public static final DERObjectIdentifier SN = new DERObjectIdentifier("2.5.4.5");

    /**
     * street - StringType(SIZE(1..64))
     */
    public static final DERObjectIdentifier STREET = new DERObjectIdentifier("2.5.4.9");
    
    /**
     * device serial number name - StringType(SIZE(1..64))
     */
    public static final DERObjectIdentifier SERIALNUMBER = SN;

    /**
     * locality name - StringType(SIZE(1..64))
     */
    public static final DERObjectIdentifier L = new DERObjectIdentifier("2.5.4.7");

    /**
     * state, or province name - StringType(SIZE(1..64))
     */
    public static final DERObjectIdentifier ST = new DERObjectIdentifier("2.5.4.8");

    /**
     * Naming attributes of type X520name
     */
    public static final DERObjectIdentifier SURNAME = new DERObjectIdentifier("2.5.4.4");
    public static final DERObjectIdentifier GIVENNAME = new DERObjectIdentifier("2.5.4.42");
    public static final DERObjectIdentifier INITIALS = new DERObjectIdentifier("2.5.4.43");
    public static final DERObjectIdentifier GENERATION = new DERObjectIdentifier("2.5.4.44");
    public static final DERObjectIdentifier UNIQUE_IDENTIFIER = new DERObjectIdentifier("2.5.4.45");

    /**
     * businessCategory - DirectoryString(SIZE(1..128)
     */
    public static final DERObjectIdentifier BUSINESS_CATEGORY = new DERObjectIdentifier(
                    "2.5.4.15");

    /**
     * postalCode - DirectoryString(SIZE(1..40)
     */
    public static final DERObjectIdentifier POSTAL_CODE = new DERObjectIdentifier(
                    "2.5.4.17");
    
    /**
     * dnQualifier - DirectoryString(SIZE(1..64)
     */
    public static final DERObjectIdentifier DN_QUALIFIER = new DERObjectIdentifier(
                    "2.5.4.46");

    /**
     * RFC 3039 Pseudonym - DirectoryString(SIZE(1..64)
     */
    public static final DERObjectIdentifier PSEUDONYM = new DERObjectIdentifier(
                    "2.5.4.65");


    /**
     * RFC 3039 DateOfBirth - GeneralizedTime - YYYYMMDD000000Z
     */
    public static final DERObjectIdentifier DATE_OF_BIRTH = new DERObjectIdentifier(
                    "1.3.6.1.5.5.7.9.1");

    /**
     * RFC 3039 PlaceOfBirth - DirectoryString(SIZE(1..128)
     */
    public static final DERObjectIdentifier PLACE_OF_BIRTH = new DERObjectIdentifier(
                    "1.3.6.1.5.5.7.9.2");

    /**
     * RFC 3039 Gender - PrintableString (SIZE(1)) -- "M", "F", "m" or "f"
     */
    public static final DERObjectIdentifier GENDER = new DERObjectIdentifier(
                    "1.3.6.1.5.5.7.9.3");

    /**
     * RFC 3039 CountryOfCitizenship - PrintableString (SIZE (2)) -- ISO 3166
     * codes only
     */
    public static final DERObjectIdentifier COUNTRY_OF_CITIZENSHIP = new DERObjectIdentifier(
                    "1.3.6.1.5.5.7.9.4");

    /**
     * RFC 3039 CountryOfResidence - PrintableString (SIZE (2)) -- ISO 3166
     * codes only
     */
    public static final DERObjectIdentifier COUNTRY_OF_RESIDENCE = new DERObjectIdentifier(
                    "1.3.6.1.5.5.7.9.5");


    /**
     * ISIS-MTT NameAtBirth - DirectoryString(SIZE(1..64)
     */
    public static final DERObjectIdentifier NAME_AT_BIRTH =  new DERObjectIdentifier("1.3.36.8.3.14");

    /**
     * RFC 3039 PostalAddress - SEQUENCE SIZE (1..6) OF
     * DirectoryString(SIZE(1..30))
     */
    public static final DERObjectIdentifier POSTAL_ADDRESS = new DERObjectIdentifier(
                    "2.5.4.16");

    /**
     * Email address (RSA PKCS#9 extension) - IA5String.
     * <p>Note: if you're trying to be ultra orthodox, don't use this! It shouldn't be in here.
     */
    public static final DERObjectIdentifier EmailAddress = PKCSObjectIdentifiers.pkcs_9_at_emailAddress;
    
    /**
     * more from PKCS#9
     */
    public static final DERObjectIdentifier UnstructuredName = PKCSObjectIdentifiers.pkcs_9_at_unstructuredName;
    public static final DERObjectIdentifier UnstructuredAddress = PKCSObjectIdentifiers.pkcs_9_at_unstructuredAddress;
    
    /**
     * email address in Verisign certificates
     */
    public static final DERObjectIdentifier E = EmailAddress;
    
    /*
     * others...
     */
    public static final DERObjectIdentifier DC = new DERObjectIdentifier("0.9.2342.19200300.100.1.25");

    /**
     * LDAP User id.
     */
    public static final DERObjectIdentifier UID = new DERObjectIdentifier("0.9.2342.19200300.100.1.1");

    /**
     * look up table translating OID values into their common symbols - this static is scheduled for deletion
     */
    public static Hashtable OIDLookUp = new Hashtable();

    /**
     * determines whether or not strings should be processed and printed
     * from back to front.
     */
    public static boolean DefaultReverse = false;

    /**
     * default look up table translating OID values into their common symbols following
     * the convention in RFC 2253 with a few extras
     */
    public static Hashtable DefaultSymbols = OIDLookUp;

    /**
     * look up table translating OID values into their common symbols following the convention in RFC 2253
     * 
     */
    public static Hashtable RFC2253Symbols = new Hashtable();

    /**
     * look up table translating OID values into their common symbols following the convention in RFC 1779
     * 
     */
    public static Hashtable RFC1779Symbols = new Hashtable();

    /**
     * look up table translating string values into their OIDS -
     * this static is scheduled for deletion
     */
    public static Hashtable SymbolLookUp = new Hashtable();

    /**
     * look up table translating common symbols into their OIDS.
     */
    public static Hashtable DefaultLookUp = SymbolLookUp;

    // BEGIN android-removed
    //private static final Boolean TRUE = new Boolean(true); // for J2ME compatibility
    //private static final Boolean FALSE = new Boolean(false);
    // END android-removed

    static
    {
        DefaultSymbols.put(C, "C");
        DefaultSymbols.put(O, "O");
        DefaultSymbols.put(T, "T");
        DefaultSymbols.put(OU, "OU");
        DefaultSymbols.put(CN, "CN");
        DefaultSymbols.put(L, "L");
        DefaultSymbols.put(ST, "ST");
        DefaultSymbols.put(SN, "SN");
        DefaultSymbols.put(EmailAddress, "E");
        DefaultSymbols.put(DC, "DC");
        DefaultSymbols.put(UID, "UID");
        DefaultSymbols.put(STREET, "STREET");
        DefaultSymbols.put(SURNAME, "SURNAME");
        DefaultSymbols.put(GIVENNAME, "GIVENNAME");
        DefaultSymbols.put(INITIALS, "INITIALS");
        DefaultSymbols.put(GENERATION, "GENERATION");
        DefaultSymbols.put(UnstructuredAddress, "unstructuredAddress");
        DefaultSymbols.put(UnstructuredName, "unstructuredName");
        DefaultSymbols.put(UNIQUE_IDENTIFIER, "UniqueIdentifier");
        DefaultSymbols.put(DN_QUALIFIER, "DN");
        DefaultSymbols.put(PSEUDONYM, "Pseudonym");
        DefaultSymbols.put(POSTAL_ADDRESS, "PostalAddress");
        DefaultSymbols.put(NAME_AT_BIRTH, "NameAtBirth");
        DefaultSymbols.put(COUNTRY_OF_CITIZENSHIP, "CountryOfCitizenship");
        DefaultSymbols.put(COUNTRY_OF_RESIDENCE, "CountryOfResidence");
        DefaultSymbols.put(GENDER, "Gender");
        DefaultSymbols.put(PLACE_OF_BIRTH, "PlaceOfBirth");
        DefaultSymbols.put(DATE_OF_BIRTH, "DateOfBirth");
        DefaultSymbols.put(POSTAL_CODE, "PostalCode");
        DefaultSymbols.put(BUSINESS_CATEGORY, "BusinessCategory");

        RFC2253Symbols.put(C, "C");
        RFC2253Symbols.put(O, "O");
        RFC2253Symbols.put(OU, "OU");
        RFC2253Symbols.put(CN, "CN");
        RFC2253Symbols.put(L, "L");
        RFC2253Symbols.put(ST, "ST");
        RFC2253Symbols.put(STREET, "STREET");
        RFC2253Symbols.put(DC, "DC");
        RFC2253Symbols.put(UID, "UID");

        RFC1779Symbols.put(C, "C");
        RFC1779Symbols.put(O, "O");
        RFC1779Symbols.put(OU, "OU");
        RFC1779Symbols.put(CN, "CN");
        RFC1779Symbols.put(L, "L");
        RFC1779Symbols.put(ST, "ST");
        RFC1779Symbols.put(STREET, "STREET");

        DefaultLookUp.put("c", C);
        DefaultLookUp.put("o", O);
        DefaultLookUp.put("t", T);
        DefaultLookUp.put("ou", OU);
        DefaultLookUp.put("cn", CN);
        DefaultLookUp.put("l", L);
        DefaultLookUp.put("st", ST);
        DefaultLookUp.put("sn", SN);
        DefaultLookUp.put("serialnumber", SN);
        DefaultLookUp.put("street", STREET);
        DefaultLookUp.put("emailaddress", E);
        DefaultLookUp.put("dc", DC);
        DefaultLookUp.put("e", E);
        DefaultLookUp.put("uid", UID);
        DefaultLookUp.put("surname", SURNAME);
        DefaultLookUp.put("givenname", GIVENNAME);
        DefaultLookUp.put("initials", INITIALS);
        DefaultLookUp.put("generation", GENERATION);
        DefaultLookUp.put("unstructuredaddress", UnstructuredAddress);
        DefaultLookUp.put("unstructuredname", UnstructuredName);
        DefaultLookUp.put("uniqueidentifier", UNIQUE_IDENTIFIER);
        DefaultLookUp.put("dn", DN_QUALIFIER);
        DefaultLookUp.put("pseudonym", PSEUDONYM);
        DefaultLookUp.put("postaladdress", POSTAL_ADDRESS);
        DefaultLookUp.put("nameofbirth", NAME_AT_BIRTH);
        DefaultLookUp.put("countryofcitizenship", COUNTRY_OF_CITIZENSHIP);
        DefaultLookUp.put("countryofresidence", COUNTRY_OF_RESIDENCE);
        DefaultLookUp.put("gender", GENDER);
        DefaultLookUp.put("placeofbirth", PLACE_OF_BIRTH);
        DefaultLookUp.put("dateofbirth", DATE_OF_BIRTH);
        DefaultLookUp.put("postalcode", POSTAL_CODE);
        DefaultLookUp.put("businesscategory", BUSINESS_CATEGORY);
    }

    private X509NameEntryConverter  converter = null;
    // BEGIN android-changed
    private X509NameElementList     elems = new X509NameElementList();
    // END android-changed
    
    private ASN1Sequence            seq;

    /**
     * Return a X509Name based on the passed in tagged object.
     * 
     * @param obj tag object holding name.
     * @param explicit true if explicitly tagged false otherwise.
     * @return the X509Name
     */
    public static X509Name getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static X509Name getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof X509Name)
        {
            return (X509Name)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new X509Name((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory \"" + obj.getClass().getName()+"\"");
    }

    /**
     * Constructor from ASN1Sequence
     *
     * the principal will be a list of constructed sets, each containing an (OID, String) pair.
     */
    public X509Name(
        ASN1Sequence  seq)
    {
        this.seq = seq;

        Enumeration e = seq.getObjects();

        while (e.hasMoreElements())
        {
            ASN1Set         set = (ASN1Set)e.nextElement();

            for (int i = 0; i < set.size(); i++) 
            {
                   // BEGIN android-changed
                   ASN1Sequence s = (ASN1Sequence)set.getObjectAt(i);
                   
                   DERObjectIdentifier key =
                       (DERObjectIdentifier) s.getObjectAt(0);
                   DEREncodable value = s.getObjectAt(1);
                   String valueStr;

                   if (value instanceof DERString)
                   {
                       valueStr = ((DERString)value).getString();
                   }
                   else
                   {
                       valueStr = "#" + bytesToString(Hex.encode(value.getDERObject().getDEREncoded()));
                   }

                   /*
                    * The added flag set to (i != 0), to allow earlier JDK
                    * compatibility.
                    */
                   elems.add(key, valueStr, i != 0);
                   // END android-changed
            }
        }
    }

    /**
     * constructor from a table of attributes.
     * <p>
     * it's is assumed the table contains OID/String pairs, and the contents
     * of the table are copied into an internal table as part of the
     * construction process.
     * <p>
     * <b>Note:</b> if the name you are trying to generate should be
     * following a specific ordering, you should use the constructor
     * with the ordering specified below.
     */
    public X509Name(
        Hashtable  attributes)
    {
        this(null, attributes);
    }

    /**
     * Constructor from a table of attributes with ordering.
     * <p>
     * it's is assumed the table contains OID/String pairs, and the contents
     * of the table are copied into an internal table as part of the
     * construction process. The ordering vector should contain the OIDs
     * in the order they are meant to be encoded or printed in toString.
     */
    public X509Name(
        Vector      ordering,
        Hashtable   attributes)
    {
        this(ordering, attributes, new X509DefaultEntryConverter());
    }

    /**
     * Constructor from a table of attributes with ordering.
     * <p>
     * it's is assumed the table contains OID/String pairs, and the contents
     * of the table are copied into an internal table as part of the
     * construction process. The ordering vector should contain the OIDs
     * in the order they are meant to be encoded or printed in toString.
     * <p>
     * The passed in converter will be used to convert the strings into their
     * ASN.1 counterparts.
     */
    public X509Name(
        Vector                      ordering,
        Hashtable                   attributes,
        X509DefaultEntryConverter   converter)
    {
        // BEGIN android-changed
        DERObjectIdentifier problem = null;
        this.converter = converter;

        if (ordering != null)
        {
            for (int i = 0; i != ordering.size(); i++)
            {
                DERObjectIdentifier key =
                    (DERObjectIdentifier) ordering.elementAt(i);
                String value = (String) attributes.get(key);
                if (value == null)
                {
                    problem = key;
                    break;
                }
                elems.add(key, value);
            }
        }
        else
        {
            Enumeration     e = attributes.keys();

            while (e.hasMoreElements())
            {
                DERObjectIdentifier key =
                    (DERObjectIdentifier) e.nextElement();
                String value = (String) attributes.get(key);
                if (value == null)
                {
                    problem = key;
                    break;
                }
                elems.add(key, value);
            }
        }

        if (problem != null)
        {
            throw new IllegalArgumentException("No attribute for object id - " + problem.getId() + " - passed to distinguished name");
        }
        // END android-changed
    }

    /**
     * Takes two vectors one of the oids and the other of the values.
     */
    public X509Name(
        Vector  oids,
        Vector  values)
    {
        this(oids, values, new X509DefaultEntryConverter());
    }

    /**
     * Takes two vectors one of the oids and the other of the values.
     * <p>
     * The passed in converter will be used to convert the strings into their
     * ASN.1 counterparts.
     */
    public X509Name(
        Vector                  oids,
        Vector                  values,
        X509NameEntryConverter  converter)
    {
        this.converter = converter;

        if (oids.size() != values.size())
        {
            throw new IllegalArgumentException("oids vector must be same length as values.");
        }

        for (int i = 0; i < oids.size(); i++)
        {
            // BEGIN android-changed
            elems.add((DERObjectIdentifier) oids.elementAt(i),
                    (String) values.elementAt(i));
            // END android-changed
        }
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes.
     */
    public X509Name(
        String  dirName)
    {
        this(DefaultReverse, DefaultLookUp, dirName);
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes with each
     * string value being converted to its associated ASN.1 type using the passed
     * in converter.
     */
    public X509Name(
        String                  dirName,
        X509NameEntryConverter  converter)
    {
        this(DefaultReverse, DefaultLookUp, dirName, converter);
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes. If reverse
     * is true, create the encoded version of the sequence starting from the
     * last element in the string.
     */
    public X509Name(
        boolean reverse,
        String  dirName)
    {
        this(reverse, DefaultLookUp, dirName);
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes with each
     * string value being converted to its associated ASN.1 type using the passed
     * in converter. If reverse is true the ASN.1 sequence representing the DN will
     * be built by starting at the end of the string, rather than the start.
     */
    public X509Name(
        boolean                 reverse,
        String                  dirName,
        X509NameEntryConverter  converter)
    {
        this(reverse, DefaultLookUp, dirName, converter);
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes. lookUp
     * should provide a table of lookups, indexed by lowercase only strings and
     * yielding a DERObjectIdentifier, other than that OID. and numeric oids
     * will be processed automatically.
     * <br>
     * If reverse is true, create the encoded version of the sequence
     * starting from the last element in the string.
     * @param reverse true if we should start scanning from the end (RFC 2553).
     * @param lookUp table of names and their oids.
     * @param dirName the X.500 string to be parsed.
     */
    public X509Name(
        boolean     reverse,
        Hashtable   lookUp,
        String      dirName)
    {
        this(reverse, lookUp, dirName, new X509DefaultEntryConverter());
    }

    private DERObjectIdentifier decodeOID(
        String      name,
        Hashtable   lookUp)
    {
        if (Strings.toUpperCase(name).startsWith("OID."))
        {
            return new DERObjectIdentifier(name.substring(4));
        }
        else if (name.charAt(0) >= '0' && name.charAt(0) <= '9')
        {
            return new DERObjectIdentifier(name);
        }

        DERObjectIdentifier oid = (DERObjectIdentifier)lookUp.get(Strings.toLowerCase(name));
        if (oid == null)
        {
            throw new IllegalArgumentException("Unknown object id - " + name + " - passed to distinguished name");
        }

        return oid;
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes. lookUp
     * should provide a table of lookups, indexed by lowercase only strings and
     * yielding a DERObjectIdentifier, other than that OID. and numeric oids
     * will be processed automatically. The passed in converter is used to convert the
     * string values to the right of each equals sign to their ASN.1 counterparts.
     * <br>
     * @param reverse true if we should start scanning from the end, false otherwise.
     * @param lookUp table of names and oids.
     * @param dirName the string dirName
     * @param converter the converter to convert string values into their ASN.1 equivalents
     */
    public X509Name(
        boolean                 reverse,
        Hashtable               lookUp,
        String                  dirName,
        X509NameEntryConverter  converter)
    {
        this.converter = converter;
        X509NameTokenizer   nTok = new X509NameTokenizer(dirName);

        while (nTok.hasMoreTokens())
        {
            String  token = nTok.nextToken();
            int     index = token.indexOf('=');

            if (index == -1)
            {
                throw new IllegalArgumentException("badly formated directory string");
            }

            String              name = token.substring(0, index);
            String              value = token.substring(index + 1);
            DERObjectIdentifier oid = decodeOID(name, lookUp);

            if (value.indexOf('+') > 0)
            {
                X509NameTokenizer   vTok = new X509NameTokenizer(value, '+');

                // BEGIN android-changed
                elems.add(oid, vTok.nextToken());
                // END android-changed

                while (vTok.hasMoreTokens())
                {
                    String  sv = vTok.nextToken();
                    int     ndx = sv.indexOf('=');

                    String  nm = sv.substring(0, ndx);
                    String  vl = sv.substring(ndx + 1);
                    // BEGIN android-changed
                    elems.add(decodeOID(nm, lookUp), vl, true);
                    // END android-changed
                }
            }
            else
            {
                // BEGIN android-changed
                elems.add(oid, value);
                // END android-changed
            }
        }

        if (reverse)
        {
            // BEGIN android-changed
            elems = elems.reverse();
            // END android-changed
        }
    }

    /**
     * return a vector of the oids in the name, in the order they were found.
     */
    public Vector getOIDs()
    {
        // BEGIN android-changed
        Vector  v = new Vector();
        int     size = elems.size();

        for (int i = 0; i < size; i++)
        {
            v.addElement(elems.getKey(i));
        }

        return v;
        // END android-changed
    }

    /**
     * return a vector of the values found in the name, in the order they
     * were found.
     */
    public Vector getValues()
    {
        // BEGIN android-changed
        Vector  v = new Vector();
        int     size = elems.size();

        for (int i = 0; i < size; i++)
        {
            v.addElement(elems.getValue(i));
        }

        return v;
        // END android-changed
    }

    public DERObject toASN1Object()
    {
        if (seq == null)
        {
            // BEGIN android-changed
            ASN1EncodableVector  vec = new ASN1EncodableVector();
            ASN1EncodableVector  sVec = new ASN1EncodableVector();
            DERObjectIdentifier  lstOid = null;
            int                  size = elems.size();
            
            for (int i = 0; i != size; i++)
            {
                ASN1EncodableVector     v = new ASN1EncodableVector();
                DERObjectIdentifier     oid = elems.getKey(i);

                v.add(oid);

                String  str = elems.getValue(i);

                v.add(converter.getConvertedValue(oid, str));

                if (lstOid == null || elems.getAdded(i))
                {
                    sVec.add(new DERSequence(v));
                }
                else
                {
                    vec.add(new DERSet(sVec));
                    sVec = new ASN1EncodableVector();
                    
                    sVec.add(new DERSequence(v));
                }
                
                lstOid = oid;
            }
            
            vec.add(new DERSet(sVec));
            
            seq = new DERSequence(vec);
            // END android-changed
        }

        return seq;
    }

    /**
     * @param inOrder if true the order of both X509 names must be the same,
     * as well as the values associated with each element.
     */
    public boolean equals(Object _obj, boolean inOrder) 
    {
        if (_obj == this)
        {
            return true;
        }

        if (!inOrder)
        {
            return this.equals(_obj);
        }

        if (!(_obj instanceof X509Name))
        {
            return false;
        }
        
        X509Name _oxn          = (X509Name)_obj;
        // BEGIN android-changed
        int      _orderingSize = elems.size();

        if (_orderingSize != _oxn.elems.size()) 
        {
            return false;
        }
        // END android-changed
        
        for(int i = 0; i < _orderingSize; i++) 
        {
            // BEGIN android-changed
            String  _oid   = elems.getKey(i).getId();
            String  _val   = elems.getValue(i);
            
            String _oOID = _oxn.elems.getKey(i).getId();
            String _oVal = _oxn.elems.getValue(i);
            // BEGIN android-changed

            if (_oid.equals(_oOID))
            {
                _val = Strings.toLowerCase(_val.trim());
                _oVal = Strings.toLowerCase(_oVal.trim());
                if (_val.equals(_oVal))
                {
                    continue;
                }
                else
                {
                    StringBuffer    v1 = new StringBuffer();
                    StringBuffer    v2 = new StringBuffer();

                    if (_val.length() != 0)
                    {
                        char    c1 = _val.charAt(0);

                        v1.append(c1);

                        for (int k = 1; k < _val.length(); k++)
                        {
                            char    c2 = _val.charAt(k);
                            if (!(c1 == ' ' && c2 == ' '))
                            {
                                v1.append(c2);
                            }
                            c1 = c2;
                        }
                    }

                    if (_oVal.length() != 0)
                    {
                        char    c1 = _oVal.charAt(0);

                        v2.append(c1);

                        for (int k = 1; k < _oVal.length(); k++)
                        {
                            char    c2 = _oVal.charAt(k);
                            if (!(c1 == ' ' && c2 == ' '))
                            {
                                v2.append(c2);
                            }
                            c1 = c2;
                        }
                    }

                    if (!v1.toString().equals(v2.toString()))
                    {
                        return false;
                    }
                }
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    /**
     * test for equality - note: case is ignored.
     */
    public boolean equals(Object _obj) 
    {
        if (_obj == this)
        {
            return true;
        }

        if (!(_obj instanceof X509Name || _obj instanceof ASN1Sequence))
        {
            return false;
        }
        
        DERObject derO = ((DEREncodable)_obj).getDERObject();
        
        if (this.getDERObject().equals(derO))
        {
            return true;
        }
        
        if (!(_obj instanceof X509Name))
        {
            return false;
        }
        
        X509Name _oxn          = (X509Name)_obj;

        // BEGIN android-changed
        int      _orderingSize = elems.size();

        if (_orderingSize != _oxn.elems.size()) 
        {
            return false;
        }
        // END android-changed
        
        boolean[] _indexes = new boolean[_orderingSize];

        for(int i = 0; i < _orderingSize; i++) 
        {
            boolean _found = false;
            // BEGIN android-changed
            String  _oid   = elems.getKey(i).getId();
            String  _val   = elems.getValue(i);
            // END android-changed
            
            for(int j = 0; j < _orderingSize; j++) 
            {
                if (_indexes[j])
                {
                    continue;
                }

                // BEGIN android-changed
                String _oOID = elems.getKey(j).getId();
                String _oVal = _oxn.elems.getValue(j);
                // END android-changed

                if (_oid.equals(_oOID))
                {
                    _val = Strings.toLowerCase(_val.trim());
                    _oVal = Strings.toLowerCase(_oVal.trim());
                    if (_val.equals(_oVal))
                    {
                        _indexes[j] = true;
                        _found      = true;
                        break;
                    }
                    else
                    {
                        StringBuffer    v1 = new StringBuffer();
                        StringBuffer    v2 = new StringBuffer();

                        if (_val.length() != 0)
                        {
                            char    c1 = _val.charAt(0);

                            v1.append(c1);

                            for (int k = 1; k < _val.length(); k++)
                            {
                                char    c2 = _val.charAt(k);
                                if (!(c1 == ' ' && c2 == ' '))
                                {
                                    v1.append(c2);
                                }
                                c1 = c2;
                            }
                        }

                        if (_oVal.length() != 0)
                        {
                            char    c1 = _oVal.charAt(0);

                            v2.append(c1);

                            for (int k = 1; k < _oVal.length(); k++)
                            {
                                char    c2 = _oVal.charAt(k);
                                if (!(c1 == ' ' && c2 == ' '))
                                {
                                    v2.append(c2);
                                }
                                c1 = c2;
                            }
                        }

                        if (v1.toString().equals(v2.toString()))
                        {
                            _indexes[j] = true;
                            _found      = true;
                            break;
                        }
                    }
                }
            }

            if(!_found)
            {
                return false;
            }
        }
        
        return true;
    }
    
    public int hashCode()
    {
        ASN1Sequence  seq = (ASN1Sequence)this.getDERObject();
        Enumeration   e = seq.getObjects();
        int           hashCode = 0;

        while (e.hasMoreElements())
        {
            hashCode ^= e.nextElement().hashCode();
        }

        return hashCode;
    }

    private void appendValue(
        StringBuffer        buf,
        Hashtable           oidSymbols,
        DERObjectIdentifier oid,
        String              value)
    {
        String  sym = (String)oidSymbols.get(oid);

        if (sym != null)
        {
            buf.append(sym);
        }
        else
        {
            buf.append(oid.getId());
        }

        buf.append('=');

        int     index = buf.length();

        buf.append(value);

        int     end = buf.length();

        while (index != end)
        {
            if ((buf.charAt(index) == ',')
               || (buf.charAt(index) == '"')
               || (buf.charAt(index) == '\\')
               || (buf.charAt(index) == '+')
               || (buf.charAt(index) == '<')
               || (buf.charAt(index) == '>')
               || (buf.charAt(index) == ';'))
            {
                buf.insert(index, "\\");
                index++;
                end++;
            }

            index++;
        }
    }

    /**
     * convert the structure to a string - if reverse is true the
     * oids and values are listed out starting with the last element
     * in the sequence (ala RFC 2253), otherwise the string will begin
     * with the first element of the structure. If no string definition
     * for the oid is found in oidSymbols the string value of the oid is
     * added. Two standard symbol tables are provided DefaultSymbols, and
     * RFC2253Symbols as part of this class.
     *
     * @param reverse if true start at the end of the sequence and work back.
     * @param oidSymbols look up table strings for oids.
     */
    public String toString(
        boolean     reverse,
        Hashtable   oidSymbols)
    {
        StringBuffer            buf = new StringBuffer();
        boolean                 first = true;

        if (reverse)
        {
            // BEGIN android-changed
            for (int i = elems.size() - 1; i >= 0; i--)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    if (elems.getAdded(i + 1))
                    {
                        buf.append('+');
                    }
                    else
                    {
                        buf.append(',');
                    }
                }

                appendValue(buf, oidSymbols, 
                            elems.getKey(i),
                            elems.getValue(i));
            }
            // END android-changed
        }
        else
        {
            // BEGIN android-changed
            for (int i = 0; i < elems.size(); i++)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    if (elems.getAdded(i))
                    {
                        buf.append('+');
                    }
                    else
                    {
                        buf.append(',');
                    }
                }

                appendValue(buf, oidSymbols, 
                            elems.getKey(i),
                            elems.getValue(i));
            }
            // END android-changed
        }

        return buf.toString();
    }

    private String bytesToString(
        byte[] data)
    {
        char[]  cs = new char[data.length];

        for (int i = 0; i != cs.length; i++)
        {
            cs[i] = (char)(data[i] & 0xff);
        }

        return new String(cs);
    }
    
    public String toString()
    {
        return toString(DefaultReverse, DefaultSymbols);
    }
}
