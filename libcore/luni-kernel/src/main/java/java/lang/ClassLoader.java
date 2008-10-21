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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;

import dalvik.system.PathClassLoader;
import dalvik.system.VMStack;

/**
 * <p>
 * A ClassLoader is used for loading classes.
 * </p>
 * 
 * <h4>VM Implementors Note</h4>
 * <p>
 * This class must be implemented by the VM. The documented methods and natives
 * must be implemented to support other provided class implementations in this
 * package.
 * </p>
 * 
 * @since 1.0
 * @see Class
 */
public abstract class ClassLoader {

// BEGIN android-note
    /*
     * Because of a potential class initialization race between ClassLoader
     * and java.lang.System, reproducible when using JDWP with "suspend=y",
     * we defer creation of the system class loader until first use.  We
     * use a static inner class to get synchronization at init time without
     * having to sync on every access.
     */
// END android-note

    /**
     * The 'System' ClassLoader - the one that is responsible for loading
     * classes from the classpath. It is not equal to the  bootstrap class
     * loader - that one handles the built-in classes.
     *
     * @see #getSystemClassLoader()
     */
    static private class SystemClassLoader {
        public static ClassLoader loader= ClassLoader.createSystemClassLoader();
    };

    /**
     * The parent ClassLoader.
     */
    private ClassLoader parent;

    /**
     * The packages known to the class loader.
     */
    private Map<String, Package> packages = new HashMap<String, Package>();

    /**
     * Create the system class loader.
     *
     * Note this is NOT the bootstrap class loader (which is managed by
     * the VM).  We use a null value for the parent to indicate that the
     * bootstrap loader is our parent.
     */
    private static ClassLoader createSystemClassLoader() {
        String classPath = System.getProperty("java.class.path", ".");

//        String[] paths = classPath.split(":");
//        URL[] urls = new URL[paths.length];
//        for (int i = 0; i < paths.length; i++) {
//            try {
//                urls[i] = new URL("file://" + paths[i]);
//            }
//            catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//            
//        return new java.net.URLClassLoader(urls, null);  
        
        // TODO Make this a java.net.URLClassLoader once we have those?
        return new PathClassLoader(classPath, BootClassLoader.getInstance());
    }

    /**
     * Returns the system class loader. This is the parent for new ClassLoader
     * instances, and is typically the class loader used to start the
     * application. If a security manager is present, and the caller's class
     * loader is not null and the caller's class loader is not the same as or an
     * ancestor of the system class loader, then this method calls the security
     * manager's checkPermission method with a
     * RuntimePermission("getClassLoader") permission to ensure it's ok to
     * access the system class loader. If not, a SecurityException will be
     * thrown.
     * 
     * @return The system classLoader.
     * @throws SecurityException if a security manager exists and it does not
     *         allow access to the system class loader.
     */
    public static ClassLoader getSystemClassLoader() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            ClassLoader caller = VMStack.getCallingClassLoader();
            if (caller != null &&
                !caller.isAncestorOf(SystemClassLoader.loader)) {
                smgr.checkPermission(new RuntimePermission("getClassLoader"));
            }
        }

        return SystemClassLoader.loader;
    }

    /**
     * Returns an URL specifying a resource which can be found by looking up
     * resName using the system class loader's resource lookup algorithm.
     * 
     * @return A URL specifying a system resource or null.
     * @param resName The name of the resource to find.
     * @see Class#getResource
     */
    public static URL getSystemResource(String resName) {
        return SystemClassLoader.loader.getResource(resName);
    }

    /**
     * Returns an Enumeration of URLs containing all resources which can be
     * found by looking up resName using the system class loader's resource
     * lookup algorithm.
     * 
     * @return An Enumeration of URLs containing the system resources
     * @param resName String the name of the resource to find.
     */
    public static Enumeration<URL> getSystemResources(String resName) throws IOException {
        return SystemClassLoader.loader.getResources(resName);
    }

    /**
     * Returns a stream on a resource found by looking up resName using the
     * system class loader's resource lookup algorithm. Basically, the contents
     * of the java.class.path are searched in order, looking for a path which
     * matches the specified resource.
     * 
     * @return A stream on the resource or null.
     * @param resName The name of the resource to find.
     * @see Class#getResourceAsStream
     */
    public static InputStream getSystemResourceAsStream(String resName) {
        return SystemClassLoader.loader.getResourceAsStream(resName);
    }

    /**
     * Constructs a new instance of this class with the system class loader as
     * its parent.
     * 
     * @throws SecurityException if a security manager exists and it does not
     *         allow the creation of new ClassLoaders.
     */
    protected ClassLoader() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkCreateClassLoader();
        }
        
        parent = getSystemClassLoader();
    }

    /**
     * Constructs a new instance of this class with the given class loader as
     * its parent.
     * 
     * @param parentLoader The ClassLoader to use as the new class loaders
     *        parent.
     * @throws SecurityException if a security manager exists and it does not
     *         allow the creation of new ClassLoaders.
     * @throws NullPointerException if the parent is null.
     */
    protected ClassLoader(ClassLoader parentLoader) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkCreateClassLoader();
        }

        // TODO Shouldn't we check for null values here?
