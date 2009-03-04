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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides a wrapper around a SAX {@link XMLReader}. This abstract
 * class only defines the interface, whereas the {@link SAXParserFactory} class
 * is used to obtain instances of concrete subclasses.
 * 
 * @since Android 1.0
 */
public abstract class SAXParser extends java.lang.Object {

    /**
     * Do-nothing constructor. Prevents instantiation. To be overridden by
     * concrete subclasses.
     * 
     * @since Android 1.0
     */
    protected SAXParser() {
        // Does nothing.
    }

    /**
     * Queries the underlying SAX {@link Parser} object.
     * 
     * @return the SAX {@code Parser}.
     * 
     * @throws SAXException if a problem occurs.
     * 
     * @since Android 1.0
     */
    public abstract Parser getParser()
            throws SAXException;

    /**
     * Queries a property of the underlying SAX {@link XMLReader}.
     * 
     * @param name the name of the property.
     * @return the value of the property.
     * 
     * @throws SAXNotRecognizedException if the property is not known to the
     *         underlying SAX {@code XMLReader}.
     * @throws SAXNotSupportedException if the property is known, but not
     *         supported by the underlying SAX {@code XMLReader}.
     * 
     * @since Android 1.0
     */
    public abstract Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException;

// TODO No XSchema support in Android 1.0. Maybe later.
//    /**
//     * Queries the XML Schema used by the underlying XMLReader.
//     * 
//     * @return The XML Schema.
//     */
//    public Schema getSchema() {
//        return schema;
//    }
    
    /**
     * Queries the underlying SAX XMLReader object.
     * 
     * @return the SAX XMLREader.
     * 
     * @throws SAXException if a problem occurs.
     * 
     * @since Android 1.0
     */
    public abstract XMLReader getXMLReader() throws SAXException;

    /**
     * Reflects whether this {@code SAXParser} is namespace-aware.
     * 
     * @return {@code true} if the {@code SAXParser} is namespace-aware, or
     * {@code false} otherwise.
     * 
     * @since Android 1.0
     */
    public abstract boolean isNamespaceAware();

    /**
     * Reflects whether this {@code SAXParser} is validating.
     * 
     * @return {@code true} if the {@code SAXParser} is validating, or {@code
     * false} otherwise.
     * 
     * @since Android 1.0
     */
    public abstract boolean isValidating();

    /**
     * Reflects whether this {@code SAXParser} is XInclude-aware.
     * 
     * @return {@code true} if the {@code SAXParser} is XInclude-aware, or
     *         {@code false} otherwise.
     * 
     * @throws UnsupportedOperationException if the underlying implementation
     *         doesn't know about XInclude at all (backwards compatibility).
     * 
     * @since Android 1.0
     */
    public boolean isXIncludeAware() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses the given XML file using the given SAX event handler.
     * 
     * @param file the file containing the XML document.
     * @param handler the SAX handler.
     * 
     * @throws SAXException if a problem occurs during SAX parsing.
     * @throws IOException if a general IO problem occurs.
     * 
     * @since Android 1.0
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
     * @param file the file containing the XML document.
     * @param handler the SAX handler.
     * 
     * @throws SAXException if a problem occurs during SAX parsing.
     * @throws IOException if a general IO problem occurs.
     * 
     * @since Android 1.0
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
     * @param stream the InputStream containing the XML document.
     * @param handler the SAX handler.
     * 
     * @throws SAXException if a problem occurs during SAX parsing.
     * @throws IOException if a general IO problem occurs.
     * 
     * @since Android 1.0
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
     * @param stream the InputStream containing the XML document.
     * @param handler the SAX handler.
     * @param systemId the system ID.
     * 
     * @throws SAXException if a problem occurs during SAX parsing.
     * @throws IOException if a general IO problem occurs.
     * 
     * @since Android 1.0
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
     * @param stream the InputStream containing the XML document.
     * @param handler the SAX handler.
     * 
     * @throws SAXException if a problem occurs during SAX parsing.
     * @throws IOException if a general IO problem occurs.
     * 
     * @since Android 1.0
     */
    public void parse(InputStream stream, DefaultHandler handler)
            throws SAXException, IOException {
        parse(new InputSource(stream), handler);
    }

    /**
     * Parses the given XML InputStream using the given SAX event handler and
     * system ID.
     * 
     * @param stream the InputStream containing the XML document.
     * @param handler the SAX handler.
     * @param systemId the system ID.
     * 
     * @throws SAXException if a problem occurs during SAX parsing.
     * @throws IOException if a general IO problem occurs.
     * 
     * @since Android 1.0
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
     * @param uri the URI pointing to the XML document.
     * @param handler the SAX handler.
     * 
     * @throws SAXException if a problem occurs during SAX parsing.
     * @throws IOException if a general IO problem occurs.
     * 
     * @since Android 1.0
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
     * @param uri the URI pointing to the XML document.
     * @param handler the SAX handler.
     * 
     * @throws SAXException if a problem occurs during SAX parsing.
     * @throws IOException if a general IO problem occurs.
     * 
     * @since Android 1.0
     */
    public void parse(String uri, DefaultHandler handler) throws SAXException,
            IOException {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        parse(new InputSource(uri), handler);
    }

    /**
     * Parses the given SAX {@link InputSource} using the given SAX event
     * handler.
     * 
     * @param source the SAX {@code InputSource} containing the XML document.
     * @param handler the SAX handler.
     * 
     * @throws SAXException if a problem occurs during SAX parsing.
     * @throws IOException if a general IO problem occurs.
     * 
     * @since Android 1.0
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
     * Parses the given SAX {@link InputSource} using the given SAX event
     * handler.
     * 
     * @param source the SAX {@code InputSource} containing the XML document.
     * @param handler the SAX handler.
     * 
     * @throws SAXException if a problem occurs during SAX parsing.
     * @throws IOException if a general IO problem occurs.
     * 
     * @since Android 1.0
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
     * Resets the {@code SAXParser} to the same state is was in after its
     * creation.
     * 
     * @since Android 1.0
     */
    public void reset() {
        // Do nothing.
    }

    /**
     * Sets a property of the underlying SAX {@link XMLReader}.
     * 
     * @param name the name of the property.
     * @param value the value of the property.
     * 
     * @throws SAXNotRecognizedException if the property is not known to the
     *         underlying SAX {@code XMLReader}.
     * @throws SAXNotSupportedException if the property is known, but not
     *         supported by the underlying SAX {@code XMLReader}.
     * 
     * @since Android 1.0
     */
    public abstract void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException;

}
