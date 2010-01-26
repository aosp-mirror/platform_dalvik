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
 * $Id: XSLTErrorResources_it.java 468641 2006-10-28 06:54:42Z minchau $
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
public class XSLTErrorResources_it extends ListResourceBundle
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
      "Errore: '{' non pu\u00f2 essere contenuto in un'espressione"},

    { ER_ILLEGAL_ATTRIBUTE ,
     "{0} ha un attributo non valido: {1}"},

  {ER_NULL_SOURCENODE_APPLYIMPORTS ,
      "sourceNode nullo in xsl:apply-imports!"},

  {ER_CANNOT_ADD,
      "Impossibile aggiungere {0} a {1}"},

    { ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES,
      "sourceNode nullo in handleApplyTemplatesInstruction."},

    { ER_NO_NAME_ATTRIB,
     "{0} deve avere un attributo name."},

    {ER_TEMPLATE_NOT_FOUND,
     "Impossibile trovare la maschera: {0}"},

    {ER_CANT_RESOLVE_NAME_AVT,
      "Impossibile risolvere il nome AVT in xsl:call-template."},

    {ER_REQUIRES_ATTRIB,
     "{0} richiede l''''attributo: {1}"},

    { ER_MUST_HAVE_TEST_ATTRIB,
      "{0} deve avere un attributo ''test''."},

    {ER_BAD_VAL_ON_LEVEL_ATTRIB,
      "Valore errato nell''''attributo livello: {0}"},

    {ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "Il nome dell'istruzione di elaborazione non pu\u00f2 essere 'xml'"},

    { ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "il nome dell''''istruzione di elaborazione deve essere un NCName valido: {0}"},

    { ER_NEED_MATCH_ATTRIB,
      "{0} deve avere un attributo match nel caso abbia un modo."},

    { ER_NEED_NAME_OR_MATCH_ATTRIB,
      "{0} richiede un attributo match o name."},

    {ER_CANT_RESOLVE_NSPREFIX,
      "Impossibile risolvere il prefisso dello namespace: {0}"},

    { ER_ILLEGAL_VALUE,
     "xml:space ha un valore non valido: {0}"},

    { ER_NO_OWNERDOC,
      "Il nodo secondario non ha un documento proprietario."},

    { ER_ELEMTEMPLATEELEM_ERR,
     "Errore ElemTemplateElement: {0}"},

    { ER_NULL_CHILD,
     "\u00c8 stato effettuato un tentativo di aggiungere un secondario nullo."},

    { ER_NEED_SELECT_ATTRIB,
     "{0} richiede un attributo select."},

    { ER_NEED_TEST_ATTRIB ,
      "xsl:when deve avere un attributo 'test'."},

    { ER_NEED_NAME_ATTRIB,
      "xsl:with-param deve avere un attributo 'name'."},

    { ER_NO_CONTEXT_OWNERDOC,
      "il contesto non ha un documento proprietario."},

    {ER_COULD_NOT_CREATE_XML_PROC_LIAISON,
      "Impossibile creare XML TransformerFactory Liaison: {0}"},

    {ER_PROCESS_NOT_SUCCESSFUL,
      "Xalan: Processo non eseguito correttamente."},

    { ER_NOT_SUCCESSFUL,
     "Xalan: non eseguito correttamente."},

    { ER_ENCODING_NOT_SUPPORTED,
     "Codifica non supportata: {0}"},

    {ER_COULD_NOT_CREATE_TRACELISTENER,
      "Impossibile creare TraceListener: {0}"},

    {ER_KEY_REQUIRES_NAME_ATTRIB,
      "xsl:key richiede un attributo 'name'."},

    { ER_KEY_REQUIRES_MATCH_ATTRIB,
      "xsl:key richiede un attributo 'match'."},

    { ER_KEY_REQUIRES_USE_ATTRIB,
      "xsl:key richiede un attributo 'use'."},

    { ER_REQUIRES_ELEMENTS_ATTRIB,
      "(StylesheetHandler) {0} richiede un attributo ''elements''."},

    { ER_MISSING_PREFIX_ATTRIB,
      "(StylesheetHandler) {0} attributo ''prefix'' mancante"},

    { ER_BAD_STYLESHEET_URL,
     "URL del foglio di lavoro errato: {0}"},

    { ER_FILE_NOT_FOUND,
     "File del foglio di lavoro non trovato: {0}"},

    { ER_IOEXCEPTION,
      "Eccezione IO nel file del foglio di lavoro: {0}"},

    { ER_NO_HREF_ATTRIB,
      "(StylesheetHandler) Impossibile trovare l''''attributo href per {0}"},

    { ER_STYLESHEET_INCLUDES_ITSELF,
      "(StylesheetHandler) {0} sta direttamente o indirettamente includendo se stesso."},

    { ER_PROCESSINCLUDE_ERROR,
      "Errore StylesheetHandler.processInclude, {0}"},

    { ER_MISSING_LANG_ATTRIB,
      "(StylesheetHandler) {0} attributo ''lang'' mancante"},

    { ER_MISSING_CONTAINER_ELEMENT_COMPONENT,
      "(StylesheetHandler) elemento {0} non ubicato correttamente. Elemento contenitore ''component'' mancante "},

    { ER_CAN_ONLY_OUTPUT_TO_ELEMENT,
      "L'emissione \u00e8 consentita solo in un elemento, frammento di documento, documento o stampante."},

    { ER_PROCESS_ERROR,
     "Errore StylesheetRoot.process"},

    { ER_UNIMPLNODE_ERROR,
     "Errore UnImplNode: {0}"},

    { ER_NO_SELECT_EXPRESSION,
      "Errore! Impossibile trovare espressione selezione xpath (-select)."},

    { ER_CANNOT_SERIALIZE_XSLPROCESSOR,
      "Impossibile serializzare XSLProcessor!"},

    { ER_NO_INPUT_STYLESHEET,
      "Input del foglio di lavoro non specificato."},

    { ER_FAILED_PROCESS_STYLESHEET,
      "Impossibile elaborare il foglio di lavoro."},

    { ER_COULDNT_PARSE_DOC,
     "Impossibile analizzare il documento {0}."},

    { ER_COULDNT_FIND_FRAGMENT,
     "Impossibile trovare il frammento: {0}"},

    { ER_NODE_NOT_ELEMENT,
      "Il nodo a cui fa riferimento l''''identificativo del frammento non \u00e8 un elemento: {0}"},

    { ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB,
      "for-each deve avere un attributo match o name"},

    { ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB,
      "le maschere devono avere un attributo match o name"},

    { ER_NO_CLONE_OF_DOCUMENT_FRAG,
      "Non \u00e8 possibile avere un clone di un frammento di documento."},

    { ER_CANT_CREATE_ITEM,
      "Impossibile creare la voce nella struttura dei risultati: {0}"},

    { ER_XMLSPACE_ILLEGAL_VALUE,
      "xml:space in XML di origine ha un valore non valido: {0}"},

    { ER_NO_XSLKEY_DECLARATION,
      "Nessuna dichiarazione xsl:key per {0}!"},

    { ER_CANT_CREATE_URL,
     "Errore! Impossibile creare url per: {0}"},

    { ER_XSLFUNCTIONS_UNSUPPORTED,
     "xsl:functions non supportato"},

    { ER_PROCESSOR_ERROR,
     "Errore XSLT TransformerFactory"},

    { ER_NOT_ALLOWED_INSIDE_STYLESHEET,
      "(StylesheetHandler) {0} non consentito nel foglio di lavoro."},

    { ER_RESULTNS_NOT_SUPPORTED,
      "result-ns non \u00e8 pi\u00f9 supportato.  Utilizzare xsl:output."},

    { ER_DEFAULTSPACE_NOT_SUPPORTED,
      "default-space non \u00e8 pi\u00f9 supportato.  Utilizzare xsl:strip-space oppure xsl:preserve-space."},

    { ER_INDENTRESULT_NOT_SUPPORTED,
      "indent-result non \u00e8 pi\u00f9 supportato.  Utilizzare xsl:output."},

    { ER_ILLEGAL_ATTRIB,
      "(StylesheetHandler) {0} ha un attributo non valido: {1}"},

    { ER_UNKNOWN_XSL_ELEM,
     "Elemento XSL sconosciuto: {0}"},

    { ER_BAD_XSLSORT_USE,
      "(StylesheetHandler) xsl:sort pu\u00f2 essere utilizzato solo con xsl:apply-templates oppure xsl:for-each."},

    { ER_MISPLACED_XSLWHEN,
      "(StylesheetHandler) xsl:when posizionato in modo non corretto."},

    { ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:when non reso principale da xsl:choose!"},

    { ER_MISPLACED_XSLOTHERWISE,
      "(StylesheetHandler) xsl:otherwise posizionato in modo non corretto."},

    { ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:otherwise non reso principale da xsl:choose!"},

    { ER_NOT_ALLOWED_INSIDE_TEMPLATE,
      "(StylesheetHandler) {0} non \u00e8 consentito in una maschera."},

    { ER_UNKNOWN_EXT_NS_PREFIX,
      "(StylesheetHandler) {0} prefisso namespace estensione {1} sconosciuto"},

    { ER_IMPORTS_AS_FIRST_ELEM,
      "(StylesheetHandler) Le importazioni possono verificarsi solo come primi elementi nel foglio di lavoro."},

    { ER_IMPORTING_ITSELF,
      "(StylesheetHandler) {0} sta direttamente o indirettamente importando se stesso."},

    { ER_XMLSPACE_ILLEGAL_VAL,
      "(StylesheetHandler) xml:space ha un valore non valido: {0}"},

    { ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL,
      "processStylesheet con esito negativo."},

    { ER_SAX_EXCEPTION,
     "Eccezione SAX"},

//  add this message to fix bug 21478
    { ER_FUNCTION_NOT_SUPPORTED,
     "Funzione non supportata."},


    { ER_XSLT_ERROR,
     "Errore XSLT"},

    { ER_CURRENCY_SIGN_ILLEGAL,
      "il simbolo della valuta non \u00e8 consentito nella stringa modello formato."},

    { ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM,
      "La funzione documento non \u00e8 supportata nel DOM del foglio di lavoro."},

    { ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER,
      "Impossibile risolvere il prefisso di un resolver non di prefisso."},

    { ER_REDIRECT_COULDNT_GET_FILENAME,
      "Redirect extension: Impossibile richiamare il nome file - l'attributo file o select deve restituire una stringa valida."},

    { ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT,
      "Impossibile creare FormatterListener in Redirect extension!"},

    { ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX,
      "Prefisso in exclude-result-prefixes non valido: {0}"},

    { ER_MISSING_NS_URI,
      "URI spazio nome mancante per il prefisso specificato"},

    { ER_MISSING_ARG_FOR_OPTION,
      "Argomento mancante per l''''opzione: {0}"},

    { ER_INVALID_OPTION,
     "Opzione non valida: {0}"},

    { ER_MALFORMED_FORMAT_STRING,
     "Stringa di formato errato: {0}"},

    { ER_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet richiede un attributo 'version'."},

    { ER_ILLEGAL_ATTRIBUTE_VALUE,
      "L''attributo: {0} ha un valore non valido: {1}"},

    { ER_CHOOSE_REQUIRES_WHEN,
     "xsl:choose richiede xsl:when"},

    { ER_NO_APPLY_IMPORT_IN_FOR_EACH,
      "xsl:apply-imports non consentito in xsl:for-each"},

    { ER_CANT_USE_DTM_FOR_OUTPUT,
      "Impossibile utilizzare DTMLiaison per un nodo DOM di output... utilizzare invece org.apache.xpath.DOM2Helper."},

    { ER_CANT_USE_DTM_FOR_INPUT,
      "Impossibile utilizzare DTMLiaison per un nodo DON di input... utilizzare invece org.apache.xpath.DOM2Helper."},

    { ER_CALL_TO_EXT_FAILED,
      "Chiamata all''''elemento estensione non riuscita: {0}"},

    { ER_PREFIX_MUST_RESOLVE,
      "Il prefisso deve risolvere in uno namespace: {0}"},

    { ER_INVALID_UTF16_SURROGATE,
      "Rilevato surrogato UTF-16 non valido: {0} ?"},

    { ER_XSLATTRSET_USED_ITSELF,
      "xsl:attribute-set {0} sta utilizzando se stesso, determinando un loop infinito."},

    { ER_CANNOT_MIX_XERCESDOM,
      "Impossibile unire input non Xerces-DOM con output Xerces-DOM."},

    { ER_TOO_MANY_LISTENERS,
      "addTraceListenersToStylesheet - TooManyListenersException"},

    { ER_IN_ELEMTEMPLATEELEM_READOBJECT,
      "In ElemTemplateElement.readObject: {0}"},

    { ER_DUPLICATE_NAMED_TEMPLATE,
      "Sono state rilevate pi\u00f9 maschere denominate: {0}"},

    { ER_INVALID_KEY_CALL,
      "Chiamata funzione non valida: le chiamate key() ricorsive non sono consentite"},

    { ER_REFERENCING_ITSELF,
      "La variabile {0} sta direttamente o indirettamente facendo riferimento a se stessa."},

    { ER_ILLEGAL_DOMSOURCE_INPUT,
      "Il nodo di input non pu\u00f2 essere nullo per DOMSource per newTemplates."},

    { ER_CLASS_NOT_FOUND_FOR_OPTION,
        "File di classe non trovato per l''opzione {0}"},

    { ER_REQUIRED_ELEM_NOT_FOUND,
        "Elemento richiesto non trovato: {0}"},

    { ER_INPUT_CANNOT_BE_NULL,
        "InputStream non pu\u00f2 essere nullo"},

    { ER_URI_CANNOT_BE_NULL,
        "URI non pu\u00f2 essere nullo"},

    { ER_FILE_CANNOT_BE_NULL,
        "File non pu\u00f2 essere nullo"},

    { ER_SOURCE_CANNOT_BE_NULL,
                "InputSource non pu\u00f2 essere nullo"},

    { ER_CANNOT_INIT_BSFMGR,
                "Impossibile inizializzare BSF Manager"},

    { ER_CANNOT_CMPL_EXTENSN,
                "Impossibile compilare l'estensione"},

    { ER_CANNOT_CREATE_EXTENSN,
      "Impossibile creare l''''estensione: {0} a causa di: {1}"},

    { ER_INSTANCE_MTHD_CALL_REQUIRES,
      "La chiamata metodo istanza al metodo {0} richiede un''istanza Object come primo argomento"},

    { ER_INVALID_ELEMENT_NAME,
      "Specificato nome elemento non valido{0}"},

    { ER_ELEMENT_NAME_METHOD_STATIC,
      "Il metodo nome elemento deve essere statico {0}"},

    { ER_EXTENSION_FUNC_UNKNOWN,
             "Funzione estensione {0} : {1} sconosciuta"},

    { ER_MORE_MATCH_CONSTRUCTOR,
             "\u00c8 stata trovata pi\u00f9 di una corrispondenza migliore per il costruttore per {0}"},

    { ER_MORE_MATCH_METHOD,
             "\u00c8 stata trovata pi\u00f9 di una corrispondenza migliore per il metodo {0}"},

    { ER_MORE_MATCH_ELEMENT,
             "\u00c8 stata trovata pi\u00f9 di una corrispondenza migliore per il metodo elemento {0}"},

    { ER_INVALID_CONTEXT_PASSED,
             "Specificato contesto non valido per valutare {0}"},

    { ER_POOL_EXISTS,
             "Pool gi\u00e0 esistente"},

    { ER_NO_DRIVER_NAME,
             "Non \u00e8 stato specificato alcun Nome driver"},

    { ER_NO_URL,
             "Non \u00e8 stata specificata alcuna URL"},

    { ER_POOL_SIZE_LESSTHAN_ONE,
             "La dimensione del pool \u00e8 inferiore a uno."},

    { ER_INVALID_DRIVER,
             "Specificato nome driver non valido."},

    { ER_NO_STYLESHEETROOT,
             "Impossibile trovare la root del foglio di lavoro."},

    { ER_ILLEGAL_XMLSPACE_VALUE,
         "Valore non valido per xml:space"},

    { ER_PROCESSFROMNODE_FAILED,
         "processFromNode non riuscito"},

    { ER_RESOURCE_COULD_NOT_LOAD,
        "Impossibile caricare la risorsa [ {0} ]: {1} \n {2} \t {3}"},

    { ER_BUFFER_SIZE_LESSTHAN_ZERO,
        "Dimensione buffer <=0"},

    { ER_UNKNOWN_ERROR_CALLING_EXTENSION,
        "Errore sconosciuto durante la chiamata all'estensione"},

    { ER_NO_NAMESPACE_DECL,
        "Il prefisso {0} non ha una dichiarazione namaspace corrispondente"},

    { ER_ELEM_CONTENT_NOT_ALLOWED,
        "Contenuto elemento non consentito per lang=javaclass {0}"},

    { ER_STYLESHEET_DIRECTED_TERMINATION,
        "Il foglio di lavoro ha indirizzato l'interruzione"},

    { ER_ONE_OR_TWO,
        "1 o 2"},

    { ER_TWO_OR_THREE,
        "2 o 3"},

    { ER_COULD_NOT_LOAD_RESOURCE,
        "Impossibile caricare {0} (controllare CLASSPATH), verranno utilizzati i valori predefiniti."},

    { ER_CANNOT_INIT_DEFAULT_TEMPLATES,
        "Impossibile inizializzare le maschere predefinite"},

    { ER_RESULT_NULL,
        "Il risultato non pu\u00f2 essere nullo"},

    { ER_RESULT_COULD_NOT_BE_SET,
        "Impossibile impostare il risultato"},

    { ER_NO_OUTPUT_SPECIFIED,
        "Non \u00e8 stato specificato alcun output"},

    { ER_CANNOT_TRANSFORM_TO_RESULT_TYPE,
        "Impossibile trasformare in un risultato di tipo {0}"},

    { ER_CANNOT_TRANSFORM_SOURCE_TYPE,
        "Impossibile trasformare in un''origine di tipo {0}"},

    { ER_NULL_CONTENT_HANDLER,
        "Handler contenuto nullo"},

    { ER_NULL_ERROR_HANDLER,
        "Handler errori nullo"},

    { ER_CANNOT_CALL_PARSE,
        "non \u00e8 possibile richiamare l'analisi se ContentHandler non \u00e8 stato impostato"},

    { ER_NO_PARENT_FOR_FILTER,
        "Nessun principale per il filtro"},

    { ER_NO_STYLESHEET_IN_MEDIA,
         "Nessun foglio di lavoro trovato in: {0}, supporto= {1}"},

    { ER_NO_STYLESHEET_PI,
         "Nessun PI xml-stylesheet trovato in: {0}"},

    { ER_NOT_SUPPORTED,
       "Non supportato: {0}"},

    { ER_PROPERTY_VALUE_BOOLEAN,
       "Il valore della propriet\u00e0 {0} deve essere una istanza booleana"},

    { ER_COULD_NOT_FIND_EXTERN_SCRIPT,
         "Impossibile richiamare lo script esterno in {0}"},

    { ER_RESOURCE_COULD_NOT_FIND,
        "Risorsa [ {0} ] non trovata.\n {1}"},

    { ER_OUTPUT_PROPERTY_NOT_RECOGNIZED,
        "Propriet\u00e0 Output non riconosciuta: {0}"},

    { ER_FAILED_CREATING_ELEMLITRSLT,
        "Creazione dell'istanza ElemLiteralResult non riuscita"},

  //Earlier (JDK 1.4 XALAN 2.2-D11) at key code '204' the key name was ER_PRIORITY_NOT_PARSABLE
  // In latest Xalan code base key name is  ER_VALUE_SHOULD_BE_NUMBER. This should also be taken care
  //in locale specific files like XSLTErrorResources_de.java, XSLTErrorResources_fr.java etc.
  //NOTE: Not only the key name but message has also been changed.

    { ER_VALUE_SHOULD_BE_NUMBER,
        "Il valore di {0} deve contenere un numero analizzabile"},

    { ER_VALUE_SHOULD_EQUAL,
        "Il valore di {0} deve essere uguale a yes o no"},

    { ER_FAILED_CALLING_METHOD,
        "Chiamata al metodo {0} non riuscita"},

    { ER_FAILED_CREATING_ELEMTMPL,
        "Creazione dell'istanza ElemTemplateElement non riuscita"},

    { ER_CHARS_NOT_ALLOWED,
        "I caratteri non sono consentiti in questo punto del documento"},

    { ER_ATTR_NOT_ALLOWED,
        "L''''attributo \"{0}\" non \u00e8 consentito nell''''elemento {1}."},

    { ER_BAD_VALUE,
     "{0} valore errato {1} "},

    { ER_ATTRIB_VALUE_NOT_FOUND,
     "Valore attributo {0} non trovato "},

    { ER_ATTRIB_VALUE_NOT_RECOGNIZED,
     "Valore attributo {0} non riconosciuto "},

    { ER_NULL_URI_NAMESPACE,
     "\u00c8 stato effettuato un tentativo di generare un prefisso spazio nome con un URI nullo"},

  //New ERROR keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    { ER_NUMBER_TOO_BIG,
     "Si sta effettuando un tentativo di formattare un numero superiore all'intero Long pi\u00f9 grande"},

    { ER_CANNOT_FIND_SAX1_DRIVER,
     "Impossibile trovare la classe driver SAX1 {0}"},

    { ER_SAX1_DRIVER_NOT_LOADED,
     "La classe driver SAX1 {0} \u00e8 stata trovata ma non \u00e8 stato possibile caricarla"},

    { ER_SAX1_DRIVER_NOT_INSTANTIATED,
     "La classe driver SAX1 {0} \u00e8 stata caricata ma non \u00e8 stato possibile istanziarla"},

    { ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER,
     "La classe driver SAX1 {0} non implementa org.xml.sax.Parser"},

    { ER_PARSER_PROPERTY_NOT_SPECIFIED,
     "Propriet\u00e0 di sistema org.xml.sax.parser non specificata"},

    { ER_PARSER_ARG_CANNOT_BE_NULL,
     "L'argomento Parser non pu\u00f2 essere nullo"},

    { ER_FEATURE,
     "Funzione: {0}"},

    { ER_PROPERTY,
     "Propriet\u00e0: {0}"},

    { ER_NULL_ENTITY_RESOLVER,
     "Resolver entit\u00e0 nullo"},

    { ER_NULL_DTD_HANDLER,
     "Handler DTD nullo"},

    { ER_NO_DRIVER_NAME_SPECIFIED,
     "Non \u00e8 stato specificato alcun nome driver."},

    { ER_NO_URL_SPECIFIED,
     "Non \u00e8 stato specificato alcun URL."},

    { ER_POOLSIZE_LESS_THAN_ONE,
     "La dimensione del pool \u00e8 inferiore a 1."},

    { ER_INVALID_DRIVER_NAME,
     "Specificato nome driver non valido."},

    { ER_ERRORLISTENER,
     "ErrorListener"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The name
//   'ElemTemplateElement' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_NO_TEMPLATE_PARENT,
     "Errore di programmazione. Espressione senza ElemTemplateElement principale"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The substitution text
//   provides further information in order to diagnose the problem.  The name
//   'RedundentExprEliminator' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR,
     "Asserzione del programmatore in RedundentExprEliminator: {0}"},

    { ER_NOT_ALLOWED_IN_POSITION,
     "{0}non \u00e8 consentito in questa posizione in stylesheet"},

    { ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION,
     "Testo Non-whitespace non consentito in questa posizione in stylesheet"},

  // This code is shared with warning codes.
  // SystemId Unknown
    { INVALID_TCHAR,
     "Valore non valido: {1} utilizzato per l''''attributo CHAR: {0}.  Un attributo di tipo CHAR deve essere di 1 solo carattere."},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "QNAME" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value and {0} is the attribute name.
    //The following codes are shared with the warning codes...
    { INVALID_QNAME,
     "Valore non valido: {1} utilizzato per l''''attributo QNAME: {0}"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "ENUM" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value, {0} is the attribute name, and {2} is a list of valid
    // values.
    { INVALID_ENUM,
     "Valore non valido: {1} utilizzato per l''''attributo ENUM: {0}.  I valori validi sono: {2}."},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NMTOKEN" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NMTOKEN,
     "Valore non valido: {1} utilizzato per l''''attributo NMTOKEN: {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NCNAME" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NCNAME,
     "Valore non valido: {1} utilizzato per l''''attributo NCNAME: {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "boolean" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_BOOLEAN,
     "Valore non valido: {1} utilizzato per l''''attributo boolean: {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "number" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
     { INVALID_NUMBER,
     "Valore non valido: {1} utilizzato per l''''attributo number: {0} "},


  // End of shared codes...

// Note to translators:  A "match pattern" is a special form of XPath expression
// that is used for matching patterns.  The substitution text is the name of
// a function.  The message indicates that when this function is referenced in
// a match pattern, its argument must be a string literal (or constant.)
// ER_ARG_LITERAL - new error message for bugzilla //5202
    { ER_ARG_LITERAL,
     "L''''argomento di {0} nel modello di corrispondenza deve essere letterale."},

// Note to translators:  The following message indicates that two definitions of
// a variable.  A "global variable" is a variable that is accessible everywher
// in the stylesheet.
// ER_DUPLICATE_GLOBAL_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_GLOBAL_VAR,
     "Dichiarazione di variabile globale duplicata."},


// Note to translators:  The following message indicates that two definitions of
// a variable were encountered.
// ER_DUPLICATE_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_VAR,
     "Dichiarazione di variabile duplicata."},

    // Note to translators:  "xsl:template, "name" and "match" are XSLT keywords
    // which must not be translated.
    // ER_TEMPLATE_NAME_MATCH - new error message for bugzilla #789
    { ER_TEMPLATE_NAME_MATCH,
     "xsl:template deve avere un attributo name oppure match (o entrambi)"},

    // Note to translators:  "exclude-result-prefixes" is an XSLT keyword which
    // should not be translated.  The message indicates that a namespace prefix
    // encountered as part of the value of the exclude-result-prefixes attribute
    // was in error.
    // ER_INVALID_PREFIX - new error message for bugzilla #788
    { ER_INVALID_PREFIX,
     "Prefisso in exclude-result-prefixes non valido: {0}"},

    // Note to translators:  An "attribute set" is a set of attributes that can
    // be added to an element in the output document as a group.  The message
    // indicates that there was a reference to an attribute set named {0} that
    // was never defined.
    // ER_NO_ATTRIB_SET - new error message for bugzilla #782
    { ER_NO_ATTRIB_SET,
     "attribute-set denominato {0} non esiste"},

    // Note to translators:  This message indicates that there was a reference
    // to a function named {0} for which no function definition could be found.
    { ER_FUNCTION_NOT_FOUND,
     "La funzione {0} indicata non esiste"},

    // Note to translators:  This message indicates that the XSLT instruction
    // that is named by the substitution text {0} must not contain other XSLT
    // instructions (content) or a "select" attribute.  The word "select" is
    // an XSLT keyword in this case and must not be translated.
    { ER_CANT_HAVE_CONTENT_AND_SELECT,
     "L''''elemento {0} non deve avere sia un attributo content o selection."},

    // Note to translators:  This message indicates that the value argument
    // of setParameter must be a valid Java Object.
    { ER_INVALID_SET_PARAM_VALUE,
     "Il valore del parametro {0} deve essere un oggetto Java valido"},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT,
         "L'attributo result-prefix si un elemento xsl:namespace-alias ha il valore '#default', ma non c'\u00e8 dichiarazione dello spazio nome predefinito nell'ambito per l'elemento"},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX,
         "L''attributo result-prefix di un elemento xsl:namespace-alias ha il valore ''{0}'', ma non c''\u00e8 dichiarazione dello spazio per il prefisso ''{0}'' nell''ambito per l''elemento."},

    { ER_SET_FEATURE_NULL_NAME,
      "Il nome della funzione non pu\u00f2 essere nullo in TransformerFactory.setFeature(Nome stringa, valore booleano)."},

    { ER_GET_FEATURE_NULL_NAME,
      "Il nome della funzione non pu\u00f2 essere nullo in TransformerFactory.getFeature(Nome stringa)."},

    { ER_UNSUPPORTED_FEATURE,
      "Impossibile impostare la funzione ''{0}'' su questo TransformerFactory."},

    { ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING,
        "L''''utilizzo di un elemento di estensione ''{0}'' non \u00e8 consentito quando la funzione di elaborazione sicura \u00e8 impostata su true."},

        { ER_NAMESPACE_CONTEXT_NULL_NAMESPACE,
          "Impossibile ottenere il prefisso per un uri dello spazio nome nullo."},

        { ER_NAMESPACE_CONTEXT_NULL_PREFIX,
          "Impossibile ottenere l'uri dello spazio nome per il prefisso null."},

        { ER_XPATH_RESOLVER_NULL_QNAME,
          "Il nome della funzione non pu\u00f2 essere null."},

        { ER_XPATH_RESOLVER_NEGATIVE_ARITY,
          "Arity non pu\u00f2 essere negativo."},

  // Warnings...

    { WG_FOUND_CURLYBRACE,
      "Rilevato '}' senza una maschera attributo aperta."},

    { WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR,
      "Attenzione: l''attributo count non corrisponde ad un predecessore in xsl:number! Destinazione = {0}"},

    { WG_EXPR_ATTRIB_CHANGED_TO_SELECT,
      "Sintassi obsoleta: Il nome dell'attributo 'expr' \u00e8 stato modificato in 'select'."},

    { WG_NO_LOCALE_IN_FORMATNUMBER,
      "Xalan non gestisce ancora il nome locale nella funzione formato-numero."},

    { WG_LOCALE_NOT_FOUND,
      "Attenzione: Impossibile trovare la locale per xml:lang={0}"},

    { WG_CANNOT_MAKE_URL_FROM,
      "Impossibile ricavare l''''URL da: {0}"},

    { WG_CANNOT_LOAD_REQUESTED_DOC,
      "Impossibile caricare il documento richiesto: {0}"},

    { WG_CANNOT_FIND_COLLATOR,
      "Impossibile trovare Collator per <sort xml:lang={0}"},

    { WG_FUNCTIONS_SHOULD_USE_URL,
      "Sintassi obsoleta: l''istruzione functions deve utilizzare un url di {0}"},

    { WG_ENCODING_NOT_SUPPORTED_USING_UTF8,
      "codifica non supportata: {0}, viene utilizzato UTF-8"},

    { WG_ENCODING_NOT_SUPPORTED_USING_JAVA,
      "codifica non supportata: {0}, viene utilizzato Java {1}"},

    { WG_SPECIFICITY_CONFLICTS,
      "Sono stati rilevati conflitti di specificit\u00e0: {0} Verr\u00e0 utilizzato l''ultimo trovato nel foglio di lavoro."},

    { WG_PARSING_AND_PREPARING,
      "========= Analisi e preparazione {0} =========="},

    { WG_ATTR_TEMPLATE,
     "Maschera attributo, {0}"},

    { WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE,
      "Conflitto di corrispondenza tra xsl:strip-space e xsl:preserve-space"},

    { WG_ATTRIB_NOT_HANDLED,
      "Xalan non pu\u00f2 ancora gestire l''''attributo {0}."},

    { WG_NO_DECIMALFORMAT_DECLARATION,
      "Nessuna dichiarazione trovata per il formato decimale: {0}"},

    { WG_OLD_XSLT_NS,
     "XSLT Namespace mancante o non corretto. "},

    { WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED,
      "\u00c8 consentita una sola dichiarazione xsl:decimal-format predefinita."},

    { WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE,
      "I nomi xsl:decimal-format devono essere univoci. Il nome \"{0}\" \u00e8 stato duplicato."},

    { WG_ILLEGAL_ATTRIBUTE,
      "{0} ha un attributo non valido: {1}"},

    { WG_COULD_NOT_RESOLVE_PREFIX,
      "Impossibile risolvere il prefisso dello spazio nome: {0}. Il nodo verr\u00e0 ignorato."},

    { WG_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet richiede un attributo 'version'."},

    { WG_ILLEGAL_ATTRIBUTE_NAME,
      "Nome attributo non valido: {0}"},

    { WG_ILLEGAL_ATTRIBUTE_VALUE,
      "Valore non valido utilizzato per l''''attributo {0}: {1}"},

    { WG_EMPTY_SECOND_ARG,
      "Il nodeset che risulta dal secondo argomento della funzione documento \u00e8 vuoto. Restituisce un nodeset vuoto."},

  //Following are the new WARNING keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.
    { WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "Il valore dell'attributo 'name' del nome xsl:processing-instruction non deve essere 'xml'"},

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.  "NCName" is an XML data-type and must not be
    // translated.
    { WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "Il valore dell''attributo ''name'' di xsl:processing-instruction deve essere un NCName valido: {0}"},

    // Note to translators:  This message is reported if the stylesheet that is
    // being processed attempted to construct an XML document with an attribute in a
    // place other than on an element.  The substitution text specifies the name of
    // the attribute.
    { WG_ILLEGAL_ATTRIBUTE_POSITION,
      "Impossibile aggiungere l''''attributo {0} dopo i nodi secondari o prima che sia prodotto un elemento.  L''''attributo verr\u00e0 ignorato."},

    { NO_MODIFICATION_ALLOWED_ERR,
      "\u00c8 stato effettuato un tentativo di modificare un oggetto in un contesto in cui le modifiche non sono supportate."
    },

    //Check: WHY THERE IS A GAP B/W NUMBERS in the XSLTErrorResources properties file?

  // Other miscellaneous text used inside the code...
  { "ui_language", "it"},
  {  "help_language",  "it" },
  {  "language",  "it" },
  { "BAD_CODE", "Il parametro per createMessage fuori limite"},
  {  "FORMAT_FAILED", "Rilevata eccezione durante la chiamata messageFormat"},
  {  "version", ">>>>>>> Versione Xalan "},
  {  "version2",  "<<<<<<<"},
  {  "yes", "s\u00ec"},
  { "line", "Riga #"},
  { "column","Colonna #"},
  { "xsldone", "XSLProcessor: eseguito"},


  // Note to translators:  The following messages provide usage information
  // for the Xalan Process command line.  "Process" is the name of a Java class,
  // and should not be translated.
  { "xslProc_option", "Opzioni classe Process riga comandi Xalan-J:"},
  { "xslProc_option", "Opzioni classe Process riga comandi Xalan-J\u003a"},
  { "xslProc_invalid_xsltc_option", "Opzione {0} non supportata in modalit\u00e0."},
  { "xslProc_invalid_xalan_option", "L''''opzione {0} pu\u00f2 essere utilizzata solo con -XSLTC."},
  { "xslProc_no_input", "Errore: Nessun foglio di lavoro o xml di immissione specificato. Eseguire questo comando senza opzioni per istruzioni sull'utilizzo."},
  { "xslProc_common_options", "-Opzioni comuni-"},
  { "xslProc_xalan_options", "-Opzioni per Xalan-"},
  { "xslProc_xsltc_options", "-Opzioni per XSLTC-"},
  { "xslProc_return_to_continue", "(premere <invio> per continuare)"},

   // Note to translators: The option name and the parameter name do not need to
   // be translated. Only translate the messages in parentheses.  Note also that
   // leading whitespace in the messages is used to indent the usage information
   // for each option in the English messages.
   // Do not translate the keywords: XSLTC, SAX, DOM and DTM.
  { "optionXSLTC", "   [-XSLTC (utilizza XSLTC per la trasformazioni)]"},
  { "optionIN", "   [-IN inputXMLURL]"},
  { "optionXSL", "   [-XSL XSLTransformationURL]"},
  { "optionOUT",  "   [-OUT outputFileName]"},
  { "optionLXCIN", "   [-LXCIN compiledStylesheetFileNameIn]"},
  { "optionLXCOUT", "   [-LXCOUT compiledStylesheetFileNameOutOut]"},
  { "optionPARSER", "   [-PARSER nome classe completo del collegamento parser]"},
  {  "optionE", "   [-E (non espandere i riferimenti entit\u00e0)]"},
  {  "optionV",  "   [-E (non espandere i riferimenti entit\u00e0)]"},
  {  "optionQC", "   [-QC (Silenziamento avvertenze conflitti modelli)]"},
  {  "optionQ", "   [-Q  (Modo silenzioso)]"},
  {  "optionLF", "   [-LF (Utilizza il caricamento riga solo sull'output {valore predefinito: CR/LF})]"},
  {  "optionCR", "   [-CR (Utilizza il ritorno a capo solo sull'output {valore predefinito: CR/LF})]"},
  { "optionESCAPE", "   [-ESCAPE (specifica quali caratteri saltare {valore predefinito: <>&\"\'\\r\\n}]"},
  { "optionINDENT", "   [-INDENT (Controlla il numero dei rientri {valore predefinito: 0})]"},
  { "optionTT", "   [-TT (Traccia le maschere quando vengono richiamate.)]"},
  { "optionTG", "   [-TG (Traccia ogni evento di generazione.)]"},
  { "optionTS", "   [-TS (Traccia ogni evento di selezione.)]"},
  {  "optionTTC", "   [-TTC (Traccia il secondario della maschera quando viene elaborato.)]"},
  { "optionTCLASS", "   [-TCLASS (classe TraceListener per le estensioni di traccia.)]"},
  { "optionVALIDATE", "   [-VALIDATE (Imposta se eseguire la convalida.  Il valore predefinito per la convalida \u00e8 disattivato.)]"},
  { "optionEDUMP", "   [-EDUMP {nome file facoltativo} (Eseguire stackdump in caso di errori.)]"},
  {  "optionXML", "   [-XML (Utilizza la formattazione XML e aggiunge intestazione XML.)]"},
  {  "optionTEXT", "   [-TEXT (Utilizza la formattazione Testo semplice.)]"},
  {  "optionHTML", "   [-HTML (Utilizza la formattazione HTML.)]"},
  {  "optionPARAM", "   [-PARAM nome espressione (imposta un parametro del foglio di lavoro)]"},
  {  "noParsermsg1", "Elaborazione XSL non riuscita."},
  {  "noParsermsg2", "** Impossibile trovare il parser **"},
  { "noParsermsg3",  "Controllare il classpath."},
  { "noParsermsg4", "Se non si possiede IBM XML Parser per Java, \u00e8 possibile scaricarlo da"},
  { "noParsermsg5", "IBM AlphaWorks: http://www.alphaworks.ibm.com/formula/xml"},
  { "optionURIRESOLVER", "   [-URIRESOLVER nome classe completo (URIResolver da utilizzare per risolvere gli URI)]"},
  { "optionENTITYRESOLVER",  "   [-ENTITYRESOLVER nome classe completo (EntityResolver da utilizzare per risolvere le entit\u00e0)]"},
  { "optionCONTENTHANDLER",  "   [-CONTENTHANDLER nome classe completo (ContentHandler da utilizzare per serializzare l'output)]"},
  {  "optionLINENUMBERS",  "   [-L utilizza i numeri riga per il documento di origine]"},
  { "optionSECUREPROCESSING", "   [-SECURE (imposta la funzione di elaborazione sicura su true.)]"},

    // Following are the new options added in XSLTErrorResources.properties files after Jdk 1.4 (Xalan 2.2-D11)


  {  "optionMEDIA",  "   [-MEDIA mediaType (utilizza l'attributo media per individuare il foglio di lavoro associato ad un documento.)]"},
  {  "optionFLAVOR",  "   [-FLAVOR flavorName (Utilizza in modo esplicito s2s=SAX oppure d2d=DOM per eseguire la trasformazione.)] "}, // Added by sboag/scurcuru; experimental
  { "optionDIAG", "   [-DIAG (Visualizza il tempo impiegato in millisecondi per la trasformazione.)]"},
  { "optionINCREMENTAL",  "   [-INCREMENTAL (richiede la costruzione DTM incrementale impostando http://xml.apache.org/xalan/features/incremental true.)]"},
  {  "optionNOOPTIMIMIZE",  "   [-NOOPTIMIMIZE (non richiede alcuna elaborazione di ottimizzazione del foglio di lavoro impostando http://xml.apache.org/xalan/features/optimize false.)]"},
  { "optionRL",  "   [-RL recursionlimit (limite numerico asserzioni nella profondit\u00e0 ricorsiva del foglio di lavoro.)]"},
  {   "optionXO",  "   [-XO [transletName] (assegna il nome al translet generato)]"},
  {  "optionXD", "   [-XD destinationDirectory (specifica una directory di destinazione per il translet)]"},
  {  "optionXJ",  "   [-XJ jarfile (raggruppa la classi translet in un file jar di nome <jarfile>)]"},
  {   "optionXP",  "   [-XP package (specifica un prefisso di nome pacchetto per tutte le classi translet generate)]"},

  //AddITIONAL  STRINGS that need L10n
  // Note to translators:  The following message describes usage of a particular
  // command-line option that is used to enable the "template inlining"
  // optimization.  The optimization involves making a copy of the code
  // generated for a template in another template that refers to it.
  { "optionXN",  "   [-XN (abilita l'allineamento della maschera)]" },
  { "optionXX",  "   [-XX (attiva ulteriori emissioni di messaggi di debug)]"},
  { "optionXT" , "   [-XT (utilizza il translet per la trasformazione, se possibile)]"},
  { "diagTiming"," --------- La trasformazione di {0} utilizzando {1} ha impiegato {2} ms" },
  { "recursionTooDeep","Nidificazione della maschera troppo elevata. nesting = {0}, maschera {1} {2}" },
  { "nameIs", "il nome \u00e8" },
  { "matchPatternIs", "il modello di corrispondenza \u00e8" }

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
  public static final String ERROR_HEADER = "Errore: ";

  /** String to prepend to warning messages.    */
  public static final String WARNING_HEADER = "Avvertenza: ";

  /** String to specify the XSLT module.  */
  public static final String XSL_HEADER = "XSLT ";

  /** String to specify the XML parser module.  */
  public static final String XML_HEADER = "XML ";

  /** I don't think this is used any more.
   * @deprecated  */
  public static final String QUERY_HEADER = "MODELLO ";


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
