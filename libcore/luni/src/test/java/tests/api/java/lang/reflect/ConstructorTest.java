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

package tests.api.java.lang.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class ConstructorTest extends junit.framework.TestCase {

    static class ConstructorTestHelper extends Object {
        int cval;

        public ConstructorTestHelper() throws IndexOutOfBoundsException {
            cval = 99;
        }

        public ConstructorTestHelper(Object x) {
        }

        private ConstructorTestHelper(int a) {
        }

        protected ConstructorTestHelper(long a) {
        }

        public int check() {
            return cval;
        }
    }

    /**
     * @tests java.lang.reflect.Constructor#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean
        // java.lang.reflect.Constructor.equals(java.lang.Object)
        Class[] types = null;
        Constructor ctor1 = null, ctor2 = null;
        try {
            ctor1 = new ConstructorTestHelper().getClass().getConstructor(
                    new Class[0]);

            Class[] parms = null;
            parms = new Class[1];
            parms[0] = new Object().getClass();
            ctor2 = new ConstructorTestHelper().getClass()
                    .getConstructor(parms);
        } catch (Exception e) {
            fail("Exception during equals test : " + e.getMessage());
        }
        assertTrue("Different Contructors returned equal", !ctor1.equals(ctor2));
    }

    /**
     * @tests java.lang.reflect.Constructor#getDeclaringClass()
     */
    public void test_getDeclaringClass() {
        // Test for method java.lang.Class
        // java.lang.reflect.Constructor.getDeclaringClass()
        boolean val = false;
        try {
            Class pclass = new ConstructorTestHelper().getClass();
            Constructor ctor = pclass.getConstructor(new Class[0]);
            val = ctor.getDeclaringClass().equals(pclass);
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
        assertTrue("Returned incorrect declaring class", val);
    }

    /**
     * @tests java.lang.reflect.Constructor#getExceptionTypes()
     */
    public void test_getExceptionTypes() {
        // Test for method java.lang.Class []
        // java.lang.reflect.Constructor.getExceptionTypes()
        Class[] exceptions = null;
        Class ex = null;
        try {
            Constructor ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(new Class[0]);
            exceptions = ctor.getExceptionTypes();
            ex = new IndexOutOfBoundsException().getClass();
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
        assertEquals("Returned exception list of incorrect length",
                1, exceptions.length);
        assertTrue("Returned incorrect exception", exceptions[0].equals(ex));
    }

    /**
     * @tests java.lang.reflect.Constructor#getModifiers()
     */
    public void test_getModifiers() {
        // Test for method int java.lang.reflect.Constructor.getModifiers()
        int mod = 0;
        try {
            Constructor ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(new Class[0]);
            mod = ctor.getModifiers();
            assertTrue("Returned incorrect modifers for public ctor",
                    ((mod & Modifier.PUBLIC) == Modifier.PUBLIC)
                            && ((mod & Modifier.PRIVATE) == 0));
        } catch (NoSuchMethodException e) {
            fail("Exception during test : " + e.getMessage());
        }
        try {
            Class[] cl = { int.class };
            Constructor ctor = new ConstructorTestHelper().getClass()
                    .getDeclaredConstructor(cl);
            mod = ctor.getModifiers();
            assertTrue("Returned incorrect modifers for private ctor",
                    ((mod & Modifier.PRIVATE) == Modifier.PRIVATE)
                            && ((mod & Modifier.PUBLIC) == 0));
        } catch (NoSuchMethodException e) {
            fail("Exception during test : " + e.getMessage());
        }
        try {
            Class[] cl = { long.class };
            Constructor ctor = new ConstructorTestHelper().getClass()
                    .getDeclaredConstructor(cl);
            mod = ctor.getModifiers();
            assertTrue("Returned incorrect modifers for private ctor",
                    ((mod & Modifier.PROTECTED) == Modifier.PROTECTED)
                            && ((mod & Modifier.PUBLIC) == 0));
        } catch (NoSuchMethodException e) {
            fail("NoSuchMethodException during test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Constructor#getName()
     */
    public void test_getName() {
        // Test for method java.lang.String
        // java.lang.reflect.Constructor.getName()
        try {
            Constructor ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(new Class[0]);
            assertTrue(
                    "Returned incorrect name: " + ctor.getName(),
                    ctor
                            .getName()
                            .equals(
                                    "tests.api.java.lang.reflect.ConstructorTest$ConstructorTestHelper"));
        } catch (Exception e) {
            fail("Exception obtaining contructor : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Constructor#getParameterTypes()
     */
    public void test_getParameterTypes() {
        // Test for method java.lang.Class []
        // java.lang.reflect.Constructor.getParameterTypes()
        Class[] types = null;
        try {
            Constructor ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(new Class[0]);
            types = ctor.getParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test:"
                    + e.toString());
        }
        assertEquals("Incorrect parameter returned", 0, types.length);

        Class[] parms = null;
        try {
            parms = new Class[1];
            parms[0] = new Object().getClass();
            Constructor ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(parms);
            types = ctor.getParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test:"
                    + e.toString());
        }
        assertTrue("Incorrect parameter returned", types[0].equals(parms[0]));
    }

    /**
     * @tests java.lang.reflect.Constructor#newInstance(java.lang.Object[])
     */
    public void test_newInstance$Ljava_lang_Object() {
        // Test for method java.lang.Object
        // java.lang.reflect.Constructor.newInstance(java.lang.Object [])

        ConstructorTestHelper test = null;
        try {
            Constructor ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(new Class[0]);
            test = (ConstructorTestHelper) ctor.newInstance((Object[])null);
        } catch (Exception e) {
            fail("Failed to create instance : " + e.getMessage());
        }
        assertEquals("improper instance created", 99, test.check());
    }

    /**
     * @tests java.lang.reflect.Constructor#toString()
     */
    public void test_toString() {
        // Test for method java.lang.String
        // java.lang.reflect.Constructor.toString()
        Class[] parms = null;
        Constructor ctor = null;
        try {
            parms = new Class[1];
            parms[0] = new Object().getClass();
            ctor = new ConstructorTestHelper().getClass().getConstructor(parms);
        } catch (Exception e) {
            fail("Exception during getParameterTypes test:"
                    + e.toString());
        }
        assertTrue(
                "Returned incorrect string representation: " + ctor.toString(),
                ctor
                        .toString()
                        .equals(
                                "public tests.api.java.lang.reflect.ConstructorTest$ConstructorTestHelper(java.lang.Object)"));
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
}
