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
import java.sql.SQLException;
import java.sql.SQLWarning;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

import junit.framework.TestCase;

public class SQLWarningTest extends TestCase {

    /*
     * ConstructorTest
     */
    public void testSQLWarning() {

        String[] theFinalStates1 = { null };
        String[] theFinalStates2 = { null };
        int[] theFinalStates3 = { 0 };
        SQLWarning[] theFinalStates4 = { null };

        Exception[] theExceptions = { null };

        SQLWarning aSQLWarning;
        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLWarning = new SQLWarning();
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getNextWarning(), theFinalStates4[i]);

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

    } // end method testSQLWarning

    /*
     * ConstructorTest
     */
    public void testSQLWarningString() {

        String[] init1 = { "a", "1", "valid1", "----", "&valid*", null, 
                "", "\u0000" };

        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = { null, null, null, null, null, null, null,
                null };
        int[] theFinalStates3 = { 0, 0, 0, 0, 0, 0, 0, 0 };
        SQLWarning[] theFinalStates4 = { null, null, null, null, null, null,
                null, null };

        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null };

        SQLWarning aSQLWarning;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLWarning = new SQLWarning(init1[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getNextWarning(), theFinalStates4[i]);

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

    } // end method testSQLWarningString

    /*
     * ConstructorTest
     */
    public void testSQLWarningStringString() {

        String[] init1 = { "a", "1", "valid1", "----", "&valid*", null, "",
                "\u0000", "a", "a", "a" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", "a", "a",
                "a", null, "", "\u0000" };

        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = init2;
        int[] theFinalStates3 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        SQLWarning[] theFinalStates4 = { null, null, null, null, null, null,
                null, null, null, null, null };

        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null, null, null };

        SQLWarning aSQLWarning;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLWarning = new SQLWarning(init1[i], init2[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getNextWarning(), theFinalStates4[i]);

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

    } // end method testSQLWarningStringString

    /*
     * ConstructorTest
     */
    public void testSQLWarningStringStringint() {

        String[] init1 = { "a", "1", "valid1", "----", "&valid*", "----",
                "----", null, "", "\u0000", "a", "a", "a" };
        String[] init2 = { "a", "1", "valid1", "----", "&valid*", "valid1",
                "----", "a", "a", "a", null, "", "\u0000" };
        int[] init3 = { -2147483648, 2147483647, 0, 1412862821, -733923487,
                488067774, -1529953616, -2147483648, -2147483648, -2147483648,
                -2147483648, -2147483648, -2147483648 };

        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = init2;
        int[] theFinalStates3 = init3;
        SQLWarning[] theFinalStates4 = { null, null, null, null, null, null,
                null, null, null, null, null, null, null };

        Exception[] theExceptions = { null, null, null, null, null, null, null,
                null, null, null, null, null, null };

        SQLWarning aSQLWarning;
        int loopCount = init1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLWarning = new SQLWarning(init1[i], init2[i], init3[i]);
                if (theExceptions[i] != null) {
                    fail();
                }
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getNextWarning(), theFinalStates4[i]);

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

    } // end method testSQLWarningStringStringint

    /*
     * Method test for getNextWarning
     */
    public void testGetNextWarning() {

        SQLWarning aSQLWarning;
        String[] init1 = { "a", "1", "valid1", "----", "&valid*" };

        SQLWarning theReturn;
        SQLWarning[] theReturns = { null };
        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = { null };
        int[] theFinalStates3 = { 0 };
        SQLWarning[] theFinalStates4 = { null };

        Exception[] theExceptions = { null };

        int loopCount = 1;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLWarning = new SQLWarning(init1[i]);
                theReturn = aSQLWarning.getNextWarning();
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "Return value mismatch", theReturn,
                        theReturns[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getNextWarning(), theFinalStates4[i]);

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

    } // end method testGetNextWarning

    /*
     * Method test for setNextWarning
     */
    public void testSetNextWarningSQLWarning() {

        SQLWarning[] parm1 = { new SQLWarning(), null };

        SQLWarning aSQLWarning;
        String[] init1 = { "a", "1" };

        String[] theFinalStates1 = init1;
        String[] theFinalStates2 = { null, null };
        int[] theFinalStates3 = { 0, 0 };
        SQLWarning[] theFinalStates4 = parm1;

        Exception[] theExceptions = { null, null };

        int loopCount = parm1.length;
        for (int i = 0; i < loopCount; i++) {
            try {
                aSQLWarning = new SQLWarning(init1[i]);
                aSQLWarning.setNextWarning(parm1[i]);
                if (theExceptions[i] != null) {
                    fail(i + "Exception missed");
                }
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getMessage(), theFinalStates1[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getSQLState(), theFinalStates2[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getErrorCode(), theFinalStates3[i]);
                assertEquals(i + "  Final state mismatch", aSQLWarning
                        .getNextWarning(), theFinalStates4[i]);

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

    } // end method testSetNextWarningSQLWarning
       
    /**
     * @tests java.sql.SQLWarning#setNextWarning(java.sql.SQLWarning)
     */
    public void test_setNextWarning_SQLWarning() {
        SQLWarning sw = new SQLWarning("reason", "SQLState", 0);
        SQLWarning sw1 = new SQLWarning("reason", "SQLState", 1);
        SQLWarning sw2 = new SQLWarning("reason", "SQLState", 2);
        SQLWarning sw3 = new SQLWarning("reason", "SQLState", 3);
        
        SQLException se = new SQLException("reason", "SQLState", 4);
        
        sw.setNextWarning(sw1);
        assertSame(sw1, sw.getNextException());
        assertSame(sw1, sw.getNextWarning());
        
        
        sw.setNextWarning(sw2);
        assertSame(sw2, sw1.getNextException());
        assertSame(sw2, sw1.getNextWarning());
        
        sw.setNextException(sw3);
        assertSame(sw3, sw2.getNextException());
        assertSame(sw3, sw2.getNextWarning());
        
        sw.setNextException(se);
        assertSame(se, sw3.getNextException());
        try {
            sw3.getNextWarning();
            fail("should throw Error");
        } catch (Error e) {
            //expected
        }
    }
    
    /**
     * @tests serialization/deserialization compatibility.
     */
    public void testSerializationSelf() throws Exception {
        SQLWarning object = new SQLWarning();
        SerializationTest.verifySelf(object, SQLWARNING_COMPARATOR);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {
        SQLWarning object = new SQLWarning();
        
        SQLWarning nextSQLWarning = new SQLWarning("nextReason",
                "nextSQLState", 10);
      
        object.setNextWarning(nextSQLWarning);

        SerializationTest.verifyGolden(this, object, SQLWARNING_COMPARATOR);
    }

    // comparator for SQLWarning objects
    private static final SerializableAssert SQLWARNING_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            // do common checks for all throwable objects
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            SQLWarning initThr = (SQLWarning) initial;
            SQLWarning dserThr = (SQLWarning) deserialized;

            // verify getNextWarning() method
            if (initThr.getNextWarning() == null) {
                assertNull(dserThr.getNextWarning());
            } else {
                // use the same comparator
                SQLWARNING_COMPARATOR.assertDeserialized(initThr
                        .getNextWarning(), dserThr.getNextWarning());
            }
        }
    };

} // end class SQLWarningTest

