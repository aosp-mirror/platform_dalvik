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
import java.sql.DataTruncation;
import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

import junit.framework.TestCase;

public class DataTruncationTest extends TestCase {

    /*
     * ConstructorTest
     */
    public void testDataTruncationintbooleanbooleanintint() {

        int[] init1 = { -2147483648, 2147483647, 0, 329751502, 318587557,
                -1217247045, 329474146 };
        boolean[] init2 = { false, true, false, false, false, true, false };
        boolean[] init3 = { false, true, false, false, false, false, true };
        int[] init4 = { -2147483648, 2147483647, 0, 1761409290, -1331044048,
                -576231606, 661635011 };
        int[] init5 = { -2147483648, 2147483647, 0, 540816689, -1890783845,
                -105552912, -85923935 };

        String[] theFinalStates1 = { "01004", "01004", "01004", "01004",
                "01004", "01004", "01004" };
        String state2 = "Data truncation";
        String[] theFinalStates2 = { state2, state2, state2, state2, state2,
                state2, state2 };
        int[] theFinalStates3 = { 0, 0, 0, 0, 0, 0, 0 };
        int[] theFinalStates4 = init1;
        int[] theFinalStates5 = init4;
        int[] theFinalStates6 = init5;
        boolean[] theFinalStates7 = init2;
        boolean[] theFinalStates8 = init3;

        Exception[] theExceptions = { null, null, null, null, null, null, null };

        DataTruncation aDataTruncation;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aDataTruncation = new DataTruncation(init1[i], init2[i],
                        init3[i], init4[i], init5[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getSQLState(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getMessage(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getIndex(), theFinalStates4[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getDataSize(), theFinalStates5[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getTransferSize(), theFinalStates6[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getParameter(), theFinalStates7[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getRead(), theFinalStates8[i]);

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

    } // end method testDataTruncationintbooleanbooleanintint

    /*
     * Method test for getIndex
     */
    public void testGetIndex() {

        DataTruncation aDataTruncation;
        int[] init1 = { -2147483648, 2147483647, 0, -2045829673, 1977156911,
                478985827, 1687271915 };
        boolean[] init2 = { false, true, false, false, true, true, true };
        boolean[] init3 = { false, true, false, false, true, true, true };
        int[] init4 = { -2147483648, 2147483647, 0, -631377748, 21025030,
                1215194589, 1064137121 };
        int[] init5 = { -2147483648, 2147483647, 0, -897998505, 997578180,
                735015866, 264619424 };

        int theReturn;
        int[] theReturns = init1;
        String[] theFinalStates1 = { "01004", "01004", "01004", "01004",
                "01004", "01004", "01004" };
        String state2 = "Data truncation";
        String[] theFinalStates2 = { state2, state2, state2, state2, state2,
                state2, state2 };
        int[] theFinalStates3 = { 0, 0, 0, 0, 0, 0, 0 };
        int[] theFinalStates4 = init1;
        int[] theFinalStates5 = init4;
        int[] theFinalStates6 = init5;
        boolean[] theFinalStates7 = init2;
        boolean[] theFinalStates8 = init3;

        Exception[] theExceptions = { null, null, null, null, null, null, null };

        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aDataTruncation = new DataTruncation(init1[i], init2[i],
                        init3[i], init4[i], init5[i]);
                theReturn = aDataTruncation.getIndex();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getSQLState(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getMessage(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getIndex(), theFinalStates4[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getDataSize(), theFinalStates5[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getTransferSize(), theFinalStates6[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getParameter(), theFinalStates7[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getRead(), theFinalStates8[i]);

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

    } // end method testGetIndex

    /*
     * Method test for getParameter
     */
    public void testGetParameter() {

        DataTruncation aDataTruncation;
        int[] init1 = { -2147483648, 2147483647, 0, -492314242, 1637665948,
                -305785075, 258819883 };
        boolean[] init2 = { false, true, false, true, true, false, true };
        boolean[] init3 = { false, true, false, false, false, true, true };
        int[] init4 = { -2147483648, 2147483647, 0, 1134512579, 533874007,
                1709608139, 990656593 };
        int[] init5 = { -2147483648, 2147483647, 0, -1566784226, -744009101,
                -444614454, 356465980 };

        boolean theReturn;
        boolean[] theReturns = init2;
        String[] theFinalStates1 = { "01004", "01004", "01004", "01004",
                "01004", "01004", "01004" };
        String state2 = "Data truncation";
        String[] theFinalStates2 = { state2, state2, state2, state2, state2,
                state2, state2 };
        int[] theFinalStates3 = { 0, 0, 0, 0, 0, 0, 0 };
        int[] theFinalStates4 = init1;
        int[] theFinalStates5 = init4;
        int[] theFinalStates6 = init5;
        boolean[] theFinalStates7 = init2;
        boolean[] theFinalStates8 = init3;

        Exception[] theExceptions = { null, null, null, null, null, null, null };

        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aDataTruncation = new DataTruncation(init1[i], init2[i],
                        init3[i], init4[i], init5[i]);
                theReturn = aDataTruncation.getParameter();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getSQLState(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getMessage(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getIndex(), theFinalStates4[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getDataSize(), theFinalStates5[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getTransferSize(), theFinalStates6[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getParameter(), theFinalStates7[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getRead(), theFinalStates8[i]);

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

    } // end method testGetParameter

    /*
     * Method test for getRead
     */
    public void testGetRead() {

        DataTruncation aDataTruncation;
        int[] init1 = { -2147483648, 2147483647, 0, 2092420209, -1695764964,
                1832837995, -80199594 };
        boolean[] init2 = { false, true, false, false, false, true, true };
        boolean[] init3 = { false, true, false, false, true, true, false };
        int[] init4 = { -2147483648, 2147483647, 0, 1762375167, -604897453,
                1362491587, 1007466498 };
        int[] init5 = { -2147483648, 2147483647, 0, 1494407222, -1696982311,
                -940493360, -1777579868 };

        boolean theReturn;
        boolean[] theReturns = init3;
        String[] theFinalStates1 = { "01004", "01004", "01004", "01004",
                "01004", "01004", "01004" };
        String state2 = "Data truncation";
        String[] theFinalStates2 = { state2, state2, state2, state2, state2,
                state2, state2 };
        int[] theFinalStates3 = { 0, 0, 0, 0, 0, 0, 0 };
        int[] theFinalStates4 = init1;
        int[] theFinalStates5 = init4;
        int[] theFinalStates6 = init5;
        boolean[] theFinalStates7 = init2;
        boolean[] theFinalStates8 = init3;

        Exception[] theExceptions = { null, null, null, null, null, null, null };

        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aDataTruncation = new DataTruncation(init1[i], init2[i],
                        init3[i], init4[i], init5[i]);
                theReturn = aDataTruncation.getRead();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getSQLState(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getMessage(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getIndex(), theFinalStates4[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getDataSize(), theFinalStates5[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getTransferSize(), theFinalStates6[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getParameter(), theFinalStates7[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getRead(), theFinalStates8[i]);

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

    } // end method testGetRead

    /*
     * Method test for getDataSize
     */
    public void testGetDataSize() {

        DataTruncation aDataTruncation;
        int[] init1 = { -2147483648, 2147483647, 0, 1146707040, -2020665632,
                1268632617, -1595624039 };
        boolean[] init2 = { false, true, false, true, false, true, true };
        boolean[] init3 = { false, true, false, true, true, false, false };
        int[] init4 = { -2147483648, 2147483647, 0, -367493363, 328996907,
                -1581326731, 835022052 };
        int[] init5 = { -2147483648, 2147483647, 0, -886134194, 908213800,
                1123419516, -429606389 };

        int theReturn;
        int[] theReturns = init4;
        String[] theFinalStates1 = { "01004", "01004", "01004", "01004",
                "01004", "01004", "01004" };
        String state2 = "Data truncation";
        String[] theFinalStates2 = { state2, state2, state2, state2, state2,
                state2, state2 };
        int[] theFinalStates3 = { 0, 0, 0, 0, 0, 0, 0 };
        int[] theFinalStates4 = init1;
        int[] theFinalStates5 = init4;
        int[] theFinalStates6 = init5;
        boolean[] theFinalStates7 = init2;
        boolean[] theFinalStates8 = init3;

        Exception[] theExceptions = { null, null, null, null, null, null, null };

        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aDataTruncation = new DataTruncation(init1[i], init2[i],
                        init3[i], init4[i], init5[i]);
                theReturn = aDataTruncation.getDataSize();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getSQLState(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getMessage(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getIndex(), theFinalStates4[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getDataSize(), theFinalStates5[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getTransferSize(), theFinalStates6[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getParameter(), theFinalStates7[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getRead(), theFinalStates8[i]);

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

    } // end method testGetDataSize

    /*
     * Method test for getTransferSize
     */
    public void testGetTransferSize() {

        DataTruncation aDataTruncation;
        int[] init1 = { -2147483648, 2147483647, 0, 78096124, 1719192600,
                -1661234694, -1205825753 };
        boolean[] init2 = { false, true, false, false, true, false, true };
        boolean[] init3 = { false, true, false, false, false, false, false };
        int[] init4 = { -2147483648, 2147483647, 0, -493779295, -2042560243,
                -217347438, 1357818664 };
        int[] init5 = { -2147483648, 2147483647, 0, -1647009002, -717544563,
                -1368171905, -918209633 };

        int theReturn;
        int[] theReturns = init5;
        String[] theFinalStates1 = { "01004", "01004", "01004", "01004",
                "01004", "01004", "01004" };
        String state2 = "Data truncation";
        String[] theFinalStates2 = { state2, state2, state2, state2, state2,
                state2, state2 };
        int[] theFinalStates3 = { 0, 0, 0, 0, 0, 0, 0 };
        int[] theFinalStates4 = init1;
        int[] theFinalStates5 = init4;
        int[] theFinalStates6 = init5;
        boolean[] theFinalStates7 = init2;
        boolean[] theFinalStates8 = init3;

        Exception[] theExceptions = { null, null, null, null, null, null, null };

        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aDataTruncation = new DataTruncation(init1[i], init2[i],
                        init3[i], init4[i], init5[i]);
                theReturn = aDataTruncation.getTransferSize();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getSQLState(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getMessage(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getIndex(), theFinalStates4[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getDataSize(), theFinalStates5[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getTransferSize(), theFinalStates6[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getParameter(), theFinalStates7[i]);
                assertEquals(i + "  Final state mismatch", aDataTruncation
                        .getRead(), theFinalStates8[i]);

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

    } // end method testGetTransferSize
    
    /**
     * @tests serialization/deserialization compatibility.
     */
    public void testSerializationSelf() throws Exception {
        DataTruncation object = new DataTruncation(10, true, true, 10, 10);
        SerializationTest.verifySelf(object, DATATRUNCATION_COMPARATOR);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {
        DataTruncation object = new DataTruncation(10, true, true, 10, 10);
        SerializationTest.verifyGolden(this, object, DATATRUNCATION_COMPARATOR);
    }

    // comparator for DataTruncation objects
    private static final SerializableAssert DATATRUNCATION_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            // do common checks for all throwable objects
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            DataTruncation initThr = (DataTruncation) initial;
            DataTruncation dserThr = (DataTruncation) deserialized;

            // verify index
            assertEquals(initThr.getIndex(), dserThr.getIndex());

            // verify parameter
            assertEquals(initThr.getParameter(), dserThr.getParameter());

            // verify read
            assertEquals(initThr.getRead(), dserThr.getRead());

            // verify dataSize
            assertEquals(initThr.getDataSize(), dserThr.getDataSize());

            // verify transferSize
            assertEquals(initThr.getTransferSize(), dserThr.getTransferSize());
        }
    };

} // end class DataTruncationTest
