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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;

import org.apache.harmony.prefs.internal.nls.Messages;

/**
 * <code>Preferences</code> instance represents one node in preferences tree,
 * which provide a mechanisms to store and access configuration data in a
 * hierarchical way. Two hierarchy tree is maintained, one for system
 * preferences shared by all users, and the other for user preferences which is
 * specific for each user. Preferences hierarchy tree and data is stored
 * precisely in implementation-dependent backend, and user doesn't need to care
 * about the details.
 * <p>
 * Every node has one name and one unique absolute path in a similar way with
 * directories in file system. The root node's name is "", and other nodes' name
 * string cannot contains slash and cannot be empty. The root node's absolute
 * path is "/", and other nodes' absolute path equals &lt;parent's absolute
 * path&gt; + "/" + &lt;node's name&gt;. All absolute paths start with slash.
 * Every node has one relative path to one of its ancestor. Relative path
 * doesn't start with slash, and equals to absolute path when following after
 * ancestor's absolute path and a slash.
 * </p>
 * <p>
 * The modification to preferences data may be asynchronous, which means they
 * may don't block and may returns immediately, implementation can feel free to
 * the modifications to the backend in any time until the flush() or sync()
 * method is invoked, these two methods force synchronized updates to backend.
 * Please note that if JVM exit normally, the implementation must assure all
 * modifications are persisted implicitly.
 * </p>
 * <p>
 * User invoking methods that retrieve preferences must provide default value,
 * default value is returned when preferences cannot be found or backend is
 * unavailable. Some other methods will throw <code>BackingStoreException</code>
 * when backend is unavailable.
 * </p>
 * <p>
 * Preferences can be export to/import from XML files, the XML document must
 * have the following DOCTYPE declaration:
 * </p>
 * <p>
 * <!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">
 * </p>
 * <p>
 * This system URI is not really accessed by network, it is only a
 * identification string. Visit the DTD location to see the actual format
 * permitted.
 * </p>
 * <p>
 * There has to be a concrete <code>PreferencesFactory</code> type for every
 * concrete <code>Preferences</code> type developed. Every J2SE implementation
 * must provide a default implementation for every supported platform, and the
 * default implementation can be replaced in some way. This implementation uses
 * system property java.util.prefs.PreferencesFactory to dictate the preferences
 * implementation.
 * </p>
 * <p>
 * Methods of this class is thread-safe. If multi JVMs using same backend
 * concurrently, the backend won't be corrupted, but no other guarantees is
 * made.
 * </p>
 * 
 * @see PreferencesFactory
 * 
 * @since 1.4
 */
public abstract class Preferences {
    
    /*
     * ---------------------------------------------------------
     * Class fields 
     * ---------------------------------------------------------
     */
    
    /**
     * Maximum size in characters of preferences key  
     */
    public static final int MAX_KEY_LENGTH = 80;
    
    /**
     * Maximum size in characters of preferences name
     */
    public static final int MAX_NAME_LENGTH = 80;
    
    /**
     * Maximum size in characters of preferences value
     */
    public static final int MAX_VALUE_LENGTH = 8192;
    
    // BEGIN android-added
    /**
     * the name of the configuration file where preferences factory class names 
     * can be specified.
     */
    private static final String FACTORY_CONFIGURATION_FILE_NAME = "META-INF/services/java.util.prefs.PreferencesFactory"; //$NON-NLS-1$
   
    /**
     * the encoding of configuration files
     */
    private static final String CONFIGURATION_FILE_ENCODING = "UTF-8"; //$NON-NLS-1$

