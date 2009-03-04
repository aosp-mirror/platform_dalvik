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
package java.util.jar;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.SortedMap;

/**
 * Class factory for {@link Pack200.Packer} and {@link Pack200.Unpacker}.
 * 
 * @since Android 1.0
 */
public abstract class Pack200 {

    private static final String SYSTEM_PROPERTY_PACKER = "java.util.jar.Pack200.Packer"; //$NON-NLS-1$

    private static final String SYSTEM_PROPERTY_UNPACKER = "java.util.jar.Pack200.Unpacker"; //$NON-NLS-1$

    /**
     * Prevent this class from being instantiated.
     */
    private Pack200(){
        //do nothing
    }

    /**
     * Returns a new instance of a packer engine.
     * <p>
     * The implementation of the packer engine is defined by the system property
     * {@code 'java.util.jar.Pack200.Packer'}. If this system property is
     * defined an instance of the specified class is returned, otherwise the
     * system's default implementation is returned.
     * </p>
     * 
     * @return an instance of {@code Packer}
     * @since Android 1.0
     */
    public static Pack200.Packer newPacker() {
        return (Packer) AccessController
                .doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        String className = System
                                .getProperty(SYSTEM_PROPERTY_PACKER,
                                        "org.apache.harmony.archive.internal.pack200.Pack200PackerAdapter"); //$NON-NLS-1$
                        try {
                            // TODO Not sure if this will cause problems with
                            // loading the packer
                            return ClassLoader.getSystemClassLoader()
                                    .loadClass(className).newInstance();
                        } catch (Exception e) {
                            throw new Error("Can't load class " + className, e);
                        }
                    }
                });

    }

    /**
     * Returns a new instance of a unpacker engine.
     * <p>
     * The implementation of the unpacker engine is defined by the system
     * property {@code 'java.util.jar.Pack200.Unpacker'}. If this system
     * property is defined an instance of the specified class is returned,
     * otherwise the system's default implementation is returned.
     * </p>
     * 
     * @return a instance of {@code Unpacker}.
     * @since Android 1.0
     */
    public static Pack200.Unpacker newUnpacker() {
        return (Unpacker) AccessController
                .doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        String className = System
                                .getProperty(SYSTEM_PROPERTY_UNPACKER,
                                        "org.apache.harmony.archive.internal.pack200.Pack200UnpackerAdapter");//$NON-NLS-1$
                        try {
                            return ClassLoader.getSystemClassLoader()
                                    .loadClass(className).newInstance();
                        } catch (Exception e) {
                            throw new Error("Can't load class " + className, e);
                        }
                    }
                });
    }

    /**
     * The interface defining the API for converting a JAR file to an output
     * stream in the Pack200 format.
     * 
     * @since Android 1.0
     */
    public static interface Packer {

        /**
         * the format of a class attribute name.
         * 
         * @since Android 1.0
         */
        static final String CLASS_ATTRIBUTE_PFX = "pack.class.attribute."; //$NON-NLS-1$

        /**
         * the format of a code attribute name.
         * 
         * @since Android 1.0
         */
        static final String CODE_ATTRIBUTE_PFX = "pack.code.attribute."; //$NON-NLS-1$

        /**
         * the deflation hint to set in the output archive.
         * 
         * @since Android 1.0
         */
        static final String DEFLATE_HINT = "pack.deflate.hint";//$NON-NLS-1$

        /**
         * the indicated amount of effort to use in compressing the archive.
         * 
         * @since Android 1.0
         */
        static final String EFFORT = "pack.effort";//$NON-NLS-1$

        /**
         * a String representation for {@code error}.
         * 
         * @since Android 1.0
         */
        static final String ERROR = "error";//$NON-NLS-1$

        /**
         * a String representation of {@code false}.
         * 
         * @since Android 1.0
         */
        static final String FALSE = "false";//$NON-NLS-1$

        /**
         * the format of a field attribute name.
         * 
         * @since Android 1.0
         */
        static final String FIELD_ATTRIBUTE_PFX = "pack.field.attribute.";//$NON-NLS-1$

        /**
         * a String representation for {@code keep}.
         * 
         * @since Android 1.0
         */
        static final String KEEP = "keep";//$NON-NLS-1$

        /**
         * decide if all elements shall transmit in their original order.
         * 
         * @since Android 1.0
         */
        static final String KEEP_FILE_ORDER = "pack.keep.file.order";//$NON-NLS-1$

        /**
         * a String representation for {@code latest}.
         * 
         * @since Android 1.0
         */
        static final String LATEST = "latest";//$NON-NLS-1$

        /**
         * the format of a method attribute name.
         * 
         * @since Android 1.0
         */
        static final String METHOD_ATTRIBUTE_PFX = "pack.method.attribute.";//$NON-NLS-1$

        /**
         * if it shall attempt to determine the latest modification time if this
         * is set to {@code LATEST}.
         * 
         * @since Android 1.0
         */
        static final String MODIFICATION_TIME = "pack.modification.time";//$NON-NLS-1$

        /**
         * a String representation of {@code pass}.
         * 
         * @since Android 1.0
         */
        static final String PASS = "pass";//$NON-NLS-1$

        /**
         * the file that will not be compressed.
         * 
         * @since Android 1.0
         */
        static final String PASS_FILE_PFX = "pack.pass.file.";//$NON-NLS-1$

        /**
         * packer progress as a percentage.
         * 
         * @since Android 1.0
         */
        static final String PROGRESS = "pack.progress";//$NON-NLS-1$

        /**
         * The number of bytes of each archive segment.
         * 
         * @since Android 1.0
         */
        static final String SEGMENT_LIMIT = "pack.segment.limit";//$NON-NLS-1$

        /**
         * a String representation of {@code strip}.
         * 
         * @since Android 1.0
         */
        static final String STRIP = "strip";//$NON-NLS-1$

        /**
         * a String representation of {@code true}.
         * 
         * @since Android 1.0
         */
        static final String TRUE = "true";//$NON-NLS-1$

        /**
         * the action to take if an unknown attribute is encountered.
         * 
         * @since Android 1.0
         */
        static final String UNKNOWN_ATTRIBUTE = "pack.unknown.attribute";//$NON-NLS-1$

        /**
         * Returns a sorted map of the properties of this packer.
         * 
         * @return the properties of the packer.
         * @since Android 1.0
         */
        SortedMap<String, String> properties();

        /**
         * Pack the specified JAR file to the specified output stream.
         * 
         * @param in
         *            JAR file to be compressed.
         * @param out
         *            stream of compressed data.
         * @throws IOException
         *             if I/O exception occurs.
         * @since Android 1.0
         */
        void pack(JarFile in, OutputStream out) throws IOException;

        /**
         * Pack the data from the specified jar input stream to the specified
         * output stream.
         * 
         * @param in
         *            stream of uncompressed JAR data.
         * @param out
         *            stream of compressed data.
         * @throws IOException
         *             if I/O exception occurs.
         * @since Android 1.0
         */
        void pack(JarInputStream in, OutputStream out) throws IOException;

        /**
         * add a listener for PropertyChange events
         * 
         * @param listener
         *            the listener to listen if PropertyChange events occurs
         */
        void addPropertyChangeListener(PropertyChangeListener listener);

        /**
         * remove a listener
         * 
         * @param listener
         *            listener to remove
         */
        void removePropertyChangeListener(PropertyChangeListener listener);
    }

    /**
     * The interface defining the API for converting a packed stream in the
     * Pack200 format to a JAR file.
     * 
     * @since Android 1.0
     */
    public static interface Unpacker {

        /**
         * The String indicating if the unpacker should ignore all transmitted
         * values,can be replaced by either {@code true} or {@code false}.
         * 
         * @since Android 1.0
         */
        static final String DEFLATE_HINT = "unpack.deflate.hint";//$NON-NLS-1$

        /**
         * a String representation of {@code false}.
         * 
         * @since Android 1.0
         */
        static final String FALSE = "false";//$NON-NLS-1$

        /**
         * a String representation of {@code keep}.
         * 
         * @since Android 1.0
         */
        static final String KEEP = "keep";//$NON-NLS-1$

        /**
         * the progress as a {@code percentage}.
         * 
         * @since Android 1.0
         */
        static final String PROGRESS = "unpack.progress";//$NON-NLS-1$

        /**
         * a String representation of {@code true}.
         * 
         * @since Android 1.0
         */
        static final String TRUE = "true";//$NON-NLS-1$

        /**
         * Returns a sorted map of the properties of this unpacker.
         * 
         * @return the properties of unpacker.
         * @since Android 1.0
         */
        SortedMap<String, String> properties();

        /**
         * Unpack the specified stream to the specified JAR output stream.
         * 
         * @param in
         *            stream to uncompressed.
         * @param out
         *            JAR output stream of uncompressed data.
         * @throws IOException
         *             if I/O exception occurs.
         * @since Android 1.0
         */
        void unpack(InputStream in, JarOutputStream out) throws IOException;

        /**
         * Unpack the contents of the specified {@code File} to the specified
         * JAR output stream.
         * 
         * @param in
         *            file to be uncompressed.
         * @param out
         *            JAR output stream of uncompressed data.
         * @throws IOException
         *             if I/O exception occurs.
         * @since Android 1.0
         */
        void unpack(File in, JarOutputStream out) throws IOException;

        /**
         * add a listener for {@code PropertyChange} events.
         * 
         * @param listener
         *            the listener to listen if {@code PropertyChange} events
         *            occurs.
         */
        void addPropertyChangeListener(PropertyChangeListener listener);

        /**
         * remove a listener.
         * 
         * @param listener
         *            listener to remove.
         */
        void removePropertyChangeListener(PropertyChangeListener listener);
    }

}
