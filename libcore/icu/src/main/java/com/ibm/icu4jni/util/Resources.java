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

package com.ibm.icu4jni.util;

import java.util.Enumeration;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Helper class that delivers ResourceBundle instances expected by Harmony, but
 * with the data taken from ICU's database. This approach has a couple of
 * advantages:
 * <ol>
 * <li> We have less classes in the overall system, since we use different
 * instances for different ResourceBundles.
 * <li> We don't have these classes that consists of monstrous static arrays
 * with anymore.
 * <li> We have control over which values we load at which time or even cache
 * for later use.
 * <li> There is only one central place left in the system where I18N data needs
 * to be configured, namely ICU.
 * </ol>
 * Since we're mimicking the original Harmony ResourceBundle structures, most of
 * the Harmony code can stay the same. We basically just need to change the
 * ResourceBundle instantiation. Only the special case of the Locale bundles
 * needs some more tweaking, since we don't want to keep several hundred
 * timezone names in memory.
 */
public class Resources {

    /**
     * Cache for ISO language names.
     */
    private static String[] isoLanguages = null;

    /**
     * Cache for ISO country names.
     */
    private static String[] isoCountries = null;

    /**
     * Available locales cache.
     */
    private static String[] availableLocales = null;

    /**
     * Available timezones cache.
     */
    private static String[] availableTimezones = null;

    /**
     * Creates ResourceBundle instance and fills it with ICU data.
     * 
     * @param bundleName The name of the requested Harmony resource bundle,
     *            excluding the package name.
     * @param locale The locale to use for the resources. A null value denotes
     *            the default locale as configured in Java.
     * @return The new ResourceBundle, or null, if no ResourceBundle was
     *         created.
     */
    public static ResourceBundle getInstance(String bundleName, String locale) {
        if (locale == null) {
            locale = java.util.Locale.getDefault().toString();
        }

        if (bundleName.startsWith("Locale")) {
            return new Locale(locale);
        } else if (bundleName.startsWith("Country")) {
            return new Country(locale);
        } else if (bundleName.startsWith("Currency")) {
            return new Currency(locale);
        } else if (bundleName.startsWith("Language")) {
            return new Language(locale);
        } else if (bundleName.startsWith("Variant")) {
            return new Variant(locale);
        } else if (bundleName.equals("ISO3Countries")) {
            return new ISO3Countries();
        } else if (bundleName.equals("ISO3Languages")) {
            return new ISO3Languages();
        } else if (bundleName.equals("ISO4CurrenciesToDigits")) {
            return new ISO4CurrenciesToDigits();
        } else if (bundleName.equals("ISO4Currencies")) {
            return new ISO4Currencies();
        }

        return null;
    }

    /**
     * Returns an array of ISO language names (two-letter codes), fetched either
     * from ICU's database or from our memory cache.
     * 
     * @return The array.
     */
    public static String[] getISOLanguages() {
        if (isoLanguages == null) {
            isoLanguages = getISOLanguagesNative();
        }

        return isoLanguages;
    }

    /**
     * Returns an array of ISO country names (two-letter codes), fetched either
     * from ICU's database or from our memory cache.
     * 
     * @return The array.
     */
    public static String[] getISOCountries() {
        if (isoCountries == null) {
            isoCountries = getISOCountriesNative();
        }

        return isoCountries;
    }

    /**
     * Returns an array of names of locales that are available in the system,
     * fetched either from ICU's database or from our memory cache.
     * 
     * @return The array.
     */
    public static String[] getAvailableLocales() {
        if (availableLocales == null) {
            availableLocales = getAvailableLocalesNative();
        }

        return availableLocales;
    }

    /**
     * Returns an array of names of timezones that are available in the system,
     * fetched either from the TimeZone class or from our memory cache.
     * 
     * @return The array.
     */
    public static String[] getKnownTimezones() {
        // TODO Drop the Linux ZoneInfo stuff in favor of ICU.
        if (availableTimezones == null) {
            availableTimezones = TimeZone.getAvailableIDs();
        }

        return availableTimezones;
    }

    /**
     * Returns the display name for the given time zone using the given locale.
     * 
     * @param id The time zone ID, for example "Europe/Berlin"
     * @param isDST Indicates whether daylight savings is in use
     * @param style The style, 0 for long, 1 for short
     * @param locale The locale name, for example "en_US".
     * @return The desired display name
     */
    public static String getDisplayTimeZone(String id, boolean isDST, int style, String locale) {
        return getDisplayTimeZoneNative(id, isDST, style, locale);
    }

    /**
     * Gets the name of the default locale.
     */
    private static String getDefaultLocaleName() {
        return java.util.Locale.getDefault().toString();
    }
    
    /**
     * Name of default locale at the time this class was initialized.
     */
    private static final String initialLocale = getDefaultLocaleName();

    /**
     * Names of time zones for the default locale.
     */
    private static String[][] defaultTimezoneNames = null;

