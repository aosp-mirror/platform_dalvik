/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.ibm.icu4jni.util;

/**
 * Passes locale-specific from ICU native code to Java.
 * <p>
 * Note that you share these; you must not alter any of the fields, nor their array elements
 * in the case of arrays. If you ever expose any of these things to user code, you must give
 * them a clone rather than the original.
 */
public class LocaleData {
    public Integer firstDayOfWeek;
    public Integer minimalDaysInFirstWeek;
    
    public String[] amPm;
    
    public String[] eras;
    
    public String[] longMonthNames;
    public String[] shortMonthNames;
    
    public String[] longWeekdayNames;
    public String[] shortWeekdayNames;
    
    public String fullTimeFormat;
    public String longTimeFormat;
    public String mediumTimeFormat;
    public String shortTimeFormat;
    
    public String fullDateFormat;
    public String longDateFormat;
    public String mediumDateFormat;
    public String shortDateFormat;
    
    public String decimalPatternChars;
    
    public String infinity;
    public String NaN;
    
    public String currencySymbol;
    public String internationalCurrencySymbol;
    
    public String numberPattern;
    public String integerPattern;
    public String currencyPattern;
    public String percentPattern;
    
    @Override public String toString() {
        return "LocaleData[" +
                "firstDayOfWeek=" + firstDayOfWeek + "," +
                "minimalDaysInFirstWeek=" + minimalDaysInFirstWeek + "," +
                "amPm=" + amPm + "," +
                "eras=" + eras + "," +
                "longMonthNames=" + longMonthNames + "," +
                "shortMonthNames=" + shortMonthNames + "," +
                "longWeekdayNames=" + longWeekdayNames + "," +
                "shortWeekdayNames=" + shortWeekdayNames + "," +
                "fullTimeFormat=" + fullTimeFormat + "," +
                "longTimeFormat=" + longTimeFormat + "," +
                "mediumTimeFormat=" + mediumTimeFormat + "," +
                "shortTimeFormat=" + shortTimeFormat + "," +
                "fullDateFormat=" + fullDateFormat + "," +
                "longDateFormat=" + longDateFormat + "," +
                "mediumDateFormat=" + mediumDateFormat + "," +
                "shortDateFormat=" + shortDateFormat + "," +
                "decimalPatternChars=" + decimalPatternChars + "," +
                "infinity=" + infinity + "," +
                "NaN=" + NaN + "," +
                "currencySymbol=" + currencySymbol + "," +
                "internationalCurrencySymbol=" + internationalCurrencySymbol + "," +
                "numberPattern=" + numberPattern + "," +
                "integerPattern=" + integerPattern + "," +
                "currencyPattern=" + currencyPattern + "," +
                "percentPattern=" + percentPattern + "]";
    }
    
    public void overrideWithDataFrom(LocaleData overrides) {
        if (overrides.firstDayOfWeek != null) {
            firstDayOfWeek = overrides.firstDayOfWeek;
        }
        if (overrides.minimalDaysInFirstWeek != null) {
            minimalDaysInFirstWeek = overrides.minimalDaysInFirstWeek;
        }
        if (overrides.amPm != null) {
            amPm = overrides.amPm;
        }
        if (overrides.eras != null) {
            eras = overrides.eras;
        }
        if (overrides.longMonthNames != null) {
            longMonthNames = overrides.longMonthNames;
        }
        if (overrides.shortMonthNames != null) {
            shortMonthNames = overrides.shortMonthNames;
        }
        if (overrides.longWeekdayNames != null) {
            longWeekdayNames = overrides.longWeekdayNames;
        }
        if (overrides.shortWeekdayNames != null) {
            shortWeekdayNames = overrides.shortWeekdayNames;
        }
        if (overrides.fullTimeFormat != null) {
            fullTimeFormat = overrides.fullTimeFormat;
        }
        if (overrides.longTimeFormat != null) {
            longTimeFormat = overrides.longTimeFormat;
        }
        if (overrides.mediumTimeFormat != null) {
            mediumTimeFormat = overrides.mediumTimeFormat;
        }
        if (overrides.shortTimeFormat != null) {
            shortTimeFormat = overrides.shortTimeFormat;
        }
        if (overrides.fullDateFormat != null) {
            fullDateFormat = overrides.fullDateFormat;
        }
        if (overrides.longDateFormat != null) {
            longDateFormat = overrides.longDateFormat;
        }
        if (overrides.mediumDateFormat != null) {
            mediumDateFormat = overrides.mediumDateFormat;
        }
        if (overrides.shortDateFormat != null) {
            shortDateFormat = overrides.shortDateFormat;
        }
        if (overrides.decimalPatternChars != null) {
            decimalPatternChars = overrides.decimalPatternChars;
        }
        if (overrides.NaN != null) {
            NaN = overrides.NaN;
        }
        if (overrides.infinity != null) {
            infinity = overrides.infinity;
        }
        if (overrides.currencySymbol != null) {
            currencySymbol = overrides.currencySymbol;
        }
        if (overrides.internationalCurrencySymbol != null) {
            internationalCurrencySymbol = overrides.internationalCurrencySymbol;
        }
        if (overrides.numberPattern != null) {
            numberPattern = overrides.numberPattern;
        }
        if (overrides.integerPattern != null) {
            integerPattern = overrides.integerPattern;
        }
        if (overrides.currencyPattern != null) {
            currencyPattern = overrides.currencyPattern;
        }
        if (overrides.percentPattern != null) {
            percentPattern = overrides.percentPattern;
        }
    }
}
