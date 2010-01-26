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
 * $Id: XPATHErrorResources_pl.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xpath.res;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Set up error messages.
 * We build a two dimensional array of message keys and
 * message strings. In order to add a new message here,
 * you need to first add a Static string constant for the
 * Key and update the contents array with Key, Value pair
  * Also you need to  update the count of messages(MAX_CODE)or
 * the count of warnings(MAX_WARNING) [ Information purpose only]
 * @xsl.usage advanced
 */
public class XPATHErrorResources_pl extends ListResourceBundle
{

/*
 * General notes to translators:
 *
 * This file contains error and warning messages related to XPath Error
 * Handling.
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
 *  8) The context node is the node in the document with respect to which an
 *     XPath expression is being evaluated.
 *
 *  9) An iterator is an object that traverses nodes in the tree, one at a time.
 *
 *  10) NCName is an XML term used to describe a name that does not contain a
 *     colon (a "no-colon name").
 *
 *  11) QName is an XML term meaning "qualified name".
 */

  /*
   * static variables
   */
  public static final String ERROR0000 = "ERROR0000";
  public static final String ER_CURRENT_NOT_ALLOWED_IN_MATCH =
         "ER_CURRENT_NOT_ALLOWED_IN_MATCH";
  public static final String ER_CURRENT_TAKES_NO_ARGS =
         "ER_CURRENT_TAKES_NO_ARGS";
  public static final String ER_DOCUMENT_REPLACED = "ER_DOCUMENT_REPLACED";
  public static final String ER_CONTEXT_HAS_NO_OWNERDOC =
         "ER_CONTEXT_HAS_NO_OWNERDOC";
  public static final String ER_LOCALNAME_HAS_TOO_MANY_ARGS =
         "ER_LOCALNAME_HAS_TOO_MANY_ARGS";
  public static final String ER_NAMESPACEURI_HAS_TOO_MANY_ARGS =
         "ER_NAMESPACEURI_HAS_TOO_MANY_ARGS";
  public static final String ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS =
         "ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS";
  public static final String ER_NUMBER_HAS_TOO_MANY_ARGS =
         "ER_NUMBER_HAS_TOO_MANY_ARGS";
  public static final String ER_NAME_HAS_TOO_MANY_ARGS =
         "ER_NAME_HAS_TOO_MANY_ARGS";
  public static final String ER_STRING_HAS_TOO_MANY_ARGS =
         "ER_STRING_HAS_TOO_MANY_ARGS";
  public static final String ER_STRINGLENGTH_HAS_TOO_MANY_ARGS =
         "ER_STRINGLENGTH_HAS_TOO_MANY_ARGS";
  public static final String ER_TRANSLATE_TAKES_3_ARGS =
         "ER_TRANSLATE_TAKES_3_ARGS";
  public static final String ER_UNPARSEDENTITYURI_TAKES_1_ARG =
         "ER_UNPARSEDENTITYURI_TAKES_1_ARG";
  public static final String ER_NAMESPACEAXIS_NOT_IMPLEMENTED =
         "ER_NAMESPACEAXIS_NOT_IMPLEMENTED";
  public static final String ER_UNKNOWN_AXIS = "ER_UNKNOWN_AXIS";
  public static final String ER_UNKNOWN_MATCH_OPERATION =
         "ER_UNKNOWN_MATCH_OPERATION";
  public static final String ER_INCORRECT_ARG_LENGTH ="ER_INCORRECT_ARG_LENGTH";
  public static final String ER_CANT_CONVERT_TO_NUMBER =
         "ER_CANT_CONVERT_TO_NUMBER";
  public static final String ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER =
           "ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER";
  public static final String ER_CANT_CONVERT_TO_NODELIST =
         "ER_CANT_CONVERT_TO_NODELIST";
  public static final String ER_CANT_CONVERT_TO_MUTABLENODELIST =
         "ER_CANT_CONVERT_TO_MUTABLENODELIST";
  public static final String ER_CANT_CONVERT_TO_TYPE ="ER_CANT_CONVERT_TO_TYPE";
  public static final String ER_EXPECTED_MATCH_PATTERN =
         "ER_EXPECTED_MATCH_PATTERN";
  public static final String ER_COULDNOT_GET_VAR_NAMED =
         "ER_COULDNOT_GET_VAR_NAMED";
  public static final String ER_UNKNOWN_OPCODE = "ER_UNKNOWN_OPCODE";
  public static final String ER_EXTRA_ILLEGAL_TOKENS ="ER_EXTRA_ILLEGAL_TOKENS";
  public static final String ER_EXPECTED_DOUBLE_QUOTE =
         "ER_EXPECTED_DOUBLE_QUOTE";
  public static final String ER_EXPECTED_SINGLE_QUOTE =
         "ER_EXPECTED_SINGLE_QUOTE";
  public static final String ER_EMPTY_EXPRESSION = "ER_EMPTY_EXPRESSION";
  public static final String ER_EXPECTED_BUT_FOUND = "ER_EXPECTED_BUT_FOUND";
  public static final String ER_INCORRECT_PROGRAMMER_ASSERTION =
         "ER_INCORRECT_PROGRAMMER_ASSERTION";
  public static final String ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL =
         "ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL";
  public static final String ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG =
         "ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG";
  public static final String ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG =
         "ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG";
  public static final String ER_PREDICATE_ILLEGAL_SYNTAX =
         "ER_PREDICATE_ILLEGAL_SYNTAX";
  public static final String ER_ILLEGAL_AXIS_NAME = "ER_ILLEGAL_AXIS_NAME";
  public static final String ER_UNKNOWN_NODETYPE = "ER_UNKNOWN_NODETYPE";
  public static final String ER_PATTERN_LITERAL_NEEDS_BE_QUOTED =
         "ER_PATTERN_LITERAL_NEEDS_BE_QUOTED";
  public static final String ER_COULDNOT_BE_FORMATTED_TO_NUMBER =
         "ER_COULDNOT_BE_FORMATTED_TO_NUMBER";
  public static final String ER_COULDNOT_CREATE_XMLPROCESSORLIAISON =
         "ER_COULDNOT_CREATE_XMLPROCESSORLIAISON";
  public static final String ER_DIDNOT_FIND_XPATH_SELECT_EXP =
         "ER_DIDNOT_FIND_XPATH_SELECT_EXP";
  public static final String ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH =
         "ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH";
  public static final String ER_ERROR_OCCURED = "ER_ERROR_OCCURED";
  public static final String ER_ILLEGAL_VARIABLE_REFERENCE =
         "ER_ILLEGAL_VARIABLE_REFERENCE";
  public static final String ER_AXES_NOT_ALLOWED = "ER_AXES_NOT_ALLOWED";
  public static final String ER_KEY_HAS_TOO_MANY_ARGS =
         "ER_KEY_HAS_TOO_MANY_ARGS";
  public static final String ER_COUNT_TAKES_1_ARG = "ER_COUNT_TAKES_1_ARG";
  public static final String ER_COULDNOT_FIND_FUNCTION =
         "ER_COULDNOT_FIND_FUNCTION";
  public static final String ER_UNSUPPORTED_ENCODING ="ER_UNSUPPORTED_ENCODING";
  public static final String ER_PROBLEM_IN_DTM_NEXTSIBLING =
         "ER_PROBLEM_IN_DTM_NEXTSIBLING";
  public static final String ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL =
         "ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL";
  public static final String ER_SETDOMFACTORY_NOT_SUPPORTED =
         "ER_SETDOMFACTORY_NOT_SUPPORTED";
  public static final String ER_PREFIX_MUST_RESOLVE = "ER_PREFIX_MUST_RESOLVE";
  public static final String ER_PARSE_NOT_SUPPORTED = "ER_PARSE_NOT_SUPPORTED";
  public static final String ER_SAX_API_NOT_HANDLED = "ER_SAX_API_NOT_HANDLED";
public static final String ER_IGNORABLE_WHITESPACE_NOT_HANDLED =
         "ER_IGNORABLE_WHITESPACE_NOT_HANDLED";
  public static final String ER_DTM_CANNOT_HANDLE_NODES =
         "ER_DTM_CANNOT_HANDLE_NODES";
  public static final String ER_XERCES_CANNOT_HANDLE_NODES =
         "ER_XERCES_CANNOT_HANDLE_NODES";
  public static final String ER_XERCES_PARSE_ERROR_DETAILS =
         "ER_XERCES_PARSE_ERROR_DETAILS";
  public static final String ER_XERCES_PARSE_ERROR = "ER_XERCES_PARSE_ERROR";
  public static final String ER_INVALID_UTF16_SURROGATE =
         "ER_INVALID_UTF16_SURROGATE";
  public static final String ER_OIERROR = "ER_OIERROR";
  public static final String ER_CANNOT_CREATE_URL = "ER_CANNOT_CREATE_URL";
  public static final String ER_XPATH_READOBJECT = "ER_XPATH_READOBJECT";
 public static final String ER_FUNCTION_TOKEN_NOT_FOUND =
         "ER_FUNCTION_TOKEN_NOT_FOUND";
  public static final String ER_CANNOT_DEAL_XPATH_TYPE =
         "ER_CANNOT_DEAL_XPATH_TYPE";
  public static final String ER_NODESET_NOT_MUTABLE = "ER_NODESET_NOT_MUTABLE";
  public static final String ER_NODESETDTM_NOT_MUTABLE =
         "ER_NODESETDTM_NOT_MUTABLE";
   /**  Variable not resolvable:   */
  public static final String ER_VAR_NOT_RESOLVABLE = "ER_VAR_NOT_RESOLVABLE";
   /** Null error handler  */
 public static final String ER_NULL_ERROR_HANDLER = "ER_NULL_ERROR_HANDLER";
   /**  Programmer's assertion: unknown opcode  */
  public static final String ER_PROG_ASSERT_UNKNOWN_OPCODE =
         "ER_PROG_ASSERT_UNKNOWN_OPCODE";
   /**  0 or 1   */
  public static final String ER_ZERO_OR_ONE = "ER_ZERO_OR_ONE";
   /**  rtf() not supported by XRTreeFragSelectWrapper   */
  public static final String ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER =
         "ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER";
   /**  asNodeIterator() not supported by XRTreeFragSelectWrapper   */
  public static final String ER_ASNODEITERATOR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER = "ER_ASNODEITERATOR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER";
   /**  fsb() not supported for XStringForChars   */
  public static final String ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS =
         "ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS";
   /**  Could not find variable with the name of   */
 public static final String ER_COULD_NOT_FIND_VAR = "ER_COULD_NOT_FIND_VAR";
   /**  XStringForChars can not take a string for an argument   */
 public static final String ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING =
         "ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING";
   /**  The FastStringBuffer argument can not be null   */
 public static final String ER_FASTSTRINGBUFFER_CANNOT_BE_NULL =
         "ER_FASTSTRINGBUFFER_CANNOT_BE_NULL";
   /**  2 or 3   */
  public static final String ER_TWO_OR_THREE = "ER_TWO_OR_THREE";
   /** Variable accessed before it is bound! */
  public static final String ER_VARIABLE_ACCESSED_BEFORE_BIND =
         "ER_VARIABLE_ACCESSED_BEFORE_BIND";
   /** XStringForFSB can not take a string for an argument! */
 public static final String ER_FSB_CANNOT_TAKE_STRING =
         "ER_FSB_CANNOT_TAKE_STRING";
   /** Error! Setting the root of a walker to null! */
  public static final String ER_SETTING_WALKER_ROOT_TO_NULL =
         "ER_SETTING_WALKER_ROOT_TO_NULL";
   /** This NodeSetDTM can not iterate to a previous node! */
  public static final String ER_NODESETDTM_CANNOT_ITERATE =
         "ER_NODESETDTM_CANNOT_ITERATE";
  /** This NodeSet can not iterate to a previous node! */
 public static final String ER_NODESET_CANNOT_ITERATE =
         "ER_NODESET_CANNOT_ITERATE";
  /** This NodeSetDTM can not do indexing or counting functions! */
  public static final String ER_NODESETDTM_CANNOT_INDEX =
         "ER_NODESETDTM_CANNOT_INDEX";
  /** This NodeSet can not do indexing or counting functions! */
  public static final String ER_NODESET_CANNOT_INDEX =
         "ER_NODESET_CANNOT_INDEX";
  /** Can not call setShouldCacheNodes after nextNode has been called! */
  public static final String ER_CANNOT_CALL_SETSHOULDCACHENODE =
         "ER_CANNOT_CALL_SETSHOULDCACHENODE";
  /** {0} only allows {1} arguments */
 public static final String ER_ONLY_ALLOWS = "ER_ONLY_ALLOWS";
  /** Programmer's assertion in getNextStepPos: unknown stepType: {0} */
  public static final String ER_UNKNOWN_STEP = "ER_UNKNOWN_STEP";
  /** Problem with RelativeLocationPath */
  public static final String ER_EXPECTED_REL_LOC_PATH =
         "ER_EXPECTED_REL_LOC_PATH";
  /** Problem with LocationPath */
  public static final String ER_EXPECTED_LOC_PATH = "ER_EXPECTED_LOC_PATH";
  public static final String ER_EXPECTED_LOC_PATH_AT_END_EXPR =
                                        "ER_EXPECTED_LOC_PATH_AT_END_EXPR";
  /** Problem with Step */
  public static final String ER_EXPECTED_LOC_STEP = "ER_EXPECTED_LOC_STEP";
  /** Problem with NodeTest */
  public static final String ER_EXPECTED_NODE_TEST = "ER_EXPECTED_NODE_TEST";
  /** Expected step pattern */
  public static final String ER_EXPECTED_STEP_PATTERN =
        "ER_EXPECTED_STEP_PATTERN";
  /** Expected relative path pattern */
  public static final String ER_EXPECTED_REL_PATH_PATTERN =
         "ER_EXPECTED_REL_PATH_PATTERN";
  /** ER_CANT_CONVERT_XPATHRESULTTYPE_TO_BOOLEAN          */
  public static final String ER_CANT_CONVERT_TO_BOOLEAN =
         "ER_CANT_CONVERT_TO_BOOLEAN";
  /** Field ER_CANT_CONVERT_TO_SINGLENODE       */
  public static final String ER_CANT_CONVERT_TO_SINGLENODE =
         "ER_CANT_CONVERT_TO_SINGLENODE";
  /** Field ER_CANT_GET_SNAPSHOT_LENGTH         */
  public static final String ER_CANT_GET_SNAPSHOT_LENGTH =
         "ER_CANT_GET_SNAPSHOT_LENGTH";
  /** Field ER_NON_ITERATOR_TYPE                */
  public static final String ER_NON_ITERATOR_TYPE = "ER_NON_ITERATOR_TYPE";
  /** Field ER_DOC_MUTATED                      */
  public static final String ER_DOC_MUTATED = "ER_DOC_MUTATED";
  public static final String ER_INVALID_XPATH_TYPE = "ER_INVALID_XPATH_TYPE";
  public static final String ER_EMPTY_XPATH_RESULT = "ER_EMPTY_XPATH_RESULT";
  public static final String ER_INCOMPATIBLE_TYPES = "ER_INCOMPATIBLE_TYPES";
  public static final String ER_NULL_RESOLVER = "ER_NULL_RESOLVER";
  public static final String ER_CANT_CONVERT_TO_STRING =
         "ER_CANT_CONVERT_TO_STRING";
  public static final String ER_NON_SNAPSHOT_TYPE = "ER_NON_SNAPSHOT_TYPE";
  public static final String ER_WRONG_DOCUMENT = "ER_WRONG_DOCUMENT";
  /* Note to translators:  The XPath expression cannot be evaluated with respect
   * to this type of node.
   */
  /** Field ER_WRONG_NODETYPE                    */
  public static final String ER_WRONG_NODETYPE = "ER_WRONG_NODETYPE";
  public static final String ER_XPATH_ERROR = "ER_XPATH_ERROR";

