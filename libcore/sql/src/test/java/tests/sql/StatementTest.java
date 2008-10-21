/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.sql;

import java.sql.BatchUpdateException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Vector;

public class StatementTest extends SQLTest {
    
    /**
     * @test java.sql.Statement#addBatch(String)
     */
    public void testAddBatch() throws SQLException {
        
        Statement st = null;
        try {
            st = conn.createStatement();
            st.addBatch("INSERT INTO zoo VALUES (3,'Tuzik','dog')");
            st.addBatch("INSERT INTO zoo VALUES (4,'Mashka','cat')");

            int[] updateCounts = st.executeBatch();
            assertEquals(2, updateCounts.length);
            assertEquals(1, updateCounts[0]);
            assertEquals(1, updateCounts[1]);

        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }

        try {
            st = conn.createStatement();
            st.addBatch("");
            st.executeBatch();
            fail("SQLException is not thrown");
        } catch (SQLException e) {
            // expected
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }

        try {
            st = conn.createStatement();
            st.addBatch(null);
            st.executeBatch();
        } catch (SQLException e) {
            // expected
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.Statement#clearWarnings()
     */
    public void testClearWarnings() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.execute("select animals from zoo");
        } catch (SQLException e) {
            // expected
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st = conn.createStatement();
            st.clearWarnings();
            SQLWarning w = st.getWarnings();
            assertNull(w);
        } catch (Exception e) {
            fail("Unexpected Exception: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.Statement#getWarnings()
     * 
     * TODO getWarnings is not supported
     */ 
/*    public void testGetWarnings() {

        Statement st = null;
        int errorCode1 = -1;
        int errorCode2 = -1;

        try {
            st = conn.createStatement();
            st.execute("select animals from zoooo");
        } catch (SQLException e) {
            // expected
            errorCode1 = e.getErrorCode();
        }
        try {
            SQLWarning wrs = st.getWarnings();
            assertNull(wrs);
        } catch (Exception e) {
            fail("Unexpected Exception: " + e.getMessage());
        }
        try {
            st.execute("select horse from zoooooo");
        } catch (SQLException e) {
            // expected
            errorCode2 = e.getErrorCode();
        }

        try {
            SQLWarning wrs = st.getWarnings();
            assertEquals(errorCode1, wrs.getErrorCode());
            assertNotNull(wrs.getNextWarning());
            assertEquals(errorCode2, wrs.getErrorCode());
        } catch (Exception e) {
            fail("Unexpected Exception: " + e.getMessage());
        }
        
        try {
            st.close();
        } catch (SQLException ee) {
        }

    }
*/
    /**
     * @test java.sql.Statement#clearBatch(String)
     */
    public void testClearBatch() throws SQLException {
        
        Statement st = null;
        
        try {
            st = conn.createStatement();
            st.addBatch("INSERT INTO zoo VALUES (3,'Tuzik','dog'); ");
            st.addBatch("INSERT INTO zoo VALUES (4,'Mashka','cat')");

            int[] updateCounts = st.executeBatch();
            assertEquals(2, updateCounts.length);
            assertEquals(1, updateCounts[0]);
            assertEquals(1, updateCounts[1]);

        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }

        try {
            st = conn.createStatement();
            st.addBatch("");
            st.executeBatch();
            fail("SQLException is not thrown");
        } catch (SQLException e) {
            // expected
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }

        try {
            st = conn.createStatement();
            st.addBatch(null);
            st.executeBatch();
        } catch (SQLException e) {
            // expected
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.Statement#execute(String sql)
     * 
     * TODO not pass on SQLite and RI.
     * 
     */
    public void testExecute() throws SQLException {

        String[] queries = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "select animal_id, address from hutch where animal_id=1;",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;" };
        boolean[] results = {true, true, true, true, true, true, true,
                true, true};

        for (int i = 0; i < queries.length; i++) {
            Statement st = null;
            try {
                st = conn.createStatement();
                boolean res = st.execute(queries[i]);
                assertEquals(results[i], res);
            } catch (SQLException e) {
                fail("SQLException is thrown: " + e.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }
        }

        String[] inc_queries = {
                "update zoo_zoo set name='Masha', family='cat' where id=5;",
                "drop table hutchNO",
                "insert into hutch (id, animal_id, address) values (1, 2, 10);",
                "select animal_id, from hutch where animal_id=1;",
                "drop view address;", "drop table hutch;", "", null };

        for (int i = 0; i < inc_queries.length; i++) {
            Statement st = null;
            try {
                st = conn.createStatement();
                st.execute(inc_queries[i]);
                fail("SQLException is not thrown for query: " + inc_queries[i]);
            } catch (SQLException e) {
                // expected
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            } 
        }
    }

    /**
     * @test java.sql.Statement#execute(String sql, int autoGeneratedKeys)
     * TODO not supported 
     */
 /*   public void testExecute_String_int() {
        String[] queries = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "select animal_id, address from hutch where animal_id=1;",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;" };

        for (int i = 0; i < queries.length; i++) {
            Statement st = null;
            try {
                st = conn.createStatement();
                st.execute(queries[i], Statement.NO_GENERATED_KEYS);
                ResultSet rs = st.getGeneratedKeys();
                assertFalse(rs.next());
            } catch (SQLException e) {
                fail("SQLException is thrown: " + e.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            } 
        }
    }
*/
    /**
     * @test java.sql.Statement#getConnection()
     */
    public void testGetConnection() {
        Statement st = null;
        try {
            st = conn.createStatement();
            assertSame(conn, st.getConnection());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.Statement#getFetchDirection()
     */
    public void testGetFetchDirection() {
        Statement st = null;
        try {
            st = conn.createStatement();
            assertEquals(ResultSet.FETCH_UNKNOWN, st.getFetchDirection());
        } catch (SQLException e) {
            fail("SQLException is thrown" + e.getMessage());
        }  finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.Statement#setFetchDirection()
     * TODO not supported
     */
/*    public void testSetFetchDirection() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.setFetchDirection(ResultSet.FETCH_FORWARD);
            assertEquals(ResultSet.FETCH_FORWARD, st.getFetchDirection());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st = conn.createStatement();
            st.setFetchDirection(-1);
            fail("SQLException is not thrown");
        } catch (SQLException e) {
            // expected
        }  finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }

        try {
            st = conn.createStatement();
            st.setFetchDirection(100);
            fail("SQLException is not thrown");
        } catch (SQLException e) {
            // expected
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
*/
    /**
     * @test java.sql.Statement#getFetchSize()
     */
    public void testGetFetchSize() {
        Statement st = null;
        try {
            st = conn.createStatement();
            assertEquals(1, st.getFetchSize());
        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.Statement#setFetchSize()
     * TODO not supported
     */
/*    public void testSetFetchSize() {
        Statement st = null;
        try {
            st = conn.createStatement();
            int rows = 100;
            for (int i = 0; i < rows; i++) {
                try {
                    st.setFetchSize(i);
                    assertEquals(i, st.getFetchSize());
                } catch (SQLException sqle) {
                    fail("SQLException is thrown: " + sqle.toString());
                }
            }
            try {
                st.setFetchSize(-1);
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
*/
    /**
     * @test java.sql.Statement#setMaxFieldSize(int max)
     * TODO not supported
     */
/*    public void testSetMaxFieldSize() {
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 0; i < 300; i += 50) {
                try {
                    st.setMaxFieldSize(i);
                    assertEquals(i, st.getMaxFieldSize());
                } catch (SQLException sqle) {
                    fail("SQLException is thrown: " + sqle.getMessage());
                }
            }
            try {
                st.setMaxFieldSize(-1);
                fail("SQLException isn't thrown");
            } catch (SQLException sqle) {
                // expecteds
            }
        } catch (SQLException e) {
            fail("Can't create statement, SQLException is thrown: "
                    + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
*/
    /**
     * @test java.sql.Statement#getMaxFieldSize()
     * TODO not supported
     */
/*    public void testGetMaxFieldSize() {
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 200; i < 500; i += 50) {
                try {
                    st.setMaxFieldSize(i);
                    assertEquals(i, st.getMaxFieldSize());
                } catch (SQLException sqle) {
                    fail("SQLException is thrown: " + sqle.getMessage());
                }
            }
        } catch (SQLException e) {
            fail("Can't create statement, SQLException is thrown: "
                    + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
*/
    /**
     * @test java.sql.Statement#setMaxRows(int max)
     * TODO not supported
     */
 /*   public void testSetMaxRows() {
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 0; i < 300; i += 50) {
                try {
                    st.setMaxRows(i);
                    assertEquals(i, st.getMaxRows());
                } catch (SQLException sqle) {
                    fail("SQLException is thrown: " + sqle.getMessage());
                }
            }
            try {
                st.setMaxRows(-1);
                fail("SQLException isn't thrown");
            } catch (SQLException sqle) {
                // expecteds
            }
        } catch (SQLException e) {
            fail("Can't create statement, SQLException is thrown: "
                    + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
*/
    /**
     * @test java.sql.Statement#getMaxRows()
     * TODO not supported
     */
/*    public void testGetMaxRows() {
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 200; i < 500; i += 50) {
                try {
                    st.setMaxRows(i);
                    assertEquals(i, st.getMaxRows());
                } catch (SQLException sqle) {
                    fail("SQLException is thrown: " + sqle.getMessage());
                }
            }
        } catch (SQLException e) {
            fail("Can't create statement, SQLException is thrown: "
                    + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
*/
    /**
     * @test java.sql.Statement#close()
     * TODO not passed but according to Java Docs
     */
    public void testClose() {
        Statement st = null;
        try {
            String[] queries = {
                    "update zoo set name='Masha', family='cat' where id=2;",
                    "insert into zoo (id, name, family) values (3, 'Vorobey', 'sparrow');",
                    "insert into zoo (id, name, family) values (4, 'Slon', 'elephant');",
                    "select * from zoo" };
            st = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st.execute(queries[i]);
            }
            assertNotNull(st.getResultSet());
            st.close();
            assertNull(st.getResultSet());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.Statement#execute(String sql, int[] columnIndexes)
     * TODO not supported
     */
/*    public void testExecute_String_intArray() {
        Statement st = null;
        try {
            String[] queries = {
                    "update zoo set name='Masha', family='cat' where id=2;",
                    "insert zoo(id, name, family) values (3, 'Vorobey', 'sparrow');",
                    "insert zoo(id, name, family) values (4, 'Slon', 'elephant');",
                    "select * from zoo" };
            Vector<int[]> array = new Vector<int[]>();
            array.addElement(null);
            array.addElement(new int[] { 1, 2, 3 });
            array.addElement(new int[] { 1, 2, 10, 100 });
            array.addElement(new int[] {});

            st = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st.execute(queries[i], (int[]) array.elementAt(i));
            }
            assertNotNull(st.getResultSet());
            st.close();
            assertNull(st.getResultSet());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
*/
    /**
     * @test java.sql.Statement#execute(String sql, String[] columnNames)
     */
/*    public void testExecute_String_StringArray() {
        Statement st = null;
        try {
            String[] queries = {
                    "update zoo set name='Masha', family='cat' where id=2;",
                    "insert zoo(id, name, family) values (3, 'Vorobey', 'sparrow');",
                    "insert zoo(id, name, family) values (4, 'Slon', 'elephant');",
                    "select * from zoo" };
            Vector<String[]> array = new Vector<String[]>();
            array.addElement(null);
            array.addElement(new String[] { "", "", "", "", "", "", "", "" });
            array.addElement(new String[] { "field 1", "", "field2" });
            array.addElement(new String[] { "id", "family", "name" });

            st = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st.execute(queries[i], (String[]) array.elementAt(i));
            }
            assertNotNull(st.getResultSet());
            st.close();
            assertNull(st.getResultSet());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 
    }
*/
    /**
     * @test java.sql.Statement#executeBatch()
     */
    public void testExecuteBatch() {

        String[] queries = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;" };

        int[] result = { 1, 1, 1, 1, 1, 1, 1, 1 };
        Statement st = null;
        try {
            st = conn.createStatement();
            assertEquals(0, st.executeBatch().length);
            for (int i = 0; i < queries.length; i++) {
                st.addBatch(queries[i]);
            }
            int[] resArray = st.executeBatch();
            assertTrue(java.util.Arrays.equals(result, resArray));
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 

        try {
            st = conn.createStatement();
            st.addBatch("select * from zoo");
            st.executeBatch();
        } catch (BatchUpdateException bue) {
            fail("BatchUpdateException is thrown: " + bue.toString());
        } catch (SQLException sqle) {
            fail("Unknown SQLException is thrown: " + sqle.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 
    }

    /**
     * @test java.sql.Statement#executeQuery(String sql)
     */
    public void testExecuteQuery_String() {

        String[] queries1 = { "select * from zoo",
                "select name, family from zoo where id = 1" };

        String[] queries2 = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;", "select from zoo" };
        
        Statement st = null;
        
        try {
            st = conn.createStatement();
            for (int i = 0; i < queries1.length; i++) {
                try {
                    ResultSet rs = st.executeQuery(queries1[i]);
                    assertNotNull(rs);
                } catch (SQLException sqle) {
                    fail("SQLException is thrown for query: " + queries1[i]);
                }
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (Exception ee) {
            }
        } 

        try {
            st = conn.createStatement();
            for (int i = 0; i < queries2.length; i++) {
                try {
                    st.executeQuery(queries2[i]);
                    fail("SQLException is not thrown for query: " + queries2[i]);
                } catch (SQLException sqle) {
                    // expected
                }
            }
        } catch (SQLException sqle) {
            fail("Unknown SQLException is thrown: " + sqle.getMessage());
        } finally {
            try {
                st.close();
            } catch (Exception ee) {
            }
        } 

    }

    /**
     * @test java.sql.Statement#executeUpdate(String sql)
     */
    public void testExecuteUpdate_String() {

        String[] queries1 = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;" };

        String[] queries2 = { "select * from zoo",
                "select name, family from zoo where id = 1" };
        
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 0; i < queries1.length; i++) {
                try {
                    st.executeUpdate(queries1[i]);
                } catch (SQLException e) {
                    fail("SQLException is thrown: " + e.getMessage());
                }
            }

            for (int i = 0; i < queries2.length; i++) {
                try {
                    st.executeUpdate(queries2[i]);
                    fail("SQLException is not thrown for query: " + queries2[i]);
                } catch (SQLException e) {
                    // expected
                }
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (Exception ee) {
            }
        } 
    }

    /**
     * @test java.sql.Statement#executeUpdate(String sql, int[] columnIndexes)
     * 
     * TODO executeUpdate(String sql, int[] columnIndexes) is not supported
     */
/*    public void testExecuteUpdate_String_intArray() {
        Statement st = null;
        try {
            String[] queries1 = {
                    "update zoo set name='Masha', family='cat' where id=2;",
                    "drop table if exists hutch",
                    "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                    "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                    "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                    "create view address as select address from hutch where animal_id=2",
                    "drop view address;", "drop table hutch;" };

            Vector<int[]> array = new Vector<int[]>();
            array.addElement(null);
            array.addElement(new int[] { 1, 2, 3 });
            array.addElement(new int[] { 1, 2, 10, 100 });
            array.addElement(new int[] {});
            array.addElement(new int[] { 100, 200 });
            array.addElement(new int[] { -1, 0 });
            array.addElement(new int[] { 0, 0, 0, 1, 2, 3 });
            array.addElement(new int[] { -100, -200 });

            st = conn.createStatement();
            for (int i = 0; i < queries1.length; i++) {
                st.executeUpdate(queries1[i], (int[]) array.elementAt(i));
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 
    }
*/
    /**
     * @test java.sql.Statement#executeUpdate(String sql, int autoGeneratedKeys)
     * 
     * TODO  executeUpdate(String sql, int autoGeneratedKeys) is not supported
     */
/*    public void testExecuteUpdate_String_int() {
        String[] queries = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "select animal_id, address from hutch where animal_id=1;",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;" };

        for (int i = 0; i < queries.length; i++) {
            Statement st = null;
            ResultSet rs = null;
            try {
                st = conn.createStatement();
                st.executeUpdate(queries[i], Statement.NO_GENERATED_KEYS);
                rs = st.getGeneratedKeys();
                assertFalse(rs.next());
            } catch (SQLException e) {
                fail("SQLException is thrown: " + e.getMessage());
            } finally {
                try {
                    rs.close();
                    st.close();
                } catch (Exception ee) {
                }
            } 
        }
    }
*/
    /**
     * @test java.sql.Statement#executeUpdate(String sql, String[] columnNames)
     * 
     * TODO executeUpdate(String sql, String[] columnNames) is not supported
     */
/*    public void testExecuteUpdate_String_StringArray() {
        Statement st = null;
        try {
            String[] queries = {
                    "update zoo set name='Masha', family='cat' where id=2;",
                    "drop table if exists hutch",
                    "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                    "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                    "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                    "create view address as select address from hutch where animal_id=2",
                    "drop view address;", "drop table hutch;" };

            Vector<String[]> array = new Vector<String[]>();
            array.addElement(null);
            array.addElement(new String[] { "", "", "", "", "", "", "", "" });
            array.addElement(new String[] { "field 1", "", "field2" });
            array.addElement(new String[] { "id", "family", "name" });
            array
                    .addElement(new String[] { "id", null, "family", null,
                            "name" });
            array.addElement(new String[] { "id", " ", "name" });
            array.addElement(new String[] { null, null, null, null });
            array.addElement(new String[] { " ", "123 21", "~!@#$%^&*()_+ ",
                    null });

            st = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st.executeUpdate(queries[i], (String[]) array.elementAt(i));
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 
    }
*/
    /**
     * @test java.sql.Statement#getUpdateCount()
     */
    public void testGetUpdateCount() {
        Statement st = null;
        try {
            String query = "update zoo set name='Masha', family='cat' where id=2;";
            st = conn.createStatement();
            assertEquals(0, st.getUpdateCount());
            st.executeUpdate(query);
            assertEquals(1, st.getUpdateCount());
            query = "update zoo set name='Masha', family='cat' where id=5;";
            st.executeUpdate(query);
            assertEquals(0, st.getUpdateCount());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 
    }
}
