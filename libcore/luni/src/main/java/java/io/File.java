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

package java.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.harmony.luni.util.DeleteOnExit;
import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.PriviAction;
import org.apache.harmony.luni.util.Util;

/**
 * An "abstract" representation of a file system entity identified by a
 * pathname. The pathname may be absolute (relative to the root directory
 * of the file system) or relative to the current directory in which the program
 * is running.
 * <p>
 * This class provides methods for querying/changing information about the file
 * as well as directory listing capabilities if the file represents a directory.
 * <p>
 * When manipulating file paths, the static fields of this class may be used to
 * determine the platform specific separators.
 *
 * @see java.io.Serializable
 * @see java.lang.Comparable
 */
public class File implements Serializable, Comparable<File> {

    private static final long serialVersionUID = 301077366599181567L;

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private String path;

    /**
     * The cached UTF-8 byte sequence corresponding to 'path'.
     * This is suitable for direct use by our JNI, and includes a trailing NUL.
     * For non-absolute paths, the "user.dir" property is prepended.
     */
    transient byte[] pathBytes;

    /**
     * The system dependent file separator character.
     */
    public static final char separatorChar;

    /**
     * The system dependent file separator string. The initial value of this
     * field is the system property "file.separator".
     */
    public static final String separator;

    /**
     * The system dependent path separator character.
     */
    public static final char pathSeparatorChar;

    /**
     * The system dependent path separator string. The initial value of this
     * field is the system property "path.separator".
     */
    public static final String pathSeparator;

    /* Temp file counter */
    private static int counter;

    private static boolean caseSensitive;

    // BEGIN android-removed
    // private static native void oneTimeInitialization();
    // END android-removed

    static {
        // The default protection domain grants access to these properties
        // BEGIN android-changed
        // We're on linux so the filesystem is case sensitive and the separator is /.
        separatorChar = System.getProperty("file.separator", "/").charAt(0); //$NON-NLS-1$ //$NON-NLS-2$
        pathSeparatorChar = System.getProperty("path.separator", ";").charAt(0); //$NON-NLS-1$//$NON-NLS-2$
        separator = new String(new char[] { separatorChar }, 0, 1);
        pathSeparator = new String(new char[] { pathSeparatorChar }, 0, 1);
        caseSensitive = true;
        // END android-changed
    }

    /**
     * Constructs a new file using the specified directory and name.
     *
     * @param dir
     *            the directory where the file is stored.
     * @param name
     *            the file's name.
     * @throws NullPointerException
     *             if {@code name} is {@code null}.
     */
    public File(File dir, String name) {
        this(dir == null ? null : dir.getPath(), name);
    }

    /**
     * Constructs a new file using the specified path.
     *
     * @param path
     *            the path to be used for the file.
     */
    public File(String path) {
        // path == null check & NullPointerException thrown by fixSlashes
        init(fixSlashes(path));
    }

    /**
     * Constructs a new File using the specified directory path and file name,
     * placing a path separator between the two.
     *
     * @param dirPath
     *            the path to the directory where the file is stored.
     * @param name
     *            the file's name.
     * @throws NullPointerException
     *             if {@code name} is {@code null}.
     */
    public File(String dirPath, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (dirPath == null) {
            init(fixSlashes(name));
        } else {
            init(calculatePath(dirPath, name));
        }
    }

    /**
     * Constructs a new File using the path of the specified URI. {@code uri}
     * needs to be an absolute and hierarchical Unified Resource Identifier with
     * file scheme and non-empty path component, but with undefined authority,
     * query or fragment components.
     *
     * @param uri
     *            the Unified Resource Identifier that is used to construct this
     *            file.
     * @throws IllegalArgumentException
     *             if {@code uri} does not comply with the conditions above.
     * @see #toURI
     * @see java.net.URI
     */
    public File(URI uri) {
        // check pre-conditions
        checkURI(uri);
        init(fixSlashes(uri.getPath()));
    }

    private void init(String cleanPath) {
        // Keep a copy of the string path.
        this.path = cleanPath;
        // Cache the UTF-8 bytes we need for the JNI.
        if (isAbsolute()) {
            this.pathBytes = newCString(path);
            return;
        }
        String userDir = AccessController.doPrivileged(
            new PriviAction<String>("user.dir")); //$NON-NLS-1$
        if (path.length() == 0) {
            this.pathBytes = newCString(userDir);
            return;
        }
        int length = userDir.length();
        if (length > 0 && userDir.charAt(length - 1) == separatorChar) {
            this.pathBytes = newCString(userDir + path);
        } else {
            this.pathBytes = newCString(userDir + separator + path);
        }
    }

