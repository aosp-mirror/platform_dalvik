/*
 * Copyright (C) 2007 The Android Open Source Project
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

package org.apache.harmony.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides a straightforward implementation of the corresponding W3C DOM
 * interface. The class is used internally only, thus only notable members that
 * are not in the original interface are documented (the W3C docs are quite
 * extensive). Hope that's ok.
 * <p>
 * Some of the fields may have package visibility, so other classes belonging to
 * the DOM implementation can easily access them while maintaining the DOM tree
 * structure.
 * <p>
 * This class represents a Node that has neither a parent nor children.
 */
public abstract class NodeImpl implements Node {

    private static final NodeList EMPTY_LIST = new NodeListImpl();
    
    // Maintained by InnerNodeImpl and ElementImpl.
    DocumentImpl document;

    NodeImpl(DocumentImpl document) {
        this.document = document;
    }

    public Node appendChild(Node newChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
    }

    public Node cloneNode(boolean deep) {
        return document.cloneNode(this, deep);
    }

    public NamedNodeMap getAttributes() {
        return null;
    }

    public NodeList getChildNodes() {
        return EMPTY_LIST;
    }

    public Node getFirstChild() {
        return null;
    }

    public Node getLastChild() {
        return null;
    }

    public String getLocalName() {
        return null;
    }

    public String getNamespaceURI() {
        return null;
    }

    public Node getNextSibling() {
        return null;
    }

    public String getNodeName() {
        return null;
    }

    public abstract short getNodeType();

    public String getNodeValue() throws DOMException {
        return null;
    }

    public Document getOwnerDocument() {
        return document;
    }

    public Node getParentNode() {
        return null;
    }

    public String getPrefix() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public boolean hasAttributes() {
        return false;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
    }

    public boolean isSupported(String feature, String version) {
        return DOMImplementationImpl.getInstance().hasFeature(feature, version);
    }

    public void normalize() {
    }

    public Node removeChild(Node oldChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
    }

    public void setNodeValue(String nodeValue) throws DOMException {
    }

    public void setPrefix(String prefix) throws DOMException {
    }

    /**
     * Checks whether a required string matches an actual string. This utility
     * method is used for comparing namespaces and such. It takes into account
     * null arguments and the "*" special case.
     * 
     * @param required The required string.
     * @param actual The actual string.
     * @return True if and only if the actual string matches the required one.
     */
    private static boolean matchesName(String required, String actual, boolean wildcard) {
        if (wildcard && "*".equals(required)) {
            return true;
        }
        
        if (required == null) {
            return (actual == null);
        }
        
        return required.equals(actual);
    }

    /**
     * Checks whether this node's name matches a required name. It takes into
     * account null arguments and the "*" special case.
     * 
     * @param name The required name.
     * @param wildcard TODO
     * @return True if and only if the actual name matches the required one.
     */
    public boolean matchesName(String name, boolean wildcard) {
        return matchesName(name, getNodeName(), wildcard);
    }

    /**
     * Checks whether this node's namespace and local name match a required
     * pair of namespace and local name. It takes into account null arguments
     * and the "*" special case.
     *
     * @param namespaceURI The required namespace.
     * @param localName The required local name.
     * @param wildcard TODO
     * @return True if and only if the actual namespace and local name match
     *         the required pair of namespace and local name.
     */
    public boolean matchesNameNS(String namespaceURI, String localName, boolean wildcard) {
        return matchesName(namespaceURI, getNamespaceURI(), wildcard) && matchesName(localName, getLocalName(), wildcard);
    }
    
}
