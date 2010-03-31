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

package java.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

import com.ibm.icu4jni.util.LocaleData;

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
 */
public final class DecimalFormatSymbols implements Cloneable, Serializable {

    private static final long serialVersionUID = 5772796243397350300L;

    // Indexes into the patternChars array.
    private static final int ZERO_DIGIT = 0;
    private static final int DIGIT = 1;
    private static final int DECIMAL_SEPARATOR = 2;
    private static final int GROUPING_SEPARATOR = 3;
    private static final int PATTERN_SEPARATOR = 4;
    private static final int PERCENT = 5;
    private static final int PER_MILL = 6;
    private static final int MONETARY_DECIMAL_SEPARATOR = 7;
    private static final int MINUS_SIGN = 8;

    // TODO: replace this with individual char fields.
    private transient char[] patternChars;

    private transient Currency currency;
    private transient Locale locale;
    private transient String exponentSeparator;

    private String infinity, NaN, currencySymbol, intlCurrencySymbol;

    /**
     * Constructs a new {@code DecimalFormatSymbols} containing the symbols for
     * the default locale. Best practice is to create a {@code DecimalFormat}
     * and then to get the {@code DecimalFormatSymbols} from that object by
     * calling {@link DecimalFormat#getDecimalFormatSymbols()}.
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
     */
    public DecimalFormatSymbols(Locale locale) {
        // BEGIN android-changed
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        this.patternChars = localeData.decimalPatternChars.toCharArray();
        this.infinity = localeData.infinity;
        this.NaN = localeData.NaN;
        this.exponentSeparator = localeData.exponentSeparator;
        this.locale = locale;
        try {
            currency = Currency.getInstance(locale);
            currencySymbol = currency.getSymbol(locale);
            intlCurrencySymbol = currency.getCurrencyCode();
        } catch (IllegalArgumentException e) {
            currency = Currency.getInstance("XXX");
            currencySymbol = localeData.currencySymbol;
            intlCurrencySymbol = localeData.internationalCurrencySymbol;
        }
        // END android-changed
    }

    /**
     * Returns a new {@code DecimalFormatSymbols} instance for the default locale.
     *
     * @return an instance of {@code DecimalFormatSymbols}
     * @since 1.6
     * @hide
     */
    public static final DecimalFormatSymbols getInstance() {
        return getInstance(Locale.getDefault());
    }

