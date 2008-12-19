/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.util.prefs;

// BEGIN android-added
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
// END android-added

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.harmony.prefs.internal.nls.Messages;

/**
 * An instance of the class {@code Preferences} represents one node in a preference tree,
 * which provides a mechanism to store and access configuration data in a
 * hierarchical way. Two hierarchy trees are maintained, one for system
 * preferences shared by all users and the other for user preferences 
 * specific to the user. {@code Preferences} hierarchy trees and data are stored
 * in an implementation-dependent back-end.
 * <p>
 * Every node has one name and one unique absolute path following the same 
 * notational conventions as directories in a file system. The root node's 
 * name is "", and other node name strings cannot contain the slash character 
 * and cannot be empty. The root node's absolute path is "/", and all other 
 * nodes' absolute paths are constructed in the standard way: &lt;parent's absolute
 * path&gt; + "/" + &lt;node's name&gt;. Since the set of nodes forms a tree with 
 * the root node at its base, all absolute paths start with the slash character.
 * Every node has one relative path to each of its ancestors. The relative path
 * doesn't start with slash: it equals the node's absolute path with leading 
 * substring removed corresponding to the ancestor's absolute path and a slash.
 * </p>
 * <p>
 * Modification to preferences data may be asynchronous, which means that 
 * preference update method calls may return immediately instead of blocking. 
 * The {@code flush()} and {@code sync()} methods force the back-end to 
 * synchronously perform all pending updates, but the implementation is 
 * permitted to perform the modifications on the underlying back-end data 
 * at any time between the moment the request is made and the moment the 
 * {@code flush()} or {@code sync()} method returns.
 * Please note that if JVM exit normally, the implementation must assure all
 * modifications are persisted implicitly.
 * </p>
 * <p>
 * When invoking a method that retrieves preferences, the user must provide 
 * a default value. The default value is returned when the preferences cannot 
 * be found or the back-end is unavailable. Some other methods will throw 
 * {@code BackingStoreException} when the back-end is unavailable.
 * </p>
 * <p>
 * Preferences can be exported to and imported from an XML files.
 * <p>
 * There must be a concrete {@code PreferencesFactory} type for every concrete
 * {@code Preferences} type developed. Every J2SE implementation must provide a default
 * implementation for every supported platform, and must also provide a means of
 * replacing the default implementation. This implementation uses the system property
 * {@code java.util.prefs.PreferencesFactory} to detemine which preferences 
 * implementation to use.
 * </p>
 * <p>
 * The methods of this class are thread-safe. If multiple JVMs are using the same
 * back-end concurrently, the back-end won't be corrupted, but no other
 * behavior guarantees are made.
 * </p>
 * 
 * @since Android 1.0
 */
public abstract class Preferences {
    
    /*
     * ---------------------------------------------------------
     * Class fields 
     * ---------------------------------------------------------
     */
    
    /**
     * Maximum size in characters allowed for a preferences key.
     * 
     * @since Android 1.0
     */
    public static final int MAX_KEY_LENGTH = 80;
    
    /**
     * Maximum size in characters allowed for a preferences name.
     * 
     * @since Android 1.0
     */
    public static final int MAX_NAME_LENGTH = 80;
    
    /**
     * Maximum size in characters allowed for a preferences value.
     * 
     * @since Android 1.0
     */
    public static final int MAX_VALUE_LENGTH = 8192;

    // BEGIN android-added
    /**
     * The name of the configuration file where preferences factory class names
     * can be specified.
     */
    private static final String FACTORY_CONFIGURATION_FILE_NAME = "META-INF/services/java.util.prefs.PreferencesFactory"; //$NON-NLS-1$

    /**
     * The encoding of configuration files
     */
    private static final String CONFIGURATION_FILE_ENCODING = "UTF-8"; //$NON-NLS-1$

    /**
     * The comment string used in configuration files
     */
    private static final String CONFIGURATION_FILE_COMMENT = "#"; //$NON-NLS-1$

    // END android-added

    //permission
    private static final RuntimePermission PREFS_PERM = new RuntimePermission("preferences"); //$NON-NLS-1$
    
    //factory used to get user/system prefs root
    private static final PreferencesFactory factory;
    