    /**
     * the comment string used in configuration files
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
        //     try {
        //         ClassLoader loader = Thread.currentThread().getContextClassLoader();
        //         if(loader == null){
        //             loader = ClassLoader.getSystemClassLoader();
        //         }
        //         Class<?> factoryClass = loader.loadClass(factoryClassName);
        //         factory = (PreferencesFactory) factoryClass.newInstance();
        //     } catch (Exception e) {
        //         // prefs.10=Cannot initiate PreferencesFactory: {0}. Caused by {1}
        //         throw new InternalError(Messages.getString("prefs.10", factoryClassName, e));   //$NON-NLS-1$
        //     }
        // }
        // END android-removed
        // BEGIN android-added
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if(loader == null){
            loader = ClassLoader.getSystemClassLoader();
        }
        if(factoryClassName == null) {
            Enumeration<URL> en = null;
            try {
                en = loader.getResources(FACTORY_CONFIGURATION_FILE_NAME);
                BufferedReader reader = null;
                int commentIndex = 0;
                while(en.hasMoreElements()) {
                   try {
                       InputStream is = en.nextElement().openStream();
                       // Read each line for charset provider class names
                       // BEGIN android-modified
                       reader = new BufferedReader(new InputStreamReader(is, 
                                       CONFIGURATION_FILE_ENCODING), 8192);
                       // END android-modified
                       factoryClassName = reader.readLine();
                       commentIndex = factoryClassName
                           .indexOf(CONFIGURATION_FILE_COMMENT);
                       if(commentIndex > 0) {
                           factoryClassName = factoryClassName.substring(0,
                                   commentIndex).trim();
                       }
                       if(factoryClassName.length() > 0) {
                           break;
                       }
                   } catch (IOException ex) {
                       // ignore if a resource couldn't be read
                   }
                }
            } catch (Exception e) {
                // prefs.10=Cannot initiate PreferencesFactory: {0}. Caused by {1}
                throw new InternalError(Messages.getString("prefs.10", 
                        FACTORY_CONFIGURATION_FILE_NAME, e));   //$NON-NLS-1$
            }
        }

        if(factoryClassName == null) {
            factoryClassName = "java.util.prefs.FilePreferencesFactoryImpl";
        }
        
        try {
            Class<?> c = loader.loadClass(factoryClassName);
            factory = (PreferencesFactory) c.newInstance();
        } catch(Exception e) {
            // prefs.10=Cannot initiate PreferencesFactory: {0}. Caused by {1}
            throw new InternalError(Messages.getString("prefs.10", 
                    factoryClassName, e));   //$NON-NLS-1$  
        }
        // END android-added
    }
    
    /*
     * ---------------------------------------------------------
     * Constructors
     * ---------------------------------------------------------
     */
    
    /**
     *    Default constructor, for use by subclasses only.
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
     * Get this preference node's absolute path string.
     * 
     * @return this preference node's absolute path string.
     */
    public abstract String absolutePath();
    
    /**
     * Return names of all children of this node, or empty string if this node 
     * has no children. 
     * 
     * @return         names of all children of this node
     * @throws BackingStoreException
     *                 if backing store is unavailable or causes operation failure
     * @throws IllegalStateException
     *                 if this node has been removed
     */
    public abstract String[] childrenNames() throws BackingStoreException;
    
    /**
     * Remove all preferences of this node. 
     * 
     * @throws BackingStoreException
     *                 if backing store is unavailable or causes operation failure
     * @throws IllegalStateException
     *                 if this node has been removed
     */
    public abstract void clear() throws BackingStoreException;
    
    /**
     * Export all preferences of this node to the given output stream in XML 
     * document. 
     * <p>
     * This XML document has the following DOCTYPE declaration:
     * <pre>
     * &lt;!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd"&gt;</pre>
     * And the UTF-8 encoding will be used. Please note that this node is not 
     * thread-safe, which is an exception of this class. 
     * </p>
     * @param  ostream
     *                 the output stream to export the XML
     * @throws IOException
     *                 if export operation caused an <code>IOException</code>
     * @throws BackingStoreException
     *                 if backing store is unavailable or causes operation failure
     * @throws IllegalStateException
     *                 if this node has been removed
     */
    public abstract void exportNode (OutputStream ostream) throws IOException, BackingStoreException;
    
