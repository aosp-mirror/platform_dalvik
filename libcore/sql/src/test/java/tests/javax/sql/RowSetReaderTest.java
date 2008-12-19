package tests.javax.sql;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.RowSetInternal;
import javax.sql.RowSetReader;

@TestTargetClass(RowSetReader.class)
public class RowSetReaderTest extends TestCase {

    /**
     * @tests {@link javax.sql.RowSetReader#readData(RowSetInternal)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "setColumnName",
                                   methodArgs = {RowSetInternal.class})
            }
    )
    public void testReadData() {
        fail("Not yet implemented");
    }

}
