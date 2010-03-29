/*
 * Copyright (C) 2010 The Android Open Source Project
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

package tests.xml;

import dalvik.annotation.KnownFailure;
import junit.framework.TestCase;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

/**
 * Initiate and observe a SAX parse session.
 */
public class SaxTest extends TestCase {

    public void testNoPrefixesNoNamespaces() throws Exception {
        parse(false, false, "<foo bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("", localName);
                assertEquals("foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertOneOf("bar", "", attributes.getLocalName(0));
                assertEquals("bar", attributes.getQName(0));
            }
        });

        parse(false, false, "<a:foo a:bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("", localName);
                assertEquals("a:foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertOneOf("a:bar", "", attributes.getLocalName(0));
                assertEquals("a:bar", attributes.getQName(0));
            }
        });
    }

    public void testNoPrefixesYesNamespaces() throws Exception {
        parse(false, true, "<foo bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("foo", localName);
                assertEquals("foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertEquals("bar", attributes.getLocalName(0));
                assertEquals("bar", attributes.getQName(0));
            }
        });

        parse(false, true, "<a:foo a:bar=\"baz\" xmlns:a=\"http://quux\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("http://quux", uri);
                assertEquals("foo", localName);
                assertEquals("a:foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("http://quux", attributes.getURI(0));
                assertEquals("bar", attributes.getLocalName(0));
                assertEquals("a:bar", attributes.getQName(0));
            }
        });
    }

    /**
     * Android's Expat-based SAX parser fails this test because Expat doesn't
     * supply us with our much desired {@code xmlns="http://..."} attributes.
     */
    @KnownFailure("No xmlns attributes from Expat")
    public void testYesPrefixesYesNamespaces() throws Exception {
        parse(true, true, "<foo bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("foo", localName);
                assertEquals("foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertEquals("bar", attributes.getLocalName(0));
                assertEquals("bar", attributes.getQName(0));
            }
        });

        parse(true, true, "<a:foo a:bar=\"baz\" xmlns:a=\"http://quux\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("http://quux", uri);
                assertEquals("foo", localName);
                assertEquals("a:foo", qName);
                assertEquals(2, attributes.getLength());
                assertEquals("http://quux", attributes.getURI(0));
                assertEquals("bar", attributes.getLocalName(0));
                assertEquals("a:bar", attributes.getQName(0));
                assertEquals("", attributes.getURI(1));
                assertEquals("", attributes.getLocalName(1));
                assertEquals("xmlns:a", attributes.getQName(1));
            }
        });
    }

    public void testYesPrefixesNoNamespaces() throws Exception {
        parse(true, false, "<foo bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("", localName);
                assertEquals("foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertOneOf("bar", "", attributes.getLocalName(0));
                assertEquals("bar", attributes.getQName(0));
            }
        });

        parse(true, false, "<a:foo a:bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("", localName);
                assertEquals("a:foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertOneOf("a:bar", "", attributes.getLocalName(0));
                assertEquals("a:bar", attributes.getQName(0));
            }
        });
    }

    private void parse(boolean prefixes, boolean namespaces, String xml,
            ContentHandler handler) throws Exception {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", prefixes);
        reader.setFeature("http://xml.org/sax/features/namespaces", namespaces);
        reader.setContentHandler(handler);
        reader.parse(new InputSource(new StringReader(xml)));
    }

    /**
     * @param expected an optional value that may or may have not been supplied
     * @param sentinel a marker value that means the expected value was omitted
     */
    private void assertOneOf(String expected, String sentinel, String actual) {
        List<String> optionsList = Arrays.asList(sentinel, expected);
        assertTrue("Expected one of " + optionsList + " but was " + actual,
                optionsList.contains(actual));
    }
}
