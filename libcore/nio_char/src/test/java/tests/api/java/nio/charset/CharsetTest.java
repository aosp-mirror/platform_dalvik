/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.api.java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.security.Permission;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedMap;
import java.util.Vector;

import junit.framework.TestCase;

/**
 * Test class java.nio.Charset.
 */
public class CharsetTest extends TestCase {

    static MockCharset charset1 = new MockCharset("mockCharset00",
            new String[] { "mockCharset01", "mockCharset02" });

    static MockCharset charset2 = new MockCharset("mockCharset10",
            new String[] { "mockCharset11", "mockCharset12" });

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test the required 6 charsets are supported.
     */
    public void testRequiredCharsetSupported() {
        assertTrue(Charset.isSupported("US-ASCII"));
        assertTrue(Charset.isSupported("ASCII"));
        assertTrue(Charset.isSupported("ISO-8859-1"));
        assertTrue(Charset.isSupported("ISO8859_1"));
        assertTrue(Charset.isSupported("UTF-8"));
        assertTrue(Charset.isSupported("UTF8"));
        assertTrue(Charset.isSupported("UTF-16"));
        assertTrue(Charset.isSupported("UTF-16BE"));
        assertTrue(Charset.isSupported("UTF-16LE"));

        Charset c1 = Charset.forName("US-ASCII");
        assertEquals("US-ASCII", Charset.forName("US-ASCII").name());
        assertEquals("US-ASCII", Charset.forName("ASCII").name());
        assertEquals("ISO-8859-1", Charset.forName("ISO-8859-1").name());
        assertEquals("ISO-8859-1", Charset.forName("ISO8859_1").name());
        assertEquals("UTF-8", Charset.forName("UTF-8").name());
        assertEquals("UTF-8", Charset.forName("UTF8").name());
        assertEquals("UTF-16", Charset.forName("UTF-16").name());
        assertEquals("UTF-16BE", Charset.forName("UTF-16BE").name());
        assertEquals("UTF-16LE", Charset.forName("UTF-16LE").name());

        assertNotSame(Charset.availableCharsets(), Charset.availableCharsets());
        // assertSame(Charset.forName("US-ASCII"), Charset.availableCharsets()
        // .get("US-ASCII"));
        // assertSame(Charset.forName("US-ASCII"), c1);
        assertTrue(Charset.availableCharsets().containsKey("US-ASCII"));
        assertTrue(Charset.availableCharsets().containsKey("ISO-8859-1"));
        assertTrue(Charset.availableCharsets().containsKey("UTF-8"));
        assertTrue(Charset.availableCharsets().containsKey("UTF-16"));
        assertTrue(Charset.availableCharsets().containsKey("UTF-16BE"));
        assertTrue(Charset.availableCharsets().containsKey("UTF-16LE"));
    }

