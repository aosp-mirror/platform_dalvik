package tests.javax.sql;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.RowSet;
import javax.sql.RowSetListener;

/**
 * Annotation conventions for setters and getters?
 * @author andrea.groessbauer@noser.com
 *
 */
@TestTargetClass(RowSet.class)
public class RowSetTest extends TestCase {

    /**
     * Sets up the mock RowSet.
     */
    @Override
    protected void setUp() {
        // Reuse org.apache.harmony.sql.tests.javax.sql.Impl_RowSet.java
    }
    
    /**
     * @tests {@link javax.sql.RowSet#addRowSetListener(javax.sql.RowSetListener)}.
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = { @TestTarget(methodName = "addRowSetListener", 
                                    methodArgs = {RowSetListener.class})                         
            }
    )    
    public void testAddRowSetListener() {
        fail("Not yet implemented");
    }
    
    /**
     * @tests {@link javax.sql.RowSet#clearParameters()}.
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = { @TestTarget(methodName = "clearParameters", 
                                    methodArgs = {})                         
            }
    )    
    public void testClearParameters() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSet#addRowSetListener(javax.sql.RowSetListener)}.
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = { @TestTarget(methodName = "addRowSetListener", 
                                    methodArgs = {RowSetListener.class})                         
            }
    )    
    public void testExecute() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSet#getCommand()}.
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = { @TestTarget(methodName = "getCommand", 
                                    methodArgs = {})                         
            }
    )    
    public void testSetGetCommand() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSet#getDataSourceName()}.
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = { @TestTarget(methodName = "getDataSourceName", 
                                    methodArgs = {})                         
            }
    )    
    public void testSetGetDataSourceName() {
        fail("Not yet implemented");
    }

    public void testSetGetEscapeProcessing() {
        fail("Not yet implemented");
    }

    public void testSetGetMaxFieldSize() {
        fail("Not yet implemented");
    }

    public void testSetGetMaxRows() {
        fail("Not yet implemented");
    }

    public void testSetGetPassword() {
        fail("Not yet implemented");
    }

    public void testSetGetQueryTimeout() {
        fail("Not yet implemented");
    }

    public void testSetGetTransactionIsolation() {
        fail("Not yet implemented");
    }

    public void testSetGetTypeMap() {
        fail("Not yet implemented");
    }

    public void testSetGetUrl() {
        fail("Not yet implemented");
    }

    public void testSetGetUsername() {
        fail("Not yet implemented");
    }

    public void testIsReadOnly() {
        fail("Not yet implemented");
    }

    public void testRemoveRowSetListener() {
        fail("Not yet implemented");
    }

    public void testSetGetArray() {
        fail("Not yet implemented");
    }

    public void testSetGetAsciiStream() {
        fail("Not yet implemented");
    }

    public void testSetGetBigDecimal() {
        fail("Not yet implemented");
    }

    public void testSetGetBinaryStream() {
        fail("Not yet implemented");
    }

    public void testSetGetBlob() {
        fail("Not yet implemented");
    }

    public void testSetGetBoolean() {
        fail("Not yet implemented");
    }

    public void testSetGetByte() {
        fail("Not yet implemented");
    }

    public void testSetGetBytes() {
        fail("Not yet implemented");
    }

    public void testSetGetCharacterStream() {
        fail("Not yet implemented");
    }

    public void testSetGetClob() {
        fail("Not yet implemented");
    }

    public void testSetGetConcurrency() {
        fail("Not yet implemented");
    }

    public void testSetGetDateIntDate() {
        fail("Not yet implemented");
    }

    public void testSetDateIntDateCalendar() {
        fail("Not yet implemented");
    }

    public void testSetGetDouble() {
        fail("Not yet implemented");
    }

    public void testSetGetFloat() {
        fail("Not yet implemented");
    }

    public void testSetGetInt() {
        fail("Not yet implemented");
    }

    public void testSetGetLong() {
        fail("Not yet implemented");
    }

    public void testSetGetGetMaxFieldSize() {
        fail("Not yet implemented");
    }

    public void testSetGetNullIntInt() {
        fail("Not yet implemented");
    }

    public void testSetGetNullIntIntString() {
        fail("Not yet implemented");
    }

    public void testSetGetObjectIntObject() {
        fail("Not yet implemented");
    }

    public void testSetGetObjectIntObjectInt() {
        fail("Not yet implemented");
    }

    public void testSetGetObjectIntObjectIntInt() {
        fail("Not yet implemented");
    }

    public void testSetPassword() {
        fail("Not yet implemented");
    }

    public void testSetQueryTimeout() {
        fail("Not yet implemented");
    }

    public void testSetReadOnly() {
        fail("Not yet implemented");
    }

    public void testSetRef() {
        fail("Not yet implemented");
    }

    public void testSetShort() {
        fail("Not yet implemented");
    }

    public void testSetString() {
        fail("Not yet implemented");
    }

    public void testSetTimeIntTime() {
        fail("Not yet implemented");
    }

    public void testSetTimeIntTimeCalendar() {
        fail("Not yet implemented");
    }

    public void testSetTimestampIntTimestamp() {
        fail("Not yet implemented");
    }

    public void testSetTimestampIntTimestampCalendar() {
        fail("Not yet implemented");
    }

    public void testSetTransactionIsolation() {
        fail("Not yet implemented");
    }

    public void testSetType() {
        fail("Not yet implemented");
    }

    public void testSetTypeMap() {
        fail("Not yet implemented");
    }

    public void testSetUrl() {
        fail("Not yet implemented");
    }

    public void testSetUsername() {
        fail("Not yet implemented");
    }

}
