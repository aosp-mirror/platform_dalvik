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
 * used for easily obtaining a Document for the input. The class itself is
 * abstract. The class DocumentBuilderFactory is able to provide instances (of
 * concrete subclasses known to the system).
 */
public abstract class DocumentBuilder {

    /**
     * Do-nothing constructor. Prevents instantiation. To be overridden by
     * concrete subclasses.
     */
    protected DocumentBuilder() {
        // Does nothing.
    }

    /**
     * Queries the DOM implementation this DocumentBuilder is working on.
     * 
     * @return The DOM implementation
     */
    public abstract DOMImplementation getDOMImplementation();

    /**
     * Queries the XML schema used by the DocumentBuilder.
     * 
     * @return The XML schema
     * 
     * @throws UnsupportedOperationException when the underlying implementation
     *         doesn't support XML schemas.
     */
    // TODO Do we want the validation package in?
    // public javax.xml.validation.Schema getSchema() throws
    // UnsupportedOperationException {
    // throw new UnsupportedOperationException();
    // }
    /**
     * Queries whether the DocumentBuilder has namespaces enabled.
     * 
     * @return true if namespaces are turned on, false otherwise.
     */
    public abstract boolean isNamespaceAware();

    /**
     * Queries whether the DocumentBuilder has validating enabled.
     * 
     * @return true if validating is turned on, false otherwise.
     */
    public abstract boolean isValidating();

    /**
     * Queries whether the DocumentBuilder has XInclude support enabled.
     * 
     * @return true if XInclude support is turned on, false otherwise.
     * 
     * @throws UnsupportedOperationException when the underlying imlementation
     *         doesn't support XInclude.
     */
    public boolean isXIncludeAware() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new document, serving as the starting point for a DOM tree.
     * 
     * @return The document.
     */
    public abstract Document newDocument();

    /**
     * Parses a given XML file and builds a DOM tree from it.
     * 
     * @param file The file to be parsed.
     * @return The document element that represents the root of the DOM tree.
     * 
     * @throws SAXException if the XML parsing fails.
     * @throws IOException if an input/output error occurs.
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
     * @param stream The stream to be parsed.
     * @return The document element that represents the root of the DOM tree.
     * 
     * @throws SAXException if the XML parsing fails.
     * @throws IOException if an input/output error occurs.
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
     * @param stream The stream to be parsed.
     * @param systemId The base for resolving relative URIs.
     * @return The document element that represents the root of the DOM tree.
     * 
     * @throws SAXException if the XML parsing fails.
     * @throws IOException if an input/output error occurs.
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
     * @param uri The URI to fetch the XML stream from.
     * @return The document element that represents the root of the DOM tree.
     * 
     * @throws SAXException if the XML parsing fails.
     * @throws IOException if an input/output error occurs.
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
     * @param source The input source to parse.
     * @return The document element that represents the root of the DOM tree.
     * 
     * @throws SAXException if the XML parsing fails.
     * @throws IOException if an input/output error occurs.
     */
    public abstract org.w3c.dom.Document parse(InputSource source)
            throws SAXException, IOException;

    /**
     * Resets the DocumentBuilder to the same state is was in after its
     * creation.
     */
    public void reset() {
        // Do nothing.
    }

    /**
     * Sets the EntityResolver used for resolving entities encountered during
     * the parse process. Passing null results in the DocumentBuilder's own
     * EntityResolver being used.
     * 
     * @param resolver The EntityResolver to use, or null for the built-in one.
     */
    public abstract void setEntityResolver(EntityResolver resolver);

    /**
     * Sets the ErrorHandler used for dealing with errors encountered during the
     * parse process. Passing null results in the DocumentBuilder's own
     * ErrorHandler being used.
     * 
     * @param handler The ErrorHandler to use, or null for the built-in one.
     */
    public abstract void setErrorHandler(ErrorHandler handler);

}
