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
 * Interface used for executing static SQL statements and returning their
 * results.
 * 
 * By default, an object implementing the Statement interface can returns
 * results as ResultSets. For any given Statement object, only one ResultSet can
 * be open at one time. A call to any of the execution methods of Statement will
 * cause any previously created ResultSet object for that Statement to be closed
 * implicitly.
 * <p>
 * To have multiple ResultSet objects open concurrently, multiple Statement
 * objects must be used.
 */
public interface Statement {

    /**
     * Passing this constant to getMoreResults implies that all ResultSet
     * objects previously kept open should be closed.
     */
    public static final int CLOSE_ALL_RESULTS = 3;

    /**
     * Passing this constant to getMoreResults implies that the current
     * ResultSet object should be closed
     */
    public static final int CLOSE_CURRENT_RESULT = 1;

    /**
     * Indicates that an error was encountered during execution of a batch
     * statement.
     */
    public static final int EXECUTE_FAILED = -3;

    /**
     * Passing this constant to getMoreResults implies that the current
     * ResultSet object should not be closed.
     */
    public static final int KEEP_CURRENT_RESULT = 2;

    /**
     * Indicates that generated keys should not be accessible for retrieval.
     */
    public static final int NO_GENERATED_KEYS = 2;

    /**
     * Indicates that generated keys should be accessible for retrieval.
     */
    public static final int RETURN_GENERATED_KEYS = 1;

    /**
     * Indicates that a batch statement was executed with a successful result,
     * but a count of the number of rows it affected is unavailable.
     */
    public static final int SUCCESS_NO_INFO = -2;

    /**
     * Adds a specified SQL commands to the list of commands for this Statement.
     * <p>
     * The list of commands is executed by invoking the
     * <code>executeBatch</code> method.
     * 
     * @param sql
     *            the SQL command as a String. Typically an INSERT or UPDATE
     *            statement.
     * @throws SQLException
     *             if an error occurs accessing the database or the database
     *             does not support batch updates
     */
    public void addBatch(String sql) throws SQLException;

    /**
     * Cancels this Statement execution if both the database and the JDBC driver
     * support aborting an SQL statement in flight. This method can be used by
     * one thread to stop a Statement that is being executed on another thread.
     * 
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public void cancel() throws SQLException;

    /**
     * Clears the current list of SQL commands for this Statement.
     * 
     * @throws SQLException
     *             if an error occurs accessing the database or the database
     *             does not support batch updates
     */
    public void clearBatch() throws SQLException;

