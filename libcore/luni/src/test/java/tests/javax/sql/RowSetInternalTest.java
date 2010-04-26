package tests.javax.sql;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.RowSetInternal;
import javax.sql.RowSetMetaData;

@TestTargetClass(RowSetInternal.class)
public class RowSetInternalTest extends TestCase {

    /**
     * @tests {@link javax.sql.RowSetInternal#getConnection()}
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
     * @tests {@link javax.sql.RowSetInternal#getOriginal()}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getOriginal",
        args = {}
    )
    public void testGetOriginal() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSetInternal#getOriginalRow()}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getOriginalRow",
        args = {}
    )
    public void testGetOriginalRow() {
        fail("Not yet implemented");
    }

    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
      method = "getParams",
      args = {}
    )
    public void testGetParams() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSetInternal#setMetaData(javax.sql.RowSetMetaData)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setMetaData",
        args = {javax.sql.RowSetMetaData.class}
    )
    public void testSetMetaData() {
        fail("Not yet implemented");
    }

}
