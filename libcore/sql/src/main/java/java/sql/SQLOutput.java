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
import java.math.BigDecimal;
import java.net.URL;

/**
 * The interface for an output stream used to write attributes of an SQL User
 * Defined Type to the database. This interface is used for custom mapping of
 * types and is called by the JDBC driver. It is not expected that this
 * interface is used by applications.
 * <p>
 * When an object which implements the SQLData interface is used as an argument
 * to an SQL statement, the JDBC driver calls the method
 * <code>SQLData.getSQLType</code> to establish the type of the SQL UDT that
 * is being passed. The driver then creates an SQLOutput stream and passes it to
 * the <code>SQLData.writeSQL</code> method, which in turn uses the
 * appropriate SQLOutput writer methods to write the data from the SQLData
 * object into the stream according to the defined mapping.
 */
public interface SQLOutput {

    /**
     * Write a String value into the output stream.
     * 
     * @param theString
     *            the String to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeString(String theString) throws SQLException;

    /**
     * Write a boolean value into the output stream.
     * 
     * @param theFlag
     *            the boolean value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeBoolean(boolean theFlag) throws SQLException;

    /**
     * Write a byte value into the output stream.
     * 
     * @param theByte
     *            the byte value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeByte(byte theByte) throws SQLException;

    /**
     * Write a short value into the output stream.
     * 
     * @param theShort
     *            the short value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeShort(short theShort) throws SQLException;

    /**
     * Write an int value into the output stream.
     * 
     * @param theInt
     *            the int value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeInt(int theInt) throws SQLException;

    /**
     * Write a long value into the output stream.
     * 
     * @param theLong
     *            the long value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeLong(long theLong) throws SQLException;

    /**
     * Write a float value into the output stream.
     * 
     * @param theFloat
     *            the float value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeFloat(float theFloat) throws SQLException;

    /**
     * Write a double value into the output stream.
     * 
     * @param theDouble
     *            the double value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeDouble(double theDouble) throws SQLException;

    /**
     * Write a java.math.BigDecimal value into the output stream.
     * 
     * @param theBigDecimal
     *            the BigDecimal value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeBigDecimal(BigDecimal theBigDecimal) throws SQLException;

    /**
     * Write an array of bytes into the output stream.
     * 
     * @param theBytes
     *            the array of bytes to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeBytes(byte[] theBytes) throws SQLException;

    /**
     * Write a java.sql.Date value into the output stream.
     * 
     * @param theDate
     *            the Date value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeDate(Date theDate) throws SQLException;

    /**
     * Write a java.sql.Time value into the output stream.
     * 
     * @param theTime
     *            the Time value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeTime(Time theTime) throws SQLException;

    /**
     * Write a java.sql.Timestamp value into the output stream.
     * 
     * @param theTimestamp
     *            the Timestamp value to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeTimestamp(Timestamp theTimestamp) throws SQLException;

    /**
     * Write a stream of Unicode characters into the output stream.
     * 
     * @param theStream
     *            the stream of Unicode characters to write, as a java.io.Reader
     *            object
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeCharacterStream(Reader theStream) throws SQLException;

    /**
     * Write a stream of ASCII characters into the output stream.
     * 
     * @param theStream
     *            the stream of ASCII characters to write, as a
     *            java.io.InputStream object
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeAsciiStream(InputStream theStream) throws SQLException;

    /**
     * Write a stream of uninterpreted bytes into the output stream.
     * 
     * @param theStream
     *            the stream of bytes to write, as a java.io.InputStream object
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeBinaryStream(InputStream theStream) throws SQLException;

    /**
     * Write an SQLData object into the output stream.
     * <p>
     * If the SQLData object is null, writes SQL NULL to the stream.
     * <p>
     * Otherwise, calls the <code>SQLData.writeSQL</code> method of the
     * object, which writes the object's attributes to the stream by calling the
     * appropriate SQLOutput writer methods for each attribute, in order. The
     * order of the attributes is the order they are listed in the SQL
     * definition of the User Defined Type.
     * 
     * @param theObject
     *            the SQLData object to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeObject(SQLData theObject) throws SQLException;

    /**
     * Write an SQL Ref value into the output stream.
     * 
     * @param theRef
     *            the java.sql.Ref object to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeRef(Ref theRef) throws SQLException;

    /**
     * Write an SQL Blob value into the output stream.
     * 
     * @param theBlob
     *            the java.sql.Blob object to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeBlob(Blob theBlob) throws SQLException;

    /**
     * Write an SQL Clob value into the output stream.
     * 
     * @param theClob
     *            the java.sql.Clob object to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeClob(Clob theClob) throws SQLException;

    /**
     * Write an SQL Struct value into the output stream.
     * 
     * @param theStruct
     *            the java.sql.Struct object to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeStruct(Struct theStruct) throws SQLException;

    /**
     * Write an SQL Array value into the output stream.
     * 
     * @param theArray
     *            the java.sql.Array object to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeArray(Array theArray) throws SQLException;

    /**
     * Write an SQL DATALINK value into the output stream.
     * 
     * @param theURL
     *            the Datalink value as a java.net.URL to write
     * @throws SQLException
     *             if a database error occurs
     */
    public void writeURL(URL theURL) throws SQLException;
}
