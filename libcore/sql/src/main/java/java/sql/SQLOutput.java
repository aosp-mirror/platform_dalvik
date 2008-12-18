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
 * The interface for an output stream used to write attributes of an SQL <i>User
 * Defined Type</i> (UDT) to the database. This interface is used for custom
 * mapping of types and is called by the JDBC driver. It is not intended to be
 * used by applications.
 * <p>
 * When an object which implements the {@code SQLData} interface is used as an
 * argument to an SQL statement, the JDBC driver calls the method {@code
 * SQLData.getSQLType} to establish the type of the SQL UDT that is being
 * passed. The driver then creates an {@code SQLOutput} stream and passes it to
 * the {@code SQLData.writeSQL} method, which in turn uses the appropriate
 * {@code SQLOutput} writer methods to write the data from the {@code SQLData}
 * object into the stream according to the defined mapping.
 * </p>
 * 
 * @see SQLData
 * 
 * @since Android 1.0
 */
public interface SQLOutput {

    /**
     * Write a {@code String} value into the output stream.
     * 
     * @param theString
     *            the {@code String} to write.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeString(String theString) throws SQLException;

    /**
     * Write a {@code boolean} value into the output stream.
     * 
     * @param theFlag
     *            the {@code boolean} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeBoolean(boolean theFlag) throws SQLException;

    /**
     * Write a {@code byte} value into the output stream.
     * 
     * @param theByte
     *            the {@code byte} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeByte(byte theByte) throws SQLException;

    /**
     * Write a {@code short} value into the output stream.
     * 
     * @param theShort
     *            the {@code short} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeShort(short theShort) throws SQLException;

    /**
     * Write an {@code int} value into the output stream.
     * 
     * @param theInt
     *            the {@code int} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeInt(int theInt) throws SQLException;

    /**
     * Write a {@code long} value into the output stream.
     * 
     * @param theLong
     *            the {@code long} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeLong(long theLong) throws SQLException;

    /**
     * Write a {@code float} value into the output stream.
     * 
     * @param theFloat
     *            the {@code float} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeFloat(float theFloat) throws SQLException;

    /**
     * Write a {@code double} value into the output stream.
     * 
     * @param theDouble
     *            the {@code double} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeDouble(double theDouble) throws SQLException;

    /**
     * Write a {@code java.math.BigDecimal} value into the output stream.
     * 
     * @param theBigDecimal
     *            the {@code BigDecimal} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeBigDecimal(BigDecimal theBigDecimal) throws SQLException;

    /**
     * Write an array of bytes into the output stream.
     * 
     * @param theBytes
     *            the array of bytes to write.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeBytes(byte[] theBytes) throws SQLException;

    /**
     * Write a {@code java.sql.Date} value into the output stream.
     * 
     * @param theDate
     *            the {@code Date} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @see Date
     * @since Android 1.0
     */
    public void writeDate(Date theDate) throws SQLException;

    /**
     * Write a {@code java.sql.Time} value into the output stream.
     * 
     * @param theTime
     *            the {@code Time} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @see Time
     * @since Android 1.0
     */
    public void writeTime(Time theTime) throws SQLException;

    /**
     * Write a {@code java.sql.Timestamp} value into the output stream.
     * 
     * @param theTimestamp
     *            the {@code Timestamp} value to write.
     * @throws SQLException
     *             if a database error occurs.
     * @see Timestamp
     * @since Android 1.0
     */
    public void writeTimestamp(Timestamp theTimestamp) throws SQLException;

    /**
     * Write a stream of unicode characters into the output stream.
     * 
     * @param theStream
     *            the stream of unicode characters to write, as a {@code
     *            java.io.Reader} object.
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeCharacterStream(Reader theStream) throws SQLException;

    /**
     * Write a stream of ASCII characters into the output stream.
     * 
     * @param theStream
     *            the stream of ASCII characters to write, as a {@code
     *            java.io.InputStream} object
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeAsciiStream(InputStream theStream) throws SQLException;

    /**
     * Write a stream of uninterpreted bytes into the output stream.
     * 
     * @param theStream
     *            the stream of bytes to write, as a {@code java.io.InputStream}
     *            object
     * @throws SQLException
     *             if a database error occurs.
     * @since Android 1.0
     */
    public void writeBinaryStream(InputStream theStream) throws SQLException;

    /**
     * Write an {@code SQLData} object into the output stream.
     * <p>
     * If the {@code SQLData} object is null, writes {@code NULL} to the stream.
     * </p>
     * <p>
     * Otherwise, calls the {@code SQLData.writeSQL} method of the object, which
     * writes the object's attributes to the stream by calling the appropriate
     * SQLOutput writer methods for each attribute, in order. The order of the
     * attributes is the order they are listed in the SQL definition of the User
     * Defined Type.
     * </p>
     * 
     * @param theObject
     *            the {@code SQLData} object to write.
     * @throws SQLException
     *             if a database error occurs.
     * @see SQLData
     * @since Android 1.0
     */
    public void writeObject(SQLData theObject) throws SQLException;

    /**
     * Write an SQL {@code Ref} value into the output stream.
     * 
     * @param theRef
     *            the {@code java.sql.Ref} object to write.
     * @throws SQLException
     *             if a database error occurs.
     * @see Ref
     * @since Android 1.0
     */
    public void writeRef(Ref theRef) throws SQLException;

    /**
     * Write an SQL {@code Blob} value into the output stream.
     * 
     * @param theBlob
     *            the {@code java.sql.Blob} object to write.
     * @throws SQLException
     *             if a database error occurs.
     * @see Blob
     * @since Android 1.0
     */
    public void writeBlob(Blob theBlob) throws SQLException;

    /**
     * Write an SQL {@code Clob} value into the output stream.
     * 
     * @param theClob
     *            the {@code java.sql.Clob} object to write.
     * @throws SQLException
     *             if a database error occurs.
     * @see Clob
     * @since Android 1.0
     */
    public void writeClob(Clob theClob) throws SQLException;

    /**
     * Write an SQL {@code Struct} value into the output stream.
     * 
     * @param theStruct
     *            the {@code java.sql.Struct} object to write.
     * @throws SQLException
     *             if a database error occurs.
     * @see Struct
     * @since Android 1.0
     */
    public void writeStruct(Struct theStruct) throws SQLException;

    /**
     * Write an SQL {@code Array} value into the output stream.
     * 
     * @param theArray
     *            the {@code java.sql.Array} object to write.
     * @throws SQLException
     *             if a database error occurs.
     * @see Array
     * @since Android 1.0
     */
    public void writeArray(Array theArray) throws SQLException;

    /**
     * Write an SQL {@code DATALINK} value into the output stream.
     * 
     * @param theURL
     *            the datalink value as a {@code java.net.URL} to write.
     * @throws SQLException
     *             if a database error occurs.
     * @see java.net.URL
     * @since Android 1.0
     */
    public void writeURL(URL theURL) throws SQLException;
}
