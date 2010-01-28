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
 * $Id: XPATHErrorResources_tr.java 468655 2006-10-28 07:12:06Z minchau $
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
public class XPATHErrorResources_tr extends ListResourceBundle
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

  { ER_CURRENT_NOT_ALLOWED_IN_MATCH, "E\u015fle\u015fme \u00f6r\u00fcnt\u00fcs\u00fcnde current() i\u015flevine izin verilmez!" },

  { ER_CURRENT_TAKES_NO_ARGS, "current() i\u015flevi ba\u011f\u0131ms\u0131z de\u011fi\u015fken kabul etmez!" },

  { ER_DOCUMENT_REPLACED,
      "document() i\u015flevi uygulamas\u0131 org.apache.xalan.xslt.FuncDocument ile de\u011fi\u015ftirildi!"},

  { ER_CONTEXT_HAS_NO_OWNERDOC,
      "Ba\u011flam\u0131n iye belgesi yok!"},

  { ER_LOCALNAME_HAS_TOO_MANY_ARGS,
      "local-name() i\u015flevinde \u00e7ok fazla ba\u011f\u0131ms\u0131z de\u011fi\u015fken var."},

  { ER_NAMESPACEURI_HAS_TOO_MANY_ARGS,
      "namespace-uri() i\u015flevinde \u00e7ok fazla ba\u011f\u0131ms\u0131z de\u011fi\u015fken var."},

  { ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS,
      "normalize-space() i\u015flevinde \u00e7ok fazla ba\u011f\u0131ms\u0131z de\u011fi\u015fken var."},

  { ER_NUMBER_HAS_TOO_MANY_ARGS,
      "number() i\u015flevinde \u00e7ok fazla ba\u011f\u0131ms\u0131z de\u011fi\u015fken var."},

  { ER_NAME_HAS_TOO_MANY_ARGS,
     "name() i\u015flevinde \u00e7ok fazla ba\u011f\u0131ms\u0131z de\u011fi\u015fken var."},

  { ER_STRING_HAS_TOO_MANY_ARGS,
      "string() i\u015flevinde \u00e7ok fazla ba\u011f\u0131ms\u0131z de\u011fi\u015fken var."},

  { ER_STRINGLENGTH_HAS_TOO_MANY_ARGS,
      "string-length() i\u015flevinde \u00e7ok fazla ba\u011f\u0131ms\u0131z de\u011fi\u015fken var."},

  { ER_TRANSLATE_TAKES_3_ARGS,
      "translate() i\u015flevi \u00fc\u00e7 ba\u011f\u0131ms\u0131z de\u011fi\u015fken al\u0131r!"},

  { ER_UNPARSEDENTITYURI_TAKES_1_ARG,
      "unparsed-entity-uri i\u015flevi bir ba\u011f\u0131ms\u0131z de\u011fi\u015fken almal\u0131d\u0131r!"},

  { ER_NAMESPACEAXIS_NOT_IMPLEMENTED,
      "namespace ekseni hen\u00fcz ger\u00e7ekle\u015ftirilmedi!"},

  { ER_UNKNOWN_AXIS,
     "Bilinmeyen eksen: {0}"},

  { ER_UNKNOWN_MATCH_OPERATION,
     "Bilinmeyen e\u015fle\u015fme i\u015flemi!"},

  { ER_INCORRECT_ARG_LENGTH,
      "processing-instruction() d\u00fc\u011f\u00fcm s\u0131namas\u0131n\u0131n ba\u011f\u0131ms\u0131z de\u011fi\u015fken uzunlu\u011fu yanl\u0131\u015f!"},

  { ER_CANT_CONVERT_TO_NUMBER,
      "{0} bir say\u0131ya d\u00f6n\u00fc\u015ft\u00fcr\u00fclemez"},

  { ER_CANT_CONVERT_TO_NODELIST,
      "{0} NodeList''e d\u00f6n\u00fc\u015ft\u00fcr\u00fclemez!"},

  { ER_CANT_CONVERT_TO_MUTABLENODELIST,
      "{0} NodeSetDTM''ye d\u00f6n\u00fc\u015ft\u00fcr\u00fclemez!"},

  { ER_CANT_CONVERT_TO_TYPE,
      "{0} - type#{1} d\u00f6n\u00fc\u015f\u00fcm\u00fc yap\u0131lamaz"},

  { ER_EXPECTED_MATCH_PATTERN,
      "getMatchScore i\u00e7inde e\u015fle\u015fme \u00f6r\u00fcnt\u00fcs\u00fc bekleniyor!"},

  { ER_COULDNOT_GET_VAR_NAMED,
      "{0} adl\u0131 de\u011fi\u015fken al\u0131namad\u0131"},

  { ER_UNKNOWN_OPCODE,
     "HATA! Bilinmeyen i\u015flem kodu: {0}"},

  { ER_EXTRA_ILLEGAL_TOKENS,
     "Fazladan ge\u00e7ersiz simgeler: {0}"},


  { ER_EXPECTED_DOUBLE_QUOTE,
      "Haz\u0131r bilginin t\u0131rnak imi yanl\u0131\u015f... \u00e7ift t\u0131rnak bekleniyor!"},

  { ER_EXPECTED_SINGLE_QUOTE,
      "Haz\u0131r bilginin t\u0131rnak imi yanl\u0131\u015f... tek t\u0131rnak bekleniyor!"},

  { ER_EMPTY_EXPRESSION,
     "\u0130fade bo\u015f!"},

  { ER_EXPECTED_BUT_FOUND,
     "{0} bekleniyordu, {1} bulundu"},

  { ER_INCORRECT_PROGRAMMER_ASSERTION,
      "Programc\u0131 de\u011ferlendirmesi yanl\u0131\u015f! - {0}"},

  { ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,
      "boolean(...) ba\u011f\u0131ms\u0131z de\u011fi\u015fkeni 19990709 XPath tasla\u011f\u0131yla art\u0131k iste\u011fe ba\u011fl\u0131 de\u011fil."},

  { ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,
      "',' bulundu, ancak \u00f6ncesinde ba\u011f\u0131ms\u0131z de\u011fi\u015fken yok!"},

  { ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
      "',' bulundu, ancak sonras\u0131nda ba\u011f\u0131ms\u0131z de\u011fi\u015fken yok!"},

  { ER_PREDICATE_ILLEGAL_SYNTAX,
      "'..[kar\u015f\u0131la\u015ft\u0131rma belirtimi]' ya da '.[kar\u015f\u0131la\u015ft\u0131rma belirtimi]' ge\u00e7ersiz bir s\u00f6zdizimi.  Yerine \u015funu kullan\u0131n: 'self::node()[kar\u015f\u0131la\u015ft\u0131rma belirtimi]'."},

  { ER_ILLEGAL_AXIS_NAME,
     "Eksen ad\u0131 ge\u00e7ersiz: {0}"},

  { ER_UNKNOWN_NODETYPE,
     "D\u00fc\u011f\u00fcm tipi ge\u00e7ersiz: {0}"},

  { ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
      "\u00d6r\u00fcnt\u00fc haz\u0131r bilgisinin ({0}) t\u0131rnak i\u00e7ine al\u0131nmas\u0131 gerekiyor!"},

  { ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
      "{0} bir say\u0131 olarak bi\u00e7imlenemedi!"},

  { ER_COULDNOT_CREATE_XMLPROCESSORLIAISON,
      "XML TransformerFactory ili\u015fkisi {0} yarat\u0131lamad\u0131"},

  { ER_DIDNOT_FIND_XPATH_SELECT_EXP,
      "Hata! xpath select ifadesi (-select) bulunamad\u0131."},

  { ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH,
      "HATA! OP_LOCATIONPATH sonras\u0131nda ENDOP bulunamad\u0131."},

  { ER_ERROR_OCCURED,
     "Hata olu\u015ftu!"},

  { ER_ILLEGAL_VARIABLE_REFERENCE,
      "De\u011fi\u015fken i\u00e7in belirtilen VariableReference ba\u011flam d\u0131\u015f\u0131 ya da tan\u0131ms\u0131z!  Ad = {0}"},

  { ER_AXES_NOT_ALLOWED,
      "E\u015fle\u015fme \u00f6r\u00fcnt\u00fclerinde yaln\u0131zca child:: ve attribute:: eksenlerine izin verilir!  Ge\u00e7ersiz eksenler = {0}"},

  { ER_KEY_HAS_TOO_MANY_ARGS,
      "key() yanl\u0131\u015f say\u0131da ba\u011f\u0131ms\u0131z de\u011fi\u015fken i\u00e7eriyor."},

  { ER_COUNT_TAKES_1_ARG,
      "Say\u0131m i\u015flevi tek bir ba\u011f\u0131ms\u0131z de\u011fi\u015fken almal\u0131d\u0131r!"},

  { ER_COULDNOT_FIND_FUNCTION,
     "\u0130\u015flev bulunamad\u0131: {0}"},

  { ER_UNSUPPORTED_ENCODING,
     "Desteklenmeyen kodlama: {0}"},

  { ER_PROBLEM_IN_DTM_NEXTSIBLING,
      "getNextSibling s\u0131ras\u0131nda DTM i\u00e7inde sorun olu\u015ftu... kurtarma giri\u015fiminde bulunuluyor"},

  { ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL,
      "Programc\u0131 hatas\u0131: EmptyNodeList i\u00e7ine yaz\u0131lamaz."},

  { ER_SETDOMFACTORY_NOT_SUPPORTED,
      "setDOMFactory, XPathContext taraf\u0131ndan desteklenmiyor!"},

  { ER_PREFIX_MUST_RESOLVE,
      "\u00d6nek bir ad alan\u0131na \u00e7\u00f6z\u00fclmelidir: {0}"},

  { ER_PARSE_NOT_SUPPORTED,
      "XPathContext i\u00e7inde parse (InputSource kayna\u011f\u0131) desteklenmiyor! {0} a\u00e7\u0131lam\u0131yor"},

  { ER_SAX_API_NOT_HANDLED,
      "SAX API characters(char ch[]... DTM taraf\u0131ndan i\u015flenmedi!"},

  { ER_IGNORABLE_WHITESPACE_NOT_HANDLED,
      "ignorableWhitespace(char ch[]... DTM taraf\u0131ndan i\u015flenmedi!"},

  { ER_DTM_CANNOT_HANDLE_NODES,
      "DTMLiaison {0} tipi d\u00fc\u011f\u00fcmleri i\u015fleyemez"},

  { ER_XERCES_CANNOT_HANDLE_NODES,
      "DOM2Helper {0} tipi d\u00fc\u011f\u00fcmleri i\u015fleyemez"},

  { ER_XERCES_PARSE_ERROR_DETAILS,
      "DOM2Helper.parse hatas\u0131: Sistem tnt - {0} sat\u0131r - {1}"},

  { ER_XERCES_PARSE_ERROR,
     "DOM2Helper.parse hatas\u0131"},

  { ER_INVALID_UTF16_SURROGATE,
      "UTF-16 yerine kullan\u0131lan de\u011fer ge\u00e7ersiz: {0} ?"},

  { ER_OIERROR,
     "G\u00c7 hatas\u0131"},

  { ER_CANNOT_CREATE_URL,
     "\u0130lgili url yarat\u0131lam\u0131yor: {0}"},

  { ER_XPATH_READOBJECT,
     "XPath.readObject i\u00e7inde: {0}"},

  { ER_FUNCTION_TOKEN_NOT_FOUND,
      "\u0130\u015flev simgesi bulunamad\u0131."},

  { ER_CANNOT_DEAL_XPATH_TYPE,
       "XPath tipi i\u015flenemiyor: {0}"},

  { ER_NODESET_NOT_MUTABLE,
       "Bu NodeSet de\u011fi\u015febilir t\u00fcrde de\u011fil"},

  { ER_NODESETDTM_NOT_MUTABLE,
       "Bu NodeSetDTM de\u011fi\u015febilir t\u00fcrde de\u011fil"},

  { ER_VAR_NOT_RESOLVABLE,
        "De\u011fi\u015fken \u00e7\u00f6z\u00fclebilir bir de\u011fi\u015fken de\u011fil: {0}"},

  { ER_NULL_ERROR_HANDLER,
        "Bo\u015f de\u011ferli hata i\u015fleyici"},

  { ER_PROG_ASSERT_UNKNOWN_OPCODE,
       "Programc\u0131 do\u011frulamas\u0131: bilinmeyen opcode:{0}"},

  { ER_ZERO_OR_ONE,
       "0 ya da 1"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "rtf() XRTreeFragSelectWrapper taraf\u0131ndan desteklenmiyor"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "asNodeIterator() XRTreeFragSelectWrapper taraf\u0131ndan desteklenmiyor"},

   { ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "detach() XRTreeFragSelectWrapper taraf\u0131ndan desteklenmiyor"},

   { ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "num() XRTreeFragSelectWrapper taraf\u0131ndan desteklenmiyor"},

   { ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "xstr() XRTreeFragSelectWrapper taraf\u0131ndan desteklenmiyor"},

   { ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "str() XRTreeFragSelectWrapper taraf\u0131ndan desteklenmiyor"},

  { ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,
       "fsb() XStringForChars i\u00e7in desteklenmiyor"},

  { ER_COULD_NOT_FIND_VAR,
      "{0} ad\u0131nda de\u011fi\u015fken bulunamad\u0131"},

  { ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,
      "XStringForChars ba\u011f\u0131ms\u0131z de\u011fi\u015fken olarak dizgi alamaz"},

  { ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,
      "FastStringBuffer ba\u011f\u0131ms\u0131z de\u011fi\u015fkeni bo\u015f de\u011ferli olamaz"},

  { ER_TWO_OR_THREE,
       "2 ya da 3"},

  { ER_VARIABLE_ACCESSED_BEFORE_BIND,
       "De\u011fi\u015fkene ba\u011f tan\u0131mlamadan \u00f6nce eri\u015fildi!"},

  { ER_FSB_CANNOT_TAKE_STRING,
       "XStringForFSB ba\u011f\u0131ms\u0131z de\u011fi\u015fken olarak dizgi alamaz!"},

  { ER_SETTING_WALKER_ROOT_TO_NULL,
       "\n !!!! Hata! Walker k\u00f6k\u00fc bo\u015f de\u011fere ayarlan\u0131yor!!!"},

  { ER_NODESETDTM_CANNOT_ITERATE,
       "Bu NodeSetDTM \u00f6nceki bir d\u00fc\u011f\u00fcme yineleme yapamaz!"},

  { ER_NODESET_CANNOT_ITERATE,
       "Bu NodeSet \u00f6nceki bir d\u00fc\u011f\u00fcme yineleme yapamaz!"},

  { ER_NODESETDTM_CANNOT_INDEX,
       "Bu NodeSetDTM dizinleme ya da sayma i\u015flevleri yapamaz!"},

  { ER_NODESET_CANNOT_INDEX,
       "Bu NodeSet dizinleme ya da sayma i\u015flevleri yapamaz!"},

  { ER_CANNOT_CALL_SETSHOULDCACHENODE,
       "nextNode \u00e7a\u011fr\u0131ld\u0131ktan sonra setShouldCacheNodes \u00e7a\u011fr\u0131lamaz!"},

  { ER_ONLY_ALLOWS,
       "{0} yaln\u0131zca {1} ba\u011f\u0131ms\u0131z de\u011fi\u015fkene izin verir"},

  { ER_UNKNOWN_STEP,
       "getNextStepPos i\u00e7inde programc\u0131 do\u011frulamas\u0131: bilinmeyen stepType: {0}"},

  //Note to translators:  A relative location path is a form of XPath expression.
  // The message indicates that such an expression was expected following the
  // characters '/' or '//', but was not found.
  { ER_EXPECTED_REL_LOC_PATH,
      "'/' ya da '//' simgesinden sonra g\u00f6reli yer yolu bekleniyordu."},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such an expression was expected,but
  // the characters specified by the substitution text were encountered instead.
  { ER_EXPECTED_LOC_PATH,
       "Yer yolu bekleniyordu, ancak \u015fu simge saptand\u0131\u003a  {0}"},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such a subexpression was expected,
  // but no more characters were found in the expression.
  { ER_EXPECTED_LOC_PATH_AT_END_EXPR,
       "Yer yolu bekleniyordu, ancak XPath ifadesinin sonu saptand\u0131."},

  // Note to translators:  A location step is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected
  // following the specified characters.
  { ER_EXPECTED_LOC_STEP,
       "'/' ya da '//' simgesinden sonra yer ad\u0131m\u0131 bekleniyordu."},

  // Note to translators:  A node test is part of an XPath expression that is
  // used to test for particular kinds of nodes.  In this case, a node test that
  // consists of an NCName followed by a colon and an asterisk or that consists
  // of a QName was expected, but was not found.
  { ER_EXPECTED_NODE_TEST,
       "NCName:* ya da QName ile e\u015fle\u015fen bir d\u00fc\u011f\u00fcm s\u0131namas\u0131 bekleniyordu."},

  // Note to translators:  A step pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but the specified character was found in the expression instead.
  { ER_EXPECTED_STEP_PATTERN,
       "Ad\u0131m \u00f6r\u00fcnt\u00fcs\u00fc bekleniyordu, ancak '/' saptand\u0131."},

  // Note to translators: A relative path pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but was not found.
  { ER_EXPECTED_REL_PATH_PATTERN,
       "G\u00f6reli yol \u00f6r\u00fcnt\u00fcs\u00fc bekleniyordu."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type boolean.
  { ER_CANT_CONVERT_TO_BOOLEAN,
       "''{0}'' XPath ifadesine ili\u015fkin XPathResult''\u0131n XPathResultType de\u011feri ({1}) bir boole de\u011fere d\u00f6n\u00fc\u015ft\u00fcr\u00fclemez."},

  // Note to translators: Do not translate ANY_UNORDERED_NODE_TYPE and
  // FIRST_ORDERED_NODE_TYPE.
  { ER_CANT_CONVERT_TO_SINGLENODE,
       "''{0}'' XPath ifadesine ili\u015fkin XPathResult''\u0131n XPathResultType de\u011feri ({1}) tek bir d\u00fc\u011f\u00fcme d\u00f6n\u00fc\u015ft\u00fcr\u00fclemez. getSingleNodeValue y\u00f6ntemi yaln\u0131zca ANY_UNORDERED_NODE_TYPE ve FIRST_ORDERED_NODE_TYPE tipleri i\u00e7in ge\u00e7erlidir."},

  // Note to translators: Do not translate UNORDERED_NODE_SNAPSHOT_TYPE and
  // ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_CANT_GET_SNAPSHOT_LENGTH,
       "getSnapshotLength y\u00f6ntemi, ''{0}'' XPath ifadesinin XPathResult''\u0131nda \u00e7a\u011fr\u0131lamaz; y\u00f6nteme ili\u015fkin XPathResultType {1}. Bu y\u00f6ntem yaln\u0131zca UNORDERED_NODE_SNAPSHOT_TYPE ve ORDERED_NODE_SNAPSHOT_TYPE tipleri i\u00e7in ge\u00e7erlidir."},

  { ER_NON_ITERATOR_TYPE,
       "iterateNext y\u00f6ntemi, ''{0}'' XPath ifadesinin XPathResult''\u0131nda \u00e7a\u011fr\u0131lamaz; y\u00f6nteme ili\u015fkin XPathResultType {1}. Bu y\u00f6ntem yaln\u0131zca UNORDERED_NODE_ITERATOR_TYPE ve ORDERED_NODE_ITERATOR_TYPE tipleri i\u00e7in ge\u00e7erlidir."},

  // Note to translators: This message indicates that the document being operated
  // upon changed, so the iterator object that was being used to traverse the
  // document has now become invalid.
  { ER_DOC_MUTATED,
       "Sonu\u00e7 d\u00f6nd\u00fcr\u00fcld\u00fckten sonra belge de\u011fi\u015ftirildi. Yineleyici ge\u00e7ersiz."},

  { ER_INVALID_XPATH_TYPE,
       "Ge\u00e7ersiz XPath tipi ba\u011f\u0131ms\u0131z de\u011fi\u015fkeni: {0}"},

  { ER_EMPTY_XPATH_RESULT,
       "Bo\u015f XPath sonu\u00e7 nesnesi"},

  { ER_INCOMPATIBLE_TYPES,
       "''{0}'' XPath ifadesine ili\u015fkin XPathResult''\u0131n XPathResultType de\u011feri ({1}), belirtilen XPathResultType {2} tipine zorlanamaz."},

  { ER_NULL_RESOLVER,
       "Bo\u015f de\u011ferli \u00f6nek \u00e7\u00f6z\u00fcc\u00fcyle \u00f6nek \u00e7\u00f6z\u00fclemez."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.
  { ER_CANT_CONVERT_TO_STRING,
       "''{0}'' XPath ifadesine ili\u015fkin XPathResult''\u0131n XPathResultType de\u011feri ({1}) bir dizgiye d\u00f6n\u00fc\u015ft\u00fcr\u00fclemez."},

  // Note to translators: Do not translate snapshotItem,
  // UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_NON_SNAPSHOT_TYPE,
       "snapshotItem y\u00f6ntemi, ''{0}'' XPath ifadesinin XPathResult''\u0131nda \u00e7a\u011fr\u0131lamaz; y\u00f6nteme ili\u015fkin XPathResultType {1}. Bu y\u00f6ntem yaln\u0131zca UNORDERED_NODE_SNAPSHOT_TYPE ve ORDERED_NODE_SNAPSHOT_TYPE tipleri i\u00e7in ge\u00e7erlidir."},

  // Note to translators:  XPathEvaluator is a Java interface name.  An
  // XPathEvaluator is created with respect to a particular XML document, and in
  // this case the expression represented by this object was being evaluated with
  // respect to a context node from a different document.
  { ER_WRONG_DOCUMENT,
       "Ba\u011flam d\u00fc\u011f\u00fcm\u00fc, bu XPathEvaluator arabirimine ba\u011flanan belgeye ait de\u011fil."},

  // Note to translators:  The XPath expression cannot be evaluated with respect
  // to this type of node.
  { ER_WRONG_NODETYPE,
       "Ba\u011flam d\u00fc\u011f\u00fcm\u00fc tipi desteklenmiyor."},

  { ER_XPATH_ERROR,
       "XPath i\u00e7inde bilinmeyen hata."},

        { ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER,
                "''{0}'' XPath ifadesine ili\u015fkin XPathResult''\u0131n XPathResultType de\u011feri ({1}) bir say\u0131ya d\u00f6n\u00fc\u015ft\u00fcr\u00fclemez."},

  //BEGIN:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  /** Field ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED                       */

  { ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
       "XMLConstants.FEATURE_SECURE_PROCESSING \u00f6zelli\u011fi true de\u011ferine ayarland\u0131\u011f\u0131nda ''{0}'' eklenti i\u015flevi \u00e7a\u011fr\u0131lamaz."},

  /** Field ER_RESOLVE_VARIABLE_RETURNS_NULL                       */

  { ER_RESOLVE_VARIABLE_RETURNS_NULL,
       "{0} de\u011fi\u015fkenine ili\u015fkin resolveVariable bo\u015f de\u011fer d\u00f6nd\u00fcr\u00fcyor"},

  /** Field ER_UNSUPPORTED_RETURN_TYPE                       */

  { ER_UNSUPPORTED_RETURN_TYPE,
       "Desteklenmeyen d\u00f6n\u00fc\u015f tipi: {0}"},

  /** Field ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL                       */

  { ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL,
       "Kaynak ve/ya da d\u00f6n\u00fc\u015f tipi bo\u015f de\u011ferli olamaz"},

  /** Field ER_ARG_CANNOT_BE_NULL                       */

  { ER_ARG_CANNOT_BE_NULL,
       "{0} ba\u011f\u0131ms\u0131z de\u011fi\u015fkeni bo\u015f de\u011ferli olamaz"},

  /** Field ER_OBJECT_MODEL_NULL                       */

  { ER_OBJECT_MODEL_NULL,
       "{0}#isObjectModelSupported( String objectModel ) objectModel == null ile \u00e7a\u011fr\u0131lamaz"},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_OBJECT_MODEL_EMPTY,
       "{0}#isObjectModelSupported( String objectModel ) objectModel == \"\" ile \u00e7a\u011fr\u0131lamaz"},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_FEATURE_NAME_NULL,
       "Ad\u0131 bo\u015f de\u011ferli bir \u00f6zellik belirleme giri\u015fimi: {0}#setFeature( null, {1})"},

  /** Field ER_FEATURE_UNKNOWN                       */

  { ER_FEATURE_UNKNOWN,
       "Bilinmeyen \"{0}\" \u00f6zelli\u011fini belirleme giri\u015fimi: {1}#setFeature({0},{2})"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_NULL_FEATURE,
       "Bo\u015f de\u011ferli bir adla \u00f6zellik alma giri\u015fimi: {0}#getFeature(null)"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_UNKNOWN_FEATURE,
       "Bilinmeyen \"{0}\" \u00f6zelli\u011fini alma giri\u015fimi: {1}#getFeature({0})"},

  /** Field ER_NULL_XPATH_FUNCTION_RESOLVER                       */

  { ER_NULL_XPATH_FUNCTION_RESOLVER,
       "Bo\u015f de\u011ferli XPathFunctionResolver belirleme giri\u015fimi: {0}#setXPathFunctionResolver(null)"},

  /** Field ER_NULL_XPATH_VARIABLE_RESOLVER                       */

  { ER_NULL_XPATH_VARIABLE_RESOLVER,
       "Bo\u015f de\u011ferli bir XPathVariableResolver belirleme giri\u015fimi: {0}#setXPathVariableResolver(null)"},

  //END:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  // Warnings...

  { WG_LOCALE_NAME_NOT_HANDLED,
      "format-number i\u015flevinde \u00fclke de\u011feri ad\u0131 hen\u00fcz i\u015flenmedi!"},

  { WG_PROPERTY_NOT_SUPPORTED,
      "XSL \u00f6zelli\u011fi desteklenmiyor: {0}"},

  { WG_DONT_DO_ANYTHING_WITH_NS,
      "{1} \u00f6zelli\u011findeki {0} ad alan\u0131yla \u015fu an hi\u00e7bir \u015fey yapmay\u0131n"},

  { WG_SECURITY_EXCEPTION,
      "{0} XSL sistem \u00f6zelli\u011fine eri\u015fme giri\u015fimi s\u0131ras\u0131nda SecurityException"},

  { WG_QUO_NO_LONGER_DEFINED,
      "Eski s\u00f6zdizimi: quo(...) art\u0131k XPath i\u00e7inde tan\u0131mlanmaz."},

  { WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST,
      "nodeTest uygulanmas\u0131 i\u00e7in XPath t\u00fcretilmi\u015f bir nesne gerektirir!"},

  { WG_FUNCTION_TOKEN_NOT_FOUND,
      "\u0130\u015flev simgesi bulunamad\u0131."},

  { WG_COULDNOT_FIND_FUNCTION,
      "\u0130\u015flev bulunamad\u0131: {0}"},

  { WG_CANNOT_MAKE_URL_FROM,
      "Dizgiden URL olu\u015fturulamad\u0131: {0}"},

  { WG_EXPAND_ENTITIES_NOT_SUPPORTED,
      "DTM ayr\u0131\u015ft\u0131r\u0131c\u0131s\u0131 i\u00e7in -E se\u00e7ene\u011fi desteklenmiyor"},

  { WG_ILLEGAL_VARIABLE_REFERENCE,
      "De\u011fi\u015fken i\u00e7in belirtilen VariableReference ba\u011flam d\u0131\u015f\u0131 ya da tan\u0131ms\u0131z!  Ad = {0}"},

  { WG_UNSUPPORTED_ENCODING,
     "Desteklenmeyen kodlama: {0}"},



  // Other miscellaneous text used inside the code...
  { "ui_language", "tr"},
  { "help_language", "tr"},
  { "language", "tr"},
  { "BAD_CODE", "createMessage i\u00e7in kullan\u0131lan de\u011fi\u015ftirge s\u0131n\u0131rlar\u0131n d\u0131\u015f\u0131nda"},
  { "FORMAT_FAILED", "messageFormat \u00e7a\u011fr\u0131s\u0131 s\u0131ras\u0131nda kural d\u0131\u015f\u0131 durum yay\u0131nland\u0131"},
  { "version", ">>>>>>> Xalan S\u00fcr\u00fcm "},
  { "version2", "<<<<<<<"},
  { "yes", "yes"},
  { "line", "Sat\u0131r #"},
  { "column", "Kolon #"},
  { "xsldone", "XSLProcessor: bitti"},
  { "xpath_option", "xpath se\u00e7enekleri: "},
  { "optionIN", "   [-in inputXMLURL]"},
  { "optionSelect", "   [-select xpath ifadesi]"},
  { "optionMatch", "   [-match e\u015fle\u015fme \u00f6r\u00fcnt\u00fcs\u00fc (e\u015fle\u015fme tan\u0131lamas\u0131 i\u00e7in)]"},
  { "optionAnyExpr", "Ya da yaln\u0131zca xpath ifadesi de tan\u0131lama d\u00f6k\u00fcm\u00fc sa\u011flar"},
  { "noParsermsg1", "XSL i\u015flemi ba\u015far\u0131s\u0131z oldu."},
  { "noParsermsg2", "** Ayr\u0131\u015ft\u0131r\u0131c\u0131 bulunamad\u0131 **"},
  { "noParsermsg3", "L\u00fctfen classpath de\u011fi\u015fkeninizi inceleyin."},
  { "noParsermsg4", "Sisteminizde IBM XML Parser for Java arac\u0131 yoksa, \u015fu adresten y\u00fckleyebilirsiniz:"},
  { "noParsermsg5", "IBM's AlphaWorks: http://www.alphaworks.ibm.com/formula/xml"},
  { "gtone", ">1" },
  { "zero", "0" },
  { "one", "1" },
  { "two" , "2" },
  { "three", "3" }

  };
  }


  // ================= INFRASTRUCTURE ======================

  /** Field BAD_CODE          */
  public static final String BAD_CODE = "HATALI_KOD";

  /** Field FORMAT_FAILED          */
  public static final String FORMAT_FAILED = "B\u0130\u00c7\u0130MLEME_BA\u015eARISIZ";

  /** Field ERROR_RESOURCES          */
  public static final String ERROR_RESOURCES =
    "org.apache.xpath.res.XPATHErrorResources";

  /** Field ERROR_STRING          */
  public static final String ERROR_STRING = "#hata";

  /** Field ERROR_HEADER          */
  public static final String ERROR_HEADER = "Hata: ";

  /** Field WARNING_HEADER          */
  public static final String WARNING_HEADER = "Uyar\u0131: ";

  /** Field XSL_HEADER          */
  public static final String XSL_HEADER = "XSL ";

  /** Field XML_HEADER          */
  public static final String XML_HEADER = "XML ";

  /** Field QUERY_HEADER          */
  public static final String QUERY_HEADER = "\u00d6R\u00dcNT\u00dc ";


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
                new Locale("tr", "TR"));
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
