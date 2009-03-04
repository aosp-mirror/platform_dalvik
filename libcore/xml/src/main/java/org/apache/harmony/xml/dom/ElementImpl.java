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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
 */
public class ElementImpl extends InnerNodeImpl implements Element {

    private boolean namespaceAware;
    
    private String namespaceURI;

    private String prefix;
    
    private String localName;

    private List<AttrImpl> attributes = new ArrayList<AttrImpl>();

    ElementImpl(DocumentImpl document, String namespaceURI, String qualifiedName) {
        super(document);

        this.namespaceAware = true;
        this.namespaceURI = namespaceURI;

        if (qualifiedName == null || "".equals(qualifiedName)) {
            throw new DOMException(DOMException.NAMESPACE_ERR, qualifiedName);
        }
        
        int p = qualifiedName.lastIndexOf(":");
        if (p != -1) {
            setPrefix(qualifiedName.substring(0, p));
            qualifiedName = qualifiedName.substring(p + 1);
        }
        
        if (!document.isXMLIdentifier(qualifiedName)) {
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, qualifiedName);
        }
            
        this.localName = qualifiedName;
    }

    ElementImpl(DocumentImpl document, String name) {
        super(document);

        this.namespaceAware = false;
        
        int p = name.lastIndexOf(":");
        if (p != -1) {
            String prefix = name.substring(0, p);
            String localName = name.substring(p + 1);
            
            if (!document.isXMLIdentifier(prefix) || !document.isXMLIdentifier(localName)) {
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, name);
            }
        } else {
            if (!document.isXMLIdentifier(name)) {
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, name);
            }
        }
        
        this.localName = name;
    }

    private int indexOfAttribute(String name) {
        for (int i = 0; i < attributes.size(); i++) {
            AttrImpl attr = attributes.get(i);
            if (attr.matchesName(name, false)) {
                return i;
            }
        }
        
        return -1;
    }
    
    private int indexOfAttributeNS(String namespaceURI, String localName) {
        for (int i = 0; i < attributes.size(); i++) {
            AttrImpl attr = attributes.get(i);
            if (attr.matchesNameNS(namespaceURI, localName, false)) {
                return i;
            }
        }
        
        return -1;
    }
    
    public String getAttribute(String name) {
        Attr attr = getAttributeNode(name);

        if (attr == null) {
            return "";
        }

        return attr.getValue();
    }

    public String getAttributeNS(String namespaceURI, String localName) {
        Attr attr = getAttributeNodeNS(namespaceURI, localName);

        if (attr == null) {
            return "";
        }

        return attr.getValue();
    }

    public Attr getAttributeNode(String name) {
        int i = indexOfAttribute(name);
        
        if (i == -1) {
            return null;
        }
        
        return attributes.get(i);
    }

    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        int i = indexOfAttributeNS(namespaceURI, localName);
        
        if (i == -1) {
            return null;
        }
        
        return attributes.get(i);
    }

    @Override
    public NamedNodeMap getAttributes() {
        return new ElementAttrNamedNodeMapImpl();
    }
    
    Element getElementById(String name) {
        if (name.equals(getAttribute("id"))) {
            return this;
        }

        for (NodeImpl node : children) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = ((ElementImpl) node).getElementById(name);
                if (element != null) {
                    return element;
                }
            }
        }

        return null;
    }

    public NodeList getElementsByTagName(String name) {
        NodeListImpl list = new NodeListImpl();
        getElementsByTagName(list, name);
        return list;
    }

    void getElementsByTagName(NodeListImpl list, String name) {
        if (matchesName(name, true)) {
            list.add(this);
        }

        for (NodeImpl node : children) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ((ElementImpl) node).getElementsByTagName(list, name);
            }
        }
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        NodeListImpl list = new NodeListImpl();
        getElementsByTagNameNS(list, namespaceURI, localName);
        return list;
    }

    void getElementsByTagNameNS(NodeListImpl list, String namespaceURI,
            String localName) {
        if (matchesNameNS(namespaceURI, localName, true)) {
            list.add(this);
        }
        
        for (NodeImpl node : children) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ((ElementImpl) node).getElementsByTagNameNS(list, namespaceURI,
                        localName);
            }
        }
    }

    @Override
    public String getLocalName() {
        return namespaceAware ? localName : null;
    }

    @Override
    public String getNamespaceURI() {
        return namespaceURI;
    }

    @Override
    public String getNodeName() {
        return getTagName();
    }

    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    public String getTagName() {
        return (prefix != null ? prefix + ":" : "") + localName;
    }

    public boolean hasAttribute(String name) {
        return indexOfAttribute(name) != -1;
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return indexOfAttributeNS(namespaceURI, localName) != -1;
    }

    @Override
    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    public void removeAttribute(String name) throws DOMException {
        int i = indexOfAttribute(name);
        
        if (i != -1) {
            attributes.remove(i);
        }
    }

    public void removeAttributeNS(String namespaceURI, String localName)
            throws DOMException {
        int i = indexOfAttributeNS(namespaceURI, localName);
        
        if (i != -1) {
            attributes.remove(i);
        }
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        AttrImpl oldAttrImpl = (AttrImpl) oldAttr;

        if (oldAttrImpl.getOwnerElement() != this) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, null);
        }

        attributes.remove(oldAttr);
        oldAttrImpl.ownerElement = null;

        return oldAttrImpl;
    }

    public void setAttribute(String name, String value) throws DOMException {
        Attr attr = getAttributeNode(name);

        if (attr == null) {
            attr = document.createAttribute(name);
            setAttributeNode(attr);
        }

        attr.setValue(value);
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName,
            String value) throws DOMException {
        Attr attr = getAttributeNodeNS(namespaceURI, qualifiedName);

        if (attr == null) {
            attr = document.createAttributeNS(namespaceURI, qualifiedName);
            setAttributeNodeNS(attr);
        }

        attr.setValue(value);
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        AttrImpl newAttrImpl = (AttrImpl) newAttr;
        
        if (newAttrImpl.document != this.getOwnerDocument()) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }

        if (newAttrImpl.getOwnerElement() != null) {
            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, null);
        }

        AttrImpl oldAttrImpl = null;
        
        int i = indexOfAttribute(newAttr.getName());
        if (i != -1) {
            oldAttrImpl = attributes.get(i);
            attributes.remove(i);
        }
        
        attributes.add(newAttrImpl);
        newAttrImpl.ownerElement = this;

        return oldAttrImpl;
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        AttrImpl newAttrImpl = (AttrImpl) newAttr;

        if (newAttrImpl.document != this.getOwnerDocument()) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }

        if (newAttrImpl.getOwnerElement() != null) {
            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, null);
        }

        AttrImpl oldAttrImpl = null;
        
        int i = indexOfAttributeNS(newAttr.getNamespaceURI(), newAttr.getLocalName());
        if (i != -1) {
            oldAttrImpl = attributes.get(i);
            attributes.remove(i);
        }
        
        attributes.add(newAttrImpl);
        newAttrImpl.ownerElement = this;

        return oldAttrImpl;
    }

    @Override
    public void setPrefix(String prefix) {
        if (!namespaceAware) {
            throw new DOMException(DOMException.NAMESPACE_ERR, prefix);
        }
        
        if (prefix != null) {
            if (namespaceURI == null || !document.isXMLIdentifier(prefix)) {
                throw new DOMException(DOMException.NAMESPACE_ERR, prefix);
            }
            
            if ("xml".equals(prefix) && !"http://www.w3.org/XML/1998/namespace".equals(namespaceURI)) {
                throw new DOMException(DOMException.NAMESPACE_ERR, prefix);
            }
        }
        
        this.prefix = prefix;
    }
    
    public class ElementAttrNamedNodeMapImpl implements NamedNodeMap {

        public int getLength() {
            return ElementImpl.this.attributes.size();
        }

        private int indexOfItem(String name) {
            return ElementImpl.this.indexOfAttribute(name);
        }
        
        private int indexOfItemNS(String namespaceURI, String localName) {
            return ElementImpl.this.indexOfAttributeNS(namespaceURI, localName);
        }

        public Node getNamedItem(String name) {
            return ElementImpl.this.getAttributeNode(name);
        }

        public Node getNamedItemNS(String namespaceURI, String localName) {
            return ElementImpl.this.getAttributeNodeNS(namespaceURI, localName);
        }

        public Node item(int index) {
            return ElementImpl.this.attributes.get(index);
        }

        public Node removeNamedItem(String name) throws DOMException {
            int i = indexOfItem(name);
            
            if (i == -1) {
                throw new DOMException(DOMException.NOT_FOUND_ERR, null);
            }

            return ElementImpl.this.attributes.remove(i);
        }

        public Node removeNamedItemNS(String namespaceURI, String localName)
                throws DOMException {
            int i = indexOfItemNS(namespaceURI, localName);
            
            if (i == -1) {
                throw new DOMException(DOMException.NOT_FOUND_ERR, null);
            }

            return ElementImpl.this.attributes.remove(i);
        }

        public Node setNamedItem(Node arg) throws DOMException {
            if (!(arg instanceof Attr)) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
            }

            return ElementImpl.this.setAttributeNode((Attr)arg);
        }

        public Node setNamedItemNS(Node arg) throws DOMException {
            if (!(arg instanceof Attr)) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
            }
            
            return ElementImpl.this.setAttributeNodeNS((Attr)arg);
        }
    }
    
}
