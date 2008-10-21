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
 * The SQLInput interface defines operations which apply to a type of input
 * stream which carries a series of values which represent an instance of an SQL
 * structured type or SQL distinct type.
 * <p>
 * SQLInput interface is used for custom mapping of SQL User Defined Types
 * (UDTs)to Java classes. It is used by JDBC drivers below the level of the
 * public interfaces and application programs do not normally use the SQLInput
 * methods directly. Reader methods such as readLong and readBytes provide means
 * to read values from an SQLInput stream.
 * <p>
 * When the getObject method is called with an object which implements the
 * SQLData interface, the JDBC driver determines the SQL type of the UDT being
 * mapped by calling the SQLData.getSQLType method. The driver creates an
 * instance of an SQLInput stream, filling the stream with the attributes of the
 * UDT. The SQLInput stream is passed to the SQLData.readSQL method which then
 * calls the SQLInput reader methods to read the attributes.
 */
public interface SQLInput {

    /**
     * Returns the next attribute in the stream in the form of a String.
     * 
     * @return the next attribute as a String. null if the value is SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public String readString() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a boolean.
     * 
     * @return the next attribute as a boolean. false if the value is SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public boolean readBoolean() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a byte.
     * 
     * @return the next attribute as a byte. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public byte readByte() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a short.
     * 
     * @return the next attribute as a short. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public short readShort() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of an int.
     * 
     * @return the next attribute as an int. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public int readInt() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a long.
     * 
     * @return the next attribute as a long. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public long readLong() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a float.
     * 
     * @return the next attribute as a float. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public float readFloat() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a double.
     * 
     * @return the next attribute as a double. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public double readDouble() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a
     * java.math.BigDecimal.
     * 
     * @return the attribute as a java.math.BigDecimal. null if the read returns
     *         SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public BigDecimal readBigDecimal() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a byte array.
     * 
     * @return the attribute as a byte array. null if the read returns SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public byte[] readBytes() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a java.sql.Date.
     * 
     * @return the next attribute as a java.sql.Date. null if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public Date readDate() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a java.sql.Time.
     * 
     * @return the attribute as a java.sql.Time. null if the read returns SQL
     *         NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public Time readTime() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a
     * java.sql.Timestamp.
     * 
     * @return the attribute as a java.sql.Timestamp. null if the read returns
     *         SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public Timestamp readTimestamp() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a Unicode
     * character stream embodied as a java.io.Reader.
     * 
     * @return the next attribute as a java.io.Reader. null if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public Reader readCharacterStream() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of an ASCII
     * character stream embodied as a java.io.InputStream.
     * 
     * @return the next attribute as a java.io.InputStream. null if the value is
     *         SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public InputStream readAsciiStream() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a stream of bytes
     * embodied as a java.io.InputStream.
     * 
     * @return the next attribute as a java.io.InputStream. null if the value is
     *         SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public InputStream readBinaryStream() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a
     * java.lang.Object.
     * <p>
     * The type of the Object returned is determined by the type mapping for
     * this JDBC driver, including any customized mappings in force. A type map
     * is given to the SQLInput by the JDBC driver before the SQLInput is given
     * to the application.
     * <p>
     * If the attribute is an SQL structured or distinct type, its SQL type is
     * determined. If the streams type map contains an element for that SQL
     * type, the driver creates an object of relevant type and invokes the
     * method SQLData.readSQL on it, which reads supplementary data from the
     * stream using whichever protocol is defined for that method.
     * 
     * @return the next attribute as an Object. null if the value is SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public Object readObject() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a java.sql.Ref.
     * 
     * @return the next attribute as a java.sql.Ref. null if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public Ref readRef() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a java.sql.Blob.
     * 
     * @return the next attribute as a java.sql.Blob. null if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public Blob readBlob() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a java.sql.Clob.
     * 
     * @return the next attribute as a java.sql.Clob. null if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public Clob readClob() throws SQLException;

    /**
     * Returns the next attribute in the stream in the form of a java.sql.Array.
     * 
     * @return the next attribute as an Array. null if the value is SQL NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public Array readArray() throws SQLException;

    /**
     * Reports whether the last value read was SQL NULL.
     * 
     * @return true if the last value read was SQL NULL, false otherwise.
     * @throws SQLException
     *             if there is a database error
     */
    public boolean wasNull() throws SQLException;

    /**
     * Reads the next attribute in the stream (SQL DATALINK value) and returns
     * it as a java.net.URL object.
     * 
     * @return the next attribute as a java.net.URL. null if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if there is a database error
     */
    public URL readURL() throws SQLException;
}
