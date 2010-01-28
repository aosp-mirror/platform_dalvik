/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: XMLErrorResources_sl.java,v 1.9 2004/12/16 19:29:01 minchau Exp $
 */

package org.apache.xml.res;


import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Set up error messages.
 * We build a two dimensional array of message keys and
 * message strings. In order to add a new message here,
 * you need to first add a String constant. And you need
 * to enter key, value pair as part of the contents
 * array. You also need to update MAX_CODE for error strings
 * and MAX_WARNING for warnings ( Needed for only information
 * purpose )
 */
public class XMLErrorResources_sl extends ListResourceBundle
{

/*
 * This file contains error and warning messages related to Xalan Error
 * Handling.
 *
 *  General notes to translators:
 *
 *  1) Xalan (or more properly, Xalan-interpretive) and XSLTC are names of
 *     components.
 *     XSLT is an acronym for "XML Stylesheet Language: Transformations".
 *     XSLTC is an acronym for XSLT Compiler.
 *
 *  2) A stylesheet is a description of how to transform an input XML document
 *     into a resultant XML document (or HTML document or text).  The
 *     stylesheet itself is described in the form of an XML document.
 *
 *  3) A template is a component of a stylesheet that is used to match a
 *     particular portion of an input document and specifies the form of the
 *     corresponding portion of the output document.
 *
 *  4) An element is a mark-up tag in an XML document; an attribute is a
 *     modifier on the tag.  For example, in <elem attr='val' attr2='val2'>
 *     "elem" is an element name, "attr" and "attr2" are attribute names with
 *     the values "val" and "val2", respectively.
 *
 *  5) A namespace declaration is a special attribute that is used to associate
 *     a prefix with a URI (the namespace).  The meanings of element names and
 *     attribute names that use that prefix are defined with respect to that
 *     namespace.
 *
 *  6) "Translet" is an invented term that describes the class file that
 *     results from compiling an XML stylesheet into a Java class.
 *
 *  7) XPath is a specification that describes a notation for identifying
 *     nodes in a tree-structured representation of an XML document.  An
 *     instance of that notation is referred to as an XPath expression.
 *
 */

