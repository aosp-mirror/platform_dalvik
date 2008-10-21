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

/**
 * FileWriter is a class for writing characters out to a file. The default
 * character encoding, 8859_1 is currently used to convert characters to bytes
 * in the file.
 * 
 * @see FileReader
 */
public class FileWriter extends OutputStreamWriter {

    /**
     * Creates a FileWriter using the File <code>file</code>.
     * 
     * @param file
     *            the non-null File to write bytes to.
     * 
     * @throws IOException
     *             If the given file is not found
     */
    public FileWriter(File file) throws IOException {
        super(new FileOutputStream(file));
    }

    /**
     * Creates a FileWriter using the File <code>file</code>. The parameter
     * <code>append</code> determines whether or not the file is opened and
     * appended to or just opened empty.
     * 
     * @param file
     *            the non-null File to write bytes to.
     * @param append
     *            should the file be appened to or opened empty.
     * 
     * @throws IOException
     *             If the given file is not found
     */
    public FileWriter(File file, boolean append) throws IOException {
        super(new FileOutputStream(file, append));
    }

    /**
     * Creates a FileWriter using the existing FileDescriptor <code>fd</code>.
     * 
     * @param fd
     *            the non-null FileDescriptor to write bytes to.
     */
    public FileWriter(FileDescriptor fd) {
        super(new FileOutputStream(fd));
    }

    /**
     * Creates a FileWriter using the platform dependent <code>filename</code>.
     * See the class description for how characters are converted to bytes.
     * 
     * @param filename
     *            the non-null name of the file to write bytes to.
     * 
     * @throws IOException
     *             If the given file is not found
     */
    public FileWriter(String filename) throws IOException {
        super(new FileOutputStream(new File(filename)));
    }

    /**
     * Creates a FileWriter using the platform dependent <code>filename</code>.
     * See the class description for how characters are converted to bytes. The
     * parameter <code>append</code> determines whether or not the file is
     * opened and appended to or just opened empty.
     * 
     * @param filename
     *            the non-null name of the file to write bytes to.
     * @param append
     *            should the file be appened to or opened empty.
     * 
     * @throws IOException
     *             If the given file is not found
     */
    public FileWriter(String filename, boolean append) throws IOException {
        super(new FileOutputStream(filename, append));
    }
}
