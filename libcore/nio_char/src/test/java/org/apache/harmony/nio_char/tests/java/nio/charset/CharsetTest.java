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

package org.apache.harmony.nio_char.tests.java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.IllegalCharsetNameException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

public class CharsetTest extends TestCase {

    // Will contain names of charsets registered with IANA
    Set knownRegisteredCharsets = new HashSet();

    // Will contain names of charsets not known to be registered with IANA
    Set unknownRegisteredCharsets = new HashSet();

    /**
     * JUnit set-up method
     */
    public void setUp() {
        // Populate the known charset vars
        Set names = Charset.availableCharsets().keySet();
        for (Iterator nameItr = names.iterator(); nameItr.hasNext();) {
            String name = (String) nameItr.next();
            if (name.toLowerCase().startsWith("x-"))
                unknownRegisteredCharsets.add(name);
            else
                knownRegisteredCharsets.add(name);
        }
    }

    /**
     * @tests java.nio.charset.Charset#isRegistered()
     */
    public void test_isRegistered() {
        // Regression for HARMONY-45
        for (Iterator nameItr = knownRegisteredCharsets.iterator(); nameItr.hasNext();) {
            String name = (String) nameItr.next();
            assertTrue("Assert 0: isRegistered() failed for " + name,
                    Charset.forName(name).isRegistered());
        }
        for (Iterator nameItr = unknownRegisteredCharsets.iterator(); nameItr.hasNext();) {
            String name = (String) nameItr.next();
            assertFalse("Assert 0: isRegistered() failed for " + name,
                    Charset.forName(name).isRegistered());
        }
    }
    
    /**
     * @tests java.nio.charset.Charset#isSupported(String)
     */
    public void testIsSupported_EmptyString() {
        // Regression for HARMONY-113
        try {
            Charset.isSupported("");
            fail("Assert 0: Should throw IllegalCharsetNameException");
        } catch (IllegalCharsetNameException e) {
            // Expected
        }
    }
    
    /**
     * @tests java.nio.charset.Charset#defaultCharset()
     */
    public void test_defaultCharset() {
        String charsetName = null;
        String defaultCharsetName = null;
        String oldDefaultEncoding = System.getProperty("file.encoding");
        try {
            // Normal behavior
            charsetName = "UTF-8"; //$NON-NLS-1$
            System.setProperty("file.encoding", charsetName);//$NON-NLS-1$
            defaultCharsetName = Charset.defaultCharset().name();
            assertEquals(charsetName, defaultCharsetName);

            charsetName = "ISO-8859-1"; //$NON-NLS-1$
            System.setProperty("file.encoding", charsetName);//$NON-NLS-1$
            defaultCharsetName = Charset.defaultCharset().name();
            assertEquals(charsetName, defaultCharsetName);

            // Unsupported behavior
            charsetName = "IMPOSSIBLE-8"; //$NON-NLS-1$
            System.setProperty("file.encoding", charsetName);//$NON-NLS-1$
            defaultCharsetName = Charset.defaultCharset().name();
            assertEquals("UTF-8", defaultCharsetName);

            // Null behavior
            try {
                Properties currentProps = System.getProperties();
                currentProps.remove("file.encoding");//$NON-NLS-1$
                Charset.defaultCharset().name();
                fail("Should throw illegal IllegalArgumentException");//$NON-NLS-1$
            } catch (IllegalArgumentException e) {
                // expected
            }

            // IllegalCharsetName behavior
            try {
                charsetName = "IMP~~OSSIBLE-8"; //$NON-NLS-1$
                System.setProperty("file.encoding", charsetName);//$NON-NLS-1$
                Charset.defaultCharset().name();
                fail("Should throw IllegalCharsetNameException");//$NON-NLS-1$
            } catch (IllegalCharsetNameException e) {
                // expected
            }
        } finally {
            System.setProperty("file.encoding", oldDefaultEncoding);//$NON-NLS-1$
        }
    }
    
