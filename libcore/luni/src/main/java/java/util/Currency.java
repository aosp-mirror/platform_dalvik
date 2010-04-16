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

package java.util;

import com.ibm.icu4jni.util.ICU;
import com.ibm.icu4jni.util.LocaleData;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;
import org.apache.harmony.luni.util.Msg;

/**
 * This class represents a currency as identified in the ISO 4217 currency
 * codes.
 */
public final class Currency implements Serializable {

    private static final long serialVersionUID = -158308464356906721L;

    private static final Hashtable<String, Currency> codesToCurrencies = new Hashtable<String, Currency>();
    private static final Hashtable<Locale, Currency> localesToCurrencies = new Hashtable<Locale, Currency>();

    private final String currencyCode;

    // BEGIN android-added
    // TODO: this isn't set if we're restored from serialized form,
    // so getDefaultFractionDigits always returns 0!
    private transient int defaultFractionDigits;
    // END android-added

    private Currency(String currencyCode) {
        // BEGIN android-changed
        this.currencyCode = currencyCode;

        // In some places the code XXX is used as the fall back currency.
        // The RI returns -1, but ICU defaults to 2 for unknown currencies.
        if (currencyCode.equals("XXX")) {
            this.defaultFractionDigits = -1;
            return;
        }

        // Ensure that we throw if the our currency code isn't an ISO currency code.
        String symbol = ICU.getCurrencySymbolNative(Locale.US.toString(), currencyCode);
        if (symbol == null) {
            throw new IllegalArgumentException(Msg.getString("K0322", currencyCode));
        }

        this.defaultFractionDigits = ICU.getCurrencyFractionDigitsNative(currencyCode);
        if (defaultFractionDigits < 0) {
            // In practice, I don't think this can fail because ICU doesn't care whether you give
            // it a valid country code, and will just return a sensible default for the default
            // locale's currency.
            throw new IllegalArgumentException(Msg.getString("K0322", currencyCode));
        }
        // END android-changed
    }

    /**
     * Returns the {@code Currency} instance for the given currency code.
     * <p>
     *
     * @param currencyCode
     *            the currency code.
     * @return the {@code Currency} instance for this currency code.
     *
     * @throws IllegalArgumentException
     *             if the currency code is not a supported ISO 4217 currency
     *             code.
     */
    public static Currency getInstance(String currencyCode) {
        // BEGIN android-changed
        Currency currency = codesToCurrencies.get(currencyCode);
        if (currency == null) {
            currency = new Currency(currencyCode);
            codesToCurrencies.put(currencyCode, currency);
        }
        return currency;
        // END android-changed
    }

    /**
     * Returns the {@code Currency} instance for this {@code Locale}'s country.
     *
     * @param locale
     *            the {@code Locale} of a country.
     * @return the {@code Currency} used in the country defined by the locale parameter.
     *
     * @throws IllegalArgumentException
     *             if the locale's country is not a supported ISO 3166 Country.
     */
    public static Currency getInstance(Locale locale) {
        // BEGIN android-changed
        Currency currency = localesToCurrencies.get(locale);
        if (currency != null) {
            return currency;
        }
        String country = locale.getCountry();
        String variant = locale.getVariant();
        if (variant.length() > 0 && (variant.equals("EURO") || variant.equals("HK") ||
                variant.equals("PREEURO"))) {
            country = country + "_" + variant;
        }

        String currencyCode = ICU.getCurrencyCodeNative(country);
        if (currencyCode == null) {
            throw new IllegalArgumentException(Msg.getString("K0323", locale.toString()));
        } else if (currencyCode.equals("None")) {
            return null;
        }
        Currency result = getInstance(currencyCode);
        localesToCurrencies.put(locale, result);
        return result;
        // END android-changed
    }

    /**
     * Returns this currency's ISO 4217 currency code.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Returns the localized currency symbol for this currency in the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     */
    public String getSymbol() {
        return getSymbol(Locale.getDefault());
    }

    /**
     * Returns the localized currency symbol for this currency in {@code locale}.
     * That is, given "USD" and Locale.US, you'd get "$", but given "USD" and a non-US locale,
     * you'd get "US$".
     *
     * <p>If the locale only specifies a language rather than a language and a country (such as
     * {@code Locale.JAPANESE} or {new Locale("en", "")} rather than {@code Locale.JAPAN} or
     * {new Locale("en", "US")}), the ISO 4217 currency code is returned.
     *
     * <p>If there is no locale-specific currency symbol, the ISO 4217 currency code is returned.
     */
    public String getSymbol(Locale locale) {
        if (locale.getCountry().length() == 0) {
            return currencyCode;
        }

        // Check the locale first, in case the locale has the same currency.
        LocaleData localeData = LocaleData.get(locale);
        if (localeData.internationalCurrencySymbol.equals(currencyCode)) {
            return localeData.currencySymbol;
        }

        // Try ICU, and fall back to the currency code if ICU has nothing.
        String symbol = ICU.getCurrencySymbolNative(locale.toString(), currencyCode);
        return symbol != null ? symbol : currencyCode;
    }

    /**
     * Returns the default number of fraction digits for this currency. For
     * instance, the default number of fraction digits for the US dollar is 2.
     * For the Japanese Yen the number is 0. In the case of pseudo-currencies,
     * such as IMF Special Drawing Rights, -1 is returned.
     *
     * @return the default number of fraction digits for this currency.
     */
    public int getDefaultFractionDigits() {
        // BEGIN android-changed
        // return com.ibm.icu.util.Currency.getInstance(currencyCode).getDefaultFractionDigits();
        return defaultFractionDigits;
        // END android-changed
    }

    /**
     * Returns this currency's ISO 4217 currency code.
     *
     * @return this currency's ISO 4217 currency code.
     */
    @Override
    public String toString() {
        return currencyCode;
    }

    private Object readResolve() {
        return getInstance(currencyCode);
    }

    // TODO: remove this in favor of direct access (and no ResourceBundle cruft).
    private static ResourceBundle getCurrencyBundle(final Locale locale) {
        return AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>() {
            public ResourceBundle run() {
                String bundle = "org.apache.harmony.luni.internal.locale.Currency";
                return ResourceBundle.getBundle(bundle, locale);
            }
        });
    }
}