    /**
     * Export all preferences of this node and its all descendants to the given 
     * output stream in XML document. 
     * <p>
     * This XML document has the following DOCTYPE declaration:
     * <pre>
     * &lt;!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd"&gt;</pre>     * 
     * And the UTF-8 encoding will be used. Please note that this node is not 
     * thread-safe, which is an exception of this class. 
     * </p>
     * @param  ostream
     *                 the output stream to export the XML
     * @throws IOException
     *                 if export operation caused an <code>IOException</code>
     * @throws BackingStoreException
     *                 if backing store is unavailable or causes operation failure
     * @throws IllegalStateException
     *                 if this node has been removed
     */
    public abstract void exportSubtree (OutputStream ostream) throws IOException, BackingStoreException;
    
    /**
     * Force the updates to this node and its descendants to the backing store. 
     * <p>
     * If this node has been removed, then the invocation of this method only 
     * flush this node without descendants.
     * </p> 
     * @throws BackingStoreException
     *                 if backing store is unavailable or causes operation failure
     */
    public abstract void flush() throws BackingStoreException;
    
    /**
     * Return the string value mapped to the given key, or default value if no 
     * value is mapped or backing store is unavailable.
     * <p>
     * Some implementations may store default values in backing stores. In this case, 
     * if there is no value mapped to the given key, the stored default value is 
     * returned.
     * </p>
     * 
     * @param key    the preference key
     * @param deflt    the default value, which will be returned if no value is 
     *                 mapped to the given key or backing store unavailable 
     * @return         the preference value mapped to the given key, or default value if 
     *                 no value is mapped or backing store unavailable 
     * @throws IllegalStateException
     *                 if this node has been removed
     * @throws NullPointerException
     *                 if parameter key is null 
     */
    public abstract String get (String key, String deflt);
    
    /**
     * Return the boolean value mapped to the given key, or default value if no 
     * value is mapped, backing store is unavailable, or the value is invalid.
     * <p>
     * The valid value is string equals "true", which represents true, or "false", 
     * which represents false, case is ignored. 
     * </p>  
     * <p>
     * Some implementations may store default values in backing stores. In this case, 
     * if there is no value mapped to the given key, the stored default value is 
     * returned.
     * </p>
     * 
     * @param key    the preference key
     * @param deflt    the default value, which will be returned if no value is 
     *                 mapped to the given key, backing store unavailable or value 
     *                 is invalid 
     * @return         the boolean value mapped to the given key, or default value if 
     *                 no value is mapped, backing store unavailable or value is invalid
     * @throws IllegalStateException
     *                 if this node has been removed
     * @throws NullPointerException
     *                 if parameter key is null 
     */
    public abstract boolean getBoolean (String key, boolean deflt);
    
    /**
     * Return the byte array value mapped to the given key, or default value if no 
     * value is mapped, backing store is unavailable, or the value is invalid string.
     * <p>
     * The valid value string is Base64 encoded binary data. The Base64 encoding 
     * is as defined in <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>, 
     * section 6.8.
     * </p>  
     * <p>
     * Some implementations may store default values in backing stores. In this case, 
     * if there is no value mapped to the given key, the stored default value is 
     * returned.
     * </p>
     * 
     * @param key    the preference key
     * @param deflt    the default value, which will be returned if no value is 
     *                 mapped to the given key, backing store unavailable or value 
     *                 is invalid 
     * @return         the byte array value mapped to the given key, or default value if 
     *                 no value is mapped, backing store unavailable or value is invalid
     * @throws IllegalStateException
     *                 if this node has been removed
     * @throws NullPointerException
     *                 if parameter key is null 
     */
    public abstract byte[] getByteArray (String key, byte[] deflt);
    
    /**
     * Return the double value mapped to the given key, or default value if no 
     * value is mapped, backing store is unavailable, or the value is invalid string.
     * <p>
     * The valid value string can be converted to double number by 
     * {@link Double#parseDouble(String) Double.parseDouble(String)}.
     * </p>  
     * <p>
     * Some implementations may store default values in backing stores. In this case, 
     * if there is no value mapped to the given key, the stored default value is 
     * returned.
     * </p>
     * 
     * @param key    the preference key
     * @param deflt    the default value, which will be returned if no value is 
     *                 mapped to the given key, backing store unavailable or value 
     *                 is invalid 
     * @return         the double value mapped to the given key, or default value if 
     *                 no value is mapped, backing store unavailable or value is invalid
     * @throws IllegalStateException
     *                 if this node has been removed
     * @throws NullPointerException
     *                 if parameter key is null 
     */
    public abstract double getDouble (String key, double deflt);
    
