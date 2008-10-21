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

/**
 * Provides information about the columns in a ResultSet.
 */
public interface ResultSetMetaData {

    /**
     * Indicates that a column cannot contain NULL values
     */
    public static final int columnNoNulls = 0;

    /**
     * Indicates that a column can contain NULL values
     */
    public static final int columnNullable = 1;

    /**
     * Indicates that it is unknown whether a column can contain NULLs or not
     */
    public static final int columnNullableUnknown = 2;

    /**
     * Returns the title of indexed columns catalog
     * 
     * @param column
     *            the column index, starting at 1
     * @return the catalog title
     * @throws SQLException
     *             if there is a database error
     */
    public String getCatalogName(int column) throws SQLException;

    /**
     * Returns the fully-qualified type of the class that is produced when
     * invoking ResultSet.getObject to recover this columns value.
     * 
     * @param column
     *            the column index, starting at 1
     * @return the fully-qualified class name
     * @throws SQLException
     *             if there is a database error
     */
    public String getColumnClassName(int column) throws SQLException;

    /**
     * Returns a count of the columns in this set of results.
     * 
     * @return the column count
     * @throws SQLException
     *             if there is a database error
     */
    public int getColumnCount() throws SQLException;

    /**
     * Returns the indexed column's standard maximum width, expressed in number
     * of characters.
     * 
     * @param column
     *            the column index, starting at 1
     * @return the column's max width
     * @throws SQLException
     *             if there is a database error
     */
    public int getColumnDisplaySize(int column) throws SQLException;

    /**
     * Returns a recommended title for the indexed column, to be used when the
     * title needs to be displayed.
     * 
     * @param column
     *            the column index, starting at 1
     * @return the column's title
     * @throws SQLException
     *             if there is a database error
     */
    public String getColumnLabel(int column) throws SQLException;

    /**
     * Returns the title of the indexed column
     * 
     * @param column
     *            the column index, starting at 1
     * @return the column title
     * @throws SQLException
     *             if there is a database error
     */
    public String getColumnName(int column) throws SQLException;

    /**
     * Returns the type of the indexed column
     * 
     * @param column
     *            the column index, starting at 1
     * @return the column type
     * @throws SQLException
     *             if there is a database error
     */
    public int getColumnType(int column) throws SQLException;

    /**
     * Returns the type name of the indexed column
     * 
     * @param column
     *            the column index, starting at 1
     * @return the type name
     * @throws SQLException
     *             if there is a database error
     */
    public String getColumnTypeName(int column) throws SQLException;

    /**
     * Returns the decimal precision of the indexed column
     * 
     * @param column
     *            the column index, starting at 1
     * @return the precision
     * @throws SQLException
     *             if there is a database error
     */
    public int getPrecision(int column) throws SQLException;

    /**
     * Returns the number of decimal places in the indexed column.
     * 
     * @param column
     *            the column index, starting at 1
     * @return number of decimal places
     * @throws SQLException
     *             if there is a database error
     */
    public int getScale(int column) throws SQLException;

    /**
     * Returns the name of the indexed columns schema
     * 
     * @param column
     *            the column index, starting at 1
     * @return the name of the columns schema
     * @throws SQLException
     *             if there is a database error
     */
    public String getSchemaName(int column) throws SQLException;

    /**
     * Returns the title of the indexed columns table.
     * 
     * @param column
     *            the column index, starting at 1
     * @return the table title
     * @throws SQLException
     *             if there is a database error
     */
    public String getTableName(int column) throws SQLException;

    /**
     * Returns and indication of whether the indexed column has automatic
     * numbering and is therefore read-only
     * 
     * @param column
     *            the column index, starting at 1
     * @return true if it is automatically numbered, false otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean isAutoIncrement(int column) throws SQLException;

    /**
     * Returns an indicator of whether the case of the indexed column is
     * important
     * 
     * @param column
     *            the column index, starting at 1
     * @return true if case matters, false otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean isCaseSensitive(int column) throws SQLException;

    /**
     * Returns if the indexed column contains a monetary amount.
     * 
     * @param column
     *            the column index, starting at 1
     * @return true if it is a monetary value, false otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean isCurrency(int column) throws SQLException;

    /**
     * Returns an indication of whether writing to the indexed column is
     * guaranteed to be successful
     * 
     * @param column
     *            the column index, starting at 1
     * @return true if the write is guaranteed, false otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean isDefinitelyWritable(int column) throws SQLException;

    /**
     * Returns whether the indexed column is nullable.
     * 
     * @param column
     *            the column index, starting at 1
     * @return true if it is nullable, false otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public int isNullable(int column) throws SQLException;

    /**
     * Returns an indication of whether writing to the indexed column is
     * guaranteed to be unsuccessful
     * 
     * @param column
     *            the column index, starting at 1
     * @return true if the column is read-only, false otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean isReadOnly(int column) throws SQLException;

    /**
     * Returns an indication of whether the indexed column is searchable.
     * 
     * @param column
     *            the column index, starting at 1
     * @return true if the indexed column is searchable, false otherwise.
     * @throws SQLException
     *             if there is a database error
     */
    public boolean isSearchable(int column) throws SQLException;

    /**
     * Returns an indicator of whether the values contained in the indexed
     * column are signed.
     * 
     * @param column
     *            the column index, starting at 1
     * @return true if they are signed, false otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean isSigned(int column) throws SQLException;

    /**
     * Returns an indication of whether writing to the indexed column is
     * possible.
     * 
     * @param column
     *            the column index, starting at 1
     * @return true if it is possible to write, false otherwise
     * @throws SQLException
     *             if there is a database error
     */
    public boolean isWritable(int column) throws SQLException;
}