    /**
     * Clears all SQLWarnings from this Statement.
     * 
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public void clearWarnings() throws SQLException;

    /**
     * Releases this Statement's database and JDBC driver resources.
     * <p>
     * Using this method to release these resources as soon as possible is
     * strongly recommended. It is not a good idea to rely on these resources
     * being released when the Statement object is finalized during garbage
     * collection. Doing so can result in unpredictable performance
     * characteristics for the application.
     * 
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public void close() throws SQLException;

    /**
     * Executes a supplied SQL statement. This may return multiple ResultSets.
     * <p>
     * Use the <code>getResultSet</code> or <code>getUpdateCount</code>
     * methods to get the first result and <code>getMoreResults</code> to get
     * any subsequent results.
     * 
     * @param sql
     *            the SQL statement to execute
     * @return true if the first result is a ResultSet, false if the first
     *         result is an update count or if there is no result
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public boolean execute(String sql) throws SQLException;

    /**
     * Executes a supplied SQL statement. This may return multiple ResultSets.
     * This method allows control of whether auto-generated Keys should be made
     * available for retrieval, if the SQL statement is an INSERT statement.
     * <p>
     * Use the <code>getResultSet</code> or <code>getUpdateCount</code>
     * methods to get the first result and <code>getMoreResults</code> to get
     * any subsequent results.
     * 
     * @param sql
     *            the SQL statement to execute
     * @param autoGeneratedKeys
     *            a flag indicating whether to make auto generated keys
     *            available for retrieval. This parameter must be one of
     *            Statement.NO_GENERATED_KEYS or Statement.RETURN_GENERATED_KEYS
     * @return true if results exists and the first result is a ResultSet, false
     *         if the first result is an update count or if there is no result
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public boolean execute(String sql, int autoGeneratedKeys)
            throws SQLException;

    /**
     * Executes the supplied SQL statement. This may return multiple ResultSets.
     * This method allows retrieval of auto generated keys specified by the
     * supplied array of column indexes, if the SQL statement is an INSERT
     * statement.
     * <p>
     * Use the <code>getResultSet</code> or <code>getUpdateCount</code>
     * methods to get the first result and <code>getMoreResults</code> to get
     * any subsequent results.
     * 
     * @param sql
     *            the SQL statement to execute
     * @param columnIndexes
     *            an array of indexes of the columns in the inserted row which
     *            should be made available for retrieval via the
     *            <code>getGeneratedKeys</code> method.
     * @return true if the first result is a ResultSet, false if the first
     *         result is an update count or if there is no result
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public boolean execute(String sql, int[] columnIndexes) throws SQLException;

    /**
     * Executes the supplied SQL statement. This may return multiple ResultSets.
     * This method allows retrieval of auto generated keys specified by the
     * supplied array of column indexes, if the SQL statement is an INSERT
     * statement.
     * <p>
     * Use the <code>getResultSet</code> or <code>getUpdateCount</code>
     * methods to get the first result and <code>getMoreResults</code> to get
     * any subsequent results.
     * 
     * @param sql
     *            the SQL statement to execute
     * @param columnNames
     *            an array of column names in the inserted row which should be
     *            made available for retrieval via the
     *            <code>getGeneratedKeys</code> method.
     * @return true if the first result is a ResultSet, false if the first
     *         result is an update count or if there is no result
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public boolean execute(String sql, String[] columnNames)
            throws SQLException;

    /**
     * Submits a batch of SQL commands to the database. Returns an array of
     * update counts, if all the commands execute successfully.
     * <p>
     * If one of the commands in the batch fails, this method can throw a
     * BatchUpdateException and the JDBC driver may or may not process the
     * remaining commands. The JDBC driver must behave consistently with the
     * underlying database, either always continuing or never continuing. If the
     * driver continues processing, the array of results returned contains the
     * same number of elements as there are commands in the batch, with a
     * minimum of one of the elements having the EXECUTE_FAILED value.
     * 
     * @return an array of update counts, with one entry for each command in the
     *         batch. The elements are ordered according to the order in which
     *         the commands were added to the batch.
     *         <p>
     *         <ol>
     *         <li> If the value of an element is >=0, the corresponding command
     *         completed successfully and the value is the update count for that
     *         command, which is the number of rows in the database affected by
     *         the command.</li>
     *         <li> If the value is SUCCESS_NO_INFO, the command completed
     *         successfully but the number of rows affected is unknown.
     *         <li>
     *         <li> If the value is EXECUTE_FAILED, the command failed.
     *         </ol>
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public int[] executeBatch() throws SQLException;

    /**
     * Executes a supplied SQL statement. Returns a single ResultSet.
     * 
     * @param sql
     *            an SQL statement to execute. Typically a SELECT statement
     * @return a ResultSet containing the data produced by the SQL statement.
     *         Never null.
     * @throws SQLException
     *             if an error occurs accessing the database or if the statement
     *             produces anything other than a single ResultSet
     */
    public ResultSet executeQuery(String sql) throws SQLException;

    /**
     * Executes the supplied SQL statement. The statement may be an INSERT,
     * UPDATE or DELETE statement or a statement which returns nothing.
     * 
     * @param sql
     *            an SQL statement to execute - an SQL INSERT, UPDATE, DELETE or
     *            a statement which returns nothing
     * @return the count of updated rows, or 0 for a statement that returns
     *         nothing.
     * @throws SQLException
     *             if an error occurs accessing the database or if the statement
     *             produces a ResultSet
     */
    public int executeUpdate(String sql) throws SQLException;

    /**
     * Executes the supplied SQL statement. This method allows control of
     * whether auto-generated Keys should be made available for retrieval.
     * 
     * @param sql
     *            an SQL statement to execute - an SQL INSERT, UPDATE, DELETE or
     *            a statement which does not return anything.
     * @param autoGeneratedKeys
     *            a flag that indicates whether to allow retrieval of auto
     *            generated keys. Parameter must be one of
     *            Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
     * @return the number of updated rows, or 0 if the statement returns
     *         nothing.
     * @throws SQLException
     *             if an error occurs accessing the database or if the statement
     *             produces a ResultSet
     */
    public int executeUpdate(String sql, int autoGeneratedKeys)
            throws SQLException;

    /**
     * Executes the supplied SQL statement. This method allows retrieval of auto
     * generated keys specified by the supplied array of column indexes.
     * 
     * @param sql
     *            an SQL statement to execute - an SQL INSERT, UPDATE, DELETE or
     *            a statement which returns nothing
     * @param columnIndexes
     *            an array of indexes of the columns in the inserted row which
     *            should be made available for retrieval via the
     *            <code>getGeneratedKeys</code> method.
     * @return the count of updated rows, or 0 for a statement that returns
     *         nothing.
     * @throws SQLException
     *             if an error occurs accessing the database or if the statement
     *             produces a ResultSet
     */
    public int executeUpdate(String sql, int[] columnIndexes)
            throws SQLException;

