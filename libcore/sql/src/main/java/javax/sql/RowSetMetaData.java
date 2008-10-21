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

package javax.sql;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * An interface which provides facilities for getting information about the
 * columns in a RowSet.
 * <p>
 * RowSetMetaData extends ResultSetMetaData, adding new operations for carrying
 * out value sets.
 * <p>
 * Application code would not normally call this interface directly. It would be
 * called internally when <code>RowSet.execute</code> is called.
 */
public interface RowSetMetaData extends ResultSetMetaData {

    /**
     * Sets automatic numbering for a specified column in the RowSet. If
     * automatic numbering is on, the column is read only. The default value is
     * for automatic numbering to be off.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param autoIncrement
     *            true to set automatic numbering on, false to turn it off.
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setAutoIncrement(int columnIndex, boolean autoIncrement)
            throws SQLException;

    /**
     * Sets the case sensitive property for a specified column in the RowSet.
     * The default is that the column is not case sensitive.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param caseSensitive
     *            true to make the column case sensitive, false to make it not
     *            case sensitive.
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setCaseSensitive(int columnIndex, boolean caseSensitive)
            throws SQLException;

    /**
     * Sets the Catalog Name for a specified column in the RowSet.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param catalogName
     *            a string containing the new Catalog Name
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setCatalogName(int columnIndex, String catalogName)
            throws SQLException;

    /**
     * Sets the number of columns in the Row Set.
     * 
     * @param columnCount
     *            an integer containing the number of columns in the RowSet.
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setColumnCount(int columnCount) throws SQLException;

    /**
     * Sets the normal maximum width in characters for a specified column in the
     * RowSet.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param displaySize
     *            an integer with the normal maximum column width in characters
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setColumnDisplaySize(int columnIndex, int displaySize)
            throws SQLException;

    /**
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param theLabel
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setColumnLabel(int columnIndex, String theLabel)
            throws SQLException;

    /**
     * Sets the suggested column label for a specified column in the RowSet.
     * This label is typically used in displaying or printing the column.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param theColumnName
     *            a string containing the column label
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setColumnName(int columnIndex, String theColumnName)
            throws SQLException;

    /**
     * Sets the SQL type for a specified column in the RowSet
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param theSQLType
     *            an integer containing the SQL Type, as defined by
     *            java.sql.Types.
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setColumnType(int columnIndex, int theSQLType)
            throws SQLException;

    /**
     * Sets the Type Name for a specified column in the RowSet, where the data
     * type is specific to the datasource.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param theTypeName
     *            a string containing the Type Name for the column
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setColumnTypeName(int columnIndex, String theTypeName)
            throws SQLException;

    /**
     * Sets whether a specified column is a currency value.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param isCurrency
     *            true if the column should be treated as a currency value,
     *            false if it should not be treated as a currency value.
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setCurrency(int columnIndex, boolean isCurrency)
            throws SQLException;

    /**
     * Sets whether a specified column can contain SQL NULL values.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param nullability
     *            an integer which is one of the following values:
     *            ResultSetMetaData.columnNoNulls,
     *            ResultSetMetaData.columnNullable, or
     *            ResultSetMetaData.columnNullableUnknown
     *            <p>
     *            The default value is ResultSetMetaData.columnNullableUnknown
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setNullable(int columnIndex, int nullability)
            throws SQLException;

    /**
     * Sets the number of decimal digits for a specified column in the RowSet.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param thePrecision
     *            an integer containing the number of decimal digits
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setPrecision(int columnIndex, int thePrecision)
            throws SQLException;

    /**
     * For the column specified by <code>columnIndex</code> declares how many
     * digits there should be after a decimal point.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param theScale
     *            an integer containing the number of digits after the decimal
     *            point
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setScale(int columnIndex, int theScale) throws SQLException;

    /**
     * Sets the Schema Name for a specified column in the RowSet
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param theSchemaName
     *            a String containing the schema name
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setSchemaName(int columnIndex, String theSchemaName)
            throws SQLException;

    /**
     * Sets whether a specified column can be used in a search involving a WHERE
     * clause. The default value is false.
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param isSearchable
     *            true of the column can be used in a WHERE clause search, false
     *            otherwise.
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setSearchable(int columnIndex, boolean isSearchable)
            throws SQLException;

    /**
     * Sets if a specified column can contain signed numbers
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param isSigned
     *            true if the column can contain signed numbers, false otherwise
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setSigned(int columnIndex, boolean isSigned)
            throws SQLException;

    /**
     * Sets the Table Name for a specified column in the RowSet
     * 
     * @param columnIndex
     *            the index number for the column, where the first column has
     *            index 1.
     * @param theTableName
     *            a String containing the Table Name for the column
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public void setTableName(int columnIndex, String theTableName)
            throws SQLException;
}
