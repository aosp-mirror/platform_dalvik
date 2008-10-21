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
import java.math.BigDecimal;
import java.io.Reader;
import java.util.Calendar;
import java.util.Map;
import java.net.URL;

/**
 * An interface to an Object which represents a Table of Data, typically
 * returned as the result of a Query to a Database.
 * <p>
 * <code>ResultSets</code> have a Cursor which points to a current row of
 * data. When a ResultSet is created, the Cursor is positioned before the first
 * row. To move the Cursor to the next row in the table, use the
 * <code>next</code> method. The next method returns true until there are no
 * more rows in the ResultSet, when it returns false.
 * <p>
 * The default type of ResultSet cannot be updated and its cursor can only move
 * forward through the rows of data. This means that it is only possible to read
 * through it once. However, it is possible to create types of ResultSet that
 * can be updated and also types where the cursor can be scrolled forward and
 * backward through the rows of data. This is shown in the following code
 * example: <code>
 *         Connection con;
 *         Statement aStatement = con.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE,
 *                                                       ResultSet.CONCUR_UPDATABLE );
 *         ResultSet theResultSet = theStatement.executeQuery("SELECT price, quantity FROM STOCKTABLE");
 *         // theResultSet will be both scrollable and updateable
 * </code>
 * <p>
 * The ResultSet interface provides a series of methods for retrieving data from
 * columns in the current row, such as getDate, getFloat. The columns are
 * identified either by their index number (starting at 1) or by their name -
 * there are separate methods for both techniques of column addressing. The
 * column names are case insensitive. If several columns have the same name,
 * then the getter methods use the first matching column. This means that if
 * column names are used, it is not possible to guarantee that the name will
 * retrieve data from the intended column - for certainty it is better to use
 * column indexes. Ideally the columns should be read left-to-right and read
 * once only, since not all * databases are optimized to handle other techniques
 * of reading the data.
 * <p>
 * When reading data, the JDBC driver maps the SQL data retrieved from the
 * database to the Java type implied by the method invoked by the application.
 * The JDBC specification has a table of allowable mappings from SQL types to
 * Java types.
 * <p>
 * There are also methods for writing data into the ResultSet, such as
 * updateInt, updateString. The update methods can be used either to modify the
 * data of an existing row or to insert new data rows into the ResultSet.
 * Modification of existing data involves moving the Cursor to the row which
 * needs modification and then using the update methods to modify the data,
 * followed by calling the ResultSet.updateRow method. For insertion of new
 * rows, the cursor is first moved to a special row called the Insert Row, data
 * is added using the update methods, followed by calling the
 * ResultSet.insertRow method.
 * <p>
 * A ResultSet is closed if the Statement object which generated it closed,
 * executed again or is used to retrieve the next result from a sequence of
 * results.
 * 
 */
public interface ResultSet {

    /**
     * A constant used to indicate that a ResultSet object must be closed when
     * the method Connection.commit is invoked.
     */
    public static final int CLOSE_CURSORS_AT_COMMIT = 2;

    /**
     * A constant used to indicate that a ResultSet object must not be closed
     * when the method Connection.commit is invoked.
     */
    public static final int HOLD_CURSORS_OVER_COMMIT = 1;

    /**
     * A constant used to indicate the Concurrency Mode for a ResultSet object
     * that cannot be updated.
     */
    public static final int CONCUR_READ_ONLY = 1007;

    /**
     * A constant used to indicate the Concurrency Mode for a ResultSet object
     * that can be updated.
     */
    public static final int CONCUR_UPDATABLE = 1008;

    /**
     * A constant used to indicate processing of the rows of a ResultSet in the
     * forward direction, first to last
     */
    public static final int FETCH_FORWARD = 1000;

    /**
     * A constant used to indicate processing of the rows of a ResultSet in the
     * reverse direction, last to first
     */
    public static final int FETCH_REVERSE = 1001;

    /**
     * A constant used to indicate that the order of processing of the rows of a
     * ResultSet is unknown.
     */
    public static final int FETCH_UNKNOWN = 1002;

    /**
     * A constant used to indicate a ResultSet object whose Cursor can only move
     * forward
     */
    public static final int TYPE_FORWARD_ONLY = 1003;

    /**
     * A constant used to indicate a ResultSet object which is Scrollable but
     * which is not sensitive to changes made by others
     */
    public static final int TYPE_SCROLL_INSENSITIVE = 1004;

