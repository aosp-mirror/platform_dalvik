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
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
public class AttrImpl extends NodeImpl implements Attr {

    // Maintained by ElementImpl.
    ElementImpl ownerElement;

    private boolean namespaceAware;
    
    private String namespaceURI;

    private String localName;

    private String prefix;
    
    private String value;

    AttrImpl(DocumentImpl document, String namespaceURI, String qualifiedName) {
        super(document);

        namespaceAware = true;
        this.namespaceURI = namespaceURI;

        if (qualifiedName == null || "".equals(qualifiedName)) {
            throw new DOMException(DOMException.NAMESPACE_ERR, qualifiedName);
        }
        
        int prefixSeparator = qualifiedName.lastIndexOf(":");
        if (prefixSeparator != -1) {
            setPrefix(qualifiedName.substring(0, prefixSeparator));
            qualifiedName = qualifiedName.substring(prefixSeparator + 1);
        }

        localName = qualifiedName;
        
        if ("".equals(localName)) {
            throw new DOMException(DOMException.NAMESPACE_ERR, localName);
        }
        
        if ("xmlns".equals(localName) && !"http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
            throw new DOMException(DOMException.NAMESPACE_ERR, localName);
        }
            
        if (!document.isXMLIdentifier(localName)) {
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, localName);
        }
            
        value = "";
    }

    AttrImpl(DocumentImpl document, String name) {
        super(document);

        this.namespaceAware = false;
        
        int prefixSeparator = name.lastIndexOf(":");
        if (prefixSeparator != -1) {
            String prefix = name.substring(0, prefixSeparator);
            String localName = name.substring(prefixSeparator + 1);
            
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
    
    @Override
    public String getLocalName() {
        return namespaceAware ? localName : null;
    }

    public String getName() {
        return (prefix != null ? prefix + ":" : "") + localName;
    }

    @Override
    public String getNamespaceURI() {
        return namespaceURI;
    }

    @Override
    public String getNodeName() {
        return getName();
    }

    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }

    @Override
    public String getNodeValue() {
        return getValue();
    }

    public Element getOwnerElement() {
        return ownerElement;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    public boolean getSpecified() {
        return value != null;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void setNodeValue(String value) throws DOMException {
        setValue(value);
    }
    
    @Override
    public void setPrefix(String prefix) {
        if (!namespaceAware) {
            throw new DOMException(DOMException.NAMESPACE_ERR, prefix);
        }
        
        if (prefix != null) {
            if (namespaceURI == null || !document.isXMLIdentifier(prefix) || "xmlns".equals(prefix)) {
                throw new DOMException(DOMException.NAMESPACE_ERR, prefix);
            }

            if ("xml".equals(prefix) && !"http://www.w3.org/XML/1998/namespace".equals(namespaceURI)) {
                throw new DOMException(DOMException.NAMESPACE_ERR, prefix);
            }
        }

        this.prefix = prefix;
    }
    
    public void setValue(String value) throws DOMException {
        this.value = value;
    }
    
}
