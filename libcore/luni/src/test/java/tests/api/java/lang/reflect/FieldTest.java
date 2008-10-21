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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import tests.support.Support_Field;

public class FieldTest extends junit.framework.TestCase {

    // BEGIN android-note
    // This test had a couple of bugs in it. Some parts of the code were
    // unreachable before. Also some tests expected the wrong excpetions
    // to be thrown. This version has been validated to pass on a standard
    // JDK 1.5.
    // END android-note
    
    static class TestField {
        public static int pubfield1;

        protected static double doubleSField = Double.MAX_VALUE;

        private static int privfield1 = 123;

        protected int intField = Integer.MAX_VALUE;

        protected short shortField = Short.MAX_VALUE;

        protected boolean booleanField = true;

        protected byte byteField = Byte.MAX_VALUE;

        protected long longField = Long.MAX_VALUE;

        protected double doubleField = Double.MAX_VALUE;

        protected float floatField = Float.MAX_VALUE;

        protected char charField = 'T';

        protected final int intFField = Integer.MAX_VALUE;

        protected final short shortFField = Short.MAX_VALUE;

        protected final boolean booleanFField = true;

        protected final byte byteFField = Byte.MAX_VALUE;

        protected final long longFField = Long.MAX_VALUE;

        protected final double doubleFField = Double.MAX_VALUE;

        protected final float floatFField = Float.MAX_VALUE;

        protected final char charFField = 'T';

        private static final int x = 1;

        public volatile transient int y = 0;

        protected static transient volatile int prsttrvol = 99;
    }

    public class TestFieldSub1 extends TestField {
    }

    public class TestFieldSub2 extends TestField {
    }

    static class A {
        protected short shortField = Short.MAX_VALUE;
    }

    /**
     * @tests java.lang.reflect.Field#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean
        // java.lang.reflect.Field.equals(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        try {
            f = x.getClass().getDeclaredField("shortField");
        } catch (Exception e) {
            fail("Exception during getType test : " + e.getMessage());
        }
        try {
            assertTrue("Same Field returned false", f.equals(f));
            assertTrue("Inherited Field returned false", f.equals(x.getClass()
                    .getDeclaredField("shortField")));
            assertTrue("Identical Field from different class returned true", !f
                    .equals(A.class.getDeclaredField("shortField")));
        } catch (Exception e) {
            fail("Exception during getType test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Field#get(java.lang.Object)
     */
    public void test_getLjava_lang_Object() throws Throwable {
        // Test for method java.lang.Object
        // java.lang.reflect.Field.get(java.lang.Object)
        TestField x = new TestField();
        Field f = x.getClass().getDeclaredField("doubleField");
        Double val = (Double) f.get(x);

        assertTrue("Returned incorrect double field value",
                val.doubleValue() == Double.MAX_VALUE);
        // Test getting a static field;
        f = x.getClass().getDeclaredField("doubleSField");
        f.set(x, new Double(1.0));
        val = (Double) f.get(x);
        assertEquals("Returned incorrect double field value", 1.0, val
                .doubleValue());

        // Try a get on a private field
        try {
            f = TestAccess.class.getDeclaredField("xxx");
            assertNotNull(f);
            f.get(null);
            fail("No expected IllegalAccessException");
        } catch (IllegalAccessException ok) {}
        
        // Try a get on a private field in nested member
        // temporarily commented because it breaks J9 VM
        // Regression for HARMONY-1309
        //f = x.getClass().getDeclaredField("privfield1");
        //assertEquals(x.privfield1, f.get(x));

        // Try a get using an invalid class.
        try {
            f = x.getClass().getDeclaredField("doubleField");
            f.get(new String());
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException exc) {
            // Correct - Passed an Object that does not declare or inherit f
        }
    }

    class SupportSubClass extends Support_Field {