//        if (parent == null) {
//            throw new NullPointerException();
//        }
        
        parent = parentLoader;
    }

    /**
     * Constructs a new class from an array of bytes containing a class
     * definition in class file format.
     * 
     * @param classRep A memory image of a class file.
     * @param offset The offset into the classRep.
     * @param length The length of the class file.
     * @deprecated Use {@link #defineClass(String, byte[], int, int)}
     */
    @Deprecated
    protected final Class<?> defineClass(byte[] classRep, int offset, int length)
            throws ClassFormatError {
        
        return VMClassLoader.defineClass(this, classRep, offset, length, null);
    }

    /**
     * Constructs a new class from an array of bytes containing a class
     * definition in class file format.
     * 
     * @param className The name of the new class
     * @param classRep A memory image of a class file
     * @param offset The offset into the classRep
     * @param length The length of the class file
     */
    protected final Class<?> defineClass(String className, byte[] classRep, int offset,
            int length) throws ClassFormatError {

        // TODO Define a default ProtectionDomain on first use
        return defineClass(className, classRep, offset, length, null);
    }

    /**
     * Constructs a new class from an array of bytes containing a class
     * definition in class file format and assigns the new class to the
     * specified protection domain.
     * 
     * @param className The name of the new class.
     * @param classRep A memory image of a class file.
     * @param offset The offset into the classRep.
     * @param length The length of the class file.
     * @param protectionDomain The protection domain this class should belongs
     *        to.
     */
    protected final Class<?> defineClass(String className, byte[] classRep, int offset,
            int length, ProtectionDomain protectionDomain) throws java.lang.ClassFormatError {
        
        return VMClassLoader.defineClass(this, className, classRep, offset, length,
                protectionDomain);
    }

    /**
     * <p>
     * Defines a new class for the name, bytecodes in the byte buffer and the
     * protection domain.
     * </p>
     * 
     * @param name The name of the class to define.
     * @param b The byte buffer containing the bytecodes of the new class.
     * @param protectionDomain The protection domain this class belongs to.
     * @return The defined class.
     * @throws ClassFormatError if an invalid class file is defined.
     * @since 1.5
     */
    protected final Class<?> defineClass(String name, ByteBuffer b,
            ProtectionDomain protectionDomain) throws ClassFormatError {
        
        byte[] temp = new byte[b.remaining()];
        b.get(temp);
        return defineClass(name, temp, 0, temp.length, protectionDomain);
    }

    /**
     * Overridden by subclasses, by default throws ClassNotFoundException. This
     * method is called by loadClass() after the parent ClassLoader has failed
     * to find a loaded class of the same name.
     * 
     * @return The class or null.
     * @param className The name of the class to search for.
     * @throws ClassNotFoundException if the class cannot be found.
     */
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        throw new ClassNotFoundException(className);
    }

    /**
     * Attempts to find and return a class which has already been loaded by the
     * virtual machine. Note that the class may not have been linked and the
     * caller should call resolveClass() on the result if necessary.
     * 
     * @return The class or null.
     * @param className The name of the class to search for.
     */
    protected final Class<?> findLoadedClass(String className) {
        // BEGIN android-changed
        ClassLoader loader;
        if (this == BootClassLoader.getInstance())
            loader = null;
        else
            loader = this;
        return VMClassLoader.findLoadedClass(loader, className);
        // END android-changed
    }

    /**
     * Attempts to load a class using the system class loader. Note that the
     * class has already been been linked.
     * 
     * @return The class which was loaded.
     * @param className The name of the class to search for.
     * @throws ClassNotFoundException if the class cannot be found.
     */
    protected final Class<?> findSystemClass(String className) throws ClassNotFoundException {
        return Class.forName(className, false, getSystemClassLoader());
    }

    /**
     * Returns the specified ClassLoader's parent.
     * 
     * @return The class or null.
     * @throws SecurityException if a security manager exists and it does not
     *         allow the parent loader to be retrieved.
     */
    public final ClassLoader getParent() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkPermission(new RuntimePermission("getClassLoader"));
        }
        
        return parent;
    }

    /**
     * Returns an URL which can be used to access the resource described by
     * resName, using the class loader's resource lookup algorithm. The default
     * behavior is just to return null.
     * 
     * @return The location of the resource.
     * @param resName String the name of the resource to find.
     * @see Class#getResource
     */
    public URL getResource(String resName) {
        URL resource = null;

        if (parent != null) {
            resource = parent.getResource(resName);
        }

        if (resource == null) {
            resource = findResource(resName);
        }

        return resource;
    }

    /**
     * Returns an Enumeration of URL which can be used to access the resources
     * described by resName, using the class loader's resource lookup algorithm.
     * The default behavior is just to return an empty Enumeration.
     * 
     * @return The location of the resources.
     * @param resName String the name of the resource to find.
     */
    @SuppressWarnings("unchecked")
    public Enumeration<URL> getResources(String resName) throws IOException {
        Enumeration first = EmptyEnumeration.getInstance();

        if (parent != null) {
            first = parent.getResources(resName);
        }

        Enumeration second = findResources(resName);

        return new TwoEnumerationsInOne(first, second);
    }

    /**
     * Returns a stream on a resource found by looking up resName using the
     * class loader's resource lookup algorithm. The default behavior is just to
     * return null.
     * 
     * @return A stream on the resource or null.
     * @param resName String the name of the resource to find.
     * @see Class#getResourceAsStream
     */
    public InputStream getResourceAsStream(String resName) {
        try {
            URL url = getResource(resName);
            if (url != null) {
                return url.openStream();
            }
        } catch (IOException ex) {
            // Don't want to see the exception. 
        }
        
        return null;
    }

    /**
     * Invoked by the Virtual Machine when resolving class references.
     * Equivalent to loadClass(className, false);
     * 
     * @return The Class object.
     * @param className The name of the class to search for.
     * @throws ClassNotFoundException if the class could not be found.
     */
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, false);
    }

    // BEGIN android-changed
    // Made resolveClass a no-op and changed the documentation accordingly.
    /**
     * Loads the class with the specified name, optionally linking the class
     * after load. Steps are: 1) Call findLoadedClass(className) to determine if
     * class is loaded 2) Call loadClass(className, resolveClass) on the parent
     * loader. 3) Call findClass(className) to find the class
     * 
     * @return The Class object.
     * @param className The name of the class to search for.
     * @param resolve Indicates if class should be resolved after loading. 
     *     Note: On the android reference implementation this parameter 
     *     does not have any effect.
     * @throws ClassNotFoundException if the class could not be found.
     */
    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(className);

        if (clazz == null) {
            try {
                if (parent != null) {
                    clazz = parent.loadClass(className, false);
                }
            } catch (ClassNotFoundException e) {
                // Don't want to see this.
            }

            if (clazz == null) {
                clazz = findClass(className);
            }
        }

        return clazz;
    }

    /**
     * Forces a class to be linked (initialized). If the class has already been
     * linked this operation has no effect.
     * 
     * Note that for the android reference implementation this method does not 
     * have any effect.
     * 
     * @param clazz The Class to link.
     * @throws NullPointerException if clazz is null.
     * @see Class#getResource
     */
    protected final void resolveClass(Class<?> clazz) {
        // no-op, doesn't make sense on android.
    }
    // END android-changed
    
    /**
     * <p>
     * This method must be provided by the VM vendor, as it is used by other
     * provided class implementations in this package. A sample implementation
     * of this method is provided by the reference implementation. This method
     * is used by SecurityManager.classLoaderDepth(), currentClassLoader() and
     * currentLoadedClass(). Returns true if the receiver is a system class
     * loader.
     * </p>
     * <p>
     * Note that this method has package visibility only. It is defined here to
     * avoid the security manager check in getSystemClassLoader, which would be
     * required to implement this method anywhere else.
     * </p>
     * 
     * @return <code>true</code> if the receiver is a system class loader
     * @see Class#getClassLoaderImpl()
     */
    final boolean isSystemClassLoader() {
        return false;
    }

    /**
     * <p>
     * Returns true if the receiver is ancestor of another class loader. It also
     * returns true if the two class loader are equal.
     * </p>
     * <p>
     * Note that this method has package visibility only. It is defined here to
     * avoid the security manager check in getParent, which would be required to
     * implement this method anywhere else. The method is also required in other
     * places where class loaders are accesses.
     * </p>
     * 
     * @param child A child candidate
     * @return <code>true</code> if the receiver is ancestor of, or equal to,
     *         the parameter
     */
    final boolean isAncestorOf(ClassLoader child) {
        return (child == this || isAncestorOf(child.getParent()));
    }

    /**
     * Returns an URL which can be used to access the resource described by
     * resName, using the class loader's resource lookup algorithm. The default
     * behavior is just to return null. This should be implemented by a
     * ClassLoader.
     * 
     * @return The location of the resource.
     * @param resName The name of the resource to find.
     */
    protected URL findResource(String resName) {
        return null;
    }

    /**
     * Returns an Enumeration of URL which can be used to access the resources
     * described by resName, using the class loader's resource lookup algorithm.
     * The default behavior is just to return an empty Enumeration.
     * 
     * @param resName The name of the resource to find.
     * 
     * @return The locations of the resources.
     * 
     * @throws IOException when an error occurs
     */
    @SuppressWarnings({
            "unchecked", "unused"
    })
    protected Enumeration<URL> findResources(String resName) throws IOException {
        return EmptyEnumeration.getInstance();
    }

    /**
     * Returns the absolute path of the file containing the library associated
     * with the given name, or null. If null is answered, the system searches
     * the directories specified by the system property "java.library.path".
     * 
     * @return The library file name or null.
     * @param libName The name of the library to find.
     */
    protected String findLibrary(String libName) {
        return null;
    }

    /**
     * Attempt to locate the requested package. If no package information can be
     * located, null is returned.
     * 
     * @param name The name of the package to find
     * @return The package requested, or null
     */
    protected Package getPackage(String name) {
        synchronized (packages) {
            Package p = packages.get(name);
            return p;
        }
    }

    /**
     * Attempt to locate the requested package using the given class loader.
     * If no package information can be located, null is returned.
     * 
     * @param loader The class loader to use
     * @param name The name of the package to find
     * @return The package requested, or null
     */
    static Package getPackage(ClassLoader loader, String name) {
        return loader.getPackage(name);
    }

    /**
     * Return all the packages known to this class loader.
     * 
     * @return All the packages known to this classloader
     */
    protected Package[] getPackages() {
        synchronized (packages) {
            Collection<Package> col = packages.values();
            return (Package[]) col.toArray();
        }
    }

    /**
     * Define a new Package using the specified information.
     * 
     * @param name The name of the package
     * @param specTitle The title of the specification for the Package
     * @param specVersion The version of the specification for the Package
     * @param specVendor The vendor of the specification for the Package
     * @param implTitle The implementation title of the Package
     * @param implVersion The implementation version of the Package
     * @param implVendor The specification vendor of the Package
     * @param sealBase If sealBase is null, the package is left unsealed.
     *        Otherwise, the the package is sealed using this URL.
     * @return The Package created
     * @throws IllegalArgumentException if the Package already exists
     */
    protected Package definePackage(String name, String specTitle, String specVersion,
            String specVendor, String implTitle, String implVersion, String implVendor,
            URL sealBase) throws IllegalArgumentException {

        synchronized(packages) {
            if (packages.containsKey(name)) {
                throw new IllegalArgumentException("Package " + name + " already defined");
            }
        
            Package newPackage = new Package(name, specTitle, specVersion,
                    specVendor, implTitle, implVersion, implVendor, sealBase);

            packages.put(name, newPackage);
            
            return newPackage;
        }
    }

    /**
     * Gets the signers of a class.
     * 
     * @param c The Class object
     * @return signers The signers for the class
     */
    final Object[] getSigners(Class<?> c) {
        return null;
    }

    /**
     * Sets the signers of a class.
     * 
     * @param c The Class object
     * @param signers The signers for the class
     */
    protected final void setSigners(Class<?> c, Object[] signers) {
        return;
    }

    /**
     * <p>
     * This must be provided by the VM vendor. It is used by
     * SecurityManager.checkMemberAccess() with depth = 3. Note that
     * checkMemberAccess() assumes the following stack when called:<br>
     * </p>
     * 
     * <pre>
     *          &lt; user code &amp;gt; &lt;- want this class
     *          Class.getDeclared*();
     *          Class.checkMemberAccess();
     *          SecurityManager.checkMemberAccess(); &lt;- current frame
     * </pre>
     * 
     * <p>
     * Returns the ClassLoader of the method (including natives) at the
     * specified depth on the stack of the calling thread. Frames representing
     * the VM implementation of java.lang.reflect are not included in the list.
     * </p>
     * Notes:
     * <ul>
     * <li>This method operates on the defining classes of methods on stack.
     * NOT the classes of receivers.</li>
     * <li>The item at depth zero is the caller of this method</li>
     * </ul>
     * 
     * @param depth the stack depth of the requested ClassLoader
     * @return the ClassLoader at the specified depth
     */
    static final ClassLoader getStackClassLoader(int depth) {
        return null;
    }

    /**
     * This method must be included, as it is used by System.load(),
     * System.loadLibrary(). The reference implementation of this method uses
     * the getStackClassLoader() method. Returns the ClassLoader of the method
     * that called the caller. i.e. A.x() calls B.y() calls callerClassLoader(),
     * A's ClassLoader will be returned. Returns null for the bootstrap
     * ClassLoader.
     * 
     * @return a ClassLoader or null for the bootstrap ClassLoader
     */
    static ClassLoader callerClassLoader() {
        return null;
    }

    /**
     * This method must be provided by the VM vendor, as it is called by
     * java.lang.System.loadLibrary(). System.loadLibrary() cannot call
     * Runtime.loadLibrary() because this method loads the library using the
     * ClassLoader of the calling method. Loads and links the library specified
     * by the argument.
     * 
     * @param libName the name of the library to load
     * @param loader the classloader in which to load the library
     * @throws UnsatisfiedLinkError if the library could not be loaded
     * @throws SecurityException if the library was not allowed to be loaded
     */
    static void loadLibraryWithClassLoader(String libName, ClassLoader loader) {
        return;
    }

    /**
     * This method must be provided by the VM vendor, as it is called by
     * java.lang.System.load(). System.load() cannot call Runtime.load() because
     * the library is loaded using the ClassLoader of the calling method. Loads
     * and links the library specified by the argument. No security check is
     * done.
     * 
     * @param libName the name of the library to load
     * @param loader the classloader in which to load the library
     * @param libraryPath the library path to search, or null
     * @throws UnsatisfiedLinkError if the library could not be loaded
     */
    static void loadLibraryWithPath(String libName, ClassLoader loader, String libraryPath) {
        return;
    }

    /**
     * Sets the assertion status of a class.
     * 
     * @param cname Class name
     * @param enable Enable or disable assertion
     */
    public void setClassAssertionStatus(String cname, boolean enable) {
        return;
    }

    /**
     * Sets the assertion status of a package.
     * 
     * @param pname Package name
     * @param enable Enable or disable assertion
     */
    public void setPackageAssertionStatus(String pname, boolean enable) {
        return;
    }

    /**
     * Sets the default assertion status of a classloader
     * 
     * @param enable Enable or disable assertion
     */
    public void setDefaultAssertionStatus(boolean enable) {
        return;
    }

    /**
     * Clears the default, package and class assertion status of a classloader
     * 
     */
    public void clearAssertionStatus() {
        return;
    }

    /**
     * Returns the assertion status of the named class Returns the assertion
     * status of the class or nested class if it has been set. Otherwise returns
     * the assertion status of its package or superpackage if that has been set.
     * Otherwise returns the default assertion status. Returns 1 for enabled and
     * 0 for disabled.
     * 
     * @return the assertion status.
     * @param cname the name of class.
     */
    boolean getClassAssertionStatus(String cname) {
        return false;
    }

    /**
     * Returns the assertion status of the named package Returns the assertion
     * status of the named package or superpackage if that has been set.
     * Otherwise returns the default assertion status. Returns 1 for enabled and
     * 0 for disabled.
     * 
     * @return the assertion status.
     * @param pname the name of package.
     */
    boolean getPackageAssertionStatus(String pname) {
        return false;
    }

    /**
     * Returns the default assertion status
     * 
     * @return boolean the default assertion status.
     */
    boolean getDefaultAssertionStatus() {
        return false;
    }
}

