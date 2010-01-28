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
 * $Id: XSLTErrorResources_fr.java 468641 2006-10-28 06:54:42Z minchau $
 */
package org.apache.xalan.res;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Set up error messages.
 * We build a two dimensional array of message keys and
 * message strings. In order to add a new message here,
 * you need to first add a String constant. And
 *  you need to enter key , value pair as part of contents
 * Array. You also need to update MAX_CODE for error strings
 * and MAX_WARNING for warnings ( Needed for only information
 * purpose )
 */
public class XSLTErrorResources_fr extends ListResourceBundle
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

  /** Maximum error messages, this is needed to keep track of the number of messages.    */
  public static final int MAX_CODE = 201;

  /** Maximum warnings, this is needed to keep track of the number of warnings.          */
  public static final int MAX_WARNING = 29;

  /** Maximum misc strings.   */
  public static final int MAX_OTHERS = 55;

  /** Maximum total warnings and error messages.          */
  public static final int MAX_MESSAGES = MAX_CODE + MAX_WARNING + 1;


  /*
   * Static variables
   */
  public static final String ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX =
        "ER_INVALID_SET_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX";

  public static final String ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT =
        "ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT";

  public static final String ER_NO_CURLYBRACE = "ER_NO_CURLYBRACE";
  public static final String ER_FUNCTION_NOT_SUPPORTED = "ER_FUNCTION_NOT_SUPPORTED";
  public static final String ER_ILLEGAL_ATTRIBUTE = "ER_ILLEGAL_ATTRIBUTE";
  public static final String ER_NULL_SOURCENODE_APPLYIMPORTS = "ER_NULL_SOURCENODE_APPLYIMPORTS";
  public static final String ER_CANNOT_ADD = "ER_CANNOT_ADD";
  public static final String ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES="ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES";
  public static final String ER_NO_NAME_ATTRIB = "ER_NO_NAME_ATTRIB";
  public static final String ER_TEMPLATE_NOT_FOUND = "ER_TEMPLATE_NOT_FOUND";
  public static final String ER_CANT_RESOLVE_NAME_AVT = "ER_CANT_RESOLVE_NAME_AVT";
  public static final String ER_REQUIRES_ATTRIB = "ER_REQUIRES_ATTRIB";
  public static final String ER_MUST_HAVE_TEST_ATTRIB = "ER_MUST_HAVE_TEST_ATTRIB";
  public static final String ER_BAD_VAL_ON_LEVEL_ATTRIB =
         "ER_BAD_VAL_ON_LEVEL_ATTRIB";
  public static final String ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML =
         "ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML";
  public static final String ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME =
         "ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME";
  public static final String ER_NEED_MATCH_ATTRIB = "ER_NEED_MATCH_ATTRIB";
  public static final String ER_NEED_NAME_OR_MATCH_ATTRIB =
         "ER_NEED_NAME_OR_MATCH_ATTRIB";
  public static final String ER_CANT_RESOLVE_NSPREFIX =
         "ER_CANT_RESOLVE_NSPREFIX";
  public static final String ER_ILLEGAL_VALUE = "ER_ILLEGAL_VALUE";
  public static final String ER_NO_OWNERDOC = "ER_NO_OWNERDOC";
  public static final String ER_ELEMTEMPLATEELEM_ERR ="ER_ELEMTEMPLATEELEM_ERR";
  public static final String ER_NULL_CHILD = "ER_NULL_CHILD";
  public static final String ER_NEED_SELECT_ATTRIB = "ER_NEED_SELECT_ATTRIB";
  public static final String ER_NEED_TEST_ATTRIB = "ER_NEED_TEST_ATTRIB";
  public static final String ER_NEED_NAME_ATTRIB = "ER_NEED_NAME_ATTRIB";
  public static final String ER_NO_CONTEXT_OWNERDOC = "ER_NO_CONTEXT_OWNERDOC";
  public static final String ER_COULD_NOT_CREATE_XML_PROC_LIAISON =
         "ER_COULD_NOT_CREATE_XML_PROC_LIAISON";
  public static final String ER_PROCESS_NOT_SUCCESSFUL =
         "ER_PROCESS_NOT_SUCCESSFUL";
  public static final String ER_NOT_SUCCESSFUL = "ER_NOT_SUCCESSFUL";
  public static final String ER_ENCODING_NOT_SUPPORTED =
         "ER_ENCODING_NOT_SUPPORTED";
  public static final String ER_COULD_NOT_CREATE_TRACELISTENER =
         "ER_COULD_NOT_CREATE_TRACELISTENER";
  public static final String ER_KEY_REQUIRES_NAME_ATTRIB =
         "ER_KEY_REQUIRES_NAME_ATTRIB";
  public static final String ER_KEY_REQUIRES_MATCH_ATTRIB =
         "ER_KEY_REQUIRES_MATCH_ATTRIB";
  public static final String ER_KEY_REQUIRES_USE_ATTRIB =
         "ER_KEY_REQUIRES_USE_ATTRIB";
  public static final String ER_REQUIRES_ELEMENTS_ATTRIB =
         "ER_REQUIRES_ELEMENTS_ATTRIB";
  public static final String ER_MISSING_PREFIX_ATTRIB =
         "ER_MISSING_PREFIX_ATTRIB";
  public static final String ER_BAD_STYLESHEET_URL = "ER_BAD_STYLESHEET_URL";
  public static final String ER_FILE_NOT_FOUND = "ER_FILE_NOT_FOUND";
  public static final String ER_IOEXCEPTION = "ER_IOEXCEPTION";
  public static final String ER_NO_HREF_ATTRIB = "ER_NO_HREF_ATTRIB";
  public static final String ER_STYLESHEET_INCLUDES_ITSELF =
         "ER_STYLESHEET_INCLUDES_ITSELF";
  public static final String ER_PROCESSINCLUDE_ERROR ="ER_PROCESSINCLUDE_ERROR";
  public static final String ER_MISSING_LANG_ATTRIB = "ER_MISSING_LANG_ATTRIB";
  public static final String ER_MISSING_CONTAINER_ELEMENT_COMPONENT =
         "ER_MISSING_CONTAINER_ELEMENT_COMPONENT";
  public static final String ER_CAN_ONLY_OUTPUT_TO_ELEMENT =
         "ER_CAN_ONLY_OUTPUT_TO_ELEMENT";
  public static final String ER_PROCESS_ERROR = "ER_PROCESS_ERROR";
  public static final String ER_UNIMPLNODE_ERROR = "ER_UNIMPLNODE_ERROR";
  public static final String ER_NO_SELECT_EXPRESSION ="ER_NO_SELECT_EXPRESSION";
  public static final String ER_CANNOT_SERIALIZE_XSLPROCESSOR =
         "ER_CANNOT_SERIALIZE_XSLPROCESSOR";
  public static final String ER_NO_INPUT_STYLESHEET = "ER_NO_INPUT_STYLESHEET";
  public static final String ER_FAILED_PROCESS_STYLESHEET =
         "ER_FAILED_PROCESS_STYLESHEET";
  public static final String ER_COULDNT_PARSE_DOC = "ER_COULDNT_PARSE_DOC";
  public static final String ER_COULDNT_FIND_FRAGMENT =
         "ER_COULDNT_FIND_FRAGMENT";
  public static final String ER_NODE_NOT_ELEMENT = "ER_NODE_NOT_ELEMENT";
  public static final String ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB =
         "ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB";
  public static final String ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB =
         "ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB";
  public static final String ER_NO_CLONE_OF_DOCUMENT_FRAG =
         "ER_NO_CLONE_OF_DOCUMENT_FRAG";
  public static final String ER_CANT_CREATE_ITEM = "ER_CANT_CREATE_ITEM";
  public static final String ER_XMLSPACE_ILLEGAL_VALUE =
         "ER_XMLSPACE_ILLEGAL_VALUE";
  public static final String ER_NO_XSLKEY_DECLARATION =
         "ER_NO_XSLKEY_DECLARATION";
  public static final String ER_CANT_CREATE_URL = "ER_CANT_CREATE_URL";
  public static final String ER_XSLFUNCTIONS_UNSUPPORTED =
         "ER_XSLFUNCTIONS_UNSUPPORTED";
  public static final String ER_PROCESSOR_ERROR = "ER_PROCESSOR_ERROR";
  public static final String ER_NOT_ALLOWED_INSIDE_STYLESHEET =
         "ER_NOT_ALLOWED_INSIDE_STYLESHEET";
  public static final String ER_RESULTNS_NOT_SUPPORTED =
         "ER_RESULTNS_NOT_SUPPORTED";
  public static final String ER_DEFAULTSPACE_NOT_SUPPORTED =
         "ER_DEFAULTSPACE_NOT_SUPPORTED";
  public static final String ER_INDENTRESULT_NOT_SUPPORTED =
         "ER_INDENTRESULT_NOT_SUPPORTED";
  public static final String ER_ILLEGAL_ATTRIB = "ER_ILLEGAL_ATTRIB";
  public static final String ER_UNKNOWN_XSL_ELEM = "ER_UNKNOWN_XSL_ELEM";
  public static final String ER_BAD_XSLSORT_USE = "ER_BAD_XSLSORT_USE";
  public static final String ER_MISPLACED_XSLWHEN = "ER_MISPLACED_XSLWHEN";
  public static final String ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE =
         "ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE";
  public static final String ER_MISPLACED_XSLOTHERWISE =
         "ER_MISPLACED_XSLOTHERWISE";
  public static final String ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE =
         "ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE";
  public static final String ER_NOT_ALLOWED_INSIDE_TEMPLATE =
         "ER_NOT_ALLOWED_INSIDE_TEMPLATE";
  public static final String ER_UNKNOWN_EXT_NS_PREFIX =
         "ER_UNKNOWN_EXT_NS_PREFIX";
  public static final String ER_IMPORTS_AS_FIRST_ELEM =
         "ER_IMPORTS_AS_FIRST_ELEM";
  public static final String ER_IMPORTING_ITSELF = "ER_IMPORTING_ITSELF";
  public static final String ER_XMLSPACE_ILLEGAL_VAL ="ER_XMLSPACE_ILLEGAL_VAL";
  public static final String ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL =
         "ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL";
  public static final String ER_SAX_EXCEPTION = "ER_SAX_EXCEPTION";
  public static final String ER_XSLT_ERROR = "ER_XSLT_ERROR";
  public static final String ER_CURRENCY_SIGN_ILLEGAL=
         "ER_CURRENCY_SIGN_ILLEGAL";
  public static final String ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM =
         "ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM";
  public static final String ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER =
         "ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER";
  public static final String ER_REDIRECT_COULDNT_GET_FILENAME =
         "ER_REDIRECT_COULDNT_GET_FILENAME";
  public static final String ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT =
         "ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT";
  public static final String ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX =
         "ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX";
  public static final String ER_MISSING_NS_URI = "ER_MISSING_NS_URI";
  public static final String ER_MISSING_ARG_FOR_OPTION =
         "ER_MISSING_ARG_FOR_OPTION";
  public static final String ER_INVALID_OPTION = "ER_INVALID_OPTION";
  public static final String ER_MALFORMED_FORMAT_STRING =
         "ER_MALFORMED_FORMAT_STRING";
  public static final String ER_STYLESHEET_REQUIRES_VERSION_ATTRIB =
         "ER_STYLESHEET_REQUIRES_VERSION_ATTRIB";
  public static final String ER_ILLEGAL_ATTRIBUTE_VALUE =
         "ER_ILLEGAL_ATTRIBUTE_VALUE";
  public static final String ER_CHOOSE_REQUIRES_WHEN ="ER_CHOOSE_REQUIRES_WHEN";
  public static final String ER_NO_APPLY_IMPORT_IN_FOR_EACH =
         "ER_NO_APPLY_IMPORT_IN_FOR_EACH";
  public static final String ER_CANT_USE_DTM_FOR_OUTPUT =
         "ER_CANT_USE_DTM_FOR_OUTPUT";
  public static final String ER_CANT_USE_DTM_FOR_INPUT =
         "ER_CANT_USE_DTM_FOR_INPUT";
  public static final String ER_CALL_TO_EXT_FAILED = "ER_CALL_TO_EXT_FAILED";
  public static final String ER_PREFIX_MUST_RESOLVE = "ER_PREFIX_MUST_RESOLVE";
  public static final String ER_INVALID_UTF16_SURROGATE =
         "ER_INVALID_UTF16_SURROGATE";
  public static final String ER_XSLATTRSET_USED_ITSELF =
         "ER_XSLATTRSET_USED_ITSELF";
  public static final String ER_CANNOT_MIX_XERCESDOM ="ER_CANNOT_MIX_XERCESDOM";
  public static final String ER_TOO_MANY_LISTENERS = "ER_TOO_MANY_LISTENERS";
  public static final String ER_IN_ELEMTEMPLATEELEM_READOBJECT =
         "ER_IN_ELEMTEMPLATEELEM_READOBJECT";
  public static final String ER_DUPLICATE_NAMED_TEMPLATE =
         "ER_DUPLICATE_NAMED_TEMPLATE";
  public static final String ER_INVALID_KEY_CALL = "ER_INVALID_KEY_CALL";
  public static final String ER_REFERENCING_ITSELF = "ER_REFERENCING_ITSELF";
  public static final String ER_ILLEGAL_DOMSOURCE_INPUT =
         "ER_ILLEGAL_DOMSOURCE_INPUT";
  public static final String ER_CLASS_NOT_FOUND_FOR_OPTION =
         "ER_CLASS_NOT_FOUND_FOR_OPTION";
  public static final String ER_REQUIRED_ELEM_NOT_FOUND =
         "ER_REQUIRED_ELEM_NOT_FOUND";
  public static final String ER_INPUT_CANNOT_BE_NULL ="ER_INPUT_CANNOT_BE_NULL";
  public static final String ER_URI_CANNOT_BE_NULL = "ER_URI_CANNOT_BE_NULL";
  public static final String ER_FILE_CANNOT_BE_NULL = "ER_FILE_CANNOT_BE_NULL";
  public static final String ER_SOURCE_CANNOT_BE_NULL =
         "ER_SOURCE_CANNOT_BE_NULL";
  public static final String ER_CANNOT_INIT_BSFMGR = "ER_CANNOT_INIT_BSFMGR";
  public static final String ER_CANNOT_CMPL_EXTENSN = "ER_CANNOT_CMPL_EXTENSN";
  public static final String ER_CANNOT_CREATE_EXTENSN =
         "ER_CANNOT_CREATE_EXTENSN";
  public static final String ER_INSTANCE_MTHD_CALL_REQUIRES =
         "ER_INSTANCE_MTHD_CALL_REQUIRES";
  public static final String ER_INVALID_ELEMENT_NAME ="ER_INVALID_ELEMENT_NAME";
  public static final String ER_ELEMENT_NAME_METHOD_STATIC =
         "ER_ELEMENT_NAME_METHOD_STATIC";
  public static final String ER_EXTENSION_FUNC_UNKNOWN =
         "ER_EXTENSION_FUNC_UNKNOWN";
  public static final String ER_MORE_MATCH_CONSTRUCTOR =
         "ER_MORE_MATCH_CONSTRUCTOR";
  public static final String ER_MORE_MATCH_METHOD = "ER_MORE_MATCH_METHOD";
  public static final String ER_MORE_MATCH_ELEMENT = "ER_MORE_MATCH_ELEMENT";
  public static final String ER_INVALID_CONTEXT_PASSED =
         "ER_INVALID_CONTEXT_PASSED";
  public static final String ER_POOL_EXISTS = "ER_POOL_EXISTS";
  public static final String ER_NO_DRIVER_NAME = "ER_NO_DRIVER_NAME";
  public static final String ER_NO_URL = "ER_NO_URL";
  public static final String ER_POOL_SIZE_LESSTHAN_ONE =
         "ER_POOL_SIZE_LESSTHAN_ONE";
  public static final String ER_INVALID_DRIVER = "ER_INVALID_DRIVER";
  public static final String ER_NO_STYLESHEETROOT = "ER_NO_STYLESHEETROOT";
  public static final String ER_ILLEGAL_XMLSPACE_VALUE =
         "ER_ILLEGAL_XMLSPACE_VALUE";
  public static final String ER_PROCESSFROMNODE_FAILED =
         "ER_PROCESSFROMNODE_FAILED";
  public static final String ER_RESOURCE_COULD_NOT_LOAD =
         "ER_RESOURCE_COULD_NOT_LOAD";
  public static final String ER_BUFFER_SIZE_LESSTHAN_ZERO =
         "ER_BUFFER_SIZE_LESSTHAN_ZERO";
  public static final String ER_UNKNOWN_ERROR_CALLING_EXTENSION =
         "ER_UNKNOWN_ERROR_CALLING_EXTENSION";
  public static final String ER_NO_NAMESPACE_DECL = "ER_NO_NAMESPACE_DECL";
  public static final String ER_ELEM_CONTENT_NOT_ALLOWED =
         "ER_ELEM_CONTENT_NOT_ALLOWED";
  public static final String ER_STYLESHEET_DIRECTED_TERMINATION =
         "ER_STYLESHEET_DIRECTED_TERMINATION";
  public static final String ER_ONE_OR_TWO = "ER_ONE_OR_TWO";
  public static final String ER_TWO_OR_THREE = "ER_TWO_OR_THREE";
  public static final String ER_COULD_NOT_LOAD_RESOURCE =
         "ER_COULD_NOT_LOAD_RESOURCE";
  public static final String ER_CANNOT_INIT_DEFAULT_TEMPLATES =
         "ER_CANNOT_INIT_DEFAULT_TEMPLATES";
  public static final String ER_RESULT_NULL = "ER_RESULT_NULL";
  public static final String ER_RESULT_COULD_NOT_BE_SET =
         "ER_RESULT_COULD_NOT_BE_SET";
  public static final String ER_NO_OUTPUT_SPECIFIED = "ER_NO_OUTPUT_SPECIFIED";
  public static final String ER_CANNOT_TRANSFORM_TO_RESULT_TYPE =
         "ER_CANNOT_TRANSFORM_TO_RESULT_TYPE";
  public static final String ER_CANNOT_TRANSFORM_SOURCE_TYPE =
         "ER_CANNOT_TRANSFORM_SOURCE_TYPE";
  public static final String ER_NULL_CONTENT_HANDLER ="ER_NULL_CONTENT_HANDLER";
  public static final String ER_NULL_ERROR_HANDLER = "ER_NULL_ERROR_HANDLER";
  public static final String ER_CANNOT_CALL_PARSE = "ER_CANNOT_CALL_PARSE";
  public static final String ER_NO_PARENT_FOR_FILTER ="ER_NO_PARENT_FOR_FILTER";
  public static final String ER_NO_STYLESHEET_IN_MEDIA =
         "ER_NO_STYLESHEET_IN_MEDIA";
  public static final String ER_NO_STYLESHEET_PI = "ER_NO_STYLESHEET_PI";
  public static final String ER_NOT_SUPPORTED = "ER_NOT_SUPPORTED";
  public static final String ER_PROPERTY_VALUE_BOOLEAN =
         "ER_PROPERTY_VALUE_BOOLEAN";
  public static final String ER_COULD_NOT_FIND_EXTERN_SCRIPT =
         "ER_COULD_NOT_FIND_EXTERN_SCRIPT";
  public static final String ER_RESOURCE_COULD_NOT_FIND =
         "ER_RESOURCE_COULD_NOT_FIND";
  public static final String ER_OUTPUT_PROPERTY_NOT_RECOGNIZED =
         "ER_OUTPUT_PROPERTY_NOT_RECOGNIZED";
  public static final String ER_FAILED_CREATING_ELEMLITRSLT =
         "ER_FAILED_CREATING_ELEMLITRSLT";
  public static final String ER_VALUE_SHOULD_BE_NUMBER =
         "ER_VALUE_SHOULD_BE_NUMBER";
  public static final String ER_VALUE_SHOULD_EQUAL = "ER_VALUE_SHOULD_EQUAL";
  public static final String ER_FAILED_CALLING_METHOD =
         "ER_FAILED_CALLING_METHOD";
  public static final String ER_FAILED_CREATING_ELEMTMPL =
         "ER_FAILED_CREATING_ELEMTMPL";
  public static final String ER_CHARS_NOT_ALLOWED = "ER_CHARS_NOT_ALLOWED";
  public static final String ER_ATTR_NOT_ALLOWED = "ER_ATTR_NOT_ALLOWED";
  public static final String ER_BAD_VALUE = "ER_BAD_VALUE";
  public static final String ER_ATTRIB_VALUE_NOT_FOUND =
         "ER_ATTRIB_VALUE_NOT_FOUND";
  public static final String ER_ATTRIB_VALUE_NOT_RECOGNIZED =
         "ER_ATTRIB_VALUE_NOT_RECOGNIZED";
  public static final String ER_NULL_URI_NAMESPACE = "ER_NULL_URI_NAMESPACE";
  public static final String ER_NUMBER_TOO_BIG = "ER_NUMBER_TOO_BIG";
  public static final String  ER_CANNOT_FIND_SAX1_DRIVER =
         "ER_CANNOT_FIND_SAX1_DRIVER";
  public static final String  ER_SAX1_DRIVER_NOT_LOADED =
         "ER_SAX1_DRIVER_NOT_LOADED";
  public static final String  ER_SAX1_DRIVER_NOT_INSTANTIATED =
         "ER_SAX1_DRIVER_NOT_INSTANTIATED" ;
  public static final String ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER =
         "ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER";
  public static final String  ER_PARSER_PROPERTY_NOT_SPECIFIED =
         "ER_PARSER_PROPERTY_NOT_SPECIFIED";
  public static final String  ER_PARSER_ARG_CANNOT_BE_NULL =
         "ER_PARSER_ARG_CANNOT_BE_NULL" ;
  public static final String  ER_FEATURE = "ER_FEATURE";
  public static final String ER_PROPERTY = "ER_PROPERTY" ;
  public static final String ER_NULL_ENTITY_RESOLVER ="ER_NULL_ENTITY_RESOLVER";
  public static final String  ER_NULL_DTD_HANDLER = "ER_NULL_DTD_HANDLER" ;
  public static final String ER_NO_DRIVER_NAME_SPECIFIED =
         "ER_NO_DRIVER_NAME_SPECIFIED";
  public static final String ER_NO_URL_SPECIFIED = "ER_NO_URL_SPECIFIED";
  public static final String ER_POOLSIZE_LESS_THAN_ONE =
         "ER_POOLSIZE_LESS_THAN_ONE";
  public static final String ER_INVALID_DRIVER_NAME = "ER_INVALID_DRIVER_NAME";
  public static final String ER_ERRORLISTENER = "ER_ERRORLISTENER";
  public static final String ER_ASSERT_NO_TEMPLATE_PARENT =
         "ER_ASSERT_NO_TEMPLATE_PARENT";
  public static final String ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR =
         "ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR";
  public static final String ER_NOT_ALLOWED_IN_POSITION =
         "ER_NOT_ALLOWED_IN_POSITION";
  public static final String ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION =
         "ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION";
  public static final String ER_NAMESPACE_CONTEXT_NULL_NAMESPACE =
         "ER_NAMESPACE_CONTEXT_NULL_NAMESPACE";
  public static final String ER_NAMESPACE_CONTEXT_NULL_PREFIX =
         "ER_NAMESPACE_CONTEXT_NULL_PREFIX";
  public static final String ER_XPATH_RESOLVER_NULL_QNAME =
         "ER_XPATH_RESOLVER_NULL_QNAME";
  public static final String ER_XPATH_RESOLVER_NEGATIVE_ARITY =
         "ER_XPATH_RESOLVER_NEGATIVE_ARITY";
  public static final String INVALID_TCHAR = "INVALID_TCHAR";
  public static final String INVALID_QNAME = "INVALID_QNAME";
  public static final String INVALID_ENUM = "INVALID_ENUM";
  public static final String INVALID_NMTOKEN = "INVALID_NMTOKEN";
  public static final String INVALID_NCNAME = "INVALID_NCNAME";
  public static final String INVALID_BOOLEAN = "INVALID_BOOLEAN";
  public static final String INVALID_NUMBER = "INVALID_NUMBER";
  public static final String ER_ARG_LITERAL = "ER_ARG_LITERAL";
  public static final String ER_DUPLICATE_GLOBAL_VAR ="ER_DUPLICATE_GLOBAL_VAR";
  public static final String ER_DUPLICATE_VAR = "ER_DUPLICATE_VAR";
  public static final String ER_TEMPLATE_NAME_MATCH = "ER_TEMPLATE_NAME_MATCH";
  public static final String ER_INVALID_PREFIX = "ER_INVALID_PREFIX";
  public static final String ER_NO_ATTRIB_SET = "ER_NO_ATTRIB_SET";
  public static final String ER_FUNCTION_NOT_FOUND =
         "ER_FUNCTION_NOT_FOUND";
  public static final String ER_CANT_HAVE_CONTENT_AND_SELECT =
     "ER_CANT_HAVE_CONTENT_AND_SELECT";
  public static final String ER_INVALID_SET_PARAM_VALUE = "ER_INVALID_SET_PARAM_VALUE";
  public static final String ER_SET_FEATURE_NULL_NAME =
        "ER_SET_FEATURE_NULL_NAME";
  public static final String ER_GET_FEATURE_NULL_NAME =
        "ER_GET_FEATURE_NULL_NAME";
  public static final String ER_UNSUPPORTED_FEATURE =
        "ER_UNSUPPORTED_FEATURE";
  public static final String ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING =
        "ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING";

  public static final String WG_FOUND_CURLYBRACE = "WG_FOUND_CURLYBRACE";
  public static final String WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR =
         "WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR";
  public static final String WG_EXPR_ATTRIB_CHANGED_TO_SELECT =
         "WG_EXPR_ATTRIB_CHANGED_TO_SELECT";
  public static final String WG_NO_LOCALE_IN_FORMATNUMBER =
         "WG_NO_LOCALE_IN_FORMATNUMBER";
  public static final String WG_LOCALE_NOT_FOUND = "WG_LOCALE_NOT_FOUND";
  public static final String WG_CANNOT_MAKE_URL_FROM ="WG_CANNOT_MAKE_URL_FROM";
  public static final String WG_CANNOT_LOAD_REQUESTED_DOC =
         "WG_CANNOT_LOAD_REQUESTED_DOC";
  public static final String WG_CANNOT_FIND_COLLATOR ="WG_CANNOT_FIND_COLLATOR";
  public static final String WG_FUNCTIONS_SHOULD_USE_URL =
         "WG_FUNCTIONS_SHOULD_USE_URL";
  public static final String WG_ENCODING_NOT_SUPPORTED_USING_UTF8 =
         "WG_ENCODING_NOT_SUPPORTED_USING_UTF8";
  public static final String WG_ENCODING_NOT_SUPPORTED_USING_JAVA =
         "WG_ENCODING_NOT_SUPPORTED_USING_JAVA";
  public static final String WG_SPECIFICITY_CONFLICTS =
         "WG_SPECIFICITY_CONFLICTS";
  public static final String WG_PARSING_AND_PREPARING =
         "WG_PARSING_AND_PREPARING";
  public static final String WG_ATTR_TEMPLATE = "WG_ATTR_TEMPLATE";
  public static final String WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE = "WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESP";
  public static final String WG_ATTRIB_NOT_HANDLED = "WG_ATTRIB_NOT_HANDLED";
  public static final String WG_NO_DECIMALFORMAT_DECLARATION =
         "WG_NO_DECIMALFORMAT_DECLARATION";
  public static final String WG_OLD_XSLT_NS = "WG_OLD_XSLT_NS";
  public static final String WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED =
         "WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED";
  public static final String WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE =
         "WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE";
  public static final String WG_ILLEGAL_ATTRIBUTE = "WG_ILLEGAL_ATTRIBUTE";
  public static final String WG_COULD_NOT_RESOLVE_PREFIX =
         "WG_COULD_NOT_RESOLVE_PREFIX";
  public static final String WG_STYLESHEET_REQUIRES_VERSION_ATTRIB =
         "WG_STYLESHEET_REQUIRES_VERSION_ATTRIB";
  public static final String WG_ILLEGAL_ATTRIBUTE_NAME =
         "WG_ILLEGAL_ATTRIBUTE_NAME";
  public static final String WG_ILLEGAL_ATTRIBUTE_VALUE =
         "WG_ILLEGAL_ATTRIBUTE_VALUE";
  public static final String WG_EMPTY_SECOND_ARG = "WG_EMPTY_SECOND_ARG";
  public static final String WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML =
         "WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML";
  public static final String WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME =
         "WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME";
  public static final String WG_ILLEGAL_ATTRIBUTE_POSITION =
         "WG_ILLEGAL_ATTRIBUTE_POSITION";
  public static final String NO_MODIFICATION_ALLOWED_ERR =
         "NO_MODIFICATION_ALLOWED_ERR";

  /*
   * Now fill in the message text.
   * Then fill in the message text for that message code in the
   * array. Use the new error code as the index into the array.
   */

  // Error messages...

  /** Get the lookup table for error messages.
   *
   * @return The message lookup table.
   */
  public Object[][] getContents()
  {
    return new Object[][] {

  /** Error message ID that has a null message, but takes in a single object.    */
  {"ER0000" , "{0}" },


    { ER_NO_CURLYBRACE,
      "Erreur : '{' interdit dans une expression"},

    { ER_ILLEGAL_ATTRIBUTE ,
     "{0} comporte un attribut incorrect : {1}"},

  {ER_NULL_SOURCENODE_APPLYIMPORTS ,
      "sourceNode est vide dans xsl:apply-imports !"},

  {ER_CANNOT_ADD,
      "Impossible d''ajouter {0} \u00e0 {1}"},

    { ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES,
      "sourceNode est vide dans handleApplyTemplatesInstruction !"},

    { ER_NO_NAME_ATTRIB,
     "{0} doit poss\u00e9der un attribut de nom."},

    {ER_TEMPLATE_NOT_FOUND,
     "Impossible de trouver le mod\u00e8le : {0}"},

    {ER_CANT_RESOLVE_NAME_AVT,
      "Impossible de convertir l'AVT du nom dans xsl:call-template."},

    {ER_REQUIRES_ATTRIB,
     "{0} requiert l''attribut : {1}"},

    { ER_MUST_HAVE_TEST_ATTRIB,
      "{0} doit avoir un attribut ''test''."},

    {ER_BAD_VAL_ON_LEVEL_ATTRIB,
      "Valeur erron\u00e9e dans l''attribut de niveau : {0}"},

    {ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "Le nom de l'instruction de traitement ne peut \u00eatre 'xml'"},

    { ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "Le nom de l''instruction de traitement doit \u00eatre un NCName valide : {0}"},

    { ER_NEED_MATCH_ATTRIB,
      "{0} doit poss\u00e9der un attribut de correspondance s''il poss\u00e8de un mode."},

    { ER_NEED_NAME_OR_MATCH_ATTRIB,
      "{0} requiert un nom ou un attribut de correspondance."},

    {ER_CANT_RESOLVE_NSPREFIX,
      "Impossible de r\u00e9soudre le pr\u00e9fixe de l''espace de noms : {0}"},

    { ER_ILLEGAL_VALUE,
     "xml:space comporte une valeur non valide : {0}"},

    { ER_NO_OWNERDOC,
      "Le noeud enfant ne poss\u00e8de pas de document propri\u00e9taire !"},

    { ER_ELEMTEMPLATEELEM_ERR,
     "Erreur de ElemTemplateElement : {0}"},

    { ER_NULL_CHILD,
     "Tentative d'ajout d'un enfant vide !"},

    { ER_NEED_SELECT_ATTRIB,
     "{0} requiert un attribut de s\u00e9lection."},

    { ER_NEED_TEST_ATTRIB ,
      "xsl:when doit poss\u00e9der un attribut 'test'."},

    { ER_NEED_NAME_ATTRIB,
      "xsl:with-param doit poss\u00e9der un attribut 'name'."},

    { ER_NO_CONTEXT_OWNERDOC,
      "Le contexte ne poss\u00e8de pas de document propri\u00e9taire !"},

    {ER_COULD_NOT_CREATE_XML_PROC_LIAISON,
      "Impossible de cr\u00e9er XML TransformerFactory Liaison : {0}"},

    {ER_PROCESS_NOT_SUCCESSFUL,
      "Echec du processus Xalan."},

    { ER_NOT_SUCCESSFUL,
     "Echec de Xalan."},

    { ER_ENCODING_NOT_SUPPORTED,
     "Encodage non pris en charge : {0}"},

    {ER_COULD_NOT_CREATE_TRACELISTENER,
      "Impossible de cr\u00e9er TraceListener : {0}"},

    {ER_KEY_REQUIRES_NAME_ATTRIB,
      "xsl:key requiert un attribut 'name' !"},

    { ER_KEY_REQUIRES_MATCH_ATTRIB,
      "xsl:key requiert un attribut 'match' !"},

    { ER_KEY_REQUIRES_USE_ATTRIB,
      "xsl:key requiert un attribut 'use' !"},

    { ER_REQUIRES_ELEMENTS_ATTRIB,
      "(StylesheetHandler) {0} requiert un attribut ''elements''"},

    { ER_MISSING_PREFIX_ATTRIB,
      "L''attribut ''prefix'' de (StylesheetHandler) {0} est manquant"},

    { ER_BAD_STYLESHEET_URL,
     "URL de la feuille de style erron\u00e9 : {0}"},

    { ER_FILE_NOT_FOUND,
     "Fichier de la feuille de style introuvable : {0}"},

    { ER_IOEXCEPTION,
      "Exception d''E-S avec le fichier de la feuille de style : {0}"},

    { ER_NO_HREF_ATTRIB,
      "(StylesheetHandler) Impossible de trouver d''attribut href pour {0}"},

    { ER_STYLESHEET_INCLUDES_ITSELF,
      "(StylesheetHandler) {0} est directement ou indirectement inclus dans lui-m\u00eame !"},

    { ER_PROCESSINCLUDE_ERROR,
      "Erreur de StylesheetHandler.processInclude, {0}"},

    { ER_MISSING_LANG_ATTRIB,
      "L''attribut ''lang'' de (StylesheetHandler) {0} est manquant"},

    { ER_MISSING_CONTAINER_ELEMENT_COMPONENT,
      "(StylesheetHandler) position de l''\u00e9l\u00e9ment {0} inad\u00e9quate ? El\u00e9ment ''component'' de conteneur manquant"},

    { ER_CAN_ONLY_OUTPUT_TO_ELEMENT,
      "Seule sortie possible vers Element, DocumentFragment, Document ou PrintWriter."},

    { ER_PROCESS_ERROR,
     "Erreur de StylesheetRoot.process"},

    { ER_UNIMPLNODE_ERROR,
     "Erreur de UnImplNode : {0}"},

    { ER_NO_SELECT_EXPRESSION,
      "Erreur ! Impossible de trouver l'expression de s\u00e9lection xpath (-select)."},

    { ER_CANNOT_SERIALIZE_XSLPROCESSOR,
      "Impossible de s\u00e9rialiser un XSLProcessor !"},

    { ER_NO_INPUT_STYLESHEET,
      "Entr\u00e9e de feuille de style non sp\u00e9cifi\u00e9e !"},

    { ER_FAILED_PROCESS_STYLESHEET,
      "Impossible de traiter la feuille de style !"},

    { ER_COULDNT_PARSE_DOC,
     "Impossible d''analyser le document {0} !"},

    { ER_COULDNT_FIND_FRAGMENT,
     "Impossible de trouver le fragment : {0}"},

    { ER_NODE_NOT_ELEMENT,
      "Le noeud d\u00e9sign\u00e9 par l''identificateur de fragment n''est pas un \u00e9l\u00e9ment : {0}"},

    { ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB,
      "for-each doit poss\u00e9der un attribut de correspondance ou de nom"},

    { ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB,
      "Les mod\u00e8les doivent poss\u00e9der un attribut de correspondance ou de nom"},

    { ER_NO_CLONE_OF_DOCUMENT_FRAG,
      "Pas de clone dans un fragment de document !"},

    { ER_CANT_CREATE_ITEM,
      "Impossible de cr\u00e9er l''\u00e9l\u00e9ment dans l''arborescence de r\u00e9sultats : {0}"},

    { ER_XMLSPACE_ILLEGAL_VALUE,
      "xml:space du source XML poss\u00e8de une valeur incorrecte : {0}"},

    { ER_NO_XSLKEY_DECLARATION,
      "Aucune d\u00e9claration xsl:key pour {0} !"},

    { ER_CANT_CREATE_URL,
     "Erreur ! Impossible de cr\u00e9er une URL pour : {0}"},

    { ER_XSLFUNCTIONS_UNSUPPORTED,
     "xsl:functions n'est pas pris en charge"},

    { ER_PROCESSOR_ERROR,
     "Erreur TransformerFactory de XSLT"},

    { ER_NOT_ALLOWED_INSIDE_STYLESHEET,
      "(StylesheetHandler) {0} n''est pas pris en charge dans une feuille de style !"},

    { ER_RESULTNS_NOT_SUPPORTED,
      "result-ns n'est plus pris en charge !  Pr\u00e9f\u00e9rez xsl:output."},

    { ER_DEFAULTSPACE_NOT_SUPPORTED,
      "default-space n'est plus pris en charge !  Pr\u00e9f\u00e9rez xsl:strip-space ou xsl:preserve-space."},

    { ER_INDENTRESULT_NOT_SUPPORTED,
      "indent-result n'est plus pris en charge !  Pr\u00e9f\u00e9rez xsl:output."},

    { ER_ILLEGAL_ATTRIB,
      "(StylesheetHandler) {0} comporte un attribut incorrect : {1}"},

    { ER_UNKNOWN_XSL_ELEM,
     "El\u00e9ment XSL inconnu : {0}"},

    { ER_BAD_XSLSORT_USE,
      "(StylesheetHandler) xsl:sort ne peut \u00eatre utilis\u00e9 qu'avec xsl:apply-templates ou xsl:for-each."},

    { ER_MISPLACED_XSLWHEN,
      "(StylesheetHandler) xsl:when ne figure pas \u00e0 la bonne position !"},

    { ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:when sans rapport avec xsl:choose !"},

    { ER_MISPLACED_XSLOTHERWISE,
      "(StylesheetHandler) xsl:otherwise ne figure pas \u00e0 la bonne position !"},

    { ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:otherwise sans rapport avec xsl:choose !"},

    { ER_NOT_ALLOWED_INSIDE_TEMPLATE,
      "(StylesheetHandler) {0} n''est pas admis dans un mod\u00e8le !"},

    { ER_UNKNOWN_EXT_NS_PREFIX,
      "(StylesheetHandler) {0} pr\u00e9fixe de l''espace de noms de l''extension {1} inconnu"},

    { ER_IMPORTS_AS_FIRST_ELEM,
      "(StylesheetHandler) Les importations peuvent \u00eatre effectu\u00e9es uniquement en tant que premiers \u00e9l\u00e9ments de la feuille de style !"},

    { ER_IMPORTING_ITSELF,
      "(StylesheetHandler) {0} s''importe lui-m\u00eame directement ou indirectement !"},

    { ER_XMLSPACE_ILLEGAL_VAL,
      "(StylesheetHandler) xml:space poss\u00e8de une valeur incorrecte : {0}"},

    { ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL,
      "Echec de processStylesheet !"},

    { ER_SAX_EXCEPTION,
     "Exception SAX"},

//  add this message to fix bug 21478
    { ER_FUNCTION_NOT_SUPPORTED,
     "Fonction non prise en charge !"},


    { ER_XSLT_ERROR,
     "Erreur XSLT"},

    { ER_CURRENCY_SIGN_ILLEGAL,
      "Tout symbole mon\u00e9taire est interdit dans une cha\u00eene de motif de correspondance"},

    { ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM,
      "Fonction de document non prise en charge dans le DOM de la feuille de style !"},

    { ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER,
      "Impossible de r\u00e9soudre le pr\u00e9fixe du solveur !"},

    { ER_REDIRECT_COULDNT_GET_FILENAME,
      "Extension de redirection : Impossible d'extraire le nom du fichier - l'attribut de fichier ou de s\u00e9lection doit retourner une cha\u00eene valide."},

    { ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT,
      "Impossible de cr\u00e9er FormatterListener dans une extension Redirect !"},

    { ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX,
      "Pr\u00e9fixe de exclude-result-prefixes non valide : {0}"},

    { ER_MISSING_NS_URI,
      "URI de l'espace de noms manquant pour le pr\u00e9fixe indiqu\u00e9"},

    { ER_MISSING_ARG_FOR_OPTION,
      "Argument manquant pour l''option : {0}"},

    { ER_INVALID_OPTION,
     "Option incorrecte : {0}"},

    { ER_MALFORMED_FORMAT_STRING,
     "Cha\u00eene de format mal form\u00e9e : {0}"},

    { ER_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet requiert un attribut 'version' !"},

    { ER_ILLEGAL_ATTRIBUTE_VALUE,
      "L''attribut : {0} poss\u00e8de une valeur non valide : {1}"},

    { ER_CHOOSE_REQUIRES_WHEN,
     "xsl:choose requiert xsl:when"},

    { ER_NO_APPLY_IMPORT_IN_FOR_EACH,
      "xsl:apply-imports interdit dans un xsl:for-each"},

    { ER_CANT_USE_DTM_FOR_OUTPUT,
      "Impossible d'utiliser DTMLiaison pour un noeud de DOM en sortie... Transmettez org.apache.xpath.DOM2Helper \u00e0 la place !"},

    { ER_CANT_USE_DTM_FOR_INPUT,
      "Impossible d'utiliser DTMLiaison pour un noeud de DOM en entr\u00e9e... Transmettez org.apache.xpath.DOM2Helper \u00e0 la place !"},

    { ER_CALL_TO_EXT_FAILED,
      "Echec de l''appel de l''\u00e9l\u00e9ment d''extension : {0}"},

    { ER_PREFIX_MUST_RESOLVE,
      "Le pr\u00e9fixe doit se convertir en espace de noms : {0}"},

    { ER_INVALID_UTF16_SURROGATE,
      "Substitut UTF-16 non valide d\u00e9tect\u00e9 : {0} ?"},

    { ER_XSLATTRSET_USED_ITSELF,
      "xsl:attribute-set {0} s''utilise lui-m\u00eame, ce qui provoque une boucle infinie."},

    { ER_CANNOT_MIX_XERCESDOM,
      "Impossible de m\u00e9langer une entr\u00e9e autre que Xerces-DOM avec une sortie Xerces-DOM !"},

    { ER_TOO_MANY_LISTENERS,
      "addTraceListenersToStylesheet - TooManyListenersException"},

    { ER_IN_ELEMTEMPLATEELEM_READOBJECT,
      "Dans ElemTemplateElement.readObject : {0}"},

    { ER_DUPLICATE_NAMED_TEMPLATE,
      "Plusieurs mod\u00e8les s''appellent : {0}"},

    { ER_INVALID_KEY_CALL,
      "Appel de fonction non valide : appels de key() r\u00e9cursifs interdits"},

    { ER_REFERENCING_ITSELF,
      "La variable {0} fait r\u00e9f\u00e9rence \u00e0 elle-m\u00eame directement ou indirectement !"},

    { ER_ILLEGAL_DOMSOURCE_INPUT,
      "Le noeud d'entr\u00e9e ne peut \u00eatre vide pour un DOMSource de newTemplates !"},

    { ER_CLASS_NOT_FOUND_FOR_OPTION,
        "Fichier de classe introuvable pour l''option {0}"},

    { ER_REQUIRED_ELEM_NOT_FOUND,
        "El\u00e9ment requis introuvable : {0}"},

    { ER_INPUT_CANNOT_BE_NULL,
        "InputStream ne doit pas \u00eatre vide"},

    { ER_URI_CANNOT_BE_NULL,
        "L'URI ne doit pas \u00eatre vide"},

    { ER_FILE_CANNOT_BE_NULL,
        "Le fichier ne doit pas \u00eatre vide"},

    { ER_SOURCE_CANNOT_BE_NULL,
                "InputSource ne doit pas \u00eatre vide"},

    { ER_CANNOT_INIT_BSFMGR,
                "Impossible d'initialiser le gestionnaire de BSF"},

    { ER_CANNOT_CMPL_EXTENSN,
                "Impossible de compiler l'extension"},

    { ER_CANNOT_CREATE_EXTENSN,
      "Impossible de cr\u00e9er l''extension : {0} en raison de : {1}"},

    { ER_INSTANCE_MTHD_CALL_REQUIRES,
      "L''appel de la m\u00e9thode d''instance de la m\u00e9thode {0} requiert une instance d''Object comme premier argument"},

    { ER_INVALID_ELEMENT_NAME,
      "Nom d''\u00e9l\u00e9ment non valide sp\u00e9cifi\u00e9 {0}"},

    { ER_ELEMENT_NAME_METHOD_STATIC,
      "La m\u00e9thode de nom d''\u00e9l\u00e9ment doit \u00eatre statique {0}"},

    { ER_EXTENSION_FUNC_UNKNOWN,
             "La fonction d''extension {0} : {1} est inconnue"},

    { ER_MORE_MATCH_CONSTRUCTOR,
             "Plusieurs occurrences proches pour le constructeur de {0}"},

    { ER_MORE_MATCH_METHOD,
             "Plusieurs occurrences proches pour la m\u00e9thode {0}"},

    { ER_MORE_MATCH_ELEMENT,
             "Plusieurs occurrences proches pour la m\u00e9thode d''\u00e9l\u00e9ment {0}"},

    { ER_INVALID_CONTEXT_PASSED,
             "Contexte non valide transmis pour \u00e9valuer {0}"},

    { ER_POOL_EXISTS,
             "Pool d\u00e9j\u00e0 existant"},

    { ER_NO_DRIVER_NAME,
             "Aucun nom de p\u00e9riph\u00e9rique indiqu\u00e9"},

    { ER_NO_URL,
             "Aucune URL sp\u00e9cifi\u00e9e"},

    { ER_POOL_SIZE_LESSTHAN_ONE,
             "La taille du pool est inf\u00e9rieure \u00e0 1 !"},

    { ER_INVALID_DRIVER,
             "Nom de pilote non valide sp\u00e9cifi\u00e9 !"},

    { ER_NO_STYLESHEETROOT,
             "Impossible de trouver la racine de la feuille de style !"},

    { ER_ILLEGAL_XMLSPACE_VALUE,
         "Valeur incorrecte pour xml:space"},

    { ER_PROCESSFROMNODE_FAILED,
         "Echec de processFromNode"},

    { ER_RESOURCE_COULD_NOT_LOAD,
        "La ressource [ {0} ] n''a pas pu charger : {1} \n {2} \t {3}"},

    { ER_BUFFER_SIZE_LESSTHAN_ZERO,
        "Taille du tampon <=0"},

    { ER_UNKNOWN_ERROR_CALLING_EXTENSION,
        "Erreur inconnue lors de l'appel de l'extension"},

    { ER_NO_NAMESPACE_DECL,
        "Le pr\u00e9fixe {0} ne poss\u00e8de pas de d\u00e9claration d''espace de noms correspondante"},

    { ER_ELEM_CONTENT_NOT_ALLOWED,
        "Contenu d''\u00e9l\u00e9ment interdit pour lang=javaclass {0}"},

    { ER_STYLESHEET_DIRECTED_TERMINATION,
        "La feuille de style a provoqu\u00e9 l'arr\u00eat"},

    { ER_ONE_OR_TWO,
        "1 ou 2"},

    { ER_TWO_OR_THREE,
        "2 ou 3"},

    { ER_COULD_NOT_LOAD_RESOURCE,
        "Impossible de charger {0} (v\u00e9rifier CLASSPATH), les valeurs par d\u00e9faut sont donc employ\u00e9es"},

    { ER_CANNOT_INIT_DEFAULT_TEMPLATES,
        "Impossible d'initialiser les mod\u00e8les par d\u00e9faut"},

    { ER_RESULT_NULL,
        "Le r\u00e9sultat doit \u00eatre vide"},

    { ER_RESULT_COULD_NOT_BE_SET,
        "Le r\u00e9sultat ne peut \u00eatre d\u00e9fini"},

    { ER_NO_OUTPUT_SPECIFIED,
        "Aucune sortie sp\u00e9cifi\u00e9e"},

    { ER_CANNOT_TRANSFORM_TO_RESULT_TYPE,
        "Transformation impossible vers un r\u00e9sultat de type {0}"},

    { ER_CANNOT_TRANSFORM_SOURCE_TYPE,
        "Impossible de transformer une source de type {0}"},

    { ER_NULL_CONTENT_HANDLER,
        "Gestionnaire de contenu vide"},

    { ER_NULL_ERROR_HANDLER,
        "Gestionnaire d'erreurs vide"},

    { ER_CANNOT_CALL_PARSE,
        "L'analyse ne peut \u00eatre appel\u00e9e si le ContentHandler n'a pas \u00e9t\u00e9 d\u00e9fini"},

    { ER_NO_PARENT_FOR_FILTER,
        "Pas de parent pour le filtre"},

    { ER_NO_STYLESHEET_IN_MEDIA,
         "Aucune feuille de style dans : {0}, support = {1}"},

    { ER_NO_STYLESHEET_PI,
         "Pas de PI xml-stylesheet dans : {0}"},

    { ER_NOT_SUPPORTED,
       "Non pris en charge : {0}"},

    { ER_PROPERTY_VALUE_BOOLEAN,
       "La valeur de la propri\u00e9t\u00e9 {0} doit \u00eatre une instance bool\u00e9enne"},

    { ER_COULD_NOT_FIND_EXTERN_SCRIPT,
         "Impossible d''extraire le script externe \u00e0 {0}"},

    { ER_RESOURCE_COULD_NOT_FIND,
        "La ressource [ {0} ] est introuvable.\n {1}"},

    { ER_OUTPUT_PROPERTY_NOT_RECOGNIZED,
        "Propri\u00e9t\u00e9 de sortie non identifi\u00e9e : {0}"},

    { ER_FAILED_CREATING_ELEMLITRSLT,
        "Impossible de cr\u00e9er une instance de ElemLiteralResult"},

  //Earlier (JDK 1.4 XALAN 2.2-D11) at key code '204' the key name was ER_PRIORITY_NOT_PARSABLE
  // In latest Xalan code base key name is  ER_VALUE_SHOULD_BE_NUMBER. This should also be taken care
  //in locale specific files like XSLTErrorResources_de.java, XSLTErrorResources_fr.java etc.
  //NOTE: Not only the key name but message has also been changed.

    { ER_VALUE_SHOULD_BE_NUMBER,
        "La valeur de {0} doit contenir un nombre analysable"},

    { ER_VALUE_SHOULD_EQUAL,
        "La valeur de {0} doit \u00eatre oui ou non"},

    { ER_FAILED_CALLING_METHOD,
        "Echec de l''appel de la m\u00e9thode {0}"},

    { ER_FAILED_CREATING_ELEMTMPL,
        "Echec de la cr\u00e9ation de l'instance de ElemTemplateElement"},

    { ER_CHARS_NOT_ALLOWED,
        "La pr\u00e9sence de caract\u00e8res n'est pas admise \u00e0 cet endroit du document"},

    { ER_ATTR_NOT_ALLOWED,
        "L''attribut \"{0}\" n''est pas admis sur l''\u00e9l\u00e9ment {1} !"},

    { ER_BAD_VALUE,
     "{0} valeur erron\u00e9e {1} "},

    { ER_ATTRIB_VALUE_NOT_FOUND,
     "Impossible de trouver la valeur de l''attribut {0} "},

    { ER_ATTRIB_VALUE_NOT_RECOGNIZED,
     "Valeur de l''attribut {0} non identifi\u00e9e "},

    { ER_NULL_URI_NAMESPACE,
     "Tentative de cr\u00e9ation d'un pr\u00e9fixe d'espace de noms avec un URI vide"},

  //New ERROR keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    { ER_NUMBER_TOO_BIG,
     "Tentative de formatage d'un nombre sup\u00e9rieur \u00e0 l'entier Long le plus \u00e9lev\u00e9"},

    { ER_CANNOT_FIND_SAX1_DRIVER,
     "Impossible de trouver la classe {0} du pilote SAX1"},

    { ER_SAX1_DRIVER_NOT_LOADED,
     "Classe {0} du pilote SAX1 trouv\u00e9e mais non charg\u00e9e"},

    { ER_SAX1_DRIVER_NOT_INSTANTIATED,
     "Classe {0} du pilote SAX1 trouv\u00e9e mais non instanci\u00e9e"},

    { ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER,
     "La classe {0} du pilote SAX1 n''impl\u00e9mente pas org.xml.sax.Parser"},

    { ER_PARSER_PROPERTY_NOT_SPECIFIED,
     "Propri\u00e9t\u00e9 syst\u00e8me org.xml.sax.parser non sp\u00e9cifi\u00e9e"},

    { ER_PARSER_ARG_CANNOT_BE_NULL,
     "L'argument de l'analyseur ne doit pas \u00eatre vide"},

    { ER_FEATURE,
     "Fonction : {0}"},

    { ER_PROPERTY,
     "Propri\u00e9t\u00e9 : {0}"},

    { ER_NULL_ENTITY_RESOLVER,
     "Solveur d'entit\u00e9 vide"},

    { ER_NULL_DTD_HANDLER,
     "Gestionnaire de DT vide"},

    { ER_NO_DRIVER_NAME_SPECIFIED,
     "Aucun nom de pilote sp\u00e9cifi\u00e9 !"},

    { ER_NO_URL_SPECIFIED,
     "Aucune URL sp\u00e9cifi\u00e9e !"},

    { ER_POOLSIZE_LESS_THAN_ONE,
     "La taille du pool est inf\u00e9rieure \u00e0 1 !"},

    { ER_INVALID_DRIVER_NAME,
     "Nom de pilote non valide sp\u00e9cifi\u00e9 !"},

    { ER_ERRORLISTENER,
     "ErrorListener"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The name
//   'ElemTemplateElement' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_NO_TEMPLATE_PARENT,
     "Erreur de programme ! L'expression n'a pas de parent ElemTemplateElement !"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The substitution text
//   provides further information in order to diagnose the problem.  The name
//   'RedundentExprEliminator' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR,
     "Assertion du programmeur dans RedundentExprEliminator : {0}"},

    { ER_NOT_ALLOWED_IN_POSITION,
     "{0} ne peut pas figurer \u00e0 cette position dans la feuille de style !"},

    { ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION,
     "Seul de l'espace est accept\u00e9 \u00e0 cette position dans la feuille de style !"},

  // This code is shared with warning codes.
  // SystemId Unknown
    { INVALID_TCHAR,
     "Valeur incorrecte : {1} utilis\u00e9e pour l''attribut CHAR : {0}. Un attribut de type CHAR ne peut comporter qu''un seul caract\u00e8re !"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "QNAME" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value and {0} is the attribute name.
    //The following codes are shared with the warning codes...
    { INVALID_QNAME,
     "Valeur incorrecte : {1} utilis\u00e9e pour l''attribut QNAME : {0}"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "ENUM" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value, {0} is the attribute name, and {2} is a list of valid
    // values.
    { INVALID_ENUM,
     "Valeur incorrecte : {1} utilis\u00e9e pour l''attribut ENUM : {0}. Les valeurs autoris\u00e9es sont : {2}."},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NMTOKEN" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NMTOKEN,
     "Valeur incorrecte : {1} utilis\u00e9e pour l''attribut NMTOKEN : {0}. "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NCNAME" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NCNAME,
     "Valeur incorrecte : {1} utilis\u00e9e pour l''attribut NCNAME : {0}. "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "boolean" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_BOOLEAN,
     "Valeur incorrecte : {1} utilis\u00e9e pour l''attribut bool\u00e9en : {0}. "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "number" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
     { INVALID_NUMBER,
     "Valeur incorrecte : {1} utilis\u00e9e pour l''attribut number : {0}. "},


  // End of shared codes...

// Note to translators:  A "match pattern" is a special form of XPath expression
// that is used for matching patterns.  The substitution text is the name of
// a function.  The message indicates that when this function is referenced in
// a match pattern, its argument must be a string literal (or constant.)
// ER_ARG_LITERAL - new error message for bugzilla //5202
    { ER_ARG_LITERAL,
     "L''argument de {0} dans le motif de correspondance doit \u00eatre un litt\u00e9ral."},

// Note to translators:  The following message indicates that two definitions of
// a variable.  A "global variable" is a variable that is accessible everywher
// in the stylesheet.
// ER_DUPLICATE_GLOBAL_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_GLOBAL_VAR,
     "D\u00e9claration de variable globale en double."},


// Note to translators:  The following message indicates that two definitions of
// a variable were encountered.
// ER_DUPLICATE_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_VAR,
     "D\u00e9claration de variable en double."},

    // Note to translators:  "xsl:template, "name" and "match" are XSLT keywords
    // which must not be translated.
    // ER_TEMPLATE_NAME_MATCH - new error message for bugzilla #789
    { ER_TEMPLATE_NAME_MATCH,
     "xsl:template doit comporter un attribut name et/ou match"},

    // Note to translators:  "exclude-result-prefixes" is an XSLT keyword which
    // should not be translated.  The message indicates that a namespace prefix
    // encountered as part of the value of the exclude-result-prefixes attribute
    // was in error.
    // ER_INVALID_PREFIX - new error message for bugzilla #788
    { ER_INVALID_PREFIX,
     "Pr\u00e9fixe de exclude-result-prefixes non valide : {0}"},

    // Note to translators:  An "attribute set" is a set of attributes that can
    // be added to an element in the output document as a group.  The message
    // indicates that there was a reference to an attribute set named {0} that
    // was never defined.
    // ER_NO_ATTRIB_SET - new error message for bugzilla #782
    { ER_NO_ATTRIB_SET,
     "attribute-set {0} n''existe pas"},

    // Note to translators:  This message indicates that there was a reference
    // to a function named {0} for which no function definition could be found.
    { ER_FUNCTION_NOT_FOUND,
     "La fonction {0} n''existe pas"},

    // Note to translators:  This message indicates that the XSLT instruction
    // that is named by the substitution text {0} must not contain other XSLT
    // instructions (content) or a "select" attribute.  The word "select" is
    // an XSLT keyword in this case and must not be translated.
    { ER_CANT_HAVE_CONTENT_AND_SELECT,
     "L''\u00e9l\u00e9ment {0} ne peut poss\u00e9der un attribut content et un attribut select."},

    // Note to translators:  This message indicates that the value argument
    // of setParameter must be a valid Java Object.
    { ER_INVALID_SET_PARAM_VALUE,
     "La valeur du param\u00e8tre {0} doit \u00eatre un objet Java valide"},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT,
         "L'attribut result-prefix d'un \u00e9l\u00e9ment xsl:namespace-alias a la valeur '#default', mais il n'existe aucune d\u00e9claration de l'espace de noms par d\u00e9faut dans la port\u00e9e de l'\u00e9l\u00e9ment"},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX,
         "L''attribut result-prefix d''un \u00e9l\u00e9ment xsl:namespace-alias a la valeur ''{0}'', mais il n''existe aucune d\u00e9claration d''espace de noms pour le pr\u00e9fixe ''{0}'' dans la port\u00e9e de l''\u00e9l\u00e9ment."},

    { ER_SET_FEATURE_NULL_NAME,
      "Le nom de la fonction ne peut pas avoir une valeur null dans TransformerFactory.setFeature (nom cha\u00eene, valeur bool\u00e9nne)."},

    { ER_GET_FEATURE_NULL_NAME,
      "Le nom de la fonction ne peut pas avoir une valeur null dans TransformerFactory.getFeature (nom cha\u00eene)."},

    { ER_UNSUPPORTED_FEATURE,
      "Impossible de d\u00e9finir la fonction ''{0}'' sur cet \u00e9l\u00e9ment TransformerFactory."},

    { ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING,
        "L''utilisation de l''\u00e9l\u00e9ment d''extension ''{0}'' n''est pas admise lorsque la fonction de traitement s\u00e9curis\u00e9e a la valeur true."},

        { ER_NAMESPACE_CONTEXT_NULL_NAMESPACE,
          "Impossible d'obtenir le pr\u00e9fixe pour un uri d'espace de noms null."},

        { ER_NAMESPACE_CONTEXT_NULL_PREFIX,
          "Impossible d'obtenir l'uri d'espace de noms pour le pr\u00e9fixe null."},

        { ER_XPATH_RESOLVER_NULL_QNAME,
          "Le nom de la fonction ne peut pas avoir une valeur null."},

        { ER_XPATH_RESOLVER_NEGATIVE_ARITY,
          "La parit\u00e9 ne peut pas \u00eatre n\u00e9gative."},

  // Warnings...

    { WG_FOUND_CURLYBRACE,
      "Une accolade ('}') a \u00e9t\u00e9 trouv\u00e9e alors qu'aucun mod\u00e8le d'attribut n'est ouvert !"},

    { WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR,
      "Avertissement : L''attribut de count n''a pas d''ascendant dans xsl:number ! Cible = {0}"},

    { WG_EXPR_ATTRIB_CHANGED_TO_SELECT,
      "Syntaxe obsol\u00e8te : Le nom de l'attribut 'expr' a \u00e9t\u00e9 remplac\u00e9 par 'select'."},

    { WG_NO_LOCALE_IN_FORMATNUMBER,
      "Xalan ne g\u00e8re pas encore le nom d'environnement local de la fonction format-number."},

    { WG_LOCALE_NOT_FOUND,
      "Avertissement : Impossible de trouver un environnement local pour xml:lang={0}"},

    { WG_CANNOT_MAKE_URL_FROM,
      "Impossible de cr\u00e9er l''URL \u00e0 partir de : {0}"},

    { WG_CANNOT_LOAD_REQUESTED_DOC,
      "Impossible de charger le document demand\u00e9 : {0}"},

    { WG_CANNOT_FIND_COLLATOR,
      "Impossible de trouver une fonction de regroupement pour <sort xml:lang= {0}"},

    { WG_FUNCTIONS_SHOULD_USE_URL,
      "Syntaxe obsol\u00e8te : L''instruction de fonction doit utiliser une URL {0}"},

    { WG_ENCODING_NOT_SUPPORTED_USING_UTF8,
      "encodage non pris en charge : {0}, en utilisant UTF-8"},

    { WG_ENCODING_NOT_SUPPORTED_USING_JAVA,
      "encodage non pris en charge : {0}, en utilisant Java {1}"},

    { WG_SPECIFICITY_CONFLICTS,
      "Conflits de sp\u00e9cificit\u00e9s trouv\u00e9s : {0} La derni\u00e8re de la feuille de style sera employ\u00e9e."},

    { WG_PARSING_AND_PREPARING,
      "========= Analyse et pr\u00e9paration de {0} =========="},

    { WG_ATTR_TEMPLATE,
     "Mod\u00e8le d''attribut, {0}"},

    { WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE,
      "Conflit de correspondances entre xsl:strip-space et xsl:preserve-space"},

    { WG_ATTRIB_NOT_HANDLED,
      "Xalan ne g\u00e8re pas encore l''attribut {0} !"},

    { WG_NO_DECIMALFORMAT_DECLARATION,
      "Pas de d\u00e9claration pour le format d\u00e9cimal : {0}"},

    { WG_OLD_XSLT_NS,
     "Espace de noms XSLT manquant ou incorrect. "},

    { WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED,
      "Une seule d\u00e9claration xsl:decimal-format par d\u00e9faut est admise."},

    { WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE,
      "Les noms xsl:decimal-format doivent \u00eatre uniques. Le nom \"{0}\" a \u00e9t\u00e9 dupliqu\u00e9."},

    { WG_ILLEGAL_ATTRIBUTE,
      "{0} comporte un attribut incorrect : {1}"},

    { WG_COULD_NOT_RESOLVE_PREFIX,
      "Impossible de convertir le pr\u00e9fixe de l''espace de noms : {0}. Le noeud n''est pas trait\u00e9."},

    { WG_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet requiert un attribut 'version' !"},

    { WG_ILLEGAL_ATTRIBUTE_NAME,
      "Nom d''attribut incorrect : {0}"},

    { WG_ILLEGAL_ATTRIBUTE_VALUE,
      "Valeur incorrecte pour l''attribut {0} : {1}"},

    { WG_EMPTY_SECOND_ARG,
      "L'ensemble de noeuds r\u00e9sultant du second argument de la fonction du document est vide. Un ensemble de noeuds vide est retourn\u00e9."},

  //Following are the new WARNING keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.
    { WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "La valeur de l'attribut 'name' de xsl:processing-instruction doit \u00eatre diff\u00e9rente de 'xml'"},

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.  "NCName" is an XML data-type and must not be
    // translated.
    { WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "La valeur de l''attribut ''name'' de xsl:processing-instruction doit \u00eatre un nom NCName valide : {0}"},

    // Note to translators:  This message is reported if the stylesheet that is
    // being processed attempted to construct an XML document with an attribute in a
    // place other than on an element.  The substitution text specifies the name of
    // the attribute.
    { WG_ILLEGAL_ATTRIBUTE_POSITION,
      "Ajout impossible de l''attribut {0} apr\u00e8s des noeuds enfants ou avant la production d''un \u00e9l\u00e9ment. L''attribut est ignor\u00e9."},

    { NO_MODIFICATION_ALLOWED_ERR,
      "Tentative de modification d'un objet qui n'accepte pas les modifications."
    },

    //Check: WHY THERE IS A GAP B/W NUMBERS in the XSLTErrorResources properties file?

  // Other miscellaneous text used inside the code...
  { "ui_language", "en"},
  {  "help_language",  "en" },
  {  "language",  "en" },
  { "BAD_CODE", "Le param\u00e8tre de createMessage se trouve hors limites"},
  {  "FORMAT_FAILED", "Exception soulev\u00e9e lors de l'appel de messageFormat"},
  {  "version", ">>>>>>> Version de Xalan "},
  {  "version2",  "<<<<<<<"},
  {  "yes", "oui"},
  { "line", "Ligne #"},
  { "column","Colonne #"},
  { "xsldone", "XSLProcessor : termin\u00e9"},


  // Note to translators:  The following messages provide usage information
  // for the Xalan Process command line.  "Process" is the name of a Java class,
  // and should not be translated.
  { "xslProc_option", "Options de classe Process de ligne de commande Xalan-J :"},
  { "xslProc_option", "Options de classe Process de ligne de commande Xalan-J\u003a"},
  { "xslProc_invalid_xsltc_option", "L''option {0} n''est pas prise en charge en mode XSLTC."},
  { "xslProc_invalid_xalan_option", "L''option {0} s''utilise uniquement avec -XSLTC."},
  { "xslProc_no_input", "Erreur : Aucun xml de feuille de style ou d'entr\u00e9e n'est sp\u00e9cifi\u00e9. Ex\u00e9cutez cette commande sans option pour les instructions d'utilisation."},
  { "xslProc_common_options", "-Options courantes-"},
  { "xslProc_xalan_options", "-Options pour Xalan-"},
  { "xslProc_xsltc_options", "-Options pour XSLTC-"},
  { "xslProc_return_to_continue", "(appuyez sur <Retour> pour continuer)"},

   // Note to translators: The option name and the parameter name do not need to
   // be translated. Only translate the messages in parentheses.  Note also that
   // leading whitespace in the messages is used to indent the usage information
   // for each option in the English messages.
   // Do not translate the keywords: XSLTC, SAX, DOM and DTM.
  { "optionXSLTC", "   [-XSLTC (utilisez XSLTC pour la transformation)]"},
  { "optionIN", "   [-IN inputXMLURL]"},
  { "optionXSL", "   [-XSL URLXSLTransformation]"},
  { "optionOUT",  "   [-OUT nomFichierSortie]"},
  { "optionLXCIN", "   [-LXCIN NomFichierFeuilleDeStylesCompil\u00e9Entr\u00e9e]"},
  { "optionLXCOUT", "   [-LXCOUT NomFichierFeuilleDeStylesCompil\u00e9Sortie]"},
  { "optionPARSER", "   [-PARSER nom de classe pleinement qualifi\u00e9 pour la liaison de l'analyseur]"},
  {  "optionE", "   [-E (Ne pas d\u00e9velopper les r\u00e9f. d'entit\u00e9)]"},
  {  "optionV",  "   [-E (Ne pas d\u00e9velopper les r\u00e9f. d'entit\u00e9)]"},
  {  "optionQC", "   [-QC (Avertissements brefs de conflits de motifs)]"},
  {  "optionQ", "   [-Q  (Mode bref)]"},
  {  "optionLF", "   [-LF (Utilise des sauts de ligne uniquement dans la sortie {CR/LF par d\u00e9faut})]"},
  {  "optionCR", "   [-LF (Utilise des retours chariot uniquement dans la sortie {CR/LF par d\u00e9faut})]"},
  { "optionESCAPE", "   [-ESCAPE (Caract\u00e8res d'\u00e9chappement {<>&\"\'\\r\\n par d\u00e9faut}]"},
  { "optionINDENT", "   [-INDENT (Nombre d'espaces pour le retrait {par d\u00e9faut 0})]"},
  { "optionTT", "   [-TT (Contr\u00f4le les appels de mod\u00e8les - fonction de trace.)]"},
  { "optionTG", "   [-TG (Contr\u00f4le chaque \u00e9v\u00e9nement de g\u00e9n\u00e9ration - fonction de trace.)]"},
  { "optionTS", "   [-TS (Contr\u00f4le chaque \u00e9v\u00e9nement de s\u00e9lection - fonction de trace.)]"},
  {  "optionTTC", "   [-TTC (Contr\u00f4le les enfants du mod\u00e8le lors de leur traitement - fonction de trace.)]"},
  { "optionTCLASS", "   [-TCLASS (Classe TraceListener pour les extensions de trace.)]"},
  { "optionVALIDATE", "   [-VALIDATE (Indique si la validation se produit. La validation est d\u00e9sactiv\u00e9e par d\u00e9faut.)]"},
  { "optionEDUMP", "   [-EDUMP {nom de fichier optionnel} (Cr\u00e9e un vidage de pile en cas d'erreur.)]"},
  {  "optionXML", "   [-XML (Utilise un formateur XML et ajoute un en-t\u00eate XML.)]"},
  {  "optionTEXT", "   [-TEXT (Utilise un formateur de texte simple.)]"},
  {  "optionHTML", "   [-HTML (Utilise un formateur HTML.)]"},
  {  "optionPARAM", "   [-PARAM nom expression (D\u00e9finit un param\u00e8tre de feuille de style)]"},
  {  "noParsermsg1", "Echec du processus XSL."},
  {  "noParsermsg2", "** Analyseur introuvable **"},
  { "noParsermsg3",  "V\u00e9rifiez le chemin d'acc\u00e8s des classes."},
  { "noParsermsg4", "XML Parser for Java disponible en t\u00e9l\u00e9chargement sur le site"},
  { "noParsermsg5", "AlphaWorks de IBM : http://www.alphaworks.ibm.com/formula/xml"},
  { "optionURIRESOLVER", "   [-URIRESOLVER nom de classe complet (Les URI sont r\u00e9solus par URIResolver)]"},
  { "optionENTITYRESOLVER",  "   [-ENTITYRESOLVER nom de classe complet (Les URI sont r\u00e9solus par EntityResolver)]"},
  { "optionCONTENTHANDLER",  "   [-CONTENTHANDLER nom de classe complet (La s\u00e9rialisation de la sortie est effectu\u00e9e par ContentHandler)]"},
  {  "optionLINENUMBERS",  "   [-L utilisation des num\u00e9ros de ligne pour le document source]"},
  { "optionSECUREPROCESSING", "   [-SECURE (attribuez la valeur true \u00e0 la fonction de traitement s\u00e9curis\u00e9.)]"},

    // Following are the new options added in XSLTErrorResources.properties files after Jdk 1.4 (Xalan 2.2-D11)


  {  "optionMEDIA",  "   [-MEDIA type_de_support (Utilise un attribut de support pour trouver la feuille de style associ\u00e9e \u00e0 un document.)]"},
  {  "optionFLAVOR",  "   [-FLAVOR sax_ou_dom (effectue la transformation \u00e0 l'aide de SAX (s2s) ou de DOM (d2d).)] "}, // Added by sboag/scurcuru; experimental
  { "optionDIAG", "   [-DIAG (affiche la dur\u00e9e globale de la transformation - en millisecondes.)]"},
  { "optionINCREMENTAL",  "   [-INCREMENTAL (construction incr\u00e9mentielle du DTM en d\u00e9finissant http://xml.apache.org/xalan/features/incremental true.)]"},
  {  "optionNOOPTIMIMIZE",  "   [-NOOPTIMIMIZE (pas de traitement d'optimisation des feuilles de style en d\u00e9finissant http://xml.apache.org/xalan/features/optimize false.)]"},
  { "optionRL",  "   [-RL r\u00e9cursivit\u00e9_maxi (limite de la profondeur de la r\u00e9cursivit\u00e9 pour les feuilles de style.)]"},
  {   "optionXO",  "   [-XO [nom_translet] (assignation du nom au translet g\u00e9n\u00e9r\u00e9)]"},
  {  "optionXD", "   [-XD r\u00e9pertoire_cible (sp\u00e9cification d'un r\u00e9pertoire de destination pour translet)]"},
  {  "optionXJ",  "   [-XJ fichier_jar (r\u00e9union des classes translet dans un fichier jar appel\u00e9 <fichier_jar>)]"},
  {   "optionXP",  "   [-XP module (sp\u00e9cification d'un pr\u00e9fixe de nom de module pour toutes les classes translet g\u00e9n\u00e9r\u00e9es)]"},

  //AddITIONAL  STRINGS that need L10n
  // Note to translators:  The following message describes usage of a particular
  // command-line option that is used to enable the "template inlining"
  // optimization.  The optimization involves making a copy of the code
  // generated for a template in another template that refers to it.
  { "optionXN",  "   [-XN (activation de la mise en ligne de mod\u00e8le)]" },
  { "optionXX",  "   [-XX (activation du d\u00e9bogage suppl\u00e9mentaire de sortie de message)]"},
  { "optionXT" , "   [-XT (utilisation de translet pour la transformation si possible)]"},
  { "diagTiming"," --------- La transformation de {0} via {1} a pris {2} ms" },
  { "recursionTooDeep","Trop grande imbrication de mod\u00e8le. imbrication = {0}, mod\u00e8le {1} {2}" },
  { "nameIs", "le nom est" },
  { "matchPatternIs", "le motif de correspondance est" }

  };
  }
  // ================= INFRASTRUCTURE ======================

  /** String for use when a bad error code was encountered.    */
  public static final String BAD_CODE = "BAD_CODE";

  /** String for use when formatting of the error string failed.   */
  public static final String FORMAT_FAILED = "FORMAT_FAILED";

  /** General error string.   */
  public static final String ERROR_STRING = "#error";

  /** String to prepend to error messages.  */
  public static final String ERROR_HEADER = "Erreur : ";

  /** String to prepend to warning messages.    */
  public static final String WARNING_HEADER = "Avertissement : ";

  /** String to specify the XSLT module.  */
  public static final String XSL_HEADER = "XSLT ";

  /** String to specify the XML parser module.  */
  public static final String XML_HEADER = "XML ";

  /** I don't think this is used any more.
   * @deprecated  */
  public static final String QUERY_HEADER = "PATTERN ";


  /**
   *   Return a named ResourceBundle for a particular locale.  This method mimics the behavior
   *   of ResourceBundle.getBundle().
   *
   *   @param className the name of the class that implements the resource bundle.
   *   @return the ResourceBundle
   *   @throws MissingResourceException
   */
  public static final XSLTErrorResources loadResourceBundle(String className)
          throws MissingResourceException
  {

    Locale locale = Locale.getDefault();
    String suffix = getResourceSuffix(locale);

    try
    {

      // first try with the given locale
      return (XSLTErrorResources) ResourceBundle.getBundle(className
              + suffix, locale);
    }
    catch (MissingResourceException e)
    {
      try  // try to fall back to en_US if we can't load
      {

        // Since we can't find the localized property file,
        // fall back to en_US.
        return (XSLTErrorResources) ResourceBundle.getBundle(className,
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
