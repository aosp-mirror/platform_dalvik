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

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 * An interface provided by a RowSet object to either a RowSetReader or a
 * RowSetWriter, providing facilities to read and update the internal state of
 * the RowSet.
 */
public interface RowSetInternal {

    /**
     * Gets the Connection associated with this RowSet object.
     * 
     * @return the Connection
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public Connection getConnection() throws SQLException;

    /**
     * Gets the ResultSet that was the original (unmodified) content of the
     * RowSet.
     * <p>
     * The ResultSet cursor is positioned before the first row of data
     * 
     * @return the ResultSet that contained the original data value of the
     *         RowSet
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public ResultSet getOriginal() throws SQLException;

    /**
     * Gets the original value of the current row only. If the current row did
     * not have an original value, then an empty value is returned.
     * 
     * @return a ResultSet containing the value of the current row only.
     * @throws SQLException
     *             if there is a problem accessing the database, or if the
     *             cursor is not on a valid row (before first, after last or
     *             pointing to the insert row).
     */
    public ResultSet getOriginalRow() throws SQLException;

    /**
     * Gets the parameter values that have been set for this RowSet's command.
     * 
     * @return an Object array containing the values of parameters that have
     *         been set.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public Object[] getParams() throws SQLException;

    /**
     * Sets RowSetMetaData for this RowSet. The RowSetMetaData is used by a
     * RowSetReader to set values giving information about the RowSet's columns.
     * 
     * @param theMetaData
     *            a RowSetMetaData holding the metadata about the RowSet's
     *            columns.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public void setMetaData(RowSetMetaData theMetaData) throws SQLException;
}
