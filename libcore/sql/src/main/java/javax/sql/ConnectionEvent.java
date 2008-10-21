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

import java.util.EventObject;
import java.sql.SQLException;
import java.io.Serializable;

/**
 * An Event object which is sent when specific events happen on a
 * PooledConnection object. The events involved are when the application closing
 * the PooledConnection and when an error occurs in the PooledConnection.
 */
public class ConnectionEvent extends EventObject implements Serializable {

    private static final long serialVersionUID = -4843217645290030002L;

    private SQLException theSQLException;

    /**
     * Creates a connection event initialized with a supplied PooledConnection.
     * 
     * @param theConnection
     *            the PooledConnection
     */
    public ConnectionEvent(PooledConnection theConnection) {
        super(theConnection);
    }

    /**
     * Creates a ConnectionEvent initialized with a supplied PooledConnection
     * and with a supplied SQLException indicating that an error has occurred
     * within the PooledConnection.
     * 
     * @param theConnection
     *            the PooledConnection
     * @param theException
     *            the SQLException holding information about the error that has
     *            occurred, which is about to be returned to the application.
     */
    public ConnectionEvent(PooledConnection theConnection,
            SQLException theException) {
        super(theConnection);
        theSQLException = theException;
    }

    /**
     * Gets the SQLException which holds information about the error which
     * occurred in the PooledConnection.
     * 
     * @return an SQLException containing information about the error. May be
     *         null if no error has occurred.
     */
    public SQLException getSQLException() {
        return theSQLException;
    }
}
