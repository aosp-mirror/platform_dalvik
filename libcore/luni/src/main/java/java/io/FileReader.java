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
 * FileReader is class for turning a file into a character Stream. Data read
 * from the source is converted into characters. The encoding is assumed to
 * 8859_1. The FileReader contains a buffer of bytes read from the source and
 * converts these into characters as needed. The buffer size is 8K.
 * 
 * @see FileWriter
 */
public class FileReader extends InputStreamReader {

    /**
     * Construct a new FileReader on the given File <code>file</code>. If the
     * <code>file</code> specified cannot be found, throw a
     * FileNotFoundException.
     * 
     * @param file
     *            a File to be opened for reading characters from.
     * 
     * @throws FileNotFoundException
     *             if the file cannot be opened for reading.
     */
    public FileReader(File file) throws FileNotFoundException {
        super(new FileInputStream(file));
    }

    /**
     * Construct a new FileReader on the given FileDescriptor <code>fd</code>.
     * Since a previously opened FileDescriptor is passed as an argument, no
     * FileNotFoundException is thrown.
     * 
     * @param fd
     *            the previously opened file descriptor.
     */
    public FileReader(FileDescriptor fd) {
        super(new FileInputStream(fd));
    }

    /**
     * Construct a new FileReader on the given file named <code>filename</code>.
     * If the <code>filename</code> specified cannot be found, throw a
     * FileNotFoundException.
     * 
     * @param filename
     *            an absolute or relative path specifying the file to open.
     * 
     * @throws FileNotFoundException
     *             if the filename cannot be opened for reading.
     */
    public FileReader(String filename) throws FileNotFoundException {
        super(new FileInputStream(filename));
    }
}
