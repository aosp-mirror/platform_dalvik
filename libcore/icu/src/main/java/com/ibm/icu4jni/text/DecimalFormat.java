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

import com.ibm.icu4jni.text.NativeDecimalFormat.UNumberFormatAttribute;
import com.ibm.icu4jni.text.NativeDecimalFormat.UNumberFormatTextAttribute;
import com.ibm.icu4jni.util.LocaleData;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.Locale;

public class DecimalFormat {
    // Constants corresponding to the native type UNumberFormatSymbol, for use with getSymbol/setSymbol.
    private static final int UNUM_DECIMAL_SEPARATOR_SYMBOL = 0;
    private static final int UNUM_GROUPING_SEPARATOR_SYMBOL = 1;
    private static final int UNUM_PATTERN_SEPARATOR_SYMBOL = 2;
    private static final int UNUM_PERCENT_SYMBOL = 3;
    private static final int UNUM_ZERO_DIGIT_SYMBOL = 4;
    private static final int UNUM_DIGIT_SYMBOL = 5;
    private static final int UNUM_MINUS_SIGN_SYMBOL = 6;
    private static final int UNUM_PLUS_SIGN_SYMBOL = 7;
    private static final int UNUM_CURRENCY_SYMBOL = 8;
    private static final int UNUM_INTL_CURRENCY_SYMBOL = 9;
    private static final int UNUM_MONETARY_SEPARATOR_SYMBOL = 10;
    private static final int UNUM_EXPONENTIAL_SYMBOL = 11;
    private static final int UNUM_PERMILL_SYMBOL = 12;
    private static final int UNUM_PAD_ESCAPE_SYMBOL = 13;
    private static final int UNUM_INFINITY_SYMBOL = 14;
    private static final int UNUM_NAN_SYMBOL = 15;
    private static final int UNUM_SIGNIFICANT_DIGIT_SYMBOL = 16;
    private static final int UNUM_MONETARY_GROUPING_SEPARATOR_SYMBOL = 17;
    private static final int UNUM_FORMAT_SYMBOL_COUNT = 18;

    // The address of the ICU DecimalFormat* on the native heap.
    private final int addr;

    // TODO: store all these in java.text.DecimalFormat instead!
    private boolean negPrefNull;
    private boolean negSuffNull;
    private boolean posPrefNull;
    private boolean posSuffNull;

    /**
     * Cache the BigDecimal form of the multiplier. This is null until we've
     * formatted a BigDecimal (with a multiplier that is not 1), or the user has
     * explicitly called {@link #setMultiplier(int)} with any multiplier.
     */
    private transient BigDecimal multiplierBigDecimal = null;

    public DecimalFormat(String pattern, Locale locale, DecimalFormatSymbols symbols) {
        this.addr = NativeDecimalFormat.openDecimalFormat(locale.toString(), pattern);
        setDecimalFormatSymbols(symbols);
    }

    // Used to implement clone.
    private DecimalFormat(DecimalFormat other) {
        this.addr = NativeDecimalFormat.cloneDecimalFormatImpl(other.addr);
        this.negPrefNull = other.negPrefNull;
        this.negSuffNull = other.negSuffNull;
        this.posPrefNull = other.posPrefNull;
        this.posSuffNull = other.posSuffNull;
    }

    // TODO: remove this and just have java.text.DecimalFormat.hashCode do the right thing itself.
    @Override
    public int hashCode() {
        return this.getPositivePrefix().hashCode();
    }

    @Override
    public Object clone() {
        return new DecimalFormat(this);
    }

    @Override
    protected void finalize() {
        NativeDecimalFormat.closeDecimalFormatImpl(this.addr);
    }