    /**
     * Return the float value mapped to the given key, or default value if no 
     * value is mapped, backing store is unavailable, or the value is invalid string.
     * <p>
     * The valid value string can be converted to float number by 
     * {@link Float#parseFloat(String) Float.parseFloat(String)}.
     * </p>  
     * <p>
     * Some implementations may store default values in backing stores. In this case, 
     * if there is no value mapped to the given key, the stored default value is 
     * returned.
     * </p>
     * 
     * @param key    the preference key
     * @param deflt    the default value, which will be returned if no value is 
     *                 mapped to the given key, backing store unavailable or value 
     *                 is invalid 
     * @return         the float value mapped to the given key, or default value if 
     *                 no value is mapped, backing store unavailable or value is invalid
     * @throws IllegalStateException
     *                 if this node has been removed
     * @throws NullPointerException
     *                 if parameter key is null 
     */
    public abstract float getFloat (String key, float deflt);
    
    /**
     * Return the float value mapped to the given key, or default value if no 
     * value is mapped, backing store is unavailable, or the value is invalid string.
     * <p>
     * The valid value string can be converted to integer by 
     * {@link Integer#parseInt(String) Integer.parseInt(String)}.
     * </p>  
     * <p>
     * Some implementations may store default values in backing stores. In this case, 
     * if there is no value mapped to the given key, the stored default value is 
     * returned.
     * </p>
     * 
     * @param key    the preference key
     * @param deflt    the default value, which will be returned if no value is 
     *                 mapped to the given key, backing store unavailable or value 
     *                 is invalid 
     * @return         the integer value mapped to the given key, or default value if 
     *                 no value is mapped, backing store unavailable or value is invalid
     * @throws IllegalStateException
     *                 if this node has been removed
     * @throws NullPointerException
     *                 if parameter key is null 
     */
    public abstract int getInt (String key, int deflt);
    
    /**
     * Return the long value mapped to the given key, or default value if no 
     * value is mapped, backing store is unavailable, or the value is invalid string.
     * <p>
     * The valid value string can be converted to long integer by 
     * {@link Long#parseLong(String) Long.parseLong(String)}.
     * </p>  
     * <p>
     * Some implementations may store default values in backing stores. In this case, 
     * if there is no value mapped to the given key, the stored default value is 
     * returned.
     * </p>
     * 
     * @param key    the preference key
     * @param deflt    the default value, which will be returned if no value is 
     *                 mapped to the given key, backing store unavailable or value 
     *                 is invalid 
     * @return         the long value mapped to the given key, or default value if 
     *                 no value is mapped, backing store unavailable or value is invalid
     * @throws IllegalStateException
     *                 if this node has been removed
     * @throws NullPointerException
     *                 if parameter key is null 
     */
    public abstract long getLong (String key, long deflt);
    
    /**
     * Import all preferences from the given input stream in XML document. 
     * <p>
     * This XML document has the following DOCTYPE declaration:
     * <pre>
     * &lt;!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd"&gt;</pre>     * 
     * Please note that this node is not thread-safe, which is an exception of 
     * this class. 
     * </p>
     * 
     * @param istream
     *                 the given input stream to read data
     * @throws InvalidPreferencesFormatException
     *                 if the data read from given input stream is not valid XML 
     *                 document
     * @throws IOException
     *                 if import operation caused an <code>IOException</code>
     * @throws SecurityException
     *                 if <code>RuntimePermission("preferences")</code> is denied 
     *                 by a <code>SecurityManager</code>
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
     * Return true if this is a user preferences, false if this is a system 
     * preferences
     * 
     * @return         true if this is a user preferences, false if this is a 
     *                 system preferences
     */
    public abstract boolean isUserNode();
    
