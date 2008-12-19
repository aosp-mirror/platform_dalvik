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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import org.xml.sax.Attributes;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import tests.api.javax.xml.parsers.SAXParserTestSupport.MyDefaultHandler;
import tests.api.javax.xml.parsers.SAXParserTestSupport.MyHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

@SuppressWarnings("deprecation")
@TestTargetClass(SAXParser.class) 
public class SAXParserTest extends TestCase {

    private class MockSAXParser extends SAXParser {
        public MockSAXParser() {
            super();
        }

        /*
         * @see javax.xml.parsers.SAXParser#getParser()
         */
        @SuppressWarnings("deprecation")
        @Override
        public Parser getParser() throws SAXException {
            // it is a fake
            return null;
        }

        /*
         * @see javax.xml.parsers.SAXParser#getProperty(java.lang.String)
         */
        @Override
        public Object getProperty(String name) throws SAXNotRecognizedException,
                SAXNotSupportedException {
            // it is a fake
            return null;
        }

        /*
         * @see javax.xml.parsers.SAXParser#getXMLReader()
         */
        @Override
        public XMLReader getXMLReader() throws SAXException {
            // it is a fake
            return null;
        }

        /*
         * @see javax.xml.parsers.SAXParser#isNamespaceAware()
         */
        @Override
        public boolean isNamespaceAware() {
            // it is a fake
            return false;
        }

        /*
         * @see javax.xml.parsers.SAXParser#isValidating()
         */
        @Override
        public boolean isValidating() {
            // it is a fake
            return false;
        }

        /*
         * @see javax.xml.parsers.SAXParser#setProperty(java.lang.String,
         * java.lang.Object)
         */
        @Override
        public void setProperty(String name, Object value) throws
                SAXNotRecognizedException, SAXNotSupportedException {
            // it is a fake
        }
    }

    SAXParserFactory spf;

    SAXParser parser;

    static HashMap<String, String> ns;

    static Vector<String> el;

    static HashMap<String, String> attr;

    SAXParserTestSupport sp = new SAXParserTestSupport();

    File [] list_wf;
    File [] list_nwf;
    File [] list_out_dh;
    File [] list_out_hb;

    boolean validating = false;

    private InputStream getResource(String name) {
        return this.getClass().getResourceAsStream(name);        
    }
    
    public SAXParserTest() throws Exception{
        // we differntiate between a validating and a non validating parser
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            validating = parser.isValidating();
        } catch (Exception e) {
            fail("could not obtain a SAXParser");
        }

        // nwf = non well formed, wf = well formed 
        list_wf = new File[] {File.createTempFile(
                SAXParserTestSupport.XML_WF + "staff","xml")};
        list_nwf = new File[] {File.createTempFile(
                SAXParserTestSupport.XML_NWF + "staff","xml")};

        copyFile(getResource(SAXParserTestSupport.XML_WF + "staff.xml"),
                list_wf[0].getAbsolutePath());
        copyFile(getResource(SAXParserTestSupport.XML_WF + "staff.dtd"),
                File.createTempFile(SAXParserTestSupport.XML_WF + "staff",
                        "dtd").getAbsolutePath());
        copyFile(getResource(SAXParserTestSupport.XML_NWF + "staff.xml"),
                list_nwf[0].getAbsolutePath());
        copyFile(getResource(SAXParserTestSupport.XML_NWF + "staff.dtd"),
                File.createTempFile(SAXParserTestSupport.XML_NWF + "staff",
                        "dtd").getAbsolutePath());

