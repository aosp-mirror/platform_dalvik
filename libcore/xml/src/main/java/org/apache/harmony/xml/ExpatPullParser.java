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

package org.apache.harmony.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Fast, partial XmlPullParser implementation based upon Expat. Does not
 * support validation or {@code DOCTYPE} processing.
 */
public class ExpatPullParser implements XmlPullParser {
    /**
     * This feature is identified by http://xmlpull.org/v1/doc/features.html#relaxed
     * If this feature is supported that means that XmlPull parser will be
     * lenient when checking XML well formedness.
     * NOTE: use it only if XML input is not well-formed and in general usage
     * if this feature is discouraged
     * NOTE: as there is no definition of what is relaxed XML parsing
     * therefore what parser will do completely depends on implementation used
     */
    public static final String FEATURE_RELAXED =
            "http://xmlpull.org/v1/doc/features.html#relaxed";

    private static final int BUFFER_SIZE = 8096;

    private static final String NOT_A_START_TAG = "This is not a start tag.";

    private Document document;
    private boolean processNamespaces = false;
    private boolean relaxed = false;

    public void setFeature(String name, boolean state)
            throws XmlPullParserException {
        if (name == null) {
            // Required by API.          
            throw new IllegalArgumentException("Null feature name");
        }

        if (name.equals(FEATURE_PROCESS_NAMESPACES)) {
            processNamespaces = state;
            return;
        }

        if (name.equals(FEATURE_RELAXED)) {
            relaxed = true;
            return;
        }

        // You're free to turn these features off because we don't support them.
        if (!state && (name.equals(FEATURE_REPORT_NAMESPACE_ATTRIBUTES)
                || name.equals(FEATURE_PROCESS_DOCDECL)
                || name.equals(FEATURE_VALIDATION))) {
            return;
        }

        throw new XmlPullParserException("Unsupported feature: " + name);
    }

    public boolean getFeature(String name) {
        if (name == null) {
            // Required by API.
            throw new IllegalArgumentException("Null feature name");
        }

        // We always support namespaces, but no other features.
        return name.equals(FEATURE_PROCESS_NAMESPACES) && processNamespaces;
    }

    /**
     * Returns true if this parser processes namespaces.
     *
     * @see #setNamespaceProcessingEnabled(boolean)
     */
    public boolean isNamespaceProcessingEnabled() {
        return processNamespaces;
    }

    /**
     * Enables or disables namespace processing. Set to false by default.
     *
     * @see #isNamespaceProcessingEnabled()
     */
    public void setNamespaceProcessingEnabled(boolean processNamespaces) {
        this.processNamespaces = processNamespaces;
    }

    public void setProperty(String name, Object value)
            throws XmlPullParserException {
        if (name == null) {
            // Required by API.
            throw new IllegalArgumentException("Null feature name");
        }

        // We don't support any properties.
        throw new XmlPullParserException("Properties aren't supported.");
    }

    public Object getProperty(String name) {
        return null;
    }

    public void setInput(Reader in) throws XmlPullParserException {
        this.document = new CharDocument(in, processNamespaces);
    }

    public void setInput(InputStream in, String encodingName)
            throws XmlPullParserException {
        this.document = new ByteDocument(in, encodingName, processNamespaces);
    }

