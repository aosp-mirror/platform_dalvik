/*
 * Copyright (C) 2010 The Android Open Source Project
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Class loader test.
 */
public class Main {

    /**
     * A class loader which loads classes from the dex file
     * "test.jar". However, it will return null when asked to load the
     * class InaccessibleSuper.
     *
     * When testing code calls BrokenDexLoader's findBrokenClass(),
     * a BrokenDexLoader will be the defining loader for the class
     * Inaccessible.  The VM will call the defining loader for
     * "InaccessibleSuper", which will return null, which the VM
     * should be able to deal with gracefully.
     *
     * Note that this depends heavily on the Dalvik test harness.
     */
    static class BrokenDexLoader extends ClassLoader {

        /** We return null when asked to load InaccessibleSuper. */
        private static class InaccessibleSuper {}
        private static class Inaccessible extends InaccessibleSuper {}

        private static final String SUPERCLASS_NAME =
                "Main$BrokenDexLoader$InaccessibleSuper";
        private static final String CLASS_NAME =
                "Main$BrokenDexLoader$Inaccessible";

        private static final String DEX_FILE = "test.jar";

        public BrokenDexLoader(ClassLoader parent) {
            super(parent);
        }

        /**
         * Finds the class with the specified binary name, from DEX_FILE.
         *
         * If we don't find a match, we throw an exception.
         */
        private Class<?> findDexClass(String name) throws ClassNotFoundException
        {
            Class mDexClass;
            Object mDexFile;

            /*
             * Find the DexFile class, and construct a DexFile object
             * through reflection.
             */

            mDexClass = ClassLoader.getSystemClassLoader().
                    loadClass("dalvik/system/DexFile");

            Constructor ctor;
            try {
                ctor = mDexClass.getConstructor(new Class[] {String.class});
            } catch (NoSuchMethodException nsme) {
                throw new ClassNotFoundException("getConstructor failed",
                                                 nsme);
            }

            try {
                mDexFile = ctor.newInstance(DEX_FILE);
            } catch (InstantiationException ie) {
                throw new ClassNotFoundException("newInstance failed", ie);
            } catch (IllegalAccessException iae) {
                throw new ClassNotFoundException("newInstance failed", iae);
            } catch (InvocationTargetException ite) {
                throw new ClassNotFoundException("newInstance failed", ite);
            }

            /*
             * Call DexFile.loadClass(String, ClassLoader).
             */
            Method meth;

            try {
                meth = mDexClass.getMethod("loadClass",
                           new Class[] { String.class, ClassLoader.class });
            } catch (NoSuchMethodException nsme) {
                throw new ClassNotFoundException("getMethod failed", nsme);
            }

            try {
                meth.invoke(mDexFile, name, this);
            } catch (IllegalAccessException iae) {
                throw new ClassNotFoundException("loadClass failed", iae);
            } catch (InvocationTargetException ite) {
                throw new ClassNotFoundException("loadClass failed",
                                                 ite.getCause());
            }

            return null;
        }

        /**
         * Load a class.
         *
         * Return null if the class's name is SUPERCLASS_NAME;
         * otherwise invoke the super's loadClass method.
         */
        public Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException
        {
            if (SUPERCLASS_NAME.equals(name)) {
                return null;
            }

            return super.loadClass(name, resolve);
        }

        /**
         * Attempt to find the class with the superclass we refuse to
         * load.
         */
        public void findBrokenClass() throws ClassNotFoundException
        {
            findDexClass(CLASS_NAME);
        }
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) throws ClassNotFoundException {
        /*
         * Run tests.
         */
        testAccess();
    }

    /**
     * See if we can load a class when the loader returns null for the
     * superclass.
     */
    static void testAccess() {
        try {
            BrokenDexLoader loader;

            loader = new BrokenDexLoader(ClassLoader.getSystemClassLoader());
            loader.findBrokenClass();
            System.err.println("ERROR: Inaccessible was accessible");
        } catch (ClassNotFoundException cnfe) {
            Throwable cause = cnfe.getCause();
            if (cause instanceof NullPointerException) {
                System.out.println("Got expected CNFE/NPE");
            } else {
                System.err.println("Got unexpected CNFE");
                cnfe.printStackTrace();
            }
        }
    }
}
