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
 * An interface for the creation of PooledConnection objects. Used internally
 * within the package.
 * <p>
 * A class which implements the ConnectionPoolDataSource interface is typically
 * registered with a JNDI naming service directory and is retrieved from there
 * by name.
 */
public interface ConnectionPoolDataSource {

    /**
     * Gets the Login Timeout value for this ConnectionPoolDataSource. The Login
     * Timeout is the maximum time in seconds that the ConnectionPoolDataSource
     * will wait when opening a connection to a database. A Timeout value of 0
     * implies either the system default timeout value (if there is one) or that
     * there is no timeout. The default value for the Login Timeout is 0.
     * 
     * @return the Login Timeout value in seconds.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public int getLoginTimeout() throws SQLException;

    /**
     * Gets the Log Writer for this ConnectionPoolDataSource.
     * <p>
     * The Log Writer is a stream to which all log and trace messages are sent
     * from this ConnectionPoolDataSource. The Log Writer can be null, in which
     * case, log and trace capture is disabled. The default value for the Log
     * Writer when an ConnectionPoolDataSource is created is null. Note that the
     * Log Writer for an ConnectionPoolDataSource is not the same as the Log
     * Writer used by a <code>DriverManager</code>.
     * 
     * @return a PrintWriter which is the Log Writer for this
     *         ConnectionPoolDataSource. Can be null, in which case log writing
     *         is disabled for this ConnectionPoolDataSource.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public PrintWriter getLogWriter() throws SQLException;

    /**
     * Create a connection to a database which can then be used as a pooled
     * connection.
     * 
     * @return a PooledConnection which represents the connection to the
     *         database
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public PooledConnection getPooledConnection() throws SQLException;

    /**
     * Create a connection to a database, using a supplied Username and
     * Password, which can then be used as a pooled connection.
     * 
     * @param theUser
     *            a String containing a User Name for the database
     * @param thePassword
     *            a String containing the Password for the user identified by
     *            <code>theUser</code>
     * @return a PooledConnection which represents the connection to the
     *         database
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public PooledConnection getPooledConnection(String theUser,
            String thePassword) throws SQLException;

    /**
     * Sets the Login Timeout value for this ConnectionPoolDataSource. The Login
     * Timeout is the maximum time in seconds that the ConnectionPoolDataSource
     * will wait when opening a connection to a database. A Timeout value of 0
     * implies either the system default timeout value (if there is one) or that
     * there is no timeout. The default value for the Login Timeout is 0.
     * 
     * @param theTimeout
     *            the new Login Timeout value in seconds.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public void setLoginTimeout(int theTimeout) throws SQLException;

    /**
     * Sets the Log Writer for this ConnectionPoolDataSource.
     * <p>
     * The Log Writer is a stream to which all log and trace messages are sent
     * from this ConnectionPoolDataSource. The Log Writer can be null, in which
     * case, log and trace capture is disabled. The default value for the Log
     * Writer when an ConnectionPoolDataSource is created is null. Note that the
     * Log Writer for an ConnectionPoolDataSource is not the same as the Log
     * Writer used by a <code>DriverManager</code>.
     * 
     * @param theWriter
     *            a PrintWriter to use as the Log Writer for this
     *            ConnectionPoolDataSource.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public void setLogWriter(PrintWriter theWriter) throws SQLException;
}
