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

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
 * <p>
 * This class represents a Node that has a parent Node as well as (potentially)
 * a number of children.
 */
public abstract class InnerNodeImpl extends LeafNodeImpl {

    // Maintained by LeafNodeImpl and ElementImpl.
    List<LeafNodeImpl> children = new ArrayList<LeafNodeImpl>();

    public InnerNodeImpl(DocumentImpl document) {
        super(document);
    }

    public Node appendChild(Node newChild) throws DOMException {
        return insertChildAt(newChild, children.size());
    }

    public NodeList getChildNodes() {
        NodeListImpl list = new NodeListImpl();

        for (NodeImpl node : children) {
            list.add(node);
        }

        return list;
    }

    public Node getFirstChild() {
        return (!children.isEmpty() ? children.get(0) : null);
    }

    public Node getLastChild() {
        return (!children.isEmpty() ? children.get(children.size() - 1) : null);
    }

    public Node getNextSibling() {
        if (parent == null || index >= parent.children.size()) {
            return null;
        }

        return parent.children.get(index + 1);
    }

    public boolean hasChildNodes() {
        return children.size() != 0;
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        LeafNodeImpl refChildImpl = (LeafNodeImpl) refChild;

        if (refChildImpl.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }

        if (refChildImpl.parent != this) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }

        return insertChildAt(newChild, refChildImpl.index);
    }

    /**
     * Inserts a new child node into this node at a given position. If the new
     * node is already child of another node, it is first removed from there.
     * This method is the generalization of the appendChild() and insertBefore()
     * methods.
     * 
     * @param newChild The new child node to add.
     * @param index The index at which to insert the new child node.
     * 
     * @return The node added.
     * 
     * @throws DOMException If the attempted operation violates the XML/DOM
     *         well-formedness rules.
     */
    public Node insertChildAt(Node newChild, int index) throws DOMException {
        LeafNodeImpl newChildImpl = (LeafNodeImpl) newChild;

        if (document != null && newChildImpl.document != null && newChildImpl.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }

        if (newChildImpl.isParentOf(this)) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }

        if (newChildImpl.parent != null) {
            int oldIndex = newChildImpl.index;
            newChildImpl.parent.children.remove(oldIndex);
            newChildImpl.parent.refreshIndices(oldIndex);
        }

        children.add(index, newChildImpl);
        newChildImpl.parent = this;
        refreshIndices(index);

        return newChild;
    }

    public boolean isParentOf(Node node) {
        LeafNodeImpl nodeImpl = (LeafNodeImpl) node;

        while (nodeImpl != null) {
            if (nodeImpl == this) {
                return true;
            }

            nodeImpl = nodeImpl.parent;
        }

        return false;
    }

    @Override
    public void normalize() {
        Node nextNode = null;
        
        for (int i = children.size() - 1; i >= 0; i--) {
            Node thisNode = children.get(i);

            thisNode.normalize();
            
            if (thisNode.getNodeType() == Node.TEXT_NODE) {
                if (nextNode != null && nextNode.getNodeType() == Node.TEXT_NODE) {
                    ((Text)thisNode).setData(thisNode.getNodeValue() + nextNode.getNodeValue());
                    removeChild(nextNode);
                }
                
                if ("".equals(thisNode.getNodeValue())) {
                    removeChild(thisNode);
                    nextNode = null;
                } else {
                    nextNode = thisNode;
                }
            }
        }
    }

    private void refreshIndices(int fromIndex) {
        for (int i = fromIndex; i < children.size(); i++) {
            children.get(i).index = i;
        }
    }

    public Node removeChild(Node oldChild) throws DOMException {
        LeafNodeImpl oldChildImpl = (LeafNodeImpl) oldChild;

        if (oldChildImpl.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }

        if (oldChildImpl.parent != this) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }

        int index = oldChildImpl.index;
        children.remove(index);
        oldChildImpl.parent = null;
        refreshIndices(index);

        return oldChild;
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        LeafNodeImpl oldChildImpl = (LeafNodeImpl) oldChild;
        LeafNodeImpl newChildImpl = (LeafNodeImpl) newChild;

        if (oldChildImpl.document != document
                || newChildImpl.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }

        if (oldChildImpl.parent != this || newChildImpl.isParentOf(this)) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }

        int index = oldChildImpl.index;
        children.set(index, newChildImpl);
        oldChildImpl.parent = null;
        newChildImpl.parent = this;
        refreshIndices(index);

        return oldChildImpl;
    }

}