    /*
     * Test the method isSupported(String) with null.
     */
    public void testIsSupported_Null() {
        try {
            Charset.isSupported(null);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /*
     * Test the method isSupported(String) with empty string.
     * 
     */
    public void testIsSupported_EmptyString() {
        try {
            Charset.isSupported("");
        } catch (IllegalArgumentException e) {
                        // FIXME: Commented out since RI does throw IAE
                        // fail("Should not throw IllegalArgumentException!");
        }
    }

    /*
     * Test the method isSupported(String) with a string starting with ".".
     * 
     */
    public void testIsSupported_InvalidInitialCharacter() {
        try {
            Charset.isSupported(".char");
        } catch (IllegalArgumentException e) {
            fail("Should not throw IllegalArgumentException!");
        }
    }

    /*
     * Test the method isSupported(String) with illegal charset name.
     */
    public void testIsSupported_IllegalName() {
        try {
            Charset.isSupported(" ///#$$");
            fail("Should throw IllegalCharsetNameException!");
        } catch (IllegalCharsetNameException e) {
            // expected
        }
    }

    /*
     * Test the method isSupported(String) with not supported charset name.
     */
    public void testIsSupported_NotSupported() {
        assertFalse(Charset.isSupported("impossible"));
    }

    /*
     * Test the method forName(String) with null.
     */
    public void testForName_Null() {
        try {
            Charset.forName(null);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /*
     * Test the method forName(String) with empty string.
     */
    public void testForName_EmptyString() {
        try {
            Charset.forName("");
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /*
     * Test the method forName(String) with a string starting with ".".
     */
    public void testForName_InvalidInitialCharacter() {
        try {
            Charset.forName(".char");
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /*
     * Test the method forName(String) with illegal charset name.
     */
    public void testForName_IllegalName() {
        try {
            Charset.forName(" ///#$$");
            fail("Should throw IllegalCharsetNameException!");
        } catch (IllegalCharsetNameException e) {
            // expected
        }
    }

    /*
     * Test the method forName(String) with not supported charset name.
     */
    public void testForName_NotSupported() {
        try {
            Charset.forName("impossible");
            fail("Should throw UnsupportedCharsetException!");
        } catch (UnsupportedCharsetException e) {
            // expected
        }
    }

    /*
     * Test the constructor with normal parameter values.
     */
    public void testConstructor_Normal() {
        final String mockName = "mockChar1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.:-_";
        MockCharset c = new MockCharset(mockName, new String[] { "mock" });
        assertEquals(mockName, c.name());
        assertEquals(mockName, c.displayName());
        assertEquals(mockName, c.displayName(Locale.getDefault()));
        assertEquals("mock", c.aliases().toArray()[0]);
        assertEquals(1, c.aliases().toArray().length);
    }

    /*
     * Test the constructor with empty canonical name.
     * 
     */
    public void testConstructor_EmptyCanonicalName() {
        try {
            new MockCharset("", new String[0]);
        } catch (IllegalCharsetNameException e) {
                        // FIXME: Commented out since RI does throw IAE
                        // fail("Should not throw IllegalArgumentException!");
        }
    }

    /*
     * Test the constructor with illegal canonical name: starting with neither a
     * digit nor a letter.
     * 
     */
    public void testConstructor_IllegalCanonicalName_Initial() {
        try {
            new MockCharset("-123", new String[] { "mock" });
        } catch (IllegalCharsetNameException e) {
            fail("Should not throw IllegalArgumentException!");
        }
    }

    /*
     * Test the constructor with illegal canonical name, illegal character in
     * the middle.
     */
    public void testConstructor_IllegalCanonicalName_Middle() {
        try {
            new MockCharset("1%%23", new String[] { "mock" });
            fail("Should throw IllegalCharsetNameException!");
        } catch (IllegalCharsetNameException e) {
            // expected
        }
        try {
            new MockCharset("1//23", new String[] { "mock" });
            fail("Should throw IllegalCharsetNameException!");
        } catch (IllegalCharsetNameException e) {
            // expected
        }
    }

    /*
     * Test the constructor with null canonical name.
     */
    public void testConstructor_NullCanonicalName() {
        try {
            MockCharset c = new MockCharset(null, new String[] { "mock" });
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Test the constructor with null aliases.
     */
    public void testConstructor_NullAliases() {
        MockCharset c = new MockCharset("mockChar", null);
        assertEquals("mockChar", c.name());
        assertEquals("mockChar", c.displayName());
        assertEquals("mockChar", c.displayName(Locale.getDefault()));
        assertEquals(0, c.aliases().toArray().length);
    }

    /*
     * Test the constructor with a null aliases.
     */
    public void testConstructor_NullAliase() {
        try {
            new MockCharset("mockChar", new String[] { "mock", null });
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Test the constructor with no aliases.
     */
    public void testConstructor_NoAliases() {
        MockCharset c = new MockCharset("mockChar", new String[0]);
        assertEquals("mockChar", c.name());
        assertEquals("mockChar", c.displayName());
        assertEquals("mockChar", c.displayName(Locale.getDefault()));
        assertEquals(0, c.aliases().toArray().length);
    }

    /*
     * Test the constructor with empty aliases.
     * 
     */
    public void testConstructor_EmptyAliases() {
        try {
            new MockCharset("mockChar", new String[] { "" });
        } catch (IllegalCharsetNameException e) {
                        // FIXME: Commented out since RI does throw IAE
            // fail("Should not throw IllegalArgumentException!");
        }
    }

    /*
     * Test the constructor with illegal aliases: starting with neither a digit
     * nor a letter.
     * 
     */
    public void testConstructor_IllegalAliases_Initial() {
        try {
            new MockCharset("mockChar", new String[] { "mock", "-123" });
        } catch (IllegalCharsetNameException e) {
            fail("Should not throw IllegalArgumentException!");
        }
    }

    /*
     * Test the constructor with illegal aliase, illegal character in the
     * middle.
     */
    public void testConstructor_IllegalAliases_Middle() {
        try {
            new MockCharset("mockChar", new String[] { "mock", "22##ab" });
            fail("Should throw IllegalCharsetNameException!");
        } catch (IllegalCharsetNameException e) {
            // expected
        }
        try {
            new MockCharset("mockChar", new String[] { "mock", "22%%ab" });
            fail("Should throw IllegalCharsetNameException!");
        } catch (IllegalCharsetNameException e) {
            // expected
        }
    }

    /*
     * Test the method aliases() with multiple aliases. Most conditions have
     * been tested in the testcases for the constructors.
     */
    public void testAliases_Multiple() {
        final String mockName = "mockChar1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.:-_";
        MockCharset c = new MockCharset("mockChar", new String[] { "mock",
                mockName, "mock2" });
        assertEquals("mockChar", c.name());
        assertEquals(3, c.aliases().size());
        assertTrue(c.aliases().contains("mock"));
        assertTrue(c.aliases().contains(mockName));
        assertTrue(c.aliases().contains("mock2"));

        try {
            c.aliases().clear();
            fail("Should throw UnsupportedOperationException!");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /*
     * Test the method aliases() with duplicate aliases, one same with its
     * canonical name.
     */
    public void testAliases_Duplicate() {
        final String mockName = "mockChar1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.:-_";
        MockCharset c = new MockCharset("mockChar", new String[] { "mockChar",
                "mock", mockName, "mock", "mockChar", "mock", "mock2" });
        assertEquals("mockChar", c.name());
        assertEquals(4, c.aliases().size());
        assertTrue(c.aliases().contains("mockChar"));
        assertTrue(c.aliases().contains("mock"));
        assertTrue(c.aliases().contains(mockName));
        assertTrue(c.aliases().contains("mock2"));
    }

    /*
     * Test the method canEncode(). Test the default return value.
     */
    public void testCanEncode() {
        MockCharset c = new MockCharset("mock", null);
        assertTrue(c.canEncode());
    }

    /*
     * Test the method isRegistered(). Test the default return value.
     */
    public void testIsRegistered() {
        MockCharset c = new MockCharset("mock", null);
        assertTrue(c.isRegistered());
    }

    /*
     * The name() method has been tested by the testcases for the constructor.
     */
    public void testName() {
        // already covered by testConstructor_XXX series
    }

    /*
     * The displayName() method have been tested by the testcases for the
     * constructor.
     */
    public void testDisplayName() {
        // already covered by testConstructor_XXX series
    }

    /*
     * Test displayName(Locale) with null.
     */
    public void testDisplayName_Locale_Null() {
        MockCharset c = new MockCharset("mock", null);
        assertEquals("mock", c.displayName(null));
    }

    /*
     * Test the method compareTo(Object) with normal conditions.
     */
    public void testCompareTo_Normal() {
        MockCharset c1 = new MockCharset("mock", null);
        assertEquals(0, c1.compareTo(c1));

        MockCharset c2 = new MockCharset("Mock", null);
        assertEquals(0, c1.compareTo(c2));

        c2 = new MockCharset("mock2", null);
        assertTrue(c1.compareTo(c2) < 0);
        assertTrue(c2.compareTo(c1) > 0);

        c2 = new MockCharset("mack", null);
        assertTrue(c1.compareTo(c2) > 0);
        assertTrue(c2.compareTo(c1) < 0);

        c2 = new MockCharset("m.", null);
        assertTrue(c1.compareTo(c2) > 0);
        assertTrue(c2.compareTo(c1) < 0);

        c2 = new MockCharset("m:", null);
        assertEquals("mock".compareToIgnoreCase("m:"), c1.compareTo(c2));
        assertEquals("m:".compareToIgnoreCase("mock"), c2.compareTo(c1));

        c2 = new MockCharset("m-", null);
        assertTrue(c1.compareTo(c2) > 0);
        assertTrue(c2.compareTo(c1) < 0);

        c2 = new MockCharset("m_", null);
        assertTrue(c1.compareTo(c2) > 0);
        assertTrue(c2.compareTo(c1) < 0);
    }

    /*
     * Test the method compareTo(Object) with null param.
     */
    public void testCompareTo_Null() {
        MockCharset c1 = new MockCharset("mock", null);
        try {
            c1.compareTo(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Test the method compareTo(Object) with another kind of charset object.
     */
    public void testCompareTo_DiffCharsetClass() {
        MockCharset c1 = new MockCharset("mock", null);
        MockCharset2 c2 = new MockCharset2("Mock", new String[] { "myname" });
        assertEquals(0, c1.compareTo(c2));
        assertEquals(0, c2.compareTo(c1));
    }

    /*
     * Test the method equals(Object) with null param.
     */
    public void testEquals_Normal() {
        MockCharset c1 = new MockCharset("mock", null);
        MockCharset2 c2 = new MockCharset2("mock", null);
        assertTrue(c1.equals(c2));
        assertTrue(c2.equals(c1));

        c2 = new MockCharset2("Mock", null);
        assertFalse(c1.equals(c2));
        assertFalse(c2.equals(c1));
    }

    /*
     * Test the method equals(Object) with normal conditions.
     */
    public void testEquals_Null() {
        MockCharset c1 = new MockCharset("mock", null);
        assertFalse(c1.equals(null));
    }

    /*
     * Test the method equals(Object) with another kind of charset object.
     */
    public void testEquals_NonCharsetObject() {
        MockCharset c1 = new MockCharset("mock", null);
        assertFalse(c1.equals("test"));
    }

    /*
     * Test the method equals(Object) with another kind of charset object.
     */
    public void testEquals_DiffCharsetClass() {
        MockCharset c1 = new MockCharset("mock", null);
        MockCharset2 c2 = new MockCharset2("mock", null);
        assertTrue(c1.equals(c2));
        assertTrue(c2.equals(c1));
    }

    /*
     * Test the method hashCode().
     */
    public void testHashCode_DiffCharsetClass() {
        MockCharset c1 = new MockCharset("mock", null);
        assertEquals(c1.hashCode(), "mock".hashCode());

        final String mockName = "mockChar1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.:-_";
        c1 = new MockCharset(mockName, new String[] { "mockChar", "mock",
                mockName, "mock", "mockChar", "mock", "mock2" });
        assertEquals(mockName.hashCode(), c1.hashCode());
    }

    /*
     * Test the method encode(CharBuffer) under normal condition.
     */
    public void testEncode_CharBuffer_Normal() throws Exception {
        MockCharset c1 = new MockCharset("testEncode_CharBuffer_Normal_mock", null);
        ByteBuffer bb = c1.encode(CharBuffer.wrap("abcdefg"));
        assertEquals("abcdefg", new String(bb.array(), "iso8859-1"));
        bb = c1.encode(CharBuffer.wrap(""));
        assertEquals("", new String(bb.array(), "iso8859-1"));
    }

    /*
     * Test the method encode(CharBuffer) with an unmappable char.
     */
    public void testEncode_CharBuffer_Unmappable() throws Exception {
        Charset c1 = Charset.forName("iso8859-1");
        ByteBuffer bb = c1.encode(CharBuffer.wrap("abcd\u5D14efg"));
        assertEquals(new String(bb.array(), "iso8859-1"), "abcd"
                + new String(c1.newEncoder().replacement(), "iso8859-1")
                + "efg");
    }

    /*
     * Test the method encode(CharBuffer) with null CharBuffer.
     */
    public void testEncode_CharBuffer_NullCharBuffer() {
        MockCharset c = new MockCharset("mock", null);
        try {
            c.encode((CharBuffer) null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Test the method encode(CharBuffer) with null encoder.
     */
    public void testEncode_CharBuffer_NullEncoder() {
        MockCharset2 c = new MockCharset2("mock2", null);
        try {
            c.encode(CharBuffer.wrap("hehe"));
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Test the method encode(String) under normal condition.
     */
    public void testEncode_String_Normal() throws Exception {
        MockCharset c1 = new MockCharset("testEncode_String_Normal_mock", null);
        ByteBuffer bb = c1.encode("abcdefg");
        assertEquals("abcdefg", new String(bb.array(), "iso8859-1"));
        bb = c1.encode("");
        assertEquals("", new String(bb.array(), "iso8859-1"));
    }

    /*
     * Test the method encode(String) with an unmappable char.
     */
    public void testEncode_String_Unmappable() throws Exception {
        Charset c1 = Charset.forName("iso8859-1");
        ByteBuffer bb = c1.encode("abcd\u5D14efg");
        assertEquals(new String(bb.array(), "iso8859-1"), "abcd"
                + new String(c1.newEncoder().replacement(), "iso8859-1")
                + "efg");
    }

    /*
     * Test the method encode(String) with null CharBuffer.
     */
    public void testEncode_String_NullString() {
        MockCharset c = new MockCharset("mock", null);
        try {
            c.encode((String) null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Test the method encode(String) with null encoder.
     */
    public void testEncode_String_NullEncoder() {

        MockCharset2 c = new MockCharset2("mock2", null);
        try {
            c.encode("hehe");
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Test the method decode(ByteBuffer) under normal condition.
     */
    public void testDecode_Normal() throws Exception {
        MockCharset c1 = new MockCharset("mock", null);
        CharBuffer cb = c1.decode(ByteBuffer.wrap("abcdefg"
                .getBytes("iso8859-1")));
        assertEquals("abcdefg", new String(cb.array()));
        cb = c1.decode(ByteBuffer.wrap("".getBytes("iso8859-1")));
        assertEquals("", new String(cb.array()));
    }

    /*
     * Test the method decode(ByteBuffer) with a malformed input.
     */
    public void testDecode_Malformed() throws Exception {
        Charset c1 = Charset.forName("iso8859-1");
        CharBuffer cb = c1.decode(ByteBuffer.wrap("abcd\u5D14efg"
                .getBytes("iso8859-1")));
        byte[] replacement = c1.newEncoder().replacement();
        assertEquals(new String(cb.array()), "abcd" + new String(replacement)
                + "efg");
    }

    /*
     * Test the method decode(ByteBuffer) with null CharBuffer.
     */
    public void testDecode_NullByteBuffer() {
        MockCharset c = new MockCharset("mock", null);
        try {
            c.decode(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Test the method decode(ByteBuffer) with null encoder.
     */
    public void testDecode_NullDecoder() {
        MockCharset2 c = new MockCharset2("mock2", null);
        try {
            c.decode(ByteBuffer.wrap("hehe".getBytes()));
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Test the method toString().
     */
    public void testToString() {
        MockCharset c1 = new MockCharset("mock", null);
        assertTrue(-1 != c1.toString().indexOf("mock"));
    }

    /**
     * @tests java.nio.charset.Charset#availableCharsets()
     */
    public void test_availableCharsets() throws Exception {
        // regression test for Harmony-1051
        ClassLoader originalClassLoader = Thread.currentThread()
                .getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);
            SortedMap<String, Charset> charsets = Charset.availableCharsets();
            // make sure "mockCharset00" is loaded by MockCharsetProvider
            assertTrue(charsets.containsKey("mockCharset00"));
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    /**
     * @tests java.nio.charset.Charset#availableCharsets()
     */
    public void test_forNameLString() throws Exception {
        // regression test for Harmony-1051
        ClassLoader originalClassLoader = Thread.currentThread()
                .getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);
            // make sure "mockCharset00" is loaded by MockCharsetProvider
            assertNotNull(Charset.forName("mockCharset00"));
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    /*
     * Mock charset class.
     */
    static final class MockCharset extends Charset {

        public MockCharset(String canonicalName, String[] aliases) {
            super(canonicalName, aliases);
        }

        public boolean contains(Charset cs) {
            return false;
        }

        public CharsetDecoder newDecoder() {
            return new MockDecoder(this);
        }

        public CharsetEncoder newEncoder() {
            return new MockEncoder(this);
        }
    }

    /*
     * Another mock charset class.
     */
    static class MockCharset2 extends Charset {

        public MockCharset2(String canonicalName, String[] aliases) {
            super(canonicalName, aliases);
        }

        public boolean contains(Charset cs) {
            return false;
        }

        public CharsetDecoder newDecoder() {
            return null;
        }

        public CharsetEncoder newEncoder() {
            return null;
        }
    }

    /*
     * Mock encoder.
     */
    static class MockEncoder extends java.nio.charset.CharsetEncoder {

        public MockEncoder(Charset cs) {
            super(cs, 1, 3, new byte[] { (byte) '?' });
        }

        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            while (in.remaining() > 0) {
                out.put((byte) in.get());
                // out.put((byte) '!');
            }
            return CoderResult.UNDERFLOW;
        }
    }

    /*
     * Mock decoder.
     */
    static class MockDecoder extends java.nio.charset.CharsetDecoder {

        public MockDecoder(Charset cs) {
            super(cs, 1, 10);
        }

        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            while (in.remaining() > 0) {
                out.put((char) in.get());
            }
            return CoderResult.UNDERFLOW;
        }
    }

    /*
     * Mock charset provider.
     */
    public static class MockCharsetProvider extends CharsetProvider {

        public Charset charsetForName(String charsetName) {
            if ("MockCharset00".equalsIgnoreCase(charsetName)
                    || "MockCharset01".equalsIgnoreCase(charsetName)
                    || "MockCharset02".equalsIgnoreCase(charsetName)) {
                return new MockCharset("mockCharset00", new String[] {
                        "mockCharset01", "mockCharset02" });
            }
            return null;
        }

        public Iterator charsets() {
            Vector v = new Vector();
            v.add(new MockCharset("mockCharset00", new String[] {
                    "mockCharset01", "mockCharset02" }));
            return v.iterator();
        }
    }
    
    /*
     * Used to grant all permissions except charset provider access.
     */
    public static class MockSecurityManager extends SecurityManager {

        public MockSecurityManager() {
        }

        public void checkPermission(Permission perm) {
            // grant all permissions except logging control
            if (perm instanceof RuntimePermission) {
                RuntimePermission rp = (RuntimePermission) perm;
                if (rp.getName().equals("charsetProvider")) {
                    throw new SecurityException();
                }
            }
        }

        public void checkPermission(Permission perm, Object context) {
            // grant all permissions except logging control
            if (perm instanceof RuntimePermission) {
                RuntimePermission rp = (RuntimePermission) perm;
                if (rp.getName().equals("charsetProvider")) {
                    throw new SecurityException();
                }
            }
        }
    }
}
