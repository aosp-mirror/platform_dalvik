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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class ResultSetMetaDataTest extends SQLTest {

    ResultSetMetaData rsmd = null;
    Statement st = null;
    ResultSet rs = null;

    public void setUp() {
        super.setUp();
        try {
            String query = "select * from zoo";
            st = conn.createStatement();
            st.execute(query);
            rs = st.getResultSet();
            rsmd = rs.getMetaData();
        } catch (SQLException e) {
            fail("Couldn't get ResultSetMetaData object");
        }
    }
    
    public void tearDown() {
        super.tearDown();
        try {
            rs.close();
            st.close();
        } catch (SQLException e) {
            fail("Couldn't close Statement object");
        }
    }

    /**
     * @test java.sql.ResultSetMetaData#getCatalogName(int column)
     */
    public void testGetCatalogName() throws SQLException {
        try {
            assertNull(rsmd.getCatalogName(0));
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }
    }

    /**
     * @test java.sql.ResultSetMetaData#getColumnClassName(int column)
     */
    public void testGetColumnClassName() {
        try {
            assertNotNull(rsmd);
            assertEquals(Short.class.getName(), rsmd.getColumnClassName(1));
            assertEquals(String.class.getName(), rsmd.getColumnClassName(2));
            assertEquals(String.class.getName(), rsmd.getColumnClassName(3));
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }

        try {
            String name  = rsmd.getColumnClassName(-1);
            assertNull(name);
        } catch (SQLException e) {
            fail("SQLException is thrown");
        }
        
        try {
            String name  = rsmd.getColumnClassName(4);
            assertNull(name);
        } catch (SQLException e) {
            fail("SQLException is thrown");
        }
    }

    /**
     * @test java.sql.ResultSetMetaData#getColumnCount()
     */
    public void testGetColumnCount() {
        try {
            assertEquals(3, rsmd.getColumnCount());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }
    }

    /**
     * @test java.sql.ResultSetMetaData#getColumnLabel(int column)
     */
    public void testGetColumnLabel() {
        String[] labels = { "zoo.id", "zoo.name", "zoo.family" };
        try {
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                String label = rsmd.getColumnLabel(i + 1);
                assertEquals(labels[i], label);
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }
    }

    /**
     * @test java.sql.ResultSetMetaData#getColumnName(int column)
     */
    public void testGetColumnName() {
        String[] labels = { "zoo.id", "zoo.name", "zoo.family" };
        try {
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                String label = rsmd.getColumnLabel(i + 1);
                assertEquals(labels[i], label);
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }

        try {
            String label = rsmd.getColumnLabel(-1);
            assertNull(label);
        } catch (SQLException e) {
            fail("SQLException is thrown");
        }
        
        try {
            String label = rsmd.getColumnLabel(5);
            assertNull(label);
        } catch (SQLException e) {
            fail("SQLException is thrown");
        }
    }

    /**
     * @test java.sql.ResultSetMetaData#getColumnType(int column)
     */
    public void testGetColumnType() {
        int[] types = { Types.SMALLINT, Types.VARCHAR, Types.VARCHAR};
        try {
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                int type = rsmd.getColumnType(i + 1);
                assertEquals(types[i], type);
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }

        try {
            rsmd.getColumnType(-1);
            fail("SQLException is not thrown");
        } catch (SQLException e) {
            // expected
        }
        try {
            rsmd.getColumnType(5);
            fail("SQLException is not thrown");
        } catch (SQLException e) {
            // expected
        }
    }

    /**
     * @test java.sql.ResultSetMetaData#getColumnTypeName(int column)
     */
    public void testGetColumnTypeName() {
        try {
            assertEquals("smallint", rsmd.getColumnTypeName(1));
            assertEquals("varchar", rsmd.getColumnTypeName(2));
            assertEquals("varchar", rsmd.getColumnTypeName(3));
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }

        try {
            rsmd.getColumnTypeName(-1);
            fail("SQLException is not thrown");
        } catch (SQLException e) {
            // expected
        }
        try {
            rsmd.getColumnTypeName(5);
            fail("SQLException is not thrown");
        } catch (SQLException e) {
            // expected
        }
    }

    /**
     * @test java.sql.ResultSetMetaData#getTableName(int column)
     */
    public void testGetTableName() {
        try {
            assertEquals("zoo", rsmd.getTableName(1));
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }
        Statement st1 = null;
        ResultSet rs1 = null;
        try {
            String[] queries = {
                    "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                    "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                    "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                    "select name, animal_id from hutch, zoo where zoo.id = 1" };
            st1 = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st1.execute(queries[i]);
            }
            rs1 = st1.getResultSet();
            ResultSetMetaData rsmd1 = rs1.getMetaData();
            assertEquals("zoo", rsmd1.getTableName(1));
            assertEquals("hutch", rsmd1.getTableName(2));
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                rs1.close();
                st1.execute("drop table ifexists hutch");
                st1.close();
                
            } catch (SQLException sqle) {
            }
        }

        try {
            String name = rsmd.getTableName(-1);
            assertNull(name);
        } catch (SQLException e) {
            fail("SQLException is thrown");
        }
        try {
            String name = rsmd.getTableName(5);
            assertNull(name);
        } catch (SQLException e) {
            fail("SQLException is thrown");
        }
    }
}
