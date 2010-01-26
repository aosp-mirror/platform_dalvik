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
 * $Id: XSLTErrorResources_ca.java 468641 2006-10-28 06:54:42Z minchau $
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
public class XSLTErrorResources_ca extends ListResourceBundle
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
      "Error: no hi pot haver un car\u00e0cter '{' dins l'expressi\u00f3"},

    { ER_ILLEGAL_ATTRIBUTE ,
     "{0} t\u00e9 un atribut no perm\u00e8s: {1}"},

  {ER_NULL_SOURCENODE_APPLYIMPORTS ,
      "sourceNode \u00e9s nul en xsl:apply-imports."},

  {ER_CANNOT_ADD,
      "No es pot afegir {0} a {1}"},

    { ER_NULL_SOURCENODE_HANDLEAPPLYTEMPLATES,
      "sourceNode \u00e9s nul en handleApplyTemplatesInstruction."},

    { ER_NO_NAME_ATTRIB,
     "{0} ha de tenir un atribut de nom."},

    {ER_TEMPLATE_NOT_FOUND,
     "No s''ha trobat la plantilla anomenada: {0}"},

    {ER_CANT_RESOLVE_NAME_AVT,
      "No s'ha pogut resoldre l'AVT de noms a xsl:call-template."},

    {ER_REQUIRES_ATTRIB,
     "{0} necessita l''atribut: {1}"},

    { ER_MUST_HAVE_TEST_ATTRIB,
      "{0} ha de tenir un atribut ''test''. "},

    {ER_BAD_VAL_ON_LEVEL_ATTRIB,
      "Valor incorrecte a l''atribut de nivell: {0}"},

    {ER_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "El nom processing-instruction no pot ser 'xml'"},

    { ER_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "El nom processing-instruction ha de ser un NCName v\u00e0lid: {0}"},

    { ER_NEED_MATCH_ATTRIB,
      "{0} ha de tenir un atribut que hi coincideixi si t\u00e9 una modalitat."},

    { ER_NEED_NAME_OR_MATCH_ATTRIB,
      "{0} necessita un nom o un atribut que hi coincideixi."},

    {ER_CANT_RESOLVE_NSPREFIX,
      "No s''ha pogut resoldre el prefix d''espai de noms: {0}"},

    { ER_ILLEGAL_VALUE,
     "xml:space t\u00e9 un valor no v\u00e0lid: {0}"},

    { ER_NO_OWNERDOC,
      "El node subordinat no t\u00e9 un document de propietari."},

    { ER_ELEMTEMPLATEELEM_ERR,
     "Error d''ElemTemplateElement: {0}"},

    { ER_NULL_CHILD,
     "S'est\u00e0 intentant afegir un subordinat nul."},

    { ER_NEED_SELECT_ATTRIB,
     "{0} necessita un atribut de selecci\u00f3."},

    { ER_NEED_TEST_ATTRIB ,
      "xsl:when ha de tenir un atribut 'test'."},

    { ER_NEED_NAME_ATTRIB,
      "xsl:with-param ha de tenir un atribut 'name'."},

    { ER_NO_CONTEXT_OWNERDOC,
      "El context no t\u00e9 un document de propietari."},

    {ER_COULD_NOT_CREATE_XML_PROC_LIAISON,
      "No s''ha pogut crear la relaci\u00f3 XML TransformerFactory: {0}"},

    {ER_PROCESS_NOT_SUCCESSFUL,
      "Xalan: el proc\u00e9s no ha estat correcte."},

    { ER_NOT_SUCCESSFUL,
     "Xalan no ha estat correcte."},

    { ER_ENCODING_NOT_SUPPORTED,
     "La codificaci\u00f3 no t\u00e9 suport: {0}"},

    {ER_COULD_NOT_CREATE_TRACELISTENER,
      "No s''ha pogut crear TraceListener: {0}"},

    {ER_KEY_REQUIRES_NAME_ATTRIB,
      "xsl:key necessita un atribut 'name'."},

    { ER_KEY_REQUIRES_MATCH_ATTRIB,
      "xsl:key necessita un atribut 'match'."},

    { ER_KEY_REQUIRES_USE_ATTRIB,
      "xsl:key necessita un atribut 'use'."},

    { ER_REQUIRES_ELEMENTS_ATTRIB,
      "(StylesheetHandler) {0} necessita un atribut ''elements''. "},

    { ER_MISSING_PREFIX_ATTRIB,
      "(StylesheetHandler) falta l''atribut ''prefix'' {0}. "},

    { ER_BAD_STYLESHEET_URL,
     "La URL del full d''estils \u00e9s incorrecta: {0}"},

    { ER_FILE_NOT_FOUND,
     "No s''ha trobat el fitxer del full d''estils: {0}"},

    { ER_IOEXCEPTION,
      "S''ha produ\u00eft una excepci\u00f3 d''E/S amb el fitxer de full d''estils: {0}"},

    { ER_NO_HREF_ATTRIB,
      "(StylesheetHandler) No s''ha trobat l''atribut href de {0}"},

    { ER_STYLESHEET_INCLUDES_ITSELF,
      "(StylesheetHandler) {0} s''est\u00e0 incloent a ell mateix directament o indirecta."},

    { ER_PROCESSINCLUDE_ERROR,
      "Error de StylesheetHandler.processInclude, {0}"},

    { ER_MISSING_LANG_ATTRIB,
      "(StylesheetHandler) falta l''atribut ''lang'' {0}. "},

    { ER_MISSING_CONTAINER_ELEMENT_COMPONENT,
      "(StylesheetHandler) L''element {0} \u00e9s fora de lloc? Falta l''element de contenidor ''component''"},

    { ER_CAN_ONLY_OUTPUT_TO_ELEMENT,
      "La sortida nom\u00e9s pot ser cap a un Element, Fragment de document, Document o Transcriptor de documents."},

    { ER_PROCESS_ERROR,
     "Error de StylesheetRoot.process"},

    { ER_UNIMPLNODE_ERROR,
     "Error d''UnImplNode: {0}"},

    { ER_NO_SELECT_EXPRESSION,
      "Error. No s'ha trobat l'expressi\u00f3 select d'xpath (-select)."},

    { ER_CANNOT_SERIALIZE_XSLPROCESSOR,
      "No es pot serialitzar un XSLProcessor."},

    { ER_NO_INPUT_STYLESHEET,
      "No s'ha especificat l'entrada del full d'estils."},

    { ER_FAILED_PROCESS_STYLESHEET,
      "No s'ha pogut processar el full d'estils."},

    { ER_COULDNT_PARSE_DOC,
     "No s''ha pogut analitzar el document {0}."},

    { ER_COULDNT_FIND_FRAGMENT,
     "No s''ha pogut trobar el fragment: {0}"},

    { ER_NODE_NOT_ELEMENT,
      "El node al qual apuntava l''identificador de fragments no era un element: {0}"},

    { ER_FOREACH_NEED_MATCH_OR_NAME_ATTRIB,
      "for-each ha de tenir o b\u00e9 una coincid\u00e8ncia o b\u00e9 un atribut de nom."},

    { ER_TEMPLATES_NEED_MATCH_OR_NAME_ATTRIB,
      "Les plantilles han de tenir o b\u00e9 una coincid\u00e8ncia o b\u00e9 un atribut de nom."},

    { ER_NO_CLONE_OF_DOCUMENT_FRAG,
      "No hi ha cap clonatge d'un fragment de document."},

    { ER_CANT_CREATE_ITEM,
      "No es pot crear un element a l''arbre de resultats: {0}"},

    { ER_XMLSPACE_ILLEGAL_VALUE,
      "xml:space de l''XML d''origen t\u00e9 un valor no perm\u00e8s: {0}"},

    { ER_NO_XSLKEY_DECLARATION,
      "No hi ha cap declaraci\u00f3 d''xls:key per a {0}."},

    { ER_CANT_CREATE_URL,
     "Error. No es pot crear la URL de: {0}"},

    { ER_XSLFUNCTIONS_UNSUPPORTED,
     "xsl:functions no t\u00e9 suport."},

    { ER_PROCESSOR_ERROR,
     "Error d'XSLT TransformerFactory"},

    { ER_NOT_ALLOWED_INSIDE_STYLESHEET,
      "(StylesheetHandler) {0} no est\u00e0 perm\u00e8s dins d''un full d''estils."},

    { ER_RESULTNS_NOT_SUPPORTED,
      "result-ns ja no t\u00e9 suport. En comptes d'aix\u00f2, feu servir xsl:output."},

    { ER_DEFAULTSPACE_NOT_SUPPORTED,
      "default-space ja no t\u00e9 suport. En comptes d'aix\u00f2, feu servir xsl:strip-space o xsl:preserve-space."},

    { ER_INDENTRESULT_NOT_SUPPORTED,
      "indent-result ja no t\u00e9 suport. En comptes d'aix\u00f2, feu servir xsl:output."},

    { ER_ILLEGAL_ATTRIB,
      "(StylesheetHandler) {0} t\u00e9 un atribut no perm\u00e8s: {1}"},

    { ER_UNKNOWN_XSL_ELEM,
     "Element XSL desconegut: {0}"},

    { ER_BAD_XSLSORT_USE,
      "(StylesheetHandler) xsl:sort nom\u00e9s es pot utilitzar amb xsl:apply-templates o xsl:for-each."},

    { ER_MISPLACED_XSLWHEN,
      "(StylesheetHandler) xsl:when est\u00e0 mal col\u00b7locat."},

    { ER_XSLWHEN_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:when no ha estat analitzat per xsl:choose."},

    { ER_MISPLACED_XSLOTHERWISE,
      "(StylesheetHandler) xsl:otherwise est\u00e0 mal col\u00b7locat."},

    { ER_XSLOTHERWISE_NOT_PARENTED_BY_XSLCHOOSE,
      "(StylesheetHandler) xsl:otherwise no t\u00e9 com a superior xsl:choose."},

    { ER_NOT_ALLOWED_INSIDE_TEMPLATE,
      "(StylesheetHandler) {0} no est\u00e0 perm\u00e8s dins d''una plantilla."},

    { ER_UNKNOWN_EXT_NS_PREFIX,
      "(StylesheetHandler) {0} prefix d''espai de noms d''extensi\u00f3 {1} desconegut"},

    { ER_IMPORTS_AS_FIRST_ELEM,
      "(StylesheetHandler) Les importacions nom\u00e9s es poden produir com els primers elements del full d'estils."},

    { ER_IMPORTING_ITSELF,
      "(StylesheetHandler) {0} s''est\u00e0 important a ell mateix directament o indirecta."},

    { ER_XMLSPACE_ILLEGAL_VAL,
      "(StylesheetHandler) xml:space t\u00e9 un valor no perm\u00e8s: {0}"},

    { ER_PROCESSSTYLESHEET_NOT_SUCCESSFUL,
      "processStylesheet no ha estat correcte."},

    { ER_SAX_EXCEPTION,
     "Excepci\u00f3 SAX"},

//  add this message to fix bug 21478
    { ER_FUNCTION_NOT_SUPPORTED,
     "Aquesta funci\u00f3 no t\u00e9 suport."},


    { ER_XSLT_ERROR,
     "Error d'XSLT"},

    { ER_CURRENCY_SIGN_ILLEGAL,
      "El signe de moneda no est\u00e0 perm\u00e8s en una cadena de patr\u00f3 de format."},

    { ER_DOCUMENT_FUNCTION_INVALID_IN_STYLESHEET_DOM,
      "La funci\u00f3 document no t\u00e9 suport al DOM de full d'estils."},

    { ER_CANT_RESOLVE_PREFIX_OF_NON_PREFIX_RESOLVER,
      "No es pot resoldre el prefix del solucionador sense prefix."},

    { ER_REDIRECT_COULDNT_GET_FILENAME,
      "Extensi\u00f3 de redirecci\u00f3: No s'ha pogut obtenir el nom del fitxer - els atributs file o select han de retornar una cadena v\u00e0lida."},

    { ER_CANNOT_BUILD_FORMATTERLISTENER_IN_REDIRECT,
      "No es pot crear build FormatterListener en l'extensi\u00f3 de redirecci\u00f3."},

    { ER_INVALID_PREFIX_IN_EXCLUDERESULTPREFIX,
      "El prefix d''exclude-result-prefixes no \u00e9s v\u00e0lid: {0}"},

    { ER_MISSING_NS_URI,
      "Falta l'URI d'espai de noms del prefix especificat."},

    { ER_MISSING_ARG_FOR_OPTION,
      "Falta un argument de l''opci\u00f3: {0}"},

    { ER_INVALID_OPTION,
     "Opci\u00f3 no v\u00e0lida: {0}"},

    { ER_MALFORMED_FORMAT_STRING,
     "Cadena de format mal formada: {0}"},

    { ER_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet necessita un atribut 'version'."},

    { ER_ILLEGAL_ATTRIBUTE_VALUE,
      "L''atribut {0} t\u00e9 un valor no perm\u00e8s {1}"},

    { ER_CHOOSE_REQUIRES_WHEN,
     "xsl:choose necessita un xsl:when"},

    { ER_NO_APPLY_IMPORT_IN_FOR_EACH,
      "xsl:apply-imports no es permeten en un xsl:for-each"},

    { ER_CANT_USE_DTM_FOR_OUTPUT,
      "No es pot utilitzar una DTMLiaison per a un node DOM de sortida. En lloc d'aix\u00f2, utilitzeu org.apache.xpath.DOM2Helper."},

    { ER_CANT_USE_DTM_FOR_INPUT,
      "No es pot utilitzar una DTMLiaison per a un node DOM d'entrada. En lloc d'aix\u00f2, utilitzeu org.apache.xpath.DOM2Helper."},

    { ER_CALL_TO_EXT_FAILED,
      "S''ha produ\u00eft un error en la crida de l''element d''extensi\u00f3 {0}"},

    { ER_PREFIX_MUST_RESOLVE,
      "El prefix s''ha de resoldre en un espai de noms: {0}"},

    { ER_INVALID_UTF16_SURROGATE,
      "S''ha detectat un suplent UTF-16 no v\u00e0lid: {0} ?"},

    { ER_XSLATTRSET_USED_ITSELF,
      "xsl:attribute-set {0} s''ha utilitzat a ell mateix; aix\u00f2 crear\u00e0 un bucle infinit."},

    { ER_CANNOT_MIX_XERCESDOM,
      "No es pot barrejar entrada no Xerces-DOM amb sortida Xerces-DOM."},

    { ER_TOO_MANY_LISTENERS,
      "addTraceListenersToStylesheet - TooManyListenersException"},

    { ER_IN_ELEMTEMPLATEELEM_READOBJECT,
      "En ElemTemplateElement.readObject: {0}"},

    { ER_DUPLICATE_NAMED_TEMPLATE,
      "S''ha trobat m\u00e9s d''una plantilla anomenada {0}"},

    { ER_INVALID_KEY_CALL,
      "Crida de funci\u00f3 no v\u00e0lida: les crides key() recursives no estan permeses."},

    { ER_REFERENCING_ITSELF,
      "La variable {0} est\u00e0 fent refer\u00e8ncia a ella mateixa directa o indirectament."},

    { ER_ILLEGAL_DOMSOURCE_INPUT,
      "El node d'entrada no pot ser nul per a DOMSource de newTemplates."},

    { ER_CLASS_NOT_FOUND_FOR_OPTION,
        "No s''ha trobat el fitxer de classe per a l''opci\u00f3 {0}"},

    { ER_REQUIRED_ELEM_NOT_FOUND,
        "L''element necessari no s''ha trobat: {0}"},

    { ER_INPUT_CANNOT_BE_NULL,
        "InputStream no pot ser nul."},

    { ER_URI_CANNOT_BE_NULL,
        "L'URI no pot ser nul."},

    { ER_FILE_CANNOT_BE_NULL,
        "El fitxer no pot ser nul."},

    { ER_SOURCE_CANNOT_BE_NULL,
                "InputSource no pot ser nul."},

    { ER_CANNOT_INIT_BSFMGR,
                "No s'ha pogut inicialitzar BSF Manager"},

    { ER_CANNOT_CMPL_EXTENSN,
                "No s'ha pogut compilar l'extensi\u00f3"},

    { ER_CANNOT_CREATE_EXTENSN,
      "No s''ha pogut crear l''extensi\u00f3 {0} a causa de {1}"},

    { ER_INSTANCE_MTHD_CALL_REQUIRES,
      "La crida del m\u00e8tode d''inst\u00e0ncia {0} necessita una inst\u00e0ncia d''objecte com a primer argument"},

    { ER_INVALID_ELEMENT_NAME,
      "S''ha especificat un nom d''element no v\u00e0lid {0}"},

    { ER_ELEMENT_NAME_METHOD_STATIC,
      "El m\u00e8tode del nom de l''element ha de ser est\u00e0tic {0}"},

    { ER_EXTENSION_FUNC_UNKNOWN,
             "No es coneix la funci\u00f3 d''extensi\u00f3 {0} : {1}."},

    { ER_MORE_MATCH_CONSTRUCTOR,
             "Hi ha m\u00e9s d''una millor coincid\u00e8ncia per al constructor de {0}"},

    { ER_MORE_MATCH_METHOD,
             "Hi ha m\u00e9s d''una millor coincid\u00e8ncia per al m\u00e8tode {0}"},

    { ER_MORE_MATCH_ELEMENT,
             "Hi ha m\u00e9s d''una millor coincid\u00e8ncia per al m\u00e8tode d''element {0}"},

    { ER_INVALID_CONTEXT_PASSED,
             "S''ha donat un context no v\u00e0lid per avaluar {0}"},

    { ER_POOL_EXISTS,
             "L'agrupaci\u00f3 ja existeix"},

    { ER_NO_DRIVER_NAME,
             "No s'ha especificat cap nom de controlador"},

    { ER_NO_URL,
             "No s'ha especificat cap URL"},

    { ER_POOL_SIZE_LESSTHAN_ONE,
             "La grand\u00e0ria de l'agrupaci\u00f3 \u00e9s inferior a u"},

    { ER_INVALID_DRIVER,
             "S'ha especificat un nom de controlador no v\u00e0lid"},

    { ER_NO_STYLESHEETROOT,
             "No s'ha trobat l'arrel del full d'estils"},

    { ER_ILLEGAL_XMLSPACE_VALUE,
         "Valor no perm\u00e8s per a xml:space"},

    { ER_PROCESSFROMNODE_FAILED,
         "S'ha produ\u00eft un error a processFromNode"},

    { ER_RESOURCE_COULD_NOT_LOAD,
        "No s''ha pogut carregar el recurs [ {0} ]: {1} \n {2} \t {3}"},

    { ER_BUFFER_SIZE_LESSTHAN_ZERO,
        "Grand\u00e0ria del buffer <=0"},

    { ER_UNKNOWN_ERROR_CALLING_EXTENSION,
        "S'ha produ\u00eft un error desconegut en cridar l'extensi\u00f3"},

    { ER_NO_NAMESPACE_DECL,
        "El prefix {0} no t\u00e9 una declaraci\u00f3 d''espai de noms corresponent"},

    { ER_ELEM_CONTENT_NOT_ALLOWED,
        "El contingut de l''element no est\u00e0 perm\u00e8s per a lang=javaclass {0}"},

    { ER_STYLESHEET_DIRECTED_TERMINATION,
        "El full d'estils ha ordenat l'acabament"},

    { ER_ONE_OR_TWO,
        "1 o 2"},

    { ER_TWO_OR_THREE,
        "2 o 3"},

    { ER_COULD_NOT_LOAD_RESOURCE,
        "No s''ha pogut carregar {0} (comproveu la CLASSPATH); ara s''estan fent servir els valors per defecte."},

    { ER_CANNOT_INIT_DEFAULT_TEMPLATES,
        "No es poden inicialitzar les plantilles per defecte"},

    { ER_RESULT_NULL,
        "El resultat no ha de ser nul"},

    { ER_RESULT_COULD_NOT_BE_SET,
        "No s'ha pogut establir el resultat"},

    { ER_NO_OUTPUT_SPECIFIED,
        "No s'ha especificat cap sortida"},

    { ER_CANNOT_TRANSFORM_TO_RESULT_TYPE,
        "No s''ha pogut transformar en un resultat del tipus {0}"},

    { ER_CANNOT_TRANSFORM_SOURCE_TYPE,
        "No s''ha pogut transformar en un origen del tipus {0}"},

    { ER_NULL_CONTENT_HANDLER,
        "Manejador de contingut nul"},

    { ER_NULL_ERROR_HANDLER,
        "Manejador d'error nul"},

    { ER_CANNOT_CALL_PARSE,
        "L'an\u00e0lisi no es pot cridar si no s'ha establert ContentHandler"},

    { ER_NO_PARENT_FOR_FILTER,
        "El filtre no t\u00e9 superior"},

    { ER_NO_STYLESHEET_IN_MEDIA,
         "No s''ha trobat cap full d''estils a {0}, suport= {1}"},

    { ER_NO_STYLESHEET_PI,
         "No s''ha trobat cap PI d''xml-stylesheet a {0}"},

    { ER_NOT_SUPPORTED,
       "No t\u00e9 suport: {0}"},

    { ER_PROPERTY_VALUE_BOOLEAN,
       "El valor de la propietat {0} ha de ser una inst\u00e0ncia booleana"},

    { ER_COULD_NOT_FIND_EXTERN_SCRIPT,
         "No s''ha pogut arribar a l''script extern a {0}"},

    { ER_RESOURCE_COULD_NOT_FIND,
        "No s''ha trobat el recurs [ {0} ].\n {1}"},

    { ER_OUTPUT_PROPERTY_NOT_RECOGNIZED,
        "La propietat de sortida no es reconeix: {0}"},

    { ER_FAILED_CREATING_ELEMLITRSLT,
        "S'ha produ\u00eft un error en crear la inst\u00e0ncia ElemLiteralResult"},

  //Earlier (JDK 1.4 XALAN 2.2-D11) at key code '204' the key name was ER_PRIORITY_NOT_PARSABLE
  // In latest Xalan code base key name is  ER_VALUE_SHOULD_BE_NUMBER. This should also be taken care
  //in locale specific files like XSLTErrorResources_de.java, XSLTErrorResources_fr.java etc.
  //NOTE: Not only the key name but message has also been changed.

    { ER_VALUE_SHOULD_BE_NUMBER,
        "El valor de {0} ha de contenir un n\u00famero que es pugui analitzar"},

    { ER_VALUE_SHOULD_EQUAL,
        "El valor de {0} ha de ser igual a yes o no"},

    { ER_FAILED_CALLING_METHOD,
        "No s''ha pogut cridar el m\u00e8tode {0}"},

    { ER_FAILED_CREATING_ELEMTMPL,
        "No s'ha pogut crear la inst\u00e0ncia ElemTemplateElement"},

    { ER_CHARS_NOT_ALLOWED,
        "En aquest punt del document no es permeten els car\u00e0cters"},

    { ER_ATTR_NOT_ALLOWED,
        "L''atribut \"{0}\" no es permet en l''element {1}"},

    { ER_BAD_VALUE,
     "{0} valor erroni {1} "},

    { ER_ATTRIB_VALUE_NOT_FOUND,
     "No s''ha trobat el valor de l''atribut {0} "},

    { ER_ATTRIB_VALUE_NOT_RECOGNIZED,
     "No es reconeix el valor de l''atribut {0} "},

    { ER_NULL_URI_NAMESPACE,
     "S'intenta generar un prefix d'espai de noms amb un URI nul"},

  //New ERROR keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    { ER_NUMBER_TOO_BIG,
     "S'intenta formatar un n\u00famero m\u00e9s gran que l'enter llarg m\u00e9s gran"},

    { ER_CANNOT_FIND_SAX1_DRIVER,
     "No es pot trobar la classe de controlador SAX1 {0}"},

    { ER_SAX1_DRIVER_NOT_LOADED,
     "S''ha trobat la classe de controlador SAX1 {0} per\u00f2 no es pot carregar"},

    { ER_SAX1_DRIVER_NOT_INSTANTIATED,
     "S''ha carregat la classe de controlador SAX1 {0} per\u00f2 no es pot particularitzar"},

    { ER_SAX1_DRIVER_NOT_IMPLEMENT_PARSER,
     "La classe de controlador SAX1 {0} no implementa org.xml.sax.Parser"},

    { ER_PARSER_PROPERTY_NOT_SPECIFIED,
     "No s'ha identificat la propietat del sistema org.xml.sax.parser"},

    { ER_PARSER_ARG_CANNOT_BE_NULL,
     "L'argument d'analitzador ha de ser nul"},

    { ER_FEATURE,
     "Caracter\u00edstica: {0}"},

    { ER_PROPERTY,
     "Propietat: {0}"},

    { ER_NULL_ENTITY_RESOLVER,
     "Solucionador d'entitat nul"},

    { ER_NULL_DTD_HANDLER,
     "Manejador de DTD nul"},

    { ER_NO_DRIVER_NAME_SPECIFIED,
     "No s'ha especificat cap nom de controlador"},

    { ER_NO_URL_SPECIFIED,
     "No s'ha especificat cap URL"},

    { ER_POOLSIZE_LESS_THAN_ONE,
     "La grand\u00e0ria de l'agrupaci\u00f3 \u00e9s inferior a 1"},

    { ER_INVALID_DRIVER_NAME,
     "S'ha especificat un nom de controlador no v\u00e0lid"},

    { ER_ERRORLISTENER,
     "ErrorListener"},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The name
//   'ElemTemplateElement' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_NO_TEMPLATE_PARENT,
     "Error del programador. L'expressi\u00f3 no t\u00e9 cap superior ElemTemplateElement "},


