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
import java.lang.reflect.Field;
import java.sql.SQLException;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SQLExceptionTest extends TestCase {

    static long theFixedSUID = 2135244094396331484L;

    /*
     * SUID test
     */
    public void testSUID() {

        try {
            Class<?> theClass = Class.forName("java.sql.SQLException");
            Field theField = theClass.getDeclaredField("serialVersionUID");
            theField.setAccessible(true);
            long theSUID = theField.getLong(null);
            assertEquals("SUID mismatch: ", theFixedSUID, theSUID);
        } catch (Exception e) {
            System.out.println("SUID check got exception: " + e.getMessage());
            // assertTrue("Exception while testing SUID ", false );
        } // end catch

    } // end method testSUID

    /*
     * ConstructorTest
     */
    public void testSQLExceptionStringStringint() {

        String[] init1 = { "a", "1", "valid1", "----", "&valid*", "1", "a",
                null, "", "\u0000", "a", "a", "a" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", "a",
                "&valid*", "a", "a", "a", null, "", "\u0000" };
        int[] init3 = { -2147483648, 2147483647, 0, 48429456, 1770127344,
                1047282235, -545472907, -2147483648, -2147483648, -2147483648,
                -2147483648, -2147483648, -2147483648 };

        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = init2;
        int[] theFinalStates3 = init3;
        SQLException[] theFinalStates4 = { null, null, null, null, null, null,
                null, null, null, null, null, null, null };

        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null, null, null, null, null };

        SQLException aSQLException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLException = new SQLException(init1[i], init2[i], init3[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getNextException(), theFinalStates4[i]);

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

    } // end method testSQLExceptionStringStringint

    /*
     * ConstructorTest
     */
    public void testSQLExceptionStringString() {

        String[] init1 = { "a", "1", "valid1", "----", "&valid*", null, "",
                "\u0000", "a", "a", "a" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", "a", "a",
                "a", null, "", "\u0000" };

        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = init2;
        int[] theFinalStates3 = { 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0 };
        SQLException[] theFinalStates4 = { null, null, null, null, null, null,
                null, null, null, null, null };

        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null, null, null };

        SQLException aSQLException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLException = new SQLException(init1[i], init2[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getNextException(), theFinalStates4[i]);

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

    } // end method testSQLExceptionStringString

    /*
     * ConstructorTest
     */
    public void testSQLExceptionString() {

        String[] init1 = { "a", "1", "valid1", "----", "&valid*", null, 
                "", "\u0000" };

        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = { null, null, null, null, null, null, null,
                null };
        int[] theFinalStates3 = { 0, 0, 0, 0, 0,
                0, 0, 0 };
        SQLException[] theFinalStates4 = { null, null, null, null, null, null,
                null, null };

        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null };

        SQLException aSQLException;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLException = new SQLException(init1[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getNextException(), theFinalStates4[i]);

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

    } // end method testSQLExceptionString

    /*
     * ConstructorTest
     */
    public void testSQLException() {

        String[] theFinalStates1 = { null };
        String[] theFinalStates2 = { null };
        int[] theFinalStates3 = { 0 };
        SQLException[] theFinalStates4 = { null };

        Exception[] theExceptions = { null };

        SQLException aSQLException;
        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLException = new SQLException();
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getNextException(), theFinalStates4[i]);

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

    } // end method testSQLException

    /*
     * Method test for getErrorCode
     */
    public void testGetErrorCode() {

        SQLException aSQLException;
        String[] init1 = { "a", "1", "valid1", "----", null, "&valid*", "1" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", null, "a" };
        int[] init3 = { -2147483648, 2147483647, 0, 48429456, 1770127344,
                1047282235, -545472907 };

        int theReturn;
        int[] theReturns = init3;
        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = init2;
        int[] theFinalStates3 = init3;
        SQLException[] theFinalStates4 = { null, null, null, null, null, null,
                null };

        Exception[] theExceptions = { null, null, null, null, null, null, null };

        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLException = new SQLException(init1[i], init2[i], init3[i]);
                theReturn = aSQLException.getErrorCode();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getNextException(), theFinalStates4[i]);

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

    } // end method testGetErrorCode

    /*
     * Method test for getNextException
     */
    public void testGetNextException() {

        SQLException aSQLException;
        String[] init1 = { "a", "1", "valid1", "----", null, "&valid*", "1" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", null, "a" };
        int[] init3 = { -2147483648, 2147483647, 0, 48429456, 1770127344,
                1047282235, -545472907 };
        SQLException[] init4 = { new SQLException(), null, new SQLException(),
                new SQLException(), new SQLException(), null,
                new SQLException() };

        SQLException theReturn;
        SQLException[] theReturns = init4;
        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = init2;
        int[] theFinalStates3 = init3;
        SQLException[] theFinalStates4 = init4;

        Exception[] theExceptions = { null, null, null, null, null, null, null };

        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLException = new SQLException(init1[i], init2[i], init3[i]);
                aSQLException.setNextException(init4[i]);
                theReturn = aSQLException.getNextException();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getNextException(), theFinalStates4[i]);

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

    } // end method testGetNextException

    /*
     * Method test for getSQLState
     */
    public void testGetSQLState() {

        SQLException aSQLException;
        String[] init1 = { "a", "1", "valid1", "----", null, "&valid*", "1" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", null, "a" };
        int[] init3 = { -2147483648, 2147483647, 0, 48429456, 1770127344,
                1047282235, -545472907 };

        String theReturn;
        String[] theReturns = init2;
        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = init2;
        int[] theFinalStates3 = init3;
        SQLException[] theFinalStates4 = { null, null, null, null, null, null,
                null };

        Exception[] theExceptions = { null, null, null, null, null, null, null };

        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLException = new SQLException(init1[i], init2[i], init3[i]);
                theReturn = aSQLException.getSQLState();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getNextException(), theFinalStates4[i]);

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

    } // end method testGetSQLState

    /*
     * Method test for setNextException
     */
    public void testSetNextExceptionSQLException() {

        SQLException[] parm1 = { new SQLException(), null, new SQLException(),
                new SQLException(), new SQLException(), null,
                new SQLException() };

        SQLException aSQLException;

        String[] init1 = { "a", "1", "valid1", "----", null, "&valid*", "1" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", null, "a" };
        int[] init3 = { -2147483648, 2147483647, 0, 48429456, 1770127344,
                1047282235, -545472907 };

        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = init2;
        int[] theFinalStates3 = init3;
        SQLException[] theFinalStates4 = parm1;

        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null, null, null };

        int loopCount = parm1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLException = new SQLException(init1[i], init2[i], init3[i]);
                aSQLException.setNextException(parm1[i]);
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLException
                        .getNextException(), theFinalStates4[i]);

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

    } // end method testSetNextExceptionSQLException

    /**
     * @tests serialization/deserialization compatibility.
     */
    public void testSerializationSelf() throws Exception {
        SQLException object = new SQLException();
        SerializationTest.verifySelf(object, SQLEXCEPTION_COMPARATOR);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SQLException nextSQLException = new SQLException("nextReason",
                "nextSQLState", 33);

        int vendorCode = 10;
        SQLException object = new SQLException("reason", "SQLState", vendorCode);

        object.setNextException(nextSQLException);

        SerializationTest.verifyGolden(this, object, SQLEXCEPTION_COMPARATOR);
    }

    // comparator for SQLException objects
    private static final SerializableAssert SQLEXCEPTION_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            // do common checks for all throwable objects
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            SQLException initThr = (SQLException) initial;
            SQLException dserThr = (SQLException) deserialized;

            // verify SQLState
            Assert.assertEquals("SQLState", initThr.getSQLState(), dserThr
                    .getSQLState());

            // verify vendorCode
            Assert.assertEquals("vendorCode", initThr.getErrorCode(), dserThr
                    .getErrorCode());

            // verify next
            if (initThr.getNextException() == null) {
                assertNull(dserThr.getNextException());
            } else {
                // use the same comparator
                SQLEXCEPTION_COMPARATOR.assertDeserialized(initThr
                        .getNextException(), dserThr.getNextException());
            }
        }
    };
    
    /**
     * @tests java.sql.SQLException#setNextException(java.sql.SQLException)
     */
    public void test_setNextException_LSQLException() {
        SQLException se1 = new SQLException("reason" , "SQLState" , 1);
        SQLException se2 = new SQLException("reason" , "SQLState" , 2);
        SQLException se3 = new SQLException("reason" , "SQLState" , 3);
        SQLException se4 = new SQLException("reason" , "SQLState" , 4);
        
        se1.setNextException(se2);
        assertSame(se2, se1.getNextException());
        
        se1.setNextException(se3);
        assertSame(se2, se1.getNextException());
        assertSame(se3, se2.getNextException());
        assertNull(se3.getNextException());
        
        se3.setNextException(null);
        assertNull(se3.getNextException());
        
        se3.setNextException(se4);
        assertSame(se4, se3.getNextException());
    }

} // end class SQLExceptionTest

