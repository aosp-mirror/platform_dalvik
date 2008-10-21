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

package java.lang;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.nio.channels.Channel;
import java.nio.channels.spi.SelectorProvider;

import java.security.SecurityPermission;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyPermission;
import java.util.Set;

// BEGIN android-added
import dalvik.system.VMStack;
// END android-added

/**
 * Class System provides a standard place for programs to find system related
 * information. All System API is static.
 * 
 */
public final class System {

    /**
     * Default input stream
     */
    public static final InputStream in;

    /**
     * Default output stream
     */
    public static final PrintStream out;

    /**
     * Default error output stream
     */
    public static final PrintStream err;

    /**
     * The System Properties table
     */
    private static Properties systemProperties;

    /**
     * The System default SecurityManager
     */
    private static SecurityManager securityManager;

    /**
     * Initialize all the slots in System on first use.
     */
    static {
        /*
         * Set up standard in, out, and err.
         * 
         * TODO err and out are String.ConsolePrintStream. All three are
         * buffered in Harmony. Check and possibly change this later.
         */
        err = new PrintStream(new FileOutputStream(FileDescriptor.err));
        out = new PrintStream(new FileOutputStream(FileDescriptor.out));
        in = new FileInputStream(FileDescriptor.in);
    }

    /**
     * Sets the value of the static slot "in" in the receiver to the passed in
     * argument.
     * 
     * @param newIn the new value for in.
     */
    @SuppressWarnings("unused")
    public static void setIn(InputStream newIn) {
        SecurityManager secMgr = System.getSecurityManager();
        setFieldImpl("in", "Ljava/io/InputStream;", newIn);
    }

    /**
     * Sets the value of the static slot "out" in the receiver to the passed in
     * argument.
     * 
     * @param newOut the new value for out.
     */
    @SuppressWarnings("unused")
    public static void setOut(java.io.PrintStream newOut) {
        SecurityManager secMgr = System.getSecurityManager();
        setFieldImpl("out", "Ljava/io/PrintStream;", newOut);
    }

    /**
     * Sets the value of the static slot "err" in the receiver to the passed in
     * argument.
     * 
     * @param newErr the new value for err.
     */
    @SuppressWarnings("unused")
    public static void setErr(java.io.PrintStream newErr) {
        SecurityManager secMgr = System.getSecurityManager();
        setFieldImpl("err", "Ljava/io/PrintStream;", newErr);
    }

    /**
     * Prevents this class from being instantiated.
     */
    private System() {
    }

    /**
     * Copies the contents of <code>array1</code> starting at offset
     * <code>start1</code> into <code>array2</code> starting at offset
     * <code>start2</code> for <code>length</code> elements.
     * 
     * @param array1 the array to copy out of
     * @param start1 the starting index in array1
     * @param array2 the array to copy into
     * @param start2 the starting index in array2
     * @param length the number of elements in the array to copy
     */
    public static native void arraycopy(Object array1, int start1, Object array2, int start2, int length);

    /**
     * Returns the current time expressed as milliseconds since the time
     * 00:00:00 UTC on January 1, 1970.
     * 
     * @return the time in milliseconds.
     */
    public static native long currentTimeMillis();

    /**
     * <p>
     * Returns the most precise time measurement in nanoseconds that's
     * available.
     * </p>
     * 
     * @return The current time in nanoseconds.
     */
    public static native long nanoTime();

    /**
     * Causes the virtual machine to stop running, and the program to exit. If
     * runFinalizersOnExit(true) has been invoked, then all finalizers will be
     * run first.
     * 
     * @param code the return code.
     * 
     * @throws SecurityException if the running thread is not allowed to cause
     *         the vm to exit.
     * 
     * @see SecurityManager#checkExit
     */
    public static void exit(int code) {
        Runtime.getRuntime().exit(code);
    }

    /**
     * Indicate to the virtual machine that it would be a good time to collect
     * available memory. Note that, this is a hint only.
     */
    public static void gc() {
        Runtime.getRuntime().gc();
    }

