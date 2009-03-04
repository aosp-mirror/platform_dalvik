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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.net.MalformedURLException;

import dalvik.system.DexFile;

/**
 * Cloned out of PathClassLoader for TouchDex.  This could be made
 * substantially smaller, since we don't need most of this.
 */
class TouchDexLoader extends ClassLoader {

    private String path;
    
    private boolean initialized;
    
    /**
     * Create a ClassLoader that finds files in the specified path.
     */
    public TouchDexLoader(String path, ClassLoader parent) {
        super(parent);

        if (path == null)
            throw new NullPointerException();

        this.path = path;
    }
    
    private void ensureInit() {
        if (initialized) {
            return;
        }
        
        initialized = true;
        
        mPaths = path.split(":");
        //System.out.println("TouchDexLoader: " + mPaths);
        mFiles = new File[mPaths.length];
        mZips = new ZipFile[mPaths.length];
        mDexs = new DexFile[mPaths.length];

        boolean wantDex = 
            System.getProperty("android.vm.dexfile", "").equals("true");

        /* open all Zip and DEX files up front */
        for (int i = 0; i < mPaths.length; i++) {
            //System.out.println("My path is: " + mPaths[i]);
            File pathFile = new File(mPaths[i]);
            mFiles[i] = pathFile;

            if (pathFile.isFile()) {
                if (false) {    //--------------------
                try {
                    mZips[i] = new ZipFile(pathFile);
                }
                catch (IOException ioex) {
                    // expecting IOException and ZipException
                    //System.out.println("Failed opening '" + archive + "': " + ioex);
                    //ioex.printStackTrace();
                }
                }               //--------------------
                if (wantDex) {
                    /* we need both DEX and Zip, because dex has no resources */
                    try {
                        mDexs[i] = new DexFile(pathFile);
                    }
                    catch (IOException ioex) {
                        System.err.println("Couldn't open " + mPaths[i]
                            + " as DEX");
                    }
                }
            } else {
                System.err.println("File not found: " + mPaths[i]);
            }
        }

        /*
         * Prep for native library loading.
         */
        String pathList = System.getProperty("java.library.path", ".");
        String pathSep = System.getProperty("path.separator", ":");
        String fileSep = System.getProperty("file.separator", "/");

        mLibPaths = pathList.split(pathSep);

        // Add a '/' to the end so we don't have to do the property lookup
        // and concatenation later.
        for (int i = 0; i < mLibPaths.length; i++) {
            if (!mLibPaths[i].endsWith(fileSep))
                mLibPaths[i] += fileSep;
            if (false)
                System.out.println("Native lib path:  " + mLibPaths[i]);
        }
    }

