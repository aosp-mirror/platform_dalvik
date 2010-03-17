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

package tests.api.java.io;

import java.io.File;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.net.URL;                                                                                                                 
import java.net.URLClassLoader; 
import java.lang.reflect.Proxy;

import junit.framework.TestCase;

public class ObjectStreamClassTest extends TestCase {

    static class DummyClass implements Serializable {
        private static final long serialVersionUID = 999999999999999L;

        long bam = 999L;

        int ham = 9999;

		public static long getUID() {
			return serialVersionUID;
		}
	}
    
    /**
     * @tests java.io.ObjectStreamClass#forClass()
     */
    public void test_forClass() {
        // Need to test during serialization to be sure an instance is
        // returned
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        assertEquals("forClass returned an object: " + osc.forClass(),
                DummyClass.class, osc.forClass()); 
    }

    /**
     * @tests java.io.ObjectStreamClass#getField(java.lang.String)
     */
    public void test_getFieldLjava_lang_String() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        assertEquals("getField did not return correct field", 'J', osc
                .getField("bam").getTypeCode());
        assertNull("getField did not null for non-existent field", osc
                .getField("wham"));
    }

    /**
     * @tests java.io.ObjectStreamClass#getFields()
     */
    public void test_getFields() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        ObjectStreamField[] osfArray = osc.getFields();
        assertTrue(
                "Array of fields should be of length 2 but is instead of length: "
                        + osfArray.length, osfArray.length == 2);
    }

    /**
     * @tests java.io.ObjectStreamClass#getName()
     */
    public void test_getName() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        assertEquals(
                "getName returned incorrect name: " + osc.getName(),
                "tests.api.java.io.ObjectStreamClassTest$DummyClass", // android-changed
                osc.getName());
    }

    /**
     * @tests java.io.ObjectStreamClass#getSerialVersionUID()
     */
    public void test_getSerialVersionUID() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        assertTrue("getSerialversionUID returned incorrect uid: "
                + osc.getSerialVersionUID() + " instead of "
                + DummyClass.getUID(), osc.getSerialVersionUID() == DummyClass
                .getUID());
    }

    static class SyntheticTest implements Serializable {
        private int i;

        private class X implements Serializable {
            public int get() {
                return i;
            }
        }

        public X foo() {
            return new X();
        }
    }

    /**
     * @tests java.io.ObjectStreamClass#getSerialVersionUID()
     */
    public void test_getSerialVersionUID_inner_private_class() {
        ObjectStreamClass osc1 = ObjectStreamClass.lookup(SyntheticTest.class);
        assertEquals("SyntheticTest unexpected UID: "
                + osc1.getSerialVersionUID(), -7784078941584535183L, osc1
                .getSerialVersionUID());

        ObjectStreamClass osc2 = ObjectStreamClass
                .lookup(SyntheticTest.X.class);
        assertEquals("SyntheticTest.X unexpected UID: "
                + osc2.getSerialVersionUID(), -7703000075736397332L, osc2
                .getSerialVersionUID());
    }

    /**
     * @tests java.io.ObjectStreamClass#getSerialVersionUID()
     */
    public void test_getSerialVersionUID_classloader() throws Exception {
        File file = new File(
                "resources/org/apache/harmony/luni/tests/ObjectStreamClassTest.jar");
        ClassLoader loader = new URLClassLoader(new URL[] { file.toURL() },
                null);
        Class cl1 = Class.forName("Test1$TestVarArgs", false, loader);
        ObjectStreamClass osc1 = ObjectStreamClass.lookup(cl1);
        assertEquals("Test1$TestVarArgs unexpected UID: "
                + osc1.getSerialVersionUID(), -6051121963037986215L, osc1
                .getSerialVersionUID());

        Class cl2 = Class.forName("Test1$TestBridge", false, loader);
        ObjectStreamClass osc2 = ObjectStreamClass.lookup(cl2);
        assertEquals("Test1$TestBridge unexpected UID: "
                + osc2.getSerialVersionUID(), 568585976855071180L, osc2
                .getSerialVersionUID());
    }

    /**
     * @tests java.io.ObjectStreamClass#lookup(java.lang.Class)
     */
    public void test_lookupLjava_lang_Class() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        assertEquals(
                "lookup returned wrong class: " + osc.getName(),
                "tests.api.java.io.ObjectStreamClassTest$DummyClass", // android-changed
                osc.getName());
    }

    /**
     * @tests java.io.ObjectStreamClass#toString()
     */
    public void test_toString() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
        String oscString = osc.toString();

        // The previous test was more specific than the spec so it was replaced
        // with the test below
        assertTrue("toString returned incorrect string: " + osc.toString(),
                oscString.indexOf("serialVersionUID") >= 0
                        && oscString.indexOf("999999999999999L") >= 0);
    }

    public void testSerialization() {
        ObjectStreamClass osc = ObjectStreamClass
                .lookup(ObjectStreamClass.class);
        assertEquals(0, osc.getFields().length);
    }

    public void test_specialTypes() {
        Class<?> proxyClass = Proxy.getProxyClass(this.getClass()
                .getClassLoader(), new Class[] { Runnable.class });

        ObjectStreamClass proxyStreamClass = ObjectStreamClass
                .lookup(proxyClass);

        assertEquals("Proxy classes should have zero serialVersionUID", 0,
                proxyStreamClass.getSerialVersionUID());
        ObjectStreamField[] proxyFields = proxyStreamClass.getFields();
        assertEquals("Proxy classes should have no serialized fields", 0,
                proxyFields.length);

        ObjectStreamClass enumStreamClass = ObjectStreamClass
                .lookup(Thread.State.class);

        assertEquals("Enum classes should have zero serialVersionUID", 0,
                enumStreamClass.getSerialVersionUID());
        ObjectStreamField[] enumFields = enumStreamClass.getFields();
        assertEquals("Enum classes should have no serialized fields", 0,
                enumFields.length);
    }
    
        /**
     * @since 1.6 
     */
    static class NonSerialzableClass {
        private static final long serialVersionUID = 1l;
        public static long getUID() {
            return serialVersionUID;
        }
    }
    
    /**
     * @since 1.6
     */
    static class ExternalizableClass implements Externalizable {

        private static final long serialVersionUID = -4285635779249689129L;

        public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
            throw new ClassNotFoundException();
        }

        public void writeExternal(ObjectOutput output) throws IOException {
            throw new IOException();
        }
        
	}
	
    /**
     * @tests java.io.ObjectStreamClass#lookupAny(java.lang.Class)
     * @since 1.6
     */
    public void test_lookupAnyLjava_lang_Class() {
        // Test for method java.io.ObjectStreamClass
        // java.io.ObjectStreamClass.lookupAny(java.lang.Class)
        ObjectStreamClass osc = ObjectStreamClass.lookupAny(DummyClass.class);
        assertEquals("lookup returned wrong class: " + osc.getName(),
                "tests.api.java.io.ObjectStreamClassTest$DummyClass", osc  // android-changed
                        .getName());
        
        osc = ObjectStreamClass.lookupAny(NonSerialzableClass.class);
        assertEquals("lookup returned wrong class: " + osc.getName(),
                "tests.api.java.io.ObjectStreamClassTest$NonSerialzableClass", // android-changed
                osc.getName());
        
        osc = ObjectStreamClass.lookupAny(ExternalizableClass.class);        
        assertEquals("lookup returned wrong class: " + osc.getName(),
                "tests.api.java.io.ObjectStreamClassTest$ExternalizableClass", // android-changed
                osc.getName());

        osc = ObjectStreamClass.lookup(NonSerialzableClass.class);
        assertNull(osc);
        
    }
    
    
}