    /**
     * Executes the supplied SQL statement. This method allows retrieval of auto
     * generated keys specified by the supplied array of column names.
     * 
     * @param sql
     *            an SQL statement to execute - an SQL INSERT, UPDATE, DELETE or
     *            a statement which returns nothing
     * @param columnNames
     *            an array of column names in the inserted row which should be
     *            made available for retrieval via the
     *            <code>getGeneratedKeys</code> method.
     * @return the count of updated rows, or 0 for a statement that returns
     *         nothing.
     * @throws SQLException
     *             if an error occurs accessing the database or if the statement
     *             produces a ResultSet
     */
    public int executeUpdate(String sql, String[] columnNames)
            throws SQLException;

    /**
     * Gets the Connection that produced this Statement.
     * 
     * @return the Connection
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public Connection getConnection() throws SQLException;

    /**
     * Gets the default direction for fetching rows for ResultSets generated
     * from this Statement.
     * 
     * @return an integer describing the default fetch direction, one of:
     *         ResultSet.FETCH_FORWARD, ResultSet.FETCH_REVERSE,
     *         ResultSet.FETCH_UNKNOWN
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public int getFetchDirection() throws SQLException;

    /**
     * Gets the default number of rows for a fetch for the ResultSet objects
     * returned from this Statement.
     * 
     * @return the default fetch size for ResultSets produced by this Statement
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public int getFetchSize() throws SQLException;

    /**
     * Returns auto generated keys created by executing this Statement.
     * 
     * @return a ResultSet containing the auto generated keys - empty if no keys
     *         were generated by the Statement
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public ResultSet getGeneratedKeys() throws SQLException;

    /**
     * Gets the maximum number of bytes which can be returned for values from
     * Character and Binary values in a ResultSet derived from this Statement.
     * This limit applies to BINARY, VARBINARY, LONGVARBINARY, CHAR, VARCHAR,
     * and LONGVARCHAR types. Any data exceeding the maximum size is abandoned
     * without announcement.
     * 
     * @return the current size limit, where 0 means that there is no limit
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public int getMaxFieldSize() throws SQLException;

    /**
     * Gets the maximum number of rows that a ResultSet can contain when
     * produced from this Statement. If the limit is exceeded, the excess rows
     * are discarded silently.
     * 
     * @return the current row limit, where 0 means that there is no limit.
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public int getMaxRows() throws SQLException;

    /**
     * Moves to this Statement's next result. Returns true if it is a ResultSet.
     * Any current ResultSet objects previously obtained with
     * <code>getResultSet()</code> are closed implicitly.
     * 
     * @return true if the next result is a ResultSet, false if the next result
     *         is not a ResultSet or if there are no more results. Note that if
     *         there is no more data, this method will return false and
     *         <code>getUpdateCount</code> will return -1.
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public boolean getMoreResults() throws SQLException;

    /**
     * Moves to this Statement's next result. Returns true if the next result is
     * a ResultSet. Any current ResultSet objects previously obtained with
     * <code>getResultSet()</code> are handled as indicated by a supplied Flag
     * parameter.
     * 
     * @param current
     *            a flag indicating what to do with existing ResultSets. This
     *            parameter must be one of Statement.CLOSE_ALL_RESULTS,
     *            Statement.CLOSE_CURRENT_RESULT or
     *            Statement.KEEP_CURRENT_RESULT.
     * @return true if the next result exists and is a ResultSet, false if the
     *         next result is not a ResultSet or if there are no more results.
     *         Note that if there is no more data, this method will return false
     *         and <code>getUpdateCount</code> will return -1.
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public boolean getMoreResults(int current) throws SQLException;

    /**
     * Gets the timeout value for Statement execution. The JDBC driver will wait
     * up to this value for the execution to complete - after the limit is
     * exceeded an SQL Exception is thrown.
     * 
     * @return the current Query Timeout value, where 0 indicates that there is
     *         no current timeout.
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public int getQueryTimeout() throws SQLException;

    /**
     * Gets the current result. Should only be called once per result.
     * 
     * @return the ResultSet for the current result. null if the result is an
     *         update count or if there are no more results.
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public ResultSet getResultSet() throws SQLException;

    /**
     * Gets the concurrency setting for ResultSet objects generated by this
     * Statement.
     * 
     * @return ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public int getResultSetConcurrency() throws SQLException;

    /**
     * Gets the cursor hold setting for ResultSet objects generated by this
     * Statement.
     * 
     * @return ResultSet.HOLD_CURSORS_OVER_COMMIT or
     *         ResultSet.CLOSE_CURSORS_AT_COMMIT
     * @throws SQLException
     *             if there is an error while accessing the database
     */
    public int getResultSetHoldability() throws SQLException;