    /**
     * Returns a new {@code DecimalFormatSymbols} for the given locale.
     *
     * @param locale the locale
     * @return an instance of {@code DecimalFormatSymbols}
     * @throws NullPointerException if {@code locale == null}
     * @since 1.6
     * @hide
     */
    public static final DecimalFormatSymbols getInstance(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }
        return new DecimalFormatSymbols(locale);
    }

    /**
     * Returns an array of locales for which custom {@code DecimalFormatSymbols} instances
     * are available.
     * @since 1.6
     * @hide
     */
    public static Locale[] getAvailableLocales() {
        return Locale.getAvailableLocales();
    }

    /**
     * Returns a new {@code DecimalFormatSymbols} with the same symbols as this
     * {@code DecimalFormatSymbols}.
     * 
     * @return a shallow copy of this {@code DecimalFormatSymbols}.
     * 
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            DecimalFormatSymbols symbols = (DecimalFormatSymbols) super.clone();
            symbols.patternChars = patternChars.clone();
            return symbols;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // android-changed
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
        return currency.equals(obj.currency) &&
                currencySymbol.equals(obj.currencySymbol) &&
                patternChars[DECIMAL_SEPARATOR] == obj.patternChars[DECIMAL_SEPARATOR] &&
                patternChars[DIGIT] == obj.patternChars[DIGIT] &&
                exponentSeparator.equals(obj.exponentSeparator) &&
                patternChars[GROUPING_SEPARATOR] == obj.patternChars[GROUPING_SEPARATOR] &&
                infinity.equals(obj.infinity) &&
                intlCurrencySymbol.equals(obj.intlCurrencySymbol) &&
                patternChars[MINUS_SIGN] == obj.patternChars[MINUS_SIGN] &&
                patternChars[MONETARY_DECIMAL_SEPARATOR] == obj.patternChars[MONETARY_DECIMAL_SEPARATOR] &&
                NaN.equals(obj.NaN) &&
                patternChars[PATTERN_SEPARATOR] == obj.patternChars[PATTERN_SEPARATOR] &&
                patternChars[PER_MILL] == obj.patternChars[PER_MILL] &&
                patternChars[PERCENT] == obj.patternChars[PERCENT] &&
                patternChars[ZERO_DIGIT] == obj.patternChars[ZERO_DIGIT];
    }

    @Override
    public String toString() {
        return getClass().getName() +
                "[currency=" + currency +
                ",currencySymbol=" + currencySymbol +
                ",decimalSeparator=" + patternChars[DECIMAL_SEPARATOR] +
                ",digit=" + patternChars[DIGIT] +
                ",exponentSeparator=" + exponentSeparator +
                ",groupingSeparator=" + patternChars[GROUPING_SEPARATOR] +
                ",infinity=" + infinity +
                ",intlCurrencySymbol=" + intlCurrencySymbol +
                ",minusSign=" + patternChars[MINUS_SIGN] +
                ",monetaryDecimalSeparator=" + patternChars[MONETARY_DECIMAL_SEPARATOR] +
                ",NaN=" + NaN +
                ",patternSeparator=" + patternChars[PATTERN_SEPARATOR] +
                ",perMill=" + patternChars[PER_MILL] +
                ",percent=" + patternChars[PERCENT] +
                ",zeroDigit=" + patternChars[ZERO_DIGIT] +
                "]";
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
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Returns the international currency symbol.
     * 
     * @return the international currency symbol as string.
     */
    public String getInternationalCurrencySymbol() {
        return intlCurrencySymbol;
    }

    /**
     * Returns the currency symbol.
     * 
     * @return the currency symbol as string.
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    /**
     * Returns the character which represents the decimal point in a number.
     * 
     * @return the decimal separator character.
     */
    public char getDecimalSeparator() {
        return patternChars[DECIMAL_SEPARATOR];
    }

    /**
     * Returns the character which represents a single digit in a format
     * pattern.
     * 
     * @return the digit pattern character.
     */
    public char getDigit() {
        return patternChars[DIGIT];
    }

    /**
     * Returns the character used as the thousands separator in a number.
     * 
     * @return the thousands separator character.
     */
    public char getGroupingSeparator() {
        return patternChars[GROUPING_SEPARATOR];
    }

    /**
     * Returns the string which represents infinity.
     * 
     * @return the infinity symbol as a string.
     */
    public String getInfinity() {
        return infinity;
    }

    /**
     * Returns the minus sign character.
     * 
     * @return the minus sign as a character.
     */
    public char getMinusSign() {
        return patternChars[MINUS_SIGN];
    }

    /**
     * Returns the character which represents the decimal point in a monetary
     * value.
     * 
     * @return the monetary decimal point as a character.
     */
    public char getMonetaryDecimalSeparator() {
        return patternChars[MONETARY_DECIMAL_SEPARATOR];
    }

    /**
     * Returns the string which represents NaN.
     * 
     * @return the symbol NaN as a string.
     */
    public String getNaN() {
        return NaN;
    }

    /**
     * Returns the character which separates the positive and negative patterns
     * in a format pattern.
     * 
     * @return the pattern separator character.
     */
    public char getPatternSeparator() {
        return patternChars[PATTERN_SEPARATOR];
    }

    /**
     * Returns the percent character.
     * 
     * @return the percent character.
     */
    public char getPercent() {
        return patternChars[PERCENT];
    }

    /**
     * Returns the per mill sign character.
     * 
     * @return the per mill sign character.
     */
    public char getPerMill() {
        return patternChars[PER_MILL];
    }

    /**
     * Returns the character which represents zero.
     * 
     * @return the zero character.
     */
    public char getZeroDigit() {
        return patternChars[ZERO_DIGIT];
    }

    /*
     * Returns the string used to separate mantissa and exponent. Typically "E", as in "1.2E3".
     * @since 1.6
     * @hide
     */
    public String getExponentSeparator() {
        return exponentSeparator;
    }

    @Override
    public int hashCode() {
        return new String(patternChars).hashCode() + exponentSeparator.hashCode() +
                infinity.hashCode() + NaN.hashCode() + currencySymbol.hashCode() +
                intlCurrencySymbol.hashCode();
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
     */
    public void setCurrencySymbol(String value) {
        currencySymbol = value;
    }

    /**
     * Sets the character which represents the decimal point in a number.
     * 
     * @param value
     *            the decimal separator character.
     */
    public void setDecimalSeparator(char value) {
        patternChars[DECIMAL_SEPARATOR] = value;
    }

    /**
     * Sets the character which represents a single digit in a format pattern.
     * 
     * @param value
     *            the digit character.
     */
    public void setDigit(char value) {
        patternChars[DIGIT] = value;
    }

    /**
     * Sets the character used as the thousands separator in a number.
     * 
     * @param value
     *            the grouping separator character.
     */
    public void setGroupingSeparator(char value) {
        patternChars[GROUPING_SEPARATOR] = value;
    }

    /**
     * Sets the string which represents infinity.
     * 
     * @param value
     *            the string representing infinity.
     */
    public void setInfinity(String value) {
        infinity = value;
    }

    /**
     * Sets the minus sign character.
     * 
     * @param value
     *            the minus sign character.
     */
    public void setMinusSign(char value) {
        patternChars[MINUS_SIGN] = value;
    }

    /**
     * Sets the character which represents the decimal point in a monetary
     * value.
     * 
     * @param value
     *            the monetary decimal separator character.
     */
    public void setMonetaryDecimalSeparator(char value) {
        patternChars[MONETARY_DECIMAL_SEPARATOR] = value;
    }

    /**
     * Sets the string which represents NaN.
     * 
     * @param value
     *            the string representing NaN.
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
     */
    public void setPatternSeparator(char value) {
        patternChars[PATTERN_SEPARATOR] = value;
    }

    /**
     * Sets the percent character.
     * 
     * @param value
     *            the percent character.
     */
    public void setPercent(char value) {
        patternChars[PERCENT] = value;
    }

    /**
     * Sets the per mill sign character.
     * 
     * @param value
     *            the per mill character.
     */
    public void setPerMill(char value) {
        patternChars[PER_MILL] = value;
    }

    /**
     * Sets the character which represents zero.
     * 
     * @param value
     *            the zero digit character.
     */
    public void setZeroDigit(char value) {
        patternChars[ZERO_DIGIT] = value;
    }

    /**
     * Sets the string used to separate mantissa and exponent. Typically "E", as in "1.2E3".
     * @since 1.6
     * @hide
     */
    public void setExponentSeparator(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
        this.exponentSeparator = value;
    }

    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("currencySymbol", String.class),
        new ObjectStreamField("decimalSeparator", Character.TYPE),
        new ObjectStreamField("digit", Character.TYPE),
        new ObjectStreamField("exponential", Character.TYPE),
        new ObjectStreamField("exponentialSeparator", String.class),
        new ObjectStreamField("groupingSeparator", Character.TYPE),
        new ObjectStreamField("infinity", String.class),
        new ObjectStreamField("intlCurrencySymbol", String.class),
        new ObjectStreamField("minusSign", Character.TYPE),
        new ObjectStreamField("monetarySeparator", Character.TYPE),
        new ObjectStreamField("NaN", String.class),
        new ObjectStreamField("patternSeparator", Character.TYPE),
        new ObjectStreamField("percent", Character.TYPE),
        new ObjectStreamField("perMill", Character.TYPE),
        new ObjectStreamField("serialVersionOnStream", Integer.TYPE),
        new ObjectStreamField("zeroDigit", Character.TYPE),
        new ObjectStreamField("locale", Locale.class),
    };

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("currencySymbol", currencySymbol);
        fields.put("decimalSeparator", getDecimalSeparator());
        fields.put("digit", getDigit());
        fields.put("exponential", exponentSeparator.charAt(0));
        fields.put("exponentialSeparator", exponentSeparator);
        fields.put("groupingSeparator", getGroupingSeparator());
        fields.put("infinity", infinity);
        fields.put("intlCurrencySymbol", intlCurrencySymbol);
        fields.put("minusSign", getMinusSign());
        fields.put("monetarySeparator", getMonetaryDecimalSeparator());
        fields.put("NaN", NaN);
        fields.put("patternSeparator", getPatternSeparator());
        fields.put("percent", getPercent());
        fields.put("perMill", getPerMill());
        fields.put("serialVersionOnStream", 3);
        fields.put("zeroDigit", getZeroDigit());
        fields.put("locale", locale);
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        final int serialVersionOnStream = fields.get("serialVersionOnStream", 0);
        patternChars = new char[9];
        currencySymbol = (String) fields.get("currencySymbol", "");
        setDecimalSeparator(fields.get("decimalSeparator", '.'));
        setDigit(fields.get("digit", '#'));
        setGroupingSeparator(fields.get("groupingSeparator", ','));
        infinity = (String) fields.get("infinity", "");
        intlCurrencySymbol = (String) fields.get("intlCurrencySymbol", "");
        setMinusSign(fields.get("minusSign", '-'));
        NaN = (String) fields.get("NaN", "");
        setPatternSeparator(fields.get("patternSeparator", ';'));
        setPercent(fields.get("percent", '%'));
        setPerMill(fields.get("perMill", '\u2030'));
        setZeroDigit(fields.get("zeroDigit", '0'));
        locale = (Locale) fields.get("locale", null);
        if (serialVersionOnStream == 0) {
            setMonetaryDecimalSeparator(getDecimalSeparator());
        } else {
            setMonetaryDecimalSeparator(fields.get("monetarySeparator", '.'));
        }

        if (serialVersionOnStream == 0) {
            // Prior to Java 1.1.6, the exponent separator wasn't configurable.
            exponentSeparator = "E";
        } else if (serialVersionOnStream < 3) {
            // In Javas 1.1.6 and 1.4, there was a character field "exponential".
            setExponentSeparator(String.valueOf(fields.get("exponential", 'E')));
        } else {
            // In Java 6, there's a new "exponentialSeparator" field.
            setExponentSeparator((String) fields.get("exponentialSeparator", "E"));
        }

        try {
            currency = Currency.getInstance(intlCurrencySymbol);
        } catch (IllegalArgumentException e) {
            currency = null;
        }
    }
}
