/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.sql.tests.java.sql;

import java.io.Serializable;
import java.sql.BatchUpdateException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class BatchUpdateExceptionTest extends TestCase {

    /*
     * ConstructorTest
     */
    public void testBatchUpdateException() {

        int[] theFinalStates1 = { 0 }; // Error Code state
        int[][] theFinalStates2 = { null }; // Update Counts array state
        String[] theFinalStates3 = { null }; // SQL State state value
        String[] theFinalStates4 = { null }; // Message state

        Exception[] theExceptions = { null };

        BatchUpdateException aBatchUpdateException;
        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException();
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);

            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } // end try
        } // end for

    } // end method testBatchUpdateException

    /*
     * ConstructorTest
     */
    public void testBatchUpdateExceptionintArray() {

        int[][] init1 = { { 1, 2, 3 }, null };

        int[] theFinalStates1 = { 0, 0 }; // Error Code state
        int[][] theFinalStates2 = init1; // Update Counts array state
        String[] theFinalStates3 = { null, null }; // SQL State state value
        String[] theFinalStates4 = { null, null }; // Message state

        Exception[] theExceptions = { null, null };

        BatchUpdateException aBatchUpdateException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException(init1[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);

            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } // end try
        } // end for

    } // end method testBatchUpdateExceptionintArray

    /*
     * ConstructorTest
     */
    public void testBatchUpdateExceptionStringintArray() {

        String[] init1 = { "a", "1", "valid1", "----", "&valid*", null, "",
                ".", "a" };
        int[][] init2 = { { 1, 2, 3 }, {}, { 3 }, null, { 5, 5 }, { 6 },
                { 121, 2, 1 }, { 1 }, { 1, 2 } };

        int[] theFinalStates1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // Error Code
                                                                // state
        // Update Counts array state
        int[][] theFinalStates2 = init2;
        // SQL State state value
        String[] theFinalStates3 = { null, null, null, null, null, null, null,
                null, null };
        String[] theFinalStates4 = init1; // Message state

        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null };

        BatchUpdateException aBatchUpdateException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException(init1[i],
                        init2[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);

            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } // end try
        } // end for

    } // end method testBatchUpdateExceptionStringintArray

    /*
     * ConstructorTest
     */
    public void testBatchUpdateExceptionStringStringintArray() {

        String[] init1 = { "a", "1", "valid1", "----", "&valid*", null, "",
                ".", "a", "a" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", "a", null,
                "", ".", "a" };
        int[][] init3 = { { 1, 2, 3 }, {}, { 3 }, { 5, 5 }, { 6 },
                { 121, 2, 1 }, { 1 }, { 1, 2 }, { 1 }, { 2 }, null };

        int[] theFinalStates1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // Error
                                                                        // Code
                                                                        // state
        // Update Counts array state
        int[][] theFinalStates2 = init3;
        // SQL State state value
        String[] theFinalStates3 = init2;
        String[] theFinalStates4 = init1; // Message state

        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null, null, null };

        BatchUpdateException aBatchUpdateException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException(init1[i],
                        init2[i], init3[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);

            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } // end try
        } // end for

    } // end method testBatchUpdateExceptionStringStringintArray

    /*
     * ConstructorTest
     */
    public void testBatchUpdateExceptionStringStringintintArray() {

        String[] init1 = { "a", "1", "valid1", "----", "&valid*", null, "",
                ".", "a", "a" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", "a", null,
                "", ".", "a" };
        int[] init3 = { -2147483648, 2147483647, 0, -492417162, -156220255,
                -173012890, -631026360, -2147483648, -2147483648, -2147483648,
                -2147483648 };
        int[][] init4 = { { 1, 2, 3 }, {}, { 3 }, { 5, 5 }, { 6 },
                { 121, 2, 1 }, { 1 }, { 1, 2 }, { 1 }, { 2 }, null };

        int[] theFinalStates1 = init3; // Error Code state
        // Update Counts array state
        int[][] theFinalStates2 = init4;
        // SQL State state value
        String[] theFinalStates3 = init2;
        String[] theFinalStates4 = init1; // Message state

        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null, null, null };

        BatchUpdateException aBatchUpdateException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException(init1[i],
                        init2[i], init3[i], init4[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);

            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } // end try
        } // end for

    } // end method testBatchUpdateExceptionStringStringintintArray

    /*
     * Method test for getUpdateCounts
     */
    public void testGetUpdateCounts() {

        BatchUpdateException aBatchUpdateException;
        int[][] init1 = { { 1, 2, 3 }, {}, null };

        int[] theReturn;
        int[][] theReturns = init1;

        int[] theFinalStates1 = { 0, 0, 0 }; // Error Code state
        int[][] theFinalStates2 = init1; // Update Counts array state
        String[] theFinalStates3 = { null, null, null }; // SQL State state
                                                            // value
        String[] theFinalStates4 = { null, null, null }; // Message state

        Exception[] theExceptions = { null, null, null };

        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aBatchUpdateException = new BatchUpdateException(init1[i]);
                theReturn = aBatchUpdateException.getUpdateCounts();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getErrorCode(),
                        theFinalStates1[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getUpdateCounts(),
                        theFinalStates2[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getSQLState(), theFinalStates3[i]);
                assertEquals(i + " Final state mismatch: ",
                        aBatchUpdateException.getMessage(), theFinalStates4[i]);

            } catch (Exception e) {
                if (theExceptions[i] == null) {
                    fail(i + "Unexpected exception");
                }
                assertEquals(i + "Exception mismatch", e.getClass(),
                        theExceptions[i].getClass());
                assertEquals(i + "Exception mismatch", e.getMessage(),
                        theExceptions[i].getMessage());
            } // end try
        } // end for

    } // end method testGetUpdateCounts
    
    /**
     * @tests serialization/deserialization compatibility.
     */
    public void testSerializationSelf() throws Exception {
        BatchUpdateException object = new BatchUpdateException();
        SerializationTest.verifySelf(object, BATCHUPDATEEXCEPTION_COMPARATOR);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {
        int vendorCode = 10;
        int[] updateCounts = { 1, 2, 3, 4 };
        BatchUpdateException object = new BatchUpdateException("reason",
                "SQLState", vendorCode, updateCounts);
        SerializationTest.verifyGolden(this, object,
                BATCHUPDATEEXCEPTION_COMPARATOR);
    }

    // comparator for BatchUpdateException field updateCounts
    private static final SerializableAssert BATCHUPDATEEXCEPTION_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            // do common checks for all throwable objects
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            BatchUpdateException initThr = (BatchUpdateException) initial;
            BatchUpdateException dserThr = (BatchUpdateException) deserialized;

            // verify updateCounts
            int[] initUpdateCounts = initThr.getUpdateCounts();
            int[] dserUpdateCounts = dserThr.getUpdateCounts();
            assertTrue(Arrays.equals(initUpdateCounts, dserUpdateCounts));
        }
    };

} // end class BatchUpdateExceptionTest

