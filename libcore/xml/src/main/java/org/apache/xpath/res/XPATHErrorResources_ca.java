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
 * $Id: XPATHErrorResources_ca.java 468655 2006-10-28 07:12:06Z minchau $
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
public class XPATHErrorResources_ca extends ListResourceBundle
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

  { ER_CURRENT_NOT_ALLOWED_IN_MATCH, "La funci\u00f3 current() no \u00e9s permesa en un patr\u00f3 de coincid\u00e8ncia." },

  { ER_CURRENT_TAKES_NO_ARGS, "La funci\u00f3 current() no accepta arguments." },

  { ER_DOCUMENT_REPLACED,
      "La implementaci\u00f3 de la funci\u00f3 document() s'ha substitu\u00eft per org.apache.xalan.xslt.FuncDocument."},

  { ER_CONTEXT_HAS_NO_OWNERDOC,
      "El context no t\u00e9 un document de propietari."},

  { ER_LOCALNAME_HAS_TOO_MANY_ARGS,
      "local-name() t\u00e9 massa arguments."},

  { ER_NAMESPACEURI_HAS_TOO_MANY_ARGS,
      "namespace-uri() t\u00e9 massa arguments."},

  { ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS,
      "normalize-space() t\u00e9 massa arguments."},

  { ER_NUMBER_HAS_TOO_MANY_ARGS,
      "number() t\u00e9 massa arguments."},

  { ER_NAME_HAS_TOO_MANY_ARGS,
     "name() t\u00e9 massa arguments."},

  { ER_STRING_HAS_TOO_MANY_ARGS,
      "string() t\u00e9 massa arguments."},

  { ER_STRINGLENGTH_HAS_TOO_MANY_ARGS,
      "string-length() t\u00e9 massa arguments."},

  { ER_TRANSLATE_TAKES_3_ARGS,
      "La funci\u00f3 translate() t\u00e9 tres arguments."},

  { ER_UNPARSEDENTITYURI_TAKES_1_ARG,
      "La funci\u00f3 unparsed-entity-uri ha de tenir un argument."},

  { ER_NAMESPACEAXIS_NOT_IMPLEMENTED,
      "L'eix de l'espai de noms encara no s'ha implementat."},

  { ER_UNKNOWN_AXIS,
     "Eix desconegut: {0}"},

  { ER_UNKNOWN_MATCH_OPERATION,
     "Operaci\u00f3 de coincid\u00e8ncia desconeguda."},

  { ER_INCORRECT_ARG_LENGTH,
      "La longitud de l'argument de la prova de node processing-instruction() no \u00e9s correcta."},

  { ER_CANT_CONVERT_TO_NUMBER,
      "No es pot convertir {0} en un n\u00famero."},

  { ER_CANT_CONVERT_TO_NODELIST,
      "No es pot convertir {0} en una NodeList."},

  { ER_CANT_CONVERT_TO_MUTABLENODELIST,
      "No es pot convertir {0} en un NodeSetDTM."},

  { ER_CANT_CONVERT_TO_TYPE,
      "No es pot convertir {0} en un tipus #{1}"},

  { ER_EXPECTED_MATCH_PATTERN,
      "El patr\u00f3 de coincid\u00e8ncia de getMatchScore \u00e9s l'esperat."},

  { ER_COULDNOT_GET_VAR_NAMED,
      "No s''ha pogut obtenir la variable {0}."},

  { ER_UNKNOWN_OPCODE,
     "ERROR. Codi op desconegut: {0}"},

  { ER_EXTRA_ILLEGAL_TOKENS,
     "Senyals addicionals no permesos: {0}"},


  { ER_EXPECTED_DOUBLE_QUOTE,
      "Les cometes del literal s\u00f3n incorrectes. Hi ha d'haver cometes dobles."},

  { ER_EXPECTED_SINGLE_QUOTE,
      "Les cometes del literal s\u00f3n incorrectes. Hi ha d'haver una cometa simple."},

  { ER_EMPTY_EXPRESSION,
     "Expressi\u00f3 buida."},

  { ER_EXPECTED_BUT_FOUND,
     "S''esperava {0}, per\u00f2 s''ha detectat {1}"},

  { ER_INCORRECT_PROGRAMMER_ASSERTION,
      "L''afirmaci\u00f3 del programador \u00e9s incorrecta. - {0}"},

  { ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,
      "L'argument boolean(...) ja no \u00e9s opcional amb l'esborrany d'XPath 19990709."},

  { ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,
      "S'ha trobat ',' per\u00f2 al davant no hi havia cap argument."},

  { ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
      "S'ha trobat ',' per\u00f2 al darrere no hi havia cap argument."},

  { ER_PREDICATE_ILLEGAL_SYNTAX,
      "'..[predicate]' o '.[predicate]' no \u00e9s una sintaxi permesa. En comptes d'aix\u00f2, utilitzeu 'self::node()[predicate]'."},

  { ER_ILLEGAL_AXIS_NAME,
     "Nom d''eix no perm\u00e8s: {0}"},

  { ER_UNKNOWN_NODETYPE,
     "Tipus de node desconegut: {0}"},

  { ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
      "El literal de patr\u00f3 ({0}) ha d''anar entre cometes."},

  { ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
      "{0} no s''ha pogut formatar com a n\u00famero."},

  { ER_COULDNOT_CREATE_XMLPROCESSORLIAISON,
      "No s''ha pogut crear la relaci\u00f3 XML TransformerFactory: {0}"},

  { ER_DIDNOT_FIND_XPATH_SELECT_EXP,
      "Error. No s'ha trobat l'expressi\u00f3 select d'xpath (-select)."},

  { ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH,
      "ERROR. No s'ha trobat ENDOP despr\u00e9s d'OP_LOCATIONPATH."},

  { ER_ERROR_OCCURED,
     "S'ha produ\u00eft un error."},

  { ER_ILLEGAL_VARIABLE_REFERENCE,
      "S''ha donat VariableReference per a una variable fora de context o sense definici\u00f3. Nom = {0}"},

  { ER_AXES_NOT_ALLOWED,
      "Nom\u00e9s es permeten els eixos subordinat:: i atribut:: en els patrons de coincid\u00e8ncia. Eixos incorrectes = {0}"},

  { ER_KEY_HAS_TOO_MANY_ARGS,
      "key() t\u00e9 un nombre incorrecte d'arguments."},

  { ER_COUNT_TAKES_1_ARG,
      "La funci\u00f3 count ha de tenir un argument."},

  { ER_COULDNOT_FIND_FUNCTION,
     "No s''ha pogut trobar la funci\u00f3: {0}"},

  { ER_UNSUPPORTED_ENCODING,
     "Codificaci\u00f3 sense suport: {0}"},

  { ER_PROBLEM_IN_DTM_NEXTSIBLING,
      "S'ha produ\u00eft un error en el DTM de getNextSibling. S'intentar\u00e0 solucionar."},

  { ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL,
      "Error del programador: no es pot escriure a EmptyNodeList."},

  { ER_SETDOMFACTORY_NOT_SUPPORTED,
      "XPathContext no d\u00f3na suport a setDOMFactory."},

  { ER_PREFIX_MUST_RESOLVE,
      "El prefix s''ha de resoldre en un espai de noms: {0}"},

  { ER_PARSE_NOT_SUPPORTED,
      "L''an\u00e0lisi (origen InputSource) no t\u00e9 suport a XPathContext. No es pot obrir {0}."},

  { ER_SAX_API_NOT_HANDLED,
      "Els car\u00e0cters de l'API SAX (char ch[]... no es poden gestionar pel DTM."},

  { ER_IGNORABLE_WHITESPACE_NOT_HANDLED,
      "ignorableWhitespace(char ch[]... no es poden gestionar pel DTM."},

  { ER_DTM_CANNOT_HANDLE_NODES,
      "DTMLiaison no pot gestionar nodes del tipus {0}."},

  { ER_XERCES_CANNOT_HANDLE_NODES,
      "DOM2Helper no pot gestionar nodes del tipus {0}."},

  { ER_XERCES_PARSE_ERROR_DETAILS,
      "Error de DOM2Helper.parse: ID del sistema - {0} l\u00ednia - {1}"},

  { ER_XERCES_PARSE_ERROR,
     "Error de DOM2Helper.parse"},

  { ER_INVALID_UTF16_SURROGATE,
      "S''ha detectat un suplent UTF-16 no v\u00e0lid: {0} ?"},

  { ER_OIERROR,
     "Error d'E/S"},

  { ER_CANNOT_CREATE_URL,
     "No es pot crear la URL de: {0}"},

  { ER_XPATH_READOBJECT,
     "En XPath.readObject: {0}"},

  { ER_FUNCTION_TOKEN_NOT_FOUND,
      "No s'ha trobat el senyal de funci\u00f3."},

  { ER_CANNOT_DEAL_XPATH_TYPE,
       "No s''ha pogut tractar amb el tipus d''XPath: {0}"},

  { ER_NODESET_NOT_MUTABLE,
       "Aquest NodeSet no \u00e9s mutable."},

  { ER_NODESETDTM_NOT_MUTABLE,
       "Aquest NodeSetDTM no \u00e9s mutable."},

  { ER_VAR_NOT_RESOLVABLE,
        "No es pot resoldre la variable: {0}"},

  { ER_NULL_ERROR_HANDLER,
        "Manejador d'error nul"},

  { ER_PROG_ASSERT_UNKNOWN_OPCODE,
       "Afirmaci\u00f3 del programador: opcode desconegut: {0}"},

  { ER_ZERO_OR_ONE,
       "0 o 1"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "rtf() no t\u00e9 suport d'XRTreeFragSelectWrapper"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "asNodeIterator() no t\u00e9 suport d'XRTreeFragSelectWrapper"},

   { ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "detach() no t\u00e9 suport d'XRTreeFragSelectWrapper"},

   { ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "num() no t\u00e9 suport d'XRTreeFragSelectWrapper"},

   { ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "xstr() no t\u00e9 suport d'XRTreeFragSelectWrapper"},

   { ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "str() no t\u00e9 suport d'XRTreeFragSelectWrapper"},

  { ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,
       "fsb() no t\u00e9 suport d'XStringForChars"},

  { ER_COULD_NOT_FIND_VAR,
      "No s''ha trobat la variable amb el nom de {0}"},

  { ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,
      "XStringForChars no pot agafar una cadena com a argument."},

  { ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,
      "L'argument FastStringBuffer no pot ser nul."},

  { ER_TWO_OR_THREE,
       "2 o 3"},

  { ER_VARIABLE_ACCESSED_BEFORE_BIND,
       "S'ha accedit a la variable abans que estigu\u00e9s vinculada."},

  { ER_FSB_CANNOT_TAKE_STRING,
       "XStringForFSB no pot agafar una cadena com a argument."},

  { ER_SETTING_WALKER_ROOT_TO_NULL,
       "\n Error. S'est\u00e0 establint l'arrel d'un itinerant en nul."},

  { ER_NODESETDTM_CANNOT_ITERATE,
       "Aquest NodeSetDTM no es pot iterar en un node previ"},

  { ER_NODESET_CANNOT_ITERATE,
       "Aquest NodeSet no es pot iterar en un node previ"},

  { ER_NODESETDTM_CANNOT_INDEX,
       "Aquest NodeSetDTM no pot indexar ni efectuar funcions de recompte"},

  { ER_NODESET_CANNOT_INDEX,
       "Aquest NodeSet no pot indexar ni efectuar funcions de recompte"},

  { ER_CANNOT_CALL_SETSHOULDCACHENODE,
       "No es pot cridar setShouldCacheNodes despr\u00e9s que s'hagi cridat nextNode"},

  { ER_ONLY_ALLOWS,
       "{0} nom\u00e9s permet {1} arguments"},

  { ER_UNKNOWN_STEP,
       "Afirmaci\u00f3 del programador a getNextStepPos: stepType desconegut: {0}"},

  //Note to translators:  A relative location path is a form of XPath expression.
  // The message indicates that such an expression was expected following the
  // characters '/' or '//', but was not found.
  { ER_EXPECTED_REL_LOC_PATH,
      "S'esperava una via d'acc\u00e9s relativa darrere del senyal '/' o '//'."},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such an expression was expected,but
  // the characters specified by the substitution text were encountered instead.
  { ER_EXPECTED_LOC_PATH,
       "S''esperava una via d''acc\u00e9s d''ubicaci\u00f3, per\u00f2 s''ha trobat el senyal seg\u00fcent\u003a {0}"},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such a subexpression was expected,
  // but no more characters were found in the expression.
  { ER_EXPECTED_LOC_PATH_AT_END_EXPR,
       "S'esperava una via d'acc\u00e9s, per\u00f2 enlloc d'aix\u00f2 s'ha trobat el final de l'expressi\u00f3 XPath. "},

  // Note to translators:  A location step is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected
  // following the specified characters.
  { ER_EXPECTED_LOC_STEP,
       "S'esperava un pas d'ubicaci\u00f3 despr\u00e9s del senyal '/' o '//'."},

  // Note to translators:  A node test is part of an XPath expression that is
  // used to test for particular kinds of nodes.  In this case, a node test that
  // consists of an NCName followed by a colon and an asterisk or that consists
  // of a QName was expected, but was not found.
  { ER_EXPECTED_NODE_TEST,
       "S'esperava una prova de node que coincid\u00eds amb NCName:* o QName."},

  // Note to translators:  A step pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but the specified character was found in the expression instead.
  { ER_EXPECTED_STEP_PATTERN,
       "S'esperava un patr\u00f3 de pas per\u00f2 s'ha trobat '/'."},

  // Note to translators: A relative path pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but was not found.
  { ER_EXPECTED_REL_PATH_PATTERN,
       "S'esperava un patr\u00f3 de via d'acc\u00e9s relativa."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type boolean.
  { ER_CANT_CONVERT_TO_BOOLEAN,
       "L''expressi\u00f3 XPathResult d''XPath ''{0}'' t\u00e9 un XPathResultType de {1} que no es pot convertir a un cap valor boole\u00e0. "},

  // Note to translators: Do not translate ANY_UNORDERED_NODE_TYPE and
  // FIRST_ORDERED_NODE_TYPE.
  { ER_CANT_CONVERT_TO_SINGLENODE,
       "L''expressi\u00f3 XPathResult d''XPath ''{0}'' t\u00e9 un XPathResultType de {1} que no es pot convertir a un node \u00fanic. El m\u00e8tode getSingleNodeValue s''aplica nom\u00e9s al tipus ANY_UNORDERED_NODE_TYPE i FIRST_ORDERED_NODE_TYPE."},

  // Note to translators: Do not translate UNORDERED_NODE_SNAPSHOT_TYPE and
  // ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_CANT_GET_SNAPSHOT_LENGTH,
       "El m\u00e8tode getSnapshotLength no es pot cridar a l''expressi\u00f3 XPathResult d''XPath ''{0}'' perqu\u00e8 el seu XPathResultType \u00e9s {1}. Aquest m\u00e8tode nom\u00e9s s''aplica als tipus UNORDERED_NODE_SNAPSHOT_TYPE i ORDERED_NODE_SNAPSHOT_TYPE."},

  { ER_NON_ITERATOR_TYPE,
       "El m\u00e8tode iterateNext no es pot cridar a l''expressi\u00f3 XPathResult d''XPath ''{0}'' perqu\u00e8 el seu XPathResultType \u00e9s {1}. Aquest m\u00e8tode nom\u00e9s s''aplica als tipus UNORDERED_NODE_ITERATOR_TYPE i ORDERED_NODE_ITERATOR_TYPE."},

  // Note to translators: This message indicates that the document being operated
  // upon changed, so the iterator object that was being used to traverse the
  // document has now become invalid.
  { ER_DOC_MUTATED,
       "El document s'ha modificat des que es van produir els resultats. L'iterador no \u00e9s v\u00e0lid."},

  { ER_INVALID_XPATH_TYPE,
       "L''argument de tipus XPath no \u00e9s v\u00e0lid: {0}"},

  { ER_EMPTY_XPATH_RESULT,
       "L'objecte de resultats XPath est\u00e0 buit."},

  { ER_INCOMPATIBLE_TYPES,
       "L''expressi\u00f3 XPathResult d''XPath ''{0}'' t\u00e9 un XPathResultType de {1} que no es pot encaixar al XPathResultType especificat de {2}."},

  { ER_NULL_RESOLVER,
       "No es pot resoldre el prefix amb un solucionador de prefix nul."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.
  { ER_CANT_CONVERT_TO_STRING,
       "L''expressi\u00f3 XPathResult d''XPath ''{0}'' t\u00e9 un XPathResultType de {1} que no es pot convertir a cap cadena. "},

  // Note to translators: Do not translate snapshotItem,
  // UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_NON_SNAPSHOT_TYPE,
       "El m\u00e8tode snapshotItem no es pot cridar a l''expressi\u00f3 XPathResult d''XPath ''{0}'' perqu\u00e8 el seu XPathResultType \u00e9s {1}. Aquest m\u00e8tode nom\u00e9s s''aplica als tipus UNORDERED_NODE_SNAPSHOT_TYPE i ORDERED_NODE_SNAPSHOT_TYPE."},

  // Note to translators:  XPathEvaluator is a Java interface name.  An
  // XPathEvaluator is created with respect to a particular XML document, and in
  // this case the expression represented by this object was being evaluated with
  // respect to a context node from a different document.
  { ER_WRONG_DOCUMENT,
       "El node de context no pertany al document vinculat a aquest XPathEvaluator."},

  // Note to translators:  The XPath expression cannot be evaluated with respect
  // to this type of node.
  { ER_WRONG_NODETYPE,
       "El tipus de node de context no t\u00e9 suport."},

  { ER_XPATH_ERROR,
       "S'ha produ\u00eft un error desconegut a XPath."},

        { ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER,
                "L''expressi\u00f3 XPathResult d''XPath ''{0}'' t\u00e9 un XPathResultType de {1} que no es pot convertir a cap n\u00famero "},

  //BEGIN:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  /** Field ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED                       */

  { ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
       "Funci\u00f3 d''extensi\u00f3: no es pot invocar ''{0}'' si la caracter\u00edstica XMLConstants.FEATURE_SECURE_PROCESSING s''ha establert en true."},

  /** Field ER_RESOLVE_VARIABLE_RETURNS_NULL                       */

  { ER_RESOLVE_VARIABLE_RETURNS_NULL,
       "resolveVariable de la variable {0} torna el valor nul"},

  /** Field ER_UNSUPPORTED_RETURN_TYPE                       */

  { ER_UNSUPPORTED_RETURN_TYPE,
       "Tipus de retorn no suportat: {0}"},

  /** Field ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL                       */

  { ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL,
       "El tipus de retorn o d'origen no pot ser nul"},

  /** Field ER_ARG_CANNOT_BE_NULL                       */

  { ER_ARG_CANNOT_BE_NULL,
       "L''argument {0} no pot ser nul "},

  /** Field ER_OBJECT_MODEL_NULL                       */

  { ER_OBJECT_MODEL_NULL,
       "{0}#isObjectModelSupported( String objectModel) no es pot cridar amb objectModel == null"},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_OBJECT_MODEL_EMPTY,
       "{0}#isObjectModelSupported( String objectModel ) no es pot cridar amb objectModel == \"\""},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_FEATURE_NAME_NULL,
       "Intent d''establir una caracter\u00edstica amb un nom nul: {0}#setFeature( null, {1})"},

  /** Field ER_FEATURE_UNKNOWN                       */

  { ER_FEATURE_UNKNOWN,
       "Intent d''establir una caracter\u00edstica desconeguda \"{0}\":{1}#setFeature({0},{2})"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_NULL_FEATURE,
       "Intent d''obtenir una caracter\u00edstica amb un nom nul: {0}#getFeature(null)"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_UNKNOWN_FEATURE,
       "Intent d''obtenir la caracter\u00edstica desconeguda \"{0}\":{1}#getFeature({0})"},

  /** Field ER_NULL_XPATH_FUNCTION_RESOLVER                       */

  { ER_NULL_XPATH_FUNCTION_RESOLVER,
       "S''ha intentat establir un XPathFunctionResolver nul:{0}#setXPathFunctionResolver(null)"},

  /** Field ER_NULL_XPATH_VARIABLE_RESOLVER                       */

  { ER_NULL_XPATH_VARIABLE_RESOLVER,
       "S''ha intentat establir un XPathVariableResolver null:{0}#setXPathVariableResolver(null)"},

  //END:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  // Warnings...

  { WG_LOCALE_NAME_NOT_HANDLED,
      "No s'ha gestionat encara el nom d'entorn nacional en la funci\u00f3 format-number."},

  { WG_PROPERTY_NOT_SUPPORTED,
      "La propietat XSL no t\u00e9 suport: {0}"},

  { WG_DONT_DO_ANYTHING_WITH_NS,
      "No feu res ara mateix amb l''espai de noms {0} de la propietat: {1}"},

  { WG_SECURITY_EXCEPTION,
      "S''ha produ\u00eft SecurityException en intentar accedir a la propietat de sistema XSL: {0}"},

  { WG_QUO_NO_LONGER_DEFINED,
      "Sintaxi antiga: quo(...) ja no est\u00e0 definit a XPath."},

  { WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST,
      "XPath necessita un objecte dedu\u00eft per implementar nodeTest."},

  { WG_FUNCTION_TOKEN_NOT_FOUND,
      "No s'ha trobat el senyal de funci\u00f3."},

  { WG_COULDNOT_FIND_FUNCTION,
      "No s''ha pogut trobar la funci\u00f3: {0}"},

  { WG_CANNOT_MAKE_URL_FROM,
      "No es pot crear la URL de: {0}"},

  { WG_EXPAND_ENTITIES_NOT_SUPPORTED,
      "L'opci\u00f3 -E no t\u00e9 suport a l'analitzador de DTM"},

  { WG_ILLEGAL_VARIABLE_REFERENCE,
      "S''ha donat VariableReference per a una variable fora de context o sense definici\u00f3. Nom = {0}"},

  { WG_UNSUPPORTED_ENCODING,
     "Codificaci\u00f3 sense suport: {0}"},



  // Other miscellaneous text used inside the code...
  { "ui_language", "ca"},
  { "help_language", "ca"},
  { "language", "ca"},
  { "BAD_CODE", "El par\u00e0metre de createMessage estava fora dels l\u00edmits."},
  { "FORMAT_FAILED", "S'ha generat una excepci\u00f3 durant la crida messageFormat."},
  { "version", ">>>>>>> Versi\u00f3 Xalan "},
  { "version2", "<<<<<<<"},
  { "yes", "s\u00ed"},
  { "line", "L\u00ednia n\u00fam."},
  { "column", "Columna n\u00fam."},
  { "xsldone", "XSLProcessor: fet"},
  { "xpath_option", "Opcions d'xpath: "},
  { "optionIN", "   [-in inputXMLURL]"},
  { "optionSelect", "   [-select expressi\u00f3 xpath]"},
  { "optionMatch", "   [-match patr\u00f3 coincid\u00e8ncia (per a diagn\u00f2stics de coincid\u00e8ncia)]"},
  { "optionAnyExpr", "O nom\u00e9s una expressi\u00f3 xpath far\u00e0 un buidatge de diagn\u00f2stic."},
  { "noParsermsg1", "El proc\u00e9s XSL no ha estat correcte."},
  { "noParsermsg2", "** No s'ha trobat l'analitzador **"},
  { "noParsermsg3", "Comproveu la vostra classpath."},
  { "noParsermsg4", "Si no teniu XML Parser for Java d'IBM, el podeu baixar de l'indret web"},
  { "noParsermsg5", "AlphaWorks d'IBM: http://www.alphaworks.ibm.com/formula/xml"},
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
  public static final String ERROR_HEADER = "Error: ";

  /** Field WARNING_HEADER          */
  public static final String WARNING_HEADER = "Av\u00eds: ";

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
                new Locale("ca", "ES"));
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
