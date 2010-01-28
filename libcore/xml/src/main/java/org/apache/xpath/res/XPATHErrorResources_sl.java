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
 * $Id: XPATHErrorResources_sl.java,v 1.29 2005/02/09 21:44:08 zongaro Exp $
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
public class XPATHErrorResources_sl extends ListResourceBundle
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

  { ER_CURRENT_NOT_ALLOWED_IN_MATCH, "Funkcija current() v primerjalnem vzorcu ni dovoljena!" },

  { ER_CURRENT_TAKES_NO_ARGS, "Funkcija current() ne sprejema argumentov!" },

  { ER_DOCUMENT_REPLACED,
      "Implementacija funkcije document() je bila nadome\u0161\u010dena z org.apache.xalan.xslt.FuncDocument!"},

  { ER_CONTEXT_HAS_NO_OWNERDOC,
      "Kontekst ne vsebuje lastni\u0161kega dokumenta!"},

  { ER_LOCALNAME_HAS_TOO_MANY_ARGS,
      "local-name() ima preve\u010d argumentov."},

  { ER_NAMESPACEURI_HAS_TOO_MANY_ARGS,
      "namespace-uri() ima preve\u010d argumentov."},

  { ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS,
      "normalize-space() ima preve\u010d argumentov."},

  { ER_NUMBER_HAS_TOO_MANY_ARGS,
      "number() ima preve\u010d argumentov."},

  { ER_NAME_HAS_TOO_MANY_ARGS,
     "name() ima preve\u010d argumentov."},

  { ER_STRING_HAS_TOO_MANY_ARGS,
      "string() ima preve\u010d argumentov."},

  { ER_STRINGLENGTH_HAS_TOO_MANY_ARGS,
      "string-length() ima preve\u010d argumentov."},

  { ER_TRANSLATE_TAKES_3_ARGS,
      "Funkcija translate() sprejme tri argumente!"},

  { ER_UNPARSEDENTITYURI_TAKES_1_ARG,
      "Funkcija unparsed-entity-uri bi morala vsebovati en argument!"},

  { ER_NAMESPACEAXIS_NOT_IMPLEMENTED,
      "Os imenskega prostora \u0161e ni implementirana!"},

  { ER_UNKNOWN_AXIS,
     "neznana os: {0}"},

  { ER_UNKNOWN_MATCH_OPERATION,
     "neznana operacija ujemanja!"},

  { ER_INCORRECT_ARG_LENGTH,
      "Dol\u017eina argumenta pri preskusu vozli\u0161\u010da s processing-instruction() ni pravilna!"},

  { ER_CANT_CONVERT_TO_NUMBER,
      "{0} ni mogo\u010de pretvoriti v \u0161tevilko"},

  { ER_CANT_CONVERT_TO_NODELIST,
      "{0} ni mogo\u010de pretvoriti v seznam vozli\u0161\u010d (NodeList)"},

  { ER_CANT_CONVERT_TO_MUTABLENODELIST,
      "{0} ni mogo\u010de pretvoriti v NodeSetDTM"},

  { ER_CANT_CONVERT_TO_TYPE,
      "{0} ni mogo\u010de pretvoriti v type#{1}"},

  { ER_EXPECTED_MATCH_PATTERN,
      "Pri\u010dakovan primerjalni vzorec v getMatchScore!"},

  { ER_COULDNOT_GET_VAR_NAMED,
      "Nisem na\u0161el predloge z imenom {0}"},

  { ER_UNKNOWN_OPCODE,
     "NAPAKA! Neznana op. koda: {0}"},

  { ER_EXTRA_ILLEGAL_TOKENS,
     "Dodatni neveljavni \u017eetoni: {0}"},


  { ER_EXPECTED_DOUBLE_QUOTE,
      "Napa\u010dno postavljena dobesedna navedba... pri\u010dakovani dvojni narekovaji!"},

  { ER_EXPECTED_SINGLE_QUOTE,
      "Napa\u010dno postavljena dobesedna navedba... pri\u010dakovani enojni narekovaji!"},

  { ER_EMPTY_EXPRESSION,
     "Prazen izraz!"},

  { ER_EXPECTED_BUT_FOUND,
     "Pri\u010dakovano {0}, najdeno: {1}"},

  { ER_INCORRECT_PROGRAMMER_ASSERTION,
      "Programerjeva predpostavka ni pravilna! - {0}"},

  { ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,
      "Argument logi\u010dne vrednosti(...) ni ve\u010d izbiren z osnutkom 19990709 XPath."},

  { ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,
      "Najdeno ',' vendar ni predhodnih argumentov!"},

  { ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
      "Najdeno ',' vendar ni slede\u010dih argumentov!"},

  { ER_PREDICATE_ILLEGAL_SYNTAX,
      "'..[predicate]' ali '.[predicate]' je neveljavna sintaksa.  Namesto tega uporabite 'self::node()[predicate]'."},

  { ER_ILLEGAL_AXIS_NAME,
     "Neveljavno ime osi: {0}"},

  { ER_UNKNOWN_NODETYPE,
     "Neveljavni tip vozli\u0161\u010da: {0}"},

  { ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
      "Navedek vzorca ({0}) mora biti v navednicah!"},

  { ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
      "{0} ni mogo\u010de oblikovati v \u0161tevilko!"},

  { ER_COULDNOT_CREATE_XMLPROCESSORLIAISON,
      "Ne morem ustvariti zveze XML TransformerFactory: {0}"},

  { ER_DIDNOT_FIND_XPATH_SELECT_EXP,
      "Napaka! Ne najdem izbirnega izraza xpath (-select)."},

  { ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH,
      "NAPAKA! Ne najdem ENDOP po OP_LOCATIONPATH"},

  { ER_ERROR_OCCURED,
     "Pri\u0161lo je do napake!"},

  { ER_ILLEGAL_VARIABLE_REFERENCE,
      "Dani VariableReference je izven konteksta ali brez definicije!  Ime = {0}"},

  { ER_AXES_NOT_ALLOWED,
      "V primerjalnih vzorcih so dovoljene samo osi podrejenega:: in atributa::!  Sporne osi = {0}"},

  { ER_KEY_HAS_TOO_MANY_ARGS,
      "key()ima nepravilno \u0161tevilo argumentov."},

  { ER_COUNT_TAKES_1_ARG,
      "\u0160tevna funkcija zahteva en argument!"},

  { ER_COULDNOT_FIND_FUNCTION,
     "Ne najdem funkcije: {0}"},

  { ER_UNSUPPORTED_ENCODING,
     "Nepodprto kodiranje: {0}"},

  { ER_PROBLEM_IN_DTM_NEXTSIBLING,
      "Pri\u0161lo je do te\u017eave v DTM pri getNextSibling... poskus obnovitve"},

  { ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL,
      "Programerska napaka: pisanje v EmptyNodeList ni mogo\u010de."},

  { ER_SETDOMFACTORY_NOT_SUPPORTED,
      "setDOMFactory v XPathContext ni podprt!"},

  { ER_PREFIX_MUST_RESOLVE,
      "Predpona se mora razre\u0161iti v imenski prostor: {0}"},

  { ER_PARSE_NOT_SUPPORTED,
      "Raz\u010dlenitev (vir InputSource) v XPathContext ni podprta! Ne morem odpreti {0}"},

  { ER_SAX_API_NOT_HANDLED,
      "Znaki SAX API(znaka ch[]... ne obravnava DTM!"},

  { ER_IGNORABLE_WHITESPACE_NOT_HANDLED,
      "ignorableWhitespace(znaka ch[]... ne obravnava DTM!"},

  { ER_DTM_CANNOT_HANDLE_NODES,
      "DTMLiaison ne more upravljati z vozli\u0161\u010di tipa {0}"},

  { ER_XERCES_CANNOT_HANDLE_NODES,
      "DOM2Helper ne more upravljati z vozli\u0161\u010di tipa {0}"},

  { ER_XERCES_PARSE_ERROR_DETAILS,
      "Napaka DOM2Helper.parse: ID sistema - {0} vrstica - {1}"},

  { ER_XERCES_PARSE_ERROR,
     "Napaka DOM2Helper.parse"},

  { ER_INVALID_UTF16_SURROGATE,
      "Zaznan neveljaven nadomestek UTF-16: {0} ?"},

  { ER_OIERROR,
     "Napaka V/I"},

  { ER_CANNOT_CREATE_URL,
     "Ne morem ustvariti naslova URL za: {0}"},

  { ER_XPATH_READOBJECT,
     "V XPath.readObject: {0}"},

  { ER_FUNCTION_TOKEN_NOT_FOUND,
      "ne najdem \u017eetona funkcije."},

  { ER_CANNOT_DEAL_XPATH_TYPE,
       "Ne morem ravnati z tipom XPath: {0}"},

  { ER_NODESET_NOT_MUTABLE,
       "Ta NodeSet ni spremenljiv"},

  { ER_NODESETDTM_NOT_MUTABLE,
       "Ta NodeSetDTM ni spremenljiv"},

  { ER_VAR_NOT_RESOLVABLE,
        "Spremenljivka ni razre\u0161ljiva: {0}"},

  { ER_NULL_ERROR_HANDLER,
        "Program za obravnavo napak NULL"},

  { ER_PROG_ASSERT_UNKNOWN_OPCODE,
       "Programerjeva izjava: neznana opkoda: {0}"},

  { ER_ZERO_OR_ONE,
       "0 ali 1"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "XRTreeFragSelectWrapper ne podpira rtf()"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "XRTreeFragSelectWrapper ne podpira asNodeIterator()"},

   { ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper ne podpira detach()"},

   { ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper ne podpira num()"},

   { ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper ne podpira xstr()"},

   { ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "XRTreeFragSelectWrapper ne podpira str()"},

  { ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,
       "fsb() ni podprt za XStringForChars"},

  { ER_COULD_NOT_FIND_VAR,
      "Spremenljivke z imenom {0} ni mogo\u010de najti"},

  { ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,
      "XStringForChars ne more uporabiti niza za argument"},

  { ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,
      "Argument FastStringBuffer ne more biti NULL"},

  { ER_TWO_OR_THREE,
       "2 ali 3"},

  { ER_VARIABLE_ACCESSED_BEFORE_BIND,
       "Spremenljivka uporabljena \u0161e pred njeno vezavo!"},

  { ER_FSB_CANNOT_TAKE_STRING,
       "XStringForFSB ne more uporabiti niza za argument!"},

  { ER_SETTING_WALKER_ROOT_TO_NULL,
       "\n !!!! Napaka! Koren sprehajalca nastavljam na NULL!!!"},

  { ER_NODESETDTM_CANNOT_ITERATE,
       "Tega NodeSetDTM ni mogo\u010de ponavljati do prej\u0161njega vozli\u0161\u010da!"},

  { ER_NODESET_CANNOT_ITERATE,
       "Tega NodeSet ni mogo\u010de ponavljati do prej\u0161njega vozli\u0161\u010da!"},

  { ER_NODESETDTM_CANNOT_INDEX,
       "Ta NodeSetDTM ne more opravljati funkcij priprave kazala ali \u0161tetja!"},

  { ER_NODESET_CANNOT_INDEX,
       "Ta NodeSet ne more opravljati funkcij priprave kazala ali \u0161tetja!"},

  { ER_CANNOT_CALL_SETSHOULDCACHENODE,
       "Za klicem nextNode klic setShouldCacheNodes ni mogo\u010d!"},

  { ER_ONLY_ALLOWS,
       "{0} dovoljuje samo argumente {1}"},

  { ER_UNKNOWN_STEP,
       "Programerjeva izjava v getNextStepPos: neznan stepType: {0}"},

  //Note to translators:  A relative location path is a form of XPath expression.
  // The message indicates that such an expression was expected following the
  // characters '/' or '//', but was not found.
  { ER_EXPECTED_REL_LOC_PATH,
      "Za \u017eetonom '/' ali '//' je pri\u010dakovana relativna pot do mesta."},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such an expression was expected,but
  // the characters specified by the substitution text were encountered instead.
  { ER_EXPECTED_LOC_PATH,
       "Pri\u010dakovana pot do lokacije, na\u0161jden pa je naslednji \u017eeton\u003a  {0}"},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such a subexpression was expected,
  // but no more characters were found in the expression.
  { ER_EXPECTED_LOC_PATH_AT_END_EXPR,
       "Namesto pri\u010dakovane poti do lokacije je najden konec izraza XPath."},

  // Note to translators:  A location step is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected
  // following the specified characters.
  { ER_EXPECTED_LOC_STEP,
       "Za \u017eetonom '/' ali '//' je pri\u010dakovan korak mesta."},

  // Note to translators:  A node test is part of an XPath expression that is
  // used to test for particular kinds of nodes.  In this case, a node test that
  // consists of an NCName followed by a colon and an asterisk or that consists
  // of a QName was expected, but was not found.
  { ER_EXPECTED_NODE_TEST,
       "Pri\u010dakovan preskus vozli\u0161\u010da, ki ustreza NCImenu:* ali QImenu."},

  // Note to translators:  A step pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but the specified character was found in the expression instead.
  { ER_EXPECTED_STEP_PATTERN,
       "Pri\u010dakovan stopnjevalni vzorec, najden pa je '/'."},

  // Note to translators: A relative path pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but was not found.
  { ER_EXPECTED_REL_PATH_PATTERN,
       "Pri\u010dakovan vzorec z relativno potjo."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type boolean.
  { ER_CANT_CONVERT_TO_BOOLEAN,
       "XPathResult izraza XPath ''{0}'' ima XPathResultType {1}, ki ga ni mogoe\u010de pretvoriti v boolovo vrednost."},

  // Note to translators: Do not translate ANY_UNORDERED_NODE_TYPE and
  // FIRST_ORDERED_NODE_TYPE.
  { ER_CANT_CONVERT_TO_SINGLENODE,
       "XPathResult izraza XPath ''{0}'' ima XPathResultType {1}, ki ga ni mogo\u010de pretvoriti v eno vozli\u0161\u010de. Metoda getSingleNodeValue velja samo za tipa ANY_UNORDERED_NODE_TYPE in FIRST_ORDERED_NODE_TYPE."},

  // Note to translators: Do not translate UNORDERED_NODE_SNAPSHOT_TYPE and
  // ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_CANT_GET_SNAPSHOT_LENGTH,
       "Metoda getSnapshotLength ne more biti priklicana za XPathResult izraza XPath ''{0}'', ker je tip XPathResultType {1}. Ta metoda se nana\u0161a samo na tipa UNORDERED_NODE_SNAPSHOT_TYPE in ORDERED_NODE_SNAPSHOT_TYPE."},

  { ER_NON_ITERATOR_TYPE,
       "Metoda iterateNext ne more biti priklicana za XPathResult izraza XPath ''{0}'', ker je tip XPathResultType {1}. Ta metoda se nana\u0161a samo na tipa UNORDERED_NODE_ITERATOR_TYPE in ORDERED_NODE_ITERATOR_TYPE."},

  // Note to translators: This message indicates that the document being operated
  // upon changed, so the iterator object that was being used to traverse the
  // document has now become invalid.
  { ER_DOC_MUTATED,
       "Dokument se je spremenil po vrnitvi rezultatov. Iterator je neveljaven."},

  { ER_INVALID_XPATH_TYPE,
       "Neveljaven argument tipa XPath: {0}"},

  { ER_EMPTY_XPATH_RESULT,
       "Prazen objekt rezultatov XPath"},

  { ER_INCOMPATIBLE_TYPES,
       "Rezultat XPathResult izraza XPath ''{0}'' ima XPathResultType {1}, ki ga ni mogo\u010de prisiliti v dolo\u010den tip XPathResultType {2}."},

  { ER_NULL_RESOLVER,
       "Predpone ni bilo mogo\u010de razre\u0161iti z razre\u0161evalnikom predpon NULL."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.
  { ER_CANT_CONVERT_TO_STRING,
       "Rezultat XPathResult izraza XPath''{0}'' ima XPathResultType {1}, ki ga ni mogo\u010de pretvoriti v niz."},

  // Note to translators: Do not translate snapshotItem,
  // UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_NON_SNAPSHOT_TYPE,
       "Metoda snapshotItem ne more biti priklicana za XPathResult izraza XPath ''{0}'', ker je tip XPathResultType {1}. Ta metoda se nana\u0161a samo na tipa UNORDERED_NODE_SNAPSHOT_TYPE in ORDERED_NODE_SNAPSHOT_TYPE."},

  // Note to translators:  XPathEvaluator is a Java interface name.  An
  // XPathEvaluator is created with respect to a particular XML document, and in
  // this case the expression represented by this object was being evaluated with
  // respect to a context node from a different document.
  { ER_WRONG_DOCUMENT,
       "Kontekstno vozli\u0161\u010de ne pripada dokumentu, povezanem s tem XPathEvaluator."},

  // Note to translators:  The XPath expression cannot be evaluated with respect
  // to this type of node.
  { ER_WRONG_NODETYPE,
       "Tip kontekstnega vozli\u0161\u010da ni podprt."},

  { ER_XPATH_ERROR,
       "Neznana napaka v XPath."},

        { ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER,
                "XPathResult izraza XPath ''{0}'' ima XPathResultType {1}, ki ga ni mogo\u010de pretvoriti v \u0161tevilko."},

  //BEGIN:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  /** Field ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED                       */

  { ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
       "Raz\u0161iritvene funkcije: ''{0}'' ni mogo\u010de priklicati, kadar je zna\u010dilnost XMLConstants.FEATURE_SECURE_PROCESSING nastavljena na True."},

  /** Field ER_RESOLVE_VARIABLE_RETURNS_NULL                       */

  { ER_RESOLVE_VARIABLE_RETURNS_NULL,
       "Funkcija resolveVariable za spremenljivko {0} vra\u010da rezultat ni\u010d"},

  /** Field ER_UNSUPPORTED_RETURN_TYPE                       */

  { ER_UNSUPPORTED_RETURN_TYPE,
       "Nepodprt tip vrnitve : {0}"},

  /** Field ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL                       */

  { ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL,
       "Vir in/ali Tip vrnitve ne moreta biti ni\u010d"},

  /** Field ER_ARG_CANNOT_BE_NULL                       */

  { ER_ARG_CANNOT_BE_NULL,
       "Argument {0} ne more biti ni\u010d"},

  /** Field ER_OBJECT_MODEL_NULL                       */

  { ER_OBJECT_MODEL_NULL,
       "Funkcije {0}#isObjectModelSupported( String objectModel ) ni mogo\u010de priklicati, kadar je objectModel == null"},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_OBJECT_MODEL_EMPTY,
       "Funkcije {0}#isObjectModelSupported( String objectModel ) ni mogo\u010de priklicati, kadar je objectModel == \"\""},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_FEATURE_NAME_NULL,
       "Poskus nastavitve funkcije brez imena (null name): {0}#setFeature( null, {1})"},

  /** Field ER_FEATURE_UNKNOWN                       */

  { ER_FEATURE_UNKNOWN,
       "Poskus nastavitve neznane funkcije \"{0}\":{1}#setFeature({0},{2})"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_NULL_FEATURE,
       "Poskus pridobitve funkcije brez imena (null name): {0}#getFeature(null)"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_UNKNOWN_FEATURE,
       "Poskus pridobitve neznane funkcije \"{0}\":{1}#getFeature({0})"},

  /** Field ER_NULL_XPATH_FUNCTION_RESOLVER                       */

  { ER_NULL_XPATH_FUNCTION_RESOLVER,
       "Poskus nastavitve XPathFunctionResolver na ni\u010d:{0}#setXPathFunctionResolver(null)"},

  /** Field ER_NULL_XPATH_VARIABLE_RESOLVER                       */

  { ER_NULL_XPATH_VARIABLE_RESOLVER,
       "Poskus nastavitve funkcije XPathVariableResolver na ni\u010d:{0}#setXPathVariableResolver(null)"},

  //END:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  // Warnings...

  { WG_LOCALE_NAME_NOT_HANDLED,
      "Podro\u010dno ime v funkciji za oblikovanje \u0161tevilk \u0161e ni podprto!"},

  { WG_PROPERTY_NOT_SUPPORTED,
      "Lastnost XSL ni podprta: {0}"},

  { WG_DONT_DO_ANYTHING_WITH_NS,
      "V tem trenutku ne po\u010dnite ni\u010desar z imenskim prostorom {0} v lastnosti: {1}"},

  { WG_SECURITY_EXCEPTION,
      "Pri\u0161lo je do SecurityException (varnostna izjema) pri poskusu dostopa do sistemske lastnosti XSL: {0}"},

  { WG_QUO_NO_LONGER_DEFINED,
      "Stara sintaksa: quo(...) v XPath ni ve\u010d definiran."},

  { WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST,
      "XPath potrebuje izpeljani objekt za implementacijo nodeTest!"},

  { WG_FUNCTION_TOKEN_NOT_FOUND,
      "ne najdem \u017eetona funkcije."},

  { WG_COULDNOT_FIND_FUNCTION,
      "Ne najdem funkcije: {0}"},

  { WG_CANNOT_MAKE_URL_FROM,
      "Ne morem narediti naslova URL iz: {0}"},

  { WG_EXPAND_ENTITIES_NOT_SUPPORTED,
      "Mo\u017enost -E za raz\u010dlenjevalnik DTM ni podprta."},

  { WG_ILLEGAL_VARIABLE_REFERENCE,
      "Dani VariableReference je izven konteksta ali brez definicije!  Ime = {0}"},

  { WG_UNSUPPORTED_ENCODING,
     "Nepodprto kodiranje: {0}"},



  // Other miscellaneous text used inside the code...
  { "ui_language", "sl"},
  { "help_language", "sl"},
  { "language", "sl"},
  { "BAD_CODE", "Parameter za createMessage presega meje"},
  { "FORMAT_FAILED", "Med klicem je messageFormat naletel na izjemo"},
  { "version", ">>>>>>> Razli\u010dica Xalan "},
  { "version2", "<<<<<<<"},
  { "yes", "da"},
  { "line", "Vrstica #"},
  { "column", "Stolpec #"},
  { "xsldone", "XSLProcessor: dokon\u010dano"},
  { "xpath_option", "Mo\u017enosti xpath: "},
  { "optionIN", "   [-in inputXMLURL]"},
  { "optionSelect", "   [-select izraz xpath]"},
  { "optionMatch", "   [-match primerjalni vzorec (za diagnostiko ujemanja)]"},
  { "optionAnyExpr", "Ali pa bo samo izraz xpath izvedel diagnosti\u010dni izvoz podatkov"},
  { "noParsermsg1", "Postopek XSL ni uspel."},
  { "noParsermsg2", "** Nisem na\u0161el raz\u010dlenjevalnika **"},
  { "noParsermsg3", "Preverite pot razreda."},
  { "noParsermsg4", "\u010ce nimate IBM raz\u010dlenjevalnika za Javo, ga lahko prenesete iz"},
  { "noParsermsg5", "IBM AlphaWorks: http://www.alphaworks.ibm.com/formula/xml"},
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
  public static final String ERROR_HEADER = "Napaka: ";

  /** Field WARNING_HEADER          */
  public static final String WARNING_HEADER = "Opozorilo: ";

  /** Field XSL_HEADER          */
  public static final String XSL_HEADER = "XSL ";

  /** Field XML_HEADER          */
  public static final String XML_HEADER = "XML ";

  /** Field QUERY_HEADER          */
  public static final String QUERY_HEADER = "VZOREC ";


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
