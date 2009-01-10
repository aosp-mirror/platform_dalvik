/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.w3c.dom;
public interface DOMError {
    // ErrorSeverity
    /**
     * The severity of the error described by the <code>DOMError</code> is 
     * warning. A <code>SEVERITY_WARNING</code> will not cause the 
     * processing to stop, unless <code>DOMErrorHandler.handleError()</code> 
     * returns <code>false</code>.
     */
    public static final short SEVERITY_WARNING          = 1;
    /**
     * The severity of the error described by the <code>DOMError</code> is 
     * error. A <code>SEVERITY_ERROR</code> may not cause the processing to 
     * stop if the error can be recovered, unless 
     * <code>DOMErrorHandler.handleError()</code> returns <code>false</code>.
     */
    public static final short SEVERITY_ERROR            = 2;
    /**
     * The severity of the error described by the <code>DOMError</code> is 
     * fatal error. A <code>SEVERITY_FATAL_ERROR</code> will cause the 
     * normal processing to stop. The return value of 
     * <code>DOMErrorHandler.handleError()</code> is ignored unless the 
     * implementation chooses to continue, in which case the behavior 
     * becomes undefined.
     */
    public static final short SEVERITY_FATAL_ERROR      = 3;

    /**
     * The severity of the error, either <code>SEVERITY_WARNING</code>, 
     * <code>SEVERITY_ERROR</code>, or <code>SEVERITY_FATAL_ERROR</code>.
     */
    public short getSeverity();

    /**
     * An implementation specific string describing the error that occurred.
     */
    public String getMessage();

    /**
     *  A <code>DOMString</code> indicating which related data is expected in 
     * <code>relatedData</code>. Users should refer to the specification of 
     * the error in order to find its <code>DOMString</code> type and 
     * <code>relatedData</code> definitions if any. 
     * <p ><b>Note:</b>  As an example, 
     * <code>Document.normalizeDocument()</code> does generate warnings when 
     * the "split-cdata-sections" parameter is in use. Therefore, the method 
     * generates a <code>SEVERITY_WARNING</code> with <code>type</code> 
     * <code>"cdata-sections-splitted"</code> and the first 
     * <code>CDATASection</code> node in document order resulting from the 
     * split is returned by the <code>relatedData</code> attribute. 
     */
    public String getType();

    /**
     * The related platform dependent exception if any.
     */
    public Object getRelatedException();

    /**
     *  The related <code>DOMError.type</code> dependent data if any. 
     */
    public Object getRelatedData();

    /**
     * The location of the error.
     */
    public DOMLocator getLocation();

}