  /*
   * Message keys
   */
  public static final String ER_FUNCTION_NOT_SUPPORTED = "ER_FUNCTION_NOT_SUPPORTED";
  public static final String ER_CANNOT_OVERWRITE_CAUSE = "ER_CANNOT_OVERWRITE_CAUSE";
  public static final String ER_NO_DEFAULT_IMPL = "ER_NO_DEFAULT_IMPL";
  public static final String ER_CHUNKEDINTARRAY_NOT_SUPPORTED = "ER_CHUNKEDINTARRAY_NOT_SUPPORTED";
  public static final String ER_OFFSET_BIGGER_THAN_SLOT = "ER_OFFSET_BIGGER_THAN_SLOT";
  public static final String ER_COROUTINE_NOT_AVAIL = "ER_COROUTINE_NOT_AVAIL";
  public static final String ER_COROUTINE_CO_EXIT = "ER_COROUTINE_CO_EXIT";
  public static final String ER_COJOINROUTINESET_FAILED = "ER_COJOINROUTINESET_FAILED";
  public static final String ER_COROUTINE_PARAM = "ER_COROUTINE_PARAM";
  public static final String ER_PARSER_DOTERMINATE_ANSWERS = "ER_PARSER_DOTERMINATE_ANSWERS";
  public static final String ER_NO_PARSE_CALL_WHILE_PARSING = "ER_NO_PARSE_CALL_WHILE_PARSING";
  public static final String ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED = "ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED";
  public static final String ER_ITERATOR_AXIS_NOT_IMPLEMENTED = "ER_ITERATOR_AXIS_NOT_IMPLEMENTED";
  public static final String ER_ITERATOR_CLONE_NOT_SUPPORTED = "ER_ITERATOR_CLONE_NOT_SUPPORTED";
  public static final String ER_UNKNOWN_AXIS_TYPE = "ER_UNKNOWN_AXIS_TYPE";
  public static final String ER_AXIS_NOT_SUPPORTED = "ER_AXIS_NOT_SUPPORTED";
  public static final String ER_NO_DTMIDS_AVAIL = "ER_NO_DTMIDS_AVAIL";
  public static final String ER_NOT_SUPPORTED = "ER_NOT_SUPPORTED";
  public static final String ER_NODE_NON_NULL = "ER_NODE_NON_NULL";
  public static final String ER_COULD_NOT_RESOLVE_NODE = "ER_COULD_NOT_RESOLVE_NODE";
  public static final String ER_STARTPARSE_WHILE_PARSING = "ER_STARTPARSE_WHILE_PARSING";
  public static final String ER_STARTPARSE_NEEDS_SAXPARSER = "ER_STARTPARSE_NEEDS_SAXPARSER";
  public static final String ER_COULD_NOT_INIT_PARSER = "ER_COULD_NOT_INIT_PARSER";
  public static final String ER_EXCEPTION_CREATING_POOL = "ER_EXCEPTION_CREATING_POOL";
  public static final String ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE = "ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE";
  public static final String ER_SCHEME_REQUIRED = "ER_SCHEME_REQUIRED";
  public static final String ER_NO_SCHEME_IN_URI = "ER_NO_SCHEME_IN_URI";
  public static final String ER_NO_SCHEME_INURI = "ER_NO_SCHEME_INURI";
  public static final String ER_PATH_INVALID_CHAR = "ER_PATH_INVALID_CHAR";
  public static final String ER_SCHEME_FROM_NULL_STRING = "ER_SCHEME_FROM_NULL_STRING";
  public static final String ER_SCHEME_NOT_CONFORMANT = "ER_SCHEME_NOT_CONFORMANT";
  public static final String ER_HOST_ADDRESS_NOT_WELLFORMED = "ER_HOST_ADDRESS_NOT_WELLFORMED";
  public static final String ER_PORT_WHEN_HOST_NULL = "ER_PORT_WHEN_HOST_NULL";
  public static final String ER_INVALID_PORT = "ER_INVALID_PORT";
  public static final String ER_FRAG_FOR_GENERIC_URI ="ER_FRAG_FOR_GENERIC_URI";
  public static final String ER_FRAG_WHEN_PATH_NULL = "ER_FRAG_WHEN_PATH_NULL";
  public static final String ER_FRAG_INVALID_CHAR = "ER_FRAG_INVALID_CHAR";
  public static final String ER_PARSER_IN_USE = "ER_PARSER_IN_USE";
  public static final String ER_CANNOT_CHANGE_WHILE_PARSING = "ER_CANNOT_CHANGE_WHILE_PARSING";
  public static final String ER_SELF_CAUSATION_NOT_PERMITTED = "ER_SELF_CAUSATION_NOT_PERMITTED";
  public static final String ER_NO_USERINFO_IF_NO_HOST = "ER_NO_USERINFO_IF_NO_HOST";
  public static final String ER_NO_PORT_IF_NO_HOST = "ER_NO_PORT_IF_NO_HOST";
  public static final String ER_NO_QUERY_STRING_IN_PATH = "ER_NO_QUERY_STRING_IN_PATH";
  public static final String ER_NO_FRAGMENT_STRING_IN_PATH = "ER_NO_FRAGMENT_STRING_IN_PATH";
  public static final String ER_CANNOT_INIT_URI_EMPTY_PARMS = "ER_CANNOT_INIT_URI_EMPTY_PARMS";
  public static final String ER_METHOD_NOT_SUPPORTED ="ER_METHOD_NOT_SUPPORTED";
  public static final String ER_INCRSAXSRCFILTER_NOT_RESTARTABLE = "ER_INCRSAXSRCFILTER_NOT_RESTARTABLE";
  public static final String ER_XMLRDR_NOT_BEFORE_STARTPARSE = "ER_XMLRDR_NOT_BEFORE_STARTPARSE";
  public static final String ER_AXIS_TRAVERSER_NOT_SUPPORTED = "ER_AXIS_TRAVERSER_NOT_SUPPORTED";
  public static final String ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER = "ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER";
  public static final String ER_SYSTEMID_UNKNOWN = "ER_SYSTEMID_UNKNOWN";
  public static final String ER_LOCATION_UNKNOWN = "ER_LOCATION_UNKNOWN";
  public static final String ER_PREFIX_MUST_RESOLVE = "ER_PREFIX_MUST_RESOLVE";
  public static final String ER_CREATEDOCUMENT_NOT_SUPPORTED = "ER_CREATEDOCUMENT_NOT_SUPPORTED";
  public static final String ER_CHILD_HAS_NO_OWNER_DOCUMENT = "ER_CHILD_HAS_NO_OWNER_DOCUMENT";
  public static final String ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT = "ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT";
  public static final String ER_CANT_OUTPUT_TEXT_BEFORE_DOC = "ER_CANT_OUTPUT_TEXT_BEFORE_DOC";
  public static final String ER_CANT_HAVE_MORE_THAN_ONE_ROOT = "ER_CANT_HAVE_MORE_THAN_ONE_ROOT";
  public static final String ER_ARG_LOCALNAME_NULL = "ER_ARG_LOCALNAME_NULL";
  public static final String ER_ARG_LOCALNAME_INVALID = "ER_ARG_LOCALNAME_INVALID";
  public static final String ER_ARG_PREFIX_INVALID = "ER_ARG_PREFIX_INVALID";
  public static final String ER_NAME_CANT_START_WITH_COLON = "ER_NAME_CANT_START_WITH_COLON";

