/*
 * Copyright (C) 2007 The Android Open Source Project
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
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import tests.support.DatabaseCreator;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author andrea@google.com (Your Name Here)
 *
 */
@TestTargetClass(ResultSet.class)
public class ResultSetTest extends SQLTest {
    
    ResultSet target = null;
    ResultSet emptyTarget = null;
    ResultSet scrollableTarget = null;
    ResultSet writableTarget = null;
    Statement stForward = null;
    Statement stScrollable = null;
    Statement stWritable = null;
    final String selectAllAnimals = "select id, name from zoo";
    final String selectEmptyTable = "select * from "+DatabaseCreator.SIMPLE_TABLE1;
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        try {
            conn.setAutoCommit(false);
            stForward = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            stForward.execute(selectAllAnimals);
            target = stForward.getResultSet();
            assertNotNull(target);

            // empty table
            stForward = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            stForward.execute(DatabaseCreator.CREATE_TABLE_SIMPLE1);
            stForward.execute(selectEmptyTable);
            emptyTarget = stForward.getResultSet();

        } catch (SQLException e) {
            fail("SQLException was thrown: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() {
        super.tearDown();
        try {
            target.close();
            stForward.close();
        } catch (SQLException e) {
            fail("Error in test setup");
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link java.sql.ResultSet#absolute(int)}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "absolute",
        args = {int.class}
    )
    public void testAbsolute() {
        try {
            assertTrue(target.isBeforeFirst());
            assertFalse(target.absolute(0));
            assertTrue(target.absolute(1));
            assertTrue(target.isFirst());
            assertTrue(target.absolute(-1));
            assertTrue(target.isLast());
            target.next();
            assertTrue(target.isAfterLast());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
//          Go back in position with forward only cursor
            assertEquals(ResultSet.TYPE_FORWARD_ONLY, target.getFetchDirection());
            target.absolute(2);
            target.absolute(1);
            fail("Should get SQLException");
        } catch (SQLException e) {
            // ok
        }
        
        
        
    }

    /**
     * Test method for {@link java.sql.ResultSet#afterLast()}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails",
        method = "afterLast",
        args = {}
    )
    @KnownFailure("res.close() does not wrap up")
    public void testAfterLast() {
        try {
            target.afterLast();
            assertTrue(target.isAfterLast());
            assertFalse(target.next());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            emptyTarget.afterLast();
            assertFalse(emptyTarget.isAfterLast());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        
        try {
            target.close();
            target.beforeFirst();
            fail("Should get SQLException");
        } catch (SQLException e) {
            
        }
        
        
    }

    /**
     * Test method for {@link java.sql.ResultSet#beforeFirst()}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails",
        method = "beforeFirst",
        args = {}
    )
    @KnownFailure("statment.close() does not wrap up")
    public void testBeforeFirst() {
        
        try {
            target.beforeFirst();
            assertTrue(target.isBeforeFirst());
            assertTrue(target.next());
            assertFalse(target.isBeforeFirst());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            emptyTarget.beforeFirst();
            assertFalse(emptyTarget.isBeforeFirst());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        
        try {
            target.close();
            target.beforeFirst();
            fail("Should get SQLException");
        } catch (SQLException e) {
            
        }
        
       
    }

    /**
     * Test method for {@link java.sql.ResultSet#clearWarnings()}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "Not supported",
        method = "clearWarnings",
        args = {}
    )
    @KnownFailure("Not supported")
    public void testClearWarnings() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link java.sql.ResultSet#close()}. 
     * 
     * According to the JDBC spec close has to "Releases this ResultSet
     * object's database and JDBC resources immediately", and this implies
     * the fields should be released as well (so that garbage collection 
     *  can take place)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test immediate release of resources, test fails",
        method = "close",
        args = {}
    )
    @KnownFailure("Resultset.close() does not wrap up")
    public void testClose1() {
        try {
            target.close();
            target.next();
            fail("Should get SQLException");
        } catch (SQLException e) {
            //ok
        }
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#close()}. 
     * 
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "test that exception in one prepared statement does not affect second statement. (Atomicity Rule)",
        method = "close",
        args = {}
    )
    public void testClose() {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        try {
            
            Statement s = conn.createStatement();
            s.addBatch("create table t1 (a text);");

            s.addBatch("insert into t1 values('abc');");
            s.addBatch("insert into t1 values('def');");
            s.addBatch("insert into t1 values('ghi');");
            s.executeBatch();
            s.close();

            conn.commit();
            ps1 = conn.prepareStatement("select * from t1");
            ps2 = conn
                    .prepareStatement("select * from t1 whe a like '?000'");

            ResultSet rs1 = ps1.executeQuery();

            try {
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()){
                    // do nothing
                }
                fail("Should get SQLException");
            } catch (SQLException sqle) {
                // ok : Division by zero
            }

            // Although exception happened on ps2 rs1 should still work 
            // Isolation property if ACID rules
            
            while (rs1.next()) {
                // do nothing: switching of rows should be possible
            }

            conn.commit();

            rs1.close();
            ps1.close();
            ps2.close();
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            try {
                if (ps1 != null) ps1.close();
                if (ps2 != null) ps2.close();
                conn.rollback();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    

    /**
     * Test method for {@link java.sql.ResultSet#findColumn(java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "findColumn",
        args = {java.lang.String.class}
    )
    public void testFindColumn() {
        try {
            assertEquals(1, target.findColumn("id"));
            assertEquals(2, target.findColumn("name"));
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
           
        try {
            target.findColumn("bla");
            fail("Should get SQLException");
        } catch (SQLException e) {
            // ok
        }
    }

    /**
     * Test method for {@link java.sql.ResultSet#first()}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails",
        method = "first",
        args = {}
    )
    @KnownFailure("statment.close() does not wrap up")
    public void testtestFirst() {
        try {
            assertFalse(emptyTarget.first());
            assertTrue(target.first());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        
        try {
            target.close();
            // releases all resources such that it can be finalized!
            target.first();
            fail("Should get SQLException");
        } catch (SQLException e) {
            
        }
        
        
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#isAfterLast()}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails",
        method = "isAfterLast",
        args = {}
    )
    @KnownFailure("statment.close() does not wrap up")
    public void testtestIsAfterLast() {
        try {
            assertFalse(target.isAfterLast());
            target.absolute(-1); // last
            target.next();
            assertTrue(target.isAfterLast());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            assertFalse(emptyTarget.isAfterLast());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        
        try {
            target.close();
            // releases all resources such that it can be finalized!
            target.isAfterLast();
            fail("Should get SQLException");
        } catch (SQLException e) {
            
        }
        
    }

    /**
     * Test method for {@link java.sql.ResultSet#isBeforeFirst()}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails",
        method = "isBeforeFirst",
        args = {}
    )
    @KnownFailure("In Second code block assertion fails. statment. "+
            "close() does not wrap up")
    public void testtestIsBeforeFirst() {
        try {
            assertTrue(target.isBeforeFirst());
            assertTrue(target.next());
            assertFalse(target.isBeforeFirst());
            assertTrue(target.isFirst());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        
        try {
            assertTrue(emptyTarget.isBeforeFirst());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        
        try {
            target.close();
            // releases all resources such that it can be finalized!
            target.isBeforeFirst();
            fail("Should get SQLException");
        } catch (SQLException e) {
            //ok
        }
        
    }

    /**
     * Test method for {@link java.sql.ResultSet#isFirst()}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails",
        method = "isFirst",
        args = {}
    )
    @KnownFailure("statment.close() does not wrap up")
    public void testtestIsFirst() {
        try {
            assertFalse(target.isFirst());
            target.first();
            assertTrue(target.isFirst());
            target.next();
            assertFalse(target.isFirst());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            assertFalse(emptyTarget.isFirst());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        
        try {
            target.close();
            // releases all resources such that it can be finalized!
            target.isFirst();
            fail("Should get SQLException");
        } catch (SQLException e) {
            
        }
       
    }

    /**
     * Test method for {@link java.sql.ResultSet#isLast()}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails. Test for empty result set fails",
        method = "isLast",
        args = {}
    )
    @KnownFailure("Second block first assertion fails. Is Last should evaluate "+
            "true if the row on which the cursor is actually provides a result."+
            "statment.close() does not wrap up")
    public void testtestIsLast() {
        
        try {
            assertFalse(target.isLast());
            target.absolute(-1);
            assertTrue(target.isLast());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        //check default value no valid row
        try {
            assertFalse(emptyTarget.isLast());
            assertFalse(emptyTarget.next());
            assertFalse(emptyTarget.isLast());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        
        
        try {
            target.close();
            target.isLast();
            fail("Should get SQLException");
        } catch (SQLException e) {
            // ok
        }
       
    }

    /**
     * Test method for {@link java.sql.ResultSet#last()}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails",
        method = "last",
        args = {}
    )
    @KnownFailure("statment.close() does not wrap up")
    public void testtestLast() {
        try {
            assertFalse(target.isLast());
            target.last();
            assertTrue(target.isLast());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        
        try {
            target.close();
            target.last();
            fail("Should get SQLException");
        } catch (SQLException e) {
            // ok
        }
      
    }

    /**
     * Test method for {@link java.sql.ResultSet#next()}.
     * @throws SQLException 
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SQLException checking test fails. Clearing of warnings"+
                " and closed streams not supported.",
        method = "next",
        args = {}
    )
    @KnownFailure("Resultset.close() does not wrap up")
    public void testNext() throws SQLException {
        try {
            //before first - first
            assertTrue(target.next());
            //first - second
            assertTrue(target.next());
            //after last
            assertFalse(target.next());
            assertTrue(target.isAfterLast());
            // one more
            assertFalse(target.next());
            
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            assertFalse(emptyTarget.next());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        target.close();
        try {
            target.next();
            fail("Exception expected");
        } catch (SQLException e) {
            //ok
        }
        
    }

    /**
     * Test method for {@link java.sql.ResultSet#previous()}.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "tests SQLException",
        method = "previous",
        args = {}
    )
    public void testPrevious() {
        
        try {
            assertEquals(ResultSet.FETCH_FORWARD, target.getFetchDirection());
            target.last();
            target.previous();
            fail("Should get SQLException");
        } catch (SQLException e) {
            // ok
        }
    }
    
    /**
     * Test method for {@link java.sql.ResultSet#previous()}.
     * @throws SQLException 
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "not supported",
        method = "previous",
        args = {}
    )
    @KnownFailure("not supported")
    public void testPrevious2() throws SQLException {
        try {
            assertSame(ResultSet.TYPE_SCROLL_INSENSITIVE, scrollableTarget.getFetchDirection());
            
            target.first();
            target.previous();
            assertTrue(target.isBeforeFirst());
            
            target.last();
            target.next();
            target.previous();
            assertFalse(target.isAfterLast());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        target.close();
        try {
            target.previous();
            fail("Exception expected");
        } catch (SQLException e) {
            //ok
        }
    }

    /**
     * Test method for {@link java.sql.ResultSet#relative(int)}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test fails: no exception is thrown when moving cursor backwards",
        method = "relative",
        args = {int.class}
    )
    @KnownFailure("no exception is thrown when moving cursor backwards"
            +" on forward only statement")
    public void testRelative() {

        // forward only
        try {
            int initialRow = target.getRow();
            assertFalse(target.relative(0));
            assertEquals(initialRow, target.getRow());
            
            assertTrue(target.relative(1));
            assertTrue(target.isFirst());
            assertEquals(1, target.getRow());

            assertTrue(target.relative(1));
            assertFalse(target.isFirst());
            assertEquals(2, target.getRow());
            
            
            assertFalse(target.relative(2));

        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        

        try {
            // should not be able to scroll backwards in forward only RS 
            target.relative(-2);
            assertEquals(2,target.getRow());
            fail("Should get SQLException");
        } catch (SQLException e) {
            // ok
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            assertFalse(emptyTarget.relative(Integer.MAX_VALUE));
            assertTrue(emptyTarget.isAfterLast());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

    }
    
    /**
     * Test method for {@link java.sql.ResultSet#relative(int)}.
     * @throws SQLException 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test fails: does not move before first row on min_value",
        method = "relative",
        args = {int.class}
    )
    @KnownFailure("Scrollable resultSet. Not supported")
    public void testRelativeScrollableResultSet() throws SQLException {
     // scrollable resultSet 
        try {
            
            int initialRow = scrollableTarget.getRow();
            assertFalse(scrollableTarget.relative(0));
            assertEquals(initialRow, scrollableTarget.getRow());
            
            assertTrue(scrollableTarget.relative(1));
            assertTrue(scrollableTarget.isFirst());
            assertEquals(1, scrollableTarget.getRow());

            assertTrue(scrollableTarget.relative(1));
            assertFalse(scrollableTarget.isFirst());

        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        try {
            assertEquals(2, scrollableTarget.getRow());
            assertFalse(scrollableTarget.relative(2));
            scrollableTarget.relative(-2);
            assertEquals(2,scrollableTarget.getRow());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            assertFalse(scrollableTarget.relative(Integer.MIN_VALUE));
            assertTrue(scrollableTarget.isBeforeFirst());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        stScrollable.close();
        try {
            scrollableTarget.relative(1);
            fail("Exception expected");
        } catch (SQLException e) {
            //ok
        }
    }
    

   
    /**
     * Test method for {@link java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object)}.
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "updateObject",
        args = {java.lang.String.class, java.lang.Object.class}
    )
    @KnownFailure("not supported")
    public void testUpdateObjectStringObject() {
        try {
           writableTarget.next();
           writableTarget.updateObject("family","bird");
        } catch (SQLException e) {
           fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
           target.next();
           target.updateObject("family","bird");
           fail("SQLException was not thrown");
        } catch (SQLException e) {
           fail("Unexpected exception: " + e.getMessage());
        }
    }

    
    /**
     * Test method for {@link java.sql.ResultSet#updateString(java.lang.String, java.lang.String)}.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported. Only exception testing. Missing testing for wrong type",
        method = "updateString",
        args = {java.lang.String.class, java.lang.String.class}
    )
    @KnownFailure("Feature not supported")
    public void testUpdateStringStringString() throws Exception {
        try {
            writableTarget.next();
            writableTarget.updateString("family","bird");
         } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
         }
         
         // non writable target.
         try {
            target.next();
            target.updateString("family","bird");
            fail("SQLException was not thrown");
         } catch (SQLException e) {
            //ok
         }
         
         
         // writable but wrong type
         try {
             target.updateString(1,"test");
         } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
         }
         
         target.close();
         
      // Exception test
         try {
             target.updateString("family","test");
             fail("Exception expected");
         } catch (SQLException e) {
            //ok
         }
    }

    /**
     * Test method for {@link java.sql.ResultSet#wasNull()}.
     * Spec sais: if something was read... -> if nothing was read it should be false
     * @throws SQLException 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "failing tests.",
        method = "wasNull",
        args = {}
    )
    @KnownFailure("the default tests, and exception tests fail.")
    public void testWasNull() throws SQLException {
        
        // Check default: select statement executed but no get on target called yet
        // Either false or throw an exception.
        try {
            assertFalse(target.wasNull());
        } catch (SQLException e) {
            //ok
        }
        
        
        try {
            stForward.execute("insert into zoo values(8,null,null);");
            stForward.execute(selectAllAnimals);
            target = stForward.getResultSet();
            assertNotNull(target);
            assertTrue(target.last());
            assertNull(target.getObject(2));
            assertTrue(target.wasNull());
            assertNotNull(target.getObject(1));
            assertFalse(target.wasNull());
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        target.close();
        try {
            target.wasNull();
            fail("Exception expected");
        } catch (SQLException e) {
            //ok
        }
    }
}
