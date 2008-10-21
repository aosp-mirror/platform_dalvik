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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The JarOutputStream is used to output data in JarFile format.
 */
public class JarOutputStream extends ZipOutputStream {

    private Manifest manifest;

    /**
     * Constructs a new JarOutputStream using os as the underlying stream.
     * Manifest information for the JarFile to be written is obtained from the
     * parameter Manifest, mf.
     * 
     * @param os
     *            The OutputStream to write to
     * @param mf
     *            The Manifest to output for this Jar.
     * @exception IOException
     *                If an error occurs creating the JarOutputStream
     */
    public JarOutputStream(OutputStream os, Manifest mf) throws IOException {
        super(os);
        if (mf == null) {
            throw new NullPointerException();
        }
        manifest = mf;
        ZipEntry ze = new ZipEntry(JarFile.MANIFEST_NAME);
        putNextEntry(ze);
        manifest.write(this);
        closeEntry();
    }

    /**
     * Constructs a new JarOutputStream using os as the underlying stream.
     * 
     * @param os
     *            The OutputStream to write to
     * @exception IOException
     *                If an error occurs creating the JarOutputStream
     */
    @SuppressWarnings("unused")
    public JarOutputStream(OutputStream os) throws IOException {
        super(os);
    }

    /**
     * Writes the specified entry to the underlying stream. The previous entry
     * is closed if it is still open.
     * 
     * 
     * @param ze
     *            The ZipEntry to write
     * @exception IOException
     *                If an error occurs writing the entry
     */
    @Override
    public void putNextEntry(ZipEntry ze) throws IOException {
        super.putNextEntry(ze);
    }
}