    // Cache the UTF-8 Charset for newCString.
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private byte[] newCString(String s) {
        ByteBuffer buffer = UTF8.encode(s);
        // Add a trailing NUL, because this byte[] is going to be used as a char*.
        int byteCount = buffer.limit() + 1;
        byte[] bytes = new byte[byteCount];
        buffer.get(bytes, 0, byteCount - 1);
        // This is an awful mistake, because '\' is a perfectly acceptable
        // character on Linux/Android. But we've shipped so many versions
        // that behaved like this, I'm too scared to change it.
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] == '\\') {
                bytes[i] = '/';
            }
        }
        return bytes;
    }

    private String calculatePath(String dirPath, String name) {
        dirPath = fixSlashes(dirPath);
        if (!name.equals(EMPTY_STRING) || dirPath.equals(EMPTY_STRING)) {
            // Remove all the proceeding separator chars from name
            name = fixSlashes(name);

            int separatorIndex = 0;
            while ((separatorIndex < name.length())
                    && (name.charAt(separatorIndex) == separatorChar)) {
                separatorIndex++;
            }
            if (separatorIndex > 0) {
                name = name.substring(separatorIndex, name.length());
            }

            // Ensure there is a separator char between dirPath and name
            if (dirPath.length() > 0
                    && (dirPath.charAt(dirPath.length() - 1) == separatorChar)) {
                return dirPath + name;
            }
            return dirPath + separatorChar + name;
        }

        return dirPath;
    }

    @SuppressWarnings("nls")
    private void checkURI(URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException(Msg.getString("K031a", uri));
        } else if (!uri.getRawSchemeSpecificPart().startsWith("/")) {
            throw new IllegalArgumentException(Msg.getString("K031b", uri));
        }

        String temp = uri.getScheme();
        if (temp == null || !temp.equals("file")) {
            throw new IllegalArgumentException(Msg.getString("K031c", uri));
        }

        temp = uri.getRawPath();
        if (temp == null || temp.length() == 0) {
            throw new IllegalArgumentException(Msg.getString("K031d", uri));
        }

        if (uri.getRawAuthority() != null) {
            throw new IllegalArgumentException(Msg.getString("K031e",
                    new String[] { "authority", uri.toString() }));
        }

        if (uri.getRawQuery() != null) {
            throw new IllegalArgumentException(Msg.getString("K031e",
                    new String[] { "query", uri.toString() }));
        }

        if (uri.getRawFragment() != null) {
            throw new IllegalArgumentException(Msg.getString("K031e",
                    new String[] { "fragment", uri.toString() }));
        }
    }

    // BEGIN android-removed
    // private static native byte[][] rootsImpl();
    // private static native boolean isCaseSensitiveImpl();
    // END android-removed

    /**
     * Lists the file system roots. The Java platform may support zero or more
     * file systems, each with its own platform-dependent root. Further, the
     * canonical pathname of any file on the system will always begin with one
     * of the returned file system roots.
     *
     * @return the array of file system roots.
     */
    public static File[] listRoots() {
        // BEGIN android-only
        return new File[] { new File("/") };
        // END android-only
    }

    // Removes duplicate adjacent slashes and any trailing slash.
    // BEGIN android-only: removed support for Windows UNC paths, avoid unnecessary allocation.
    private String fixSlashes(String origPath) {
        // Remove duplicate adjacent slashes.
        boolean lastWasSlash = false;
        char[] newPath = origPath.toCharArray();
        int length = newPath.length;
        int newLength = 0;
        for (int i = 0; i < length; ++i) {
            char ch = newPath[i];
            if (ch == '/') {
                if (!lastWasSlash) {
                    newPath[newLength++] = separatorChar;
                    lastWasSlash = true;
                }
            } else {
                newPath[newLength++] = ch;
                lastWasSlash = false;
            }
        }
        // Remove any trailing slash (unless this is the root of the file system).
        if (lastWasSlash && newLength > 1) {
            newLength--;
        }
        // Reuse the original string if possible.
        return (newLength != length) ? new String(newPath, 0, newLength) : origPath;
    }
    // END android-only

    /**
     * Indicates whether the current context is allowed to read from this file.
     *
     * @return {@code true} if this file can be read, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             read request.
     */
    public boolean canRead() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        // BEGIN android-changed
        return isReadableImpl(pathBytes);
        // END android-changed
    }

    /**
     * Indicates whether the current context is allowed to write to this file.
     *
     * @return {@code true} if this file can be written, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             write request.
     */
    public boolean canWrite() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        // BEGIN android-changed
        return isWritableImpl(pathBytes);
        // END android-changed
    }

    /**
     * Returns the relative sort ordering of the paths for this file and the
     * file {@code another}. The ordering is platform dependent.
     *
     * @param another
     *            a file to compare this file to
     * @return an int determined by comparing the two paths. Possible values are
     *         described in the Comparable interface.
     * @see Comparable
     */
    public int compareTo(File another) {
        if (caseSensitive) {
            return this.getPath().compareTo(another.getPath());
        }
        return this.getPath().compareToIgnoreCase(another.getPath());
    }

    /**
     * Deletes this file. Directories must be empty before they will be deleted.
     *
     * @return {@code true} if this file was deleted, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             request.
     * @see java.lang.SecurityManager#checkDelete
     */
    public boolean delete() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }
        return deleteImpl(pathBytes);
    }

    // BEGIN android-changed
    private native boolean deleteImpl(byte[] filePath);
    // END android-changed

    /**
     * Schedules this file to be automatically deleted once the virtual machine
     * terminates. This will only happen when the virtual machine terminates
     * normally as described by the Java Language Specification section 12.9.
     *
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             request.
     */
    public void deleteOnExit() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }
        // BEGIN android-changed
        DeleteOnExit.getInstance().addFile(getAbsoluteName());
        // END android-changed
    }

    /**
     * Compares {@code obj} to this file and returns {@code true} if they
     * represent the <em>same</em> object using a path specific comparison.
     *
     * @param obj
     *            the object to compare this file with.
     * @return {@code true} if {@code obj} is the same as this object,
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof File)) {
            return false;
        }
        if (!caseSensitive) {
            return path.equalsIgnoreCase(((File) obj).getPath());
        }
        return path.equals(((File) obj).getPath());
    }

    /**
     * Returns a boolean indicating whether this file can be found on the
     * underlying file system.
     *
     * @return {@code true} if this file exists, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #getPath
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public boolean exists() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return existsImpl(pathBytes);
    }

    private native boolean existsImpl(byte[] filePath);

    /**
     * Returns the absolute path of this file.
     *
     * @return the absolute file path.
     */
    public String getAbsolutePath() {
        return Util.toUTF8String(pathBytes, 0, pathBytes.length - 1);
    }

    /**
     * Returns a new file constructed using the absolute path of this file.
     *
     * @return a new file from this file's absolute path.
     * @see java.lang.SecurityManager#checkPropertyAccess
     */
    public File getAbsoluteFile() {
        return new File(this.getAbsolutePath());
    }

    /**
     * Returns the absolute path of this file with all references resolved. An
     * <em>absolute</em> path is one that begins at the root of the file
     * system. The canonical path is one in which all references have been
     * resolved. For the cases of '..' and '.', where the file system supports
     * parent and working directory respectively, these are removed and replaced
     * with a direct directory reference. If the file does not exist,
     * getCanonicalPath() may not resolve any references and simply returns an
     * absolute path name or throws an IOException.
     *
     * @return the canonical path of this file.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public String getCanonicalPath() throws IOException {
        // BEGIN android-removed
        //     Caching the canonical path is bogus. Users facing specific
        //     performance problems can perform their own caching, with
        //     eviction strategies that are appropriate for their application.
        //     A VM-wide cache with no mechanism to evict stale elements is a
        //     disservice to applications that need up-to-date data.
        // String canonPath = FileCanonPathCache.get(absPath);
        // if (canonPath != null) {
        //     return canonPath;
        // }
        // END android-removed

        byte[] result = pathBytes;
        if(separatorChar == '/') {
            // resolve the full path first
            result = resolveLink(result, result.length, false);
            // resolve the parent directories
            result = resolve(result);
        }
        int numSeparators = 1;
        for (int i = 0; i < result.length; i++) {
            if (result[i] == separatorChar) {
                numSeparators++;
            }
        }
        int sepLocations[] = new int[numSeparators];
        int rootLoc = 0;
        if (separatorChar != '/') {
            if (result[0] == '\\') {
                rootLoc = (result.length > 1 && result[1] == '\\') ? 1 : 0;
            } else {
                rootLoc = 2; // skip drive i.e. c:
            }
        }
        byte newResult[] = new byte[result.length + 1];
        int newLength = 0, lastSlash = 0, foundDots = 0;
        sepLocations[lastSlash] = rootLoc;
        for (int i = 0; i <= result.length; i++) {
            if (i < rootLoc) {
                newResult[newLength++] = result[i];
            } else {
                if (i == result.length || result[i] == separatorChar) {
                    if (i == result.length && foundDots == 0) {
                        break;
                    }
                    if (foundDots == 1) {
                        /* Don't write anything, just reset and continue */
                        foundDots = 0;
                        continue;
                    }
                    if (foundDots > 1) {
                        /* Go back N levels */
                        lastSlash = lastSlash > (foundDots - 1) ? lastSlash
                                - (foundDots - 1) : 0;
                        newLength = sepLocations[lastSlash] + 1;
                        foundDots = 0;
                        continue;
                    }
                    sepLocations[++lastSlash] = newLength;
                    newResult[newLength++] = (byte) separatorChar;
                    continue;
                }
                if (result[i] == '.') {
                    foundDots++;
                    continue;
                }
                /* Found some dots within text, write them out */
                if (foundDots > 0) {
                    for (int j = 0; j < foundDots; j++) {
                        newResult[newLength++] = (byte) '.';
                    }
                }
                newResult[newLength++] = result[i];
                foundDots = 0;
            }
        }
        // remove trailing slash
        if (newLength > (rootLoc + 1)
                && newResult[newLength - 1] == separatorChar) {
            newLength--;
        }
        newResult[newLength] = 0;
        newResult = getCanonImpl(newResult);
        newLength = newResult.length;

        // BEGIN android-changed
        //     caching the canonical path is completely bogus
        return Util.toUTF8String(newResult, 0, newLength);
        // FileCanonPathCache.put(absPath, canonPath);
        // return canonPath;
        // END android-changed
    }

    /*
     * Resolve symbolic links in the parent directories.
     */
    private byte[] resolve(byte[] newResult) throws IOException {
        int last = 1, nextSize, linkSize;
        byte[] linkPath = newResult, bytes;
        boolean done, inPlace;
        for (int i = 1; i <= newResult.length; i++) {
            if (i == newResult.length || newResult[i] == separatorChar) {
                done = i >= newResult.length - 1;
                // if there is only one segment, do nothing
                if (done && linkPath.length == 1) {
                    return newResult;
                }
                inPlace = false;
                if (linkPath == newResult) {
                    bytes = newResult;
                    // if there are no symbolic links, terminate the C string
                    // instead of copying
                    if (!done) {
                        inPlace = true;
                        newResult[i] = '\0';
                    }
                } else {
                    nextSize = i - last + 1;
                    linkSize = linkPath.length;
                    if (linkPath[linkSize - 1] == separatorChar) {
                        linkSize--;
                    }
                    bytes = new byte[linkSize + nextSize];
                    System.arraycopy(linkPath, 0, bytes, 0, linkSize);
                    System.arraycopy(newResult, last - 1, bytes, linkSize,
                            nextSize);
                    // the full path has already been resolved
                }
                if (done) {
                    return bytes;
                }
                linkPath = resolveLink(bytes, inPlace ? i : bytes.length, true);
                if (inPlace) {
                    newResult[i] = '/';
                }
                last = i + 1;
            }
        }
        throw new InternalError();
    }

    /*
     * Resolve a symbolic link. While the path resolves to an existing path,
     * keep resolving. If an absolute link is found, resolve the parent
     * directories if resolveAbsolute is true.
     */
    private byte[] resolveLink(byte[] pathBytes, int length,
            boolean resolveAbsolute) throws IOException {
        boolean restart = false;
        byte[] linkBytes, temp;
        do {
            linkBytes = getLinkImpl(pathBytes);
            if (linkBytes == pathBytes) {
                break;
            }
            if (linkBytes[0] == separatorChar) {
                // link to an absolute path, if resolving absolute paths,
                // resolve the parent dirs again
                restart = resolveAbsolute;
                pathBytes = linkBytes;
            } else {
                int last = length - 1;
                while (pathBytes[last] != separatorChar) {
                    last--;
                }
                last++;
                temp = new byte[last + linkBytes.length];
                System.arraycopy(pathBytes, 0, temp, 0, last);
                System.arraycopy(linkBytes, 0, temp, last, linkBytes.length);
                pathBytes = temp;
            }
            length = pathBytes.length;
        } while (existsImpl(pathBytes));
        // resolve the parent directories
        if (restart) {
            return resolve(pathBytes);
        }
        return pathBytes;
    }

    /**
     * Returns a new file created using the canonical path of this file.
     * Equivalent to {@code new File(this.getCanonicalPath())}.
     *
     * @return the new file constructed from this file's canonical path.
     * @throws IOException
     *             if an I/O error occurs.
     * @see java.lang.SecurityManager#checkPropertyAccess
     */
    public File getCanonicalFile() throws IOException {
        return new File(getCanonicalPath());
    }

    private native byte[] getCanonImpl(byte[] filePath);

    /**
     * Returns the name of the file or directory represented by this file.
     *
     * @return this file's name or an empty string if there is no name part in
     *         the file's path.
     */
    public String getName() {
        int separatorIndex = path.lastIndexOf(separator);
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1,
                path.length());
    }

    /**
     * Returns the pathname of the parent of this file. This is the path up to
     * but not including the last name. {@code null} is returned if there is no
     * parent.
     *
     * @return this file's parent pathname or {@code null}.
     */
    public String getParent() {
        int length = path.length(), firstInPath = 0;
        if (separatorChar == '\\' && length > 2 && path.charAt(1) == ':') {
            firstInPath = 2;
        }
        int index = path.lastIndexOf(separatorChar);
        if (index == -1 && firstInPath > 0) {
            index = 2;
        }
        if (index == -1 || path.charAt(length - 1) == separatorChar) {
            return null;
        }
        if (path.indexOf(separatorChar) == index
                && path.charAt(firstInPath) == separatorChar) {
            return path.substring(0, index + 1);
        }
        return path.substring(0, index);
    }

    /**
     * Returns a new file made from the pathname of the parent of this file.
     * This is the path up to but not including the last name. {@code null} is
     * returned when there is no parent.
     *
     * @return a new file representing this file's parent or {@code null}.
     */
    public File getParentFile() {
        String tempParent = getParent();
        if (tempParent == null) {
            return null;
        }
        return new File(tempParent);
    }

    /**
     * Returns the path of this file.
     *
     * @return this file's path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns an integer hash code for the receiver. Any two objects for which
     * {@code equals} returns {@code true} must return the same hash code.
     *
     * @return this files's hash value.
     * @see #equals
     */
    @Override
    public int hashCode() {
        if (caseSensitive) {
            return path.hashCode() ^ 1234321;
        }
        return path.toLowerCase().hashCode() ^ 1234321;
    }

    /**
     * Indicates if this file's pathname is absolute. Whether a pathname is
     * absolute is platform specific. On UNIX, absolute paths must start with
     * the character '/'; on Windows it is absolute if either it starts with
     * '\\' (to represent a file server), or a letter followed by a colon.
     * 
     * @return {@code true} if this file's pathname is absolute, {@code false}
     *         otherwise.
     * @see #getPath
     */
    public boolean isAbsolute() {
        // BEGIN android-changed
        // Removing platform independent code because we're always on linux.
        return path.length() > 0 && path.charAt(0) == separatorChar;
        // END android-changed
    }

    /**
     * Indicates if this file represents a <em>directory</em> on the
     * underlying file system.
     *
     * @return {@code true} if this file is a directory, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public boolean isDirectory() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isDirectoryImpl(pathBytes);
    }

    private native boolean isDirectoryImpl(byte[] filePath);

    /**
     * Indicates if this file represents a <em>file</em> on the underlying
     * file system.
     *
     * @return {@code true} if this file is a file, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public boolean isFile() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isFileImpl(pathBytes);
    }

    private native boolean isFileImpl(byte[] filePath);

    /**
     * Returns whether or not this file is a hidden file as defined by the
     * operating system. The notion of "hidden" is system-dependent. For Unix
     * systems a file is considered hidden if its name starts with a ".". For
     * Windows systems there is an explicit flag in the file system for this
     * purpose.
     *
     * @return {@code true} if the file is hidden, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public boolean isHidden() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        // BEGIN android-only
        return getName().startsWith(".");
        // END android-only
    }

    // BEGIN android-removed
    // private native boolean isHiddenImpl(byte[] filePath);
    // END android-removed

    // BEGIN android-changed
    private native boolean isReadableImpl(byte[] filePath);

    private native boolean isWritableImpl(byte[] filePath);
    // END android-changed

    private native byte[] getLinkImpl(byte[] filePath);

    /**
     * Returns the time when this file was last modified, measured in
     * milliseconds since January 1st, 1970, midnight.
     * Returns 0 if the file does not exist.
     *
     * @return the time when this file was last modified.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public long lastModified() {
        if (path.length() == 0) {
            return 0;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return lastModifiedImpl(pathBytes);
    }

    private native long lastModifiedImpl(byte[] filePath);

    /**
     * Sets the time this file was last modified, measured in milliseconds since
     * January 1st, 1970, midnight.
     *
     * @param time
     *            the last modification time for this file.
     * @return {@code true} if the operation is successful, {@code false}
     *         otherwise.
     * @throws IllegalArgumentException
     *             if {@code time < 0}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access to this file.
     */
    public boolean setLastModified(long time) {
        if (path.length() == 0) {
            return false;
        }
        if (time < 0) {
            throw new IllegalArgumentException(Msg.getString("K006a")); //$NON-NLS-1$
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return setLastModifiedImpl(pathBytes, time);
    }

    private native boolean setLastModifiedImpl(byte[] path, long time);

    /**
     * Marks this file or directory to be read-only as defined by the operating
     * system.
     *
     * @return {@code true} if the operation is successful, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access to this file.
     */
    public boolean setReadOnly() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return setReadOnlyImpl(pathBytes);
    }

    private native boolean setReadOnlyImpl(byte[] path);

    /**
     * Returns the length of this file in bytes.
     * Returns 0 if the file does not exist.
     * The result for a directory is not defined.
     *
     * @return the number of bytes in this file.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public long length() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return lengthImpl(pathBytes);
    }

    private native long lengthImpl(byte[] filePath);

    /**
     * Returns an array of strings with the file names in the directory
     * represented by this file. The result is {@code null} if this file is not
     * a directory.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directory are not returned as part of the list.
     *
     * @return an array of strings with file names or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public java.lang.String[] list() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }

        if (path.length() == 0) {
            return null;
        }

        byte[] bs = pathBytes;
        if (!isDirectoryImpl(bs) || !existsImpl(bs) || !isReadableImpl(bs)) {
            return null;
        }

        byte[][] implList = listImpl(bs);
        if (implList == null) {
            // empty list
            return new String[0];
        }
        String result[] = new String[implList.length];
        for (int index = 0; index < implList.length; index++) {
            result[index] = Util.toUTF8String(implList[index]);
        }
        return result;
    }

    /**
     * Returns an array of files contained in the directory represented by this
     * file. The result is {@code null} if this file is not a directory. The
     * paths of the files in the array are absolute if the path of this file is
     * absolute, they are relative otherwise.
     *
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #list
     * @see #isDirectory
     */
    public File[] listFiles() {
        String[] tempNames = list();
        if (tempNames == null) {
            return null;
        }
        int resultLength = tempNames.length;
        File results[] = new File[resultLength];
        for (int i = 0; i < resultLength; i++) {
            results[i] = new File(this, tempNames[i]);
        }
        return results;
    }

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FilenameFilter and files with matching
     * names are returned as an array of files. Returns {@code null} if this
     * file is not a directory. If {@code filter} is {@code null} then all
     * filenames match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #list(FilenameFilter filter)
     * @see #getPath
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public File[] listFiles(FilenameFilter filter) {
        String[] tempNames = list(filter);
        if (tempNames == null) {
            return null;
        }
        int resultLength = tempNames.length;
        File results[] = new File[resultLength];
        for (int i = 0; i < resultLength; i++) {
            results[i] = new File(this, tempNames[i]);
        }
        return results;
    }

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FileFilter and matching files are
     * returned as an array of files. Returns {@code null} if this file is not a
     * directory. If {@code filter} is {@code null} then all files match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #getPath
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public File[] listFiles(FileFilter filter) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }

        if (path.length() == 0) {
            return null;
        }

        byte[] bs = pathBytes;
        if (!isDirectoryImpl(bs) || !existsImpl(bs) || !isReadableImpl(bs)) {
            return null;
        }

        byte[][] implList = listImpl(bs);
        if (implList == null) {
            return new File[0];
        }
        List<File> tempResult = new ArrayList<File>();
        for (int index = 0; index < implList.length; index++) {
            String aName = Util.toString(implList[index]);
            File aFile = new File(this, aName);
            if (filter == null || filter.accept(aFile)) {
                tempResult.add(aFile);
            }
        }
        return tempResult.toArray(new File[tempResult.size()]);
    }

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FilenameFilter and the names of files
     * with matching names are returned as an array of strings. Returns
     * {@code null} if this file is not a directory. If {@code filter} is
     * {@code null} then all filenames match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #getPath
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public java.lang.String[] list(FilenameFilter filter) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }

        if (path.length() == 0) {
            return null;
        }

        byte[] bs = pathBytes;
        if (!isDirectoryImpl(bs) || !existsImpl(bs) || !isReadableImpl(bs)) {
            return null;
        }

        byte[][] implList = listImpl(bs);
        if (implList == null) {
            // empty list
            return new String[0];
        }
        List<String> tempResult = new ArrayList<String>();
        for (int index = 0; index < implList.length; index++) {
            String aName = Util.toString(implList[index]);
            if (filter == null || filter.accept(this, aName)) {
                tempResult.add(aName);
            }
        }

        return tempResult.toArray(new String[tempResult.size()]);
    }

    private synchronized static native byte[][] listImpl(byte[] path);

    /**
     * Creates the directory named by the trailing filename of this file. Does
     * not create the complete path required to create this directory.
     *
     * @return {@code true} if the directory has been created, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file.
     * @see #mkdirs
     */
    public boolean mkdir() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return mkdirImpl(pathBytes);
    }

    private native boolean mkdirImpl(byte[] filePath);

    /**
     * Creates the directory named by the trailing filename of this file,
     * including the complete directory path required to create this directory.
     *
     * @return {@code true} if the necessary directories have been created,
     *         {@code false} if the target directory already exists or one of
     *         the directories can not be created.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file.
     * @see #mkdir
     */
    public boolean mkdirs() {
        /* If the terminal directory already exists, answer false */
        if (exists()) {
            return false;
        }

        /* If the receiver can be created, answer true */
        if (mkdir()) {
            return true;
        }

        String parentDir = getParent();
        /* If there is no parent and we were not created, answer false */
        if (parentDir == null) {
            return false;
        }

        /* Otherwise, try to create a parent directory and then this directory */
        return (new File(parentDir).mkdirs() && mkdir());
    }

    /**
     * Creates a new, empty file on the file system according to the path
     * information stored in this file.
     *
     * @return {@code true} if the file has been created, {@code false} if it
     *         already exists.
     * @throws IOException if it's not possible to create the file.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file.
     */
    public boolean createNewFile() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        if (path.length() == 0) {
            throw new IOException(Msg.getString("KA012")); //$NON-NLS-1$
        }
        // BEGIN android-changed
        return createNewFileImpl(pathBytes);
        // END android-changed
    }

    private native boolean createNewFileImpl(byte[] filePath);

    /**
     * Creates an empty temporary file using the given prefix and suffix as part
     * of the file name. If suffix is {@code null}, {@code .tmp} is used. This
     * method is a convenience method that calls
     * {@link #createTempFile(String, String, File)} with the third argument
     * being {@code null}.
     *
     * @param prefix
     *            the prefix to the temp file name.
     * @param suffix
     *            the suffix to the temp file name.
     * @return the temporary file.
     * @throws IOException
     *             if an error occurs when writing the file.
     */
    public static File createTempFile(String prefix, String suffix)
            throws IOException {
        return createTempFile(prefix, suffix, null);
    }

    /**
     * Creates an empty temporary file in the given directory using the given
     * prefix and suffix as part of the file name.
     *
     * @param prefix
     *            the prefix to the temp file name.
     * @param suffix
     *            the suffix to the temp file name.
     * @param directory
     *            the location to which the temp file is to be written, or
     *            {@code null} for the default location for temporary files,
     *            which is taken from the "java.io.tmpdir" system property. It
     *            may be necessary to set this property to an existing, writable
     *            directory for this method to work properly.
     * @return the temporary file.
     * @throws IllegalArgumentException
     *             if the length of {@code prefix} is less than 3.
     * @throws IOException
     *             if an error occurs when writing the file.
     */
    @SuppressWarnings("nls")
    public static File createTempFile(String prefix, String suffix,
            File directory) throws IOException {
        // Force a prefix null check first
        if (prefix.length() < 3) {
            throw new IllegalArgumentException(Msg.getString("K006b"));
        }
        String newSuffix = suffix == null ? ".tmp" : suffix;
        File tmpDirFile;
        if (directory == null) {
            String tmpDir = AccessController.doPrivileged(
                new PriviAction<String>("java.io.tmpdir", "."));
            tmpDirFile = new File(tmpDir);
        } else {
            tmpDirFile = directory;
        }
        File result;
        do {
            result = genTempFile(prefix, newSuffix, tmpDirFile);
        } while (!result.createNewFile());
        return result;
    }

    private static File genTempFile(String prefix, String suffix, File directory) {
        if (counter == 0) {
            int newInt = new SecureRandom().nextInt();
            counter = ((newInt / 65535) & 0xFFFF) + 0x2710;
        }
        StringBuilder newName = new StringBuilder();
        newName.append(prefix);
        newName.append(counter++);
        newName.append(suffix);
        return new File(directory, newName.toString());
    }

    /**
     * Renames this file to the name represented by the {@code dest} file. This
     * works for both normal files and directories.
     *
     * @param dest
     *            the file containing the new name.
     * @return {@code true} if the File was renamed, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file or the {@code dest} file.
     */
    public boolean renameTo(java.io.File dest) {
        if (path.length() == 0 || dest.path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
            security.checkWrite(dest.path);
        }
        return renameToImpl(pathBytes, dest.pathBytes);
    }

    private native boolean renameToImpl(byte[] pathExist, byte[] pathNew);

    /**
     * Returns a string containing a concise, human-readable description of this
     * file.
     *
     * @return a printable representation of this file.
     */
    @Override
    public String toString() {
        return path;
    }

    /**
     * Returns a Uniform Resource Identifier for this file. The URI is system
     * dependent and may not be transferable between different operating / file
     * systems.
     *
     * @return an URI for this file.
     */
    @SuppressWarnings("nls")
    public URI toURI() {
        String name = getAbsoluteName();
        try {
            if (!name.startsWith("/")) {
                // start with sep.
                return new URI("file", null, new StringBuilder(
                        name.length() + 1).append('/').append(name).toString(),
                        null, null);
            } else if (name.startsWith("//")) {
                return new URI("file", "", name, null); // UNC path
            }
            return new URI("file", null, name, null, null);
        } catch (URISyntaxException e) {
            // this should never happen
            return null;
        }
    }

    /**
     * Returns a Uniform Resource Locator for this file. The URL is system
     * dependent and may not be transferable between different operating / file
     * systems.
     *
     * @return a URL for this file.
     * @throws java.net.MalformedURLException
     *             if the path cannot be transformed into a URL.
     */
    @SuppressWarnings("nls")
    public URL toURL() throws java.net.MalformedURLException {
        String name = getAbsoluteName();
        if (!name.startsWith("/")) {
            // start with sep.
            return new URL(
                    "file", EMPTY_STRING, -1, new StringBuilder(name.length() + 1) //$NON-NLS-1$
                            .append('/').append(name).toString(), null);
        } else if (name.startsWith("//")) {
            return new URL("file:" + name); // UNC path
        }
        return new URL("file", EMPTY_STRING, -1, name, null);
    }

    private String getAbsoluteName() {
        File f = getAbsoluteFile();
        String name = f.getPath();

        if (f.isDirectory() && name.charAt(name.length() - 1) != separatorChar) {
            // Directories must end with a slash
            name = new StringBuilder(name.length() + 1).append(name)
                    .append('/').toString();
        }
        if (separatorChar != '/') { // Must convert slashes.
            name = name.replace(separatorChar, '/');
        }
        return name;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeChar(separatorChar);

    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        char inSeparator = stream.readChar();
        init(path.replace(inSeparator, separatorChar));
    }
}
