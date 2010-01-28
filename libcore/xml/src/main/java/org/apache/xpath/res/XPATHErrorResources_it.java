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
 * $Id: XPATHErrorResources_it.java 468655 2006-10-28 07:12:06Z minchau $
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
public class XPATHErrorResources_it extends ListResourceBundle
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

  { ER_CURRENT_NOT_ALLOWED_IN_MATCH, "La funzione corrente () non \u00e8 consentita in un modello di corrispondenza." },

  { ER_CURRENT_TAKES_NO_ARGS, "La funzione corrente () non accetta argomenti." },

  { ER_DOCUMENT_REPLACED,
      "L'implementazione della funzione documento () \u00e8 stata sostituita da org.apache.xalan.xslt.FuncDocument."},

  { ER_CONTEXT_HAS_NO_OWNERDOC,
      "il contesto non ha un documento proprietario."},

  { ER_LOCALNAME_HAS_TOO_MANY_ARGS,
      "local-name() ha troppi argomenti."},

  { ER_NAMESPACEURI_HAS_TOO_MANY_ARGS,
      "namespace-uri() ha troppi argomenti."},

  { ER_NORMALIZESPACE_HAS_TOO_MANY_ARGS,
      "normalize-space() ha troppi argomenti."},

  { ER_NUMBER_HAS_TOO_MANY_ARGS,
      "number() ha troppi argomenti."},

  { ER_NAME_HAS_TOO_MANY_ARGS,
     "name() ha troppi argomenti."},

  { ER_STRING_HAS_TOO_MANY_ARGS,
      "string() ha troppi argomenti."},

  { ER_STRINGLENGTH_HAS_TOO_MANY_ARGS,
      "string-length() ha troppi argomenti."},

  { ER_TRANSLATE_TAKES_3_ARGS,
      "La funzione translate() richiede tre argomenti."},

  { ER_UNPARSEDENTITYURI_TAKES_1_ARG,
      "La funzione unparsed-entity-uri richiede un argomento."},

  { ER_NAMESPACEAXIS_NOT_IMPLEMENTED,
      "namespace axis non ancora implementato."},

  { ER_UNKNOWN_AXIS,
     "asse sconosciuto: {0}"},

  { ER_UNKNOWN_MATCH_OPERATION,
     "operazione di corrispondenza sconosciuta."},

  { ER_INCORRECT_ARG_LENGTH,
      "Lunghezza argomento nella prova nodo processing-instruction() non corretta."},

  { ER_CANT_CONVERT_TO_NUMBER,
      "Impossibile convertire {0} in un numero"},

  { ER_CANT_CONVERT_TO_NODELIST,
      "Impossibile convertire {0} in un NodeList."},

  { ER_CANT_CONVERT_TO_MUTABLENODELIST,
      "Impossibile convertire {0} in un NodeSetDTM."},

  { ER_CANT_CONVERT_TO_TYPE,
      "Impossibile convertire {0} in un type#{1}"},

  { ER_EXPECTED_MATCH_PATTERN,
      "Modello corrispondenza previsto in getMatchScore!"},

  { ER_COULDNOT_GET_VAR_NAMED,
      "Impossibile richiamare la variabile denominata {0}"},

  { ER_UNKNOWN_OPCODE,
     "ERRORE! Codice operativo sconosciuto: {0}"},

  { ER_EXTRA_ILLEGAL_TOKENS,
     "Token aggiuntivi non validi: {0}"},


  { ER_EXPECTED_DOUBLE_QUOTE,
      "letterale con numero di apici errato... previsti i doppi apici."},

  { ER_EXPECTED_SINGLE_QUOTE,
      "letterale con numero di apici errato... previsto un solo apice."},

  { ER_EMPTY_EXPRESSION,
     "Espressione vuota."},

  { ER_EXPECTED_BUT_FOUND,
     "Era previsto {0}, ma \u00e8 stato trovato: {1}"},

  { ER_INCORRECT_PROGRAMMER_ASSERTION,
      "Asserzione programmatore errata. - {0}"},

  { ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL,
      "Argomento boolean(...) non pi\u00f9 facoltativo con la versione 19990709 XPath."},

  { ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG,
      "Trovata ',' senza argomento che la precede."},

  { ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG,
      "Trovata ',' senza argomento che la segue."},

  { ER_PREDICATE_ILLEGAL_SYNTAX,
      "'..[predicato]' o '.[predicato]' \u00e8 una sintassi non valida.  Utilizzare 'self::node()[predicato]'."},

  { ER_ILLEGAL_AXIS_NAME,
     "nome asse non valido: {0}"},

  { ER_UNKNOWN_NODETYPE,
     "Nodetype sconosciuto: {0}"},

  { ER_PATTERN_LITERAL_NEEDS_BE_QUOTED,
      "Il letterale modello ({0}) deve essere racchiuso fra virgolette."},

  { ER_COULDNOT_BE_FORMATTED_TO_NUMBER,
      "{0} non pu\u00f2 essere formattato in un numero."},

  { ER_COULDNOT_CREATE_XMLPROCESSORLIAISON,
      "Impossibile creare XML TransformerFactory Liaison: {0}"},

  { ER_DIDNOT_FIND_XPATH_SELECT_EXP,
      "Errore! Impossibile trovare espressione selezione xpath (-select)."},

  { ER_COULDNOT_FIND_ENDOP_AFTER_OPLOCATIONPATH,
      "ERRORE! Impossibile trovare ENDOP dopo OP_LOCATIONPATH"},

  { ER_ERROR_OCCURED,
     "Si \u00e8 verificato un errore."},

  { ER_ILLEGAL_VARIABLE_REFERENCE,
      "VariableReference fornito per la variabile \u00e8 fuori contesto o senza definizione.  Nome = {0}"},

  { ER_AXES_NOT_ALLOWED,
      "Sono consentiti solo gli assi child:: e attribute:: nei modelli di corrispondenza.  Violazione asse = {0}"},

  { ER_KEY_HAS_TOO_MANY_ARGS,
      "key() con numero di argomenti scorretto."},

  { ER_COUNT_TAKES_1_ARG,
      "La funzione count richiede un argomento."},

  { ER_COULDNOT_FIND_FUNCTION,
     "Impossibile trovare la funzione: {0}"},

  { ER_UNSUPPORTED_ENCODING,
     "Codifica non supportata: {0}"},

  { ER_PROBLEM_IN_DTM_NEXTSIBLING,
      "Si \u00e8 verificato un problema in DTM durante l'esecuzione di getNextSibling... tentativo di recupero in corso"},

  { ER_CANNOT_WRITE_TO_EMPTYNODELISTIMPL,
      "Errore di programmazione: Impossibile scrivere su EmptyNodeList."},

  { ER_SETDOMFACTORY_NOT_SUPPORTED,
      "setDOMFactory non supportato da XPathContext!"},

  { ER_PREFIX_MUST_RESOLVE,
      "Il prefisso deve risolvere in uno namespace: {0}"},

  { ER_PARSE_NOT_SUPPORTED,
      "parse (InputSource source) non supportato in XPathContext! Impossibile aprire {0}"},

  { ER_SAX_API_NOT_HANDLED,
      "Caratteri SAX API (char ch[]... non gestiti da DTM!"},

  { ER_IGNORABLE_WHITESPACE_NOT_HANDLED,
      "ignorableWhitespace(char ch[]... non gestiti da DTM!"},

  { ER_DTM_CANNOT_HANDLE_NODES,
      "DTMLiaison non pu\u00f2 gestire i nodi di tipo {0}"},

  { ER_XERCES_CANNOT_HANDLE_NODES,
      "DOM2Helper non pu\u00f2 gestire i nodi di tipo {0}"},

  { ER_XERCES_PARSE_ERROR_DETAILS,
      "Errore DOM2Helper.parse: SystemID - {0} riga - {1}"},

  { ER_XERCES_PARSE_ERROR,
     "Errore DOM2Helper.parse"},

  { ER_INVALID_UTF16_SURROGATE,
      "Rilevato surrogato UTF-16 non valido: {0} ?"},

  { ER_OIERROR,
     "Errore IO"},

  { ER_CANNOT_CREATE_URL,
     "Impossibile creare url per: {0}"},

  { ER_XPATH_READOBJECT,
     "In XPath.readObject: {0}"},

  { ER_FUNCTION_TOKEN_NOT_FOUND,
      "token funzione non trovato."},

  { ER_CANNOT_DEAL_XPATH_TYPE,
       "Impossibile gestire il tipo XPath: {0}"},

  { ER_NODESET_NOT_MUTABLE,
       "Questo NodeSet non \u00e8 trasformabile"},

  { ER_NODESETDTM_NOT_MUTABLE,
       "Questo NodeSetDTM non \u00e8 trasformabile"},

  { ER_VAR_NOT_RESOLVABLE,
        "Variabile non risolvibile: {0}"},

  { ER_NULL_ERROR_HANDLER,
        "Handler errori nullo"},

  { ER_PROG_ASSERT_UNKNOWN_OPCODE,
       "Asserzione di programmatore: codice operativo sconosciuto: {0}"},

  { ER_ZERO_OR_ONE,
       "0 oppure 1"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "rtf() non supportato da XRTreeFragSelectWrapper"},

  { ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
       "asNodeIterator() non supportato da XRTreeFragSelectWrapper"},

   { ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "detach() non supportato da XRTreeFragSelectWrapper"},

   { ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "num() non supportato da XRTreeFragSelectWrapper"},

   { ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "xstr() non supportato da XRTreeFragSelectWrapper"},

   { ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER,
                "str() non supportato da XRTreeFragSelectWrapper"},

  { ER_FSB_NOT_SUPPORTED_XSTRINGFORCHARS,
       "fsb() non supportato per XStringForChars"},

  { ER_COULD_NOT_FIND_VAR,
      "Impossibile trovare la variabile con il nome {0}"},

  { ER_XSTRINGFORCHARS_CANNOT_TAKE_STRING,
      "XStringForChars non pu\u00f2 accettare una stringa come argomento"},

  { ER_FASTSTRINGBUFFER_CANNOT_BE_NULL,
      "L'argomento FastStringBuffer non pu\u00f2 essere nullo"},

  { ER_TWO_OR_THREE,
       "2 o 3"},

  { ER_VARIABLE_ACCESSED_BEFORE_BIND,
       "Variabile acceduta prima che fosse delimitata."},

  { ER_FSB_CANNOT_TAKE_STRING,
       "XStringForFSB non pu\u00f2 accettare una stringa come argomento."},

  { ER_SETTING_WALKER_ROOT_TO_NULL,
       "\n !!!! Errore! Si sta impostando il nodo di partenza su null"},

  { ER_NODESETDTM_CANNOT_ITERATE,
       "NodeSetDTM non pu\u00f2 collegarsi al nodo precedente"},

  { ER_NODESET_CANNOT_ITERATE,
       "NodeSet non pu\u00f2 collegarsi al nodo precedente"},

  { ER_NODESETDTM_CANNOT_INDEX,
       "NodeSetDTM non pu\u00f2 eseguire l'indicizzazione o il conteggio delle funzioni."},

  { ER_NODESET_CANNOT_INDEX,
       "NodeSet non pu\u00f2 eseguire l'indicizzazione o il conteggio delle funzioni."},

  { ER_CANNOT_CALL_SETSHOULDCACHENODE,
       "Impossibile chiamare setShouldCacheNodes dopo aver chiamato nextNode."},

  { ER_ONLY_ALLOWS,
       "{0} consente solo {1} argomenti"},

  { ER_UNKNOWN_STEP,
       "Asserzione di programmatore in getNextStepPos: stepType sconosciuto: {0}"},

  //Note to translators:  A relative location path is a form of XPath expression.
  // The message indicates that such an expression was expected following the
  // characters '/' or '//', but was not found.
  { ER_EXPECTED_REL_LOC_PATH,
      "Era previsto un percorso relativo dopo il token '/' oppure '//'."},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such an expression was expected,but
  // the characters specified by the substitution text were encountered instead.
  { ER_EXPECTED_LOC_PATH,
       "Era previsto un percorso, ma \u00e8 stato rilevato il seguente token\u003a  {0}"},

  // Note to translators:  A location path is a form of XPath expression.
  // The message indicates that syntactically such a subexpression was expected,
  // but no more characters were found in the expression.
  { ER_EXPECTED_LOC_PATH_AT_END_EXPR,
       "Era previsto un percorso, ma invece \u00e8 stata trovata la fine dell'espressione XPath."},

  // Note to translators:  A location step is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected
  // following the specified characters.
  { ER_EXPECTED_LOC_STEP,
       "Era previsto un passo di posizione dopo il token '/' oppure '//'."},

  // Note to translators:  A node test is part of an XPath expression that is
  // used to test for particular kinds of nodes.  In this case, a node test that
  // consists of an NCName followed by a colon and an asterisk or that consists
  // of a QName was expected, but was not found.
  { ER_EXPECTED_NODE_TEST,
       "Era prevista una prova nodo che corrisponde a NCName:* oppure a QName."},

  // Note to translators:  A step pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but the specified character was found in the expression instead.
  { ER_EXPECTED_STEP_PATTERN,
       "Era previsto un modello passo, ma \u00e8 stato rilevato '/'."},

  // Note to translators: A relative path pattern is part of an XPath expression.
  // The message indicates that syntactically such an expression was expected,
  // but was not found.
  { ER_EXPECTED_REL_PATH_PATTERN,
       "Era previsto un modello percorso relativo."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type boolean.
  { ER_CANT_CONVERT_TO_BOOLEAN,
       "XPathResult dell''''espressione XPath ''{0}'' ha un XPathResultType di {1} che non pu\u00f2 essere convertito in valore booleano."},

  // Note to translators: Do not translate ANY_UNORDERED_NODE_TYPE and
  // FIRST_ORDERED_NODE_TYPE.
  { ER_CANT_CONVERT_TO_SINGLENODE,
       "XPathResult dell''''espressione XPath ''{0}'' ha un XPathResultType di {1} che non pu\u00f2 essere convertito in un nodo singolo. Il metodo getSingleNodeValue si applica solo ai tipi ANY_UNORDERED_NODE_TYPE eFIRST_ORDERED_NODE_TYPE."},

  // Note to translators: Do not translate UNORDERED_NODE_SNAPSHOT_TYPE and
  // ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_CANT_GET_SNAPSHOT_LENGTH,
       "Il metodo getSnapshotLength non pu\u00f2 essere chiamato al XPathResult dell''''espressione XPath ''{0}'' poich\u00e9 il XPathResultType \u00e8 {1}. Questo metodo si applica solo ai tipi UNORDERED_NODE_SNAPSHOT_TYPE e ORDERED_NODE_SNAPSHOT_TYPE."},

  { ER_NON_ITERATOR_TYPE,
       "Il metodo iterateNext non pu\u00f2 essere chiamato in XPathResult dell''''espressione XPath ''{0}'' poich\u00e9 XPathResultType \u00e8 {1}. Questo metodo si applica solo ai tipi UNORDERED_NODE_ITERATOR_TYPE e ORDERED_NODE_ITERATOR_TYPE."},

  // Note to translators: This message indicates that the document being operated
  // upon changed, so the iterator object that was being used to traverse the
  // document has now become invalid.
  { ER_DOC_MUTATED,
       "Documento modificato da quando \u00e8 stato restituito il risultato. Iteratore non valido."},

  { ER_INVALID_XPATH_TYPE,
       "Argomento di tipo XPath non valido: {0}"},

  { ER_EMPTY_XPATH_RESULT,
       "Oggetto risultato XPath vuoto"},

  { ER_INCOMPATIBLE_TYPES,
       "XPathResult dell''''espressione XPath ''{0}'' ha un XPathResultType di {1} che non pu\u00f2 essere convertito nel XPathResultType specificato di {2}."},

  { ER_NULL_RESOLVER,
       "Impossibile risolvere il prefisso con resolver di prefisso nullo."},

  // Note to translators:  The substitution text is the name of a data type.  The
  // message indicates that a value of a particular type could not be converted
  // to a value of type string.
  { ER_CANT_CONVERT_TO_STRING,
       "XPathResult dell''''espressione XPath ''{0}'' ha un XPathResultType di {1} che non pu\u00f2 essere convertito in una stringa."},

  // Note to translators: Do not translate snapshotItem,
  // UNORDERED_NODE_SNAPSHOT_TYPE and ORDERED_NODE_SNAPSHOT_TYPE.
  { ER_NON_SNAPSHOT_TYPE,
       "Il metodo snapshotItem non pu\u00f2 essere chiamato al XPathResult dell''''espressione XPath ''{0}''  poich\u00e9 XPathResultType \u00e8 {1}. Questo metodo si applica solo ai tipi UNORDERED_NODE_SNAPSHOT_TYPE eORDERED_NODE_SNAPSHOT_TYPE."},

  // Note to translators:  XPathEvaluator is a Java interface name.  An
  // XPathEvaluator is created with respect to a particular XML document, and in
  // this case the expression represented by this object was being evaluated with
  // respect to a context node from a different document.
  { ER_WRONG_DOCUMENT,
       "Il nodo di contesto non appartiene al documento collegato a questo XPathEvaluator."},

  // Note to translators:  The XPath expression cannot be evaluated with respect
  // to this type of node.
  { ER_WRONG_NODETYPE,
       "Il tipo di nodo di contesto non \u00e8 supportato."},

  { ER_XPATH_ERROR,
       "Errore sconosciuto in XPath."},

        { ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER,
                "XPathResult dell''''espressione XPath ''{0}'' ha un XPathResultType di {1} che non pu\u00f2 essere convertito in un numero"},

  //BEGIN:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  /** Field ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED                       */

  { ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED,
       "Funzione di estensione: Impossibile richiamare ''{0}'' quando la funzione XMLConstants.FEATURE_SECURE_PROCESSING \u00e8 impostata su true."},

  /** Field ER_RESOLVE_VARIABLE_RETURNS_NULL                       */

  { ER_RESOLVE_VARIABLE_RETURNS_NULL,
       "resolveVariable per la variabile {0} che restituisce null"},

  /** Field ER_UNSUPPORTED_RETURN_TYPE                       */

  { ER_UNSUPPORTED_RETURN_TYPE,
       "Tipo di ritorno non supportato : {0}"},

  /** Field ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL                       */

  { ER_SOURCE_RETURN_TYPE_CANNOT_BE_NULL,
       "Il tipo origine e/o ritorno non pu\u00f2 essere nullo"},

  /** Field ER_ARG_CANNOT_BE_NULL                       */

  { ER_ARG_CANNOT_BE_NULL,
       "L''''argomento {0} non pu\u00f2 essere nullo"},

  /** Field ER_OBJECT_MODEL_NULL                       */

  { ER_OBJECT_MODEL_NULL,
       "{0}#isObjectModelSupported( String objectModel ) non pu\u00f2 essere chiamato con objectModel == null"},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_OBJECT_MODEL_EMPTY,
       "{0}#isObjectModelSupported( String objectModel ) non pu\u00f2 essere chiamato con objectModel == \"\""},

  /** Field ER_OBJECT_MODEL_EMPTY                       */

  { ER_FEATURE_NAME_NULL,
       "Tentativo di impostare una funzione con un nome nullo: {0}#setFeature( null, {1})"},

  /** Field ER_FEATURE_UNKNOWN                       */

  { ER_FEATURE_UNKNOWN,
       "Tentativo di impostare una funzione sconosciuta \"{0}\":{1}#setFeature({0},{2})"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_NULL_FEATURE,
       "Tentativo di ottenere una funzione con un nome nullo: {0}#getFeature(null)"},

  /** Field ER_GETTING_NULL_FEATURE                       */

  { ER_GETTING_UNKNOWN_FEATURE,
       "Tentativo di ottenere una funzione sconosciuta \"{0}\":{1}#getFeature({0})"},

  /** Field ER_NULL_XPATH_FUNCTION_RESOLVER                       */

  { ER_NULL_XPATH_FUNCTION_RESOLVER,
       "Tentativo di impostare un XPathFunctionResolver:{0}#setXPathFunctionResolver(null) nullo"},

  /** Field ER_NULL_XPATH_VARIABLE_RESOLVER                       */

  { ER_NULL_XPATH_VARIABLE_RESOLVER,
       "Tentativo di impostare una XPathVariableResolver:{0}#setXPathVariableResolver(null) nulla"},

  //END:  Definitions of error keys used  in exception messages of  JAXP 1.3 XPath API implementation

  // Warnings...

  { WG_LOCALE_NAME_NOT_HANDLED,
      "nome locale nella funzione format-number non ancora gestito."},

  { WG_PROPERTY_NOT_SUPPORTED,
      "Propriet\u00e0 XSL non supportata: {0}"},

  { WG_DONT_DO_ANYTHING_WITH_NS,
      "Non eseguire alcune azione per lo namespace {0} nella propriet\u00e0: {1}"},

  { WG_SECURITY_EXCEPTION,
      "SecurityException durante il tentativo di accesso alla propriet\u00e0 di sistema XSL: {0}"},

  { WG_QUO_NO_LONGER_DEFINED,
      "Sintassi obsoleta: quo(...) non \u00e8 pi\u00f9 definito in XPath."},

  { WG_NEED_DERIVED_OBJECT_TO_IMPLEMENT_NODETEST,
      "XPath richiede un oggetto derivato per implementare nodeTest!"},

  { WG_FUNCTION_TOKEN_NOT_FOUND,
      "token funzione non trovato."},

  { WG_COULDNOT_FIND_FUNCTION,
      "Impossibile trovare la funzione: {0}"},

  { WG_CANNOT_MAKE_URL_FROM,
      "Impossibile ricavare l''''URL da: {0}"},

  { WG_EXPAND_ENTITIES_NOT_SUPPORTED,
      "Opzione -E non supportata per il parser DTM"},

  { WG_ILLEGAL_VARIABLE_REFERENCE,
      "VariableReference fornito per la variabile \u00e8 fuori contesto o senza definizione.  Nome = {0}"},

  { WG_UNSUPPORTED_ENCODING,
     "Codifica non supportata: {0}"},



  // Other miscellaneous text used inside the code...
  { "ui_language", "it"},
  { "help_language", "it"},
  { "language", "it"},
  { "BAD_CODE", "Il parametro per createMessage fuori limite"},
  { "FORMAT_FAILED", "Rilevata eccezione durante la chiamata messageFormat"},
  { "version", ">>>>>>> Versione Xalan "},
  { "version2", "<<<<<<<"},
  { "yes", "s\u00ec"},
  { "line", "Riga #"},
  { "column", "Colonna #"},
  { "xsldone", "XSLProcessor: eseguito"},
  { "xpath_option", "opzioni xpath: "},
  { "optionIN", "   [-in inputXMLURL]"},
  { "optionSelect", "   [-select espressione xpath]"},
  { "optionMatch", "   [-match associa il modello (per le diagnostiche di corrispondenza)]"},
  { "optionAnyExpr", "Oppure per un'espressione xpath eseguir\u00e0 un dump diagnostico"},
  { "noParsermsg1", "Elaborazione XSL non riuscita."},
  { "noParsermsg2", "** Impossibile trovare il parser **"},
  { "noParsermsg3", "Controllare il classpath."},
  { "noParsermsg4", "Se non si possiede IBM XML Parser per Java, \u00e8 possibile scaricarlo da"},
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
  public static final String ERROR_STRING = "#errore";

  /** Field ERROR_HEADER          */
  public static final String ERROR_HEADER = "Errore: ";

  /** Field WARNING_HEADER          */
  public static final String WARNING_HEADER = "Avvertenza: ";

  /** Field XSL_HEADER          */
  public static final String XSL_HEADER = "XSL ";

  /** Field XML_HEADER          */
  public static final String XML_HEADER = "XML ";

  /** Field QUERY_HEADER          */
  public static final String QUERY_HEADER = "MODELLO ";


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
                new Locale("it", "IT"));
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
