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
public interface DOMErrorHandler {
    /**
     * This method is called on the error handler when an error occurs.
     * <br> If an exception is thrown from this method, it is considered to be 
     * equivalent of returning <code>true</code>. 
     * @param error  The error object that describes the error. This object 
     *   may be reused by the DOM implementation across multiple calls to 
     *   the <code>handleError</code> method. 
     * @return  If the <code>handleError</code> method returns 
     *   <code>false</code>, the DOM implementation should stop the current 
     *   processing when possible. If the method returns <code>true</code>, 
     *   the processing may continue depending on 
     *   <code>DOMError.severity</code>. 
     */
    public boolean handleError(DOMError error);

}