        Object getField(char primitiveType, Object o, Field f,
                Class expectedException) {
            Object res = null;
            try {
                primitiveType = Character.toUpperCase(primitiveType);
                switch (primitiveType) {
                case 'I': // int
                    res = new Integer(f.getInt(o));
                    break;
                case 'J': // long
                    res = new Long(f.getLong(o));
                    break;
                case 'Z': // boolean
                    res = new Boolean(f.getBoolean(o));
                    break;
                case 'S': // short
                    res = new Short(f.getShort(o));
                    break;
                case 'B': // byte
                    res = new Byte(f.getByte(o));
                    break;
                case 'C': // char
                    res = new Character(f.getChar(o));
                    break;
                case 'D': // double
                    res = new Double(f.getDouble(o));
                    break;
                case 'F': // float
                    res = new Float(f.getFloat(o));
                    break;
                default:
                    res = f.get(o);
                }
                if (expectedException != null) {
                    fail("expected exception " + expectedException.getName());
                }
            } catch (Exception e) {
                if (expectedException == null) {
                    fail("unexpected exception " + e);
                } else {
                    assertTrue("expected exception "
                            + expectedException.getName() + " and got " + e, e
                            .getClass().equals(expectedException));
                }
            }
            return res;
        }

        void setField(char primitiveType, Object o, Field f,
                Class expectedException, Object value) {
            try {
                primitiveType = Character.toUpperCase(primitiveType);
                switch (primitiveType) {
                case 'I': // int
                    f.setInt(o, ((Integer) value).intValue());
                    break;
                case 'J': // long
                    f.setLong(o, ((Long) value).longValue());
                    break;
                case 'Z': // boolean
                    f.setBoolean(o, ((Boolean) value).booleanValue());
                    break;
                case 'S': // short
                    f.setShort(o, ((Short) value).shortValue());
                    break;
                case 'B': // byte
                    f.setByte(o, ((Byte) value).byteValue());
                    break;
                case 'C': // char
                    f.setChar(o, ((Character) value).charValue());
                    break;
                case 'D': // double
                    f.setDouble(o, ((Double) value).doubleValue());
                    break;
                case 'F': // float
                    f.setFloat(o, ((Float) value).floatValue());
                    break;
                default:
                    f.set(o, value);
                }
                if (expectedException != null) {
                    fail("expected exception " + expectedException.getName() + " for field " + f.getName() + ", value " + value);
                }
            } catch (Exception e) {
                if (expectedException == null) {
                    fail("unexpected exception " + e + " for field " + f.getName() + ", value " + value);
                } else {
                    assertTrue("expected exception "
                            + expectedException.getName() + " and got " + e + " for field " + f.getName() + ", value " + value, e
                            .getClass().equals(expectedException));
                }
            }
        }
    }

