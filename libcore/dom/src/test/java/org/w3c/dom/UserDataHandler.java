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
public interface UserDataHandler {
    // OperationType
    /**
     * The node is cloned, using <code>Node.cloneNode()</code>.
     */
    public static final short NODE_CLONED               = 1;
    /**
     * The node is imported, using <code>Document.importNode()</code>.
     */
    public static final short NODE_IMPORTED             = 2;
    /**
     * The node is deleted.
     * <p ><b>Note:</b> This may not be supported or may not be reliable in 
     * certain environments, such as Java, where the implementation has no 
     * real control over when objects are actually deleted.
     */
    public static final short NODE_DELETED              = 3;
    /**
     * The node is renamed, using <code>Document.renameNode()</code>.
     */
    public static final short NODE_RENAMED              = 4;
    /**
     * The node is adopted, using <code>Document.adoptNode()</code>.
     */
    public static final short NODE_ADOPTED              = 5;

    /**
     * This method is called whenever the node for which this handler is 
     * registered is imported or cloned.
     * <br> DOM applications must not raise exceptions in a 
     * <code>UserDataHandler</code>. The effect of throwing exceptions from 
     * the handler is DOM implementation dependent. 
     * @param operation Specifies the type of operation that is being 
     *   performed on the node.
     * @param key Specifies the key for which this handler is being called. 
     * @param data Specifies the data for which this handler is being called. 
     * @param src Specifies the node being cloned, adopted, imported, or 
     *   renamed. This is <code>null</code> when the node is being deleted.
     * @param dst Specifies the node newly created if any, or 
     *   <code>null</code>.
     */
    public void handle(short operation, 
                       String key, 
                       Object data, 
                       Node src, 
                       Node dst);

}
