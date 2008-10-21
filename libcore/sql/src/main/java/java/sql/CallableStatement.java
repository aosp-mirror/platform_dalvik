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
import java.util.Calendar;
import java.util.Map;
import java.net.URL;
import java.io.InputStream;
import java.io.Reader;

/**
 * An interface used to call Stored Procedures.
 * <p>
 * The JDBC API provides an SQL escape syntax allowing Stored Procedures to be
 * called in a standard way for all databases. The JDBC escape syntax has two
 * forms. One form includes a result parameter. The second form does not include
 * a result parameter. Where the result parameter is used, it must be declared
 * as an OUT parameter. Other parameters can be declared as IN, OUT or INOUT.
 * Parameters are referenced either by name or by a numerical index, with the
 * first parameter being 1, the second 1 and so on. Here are examples of the two
 * forms of the escape syntax: <code>
 * 
 * { ?= call &lt.procedurename&gt.[([parameter1,parameter2,...])]}
 * 
 * {call &lt.procedurename&gt.[([parameter1,parameter2,...])]}
 * </code>
 * <p>
 * IN parameters are set before calling the procedure, using the setter methods
 * which are inherited from <code>PreparedStatement</code>. For OUT
 * parameters, their Type must be registered before executing the stored
 * procedure, and the value is retrieved using the getter methods defined in the
 * CallableStatement interface.
 * <p>
 * CallableStatements can return one or more ResultSets. Where multiple
 * ResultSets are returned they are accessed using the methods inherited from
 * the <code>Statement</code> interface.
 */
public interface CallableStatement extends PreparedStatement {