    /**
     * Return all preferences keys stored in this node, or empty array if no 
     * key is found.
     * 
     * @return         all preferences keys in this node
     * @throws BackingStoreException
     *                 if backing store is unavailable or causes operation failure
     * @throws IllegalStateException
     *                 if this node has been removed
     */
    public abstract String[] keys() throws BackingStoreException;
    
    /**
     * Return name of this node.
     * 
     * @return         the name of this node
     */
    public abstract String name();
    
    /**
     * Return the preferences node with the given path name. The path name can 
     * be relative or absolute. The dictated preferences and its ancestors will 
     * be created if they do not exist.
     * <p>
     * The path is treated as relative to this node if it doesn't start with 
     * slash, or as absolute otherwise.</p>  
     *  
     * @param path    the path name of dictated preferences
     * @return         the dictated preferences node
     * @throws IllegalStateException
     *                 if this node has been removed.
     * @throws IllegalArgumentException
     *                 if the path name is invalid.
     * @throws NullPointerException
     *                 if given path is null.
     */
    public abstract Preferences node (String path);
    
    /**
     * Return the preferences node with the given path name. The path is treated 
     * as relative to this node if it doesn't start with slash, or as absolute 
     * otherwise.
     * <p>
     * Please note that if this node has been removed, invocation of this node 
     * will throw <code>IllegalStateException</code> except the given path is 
     * empty string, which will return false.
     * </p>
     * 
     * @param path    the path name of dictated preferences
     * @return         true if the dictated preferences node exists
     * @throws IllegalStateException
     *                 if this node has been removed and the path is not empty string.
     * @throws IllegalArgumentException
     *                 if the path name is invalid.
     * @throws NullPointerException
     *                 if given path is null.
     * @throws BackingStoreException
     *                 if backing store is unavailable or causes operation failure
     */
    public abstract boolean nodeExists (String path) throws BackingStoreException;
    
    /**
     * Return the parent preferences node of this node, or null if this node is root.
     * 
     * @return the parent preferences node of this node.
     * @throws IllegalStateException
     *             if this node has been removed    
     */
    public abstract Preferences parent();
    
    /**
     * Add new preferences to this node using given key and value, or update 
     * value if preferences with given key has already existed.
     * 
     * @param key    the preferences key to be added or be updated 
     * @param value    the preferences value for the given key
     * @throws NullPointerException
     *                 if the given key or value is null
     * @throws IllegalArgumentException
     *                 if the given key's length is bigger than 
     *                 <code>MAX_KEY_LENGTH</code>, or the value's length is bigger 
     *                 than <code>MAX_VALUE_LENGTH</code>
     * @throws IllegalStateException
     *             if this node has been removed    
     */
    public abstract void put (String key, String value);
    
    /**
     * Add new preferences to this node using given key and string form of given 
     * value, or update value if preferences with given key has already existed. 
     * 
     * @param key    the preferences key to be added or be updated 
     * @param value    the preferences value for the given key
     * @throws NullPointerException
     *                 if the given key is null
     * @throws IllegalArgumentException
     *                 if the given key's length is bigger than 
     *                 <code>MAX_KEY_LENGTH</code>
     * @throws IllegalStateException
     *             if this node has been removed    
     */
    public abstract void putBoolean (String key, boolean value);
    
    /**
     * Add new preferences to this node using given key and string form of given 
     * value, or update value if preferences with given key has already existed. 
     * <p>
     * The string form of value is the Base64 encoded binary data of the given 
     * byte array. The Base64 encoding is as defined in 
     * <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>, section 6.8.</p>
     * 
     * @param key    the preferences key to be added or be updated 
     * @param value    the preferences value for the given key
     * @throws NullPointerException
     *                 if the given key or value is null
     * @throws IllegalArgumentException
     *                 if the given key's length is bigger than 
     *                 <code>MAX_KEY_LENGTH</code> or value's length is bigger than  
     *                 three quarters of <code>MAX_KEY_LENGTH</code>
     * @throws IllegalStateException
     *             if this node has been removed    
     */
    public abstract void putByteArray (String key, byte[] value);
    
