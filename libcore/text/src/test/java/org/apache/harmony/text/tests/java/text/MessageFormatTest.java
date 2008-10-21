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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestCase;
import tests.support.Support_MessageFormat;

public class MessageFormatTest extends TestCase {

    private MessageFormat format1, format2, format3;

    private Locale defaultLocale;

    private void checkSerialization(MessageFormat format) {
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(ba);
            out.writeObject(format);
            out.close();
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(ba.toByteArray()));
            MessageFormat read = (MessageFormat) in.readObject();
            assertTrue("Not equal: " + format.toPattern(), format.equals(read));
        } catch (IOException e) {
            fail("Format: " + format.toPattern() + " caused IOException: " + e);
        } catch (ClassNotFoundException e) {
            fail("Format: " + format.toPattern()
                    + " caused ClassNotFoundException: " + e);
        }
    }

    /**
     * @tests java.text.MessageFormat#MessageFormat(java.lang.String,
     *        java.util.Locale)
     */
    public void test_ConstructorLjava_lang_StringLjava_util_Locale() {
        // Test for method java.text.MessageFormat(java.lang.String,
        // java.util.Locale)
        Locale mk = new Locale("mk", "MK");
        MessageFormat format = new MessageFormat(
                "Date: {0,date} Currency: {1, number, currency} Integer: {2, number, integer}",
                mk);

        assertTrue("Wrong locale1", format.getLocale().equals(mk));
        assertTrue("Wrong locale2", format.getFormats()[0].equals(DateFormat
                .getDateInstance(DateFormat.DEFAULT, mk)));
        assertTrue("Wrong locale3", format.getFormats()[1].equals(NumberFormat
                .getCurrencyInstance(mk)));
        assertTrue("Wrong locale4", format.getFormats()[2].equals(NumberFormat
                .getIntegerInstance(mk)));
    }

    /**
     * @tests java.text.MessageFormat#MessageFormat(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.text.MessageFormat(java.lang.String)
        MessageFormat format = new MessageFormat(
                "abc {4,time} def {3,date} ghi {2,number} jkl {1,choice,0#low|1#high} mnop {0}");
        assertTrue("Not a MessageFormat",
                format.getClass() == MessageFormat.class);
        Format[] formats = format.getFormats();
        assertNotNull("null formats", formats);
        assertTrue("Wrong format count: " + formats.length, formats.length >= 5);
        assertTrue("Wrong time format", formats[0].equals(DateFormat
                .getTimeInstance()));
        assertTrue("Wrong date format", formats[1].equals(DateFormat
                .getDateInstance()));
        assertTrue("Wrong number format", formats[2].equals(NumberFormat
                .getInstance()));
        assertTrue("Wrong choice format", formats[3].equals(new ChoiceFormat(
                "0.0#low|1.0#high")));
        assertNull("Wrong string format", formats[4]);

        Date date = new Date();
        FieldPosition pos = new FieldPosition(-1);
        StringBuffer buffer = new StringBuffer();
        format.format(new Object[] { "123", new Double(1.6), new Double(7.2),
                date, date }, buffer, pos);
        String result = buffer.toString();
        buffer.setLength(0);
        buffer.append("abc ");
        buffer.append(DateFormat.getTimeInstance().format(date));
        buffer.append(" def ");
        buffer.append(DateFormat.getDateInstance().format(date));
        buffer.append(" ghi ");
        buffer.append(NumberFormat.getInstance().format(new Double(7.2)));
        buffer.append(" jkl high mnop 123");
        assertTrue("Wrong answer:\n" + result + "\n" + buffer, result
                .equals(buffer.toString()));

        assertEquals("Simple string", "Test message", new MessageFormat(
                "Test message").format(new Object[0]));

        try {
            result = new MessageFormat("Don't").format(new Object[0]);
            assertTrue("Should not throw IllegalArgumentException: " + result,
                    "Dont".equals(result));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            new MessageFormat("Invalid {1,foobar} format descriptor!");
            fail("Expected test_ConstructorLjava_lang_String to throw IAE.");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Expected test_ConstructorLjava_lang_String to throw IAE, not a "
                    + ex.getClass().getName());
        }

        try {
            new MessageFormat(
                    "Invalid {1,date,invalid-spec} format descriptor!");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable ex) {
            fail("Expected test_ConstructorLjava_lang_String to throw IAE, not a "
                    + ex.getClass().getName());
        }

        checkSerialization(new MessageFormat(""));
        checkSerialization(new MessageFormat("noargs"));
        checkSerialization(new MessageFormat("{0}"));
        checkSerialization(new MessageFormat("a{0}"));
        checkSerialization(new MessageFormat("{0}b"));
        checkSerialization(new MessageFormat("a{0}b"));

        // Regression for HARMONY-65
        try {
            new MessageFormat("{0,number,integer");
            fail("Assert 0: Failed to detect unmatched brackets.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.text.MessageFormat#applyPattern(java.lang.String)
     */
    public void test_applyPatternLjava_lang_String() {
        // Test for method void
        // java.text.MessageFormat.applyPattern(java.lang.String)
        MessageFormat format = new MessageFormat("test");
        format.applyPattern("xx {0}");
        assertEquals("Invalid number", "xx 46", format
                .format(new Object[] { new Integer(46) }));
        Date date = new Date();
        String result = format.format(new Object[] { date });
        String expected = "xx " + DateFormat.getInstance().format(date);
        assertTrue("Invalid date:\n" + result + "\n" + expected, result
                .equals(expected));
        format = new MessageFormat("{0,date}{1,time}{2,number,integer}");
        format.applyPattern("nothing");
        assertEquals("Found formats", "nothing", format.toPattern());

        format.applyPattern("{0}");
        assertNull("Wrong format", format.getFormats()[0]);
        assertEquals("Wrong pattern", "{0}", format.toPattern());

        format.applyPattern("{0, \t\u001ftime }");
        assertTrue("Wrong time format", format.getFormats()[0]
                .equals(DateFormat.getTimeInstance()));
        assertEquals("Wrong time pattern", "{0,time}", format.toPattern());
        format.applyPattern("{0,Time, Short\n}");
        assertTrue("Wrong short time format", format.getFormats()[0]
                .equals(DateFormat.getTimeInstance(DateFormat.SHORT)));
        assertEquals("Wrong short time pattern", "{0,time,short}", format
                .toPattern());
        format.applyPattern("{0,TIME,\nmedium  }");
        assertTrue("Wrong medium time format", format.getFormats()[0]
                .equals(DateFormat.getTimeInstance(DateFormat.MEDIUM)));
        assertEquals("Wrong medium time pattern", "{0,time}", format
                .toPattern());
        format.applyPattern("{0,time,LONG}");
        assertTrue("Wrong long time format", format.getFormats()[0]
                .equals(DateFormat.getTimeInstance(DateFormat.LONG)));
        assertEquals("Wrong long time pattern", "{0,time,long}", format
                .toPattern());
        format.setLocale(Locale.FRENCH); // use French since English has the
        // same LONG and FULL time patterns
        format.applyPattern("{0,time, Full}");
        assertTrue("Wrong full time format", format.getFormats()[0]
                .equals(DateFormat.getTimeInstance(DateFormat.FULL,
                        Locale.FRENCH)));
        assertEquals("Wrong full time pattern", "{0,time,full}", format
                .toPattern());
        format.setLocale(Locale.getDefault());

        format.applyPattern("{0, date}");
        assertTrue("Wrong date format", format.getFormats()[0]
                .equals(DateFormat.getDateInstance()));
        assertEquals("Wrong date pattern", "{0,date}", format.toPattern());
        format.applyPattern("{0, date, short}");
        assertTrue("Wrong short date format", format.getFormats()[0]
                .equals(DateFormat.getDateInstance(DateFormat.SHORT)));
        assertEquals("Wrong short date pattern", "{0,date,short}", format
                .toPattern());
        format.applyPattern("{0, date, medium}");
        assertTrue("Wrong medium date format", format.getFormats()[0]
                .equals(DateFormat.getDateInstance(DateFormat.MEDIUM)));
        assertEquals("Wrong medium date pattern", "{0,date}", format
                .toPattern());
        format.applyPattern("{0, date, long}");
        assertTrue("Wrong long date format", format.getFormats()[0]
                .equals(DateFormat.getDateInstance(DateFormat.LONG)));
        assertEquals("Wrong long date pattern", "{0,date,long}", format
                .toPattern());
        format.applyPattern("{0, date, full}");
        assertTrue("Wrong full date format", format.getFormats()[0]
                .equals(DateFormat.getDateInstance(DateFormat.FULL)));
        assertEquals("Wrong full date pattern", "{0,date,full}", format
                .toPattern());

        format.applyPattern("{0, date, MMM d {hh:mm:ss}}");
        assertEquals("Wrong time/date format", " MMM d {hh:mm:ss}",
                ((SimpleDateFormat) (format.getFormats()[0])).toPattern());
        assertEquals("Wrong time/date pattern", "{0,date, MMM d {hh:mm:ss}}",
                format.toPattern());

        format.applyPattern("{0, number}");
        assertTrue("Wrong number format", format.getFormats()[0]
                .equals(NumberFormat.getNumberInstance()));
        assertEquals("Wrong number pattern", "{0,number}", format.toPattern());
        format.applyPattern("{0, number, currency}");
        assertTrue("Wrong currency number format", format.getFormats()[0]
                .equals(NumberFormat.getCurrencyInstance()));
        assertEquals("Wrong currency number pattern", "{0,number,currency}",
                format.toPattern());
        format.applyPattern("{0, number, percent}");
        assertTrue("Wrong percent number format", format.getFormats()[0]
                .equals(NumberFormat.getPercentInstance()));
        assertEquals("Wrong percent number pattern", "{0,number,percent}",
                format.toPattern());
        format.applyPattern("{0, number, integer}");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setParseIntegerOnly(true);
        assertTrue("Wrong integer number format", format.getFormats()[0]
                .equals(nf));
        assertEquals("Wrong integer number pattern", "{0,number,integer}",
                format.toPattern());

        format.applyPattern("{0, number, {'#'}##0.0E0}");

        /*
         * TODO validate these assertions String actual =
         * ((DecimalFormat)(format.getFormats()[0])).toPattern();
         * assertEquals("Wrong pattern number format", "' {#}'##0.0E0", actual);
         * assertEquals("Wrong pattern number pattern", "{0,number,'
         * {#}'##0.0E0}", format.toPattern());
         * 
         */

        format.applyPattern("{0, choice,0#no|1#one|2#{1,number}}");
        assertEquals("Wrong choice format",

        "0.0#no|1.0#one|2.0#{1,number}",
                ((ChoiceFormat) format.getFormats()[0]).toPattern());
        assertEquals("Wrong choice pattern",
                "{0,choice,0.0#no|1.0#one|2.0#{1,number}}", format.toPattern());
        assertEquals("Wrong formatted choice", "3.6", format
                .format(new Object[] { new Integer(2), new Float(3.6) }));

        try {
            format.applyPattern("WRONG MESSAGE FORMAT {0,number,{}");
            fail("Expected IllegalArgumentException for invalid pattern");
        } catch (IllegalArgumentException e) {
        }

        // Regression for HARMONY-65
        MessageFormat mf = new MessageFormat("{0,number,integer}");
        String badpattern = "{0,number,#";
        try {
            mf.applyPattern(badpattern);
            fail("Assert 0: Failed to detect unmatched brackets.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.text.MessageFormat#clone()
     */
    public void test_clone() {
        // Test for method java.lang.Object java.text.MessageFormat.clone()
        MessageFormat format = new MessageFormat("'{'choice'}'{0}");
        MessageFormat clone = (MessageFormat) format.clone();
        assertTrue("Clone not equal", format.equals(clone));
        assertEquals("Wrong answer", "{choice}{0}", format
                .format(new Object[] {}));
        clone.setFormat(0, DateFormat.getInstance());
        assertTrue("Clone shares format data", !format.equals(clone));
        format = (MessageFormat) clone.clone();
        Format[] formats = clone.getFormats();
        ((SimpleDateFormat) formats[0]).applyPattern("adk123");
        assertTrue("Clone shares format data", !format.equals(clone));
    }

    /**
     * @tests java.text.MessageFormat#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean
        // java.text.MessageFormat.equals(java.lang.Object)
        MessageFormat format1 = new MessageFormat("{0}");
        MessageFormat format2 = new MessageFormat("{1}");
        assertTrue("Should not be equal", !format1.equals(format2));
        format2.applyPattern("{0}");
        assertTrue("Should be equal", format1.equals(format2));
        SimpleDateFormat date = (SimpleDateFormat) DateFormat.getTimeInstance();
        format1.setFormat(0, DateFormat.getTimeInstance());
        format2.setFormat(0, new SimpleDateFormat(date.toPattern()));
        assertTrue("Should be equal2", format1.equals(format2));
    }

    /**
     * @tests java.text.MessageFormat#hashCode()
     */
    public void test_hashCode() {
        // Test for method
        // int java.text.MessageFormat.hashCode()
        assertEquals("Should be equal", 3648, new MessageFormat("rr", null)
                .hashCode());
    }

    /**
     * @tests java.text.MessageFormat#formatToCharacterIterator(java.lang.Object)
     */
    // FIXME This test fails on Harmony ClassLibrary
    public void failing_test_formatToCharacterIteratorLjava_lang_Object() {
        // Test for method formatToCharacterIterator(java.lang.Object)
        new Support_MessageFormat(
                "test_formatToCharacterIteratorLjava_lang_Object")
                .t_formatToCharacterIterator();
    }

    /**
     * @tests java.text.MessageFormat#format(java.lang.Object[],
     *        java.lang.StringBuffer, java.text.FieldPosition)
     */
    public void test_format$Ljava_lang_ObjectLjava_lang_StringBufferLjava_text_FieldPosition() {
        // Test for method java.lang.StringBuffer
        // java.text.MessageFormat.format(java.lang.Object [],
        // java.lang.StringBuffer, java.text.FieldPosition)
        MessageFormat format = new MessageFormat("{1,number,integer}");
        StringBuffer buffer = new StringBuffer();
        format.format(new Object[] { "0", new Double(53.863) }, buffer,
                new FieldPosition(0));
        assertEquals("Wrong result", "54", buffer.toString());
        format
                .applyPattern("{0,choice,0#zero|1#one '{1,choice,2#two {2,time}}'}");
        Date date = new Date();
        String expected = "one two "
                + DateFormat.getTimeInstance().format(date);
        String result = format.format(new Object[] { new Double(1.6),
                new Integer(3), date });
        assertTrue("Choice not recursive:\n" + expected + "\n" + result,
                expected.equals(result));
    }

    /**
     * @tests java.text.MessageFormat#format(java.lang.Object,
     *        java.lang.StringBuffer, java.text.FieldPosition)
     */
    public void test_formatLjava_lang_ObjectLjava_lang_StringBufferLjava_text_FieldPosition() {
        // Test for method java.lang.StringBuffer
        // java.text.MessageFormat.format(java.lang.Object,
        // java.lang.StringBuffer, java.text.FieldPosition)
        new Support_MessageFormat(
                "test_formatLjava_lang_ObjectLjava_lang_StringBufferLjava_text_FieldPosition")
                .t_format_with_FieldPosition();
    }

    /**
     * @tests java.text.MessageFormat#format(java.lang.String,
     *        java.lang.Object...) Test of method
     *        java.text.MessageFormat#format(java.lang.String,
     *        java.lang.Object...).
     */
    public void test_formatLjava_lang_StringLjava_lang_Object() {
        int iCurrency = 123;
        int iInteger  = Integer.MIN_VALUE;

        Date     date     = new Date(12345678);
        Object[] args     = { date, iCurrency, iInteger };
        String   resStr   = "Date: Jan 1, 1970 Currency: $" + iCurrency
                    + ".00 Integer: -2,147,483,648";
        String   pattern  = "Date: {0,date} Currency: {1, number, currency} Integer: {2, number, integer}";
        String   sFormat  = MessageFormat.format(pattern, (Object[]) args);
        assertEquals(
                "format(String, Object[]) with valid parameters returns incorrect string: case 1",
                sFormat, resStr);

        pattern = "abc {4, number, integer} def {3,date} ghi {2,number} jkl {1,choice,0#low|1#high} mnop {0}";
        resStr  = "abc -2,147,483,648 def Jan 1, 1970 ghi -2,147,483,648 jkl high mnop -2,147,483,648";
        Object[] args_ = { iInteger, 1, iInteger, date, iInteger };
        sFormat = MessageFormat.format(pattern, args_);
        assertEquals(
                "format(String, Object[]) with valid parameters returns incorrect string: case 1",
                sFormat, resStr);

        try {
            args = null;
            MessageFormat.format(null, args);
            fail("Doesn't throw IllegalArgumentException: null, null");
        } catch (Exception e) {
            // expected
        }

        try {
            MessageFormat.format("Invalid {1,foobar} format descriptor!",
                    new Object[] {iInteger} );
            fail("Doesn't throw IllegalArgumentException with invalid pattern as a parameter: case 1");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MessageFormat.format(
                    "Invalid {1,date,invalid-spec} format descriptor!", new Object[]{""});
            fail("Doesn't throw IllegalArgumentException with invalid pattern as a parameter: case 2");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MessageFormat.format("{0,number,integer", new Object[] {iInteger});
            fail("Doesn't throw IllegalArgumentException, doesn't detect unmatched brackets");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            MessageFormat.format(
                    "Valid {1, date} format {0, number} descriptor!", new Object[]{ "" } );
            fail("Doesn't throw IllegalArgumentException with invalid Object array");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    /**
     * @tests java.text.MessageFormat#getFormats()
     */
    public void test_getFormats() {
        // Test for method java.text.Format []
        // java.text.MessageFormat.getFormats()

        // test with repeating formats and max argument index < max offset
        Format[] formats = format1.getFormats();
        Format[] correctFormats = new Format[] {
                NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(),
                NumberFormat.getPercentInstance(), null,
                new ChoiceFormat("0#off|1#on"), DateFormat.getDateInstance(), };

        assertEquals("Test1:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        // test with max argument index > max offset
        formats = format2.getFormats();
        correctFormats = new Format[] { NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(),
                NumberFormat.getPercentInstance(), null,
                new ChoiceFormat("0#off|1#on"), DateFormat.getDateInstance() };

        assertEquals("Test2:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        // test with argument number being zero
        formats = format3.getFormats();
        assertEquals("Test3: Returned wrong number of formats:", 0,
                formats.length);
    }

    /**
     * @tests java.text.MessageFormat#getFormatsByArgumentIndex()
     */
    public void test_getFormatsByArgumentIndex() {
        // Test for method java.text.Format [] test_getFormatsByArgumentIndex()

        // test with repeating formats and max argument index < max offset
        Format[] formats = format1.getFormatsByArgumentIndex();
        Format[] correctFormats = new Format[] { DateFormat.getDateInstance(),
                new ChoiceFormat("0#off|1#on"), DateFormat.getTimeInstance(),
                NumberFormat.getCurrencyInstance(), null };

        assertEquals("Test1:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        // test with max argument index > max offset
        formats = format2.getFormatsByArgumentIndex();
        correctFormats = new Format[] { DateFormat.getDateInstance(),
                new ChoiceFormat("0#off|1#on"), null,
                NumberFormat.getCurrencyInstance(), null, null, null, null,
                DateFormat.getTimeInstance() };

        assertEquals("Test2:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        // test with argument number being zero
        formats = format3.getFormatsByArgumentIndex();
        assertEquals("Test3: Returned wrong number of formats:", 0,
                formats.length);
    }

    /**
     * @tests java.text.MessageFormat#getLocale() Test of method
     *        java.text.MessageFormat#getLocale().
     */
    public void test_getLocale() {
        try {
            Locale[] l = {
                    Locale.FRANCE,
                    Locale.KOREA,
                    new Locale(Locale.FRANCE.getCountry(), Locale.FRANCE
                            .getLanguage()), new Locale("mk"),
                    new Locale("mk", "MK"), Locale.US,
                    new Locale("#ru", "@31230") };

            String pattern = "getLocale test {0,number,#,####}";
            MessageFormat mf;

            for (int i = 0; i < 0; i++) {
                mf = new MessageFormat(pattern, l[i]);
                Locale result = mf.getLocale();
                assertEquals("Returned local: " + result + " instead of "
                        + l[i], l[i], result);
                assertEquals("Returned language: " + result.getLanguage()
                        + " instead of " + l[i].getLanguage(), l[i]
                        .getLanguage(), result.getLanguage());
                assertEquals("Returned country: " + result.getCountry()
                        + " instead of " + l[i].getCountry(),
                        l[i].getCountry(), result.getCountry());
            }

            mf = new MessageFormat(pattern);
            mf.setLocale(null);
            Locale result = mf.getLocale();
            assertEquals("Returned local: " + result + " instead of null",
                    null, result);
        } catch (Exception e) {
            fail("unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.MessageFormat#setFormat(int, Format) Test of method
     *        java.text.MessageFormat#setFormat(int, Format). Case 1: Compare
     *        getFormats() results after calls to setFormat(). Case 2: Try to
     *        call setFormat() using incorrect index.
     */
    public void test_setFormatILjava_text_Format() {
        try {
            // case 1: Compare getFormats() results after calls to setFormat()
            MessageFormat f1 = (MessageFormat) format1.clone();
            f1.setFormat(0, DateFormat.getTimeInstance());
            f1.setFormat(1, DateFormat.getTimeInstance());
            f1.setFormat(2, NumberFormat.getInstance());
            f1.setFormat(3, new ChoiceFormat("0#off|1#on"));
            f1.setFormat(4, new ChoiceFormat("1#few|2#ok|3#a lot"));
            f1.setFormat(5, DateFormat.getTimeInstance());

            Format[] formats = f1.getFormats();
            formats = f1.getFormats();

            Format[] correctFormats = new Format[] {
                    DateFormat.getTimeInstance(), DateFormat.getTimeInstance(),
                    NumberFormat.getInstance(), new ChoiceFormat("0#off|1#on"),
                    new ChoiceFormat("1#few|2#ok|3#a lot"),
                    DateFormat.getTimeInstance() };

            assertEquals("Test1A:Returned wrong number of formats:",
                    correctFormats.length, formats.length);
            for (int i = 0; i < correctFormats.length; i++) {
                assertEquals(
                        "Test1B:wrong format for pattern index " + i + ":",
                        correctFormats[i], formats[i]);
            }

            // case 2: Try to setFormat using incorrect index
            try {
                f1.setFormat(-1, DateFormat.getDateInstance());
                fail("Expected ArrayIndexOutOfBoundsException was not thrown");
                f1.setFormat(f1.getFormats().length, DateFormat
                        .getDateInstance());
                fail("Expected ArrayIndexOutOfBoundsException was not thrown");
            } catch (ArrayIndexOutOfBoundsException e) {
                // expected
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.MessageFormat#setFormatByArgumentIndex(int,
     *        java.text.Format)
     */
    public void test_setFormatByArgumentIndexILjava_text_Format() {
        // test for method setFormatByArgumentIndex(int, Format)
        MessageFormat f1 = (MessageFormat) format1.clone();
        f1.setFormatByArgumentIndex(0, DateFormat.getTimeInstance());
        f1.setFormatByArgumentIndex(4, new ChoiceFormat("1#few|2#ok|3#a lot"));

        // test with repeating formats and max argument index < max offset
        // compare getFormatsByArgumentIndex() results after calls to
        // setFormatByArgumentIndex()
        Format[] formats = f1.getFormatsByArgumentIndex();

        Format[] correctFormats = new Format[] { DateFormat.getTimeInstance(),
                new ChoiceFormat("0#off|1#on"), DateFormat.getTimeInstance(),
                NumberFormat.getCurrencyInstance(),
                new ChoiceFormat("1#few|2#ok|3#a lot") };

        assertEquals("Test1A:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1B:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        // compare getFormats() results after calls to
        // setFormatByArgumentIndex()
        formats = f1.getFormats();

        correctFormats = new Format[] { NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(), DateFormat.getTimeInstance(),
                new ChoiceFormat("1#few|2#ok|3#a lot"),
                new ChoiceFormat("0#off|1#on"), DateFormat.getTimeInstance(), };

        assertEquals("Test1C:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1D:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        // test setting argumentIndexes that are not used
        MessageFormat f2 = (MessageFormat) format2.clone();
        f2.setFormatByArgumentIndex(2, NumberFormat.getPercentInstance());
        f2.setFormatByArgumentIndex(4, DateFormat.getTimeInstance());

        formats = f2.getFormatsByArgumentIndex();
        correctFormats = format2.getFormatsByArgumentIndex();

        assertEquals("Test2A:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2B:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        formats = f2.getFormats();
        correctFormats = format2.getFormats();

        assertEquals("Test2C:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2D:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        // test exceeding the argumentIndex number
        MessageFormat f3 = (MessageFormat) format3.clone();
        f3.setFormatByArgumentIndex(1, NumberFormat.getCurrencyInstance());

        formats = f3.getFormatsByArgumentIndex();
        assertEquals("Test3A:Returned wrong number of formats:", 0,
                formats.length);

        formats = f3.getFormats();
        assertEquals("Test3B:Returned wrong number of formats:", 0,
                formats.length);
    }

    /**
     * @tests java.text.MessageFormat#setFormats(Format[]) Test of method
     *        java.text.MessageFormat#setFormats(Format[]). Case 1: Test with
     *        repeating formats and max argument index < max offset compare
     *        getFormats() results after calls to setFormats(Format[]) Case 2:
     *        Try to pass null argument to setFormats().
     */
    public void test_setFormats$Ljava_text_Format() {
        try {
            MessageFormat f1 = (MessageFormat) format1.clone();

            // case 1: Test with repeating formats and max argument index < max
            // offset
            // compare getFormats() results after calls to setFormats(Format[])
            Format[] correctFormats = new Format[] {
                    DateFormat.getTimeInstance(),
                    new ChoiceFormat("0#off|1#on"),
                    DateFormat.getTimeInstance(),
                    NumberFormat.getCurrencyInstance(),
                    new ChoiceFormat("1#few|2#ok|3#a lot") };

            f1.setFormats(correctFormats);
            Format[] formats = f1.getFormats();

            assertTrue("Test1A:Returned wrong number of formats:",
                    correctFormats.length <= formats.length);
            for (int i = 0; i < correctFormats.length; i++) {
                assertEquals("Test1B:wrong format for argument index " + i
                        + ":", correctFormats[i], formats[i]);
            }

            // case 2: Try to pass null argument to setFormats().
            try {
                f1.setFormats(null);
                fail("Expected exception NullPointerException was not thrown");
            } catch (NullPointerException e) {
                // expected
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.MessageFormat#setFormatsByArgumentIndex(java.text.Format[])
     */
    public void test_setFormatsByArgumentIndex$Ljava_text_Format() {
        // test for method setFormatByArgumentIndex(Format[])
        MessageFormat f1 = (MessageFormat) format1.clone();

        // test with repeating formats and max argument index < max offset
        // compare getFormatsByArgumentIndex() results after calls to
        // setFormatsByArgumentIndex(Format[])
        Format[] correctFormats = new Format[] { DateFormat.getTimeInstance(),
                new ChoiceFormat("0#off|1#on"), DateFormat.getTimeInstance(),
                NumberFormat.getCurrencyInstance(),
                new ChoiceFormat("1#few|2#ok|3#a lot") };

        f1.setFormatsByArgumentIndex(correctFormats);
        Format[] formats = f1.getFormatsByArgumentIndex();

        assertEquals("Test1A:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1B:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        // compare getFormats() results after calls to
        // setFormatByArgumentIndex()
        formats = f1.getFormats();
        correctFormats = new Format[] { NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(), DateFormat.getTimeInstance(),
                new ChoiceFormat("1#few|2#ok|3#a lot"),
                new ChoiceFormat("0#off|1#on"), DateFormat.getTimeInstance(), };

        assertEquals("Test1C:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test1D:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        // test setting argumentIndexes that are not used
        MessageFormat f2 = (MessageFormat) format2.clone();
        Format[] inputFormats = new Format[] { DateFormat.getDateInstance(),
                new ChoiceFormat("0#off|1#on"),
                NumberFormat.getPercentInstance(),
                NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(), null, null, null,
                DateFormat.getTimeInstance() };
        f2.setFormatsByArgumentIndex(inputFormats);

        formats = f2.getFormatsByArgumentIndex();
        correctFormats = format2.getFormatsByArgumentIndex();

        assertEquals("Test2A:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2B:wrong format for argument index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        formats = f2.getFormats();
        correctFormats = new Format[] { NumberFormat.getCurrencyInstance(),
                DateFormat.getTimeInstance(), DateFormat.getDateInstance(),
                null, new ChoiceFormat("0#off|1#on"),
                DateFormat.getDateInstance() };

        assertEquals("Test2C:Returned wrong number of formats:",
                correctFormats.length, formats.length);
        for (int i = 0; i < correctFormats.length; i++) {
            assertEquals("Test2D:wrong format for pattern index " + i + ":",
                    correctFormats[i], formats[i]);
        }

        // test exceeding the argumentIndex number
        MessageFormat f3 = (MessageFormat) format3.clone();
        f3.setFormatsByArgumentIndex(inputFormats);

        formats = f3.getFormatsByArgumentIndex();
        assertEquals("Test3A:Returned wrong number of formats:", 0,
                formats.length);

        formats = f3.getFormats();
        assertEquals("Test3B:Returned wrong number of formats:", 0,
                formats.length);

    }

    /**
     * @tests java.text.MessageFormat#parse(java.lang.String,
     *        java.text.ParsePosition)
     */
    public void test_parseLjava_lang_StringLjava_text_ParsePosition() {
        // Test for method java.lang.Object []
        // java.text.MessageFormat.parse(java.lang.String,
        // java.text.ParsePosition)
        MessageFormat format = new MessageFormat("date is {0,date,MMM d, yyyy}");
        ParsePosition pos = new ParsePosition(2);
        Object[] result = (Object[]) format
                .parse("xxdate is Feb 28, 1999", pos);
        assertTrue("No result: " + result.length, result.length >= 1);
        assertTrue("Wrong answer", ((Date) result[0])
                .equals(new GregorianCalendar(1999, Calendar.FEBRUARY, 28)
                        .getTime()));

        MessageFormat mf = new MessageFormat("vm={0},{1},{2}");
        result = mf.parse("vm=win,foo,bar", new ParsePosition(0));
        assertTrue("Invalid parse", result[0].equals("win")
                && result[1].equals("foo") && result[2].equals("bar"));

        mf = new MessageFormat("{0}; {0}; {0}");
        String parse = "a; b; c";
        result = mf.parse(parse, new ParsePosition(0));
        assertEquals("Wrong variable result", "c", result[0]);
    }

    /**
     * @tests java.text.MessageFormat#setLocale(java.util.Locale)
     */
    public void test_setLocaleLjava_util_Locale() {
        // Test for method void
        // java.text.MessageFormat.setLocale(java.util.Locale)
        MessageFormat format = new MessageFormat("date {0,date}");
        format.setLocale(Locale.CHINA);
        assertEquals("Wrong locale1", Locale.CHINA, format.getLocale());
        format.applyPattern("{1,date}");
        assertEquals("Wrong locale3", DateFormat.getDateInstance(
                DateFormat.DEFAULT, Locale.CHINA), format.getFormats()[0]);
    }

    /**
     * @tests java.text.MessageFormat#toPattern()
     */
    public void test_toPattern() {
        // Test for method java.lang.String java.text.MessageFormat.toPattern()
        String pattern = "[{0}]";
        MessageFormat mf = new MessageFormat(pattern);
        assertTrue("Wrong pattern", mf.toPattern().equals(pattern));

        // Regression for HARMONY-59
        new MessageFormat("CHOICE {1,choice}").toPattern();
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        // test with repeating formats and max argument index < max offset
        String pattern = "A {3, number, currency} B {2, time} C {0, number, percent} D {4}  E {1,choice,0#off|1#on} F {0, date}";
        format1 = new MessageFormat(pattern);

        // test with max argument index > max offset
        pattern = "A {3, number, currency} B {8, time} C {0, number, percent} D {6}  E {1,choice,0#off|1#on} F {0, date}";
        format2 = new MessageFormat(pattern);

        // test with argument number being zero
        pattern = "A B C D E F";
        format3 = new MessageFormat(pattern);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        Locale.setDefault(defaultLocale);
    }

    /**
     * @tests java.text.MessageFormat(java.util.Locale)
     */
    public void test_ConstructorLjava_util_Locale() {
        // Regression for HARMONY-65
        try {
            new MessageFormat("{0,number,integer", Locale.US);
            fail("Assert 0: Failed to detect unmatched brackets.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.text.MessageFormat#parse(java.lang.String) Test of method
     *        java.text.MessageFormat#parse(java.lang.String).
     */
    public void test_parseLjava_lang_String() throws ParseException {
        String pattern = "A {3, number, currency} B {2, time} C {0, number, percent} D {4}  E {1,choice,0#off|1#on} F {0, date}";
        MessageFormat mf = new MessageFormat(pattern);
        String sToParse = "A $12,345.00 B 9:56:07 AM C 3,200% D 1/15/70 9:56 AM  E on F Jan 1, 1970";
        Object[] result;
        try {
            result = mf.parse(sToParse);

            assertTrue("No result: " + result.length, result.length == 5);
            assertTrue("Object 0 is not date", result[0] instanceof Date);
            assertEquals("Object 1 is not stringr", result[1].toString(), "1.0");
            assertTrue("Object 2 is not date", result[2] instanceof Date);
            assertEquals("Object 3 is not number", result[3].toString(),
                    "12345");
            assertEquals("Object 4 is not string", result[4].toString(),
                    "1/15/70 9:56 AM");

        } catch (java.text.ParseException pe) {
            fail("ParseException is thrown for incorrect string " + sToParse);
        }

        sToParse = "xxdate is Feb 28, 1999";
        try {
            result = format1.parse(sToParse);
            fail("ParseException is thrown for incorrect string " + sToParse);
        } catch (java.text.ParseException pe) {
            // expected
        }

        sToParse = "vm=Test, @3 4 6, 3   ";
        mf = new MessageFormat("vm={0},{1},{2}");
        try {
            result = mf.parse(sToParse);
            assertTrue("No result: " + result.length, result.length == 3);
            assertEquals("Object 0 is not string", result[0].toString(), "Test");
            assertEquals("Object 1 is not string", result[1].toString(),
                    " @3 4 6");
            assertEquals("Object 2 is not string", result[2].toString(),
                    " 3   ");
        } catch (java.text.ParseException pe) {
            fail("ParseException is thrown for correct string " + sToParse);
        }

        try {
            result = mf.parse(null);
            fail("ParseException is not thrown for null " + sToParse);
        } catch (java.text.ParseException pe) {
            // expected
        }
    }

    /**
     * @tests java.text.MessageFormat#parseObject(java.lang.String,
     *        java.text.ParsePosition) Test of method
     *        java.text.MessageFormat#parseObject(java.lang.String,
     *        java.text.ParsePosition). Case 1: Parsing of correct data string.
     *        Case 2: Parsing of partial correct data string. Case 3: Try to use
     *        argument ParsePosition as null.
     */
    public void test_parseObjectLjava_lang_StringLjavajava_text_ParsePosition() {
        MessageFormat mf = new MessageFormat("{0,number,#.##}, {0,number,#.#}");
        try {
            // case 1: Try to parse correct data string.
            Object[] objs = { new Double(3.1415) };
            String result = mf.format(objs);
            // result now equals "3.14, 3.1"
            Object[] res = null;
            ParsePosition pp = new ParsePosition(0);
            int parseIndex = pp.getIndex();
            res = (Object[]) mf.parseObject(result, pp);
            assertTrue("Parse operation return null", res != null);
            assertTrue("parse operation return array with incorrect length",
                    1 == res.length);
            assertTrue("ParseIndex is incorrect", pp.getIndex() != parseIndex);
            assertTrue("Result object is incorrect", new Double(3.1)
                    .equals(res[0]));

            // case 2: Try to parse partially correct data string.
            pp.setIndex(0);
            char[] cur = result.toCharArray();
            cur[cur.length / 2] = 'Z';
            String partialCorrect = new String(cur);
            res = (Object[]) mf.parseObject(partialCorrect, pp);
            assertTrue("Parse operation return null", res == null);
            assertTrue("ParseIndex is incorrect", pp.getIndex() == 0);
            assertTrue("ParseErrorIndex is incorrect",
                    pp.getErrorIndex() == cur.length / 2);

            // case 3: Try to use argument ParsePosition as null.
            try {
                mf.parseObject(result, null);
                fail("Expected NullPointerException was not thrown");
            } catch (NullPointerException e) {
                // expected
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    public void test_format_Object() {
        // Regression for HARMONY-1875
        Locale.setDefault(Locale.CANADA);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String pat = "text here {0,date,yyyyyyyyy} and here";
        Calendar c = Calendar.getInstance();
        String etalon = "text here 00000" + c.get(Calendar.YEAR) + " and here";
        MessageFormat obj = new MessageFormat(pat);
        assertEquals(etalon, obj.format(new Object[] { new Date() }));
    }

}
