/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.security.tests.java.security;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

public class MessageDigest2Test extends junit.framework.TestCase {

    private static final String MESSAGEDIGEST_ID = "MessageDigest.";

    private String[] digestAlgs = null;

    private String providerName = null;

    private static final byte[] AR1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };

    private static final byte[] AR2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };

    private static final String MESSAGE = "abc";

    private static final byte[] MESSAGE_DIGEST = { -87, -103, 62, 54, 71, 6,
            -127, 106, -70, 62, 37, 113, 120, 80, -62, 108, -100, -48, -40,
            -99, };

    private static final byte[] MESSAGE_DIGEST_63_As = { 3, -16, -97, 91, 21,
            -118, 122, -116, -38, -39, 32, -67, -36, 41, -72, 28, 24, -91, 81,
            -11, };

    private static final byte[] MESSAGE_DIGEST_64_As = { 0, -104, -70, -126,
            75, 92, 22, 66, 123, -41, -95, 18, 42, 90, 68, 42, 37, -20, 100,
            77, };

    private static final byte[] MESSAGE_DIGEST_65_As = { 17, 101, 83, 38, -57,
            8, -41, 3, 25, -66, 38, 16, -24, -91, 125, -102, 91, -107, -99, 59, };

    /**
     * @tests java.security.MessageDigest#MessageDigest(java.lang.String)
     */
    public void test_constructor() {
        for (int i = 0; i < digestAlgs.length; i++) {
            MessageDigestStub md = new MessageDigestStub(digestAlgs[i]);
            assertEquals(digestAlgs[i], md.getAlgorithm());
            assertEquals(0, md.getDigestLength());
            assertNull(md.getProvider());
        }
    }
    
    /**
     * @tests java.security.MessageDigest#clone()
     */
    public void test_clone() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                MessageDigest d1 = MessageDigest.getInstance(digestAlgs[i],
                        providerName);
                for (byte b = 0; b < 84; b++) {
                    d1.update(b);
                }

                MessageDigest d2 = (MessageDigest) d1.clone();
                d1.update((byte) 1);
                d2.update((byte) 1);

                assertTrue("cloned hash differs from original for algorithm "
                        + digestAlgs[i], MessageDigest.isEqual(d1.digest(), d2
                        .digest()));
            } catch (CloneNotSupportedException e) {
                // Expected - a Signature may not be cloneable
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }

    private static final byte[] SHA_DATA_2 = { 70, -54, 124, 120, -29, 57, 56,
            119, -108, -54, -97, -76, -97, -50, -63, -73, 2, 85, -53, -79, };

    private void testSerializationSHA_DATA_2(MessageDigest sha) {
        try {
            sha.reset();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(out);
            // -----------------------------------------------------------------------

            // Made up data
            output
                    .writeUTF("tests.api.java.security.MessageDigestTest$InitializerFieldsTest3");
            output.writeInt(0); // class modifiers
            output.writeUTF("java.io.Serializable"); // interfaces

            // Fields
            output.writeUTF("sub_toBeNotSerialized"); // name
            output.writeInt(9); // modifiers
            output.writeUTF("Ljava/lang/String;"); // signature

            output.writeUTF("sub_toBeNotSerialized2"); // name
            output.writeInt(9); // modifiers
            output.writeUTF("Ljava/lang/String;"); // signature

            output.writeUTF("sub_toBeSerialized"); // name
            output.writeInt(1); // modifiers
            output.writeUTF("Ljava/lang/String;"); // signature

            output.writeUTF("sub_toBeSerialized3"); // name
            output.writeInt(1); // modifiers
            output.writeUTF("Ljava/lang/String;"); // signature

            output.writeUTF("sub_toBeSerialized4"); // name
            output.writeInt(1); // modifiers
            output.writeUTF("Ljava/lang/String;"); // signature

            output.writeUTF("sub_toBeSerialized5"); // name
            output.writeInt(1); // modifiers
            output.writeUTF("Ljava/lang/String;"); // signature

            // clinit
            output.writeUTF("<clinit>"); // name
            output.writeInt(8); // modifiers
            output.writeUTF("()V"); // signature

            // constructors
            output.writeUTF("<init>"); // name
            output.writeInt(0); // modifiers
            output.writeUTF("()V"); // signature

            // methods
            output.writeUTF("equals"); // name
            output.writeInt(1); // modifiers
            output.writeUTF("(Ljava.lang.Object;)Z"); // signature

            // -----------------------------------------------------------------------

            output.flush();

            byte[] data = out.toByteArray();
            byte[] hash = sha.digest(data);
            assertTrue("SHA_DATA_2 NOT ok", Arrays.equals(hash, SHA_DATA_2));
        } catch (IOException e) {
            fail("SHA_DATA_2 NOT ok");
        }
    }

    private static final byte[] SHA_DATA_1 = { 90, 36, 111, 106, -32, 38, 4,
            126, 21, -51, 107, 45, -64, -68, -109, 112, -31, -46, 34, 115, };

    private void testSerializationSHA_DATA_1(MessageDigest sha) {
        try {
            sha.reset();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(out);
            // -----------------------------------------------------------------------

            // Made up data
            output
                    .writeUTF("tests.api.java.security.MessageDigestTest$OptionalDataNotRead");
            // name
            output.writeInt(0); // class modifiers
            output.writeUTF("java.io.Serializable"); // interfaces

            // Fields
            output.writeUTF("class$0"); // name
            output.writeInt(8); // modifiers
            output.writeUTF("Ljava/lang/Class;"); // signature

            output.writeUTF("field1"); // name
            output.writeInt(2); // modifiers
            output.writeUTF("I"); // signature

            output.writeUTF("field2"); // name
            output.writeInt(2); // modifiers
            output.writeUTF("I"); // signature

            // clinit
            output.writeUTF("<clinit>"); // name
            output.writeInt(8); // modifiers
            output.writeUTF("()V"); // signature

            // constructors
            output.writeUTF("<init>"); // name
            output.writeInt(1); // modifiers
            output.writeUTF("()V"); // signature
            // -----------------------------------------------------------------------

            output.flush();
            byte[] data = out.toByteArray();
            byte[] hash = sha.digest(data);
            assertTrue("SHA_DATA_1 NOT ok", Arrays.equals(hash, SHA_DATA_1));
        } catch (IOException e) {
            fail("SHA_DATA_1 NOT ok");
        }
    }

    /**
     * @tests java.security.MessageDigest#digest()
     */
    public void test_digest() {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA");
            assertNotNull(sha);
        } catch (NoSuchAlgorithmException e) {
            fail("getInstance did not find algorithm");
        }
        sha.update(MESSAGE.getBytes());
        byte[] digest = sha.digest();
        assertTrue("bug in SHA", MessageDigest.isEqual(digest, MESSAGE_DIGEST));

        sha.reset();
        for (int i = 0; i < 63; i++) {
            // just under buffer capacity
            sha.update((byte) 'a');
        }
        digest = sha.digest();
        assertTrue("bug in SHA", MessageDigest.isEqual(digest,
                MESSAGE_DIGEST_63_As));

        sha.reset();
        for (int i = 0; i < 64; i++) {
            // exact SHA buffer capacity
            sha.update((byte) 'a');
        }
        digest = sha.digest();
        assertTrue("bug in SHA", MessageDigest.isEqual(digest,
                MESSAGE_DIGEST_64_As));

        sha.reset();
        for (int i = 0; i < 65; i++) {
            // just above SHA buffer capacity
            sha.update((byte) 'a');
        }
        digest = sha.digest();
        assertTrue("bug in SHA", MessageDigest.isEqual(digest,
                MESSAGE_DIGEST_65_As));

        testSerializationSHA_DATA_1(sha);
        testSerializationSHA_DATA_2(sha);
    }

    /**
     * @tests java.security.MessageDigest#digest(byte[])
     */
    public void test_digest$B() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                MessageDigest digest = MessageDigest.getInstance(digestAlgs[i],
                        providerName);
                assertNotNull(digest);
                digest.digest(AR1);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }

    /**
     * @tests java.security.MessageDigest#digest(byte[], int, int)
     */
    public void test_digest$BII() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                MessageDigest digest = MessageDigest.getInstance(digestAlgs[i],
                        providerName);
                assertNotNull(digest);
                int len = digest.getDigestLength();
                byte[] digestBytes = new byte[len];
                digest.digest(digestBytes, 0, digestBytes.length);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            } catch (DigestException e) {
                fail("digest caused exception for algorithm " + digestAlgs[i]
                        + " : " + e);
            }
        }
        try {
            MessageDigest.getInstance("SHA").digest(new byte[] {},
                    Integer.MAX_VALUE, 755);
        } catch (NoSuchAlgorithmException e) {
            // allowed
        } catch (DigestException e) {
            // allowed
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.security.MessageDigest#update(byte[], int, int)
     */
    public void test_update$BII() {
        try {
            MessageDigest.getInstance("SHA").update(new byte[] {},
                    Integer.MAX_VALUE, Integer.MAX_VALUE);
        } catch (NoSuchAlgorithmException e) {
            // allowed
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.security.MessageDigest#getAlgorithm()
     */
    public void test_getAlgorithm() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                String alg = MessageDigest.getInstance(digestAlgs[i],
                        providerName).getAlgorithm();
                assertTrue("getAlgorithm ok", alg.equals(digestAlgs[i]));
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }

    /**
     * @tests java.security.MessageDigest#getDigestLength()
     */
    public void test_getDigestLength() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                int len = MessageDigest
                        .getInstance(digestAlgs[i], providerName)
                        .getDigestLength();
                assertTrue("length not ok", len > 0);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }// end for
    }

    /**
     * @tests java.security.MessageDigest#getInstance(java.lang.String)
     */
    public void test_getInstanceLjava_lang_String() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                MessageDigest.getInstance(digestAlgs[i]);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            }
        }
    }

    /**
     * @tests java.security.MessageDigest#getInstance(java.lang.String,
     *        java.lang.String)
     */
    public void test_getInstanceLjava_lang_StringLjava_lang_String() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                MessageDigest.getInstance(digestAlgs[i], providerName);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }

    /**
     * @tests java.security.MessageDigest#getInstance(java.lang.String,
     *        java.security.Provider)
     */
    public void test_getInstanceLjava_lang_StringLjava_security_Provider() {
        Provider[] providers = Security.getProviders("MessageDigest.SHA");
        for (int i = 0; i < digestAlgs.length; i++) {
            for (int j = 0; j < providers.length; j++) {
                try {
                    MessageDigest.getInstance(digestAlgs[i], providers[j]);
                } catch (NoSuchAlgorithmException e) {
                    fail("getInstance did not find algorithm " + digestAlgs[i]);
                }
            }

        }
    }

    /**
     * @tests java.security.MessageDigest#getProvider()
     */
    public void test_getProvider() {
        for (int i = 0; i < digestAlgs.length; i++) {
            try {
                Provider p = MessageDigest.getInstance(digestAlgs[i],
                        providerName).getProvider();
                assertNotNull("provider is null", p);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + digestAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }

    /**
     * @tests java.security.MessageDigest#isEqual(byte[], byte[])
     */
    public void test_isEqual$B$B() {
        assertTrue("isEqual is not correct", MessageDigest.isEqual(AR1, AR2));
    }

    /**
     * @tests java.security.MessageDigest#toString()
     */
    public void test_toString() {
        try {
            String str = MessageDigest.getInstance("SHA").toString();
            assertNotNull("toString is null", str);
        } catch (NoSuchAlgorithmException e) {
            fail("getInstance did not find algorithm");
        }
    }

    protected void setUp() {
        if (digestAlgs == null) {
            Provider[] providers = Security.getProviders("MessageDigest.SHA");
            if (providers == null) {
                fail("No providers available for test");
            }

            // Arbitrarily select the first available provider
            providerName = providers[0].getName();
            digestAlgs = getDigestAlgorithms(providerName);
            if (digestAlgs == null || digestAlgs.length == 0) {
                fail("No digest algorithms were found");
            }
        }
    }

    /*
     * Returns the digest algorithms that the given provider supports.
     */
    private String[] getDigestAlgorithms(String providerName) {
        Vector<String> algs = new Vector<String>();

        Provider provider = Security.getProvider(providerName);
        if (provider == null)
            return new String[0];
        Enumeration e = provider.keys();
        while (e.hasMoreElements()) {
            String algorithm = (String) e.nextElement();
            if (algorithm.startsWith(MESSAGEDIGEST_ID)
                    && !algorithm.contains(" ")) {
                algs.addElement(algorithm.substring(MESSAGEDIGEST_ID.length()));
            }
        }// end while

        return (String[]) algs.toArray(new String[algs.size()]);
    }

    private class MessageDigestStub extends MessageDigest {
        public MessageDigestStub(String algorithm) {
            super(algorithm);
        }

        public byte[] engineDigest() {
            return null;
        }

        public void engineReset() {

        }

        public void engineUpdate(byte input) {

        }

        public void engineUpdate(byte[] input, int offset, int len) {

        }

    }
}