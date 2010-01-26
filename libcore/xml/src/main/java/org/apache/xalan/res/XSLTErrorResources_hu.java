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
 * $Id: XSLTErrorResources_hu.java 468641 2006-10-28 06:54:42Z minchau $
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
public class XSLTErrorResources_hu extends ListResourceBundle
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
      "Hiba: Nem lehet '{' a kifejez\u00e9seken bel\u00fcl"},

    { ER_ILLEGAL_ATTRIBUTE ,
     "A(z) {0}-nak \u00e9rv\u00e9nytelen attrib\u00fatuma van: {1}"},

  {ER_NULL_SOURCENODE_APPLYIMPORTS ,
      "A sourceNode \u00e9rt\u00e9ke null az xsl:apply-imports met\u00f3dusban."},

  {ER_CANNOT_ADD,
      "Nem lehet a(z) {0}-t felvenni a(z) {1}-ba"},

    { ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES,
      "A sourceNode null a handleApplyTemplatesInstruction-ban!"},

    { ER_NO_NAME_ATTRIB,
     "A(z) {0}-nak kell legyen name attrib\u00fatuma."},

    {ER_TEMPLATE_NOT_FOUND,
     "Nem tal\u00e1lhat\u00f3 {0} nev\u0171 sablon"},

    {ER_CANT_RESOLVE_NAME_AVT,
      "Nem lehet feloldani a n\u00e9v AVT-t az xsl:call-template-ben."},

    {ER_REQUIRES_ATTRIB,
     "{0}-nek attrib\u00fatum sz\u00fcks\u00e9ges: {1}"},

    { ER_MUST_HAVE_TEST_ATTRIB,
      "A(z) {0} -nak kell legyen ''test'' attrib\u00fatuma. "},

    {ER_BAD_VAL_ON_LEVEL_ATTRIB,
      "Rossz \u00e9rt\u00e9k a level attrib\u00fatumban: {0}"},

    {ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "A feldolgoz\u00e1si utas\u00edt\u00e1s neve nem lehet 'xml'"},

    { ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "A feldolgoz\u00e1si utas\u00edt\u00e1s neve \u00e9rv\u00e9nyes NCName kell legyen: {0}"},

    { ER_NEED_MATCH_ATTRIB,
      "A(z) {0}-nek kell legyen illeszked\u00e9si attrib\u00fatuma, ha van m\u00f3dja."},

    { ER_NEED_NAME_OR_MATCH_ATTRIB,
      "A(z) {0}-nak kell vagy n\u00e9v vagy illeszked\u00e9si attrib\u00fatum."},

    {ER_CANT_RESOLVE_NSPREFIX,
      "Nem lehet feloldani a n\u00e9vt\u00e9r el\u0151tagot: {0}"},

    { ER_ILLEGAL_VALUE,
     "Az xml:space \u00e9rt\u00e9ke \u00e9rv\u00e9nytelen: {0}"},

    { ER_NO_OWNERDOC,
      "A lesz\u00e1rmazott csom\u00f3pontnak nincs tulajdonos dokumentuma!"},

    { ER_ELEMTEMPLATEELEM_ERR,
     "ElemTemplateElement hiba: {0}"},

    { ER_NULL_CHILD,
     "K\u00eds\u00e9rlet null lesz\u00e1rmazott felv\u00e9tel\u00e9re!"},

    { ER_NEED_SELECT_ATTRIB,
     "A(z) {0}-nak kell kiv\u00e1laszt\u00e1si attrib\u00fatum."},

    { ER_NEED_TEST_ATTRIB ,
      "Az xsl:when-nek kell legyen 'test' attrib\u00fatuma."},

    { ER_NEED_NAME_ATTRIB,
      "Az xsl:param-nak kell legyen 'name' attrib\u00fatuma."},

    { ER_NO_CONTEXT_OWNERDOC,
      "A k\u00f6rnyezetnek nincs tulajdonos dokumentuma!"},

    {ER_COULD_NOT_CREATE_XML_PROC_LIAISON,
      "Nem lehet XML TransformerFactory Liaison-t l\u00e9trehozni: {0}"},

    {ER_PROCESS_NOT_SUCCESSFUL,
      "A Xalan folyamat sikertelen volt."},

    { ER_NOT_SUCCESSFUL,
     "Xalan: sikertelen volt."},

    { ER_ENCODING_NOT_SUPPORTED,
     "A k\u00f3dol\u00e1s nem t\u00e1mogatott: {0}"},

    {ER_COULD_NOT_CREATE_TRACELISTENER,
      "Nem lehet TraceListener-t l\u00e9trehozni: {0}"},

    {ER_KEY_REQUIRES_NAME_ATTRIB,
      "Az xsl:key-nek kell legyen 'name' attrib\u00fatuma!"},

    { ER_KEY_REQUIRES_MATCH_ATTRIB,
      "Az xsl:key-nek kell legyen 'match' attrib\u00fatuma!"},

    { ER_KEY_REQUIRES_USE_ATTRIB,
      "Az xsl:key-nek kell legyen 'use' attrib\u00fatuma!"},

    { ER_REQUIRES_ELEMENTS_ATTRIB,
      "(StylesheetHandler) A(z) {0}-nak kell legyen ''elements'' attrib\u00fatuma! "},

    { ER_MISSING_PREFIX_ATTRIB,
      "(StylesheetHandler) A(z) {0}-nak hi\u00e1nyzik a ''prefix'' attrib\u00fatuma"},

    { ER_BAD_STYLESHEET_URL,
     "A st\u00edluslap URL rossz: {0}"},

    { ER_FILE_NOT_FOUND,
     "A st\u00edluslap f\u00e1jl nem tal\u00e1lhat\u00f3: {0}"},

    { ER_IOEXCEPTION,
      "IO kiv\u00e9tel t\u00f6rt\u00e9nt a st\u00edluslap f\u00e1jln\u00e1l: {0}"},

    { ER_NO_HREF_ATTRIB,
      "(StylesheetHandler) A(z) {0} href attrib\u00fatuma nem tal\u00e1lhat\u00f3"},

    { ER_STYLESHEET_INCLUDES_ITSELF,
      "(StylesheetHandler) A(z) {0} k\u00f6zvetlen\u00fcl vagy k\u00f6zvetetten tartalmazza saj\u00e1t mag\u00e1t!"},

    { ER_PROCESSINCLUDE_ERROR,
      "StylesheetHandler.processInclude hiba, {0}"},

    { ER_MISSING_LANG_ATTRIB,
      "(StylesheetHandler) A(z) {0}-nak hi\u00e1nyzik a ''lang'' attrib\u00fatuma "},

    { ER_MISSING_CONTAINER_ELEMENT_COMPONENT,
      "(StylesheetHandler) Rosszul elhelyezett {0} elem?? Hi\u00e1nyzik a ''component'' t\u00e1rol\u00f3elem"},

    { ER_CAN_ONLY_OUTPUT_TO_ELEMENT,
      "Csak egy Element-be, DocumentFragment-be, Document-be vagy PrintWriter-be lehet kimenetet k\u00fcldeni."},

    { ER_PROCESS_ERROR,
     "StylesheetRoot.process hiba"},

    { ER_UNIMPLNODE_ERROR,
     "UnImplNode hiba: {0}"},

    { ER_NO_SELECT_EXPRESSION,
      "Hiba! Az xpath kiv\u00e1laszt\u00e1si kifejez\u00e9s nem tal\u00e1lhat\u00f3 (-select)."},

    { ER_CANNOT_SERIALIZE_XSLPROCESSOR,
      "Nem lehet sorbarakni az XSLProcessor-t!"},

    { ER_NO_INPUT_STYLESHEET,
      "Nem adott meg st\u00edluslap bemenetet!"},

    { ER_FAILED_PROCESS_STYLESHEET,
      "Nem siker\u00fclt feldolgozni a st\u00edluslapot!"},

    { ER_COULDNT_PARSE_DOC,
     "Nem lehet elemezni a(z) {0} dokumentumot!"},

    { ER_COULDNT_FIND_FRAGMENT,
     "Nem tal\u00e1lhat\u00f3 a darab: {0}"},

    { ER_NODE_NOT_ELEMENT,
      "A darab azonos\u00edt\u00f3 \u00e1ltal mutatott csom\u00f3pont nem elem: {0}"},

    { ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB,
      "A for-each-nek legal\u00e1bb egy match vagy egy name attrib\u00fatuma kell legyen"},

    { ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB,
      "A sablonoknak vagy match vagy name attrib\u00fatumuk kell legyen"},

    { ER_NO_CLONE_OF_DOCUMENT_FRAG,
      "Nincs kl\u00f3nja egy dokumentumdarabnak!"},

    { ER_CANT_CREATE_ITEM,
      "Nem lehet elemet l\u00e9trehozni az eredm\u00e9nyf\u00e1ban: {0}"},

    { ER_XMLSPACE_ILLEGAL_VALUE,
      "Az xml:space-nek a forr\u00e1s XML-ben tiltott \u00e9rt\u00e9ke van: {0}"},

    { ER_NO_XSLKEY_DECLARATION,
      "Nincs xsl:key deklar\u00e1ci\u00f3 a(z) {0}-hoz!"},

    { ER_CANT_CREATE_URL,
     "Hiba! Nem lehet URL-t l\u00e9trehozni ehhez: {0}"},

    { ER_XSLFUNCTIONS_UNSUPPORTED,
     "Az xsl:functions nem t\u00e1mogatott"},

    { ER_PROCESSOR_ERROR,
     "XSLT TransformerFactory hiba"},

    { ER_NOT_ALLOWED_INSIDE_STYLESHEET,
      "(StylesheetHandler) A(z) {0} nem megengedett a st\u00edluslapon bel\u00fcl!"},

    { ER_RESULTNS_NOT_SUPPORTED,
      "A result-ns t\u00f6bb\u00e9 m\u00e1r nem t\u00e1mogatott!  Haszn\u00e1lja ink\u00e1bb az xsl:output-ot."},

    { ER_DEFAULTSPACE_NOT_SUPPORTED,
      "A default-space t\u00f6bb\u00e9 m\u00e1r nem t\u00e1mogatott!  Haszn\u00e1lja ink\u00e1bb az xsl:strip-space-t vagy az  xsl:preserve-space-t."},

    { ER_INDENTRESULT_NOT_SUPPORTED,
      "Az indent-result t\u00f6bb\u00e9 m\u00e1r nem t\u00e1mogatott!  Haszn\u00e1lja ink\u00e1bb az xsl:output-ot."},

    { ER_ILLEGAL_ATTRIB,
      "(StylesheetHandler) A(z) {0}-nak tiltott attrib\u00fatuma van: {1}"},

    { ER_UNKNOWN_XSL_ELEM,
     "Ismeretlen XSL elem: {0}"},

    { ER_BAD_XSLSORT_USE,
      "(StylesheetHandler) Az xsl:sort csak az xsl:apply-templates-szel vagy xsl:for-each-el egy\u00fctt haszn\u00e1lhat\u00f3."},

    { ER_MISPLACED_XSLWHEN,
      "(StylesheetHandler) Rosszul elhelyezett xsl:when!"},

    { ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) Az xsl:when sz\u00fcl\u0151je nem xsl:choose!"},

    { ER_MISPLACED_XSLOTHERWISE,
      "(StylesheetHandler) Rosszul elhelyezett xsl:otherwise!"},

    { ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) Az xsl:otherwise sz\u00fcl\u0151je nem xsl:choose!"},

    { ER_NOT_ALLOWED_INSIDE_TEMPLATE,
      "(StylesheetHandler) A(z) {0} nem megengedett sablonok belsej\u00e9ben!"},

    { ER_UNKNOWN_EXT_NS_PREFIX,
      "(StylesheetHandler) A(z) {0} kiterjeszt\u00e9s n\u00e9vt\u00e9r el\u0151tag {1} ismeretlen"},

    { ER_IMPORTS_AS_FIRST_ELEM,
      "(StylesheetHandler) Az import\u00e1l\u00e1sok csak a st\u00edluslap els\u0151 elemei lehetnek!"},

    { ER_IMPORTING_ITSELF,
      "(StylesheetHandler) A(z) {0} k\u00f6zvetlen\u00fcl vagy k\u00f6zvetve tartalmazza saj\u00e1t mag\u00e1t!"},

    { ER_XMLSPACE_ILLEGAL_VAL,
      "(StylesheetHandler) xml:space \u00e9rt\u00e9ke nem megengedett: {0}"},

    { ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL,
      "A processStylesheet sikertelen volt!"},

    { ER_SAX_EXCEPTION,
     "SAX kiv\u00e9tel"},

