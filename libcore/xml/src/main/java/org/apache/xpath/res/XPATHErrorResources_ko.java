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
 * $Id: XPATHErrorResources_ko.java 468655 2006-10-28 07:12:06Z minchau $
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
public class XPATHErrorResources_ko extends ListResourceBundle
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

  { ER_CURRENT_NOT_ALLOWED_IN_MATCH, "\uc77c\uce58 \ud328\ud134\uc5d0\uc11c current() \ud568\uc218\uac00 \ud5c8\uc6a9\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4." },

  { ER_CURRENT_TAKES_NO_ARGS, "current() \ud568\uc218\uac00 \uc778\uc218\ub97c \uc2b9\uc778\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4." },

  { ER_DOCUMENT_REPLACED,
      "document() \ud568\uc218 \uad6c\ud604\uc774 org.apache.xalan.xslt.FuncDocument\ub85c \ubc14\ub00c\uc5c8\uc2b5\ub2c8\ub2e4."},

  { ER_CONTEXT_HAS_NO_OWNERDOC,
      "\ubb38\ub9e5\uc5d0 \uc18c\uc720\uc790 \ubb38\uc11c\uac00 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_LOCALNAME_HAS_TOO_MANY_ARGS,
      "local-name()\uc5d0 \ub9ce\uc740 \uc778\uc218\uac00 \uc788\uc2b5\ub2c8\ub2e4."},

  { ER_NAMESPACEURI_HAS_TOO_MANY_ARGS,
      "namespace-uri()\uc5d0 \ub9ce\uc740 \uc778\uc218\uac00 \uc788\uc2b5\ub2c8\ub2e4."},

  { ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS,
      "normalize-space()\uc5d0 \ub9ce\uc740 \uc778\uc218\uac00 \uc788\uc2b5\ub2c8\ub2e4."},

  { ER_NUMBER_HAS_TOO_MANY_ARGS,
      "number()\uc5d0 \ub9ce\uc740 \uc778\uc218\uac00 \uc788\uc2b5\ub2c8\ub2e4."},

  { ER_NAME_HAS_TOO_MANY_ARGS,
     "name()\uc5d0 \ub9ce\uc740 \uc778\uc218\uac00 \uc788\uc2b5\ub2c8\ub2e4."},

  { ER_STRING_HAS_TOO_MANY_ARGS,
      "string()\uc5d0 \ub9ce\uc740 \uc778\uc218\uac00 \uc788\uc2b5\ub2c8\ub2e4."},

  { ER_STRINGLENGTH_HAS_TOO_MANY_ARGS,
      "string-length()\uc5d0 \ub9ce\uc740 \uc778\uc218\uac00 \uc788\uc2b5\ub2c8\ub2e4."},

  { ER_TRANSLATE_TAKES_3_ARGS,
      "translate() \ud568\uc218\uac00 \uc138 \uac1c\uc758 \uc778\uc218\ub97c \ucde8\ud569\ub2c8\ub2e4."},

  { ER_UNPARSEDENTITYURI_TAKES_1_ARG,
      "unparsed-entity-uri \ud568\uc218\ub294 \ud558\ub098\uc758 \uc778\uc218\ub97c \ucde8\ud574\uc57c \ud569\ub2c8\ub2e4."},

  { ER_NAMESPACEAXIS_NOT_IMPLEMENTED,
      "\uc774\ub984 \uacf5\uac04 \ucd95\uc774 \uc544\uc9c1 \uad6c\ud604\ub418\uc9c0 \uc54a\uc558\uc2b5\ub2c8\ub2e4."},

  { ER_UNKNOWN_AXIS,
     "\uc54c \uc218 \uc5c6\ub294 \ucd95: {0}"},

  { ER_UNKNOWN_MATCH_OPERATION,
     "\uc54c \uc218 \uc5c6\ub294 \uc77c\uce58 \uc870\uc791\uc785\ub2c8\ub2e4."},

  { ER_INCORRECT_ARG_LENGTH,
      "processing-instruction() node \ud14c\uc2a4\ud2b8\uc758 \uc778\uc218 \uae38\uc774\uac00 \uc62c\ubc14\ub974\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_CANT_CONVERT_TO_NUMBER,
      "{0}\uc744(\ub97c) \uc22b\uc790\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_CANT_CONVERT_TO_NODELIST,
      "{0}\uc744(\ub97c) NodeList\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_CANT_CONVERT_TO_MUTABLENODELIST,
      "{0}\uc744(\ub97c) NodeSetDTM\uc73c\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_CANT_CONVERT_TO_TYPE,
      "{0}\uc744(\ub97c) \uc720\ud615 \ubc88\ud638 {1}(\uc73c)\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_EXPECTED_MATCH_PATTERN,
      "\uc608\uc0c1\ub41c getMatchScore\uc758 \ud328\ud134 \uc77c\uce58\uc785\ub2c8\ub2e4."},

  { ER_COULDNOT_GET_VAR_NAMED,
      "\uc774\ub984\uc774 {0}\uc778 \ubcc0\uc218\ub97c \uac00\uc838\uc62c \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_UNKNOWN_OPCODE,
     "\uc624\ub958. \uc54c \uc218 \uc5c6\ub294 op \ucf54\ub4dc: {0}"},

  { ER_EXTRA_ILLEGAL_TOKENS,
     "\uc720\ud6a8\ud558\uc9c0 \uc54a\uc740 \ucd94\uac00 \ud1a0\ud070: {0}"},


  { ER_EXPECTED_DOUBLE_QUOTE,
      "\ub530\uc634\ud45c\uac00 \ud2c0\ub9b0 \ub9ac\ud130\ub7f4\uc774 \uc788\uc2b5\ub2c8\ub2e4. \ud070\ub530\uc634\ud45c\uac00 \uc608\uc0c1\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  { ER_EXPECTED_SINGLE_QUOTE,
      "\ub530\uc634\ud45c\uac00 \ud2c0\ub9b0 \ub9ac\ud130\ub7f4\uc774 \uc788\uc2b5\ub2c8\ub2e4. \uc791\uc740\ub530\uc634\ud45c\uac00 \uc608\uc0c1\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  { ER_EMPTY_EXPRESSION,
     "\ube48 \ud45c\ud604\uc2dd\uc785\ub2c8\ub2e4."},

  { ER_EXPECTED_BUT_FOUND,
     "{0}\uc744(\ub97c) \uc608\uc0c1\ud588\uc73c\ub098 {1}\uc774(\uac00) \ubc1c\uacac\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  { ER_INCORRECT_PROGRAMMER_ASSERTION,
      "\ud504\ub85c\uadf8\ub798\uba38 \ub2e8\uc5b8\ubb38\uc774 \uc62c\ubc14\ub974\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4. - {0}"},

  { ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,
      "19990709 XPath \ucd08\uc548\uc5d0\uc11c\ub294 \ubd80\uc6b8(...) \uc778\uc218\uac00 \ub354 \uc774\uc0c1 \uc120\ud0dd\uc801\uc774\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,
      "','\ub97c \ubc1c\uacac\ud588\uc73c\ub098 \uadf8 \uc55e\uc5d0 \uc5b4\ub5a0\ud55c \uc778\uc218\ub3c4 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
      "','\ub97c \ubc1c\uacac\ud588\uc73c\ub098 \ub4a4\uc5d0 \uc5b4\ub5a0\ud55c \uc778\uc218\ub3c4 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_PREDICATE_ILLEGAL_SYNTAX,
      "'..[predicate]' \ub610\ub294 '.[predicate]'\ub294 \uc720\ud6a8\ud558\uc9c0 \uc54a\uc740 \uad6c\ubb38\uc785\ub2c8\ub2e4. \ub300\uc2e0 'self::node()[predicate]'\ub97c \uc0ac\uc6a9\ud558\uc2ed\uc2dc\uc624."},

  { ER_ILLEGAL_AXIS_NAME,
     "\uc720\ud6a8\ud558\uc9c0 \uc54a\uc740 \ucd95 \uc774\ub984: {0}"},

  { ER_UNKNOWN_NODETYPE,
     "\uc54c \uc218 \uc5c6\ub294 \ub178\ub4dc \uc720\ud615: {0}"},

  { ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
      "\ud328\ud134 \ub9ac\ud130\ub7f4({0})\uc5d0\ub294 \ub530\uc634\ud45c\uac00 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4."},

  { ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
      "{0}\uc740(\ub294) \uc22b\uc790\ub85c \ud3ec\ub9f7\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_COULDNOT_CREATE_XMLPROCESSORLIAISON,
      "XML TransformerFactory Liaison\uc744 \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}"},

  { ER_DIDNOT_FIND_XPATH_SELECT_EXP,
      "\uc624\ub958. xpath \uc120\ud0dd \ud45c\ud604\uc2dd(-select)\uc744 \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH,
      "\uc624\ub958. OP_LOCATIONPATH \ub4a4\uc5d0 ENDOP\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_ERROR_OCCURED,
     "\uc624\ub958\uac00 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4."},

  { ER_ILLEGAL_VARIABLE_REFERENCE,
      "\ubcc0\uc218\uc5d0 \ub300\ud574 \uc8fc\uc5b4\uc9c4 VariableReference\uac00 \ubc94\uc704\ub97c \ubc97\uc5b4\ub0ac\uac70\ub098 \uc815\uc758\uac00 \uc5c6\uc2b5\ub2c8\ub2e4. \uc774\ub984 = {0}"},

  { ER_AXES_NOT_ALLOWED,
      "\ud558\uc704:: \ubc0f \uc18d\uc131:: \ucd95\ub9cc \ud328\ud134\uc5d0 \uc77c\uce58\ud560 \uc218 \uc788\uc2b5\ub2c8\ub2e4. \uc704\ubc18 \ucd95 = {0}"},

  { ER_KEY_HAS_TOO_MANY_ARGS,
      "key()\uc758 \uc778\uc218 \uc218\uac00 \uc62c\ubc14\ub974\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_COUNT_TAKES_1_ARG,
      "count \ud568\uc218\ub294 \ud558\ub098\uc758 \uc778\uc218\ub97c \ucde8\ud574\uc57c \ud569\ub2c8\ub2e4."},

  { ER_COULDNOT_FIND_FUNCTION,
     "\ud568\uc218\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}"},

  { ER_UNSUPPORTED_ENCODING,
     "\uc9c0\uc6d0\ub418\uc9c0 \uc54a\ub294 \uc778\ucf54\ub529: {0}"},

  { ER_PROBLEM_IN_DTM_NEXTSIBLING,
      "getNextSibling\uc758 DTM\uc5d0 \ubb38\uc81c\uac00 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4. \ubcf5\uad6c \uc2dc\ub3c4 \uc911\uc785\ub2c8\ub2e4."},

  { ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL,
      "\ud504\ub85c\uadf8\ub798\uba38 \uc624\ub958: EmptyNodeList\ub97c \uc4f8 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_SETDOMFACTORY_NOT_SUPPORTED,
      "XPathContext\uc5d0\uc11c setDOMFactory\ub97c \uc9c0\uc6d0\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_PREFIX_MUST_RESOLVE,
      "\uc811\ub450\ubd80\ub294 \uc774\ub984 \uacf5\uac04\uc73c\ub85c \ubd84\uc11d\ub418\uc5b4\uc57c \ud569\ub2c8\ub2e4: {0}"},

  { ER_PARSE_NOT_SUPPORTED,
      "XPathContext\uc5d0\uc11c \uad6c\ubb38 \ubd84\uc11d(InputSource \uc18c\uc2a4)\uc774 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4. {0}\uc744(\ub97c) \uc5f4 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_SAX_API_NOT_HANDLED,
      "SAX API \ubb38\uc790(char ch[]... \uac00 DTM\uc5d0 \uc758\ud574 \ucc98\ub9ac\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_IGNORABLE_WHITESPACE_NOT_HANDLED,
      "ignorableWhitespace(char ch[]... \uac00 DTM\uc5d0 \uc758\ud574 \ucc98\ub9ac\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_DTM_CANNOT_HANDLE_NODES,
      "DTMLiaison\uc774 {0} \uc720\ud615\uc758 \ub178\ub4dc\ub97c \ucc98\ub9ac\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_XERCES_CANNOT_HANDLE_NODES,
      "DOM2Helper\uac00 {0} \uc720\ud615\uc758 \ub178\ub4dc\ub97c \ucc98\ub9ac\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_XERCES_PARSE_ERROR_DETAILS,
      "DOM2Helper.parse \uc624\ub958: \uc2dc\uc2a4\ud15c ID - {0} \ud68c\uc120 - {1}"},

  { ER_XERCES_PARSE_ERROR,
     "DOM2Helper.parse \uc624\ub958"},

  { ER_INVALID_UTF16_SURROGATE,
      "\uc798\ubabb\ub41c UTF-16 \ub300\ub9ac\uc790(surrogate)\uac00 \ubc1c\uacac\ub418\uc5c8\uc2b5\ub2c8\ub2e4: {0} ?"},

  { ER_OIERROR,
     "IO \uc624\ub958"},

  { ER_CANNOT_CREATE_URL,
     "url\uc744 \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}"},

  { ER_XPATH_READOBJECT,
     "XPath.readObject\uc758 {0}"},

  { ER_FUNCTION_TOKEN_NOT_FOUND,
      "\ud568\uc218 \ud1a0\ud070\uc774 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_CANNOT_DEAL_XPATH_TYPE,
       "XPath \uc720\ud615\uc744 \ucc98\ub9ac\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}"},

  { ER_NODESET_NOT_MUTABLE,
       "\uc774 NodeSet\uac00 \uac00\ubcc0\uc801\uc774\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_NODESETDTM_NOT_MUTABLE,
       "\uc774 NodeSetDTM\uc774 \uac00\ubcc0\uc801\uc774\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_VAR_NOT_RESOLVABLE,
        "\ubcc0\uc218\ub97c \ubd84\uc11d\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}"},

  { ER_NULL_ERROR_HANDLER,
        "\ub110(null) \uc624\ub958 \ud578\ub4e4\ub7ec"},

  { ER_PROG_ASSERT_UNKNOWN_OPCODE,
       "\ud504\ub85c\uadf8\ub798\uba38\uc758 \ub2e8\uc5b8\ubb38: \uc54c \uc218 \uc5c6\ub294 op \ucf54\ub4dc: {0}"},

  { ER_ZERO_OR_ONE,
       "0 \ub610\ub294 1"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "XRTreeFragSelectWrapper\uc5d0\uc11c rtf()\ub97c \uc9c0\uc6d0\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "XRTreeFragSelectWrapper\uc5d0\uc11c asNodeIterator()\ub97c \uc9c0\uc6d0\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

   { ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper\uc5d0\uc11c detach()\ub97c \uc9c0\uc6d0\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

   { ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper\uc5d0\uc11c num()\uc744 \uc9c0\uc6d0\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

   { ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper\uc5d0\uc11c xstr()\uc744 \uc9c0\uc6d0\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

   { ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper\uc5d0\uc11c str()\uc744 \uc9c0\uc6d0\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,
       "XStringForChars\uc5d0 \ub300\ud574 fsb()\uac00 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_COULD_NOT_FIND_VAR,
      "\uc774\ub984\uc774 {0}\uc778 \ubcc0\uc218\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,
      "XStringForChars\ub294 \uc778\uc218\ub85c \ubb38\uc790\uc5f4\uc744 \uac00\uc838\uc62c \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,
      "FastStringBuffer \uc778\uc218\ub294 \ub110(null)\uc774 \ub420 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_TWO_OR_THREE,
       "2 \ub610\ub294 3"},

  { ER_VARIABLE_ACCESSED_BEFORE_BIND,
       "\ubcc0\uc218\uac00 \ubc14\uc778\ub4dc\ub418\uae30 \uc804\uc5d0 \ubcc0\uc218\uc5d0 \uc561\uc138\uc2a4\ud588\uc2b5\ub2c8\ub2e4."},

  { ER_FSB_CANNOT_TAKE_STRING,
       "XStringForFSB\ub294 \uc778\uc218\ub85c \ubb38\uc790\uc5f4\uc744 \ucde8\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_SETTING_WALKER_ROOT_TO_NULL,
       "\n !!!! \uc624\ub958. \uc6cc\ucee4\uc758 \ub8e8\ud2b8\ub85c \ub110(null)\uc774 \uc124\uc815\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  { ER_NODESETDTM_CANNOT_ITERATE,
       "\uc774 NodeSetDTM\uc740 \uc774\uc804 \ub178\ub4dc\uc5d0 \ubc18\ubcf5 \uc801\uc6a9\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_NODESET_CANNOT_ITERATE,
       "\uc774 NodeSet\ub294 \uc774\uc804 \ub178\ub4dc\uc5d0 \ubc18\ubcf5 \uc801\uc6a9\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_NODESETDTM_CANNOT_INDEX,
       "\uc774 NodeSetDTM\uc740 \uc0c9\uc778 \ub610\ub294 \uce74\uc6b4\ud305 \ud568\uc218\ub97c \uc0ac\uc6a9\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_NODESET_CANNOT_INDEX,
       "\uc774 NodeSet\ub294 \uc0c9\uc778 \ub610\ub294 \uce74\uc6b4\ud305 \ud568\uc218\ub97c \uc0ac\uc6a9\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_CANNOT_CALL_SETSHOULDCACHENODE,
       "nextNode\uac00 \ud638\ucd9c\ub41c \ud6c4\uc5d0 setShouldCacheNodes\ub97c \ud638\ucd9c\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { ER_ONLY_ALLOWS,
       "{0}\uc740(\ub294) {1} \uc778\uc218\ub9cc\uc744 \ud5c8\uc6a9\ud569\ub2c8\ub2e4."},

  { ER_UNKNOWN_STEP,
       "getNextStepPos\uc5d0 \ud504\ub85c\uadf8\ub798\uba38\uc758 \ub2e8\uc5b8\ubb38\uc774 \uc788\uc74c: \uc54c \uc218 \uc5c6\ub294 stepType: {0} "},

  //Note to translators:  A relative location path is a form of XPath expression.
  // The message indicates that such an expression was expected following the
  // characters '/' or '//', but was not found.
  { ER_EXPECTED_REL_LOC_PATH,
      "'/' \ub610\ub294 '//' \ud1a0\ud070 \ub2e4\uc74c\uc5d0 \uad00\ub828 \uc704\uce58 \uacbd\ub85c\uac00 \uc608\uc0c1\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such an expression was expected,but
  // the characters specified by the substitution text were encountered instead.
  { ER_EXPECTED_LOC_PATH,
       "\uc704\uce58 \uacbd\ub85c\uac00 \uc608\uc0c1\ub418\uc5c8\uc9c0\ub9cc \ub2e4\uc74c \ud1a0\ud070\uc774 \ubc1c\uacac\ub418\uc5c8\uc2b5\ub2c8\ub2e4.\u003a  {0}"},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such a subexpression was expected,
  // but no more characters were found in the expression.
  { ER_EXPECTED_LOC_PATH_AT_END_EXPR,
       "\uc704\uce58 \uacbd\ub85c\uac00 \uc608\uc0c1\ub418\uc5c8\uc9c0\ub9cc XPath \ud45c\ud604\uc2dd\uc758 \ub05d\uc774 \ubc1c\uacac\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  // Note to translators:  A location step is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected
  // following the specified characters.
  { ER_EXPECTED_LOC_STEP,
       "'/' \ub610\ub294 '//' \ud1a0\ud070 \ub2e4\uc74c\uc5d0 \uc704\uce58 \ub2e8\uacc4\uac00 \uc608\uc0c1\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  // Note to translators:  A node test is part of an XPath expression that is
  // used to test for particular kinds of nodes.  In this case, a node test that
  // consists of an NCName followed by a colon and an asterisk or that consists
  // of a QName was expected, but was not found.
  { ER_EXPECTED_NODE_TEST,
       "NCName:* \ub610\ub294 QName\uacfc \uc77c\uce58\ud558\ub294 \ub178\ub4dc \ud14c\uc2a4\ud2b8\uac00 \uc608\uc0c1\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  // Note to translators:  A step pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but the specified character was found in the expression instead.
  { ER_EXPECTED_STEP_PATTERN,
       "\ub2e8\uacc4 \ud328\ud134\uc774 \uc608\uc0c1\ub418\uc5c8\uc9c0\ub9cc '/'\uac00 \ubc1c\uacac\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  // Note to translators: A relative path pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but was not found.
  { ER_EXPECTED_REL_PATH_PATTERN,
       "\uad00\ub828 \uacbd\ub85c \ud328\ud134\uc774 \uc608\uc0c1\ub418\uc5c8\uc2b5\ub2c8\ub2e4."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type boolean.
  { ER_CANT_CONVERT_TO_BOOLEAN,
       "XPath \ud45c\ud604\uc2dd ''{0}''\uc758 XPathResult\uc5d0 \ubd80\uc6b8\ub85c \ubcc0\ud658\ub420 \uc218 \uc5c6\ub294 XPathResultType {1}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},

  // Note to translators: Do not translate ANY_UNORDERED_NODE_TYPE and
  // FIRST_ORDERED_NODE_TYPE.
  { ER_CANT_CONVERT_TO_SINGLENODE,
       "XPath \ud45c\ud604\uc2dd ''{0}''\uc758 XPathResult\uc5d0 \ub2e8\uc77c \ub178\ub4dc\ub85c \ubcc0\ud658\ub420 \uc218 \uc5c6\ub294 XPathResultType {1}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4. \uba54\uc18c\ub4dc getSingleNodeValue\ub294 ANY_UNORDERED_NODE_TYPE \ubc0f FIRST_ORDERED_NODE_TYPE \uc720\ud615\uc5d0\ub9cc \uc801\uc6a9\ub429\ub2c8\ub2e4."},

  // Note to translators: Do not translate UNORDERED_NODE_SNAPSHOT_TYPE and
  // ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_CANT_GET_SNAPSHOT_LENGTH,
       "XPathResultType\uc774 {1}\uc774\uae30 \ub54c\ubb38\uc5d0 XPath \ud45c\ud604\uc2dd ''{0}''\uc758 XPathResult\uc5d0\uc11c getSnapshotLength \uba54\uc18c\ub4dc\ub97c \ud638\ucd9c\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. \uc774 \uba54\uc18c\ub4dc\ub294 UNORDERED_NODE_SNAPSHOT_TYPE \ubc0f ORDERED_NODE_SNAPSHOT_TYPE \uc720\ud615\uc5d0\ub9cc \uc801\uc6a9\ub429\ub2c8\ub2e4."},

  { ER_NON_ITERATOR_TYPE,
       "XPathResultType\uc774 {1}\uc774\uae30 \ub54c\ubb38\uc5d0 XPath \ud45c\ud604\uc2dd ''{0}''\uc758 XPathResult\uc5d0\uc11c iterateNext \uba54\uc18c\ub4dc\ub97c \ud638\ucd9c\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. \uc774 \uba54\uc18c\ub4dc\ub294 UNORDERED_NODE_ITERATOR_TYPE \ubc0f ORDERED_NODE_ITERATOR_TYPE \uc720\ud615\uc5d0\ub9cc \uc801\uc6a9\ub429\ub2c8\ub2e4."},

  // Note to translators: This message indicates that the document being operated
  // upon changed, so the iterator object that was being used to traverse the
  // document has now become invalid.
  { ER_DOC_MUTATED,
       "\uacb0\uacfc\uac00 \ub9ac\ud134\ub418\uc5c8\uc73c\ubbc0\ub85c \ubb38\uc11c\uac00 \ubcc0\uacbd\ub429\ub2c8\ub2e4. \ubc18\ubcf5\uae30\uac00 \uc720\ud6a8\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_INVALID_XPATH_TYPE,
       "\uc798\ubabb\ub41c XPath \uc720\ud615 \uc778\uc218: {0}"},

  { ER_EMPTY_XPATH_RESULT,
       "\ube44\uc5b4 \uc788\ub294 XPath \uacb0\uacfc \uc624\ube0c\uc81d\ud2b8\uc785\ub2c8\ub2e4."},

  { ER_INCOMPATIBLE_TYPES,
       "XPath \ud45c\ud604\uc2dd ''{0}''\uc758 XPathResult\uc5d0 \uc9c0\uc815\ub41c XPathResultType {2}(\uc73c)\ub85c \uac15\uc81c \uc2dc\ud589\ud560 \uc218 \uc5c6\ub294 XPathReultType {1}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},

  { ER_NULL_RESOLVER,
       "\ub110(null) \uc811\ub450\ubd80 \ubd84\uc11d\uae30\ub85c \uc811\ub450\ubd80\ub97c \ubd84\uc11d\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.
  { ER_CANT_CONVERT_TO_STRING,
       "XPath \ud45c\ud604\uc2dd ''{0}''\uc758 XPathResult\uc5d0 \ubb38\uc790\uc5f4\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\ub294 XPathResultType {1}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},

  // Note to translators: Do not translate snapshotItem,
  // UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_NON_SNAPSHOT_TYPE,
       "XPathResultType\uc774 {1}\uc774\uae30 \ub54c\ubb38\uc5d0 XPath \ud45c\ud604\uc2dd ''{0}''\uc758 XPathResult\uc5d0\uc11c snapshotItem \uba54\uc18c\ub4dc\ub97c \ud638\ucd9c\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. \uc774 \uba54\uc18c\ub4dc\ub294 UNORDERED_NODE_SNAPSHOT_TYPE \ubc0f ORDERED_NODE_SNAPSHOT_TYPE \uc720\ud615\uc5d0\ub9cc \uc801\uc6a9\ub429\ub2c8\ub2e4."},

  // Note to translators:  XPathEvaluator is a Java interface name.  An
  // XPathEvaluator is created with respect to a particular XML document, and in
  // this case the expression represented by this object was being evaluated with
  // respect to a context node from a different document.
  { ER_WRONG_DOCUMENT,
       "\ucee8\ud14d\uc2a4\ud2b8 \ub178\ub4dc\ub294 \uc774 XPathEvaluator\ub85c \ubc14\uc778\ub4dc\ub418\ub294 \ubb38\uc11c\uc5d0 \ud3ec\ud568\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  // Note to translators:  The XPath expression cannot be evaluated with respect
  // to this type of node.
  { ER_WRONG_NODETYPE,
       "\ucee8\ud14d\uc2a4\ud2b8 \ub178\ub4dc \uc720\ud615\uc774 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { ER_XPATH_ERROR,
       "XPath\uc5d0 \uc54c \uc218 \uc5c6\ub294 \uc624\ub958\uac00 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4."},

        { ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER,
                "XPath \ud45c\ud604\uc2dd ''{0}''\uc758 XPathResult\uc5d0 \uc22b\uc790\ub85c \ubcc0\ud658\ud560 \uc218 \uc5c6\ub294 XPathResultType {1}\uc774(\uac00) \uc788\uc2b5\ub2c8\ub2e4."},

  //BEGIN:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  /** Field ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED                       */

  { ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
       "\ud655\uc7a5 \ud568\uc218: XMLConstants.FEATURE_SECURE_PROCESSING \uae30\ub2a5\uc774 true\ub85c \uc124\uc815\ub41c \uacbd\uc6b0 ''{0}''\uc744(\ub97c) \ud638\ucd9c\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  /** Field ER_RESOLVE_VARIABLE_RETURNS_NULL                       */

  { ER_RESOLVE_VARIABLE_RETURNS_NULL,
       "{0} \ubcc0\uc218\uc5d0 \ub300\ud55c resolveVariable\uc774 \ub110(null)\uc744 \ub9ac\ud134\ud569\ub2c8\ub2e4."},

  /** Field ER_UNSUPPORTED_RETURN_TYPE                       */

  { ER_UNSUPPORTED_RETURN_TYPE,
       "\uc9c0\uc6d0\ub418\uc9c0 \uc54a\ub294 \ub9ac\ud134 \uc720\ud615: {0}"},

  /** Field ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL                       */

  { ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL,
       "\uc18c\uc2a4 \ubc0f/\ub610\ub294 \ub9ac\ud134 \uc720\ud615\uc774 \ub110(null)\uc774\uba74 \uc548\ub429\ub2c8\ub2e4."},

  /** Field ER_ARG_CANNOT_BE_NULL                       */

  { ER_ARG_CANNOT_BE_NULL,
       "{0} \uc778\uc218\uac00 \ub110(null)\uc774\uba74 \uc548\ub429\ub2c8\ub2e4."},

  /** Field ER_OBJECT_MODEL_NULL                       */

  { ER_OBJECT_MODEL_NULL,
       "{0}#isObjectModelSupported( String objectModel )\uc740 objectModel == null\uc744 \uc0ac\uc6a9\ud558\uc5ec \ud638\ucd9c\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_OBJECT_MODEL_EMPTY,
       "{0}#isObjectModelSupported( String objectModel )\uc740 objectModel == \"\"\uc744 \uc0ac\uc6a9\ud558\uc5ec \ud638\ucd9c\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_FEATURE_NAME_NULL,
       "\ub110(null) \uc774\ub984\uc744 \uc0ac\uc6a9\ud558\uc5ec \uae30\ub2a5\uc744 \uc124\uc815\ud558\ub824\uace0 \ud569\ub2c8\ub2e4: {0}#setFeature( null, {1})"},

  /** Field ER_FEATURE_UNKNOWN                       */

  { ER_FEATURE_UNKNOWN,
       "\uc54c \uc218 \uc5c6\ub294 \uae30\ub2a5 \"{0}\":{1}#setFeature({0},{2})\ub97c \uc124\uc815\ud558\ub824\uace0 \ud569\ub2c8\ub2e4."},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_NULL_FEATURE,
       "\ub110(null) \uc774\ub984\uc744 \uc0ac\uc6a9\ud558\uc5ec \uae30\ub2a5\uc744 \uc124\uc815\ud558\ub824\uace0 \ud569\ub2c8\ub2e4: {0}#getFeature(null)"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_UNKNOWN_FEATURE,
       "\uc54c \uc218 \uc5c6\ub294 \uae30\ub2a5 \"{0}\"\uc744 \uac00\uc838\uc624\ub824\uace0 \ud569\ub2c8\ub2e4: {1}#getFeature({0})"},

  /** Field ER_NULL_XPATH_FUNCTION_RESOLVER                       */

  { ER_NULL_XPATH_FUNCTION_RESOLVER,
       "\ub110(null)\uc778 XPathFunctionResolver\ub97c \uc124\uc815\ud558\ub824\uace0 \ud569\ub2c8\ub2e4: {0}#setXPathFunctionResolver(null)"},

  /** Field ER_NULL_XPATH_VARIABLE_RESOLVER                       */

  { ER_NULL_XPATH_VARIABLE_RESOLVER,
       "\ub110(null)\uc778 XPathVariableResolver\ub97c \uc124\uc815\ud558\ub824\uace0 \ud569\ub2c8\ub2e4: {0}#setXPathVariableResolver(null)"},

  //END:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  // Warnings...

  { WG_LOCALE_NAME_NOT_HANDLED,
      "format-number \ud568\uc218\uc5d0 \uc788\ub294 \ub85c\ucf00\uc77c \uc774\ub984\uc774 \uc544\uc9c1 \ucc98\ub9ac\ub418\uc9c0 \uc54a\uc558\uc2b5\ub2c8\ub2e4."},

  { WG_PROPERTY_NOT_SUPPORTED,
      "XSL \ud2b9\uc131\uc774 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4: {0}"},

  { WG_DONT_DO_ANYTHING_WITH_NS,
      "\ud2b9\uc131\uc5d0\uc11c {0} \uc774\ub984 \uacf5\uac04\uacfc \uad00\ub828\ud558\uc5ec \ud604\uc7ac \uc544\ubb34\ub7f0 \uc791\uc5c5\ub3c4 \uc218\ud589\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4: {1}"},

  { WG_SECURITY_EXCEPTION,
      "XSL \uc2dc\uc2a4\ud15c \ud2b9\uc131\uc5d0 \uc561\uc138\uc2a4\ud558\ub294 \uc911 SecurityException\uc774 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4: {0}"},

  { WG_QUO_NO_LONGER_DEFINED,
      "\uc774\uc804 \uad6c\ubb38: quo(...)\uac00 \ub354 \uc774\uc0c1 XPath\uc5d0 \uc815\uc758\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST,
      "nodeTest\ub97c \uad6c\ud604\ud558\ub824\uba74 XPath\uc5d0 \ud30c\uc0dd\ub41c \uc624\ube0c\uc81d\ud2b8\uac00 \uc788\uc5b4\uc57c \ud569\ub2c8\ub2e4."},

  { WG_FUNCTION_TOKEN_NOT_FOUND,
      "\ud568\uc218 \ud1a0\ud070\uc774 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { WG_COULDNOT_FIND_FUNCTION,
      "\ud568\uc218\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4: {0}"},

  { WG_CANNOT_MAKE_URL_FROM,
      "{0}\uc5d0\uc11c URL\uc744 \uc791\uc131\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4."},

  { WG_EXPAND_ENTITIES_NOT_SUPPORTED,
      "DTM \uad6c\ubb38 \ubd84\uc11d\uae30\uc5d0 \ub300\ud574 -E \uc635\uc158\uc774 \uc9c0\uc6d0\ub418\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4."},

  { WG_ILLEGAL_VARIABLE_REFERENCE,
      "\ubcc0\uc218\uc5d0 \ub300\ud574 \uc8fc\uc5b4\uc9c4 VariableReference\uac00 \ubc94\uc704\ub97c \ubc97\uc5b4\ub0ac\uac70\ub098 \uc815\uc758\uac00 \uc5c6\uc2b5\ub2c8\ub2e4. \uc774\ub984 = {0}"},

  { WG_UNSUPPORTED_ENCODING,
     "\uc9c0\uc6d0\ub418\uc9c0 \uc54a\ub294 \uc778\ucf54\ub529: {0}"},



  // Other miscellaneous text used inside the code...
  { "ui_language", "ko"},
  { "help_language", "ko"},
  { "language", "ko"},
  { "BAD_CODE", "createMessage\uc5d0 \ub300\ud55c \ub9e4\uac1c\ubcc0\uc218\uac00 \ubc94\uc704\ub97c \ubc97\uc5b4\ub0ac\uc2b5\ub2c8\ub2e4."},
  { "FORMAT_FAILED", "messageFormat \ud638\ucd9c \uc911 \uc608\uc678\uac00 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4."},
  { "version", ">>>>>>> Xalan \ubc84\uc804 "},
  { "version2", "<<<<<<<"},
  { "yes", "\uc608"},
  { "line", "\ud589 #"},
  { "column", "\uc5f4 #"},
  { "xsldone", "XSLProcessor: \uc644\ub8cc"},
  { "xpath_option", "xpath \uc635\uc158: "},
  { "optionIN", "[-in inputXMLURL]"},
  { "optionSelect", "[-select xpath expression]"},
  { "optionMatch", "[-match match pattern(\uc77c\uce58 \uc9c4\ub2e8\uc6a9)]"},
  { "optionAnyExpr", "\ub610\ub294 xpath \ud45c\ud604\uc2dd\ub9cc\uc73c\ub85c \uc9c4\ub2e8 \ub364\ud504\uac00 \uc218\ud589\ub420 \uac83\uc785\ub2c8\ub2e4."},
  { "noParsermsg1", "XSL \ud504\ub85c\uc138\uc2a4\uac00 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4."},
  { "noParsermsg2", "** \uad6c\ubb38 \ubd84\uc11d\uae30\ub97c \ucc3e\uc744 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. **"},
  { "noParsermsg3", "\ud074\ub798\uc2a4 \uacbd\ub85c\ub97c \uc810\uac80\ud558\uc2ed\uc2dc\uc624."},
  { "noParsermsg4", "Java\uc6a9 IBM XML \uad6c\ubb38 \ubd84\uc11d\uae30\uac00 \uc5c6\uc73c\uba74"},
  { "noParsermsg5", "IBM's AlphaWorks: http://www.alphaworks.ibm.com/formula/xml\uc5d0\uc11c \ub2e4\uc6b4\ub85c\ub4dc\ud560 \uc218 \uc788\uc2b5\ub2c8\ub2e4."},
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
  public static final String ERROR_HEADER = "\uc624\ub958: ";

  /** Field WARNING_HEADER          */
  public static final String WARNING_HEADER = "\uacbd\uace0: ";

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
                new Locale("ko", "KR"));
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