    /**
     * @tests java.nio.charset.Charset#forName(java.lang.String)
     */
    public void test_forNameLjava_lang_String() {
        /*
         * invoke forName two times with the same canonical name, it
         * should return the same reference.
         */
        Charset cs1 = Charset.forName("UTF-8");
        Charset cs2 = Charset.forName("UTF-8");
        assertSame(cs1, cs2);

        /*
         * test forName: invoke forName two times for the same Charset using
         * canonical name and alias, it should return the same reference.
         */
        Charset cs3 = Charset.forName("ASCII");
        Charset cs4 = Charset.forName("US-ASCII");
        assertSame(cs3, cs4);
    }
    
    /*
     * test cached decoder
     */
    public void test_DecodeLjava_nio_ByteBuffer() throws Exception{
            MockCharsetForDecoder cs1 = new MockCharsetForDecoder("CachedCharset",null);
            MockCharsetForDecoder cs2 = new MockCharsetForDecoder("CachedCharset",null);
            ByteBuffer in = ByteBuffer.wrap(new byte[]{0x00});
            cs1.decode(in);
            in.flip();
            cs2.decode(in);
            in.flip();
    }
    /*
     * Mock Charset for cached decoder test
     */
    static class MockCharsetForDecoder extends Charset{

            public MockCharsetForDecoder(String canonicalName, String[] aliases){
                    super(canonicalName, aliases);
            }

            public boolean contains(Charset charset) {
                    return false;
            }

            public CharsetEncoder newEncoder() {
                    return null;
            }

            public CharsetDecoder newDecoder() {
                    return new MockCachedDecoder(this);
            }


    }
    /*
     * Mock decoder. Only one caller is permitted.
     */
    static class MockCachedDecoder extends CharsetDecoder {
            static MockCachedDecoder caller = null;

            public MockCachedDecoder(Charset cs) {
                    super(cs, 1, 10);
            }

            /*
             * Only one caller is permitted.
             * If there's another caller, throw RuntimeException.
             */
            protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
                    if(null == caller){
                            caller = this;
                    }else{
                            if(caller != this){
                                // Another instance
                                fail("should use the same instance");
                            }
                    }
                    return CoderResult.UNDERFLOW;
            }
    }

    /*
     * test cached encoder
     */
    public void test_EncodeLjava_nio_CharBuffer() throws Exception {
            MockCharsetForEncoder cs1 = new MockCharsetForEncoder("CachedCharset", null);
            MockCharsetForEncoder cs2 = new MockCharsetForEncoder("CachedCharset", null);
            CharBuffer in = CharBuffer.wrap("A");
            cs1.encode(in);
            in.flip();
            cs2.encode(in);
    }

    /*
     * Mock Charset for cached encoder test
     */
    static class MockCharsetForEncoder extends Charset {

            public MockCharsetForEncoder(String canonicalName, String[] aliases) {
                    super(canonicalName, aliases);
            }

            public boolean contains(Charset charset) {
                    return false;
            }

            public CharsetDecoder newDecoder() {
                    return new MockDecoderForEncoder(this);
            }

            public CharsetEncoder newEncoder() {
                    return new MockCachedEncoder(this);
            }
    }

    /*
     * Mock encoder. Only one caller is permitted.
     */
    static class MockCachedEncoder extends CharsetEncoder {
            static MockCachedEncoder caller = null;

            public MockCachedEncoder(Charset cs) {
                    super(cs, 1, 10);
            }

            /*
             * Only one caller is permitted.
             */
            protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
                    if (null == caller) {
                            caller = this;
                    } else {
                            if (caller != this) {
                                    // Another instance
                                    fail("should use the same instance");
                            }
                    }
                    return CoderResult.UNDERFLOW;
            }
    }

    /*
     * Mock decoder for MockCachedEncoder.
     */
    static class MockDecoderForEncoder extends CharsetDecoder {
            public MockDecoderForEncoder(Charset cs) {
                    super(cs, 1, 10);
            }

            protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
                    in.position(in.limit());
                    return CoderResult.UNDERFLOW;
            }
    }

}