        list_out_dh = new File[] {File.createTempFile(
                SAXParserTestSupport.XML_WF_OUT_DH + "staff", "out")};
        list_out_hb = new File[] {File.createTempFile(
                SAXParserTestSupport.XML_WF_OUT_HB + "staff", "out")};
        copyFile(getResource(SAXParserTestSupport.XML_WF_OUT_HB + "staff.out"),
                list_out_hb[0].getAbsolutePath());
        copyFile(getResource(SAXParserTestSupport.XML_WF_OUT_DH + "staff.out"),
                list_out_dh[0].getAbsolutePath());
    }

    private void copyFile(InputStream toCopy, String target) throws Exception {
        new File(target).getParentFile().mkdirs();
        OutputStream writer = new FileOutputStream(target);
        byte[] buffer = new byte[512];
        int i = toCopy.read(buffer);
        while (i >= 0) {
            writer.write(buffer,0,i);
            i = toCopy.read(buffer);
        }
        writer.flush();
        writer.close();
        toCopy.close();
    }
    
    @Override
    protected void setUp() throws Exception {
        spf = SAXParserFactory.newInstance();
        parser = spf.newSAXParser(); 
        assertNotNull(parser);

        ns = new HashMap<String, String>();
        attr = new HashMap<String, String>();
        el = new Vector<String>();
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
//    public static void main(String[] args) throws Exception {
//        SAXParserTest st = new SAXParserTest();
//        st.setUp();
//        st.generateDataFromReferenceImpl();
//        
//    }
//    
//    private void generateDataFromReferenceImpl() {
//        try {
//            for(int i = 0; i < list_wf.length; i++) {
//                MyDefaultHandler dh = new MyDefaultHandler();
//                InputStream is = new FileInputStream(list_wf[i]);
//                parser.parse(is, dh, ParsingSupport.XML_SYSTEM_ID);
//                HashMap refHm = dh.createData();
//                
//                StringBuilder sb = new StringBuilder();
//                for (int j = 0; j < ParsingSupport.KEYS.length; j++) {
//                    String key = ParsingSupport.KEYS[j];
//                    sb.append(refHm.get(key)).append(
//                            ParsingSupport.SEPARATOR_DATA);
//                }
//                FileWriter fw = new FileWriter("/tmp/build_dh"+i+".out");
//                fw.append(sb.toString());
//                fw.close();
//            }
//            
//            for(int i = 0; i < list_nwf.length; i++) {
//                MyHandler hb = new MyHandler();
//                InputStream is = new FileInputStream(list_wf[i]);
//                parser.parse(is, hb, ParsingSupport.XML_SYSTEM_ID);
//                HashMap refHm = hb.createData();
//                
//                StringBuilder sb = new StringBuilder();
//                for (int j = 0; j < ParsingSupport.KEYS.length; j++) {
//                    String key = ParsingSupport.KEYS[j];
//                    sb.append(refHm.get(key)).append(
//                            ParsingSupport.SEPARATOR_DATA);
//                }
//                FileWriter fw = new FileWriter("/tmp/build_hb"+i+".out");
//                fw.append(sb.toString());
//                fw.close();
//            }
//
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * @tests javax.xml.parser.SAXParser#SAXParser().
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "SAXParser",
          methodArgs = {}
        )
    })
    public void _test_Constructor() {
        try {
            new MockSAXParser();
        } catch (Exception e) {
            fail("unexpected exception " + e.toString());
        }
    }

    /**
     * @tests javax.xml.parser.SAXParser#getSchema().
     * TODO getSchema() IS NOT SUPPORTED
     */
    /*   public void test_getSchema() {
        assertNull(parser.getSchema());
        SchemaFactory sf =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = sf.newSchema();
            spf.setSchema(schema);
            assertNotNull(spf.newSAXParser().getSchema());
        } catch (ParserConfigurationException pce) {
            fail("Unexpected ParserConfigurationException " + pce.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
    }
     */

    /**
     * @test javax.xml.parsers.SAXParser#isNamespaceAware()
     *
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "isNamespaceAware",
          methodArgs = {}
        )
    })
    public void _test_isNamespaceAware() {

        spf.setNamespaceAware(true);
        assertTrue(spf.isNamespaceAware());
        spf.setNamespaceAware(false);
        assertFalse(spf.isNamespaceAware());
        spf.setNamespaceAware(true);
        assertTrue(spf.isNamespaceAware());
    }

    /**
     * @test javax.xml.parsers.SAXParser#isValidating()
     *
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "isValidating",
          methodArgs = {}
        )
    })
    public void _test_isValidating() {
        assertFalse(parser.isValidating());
        spf.setValidating(true);
        assertFalse(parser.isValidating());
        spf.setValidating(false);
        assertFalse(parser.isValidating());
    }

    /**
     * @test javax.xml.parser.SAXParser#isXIncludeAware()
     * TODO X include aware is not supported
     */
