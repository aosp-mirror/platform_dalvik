/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.icu4jni.text;

import java.text.FieldPosition;
import java.text.ParsePosition;

final class NativeDecimalFormat {

    enum UNumberFormatAttribute {
        UNUM_PARSE_INT_ONLY, 
        UNUM_GROUPING_USED, 
        UNUM_DECIMAL_ALWAYS_SHOWN, 
        UNUM_MAX_INTEGER_DIGITS,
        UNUM_MIN_INTEGER_DIGITS, 
        UNUM_INTEGER_DIGITS, 
        UNUM_MAX_FRACTION_DIGITS, 
        UNUM_MIN_FRACTION_DIGITS,
        UNUM_FRACTION_DIGITS,
        UNUM_MULTIPLIER, 
        UNUM_GROUPING_SIZE, 
        UNUM_ROUNDING_MODE,
        UNUM_ROUNDING_INCREMENT, 
        UNUM_FORMAT_WIDTH, 
        UNUM_PADDING_POSITION, 
        UNUM_SECONDARY_GROUPING_SIZE,
        UNUM_SIGNIFICANT_DIGITS_USED, 
        UNUM_MIN_SIGNIFICANT_DIGITS, 
        UNUM_MAX_SIGNIFICANT_DIGITS, 
        UNUM_LENIENT_PARSE
    }

    enum UNumberFormatTextAttribute {
        UNUM_POSITIVE_PREFIX, 
        UNUM_POSITIVE_SUFFIX, 
        UNUM_NEGATIVE_PREFIX, 
        UNUM_NEGATIVE_SUFFIX,
        UNUM_PADDING_CHARACTER, 
        UNUM_CURRENCY_CODE, 
        UNUM_DEFAULT_RULESET, 
        UNUM_PUBLIC_RULESETS
    }
    
    static int openDecimalFormat(String locale, String pattern) {
        try {
            // FIXME: if we're about to override everything, should we just ask for the cheapest locale (presumably the root locale)?
            return NativeDecimalFormat.openDecimalFormatImpl(locale, pattern);
        } catch (NullPointerException npe) {
            throw npe;
        } catch (RuntimeException re) {
            throw new IllegalArgumentException("syntax error: " + re.getMessage() + ": " + pattern);
        }
    }
    private static native int openDecimalFormatImpl(String locale, String pattern);

    static native void closeDecimalFormatImpl(int addr);
    
    static native int cloneDecimalFormatImpl(int addr);
    
    static native void setSymbol(int addr, int symbol, String str);
    static native void setSymbol(int addr, int symbol, char ch);
    
    // FIXME: do we need this any more? the Java-side object should be the canonical source.
    static native String getSymbol(int addr, int symbol);
    
    static native void setAttribute(int addr, int symbol, int i);
    
    static native int getAttribute(int addr, int symbol);

    static native void setTextAttribute(int addr, int symbol, String str);

    static native String getTextAttribute(int addr, int symbol);

    static void applyPattern(int addr, boolean localized, String pattern) {
        try {
            NativeDecimalFormat.applyPatternImpl(addr, localized, pattern);
        } catch (NullPointerException npe) {
            throw npe;
        } catch (RuntimeException re) {
            throw new IllegalArgumentException("syntax error: " + re.getMessage() + ": " + pattern);
        }
    }
    private static native void applyPatternImpl(int addr, boolean localized, String pattern);

    static native String toPatternImpl(int addr, boolean localized);
    
    static native String format(int addr, long value, FieldPosition position, String fieldType, StringBuffer attributes);

    static native String format(int addr, double value, FieldPosition position, String fieldType, StringBuffer attributes);

    static native String format(int addr, String value, FieldPosition position, String fieldType, StringBuffer attributes, int scale);
    
    static native Number parse(int addr, String string, ParsePosition position);
}