    /**
     * Returns an environment variable.
     * 
     * @param var the name of the environment variable
     * @return the value of the specified environment variable
     */
    public static String getenv(String var) {
        if (var == null) {
            throw new NullPointerException();
        }
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new RuntimePermission("getenv." + var));
        }

        return getEnvByName(var);
    }

    /*
     * Returns an environment variable. No security checks are
     * performed.
     * 
     * @param var the name of the environment variable
     * @return the value of the specified environment variable
     */
    private static native String getEnvByName(String var);
    
    /**
     * <p>
     * Returns all environment variables.
     * </p>
     * 
     * @return A Map of all environment variables.
     */
    public static Map<String, String> getenv() {
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new RuntimePermission("getenv.*"));
        }

        Map<String, String> map = new HashMap<String, String>();
        
        int index = 0;
        String entry = getEnvByIndex(index++);
        while(entry != null) {
            int pos = entry.indexOf('=');
            if (pos != -1) {
                map.put(entry.substring(0, pos), entry.substring(pos + 1));
            }
            
            entry = getEnvByIndex(index++);
        }

        return new SystemEnvironment(map);
    }

    /*
     * Returns an environment variable. No security checks are
     * performed. The safe way of traversing the environment is
     * to start at index zero and count upwards until a null
     * pointer is encountered. This marks the end of the Unix
     * environment.
     * 
     * @param index the index of the environment variable
     * @return the value of the specified environment variable
     */
    private static native String getEnvByIndex(int index);
    
    /**
     * <p>
     * Returns the inherited channel from the system-wide provider.
     * </p>
     * 
     * @return A {@link Channel} or <code>null</code>.
     * @throws IOException
     * @see SelectorProvider
     * @see SelectorProvider#inheritedChannel()
     */
    public static Channel inheritedChannel() throws IOException {
        return SelectorProvider.provider().inheritedChannel();
    }

    /**
     * Returns the system properties. Note that this is not a copy, so that
     * changes made to the returned Properties object will be reflected in
     * subsequent calls to getProperty and getProperties.
     * <p>
     * Security managers should restrict access to this API if possible.
     * 
     * @return the system properties
     */
    public static Properties getProperties() {
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPropertiesAccess();
        }
        
        return internalGetProperties();
    }

    /**
     * Returns the system properties without any security checks. This is used
     * for access from within java.lang.
     * 
     * @return the system properties
     */
    static Properties internalGetProperties() {
        if (System.systemProperties == null) {
            SystemProperties props = new SystemProperties();
            props.preInit();
            props.postInit();
            System.systemProperties = props;
        }
        
        return systemProperties;
    }

    /**
     * Returns the value of a particular system property. Returns null if no
     * such property exists,
     * <p>
     * The properties currently provided by the virtual machine are:
     * 
     * <pre>
     *        java.vendor.url
     *        java.class.path
     *        user.home
     *        java.class.version
     *        os.version
     *        java.vendor
     *        user.dir
     *        user.timezone
     *        path.separator
     *        os.name
     *        os.arch
     *        line.separator
     *        file.separator
     *        user.name
     *        java.version
     *        java.home
     * </pre>
     * 
     * @param prop the system property to look up
     * @return the value of the specified system property, or null if the
     *         property doesn't exist
     */
    public static String getProperty(String prop) {
        return getProperty(prop, null);
    }

    /**
     * Returns the value of a particular system property. If no such property is
     * found, returns the defaultValue.
     * 
     * @param prop the system property to look up
     * @param defaultValue return value if system property is not found
     * @return the value of the specified system property, or defaultValue if
     *         the property doesn't exist
     */
    public static String getProperty(String prop, String defaultValue) {
        if (prop.length() == 0) {
            throw new IllegalArgumentException();
        }
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPropertyAccess(prop);
        }
        
        return internalGetProperties().getProperty(prop, defaultValue);
    }

    /**
     * Sets the value of a particular system property.
     * 
     * @param prop the system property to change
     * @param value the value to associate with prop
     * @return the old value of the property, or null
     */
    public static String setProperty(String prop, String value) {
        if (prop.length() == 0) {
            throw new IllegalArgumentException();
        }
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new PropertyPermission(prop, "write"));
        }
        return (String) internalGetProperties().setProperty(prop, value);
    }

    /**
     * <p>
     * Removes the system property for the specified key.
     * </p>
     * 
     * <p>
     * Please see the Java SE API documentation for further
     * information on this method.
     * <p>
     * 
     * @param key the system property to be removed.
     * @return previous value or null if no value existed
     * 
     * @throws NullPointerException if the <code>key</code> argument is
     *         <code>null</code>.
     * @throws IllegalArgumentException if the <code>key</code> argument is
     *         empty.
     * @throws SecurityException if a security manager exists and write access
     *         to the specified property is not allowed.
     * @since 1.5
     */
    public static String clearProperty(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException();
        }

        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new PropertyPermission(key, "write"));
        }
        return (String) internalGetProperties().remove(key);
    }

    /**
     * Returns the active security manager.
     * 
     * @return the system security manager object.
     */
    public static SecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Returns an integer hash code for the parameter. The hash code returned is
     * the same one that would be returned by java.lang.Object.hashCode(),
     * whether or not the object's class has overridden hashCode(). The hash
     * code for null is 0.
     * 
     * @param anObject the object
     * @return the hash code for the object
     * 
     * @see java.lang.Object#hashCode
     */
    public static native int identityHashCode(Object anObject);

    /**
     * Loads the specified file as a dynamic library.
     * 
     * @param pathName the path of the file to be loaded
     */
    public static void load(String pathName) {
        SecurityManager smngr = System.getSecurityManager();
        if (smngr != null) {
            smngr.checkLink(pathName);
        }
        // BEGIN android-changed
        Runtime.getRuntime().load(pathName, VMStack.getCallingClassLoader());
        // END android-changed
    }

    /**
     * Loads and links the library specified by the argument.
     * 
     * @param libName the name of the library to load
     * 
     * @throws UnsatisfiedLinkError if the library could not be loaded
     * @throws SecurityException if the library was not allowed to be loaded
     */
    public static void loadLibrary(String libName) {
        // BEGIN android-changed
        Runtime.getRuntime().loadLibrary(libName, VMStack.getCallingClassLoader());
        // END android-changed
    }

    /**
     * Provides a hint to the virtual machine that it would be useful to attempt
     * to perform any outstanding object finalizations.
     */
    public static void runFinalization() {
        Runtime.getRuntime().runFinalization();
    }

    /**
     * Ensure that, when the virtual machine is about to exit, all objects are
     * finalized. Note that all finalization which occurs when the system is
     * exiting is performed after all running threads have been terminated.
     * 
     * @param flag
     *            true means finalize all on exit.
     * 
     * @deprecated This method is unsafe.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public static void runFinalizersOnExit(boolean flag) {
        Runtime.runFinalizersOnExit(flag);
    }

    /**
     * Returns the system properties. Note that the object which is passed in
     * not copied, so that subsequent changes made to the object will be
     * reflected in calls to getProperty and getProperties.
     * <p>
     * Security managers should restrict access to this API if possible.
     * 
     * @param p
     *            the property to set
     */
    public static void setProperties(Properties p) {
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPropertiesAccess();
        }

        systemProperties = p;
    }

    /**
     * Sets the active security manager. Note that once the security manager has
     * been set, it can not be changed. Attempts to do so will cause a security
     * exception.
     * 
     * @param sm
     *            the new security manager
     * 
     * @throws SecurityException
     *             if the security manager has already been set and if its
     *             checkPermission method does not allow to redefine the
     *             security manager. 
     */
    public static void setSecurityManager(final SecurityManager sm) {
        if (securityManager != null) {
            securityManager
                  .checkPermission(new java.lang.RuntimePermission("setSecurityManager"));
        }

        if (sm != null) {
            // before the new manager assumed office, make a pass through 
            // the common operations and let it load needed classes (if any),
            // to avoid infinite recursion later on 
            try {
                sm.checkPermission(new SecurityPermission("getProperty.package.access")); 
            } catch (Exception ignore) {}
            try {
                sm.checkPackageAccess("java.lang"); 
            } catch (Exception ignore) {}
        }
        
        securityManager = sm;
    }

    /**
     * Returns the platform specific file name format for the shared library
     * named by the argument.
     * 
     * @param userLibName
     *            the name of the library to look up.
     * @return the platform specific filename for the library
     */
    public static native String mapLibraryName(String userLibName);

    /**
     * Sets the value of the named static field in the receiver to the passed in
     * argument.
     * 
     * @param fieldName
     *            the name of the field to set, one of in, out, or err
     * @param stream
     *            the new value of the field
     */
    private static native void setFieldImpl(String fieldName, String signature, Object stream);

}

