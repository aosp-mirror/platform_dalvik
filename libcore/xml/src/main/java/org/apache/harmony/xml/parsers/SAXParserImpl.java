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

import org.apache.harmony.xml.ExpatReader;

import java.util.Map;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderAdapter;

/**
 * Provides a straightforward SAXParser implementation based on ExpatReader.
 * The class is used internally only, thus only notable members that are not
 * already in the abstract superclass are documented. Hope that's ok.
 */
class SAXParserImpl extends SAXParser {

    private XMLReader reader;

    private Parser parser;

    SAXParserImpl(Map<String, Boolean> features)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        reader = new ExpatReader();

        for (Map.Entry<String,Boolean> entry : features.entrySet()) {
            reader.setFeature(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Parser getParser() {
        if (parser == null) {
            parser = new XMLReaderAdapter(reader);
        }

        return parser;
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return reader.getProperty(name);
    }

    @Override
    public XMLReader getXMLReader() {
        return reader;
    }

    @Override
    public boolean isNamespaceAware() {
        try {
            return reader.getFeature("http://xml.org/sax/features/namespaces");
        } catch (SAXException ex) {
            return false;
        }
    }

    @Override
    public boolean isValidating() {
        return false;
    }

    @Override
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        reader.setProperty(name, value);
    }
}