    /**
     * Creates array of time zone names for the given locale. This method takes
     * about 2s to run on a 400mhz ARM11.
     */
    private static String[][] createTimeZoneNamesFor(String locale) {
        long start = System.currentTimeMillis();

        /*
         * The following code is optimized for fast native response (the time a
         * method call can be in native code is limited). It prepares an empty
         * array to keep native code from having to create new Java objects. It
         * also fill in the time zone IDs to speed things up a bit. There's one
         * array for each time zone name type. (standard/long, standard/short,
         * daylight/long, daylight/short) The native method that fetches these
         * strings is faster if it can do all entries of one type, before having
         * to change to the next type. That's why the array passed down to
         * native has 5 entries, each providing space for all time zone names of
         * one type. Likely this access to the fields is much faster in the
         * native code because there's less array access overhead.
         */
        String[][] arrayToFill = new String[5][];
        arrayToFill[0] = getKnownTimezones();
        arrayToFill[1] = new String[availableTimezones.length];
        arrayToFill[2] = new String[availableTimezones.length];
        arrayToFill[3] = new String[availableTimezones.length];
        arrayToFill[4] = new String[availableTimezones.length];

        /*
         * Fill in the zone names in native.
         */
        getTimeZonesNative(arrayToFill, locale);

        /*
         * Finally we need to reorder the entries so we get the expected result.
         */
        String[][] result = new String[availableTimezones.length][5];
        for (int i = 0; i < availableTimezones.length; i++) {
            result[i][0] = arrayToFill[0][i];
            result[i][1] = arrayToFill[1][i];
            result[i][2] = arrayToFill[2][i];
            result[i][3] = arrayToFill[3][i];
            result[i][4] = arrayToFill[4][i];
        }

        Logger.getLogger(Resources.class.getSimpleName()).info(
                "Loaded time zone names for " + locale + " in "
                + (System.currentTimeMillis() - start) + "ms.");

        return result;
    }

    /**
     * Returns the display names for all given timezones using the given locale.
     * 
     * @return An array of time zone strings. Each row represents one time zone.
     *         The first columns holds the ID of the time zone, for example
     *         "Europe/Berlin". The other columns then hold for each row the
     *         four time zone names with and without daylight savings and in
     *         long and short format. It's exactly the array layout required by
     *         the TomeZone class.
     */
    public static String[][] getDisplayTimeZones(String locale) {
        // Note: Defer loading DefaultTimeZones as long as possible.

        String defaultLocaleName = getDefaultLocaleName();
        if (locale == null) {
            locale = defaultLocaleName;
        }

        // If locale == default and the default locale hasn't changed since
        // DefaultTimeZones loaded, return the cached names.
        // TODO: We should force a reboot if the default locale changes.
        if (defaultLocaleName.equals(locale)
                && initialLocale.equals(defaultLocaleName)) {
            if (defaultTimezoneNames == null) {
                defaultTimezoneNames = createTimeZoneNamesFor(locale);
            }
            return defaultTimezoneNames;
        }
        
        return createTimeZoneNamesFor(locale);
    }

    // --- Specialized ResourceBundle subclasses ------------------------------

    /**
     * Internal ResourceBundle mimicking the Harmony "ISO3Countries" bundle.
     * Keys are the two-letter ISO country codes. Values are the three-letter
     * ISO country abbreviations. An example entry is "US"->"USA".
     */
    private static final class ISO3Countries extends ResourceBundle {

        @Override
        public Enumeration<String> getKeys() {
            // Won't get used
            throw new UnsupportedOperationException();
        }

        @Override
        protected Object handleGetObject(String key) {
            return getISO3CountryNative(key);
        }

    }

    /**
     * Internal ResourceBundle mimicking the Harmony "ISO3Languages" bundle.
     * Keys are the two-letter ISO language codes. Values are the three-letter
     * ISO language abbreviations. An example entry is "EN"->"ENG".
     */
    private static final class ISO3Languages extends ResourceBundle {

        @Override
        public Enumeration<String> getKeys() {
            // Won't get used
            throw new UnsupportedOperationException();
        }

        @Override
        protected Object handleGetObject(String key) {
            return getISO3LanguageNative(key);
        }

    }

    /**
     * Internal ResourceBundle mimicking the Harmony "ISO4Currencies" bundle.
     * Keys are the two-letter ISO language codes. Values are the three-letter
     * ISO currency abbreviations. An example entry is "US"->"USD".
     */
    private static final class ISO4Currencies extends ResourceBundle {

        @Override
        public Enumeration<String> getKeys() {
            // Won't get used
            throw new UnsupportedOperationException();
        }

        @Override
        protected Object handleGetObject(String key) {
            return getCurrencyCodeNative(key);
        }

    }

