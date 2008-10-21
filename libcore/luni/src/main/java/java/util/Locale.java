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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// BEGIN android-removed
//import org.apache.harmony.luni.internal.locale.Country;
//import org.apache.harmony.luni.internal.locale.Language;
// END android-removed
import org.apache.harmony.luni.util.PriviAction;
import org.apache.harmony.luni.util.Util;

//BEGIN android-added
import com.ibm.icu4jni.util.Resources;
// END android-added

/**
 * Locale represents a language/country/variant combination. It is an identifier
 * which dictates particular conventions for the presentation of information.
 * The language codes are two letter lowercase codes as defined by ISO-639. The
 * country codes are three letter uppercase codes as defined by ISO-3166. The
 * variant codes are unspecified.
 * 
 * @see ResourceBundle
 */
public final class Locale implements Cloneable, Serializable {
    
    private static final long serialVersionUID = 9149081749638150636L;

    private static Locale[] availableLocales;

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
    public static final Locale CHINESE = new Locale("zh", "");  //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for en.
     */
    public static final Locale ENGLISH = new Locale("en", ""); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Locale constant for fr_FR.
     */
    public static final Locale FRANCE = new Locale("fr", "FR");  //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for fr.
     */
    public static final Locale FRENCH = new Locale("fr", "");  //$NON-NLS-1$//$NON-NLS-2$

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
    public static final Locale JAPAN = new Locale("ja", "JP");  //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for ja.
     */
    public static final Locale JAPANESE = new Locale("ja", "");  //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for ko_KR.
     */
    public static final Locale KOREA = new Locale("ko", "KR");  //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for ko.
     */
    public static final Locale KOREAN = new Locale("ko", "");  //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale PRC = new Locale("zh", "CN");  //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale SIMPLIFIED_CHINESE = new Locale("zh", "CN");  //$NON-NLS-1$//$NON-NLS-2$

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
    public static final Locale US = new Locale("en", "US");  //$NON-NLS-1$//$NON-NLS-2$

    private static final PropertyPermission setLocalePermission = new PropertyPermission(
            "user.language", "write");  //$NON-NLS-1$//$NON-NLS-2$

    static {
        String language = AccessController
                .doPrivileged(new PriviAction<String>("user.language", "en")); //$NON-NLS-1$ //$NON-NLS-2$
        // BEGIN android-changed
        String region = AccessController
                .doPrivileged(new PriviAction<String>("user.region", "US")); //$NON-NLS-1$ //$NON-NLS-2$
        // END android-changed
        String variant = AccessController
                .doPrivileged(new PriviAction<String>("user.variant", "")); //$NON-NLS-1$ //$NON-NLS-2$
        defaultLocale = new Locale(language, region, variant);
    }
    
