package tests.javax.sql;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.PrintWriter;

import javax.sql.ConnectionPoolDataSource;


@TestTargetClass(ConnectionPoolDataSource.class)
public class ConnectionPoolDataSourceTest extends TestCase {

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#getLoginTimeout()}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getLoginTimeout",
        args = {}
    )
    public void testGetLoginTimeout() {
        fail("Not yet implemented"); // NOT_FEASIBLE
    }

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#getLogWriter()}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getLogWriter",
        args = {}
    )
    public void testGetLogWriter() {
        fail("Not yet implemented"); // NOT_FEASIBLE
    }

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#getPooledConnection()}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getPooledConnection",
        args = {}
    )
    public void testGetPooledConnection() {
        fail("Not yet implemented"); // NOT_FEASIBLE
    }

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#getPooledConnection(String, String)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getPooledConnection",
        args = {String.class, String.class}
    )
    public void testGetPooledConnectionStringString() {
        fail("Not yet implemented"); // NOT_FEASIBLE
    }

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#setLoginTimeout(int)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setLoginTimeout",
        args = {int.class}
    )
    public void testSetLoginTimeout() {
        fail("Not yet implemented"); // NOT_FEASIBLE
    }

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#setLogWriter(java.io.PrintWriter)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setLogWriter",
        args = {java.io.PrintWriter.class}
    )
    public void testSetLogWriter() {
        fail("Not yet implemented"); // NOT_FEASIBLE
    }

}
