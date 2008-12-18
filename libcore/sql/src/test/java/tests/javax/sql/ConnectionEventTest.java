package tests.javax.sql;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.sql.SQLException;

import javax.sql.ConnectionEvent;
import javax.sql.PooledConnection;

@TestTargetClass(ConnectionEvent.class)
public class ConnectionEventTest extends TestCase {

    /**
     * @tests {@link javax.sql.ConnectionEvent#ConnectionEvent(PooledConnection)}
     * @see {@link org.apache.harmony.sql.tests.javax.sql.ConnectionEventTest}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "Verifies ConnectionEvent() constructor for the normal case.",
            targets = { @TestTarget(methodName = "testConnectionEventPooledConnection", 
                                    methodArgs = {PooledConnection.class})                         
            }
    )  
    
    public void testConnectionEventPooledConnection() {
        // delegate to package org.apache.harmony.sql.tests.javax.sql.
        fail("Not yet implemented"); // TODO
    }

    /**
     * @tests {@link javax.sql.ConnectionEvent#ConnectionEvent(PooledConnection, SQLException)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "Verifies ConnectionEvent() constructor for the abnormal case that an error has occurred on the pooled connection.",
            targets = { @TestTarget(methodName = "testConnectionEventPooledConnection", 
                                    methodArgs = {PooledConnection.class,SQLException.class})                         
            }
    )    
    public void testConnectionEventPooledConnectionSQLException() {
     // delegate to org.apache.harmony.sql.tests.javax.sql.
        fail("Not yet implemented"); // TODO
    }

    /**
     * @tests {@link javax.sql.ConnectionEvent#getSQLException()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "Verifies that a SQLException is thrown after an error has occurred in the connection pool.",
            targets = { @TestTarget(methodName = "testGetSQLException", 
                                    methodArgs = {})                         
            }
    )    
    public void testGetSQLException() {
        fail("Not yet implemented"); // TODO
    }

}
