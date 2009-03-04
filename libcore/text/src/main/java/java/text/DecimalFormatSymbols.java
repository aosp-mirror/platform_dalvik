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
/**
*******************************************************************************
* Copyright (C) 1996-2007, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

// BEGIN android-note
// The class javadoc and some of the method descriptions are copied from ICU4J
// source files. Changes have been made to the copied descriptions.
// The icu license header was added to this file. 
// END android-note

package java.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Encapsulates the set of symbols (such as the decimal separator, the grouping
 * separator, and so on) needed by {@code DecimalFormat} to format numbers.
 * {@code DecimalFormat} internally creates an instance of
 * {@code DecimalFormatSymbols} from its locale data. If you need to change any
 * of these symbols, you can get the {@code DecimalFormatSymbols} object from
 * your {@code DecimalFormat} and modify it.
 * 
 * @see java.util.Locale
 * @see DecimalFormat
 * @since Android 1.0
 */
public final class DecimalFormatSymbols implements Cloneable, Serializable {

    private static final long serialVersionUID = 5772796243397350300L;

    private final int ZeroDigit = 0, Digit = 1, DecimalSeparator = 2,
            GroupingSeparator = 3, PatternSeparator = 4, Percent = 5,
            PerMill = 6, Exponent = 7, MonetaryDecimalSeparator = 8,
            MinusSign = 9;

    transient char[] patternChars;

    private transient Currency currency;

    private transient Locale locale;

    private String infinity, NaN, currencySymbol, intlCurrencySymbol;

    /**
     * Constructs a new {@code DecimalFormatSymbols} containing the symbols for
     * the default locale. Best practice is to create a {@code DecimalFormat}
     * and then to get the {@code DecimalFormatSymbols} from that object by
     * calling {@link DecimalFormat#getDecimalFormatSymbols()}.
     * 
     * @since Android 1.0
     */
    public DecimalFormatSymbols() {
        this(Locale.getDefault());
    }

    /**
     * Constructs a new DecimalFormatSymbols containing the symbols for the
     * specified Locale. Best practice is to create a {@code DecimalFormat}
     * and then to get the {@code DecimalFormatSymbols} from that object by
     * calling {@link DecimalFormat#getDecimalFormatSymbols()}.
     * 
     * @param locale
     *            the locale.
     * @since Android 1.0
     */
    public DecimalFormatSymbols(Locale locale) {
        ResourceBundle bundle = Format.getBundle(locale);
        patternChars = bundle.getString("DecimalPatternChars").toCharArray(); //$NON-NLS-1$
        infinity = bundle.getString("Infinity"); //$NON-NLS-1$
        NaN = bundle.getString("NaN"); //$NON-NLS-1$
        this.locale = locale;
        try {
            currency = Currency.getInstance(locale);
            currencySymbol = currency.getSymbol(locale);
            intlCurrencySymbol = currency.getCurrencyCode();
        } catch (IllegalArgumentException e) {
            currency = Currency.getInstance("XXX"); //$NON-NLS-1$
            currencySymbol = bundle.getString("CurrencySymbol"); //$NON-NLS-1$
            intlCurrencySymbol = bundle.getString("IntCurrencySymbol"); //$NON-NLS-1$
        }
    }

