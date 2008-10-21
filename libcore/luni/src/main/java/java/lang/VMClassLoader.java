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

package java.lang;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;

class VMClassLoader {

    /**
     * Get a resource from a file in the bootstrap class path.
     *
     * It would be simpler to just walk through the class path elements
     * ourselves, but that would require reopening Jar files.
     *
     * We assume that the bootclasspath can't change once the VM has
     * started.  This assumption seems to be supported by the spec.
     */
    static URL getResource(String name) {
        int numEntries = getBootClassPathSize();
        int i;

        for (i = 0; i < numEntries; i++) {
            String urlStr = getBootClassPathResource(name, i);
            if (urlStr != null) {
                try {
                    return new URL(urlStr);
                }
                catch (MalformedURLException mue) {
                    mue.printStackTrace();
                    // unexpected; keep going
                }
            }
        }

        return null;
    }

    /*
     * Get an enumeration with all matching resources.
     */
    static Enumeration<URL> getResources(String name) {
        ArrayList<URL> list = null;
        int numEntries = getBootClassPathSize();
        int i;

        for (i = 0; i < numEntries; i++) {
            String urlStr = getBootClassPathResource(name, i);
            if (urlStr != null) {
                if (list == null)
                    list = new ArrayList<URL>();

                try {
                    list.add(new URL(urlStr));
                }
                catch (MalformedURLException mue) {
                    mue.printStackTrace();
                    // unexpected; keep going
                }
            }
        }

        if (list == null)
            return null;
        else
            return new EnumerateListArray<URL>(list);
    }

    /**
     * Load class with bootstrap class loader.
     */
    native static Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException;

    native static Class getPrimitiveClass(char type);

    /*
     * TODO(Google) Ticket 156: Native implementation does nothing, just throws
     * OperationNotSupportedException.
     */
    native static Class defineClass(ClassLoader cl, String name,
        byte[] data, int offset, int len, ProtectionDomain pd)
        throws ClassFormatError;

    /*
     * TODO(Google) Ticket 156: Native implementation does nothing, just throws
     * OperationNotSupportedException.
     */
    native static Class defineClass(ClassLoader cl,
            byte[] data, int offset, int len, ProtectionDomain pd)
            throws ClassFormatError;

    native static Class findLoadedClass(ClassLoader cl, String name);

    /**
     * Boot class path manipulation, for getResources().
     */
    native private static int getBootClassPathSize();
    native private static String getBootClassPathResource(String name,
            int index);

    /*
     * Create an Enumeration for an ArrayList.
     */
    private static class EnumerateListArray<T> implements Enumeration<T> {
        private final ArrayList mList;
        private int i = 0;

        EnumerateListArray(ArrayList list) {
            mList = list;
        }

        public boolean hasMoreElements() {
            return i < mList.size();
        }

        public T nextElement() {
            if (i >= mList.size())
                throw new NoSuchElementException();
            return (T) mList.get(i++);
        }
    };
}