  //BEGIN: Keys needed for exception messages of  JAXP 1.3 XPath API implementation
  public static final String ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED = "ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED";
  public static final String ER_RESOLVE_VARIABLE_RETURNS_NULL = "ER_RESOLVE_VARIABLE_RETURNS_NULL";
  public static final String ER_UNSUPPORTED_RETURN_TYPE = "ER_UNSUPPORTED_RETURN_TYPE";
  public static final String ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL = "ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL";
  public static final String ER_ARG_CANNOT_BE_NULL = "ER_ARG_CANNOT_BE_NULL";

  public static final String ER_OBJECT_MODEL_NULL = "ER_OBJECT_MODEL_NULL";
  public static final String ER_OBJECT_MODEL_EMPTY = "ER_OBJECT_MODEL_EMPTY";
  public static final String ER_FEATURE_NAME_NULL = "ER_FEATURE_NAME_NULL";
  public static final String ER_FEATURE_UNKNOWN = "ER_FEATURE_UNKNOWN";
  public static final String ER_GETTING_NULL_FEATURE = "ER_GETTING_NULL_FEATURE";
  public static final String ER_GETTING_UNKNOWN_FEATURE = "ER_GETTING_UNKNOWN_FEATURE";
  public static final String ER_NULL_XPATH_FUNCTION_RESOLVER = "ER_NULL_XPATH_FUNCTION_RESOLVER";
  public static final String ER_NULL_XPATH_VARIABLE_RESOLVER = "ER_NULL_XPATH_VARIABLE_RESOLVER";
  //END: Keys needed for exception messages of  JAXP 1.3 XPath API implementation

