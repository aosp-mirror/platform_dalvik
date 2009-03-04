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

import dalvik.system.VMStack;

/**
 * Provides access to system-related information and resources including
 * standard input and output. Enables clients to dynamically load native
 * libraries. All methods of this class are accessed in a static way and the
 * class itself can not be instantiated.
 * 
 * @see Runtime
 * 
 * @since Android 1.0
 */
public final class System {

    /**
     * Default input stream.
     * 
     * @since Android 1.0
     */
    public static final InputStream in;

    /**
     * Default output stream.
     * 
     * @since Android 1.0
     */
    public static final PrintStream out;

    /**
     * Default error output stream.
     * 
     * @since Android 1.0
     */
    public static final PrintStream err;

    /**
     * The System Properties table.
     */
    private static Properties systemProperties;

    /**
     * The System default SecurityManager.
     */
    private static SecurityManager securityManager;

    /**
     * Initialize all the slots in System on first use.
     */
    static {
        /*
         * Set up standard in, out, and err. TODO err and out are
         * String.ConsolePrintStream. All three are buffered in Harmony. Check
         * and possibly change this later.
         */
        err = new PrintStream(new FileOutputStream(FileDescriptor.err));
        out = new PrintStream(new FileOutputStream(FileDescriptor.out));
        in = new FileInputStream(FileDescriptor.in);
    }

    /**
     * Sets the standard input stream to the given user defined input stream.
     * 
     * @param newIn
     *            the user defined input stream to set as the standard input
     *            stream.
     * @throws SecurityException
     *             if a {@link SecurityManager} is installed and its {@code
     *             checkPermission()} method does not allow the change of the
     *             stream.
     * @since Android 1.0
     */
    public static void setIn(InputStream newIn) {
        SecurityManager secMgr = System.getSecurityManager();
        if(secMgr != null) {
            secMgr.checkPermission(RuntimePermission.permissionToSetIO);
        }
        setFieldImpl("in", "Ljava/io/InputStream;", newIn);
    }

    /**
     * Sets the standard output stream to the given user defined output stream.
     * 
     * @param newOut
     *            the user defined output stream to set as the standard output
     *            stream.
     * @throws SecurityException
     *             if a {@link SecurityManager} is installed and its {@code
     *             checkPermission()} method does not allow the change of the
     *             stream.
     * @since Android 1.0
     */
    public static void setOut(java.io.PrintStream newOut) {
        SecurityManager secMgr = System.getSecurityManager();
        if(secMgr != null) {
            secMgr.checkPermission(RuntimePermission.permissionToSetIO);
        }
        setFieldImpl("out", "Ljava/io/PrintStream;", newOut);
    }

    /**
     * Sets the standard error output stream to the given user defined output
     * stream.
     * 
     * @param newErr
     *            the user defined output stream to set as the standard error
     *            output stream.
     * @throws SecurityException
     *             if a {@link SecurityManager} is installed and its {@code
     *             checkPermission()} method does not allow the change of the
     *             stream.
     * @since Android 1.0
     */
    public static void setErr(java.io.PrintStream newErr) {
        SecurityManager secMgr = System.getSecurityManager();
        if(secMgr != null) {
            secMgr.checkPermission(RuntimePermission.permissionToSetIO);
        }
        setFieldImpl("err", "Ljava/io/PrintStream;", newErr);
    }

    /**
     * Prevents this class from being instantiated.
     */
    private System() {
    }

    /**
     * Copies the number of {@code length} elements of the Array {@code src}
     * starting at the offset {@code srcPos} into the Array {@code dest} at
     * the position {@code destPos}.
     * 
     * @param src
     *            the source array to copy the content.
     * @param srcPos
     *            the starting index of the content in {@code src}.
     * @param dest
     *            the destination array to copy the data into.
     * @param destPos
     *            the starting index for the copied content in {@code dest}.
     * @param length
     *            the number of elements of the {@code array1} content they have
     *            to be copied.
     * @since Android 1.0
     */
    public static native void arraycopy(Object src, int srcPos, Object dest,
            int destPos, int length);

    /**
     * Returns the current system time in milliseconds since January 1, 1970
     * 00:00:00 UTC. This method shouldn't be used for measuring timeouts or
     * other elapsed time measurements, as changing the system time can affect
     * the results.
     * 
     * @return the local system time in milliseconds.
     * @since Android 1.0
     */
    public static native long currentTimeMillis();

    /**
     * Returns the current timestamp of the most precise timer available on the
     * local system. This timestamp can only be used to measure an elapsed
     * period by comparing it against another timestamp. It cannot be used as a
     * very exact system time expression.
     * 
     * @return the current timestamp in nanoseconds.
     * @since Android 1.0
     */
    public static native long nanoTime();