    public String getInputEncoding() {
        return this.document.getEncoding();
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException always
     */
    public void defineEntityReplacementText(String entityName,
            String replacementText) throws XmlPullParserException {
        throw new UnsupportedOperationException();
    }

    public int getNamespaceCount(int depth) throws XmlPullParserException {
        return document.currentEvent.namespaceStack.countAt(depth);
    }

    public String getNamespacePrefix(int pos) throws XmlPullParserException {
        String prefix = document.currentEvent.namespaceStack.prefixAt(pos);
        @SuppressWarnings("StringEquality")
        boolean hasPrefix = prefix != "";
        return hasPrefix ? prefix : null;
    }

    public String getNamespaceUri(int pos) throws XmlPullParserException {
        return document.currentEvent.namespaceStack.uriAt(pos);
    }

    public String getNamespace(String prefix) {
        // In XmlPullParser API, null == default namespace.
        if (prefix == null) {
            // Internally, we use empty string instead of null.
            prefix = "";
        }

        return document.currentEvent.namespaceStack.uriFor(prefix);
    }

    public int getDepth() {
        return this.document.getDepth();
    }

    public String getPositionDescription() {
        return "line " + getLineNumber() + ", column " + getColumnNumber();
    }

    /**
     * Not supported.
     *
     * @return {@literal -1} always
     */
    public int getLineNumber() {
        // We would have to record the line number in each event.
        return -1;
    }

    /**
     * Not supported.
     *
     * @return {@literal -1} always
     */
    public int getColumnNumber() {
        // We would have to record the column number in each event.
        return -1;
    }

    public boolean isWhitespace() throws XmlPullParserException {
        if (getEventType() != TEXT) {
            throw new XmlPullParserException("Not on text.");
        }

        String text = getText();

        if (text.length() == 0) {
            return true;
        }

        int length = text.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public String getText() {
        final StringBuilder builder = this.document.currentEvent.getText();
        return builder == null ? null : builder.toString();
    }

    public char[] getTextCharacters(int[] holderForStartAndLength) {
        final StringBuilder builder = this.document.currentEvent.getText();

        final int length = builder.length();
        char[] characters = new char[length];
        builder.getChars(0, length, characters, 0);

        holderForStartAndLength[0] = 0;
        holderForStartAndLength[1] = length;

        return characters;
    }

    public String getNamespace() {
        return this.document.currentEvent.getNamespace();
    }

    public String getName() {
        return this.document.currentEvent.getName();
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException always
     */
    public String getPrefix() {
        throw new UnsupportedOperationException();
    }

    public boolean isEmptyElementTag() throws XmlPullParserException {
        return this.document.isCurrentElementEmpty();
    }

    public int getAttributeCount() {
        return this.document.currentEvent.getAttributeCount();
    }

    public String getAttributeNamespace(int index) {
        return this.document.currentEvent.getAttributeNamespace(index);
    }

    public String getAttributeName(int index) {
        return this.document.currentEvent.getAttributeName(index);
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException always
     */
    public String getAttributePrefix(int index) {
        throw new UnsupportedOperationException();
    }

    public String getAttributeType(int index) {
        return "CDATA";
    }

    public boolean isAttributeDefault(int index) {
        return false;
    }

    public String getAttributeValue(int index) {
        return this.document.currentEvent.getAttributeValue(index);
    }

    public String getAttributeValue(String namespace, String name) {
        return this.document.currentEvent.getAttributeValue(namespace, name);
    }

    public int getEventType() throws XmlPullParserException {
        return this.document.currentEvent.getType();
    }

    public int next() throws XmlPullParserException, IOException {
        return this.document.dequeue();
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException always
     */
    public int nextToken() throws XmlPullParserException, IOException {
        throw new UnsupportedOperationException();
    }

    public void require(int type, String namespace, String name)
            throws XmlPullParserException, IOException {
        if (type != getEventType()
                || (namespace != null && !namespace.equals(getNamespace()))
                || (name != null && !name.equals(getName()))) {
            throw new XmlPullParserException("expected "
                    + TYPES[type] + getPositionDescription());
        }
    }

    public String nextText() throws XmlPullParserException, IOException {
        if (this.document.currentEvent.getType() != START_TAG)
            throw new XmlPullParserException("Not on start tag.");

        int next = this.document.dequeue();
        switch (next) {
            case TEXT: return getText();
            case END_TAG: return "";
            default: throw new XmlPullParserException(
                "Unexpected event type: " + TYPES[next]);
        }
    }

    public int nextTag() throws XmlPullParserException, IOException {
        int eventType = next();
        if (eventType == TEXT && isWhitespace()) {
            eventType = next();
        }
        if (eventType != START_TAG && eventType != END_TAG) {
            throw new XmlPullParserException(
                "Expected start or end tag", this, null);
        }
        return eventType;
    }

    /**
     * Immutable namespace stack. Pushing a new namespace on to the stack
     * only results in one object allocation. Most operations are O(N) where
     * N is the stack size. Accessing recently pushed namespaces, like those
     * for the current element, is significantly faster.
     */
    static class NamespaceStack {

        /** An empty stack. */
        static final NamespaceStack EMPTY = new NamespaceStack();

        private final NamespaceStack parent;
        private final String prefix;
        private final String uri;
        private final int index;
        private final int depth;

        /**
         * Constructs an actual namespace stack node. Internally, the nodes
         * and the stack are one in the same making for a very efficient
         * implementation. The user just sees an immutable stack and the
         * builder.
         */
        private NamespaceStack(NamespaceStack parent, String prefix,
                String uri, int depth) {
            this.parent = parent;
            this.prefix = prefix;
            this.uri = uri;
            this.index = parent.index + 1;
            this.depth = depth;
        }

        /**
         * Constructs a dummy node which only serves to point to the bottom
         * of the stack. Using an actual node instead of null simplifies the
         * code.
         */
        private NamespaceStack() {
            this.parent = null;
            this.prefix = null;
            this.uri = null;

            // This node has an index of -1 since the actual first node in the
            // stack has index 0.
            this.index = -1;
            
            // The actual first node will have a depth of 1.
            this.depth = 0;
        }

        String uriFor(String prefix) {
            for (NamespaceStack node = this; node.index >= 0;
                    node = node.parent) {
                if (node.prefix.equals(prefix)) {
                    return node.uri;
                }
            }

            // Not found.
            return null;
        }

        /**
         * Gets the prefix at the given index in the stack.
         */
        String prefixAt(int index) {
            return nodeAt(index).prefix;
        }

        /**
         * Gets the URI at the given index in the stack.
         */
        String uriAt(int index) {
            return nodeAt(index).uri;
        }

        private NamespaceStack nodeAt(int index) {
            if (index > this.index) {
                throw new IndexOutOfBoundsException("Index > size.");
            }
            if (index < 0) {
                throw new IndexOutOfBoundsException("Index < 0.");
            }

            NamespaceStack node = this;
            while (index != node.index) {
                node = node.parent;
            }
            return node;
        }

        /**
         * Gets the size of the stack at the given element depth.
         */
        int countAt(int depth) {
            if (depth > this.depth) {
                throw new IndexOutOfBoundsException("Depth > maximum.");
            }
            if (depth < 0) {
                throw new IndexOutOfBoundsException("Depth < 0.");
            }

            NamespaceStack node = this;
            while (depth < node.depth) {
                node = node.parent;
            }
            return node.index + 1;         
        }

        /** Builds a NamespaceStack. */
        static class Builder {

            NamespaceStack top = EMPTY;

            /**
             * Pushes a namespace onto the stack.
             *
             * @param depth of the element upon which the namespace was
             *  declared
             */
            void push(String prefix, String uri, int depth) {
                top = new NamespaceStack(top, prefix, uri, depth);
            }

            /**
             * Pops all namespaces from the given element depth.
             */
            void pop(int depth) {
                // Remove all nodes at the specified depth.
                while (top != null && top.depth == depth) {
                    top = top.parent;
                }
            }

            /** Returns the current stack. */
            NamespaceStack build() {
                return top;
            }
        }
    }

    /**
     * Base class for events. Implements event chaining and defines event API
     * along with common implementations which can be overridden.
     */
    static abstract class Event {

        /** Element depth at the time of this event. */
        final int depth;

        /** The namespace stack at the time of this event. */
        final NamespaceStack namespaceStack;

        /** Next event in the queue. */ 
        Event next = null;

        Event(int depth, NamespaceStack namespaceStack) {
            this.depth = depth;
            this.namespaceStack = namespaceStack;
        }

        void setNext(Event next) {
            this.next = next;
        }

        Event getNext() {
            return next;
        }

        StringBuilder getText() {
            return null;
        }

        String getNamespace() {
            return null;
        }

        String getName() {
            return null;
        }

        int getAttributeCount() {
            return -1;
        }

        String getAttributeNamespace(int index) {
            throw new IndexOutOfBoundsException(NOT_A_START_TAG);
        }

        String getAttributeName(int index) {
            throw new IndexOutOfBoundsException(NOT_A_START_TAG);
        }

        String getAttributeValue(int index) {
            throw new IndexOutOfBoundsException(NOT_A_START_TAG);
        }

        abstract int getType();

        String getAttributeValue(String namespace, String name) {
            throw new IndexOutOfBoundsException(NOT_A_START_TAG);
        }

        public int getDepth() {
            return this.depth;
        }
    }

    static class StartDocumentEvent extends Event {

        public StartDocumentEvent() {
            super(0, NamespaceStack.EMPTY);
        }

        @Override
        int getType() {
            return START_DOCUMENT;
        }
    }

    static class StartTagEvent extends Event {

        final String name;
        final String namespace;
        final Attributes attributes;
        final boolean processNamespaces;

        StartTagEvent(String namespace,
                String name,
                ExpatParser expatParser,
                int depth,
                NamespaceStack namespaceStack,
                boolean processNamespaces) {
            super(depth, namespaceStack);
            this.namespace = namespace;
            this.name = name;
            this.attributes = expatParser.cloneAttributes();
            this.processNamespaces = processNamespaces;
        }

        @Override
        String getNamespace() {
            return namespace;
        }

        @Override
        String getName() {
            return name;
        }

        @Override
        int getAttributeCount() {
            return attributes.getLength();
        }

        @Override
        String getAttributeNamespace(int index) {
            return attributes.getURI(index);
        }

        @Override
        String getAttributeName(int index) {
            return processNamespaces ? attributes.getLocalName(index)
                    : attributes.getQName(index);
        }

        @Override
        String getAttributeValue(int index) {
            return attributes.getValue(index);
        }

        @Override
        String getAttributeValue(String namespace, String name) {
            if (namespace == null) {
                namespace = "";
            }

            return attributes.getValue(namespace, name);
        }

        @Override
        int getType() {
            return START_TAG;
        }
    }

    static class EndTagEvent extends Event {

        final String namespace;
        final String localName;

        EndTagEvent(String namespace, String localName, int depth,
                NamespaceStack namespaceStack) {
            super(depth, namespaceStack);
            this.namespace = namespace;
            this.localName = localName;
        }

        @Override
        String getName() {
            return this.localName;
        }

        @Override
        String getNamespace() {
            return this.namespace;
        }

        @Override
        int getType() {
            return END_TAG;
        }
    }

    static class TextEvent extends Event {

        final StringBuilder builder;

        public TextEvent(int initialCapacity, int depth,
                NamespaceStack namespaceStack) {
            super(depth, namespaceStack);
            this.builder = new StringBuilder(initialCapacity);
        }

        @Override
        int getType() {
            return TEXT;
        }

        @Override
        StringBuilder getText() {
            return this.builder;
        }

        void append(char[] text, int start, int length) {
            builder.append(text, start, length);
        }
    }

    static class EndDocumentEvent extends Event {

        EndDocumentEvent() {
            super(0, NamespaceStack.EMPTY);
        }

        @Override
        Event getNext() {
            throw new IllegalStateException("End of document.");
        }

        @Override
        void setNext(Event next) {
            throw new IllegalStateException("End of document.");
        }

        @Override
        int getType() {
            return END_DOCUMENT;
        }
    }

    /**
     * Encapsulates the parsing context of the current document.
     */
    abstract class Document {

        final String encoding;
        final ExpatParser parser;
        final boolean processNamespaces;

        TextEvent textEvent = null;
        boolean finished = false;

        Document(String encoding, boolean processNamespaces) {
            this.encoding = encoding;
            this.processNamespaces = processNamespaces;

            ExpatReader xmlReader = new ExpatReader();
            xmlReader.setContentHandler(new SaxHandler());

            this.parser = new ExpatParser(
                    encoding, xmlReader, processNamespaces, null, null);
        }

        /** Namespace stack builder. */
        NamespaceStack.Builder namespaceStackBuilder
                = new NamespaceStack.Builder();
        
        Event currentEvent = new StartDocumentEvent();
        Event last = currentEvent;

        /**
         * Sends some more XML to the parser.
         */
        void pump() throws IOException, XmlPullParserException {
            if (this.finished) {
                return;
            }

            int length = buffer();

            // End of document.
            if (length == -1) {
                this.finished = true;
                if (!relaxed) {
                    try {
                        parser.finish();
                    } catch (SAXException e) {
                        throw new XmlPullParserException(
                            "Premature end of document.", ExpatPullParser.this, e);
                    }
                }
                add(new EndDocumentEvent());
                return;
            }

            if (length == 0) {
                return;
            }

            flush(parser, length);
        }

        /**
         * Reads data into the buffer.
         *
         * @return the length of data buffered or {@code -1} if we've reached
         *  the end of the data.
         */
        abstract int buffer() throws IOException;

        /**
         * Sends buffered data to the parser.
         *
         * @param parser the parser to flush to
         * @param length of data buffered
         */
        abstract void flush(ExpatParser parser, int length)
                throws XmlPullParserException;

        /**
         * Adds an event.
         */
        void add(Event event) {
            // Flush pre-exising text event if necessary.
            if (textEvent != null) {
                last.setNext(textEvent);
                last = textEvent;
                textEvent = null;
            }

            last.setNext(event);
            last = event;
        }

        /**
         * Moves to the next event in the queue.
         *
         * @return type of next event
         */
        int dequeue() throws XmlPullParserException, IOException {
            Event next;

            while ((next = currentEvent.getNext()) == null) {
                pump();
            }

            currentEvent.next = null;
            currentEvent = next;

            return currentEvent.getType();
        }

        String getEncoding() {
            return this.encoding;
        }

        int getDepth() {
            return currentEvent.getDepth();
        }

        /**
         * Returns true if we're on a start element and the next event is
         * its corresponding end element.
         *
         * @throws XmlPullParserException if we aren't on a start element
         */
        boolean isCurrentElementEmpty() throws XmlPullParserException {
            if (currentEvent.getType() != START_TAG) {
                throw new XmlPullParserException(NOT_A_START_TAG);
            }

            Event next;

            try {
                while ((next = currentEvent.getNext()) == null) {
                    pump();
                }
            } catch (IOException ex) {
                throw new XmlPullParserException(ex.toString());
            }

            return next.getType() == END_TAG;
        }

        private class SaxHandler implements ContentHandler {

            int depth = 0;

            public void startPrefixMapping(String prefix, String uri)
                    throws SAXException {
                // Depth + 1--we aren't actually in the element yet.
                namespaceStackBuilder.push(prefix, uri, depth + 1);
            }

            public void startElement(String uri, String localName, String qName,
                    Attributes attributes) {
                String name = processNamespaces ? localName : qName;

                add(new StartTagEvent(uri, name, parser, ++this.depth,
                        namespaceStackBuilder.build(), processNamespaces));
            }

            public void endElement(String uri, String localName, String qName) {
                String name = processNamespaces ? localName : qName;

                int depth = this.depth--;
                add(new EndTagEvent(uri, name, depth,
                        namespaceStackBuilder.build()));
                namespaceStackBuilder.pop(depth);
            }

            public void characters(char ch[], int start, int length) {
                // Ignore empty strings.
                if (length == 0) {
                    return;
                }

                // Start a new text event if necessary.
                if (textEvent == null) {
                    textEvent = new TextEvent(length, this.depth,
                            namespaceStackBuilder.build());
                }

                // Append to an existing text event.
                textEvent.append(ch, start, length);
            }

            public void setDocumentLocator(Locator locator) {}
            public void startDocument() throws SAXException {}
            public void endDocument() throws SAXException {}
            public void endPrefixMapping(String prefix) throws SAXException {}
            public void ignorableWhitespace(char ch[], int start, int length)
                    throws SAXException {}
            public void processingInstruction(String target, String data)
                    throws SAXException {}
            public void skippedEntity(String name) throws SAXException {}
        }
    }

    class CharDocument extends Document {

        final char[] buffer = new char[BUFFER_SIZE / 2];
        final Reader in;

        CharDocument(Reader in, boolean processNamespaces) {
            super("UTF-16", processNamespaces);
            this.in = in;
        }

        @Override
        int buffer() throws IOException {
            return in.read(buffer);
        }

        @Override
        void flush(ExpatParser parser, int length)
                throws XmlPullParserException {
            try {
                parser.append(buffer, 0, length);
            } catch (SAXException e) {
                throw new XmlPullParserException(
                        "Error parsing document.", ExpatPullParser.this, e);
            }
        }
    }

    class ByteDocument extends Document {

        final byte[] buffer = new byte[BUFFER_SIZE];
        final InputStream in;

        ByteDocument(InputStream in, String encoding,
                boolean processNamespaces) {
            super(encoding, processNamespaces);
            this.in = in;
        }

        @Override
        int buffer() throws IOException {
            return in.read(buffer);
        }

        @Override
        void flush(ExpatParser parser, int length)
                throws XmlPullParserException {
            try {
                parser.append(buffer, 0, length);
            } catch (SAXException e) {
                throw new XmlPullParserException(
                        "Error parsing document.", ExpatPullParser.this, e);
            }
        }
    }
}
