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
 * $Id: XPATHErrorResources_cs.java 468655 2006-10-28 07:12:06Z minchau $
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
public class XPATHErrorResources_cs extends ListResourceBundle
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

  { ER_CURRENT_NOT_ALLOWED_IN_MATCH, "Funkce current() nen\u00ed ve vzorku shody povolena!" },

  { ER_CURRENT_TAKES_NO_ARGS, "Funkce current() neakceptuje argumenty!" },

  { ER_DOCUMENT_REPLACED,
      "implementace funkce document() byla nahrazena funkc\u00ed org.apache.xalan.xslt.FuncDocument!"},

  { ER_CONTEXT_HAS_NO_OWNERDOC,
      "Parametr context nem\u00e1 dokument vlastn\u00edka!"},

  { ER_LOCALNAME_HAS_TOO_MANY_ARGS,
      "P\u0159\u00edli\u0161 mnoho argument\u016f funkce local-name()."},

  { ER_NAMESPACEURI_HAS_TOO_MANY_ARGS,
      "P\u0159\u00edli\u0161 mnoho argument\u016f funkce namespace-uri()."},

  { ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS,
      "P\u0159\u00edli\u0161 mnoho argument\u016f funkce normalize-space()."},

  { ER_NUMBER_HAS_TOO_MANY_ARGS,
      "P\u0159\u00edli\u0161 mnoho argument\u016f funkce number()."},

  { ER_NAME_HAS_TOO_MANY_ARGS,
     "P\u0159\u00edli\u0161 mnoho argument\u016f funkce name()."},

  { ER_STRING_HAS_TOO_MANY_ARGS,
      "P\u0159\u00edli\u0161 mnoho argument\u016f funkce string()."},

  { ER_STRINGLENGTH_HAS_TOO_MANY_ARGS,
      "P\u0159\u00edli\u0161 mnoho argument\u016f funkce string-length()."},

  { ER_TRANSLATE_TAKES_3_ARGS,
      "Funkce translate() akceptuje t\u0159i argumenty!"},

  { ER_UNPARSEDENTITYURI_TAKES_1_ARG,
      "Funkce unparsed-entity-uri mus\u00ed akceptovat jeden argument!"},

  { ER_NAMESPACEAXIS_NOT_IMPLEMENTED,
      "Obor n\u00e1zv\u016f axis nebyl je\u0161t\u011b implementov\u00e1n!"},

  { ER_UNKNOWN_AXIS,
     "nezn\u00e1m\u00fd parametr axis: {0}"},

  { ER_UNKNOWN_MATCH_OPERATION,
     "nezn\u00e1m\u00e1 operace shody!"},

  { ER_INCORRECT_ARG_LENGTH,
      "Nespr\u00e1vn\u00e1 d\u00e9lka argumentu testu uzlu processing-instruction()!"},

  { ER_CANT_CONVERT_TO_NUMBER,
      "{0} nelze p\u0159ev\u00e9st na parametr number"},

  { ER_CANT_CONVERT_TO_NODELIST,
      "{0} nelze p\u0159ev\u00e9st na parametr NodeList!"},

  { ER_CANT_CONVERT_TO_MUTABLENODELIST,
      "{0} nelze p\u0159ev\u00e9st na parametr NodeSetDTM!"},

  { ER_CANT_CONVERT_TO_TYPE,
      "{0} nelze p\u0159ev\u00e9st na parametr type#{1}"},

  { ER_EXPECTED_MATCH_PATTERN,
      "Funkce getMatchScore o\u010dek\u00e1v\u00e1 parametr!"},

  { ER_COULDNOT_GET_VAR_NAMED,
      "Nelze z\u00edskat prom\u011bnnou s n\u00e1zvem {0}"},

  { ER_UNKNOWN_OPCODE,
     "Chyba! Nezn\u00e1m\u00fd k\u00f3d operace: {0}"},

  { ER_EXTRA_ILLEGAL_TOKENS,
     "Dal\u0161\u00ed nepovolen\u00e9 tokeny: {0}"},


  { ER_EXPECTED_DOUBLE_QUOTE,
      "nespr\u00e1vn\u011b uveden\u00fd liter\u00e1l... Byly o\u010dek\u00e1v\u00e1ny uvozovky!"},

  { ER_EXPECTED_SINGLE_QUOTE,
      "nespr\u00e1vn\u011b uveden\u00fd liter\u00e1l... Byly o\u010dek\u00e1v\u00e1ny jednoduch\u00e9 uvozovky!"},

  { ER_EMPTY_EXPRESSION,
     "Pr\u00e1zdn\u00fd v\u00fdraz!"},

  { ER_EXPECTED_BUT_FOUND,
     "O\u010dek\u00e1v\u00e1no: {0}, ale nalezeno: {1}"},

  { ER_INCORRECT_PROGRAMMER_ASSERTION,
      "Nespr\u00e1vn\u00e9 tvrzen\u00ed program\u00e1tora! - {0}"},

  { ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,
      "booleovsk\u00fd(...) argument ji\u017e nen\u00ed v n\u00e1vrhu 19990709 XPath voliteln\u00fd."},

  { ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,
      "Byl nalezen znak ',' bez p\u0159edchoz\u00edho argumentu!"},

  { ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
      "Byl nalezen znak ',' bez n\u00e1sleduj\u00edc\u00edho argumentu!"},

  { ER_PREDICATE_ILLEGAL_SYNTAX,
      "V\u00fdraz '..[predicate]' nebo '.[predicate]' m\u00e1 nespr\u00e1vnou syntaxi. Pou\u017eijte m\u00edsto toho 'self::node()[predicate]'."},

  { ER_ILLEGAL_AXIS_NAME,
     "nepovolen\u00fd n\u00e1zev osy: {0}"},

  { ER_UNKNOWN_NODETYPE,
     "Nezn\u00e1m\u00fd typ uzlu: {0}"},

  { ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
      "Je nutno uv\u00e9st vzorek liter\u00e1lu ({0})!"},

  { ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
      "{0} nelze zform\u00e1tovat jako \u010d\u00edslo!"},

  { ER_COULDNOT_CREATE_XMLPROCESSORLIAISON,
      "Nelze vytvo\u0159it prvek XML TransformerFactory Liaison: {0}"},

  { ER_DIDNOT_FIND_XPATH_SELECT_EXP,
      "Chyba! Nebyl nalezen v\u00fdraz v\u00fdb\u011bru xpath (-select)."},

  { ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH,
      "Chyba! Nebyl nalezen v\u00fdraz ENDOP po OP_LOCATIONPATH"},

  { ER_ERROR_OCCURED,
     "Do\u0161lo k chyb\u011b!"},

  { ER_ILLEGAL_VARIABLE_REFERENCE,
      "Odkaz VariableReference uveden k prom\u011bnn\u00e9 mimo kontext nebo bez definice!  N\u00e1zev = {0}"},

  { ER_AXES_NOT_ALLOWED,
      "Ve vzorc\u00edch shody jsou povoleny pouze osy child:: a attribute::!  Nepovolen\u00e9 osy = {0}"},

  { ER_KEY_HAS_TOO_MANY_ARGS,
      "nespr\u00e1vn\u00fd po\u010det argument\u016f parametru key()."},

  { ER_COUNT_TAKES_1_ARG,
      "Funkce count mus\u00ed obsahovat jeden argument!"},

  { ER_COULDNOT_FIND_FUNCTION,
     "Nelze nal\u00e9zt funkci: {0}"},

  { ER_UNSUPPORTED_ENCODING,
     "Nepodporovan\u00e9 k\u00f3dov\u00e1n\u00ed: {0}"},

  { ER_PROBLEM_IN_DTM_NEXTSIBLING,
      "Ve funkci getNextSibling do\u0161lo v DTM k chyb\u011b... Prob\u00edh\u00e1 pokus o obnovu"},

  { ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL,
      "Chyba program\u00e1tora: Do funkce EmptyNodeList nelze zapisovat."},

  { ER_SETDOMFACTORY_NOT_SUPPORTED,
      "Funkce XPathContext nepodporuje funkci setDOMFactory!"},

  { ER_PREFIX_MUST_RESOLVE,
      "P\u0159edponu mus\u00ed b\u00fdt mo\u017eno p\u0159elo\u017eit do oboru n\u00e1zv\u016f: {0}"},

  { ER_PARSE_NOT_SUPPORTED,
      "Funkce XPathContext nepodporuje anal\u00fdzu (InputSource source)! {0} - nelze otev\u0159\u00edt"},

  { ER_SAX_API_NOT_HANDLED,
      "Znaky SAX API (char ch[]... nen\u00ed v DTM zpracov\u00e1v\u00e1na!"},

  { ER_IGNORABLE_WHITESPACE_NOT_HANDLED,
      "Funkce ignorableWhitespace(char ch[]... nen\u00ed v DTM zpracov\u00e1v\u00e1na!"},

  { ER_DTM_CANNOT_HANDLE_NODES,
      "Funkce DTMLiaison nem\u016f\u017ee zpracov\u00e1vat uzly typu {0}"},

  { ER_XERCES_CANNOT_HANDLE_NODES,
      "Funkce DOM2Helper nem\u016f\u017ee zpracov\u00e1vat uzly typu {0}"},

  { ER_XERCES_PARSE_ERROR_DETAILS,
      "Chyba funkce DOM2Helper.parse: SystemID - {0} \u0159\u00e1dek - {1}"},

  { ER_XERCES_PARSE_ERROR,
     "Chyba funkce DOM2Helper.parse"},

  { ER_INVALID_UTF16_SURROGATE,
      "Byla zji\u0161t\u011bna neplatn\u00e1 n\u00e1hrada UTF-16: {0} ?"},

  { ER_OIERROR,
     "Chyba vstupu/v\u00fdstupu"},

  { ER_CANNOT_CREATE_URL,
     "Nelze vytvo\u0159it url pro: {0}"},

  { ER_XPATH_READOBJECT,
     "Ve funkci XPath.readObject: {0}"},

  { ER_FUNCTION_TOKEN_NOT_FOUND,
      "nebyl nalezen token funkce."},

  { ER_CANNOT_DEAL_XPATH_TYPE,
       "Nelze pracovat s typem XPath: {0}"},

  { ER_NODESET_NOT_MUTABLE,
       "Tento prvek NodeSet nelze m\u011bnit"},

  { ER_NODESETDTM_NOT_MUTABLE,
       "Tento prvek NodeSetDTM nelze m\u011bnit"},

  { ER_VAR_NOT_RESOLVABLE,
        "Prom\u011bnnou nelze p\u0159elo\u017eit: {0}"},

  { ER_NULL_ERROR_HANDLER,
        "Obslu\u017en\u00fd program pro zpracov\u00e1n\u00ed chyb hodnoty null"},

  { ER_PROG_ASSERT_UNKNOWN_OPCODE,
       "Tvrzen\u00ed program\u00e1tora: nezn\u00e1m\u00fd k\u00f3d operace: {0}"},

  { ER_ZERO_OR_ONE,
       "0 nebo 1"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "Modul XRTreeFragSelectWrapper nepodporuje rtf()"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "Modul XRTreeFragSelectWrapper nepodporuje funkci asNodeIterator()"},

   { ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "Modul XRTreeFragSelectWrapper nepodporuje funkci detach()"},

   { ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "Modul XRTreeFragSelectWrapper nepodporuje funkci num()"},

   { ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "Modul XRTreeFragSelectWrapper nepodporuje funkci xstr()"},

   { ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "Modul XRTreeFragSelectWrapper nepodporuje funkci str()"},

  { ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,
       "Funkce XStringForChars nepodporuje funkci fsb()"},

  { ER_COULD_NOT_FIND_VAR,
      "Nelze nal\u00e9zt prom\u011bnnou s n\u00e1zvem {0}"},

  { ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,
      "Argumentem funkce XStringForChars nem\u016f\u017ee b\u00fdt \u0159et\u011bzec"},

  { ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,
      "Argument funkce FastStringBuffer nem\u016f\u017ee m\u00edt hodnotu null"},

  { ER_TWO_OR_THREE,
       "2 nebo 3"},

  { ER_VARIABLE_ACCESSED_BEFORE_BIND,
       "P\u0159\u00edstup k prom\u011bnn\u00e9 p\u0159edt\u00edm, ne\u017e je z\u00e1vazn\u00e1!"},

  { ER_FSB_CANNOT_TAKE_STRING,
       "Argumentem funkce XStringForFSB nem\u016f\u017ee b\u00fdt \u0159et\u011bzec!"},

  { ER_SETTING_WALKER_ROOT_TO_NULL,
       "\n !!!! Chyba! Nastaven\u00ed ko\u0159ene objektu walker na hodnotu null!!!"},

  { ER_NODESETDTM_CANNOT_ITERATE,
       "Tato funkce NodeSetDTM nem\u016f\u017ee b\u00fdt stejn\u00e1 jako p\u0159edch\u00e1zej\u00edc\u00ed uzel!"},

  { ER_NODESET_CANNOT_ITERATE,
       "Tato funkce NodeSet nem\u016f\u017ee b\u00fdt stejn\u00e1 jako p\u0159edch\u00e1zej\u00edc\u00ed uzel!"},

  { ER_NODESETDTM_CANNOT_INDEX,
       "Tato funkce NodeSetDTM nem\u016f\u017ee prov\u00e1d\u011bt indexovac\u00ed nebo po\u010detn\u00ed funkce!"},

  { ER_NODESET_CANNOT_INDEX,
       "Tato funkce NodeSet nem\u016f\u017ee prov\u00e1d\u011bt indexovac\u00ed nebo po\u010detn\u00ed funkce!"},

  { ER_CANNOT_CALL_SETSHOULDCACHENODE,
       "Nelze volat funkci setShouldCacheNodes pot\u00e9, co byla vol\u00e1na funkce nextNode!"},

  { ER_ONLY_ALLOWS,
       "{0} povoluje pouze {1} argument\u016f"},

  { ER_UNKNOWN_STEP,
       "Tvrzen\u00ed program\u00e1tora ve funkci getNextStepPos: nezn\u00e1m\u00fd typ stepType: {0}"},

  //Note to translators:  A relative location path is a form of XPath expression.
  // The message indicates that such an expression was expected following the
  // characters '/' or '//', but was not found.
  { ER_EXPECTED_REL_LOC_PATH,
      "Po tokenu '/' nebo '//' byla o\u010dek\u00e1v\u00e1na cesta relativn\u00edho um\u00edst\u011bn\u00ed."},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such an expression was expected,but
  // the characters specified by the substitution text were encountered instead.
  { ER_EXPECTED_LOC_PATH,
       "O\u010dek\u00e1vala se cesta um\u00edst\u011bn\u00ed, av\u0161ak byl zaznamen\u00e1n n\u00e1sleduj\u00edc\u00ed token\u003a  {0}"},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such a subexpression was expected,
  // but no more characters were found in the expression.
  { ER_EXPECTED_LOC_PATH_AT_END_EXPR,
       "Byla o\u010dek\u00e1v\u00e1na cesta um\u00edst\u011bn\u00ed, m\u00edsto toho v\u0161ak byl nalezen konec v\u00fdrazu XPath. "},

  // Note to translators:  A location step is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected
  // following the specified characters.
  { ER_EXPECTED_LOC_STEP,
       "Po tokenu '/' nebo '//' byl o\u010dek\u00e1v\u00e1n krok um\u00edst\u011bn\u00ed"},

  // Note to translators:  A node test is part of an XPath expression that is
  // used to test for particular kinds of nodes.  In this case, a node test that
  // consists of an NCName followed by a colon and an asterisk or that consists
  // of a QName was expected, but was not found.
  { ER_EXPECTED_NODE_TEST,
       "Byl o\u010dek\u00e1v\u00e1n test uzlu, kter\u00fd odpov\u00edd\u00e1 bu\u010f prvk\u016fm NCName:* nebo QName."},

  // Note to translators:  A step pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but the specified character was found in the expression instead.
  { ER_EXPECTED_STEP_PATTERN,
       "Byl o\u010dek\u00e1v\u00e1n vzorek kroku, av\u0161ak byl zaznamen\u00e1n znak '/'."},

  // Note to translators: A relative path pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but was not found.
  { ER_EXPECTED_REL_PATH_PATTERN,
       "Byl o\u010dek\u00e1v\u00e1n vzorek relativn\u00ed cesty."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type boolean.
  { ER_CANT_CONVERT_TO_BOOLEAN,
       "Hodnota XPathResult v\u00fdrazu XPath ''{0}'' je typu XPathResultType {1}, kter\u00fd nelze p\u0159ev\u00e9st na booleovsk\u00fd typ. "},

  // Note to translators: Do not translate ANY_UNORDERED_NODE_TYPE and
  // FIRST_ORDERED_NODE_TYPE.
  { ER_CANT_CONVERT_TO_SINGLENODE,
       "Hodnota XPathResult v\u00fdrazu XPath ''{0}'' je typu XPathResultType {1}, kter\u00fd nelze p\u0159ev\u00e9st na jedin\u00fd uzel. Metoda getSingleNodeValue je pou\u017eiteln\u00e1 pouze pro typy ANY_UNORDERED_NODE_TYPE a FIRST_ORDERED_NODE_TYPE. "},

  // Note to translators: Do not translate UNORDERED_NODE_SNAPSHOT_TYPE and
  // ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_CANT_GET_SNAPSHOT_LENGTH,
       "Metodu getSnapshotLength nelze volat v datech XPathResult ani ve v\u00fdrazu XPath ''{0}'', proto\u017ee jej\u00ed typ XPathResultType je {1}. Tato metoda se pou\u017e\u00edv\u00e1 pouze pro typy UNORDERED_NODE_SNAPSHOT_TYPE a ORDERED_NODE_SNAPSHOT_TYPE. "},

  { ER_NON_ITERATOR_TYPE,
       "Metodu iterateNext nelze volat v datech XPathResult ani ve v\u00fdrazu XPath ''{0}'', proto\u017ee jej\u00ed typ XPathResultType je {1}. Tato metoda se pou\u017e\u00edv\u00e1 pouze pro typy UNORDERED_NODE_ITERATOR_TYPE a ORDERED_NODE_ITERATOR_TYPE. "},

  // Note to translators: This message indicates that the document being operated
  // upon changed, so the iterator object that was being used to traverse the
  // document has now become invalid.
  { ER_DOC_MUTATED,
       "Dokument se od doby, kdy byly vr\u00e1ceny v\u00fdsledky, zm\u011bnil. Iter\u00e1tor je neplatn\u00fd."},

  { ER_INVALID_XPATH_TYPE,
       "Neplatn\u00fd argument typu XPath: {0}"},

  { ER_EMPTY_XPATH_RESULT,
       "Pr\u00e1zdn\u00fd objekt v\u00fdsledku XPath"},

  { ER_INCOMPATIBLE_TYPES,
       "Hodnota XPathResult v\u00fdrazu XPath ''{0}'' je typu XPathResultType {1}, kter\u00fd nelze vynucen\u011b p\u0159ev\u00e9st na zadan\u00fd typ XPathResultType {2}. "},

  { ER_NULL_RESOLVER,
       "Nelze \u0159e\u0161it p\u0159edponu \u0159e\u0161itelem (resolver) s p\u0159edponou hodnoty null."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.
  { ER_CANT_CONVERT_TO_STRING,
       "Hodnota XPathResult v\u00fdrazu XPath ''{0}'' je typu XPathResultType {1}, kter\u00fd nelze p\u0159ev\u00e9st na \u0159et\u011bzec. "},

  // Note to translators: Do not translate snapshotItem,
  // UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_NON_SNAPSHOT_TYPE,
       "Metodu snapshotItem nelze volat v datech XPathResult ani ve v\u00fdrazu of XPath ''{0}'', proto\u017ee jej\u00ed typ XPathResultType je {1}. Tato metoda se pou\u017e\u00edv\u00e1 pouze pro typy UNORDERED_NODE_SNAPSHOT_TYPE a ORDERED_NODE_SNAPSHOT_TYPE. "},

  // Note to translators:  XPathEvaluator is a Java interface name.  An
  // XPathEvaluator is created with respect to a particular XML document, and in
  // this case the expression represented by this object was being evaluated with
  // respect to a context node from a different document.
  { ER_WRONG_DOCUMENT,
       "Uzel kontextu nepat\u0159\u00ed mezi dokumenty, kter\u00e9 jsou v\u00e1z\u00e1ny k XPathEvaluator."},

  // Note to translators:  The XPath expression cannot be evaluated with respect
  // to this type of node.
  { ER_WRONG_NODETYPE,
       "Typ uzlu kontextu nen\u00ed podporov\u00e1n."},

  { ER_XPATH_ERROR,
       "Nezn\u00e1m\u00e1 chyba objektu XPath."},

        { ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER,
                "Hodnota XPathResult v\u00fdrazu XPath ''{0}'' je typu XPathResultType {1}, kter\u00fd nelze p\u0159ev\u00e9st na \u010d\u00edslo. "},

  //BEGIN:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  /** Field ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED                       */

  { ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
       "Roz\u0161i\u0159uj\u00edc\u00ed funkci ''{0}'' nelze vyvolat, pokud je funkce XMLConstants.FEATURE_SECURE_PROCESSING nastavena na hodnotu True. "},

  /** Field ER_RESOLVE_VARIABLE_RETURNS_NULL                       */

  { ER_RESOLVE_VARIABLE_RETURNS_NULL,
       "Funkce resolveVariable pro prom\u011bnnou {0} vrac\u00ed hodnotu Null"},

  /** Field ER_UNSUPPORTED_RETURN_TYPE                       */

  { ER_UNSUPPORTED_RETURN_TYPE,
       "Nepodporovan\u00fd n\u00e1vratov\u00fd typ: {0}"},

  /** Field ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL                       */

  { ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL,
       "Zdrojov\u00fd a n\u00e1vratov\u00fd typ nesm\u00ed m\u00edt hodnotu Null"},

  /** Field ER_ARG_CANNOT_BE_NULL                       */

  { ER_ARG_CANNOT_BE_NULL,
       "Argument {0} nesm\u00ed m\u00edt hodnotu Null"},

  /** Field ER_OBJECT_MODEL_NULL                       */

  { ER_OBJECT_MODEL_NULL,
       "Funkci {0}#isObjectModelSupported( String objectModel ) nelze volat s hodnotou objectModel == Null"},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_OBJECT_MODEL_EMPTY,
       "Funkci {0}#isObjectModelSupported( String objectModel ) nelze volat s hodnotou objectModel == \"\""},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_FEATURE_NAME_NULL,
       "Pokus o nastaven\u00ed funkce s n\u00e1zvem s hodnotou Null: {0}#setFeature( null, {1})"},

  /** Field ER_FEATURE_UNKNOWN                       */

  { ER_FEATURE_UNKNOWN,
       "Pokus o nastaven\u00ed nezn\u00e1m\u00e9 funkce \"{0}\":{1}#setFeature({0},{2})"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_NULL_FEATURE,
       "Pokus o na\u010dten\u00ed funkce s n\u00e1zvem s hodnotou Null: {0}#getFeature(null)"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_UNKNOWN_FEATURE,
       "Pokus o z\u00edsk\u00e1n\u00ed nezn\u00e1m\u00e9 funkce \"{0}\":{1}#getFeature({0})"},

  /** Field ER_NULL_XPATH_FUNCTION_RESOLVER                       */

  { ER_NULL_XPATH_FUNCTION_RESOLVER,
       "Pokus o nastaven\u00ed parametru XPathFunctionResolver s hodnotou Null:{0}#setXPathFunctionResolver(null)"},

  /** Field ER_NULL_XPATH_VARIABLE_RESOLVER                       */

  { ER_NULL_XPATH_VARIABLE_RESOLVER,
       "Pokus o nastaven\u00ed parametru XPathVariableResolver s hodnotou Null:{0}#setXPathVariableResolver(null)"},

  //END:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  // Warnings...

  { WG_LOCALE_NAME_NOT_HANDLED,
      "funkce format-number prozat\u00edm nezpracovala n\u00e1zev n\u00e1rodn\u00edho prost\u0159ed\u00ed (locale)!"},

  { WG_PROPERTY_NOT_SUPPORTED,
      "Vlastnost XSL nen\u00ed podporov\u00e1na: {0}"},

  { WG_DONT_DO_ANYTHING_WITH_NS,
      "Aktu\u00e1ln\u011b ned\u011blejte nic s oborem n\u00e1zv\u016f {0} vlastnosti: {1}"},

  { WG_SECURITY_EXCEPTION,
      "P\u0159i pokusu o p\u0159\u00edstup k syst\u00e9mov\u00e9 vlastnosti XSL do\u0161lo k v\u00fdjimce SecurityException: {0}"},

  { WG_QUO_NO_LONGER_DEFINED,
      "Zastaral\u00e1 syntaxe: quo(...) ji\u017e nen\u00ed v XPath definov\u00e1no."},

  { WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST,
      "XPath pot\u0159ebuje k implementaci funkce nodeTest odvozen\u00fd objekt!"},

  { WG_FUNCTION_TOKEN_NOT_FOUND,
      "nebyl nalezen token funkce."},

  { WG_COULDNOT_FIND_FUNCTION,
      "Nelze nal\u00e9zt funkci: {0}"},

  { WG_CANNOT_MAKE_URL_FROM,
      "Nelze vytvo\u0159it adresu URL z: {0}"},

  { WG_EXPAND_ENTITIES_NOT_SUPPORTED,
      "Analyz\u00e1tor DTM nepodporuje volbu -E"},

  { WG_ILLEGAL_VARIABLE_REFERENCE,
      "Odkaz VariableReference uveden k prom\u011bnn\u00e9 mimo kontext nebo bez definice!  N\u00e1zev = {0}"},

  { WG_UNSUPPORTED_ENCODING,
     "Nepodporovan\u00e9 k\u00f3dov\u00e1n\u00ed: {0}"},



  // Other miscellaneous text used inside the code...
  { "ui_language", "cs"},
  { "help_language", "cs"},
  { "language", "cs"},
  { "BAD_CODE", "Parametr funkce createMessage je mimo limit"},
  { "FORMAT_FAILED", "P\u0159i vol\u00e1n\u00ed funkce messageFormat do\u0161lo k v\u00fdjimce"},
  { "version", ">>>>>>> Verze Xalan "},
  { "version2", "<<<<<<<"},
  { "yes", "ano"},
  { "line", "\u0158\u00e1dek #"},
  { "column", "Sloupec #"},
  { "xsldone", "XSLProcessor: hotovo"},
  { "xpath_option", "volby xpath: "},
  { "optionIN", "   [-in inputXMLURL]"},
  { "optionSelect", "   [-select v\u00fdraz xpath]"},
  { "optionMatch", "   [-match vzorek shody (pro diagnostiku shody)]"},
  { "optionAnyExpr", "Jinak v\u00fdpis diagnostiky provede pouze v\u00fdraz xpath"},
  { "noParsermsg1", "Proces XSL nebyl \u00fasp\u011b\u0161n\u00fd."},
  { "noParsermsg2", "** Nelze naj\u00edt analyz\u00e1tor **"},
  { "noParsermsg3", "Zkontrolujte cestu classpath."},
  { "noParsermsg4", "Nem\u00e1te-li analyz\u00e1tor XML jazyka Java spole\u010dnosti IBM, m\u016f\u017eete si jej st\u00e1hnout z adresy:"},
  { "noParsermsg5", "AlphaWorks: http://www.alphaworks.ibm.com/formula/xml"},
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
  public static final String ERROR_STRING = "#chyba";

  /** Field ERROR_HEADER          */
  public static final String ERROR_HEADER = "Chyba: ";

  /** Field WARNING_HEADER          */
  public static final String WARNING_HEADER = "Varov\u00e1n\u00ed: ";

  /** Field XSL_HEADER          */
  public static final String XSL_HEADER = "XSL ";

  /** Field XML_HEADER          */
  public static final String XML_HEADER = "XML ";

  /** Field QUERY_HEADER          */
  public static final String QUERY_HEADER = "PATTERN ";


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
                new Locale("cs", "CZ"));
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
