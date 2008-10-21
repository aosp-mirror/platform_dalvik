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

package javax.xml.parsers;

import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides a wrapper around a SAX XMLReader. This abstract class only defines
 * the interface, whereas the SAXParserFactory class is used to obtain instances
 * of concrete subclasses.
 */
public abstract class SAXParser extends java.lang.Object {

    /**
     * Do-nothing constructor. Prevents instantiation. To be overridden by
     * concrete subclasses.
     */
    protected SAXParser() {
        // Does nothing.
    }

    /**
     * Queries the underlying SAX Parser object.
     * 
     * @return The SAX Parser.
     * 
     * @throws org.xml.sax.SAXException if a problem occurs.
     */
    public abstract org.xml.sax.Parser getParser()
            throws org.xml.sax.SAXException;

    /**
     * Queries a property of the underlying SAX XMLReader.
     * 
     * @param name The name of the property.
     * @return The value of the property.
     * 
     * @throws SAXNotRecognizedException If the property is not known to the
     *         underlying SAX XMLReader.
     * @throws SAXNotSupportedException If the property is known, but not
     *         supported by the underlying SAX XMLReader.
     */
    public abstract Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException;

    /**
     * Queries the XML Schema used by the underlying XMLReader.
     * 
     * @return The XML Schema.
     */
    // TODO Do we want the validation package in?
    // public Schema getSchema() {
    // return schema;
    // }
    
    /**
     * Queries the underlying SAX XMLReader object.
     * 
     * @return The SAX XMLREader.
     * 
     * @throws org.xml.sax.SAXException if a problem occurs.
     */
    public abstract XMLReader getXMLReader() throws SAXException;

    /**
     * Reflects whether this SAXParser is namespace-aware.
     * 
     * @return true if the SAXParser is namespace-aware, or false otherwise.
     */
    public abstract boolean isNamespaceAware();

    /**
     * Reflects whether this SAXParser is validating.
     * 
     * @return true if the SAXParser is validating, or false otherwise.
     */
    public abstract boolean isValidating();

