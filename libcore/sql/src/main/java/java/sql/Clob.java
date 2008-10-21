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

import java.io.InputStream;
import java.io.Reader;
import java.io.OutputStream;
import java.io.Writer;

/**
 * A Java interface mapping for the SQL CLOB type.
 * <p>
 * An SQL CLOB type stores a large array of characters as the value in a column
 * of a database.
 * <p>
 * The java.sql.Clob interface provides methods for setting and retrieving data
 * in the Clob, for querying Clob data length, for searching for data within the
 * Clob.
 */
public interface Clob {

    /**
     * Gets the value of this Clob object as an ASCII stream.
     * 
     * @return an ASCII InputStream giving access to the Clob data
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public InputStream getAsciiStream() throws SQLException;

    /**
     * Gets the value of this Clob object as a java.io.Reader.
     * 
     * @return a character stream Reader object giving access to the Clob data
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public Reader getCharacterStream() throws SQLException;

    /**
     * Gets a copy of a specified substring in this Clob.
     * 
     * @param pos
     *            the index of the start of the substring in the Clob
     * @param length
     *            the length of the data to retrieve
     * @return A String containing the requested data
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public String getSubString(long pos, int length) throws SQLException;

    /**
     * Retrieves the number of characters in this Clob object.
     * 
     * @return a long value with the number of character in this Clob.
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public long length() throws SQLException;

    /**
     * Retrieves the character position at which a specified Clob object appears
     * in this Clob object.
     * 
     * @param searchstr
     *            the specified Clob to search for
     * @param start
     *            the position within this Clob to start the search
     * @return a long value with the position at which the specified Clob occurs
     *         within this Clob.
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public long position(Clob searchstr, long start) throws SQLException;

    /**
     * Retrieves the character position at which a specified substring appears
     * in this Clob object.
     * 
     * @param searchstr
     *            th String to search for
     * @param start
     *            the position at which to start the search within this Clob.
     * @return a long value with the position at which the specified String
     *         occurs within this Clob.
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public long position(String searchstr, long start) throws SQLException;

    /**
     * Retrieves a stream which can be used to write Ascii characters to this
     * Clob object, starting at specified position.
     * 
     * @param pos
     *            the position at which to start the writing
     * @return an OutputStream which can be used to write ASCII characters to
     *         this Clob.
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public OutputStream setAsciiStream(long pos) throws SQLException;

    /**
     * Retrieves a stream which can be used to write a stream of Unicode
     * characters to this Clob object, at a specified position.
     * 
     * @param pos
     *            the position at which to start the writing
     * @return a Writer which can be used to write Unicode characters to this
     *         Clob.
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public Writer setCharacterStream(long pos) throws SQLException;

    /**
     * Writes a given Java String to this Clob object at a specified position.
     * 
     * @param pos
     *            the position at which to start the writing
     * @param str
     *            the String to write
     * @return the number of characters written
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public int setString(long pos, String str) throws SQLException;

    /**
     * Writes len characters of String, starting at a specified character
     * offset, to this Clob.
     * 
     * @param pos
     *            the position at which to start the writing
     * @param str
     *            the String to write
     * @param offset
     *            the offset within str to start writing from
     * @param len
     *            the number of characters to write
     * @return the number of characters written
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public int setString(long pos, String str, int offset, int len)
            throws SQLException;

    /**
     * Truncates this Clob to have a specified length of characters.
     * 
     * @param len
     *            the length in characters to truncate this Clob
     * @throws SQLException
     *             if an error occurs accessing the Clob
     */
    public void truncate(long len) throws SQLException;
}
