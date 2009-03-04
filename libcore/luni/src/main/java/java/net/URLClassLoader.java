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

package java.net;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.harmony.luni.util.InvalidJarIndexException;
import org.apache.harmony.luni.util.Msg;

/**
 * This class loader is responsible for loading classes and resources from a
 * list of URLs which can refer to either directories or JAR files. Classes
 * loaded by this {@code URLClassLoader} are granted permission to access the
 * URLs contained in the URL search list.
 * 
 * @since Android 1.0
 */
public class URLClassLoader extends SecureClassLoader {

    private static URL[] NO_PATH = new URL[0];

    @SuppressWarnings("unchecked")
    private static <K, V> Hashtable<K, V>[] newHashtableArray(int size) {
        return new Hashtable[size];
    }

    URL[] urls, orgUrls;

    Set<URL> invalidUrls = Collections.synchronizedSet(new HashSet<URL>());

    private Map<URL, JarFile> resCache = 
            Collections.synchronizedMap(new IdentityHashMap<URL, JarFile>(32));

    private Object lock = new Object();

    private URLStreamHandlerFactory factory;

    HashMap<URL, URL[]> extensions;

    Hashtable<String, URL[]>[] indexes;

    private AccessControlContext currentContext;

    static class SubURLClassLoader extends URLClassLoader {
        // The subclass that overwrites the loadClass() method
        private boolean checkingPackageAccess = false;

        SubURLClassLoader(URL[] urls) {
            super(urls, ClassLoader.getSystemClassLoader());
        }

        SubURLClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        /**
         * Overrides the {@code loadClass()} of {@code ClassLoader}. It calls
         * the security manager's {@code checkPackageAccess()} before
         * attempting to load the class.
         * 
         * @return the Class object.
         * @param className
         *            String the name of the class to search for.
         * @param resolveClass
         *            boolean indicates if class should be resolved after
         *            loading.
         * @throws ClassNotFoundException
         *             If the class could not be found.
         */
        @Override
        protected synchronized Class<?> loadClass(String className,
                boolean resolveClass) throws ClassNotFoundException {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null && !checkingPackageAccess) {
                int index = className.lastIndexOf('.');
                if (index > 0) { // skip if class is from a default package
                    try {
                        checkingPackageAccess = true;
                        sm.checkPackageAccess(className.substring(0, index));
                    } finally {
                        checkingPackageAccess = false;
                    }
                }
            }
            return super.loadClass(className, resolveClass);
        }
    }

    /**
     * Constructs a new {@code URLClassLoader} instance. The newly created
     * instance will have the system ClassLoader as its parent. URLs that end
     * with "/" are assumed to be directories, otherwise they are assumed to be
     * JAR files.
     * 
     * @param urls
     *            the list of URLs where a specific class or file could be
     *            found.
     * @throws SecurityException
     *             if a security manager exists and its {@code
     *             checkCreateClassLoader()} method doesn't allow creation of
     *             new ClassLoaders.
     * @since Android 1.0
     */
    public URLClassLoader(URL[] urls) {
        this(urls, ClassLoader.getSystemClassLoader(), null);
    }

    /**
     * Constructs a new URLClassLoader instance. The newly created instance will
     * have the system ClassLoader as its parent. URLs that end with "/" are
     * assumed to be directories, otherwise they are assumed to be JAR files.
     * 
     * @param urls
     *            the list of URLs where a specific class or file could be
     *            found.
     * @param parent
     *            the class loader to assign as this loader's parent.
     * @throws SecurityException
     *             if a security manager exists and its {@code
     *             checkCreateClassLoader()} method doesn't allow creation of
     *             new class loaders.
     * @since Android 1.0
     */
    public URLClassLoader(URL[] urls, ClassLoader parent) {
        this(urls, parent, null);
    }

    /**
     * Adds the specified URL to the search list.
     * 
     * @param url
     *            the URL which is to add.
     * @since Android 1.0
     */
    protected void addURL(URL url) {
        try {
            URL search = createSearchURL(url);
            urls = addURL(urls, search);
            orgUrls = addURL(orgUrls, url);
            synchronized (extensions) {
                extensions.put(search, null);
            }
        } catch (MalformedURLException e) {
        }
    }

    /**
     * Returns an array with the given URL added to the given array.
     * 
     * @param urlArray
     *            {@code java.net.URL[]} the source array
     * @param url
     *            {@code java.net.URL} the URL to be added
     * @return java.net.URL[] an array made of the given array and the new URL
     */
    URL[] addURL(URL[] urlArray, URL url) {
        URL[] newPath = new URL[urlArray.length + 1];
        System.arraycopy(urlArray, 0, newPath, 0, urlArray.length);
        newPath[urlArray.length] = url;
        Hashtable<String, URL[]>[] newIndexes = newHashtableArray(indexes.length + 1);
        System.arraycopy(indexes, 0, newIndexes, 0, indexes.length);
        indexes = newIndexes;
        return newPath;
    }

    /**
     * Returns all known URLs which point to the specified resource.
     * 
     * @param name
     *            the name of the requested resource.
     * @return the enumeration of URLs which point to the specified resource.
     * @throws IOException
     *             if an I/O error occurs while attempting to connect.
     * @since Android 1.0
     */
    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {
        if (name == null) {
            return null;
        }
        Vector<URL> result = AccessController.doPrivileged(
                new PrivilegedAction<Vector<URL>>() {
                    public Vector<URL> run() {
                        return findResources(urls, name, new Vector<URL>());
                    }
                }, currentContext);
        SecurityManager sm;
        int length = result.size();
        if (length > 0 && (sm = System.getSecurityManager()) != null) {
            Vector<URL> reduced = new Vector<URL>(length);
            for (int i = 0; i < length; i++) {
                URL url = result.elementAt(i);
                try {
                    sm.checkPermission(url.openConnection().getPermission());
                    reduced.addElement(url);
                } catch (IOException e) {
                } catch (SecurityException e) {
                }
            }
            result = reduced;
        }
        return result.elements();
    }

    /**
     * Returns a Vector of URLs among the given ones that contain the specified
     * resource.
     * 
     * @return Vector the enumeration of URLs that contain the specified
     *         resource.
     * @param searchURLs
     *            {@code java.net.URL[]} the array to be searched.
     * @param name
     *            {@code java.lang.String} the name of the requested resource.
     */
    Vector<URL> findResources(URL[] searchURLs, String name, Vector<URL> result) {
        boolean findInExtensions = searchURLs == urls;
        for (int i = 0; i < searchURLs.length; i++) {
            if (!invalidUrls.contains(searchURLs[i])) {
                URL[] search = new URL[] { searchURLs[i] };
                URL res = findResourceImpl(search, name);
                if (!invalidUrls.contains(search[0])) {
                    if (res != null && !result.contains(res)) {
                        result.addElement(res);
                    }
                    if (findInExtensions) {
                        findInExtensions(explore(searchURLs[i], i), name, i,
                                result, false);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns an {@code Object[]} containing a class, a URL, and a vector of
     * URLs, two of which are {@code null}, according to the caller, which is
     * identified by the {@code int} type.
     * 
     * @return Object[] a 3-element array : {Class, URL, Vector}. The non-null
     *         element contains the resource(s) found, which are searched in in
     *         {@code indexes[i]}.
     * @param i
     *            the index of 'indexes' array to use.
     * @param name
     *            the resource to look for : either a resource or a class.
     * @param resources
     *            {@code boolean} indicates that a vector of URL should be
     *            returned as the non {@code null} element in {@code Object[]}.
     * @param url
     *            if {@code true} a URL should be returned as the non-null
     *            element, if {@code false} a class should be returned.
     */
    Object findInIndex(int i, String name, Vector<URL> resources, boolean url) {
        Hashtable<String, URL[]> index = indexes[i];
        if (index != null) {
            int pos = name.lastIndexOf("/"); //$NON-NLS-1$
            // only keep the directory part of the resource
            // as index.list only keeps track of directories and root files
            String indexedName = (pos > 0) ? name.substring(0, pos) : name;
            URL[] jarURLs;
            if (resources != null) {
                jarURLs = index.get(indexedName);
                if (jarURLs != null) {
                    findResources(jarURLs, name, resources);
                }
            } else if (url) {
                jarURLs = index.get(indexedName);
                if (jarURLs != null) {
                    return findResourceImpl(jarURLs, name);
                }
            } else {
                String clsName = name;
                String partialName = clsName.replace('.', '/');
                int position;
                if ((position = partialName.lastIndexOf('/')) != -1) {
                    String packageName = partialName.substring(0, position);
                    jarURLs = index.get(packageName);
                } else {
                    String className = partialName.substring(0, partialName
                            .length())
                            + ".class"; //$NON-NLS-1$
                    jarURLs = index.get(className);
                }
                if (jarURLs != null) {
                    Class<?> c = findClassImpl(jarURLs, clsName);
                    // InvalidJarException is thrown when a mapping for a class
                    // is not valid, i.e. we can't find the class by following
                    // the mapping.
                    if (c == null) {
                        throw new InvalidJarIndexException();
                    }
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Returns an {@code Object[]} containing a Class, a URL, and a Vector of
     * URLs, two of which are {@code null}, according to the caller, which is
     * identified by the {@code int} type.
     * 
     * @return Object[] a 3-element array : {Class, URL, Vector}. The non-null
     *         element contains the resource(s) found, which are searched in
     *         newExtensions.
     * @param newExtensions
     *            URL[] the URLs to look in for.
     * @param name
     *            the resource to look for : either a resource or a class.
     * @param i
     *            the index of 'indexes' array to use.
     * @param resources
     *            indicates that a Vector of URL should be returned as the
     *            non-null element in {@code Object[]}.
     * @param url
     *            if {@code true} a URL should be returned as the non-null
     *            element, if {@code false} a class should be returned.
     */
    Object findInExtensions(URL[] newExtensions, String name, int i,
            Vector<URL> resources, boolean url) {
        if (newExtensions != null) {
            for (int k = 0; k < newExtensions.length; k++) {
                if (newExtensions[k] != null) {
                    URL[] search = new URL[] { newExtensions[k] };
                    if (resources != null) {
                        URL res = findResourceImpl(search, name);
                        if (!invalidUrls.contains(search[0])) { // the URL does
                            // not exist
                            if (res != null && !resources.contains(res)) {
                                resources.addElement(res);
                            }
                            findInExtensions(explore(newExtensions[k], i),
                                    name, i, resources, url);
                        }
                    } else {
                        Object result;
                        if (url) {
                            result = findResourceImpl(search, name);
                        } else {
                            result = findClassImpl(search, name);
                        }
                        if (result != null) {
                            return result;
                        }
                        if (!invalidUrls.contains(search[0])) { // the URL
                            // exists
                            result = findInExtensions(explore(newExtensions[k],
                                    i), name, i, null, url);
                            if (result != null) {
                                return result;
                            }
                        }
                    }
                }
            }
        } else {
            try {
                return findInIndex(i, name, resources, url);
            } catch (InvalidJarIndexException ex) {
                // Ignore misleading/wrong jar index
                return null;
            }
        }
        return null;
    }

    /**
     * Converts an input stream into a byte array.
     * 
     * @return byte[] the byte array
     * @param is
     *            the input stream
     */
    private static byte[] getBytes(InputStream is, boolean readAvailable)
            throws IOException {
        if (readAvailable) {
            byte[] buf = new byte[is.available()];
            is.read(buf, 0, buf.length);
            is.close();
            return buf;
        }
        byte[] buf = new byte[4096];
        int size = is.available();
        if (size < 1024) {
            size = 1024;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
        int count;
        while ((count = is.read(buf)) > 0) {
            bos.write(buf, 0, count);
        }
        return bos.toByteArray();
    }

    /**
     * Gets all permissions for the specified {@code codesource}. First, this
     * method retrieves the permissions from the system policy. If the protocol
     * is "file:/" then a new permission, {@code FilePermission}, granting the
     * read permission to the file is added to the permission collection.
     * Otherwise, connecting to and accepting connections from the URL is
     * granted.
     * 
     * @param codesource
     *            the code source object whose permissions have to be known.
     * @return the list of permissions according to the code source object.
     * @since Android 1.0
     */
    @Override
    protected PermissionCollection getPermissions(final CodeSource codesource) {
        PermissionCollection pc = super.getPermissions(codesource);
        URL u = codesource.getLocation();
        if (u.getProtocol().equals("jar")) { //$NON-NLS-1$
            try {
                // Create a URL for the resource the jar refers to
                u = ((JarURLConnection) u.openConnection()).getJarFileURL();
            } catch (IOException e) {
                // This should never occur. If it does continue using the jar
                // URL
            }
        }
        if (u.getProtocol().equals("file")) { //$NON-NLS-1$
            String path = u.getFile();
            String host = u.getHost();
            if (host != null && host.length() > 0) {
                path = "//" + host + path; //$NON-NLS-1$
            }

            if (File.separatorChar != '/') {
                path = path.replace('/', File.separatorChar);
            }
            if (isDirectory(u)) {
                pc.add(new FilePermission(path + "-", "read")); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                pc.add(new FilePermission(path, "read")); //$NON-NLS-1$
            }
        } else {
            String host = u.getHost();
            if (host.length() == 0) {
                host = "localhost"; //$NON-NLS-1$
            }
            pc.add(new SocketPermission(host, "connect, accept")); //$NON-NLS-1$
        }
        return pc;
    }

    /**
     * Returns the search list of this {@code URLClassLoader}.
     * 
     * @return the list of all known URLs of this instance.
     * @since Android 1.0
     */
    public URL[] getURLs() {
        return orgUrls.clone();
    }

    /**
     * Determines if the URL is pointing to a directory.
     */
    private static boolean isDirectory(URL url) {
        String file = url.getFile();
        return (file.length() > 0 && file.charAt(file.length() - 1) == '/');
    }

    /**
     * Returns a new {@code URLClassLoader} instance for the given URLs and the
     * system {@code ClassLoader} as its parent. The method {@code loadClass()}
     * of the new instance will call {@code
     * SecurityManager.checkPackageAccess()} before loading a class.
     * 
     * @param urls
     *            the list of URLs that is passed to the new {@code
     *            URLClassloader}.
     * @return the created {@code URLClassLoader} instance.
     * @since Android 1.0
     */
    public static URLClassLoader newInstance(final URL[] urls) {
        URLClassLoader sub = AccessController
                .doPrivileged(new PrivilegedAction<URLClassLoader>() {
                    public URLClassLoader run() {
                        return new SubURLClassLoader(urls);
                    }
                });
        sub.currentContext = AccessController.getContext();
        return sub;
    }

    /**
     * Returns a new {@code URLClassLoader} instance for the given URLs and the
     * specified {@code ClassLoader} as its parent. The method {@code
     * loadClass()} of the new instance will call the SecurityManager's {@code
     * checkPackageAccess()} before loading a class.
     * 
     * @param urls
     *            the list of URLs that is passed to the new URLClassloader.
     * @param parentCl
     *            the parent class loader that is passed to the new
     *            URLClassloader.
     * @return the created {@code URLClassLoader} instance.
     * @since Android 1.0
     */
    public static URLClassLoader newInstance(final URL[] urls,
            final ClassLoader parentCl) {
        URLClassLoader sub = AccessController
                .doPrivileged(new PrivilegedAction<URLClassLoader>() {
                    public URLClassLoader run() {
                        return new SubURLClassLoader(urls, parentCl);
                    }
                });
        sub.currentContext = AccessController.getContext();
        return sub;
    }

    /**
     * Constructs a new {@code URLClassLoader} instance. The newly created
     * instance will have the specified {@code ClassLoader} as its parent and
     * use the specified factory to create stream handlers. URLs that end with
     * "/" are assumed to be directories, otherwise they are assumed to be JAR
     * files.
     * 
     * @param searchUrls
     *            the list of URLs where a specific class or file could be
     *            found.
     * @param parent
     *            the {@code ClassLoader} to assign as this loader's parent.
     * @param factory
     *            the factory that will be used to create protocol-specific
     *            stream handlers.
     * @throws SecurityException
     *             if a security manager exists and its {@code
     *             checkCreateClassLoader()} method doesn't allow creation of
     *             new {@code ClassLoader}s.
     * @since Android 1.0
     */
    public URLClassLoader(URL[] searchUrls, ClassLoader parent,
            URLStreamHandlerFactory factory) {
        super(parent);
        // Required for pre-v1.2 security managers to work
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.factory = factory;
        // capture the context of the thread that creates this URLClassLoader
        currentContext = AccessController.getContext();
        int nbUrls = searchUrls.length;
        urls = new URL[nbUrls];
        orgUrls = new URL[nbUrls];
        // Search each jar for CLASS-PATH attribute in manifest
        extensions = new HashMap<URL, URL[]>(nbUrls * 2);
        for (int i = 0; i < nbUrls; i++) {
            try {
                urls[i] = createSearchURL(searchUrls[i]);
                extensions.put(urls[i], null);
            } catch (MalformedURLException e) {
            }
            orgUrls[i] = searchUrls[i];
        }
        // Search each jar for META-INF/INDEX.LIST
        indexes = newHashtableArray(nbUrls);
    }

    /**
     * Tries to locate and load the specified class using the known URLs. If the
     * class could be found, a class object representing the loaded class will
     * be returned.
     * 
     * @return the class that has been loaded.
     * @param clsName
     *            the name of the class which has to be found.
     * @throws ClassNotFoundException
     *             if the specified class cannot be loaded.
     * @since Android 1.0
     */
    @Override
    protected Class<?> findClass(final String clsName)
            throws ClassNotFoundException {
        Class<?> cls = AccessController.doPrivileged(
                new PrivilegedAction<Class<?>>() {
                    public Class<?> run() {
                        return findClassImpl(urls, clsName);
                    }
                }, currentContext);
        if (cls != null) {
            return cls;
        }
        throw new ClassNotFoundException(clsName);
    }

    /**
     * Returns an URL that will be checked if it contains the class or resource.
     * If the file component of the URL is not a directory, a Jar URL will be
     * created.
     * 
     * @return java.net.URL a test URL
     */
    private URL createSearchURL(URL url) throws MalformedURLException {
        if (url == null) {
            return url;
        }

        String protocol = url.getProtocol();

        if (isDirectory(url) || protocol.equals("jar")) { //$NON-NLS-1$
            return url;
        }
        if (factory == null) {
            return new URL("jar", "", //$NON-NLS-1$ //$NON-NLS-2$
                    -1, url.toString() + "!/"); //$NON-NLS-1$
        }
        return new URL("jar", "", //$NON-NLS-1$ //$NON-NLS-2$
                -1, url.toString() + "!/", //$NON-NLS-1$
                factory.createURLStreamHandler(protocol));
    }

    /**
     * Returns an URL referencing the specified resource or {@code null} if the
     * resource could not be found.
     * 
     * @param name
     *            the name of the requested resource.
     * @return the URL which points to the given resource.
     * @since Android 1.0
     */
    @Override
    public URL findResource(final String name) {
        if (name == null) {
            return null;
        }
        URL result = AccessController.doPrivileged(new PrivilegedAction<URL>() {
            public URL run() {
                return findResourceImpl(urls, name);
            }
        }, currentContext);
        SecurityManager sm;
        if (result != null && (sm = System.getSecurityManager()) != null) {
            try {
                sm.checkPermission(result.openConnection().getPermission());
            } catch (IOException e) {
                return null;
            } catch (SecurityException e) {
                return null;
            }
        }
        return result;
    }

    /**
     * Returns a URL among the given ones referencing the specified resource or
     * null if no resource could be found.
     * 
     * @return URL URL for the resource.
     * @param searchList
     *            java.net.URL[] the array to be searched
     * @param resName
     *            java.lang.String the name of the requested resource
     */
    URL findResourceImpl(URL[] searchList, String resName) {
        boolean findInExtensions = searchList == urls;
        int i = 0;
        while (i < searchList.length) {
            if (searchList[i] == null) {
                // KA024=One of urls is null
                throw new NullPointerException(Msg.getString("KA024")); //$NON-NLS-1$
            } else if (!invalidUrls.contains(searchList[i])) {
                JarFile jf = null;
                try {
                    URL currentUrl = searchList[i];
                    String protocol = currentUrl.getProtocol();
                    
                    if (protocol.equals("jar")) { //$NON-NLS-1$
                        jf = resCache.get(currentUrl);
                        if (jf == null) {
                            if (invalidUrls.contains(currentUrl)) {
                                continue;
                            }
                            // each jf should be found only once 
                            // so we do this job in the synchronized block
                            synchronized (lock) {
                                // Check the cache again in case another thread 
                                // updated it while we're waiting on lock
                                jf = resCache.get(currentUrl);
                                if (jf == null) {
                                    if (invalidUrls.contains(currentUrl)) {
                                        continue;
                                    }
                                    /*
                                     * If the connection for currentUrl or resURL is
                                     * used, getJarFile() will throw an exception if the
                                     * entry doesn't exist.
                                     */
                                    URL jarURL = ((JarURLConnection) currentUrl
                                              .openConnection()).getJarFileURL();
                                    try {
                                        JarURLConnection juc = (JarURLConnection) new URL(
                                                "jar", "", //$NON-NLS-1$ //$NON-NLS-2$
                                                jarURL.toExternalForm() + "!/").openConnection(); //$NON-NLS-1$
                                        jf = juc.getJarFile();
                                        resCache.put(currentUrl, jf);
                                    } catch (IOException e) {
                                        // Don't look for this jar file again
                                        invalidUrls.add(searchList[i]);
                                        throw e;
                                    }
                                }
                            }
                        }
                        String entryName;
                        if (currentUrl.getFile().endsWith("!/")) { //$NON-NLS-1$
                            entryName = resName;
                        } else {
                            String file = currentUrl.getFile();
                            int sepIdx = file.lastIndexOf("!/"); //$NON-NLS-1$
                            if (sepIdx == -1) {
                                // Invalid URL, don't look here again
                                invalidUrls.add(searchList[i]);
                                continue;
                            }
                            sepIdx += 2;
                            entryName = new StringBuffer(file.length() - sepIdx
                                    + resName.length()).append(
                                    file.substring(sepIdx)).append(resName)
                                    .toString();
                        }
                        if (jf.getEntry(entryName) != null) {
                            return targetURL(currentUrl, resName);
                        }
                    } else if (protocol.equals("file")) { //$NON-NLS-1$
                        String baseFile = currentUrl.getFile();
                        String host = currentUrl.getHost();
                        int hostLength = 0;
                        if (host != null) {
                            hostLength = host.length();
                        }
                        StringBuffer buf = new StringBuffer(2 + hostLength
                                + baseFile.length() + resName.length());
                        if (hostLength > 0) {
                            buf.append("//").append(host); //$NON-NLS-1$
                        }
                        // baseFile always ends with '/'
                        buf.append(baseFile);
                        String fixedResName = resName;
                        // Do not create a UNC path, i.e. \\host
                        while (fixedResName.startsWith("/") //$NON-NLS-1$
                                || fixedResName.startsWith("\\")) { //$NON-NLS-1$
                            fixedResName = fixedResName.substring(1);
                        }
                        buf.append(fixedResName);

                        String filename = buf.toString();
                        
                        try {
                            filename = URLDecoder.decode(filename, "UTF-8"); //$NON-NLS-1$
                        } catch (IllegalArgumentException e) {
                            return null;
                        }

                        if (new File(filename).exists()) {
                            return targetURL(currentUrl, fixedResName);
                        }
                    } else {
                        URL resURL = targetURL(currentUrl, resName);
                        URLConnection uc = resURL.openConnection();
                        try {
                            uc.getInputStream().close();
                        } catch (SecurityException e) {
                            return null;
                        }
                        // HTTP can return a stream on a non-existent file
                        // So check for the return code;
                        if (!resURL.getProtocol().equals("http")) { //$NON-NLS-1$
                            return resURL;
                        }
                        int code;
                        if ((code = ((HttpURLConnection) uc).getResponseCode()) >= 200
                                && code < 300) {
                            return resURL;
                        }
                    }
                } catch (MalformedURLException e) {
                    // Keep iterating through the URL list
                } catch (IOException e) {
                } catch (SecurityException e) {
                }
                if ((jf != null) && findInExtensions) {
                    if (indexes[i] != null) {
                        try {
                            URL result = (URL) findInIndex(i, resName, null,
                                    true);
                            if (result != null) {
                                return result;
                            }
                        } catch (InvalidJarIndexException ex) {
                            // Ignore invalid/misleading JAR index file
                        }
                    } else {
                        URL result = (URL) findInExtensions(explore(
                                searchList[i], i), resName, i, null, true);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
            ++i;
        }
        return null;
    }

    /**
     * Defines a new package using the information extracted from the specified
     * manifest.
     * 
     * @param packageName
     *            the name of the new package.
     * @param manifest
     *            the manifest containing additional information for the new
     *            package.
     * @param url
     *            the URL to the code source for the new package.
     * @return the created package.
     * @throws IllegalArgumentException
     *             if a package with the given name already exists.
     * @since Android 1.0
     */
    protected Package definePackage(String packageName, Manifest manifest,
            URL url) throws IllegalArgumentException {
        Attributes mainAttributes = manifest.getMainAttributes();
        String dirName = packageName.replace('.', '/') + "/"; //$NON-NLS-1$
        Attributes packageAttributes = manifest.getAttributes(dirName);
        boolean noEntry = false;
        if (packageAttributes == null) {
            noEntry = true;
            packageAttributes = mainAttributes;
        }
        String specificationTitle = packageAttributes
                .getValue(Attributes.Name.SPECIFICATION_TITLE);
        if (specificationTitle == null && !noEntry) {
            specificationTitle = mainAttributes
                    .getValue(Attributes.Name.SPECIFICATION_TITLE);
        }
        String specificationVersion = packageAttributes
                .getValue(Attributes.Name.SPECIFICATION_VERSION);
        if (specificationVersion == null && !noEntry) {
            specificationVersion = mainAttributes
                    .getValue(Attributes.Name.SPECIFICATION_VERSION);
        }
        String specificationVendor = packageAttributes
                .getValue(Attributes.Name.SPECIFICATION_VENDOR);
        if (specificationVendor == null && !noEntry) {
            specificationVendor = mainAttributes
                    .getValue(Attributes.Name.SPECIFICATION_VENDOR);
        }
        String implementationTitle = packageAttributes
                .getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        if (implementationTitle == null && !noEntry) {
            implementationTitle = mainAttributes
                    .getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        }
        String implementationVersion = packageAttributes
                .getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        if (implementationVersion == null && !noEntry) {
            implementationVersion = mainAttributes
                    .getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        }
        String implementationVendor = packageAttributes
                .getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
        if (implementationVendor == null && !noEntry) {
            implementationVendor = mainAttributes
                    .getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
        }

        return definePackage(packageName, specificationTitle,
                specificationVersion, specificationVendor, implementationTitle,
                implementationVersion, implementationVendor, isSealed(manifest,
                        dirName) ? url : null);
    }

    private boolean isSealed(Manifest manifest, String dirName) {
        Attributes mainAttributes = manifest.getMainAttributes();
        String value = mainAttributes.getValue(Attributes.Name.SEALED);
        boolean sealed = value != null && value.toLowerCase().equals("true"); //$NON-NLS-1$
        Attributes attributes = manifest.getAttributes(dirName);
        if (attributes != null) {
            value = attributes.getValue(Attributes.Name.SEALED);
            if (value != null) {
                sealed = value.toLowerCase().equals("true"); //$NON-NLS-1$
            }
        }
        return sealed;
    }

    /**
     * returns URLs referenced in the string classpath.
     * 
     * @param root
     *            the jar URL that classpath is related to
     * @param classpath
     *            the relative URLs separated by spaces
     * 
     * @return URL[] the URLs contained in the string classpath.
     */
    private URL[] getInternalURLs(URL root, String classpath) {
        // Class-path attribute is composed of space-separated values.
        StringTokenizer tokenizer = new java.util.StringTokenizer(classpath);
        Vector<URL> addedURLs = new Vector<URL>();
        String file = root.getFile();        
        int jarIndex = file.lastIndexOf("!/") - 1; //$NON-NLS-1$
        int index = file.lastIndexOf("/", jarIndex) + 1; //$NON-NLS-1$
        if (index == 0) {
            index = file.lastIndexOf(
                    System.getProperty("file.separator"), jarIndex) + 1; //$NON-NLS-1$
        }
        file = file.substring(0, index);
        String protocol = root.getProtocol();
        String host = root.getHost();
        int port = root.getPort();
        while (tokenizer.hasMoreElements()) {
            String element = tokenizer.nextToken();
            if (!element.equals("")) { //$NON-NLS-1$
                try {
                    URL newURL = new URL(protocol, host, port, file + element
                            + "!/"); //$NON-NLS-1$
                    synchronized (extensions) {
                        if (!extensions.containsKey(newURL)) {
                            extensions.put(newURL, null);
                            addedURLs.add(newURL);
                        }
                    }
                } catch (MalformedURLException e) {
                    // Nothing is added
                }
            }
        }
        URL[] newURLs = addedURLs.toArray(new URL[] {});
        return newURLs;
    }

    /**
     * @param in
     *            InputStream the stream to read lines from
     * @return List a list of String lines
     */
    private List<String> readLines(InputStream in) throws IOException {
        byte[] buff = new byte[144];
        List<String> lines = new ArrayList<String>();
        int pos = 0;
        int next;
        while ((next = in.read()) != -1) {
            if (next == '\n') {
                lines.add(new String(buff, 0, pos, "UTF8")); //$NON-NLS-1$
                pos = 0;
                continue;
            }
            if (next == '\r') {
                lines.add(new String(buff, 0, pos, "UTF8")); //$NON-NLS-1$
                pos = 0;
                if ((next = in.read()) == '\n') {
                    continue;
                }
            }
            if (pos == buff.length) {
                byte[] newBuf = new byte[buff.length * 2];
                System.arraycopy(buff, 0, newBuf, 0, buff.length);
                buff = newBuf;
            }
            buff[pos++] = (byte) next;
        }
        if (pos > 0) {
            lines.add(new String(buff, 0, pos, "UTF8")); //$NON-NLS-1$
        }
        return lines;
    }

    private URL targetURL(URL base, String name) throws MalformedURLException {
        try {
            String file = base.getFile() + URIEncoderDecoder.quoteIllegal(name,
                    "/@" + URI.someLegal);

            return new URL(base.getProtocol(), base.getHost(), base.getPort(),
                    file, null);
        } catch (UnsupportedEncodingException e) {
            MalformedURLException e2 = new MalformedURLException(e.toString());
            
            e2.initCause(e);
            throw e2;
        }
        
    }

    /**
     * @param searchURLs
     *            java.net.URL[] the URLs to search in
     * @param clsName
     *            java.lang.String the class name to be found
     * @return Class the class found or null if not found
     */
    Class<?> findClassImpl(URL[] searchURLs, String clsName) {
        boolean readAvailable = false;
        boolean findInExtensions = searchURLs == urls;
        final String name = new StringBuffer(clsName.replace('.', '/')).append(
                ".class").toString(); //$NON-NLS-1$
        for (int i = 0; i < searchURLs.length; i++) {
            if (searchURLs[i] == null) {
                // KA024=One of urls is null
                throw new NullPointerException(Msg.getString("KA024")); //$NON-NLS-1$
            } else if (!invalidUrls.contains(searchURLs[i])) {
                Manifest manifest = null;
                InputStream is = null;
                JarEntry entry = null;
                JarFile jf = null;
                byte[] clBuf = null;
                try {
                    URL thisURL = searchURLs[i];
                    String protocol = thisURL.getProtocol();
                    if (protocol.equals("jar")) { //$NON-NLS-1$
                        jf = resCache.get(thisURL);
                        if ((jf == null) && (!invalidUrls.contains(thisURL))) {
                            synchronized (lock) {
                                // Check the cache again in case another thread updated it 
                                // updated it while we're waiting on lock
                                jf = resCache.get(thisURL);
                                if (jf == null) {
                                    if (invalidUrls.contains(thisURL)) {
                                        continue;
                                    }
                                    // If the connection for testURL or thisURL is used,
                                    // getJarFile() will throw an exception if the entry
                                    // doesn't exist.
                                    URL jarURL = ((JarURLConnection) thisURL
                                              .openConnection()).getJarFileURL();
                                    try {
                                        JarURLConnection juc = (JarURLConnection) new URL(
                                                "jar", "", jarURL.toExternalForm() + "!/") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                .openConnection();
                                        jf = juc.getJarFile();
                                        resCache.put(thisURL, jf);
                                    } catch (IOException e) {
                                        // Don't look for this jar file again
                                        invalidUrls.add(searchURLs[i]);
                                        throw e;
                                    }
                                }
                            }
                        }
                        if (thisURL.getFile().endsWith("!/")) { //$NON-NLS-1$
                            entry = jf.getJarEntry(name);
                        } else {
                            String file = thisURL.getFile();
                            int sepIdx = file.lastIndexOf("!/"); //$NON-NLS-1$
                            if (sepIdx == -1) {
                                // Invalid URL, don't look here again
                                invalidUrls.add(searchURLs[i]);
                                continue;
                            }
                            sepIdx += 2;
                            String entryName = new StringBuffer(file.length()
                                    - sepIdx + name.length()).append(
                                    file.substring(sepIdx)).append(name)
                                    .toString();
                            entry = jf.getJarEntry(entryName);
                        }
                        if (entry != null) {
                            readAvailable = true;
                            is = jf.getInputStream(entry);
                            /**
                             * Avoid recursive load class, especially the class
                             * is an implementation class of security provider
                             * and the jar is signed.
                             */
                            Class loadedClass = findLoadedClass(clsName);
                            if (null != loadedClass) {
                                is.close();
                                return loadedClass;
                            }
                            manifest = jf.getManifest();
                        }
                    } else if (protocol.equals("file")) { //$NON-NLS-1$
                        String filename = thisURL.getFile();
                        String host = thisURL.getHost();
                        if (host != null && host.length() > 0) {
                            filename = new StringBuffer(host.length()
                                    + filename.length() + name.length() + 2)
                                    .append("//").append(host).append(filename) //$NON-NLS-1$
                                    .append(name).toString();
                        } else {
                            filename = new StringBuffer(filename.length()
                                    + name.length()).append(filename).append(
                                    name).toString();
                        }

                        // Just return null for caller to throw
                        // ClassNotFoundException.
                        try {
                            filename = URLDecoder.decode(filename, "UTF-8"); //$NON-NLS-1$
                        } catch (IllegalArgumentException e) {
                            return null;
                        }

                        File file = new File(filename);
                        // Don't throw exceptions for speed
                        if (file.exists()) {
                            is = new FileInputStream(file);
                            readAvailable = true;
                        } else {
                            continue;
                        }
                    } else {
                        is = targetURL(thisURL, name).openStream();
                    }
                } catch (MalformedURLException e) {
                    // Keep iterating through the URL list
                } catch (IOException e) {
                }
                if (is != null) {
                    URL codeSourceURL = null;
                    Certificate[] certificates = null;
                    CodeSource codeS = null;
                    try {
                        codeSourceURL = findInExtensions ? orgUrls[i]
                                : ((JarURLConnection) searchURLs[i]
                                        .openConnection()).getJarFileURL();
                    } catch (IOException e) {
                        codeSourceURL = searchURLs[i];
                    }
                    if (is != null) {
                        try {
                            clBuf = getBytes(is, readAvailable);
                            is.close();
                        } catch (IOException e) {
                            return null;
                        }
                    }
                    if (entry != null) {
                        certificates = entry.getCertificates();
                    }
                    // Use the original URL, not the possible jar URL
                    codeS = new CodeSource(codeSourceURL, certificates);
                    int dotIndex = clsName.lastIndexOf("."); //$NON-NLS-1$
                    if (dotIndex != -1) {
                        String packageName = clsName.substring(0, dotIndex);
                        synchronized (this) {
                            Package packageObj = getPackage(packageName);
                            if (packageObj == null) {
                                if (manifest != null) {
                                    definePackage(packageName, manifest,
                                            codeSourceURL);
                                } else {
                                    definePackage(packageName, null, null,
                                            null, null, null, null, null);
                                }
                            } else {
                                boolean exception = false;
                                if (manifest != null) {
                                    String dirName = packageName.replace('.',
                                            '/')
                                            + "/"; //$NON-NLS-1$
                                    if (isSealed(manifest, dirName)) {
                                        exception = !packageObj
                                                .isSealed(codeSourceURL);
                                    }
                                } else {
                                    exception = packageObj.isSealed();
                                }
                                if (exception) {
                                    throw new SecurityException(Msg
                                            .getString("K004c")); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                    return defineClass(clsName, clBuf, 0, clBuf.length, codeS);
                }
                if ((jf != null) && findInExtensions) {
                    if (indexes[i] != null) {
                        try {
                            Class<?> c = (Class<?>) findInIndex(i, clsName,
                                    null, false);
                            if (c != null) {
                                return c;
                            }
                        } catch (InvalidJarIndexException ex) {
                            // Ignore misleading/wrong jar index
                        }
                    } else {
                        Class<?> c = (Class<?>) findInExtensions(explore(
                                searchURLs[i], i), clsName, i, null, false);
                        if (c != null) {
                            return c;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param url
     *            URL the URL to explore
     * @param indexNumber
     *            int the index in extensions to consider
     * 
     * @return URL[] the URLs of bundled extensions that have been found (i.e.
     *         the URL of jar files in the class-path attribute), or null if
     *         none. if an INDEX.LIST has been found, an empty array is returned
     */
    URL[] explore(URL url, int indexNumber) {
        URL[] internal;
        synchronized (extensions) {
            internal = extensions.get(url);
        }
        if (internal != null) {
            return internal;
        }
        if (indexes[indexNumber] != null) {
            return null;
        }

        if (!url.getProtocol().equals("jar")) { //$NON-NLS-1$
            return null;
        }

        JarFile jf = resCache.get(url);
        // Add mappings from INDEX.LIST
        ZipEntry ze = jf.getEntry("META-INF/INDEX.LIST"); //$NON-NLS-1$
        if (ze != null) {
            if (url.equals(urls[indexNumber])) {
                try {
                    Hashtable<String, URL[]> index = new Hashtable<String, URL[]>(
                            15);
                    InputStream indexIS = jf.getInputStream(ze);
                    List<String> lines = readLines(indexIS);
                    indexIS.close();
                    ListIterator<String> iterator = lines.listIterator();
                    // Ignore the 2 first lines (index version)
                    iterator.next();
                    iterator.next();
                    // Add mappings from resource to jar file
                    URL fileURL = ((JarURLConnection) url.openConnection())
                            .getJarFileURL();
                    String file = fileURL.getFile();
                    String parentFile = new File(file).getParent();
                    parentFile = parentFile.replace(File.separatorChar, '/');
                    if (parentFile.charAt(0) != '/') {
                        parentFile = "/" + parentFile; //$NON-NLS-1$
                    }
                    URL parentURL = new URL(fileURL.getProtocol(), fileURL
                            .getHost(), fileURL.getPort(), parentFile);
                    while (iterator.hasNext()) {
                        URL jar = new URL("jar:" //$NON-NLS-1$
                                + parentURL.toExternalForm() + "/" //$NON-NLS-1$
                                + iterator.next() + "!/"); //$NON-NLS-1$
                        String resource = null;
                        while (iterator.hasNext()
                                && !(resource = iterator.next()).equals("")) { //$NON-NLS-1$
                            if (index.containsKey(resource)) {
                                URL[] jars = index.get(resource);
                                URL[] newJars = new URL[jars.length + 1];
                                System.arraycopy(jars, 0, newJars, 0,
                                        jars.length);
                                newJars[jars.length] = jar;
                                index.put(resource, newJars);
                            } else {
                                URL[] jars = { jar };
                                index.put(resource, jars);
                            }
                        }
                    }
                    indexes[indexNumber] = index;
                } catch (MalformedURLException e) {
                    // Ignore this jar's index
                } catch (IOException e) {
                    // Ignore this jar's index
                }
            }
            return null;
        }

        // Returns URLs referenced by the class-path attribute.
        Manifest manifest = null;
        try {
            manifest = jf.getManifest();
        } catch (IOException e) {
        }
        String classpath = null;
        if (manifest != null) {
            classpath = manifest.getMainAttributes().getValue(
                    Attributes.Name.CLASS_PATH);
        }
        synchronized (extensions) {
            internal = extensions.get(url);
            if (internal == null) {
                internal = classpath != null ? getInternalURLs(url, classpath)
                        : NO_PATH;
                extensions.put(url, internal);
            }
        }
        return internal;
    }
}