  public static final String WG_LOCALE_NAME_NOT_HANDLED =
         "WG_LOCALE_NAME_NOT_HANDLED";
  public static final String WG_PROPERTY_NOT_SUPPORTED =
         "WG_PROPERTY_NOT_SUPPORTED";
  public static final String WG_DONT_DO_ANYTHING_WITH_NS =
         "WG_DONT_DO_ANYTHING_WITH_NS";
  public static final String WG_SECURITY_EXCEPTION = "WG_SECURITY_EXCEPTION";
  public static final String WG_QUO_NO_LONGER_DEFINED =
         "WG_QUO_NO_LONGER_DEFINED";
  public static final String WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST =
         "WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST";
  public static final String WG_FUNCTION_TOKEN_NOT_FOUND =
         "WG_FUNCTION_TOKEN_NOT_FOUND";
  public static final String WG_COULDNOT_FIND_FUNCTION =
         "WG_COULDNOT_FIND_FUNCTION";
  public static final String WG_CANNOT_MAKE_URL_FROM ="WG_CANNOT_MAKE_URL_FROM";
  public static final String WG_EXPAND_ENTITIES_NOT_SUPPORTED =
         "WG_EXPAND_ENTITIES_NOT_SUPPORTED";
  public static final String WG_ILLEGAL_VARIABLE_REFERENCE =
         "WG_ILLEGAL_VARIABLE_REFERENCE";
  public static final String WG_UNSUPPORTED_ENCODING ="WG_UNSUPPORTED_ENCODING";

