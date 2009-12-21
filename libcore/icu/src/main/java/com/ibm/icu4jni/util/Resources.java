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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Makes ICU data accessible to Java.
 *
 * TODO: finish removing the expensive ResourceBundle nonsense and rename this class.
 */
public class Resources {
    // A cache for the locale-specific data.
    private static final ConcurrentHashMap<String, LocaleResourceBundle> localeInstanceCache =
            new ConcurrentHashMap<String, LocaleResourceBundle>();

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
     * Returns a LocaleResourceBundle corresponding to the given locale.
     * TODO: return something that allows cheap static field lookup rather than
     * expensive chained hash table lookup.
     */
    public static ResourceBundle getLocaleInstance(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        String localeName = locale.toString();
        LocaleResourceBundle bundle = localeInstanceCache.get(localeName);
        if (bundle != null) {
            return bundle;
        }
        bundle = makeLocaleResourceBundle(locale);
        localeInstanceCache.put(localeName, bundle);
        boolean absent = (localeInstanceCache.putIfAbsent(localeName, bundle) == null);
        return absent ? bundle : localeInstanceCache.get(localeName);
    }

    private static LocaleResourceBundle makeLocaleResourceBundle(Locale locale) {
        LocaleResourceBundle result = new LocaleResourceBundle(locale);
        
        // Anything not found in this ResourceBundle should be passed on to
        // a parent ResourceBundle corresponding to the next-most-specific locale.
        String country = locale.getCountry();
        String language = locale.getLanguage();
        if (locale.getVariant().length() > 0) {
            result.setParent(getLocaleInstance(new Locale(language, country, "")));
        } else if (country.length() > 0) {
            result.setParent(getLocaleInstance(new Locale(language, "", "")));
        } else if (language.length() > 0) {
            result.setParent(getLocaleInstance(new Locale("", "", "")));
        }
        
        return result;
    }

    // TODO: fix remaining caller and remove this.
    public static ResourceBundle getInstance(String bundleName, String locale) {
        if (locale == null) {
            locale = Locale.getDefault().toString();
        }
        if (bundleName.startsWith("Currency")) {
            return new CurrencyResourceBundle(locale);
        }
        throw new AssertionError("bundle="+bundleName+" locale="+locale);
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

        return isoLanguages.clone();
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

        return isoCountries.clone();
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

        return availableLocales.clone();
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

        return availableTimezones.clone();
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
     * Initialization holder for default time zone names. This class will
     * be preloaded by the zygote to share the time and space costs of setting
     * up the list of time zone names, so although it looks like the lazy
     * initialization idiom, it's actually the opposite.
     */
    private static class DefaultTimeZones {
        /**
         * Name of default locale at the time this class was initialized.
         */
        private static final String locale = Locale.getDefault().toString();

        /**
         * Names of time zones for the default locale.
         */
        private static final String[][] names = createTimeZoneNamesFor(locale);
    }

    /**
     * Creates array of time zone names for the given locale. This method takes
     * about 2s to run on a 400MHz ARM11.
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
        String defaultLocale = Locale.getDefault().toString();
        if (locale == null) {
            locale = defaultLocale;
        }
        
        // If locale == default and the default locale hasn't changed since
        // DefaultTimeZones loaded, return the cached names.
        // TODO: We should force a reboot if the default locale changes.
        if (defaultLocale.equals(locale) && DefaultTimeZones.locale.equals(defaultLocale)) {
            return clone2dStringArray(DefaultTimeZones.names);
        }
        
        return createTimeZoneNamesFor(locale);
    }

    private static String[][] clone2dStringArray(String[][] array) {
        String[][] result = new String[array.length][];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i].clone();
        }
        return result;
    }

    // --- Specialized ResourceBundle subclasses ------------------------------

    /**
     * Internal ResourceBundle mimicking the Harmony "Currency_*" bundles. Keys
     * are the three-letter ISO currency codes. Values are the printable
     * currency names in terms of the specified locale. An example entry is
     * "USD"->"$" (for inside the US) and "USD->"US$" (for outside the US).
     */
    private static final class CurrencyResourceBundle extends ResourceBundle {

        private String locale;

        public CurrencyResourceBundle(String locale) {
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
     * Internal ResourceBundle mimicking the Harmony "Locale_*" bundles.
     * The content covers a wide range of
     * data items, with values even being arrays in some cases. Note we are
     * cheating with the "timezones" entry, since we normally don't want to
     * waste our precious RAM on several thousand of these Strings.
     */
    private static final class LocaleResourceBundle extends ListResourceBundle {
        private final Locale locale;
        
        public LocaleResourceBundle(Locale locale) {
            this.locale = locale;
        }
        
        // We can't set the superclass' locale field, so we need our own, and our own accessor.
        @Override
        public Locale getLocale() {
            return locale;
        }

        @Override
        protected Object[][] getContents() {
            return getContentImpl(locale.toString());
        }

        // Increase accessibility of this method so we can call it.
        @Override
        public void setParent(ResourceBundle bundle) {
            this.parent = bundle;
        }
        
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("LocaleResourceBundle[locale=");
            result.append(getLocale());
            result.append(",contents=");
            result.append(Arrays.deepToString(getContents()));
            return result.toString();
        }
    }

    // --- Native methods accessing ICU's database ----------------------------

    public static native String getDisplayCountryNative(String countryCode, String locale);
    public static native String getDisplayLanguageNative(String languageCode, String locale);
    public static native String getDisplayVariantNative(String variantCode, String locale);

    public static native String getISO3CountryNative(String locale);
    public static native String getISO3LanguageNative(String locale);

    public static native String getCurrencyCodeNative(String locale);
    public static native String getCurrencySymbolNative(String locale, String currencyCode);

    public static native int getCurrencyFractionDigitsNative(String currencyCode);

    private static native String[] getAvailableLocalesNative();

    private static native String[] getISOLanguagesNative();
    private static native String[] getISOCountriesNative();

    private static native void getTimeZonesNative(String[][] arrayToFill, String locale);

    private static native String getDisplayTimeZoneNative(String id, boolean isDST, int style,
            String locale);

    private static native Object[][] getContentImpl(String locale);
}