    /**
     * Note: this doesn't check that the underlying native DecimalFormat objects' configured
     * native DecimalFormatSymbols objects are equal. It is assumed that the
     * caller (java.text.DecimalFormat) will check the java.text.DecimalFormatSymbols objects
     * instead, for performance.
     * 
     * This is also unreasonably expensive, calling down to JNI multiple times.
     * 
     * TODO: remove this and just have java.text.DecimalFormat.equals do the right thing itself.
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DecimalFormat)) {
            return false;
        }
        DecimalFormat obj = (DecimalFormat) object;
        if (obj.addr == this.addr) {
            return true;
        }
        return obj.toPattern().equals(this.toPattern()) &&
                obj.isDecimalSeparatorAlwaysShown() == this.isDecimalSeparatorAlwaysShown() &&
                obj.getGroupingSize() == this.getGroupingSize() &&
                obj.getMultiplier() == this.getMultiplier() &&
                obj.getNegativePrefix().equals(this.getNegativePrefix()) &&
                obj.getNegativeSuffix().equals(this.getNegativeSuffix()) &&
                obj.getPositivePrefix().equals(this.getPositivePrefix()) &&
                obj.getPositiveSuffix().equals(this.getPositiveSuffix()) &&
                obj.getMaximumIntegerDigits() == this.getMaximumIntegerDigits() &&
                obj.getMaximumFractionDigits() == this.getMaximumFractionDigits() &&
                obj.getMinimumIntegerDigits() == this.getMinimumIntegerDigits() &&
                obj.getMinimumFractionDigits() == this.getMinimumFractionDigits() &&
                obj.isGroupingUsed() == this.isGroupingUsed() &&
                obj.getCurrency() == this.getCurrency();
    }

    /**
     * Copies the java.text.DecimalFormatSymbols' settings into our native peer.
     */
    public void setDecimalFormatSymbols(final java.text.DecimalFormatSymbols dfs) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_CURRENCY_SYMBOL, dfs.getCurrencySymbol());

        NativeDecimalFormat.setSymbol(this.addr, UNUM_DECIMAL_SEPARATOR_SYMBOL, dfs.getDecimalSeparator());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_DIGIT_SYMBOL, dfs.getDigit());

        char groupingSeparator = dfs.getGroupingSeparator();
        NativeDecimalFormat.setSymbol(this.addr, UNUM_GROUPING_SEPARATOR_SYMBOL, groupingSeparator);
        NativeDecimalFormat.setSymbol(this.addr, UNUM_MONETARY_GROUPING_SEPARATOR_SYMBOL, groupingSeparator);

        NativeDecimalFormat.setSymbol(this.addr, UNUM_INFINITY_SYMBOL, dfs.getInfinity());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_INTL_CURRENCY_SYMBOL, dfs.getInternationalCurrencySymbol());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_MINUS_SIGN_SYMBOL, dfs.getMinusSign());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_MONETARY_SEPARATOR_SYMBOL, dfs.getMonetaryDecimalSeparator());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_NAN_SYMBOL, dfs.getNaN());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_PATTERN_SEPARATOR_SYMBOL, dfs.getPatternSeparator());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_PERCENT_SYMBOL, dfs.getPercent());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_PERMILL_SYMBOL, dfs.getPerMill());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_ZERO_DIGIT_SYMBOL, dfs.getZeroDigit());
    }

    private BigDecimal applyMultiplier(BigDecimal valBigDecimal) {
       if (multiplierBigDecimal == null) {
           multiplierBigDecimal = BigDecimal.valueOf(getMultiplier());
       }
       // Get new value by multiplying multiplier.
       return valBigDecimal.multiply(multiplierBigDecimal);
    }

    public StringBuffer format(Object value, StringBuffer buffer, FieldPosition field) {
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException();
        }
        if (buffer == null || field == null) {
            throw new NullPointerException();
        }
        String fieldType = getFieldType(field.getFieldAttribute());
        Number number = (Number) value;
        if (number instanceof BigInteger) {
            BigInteger valBigInteger = (BigInteger) number;
            String result = NativeDecimalFormat.format(this.addr, valBigInteger.toString(10),
                    field, fieldType, null, 0);
            return buffer.append(result);
        } else if (number instanceof BigDecimal) {
            BigDecimal valBigDecimal = (BigDecimal) number;
            if (getMultiplier() != 1) {
                valBigDecimal = applyMultiplier(valBigDecimal);
            }
            StringBuilder val = new StringBuilder();
            val.append(valBigDecimal.unscaledValue().toString(10));
            int scale = valBigDecimal.scale();
            scale = makeScalePositive(scale, val);
            String result = NativeDecimalFormat.format(this.addr, val.toString(),
                    field, fieldType, null, scale);
            return buffer.append(result);
        } else if (number instanceof Double || number instanceof Float) {
            double dv = number.doubleValue();
            String result = NativeDecimalFormat.format(this.addr, dv, field, fieldType, null);
            return buffer.append(result);
        } else {
            long lv = number.longValue();
            String result = NativeDecimalFormat.format(this.addr, lv, field, fieldType, null);
            return buffer.append(result);
        }
    }

    public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {
        if (buffer == null || field == null) {
            throw new NullPointerException();
        }
        String fieldType = getFieldType(field.getFieldAttribute());
        buffer.append(NativeDecimalFormat.format(this.addr, value, field, fieldType, null));
        return buffer;
    }

    public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {
        if (buffer == null || field == null) {
            throw new NullPointerException();
        }
        String fieldType = getFieldType(field.getFieldAttribute());
        buffer.append(NativeDecimalFormat.format(this.addr, value, field, fieldType, null));
        return buffer;
    }

    public void applyLocalizedPattern(String pattern) {
        NativeDecimalFormat.applyPattern(this.addr, true, pattern);
    }

    public void applyPattern(String pattern) {
        NativeDecimalFormat.applyPattern(this.addr, false, pattern);
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object object) {
        if (!(object instanceof Number)) {
            throw new IllegalArgumentException();
        }
        Number number = (Number) object;
        String text = null;
        StringBuffer attributes = new StringBuffer();

        if(number instanceof BigInteger) {
            BigInteger valBigInteger = (BigInteger) number;
            text = NativeDecimalFormat.format(this.addr,
                    valBigInteger.toString(10), null, null, attributes, 0);
        } else if(number instanceof BigDecimal) {
            BigDecimal valBigDecimal = (BigDecimal) number;
            if (getMultiplier() != 1) {
                valBigDecimal = applyMultiplier(valBigDecimal);
            }
            StringBuilder val = new StringBuilder();
            val.append(valBigDecimal.unscaledValue().toString(10));
            int scale = valBigDecimal.scale();
            scale = makeScalePositive(scale, val);
            text = NativeDecimalFormat.format(this.addr, val.toString(), null,
                    null, attributes, scale);
        } else if (number instanceof Double || number instanceof Float) {
            double dv = number.doubleValue();
            text = NativeDecimalFormat.format(this.addr, dv, null, null, attributes);
        } else {
            long lv = number.longValue();
            text = NativeDecimalFormat.format(this.addr, lv, null, null, attributes);
        }

        AttributedString as = new AttributedString(text);

        String[] attrs = attributes.toString().split(";");
        // add NumberFormat field attributes to the AttributedString
        int size = attrs.length / 3;
        if(size * 3 != attrs.length) {
            return as.getIterator();
        }
        for (int i = 0; i < size; i++) {
            Format.Field attribute = getField(attrs[3*i]);
            as.addAttribute(attribute, attribute, Integer.parseInt(attrs[3*i+1]),
                    Integer.parseInt(attrs[3*i+2]));
        }

        // return the CharacterIterator from AttributedString
        return as.getIterator();
    }

    private int makeScalePositive(int scale, StringBuilder val) {
        if (scale < 0) {
            scale = -scale;
            for (int i = scale; i > 0; i--) {
                val.append('0');
            }
            scale = 0;
        }
        return scale;
    }

    public String toLocalizedPattern() {
        return NativeDecimalFormat.toPatternImpl(this.addr, true);
    }

    public String toPattern() {
        return NativeDecimalFormat.toPatternImpl(this.addr, false);
    }

    public Number parse(String string, ParsePosition position) {
        return NativeDecimalFormat.parse(addr, string, position);
    }

    // start getter and setter

    public int getMaximumFractionDigits() {
        return NativeDecimalFormat.getAttribute(this.addr,
                UNumberFormatAttribute.UNUM_MAX_FRACTION_DIGITS.ordinal());
    }

    public int getMaximumIntegerDigits() {
        return NativeDecimalFormat.getAttribute(this.addr,
                UNumberFormatAttribute.UNUM_MAX_INTEGER_DIGITS.ordinal());
    }

    public int getMinimumFractionDigits() {
        return NativeDecimalFormat.getAttribute(this.addr,
                UNumberFormatAttribute.UNUM_MIN_FRACTION_DIGITS.ordinal());
    }

    public int getMinimumIntegerDigits() {
        return NativeDecimalFormat.getAttribute(this.addr,
                UNumberFormatAttribute.UNUM_MIN_INTEGER_DIGITS.ordinal());
    }

    public Currency getCurrency() {
        String curr = NativeDecimalFormat.getSymbol(this.addr, UNUM_INTL_CURRENCY_SYMBOL);
        if (curr.equals("") || curr.equals("\u00a4\u00a4")) {
            return null;
        }
        return Currency.getInstance(curr);
    }

    public int getGroupingSize() {
        return NativeDecimalFormat.getAttribute(this.addr,
                UNumberFormatAttribute.UNUM_GROUPING_SIZE.ordinal());
    }

    public int getMultiplier() {
        return NativeDecimalFormat.getAttribute(this.addr,
                UNumberFormatAttribute.UNUM_MULTIPLIER.ordinal());
    }

    public String getNegativePrefix() {
        if (negPrefNull) {
            return null;
        }
        return NativeDecimalFormat.getTextAttribute(this.addr,
                UNumberFormatTextAttribute.UNUM_NEGATIVE_PREFIX.ordinal());
    }

    public String getNegativeSuffix() {
        if (negSuffNull) {
            return null;
        }
        return NativeDecimalFormat.getTextAttribute(this.addr,
                UNumberFormatTextAttribute.UNUM_NEGATIVE_SUFFIX.ordinal());
    }

    public String getPositivePrefix() {
        if (posPrefNull) {
            return null;
        }
        return NativeDecimalFormat.getTextAttribute(this.addr,
                UNumberFormatTextAttribute.UNUM_POSITIVE_PREFIX.ordinal());
    }

    public String getPositiveSuffix() {
        if (posSuffNull) {
            return null;
        }
        return NativeDecimalFormat.getTextAttribute(this.addr,
                UNumberFormatTextAttribute.UNUM_POSITIVE_SUFFIX.ordinal());
    }

    public boolean isDecimalSeparatorAlwaysShown() {
        return NativeDecimalFormat.getAttribute(this.addr,
                UNumberFormatAttribute.UNUM_DECIMAL_ALWAYS_SHOWN.ordinal()) != 0;
    }

    public boolean isParseIntegerOnly() {
        return NativeDecimalFormat.getAttribute(this.addr,
                UNumberFormatAttribute.UNUM_PARSE_INT_ONLY.ordinal()) != 0;
    }

    public boolean isGroupingUsed() {
        return NativeDecimalFormat.getAttribute(this.addr,
                UNumberFormatAttribute.UNUM_GROUPING_USED.ordinal()) != 0;
    }

    public void setDecimalSeparatorAlwaysShown(boolean value) {
        int i = value ? -1 : 0;
        NativeDecimalFormat.setAttribute(this.addr,
                UNumberFormatAttribute.UNUM_DECIMAL_ALWAYS_SHOWN.ordinal(), i);
    }

    public void setCurrency(Currency currency) {
        NativeDecimalFormat.setSymbol(this.addr, UNUM_CURRENCY_SYMBOL, currency.getSymbol());
        NativeDecimalFormat.setSymbol(this.addr, UNUM_INTL_CURRENCY_SYMBOL, currency.getCurrencyCode());
    }

    public void setGroupingSize(int value) {
        NativeDecimalFormat.setAttribute(this.addr,
                UNumberFormatAttribute.UNUM_GROUPING_SIZE.ordinal(), value);
    }

    public void setGroupingUsed(boolean value) {
        int i = value ? -1 : 0;
        NativeDecimalFormat.setAttribute(this.addr,
                UNumberFormatAttribute.UNUM_GROUPING_USED.ordinal(), i);
    }

    public void setMaximumFractionDigits(int value) {
        NativeDecimalFormat.setAttribute(this.addr,
                UNumberFormatAttribute.UNUM_MAX_FRACTION_DIGITS.ordinal(), value);
    }

    public void setMaximumIntegerDigits(int value) {
        NativeDecimalFormat.setAttribute(this.addr,
                UNumberFormatAttribute.UNUM_MAX_INTEGER_DIGITS.ordinal(), value);
    }

    public void setMinimumFractionDigits(int value) {
        NativeDecimalFormat.setAttribute(this.addr,
                UNumberFormatAttribute.UNUM_MIN_FRACTION_DIGITS.ordinal(), value);
    }

    public void setMinimumIntegerDigits(int value) {
        NativeDecimalFormat.setAttribute(this.addr,
                UNumberFormatAttribute.UNUM_MIN_INTEGER_DIGITS.ordinal(), value);
    }

    public void setMultiplier(int value) {
        NativeDecimalFormat.setAttribute(this.addr,
                UNumberFormatAttribute.UNUM_MULTIPLIER.ordinal(), value);
        // Update the cached BigDecimal for multiplier.
        multiplierBigDecimal = BigDecimal.valueOf(value);
    }

    public void setNegativePrefix(String value) {
        negPrefNull = value == null;
        if (!negPrefNull) {
            NativeDecimalFormat.setTextAttribute(this.addr,
                    UNumberFormatTextAttribute.UNUM_NEGATIVE_PREFIX.ordinal(),
                    value);
        }
    }

    public void setNegativeSuffix(String value) {
        negSuffNull = value == null;
        if (!negSuffNull) {
            NativeDecimalFormat.setTextAttribute(this.addr,
                    UNumberFormatTextAttribute.UNUM_NEGATIVE_SUFFIX.ordinal(),
                    value);
        }
    }

    public void setPositivePrefix(String value) {
        posPrefNull = value == null;
        if (!posPrefNull) {
            NativeDecimalFormat.setTextAttribute(this.addr,
                    UNumberFormatTextAttribute.UNUM_POSITIVE_PREFIX.ordinal(),
                    value);
        }
    }

    public void setPositiveSuffix(String value) {
        posSuffNull = value == null;
        if (!posSuffNull) {
            NativeDecimalFormat.setTextAttribute(this.addr,
                    UNumberFormatTextAttribute.UNUM_POSITIVE_SUFFIX.ordinal(),
                    value);
        }
    }

    public void setParseIntegerOnly(boolean value) {
        int i = value ? -1 : 0;
        NativeDecimalFormat.setAttribute(this.addr,
                UNumberFormatAttribute.UNUM_PARSE_INT_ONLY.ordinal(), i);
    }

    static protected String getFieldType(Format.Field field) {
        if(field == null) {
            return null;
        }
        if(field.equals(NumberFormat.Field.SIGN)) {
            return "sign";
        }
        if(field.equals(NumberFormat.Field.INTEGER)) {
            return "integer";
        }
        if(field.equals(NumberFormat.Field.FRACTION)) {
            return "fraction";
        }
        if(field.equals(NumberFormat.Field.EXPONENT)) {
            return "exponent";
        }
        if(field.equals(NumberFormat.Field.EXPONENT_SIGN)) {
            return "exponent_sign";
        }
        if(field.equals(NumberFormat.Field.EXPONENT_SYMBOL)) {
            return "exponent_symbol";
        }
        if(field.equals(NumberFormat.Field.CURRENCY)) {
            return "currency";
        }
        if(field.equals(NumberFormat.Field.GROUPING_SEPARATOR)) {
            return "grouping_separator";
        }
        if(field.equals(NumberFormat.Field.DECIMAL_SEPARATOR)) {
            return "decimal_separator";
        }
        if(field.equals(NumberFormat.Field.PERCENT)) {
            return "percent";
        }
        if(field.equals(NumberFormat.Field.PERMILLE)) {
            return "permille";
        }
        return null;
    }

    protected Format.Field getField(String type) {
        if(type.equals("")) {
            return null;
        }
        if(type.equals("sign")) {
            return NumberFormat.Field.SIGN;
        }
        if(type.equals("integer")) {
            return NumberFormat.Field.INTEGER;
        }
        if(type.equals("fraction")) {
            return NumberFormat.Field.FRACTION;
        }
        if(type.equals("exponent")) {
            return NumberFormat.Field.EXPONENT;
        }
        if(type.equals("exponent_sign")) {
            return NumberFormat.Field.EXPONENT_SIGN;
        }
        if(type.equals("exponent_symbol")) {
            return NumberFormat.Field.EXPONENT_SYMBOL;
        }
        if(type.equals("currency")) {
            return NumberFormat.Field.CURRENCY;
        }
        if(type.equals("grouping_separator")) {
            return NumberFormat.Field.GROUPING_SEPARATOR;
        }
        if(type.equals("decimal_separator")) {
            return NumberFormat.Field.DECIMAL_SEPARATOR;
        }
        if(type.equals("percent")) {
            return NumberFormat.Field.PERCENT;
        }
        if(type.equals("permille")) {
            return NumberFormat.Field.PERMILLE;
        }
        return null;
    }
}
