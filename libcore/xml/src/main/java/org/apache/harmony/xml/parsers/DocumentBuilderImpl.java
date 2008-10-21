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

package org.apache.harmony.xml.parsers;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;

import org.kxml2.io.KXmlParser;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import org.apache.harmony.xml.dom.DOMImplementationImpl;

/**
 * Provides a straightforward DocumentBuilder implementation based on
 * XMLPull/KXML. The class is used internally only, thus only notable members
 * that are not already in the abstract superclass are documented. Hope that's
 * ok.
 */
class DocumentBuilderImpl extends DocumentBuilder {

    private static DOMImplementation dom = DOMImplementationImpl.getInstance();

    private EntityResolver entityResolver;

    private ErrorHandler errorHandler;

    private boolean ignoreComments;

    private boolean ignoreElementContentWhitespace;

    private boolean namespaceAware;

    DocumentBuilderImpl() {
        // Do nothing.
    }

    @Override
    public DOMImplementation getDOMImplementation() {
        return dom;
    }

    /**
     * Reflects whether this DocumentBuilder is configured to ignore comments.
     * 
     * @return True if and only if comments are ignored.
     */
    public boolean isIgnoringComments() {
        return ignoreComments;
    }

    /**
     * Reflects whether this DocumentBuilder is configured to ignore element
     * content whitespace.
     * 
     * @return True if and only if whitespace element content is ignored.
     */
    public boolean isIgnoringElementContentWhitespace() {
        return ignoreElementContentWhitespace;
    }

    @Override
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    @Override
    public boolean isValidating() {
        return false;
    }

    @Override
    public Document newDocument() {
        return dom.createDocument(null, null, null);
    }

    @Override
    public Document parse(InputSource source) throws SAXException, IOException {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        
        Document document = newDocument();

        try {
            XmlPullParser parser = new KXmlParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    namespaceAware);
            
            if (source.getByteStream() != null) {
                parser.setInput(source.getByteStream(), source.getEncoding());
            } else if (source.getCharacterStream() != null) {
                parser.setInput(source.getCharacterStream());
            } else {
                // TODO Accept other sources as well?
                throw new SAXParseException(
                        "InputSource needs either stream or reader", null);
            }

            if(parser.nextToken() == XmlPullParser.END_DOCUMENT) {
                throw new SAXParseException(
                        "Unexpected end of document", null);
            }

            parse(parser, document, document, XmlPullParser.END_DOCUMENT);

            parser.require(XmlPullParser.END_DOCUMENT, null, null);
        } catch (XmlPullParserException ex) {
            if(ex.getDetail() instanceof IOException) {
                throw (IOException)ex.getDetail();
            }
            if(ex.getDetail() instanceof RuntimeException) {
                throw (RuntimeException)ex.getDetail();
            }
            
            LocatorImpl locator = new LocatorImpl();

            locator.setPublicId(source.getPublicId());
            locator.setSystemId(source.getSystemId());
            locator.setLineNumber(ex.getLineNumber());
            locator.setColumnNumber(ex.getColumnNumber());

            SAXParseException newEx = new SAXParseException(ex.getMessage(),
                    locator);

            if (errorHandler != null) {
                errorHandler.error(newEx);
            }

            throw newEx;
        }