    /**
     * A constant used to indicate a ResultSet object which is Scrollable but
     * which is sensitive to changes made by others
     */
    public static final int TYPE_SCROLL_SENSITIVE = 1005;

    /**
     * Moves the Cursor to a specified row number in the ResultSet.
     * 
     * @param row
     *            The new row number for the Cursor
     * @return true if the new Cursor position is on the ResultSet, false
     *         otherwise
     * @throws SQLException
     *             if a database error happens
     */
    public boolean absolute(int row) throws SQLException;

    /**
     * Moves the Cursor to the end of the ResultSet, after the last row.
     * 
     * @throws SQLException
     *             if a database error happens
     */
    public void afterLast() throws SQLException;

    /**
     * Moves the Cursor to the start of the ResultSet, before the first row.
     * 
     * @throws SQLException
     *             if a database error happens
     */
    public void beforeFirst() throws SQLException;

    /**
     * Cancels any updates made to the current row in the ResultSet.
     * 
     * @throws SQLException
     *             if a database error happens
     */
    public void cancelRowUpdates() throws SQLException;

    /**
     * Clears all the warnings related to this ResultSet.
     * 
     * @throws SQLException
     *             if a database error happens
     */
    public void clearWarnings() throws SQLException;

    /**
     * Releases this ResultSet's database and JDBC resources. You are strongly
     * advised to use this method rather than relying on the release being done
     * when the ResultSet's finalize method is called during garbage collection
     * process. Note that the close() method might take some time to complete
     * since it is dependent on the behaviour of the connection to the database
     * and the database itself.
     * 
     * @throws SQLException
     *             if a database error happens
     */
    public void close() throws SQLException;

    /**
     * Deletes the current row from the ResultSet and from the underlying
     * database.
     * 
     * @throws SQLException
     *             if a database error happens
     */
    public void deleteRow() throws SQLException;

    /**
     * Gets the index number for a column in the ResultSet from the provided
     * Column Name.
     * 
     * @param columnName
     *            the column name
     * @return the index of the column in the ResultSet for the column name
     * @throws SQLException
     *             if a database error happens
     */
    public int findColumn(String columnName) throws SQLException;

