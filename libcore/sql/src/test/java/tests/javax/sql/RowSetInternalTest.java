package tests.javax.sql;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.RowSetInternal;
import javax.sql.RowSetMetaData;

@TestTargetClass(RowSetInternal.class)
public class RowSetInternalTest extends TestCase {

    /**
     * @tests {@link javax.sql.RowSetInternal#getConnection()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getConnection",
                                   methodArgs = {})
            }
    )
    public void testGetConnection() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSetInternal#getOriginal()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getOriginal",
                                   methodArgs = {})
            }
    )
    public void testGetOriginal() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSetInternal#getOriginalRow()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getOriginalRow",
                                   methodArgs = {})
            }
    )
    public void testGetOriginalRow() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSetInternal#getOriginalRow()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getOriginalRow",
                                   methodArgs = {})
            }
    )
    public void testGetParams() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSetInternal#setMetaData(javax.sql.RowSetMetaData)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getOriginalRow",
                                   methodArgs = {RowSetMetaData.class})
            }
    )
    public void testSetMetaData() {
        fail("Not yet implemented");
    }

}
