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

// BEGIN android-changed
// import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
// import java.util.zip.ZipEntry;
// import java.util.zip.ZipFile;

import org.apache.harmony.luni.util.PriviAction;
import org.apache.harmony.luni.util.Util;

import com.ibm.icu4jni.util.Resources;
// END android-changed

/**
 * {@code Locale} represents a language/country/variant combination. Locales are used to
 * alter the presentation of information such as numbers or dates to suit the conventions
 * in the region they describe.
 *
 * <p>The language codes are two-letter lowercase ISO language codes (such as "en") as defined by
 * <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1</a>.
 * The country codes are two-letter uppercase ISO country codes (such as "US") as defined by
 * <a href="http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3">ISO 3166-1</a>.
 * The variant codes are unspecified.
 *
 * <p>Note that Java uses several deprecated two-letter codes. The Hebrew ("he") language
 * code is rewritten as "iw", Indonesian ("id") as "in", and Yiddish ("yi") as "ji". This
 * is true even if you construct your own {@code Locale} object, not just of instances returned by
 * the various lookup methods.
 *
 * <p>Just because you can create a {@code Locale} doesn't mean that it makes much sense.
 * Imagine "de_US" for German as spoken in the US, for example. It is also a mistake to
 * assume that all devices have the same locales available. A device sold in the US will
 * almost certainly support en_US and sp_US (English and Spanish, as spoken in the US),
 * but not necessarily en_GB or sp_SP (English as spoken in Great Britain or Spanish as
 * spoken in Spain), for example. The opposite may well be true for a device sold in Europe.
 * (This limitation even affects those locales pre-defined as constants in this class.)
 *
 * <p>You can use {@code getDefault} to get an appropriate locale for the device you're
 * running on, or {@code getAvailableLocales} to get a list of all the locales available
 * on the device you're running on.
 *
 * @see ResourceBundle
 */
public final class Locale implements Cloneable, Serializable {

    private static final long serialVersionUID = 9149081749638150636L;

    // BEGIN android-added
    private static volatile Locale[] availableLocales;
    // END android-added

    // Initialize a default which is used during static
    // initialization of the default for the platform.
    private static Locale defaultLocale = new Locale();

