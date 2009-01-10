package tests.javax.sql;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import javax.sql.RowSet;
import javax.sql.RowSetListener;

/**
 *No Implementation class of this interface is available: tests not feasible
 */
@TestTargetClass(RowSet.class)
public class RowSetTest extends TestCase {
    
    /**
     * @tests {@link javax.sql.RowSet#addRowSetListener(javax.sql.RowSetListener)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "addRowSetListener",
        args = {javax.sql.RowSetListener.class}
    )
    public void testAddRowSetListener() {
        fail("Not yet implemented");
    }
    
    /**
     * @tests {@link javax.sql.RowSet#clearParameters()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "clearParameters",
        args = {}
    )
    public void testClearParameters() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSet#addRowSetListener(javax.sql.RowSetListener)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "execute",
        args = {}
    )
    public void testExecute() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSet#getCommand()}.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "setCommand",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "getCommand",
            args = {}
        )
    })
    public void testSetGetCommand() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.RowSet#getDataSourceName()}.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "setDataSourceName",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "getDataSourceName",
            args = {}
        )
    })
    public void testSetGetDataSourceName() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getEscapeProcessing",
        args = {}
    )
    public void testSetGetEscapeProcessing() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getMaxFieldSize",
        args = {}
    )
    public void testSetGetMaxFieldSize() {
        fail("Not yet implemented");
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "getMaxRows",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "setMaxRows",
            args = {int.class}
        )
    })
    public void testSetGetMaxRows() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getPassword",
        args = {}
    )
    public void testSetGetPassword() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getQueryTimeout",
        args = {}
    )
    public void testSetGetQueryTimeout() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getTransactionIsolation",
        args = {}
    )
    public void testSetGetTransactionIsolation() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getTypeMap",
        args = {}
    )
    public void testSetGetTypeMap() {
        fail("Not yet implemented");
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "setUrl",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "getUrl",
            args = {}
        )
    })
    public void testSetGetUrl() {
        fail("Not yet implemented");
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "setUsername",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "getUsername",
            args = {}
        )
    })
    public void testSetGetUsername() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "isReadOnly",
        args = {}
    )
    public void testIsReadOnly() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "removeRowSetListener",
        args = {javax.sql.RowSetListener.class}
    )
    public void testRemoveRowSetListener() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setArray",
        args = {int.class, java.sql.Array.class}
    )
    public void testSetArray() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setAsciiStream",
        args = {int.class, java.io.InputStream.class, int.class}
    )
    public void testSetGetAsciiStream() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setBigDecimal",
        args = {int.class, java.math.BigDecimal.class}
    )
    public void testSetGetBigDecimal() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setBinaryStream",
        args = {int.class, java.io.InputStream.class, int.class}
    )
    public void testSetGetBinaryStream() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setBlob",
        args = {int.class, java.sql.Blob.class}
    )
    public void testSetGetBlob() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setBoolean",
        args = {int.class, boolean.class}
    )
    public void testSetGetBoolean() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setByte",
        args = {int.class, byte.class}
    )
    public void testSetGetByte() {
        fail("Not yet implemented");
    }
     
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setBytes",
        args = {int.class, byte[].class}
    )
    public void testSetGetBytes() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setCharacterStream",
        args = {int.class, java.io.Reader.class, int.class}
    )
    public void testSetGetCharacterStream() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setClob",
        args = {int.class, java.sql.Clob.class}
    )
    public void testSetGetClob() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setConcurrency",
        args = {int.class}
    )
    public void testSetGetConcurrency() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setDate",
        args = {int.class, java.sql.Date.class}
    )
    public void testSetGetDateIntDate() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setDate",
        args = {int.class, java.sql.Date.class, java.util.Calendar.class}
    )
    public void testSetDateIntDateCalendar() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setEscapeProcessing",
        args = {boolean.class}
    )
    public void testSetEscapeProcessing() {
        
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setDouble",
        args = {int.class, double.class}
    )
    public void testSetGetDouble() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setFloat",
        args = {int.class, float.class}
    )
    public void testSetGetFloat() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setInt",
        args = {int.class, int.class}
    )
    public void testSetGetInt() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setLong",
        args = {int.class, long.class}
    )
    public void testSetGetLong() {
        fail("Not yet implemented");
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "getMaxFieldSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "setMaxFieldSize",
            args = {int.class}
        )
    })
    public void testSetGetGetMaxFieldSize() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setNull",
        args = {int.class, int.class}
    )
    public void testSetGetNullIntInt() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setNull",
        args = {int.class, int.class, java.lang.String.class}
    )
    public void testSetGetNullIntIntString() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setObject",
        args = {int.class, java.lang.Object.class}
    )
    public void testSetGetObjectIntObject() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setObject",
        args = {int.class, java.lang.Object.class, int.class}
    )
    public void testSetGetObjectIntObjectInt() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setObject",
        args = {int.class, java.lang.Object.class, int.class, int.class}
    )
    public void testSetGetObjectIntObjectIntInt() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setPassword",
        args = {java.lang.String.class}
    )
    public void testSetPassword() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setQueryTimeout",
        args = {int.class}
    )
    public void testSetQueryTimeout() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setReadOnly",
        args = {boolean.class}
    )
    public void testSetReadOnly() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setRef",
        args = {int.class, java.sql.Ref.class}
    )
    public void testSetRef() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setShort",
        args = {int.class, short.class}
    )
    public void testSetShort() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setString",
        args = {int.class, java.lang.String.class}
    )
    public void testSetString() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setTime",
        args = {int.class, java.sql.Time.class}
    )
    public void testSetTimeIntTime() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setTime",
        args = {int.class, java.sql.Time.class, java.util.Calendar.class}
    )
    public void testSetTimeIntTimeCalendar() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setTimestamp",
        args = {int.class, java.sql.Timestamp.class}
    )
    public void testSetTimestampIntTimestamp() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setTimestamp",
        args = {int.class, java.sql.Timestamp.class, java.util.Calendar.class}
    )
    public void testSetTimestampIntTimestampCalendar() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setTransactionIsolation",
        args = {int.class}
    )
    public void testSetTransactionIsolation() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setType",
        args = {int.class}
    )
    public void testSetType() {
        fail("Not yet implemented");
    }
    
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setTypeMap",
        args = {java.util.Map.class}
    )
    public void testSetTypeMap() {
        fail("Not yet implemented");
    }
}
