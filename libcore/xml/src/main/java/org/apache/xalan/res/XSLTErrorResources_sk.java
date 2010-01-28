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
 * $Id: XSLTErrorResources_sk.java 468641 2006-10-28 06:54:42Z minchau $
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
public class XSLTErrorResources_sk extends ListResourceBundle
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
      "Chyba: Nie je mo\u017en\u00e9 ma\u0165 vo v\u00fdraze '{'"},

    { ER_ILLEGAL_ATTRIBUTE ,
     "{0} m\u00e1 neplatn\u00fd atrib\u00fat: {1}"},

  {ER_NULL_SOURCENODE_APPLYIMPORTS ,
      "sourceNode je v xsl:apply-imports nulov\u00fd!"},

  {ER_CANNOT_ADD,
      "Nem\u00f4\u017ee prida\u0165 {0} do {1}"},

    { ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES,
      "sourceNode je nulov\u00fd v handleApplyTemplatesInstruction!"},

    { ER_NO_NAME_ATTRIB,
     "{0} mus\u00ed ma\u0165 atrib\u00fat n\u00e1zvu."},

    {ER_TEMPLATE_NOT_FOUND,
     "Nebolo mo\u017en\u00e9 n\u00e1js\u0165 vzor s n\u00e1zvom: {0}"},

    {ER_CANT_RESOLVE_NAME_AVT,
      "Nebolo mo\u017en\u00e9 rozl\u00ed\u0161i\u0165 n\u00e1zov AVT v xsl:call-template."},

    {ER_REQUIRES_ATTRIB,
     "{0} vy\u017eaduje atrib\u00fat: {1}"},

    { ER_MUST_HAVE_TEST_ATTRIB,
      "{0} mus\u00ed ma\u0165 atrib\u00fat ''test''."},

    {ER_BAD_VAL_ON_LEVEL_ATTRIB,
      "Nespr\u00e1vna hodnota na atrib\u00fate \u00farovne: {0}"},

    {ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "n\u00e1zov processing-instruction nem\u00f4\u017ee by\u0165 'xml'"},

    { ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "n\u00e1zov in\u0161trukcie spracovania mus\u00ed by\u0165 platn\u00fdm NCName: {0}"},

    { ER_NEED_MATCH_ATTRIB,
      "{0} mus\u00ed ma\u0165 porovn\u00e1vac\u00ed atrib\u00fat, ak m\u00e1 re\u017eim."},

    { ER_NEED_NAME_OR_MATCH_ATTRIB,
      "{0} vy\u017eaduje bu\u010f n\u00e1zov, alebo porovn\u00e1vac\u00ed atrib\u00fat."},

    {ER_CANT_RESOLVE_NSPREFIX,
      "Nie je mo\u017en\u00e9 rozl\u00ed\u0161i\u0165 predponu n\u00e1zvov\u00e9ho priestoru: {0}"},

    { ER_ILLEGAL_VALUE,
     "xml:space m\u00e1 neplatn\u00fa hodnotu: {0}"},

    { ER_NO_OWNERDOC,
      "Potomok uzla nem\u00e1 dokument vlastn\u00edka!"},

    { ER_ELEMTEMPLATEELEM_ERR,
     "Chyba ElemTemplateElement: {0}"},

    { ER_NULL_CHILD,
     "Pokus o pridanie nulov\u00e9ho potomka!"},

    { ER_NEED_SELECT_ATTRIB,
     "{0} vy\u017eaduje atrib\u00fat v\u00fdberu."},

    { ER_NEED_TEST_ATTRIB ,
      "xsl:when mus\u00ed ma\u0165 atrib\u00fat 'test'."},

    { ER_NEED_NAME_ATTRIB,
      "xsl:with-param mus\u00ed ma\u0165 atrib\u00fat 'name'."},

    { ER_NO_CONTEXT_OWNERDOC,
      "kontext nem\u00e1 dokument vlastn\u00edka!"},

    {ER_COULD_NOT_CREATE_XML_PROC_LIAISON,
      "Nebolo mo\u017en\u00e9 vytvori\u0165 vz\u0165ah XML TransformerFactory: {0}"},

    {ER_PROCESS_NOT_SUCCESSFUL,
      "Xalan: Proces bol ne\u00faspe\u0161n\u00fd."},

    { ER_NOT_SUCCESSFUL,
     "Xalan: bol ne\u00faspe\u0161n\u00fd."},

    { ER_ENCODING_NOT_SUPPORTED,
     "K\u00f3dovanie nie je podporovan\u00e9: {0}"},

    {ER_COULD_NOT_CREATE_TRACELISTENER,
      "Nebolo mo\u017en\u00e9 vytvori\u0165 TraceListener: {0}"},

    {ER_KEY_REQUIRES_NAME_ATTRIB,
      "xsl:key vy\u017eaduje atrib\u00fat 'name'!"},

    { ER_KEY_REQUIRES_MATCH_ATTRIB,
      "xsl:key vy\u017eaduje atrib\u00fat 'match'!"},

    { ER_KEY_REQUIRES_USE_ATTRIB,
      "xsl:key vy\u017eaduje atrib\u00fat 'use'!"},

    { ER_REQUIRES_ELEMENTS_ATTRIB,
      "(StylesheetHandler) {0} vy\u017eaduje atrib\u00fat ''elements''!"},

    { ER_MISSING_PREFIX_ATTRIB,
      "(StylesheetHandler) {0} ch\u00fdba atrib\u00fat ''prefix''"},

    { ER_BAD_STYLESHEET_URL,
     "URL \u0161t\u00fdlu dokumentu je nespr\u00e1vna: {0}"},

    { ER_FILE_NOT_FOUND,
     "S\u00fabor \u0161t\u00fdlu dokumentu nebol n\u00e1jden\u00fd: {0}"},

    { ER_IOEXCEPTION,
      "V s\u00fabore \u0161t\u00fdlu dokumentu bola vstupno-v\u00fdstupn\u00e1 v\u00fdnimka: {0}"},

    { ER_NO_HREF_ATTRIB,
      "(StylesheetHandler) Nebolo mo\u017en\u00e9 n\u00e1js\u0165 atrib\u00fat href pre {0}"},

    { ER_STYLESHEET_INCLUDES_ITSELF,
      "(StylesheetHandler) {0} priamo, alebo nepriamo, obsahuje s\u00e1m seba!"},

    { ER_PROCESSINCLUDE_ERROR,
      "chyba StylesheetHandler.processInclude, {0}"},

    { ER_MISSING_LANG_ATTRIB,
      "(StylesheetHandler) {0} ch\u00fdba atrib\u00fat ''lang''"},

    { ER_MISSING_CONTAINER_ELEMENT_COMPONENT,
      "(StylesheetHandler) chybne umiestnen\u00fd {0} element?? Ch\u00fdba kontajnerov\u00fd prvok ''component''"},

    { ER_CAN_ONLY_OUTPUT_TO_ELEMENT,
      "M\u00f4\u017ee prev\u00e1dza\u0165 v\u00fdstup len do Element, DocumentFragment, Document, alebo PrintWriter."},

    { ER_PROCESS_ERROR,
     "chyba StylesheetRoot.process"},

    { ER_UNIMPLNODE_ERROR,
     "Chyba UnImplNode: {0}"},

    { ER_NO_SELECT_EXPRESSION,
      "Chyba! Nena\u0161lo sa vyjadrenie v\u00fdberu xpath (-select)."},

    { ER_CANNOT_SERIALIZE_XSLPROCESSOR,
      "Nie je mo\u017en\u00e9 serializova\u0165 XSLProcessor!"},

    { ER_NO_INPUT_STYLESHEET,
      "Nebol zadan\u00fd vstup \u0161t\u00fdl dokumentu!"},

    { ER_FAILED_PROCESS_STYLESHEET,
      "Zlyhalo spracovanie \u0161t\u00fdlu dokumentu!"},

    { ER_COULDNT_PARSE_DOC,
     "Nebolo mo\u017en\u00e9 analyzova\u0165 dokument {0}!"},

    { ER_COULDNT_FIND_FRAGMENT,
     "Nebolo mo\u017en\u00e9 n\u00e1js\u0165 fragment: {0}"},

    { ER_NODE_NOT_ELEMENT,
      "Uzol, na ktor\u00fd ukazuje identifik\u00e1tor fragmentu nebol elementom: {0}"},

    { ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB,
      "for-each mus\u00ed ma\u0165 bu\u010f porovn\u00e1vac\u00ed atrib\u00fat, alebo atrib\u00fat n\u00e1zvu"},

    { ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB,
      "vzory musia ma\u0165 bu\u010f porovn\u00e1vacie atrib\u00faty, alebo atrib\u00faty n\u00e1zvov"},

    { ER_NO_CLONE_OF_DOCUMENT_FRAG,
      "\u017diadna k\u00f3pia fragmentu dokumentu!"},

    { ER_CANT_CREATE_ITEM,
      "Nie je mo\u017en\u00e9 vytvori\u0165 polo\u017eku vo v\u00fdsledkovom strome: {0}"},

    { ER_XMLSPACE_ILLEGAL_VALUE,
      "xml:space v zdrojovom XML m\u00e1 neplatn\u00fa hodnotu: {0}"},

    { ER_NO_XSLKEY_DECLARATION,
      "Neexistuje \u017eiadna deklar\u00e1cia xsl:key pre {0}!"},

    { ER_CANT_CREATE_URL,
     "Chyba! Nie je mo\u017en\u00e9 vytvori\u0165 url pre: {0}"},

    { ER_XSLFUNCTIONS_UNSUPPORTED,
     "xsl:functions nie je podporovan\u00e9"},

    { ER_PROCESSOR_ERROR,
     "Chyba XSLT TransformerFactory"},

    { ER_NOT_ALLOWED_INSIDE_STYLESHEET,
      "(StylesheetHandler) {0} nie je povolen\u00fd vn\u00fatri \u0161t\u00fdlu dokumentu!"},

    { ER_RESULTNS_NOT_SUPPORTED,
      "result-ns u\u017e viac nie je podporovan\u00fd!  Pou\u017eite namiesto toho xsl:output."},

    { ER_DEFAULTSPACE_NOT_SUPPORTED,
      "default-space u\u017e viac nie je podporovan\u00fd!  Pou\u017eite namiesto toho xsl:strip-space alebo xsl:preserve-space."},

    { ER_INDENTRESULT_NOT_SUPPORTED,
      "indent-result u\u017e viac nie je podporovan\u00fd!  Pou\u017eite namiesto toho xsl:output."},

    { ER_ILLEGAL_ATTRIB,
      "(StylesheetHandler) {0} m\u00e1 neplatn\u00fd atrib\u00fat: {1}"},

    { ER_UNKNOWN_XSL_ELEM,
     "Nezn\u00e1my element XSL: {0}"},

    { ER_BAD_XSLSORT_USE,
      "(StylesheetHandler) xsl:sort mo\u017eno pou\u017ei\u0165 len s xsl:apply-templates alebo xsl:for-each."},

    { ER_MISPLACED_XSLWHEN,
      "(StylesheetHandler) xsl:when na nespr\u00e1vnom mieste!"},

    { ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:when nem\u00e1 ako rodi\u010da xsl:choose!"},

    { ER_MISPLACED_XSLOTHERWISE,
      "(StylesheetHandler) xsl:otherwise na nespr\u00e1vnom mieste!"},

    { ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:otherwise nem\u00e1 ako rodi\u010da xsl:choose!"},

    { ER_NOT_ALLOWED_INSIDE_TEMPLATE,
      "(StylesheetHandler) {0} nie je povolen\u00fd vn\u00fatri vzoru!"},

    { ER_UNKNOWN_EXT_NS_PREFIX,
      "(StylesheetHandler) {0} prefix roz\u0161\u00edren\u00e9ho n\u00e1zvov\u00e9ho priestoru {1} nie je zn\u00e1my"},

    { ER_IMPORTS_AS_FIRST_ELEM,
      "(StylesheetHandler) Importy sa m\u00f4\u017eu vyskytn\u00fa\u0165 len ako prv\u00e9 \u010dasti \u0161t\u00fdlu dokumentu!"},

    { ER_IMPORTING_ITSELF,
      "(StylesheetHandler) {0} priamo, alebo nepriami, importuje s\u00e1m seba!"},

    { ER_XMLSPACE_ILLEGAL_VAL,
      "(StylesheetHandler) xml:space m\u00e1 neplatn\u00fa hodnotu: {0}"},

    { ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL,
      "processStylesheet nebol \u00faspe\u0161n\u00fd!"},

    { ER_SAX_EXCEPTION,
     "V\u00fdnimka SAX"},

//  add this message to fix bug 21478
    { ER_FUNCTION_NOT_SUPPORTED,
     "Funkcia nie je podporovan\u00e1!"},


    { ER_XSLT_ERROR,
     "Chyba XSLT"},

    { ER_CURRENCY_SIGN_ILLEGAL,
      "znak meny nie je povolen\u00fd vo re\u0165azci form\u00e1tu vzoru"},

    { ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM,
      "Funckia dokumentu nie je podporovan\u00e1 v \u0161t\u00fdle dokumentu DOM!"},

    { ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER,
      "Nie je mo\u017en\u00e9 ur\u010di\u0165 prefix bezprefixov\u00e9ho rozklada\u010da!"},

    { ER_REDIRECT_COULDNT_GET_FILENAME,
      "Roz\u0161\u00edrenie presmerovania: Nedal sa z\u00edska\u0165 n\u00e1zov s\u00faboru - s\u00fabor alebo atrib\u00fat v\u00fdberu mus\u00ed vr\u00e1ti\u0165 platn\u00fd re\u0165azec."},

    { ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT,
      "Nie je mo\u017en\u00e9 vytvori\u0165 FormatterListener v roz\u0161\u00edren\u00ed Redirect!"},

    { ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX,
      "Predpona v exclude-result-prefixes je neplatn\u00e1: {0}"},

    { ER_MISSING_NS_URI,
      "Ch\u00fdbaj\u00faci n\u00e1zvov\u00fd priestor URI pre zadan\u00fd prefix"},

    { ER_MISSING_ARG_FOR_OPTION,
      "Ch\u00fdbaj\u00faci argument pre vo\u013ebu: {0}"},

    { ER_INVALID_OPTION,
     "Neplatn\u00e1 vo\u013eba. {0}"},

    { ER_MALFORMED_FORMAT_STRING,
     "Znetvoren\u00fd re\u0165azec form\u00e1tu: {0}"},

    { ER_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet si vy\u017eaduje atrib\u00fat 'version'!"},

    { ER_ILLEGAL_ATTRIBUTE_VALUE,
      "Atrib\u00fat: {0} m\u00e1 neplatn\u00fa hodnotu: {1}"},

    { ER_CHOOSE_REQUIRES_WHEN,
     "xsl:choose vy\u017eaduje xsl:when"},

    { ER_NO_APPLY_IMPORT_IN_FOR_EACH,
      "xsl:apply-imports nie je povolen\u00fd v xsl:for-each"},

    { ER_CANT_USE_DTM_FOR_OUTPUT,
      "Nem\u00f4\u017ee pou\u017ei\u0165 DTMLiaison pre v\u00fdstupn\u00fd uzol DOM... namiesto neho odo\u0161lite org.apache.xpath.DOM2Helper!"},

    { ER_CANT_USE_DTM_FOR_INPUT,
      "Nem\u00f4\u017ee pou\u017ei\u0165 DTMLiaison pre vstupn\u00fd uzol DOM... namiesto neho odo\u0161lite org.apache.xpath.DOM2Helper!"},

    { ER_CALL_TO_EXT_FAILED,
      "Volanie elementu roz\u0161\u00edrenia zlyhalo: {0}"},

    { ER_PREFIX_MUST_RESOLVE,
      "Predpona sa mus\u00ed rozl\u00ed\u0161i\u0165 do n\u00e1zvov\u00e9ho priestoru: {0}"},

    { ER_INVALID_UTF16_SURROGATE,
      "Bolo zisten\u00e9 neplatn\u00e9 nahradenie UTF-16: {0} ?"},

    { ER_XSLATTRSET_USED_ITSELF,
      "xsl:attribute-set {0} pou\u017eil s\u00e1m seba, \u010do sp\u00f4sob\u00ed nekone\u010dn\u00fa slu\u010dku."},

    { ER_CANNOT_MIX_XERCESDOM,
      "Nie je mo\u017en\u00e9 mie\u0161a\u0165 vstup in\u00fd, ne\u017e Xerces-DOM s v\u00fdstupom Xerces-DOM!"},

    { ER_TOO_MANY_LISTENERS,
      "addTraceListenersToStylesheet - TooManyListenersException"},

    { ER_IN_ELEMTEMPLATEELEM_READOBJECT,
      "V ElemTemplateElement.readObject: {0}"},

    { ER_DUPLICATE_NAMED_TEMPLATE,
      "Na\u0161iel sa viac ne\u017e jeden vzor s n\u00e1zvom: {0}"},

    { ER_INVALID_KEY_CALL,
      "Neplatn\u00e9 volanie funkcie: rekurz\u00edvne volanie k\u013e\u00fa\u010da() nie je povolen\u00e9"},

    { ER_REFERENCING_ITSELF,
      "Premenn\u00e1 {0} sa priamo, alebo nepriamo, odkazuje sama na seba!"},

    { ER_ILLEGAL_DOMSOURCE_INPUT,
      "Vstupn\u00fd uzol nem\u00f4\u017ee by\u0165 pre DOMSource pre newTemplates nulov\u00fd!"},

    { ER_CLASS_NOT_FOUND_FOR_OPTION,
        "S\u00fabor triedy nebol pre vo\u013ebu {0} n\u00e1jden\u00fd"},

    { ER_REQUIRED_ELEM_NOT_FOUND,
        "Po\u017eadovan\u00fd element sa nena\u0161iel: {0}"},

    { ER_INPUT_CANNOT_BE_NULL,
        "InputStream nem\u00f4\u017ee by\u0165 nulov\u00fd"},

    { ER_URI_CANNOT_BE_NULL,
        "URI nem\u00f4\u017ee by\u0165 nulov\u00fd"},

    { ER_FILE_CANNOT_BE_NULL,
        "S\u00fabor nem\u00f4\u017ee by\u0165 nulov\u00fd"},

    { ER_SOURCE_CANNOT_BE_NULL,
                "InputSource nem\u00f4\u017ee by\u0165 nulov\u00fd"},

    { ER_CANNOT_INIT_BSFMGR,
                "Nebolo mo\u017en\u00e9 inicializova\u0165 Spr\u00e1vcu BSF"},

    { ER_CANNOT_CMPL_EXTENSN,
                "Nebolo mo\u017en\u00e9 skompilova\u0165 pr\u00edponu"},

    { ER_CANNOT_CREATE_EXTENSN,
      "Nebolo mo\u017en\u00e9 vytvori\u0165 roz\u0161\u00edrenie: {0} z d\u00f4vodu: {1}"},

    { ER_INSTANCE_MTHD_CALL_REQUIRES,
      "Volanie met\u00f3dy met\u00f3dou in\u0161tancie {0} vy\u017eaduje ako prv\u00fd argument In\u0161tanciu objektu"},

    { ER_INVALID_ELEMENT_NAME,
      "Bol zadan\u00fd neplatn\u00fd n\u00e1zov s\u00fa\u010dasti {0}"},

    { ER_ELEMENT_NAME_METHOD_STATIC,
      "Met\u00f3da n\u00e1zvu s\u00fa\u010dasti mus\u00ed by\u0165 statick\u00e1 {0}"},

    { ER_EXTENSION_FUNC_UNKNOWN,
             "Roz\u0161\u00edrenie funkcie {0} : {1} je nezn\u00e1me"},

    { ER_MORE_MATCH_CONSTRUCTOR,
             "Bola n\u00e1jden\u00e1 viac ne\u017e jedna najlep\u0161ia zhoda s kon\u0161truktorom pre {0}"},

    { ER_MORE_MATCH_METHOD,
             "Bola n\u00e1jden\u00e1 viac ne\u017e jedna najlep\u0161ia zhoda pre met\u00f3du {0}"},

    { ER_MORE_MATCH_ELEMENT,
             "Bola n\u00e1jden\u00e1 viac ne\u017e jedna najlep\u0161ia zhoda pre met\u00f3du s\u00fa\u010dasti {0}"},

    { ER_INVALID_CONTEXT_PASSED,
             "Bolo odoslan\u00fd neplatn\u00fd kontext na zhodnotenie {0}"},

    { ER_POOL_EXISTS,
             "Oblas\u0165 u\u017e existuje"},

    { ER_NO_DRIVER_NAME,
             "Nebol zadan\u00fd \u017eiaden n\u00e1zov ovl\u00e1da\u010da"},

    { ER_NO_URL,
             "Nebola zadan\u00e1 \u017eiadna URL"},

    { ER_POOL_SIZE_LESSTHAN_ONE,
             "Ve\u013ekos\u0165 oblasti je men\u0161ia ne\u017e jeden!"},

    { ER_INVALID_DRIVER,
             "Bol zadan\u00fd neplatn\u00fd n\u00e1zov ovl\u00e1da\u010da!"},

    { ER_NO_STYLESHEETROOT,
             "Nebol n\u00e1jden\u00fd kore\u0148 \u0161t\u00fdlu dokumentu!"},

    { ER_ILLEGAL_XMLSPACE_VALUE,
         "Neplatn\u00e1 hodnota pre xml:space"},

    { ER_PROCESSFROMNODE_FAILED,
         "zlyhal processFromNode"},

    { ER_RESOURCE_COULD_NOT_LOAD,
        "Prostriedok [ {0} ] sa nedal na\u010d\u00edta\u0165: {1} \n {2} \t {3}"},

    { ER_BUFFER_SIZE_LESSTHAN_ZERO,
        "Ve\u013ekos\u0165 vyrovn\u00e1vacej pam\u00e4te <=0"},

    { ER_UNKNOWN_ERROR_CALLING_EXTENSION,
        "Nezn\u00e1ma chyba po\u010das volania pr\u00edpony"},

    { ER_NO_NAMESPACE_DECL,
        "Prefix {0} nem\u00e1 zodpovedaj\u00facu deklar\u00e1ciu n\u00e1zvov\u00e9ho priestoru"},

    { ER_ELEM_CONTENT_NOT_ALLOWED,
        "Obsah elementu nie je povolen\u00fd pre lang=javaclass {0}"},

    { ER_STYLESHEET_DIRECTED_TERMINATION,
        "Ukon\u010denie riaden\u00e9 \u0161t\u00fdlom dokumentu"},

    { ER_ONE_OR_TWO,
        "1, alebo 2"},

    { ER_TWO_OR_THREE,
        "2, alebo 3"},

    { ER_COULD_NOT_LOAD_RESOURCE,
        "Nebolo mo\u017en\u00e9 zavies\u0165 {0} (check CLASSPATH), teraz s\u00fa po\u017eit\u00e9 len predvolen\u00e9 \u0161tandardy"},

    { ER_CANNOT_INIT_DEFAULT_TEMPLATES,
        "Nie je mo\u017en\u00e9 inicializova\u0165 predvolen\u00e9 vzory"},

    { ER_RESULT_NULL,
        "V\u00fdsledok by nemal by\u0165 nulov\u00fd"},

    { ER_RESULT_COULD_NOT_BE_SET,
        "V\u00fdsledkom nem\u00f4\u017ee by\u0165 mno\u017eina"},

    { ER_NO_OUTPUT_SPECIFIED,
        "Nie je zadan\u00fd \u017eiaden v\u00fdstup"},

    { ER_CANNOT_TRANSFORM_TO_RESULT_TYPE,
        "Ned\u00e1 sa transformova\u0165 na v\u00fdsledok typu {0}"},

    { ER_CANNOT_TRANSFORM_SOURCE_TYPE,
        "Ned\u00e1 sa transformova\u0165 zdroj typu {0}"},

    { ER_NULL_CONTENT_HANDLER,
        "Nulov\u00fd manipula\u010dn\u00fd program obsahu"},

    { ER_NULL_ERROR_HANDLER,
        "Nulov\u00fd chybov\u00fd manipula\u010dn\u00fd program"},

    { ER_CANNOT_CALL_PARSE,
        "nem\u00f4\u017ee by\u0165 volan\u00e9 analyzovanie, ak nebol nastaven\u00fd ContentHandler"},

    { ER_NO_PARENT_FOR_FILTER,
        "\u017diaden rodi\u010d pre filter"},

    { ER_NO_STYLESHEET_IN_MEDIA,
         "Nena\u0161iel sa \u017eiadny stylesheet v: {0}, m\u00e9dium= {1}"},

    { ER_NO_STYLESHEET_PI,
         "Nena\u0161iel sa \u017eiadny xml-stylesheet PI v: {0}"},

    { ER_NOT_SUPPORTED,
       "Nie je podporovan\u00e9: {0}"},

    { ER_PROPERTY_VALUE_BOOLEAN,
       "Hodnota vlastnosti {0} by mala by\u0165 boolovsk\u00e1 in\u0161tancia"},

    { ER_COULD_NOT_FIND_EXTERN_SCRIPT,
         "Nie je mo\u017en\u00e9 dosiahnu\u0165 extern\u00fd skript na {0}"},

    { ER_RESOURCE_COULD_NOT_FIND,
        "Prostriedok [ {0} ] nemohol by\u0165 n\u00e1jden\u00fd.\n {1}"},

    { ER_OUTPUT_PROPERTY_NOT_RECOGNIZED,
        "V\u00fdstupn\u00e9 vlastn\u00edctvo nebolo rozoznan\u00e9: {0}"},

    { ER_FAILED_CREATING_ELEMLITRSLT,
        "Zlyhalo vytv\u00e1ranie in\u0161tancie ElemLiteralResult"},

  //Earlier (JDK 1.4 XALAN 2.2-D11) at key code '204' the key name was ER_PRIORITY_NOT_PARSABLE
  // In latest Xalan code base key name is  ER_VALUE_SHOULD_BE_NUMBER. This should also be taken care
  //in locale specific files like XSLTErrorResources_de.java, XSLTErrorResources_fr.java etc.
  //NOTE: Not only the key name but message has also been changed.

    { ER_VALUE_SHOULD_BE_NUMBER,
        "Hodnota pre {0} by mala obsahova\u0165 analyzovate\u013en\u00e9 \u010d\u00edslo"},

    { ER_VALUE_SHOULD_EQUAL,
        "Hodnota {0} by sa mala rovna\u0165 \u00e1no, alebo nie"},

    { ER_FAILED_CALLING_METHOD,
        "Zlyhalo volanie met\u00f3dy {0}"},

    { ER_FAILED_CREATING_ELEMTMPL,
        "Zlyhalo vytv\u00e1ranie in\u0161tancie ElemTemplateElement"},

    { ER_CHARS_NOT_ALLOWED,
        "V tomto bode dokumentu nie s\u00fa znaky povolen\u00e9"},

    { ER_ATTR_NOT_ALLOWED,
        "Atrib\u00fat \"{0}\" nie je povolen\u00fd na s\u00fa\u010dasti {1}!"},

    { ER_BAD_VALUE,
     "{0} zl\u00e1 hodnota {1} "},

    { ER_ATTRIB_VALUE_NOT_FOUND,
     "Hodnota atrib\u00fatu {0} nebola n\u00e1jden\u00e1 "},

    { ER_ATTRIB_VALUE_NOT_RECOGNIZED,
     "Hodnota atrib\u00fatu {0} nebola rozpoznan\u00e1 "},

    { ER_NULL_URI_NAMESPACE,
     "Pokus o vytvorenie prefixu n\u00e1zvov\u00e9ho priestoru s nulov\u00fdm URI"},

  //New ERROR keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    { ER_NUMBER_TOO_BIG,
     "Pokus o form\u00e1tovanie \u010d\u00edsla v\u00e4\u010d\u0161ieho, ne\u017e je najdlh\u0161\u00ed dlh\u00fd celo\u010d\u00edseln\u00fd typ"},

    { ER_CANNOT_FIND_SAX1_DRIVER,
     "Nie je mo\u017en\u00e9 n\u00e1js\u0165 triedu ovl\u00e1da\u010da SAX1 {0}"},

    { ER_SAX1_DRIVER_NOT_LOADED,
     "Trieda ovl\u00e1da\u010da SAX1 {0} bola n\u00e1jden\u00e1, ale nem\u00f4\u017ee by\u0165 zaveden\u00e1"},

    { ER_SAX1_DRIVER_NOT_INSTANTIATED,
     "Trieda ovl\u00e1da\u010da SAX1 {0} bola zaveden\u00e1, ale nem\u00f4\u017ee by\u0165 dolo\u017een\u00e1 pr\u00edkladom"},

    { ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER,
     "Trieda ovl\u00e1da\u010da SAX1 {0} neimplementuje org.xml.sax.Parser"},

    { ER_PARSER_PROPERTY_NOT_SPECIFIED,
     "Syst\u00e9mov\u00e1 vlastnos\u0165 org.xml.sax.parser nie je zadan\u00e1"},

    { ER_PARSER_ARG_CANNOT_BE_NULL,
     "Argument syntaktick\u00e9ho analyz\u00e1tora nesmie by\u0165 nulov\u00fd"},

    { ER_FEATURE,
     "Vlastnos\u0165: {0}"},

    { ER_PROPERTY,
     "Vlastn\u00edctvo: {0}"},

    { ER_NULL_ENTITY_RESOLVER,
     "Rozklada\u010d nulov\u00fdch ent\u00edt"},

    { ER_NULL_DTD_HANDLER,
     "Nulov\u00fd manipula\u010dn\u00fd program DTD"},

    { ER_NO_DRIVER_NAME_SPECIFIED,
     "Nie je zadan\u00fd \u017eiaden n\u00e1zov ovl\u00e1da\u010da!"},

    { ER_NO_URL_SPECIFIED,
     "Nie je zadan\u00e1 \u017eiadna URL!"},

    { ER_POOLSIZE_LESS_THAN_ONE,
     "Ve\u013ekos\u0165 oblasti je men\u0161ia ne\u017e 1!"},

    { ER_INVALID_DRIVER_NAME,
     "Je zadan\u00fd neplatn\u00fd n\u00e1zov ovl\u00e1da\u010da!"},

    { ER_ERRORLISTENER,
     "ErrorListener"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The name
//   'ElemTemplateElement' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_NO_TEMPLATE_PARENT,
     "Chyba program\u00e1tora! V\u00fdraz nem\u00e1 rodi\u010da ElemTemplateElement!"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The substitution text
//   provides further information in order to diagnose the problem.  The name
//   'RedundentExprEliminator' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR,
     "Tvrdenie program\u00e1tora v RedundentExprEliminator: {0}"},

    { ER_NOT_ALLOWED_IN_POSITION,
     "{0}nie je na tejto poz\u00edcii predlohy so \u0161t\u00fdlmi povolen\u00e9!"},

    { ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION,
     "Text bez medzier nie je povolen\u00fd na tejto poz\u00edcii predlohy so \u0161t\u00fdlmi!"},

  // This code is shared with warning codes.
  // SystemId Unknown
    { INVALID_TCHAR,
     "Neplatn\u00e1 hodnota: {1} pou\u017e\u00edvan\u00fd pre atrib\u00fat CHAR: {0}.  Atrib\u00fat typu CHAR mus\u00ed by\u0165 len 1 znak!"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "QNAME" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value and {0} is the attribute name.
    //The following codes are shared with the warning codes...
    { INVALID_QNAME,
     "Neplatn\u00e1 hodnota: {1} pou\u017e\u00edvan\u00e1 pre atrib\u00fat QNAME: {0}"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "ENUM" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value, {0} is the attribute name, and {2} is a list of valid
    // values.
    { INVALID_ENUM,
     "Neplatn\u00e1 hodnota: {1} pou\u017e\u00edvan\u00e1 pre atrib\u00fat ENUM: {0}.  Platn\u00e9 hodnoty s\u00fa: {2}."},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NMTOKEN" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NMTOKEN,
     "Neplatn\u00e1 hodnota: {1} pou\u017e\u00edvan\u00e1 pre atrib\u00fat NMTOKEN:{0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NCNAME" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NCNAME,
     "Neplatn\u00e1 hodnota: {1} pou\u017e\u00edvan\u00e1 pre atrib\u00fat NCNAME: {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "boolean" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_BOOLEAN,
     "Neplatn\u00e1 hodnota: {1} pou\u017e\u00edvan\u00e1 pre boolovsk\u00fd atrib\u00fat: {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "number" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
     { INVALID_NUMBER,
     "Neplatn\u00e1 hodnota: {1} pou\u017e\u00edvan\u00e1 pre atrib\u00fat \u010d\u00edsla: {0} "},


  // End of shared codes...

// Note to translators:  A "match pattern" is a special form of XPath expression
// that is used for matching patterns.  The substitution text is the name of
// a function.  The message indicates that when this function is referenced in
// a match pattern, its argument must be a string literal (or constant.)
// ER_ARG_LITERAL - new error message for bugzilla //5202
    { ER_ARG_LITERAL,
     "Argument pre {0} v zhodnom vzore mus\u00ed by\u0165 liter\u00e1lom."},

// Note to translators:  The following message indicates that two definitions of
// a variable.  A "global variable" is a variable that is accessible everywher
// in the stylesheet.
// ER_DUPLICATE_GLOBAL_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_GLOBAL_VAR,
     "Duplicitn\u00e1 deklar\u00e1cia glob\u00e1lnej premennej."},


// Note to translators:  The following message indicates that two definitions of
// a variable were encountered.
// ER_DUPLICATE_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_VAR,
     "Duplicitn\u00e1 deklar\u00e1cia premennej."},

    // Note to translators:  "xsl:template, "name" and "match" are XSLT keywords
    // which must not be translated.
    // ER_TEMPLATE_NAME_MATCH - new error message for bugzilla #789
    { ER_TEMPLATE_NAME_MATCH,
     "xsl:template mus\u00ed ma\u0165 n\u00e1zov alebo atrib\u00fat zhody (alebo oboje)"},

    // Note to translators:  "exclude-result-prefixes" is an XSLT keyword which
    // should not be translated.  The message indicates that a namespace prefix
    // encountered as part of the value of the exclude-result-prefixes attribute
    // was in error.
    // ER_INVALID_PREFIX - new error message for bugzilla #788
    { ER_INVALID_PREFIX,
     "Predpona v exclude-result-prefixes je neplatn\u00e1: {0}"},

    // Note to translators:  An "attribute set" is a set of attributes that can
    // be added to an element in the output document as a group.  The message
    // indicates that there was a reference to an attribute set named {0} that
    // was never defined.
    // ER_NO_ATTRIB_SET - new error message for bugzilla #782
    { ER_NO_ATTRIB_SET,
     "pomenovan\u00e1 sada atrib\u00fatov {0} neexistuje"},

    // Note to translators:  This message indicates that there was a reference
    // to a function named {0} for which no function definition could be found.
    { ER_FUNCTION_NOT_FOUND,
     "Funkcia s n\u00e1zvom {0} neexistuje."},

    // Note to translators:  This message indicates that the XSLT instruction
    // that is named by the substitution text {0} must not contain other XSLT
    // instructions (content) or a "select" attribute.  The word "select" is
    // an XSLT keyword in this case and must not be translated.
    { ER_CANT_HAVE_CONTENT_AND_SELECT,
     "Prvok {0} nesmie ma\u0165 aj atrib\u00fat content aj atrib\u00fat select."},

    // Note to translators:  This message indicates that the value argument
    // of setParameter must be a valid Java Object.
    { ER_INVALID_SET_PARAM_VALUE,
     "Hodnota parametra {0} mus\u00ed by\u0165 platn\u00fdm objektom jazyka Java."},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT,
         "Atrib\u00fat result-prefix prvku xsl:namespace-alias m\u00e1 hodnotu '#default', ale v rozsahu pre prvok neexistuje \u017eiadna deklar\u00e1cia \u0161tandardn\u00e9ho n\u00e1zvov\u00e9ho priestoru"},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX,
         "Atrib\u00fat result-prefix prvku xsl:namespace-alias m\u00e1 hodnotu ''{0}'', ale v rozsahu pre prvok neexistuje \u017eiadna deklar\u00e1cia n\u00e1zvov\u00e9ho priestoru pre predponu ''{0}''."},

    { ER_SET_FEATURE_NULL_NAME,
      "V TransformerFactory.setFeature(N\u00e1zov re\u0165azca, boolovsk\u00e1 hodnota)nem\u00f4\u017ee ma\u0165 funkcia n\u00e1zov null."},

    { ER_GET_FEATURE_NULL_NAME,
      "N\u00e1zov vlastnosti nem\u00f4\u017ee by\u0165 null v TransformerFactory.getFeature(N\u00e1zov re\u0165azca)."},

    { ER_UNSUPPORTED_FEATURE,
      "V tomto TransformerFactory sa ned\u00e1 nastavi\u0165 vlastnos\u0165 ''{0}''."},

    { ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING,
        "Pou\u017e\u00edvanie prvku roz\u0161\u00edrenia ''{0}'' nie je povolen\u00e9, ke\u010f je funkcia bezpe\u010dn\u00e9ho spracovania nastaven\u00e1 na hodnotu true."},

        { ER_NAMESPACE_CONTEXT_NULL_NAMESPACE,
          "Ned\u00e1 sa z\u00edska\u0165 predpona pre null n\u00e1zvov\u00fd priestor uri."},

        { ER_NAMESPACE_CONTEXT_NULL_PREFIX,
          "Ned\u00e1 sa z\u00edska\u0165 n\u00e1zvov\u00fd priestor uri pre predponu null."},

        { ER_XPATH_RESOLVER_NULL_QNAME,
          "N\u00e1zov funkcie nem\u00f4\u017ee by\u0165 null."},

        { ER_XPATH_RESOLVER_NEGATIVE_ARITY,
          "Arita nem\u00f4\u017ee by\u0165 z\u00e1porn\u00e1."},

  // Warnings...

    { WG_FOUND_CURLYBRACE,
      "Bol n\u00e1jden\u00fd znak '}', ale nie otvoren\u00fd \u017eiaden vzor atrib\u00fatu!"},

    { WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR,
      "Upozornenie: atrib\u00fat po\u010dtu sa nezhoduje s predchodcom v xsl:number! Cie\u013e = {0}"},

    { WG_EXPR_ATTRIB_CHANGED_TO_SELECT,
      "Star\u00e1 syntax: N\u00e1zov atrib\u00fatu 'expr' bol zmenen\u00fd na 'select'."},

    { WG_NO_LOCALE_IN_FORMATNUMBER,
      "Xalan zatia\u013e nespracov\u00e1va n\u00e1zov umiestnenia vo funkcii format-number."},

    { WG_LOCALE_NOT_FOUND,
      "Upozornenie: Nebolo mo\u017en\u00e9 n\u00e1js\u0165 lok\u00e1l pre xml:lang={0}"},

    { WG_CANNOT_MAKE_URL_FROM,
      "Nie je mo\u017en\u00e9 vytvori\u0165 URL z: {0}"},

    { WG_CANNOT_LOAD_REQUESTED_DOC,
      "Nie je mo\u017en\u00e9 zavies\u0165 po\u017eadovan\u00fd doc: {0}"},

    { WG_CANNOT_FIND_COLLATOR,
      "Nebolo mo\u017en\u00e9 n\u00e1js\u0165 porovn\u00e1va\u010d pre <sort xml:lang={0}"},

    { WG_FUNCTIONS_SHOULD_USE_URL,
      "Star\u00e1 syntax: in\u0161trukcia funkci\u00ed by mala pou\u017e\u00edva\u0165 url {0}"},

    { WG_ENCODING_NOT_SUPPORTED_USING_UTF8,
      "K\u00f3dovanie nie je podporovan\u00e9: {0}, pou\u017e\u00edva UTF-8"},

    { WG_ENCODING_NOT_SUPPORTED_USING_JAVA,
      "K\u00f3dovanie nie je podporovan\u00e9: {0}, pou\u017e\u00edva Java {1}"},

    { WG_SPECIFICITY_CONFLICTS,
      "Boli zisten\u00e9 konflikty \u0161pecifickosti: {0} naposledy n\u00e1jden\u00e1 v \u0161t\u00fdle dokumentu bude pou\u017eit\u00e1."},

    { WG_PARSING_AND_PREPARING,
      "========= Anal\u00fdza a pr\u00edprava {0} =========="},

    { WG_ATTR_TEMPLATE,
     "Attr vzor, {0}"},

    { WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE,
      "Konflikt zhodnosti medzi xsl:strip-space a xsl:preserve-space"},

    { WG_ATTRIB_NOT_HANDLED,
      "Xalan zatia\u013e nesprac\u00fava atrib\u00fat {0}!"},

    { WG_NO_DECIMALFORMAT_DECLARATION,
      "Pre desiatkov\u00fd form\u00e1t sa nena\u0161la \u017eiadna deklar\u00e1cia: {0}"},

    { WG_OLD_XSLT_NS,
     "Ch\u00fdbaj\u00faci, alebo nespr\u00e1vny n\u00e1zvov\u00fd priestor XSLT. "},

    { WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED,
      "Povolen\u00e1 je len jedna \u0161tandardn\u00e1 deklar\u00e1cia xsl:decimal-format."},

    { WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE,
      "N\u00e1zvy xsl:decimal-format musia by\u0165 jedine\u010dn\u00e9. N\u00e1zov \"{0}\" bol zopakovan\u00fd."},

    { WG_ILLEGAL_ATTRIBUTE,
      "{0} m\u00e1 neplatn\u00fd atrib\u00fat: {1}"},

    { WG_COULD_NOT_RESOLVE_PREFIX,
      "Nebolo mo\u017en\u00e9 rozl\u00ed\u0161i\u0165 predponu n\u00e1zvov\u00e9ho priestoru: {0}. Uzol bude ignorovan\u00fd."},

    { WG_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet si vy\u017eaduje atrib\u00fat 'version'!"},

    { WG_ILLEGAL_ATTRIBUTE_NAME,
      "Neplatn\u00fd n\u00e1zov atrib\u00fatu: {0}"},

    { WG_ILLEGAL_ATTRIBUTE_VALUE,
      "Neplatn\u00e1 hodnota pou\u017e\u00edvan\u00e1 pre atrib\u00fat {0}: {1}"},

    { WG_EMPTY_SECOND_ARG,
      "V\u00fdsledn\u00fd nodeset z druh\u00e9ho argumentu funkcie dokumentu je pr\u00e1zdny. Vr\u00e1\u0165te pr\u00e1zdnu mno\u017einu uzlov."},

  //Following are the new WARNING keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.
    { WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "Hodnota atrib\u00fatu 'name' n\u00e1zvu xsl:processing-instruction nesmie by\u0165 'xml'"},

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.  "NCName" is an XML data-type and must not be
    // translated.
    { WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "Hodnota atrib\u00fatu ''name'' xsl:processing-instruction mus\u00ed by\u0165 platn\u00fdm NCName: {0}"},

    // Note to translators:  This message is reported if the stylesheet that is
    // being processed attempted to construct an XML document with an attribute in a
    // place other than on an element.  The substitution text specifies the name of
    // the attribute.
    { WG_ILLEGAL_ATTRIBUTE_POSITION,
      "Nie je mo\u017en\u00e9 prida\u0165 atrib\u00fat {0} po uzloch potomka alebo pred vytvoren\u00edm elementu.  Atrib\u00fat bude ignorovan\u00fd."},

    { NO_MODIFICATION_ALLOWED_ERR,
      "Prebieha pokus o \u00fapravu objektu, pre ktor\u00fd nie s\u00fa povolen\u00e9 \u00fapravy."
    },

    //Check: WHY THERE IS A GAP B/W NUMBERS in the XSLTErrorResources properties file?

  // Other miscellaneous text used inside the code...
  { "ui_language", "en"},
  {  "help_language",  "en" },
  {  "language",  "en" },
  { "BAD_CODE", "Parameter na createMessage bol mimo ohrani\u010denia"},
  {  "FORMAT_FAILED", "V\u00fdnimka po\u010das volania messageFormat"},
  {  "version", ">>>>>>> Verzia Xalan "},
  {  "version2",  "<<<<<<<"},
  {  "yes", "\u00e1no"},
  { "line", "Riadok #"},
  { "column","St\u013apec #"},
  { "xsldone", "XSLProcessor: vykonan\u00e9"},


  // Note to translators:  The following messages provide usage information
  // for the Xalan Process command line.  "Process" is the name of a Java class,
  // and should not be translated.
  { "xslProc_option", "Vo\u013eby triedy procesu pr\u00edkazov\u00e9ho riadka Xalan-J:"},
  { "xslProc_option", "Vo\u013eby triedy Process pr\u00edkazov\u00e9ho riadka Xalan-J\u003a"},
  { "xslProc_invalid_xsltc_option", "Vo\u013eba {0} nie je podporovan\u00e1 v re\u017eime XSLTC."},
  { "xslProc_invalid_xalan_option", "Vo\u013ebu {0} mo\u017eno pou\u017ei\u0165 len spolu s -XSLTC."},
  { "xslProc_no_input", "Chyba: nie je uveden\u00fd \u017eiadny \u0161t\u00fdl dokumentu, ani vstupn\u00fd xml. Spustite tento pr\u00edkaz bez akejko\u013evek vo\u013eby pre in\u0161trukcie pou\u017eitia."},
  { "xslProc_common_options", "-Be\u017en\u00e9 vo\u013eby-"},
  { "xslProc_xalan_options", "-Vo\u013eby pre Xalan-"},
  { "xslProc_xsltc_options", "-Vo\u013eby pre XSLTC-"},
  { "xslProc_return_to_continue", "(stla\u010dte <return> a pokra\u010dujte)"},

   // Note to translators: The option name and the parameter name do not need to
   // be translated. Only translate the messages in parentheses.  Note also that
   // leading whitespace in the messages is used to indent the usage information
   // for each option in the English messages.
   // Do not translate the keywords: XSLTC, SAX, DOM and DTM.
  { "optionXSLTC", "   [-XSLTC (pou\u017eite XSLTC na transform\u00e1ciu)]"},
  { "optionIN", "   [-IN inputXMLURL]"},
  { "optionXSL", "   [-XSL XSLTransformationURL]"},
  { "optionOUT",  "   [-OUT outputFileName]"},
  { "optionLXCIN", "   [-LXCIN compiledStylesheetFileNameIn]"},
  { "optionLXCOUT", "   [-LXCOUT compiledStylesheetFileNameOutOut]"},
  { "optionPARSER", "   [-PARSER plne kvalifikovan\u00fd n\u00e1zov triedy sprostredkovate\u013ea syntaktick\u00e9ho analyz\u00e1tora]"},
  {  "optionE", "   [-E (Nerozvinie odkazy na entity)]"},
  {  "optionV",  "   [-E (Nerozvinie odkazy na entity)]"},
  {  "optionQC", "   [-QC (Varovania pri konfliktoch Quiet Pattern)]"},
  {  "optionQ", "   [-Q  (Tich\u00fd re\u017eim)]"},
  {  "optionLF", "   [-LF (Znaky pre posun riadka pou\u017ei\u0165 len vo v\u00fdstupe {default is CR/LF})]"},
  {  "optionCR", "   [-CR (Znaky n\u00e1vratu voz\u00edka pou\u017ei\u0165 len vo v\u00fdstupe {default is CR/LF})]"},
  { "optionESCAPE", "   [-ESCAPE (Ktor\u00e9 znaky maj\u00fa ma\u0165 zmenen\u00fd v\u00fdznam {default is <>&\"\'\\r\\n}]"},
  { "optionINDENT", "   [-INDENT (Riadi po\u010det medzier odsadenia {default is 0})]"},
  { "optionTT", "   [-TT (Sledovanie, ako s\u00fa volan\u00e9 vzory.)]"},
  { "optionTG", "   [-TG (Sledovanie udalost\u00ed ka\u017edej gener\u00e1cie.)]"},
  { "optionTS", "   [-TS (Sledovanie udalost\u00ed ka\u017ed\u00e9ho v\u00fdberu.)]"},
  {  "optionTTC", "   [-TTC (Sledovanie ako s\u00fa vytv\u00e1ran\u00ed potomkovia vzorov.)]"},
  { "optionTCLASS", "   [-TCLASS (Trieda TraceListener pre pr\u00edpony sledovania.)]"},
  { "optionVALIDATE", "   [-VALIDATE (Ur\u010duje, \u010di m\u00e1 doch\u00e1dza\u0165 k overovaniu.  Overovanie je \u0161tandardne vypnut\u00e9.)]"},
  { "optionEDUMP", "   [-EDUMP {optional filename} (Vytvori\u0165 v\u00fdpis z\u00e1sobn\u00edka pri chybe.)]"},
  {  "optionXML", "   [-XML (Pou\u017eije form\u00e1tor XML a prid\u00e1 hlavi\u010dku XML.)]"},
  {  "optionTEXT", "   [-TEXT (Jednoduch\u00fd textov\u00fd form\u00e1tor.)]"},
  {  "optionHTML", "   [-HTML (Pou\u017eije form\u00e1tor HTML.)]"},
  {  "optionPARAM", "   [-PARAM vyjadrenie n\u00e1zvu (nastav\u00ed parameter \u0161t\u00fdlu dokumentu)]"},
  {  "noParsermsg1", "Proces XSL nebol \u00faspe\u0161n\u00fd."},
  {  "noParsermsg2", "** Nebolo mo\u017en\u00e9 n\u00e1js\u0165 syntaktick\u00fd analyz\u00e1tor **"},
  { "noParsermsg3",  "Skontroluje, pros\u00edm, svoju classpath."},
  { "noParsermsg4", "Ak nem\u00e1te Syntaktick\u00fd analyz\u00e1tor XML pre jazyk Java od firmy IBM, m\u00f4\u017eete si ho stiahnu\u0165 z"},
  { "noParsermsg5", "IBM's AlphaWorks: http://www.alphaworks.ibm.com/formula/xml"},
  { "optionURIRESOLVER", "   [-URIRESOLVER pln\u00fd n\u00e1zov triedy (URIResolver bude pou\u017eit\u00fd na ur\u010dovanie URI)]"},
  { "optionENTITYRESOLVER",  "   [-ENTITYRESOLVER pln\u00fd n\u00e1zov triedy (EntityResolver bude pou\u017eit\u00fd na ur\u010denie ent\u00edt)]"},
  { "optionCONTENTHANDLER",  "   [-CONTENTHANDLER pln\u00fd n\u00e1zov triedy (ContentHandler bude pou\u017eit\u00fd na serializ\u00e1ciu v\u00fdstupu)]"},
  {  "optionLINENUMBERS",  "   [-L pou\u017eije \u010d\u00edsla riadkov pre zdrojov\u00fd dokument]"},
  { "optionSECUREPROCESSING", "   [-SECURE (nastav\u00ed funkciu bezpe\u010dn\u00e9ho spracovania na hodnotu true.)]"},

    // Following are the new options added in XSLTErrorResources.properties files after Jdk 1.4 (Xalan 2.2-D11)


  {  "optionMEDIA",  "   [-MEDIA mediaType (pou\u017ei\u0165 atrib\u00fat m\u00e9dia na n\u00e1jdenie \u0161t\u00fdlu h\u00e1rka, priraden\u00e9ho k dokumentu.)]"},
  {  "optionFLAVOR",  "   [-FLAVOR flavorName (Explicitne pou\u017ei\u0165 s2s=SAX alebo d2d=DOM na vykonanie transform\u00e1cie.)] "}, // Added by sboag/scurcuru; experimental
  { "optionDIAG", "   [-DIAG (Vytla\u010di\u0165 celkov\u00fd \u010das transform\u00e1cie v milisekund\u00e1ch.)]"},
  { "optionINCREMENTAL",  "   [-INCREMENTAL (\u017eiados\u0165 o inkrement\u00e1lnu kon\u0161trukciu DTM nastaven\u00edm http://xml.apache.org/xalan/features/incremental true.)]"},
  {  "optionNOOPTIMIMIZE",  "   [-NOOPTIMIMIZE (po\u017eiadavka na nesprac\u00favanie optimaliz\u00e1cie defin\u00edcie \u0161t\u00fdlov nastaven\u00edm http://xml.apache.org/xalan/features/optimize na hodnotu false.)]"},
  { "optionRL",  "   [-RL recursionlimit (nastavi\u0165 \u010d\u00edseln\u00fd limit pre h\u013abku rekurzie \u0161t\u00fdlov h\u00e1rkov.)]"},
  {   "optionXO",  "   [-XO [transletName] (prira\u010fuje n\u00e1zov ku generovan\u00e9mu transletu)]"},
  {  "optionXD", "   [-XD destinationDirectory (uv\u00e1dza cie\u013eov\u00fd adres\u00e1r pre translet)]"},
  {  "optionXJ",  "   [-XJ jarfile (pakuje triedy transletu do s\u00faboru jar s n\u00e1zvom <jarfile>)]"},
  {   "optionXP",  "   [-XP package (uv\u00e1dza predponu n\u00e1zvu bal\u00edka pre v\u0161etky generovan\u00e9 triedy transletu)]"},

  //AddITIONAL  STRINGS that need L10n
  // Note to translators:  The following message describes usage of a particular
  // command-line option that is used to enable the "template inlining"
  // optimization.  The optimization involves making a copy of the code
  // generated for a template in another template that refers to it.
  { "optionXN",  "   [-XN (povo\u013euje zoradenie vzorov do riadka)]" },
  { "optionXX",  "   [-XX (zap\u00edna \u010fal\u0161\u00ed v\u00fdstup spr\u00e1v ladenia)]"},
  { "optionXT" , "   [-XT (ak je to mo\u017en\u00e9, pou\u017eite translet na transform\u00e1ciu)]"},
  { "diagTiming"," --------- Transform\u00e1cia z {0} cez {1} trvala {2} ms" },
  { "recursionTooDeep","Vnorenie vzoru je pr\u00edli\u0161 hlbok\u00e9. vnorenie = {0}, vzor {1} {2}" },
  { "nameIs", "n\u00e1zov je" },
  { "matchPatternIs", "vzor zhody je" }

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
  public static final String ERROR_HEADER = "Chyba: ";

  /** String to prepend to warning messages.    */
  public static final String WARNING_HEADER = "Upozornenie: ";

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
