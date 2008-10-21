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

import java.math.BigDecimal;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class PreparedStatementTest extends SQLTest {

    String queryAllSelect = "select * from type";

    String[] queries = {
            "create table type (" +

            " BoolVal BOOLEAN," + " IntVal INT," + " LongVal LONG,"
                    + " Bint BIGINT," + " Tint TINYINT," + " Sint SMALLINT,"
                    + " Mint MEDIUMINT, " +

                    " IntegerVal INTEGER, " + " RealVal REAL, "
                    + " DoubleVal DOUBLE, " + " FloatVal FLOAT, "
                    + " DecVal DECIMAL, " +

                    " NumVal NUMERIC, " + " charStr CHAR(20), "
                    + " dateVal DATE, " + " timeVal TIME, " + " TS TIMESTAMP, "
                    +

                    " DT DATETIME, " + " TBlob TINYBLOB, " + " BlobVal BLOB, "
                    + " MBlob MEDIUMBLOB, " + " LBlob LONGBLOB, " +

                    " TText TINYTEXT, " + " TextVal TEXT, "
                    + " MText MEDIUMTEXT, " + " LText LONGTEXT " + ");",

            "insert into type (BoolVal, IntVal, LongVal, Bint, Tint, Sint, Mint,"
                    + "IntegerVal, RealVal, DoubleVal, FloatVal, DecVal,"
                    + "NumVal, charStr, dateVal, timeVal, TS,"
                    + "DT, TBlob, BlobVal, MBlob, LBlob,"
                    + "TText, TextVal, MText, LText"
                    + ") "
                    + "values (1, -1, 22, 2, 33,"
                    + "3, 1, 2, 3.9, 23.2, 33.3, 44,"
                    + "5, 'test string', '1799-05-26', '12:35:45', '2007-10-09 14:28:02.0',"
                    + "'1221-09-22 10:11:55', 1, 2, 3, 4,"
                    + "'Test text message tiny', 'Test text message', 'Test text message medium', 'Test text message long');" };

    public void createTables() {
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st.execute(queries[i]);
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.toString());
        } finally {
            try {
                st.close();
            } catch (Exception ee) {
            }
        }
    }

    public void clearTables() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.execute("drop table if exists type");
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
     * @test java.sql.PreparedStatement#addBatch()
     */
    public void testAddBatch() throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn
                    .prepareStatement("INSERT INTO zoo VALUES (3,'Tuzik', ?);");
            ps.addBatch("INSERT INTO zoo VALUES (?,'Burenka', ?); ");
            ps.addBatch("INSERT INTO zoo VALUES (?,'Mashka','cat')");
            try {
                ps.executeBatch();
            } catch (SQLException sqle) {
                fail("SQLException is thrown for executeBatch()");
            }
            ps.setString(1, "dog");
            Statement st = null;
            try {
                ps.executeBatch();
                st = conn.createStatement();
                st.execute("select * from zoo");
                ResultSet rs = st.getResultSet();
                assertEquals(2, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown for executeBatch()");
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }
        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                ps.close();
            } catch (SQLException ee) {
            }
        }

        try {
            ps = conn
                    .prepareStatement("INSERT INTO zoo VALUES (3,'Tuzik', ?);");
            ps.addBatch("");
        } catch (SQLException e) {
            // expected
        } finally {
            try {
                ps.close();
            } catch (SQLException ee) {
            }
        }

        try {
            ps = conn
                    .prepareStatement("INSERT INTO zoo VALUES (3,'Tuzik', ?);");
            ps.addBatch(null);
        } catch (SQLException e) {
            // expected
        } finally {
            try {
                ps.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#execute()
     */

    // TODO Crashes VM. Fix later.

/*      public void testExecute() { 
          Statement st = null; 
          PreparedStatement ps = null; 
          try { 
              String query = "insert into zoo(id, family, name) values(?, ?, 'unknown animal')"; 
              ps = conn.prepareStatement(query);
              ps.setInt(1, 3);
              ps.setString(2, "No name");
              assertTrue(ps.execute()); 
              st = conn.createStatement(); 
              st.execute("select * from zoo"); 
              assertEquals(3, getCount(st.getResultSet())); 
       } catch (SQLException e) {
              fail("SQLException is thrown: " + e.getMessage()); 
       } finally { 
           try {
                  ps.close(); 
                  st.close(); 
              } catch(Exception ee) {} 
      }
      
         try { 
              String query = "update zoo set name='Masha', family=? where id=?;";
              ps = conn.prepareStatement(query); 
              ps.setString(1, "cat"); 
              ps.setInt(2, 2); 
              assertTrue(ps.execute()); 
              assertEquals(1, ps.getUpdateCount()); 
              st = conn.createStatement(); 
              st.execute("select family from zoo where id=2");
             ResultSet rs = st.getResultSet(); 
              rs.next(); assertEquals("cat", rs.getString(1)); 
          } catch (SQLException e) { 
              fail("SQLException is thrown: " + e.getMessage()); 
       } finally { 
           try { 
               ps.close(); 
               st.close(); 
           } catch(Exception ee) {} 
       }
      
          try { 
              conn.createStatement().execute("drop table if exists hutch");
              String query = "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));"; 
              ps = conn.prepareStatement(query); 
              assertTrue(ps.execute()); 
          } catch(SQLException e) { 
              fail("SQLException is thrown: " + e.getMessage()); 
         } finally { 
              try { 
                  ps.close(); 
              } catch(Exception ee) {} 
          }
     
          try { 
              String query = "select name, family from zoo where id = ?"; 
              ps = conn.prepareStatement(query); 
              ps.setInt(1, 1); 
              assertTrue(ps.execute()); 
          } catch (SQLException e) { 
              fail("SQLException is thrown: " + e.getMessage()); 
         } finally { 
              try { 
                  ps.close(); 
              } catch(Exception ee){} 
      }
      
          try { 
              String query = "select name, family from zoo where id = ?"; 
              ps = conn.prepareStatement(query); 
              ps.execute(); 
          } catch (SQLException e) { 
              fail("SQLException is thrown"); 
      } finally { 
              try {
                  ps.close(); 
              } catch(Exception ee) {} 
          } 
      }
*/     
    /*
     * @test java.sql.PreparedStatement#executeQuery()
     */
    
     // TODO Crashes VM. Fix later.
 /*     public void testExecuteQuery() {
      
          String[] queries2 = { "update zoo set name='Masha', family='cat' where id=;", 
                                "insert into hutch (id, animal_id, address) values (1, ?,'Birds-house, 1');", 
                                "insert into hutch (id, animal_id, address) values (?, 1, 'Horse-house, 5');", 
                                "create view address as select address from hutch where animal_id=?" };
      
          for (int i = 0; i < queries2.length; i++) { 
              PreparedStatement ps = null;
              try { 
                  ps = conn.prepareStatement(queries2[i]); 
                  ps.executeQuery();
                  fail("SQLException is not thrown for query: " + queries2[i]); 
              } catch(SQLException sqle) { 
                  // expected 
              } finally { 
                  try { 
                      ps.close(); 
                  } catch(Exception ee) {} 
              } 
          }
      
          String query = "select * from zoo where id = ?"; 
          PreparedStatement ps = null; 
          try { 
              ps = conn.prepareStatement(query); 
              ps.setInt(1, 1); 
              ResultSet rs = ps.executeQuery(); 
              rs.next(); 
              assertEquals(1, rs.getInt(1));
              assertEquals("Kesha", rs.getString(2)); 
              assertEquals("parrot", rs.getString(3)); 
          } catch (SQLException e) { 
              fail("SQLException is thrown for query"); 
          } finally { 
              try { 
                  ps.close(); 
              } catch(Exception ee) {} 
          }
          
          try { 
              ps = conn.prepareStatement(query); 
              ps.setInt(1, 5); 
              ResultSet rs = ps.executeQuery(); 
              assertNotNull(rs); 
              assertFalse(rs.next()); 
          } catch(SQLException e) { 
              fail("SQLException is thrown for query"); 
          } finally {
              try { 
                  ps.close(); 
              } catch(Exception ee) {} 
          } 
      }
*/     
    /**
     * @test java.sql.PreparedStatement#executeUpdate()
     */
    
     // TODO Crashes VM. Fix later.
/*      public void testExecuteUpdate() { 
          String[] queries1 = { "insert into hutch (id, animal_id, address) values (1, ?, 'Birds-house, 1');", 
                              "insert into hutch (id, animal_id, address) values (?, 1, 'Horse-house, 5');",
                              "create view address as select address from hutch where animal_id=2" };
      
          for (int i = 0; i < queries1.length; i++) { 
              PreparedStatement ps = null;
              try { 
                  ps = conn.prepareStatement(queries1[i]); 
                  ps.executeUpdate();
                  fail("SQLException is not thrown for query: " + queries1[i]); 
          } catch(SQLException sqle) { 
              // expected 
          } finally { 
              try { 
                  ps.close(); 
              } catch(Exception ee) {} 
          } 
      } 
      
          String query = "update zoo set name='Masha', family='cat' where id=?;"; 
          PreparedStatement ps = null; 
          try {
              ps = conn.prepareStatement(query); 
              ps.setInt(1, 2); 
              int updateCount = ps.executeUpdate(); 
              assertEquals(1, updateCount); 
              ps.setInt(1, 1); 
              int updateCount1 = ps.executeUpdate(); 
              assertEquals(1, updateCount1); 
          } catch (SQLException e) { 
              fail("SQLException is thrown for query"); 
          } finally {
              try { 
                  ps.close(); 
              } catch(Exception ee) {} 
          } 
      }
  */   
    /**
     * @test java.sql.PreparedStatement#getMetaData()
     *  
     * TODO Doesn't pass on SQLite but according to Java docs:
     * it is possible to invoke the method getMetaData on a 
     * PreparedStatement object rather than waiting to execute it.
     */
    public void testGetMetaData() {
        PreparedStatement ps = null;
        try {
            String query = "update zoo set name='Masha', family='cat' where id=?;";
            ps = conn.prepareStatement(query);
            assertNull(ps.getMetaData());
        } catch (SQLException sqle) {
            fail("SQLException is thrown: " + sqle.toString());
        } catch (Exception e) {
            fail("Exception is thrown: " + e.toString());
        } finally {
            try {
                ps.close();
            } catch (SQLException ee) {
            }
        }

        try {
            String query = "select * from zoo where id = ?";
            ps = conn.prepareStatement(query);
            ResultSetMetaData rsmd = ps.getMetaData();
            assertNotNull(rsmd);
            assertEquals(3, rsmd.getColumnCount());
            assertEquals("id", rsmd.getColumnName(1));
            assertEquals("id", rsmd.getColumnName(1));
        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                ps.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#getParameterMetaData()
     * TODO not supported exception
     */
 /*   public void testGetParameterMetaData() {
        PreparedStatement ps = null;
        try {
            String query = "select * from zoo where id = ?";
            ps = conn.prepareStatement(query);
            ParameterMetaData rsmd = ps.getParameterMetaData();
            assertNotNull(rsmd);
            assertEquals(1, rsmd.getParameterCount());
            ps.setInt(1, 2);
            ps.execute();
            ParameterMetaData rsmd1 = ps.getParameterMetaData();
            assertNotNull(rsmd1);
            assertEquals(1, rsmd1.getParameterCount());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.toString());
        } finally {
            try {
                ps.close();
            } catch (SQLException ee) {
            }
        }

        try {
            String query = "select * from zoo where id = ?";
            ps = conn.prepareStatement(query);
            ParameterMetaData rsmd = ps.getParameterMetaData();
            assertNotNull(rsmd);
            assertEquals(1, rsmd.getParameterCount());
            ps.setInt(1, 2);
            ps.execute();
            ParameterMetaData rsmd1 = ps.getParameterMetaData();
            assertNotNull(rsmd1);
            assertEquals(1, rsmd1.getParameterCount());
        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                ps.close();
            } catch (SQLException ee) {
            }
        }

        try {
            String query = "select * from zoo where id = 1";
            ps = conn.prepareStatement(query);
            ParameterMetaData rsmd = ps.getParameterMetaData();
            assertNotNull(rsmd);
            assertEquals(0, rsmd.getParameterCount());
        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                ps.close();
            } catch (SQLException ee) {
            }
        }
    }
*/
    /**
     * @test java.sql.PreparedStatement#clearParameters()
     */
    /*
     * TODO Crashes VM. Fix later.
     * public void testClearParameters() { 
     *     PreparedStatement ps = null; 
     *     try {
     *         String query = "select * from zoo where id = ? and family=?"; 
     *         ps = conn.prepareStatement(query); 
     *         ps.setInt(1, 2); 
     *         ps.setString(2, "dog");
     *         ps.clearParameters(); 
     *         try { ps.execute(); 
     *         fail("SQLException is not thrown during execute method after calling clearParameters()"); 
     *     } catch(SQLException sqle) {
     *          // expected 
     *     } 
     *     ps.setInt(1, 2);
     *     ps.clearParameters(); 
     *     try { 
     *         ps.execute(); 
     *         fail("SQLException is not thrown during execute method after calling clearParameters()"); 
     *     } catch(SQLException sqle) { 
     *         // expected 
     *     } 
     *     ps.setInt(1, 2); 
     *     ps.setString(2, "cat"); 
     * 
     *     try { 
     *         ps.execute(); 
     *     } catch (SQLException sqle) {
     *         fail("SQLException is thrown during execute method after calling clearParameters() twice"); 
     *     }  
     * } catch (SQLException e) { 
     *         fail("SQLException is thrown"); 
     * } finally {
     *         try { 
     *             ps.close(); 
     *         } catch(SQLException ee) {} 
     *     } 
     * }
     */
    /**
     * @test java.sql.PreparedStatement#setInt(int parameterIndex, int x)
     */
    public void testSetInt() throws SQLException {
        createTables();
        PreparedStatement ps = null;
        Statement st = null;
        try {
            String query = "insert into type (IntVal) values (?);";
            ps = conn.prepareStatement(query);
            try {
                ps.setInt(1, Integer.MAX_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where IntVal="
                        + Integer.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setInt(1, Integer.MIN_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where IntVal="
                        + Integer.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setInt(2, Integer.MIN_VALUE);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setInt(-2, 0);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setLong(int parameterIndex, long x)
     */
    public void testSetLong() {
        createTables();
        PreparedStatement ps = null;
        try {
            String query = "insert into type (LongVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setLong(1, Long.MAX_VALUE);
                ps.execute();
                st = conn.createStatement();
                st
                        .execute("select * from type where LongVal="
                                + Long.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setLong(1, Long.MIN_VALUE);
                ps.execute();
                st = conn.createStatement();
                st
                        .execute("select * from type where LongVal="
                                + Long.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setLong(2, Long.MIN_VALUE);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setLong(-2, 0);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (SQLException ee) {
            }
        }

    }

    /**
     * @test java.sql.PreparedStatement#setFloat(int parameterIndex, float x)
     */
    public void testSetFloat() {
        float value1 = 12345678.12345689f;
        float value2 = -12345678.12345689f;
        createTables();
        PreparedStatement ps = null;
        try {
            String query = "insert into type (FloatVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setFloat(1, value1);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where FloatVal=" + value1);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setFloat(1, value2);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where FloatVal=" + value2);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setFloat(2, Float.MIN_VALUE);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setFloat(-2, 0);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setDouble(int parameterIndex, double x)
     */
    public void testSetDouble() {
        createTables();
        PreparedStatement ps = null;
        try {
            String query = "insert into type (DoubleVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setDouble(1, Double.MAX_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where DoubleVal="
                        + Double.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setDouble(1, Double.MIN_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where DoubleVal="
                        + Double.MIN_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setDouble(2, Double.MIN_VALUE);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setDouble(-2, 0);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (SQLException ee) {
            }
        }

    }

    /**
     * @test java.sql.PreparedStatement#setString(int parameterIndex, String x)
     */
    public void testSetString_charField() {
        createTables();
        PreparedStatement ps = null;
        try {
            String str = "test^text$test%";
            String query = "insert into type (charStr) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setString(1, str);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where charStr='" + str + "'");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setString(1, "");
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where charStr=''");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setString(1, "                   ");
                ps.execute();
                st = conn.createStatement();
                st
                        .execute("select * from type where charStr='                   '");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setString(2, "test text");
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setString(-2, "test text");
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setString(1, " test & text * test % text * test ^ text ");
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown");
            }

            try {
                ps.setString(1, null);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setString(int parameterIndex, String x)
     */
    public void testSetString_tinyTextField() {
        createTables();
        PreparedStatement ps = null;
        try {
            String str = "test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test";
            String query = "insert into type (TText) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setString(1, str);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where TText='" + str + "'");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setString(1, "");
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where TText=''");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setString(1, "                   ");
                ps.execute();
                st = conn.createStatement();
                st
                        .execute("select * from type where TText='                   '");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setString(2, "test text");
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setString(-2, "test text");
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setString(
                                1,
                                "test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test*test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test-test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test+test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test?test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test#test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test ");
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown");
            }

            try {
                ps.setString(1, null);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setString(int parameterIndex, String x)
     */
    public void testSetString_textField() {
        createTables();
        PreparedStatement ps = null;
        try {
            String str = "test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test";
            String query = "insert into type (TextVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setString(1, str);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where TextVal='" + str + "'");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setString(1, "");
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where TextVal=''");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setString(1, "                   ");
                ps.execute();
                st = conn.createStatement();
                st
                        .execute("select * from type where TextVal='                   '");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setString(2, "test text");
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setString(-2, "test text");
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                String longString = " test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/";
                for (int i = 0; i < 10; i++) {
                    longString += longString;
                }
                ps.setString(1, longString);
                ps.execute();
 
            } catch (SQLException sqle) {
                fail("SQLException is thrown");
            }

            try {
                ps.setString(1, null);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setString(int parameterIndex, String x)
     */
    public void testSetString_mediumTextField() {
        createTables();
        PreparedStatement ps = null;
        try {
            String str = "test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test";
            String query = "insert into type (MText) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setString(1, str);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where MText='" + str + "'");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setString(1, "");
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where MText=''");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setString(1, "                   ");
                ps.execute();
                st = conn.createStatement();
                st
                        .execute("select * from type where MText='                   '");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setString(2, "test text");
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setString(-2, "test text");
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setString(1, null);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setString(int parameterIndex, String x)
     */
    public void testSetString_longTextField() {
        createTables();
        PreparedStatement ps = null;
        try {
            String str = "test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test/test^text$test%test(text)test@text5test~test^text$test%test(text)test@text5test";
            String query = "insert into type (LText) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setString(1, str);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where LText='" + str + "'");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setString(1, "");
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where LText=''");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setString(1, "                   ");
                ps.execute();
                st = conn.createStatement();
                st
                        .execute("select * from type where LText='                   '");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setString(2, "test text");
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setString(-2, "test text");
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setString(1, null);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setShort(int parameterIndex, short x)
     */
    public void testSetShort() {
        createTables();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        try {
            String query = "insert into type (Sint) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setShort(1, Short.MAX_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where Sint=" + Short.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setShort(1, Short.MIN_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where Sint=" + Short.MIN_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setShort(2, Short.MAX_VALUE);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setShort(-2, Short.MIN_VALUE);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            String query1 = "insert type(Tint) values (?);";
            ps1 = conn.prepareStatement(query1);
            try {
                ps1.setShort(1, Short.MAX_VALUE);
                ps1.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            String query2 = "insert into type (IntVal) values (?);";
            ps2 = conn.prepareStatement(query2);
            try {
                ps2.setShort(1, Short.MAX_VALUE);
                ps2.execute();
                st = conn.createStatement();
                st
                        .execute("select * from type where IntVal="
                                + Short.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
                ps1.close();
                ps2.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setBoolean(int parameterIndex, boolean
     *       x)
     */
    public void testSetBoolean() {
        createTables();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        try {
            String query = "insert into type (BoolVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setBoolean(1, false);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where BoolVal = 0");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setBoolean(1, true);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where BoolVal= 1");
                ResultSet rs = st.getResultSet();
                assertEquals(2, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setBoolean(2, true);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setBoolean(-2, false);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            String query1 = "insert into type (Tint) values (?);";
            ps1 = conn.prepareStatement(query1);
            try {
                ps1.setBoolean(1, true);
                ps1.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            }

        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
                ps1.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setByte(int parameterIndex, byte x)
     */
    public void testSetByte() {
        createTables();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        try {
            String query = "insert into type (Tint) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setByte(1, Byte.MAX_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where Tint=" + Byte.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setByte(1, Byte.MIN_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where Tint=" + Byte.MIN_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setByte(2, Byte.MAX_VALUE);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setByte(-2, Byte.MIN_VALUE);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            String query1 = "insert into type (IntVal) values (?);";
            ps1 = conn.prepareStatement(query1);
            try {
                ps1.setByte(1, Byte.MAX_VALUE);
                ps1.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            }

        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
                ps1.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setBytes(int parameterIndex, byte[] x)
     */
    public void testSetBytes() {

        byte[] bytesArray = {1, 0};
        createTables();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        try {
            String query = "insert into type (LBlob) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setBytes(1, bytesArray);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            try {
                ps.setBytes(2, bytesArray);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setBytes(-2, bytesArray);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
            String query1 = "insert into type (TBlob) values (?);";
            ps1 = conn.prepareStatement(query1);

            try {
                ps.setBytes(1, bytesArray);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
                ps1.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setBigDecimal(int parameterIndex,
     *       BigDecimal x)
     */
    public void testSetBigDecimal() {

        BigDecimal bd = new BigDecimal("50");
        createTables();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        try {
            String query = "insert into type (DecVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setBigDecimal(1, bd);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where DecVal=" + bd);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            try {
                ps.setBigDecimal(2, bd);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setBigDecimal(-2, bd);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
            String query1 = "insert into type (Tint) values (?);";
            ps1 = conn.prepareStatement(query1);

            try {
                ps1.setBigDecimal(1, bd);
                ps1.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown");
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
                ps1.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setDate(int parameterIndex, Date x)
     */
    public void testSetDate_int_Date() {

        Date[] dates = { new Date(1799, 05, 26), new Date(Integer.MAX_VALUE),
                new Date(123456789) };

        createTables();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        try {
            String query = "insert into type (dateVal) values (?);";
            ps = conn.prepareStatement(query);

            for (int i = 0; i < dates.length; i++) {
                Statement st = null;
                try {
                    ps.setDate(1, dates[i]);
                    ps.execute();
                    st = conn.createStatement();
                    st.execute("select * from type where dateVal='"
                            + dates[i].toString() + "'");
                    ResultSet rs = st.getResultSet();
                    assertEquals(1, getCount(rs));
                } catch (SQLException sqle) {
                    fail("SQLException is thrown: " + sqle.getMessage());
                } finally {
                    try {
                        st.close();
                    } catch (SQLException ee) {
                    }
                }
            }

            try {
                ps.setDate(2, dates[0]);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setDate(-2, dates[0]);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
            String query1 = "insert type(Tint) values (?);";
            ps1 = conn.prepareStatement(query1);

            try {
                ps1.setDate(1, dates[0]);
                ps1.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
                ps1.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setDate(int parameterIndex, Date x,
     *       Calendar cal)
     */
    public void testSetDate_int_Date_Calendar() {

        Calendar[] cals = { Calendar.getInstance(),
                Calendar.getInstance(Locale.GERMANY),
                Calendar.getInstance(TimeZone.getDefault()) };

        Date[] dates = { new Date(1799, 05, 26), new Date(Integer.MAX_VALUE),
                new Date(123456789) };

        createTables();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        try {
            String query = "insert into type (dateVal) values (?);";
            ps = conn.prepareStatement(query);

            for (int i = 0; i < dates.length; i++) {
                Statement st = null;
                try {
                    ps.setDate(1, dates[i], cals[i]);
                    ps.execute();
                    st = conn.createStatement();
                    st.execute("select * from type where dateVal='"
                            + dates[i].toString() + "'");
                    ResultSet rs = st.getResultSet();
                    assertEquals(1, getCount(rs));
                } catch (SQLException sqle) {
                    fail("SQLException is thrown: " + sqle.getMessage());
                } finally {
                    try {
                        st.close();
                    } catch (SQLException ee) {
                    }
                }
            }

            try {
                ps.setDate(2, dates[0], cals[0]);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setDate(-2, dates[0], cals[1]);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
            String query1 = "insert into type (Tint) values (?);";
            ps1 = conn.prepareStatement(query1);

            try {
                ps1.setDate(1, dates[0], cals[2]);
                ps1.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown");
            }
            
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
                ps1.close();
            } catch (SQLException ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setNull(int parameterIndex, int sqlType)
     * 
     * this test doesn't passed on RI
     */
    public void testSetNull_int_int() {
        createTables();
        PreparedStatement ps = null;
        try {
            String query = "insert into type (BoolVal, IntVal) values ('true', ?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setNull(1, Types.INTEGER);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            query = "insert into type (BoolVal, LongVal) values (true, ?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setNull(1, Types.BIGINT);
                ps.execute();
                fail("SQLException is not thrown");
             } catch (SQLException sqle) {
                //expected
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            query = "insert into type (BoolVal, DecVal) values ('true', ?)";
            ps = conn.prepareStatement(query);

            try {
                ps.setNull(1, Types.DECIMAL);
                ps.execute();
             } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            query = "insert into type (BoolVal, dateVal) values (true, ?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setNull(1, Types.DATE);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                //expected
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            query = "insert into type (BoolVal, BlobVal) values (true, ?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setNull(1, Types.BLOB);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                //expected
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            query = "insert into type (BoolVal, TextVal) values (true, ?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setNull(1, Types.CHAR);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                //expected
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setObject(int parameterIndex, Object x)
     * 
     * this test doesn't pass on RI
     */
    public void testSetObject_int_Object() {
        createTables();
        PreparedStatement ps = null;
        try {
            String query = "insert into type (IntVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setObject(1, Integer.MAX_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where IntVal="
                        + Integer.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            query = "insert into type (LongVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, "test text");
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where LongVal='test text';");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

            query = "insert into type (DecVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, new Object());
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is not thrown");
            }

            query = "insert into type (dateVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, new Date(123456789));
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where dateVal='"
                        + new Date(123456789) + "';");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            // this sub test doesn't pass on RI
            query = "insert into type (BlobVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, null);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            }

        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setObject(int parameterIndex, Object x,
     *       int targetSqlType)
     * 
     * this test doesn't pass on RI
     */
    public void testSetObject_int_Object_int() {
        createTables();
        PreparedStatement ps = null;
        try {
            String query = "insert into type(IntVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setObject(1, Integer.MAX_VALUE, Types.INTEGER);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where IntVal="
                        + Integer.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            query = "insert into type (LongVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, "test text", Types.CHAR);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where LongVal='test text';");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            query = "insert into type (DecVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, new Object(), Types.DECIMAL);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.toString());
            }

            query = "insert into type (dateVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, new Date(123456789), Types.DATE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where dateVal='"
                        + new Date(123456789) + "';");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            // this sub test doesn't pass on RI
            query = "insert into type (BlobVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, "", Types.BLOB);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setObject(int parameterIndex, Object x,
     *       int targetSqlType, int scale)
     * 
     * this test doesn't pass on RI
     */
    public void testSetObject_int_Object_int_int() {
        createTables();
        PreparedStatement ps = null;
        try {
            String query = "insert into type (IntVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            try {
                ps.setObject(1, Integer.MAX_VALUE, Types.INTEGER,
                        Integer.MAX_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where IntVal="
                        + Integer.MAX_VALUE);
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            query = "insert into type (LongVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, "test text", Types.CHAR, Integer.MIN_VALUE);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where LongVal='test text';");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            query = "insert into type (DecVal) values (?);";
            ps = conn.prepareStatement(query);
            BigDecimal bd2 = new BigDecimal("12.21");

            try {
                ps.setObject(1, bd2, Types.DECIMAL, 2);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            }

            query = "insert into type (dateVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, new Date(123456789), Types.DATE, -1);
                ps.execute();
                st = conn.createStatement();
                st.execute("select * from type where dateVal='"
                        + new Date(123456789) + "';");
                ResultSet rs = st.getResultSet();
                assertEquals(1, getCount(rs));
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

            // this sub test doesn't pass on RI
            query = "insert into type(BlobVal) values (?);";
            ps = conn.prepareStatement(query);

            try {
                ps.setObject(1, "", Types.BLOB, 0);
                ps.execute();
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }

        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setTime(int parameterIndex, Time x)
     */
    public void testSetTimeint_Time() {

        Time[] times = { new Time(24, 25, 26), new Time(Integer.MAX_VALUE),
                new Time(123456789) };

        createTables();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        try {
            String query = "insert into type (timeVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            for (int i = 0; i < times.length; i++) {
                try {
                    ps.setTime(1, times[i]);
                    ps.execute();
                    st = conn.createStatement();
                    st.execute("select * from type where timeVal='"
                            + times[i].toString() + "'");
                    ResultSet rs = st.getResultSet();
                    assertEquals(1, getCount(rs));
                } catch (SQLException sqle) {
                    fail("SQLException is thrown: " + sqle.getMessage());
                } finally {
                    try {
                        st.close();
                    } catch (Exception ee) {
                    }
                }
            }

            try {
                ps.setTime(2, times[0]);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setTime(-2, times[0]);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
            String query1 = "insert into type (Tint) values (?)";
            ps1 = conn.prepareStatement(query1);

            try {
                ps1.setTime(1, times[0]);
                ps1.execute();

            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.toString());
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
                ps1.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setTime(int parameterIndex, Time x,
     *       Calendar cal)
     */
    public void testSetTime_int_Time_Calendar() {

        Calendar[] cals = { Calendar.getInstance(),
                Calendar.getInstance(Locale.GERMANY),
                Calendar.getInstance(TimeZone.getDefault()) };

        Time[] times = { new Time(24, 25, 26), new Time(Integer.MAX_VALUE),
                new Time(123456789) };

        createTables();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        try {
            String query = "insert into type (timeVal) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            for (int i = 0; i < times.length; i++) {
                try {
                    ps.setTime(1, times[i], cals[i]);
                    ps.execute();
                    st = conn.createStatement();
                    st.execute("select * from type where timeVal='"
                            + times[i].toString() + "'");
                    ResultSet rs = st.getResultSet();
                    assertEquals(1, getCount(rs));
                } catch (SQLException sqle) {
                    fail("SQLException is thrown: " + sqle.getMessage());
                } finally {
                    try {
                        st.close();
                    } catch (Exception ee) {
                    }
                }
            }

            try {
                ps.setTime(2, times[0], cals[0]);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setTime(-2, times[0], cals[1]);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
            String query1 = "insert into type (Tint) values (?);";
            ps1 = conn.prepareStatement(query1);

            try {
                ps1.setTime(1, times[0], cals[2]);
                ps1.execute();
                
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.toString());
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
                ps1.close();
            } catch (Exception ee) {
            }
        }
    }

    /**
     * @test java.sql.PreparedStatement#setTimestamp(int parameterIndex,
     *       Timestamp x)
     */
    public void testSetTimestamp_int_Timestamp() {

        Timestamp[] timestamps = { new Timestamp(2007, 10, 17, 19, 06, 50, 23),
                new Timestamp(123) };

        createTables();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        try {
            String query = "insert into type (TS) values (?);";
            ps = conn.prepareStatement(query);
            Statement st = null;
            for (int i = 0; i < timestamps.length; i++) {
                try {
                    ps.setTimestamp(1, timestamps[i]);
                    ps.execute();
                    st = conn.createStatement();
                    st.execute("select * from type where TS='"
                            + timestamps[i].toString() + "'");
                    ResultSet rs = st.getResultSet();
                    assertEquals(1, getCount(rs));
                } catch (SQLException sqle) {
                    fail("SQLException is thrown: " + sqle.getMessage());
                } finally {
                    try {
                        st.close();
                    } catch (SQLException ee) {
                    }
                }
            }

            try {
                ps.setTimestamp(2, timestamps[0]);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }

            try {
                ps.setTimestamp(-2, timestamps[0]);
                ps.execute();
                fail("SQLException is not thrown");
            } catch (SQLException sqle) {
                // expected
            }
            String query1 = "insert into type (Tint) values (?);";
            ps1 = conn.prepareStatement(query1);

            try {
                ps1.setTimestamp(1, timestamps[0]);
                ps1.execute();
                
            } catch (SQLException sqle) {
                fail("SQLException is thrown: " + sqle.toString());
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                clearTables();
                ps.close();
                ps1.close();
            } catch (Exception ee) {
            }
        }
    }
}