    /**
     * @tests java.lang.reflect.Field#get(java.lang.Object)
     * @tests java.lang.reflect.Field#getByte(java.lang.Object)
     * @tests java.lang.reflect.Field#getBoolean(java.lang.Object)
     * @tests java.lang.reflect.Field#getShort(java.lang.Object)
     * @tests java.lang.reflect.Field#getInt(java.lang.Object)
     * @tests java.lang.reflect.Field#getLong(java.lang.Object)
     * @tests java.lang.reflect.Field#getFloat(java.lang.Object)
     * @tests java.lang.reflect.Field#getDouble(java.lang.Object)
     * @tests java.lang.reflect.Field#getChar(java.lang.Object)
     * @tests java.lang.reflect.Field#set(java.lang.Object, java.lang.Object)
     * @tests java.lang.reflect.Field#setByte(java.lang.Object, byte)
     * @tests java.lang.reflect.Field#setBoolean(java.lang.Object, boolean)
     * @tests java.lang.reflect.Field#setShort(java.lang.Object, short)
     * @tests java.lang.reflect.Field#setInt(java.lang.Object, int)
     * @tests java.lang.reflect.Field#setLong(java.lang.Object, long)
     * @tests java.lang.reflect.Field#setFloat(java.lang.Object, float)
     * @tests java.lang.reflect.Field#setDouble(java.lang.Object, double)
     * @tests java.lang.reflect.Field#setChar(java.lang.Object, char)
     */
    public void testProtectedFieldAccess() {
        Class fieldClass = new Support_Field().getClass();
        String fieldName = null;
        Field objectField = null;
        Field booleanField = null;
        Field byteField = null;
        Field charField = null;
        Field shortField = null;
        Field intField = null;
        Field longField = null;
        Field floatField = null;
        Field doubleField = null;
        try {
            fieldName = "objectField";
            objectField = fieldClass.getDeclaredField(fieldName);

            fieldName = "booleanField";
            booleanField = fieldClass.getDeclaredField(fieldName);

            fieldName = "byteField";
            byteField = fieldClass.getDeclaredField(fieldName);

            fieldName = "charField";
            charField = fieldClass.getDeclaredField(fieldName);

            fieldName = "shortField";
            shortField = fieldClass.getDeclaredField(fieldName);

            fieldName = "intField";
            intField = fieldClass.getDeclaredField(fieldName);

            fieldName = "longField";
            longField = fieldClass.getDeclaredField(fieldName);

            fieldName = "floatField";
            floatField = fieldClass.getDeclaredField(fieldName);

            fieldName = "doubleField";
            doubleField = fieldClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            fail("missing field " + fieldName + " in test support class "
                    + fieldClass.getName());
        }

        // create the various objects that might or might not have an instance
        // of the field
        Support_Field parentClass = new Support_Field();
        SupportSubClass subclass = new SupportSubClass();
        SupportSubClass otherSubclass = new SupportSubClass();
        Object plainObject = new Object();

        Class illegalAccessExceptionClass = new IllegalAccessException()
                .getClass();
        Class illegalArgumentExceptionClass = new IllegalArgumentException()
                .getClass();

        // The test will attempt to use pass an object to set for object, byte,
        // short, ..., float and double fields
        // and pass a byte to to setByte for byte, short, ..., float and double
        // fields and so on.
        // It will also test if IllegalArgumentException is thrown when the
        // field does not exist in the given object and that
        // IllegalAccessException is thrown when trying to access an
        // inaccessible protected field.
        // The test will also check that IllegalArgumentException is thrown for
        // all other attempts.

        // Ordered by widening conversion, except for 'L' at the beg (which
        // stands for Object).
        // If the object provided to set can be unwrapped to a primitive, then
        // the set method can set
        // primitive fields.
        char types[] = { 'L', 'B', 'S', 'C', 'I', 'J', 'F', 'D' };
        Field fields[] = { objectField, byteField, shortField, charField,
                intField, longField, floatField, doubleField };
        Object values[] = { new Byte((byte) 1), new Byte((byte) 1),
                new Short((short) 1), new Character((char) 1), new Integer(1),
                new Long(1), new Float(1), new Double(1) };

        // test set methods
        for (int i = 0; i < types.length; i++) {
            char type = types[i];
            Object value = values[i];
            for (int j = i; j < fields.length; j++) {
                Field field = fields[j];
                fieldName = field.getName();
                
                if (field == charField && type != 'C') {
                    // the exception is that bytes and shorts CANNOT be
                    // converted into chars even though chars CAN be
                    // converted into ints, longs, floats and doubles
                    subclass.setField(type, subclass, field,
                            illegalArgumentExceptionClass, value);
                } else {
                    // setting type into field);
                    subclass.setField(type, subclass, field, null, value);
                    subclass.setField(type, otherSubclass, field, null, value);
                    subclass.setField(type, parentClass, field,
                            illegalAccessExceptionClass, value);
                    subclass.setField(type, plainObject, field,
                            // Failed on JDK.
                            illegalAccessExceptionClass, value);
                }
            }
            for (int j = 0; j < i; j++) {
                Field field = fields[j];
                fieldName = field.getName();
                // not setting type into field);
                subclass.setField(type, subclass, field,
                        illegalArgumentExceptionClass, value);
            }
        }

        // test setBoolean
        Boolean booleanValue = Boolean.TRUE;
        subclass.setField('Z', subclass, booleanField, null, booleanValue);
        subclass.setField('Z', otherSubclass, booleanField, null, booleanValue);
        subclass.setField('Z', parentClass, booleanField,
                illegalAccessExceptionClass, booleanValue);
        subclass.setField('Z', plainObject, booleanField,
                // Failed on JDK
                illegalAccessExceptionClass, booleanValue);
        for (int j = 0; j < fields.length; j++) {
            Field listedField = fields[j];
            fieldName = listedField.getName();
            // not setting boolean into listedField
            subclass.setField('Z', subclass, listedField,
                    illegalArgumentExceptionClass, booleanValue);
        }
        for (int i = 0; i < types.length; i++) {
            char type = types[i];
            Object value = values[i];
            subclass.setField(type, subclass, booleanField,
                    illegalArgumentExceptionClass, value);
        }

        // We perform the analagous test on the get methods.

        // ordered by widening conversion, except for 'L' at the end (which
        // stands for Object), to which all primitives can be converted by
        // wrapping
        char newTypes[] = new char[] { 'B', 'S', 'C', 'I', 'J', 'F', 'D', 'L' };
        Field newFields[] = { byteField, shortField, charField, intField,
                longField, floatField, doubleField, objectField };
        fields = newFields;
        types = newTypes;
        // test get methods
        for (int i = 0; i < types.length; i++) {
            char type = types[i];
            for (int j = 0; j <= i; j++) {
                Field field = fields[j];
                fieldName = field.getName();
                if (type == 'C' && field != charField) {
                    // the exception is that bytes and shorts CANNOT be
                    // converted into chars even though chars CAN be
                    // converted into ints, longs, floats and doubles
                    subclass.getField(type, subclass, field,
                            illegalArgumentExceptionClass);
                } else {
                    // getting type from field
                    subclass.getField(type, subclass, field, null);
                    subclass.getField(type, otherSubclass, field, null);
                    subclass.getField(type, parentClass, field,
                            illegalAccessExceptionClass);
                    subclass.getField(type, plainObject, field,
                            illegalAccessExceptionClass);
                }
            }
            for (int j = i + 1; j < fields.length; j++) {
                Field field = fields[j];
                fieldName = field.getName();
                subclass.getField(type, subclass, field,
                        illegalArgumentExceptionClass);
            }
        }

        // test getBoolean
        subclass.getField('Z', subclass, booleanField, null);
        subclass.getField('Z', otherSubclass, booleanField, null);
        subclass.getField('Z', parentClass, booleanField,
                illegalAccessExceptionClass);
        subclass.getField('Z', plainObject, booleanField,
                illegalAccessExceptionClass);
        for (int j = 0; j < fields.length; j++) {
            Field listedField = fields[j];
            fieldName = listedField.getName();
            // not getting boolean from listedField
            subclass.getField('Z', subclass, listedField,
                    illegalArgumentExceptionClass);
        }
        for (int i = 0; i < types.length - 1; i++) {
            char type = types[i];
            subclass.getField(type, subclass, booleanField,
                    illegalArgumentExceptionClass);
        }
        Object res = subclass.getField('L', subclass, booleanField, null);
        assertTrue("unexpected object " + res, res instanceof Boolean);
    }

