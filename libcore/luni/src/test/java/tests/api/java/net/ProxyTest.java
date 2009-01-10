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
package tests.api.java.net;

import dalvik.annotation.TestTargetClass; 
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

import junit.framework.TestCase;

@TestTargetClass(Proxy.class) 
public class ProxyTest extends TestCase {

    private SocketAddress address = new InetSocketAddress("127.0.0.1", 1234);

    /**
     * @tests java.net.Proxy#Proxy(java.net.Proxy.Type, SocketAddress)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for Proxy constructor.",
        method = "Proxy",
        args = {java.net.Proxy.Type.class, java.net.SocketAddress.class}
    )
    public void test_ConstructorLjava_net_ProxyLjava_net_SocketAddress_Normal() {
        // test HTTP type proxy
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        assertEquals(Proxy.Type.HTTP, proxy.type());
        assertEquals(address, proxy.address());

        // test SOCKS type proxy
        proxy = new Proxy(Proxy.Type.SOCKS, address);
        assertEquals(Proxy.Type.SOCKS, proxy.type());
        assertEquals(address, proxy.address());

        // test DIRECT type proxy
        proxy = Proxy.NO_PROXY;
        assertEquals(Proxy.Type.DIRECT, proxy.type());
        assertNull(proxy.address());
    }

    /**
     * @tests java.net.Proxy#Proxy(java.net.Proxy.Type, SocketAddress)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for Proxy constructor.",
        method = "Proxy",
        args = {java.net.Proxy.Type.class, java.net.SocketAddress.class}
    )
    public void test_ConstructorLjava_net_ProxyLjava_net_SocketAddress_IllegalAddress() {
        Proxy proxy = null;
        // test HTTP type proxy
        try {
            proxy = new Proxy(Proxy.Type.HTTP, null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // test SOCKS type proxy
        try {
            proxy = new Proxy(Proxy.Type.SOCKS, null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // test DIRECT type proxy
        try {
            proxy = new Proxy(Proxy.Type.DIRECT, null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // test DIRECT type proxy, any address is illegal
        try {
            proxy = new Proxy(Proxy.Type.DIRECT, address);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

    }

    /**
     * @tests java.net.Proxy#hashCode()
     * @see test_equalsLjava_lang_Object_Equals
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        SocketAddress address1 = new InetSocketAddress("127.0.0.1", 1234);
        
        Proxy proxy1 = new Proxy(Proxy.Type.HTTP, address1);
        Proxy proxy2 = new Proxy(Proxy.Type.HTTP, address1);
        assertTrue(proxy1.hashCode() == proxy2.hashCode());

        Proxy proxy3 = new Proxy(Proxy.Type.SOCKS, address1);
        Proxy proxy4 = new Proxy(Proxy.Type.SOCKS, address1);
        assertTrue(proxy1.hashCode() == proxy2.hashCode());
        
        assertTrue(proxy1.hashCode() != proxy4.hashCode());
        
        SocketAddress address2 = new InetSocketAddress("127.0.0.1", 1235);
        
        Proxy proxy5 = new Proxy(Proxy.Type.SOCKS, address1);
        Proxy proxy6 = new Proxy(Proxy.Type.SOCKS, address2);
        assertTrue(proxy5.hashCode() != proxy6.hashCode());
    }

    /**
     * @tests java.net.Proxy#type()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "type",
        args = {}
    )
    public void test_type() {
        
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        assertEquals(Proxy.Type.HTTP, proxy.type());

        proxy = new Proxy(Proxy.Type.SOCKS, address);
        assertEquals(Proxy.Type.SOCKS, proxy.type());

        proxy = Proxy.NO_PROXY;
        assertEquals(Proxy.Type.DIRECT, proxy.type());
    }

    /**
     * @tests java.net.Proxy#address() This method has been tested in
     *        Constructor test case.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "address",
        args = {}
    )
    public void test_address() {
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, address);
        assertEquals(address, proxy.address());
        
        try {
            new Proxy(Proxy.Type.SOCKS, null);
            fail("IllegalArgumentException was thrown.");
        } catch(IllegalArgumentException iae) {
            //expected
        }
    }

    /**
     * @tests java.net.Proxy#toString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        // include type String
        assertTrue(proxy.toString().indexOf(proxy.type().toString()) != -1);
        // include address String
        assertTrue(proxy.toString().indexOf(proxy.address().toString()) != -1);

        proxy = new Proxy(Proxy.Type.SOCKS, address);
        // include type String
        assertTrue(proxy.toString().indexOf(proxy.type().toString()) != -1);
        // include address String
        assertTrue(proxy.toString().indexOf(proxy.address().toString()) != -1);

        proxy = Proxy.NO_PROXY;
        // include type String
        assertTrue(proxy.toString().indexOf(proxy.type().toString()) != -1);

        proxy = new Proxy(null, address);
        // ensure no NPE is thrown
        proxy.toString();

    }

    /**
     * @tests java.net.Proxy#equals(Object)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object_Equals() {
        SocketAddress address1 = new InetSocketAddress("127.0.0.1", 1234);
        SocketAddress address2 = new InetSocketAddress("127.0.0.1", 1234);
        // HTTP type
        Proxy proxy1 = new Proxy(Proxy.Type.HTTP, address1);
        Proxy proxy2 = new Proxy(Proxy.Type.HTTP, address2);
        assertTrue(proxy1.equals(proxy2));
        // assert hashCode
        assertTrue(proxy1.hashCode() == proxy2.hashCode());

        // SOCKS type
        Proxy proxy3 = new Proxy(Proxy.Type.SOCKS, address1);
        Proxy proxy4 = new Proxy(Proxy.Type.SOCKS, address2);
        assertTrue(proxy3.equals(proxy4));
        // assert hashCode
        assertTrue(proxy3.hashCode() == proxy4.hashCode());

        // null type
        Proxy proxy5 = new Proxy(null, address1);
        Proxy proxy6 = new Proxy(null, address2);
        assertTrue(proxy5.equals(proxy6));
    }

    /**
     * @tests java.net.Proxy#equals(Object)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object_NotEquals() {
        SocketAddress address1 = new InetSocketAddress("127.0.0.1", 1234);
        SocketAddress address2 = new InetSocketAddress("127.0.0.1", 1235);
        Proxy proxy[] = { new Proxy(Proxy.Type.HTTP, address1),
                new Proxy(Proxy.Type.HTTP, address2),
                new Proxy(Proxy.Type.SOCKS, address1),
                new Proxy(Proxy.Type.SOCKS, address2), Proxy.NO_PROXY,
                new Proxy(null, address1), new Proxy(null, address2) };
        // All of them are not equals
        for (int i = 0; i < proxy.length; i++) {
            for (int j = i + 1; j < proxy.length; j++) {
                assertFalse(proxy[i].equals(proxy[j]));
            }
        }
        // Not equals to an Object type instance. Ensure no exception is thrown.
        assertFalse(proxy[0].equals(new Object()));
    }

    /**
     * @tests java.net.Proxy.Type#valueOf(String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Proxy.Type.DIRECT, Proxy.Type.HTTP, Proxy.Type.SOCKS",
        method = "!Constants",
        args = {}
    )
    public void test_Type_valueOfLjava_lang_String_Normal() {
        assertEquals(Proxy.Type.DIRECT, Proxy.Type.valueOf("DIRECT"));
        assertEquals(Proxy.Type.HTTP, Proxy.Type.valueOf("HTTP"));
        assertEquals(Proxy.Type.SOCKS, Proxy.Type.valueOf("SOCKS"));
    }

    /**
     * @tests java.net.Proxy.Type#valueOf(String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!Constants",
        args = {}
    )
    public void test_Type_valueOfLjava_lang_String_IllegalName() {
        String[] illegalName = { "Direct", "direct", "http", "socks",
                "illegalName", "" };
        for (int i = 0; i < illegalName.length; i++) {
            try {
                Proxy.Type.valueOf(illegalName[i]);
                fail("should throw IllegalArgumentException, illegalName:"
                        + illegalName);
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    /**
     * @tests java.net.Proxy.Type#valueOf(String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!Constants",
        args = {}
    )
    public void test_Type_valueOfLjava_lang_String_NullPointerException() {
        // Some old RIs,which throw IllegalArgumentException.
        // Latest RIs throw NullPointerException.
        try {
            Proxy.Type.valueOf(null);
            fail("should throw an exception.");
        } catch (NullPointerException e) {
            // May be caused by some compilers' code
        } catch (IllegalArgumentException e) {
            // other compilers will throw this
        }
    }

    /**
     * @tests java.net.Proxy.Type#values()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!Constants",
        args = {}
    )
    public void test_Type_values() {
        Proxy.Type types[] = Proxy.Type.values();
        assertEquals(3, types.length);
        assertEquals(Proxy.Type.DIRECT, types[0]);
        assertEquals(Proxy.Type.HTTP, types[1]);
        assertEquals(Proxy.Type.SOCKS, types[2]);
    }

}
