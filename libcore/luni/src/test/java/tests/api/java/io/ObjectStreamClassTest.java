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

import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;

public class ObjectStreamClassTest extends junit.framework.TestCase {

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
		// Test for method java.lang.Class java.io.ObjectStreamClass.forClass()
		// Need to test during serialization to be sure an instance is
		// returned
		ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
		assertTrue("forClass returned an object: " + osc.forClass(), osc
				.forClass().equals(DummyClass.class));
	}

	/**
	 * @tests java.io.ObjectStreamClass#getField(java.lang.String)
	 */
	public void test_getFieldLjava_lang_String() {
		// Test for method java.io.ObjectStreamField
		// java.io.ObjectStreamClass.getField(java.lang.String)
		ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
		assertEquals("getField did not return correct field", 'J', osc.getField("bam")
				.getTypeCode());
		assertNull("getField did not null for non-existent field", osc
				.getField("wham"));
	}

	/**
	 * @tests java.io.ObjectStreamClass#getFields()
	 */
	public void test_getFields() {
		// Test for method java.io.ObjectStreamField []
		// java.io.ObjectStreamClass.getFields()
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
		// Test for method java.lang.String java.io.ObjectStreamClass.getName()
		ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
		assertTrue("getName returned incorrect name: " + osc.getName(), osc
				.getName().equals(
						"tests.api.java.io.ObjectStreamClassTest$DummyClass"));
	}

	/**
	 * @tests java.io.ObjectStreamClass#getSerialVersionUID()
	 */
	public void test_getSerialVersionUID() {
		// Test for method long java.io.ObjectStreamClass.getSerialVersionUID()
		ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
		assertTrue("getSerialversionUID returned incorrect uid: "
				+ osc.getSerialVersionUID() + " instead of "
				+ DummyClass.getUID(), osc.getSerialVersionUID() == DummyClass
				.getUID());
	}

	/**
	 * @tests java.io.ObjectStreamClass#lookup(java.lang.Class)
	 */
	public void test_lookupLjava_lang_Class() {
		// Test for method java.io.ObjectStreamClass
		// java.io.ObjectStreamClass.lookup(java.lang.Class)
		ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
		assertTrue("lookup returned wrong class: " + osc.getName(), osc
				.getName().equals(
						"tests.api.java.io.ObjectStreamClassTest$DummyClass"));
	}

	/**
	 * @tests java.io.ObjectStreamClass#toString()
	 */
	public void test_toString() {
		// Test for method java.lang.String java.io.ObjectStreamClass.toString()
		ObjectStreamClass osc = ObjectStreamClass.lookup(DummyClass.class);
		String oscString = osc.toString();
		// The previous test was more specific than the spec so it was replaced
		// with the test below
		assertTrue("toString returned incorrect string: " + osc.toString(),
				oscString.indexOf("serialVersionUID") >= 0
						&& oscString.indexOf("999999999999999L") >= 0);
		;
	}

    public void testSerialization() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(ObjectStreamClass.class);
        assertEquals(0, osc.getFields().length);
    }
    
	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}

// BEGIN android-added
    public void testFooSerialVersionUid() {
        assertEquals(-5887964677443030867L, Foo.serialVersionUID());
    }

    /**
     * An arbitrary class which deliberately tickles various factors affecting
     * serialVersionUID calculation.
     */
    static abstract class Foo implements Cloneable, Serializable {

        /** All fields except "private static|transient", which these aren't. */
        private final String name = "foo";
        static final long now;

        /** Presence of static initializer has an affect. */
        static {
            now = System.currentTimeMillis();
        }

        /** Non-private constructors. */
        Foo() {}
        protected Foo(int ignored) {}

        /** Non-private methods. */
        synchronized static int foo() { return 0; }
        static int bar() { return 0; }
        abstract void tee();
        protected native synchronized boolean bob();
        protected synchronized void tim() {}

        /** Calculates Foo's default serialVersionUID. */
        static long serialVersionUID() {
            return ObjectStreamClass.lookup(Foo.class).getSerialVersionUID();
        }
    }

    public static void main(String[] args) {
        System.out.println(Foo.serialVersionUID());
    }
// END android-added
}
