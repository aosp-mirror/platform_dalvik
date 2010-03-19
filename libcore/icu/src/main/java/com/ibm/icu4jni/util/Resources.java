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
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Makes ICU data accessible to Java.
 *
 * TODO: move the LocaleData stuff into LocaleData and rename this class.
 */
public final class Resources {
    // A cache for the locale-specific data.
    private static final HashMap<String, LocaleData> localeDataCache =
            new HashMap<String, LocaleData>();

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
     * Returns a shared LocaleData for the given locale.
     */
    public static LocaleData getLocaleData(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        String localeName = locale.toString();
        synchronized (localeDataCache) {
            LocaleData localeData = localeDataCache.get(localeName);
            if (localeData != null) {
                return localeData;
            }
        }
        LocaleData newLocaleData = makeLocaleData(locale);
        synchronized (localeDataCache) {
            LocaleData localeData = localeDataCache.get(localeName);
            if (localeData != null) {
                return localeData;
            }
            localeDataCache.put(localeName, newLocaleData);
            return newLocaleData;
        }
    }

    private static LocaleData makeLocaleData(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        // Start with data from the parent (next-most-specific) locale...
        LocaleData result = new LocaleData();
        if (variant.length() > 0) {
            result.overrideWithDataFrom(getLocaleData(new Locale(language, country, "")));
        } else if (country.length() > 0) {
            result.overrideWithDataFrom(getLocaleData(new Locale(language, "", "")));
        } else if (language.length() > 0) {
            result.overrideWithDataFrom(getLocaleData(Locale.ROOT));
        }
        // Override with data from this locale.
        result.overrideWithDataFrom(initLocaleData(locale));
        return result;
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
     * @param daylight Indicates whether daylight savings is in use
     * @param style The style, 0 for long, 1 for short
     * @param locale The locale name, for example "en_US".
     * @return The desired display name
     */
    public static String getDisplayTimeZone(String id, boolean daylight, int style, String locale) {
        // If we already have the strings, linear search through them is 10x quicker than
        // calling ICU for just the one we want.
        if (DefaultTimeZones.locale.equals(locale)) {
            String result = lookupDisplayTimeZone(DefaultTimeZones.names, id, daylight, style);
            if (result != null) {
                return result;
            }
        }
        return getDisplayTimeZoneNative(id, daylight, style, locale);
    }

    public static String lookupDisplayTimeZone(String[][] zoneStrings, String id, boolean daylight, int style) {
        for (String[] row : zoneStrings) {
            if (row[0].equals(id)) {
                if (daylight) {
                    return (style == TimeZone.LONG) ? row[3] : row[4];
                } else {
                    return (style == TimeZone.LONG) ? row[1] : row[2];
                }
            }
        }
        return null;
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
     *         the TimeZone class.
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

    public static String[][] clone2dStringArray(String[][] array) {
        String[][] result = new String[array.length][];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i].clone();
        }
        return result;
    }

    /**
     * Returns the appropriate {@code Locale} given a {@code String} of the form returned
     * by {@code toString}. This is very lenient, and doesn't care what's between the underscores:
     * this method can parse strings that {@code Locale.toString} won't produce.
     * Used to remove duplication.
     */
    public static Locale localeFromString(String localeName) {
        int first = localeName.indexOf('_');
        int second = localeName.indexOf('_', first + 1);
        if (first == -1) {
            // Language only ("ja").
            return new Locale(localeName);
        } else if (second == -1) {
            // Language and country ("ja_JP").
            return new Locale(localeName.substring(0, first), localeName.substring(first + 1));
        } else {
            // Language and country and variant ("ja_JP_TRADITIONAL").
            return new Locale(localeName.substring(0, first), localeName.substring(first + 1, second), localeName.substring(second + 1));
        }
    }

    public static Locale[] localesFromStrings(String[] localeNames) {
        Locale[] result = new Locale[localeNames.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = localeFromString(localeNames[i]);
        }
        return result;
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

    private static LocaleData initLocaleData(Locale locale) {
        LocaleData localeData = new LocaleData();
        if (!initLocaleDataImpl(locale.toString(), localeData)) {
            throw new AssertionError("couldn't initialize LocaleData for locale " + locale);
        }
        if (localeData.fullTimeFormat != null) {
            // There are some full time format patterns in ICU that use the pattern character 'v'.
            // Java doesn't accept this, so we replace it with 'z' which has about the same result
            // as 'v', the timezone name.
            // 'v' -> "PT", 'z' -> "PST", v is the generic timezone and z the standard tz
            // "vvvv" -> "Pacific Time", "zzzz" -> "Pacific Standard Time"
            localeData.fullTimeFormat = localeData.fullTimeFormat.replace('v', 'z');
        }
        if (localeData.numberPattern != null) {
            // The number pattern might contain positive and negative subpatterns. Arabic, for
            // example, might look like "#,##0.###;#,##0.###-" because the minus sign should be
            // written last. Macedonian supposedly looks something like "#,##0.###;(#,##0.###)".
            // (The negative subpattern is optional, though, and not present in most locales.)
            // By only swallowing '#'es and ','s after the '.', we ensure that we don't
            // accidentally eat too much.
            localeData.integerPattern = localeData.numberPattern.replaceAll("\\.[#,]*", "");
        }
        return localeData;
    }

    private static native boolean initLocaleDataImpl(String locale, LocaleData result);
}
