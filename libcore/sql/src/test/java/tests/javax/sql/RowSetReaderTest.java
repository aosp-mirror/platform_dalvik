package tests.javax.sql;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.RowSetInternal;
import javax.sql.RowSetReader;

@TestTargetClass(RowSetReader.class)
public class RowSetReaderTest extends TestCase {

    /**
     * @tests {@link javax.sql.RowSetReader#readData(RowSetInternal)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "readData",
        args = {javax.sql.RowSetInternal.class}
    )
    public void testReadData() {
        fail("Not yet implemented");
    }

}
