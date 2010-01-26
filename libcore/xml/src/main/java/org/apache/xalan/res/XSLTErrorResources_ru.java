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
 * $Id: XSLTErrorResources_ru.java 468641 2006-10-28 06:54:42Z minchau $
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
public class XSLTErrorResources_ru extends ListResourceBundle
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
      "\u041e\u0448\u0438\u0431\u043a\u0430: \u0421\u043a\u043e\u0431\u043a\u0430 '{' \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u0430 \u0432 \u0432\u044b\u0440\u0430\u0436\u0435\u043d\u0438\u0438"},

    { ER_ILLEGAL_ATTRIBUTE ,
     "\u0414\u043b\u044f {0} \u0443\u043a\u0430\u0437\u0430\u043d \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0439 \u0430\u0442\u0440\u0438\u0431\u0443\u0442: {1}"},

  {ER_NULL_SOURCENODE_APPLYIMPORTS ,
      "\u041f\u0443\u0441\u0442\u043e\u0439 sourceNode \u0432 xsl:apply-imports!"},

  {ER_CANNOT_ADD,
      "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c {0} \u0432 {1}"},

    { ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES,
      "\u041f\u0443\u0441\u0442\u043e\u0439 sourceNode \u0432 handleApplyTemplatesInstruction!"},

    { ER_NO_NAME_ATTRIB,
     "\u0423 {0} \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 name"},

    {ER_TEMPLATE_NOT_FOUND,
     "\u0423\u043a\u0430\u0437\u0430\u043d\u043d\u044b\u0439 \u0448\u0430\u0431\u043b\u043e\u043d \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: {0}"},

    {ER_CANT_RESOLVE_NAME_AVT,
      "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u0442\u044c \u0438\u043c\u044f AVT \u0432 xsl:call-template."},

    {ER_REQUIRES_ATTRIB,
     "\u0414\u043b\u044f {0} \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0443\u043a\u0430\u0437\u0430\u043d \u0430\u0442\u0440\u0438\u0431\u0443\u0442: {1}"},

    { ER_MUST_HAVE_TEST_ATTRIB,
      "{0} \u0434\u043e\u043b\u0436\u0435\u043d \u0441\u043e\u0434\u0435\u0440\u0436\u0430\u0442\u044c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 ''test''. "},

    {ER_BAD_VAL_ON_LEVEL_ATTRIB,
      "\u041d\u0435\u0432\u0435\u0440\u043d\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 \u0443\u0440\u043e\u0432\u043d\u044f: {0}"},

    {ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "\u0418\u043c\u044f processing-instruction \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u0440\u0430\u0432\u043d\u043e 'xml'"},

    { ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "\u0418\u043c\u044f processing-instruction \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u043c \u0438\u043c\u0435\u043d\u0435\u043c NCName: {0}"},

    { ER_NEED_MATCH_ATTRIB,
      "\u0415\u0441\u043b\u0438 \u0434\u043b\u044f {0} \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d \u0440\u0435\u0436\u0438\u043c, \u0442\u043e \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 match."},

    { ER_NEED_NAME_OR_MATCH_ATTRIB,
      "\u0423 {0} \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 name \u0438\u043b\u0438 match."},

    {ER_CANT_RESOLVE_NSPREFIX,
      "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u0442\u044c \u043f\u0440\u0435\u0444\u0438\u043a\u0441 \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0430 \u0438\u043c\u0435\u043d: {0}"},

    { ER_ILLEGAL_VALUE,
     "\u0412 xml:space \u0443\u043a\u0430\u0437\u0430\u043d\u043e \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435: {0}"},

    { ER_NO_OWNERDOC,
      "\u0423 \u0434\u043e\u0447\u0435\u0440\u043d\u0435\u0433\u043e \u0443\u0437\u043b\u0430 \u043d\u0435\u0442 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u0430-\u0432\u043b\u0430\u0434\u0435\u043b\u044c\u0446\u0430!"},

    { ER_ELEMTEMPLATEELEM_ERR,
     "\u041e\u0448\u0438\u0431\u043a\u0430 ElemTemplateElement: {0}"},

    { ER_NULL_CHILD,
     "\u041f\u043e\u043f\u044b\u0442\u043a\u0430 \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043f\u0443\u0441\u0442\u043e\u0433\u043e \u043f\u043e\u0442\u043e\u043c\u043a\u0430!"},

    { ER_NEED_SELECT_ATTRIB,
     "\u0423 {0} \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 select."},

    { ER_NEED_TEST_ATTRIB ,
      "\u0414\u043b\u044f xsl:when \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0437\u0430\u0434\u0430\u043d \u0430\u0442\u0440\u0438\u0431\u0443\u0442 'test'."},

    { ER_NEED_NAME_ATTRIB,
      "\u0414\u043b\u044f xsl:with-param \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0437\u0430\u0434\u0430\u043d \u0430\u0442\u0440\u0438\u0431\u0443\u0442 'name'."},

    { ER_NO_CONTEXT_OWNERDOC,
      "\u0412 \u043a\u043e\u043d\u0442\u0435\u043a\u0441\u0442\u0435 \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442-\u0432\u043b\u0430\u0434\u0435\u043b\u0435\u0446!"},

    {ER_COULD_NOT_CREATE_XML_PROC_LIAISON,
      "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0441\u043e\u0437\u0434\u0430\u0442\u044c XML TransformerFactory Liaison: {0}"},

    {ER_PROCESS_NOT_SUCCESSFUL,
      "Xalan: \u0412 \u043f\u0440\u043e\u0446\u0435\u0441\u0441\u0435 \u043e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d\u044b \u043e\u0448\u0438\u0431\u043a\u0438."},

    { ER_NOT_SUCCESSFUL,
     "Xalan: \u041e\u0448\u0438\u0431\u043a\u0430."},

    { ER_ENCODING_NOT_SUPPORTED,
     "\u041a\u043e\u0434\u0438\u0440\u043e\u0432\u043a\u0430 \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f: {0}"},

    {ER_COULD_NOT_CREATE_TRACELISTENER,
      "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0441\u043e\u0437\u0434\u0430\u0442\u044c TraceListener: {0}"},

    {ER_KEY_REQUIRES_NAME_ATTRIB,
      "\u0414\u043b\u044f xsl:key \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 'name'!"},

    { ER_KEY_REQUIRES_MATCH_ATTRIB,
      "\u0414\u043b\u044f xsl:key \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 'match'!"},

    { ER_KEY_REQUIRES_USE_ATTRIB,
      "\u0414\u043b\u044f xsl:key \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 'use'!"},

    { ER_REQUIRES_ELEMENTS_ATTRIB,
      "\u0414\u043b\u044f (StylesheetHandler) {0} \u0442\u0440\u0435\u0431\u0443\u0435\u0442\u0441\u044f \u0430\u0442\u0440\u0438\u0431\u0443\u0442 ''elements''!"},

    { ER_MISSING_PREFIX_ATTRIB,
      "\u0414\u043b\u044f (StylesheetHandler) {0} \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0430\u0442\u0440\u0438\u0431\u0443\u0442 ''prefix''"},

    { ER_BAD_STYLESHEET_URL,
     "\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 URL \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439: {0}"},

    { ER_FILE_NOT_FOUND,
     "\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0444\u0430\u0439\u043b \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439: {0}"},

    { ER_IOEXCEPTION,
      "\u0418\u0441\u043a\u043b\u044e\u0447\u0438\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u0441\u0438\u0442\u0443\u0430\u0446\u0438\u044f \u0432\u0432\u043e\u0434\u0430-\u0432\u044b\u0432\u043e\u0434\u0430 \u043f\u0440\u0438 \u043e\u0431\u0440\u0430\u0449\u0435\u043d\u0438\u0438 \u043a \u0444\u0430\u0439\u043b\u0443 \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439: {0}"},

    { ER_NO_HREF_ATTRIB,
      "(StylesheetHandler) \u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0430\u0442\u0440\u0438\u0431\u0443\u0442 href \u0434\u043b\u044f {0}"},

    { ER_STYLESHEET_INCLUDES_ITSELF,
      "(StylesheetHandler) {0} \u043d\u0435\u043f\u043e\u0441\u0440\u0435\u0434\u0441\u0442\u0432\u0435\u043d\u043d\u043e \u0438\u043b\u0438 \u043a\u043e\u0441\u0432\u0435\u043d\u043d\u043e \u0441\u0441\u044b\u043b\u0430\u0435\u0442\u0441\u044f \u043d\u0430 \u0441\u0435\u0431\u044f!"},

    { ER_PROCESSINCLUDE_ERROR,
      "\u041e\u0448\u0438\u0431\u043a\u0430 StylesheetHandler.processInclude, {0}"},

    { ER_MISSING_LANG_ATTRIB,
      "\u0414\u043b\u044f (StylesheetHandler) {0} \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0430\u0442\u0440\u0438\u0431\u0443\u0442 ''lang''"},

    { ER_MISSING_CONTAINER_ELEMENT_COMPONENT,
      "(StylesheetHandler) \u041d\u0435\u0432\u0435\u0440\u043d\u043e\u0435 \u043f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 {0} ?? \u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u044d\u043b\u0435\u043c\u0435\u043d\u0442-\u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440 ''component''"},

    { ER_CAN_ONLY_OUTPUT_TO_ELEMENT,
      "\u0412\u044b\u0432\u043e\u0434 \u0432\u043e\u0437\u043c\u043e\u0436\u0435\u043d \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0435 \u043e\u0431\u044a\u0435\u043a\u0442\u044b: Element, DocumentFragment, Document \u0438\u043b\u0438 PrintWriter."},

    { ER_PROCESS_ERROR,
     "\u041e\u0448\u0438\u0431\u043a\u0430 StylesheetRoot.process"},

    { ER_UNIMPLNODE_ERROR,
     "\u041e\u0448\u0438\u0431\u043a\u0430 UnImplNode: {0}"},

    { ER_NO_SELECT_EXPRESSION,
      "\u041e\u0448\u0438\u0431\u043a\u0430! \u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e \u0432\u044b\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u0432\u044b\u0431\u043e\u0440\u0430 xpath (-select)."},

    { ER_CANNOT_SERIALIZE_XSLPROCESSOR,
      "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u0435\u0440\u0438\u0430\u043b\u0438\u0437\u043e\u0432\u0430\u0442\u044c XSLProcessor!"},

    { ER_NO_INPUT_STYLESHEET,
      "\u041d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u0430 \u0438\u0441\u0445\u043e\u0434\u043d\u0430\u044f \u0442\u0430\u0431\u043b\u0438\u0446\u0430 \u0441\u0442\u0438\u043b\u0435\u0439!"},

    { ER_FAILED_PROCESS_STYLESHEET,
      "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u043e\u0431\u0440\u0430\u0431\u043e\u0442\u043a\u0435 \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439!"},

    { ER_COULDNT_PARSE_DOC,
     "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0430\u043d\u0430\u043b\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442 {0} !"},

    { ER_COULDNT_FIND_FRAGMENT,
     "\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0444\u0440\u0430\u0433\u043c\u0435\u043d\u0442: {0}"},

    { ER_NODE_NOT_ELEMENT,
      "\u0423\u0437\u0435\u043b, \u043d\u0430 \u043a\u043e\u0442\u043e\u0440\u044b\u0439 \u0441\u0441\u044b\u043b\u0430\u0435\u0442\u0441\u044f \u0438\u0434\u0435\u043d\u0442\u0438\u0444\u0438\u043a\u0430\u0442\u043e\u0440 \u0444\u0440\u0430\u0433\u043c\u0435\u043d\u0442\u0430, \u043d\u0435 \u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u043c: {0}"},

    { ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB,
      "\u0423 for-each \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 match \u0438\u043b\u0438 name"},

    { ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB,
      "\u0423 templates \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 match \u0438\u043b\u0438 name"},

    { ER_NO_CLONE_OF_DOCUMENT_FRAG,
      "\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u043a\u043e\u043f\u0438\u044f \u0444\u0440\u0430\u0433\u043c\u0435\u043d\u0442\u0430 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u0430!"},

    { ER_CANT_CREATE_ITEM,
      "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u044d\u043b\u0435\u043c\u0435\u043d\u0442 \u0434\u0435\u0440\u0435\u0432\u0430 \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u043e\u0432: {0}"},

    { ER_XMLSPACE_ILLEGAL_VALUE,
      "\u0417\u0430\u0434\u0430\u043d\u043e \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 xml:space \u0432 \u0438\u0441\u0445\u043e\u0434\u043d\u043e\u043c XML: {0}"},

    { ER_NO_XSLKEY_DECLARATION,
      "\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u043e\u0431\u044a\u044f\u0432\u043b\u0435\u043d\u0438\u0435 xsl:key \u0434\u043b\u044f {0}!"},

    { ER_CANT_CREATE_URL,
     "\u041e\u0448\u0438\u0431\u043a\u0430! \u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c URL \u0434\u043b\u044f {0}"},

    { ER_XSLFUNCTIONS_UNSUPPORTED,
     "xsl:functions \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f"},

    { ER_PROCESSOR_ERROR,
     "\u041e\u0448\u0438\u0431\u043a\u0430 XSLT TransformerFactory"},

    { ER_NOT_ALLOWED_INSIDE_STYLESHEET,
      "(StylesheetHandler) {0} \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c \u0432 \u0442\u0430\u0431\u043b\u0438\u0446\u0435 \u0441\u0442\u0438\u043b\u0435\u0439!"},

    { ER_RESULTNS_NOT_SUPPORTED,
      "result-ns \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f!  \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 xsl:output."},

    { ER_DEFAULTSPACE_NOT_SUPPORTED,
      "default-space \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f!  \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 xsl:strip-space \u0438\u043b\u0438 xsl:preserve-space."},

    { ER_INDENTRESULT_NOT_SUPPORTED,
      "indent-result \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f!  \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 xsl:output."},

    { ER_ILLEGAL_ATTRIB,
      "(StylesheetHandler) \u0414\u043b\u044f {0} \u0443\u043a\u0430\u0437\u0430\u043d \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0439 \u0430\u0442\u0440\u0438\u0431\u0443\u0442: {1}"},

    { ER_UNKNOWN_XSL_ELEM,
     "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u044b\u0439 \u044d\u043b\u0435\u043c\u0435\u043d\u0442 XSL: {0}"},

    { ER_BAD_XSLSORT_USE,
      "(StylesheetHandler) xsl:sort \u043c\u043e\u0436\u0435\u0442 \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u0441 xsl:apply-templates \u0438\u043b\u0438 xsl:for-each."},

    { ER_MISPLACED_XSLWHEN,
      "(StylesheetHandler) \u041d\u0435\u0432\u0435\u0440\u043d\u043e\u0435 \u043f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435 xsl:when!"},

    { ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:when \u043d\u0435 \u0438\u043c\u0435\u0435\u0442 \u043f\u0440\u0435\u0434\u0448\u0435\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e xsl:choose!"},

    { ER_MISPLACED_XSLOTHERWISE,
      "(StylesheetHandler) \u041d\u0435\u0432\u0435\u0440\u043d\u043e\u0435 \u043f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435 xsl:otherwise!"},

    { ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:otherwise \u043d\u0435 \u0438\u043c\u0435\u0435\u0442 \u043f\u0440\u0435\u0434\u0448\u0435\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e xsl:choose!"},

    { ER_NOT_ALLOWED_INSIDE_TEMPLATE,
      "(StylesheetHandler) {0} \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c \u0432 \u0448\u0430\u0431\u043b\u043e\u043d\u0435!"},

    { ER_UNKNOWN_EXT_NS_PREFIX,
      "(StylesheetHandler) \u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u044b\u0439 \u043f\u0440\u0435\u0444\u0438\u043a\u0441 {1} \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0430 \u0438\u043c\u0435\u043d \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u044f {0}"},

    { ER_IMPORTS_AS_FIRST_ELEM,
      "(StylesheetHandler) Imports \u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043a\u0430\u0447\u0435\u0441\u0442\u0432\u0435 \u043f\u0435\u0440\u0432\u043e\u0433\u043e \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439!"},

    { ER_IMPORTING_ITSELF,
      "(StylesheetHandler) {0} \u043d\u0435\u043f\u043e\u0441\u0440\u0435\u0434\u0441\u0442\u0432\u0435\u043d\u043d\u043e \u0438\u043b\u0438 \u043a\u043e\u0441\u0432\u0435\u043d\u043d\u043e \u0438\u043c\u043f\u043e\u0440\u0442\u0438\u0440\u0443\u0435\u0442 \u0441\u0435\u0431\u044f!"},

    { ER_XMLSPACE_ILLEGAL_VAL,
      "(StylesheetHandler) \u0414\u043b\u044f xml:space \u0443\u043a\u0430\u0437\u0430\u043d\u043e \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435: {0}"},

    { ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL,
      "\u041e\u0448\u0438\u0431\u043a\u0430 processStylesheet!"},

    { ER_SAX_EXCEPTION,
     "\u0418\u0441\u043a\u043b\u044e\u0447\u0438\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u0441\u0438\u0442\u0443\u0430\u0446\u0438\u044f SAX"},

//  add this message to fix bug 21478
    { ER_FUNCTION_NOT_SUPPORTED,
     "\u0424\u0443\u043d\u043a\u0446\u0438\u044f \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f!"},


    { ER_XSLT_ERROR,
     "\u041e\u0448\u0438\u0431\u043a\u0430 XSLT"},

    { ER_CURRENCY_SIGN_ILLEGAL,
      "\u0421\u0438\u043c\u0432\u043e\u043b \u0434\u0435\u043d\u0435\u0436\u043d\u043e\u0439 \u0435\u0434\u0438\u043d\u0438\u0446\u044b \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c \u0432 \u0441\u0442\u0440\u043e\u043a\u0435 \u0444\u043e\u0440\u043c\u0430\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u044f \u0448\u0430\u0431\u043b\u043e\u043d\u0430"},

    { ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM,
      "\u0424\u0443\u043d\u043a\u0446\u0438\u044f \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u0430 \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f \u0432 DOM \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439!"},

    { ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER,
      "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u0442\u044c \u043f\u0440\u0435\u0444\u0438\u043a\u0441 \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044f non-Prefix!"},

    { ER_REDIRECT_COULDNT_GET_FILENAME,
      "\u0420\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u0435 \u043f\u0435\u0440\u0435\u043d\u0430\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f: \u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u043b\u0443\u0447\u0438\u0442\u044c \u0438\u043c\u044f \u0444\u0430\u0439\u043b\u0430 - \u0430\u0442\u0440\u0438\u0431\u0443\u0442 file \u0438\u043b\u0438 select \u0434\u043e\u043b\u0436\u0435\u043d \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u0442\u044c \u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u0443\u044e \u0441\u0442\u0440\u043e\u043a\u0443."},

    { ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT,
      "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c FormatterListener \u0432 \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u0438 \u043f\u0435\u0440\u0435\u043d\u0430\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f!"},

    { ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX,
      "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0439 \u043f\u0440\u0435\u0444\u0438\u043a\u0441 \u0432 exclude-result-prefixes: {0}"},

    { ER_MISSING_NS_URI,
      "\u0414\u043b\u044f \u0443\u043a\u0430\u0437\u0430\u043d\u043d\u043e\u0433\u043e \u043f\u0440\u0435\u0444\u0438\u043a\u0441\u0430 \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 URI \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0430 \u0438\u043c\u0435\u043d"},

    { ER_MISSING_ARG_FOR_OPTION,
      "\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442 \u043e\u043f\u0446\u0438\u0438: {0}"},

    { ER_INVALID_OPTION,
     "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u0430\u044f \u043e\u043f\u0446\u0438\u044f: {0}"},

    { ER_MALFORMED_FORMAT_STRING,
     "\u041d\u0435\u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u0430\u044f \u0441\u0442\u0440\u043e\u043a\u0430 \u0444\u043e\u0440\u043c\u0430\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u044f: {0}"},

    { ER_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "\u0414\u043b\u044f xsl:stylesheet \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 'version'!"},

    { ER_ILLEGAL_ATTRIBUTE_VALUE,
      "\u0410\u0442\u0440\u0438\u0431\u0443\u0442: \u0412 {0} \u0443\u043a\u0430\u0437\u0430\u043d\u043e \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435: {1}"},

    { ER_CHOOSE_REQUIRES_WHEN,
     "\u0414\u043b\u044f xsl:choose \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c xsl:when"},

    { ER_NO_APPLY_IMPORT_IN_FOR_EACH,
      "xsl:apply-imports \u043d\u0435 \u0434\u043e\u043f\u0443\u0441\u043a\u0430\u0435\u0442\u0441\u044f \u0432 xsl:for-each"},

    { ER_CANT_USE_DTM_FOR_OUTPUT,
      "\u041d\u0435\u043b\u044c\u0437\u044f \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c DTMLiaison \u0434\u043b\u044f \u0443\u0437\u043b\u0430 \u0432\u044b\u0432\u043e\u0434\u0430 DOM ... \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 org.apache.xpath.DOM2Helper!"},

    { ER_CANT_USE_DTM_FOR_INPUT,
      "\u041d\u0435\u043b\u044c\u0437\u044f \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c DTMLiaison \u0434\u043b\u044f \u0443\u0437\u043b\u0430 \u0432\u0432\u043e\u0434\u0430 DOM ... \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 org.apache.xpath.DOM2Helper!"},

    { ER_CALL_TO_EXT_FAILED,
      "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0432\u044b\u0437\u043e\u0432\u0435 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u044f: {0}"},

    { ER_PREFIX_MUST_RESOLVE,
      "\u041f\u0440\u0435\u0444\u0438\u043a\u0441 \u0434\u043e\u043b\u0436\u0435\u043d \u043e\u0431\u0435\u0441\u043f\u0435\u0447\u0438\u0432\u0430\u0442\u044c \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u0435 \u0432 \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u043e \u0438\u043c\u0435\u043d: {0}"},

    { ER_INVALID_UTF16_SURROGATE,
      "\u041e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d\u043e \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 UTF-16: {0} ?"},

    { ER_XSLATTRSET_USED_ITSELF,
      "xsl:attribute-set {0} \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0435\u0442 \u0441\u0435\u0431\u044f, \u0447\u0442\u043e \u043f\u0440\u0438\u0432\u0435\u0434\u0435\u0442 \u043a \u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u044e \u0431\u0435\u0441\u043a\u043e\u043d\u0435\u0447\u043d\u043e\u0433\u043e \u0446\u0438\u043a\u043b\u0430."},

    { ER_CANNOT_MIX_XERCESDOM,
      "\u041d\u0435\u043b\u044c\u0437\u044f \u043e\u0434\u043d\u043e\u0432\u0440\u0435\u043c\u0435\u043d\u043d\u043e \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c \u0432\u0432\u043e\u0434 \u043d\u0435-Xerces-DOM \u0438 \u0432\u044b\u0432\u043e\u0434 Xerces-DOM!"},

    { ER_TOO_MANY_LISTENERS,
      "addTraceListenersToStylesheet - TooManyListenersException"},

    { ER_IN_ELEMTEMPLATEELEM_READOBJECT,
      "\u0412 ElemTemplateElement.readObject: {0}"},

    { ER_DUPLICATE_NAMED_TEMPLATE,
      "\u041e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d\u043e \u043d\u0435\u0441\u043a\u043e\u043b\u044c\u043a\u043e \u0448\u0430\u0431\u043b\u043e\u043d\u043e\u0432 \u0441 \u0437\u0430\u0434\u0430\u043d\u043d\u044b\u043c \u0438\u043c\u0435\u043d\u0435\u043c: {0}"},

    { ER_INVALID_KEY_CALL,
      "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0439 \u0432\u044b\u0437\u043e\u0432 \u0444\u0443\u043d\u043a\u0446\u0438\u0438: \u0440\u0435\u043a\u0443\u0440\u0441\u0438\u0432\u043d\u044b\u0435 \u0432\u044b\u0437\u043e\u0432\u044b key() \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b"},

    { ER_REFERENCING_ITSELF,
      "\u041f\u0435\u0440\u0435\u043c\u0435\u043d\u043d\u0430\u044f {0} \u043d\u0435\u043f\u043e\u0441\u0440\u0435\u0434\u0441\u0442\u0432\u0435\u043d\u043d\u043e \u0438\u043b\u0438 \u043a\u043e\u0441\u0432\u0435\u043d\u043d\u043e \u0441\u0441\u044b\u043b\u0430\u0435\u0442\u0441\u044f \u043d\u0430 \u0441\u0435\u0431\u044f!"},

    { ER_ILLEGAL_DOMSOURCE_INPUT,
      "\u0414\u043b\u044f DOMSource \u0432 newTemplates \u0443\u0437\u0435\u043b \u0432\u0432\u043e\u0434\u0430 \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u043f\u0443\u0441\u0442\u044b\u043c!"},

    { ER_CLASS_NOT_FOUND_FOR_OPTION,
        "\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0444\u0430\u0439\u043b \u043a\u043b\u0430\u0441\u0441\u0430 \u0434\u043b\u044f \u043e\u043f\u0446\u0438\u0438 {0}"},

    { ER_REQUIRED_ELEM_NOT_FOUND,
        "\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u043e\u0431\u044f\u0437\u0430\u0442\u0435\u043b\u044c\u043d\u044b\u0439 \u044d\u043b\u0435\u043c\u0435\u043d\u0442: {0}"},

    { ER_INPUT_CANNOT_BE_NULL,
        "InputStream \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u043f\u0443\u0441\u0442\u044b\u043c"},

    { ER_URI_CANNOT_BE_NULL,
        "URI \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u043f\u0443\u0441\u0442\u044b\u043c"},

    { ER_FILE_CANNOT_BE_NULL,
        "\u0424\u0430\u0439\u043b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u043f\u0443\u0441\u0442\u044b\u043c"},

    { ER_SOURCE_CANNOT_BE_NULL,
                "InputSource \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u043f\u0443\u0441\u0442\u044b\u043c"},

    { ER_CANNOT_INIT_BSFMGR,
                "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0438\u043d\u0438\u0446\u0438\u0430\u043b\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440 BSF"},

    { ER_CANNOT_CMPL_EXTENSN,
                "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u043e\u043c\u043f\u0438\u043b\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u0435"},

    { ER_CANNOT_CREATE_EXTENSN,
      "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u0435: {0}, \u043f\u0440\u0438\u0447\u0438\u043d\u0430: {1}"},

    { ER_INSTANCE_MTHD_CALL_REQUIRES,
      "\u041f\u0440\u0438 \u0432\u044b\u0437\u043e\u0432\u0435 \u0432 \u044d\u043a\u0437\u0435\u043c\u043f\u043b\u044f\u0440\u0435 \u043c\u0435\u0442\u043e\u0434\u0430 {0} \u0432 \u043f\u0435\u0440\u0432\u043e\u043c \u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442\u0435 \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u043f\u0435\u0440\u0435\u0434\u0430\u043d \u044d\u043a\u0437\u0435\u043c\u043f\u043b\u044f\u0440 \u043e\u0431\u044a\u0435\u043a\u0442\u0430"},

    { ER_INVALID_ELEMENT_NAME,
      "\u0423\u043a\u0430\u0437\u0430\u043d\u043e \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0438\u043c\u044f \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 {0}"},

    { ER_ELEMENT_NAME_METHOD_STATIC,
      "\u041c\u0435\u0442\u043e\u0434 name \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0441\u0442\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u043c {0}"},

    { ER_EXTENSION_FUNC_UNKNOWN,
             "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u0444\u0443\u043d\u043a\u0446\u0438\u044f \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u044f {0} : {1}"},

    { ER_MORE_MATCH_CONSTRUCTOR,
             "\u041d\u0435\u0441\u043a\u043e\u043b\u044c\u043a\u043e \u043d\u0430\u0438\u043b\u0443\u0447\u0448\u0438\u0445 \u0441\u043e\u043e\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0438\u0439 \u0434\u043b\u044f \u043a\u043e\u043d\u0441\u0442\u0440\u0443\u043a\u0442\u043e\u0440\u0430 {0}"},

    { ER_MORE_MATCH_METHOD,
             "\u041d\u0435\u0441\u043a\u043e\u043b\u044c\u043a\u043e \u043b\u0443\u0447\u0448\u0438\u0445 \u0441\u043e\u043e\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0438\u0439 \u0434\u043b\u044f \u043c\u0435\u0442\u043e\u0434\u0430 {0}"},

    { ER_MORE_MATCH_ELEMENT,
             "\u041d\u0435\u0441\u043a\u043e\u043b\u044c\u043a\u043e \u043b\u0443\u0447\u0448\u0438\u0445 \u0441\u043e\u043e\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0438\u0439 \u0434\u043b\u044f \u043c\u0435\u0442\u043e\u0434\u0430 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 {0}"},

    { ER_INVALID_CONTEXT_PASSED,
             "\u0414\u043b\u044f \u0432\u044b\u0447\u0438\u0441\u043b\u0435\u043d\u0438\u044f \u043f\u0435\u0440\u0435\u0434\u0430\u043d \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0439 \u043a\u043e\u043d\u0442\u0435\u043a\u0441\u0442 {0}"},

    { ER_POOL_EXISTS,
             "\u041f\u0443\u043b \u0443\u0436\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442"},

    { ER_NO_DRIVER_NAME,
             "\u041d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u043e \u0438\u043c\u044f \u0434\u0440\u0430\u0439\u0432\u0435\u0440\u0430"},

    { ER_NO_URL,
             "\u041d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d URL"},

    { ER_POOL_SIZE_LESSTHAN_ONE,
             "\u0420\u0430\u0437\u043c\u0435\u0440 \u043f\u0443\u043b\u0430 \u043c\u0435\u043d\u044c\u0448\u0435 \u0435\u0434\u0438\u043d\u0438\u0446\u044b!"},

    { ER_INVALID_DRIVER,
             "\u0423\u043a\u0430\u0437\u0430\u043d\u043e \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0438\u043c\u044f \u0434\u0440\u0430\u0439\u0432\u0435\u0440\u0430!"},

    { ER_NO_STYLESHEETROOT,
             "\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u043a\u043e\u0440\u043d\u0435\u0432\u043e\u0439 \u044d\u043b\u0435\u043c\u0435\u043d\u0442 \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439!"},

    { ER_ILLEGAL_XMLSPACE_VALUE,
         "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 xml:space"},

    { ER_PROCESSFROMNODE_FAILED,
         "\u041e\u0448\u0438\u0431\u043a\u0430 processFromNode"},

    { ER_RESOURCE_COULD_NOT_LOAD,
        "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0440\u0435\u0441\u0443\u0440\u0441 [ {0} ]: {1} \n {2} \t {3}"},

    { ER_BUFFER_SIZE_LESSTHAN_ZERO,
        "\u0420\u0430\u0437\u043c\u0435\u0440 \u0431\u0443\u0444\u0435\u0440\u0430 <=0"},

    { ER_UNKNOWN_ERROR_CALLING_EXTENSION,
        "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0432\u044b\u0437\u043e\u0432\u0435 \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u044f"},

    { ER_NO_NAMESPACE_DECL,
        "\u0423 \u043f\u0440\u0435\u0444\u0438\u043a\u0441\u0430 {0} \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u043e\u0431\u044a\u044f\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043e\u043e\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0430 \u0438\u043c\u0435\u043d"},

    { ER_ELEM_CONTENT_NOT_ALLOWED,
        "\u0421\u043e\u0434\u0435\u0440\u0436\u0438\u043c\u043e\u0435 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e \u0434\u043b\u044f lang=javaclass {0}"},

    { ER_STYLESHEET_DIRECTED_TERMINATION,
        "\u041f\u0440\u0435\u0440\u0432\u0430\u043d\u043e \u0432 \u0441\u043e\u043e\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0438\u0438 \u0441 \u0442\u0430\u0431\u043b\u0438\u0446\u0435\u0439 \u0441\u0442\u0438\u043b\u0435\u0439"},

    { ER_ONE_OR_TWO,
        "1 \u0438\u043b\u0438 2"},

    { ER_TWO_OR_THREE,
        "2 \u0438\u043b\u0438 3"},

    { ER_COULD_NOT_LOAD_RESOURCE,
        "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c {0} (\u043f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0443 CLASSPATH), \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u044e\u0442\u0441\u044f \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u044f \u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e"},

    { ER_CANNOT_INIT_DEFAULT_TEMPLATES,
        "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0438\u043d\u0438\u0446\u0438\u0430\u043b\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0448\u0430\u0431\u043b\u043e\u043d\u044b \u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e"},

    { ER_RESULT_NULL,
        "\u0420\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442 \u043d\u0435 \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u043f\u0443\u0441\u0442\u044b\u043c"},

    { ER_RESULT_COULD_NOT_BE_SET,
        "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0437\u0430\u0434\u0430\u0442\u044c \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442"},

    { ER_NO_OUTPUT_SPECIFIED,
        "\u041d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d \u0432\u044b\u0432\u043e\u0434"},

    { ER_CANNOT_TRANSFORM_TO_RESULT_TYPE,
        "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u0442\u044c \u0432 \u0420\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442 \u0442\u0438\u043f\u0430 {0}"},

    { ER_CANNOT_TRANSFORM_SOURCE_TYPE,
        "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u0442\u044c \u0432 \u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a \u0442\u0438\u043f\u0430 {0}"},

    { ER_NULL_CONTENT_HANDLER,
        "\u041f\u0443\u0441\u0442\u043e\u0439 \u043e\u0431\u0440\u0430\u0431\u043e\u0442\u0447\u0438\u043a \u0441\u043e\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u044f"},

    { ER_NULL_ERROR_HANDLER,
        "\u041f\u0443\u0441\u0442\u043e\u0439 \u043e\u0431\u0440\u0430\u0431\u043e\u0442\u0447\u0438\u043a \u043e\u0448\u0438\u0431\u043a\u0438"},

    { ER_CANNOT_CALL_PARSE,
        "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0432\u044b\u0437\u0432\u0430\u0442\u044c \u0430\u043d\u0430\u043b\u0438\u0437\u0430\u0442\u043e\u0440, \u0435\u0441\u043b\u0438 \u043d\u0435 \u0437\u0430\u0434\u0430\u043d ContentHandler"},

    { ER_NO_PARENT_FOR_FILTER,
        "\u041d\u0435 \u0437\u0430\u0434\u0430\u043d \u0440\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u0441\u043a\u0438\u0439 \u043e\u0431\u044a\u0435\u043a\u0442 \u0444\u0438\u043b\u044c\u0442\u0440\u0430"},

    { ER_NO_STYLESHEET_IN_MEDIA,
         "\u0412 {0} \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430 \u0442\u0430\u0431\u043b\u0438\u0446\u0430 \u0441\u0442\u0438\u043b\u0435\u0439, \u043d\u043e\u0441\u0438\u0442\u0435\u043b\u044c={1}"},

    { ER_NO_STYLESHEET_PI,
         "\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d PI xml-stylesheet \u0432 {0}"},

    { ER_NOT_SUPPORTED,
       "\u041d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f: {0}"},

    { ER_PROPERTY_VALUE_BOOLEAN,
       "\u0417\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u0441\u0432\u043e\u0439\u0441\u0442\u0432\u0430 {0} \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u044d\u043a\u0437\u0435\u043c\u043f\u043b\u044f\u0440\u043e\u043c Boolean"},

    { ER_COULD_NOT_FIND_EXTERN_SCRIPT,
         "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u043f\u043e\u043b\u0443\u0447\u0438\u0442\u044c \u0432\u043d\u0435\u0448\u043d\u0438\u0439 \u0441\u0446\u0435\u043d\u0430\u0440\u0438\u0439 \u0432 {0}"},

    { ER_RESOURCE_COULD_NOT_FIND,
        "\u0420\u0435\u0441\u0443\u0440\u0441 [ {0} ] \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d.\n {1}"},

    { ER_OUTPUT_PROPERTY_NOT_RECOGNIZED,
        "\u0421\u0432\u043e\u0439\u0441\u0442\u0432\u043e \u0432\u044b\u0432\u043e\u0434\u0430 \u043d\u0435 \u0440\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u043d\u043e: {0}"},

    { ER_FAILED_CREATING_ELEMLITRSLT,
        "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u044d\u043b\u0435\u043c\u0435\u043d\u0442 ElemLiteralResult"},

  //Earlier (JDK 1.4 XALAN 2.2-D11) at key code '204' the key name was ER_PRIORITY_NOT_PARSABLE
  // In latest Xalan code base key name is  ER_VALUE_SHOULD_BE_NUMBER. This should also be taken care
  //in locale specific files like XSLTErrorResources_de.java, XSLTErrorResources_fr.java etc.
  //NOTE: Not only the key name but message has also been changed.

    { ER_VALUE_SHOULD_BE_NUMBER,
        "\u0417\u043d\u0430\u0447\u0435\u043d\u0438\u0435 {0} \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u0430\u043d\u0430\u043b\u0438\u0437\u0438\u0440\u0443\u0435\u043c\u044b\u043c \u0447\u0438\u0441\u043b\u043e\u043c"},

    { ER_VALUE_SHOULD_EQUAL,
        "\u0412 {0} \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u0443\u043a\u0430\u0437\u0430\u043d\u043e \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 yes \u0438\u043b\u0438 no"},

    { ER_FAILED_CALLING_METHOD,
        "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0432\u044b\u0437\u043e\u0432\u0435 \u043c\u0435\u0442\u043e\u0434\u0430 {0}"},

    { ER_FAILED_CREATING_ELEMTMPL,
        "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0441\u043e\u0437\u0434\u0430\u043d\u0438\u0438 \u044d\u043a\u0437\u0435\u043c\u043f\u043b\u044f\u0440\u0430 ElemTemplateElement"},

    { ER_CHARS_NOT_ALLOWED,
        "\u0421\u0438\u043c\u0432\u043e\u043b\u044b \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b \u0432 \u0434\u0430\u043d\u043d\u043e\u0439 \u043f\u043e\u0437\u0438\u0446\u0438\u0438 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u0430"},

    { ER_ATTR_NOT_ALLOWED,
        "\u0410\u0442\u0440\u0438\u0431\u0443\u0442 \"{0}\" \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c \u0432 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0435 {1}!"},

    { ER_BAD_VALUE,
     "{0} \u043d\u0435\u0432\u0435\u0440\u043d\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 {1} "},

    { ER_ATTRIB_VALUE_NOT_FOUND,
     "\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 {0} "},

    { ER_ATTRIB_VALUE_NOT_RECOGNIZED,
     "\u0417\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 {0} \u043d\u0435 \u0440\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u043d\u043e "},

    { ER_NULL_URI_NAMESPACE,
     "\u041f\u043e\u043f\u044b\u0442\u043a\u0430 \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u043f\u0440\u0435\u0444\u0438\u043a\u0441 \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0430 \u0438\u043c\u0435\u043d \u0441 \u043f\u0443\u0441\u0442\u044b\u043c URI"},

  //New ERROR keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    { ER_NUMBER_TOO_BIG,
     "\u041f\u043e\u043f\u044b\u0442\u043a\u0430 \u043e\u0442\u0444\u043e\u0440\u043c\u0430\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0447\u0438\u0441\u043b\u043e \u0431\u043e\u043b\u044c\u0448\u0435 \u043c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u043e \u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0433\u043e LongInteger"},

    { ER_CANNOT_FIND_SAX1_DRIVER,
     "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043d\u0430\u0439\u0442\u0438 \u043a\u043b\u0430\u0441\u0441 \u0434\u0440\u0430\u0439\u0432\u0435\u0440\u0430 SAX1 {0}"},

    { ER_SAX1_DRIVER_NOT_LOADED,
     "\u041a\u043b\u0430\u0441\u0441 \u0434\u0440\u0430\u0439\u0432\u0435\u0440\u0430 SAX1 {0} \u043e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d, \u043d\u043e \u0435\u0433\u043e \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u0430"},

    { ER_SAX1_DRIVER_NOT_INSTANTIATED,
     "\u041a\u043b\u0430\u0441\u0441 \u0434\u0440\u0430\u0439\u0432\u0435\u0440\u0430 SAX1 {0} \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d, \u043d\u043e \u0435\u0433\u043e \u0438\u043d\u0438\u0446\u0438\u0430\u043b\u0438\u0437\u0430\u0446\u0438\u044f \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u0430"},

    { ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER,
     "\u0412 \u043a\u043b\u0430\u0441\u0441\u0435 \u0434\u0440\u0430\u0439\u0432\u0435\u0440\u0430 SAX1 {0} \u043d\u0435 \u0440\u0435\u0430\u043b\u0438\u0437\u043e\u0432\u0430\u043d org.xml.sax.Parser"},

    { ER_PARSER_PROPERTY_NOT_SPECIFIED,
     "\u041d\u0435 \u0437\u0430\u0434\u0430\u043d\u043e \u0441\u0438\u0441\u0442\u0435\u043c\u043d\u043e\u0435 \u0441\u0432\u043e\u0439\u0441\u0442\u0432\u043e org.xml.sax.parser"},

    { ER_PARSER_ARG_CANNOT_BE_NULL,
     "\u0410\u0440\u0433\u0443\u043c\u0435\u043d\u0442 \u0430\u043d\u0430\u043b\u0438\u0437\u0430\u0442\u043e\u0440\u0430 \u043d\u0435 \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u043f\u0443\u0441\u0442\u044b\u043c"},

    { ER_FEATURE,
     "\u0424\u0443\u043d\u043a\u0446\u0438\u044f: {0}"},

    { ER_PROPERTY,
     "\u0421\u0432\u043e\u0439\u0441\u0442\u0432\u043e: {0}"},

    { ER_NULL_ENTITY_RESOLVER,
     "\u041f\u0443\u0441\u0442\u043e\u0439 \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044c \u043c\u0430\u043a\u0440\u043e\u0441\u0430"},

    { ER_NULL_DTD_HANDLER,
     "\u041f\u0443\u0441\u0442\u043e\u0439 \u0434\u0435\u0441\u043a\u0440\u0438\u043f\u0442\u043e\u0440 DTD"},

    { ER_NO_DRIVER_NAME_SPECIFIED,
     "\u041d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u043e \u0438\u043c\u044f \u0434\u0440\u0430\u0439\u0432\u0435\u0440\u0430!"},

    { ER_NO_URL_SPECIFIED,
     "\u041d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d URL!"},

    { ER_POOLSIZE_LESS_THAN_ONE,
     "\u0420\u0430\u0437\u043c\u0435\u0440 \u043f\u0443\u043b\u0430 \u043c\u0435\u043d\u044c\u0448\u0435 1!"},

    { ER_INVALID_DRIVER_NAME,
     "\u0423\u043a\u0430\u0437\u0430\u043d\u043e \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0438\u043c\u044f \u0434\u0440\u0430\u0439\u0432\u0435\u0440\u0430!"},

    { ER_ERRORLISTENER,
     "ErrorListener"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The name
//   'ElemTemplateElement' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_NO_TEMPLATE_PARENT,
     "\u041f\u0440\u043e\u0433\u0440\u0430\u043c\u043c\u043d\u0430\u044f \u043e\u0448\u0438\u0431\u043a\u0430! \u0423 \u0432\u044b\u0440\u0430\u0436\u0435\u043d\u0438\u044f \u043d\u0435\u0442 \u0440\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u0441\u043a\u043e\u0433\u043e \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 ElemTemplateElement!"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The substitution text
//   provides further information in order to diagnose the problem.  The name
//   'RedundentExprEliminator' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR,
     "\u0417\u0430\u043f\u0438\u0441\u044c \u043f\u0440\u043e\u0433\u0440\u0430\u043c\u043c\u0438\u0441\u0442\u0430 \u0432 RedundentExprEliminator: {0}"},

    { ER_NOT_ALLOWED_IN_POSITION,
     "{0} \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e \u0432 \u0434\u0430\u043d\u043d\u043e\u0439 \u043f\u043e\u0437\u0438\u0446\u0438\u0438 \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439!"},

    { ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION,
     "\u0422\u0435\u043a\u0441\u0442 \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c \u0432 \u0434\u0430\u043d\u043d\u043e\u0439 \u043f\u043e\u0437\u0438\u0446\u0438\u0438 \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439!"},

  // This code is shared with warning codes.
  // SystemId Unknown
    { INVALID_TCHAR,
     "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 {1} \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 CHAR: {0}.  \u0410\u0442\u0440\u0438\u0431\u0443\u0442 \u0442\u0438\u043f\u0430 CHAR \u0434\u043e\u043b\u0436\u0435\u043d \u0441\u043e\u0434\u0435\u0440\u0436\u0430\u0442\u044c \u0442\u043e\u043b\u044c\u043a\u043e 1 \u0441\u0438\u043c\u0432\u043e\u043b!"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "QNAME" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value and {0} is the attribute name.
    //The following codes are shared with the warning codes...
    { INVALID_QNAME,
     "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 {1} \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 QNAME: {0}"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "ENUM" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value, {0} is the attribute name, and {2} is a list of valid
    // values.
    { INVALID_ENUM,
     "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 {1} \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 ENUM: {0}.  \u0414\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u044f: {2}."},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NMTOKEN" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NMTOKEN,
     "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 {1} \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 NMTOKEN: {0}. "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NCNAME" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NCNAME,
     "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 {1} \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 NCNAME: {0}. "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "boolean" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_BOOLEAN,
     "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 {1} \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 boolean: {0}. "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "number" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
     { INVALID_NUMBER,
     "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 {1} \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 number: {0}. "},


  // End of shared codes...

// Note to translators:  A "match pattern" is a special form of XPath expression
// that is used for matching patterns.  The substitution text is the name of
// a function.  The message indicates that when this function is referenced in
// a match pattern, its argument must be a string literal (or constant.)
// ER_ARG_LITERAL - new error message for bugzilla //5202
    { ER_ARG_LITERAL,
     "\u0410\u0440\u0433\u0443\u043c\u0435\u043d\u0442 {0} \u0432 \u0448\u0430\u0431\u043b\u043e\u043d\u0435 \u0441\u0440\u0430\u0432\u043d\u0435\u043d\u0438\u044f \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u043b\u0438\u0442\u0435\u0440\u0430\u043b\u043e\u043c."},

// Note to translators:  The following message indicates that two definitions of
// a variable.  A "global variable" is a variable that is accessible everywher
// in the stylesheet.
// ER_DUPLICATE_GLOBAL_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_GLOBAL_VAR,
     "\u041f\u043e\u0432\u0442\u043e\u0440\u043d\u043e\u0435 \u043e\u0431\u044a\u044f\u0432\u043b\u0435\u043d\u0438\u0435 \u0433\u043b\u043e\u0431\u0430\u043b\u044c\u043d\u043e\u0439 \u043f\u0435\u0440\u0435\u043c\u0435\u043d\u043d\u043e\u0439."},


// Note to translators:  The following message indicates that two definitions of
// a variable were encountered.
// ER_DUPLICATE_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_VAR,
     "\u041f\u043e\u0432\u0442\u043e\u0440\u043d\u043e\u0435 \u043e\u0431\u044a\u044f\u0432\u043b\u0435\u043d\u0438\u0435 \u043f\u0435\u0440\u0435\u043c\u0435\u043d\u043d\u043e\u0439."},

    // Note to translators:  "xsl:template, "name" and "match" are XSLT keywords
    // which must not be translated.
    // ER_TEMPLATE_NAME_MATCH - new error message for bugzilla #789
    { ER_TEMPLATE_NAME_MATCH,
     "\u0414\u043b\u044f xsl:template \u0434\u043e\u043b\u0436\u0435\u043d \u0431\u044b\u0442\u044c \u0437\u0430\u0434\u0430\u043d \u0430\u0442\u0440\u0438\u0431\u0443\u0442 name \u0438\u043b\u0438 match, \u043b\u0438\u0431\u043e \u043e\u0431\u0430 \u044d\u0442\u0438\u0445 \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430"},

    // Note to translators:  "exclude-result-prefixes" is an XSLT keyword which
    // should not be translated.  The message indicates that a namespace prefix
    // encountered as part of the value of the exclude-result-prefixes attribute
    // was in error.
    // ER_INVALID_PREFIX - new error message for bugzilla #788
    { ER_INVALID_PREFIX,
     "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0439 \u043f\u0440\u0435\u0444\u0438\u043a\u0441 \u0432 exclude-result-prefixes: {0}"},

    // Note to translators:  An "attribute set" is a set of attributes that can
    // be added to an element in the output document as a group.  The message
    // indicates that there was a reference to an attribute set named {0} that
    // was never defined.
    // ER_NO_ATTRIB_SET - new error message for bugzilla #782
    { ER_NO_ATTRIB_SET,
     "attribute-set \u0441 \u0438\u043c\u0435\u043d\u0435\u043c {0} \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442"},

    // Note to translators:  This message indicates that there was a reference
    // to a function named {0} for which no function definition could be found.
    { ER_FUNCTION_NOT_FOUND,
     "\u0424\u0443\u043d\u043a\u0446\u0438\u044f \u0441 \u0438\u043c\u0435\u043d\u0435\u043c {0} \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442"},

    // Note to translators:  This message indicates that the XSLT instruction
    // that is named by the substitution text {0} must not contain other XSLT
    // instructions (content) or a "select" attribute.  The word "select" is
    // an XSLT keyword in this case and must not be translated.
    { ER_CANT_HAVE_CONTENT_AND_SELECT,
     "\u0414\u043b\u044f \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 {0} \u043d\u0435\u043b\u044c\u0437\u044f \u043e\u0434\u043d\u043e\u0432\u0440\u0435\u043c\u0435\u043d\u043d\u043e \u0443\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u044b \u0441\u043e\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u044f \u0438 \u0432\u044b\u0431\u043e\u0440\u0430. "},

    // Note to translators:  This message indicates that the value argument
    // of setParameter must be a valid Java Object.
    { ER_INVALID_SET_PARAM_VALUE,
     "\u0417\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u043f\u0430\u0440\u0430\u043c\u0435\u0442\u0440\u0430 {0} \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u043c \u043e\u0431\u044a\u0435\u043a\u0442\u043e\u043c Java"},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT,
         "\u0410\u0442\u0440\u0438\u0431\u0443\u0442 result-prefix \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 xsl:namespace-alias \u0441\u043e\u0434\u0435\u0440\u0436\u0438\u0442 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 '#default', \u043d\u043e \u043e\u0431\u044a\u044f\u0432\u043b\u0435\u043d\u0438\u0435 \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0430 \u0438\u043c\u0435\u043d \u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e \u0432 \u043e\u0431\u043b\u0430\u0441\u0442\u0438 \u0434\u043b\u044f \u0434\u0430\u043d\u043d\u043e\u0433\u043e \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e"},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX,
         "\u0410\u0442\u0440\u0438\u0431\u0443\u0442 result-prefix \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 xsl:namespace-alias \u0441\u043e\u0434\u0435\u0440\u0436\u0438\u0442 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 ''{0}'', \u043d\u043e \u043e\u0431\u044a\u044f\u0432\u043b\u0435\u043d\u0438\u0435 \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0430 \u0438\u043c\u0435\u043d \u0434\u043b\u044f \u043f\u0440\u0435\u0444\u0438\u043a\u0441\u0430 ''{0}'' \u0432 \u043e\u0431\u043b\u0430\u0441\u0442\u0438 \u0434\u0430\u043d\u043d\u043e\u0433\u043e \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e. "},

    { ER_SET_FEATURE_NULL_NAME,
      "\u0418\u043c\u044f \u0444\u0443\u043d\u043a\u0446\u0438\u0438 \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u043f\u0443\u0441\u0442\u044b\u043c \u0432 TransformerFactory.setFeature(\u0418\u043c\u044f \u0441\u0442\u0440\u043e\u043a\u0438, \u0431\u0443\u043b\u0435\u0432\u0441\u043a\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435). "},

    { ER_GET_FEATURE_NULL_NAME,
      "\u0418\u043c\u044f \u0444\u0443\u043d\u043a\u0446\u0438\u0438 \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u043f\u0443\u0441\u0442\u044b\u043c \u0432 TransformerFactory.getFeature(\u0418\u043c\u044f \u0441\u0442\u0440\u043e\u043a\u0438). "},

    { ER_UNSUPPORTED_FEATURE,
      "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0437\u0430\u0434\u0430\u0442\u044c \u0444\u0443\u043d\u043a\u0446\u0438\u044e ''{0}'' \u0432 \u044d\u0442\u043e\u043c \u043a\u043b\u0430\u0441\u0441\u0435 TransformerFactory. "},

    { ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING,
        "\u041f\u0440\u0438\u043c\u0435\u043d\u0435\u043d\u0438\u0435 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430 \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u044f ''{0}'' \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e, \u0435\u0441\u043b\u0438 \u0434\u043b\u044f \u0444\u0443\u043d\u043a\u0446\u0438\u0438 \u0437\u0430\u0449\u0438\u0449\u0435\u043d\u043d\u043e\u0439 \u043e\u0431\u0440\u0430\u0431\u043e\u0442\u043a\u0438 \u0437\u0430\u0434\u0430\u043d\u043e \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 true. "},

        { ER_NAMESPACE_CONTEXT_NULL_NAMESPACE,
          "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043d\u0430\u0439\u0442\u0438 \u043f\u0440\u0435\u0444\u0438\u043a\u0441 \u0434\u043b\u044f uri \u043f\u0443\u0441\u0442\u043e\u0433\u043e \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0430 \u0438\u043c\u0435\u043d. "},

        { ER_NAMESPACE_CONTEXT_NULL_PREFIX,
          "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043d\u0430\u0439\u0442\u0438 uri \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0430 \u0438\u043c\u0435\u043d \u0434\u043b\u044f \u043f\u0443\u0441\u0442\u043e\u0433\u043e \u043f\u0440\u0435\u0444\u0438\u043a\u0441\u0430. "},

        { ER_XPATH_RESOLVER_NULL_QNAME,
          "\u0418\u043c\u044f \u0444\u0443\u043d\u043a\u0446\u0438\u0438 \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u043f\u0443\u0441\u0442\u044b\u043c. "},

        { ER_XPATH_RESOLVER_NEGATIVE_ARITY,
          "\u0427\u0438\u0441\u043b\u043e \u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442\u043e\u0432 \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u043e\u0442\u0440\u0438\u0446\u0430\u0442\u0435\u043b\u044c\u043d\u044b\u043c. "},

  // Warnings...

    { WG_FOUND_CURLYBRACE,
      "\u041d\u0430\u0439\u0434\u0435\u043d\u0430 \u0437\u0430\u043a\u0440\u044b\u0432\u0430\u044e\u0449\u0430\u044f \u0441\u043a\u043e\u0431\u043a\u0430 '}', \u043d\u043e \u043e\u0442\u043a\u0440\u044b\u0442\u044b\u0435 \u0448\u0430\u0431\u043b\u043e\u043d\u044b \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u043e\u0432 \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0442!"},

    { WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR,
      "\u041f\u0440\u0435\u0434\u0443\u043f\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u0435: \u0410\u0442\u0440\u0438\u0431\u0443\u0442 count \u043d\u0435 \u0441\u043e\u043e\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0440\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u0441\u043a\u043e\u043c\u0443 \u0432 xsl:number! \u0426\u0435\u043b\u0435\u0432\u043e\u0439 = {0}"},

    { WG_EXPR_ATTRIB_CHANGED_TO_SELECT,
      "\u0421\u0442\u0430\u0440\u044b\u0439 \u0441\u0438\u043d\u0442\u0430\u043a\u0441\u0438\u0441: \u0418\u043c\u044f \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 'expr' \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u043e \u043d\u0430 'select'."},

    { WG_NO_LOCALE_IN_FORMATNUMBER,
      "Xalan \u0435\u0449\u0435 \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u043e\u0431\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u0442\u044c \u0438\u043c\u044f \u043b\u043e\u043a\u0430\u043b\u0438 \u0432 \u0444\u0443\u043d\u043a\u0446\u0438\u0438 format-number."},

    { WG_LOCALE_NOT_FOUND,
      "\u041f\u0440\u0435\u0434\u0443\u043f\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u0435: \u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430 \u043b\u043e\u043a\u0430\u043b\u044c \u0434\u043b\u044f xml:lang={0}"},

    { WG_CANNOT_MAKE_URL_FROM,
      "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0441\u043e\u0437\u0434\u0430\u0442\u044c URL \u0438\u0437: {0}"},

    { WG_CANNOT_LOAD_REQUESTED_DOC,
      "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0437\u0430\u043f\u0440\u043e\u0448\u0435\u043d\u043d\u044b\u0439 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442: {0}"},

    { WG_CANNOT_FIND_COLLATOR,
      "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043d\u0430\u0439\u0442\u0438 Collator \u0434\u043b\u044f <sort xml:lang={0}"},

    { WG_FUNCTIONS_SHOULD_USE_URL,
      "\u0421\u0442\u0430\u0440\u044b\u0439 \u0441\u0438\u043d\u0442\u0430\u043a\u0441\u0438\u0441: \u0432 \u0438\u043d\u0441\u0442\u0440\u0443\u043a\u0446\u0438\u0438 functions \u043d\u0435 \u0441\u043b\u0435\u0434\u0443\u0435\u0442 \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c url {0}"},

    { WG_ENCODING_NOT_SUPPORTED_USING_UTF8,
      "\u041a\u043e\u0434\u0438\u0440\u043e\u0432\u043a\u0430 \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f: {0}, \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0435\u0442\u0441\u044f UTF-8"},

    { WG_ENCODING_NOT_SUPPORTED_USING_JAVA,
      "\u041a\u043e\u0434\u0438\u0440\u043e\u0432\u043a\u0430 \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f: {0}, \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0435\u0442\u0441\u044f Java {1}"},

    { WG_SPECIFICITY_CONFLICTS,
      "\u041e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d \u043a\u043e\u043d\u0444\u043b\u0438\u043a\u0442 \u0441\u043f\u0435\u0446\u0438\u0444\u0438\u043a\u0430\u0446\u0438\u0439: \u0411\u0443\u0434\u0435\u0442 \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c\u0441\u044f \u043f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0439 \u043e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d\u043d\u044b\u0439 \u0432 \u0442\u0430\u0431\u043b\u0438\u0446\u0435 \u0441\u0442\u0438\u043b\u0435\u0439 {0}."},

    { WG_PARSING_AND_PREPARING,
      "========= \u0410\u043d\u0430\u043b\u0438\u0437 \u0438 \u043f\u043e\u0434\u0433\u043e\u0442\u043e\u0432\u043a\u0430 {0} =========="},

    { WG_ATTR_TEMPLATE,
     "\u0428\u0430\u0431\u043b\u043e\u043d \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430, {0}"},

    { WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE,
      "\u041a\u043e\u043d\u0444\u043b\u0438\u043a\u0442 \u0441\u043e\u043e\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0438\u044f xsl:strip-space \u0438 xsl:preserve-space"},

    { WG_ATTRIB_NOT_HANDLED,
      "Xalan \u0435\u0449\u0435 \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u043e\u0431\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u0442\u044c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 {0}!"},

    { WG_NO_DECIMALFORMAT_DECLARATION,
      "\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e \u043e\u0431\u044a\u044f\u0432\u043b\u0435\u043d\u0438\u0435 \u0434\u0435\u0441\u044f\u0442\u0438\u0447\u043d\u043e\u0433\u043e \u0444\u043e\u0440\u043c\u0430\u0442\u0430: {0}"},

    { WG_OLD_XSLT_NS,
     "\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0435 \u0438\u043b\u0438 \u043d\u0435\u0432\u0435\u0440\u043d\u043e\u0435 \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u043e \u0438\u043c\u0435\u043d XSLT. "},

    { WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED,
      "\u0414\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e \u0442\u043e\u043b\u044c\u043a\u043e \u043e\u0434\u043d\u043e \u043e\u0431\u044a\u044f\u0432\u043b\u0435\u043d\u0438\u0435 xsl:decimal-format \u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e."},

    { WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE,
      "\u0418\u043c\u0435\u043d\u0430 xsl:decimal-format \u0434\u043e\u043b\u0436\u043d\u044b \u0431\u044b\u0442\u044c \u0443\u043d\u0438\u043a\u0430\u043b\u044c\u043d\u044b\u043c\u0438. \u0418\u043c\u044f \"{0}\" \u043f\u043e\u0432\u0442\u043e\u0440\u044f\u0435\u0442\u0441\u044f."},

    { WG_ILLEGAL_ATTRIBUTE,
      "\u0414\u043b\u044f {0} \u0443\u043a\u0430\u0437\u0430\u043d \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0439 \u0430\u0442\u0440\u0438\u0431\u0443\u0442: {1}"},

    { WG_COULD_NOT_RESOLVE_PREFIX,
      "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u0442\u044c \u043f\u0440\u0435\u0444\u0438\u043a\u0441 \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0430 \u0438\u043c\u0435\u043d: {0}. \u0423\u0437\u0435\u043b \u0431\u0443\u0434\u0435\u0442 \u043f\u0440\u043e\u0438\u0433\u043d\u043e\u0440\u0438\u0440\u043e\u0432\u0430\u043d."},

    { WG_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "\u0414\u043b\u044f xsl:stylesheet \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 'version'!"},

    { WG_ILLEGAL_ATTRIBUTE_NAME,
      "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0438\u043c\u044f \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430: {0}"},

    { WG_ILLEGAL_ATTRIBUTE_VALUE,
      "\u041d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 {0}: {1}"},

    { WG_EMPTY_SECOND_ARG,
      "\u0420\u0435\u0437\u0443\u043b\u044c\u0442\u0438\u0440\u0443\u044e\u0449\u0438\u0439 \u043d\u0430\u0431\u043e\u0440 \u0443\u0437\u043b\u043e\u0432 \u0438\u0437 \u0432\u0442\u043e\u0440\u043e\u0433\u043e \u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442\u0430 \u0444\u0443\u043d\u043a\u0446\u0438\u0438 document \u043f\u0443\u0441\u0442. \u0412\u043e\u0437\u0432\u0440\u0430\u0442 \u043f\u0443\u0441\u0442\u043e\u0433\u043e node-set."},

  //Following are the new WARNING keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.
    { WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "\u0417\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 'name' \u0432 \u0438\u043c\u0435\u043d\u0438 xsl:processing-instruction \u043d\u0435 \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u0440\u0430\u0432\u043d\u043e 'xml'"},

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.  "NCName" is an XML data-type and must not be
    // translated.
    { WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "\u0417\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u0430\u0442\u0440\u0438\u0431\u0443\u0442\u0430 ''name'' \u0432 xsl:processing-instruction \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u043c \u0438\u043c\u0435\u043d\u0435\u043c NCName: {0}"},

    // Note to translators:  This message is reported if the stylesheet that is
    // being processed attempted to construct an XML document with an attribute in a
    // place other than on an element.  The substitution text specifies the name of
    // the attribute.
    { WG_ILLEGAL_ATTRIBUTE_POSITION,
      "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 {0} \u043f\u043e\u0441\u043b\u0435 \u0434\u043e\u0447\u0435\u0440\u043d\u0438\u0445 \u0443\u0437\u043b\u043e\u0432 \u0438\u043b\u0438 \u043f\u0435\u0440\u0435\u0434 \u0441\u043e\u0437\u0434\u0430\u043d\u0438\u0435\u043c \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u0430.  \u0410\u0442\u0440\u0438\u0431\u0443\u0442 \u0431\u0443\u0434\u0435\u0442 \u043f\u0440\u043e\u0438\u0433\u043d\u043e\u0440\u0438\u0440\u043e\u0432\u0430\u043d. "},

    { NO_MODIFICATION_ALLOWED_ERR,
      "\u041f\u043e\u043f\u044b\u0442\u043a\u0430 \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f \u043e\u0431\u044a\u0435\u043a\u0442\u0430, \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u0435 \u043a\u043e\u0442\u043e\u0440\u043e\u0433\u043e \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e. "
    },

    //Check: WHY THERE IS A GAP B/W NUMBERS in the XSLTErrorResources properties file?

  // Other miscellaneous text used inside the code...
  { "ui_language", "en"},
  {  "help_language",  "en" },
  {  "language",  "en" },
  { "BAD_CODE", "\u041f\u0430\u0440\u0430\u043c\u0435\u0442\u0440 createMessage \u043b\u0435\u0436\u0438\u0442 \u0432\u043d\u0435 \u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u043e\u0433\u043e \u0434\u0438\u0430\u043f\u0430\u0437\u043e\u043d\u0430"},
  {  "FORMAT_FAILED", "\u0418\u0441\u043a\u043b\u044e\u0447\u0438\u0442\u0435\u043b\u044c\u043d\u0430\u044f \u0441\u0438\u0442\u0443\u0430\u0446\u0438\u044f \u043f\u0440\u0438 \u0432\u044b\u0437\u043e\u0432\u0435 messageFormat"},
  {  "version", ">>>>>>> \u0412\u0435\u0440\u0441\u0438\u044f Xalan "},
  {  "version2",  "<<<<<<<"},
  {  "yes", "\u0434\u0430"},
  { "line", "\u041d\u043e\u043c\u0435\u0440 \u0441\u0442\u0440\u043e\u043a\u0438 "},
  { "column","\u041d\u043e\u043c\u0435\u0440 \u0441\u0442\u043e\u043b\u0431\u0446\u0430 "},
  { "xsldone", "XSLProcessor: \u0432\u044b\u043f\u043e\u043b\u043d\u0435\u043d\u043e"},


  // Note to translators:  The following messages provide usage information
  // for the Xalan Process command line.  "Process" is the name of a Java class,
  // and should not be translated.
  { "xslProc_option", "\u041e\u043f\u0446\u0438\u0438 \u043f\u0440\u043e\u0446\u0435\u0441\u0441\u0430 \u043a\u043e\u043c\u0430\u043d\u0434\u043d\u043e\u0439 \u0441\u0442\u0440\u043e\u043a\u0438 Xalan-J:"},
  { "xslProc_option", "\u041e\u043f\u0446\u0438\u0438 \u043f\u0440\u043e\u0446\u0435\u0441\u0441\u0430 \u043a\u043e\u043c\u0430\u043d\u0434\u043d\u043e\u0439 \u0441\u0442\u0440\u043e\u043a\u0438 Xalan-J\u003a"},
  { "xslProc_invalid_xsltc_option", "\u041e\u043f\u0446\u0438\u044f {0} \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f \u0432 \u0440\u0435\u0436\u0438\u043c\u0435 XSLTC."},
  { "xslProc_invalid_xalan_option", "\u041e\u043f\u0446\u0438\u044f {0} \u043c\u043e\u0436\u0435\u0442 \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u0441 -XSLTC."},
  { "xslProc_no_input", "\u041e\u0448\u0438\u0431\u043a\u0430: \u041d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u0430 \u0442\u0430\u0431\u043b\u0438\u0446\u0430 \u0441\u0442\u0438\u043b\u0435\u0439 \u0438\u043b\u0438 \u0438\u0441\u0445\u043e\u0434\u043d\u044b\u0439 \u0444\u0430\u0439\u043b xml. \u0414\u043b\u044f \u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440\u0430 \u0441\u043f\u0440\u0430\u0432\u043a\u0438 \u043f\u043e \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044e \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u0432\u0432\u0435\u0434\u0438\u0442\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u0443 \u0431\u0435\u0437 \u043f\u0430\u0440\u0430\u043c\u0435\u0442\u0440\u043e\u0432."},
  { "xslProc_common_options", "-\u041e\u0431\u0449\u0438\u0435 \u043e\u043f\u0446\u0438\u0438-"},
  { "xslProc_xalan_options", "-\u041e\u043f\u0446\u0438\u0438 Xalan-"},
  { "xslProc_xsltc_options", "-\u041e\u043f\u0446\u0438\u0438 XSLTC-"},
  { "xslProc_return_to_continue", "(\u0414\u043b\u044f \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0435\u043d\u0438\u044f \u043d\u0430\u0436\u043c\u0438\u0442\u0435 <Enter>)"},

   // Note to translators: The option name and the parameter name do not need to
   // be translated. Only translate the messages in parentheses.  Note also that
   // leading whitespace in the messages is used to indent the usage information
   // for each option in the English messages.
   // Do not translate the keywords: XSLTC, SAX, DOM and DTM.
  { "optionXSLTC", "   [-XSLTC (\u0434\u043b\u044f \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u044f \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 XSLTC)]"},
  { "optionIN", "   [-IN inputXMLURL]"},
  { "optionXSL", "   [-XSL XSLTransformationURL]"},
  { "optionOUT",  "   [-OUT outputFileName]"},
  { "optionLXCIN", "   [-LXCIN compiledStylesheetFileNameIn]"},
  { "optionLXCOUT", "   [-LXCOUT compiledStylesheetFileNameOutOut]"},
  { "optionPARSER", "   [-PARSER \u043f\u043e\u043b\u043d\u043e\u0435 \u0438\u043c\u044f \u043a\u043b\u0430\u0441\u0441\u0430 \u0430\u043d\u0430\u043b\u0438\u0437\u0430\u0442\u043e\u0440\u0430]"},
  {  "optionE", "   [-E (\u041d\u0435 \u0440\u0430\u0437\u0432\u043e\u0440\u0430\u0447\u0438\u0432\u0430\u0442\u044c \u0441\u0441\u044b\u043b\u043a\u0438 \u043d\u0430 \u043c\u0430\u043a\u0440\u043e\u0441\u044b entity)]"},
  {  "optionV",  "   [-E (\u041d\u0435 \u0440\u0430\u0437\u0432\u043e\u0440\u0430\u0447\u0438\u0432\u0430\u0442\u044c \u0441\u0441\u044b\u043b\u043a\u0438 \u043d\u0430 \u043c\u0430\u043a\u0440\u043e\u0441\u044b entity)]"},
  {  "optionQC", "   [-QC (\u041d\u0435 \u043f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u0443\u043f\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u044f \u043e \u043a\u043e\u043d\u0444\u043b\u0438\u043a\u0442\u0430\u0445 \u0448\u0430\u0431\u043b\u043e\u043d\u043e\u0432)]"},
  {  "optionQ", "   [-Q  (\u0422\u0438\u0445\u0438\u0439 \u0440\u0435\u0436\u0438\u043c)]"},
  {  "optionLF", "   [-LF (\u041f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c \u0432 \u0432\u044b\u0432\u043e\u0434\u0435 \u0442\u043e\u043b\u044c\u043a\u043e LF {\u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e - CR/LF})]"},
  {  "optionCR", "   [-CR (\u041f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c \u0432 \u0432\u044b\u0432\u043e\u0434\u0435 \u0442\u043e\u043b\u044c\u043a\u043e CR {\u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e - CR/LF})]"},
  { "optionESCAPE", "   [-ESCAPE (\u0421\u0438\u043c\u0432\u043e\u043b\u044b, \u0442\u0440\u0435\u0431\u0443\u044e\u0449\u0438\u0435 Esc-\u043f\u043e\u0441\u043b\u0435\u0434\u043e\u0432\u0430\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u0435\u0439 {\u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e - <>&\"\'\\r\\n}]"},
  { "optionINDENT", "   [-INDENT (\u0427\u0438\u0441\u043b\u043e \u043f\u0440\u043e\u0431\u0435\u043b\u043e\u0432 \u0432 \u043e\u0442\u0441\u0442\u0443\u043f\u0435 {\u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e - 0})]"},
  { "optionTT", "   [-TT (\u0422\u0440\u0430\u0441\u0441\u0438\u0440\u043e\u0432\u043a\u0430 \u0432\u044b\u0437\u044b\u0432\u0430\u0435\u043c\u044b\u0445 \u0448\u0430\u0431\u043b\u043e\u043d\u043e\u0432.)]"},
  { "optionTG", "   [-TG (\u0422\u0440\u0430\u0441\u0441\u0438\u0440\u043e\u0432\u043a\u0430 \u0441\u043e\u0431\u044b\u0442\u0438\u0439 \u0441\u043e\u0437\u0434\u0430\u043d\u0438\u044f.)]"},
  { "optionTS", "   [-TS (\u0422\u0440\u0430\u0441\u0441\u0438\u0440\u043e\u0432\u043a\u0430 \u0441\u043e\u0431\u044b\u0442\u0438\u0439 \u0432\u044b\u0431\u043e\u0440\u0430.)]"},
  {  "optionTTC", "   [-TTC (\u0422\u0440\u0430\u0441\u0441\u0438\u0440\u043e\u0432\u043a\u0430 \u043e\u0431\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u0435\u043c\u044b\u0445 \u0434\u043e\u0447\u0435\u0440\u043d\u0438\u0445 \u043e\u0431\u044a\u0435\u043a\u0442\u043e\u0432 \u0448\u0430\u0431\u043b\u043e\u043d\u043e\u0432.)]"},
  { "optionTCLASS", "   [-TCLASS (\u041a\u043b\u0430\u0441\u0441 TraceListener \u0434\u043b\u044f \u0442\u0440\u0430\u0441\u0441\u0438\u0440\u043e\u0432\u043a\u0430 \u0440\u0430\u0441\u0448\u0438\u0440\u0435\u043d\u0438\u0439.)]"},
  { "optionVALIDATE", "   [-VALIDATE (\u0412\u043a\u043b\u044e\u0447\u0430\u0435\u0442 \u043f\u0440\u043e\u0432\u0435\u0440\u043a\u0443.  \u041f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e \u043f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u0430.)]"},
  { "optionEDUMP", "   [-EDUMP {\u043d\u0435\u043e\u0431\u044f\u0437\u0430\u0442\u0435\u043b\u044c\u043d\u043e\u0435 \u0438\u043c\u044f \u0444\u0430\u0439\u043b\u0430} (\u0414\u0430\u043c\u043f \u0441\u0442\u0435\u043a\u0430 \u043f\u0440\u0438 \u043e\u0448\u0438\u0431\u043a\u0435.)]"},
  {  "optionXML", "   [-XML (\u041f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c \u0444\u043e\u0440\u043c\u0430\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 XML \u0438 \u0434\u043e\u0431\u0430\u0432\u043b\u044f\u0442\u044c \u0437\u0430\u0433\u043e\u043b\u043e\u0432\u043e\u043a XML.)]"},
  {  "optionTEXT", "   [-TEXT (\u041f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c \u0442\u0435\u043a\u0441\u0442\u043e\u0432\u043e\u0435 \u0444\u043e\u0440\u043c\u0430\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435.)]"},
  {  "optionHTML", "   [-HTML (\u041f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c \u0444\u043e\u0440\u043c\u0430\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 HTML.)]"},
  {  "optionPARAM", "   [-PARAM \u0438\u043c\u044f \u0432\u044b\u0440\u0430\u0436\u0435\u043d\u0438\u0435 (\u0417\u0430\u0434\u0430\u0442\u044c \u043f\u0430\u0440\u0430\u043c\u0435\u0442\u0440 \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439)]"},
  {  "noParsermsg1", "\u0412 \u043f\u0440\u043e\u0446\u0435\u0441\u0441\u0435 XSL \u043e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d\u044b \u043e\u0448\u0438\u0431\u043a\u0438."},
  {  "noParsermsg2", "** \u0410\u043d\u0430\u043b\u0438\u0437\u0430\u0442\u043e\u0440 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d **"},
  { "noParsermsg3",  "\u041f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 classpath."},
  { "noParsermsg4", "\u0415\u0441\u043b\u0438 \u0443 \u0432\u0430\u0441 \u043d\u0435\u0442 \u0430\u043d\u0430\u043b\u0438\u0437\u0430\u0442\u043e\u0440\u0430 XML Parser for Java \u0444\u0438\u0440\u043c\u044b IBM, \u0432\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0435\u0433\u043e \u0441 \u0441\u0430\u0439\u0442\u0430"},
  { "noParsermsg5", "IBM AlphaWorks: http://www.alphaworks.ibm.com/formula/xml"},
  { "optionURIRESOLVER", "   [-URIRESOLVER \u0438\u043c\u044f \u043a\u043b\u0430\u0441\u0441\u0430 (URIResolver \u0434\u043b\u044f \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u044f URL)]"},
  { "optionENTITYRESOLVER",  "   [-ENTITYRESOLVER \u0438\u043c\u044f \u043a\u043b\u0430\u0441\u0441\u0430 (EntityResolver \u0434\u043b\u044f \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u044f \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432)]"},
  { "optionCONTENTHANDLER",  "   [-CONTENTHANDLER \u0438\u043c\u044f \u043a\u043b\u0430\u0441\u0441\u0430 (ContentHandler \u0434\u043b\u044f \u0441\u0435\u0440\u0438\u0430\u043b\u0438\u0437\u0430\u0446\u0438\u0438 \u0432\u044b\u0432\u043e\u0434\u0430)]"},
  {  "optionLINENUMBERS",  "   [-L \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c \u043d\u043e\u043c\u0435\u0440\u0430 \u0441\u0442\u0440\u043e\u043a \u0438\u0441\u0445\u043e\u0434\u043d\u043e\u0433\u043e \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u0430]"},
  { "optionSECUREPROCESSING", "   [-SECURE (\u0437\u0430\u0434\u0430\u0439\u0442\u0435 \u0434\u043b\u044f \u0444\u0443\u043d\u043a\u0446\u0438\u0438 \u0437\u0430\u0449\u0438\u0449\u0435\u043d\u043d\u043e\u0439 \u043e\u0431\u0440\u0430\u0431\u043e\u0442\u043a\u0438 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 true.)]"},

    // Following are the new options added in XSLTErrorResources.properties files after Jdk 1.4 (Xalan 2.2-D11)


  {  "optionMEDIA",  "   [-MEDIA \u0442\u0438\u043f-\u043d\u043e\u0441\u0438\u0442. (\u041f\u0440\u0438\u043c\u0435\u043d\u044f\u0442\u044c \u0430\u0442\u0440\u0438\u0431\u0443\u0442 \u043d\u043e\u0441\u0438\u0442\u0435\u043b\u044f \u0434\u043b\u044f \u043f\u043e\u0438\u0441\u043a\u0430 \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439.)]"},
  {  "optionFLAVOR",  "   [-FLAVOR \u0438\u043c\u044f-\u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u044f (\u042f\u0432\u043d\u043e \u0443\u043a\u0430\u0437\u0430\u0442\u044c s2s=SAX \u0438\u043b\u0438 d2d=DOM.)] "}, // Added by sboag/scurcuru; experimental
  { "optionDIAG", "   [-DIAG (\u041f\u0435\u0447\u0430\u0442\u044c \u043e\u0442\u0447\u0435\u0442\u0430 \u043e \u0432\u0440\u0435\u043c\u0435\u043d\u0438 \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u044f \u0432 \u043c\u0438\u043b\u043b\u0438\u0441\u0435\u043a\u0443\u043d\u0434\u0430\u0445.)]"},
  { "optionINCREMENTAL",  "   [-INCREMENTAL (\u0417\u0430\u043f\u0440\u043e\u0441\u0438\u0442\u044c \u0434\u043e\u043f\u043e\u043b\u043d\u044f\u044e\u0449\u0443\u044e \u043c\u043e\u0434\u0435\u043b\u044c DTM \u0441 \u043f\u043e\u043c\u043e\u0449\u044c\u044e \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u044f http://xml.apache.org/xalan/features/incremental true.)]"},
  {  "optionNOOPTIMIMIZE",  "   [-NOOPTIMIMIZE (\u041e\u0442\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u043e\u043f\u0442\u0438\u043c\u0438\u0437\u0430\u0446\u0438\u044e \u0442\u0430\u0431\u043b\u0438\u0446\u044b \u0441\u0442\u0438\u043b\u0435\u0439 \u0441 \u043f\u043e\u043c\u043e\u0449\u044c\u044e \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u044f http://xml.apache.org/xalan/features/optimize false.)]"},
  { "optionRL",  "   [-RL \u043e\u0433\u0440\u0430\u043d\u0438\u0447\u0435\u043d\u0438\u0435 (\u0427\u0438\u0441\u043b\u043e\u0432\u043e\u0435 \u043e\u0433\u0440\u0430\u043d\u0438\u0447\u0435\u043d\u0438\u0435 \u0433\u043b\u0443\u0431\u0438\u043d\u044b \u0440\u0435\u043a\u0443\u0440\u0441\u0438\u0438 \u0442\u0430\u0431\u043b\u0438\u0446 \u0441\u0442\u0438\u043b\u0435\u0439.)]"},
  {   "optionXO",  "   [-XO [\u0438\u043c\u044f-\u043f\u0440\u043e\u0446\u0435\u0434\u0443\u0440\u044b] (\u041f\u0440\u0438\u0441\u0432\u043e\u0438\u0442\u044c \u0438\u043c\u044f \u0441\u043e\u0437\u0434\u0430\u043d\u043d\u043e\u0439 \u043f\u0440\u043e\u0446\u0435\u0434\u0443\u0440\u0435 \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u044f)]"},
  {  "optionXD", "   [-XD \u0446\u0435\u043b\u0435\u0432\u043e\u0439-\u043a\u0430\u0442\u0430\u043b\u043e\u0433 (\u0417\u0430\u0434\u0430\u0435\u0442 \u0446\u0435\u043b\u0435\u0432\u043e\u0439 \u043a\u0430\u0442\u0430\u043b\u043e\u0433 \u0434\u043b\u044f \u043f\u0440\u043e\u0446\u0435\u0434\u0443\u0440\u044b \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u044f)]"},
  {  "optionXJ",  "   [-XJ \u0444\u0430\u0439\u043b-jar (\u0423\u043f\u0430\u043a\u043e\u0432\u044b\u0432\u0430\u0435\u0442 \u043a\u043b\u0430\u0441\u0441\u044b \u043f\u0440\u043e\u0446\u0435\u0434\u0443\u0440\u044b \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u044f \u0432 <\u0444\u0430\u0439\u043b-jar>)]"},
  {   "optionXP",  "   [-XP \u043f\u0430\u043a\u0435\u0442 (\u041f\u0440\u0435\u0444\u0438\u043a\u0441 \u0438\u043c\u0435\u043d\u0438 \u043f\u0430\u043a\u0435\u0442\u0430 \u0434\u043b\u044f \u0432\u0441\u0435\u0445 \u0441\u043e\u0437\u0434\u0430\u043d\u043d\u044b\u0445 \u043a\u043b\u0430\u0441\u0441\u043e\u0432 translet)]"},

  //AddITIONAL  STRINGS that need L10n
  // Note to translators:  The following message describes usage of a particular
  // command-line option that is used to enable the "template inlining"
  // optimization.  The optimization involves making a copy of the code
  // generated for a template in another template that refers to it.
  { "optionXN",  "   [-XN (\u0440\u0430\u0437\u0440\u0435\u0448\u0430\u0435\u0442 \u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 \u0448\u0430\u0431\u043b\u043e\u043d\u0430)]" },
  { "optionXX",  "   [-XX (\u0432\u043a\u043b\u044e\u0447\u0430\u0435\u0442 \u0432\u044b\u0432\u043e\u0434 \u0434\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u044b\u0445 \u043e\u0442\u043b\u0430\u0434\u043e\u0447\u043d\u044b\u0445 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0439)]"},
  { "optionXT" , "   [-XT (\u043f\u043e \u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e\u0441\u0442\u0438 \u043f\u0440\u0438\u043c\u0435\u043d\u044f\u0435\u0442 \u043f\u0440\u043e\u0446\u0435\u0434\u0443\u0440\u0443 \u043f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u044f)]"},
  { "diagTiming"," --------- \u041f\u0440\u0435\u043e\u0431\u0440\u0430\u0437\u043e\u0432\u0430\u043d\u0438\u0435 {0} \u0441 \u043f\u043e\u043c\u043e\u0449\u044c\u044e {1} \u0437\u0430\u043d\u044f\u043b\u043e {2} \u043c\u0441" },
  { "recursionTooDeep","\u0421\u043b\u0438\u0448\u043a\u043e\u043c \u0431\u043e\u043b\u044c\u0448\u0430\u044f \u0432\u043b\u043e\u0436\u0435\u043d\u043d\u043e\u0441\u0442\u044c \u0448\u0430\u0431\u043b\u043e\u043d\u043e\u0432. \u0412\u043b\u043e\u0436\u0435\u043d\u043d\u043e\u0441\u0442\u044c = {0}, \u0448\u0430\u0431\u043b\u043e\u043d {1} {2}" },
  { "nameIs", "\u0438\u043c\u044f" },
  { "matchPatternIs", "\u0448\u0430\u0431\u043b\u043e\u043d \u0441\u043e\u043e\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0438\u044f" }

  };
  }
  // ================= INFRASTRUCTURE ======================

  /** String for use when a bad error code was encountered.    */
  public static final String BAD_CODE = "BAD_CODE";

  /** String for use when formatting of the error string failed.   */
  public static final String FORMAT_FAILED = "FORMAT_FAILED";

  /** General error string.   */
  public static final String ERROR_STRING = "\u041e\u0448\u0438\u0431\u043a\u0430";

  /** String to prepend to error messages.  */
  public static final String ERROR_HEADER = "\u041e\u0448\u0438\u0431\u043a\u0430: ";

  /** String to prepend to warning messages.    */
  public static final String WARNING_HEADER = "\u041f\u0440\u0435\u0434\u0443\u043f\u0440\u0435\u0436\u0434\u0435\u043d\u0438\u0435: ";

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