    /**
     * Find the class with the specified name.  None of our ancestors were
     * able to find it, so it's up to us now.
     *
     * "name" is a "binary name", e.g. "java.lang.String" or
     * "java.net.URLClassLoader$3$1".
     *
     * This method will either return a valid Class object or throw an
     * exception.  Does not return null.
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        ensureInit();
        
        byte[] data = null;
        int i;

        //System.out.println("TouchDexLoader " + this + ": findClass '" + name + "'");

        for (i = 0; i < mPaths.length; i++) {
            //System.out.println("My path is: " + mPaths[i]);

            if (mDexs[i] != null) {
                String slashName = name.replace('.', '/');
                Class clazz = mDexs[i].loadClass(slashName, this);
                if (clazz != null)
                    return clazz;
            } else if (mZips[i] != null) {
                String fileName = name.replace('.', '/') + ".class";
                data = loadFromArchive(mZips[i], fileName);
            } else {
                File pathFile = mFiles[i];
                if (pathFile.isDirectory()) {
                    String fileName =
                        mPaths[i] + "/" + name.replace('.', '/') + ".class";
                    data = loadFromDirectory(fileName);
                } else {
                    //System.out.println("TouchDexLoader: can't find '"
                    //    + mPaths[i] + "'");
                }

            }

            if (data != null) {
                //System.out.println("  found class " + name);
                int dotIndex = name.lastIndexOf('.');
                if (dotIndex != -1) {
                    String packageName = name.substring(0, dotIndex);
                    synchronized (this) {
                        Package packageObj = getPackage(packageName);
                        if (packageObj == null) {
                            definePackage(packageName, null, null,
                                    null, null, null, null, null);
                        }
                    }
                }
                
                return defineClass(name, data, 0, data.length);
            }
        }

        throw new ClassNotFoundException(name + " in loader " + this);
    }

    /*
     * Find a resource by name.  This could be in a directory or in an
     * archive.
     */
    protected URL findResource(String name) {
        ensureInit();
        
        byte[] data = null;
        int i;

        //System.out.println("TouchDexLoader: findResource '" + name + "'");

        for (i = 0; i < mPaths.length; i++) {
            File pathFile = mFiles[i];
            ZipFile zip = mZips[i];
            if (zip != null) {
                if (isInArchive(zip, name)) {
                    //System.out.println("  found " + name + " in " + pathFile);
                    // Create URL correctly - was XXX, new code should be ok.
                    try {
                        return new URL("jar:file://" + pathFile + "!/" + name);
                    }
                    catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (pathFile.isDirectory()) {
                File dataFile = new File(mPaths[i] + "/" + name);
                if (dataFile.exists()) {
                    //System.out.println("  found resource " + name);
                    // Create URL correctly - was XXX, new code should be ok.
                    try {
                        return new URL("file:" + name);
                    }
                    catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (pathFile.isFile()) {
            } else {
                System.err.println("TouchDexLoader: can't find '"
                    + mPaths[i] + "'");
            }
        }

        return null;
    }


    /*
     * Load the contents of a file from a file in a directory.
     *
     * Returns null if the class wasn't found.
     */
    private byte[] loadFromDirectory(String path) {
        RandomAccessFile raf;
        byte[] fileData;

        //System.out.println("Trying to load from " + path);
        try {
            raf = new RandomAccessFile(path, "r");
        }
        catch (FileNotFoundException fnfe) {
            //System.out.println("  Not found: " + path);
            return null;
        }

        try {
            fileData = new byte[(int) raf.length()];
            raf.read(fileData);
            raf.close();
        }
        catch (IOException ioe) {
            System.err.println("Error reading from " + path);
            // swallow it, return null instead
            fileData = null;
        }

        return fileData;
    }

    /*
     * Load a class from a file in an archive.  We currently assume that
     * the file is a Zip archive.
     *
     * Returns null if the class wasn't found.
     */
    private byte[] loadFromArchive(ZipFile zip, String name) {
        ZipEntry entry;

        entry = zip.getEntry(name);
        if (entry == null)
            return null;

        ByteArrayOutputStream byteStream;
        InputStream stream;
        int count;

        /*
         * Copy the data out of the stream.  Because we got the ZipEntry
         * from a ZipFile, the uncompressed size is known, and we can set
         * the initial size of the ByteArrayOutputStream appropriately.
         */
        try {
            stream = zip.getInputStream(entry);
            byteStream = new ByteArrayOutputStream((int) entry.getSize());
            byte[] buf = new byte[4096];
            while ((count = stream.read(buf)) > 0)
                byteStream.write(buf, 0, count);

            stream.close();
        }
        catch (IOException ioex) {
            //System.out.println("Failed extracting '" + archive + "': " +ioex);
            return null;
        }

        //System.out.println("  loaded from Zip");
        return byteStream.toByteArray();
    }

    /*
     * Figure out if "name" is a member of "archive".
     */
    private boolean isInArchive(ZipFile zip, String name) {
        return zip.getEntry(name) != null;
    }

    /**         
     * Find a native library.
     *
     * Return the full pathname of the first appropriate-looking file
     * we find.
     */
    protected String findLibrary(String libname) {
        ensureInit();

        String fileName = System.mapLibraryName(libname);
        for (int i = 0; i < mLibPaths.length; i++) {
            String pathName = mLibPaths[i] + fileName;
            File test = new File(pathName);

            if (test.exists())
                return pathName;
        }

        return null;
    }

    private String[] mPaths;
    private File[] mFiles;
    private ZipFile[] mZips;
    private DexFile[] mDexs;
    private String[] mLibPaths;
}