  /**  detach() not supported by XRTreeFragSelectWrapper   */
  public static final String ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER =
         "ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER";
  /**  num() not supported by XRTreeFragSelectWrapper   */
  public static final String ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER =
         "ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER";
  /**  xstr() not supported by XRTreeFragSelectWrapper   */
  public static final String ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER =
         "ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER";
  /**  str() not supported by XRTreeFragSelectWrapper   */
  public static final String ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER =
         "ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER";

  // Error messages...


  /**
   * Get the association list.
   *
   * @return The association list.
   */
  public Object[][] getContents()
  {
    return new Object[][]{

  { "ERROR0000" , "{0}" },

  { ER_CURRENT_NOT_ALLOWED_IN_MATCH, "Funkcja current() jest niedozwolona we wzorcu!" },

  { ER_CURRENT_TAKES_NO_ARGS, "Funkcja current() nie akceptuje argument\u00f3w!" },

  { ER_DOCUMENT_REPLACED,
      "Implementacja funkcji document() zosta\u0142a zast\u0105piona przez org.apache.xalan.xslt.FuncDocument!"},

  { ER_CONTEXT_HAS_NO_OWNERDOC,
      "Kontekst nie ma dokumentu w\u0142a\u015bciciela!"},

  { ER_LOCALNAME_HAS_TOO_MANY_ARGS,
      "Funkcja local-name() ma zbyt wiele argument\u00f3w."},

  { ER_NAMESPACEURI_HAS_TOO_MANY_ARGS,
      "Funkcja namespace-uri() ma zbyt wiele argument\u00f3w."},

  { ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS,
      "Funkcja normalize-space() ma zbyt wiele argument\u00f3w."},

  { ER_NUMBER_HAS_TOO_MANY_ARGS,
      "Funkcja number() ma zbyt wiele argument\u00f3w."},

  { ER_NAME_HAS_TOO_MANY_ARGS,
     "Funkcja name() ma zbyt wiele argument\u00f3w."},

  { ER_STRING_HAS_TOO_MANY_ARGS,
      "Funkcja string() ma zbyt wiele argument\u00f3w."},

  { ER_STRINGLENGTH_HAS_TOO_MANY_ARGS,
      "Funkcja string-length() ma zbyt wiele argument\u00f3w."},

  { ER_TRANSLATE_TAKES_3_ARGS,
      "Funkcja translate() akceptuje trzy argumenty!"},

  { ER_UNPARSEDENTITYURI_TAKES_1_ARG,
      "Funkcja unparsed-entity-uri() akceptuje tylko jeden argument!"},

  { ER_NAMESPACEAXIS_NOT_IMPLEMENTED,
      "O\u015b przestrzeni nazw nie zosta\u0142a jeszcze zaimplementowana!"},

  { ER_UNKNOWN_AXIS,
     "nieznana o\u015b: {0}"},

  { ER_UNKNOWN_MATCH_OPERATION,
     "Nieznana operacja uzgadniania!"},

  { ER_INCORRECT_ARG_LENGTH,
      "D\u0142ugo\u015b\u0107 argumentu testu w\u0119z\u0142a processing-instruction() jest niepoprawna!"},

  { ER_CANT_CONVERT_TO_NUMBER,
      "Nie mo\u017cna przekszta\u0142ci\u0107 {0} w liczb\u0119"},

  { ER_CANT_CONVERT_TO_NODELIST,
      "Nie mo\u017cna przekszta\u0142ci\u0107 {0} w NodeList!"},

  { ER_CANT_CONVERT_TO_MUTABLENODELIST,
      "Nie mo\u017cna przekszta\u0142ci\u0107 {0} w NodeSetDTM!"},

  { ER_CANT_CONVERT_TO_TYPE,
      "Nie mo\u017cna przekszta\u0142ci\u0107 {0} w type#{1}"},

  { ER_EXPECTED_MATCH_PATTERN,
      "Oczekiwano wzorca uzgadniania w getMatchScore!"},

  { ER_COULDNOT_GET_VAR_NAMED,
      "Nie mo\u017cna pobra\u0107 zmiennej o nazwie {0}"},

  { ER_UNKNOWN_OPCODE,
     "B\u0141\u0104D! Nieznany kod operacji: {0}"},

  { ER_EXTRA_ILLEGAL_TOKENS,
     "Nadmiarowe niedozwolone leksemy: {0}"},


  { ER_EXPECTED_DOUBLE_QUOTE,
      "Litera\u0142 bez cudzys\u0142owu... oczekiwano podw\u00f3jnego cudzys\u0142owu!"},

  { ER_EXPECTED_SINGLE_QUOTE,
      "Litera\u0142 bez cudzys\u0142owu... oczekiwano pojedynczego cudzys\u0142owu!"},

  { ER_EMPTY_EXPRESSION,
     "Puste wyra\u017cenie!"},

  { ER_EXPECTED_BUT_FOUND,
     "Oczekiwano {0}, ale znaleziono: {1}"},

  { ER_INCORRECT_PROGRAMMER_ASSERTION,
      "Asercja programisty jest niepoprawna! - {0}"},

  { ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,
      "argument boolean(...) nie jest ju\u017c opcjonalny wg projektu 19990709 XPath draft."},

  { ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,
      "Znaleziono znak ',', ale nie ma poprzedzaj\u0105cego argumentu!"},

  { ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
      "Znaleziono znak ',', ale nie ma nast\u0119puj\u0105cego argumentu!"},

  { ER_PREDICATE_ILLEGAL_SYNTAX,
      "'..[predykat]' lub '.[predykat]' to niedozwolona sk\u0142adnia. U\u017cyj zamiast tego 'self::node()[predykat]'."},

  { ER_ILLEGAL_AXIS_NAME,
     "Niedozwolona nazwa osi: {0}"},

  { ER_UNKNOWN_NODETYPE,
     "Nieznany typ w\u0119z\u0142a: {0}"},

  { ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
      "Litera\u0142 wzorca ({0}) musi by\u0107 w cudzys\u0142owie!"},

  { ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
      "Nie mo\u017cna sformatowa\u0107 {0} do postaci liczbowej!"},

  { ER_COULDNOT_CREATE_XMLPROCESSORLIAISON,
      "Nie mo\u017cna utworzy\u0107 po\u0142\u0105czenia XML TransformerFactory: {0}"},

  { ER_DIDNOT_FIND_XPATH_SELECT_EXP,
      "B\u0142\u0105d! Nie znaleziono wyra\u017cenia wyboru xpath (-select)."},

  { ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH,
      "B\u0141\u0104D! Nie mo\u017cna znale\u017a\u0107 ENDOP po OP_LOCATIONPATH"},

  { ER_ERROR_OCCURED,
     "Wyst\u0105pi\u0142 b\u0142\u0105d!"},

  { ER_ILLEGAL_VARIABLE_REFERENCE,
      "VariableReference nadana zmiennej nie nale\u017cy do kontekstu lub nie ma definicji!  Nazwa = {0}"},

  { ER_AXES_NOT_ALLOWED,
      "We wzorcach zgodno\u015bci dozwolone s\u0105 tylko osie child:: oraz attribute::!  Niew\u0142a\u015bciwe osie = {0}"},

  { ER_KEY_HAS_TOO_MANY_ARGS,
      "Funkcja key() ma niepoprawn\u0105 liczb\u0119 argument\u00f3w."},

  { ER_COUNT_TAKES_1_ARG,
      "Funkcja count() akceptuje tylko jeden argument!"},

  { ER_COULDNOT_FIND_FUNCTION,
     "Nie mo\u017cna znale\u017a\u0107 funkcji: {0}"},

  { ER_UNSUPPORTED_ENCODING,
     "Nieobs\u0142ugiwane kodowanie: {0}"},

  { ER_PROBLEM_IN_DTM_NEXTSIBLING,
      "Wyst\u0105pi\u0142 problem w DTM w getNextSibling... pr\u00f3ba wyj\u015bcia z b\u0142\u0119du"},

  { ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL,
      "B\u0142\u0105d programisty: Nie mo\u017cna zapisywa\u0107 do EmptyNodeList."},

  { ER_SETDOMFACTORY_NOT_SUPPORTED,
      "setDOMFactory nie jest obs\u0142ugiwane przez XPathContext!"},

  { ER_PREFIX_MUST_RESOLVE,
      "Przedrostek musi da\u0107 si\u0119 przet\u0142umaczy\u0107 na przestrze\u0144 nazw: {0}"},

  { ER_PARSE_NOT_SUPPORTED,
      "parse (InputSource \u017ar\u00f3d\u0142o) nie jest obs\u0142ugiwane w XPathContext! Nie mo\u017cna otworzy\u0107 {0}"},

  { ER_SAX_API_NOT_HANDLED,
      "SAX API characters(char ch[]... nie jest obs\u0142ugiwane przez DTM!"},

  { ER_IGNORABLE_WHITESPACE_NOT_HANDLED,
      "ignorableWhitespace(char ch[]... nie jest obs\u0142ugiwane przez DTM!"},

  { ER_DTM_CANNOT_HANDLE_NODES,
      "DTMLiaison nie mo\u017ce obs\u0142u\u017cy\u0107 w\u0119z\u0142\u00f3w typu {0}"},

  { ER_XERCES_CANNOT_HANDLE_NODES,
      "DOM2Helper nie mo\u017ce obs\u0142u\u017cy\u0107 w\u0119z\u0142\u00f3w typu {0}"},

  { ER_XERCES_PARSE_ERROR_DETAILS,
      "B\u0142\u0105d DOM2Helper.parse : ID systemu - {0} wiersz - {1}"},

  { ER_XERCES_PARSE_ERROR,
     "B\u0142\u0105d DOM2Helper.parse"},

  { ER_INVALID_UTF16_SURROGATE,
      "Wykryto niepoprawny odpowiednik UTF-16: {0} ?"},

  { ER_OIERROR,
     "B\u0142\u0105d we/wy"},

  { ER_CANNOT_CREATE_URL,
     "Nie mo\u017cna utworzy\u0107 adresu url dla {0}"},

  { ER_XPATH_READOBJECT,
     "W XPath.readObject: {0}"},

  { ER_FUNCTION_TOKEN_NOT_FOUND,
      "Nie znaleziono leksemu funkcji."},

  { ER_CANNOT_DEAL_XPATH_TYPE,
       "Nie mo\u017cna upora\u0107 si\u0119 z typem XPath {0}"},

  { ER_NODESET_NOT_MUTABLE,
       "Ten NodeSet nie jest zmienny"},

  { ER_NODESETDTM_NOT_MUTABLE,
       "Ten NodeSetDTM nie jest zmienny"},

  { ER_VAR_NOT_RESOLVABLE,
        "Nie mo\u017cna rozstrzygn\u0105\u0107 zmiennej {0}"},

  { ER_NULL_ERROR_HANDLER,
        "Pusta procedura obs\u0142ugi b\u0142\u0119du"},

  { ER_PROG_ASSERT_UNKNOWN_OPCODE,
       "Asercja programisty: nieznany kod opcode: {0}"},

  { ER_ZERO_OR_ONE,
       "0 lub 1"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "Funkcja rtf() nie jest obs\u0142ugiwana przez XRTreeFragSelectWrapper"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "Funkcja asNodeIterator() nie jest obs\u0142ugiwana przez XRTreeFragSelectWrapper"},

   { ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "Funkcja detach() nie jest obs\u0142ugiwana przez XRTreeFragSelectWrapper"},

   { ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "Funkcja num() nie jest obs\u0142ugiwana przez XRTreeFragSelectWrapper"},

   { ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "Funkcja xstr() nie jest obs\u0142ugiwana przez XRTreeFragSelectWrapper"},

   { ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "Funkcja str() nie jest obs\u0142ugiwana przez XRTreeFragSelectWrapper"},

  { ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,
       "Funkcja fsb() nie jest obs\u0142ugiwana dla XStringForChars"},

  { ER_COULD_NOT_FIND_VAR,
      "Nie mo\u017cna znale\u017a\u0107 zmiennej o nazwie {0}"},

  { ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,
      "XStringForChars nie mo\u017ce pobra\u0107 ci\u0105gu znak\u00f3w jako argumentu"},

  { ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,
      "Argument FastStringBuffer nie mo\u017ce by\u0107 pusty"},

  { ER_TWO_OR_THREE,
       "2 lub 3"},

  { ER_VARIABLE_ACCESSED_BEFORE_BIND,
       "Nast\u0105pi\u0142o odwo\u0142anie do zmiennej, zanim zosta\u0142a ona zwi\u0105zana!"},

  { ER_FSB_CANNOT_TAKE_STRING,
       "XStringForFSB nie mo\u017ce pobra\u0107 ci\u0105gu znak\u00f3w jako argumentu!"},

  { ER_SETTING_WALKER_ROOT_TO_NULL,
       "\n !!!! B\u0142\u0105d! Ustawienie root w\u0119drownika na null!!!"},

  { ER_NODESETDTM_CANNOT_ITERATE,
       "Ten NodeSetDTM nie mo\u017ce iterowa\u0107 do poprzedniego w\u0119z\u0142a!"},

  { ER_NODESET_CANNOT_ITERATE,
       "Ten NodeSet nie mo\u017ce iterowa\u0107 do poprzedniego w\u0119z\u0142a!"},

  { ER_NODESETDTM_CANNOT_INDEX,
       "Ten NodeSetDTM nie mo\u017ce wykona\u0107 funkcji indeksowania lub zliczania!"},

  { ER_NODESET_CANNOT_INDEX,
       "Ten NodeSet nie mo\u017ce wykona\u0107 funkcji indeksowania lub zliczania!"},

  { ER_CANNOT_CALL_SETSHOULDCACHENODE,
       "Nie mo\u017cna wywo\u0142a\u0107 setShouldCacheNodes po wywo\u0142aniu nextNode!"},

  { ER_ONLY_ALLOWS,
       "{0} zezwala tylko na {1} argument\u00f3w"},

  { ER_UNKNOWN_STEP,
       "Asercja programisty w getNextStepPos: nieznany stepType: {0}"},

  //Note to translators:  A relative location path is a form of XPath expression.
  // The message indicates that such an expression was expected following the
  // characters '/' or '//', but was not found.
  { ER_EXPECTED_REL_LOC_PATH,
      "Po leksemie '/' oraz '//' oczekiwana by\u0142a \u015bcie\u017cka wzgl\u0119dna po\u0142o\u017cenia."},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such an expression was expected,but
  // the characters specified by the substitution text were encountered instead.
  { ER_EXPECTED_LOC_PATH,
       "Oczekiwano \u015bcie\u017cki po\u0142o\u017cenia, ale napotkano nast\u0119puj\u0105cy leksem\u003a  {0}"},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such a subexpression was expected,
  // but no more characters were found in the expression.
  { ER_EXPECTED_LOC_PATH_AT_END_EXPR,
       "Oczekiwano \u015bcie\u017cki po\u0142o\u017cenia, ale zamiast niej znaleziono koniec wyra\u017cenia XPath."},

  // Note to translators:  A location step is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected
  // following the specified characters.
  { ER_EXPECTED_LOC_STEP,
       "Po leksemie '/' oraz '//' oczekiwany by\u0142 krok po\u0142o\u017cenia."},

  // Note to translators:  A node test is part of an XPath expression that is
  // used to test for particular kinds of nodes.  In this case, a node test that
  // consists of an NCName followed by a colon and an asterisk or that consists
  // of a QName was expected, but was not found.
  { ER_EXPECTED_NODE_TEST,
       "Oczekiwano testu w\u0119z\u0142a zgodnego albo z NCName:*, albo z QName."},

  // Note to translators:  A step pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but the specified character was found in the expression instead.
  { ER_EXPECTED_STEP_PATTERN,
       "Oczekiwano wzorca kroku, ale napotkano '/'."},

  // Note to translators: A relative path pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but was not found.
  { ER_EXPECTED_REL_PATH_PATTERN,
       "Oczekiwano wzorca \u015bcie\u017cki wzgl\u0119dnej."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type boolean.
  { ER_CANT_CONVERT_TO_BOOLEAN,
       "Rezultat (XPathResult) wyra\u017cenia XPath ''{0}'' ma typ (XPathResultType) {1}, kt\u00f3rego nie mo\u017cna przekszta\u0142ci\u0107 w typ boolowski."},

  // Note to translators: Do not translate ANY_UNORDERED_NODE_TYPE and
  // FIRST_ORDERED_NODE_TYPE.
  { ER_CANT_CONVERT_TO_SINGLENODE,
       "Rezultat (XPathResult) wyra\u017cenia XPath ''{0}'' ma typ (XPathResultType) {1}, kt\u00f3rego nie mo\u017cna przekszta\u0142ci\u0107 w pojedynczy w\u0119ze\u0142. Metod\u0119 getSingleNodeValue mo\u017cna stosowa\u0107 tylko do typ\u00f3w ANY_UNORDERED_NODE_TYPE oraz FIRST_ORDERED_NODE_TYPE."},

  // Note to translators: Do not translate UNORDERED_NODE_SNAPSHOT_TYPE and
  // ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_CANT_GET_SNAPSHOT_LENGTH,
       "Metody getSnapshotLength nie mo\u017cna wywo\u0142a\u0107 na rezultacie (XPathResult) wyra\u017cenia XPath ''{0}'', poniewa\u017c jego typem (XPathResultType) jest {1}. Metod\u0119 t\u0119 mo\u017cna stosowa\u0107 tylko do typ\u00f3w UNORDERED_NODE_SNAPSHOT_TYPE oraz ORDERED_NODE_SNAPSHOT_TYPE."},

  { ER_NON_ITERATOR_TYPE,
       "Metody iterateNext nie mo\u017cna wywo\u0142a\u0107 na rezultacie (XPathResult) wyra\u017cenia XPath ''{0}'', poniewa\u017c jego typem (XPathResultType) jest {1}. Metod\u0119 t\u0119 mo\u017cna stosowa\u0107 tylko do typ\u00f3w UNORDERED_NODE_ITERATOR_TYPE oraz ORDERED_NODE_ITERATOR_TYPE."},

  // Note to translators: This message indicates that the document being operated
  // upon changed, so the iterator object that was being used to traverse the
  // document has now become invalid.
  { ER_DOC_MUTATED,
       "Dokument uleg\u0142 zmianie od czasu zwr\u00f3cenia rezultatu. Iterator jest niepoprawny."},

  { ER_INVALID_XPATH_TYPE,
       "Niepoprawny argument typu XPath: {0}"},

  { ER_EMPTY_XPATH_RESULT,
       "Pusty obiekt rezultatu XPath"},

  { ER_INCOMPATIBLE_TYPES,
       "Rezultat (XPathResult) wyra\u017cenia XPath ''{0}'' ma typ (XPathResultType) {1}, na kt\u00f3rym nie mo\u017cna wymusi\u0107 dzia\u0142ania jak na okre\u015blonym typie (XPathResultType) {2}."},

  { ER_NULL_RESOLVER,
       "Nie mo\u017cna przet\u0142umaczy\u0107 przedrostka za pomoc\u0105 procedury t\u0142umacz\u0105cej o pustym przedrostku."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.
  { ER_CANT_CONVERT_TO_STRING,
       "Rezultat (XPathResult) wyra\u017cenia XPath ''{0}'' ma typ (XPathResultType) {1}, kt\u00f3rego nie mo\u017cna przekszta\u0142ci\u0107 w typ \u0142a\u0144cuchowy."},

  // Note to translators: Do not translate snapshotItem,
  // UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_NON_SNAPSHOT_TYPE,
       "Metody snapshotItem nie mo\u017cna wywo\u0142a\u0107 na rezultacie (XPathResult) wyra\u017cenia XPath ''{0}'', poniewa\u017c jego typem (XPathResultType) jest {1}. Metod\u0119 t\u0119 mo\u017cna stosowa\u0107 tylko do typ\u00f3w UNORDERED_NODE_SNAPSHOT_TYPE oraz ORDERED_NODE_SNAPSHOT_TYPE."},

  // Note to translators:  XPathEvaluator is a Java interface name.  An
  // XPathEvaluator is created with respect to a particular XML document, and in
  // this case the expression represented by this object was being evaluated with
  // respect to a context node from a different document.
  { ER_WRONG_DOCUMENT,
       "W\u0119ze\u0142 kontekstu nie nale\u017cy do dokumentu, kt\u00f3ry jest zwi\u0105zany z tym interfejsem XPathEvaluator."},

  // Note to translators:  The XPath expression cannot be evaluated with respect
  // to this type of node.
  { ER_WRONG_NODETYPE,
       "Nieobs\u0142ugiwany typ w\u0119z\u0142a kontekstu."},

  { ER_XPATH_ERROR,
       "Nieznany b\u0142\u0105d w XPath."},

        { ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER,
                "Rezultat (XPathResult) wyra\u017cenia XPath ''{0}'' ma typ (XPathResultType) {1}, kt\u00f3rego nie mo\u017cna przekszta\u0142ci\u0107 w typ liczbowy."},

 //BEGIN:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  /** Field ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED                       */

  { ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
       "Nie mo\u017cna wywo\u0142a\u0107 funkcji rozszerzenia ''{0}'', kiedy opcja XMLConstants.FEATURE_SECURE_PROCESSING ma warto\u015b\u0107 true."},

  /** Field ER_RESOLVE_VARIABLE_RETURNS_NULL                       */

  { ER_RESOLVE_VARIABLE_RETURNS_NULL,
       "resolveVariable zwraca warto\u015b\u0107 null dla zmiennej {0}"},

  /** Field ER_UNSUPPORTED_RETURN_TYPE                       */

  { ER_UNSUPPORTED_RETURN_TYPE,
       "Nieobs\u0142ugiwany typ zwracany : {0}"},

  /** Field ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL                       */

  { ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL,
       "Typ \u017ar\u00f3d\u0142owy i/lub zwracany nie mo\u017ce mie\u0107 warto\u015bci null"},

  /** Field ER_ARG_CANNOT_BE_NULL                       */

  { ER_ARG_CANNOT_BE_NULL,
       "Argument {0} nie mo\u017ce mie\u0107 warto\u015bci null"},

  /** Field ER_OBJECT_MODEL_NULL                       */

  { ER_OBJECT_MODEL_NULL,
       "Nie mo\u017cna wywo\u0142a\u0107 {0}#isObjectModelSupported( String objectModel ) ze zmienn\u0105 objectModel o warto\u015bci null"},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_OBJECT_MODEL_EMPTY,
       "Nie mo\u017cna wywo\u0142a\u0107 {0}#isObjectModelSupported( String objectModel ) ze zmienn\u0105 objectModel o warto\u015bci \"\""},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_FEATURE_NAME_NULL,
       "Pr\u00f3ba ustawienia opcji o nazwie r\u00f3wnej null: {0}#setFeature( null, {1})"},