/*
 * Provides a helper class that combines two existing URL enumerations into one.
 * It is required for the getResources() methods. Items are fetched from the
 * first enumeration until it's empty, then from the second one.
 */
class TwoEnumerationsInOne implements Enumeration<URL> {

    private Enumeration<URL> first;
    
    private Enumeration<URL> second;
    
    public TwoEnumerationsInOne(Enumeration<URL> first, Enumeration<URL> second) {
        this.first = first;
        this.second = second;
    }
    
    public boolean hasMoreElements() {
        return first.hasMoreElements() || second.hasMoreElements();
    }

    public URL nextElement() {
        if (first.hasMoreElements()) {
            return first.nextElement();
        } else  {
            return second.nextElement();
        }
    }
    
}

/**
 * Provides an explicit representation of the boot class loader. It sits at the
 * head of the class loader chain and delegates requests to the VM's internal
 * class loading mechanism.
 */
class BootClassLoader extends ClassLoader {

    static BootClassLoader instance;

    public static BootClassLoader getInstance() {
        if (instance == null) {
            instance = new BootClassLoader();
        }

        return instance;
    }

    public BootClassLoader() {
        super(null);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return VMClassLoader.loadClass(name, false);
    }

    @Override
    protected URL findResource(String name) {
        return VMClassLoader.getResource(name);
    }

