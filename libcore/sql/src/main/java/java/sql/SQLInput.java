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

import java.math.BigDecimal;
import java.io.Reader;
import java.io.InputStream;
import java.net.URL;

/**
 * The {@code SQLInput} interface defines operations which apply to a type of
 * input stream which carries a series of values representing an instance of
 * an SQL structured type or SQL distinct type.
 * <p>
 * This interface is used to define custom mappings of SQL <i>User Defined
 * Types</i> (UDTs) to Java classes. It is used by JDBC drivers, therefore
 * application programmers do not normally use the {@code SQLInput} methods
 * directly. Reader methods such as {@code readLong} and {@code readBytes}
 * provide means to read values from an {@code SQLInput} stream.
 * </p><p>
 * When the {@code getObject} method is called with an object which implements
 * the {@code SQLData} interface, the JDBC driver determines the SQL type of the
 * UDT being mapped by calling the {@code SQLData.getSQLType} method. The driver
 * creates an instance of an {@code SQLInput} stream, filling the stream with
 * the attributes of the UDT. The {@code SQLInput} stream is passed to the
 * {@code SQLData.readSQL} method which then calls the {@code SQLInput} reader
 * methods to read the attributes.
 * </p>
 * 
 * @see SQLData
 * 
 * @since Android 1.0
 */
public interface SQLInput {

    /**
     * Returns the next attribute in the stream in the form of a {@code String}.
     * 
     * @return the next attribute. {@code null} if the value is SQL {@code NULL}.
     * 
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public String readString() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code boolean}
     * .
     * 
     * @return the next attribute as a {@code boolean}. {@code false} if the
     *         value is SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public boolean readBoolean() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code byte}.
     * 
     * @return the next attribute as a {@code byte}. 0 if the value is SQL
     *         {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public byte readByte() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code short}.
     * 
     * @return the next attribute as a {@code short}. 0 if the value is SQL
     *         {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public short readShort() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of an {@code int}.
     * 
     * @return the next attribute as an {@code int}. 0 if the value is SQL
     *         {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public int readInt() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code long}.
     * 
     * @return the next attribute as a {@code long}. 0 if the value is SQL
     *         {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public long readLong() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code float}.
     * 
     * @return the next attribute as a {@code float}. 0 if the value is SQL
     *         {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public float readFloat() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code double}.
     * 
     * @return the next attribute as a {@code double}. 0 if the value is SQL
     *         {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public double readDouble() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code
     * java.math.BigDecimal}.
     * 
     * @return the attribute as a {@code java.math.BigDecimal}. {@code null} if
     *         the read returns SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see java.math.BigDecimal
     * @since Android 1.0
     */
    public BigDecimal readBigDecimal() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a byte array.
     * 
     * @return the attribute as a byte array. {@code null} if the read returns
     *         SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public byte[] readBytes() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code
     * java.sql.Date}.
     * 
     * @return the next attribute as a {@code java.sql.Date}. {@code null} if
     *         the value is SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see Date
     * @since Android 1.0
     */
    public Date readDate() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code
     * java.sql.Time}.
     * 
     * @return the attribute as a {@code java.sql.Time}. {@code null} if the
     *         read returns SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see Time
     * @since Android 1.0
     */
    public Time readTime() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code
     * java.sql.Timestamp}.
     * 
     * @return the attribute as a {@code java.sql.Timestamp}. {@code null} if
     *         the read returns SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see Timestamp
     * @since Android 1.0
     */
    public Timestamp readTimestamp() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a Unicode
     * character stream embodied as a {@code java.io.Reader}.
     * 
     * @return the next attribute as a {@code java.io.Reader}. {@code null} if
     *         the value is SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see java.io.Reader
     * @since Android 1.0
     */
    public Reader readCharacterStream() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of an ASCII
     * character stream embodied as a {@code java.io.InputStream}.
     * 
     * @return the next attribute as a {@code java.io.InputStream}. {@code null}
     *         if the value is SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see java.io.InputStream
     * @since Android 1.0
     */
    public InputStream readAsciiStream() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a stream of bytes
     * embodied as a {@code java.io.InputStream}.
     * 
     * @return the next attribute as a {@code java.io.InputStream}. {@code null}
     *         if the value is SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see java.io.InputStream
     * @since Android 1.0
     */
    public InputStream readBinaryStream() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code
     * java.lang.Object}.
     * <p>
     * The type of the {@code Object} returned is determined by the type mapping
     * for this JDBC driver, including any customized mappings, if present. A
     * type map is given to the {@code SQLInput} by the JDBC driver before the
     * {@code SQLInput} is given to the application.
     * </p>
     * <p>
     * If the attribute is an SQL structured or distinct type, its SQL type is
     * determined. If the stream's type map contains an element for that SQL
     * type, the driver creates an object for the relevant type and invokes the
     * method {@code SQLData.readSQL} on it, which reads supplementary data from
     * the stream using whichever protocol is defined for that method.
     * </p>
     * 
     * @return the next attribute as an Object. {@code null} if the value is SQL
     *         {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public Object readObject() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code
     * java.sql.Ref}.
     * 
     * @return the next attribute as a {@code java.sql.Ref}. {@code null} if the
     *         value is SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see Ref
     * @since Android 1.0
     */
    public Ref readRef() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code
     * java.sql.Blob}.
     * 
     * @return the next attribute as a {@code java.sql.Blob}. {@code null} if
     *         the value is SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public Blob readBlob() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code
     * java.sql.Clob}.
     * 
     * @return the next attribute as a {@code java.sql.Clob}. {@code null} if
     *         the value is SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see Clob
     * @since Android 1.0
     */
    public Clob readClob() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a {@code
     * java.sql.Array}.
     * 
     * @return the next attribute as an {@code Array}. {@code null} if the value
     *         is SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see Array
     * @since Android 1.0
     */
    public Array readArray() throws SQLException;

    /**
     * Reports whether the last value read was SQL {@code NULL}.
     * 
     * @return {@code true} if the last value read was SQL {@code NULL}, {@code
     *         false} otherwise.
     * @throws SQLException
     *             if there is a database error.
     * @since Android 1.0
     */
    public boolean wasNull() throws SQLException;

    /**
     * Reads the next attribute in the stream (SQL DATALINK value) and returns
     * it as a {@code java.net.URL} object.
     * 
     * @return the next attribute as a {@code java.net.URL}. {@code null} if the
     *         value is SQL {@code NULL}.
     * @throws SQLException
     *             if there is a database error.
     * @see java.net.URL
     * @since Android 1.0
     */
    public URL readURL() throws SQLException;
}
