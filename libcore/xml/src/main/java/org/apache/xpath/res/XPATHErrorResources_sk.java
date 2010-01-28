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
 * $Id: XPATHErrorResources_sk.java 468655 2006-10-28 07:12:06Z minchau $
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
public class XPATHErrorResources_sk extends ListResourceBundle
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

  { ER_CURRENT_NOT_ALLOWED_IN_MATCH, "Funkcia current () nie je povolen\u00e1 v porovn\u00e1vacom vzore!" },

  { ER_CURRENT_TAKES_NO_ARGS, "Funkcia current () nepr\u00edma argumenty!" },

  { ER_DOCUMENT_REPLACED,
      "Implement\u00e1cia funkcie document() bola nahraden\u00e1 org.apache.xalan.xslt.FuncDocument!"},

  { ER_CONTEXT_HAS_NO_OWNERDOC,
      "kontext nem\u00e1 dokument vlastn\u00edka!"},

  { ER_LOCALNAME_HAS_TOO_MANY_ARGS,
      "local-name() m\u00e1 prive\u013ea argumentov."},

  { ER_NAMESPACEURI_HAS_TOO_MANY_ARGS,
      "namespace-uri() m\u00e1 prive\u013ea argumentov."},

  { ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS,
      "normalize-space() m\u00e1 prive\u013ea argumentov."},

  { ER_NUMBER_HAS_TOO_MANY_ARGS,
      "number() m\u00e1 prive\u013ea argumentov."},

  { ER_NAME_HAS_TOO_MANY_ARGS,
     "name() m\u00e1 prive\u013ea argumentov."},

  { ER_STRING_HAS_TOO_MANY_ARGS,
      "string() m\u00e1 prive\u013ea argumentov"},

  { ER_STRINGLENGTH_HAS_TOO_MANY_ARGS,
      "string-length() m\u00e1 prive\u013ea argumentov"},

  { ER_TRANSLATE_TAKES_3_ARGS,
      "Funkcia translate() pr\u00edma tri argumenty!"},

  { ER_UNPARSEDENTITYURI_TAKES_1_ARG,
      "Funkcia unparsed-entity-uri by mala prija\u0165 jeden argument!"},

  { ER_NAMESPACEAXIS_NOT_IMPLEMENTED,
      "osi n\u00e1zvov\u00fdch priestorov e\u0161te nie s\u00fa implementovan\u00e9!"},

  { ER_UNKNOWN_AXIS,
     "nezn\u00e1ma os: {0}"},

  { ER_UNKNOWN_MATCH_OPERATION,
     "nezn\u00e1ma porovn\u00e1vacia oper\u00e1cia!"},

  { ER_INCORRECT_ARG_LENGTH,
      "Testovanie uzla arg length of processing-instruction() je nespr\u00e1vne!"},

  { ER_CANT_CONVERT_TO_NUMBER,
      "Nie je mo\u017en\u00e9 konvertova\u0165 {0} na \u010d\u00edslo"},

  { ER_CANT_CONVERT_TO_NODELIST,
      "Nie je mo\u017en\u00e9 konvertova\u0165 {0} na NodeList!"},

  { ER_CANT_CONVERT_TO_MUTABLENODELIST,
      "Nie je mo\u017en\u00e9 konvertova\u0165 {0} na NodeSetDTM!"},

  { ER_CANT_CONVERT_TO_TYPE,
      "Nie je mo\u017en\u00e1 konverzia {0} na typ#{1}"},

  { ER_EXPECTED_MATCH_PATTERN,
      "O\u010dak\u00e1van\u00fd porovn\u00e1vac\u00ed vzor v getMatchScore!"},

  { ER_COULDNOT_GET_VAR_NAMED,
      "Nie je mo\u017en\u00e9 dosiahnu\u0165 premenn\u00fa s n\u00e1zvom {0}"},

  { ER_UNKNOWN_OPCODE,
     "CHYBA! Nezn\u00e1my k\u00f3d op: {0}"},

  { ER_EXTRA_ILLEGAL_TOKENS,
     "Nadbyto\u010dn\u00e9 neplatn\u00e9 symboly: {0}"},


  { ER_EXPECTED_DOUBLE_QUOTE,
      "Nespr\u00e1vny liter\u00e1l... o\u010dak\u00e1van\u00e1 dvojit\u00e1 cit\u00e1cia!"},

  { ER_EXPECTED_SINGLE_QUOTE,
      "Nespr\u00e1vny liter\u00e1l... o\u010dak\u00e1van\u00e1 jedin\u00e1 cit\u00e1cia!"},

  { ER_EMPTY_EXPRESSION,
     "Pr\u00e1zdny v\u00fdraz!"},

  { ER_EXPECTED_BUT_FOUND,
     "O\u010dak\u00e1vala sa {0}, ale bola n\u00e1jden\u00e1: {1}"},

  { ER_INCORRECT_PROGRAMMER_ASSERTION,
      "Program\u00e1torsk\u00e9 vyjadrenie je nespr\u00e1vne! - {0}"},

  { ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,
      "argument boolean(...) u\u017e nie je volite\u013en\u00fd s konceptom 19990709 XPath."},

  { ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,
      "N\u00e1jdene ',' ale \u017eiaden predch\u00e1dzaj\u00faci argument!"},

  { ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
      "N\u00e1jden\u00e9 ',' ale \u017eiaden nasleduj\u00faci argument!"},

  { ER_PREDICATE_ILLEGAL_SYNTAX,
      "'..[predicate]' alebo '.[predicate]' je nespr\u00e1vna syntax.  Pou\u017eite namiesto toho 'self::node()[predicate]'."},

  { ER_ILLEGAL_AXIS_NAME,
     "Neplatn\u00fd n\u00e1zov osi: {0}"},

  { ER_UNKNOWN_NODETYPE,
     "Nezn\u00e1my typ uzla: {0}"},

  { ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
      "Vzorov\u00fd liter\u00e1l ({0}) potrebuje by\u0165 citovan\u00fd!"},

  { ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
      "{0} nem\u00f4\u017ee by\u0165 form\u00e1tovan\u00e9 na \u010d\u00edslo!"},

  { ER_COULDNOT_CREATE_XMLPROCESSORLIAISON,
      "Nebolo mo\u017en\u00e9 vytvori\u0165 vz\u0165ah XML TransformerFactory: {0}"},

  { ER_DIDNOT_FIND_XPATH_SELECT_EXP,
      "Chyba! Nena\u0161lo sa vyjadrenie v\u00fdberu xpath (-select)."},

  { ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH,
      "CHYBA! Nebolo mo\u017en\u00e9 n\u00e1js\u0165 ENDOP po OP_LOCATIONPATH"},

  { ER_ERROR_OCCURED,
     "Vyskytla sa chyba!"},

  { ER_ILLEGAL_VARIABLE_REFERENCE,
      "VariableReference bol dan\u00fd pre premenn\u00fa mimo kontext, alebo bez defin\u00edcie!  N\u00e1zov = {0}"},

  { ER_AXES_NOT_ALLOWED,
      "Len potomok:: atrib\u00fat:: osi s\u00fa povolen\u00e9 v zhodn\u00fdch vzoroch!  Chybn\u00e9 osi = {0}"},

  { ER_KEY_HAS_TOO_MANY_ARGS,
      "key() m\u00e1 nespr\u00e1vny po\u010det argumentov."},

  { ER_COUNT_TAKES_1_ARG,
      "Funkcia count by mala prija\u0165 jeden argument!"},

  { ER_COULDNOT_FIND_FUNCTION,
     "Nebolo mo\u017en\u00e9 n\u00e1js\u0165 funkciu: {0}"},

  { ER_UNSUPPORTED_ENCODING,
     "Nepodporovan\u00e9 k\u00f3dovanie: {0}"},

  { ER_PROBLEM_IN_DTM_NEXTSIBLING,
      "Vyskytol sa probl\u00e9m v DTM v getNextSibling... pokus o obnovu"},

  { ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL,
      "Chyba program\u00e1tora: EmptyNodeList nebolo mo\u017en\u00e9 zap\u00edsa\u0165."},

  { ER_SETDOMFACTORY_NOT_SUPPORTED,
      "setDOMFactory nie je podporovan\u00e9 XPathContext!"},

  { ER_PREFIX_MUST_RESOLVE,
      "Predpona sa mus\u00ed rozl\u00ed\u0161i\u0165 do n\u00e1zvov\u00e9ho priestoru: {0}"},

  { ER_PARSE_NOT_SUPPORTED,
      "anal\u00fdza (InputSource source) nie je podporovan\u00e1 XPathContext! Nie je mo\u017en\u00e9 otvori\u0165 {0}"},

  { ER_SAX_API_NOT_HANDLED,
      "SAX API znaky(char ch[]... nie s\u00fa spracovan\u00e9 DTM!"},

  { ER_IGNORABLE_WHITESPACE_NOT_HANDLED,
      "ignorableWhitespace(char ch[]... nie s\u00fa spracovan\u00e9 DTM!"},

  { ER_DTM_CANNOT_HANDLE_NODES,
      "DTMLiaison nem\u00f4\u017ee spracova\u0165 uzly typu {0}"},

  { ER_XERCES_CANNOT_HANDLE_NODES,
      "DOM2Helper nem\u00f4\u017ee spracova\u0165 uzly typu {0}"},

  { ER_XERCES_PARSE_ERROR_DETAILS,
      "Chyba DOM2Helper.parse: SystemID - {0} riadok - {1}"},

  { ER_XERCES_PARSE_ERROR,
     "chyba DOM2Helper.parse"},

  { ER_INVALID_UTF16_SURROGATE,
      "Bolo zisten\u00e9 neplatn\u00e9 nahradenie UTF-16: {0} ?"},

  { ER_OIERROR,
     "chyba IO"},

  { ER_CANNOT_CREATE_URL,
     "Nie je mo\u017en\u00e9 vytvori\u0165 url pre: {0}"},

  { ER_XPATH_READOBJECT,
     "V XPath.readObject: {0}"},

  { ER_FUNCTION_TOKEN_NOT_FOUND,
      "nebol n\u00e1jden\u00fd symbol funkcie."},

  { ER_CANNOT_DEAL_XPATH_TYPE,
       "Nie je mo\u017en\u00e9 pracova\u0165 s typom XPath: {0}"},

  { ER_NODESET_NOT_MUTABLE,
       "Tento NodeSet je nest\u00e1ly"},

  { ER_NODESETDTM_NOT_MUTABLE,
       "Tento NodeSetDTM nie je nest\u00e1ly"},

  { ER_VAR_NOT_RESOLVABLE,
        "Premenn\u00fa nie je mo\u017en\u00e9 rozl\u00ed\u0161i\u0165: {0}"},

  { ER_NULL_ERROR_HANDLER,
        "Nulov\u00fd chybov\u00fd manipula\u010dn\u00fd program"},

  { ER_PROG_ASSERT_UNKNOWN_OPCODE,
       "Tvrdenie program\u00e1tora: nezn\u00e1my opcode: {0}"},

  { ER_ZERO_OR_ONE,
       "0, alebo 1"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "rtf() nie je podporovan\u00fd XRTreeFragSelectWrapper"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "asNodeIterator() nie je podporovan\u00fd XRTreeFragSelectWrapper"},

   { ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper nepodporuje detach()"},

   { ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper nepodporuje num()"},

   { ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper nepodporuje xstr()"},

   { ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper nepodporuje str()"},

  { ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,
       "fsb() nie je podporovan\u00fd pre XStringForChars"},

  { ER_COULD_NOT_FIND_VAR,
      "Nebolo mo\u017en\u00e9 n\u00e1js\u0165 premenn\u00fa s n\u00e1zvom {0}"},

  { ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,
      "XStringForChars nem\u00f4\u017ee ako argument prija\u0165 re\u0165azec"},

  { ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,
      "Argument FastStringBuffer nem\u00f4\u017ee by\u0165 nulov\u00fd"},

  { ER_TWO_OR_THREE,
       "2, alebo 3"},

  { ER_VARIABLE_ACCESSED_BEFORE_BIND,
       "Premenn\u00e1 bola z\u00edskan\u00e1 sk\u00f4r, ne\u017e bola viazan\u00e1!"},

  { ER_FSB_CANNOT_TAKE_STRING,
       "XStringForFSB nem\u00f4\u017ee pova\u017eova\u0165 re\u0165azec za argument!"},

  { ER_SETTING_WALKER_ROOT_TO_NULL,
       "\n !!!! Chyba! Nastavenie root of a walker na null!!!"},

  { ER_NODESETDTM_CANNOT_ITERATE,
       "Tento NodeSetDTM sa nem\u00f4\u017ee iterova\u0165 na predch\u00e1dzaj\u00faci uzol!"},

  { ER_NODESET_CANNOT_ITERATE,
       "Tento NodeSet sa nem\u00f4\u017ee iterova\u0165 na predch\u00e1dzaj\u00faci uzol!"},

  { ER_NODESETDTM_CANNOT_INDEX,
       "Tento NodeSetDTM nem\u00f4\u017ee vykon\u00e1va\u0165 funkcie indexovania alebo po\u010d\u00edtania!"},

  { ER_NODESET_CANNOT_INDEX,
       "Tento NodeSet nem\u00f4\u017ee vykon\u00e1va\u0165 funkcie indexovania alebo po\u010d\u00edtania!"},

  { ER_CANNOT_CALL_SETSHOULDCACHENODE,
       "Nie je mo\u017en\u00e9 vola\u0165 setShouldCacheNodes po volan\u00ed nextNode!"},

  { ER_ONLY_ALLOWS,
       "{0} povo\u013eulje iba {1} argumentov"},

  { ER_UNKNOWN_STEP,
       "Tvrdenie program\u00e1tora v getNextStepPos: nezn\u00e1my stepType: {0}"},

  //Note to translators:  A relative location path is a form of XPath expression.
  // The message indicates that such an expression was expected following the
  // characters '/' or '//', but was not found.
  { ER_EXPECTED_REL_LOC_PATH,
      "Po symbole '/' alebo '//' sa o\u010dak\u00e1vala cesta relat\u00edvneho umiestnenia."},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such an expression was expected,but
  // the characters specified by the substitution text were encountered instead.
  { ER_EXPECTED_LOC_PATH,
       "O\u010dak\u00e1vala sa cesta umiestnenia, ale na\u0161iel sa tento symbol \u003a {0}"},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such a subexpression was expected,
  // but no more characters were found in the expression.
  { ER_EXPECTED_LOC_PATH_AT_END_EXPR,
       "Bola o\u010dak\u00e1van\u00e1 cesta umiestnenia, ale namiesto nej bol n\u00e1jden\u00fd koniec v\u00fdrazu XPath."},

  // Note to translators:  A location step is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected
  // following the specified characters.
  { ER_EXPECTED_LOC_STEP,
       "Po symbole '/' alebo '//' sa o\u010dak\u00e1val krok umiestnenia."},

  // Note to translators:  A node test is part of an XPath expression that is
  // used to test for particular kinds of nodes.  In this case, a node test that
  // consists of an NCName followed by a colon and an asterisk or that consists
  // of a QName was expected, but was not found.
  { ER_EXPECTED_NODE_TEST,
       "O\u010dak\u00e1val sa test uzlov, ktor\u00fd sa zhoduje bu\u010f s NCName:* alebo s QName."},

  // Note to translators:  A step pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but the specified character was found in the expression instead.
  { ER_EXPECTED_STEP_PATTERN,
       "O\u010dak\u00e1val sa vzor kroku, ale bol zaznamenan\u00fd '/'."},

  // Note to translators: A relative path pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but was not found.
  { ER_EXPECTED_REL_PATH_PATTERN,
       "O\u010dak\u00e1val sa vzor relat\u00edvnej cesty."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type boolean.
  { ER_CANT_CONVERT_TO_BOOLEAN,
       "XPathResult z XPath v\u00fdrazu ''{0}'' m\u00e1 XPathResultType {1}, ktor\u00fd sa ned\u00e1 skonvertova\u0165 do boolovsk\u00e9ho v\u00fdrazu."},

  // Note to translators: Do not translate ANY_UNORDERED_NODE_TYPE and
  // FIRST_ORDERED_NODE_TYPE.
  { ER_CANT_CONVERT_TO_SINGLENODE,
       "XPathResult z XPath v\u00fdrazu ''{0}'' m\u00e1 XPathResultType {1}, ktor\u00fd sa ned\u00e1 skonvertova\u0165 do jedn\u00e9ho uzla. Met\u00f3da getSingleNodeValue sa pou\u017e\u00edva iba pre typy ANY_UNORDERED_NODE_TYPE a FIRST_ORDERED_NODE_TYPE."},

  // Note to translators: Do not translate UNORDERED_NODE_SNAPSHOT_TYPE and
  // ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_CANT_GET_SNAPSHOT_LENGTH,
       "Met\u00f3da getSnapshotLength sa nem\u00f4\u017ee vola\u0165 na XPathResult z XPath v\u00fdrazu ''{0}'', preto\u017ee jeho XPathResultType je {1}. T\u00e1to met\u00f3da sa pou\u017eije iba pre typy UNORDERED_NODE_SNAPSHOT_TYPE a ORDERED_NODE_SNAPSHOT_TYPE."},

  { ER_NON_ITERATOR_TYPE,
       "Met\u00f3da iterateNext sa nem\u00f4\u017ee vola\u0165 na XPathResult z XPath v\u00fdrazu ''{0}'', preto\u017ee jej XPathResultType je {1}. T\u00e1to met\u00f3da sa pou\u017eije iba pre typy UNORDERED_NODE_ITERATOR_TYPE a ORDERED_NODE_ITERATOR_TYPE."},

  // Note to translators: This message indicates that the document being operated
  // upon changed, so the iterator object that was being used to traverse the
  // document has now become invalid.
  { ER_DOC_MUTATED,
       "Dokument sa od vr\u00e1tenia v\u00fdsledku zmenil. Iter\u00e1tor je neplatn\u00fd."},

  { ER_INVALID_XPATH_TYPE,
       "Neplatn\u00fd argument typu XPath: {0}"},

  { ER_EMPTY_XPATH_RESULT,
       "Pr\u00e1zdny objekt v\u00fdsledku XPath"},

  { ER_INCOMPATIBLE_TYPES,
       "XPathResult z XPath v\u00fdrazu ''{0}'' m\u00e1 XPathResultType {1}, ktor\u00fd sa ned\u00e1 stla\u010di\u0165 do \u0161pecifikovan\u00e9ho XPathResultType {2}."},

  { ER_NULL_RESOLVER,
       "Nie je mo\u017en\u00e9 rozl\u00ed\u0161i\u0165 predponu s rozli\u0161ova\u010dom nulovej predpony."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.
  { ER_CANT_CONVERT_TO_STRING,
       "XPathResult z XPath v\u00fdrazu ''{0}'' m\u00e1 XPathResultType {1}, ktor\u00fd sa ned\u00e1 skonvertova\u0165 na re\u0165azec."},

  // Note to translators: Do not translate snapshotItem,
  // UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_NON_SNAPSHOT_TYPE,
       "Met\u00f3da snapshotItem sa nem\u00f4\u017ee vola\u0165 na XPathResult z XPath v\u00fdrazu ''{0}'', preto\u017ee jej XPathResultType je {1}. T\u00e1to met\u00f3da sa pou\u017eije iba pre typy UNORDERED_NODE_SNAPSHOT_TYPE a ORDERED_NODE_SNAPSHOT_TYPE."},

  // Note to translators:  XPathEvaluator is a Java interface name.  An
  // XPathEvaluator is created with respect to a particular XML document, and in
  // this case the expression represented by this object was being evaluated with
  // respect to a context node from a different document.
  { ER_WRONG_DOCUMENT,
       "Uzol kontextu nepatr\u00ed k dokumentu, ktor\u00fd je viazan\u00fd na tento XPathEvaluator."},

  // Note to translators:  The XPath expression cannot be evaluated with respect
  // to this type of node.
  { ER_WRONG_NODETYPE,
       "Typ uzla kontextu nie je podporovan\u00fd."},

  { ER_XPATH_ERROR,
       "Nezn\u00e1ma chyba v XPath."},

        { ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER,
                "XPathResult z XPath v\u00fdrazu ''{0}'' m\u00e1 XPathResultType {1}, ktor\u00fd sa ned\u00e1 skonvertova\u0165 na \u010d\u00edslo"},

  //BEGIN:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  /** Field ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED                       */

  { ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
       "Funkcia roz\u0161\u00edrenia: ''{0}'' sa ned\u00e1 vyvola\u0165, ke\u010f je funkcia XMLConstants.FEATURE_SECURE_PROCESSING nastaven\u00e1 na hodnotu true."},

  /** Field ER_RESOLVE_VARIABLE_RETURNS_NULL                       */

  { ER_RESOLVE_VARIABLE_RETURNS_NULL,
       "resolveVariable pre premenn\u00fa {0} vracia hodnotu null"},

  /** Field ER_UNSUPPORTED_RETURN_TYPE                       */

  { ER_UNSUPPORTED_RETURN_TYPE,
       "Nepodporovan\u00fd typ n\u00e1vratu : {0}"},

  /** Field ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL                       */

  { ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL,
       "Zdroj a/alebo typ n\u00e1vratu nem\u00f4\u017ee ma\u0165 hodnotu null"},

  /** Field ER_ARG_CANNOT_BE_NULL                       */

  { ER_ARG_CANNOT_BE_NULL,
       "Argument {0} nem\u00f4\u017ee ma\u0165 hodnotu null"},

  /** Field ER_OBJECT_MODEL_NULL                       */

  { ER_OBJECT_MODEL_NULL,
       "{0}#isObjectModelSupported( Re\u0165azec objectModel ) nem\u00f4\u017ee by\u0165 volan\u00fd s objectModel == null"},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_OBJECT_MODEL_EMPTY,
       "{0}#isObjectModelSupported( Re\u0165azec objectModel ) nem\u00f4\u017ee by\u0165 volan\u00fd s objectModel == \"\""},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_FEATURE_NAME_NULL,
       "Prebieha pokus o nastavenie funkcie s n\u00e1zvom null: {0}#setFeature( null, {1})"},

  /** Field ER_FEATURE_UNKNOWN                       */

  { ER_FEATURE_UNKNOWN,
       "Prebieha pokus o nastavenie nezn\u00e1mej funkcie \"{0}\":{1}#setFeature({0},{2})"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_NULL_FEATURE,
       "Prebieha pokus o z\u00edskanie funkcie s n\u00e1zvom null: {0}#getFeature(null)"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_UNKNOWN_FEATURE,
       "Prebieha pokus o z\u00edskanie nezn\u00e1mej funkcie \"{0}\":{1}#getFeature({0})"},

  /** Field ER_NULL_XPATH_FUNCTION_RESOLVER                       */

  { ER_NULL_XPATH_FUNCTION_RESOLVER,
       "Prebieha pokus o nastavenie hodnoty null pre XPathFunctionResolver:{0}#setXPathFunctionResolver(null)"},

  /** Field ER_NULL_XPATH_VARIABLE_RESOLVER                       */

  { ER_NULL_XPATH_VARIABLE_RESOLVER,
       "Prebieha pokus o nastavenie hodnoty null pre XPathVariableResolver:{0}#setXPathVariableResolver(null)"},

  //END:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  // Warnings...

  { WG_LOCALE_NAME_NOT_HANDLED,
      "n\u00e1zov umiestnenia vo funkcii format-number e\u0161te nebol spracovan\u00fd!"},

  { WG_PROPERTY_NOT_SUPPORTED,
      "Vlastn\u00edctvo XSL nie je podporovan\u00e9: {0}"},

  { WG_DONT_DO_ANYTHING_WITH_NS,
      "Nerobte moment\u00e1lne ni\u010d s n\u00e1zvov\u00fdm priestorom {0} vo vlastn\u00edctve: {1}"},

  { WG_SECURITY_EXCEPTION,
      "SecurityException po\u010das pokusu o pr\u00edstup do syst\u00e9mov\u00e9ho vlastn\u00edctva XSL: {0}"},

  { WG_QUO_NO_LONGER_DEFINED,
      "Star\u00e1 syntax: quo(...) u\u017e nie je v XPath definovan\u00e9."},

  { WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST,
      "XPath potrebuje odvoden\u00fd objekt na implement\u00e1ciu nodeTest!"},

  { WG_FUNCTION_TOKEN_NOT_FOUND,
      "nebol n\u00e1jden\u00fd symbol funkcie."},

  { WG_COULDNOT_FIND_FUNCTION,
      "Nebolo mo\u017en\u00e9 n\u00e1js\u0165 funkciu: {0}"},

  { WG_CANNOT_MAKE_URL_FROM,
      "Nie je mo\u017en\u00e9 vytvori\u0165 URL z: {0}"},

  { WG_EXPAND_ENTITIES_NOT_SUPPORTED,
      "-E vo\u013eba nie je podporovan\u00e1 syntaktick\u00fdm analyz\u00e1torom DTM"},

  { WG_ILLEGAL_VARIABLE_REFERENCE,
      "VariableReference bol dan\u00fd pre premenn\u00fa mimo kontext, alebo bez defin\u00edcie!  N\u00e1zov = {0}"},

  { WG_UNSUPPORTED_ENCODING,
     "Nepodporovan\u00e9 k\u00f3dovanie: {0}"},



  // Other miscellaneous text used inside the code...
  { "ui_language", "en"},
  { "help_language", "en"},
  { "language", "en"},
  { "BAD_CODE", "Parameter na createMessage bol mimo ohrani\u010denia"},
  { "FORMAT_FAILED", "V\u00fdnimka po\u010das volania messageFormat"},
  { "version", ">>>>>>> Verzia Xalan "},
  { "version2", "<<<<<<<"},
  { "yes", "\u00e1no"},
  { "line", "Riadok #"},
  { "column", "St\u013apec #"},
  { "xsldone", "XSLProcessor: vykonan\u00e9"},
  { "xpath_option", "vo\u013eby xpath: "},
  { "optionIN", "   [-in inputXMLURL]"},
  { "optionSelect", "   [-select vyjadrenie xpath]"},
  { "optionMatch", "   [-match porovn\u00e1vac\u00ed vzor (pre diagnostiku zhody)]"},
  { "optionAnyExpr", "Alebo len vyjadrenie xpath vykon\u00e1 v\u00fdpis pam\u00e4te diagnostiky"},
  { "noParsermsg1", "Proces XSL nebol \u00faspe\u0161n\u00fd."},
  { "noParsermsg2", "** Nebolo mo\u017en\u00e9 n\u00e1js\u0165 syntaktick\u00fd analyz\u00e1tor **"},
  { "noParsermsg3", "Skontroluje, pros\u00edm, svoju classpath."},
  { "noParsermsg4", "Ak nem\u00e1te Syntaktick\u00fd analyz\u00e1tor XML pre jazyk Java od firmy IBM, m\u00f4\u017eete si ho stiahnu\u0165 z"},
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
  public static final String BAD_CODE = "BAD_CODE";

  /** Field FORMAT_FAILED          */
  public static final String FORMAT_FAILED = "FORMAT_FAILED";

  /** Field ERROR_RESOURCES          */
  public static final String ERROR_RESOURCES =
    "org.apache.xpath.res.XPATHErrorResources";

  /** Field ERROR_STRING          */
  public static final String ERROR_STRING = "#error";

  /** Field ERROR_HEADER          */
  public static final String ERROR_HEADER = "Chyba: ";

  /** Field WARNING_HEADER          */
  public static final String WARNING_HEADER = "Upozornenie: ";

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
                new Locale("en", "US"));
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
