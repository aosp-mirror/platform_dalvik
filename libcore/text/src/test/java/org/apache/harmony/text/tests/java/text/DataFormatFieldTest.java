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

package org.apache.harmony.text.tests.java.text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.text.DateFormat;
import java.text.DateFormat.Field;
import java.util.Calendar;

import junit.framework.TestCase;

public class DataFormatFieldTest extends TestCase {

    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        // Regression for HARMONY-178
        MyField field = new MyField("day of month", Calendar.ERA);

        assertEquals("field has wrong name", "day of month", field.getName());
        assertEquals("field has wrong Calendar field number", Calendar.ERA,
                field.getCalendarField());

        DateFormat.Field realField = DateFormat.Field
                .ofCalendarField(Calendar.ERA);
        assertSame("Modified calendar field with the same field number",
                DateFormat.Field.ERA, realField);

        DateFormat.Field realField2 = DateFormat.Field
                .ofCalendarField(Calendar.DAY_OF_MONTH);
        assertSame("Modified calendar field with the same field number",
                DateFormat.Field.DAY_OF_MONTH, realField2);
    }

    static class MyField extends DateFormat.Field {
        private static final long serialVersionUID = 1L;

        protected MyField(String fieldName, int calendarField) {
            super(fieldName, calendarField);
        }

        protected String getName() {
            return super.getName();
        }
    }

    /**
     * @tests java.text.DateFormat$Field#Field(java.lang.String, int)
     */
    public void test_ConstructorLjava_lang_StringI() {
        MyField field = new MyField("a field", Calendar.DAY_OF_WEEK);

        assertEquals("field has wrong name", "a field", field.getName());
        assertEquals("field has wrong Calendar field number",
                Calendar.DAY_OF_WEEK, field.getCalendarField());

        DateFormat.Field realField = DateFormat.Field
                .ofCalendarField(Calendar.DAY_OF_WEEK);
        assertSame("Modified calendar field with the same field number",
                DateFormat.Field.DAY_OF_WEEK, realField);
    }

    /**
     * @tests java.text.DateFormat$Field#Field(java.lang.String, int)
     */
    public void test_Constructor2() {
        MyField field = new MyField("day of month", Calendar.ERA);

        assertEquals("field has wrong name", "day of month", field.getName());
        assertEquals("field has wrong Calendar field number", Calendar.ERA,
                field.getCalendarField());

        DateFormat.Field realField = DateFormat.Field
                .ofCalendarField(Calendar.ERA);
        assertSame("Modified calendar field with the same field number",
                DateFormat.Field.ERA, realField);

        DateFormat.Field realField2 = DateFormat.Field
                .ofCalendarField(Calendar.DAY_OF_MONTH);
        assertSame("Modified calendar field with the same field number",
                DateFormat.Field.DAY_OF_MONTH, realField2);
    }

    /**
     * @tests java.text.DateFormat$Field#getCalendarField()
     */
    public void test_getCalendarField() {
        // Test for method int getCalendarField()
        assertEquals("Field.AM_PM.getCalendarField() returned the wrong value",
                Calendar.AM_PM, Field.AM_PM.getCalendarField());

        // test special cases
        assertEquals(
                "Field.TIME_ZONE.getCalendarField() returned the wrong value",
                -1, Field.TIME_ZONE.getCalendarField());
        assertEquals("Field.HOUR0.getCalendarField() returned the wrong value",
                Calendar.HOUR, Field.HOUR0.getCalendarField());
        assertEquals("Field.HOUR1.getCalendarField() returned the wrong value",
                -1, Field.HOUR1.getCalendarField());
        assertEquals(
                "Field.HOUR_OF_DAY0.getCalendarField() returned the wrong value",
                Calendar.HOUR_OF_DAY, Field.HOUR_OF_DAY0.getCalendarField());
        assertEquals(
                "Field.HOUR_OF_DAY1.getCalendarField() returned the wrong value",
                -1, Field.HOUR_OF_DAY1.getCalendarField());
    }

    /**
     * @tests java.text.DateFormat$Field#ofCalendarField(int)
     */
    public void test_ofCalendarFieldI() {
        // Test for method static java.text.DateFormat.Field
        // ofCalendarField(int)
        assertSame("ofCalendarField(Calendar.AM_PM) returned the wrong value",
                Field.AM_PM, Field.ofCalendarField(Calendar.AM_PM));

        // test special cases
        assertSame("ofCalendarField(Calendar.HOUR) returned the wrong value",
                Field.HOUR0, Field.ofCalendarField(Calendar.HOUR));
        assertSame(
                "ofCalendarField(Calendar.HOUR_OF_DAY) returned the wrong value",
                Field.HOUR_OF_DAY0, Field.ofCalendarField(Calendar.HOUR_OF_DAY));

        // test illegal args
        try {
            DateFormat.Field.ofCalendarField(-1);
            fail("Expected IllegalArgumentException for ofCalendarField(-1)");
        } catch (IllegalArgumentException e) {
        }

        try {
            DateFormat.Field.ofCalendarField(Calendar.FIELD_COUNT);
            fail("Expected IllegalArgumentException for ofCalendarField(Calendar.FIELD_COUNT)");
        } catch (IllegalArgumentException e) {
        }

        // test Calendar fields that do not have corresponding DateFormat Fields
        assertNull(
                "ofCalendarField(Calendar.DST_OFFSET) returned the wrong value",
                DateFormat.Field.ofCalendarField(Calendar.DST_OFFSET));
        assertNull(
                "ofCalendarField(Calendar.ZONE_OFFSET) returned the wrong value",
                DateFormat.Field.ofCalendarField(Calendar.ZONE_OFFSET));
    }

    /**
     * @tests java.text.DateFormat$Field#readResolve()
     */
    public void test_readResolve() {
        // test for method java.lang.Object readResolve()

        // see serialization stress tests:
        // implemented in
        // SerializationStressTest4.test_writeObject_NumberFormat_Field()

        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bytes);

            DateFormat.Field dfield, dfield2;
            MyField field;

            // a regular instance of DateFormat.Field
            dfield = DateFormat.Field.MILLISECOND;

            // a subclass instance with null name
            field = new MyField(null, Calendar.AM_PM);

            out.writeObject(dfield);
            out.writeObject(field);

            in = new ObjectInputStream(new ByteArrayInputStream(bytes
                    .toByteArray()));

            try {
                dfield2 = (Field) in.readObject();
                assertSame("resolved incorrectly", dfield, dfield2);
            } catch (IllegalArgumentException e) {
                fail("Unexpected IllegalArgumentException: " + e);
            }

            try {
                in.readObject();
                fail("Expected InvalidObjectException for subclass instance with null name");
            } catch (InvalidObjectException e) {
            }

        } catch (IOException e) {
            fail("unexpected IOException" + e);
        } catch (ClassNotFoundException e) {
            fail("unexpected ClassNotFoundException" + e);
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
            }
        }
    }
}
