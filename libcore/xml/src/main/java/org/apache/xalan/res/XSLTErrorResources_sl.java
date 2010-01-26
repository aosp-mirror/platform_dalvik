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
 * $Id: XSLTErrorResources_sl.java 338081 2004-12-15 17:35:58Z jycli $
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
public class XSLTErrorResources_sl extends ListResourceBundle
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
      "Napaka: Izraz ne sme vsebovati '{'"},

    { ER_ILLEGAL_ATTRIBUTE ,
     "{0} vsebuje neveljaven atribut: {1}"},

  {ER_NULL_SOURCENODE_APPLYIMPORTS ,
      "sourceNode je NULL v xsl:apply-imports!"},

  {ER_CANNOT_ADD,
      "Ne morem dodati {0} k {1}"},

    { ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES,
      "sourceNode je NULL v handleApplyTemplatesInstruction!"},

    { ER_NO_NAME_ATTRIB,
     "{0} mora vsebovati atribut imena."},

    {ER_TEMPLATE_NOT_FOUND,
     "Nisem na\u0161em predloge z imenom: {0}"},

    {ER_CANT_RESOLVE_NAME_AVT,
      "Imena AVT v xsl:call-template ni bilo mogo\u010de razre\u0161iti."},

    {ER_REQUIRES_ATTRIB,
     "{0} zahteva atribut: {1}"},

    { ER_MUST_HAVE_TEST_ATTRIB,
      "{0} mora imeti atribut ''test''."},

    {ER_BAD_VAL_ON_LEVEL_ATTRIB,
      "Slaba vrednost pri atributu stopnje: {0}"},

    {ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "Ime navodila za obdelavo ne more biti 'xml'"},

    { ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "Ime navodila za obdelavo mora biti veljavno NCIme: {0}"},

    { ER_NEED_MATCH_ATTRIB,
      "{0} mora vsebovati primerjalni atribut, \u010de vsebuje vozli\u0161\u010de."},

    { ER_NEED_NAME_OR_MATCH_ATTRIB,
      "{0} zahteva atribut imena ali primerjalni atribut."},

    {ER_CANT_RESOLVE_NSPREFIX,
      "Predpone imenskega prostora ni mogo\u010de razre\u0161iti: {0}"},

    { ER_ILLEGAL_VALUE,
     "xml:space vsebuje neveljavno vrednost: {0}"},

    { ER_NO_OWNERDOC,
      "Podrejeno vozli\u0161\u010de ne vsebuje lastni\u0161kega dokumenta!"},

    { ER_ELEMTEMPLATEELEM_ERR,
     "Napaka ElemTemplateElement: {0}"},

    { ER_NULL_CHILD,
     "Poskus dodajanja podrejenega elementa z vrednostjo NULL!"},

    { ER_NEED_SELECT_ATTRIB,
     "{0} zahteva atribut izbire."},

    { ER_NEED_TEST_ATTRIB ,
      "xsl:when mora vsebovati atribut 'test'."},

    { ER_NEED_NAME_ATTRIB,
      "xsl:with-param mora vsebovati atribut 'ime'."},

    { ER_NO_CONTEXT_OWNERDOC,
      "Kontekst ne vsebuje lastni\u0161kega dokumenta!"},

    {ER_COULD_NOT_CREATE_XML_PROC_LIAISON,
      "Ne morem ustvariti zveze XML TransformerFactory: {0}"},

    {ER_PROCESS_NOT_SUCCESSFUL,
      "Xalan: postopek ni uspel."},

    { ER_NOT_SUCCESSFUL,
     "Xalan: ni uspel."},

    { ER_ENCODING_NOT_SUPPORTED,
     "Kodiranje ni podprto: {0}"},

    {ER_COULD_NOT_CREATE_TRACELISTENER,
      "Ne morem ustvariti javanskega razreda TraceListener: {0}"},

    {ER_KEY_REQUIRES_NAME_ATTRIB,
      "xsl:key zahteva atribut 'ime'!"},

    { ER_KEY_REQUIRES_MATCH_ATTRIB,
      "xsl:key zahteva atribut 'ujemanje'!"},

    { ER_KEY_REQUIRES_USE_ATTRIB,
      "xsl:key zahteva atribut 'uporaba'!"},

    { ER_REQUIRES_ELEMENTS_ATTRIB,
      "(StylesheetHandler) {0} zahteva atribut ''elementi''!"},

    { ER_MISSING_PREFIX_ATTRIB,
      "(StylesheetHandler) {0} manjka atribut ''predpona''"},

    { ER_BAD_STYLESHEET_URL,
     "URL slogovne datoteke je neveljaven: {0}"},

    { ER_FILE_NOT_FOUND,
     "Slogovne datoteke ni bilo mogo\u010de najti: {0}"},

    { ER_IOEXCEPTION,
      "Pri slogovni datoteki je pri\u0161lo do izjeme IO: {0}"},

    { ER_NO_HREF_ATTRIB,
      "(StylesheetHandler) Atributa href za {0} ni bilo mogo\u010de najti"},

    { ER_STYLESHEET_INCLUDES_ITSELF,
      "(StylesheetHandler) {0} neposredno ali posredno vklju\u010duje samega sebe!"},

    { ER_PROCESSINCLUDE_ERROR,
      "Napaka StylesheetHandler.processInclude, {0}"},

    { ER_MISSING_LANG_ATTRIB,
      "(StylesheetHandler) {0} manjka atribut ''lang'' "},

    { ER_MISSING_CONTAINER_ELEMENT_COMPONENT,
      "(StylesheetHandler) napa\u010dna postavitev elementa {0}?? Manjka vsebni element ''komponenta''"},

    { ER_CAN_ONLY_OUTPUT_TO_ELEMENT,
      "Prenos mogo\u010d samo v Element, DocumentFragment, Document, ali PrintWriter."},

    { ER_PROCESS_ERROR,
     "Napaka StylesheetRoot.process"},

    { ER_UNIMPLNODE_ERROR,
     "Napaka UnImplNode: {0}"},

    { ER_NO_SELECT_EXPRESSION,
      "Napaka! Ne najdem izbirnega izraza xpath (-select)."},

    { ER_CANNOT_SERIALIZE_XSLPROCESSOR,
      "Ne morem serializirati XSLProcessor!"},

    { ER_NO_INPUT_STYLESHEET,
      "Vnos slogovne datoteke ni dolo\u010den!"},

    { ER_FAILED_PROCESS_STYLESHEET,
      "Obdelava slogovne datoteke ni uspela!"},

    { ER_COULDNT_PARSE_DOC,
     "Dokumenta {0} ni mogo\u010de raz\u010dleniti!"},

    { ER_COULDNT_FIND_FRAGMENT,
     "Ne najdem fragmenta: {0}"},

    { ER_NODE_NOT_ELEMENT,
      "Vozli\u0161\u010de, na katerega ka\u017ee identifikator fragmenta, ni element: {0}"},

    { ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB,
      "vsak mora vsebovati primerjalni atribut ali atribut imena"},

    { ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB,
      "predloge morajo imeti primerjalni atribut ali atribut imena"},

    { ER_NO_CLONE_OF_DOCUMENT_FRAG,
      "Ni klona fragmenta dokumenta!"},

    { ER_CANT_CREATE_ITEM,
      "Ne morem ustvariti elementa v drevesu rezultatov: {0}"},

    { ER_XMLSPACE_ILLEGAL_VALUE,
      "xml:space v izvirnem XML ima neveljavno vrednost: {0}"},

    { ER_NO_XSLKEY_DECLARATION,
      "Ni deklaracije xsl:key za {0}!"},

    { ER_CANT_CREATE_URL,
     "Napaka! Ne morem ustvariti URL za: {0}"},

    { ER_XSLFUNCTIONS_UNSUPPORTED,
     "xsl:functions niso podprte"},

    { ER_PROCESSOR_ERROR,
     "Napaka XSLT TransformerFactory"},

    { ER_NOT_ALLOWED_INSIDE_STYLESHEET,
      "(StylesheetHandler) {0} ni dovoljen znotraj slogovne datoteke!"},

    { ER_RESULTNS_NOT_SUPPORTED,
      "result-ns ni ve\u010d podprt!  Namesto njega uporabite xsl:output."},

    { ER_DEFAULTSPACE_NOT_SUPPORTED,
      "default-space ni ve\u010d podprt!  Namesto njega uporabite xsl:strip-space ali xsl:preserve-space."},

    { ER_INDENTRESULT_NOT_SUPPORTED,
      "indent-result ni ve\u010d podprt!  Namesto njega uporabite xsl:output."},

    { ER_ILLEGAL_ATTRIB,
      "(StylesheetHandler) {0} ima neveljaven atribut: {1}"},

    { ER_UNKNOWN_XSL_ELEM,
     "Neznani element XSL: {0}"},

    { ER_BAD_XSLSORT_USE,
      "(StylesheetHandler) xsl:sort lahko uporabljamo samo z xsl:apply-templates ali z xsl:for-each."},

    { ER_MISPLACED_XSLWHEN,
      "(StylesheetHandler) napa\u010dna postavitev xsl:when!"},

    { ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:choose ni nadrejen xsl:when!"},

    { ER_MISPLACED_XSLOTHERWISE,
      "(StylesheetHandler) napa\u010dna postavitev xsl:otherwise!"},

    { ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:choose ni nadrejen xsl:otherwise!"},

    { ER_NOT_ALLOWED_INSIDE_TEMPLATE,
      "(StylesheetHandler) {0} ni dovoljen znotraj predloge!"},

    { ER_UNKNOWN_EXT_NS_PREFIX,
      "(StylesheetHandler) Neznana {0} kon\u010dnica predpone imenskega prostora {1}"},

    { ER_IMPORTS_AS_FIRST_ELEM,
      "(StylesheetHandler) Uvozi se lahko pojavijo samo kot prvi elementi v slogovni datoteki!"},

    { ER_IMPORTING_ITSELF,
      "(StylesheetHandler) {0} neposredno ali posredno uva\u017ea samega sebe!"},

    { ER_XMLSPACE_ILLEGAL_VAL,
      "(StylesheetHandler) xml:space vsebuje neveljavno vrednost: {0}"},

    { ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL,
      "processStylesheet ni uspelo!"},

    { ER_SAX_EXCEPTION,
     "Izjema SAX"},

//  add this message to fix bug 21478
    { ER_FUNCTION_NOT_SUPPORTED,
     "Funkcija ni podprta!"},


    { ER_XSLT_ERROR,
     "Napaka XSLT"},

    { ER_CURRENCY_SIGN_ILLEGAL,
      "V oblikovnem nizu vzorca znak za denarno enoto ni dovoljen"},

    { ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM,
      "Funkcija dokumenta v slogovni datoteki DOM ni podprta!"},

    { ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER,
      "Ne morem razbrati predpone nepredponskega razre\u0161evalnika!"},

    { ER_REDIRECT_COULDNT_GET_FILENAME,
      "Preusmeri kon\u010dnico: ne morem pridobiti imena datoteke - atribut datoteke ali izbire mora vrniti veljaven niz."},

    { ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT,
      "V Preusmeritvi kon\u010dnice ne morem zgraditi FormatterListener!"},

    { ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX,
      "Predpona v izklju\u010di-predpone-rezultatov (exclude-result-prefixes) ni veljavna: {0}"},

    { ER_MISSING_NS_URI,
      "Za navedeno predpono manjka imenski prostor URI"},

    { ER_MISSING_ARG_FOR_OPTION,
      "Manjka argument za mo\u017enost: {0}"},

    { ER_INVALID_OPTION,
     "Neveljavna mo\u017enost: {0}"},

    { ER_MALFORMED_FORMAT_STRING,
     "Po\u0161kodovan niz sloga: {0}"},

    { ER_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet zahteva atribut 'razli\u010dica'!"},

    { ER_ILLEGAL_ATTRIBUTE_VALUE,
      "Atribut: {0} ima neveljavno vrednost: {1}"},

    { ER_CHOOSE_REQUIRES_WHEN,
     "xsl:choose zahteva xsl:when"},

    { ER_NO_APPLY_IMPORT_IN_FOR_EACH,
      "xsl:apply-imports v xsl:for-each ni dovoljen"},

    { ER_CANT_USE_DTM_FOR_OUTPUT,
      "Za izhodno vozli\u0161\u010de DOM ne morem uporabiti DTMLiaison... namesto njega posredujte org.apache.xpath.DOM2Helper!"},

    { ER_CANT_USE_DTM_FOR_INPUT,
      "Za vhodno vozli\u0161\u010de DOM ne morem uporabiti DTMLiaison... namesto njega posredujte org.apache.xpath.DOM2Helper!"},

    { ER_CALL_TO_EXT_FAILED,
      "Klic elementa kon\u010dnice ni uspel: {0}"},

    { ER_PREFIX_MUST_RESOLVE,
      "Predpona se mora razre\u0161iti v imenski prostor: {0}"},

    { ER_INVALID_UTF16_SURROGATE,
      "Zaznan neveljaven nadomestek UTF-16: {0} ?"},

    { ER_XSLATTRSET_USED_ITSELF,
      "xsl:attribute-set {0} je uporabil samega sebe, kar bo povzro\u010dilo neskon\u010do ponavljanje."},

    { ER_CANNOT_MIX_XERCESDOM,
      "Prepletanje ne-Xerces-DOM vhoda s Xerces-DOM vhodom ni mogo\u010de!"},

    { ER_TOO_MANY_LISTENERS,
      "addTraceListenersToStylesheet - TooManyListenersException"},

    { ER_IN_ELEMTEMPLATEELEM_READOBJECT,
      "V ElemTemplateElement.readObject: {0}"},

    { ER_DUPLICATE_NAMED_TEMPLATE,
      "Na\u0161el ve\u010d predlog z istim imenom: {0}"},

    { ER_INVALID_KEY_CALL,
      "Neveljaven klic funkcije: povratni klici key() niso dovoljeni"},

    { ER_REFERENCING_ITSELF,
      "Spremenljivka {0} se neposredno ali posredno sklicuje sama nase!"},

    { ER_ILLEGAL_DOMSOURCE_INPUT,
      "Vhodno vozli\u0161\u010de za DOMSource za newTemplates ne more biti NULL!"},

    { ER_CLASS_NOT_FOUND_FOR_OPTION,
        "Datoteke razreda za mo\u017enost {0} ni bilo mogo\u010de najti"},

    { ER_REQUIRED_ELEM_NOT_FOUND,
        "Zahtevanega elementa ni bilo mogo\u010de najti: {0}"},

    { ER_INPUT_CANNOT_BE_NULL,
        "InputStream ne more biti NULL"},

    { ER_URI_CANNOT_BE_NULL,
        "URI ne more biti NULL"},

    { ER_FILE_CANNOT_BE_NULL,
        "Datoteka ne more biti NULL"},

    { ER_SOURCE_CANNOT_BE_NULL,
                "InputSource ne more biti NULL"},

    { ER_CANNOT_INIT_BSFMGR,
                "Inicializacija BSF Manager-ja ni mogo\u010da"},

    { ER_CANNOT_CMPL_EXTENSN,
                "Kon\u010dnice ni mogo\u010de prevesti"},

    { ER_CANNOT_CREATE_EXTENSN,
      "Ne morem ustvariti kon\u010dnice: {0} zaradi: {1}"},

    { ER_INSTANCE_MTHD_CALL_REQUIRES,
      "Klic primerkov metode za metodo {0} zahteva primerek objekta kot prvi argument"},

    { ER_INVALID_ELEMENT_NAME,
      "Navedeno neveljavno ime elementa {0}"},

    { ER_ELEMENT_NAME_METHOD_STATIC,
      "Metoda imena elementa mora biti stati\u010dna (static) {0}"},

    { ER_EXTENSION_FUNC_UNKNOWN,
             "Funkcija kon\u010dnice {0} : {1} je neznana"},

    { ER_MORE_MATCH_CONSTRUCTOR,
             "Ve\u010d kot eno najbolj\u0161e ujemanje za graditelja za {0}"},

    { ER_MORE_MATCH_METHOD,
             "Ve\u010d kot eno najbolj\u0161e ujemanje za metodo {0}"},

    { ER_MORE_MATCH_ELEMENT,
             "Ve\u010d kot eno najbolj\u0161e ujemanje za metodo elementa {0}"},

    { ER_INVALID_CONTEXT_PASSED,
             "Posredovan neveljaven kontekst za ovrednotenje {0}"},

    { ER_POOL_EXISTS,
             "Zaloga \u017ee obstaja"},

    { ER_NO_DRIVER_NAME,
             "Ime gonilnika ni dolo\u010deno"},

    { ER_NO_URL,
             "URL ni dolo\u010den"},

    { ER_POOL_SIZE_LESSTHAN_ONE,
             "Zaloga je manj\u0161a od ena!"},

    { ER_INVALID_DRIVER,
             "Navedeno neveljavno ime gonilnika!"},

    { ER_NO_STYLESHEETROOT,
             "Korena slogovne datoteke ni mogo\u010de najti!"},

    { ER_ILLEGAL_XMLSPACE_VALUE,
         "Neveljavna vrednost za xml:space"},

    { ER_PROCESSFROMNODE_FAILED,
         "processFromNode spodletelo"},

    { ER_RESOURCE_COULD_NOT_LOAD,
        "Sredstva [ {0} ] ni bilo mogo\u010de nalo\u017eiti: {1} \n {2} \t {3}"},

    { ER_BUFFER_SIZE_LESSTHAN_ZERO,
        "Velikost medpomnilnika <=0"},

    { ER_UNKNOWN_ERROR_CALLING_EXTENSION,
        "Neznana napaka pri klicu kon\u010dnice"},

    { ER_NO_NAMESPACE_DECL,
        "Predpona {0} nima ustrezne deklaracije imenskega prostora"},

    { ER_ELEM_CONTENT_NOT_ALLOWED,
        "Vsebina elementa za lang=javaclass {0} ni dovoljena"},

    { ER_STYLESHEET_DIRECTED_TERMINATION,
        "Prekinitev usmerja slogovna datoteka"},

    { ER_ONE_OR_TWO,
        "1 ali 2"},

    { ER_TWO_OR_THREE,
        "2 ali 3"},

    { ER_COULD_NOT_LOAD_RESOURCE,
        "Nisem mogel nalo\u017eiti {0} (preverite CLASSPATH), trenutno se uporabljajo privzete vrednosti"},

    { ER_CANNOT_INIT_DEFAULT_TEMPLATES,
        "Ne morem inicializirati privzetih predlog"},

    { ER_RESULT_NULL,
        "Rezultat naj ne bi bil NULL"},

    { ER_RESULT_COULD_NOT_BE_SET,
        "Rezultata ni bilo mogo\u010de nastaviti"},

    { ER_NO_OUTPUT_SPECIFIED,
        "Izhod ni naveden"},

    { ER_CANNOT_TRANSFORM_TO_RESULT_TYPE,
        "Ne morem pretvoriti v rezultat tipa {0}"},

    { ER_CANNOT_TRANSFORM_SOURCE_TYPE,
        "Ne morem pretvoriti vira tipa {0}"},

    { ER_NULL_CONTENT_HANDLER,
        "Program za obravnavo vsebine NULL"},

    { ER_NULL_ERROR_HANDLER,
        "Program za obravnavo napak NULL"},

    { ER_CANNOT_CALL_PARSE,
        "klic raz\u010dlenitve ni mo\u017een \u010de ContentHandler ni bil nastavljen"},

    { ER_NO_PARENT_FOR_FILTER,
        "Ni nadrejenega za filter"},

    { ER_NO_STYLESHEET_IN_MEDIA,
         "Ni mogo\u010de najti slogovne datoteke v: {0}, medij= {1}"},

    { ER_NO_STYLESHEET_PI,
         "Ne najdem xml-stylesheet PI v: {0}"},

    { ER_NOT_SUPPORTED,
       "Ni podprto: {0}"},

    { ER_PROPERTY_VALUE_BOOLEAN,
       "Vrednost lastnosti {0} bi morala biti ponovitev logi\u010dne vrednosti"},

    { ER_COULD_NOT_FIND_EXTERN_SCRIPT,
         "Ne morem dostopati do zunanje skripte na {0}"},

    { ER_RESOURCE_COULD_NOT_FIND,
        "Vira [ {0} ] ni mogo\u010de najti.\n {1}"},

    { ER_OUTPUT_PROPERTY_NOT_RECOGNIZED,
        "Izhodna lastnost ni prepoznana: {0}"},

    { ER_FAILED_CREATING_ELEMLITRSLT,
        "Priprava primerka ElemLiteralResult ni uspela"},

  //Earlier (JDK 1.4 XALAN 2.2-D11) at key code '204' the key name was ER_PRIORITY_NOT_PARSABLE
  // In latest Xalan code base key name is  ER_VALUE_SHOULD_BE_NUMBER. This should also be taken care
  //in locale specific files like XSLTErrorResources_de.java, XSLTErrorResources_fr.java etc.
  //NOTE: Not only the key name but message has also been changed.

    { ER_VALUE_SHOULD_BE_NUMBER,
        "Vrednost za {0} bi morala biti \u0161tevilka, ki jo je mogo\u010de raz\u010dleniti"},

    { ER_VALUE_SHOULD_EQUAL,
        "Vrednost za {0} bi morala biti enaka da ali ne"},

    { ER_FAILED_CALLING_METHOD,
        "Klic metode {0} ni uspel"},

    { ER_FAILED_CREATING_ELEMTMPL,
        "Priprava primerka ElemTemplateElement ni uspela"},

    { ER_CHARS_NOT_ALLOWED,
        "V tem trenutku znaki v dokumentu niso na voljo"},

    { ER_ATTR_NOT_ALLOWED,
        "Atribut \"{0}\" v elementu {1} ni dovoljen!"},

    { ER_BAD_VALUE,
     "{0} slaba vrednost {1} "},

    { ER_ATTRIB_VALUE_NOT_FOUND,
     "Vrednosti atributa {0} ni bilo mogo\u010de najti "},

    { ER_ATTRIB_VALUE_NOT_RECOGNIZED,
     "Vrednosti atributa {0} ni bilo mogo\u010de prepoznati "},

    { ER_NULL_URI_NAMESPACE,
     "Posku\u0161am generirati predpono imenskega prostora z URI z vrednostjo NULL"},

  //New ERROR keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    { ER_NUMBER_TOO_BIG,
     "Poskus oblikovanja \u0161tevilke, ve\u010dje od najve\u010djega dolgega celega \u0161tevila"},

    { ER_CANNOT_FIND_SAX1_DRIVER,
     "Ne najdem razreda gonilnika SAX1 {0}"},

    { ER_SAX1_DRIVER_NOT_LOADED,
     "Na\u0161el razred gonilnika SAX1 {0}, vendar ga ne morem nalo\u017eiti"},

    { ER_SAX1_DRIVER_NOT_INSTANTIATED,
     "Nalo\u017eil razred gonilnika SAX1 {0}, vendar ga ne morem udejaniti"},

    { ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER,
     "Razred gonilnika SAX1 {0} ne vklju\u010duje org.xml.sax.Parser"},

    { ER_PARSER_PROPERTY_NOT_SPECIFIED,
     "Sistemska lastnost org.xml.sax.parser ni dolo\u010dena"},

    { ER_PARSER_ARG_CANNOT_BE_NULL,
     "Argument raz\u010dlenjevalnika sme biti NULL"},

    { ER_FEATURE,
     "Zna\u010dilnost: {0}"},

    { ER_PROPERTY,
     "Lastnost: {0}"},

    { ER_NULL_ENTITY_RESOLVER,
     "Razre\u0161evalnik entitet NULL"},

    { ER_NULL_DTD_HANDLER,
     "Program za obravnavanje DTD z vrednostjo NULL"},

    { ER_NO_DRIVER_NAME_SPECIFIED,
     "Ime gonilnika ni dolo\u010deno!"},

    { ER_NO_URL_SPECIFIED,
     "URL ni dolo\u010den!"},

    { ER_POOLSIZE_LESS_THAN_ONE,
     "Zaloga je manj\u0161a od 1!"},

    { ER_INVALID_DRIVER_NAME,
     "Dolo\u010deno neveljavno ime gonilnika!"},

    { ER_ERRORLISTENER,
     "ErrorListener"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The name
//   'ElemTemplateElement' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_NO_TEMPLATE_PARENT,
     "Programerjeva napaka! Izraz nima nadrejenega ElemTemplateElement!"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The substitution text
//   provides further information in order to diagnose the problem.  The name
//   'RedundentExprEliminator' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR,
     "Programerjeva izjava v RedundentExprEliminator: {0}"},

    { ER_NOT_ALLOWED_IN_POSITION,
     "Na tem polo\u017eaju v slogovni datoteki {0} ni dovoljen!"},

    { ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION,
     "Besedilo, ki niso presledki in drugi podobni znaki, na tem polo\u017eaju v slogovni datoteki ni dovoljeno.!"},

  // This code is shared with warning codes.
  // SystemId Unknown
    { INVALID_TCHAR,
     "Neveljavna vrednost: {1} uporabljena za atribut CHAR: {0}.  Atribut tipa CHAR mora biti samo 1 znak!"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "QNAME" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value and {0} is the attribute name.
    //The following codes are shared with the warning codes...
    { INVALID_QNAME,
     "Neveljavna vrednost: {1} uporabljena za atribut QNAME: {0}"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "ENUM" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value, {0} is the attribute name, and {2} is a list of valid
    // values.
    { INVALID_ENUM,
     "Neveljavna vrednost: {1} uporabljena za atribut ENUM: {0}.  Veljavne vrednosti so: {2}."},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NMTOKEN" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NMTOKEN,
     "Neveljavna vrednost: {1} uporabljena za atribut NMTOKEN: {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NCNAME" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NCNAME,
     "Neveljavna vrednost: {1} uporabljena za atribut NCNAME: {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "boolean" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_BOOLEAN,
     "Neveljavna vrednost: {1} uporabljena za atribut boolean: {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "number" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
     { INVALID_NUMBER,
     "Neveljavna vrednost: {1} uporabljena za atribut number: {0} "},


  // End of shared codes...

// Note to translators:  A "match pattern" is a special form of XPath expression
// that is used for matching patterns.  The substitution text is the name of
// a function.  The message indicates that when this function is referenced in
// a match pattern, its argument must be a string literal (or constant.)
// ER_ARG_LITERAL - new error message for bugzilla //5202
    { ER_ARG_LITERAL,
     "Argument za {0} v primerjalnem vzorcu mora biti dobesedni niz."},

// Note to translators:  The following message indicates that two definitions of
// a variable.  A "global variable" is a variable that is accessible everywher
// in the stylesheet.
// ER_DUPLICATE_GLOBAL_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_GLOBAL_VAR,
     "Dvojnik deklaracije globalne spremenljivke."},


// Note to translators:  The following message indicates that two definitions of
// a variable were encountered.
// ER_DUPLICATE_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_VAR,
     "Dvojnik deklaracije spremenljivke."},

    // Note to translators:  "xsl:template, "name" and "match" are XSLT keywords
    // which must not be translated.
    // ER_TEMPLATE_NAME_MATCH - new error message for bugzilla #789
    { ER_TEMPLATE_NAME_MATCH,
     "xsl:template mora vsebovati atribut name ali match (ali oba)"},

    // Note to translators:  "exclude-result-prefixes" is an XSLT keyword which
    // should not be translated.  The message indicates that a namespace prefix
    // encountered as part of the value of the exclude-result-prefixes attribute
    // was in error.
    // ER_INVALID_PREFIX - new error message for bugzilla #788
    { ER_INVALID_PREFIX,
     "Predpona v izklju\u010di-predpone-rezultatov (exclude-result-prefixes) ni veljavna: {0}"},

    // Note to translators:  An "attribute set" is a set of attributes that can
    // be added to an element in the output document as a group.  The message
    // indicates that there was a reference to an attribute set named {0} that
    // was never defined.
    // ER_NO_ATTRIB_SET - new error message for bugzilla #782
    { ER_NO_ATTRIB_SET,
     "Nabor atributov, imenovana {0}, ne obstaja"},

    // Note to translators:  This message indicates that there was a reference
    // to a function named {0} for which no function definition could be found.
    { ER_FUNCTION_NOT_FOUND,
     "Funkcija, imenovana {0}, ne obstaja"},

    // Note to translators:  This message indicates that the XSLT instruction
    // that is named by the substitution text {0} must not contain other XSLT
    // instructions (content) or a "select" attribute.  The word "select" is
    // an XSLT keyword in this case and must not be translated.
    { ER_CANT_HAVE_CONTENT_AND_SELECT,
     "Element {0} ne sme imeti vsebine in atributa izbire hkrati."},

    // Note to translators:  This message indicates that the value argument
    // of setParameter must be a valid Java Object.
    { ER_INVALID_SET_PARAM_VALUE,
     "Vrednost parametra {0} mora biti veljaven javanski objekt"},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT,
         "Atribut result-prefix elementa xsl:namespace-alias element ima vrednost '#default' (privzeto), ampak ni deklaracije privzetega imenskega prostora v razponu za ta element."},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX,
         "Atribut result-prefix elementa xsl:namespace-alias ima vrednost ''{0}'', ampak ni deklaracije privzetega imenskega prostora za predpono ''{0}'' v razponu za ta element."},

    { ER_SET_FEATURE_NULL_NAME,
      "Ime funkcije ne sme biti null v TransformerFactory.getFeature(Ime niza, vrednost boolean)."},

    { ER_GET_FEATURE_NULL_NAME,
      "Ime funkcije ne sme biti null v TransformerFactory.getFeature(Ime niza)."},

    { ER_UNSUPPORTED_FEATURE,
      "Ni mogo\u010de nastaviti funkcije ''{0}'' v tem TransformerFactory."},

    { ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING,
        "Uporaba raz\u0161iritvene elementa ''{0}'' ni na voljo, ko je funkcija varnega procesiranja nastavljena na true."},

        { ER_NAMESPACE_CONTEXT_NULL_NAMESPACE,
          "Ni mogo\u010de dobiti predpone za URI imenskega prostora null."},

        { ER_NAMESPACE_CONTEXT_NULL_PREFIX,
          "Ni mogo\u010de dobiti URI-ja imenskega prostora za predpono null."},

        { ER_XPATH_RESOLVER_NULL_QNAME,
          "Ime funkcije ne more biti ni\u010d."},

        { ER_XPATH_RESOLVER_NEGATIVE_ARITY,
          "\u0160tevilo argumentov ne more biti negativno"},

  // Warnings...

    { WG_FOUND_CURLYBRACE,
      "Najden '}' vendar ni odprtih predlog atributov!"},

    { WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR,
      "Opozorilo: \u0161tevni atribut ni skladen s prednikom v xsl:number! Cilj = {0}"},

    { WG_EXPR_ATTRIB_CHANGED_TO_SELECT,
      "Stara sintaksa: Ime atributa 'izraz' je bilo spremenjeno v 'izbira'."},

    { WG_NO_LOCALE_IN_FORMATNUMBER,
      "Xalan \u0161e ne podpira podro\u010dnih imen v funkciji za oblikovanje \u0161tevilk."},

    { WG_LOCALE_NOT_FOUND,
      "Opozorilo: ne najdem podro\u010dnih nastavitev za xml:lang={0}"},

    { WG_CANNOT_MAKE_URL_FROM,
      "Iz {0} ni mogo\u010de narediti naslova URL."},

    { WG_CANNOT_LOAD_REQUESTED_DOC,
      "Ne morem nalo\u017eiti zahtevanega dokumenta: {0}"},

    { WG_CANNOT_FIND_COLLATOR,
      "Ne najdem kolacionista (collator) za <sort xml:lang={0}"},

    { WG_FUNCTIONS_SHOULD_USE_URL,
      "Stara sintaksa: navodilo za funkcije bi moralo uporabljati URL {0}"},

    { WG_ENCODING_NOT_SUPPORTED_USING_UTF8,
      "Kodiranje ni podprto: {0}, uporabljen bo UTF-8"},

    { WG_ENCODING_NOT_SUPPORTED_USING_JAVA,
      "kodiranje ni podprto: {0}, uporabljena bo Java {1}"},

    { WG_SPECIFICITY_CONFLICTS,
      "Spori pri specifi\u010dnosti: uporabljen bo zadnji najdeni {0} v slogovni datoteki."},

    { WG_PARSING_AND_PREPARING,
      "========= Poteka raz\u010dlenjevanje in priprava {0} =========="},

    { WG_ATTR_TEMPLATE,
     "Predloga atributa, {0}"},

    { WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE,
      "Spor ujemanja med xsl:strip-space in xsl:preserve-space"},

    { WG_ATTRIB_NOT_HANDLED,
      "Xalan \u0161e ne podpira atributa {0}!"},

    { WG_NO_DECIMALFORMAT_DECLARATION,
      "Deklaracije za decimalno obliko ni bilo mogo\u010de najti: {0}"},

    { WG_OLD_XSLT_NS,
     "Manjkajo\u010d ali nepravilen imenski prostor XSLT. "},

    { WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED,
      "Dovoljena je samo ena privzeta deklaracija xsl:decimal-format."},

    { WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE,
      "Imena xsl:decimal-format morajo biti enoli\u010dna. Ime \"{0}\" je bilo podvojeno."},

    { WG_ILLEGAL_ATTRIBUTE,
      "{0} vsebuje neveljaven atribut: {1}"},

    { WG_COULD_NOT_RESOLVE_PREFIX,
      "Ne morem razre\u0161iti predpone imenskega prostora: {0}. Vozli\u0161\u010de bo prezrto."},

    { WG_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet zahteva atribut 'razli\u010dica'!"},

    { WG_ILLEGAL_ATTRIBUTE_NAME,
      "Neveljavno ime atributa: {0}"},

    { WG_ILLEGAL_ATTRIBUTE_VALUE,
      "Uporabljena neveljavna vrednost za atribut {0}: {1}"},

    { WG_EMPTY_SECOND_ARG,
      "Posledi\u010dna skupina vozli\u0161\u010d iz drugega argumenta funkcije dokumenta je prazna. Posredujte prazno skupino vozli\u0161\u010d."},

  //Following are the new WARNING keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.
    { WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "Vrednost atributa 'ime' iz imena xsl:processing-instruction ne sme biti 'xml'"},

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.  "NCName" is an XML data-type and must not be
    // translated.
    { WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "Vrednost atributa ''ime'' iz xsl:processing-instruction mora biti veljavno NCIme: {0}"},

    // Note to translators:  This message is reported if the stylesheet that is
    // being processed attempted to construct an XML document with an attribute in a
    // place other than on an element.  The substitution text specifies the name of
    // the attribute.
    { WG_ILLEGAL_ATTRIBUTE_POSITION,
      "Atributa {0} ne morem dodati za podrejenimi vozli\u0161\u010di ali pred izdelavo elementa.  Atribut bo prezrt."},

    { NO_MODIFICATION_ALLOWED_ERR,
      "Izveden je poskus spremembe objekta tam, kjer spremembe niso dovoljene."
    },

    //Check: WHY THERE IS A GAP B/W NUMBERS in the XSLTErrorResources properties file?

  // Other miscellaneous text used inside the code...
  { "ui_language", "sl"},
  {  "help_language",  "sl" },
  {  "language",  "sl" },
  { "BAD_CODE", "Parameter za createMessage presega meje"},
  {  "FORMAT_FAILED", "Med klicem messageFormat naletel na izjemo"},
  {  "version", ">>>>>>> Razli\u010dica Xalan "},
  {  "version2",  "<<<<<<<"},
  {  "yes", "da"},
  { "line", "Vrstica #"},
  { "column","Stolpec #"},
  { "xsldone", "XSLProcessor: dokon\u010dano"},


  // Note to translators:  The following messages provide usage information
  // for the Xalan Process command line.  "Process" is the name of a Java class,
  // and should not be translated.
  { "xslProc_option", "Ukazna vrstica Xalan-J Mo\u017enosti razreda postopka:"},
  { "xslProc_option", "Ukazna vrstica Xalan-J Mo\u017enosti razredov postopkov\u003a"},
  { "xslProc_invalid_xsltc_option", "Mo\u017enost {0} v na\u010dinu XSLTC ni podprta."},
  { "xslProc_invalid_xalan_option", "Mo\u017enost {0} se lahko uporablja samo z -XSLTC."},
  { "xslProc_no_input", "Napaka: ni dolo\u010dene slogovne datoteke ali vhodnega xml. Po\u017eenite ta ukaz, za katerega ni na voljo napotkov za uporabo."},
  { "xslProc_common_options", "-Splo\u0161ne mo\u017enosti-"},
  { "xslProc_xalan_options", "-Mo\u017enosti za Xalan-"},
  { "xslProc_xsltc_options", "-Mo\u017enosti za XSLTC-"},
  { "xslProc_return_to_continue", "(za nadaljevanje pritisnite <return>)"},

   // Note to translators: The option name and the parameter name do not need to
   // be translated. Only translate the messages in parentheses.  Note also that
   // leading whitespace in the messages is used to indent the usage information
   // for each option in the English messages.
   // Do not translate the keywords: XSLTC, SAX, DOM and DTM.
  { "optionXSLTC", "   [-XSLTC (za preoblikovanje uporabite XSLTC)]"},
  { "optionIN", "   [-IN vhodniXMLURL]"},
  { "optionXSL", "   [-XSL XSLPreoblikovanjeURL]"},
  { "optionOUT",  "   [-OUT ImeIzhodneDatoteke]"},
  { "optionLXCIN", "   [-LXCIN ImeVhodneDatotekePrevedeneSlogovneDatoteke]"},
  { "optionLXCOUT", "   [-LXCOUT ImeIzhodneDatotekePrevedeneSlogovneDatoteke]"},
  { "optionPARSER", "   [-PARSER popolnoma ustrezno ime razreda zveze raz\u010dlenjevalnika]"},
  {  "optionE", "   [-E (Ne raz\u0161irjajte sklicev entitet)]"},
  {  "optionV",  "   [-E (Ne raz\u0161irjajte sklicev entitet)]"},
  {  "optionQC", "   [-QC (Tiha opozorila o sporih vzorcev)]"},
  {  "optionQ", "   [-Q  (Tihi na\u010din)]"},
  {  "optionLF", "   [-LF (Uporabite pomike samo na izhodu {privzeto je CR/LF})]"},
  {  "optionCR", "   [-CR (Uporabite prehode v novo vrstico samo na izhodu {privzeto je CR/LF})]"},
  { "optionESCAPE", "   [-ESCAPE (Znaki za izogib {privzeto je <>&\"\'\\r\\n}]"},
  { "optionINDENT", "   [-INDENT (Koliko presledkov zamika {privzeto je 0})]"},
  { "optionTT", "   [-TT (Sledite predlogam glede na njihov poziv.)]"},
  { "optionTG", "   [-TG (Sledite vsakemu dogodku rodu.)]"},
  { "optionTS", "   [-TS (Sledite vsakemu dogodku izbire.)]"},
  {  "optionTTC", "   [-TTC (Sledite podrejenim predloge kot se obdelujejo.)]"},
  { "optionTCLASS", "   [-TCLASS (Razred TraceListener za kon\u010dnice sledi.)]"},
  { "optionVALIDATE", "   [-VALIDATE (Nastavi v primeru preverjanja veljavnosti.  Privzeta vrednost za preverjanje veljavnosti je izklopljeno.)]"},
  { "optionEDUMP", "   [-EDUMP {izbirno ime datoteke} (V primeru napake naredi izvoz skladov.)]"},
  {  "optionXML", "   [-XML (Uporabite oblikovalnik XML in dodajte glavo XML.)]"},
  {  "optionTEXT", "   [-TEXT (Uporabite preprost oblikovalnik besedila.)]"},
  {  "optionHTML", "   [-HTML (Uporabite oblikovalnik za HTML.)]"},
  {  "optionPARAM", "   [-PARAM izraz imena (nastavite parameter slogovne datoteke)]"},
  {  "noParsermsg1", "Postopek XSL ni uspel."},
  {  "noParsermsg2", "** Nisem na\u0161el raz\u010dlenjevalnika **"},
  { "noParsermsg3",  "Preverite pot razreda."},
  { "noParsermsg4", "\u010ce nimate IBM raz\u010dlenjevalnika za Javo, ga lahko prenesete iz"},
  { "noParsermsg5", "IBM AlphaWorks: http://www.alphaworks.ibm.com/formula/xml"},
  { "optionURIRESOLVER", "   [-URIRESOLVER polno ime razreda (URIResolver za razre\u0161evanje URL-jev)]"},
  { "optionENTITYRESOLVER",  "   [-ENTITYRESOLVER polno ime razreda (EntityResolver za razre\u0161evanje entitet)]"},
  { "optionCONTENTHANDLER",  "   [-CONTENTHANDLER polno ime razreda (ContentHandler za serializacijo izhoda)]"},
  {  "optionLINENUMBERS",  "   [-L za izvorni dokument uporabite \u0161tevilke vrstic]"},
  { "optionSECUREPROCESSING", "   [-SECURE (nastavite funkcijo varne obdelave na True.)]"},

    // Following are the new options added in XSLTErrorResources.properties files after Jdk 1.4 (Xalan 2.2-D11)


  {  "optionMEDIA",  "   [-MEDIA TipMedija (z atributom medija poi\u0161\u010dite slogovno datoteko, ki se nana\u0161a na dokument.)]"},
  {  "optionFLAVOR",  "   [-FLAVOR ImePosebnosti (Za preoblikovanje izrecno uporabljajte s2s=SAX ali d2d=DOM.)] "}, // Added by sboag/scurcuru; experimental
  { "optionDIAG", "   [-DIAG (Natisnite skupni \u010das trajanja pretvorbe v milisekundah.)]"},
  { "optionINCREMENTAL",  "   [-INCREMENTAL (zahtevajte gradnjo prirastnega DTM tako, da nastavite http://xml.apache.org/xalan/features/incremental na resni\u010dno.)]"},
  {  "optionNOOPTIMIMIZE",  "   [-NOOPTIMIMIZE (prepre\u010dite obdelavo optimiziranja slogovne datoteke, tako da nastavite http://xml.apache.org/xalan/features/optimize na false.)]"},
  { "optionRL",  "   [-RL mejaRekurzije (zahtevajte numeri\u010dno mejo globine rekurzije slogovne datoteke.)]"},
  {   "optionXO",  "   [-XO [imeTransleta] (dodelite ime ustvarjenemu transletu)]"},
  {  "optionXD", "   [-XD ciljnaMapa (navedite ciljno mapo za translet)]"},
  {  "optionXJ",  "   [-XJ datotekaJar (zdru\u017ei razrede transleta v datoteko jar z imenom <jarfile>)]"},
  {   "optionXP",  "   [-XP paket (navede predpono imena paketa vsem ustvarjenim razredom transletov)]"},

  //AddITIONAL  STRINGS that need L10n
  // Note to translators:  The following message describes usage of a particular
  // command-line option that is used to enable the "template inlining"
  // optimization.  The optimization involves making a copy of the code
  // generated for a template in another template that refers to it.
  { "optionXN",  "   [-XN (omogo\u010da vstavljanje predlog)]" },
  { "optionXX",  "   [-XX (vklopi izhod za dodatna sporo\u010dila za iskanje napak)]"},
  { "optionXT" , "   [-XT (\u010de je mogo\u010de, uporabite translet za pretvorbo)]"},
  { "diagTiming"," --------- Pretvorba {0} prek {1} je trajala {2} ms" },
  { "recursionTooDeep","Predloga pregloboko vgnezdena. Gnezdenje = {0}, predloga {1} {2}" },
  { "nameIs", "ime je" },
  { "matchPatternIs", "primerjalni vzorec je" }

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
  public static final String ERROR_HEADER = "Napaka: ";

  /** String to prepend to warning messages.    */
  public static final String WARNING_HEADER = "Opozorilo: ";

  /** String to specify the XSLT module.  */
  public static final String XSL_HEADER = "XSLT ";

  /** String to specify the XML parser module.  */
  public static final String XML_HEADER = "XML ";

  /** I don't think this is used any more.
   * @deprecated  */
  public static final String QUERY_HEADER = "VZOREC ";


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
                new Locale("sl", "US"));
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
