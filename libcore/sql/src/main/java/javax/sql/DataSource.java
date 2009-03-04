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
import java.io.PrintWriter;

/**
 * An interface for the creation of {@code Connection} objects which represent a
 * connection to a database. This interface is an alternative to the {@code
 * java.sql.DriverManager}.
 * <p>
 * A class which implements the {@code DataSource} interface is typically
 * registered with a JNDI naming service directory and is retrieved from there
 * by name.
 * </p>
 * <p>
 * The {@code DataSource} interface is typically implemented by the writer of a
 * JDBC driver. There are three variants of the {@code DataSource} interface,
 * which produce connections with different characteristics:
 * </p>
 * <ol>
 * <li><i>Standard {@code DataSource}</i>: produces standard {@code Connection}
 * objects with no special features.</li>
 * <li><i>Connection Pool {@code DataSource}</i>: produces {@code
 * PooledConnection} objects which require a connection pool manager as an
 * intermediary component.</li>
 * <li><i>Distributed transaction {@code DataSource} ("XADataSource")</i>:
 * produces {@code XAConnection} objects which can be used to handle distributed
 * transactions which typically require an intermediary transaction manager
 * component. {@code XAConnection} objects also provide connection pooling
 * capabilities as well as distributed transaction capabilities.</li>
 * </ol>
 * <p>
 * Note that a JDBC driver which is accessed via the {@code DataSource}
 * interface is loaded via a JNDI lookup process. A driver loaded in this way
 * does not register itself with the {@code DriverManager}.
 * </p>
 * 
 * @since Android 1.0
 */
public interface DataSource {

    /**
     * Creates a connection to the database represented by this {@code
     * DataSource}.
     * 
     * @return a {@code Connection} object which is a connection to the
     *         database.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public Connection getConnection() throws SQLException;

    /**
     * Creates a connection to the database represented by this {@code
     * DataSource}, using the supplied user name and password.
     * 
     * @param theUsername
     *            the a user name for the database login.
     * @param thePassword
     *            the password associated with the user identified by {@code
     *            theUsername}.
     * @return the {@code Connection} object which is the connection to the
     *         database.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public Connection getConnection(String theUsername, String thePassword)
            throws SQLException;

    /**
     * Gets the login timeout value for this {@code DataSource}. The login
     * timeout is the maximum time in seconds that the {@code DataSource} will
     * wait when opening a connection to a database. A timeout value of 0
     * implies either the system default timeout value (if there is one) or that
     * there is no timeout. The default value for the login timeout is 0.
     * 
     * @return the login timeout value in seconds.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public int getLoginTimeout() throws SQLException;

    /**
     * Gets the log writer for this {@code DataSource}.
     * <p>
     * The log writer is a stream to which all log and trace messages are sent
     * from this {@code DataSource}. The log writer can be {@code null}, in
     * which case, log and trace capture is disabled. The default value for the
     * log writer when an {@code DataSource} is created is {@code null}. Note
     * that the log writer for a {@code DataSource} is not the same as the log
     * writer used by a {@code DriverManager}.
     * </p>
     * 
     * @return a {@code PrintWriter} which is the log writer for this {@code
     *         DataSource}. Can be {@code null}, in which case log writing is
     *         disabled for this {@code DataSource}.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public PrintWriter getLogWriter() throws SQLException;

    /**
     * Sets the login timeout value for this {@code DataSource}. The login
     * timeout is the maximum time in seconds that the {@code DataSource} will
     * wait when opening a connection to a database. A timeout value of 0
     * implies either the system default timeout value (if there is one) or that
     * there is no timeout. The default value for the login timeout is 0.
     * 
     * @param theTimeout
     *            the new login timeout value in seconds.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public void setLoginTimeout(int theTimeout) throws SQLException;

    /**
     * Sets the log writer for this {@code DataSource}.
     * <p>
     * The log writer is a stream to which all log and trace messages are sent
     * from this {@code DataSource}. The log writer can be {@code null}, in
     * which case, log and trace capture is disabled. The default value for the
     * log writer when a {@code DataSource} is created is {@code null}. Note
     * that the log writer for a {@code DataSource} is not the same as the log
     * writer used by a {@code DriverManager}.
     * </p>
     * 
     * @param theWriter
     *            a {@code PrintWriter} to use as the log writer for this
     *            {@code DataSource}.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @since Android 1.0
     */
    public void setLogWriter(PrintWriter theWriter) throws SQLException;
}
