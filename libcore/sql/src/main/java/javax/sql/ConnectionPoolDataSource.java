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
import java.io.PrintWriter;

/**
 * An interface for the creation of {@code ConnectionPoolDataSource} objects.
 * Used internally within the package.
 * <p>
 * A class which implements the {@code ConnectionPoolDataSource} interface is
 * typically registered with a JNDI naming service directory and is retrieved
 * from there by name.
 * </p>
 * 
 * @since Android 1.0
 */
public interface ConnectionPoolDataSource {

    /**
     * Gets the login timeout value for this {@code ConnectionPoolDataSource}.
     * The login timeout is the maximum time in seconds that the {@code
     * ConnectionPoolDataSource} will wait when opening a connection to a
     * database. A timeout value of 0 implies either the system default timeout
     * value (if there is one) or that there is no timeout. The default value
     * for the login timeout is {@code 0}.
     * 
     * @return the login timeout value in seconds.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public int getLoginTimeout() throws SQLException;

    /**
     * Gets the log writer for this {@code ConnectionPoolDataSource}.
     * <p>
     * The log writer is a stream to which all log and trace messages are sent
     * from this {@code ConnectionPoolDataSource}. The log writer can be {@code
     * null}, in which case the log and trace capture is disabled. The default
     * value for the log writer when an {@code ConnectionPoolDataSource} is
     * created is {@code null}. Note that the log writer for an {@code
     * ConnectionPoolDataSource} is not the same as the log writer used by a
     * {@code DriverManager}.
     * </p>
     * 
     * @return a {@code PrintWriter} which is the log writer for this {@code
     *         ConnectionPoolDataSource}. Can be {@code null}, in which case log
     *         writing is disabled for this {@code ConnectionPoolDataSource}.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public PrintWriter getLogWriter() throws SQLException;

    /**
     * Creates a connection to a database which can then be used as a pooled
     * connection.
     * 
     * @return a {@code PooledConnection} which represents the connection to the
     *         database.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public PooledConnection getPooledConnection() throws SQLException;

    /**
     * Creates a connection to a database, using the supplied user name and
     * password, which can then be used as a pooled connection.
     * 
     * @param theUser
     *            the a user name for the database login.
     * @param thePassword
     *            the password associated with the user identified by {@code
     *            theUser}.
     * @return a {@code PooledConnection} object which represents the connection
     *         to the database.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public PooledConnection getPooledConnection(String theUser,
            String thePassword) throws SQLException;

    /**
     * Sets the login timeout value for this {@code ConnectionPoolDataSource}.
     * The login timeout is the maximum time in seconds that the {@code
     * ConnectionPoolDataSource} will wait when opening a connection to a
     * database. A timeout value of 0 implies either the system default timeout
     * value (if there is one) or that there is no timeout. The default value
     * for the login timeout is 0.
     * 
     * @param theTimeout
     *            the new login timeout value in seconds.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public void setLoginTimeout(int theTimeout) throws SQLException;

    /**
     * Sets the log writer for this {@code ConnectionPoolDataSource}.
     * <p>
     * The log writer is a stream to which all log and trace messages are sent
     * from this {@code ConnectionPoolDataSource}. The log writer can be {@code
     * null}, in which case log and trace capture is disabled. The default value
     * for the log writer, when a {@code ConnectionPoolDataSource} is created,
     * is {@code null}. Note that the log writer for a {@code
     * ConnectionPoolDataSource} is not the same as the log writer used by a
     * {@code DriverManager}.
     * </p>
     * 
     * @param theWriter
     *            is the log writer for this {@code ConnectionPoolDataSource}.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public void setLogWriter(PrintWriter theWriter) throws SQLException;
}
