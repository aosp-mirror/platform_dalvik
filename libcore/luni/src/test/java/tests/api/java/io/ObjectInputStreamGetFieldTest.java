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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass; 

import java.io.ObjectInputStream;

import tests.support.Support_GetPutFields;
import tests.support.Support_GetPutFieldsDefaulted;


/**
 * Tests the methods of {@code ObjectInputStream.GetField}. Three things make
 * this class somewhat difficult to test:
 * <ol>
 * <li>It is a completely abstract class; none of the methods is implemented in
 * {@code ObjectInputStream.GetField}.</li>
 * <li>There is no public class that implements
 * {@code ObjectInputStream.GetField}. The only way to get an implementation is
 * by calling {@code ObjectInputStream.getFields()}.</li>
 * <li>Invoking {@code ObjectOutputStream.getFields()} only works from within
 * the private {@code readObject(ObjectInputStream)} method of a class that
 * implements {@code Serializable}; an exception is thrown otherwise.</li>
 * </ol>
 * <p>
 * Given these restrictions, an indirect approach is used to test
 * {@code ObjectInputStream.GetField}: Three serializable helper classes in
 * package {@code tests.support} ({@code Support_GetPutFields},
 * {@code Support_GetPutFieldsDeprecated} and
 * {@code Support_GetPutFieldsDefaulted}) implement
 * {@code readObject(ObjectInputStream)} to read data from an input stream. 
 * This input stream in turn reads from one of the corresponding files 
 * ({@code testFields.ser}, {@code testFieldsDeprecated.ser} and 
 * {@code testFieldsDefaulted.ser}) that have been created with 
 * {@code tests.util.FieldTestFileGenerator} on a reference platform.
 * </p>
 * <p>
 * The test method in this class expects to find the reference files as a
 * resource stored at {@code tests/api/java/io}.
 * </p>
 */
@TestTargetClass(ObjectInputStream.GetField.class) 
public class ObjectInputStreamGetFieldTest extends junit.framework.TestCase {

    private ObjectInputStream ois = null;
    
    private final String FILENAME = 
            "/tests/api/java/io/testFields.ser";
    private final String DEFAULTED_FILENAME = 
            "/tests/api/java/io/testFieldsDefaulted.ser";

    public boolean booleanValue;
    public byte byteValue;
    public char charValue;
    public int intValue;
    

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, byte.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, char.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with valid arguments.",
            method = "get",
            args = {java.lang.String.class, short.class}
        )
    })
    public void test_get() throws Exception {
        
        initOis(FILENAME);
        Support_GetPutFields object = (Support_GetPutFields) ois.readObject();
        Support_GetPutFields newObject = new Support_GetPutFields();
        newObject.initTestValues();
        
        assertTrue("Test 1: The object read from the reference file does " + 
                   "not match a locally created instance of the same class.", 
                   object.equals(newObject));
        
        initOis(DEFAULTED_FILENAME);
        Support_GetPutFieldsDefaulted defaulted = 
                (Support_GetPutFieldsDefaulted) ois.readObject();
        Support_GetPutFieldsDefaulted newDefaulted = 
                new Support_GetPutFieldsDefaulted();
        newDefaulted.initTestValues();
        
        assertTrue("Test 2: The object read from the reference file does " + 
                   "not match a locally created instance of the same class.", 
                   defaulted.equals(newDefaulted));
        
        // Executing the same procedure against the file created with the
        // deprecated ObjectOutputStream.PutFields.write(ObjectOutput) method
        // is not possible since there is no corresponding read(ObjectInput)
        // method. When trying to do it as in tests 1 and 2, a 
        // NullPointerException is thrown.
    }
   
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies defaulted(String).",
        method = "defaulted",
        args = {java.lang.String.class}
    )
    public void test_defaultedLjava_lang_String() throws Exception {

        initOis(FILENAME);
        Support_GetPutFields object = (Support_GetPutFields) ois.readObject();
        ObjectInputStream.GetField fields = object.getField;
        
        try {
            fields.defaulted("noField");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {}

        assertFalse("The field longValue should not be defaulted.", 
                   fields.defaulted("longValue"));

        // Now the same with defaulted fields.
        initOis(DEFAULTED_FILENAME);
        Support_GetPutFieldsDefaulted defaultedObject = 
            (Support_GetPutFieldsDefaulted) ois.readObject();
        fields = defaultedObject.getField;
        
        assertTrue("The field longValue should be defaulted.", 
                   fields.defaulted("longValue"));
        
    }
    
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, byte.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, char.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the get(String, X) methods with invalid arguments.",
            method = "get",
            args = {java.lang.String.class, short.class}
        )
    })
    public void test_getException() throws Exception {

        initOis(FILENAME);
        Support_GetPutFields object = (Support_GetPutFields) ois.readObject();
        ObjectInputStream.GetField fields = object.getField;
        
        // Methods called with invalid field name.
        try {
            fields.get("noValue", false);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, boolean).");
        } catch (IllegalArgumentException e) {}
        
        try {
            fields.get("noValue", (byte) 0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, byte).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("noValue", (char) 0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, char).");
        } catch (IllegalArgumentException e) {}
        
        try {
            fields.get("noValue", 0.0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, double).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("noValue", 0.0f);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, float).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("noValue", (long) 0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, long).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("noValue", 0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, int).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("noValue", new Object());
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, Object).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("noValue", (short) 0);
            fail("IllegalArgumentException expected for not existing name " +
                 "argument in get(String, short).");
        } catch (IllegalArgumentException e) {}

        // Methods called with correct field name but non-matching type.
        try {
            fields.get("byteValue", false);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, boolean).");
        } catch (IllegalArgumentException e) {}
        
        try {
            fields.get("booleanValue", (byte) 0);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, byte).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("intValue", (char) 0);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, char).");
        } catch (IllegalArgumentException e) {}
        
        try {
            fields.get("floatValue", 0.0);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, double).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("doubleValue", 0.0f);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, float).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("intValue", (long) 0);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, long).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("shortValue", 0);
            fail("IllegalArgumentException expected for non-matching name " +
                 "and type arguments in get(String, int).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("booleanValue", new Object());
            fail("IllegalArgumentException expected for non-matching name " + 
                 "and type arguments in get(String, Object).");
        } catch (IllegalArgumentException e) {}

        try {
            fields.get("longValue", (short) 0);
            fail("IllegalArgumentException expected for non-matching name " + 
                 "and type arguments in get(String, short).");
        } catch (IllegalArgumentException e) {}
    }
    
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that getObjectStreamClass() does not return null.",
        method = "getObjectStreamClass",
        args = {}
    )
    public void test_getObjectStreamClass() throws Exception {
        
        initOis(FILENAME);
        Support_GetPutFields object = (Support_GetPutFields) ois.readObject();
        assertNotNull("Return value of getObjectStreamClass() should not be null.",
                      object.getField.getObjectStreamClass());
    }
    
    
    private void initOis(String fileName) throws Exception {
        if (ois != null) {
            ois.close();
        }
        ois = new ObjectInputStream(
                    getClass().getResourceAsStream(fileName));
    }
    
    protected void tearDown() throws Exception {
        
        if (ois != null) {
            ois.close();
        }
        super.tearDown();
    }

}