  /** Field ER_FEATURE_UNKNOWN                       */

  { ER_FEATURE_UNKNOWN,
       "Pr\u00f3ba ustawienia nieznanej opcji \"{0}\":{1}#setFeature({0},{2})"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_NULL_FEATURE,
       "Pr\u00f3ba pobrania opcji o nazwie r\u00f3wnej null: {0}#getFeature(null)"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_UNKNOWN_FEATURE,
       "Pr\u00f3ba pobrania nieznanej opcji \"{0}\":{1}#getFeature({0})"},

  /** Field ER_NULL_XPATH_FUNCTION_RESOLVER                       */

  { ER_NULL_XPATH_FUNCTION_RESOLVER,
       "Pr\u00f3ba ustawienia XPathFunctionResolver o warto\u015bci null:{0}#setXPathFunctionResolver(null)"},

  /** Field ER_NULL_XPATH_VARIABLE_RESOLVER                       */

  { ER_NULL_XPATH_VARIABLE_RESOLVER,
       "Pr\u00f3ba ustawienia XPathVariableResolver o warto\u015bci null:{0}#setXPathVariableResolver(null)"},

  //END:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  // Warnings...

  { WG_LOCALE_NAME_NOT_HANDLED,
      "Nazwa ustawie\u0144 narodowych w funkcji format-number nie jest jeszcze obs\u0142ugiwana!"},