    /**
     * Reflects whether this SAXParser is XInclude-aware.
     * 
     * @return true if the SAXParser is XInclude-aware, or false otherwise.
     * 
     * @throws UnsupportedOperationException if the underlying implementation
     *         doesn't know about XInclude at all (backwards compatibility).
     */
    public boolean isXIncludeAware() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses the given XML file using the given SAX event handler.
     * 
     * @param file The file.
     * @param handler The SAX handler.
     * 
     * @throws SAXException If a problem occurs during SAX parsing.
     * @throws IOException If a general IO problem occurs.
     */
    public void parse(File file, HandlerBase handler) throws SAXException,
            IOException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("file must not be a directory");
        }
        InputSource source = new InputSource("file:" + file.getAbsolutePath());
        parse(source, handler);
    }

    /**
     * Parses the given XML file using the given SAX event handler.
     * 
     * @param file The file.
     * @param handler The SAX handler.
     * 
     * @throws SAXException If a problem occurs during SAX parsing.
     * @throws IOException If a general IO problem occurs.
     */
    public void parse(File file, DefaultHandler handler) throws SAXException,
            IOException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("file must not be a directory");
        }
        InputSource source = new InputSource("file:" + file.getAbsolutePath());
        parse(source, handler);
    }

    /**
     * Parses the given XML InputStream using the given SAX event handler.
     * 
     * @param stream The InputStream.
     * @param handler The SAX handler.
     * 
     * @throws SAXException If a problem occurs during SAX parsing.
     * @throws IOException If a general IO problem occurs.
     */
    public void parse(InputStream stream, HandlerBase handler)
            throws SAXException, IOException {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null");
        }
        parse(new InputSource(stream), handler);
    }

    /**
     * Parses the given XML InputStream using the given SAX event handler and
     * system ID.
     * 
     * @param stream The InputStream.
     * @param handler The SAX handler.
     * @param systemId The system ID.
     * 
     * @throws SAXException If a problem occurs during SAX parsing.
     * @throws IOException If a general IO problem occurs.
     */
    public void parse(InputStream stream, HandlerBase handler, String systemId)
            throws SAXException, IOException {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null");
        }
        InputSource source = new InputSource(stream);
        if (systemId != null) {
            source.setSystemId(systemId);
        }
        parse(source, handler);
    }

    /**
     * Parses the given XML InputStream using the given SAX event handler.
     * 
     * @param stream The InputStream.
     * @param handler The SAX handler.
     * 
     * @throws SAXException If a problem occurs during SAX parsing.
     * @throws IOException If a general IO problem occurs.
     */
    public void parse(InputStream stream, DefaultHandler handler)
            throws SAXException, IOException {
        parse(new InputSource(stream), handler);
    }

    /**
     * Parses the given XML InputStream using the given SAX event handler and
     * system ID.
     * 
     * @param stream The InputStream.
     * @param handler The SAX handler.
     * @param systemId The system ID.
     * 
     * @throws SAXException If a problem occurs during SAX parsing.
     * @throws IOException If a general IO problem occurs.
     */
    public void parse(InputStream stream, DefaultHandler handler,
            String systemId) throws SAXException, IOException {
        if (stream  == null) {
            throw new IllegalArgumentException("stream must not be null");
        }
        InputSource source = new InputSource(stream);
        if (systemId != null) {
            source.setSystemId(systemId);
        }
        parse(source, handler);
    }

    /**
     * Parses the contents of the given URI using the given SAX event handler.
     * 
     * @param uri The URI.
     * @param handler The SAX handler.
     * 
     * @throws SAXException If a problem occurs during SAX parsing.
     * @throws IOException If a general IO problem occurs.
     */
    public void parse(String uri, HandlerBase handler) throws SAXException,
            IOException {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        parse(new InputSource(uri), handler);
    }

    /**
     * Parses the contents of the given URI using the given SAX event handler.
     * 
     * @param uri The URI.
     * @param handler The SAX handler.
     * 
     * @throws SAXException If a problem occurs during SAX parsing.
     * @throws IOException If a general IO problem occurs.
     */
    public void parse(String uri, DefaultHandler handler) throws SAXException,
            IOException {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        parse(new InputSource(uri), handler);
    }

    /**
     * Parses the given SAX InputSource using the given SAX event handler.
     * 
     * @param source The SAX InputSource.
     * @param handler The SAX handler.
     * 
     * @throws SAXException If a problem occurs during SAX parsing.
     * @throws IOException If a general IO problem occurs.
     */
    public void parse(InputSource source, HandlerBase handler)
            throws SAXException, IOException {
        Parser parser = getParser();
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }

        if (handler != null) {
            parser.setDocumentHandler(handler);
            parser.setDTDHandler(handler);
            parser.setEntityResolver(handler);
            parser.setErrorHandler(handler);
        }

        parser.parse(source);
    }

    /**
     * Parses the given SAX InputSource using the given SAX event handler.
     * 
     * @param source The SAX HandlerBase.
     * @param handler The SAX handler.
     * 
     * @throws SAXException If a problem occurs during SAX parsing.
     * @throws IOException If a general IO problem occurs.
     */
    public void parse(InputSource source, DefaultHandler handler)
            throws SAXException, IOException {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        XMLReader reader = getXMLReader();

        if (handler != null) {
            reader.setContentHandler(handler);
            reader.setDTDHandler(handler);
            reader.setEntityResolver(handler);
            reader.setErrorHandler(handler);
        }

        reader.parse(source);
    }

    /**
     * Resets the DocumentBuilder to the same state is was in after its
     * creation.
     */
    public void reset() {
        // Do nothing.
    }

    /**
     * Sets a property of the underlying SAX XMLReader.
     * 
     * @param name The name of the property.
     * @param value The value of the property.
     * 
     * @throws SAXNotRecognizedException If the property is not known to the
     *         underlying SAX XMLReader.
     * @throws SAXNotSupportedException If the property is known, but not
     *         supported by the underlying SAX XMLReader.
     */
    public abstract void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException;

}
