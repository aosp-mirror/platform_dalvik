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
 * $Id: XMLErrorResources_pl.java 468653 2006-10-28 07:07:05Z minchau $
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
public class XMLErrorResources_pl extends ListResourceBundle
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
      "Nieobs\u0142ugiwana funkcja!"},

    { ER_CANNOT_OVERWRITE_CAUSE,
      "Nie mo\u017cna nadpisa\u0107 przyczyny"},

    { ER_NO_DEFAULT_IMPL,
      "Nie znaleziono domy\u015blnej implementacji"},

    { ER_CHUNKEDINTARRAY_NOT_SUPPORTED,
      "ChunkedIntArray({0}) nie jest obecnie obs\u0142ugiwane"},

    { ER_OFFSET_BIGGER_THAN_SLOT,
      "Przesuni\u0119cie wi\u0119ksze ni\u017c szczelina"},

    { ER_COROUTINE_NOT_AVAIL,
      "Koprocedura niedost\u0119pna, id={0}"},

    { ER_COROUTINE_CO_EXIT,
      "CoroutineManager otrzyma\u0142 \u017c\u0105danie co_exit()"},

    { ER_COJOINROUTINESET_FAILED,
      "co_joinCoroutineSet() nie powiod\u0142o si\u0119"},

    { ER_COROUTINE_PARAM,
      "B\u0142\u0105d parametru koprocedury ({0})"},

    { ER_PARSER_DOTERMINATE_ANSWERS,
      "\nNIEOCZEKIWANE: Analizator doTerminate odpowiada {0}"},

    { ER_NO_PARSE_CALL_WHILE_PARSING,
      "Nie mo\u017cna wywo\u0142a\u0107 parse podczas analizowania"},

    { ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED,
      "B\u0142\u0105d: Iterator okre\u015blonego typu dla osi {0} nie jest zaimplementowany"},

    { ER_ITERATOR_AXIS_NOT_IMPLEMENTED,
      "B\u0142\u0105d: Iterator dla osi {0} nie jest zaimplementowany"},

    { ER_ITERATOR_CLONE_NOT_SUPPORTED,
      "Kopia iteratora nie jest obs\u0142ugiwana"},

    { ER_UNKNOWN_AXIS_TYPE,
      "Nieznany typ przej\u015bcia osi {0}"},

    { ER_AXIS_NOT_SUPPORTED,
      "Nieobs\u0142ugiwane przej\u015bcie osi: {0}"},

    { ER_NO_DTMIDS_AVAIL,
      "Nie ma wi\u0119cej dost\u0119pnych identyfikator\u00f3w DTM"},

    { ER_NOT_SUPPORTED,
      "Nieobs\u0142ugiwane: {0}"},

    { ER_NODE_NON_NULL,
      "W\u0119ze\u0142 musi by\u0107 niepusty dla getDTMHandleFromNode"},

    { ER_COULD_NOT_RESOLVE_NODE,
      "Nie mo\u017cna przet\u0142umaczy\u0107 w\u0119z\u0142a na uchwyt"},

    { ER_STARTPARSE_WHILE_PARSING,
       "Nie mo\u017cna wywo\u0142a\u0107 startParse podczas analizowania"},

    { ER_STARTPARSE_NEEDS_SAXPARSER,
       "startParse potrzebuje niepustego SAXParser"},

    { ER_COULD_NOT_INIT_PARSER,
       "nie mo\u017cna zainicjowa\u0107 analizatora"},

    { ER_EXCEPTION_CREATING_POOL,
       "wyj\u0105tek podczas tworzenia nowej instancji dla puli"},

    { ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
       "\u015acie\u017cka zawiera nieznan\u0105 sekwencj\u0119 o zmienionym znaczeniu"},

    { ER_SCHEME_REQUIRED,
       "Schemat jest wymagany!"},

    { ER_NO_SCHEME_IN_URI,
       "Nie znaleziono schematu w URI {0}"},

    { ER_NO_SCHEME_INURI,
       "Nie znaleziono schematu w URI"},

    { ER_PATH_INVALID_CHAR,
       "\u015acie\u017cka zawiera niepoprawny znak {0}"},

    { ER_SCHEME_FROM_NULL_STRING,
       "Nie mo\u017cna ustawi\u0107 schematu z pustego ci\u0105gu znak\u00f3w"},

    { ER_SCHEME_NOT_CONFORMANT,
       "Schemat nie jest zgodny."},

    { ER_HOST_ADDRESS_NOT_WELLFORMED,
       "Host nie jest poprawnie skonstruowanym adresem"},

    { ER_PORT_WHEN_HOST_NULL,
       "Nie mo\u017cna ustawi\u0107 portu, kiedy host jest pusty"},

    { ER_INVALID_PORT,
       "Niepoprawny numer portu"},

    { ER_FRAG_FOR_GENERIC_URI,
       "Fragment mo\u017cna ustawi\u0107 tylko dla og\u00f3lnego URI"},

    { ER_FRAG_WHEN_PATH_NULL,
       "Nie mo\u017cna ustawi\u0107 fragmentu, kiedy \u015bcie\u017cka jest pusta"},

    { ER_FRAG_INVALID_CHAR,
       "Fragment zawiera niepoprawny znak"},

    { ER_PARSER_IN_USE,
      "Analizator jest ju\u017c u\u017cywany"},

    { ER_CANNOT_CHANGE_WHILE_PARSING,
      "Nie mo\u017cna zmieni\u0107 {0} {1} podczas analizowania"},

    { ER_SELF_CAUSATION_NOT_PERMITTED,
      "Bycie w\u0142asn\u0105 przyczyn\u0105 jest niedozwolone"},

    { ER_NO_USERINFO_IF_NO_HOST,
      "Nie mo\u017cna poda\u0107 informacji o u\u017cytkowniku, je\u015bli nie podano hosta"},

    { ER_NO_PORT_IF_NO_HOST,
      "Nie mo\u017cna poda\u0107 portu, je\u015bli nie podano hosta"},

    { ER_NO_QUERY_STRING_IN_PATH,
      "Tekstu zapytania nie mo\u017cna poda\u0107 w tek\u015bcie \u015bcie\u017cki i zapytania"},

    { ER_NO_FRAGMENT_STRING_IN_PATH,
      "Nie mo\u017cna poda\u0107 fragmentu jednocze\u015bnie w \u015bcie\u017cce i fragmencie"},

    { ER_CANNOT_INIT_URI_EMPTY_PARMS,
      "Nie mo\u017cna zainicjowa\u0107 URI z pustymi parametrami"},

    { ER_METHOD_NOT_SUPPORTED,
      "Metoda nie jest jeszcze obs\u0142ugiwana"},

    { ER_INCRSAXSRCFILTER_NOT_RESTARTABLE,
      "IncrementalSAXSource_Filter nie jest obecnie mo\u017cliwy do ponownego uruchomienia"},

    { ER_XMLRDR_NOT_BEFORE_STARTPARSE,
      "XMLReader nie mo\u017ce wyst\u0105pi\u0107 przed \u017c\u0105daniem startParse"},

    { ER_AXIS_TRAVERSER_NOT_SUPPORTED,
      "Nieobs\u0142ugiwane przej\u015bcie osi: {0}"},

    { ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER,
      "Utworzono ListingErrorHandler z pustym PrintWriter!"},

    { ER_SYSTEMID_UNKNOWN,
      "Nieznany identyfikator systemu"},

    { ER_LOCATION_UNKNOWN,
      "Po\u0142o\u017cenie b\u0142\u0119du jest nieznane"},

    { ER_PREFIX_MUST_RESOLVE,
      "Przedrostek musi da\u0107 si\u0119 przet\u0142umaczy\u0107 na przestrze\u0144 nazw: {0}"},

    { ER_CREATEDOCUMENT_NOT_SUPPORTED,
      "Funkcja createDocument() nie jest obs\u0142ugiwana w XPathContext!"},

    { ER_CHILD_HAS_NO_OWNER_DOCUMENT,
      "Bezpo\u015bredni element potomny atrybutu nie ma dokumentu w\u0142a\u015bciciela!"},

    { ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT,
      "Bezpo\u015bredni element potomny atrybutu nie ma elementu dokumentu w\u0142a\u015bciciela!"},

    { ER_CANT_OUTPUT_TEXT_BEFORE_DOC,
      "Ostrze\u017cenie: Nie mo\u017cna wyprowadzi\u0107 tekstu przed elementem dokumentu!  Ignorowanie..."},

    { ER_CANT_HAVE_MORE_THAN_ONE_ROOT,
      "Nie mo\u017cna mie\u0107 wi\u0119cej ni\u017c jeden element g\u0142\u00f3wny w DOM!"},

    { ER_ARG_LOCALNAME_NULL,
       "Argument 'localName' jest pusty"},

    // Note to translators:  A QNAME has the syntactic form [NCName:]NCName
    // The localname is the portion after the optional colon; the message indicates
    // that there is a problem with that part of the QNAME.
    { ER_ARG_LOCALNAME_INVALID,
       "Nazwa lokalna w QNAME powinna by\u0107 poprawn\u0105 nazw\u0105 NCName"},

    // Note to translators:  A QNAME has the syntactic form [NCName:]NCName
    // The prefix is the portion before the optional colon; the message indicates
    // that there is a problem with that part of the QNAME.
    { ER_ARG_PREFIX_INVALID,
       "Przedrostek w QNAME powinien by\u0107 poprawn\u0105 nazw\u0105 NCName"},

    { ER_NAME_CANT_START_WITH_COLON,
      "Nazwa nie mo\u017ce rozpoczyna\u0107 si\u0119 od dwukropka"},

    { "BAD_CODE", "Parametr createMessage by\u0142 spoza zakresu"},
    { "FORMAT_FAILED", "Podczas wywo\u0142ania messageFormat zg\u0142oszony zosta\u0142 wyj\u0105tek"},
    { "line", "Nr wiersza: "},
    { "column","Nr kolumny: "}


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
                new Locale("pl", "PL"));
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