    /**
     * Causes the virtual machine to stop running and the program to exit. If
     * {@link #runFinalizersOnExit(boolean)} has been previously invoked with a
     * {@code true} argument, then all all objects will be properly
     * garbage-collected and finalized first.
     * 
     * @param code
     *            the return code.
     * @throws SecurityException
     *             if the running thread has not enough permission to exit the
     *             virtual machine.
     * @see SecurityManager#checkExit
     * @since Android 1.0
     */
    public static void exit(int code) {
        Runtime.getRuntime().exit(code);
    }

    /**
     * Indicates to the virtual machine that it would be a good time to run the
     * garbage collector. Note that this is a hint only. There is no guarantee
     * that the garbage collector will actually be run.
     * 
     * @since Android 1.0
     */
    public static void gc() {
        Runtime.getRuntime().gc();
    }

    /**
     * Returns the value of the environment variable with the given name {@code
     * var}.
     * 
     * @param name
     *            the name of the environment variable.
     * @return the value of the specified environment variable or {@code null}
     *         if no variable exists with the given name.
     * @throws SecurityException
     *             if a {@link SecurityManager} is installed and its {@code
     *             checkPermission()} method does not allow the querying of
     *             single environment variables.
     * 
     * @since Android 1.0
     */
    public static String getenv(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new RuntimePermission("getenv." + name));
        }

        return getEnvByName(name);
    }

    /*
     * Returns an environment variable. No security checks are performed.
     * @param var the name of the environment variable
     * @return the value of the specified environment variable
     */
    private static native String getEnvByName(String name);

    /**
     * Returns an unmodifiable map of all available environment variables.
     * 
     * @return the map representing all environment variables.
     * @throws SecurityException
     *             if a {@link SecurityManager} is installed and its {@code
     *             checkPermission()} method does not allow the querying of
     *             all environment variables.
     * @since Android 1.0
     */
    public static Map<String, String> getenv() {
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new RuntimePermission("getenv.*"));
        }

        Map<String, String> map = new HashMap<String, String>();

        int index = 0;
        String entry = getEnvByIndex(index++);
        while (entry != null) {
            int pos = entry.indexOf('=');
            if (pos != -1) {
                map.put(entry.substring(0, pos), entry.substring(pos + 1));
            }

            entry = getEnvByIndex(index++);
        }

        return new SystemEnvironment(map);
    }

    /*
     * Returns an environment variable. No security checks are performed. The
     * safe way of traversing the environment is to start at index zero and
     * count upwards until a null pointer is encountered. This marks the end of
     * the Unix environment.
     * @param index the index of the environment variable
     * @return the value of the specified environment variable
     */
    private static native String getEnvByIndex(int index);

    /**
     * Returns the inherited channel from the creator of the current virtual
     * machine.
     * 
     * @return the inherited {@link Channel} or {@code null} if none exists.
     * @throws IOException
     *             if an I/O error occurred.
     * @see SelectorProvider
     * @see SelectorProvider#inheritedChannel()
     * @since Android 1.0
     */
    public static Channel inheritedChannel() throws IOException {
        return SelectorProvider.provider().inheritedChannel();
    }

    /**
     * Returns the system properties. Note that this is not a copy, so that
     * changes made to the returned Properties object will be reflected in
     * subsequent calls to getProperty and getProperties.
     * 
     * @return the system properties.
     * @throws SecurityException
     *             if a {@link SecurityManager} is installed and its {@code
     *             checkPropertiesAccess()} method does not allow the operation.
     * @since Android 1.0
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
     * Returns the value of a particular system property or {@code null} if no
     * such property exists.
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
     * @param prop
     *            the name of the system property to look up.
     * @return the value of the specified system property or {@code null} if the
     *         property doesn't exist.
     * @throws SecurityException
     *             if a {@link SecurityManager} is installed and its {@code
     *             checkPropertyAccess()} method does not allow the operation.
     * @since Android 1.0
     */
    public static String getProperty(String prop) {
        return getProperty(prop, null);
    }

    /**
     * Returns the value of a particular system property. The {@code
     * defaultValue} will be returned if no such property has been found.
     * 
     * @param prop
     *            the name of the system property to look up.
     * @param defaultValue
     *            the return value if the system property with the given name
     *            does not exist.
     * @return the value of the specified system property or the {@code
     *         defaultValue} if the property does not exist.
     * @throws SecurityException
     *             if a {@link SecurityManager} is installed and its {@code
     *             checkPropertyAccess()} method does not allow the operation.
     * @since Android 1.0
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
     * @param prop
     *            the name of the system property to be changed.
     * @param value
     *            the value to associate with the given property {@code prop}.
     * @return the old value of the property or {@code null} if the property
     *         didn't exist.
     * @throws SecurityException
     *             if a security manager exists and write access to the
     *             specified property is not allowed.
     * @since Android 1.0
     */
    public static String setProperty(String prop, String value) {
        if (prop.length() == 0) {
            throw new IllegalArgumentException();
        }
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new PropertyPermission(prop, "write"));
        }
        return (String)internalGetProperties().setProperty(prop, value);
    }

    /**
     * Removes a specific system property.
     * 
     * @param key
     *            the name of the system property to be removed.
     * @return the property value or {@code null} if the property didn't exist.
     * @throws NullPointerException
     *             if the argument {@code key} is {@code null}.
     * @throws IllegalArgumentException
     *             if the argument {@code key} is empty.
     * @throws SecurityException
     *             if a security manager exists and write access to the
     *             specified property is not allowed.
     * @since Android 1.0
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
        return (String)internalGetProperties().remove(key);
    }

    /**
     * Returns the active security manager.
     * 
     * @return the system security manager object.
     * @since Android 1.0
     */
    public static SecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Returns an integer hash code for the parameter. The hash code returned is
     * the same one that would be returned by the method {@code
     * java.lang.Object.hashCode()}, whether or not the object's class has
     * overridden hashCode(). The hash code for {@code null} is {@code 0}.
     * 
     * @param anObject
     *            the object to calculate the hash code.
     * @return the hash code for the given object.
     * @see java.lang.Object#hashCode
     * @since Android 1.0
     */
    public static native int identityHashCode(Object anObject);

    /**
     * Loads the specified file as a dynamic library.
     * 
     * @param pathName
     *            the path of the file to be loaded.
     * @throws SecurityException
     *             if the library was not allowed to be loaded.
     * @since Android 1.0
     */
    public static void load(String pathName) {
        SecurityManager smngr = System.getSecurityManager();
        if (smngr != null) {
            smngr.checkLink(pathName);
        }
        Runtime.getRuntime().load(pathName, VMStack.getCallingClassLoader());
    }

    /**
     * Loads and links the shared library with the given name {@code libName}.
     * The file will be searched in the default directory for shared libraries
     * of the local system.
     * 
     * @param libName
     *            the name of the library to load.
     * @throws UnsatisfiedLinkError
     *             if the library could not be loaded.
     * @throws SecurityException
     *             if the library was not allowed to be loaded.
     * @since Android 1.0
     */
    public static void loadLibrary(String libName) {
        SecurityManager smngr = System.getSecurityManager();
        if (smngr != null) {
            smngr.checkLink(libName);
        }
        Runtime.getRuntime().loadLibrary(libName, VMStack.getCallingClassLoader());
    }

    /**
     * Provides a hint to the virtual machine that it would be useful to attempt
     * to perform any outstanding object finalizations.
     * 
     * @since Android 1.0
     */
    public static void runFinalization() {
        Runtime.getRuntime().runFinalization();
    }

    /**
     * Ensures that, when the virtual machine is about to exit, all objects are
     * finalized. Note that all finalization which occurs when the system is
     * exiting is performed after all running threads have been terminated.
     * 
     * @param flag
     *            the flag determines if finalization on exit is enabled.
     * @deprecated this method is unsafe.
     * @since Android 1.0
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public static void runFinalizersOnExit(boolean flag) {
        Runtime.runFinalizersOnExit(flag);
    }

    /**
     * Sets all system properties.
     * 
     * @param p
     *            the new system property.
     * @throws SecurityException
     *             if a {@link SecurityManager} is installed and its {@code
     *             checkPropertiesAccess()} method does not allow the operation.
     * @since Android 1.0
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
     * been set, it can not be changed. Attempts to do that will cause a
     * security exception.
     * 
     * @param sm
     *            the new security manager.
     * @throws SecurityException
     *             if the security manager has already been set and if its
     *             checkPermission method does not allow to redefine the
     *             security manager.
     * @since Android 1.0
     */
    public static void setSecurityManager(final SecurityManager sm) {
        if (securityManager != null) {
            securityManager.checkPermission(new java.lang.RuntimePermission("setSecurityManager"));
        }

        if (sm != null) {
            // before the new manager assumed office, make a pass through
            // the common operations and let it load needed classes (if any),
            // to avoid infinite recursion later on
            try {
                sm.checkPermission(new SecurityPermission("getProperty.package.access"));
            } catch (Exception ignore) {
            }
            try {
                sm.checkPackageAccess("java.lang");
            } catch (Exception ignore) {
            }
        }

        securityManager = sm;
    }

    /**
     * Returns the platform specific file name format for the shared library
     * named by the argument.
     * 
     * @param userLibName
     *            the name of the library to look up.
     * @return the platform specific filename for the library.
     * @since Android 1.0
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
 * Internal class holding the System properties. Needed by the Dalvik VM for the
 * two native methods. Must not be a local class, since we don't have a System
 * instance.
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
