/*
 * Copyright (C) 2007 The Android Open Source Project
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

package dalvik.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.Enumeration;


/**
 * Manipulate DEX files.  Similar in principle to java.util.zip.ZipFile.
 * Used primarily by class loaders.
 *
 * We don't directly open and read the DEX file here.  They're mapped read-only
 * by the VM.
 */
public final class DexFile {
    private final int mCookie;
    private String mFileName;

    /**
     * Open a DEX file from a File object.
     */
    public DexFile(File file) throws IOException {
        this(file.getPath());
    }

    /**
     * Open a DEX file from a filename (preferrably a full path).
     *
     * This will usually be a Zip/Jar with a "classes.dex" inside.  Do not
     * specify the "dalvik-cache" version directly.
     */
    public DexFile(String fileName) throws IOException {
        String wantDex = System.getProperty("android.vm.dexfile", "false");
        if (!wantDex.equals("true"))
            throw new UnsupportedOperationException("No dex in this VM");

        mCookie = openDexFile(fileName);
        mFileName = fileName;
        //System.out.println("DEX FILE cookie is " + mCookie);
    }

    /**
     * Get the name of the open file.
     */
    public String getName() {
        return mFileName;
    }

    /**
     * Close a DEX file.
     *
     * This may not be able to release any resources.  If classes have
     * been loaded, the underlying storage can't be discarded.
     */
    public void close() throws IOException {
        closeDexFile(mCookie);
    }

    /**
     * Load a class.  Returns the class on success, or a null reference
     * on failure.
     *
     * If you are not calling this from a class loader, this is most likely
     * not going to do what you want.  Use Class.forName() instead.
     *
     * "name" should look like "java/lang/String".
     *
     * I'm not throwing an exception if the class isn't found because I
     * don't want to be throwing exceptions wildly every time we load a
     * class that isn't in the first DEX file we look at.  This method
     * *will* throw exceptions for anything that isn't ClassNotFoundException.
     */
    public Class loadClass(String name, ClassLoader loader) {
        return defineClass(name, loader, mCookie,
            null);
            //new ProtectionDomain(name) /*DEBUG ONLY*/);
    }
    native private static Class defineClass(String name, ClassLoader loader,
        int cookie, ProtectionDomain pd);

    /**
     * Enumerate the names of the classes in this DEX file.
     */
    public Enumeration<String> entries() {
        return new DFEnum(this);
    }

    /*
     * Helper class.
     */
    private class DFEnum implements Enumeration<String> {
        private int mIndex;
        private String[] mNameList;

        DFEnum(DexFile df) {
            mIndex = 0;
            mNameList = getClassNameList(mCookie);
        }

        public boolean hasMoreElements() {
            return (mIndex < mNameList.length);
        }

        public String nextElement() {
            return mNameList[mIndex++];
        }
    }

    /* return a String array with class names */
    native private static String[] getClassNameList(int cookie);

    /** 
     * GC helper.
     */
    protected void finalize() throws IOException {
        close();
    }

    /*
     * Open a DEX file.  The value returned is a magic VM cookie.  On
     * failure, an IOException is thrown.
     */
    native private static int openDexFile(String fileName) throws IOException;
    native private static void closeDexFile(int cookie);

    /**
     * Returns true if the VM believes that the apk/jar file is out of date
     * and should be passed through "dexopt" again.
     *
     * @param fileName the absolute path to the apk/jar file to examine.
     * @return true if dexopt should be called on the file, false otherwise.
     * @throws java.io.FileNotFoundException if fileName is not readable,
     *         not a file, or not present.
     * @throws java.io.IOException if fileName is not a valid apk/jar file or
     *         if problems occur while parsing it.
     * @throws java.lang.NullPointerException if fileName is null.
     * @throws dalvik.system.StaleDexCacheError if the optimized dex file
     *         is stale but exists on a read-only partition.
     */
    native public static boolean isDexOptNeeded(String fileName)
            throws FileNotFoundException, IOException;
}

