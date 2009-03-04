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

package org.apache.harmony.luni.util;


import java.io.ByteArrayOutputStream;
import java.io.UTFDataFormatException;
import java.util.Calendar;
import java.util.TimeZone;

public final class Util {

    private static String[] WEEKDAYS = new String[] { "", "Sunday", "Monday",
            "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

    private static String[] MONTHS = new String[] { "January", "February",
            "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December" };

    private static final String defaultEncoding;

    static {
        // BEGIN android-changed
        String encoding = System.getProperty("file.encoding");
        // END android-changed
        if (encoding != null) {
            try {
                "".getBytes(encoding);
            } catch (java.io.UnsupportedEncodingException e) {
                encoding = null;
            }
        }
        defaultEncoding = encoding;
    }

    public static byte[] getBytes(String name) {
        if (defaultEncoding != null) {
            try {
                return name.getBytes(defaultEncoding);
            } catch (java.io.UnsupportedEncodingException e) {
            }
        }
        return name.getBytes();
    }

    public static String toString(byte[] bytes) {
        if (defaultEncoding != null) {
            try {
                return new String(bytes, 0, bytes.length, defaultEncoding);
            } catch (java.io.UnsupportedEncodingException e) {
            }
        }
        return new String(bytes, 0, bytes.length);
    }

    public static String toString(byte[] bytes, int offset, int length) {
        if (defaultEncoding != null) {
            try {
                return new String(bytes, offset, length, defaultEncoding);
            } catch (java.io.UnsupportedEncodingException e) {
            }
        }
        return new String(bytes, offset, length);
    }

    /**
     * Returns the millisecond value of the date and time parsed from the
     * specified String. Many date/time formats are recognized
     * 
     * @param string
     *            the String to parse
     * @return the millisecond value parsed from the String
     */
    public static long parseDate(String string) {
        int offset = 0, length = string.length(), state = 0;
        int year = -1, month = -1, date = -1;
        int hour = -1, minute = -1, second = -1;
        final int PAD = 0, LETTERS = 1, NUMBERS = 2;
        StringBuffer buffer = new StringBuffer();

        while (offset <= length) {
            char next = offset < length ? string.charAt(offset) : '\r';
            offset++;

            int nextState;
            if ((next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))
                nextState = LETTERS;
            else if (next >= '0' && next <= '9')
                nextState = NUMBERS;
            else if (" ,-:\r\t".indexOf(next) == -1)
                throw new IllegalArgumentException();
            else
                nextState = PAD;

            if (state == NUMBERS && nextState != NUMBERS) {
                int digit = Integer.parseInt(buffer.toString());
                buffer.setLength(0);
                if (digit >= 70) {
                    if (year != -1
                            || (next != ' ' && next != ',' && next != '\r'))
                        throw new IllegalArgumentException();
                    year = digit;
                } else if (next == ':') {
                    if (hour == -1)
                        hour = digit;
                    else if (minute == -1)
                        minute = digit;
                    else
                        throw new IllegalArgumentException();
                } else if (next == ' ' || next == ',' || next == '-'
                        || next == '\r') {
                    if (hour != -1 && minute == -1)
                        minute = digit;
                    else if (minute != -1 && second == -1)
                        second = digit;
                    else if (date == -1)
                        date = digit;
                    else if (year == -1)
                        year = digit;
                    else
                        throw new IllegalArgumentException();
                } else if (year == -1 && month != -1 && date != -1)
                    year = digit;
                else
                    throw new IllegalArgumentException();
            } else if (state == LETTERS && nextState != LETTERS) {
                String text = buffer.toString().toUpperCase();
                buffer.setLength(0);
                if (text.length() < 3)
                    throw new IllegalArgumentException();
                if (parse(text, WEEKDAYS) != -1) {
                } else if (month == -1 && (month = parse(text, MONTHS)) != -1) {
                } else if (text.equals("GMT")) {
                } else
                    throw new IllegalArgumentException();
            }

            if (nextState == LETTERS || nextState == NUMBERS)
                buffer.append(next);
            state = nextState;
        }

        if (year != -1 && month != -1 && date != -1) {
            if (hour == -1)
                hour = 0;
            if (minute == -1)
                minute = 0;
            if (second == -1)
                second = 0;
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            int current = cal.get(Calendar.YEAR) - 80;
            if (year < 100) {
                year += current / 100 * 100;
                if (year < current)
                    year += 100;
            }
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DATE, date);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime().getTime();
        }
        throw new IllegalArgumentException();
    }