//  add this message to fix bug 21478
    { ER_FUNCTION_NOT_SUPPORTED,
     "A f\u00fcggv\u00e9ny nem t\u00e1mogatott!"},


    { ER_XSLT_ERROR,
     "XSLT hiba"},

    { ER_CURRENCY_SIGN_ILLEGAL,
      "A p\u00e9nzjel nem megengedett a form\u00e1tum minta karakterl\u00e1ncban"},

    { ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM,
      "A document funkci\u00f3 nem t\u00e1mogatott a Stylesheet DOM-ban!"},

    { ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER,
      "Nem lehet feloldani az el\u0151tagot egy nem-el\u0151tag felold\u00f3nak!"},

    { ER_REDIRECT_COULDNT_GET_FILENAME,
      "\u00c1tir\u00e1ny\u00edt\u00e1s kiterjeszt\u00e9s: Nem lehet megkapni a f\u00e1jlnevet - a file vagy select attrib\u00fatumnak egy \u00e9rv\u00e9nyes karakterl\u00e1ncot kell visszaadnia."},

    { ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT,
      "Nem lehet FormatterListener-t \u00e9p\u00edteni az \u00e1tir\u00e1ny\u00edt\u00e1s kiterjeszt\u00e9sben!"},

    { ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX,
      "Az el\u0151tag az exclude-result-prefixes-ben nem \u00e9rv\u00e9nyes: {0}"},

    { ER_MISSING_NS_URI,
      "Hi\u00e1nyzik a megadott el\u0151tag n\u00e9vt\u00e9r URI-ja"},

    { ER_MISSING_ARG_FOR_OPTION,
      "Hi\u00e1nyzik az opci\u00f3 argumentuma: {0}"},

    { ER_INVALID_OPTION,
     "\u00c9rv\u00e9nytelen opci\u00f3: {0}"},

    { ER_MALFORMED_FORMAT_STRING,
     "Rossz form\u00e1tum\u00fa karakterl\u00e1nc: {0}"},

    { ER_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "Az xsl:stylesheet-nek kell legyen 'version' attrib\u00fatuma!"},

    { ER_ILLEGAL_ATTRIBUTE_VALUE,
      "A(z) {0} attib\u00fatum \u00e9rt\u00e9ke \u00e9rv\u00e9nytelen: {1}"},

    { ER_CHOOSE_REQUIRES_WHEN,
     "Az xsl:choose-hoz egy xsl:when sz\u00fcks\u00e9ges"},

    { ER_NO_APPLY_IMPORT_IN_FOR_EACH,
      "Az xsl:apply-imports nem megengedett xsl:for-each-en bel\u00fcl"},

    { ER_CANT_USE_DTM_FOR_OUTPUT,
      "Nem haszn\u00e1lhat DTMLiaison-t kimeneti DOM csom\u00f3pontk\u00e9nt... adjon \u00e1t ink\u00e1bb egy org.apache.xpath.DOM2Helper-t!"},

    { ER_CANT_USE_DTM_FOR_INPUT,
      "Nem haszn\u00e1lhat DTMLiaison-t bemeneti DOM csom\u00f3pontk\u00e9nt... adjon \u00e1t ink\u00e1bb egy org.apache.xpath.DOM2Helper-t!"},

    { ER_CALL_TO_EXT_FAILED,
      "A kiterjeszt\u00e9s-elem megh\u00edv\u00e1sa sikertelen volt: {0}"},

    { ER_PREFIX_MUST_RESOLVE,
      "Az el\u0151tagnak egy n\u00e9vt\u00e9rre kell felold\u00f3dnia: {0}"},

    { ER_INVALID_UTF16_SURROGATE,
      "\u00c9rv\u00e9nytelen UTF-16 helyettes\u00edt\u00e9s: {0} ?"},

    { ER_XSLATTRSET_USED_ITSELF,
      "A(z) {0} xsl:attribute-set-et saj\u00e1t mag\u00e1val haszn\u00e1lta, ami v\u00e9gtelen ciklust eredm\u00e9nyez."},

    { ER_CANNOT_MIX_XERCESDOM,
      "Nem keverheti a nem Xerces-DOM bemenetet a Xerces-DOM kimenettel!"},

    { ER_TOO_MANY_LISTENERS,
      "addTraceListenersToStylesheet - TooManyListenersException"},

    { ER_IN_ELEMTEMPLATEELEM_READOBJECT,
      "Az ElemTemplateElement.readObject met\u00f3dusban: {0}"},

    { ER_DUPLICATE_NAMED_TEMPLATE,
      "Egyn\u00e9l t\u00f6bb ''{0}'' nev\u0171 sablont tal\u00e1ltam"},

    { ER_INVALID_KEY_CALL,
      "\u00c9rv\u00e9nytelen f\u00fcggv\u00e9nyh\u00edv\u00e1s: rekurz\u00edv key() h\u00edv\u00e1sok nem megengedettek"},

    { ER_REFERENCING_ITSELF,
      "A(z) {0} v\u00e1ltoz\u00f3 k\u00f6zvetlen\u00fcl vagy k\u00f6zvetve \u00f6nmag\u00e1ra hivatkozik!"},

    { ER_ILLEGAL_DOMSOURCE_INPUT,
      "A bemeneti csom\u00f3pont nem lehet null egy DOMSource-ban a newTemplates-hez!"},

    { ER_CLASS_NOT_FOUND_FOR_OPTION,
        "Az oszt\u00e1ly f\u00e1jl nem tal\u00e1lhat\u00f3 a(z) {0} opci\u00f3hoz"},

    { ER_REQUIRED_ELEM_NOT_FOUND,
        "A sz\u00fcks\u00e9ges elem nem tal\u00e1lhat\u00f3: {0}"},

    { ER_INPUT_CANNOT_BE_NULL,
        "Az InputStream nem lehet null"},

    { ER_URI_CANNOT_BE_NULL,
        "Az URI nem lehet null"},

    { ER_FILE_CANNOT_BE_NULL,
        "A f\u00e1jl nem lehet null"},

    { ER_SOURCE_CANNOT_BE_NULL,
                "Az InputSource nem lehet null"},

    { ER_CANNOT_INIT_BSFMGR,
                "Nem lehet inicializ\u00e1lni a BSF kezel\u0151t"},

    { ER_CANNOT_CMPL_EXTENSN,
                "Nem lehet leford\u00edtani a kiterjeszt\u00e9st"},

    { ER_CANNOT_CREATE_EXTENSN,
      "Nem lehet l\u00e9trehozni a kiterjeszt\u00e9st ({0}) {1} miatt"},

    { ER_INSTANCE_MTHD_CALL_REQUIRES,
      "Az {0} met\u00f3dus p\u00e9ld\u00e1ny met\u00f3dush\u00edv\u00e1s\u00e1hoz sz\u00fcks\u00e9g van egy objektump\u00e9ld\u00e1nyra els\u0151 argumentumk\u00e9nt"},

    { ER_INVALID_ELEMENT_NAME,
      "\u00c9rv\u00e9nytelen elemnevet adott meg {0}"},

    { ER_ELEMENT_NAME_METHOD_STATIC,
      "Az elemn\u00e9v met\u00f3dus statikus {0} kell legyen"},

    { ER_EXTENSION_FUNC_UNKNOWN,
             "{0} kiterjeszt\u00e9s funkci\u00f3 : A(z) {1} ismeretlen"},

    { ER_MORE_MATCH_CONSTRUCTOR,
             "T\u00f6bb legjobb illeszked\u00e9s a(z) {0} konstruktor\u00e1ra"},

    { ER_MORE_MATCH_METHOD,
             "T\u00f6bb legjobb illeszked\u00e9s a(z) {0} met\u00f3dusra"},

    { ER_MORE_MATCH_ELEMENT,
             "T\u00f6bb legjobb illeszked\u00e9s a(z) {0} elem met\u00f3dusra"},

    { ER_INVALID_CONTEXT_PASSED,
             "\u00c9rv\u00e9nytelen k\u00f6rnyzetet adott \u00e1t a(z) {0} ki\u00e9rt\u00e9kel\u00e9s\u00e9hez"},

    { ER_POOL_EXISTS,
             "A t\u00e1rol\u00f3 m\u00e1r l\u00e9tezik"},

    { ER_NO_DRIVER_NAME,
             "Nem adott meg meghajt\u00f3nevet"},

    { ER_NO_URL,
             "Nem adott meg URL-t"},

    { ER_POOL_SIZE_LESSTHAN_ONE,
             "A t\u00e1rol\u00f3 m\u00e9rete egyn\u00e9l kisebb!"},

    { ER_INVALID_DRIVER,
             "\u00c9rv\u00e9nytelen meghajt\u00f3nevet adott meg!"},

    { ER_NO_STYLESHEETROOT,
             "Nem tal\u00e1lhat\u00f3 a st\u00edluslap gy\u00f6kere!"},

    { ER_ILLEGAL_XMLSPACE_VALUE,
         "Tiltott \u00e9rt\u00e9k az xml:space-hez"},

    { ER_PROCESSFROMNODE_FAILED,
         "A processFromNode nem siker\u00fclt"},

    { ER_RESOURCE_COULD_NOT_LOAD,
        "Az er\u0151forr\u00e1st [ {0} ] nem lehet bet\u00f6lteni: {1} \n {2} \t {3}"},

    { ER_BUFFER_SIZE_LESSTHAN_ZERO,
        "Pufferm\u00e9ret <= 0"},

    { ER_UNKNOWN_ERROR_CALLING_EXTENSION,
        "Ismeretlen hiba a kiterjeszt\u00e9s h\u00edv\u00e1s\u00e1n\u00e1l"},

    { ER_NO_NAMESPACE_DECL,
        "A(z) {0} el\u0151taghoz nem tartozik n\u00e9vt\u00e9r deklar\u00e1ci\u00f3"},

    { ER_ELEM_CONTENT_NOT_ALLOWED,
        "Elem tartalom nem megengedett a(z) {0} lang=javaclass-hoz"},

    { ER_STYLESHEET_DIRECTED_TERMINATION,
        "St\u00edluslap \u00e1ltal ir\u00e1ny\u00edtott le\u00e1ll\u00e1s"},

    { ER_ONE_OR_TWO,
        "1 vagy 2"},

    { ER_TWO_OR_THREE,
        "2 vagy 3"},

    { ER_COULD_NOT_LOAD_RESOURCE,
        "Nem lehet bet\u00f6lteni a(z) {0}-t (ellen\u0151rizze a CLASSPATH-t), most csak az alap\u00e9rtelmez\u00e9seket haszn\u00e1ljuk"},

    { ER_CANNOT_INIT_DEFAULT_TEMPLATES,
        "Nem lehet inicializ\u00e1lni az alap\u00e9rtelmezett sablonokat"},

    { ER_RESULT_NULL,
        "Az eredm\u00e9ny nem lehet null"},

    { ER_RESULT_COULD_NOT_BE_SET,
        "Nem lehet be\u00e1ll\u00edtani az eredm\u00e9nyt"},

    { ER_NO_OUTPUT_SPECIFIED,
        "Nem adott meg kimenetet"},

    { ER_CANNOT_TRANSFORM_TO_RESULT_TYPE,
        "Nem alak\u00edthat\u00f3 \u00e1t {0} t\u00edpus\u00fa eredm\u00e9nny\u00e9"},

    { ER_CANNOT_TRANSFORM_SOURCE_TYPE,
        "A(z) {0} t\u00edpus\u00fa forr\u00e1s nem alak\u00edthat\u00f3 \u00e1t "},

    { ER_NULL_CONTENT_HANDLER,
        "Null tartalomkezel\u0151"},

    { ER_NULL_ERROR_HANDLER,
        "Null hibakezel\u0151"},

    { ER_CANNOT_CALL_PARSE,
        "A parse nem h\u00edvhat\u00f3 meg, ha a ContentHandler-t nem \u00e1ll\u00edtotta be"},

    { ER_NO_PARENT_FOR_FILTER,
        "A sz\u0171r\u0151nek nincs sz\u00fcl\u0151je"},

    { ER_NO_STYLESHEET_IN_MEDIA,
         "Nincs st\u00edluslap ebben: {0}, adathordoz\u00f3: {1}"},

    { ER_NO_STYLESHEET_PI,
         "Nem tal\u00e1lhat\u00f3 xml-stylesheet PI itt: {0}"},

    { ER_NOT_SUPPORTED,
       "Nem t\u00e1mogatott: {0}"},

    { ER_PROPERTY_VALUE_BOOLEAN,
       "A(z) {0} tulajdons\u00e1g \u00e9rt\u00e9ke Boolean p\u00e9ld\u00e1ny kell legyen"},

    { ER_COULD_NOT_FIND_EXTERN_SCRIPT,
         "Nem lehet eljutni a k\u00fcls\u0151 parancsf\u00e1jlhoz a(z) {0}-n"},

    { ER_RESOURCE_COULD_NOT_FIND,
        "A(z) [ {0} ] er\u0151forr\u00e1s nem tal\u00e1lhat\u00f3.\n {1}"},

    { ER_OUTPUT_PROPERTY_NOT_RECOGNIZED,
        "A kimeneti tulajdons\u00e1g nem felismerhet\u0151: {0}"},

    { ER_FAILED_CREATING_ELEMLITRSLT,
        "Nem siker\u00fclt ElemLiteralResult p\u00e9ld\u00e1nyt l\u00e9trehozni"},

  //Earlier (JDK 1.4 XALAN 2.2-D11) at key code '204' the key name was ER_PRIORITY_NOT_PARSABLE
  // In latest Xalan code base key name is  ER_VALUE_SHOULD_BE_NUMBER. This should also be taken care
  //in locale specific files like XSLTErrorResources_de.java, XSLTErrorResources_fr.java etc.
  //NOTE: Not only the key name but message has also been changed.

    { ER_VALUE_SHOULD_BE_NUMBER,
        "A(z) {0} tulajdons\u00e1g \u00e9rt\u00e9ke \u00e9rtelmezhet\u0151 sz\u00e1m kell legyen"},

    { ER_VALUE_SHOULD_EQUAL,
        "A(z) {0} \u00e9rt\u00e9ke igen vagy nem kell legyen"},

    { ER_FAILED_CALLING_METHOD,
        "Nem siker\u00fclt megh\u00edvni a(z) {0} met\u00f3dust"},

    { ER_FAILED_CREATING_ELEMTMPL,
        "Nem siker\u00fclt ElemTemplateElement p\u00e9ld\u00e1nyt l\u00e9trehozni"},

    { ER_CHARS_NOT_ALLOWED,
        "Karakterek nem megengedettek a dokumentumnak ezen a pontj\u00e1n"},

    { ER_ATTR_NOT_ALLOWED,
        "A(z) \"{0}\" attrib\u00fatum nem megengedett a(z) {1} elemhez!"},

    { ER_BAD_VALUE,
     "{0} rossz \u00e9rt\u00e9k {1} "},

    { ER_ATTRIB_VALUE_NOT_FOUND,
     "{0} attrib\u00fatum \u00e9rt\u00e9k nem tal\u00e1lhat\u00f3 "},

    { ER_ATTRIB_VALUE_NOT_RECOGNIZED,
     "{0} attrib\u00fatum \u00e9rt\u00e9k ismeretlen "},

    { ER_NULL_URI_NAMESPACE,
     "K\u00eds\u00e9rlet egy n\u00e9vt\u00e9r el\u0151tag l\u00e9trehoz\u00e1s\u00e1ra null URI-val"},

  //New ERROR keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    { ER_NUMBER_TOO_BIG,
     "K\u00eds\u00e9rlet egy sz\u00e1m megform\u00e1z\u00e1s\u00e1ra, ami nagyobb, mint a legnagyobb Long eg\u00e9sz"},

    { ER_CANNOT_FIND_SAX1_DRIVER,
     "Nem tal\u00e1lhat\u00f3 a(z) {0} SAX1 meghajt\u00f3oszt\u00e1ly"},

    { ER_SAX1_DRIVER_NOT_LOADED,
     "A(z) {0} SAX1 meghajt\u00f3oszt\u00e1ly megvan, de nem t\u00f6lthet\u0151 be"},

    { ER_SAX1_DRIVER_NOT_INSTANTIATED,
     "A(z) {0} SAX1 meghajt\u00f3oszt\u00e1ly bet\u00f6ltve, de nem lehet p\u00e9ld\u00e1nyt l\u00e9trehozni bel\u0151le"},

    { ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER,
     "A(z) {0} SAX1 meghajt\u00f3oszt\u00e1ly nem implement\u00e1lja az org.xml.sax.Parser-t"},

    { ER_PARSER_PROPERTY_NOT_SPECIFIED,
     "Nem adta meg az org.xml.sax.parser rendszertulajdons\u00e1got"},

    { ER_PARSER_ARG_CANNOT_BE_NULL,
     "Az \u00e9rtelmez\u0151 argumentuma nem lehet null"},

    { ER_FEATURE,
     "K\u00e9pess\u00e9g: {0}"},

    { ER_PROPERTY,
     "Tulajdons\u00e1g: {0}"},

    { ER_NULL_ENTITY_RESOLVER,
     "Null entit\u00e1s felold\u00f3"},

    { ER_NULL_DTD_HANDLER,
     "Null DTD kezel\u0151"},

    { ER_NO_DRIVER_NAME_SPECIFIED,
     "Nem adott meg meghajt\u00f3nevet!"},

    { ER_NO_URL_SPECIFIED,
     "Nem adott meg URL-t!"},

    { ER_POOLSIZE_LESS_THAN_ONE,
     "A t\u00e1rol\u00f3 m\u00e9rete 1-n\u00e9l kisebb!"},

    { ER_INVALID_DRIVER_NAME,
     "\u00c9rv\u00e9nytelen meghajt\u00f3nevet adott meg!"},

    { ER_ERRORLISTENER,
     "ErrorListener"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The name
//   'ElemTemplateElement' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_NO_TEMPLATE_PARENT,
     "Programoz\u00f3i hiba! A kifejez\u00e9snek nincs ElemTemplateElement sz\u00fcl\u0151je!"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The substitution text
//   provides further information in order to diagnose the problem.  The name
//   'RedundentExprEliminator' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR,
     "Programoz\u00f3i \u00e9rtes\u00edt\u00e9s a RedundentExprEliminator h\u00edv\u00e1sban: {0} "},

    { ER_NOT_ALLOWED_IN_POSITION,
     "{0} nem enged\u00e9lyezett a st\u00edluslap ezen hely\u00e9n!"},

    { ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION,
     "Nem-szepar\u00e1tor sz\u00f6veg nem megengedett a st\u00edluslap ezen hely\u00e9n!"},

  // This code is shared with warning codes.
  // SystemId Unknown
    { INVALID_TCHAR,
     "Tiltott \u00e9rt\u00e9ket haszn\u00e1lt a(z) {0} attrib\u00fatumhoz: {1}.  A CHAR t\u00edpus\u00fa attrib\u00fatum csak 1 karakter lehet!"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "QNAME" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value and {0} is the attribute name.
    //The following codes are shared with the warning codes...
    { INVALID_QNAME,
     "Tiltott \u00e9rt\u00e9ket haszn\u00e1lt a(z) {0} CHAR attrib\u00fatumhoz: {1}."},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "ENUM" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value, {0} is the attribute name, and {2} is a list of valid
    // values.
    { INVALID_ENUM,
     "Tiltott \u00e9rt\u00e9ket haszn\u00e1lt a(z) {0} ENUM attrib\u00fatumhoz: {1}.  Az \u00e9rv\u00e9nyes \u00e9rt\u00e9kek: {2}."},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NMTOKEN" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NMTOKEN,
     "Tiltott \u00e9rt\u00e9ket haszn\u00e1lt a(z) {0} NMTOKEN attrib\u00fatumhoz: {1}. "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NCNAME" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NCNAME,
     "Tiltott \u00e9rt\u00e9ket haszn\u00e1lt a(z) {0} NCNAME attrib\u00fatumhoz: {1}. "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "boolean" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_BOOLEAN,
     "Tiltott \u00e9rt\u00e9ket haszn\u00e1lt a(z) {0} logikai attrib\u00fatumhoz: {1}. "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "number" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
     { INVALID_NUMBER,
     "Tiltott \u00e9rt\u00e9ket haszn\u00e1lt a(z) {0} sz\u00e1m attrib\u00fatumhoz: {1}. "},


  // End of shared codes...

// Note to translators:  A "match pattern" is a special form of XPath expression
// that is used for matching patterns.  The substitution text is the name of
// a function.  The message indicates that when this function is referenced in
// a match pattern, its argument must be a string literal (or constant.)
// ER_ARG_LITERAL - new error message for bugzilla //5202
    { ER_ARG_LITERAL,
     "A(z) {0} argumentuma az illeszked\u00e9si mint\u00e1ban egy liter\u00e1l kell legyen."},

// Note to translators:  The following message indicates that two definitions of
// a variable.  A "global variable" is a variable that is accessible everywher
// in the stylesheet.
// ER_DUPLICATE_GLOBAL_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_GLOBAL_VAR,
     "K\u00e9tszer szerepel a glob\u00e1lis v\u00e1ltoz\u00f3-deklar\u00e1ci\u00f3."},


