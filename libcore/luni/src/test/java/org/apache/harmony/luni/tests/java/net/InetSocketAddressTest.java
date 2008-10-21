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
package org.apache.harmony.luni.tests.java.net;

import java.io.Serializable;
import java.net.InetSocketAddress;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class InetSocketAddressTest extends TestCase {

    /**
     * @tests java.net.InetSocketAddress#InetSocketAddress(String, int)
     */
    public void test_ConstructorLjava_lang_StringI() throws Exception {
        // regression test for Harmony-1042
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 0);
        assertNotNull(address.getHostName());
    }
    
    /**
     * @tests java.net.InetSocketAddress#createUnresolved(String, int)
     */
    public void test_createUnresolvedLjava_lang_StringI() {
        HostPortPair[] legalHostPortPairs = { new HostPortPair("127.0.0.1", 1234),
                new HostPortPair("192.168.0.1", 10000), new HostPortPair("127.0.0", 0),
                new HostPortPair("127.0.0", 65535),
                new HostPortPair("strange host", 65535) };
        for (int i = 0; i < legalHostPortPairs.length; i++) {
            InetSocketAddress isa = InetSocketAddress.createUnresolved(
                    legalHostPortPairs[i].host, legalHostPortPairs[i].port);
            assertTrue(isa.isUnresolved());
            assertNull(isa.getAddress());
            assertEquals(isa.getHostName(), legalHostPortPairs[i].host);
            assertEquals(isa.getPort(), legalHostPortPairs[i].port);
        }
    }

    /**
     * @tests java.net.InetSocketAddress#createUnresolved(String, int)
     */
    public void test_createUnresolvedLjava_lang_StringI_IllegalArgumentException() {
        HostPortPair[] illegalHostPortPairs = { new HostPortPair(null, 1),
                new HostPortPair("host", -1), new HostPortPair("host", 65536) };
        for (int i = 0; i < illegalHostPortPairs.length; i++) {
            try {
                InetSocketAddress.createUnresolved(
                        illegalHostPortPairs[i].host,
                        illegalHostPortPairs[i].port);
                fail("should throw IllegalArgumentException, host = "
                        + illegalHostPortPairs[i].host + ",port = "
                        + illegalHostPortPairs[i].port);
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    /*
     * inner class for createUnresolved test convenience.
     */
    class HostPortPair {
        String host;

        int port;

        public HostPortPair(String host, int port) {
            this.host = host;
            this.port = port;
        }
    };

    // comparator for InetSocketAddress objects
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            InetSocketAddress init = (InetSocketAddress) initial;
            InetSocketAddress desr = (InetSocketAddress) deserialized;

            assertEquals("HostName", init.getHostName(), desr.getHostName());
            assertEquals("Port", init.getPort(), desr.getPort());
            assertEquals("Address", init.getAddress(), desr.getAddress());
        }
    };

    /**
     * @tests serialization/deserialization compatibility.
     */
    public void testSerializationSelf() throws Exception {

        Object[] testCases = {
                InetSocketAddress.createUnresolved("badhost", 1000), // unresolved
                new InetSocketAddress("Localhost", 1000) };

        SerializationTest.verifySelf(testCases, COMPARATOR);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        Object[] testCases = {
                InetSocketAddress.createUnresolved("badhost", 1000), // unresolved
                new InetSocketAddress("Localhost", 1000) };

        SerializationTest.verifyGolden(this, testCases, COMPARATOR);
    }
}