    /**
     * Add new preferences to this node using given key and string form of given 
     * value, or update value if preferences with given key has already existed. 
     * <p>
     * The string form of given value is the result of invoking 
     * {@link Double#toString(double) Double.toString(double)}</p>
     * 
     * @param key    the preferences key to be added or be updated 
     * @param value    the preferences value for the given key
     * @throws NullPointerException
     *                 if the given key is null
     * @throws IllegalArgumentException
     *                 if the given key's length is bigger than 
     *                 <code>MAX_KEY_LENGTH</code>
     * @throws IllegalStateException
     *             if this node has been removed    
     */
    public abstract void putDouble (String key, double value);
    
    /**
     * Add new preferences to this node using given key and string form of given 
     * value, or update value if preferences with given key has already existed. 
     * <p>
     * The string form of given value is the result of invoking 
     * {@link Float#toString(float) Float.toString(float)}</p>
     * 
     * @param key    the preferences key to be added or be updated 
     * @param value    the preferences value for the given key
     * @throws NullPointerException
     *                 if the given key is null
     * @throws IllegalArgumentException
     *                 if the given key's length is bigger than 
     *                 <code>MAX_KEY_LENGTH</code>
     * @throws IllegalStateException
     *             if this node has been removed    
     */
    public abstract void putFloat (String key, float value);
    
    /**
     * Add new preferences to this node using given key and string form of given 
     * value, or update value if preferences with given key has already existed. 
     * <p>
     * The string form of given value is the result of invoking 
     * {@link Integer#toString(int) Integer.toString(int)}</p>
     * 
     * @param key    the preferences key to be added or be updated 
     * @param value    the preferences value for the given key
     * @throws NullPointerException
     *                 if the given key is null
     * @throws IllegalArgumentException
     *                 if the given key's length is bigger than 
     *                 <code>MAX_KEY_LENGTH</code>
     * @throws IllegalStateException
     *             if this node has been removed    
     */
    public abstract void putInt (String key, int value);
    
    /**
     * Add new preferences to this node using given key and string form of given 
     * value, or update value if preferences with given key has already existed. 
     * <p>
     * The string form of given value is the result of invoking 
     * {@link Long#toString(long) Long.toString(long)}</p>
     * 
     * @param key    the preferences key to be added or be updated 
     * @param value    the preferences value for the given key
     * @throws NullPointerException
     *                 if the given key is null
     * @throws IllegalArgumentException
     *                 if the given key's length is bigger than 
     *                 <code>MAX_KEY_LENGTH</code>
     * @throws IllegalStateException
     *             if this node has been removed    
     */
    public abstract void putLong (String key, long value);

    /**
     * Remove the preferences mapped to the given key from this node.
     * 
     * @param key    the given preferences key to removed 
     * @throws NullPointerException
     *                 if the given key is null
     * @throws IllegalStateException
     *             if this node has been removed    
     */
    public abstract void remove (String key);
    
    /**
     * Remove this preferences node and its all descendants. The removal maybe
     * won't be persisted until the <code>flush()</code> method is invoked. 
     * 
     * @throws BackingStoreException
     *                 if backing store is unavailable or causes operation failure 
     * @throws IllegalStateException
     *                 if this node has been removed
     * @throws UnsupportedOperationException
     *                 if this is a root node
     */
    public abstract void removeNode() throws BackingStoreException;
    
    /**
     * Register an <code>NodeChangeListener</code> instance for this node, which 
     * will receive <code>NodeChangeEvent</code>. <code>NodeChangeEvent</code> will 
     * be produced when direct child node is added to or removed from this node. 
     * 
     * @param ncl    the given listener to be registered
     * @throws NullPointerException
     *                 if the given listener is null
     * @throws IllegalStateException
     *                 if this node has been removed
     */
    public abstract void addNodeChangeListener (NodeChangeListener ncl);
    
