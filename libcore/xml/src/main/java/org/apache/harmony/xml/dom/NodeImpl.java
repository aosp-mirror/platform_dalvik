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

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.UserDataHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * A straightforward implementation of the corresponding W3C DOM node.
 *
 * <p>Some fields have package visibility so other classes can access them while
 * maintaining the DOM structure.
 *
 * <p>This class represents a Node that has neither a parent nor children.
 * Subclasses may have either.
 *
 * <p>Some code was adapted from Apache Xerces.
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

    public final void setNodeValue(String nodeValue) throws DOMException {
        switch (getNodeType()) {
            case CDATA_SECTION_NODE:
            case COMMENT_NODE:
            case TEXT_NODE:
                ((CharacterData) this).setData(nodeValue);
                return;

            case PROCESSING_INSTRUCTION_NODE:
                ((ProcessingInstruction) this).setData(nodeValue);
                return;

            case ATTRIBUTE_NODE:
                ((Attr) this).setValue(nodeValue);
                return;

            case ELEMENT_NODE:
            case ENTITY_REFERENCE_NODE:
            case ENTITY_NODE:
            case DOCUMENT_NODE:
            case DOCUMENT_TYPE_NODE:
            case DOCUMENT_FRAGMENT_NODE:
            case NOTATION_NODE:
                return; // do nothing!

            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported node type " + getNodeType());
        }
    }

    public void setPrefix(String prefix) throws DOMException {
    }

    /**
     * Validates the element or attribute namespace prefix on this node.
     *
     * @param namespaceAware whether this node is namespace aware
     * @param namespaceURI this node's namespace URI
     */
    protected String validatePrefix(String prefix, boolean namespaceAware, String namespaceURI) {
        if (!namespaceAware) {
            throw new DOMException(DOMException.NAMESPACE_ERR, prefix);
        }

        if (prefix != null) {
            if (namespaceURI == null
                    || !DocumentImpl.isXMLIdentifier(prefix)
                    || "xml".equals(prefix) && !"http://www.w3.org/XML/1998/namespace".equals(namespaceURI)
                    || "xmlns".equals(prefix) && !"http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
                throw new DOMException(DOMException.NAMESPACE_ERR, prefix);
            }
        }

        return prefix;
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

    public String getBaseURI() {
        /*
         * TODO: implement. For reference, here's Xerces' behaviour:
         *
         * In all cases, the returned URI should be sanitized before it is
         * returned. If the URI is malformed, null should be returned instead.
         *
         * For document nodes, this should return a member field that's
         * initialized by the parser.
         *
         * For element nodes, this should first look for the xml:base attribute.
         *   if that exists and is absolute, it should be returned.
         *   if that exists and is relative, it should be resolved to the parent's base URI
         *   if it doesn't exist, the parent's baseURI should be returned
         *
         * For entity nodes, if a base URI exists that should be returned.
         * Otherwise the document's base URI should be returned
         *
         * For entity references, if a base URI exists that should be returned
         * otherwise it dereferences the entity (via the document) and uses the
         * entity's base URI.
         *
         * For notations, it returns the base URI field.
         *
         * For processing instructions, it returns the parent's base URI.
         *
         * For all other node types, it returns null.
         */
        return null;
    }

    public short compareDocumentPosition(Node other)
            throws DOMException {
        throw new UnsupportedOperationException(); // TODO
    }

    public String getTextContent() throws DOMException {
        return getNodeValue();
    }

    void getTextContent(StringBuilder buf) throws DOMException {
        String content = getNodeValue();
        if (content != null) {
            buf.append(content);
        }
    }

    public final void setTextContent(String textContent) throws DOMException {
        switch (getNodeType()) {
            case DOCUMENT_TYPE_NODE:
            case DOCUMENT_NODE:
                return; // do nothing!

            case ELEMENT_NODE:
            case ENTITY_NODE:
            case ENTITY_REFERENCE_NODE:
            case DOCUMENT_FRAGMENT_NODE:
                // remove all existing children
                Node child;
                while ((child = getFirstChild()) != null) {
                    removeChild(child);
                }
                // create a text node to hold the given content
                if (textContent != null && textContent.length() != 0){
                    appendChild(getOwnerDocument().createTextNode(textContent));
                }
                return;

            case ATTRIBUTE_NODE:
            case TEXT_NODE:
            case CDATA_SECTION_NODE:
            case PROCESSING_INSTRUCTION_NODE:
            case COMMENT_NODE:
            case NOTATION_NODE:
                setNodeValue(textContent);
                return;

            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported node type " + getNodeType());
        }
    }

    public boolean isSameNode(Node other) {
        return this == other;
    }

    /**
     * Returns the element whose namespace definitions apply to this node. Use
     * this element when mapping prefixes to URIs and vice versa.
     */
    private NodeImpl getNamespacingElement() {
        switch (this.getNodeType()) {
            case ELEMENT_NODE:
                return this;

            case DOCUMENT_NODE:
                return (NodeImpl) ((Document) this).getDocumentElement();

            case ENTITY_NODE:
            case NOTATION_NODE:
            case DOCUMENT_FRAGMENT_NODE:
            case DOCUMENT_TYPE_NODE:
                return null;

            case ATTRIBUTE_NODE:
                return (NodeImpl) ((Attr) this).getOwnerElement();

            case TEXT_NODE:
            case CDATA_SECTION_NODE:
            case ENTITY_REFERENCE_NODE:
            case PROCESSING_INSTRUCTION_NODE:
            case COMMENT_NODE:
                return getContainingElement();

            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported node type " + getNodeType());
        }
    }

    /**
     * Returns the nearest ancestor element that contains this node.
     */
    private NodeImpl getContainingElement() {
        for (Node p = getParentNode(); p != null; p = p.getParentNode()) {
            if (p.getNodeType() == ELEMENT_NODE) {
                return (NodeImpl) p;
            }
        }
        return null;
    }

    public final String lookupPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            return null;
        }

        // the XML specs define some prefixes (like "xml" and "xmlns") but this
        // API is explicitly defined to ignore those.

        NodeImpl target = getNamespacingElement();
        for (NodeImpl node = target; node != null; node = node.getContainingElement()) {
            // check this element's namespace first
            if (namespaceURI.equals(node.getNamespaceURI())
                    && target.isPrefixMappedToUri(node.getPrefix(), namespaceURI)) {
                return node.getPrefix();
            }

            // search this element for an attribute of this form:
            //   xmlns:foo="http://namespaceURI"
            if (!node.hasAttributes()) {
                continue;
            }
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0, length = attributes.getLength(); i < length; i++) {
                Node attr = attributes.item(i);
                if (!"http://www.w3.org/2000/xmlns/".equals(attr.getNamespaceURI())
                        || !"xmlns".equals(attr.getPrefix())
                        || !namespaceURI.equals(attr.getNodeValue())) {
                    continue;
                }
                if (target.isPrefixMappedToUri(attr.getLocalName(), namespaceURI)) {
                    return attr.getLocalName();
                }
            }
        }

        return null;
    }

    /**
     * Returns true if the given prefix is mapped to the given URI on this
     * element. Since child elements can redefine prefixes, this check is
     * necessary: {@code
     * <foo xmlns:a="http://good">
     *   <bar xmlns:a="http://evil">
     *     <a:baz />
     *   </bar>
     * </foo>}
     *
     * @param prefix the prefix to find. Nullable.
     * @param uri the URI to match. Non-null.
     */
    boolean isPrefixMappedToUri(String prefix, String uri) {
        if (prefix == null) {
            return false;
        }

        String actual = lookupNamespaceURI(prefix);
        return uri.equals(actual);
    }

    public final boolean isDefaultNamespace(String namespaceURI) {
        String actual = lookupNamespaceURI(null); // null yields the default namespace
        return namespaceURI == null
                ? actual == null
                : namespaceURI.equals(actual);
    }

    public final String lookupNamespaceURI(String prefix) {
        NodeImpl target = getNamespacingElement();
        for (NodeImpl node = target; node != null; node = node.getContainingElement()) {
            // check this element's namespace first
            String nodePrefix = node.getPrefix();
            if (node.getNamespaceURI() != null) {
                if (prefix == null // null => default prefix
                        ? nodePrefix == null
                        : prefix.equals(nodePrefix)) {
                    return node.getNamespaceURI();
                }
            }

            // search this element for an attribute of the appropriate form.
            //    default namespace: xmlns="http://resultUri"
            //          non default: xmlns:specifiedPrefix="http://resultUri"
            if (!node.hasAttributes()) {
                continue;
            }
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0, length = attributes.getLength(); i < length; i++) {
                Node attr = attributes.item(i);
                if (!"http://www.w3.org/2000/xmlns/".equals(attr.getNamespaceURI())) {
                    continue;
                }
                if (prefix == null // null => default prefix
                        ? "xmlns".equals(attr.getNodeName())
                        : "xmlns".equals(attr.getPrefix()) && prefix.equals(attr.getLocalName())) {
                    String value = attr.getNodeValue();
                    return value.length() > 0 ? value : null;
                }
            }
        }

        return null;
    }

    /**
     * Returns a list of objects such that two nodes are equal if their lists
     * are equal. Be careful: the lists may contain NamedNodeMaps and Nodes,
     * neither of which override Object.equals(). Such values must be compared
     * manually.
     */
    private static List<Object> createEqualityKey(Node node) {
        List<Object> values = new ArrayList<Object>();
        values.add(node.getNodeType());
        values.add(node.getNodeName());
        values.add(node.getLocalName());
        values.add(node.getNamespaceURI());
        values.add(node.getPrefix());
        values.add(node.getNodeValue());
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            values.add(child);
        }

        switch (node.getNodeType()) {
            case DOCUMENT_TYPE_NODE:
                DocumentTypeImpl doctype = (DocumentTypeImpl) node;
                values.add(doctype.getPublicId());
                values.add(doctype.getSystemId());
                values.add(doctype.getInternalSubset());
                values.add(doctype.getEntities());
                values.add(doctype.getNotations());
                break;

            case ELEMENT_NODE:
                Element element = (Element) node;
                values.add(element.getAttributes());
                break;
        }

        return values;
    }

    public final boolean isEqualNode(Node arg) {
        if (arg == this) {
            return true;
        }

        List<Object> listA = createEqualityKey(this);
        List<Object> listB = createEqualityKey(arg);

        if (listA.size() != listB.size()) {
            return false;
        }

        for (int i = 0; i < listA.size(); i++) {
            Object a = listA.get(i);
            Object b = listB.get(i);

            if (a == b) {
                continue;

            } else if (a == null || b == null) {
                return false;

            } else if (a instanceof String || a instanceof Short) {
                if (!a.equals(b)) {
                    return false;
                }

            } else if (a instanceof NamedNodeMap) {
                if (!(b instanceof NamedNodeMap)
                        || !namedNodeMapsEqual((NamedNodeMap) a, (NamedNodeMap) b)) {
                    return false;
                }

            } else if (a instanceof Node) {
                if (!(b instanceof Node)
                        || !((Node) a).isEqualNode((Node) b)) {
                    return false;
                }

            } else {
                throw new AssertionError(); // unexpected type
            }
        }
        
        return true;
    }

    private boolean namedNodeMapsEqual(NamedNodeMap a, NamedNodeMap b) {
        if (a.getLength() != b.getLength()) {
            return false;
        }
        for (int i = 0; i < a.getLength(); i++) {
            Node aNode = a.item(i);
            Node bNode = aNode.getLocalName() == null
                    ? b.getNamedItem(aNode.getNodeName())
                    : b.getNamedItemNS(aNode.getNamespaceURI(), aNode.getLocalName());
            if (bNode == null || !aNode.isEqualNode(bNode)) {
                return false;
            }
        }
        return true;
    }

    public final Object getFeature(String feature, String version) {
        return isSupported(feature, version) ? this : null;
    }

    public Object setUserData(String key, Object data,
            UserDataHandler handler) {
        throw new UnsupportedOperationException(); // TODO
    }

    public Object getUserData(String key) {
        throw new UnsupportedOperationException(); // TODO
    }
}
