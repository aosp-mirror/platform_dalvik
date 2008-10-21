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

package org.apache.harmony.luni.internal.net.www.protocol.jar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.apache.harmony.kernel.vm.VM;
import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.Util;

/**
 * This subclass extends <code>URLConnection</code>.
 * <p>
 * 
 * This class is responsible for connecting and retrieving resources from a Jar
 * file which can be anywhere that can be refered to by an URL.
 */
public class JarURLConnection extends java.net.JarURLConnection {

    static Hashtable<String, CacheEntry<? extends JarFile>> jarCache = new Hashtable<String, CacheEntry<?>>();

    InputStream jarInput;

    private JarFile jarFile;

    private JarEntry jarEntry;
    
    private boolean closed;

    ReferenceQueue<JarFile> cacheQueue = new ReferenceQueue<JarFile>();

    static TreeSet<LRUKey> lru = new TreeSet<LRUKey>(
            new LRUComparator<LRUKey>());

    static int Limit;

    static {
        Limit = AccessController.doPrivileged(new PrivilegedAction<Integer>() {
            public Integer run() {
                return Integer.getInteger("jar.cacheSize", 500); //$NON-NLS-1$
            }
        });
        VM.closeJars();
    }

    static final class CacheEntry<T extends JarFile> extends WeakReference<T> {
        Object key;

        CacheEntry(T jar, String key, ReferenceQueue<JarFile> queue) {
            super(jar, queue);
            this.key = key;
        }
    }

    static final class LRUKey {
        JarFile jar;

        long ts;

        LRUKey(JarFile file, long time) {
            jar = file;
            ts = time;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return (obj instanceof LRUKey) &&
                (jar == ((LRUKey) obj).jar);
        }

        @Override
        public int hashCode() {
            return jar.hashCode();
        }
    }

    static final class LRUComparator<T> implements Comparator<LRUKey> {

        LRUComparator() {
        }

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(LRUKey o1, LRUKey o2) {
            if ((o1).ts > (o2).ts) {
                return 1;
            }
            return (o1).ts == (o2).ts ? 0 : -1;
        }

        /**
         * @param o1
         *            an object to compare
         * @param o2
         *            an object to compare
         * @return <code>true</code> if the objects are equal,
         *         <code>false</code> otherwise.
         */
        public boolean equals(Object o1, Object o2) {
            return o1.equals(o2);
        }
    }

    /**
     * @param url
     *            the URL of the JAR
     * @throws java.net.MalformedURLException
     *             if the URL is malformed
     */
    public JarURLConnection(java.net.URL url) throws MalformedURLException {
        super(url);
    }

    /**
     * @see java.net.URLConnection#connect()
     */
    @Override
    public void connect() throws IOException {
        jarFileURLConnection = getJarFileURL().openConnection();
        findJarFile(); // ensure the file can be found
        findJarEntry(); // ensure the entry, if any, can be found
        connected = true;
    }

    /**
     * Returns the Jar file refered by this <code>URLConnection</code>
     * 
     * @return the JAR file referenced by this connection
     * 
     * @throws IOException
     *             thrown if an IO error occurs while connecting to the
     *             resource.
     */
    @Override
    public JarFile getJarFile() throws IOException {
        if (!connected) {
            connect();
        }
        return jarFile;
    }