// Note to translators:  The following message should not normally be displayed
//   to users.  It describes a situation in which the processor has detected
//   an internal consistency problem in itself, and it provides this message
//   for the developer to help diagnose the problem.  The substitution text
//   provides further information in order to diagnose the problem.  The name
//   'RedundentExprEliminator' is the name of a class, and should not be
//   translated.
    { ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR,
     "L''afirmaci\u00f3 del programador a RedundentExprEliminator: {0}"},

    { ER_NOT_ALLOWED_IN_POSITION,
     "{0} no es permet en aquesta posici\u00f3 del full d''estil"},

    { ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION,
     "No es permet text sense espais en blanc en aquesta posici\u00f3 del full d'estil"},

  // This code is shared with warning codes.
  // SystemId Unknown
    { INVALID_TCHAR,
     "S''ha utilitzat un valor no perm\u00e8s {1} per a l''atribut CHAR {0}. Un atribut de tipus CHAR nom\u00e9s ha de contenir un car\u00e0cter."},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "QNAME" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value and {0} is the attribute name.
    //The following codes are shared with the warning codes...
    { INVALID_QNAME,
     "S''ha utilitzat un valor no perm\u00e8s {1} per a l''atribut QNAME {0}"},

    // Note to translators:  The following message is used if the value of
    // an attribute in a stylesheet is invalid.  "ENUM" is the XML data-type of
    // the attribute, and should not be translated.  The substitution text {1} is
    // the attribute value, {0} is the attribute name, and {2} is a list of valid
    // values.
    { INVALID_ENUM,
     "S''ha utilitzat un valor no perm\u00e8s {1} per a l''atribut ENUM {0}. Els valors v\u00e0lids s\u00f3n {2}."},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NMTOKEN" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NMTOKEN,
     "S''ha utilitzat un valor no perm\u00e8s {1} per a l''atribut NMTOKEN {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "NCNAME" is the XML data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_NCNAME,
     "S''ha utilitzat un valor no perm\u00e8s {1} per a l''atribut NCNAME {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "boolean" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
    { INVALID_BOOLEAN,
     "S''ha utilitzat un valor no perm\u00e8s {1} per a l''atribut boolean {0} "},

