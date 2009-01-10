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
import java.security.AccessController;
import java.util.ArrayList;
// BEGIN android-added
import java.util.Collections;
// END android-added
import java.util.List;

// BEGIN android-removed
// import org.apache.harmony.luni.util.DeleteOnExit;
// END android-removed
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
 * 
 * @since Android 1.0
 */
public class File implements Serializable, Comparable<File> {
    private static final long serialVersionUID = 301077366599181567L;

    private String path;

    transient byte[] properPath;

    /**
     * The system dependent file separator character. Since Android is a Unix-
     * based system, this defaults to '/'.
     * 
     * @since Android 1.0
     */
    public static final char separatorChar;

    /**
     * The system dependent file separator string. The initial value of this
     * field is the system property "file.separator". Since Android is a Unix-
     * based system, this defaults to "/".
     * 
     * @since Android 1.0
     */
    public static final String separator;

    /**
     * The system dependent path separator character. Since Android is a Unix-
     * based system, this defaults to ':'.
     * 
     * @since Android 1.0
     */
    public static final char pathSeparatorChar;

    /**
     * The system dependent path separator string. The initial value of this
     * field is the system property "path.separator". Since Android is a Unix-
     * based system, this defaults to ':'.
     * 
     * @since Android 1.0
     */
    public static final String pathSeparator;

    /* Temp file counter */
    private static int counter;

    private static boolean caseSensitive;

    private static native void oneTimeInitialization();

