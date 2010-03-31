/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests.api.java.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import tests.support.resource.Support_Resources;

public class PropertiesTest extends junit.framework.TestCase {

    Properties tProps;

    byte[] propsFile;

    /**
     * @tests java.util.Properties#Properties()
     */
    public void test_Constructor() {
        // Test for method java.util.Properties()
        Properties p = new Properties();
        // do something to avoid getting a variable unused warning
        p.clear();
        assertTrue("Created incorrect Properties", true);
    }

    public void test_loadLjava_io_InputStream_NPE() throws Exception {
        Properties p = new Properties();
        try {
            p.load((InputStream) null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }
    
    public void test_loadsave() throws Exception{
        Properties p = new Properties();
        try {
            p.load((InputStream) null);
            fail("should throw NPE");
        } catch (NullPointerException npe) {
        	// expected
        }        
    }

    /**
     * @tests java.util.Properties#Properties(java.util.Properties)
     */
    public void test_ConstructorLjava_util_Properties() {
        Properties systemProperties = System.getProperties();
        Properties properties = new Properties(systemProperties);
        Enumeration<?> propertyNames = systemProperties.propertyNames();
        String propertyName = null;
        while (propertyNames.hasMoreElements()) {
            propertyName = (String) propertyNames.nextElement();
            assertEquals("failed to construct correct properties",
                    systemProperties.get(propertyName), properties
                            .getProperty(propertyName));
        }
    }

    /**
     * @tests java.util.Properties#getProperty(java.lang.String)
     */
    public void test_getPropertyLjava_lang_String() {
        // Test for method java.lang.String
        // java.util.Properties.getProperty(java.lang.String)
        assertEquals("Did not retrieve property", "this is a test property",
                ((String) tProps.getProperty("test.prop")));
    }

    /**
     * @tests java.util.Properties#getProperty(java.lang.String,
     *        java.lang.String)
     */
    public void test_getPropertyLjava_lang_StringLjava_lang_String() {
        // Test for method java.lang.String
        // java.util.Properties.getProperty(java.lang.String, java.lang.String)
        assertEquals("Did not retrieve property", "this is a test property",
                ((String) tProps.getProperty("test.prop", "Blarg")));
        assertEquals("Did not return default value", "Gabba", ((String) tProps
                .getProperty("notInThere.prop", "Gabba")));
    }

    /**
     * @tests java.util.Properties#getProperty(java.lang.String)
     */
    public void test_getPropertyLjava_lang_String2() {
        // regression test for HARMONY-3518
        MyProperties props = new MyProperties();
        assertNull(props.getProperty("key"));
    }

    /**
     * @tests java.util.Properties#getProperty(java.lang.String,
     *        java.lang.String)
     */
    public void test_getPropertyLjava_lang_StringLjava_lang_String2() {
        // regression test for HARMONY-3518
        MyProperties props = new MyProperties();
        assertEquals("defaultValue", props.getProperty("key", "defaultValue"));
    }

    // regression testing for HARMONY-3518
    static class MyProperties extends Properties {
        public synchronized Object get(Object key) {
            return getProperty((String) key); // assume String
        }
    }

    /**
     * @tests java.util.Properties#list(java.io.PrintStream)
     */
    public void test_listLjava_io_PrintStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        Properties myProps = new Properties();
        myProps.setProperty("Abba", "Cadabra");
        myProps.setProperty("Open", "Sesame");
        myProps.setProperty("LongProperty",
                "a long long long long long long long property");
        myProps.list(ps);
        ps.flush();
        String propList = baos.toString();
        assertTrue("Property list innacurate",
                propList.indexOf("Abba=Cadabra") >= 0);
        assertTrue("Property list innacurate",
                propList.indexOf("Open=Sesame") >= 0);
        assertTrue("property list do not conatins \"...\"", propList
                .indexOf("...") != -1);

        ps = null;
        try {
            myProps.list(ps);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Properties#list(java.io.PrintWriter)
     */
    public void test_listLjava_io_PrintWriter() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        Properties myProps = new Properties();
        myProps.setProperty("Abba", "Cadabra");
        myProps.setProperty("Open", "Sesame");
        myProps.setProperty("LongProperty",
                "a long long long long long long long property");
        myProps.list(pw);
        pw.flush();
        String propList = baos.toString();
        assertTrue("Property list innacurate",
                propList.indexOf("Abba=Cadabra") >= 0);
        assertTrue("Property list innacurate",
                propList.indexOf("Open=Sesame") >= 0);
        pw = null;
        try {
            myProps.list(pw);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Properties#load(java.io.InputStream)
     */
    public void test_loadLjava_io_InputStream() {
        // Test for method void java.util.Properties.load(java.io.InputStream)
        Properties prop = null;
        try {
            InputStream is;
            prop = new Properties();
            prop.load(is = new ByteArrayInputStream(writeProperties()));
            is.close();
        } catch (Exception e) {
            fail("Exception during load test : " + e.getMessage());
        }
        assertEquals("Failed to load correct properties", "harmony.tests", prop
                .getProperty("test.pkg"));
        assertNull("Load failed to parse incorrectly", prop
                .getProperty("commented.entry"));

        prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream("=".getBytes()));
		} catch (IOException e) {
			// expected
		}
        assertTrue("Failed to add empty key", prop.get("").equals(""));

        prop = new Properties();
        try {
			prop.load(new ByteArrayInputStream(" = ".getBytes()));
		} catch (IOException e) {
			// expected
		}
        assertTrue("Failed to add empty key2", prop.get("").equals(""));

		prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream(" a= b".getBytes()));
		} catch (IOException e) {
			// expected
		}
		assertEquals("Failed to ignore whitespace", "b", prop.get("a"));

		prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream(" a b".getBytes()));
		} catch (IOException e) {
			// expected
		}
		assertEquals("Failed to interpret whitespace as =", "b", prop.get("a"));

		prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream("#\u008d\u00d2\na=\u008d\u00d3"
					.getBytes("ISO8859_1")));
		} catch (IOException e) {
			// expected
		}
		assertEquals("Failed to parse chars >= 0x80", "\u008d\u00d3", prop
				.get("a"));

		prop = new Properties();
		try {
			prop.load(new ByteArrayInputStream(
					"#properties file\r\nfred=1\r\n#last comment"
							.getBytes("ISO8859_1")));
		} catch (IOException e) {
			// expected
		} catch (IndexOutOfBoundsException e) {
			fail("IndexOutOfBoundsException when last line is a comment with no line terminator");
		}
        assertEquals("Failed to load when last line contains a comment", "1",
                prop.get("fred"));
    }

    /**
	 * @tests java.util.Properties#load(java.io.InputStream)
	 */
    public void test_loadLjava_io_InputStream_subtest0() {
        try {
            InputStream is = Support_Resources
                    .getStream("hyts_PropertiesTest.properties");
            Properties props = new Properties();
            props.load(is);
            is.close();
            assertEquals("1", "\n \t \f", props.getProperty(" \r"));
            assertEquals("2", "a", props.getProperty("a"));
            assertEquals("3", "bb as,dn   ", props.getProperty("b"));
            assertEquals("4", ":: cu", props.getProperty("c\r \t\nu"));
            assertEquals("5", "bu", props.getProperty("bu"));
            assertEquals("6", "d\r\ne=e", props.getProperty("d"));
            assertEquals("7", "fff", props.getProperty("f"));
            assertEquals("8", "g", props.getProperty("g"));
            assertEquals("9", "", props.getProperty("h h"));
            assertEquals("10", "i=i", props.getProperty(" "));
            assertEquals("11", "   j", props.getProperty("j"));
            assertEquals("12", "   c", props.getProperty("space"));
            assertEquals("13", "\\", props.getProperty("dblbackslash"));
        } catch (IOException e) {
            fail("Unexpected: " + e);
        }
    }

    /**
     * @throws IOException
     * @tests java.util.Properties#load(java.io.Reader)
     * @since 1.6
     */
    public void test_loadLjava_io_Reader() throws IOException {
        Properties prop = null;
        try {
            InputStream is;
            prop = new Properties();
            is = new ByteArrayInputStream(writeProperties());
            prop.load(new InputStreamReader(is));
            is.close();
        } catch (Exception e) {
            fail("Exception during load test : " + e.getMessage());
        }
        assertEquals("Failed to load correct properties", "harmony.tests", prop
                .getProperty("test.pkg"));
        assertNull("Load failed to parse incorrectly", prop
                .getProperty("commented.entry"));

        prop = new Properties();
        prop.load(new ByteArrayInputStream("=".getBytes()));
        assertEquals("Failed to add empty key", "", prop.get(""));

        prop = new Properties();
        prop.load(new ByteArrayInputStream(" = ".getBytes()));
        assertEquals("Failed to add empty key2", "", prop.get(""));

        prop = new Properties();
        prop.load(new ByteArrayInputStream(" a= b".getBytes()));
        assertEquals("Failed to ignore whitespace", "b", prop.get("a"));

        prop = new Properties();
        prop.load(new ByteArrayInputStream(" a b".getBytes()));
        assertEquals("Failed to interpret whitespace as =", "b", prop.get("a"));

        prop = new Properties();
        prop.load(new ByteArrayInputStream("#comment\na=value"
                .getBytes("UTF-8")));
        assertEquals("value", prop.getProperty("a"));

        prop = new Properties();
        prop.load(new InputStreamReader(new ByteArrayInputStream(
                "#\u008d\u00d2\na=\u008d\u00d3".getBytes("UTF-8"))));
        try {
            prop
                    .load(new InputStreamReader(new ByteArrayInputStream(
                            "#\u008d\u00d2\na=\\\\u008d\\\\\\uu00d3"
                                    .getBytes("UTF-8"))));
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        prop = new Properties();
        try {
            prop.load(new InputStreamReader(new ByteArrayInputStream(
                    "#properties file\r\nfred=1\r\n#last comment"
                            .getBytes("ISO8859_1"))));
        } catch (IndexOutOfBoundsException e) {
            fail("IndexOutOfBoundsException when last line is a comment with no line terminator");
        }
        assertEquals("Failed to load when last line contains a comment", "1",
                prop.get("fred"));

        // Regression tests for HARMONY-5414
        prop = new Properties();
        prop.load(new ByteArrayInputStream("a=\\u1234z".getBytes()));

        prop = new Properties();
        try {
            prop.load(new ByteArrayInputStream("a=\\u123".getBytes()));
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        prop = new Properties();
        try {
            prop.load(new ByteArrayInputStream("a=\\u123z".getBytes()));
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Expected
        }

        prop = new Properties();
        Properties expected = new Properties();
        expected.put("a", "\u0000");
        prop.load(new ByteArrayInputStream("a=\\".getBytes()));
        assertEquals("Failed to read trailing slash value", expected, prop);

        prop = new Properties();
        expected = new Properties();
        expected.put("a", "\u1234\u0000");
        prop.load(new ByteArrayInputStream("a=\\u1234\\".getBytes()));
        assertEquals("Failed to read trailing slash value #2", expected, prop);

        prop = new Properties();
        expected = new Properties();
        expected.put("a", "q");
        prop.load(new ByteArrayInputStream("a=\\q".getBytes()));
        assertEquals("Failed to read slash value #3", expected, prop);
    }
    
    /**
     * @tests java.util.Properties#load(java.io.InputStream)
     */
    public void test_loadLjava_io_InputStream_Special() throws IOException {
        // Test for method void java.util.Properties.load(java.io.InputStream)
        Properties prop = null;
        prop = new Properties();
        prop.load(new ByteArrayInputStream("=".getBytes()));
        assertTrue("Failed to add empty key", prop.get("").equals(""));
        
        prop = new Properties();
        prop.load(new ByteArrayInputStream("=\r\n".getBytes()));
        assertTrue("Failed to add empty key", prop.get("").equals(""));
        
        prop = new Properties();
        prop.load(new ByteArrayInputStream("=\n\r".getBytes()));
        assertTrue("Failed to add empty key", prop.get("").equals(""));
    }

    /**
     * @throws IOException
     * @tests java.util.Properties#load(java.io.Reader)
     * @since 1.6
     */
    public void test_loadLjava_io_Reader_subtest0() throws IOException {
        InputStream is = Support_Resources
                .getStream("hyts_PropertiesTest.properties");
        Properties props = new Properties();
        props.load(new InputStreamReader(is));
        is.close();
        assertEquals("1", "\n \t \f", props.getProperty(" \r"));
        assertEquals("2", "a", props.getProperty("a"));
        assertEquals("3", "bb as,dn   ", props.getProperty("b"));
        assertEquals("4", ":: cu", props.getProperty("c\r \t\nu"));
        assertEquals("5", "bu", props.getProperty("bu"));
        assertEquals("6", "d\r\ne=e", props.getProperty("d"));
        assertEquals("7", "fff", props.getProperty("f"));
        assertEquals("8", "g", props.getProperty("g"));
        assertEquals("9", "", props.getProperty("h h"));
        assertEquals("10", "i=i", props.getProperty(" "));
        assertEquals("11", "   j", props.getProperty("j"));
        assertEquals("12", "   c", props.getProperty("space"));
        assertEquals("13", "\\", props.getProperty("dblbackslash"));
    }

    /**
     * @tests java.util.Properties#propertyNames()
     */
    public void test_propertyNames() {
        Properties myPro = new Properties(tProps);
        Enumeration names = myPro.propertyNames();
        int i = 0;
        while (names.hasMoreElements()) {
            ++i;
            String p = (String) names.nextElement();
            assertTrue("Incorrect names returned", p.equals("test.prop")
                    || p.equals("bogus.prop"));
        }
    }

    public void test_propertyNames_sequence() {
        Properties parent = new Properties();
        parent.setProperty("parent.a.key", "parent.a.value");
        parent.setProperty("parent.b.key", "parent.b.value");

        Enumeration<?> names = parent.propertyNames();
        assertEquals("parent.a.key", names.nextElement());
        assertEquals("parent.b.key", names.nextElement());
        assertFalse(names.hasMoreElements());

        Properties current = new Properties(parent);
        current.setProperty("current.a.key", "current.a.value");
        current.setProperty("current.b.key", "current.b.value");

        names = current.propertyNames();
        assertEquals("parent.a.key", names.nextElement());
        assertEquals("current.b.key", names.nextElement());
        assertEquals("parent.b.key", names.nextElement());
        assertEquals("current.a.key", names.nextElement());
        assertFalse(names.hasMoreElements());

        Properties child = new Properties(current);
        child.setProperty("child.a.key", "child.a.value");
        child.setProperty("child.b.key", "child.b.value");

        names = child.propertyNames();
        assertEquals("parent.a.key", names.nextElement());
        assertEquals("child.b.key", names.nextElement());
        assertEquals("current.b.key", names.nextElement());
        assertEquals("parent.b.key", names.nextElement());
        assertEquals("child.a.key", names.nextElement());
        assertEquals("current.a.key", names.nextElement());
        assertFalse(names.hasMoreElements());
    }

    /**
     * @tests {@link java.util.Properties#stringPropertyNames()}
     * @since 1.6
     */
    public void test_stringPropertyNames() {
        Set<String> set = tProps.stringPropertyNames();
        assertEquals(2, set.size());
        assertTrue(set.contains("test.prop"));
        assertTrue(set.contains("bogus.prop"));
        assertNotSame(set, tProps.stringPropertyNames());

        set = new Properties().stringPropertyNames();
        assertEquals(0, set.size());

        set = new Properties(System.getProperties()).stringPropertyNames();
        assertTrue(set.size() > 0);

        tProps = new Properties(tProps);
        tProps.put("test.prop", "anotherValue");
        tProps.put("3rdKey", "3rdValue");
        set = tProps.stringPropertyNames();
        assertEquals(3, set.size());
        assertTrue(set.contains("test.prop"));
        assertTrue(set.contains("bogus.prop"));
        assertTrue(set.contains("3rdKey"));

        tProps.put(String.class, "valueOfNonStringKey");
        set = tProps.stringPropertyNames();
        assertEquals(3, set.size());
        assertTrue(set.contains("test.prop"));
        assertTrue(set.contains("bogus.prop"));
        assertTrue(set.contains("3rdKey"));

        tProps.put("4thKey", "4thValue");
        assertEquals(4, tProps.size());
        assertEquals(3, set.size());

        try {
            set.add("another");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /**
     * @tests {@link java.util.Properties#stringPropertyNames()}
     * @since 1.6
     */
    public void test_stringPropertyNames_scenario1() {
        String[] keys = new String[] { "key1", "key2", "key3" };
        String[] values = new String[] { "value1", "value2", "value3" };
        List<String> keyList = Arrays.asList(keys);

        Properties properties = new Properties();
        for (int index = 0; index < keys.length; index++) {
            properties.setProperty(keys[index], values[index]);
        }

        properties = new Properties(properties);
        Set<String> nameSet = properties.stringPropertyNames();
        assertEquals(keys.length, nameSet.size());
        Iterator<String> iterator = nameSet.iterator();
        while (iterator.hasNext()) {
            assertTrue(keyList.contains(iterator.next()));
        }

        Enumeration<?> nameEnum = properties.propertyNames();
        int count = 0;
        while (nameEnum.hasMoreElements()) {
            count++;
            assertTrue(keyList.contains(nameEnum.nextElement()));
        }
        assertEquals(keys.length, count);

        properties = new Properties(properties);
        nameSet = properties.stringPropertyNames();
        assertEquals(keys.length, nameSet.size());
        iterator = nameSet.iterator();
        while (iterator.hasNext()) {
            assertTrue(keyList.contains(iterator.next()));
        }

        nameEnum = properties.propertyNames();
        count = 0;
        while (nameEnum.hasMoreElements()) {
            count++;
            assertTrue(keyList.contains(nameEnum.nextElement()));
        }
        assertEquals(keys.length, count);
    }

    /**
     * @tests {@link java.util.Properties#stringPropertyNames()}
     * @since 1.6
     */
    public void test_stringPropertyNames_scenario2() {
        String[] defaultKeys = new String[] { "defaultKey1", "defaultKey2",
                "defaultKey3", "defaultKey4", "defaultKey5", "defaultKey6" };
        String[] defaultValues = new String[] { "defaultValue1",
                "defaultValue2", "defaultValue3", "defaultValue4",
                "defaultValue5", "defaultValue6" };
        List<String> keyList = new ArrayList<String>();
        Properties defaults = new Properties();
        for (int index = 0; index < 3; index++) {
            defaults.setProperty(defaultKeys[index], defaultValues[index]);
            keyList.add(defaultKeys[index]);
        }

        String[] keys = new String[] { "key1", "key2", "key3" };
        String[] values = new String[] { "value1", "value2", "value3" };
        Properties properties = new Properties(defaults);
        for (int index = 0; index < keys.length; index++) {
            properties.setProperty(keys[index], values[index]);
            keyList.add(keys[index]);
        }

        Set<String> nameSet = properties.stringPropertyNames();
        assertEquals(keyList.size(), nameSet.size());
        Iterator<String> iterator = nameSet.iterator();
        while (iterator.hasNext()) {
            assertTrue(keyList.contains(iterator.next()));
        }

        Enumeration<?> nameEnum = properties.propertyNames();
        int count = 0;
        while (nameEnum.hasMoreElements()) {
            count++;
            assertTrue(keyList.contains(nameEnum.nextElement()));
        }
        assertEquals(keyList.size(), count);

        for (int index = 3; index < defaultKeys.length; index++) {
            defaults.setProperty(defaultKeys[index], defaultValues[index]);
            keyList.add(defaultKeys[index]);
        }

        nameSet = properties.stringPropertyNames();
        assertEquals(keyList.size(), nameSet.size());
        iterator = nameSet.iterator();
        while (iterator.hasNext()) {
            assertTrue(keyList.contains(iterator.next()));
        }

        nameEnum = properties.propertyNames();
        count = 0;
        while (nameEnum.hasMoreElements()) {
            count++;
            assertTrue(keyList.contains(nameEnum.nextElement()));
        }
        assertEquals(keyList.size(), count);
    }

    /**
     * @tests java.util.Properties#save(java.io.OutputStream, java.lang.String)
     */
    public void test_saveLjava_io_OutputStreamLjava_lang_String() {
        // Test for method void java.util.Properties.save(java.io.OutputStream,
        // java.lang.String)
        Properties myProps = new Properties();
        Properties myProps2 = new Properties();

        myProps.setProperty("Property A", "aye");
        myProps.setProperty("Property B", "bee");
        myProps.setProperty("Property C", "see");

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            myProps.save(out, "A Header");
            out.close();
            ByteArrayInputStream in = new ByteArrayInputStream(out
                    .toByteArray());
            myProps2.load(in);
            in.close();
        } catch (IOException ioe) {
            fail("IOException occurred reading/writing file : "
                    + ioe.getMessage());
        }

        Enumeration e = myProps.propertyNames();
        String nextKey;
        while (e.hasMoreElements()) {
            nextKey = (String) e.nextElement();
            assertEquals("Stored property list not equal to original", myProps
                    .getProperty(nextKey), myProps2.getProperty(nextKey));
        }
    }

    /**
     * @tests java.util.Properties#setProperty(java.lang.String,
     *        java.lang.String)
     */
    public void test_setPropertyLjava_lang_StringLjava_lang_String() {
        // Test for method java.lang.Object
        // java.util.Properties.setProperty(java.lang.String, java.lang.String)
        Properties myProps = new Properties();
        myProps.setProperty("Yoink", "Yabba");
        assertEquals("Failed to set property", "Yabba", myProps
                .getProperty("Yoink"));
        myProps.setProperty("Yoink", "Gab");
        assertEquals("Failed to reset property", "Gab", myProps
                .getProperty("Yoink"));
    }

    /**
     * @tests java.util.Properties#store(java.io.OutputStream, java.lang.String)
     */
    public void test_storeLjava_io_OutputStreamLjava_lang_String() {
        // Test for method void java.util.Properties.store(java.io.OutputStream,
        // java.lang.String)
        Properties myProps = new Properties();
        Properties myProps2 = new Properties();
        Enumeration e;
        String nextKey;

        myProps.put("Property A", " aye\\\f\t\n\r\b");
        myProps.put("Property B", "b ee#!=:");
        myProps.put("Property C", "see");

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            myProps.store(out, "A Header");
            out.close();
            ByteArrayInputStream in = new ByteArrayInputStream(out
                    .toByteArray());
            myProps2.load(in);
            in.close();
        } catch (IOException ioe) {
            fail("IOException occurred reading/writing file : "
                    + ioe.getMessage());
        }

        e = myProps.propertyNames();
        while (e.hasMoreElements()) {
            nextKey = (String) e.nextElement();
            assertTrue("Stored property list not equal to original", myProps2
                    .getProperty(nextKey).equals(myProps.getProperty(nextKey)));
        }

    }

    /**
     * @throws IOException
     * @tests java.util.Properties#store(java.io.Writer, java.lang.String)
     * @since 1.6
     */
    public void test_storeLjava_io_WriterLjava_lang_String() throws IOException {
        Properties myProps = new Properties();
        Properties myProps2 = new Properties();

        myProps.put("Property A", " aye\\\f\t\n\r\b");
        myProps.put("Property B", "b ee#!=:");
        myProps.put("Property C", "see");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        myProps.store(new OutputStreamWriter(out), "A Header");
        Scanner scanner = new Scanner(out.toString());
        assertTrue(scanner.nextLine().startsWith("#A Header"));
        assertTrue(scanner.nextLine().startsWith("#"));
        out.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        myProps2.load(in);
        in.close();

        Enumeration e = myProps.propertyNames();
        String nextKey;
        while (e.hasMoreElements()) {
            nextKey = (String) e.nextElement();
            assertTrue("Stored property list not equal to original", myProps2
                    .getProperty(nextKey).equals(myProps.getProperty(nextKey)));
        }

        try {
            myProps.store((Writer) null, "some comments");
            fail("Should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        myProps.put(String.class, "wrong type");
        try {
            myProps.store(new OutputStreamWriter(new ByteArrayOutputStream()),
                    "some comments");
            fail("Should throw ClassCastException");
        } catch (ClassCastException e1) {
            // expected
        }
        myProps.remove(String.class);
        myProps.store(new OutputStreamWriter(new ByteArrayOutputStream()),
                "some comments");
        // it is OK
        myProps.put("wrong type", String.class);
        try {
            myProps.store(new OutputStreamWriter(new ByteArrayOutputStream()),
                    "some comments");
            fail("Should throw ClassCastException");
        } catch (ClassCastException e1) {
            // expected
        }
    }

    /**
     * @tests java.util.Properties#loadFromXML(java.io.InputStream)
     */
    public void test_loadFromXMLLjava_io_InputStream() throws Exception {
        // Test for method void
        // java.util.Properties.loadFromXML(java.io.InputStream)
        Properties prop = null;
        try {
            InputStream is;
            prop = new Properties();
            prop.loadFromXML(is = new ByteArrayInputStream(
                    writePropertiesXMLUTF_8()));
            is.close();
        } catch (Exception e) {
            fail("Exception during load test : " + e.getMessage());
        }
        assertEquals("Failed to load correct properties", "value3", prop
                .getProperty("key3"));
        assertEquals("Failed to load correct properties", "value1", prop
                .getProperty("key1"));

        try {
            InputStream is;
            prop = new Properties();
            prop.loadFromXML(is = new ByteArrayInputStream(
                    writePropertiesXMLISO_8859_1()));
            is.close();
        } catch (Exception e) {
            fail("Exception during load test : " + e.getMessage());
        }
        assertEquals("Failed to load correct properties", "value2", prop
                .getProperty("key2"));
        assertEquals("Failed to load correct properties", "value1", prop
                .getProperty("key1"));
        
        try {
            prop.loadFromXML(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Properties#storeToXML(java.io.OutputStream,
     *        java.lang.String, java.lang.String)
     */
    public void test_storeToXMLLjava_io_OutputStreamLjava_lang_StringLjava_lang_String()
            throws Exception {
        // Test for method void
        // java.util.Properties.storeToXML(java.io.OutputStream,
        // java.lang.String, java.lang.String)
        Properties myProps = new Properties();
        Properties myProps2 = new Properties();
        Enumeration e;
        String nextKey;

        myProps.setProperty("key1", "value1");
        myProps.setProperty("key2", "value2");
        myProps.setProperty("key3", "value3");
        myProps.setProperty("<a>key4</a>", "\"value4");
        myProps.setProperty("key5   ", "<h>value5</h>");
        myProps.setProperty("<a>key6</a>", "   <h>value6</h>   ");
        myProps.setProperty("<comment>key7</comment>", "value7");
        myProps.setProperty("  key8   ", "<comment>value8</comment>");
        myProps.setProperty("&lt;key9&gt;", "\u0027value9");
        myProps.setProperty("key10\"", "&lt;value10&gt;");
        myProps.setProperty("&amp;key11&amp;", "value11");
        myProps.setProperty("key12", "&amp;value12&amp;");
        myProps.setProperty("<a>&amp;key13&lt;</a>",
                "&amp;&value13<b>&amp;</b>");

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // store in UTF-8 encoding
            myProps.storeToXML(out, "comment");
            out.close();
            ByteArrayInputStream in = new ByteArrayInputStream(out
                    .toByteArray());
            myProps2.loadFromXML(in);
            in.close();
        } catch (InvalidPropertiesFormatException ipfe) {
            fail("InvalidPropertiesFormatException occurred reading file: "
                    + ipfe.getMessage());
        } catch (IOException ioe) {
            fail("IOException occurred reading/writing file : "
                    + ioe.getMessage());
        }

        e = myProps.propertyNames();
        while (e.hasMoreElements()) {
            nextKey = (String) e.nextElement();
            assertTrue("Stored property list not equal to original", myProps2
                    .getProperty(nextKey).equals(myProps.getProperty(nextKey)));
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // store in ISO-8859-1 encoding
            myProps.storeToXML(out, "comment", "ISO-8859-1");
            out.close();
            ByteArrayInputStream in = new ByteArrayInputStream(out
                    .toByteArray());
            myProps2 = new Properties();
            myProps2.loadFromXML(in);
            in.close();
        } catch (InvalidPropertiesFormatException ipfe) {
            fail("InvalidPropertiesFormatException occurred reading file: "
                    + ipfe.getMessage());
        } catch (IOException ioe) {
            fail("IOException occurred reading/writing file : "
                    + ioe.getMessage());
        }

        e = myProps.propertyNames();
        while (e.hasMoreElements()) {
            nextKey = (String) e.nextElement();
            assertTrue("Stored property list not equal to original", myProps2
                    .getProperty(nextKey).equals(myProps.getProperty(nextKey)));
        }
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            myProps.storeToXML(out, null, null);
            fail("should throw nullPointerException");
        } catch (NullPointerException ne) {
            // expected
        }
    }
 
    /**
     * if loading from single line like "hello" without "\n\r" neither "=", it
     * should be same as loading from "hello="
     */
    public void testLoadSingleLine() throws Exception{
        Properties props = new Properties();
        InputStream sr = new ByteArrayInputStream("hello".getBytes());
        props.load(sr);
        assertEquals(1, props.size());
    }

    public void test_SequentialpropertyNames() {
        Properties parent = new Properties();
        parent.setProperty("parent.a.key", "parent.a.value");
        parent.setProperty("parent.b.key", "parent.b.value");

        Enumeration<?> names = parent.propertyNames();
        assertEquals("parent.a.key", names.nextElement());
        assertEquals("parent.b.key", names.nextElement());
        assertFalse(names.hasMoreElements());

        Properties current = new Properties(parent);
        current.setProperty("current.a.key", "current.a.value");
        current.setProperty("current.b.key", "current.b.value");

        names = current.propertyNames();
        assertEquals("parent.a.key", names.nextElement());
        assertEquals("current.b.key", names.nextElement());
        assertEquals("parent.b.key", names.nextElement());
        assertEquals("current.a.key", names.nextElement());
        assertFalse(names.hasMoreElements());

        Properties child = new Properties(current);
        child.setProperty("child.a.key", "child.a.value");
        child.setProperty("child.b.key", "child.b.value");

        names = child.propertyNames();
        assertEquals("parent.a.key", names.nextElement());
        assertEquals("child.b.key", names.nextElement());
        assertEquals("current.b.key", names.nextElement());
        assertEquals("parent.b.key", names.nextElement());
        assertEquals("child.a.key", names.nextElement());
        assertEquals("current.a.key", names.nextElement());
        assertFalse(names.hasMoreElements());
    }

    public void test_SequentialstringPropertyNames() {
        Properties parent = new Properties();
        parent.setProperty("parent.a.key", "parent.a.value");
        parent.setProperty("parent.b.key", "parent.b.value");

        Iterator<String> nameIterator = parent.stringPropertyNames().iterator();
        assertEquals("parent.a.key", nameIterator.next());
        assertEquals("parent.b.key", nameIterator.next());
        assertFalse(nameIterator.hasNext());

        Properties current = new Properties(parent);
        current.setProperty("current.a.key", "current.a.value");
        current.setProperty("current.b.key", "current.b.value");

        nameIterator = current.stringPropertyNames().iterator();
        assertEquals("parent.a.key", nameIterator.next());
        assertEquals("current.b.key", nameIterator.next());
        assertEquals("parent.b.key", nameIterator.next());
        assertEquals("current.a.key", nameIterator.next());
        assertFalse(nameIterator.hasNext());

        Properties child = new Properties(current);
        child.setProperty("child.a.key", "child.a.value");
        child.setProperty("child.b.key", "child.b.value");

        nameIterator = child.stringPropertyNames().iterator();
        assertEquals("parent.a.key", nameIterator.next());
        assertEquals("child.b.key", nameIterator.next());
        assertEquals("current.b.key", nameIterator.next());
        assertEquals("parent.b.key", nameIterator.next());
        assertEquals("child.a.key", nameIterator.next());
        assertEquals("current.a.key", nameIterator.next());
        assertFalse(nameIterator.hasNext());
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {

        tProps = new Properties();
        tProps.put("test.prop", "this is a test property");
        tProps.put("bogus.prop", "bogus");
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected byte[] writeProperties() throws IOException {
        PrintStream ps = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ps = new PrintStream(bout);
        ps.println("#commented.entry=Bogus");
        ps.println("test.pkg=harmony.tests");
        ps.println("test.proj=Automated Tests");
        ps.close();
        return bout.toByteArray();
    }

    protected byte[] writePropertiesXMLUTF_8() throws IOException {
        PrintStream ps = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ps = new PrintStream(bout, true, "UTF-8");
        ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps
                .println("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
        ps.println("<properties>");
        ps.println("<comment>comment</comment>");
        ps.println("<entry key=\"key4\">value4</entry>");
        ps.println("<entry key=\"key3\">value3</entry>");
        ps.println("<entry key=\"key2\">value2</entry>");
        ps.println("<entry key=\"key1\"><!-- xml comment -->value1</entry>");
        ps.println("</properties>");
        ps.close();
        return bout.toByteArray();
    }

    protected byte[] writePropertiesXMLISO_8859_1() throws IOException {
        PrintStream ps = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ps = new PrintStream(bout, true, "ISO-8859-1");
        ps.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        ps
                .println("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
        ps.println("<properties>");
        ps.println("<comment>comment</comment>");
        ps.println("<entry key=\"key4\">value4</entry>");
        ps.println("<entry key=\"key3\">value3</entry>");
        ps.println("<entry key=\"key2\">value2</entry>");
        ps.println("<entry key=\"key1\"><!-- xml comment -->value1</entry>");
        ps.println("</properties>");
        ps.close();
        return bout.toByteArray();
    }
}
