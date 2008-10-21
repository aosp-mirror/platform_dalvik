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

// TODO Enable this again at a later point.
//import android.access.IPropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.SortedMap;

/**
 * Class that initialize Packer and Unpacker
 * 
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
	 * The method first read from system property for the classname of a Packer,
	 * if such property exists, the class shall be initialized; or the default
	 * Packer will be returned
	 * 
	 * @return a instance of Packer
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
	 * The method first read from system property for the classname of a
	 * Unpacker, if such property exists, the class shall be initialized; or the
	 * default Unpacker will be returned
	 * 
	 * @return a instance of Unpacker
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
	 * interface of Packer
	 * 
	 */
	public static interface Packer {

		/**
		 * the format of a class attribute name
		 */
		static final String CLASS_ATTRIBUTE_PFX = "pack.class.attribute."; //$NON-NLS-1$

		/**
		 * the format of a code attribute name
		 */
		static final String CODE_ATTRIBUTE_PFX = "pack.code.attribute."; //$NON-NLS-1$

		/**
		 * the deflation hint to set in the output archive
		 */
		static final String DEFLATE_HINT = "pack.deflate.hint";//$NON-NLS-1$

		/**
		 * the indicated amount of effort to use in compressing the archive.
		 */
		static final String EFFORT = "pack.effort";//$NON-NLS-1$

		/**
		 * a String of error
		 */
		static final String ERROR = "error";//$NON-NLS-1$

		/**
		 * a String of false
		 */
		static final String FALSE = "false";//$NON-NLS-1$

		/**
		 * the format of a field attribute name
		 */
		static final String FIELD_ATTRIBUTE_PFX = "pack.field.attribute.";//$NON-NLS-1$

		/**
		 * a String of keep
		 */
		static final String KEEP = "keep";//$NON-NLS-1$

		/**
		 * decide if all elements shall transmit in their original order
		 */
		static final String KEEP_FILE_ORDER = "pack.keep.file.order";//$NON-NLS-1$

		/**
		 * a String of latest
		 */
		static final String LATEST = "latest";//$NON-NLS-1$

		/**
		 * the format of a method attribute name
		 */
		static final String METHOD_ATTRIBUTE_PFX = "pack.method.attribute.";//$NON-NLS-1$

		/**
		 * Packer shall attempt to determine the latest modification time if
		 * this is set to LASTEST
		 */
		static final String MODIFICATION_TIME = "pack.modification.time";//$NON-NLS-1$

		/**
		 * a String of pass
		 */
		static final String PASS = "pass";//$NON-NLS-1$

		/**
		 * the file that will not be compressed.
		 */
		static final String PASS_FILE_PFX = "pack.pass.file.";//$NON-NLS-1$

		/**
		 * packer progress as a percentage
		 */
		static final String PROGRESS = "pack.progress";//$NON-NLS-1$

		/**
		 * The number of bytes of each archive segment.
		 */
		static final String SEGMENT_LIMIT = "pack.segment.limit";//$NON-NLS-1$

		/**
		 * a String of strip
		 */
		static final String STRIP = "strip";//$NON-NLS-1$

		/**
		 * a String of true
		 */
		static final String TRUE = "true";//$NON-NLS-1$

		/**
		 * the action to take if an unknown attribute is encountered.
		 */
		static final String UNKNOWN_ATTRIBUTE = "pack.unknown.attribute";//$NON-NLS-1$

		/**
		 * 
		 * @return the properties of packer
		 */
		SortedMap<String, String> properties();

		/**
		 * Pack jarfile with pack arithmetic
		 * 
		 * @param in
		 *            jarfile to be compact
		 * @param out
		 *            stream of compact data
		 * @throws IOException
		 *             if I/O exception occurs
		 */
		void pack(JarFile in, OutputStream out) throws IOException;

		/**
		 * Pack jarStream with pack arithmetic
		 * 
		 * @param in
		 *            stream of uncompact jar data
		 * @param out
		 *            stream of compact data
		 * @throws IOException
		 *             if I/O exception occurs
		 */
		void pack(JarInputStream in, OutputStream out) throws IOException;

		/**
		 * add a listener for PropertyChange events
		 * 
		 * @param listener
		 *            the listener to listen if PropertyChange events occurs
		 */
        // TODO Enable this again at a later point.
        //void addPropertyChangeListener(IPropertyChangeListener listener);

		/**
		 * remove a listener
		 * 
		 * @param listener
		 *            listener to remove
		 */
        // TODO Enable this again at a later point.
        //void removePropertyChangeListener(IPropertyChangeListener listener);
	}

	/**
	 * interface of unpacker
	 * 
	 */
	public static interface Unpacker {

		/**
		 * The String indicating if the unpacker should ignore all transmitted
		 * values,can be replaced by either true or false
		 */
		static final String DEFLATE_HINT = "unpack.deflate.hint";//$NON-NLS-1$

		/**
		 * a String of false
		 */
		static final String FALSE = "false";//$NON-NLS-1$

		/**
		 * a String of keep
		 */
		static final String KEEP = "keep";//$NON-NLS-1$

		/**
		 * the progress as a percentage
		 */
		static final String PROGRESS = "unpack.progress";//$NON-NLS-1$

		/**
		 * a String of true
		 */
		static final String TRUE = "true";//$NON-NLS-1$

		/**
		 * 
		 * @return the properties of unpacker
		 */
		SortedMap<String, String> properties();

		/**
		 * unpack stream into jarfile with pack arithmetic
		 * 
		 * @param in
		 *            stream to uncompact
		 * @param out
		 *            jarstream of uncompact data
		 * @throws IOException
		 *             if I/O exception occurs
		 */
		void unpack(InputStream in, JarOutputStream out) throws IOException;

		/**
		 * unpack File into jarfile with pack arithmetic
		 * 
		 * @param in
		 *            file to be uncompact
		 * @param out
		 *            jarstream of uncompact data
		 * @throws IOException
		 *             if I/O exception occurs
		 */
		void unpack(File in, JarOutputStream out) throws IOException;

		/**
		 * add a listener for PropertyChange events
		 * 
		 * @param listener
		 *            the listener to listen if PropertyChange events occurs
		 */
        // TODO Enable this again at a later point.
        //void addPropertyChangeListener(IPropertyChangeListener listener);

		/**
		 * remove a listener
		 * 
		 * @param listener
		 *            listener to remove
		 */
        // TODO Enable this again at a later point.
        //void removePropertyChangeListener(IPropertyChangeListener listener);
	}

}
