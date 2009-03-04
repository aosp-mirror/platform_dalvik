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
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

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
public class DocumentImpl extends InnerNodeImpl implements Document {

    private DOMImplementation domImplementation;

    DocumentImpl(DOMImplementationImpl impl, String namespaceURI,
            String qualifiedName, DocumentType doctype) {
        super(null);

        this.domImplementation = impl;
        // this.document = this;
        
        if (doctype != null) {
            appendChild(doctype);
        }

        if (qualifiedName != null) {
            appendChild(createElementNS(namespaceURI, qualifiedName));
        }
    }

    private static boolean isXMLIdentifierStart(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c == '_');
    }

    private static boolean isXMLIdentifierPart(char c) {
        return isXMLIdentifierStart(c) || (c >= '0' && c <= '9') || (c == '-') || (c == '.');
    }

    static boolean isXMLIdentifier(String s) {
        if (s.length() == 0) {
            return false;
        }
        
        if (!isXMLIdentifierStart(s.charAt(0))) {
            return false;
        }
        
        for (int i = 1; i < s.length(); i++) {
            if (!isXMLIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Clones a node and (if requested) its children. The source node(s) may
     * have been created by a different DocumentImpl or even DOM implementation.
     * 
     * @param node The node to clone.
     * @param deep If true, a deep copy is created (including all child nodes).
     * 
     * @return The new node.
     */
    Node cloneNode(Node node, boolean deep) throws DOMException {
        Node target;
        
        switch (node.getNodeType()) {
            case Node.ATTRIBUTE_NODE: {
                Attr source = (Attr)node;
                target = createAttributeNS(source.getNamespaceURI(), source.getLocalName());
                target.setPrefix(source.getPrefix());
                target.setNodeValue(source.getNodeValue());
                break;
            }
            case Node.CDATA_SECTION_NODE: {
                CharacterData source = (CharacterData)node;
                target = createCDATASection(source.getData());
                break;
            }
            case Node.COMMENT_NODE: {
                Comment source = (Comment)node;
                target = createComment(source.getData());
                break;
            }
            case Node.DOCUMENT_FRAGMENT_NODE: {
                // Source is irrelevant in this case.
                target = createDocumentFragment();
                break;
            }
            case Node.DOCUMENT_NODE: {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot clone a Document node");
            }
            case Node.DOCUMENT_TYPE_NODE: {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot clone a DocumentType node");
            }
            case Node.ELEMENT_NODE: {
                Element source = (Element)node;
                target = createElementNS(source.getNamespaceURI(), source.getLocalName());
                target.setPrefix(source.getPrefix());

                NamedNodeMap map = source.getAttributes();
                for (int i = 0; i < map.getLength(); i++) {
                    Attr attr = (Attr)map.item(i);
                    ((Element)target).setAttributeNodeNS((Attr)cloneNode(attr, deep));
                }
                break;
            }
            case Node.ENTITY_NODE: {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot clone an Entity node");
            }
            case Node.ENTITY_REFERENCE_NODE: {
                EntityReference source = (EntityReference)node;
                target = createEntityReference(source.getNodeName());
                break;
            }
            case Node.NOTATION_NODE: {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot clone a Notation node");
            }
            case Node.PROCESSING_INSTRUCTION_NODE: {
                ProcessingInstruction source = (ProcessingInstruction)node;
                target = createProcessingInstruction(source.getTarget(), source.getData());
                break;
            }
            case Node.TEXT_NODE: {
                Text source = (Text)node;
                target = createTextNode(source.getData());
                break;
            }
            default: {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot clone unknown node type " + node.getNodeType() + " (" + node.getClass().getSimpleName() + ")");
            }
        }

        if (deep) {
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node child = cloneNode(list.item(i), deep);
                target.appendChild(child);
            }
        }
        
        return target;
    }
    
    public AttrImpl createAttribute(String name) throws DOMException {
        return new AttrImpl(this, name);
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
            throws DOMException {
        return new AttrImpl(this, namespaceURI, qualifiedName);
    }

    public CDATASection createCDATASection(String data) throws DOMException {
        return new CDATASectionImpl(this, data);
    }

    public Comment createComment(String data) {
        return new CommentImpl(this, data);
    }

    public DocumentFragment createDocumentFragment() {
        return new DocumentFragmentImpl(this);
    }

    public Element createElement(String tagName) throws DOMException {
        return new ElementImpl(this, tagName);
    }

    public Element createElementNS(String namespaceURI, String qualifiedName)
            throws DOMException {
        return new ElementImpl(this, namespaceURI, qualifiedName);
    }

    public EntityReference createEntityReference(String name)
            throws DOMException {
        return new EntityReferenceImpl(this, name);
    }

    public ProcessingInstruction createProcessingInstruction(String target,
            String data) throws DOMException {
        return new ProcessingInstructionImpl(this, target, data);
    }

    public Text createTextNode(String data) {
        return new TextImpl(this, data);
    }

    public DocumentType getDoctype() {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) instanceof DocumentType) {
                return (DocumentType) children.get(i);
            }
        }

        return null;
    }

    public Element getDocumentElement() {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) instanceof Element) {
                return (Element) children.get(i);
            }
        }

        return null;
    }

    public Element getElementById(String elementId) {
        ElementImpl root = (ElementImpl) getDocumentElement();

        return (root == null ? null : root.getElementById(elementId));
    }

    public NodeList getElementsByTagName(String tagname) {
        ElementImpl root = (ElementImpl) getDocumentElement();

        return (root == null ? new NodeListImpl()
                : root.getElementsByTagName(tagname));
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        ElementImpl root = (ElementImpl) getDocumentElement();

        return (root == null ? new NodeListImpl() : root.getElementsByTagNameNS(
                namespaceURI, localName));
    }

    public DOMImplementation getImplementation() {
        return domImplementation;
    }

    @Override
    public String getNodeName() {
        return "#document";
    }

    @Override
    public short getNodeType() {
        return Node.DOCUMENT_NODE;
    }

    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        return cloneNode(importedNode, deep);
    }

    @Override
    public Node insertChildAt(Node newChild, int index) throws DOMException {
        // Make sure we have at most one root element and one DTD element.
        if (newChild instanceof Element && getDocumentElement() != null) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                    "Only one root element allowed");
        } else if (newChild instanceof DocumentType && getDoctype() != null) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                    "Only one DOCTYPE element allowed");
        }

        return super.insertChildAt(newChild, index);
    }

}
