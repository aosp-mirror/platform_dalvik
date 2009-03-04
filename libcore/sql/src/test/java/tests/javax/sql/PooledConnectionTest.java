package tests.javax.sql;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

@TestTargetClass(PooledConnection.class)
public class PooledConnectionTest extends TestCase {

    /**
     * @tests {@link javax.sql.PooledConnection#addConnectionEventListener(javax.sql.ConnectionEventListener)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "addConnectionEventListener",
        args = {javax.sql.ConnectionEventListener.class}
    )
    public void testAddConnectionEventListener() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.PooledConnection#close()}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "close",
        args = {}
    )
    public void testClose() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.PooledConnection#getConnection()}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getConnection",
        args = {}
    )
    public void testGetConnection() {
        fail("Not yet implemented");
    }

    
    /**
     * @tests {@link javax.sql.PooledConnection#removeConnectionEventListener(ConnectionEventListener)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "removeConnectionEventListener",
        args = {javax.sql.ConnectionEventListener.class}
    )
    public void testRemoveConnectionEventListener() {
        fail("Not yet implemented");
    }

}
