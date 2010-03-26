/**
*******************************************************************************
* Copyright (C) 1996-2005, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/

package com.ibm.icu4jni.text;

/**
 * TODO: move these constants into NativeCollation.
 */
public final class CollationAttribute {
    // Values from the native UColAttributeValue enum.
    public static final int VALUE_DEFAULT = -1;
    public static final int VALUE_PRIMARY = 0;
    public static final int VALUE_SECONDARY = 1;
    public static final int VALUE_TERTIARY = 2;
    public static final int VALUE_DEFAULT_STRENGTH = VALUE_TERTIARY;
    public static final int VALUE_QUATERNARY = 3;
    public static final int VALUE_IDENTICAL = 15;
    public static final int VALUE_OFF = 16;
    public static final int VALUE_ON = 17;
    public static final int VALUE_SHIFTED = 20;
    public static final int VALUE_NON_IGNORABLE = 21;
    public static final int VALUE_LOWER_FIRST = 24;
    public static final int VALUE_UPPER_FIRST = 25;
    public static final int VALUE_ON_WITHOUT_HANGUL = 28;
    public static final int VALUE_ATTRIBUTE_VALUE_COUNT = 29;
    // Values from the UColAttribute enum.
    public static final int FRENCH_COLLATION = 0;
    public static final int ALTERNATE_HANDLING = 1;
    public static final int CASE_FIRST = 2;
    public static final int CASE_LEVEL = 3;
    public static final int NORMALIZATION_MODE = 4;
    public static final int DECOMPOSITION_MODE = NORMALIZATION_MODE;
    public static final int STRENGTH = 5;
}
