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

import java.util.ArrayList;
import java.util.List;

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
public class NamedNodeMapImpl implements NamedNodeMap {

    private Class<?> type;
    
    private List<NodeImpl> list;

    NamedNodeMapImpl(Class<?> type) {
        list = new ArrayList<NodeImpl>();
        this.type = type;
    }

    NamedNodeMapImpl(List<NodeImpl> list, Class<?> type) {
        this.list = list;
        this.type = type;
    }

    public int getLength() {
        return list.size();
    }

    private int indexOfItem(String name) {
        for (int i = 0; i < list.size(); i++) {
            NodeImpl node = list.get(i);
            if (node.matchesName(name, false)) {
                return i;
            }
        }
        
        return -1;
    }
    
    private int indexOfItemNS(String namespaceURI, String localName) {
        for (int i = 0; i < list.size(); i++) {
            NodeImpl node = list.get(i);
            if (node.matchesNameNS(namespaceURI, localName, false)) {
                return i;
            }
        }
        
        return -1;
    }

    public Node getNamedItem(String name) {
        int i = indexOfItem(name);
        
        return (i == -1 ? null : item(i));
    }

    public Node getNamedItemNS(String namespaceURI, String localName) {
        int i = indexOfItemNS(namespaceURI, localName);
        
        return (i == -1 ? null : item(i));
    }

    public Node item(int index) {
        return list.get(index);
    }

    public Node removeNamedItem(String name) throws DOMException {
        int i = indexOfItem(name);
        
        if (i == -1) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, null);
        }

        return list.remove(i);
    }

    public Node removeNamedItemNS(String namespaceURI, String localName)
            throws DOMException {
        int i = indexOfItemNS(namespaceURI, localName);
        
        if (i == -1) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, null);
        }

        return list.remove(i);
    }

    public Node setNamedItem(Node arg) throws DOMException {
        // Ensure we only accept nodes of the correct type.
        if (!type.isAssignableFrom(arg.getClass())) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }
        
        // All nodes in the map must belong to the same document.
        if (list.size() != 0) {
            Document document = list.get(0).getOwnerDocument();

            if (document != null && arg.getOwnerDocument() != document) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
            }
        }

// TODO Theoretically we should ensure that the nodes don't have a parent.
//        if (newAttrImpl.getOwnerElement() != null) {
//            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, null);
//        }

        int i = indexOfItem(arg.getNodeName());
        
        if (i != -1) {
            list.remove(i);
        }
        
        list.add((NodeImpl)arg);
        return arg;
    }

    public Node setNamedItemNS(Node arg) throws DOMException {
        // Ensure we only accept nodes of the correct type.
        if (!type.isAssignableFrom(arg.getClass())) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }
        
        // All nodes in the map must belong to the same document.
        if (list.size() != 0) {
            Document document = list.get(0).getOwnerDocument();

            if (document != null && arg.getOwnerDocument() != document) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
            }
        }

// TODO Theoretically we should ensure that the nodes don't have a parent.
//        if (newAttrImpl.getOwnerElement() != null) {
//            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, null);
//        }

        int i = indexOfItemNS(arg.getNamespaceURI(), arg.getLocalName());
        
        if (i != -1) {
            list.remove(i);
        }
        
        list.add((NodeImpl)arg);
        return arg;
    }

}
