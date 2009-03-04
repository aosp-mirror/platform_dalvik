package tests.javax.sql;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.RowSetInternal;
import javax.sql.RowSetWriter;

@TestTargetClass(RowSetWriter.class)
public class RowSetWriterTest extends TestCase {

    /**
     * @tests {@link javax.sql.RowSetWriter#writeData(javax.sql.RowSetInternal)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "writeData",
        args = {javax.sql.RowSetInternal.class}
    )
    public void testWriteData() {
        fail("Not yet implemented");
    }

}
