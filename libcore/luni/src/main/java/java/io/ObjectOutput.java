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
 * Streams to be used with serialization to write objects must implement this
 * interface. ObjectOutputStream is one example.
 * 
 * @see ObjectOutputStream
 * @see ObjectInput
 */
public interface ObjectOutput extends DataOutput {
    /**
     * Close this ObjectOutput. Concrete implementations of this class should
     * free any resources during close.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this ObjectOutput.
     */
    public void close() throws IOException;

    /**
     * Flush this ObjectOutput. Concrete implementations of this class should
     * ensure any pending writes are written out when this method is envoked.
     * 
     * @throws IOException
     *             If an error occurs attempting to flush this ObjectOutput.
     */
    public void flush() throws IOException;

    /**
     * Writes the entire contents of the byte array <code>buffer</code> to
     * this ObjectOutput.
     * 
     * @param buffer
     *            the buffer to be written
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to write to this ObjectOutput.
     */
    public void write(byte[] buffer) throws IOException;

    /**
     * Writes <code>count</code> <code>bytes</code> from this byte array
     * <code>buffer</code> starting at offset <code>index</code> to this
     * ObjectOutput.
     * 
     * @param buffer
     *            the buffer to be written
     * @param offset
     *            offset in buffer to get bytes
     * @param count
     *            number of bytes in buffer to write
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to write to this ObjectOutput.
     */
    public void write(byte[] buffer, int offset, int count) throws IOException;

    /**
     * Writes the specified int <code>value</code> to this ObjectOutput.
     * 
     * @param value
     *            the int to be written
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to write to this ObjectOutput.
     */
    public void write(int value) throws IOException;

    /**
     * Writes the specified object <code>obj</code> to this ObjectOutput.
     * 
     * @param obj
     *            the object to be written
     * 
     * @throws java.io.IOException
     *             If an error occurs attempting to write to this ObjectOutput.
     */
    public void writeObject(Object obj) throws IOException;
}