        return document;
    }

    /**
     * Implements the whole parsing of the XML document. The XML pull parser is
     * actually more of a tokenizer, and we are doing a classical recursive
     * descent parsing (the method invokes itself for XML elements). Our
     * approach to parsing does accept some illegal documents (more than one
     * root element, for example). The assumption is that the DOM implementation
     * throws the proper exceptions in these cases.
     * 
     * @param parser The XML pull parser we're reading from.
     * @param document The document we're building.
     * @param node The node we're currently on (initially the document itself).
     * @param endToken The token that will end this recursive call. Either
     *        XmlPullParser.END_DOCUMENT or XmlPullParser.END_TAG.
     * 
     * @throws XmlPullParserException If a parsing error occurs.
     * @throws IOException If a general IO error occurs.
     */
    private void parse(XmlPullParser parser, Document document, Node node,
            int endToken) throws XmlPullParserException, IOException {

        int token = parser.getEventType();

        /*
         * The main parsing loop. The precondition is that we are already on the
         * token to be processed. This holds for each iteration of the loop, so
         * the inner statements have to ensure that (in particular the recursive
         * call).
         */
        while (token != endToken && token != XmlPullParser.END_DOCUMENT) {
            if (token == XmlPullParser.PROCESSING_INSTRUCTION) {
                /*
                 * Found a processing instructions. We need to split the token
                 * text at the first whitespace character.
                 */
                String text = parser.getText();

                int dot = text.indexOf(' ');

                String target = (dot != -1 ? text.substring(0, dot) : text);
                String data = (dot != -1 ? text.substring(dot + 1) : "");

                node.appendChild(document.createProcessingInstruction(target,
                        data));
            } else if (token == XmlPullParser.DOCDECL) {
                /*
                 * Found a document type declaration. Unfortunately KXML doesn't
                 * have the necessary details. Do we parse it ourselves, or do
                 * we silently ignore it, since it isn't mandatory in DOM 2
                 * anyway?
                 */
                StringTokenizer tokenizer = new StringTokenizer(parser.getText());
                if (tokenizer.hasMoreTokens()) {
                    String name = tokenizer.nextToken();
                    String pubid = null;
                    String sysid = null;
                    
                    if (tokenizer.hasMoreTokens()) {
                        String text = tokenizer.nextToken();
                        
                        if ("SYSTEM".equals(text)) {
                            if (tokenizer.hasMoreTokens()) {
                                sysid = tokenizer.nextToken();
                            }
                        } else if ("PUBLIC".equals(text)) {
                            if (tokenizer.hasMoreTokens()) {
                                pubid = tokenizer.nextToken();
                            }
                            if (tokenizer.hasMoreTokens()) {
                                sysid = tokenizer.nextToken();
                            }
                        }
                    }
                    
                    if (pubid != null && pubid.length() >= 2 && pubid.startsWith("\"") && pubid.endsWith("\"")) {
                        pubid = pubid.substring(1, pubid.length() - 1);
                    }
                    
                    if (sysid != null && sysid.length() >= 2 && sysid.startsWith("\"") && sysid.endsWith("\"")) {
                        sysid = sysid.substring(1, sysid.length() - 1);
                    }
                    
                    document.appendChild(dom.createDocumentType(name, pubid, sysid));
                }
                
            } else if (token == XmlPullParser.COMMENT) {
                /*
                 * Found a comment. We simply take the token text, but we only
                 * create a node if the client wants to see comments at all.
                 */
                if (!ignoreComments) {
                    node.appendChild(document.createComment(parser.getText()));
                }
            } else if (token == XmlPullParser.IGNORABLE_WHITESPACE) {
                /*
                 * Found some ignorable whitespace. We simply take the token
                 * text, but we only create a node if the client wants to see
                 * whitespace at all.
                 */
                if (!ignoreElementContentWhitespace) {
                    node.appendChild(document.createTextNode(parser.getText()));
                }
            } else if (token == XmlPullParser.TEXT) {
                /*
                 * Found a piece of text. That's the easiest case. We simply
                 * take it and create a corresponding node.
                 */
                node.appendChild(document.createTextNode(parser.getText()));
            } else if (token == XmlPullParser.CDSECT) {
                /*
                 * Found a CDATA section. That's also trivial. We simply
                 * take it and create a corresponding node.
                 */
                node.appendChild(document.createCDATASection(parser.getText()));
            } else if (token == XmlPullParser.ENTITY_REF) {
                /*
                 * Found an entity reference. If an entity resolver is
                 * installed, we replace it by text (if possible). Otherwise we
                 * add an entity reference node.
                 */
                String entity = parser.getName();

                if (entityResolver != null) {
                    // TODO Implement this...
                }

                String replacement = resolveStandardEntity(entity);
                if (replacement != null) {
                    node.appendChild(document.createTextNode(replacement));
                } else {
                    node.appendChild(document.createEntityReference(entity));
                }
            } else if (token == XmlPullParser.START_TAG) {
                /*
                 * Found an element start tag. We create an element node with
                 * the proper info and attributes. We then invoke parse()
                 * recursively to handle the next level of nesting. When we
                 * return from this call, we check that we are on the proper
                 * element end tag. The whole handling differs somewhat
                 * depending on whether the parser is namespace-aware or not.
                 */
                if (namespaceAware) {
                    // Collect info for element node
                    String namespace = parser.getNamespace();
                    String name = parser.getName();
                    String prefix = parser.getPrefix();

                    if ("".equals(namespace)) {
                        namespace = null;
                    }
                    
                    // Create element node and wire it correctly
                    Element element = document.createElementNS(namespace, name);
                    element.setPrefix(prefix);
                    node.appendChild(element);

                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        // Collect info for a single attribute node
                        String attrNamespace = parser.getAttributeNamespace(i);
                        String attrPrefix = parser.getAttributePrefix(i);
                        String attrName = parser.getAttributeName(i);
                        String attrValue = parser.getAttributeValue(i);

                        if ("".equals(attrNamespace)) {
                            attrNamespace = null;
                        }
                        
                        // Create attribute node and wire it correctly
                        Attr attr = document.createAttributeNS(attrNamespace, attrName);
                        attr.setPrefix(attrPrefix);
                        attr.setValue(attrValue);
                        element.setAttributeNodeNS(attr);
                    }
                    
                    // Recursive descent
                    token = parser.nextToken();
                    parse(parser, document, element, XmlPullParser.END_TAG);

                    // Expect the element's end tag here
                    parser.require(XmlPullParser.END_TAG, namespace, name);
                    
                } else {
                    // Collect info for element node
                    String name = parser.getName();

                    // Create element node and wire it correctly
                    Element element = document.createElement(name);
                    node.appendChild(element);

                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        // Collect info for a single attribute node
                        String attrName = parser.getAttributeName(i);
                        String attrValue = parser.getAttributeValue(i);

                        // Create attribute node and wire it correctly
                        Attr attr = document.createAttribute(attrName);
                        attr.setValue(attrValue);
                        element.setAttributeNode(attr);
                    }

                    // Recursive descent
                    token = parser.nextToken();
                    parse(parser, document, element, XmlPullParser.END_TAG);

                    // Expect the element's end tag here
                    parser.require(XmlPullParser.END_TAG, "", name);
                }
            }

            token = parser.nextToken();
        }
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
        entityResolver = resolver;
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        errorHandler = handler;
    }

    /**
     * Controls whether this DocumentBuilder ignores comments.
     * 
     * @param value Turns comment ignorance on or off.
     */
    public void setIgnoreComments(boolean value) {
        ignoreComments = value;
    }

    /**
     * Controls whether this DocumentBuilder ignores element content whitespace.
     * 
     * @param value Turns element whitespace content ignorance on or off.
     */
    public void setIgnoreElementContentWhitespace(boolean value) {
        ignoreElementContentWhitespace = value;
    }

    /**
     * Controls whether this DocumentBuilder is namespace-aware.
     * 
     * @param value Turns namespace awareness on or off.
     */
    public void setNamespaceAware(boolean value) {
        namespaceAware = value;
    }

    /**
     * Resolves one of the five standard XML entities.
     * 
     * @param entity The name of the entity to resolve, not including
     *               the ampersand or the semicolon.
     * 
     * @return The proper replacement, or null, if the entity is unknown.
     */
    private String resolveStandardEntity(String entity) {
        if ("lt".equals(entity)) {
            return "<";
        } else if ("gt".equals(entity)) {
            return ">";
        } else if ("amp".equals(entity)) {
            return "&";
        } else if ("apos".equals(entity)) {
            return "'";
        } else if ("quot".equals(entity)) {
            return "\"";
        } else {
            return null;
        }
    }
}