    /**
     * Register an <code>PreferenceChangeListener</code> instance for this node, which 
     * will receive <code>PreferenceChangeEvent</code>. <code>PreferenceChangeEvent</code> will 
     * be produced when preference is added to, removed from or updated for this node. 
     * 
     * @param pcl    the given listener to be registered
     * @throws NullPointerException
     *                 if the given listener is null
     * @throws IllegalStateException
     *                 if this node has been removed
     */
    public abstract void addPreferenceChangeListener (PreferenceChangeListener pcl);
    
    /**
     * Remove the given <code>NodeChangeListener</code> instance from this node. 
     * 
     * @param ncl    the given listener to be removed
     * @throws IllegalArgumentException
     *                 if the given listener 
     * @throws IllegalStateException
     *                 if this node has been removed
     */
    public abstract void removeNodeChangeListener (NodeChangeListener ncl);
    
    /**
     * Remove the given <code>PreferenceChangeListener</code> instance from this node. 
     * 
     * @param pcl    the given listener to be removed
     * @throws IllegalArgumentException
     *                 if the given listener 
     * @throws IllegalStateException
     *                 if this node has been removed
     */
    public abstract void removePreferenceChangeListener (PreferenceChangeListener pcl);
    
    /**
     * Synchronize this preferences node and its descendants' data with the back 
     * end preferences store. The changes of back end should be reflect by this 
     * node and its descendants, meanwhile, the changes of this node and descendants 
     * should be persisted.
     * 
     * @throws BackingStoreException
     *                 if backing store is unavailable or causes operation failure
     * @throws IllegalStateException
     *                 if this node has been removed
     */
    public abstract void sync() throws BackingStoreException;
    
    /**
     * Return the system preference node for the package of given class. The 
     * absolute path of the returned node is one slash followed by the given 
     * class's full package name with replacing each period ('.') with slash.
     * For example, the preference's associated with class <code>Object<code> 
     * has absolute path like "/java/lang". As a special case, the unnamed 
     * package is associated with preference node "/<unnamed>". 
     *  
     * This method will create node and its ancestors if needed, and the new 
     * created nodes maybe won't be persisted until the <code>flush()</code> 
     * is invoked.
     * 
     * @param c        the given class 
     * @return         the system preference node for the package of given class. 
     * @throws NullPointerException
     *                 if the given class is null
     * @throws SecurityException
     *                 if <code>RuntimePermission("preferences")</code> is denied 
     *                 by a <code>SecurityManager</code>
     */
    public static Preferences systemNodeForPackage (Class<?> c) {
        checkSecurity();
        return factory.systemRoot().node(getNodeName(c));
    }
    
    /**
     * Return the root node for system preference hierarchy.
     * 
     * @return         the root node for system preference hierarchy
     * @throws SecurityException
     *                 if <code>RuntimePermission("preferences")</code> is denied 
     *                 by a <code>SecurityManager</code>
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
     * Return the user preference node for the package of given class. The 
     * absolute path of the returned node is one slash followed by the given 
     * class's full package name with replacing each period ('.') with slash.
     * For example, the preference's associated with class <code>Object<code> 
     * has absolute path like "/java/lang". As a special case, the unnamed 
     * package is associated with preference node "/<unnamed>". 
     *  
     * This method will create node and its ancestors if needed, and the new 
     * created nodes maybe won't be persisted until the <code>flush()</code> 
     * is invoked.
     * 
     * @param c    the given class 
     * @return         the user preference node for the package of given class. 
     * @throws NullPointerException
     *                      if the given class is null
     * @throws SecurityException
     *                 if <code>RuntimePermission("preferences")</code> is denied 
     *                 by a <code>SecurityManager</code>
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
     * Return the root node for user preference hierarchy.
     * 
     * @return         the root node for user preference hierarchy
     * @throws SecurityException
     *                 if <code>RuntimePermission("preferences")</code> is denied 
     *                 by a <code>SecurityManager</code>
     */
    public static Preferences userRoot() {
        checkSecurity();
        return factory.userRoot();
    }
    
    /**
     * Return a string description of this node. The format is "User/System 
     * Preference Node: " followed by this node's absolute path.
     * 
     * @return a string description of this node
     * 
     */
    @Override
    public abstract String toString();
}
