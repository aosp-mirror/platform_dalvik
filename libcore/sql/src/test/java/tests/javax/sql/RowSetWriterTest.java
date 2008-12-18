package tests.javax.sql;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.RowSetInternal;
import javax.sql.RowSetWriter;

@TestTargetClass(RowSetWriter.class)
public class RowSetWriterTest extends TestCase {

    /**
     * @tests {@link javax.sql.RowSetWriter#writeData(javax.sql.RowSetInternal)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "setColumnName",
                                   methodArgs = {RowSetInternal.class})
            }
    )
    public void testWriteData() {
        fail("Not yet implemented");
    }

}