    /**
     * Shifts the cursor position to the first row in the ResultSet.
     * 
     * @return true if the position is in a legitimate row, false if the
     *         ResultSet contains no rows.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean first() throws SQLException;

    /**
     * Gets the content of a column specified as a column index in the current
     * row of this ResultSet as a java.sql.Array.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a java.sql.Array with the data from the column
     * @throws SQLException
     *             if a database error happens
     */
    public Array getArray(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a
     * java.sql.Array.
     * 
     * @param colName
     *            the name of the column to read
     * @return a java.sql.Array with the data from the column
     * @throws SQLException
     *             if a database error happens
     */
    public Array getArray(String colName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as an ASCII
     * character stream.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return an InputStream with the data from the column
     * @throws SQLException
     *             if a database error happens
     */
    public InputStream getAsciiStream(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as an ASCII
     * character stream.
     * 
     * @param columnName
     *            the name of the column to read
     * @return an InputStream with the data from the column
     * @throws SQLException
     *             if a database error happens
     */
    public InputStream getAsciiStream(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a
     * java.math.BigDecimal.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a BigDecimal with the value of the column
     * @throws SQLException
     *             if a database error happens
     */
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException;

    /**
     * @deprecated Gets the value of a column specified as a column index as a
     *             java.math.BigDecimal.
     * @param columnIndex
     *            the index of the column to read
     * @param scale
     *            the number of digits after the decimal point
     * @return a BigDecimal with the value of the column
     * @throws SQLException
     *             if a database error happens
     */
    @Deprecated
    public BigDecimal getBigDecimal(int columnIndex, int scale)
            throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as a
     * java.math.BigDecimal.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a BigDecimal with the value of the column
     * @throws SQLException
     *             if a database error happens
     */
    public BigDecimal getBigDecimal(String columnName) throws SQLException;

    /**
     * @deprecated Gets the value of a column specified as a column name, as a
     *             java.math.BigDecimal.
     * @param columnName
     *            the name of the column to read
     * @param scale
     *            the number of digits after the decimal point
     * @return a BigDecimal with the value of the column
     * @throws SQLException
     *             if a database error happens
     */
    @Deprecated
    public BigDecimal getBigDecimal(String columnName, int scale)
            throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a binary
     * stream.
     * <p>
     * This method can be used to read LONGVARBINARY values. All of the data in
     * the InputStream should be read before getting data from any other column.
     * A further call to a getter method will implicitly close the InputStream.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return an InputStream with the data from the column. If the column value
     *         is SQL NULL, null is returned.
     * @throws SQLException
     *             if a database error happens
     */
    public InputStream getBinaryStream(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a binary stream.
     * <p>
     * This method can be used to read LONGVARBINARY values. All of the data in
     * the InputStream should be read before getting data from any other column.
     * A further call to a getter method will implicitly close the InputStream.
     * 
     * @param columnName
     *            the name of the column to read
     * @return an InputStream with the data from the column If the column value
     *         is SQL NULL, null is returned.
     * @throws SQLException
     *             if a database error happens
     */
    public InputStream getBinaryStream(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a java.sql.Blob
     * object.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a java.sql.Blob with the value of the column
     * @throws SQLException
     *             if a database error happens
     */
    public Blob getBlob(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as a java.sql.Blob
     * object.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a java.sql.Blob with the value of the column
     * @throws SQLException
     *             if a database error happens
     */
    public Blob getBlob(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a boolean.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a boolean value from the column. If the column is SQL NULL, false
     *         is returned.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean getBoolean(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as a boolean.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a boolean value from the column. If the column is SQL NULL, false
     *         is returned.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean getBoolean(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a byte.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a byte containing the value of the column. 0 if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public byte getByte(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a byte.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a byte containing the value of the column. 0 if the value is SQL
     *         NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public byte getByte(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a byte array.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a byte array containing the value of the column. null if the
     *         column contains SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public byte[] getBytes(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a byte array.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a byte array containing the value of the column. null if the
     *         column contains SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public byte[] getBytes(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a
     * java.io.Reader object.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a Reader holding the value of the column. null if the column
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Reader getCharacterStream(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a java.io.Reader
     * object.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a Reader holding the value of the column. null if the column
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Reader getCharacterStream(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a
     * java.sql.Clob.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a Clob object representing the value in the column. null if the
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Clob getClob(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a java.sql.Clob.
     * 
     * @param colName
     *            the name of the column to read
     * @return a Clob object representing the value in the column. null if the
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Clob getClob(String colName) throws SQLException;

    /**
     * Gets the concurrency mode of this ResultSet.
     * 
     * @return the concurrency mode - one of: ResultSet.CONCUR_READ_ONLY,
     *         ResultSet.CONCUR_UPDATABLE
     * @throws SQLException
     *             if a database error happens
     */
    public int getConcurrency() throws SQLException;

    /**
     * Gets the name of the SQL cursor of this ResultSet.
     * 
     * @return a String containing the SQL cursor name
     * @throws SQLException
     *             if a database error happens
     */
    public String getCursorName() throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a
     * java.sql.Date.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a java.sql.Date matching the column value. null if the column is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Date getDate(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a
     * java.sql.Date. This method uses a supplied calendar to compute the Date.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @param cal
     *            a java.util.Calendar to use in constructing the Date.
     * @return a java.sql.Date matching the column value. null if the column is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Date getDate(int columnIndex, Calendar cal) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a java.sql.Date.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a java.sql.Date matching the column value. null if the column is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Date getDate(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as a java.sql.Date
     * object.
     * 
     * @param columnName
     *            the name of the column to read
     * @param cal
     *            java.util.Calendar to use in constructing the Date.
     * @return a java.sql.Date matching the column value. null if the column is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Date getDate(String columnName, Calendar cal) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a double value.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a double containing the column value. 0.0 if the column is SQL
     *         NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public double getDouble(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a double value.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a double containing the column value. 0.0 if the column is SQL
     *         NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public double getDouble(String columnName) throws SQLException;

    /**
     * Gets the direction in which rows are fetched for this ResultSet object.
     * 
     * @return the fetch direction. Will be: ResultSet.FETCH_FORWARD,
     *         ResultSet.FETCH_REVERSE or ResultSet.FETCH_UNKNOWN
     * @throws SQLException
     *             if a database error happens
     */
    public int getFetchDirection() throws SQLException;

    /**
     * Gets the fetch size (in number of rows) for this ResultSet
     * 
     * @return the fetch size as an int
     * @throws SQLException
     *             if a database error happens
     */
    public int getFetchSize() throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a float value.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a float containing the column value. 0.0 if the column is SQL
     *         NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public float getFloat(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a float value.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a float containing the column value. 0.0 if the column is SQL
     *         NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public float getFloat(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as an int value.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return an int containing the column value. 0 if the column is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public int getInt(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as an int value.
     * 
     * @param columnName
     *            the name of the column to read
     * @return an int containing the column value. 0 if the column is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public int getInt(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a long value.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a long containing the column value. 0 if the column is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public long getLong(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as a long value.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a long containing the column value. 0 if the column is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public long getLong(String columnName) throws SQLException;

    /**
     * Gets the Metadata for this ResultSet. This defines the number, types and
     * properties of the columns in the ResultSet.
     * 
     * @return a ResultSetMetaData object with information about this ResultSet.
     * @throws SQLException
     *             if a database error happens
     */
    public ResultSetMetaData getMetaData() throws SQLException;

    /**
     * Gets the value of a specified column as a Java Object. The type of the
     * returned object will be the default according to the column's SQL type,
     * following the JDBC specification for built-in types.
     * <p>
     * For SQL User Defined Types, if a column value is Structured or Distinct,
     * this method behaves the same as a call to: getObject(columnIndex,
     * this.getStatement().getConnection().getTypeMap())
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return an Object containing the value of the column. null if the column
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Object getObject(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a Java Object.
     * <p>
     * The type of the Java object will be determined by the supplied Map to
     * perform the mapping of SQL Struct or Distinct types into Java objects.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @param map
     *            a java.util.Map containing a mapping from SQL Type names to
     *            Java classes.
     * @return an Object containing the value of the column. null if the column
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Object getObject(int columnIndex, Map<String, Class<?>> map)
            throws SQLException;

    /**
     * Gets the value of a specified column as a Java Object. The type of the
     * returned object will be the default according to the column's SQL type,
     * following the JDBC specification for built-in types.
     * <p>
     * For SQL User Defined Types, if a column value is Structured or Distinct,
     * this method behaves the same as a call to: getObject(columnIndex,
     * this.getStatement().getConnection().getTypeMap())
     * 
     * @param columnName
     *            the name of the column to read
     * @return an Object containing the value of the column. null if the column
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Object getObject(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a Java Object.
     * <p>
     * The type of the Java object will be determined by the supplied Map to
     * perform the mapping of SQL Struct or Distinct types into Java objects.
     * 
     * @param columnName
     *            the name of the column to read
     * @param map
     *            a java.util.Map containing a mapping from SQL Type names to
     *            Java classes.
     * @return an Object containing the value of the column. null if the column
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Object getObject(String columnName, Map<String, Class<?>> map)
            throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a Java
     * java.sql.Ref.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a Ref representing the value of the SQL REF in the column
     * @throws SQLException
     *             if a database error happens
     */
    public Ref getRef(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a Java
     * java.sql.Ref.
     * 
     * @param colName
     *            the name of the column to read
     * @return a Ref representing the value of the SQL REF in the column
     * @throws SQLException
     *             if a database error happens
     */
    public Ref getRef(String colName) throws SQLException;

    /**
     * Gets the number of the current row in the ResultSet. Row numbers start at
     * 1 for the first row.
     * 
     * @return the index number of the current row. 0 is returned if there is no
     *         current row.
     * @throws SQLException
     *             if a database error happens
     */
    public int getRow() throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a short value.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a short value containing the value of the column. 0 if the value
     *         is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public short getShort(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as a short value.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a short value containing the value of the column. 0 if the value
     *         is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public short getShort(String columnName) throws SQLException;

    /**
     * Gets the Statement that produced this ResultSet. If the ResultSet was not
     * created by a Statement (eg it was returned from one of the
     * DatabaseMetaData methods), null is returned.
     * 
     * @return the Statement which produced this ResultSet, or null if the
     *         ResultSet was not created by a Statement.
     * @throws SQLException
     */
    public Statement getStatement() throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a String.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return the String representing the value of the column, null if the
     *         column is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public String getString(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as a String.
     * 
     * @param columnName
     *            the name of the column to read
     * @return the String representing the value of the column, null if the
     *         column is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public String getString(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a java.sql.Time
     * value.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a Time representing the column value, null if the column value is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Time getTime(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a java.sql.Time
     * value. The supplied Calendar is used to map between the SQL Time value
     * and the Java Time value.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @param cal
     *            a Calendar to use in creating the Java Time value.
     * @return a Time representing the column value, null if the column value is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Time getTime(int columnIndex, Calendar cal) throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as a java.sql.Time
     * value.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a Time representing the column value, null if the column value is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Time getTime(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index, as a
     * java.sql.Time value. The supplied Calendar is used to map between the SQL
     * Time value and the Java Time value.
     * 
     * @param columnName
     *            the name of the column to read
     * @param cal
     *            a Calendar to use in creating the Java Time value.
     * @return a Time representing the column value, null if the column value is
     *         SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Time getTime(String columnName, Calendar cal) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a
     * java.sql.Timestamp value.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a Timestamp representing the column value, null if the column
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Timestamp getTimestamp(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column index, as a
     * java.sql.Timestamp value. The supplied Calendar is used to map between
     * the SQL Timestamp value and the Java Timestamp value.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @param cal
     *            Calendar to use in creating the Java Timestamp value.
     * @return a Timestamp representing the column value, null if the column
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Timestamp getTimestamp(int columnIndex, Calendar cal)
            throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as a
     * java.sql.Timestamp value.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a Timestamp representing the column value, null if the column
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Timestamp getTimestamp(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column name, as a
     * java.sql.Timestamp value. The supplied Calendar is used to map between
     * the SQL Timestamp value and the Java Timestamp value.
     * 
     * @param columnName
     *            the name of the column to read
     * @param cal
     *            Calendar to use in creating the Java Timestamp value.
     * @return a Timestamp representing the column value, null if the column
     *         value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public Timestamp getTimestamp(String columnName, Calendar cal)
            throws SQLException;

    /**
     * Gets the type of the ResultSet.
     * 
     * @return The ResultSet type, one of: ResultSet.TYPE_FORWARD_ONLY,
     *         ResultSet.TYPE_SCROLL_INSENSITIVE, or
     *         ResultSet.TYPE_SCROLL_SENSITIVE
     * @throws SQLException
     *             if there is a database error
     */
    public int getType() throws SQLException;

    /**
     * @deprecated Use {@link #getCharacterStream}.
     *             <p>
     *             Gets the value of the column as an InputStream of Unicode
     *             characters.
     * @param columnIndex
     *            the index of the column to read
     * @return an InputStream holding the value of the column. null if the
     *         column value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    @Deprecated
    public InputStream getUnicodeStream(int columnIndex) throws SQLException;

    /**
     * @deprecated Use {@link #getCharacterStream}
     *             <p>
     *             Gets the value of the column as an InputStream of Unicode
     *             characters.
     * @param columnName
     *            the name of the column to read
     * @return an InputStream holding the value of the column. null if the
     *         column value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    @Deprecated
    public InputStream getUnicodeStream(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified as a column index as a java.net.URL.
     * 
     * @param columnIndex
     *            the index of the column to read
     * @return a URL. null if the column value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public URL getURL(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified as a column name as a java.net.URL
     * object.
     * 
     * @param columnName
     *            the name of the column to read
     * @return a URL. null if the column value is SQL NULL.
     * @throws SQLException
     *             if a database error happens
     */
    public URL getURL(String columnName) throws SQLException;

    /**
     * Gets the first warning generated by calls on this ResultSet. Subsequent
     * warnings on this ResultSet are chained to the first one.
     * <p>
     * The warnings are cleared when a new Row is read from the ResultSet. The
     * warnings returned by this method are only the warnings generated by
     * ResultSet method calls - warnings generated by Statement methods are held
     * by the Statement.
     * <p>
     * An SQLException is generated if this method is called on a closed
     * ResultSet.
     * 
     * @return an SQLWarning which is the first warning for this ResultSet. null
     *         if there are no warnings.
     * @throws SQLException
     *             if a database error happens
     */
    public SQLWarning getWarnings() throws SQLException;

    /**
     * Insert the insert row into the ResultSet and into the underlying
     * database. The Cursor must be set to the Insert Row before this method is
     * invoked.
     * 
     * @throws SQLException
     *             if a database error happens. Particular cases include the
     *             Cursor not being on the Insert Row or if any Columns in the
     *             Row do not have a value where the column is declared as
     *             not-nullable.
     */
    public void insertRow() throws SQLException;

    /**
     * Gets if the cursor is after the last row of the ResultSet.
     * 
     * @return true if the Cursor is after the last Row in the ResultSet, false
     *         if the cursor is at any other position in the ResultSet.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean isAfterLast() throws SQLException;

    /**
     * Gets if the cursor is before the first row of the ResultSet.
     * 
     * @return true if the Cursor is before the last Row in the ResultSet, false
     *         if the cursor is at any other position in the ResultSet.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean isBeforeFirst() throws SQLException;

    /**
     * Gets if the cursor is on the first row of the ResultSet.
     * 
     * @return true if the Cursor is on the first Row in the ResultSet, false if
     *         the cursor is at any other position in the ResultSet.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean isFirst() throws SQLException;

    /**
     * Gets if the cursor is on the last row of the ResultSet
     * 
     * @return true if the Cursor is on the last Row in the ResultSet, false if
     *         the cursor is at any other position in the ResultSet.
     * @throws SQLException
     */
    public boolean isLast() throws SQLException;

    /**
     * Shifts the cursor position to the last row of the ResultSet.
     * 
     * @return true if the new position is in a legitimate row, false if the
     *         ResultSet contains no rows.
     * @throws SQLException
     *             if there is a database error
     */
    public boolean last() throws SQLException;

    /**
     * Moves the cursor to the remembered position, usually the current row.
     * This only applies if the cursor is on the Insert row.
     * 
     * @throws SQLException
     *             if a database error happens
     */
    public void moveToCurrentRow() throws SQLException;

    /**
     * Moves the cursor position to the Insert row. The current position is
     * remembered and the cursor is positioned at the Insert row. The columns in
     * the Insert row should be filled in with the appropriate update methods,
     * before calling <code>insertRow</code> to insert the new row into the
     * database.
     * 
     * @throws SQLException
     *             if a database error happens
     */
    public void moveToInsertRow() throws SQLException;

    /**
     * Shifts the cursor position down one row in this ResultSet object.
     * <p>
     * Any InputStreams associated with the current row are closed and any
     * warnings are cleared.
     * 
     * @return true if the updated cursor position is pointing to a valid row,
     *         false otherwise (ie when the cursor is after the last row in the
     *         ResultSet).
     * @throws SQLException
     *             if a database error happens
     */
    public boolean next() throws SQLException;

    /**
     * Relocates the cursor position to the preceding row in this ResultSet.
     * 
     * @return true if the new position is in a legitimate row, false if the
     *         cursor is now before the first row.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean previous() throws SQLException;

    /**
     * Refreshes the current row with its most up to date value in the database.
     * Must not be called when the cursor is on the Insert row.
     * <p>
     * If any columns in the current row have been updated but the
     * <code>updateRow</code> has not been called, then the updates are lost
     * when this method is called.
     * 
     * @throws SQLException
     *             if a database error happens, including if the current row is
     *             the Insert row.
     */
    public void refreshRow() throws SQLException;

    /**
     * Moves the cursor position up or down by a specified number of rows. If
     * the new position is beyond the start or end rows, the cursor position is
     * set before the first row/after the last row.
     * 
     * @param rows
     *            a number of rows to move the cursor - may be positive or
     *            negative
     * @return true if the new cursor position is on a row, false otherwise
     * @throws SQLException
     *             if a database error happens
     */
    public boolean relative(int rows) throws SQLException;

    /**
     * Indicates whether a row has been deleted. This method depends on whether
     * the JDBC driver and database can detect deletions.
     * 
     * @return true if a row has been deleted and if deletions are detected,
     *         false otherwise.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean rowDeleted() throws SQLException;

    /**
     * Indicates whether the current row has had an insertion operation. This
     * method depends on whether the JDBC driver and database can detect
     * insertions.
     * 
     * @return true if a row has been inserted and if insertions are detected,
     *         false otherwise.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean rowInserted() throws SQLException;

    /**
     * Indicates whether the current row has been updated. This method depends
     * on whether the JDBC driver and database can detect updates.
     * 
     * @return true if the current row has been updated and if updates can be
     *         detected, false otherwise.
     * @throws SQLException
     *             if a database error happens
     */
    public boolean rowUpdated() throws SQLException;

    /**
     * Indicates which direction (forward/reverse) will be used to process the
     * rows of this ResultSet object. This is treated as a hint by the JDBC
     * driver.
     * 
     * @param direction
     *            can be ResultSet.FETCH_FORWARD, ResultSet.FETCH_REVERSE, or
     *            ResultSet.FETCH_UNKNOWN
     * @throws SQLException
     *             if there is a database error
     */
    public void setFetchDirection(int direction) throws SQLException;

    /**
     * Indicates the amount of rows to fetch from the database when extra rows
     * are required for this ResultSet. This used as a hint to the JDBC driver.
     * 
     * @param rows
     *            the number of rows to fetch. 0 implies that the JDBC driver
     *            can make its own decision about the fetch size. The number
     *            should not be greater than the maximum number of rows
     *            established by the Statement that generated the ResultSet.
     * @throws SQLException
     *             if a database error happens
     */
    public void setFetchSize(int rows) throws SQLException;

    /**
     * Updates a column specified by a column index with a java.sql.Array value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateArray(int columnIndex, Array x) throws SQLException;

    /**
     * Updates a column specified by a column name with a java.sql.Array value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateArray(String columnName, Array x) throws SQLException;

    /**
     * Updates a column specified by a column index with an ASCII stream value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @param length
     *            the length of the data to write from the stream
     * @throws SQLException
     *             if a database error happens
     */
    public void updateAsciiStream(int columnIndex, InputStream x, int length)
            throws SQLException;

    /**
     * Updates a column specified by a column name with an Ascii stream value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @param length
     *            the length of the data to write from the stream
     * @throws SQLException
     *             if a database error happens
     */
    public void updateAsciiStream(String columnName, InputStream x, int length)
            throws SQLException;

    /**
     * Updates a column specified by a column index with a java.sql.BigDecimal
     * value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws SQLException;

    /**
     * Updates a column specified by a column name with a java.sql.BigDecimal
     * value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateBigDecimal(String columnName, BigDecimal x)
            throws SQLException;

    /**
     * Updates a column specified by a column index with a binary stream value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @param length
     * @throws SQLException
     *             if a database error happens
     */
    public void updateBinaryStream(int columnIndex, InputStream x, int length)
            throws SQLException;

    /**
     * Updates a column specified by a column name with a binary stream value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @param length
     * @throws SQLException
     *             if a database error happens
     */
    public void updateBinaryStream(String columnName, InputStream x, int length)
            throws SQLException;

    /**
     * Updates a column specified by a column index with a java.sql.Blob value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateBlob(int columnIndex, Blob x) throws SQLException;

    /**
     * Updates a column specified by a column name with a java.sql.Blob value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateBlob(String columnName, Blob x) throws SQLException;

    /**
     * Updates a column specified by a column index with a boolean value.
     * 
     * @param columnIndex
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateBoolean(int columnIndex, boolean x) throws SQLException;

    /**
     * Updates a column specified by a column name with a boolean value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateBoolean(String columnName, boolean x) throws SQLException;

    /**
     * Updates a column specified by a column index with a byte value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateByte(int columnIndex, byte x) throws SQLException;

    /**
     * Updates a column specified by a column name with a byte value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateByte(String columnName, byte x) throws SQLException;

    /**
     * Updates a column specified by a column index with a byte array value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateBytes(int columnIndex, byte[] x) throws SQLException;

    /**
     * Updates a column specified by a column name with a byte array value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateBytes(String columnName, byte[] x) throws SQLException;

    /**
     * Updates a column specified by a column index with a character stream
     * value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @param length
     *            the length of data to write from the stream
     * @throws SQLException
     *             if a database error happens
     */
    public void updateCharacterStream(int columnIndex, Reader x, int length)
            throws SQLException;

    /**
     * Updates a column specified by a column name with a character stream
     * value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param reader
     *            the new value for the specified column
     * @param length
     *            the length of data to write from the Reader
     * @throws SQLException
     *             if a database error happens
     */
    public void updateCharacterStream(String columnName, Reader reader,
            int length) throws SQLException;

    /**
     * Updates a column specified by a column index with a java.sql.Clob value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateClob(int columnIndex, Clob x) throws SQLException;

    /**
     * Updates a column specified by a column name with a java.sql.Clob value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateClob(String columnName, Clob x) throws SQLException;

    /**
     * Updates a column specified by a column index with a java.sql.Date value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateDate(int columnIndex, Date x) throws SQLException;

    /**
     * Updates a column specified by a column name with a java.sql.Date value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateDate(String columnName, Date x) throws SQLException;

    /**
     * Updates a column specified by a column index with a double value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateDouble(int columnIndex, double x) throws SQLException;

    /**
     * Updates a column specified by a column name with a double value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateDouble(String columnName, double x) throws SQLException;

    /**
     * Updates a column specified by a column index with a float value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateFloat(int columnIndex, float x) throws SQLException;

    /**
     * Updates a column specified by a column name with a float value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateFloat(String columnName, float x) throws SQLException;

    /**
     * Updates a column specified by a column index with an int value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateInt(int columnIndex, int x) throws SQLException;

    /**
     * Updates a column specified by a column name with an int value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateInt(String columnName, int x) throws SQLException;

    /**
     * Updates a column specified by a column index with a long value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateLong(int columnIndex, long x) throws SQLException;

    /**
     * Updates a column specified by a column name with a long value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateLong(String columnName, long x) throws SQLException;

    /**
     * Updates a column specified by a column index with a null value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @throws SQLException
     *             if a database error happens
     */
    public void updateNull(int columnIndex) throws SQLException;

    /**
     * Updates a column specified by a column name with a null value.
     * 
     * @param columnName
     *            the name of the column to update
     * @throws SQLException
     *             if a database error happens
     */
    public void updateNull(String columnName) throws SQLException;

    /**
     * Updates a column specified by a column index with an Object value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateObject(int columnIndex, Object x) throws SQLException;

    /**
     * Updates a column specified by a column index with an Object value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @param scale
     *            for the types java.sql.Types.DECIMAL or
     *            java.sql.Types.NUMERIC, this specifies the number of digits
     *            after the decimal point.
     * @throws SQLException
     *             if a database error happens
     */
    public void updateObject(int columnIndex, Object x, int scale)
            throws SQLException;

    /**
     * Updates a column specified by a column name with an Object value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateObject(String columnName, Object x) throws SQLException;

    /**
     * Updates a column specified by a column name with an Object value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @param scale
     *            for the types java.sql.Types.DECIMAL or
     *            java.sql.Types.NUMERIC, this specifies the number of digits
     *            after the decimal point.
     * @throws SQLException
     *             if a database error happens
     */
    public void updateObject(String columnName, Object x, int scale)
            throws SQLException;

    /**
     * Updates a column specified by a column index with a java.sql.Ref value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateRef(int columnIndex, Ref x) throws SQLException;

    /**
     * Updates a column specified by a column name with a java.sql.Ref value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateRef(String columnName, Ref x) throws SQLException;

    /**
     * Updates the database with the new contents of the current row of this
     * ResultSet object.
     * 
     * @throws SQLException
     */
    public void updateRow() throws SQLException;

    /**
     * Updates a column specified by a column index with a short value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateShort(int columnIndex, short x) throws SQLException;

    /**
     * Updates a column specified by a column name with a short value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateShort(String columnName, short x) throws SQLException;

    /**
     * Updates a column specified by a column index with a String value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateString(int columnIndex, String x) throws SQLException;

    /**
     * Updates a column specified by a column name with a String value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateString(String columnName, String x) throws SQLException;

    /**
     * Updates a column specified by a column index with a Time value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateTime(int columnIndex, Time x) throws SQLException;

    /**
     * Updates a column specified by a column name with a Time value.
     * 
     * @param columnName
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateTime(String columnName, Time x) throws SQLException;

    /**
     * Updates a column specified by a column index with a Timestamp value.
     * 
     * @param columnIndex
     *            the index of the column to update
     * @param x
     *            the new value for the specified column
     * @throws SQLException
     *             if a database error happens
     */
    public void updateTimestamp(int columnIndex, Timestamp x)
            throws SQLException;

    /**
     * Updates a column specified by column name with a Timestamp value.
     * 
     * @param columnName
     *            the name of the column to update
     * @param x
     * @throws SQLException
     *             if a database error happens
     */
    public void updateTimestamp(String columnName, Timestamp x)
            throws SQLException;

    /**
     * Determines if the last column read from this ResultSet contained SQL
     * NULL.
     * 
     * @return true if the last column contained SQL NULL, false otherwise
     * @throws SQLException
     *             if a database error happens
     */
    public boolean wasNull() throws SQLException;
}