  { WG_PROPERTY_NOT_SUPPORTED,
      "Nieobs\u0142ugiwana w\u0142a\u015bciwo\u015b\u0107 XSL {0}"},

  { WG_DONT_DO_ANYTHING_WITH_NS,
      "Nie r\u00f3b teraz niczego z przestrzeni\u0105 nazw {0} we w\u0142a\u015bciwo\u015bci {1}"},

  { WG_SECURITY_EXCEPTION,
      "Wyj\u0105tek SecurityException podczas pr\u00f3by dost\u0119pu do w\u0142a\u015bciwo\u015bci systemowej XSL {0}"},

  { WG_QUO_NO_LONGER_DEFINED,
      "Stara sk\u0142adnia: quo(...) nie jest ju\u017c zdefiniowana w XPath."},

  { WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST,
      "XPath potrzebuje obiektu pochodnego, aby zaimplementowa\u0107 nodeTest!"},

  { WG_FUNCTION_TOKEN_NOT_FOUND,
      "Nie znaleziono leksemu funkcji."},

  { WG_COULDNOT_FIND_FUNCTION,
      "Nie mo\u017cna znale\u017a\u0107 funkcji: {0}"},

  { WG_CANNOT_MAKE_URL_FROM,
      "Nie mo\u017cna utworzy\u0107 adresu URL z {0}"},

