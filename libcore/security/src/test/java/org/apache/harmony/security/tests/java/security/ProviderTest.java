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

/**
 * @author Boris V. Kuznetsov
 * @version $Revision$
 */

package org.apache.harmony.security.tests.java.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.Provider;
import java.security.Provider.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;


import junit.framework.TestCase;

/**
 * Tests for <code>Provider</code> constructor and methods
 * 
 */
public class ProviderTest extends TestCase {

    Provider p;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        p = new MyProvider();
    }

    /*
     * Class under test for void Provider()
     */
    public final void testProvider() {
        if (!p.getProperty("Provider.id name").equals(
                String.valueOf(p.getName()))) {
            fail("Incorrect \"Provider.id name\" value");
        }
        if (!p.getProperty("Provider.id version").equals(
                String.valueOf(p.getVersion()))) {
            fail("Incorrect \"Provider.id version\" value");
        }
        if (!p.getProperty("Provider.id info").equals(
                String.valueOf(p.getInfo()))) {
            fail("Incorrect \"Provider.id info\" value");
        }
        if (!p.getProperty("Provider.id className").equals(
                p.getClass().getName())) {
            fail("Incorrect \"Provider.id className\" value");
        }
    }

    public final void testClear() {
        p.clear();
        if (p.getProperty("MessageDigest.SHA-1") != null) {
            fail("Provider contains properties");
        }
    }

    /*
     * Class under test for void Provider(String, double, String)
     */
    public final void testProviderStringdoubleString() {
        Provider p = new MyProvider("Provider name", 123.456, "Provider info");
        if (!p.getName().equals("Provider name") || p.getVersion() != 123.456
                || !p.getInfo().equals("Provider info")) {
            fail("Incorrect values");
        }
    }

    public final void testGetName() {
        if (!p.getName().equals("MyProvider")) {
            fail("Incorrect provider name");
        }
    }

    public final void testGetVersion() {
        if (p.getVersion() != 1.0) {
            fail("Incorrect provider version");
        }
    }

    public final void testGetInfo() {
        if (!p.getInfo().equals("Provider for testing")) {
            fail("Incorrect provider info");
        }
    }

    /*
     * Class under test for void putAll(Map)
     */
    public final void testPutAllMap() {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("MessageDigest.SHA-1", "aaa.bbb.ccc.ddd");
        hm.put("Property 1", "value 1");
        hm.put("serviceName.algName attrName", "attrValue");
        hm.put("Alg.Alias.engineClassName.aliasName", "stanbdardName");
        p.putAll(hm);
        if (!"value 1".equals(p.getProperty("Property 1").trim())
                || !"attrValue".equals(p.getProperty(
                        "serviceName.algName attrName").trim())
                || !"stanbdardName".equals(p.getProperty(
                        "Alg.Alias.engineClassName.aliasName").trim())
                || !"aaa.bbb.ccc.ddd".equals(p.getProperty(
                        "MessageDigest.SHA-1").trim())) {
            fail("Incorrect property value");
        }
    }

    /*
     * Class under test for Set entrySet()
     */
    public final void testEntrySet() {
        p.put("MessageDigest.SHA-256", "aaa.bbb.ccc.ddd");

        Set s = p.entrySet();
        try {
            s.clear();
            fail("Must return unmodifiable set");
        } catch (UnsupportedOperationException e) {
        }

        assertEquals("Incorrect set size", 8, s.size());

        for (Iterator it = s.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            String key = (String) e.getKey();
            String val = (String) e.getValue();
            if (key.equals("MessageDigest.SHA-1")
                    && val.equals("SomeClassName")) {
                continue;
            }
            if (key.equals("Alg.Alias.MessageDigest.SHA1")
                    && val.equals("SHA-1")) {
                continue;
            }
            if (key.equals("MessageDigest.abc") && val.equals("SomeClassName")) {
                continue;
            }
            if (key.equals("Provider.id className")
                    && val.equals(p.getClass().getName())) {
                continue;
            }
            if (key.equals("Provider.id name") && val.equals("MyProvider")) {
                continue;
            }
            if (key.equals("MessageDigest.SHA-256")
                    && val.equals("aaa.bbb.ccc.ddd")) {
                continue;
            }
            if (key.equals("Provider.id version") && val.equals("1.0")) {
                continue;
            }
            if (key.equals("Provider.id info")
                    && val.equals("Provider for testing")) {
                continue;
            }
            fail("Incorrect set");
        }
    }

    /*
     * Class under test for Set keySet()
     */
    public final void testKeySet() {
        p.put("MessageDigest.SHA-256", "aaa.bbb.ccc.ddd");

        Set s = p.keySet();
        try {
            s.clear();
        } catch (UnsupportedOperationException e) {
        }
        Set s1 = p.keySet();
        if ((s == s1) || s1.isEmpty()) {
            fail("Must return unmodifiable set");
        }
        if (s1.size() != 8) {
            fail("Incorrect set size");
        }
        if (!s1.contains("MessageDigest.SHA-256")
                || !s1.contains("MessageDigest.SHA-1")
                || !s1.contains("Alg.Alias.MessageDigest.SHA1")
                || !s1.contains("MessageDigest.abc")
                || !s1.contains("Provider.id info")
                || !s1.contains("Provider.id className")
                || !s1.contains("Provider.id version")
                || !s1.contains("Provider.id name")) {
            fail("Incorrect set");
        }
    }

    /*
     * Class under test for Collection values()
     */
    public final void testValues() {
        p.put("MessageDigest.SHA-256", "aaa.bbb.ccc.ddd");

        Collection c = p.values();
        try {
            c.clear();
        } catch (UnsupportedOperationException e) {
        }
        Collection c1 = p.values();
        if ((c == c1) || c1.isEmpty()) {
            fail("Must return unmodifiable set");
        }
        if (c1.size() != 8) {
            fail("Incorrect set size " + c1.size());
        }
        if (!c1.contains("MyProvider") || !c1.contains("aaa.bbb.ccc.ddd")
                || !c1.contains("Provider for testing") || !c1.contains("1.0")
                || !c1.contains("SomeClassName") || !c1.contains("SHA-1")
                || !c1.contains(p.getClass().getName())) {
            fail("Incorrect set");
        }
    }

    /*
     * Class under test for Object put(Object, Object)
     */
    public final void testPutObjectObject() {
        p.put("MessageDigest.SHA-1", "aaa.bbb.ccc.ddd");
        p.put("Type.Algorithm", "className");
        if (!"aaa.bbb.ccc.ddd".equals(p.getProperty("MessageDigest.SHA-1")
                .trim())) {
            fail("Incorrect property value");
        }

        Set services = p.getServices();
        if (services.size() != 3) {
            fail("incorrect size");
        }
        for (Iterator it = services.iterator(); it.hasNext();) {
            Provider.Service s = (Provider.Service) it.next();
            if ("Type".equals(s.getType())
                    && "Algorithm".equals(s.getAlgorithm())
                    && "className".equals(s.getClassName())) {
                continue;
            }
            if ("MessageDigest".equals(s.getType())
                    && "SHA-1".equals(s.getAlgorithm())
                    && "aaa.bbb.ccc.ddd".equals(s.getClassName())) {
                continue;
            }
            if ("MessageDigest".equals(s.getType())
                    && "abc".equals(s.getAlgorithm())
                    && "SomeClassName".equals(s.getClassName())) {
                continue;
            }
            fail("Incorrect service");
        }
    }

    /*
     * Class under test for Object remove(Object)
     */
    public final void testRemoveObject() {
        Object o = p.remove("MessageDigest.SHA-1");
        if (!"SomeClassName".equals(o)) {
            fail("Incorrect return value");
        }
        if (p.getProperty("MessageDigest.SHA-1") != null) {
            fail("Provider contains properties");
        }
        if (p.getServices().size() != 1) {
            fail("Service not removed");
        }
    }

    public final void testService1() {
        p.put("MessageDigest.SHA-1", "AnotherClassName");
        Provider.Service s = p.getService("MessageDigest", "SHA-1");
        if (!"AnotherClassName".equals(s.getClassName())) {
            fail("Incorrect class name " + s.getClassName());
        }
    }

    // public final void testService2() {
    // Provider[] pp = Security.getProviders("MessageDigest.SHA-1");
    // if (pp == null) {
    // return;
    // }
    // Provider p2 = pp[0];
    // String old = p2.getProperty("MessageDigest.SHA-1");
    // try {
    // p2.put("MessageDigest.SHA-1", "AnotherClassName");
    // Provider.Service s = p2.getService("MessageDigest", "SHA-1");
    // if (!"AnotherClassName".equals(s.getClassName())) {
    // fail("Incorrect class name " + s.getClassName());
    // }
    // try {
    // s.newInstance(null);
    // fail("No expected NoSuchAlgorithmException");
    // } catch (NoSuchAlgorithmException e) {
    // }
    // } finally {
    // p2.put("MessageDigest.SHA-1", old);
    // }
    // }

    // Regression for HARMONY-2760.
    public void testConstructor() {
        MyProvider myProvider = new MyProvider(null, 1, null);
        assertNull(myProvider.getName());
        assertNull(myProvider.getInfo());
        assertEquals("null", myProvider.getProperty("Provider.id name"));
        assertEquals("null", myProvider.getProperty("Provider.id info"));
    }

    public final void testGetServices() {
        MyProvider myProvider = new MyProvider(null, 1, null);
        Set<Provider.Service> services = myProvider.getServices();
        assertEquals(0, services.size());

        Provider.Service s[] = new Provider.Service[3];

        s[0] = new Provider.Service(p, "type1", "algorithm1", "className1",
                null, null);
        s[1] = new Provider.Service(p, "type2", "algorithm2", "className2",
                null, null);
        s[2] = new Provider.Service(p, "type3", "algorithm3", "className3",
                null, null);
        myProvider.putService(s[0]);
        myProvider.putService(s[1]);
        assertEquals(2, myProvider.getNumServices());
        Set<Service> actual = myProvider.getServices();

        assertTrue(actual.contains(s[0]));
        assertTrue(actual.contains(s[1]));
        assertTrue(!actual.contains(s[2]));

        myProvider.removeService(s[1]);
        actual = myProvider.getServices();
        assertEquals(1, myProvider.getNumServices());

        assertTrue(actual.contains(s[0]));
        assertTrue(!actual.contains(s[1]));
        assertTrue(!actual.contains(s[2]));

        myProvider.putService(s[2]);
        actual = myProvider.getServices();
        assertEquals(2, myProvider.getNumServices());
        assertTrue(actual.contains(s[0]));
        assertTrue(!actual.contains(s[1]));
        assertTrue(actual.contains(s[2]));
    }

    public final void testPutService() {
        MyProvider myProvider = new MyProvider(null, 1, null);
        Provider.Service s[] = new Provider.Service[3];

        s[0] = new Provider.Service(p, "type1", "algorithm1", "className1",
                null, null);
        s[1] = new Provider.Service(p, "type2", "algorithm2", "className2",
                null, null);
        s[2] = new Provider.Service(p, "type3", "algorithm3", "className3",
                null, null);
        myProvider.putService(s[0]);
        myProvider.putService(s[1]);
        assertEquals(2, myProvider.getNumServices());
        Set<Service> actual = myProvider.getServices();

        assertTrue(actual.contains(s[0]));
        assertTrue(actual.contains(s[1]));
        assertTrue(!actual.contains(s[2]));

        myProvider.removeService(s[1]);
        assertEquals(1, myProvider.getNumServices());
        actual = myProvider.getServices();

        assertTrue(actual.contains(s[0]));
        assertTrue(!actual.contains(s[1]));
        assertTrue(!actual.contains(s[2]));

        myProvider.putService(s[2]);
        actual = myProvider.getServices();
        assertEquals(2, myProvider.getNumServices());
        assertTrue(actual.contains(s[0]));
        assertTrue(!actual.contains(s[1]));
        assertTrue(actual.contains(s[2]));

        myProvider.putService(s[2]);
        actual = myProvider.getServices();
        assertEquals(2, myProvider.getNumServices());
        assertTrue(actual.contains(s[0]));
        assertTrue(!actual.contains(s[1]));
        assertTrue(actual.contains(s[2]));

        try {
            myProvider.putService(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    public final void testRemoveService() {
        MyProvider myProvider = new MyProvider(null, 1, null);
        try {
            myProvider.removeService(null);
            fail("NullPoiterException expected");
        } catch (NullPointerException e) {
            // expected
        }

        Provider.Service s[] = new Provider.Service[3];

        s[0] = new Provider.Service(p, "type0", "algorithm0", "className0",
                null, null);
        s[1] = new Provider.Service(p, "type1", "algorithm1", "className1",
                null, null);
        s[2] = new Provider.Service(p, "type2", "algorithm2", "className2",
                null, null);

        try {
            myProvider.removeService(s[0]);
        } catch (NullPointerException e) {
            fail("Unexpected exception");
        }

        myProvider.putService(s[0]);
        myProvider.putService(s[1]);
        myProvider.putService(s[2]);
        assertEquals(3, myProvider.getNumServices());
        Set<Service> actual = myProvider.getServices();

        assertTrue(actual.contains(s[0]));
        assertTrue(actual.contains(s[1]));
        assertTrue(actual.contains(s[2]));

        myProvider.removeService(s[1]);
        assertEquals(2, myProvider.getNumServices());
        actual = myProvider.getServices();

        assertTrue(actual.contains(s[0]));
        assertTrue(!actual.contains(s[1]));
        assertTrue(actual.contains(s[2]));

        myProvider.removeService(s[0]);
        assertEquals(1, myProvider.getNumServices());
        actual = myProvider.getServices();

        assertTrue(!actual.contains(s[0]));
        assertTrue(!actual.contains(s[1]));
        assertTrue(actual.contains(s[2]));

        myProvider.removeService(s[2]);
        assertEquals(0, myProvider.getNumServices());
        actual = myProvider.getServices();

        assertTrue(!actual.contains(s[0]));
        assertTrue(!actual.contains(s[1]));
        assertTrue(!actual.contains(s[2]));

        try {
            myProvider.removeService(null);
            fail("NullPoiterException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Class under test for void load(InputStream)
     */
    public final void testLoad() throws IOException {
        InputStream is = new ByteArrayInputStream(writeProperties());
        MyProvider myProvider = new MyProvider("name", 1, "info");
        myProvider.load(is);
        assertEquals("tests.security", myProvider.get("test.pkg"));
        assertEquals("Unit Tests", myProvider.get("test.proj"));
        assertNull(myProvider.get("#commented.entry"));

        assertEquals("info", myProvider.get("Provider.id info"));
        String className = myProvider.getClass().toString();
        assertEquals(
                className.substring("class ".length(), className.length()),
                myProvider.get("Provider.id className"));
        assertEquals("1.0", myProvider.get("Provider.id version"));

        try {
            myProvider.load(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    protected byte[] writeProperties() throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bout);
        ps.println("#commented.entry=Bogus");
        ps.println("test.pkg=tests.security");
        ps.println("test.proj=Unit Tests");
        ps.close();
        return bout.toByteArray();
    }

    class MyProvider extends Provider {
        private Set<Provider.Service> services = null;

        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
            put("MessageDigest.SHA-1", "SomeClassName");
            put("MessageDigest.abc", "SomeClassName");
            put("Alg.Alias.MessageDigest.SHA1", "SHA-1");
            if (services != null) {
                services.clear();
            } else {
                services = new HashSet<Service>();
            }
        }

        MyProvider(String name, double version, String info) {
            super(name, version, info);
            if (services != null) {
                services.clear();
            } else {
                services = new HashSet<Service>();
            }
        }

        public void putService(Provider.Service s) {
            super.putService(s);
            services.add(s);
        }

        public void removeService(Provider.Service s) {
            super.removeService(s);
            services.remove(s);
        }

        public int getNumServices() {
            return services.size();
        }
    }
}