/**
 * Internal class holding the System properties. Needed by the Dalvik VM for
 * the two native methods. Must not be a local class, since we don't have a
 * System instance. 
 */
class SystemProperties extends Properties {
    // Dummy, just to make the compiler happy.
    
    native void preInit();

    native void postInit();
}

/**
 * Internal class holding the System environment variables. The Java spec
 * mandates that this map be read-only, so we wrap our real map into this one
 * and make sure no one touches the contents. We also check for null parameters
 * and do some (seemingly unnecessary) type casts to fulfill the contract layed
 * out in the spec.
 */
class SystemEnvironment implements Map {

    private Map<String, String> map;
    
    public SystemEnvironment(Map<String, String> map) {
        this.map = map;
    }
    
    public void clear() {
        throw new UnsupportedOperationException("Can't modify environment");        
    }

    @SuppressWarnings("cast")
    public boolean containsKey(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }

        return map.containsKey((String)key);
    }

    @SuppressWarnings("cast")
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }

        return map.containsValue((String)value);
    }

    public Set entrySet() {
        return map.entrySet();
    }

    @SuppressWarnings("cast")
    public String get(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }

        return map.get((String)key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public String put(Object key, Object value) {
        throw new UnsupportedOperationException("Can't modify environment");        
    }

    public void putAll(Map map) {
        throw new UnsupportedOperationException("Can't modify environment");        
    }

    public String remove(Object key) {
        throw new UnsupportedOperationException("Can't modify environment");        
    }

    public int size() {
        return map.size();
    }

    public Collection values() {
        return map.values();
    }
    
}