//    public void test_isXIncludeAware() {
//        assertFalse(parser.isXIncludeAware());
//        spf.setXIncludeAware(true);
//        assertFalse(parser.isXIncludeAware());
//    }

    /**
     * @test javax.xml.parsers.SAXParser#parse(java.io.File,
     *     org.xml.sax.helpers.DefaultHandler)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify positive functionality properly; " +
            "not all exceptions are verified.",
      targets = {
        @TestTarget(
          methodName = "parse",
          methodArgs = {java.io.File.class, org.xml.sax.helpers.DefaultHandler.class}
        )
    })
    public void _test_parseLjava_io_FileLorg_xml_sax_helpers_DefaultHandler()
    throws Exception {

        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm =
                new SAXParserTestSupport().readFile(list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse(list_wf[i], dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                parser.parse(list_nwf[i], dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((File) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            parser.parse(list_wf[0], (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    /**
     * @test javax.xml.parsers.SAXParser#parse(java.io.File,
     *     org.xml.sax.HandlerBase)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify positive functionality properly; " +
            "not all exceptions are verified.",
      targets = {
        @TestTarget(
          methodName = "parse",
          methodArgs = {java.io.File.class, org.xml.sax.HandlerBase.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void _test_parseLjava_io_FileLorg_xml_sax_HandlerBase()
    throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = sp.readFile(list_out_hb[i].getPath());
            MyHandler dh = new MyHandler();
            parser.parse(list_wf[i], dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                parser.parse(list_nwf[i], dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyHandler dh = new MyHandler();
            parser.parse((File) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            parser.parse(list_wf[0], (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    /**
     * @test javax.xml.parsers.SAXParser#parse(org.xml.sax.InputSource,
     *     org.xml.sax.helpers.DefaultHandler)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify IOException.",
      targets = {
        @TestTarget(
          methodName = "parse",
          methodArgs = {org.xml.sax.InputSource.class, org.xml.sax.helpers.DefaultHandler.class}
        )
    })
    public void _test_parseLorg_xml_sax_InputSourceLorg_xml_sax_helpers_DefaultHandler()
    throws Exception {

        for(int i = 0; i < list_wf.length; i++) {

            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
                    list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            InputSource is = new InputSource(new FileInputStream(list_wf[i]));
            parser.parse(is, dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                InputSource is = new InputSource(
                        new FileInputStream(list_nwf[i]));
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((InputSource) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            InputSource is = new InputSource(new FileInputStream(list_wf[0]));
            parser.parse(is, (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    /**
     * @test javax.xml.parsers.SAXParser#parse(org.xml.sax.InputSource,
     *     org.xml.sax.HandlerBase)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify IOException.",
      targets = {
        @TestTarget(
          methodName = "parse",
          methodArgs = {org.xml.sax.InputSource.class, org.xml.sax.HandlerBase.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void _test_parseLorg_xml_sax_InputSourceLorg_xml_sax_HandlerBase()
    throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = sp.readFile(list_out_hb[i].getPath());
            MyHandler dh = new MyHandler();
            InputSource is = new InputSource(new FileInputStream(list_wf[i]));
            parser.parse(is, dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                InputSource is = new InputSource(new FileInputStream(
                        list_nwf[i]));
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyHandler dh = new MyHandler();
            parser.parse((InputSource) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            InputSource is = new InputSource(new FileInputStream(list_wf[0]));
            parser.parse(is, (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    /**
     * @test javax.xml.parsers.SAXParser#parse(java.io.InputStream,
     *     org.xml.sax.helpers.DefaultHandler)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify IOException.",
      targets = {
        @TestTarget(
          methodName = "parse",
          methodArgs = {java.io.InputStream.class, org.xml.sax.helpers.DefaultHandler.class}
        )
    })
    public void _test_parseLjava_io_InputStreamLorg_xml_sax_helpers_DefaultHandler()
    throws Exception {

        for(int i = 0; i < list_wf.length; i++) {

            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
                    list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            InputStream is = new FileInputStream(list_wf[i]);
            parser.parse(is, dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                InputStream is = new FileInputStream(list_nwf[i]);
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((InputStream) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    /**
     * @test javax.xml.parsers.SAXParser#parse(java.io.InputStream,
     *     org.xml.sax.helpers.DefaultHandler, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify  IOException.",
      targets = {
        @TestTarget(
          methodName = "parse",
          methodArgs = {java.io.InputStream.class, org.xml.sax.helpers.DefaultHandler.class, java.lang.String.class}
        )
    })
    public void _test_parseLjava_io_InputStreamLorg_xml_sax_helpers_DefaultHandlerLjava_lang_String()
    throws Exception {
        for(int i = 0; i < list_wf.length; i++) {

            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
                    list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            InputStream is = new FileInputStream(list_wf[i]);
            parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                InputStream is = new FileInputStream(list_nwf[i]);
                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((InputStream) null, dh,
                    SAXParserTestSupport.XML_SYSTEM_ID);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, (DefaultHandler) null, 
                    SAXParserTestSupport.XML_SYSTEM_ID);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }

        // TODO commented out since our parser is nonvalidating and thus never
        // tries to load staff.dtd in "/" ... and therefore never can fail with
        // an IOException
        /*try {
            MyDefaultHandler dh = new MyDefaultHandler();
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, dh, "/");
            fail("Expected IOException was not thrown");
        } catch(IOException ioe) {
            // expected
        }*/
    }

    /**
     * @test javax.xml.parsers.SAXParser#parse(java.io.InputStream,
     *     org.xml.sax.HandlerBase)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify positive functionality properly; " +
            "not all exceptions are verified.",
      targets = {
        @TestTarget(
          methodName = "parse",
          methodArgs = {java.io.InputStream.class, org.xml.sax.HandlerBase.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void _test_parseLjava_io_InputStreamLorg_xml_sax_HandlerBase()
    throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = sp.readFile(list_out_hb[i].getPath());
            MyHandler dh = new MyHandler();
            InputStream is = new FileInputStream(list_wf[i]);
            parser.parse(is, dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                InputStream is = new FileInputStream(list_nwf[i]);
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyHandler dh = new MyHandler();
            parser.parse((InputStream) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    /**
     * @test javax.xml.parsers.SAXParser#parse(java.io.InputStream,
     *     org.xml.sax.HandlerBase, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "parse",
          methodArgs = {java.io.InputStream.class, org.xml.sax.HandlerBase.class, java.lang.String.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void _test_parseLjava_io_InputStreamLorg_xml_sax_HandlerBaseLjava_lang_String() {
        for(int i = 0; i < list_wf.length; i++) {
            try {
                HashMap<String, String> hm = sp.readFile(
                        list_out_hb[i].getPath());
                MyHandler dh = new MyHandler();
                InputStream is = new FileInputStream(list_wf[i]);
                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
                assertTrue(SAXParserTestSupport.equalsMaps(hm, 
                        dh.createData()));
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            } catch (SAXException sax) {
                fail("Unexpected SAXException " + sax.toString());
            }
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                InputStream is = new FileInputStream(list_nwf[i]);
                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            } catch (FileNotFoundException fne) {
                fail("Unexpected FileNotFoundException " + fne.toString());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            }
        }

        try {
            MyHandler dh = new MyHandler();
            parser.parse((InputStream) null, dh, 
                    SAXParserTestSupport.XML_SYSTEM_ID);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        try {
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, (HandlerBase) null, 
                    SAXParserTestSupport.XML_SYSTEM_ID);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        // TODO commented out since our parser is nonvalidating and thus never
        // tries to load staff.dtd in "/" ... and therefore never can fail with
        // an IOException
        /*try {
            MyHandler dh = new MyHandler();
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, dh, "/");
            fail("Expected IOException was not thrown");
        } catch (IOException ioe) {
            // expected
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }*/

    }

    /**
     * @test javax.xml.parsers.SAXParser#parse(java.lang.String,
     *     org.xml.sax.helpers.DefaultHandler)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify IOException.",
      targets = {
        @TestTarget(
          methodName = "parse",
          methodArgs = {java.lang.String.class, org.xml.sax.helpers.DefaultHandler.class}
        )
    })
    public void _test_parseLjava_lang_StringLorg_xml_sax_helpers_DefaultHandler()
    throws Exception {

        for(int i = 0; i < list_wf.length; i++) {

            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
                    list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse(list_wf[i].getPath(), dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                parser.parse(list_nwf[i].getPath(), dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((String) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            parser.parse(list_wf[0].getPath(), (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    /**
     * @tests javax.xml.parsers.SAXParser#parse(java.lang.String,
     *    org.xml.sax.HandlerBase)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify IOException.",
      targets = {
        @TestTarget(
          methodName = "parse",
          methodArgs = {java.lang.String.class, org.xml.sax.HandlerBase.class}
        )
    })
    @SuppressWarnings("deprecation")
    public void _test_parseLjava_lang_StringLorg_xml_sax_HandlerBase()
    throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = sp.readFile(list_out_hb[i].getPath());
            MyHandler dh = new MyHandler();
            parser.parse(list_wf[i].getPath(), dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                parser.parse(list_nwf[i].getPath(), dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyHandler dh = new MyHandler();
            parser.parse((String) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            parser.parse(list_wf[0].getPath(), (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    /**
     * @tests javax.xml.parsers.SAXParser#reset().
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't check that SAXParser is reset to the same state " +
            "as it was created.",
      targets = {
        @TestTarget(
          methodName = "reset",
          methodArgs = {}
        )
    })
    public void _test_reset() {
        try {
            spf = SAXParserFactory.newInstance();
            parser = spf.newSAXParser();
            parser.reset();
        } catch (ParserConfigurationException pce) {
            fail("Unexpected ParserConfigurationException " + pce.toString());
        } catch(SAXException se) {
            fail("Unexpected SAXException " + se.toString());
        }
    }

    
}
