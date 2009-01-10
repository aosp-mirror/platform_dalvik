package tests.javax.sql;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.PrintWriter;

import javax.sql.DataSource;

@TestTargetClass(DataSource.class)
public class DataSourceTest extends TestCase {

    /**
     * @tests {@link javax.sql.DataSource#getConnection()}
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
     * @tests {@link javax.sql.DataSource#getConnection(String, String)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getConnection",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetConnectionStringString() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.DataSource#getLoginTimeout()}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getLoginTimeout",
        args = {}
    )
    public void testGetLoginTimeout() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.DataSource#getLogWriter()}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getLogWriter",
        args = {}
    )
    public void testGetLogWriter() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.DataSource#setLoginTimeout(int)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setLoginTimeout",
        args = {int.class}
    )
    public void testSetLoginTimeout() {
        fail("Not yet implemented");
    }

    
    /**
     * @tests {@link javax.sql.DataSource#setLogWriter(java.io.PrintWriter)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setLogWriter",
        args = {PrintWriter.class}
    )
    public void testSetLogWriter() {
        fail("Not yet implemented");
    }

}
