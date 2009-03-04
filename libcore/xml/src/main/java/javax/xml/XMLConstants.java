/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.xml;

/**
 * Defines several standard constants that are often used during XML processing.
 *  
 * @since Android 1.0
 */
public class XMLConstants {

    /**
     * The default namespace prefix. Defined to be the empty string. 
     */
    public static final String DEFAULT_NS_PREFIX = "";

    /**
     * The SAX feature name for secure processing. Turning on this feature
     * might result in a parser rejecting XML documents that are considered
     * "insecure" (having a potential for DOS attacks, for example). The
     * Android XML parsing implementation currently ignores this feature.  
     */
    public static final String FEATURE_SECURE_PROCESSING = 
        "http://javax.xml.XMLConstants/feature/secure-processing";

    /**
     * The namespace URI for the case that no namespace is being used at all.
     * Defined to be the empty string.
     */
    public static final String NULL_NS_URI = "";

    /**
     * The official Relax-NG namespace URI.
     */
    public static final String RELAXNG_NS_URI = 
        "http://relaxng.org/ns/structure/1.0";

    /**
     * The official XSchema instance namespace URI, as defined by W3C.
     */
    public static final String W3C_XML_SCHEMA_INSTANCE_NS_URI = 
        "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * The official XSchema namespace URI, as defined by W3C.
     */
    public static final String W3C_XML_SCHEMA_NS_URI = 
        "http://www.w3.org/2001/XMLSchema";

    /**
     * The official XPath datatype namespace URI, as defined by W3C.
     */
    public static final String W3C_XPATH_DATATYPE_NS_URI = 
        "http://www.w3.org/2003/11/xpath-datatypes";

    /**
     * The official XML namespace attribute, as defined by W3C.
     */
    public static final String XMLNS_ATTRIBUTE = "xmlns";

    /**
     * The official XML namespace attribute URI, as defined by W3C.
     */
    public static final String XMLNS_ATTRIBUTE_NS_URI = 
        "http://www.w3.org/2000/xmlns/";

    /**
     * The official XML DTD namespace URI, as defined by W3C.
     */
    public static final String XML_DTD_NS_URI = "http://www.w3.org/TR/REC-xml";

    /**
     * The official XML namespace prefix, as defined by W3C. 
     */
    public static final String XML_NS_PREFIX = "xml";

    /**
     * The official XML namespace URI, as defined by W3C. 
     */
    public static final String XML_NS_URI = 
        "http://www.w3.org/XML/1998/namespace";

}
