/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.sql;

import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import java.sql.Ref;
import java.sql.ResultSet;

@TestTargetClass(ResultSet.class)
public class ResultSetNotSupportedTests extends SQLTest {
    
    /**
     * Test method for {@link java.sql.ResultSet#getArray(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getArray",
        args = {int.class}
    )
    public void testGetArrayInt() {
        
        fail();
    }

    /**
     * Test method for {@link java.sql.ResultSet#getArray(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getArray",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetArrayString() {
        fail("Not yet implemented");
    }

    
    /**
     * Test method for {@link java.sql.ResultSet#getAsciiStream(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getAsciiStream",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetAsciiStreamInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getAsciiStream(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getAsciiStream",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetAsciiStreamString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getBigDecimal(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBigDecimal",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBigDecimalInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getBigDecimal(int, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getBigDecimal",
        args = {int.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBigDecimalIntInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getBigDecimal(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBigDecimal",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBigDecimalString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getBigDecimal(java.lang.String, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBigDecimal",
        args = {java.lang.String.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBigDecimalStringInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getBinaryStream(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBinaryStream",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBinaryStreamInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getBinaryStream(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBinaryStream",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBinaryStreamString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getBlob(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBlob",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBlobInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getBlob(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBlob",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBlobString() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#getBoolean(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getBoolean",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBooleanInt() {
        /*
        try {
            assertTrue(res.first());
            boolean b = res.getBoolean(1);
            assertTrue(b);
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            boolean b = res.getBoolean(5);
            fail("Should get exception");
        } catch (SQLException e) {
            //ok
        }
        
        
        
        // null value 
        try {
            assertTrue(res.next());
            boolean b = res.getBoolean(1);
            assertFalse(b);
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        */
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#getBoolean(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getBoolean",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetBooleanString() {
        fail("Not yet implemented");
    }

    

    /**
     * Test method for {@link java.sql.ResultSet#getByte(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getByte",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetByteString() {
        fail("Not yet implemented");
    }

    
    /**
     * Test method for {@link java.sql.ResultSet#getByte(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getByte",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetByteInt() {
        /*
        try {
            assertTrue(res.first());
            byte b = res.getByte(14);
            String testString = Byte.toString(b);
            assertEquals("test string",testString);
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            byte b = res.getByte(5);
            fail("Should get exception");
        } catch (SQLException e) {
            //ok
        }
        
        // null value 
        try {
            assertTrue(res.next());
            byte b = res.getByte(14);
            assertNull(b);
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        */
    }
    
    
    /**
     * Test method for {@link java.sql.ResultSet#getCharacterStream(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getCharacterStream",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetCharacterStreamInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getCharacterStream(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getCharacterStream",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetCharacterStreamString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getClob(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getClob",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetClobInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getClob(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getClob",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetClobString() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#getCursorName()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported, setColumnName is not supported, therefore GetColumname can not be tested",
        method = "getCursorName",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testGetCursorName() {
        /*
        try {
            assertNull(res.getCursorName());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        ResultSet rs2 = null;
        Statement stmt;
        String inputCName = "my \"\"\"\"quoted\"\"\"\" cursor\"\"";
        try {
            assertNull(res.getCursorName());
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setCursorName(inputCName);
            rs2 = stmt.executeQuery("select * from type");
            rs2.next();
            String name = rs2.getCursorName();
            assertEquals(inputCName, name);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        try {
            rs2.close();
            rs2.getCursorName();
            fail("Should throw exception");
        } catch (Exception e) {
            //ok
        }
        */
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#getFetchDirection()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported.",
        method = "getFetchDirection",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testGetFetchDirection() {
        /*
        try {
            assertEquals(ResultSet.TYPE_FORWARD_ONLY, res.getFetchDirection());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        */
    }
    
    
    /**
     * Test method for {@link java.sql.ResultSet#getFetchSize()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getFetchSize",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testGetFetchSize() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#getObject(int, java.util.Map)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getObject",
        args = {int.class, java.util.Map.class}
    )
    @KnownFailure("Not Supported")
    public void testGetObjectIntMapOfStringClassOfQ() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#getObject(java.lang.String, java.util.Map)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getObject",
        args = {java.lang.String.class, java.util.Map.class}
    )
    @KnownFailure("Not Supported")
    public void testGetObjectStringMapOfStringClassOfQ() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getRef(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getRef",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetRefInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getRef(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getRef",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetRefString() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#getUnicodeStream(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getUnicodeStream",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testGetUnicodeStreamInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#getUnicodeStream(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getUnicodeStream",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testGetUnicodeStreamString() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#getWarnings()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getWarnings",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testGetWarnings() {
        /*
        try {
        res.close();
        res.getWarnings();
        fail("Exception expected");
        } catch (SQLException e) {
           //ok
        }
        */
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#cancelRowUpdates()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "SQLException checking missed",
        method = "cancelRowUpdates",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testCancelRowUpdates() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#deleteRow()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "deleteRow",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testDeleteRow() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#insertRow()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "insertRow",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testInsertRow() {
        fail("Not yet implemented");
    }

    
    /**
     * Test method for {@link java.sql.ResultSet#moveToCurrentRow()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "moveToCurrentRow",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testMoveToCurrentRow() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#moveToInsertRow()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "moveToInsertRow",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testMoveToInsertRow() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#refreshRow()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "refreshRow",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testRefreshRow() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#rowDeleted()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "rowDeleted",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testRowDeleted() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#rowInserted()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "rowInserted",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testRowInserted() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#rowUpdated()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "rowUpdated",
        args = {}
    )
    @KnownFailure("Not Supported")
    public void testRowUpdated() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#setFetchDirection(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setFetchDirection",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testSetFetchDirection() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#setFetchSize(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "setFetchSize",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testSetFetchSize() {
        fail("Not yet implemented");
    }
    
    
    /**
     * Test method for {@link java.sql.ResultSet#updateArray(int, java.sql.Array)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateArray",
        args = {int.class, java.sql.Array.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateArrayIntArray() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateArray",
        args = {java.lang.String.class, java.sql.Array.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateArrayStringArray() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateAsciiStream",
        args = {int.class, java.io.InputStream.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateAsciiStreamIntInputStreamInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateAsciiStream",
        args = {String.class, java.io.InputStream.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateAsciiStreamStringInputStreamInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateBigDecimal(int, java.math.BigDecimal)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBigDecimal",
        args = {int.class, java.math.BigDecimal.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBigDecimalIntBigDecimal() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateBigDecimal(java.lang.String, java.math.BigDecimal)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBigDecimal",
        args = {java.lang.String.class, java.math.BigDecimal.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBigDecimalStringBigDecimal() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBinaryStream",
        args = {int.class, java.io.InputStream.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBinaryStreamIntInputStreamInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBinaryStream",
        args = {java.lang.String.class, java.io.InputStream.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBinaryStreamStringInputStreamInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateBlob(int, java.sql.Blob)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBlob",
        args = {int.class, java.sql.Blob.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBlobIntBlob() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBlob",
        args = {java.lang.String.class, java.sql.Blob.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBlobStringBlob() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateBoolean(int, boolean)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBoolean",
        args = {int.class, boolean.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBooleanIntBoolean() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateBoolean(java.lang.String, boolean)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBoolean",
        args = {java.lang.String.class, boolean.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBooleanStringBoolean() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateByte(int, byte)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateByte",
        args = {int.class, byte.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateByteIntByte() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateByte(java.lang.String, byte)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateByte",
        args = {java.lang.String.class, byte.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateByteStringByte() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateBytes(int, byte[])}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBytes",
        args = {int.class, byte[].class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBytesIntByteArray() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateBytes(java.lang.String, byte[])}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateBytes",
        args = {java.lang.String.class, byte[].class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateBytesStringByteArray() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateCharacterStream",
        args = {int.class, java.io.Reader.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateCharacterStreamIntReaderInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateCharacterStream",
        args = {java.lang.String.class, java.io.Reader.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateCharacterStreamStringReaderInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateClob(int, java.sql.Clob)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateClob",
        args = {int.class, java.sql.Clob.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateClobIntClob() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateClob",
        args = {java.lang.String.class, java.sql.Clob.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateClobStringClob() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateDate(int, java.sql.Date)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateDate",
        args = {int.class, java.sql.Date.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateDateIntDate() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateDate",
        args = {java.lang.String.class, java.sql.Date.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateDateStringDate() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateDouble(int, double)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateDouble",
        args = {int.class, double.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateDoubleIntDouble() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateDouble(java.lang.String, double)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateDouble",
        args = {java.lang.String.class, double.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateDoubleStringDouble() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateFloat(int, float)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateFloat",
        args = {int.class, float.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateFloatIntFloat() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateFloat(java.lang.String, float)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateFloat",
        args = {java.lang.String.class, float.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateFloatStringFloat() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateInt(int, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateInt",
        args = {int.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateIntIntInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateInt(java.lang.String, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateInt",
        args = {String.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateIntStringInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateLong(int, long)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateLong",
        args = {int.class, long.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateLongIntLong() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateLong(java.lang.String, long)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateLong",
        args = {java.lang.String.class, long.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateLongStringLong() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateNull(int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateNull",
        args = {int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateNullInt() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateNull(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateNull",
        args = {java.lang.String.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateNullString() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#updateObject(int, java.lang.Object)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateObject",
        args = {int.class, java.lang.Object.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateObjectIntObject() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateObject(int, java.lang.Object, int)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "updateObject",
        args = {int.class, java.lang.Object.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateObjectIntObjectInt() {
        fail("Not yet implemented");
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#updateObject(String, Object) }.
     */
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateObject",
            args = {String.class, Object.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateStringObject() {
        
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#updateObject(String, Object, int) }.
     */
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateObject",
            args = {String.class, Object.class, int.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateStringObjectInt() {
        
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#updateRef(int, java.sql.Ref) }.
     */
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateRef",
            args = {int.class, Ref.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateRefIntRef() {
        
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#updateRef(String, Ref) }.
     */
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateRef",
            args = {String.class, Ref.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateRefStringRef() {
        
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#updateRow() }.
     */
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateRow",
            args = {}
    )
    @KnownFailure("Not Supported")
    public void testUpdateRow() {
        
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#updateShort(int, short) }.
     */
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateShort",
            args = {int.class, short.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateShortIntShort() {
        
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#updateShort(String, short) }.
     */
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateShort",
            args = {String.class, short.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateShortStringShort() {
        
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#updateString(int, String) }.
     */
    @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "",
            method = "updateString",
            args = {int.class, String.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateStringIntString() {
        
    }

    
    /**
     * Test method for {@link java.sql.ResultSet#updateTime(int, java.sql.Time)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateTime",
        args = {int.class, java.sql.Time.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateTimeIntTime() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateTime(java.lang.String, java.sql.Time)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateTime",
        args = {java.lang.String.class, java.sql.Time.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateTimeStringTime() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateTimestamp(int, java.sql.Timestamp)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateTimestamp",
        args = {int.class, java.sql.Timestamp.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateTimestampIntTimestamp() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#updateTimestamp(java.lang.String, java.sql.Timestamp)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateTimestamp",
        args = {java.lang.String.class, java.sql.Timestamp.class}
    )
    @KnownFailure("Not Supported")
    public void testUpdateTimestampStringTimestamp() {
        fail("Not yet implemented");
    }
   
}