  { WG_EXPAND_ENTITIES_NOT_SUPPORTED,
      "Opcja -E nie jest obs\u0142ugiwana przez analizator DTM"},

  { WG_ILLEGAL_VARIABLE_REFERENCE,
      "VariableReference nadana zmiennej nie nale\u017cy do kontekstu lub nie ma definicji!  Nazwa = {0}"},

  { WG_UNSUPPORTED_ENCODING,
     "Nieobs\u0142ugiwane kodowanie: {0}"},



  // Other miscellaneous text used inside the code...
  { "ui_language", "pl"},
  { "help_language", "pl"},
  { "language", "pl"},
  { "BAD_CODE", "Parametr createMessage by\u0142 spoza zakresu"},
  { "FORMAT_FAILED", "Podczas wywo\u0142ania messageFormat zg\u0142oszony zosta\u0142 wyj\u0105tek"},
  { "version", ">>>>>>> Wersja Xalan "},
  { "version2", "<<<<<<<"},
  { "yes", "tak"},
  { "line", "Nr wiersza: "},
  { "column", "Nr kolumny: "},
  { "xsldone", "XSLProcessor: gotowe"},
  { "xpath_option", "opcje xpath: "},
  { "optionIN", "[-in wej\u015bciowyXMLURL]"},
  { "optionSelect", "[-select wyra\u017cenie xpath]"},
  { "optionMatch", "[-match wzorzec (do diagnostyki odnajdywania zgodno\u015bci ze wzorcem)]"},
  { "optionAnyExpr", "Lub po prostu wyra\u017cenie xpath dokona zrzutu diagnostycznego"},
  { "noParsermsg1", "Proces XSL nie wykona\u0142 si\u0119 pomy\u015blnie."},
  { "noParsermsg2", "** Nie mo\u017cna znale\u017a\u0107 analizatora **"},
  { "noParsermsg3", "Sprawd\u017a classpath."},
  { "noParsermsg4", "Je\u015bli nie masz analizatora XML dla j\u0119zyka Java firmy IBM, mo\u017cesz go pobra\u0107 "},
  { "noParsermsg5", "z serwisu AlphaWorks firmy IBM: http://www.alphaworks.ibm.com/formula/xml"},
  { "gtone", ">1" },
  { "zero", "0" },
  { "one", "1" },
  { "two" , "2" },
  { "three", "3" }

  };
  }


  // ================= INFRASTRUCTURE ======================

  /** Field BAD_CODE          */
  public static final String BAD_CODE = "BAD_CODE";

  /** Field FORMAT_FAILED          */
  public static final String FORMAT_FAILED = "FORMAT_FAILED";

  /** Field ERROR_RESOURCES          */
  public static final String ERROR_RESOURCES =
    "org.apache.xpath.res.XPATHErrorResources";

  /** Field ERROR_STRING          */
  public static final String ERROR_STRING = "nr b\u0142\u0119du";

  /** Field ERROR_HEADER          */
  public static final String ERROR_HEADER = "B\u0142\u0105d: ";

  /** Field WARNING_HEADER          */
  public static final String WARNING_HEADER = "Ostrze\u017cenie: ";

  /** Field XSL_HEADER          */
  public static final String XSL_HEADER = "XSL ";

  /** Field XML_HEADER          */
  public static final String XML_HEADER = "XML ";

  /** Field QUERY_HEADER          */
  public static final String QUERY_HEADER = "WZORZEC ";


  /**
   * Return a named ResourceBundle for a particular locale.  This method mimics the behavior
   * of ResourceBundle.getBundle().
   *
   * @param className Name of local-specific subclass.
   * @return the ResourceBundle
   * @throws MissingResourceException
   */
  public static final XPATHErrorResources loadResourceBundle(String className)
          throws MissingResourceException
  {

    Locale locale = Locale.getDefault();
    String suffix = getResourceSuffix(locale);

    try
    {

      // first try with the given locale
      return (XPATHErrorResources) ResourceBundle.getBundle(className
              + suffix, locale);
    }
    catch (MissingResourceException e)
    {
      try  // try to fall back to en_US if we can't load
      {

        // Since we can't find the localized property file,
        // fall back to en_US.
        return (XPATHErrorResources) ResourceBundle.getBundle(className,
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