  /*
   * Now fill in the message text.
   * Then fill in the message text for that message code in the
   * array. Use the new error code as the index into the array.
   */

  // Error messages...

  /**
   * Get the lookup table for error messages
   *
   * @return The association list.
   */
  public Object[][] getContents()
  {
    return new Object[][] {

  /** Error message ID that has a null message, but takes in a single object.    */
    {"ER0000" , "{0}" },

    { ER_FUNCTION_NOT_SUPPORTED,
      "Funkcija ni podprta!"},

    { ER_CANNOT_OVERWRITE_CAUSE,
      "Vzroka ni mogo\u010de prepisati"},

    { ER_NO_DEFAULT_IMPL,
      "Privzete implementacije ni mogo\u010de najti "},

    { ER_CHUNKEDINTARRAY_NOT_SUPPORTED,
      "ChunkedIntArray({0}) trenutno ni podprt"},

    { ER_OFFSET_BIGGER_THAN_SLOT,
      "Odmik ve\u010dji od re\u017ee"},

    { ER_COROUTINE_NOT_AVAIL,
      "Sorutina ni na voljo, id={0}"},

    { ER_COROUTINE_CO_EXIT,
      "CoroutineManager je prejel zahtevo co_exit()"},

    { ER_COJOINROUTINESET_FAILED,
      "co_joinCoroutineSet() je spodletela"},

    { ER_COROUTINE_PARAM,
      "Napaka parametra sorutine ({0})"},

    { ER_PARSER_DOTERMINATE_ANSWERS,
      "\nNEPRI\u010cAKOVANO: Odgovor raz\u010dlenjevalnika doTerminate je {0}"},

    { ER_NO_PARSE_CALL_WHILE_PARSING,
      "med raz\u010dlenjevanjem klic raz\u010dlenitve ni mo\u017een"},

    { ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED,
      "Napaka: dolo\u010den iterator za os {0} ni implementiran"},

    { ER_ITERATOR_AXIS_NOT_IMPLEMENTED,
      "Napaka: iterator za os {0} ni implementiran "},

    { ER_ITERATOR_CLONE_NOT_SUPPORTED,
      "Klon iteratorja ni podprt"},

    { ER_UNKNOWN_AXIS_TYPE,
      "Neznan pre\u010dni tip osi: {0}"},

    { ER_AXIS_NOT_SUPPORTED,
      "Pre\u010dnik osi ni podprt: {0}"},

    { ER_NO_DTMIDS_AVAIL,
      "Na voljo ni ve\u010d DTM ID-jev"},

    { ER_NOT_SUPPORTED,
      "Ni podprto: {0}"},

    { ER_NODE_NON_NULL,
      "Vozli\u0161\u010de ne sme biti NULL za getDTMHandleFromNode"},

    { ER_COULD_NOT_RESOLVE_NODE,
      "Ne morem razre\u0161iti vozli\u0161\u010da v obravnavo"},

    { ER_STARTPARSE_WHILE_PARSING,
       "Med raz\u010dlenjevanjem klic startParse ni mogo\u010d"},

    { ER_STARTPARSE_NEEDS_SAXPARSER,
       "startParse potrebuje ne-NULL SAXParser"},

    { ER_COULD_NOT_INIT_PARSER,
       "parserja ni mogo\u010de inicializirati z"},

    { ER_EXCEPTION_CREATING_POOL,
       "izjema pri ustvarjanju novega primerka za zalogo"},

    { ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
       "Pot vsebuje neveljavno zaporedje za izhod"},

    { ER_SCHEME_REQUIRED,
       "Zahtevana je shema!"},

    { ER_NO_SCHEME_IN_URI,
       "Ne najdem sheme v URI: {0}"},

    { ER_NO_SCHEME_INURI,
       "Ne najdem sheme v URI"},

    { ER_PATH_INVALID_CHAR,
       "Pot vsebuje neveljaven znak: {0}"},

    { ER_SCHEME_FROM_NULL_STRING,
       "Ne morem nastaviti sheme iz niza NULL"},

    { ER_SCHEME_NOT_CONFORMANT,
       "Shema ni skladna."},

    { ER_HOST_ADDRESS_NOT_WELLFORMED,
       "Naslov gostitelja ni pravilno oblikovan"},

    { ER_PORT_WHEN_HOST_NULL,
       "Ko je gostitelj NULL, nastavitev vrat ni mogo\u010da"},

    { ER_INVALID_PORT,
       "Neveljavna \u0161tevilka vrat"},

    { ER_FRAG_FOR_GENERIC_URI,
       "Fragment je lahko nastavljen samo za splo\u0161ni URI"},

    { ER_FRAG_WHEN_PATH_NULL,
       "Ko je pot NULL, nastavitev fragmenta ni mogo\u010da"},

    { ER_FRAG_INVALID_CHAR,
       "Fragment vsebuje neveljaven znak"},

    { ER_PARSER_IN_USE,
      "Raz\u010dlenjevalnik je \u017ee v uporabi"},

    { ER_CANNOT_CHANGE_WHILE_PARSING,
      "Med raz\u010dlenjevanjem ni mogo\u010de spremeniti {0} {1}"},

    { ER_SELF_CAUSATION_NOT_PERMITTED,
      "Samopovzro\u010ditev ni dovoljena"},

    { ER_NO_USERINFO_IF_NO_HOST,
      "Informacije o uporabniku ne morejo biti navedene, \u010de ni naveden gostitelj"},

    { ER_NO_PORT_IF_NO_HOST,
      "Vrata ne morejo biti navedena, \u010de ni naveden gostitelj"},

    { ER_NO_QUERY_STRING_IN_PATH,
      "Poizvedbeni niz ne more biti naveden v nizu poti in poizvedbenem nizu"},

    { ER_NO_FRAGMENT_STRING_IN_PATH,
      "Fragment ne more biti hkrati naveden v poti in v fragmentu"},

    { ER_CANNOT_INIT_URI_EMPTY_PARMS,
      "Ne morem inicializirat URI-ja s praznimi parametri"},

    { ER_METHOD_NOT_SUPPORTED,
      "Metoda ni ve\u010d podprta "},

    { ER_INCRSAXSRCFILTER_NOT_RESTARTABLE,
      "IncrementalSAXSource_Filter v tem trenutku ni mogo\u010de ponovno zagnati"},

    { ER_XMLRDR_NOT_BEFORE_STARTPARSE,
      "XMLReader ne pred zahtevo za startParse"},

    { ER_AXIS_TRAVERSER_NOT_SUPPORTED,
      "Pre\u010dnik osi ni podprt: {0}"},

    { ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER,
      "ListingErrorHandler ustvarjen s PrintWriter NULL!"},

    { ER_SYSTEMID_UNKNOWN,
      "Neznan sistemski ID"},

    { ER_LOCATION_UNKNOWN,
      "Mesto napake neznano"},

    { ER_PREFIX_MUST_RESOLVE,
      "Predpona se mora razre\u0161iti v imenski prostor: {0}"},

    { ER_CREATEDOCUMENT_NOT_SUPPORTED,
      "createDocument() ni podprt v XPathContext!"},

    { ER_CHILD_HAS_NO_OWNER_DOCUMENT,
      "Podrejeni predmet atributa nima lastni\u0161kega dokumenta!"},

    { ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT,
      "Podrejeni predmet atributa nima elementa lastni\u0161kega dokumenta!"},

    { ER_CANT_OUTPUT_TEXT_BEFORE_DOC,
      "Opozorilo: besedila ne morem prikazati pred elementom dokumenta!  Ignoriram..."},

    { ER_CANT_HAVE_MORE_THAN_ONE_ROOT,
      "Na DOM-u ne more biti ve\u010d kot en koren!"},

    { ER_ARG_LOCALNAME_NULL,
       "Argument 'lokalno ime' je NULL"},

    // Note to translators:  A QNAME has the syntactic form [NCName:]NCName
    // The localname is the portion after the optional colon; the message indicates
    // that there is a problem with that part of the QNAME.
    { ER_ARG_LOCALNAME_INVALID,
       "Lokalno ime v QNAME bi moralo biti veljavno NCIme"},

    // Note to translators:  A QNAME has the syntactic form [NCName:]NCName
    // The prefix is the portion before the optional colon; the message indicates
    // that there is a problem with that part of the QNAME.
    { ER_ARG_PREFIX_INVALID,
       "Predpona v QNAME bi morala biti valjavno NCIme"},

    { ER_NAME_CANT_START_WITH_COLON,
      "Ime se ne more za\u010deti z dvopi\u010djem"},

    { "BAD_CODE", "Parameter za ustvariSporo\u010dilo presega meje"},
    { "FORMAT_FAILED", "Med klicem messageFormat naletel na izjemo"},
    { "line", "Vrstica #"},
    { "column","Stolpec #"}


  };
  }