    /**
     * Internal ResourceBundle mimicking the Harmony "ISO4CurrenciesToDigits"
     * bundle. Keys are the three-letter ISO currency codes. Values are strings
     * containing the number of fraction digits to use for the currency. An
     * example entry is "USD"->"2".
     */
    private static final class ISO4CurrenciesToDigits extends ResourceBundle {

        @Override
        public Enumeration<String> getKeys() {
            // Won't get used
            throw new UnsupportedOperationException();
        }

        @Override
        protected Object handleGetObject(String key) {
            // In some places the triple-x code is used as the fall back
            // currency. The harmony package returned -1 for this requested
            // currency.
            if ("XXX".equals(key)) {
                return "-1";
            }
            int res = getFractionDigitsNative(key);
            if(res < 0) {
                throw new MissingResourceException("couldn't find resource.", 
                        ISO4CurrenciesToDigits.class.getName(), key);
            }
            return "" + res;
        }

    }

    /**
     * Internal ResourceBundle mimicking the Harmony "Country_*" bundles. Keys
     * are the two-letter ISO country codes. Values are the printable country
     * names in terms of the specified locale. An example entry is "US"->"United
     * States".
     */
    private static final class Country extends ResourceBundle {
        private String locale;

        public Country(String locale) {
            super();
            this.locale = locale;
        }

        @Override
        public Enumeration<String> getKeys() {
            // Won't get used
            throw new UnsupportedOperationException();
        }

        @Override
        protected Object handleGetObject(String key) {
            return getDisplayCountryNative(key, locale);
        }

    }

    /**
     * Internal ResourceBundle mimicking the Harmony "Currency_*" bundles. Keys
     * are the three-letter ISO currency codes. Values are the printable
     * currency names in terms of the specified locale. An example entry is
     * "USD"->"$" (for inside the US) and "USD->"US$" (for outside the US).
     */
    private static final class Currency extends ResourceBundle {

        private String locale;

        public Currency(String locale) {
            super();
            this.locale = locale;
        }

        @Override
        public Enumeration<String> getKeys() {
            // Won't get used
            throw new UnsupportedOperationException();
        }

        @Override
        protected Object handleGetObject(String key) {
            return getCurrencySymbolNative(locale, key);
        }

    }

    /**
     * Internal ResourceBundle mimicking the Harmony "Language_*" bundles. Keys
     * are the two-letter ISO language codes. Values are the printable language
     * names in terms of the specified locale. An example entry is
     * "en"->"English".
     */
    private static final class Language extends ResourceBundle {
        private String locale;

        public Language(String locale) {
            super();
            this.locale = locale;
        }

        @Override
        public Enumeration<String> getKeys() {
            // Won't get used
            throw new UnsupportedOperationException();
        }

        @Override
        protected Object handleGetObject(String key) {
            return getDisplayLanguageNative(key, locale);
        }

    }

    /**
     * Internal ResourceBundle mimicking the Harmony "Variant_*" bundles. Keys
     * are a fixed set of variants codes known to Harmony. Values are the
     * printable variant names in terms of the specified locale. An example
     * entry is "EURO"->"Euro".
     */
    private static final class Variant extends ResourceBundle {

        private String locale;

        public Variant(String locale) {
            super();
            this.locale = locale;
        }

        @Override
        public Enumeration<String> getKeys() {
            // Won't get used
            throw new UnsupportedOperationException();
        }

        @Override
        protected Object handleGetObject(String key) {
            return getDisplayVariantNative(key, locale);
        }

    }

    /**
     * Internal ResourceBundle mimicking the Harmony "Locale_*" bundles. This is
     * clearly the most complex case, because the content covers a wide range of
     * data items, with values even being arrays in some cases. Note we are
     * cheating with the "timezones" entry, since we normally don't want to
     * waste our precious RAM on several thousand of these Strings.
     */
    private static final class Locale extends ListResourceBundle {

        private String locale;

        public Locale(String locale) {
            super();
            this.locale = locale;
        }

        @Override
        protected Object[][] getContents() {
            return getContentImpl(locale, false);
        }

    }

    // --- Native methods accessing ICU's database ----------------------------

    private static native int getFractionDigitsNative(String currencyCode);

    private static native String getCurrencyCodeNative(String locale);

    private static native String getCurrencySymbolNative(String locale, String currencyCode);

    private static native String getDisplayCountryNative(String countryCode, String locale);

    private static native String getDisplayLanguageNative(String languageCode, String locale);

    private static native String getDisplayVariantNative(String variantCode, String locale);

    private static native String getISO3CountryNative(String locale);

    private static native String getISO3LanguageNative(String locale);

    private static native String[] getAvailableLocalesNative();

    private static native String[] getISOLanguagesNative();

    private static native String[] getISOCountriesNative();

    private static native void getTimeZonesNative(String[][] arrayToFill, String locale);

    private static native String getDisplayTimeZoneNative(String id, boolean isDST, int style,
            String locale);

    private static native Object[][] getContentImpl(String locale, boolean needsTimeZones);
}
