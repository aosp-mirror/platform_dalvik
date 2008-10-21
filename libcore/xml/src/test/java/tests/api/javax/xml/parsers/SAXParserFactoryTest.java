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

package tests.api.javax.xml.parsers;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;
import java.io.*;
import junit.framework.TestCase;

import java.util.Vector;
import java.util.HashMap;
import javax.xml.parsers.*;

public class SAXParserFactoryTest extends TestCase {

    SAXParserFactory spf;

    InputStream is1;

    static HashMap<String, String> ns;

    static Vector<String> el;

    static HashMap<String, String> attr;

    public void setUp() throws Exception {
        spf = SAXParserFactory.newInstance();

        is1 = getClass().getResourceAsStream("/simple.xml");

        ns = new HashMap<String, String>();
        attr = new HashMap<String, String>();
        el = new Vector<String>();
    }

    public void tearDown() throws Exception {
        is1.close();
    }

    /**
     * @test javax.xml.parsers.SAXParserFactory#SAXParserFactory()
     *
     */
    public void test_Constructor() {
        MySAXParserFactory mpf = new MySAXParserFactory();
        assertTrue(mpf instanceof SAXParserFactory);
        assertFalse(mpf.isValidating());
    }

    /**
     * @test javax.xml.parsers.SAXParserFactory#getFeature(java.lang.String)
     *
     */
    public void test_getFeatureLjava_lang_String() {
        String[] features = {
                "http://xml.org/sax/features/namespaces",
        "http://xml.org/sax/features/validation"};
        for (int i = 0; i < features.length; i++) {
            try {
                spf.setFeature(features[i], true);
                assertTrue(spf.getFeature(features[i]));
                spf.setFeature(features[i], false);
                assertFalse(spf.getFeature(features[i]));
            } catch (ParserConfigurationException pce) {
                fail("ParserConfigurationException is thrown");
            } catch (SAXNotRecognizedException snre) {
                fail("SAXNotRecognizedException is thrown");
            } catch (SAXNotSupportedException snse) {
                fail("SAXNotSupportedException is thrown");
            }
        }

        try {
            spf.getFeature("");
            fail("SAXNotRecognizedException is not thrown");
        } catch (ParserConfigurationException pce) {
            fail("ParserConfigurationException is thrown");
        } catch (SAXNotRecognizedException snre) {
            //expected
        } catch (SAXNotSupportedException snse) {
            fail("SAXNotSupportedException is thrown");
        } catch (NullPointerException npe) {
            fail("NullPointerException is thrown");
        }

        try {
            spf.getFeature(null);
            fail("NullPointerException is not thrown");
        } catch (ParserConfigurationException pce) {
            fail("ParserConfigurationException is thrown");
        } catch (SAXNotRecognizedException snre) {
            fail("SAXNotRecognizedException is thrown");
        } catch (SAXNotSupportedException snse) {
            fail("SAXNotSupportedException is thrown");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @tests javax.xml.parsers.SAXParserFactory#getSchema().
     * TBD getSchema() IS NOT SUPPORTED
     */
    /*   public void test_getSchema() {
        assertNull(spf.getSchema());
        SchemaFactory sf =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = sf.newSchema();
            spf.setSchema(schema);
            assertNotNull(spf.getSchema());
        } catch (SAXException sax) {
            fail("Unexpected exception " + sax.toString());
        }
    }
     */
    /**
     * @test javax.xml.parsers.SAXParserFactory#isNamespaceAware()
     *
     */
    public void test_isNamespaceAware() {
        spf.setNamespaceAware(true);
        assertTrue(spf.isNamespaceAware());
        spf.setNamespaceAware(false);
        assertFalse(spf.isNamespaceAware());
        spf.setNamespaceAware(true);
        assertTrue(spf.isNamespaceAware());
    }

    /**
     * @test javax.xml.parsers.SAXParserFactory#isValidating()
     *
     */
    public void test_isValidating() {
        spf.setValidating(true);
        assertTrue(spf.isValidating());
        spf.setValidating(false);
        assertFalse(spf.isValidating());
        spf.setValidating(true);
        assertTrue(spf.isValidating());
    }

    /**
     * @test javax.xml.parsers.SAXParserFactory#isXIncludeAware()
     *
     */
//    public void test_isXIncludeAware() {
//        assertFalse(spf.isXIncludeAware());
//        spf.setXIncludeAware(true);
//        assertTrue(spf.isXIncludeAware());
//        spf.setXIncludeAware(false);
//        assertFalse(spf.isXIncludeAware());
//    }

    /**
     * @test javax.xml.parsers.SAXParserFactory#newInstance()
     *
     */
    public void test_newInstance() {
        String className = null;
        try {
            SAXParserFactory dtf = SAXParserFactory.newInstance();
            assertNotNull("New Instance of DatatypeFactory is null", dtf);

            className = System.getProperty("javax.xml.parsers.SAXParserFactory");

            System.setProperty("javax.xml.parsers.SAXParserFactory",
            "org.apache.harmony.xml.parsers.SAXParserFactoryImpl");

            SAXParserFactory spf1 = SAXParserFactory.newInstance();
            assertTrue(spf1 instanceof org.apache.harmony.xml.parsers.SAXParserFactoryImpl);

            String key = "javax.xml.parsers.SAXParserFactory = org.apache.harmony.xml.parsers.SAXParserFactoryImpl";

            ByteArrayInputStream bis = new ByteArrayInputStream(key.getBytes());
            Properties prop = System.getProperties();
            prop.load(bis);
            SAXParserFactory spf2 = SAXParserFactory.newInstance();
            assertTrue(spf2 instanceof org.apache.harmony.xml.parsers.SAXParserFactoryImpl);

            System.setProperty("javax.xml.parsers.SAXParserFactory", "");
            try {
                SAXParserFactory.newInstance();
                fail("Expected FactoryConfigurationError was not thrown");
            } catch (FactoryConfigurationError e) {
                // expected
            }
        } catch (IOException ioe) {
            fail("Unexpected exception " + ioe.toString());
        } finally {
            if (className == null) {
                System.clearProperty("javax.xml.parsers.SAXParserFactory");
            } else {
                System.setProperty("javax.xml.parsers.SAXParserFactory",
                        className);
            }
        }
    }

    /**
     * @test javax.xml.parsers.SAXParserFactory#newSAXParser()
     *
     */
    public void test_newSAXParser() {
        try {
            SAXParser sp = spf.newSAXParser();
            assertTrue(sp instanceof SAXParser);
            sp.parse(is1, new MyHandler());
        } catch(Exception e) {
            fail("Exception was thrown: " + e.toString());
        }
    }

    /**
     * @test javax.xml.parsers.SAXParserFactory#setFeature(java.lang.String,
     *       boolean)
     *
     */
    public void test_setFeatureLjava_lang_StringZ() {
        String[] features = {
                "http://xml.org/sax/features/namespaces",
                "http://xml.org/sax/features/validation" };
        for (int i = 0; i < features.length; i++) {
            try {
                spf.setFeature(features[i], true);
                assertTrue(spf.getFeature(features[i]));
                spf.setFeature(features[i], false);
                assertFalse(spf.getFeature(features[i]));
            } catch (ParserConfigurationException pce) {
                fail("ParserConfigurationException is thrown");
            } catch (SAXNotRecognizedException snre) {
                fail("SAXNotRecognizedException is thrown");
            } catch (SAXNotSupportedException snse) {
                fail("SAXNotSupportedException is thrown");
            }
        }

        try {
            spf.setFeature("", true);
            fail("SAXNotRecognizedException is not thrown");
        } catch (ParserConfigurationException pce) {
            fail("ParserConfigurationException is thrown");
        } catch (SAXNotRecognizedException snre) {
            //expected
        } catch (SAXNotSupportedException snse) {
            fail("SAXNotSupportedException is thrown");
        } catch (NullPointerException npe) {
            fail("NullPointerException is thrown");
        }

// Doesn't make sense for us.
//        try {
//            spf.setFeature("http://xml.org/sax/features/use-attributes2", true);
//            fail("SAXNotSupportedException is not thrown");
//        } catch (ParserConfigurationException pce) {
//            fail("ParserConfigurationException is thrown");
//        } catch (SAXNotRecognizedException snre) {
//            fail("SAXNotRecognizedException is thrown");
//        } catch (SAXNotSupportedException snse) {
//            //expected
//        } catch (NullPointerException npe) {
//            fail("NullPointerException is thrown");
//        }

        try {
            spf.setFeature(null, true);
            fail("NullPointerException is not thrown");
        } catch (ParserConfigurationException pce) {
            fail("ParserConfigurationException is thrown");
        } catch (SAXNotRecognizedException snre) {
            fail("SAXNotRecognizedException is thrown");
        } catch (SAXNotSupportedException snse) {
            fail("SAXNotSupportedException is thrown");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @test javax.xml.parsers.SAXParserFactory#setNamespaceAware(boolean)
     *
     */
    public void test_setNamespaceAwareZ() {

        spf.setNamespaceAware(true);
        MyHandler mh = new MyHandler();
        InputStream is = getClass().getResourceAsStream("/simple_ns.xml");
        try {
            spf.newSAXParser().parse(is, mh);
        } catch(javax.xml.parsers.ParserConfigurationException pce) {
            fail("ParserConfigurationException was thrown during parsing");
        } catch(org.xml.sax.SAXException se) {
            se.printStackTrace();
            fail("SAXException was thrown during parsing");
        } catch(IOException ioe) {
            fail("IOException was thrown during parsing");
        } finally {
            try {
                is.close();
            } catch(Exception e) {}
        }
        spf.setNamespaceAware(false);
        is = getClass().getResourceAsStream("/simple_ns.xml");
        try {
            is = getClass().getResourceAsStream("/simple_ns.xml");
            spf.newSAXParser().parse(is, mh);
        } catch(javax.xml.parsers.ParserConfigurationException pce) {
            fail("ParserConfigurationException was thrown during parsing");
        } catch(org.xml.sax.SAXException se) {
            fail("SAXException was thrown during parsing");
        } catch(IOException ioe) {
            fail("IOException was thrown during parsing");
        } finally {
            try {
                is.close();
            } catch(Exception ioee) {}
        }
        is = getClass().getResourceAsStream("/simple_ns.xml");
        try {
            spf.setNamespaceAware(true);
            spf.newSAXParser().parse(is, mh);
        } catch(javax.xml.parsers.ParserConfigurationException pce) {
            fail("ParserConfigurationException was thrown during parsing");
        } catch(org.xml.sax.SAXException se) {
            fail("SAXException was thrown during parsing");
        } catch(IOException ioe) {
            fail("IOException was thrown during parsing");
        } finally {
            try {
                is.close();
            } catch(Exception ioee) {}
        }
    }

    /**
     * @tests javax.xml.parsers.SAXParserFactory#setSchema(javax.xml.validation.Schema)
     * TBD getSchema() IS NOT SUPPORTED
     */
    /*   public void test_setSchemaLjavax_xml_validation_Schema() {
        SchemaFactory sf =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = sf.newSchema();
            spf.setSchema(schema);
            assertNotNull(spf.getSchema());
        } catch (SAXException sax) {
            fail("Unexpected exception " + sax.toString());
        }
    }
     */
    /**
     * @tests javax.xml.parsers.SAXParserFactory#setValidating(boolean)
     *
     */
//    public void test_setValidatingZ() {
//        MyHandler mh = new MyHandler();
//        InputStream is2 = getClass().getResourceAsStream("/recipe.xml");
//        try {
//            spf.setValidating(true);
//            assertTrue(spf.isValidating());
//            spf.newSAXParser().parse(is2, mh);
//        } catch (org.xml.sax.SAXException se) {
//            fail("SAXException was thrown during parsing");
//        } catch (javax.xml.parsers.ParserConfigurationException pce) {
//            fail("ParserConfigurationException was thrown during parsing");
//        } catch (IOException ioe) {
//            fail("IOException was thrown during parsing");
//        } finally {
//            try {
//                is2.close();
//            } catch(Exception ioee) {}
//        }
//        InputStream is3 = getClass().getResourceAsStream("/recipe1.xml");
//        try {
//            assertTrue(spf.isValidating());
//            spf.newSAXParser().parse(is3, mh);
//        } catch (org.xml.sax.SAXException se) {
//            fail("SAXException was thrown during parsing");
//        } catch (javax.xml.parsers.ParserConfigurationException pce) {
//            fail("ParserConfigurationException was thrown during parsing");
//        } catch (IOException ioe) {
//            fail("IOEXception was thrown during parsing: " + ioe.getMessage());
//        } finally {
//            try {
//                is3.close();
//            } catch(Exception ioee) {}
//        }
//        is2 = getClass().getResourceAsStream("/recipe.xml");
//        try {
//            spf.setValidating(false);
//            assertFalse(spf.isValidating());
//            spf.newSAXParser().parse(is2, mh);
//        } catch (org.xml.sax.SAXException se) {
//            fail("SAXException was thrown during parsing");
//        } catch (javax.xml.parsers.ParserConfigurationException pce) {
//            fail("ParserConfigurationException was thrown during parsing");
//        } catch (IOException ioe) {
//            fail("IOException was thrown during parsing");
//        } finally {
//            try {
//                is2.close();
//            } catch(Exception ioee) {}
//        }
//        is3 = getClass().getResourceAsStream("/recipe1.xml");
//        try {
//            assertFalse(spf.isValidating());
//            spf.newSAXParser().parse(is3, mh);
//        } catch (org.xml.sax.SAXException se) {
//            fail("SAXException was thrown during parsing");
//        } catch (javax.xml.parsers.ParserConfigurationException pce) {
//            fail("ParserConfigurationException was thrown during parsing");
//        } catch (IOException ioe) {
//            fail("IOEXception was thrown during parsing: " + ioe.getMessage());
//        } finally {
//            try {
//                is3.close();
//            } catch(Exception ioee) {}
//        }
//    }

    /**
     * @test javax.xml.parsers.SAXParserFactory#setXIncludeAware(boolean)
     *
     */
//    public void test_setXIncludeAwareZ() {
//        spf.setXIncludeAware(true);
//        MyHandler mh = new MyHandler();
//        InputStream is = getClass().getResourceAsStream("/simple_ns.xml");
//        try {
//            spf.newSAXParser().parse(is, mh);
//        } catch(javax.xml.parsers.ParserConfigurationException pce) {
//            fail("ParserConfigurationException was thrown during parsing");
//        } catch(org.xml.sax.SAXException se) {
//            fail("SAXException was thrown during parsing");
//        } catch(IOException ioe) {
//            fail("IOException was thrown during parsing");
//        } finally {
//            try {
//                is.close();
//            } catch(Exception ioee) {}
//        }
//        spf.setXIncludeAware(false);
//        is = getClass().getResourceAsStream("/simple_ns.xml");
//        try {
//            is = getClass().getResourceAsStream("/simple_ns.xml");
//            spf.newSAXParser().parse(is, mh);
//        } catch(javax.xml.parsers.ParserConfigurationException pce) {
//            fail("ParserConfigurationException was thrown during parsing");
//        } catch(org.xml.sax.SAXException se) {
//            fail("SAXException was thrown during parsing");
//        } catch(IOException ioe) {
//            fail("IOException was thrown during parsing");
//        } finally {
//            try {
//                is.close();
//            } catch(Exception ioee) {}
//        }
//        is = getClass().getResourceAsStream("/simple_ns.xml");
//        try {
//            spf.setXIncludeAware(true);
//            spf.newSAXParser().parse(is, mh);
//        } catch(javax.xml.parsers.ParserConfigurationException pce) {
//            fail("ParserConfigurationException was thrown during parsing");
//        } catch(org.xml.sax.SAXException se) {
//            fail("SAXException was thrown during parsing");
//        } catch(IOException ioe) {
//            fail("IOException was thrown during parsing");
//        } finally {
//            try {
//                is.close();
//            } catch(Exception ioee) {}
//        }
//    }

    static class MyHandler extends DefaultHandler {

        public void startElement(String uri, String localName, String qName,
                Attributes atts) {

            el.add(qName);
            if (!uri.equals(""))
                ns.put(qName, uri);
            for (int i = 0; i < atts.getLength(); i++) {
                attr.put(atts.getQName(i), atts.getValue(i));
            }

        }
    }

    class MySAXParserFactory extends SAXParserFactory {

        public MySAXParserFactory() {
            super();
        }

        public SAXParser newSAXParser() {
            return null;
        }

        public void setFeature(String name,
                boolean value)
        throws ParserConfigurationException,
        SAXNotRecognizedException,
        SAXNotSupportedException {

        }

        public boolean getFeature(String name)
        throws ParserConfigurationException,
        SAXNotRecognizedException,
        SAXNotSupportedException {
            return true;
        }

    }
}