  /**
   *   Return a named ResourceBundle for a particular locale.  This method mimics the behavior
   *   of ResourceBundle.getBundle().
   *
   *   @param className the name of the class that implements the resource bundle.
   *   @return the ResourceBundle
   *   @throws MissingResourceException
   */
  public static final XMLErrorResources loadResourceBundle(String className)
          throws MissingResourceException
  {

    Locale locale = Locale.getDefault();
    String suffix = getResourceSuffix(locale);

    try
    {

      // first try with the given locale
      return (XMLErrorResources) ResourceBundle.getBundle(className
              + suffix, locale);
    }
    catch (MissingResourceException e)
    {
      try  // try to fall back to en_US if we can't load
      {

        // Since we can't find the localized property file,
        // fall back to en_US.
        return (XMLErrorResources) ResourceBundle.getBundle(className,
                new Locale("sl", "SL"));
      }
      catch (MissingResourceException e2)
      {

        // Now we are really in trouble.
        // very bad, definitely very bad...not going to get very far
        throw new MissingResourceException(
          "Could not load any resource bundles.", className, "");
      }
    }
  }

  /**
   * Return the resource file suffic for the indicated locale
   * For most locales, this will be based the language code.  However
   * for Chinese, we do distinguish between Taiwan and PRC
   *
   * @param locale the locale
   * @return an String suffix which canbe appended to a resource name
   */
  private static final String getResourceSuffix(Locale locale)
  {

    String suffix = "_" + locale.getLanguage();
    String country = locale.getCountry();

    if (country.equals("TW"))
      suffix += "_" + country;

    return suffix;
  }

}