    /**
     * Gets the ResultSet type setting for ResultSets derived from this
     * Statement.
     * 
     * @return ResultSet.TYPE_FORWARD_ONLY for a ResultSet where the cursor can
     *         only move forward, ResultSet.TYPE_SCROLL_INSENSITIVE for a
     *         ResultSet which is Scrollable but is not sensitive to changes
     *         made by others, ResultSet.TYPE_SCROLL_SENSITIVE for a ResultSet
     *         which is Scrollable but is sensitive to changes made by others
     * @throws SQLException
     *             if there is an error accessing the database
     */
    public int getResultSetType() throws SQLException;

    /**
     * Gets an update count for the current result if it is not a ResultSet.
     * 
     * @return the current result as an update count. -1 if the current result
     *         is a ResultSet or if there are no more results
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public int getUpdateCount() throws SQLException;

    /**
     * Retrieves the first SQLWarning reported by calls on this Statement.
     * <p>
     * If there are multiple warnings, subsequent warnings are chained to the
     * first one.
     * <p>
     * The chain or warnings is cleared each time the Statement is executed.
     * <p>
     * Warnings associated with reads from the ResultSet returned from executing
     * a Statement will be attached to the ResultSet, not the Statement object.
     * 
     * @return an SQLWarning, null if there are no warnings
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public SQLWarning getWarnings() throws SQLException;

    /**
     * Sets the SQL cursor name. This name is used by subsequent Statement
     * execute methods.
     * <p>
     * Cursor names must be unique within one Connection.
     * <p>
     * With the Cursor name set, it can then be utilized in SQL positioned
     * update or delete statements to determine the current row in a ResultSet
     * generated from this Statement. The positioned update or delete must be
     * done with a different Statement than this one.
     * 
     * @param name
     *            the Cursor name as a String,
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public void setCursorName(String name) throws SQLException;

    /**
     * Sets Escape Processing mode.
     * <p>
     * If Escape Processing is on, the JDBC driver will do escape substitution
     * on an SQL statement before sending it for execution. This does not apply
     * to PreparedStatements since they are processed when created, before this
     * method can be called.
     * 
     * @param enable
     *            true to set escape processing mode on, false to turn it off.
     * @throws SQLException
     *             if an error occurs accessing the database
     */
    public void setEscapeProcessing(boolean enable) throws SQLException;

    /**
     * Sets the fetch direction - a hint to the JDBC driver about the direction
     * of processing of rows in ResultSets created by this Statement. The
     * default fetch direction is FETCH_FORWARD.
     * 
     * @param direction
     *            which fetch direction to use. This parameter should be one of
     *            ResultSet.FETCH_UNKNOWN, ResultSet.FETCH_FORWARD or
     *            ResultSet.FETCH_REVERSE
     * @throws SQLException
     *             if there is an error while accessing the database or if the
     *             fetch direction is unrecognized
     */
    public void setFetchDirection(int direction) throws SQLException;

    /**
     * Sets the fetch size. This is a hint to the JDBC driver about how many
     * rows should be fetched from the database when more are required by
     * application processing.
     * 
     * @param rows
     *            the number of rows that should be fetched. 0 tells the driver
     *            to ignore the hint. Should be less than
     *            <code>getMaxRows</code> for this statement. Should not be
     *            negative.
     * @throws SQLException
     *             if an error occurs accessing the database, or if the rows
     *             parameter is out of range.
     */
    public void setFetchSize(int rows) throws SQLException;

    /**
     * Sets the maximum number of bytes for ResultSet columns that contain
     * character or binary values. This applies to BINARY, VARBINARY,
     * LONGVARBINARY, CHAR, VARCHAR, and LONGVARCHAR fields. Any data exceeding
     * the maximum size is abandoned without announcement.
     * 
     * @param max
     *            the maximum field size in bytes. O means "no limit".
     * @throws SQLException
     *             if an error occurs accessing the database or the max value is
     *             <0.
     */
    public void setMaxFieldSize(int max) throws SQLException;

    /**
     * Sets the maximum number of rows that any ResultSet can contain. If the
     * number of rows exceeds this value, the additional rows are silently
     * discarded.
     * 
     * @param max
     *            the maximum number of rows. 0 means "no limit".
     * @throws SQLException
     *             if an error occurs accessing the database or if max <0.
     */
    public void setMaxRows(int max) throws SQLException;

    /**
     * Sets the timeout, in seconds, for queries - how long the driver will
     * allow for completion of a Statement execution. If the timeout is
     * exceeded, the query will throw an SQLException.
     * 
     * @param seconds
     *            timeout in seconds. 0 means no timeout ("wait forever")
     * @throws SQLException
     *             if an error occurs accessing the database or if seconds <0.
     */
    public void setQueryTimeout(int seconds) throws SQLException;
}
