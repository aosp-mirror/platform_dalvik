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

/**
 * An interface which provides facilities for handling connections to a database
 * which are pooled.
 * <p>
 * Typically, a PooledConnection is recycled when it is no longer required by an
 * application, rather than being closed and discarded. The reason for treating
 * connections in this way is that it can be an expensive process both to
 * establish a connection to a database and to destroy the connection. Reusing
 * connections through a pool is a way of improving system performance and
 * reducing overhead.
 * <p>
 * It is not intended that an application use the PooledConnection interface
 * directly. The PooledConnection interface is intended for use by a component
 * called a Connection Pool Manager, typically part of the infrastructure that
 * supports use of the database by applications.
 * <p>
 * Applications obtain connections to the database by calling the
 * <code>DataSource.getConnection</code> method. Under the covers, the
 * Connection Pool Manager will get a PooledConnection object from its
 * connection pool and passes back a Connection object that wraps or references
 * the PooledConnection object. A new PooledConnection object will only be
 * created if the pool is empty.
 * <p>
 * When the application is finished using a PooledConnection, the application
 * calls the <code>Connection.close</code> method. The Connection Pool Manager
 * is notified via a ConnectionEvent from the Connection that this has happened
 * (the Pool Manager registers itself with the Connection before the Connection
 * is given to the application). The Pool Manager removes the underlying
 * PooledConnection object from the Connection and returns it to the pool for
 * reuse - the PooledConnection is thus recycled rather than being destroyed.
 * <p>
 * The connection to the database represented by the PooledConnection is kept
 * open until the PooledConnection object itself is deactivated by the
 * Connection Pool Manager, which calls the <code>PooledConnection.close</code>
 * method. This is typically done if there are too many inactive connections in
 * the pool, if the PooledConnection encounters a problem that makes it unusable
 * or if the whole system is being shut down.
 * 
 */
public interface PooledConnection {

    /**
     * Registers the supplied ConnectionEventListener with this
     * PooledConnection. Once registered, the ConnectionEventListener will
     * receive ConnectionEvent events when they occur in the PooledConnection.
     * 
     * @param theListener
     *            an object which implements the ConnectionEventListener
     *            interface.
     */
    public void addConnectionEventListener(ConnectionEventListener theListener);

    /**
     * Closes the connection to the database held by this PooledConnection. This
     * method should not be called directly by application code - it is intended
     * for use by the Connection Pool manager component.
     * 
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public void close() throws SQLException;

    /**
     * Creates a connection to the database. This method is typically called by
     * the Connection Pool manager when an application invokes the method
     * <code>DataSource.getConnection</code> and there are no PooledConnection
     * objects available in the connection pool.
     * 
     * @return a Connection object that is a handle to this PooledConnection
     *         object.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public Connection getConnection() throws SQLException;

    /**
     * Deregister the supplied ConnectionEventListener from this
     * PooledConnection. Once deregistered, the ConnectionEventListener will not
     * longer receive events occurring in the PooledConnection.
     * 
     * @param theListener
     *            an object which implements the ConnectionEventListener
     *            interface. This object should have previously been registered
     *            with the PooledConnection using the
     *            <code>addConnectionEventListener</code> method.
     */
    public void removeConnectionEventListener(
            ConnectionEventListener theListener);
}