    /**
     * Returns the Jar file refered by this <code>URLConnection</code>
     * 
     * @throws IOException
     *             if an IO error occurs while connecting to the resource.
     */
    private void findJarFile() throws IOException {
        URL jarFileURL = getJarFileURL();
        if (jarFileURL.getProtocol().equals("file")) { //$NON-NLS-1$
            String fileName = jarFileURL.getFile();
            if(!new File(Util.decode(fileName,false)).exists()){
                // KA026=JAR entry {0} not found in {1}
                throw new FileNotFoundException(Msg.getString("KA026", //$NON-NLS-1$
                        getEntryName(), fileName));
            }
            String host = jarFileURL.getHost();
            if (host != null && host.length() > 0) {
                fileName = "//" + host + fileName; //$NON-NLS-1$
            }
            jarFile = openJarFile(fileName, fileName, false);
            return;
        }

        final String externalForm = jarFileURLConnection.getURL()
                .toExternalForm();
        jarFile = AccessController
                .doPrivileged(new PrivilegedAction<JarFile>() {
                    public JarFile run() {
                        try {
                            return openJarFile(null, externalForm, false);
                        } catch (IOException e) {
                            return null;
                        }
                    }
                });
        if (jarFile != null) {
            return;
        }

        // Build a temp jar file
        final InputStream is = jarFileURLConnection.getInputStream();
        try {
            jarFile = AccessController
                    .doPrivileged(new PrivilegedAction<JarFile>() {
                        public JarFile run() {
                            try {
                                File tempJar = File.createTempFile("hyjar_", //$NON-NLS-1$
                                        ".tmp", null); //$NON-NLS-1$
                                FileOutputStream fos = new FileOutputStream(
                                        tempJar);
                                byte[] buf = new byte[4096];
                                int nbytes = 0;
                                while ((nbytes = is.read(buf)) > -1) {
                                    fos.write(buf, 0, nbytes);
                                }
                                fos.close();
                                String path = tempJar.getPath();
                                return openJarFile(path, externalForm, true);
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    });
        } finally {
            is.close();
        }
        if (jarFile == null) {
            throw new IOException();
        }
    }

    JarFile openJarFile(String fileString, String key, boolean temp)
            throws IOException {

        JarFile jar = null;
        if (useCaches) {
            CacheEntry<? extends JarFile> entry;
            while ((entry = (CacheEntry<? extends JarFile>) cacheQueue.poll()) != null) {
                jarCache.remove(entry.key);
            }
            entry = jarCache.get(key);
            if (entry != null) {
                jar = entry.get();
            }
            if (jar == null && fileString != null) {
                int flags = ZipFile.OPEN_READ
                        + (temp ? ZipFile.OPEN_DELETE : 0);
                jar = new JarFile(new File(Util.decode(fileString, false)),
                        true, flags);
                jarCache
                        .put(key, new CacheEntry<JarFile>(jar, key, cacheQueue));
            } else {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    security.checkPermission(getPermission());
                }
                if (temp) {
                    lru.remove(new LRUKey(jar, 0));
                }
            }
        } else if (fileString != null) {
            int flags = ZipFile.OPEN_READ + (temp ? ZipFile.OPEN_DELETE : 0);
            jar = new JarFile(new File(Util.decode(fileString, false)), true,
                    flags);
        }

        if (temp) {
            lru.add(new LRUKey(jar, new Date().getTime()));
            if (lru.size() > Limit) {
                lru.remove(lru.first());
            }
        }
        return jar;
    }

    /**
     * Returns the JarEntry of the entry referenced by this
     * <code>URLConnection</code>.
     * 
     * @return java.util.jar.JarEntry the JarEntry referenced
     * 
     * @throws IOException
     *             if an IO error occurs while getting the entry
     */
    @Override
    public JarEntry getJarEntry() throws IOException {
        if (!connected) {
            connect();
        }
        return jarEntry;

    }

    /**
     * Look up the JarEntry of the entry referenced by this
     * <code>URLConnection</code>.
     */
    private void findJarEntry() throws IOException {
        if (getEntryName() == null) {
            return;
        }
        jarEntry = jarFile.getJarEntry(getEntryName());
        if (jarEntry == null) {
            throw new FileNotFoundException(getEntryName());
        }
    }

    /**
     * Creates an input stream for reading from this URL Connection.
     * 
     * @return the input stream
     * 
     * @throws IOException
     *             if an IO error occurs while connecting to the resource.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (closed) {
            throw new IllegalStateException(Msg.getString("KA027"));
        }
        if (!connected) {
            connect();
        }
        if (jarInput != null) {
            return jarInput;
        }
        if (jarEntry == null) {
            throw new IOException(Msg.getString("K00fc")); //$NON-NLS-1$
        }
        return jarInput = new JarURLConnectionInputStream(jarFile
                .getInputStream(jarEntry), jarFile);
    }

    /**
     * Returns the content type of the resource. Test cases reveal that only if
     * the URL is refering to a Jar file, that this method returns a non-null
     * value - x-java/jar.
     * 
     * @return the content type
     */
    @Override
    public String getContentType() {
        // it could also return "x-java/jar" which jdk returns but here, we get
        // it from the URLConnection
        try {
            if (url.getFile().endsWith("!/")) { //$NON-NLS-1$
                return getJarFileURL().openConnection().getContentType();
            }
        } catch (IOException ioe) {
            // Ignore
        }
        // if there is an Jar Entry, get the content type from the name
        return guessContentTypeFromName(url.getFile());
    }

    /**
     * Returns the content length of the resource. Test cases reveal that if the
     * URL is refering to a Jar file, this method returns a content-length
     * returned by URLConnection. If not, it will return -1.
     * 
     * @return the content length
     */
    @Override
    public int getContentLength() {
        try {
            if (url.getFile().endsWith("!/")) { //$NON-NLS-1$
                return getJarFileURL().openConnection().getContentLength();
            }
        } catch (IOException e) {
            //Ignored
        }
        return -1;
    }

    /**
     * Returns the object pointed by this <code>URL</code>. If this
     * URLConnection is pointing to a Jar File (no Jar Entry), this method will
     * return a <code>JarFile</code> If there is a Jar Entry, it will return
     * the object corresponding to the Jar entry content type.
     * 
     * @return a non-null object
     * 
     * @throws IOException
     *             if an IO error occured
     * 
     * @see ContentHandler
     * @see ContentHandlerFactory
     * @see java.io.IOException
     * @see #setContentHandlerFactory(ContentHandlerFactory)
     */
    @Override
    public Object getContent() throws IOException {
        if (!connected) {
            connect();
        }
        // if there is no Jar Entry, return a JarFile
        if (jarEntry == null) {
            return jarFile;
        }
        return super.getContent();
    }

    /**
     * Returns the permission, in this case the subclass, FilePermission object
     * which represents the permission necessary for this URLConnection to
     * establish the connection.
     * 
     * @return the permission required for this URLConnection.
     * 
     * @throws IOException
     *             thrown when an IO exception occurs while creating the
     *             permission.
     */
    @Override
    public Permission getPermission() throws IOException {
        if (jarFileURLConnection != null) {
            return jarFileURLConnection.getPermission();
        }
        return getJarFileURL().openConnection().getPermission();
    }

    /**
     * Closes the cached files.
     */
    public static void closeCachedFiles() {
        Enumeration<CacheEntry<? extends JarFile>> elemEnum = jarCache
                .elements();
        while (elemEnum.hasMoreElements()) {
            try {
                ZipFile zip = elemEnum.nextElement().get();
                if (zip != null) {
                    zip.close();
                }
            } catch (IOException e) {
                // Ignored
            }
        }
    }

    private class JarURLConnectionInputStream extends FilterInputStream {
        InputStream inputStream;

        JarFile jarFile;

        protected JarURLConnectionInputStream(InputStream in, JarFile file) {
            super(in);
            inputStream = in;
            jarFile = file;
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (!useCaches) {
                closed = true;
                jarFile.close();
            }
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public int read(byte[] buf, int off, int nbytes) throws IOException {
            return inputStream.read(buf, off, nbytes);
        }

        @Override
        public long skip(long nbytes) throws IOException {
            return inputStream.skip(nbytes);
        }
    }
}
