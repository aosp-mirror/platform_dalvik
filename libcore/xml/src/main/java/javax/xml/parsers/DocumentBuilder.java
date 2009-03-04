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

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a bridge from XML sources (files, stream etc.) to DOM trees. Can be
 * used for easily obtaining a {@link org.w3c.dom.Document} for the input. The
 * class itself is abstract. The class {@link DocumentBuilderFactory} is able to
 * provide instances (of concrete subclasses known to the system).
 * 
 * @since Android 1.0
 */
public abstract class DocumentBuilder {

    /**
     * Do-nothing constructor. Prevents instantiation. To be overridden by
     * concrete subclasses.
     * 
     * @since Android 1.0
     */
    protected DocumentBuilder() {
        // Does nothing.
    }

    /**
     * Queries the DOM implementation this {@code DocumentBuilder} is working
     * on.
     * 
     * @return the DOM implementation
     * 
     * @since Android 1.0
     */
    public abstract DOMImplementation getDOMImplementation();

// TODO No XSchema support in Android 1.0. Maybe later.
//    /**
//     * Queries the XML schema used by the DocumentBuilder.
//     * 
//     * @return The XML schema
//     * 
//     * @throws UnsupportedOperationException when the underlying implementation
//     *         doesn't support XML schemas.
//     */
//    public javax.xml.validation.Schema getSchema() throws
//            UnsupportedOperationException {
//        throw new UnsupportedOperationException();
//    }
     
    /**
     * Queries whether the {@code DocumentBuilder} has namespace support
     * enabled.
     * 
     * @return {@code true} if namespaces are turned on, {@code false}
     *         otherwise.
     * 
     * @since Android 1.0
     */
    public abstract boolean isNamespaceAware();

    /**
     * Queries whether the {@code DocumentBuilder} has validation support
     * enabled.
     * 
     * @return {@code true} if validation is turned on, {@code false} otherwise.
     * 
     * @since Android 1.0
     */
    public abstract boolean isValidating();

    /**
     * Queries whether the {@code DocumentBuilder} has XInclude support enabled.
     * 
     * @return {@code true} if XInclude support is turned on, {@code false}
     *         otherwise.
     * 
     * @throws UnsupportedOperationException if the underlying implementation
     *         doesn't support XInclude.
     * 
     * @since Android 1.0
     */
    public boolean isXIncludeAware() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new, empty document, serving as the starting point for a DOM
     * tree.
     * 
     * @return the document.
     * 
     * @since Android 1.0
     */
    public abstract Document newDocument();

    /**
     * Parses a given XML file and builds a DOM tree from it.
     * 
     * @param file the file to be parsed.
     * @return the document element that represents the root of the DOM tree.
     * 
     * @throws SAXException if the XML parsing fails.
     * @throws IOException if an input/output error occurs.
     * 
     * @since Android 1.0
     */
    public Document parse(File file) throws SAXException, IOException {
        if (file == null) {
            throw new IllegalArgumentException();
        }
        
        return parse(new BufferedInputStream(new FileInputStream(file), 8192));
    }

    /**
     * Parses a given XML input stream and builds a DOM tree from it.
     * 
     * @param stream the stream to be parsed.
     * @return the document element that represents the root of the DOM tree.
     * 
     * @throws SAXException if the XML parsing fails.
     * @throws IOException if an input/output error occurs.
     * 
     * @since Android 1.0
     */
    public Document parse(InputStream stream) throws SAXException, IOException {
        if (stream == null) {
            throw new IllegalArgumentException();
        }
        
        return parse(new InputSource(stream));
    }

    /**
     * Parses a given XML input stream and builds a DOM tree from it.
     * 
     * @param stream the stream to be parsed.
     * @param systemId the base for resolving relative URIs.
     * @return the document element that represents the root of the DOM tree.
     * 
     * @throws SAXException if the XML parsing fails.
     * @throws IOException if an input/output error occurs.
     * 
     * @since Android 1.0
     */
    public org.w3c.dom.Document parse(InputStream stream, String systemId)
            throws SAXException, IOException {
        if (stream == null) {
            throw new IllegalArgumentException();
        }
        
        InputSource source = new InputSource(stream);
        source.setSystemId(systemId);
        return parse(source);
    }

    /**
     * Parses an XML input stream from a given URI and builds a DOM tree from
     * it.
     * 
     * @param uri the URI to fetch the XML stream from.
     * @return the document element that represents the root of the DOM tree.
     * 
     * @throws SAXException if the XML parsing fails.
     * @throws IOException if an input/output error occurs.
     * 
     * @since Android 1.0
     */
    public org.w3c.dom.Document parse(String uri) throws SAXException,
            IOException {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        
        return parse(new InputSource(uri));
    }

    /**
     * Parses an XML input source and builds a DOM tree from it.
     * 
     * @param source the input source to parse.
     * @return the document element that represents the root of the DOM tree.
     * 
     * @throws SAXException if the XML parsing fails.
     * @throws IOException if an input/output error occurs.
     * 
     * @since Android 1.0
     */
    public abstract org.w3c.dom.Document parse(InputSource source)
            throws SAXException, IOException;

    /**
     * Resets the DocumentBuilder to the same state is was in after its
     * creation.
     * 
     * @since Android 1.0
     */
    public void reset() {
        // Do nothing.
    }

    /**
     * Sets the {@link EntityResolver} used for resolving entities encountered
     * during the parse process. Passing {@code null} results in the
     * {@code DocumentBuilder}'s own {@code EntityResolver} being used.
     * 
     * @param resolver the {@code EntityResolver} to use, or null for the
     *        built-in one.
     * 
     * @since Android 1.0
     */
    public abstract void setEntityResolver(EntityResolver resolver);

    /**
     * Sets the {@link ErrorHandler} used for dealing with errors encountered
     * during the parse process. Passing {@code null} results in the
     * {@code DocumentBuilder}'s own {@code ErrorHandler} being used.
     * 
     * @param handler the {@code ErrorHandler} to use, or {@code null} for the
     *        built-in one.
     * 
     * @since Android 1.0
     */
    public abstract void setErrorHandler(ErrorHandler handler);

}
