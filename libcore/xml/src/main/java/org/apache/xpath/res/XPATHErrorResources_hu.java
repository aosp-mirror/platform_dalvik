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
 * $Id: XPATHErrorResources_hu.java 468655 2006-10-28 07:12:06Z minchau $
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
public class XPATHErrorResources_hu extends ListResourceBundle
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

  { ER_CURRENT_NOT_ALLOWED_IN_MATCH, "A current() f\u00fcggv\u00e9ny nem megengedett az illeszt\u00e9si mint\u00e1ban!" },

  { ER_CURRENT_TAKES_NO_ARGS, "A current() f\u00fcggv\u00e9ny nem fogad el argumentumokat!" },

  { ER_DOCUMENT_REPLACED,
      "A document() f\u00fcggv\u00e9ny megval\u00f3s\u00edt\u00e1s\u00e1t lecser\u00e9lte az org.apache.xalan.xslt.FuncDocument!"},

  { ER_CONTEXT_HAS_NO_OWNERDOC,
      "A k\u00f6rnyezetnek nincs tulajdonos dokumentuma!"},

  { ER_LOCALNAME_HAS_TOO_MANY_ARGS,
      "A local-name()-nek t\u00fal sok argumentuma van."},

  { ER_NAMESPACEURI_HAS_TOO_MANY_ARGS,
      "A namespace-uri()-nek t\u00fal sok argumentuma van."},

  { ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS,
      "A normalize-space()-nek t\u00fal sok argumentuma van."},

  { ER_NUMBER_HAS_TOO_MANY_ARGS,
      "A number()-nek t\u00fal sok argumentuma van."},

  { ER_NAME_HAS_TOO_MANY_ARGS,
     "A name()-nek t\u00fal sok argumentuma van."},

  { ER_STRING_HAS_TOO_MANY_ARGS,
      "A string()-nek t\u00fal sok argumentuma van."},

  { ER_STRINGLENGTH_HAS_TOO_MANY_ARGS,
      "A string-length()-nek t\u00fal sok argumentuma van."},

  { ER_TRANSLATE_TAKES_3_ARGS,
      "A translate() f\u00fcggv\u00e9ny h\u00e1rom argumentumot k\u00e9r!"},

  { ER_UNPARSEDENTITYURI_TAKES_1_ARG,
      "Az unparsed-entity-uri f\u00fcggv\u00e9nyhez egy argumentum sz\u00fcks\u00e9ges!"},

  { ER_NAMESPACEAXIS_NOT_IMPLEMENTED,
      "A n\u00e9vt\u00e9r tengely m\u00e9g nincs magval\u00f3s\u00edtva!"},

  { ER_UNKNOWN_AXIS,
     "Ismeretlen tengely: {0}"},

  { ER_UNKNOWN_MATCH_OPERATION,
     "ismeretlen illeszt\u00e9si m\u0171velet!"},

  { ER_INCORRECT_ARG_LENGTH,
      "A processing-instruction() csom\u00f3pont teszt argumentum\u00e1nak hossza helytelen!"},

  { ER_CANT_CONVERT_TO_NUMBER,
      "A(z) {0} nem konvert\u00e1lhat\u00f3 sz\u00e1mm\u00e1"},

  { ER_CANT_CONVERT_TO_NODELIST,
      "A(z) {0} nem konvert\u00e1lhat\u00f3 NodeList-t\u00e9!"},

  { ER_CANT_CONVERT_TO_MUTABLENODELIST,
      "A(z) {0} nem konvert\u00e1lhat\u00f3 NodeSetDTM-m\u00e9!"},

  { ER_CANT_CONVERT_TO_TYPE,
      "{0} nem konvert\u00e1lhat\u00f3 type#{1} t\u00edpuss\u00e1"},

  { ER_EXPECTED_MATCH_PATTERN,
      "Illeszt\u00e9si mint\u00e1t v\u00e1rtunk a getMatchScore-ban!"},

  { ER_COULDNOT_GET_VAR_NAMED,
      "Nem lehet lek\u00e9rni a(z) {0} nev\u0171 v\u00e1ltoz\u00f3t"},

  { ER_UNKNOWN_OPCODE,
     "HIBA! Ismeretlen opk\u00f3d: {0}"},

  { ER_EXTRA_ILLEGAL_TOKENS,
     "Extra tiltott tokenek: {0}"},


  { ER_EXPECTED_DOUBLE_QUOTE,
      "rosszul id\u00e9zett liter\u00e1l... dupla id\u00e9z\u0151jelet v\u00e1rtunk!"},

  { ER_EXPECTED_SINGLE_QUOTE,
      "rosszul id\u00e9zett liter\u00e1l... szimpla id\u00e9z\u0151jelet v\u00e1rtunk!"},

  { ER_EMPTY_EXPRESSION,
     "\u00dcres kifejez\u00e9s!"},

  { ER_EXPECTED_BUT_FOUND,
     "{0}-t v\u00e1rtunk, de ezt tal\u00e1ltuk: {1}"},

  { ER_INCORRECT_PROGRAMMER_ASSERTION,
      "A programoz\u00f3 feltev\u00e9se hib\u00e1s! - {0}"},

  { ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,
      "A boolean(...) argumentuma t\u00f6bb\u00e9 nem opcion\u00e1lis az 19990709 XPath v\u00e1zlat szerint."},

  { ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,
      "','-t tal\u00e1ltunk, de nincs el\u0151tte argumentum!"},

  { ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
      "','-t tal\u00e1ltunk, de nincs ut\u00e1na argumentum!"},

  { ER_PREDICATE_ILLEGAL_SYNTAX,
      "A '..[felt\u00e9tel]' vagy '.[felt\u00e9tel]' szintaktika tiltott.  Haszn\u00e1lja ink\u00e1bb a 'self::node()[predicate]' defin\u00edci\u00f3t."},

  { ER_ILLEGAL_AXIS_NAME,
     "Tiltott tengelyn\u00e9v: {0}"},

  { ER_UNKNOWN_NODETYPE,
     "Ismeretlen node-t\u00edpus: {0}"},

  { ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
      "A minta-liter\u00e1lt ({0}) id\u00e9z\u0151jelek k\u00f6z\u00e9 kell tenni!"},

  { ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
      "A(z) {0} nem form\u00e1zhat\u00f3 sz\u00e1mm\u00e1!"},

  { ER_COULDNOT_CREATE_XMLPROCESSORLIAISON,
      "Nem lehet XML TransformerFactory Liaison-t l\u00e9trehozni: {0}"},

  { ER_DIDNOT_FIND_XPATH_SELECT_EXP,
      "Hiba! Az xpath kiv\u00e1laszt\u00e1si kifejez\u00e9s nem tal\u00e1lhat\u00f3 (-select)."},

  { ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH,
      "HIBA! Nem tal\u00e1lhat\u00f3 ENDOP az OP_LOCATIONPATH ut\u00e1n"},

  { ER_ERROR_OCCURED,
     "Hiba t\u00f6rt\u00e9nt!"},

  { ER_ILLEGAL_VARIABLE_REFERENCE,
      "A v\u00e1ltoz\u00f3ra adott VariableReference (v\u00e1ltoz\u00f3hivatkoz\u00e1s) k\u00edv\u00fcl van a k\u00f6rnyezeten vagy nincs defin\u00edci\u00f3ja!  N\u00e9v = {0}"},

  { ER_AXES_NOT_ALLOWED,
      "Csak a child:: \u00e9s az attribute:: tengelyek illeszkedhetnek mint\u00e1kra.  Zavar\u00f3 tengelyek = {0}"},

  { ER_KEY_HAS_TOO_MANY_ARGS,
      "A key()-nek nem megfelel\u0151 sz\u00e1m\u00fa argumentuma van."},

  { ER_COUNT_TAKES_1_ARG,
      "A count f\u00fcggv\u00e9nyhez csak egy argumentumot lehet megadni!"},

  { ER_COULDNOT_FIND_FUNCTION,
     "Nem tal\u00e1lhat\u00f3 a f\u00fcggv\u00e9ny: {0}"},

  { ER_UNSUPPORTED_ENCODING,
     "Nem t\u00e1mogatott k\u00f3dol\u00e1s: {0}"},

  { ER_PROBLEM_IN_DTM_NEXTSIBLING,
      "Probl\u00e9ma mer\u00fclt fel a DTM-ben a getNextSibling-ben... megpr\u00f3b\u00e1ljuk helyrehozni"},

  { ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL,
      "Programoz\u00f3i hiba: az EmptyNodeList-be (\u00fcres csom\u00f3pontlist\u00e1ba) nem lehet \u00edrni."},

  { ER_SETDOMFACTORY_NOT_SUPPORTED,
      "A setDOMFactory-t nem t\u00e1mogatja az XPathContext!"},

  { ER_PREFIX_MUST_RESOLVE,
      "Az el\u0151tagnak egy n\u00e9vt\u00e9rre kell felold\u00f3dnia: {0}"},

  { ER_PARSE_NOT_SUPPORTED,
      "Az elem\u00e9s (InputSource forr\u00e1s) nem t\u00e1mogatott az XPathContext-ben! Nem lehet megnyitni a(z) {0}-t"},

  { ER_SAX_API_NOT_HANDLED,
      "SAX API characters(char ch[]... f\u00fcggv\u00e9nyt nem kezeli a DTM!"},

  { ER_IGNORABLE_WHITESPACE_NOT_HANDLED,
      "Az ignorableWhitespace(char ch[]... f\u00fcggv\u00e9nyt nem kezeli a DTM!"},

  { ER_DTM_CANNOT_HANDLE_NODES,
      "A DTMLiaison nem tud {0} t\u00edpus\u00fa csom\u00f3pontokat kezelni"},

  { ER_XERCES_CANNOT_HANDLE_NODES,
      "A DOM2Helper nem tud {0} t\u00edpus\u00fa csom\u00f3pontokat kezelni"},

  { ER_XERCES_PARSE_ERROR_DETAILS,
      "DOM2Helper.parse hiba: SystemID - {0} sor - {1}"},

  { ER_XERCES_PARSE_ERROR,
     "DOM2Helper.parse hiba"},

  { ER_INVALID_UTF16_SURROGATE,
      "\u00c9rv\u00e9nytelen UTF-16 helyettes\u00edt\u00e9s: {0} ?"},

  { ER_OIERROR,
     "IO hiba"},

  { ER_CANNOT_CREATE_URL,
     "Nem lehet URL-t l\u00e9trehozni ehhez: {0}"},

  { ER_XPATH_READOBJECT,
     "A XPath.readObject met\u00f3dusban: {0}"},

  { ER_FUNCTION_TOKEN_NOT_FOUND,
      "A f\u00fcggv\u00e9ny jelsor nem tal\u00e1lhat\u00f3."},

  { ER_CANNOT_DEAL_XPATH_TYPE,
       "Nem lehet megbirk\u00f3zni az XPath t\u00edpussal: {0}"},

  { ER_NODESET_NOT_MUTABLE,
       "Ez a NodeSet nem illeszthet\u0151 be"},

  { ER_NODESETDTM_NOT_MUTABLE,
       "Ez a NodeSetDTM nem illeszthet\u0151 be"},

  { ER_VAR_NOT_RESOLVABLE,
        "A v\u00e1ltoz\u00f3 nem oldhat\u00f3 fel: {0}"},

  { ER_NULL_ERROR_HANDLER,
        "Null hibakezel\u0151"},

  { ER_PROG_ASSERT_UNKNOWN_OPCODE,
       "Programoz\u00f3i \u00e9rtes\u00edt\u00e9s: ismeretlen m\u0171veletk\u00f3d: {0} "},

  { ER_ZERO_OR_ONE,
       "0 vagy 1"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "Az rtf()-et nem t\u00e1mogatja az XRTreeFragSelectWrapper"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "Az asNodeIterator()-t nem t\u00e1mogatja az XRTreeFragSelectWrapper"},

   { ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "A detach() f\u00fcggv\u00e9nyt nem t\u00e1mogatja az XRTreeFragSelectWrapper"},

   { ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "A num() f\u00fcggv\u00e9nyt nem t\u00e1mogatja az XRTreeFragSelectWrapper"},

   { ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "A xstr() f\u00fcggv\u00e9nyt nem t\u00e1mogatja az XRTreeFragSelectWrapper"},

   { ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "A str() f\u00fcggv\u00e9nyt nem t\u00e1mogatja az XRTreeFragSelectWrapper"},

  { ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,
       "Az fsb() nem t\u00e1mogatott az XStringForChars-n\u00e1l"},

  { ER_COULD_NOT_FIND_VAR,
      "Nem tal\u00e1lhat\u00f3 {0} nev\u0171 v\u00e1ltoz\u00f3"},

  { ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,
      "Az XStringForChars-nak nem adhat meg karakterl\u00e1nc argumentumot"},

  { ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,
      "A FastStringBuffer argumentum nem lehet null"},

  { ER_TWO_OR_THREE,
       "2 vagy 3"},

  { ER_VARIABLE_ACCESSED_BEFORE_BIND,
       "V\u00e1ltoz\u00f3el\u00e9r\u00e9s \u00e9rt\u00e9kad\u00e1s el\u0151tt!"},

  { ER_FSB_CANNOT_TAKE_STRING,
       "XStringForFSB nem kaphat sztring argumentumot!"},

  { ER_SETTING_WALKER_ROOT_TO_NULL,
       "\n !!!! Hiba! A bej\u00e1r\u00f3 gy\u00f6ker\u00e9t null-ra \u00e1ll\u00edtotta!!!"},

  { ER_NODESETDTM_CANNOT_ITERATE,
       "Ez a NodeSetDTM nem iter\u00e1lhat egy kor\u00e1bbi node-ra!"},

  { ER_NODESET_CANNOT_ITERATE,
       "Ez a NodeSet nem iter\u00e1lhat egy kor\u00e1bbi node-ra!"},

  { ER_NODESETDTM_CANNOT_INDEX,
       "Ez a NodeSetDTM nem indexelhet \u00e9s nem sz\u00e1ml\u00e1lhatja a funkci\u00f3kat!"},

  { ER_NODESET_CANNOT_INDEX,
       "Ez a NodeSet nem indexelhet \u00e9s nem sz\u00e1ml\u00e1lhatja a funkci\u00f3kat!"},

  { ER_CANNOT_CALL_SETSHOULDCACHENODE,
       "Nem h\u00edvhat\u00f3 setShouldCacheNodes nextNode h\u00edv\u00e1sa ut\u00e1n!"},

  { ER_ONLY_ALLOWS,
       "{0} csak {1} argumentumot enged\u00e9lyez"},

  { ER_UNKNOWN_STEP,
       "Programoz\u00f3i \u00e9rtes\u00edt\u00e9s getNextStepPos h\u00edv\u00e1sban: ismeretlen stepType: {0} "},

  //Note to translators:  A relative location path is a form of XPath expression.
  // The message indicates that such an expression was expected following the
  // characters '/' or '//', but was not found.
  { ER_EXPECTED_REL_LOC_PATH,
      "Egy relat\u00edv elhelyez\u00e9si \u00fatvonalat v\u00e1rtunk a '/' vagy '//' token ut\u00e1n."},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such an expression was expected,but
  // the characters specified by the substitution text were encountered instead.
  { ER_EXPECTED_LOC_PATH,
       "Egy hely \u00fatvonalat v\u00e1rtam, de a k\u00f6vetkez\u0151 tokent tal\u00e1ltam\u003a  {0}"},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such a subexpression was expected,
  // but no more characters were found in the expression.
  { ER_EXPECTED_LOC_PATH_AT_END_EXPR,
       "A rendszer hely \u00fatvonalat v\u00e1rt, de helyette az XPath kifejez\u00e9s v\u00e9g\u00e9be \u00fctk\u00f6z\u00f6tt."},

  // Note to translators:  A location step is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected
  // following the specified characters.
  { ER_EXPECTED_LOC_STEP,
       "Egy elhelyez\u00e9si l\u00e9p\u00e9st v\u00e1rtunk a '/' vagy '//' token ut\u00e1n."},

  // Note to translators:  A node test is part of an XPath expression that is
  // used to test for particular kinds of nodes.  In this case, a node test that
  // consists of an NCName followed by a colon and an asterisk or that consists
  // of a QName was expected, but was not found.
  { ER_EXPECTED_NODE_TEST,
       "Egy olyan node-tesztet v\u00e1rtunk, ami vagy az NCName:*-ra vagy a QName-re illeszkedik."},

  // Note to translators:  A step pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but the specified character was found in the expression instead.
  { ER_EXPECTED_STEP_PATTERN,
       "Egy l\u00e9p\u00e9smint\u00e1t v\u00e1rtunk, de '/' szerepelt."},

  // Note to translators: A relative path pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but was not found.
  { ER_EXPECTED_REL_PATH_PATTERN,
       "Relat\u00edv \u00fatvonalat v\u00e1rtunk."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type boolean.
  { ER_CANT_CONVERT_TO_BOOLEAN,
       "A(z) ''{0}'' XPath kifejez\u00e9s XPathResult elem\u00e9nek XPathResultType \u00e9rt\u00e9ke {1}, ami nem alak\u00edthat\u00f3 \u00e1t logikai t\u00edpuss\u00e1."},

  // Note to translators: Do not translate ANY_UNORDERED_NODE_TYPE and
  // FIRST_ORDERED_NODE_TYPE.
  { ER_CANT_CONVERT_TO_SINGLENODE,
       "A(z) ''{0}'' XPath kifejez\u00e9s XPathResult elem\u00e9nek XPathResultType \u00e9rt\u00e9ke {1}, ami nem alak\u00edthat\u00f3 \u00e1t egyetlen csom\u00f3pontt\u00e1. A getSingleNodeValue met\u00f3dus csak az ANY_UNORDERED_NODE_TYPE \u00e9s a FIRST_ORDERED_NODE_TYPE t\u00edpusra alkalmazhat\u00f3. "},

  // Note to translators: Do not translate UNORDERED_NODE_SNAPSHOT_TYPE and
  // ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_CANT_GET_SNAPSHOT_LENGTH,
       "A getSnapshotLength met\u00f3dus nem h\u00edvhat\u00f3 meg a(z) ''{0}'' XPath kifejez\u00e9s XPathResult \u00e9rt\u00e9k\u00e9re, mert az XPathResultType \u00e9rt\u00e9ke {1}. Ez a met\u00f3dus csak UNORDERED_NODE_SNAPSHOT_TYPE \u00e9s ORDERED_NODE_SNAPSHOT_TYPE t\u00edpusokra alkalmazhat\u00f3."},

  { ER_NON_ITERATOR_TYPE,
       "Az iterateNext met\u00f3dus nem h\u00edvhat\u00f3 meg a(z) ''{0}'' XPath kifejez\u00e9s XPathResult \u00e9rt\u00e9k\u00e9re, mert az XPathResultType \u00e9rt\u00e9ke {1}. Ez a met\u00f3dus csak UNORDERED_NODE_ITERATOR_TYPE \u00e9s ORDERED_NODE_ITERATOR_TYPE t\u00edpusokra alkalmazhat\u00f3."},

  // Note to translators: This message indicates that the document being operated
  // upon changed, so the iterator object that was being used to traverse the
  // document has now become invalid.
  { ER_DOC_MUTATED,
       "A dokumentum megv\u00e1ltozott, mi\u00f3ta az eredm\u00e9ny visszat\u00e9rt. Az iter\u00e1tor \u00e9rv\u00e9nytelen."},

  { ER_INVALID_XPATH_TYPE,
       "\u00c9rv\u00e9nytelen XPath t\u00edpus-argumentum: {0}"},

  { ER_EMPTY_XPATH_RESULT,
       "\u00dcres XPath eredm\u00e9nyobjektum"},

  { ER_INCOMPATIBLE_TYPES,
       "A(z) ''{0}'' XPath kifejez\u00e9s XPathResult elem\u00e9nek XPathResultType \u00e9rt\u00e9ke {1}, ami nem helyezhet\u0151 el a megadott {2} XPathResultType t\u00edpusban. "},

  { ER_NULL_RESOLVER,
       "Nem lehet feloldani a prefixet null prefix-felold\u00f3val."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.
  { ER_CANT_CONVERT_TO_STRING,
       "A(z) ''{0}'' XPath kifejez\u00e9s XPathResult elem\u00e9nek XPathResultType \u00e9rt\u00e9ke {1}, ami nem alak\u00edthat\u00f3 \u00e1t karaktersorozatt\u00e1. "},

  // Note to translators: Do not translate snapshotItem,
  // UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_NON_SNAPSHOT_TYPE,
       "A snapshotItem met\u00f3dus nem h\u00edvhat\u00f3 meg a(z) ''{0}'' XPath kifejez\u00e9s XPathResult \u00e9rt\u00e9k\u00e9re, mert az XPathResultType \u00e9rt\u00e9ke {1}. Ez a met\u00f3dus csak UNORDERED_NODE_SNAPSHOT_TYPE \u00e9s ORDERED_NODE_SNAPSHOT_TYPE t\u00edpusokra alkalmazhat\u00f3."},

  // Note to translators:  XPathEvaluator is a Java interface name.  An
  // XPathEvaluator is created with respect to a particular XML document, and in
  // this case the expression represented by this object was being evaluated with
  // respect to a context node from a different document.
  { ER_WRONG_DOCUMENT,
       "A k\u00f6rnyezeti node nem az XPathEvaluator-hoz tartoz\u00f3 dokumentumhoz tartozik."},

  // Note to translators:  The XPath expression cannot be evaluated with respect
  // to this type of node.
  { ER_WRONG_NODETYPE,
       "A k\u00f6rnyezeti node t\u00edpusa nem t\u00e1mogatott."},

  { ER_XPATH_ERROR,
       "Ismeretlen hiba az XPath-ban."},

        { ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER,
                "A(z) ''{0}'' XPath kifejez\u00e9s XPathResult elem\u00e9nek XPathResultType \u00e9rt\u00e9ke {1}, ami nem alak\u00edthat\u00f3 \u00e1t sz\u00e1mm\u00e1. "},

  //BEGIN:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  /** Field ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED                       */

  { ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
       "A(z) ''{0}'' b\u0151v\u00edt\u0151 f\u00fcggv\u00e9ny nem h\u00edvhat\u00f3 meg, amikor az XMLConstants.FEATURE_SECURE_PROCESSING szolg\u00e1ltat\u00e1s igazra van \u00e1ll\u00edtva. "},

  /** Field ER_RESOLVE_VARIABLE_RETURNS_NULL                       */

  { ER_RESOLVE_VARIABLE_RETURNS_NULL,
       "A resolveVariable null\u00e9rt\u00e9ket adott vissza a(z) {0} v\u00e1ltoz\u00f3ra"},

  /** Field ER_UNSUPPORTED_RETURN_TYPE                       */

  { ER_UNSUPPORTED_RETURN_TYPE,
       "Nem t\u00e1mogatott visszat\u00e9r\u00e9si t\u00edpus: {0}"},

  /** Field ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL                       */

  { ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL,
       "A forr\u00e1s \u00e9s/vagy a visszat\u00e9r\u00e9si t\u00edpus nem lehet null. "},

  /** Field ER_ARG_CANNOT_BE_NULL                       */

  { ER_ARG_CANNOT_BE_NULL,
       "A(z) {0} argumentum nem lehet null"},

  /** Field ER_OBJECT_MODEL_NULL                       */

  { ER_OBJECT_MODEL_NULL,
       "{0}#isObjectModelSupported( String objectModel ) nem h\u00edvhat\u00f3 meg objectModel == null \u00e9rt\u00e9kkel "},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_OBJECT_MODEL_EMPTY,
       "{0}#isObjectModelSupported( String objectModel ) nem h\u00edvhat\u00f3 meg  objectModel == \"\" \u00e9rt\u00e9kkel "},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_FEATURE_NAME_NULL,
       "Pr\u00f3b\u00e1lkoz\u00e1s egy szolg\u00e1ltat\u00e1s be\u00e1ll\u00edt\u00e1s\u00e1ra null n\u00e9vvel: {0}#setFeature( null, {1})"},

  /** Field ER_FEATURE_UNKNOWN                       */

  { ER_FEATURE_UNKNOWN,
       "Pr\u00f3b\u00e1lkoz\u00e1s az ismeretlen \"{0}\" szolg\u00e1ltat\u00e1s be\u00e1ll\u00edt\u00e1s\u00e1ra:{1}#setFeature({0},{2})"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_NULL_FEATURE,
       "Null nev\u0171 szolg\u00e1ltat\u00e1st pr\u00f3b\u00e1lt meg beolvasni: {0}#getFeature(null)"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_UNKNOWN_FEATURE,
       "Pr\u00f3b\u00e1lkoz\u00e1s az ismeretlen \"{0}\" szolg\u00e1ltat\u00e1s beolvas\u00e1s\u00e1ra:{1}#getFeature({0})"},

  /** Field ER_NULL_XPATH_FUNCTION_RESOLVER                       */

  { ER_NULL_XPATH_FUNCTION_RESOLVER,
       "Null XPathFunctionResolver \u00e9rt\u00e9ket pr\u00f3b\u00e1lt meg be\u00e1ll\u00edtani:{0}#setXPathFunctionResolver(null)"},

  /** Field ER_NULL_XPATH_VARIABLE_RESOLVER                       */

  { ER_NULL_XPATH_VARIABLE_RESOLVER,
       "Null XPathVariableResolver \u00e9rt\u00e9ket pr\u00f3b\u00e1lt meg be\u00e1ll\u00edtani:{0}#setXPathVariableResolver(null)"},

  //END:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  // Warnings...

  { WG_LOCALE_NAME_NOT_HANDLED,
      "A locale-n\u00e9v a format-number f\u00fcggv\u00e9nyben m\u00e9g nincs kezelve!"},

  { WG_PROPERTY_NOT_SUPPORTED,
      "Az XSL tulajdons\u00e1g nem t\u00e1mogatott: {0}"},

  { WG_DONT_DO_ANYTHING_WITH_NS,
      "Jelenleg ne tegyen semmit a(z) {0} n\u00e9vt\u00e9rrel a tulajdons\u00e1gban: {1}"},

  { WG_SECURITY_EXCEPTION,
      "SecurityException az XSL rendszertulajdons\u00e1g el\u00e9r\u00e9s\u00e9n\u00e9l: {0}"},

  { WG_QUO_NO_LONGER_DEFINED,
      "A r\u00e9gi szintaktika: quo(...) t\u00f6bb\u00e9 nincs defini\u00e1lva az XPath-ban."},

  { WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST,
      "Az XPath-nak kell egy sz\u00e1rmaztatott objektum a nodeTest-hez!"},

  { WG_FUNCTION_TOKEN_NOT_FOUND,
      "A f\u00fcggv\u00e9ny jelsor nem tal\u00e1lhat\u00f3."},

  { WG_COULDNOT_FIND_FUNCTION,
      "Nem tal\u00e1lhat\u00f3 a f\u00fcggv\u00e9ny: {0}"},

  { WG_CANNOT_MAKE_URL_FROM,
      "Nem k\u00e9sz\u00edthet\u0151 URL ebb\u0151l: {0}"},

  { WG_EXPAND_ENTITIES_NOT_SUPPORTED,
      "A -E opci\u00f3 nem t\u00e1mogatott a DTM \u00e9rtelmez\u0151h\u00f6z"},

  { WG_ILLEGAL_VARIABLE_REFERENCE,
      "A v\u00e1ltoz\u00f3ra adott VariableReference (v\u00e1ltoz\u00f3hivatkoz\u00e1s) k\u00edv\u00fcl van a k\u00f6rnyezeten vagy nincs defin\u00edci\u00f3ja!  N\u00e9v = {0}"},

  { WG_UNSUPPORTED_ENCODING,
     "Nem t\u00e1mogatott k\u00f3dol\u00e1s: {0}"},



  // Other miscellaneous text used inside the code...
  { "ui_language", "hu"},
  { "help_language", "hu"},
  { "language", "hu"},
  { "BAD_CODE", "A createMessage param\u00e9tere nincs a megfelel\u0151 tartom\u00e1nyban"},
  { "FORMAT_FAILED", "Kiv\u00e9tel t\u00f6rt\u00e9nt a messageFormat h\u00edv\u00e1s alatt"},
  { "version", ">>>>>>> Xalan verzi\u00f3 "},
  { "version2", "<<<<<<<"},
  { "yes", "igen"},
  { "line", "Sor #"},
  { "column", "Oszlop #"},
  { "xsldone", "XSLProcessor: k\u00e9sz"},
  { "xpath_option", "xpath opci\u00f3i: "},
  { "optionIN", "   [-in bemenetiXMLURL]"},
  { "optionSelect", "   [-select xpath kifejez\u00e9s]"},
  { "optionMatch", "   [-match illeszt\u00e9si minta (az illeszt\u00e9si diagnosztik\u00e1hoz)]"},
  { "optionAnyExpr", "Vagy csak egy xpath kifejez\u00e9s megcsin\u00e1l egy diagnosztikai dump-ot"},
  { "noParsermsg1", "Az XSL folyamat sikertelen volt."},
  { "noParsermsg2", "** Az \u00e9rtelmez\u0151 nem tal\u00e1lhat\u00f3 **"},
  { "noParsermsg3", "K\u00e9rem, ellen\u0151rizze az oszt\u00e1ly el\u00e9r\u00e9si utat."},
  { "noParsermsg4", "Ha \u00f6nnek nincs meg az IBM Java XML \u00e9rtelmez\u0151je, akkor let\u00f6ltheti az"},
  { "noParsermsg5", "IBM AlphaWorks weblapr\u00f3l: http://www.alphaworks.ibm.com/formula/xml"},
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
  public static final String ERROR_STRING = "#error";

  /** Field ERROR_HEADER          */
  public static final String ERROR_HEADER = "Hiba: ";

  /** Field WARNING_HEADER          */
  public static final String WARNING_HEADER = "Figyelmeztet\u00e9s: ";

  /** Field XSL_HEADER          */
  public static final String XSL_HEADER = "XSL ";

  /** Field XML_HEADER          */
  public static final String XML_HEADER = "XML ";

  /** Field QUERY_HEADER          */
  public static final String QUERY_HEADER = "MINTA ";


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
                new Locale("hu", "HU"));
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