    static {
        oneTimeInitialization();

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
     *             if {@code name} is null.
     * @since Android 1.0
     */
    public File(File dir, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (dir == null) {
            this.path = fixSlashes(name);
        } else {
            this.path = calculatePath(dir.getPath(), name);
        }
    }

    /**
     * Constructs a new file using the specified path.
     * 
     * @param path
     *            the path to be used for the file.
     * @since Android 1.0
     */
    public File(String path) {
        // path == null check & NullPointerException thrown by fixSlashes
        this.path = fixSlashes(path);
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
     *             if {@code name} is null.
     * @since Android 1.0
     */
    public File(String dirPath, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (dirPath == null) {
            this.path = fixSlashes(name);
        } else {
            this.path = calculatePath(dirPath, name);
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
     * @since Android 1.0
     */
    public File(URI uri) {
        // check pre-conditions
        checkURI(uri);
        this.path = fixSlashes(uri.getPath());
    }

    private String calculatePath(String dirPath, String name) {
        dirPath = fixSlashes(dirPath);
        if (!name.equals("")) { //$NON-NLS-1$
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

    private void checkURI(URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException(Msg.getString("K031a", uri)); //$NON-NLS-1$
        } else if (!uri.getRawSchemeSpecificPart().startsWith("/")) { //$NON-NLS-1$
            throw new IllegalArgumentException(Msg.getString("K031b", uri)); //$NON-NLS-1$
        }

        String temp = uri.getScheme();
        if (temp == null || !temp.equals("file")) { //$NON-NLS-1$
            throw new IllegalArgumentException(Msg.getString("K031c", uri)); //$NON-NLS-1$
        }

        temp = uri.getRawPath();
        if (temp == null || temp.length() == 0) {
            throw new IllegalArgumentException(Msg.getString("K031d", uri)); //$NON-NLS-1$
        }

        if (uri.getRawAuthority() != null) {
            throw new IllegalArgumentException(Msg.getString(
                    "K031e", new String[] { "authority", uri.toString() })); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (uri.getRawQuery() != null) {
            throw new IllegalArgumentException(Msg.getString(
                    "K031e", new String[] { "query", uri.toString() })); //$NON-NLS-1$//$NON-NLS-2$
        }

        if (uri.getRawFragment() != null) {
            throw new IllegalArgumentException(Msg.getString(
                    "K031e", new String[] { "fragment", uri.toString() })); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static native byte[][] rootsImpl();

    private static native boolean isCaseSensitiveImpl();

    /**
     * Lists the file system roots. The Java platform may support zero or more
     * file systems, each with its own platform-dependent root. Further, the
     * canonical pathname of any file on the system will always begin with one
     * of the returned file system roots.
     * 
     * @return the array of file system roots.
     * @since Android 1.0
     */
    public static File[] listRoots() {
        byte[][] rootsList = rootsImpl();
        if (rootsList == null) {
            return new File[0];
        }
        File result[] = new File[rootsList.length];
        for (int i = 0; i < rootsList.length; i++) {
            result[i] = new File(Util.toString(rootsList[i]));
        }
        return result;
    }

    /**
     * The purpose of this method is to take a path and fix the slashes up. This
     * includes changing them all to the current platforms fileSeparator and
     * removing duplicates.
     */
    private String fixSlashes(String origPath) {
        int uncIndex = 1;
        int length = origPath.length(), newLength = 0;
        if (separatorChar == '/') {
            uncIndex = 0;
        } else if (length > 2 && origPath.charAt(1) == ':') {
            uncIndex = 2;
        }

        boolean foundSlash = false;
        char newPath[] = origPath.toCharArray();
        for (int i = 0; i < length; i++) {
            char pathChar = newPath[i];
            if (pathChar == '\\' || pathChar == '/') {
                /* UNC Name requires 2 leading slashes */
                if ((foundSlash && i == uncIndex) || !foundSlash) {
                    newPath[newLength++] = separatorChar;
                    foundSlash = true;
                }
            } else {
                // check for leading slashes before a drive
                if (pathChar == ':'
                        && uncIndex > 0
                        && (newLength == 2 || (newLength == 3 && newPath[1] == separatorChar))
                        && newPath[0] == separatorChar) {
                    newPath[0] = newPath[newLength - 1];
                    newLength = 1;
                    // allow trailing slash after drive letter
                    uncIndex = 2;
                }
                newPath[newLength++] = pathChar;
                foundSlash = false;
            }
        }
        // remove trailing slash
        if (foundSlash
                && (newLength > (uncIndex + 1) || (newLength == 2 && newPath[0] != separatorChar))) {
            newLength--;
        }
        String tempPath = new String(newPath, 0, newLength);
        // If it's the same keep it identical for SecurityManager purposes
        if (!tempPath.equals(origPath)) {
            return tempPath;
        }
        return origPath;
    }

    /**
     * Indicates whether the current context is allowed to read from this file.
     * 
     * @return {@code true} if this file can be read, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             read request.
     * @since Android 1.0
     */
    public boolean canRead() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        // BEGIN android-changed
        return exists() && isReadableImpl(properPath(true));
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
     * @since Android 1.0
     */
    public boolean canWrite() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }

        // Cannot use exists() since that does an unwanted read-check.
        boolean exists = false;
        if (path.length() > 0) {
            exists = existsImpl(properPath(true));
        }
        // BEGIN android-changed
        return exists && isWriteableImpl(properPath(true));
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
     * @since Android 1.0
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
     * @since Android 1.0
     */
    public boolean delete() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkDelete(path);
        }
        byte[] propPath = properPath(true);
        if ((path.length() != 0) && isDirectoryImpl(propPath)) {
            return deleteDirImpl(propPath);
        }
        return deleteFileImpl(propPath);
    }

    private native boolean deleteDirImpl(byte[] filePath);

    private native boolean deleteFileImpl(byte[] filePath);

    /**
     * Schedules this file to be automatically deleted once the virtual machine
     * terminates. This will only happen when the virtual machine terminates 
     * normally as described by the Java Language Specification section 12.9.
     * 
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             request.
     * @since Android 1.0
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
     * @since Android 1.0
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
     * @since Android 1.0
     */
    public boolean exists() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return existsImpl(properPath(true));
    }

    private native boolean existsImpl(byte[] filePath);

    /**
     * Returns the absolute path of this file.
     * 
     * @return the absolute file path.
     * @see java.lang.SecurityManager#checkPropertyAccess
     * @since Android 1.0
     */
    public String getAbsolutePath() {
        byte[] absolute = properPath(false);
        return Util.toString(absolute);
    }

    /**
     * Returns a new file constructed using the absolute path of this file.
     * 
     * @return a new file from this file's absolute path.
     * @see java.lang.SecurityManager#checkPropertyAccess
     * @since Android 1.0
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
     * @see java.lang.SecurityManager#checkPropertyAccess
     * @since Android 1.0
     */
    public String getCanonicalPath() throws IOException {
        byte[] result = properPath(false);

        boolean exists = false;
        byte[] pathBytes = result;
        do {
            byte[] linkBytes = getLinkImpl(pathBytes);
            if (linkBytes == pathBytes) {
                break;
            }
            if (linkBytes[0] == separatorChar) {
                pathBytes = linkBytes;
            } else {
                int index = pathBytes.length - 1;
                while (pathBytes[index] != separatorChar) {
                    index--;
                }
                byte[] temp = new byte[index + 1 + linkBytes.length];
                System.arraycopy(pathBytes, 0, temp, 0, index + 1);
                System.arraycopy(linkBytes, 0, temp, index + 1,
                        linkBytes.length);
                pathBytes = temp;
            }
            exists = existsImpl(pathBytes);
        } while (exists);
        if (exists) {
            result = pathBytes;
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
        return Util.toString(newResult, 0, newLength);
    }

    /**
     * Returns a new file created using the canonical path of this file.
     * Equivalent to {@code new File(this.getCanonicalPath())}.
     * 
     * @return the new file constructed from this file's canonical path.
     * @throws IOException
     *             if an I/O error occurs.
     * @see java.lang.SecurityManager#checkPropertyAccess
     * @since Android 1.0
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
     * @since Android 1.0
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
     * @since Android 1.0
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
     * @since Android 1.0
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
     * @since Android 1.0
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
     * @since Android 1.0
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
     * '\', '/', '\\' (to represent a file server), or a letter followed by a
     * colon.
     * 
     * @return {@code true} if this file's pathname is absolute, {@code false}
     *         otherwise.
     * @see #getPath
     * @since Android 1.0
     */
    public boolean isAbsolute() {
        // BEGIN android-changed
        // Removing platform independent code because we're always on linux.
        return path.length() > 0 && path.charAt(0) == separatorChar;
        // END android-changed
    }

    // BEGIN android-removed
    // private native boolean isAbsoluteImpl(byte[] filePath);
    // END android-removed

    /**
     * Indicates if this file represents a <em>directory</em> on the
     * underlying file system.
     * 
     * @return {@code true} if this file is a directory, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @since Android 1.0
     */
    public boolean isDirectory() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isDirectoryImpl(properPath(true));
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
     * @since Android 1.0
     */
    public boolean isFile() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isFileImpl(properPath(true));
    }

    private native boolean isFileImpl(byte[] filePath);

    /**
     * Returns whether or not this file is a hidden file as defined by the
     * operating system. The notion of "hidden" is system-dependent. For
     * Unix systems (like Android) a file is considered hidden if its name
     * starts with a ".". For Windows systems there is an explicit flag in the
     * file system for this purpose.
     * 
     * @return {@code true} if the file is hidden, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @since Android 1.0
     */
    public boolean isHidden() {
        if (path.length() == 0) {
            return false;
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return isHiddenImpl(properPath(true));
    }

    private native boolean isHiddenImpl(byte[] filePath);

    // BEGIN android-changed
    private native boolean isReadableImpl(byte[] filePath);

    private native boolean isWriteableImpl(byte[] filePath);
    // END android-changed

    private native byte[] getLinkImpl(byte[] filePath);

    /**
     * Returns the time when this file was last modified, measured in
     * milliseconds since January 1st, 1970, midnight.
     * 
     * @return the time when this file was last modified.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @since Android 1.0
     */
    public long lastModified() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        long result = lastModifiedImpl(properPath(true));
        /* Temporary code to handle both return cases until natives fixed */
        if (result == -1 || result == 0) {
            return 0;
        }
        return result;
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
     * @since Android 1.0
     */
    public boolean setLastModified(long time) {
        if (time < 0) {
            throw new IllegalArgumentException(Msg.getString("K006a")); //$NON-NLS-1$
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return (setLastModifiedImpl(properPath(true), time));
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
     * @since Android 1.0
     */
    public boolean setReadOnly() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return (setReadOnlyImpl(properPath(true)));
    }

    private native boolean setReadOnlyImpl(byte[] path);

    /**
     * Returns the length of this file in bytes.
     * 
     * @return the number of bytes in this file.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @since Android 1.0
     */
    public long length() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        return lengthImpl(properPath(true));
    }

    private native long lengthImpl(byte[] filePath);

    /**
     * Returns an array of strings with the file names in the directory
     * represented by this file. The result is {@ null} if this file is not a
     * directory.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directory are not returned as part of the list.
     * </p>
     * 
     * @return an array of strings with file names or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #isDirectory
     * @since Android 1.0
     */
    public java.lang.String[] list() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (!isDirectory()) {
            return null;
        }
        byte[][] implList = listImpl(properPath(true));
        if (implList == null) {
            return new String[0];
        }
        String result[] = new String[implList.length];
        for (int index = 0; index < implList.length; index++) {
            result[index] = Util.toString(implList[index]);
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
     * @since Android 1.0
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
     * </p>
     * 
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #list(FilenameFilter filter)
     * @since Android 1.0
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
     * </p>
     * 
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @since Android 1.0
     */
    public File[] listFiles(FileFilter filter) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (!isDirectory()) {
            return null;
        }
        byte[][] implList = listImpl(properPath(true));
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
     * </p>
     * 
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @since Android 1.0
     */
    public java.lang.String[] list(FilenameFilter filter) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (!isDirectory()) {
            return null;
        }
        byte[][] implList = listImpl(properPath(true));
        if (implList == null) {
            return new String[0];
        }
        java.util.Vector<String> tempResult = new java.util.Vector<String>();
        for (int index = 0; index < implList.length; index++) {
            String aName = Util.toString(implList[index]);
            if (filter == null || filter.accept(this, aName)) {
                tempResult.addElement(aName);
            }
        }
        String[] result = new String[tempResult.size()];
        tempResult.copyInto(result);
        return result;
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
     * @since Android 1.0
     */
    public boolean mkdir() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        return mkdirImpl(properPath(true));
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
     * @since Android 1.0
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
     * @throws IOException
     *             if an I/O error occurs or the directory does not exist where
     *             the file should have been created.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file.
     * @since Android 1.0
     */
    public boolean createNewFile() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        if (0 == path.length()) {
            throw new IOException(Msg.getString("KA012")); //$NON-NLS-1$
        }
        int result = newFileImpl(properPath(true));
        switch (result) {
            case 0:
                return true;
            case 1:
                return false;
            // BEGIN android-changed
            default: {
                // Try to provide a reasonable explanation.
                String msg = null;
                try {
                    File parent = getAbsoluteFile().getParentFile();
                    if (parent == null) {
                        /*
                         * This shouldn't happen, unless the caller
                         * tried to create "/". We just use the
                         * generic message for this case.
                         */
                    } else if (! parent.exists()) {
                        msg = "Parent directory of file does not exist";
                    } else if (! parent.isDirectory()) {
                        msg = "Parent of file is not a directory";
                    } else if (! parent.canWrite()) {
                        msg = "Parent directory of file is not writable";
                    }
                } catch (RuntimeException ex) {
                    /*
                     * Ignore the exception, and just fall through to
                     * use a generic message.
                     */
                }

                if (msg == null) {
                    msg = "Cannot create";
                }
                throw new IOException(msg + ": " + path); //$NON-NLS-1$
            }
            // END android-changed
        }
    }

    private native int newFileImpl(byte[] filePath);

    /**
     * Creates an empty temporary file using the given prefix and suffix as part
     * of the file name. If suffix is null, {@code .tmp} is used. This method
     * is a convenience method that calls {@link #createTempFile(String, String,
     * File)} with the third argument being {@code null}.
     * 
     * @param prefix
     *            the prefix to the temp file name.
     * @param suffix
     *            the suffix to the temp file name.
     * @return the temporary file.
     * @throws IOException
     *             if an error occurs when writing the file.
     * @since Android 1.0
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
     * @since Android 1.0
     */
    public static File createTempFile(String prefix, String suffix,
            File directory) throws IOException {
        // Force a prefix null check first
        if (prefix.length() < 3) {
            throw new IllegalArgumentException(Msg.getString("K006b")); //$NON-NLS-1$
        }
        String newSuffix = suffix == null ? ".tmp" : suffix; //$NON-NLS-1$
        String tmpDir = "."; //$NON-NLS-1$
        tmpDir = AccessController.doPrivileged(new PriviAction<String>(
                "java.io.tmpdir", ".")); //$NON-NLS-1$//$NON-NLS-2$
        File result, tmpDirFile = directory == null ? new File(tmpDir)
                : directory;
        do {
            result = genTempFile(prefix, newSuffix, tmpDirFile);
        } while (!result.createNewFile());
        return result;
    }

    private static File genTempFile(String prefix, String suffix, File directory) {
        if (counter == 0) {
            int newInt = new java.util.Random().nextInt();
            counter = ((newInt / 65535) & 0xFFFF) + 0x2710;
        }
        StringBuilder newName = new StringBuilder();
        newName.append(prefix);
        newName.append(counter++);
        newName.append(suffix);
        return new File(directory, newName.toString());
    }

    // BEGIN android-changed
    // Removing platform independent code because we're always on linux.
    /**
     * Returns a string representing the proper path for this file. If this file
     * path is absolute, the user.dir property is not prepended, otherwise it
     * is.
     * 
     * @param internal
     *            is user.dir internal.
     * @return the proper path.
     */
    byte[] properPath(boolean internal) {
        if (properPath != null) {
            return properPath;
        }
        if(path.length() > 0 && path.charAt(0) == separatorChar) {
            return properPath = Util.getBytes(path);
        }
        // Check security by getting user.dir when the path is not absolute
        String userdir;
        if (internal) {
            userdir = AccessController.doPrivileged(new PriviAction<String>(
                    "user.dir")); //$NON-NLS-1$
        } else {
            userdir = System.getProperty("user.dir"); //$NON-NLS-1$
        }
        if (path.length() == 0) {
            return properPath = Util.getBytes(userdir);
        }
        int length = userdir.length();
        if (length > 0 && userdir.charAt(length - 1) == separatorChar) {
            return properPath = Util.getBytes(userdir + path);
        }
        return properPath = Util.getBytes(userdir + separator + path);
    }
    // END android-changed

    // BEGIN android-removed
    // private static native byte[] properPathImpl(byte[] path);
    // END android-removed

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
     * @since Android 1.0
     */
    public boolean renameTo(java.io.File dest) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
            security.checkWrite(dest.path);
        }
        return renameToImpl(properPath(true), dest.properPath(true));
    }

    private native boolean renameToImpl(byte[] pathExist, byte[] pathNew);

    /**
     * Returns a string containing a concise, human-readable description of this
     * file.
     * 
     * @return a printable representation of this file.
     * @since Android 1.0
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
     * @since Android 1.0
     */
    public URI toURI() {
        String name = getAbsoluteName();
        try {
            if (!name.startsWith("/")) { //$NON-NLS-1$
                // start with sep.
                return new URI("file", null, //$NON-NLS-1$
                        new StringBuilder(name.length() + 1).append('/')
                                .append(name).toString(), null, null);
            } else if (name.startsWith("//")) { //$NON-NLS-1$
                return new URI("file", name, null); // UNC path //$NON-NLS-1$
            }
            return new URI("file", null, name, null, null); //$NON-NLS-1$
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
     * @return an URL for this file.
     * @throws java.net.MalformedURLException
     *             if the path cannot be transformed into an URL.
     * @since Android 1.0
     */
    public URL toURL() throws java.net.MalformedURLException {
        String name = getAbsoluteName();
        if (!name.startsWith("/")) { //$NON-NLS-1$
            // start with sep.
            return new URL("file", "", -1, new StringBuilder(name.length() + 1) //$NON-NLS-1$ //$NON-NLS-2$
                    .append('/').append(name).toString(), null);
        } else if (name.startsWith("//")) { //$NON-NLS-1$
            return new URL("file:" + name); // UNC path //$NON-NLS-1$
        }
        return new URL("file", "", -1, name, null); //$NON-NLS-1$ //$NON-NLS-2$
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
        path = path.replace(inSeparator, separatorChar);
    }
}

// BEGIN android-added
/**
 * Implements the actual DeleteOnExit mechanism. Is registered as a shutdown
 * hook in the Runtime, once it is actually being used.
 */
class DeleteOnExit extends Thread {
    
    /**
     * Our singleton instance.
     */
    private static DeleteOnExit instance;
    
    /**
     * Our list of files scheduled for deletion.
     */
    private ArrayList<String> files = new ArrayList<String>();
    
    /**
     * Returns our singleton instance, creating it if necessary.
     */
    public static synchronized DeleteOnExit getInstance() {
        if (instance == null) {
            instance = new DeleteOnExit();
            Runtime.getRuntime().addShutdownHook(instance);
        }
        
        return instance;
    }
    
    /**
     * Schedules a file for deletion.
     * 
     * @param filename The file to delete.
     */
    public void addFile(String filename) {
        synchronized(files) {
            if (!files.contains(filename)) {
                files.add(filename);
            }
        }
    }
    
    /**
     * Does the actual work. Note we (a) first sort the files lexicographically
     * and then (b) delete them in reverse order. This is to make sure files
     * get deleted before their parent directories.
     */
    @Override
    public void run() {
        Collections.sort(files);
        for (int i = files.size() - 1; i >= 0; i--) {
            new File(files.get(i)).delete();
        }
    }
}
// END android-added
