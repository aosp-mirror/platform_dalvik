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

/**
 * <code>DOMLocator</code> is an interface that describes a location (e.g. 
 * where an error occurred).
 * <p>See also the <a href='http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407'>Document Object Model (DOM) Level 3 Core Specification</a>.
 * @since DOM Level 3
 */
public interface DOMLocator {
    /**
     * The line number this locator is pointing to, or <code>-1</code> if 
     * there is no column number available.
     */
    public int getLineNumber();

    /**
     * The column number this locator is pointing to, or <code>-1</code> if 
     * there is no column number available.
     */
    public int getColumnNumber();

    /**
     * The byte offset into the input source this locator is pointing to or 
     * <code>-1</code> if there is no byte offset available.
     */
    public int getByteOffset();

    /**
     * The UTF-16, as defined in [Unicode] and Amendment 1 of [ISO/IEC 10646], offset into the input source this locator is pointing to or 
     * <code>-1</code> if there is no UTF-16 offset available.
     */
    public int getUtf16Offset();

    /**
     * The node this locator is pointing to, or <code>null</code> if no node 
     * is available.
     */
    public Node getRelatedNode();

    /**
     * The URI this locator is pointing to, or <code>null</code> if no URI is 
     * available.
     */
    public String getUri();

}