    @SuppressWarnings("unused")
    @Override
    protected Enumeration<URL> findResources(String resName) throws IOException {
        Enumeration<URL> result = VMClassLoader.getResources(resName);

        // VMClassLoader doesn't keep the contract for getResources()
        if (result == null) {
            result = EmptyEnumeration.getInstance();
        }

        return result;
    }

    /**
     * Returns package information for the given package. Unfortunately, the
     * BootClassLoader doesn't really have this information, and as a
     * non-secure ClassLoader, it isn't even required to, according to the spec.
     * Yet, we want to provide it, in order to make all those hopeful callers of
     * <code>myClass.getPackage().getName()</code> happy. Thus we construct a
     * Package object the first time it is being requested and fill most of the
     * fields with dummy values. The Package object is then put into the
     * ClassLoader's Package cache, so we see the same one next time. We don't
     * create Package objects for null arguments or for the default package.
     * <p>
     * There a limited chance that we end up with multiple Package objects
     * representing the same package: It can happen when when a package is
     * scattered across different JAR files being loaded by different
     * ClassLoaders. Rather unlikely, and given that this whole thing is more or
     * less a workaround, probably not worth the effort.
     */
    @Override
    protected Package getPackage(String name) {
        if (name != null && !"".equals(name)) {
            synchronized(this) {
                Package pack = super.getPackage(name);
                
                if (pack == null) {
                    pack = definePackage(name, "Unknown", "0.0", "Unknown", "Unknown", "0.0", "Unknown", null);
                }
                
                return pack;
            }            
        }
        
        return null;
    }
    
}

/**
 * TODO Open issues
 * - Missing / empty methods
 * - Signer stuff
 * - Protection domains
 * - Assertions
 */