// Note to translators:  The following message indicates that two definitions of
// a variable were encountered.
// ER_DUPLICATE_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_VAR,
     "K\u00e9tszer szerepel a v\u00e1ltoz\u00f3-deklar\u00e1ci\u00f3."},

    // Note to translators:  "xsl:template, "name" and "match" are XSLT keywords
    // which must not be translated.
    // ER_TEMPLATE_NAME_MATCH - new error message for bugzilla #789
    { ER_TEMPLATE_NAME_MATCH,
     "Az xsl:template-nek kell legyen neve vagy illeszked\u00e9si attrib\u00fatuma (vagy mindkett\u0151)"},

    // Note to translators:  "exclude-result-prefixes" is an XSLT keyword which
    // should not be translated.  The message indicates that a namespace prefix
    // encountered as part of the value of the exclude-result-prefixes attribute
    // was in error.
    // ER_INVALID_PREFIX - new error message for bugzilla #788
    { ER_INVALID_PREFIX,
     "Az el\u0151tag az exclude-result-prefixes-ben nem \u00e9rv\u00e9nyes: {0}"},

    // Note to translators:  An "attribute set" is a set of attributes that can
    // be added to an element in the output document as a group.  The message
    // indicates that there was a reference to an attribute set named {0} that
    // was never defined.
    // ER_NO_ATTRIB_SET - new error message for bugzilla #782
    { ER_NO_ATTRIB_SET,
     "A(z) {0} nev\u0171 attribute-set nem l\u00e9tezik"},

    // Note to translators:  This message indicates that there was a reference
    // to a function named {0} for which no function definition could be found.
    { ER_FUNCTION_NOT_FOUND,
     "A(z) {0} nev\u0171 funkci\u00f3 nem l\u00e9tezik"},

    // Note to translators:  This message indicates that the XSLT instruction
    // that is named by the substitution text {0} must not contain other XSLT
    // instructions (content) or a "select" attribute.  The word "select" is
    // an XSLT keyword in this case and must not be translated.
    { ER_CANT_HAVE_CONTENT_AND_SELECT,
     "A(z) {0} elemnek nem lehet egyszerre content \u00e9s select attrib\u00fatuma."},

    // Note to translators:  This message indicates that the value argument
    // of setParameter must be a valid Java Object.
    { ER_INVALID_SET_PARAM_VALUE,
     "A(z) {0} param\u00e9ter \u00e9rt\u00e9ke egy \u00e9rv\u00e9nyes J\u00e1va objektum kell legyen"},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT,
         "Az xsl:namespace-alias elem result-prefix r\u00e9sz\u00e9nek \u00e9rt\u00e9ke '#default', de nincs meghat\u00e1rozva alap\u00e9rtelmezett n\u00e9vt\u00e9r az elem hat\u00f3k\u00f6r\u00e9ben. "},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX,
         "Egy xsl:namespace-alias elem result-prefix attrib\u00fatum\u00e1nak \u00e9rt\u00e9ke ''{0}'', de nincs n\u00e9vt\u00e9r deklar\u00e1ci\u00f3 a(z) ''{0}'' el\u0151taghoz az elem hat\u00f3k\u00f6r\u00e9ben. "},

    { ER_SET_FEATURE_NULL_NAME,
      "A szolg\u00e1ltat\u00e1s neve nem lehet null a TransformerFactory.setFeature(String name, boolean value) met\u00f3dusban."},

    { ER_GET_FEATURE_NULL_NAME,
      "A szolg\u00e1ltat\u00e1s neve nem lehet null a TransformerFactory.getFeature(String name) met\u00f3dusban."},

    { ER_UNSUPPORTED_FEATURE,
      "A(z) ''{0}'' szolg\u00e1ltat\u00e1s nem \u00e1ll\u00edthat\u00f3 be ehhez a TransformerFactory oszt\u00e1lyhoz."},

    { ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING,
        "A(z) ''{0}'' kiterjeszt\u00e9si elem haszn\u00e1lata nem megengedett, ha biztons\u00e1gos feldolgoz\u00e1s be van kapcsolva. "},

        { ER_NAMESPACE_CONTEXT_NULL_NAMESPACE,
          "Nem lehet beolvasni az el\u0151tagot null n\u00e9vt\u00e9r URI eset\u00e9n. "},

        { ER_NAMESPACE_CONTEXT_NULL_PREFIX,
          "Nem olvashat\u00f3 be a n\u00e9vt\u00e9r null el\u0151tag miatt. "},

        { ER_XPATH_RESOLVER_NULL_QNAME,
          "A f\u00fcggv\u00e9ny neve nem lehet null."},

        { ER_XPATH_RESOLVER_NEGATIVE_ARITY,
          "Az arit\u00e1s nem lehet negat\u00edv."},

  // Warnings...

    { WG_FOUND_CURLYBRACE,
      "'}'-t tal\u00e1ltunk, de nincs attrib\u00fatumsablon megnyitva!"},

    { WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR,
      "Figyelmeztet\u00e9s: A count attrib\u00fatum nem felel meg a egy felmen\u0151nek az xsl:number-ben! C\u00e9l = {0}"},

    { WG_EXPR_ATTRIB_CHANGED_TO_SELECT,
      "R\u00e9gi szintaktika: Az 'expr' attrib\u00fatum neve 'select'-re v\u00e1ltozott."},

    { WG_NO_LOCALE_IN_FORMATNUMBER,
      "Az Xalan m\u00e9g nem kezeli a locale nevet a format-number f\u00fcggv\u00e9nyben."},

    { WG_LOCALE_NOT_FOUND,
      "Figyelmeztet\u00e9s: Nem tal\u00e1lhat\u00f3 az xml:lang={0} \u00e9rt\u00e9khez tartoz\u00f3 locale"},

    { WG_CANNOT_MAKE_URL_FROM,
      "Nem k\u00e9sz\u00edthet\u0151 URL ebb\u0151l: {0}"},

    { WG_CANNOT_LOAD_REQUESTED_DOC,
      "A k\u00e9r dokumentum nem t\u00f6lthet\u0151 be: {0}"},

    { WG_CANNOT_FIND_COLLATOR,
      "Nem tal\u00e1lhat\u00f3 Collator a <sort xml:lang={0}-hez"},

    { WG_FUNCTIONS_SHOULD_USE_URL,
      "R\u00e9gi szintaktika: a functions utas\u00edt\u00e1s {0} URL-t kell haszn\u00e1ljon"},

    { WG_ENCODING_NOT_SUPPORTED_USING_UTF8,
      "a k\u00f3dol\u00e1s nem t\u00e1mogatott: {0}, UTF-8-at haszn\u00e1lunk"},

    { WG_ENCODING_NOT_SUPPORTED_USING_JAVA,
      "a k\u00f3dol\u00e1s nem t\u00e1mogatott: {0}, Java {1}-t haszn\u00e1lunk"},

    { WG_SPECIFICITY_CONFLICTS,
      "Specifikuss\u00e1gi konfliktust tal\u00e1ltunk: {0} A st\u00edluslapon legutolj\u00e1ra megtal\u00e1ltat haszn\u00e1ljuk."},

    { WG_PARSING_AND_PREPARING,
      "========= {0} elemz\u00e9se \u00e9s el\u0151k\u00e9sz\u00edt\u00e9se =========="},

    { WG_ATTR_TEMPLATE,
     "Attr sablon, {0}"},

    { WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE,
      "Illeszt\u00e9si konfliktus az xsl:strip-space \u00e9s az xsl:preserve-space k\u00f6z\u00f6tt"},

    { WG_ATTRIB_NOT_HANDLED,
      "A Xalan m\u00e9g nem kezeli a(z) {0} attrib\u00fatumot!"},

    { WG_NO_DECIMALFORMAT_DECLARATION,
      "Nem tal\u00e1ltuk meg a deklar\u00e1ci\u00f3t a decim\u00e1lis form\u00e1tumhoz: {0}"},

    { WG_OLD_XSLT_NS,
     "Hi\u00e1nyz\u00f3 vagy helytelen XSLT n\u00e9vt\u00e9r. "},

    { WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED,
      "Csak az alap\u00e9rtelmezett xsl:decimal-format deklar\u00e1ci\u00f3 megengedett."},

    { WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE,
      "Az xsl:decimal-format neveknek egyedieknek kell lenni\u00fck. A(z) \"{0}\" n\u00e9v meg lett ism\u00e9telve."},

    { WG_ILLEGAL_ATTRIBUTE,
      "A(z) {0}-nak \u00e9rv\u00e9nytelen attrib\u00fatuma van: {1}"},

    { WG_COULD_NOT_RESOLVE_PREFIX,
      "Nem lehet feloldani a n\u00e9vt\u00e9r el\u0151tagot: {0}. A csom\u00f3pont figyelmen k\u00edv\u00fcl marad."},

    { WG_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "Az xsl:stylesheet-nek kell legyen 'version' attrib\u00fatuma!"},

    { WG_ILLEGAL_ATTRIBUTE_NAME,
      "Nem megengedett attrib\u00fatumn\u00e9v: {0}"},

    { WG_ILLEGAL_ATTRIBUTE_VALUE,
      "Tiltott \u00e9rt\u00e9ket haszn\u00e1lt a(z) {0} attrib\u00fatumhoz: {1}"},

    { WG_EMPTY_SECOND_ARG,
      "A document f\u00fcggv\u00e9ny m\u00e1sodik argumentum\u00e1b\u00f3l el\u0151\u00e1ll\u00f3 csom\u00f3ponthalmaz \u00fcres. \u00dcres node-k\u00e9szletetet adok vissza."},

  //Following are the new WARNING keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.
    { WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "A(z) xsl:processing-instruction  n\u00e9v 'name' attrib\u00fatuma nem lehet 'xml'"},

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.  "NCName" is an XML data-type and must not be
    // translated.
    { WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "A(z) xsl:processing-instruction  n\u00e9v ''name'' attrib\u00fatuma \u00e9rv\u00e9nyes NCName kell legyen: {0}"},

    // Note to translators:  This message is reported if the stylesheet that is
    // being processed attempted to construct an XML document with an attribute in a
    // place other than on an element.  The substitution text specifies the name of
    // the attribute.
    { WG_ILLEGAL_ATTRIBUTE_POSITION,
      "Nem lehet {0} attrib\u00fatumat felvenni a gyermek node-ok ut\u00e1n vagy miel\u0151tt egy elem l\u00e9trej\u00f6nne.  Az attrib\u00fatum figyelmen k\u00edv\u00fcl marad."},

    { NO_MODIFICATION_ALLOWED_ERR,
      "K\u00eds\u00e9rlet t\u00f6rt\u00e9nt egy objektum m\u00f3dos\u00edt\u00e1s\u00e1ra, ahol a m\u00f3dos\u00edt\u00e1sok nem megengedettek. "
    },

    //Check: WHY THERE IS A GAP B/W NUMBERS in the XSLTErrorResources properties file?

  // Other miscellaneous text used inside the code...
  { "ui_language", "hu"},
  {  "help_language",  "hu" },
  {  "language",  "hu" },
  { "BAD_CODE", "A createMessage param\u00e9tere nincs a megfelel\u0151 tartom\u00e1nyban"},
  {  "FORMAT_FAILED", "Kiv\u00e9tel t\u00f6rt\u00e9nt a messageFormat h\u00edv\u00e1s alatt"},
  {  "version", ">>>>>>> Xalan verzi\u00f3 "},
  {  "version2",  "<<<<<<<"},
  {  "yes", "igen"},
  { "line", "Sor #"},
  { "column","Oszlop #"},
  { "xsldone", "XSLProcessor: k\u00e9sz"},


  // Note to translators:  The following messages provide usage information
  // for the Xalan Process command line.  "Process" is the name of a Java class,
  // and should not be translated.
  { "xslProc_option", "Xalan-J parancssori Process oszt\u00e1ly opci\u00f3k:"},
  { "xslProc_option", "Xalan-J parancssori Process oszt\u00e1ly opci\u00f3k\u003a"},
  { "xslProc_invalid_xsltc_option", "A(z) {0} opci\u00f3 nem t\u00e1mogatott XSLTC m\u00f3dban."},
  { "xslProc_invalid_xalan_option", "A(z) {0} opci\u00f3 csak -XSLTC-vel egy\u00fctt haszn\u00e1lhat\u00f3."},
  { "xslProc_no_input", "Hiba: Nem adott meg st\u00edluslapot vagy bemeneti xml-t. Futtassa ezt a parancsot kapcsol\u00f3k n\u00e9lk\u00fcl a haszn\u00e1lati utas\u00edt\u00e1sok megjelen\u00edt\u00e9s\u00e9re."},
  { "xslProc_common_options", "-\u00c1ltal\u00e1nos opci\u00f3k-"},
  { "xslProc_xalan_options", "-Xalan opci\u00f3k-"},
  { "xslProc_xsltc_options", "-XSLTC opci\u00f3k-"},
  { "xslProc_return_to_continue", "(nyomja la a <return> gombot a folytat\u00e1shoz)"},

   // Note to translators: The option name and the parameter name do not need to
   // be translated. Only translate the messages in parentheses.  Note also that
   // leading whitespace in the messages is used to indent the usage information
   // for each option in the English messages.
   // Do not translate the keywords: XSLTC, SAX, DOM and DTM.
  { "optionXSLTC", "   [-XSLTC (XSLTC-t haszn\u00e1l a transzform\u00e1l\u00e1shoz)]"},
  { "optionIN", "   [-IN bemenetiXMLURL]"},
  { "optionXSL", "   [-XSL XSLTranszform\u00e1ci\u00f3sURL]"},
  { "optionOUT",  "   [-OUT kimenetiF\u00e1jln\u00e9v]"},
  { "optionLXCIN", "   [-LXCIN leford\u00edtottst\u00edluslapF\u00e1jln\u00e9vBe]"},
  { "optionLXCOUT", "   [-LXCOUT leford\u00edtottSt\u00edluslapF\u00e1jln\u00e9vKi]"},
  { "optionPARSER", "   [-PARSER az \u00e9rtelmez\u0151kapcsolat teljesen meghat\u00e1rozott oszt\u00e1lyneve]"},
  {  "optionE", "   [-E (Nem bontja ki az entit\u00e1s hivatkoz\u00e1sokat)]"},
  {  "optionV",  "   [-E (Nem bontja ki az entit\u00e1s hivatkoz\u00e1sokat)]"},
  {  "optionQC", "   [-QC (Csendes mintakonfliktus figyelmeztet\u00e9sek)]"},
  {  "optionQ", "   [-Q  (Csendes m\u00f3d)]"},
  {  "optionLF", "   [-LF (A soremel\u00e9seket csak kimenet eset\u00e9n haszn\u00e1lja {alap\u00e9rtelmez\u00e9s: CR/LF})]"},
  {  "optionCR", "   [-CR (A kocsivissza karaktert csak kimenet eset\u00e9n haszn\u00e1lja {alap\u00e9rtelmez\u00e9s: CR/LF})]"},
  { "optionESCAPE", "   [-ESCAPE (Mely karaktereket kell escape-elni {alap\u00e9rtelmez\u00e9s: <>&\"\'\\r\\n}]"},
  { "optionINDENT", "   [-INDENT (Meghat\u00e1rozza, hogy h\u00e1ny sz\u00f3k\u00f6zzel kell beljebb kezdeni {alap\u00e9rtelmez\u00e9s: 0})]"},
  { "optionTT", "   [-TT (Nyomk\u00f6veti a sablonokat, ahogy azokat megh\u00edvj\u00e1k.)]"},
  { "optionTG", "   [-TG (Nyomk\u00f6veti az \u00f6sszes gener\u00e1l\u00e1si esem\u00e9nyt.)]"},
  { "optionTS", "   [-TS (Nyomk\u00f6veti az \u00f6sszes kiv\u00e1laszt\u00e1si esem\u00e9nyt.)]"},
  {  "optionTTC", "   [-TTC (Nyomk\u00f6veti a sablon-lesz\u00e1rmazottakat, ahogy azokat feldolgozz\u00e1k.)]"},
  { "optionTCLASS", "   [-TCLASS (TraceListener oszt\u00e1ly a nyomk\u00f6vet\u00e9si kiterjeszt\u00e9sekhez.)]"},
  { "optionVALIDATE", "   [-VALIDATE (Be\u00e1ll\u00edtja, hogy legyen-e \u00e9rv\u00e9nyess\u00e9gvizsg\u00e1lat.  Alap\u00e9rtelmez\u00e9sben nincs \u00e9rv\u00e9nyess\u00e9gvizsg\u00e1lat.)]"},
  { "optionEDUMP", "   [-EDUMP {opcion\u00e1lis f\u00e1jln\u00e9v} (Hib\u00e1n\u00e1l stackdump-ot hajt v\u00e9gre.)]"},
  {  "optionXML", "   [-XML (XML form\u00e1z\u00f3 haszn\u00e1lata \u00e9s XML fejl\u00e9c hozz\u00e1ad\u00e1sa.)]"},
  {  "optionTEXT", "   [-TEXT (Egyszer\u0171 sz\u00f6vegform\u00e1z\u00f3 haszn\u00e1lata.)]"},
  {  "optionHTML", "   [-HTML (HTML form\u00e1z\u00f3 haszn\u00e1lata.)]"},
  {  "optionPARAM", "   [-PARAM n\u00e9v kifejez\u00e9s (Be\u00e1ll\u00edt egy st\u00edluslap param\u00e9tert)]"},
  {  "noParsermsg1", "Az XSL folyamat sikertelen volt."},
  {  "noParsermsg2", "** Az \u00e9rtelmez\u0151 nem tal\u00e1lhat\u00f3 **"},
  { "noParsermsg3",  "K\u00e9rem, ellen\u0151rizze az oszt\u00e1ly el\u00e9r\u00e9si utat."},
  { "noParsermsg4", "Ha \u00f6nnek nincs meg az IBM Java XML \u00e9rtelmez\u0151je, akkor let\u00f6ltheti az"},
  { "noParsermsg5", "az IBM AlphaWorks weblapr\u00f3l: http://www.alphaworks.ibm.com/formula/xml"},
  { "optionURIRESOLVER", "   [-URIRESOLVER teljes oszt\u00e1lyn\u00e9v (az URIResolver fogja feloldani az URI-kat)]"},
  { "optionENTITYRESOLVER",  "   [-ENTITYRESOLVER teljes oszt\u00e1lyn\u00e9v (az EntityResolver fogja feloldani az entit\u00e1sokat)]"},
  { "optionCONTENTHANDLER",  "   [-CONTENTHANDLER teljes oszt\u00e1lyn\u00e9v (a ContentHandler fogja soros\u00edtani a kimenetet)]"},
  {  "optionLINENUMBERS",  "   [-L sorsz\u00e1mokat haszn\u00e1l a forr\u00e1sdokumentumhoz]"},
  { "optionSECUREPROCESSING", "   [-SECURE (biztons\u00e1gos feldolgoz\u00e1s szolg\u00e1ltat\u00e1s igazra \u00e1ll\u00edt\u00e1sa.)]"},

    // Following are the new options added in XSLTErrorResources.properties files after Jdk 1.4 (Xalan 2.2-D11)


  {  "optionMEDIA",  "   [-MEDIA adathordoz\u00f3T\u00edpus (a media attrib\u00fatum seg\u00edts\u00e9g\u00e9vel megkeresi a dokumentumhoz tartoz\u00f3 st\u00edluslapot.)]"},
  {  "optionFLAVOR",  "   [-FLAVOR \u00edzl\u00e9sN\u00e9v (Explicit haszn\u00e1lja az s2s=SAX-ot vagy d2d=DOM-ot a transzform\u00e1ci\u00f3hoz.)] "}, // Added by sboag/scurcuru; experimental
  { "optionDIAG", "   [-DIAG (Ki\u00edrja, hogy \u00f6sszesen h\u00e1ny ezredm\u00e1sodpercig tartott a transzform\u00e1ci\u00f3.)]"},
  { "optionINCREMENTAL",  "   [-INCREMENTAL (n\u00f6vekm\u00e9nyes DTM l\u00e9trehoz\u00e1st ig\u00e9nyel a http://xml.apache.org/xalan/features/incremental igazra \u00e1ll\u00edt\u00e1s\u00e1val.)]"},
  {  "optionNOOPTIMIMIZE",  "   [-NOOPTIMIMIZE (nem ig\u00e9nyel st\u00edluslap optimiz\u00e1l\u00e1st a http://xml.apache.org/xalan/features/optimize hamisra \u00e1ll\u00edt\u00e1s\u00e1t.)]"},
  { "optionRL",  "   [-RL rekurzi\u00f3korl\u00e1t (numerikusan korl\u00e1tozza a st\u00edluslap rekurzi\u00f3 m\u00e9lys\u00e9g\u00e9t.)]"},
  {   "optionXO",  "   [-XO [transletNeve] (a nevet rendeli a gener\u00e1lt translethez)]"},
  {  "optionXD", "   [-XD c\u00e9lAlk\u00f6nyvt\u00e1r (a translet c\u00e9l-alk\u00f6nyvt\u00e1ra)]"},
  {  "optionXJ",  "   [-XJ jarf\u00e1jl (a translet oszt\u00e1lyokat a megadott <jarf\u00e1jl>-ba csomagolja)]"},
  {   "optionXP",  "   [-XP csomag (megadja a gener\u00e1lt translet oszt\u00e1lyok n\u00e9v-prefix\u00e9t)]"},

  //AddITIONAL  STRINGS that need L10n
  // Note to translators:  The following message describes usage of a particular
  // command-line option that is used to enable the "template inlining"
  // optimization.  The optimization involves making a copy of the code
  // generated for a template in another template that refers to it.
  { "optionXN",  "   [-XN (enged\u00e9lyezi a template inlining optimaliz\u00e1l\u00e1st)]" },
  { "optionXX",  "   [-XX (bekapcsolja a tov\u00e1bbi hibakeres\u00e9si kimenetet)]"},
  { "optionXT" , "   [-XT (translet-et haszn\u00e1lt az \u00e1talak\u00edt\u00e1shoz, ha lehet)]"},
  { "diagTiming"," --------- A(z) {0} tarnszform\u00e1ci\u00f3a a(z) {1}-el {2} ms-ig tartott" },
  { "recursionTooDeep","A sablonon egym\u00e1sba \u00e1gyaz\u00e1sa t\u00fal m\u00e9ly. Be\u00e1gyaz\u00e1s = {0}, sablon: {1} {2}" },
  { "nameIs", "A n\u00e9v:" },
  { "matchPatternIs", "Az illeszked\u00e9si minta:" }

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
  public static final String ERROR_HEADER = "Hiba: ";

  /** String to prepend to warning messages.    */
  public static final String WARNING_HEADER = "Figyelmeztet\u00e9s: ";

  /** String to specify the XSLT module.  */
  public static final String XSL_HEADER = "XSLT ";

  /** String to specify the XML parser module.  */
  public static final String XML_HEADER = "XML ";

  /** I don't think this is used any more.
   * @deprecated  */
  public static final String QUERY_HEADER = "MINTA ";


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