    /**
     * Gets the value of a specified JDBC <code>ARRAY</code> parameter as a
     * java.sql.Array.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a java.sql.Array containing the parameter value
     * @throws SQLException
     *             if a database error happens
     */
    public Array getArray(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC ARRAY parameter as a java.sql.Array.
     * 
     * @param parameterName
     *            the parameter of interest's name
     * @return a <code>java.sql.Array</code> containing the parameter value
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public Array getArray(String parameterName) throws SQLException;

    /**
     * Returns a new {@link BigDecimal} representation of the JDBC
     * <code>NUMERIC</code> parameter specified by the input index.
     * 
     * @param parameterIndex
     *            the parameter number index (starts from 1)
     * @return a <code>java.math.BigDecimal</code> with the value of the
     *         specified parameter. The value <code>null</code> is returned if
     *         the parameter in question is an SQL <code>NULL</code>
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException;

    /**
     * Returns a new {@link BigDecimal} representation of the JDBC
     * <code>NUMERIC</code> parameter specified by the input index. The number
     * of digits after the decimal point is specified by <code>scale</code>.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @param scale
     *            the number of digits after the decimal point to get
     * @return a <code>java.math.BigDecimal</code> with the value of the
     *         specified parameter. The value <code>null</code> is returned if
     *         the parameter in question is an SQL <code>NULL</code>
     * @throws SQLException
     *             if there is a problem accessing the database
     * @deprecated Use {@link #getBigDecimal(int)} or {@link #getBigDecimal(String)}
     */
    @Deprecated
    public BigDecimal getBigDecimal(int parameterIndex, int scale)
            throws SQLException;

    /**
     * Returns a new {@link BigDecimal} representation of the JDBC
     * <code>NUMERIC</code> parameter specified by the input name.
     * 
     * @param parameterName
     *            the name of the parameter
     * @return a java.math.BigDecimal with the value of the specified parameter.
     *         null if the value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public BigDecimal getBigDecimal(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC BLOB parameter as a java.sql.Blob
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a java.sql.Blob with the value. null if the value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Blob getBlob(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC BLOB parameter as a java.sql.Blob
     * 
     * @param parameterName
     *            the name of the parameter
     * @return a java.sql.Blob with the value. null if the value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Blob getBlob(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC BIT parameter as a boolean
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a boolean representing the parameter value. false if the value is
     *         SQL NULL
     * @throws SQLException
     *             if a database error happens
     */
    public boolean getBoolean(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC <code>BIT</code> parameter as a
     * boolean
     * 
     * @param parameterName
     *            the parameter of interest's name
     * @return a <code>boolean</code> representation of the value of the
     *         parameter. <code>false</code> is returned if the SQL value is
     *         <code>NULL</code>.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public boolean getBoolean(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC TINYINT parameter as a byte
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a byte with the value of the parameter. 0 if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public byte getByte(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC <code>TINYINT</code> parameter as a
     * Java <code>byte</code>.
     * 
     * @param parameterName
     *            the parameter of interest's name
     * @return a <code>byte</code> representation of the value of the
     *         parameter. <code>0</code> is returned if the SQL value is
     *         <code>NULL</code>.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public byte getByte(String parameterName) throws SQLException;

    /**
     * Returns a byte array representation of the indexed JDBC
     * <code>BINARY</code> or <code>VARBINARY</code> parameter.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return an array of bytes with the value of the parameter. null if the
     *         value is SQL NULL.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public byte[] getBytes(int parameterIndex) throws SQLException;

    /**
     * Returns a byte array representation of the named JDBC <code>BINARY</code>
     * or <code>VARBINARY</code> parameter.
     * 
     * @param parameterName
     *            the name of the parameter
     * @return an array of bytes with the value of the parameter. null if the
     *         value is SQL NULL.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public byte[] getBytes(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC CLOB parameter as a java.sql.Clob
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a java.sql.Clob with the value of the parameter. null if the
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Clob getClob(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC CLOB parameter as a java.sql.Clob
     * 
     * @param parameterName
     *            the name of the parameter
     * @return a java.sql.Clob with the value of the parameter. null if the
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Clob getClob(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC DATE parameter as a java.sql.Date.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return the java.sql.Date with the parameter value. null if the value is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Date getDate(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC DATE parameter as a java.sql.Date.,
     * using a specified Calendar to construct the date.
     * <p>
     * The JDBC driver uses the Calendar to create the Date using a particular
     * timezone and locale. Default behaviour of the driver is to use the Java
     * virtual machine default settings.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @param cal
     *            the Calendar to use to construct the Date
     * @return the java.sql.Date with the parameter value. null if the value is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Date getDate(int parameterIndex, Calendar cal) throws SQLException;

    /**
     * Gets the value of a specified JDBC DATE parameter as a java.sql.Date.
     * 
     * @param parameterName
     *            the name of the parameter
     * @return the java.sql.Date with the parameter value. null if the value is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Date getDate(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC DATE parameter as a java.sql.Date.,
     * using a specified Calendar to construct the date.
     * <p>
     * The JDBC driver uses the Calendar to create the Date using a particular
     * timezone and locale. Default behaviour of the driver is to use the Java
     * virtual machine default settings.
     * 
     * @param parameterName
     *            the parameter name
     * @param cal
     *            used for creating the returned <code>Date</code>
     * @return the java.sql.Date with the parameter value. null if the value is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Date getDate(String parameterName, Calendar cal) throws SQLException;

    /**
     * Gets the value of a specified JDBC DOUBLE parameter as a double
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return the double with the parameter value. 0.0 if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public double getDouble(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC DOUBLE parameter as a double
     * 
     * @param parameterName
     *            the parameter name
     * @return the parameter value as represented in a Java <code>double</code>.
     *         An SQL value of <code>NULL</code> gets represented as
     *         <code>0</code> (zero).
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public double getDouble(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC FLOAT parameter as a float
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return the float with the parameter value. 0.0 if the value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public float getFloat(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC <code>FLOAT</code> parameter as a
     * Java <code>float</code>.
     * 
     * @param parameterName
     *            the parameter name
     * @return the parameter value as represented in a Java <code>float</code>.
     *         An SQL value of <code>NULL</code> gets represented as
     *         <code>0</code> (zero).
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public float getFloat(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC INTEGER parameter as an int
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return the int with the parameter value. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public int getInt(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC INTEGER parameter as an int
     * 
     * @param parameterName
     *            the name of the parameter
     * @return the int with the parameter value. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public int getInt(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC BIGINT parameter as a long
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return the long with the parameter value. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public long getLong(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC BIGINT parameter as a long
     * 
     * @param parameterName
     *            the name of the parameter
     * @return the long with the parameter value. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public long getLong(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified parameter as a Java <code>Object</code>.
     * <p>
     * The object type returned is the JDBC type registered for the parameter
     * with a <code>registerOutParameter</code> call. If a parameter was
     * registered as a <code>java.sql.Types.OTHER</code> then it may hold
     * abstract types that are particular to the connected database.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return an Object holding the value of the parameter.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public Object getObject(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified parameter as an Object. A Map is supplied
     * to provide custom mapping of the parameter value.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @param map
     *            the Map holing the mapping from SQL types to Java classes
     * @return an Object holding the value of the parameter.
     * @throws SQLException
     *             if a database error happens
     */
    public Object getObject(int parameterIndex, Map<String, Class<?>> map)
            throws SQLException;

    /**
     * Gets the value of a specified parameter as an Object.
     * <p>
     * The object type returned is the JDBC type registered for the parameter
     * with a <code>registerOutParameter</code> call. If a parameter was
     * registered as a <code>java.sql.Types.OTHER</code> then it may hold
     * abstract types that are particular to the connected database.
     * 
     * @param parameterName
     *            the parameter name
     * @return the Java <code>Object</code> representation of the value of the
     *         parameter.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public Object getObject(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified parameter as an Object. A Map is supplied
     * to provide custom mapping of the parameter value.
     * 
     * @param parameterName
     *            the parameter name
     * @param map
     *            the <code>Map</code> of SQL types to their Java counterparts
     * @return an <code>Object</code> holding the value of the parameter.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public Object getObject(String parameterName, Map<String, Class<?>> map)
            throws SQLException;

    /**
     * Gets the value of a specified JDBC REF(<structured type>) parameter as a
     * java.sql.Ref
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a java.sql.Ref with the parameter value. null if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Ref getRef(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC REF(<structured type>) parameter as a
     * java.sql.Ref
     * 
     * @param parameterName
     *            the parameter name
     * @return the target parameter's value in the form of a
     *         <code>java.sql.Ref</code>. A <code>null</code> reference is
     *         returned for a parameter value of SQL <code>NULL</code>.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public Ref getRef(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC SMALLINT parameter as a short
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a short with the parameter value. 0 if the value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public short getShort(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC <code>SMALLINT</code> parameter as a
     * short
     * 
     * @param parameterName
     *            the parameter name
     * @return the value of the target parameter as a Java <code>short</code>.
     *         If the value is an SQL <code>NULL</code> then <code>0</code>
     *         (zero) is returned.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public short getShort(String parameterName) throws SQLException;

    /**
     * Returns the indexed parameter's value as a string. The parameter value
     * must be one of the JDBC types <code>CHAR</code>, <code>VARCHAR</code>
     * or <code>LONGVARCHAR</code>.
     * <p>
     * The string corresponding to a <code>CHAR</code> of fixed length will be
     * of identical length to the value in the database inclusive of padding
     * characters.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a String with the parameter value. null if the value is SQL NULL.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public String getString(int parameterIndex) throws SQLException;

    /**
     * Returns the named parameter's value as a string. The parameter value must
     * be one of the JDBC types <code>CHAR</code>, <code>VARCHAR</code> or
     * <code>LONGVARCHAR</code>.
     * <p>
     * The string corresponding to a <code>CHAR</code> of fixed length will be
     * of identical length to the value in the database inclusive of padding
     * characters.
     * 
     * @param parameterName
     *            the parameter name
     * @return a String with the parameter value. null if the value is SQL NULL.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public String getString(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC TIME parameter as a java.sql.Time.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a java.sql.Time with the parameter value. null if the value is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Time getTime(int parameterIndex) throws SQLException;

    /**
     * Gets the value of a specified JDBC TIME parameter as a java.sql.Time,
     * using the supplied Calendar to construct the time. The JDBC driver uses
     * the Calendar to handle specific timezones and locales when creating the
     * Time.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @param cal
     *            the Calendar to use in constructing the Time.
     * @return a java.sql.Time with the parameter value. null if the value is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException;

    /**
     * Gets the value of a specified JDBC <code>TIME</code> parameter as a
     * <code>java.sql.Time</code>
     * 
     * @param parameterName
     *            the parameter name
     * @return a new <code>java.sql.Time</code> with the parameter value. A
     *         <code>null</code> reference is returned for an SQL value of
     *         <code>NULL</code>
     * @throws SQLException
     *             if a database error happens
     */
    public Time getTime(String parameterName) throws SQLException;

    /**
     * Gets the value of a specified JDBC TIME parameter as a java.sql.Time,
     * using the supplied Calendar to construct the time. The JDBC driver uses
     * the Calendar to handle specific timezones and locales when creating the
     * Time.
     * 
     * @param parameterName
     *            the parameter name
     * @param cal
     *            used for creating the returned <code>Time</code>
     * @return a <code>java.sql.Time</code> with the parameter value. A
     *         <code>null</code> reference is returned for an SQL value of
     *         <code>NULL</code>
     * @throws SQLException
     *             if a database error happens
     */
    public Time getTime(String parameterName, Calendar cal) throws SQLException;

    /**
     * Returns the indexed parameter's <code>TIMESTAMP</code> value as a
     * <code>java.sql.Timestamp</code>.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a new <code>java.sql.Timestamp</code> with the parameter value.
     *         A <code>null</code> reference is returned for an SQL value of
     *         <code>NULL</code>
     * @throws SQLException
     *             if a database error happens
     */
    public Timestamp getTimestamp(int parameterIndex) throws SQLException;

    /**
     * Returns the indexed parameter's <code>TIMESTAMP</code> value as a
     * <code>java.sql.Timestamp</code>. The JDBC driver uses the supplied
     * <code>Calendar</code> to handle specific timezones and locales when
     * creating the result.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @param cal
     *            used for creating the returned <code>Timestamp</code>
     * @return a new <code>java.sql.Timestamp</code> with the parameter value.
     *         A <code>null</code> reference is returned for an SQL value of
     *         <code>NULL</code>
     * @throws SQLException
     *             if a database error happens
     */
    public Timestamp getTimestamp(int parameterIndex, Calendar cal)
            throws SQLException;

    /**
     * Returns the named parameter's <code>TIMESTAMP</code> value as a
     * <code>java.sql.Timestamp</code>.
     * 
     * @param parameterName
     *            the parameter name
     * @return a new <code>java.sql.Timestamp</code> with the parameter value.
     *         A <code>null</code> reference is returned for an SQL value of
     *         <code>NULL</code>
     * @throws SQLException
     *             if a database error happens
     */
    public Timestamp getTimestamp(String parameterName) throws SQLException;

    /**
     * Returns the indexed parameter's <code>TIMESTAMP</code> value as a
     * <code>java.sql.Timestamp</code>. The JDBC driver uses the supplied
     * <code>Calendar</code> to handle specific timezones and locales when
     * creating the result.
     * 
     * @param parameterName
     *            the parameter name
     * @param cal
     *            used for creating the returned <code>Timestamp</code>
     * @return a new <code>java.sql.Timestamp</code> with the parameter value.
     *         A <code>null</code> reference is returned for an SQL value of
     *         <code>NULL</code>
     * @throws SQLException
     *             if a database error happens
     */
    public Timestamp getTimestamp(String parameterName, Calendar cal)
            throws SQLException;

    /**
     * Gets the value of a specified JDBC DATALINK parameter as a java.net.URL.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @return a java.sql.Datalink with the parameter value. null if the value
     *         is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public URL getURL(int parameterIndex) throws SQLException;

    /**
     * Returns the named parameter's JDBC <code>DATALINK</code> value in a new
     * Java <code>java.net.URL</code>.
     * 
     * @param parameterName
     *            the parameter name
     * @return a new <code>java.net.URL</code> encapsulating the parameter
     *         value. A <code>null</code> reference is returned for an SQL
     *         value of <code>NULL</code>
     * @throws SQLException
     *             if a database error happens
     */
    public URL getURL(String parameterName) throws SQLException;

    /**
     * Defines the Type of a specified OUT parameter. All OUT parameters must
     * have their Type defined before a stored procedure is executed.
     * <p>
     * The Type defined by this method fixes the Java type that must be
     * retrieved using the getter methods of CallableStatement. If a database
     * specific type is expected for a parameter, the Type java.sql.Types.OTHER
     * should be used. Note that there is another variant of this method for
     * User Defined Types or a REF type.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @param sqlType
     *            the JDBC type as defined by java.sql.Types. The JDBC types
     *            NUMERIC and DECIMAL should be defined using the version of
     *            <code>registerOutParameter</code> that takes a
     *            <code>scale</code> parameter.
     * @throws SQLException
     *             if a database error happens
     */
    public void registerOutParameter(int parameterIndex, int sqlType)
            throws SQLException;

    /**
     * Defines the Type of a specified OUT parameter. All OUT parameters must
     * have their Type defined before a stored procedure is executed. This
     * version of the registerOutParameter method, which has a scale parameter,
     * should be used for the JDBC types NUMERIC and DECIMAL, where there is a
     * need to specify the number of digits expected after the decimal point.
     * <p>
     * The Type defined by this method fixes the Java type that must be
     * retrieved using the getter methods of CallableStatement.
     * 
     * @param parameterIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @param sqlType
     *            the JDBC type as defined by java.sql.Types.
     * @param scale
     *            the number of digits after the decimal point. Must be greater
     *            than or equal to 0.
     * @throws SQLException
     *             if a database error happens
     */
    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
            throws SQLException;

    /**
     * Defines the Type of a specified OUT parameter. This variant of the method
     * is designed for use with parameters that are User Defined Types (UDT) or
     * a REF type, although it can be used for any type.
     * 
     * @param paramIndex
     *            the parameter number index, where the first parameter has
     *            index 1
     * @param sqlType
     *            a JDBC type expressed as a constant from {@link Types}
     * @param typeName
     *            an SQL type name. For a REF type, this name should be the
     *            fully qualified name of the referenced type.
     * @throws SQLException
     *             if a database error happens
     */
    public void registerOutParameter(int paramIndex, int sqlType,
            String typeName) throws SQLException;

    /**
     * Defines the Type of a specified OUT parameter. All OUT parameters must
     * have their Type defined before a stored procedure is executed.
     * <p>
     * The Type defined by this method fixes the Java type that must be
     * retrieved using the getter methods of CallableStatement. If a database
     * specific type is expected for a parameter, the Type java.sql.Types.OTHER
     * should be used. Note that there is another variant of this method for
     * User Defined Types or a REF type.
     * 
     * @param parameterName
     *            the parameter name
     * @param sqlType
     *            a JDBC type expressed as a constant from {@link Types}. Types
     *            NUMERIC and DECIMAL should be defined using the variant of
     *            this method that takes a <code>scale</code> parameter.
     * @throws SQLException
     *             if a database error happens
     */
    public void registerOutParameter(String parameterName, int sqlType)
            throws SQLException;

    /**
     * Defines the Type of a specified OUT parameter. All OUT parameters must
     * have their Type defined before a stored procedure is executed. This
     * version of the registerOutParameter method, which has a scale parameter,
     * should be used for the JDBC types NUMERIC and DECIMAL, where there is a
     * need to specify the number of digits expected after the decimal point.
     * <p>
     * The Type defined by this method fixes the Java type that must be
     * retrieved using the getter methods of CallableStatement.
     * 
     * @param parameterName
     *            the parameter name
     * @param sqlType
     *            a JDBC type expressed as a constant from {@link Types}
     * @param scale
     *            the number of digits after the decimal point. Must be greater
     *            than or equal to 0.
     * @throws SQLException
     *             if a database error happens
     */
    public void registerOutParameter(String parameterName, int sqlType,
            int scale) throws SQLException;

    /**
     * Defines the Type of a specified OUT parameter. This variant of the method
     * is designed for use with parameters that are User Defined Types (UDT) or
     * a REF type, although it can be used for any type.Registers the designated
     * output parameter.
     * 
     * @param parameterName
     *            the parameter name
     * @param sqlType
     *            a JDBC type expressed as a constant from {@link Types}
     * @param typeName
     *            the fully qualified name of an SQL structured type. For a REF
     *            type, this name should be the fully qualified name of the
     *            referenced type.
     * @throws SQLException
     *             if a database error happens
     */
    public void registerOutParameter(String parameterName, int sqlType,
            String typeName) throws SQLException;

    /**
     * Sets the value of a specified parameter to the content of a supplied
     * InputStream, which has a specified number of bytes.
     * <p>
     * This is a good method for setting an SQL LONVARCHAR parameter where the
     * length of the data is large. Data is read from the InputStream until
     * end-of-file is reached or the specified number of bytes is copied.
     * 
     * @param parameterName
     *            the parameter name
     * @param theInputStream
     *            the ASCII InputStream carrying the data to update the
     *            parameter with
     * @param length
     *            the number of bytes in the InputStream to copy to the
     *            parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setAsciiStream(String parameterName,
            InputStream theInputStream, int length) throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied
     * java.math.BigDecimal value.
     * 
     * @param parameterName
     *            the name of the parameter
     * @param theBigDecimal
     *            the java.math.BigInteger value to set
     * @throws SQLException
     *             if a database error happens
     */
    public void setBigDecimal(String parameterName, BigDecimal theBigDecimal)
            throws SQLException;

    /**
     * Sets the value of a specified parameter to the content of a supplied
     * binary InputStream, which has a specified number of bytes.
     * <p>
     * Use this method when a large amount of data needs to be set into a
     * LONGVARBINARY parameter.
     * 
     * @param parameterName
     *            the name of the parameter
     * @param theInputStream
     *            the binary InputStream carrying the data to update the
     *            parameter
     * @param length
     *            the number of bytes in the InputStream to copy to the
     *            parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setBinaryStream(String parameterName,
            InputStream theInputStream, int length) throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied boolean value.
     * 
     * @param parameterName
     *            the parameter name
     * @param theBoolean
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setBoolean(String parameterName, boolean theBoolean)
            throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied byte value.
     * 
     * @param parameterName
     *            the parameter name
     * @param theByte
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setByte(String parameterName, byte theByte) throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied array of bytes. The
     * array is mapped to <code>VARBINARY</code> or else
     * <code>LONGVARBINARY</code> in the connected database.
     * 
     * @param parameterName
     *            the parameter name
     * @param theBytes
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setBytes(String parameterName, byte[] theBytes)
            throws SQLException;

    /**
     * Sets the value of a specified parameter to the character content of a
     * Reader object, with the specified length of character data.
     * 
     * @param parameterName
     *            the parameter name
     * @param reader
     *            the new value with which to update the parameter
     * @param length
     *            a count of the characters contained in <code>reader</code>
     * @throws SQLException
     *             if a database error happens
     */
    public void setCharacterStream(String parameterName, Reader reader,
            int length) throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied java.sql.Date
     * value.
     * 
     * @param parameterName
     *            the parameter name
     * @param theDate
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setDate(String parameterName, Date theDate) throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied java.sql.Date
     * value, using a supplied Calendar to map the Date. The Calendar allows the
     * application to control the timezone used to compute the SQL DATE in the
     * database - without the supplied Calendar, the driver uses the default
     * timezone of the Java virtual machine.
     * 
     * @param parameterName
     *            the parameter name
     * @param theDate
     *            the new value with which to update the parameter
     * @param cal
     *            a Calendar to use to construct the SQL DATE value
     * @throws SQLException
     *             if a database error happens
     */
    public void setDate(String parameterName, Date theDate, Calendar cal)
            throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied double value.
     * 
     * @param parameterName
     *            the parameter name
     * @param theDouble
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setDouble(String parameterName, double theDouble)
            throws SQLException;

    /**
     * Sets the value of a specified parameter to to a supplied float value.
     * 
     * @param parameterName
     *            the parameter name
     * @param theFloat
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setFloat(String parameterName, float theFloat)
            throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied int value.
     * 
     * @param parameterName
     *            the parameter name
     * @param theInt
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setInt(String parameterName, int theInt) throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied long value.
     * 
     * @param parameterName
     *            the parameter name
     * @param theLong
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setLong(String parameterName, long theLong) throws SQLException;

    /**
     * Sets the value of a specified parameter to SQL NULL. Don't use this
     * version of setNull for User Defined Types or for REF type parameters.
     * 
     * @param parameterName
     *            the parameter name
     * @param sqlType
     *            a JDBC type expressed as a constant from {@link Types}
     * @throws SQLException
     *             if a database error happens
     */
    public void setNull(String parameterName, int sqlType) throws SQLException;

    /**
     * Sets the value of a specified parameter to be SQL NULL where the
     * parameter type is either <code>REF</code> or user defined (e.g.
     * <code>STRUCT</code>, <code>JAVA_OBJECT</code> etc).
     * <p>
     * For reasons of portability, the caller is expected to supply both the SQL
     * Type code and Type name (which is just the parameter name if the type is
     * user defined, or the name of the type being referenced if a REF).
     * 
     * @param parameterName
     *            the parameter name
     * @param sqlType
     *            a JDBC type expressed as a constant from {@link Types}
     * @param typeName
     *            if the target parameter is a user defined type then this
     *            should contain the full type name
     * 
     * the fully qualified name of a UDT or REF type - ignored if the parameter
     * is not a UDT.
     * @throws SQLException
     *             if a database error happens
     */
    public void setNull(String parameterName, int sqlType, String typeName)
            throws SQLException;

    /**
     * Sets the value of a specified parameter using a supplied object. Prior to
     * issuing this request to the connected database <code>theObject</code>
     * is transformed to the corresponding SQL type according to the normal Java
     * to SQL mapping rules.
     * <p>
     * If the object's class implements the interface SQLData, the JDBC driver
     * calls <code>SQLData.writeSQL</code> to write it to the SQL data stream.
     * If <code>theObject</code> implements any of the following interfaces
     * then it is the role of the driver to flow the value to the connected
     * database using the appropriate SQL type :
     * <ul>
     * <li>{@link Ref}
     * <li>{@link Struct}
     * <li>{@link Array}
     * <li>{@link Clob}
     * <li>{@link Blob}
     * </ul>
     * 
     * @param parameterName
     *            the parameter name
     * @param theObject
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setObject(String parameterName, Object theObject)
            throws SQLException;

    /**
     * Sets the value of a specified parameter using a supplied object.
     * <p>
     * The Object is converted to the given targetSqlType before it is sent to
     * the database. If the object has a custom mapping (its class implements
     * the interface SQLData), the JDBC driver will call the method
     * SQLData.writeSQL to write it to the SQL data stream. If
     * <code>theObject</code> implements any of the following interfaces then
     * it is the role of the driver to flow the value to the connected database
     * using the appropriate SQL type :
     * <ul>
     * <li>{@link Ref}
     * <li>{@link Struct}
     * <li>{@link Array}
     * <li>{@link Clob}
     * <li>{@link Blob}
     * </ul>
     * 
     * @param parameterName
     *            the parameter name
     * @param theObject
     *            the new value with which to update the parameter
     * @param targetSqlType
     *            a JDBC type expressed as a constant from {@link Types}
     * @throws SQLException
     *             if a database error happens
     */
    public void setObject(String parameterName, Object theObject,
            int targetSqlType) throws SQLException;

    /**
     * Sets the value of a specified parameter using a supplied object.
     * <p>
     * The Object is converted to the given targetSqlType before it is sent to
     * the database. If the object has a custom mapping (its class implements
     * the interface SQLData), the JDBC driver will call the method
     * SQLData.writeSQL to write it to the SQL data stream. If
     * <code>theObject</code> implements any of the following interfaces then
     * it is the role of the driver to flow the value to the connected database
     * using the appropriate SQL type :
     * <ul>
     * <li>{@link Ref}
     * <li>{@link Struct}
     * <li>{@link Array}
     * <li>{@link Clob}
     * <li>{@link Blob}
     * </ul>
     * 
     * @param parameterName
     *            the parameter name
     * @param theObject
     *            the new value with which to update the parameter
     * @param targetSqlType
     *            a JDBC type expressed as a constant from {@link Types}
     * @param scale
     *            where applicable, the number of digits after the decimal
     *            point.
     * @throws SQLException
     *             if a database error happens
     */
    public void setObject(String parameterName, Object theObject,
            int targetSqlType, int scale) throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied short value.
     * 
     * @param parameterName
     *            the name of the parameter
     * @param theShort
     *            a short value to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setShort(String parameterName, short theShort)
            throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied String.
     * 
     * @param parameterName
     *            the name of the parameter
     * @param theString
     *            a String value to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setString(String parameterName, String theString)
            throws SQLException;

    /**
     * Sets the value of the parameter named <code>parameterName</code> to the
     * value of the supplied <code>java.sql.Time</code>.
     * 
     * @param parameterName
     *            the parameter name
     * @param theTime
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setTime(String parameterName, Time theTime) throws SQLException;

    /**
     * Sets the value of the parameter named <code>parameterName</code> to the
     * value of the supplied <code>java.sql.Time</code> using the supplied
     * Calendar.
     * <p>
     * The driver uses the supplied Calendar to create the SQL TIME value, which
     * allows it to use a custom timezone - otherwise the driver uses the
     * default timezone of the Java virtual machine.
     * 
     * @param parameterName
     *            the parameter name
     * @param theTime
     *            the new value with which to update the parameter
     * @param cal
     *            used for creating the new SQL <code>TIME</code> value
     * @throws SQLException
     *             if a database error happens
     */
    public void setTime(String parameterName, Time theTime, Calendar cal)
            throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied java.sql.Timestamp
     * value.
     * 
     * @param parameterName
     *            the parameter name
     * @param theTimestamp
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setTimestamp(String parameterName, Timestamp theTimestamp)
            throws SQLException;

    /**
     * Sets the value of a specified parameter to a supplied java.sql.Timestamp
     * value, using the supplied Calendar.
     * <p>
     * The driver uses the supplied Calendar to create the SQL TIMESTAMP value,
     * which allows it to use a custom timezone - otherwise the driver uses the
     * default timezone of the Java virtual machine.
     * 
     * @param parameterName
     *            the parameter name
     * @param theTimestamp
     *            the new value with which to update the parameter
     * @param cal
     *            used for creating the new SQL <code>TIME</code> value
     * @throws SQLException
     *             if a database error happens
     */
    public void setTimestamp(String parameterName, Timestamp theTimestamp,
            Calendar cal) throws SQLException;

    /**
     * Sets the value of a specified parameter to the supplied java.net.URL.
     * 
     * @param parameterName
     *            the parameter name
     * @param theURL
     *            the new value with which to update the parameter
     * @throws SQLException
     *             if a database error happens
     */
    public void setURL(String parameterName, URL theURL) throws SQLException;

    /**
     * Gets whether the value of the last OUT parameter read was SQL NULL.
     * 
     * @return true if the last parameter was SQL NULL, false otherwise.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean wasNull() throws SQLException;
}