    /**
     * ---------------------------------------------------------
     * Class initializer
     * ---------------------------------------------------------
     */        
    static{
        String factoryClassName = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty("java.util.prefs.PreferencesFactory"); //$NON-NLS-1$
            }
        });
        // BEGIN android-removed
        // if(factoryClassName != null) {
        // try {
        // ClassLoader loader = Thread.currentThread().getContextClassLoader();
        // if(loader == null){
        // loader = ClassLoader.getSystemClassLoader();
        // }
        // Class<?> factoryClass = loader.loadClass(factoryClassName);
        // factory = (PreferencesFactory) factoryClass.newInstance();
        // } catch (Exception e) {
        // // prefs.10=Cannot initiate PreferencesFactory: {0}. Caused by {1}
        //         throw new InternalError(Messages.getString("prefs.10", factoryClassName, e));   //$NON-NLS-1$
        // }
        // }
        // END android-removed
        // BEGIN android-added
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        if (factoryClassName == null) {
            Enumeration<URL> en = null;
            try {
                en = loader.getResources(FACTORY_CONFIGURATION_FILE_NAME);
                BufferedReader reader = null;
                int commentIndex = 0;
                while (en.hasMoreElements()) {
                    try {
                        InputStream is = en.nextElement().openStream();
                        // Read each line for charset provider class names
                        // BEGIN android-modified
                        reader = new BufferedReader(new InputStreamReader(is,
                                CONFIGURATION_FILE_ENCODING), 8192);
                        // END android-modified
                        factoryClassName = reader.readLine();
                        commentIndex = factoryClassName.indexOf(CONFIGURATION_FILE_COMMENT);
                        if (commentIndex > 0) {
                            factoryClassName = factoryClassName.substring(0, commentIndex).trim();
                        }
                        if (factoryClassName.length() > 0) {
                            break;
                        }
                    } catch (IOException ex) {
                        // ignore if a resource couldn't be read
                    }
                }
            } catch (Exception e) {
                // prefs.10=Cannot initiate PreferencesFactory: {0}. Caused by
                // {1}
                throw new InternalError(Messages.getString("prefs.10",
                        FACTORY_CONFIGURATION_FILE_NAME, e)); //$NON-NLS-1$
            }
        }

        if (factoryClassName == null) {
            factoryClassName = "java.util.prefs.FilePreferencesFactoryImpl";
        }

        try {
            Class<?> c = loader.loadClass(factoryClassName);
            factory = (PreferencesFactory)c.newInstance();
        } catch (Exception e) {
            // prefs.10=Cannot initiate PreferencesFactory: {0}. Caused by {1}
            throw new InternalError(Messages.getString("prefs.10", factoryClassName, e)); //$NON-NLS-1$  
        }
        // END android-added
    }
    
    /*
     * ---------------------------------------------------------
     * Constructors
     * ---------------------------------------------------------
     */
    
    /**
     * Default constructor, for use by subclasses only.
     * 
     * @since Android 1.0
     */
    protected Preferences() {
        super();
    }
    
    /*
     * ---------------------------------------------------------
     * Methods
     * ---------------------------------------------------------
     */
    
    /**
     * Gets the absolute path string of this preference node.
     * 
     * @return the preference node's absolute path string.
     * @since Android 1.0
     */
    public abstract String absolutePath();
    
    /**
     * Returns the names of all children of this node or an empty string if this
     * node has no children.
     * 
     * @return the names of all children of this node.
     * @throws BackingStoreException
     *             if backing store is unavailable or causes an operation
     *             failure.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract String[] childrenNames() throws BackingStoreException;
    
    /**
     * Removes all preferences of this node.
     * 
     * @throws BackingStoreException
     *             if backing store is unavailable or causes an operation
     *             failure.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void clear() throws BackingStoreException;
    
    /**
     * Exports all of the preferences of this node to a XML document using the given
     * output stream.
     * <p>
     * This XML document uses the UTF-8 encoding and is written according to the
     * DTD in its DOCTYPE declaration, which is the following:
     * 
     * <pre>
     * &lt;!DOCTYPE preferences SYSTEM &quot;http://java.sun.com/dtd/preferences.dtd&quot;&gt;
     * </pre>
     * 
     * <i>Please note that (unlike the methods of this class that don't concern serialization), this call is not thread-safe.</i>
     * </p>
     * 
     * @param ostream
     *            the output stream to write the XML-formatted data to.
     * @throws IOException
     *             if an error occurs while exporting.
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void exportNode (OutputStream ostream) throws IOException, BackingStoreException;
    
    /**
     * Exports all of the preferences of this node and all its descendants to a XML
     * document using the given output stream.
     * <p>
     * This XML document uses the UTF-8 encoding and is written according to the
     * DTD in its DOCTYPE declaration, which is the following:
     * 
     * <pre>
     * &lt;!DOCTYPE preferences SYSTEM &quot;http://java.sun.com/dtd/preferences.dtd&quot;&gt;
     * </pre>
     * 
     * <i>Please note that (unlike the methods of this class that don't concern serialization), this call is not thread-safe.</i>
     * </p>
     * 
     * @param ostream
     *            the output stream to write the XML-formatted data to.
     * @throws IOException
     *             if an error occurs while exporting.
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void exportSubtree (OutputStream ostream) throws IOException, BackingStoreException;
    
    /**
     * Forces all pending updates to this node and its descendants to be 
     * persisted in the backing store.
     * <p>
     * If this node has been removed, the invocation of this method only flushes
     * this node, not its descendants.
     * </p>
     * 
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @since Android 1.0
     */
    public abstract void flush() throws BackingStoreException;
    
    /**
     * Gets the {@code String} value mapped to the given key or its default value if no
     * value is mapped or no backing store is available.
     * <p>
     * Some implementations may store default values in backing stores. In this
     * case, if there is no value mapped to the given key, the stored default
     * value is returned.
     * </p>
     * 
     * @param key
     *            the preference key.
     * @param deflt
     *            the default value, which will be returned if no value is
     *            mapped to the given key or no backing store is available.
     * @return the preference value mapped to the given key.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @throws NullPointerException
     *             if the parameter {@code key} is {@code null}.
     * @since Android 1.0
     */
    public abstract String get (String key, String deflt);
    
    /**
     * Gets the {@code boolean} value mapped to the given key or its default value if no
     * value is mapped, if the backing store is unavailable, or if the value is invalid.
     * <p>
     * The only valid values are the {@code String} "true", which represents {@code true} and
     * "false", which represents {@code false}, ignoring case.
     * </p>
     * <p>
     * Some implementations may store default values in backing stores. In this
     * case, if there is no value mapped to the given key, the stored default
     * value is returned.
     * </p>
     * 
     * @param key
     *            the preference key.
     * @param deflt
     *            the default value, which will be returned if no value is
     *            mapped to the given key, if the backing store is unavailable, or if the
     *            value is invalid.
     * @return the boolean value mapped to the given key.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @throws NullPointerException
     *             if the parameter {@code key} is {@code null}.
     * @since Android 1.0
     */
    public abstract boolean getBoolean (String key, boolean deflt);
    
    /**
     * Gets the {@code byte} array value mapped to the given key or its default value if
     * no value is mapped, if the backing store is unavailable, or if the value is an
     * invalid string.
     * <p>
     * To be valid, the value string must be Base64-encoded binary data. The Base64 encoding
     * is as defined in <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC
     * 2045</a>, section 6.8.
     * </p>
     * <p>
     * Some implementations may store default values in backing stores. In this
     * case, if there is no value mapped to the given key, the stored default
     * value is returned.
     * </p>
     * 
     * @param key
     *            the preference key.
     * @param deflt
     *            the default value, which will be returned if no value is
     *            mapped to the given key, if the backing store is unavailable, or if the
     *            value is invalid.
     * @return the byte array value mapped to the given key.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @throws NullPointerException
     *             if the parameter {@code key} is {@code null}.
     * @since Android 1.0
     */
    public abstract byte[] getByteArray (String key, byte[] deflt);
    
    /**
     * Gets the {@code double} value mapped to the given key or its default value if no
     * value is mapped, if the backing store is unavailable, or if the value is an invalid
     * string.
     * <p>
     * To be valid, the value string must be a string that can be converted to a {@code double} by
     * {@link Double#parseDouble(String) Double.parseDouble(String)}.
     * </p>
     * <p>
     * Some implementations may store default values in backing stores. In this
     * case, if there is no value mapped to the given key, the stored default
     * value is returned.
     * </p>
     * 
     * @param key
     *            the preference key.
     * @param deflt
     *            the default value, which will be returned if no value is
     *            mapped to the given key, if the backing store is unavailable, or if the
     *            value is invalid.
     * @return the double value mapped to the given key.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @throws NullPointerException
     *             if the parameter {@code key} is {@code null}.
     * @since Android 1.0
     */
    public abstract double getDouble (String key, double deflt);
    
    /**
     * Gets the {@code float} value mapped to the given key or its default value if no
     * value is mapped, if the backing store is unavailable, or if the value is an invalid
     * string.
     * <p>
     * To be valid, the value string must be a string that can be converted to a {@code float} by
     * {@link Float#parseFloat(String) Float.parseFloat(String)}.
     * </p>
     * <p>
     * Some implementations may store default values in backing stores. In this
     * case, if there is no value mapped to the given key, the stored default
     * value is returned.
     * </p>
     * 
     * @param key
     *            the preference key.
     * @param deflt
     *            the default value, which will be returned if no value is
     *            mapped to the given key, if the backing store is unavailable, or if the
     *            value is invalid.
     * @return the float value mapped to the given key.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @throws NullPointerException
     *             if the parameter {@code key} is {@code null}.
     * @since Android 1.0
     */
    public abstract float getFloat (String key, float deflt);
    
    /**
     * Gets the {@code int} value mapped to the given key or its default value if no
     * value is mapped, if the backing store is unavailable, or if the value is an invalid
     * string.
     * <p>
     * To be valid, the value string must be a string that can be converted to an {@code int} by
     * {@link Integer#parseInt(String) Integer.parseInt(String)}.
     * </p>
     * <p>
     * Some implementations may store default values in backing stores. In this
     * case, if there is no value mapped to the given key, the stored default
     * value is returned.
     * </p>
     * 
     * @param key
     *            the preference key.
     * @param deflt
     *            the default value, which will be returned if no value is
     *            mapped to the given key, if the backing store is unavailable, or if the
     *            value is invalid.
     * @return the integer value mapped to the given key.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @throws NullPointerException
     *             if the parameter {@code key} is {@code null}.
     * @since Android 1.0
     */
    public abstract int getInt (String key, int deflt);
    
    /**
     * Gets the {@code long} value mapped to the given key or its default value if no
     * value is mapped, if the backing store is unavailable, or if the value is an invalid
     * string.
     * <p>
     * To be valid, the value string must be a string that can be converted to a {@code long} by
     * {@link Long#parseLong(String) Long.parseLong(String)}.
     * </p>
     * <p>
     * Some implementations may store default values in backing stores. In this
     * case, if there is no value mapped to the given key, the stored default
     * value is returned.
     * </p>
     * 
     * @param key
     *            the preference key.
     * @param deflt
     *            the default value, which will be returned if no value is
     *            mapped to the given key, if the backing store is unavailable, or if the
     *            value is invalid.
     * @return the long value mapped to the given key.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @throws NullPointerException
     *             if the parameter {@code key} is {@code null}.
     * @since Android 1.0
     */
    public abstract long getLong (String key, long deflt);
    
    /**
     * Imports all the preferences from an XML document using the given input
     * stream.
     * <p>
     * This XML document uses the UTF-8 encoding and must be written according to the
     * DTD in its DOCTYPE declaration, which must be the following:
     * 
     * <pre>
     * &lt;!DOCTYPE preferences SYSTEM &quot;http://java.sun.com/dtd/preferences.dtd&quot;&gt;
     * </pre>
     * 
     * <i>Please note that (unlike the methods of this class that don't concern serialization), this call is not thread-safe.</i>
     * </p>
     * 
     * @param istream
     *            the input stream to read the data from.
     * @throws InvalidPreferencesFormatException
     *             if the data read from the given input stream is not from a
     *             valid XML document.
     * @throws IOException
     *             if an error occurs while importing.
     * @throws SecurityException
     *             if {@code RuntimePermission("preferences")} is denied by a
     *             SecurityManager.
     * @since Android 1.0
     */
    public static void importPreferences (InputStream istream) throws InvalidPreferencesFormatException, IOException {
        checkSecurity();
        if(null == istream){
            // prefs.0=Inputstream cannot be null\!
            throw new MalformedURLException(Messages.getString("prefs.0")); //$NON-NLS-1$
        }
        XMLParser.importPrefs(istream);
    }
    
    /**
     * Returns whether this is a user preference node.
     * 
     * @return {@code true}, if this is a user preference node, {@code false} if
     *         this is a system preference node.
     * @since Android 1.0
     */
    public abstract boolean isUserNode();
    
    /**
     * Returns all preference keys stored in this node or an empty array if no
     * key was found.
     * 
     * @return the list of all preference keys of this node.
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract String[] keys() throws BackingStoreException;
    
    /**
     * Returns the name of this node.
     * 
     * @return the name of this node.
     * @since Android 1.0
     */
    public abstract String name();
    
    /**
     * Returns the preference node with the given path name. The path name can
     * be relative or absolute. The requested node and its ancestors will
     * be created if they do not exist.
     * <p>
     * The path is treated as relative to this node if it doesn't start with a
     * slash, otherwise it will be treated as an absolute path.
     * </p>
     * 
     * @param path
     *            the path name of the requested preference node.
     * @return the requested preference node.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @throws IllegalArgumentException
     *             if the path name is invalid.
     * @throws NullPointerException
     *             if the given path is {@code null}.
     * @since Android 1.0
     */
    public abstract Preferences node (String path);
    
    /**
     * Returns whether the preference node with the given path name exists. The
     * path is treated as relative to this node if it doesn't start with a slash,
     * otherwise it is treated as an absolute path.
     * <p>
     * Please note that if this node has been removed, an invocation of this
     * node will throw an {@code IllegalStateException} unless the given path is
     * an empty string, which will return {@code false}.
     * </p>
     * 
     * @param path
     *            the path name of the preference node to query.
     * @return {@code true}, if the queried preference node exists, {@code false}
     *         otherwise.
     * @throws IllegalStateException
     *             if this node has been removed and the path is not an empty
     *             string.
     * @throws IllegalArgumentException
     *             if the path name is invalid.
     * @throws NullPointerException
     *             if the given path is {@code null}.
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @since Android 1.0
     */
    public abstract boolean nodeExists (String path) throws BackingStoreException;
    
    /**
     * Returns the parent preference node of this node or {@code null} if this
     * node is the root node.
     * 
     * @return the parent preference node of this node.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract Preferences parent();
    
    /**
     * Adds a new preference to this node using the given key and value or
     * updates the value if a preference with the given key already exists.
     * 
     * @param key
     *            the preference key to be added or updated.
     * @param value
     *            the preference value for the given key.
     * @throws NullPointerException
     *             if the given key or value is {@code null}.
     * @throws IllegalArgumentException
     *             if the given key's length is bigger than {@code
     *             MAX_KEY_LENGTH} or the value's length is bigger than {@code
     *             MAX_VALUE_LENGTH}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void put (String key, String value);
    
    /**
     * Adds a new preference with a {@code boolean} value to this node using the given
     * key and value or updates the value if a preference with the given key
     * already exists.
     * 
     * @param key
     *            the preference key to be added or updated.
     * @param value
     *            the preference {@code boolean} value for the given key.
     * @throws NullPointerException
     *             if the given key is {@code null}.
     * @throws IllegalArgumentException
     *             if the given key's length is bigger than {@code
     *             MAX_KEY_LENGTH}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void putBoolean (String key, boolean value);
    
    /**
     * Adds a new preference to this node using the given key and the string
     * form of the given value or updates the value if a preference with the
     * given key already exists.
     * <p>
     * The string form of the value is the Base64-encoded binary data of the
     * given byte array. The Base64 encoding is as defined in <a
     * href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>, section 6.8.
     * </p>
     * 
     * @param key
     *            the preference key to be added or updated.
     * @param value
     *            the preference value for the given key.
     * @throws NullPointerException
     *             if the given key or value is {@code null}.
     * @throws IllegalArgumentException
     *             if the given key's length is bigger than {@code
     *             MAX_KEY_LENGTH} or value's length is bigger than three
     *             quarters of {@code MAX_KEY_LENGTH}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void putByteArray (String key, byte[] value);
    
    /**
     * Adds a new preference to this node using the given key and {@code double}
     * value or updates the value if a preference with the
     * given key already exists.
     * <p>
     * The value is stored in its string form, which is the result of invoking
     * {@link Double#toString(double) Double.toString(double)}.
     * </p>
     * 
     * @param key
     *            the preference key to be added or updated.
     * @param value
     *            the preference value for the given key.
     * @throws NullPointerException
     *             if the given key is {@code null}.
     * @throws IllegalArgumentException
     *             if the given key's length is bigger than {@code
     *             MAX_KEY_LENGTH}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void putDouble (String key, double value);
    
    /**
     * Adds a new preference to this node using the given key and {@code float} 
     * value or updates the value if a preference with the
     * given key already exists.
     * <p>
     * The value is stored in its string form, which is the result of invoking
     * {@link Float#toString(float) Float.toString(float)}.
     * </p>
     * 
     * @param key
     *            the preference key to be added or updated.
     * @param value
     *            the preference value for the given key.
     * @throws NullPointerException
     *             if the given key is {@code null}.
     * @throws IllegalArgumentException
     *             if the given key's length is bigger than {@code
     *             MAX_KEY_LENGTH}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void putFloat (String key, float value);
    
    /**
     * Adds a new preference to this node using the given key and {@code int} 
     * value or updates the value if a preference with the
     * given key already exists.
     * <p>
     * The value is stored in its string form, which is the result of invoking
     * {@link Integer#toString(int) Integer.toString(int)}.
     * </p>
     * 
     * @param key
     *            the preference key to be added or updated.
     * @param value
     *            the preference value for the given key.
     * @throws NullPointerException
     *             if the given key is {@code null}.
     * @throws IllegalArgumentException
     *             if the given key's length is bigger than {@code
     *             MAX_KEY_LENGTH}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void putInt (String key, int value);
    
    /**
     * Adds a new preference to this node using the given key and {@code long} 
     * value or updates the value if a preference with the
     * given key already exists.
     * <p>
     * The value is stored in its string form, which is the result of invoking
     * {@link Long#toString(long) Long.toString(long)}.
     * </p>
     * 
     * @param key
     *            the preference key to be added or updated.
     * @param value
     *            the preference value for the given key.
     * @throws NullPointerException
     *             if the given key is {@code null}.
     * @throws IllegalArgumentException
     *             if the given key's length is bigger than {@code
     *             MAX_KEY_LENGTH}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void putLong (String key, long value);

    /**
     * Removes the preference mapped to the given key from this node.
     * 
     * @param key
     *            the key of the preference to be removed.
     * @throws NullPointerException
     *             if the given key is {@code null}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void remove (String key);
    
    /**
     * Removes this preference node with all its descendants. The removal 
     * won't necessarily be persisted until the method {@code flush()} is invoked.
     * 
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @throws UnsupportedOperationException
     *             if this is a root node.
     * @since Android 1.0
     */
    public abstract void removeNode() throws BackingStoreException;
    
    /**
     * Registers a {@code NodeChangeListener} instance for this node, which will handle
     * {@code NodeChangeEvent}s. {@code NodeChangeEvent}s will be fired when a child node has
     * been added to or removed from this node.
     * 
     * @param ncl
     *            the listener to be registered.
     * @throws NullPointerException
     *             if the given listener is {@code null}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void addNodeChangeListener (NodeChangeListener ncl);
    
    /**
     * Registers a {@code PreferenceChangeListener} instance for this node, which will
     * handle {@code PreferenceChangeEvent}s. {@code PreferenceChangeEvent}s will be fired when
     * a preference has been added to, removed from, or updated for this node.
     * 
     * @param pcl
     *            the listener to be registered.
     * @throws NullPointerException
     *             if the given listener is {@code null}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void addPreferenceChangeListener (PreferenceChangeListener pcl);
    
    /**
     * Removes the given {@code NodeChangeListener} instance from this node.
     * 
     * @param ncl
     *            the listener to be removed.
     * @throws IllegalArgumentException
     *             if the given listener is {@code null}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void removeNodeChangeListener (NodeChangeListener ncl);
    
    /**
     * Removes the given {@code PreferenceChangeListener} instance from this node.
     * 
     * @param pcl
     *            the listener to be removed.
     * @throws IllegalArgumentException
     *             if the given listener is {@code null}.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void removePreferenceChangeListener (PreferenceChangeListener pcl);
    
    /**
     * Synchronizes the data of this preference node and its descendants with
     * the back-end preference store. Any changes found in the back-end data should be reflected
     * in this node and its descendants, and at the same time any local changes to this node and
     * descendants should be persisted.
     * 
     * @throws BackingStoreException
     *             if the backing store is unavailable or causes an operation
     *             failure.
     * @throws IllegalStateException
     *             if this node has been removed.
     * @since Android 1.0
     */
    public abstract void sync() throws BackingStoreException;
    
    /**
     * Returns the system preference node for the package of the given class.
     * The absolute path of the returned node is one slash followed by the given
     * class's full package name, replacing each period character ('.') with
     * a slash. For example, the absolute path of the preference associated with 
     * the class Object would be "/java/lang". As a special case, the unnamed
     * package is associated with a preference node "/&lt;unnamed&gt;". This
     * method will create the node and its ancestors as needed. Any nodes created
     * by this method won't necessarily be persisted until the method {@code flush()} is
     * invoked.
     * 
     * @param c
     *            the given class.
     * @return the system preference node for the package of the given class.
     * @throws NullPointerException
     *             if the given class is {@code null}.
     * @throws SecurityException
     *             if the {@code RuntimePermission("preferences")} is denied by
     *             a SecurityManager.
     * @since Android 1.0
     */
    public static Preferences systemNodeForPackage (Class<?> c) {
        checkSecurity();
        return factory.systemRoot().node(getNodeName(c));
    }
    
    /**
     * Returns the root node of the system preference hierarchy.
     * 
     * @return the system preference hierarchy root node.
     * @throws SecurityException
     *             if the {@code RuntimePermission("preferences")} is denied by
     *             a SecurityManager.
     * @since Android 1.0
     */
    public static Preferences systemRoot() {
        checkSecurity();
        return factory.systemRoot();
    }
    
    //check the RuntimePermission("preferences")
    private static void checkSecurity() {
        SecurityManager manager = System.getSecurityManager();
        if(null != manager){
            manager.checkPermission(PREFS_PERM);
        }
        
    }

    /**
     * Returns the user preference node for the package of the given class.
     * The absolute path of the returned node is one slash followed by the given
     * class's full package name, replacing each period character ('.') with
     * a slash. For example, the absolute path of the preference associated with 
     * the class Object would be "/java/lang". As a special case, the unnamed
     * package is associated with a preference node "/&lt;unnamed&gt;". This
     * method will create the node and its ancestors as needed. Any nodes created
     * by this method won't necessarily be persisted until the method {@code flush()} is
     * invoked.
     * 
     * @param c
     *            the given class.
     * @return the user preference node for the package of the given class.
     * @throws NullPointerException
     *             if the given class is {@code null}.
     * @throws SecurityException
     *             if the {@code RuntimePermission("preferences")} is denied by
     *             a SecurityManager.
     * @since Android 1.0
     */
    public static Preferences userNodeForPackage (Class<?> c) {
        checkSecurity();
        return factory.userRoot().node(getNodeName(c));
    }
    
    //parse node's absolute path from class instance
    private static String getNodeName(Class<?> c){
        // ??? PREFS TODO change back to harmony code once getPackage
        // delivers the correct results
        // Package p = c.getPackage();
        // if(null == p){
        //     return "/<unnamed>"; //$NON-NLS-1$
        // }
        // return "/"+p.getName().replace('.', '/'); //$NON-NLS-1$
        int dotIndex = c.getName().lastIndexOf(".");
        return "/" + c.getName().substring(0, dotIndex).replace(".", "/");
    }

    /**
     * Returns the root node of the user preference hierarchy.
     * 
     * @return the user preference hierarchy root node.
     * @throws SecurityException
     *             if the {@code RuntimePermission("preferences")} is denied by
     *             a SecurityManager.
     * @since Android 1.0
     */
    public static Preferences userRoot() {
        checkSecurity();
        return factory.userRoot();
    }

    /**
     * Returns a string representation of this node. The format is "User/System
     * Preference Node: " followed by this node's absolute path.
     * 
     * @return the string representation of this node.
     * @since Android 1.0
     */
    @Override
    public abstract String toString();
}