    /**
     * Returns a new {@code DecimalFormatSymbols} with the same symbols as this
     * {@code DecimalFormatSymbols}.
     * 
     * @return a shallow copy of this {@code DecimalFormatSymbols}.
     * 
     * @see java.lang.Cloneable
     * @since Android 1.0
     */
    @Override
    public Object clone() {
        try {
            DecimalFormatSymbols symbols = (DecimalFormatSymbols) super.clone();
            symbols.patternChars = patternChars.clone();
            return symbols;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Compares the specified object to this {@code DecimalFormatSymbols} and
     * indicates if they are equal. In order to be equal, {@code object} must be
     * an instance of {@code DecimalFormatSymbols} and contain the same symbols.
     * 
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this
     *         {@code DecimalFormatSymbols}; {@code false} otherwise.
     * @see #hashCode
     * @since Android 1.0
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DecimalFormatSymbols)) {
            return false;
        }
        DecimalFormatSymbols obj = (DecimalFormatSymbols) object;
        return Arrays.equals(patternChars, obj.patternChars)
                && infinity.equals(obj.infinity) && NaN.equals(obj.NaN)
                && currencySymbol.equals(obj.currencySymbol)
                && intlCurrencySymbol.equals(obj.intlCurrencySymbol);
    }

    /**
     * Returns the currency.
     * <p>
     * {@code null} is returned if {@code setInternationalCurrencySymbol()} has
     * been previously called with a value that is not a valid ISO 4217 currency
     * code.
     * <p>
     * 
     * @return the currency that was set in the constructor or by calling
     *         {@code setCurrency()} or {@code setInternationalCurrencySymbol()},
     *         or {@code null} if an invalid currency was set.
     * @see #setCurrency(Currency)
     * @see #setInternationalCurrencySymbol(String)
     * @since Android 1.0
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Returns the international currency symbol.
     * 
     * @return the international currency symbol as string.
     * @since Android 1.0
     */
    public String getInternationalCurrencySymbol() {
        return intlCurrencySymbol;
    }

    /**
     * Returns the currency symbol.
     * 
     * @return the currency symbol as string.
     * @since Android 1.0
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    /**
     * Returns the character which represents the decimal point in a number.
     * 
     * @return the decimal separator character.
     * @since Android 1.0
     */
    public char getDecimalSeparator() {
        return patternChars[DecimalSeparator];
    }

    /**
     * Returns the character which represents a single digit in a format
     * pattern.
     * 
     * @return the digit pattern character.
     * @since Android 1.0
     */
    public char getDigit() {
        return patternChars[Digit];
    }

    /**
     * Returns the character used as the thousands separator in a number.
     * 
     * @return the thousands separator character.
     * @since Android 1.0
     */
    public char getGroupingSeparator() {
        return patternChars[GroupingSeparator];
    }

    /**
     * Returns the string which represents infinity.
     * 
     * @return the infinity symbol as a string.
     * @since Android 1.0
     */
    public String getInfinity() {
        return infinity;
    }

    /**
     * Returns the minus sign character.
     * 
     * @return the minus sign as a character.
     * @since Android 1.0
     */
    public char getMinusSign() {
        return patternChars[MinusSign];
    }

    /**
     * Returns the character which represents the decimal point in a monetary
     * value.
     * 
     * @return the monetary decimal point as a character.
     * @since Android 1.0
     */
    public char getMonetaryDecimalSeparator() {
        return patternChars[MonetaryDecimalSeparator];
    }

    /**
     * Returns the string which represents NaN.
     * 
     * @return the symbol NaN as a string.
     * @since Android 1.0
     */
    public String getNaN() {
        return NaN;
    }

    /**
     * Returns the character which separates the positive and negative patterns
     * in a format pattern.
     * 
     * @return the pattern separator character.
     * @since Android 1.0
     */
    public char getPatternSeparator() {
        return patternChars[PatternSeparator];
    }

    /**
     * Returns the percent character.
     * 
     * @return the percent character.
     * @since Android 1.0
     */
    public char getPercent() {
        return patternChars[Percent];
    }

    /**
     * Returns the per mill sign character.
     * 
     * @return the per mill sign character.
     * @since Android 1.0
     */
    public char getPerMill() {
        return patternChars[PerMill];
    }

    /**
     * Returns the character which represents zero.
     * 
     * @return the zero character.
     * @since Android 1.0
     */
    public char getZeroDigit() {
        return patternChars[ZeroDigit];
    }

    /*
     * Returns the exponent as a character.
     */
    char getExponential() {
        return patternChars[Exponent];
    }

    @Override
    public int hashCode() {
        return new String(patternChars).hashCode() + infinity.hashCode()
                + NaN.hashCode() + currencySymbol.hashCode()
                + intlCurrencySymbol.hashCode();
    }

    /**
     * Sets the currency.
     * <p>
     * The international currency symbol and the currency symbol are updated,
     * but the min and max number of fraction digits stays the same.
     * <p>
     * 
     * @param currency
     *            the new currency.
     * @throws NullPointerException
     *             if {@code currency} is {@code null}.
     * @since Android 1.0
     */
    public void setCurrency(Currency currency) {
        if (currency == null) {
            throw new NullPointerException();
        }
        if (currency == this.currency) {
            return;
        }
        this.currency = currency;
        intlCurrencySymbol = currency.getCurrencyCode();
        currencySymbol = currency.getSymbol(locale);
    }

    /**
     * Sets the international currency symbol.
     * <p>
     * The currency and currency symbol are also updated if {@code value} is a
     * valid ISO4217 currency code.
     * <p>
     * The min and max number of fraction digits stay the same.
     * 
     * @param value
     *            the currency code.
     * @since Android 1.0
     */
    public void setInternationalCurrencySymbol(String value) {
        if (value == null) {
            currency = null;
            intlCurrencySymbol = null;
            return;
        }

        if (value.equals(intlCurrencySymbol)) {
            return;
        }

        try {
            currency = Currency.getInstance(value);
            currencySymbol = currency.getSymbol(locale);
        } catch (IllegalArgumentException e) {
            currency = null;
        }
        intlCurrencySymbol = value;
    }

    /**
     * Sets the currency symbol.
     * 
     * @param value
     *            the currency symbol.
     * @since Android 1.0
     */
    public void setCurrencySymbol(String value) {
        currencySymbol = value;
    }

    /**
     * Sets the character which represents the decimal point in a number.
     * 
     * @param value
     *            the decimal separator character.
     * @since Android 1.0
     */
    public void setDecimalSeparator(char value) {
        patternChars[DecimalSeparator] = value;
    }

    /**
     * Sets the character which represents a single digit in a format pattern.
     * 
     * @param value
     *            the digit character.
     * @since Android 1.0
     */
    public void setDigit(char value) {
        patternChars[Digit] = value;
    }

    /**
     * Sets the character used as the thousands separator in a number.
     * 
     * @param value
     *            the grouping separator character.
     * @since Android 1.0
     */
    public void setGroupingSeparator(char value) {
        patternChars[GroupingSeparator] = value;
    }

    /**
     * Sets the string which represents infinity.
     * 
     * @param value
     *            the string representing infinity.
     * @since Android 1.0
     */
    public void setInfinity(String value) {
        infinity = value;
    }

    /**
     * Sets the minus sign character.
     * 
     * @param value
     *            the minus sign character.
     * @since Android 1.0
     */
    public void setMinusSign(char value) {
        patternChars[MinusSign] = value;
    }

    /**
     * Sets the character which represents the decimal point in a monetary
     * value.
     * 
     * @param value
     *            the monetary decimal separator character.
     * @since Android 1.0
     */
    public void setMonetaryDecimalSeparator(char value) {
        patternChars[MonetaryDecimalSeparator] = value;
    }

    /**
     * Sets the string which represents NaN.
     * 
     * @param value
     *            the string representing NaN.
     * @since Android 1.0
     */
    public void setNaN(String value) {
        NaN = value;
    }

    /**
     * Sets the character which separates the positive and negative patterns in
     * a format pattern.
     * 
     * @param value
     *            the pattern separator character.
     * @since Android 1.0
     */
    public void setPatternSeparator(char value) {
        patternChars[PatternSeparator] = value;
    }

    /**
     * Sets the percent character.
     * 
     * @param value
     *            the percent character.
     * @since Android 1.0
     */
    public void setPercent(char value) {
        patternChars[Percent] = value;
    }

    /**
     * Sets the per mill sign character.
     * 
     * @param value
     *            the per mill character.
     * @since Android 1.0
     */
    public void setPerMill(char value) {
        patternChars[PerMill] = value;
    }

    /**
     * Sets the character which represents zero.
     * 
     * @param value
     *            the zero digit character.
     * @since Android 1.0
     */
    public void setZeroDigit(char value) {
        patternChars[ZeroDigit] = value;
    }

    /*
     * Sets the exponent character.
     */
    void setExponential(char value) {
        patternChars[Exponent] = value;
    }

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("currencySymbol", String.class), //$NON-NLS-1$
            new ObjectStreamField("decimalSeparator", Character.TYPE), //$NON-NLS-1$
            new ObjectStreamField("digit", Character.TYPE), //$NON-NLS-1$
            new ObjectStreamField("exponential", Character.TYPE), //$NON-NLS-1$
            new ObjectStreamField("groupingSeparator", Character.TYPE), //$NON-NLS-1$
            new ObjectStreamField("infinity", String.class), //$NON-NLS-1$
            new ObjectStreamField("intlCurrencySymbol", String.class), //$NON-NLS-1$
            new ObjectStreamField("minusSign", Character.TYPE), //$NON-NLS-1$
            new ObjectStreamField("monetarySeparator", Character.TYPE), //$NON-NLS-1$
            new ObjectStreamField("NaN", String.class), //$NON-NLS-1$
            new ObjectStreamField("patternSeparator", Character.TYPE), //$NON-NLS-1$
            new ObjectStreamField("percent", Character.TYPE), //$NON-NLS-1$
            new ObjectStreamField("perMill", Character.TYPE), //$NON-NLS-1$
            new ObjectStreamField("serialVersionOnStream", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("zeroDigit", Character.TYPE), //$NON-NLS-1$
            new ObjectStreamField("locale", Locale.class), }; //$NON-NLS-1$

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("currencySymbol", currencySymbol); //$NON-NLS-1$
        fields.put("decimalSeparator", getDecimalSeparator()); //$NON-NLS-1$
        fields.put("digit", getDigit()); //$NON-NLS-1$
        fields.put("exponential", getExponential()); //$NON-NLS-1$
        fields.put("groupingSeparator", getGroupingSeparator()); //$NON-NLS-1$
        fields.put("infinity", infinity); //$NON-NLS-1$
        fields.put("intlCurrencySymbol", intlCurrencySymbol); //$NON-NLS-1$
        fields.put("minusSign", getMinusSign()); //$NON-NLS-1$
        fields.put("monetarySeparator", getMonetaryDecimalSeparator()); //$NON-NLS-1$
        fields.put("NaN", NaN); //$NON-NLS-1$
        fields.put("patternSeparator", getPatternSeparator()); //$NON-NLS-1$
        fields.put("percent", getPercent()); //$NON-NLS-1$
        fields.put("perMill", getPerMill()); //$NON-NLS-1$
        fields.put("serialVersionOnStream", 1); //$NON-NLS-1$
        fields.put("zeroDigit", getZeroDigit()); //$NON-NLS-1$
        fields.put("locale", locale); //$NON-NLS-1$
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        patternChars = new char[10];
        currencySymbol = (String) fields.get("currencySymbol", ""); //$NON-NLS-1$ //$NON-NLS-2$
        setDecimalSeparator(fields.get("decimalSeparator", '.')); //$NON-NLS-1$
        setDigit(fields.get("digit", '#')); //$NON-NLS-1$
        setGroupingSeparator(fields.get("groupingSeparator", ',')); //$NON-NLS-1$
        infinity = (String) fields.get("infinity", ""); //$NON-NLS-1$ //$NON-NLS-2$
        intlCurrencySymbol = (String) fields.get("intlCurrencySymbol", ""); //$NON-NLS-1$ //$NON-NLS-2$
        setMinusSign(fields.get("minusSign", '-')); //$NON-NLS-1$
        NaN = (String) fields.get("NaN", ""); //$NON-NLS-1$ //$NON-NLS-2$
        setPatternSeparator(fields.get("patternSeparator", ';')); //$NON-NLS-1$
        setPercent(fields.get("percent", '%')); //$NON-NLS-1$
        setPerMill(fields.get("perMill", '\u2030')); //$NON-NLS-1$
        setZeroDigit(fields.get("zeroDigit", '0')); //$NON-NLS-1$
        locale = (Locale) fields.get("locale", null); //$NON-NLS-1$
        if (fields.get("serialVersionOnStream", 0) == 0) { //$NON-NLS-1$
            setMonetaryDecimalSeparator(getDecimalSeparator());
            setExponential('E');
        } else {
            setMonetaryDecimalSeparator(fields.get("monetarySeparator", '.')); //$NON-NLS-1$
            setExponential(fields.get("exponential", 'E')); //$NON-NLS-1$

        }
        try {
            currency = Currency.getInstance(intlCurrencySymbol);
        } catch (IllegalArgumentException e) {
            currency = null;
        }
    }
}
