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
 * Streams to be used with serialization to read objects must implement this
 * interface. ObjectInputStream is one example.
 * 
 * @see ObjectInputStream
 * @see ObjectOutput
 */
public interface ObjectInput extends DataInput {
    /**
     * Returns a int representing then number of bytes of primitive data that
     * are available.
     * 
     * @return int the number of primitive bytes available.
     * 
     * @throws IOException
     *             If an error occurs in this ObjectInput.
     */
    public int available() throws IOException;

    /**
     * Close this ObjectInput. Concrete implementations of this class should
     * free any resources during close.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this ObjectInput.
     */
    public void close() throws IOException;

    /**
     * Reads a single byte from this ObjectInput and returns the result as an
     * int. The low-order byte is returned or -1 of the end of stream was
     * encountered.
     * 
     * @return int The byte read or -1 if end of ObjectInput.
     * 
     * @throws IOException
     *             If the ObjectInput is already closed or another IOException
     *             occurs.
     */
    public int read() throws IOException;

    /**
     * Reads bytes from the <code>ObjectInput</code> and stores them in byte
     * array <code>buffer</code>. Blocks while waiting for input.
     * 
     * @param buffer
     *            the array in which to store the read bytes.
     * @return how many bytes were read or <code>-1</code> if encountered end
     *         of <code>ObjectInput</code>.
     * 
     * @throws IOException
     *             If the <code>ObjectInput</code> is already closed or
     *             another IOException occurs.
     */
    public int read(byte[] buffer) throws IOException;

    /**
     * Reads at most <code>count</code> bytes from the ObjectInput and stores
     * them in byte array <code>buffer</code> starting at offset
     * <code>count</code>. Answer the number of bytes actually read or -1 if
     * no bytes were read and end of ObjectInput was encountered.
     * 
     * @param buffer
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param count
     *            the maximum number of bytes to store in <code>buffer</code>.
     * @return the number of bytes actually read or -1 if end of ObjectInput.
     * 
     * @throws IOException
     *             If the ObjectInput is already closed or another IOException
     *             occurs.
     */
    public int read(byte[] buffer, int offset, int count) throws IOException;

    /**
     * Reads the next object from this ObjectInput.
     * 
     * @return the next object read from this ObjectInput
     * 
     * @throws IOException
     *             If an error occurs attempting to read from this ObjectInput.
     * @throws ClassNotFoundException
     *             If the object's class cannot be found
     */
    public Object readObject() throws ClassNotFoundException, IOException;

    /**
     * Skips <code>toSkip</code> number of bytes in this ObjectInput.
     * Subsequent <code>read()</code>'s will not return these bytes.
     * 
     * @param toSkip
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * 
     * @throws IOException
     *             If the ObjectInput is already closed or another IOException
     *             occurs.
     */
    public long skip(long toSkip) throws IOException;
}