// Note to translators:  The following message is used if the value of
// an attribute in a stylesheet is invalid.  "number" is the XSLT data-type
// of the attribute, and should not be translated.  The substitution text {1} is
// the attribute value and {0} is the attribute name.
     { INVALID_NUMBER,
     "S''ha utilitzat un valor no perm\u00e8s {1} per a l''atribut number {0} "},


  // End of shared codes...

// Note to translators:  A "match pattern" is a special form of XPath expression
// that is used for matching patterns.  The substitution text is the name of
// a function.  The message indicates that when this function is referenced in
// a match pattern, its argument must be a string literal (or constant.)
// ER_ARG_LITERAL - new error message for bugzilla //5202
    { ER_ARG_LITERAL,
     "L''argument de {0} del patr\u00f3 de coincid\u00e8ncia ha de ser un literal."},

// Note to translators:  The following message indicates that two definitions of
// a variable.  A "global variable" is a variable that is accessible everywher
// in the stylesheet.
// ER_DUPLICATE_GLOBAL_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_GLOBAL_VAR,
     "La declaraci\u00f3 de variable global est\u00e0 duplicada."},


// Note to translators:  The following message indicates that two definitions of
// a variable were encountered.
// ER_DUPLICATE_VAR - new error message for bugzilla #790
    { ER_DUPLICATE_VAR,
     "La declaraci\u00f3 de variable est\u00e0 duplicada."},

    // Note to translators:  "xsl:template, "name" and "match" are XSLT keywords
    // which must not be translated.
    // ER_TEMPLATE_NAME_MATCH - new error message for bugzilla #789
    { ER_TEMPLATE_NAME_MATCH,
     "xsl:template ha de tenir un nom o un atribut de coincid\u00e8ncia (o tots dos)"},

    // Note to translators:  "exclude-result-prefixes" is an XSLT keyword which
    // should not be translated.  The message indicates that a namespace prefix
    // encountered as part of the value of the exclude-result-prefixes attribute
    // was in error.
    // ER_INVALID_PREFIX - new error message for bugzilla #788
    { ER_INVALID_PREFIX,
     "El prefix d''exclude-result-prefixes no \u00e9s v\u00e0lid: {0}"},

    // Note to translators:  An "attribute set" is a set of attributes that can
    // be added to an element in the output document as a group.  The message
    // indicates that there was a reference to an attribute set named {0} that
    // was never defined.
    // ER_NO_ATTRIB_SET - new error message for bugzilla #782
    { ER_NO_ATTRIB_SET,
     "attribute-set anomenat {0} no existeix"},

    // Note to translators:  This message indicates that there was a reference
    // to a function named {0} for which no function definition could be found.
    { ER_FUNCTION_NOT_FOUND,
     "La funci\u00f3 anomenada {0} no existeix"},

    // Note to translators:  This message indicates that the XSLT instruction
    // that is named by the substitution text {0} must not contain other XSLT
    // instructions (content) or a "select" attribute.  The word "select" is
    // an XSLT keyword in this case and must not be translated.
    { ER_CANT_HAVE_CONTENT_AND_SELECT,
     "L''element {0} no ha de tenir ni l''atribut content ni el select. "},

    // Note to translators:  This message indicates that the value argument
    // of setParameter must be a valid Java Object.
    { ER_INVALID_SET_PARAM_VALUE,
     "El valor del par\u00e0metre {0} ha de ser un objecte Java v\u00e0lid "},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT,
         "L'atribut result-prefix d'un element xsl:namespace-alias t\u00e9 el valor '#default', per\u00f2 no hi ha cap declaraci\u00f3 de l'espai de noms per defecte en l'\u00e0mbit de l'element "},

        { ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX,
         "L''atribut result-prefix d''un element xsl:namespace-alias t\u00e9 el valor ''{0}'', per\u00f2 no hi ha cap declaraci\u00f3 d''espai de noms per al prefix ''{0}'' en l''\u00e0mbit de l''element. "},

    { ER_SET_FEATURE_NULL_NAME,
      "El nom de la caracter\u00edstica no pot ser nul a TransformerFactory.setFeature(nom de la cadena, valor boole\u00e0). "},

    { ER_GET_FEATURE_NULL_NAME,
      "El nom de la caracter\u00edstica no pot ser nul a TransformerFactory.getFeature(nom de cadena). "},

    { ER_UNSUPPORTED_FEATURE,
      "No es pot establir la caracter\u00edstica ''{0}'' en aquesta TransformerFactory."},

    { ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING,
        "L''\u00fas de l''element d''extensi\u00f3 ''{0}'' no est\u00e0 perm\u00e8s, si la caracter\u00edstica de proc\u00e9s segur s''ha establert en true."},

        { ER_NAMESPACE_CONTEXT_NULL_NAMESPACE,
          "No es pot obtenir el prefix per a un URI de nom d'espais nul. "},

        { ER_NAMESPACE_CONTEXT_NULL_PREFIX,
          "No es pot obtenir l'URI del nom d'espais per a un prefix nul. "},

        { ER_XPATH_RESOLVER_NULL_QNAME,
          "El nom de la funci\u00f3 no pot ser nul. "},

        { ER_XPATH_RESOLVER_NEGATIVE_ARITY,
          "L'aritat no pot ser negativa."},

  // Warnings...

    { WG_FOUND_CURLYBRACE,
      "S'ha trobat '}' per\u00f2 no hi ha cap plantilla d'atribut oberta"},

    { WG_COUNT_ATTRIB_MATCHES_NO_ANCESTOR,
      "Av\u00eds: l''atribut de recompte no coincideix amb un antecessor d''xsl:number. Destinaci\u00f3 = {0}"},

    { WG_EXPR_ATTRIB_CHANGED_TO_SELECT,
      "Sintaxi antiga: El nom de l'atribut 'expr' s'ha canviat per 'select'."},

    { WG_NO_LOCALE_IN_FORMATNUMBER,
      "Xalan encara no pot gestionar el nom de l'entorn nacional a la funci\u00f3 format-number."},

    { WG_LOCALE_NOT_FOUND,
      "Av\u00eds: no s''ha trobat l''entorn nacional d''xml:lang={0}"},

    { WG_CANNOT_MAKE_URL_FROM,
      "No es pot crear la URL de: {0}"},

    { WG_CANNOT_LOAD_REQUESTED_DOC,
      "No es pot carregar el document sol\u00b7licitat: {0}"},

    { WG_CANNOT_FIND_COLLATOR,
      "No s''ha trobat el classificador de <sort xml:lang={0}"},

    { WG_FUNCTIONS_SHOULD_USE_URL,
      "Sintaxi antiga: la instrucci\u00f3 de funcions ha d''utilitzar una URL de {0}"},

    { WG_ENCODING_NOT_SUPPORTED_USING_UTF8,
      "Codificaci\u00f3 sense suport: {0}, s''utilitza UTF-8"},

    { WG_ENCODING_NOT_SUPPORTED_USING_JAVA,
      "Codificaci\u00f3 sense suport: {0}, s''utilitza Java {1}"},

    { WG_SPECIFICITY_CONFLICTS,
      "S''han trobat conflictes d''especificitat: {0} S''utilitzar\u00e0 el darrer trobat al full d''estils."},

    { WG_PARSING_AND_PREPARING,
      "========= S''est\u00e0 analitzant i preparant {0} =========="},

    { WG_ATTR_TEMPLATE,
     "Plantilla Attr, {0}"},

    { WG_CONFLICT_BETWEEN_XSLSTRIPSPACE_AND_XSLPRESERVESPACE,
      "S'ha produ\u00eft un conflicte de coincid\u00e8ncia entre xsl:strip-space i xsl:preserve-space"},

    { WG_ATTRIB_NOT_HANDLED,
      "Xalan encara no pot gestionar l''atribut {0}"},

    { WG_NO_DECIMALFORMAT_DECLARATION,
      "No s''ha trobat cap declaraci\u00f3 per al format decimal: {0}"},

    { WG_OLD_XSLT_NS,
     "Falta l'espai de noms XSLT o \u00e9s incorrecte. "},

    { WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED,
      "Nom\u00e9s es permet una declaraci\u00f3 xsl:decimal-format per defecte."},

    { WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE,
      "Els noms d''xsl:decimal-format han de ser exclusius. El nom \"{0}\" est\u00e0 duplicat."},

    { WG_ILLEGAL_ATTRIBUTE,
      "{0} t\u00e9 un atribut no perm\u00e8s: {1}"},

    { WG_COULD_NOT_RESOLVE_PREFIX,
      "No s''ha pogut resoldre el prefix d''espai de noms: {0}. Es passar\u00e0 per alt el node."},

    { WG_STYLESHEET_REQUIRES_VERSION_ATTRIB,
      "xsl:stylesheet necessita un atribut 'version'."},

    { WG_ILLEGAL_ATTRIBUTE_NAME,
      "El nom d''atribut no \u00e9s perm\u00e8s: {0}"},

    { WG_ILLEGAL_ATTRIBUTE_VALUE,
      "S''ha utilitzat un valor no perm\u00e8s a l''atribut {0}: {1}"},

    { WG_EMPTY_SECOND_ARG,
      "El conjunt de nodes resultant del segon argument de la funci\u00f3 document est\u00e0 buit. Torna un conjunt de nodes buit."},

  //Following are the new WARNING keys added in XALAN code base after Jdk 1.4 (Xalan 2.2-D11)

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.
    { WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
      "El valor de l'atribut 'name' del nom xsl:processing-instruction no ha de ser 'xml'"},

    // Note to translators:  "name" and "xsl:processing-instruction" are keywords
    // and must not be translated.  "NCName" is an XML data-type and must not be
    // translated.
    { WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
      "El valor de l''atribut ''name'' de xsl:processing-instruction ha de ser un NCName v\u00e0lid: {0}"},

    // Note to translators:  This message is reported if the stylesheet that is
    // being processed attempted to construct an XML document with an attribute in a
    // place other than on an element.  The substitution text specifies the name of
    // the attribute.
    { WG_ILLEGAL_ATTRIBUTE_POSITION,
      "No es pot afegir l''atribut {0} despr\u00e9s dels nodes subordinats o abans que es produeixi un element. Es passar\u00e0 per alt l''atribut."},

    { NO_MODIFICATION_ALLOWED_ERR,
      "S'ha intentat modificar un objecte on no es permeten modificacions. "
    },

    //Check: WHY THERE IS A GAP B/W NUMBERS in the XSLTErrorResources properties file?

  // Other miscellaneous text used inside the code...
  { "ui_language", "ca"},
  {  "help_language",  "ca" },
  {  "language",  "ca" },
  { "BAD_CODE", "El par\u00e0metre de createMessage estava fora dels l\u00edmits."},
  {  "FORMAT_FAILED", "S'ha generat una excepci\u00f3 durant la crida messageFormat."},
  {  "version", ">>>>>>> Versi\u00f3 Xalan "},
  {  "version2",  "<<<<<<<"},
  {  "yes", "s\u00ed"},
  { "line", "L\u00ednia n\u00fam."},
  { "column","Columna n\u00fam."},
  { "xsldone", "XSLProcessor: fet"},


  // Note to translators:  The following messages provide usage information
  // for the Xalan Process command line.  "Process" is the name of a Java class,
  // and should not be translated.
  { "xslProc_option", "Opcions de classe del proc\u00e9s de l\u00ednia d'ordres de Xalan-J:"},
  { "xslProc_option", "Opcions de classe del proc\u00e9s de l\u00ednia d'ordres de Xalan-J\u003a"},
  { "xslProc_invalid_xsltc_option", "L''opci\u00f3 {0} no t\u00e9 suport en modalitat XSLTC."},
  { "xslProc_invalid_xalan_option", "L''opci\u00f3 {0} nom\u00e9s es pot fer servir amb -XSLTC."},
  { "xslProc_no_input", "Error: no s'ha especificat cap full d'estils o xml d'entrada. Per obtenir les instruccions d'\u00fas, executeu aquesta ordre sense opcions."},
  { "xslProc_common_options", "-Opcions comuns-"},
  { "xslProc_xalan_options", "-Opcions per a Xalan-"},
  { "xslProc_xsltc_options", "-Opcions per a XSLTC-"},
  { "xslProc_return_to_continue", "(premeu <retorn> per continuar)"},

   // Note to translators: The option name and the parameter name do not need to
   // be translated. Only translate the messages in parentheses.  Note also that
   // leading whitespace in the messages is used to indent the usage information
   // for each option in the English messages.
   // Do not translate the keywords: XSLTC, SAX, DOM and DTM.
  { "optionXSLTC", "   [-XSLTC (Utilitza XSLTC per a la transformaci\u00f3)]"},
  { "optionIN", "   [-IN URL_XML_entrada]"},
  { "optionXSL", "   [-XSL URL_transformaci\u00f3_XSL]"},
  { "optionOUT",  "   [-OUT nom_fitxer_sortida]"},
  { "optionLXCIN", "   [-LXCIN entrada_nom_fitxer_full_estil_compilat]"},
  { "optionLXCOUT", "   [-LXCOUT sortida_nom_fitxer_full_estil_compilat]"},
  { "optionPARSER", "   [-PARSER nom de classe completament qualificat de la relaci\u00f3 de l'analitzador]"},
  {  "optionE", "   [-E (No amplia les refer\u00e8ncies d'entitat)]"},
  {  "optionV",  "   [-E (No amplia les refer\u00e8ncies d'entitat)]"},
  {  "optionQC", "   [-QC (Avisos de conflictes de patr\u00f3 redu\u00eft)]"},
  {  "optionQ", "   [-Q  (Modalitat redu\u00efda)]"},
  {  "optionLF", "   [-LF (Utilitza salts de l\u00ednia nom\u00e9s a la sortida {el valor per defecte \u00e9s CR/LF})]"},
  {  "optionCR", "   [-CR (Utilitza retorns de carro nom\u00e9s a la sortida {el valor per defecte \u00e9s CR/LF})]"},
  { "optionESCAPE", "   [-ESCAPE (Car\u00e0cters per aplicar un escapament {el valor per defecte \u00e9s <>&\"\'\\r\\n}]"},
  { "optionINDENT", "   [-INDENT (Controla quants espais tindr\u00e0 el sagnat {el valor per defecte \u00e9s 0})]"},
  { "optionTT", "   [-TT (Fa un rastreig de les plantilles a mesura que es criden.)]"},
  { "optionTG", "   [-TG (Fa un rastreig de cada un dels esdeveniments de generaci\u00f3.)]"},
  { "optionTS", "   [-TS (Fa un rastreig de cada un dels esdeveniments de selecci\u00f3.)]"},
  {  "optionTTC", "   [-TTC (Fa un rastreig dels subordinats de plantilla a mesura que es processen.)]"},
  { "optionTCLASS", "   [-TCLASS (Classe TraceListener per a extensions de rastreig.)]"},
  { "optionVALIDATE", "   [-VALIDATE (Estableix si es produeix la validaci\u00f3. Per defecte no est\u00e0 activada.)]"},
  { "optionEDUMP", "   [-EDUMP {nom de fitxer opcional} (Fer el buidatge de la pila si es produeix un error.)]"},
  {  "optionXML", "   [-XML (Utilitza el formatador XML i afegeix la cap\u00e7alera XML.)]"},
  {  "optionTEXT", "   [-TEXT (Utilitza el formatador de text simple.)]"},
  {  "optionHTML", "   [-HTML (Utilitza el formatador HTML.)]"},
  {  "optionPARAM", "   [-PARAM expressi\u00f3 del nom (Estableix un par\u00e0metre de full d'estils)]"},
  {  "noParsermsg1", "El proc\u00e9s XSL no ha estat correcte."},
  {  "noParsermsg2", "** No s'ha trobat l'analitzador **"},
  { "noParsermsg3",  "Comproveu la vostra classpath."},
  { "noParsermsg4", "Si no teniu XML Parser for Java d'IBM, el podeu baixar de l'indret web"},
  { "noParsermsg5", "AlphaWorks d'IBM: http://www.alphaworks.ibm.com/formula/xml"},
  { "optionURIRESOLVER", "   [-URIRESOLVER nom de classe complet (URIResolver que s'ha d'utilitzar per resoldre URI)]"},
  { "optionENTITYRESOLVER",  "   [-ENTITYRESOLVER nom de classe complet (EntityResolver que s'ha d'utilitzar per resoldre entitats)]"},
  { "optionCONTENTHANDLER",  "   [-CONTENTHANDLER nom de classe complet (ContentHandler que s'ha d'utilitzar per serialitzar la sortida)]"},
  {  "optionLINENUMBERS",  "   [-L utilitza els n\u00fameros de l\u00ednia del document origen]"},
  { "optionSECUREPROCESSING", "   [-SECURE (estableix la caracter\u00edstica de proc\u00e9s segur en true.)]"},

    // Following are the new options added in XSLTErrorResources.properties files after Jdk 1.4 (Xalan 2.2-D11)


  {  "optionMEDIA",  "   [-MEDIA mediaType (utilitza l'atribut media per trobar un full d'estils relacionat amb un document.)]"},
  {  "optionFLAVOR",  "   [-FLAVOR nom_flavor (utilitza expl\u00edcitament s2s=SAX o d2d=DOM per fer una transformaci\u00f3.)] "}, // Added by sboag/scurcuru; experimental
  { "optionDIAG", "   [-DIAG (Imprimex els mil\u00b7lisegons en total que ha trigat la transformaci\u00f3.)]"},
  { "optionINCREMENTAL",  "   [-INCREMENTAL (sol\u00b7licita la construcci\u00f3 de DTM incremental establint http://xml.apache.org/xalan/features/incremental en true.)]"},
  {  "optionNOOPTIMIMIZE",  "   [-NOOPTIMIMIZE (sol\u00b7licita que no es processi l'optimitzaci\u00f3 de full d'estils establint http://xml.apache.org/xalan/features/optimize en false.)]"},
  { "optionRL",  "   [-RL recursionlimit (confirma el l\u00edmit num\u00e8ric de la profunditat de recursivitat del full d'estils.)]"},
  {   "optionXO",  "   [-XO [nom_translet] (assigna el nom al translet generat)]"},
  {  "optionXD", "   [-XD directori_destinaci\u00f3 (especifica un directori de destinaci\u00f3 per al translet)]"},
  {  "optionXJ",  "   [-XJ fitxer_jar (empaqueta les classes de translet en un fitxer jar amb el nom <fitxer_jar>)]"},
  {   "optionXP",  "   [-XP paquet (especifica un prefix de nom de paquet per a totes les classes de translet generades)]"},

  //AddITIONAL  STRINGS that need L10n
  // Note to translators:  The following message describes usage of a particular
  // command-line option that is used to enable the "template inlining"
  // optimization.  The optimization involves making a copy of the code
  // generated for a template in another template that refers to it.
  { "optionXN",  "   [-XN (habilita l'inlining de plantilles)]" },
  { "optionXX",  "   [-XX (activa la sortida de missatges de depuraci\u00f3 addicionals)]"},
  { "optionXT" , "   [-XT (utilitza el translet per a la transformaci\u00f3 si \u00e9s possible)]"},
  { "diagTiming"," --------- La transformaci\u00f3 de {0} mitjan\u00e7ant {1} ha trigat {2} ms" },
  { "recursionTooDeep","La imbricaci\u00f3 de plantilles t\u00e9 massa nivells. Imbricaci\u00f3 = {0}, plantilla{1} {2}" },
  { "nameIs", "el nom \u00e9s" },
  { "matchPatternIs", "el patr\u00f3 de coincid\u00e8ncia \u00e9s" }

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
  public static final String ERROR_HEADER = "Error: ";

  /** String to prepend to warning messages.    */
  public static final String WARNING_HEADER = "Av\u00eds: ";

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