    private static int parse(String string, String[] array) {
        int length = string.length();
        for (int i = 0; i < array.length; i++) {
            if (string.regionMatches(true, 0, array[i], 0, length))
                return i;
        }
        return -1;
    }

    public static String convertFromUTF8(byte[] buf, int offset, int utfSize)
            throws UTFDataFormatException {
        return convertUTF8WithBuf(buf, new char[utfSize], offset, utfSize);
    }

    public static String convertUTF8WithBuf(byte[] buf, char[] out, int offset,
            int utfSize) throws UTFDataFormatException {
        int count = 0, s = 0, a;
        while (count < utfSize) {
            if ((out[s] = (char) buf[offset + count++]) < '\u0080')
                s++;
            else if (((a = out[s]) & 0xe0) == 0xc0) {
                if (count >= utfSize)
                    throw new UTFDataFormatException(Msg.getString("K0062",
                            count));
                // BEGIN android-changed
                int b = buf[offset + count++];
                // END android-changed
                if ((b & 0xC0) != 0x80)
                    throw new UTFDataFormatException(Msg.getString("K0062",
                            (count - 1)));
                out[s++] = (char) (((a & 0x1F) << 6) | (b & 0x3F));
            } else if ((a & 0xf0) == 0xe0) {
                if (count + 1 >= utfSize)
                    throw new UTFDataFormatException(Msg.getString("K0063",
                            (count + 1)));
                // BEGIN android-changed
                int b = buf[offset + count++];
                int c = buf[offset + count++];
                // END android-changed
                if (((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80))
                    throw new UTFDataFormatException(Msg.getString("K0064",
                            (count - 2)));
                out[s++] = (char) (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F));
            } else {
                throw new UTFDataFormatException(Msg.getString("K0065",
                        (count - 1)));
            }
        }
        return new String(out, 0, s);
    }

    /**
     * '%' and two following hex digit characters are converted to the
     * equivalent byte value. All other characters are passed through
     * unmodified. e.g. "ABC %24%25" -> "ABC $%"
     * 
     * @param s
     *            java.lang.String The encoded string.
     * @return java.lang.String The decoded version.
     */
    public static String decode(String s, boolean convertPlus) {
        if (!convertPlus && s.indexOf('%') == -1)
            return s;
        StringBuffer result = new StringBuffer(s.length());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (convertPlus && c == '+')
                result.append(' ');
            else if (c == '%') {
                out.reset();
                do {
                    if (i + 2 >= s.length())
                        throw new IllegalArgumentException(Msg.getString(
                                "K01fe", i));
                    int d1 = Character.digit(s.charAt(i + 1), 16);
                    int d2 = Character.digit(s.charAt(i + 2), 16);
                    if (d1 == -1 || d2 == -1)
                        throw new IllegalArgumentException(Msg.getString(
                                "K01ff", s.substring(i, i + 3), String
                                        .valueOf(i)));
                    out.write((byte) ((d1 << 4) + d2));
                    i += 3;
                } while (i < s.length() && s.charAt(i) == '%');
                result.append(out.toString());
                continue;
            } else
                result.append(c);
            i++;
        }
        return result.toString();
    }
    
    public static String toASCIILowerCase(String s) {
        int len = s.length();
        StringBuilder buffer = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ('A' <= c && c <= 'Z') {
                buffer.append((char) (c + ('a' - 'A')));
            } else {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }
    
    public static String toASCIIUpperCase(String s) {
        int len = s.length();
        StringBuilder buffer = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ('a' <= c && c <= 'z') {
                buffer.append((char) (c - ('a' - 'A')));
            } else {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }
}
