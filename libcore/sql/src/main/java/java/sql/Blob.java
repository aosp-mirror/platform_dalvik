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

package java.sql;

import java.io.OutputStream;
import java.io.InputStream;

/**
 * A Java interface mapping for the SQL BLOB type.
 * <p>
 * An SQL CLOB type stores a large array of bytes (binary data) as the value in
 * a column of a database.
 * <p>
 * The java.sql.Blob interface provides methods for setting and retrieving data
 * in the Blob, for querying Clob data length, for searching for data within the
 * Blob.
 */
public interface Blob {

    /**
     * Retrieves this Blob object as a binary stream.
     * 
     * @return a binary InputStream giving access to the Blob data
     * @throws SQLException
     *             if an error occurs accessing the Blob
     */
    public InputStream getBinaryStream() throws SQLException;

    /**
     * Gets a portion of the value of this Blob as an array of bytes.
     * 
     * @param pos
     *            the position of the first byte in the Blob to get, where the
     *            first byte in the Blob has position = 1
     * @param length
     *            the number of bytes to get
     * @return a byte array containing the data from the Blob, starting at pos
     *         and of length up to <code>length</code> bytes long
     * @throws SQLException
     *             if an error occurs accessing the Blob
     */
    public byte[] getBytes(long pos, int length) throws SQLException;

    /**
     * Gets the number of bytes in this Blob object.
     * 
     * @return an long value with the length of the Blob in bytes
     * @throws SQLException
     *             if an error occurs accessing the Blob
     */
    public long length() throws SQLException;

    /**
     * Search for the position in this Blob at which a specified pattern begins,
     * starting at a specified position within the Blob.
     * 
     * @param pattern
     *            a Blob containing the pattern of data to search for in this
     *            Blob
     * @param start
     *            the position within this Blob to start the search, where the
     *            first position in the Blob is 1
     * @return a long value with the position at which the pattern begins. -1 if
     *         the pattern is not found in this Blob.
     * @throws SQLException
     *             if an error occurs accessing the Blob
     */
    public long position(Blob pattern, long start) throws SQLException;

    /**
     * Search for the position in this Blob at which the specified pattern
     * begins, starting at a specified position within the Blob.
     * 
     * @param pattern
     *            a byte array containing the pattern of data to search for in
     *            this Blob
     * @param start
     *            the position within this Blob to start the search, where the
     *            first position in the Blob is 1
     * @return a long value with the position at which the pattern begins. -1 if
     *         the pattern is not found in this Blob.
     * @throws SQLException
     *             if an error occurs accessing the Blob
     */
    public long position(byte[] pattern, long start) throws SQLException;

    /**
     * Gets a stream that can be used to write binary data to this Blob.
     * 
     * @param pos
     *            the position within this Blob at which to start writing, where
     *            the first position in the Blob is 1
     * @return a binary InputStream which can be used to write data into the
     *         Blob starting at the specified position.
     * @throws SQLException
     *             if an error occurs accessing the Blob
     */
    public OutputStream setBinaryStream(long pos) throws SQLException;

    /**
     * Writes a specified array of bytes to this Blob. object, starting at a
     * specified position. Returns the number of bytes written.
     * 
     * @param pos
     *            the position within this Blob at which to start writing, where
     *            the first position in the Blob is 1
     * @param theBytes
     *            an array of bytes to write into the Blob
     * @return an integer containing the number of bytes written to the Blob
     * @throws SQLException
     *             if an error occurs accessing the Blob
     */
    public int setBytes(long pos, byte[] theBytes) throws SQLException;

    /**
     * Writes a portion of a specified byte array to this Blob. Returns the
     * number of bytes written.
     * 
     * @param pos
     *            the position within this Blob at which to start writing, where
     *            the first position in the Blob is 1
     * @param theBytes
     *            an array of bytes to write into the Blob
     * @param offset
     *            the offset into the byte array from which to start writing
     *            data - the first byte in the array has offset 0.
     * @param len
     *            the length of data to write, as the number of bytes
     * @return an integer containing the number of bytes written to the Blob
     * @throws SQLException
     *             if an error occurs accessing the Blob
     */
    public int setBytes(long pos, byte[] theBytes, int offset, int len)
            throws SQLException;

    /**
     * Truncate the value of this Blob object to a specified length in bytes.
     * 
     * @param len
     *            the length of data in bytes to truncate the value of this Blob
     * @throws SQLException
     *             if an error occurs accessing the Blob
     */
    public void truncate(long len) throws SQLException;
}