    private transient String countryCode;
    private transient String languageCode;
    private transient String variantCode;

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
     * Constructs a new Locale using the specified language.
     * @param language 
     * 
     */
    public Locale(String language) {
        this(language, "", "");  //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Constructs a new Locale using the specified language and country codes.
     * @param language 
     * @param country 
     * 
     */
    public Locale(String language, String country) {
        this(language, country, ""); //$NON-NLS-1$
    }

    /**
     * Constructs a new Locale using the specified language, country, and
     * variant codes.
     * 
     * @param language
     * @param country
     * @param variant
     * @throws NullPointerException if <code>language</code>,
     *         <code>country</code> or <code>variant</code> is
     *         <code>null</code>.
     */
    public Locale(String language, String country, String variant) {
        if (language == null || country == null || variant == null) {
            throw new NullPointerException();
        }
        languageCode = Util.toASCIILowerCase(language);
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
        countryCode = Util.toASCIIUpperCase(country);

        variantCode = variant;
    }

    /**
     * Returns a new Locale with the same language, country and variant codes as
     * this Locale.
     * 
     * @return a shallow copy of this Locale
     * 
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Compares the specified object to this Locale and answer if they are
     * equal. The object must be an instance of Locale and have the same
     * language, country and variant.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this Locale, false
     *         otherwise
     * 
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

    // BEGIN android-removed
    // static Locale[] find(String prefix) {
    //     int last = prefix.lastIndexOf('/');
    //     final String thePackage = prefix.substring(0, last + 1);
    //     int length = prefix.length();
    //     final String classPrefix = prefix.substring(last + 1, length);
    //     Set<String> result = new HashSet<String>();
    //     StringTokenizer paths = new StringTokenizer(System.getProperty(
    //             // BEGIN android-removed
    //             // "org.apache.harmony.boot.class.path", ""), System.getProperty( //$NON-NLS-1$ //$NON-NLS-2$
    //             // END android-removed
    //             // BEGIN android-added
    //             "java.boot.class.path", ""), System.getProperty( //$NON-NLS-1$ //$NON-NLS-2$
    //             // END android-added
    //             "path.separator", ";"));  //$NON-NLS-1$//$NON-NLS-2$
    //     while (paths.hasMoreTokens()) {
    //         String nextToken = paths.nextToken();
    //         File directory = new File(nextToken);
    //         if (directory.exists()) {
    //             if (directory.isDirectory()) {
    //                 String path;
    //                 try {
    //                     path = directory.getCanonicalPath();
    //                 } catch (IOException e) {
    //                     continue;
    //                 }
    //                 File newDir;
    //                 if (path.charAt(path.length() - 1) == File.separatorChar) {
    //                     newDir = new File(path + thePackage);
    //                 } else {
    //                     newDir = new File(path + File.separatorChar
    //                             + thePackage);
    //                 }
    //                 if (newDir.isDirectory()) {
    //                     String[] list = newDir.list();
    //                     for (int i = 0; i < list.length; i++) {
    //                         String name = list[i];
    //                         if (name.startsWith(classPrefix)
    //                                 && name.endsWith(".class")) { //$NON-NLS-1$
    //                             result.add(name.substring(0,
    //                                             name.length() - 6));
    //                         }
    //                     }
    //                 }
    //                 
    //             } else {
    //                 // Handle ZIP/JAR files.
    //                 try {
    //                     ZipFile zip = new ZipFile(directory);
    //                     Enumeration<? extends ZipEntry> entries = zip.entries();
    //                     while (entries.hasMoreElements()) {
    //                         ZipEntry e = entries.nextElement();
    //                         String name = e.getName();
    //                         if (name.startsWith(prefix)
    //                                 && name.endsWith(".class")) {//$NON-NLS-1$
    //                             result.add(name.substring(last+1, name.length() - 6));
    //                         }
    //                     }
    //                     zip.close();
    //                 } catch (IOException e) {
    //                    // Empty
    //                 }
    //             }
    //         }
    //     }
    //     Locale[] locales = new Locale[result.size()];
    //     int i = 0;
    //     for (String name: result) {
    //         int index = name.indexOf('_');
    //         int nextIndex = name.indexOf('_', index + 1);
    //         if (nextIndex == -1) {
    //             locales[i++] = new Locale(name
    //                     .substring(index + 1, name.length()), ""); //$NON-NLS-1$
    //         }else{
    //             String language = name.substring(index + 1, nextIndex);
    //             String variant;
    //             if ((index = name.indexOf('_', nextIndex + 1)) == -1) {
    //                 variant = ""; //$NON-NLS-1$
    //                 index = name.length();
    //             } else {
    //                 variant = name.substring(index + 1, name.length());
    //             }
    //             String country = name.substring(nextIndex + 1, index);
    //             locales[i++] = new Locale(language, country, variant);
    //         }
    //     }
    //     return locales;
    // }
    // END android-removed

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
     * Gets the list of installed Locales.
     * 
     * @return an array of Locale
     */
    public static Locale[] getAvailableLocales() {
        if (availableLocales == null) {
            // BEGIN android-removed
            // availableLocales = AccessController
            //         .doPrivileged(new PrivilegedAction<Locale[]>() {
            //             public Locale[] run() {
            //                 return find("org/apache/harmony/luni/internal/locale/Locale_"); //$NON-NLS-1$
            //             }
            //         });
            // END android-removed
            
            // BEGIN android-added
            availableLocales = find();
            // END android-added
        }
        return availableLocales.clone();
    }

    /**
     * Gets the country code for this Locale.
     * 
     * @return a country code
     */
    public String getCountry() {
        return countryCode;
    }

    /**
     * Gets the default Locale.
     * 
     * @return the default Locale
     */
    public static Locale getDefault() {
        return defaultLocale;
    }

    /**
     * Gets the full country name in the default Locale for the country code of
     * this Locale. If there is no matching country name, the country code is
     * returned.
     * 
     * @return a country name
     */
    public final String getDisplayCountry() {
        return getDisplayCountry(getDefault());
    }

    /**
     * Gets the full country name in the specified Locale for the country code
     * of this Locale. If there is no matching country name, the country code is
     * returned.
     * 
     * @param locale
     *            the Locale
     * @return a country name
     */
    public String getDisplayCountry(Locale locale) {
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
    }

    /**
     * Gets the full language name in the default Locale for the language code
     * of this Locale. If there is no matching language name, the language code
     * is returned.
     * 
     * @return a language name
     */
    public final String getDisplayLanguage() {
        return getDisplayLanguage(getDefault());
    }

    /**
     * Gets the full language name in the specified Locale for the language code
     * of this Locale. If there is no matching language name, the language code
     * is returned.
     * 
     * @param locale
     *            the Locale
     * @return a language name
     */
    public String getDisplayLanguage(Locale locale) {
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
    }

    /**
     * Gets the full language, country, and variant names in the default Locale
     * for the codes of this Locale.
     * 
     * @return a Locale name
     */
    public final String getDisplayName() {
        return getDisplayName(getDefault());
    }

    /**
     * Gets the full language, country, and variant names in the specified
     * Locale for the codes of this Locale.
     * 
     * @param locale
     *            the Locale
     * @return a Locale name
     */
    public String getDisplayName(Locale locale) {
        int count = 0;
        StringBuffer buffer = new StringBuffer();
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
     * Gets the full variant name in the default Locale for the variant code of
     * this Locale. If there is no matching variant name, the variant code is
     * returned.
     * 
     * @return a variant name
     */
    public final String getDisplayVariant() {
        return getDisplayVariant(getDefault());
    }

    /**
     * Gets the full variant name in the specified Locale for the variant code
     * of this Locale. If there is no matching variant name, the variant code is
     * returned.
     * 
     * @param locale
     *            the Locale
     * @return a variant name
     */
    public String getDisplayVariant(Locale locale) {
        if (variantCode.length() == 0) {
            return variantCode;
        }
// BEGIN android-removed
//         ResourceBundle bundle;
//         try {
//             bundle = getBundle("Variant", locale); //$NON-NLS-1$
//         } catch (MissingResourceException e) {
//             return variantCode.replace('_', ',');
//         }
// 
//         StringBuffer result = new StringBuffer();
//         StringTokenizer tokens = new StringTokenizer(variantCode, "_"); //$NON-NLS-1$
//         while (tokens.hasMoreTokens()) {
//             String code, variant = tokens.nextToken();
//             try {
//                 code = bundle.getString(variant);
//             } catch (MissingResourceException e) {
//                 code = variant;
//             }
//             result.append(code);
//             if (tokens.hasMoreTokens()) {
//                 result.append(',');
//             }
//         }
//         return result.toString();
// END android-removed
// BEGIN android-added
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
// END android-added
    }

    /**
     * Gets the three letter ISO country code which corresponds to the country
     * code for this Locale.
     * 
     * @return a three letter ISO language code
     * 
     * @exception MissingResourceException
     *                when there is no matching three letter ISO country code
     */
    public String getISO3Country() throws MissingResourceException {
        if (countryCode.length() == 0) {
            return ""; //$NON-NLS-1$
        }
        ResourceBundle bundle = getBundle("ISO3Countries", this); //$NON-NLS-1$
        return bundle.getString(this.toString());
    }

    /**
     * Gets the three letter ISO language code which corresponds to the language
     * code for this Locale.
     * 
     * @return a three letter ISO language code
     * 
     * @exception MissingResourceException
     *                when there is no matching three letter ISO language code
     */
    public String getISO3Language() throws MissingResourceException {
        if (languageCode.length() == 0) {
            return ""; //$NON-NLS-1$
        }
        ResourceBundle bundle = getBundle("ISO3Languages", this); //$NON-NLS-1$
        return bundle.getString(this.toString());
    }

    /**
     * Gets the list of two letter ISO country codes which can be used as the
     * country code for a Locale.
     * 
     * @return an array of String
     */
    public static String[] getISOCountries() {
        // BEGIN android-removed
        // ListResourceBundle bundle = new Country();
        // 
        // // To initialize the table
        // Enumeration<String> keys = bundle.getKeys();
        // int size = bundle.table.size();
        // String[] result = new String[size];
        // int index = 0;
        // while (keys.hasMoreElements()) {
        //     String element = keys.nextElement();
        //     result[index++] = element;
        // }
        // return result;
        // END android-removed
        
        // BEGIN android-added
        return Resources.getISOCountries();
        // END android-added
    }

    /**
     * Gets the list of two letter ISO language codes which can be used as the
     * language code for a Locale.
     * 
     * @return an array of String
     */
    public static String[] getISOLanguages() {
        // BEGIN android-removed
        // ListResourceBundle bundle = new Language();
        // Enumeration<String> keys = bundle.getKeys(); // to initialize the table
        // String[] result = new String[bundle.table.size()];
        // int index = 0;
        // while (keys.hasMoreElements()) {
        //     result[index++] = keys.nextElement();
        // }
        // return result;
        // END android-removed
        
        // BEGIN android-added
        return Resources.getISOLanguages();
        // END android-added
    }

    /**
     * Gets the language code for this Locale.
     * 
     * @return a language code
     */
    public String getLanguage() {
        return languageCode;
    }

    /**
     * Gets the variant code for this Locale.
     * 
     * @return a variant code
     */
    public String getVariant() {
        return variantCode;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    @Override
    public synchronized int hashCode() {
        return countryCode.hashCode() + languageCode.hashCode()
                + variantCode.hashCode();
    }

    /**
     * Sets the default Locale to the specified Locale.
     * 
     * @param locale
     *            the new default Locale
     * 
     * @exception SecurityException
     *                when there is a security manager which does not allow this
     *                operation
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
     * Returns the string representation of this Locale.
     * 
     * @return the string representation of this Locale
     */
    @Override
    public final String toString() {
        StringBuilder result = new StringBuilder();
        result.append(languageCode);
        if (countryCode.length() > 0) {
            result.append('_');
            result.append(countryCode);
        }
        if (variantCode.length() > 0 && result.length() > 0 ) {
            if (0 == countryCode.length()) {
                result.append("__"); //$NON-NLS-1$
            } else {
                result.append('_');
            }
            result.append(variantCode);
        }
        return result.toString();
    }

    static ResourceBundle getBundle(final String clName, final Locale locale) {
        return AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>() {
                    public ResourceBundle run() {
                        return ResourceBundle.getBundle("org.apache.harmony.luni.internal.locale." //$NON-NLS-1$
                                + clName, locale);
                    }
                });
    }

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
        countryCode = (String) fields.get("country", "");  //$NON-NLS-1$//$NON-NLS-2$
        languageCode = (String) fields.get("language", "");  //$NON-NLS-1$//$NON-NLS-2$
        variantCode = (String) fields.get("variant", "");  //$NON-NLS-1$//$NON-NLS-2$
    }
}

