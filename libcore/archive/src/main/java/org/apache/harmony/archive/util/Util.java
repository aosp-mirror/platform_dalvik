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

package org.apache.harmony.archive.util;

public class Util {

    public static boolean ASCIIIgnoreCaseRegionMatches(String s1, int start1,
            String s2, int start2, int length) {

        if (s1 != null && s2 != null) {
            if (start1 < 0 || length > s1.length() - start1) {
                return false;
            }
            if (start2 < 0 || length > s2.length() - start2) {
                return false;
            }

            char c1, c2;
            for (int i = 0; i < length; i++) {
                if ((c1 = s1.charAt(start1++)) != (c2 = s2.charAt(start2++))
                        && toASCIIUpperCase(c1) != toASCIIUpperCase(c2)) {
                    return false;
                }
            }
            return true;
        }
        throw new NullPointerException();
    }

    public static final boolean equalsIgnoreCase(byte[] buf1, byte[] buf2) {
        if (buf1 == buf2) {
            return true;
        }

        if (buf1 == null || buf2 == null || buf1.length != buf2.length) {
            return false;
        }

        byte b1, b2;

        for (int i = 0; i < buf1.length; i++) {
            if ((b1 = buf1[i]) != (b2 = buf2[i])
                    && toASCIIUpperCase(b1) != toASCIIUpperCase(b2)) {
                return false;
            }
        }
        return true;
    }

    static final char toASCIIUpperCase(char c) {
        if ('a' <= c && c <= 'z') {
            return (char) (c - ('a' - 'A'));
        }
        return c;
    }

    static final byte toASCIIUpperCase(byte b) {
        if ('a' <= b && b <= 'z') {
            return (byte) (b - ('a' - 'A'));
        }
        return b;
    }
}
