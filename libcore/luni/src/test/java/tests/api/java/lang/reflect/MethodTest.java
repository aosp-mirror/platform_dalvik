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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodTest extends junit.framework.TestCase {

    static class TestMethod {
        public TestMethod() {
        }

        public void voidMethod() throws IllegalArgumentException {
        }

        public void parmTest(int x, short y, String s, boolean bool, Object o,
                long l, byte b, char c, double d, float f) {
        }

        public int intMethod() {
            return 1;
        }

        public static final void printTest(int x, short y, String s,
                boolean bool, Object o, long l, byte b, char c, double d,
                float f) {
        }

        public double doubleMethod() {
            return 1.0;
        }

        public short shortMethod() {
            return (short) 1;
        }

        public byte byteMethod() {
            return (byte) 1;
        }

        public float floatMethod() {
            return 1.0f;
        }

        public long longMethod() {
            return 1l;
        }

        public char charMethod() {
            return 'T';
        }

        public Object objectMethod() {
            return new Object();
        }

        private static void prstatic() {
        }

        public static void pustatic() {
        }

        public static synchronized void pustatsynch() {
        }

        public static int invokeStaticTest() {
            return 1;
        }

        public int invokeInstanceTest() {
            return 1;
        }

        private int privateInvokeTest() {
            return 1;
        }

        public int invokeExceptionTest() throws NullPointerException {
            throw new NullPointerException();
        }

        public static synchronized native void pustatsynchnat();

        public void invokeCastTest1(byte param) {
        }

        public void invokeCastTest1(short param) {
        }

        public void invokeCastTest1(int param) {
        }

        public void invokeCastTest1(long param) {
        }

        public void invokeCastTest1(float param) {
        }

        public void invokeCastTest1(double param) {
        }

        public void invokeCastTest1(char param) {
        }

        public void invokeCastTest1(boolean param) {
        }
    }

    abstract class AbstractTestMethod {
        public abstract void puabs();
    }

    class TestMethodSub extends TestMethod {
        public int invokeInstanceTest() {
            return 0;
        }
    }

    /**
     * @tests java.lang.reflect.Method#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean
        // java.lang.reflect.Method.equals(java.lang.Object)

        Method m1 = null, m2 = null;
        try {
            m1 = TestMethod.class.getMethod("invokeInstanceTest", new Class[0]);
            m2 = TestMethodSub.class.getMethod("invokeInstanceTest",
                    new Class[0]);
        } catch (Exception e) {
            fail("Exception during equals test : " + e.getMessage());
        }
        assertTrue("Overriden method returned equal", !m1.equals(m2));
        assertTrue("Same method returned not-equal", m1.equals(m1));
        try {
            m1 = TestMethod.class.getMethod("invokeStaticTest", new Class[0]);
            m2 = TestMethodSub.class
                    .getMethod("invokeStaticTest", new Class[0]);
        } catch (Exception e) {
            fail("Exception during equals test : " + e.getMessage());
        }
        assertTrue("Inherited method returned not-equal", m1.equals(m2));
    }

    /**
     * @tests java.lang.reflect.Method#getDeclaringClass()
     */
    public void test_getDeclaringClass() {
        // Test for method java.lang.Class
        // java.lang.reflect.Method.getDeclaringClass()

        Method[] mths;

        try {
            mths = TestMethod.class.getDeclaredMethods();
            assertTrue("Returned incorrect declaring class: "
                    + mths[0].getDeclaringClass().toString(), mths[0]
                    .getDeclaringClass().equals(TestMethod.class));
        } catch (Exception e) {
            fail("Exception during getDeclaringClass test: "
                    + e.toString());
        }
    }

    /**
     * @tests java.lang.reflect.Method#getExceptionTypes()
     */
    public void test_getExceptionTypes() {
        // Test for method java.lang.Class []
        // java.lang.reflect.Method.getExceptionTypes()

        try {
            Method mth = TestMethod.class.getMethod("voidMethod", new Class[0]);
            Class[] ex = mth.getExceptionTypes();
            assertEquals("Returned incorrect number of exceptions",
                    1, ex.length);
            assertTrue("Returned incorrect exception type", ex[0]
                    .equals(IllegalArgumentException.class));
            mth = TestMethod.class.getMethod("intMethod", new Class[0]);
            ex = mth.getExceptionTypes();
            assertEquals("Returned incorrect number of exceptions",
                    0, ex.length);
        } catch (Exception e) {
            fail("Exception during getExceptionTypes: " + e.toString());
        }

    }

    /**
     * @tests java.lang.reflect.Method#getModifiers()
     */
    public void test_getModifiers() {
        // Test for method int java.lang.reflect.Method.getModifiers()

        Class cl = TestMethod.class;
        int mods = 0;
        Method mth = null;
        int mask = 0;
        try {
            mth = cl.getMethod("pustatic", new Class[0]);
            mods = mth.getModifiers();
        } catch (Exception e) {
            fail("Exception during getModfiers test: " + e.toString());
        }
        mask = Modifier.PUBLIC | Modifier.STATIC;
        assertTrue("Incorrect modifiers returned", (mods | mask) == mask);
        try {
            mth = cl.getDeclaredMethod("prstatic", new Class[0]);
            mods = mth.getModifiers();
        } catch (Exception e) {
            fail("Exception during getModfiers test: " + e.toString());
        }
        mask = Modifier.PRIVATE | Modifier.STATIC;
        assertTrue("Incorrect modifiers returned", (mods | mask) == mask);
        try {
            mth = cl.getDeclaredMethod("pustatsynch", new Class[0]);
            mods = mth.getModifiers();
        } catch (Exception e) {
            fail("Exception during getModfiers test: " + e.toString());
        }
        mask = (Modifier.PUBLIC | Modifier.STATIC) | Modifier.SYNCHRONIZED;
        assertTrue("Incorrect modifiers returned", (mods | mask) == mask);
        try {
            mth = cl.getDeclaredMethod("pustatsynchnat", new Class[0]);
            mods = mth.getModifiers();
        } catch (Exception e) {
            fail("Exception during getModfiers test: " + e.toString());
        }
        mask = ((Modifier.PUBLIC | Modifier.STATIC) | Modifier.SYNCHRONIZED)
                | Modifier.NATIVE;
        assertTrue("Incorrect modifiers returned", (mods | mask) == mask);
        cl = AbstractTestMethod.class;
        try {
            mth = cl.getDeclaredMethod("puabs", new Class[0]);
            mods = mth.getModifiers();
        } catch (Exception e) {
            fail("Exception during getModfiers test: " + e.toString());
        }
        mask = Modifier.PUBLIC | Modifier.ABSTRACT;
        assertTrue("Incorrect modifiers returned", (mods | mask) == mask);
    }

    /**
     * @tests java.lang.reflect.Method#getName()
     */
    public void test_getName() {
        // Test for method java.lang.String java.lang.reflect.Method.getName()
        Method mth = null;
        try {
            mth = TestMethod.class.getMethod("voidMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getMethodName(): " + e.toString());
        }
        assertEquals("Returned incorrect method name", 
                "voidMethod", mth.getName());
    }

    /**
     * @tests java.lang.reflect.Method#getParameterTypes()
     */
    public void test_getParameterTypes() {
        // Test for method java.lang.Class []
        // java.lang.reflect.Method.getParameterTypes()
        Class cl = TestMethod.class;
        Method mth = null;
        Class[] parms = null;
        Method[] methods = null;
        Class[] plist = { int.class, short.class, String.class, boolean.class,
                Object.class, long.class, byte.class, char.class, double.class,
                float.class };
        try {
            mth = cl.getMethod("voidMethod", new Class[0]);
            parms = mth.getParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test: "
                    + e.toString());
        }
        assertEquals("Returned incorrect parameterTypes", 0, parms.length);
        try {
            mth = cl.getMethod("parmTest", plist);
            parms = mth.getParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test: "
                    + e.toString());
        }
        assertTrue("Invalid number of parameters returned",
                plist.length == parms.length);
        for (int i = 0; i < plist.length; i++)
            assertTrue("Incorrect parameter returned", plist[i]
                    .equals(parms[i]));

        // Test same method. but this time pull it from the list of methods
        // rather than asking for it explicitly
        methods = cl.getDeclaredMethods();

        int i;
        for (i = 0; i < methods.length; i++)
            if (methods[i].getName().equals("parmTest")) {
                mth = methods[i];
                i = methods.length + 1;
            }
        if (i < methods.length) {
            parms = mth.getParameterTypes();
            assertTrue("Incorrect number of parameters returned",
                    parms.length == plist.length);
            for (i = 0; i < plist.length; i++)
                assertTrue("Incorrect parameter returned", plist[i]
                        .equals(parms[i]));
        }
    }

    /**
     * @tests java.lang.reflect.Method#getReturnType()
     */
    public void test_getReturnType() {
        // Test for method java.lang.Class
        // java.lang.reflect.Method.getReturnType()
        Class cl = TestMethod.class;
        Method mth = null;
        try {
            mth = cl.getMethod("charMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted char", mth
                .getReturnType().equals(char.class));
        try {
            mth = cl.getMethod("longMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted long", mth
                .getReturnType().equals(long.class));
        try {
            mth = cl.getMethod("shortMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted short", mth
                .getReturnType().equals(short.class));
        try {
            mth = cl.getMethod("intMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted int: "
                + mth.getReturnType(), mth.getReturnType().equals(int.class));
        try {
            mth = cl.getMethod("doubleMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted double", mth
                .getReturnType().equals(double.class));
        try {
            mth = cl.getMethod("byteMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted byte", mth
                .getReturnType().equals(byte.class));
        try {
            mth = cl.getMethod("byteMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test:" + e.toString());
        }
        assertTrue("Gave incorrect returne type, wanted byte", mth
                .getReturnType().equals(byte.class));
        try {
            mth = cl.getMethod("objectMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted Object", mth
                .getReturnType().equals(Object.class));

        try {
            mth = cl.getMethod("voidMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted void", mth
                .getReturnType().equals(void.class));
    }

    /**
     * @tests java.lang.reflect.Method#invoke(java.lang.Object,
     *        java.lang.Object[])
     */
    public void test_invokeLjava_lang_Object$Ljava_lang_Object() {
        // Test for method java.lang.Object
        // java.lang.reflect.Method.invoke(java.lang.Object, java.lang.Object
        // [])

        Class cl = TestMethod.class;
        Method mth = null;
        Object ret = null;
        Class[] dcl = new Class[0];

        // Get and invoke a static method
        try {
            mth = cl.getDeclaredMethod("invokeStaticTest", dcl);
        } catch (Exception e) {
            fail(
                    "Unable to obtain method for invoke test: invokeStaticTest");
        }
        try {
            ret = mth.invoke(null, new Object[0]);
        } catch (Exception e) {
            fail("Exception during invoke test : " + e.getMessage());
        }
        assertEquals("Invoke returned incorrect value", 1, ((Integer) ret)
                .intValue());

        // Get and invoke an instance method
        try {
            mth = cl.getDeclaredMethod("invokeInstanceTest", dcl);
        } catch (Exception e) {
            fail(
                    "Unable to obtain method for invoke test: invokeInstanceTest");
        }
        try {
            ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (Exception e) {
            fail("Exception during invoke test : " + e.getMessage());
        }
        assertEquals("Invoke returned incorrect value", 1, ((Integer) ret)
                .intValue());

        // Get and attempt to invoke a private method
        try {
            mth = cl.getDeclaredMethod("privateInvokeTest", dcl);
        } catch (Exception e) {
            fail(
                    "Unable to obtain method for invoke test: privateInvokeTest");
        }
        try {
            ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (IllegalAccessException e) {
            // Correct behaviour
        } catch (Exception e) {
            fail("Exception during invoke test : " + e.getMessage());
        }
        // Generate an IllegalArgumentException
        try {
            mth = cl.getDeclaredMethod("invokeInstanceTest", dcl);
        } catch (Exception e) {
            fail(
                    "Unable to obtain method for invoke test: invokeInstanceTest");
        }
        try {
            Object[] args = { Object.class };
            ret = mth.invoke(new TestMethod(), args);
        } catch (IllegalArgumentException e) {
            // Correct behaviour
        } catch (Exception e) {
            fail("Exception during invoke test : " + e.getMessage());
        }

        // Generate a NullPointerException
        try {
            mth = cl.getDeclaredMethod("invokeInstanceTest", dcl);
        } catch (Exception e) {
            fail("Unable to obtain method invokeInstanceTest for invoke test : "
                    + e.getMessage());
        }
        try {
            ret = mth.invoke(null, new Object[0]);
        } catch (NullPointerException e) {
            // Correct behaviour
        } catch (Exception e) {
            fail("Exception during invoke test : " + e.getMessage());
        }

        // Generate an InvocationTargetException
        try {
            mth = cl.getDeclaredMethod("invokeExceptionTest", dcl);
        } catch (Exception e) {
            fail("Unable to obtain method invokeExceptionTest for invoke test: "
                    + e.getMessage());
        }
        try {
            ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (InvocationTargetException e) {
            // Correct behaviour
        } catch (Exception e) {
            fail("Exception during invoke test : " + e.getMessage());
        }

        TestMethod testMethod = new TestMethod();
        Method methods[] = cl.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("invokeCastTest1")) {
                Class param = methods[i].getParameterTypes()[0];

                try {
                    methods[i].invoke(testMethod, new Object[] { new Byte(
                            (byte) 1) });
                    assertTrue("invalid invoke with Byte: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Float.TYPE
                                    || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Byte invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Byte invalid failure: " + methods[i],
                            param == Boolean.TYPE || param == Character.TYPE);
                }

                try {
                    methods[i].invoke(testMethod, new Object[] { new Short(
                            (short) 1) });
                    assertTrue("invalid invoke with Short: " + methods[i],
                            param == Short.TYPE || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Float.TYPE
                                    || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Short invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Short invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Boolean.TYPE
                                    || param == Character.TYPE);
                }

                try {
                    methods[i].invoke(testMethod,
                            new Object[] { new Integer(1) });
                    assertTrue("invalid invoke with Integer: " + methods[i],
                            param == Integer.TYPE || param == Long.TYPE
                                    || param == Float.TYPE
                                    || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Integer invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Integer invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Boolean.TYPE
                                    || param == Character.TYPE);
                }

                try {
                    methods[i].invoke(testMethod, new Object[] { new Long(1) });
                    assertTrue("invalid invoke with Long: " + methods[i],
                            param == Long.TYPE || param == Float.TYPE
                                    || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Long invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Long invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Integer.TYPE
                                    || param == Boolean.TYPE
                                    || param == Character.TYPE);
                }

                try {
                    methods[i].invoke(testMethod, new Object[] { new Character(
                            'a') });
                    assertTrue("invalid invoke with Character: " + methods[i],
                            param == Character.TYPE || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Float.TYPE
                                    || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Character invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Character invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Boolean.TYPE);
                }

                try {
                    methods[i]
                            .invoke(testMethod, new Object[] { new Float(1) });
                    assertTrue("invalid invoke with Float: " + methods[i],
                            param == Float.TYPE || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Float invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Float invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Boolean.TYPE
                                    || param == Character.TYPE);
                }

                try {
                    methods[i].invoke(testMethod,
                            new Object[] { new Double(1) });
                    assertTrue("invalid invoke with Double: " + methods[i],
                            param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Double invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Double invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Boolean.TYPE
                                    || param == Character.TYPE
                                    || param == Float.TYPE);
                }

                try {
                    methods[i].invoke(testMethod, new Object[] { new Boolean(
                            true) });
                    assertTrue("invalid invoke with Boolean: " + methods[i],
                            param == Boolean.TYPE);
                } catch (Exception e) {
                    assertTrue("Boolean invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Boolean invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Character.TYPE
                                    || param == Float.TYPE
                                    || param == Double.TYPE);
                }
            }
        }
    }

    /**
     * @tests java.lang.reflect.Method#toString()
     */
    public void test_toString() {
        // Test for method java.lang.String java.lang.reflect.Method.toString()
        Method mth = null;
        Class[] parms = { int.class, short.class, String.class, boolean.class,
                Object.class, long.class, byte.class, char.class, double.class,
                float.class };
        try {

            mth = TestMethod.class.getDeclaredMethod("printTest", parms);
        } catch (Exception e) {
            fail("Exception during toString test : " + e.getMessage());
        }

        assertTrue(
                "Returned incorrect string for method: " + mth.toString(),
                mth
                        .toString()
                        .equals(
                                "public static final void tests.api.java.lang.reflect.MethodTest$TestMethod.printTest(int,short,java.lang.String,boolean,java.lang.Object,long,byte,char,double,float)"));
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