    /**
     * @tests java.lang.reflect.Field#getBoolean(java.lang.Object)
     */
    public void test_getBooleanLjava_lang_Object() {
        // Test for method boolean
        // java.lang.reflect.Field.getBoolean(java.lang.Object)

        TestField x = new TestField();
        Field f = null;
        boolean val = false;
        try {
            f = x.getClass().getDeclaredField("booleanField");
            val = f.getBoolean(x);

        } catch (Exception e) {
            fail("Exception during getBoolean test: " + e.toString());
        }
        assertTrue("Returned incorrect boolean field value", val);
        try {
            try {
                f = x.getClass().getDeclaredField("doubleField");
                f.getBoolean(x);
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since doubleField is not a
                // boolean type
                return;
            }
        } catch (Exception e) {
            fail("Exception during getBoolean test: " + e.toString());
        }
        fail("Accessed field of invalid type");
    }

    /**
     * @tests java.lang.reflect.Field#getByte(java.lang.Object)
     */
    public void test_getByteLjava_lang_Object() {
        // Test for method byte
        // java.lang.reflect.Field.getByte(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        byte val = 0;
        try {
            f = x.getClass().getDeclaredField("byteField");
            val = f.getByte(x);
        } catch (Exception e) {
            fail("Exception during getbyte test : " + e.getMessage());
        }
        assertTrue("Returned incorrect byte field value", val == Byte.MAX_VALUE);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.getByte(x);
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since byteField is not a
                // boolean type
                return;
            }
        } catch (Exception e) {
            fail("Exception during getbyte test : " + e.getMessage());
        }
        fail("Accessed field of invalid type");
    }

    /**
     * @tests java.lang.reflect.Field#getChar(java.lang.Object)
     */
    public void test_getCharLjava_lang_Object() {
        // Test for method char
        // java.lang.reflect.Field.getChar(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        char val = 0;
        try {
            f = x.getClass().getDeclaredField("charField");
            val = f.getChar(x);
        } catch (Exception e) {
            fail("Exception during getCharacter test: " + e.toString());
        }
        assertEquals("Returned incorrect char field value", 'T', val);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.getChar(x);
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since charField is not a
                // boolean type
                return;
            }
        } catch (Exception e) {
            fail("Exception during getchar test : " + e.getMessage());
        }
        fail("Accessed field of invalid type");
    }

    /**
     * @tests java.lang.reflect.Field#getDeclaringClass()
     */
    public void test_getDeclaringClass() {
        // Test for method java.lang.Class
        // java.lang.reflect.Field.getDeclaringClass()
        Field[] fields;

        try {
            fields = new TestField().getClass().getFields();
            assertTrue("Returned incorrect declaring class", fields[0]
                    .getDeclaringClass().equals(new TestField().getClass()));

            // Check the case where the field is inherited to be sure the parent
            // is returned as the declarator
            fields = new TestFieldSub1().getClass().getFields();
            assertTrue("Returned incorrect declaring class", fields[0]
                    .getDeclaringClass().equals(new TestField().getClass()));
        } catch (Exception e) {
            fail("Exception : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Field#getDouble(java.lang.Object)
     */
    public void test_getDoubleLjava_lang_Object() {
        // Test for method double
        // java.lang.reflect.Field.getDouble(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        double val = 0.0;
        try {
            f = x.getClass().getDeclaredField("doubleField");
            val = f.getDouble(x);
        } catch (Exception e) {
            fail("Exception during getDouble test: " + e.toString());
        }
        assertTrue("Returned incorrect double field value",
                val == Double.MAX_VALUE);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.getDouble(x);
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since doubleField is not a
                // boolean type
                return;
            }
        } catch (Exception e) {
            fail("Exception during getDouble test: " + e.toString());
        }
        fail("Accessed field of invalid type");
    }

    /**
     * @tests java.lang.reflect.Field#getFloat(java.lang.Object)
     */
    public void test_getFloatLjava_lang_Object() {
        // Test for method float
        // java.lang.reflect.Field.getFloat(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        float val = 0;
        try {
            f = x.getClass().getDeclaredField("floatField");
            val = f.getFloat(x);
        } catch (Exception e) {
            fail("Exception during getFloat test : " + e.getMessage());
        }
        assertTrue("Returned incorrect float field value",
                val == Float.MAX_VALUE);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.getFloat(x);
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since floatField is not a
                // boolean type
                return;
            }
        } catch (Exception e) {
            fail("Exception during getfloat test : " + e.getMessage());
        }
        fail("Accessed field of invalid type");
    }

    /**
     * @tests java.lang.reflect.Field#getInt(java.lang.Object)
     */
    public void test_getIntLjava_lang_Object() {
        // Test for method int java.lang.reflect.Field.getInt(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        int val = 0;
        try {
            f = x.getClass().getDeclaredField("intField");
            val = f.getInt(x);
        } catch (Exception e) {
            fail("Exception during getInt test : " + e.getMessage());
        }
        assertTrue("Returned incorrect Int field value",
                val == Integer.MAX_VALUE);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.getInt(x);
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since IntField is not a
                // boolean type
                return;
            }
        } catch (Exception e) {
            fail("Exception during getInt test : " + e.getMessage());
        }
        fail("Accessed field of invalid type");
    }

    /**
     * @tests java.lang.reflect.Field#getLong(java.lang.Object)
     */
    public void test_getLongLjava_lang_Object() {
        // Test for method long
        // java.lang.reflect.Field.getLong(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        long val = 0;
        try {
            f = x.getClass().getDeclaredField("longField");
            val = f.getLong(x);
        } catch (Exception e) {
            fail("Exception during getLong test : " + e.getMessage());
        }
        assertTrue("Returned incorrect long field value", val == Long.MAX_VALUE);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.getLong(x);
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // long type
                return;
            }
        } catch (Exception e) {
            fail("Exception during getlong test : " + e.getMessage());
        }
        fail("Accessed field of invalid type");
    }

    /**
     * @tests java.lang.reflect.Field#getModifiers()
     */
    public void test_getModifiers() {
        // Test for method int java.lang.reflect.Field.getModifiers()
        TestField x = new TestField();
        Field f = null;
        try {
            f = x.getClass().getDeclaredField("prsttrvol");
        } catch (Exception e) {
            fail("Exception during getModifiers test: " + e.toString());
        }
        int mod = f.getModifiers();
        int mask = (Modifier.PROTECTED | Modifier.STATIC)
                | (Modifier.TRANSIENT | Modifier.VOLATILE);
        int nmask = (Modifier.PUBLIC | Modifier.NATIVE);
        assertTrue("Returned incorrect field modifiers: ",
                ((mod & mask) == mask) && ((mod & nmask) == 0));
    }

    /**
     * @tests java.lang.reflect.Field#getName()
     */
    public void test_getName() {
        // Test for method java.lang.String java.lang.reflect.Field.getName()
        TestField x = new TestField();
        Field f = null;
        try {
            f = x.getClass().getDeclaredField("shortField");
        } catch (Exception e) {
            fail("Exception during getType test : " + e.getMessage());
        }
        assertEquals("Returned incorrect field name", 
                "shortField", f.getName());
    }

    /**
     * @tests java.lang.reflect.Field#getShort(java.lang.Object)
     */
    public void test_getShortLjava_lang_Object() {
        // Test for method short
        // java.lang.reflect.Field.getShort(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        short val = 0;
        ;
        try {
            f = x.getClass().getDeclaredField("shortField");
            val = f.getShort(x);
        } catch (Exception e) {
            fail("Exception during getShort test : " + e.getMessage());
        }
        assertTrue("Returned incorrect short field value",
                val == Short.MAX_VALUE);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.getShort(x);
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // short type
                return;
            }
        } catch (Exception e) {
            fail("Exception during getshort test : " + e.getMessage());
        }
        fail("Accessed field of invalid type");
    }

    /**
     * @tests java.lang.reflect.Field#getType()
     */
    public void test_getType() {
        // Test for method java.lang.Class java.lang.reflect.Field.getType()
        TestField x = new TestField();
        Field f = null;
        try {
            f = x.getClass().getDeclaredField("shortField");
        } catch (Exception e) {
            fail("Exception during getType test : " + e.getMessage());
        }
        assertTrue("Returned incorrect field type: " + f.getType().toString(),
                f.getType().equals(short.class));
    }

    /**
     * @tests java.lang.reflect.Field#set(java.lang.Object, java.lang.Object)
     */
    public void test_setLjava_lang_ObjectLjava_lang_Object() {
        // Test for method void java.lang.reflect.Field.set(java.lang.Object,
        // java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        double val = 0.0;
        try {
            f = x.getClass().getDeclaredField("doubleField");
            f.set(x, new Double(1.0));
            val = f.getDouble(x);
        } catch (Exception e) {
            fail("Exception during set test : " + e.getMessage());
        }
        assertEquals("Returned incorrect double field value", 1.0, val);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.set(x, new Double(1.0));
                fail("Accessed field of invalid type");
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // double type
            }
            try {
                f = x.getClass().getDeclaredField("doubleFField");
                f.set(x, new Double(1.0));
                fail("Accessed field of invalid type");
            } catch (IllegalAccessException ex) {
                // Good, Exception should be thrown since doubleFField is
                // declared as final
            }
            // Test setting a static field;
            f = x.getClass().getDeclaredField("doubleSField");
            f.set(x, new Double(1.0));
            val = f.getDouble(x);
            assertEquals("Returned incorrect double field value", 1.0, val);
        } catch (Exception e) {
            fail("Exception during setDouble test: " + e.toString());
        }
    }

    /**
     * @tests java.lang.reflect.Field#setBoolean(java.lang.Object, boolean)
     */
    public void test_setBooleanLjava_lang_ObjectZ() {
        // Test for method void
        // java.lang.reflect.Field.setBoolean(java.lang.Object, boolean)
        TestField x = new TestField();
        Field f = null;
        boolean val = false;
        try {
            f = x.getClass().getDeclaredField("booleanField");
            f.setBoolean(x, false);
            val = f.getBoolean(x);
        } catch (Exception e) {
            fail("Exception during setboolean test: " + e.toString());
        }
        assertTrue("Returned incorrect float field value", !val);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.setInt(x, 42);
                fail("Accessed field of invalid type");
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // int type
            }

            try {
                f = x.getClass().getDeclaredField("booleanFField");
                f.setBoolean(x, true);
                fail("Accessed field of invalid type");
            } catch (IllegalAccessException ex) {
                // Good, Exception should be thrown since booleanField is
                // declared as final
            }
        } catch (Exception e) {
            fail("Exception during setboolean test: " + e.toString());
        }
    }

    /**
     * @tests java.lang.reflect.Field#setByte(java.lang.Object, byte)
     */
    public void test_setByteLjava_lang_ObjectB() {
        // Test for method void
        // java.lang.reflect.Field.setByte(java.lang.Object, byte)
        TestField x = new TestField();
        Field f = null;
        byte val = 0;
        try {
            f = x.getClass().getDeclaredField("byteField");
            f.setByte(x, (byte) 1);
            val = f.getByte(x);
        } catch (Exception e) {
            fail("Exception during setByte test : " + e.getMessage());
        }
        assertEquals("Returned incorrect float field value", 1, val);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.setByte(x, (byte) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // byte type
            }

            try {
                f = x.getClass().getDeclaredField("byteFField");
                f.setByte(x, (byte) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalAccessException ex) {
                // Good, Exception should be thrown since byteFField is declared
                // as final
            }
        } catch (Exception e) {
            fail("Exception during setByte test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Field#setChar(java.lang.Object, char)
     */
    public void test_setCharLjava_lang_ObjectC() {
        // Test for method void
        // java.lang.reflect.Field.setChar(java.lang.Object, char)
        TestField x = new TestField();
        Field f = null;
        char val = 0;
        try {
            f = x.getClass().getDeclaredField("charField");
            f.setChar(x, (char) 1);
            val = f.getChar(x);
        } catch (Exception e) {
            fail("Exception during setChar test : " + e.getMessage());
        }
        assertEquals("Returned incorrect float field value", 1, val);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.setChar(x, (char) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // char type
            }

            try {
                f = x.getClass().getDeclaredField("charFField");
                f.setChar(x, (char) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalAccessException ex) {
                // Good, Exception should be thrown since charFField is declared
                // as final
            }
        } catch (Exception e) {
            fail("Exception during setChar test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Field#setDouble(java.lang.Object, double)
     */
    public void test_setDoubleLjava_lang_ObjectD() {
        // Test for method void
        // java.lang.reflect.Field.setDouble(java.lang.Object, double)
        TestField x = new TestField();
        Field f = null;
        double val = 0.0;
        try {
            f = x.getClass().getDeclaredField("doubleField");
            f.setDouble(x, 1.0);
            val = f.getDouble(x);
        } catch (Exception e) {
            fail("Exception during setDouble test: " + e.toString());
        }
        assertEquals("Returned incorrect double field value", 1.0, val);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.setDouble(x, 1.0);
                fail("Accessed field of invalid type");
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // double type
            }

            try {
                f = x.getClass().getDeclaredField("doubleFField");
                f.setDouble(x, 1.0);
                fail("Accessed field of invalid type");
            } catch (IllegalAccessException ex) {
                // Good, Exception should be thrown since doubleFField is
                // declared as final
            }
        } catch (Exception e) {
            fail("Exception during setDouble test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Field#setFloat(java.lang.Object, float)
     */
    public void test_setFloatLjava_lang_ObjectF() {
        // Test for method void
        // java.lang.reflect.Field.setFloat(java.lang.Object, float)
        TestField x = new TestField();
        Field f = null;
        float val = 0.0F;
        try {
            f = x.getClass().getDeclaredField("floatField");
            f.setFloat(x, (float) 1);
            val = f.getFloat(x);
        } catch (Exception e) {
            fail("Exception during setFloat test : " + e.getMessage());
        }
        assertEquals("Returned incorrect float field value", 1.0, val, 0.0);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.setFloat(x, (float) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // float type
            }
            try {
                f = x.getClass().getDeclaredField("floatFField");
                f.setFloat(x, (float) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalAccessException ex) {
                // Good, Exception should be thrown since floatFField is
                // declared as final
            }
        } catch (Exception e) {
            fail("Exception during setFloat test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Field#setInt(java.lang.Object, int)
     */
    public void test_setIntLjava_lang_ObjectI() {
        // Test for method void java.lang.reflect.Field.setInt(java.lang.Object,
        // int)
        TestField x = new TestField();
        Field f = null;
        int val = 0;
        try {
            f = x.getClass().getDeclaredField("intField");
            f.setInt(x, (int) 1);
            val = f.getInt(x);
        } catch (Exception e) {
            fail("Exception during setInteger test: " + e.toString());
        }
        assertEquals("Returned incorrect int field value", 1, val);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.setInt(x, (int) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // int type
            }
            try {
                f = x.getClass().getDeclaredField("intFField");
                f.setInt(x, (int) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalAccessException ex) {
                // Good, Exception should be thrown since intFField is declared
                // as final
            }
        } catch (Exception e) {
            fail("Exception during setInteger test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Field#setLong(java.lang.Object, long)
     */
    public void test_setLongLjava_lang_ObjectJ() {
        // Test for method void
        // java.lang.reflect.Field.setLong(java.lang.Object, long)
        TestField x = new TestField();
        Field f = null;
        long val = 0L;
        try {
            f = x.getClass().getDeclaredField("longField");
            f.setLong(x, (long) 1);
            val = f.getLong(x);
        } catch (Exception e) {
            fail("Exception during setLong test : " + e.getMessage());
        }
        assertEquals("Returned incorrect long field value", 1, val);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.setLong(x, (long) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // long type
            }
            try {
                f = x.getClass().getDeclaredField("longFField");
                f.setLong(x, (long) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalAccessException ex) {
                // Good, Exception should be thrown since longFField is declared
                // as final
            }
        } catch (Exception e) {
            fail("Exception during setLong test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Field#setShort(java.lang.Object, short)
     */
    public void test_setShortLjava_lang_ObjectS() {
        // Test for method void
        // java.lang.reflect.Field.setShort(java.lang.Object, short)
        TestField x = new TestField();
        Field f = null;
        short val = 0;
        try {
            f = x.getClass().getDeclaredField("shortField");
            f.setShort(x, (short) 1);
            val = f.getShort(x);
        } catch (Exception e) {
            fail("Exception during setShort test : " + e.getMessage());
        }
        assertEquals("Returned incorrect short field value", 1, val);
        try {
            try {
                f = x.getClass().getDeclaredField("booleanField");
                f.setShort(x, (short) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalArgumentException ex) {
                // Good, Exception should be thrown since booleanField is not a
                // short type
            }
            try {
                f = x.getClass().getDeclaredField("shortFField");
                f.setShort(x, (short) 1);
                fail("Accessed field of invalid type");
            } catch (IllegalAccessException ex) {
                // Good, Exception should be thrown since shortFField is
                // declared as final
            }
        } catch (Exception e) {
            fail("Exception during setShort test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.Field#toString()
     */
    public void test_toString() {
        // Test for method java.lang.String java.lang.reflect.Field.toString()
        Field f = null;

        try {
            f = TestField.class.getDeclaredField("x");
        } catch (Exception e) {
            fail("Exception getting field : " + e.getMessage());
        }
        assertEquals("Field returned incorrect string",
                "private static final int tests.api.java.lang.reflect.FieldTest$TestField.x",
                        f.toString());
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

class TestAccess {
    private static int xxx;
}