    /**
     * Locale constant for en_CA.
     */
    public static final Locale CANADA = new Locale("en", "CA"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for fr_CA.
     */
    public static final Locale CANADA_FRENCH = new Locale("fr", "CA"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale CHINA = new Locale("zh", "CN"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for zh.
     */
    public static final Locale CHINESE = new Locale("zh", ""); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for en.
     */
    public static final Locale ENGLISH = new Locale("en", ""); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for fr_FR.
     */
    public static final Locale FRANCE = new Locale("fr", "FR"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for fr.
     */
    public static final Locale FRENCH = new Locale("fr", ""); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for de.
     */
    public static final Locale GERMAN = new Locale("de", ""); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for de_DE.
     */
    public static final Locale GERMANY = new Locale("de", "DE"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for it.
     */
    public static final Locale ITALIAN = new Locale("it", ""); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for it_IT.
     */
    public static final Locale ITALY = new Locale("it", "IT"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for ja_JP.
     */
    public static final Locale JAPAN = new Locale("ja", "JP"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for ja.
     */
    public static final Locale JAPANESE = new Locale("ja", ""); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for ko_KR.
     */
    public static final Locale KOREA = new Locale("ko", "KR"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for ko.
     */
    public static final Locale KOREAN = new Locale("ko", ""); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale PRC = new Locale("zh", "CN"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale SIMPLIFIED_CHINESE = new Locale("zh", "CN"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for zh_TW.
     */
    public static final Locale TAIWAN = new Locale("zh", "TW"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for zh_TW.
     */
    public static final Locale TRADITIONAL_CHINESE = new Locale("zh", "TW"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for en_GB.
     */
    public static final Locale UK = new Locale("en", "GB"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for en_US.
     */
    public static final Locale US = new Locale("en", "US"); //$NON-NLS-1$//$NON-NLS-2$

    private static final PropertyPermission setLocalePermission = new PropertyPermission(
            "user.language", "write"); //$NON-NLS-1$//$NON-NLS-2$

    static {
        String language = AccessController
                .doPrivileged(new PriviAction<String>("user.language", "en")); //$NON-NLS-1$ //$NON-NLS-2$
        // BEGIN android-changed
        String region = AccessController.doPrivileged(new PriviAction<String>(
                "user.region", "US")); //$NON-NLS-1$ //$NON-NLS-2$
        // END android-changed
        String variant = AccessController.doPrivileged(new PriviAction<String>(
                "user.variant", "")); //$NON-NLS-1$ //$NON-NLS-2$
        defaultLocale = new Locale(language, region, variant);
    }

    private transient String countryCode;
    private transient String languageCode;
    private transient String variantCode;

    // BEGIN android-removed
    // private transient ULocale uLocale;
    // END android-removed

	/**
	 * Constructs a default which is used during static initialization of the
	 * default for the platform.
	 */
	private Locale() {
		languageCode = "en"; //$NON-NLS-1$
		countryCode = "US"; //$NON-NLS-1$
		variantCode = ""; //$NON-NLS-1$
	}

    /**
     * Constructs a new {@code Locale} using the specified language.
     *
     * @param language
     *            the language this {@code Locale} represents.
     */
    public Locale(String language) {
        this(language, "", ""); //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Constructs a new {@code Locale} using the specified language and country codes.
     *
     * @param language
     *            the language this {@code Locale} represents.
     * @param country
     *            the country this {@code Locale} represents.
     */
    public Locale(String language, String country) {
        this(language, country, ""); //$NON-NLS-1$
    }

    /**
     * Constructs a new {@code Locale} using the specified language, country, and
     * variant codes.
     *
     * @param language
     *            the language this {@code Locale} represents.
     * @param country
     *            the country this {@code Locale} represents.
     * @param variant
     *            the variant this {@code Locale} represents.
     * @throws NullPointerException
     *             if {@code language}, {@code country}, or
     *             {@code variant} is {@code null}.
     */
    public Locale(String language, String country, String variant) {
        if (language == null || country == null || variant == null) {
            throw new NullPointerException();
        }
        if(language.length() == 0 && country.length() == 0){
            languageCode = "";
            countryCode = "";
            variantCode = variant;
            return;
        }
        // BEGIN android-changed
        // this.uLocale = new ULocale(language, country, variant);
        // languageCode = uLocale.getLanguage();
        languageCode = Util.toASCIILowerCase(language);
        // END android-changed
        // Map new language codes to the obsolete language
        // codes so the correct resource bundles will be used.
        if (languageCode.equals("he")) {//$NON-NLS-1$
            languageCode = "iw"; //$NON-NLS-1$
        } else if (languageCode.equals("id")) {//$NON-NLS-1$
            languageCode = "in"; //$NON-NLS-1$
        } else if (languageCode.equals("yi")) {//$NON-NLS-1$
            languageCode = "ji"; //$NON-NLS-1$
        }

        // countryCode is defined in ASCII character set
        // BEGIN android-changed
        // countryCode = country.length()!=0?uLocale.getCountry():"";
        countryCode = Util.toASCIIUpperCase(country);
        // END android-changed

        // Work around for be compatible with RI
        variantCode = variant;
    }

    /**
     * Returns a new {@code Locale} with the same language, country and variant codes as
     * this {@code Locale}.
     *
     * @return a shallow copy of this {@code Locale}.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // android-changed
        }
    }

    /**
     * Compares the specified object to this {@code Locale} and returns whether they are
     * equal. The object must be an instance of {@code Locale} and have the same
     * language, country and variant.
     *
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this {@code Locale},
     *         {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Locale) {
            Locale o = (Locale) object;
            return languageCode.equals(o.languageCode)
                    && countryCode.equals(o.countryCode)
                    && variantCode.equals(o.variantCode);
        }
        return false;
    }

    // BEGIN android-added
    static Locale[] find() {
        String[] locales = Resources.getAvailableLocales();
        ArrayList<Locale> temp = new ArrayList<Locale>();
        for (int i = 0; i < locales.length; i++) {
            String s = locales[i];
            int first = s.indexOf('_');
            int second = s.indexOf('_', first + 1);

            if (first == -1) {
                // Language only
                temp.add(new Locale(s));
            } else if (second == -1) {
                // Language and country
                temp.add(new Locale(s.substring(0, first), s.substring(first + 1)));
            } else {
                // Language and country and variant
                temp.add(new Locale(s.substring(0, first), s.substring(first + 1, second), s.substring(second + 1)));
            }
        }
        Locale[] result = new Locale[temp.size()];
        return temp.toArray(result);
    }
    // END android-added

    /**
     * Gets the list of installed {@code Locale}s. At least a {@code Locale} that is equal to
     * {@code Locale.US} must be contained in this array.
     * 
     * @return an array of {@code Locale}s.
     */
    public static Locale[] getAvailableLocales() {
        // BEGIN android-changed
        // ULocale[] ulocales =  ULocale.getAvailableLocales();
        // Locale[] locales = new Locale[ulocales.length];
        // for (int i = 0; i < locales.length; i++) {
        //     locales[i] = ulocales[i].toLocale();
        // }
        // return locales;
        if (availableLocales == null) {
            availableLocales = find();
        }
        return availableLocales.clone();
        // END android-changed
    }

    /**
     * Gets the country code for this {@code Locale} or an empty string of no country
     * was set.
     *
     * @return a country code.
     */
    public String getCountry() {
        return countryCode;
    }

    /**
     * Gets the default {@code Locale}.
     *
     * @return the default {@code Locale}.
     */
    public static Locale getDefault() {
        return defaultLocale;
    }

    /**
     * Gets the full country name in the default {@code Locale} for the country code of
     * this {@code Locale}. If there is no matching country name, the country code is
     * returned.
     *
     * @return a country name.
     */
    public final String getDisplayCountry() {
        return getDisplayCountry(getDefault());
    }

    /**
     * Gets the full country name in the specified {@code Locale} for the country code
     * of this {@code Locale}. If there is no matching country name, the country code is
     * returned.
     *
     * @param locale
     *            the {@code Locale} for which the display name is retrieved.
     * @return a country name.
     */
	public String getDisplayCountry(Locale locale) {
        // BEGIN android-changed
		// return ULocale.forLocale(this).getDisplayCountry(ULocale.forLocale(locale));
        if (countryCode.length() == 0) {
            return countryCode;
        }
        try {
            // First try the specified locale
            ResourceBundle bundle = getBundle("Country", locale); //$NON-NLS-1$
            String result = bundle.getString(this.toString());
            if (result != null) {
                return result;
            }
            // Now use the default locale
            if (locale != Locale.getDefault()) {
                bundle = getBundle("Country", Locale.getDefault()); //$NON-NLS-1$
            }
            return bundle.getString(countryCode);
        } catch (MissingResourceException e) {
            return countryCode;
        }
        // END android-changed
	}

    /**
     * Gets the full language name in the default {@code Locale} for the language code
     * of this {@code Locale}. If there is no matching language name, the language code
     * is returned.
     *
     * @return a language name.
     */
    public final String getDisplayLanguage() {
        return getDisplayLanguage(getDefault());
    }

    /**
     * Gets the full language name in the specified {@code Locale} for the language code
     * of this {@code Locale}. If there is no matching language name, the language code
     * is returned.
     *
     * @param locale
     *            the {@code Locale} for which the display name is retrieved.
     * @return a language name.
     */
	public String getDisplayLanguage(Locale locale) {
        // BEGIN android-changed
        // return ULocale.forLocale(this).getDisplayLanguage(ULocale.forLocale(locale));
        if (languageCode.length() == 0) {
            return languageCode;
        }
        try {
            // First try the specified locale
            ResourceBundle bundle = getBundle("Language", locale); //$NON-NLS-1$
            String result = bundle.getString(this.toString());
            if (result != null) {
                return result;
            }
            // Now use the default locale
            if (locale != Locale.getDefault()) {
                bundle = getBundle("Language", Locale.getDefault()); //$NON-NLS-1$
            }
            return bundle.getString(languageCode);
        } catch (MissingResourceException e) {
            return languageCode;
        }
        // END android-changed
	}

    /**
     * Gets the full language, country, and variant names in the default {@code Locale}
     * for the codes of this {@code Locale}.
     *
     * @return a {@code Locale} name.
     */
    public final String getDisplayName() {
        return getDisplayName(getDefault());
    }

    /**
     * Gets the full language, country, and variant names in the specified
     * Locale for the codes of this {@code Locale}.
     *
     * @param locale
     *            the {@code Locale} for which the display name is retrieved.
     * @return a {@code Locale} name.
     */
    public String getDisplayName(Locale locale) {
        int count = 0;
        StringBuilder buffer = new StringBuilder();
        if (languageCode.length() > 0) {
            buffer.append(getDisplayLanguage(locale));
            count++;
        }
        if (countryCode.length() > 0) {
            if (count == 1) {
                buffer.append(" ("); //$NON-NLS-1$
            }
            buffer.append(getDisplayCountry(locale));
            count++;
        }
        if (variantCode.length() > 0) {
            if (count == 1) {
                buffer.append(" ("); //$NON-NLS-1$
            } else if (count == 2) {
                buffer.append(","); //$NON-NLS-1$
            }
            buffer.append(getDisplayVariant(locale));
            count++;
        }
        if (count > 1) {
            buffer.append(")"); //$NON-NLS-1$
        }
        return buffer.toString();
    }

    /**
     * Gets the full variant name in the default {@code Locale} for the variant code of
     * this {@code Locale}. If there is no matching variant name, the variant code is
     * returned.
     *
     * @return a variant name.
     */
    public final String getDisplayVariant() {
        return getDisplayVariant(getDefault());
    }

    /**
     * Gets the full variant name in the specified {@code Locale} for the variant code
     * of this {@code Locale}. If there is no matching variant name, the variant code is
     * returned.
     *
     * @param locale
     *            the {@code Locale} for which the display name is retrieved.
     * @return a variant name.
     */
	public String getDisplayVariant(Locale locale) {
        // BEGIN android-changed
        // return ULocale.forLocale(this).getDisplayVariant(ULocale.forLocale(locale));
        if (variantCode.length() == 0) {
            return variantCode;
        }
        try {
            // First try the specified locale
            ResourceBundle bundle = getBundle("Variant", locale); //$NON-NLS-1$
            String result = bundle.getString(this.toString());
            if (result != null) {
                return result;
            }
            // Now use the default locale
            if (locale != Locale.getDefault()) {
                bundle = getBundle("Variant", Locale.getDefault()); //$NON-NLS-1$
            }
            return bundle.getString(variantCode);
        } catch (MissingResourceException e) {
            return variantCode;
        }
        // END android-changed
	}

    /**
     * Gets the three letter ISO country code which corresponds to the country
     * code for this {@code Locale}.
     *
     * @return a three letter ISO language code.
     * @throws MissingResourceException
     *                if there is no matching three letter ISO country code.
     */
	public String getISO3Country() throws MissingResourceException {
        // BEGIN android-changed
        // return ULocale.forLocale(this).getISO3Country();
        if (countryCode.length() == 0) {
            return ""; //$NON-NLS-1$
        }
        ResourceBundle bundle = getBundle("ISO3Countries", this); //$NON-NLS-1$
        return bundle.getString(this.toString());
        // END android-changed
	}

    /**
     * Gets the three letter ISO language code which corresponds to the language
     * code for this {@code Locale}.
     *
     * @return a three letter ISO language code.
     * @throws MissingResourceException
     *                if there is no matching three letter ISO language code.
     */
	public String getISO3Language() throws MissingResourceException {
        // BEGIN android-changed
        // return ULocale.forLocale(this).getISO3Language();
        if (languageCode.length() == 0) {
            return ""; //$NON-NLS-1$
        }
        ResourceBundle bundle = getBundle("ISO3Languages", this); //$NON-NLS-1$
        return bundle.getString(this.toString());
        // END android-changed
	}

    /**
     * Gets the list of two letter ISO country codes which can be used as the
     * country code for a {@code Locale}.
     *
     * @return an array of strings.
     */
    public static String[] getISOCountries() {
        // BEGIN android-changed
        // return ULocale.getISOCountries();
        return Resources.getISOCountries();
        // END android-changed
    }

    /**
     * Gets the list of two letter ISO language codes which can be used as the
     * language code for a {@code Locale}.
     *
     * @return an array of strings.
     */
	public static String[] getISOLanguages() {
        // BEGIN android-changed
        // return ULocale.getISOLanguages();
        return Resources.getISOLanguages();
        // END android-changed
	}

    /**
     * Gets the language code for this {@code Locale} or the empty string of no language
     * was set.
     *
     * @return a language code.
     */
    public String getLanguage() {
        return languageCode;
    }

    /**
     * Gets the variant code for this {@code Locale} or an empty {@code String} of no variant
     * was set.
     *
     * @return a variant code.
     */
    public String getVariant() {
        return variantCode;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * return the same value for this method.
     *
     * @return the receiver's hash.
     * @see #equals
     */
    @Override
    public synchronized int hashCode() {
        return countryCode.hashCode() + languageCode.hashCode()
                + variantCode.hashCode();
    }

    /**
     * Sets the default {@code Locale} to the specified {@code Locale}.
     *
     * @param locale
     *            the new default {@code Locale}.
     * @throws SecurityException
     *                if there is a {@code SecurityManager} in place which does not allow this
     *                operation.
     */
    public synchronized static void setDefault(Locale locale) {
        if (locale != null) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(setLocalePermission);
            }
            defaultLocale = locale;
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * Returns the string representation of this {@code Locale}. It consists of the
     * language code, country code and variant separated by underscores.
     * If the language is missing the string begins
     * with an underscore. If the country is missing there are 2 underscores
     * between the language and the variant. The variant cannot stand alone
     * without a language and/or country code: in this case this method would
     * return the empty string.
     *
     * <p>Examples: "en", "en_US", "_US", "en__POSIX", "en_US_POSIX"
     *
     * @return the string representation of this {@code Locale}.
     */
    @Override
    public final String toString() {
        StringBuilder result = new StringBuilder();
        result.append(languageCode);
        if (countryCode.length() > 0) {
            result.append('_');
            result.append(countryCode);
        }
        if (variantCode.length() > 0 && result.length() > 0) {
            if (0 == countryCode.length()) {
                result.append("__"); //$NON-NLS-1$
            } else {
                result.append('_');
            }
            result.append(variantCode);
        }
        return result.toString();
    }

    // BEGIN android-added
    static ResourceBundle getBundle(final String clName, final Locale locale) {
        return AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>() {
            public ResourceBundle run() {
                return ResourceBundle.getBundle("org.apache.harmony.luni.internal.locale." //$NON-NLS-1$
                        + clName, locale);
            }
        });
    }
    // END android-added

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("country", String.class), //$NON-NLS-1$
            new ObjectStreamField("hashcode", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("language", String.class), //$NON-NLS-1$
            new ObjectStreamField("variant", String.class) }; //$NON-NLS-1$

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("country", countryCode); //$NON-NLS-1$
        fields.put("hashcode", -1); //$NON-NLS-1$
        fields.put("language", languageCode); //$NON-NLS-1$
        fields.put("variant", variantCode); //$NON-NLS-1$
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        countryCode = (String) fields.get("country", ""); //$NON-NLS-1$//$NON-NLS-2$
        languageCode = (String) fields.get("language", ""); //$NON-NLS-1$//$NON-NLS-2$
        variantCode = (String) fields.get("variant", ""); //$NON-NLS-1$//$NON-NLS-2$
    }
}
