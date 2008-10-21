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

package org.apache.harmony.sql.tests.javax.sql;

import java.sql.SQLException;
import javax.sql.ConnectionEvent;
import junit.framework.TestCase;

public class ConnectionEventTest extends TestCase {

    public void testConstructorConnection() {
        try {
            new ConnectionEvent(null);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
        }
        
        Impl_PooledConnection ipc = new Impl_PooledConnection();
        ConnectionEvent ce = new ConnectionEvent(ipc);
        assertSame(ipc, ce.getSource());
        assertNull(ce.getSQLException());
    }
    
    public void testConstructorConnectionSQLException() {
        try {
            new ConnectionEvent(null, null);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
        }
        
        Impl_PooledConnection ipc = new Impl_PooledConnection();
        ConnectionEvent ce = new ConnectionEvent(ipc, null);
        assertSame(ipc, ce.getSource());
        assertNull(ce.getSQLException());
        
        SQLException e = new SQLException();
        ce = new ConnectionEvent(ipc, e);
        assertSame(ipc, ce.getSource());
        assertSame(e, ce.getSQLException());
    }
}   

