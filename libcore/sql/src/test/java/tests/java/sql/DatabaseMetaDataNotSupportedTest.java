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

package tests.java.sql;

import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import tests.support.DatabaseCreator;
import tests.support.Support_SQL;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

@TestTargetClass(DatabaseMetaData.class)
public class DatabaseMetaDataNotSupportedTest extends TestCase {
    
    private static String VIEW_NAME = "myView";

    private static String CREATE_VIEW_QUERY = "CREATE VIEW " + VIEW_NAME
            + " AS SELECT * FROM " + DatabaseCreator.TEST_TABLE1;

    private static String DROP_VIEW_QUERY = "DROP VIEW " + VIEW_NAME;

    protected static Connection conn;

    protected static DatabaseMetaData meta;

    protected static Statement statement;

    protected static Statement statementForward;

    private static int id = 1;
    
    public void setUp() throws Exception {
        super.setUp();
        Support_SQL.loadDriver();
        try {
            conn = Support_SQL.getConnection();
            meta = conn.getMetaData();
            statement = conn.createStatement();
            createTestTables();
        } catch (SQLException e) {
            System.out.println("Error in test setup: "+e.getMessage());
        }
    }

    public void tearDown() throws Exception {
        try {
            conn = Support_SQL.getConnection();
            meta = conn.getMetaData();
            statement = conn.createStatement();
            deleteTestTables();
        } catch (SQLException e) {
            System.out.println("Error in teardown: "+e.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        super.tearDown();
    }

    private void createTestTables() {
        try {
            ResultSet userTab = meta.getTables(null, null, null, null);
            while (userTab.next()) {
                String tableName = userTab.getString("TABLE_NAME");
                if (tableName.equals(DatabaseCreator.TEST_TABLE1)) {
                    statement.execute(DatabaseCreator.DROP_TABLE1);
                } else if (tableName
                        .equals(DatabaseCreator.TEST_TABLE3)) {
                    statement.execute(DatabaseCreator.DROP_TABLE3);
                } else if (tableName.equals(VIEW_NAME)) {
                    statement.execute(DROP_VIEW_QUERY);
                }
            }
            userTab.close();
            statement.execute(DatabaseCreator.CREATE_TABLE3);
            statement.execute(DatabaseCreator.CREATE_TABLE1);
            statement.execute(CREATE_VIEW_QUERY);
            meta = conn.getMetaData();
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
    }

    private void deleteTestTables() {
        try {
            statement.execute(DatabaseCreator.DROP_TABLE1);
            statement.execute(DatabaseCreator.DROP_TABLE3);
            statement.execute(DROP_VIEW_QUERY);
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        } finally {
            try {
            if (! conn.isClosed()) {
                conn.close();
            }
            } catch (SQLException e) {
                
            }
        }
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#allProceduresAreCallable()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "Granting not supported.",
        method = "allProceduresAreCallable",
        args = {}
    )
    public void test_allProceduresAreCallable() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.allProceduresAreCallable());
    }
    
    /**
     * @tests {@link java.sql.DatabaseMetaData#allTablesAreSelectable()}
     * 
     * // NOT_FEASIBLE GRANT and REVOKE are not supported
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "test fails. GRANT and REVOKE not supported",
        method = "allTablesAreSelectable",
        args = {}
    )
    @KnownFailure("Not supported ops applied")
    public void test_allTablesAreSelectable() throws SQLException {
        // grant SELECT privileges
        
        String query = "GRANT CREATE, SELECT ON " + DatabaseCreator.TEST_TABLE1
                + " TO " + Support_SQL.sqlUser;
        statement.execute(query);
        Connection userConn = Support_SQL.getConnection(Support_SQL.sqlUrl,
                Support_SQL.sqlUser, Support_SQL.sqlUser);
        DatabaseMetaData userMeta = userConn.getMetaData();
        ResultSet userTab = userMeta.getTables(null, null, null, null);

        assertTrue("Tables are not obtained", userTab.next());
        assertEquals("Incorrect name of obtained table",
                DatabaseCreator.TEST_TABLE1.toLowerCase(), userTab.getString(
                        "TABLE_NAME").toLowerCase());
        assertTrue("Not all of obtained tables are selectable", userMeta
                .allTablesAreSelectable());

        userTab.close();
        // revoke SELECT privileges
        query = "REVOKE SELECT ON " + DatabaseCreator.TEST_TABLE1 + " FROM "
                + Support_SQL.sqlUser;
        statement.execute(query);

        userTab = userMeta.getTables(null, null, null, null);

        assertTrue("Tables are not obtained", userTab.next());
        assertEquals("Incorrect name of obtained table",
                DatabaseCreator.TEST_TABLE1.toLowerCase(), userTab.getString(
                        "TABLE_NAME").toLowerCase());
        assertFalse("No SELECT privileges", userMeta.allTablesAreSelectable());

        userTab.close();
        // revoke CREATE privileges
        query = "REVOKE CREATE ON " + DatabaseCreator.TEST_TABLE1 + " FROM "
                + Support_SQL.sqlUser;
        statement.execute(query);
        userConn.close();
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#dataDefinitionCausesTransactionCommit()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "dataDefinitionCausesTransactionCommit",
        args = {}
    )
    public void test_dataDefinitionCausesTransactionCommit()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#dataDefinitionIgnoredInTransactions()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "dataDefinitionIgnoredInTransactions",
        args = {}
    )
    public void test_dataDefinitionIgnoredInTransactions() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.dataDefinitionIgnoredInTransactions());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#deletesAreDetected(int)
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "deletesAreDetected",
        args = {int.class}
    )
    public void test_deletesAreDetectedI() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.deletesAreDetected(0));
    }

    /**
     * @tests java.sql.DatabaseMetaData#doesMaxRowSizeIncludeBlobs()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "doesMaxRowSizeIncludeBlobs",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_doesMaxRowSizeIncludeBlobs() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.doesMaxRowSizeIncludeBlobs());
    }

    /**
     * @tests java.sql.DatabaseMetaData #getAttributes(java.lang.String,
     *        java.lang.String, java.lang.String, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getAttributes",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_getAttributesLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#getCatalogs()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. not supported. Received result wasn't checked.",
        method = "getCatalogs",
        args = {}
    )
    public void test_getCatalogs() throws SQLException {
        ResultSet rs = meta.getCatalogs();
        // NOT_FEASIBLE getCatalog is not supported
//        while (rs.next()) {
            //if (rs.getString("TABLE_CAT").equalsIgnoreCase(conn.getCatalog())) {
            //    rs.close();
            //    return;
            //}
//        }
        rs.close();
//        fail("Incorrect a set of catalogs");
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#getCatalogSeparator()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getCatalogSeparator",
        args = {}
    )
    public void test_getCatalogSeparator() throws SQLException {
        assertTrue("Incorrect catalog separator", "".equals(meta
                .getCatalogSeparator().trim()));
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#getCatalogTerm()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getCatalogTerm",
        args = {}
    )
    public void test_getCatalogTerm() throws SQLException {
        assertTrue("Incorrect catalog term", "".equals(meta
                .getCatalogSeparator().trim()));
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#getExtraNameCharacters()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getExtraNameCharacters",
        args = {}
    )
    public void test_getExtraNameCharacters() throws SQLException {
        assertNotNull("Incorrect extra name characters", meta
                .getExtraNameCharacters());
       
    }
    
    /**
     * @tests {@link java.sql.DatabaseMetaData #getIndexInfo(java.lang.String,
     *        java.lang.String, java.lang.String, boolean, boolean)}
     *        
     *  NOT_FEASIBLE getCatalog is not supported      
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. not supported. Received result wasn't checked.",
        method = "getIndexInfo",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, boolean.class, boolean.class}
    )
    @KnownFailure("not supported")
    public void test_getIndexInfoLjava_lang_StringLjava_lang_StringLjava_lang_StringZZ()
            throws SQLException {
        boolean unique = false;
        ResultSet rs = meta.getIndexInfo(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1, unique, true);
        ResultSetMetaData rsmd = rs.getMetaData();
        assertTrue("Rows do not obtained", rs.next());
        int col = rsmd.getColumnCount();
        assertEquals("Incorrect number of columns", 13, col);
        String[] columnNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
                "NON_UNIQUE", "INDEX_QUALIFIER", "INDEX_NAME", "TYPE",
                "ORDINAL_POSITION", "COLUMN_NAME", "ASC_OR_DESC",
                "CARDINALITY", "PAGES", "FILTER_CONDITION" };
        for (int c = 1; c <= col; ++c) {
            assertEquals("Incorrect column name", columnNames[c - 1], rsmd
                    .getColumnName(c));
        }

        assertEquals("Incorrect table catalog", conn.getCatalog(), rs
                .getString("TABLE_CAT"));
        assertEquals("Incorrect table schema", null, rs
                .getString("TABLE_SCHEM"));
        assertEquals("Incorrect table name", DatabaseCreator.TEST_TABLE1, rs
                .getString("TABLE_NAME"));
        assertEquals("Incorrect state of uniquess", unique, rs
                .getBoolean("NON_UNIQUE"));
        assertEquals("Incorrect index catalog", "", rs
                .getString("INDEX_QUALIFIER"));
        assertEquals("Incorrect index name", "primary", rs.getString(
                "INDEX_NAME").toLowerCase());
        assertEquals("Incorrect index type", DatabaseMetaData.tableIndexOther,
                rs.getShort("TYPE"));
        assertEquals("Incorrect column sequence number within index", 1, rs
                .getShort("ORDINAL_POSITION"));
        assertEquals("Incorrect column name", "id", rs.getString("COLUMN_NAME"));
        assertEquals("Incorrect column sort sequence", "a", rs.getString(
                "ASC_OR_DESC").toLowerCase());
        assertEquals("Incorrect cardinality", 1, rs.getInt("CARDINALITY"));
        assertEquals("Incorrect value of pages", 0, rs.getInt("PAGES"));
        assertEquals("Incorrect filter condition", null, rs
                .getString("FILTER_CONDITION"));
        rs.close();
    }
    
    /**
     * @tests {@link java.sql.DatabaseMetaData #getColumnPrivileges(java.lang.String,
     *        java.lang.String, java.lang.String, java.lang.String)}
     *        
     *  NOT_FEASIBLE GRANT is not supported      
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. Received result wasn't checked.",
        method = "getColumnPrivileges",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    @KnownFailure("not supported. Privileges are not supported.")
     public void test_getColumnPrivilegesLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        ResultSet rs = meta.getColumnPrivileges(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1, "id");
        ResultSetMetaData rsmd = rs.getMetaData();
        assertFalse("Rows are obtained", rs.next());
        rs.close();

        String query = "GRANT REFERENCES(id) ON " + DatabaseCreator.TEST_TABLE1
                + " TO " + Support_SQL.sqlLogin;
        statement.execute(query);

        rs = meta.getColumnPrivileges(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1, "id");
        rsmd = rs.getMetaData();
        assertTrue("Rows do not obtained", rs.next());
        int col = rsmd.getColumnCount();
        assertEquals("Incorrect number of columns", 8, col);
        String[] columnNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
                "COLUMN_NAME", "GRANTOR", "GRANTEE", "PRIVILEGE",
                "IS_GRANTABLE" };
        for (int c = 1; c <= col; ++c) {
            assertEquals("Incorrect column name", columnNames[c - 1], rsmd
                    .getColumnName(c));
        }
        assertEquals("Incorrect table catalogue", conn.getCatalog(), rs
                .getString("TABLE_CAT").toLowerCase());
        assertEquals("Incorrect table schema", null, rs
                .getString("TABLE_SCHEM"));
        assertEquals("Incorrect table name", DatabaseCreator.TEST_TABLE1, rs
                .getString("TABLE_NAME").toLowerCase());
        assertEquals("Incorrect column name", "id", rs.getString("COLUMN_NAME")
                .toLowerCase());
        assertEquals("Incorrect grantor", Support_SQL.sqlLogin + "@"
                + Support_SQL.sqlHost, rs.getString("GRANTOR").toLowerCase());
        assertTrue("Incorrect grantee",
                rs.getString("GRANTEE").indexOf("root") != -1);
        assertEquals("Incorrect privilege", "references", rs.getString(
                "PRIVILEGE").toLowerCase());

        query = "REVOKE REFERENCES(id) ON " + DatabaseCreator.TEST_TABLE1
                + " FROM " + Support_SQL.sqlLogin;
        statement.execute(query);
        rs.close();
    }

    
    /**
     * @tests {@link java.sql.DatabaseMetaData #getExportedKeys(java.lang.String,
     *        java.lang.String, java.lang.String)}
     *        
     * NOT_FEASIBLE foreign keys are not supported       
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. not supported. Received result wasn't checked.",
        method = "getExportedKeys",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    @KnownFailure("not supported")
     public void test_getExportedKeysLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        ResultSet rs = meta.getExportedKeys(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE3);
        ResultSetMetaData rsmd = rs.getMetaData();
        assertTrue("Rows do not obtained", rs.next());
        int col = rsmd.getColumnCount();
        assertEquals("Incorrect number of columns", 14, col);
        String[] columnNames = { "PKTABLE_CAT", "PKTABLE_SCHEM",
                "PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_CAT",
                "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ",
                "UPDATE_RULE", "DELETE_RULE", "FK_NAME", "PK_NAME",
                "DEFERRABILITY" };
        for (int c = 1; c <= col; ++c) {
            assertEquals("Incorrect column name", columnNames[c - 1], rsmd
                    .getColumnName(c));
        }

        assertEquals("Incorrect primary key table catalog", conn.getCatalog(),
                rs.getString("PKTABLE_CAT"));
        assertEquals("Incorrect primary key table schema", null, rs
                .getString("PKTABLE_SCHEM"));
        assertEquals("Incorrect primary key table name",
                DatabaseCreator.TEST_TABLE3, rs.getString("PKTABLE_NAME"));
        assertEquals("Incorrect primary key column name", "fk", rs
                .getString("PKCOLUMN_NAME"));
        assertEquals("Incorrect foreign key table catalog", conn.getCatalog(),
                rs.getString("FKTABLE_CAT"));
        assertEquals("Incorrect foreign key table schema", null, rs
                .getString("FKTABLE_SCHEM"));
        assertEquals("Incorrect foreign key table name",
                DatabaseCreator.TEST_TABLE1, rs.getString("FKTABLE_NAME"));
        assertEquals("Incorrect foreign key column name", "fkey", rs
                .getString("FKCOLUMN_NAME"));
        assertEquals("Incorrect sequence number within foreign key", 1, rs
                .getShort("KEY_SEQ"));
        assertEquals("Incorrect update rule value",
                DatabaseMetaData.importedKeyNoAction, rs
                        .getShort("UPDATE_RULE"));
        assertEquals("Incorrect delete rule value",
                DatabaseMetaData.importedKeyNoAction, rs
                        .getShort("DELETE_RULE"));
        assertNotNull("Incorrect foreign key name", rs.getString("FK_NAME"));
        assertEquals("Incorrect primary key name", null, rs
                .getString("PK_NAME"));
        assertEquals("Incorrect deferrability",
                DatabaseMetaData.importedKeyNotDeferrable, rs
                        .getShort("DEFERRABILITY"));
        rs.close();
    }
    
    /**
     * @tests java.sql.DatabaseMetaData #getProcedureColumns(java.lang.String,
     *        java.lang.String, java.lang.String, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getProcedureColumns",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_getProcedureColumnsLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        meta.getProcedureColumns("", "", "", "");
    }
    
    /**
     * @tests java.sql.DatabaseMetaData #getProcedures(java.lang.String,
     *        java.lang.String, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getProcedures",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_getProceduresLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#getProcedureTerm()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProcedureTerm",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_getProcedureTerm() throws SQLException {
        assertTrue("Incorrect procedure term", "".equals(meta
                .getProcedureTerm().trim()));
        
      //Exception checking
        conn.close();
         
         try {
             meta.getProcedureTerm();
             fail("SQLException not thrown");
         } catch (SQLException e) {
             //ok
         }
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#getSchemaTerm()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getSchemaTerm",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_getSchemaTerm() throws SQLException {
        String term = meta.getSchemaTerm();
        assertNotNull("Incorrect schema term", term );
        
        assertTrue("".equals(term));
        
      //Exception checking
        conn.close();
         
         try {
             meta.getSchemaTerm();
             fail("SQLException not thrown");
         } catch (SQLException e) {
             //ok
         }
    }
     
    /**
     * @tests java.sql.DatabaseMetaData #getSuperTables(java.lang.String,
     *        java.lang.String, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getSuperTables",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_getSuperTablesLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData #getSuperTypes(java.lang.String,
     *        java.lang.String, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getSuperTypes",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_getSuperTypesLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
    }
    
    /**
     * @tests java.sql.DatabaseMetaData #getTablePrivileges(java.lang.String,
     *        java.lang.String, java.lang.String)
     *        
     *  NOT_FEASIBLE GRANT is not supported      
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. Received result wasn't checked.",
        method = "getTablePrivileges",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    @KnownFailure("not supported. Privileges are not supported.")
    public void test_getTablePrivilegesLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // case 1. Get privileges when no privilegies exist for one table
        ResultSet privileges = meta.getTablePrivileges(conn.getCatalog(), "%",
                DatabaseCreator.TEST_TABLE3);
        assertFalse("Some privilegies exist", privileges.next());
        privileges.close();

        // case 2. Get privileges when no privilegies exist for all tables
        privileges = meta.getTablePrivileges(null, null, null);
        assertFalse("Some privilegies exist", privileges.next());
        privileges.close();

        // case 3. grant CREATE and SELECT privileges ang get them
        HashSet<String> expectedPrivs = new HashSet<String>();
        expectedPrivs.add("CREATE");
        expectedPrivs.add("SELECT");

        String query = "GRANT CREATE, SELECT ON " + DatabaseCreator.TEST_TABLE3
                + " TO " + Support_SQL.sqlUser;
        statement.execute(query);

        privileges = meta.getTablePrivileges(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE3);

        while (privileges.next()) {
            assertEquals("Wrong catalog name", Support_SQL.sqlCatalog,
                    privileges.getString("TABLE_CAT"));
            assertNull("Wrong schema", privileges.getString("TABLE_SCHEM"));
            assertEquals("Wrong table name", DatabaseCreator.TEST_TABLE3,
                    privileges.getString("TABLE_NAME"));
            assertTrue("Wrong privilege " + privileges.getString("PRIVILEGE"),
                    expectedPrivs.remove(privileges.getString("PRIVILEGE")));
            assertEquals("Wrong grantor", Support_SQL.sqlLogin + "@"
                    + Support_SQL.sqlHost, privileges.getString("GRANTOR"));
            assertEquals("Wrong grantee", Support_SQL.sqlUser + "@%",
                    privileges.getString("GRANTEE"));
            assertNull("Wrong value of IS_GRANTABLE", privileges
                    .getString("IS_GRANTABLE"));
        }
        privileges.close();
        assertTrue("Wrong privileges were returned", expectedPrivs.isEmpty());

        query = "REVOKE CREATE, SELECT ON " + DatabaseCreator.TEST_TABLE3
                + " FROM " + Support_SQL.sqlUser;
        statement.execute(query);

        // case 4. grant all privileges ang get them
        String[] privs = new String[] { "ALTER", "CREATE", "CREATE VIEW",
                "DELETE", "DROP", "INDEX", "INSERT", "REFERENCES", "SELECT",
                "SHOW VIEW", "UPDATE" };
        expectedPrivs = new HashSet<String>();
        for (int i = 0; i < privs.length; i++) {
            expectedPrivs.add(privs[i]);
        }
        query = "GRANT ALL ON " + DatabaseCreator.TEST_TABLE3 + " TO "
                + Support_SQL.sqlUser;
        statement.execute(query);

        privileges = meta.getTablePrivileges(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE3);

        while (privileges.next()) {
            assertEquals("Wrong catalog name", Support_SQL.sqlCatalog,
                    privileges.getString("TABLE_CAT"));
            assertNull("Wrong schema", privileges.getString("TABLE_SCHEM"));
            assertEquals("Wrong table name", DatabaseCreator.TEST_TABLE3,
                    privileges.getString("TABLE_NAME"));
            assertTrue("Wrong privilege " + privileges.getString("PRIVILEGE"),
                    expectedPrivs.remove(privileges.getString("PRIVILEGE")));
            assertEquals("Wrong grantor", Support_SQL.sqlLogin + "@"
                    + Support_SQL.sqlHost, privileges.getString("GRANTOR"));
            assertEquals("Wrong grantee", Support_SQL.sqlUser + "@%",
                    privileges.getString("GRANTEE"));
            assertNull("Wrong value of IS_GRANTABLE", privileges
                    .getString("IS_GRANTABLE"));
        }
        privileges.close();
        assertTrue("Wrong privileges were returned", expectedPrivs.isEmpty());

        query = "REVOKE ALL ON " + DatabaseCreator.TEST_TABLE3 + " FROM "
                + Support_SQL.sqlUser;
        statement.execute(query);

        // case 5. check no privelegies after revoke
        privileges = meta.getTablePrivileges(conn.getCatalog(), "%",
                DatabaseCreator.TEST_TABLE3);
        assertFalse("Some privilegies exist", privileges.next());
        privileges.close();

        privileges = meta.getTablePrivileges(null, null, null);
        assertFalse("Some privilegies exist", privileges.next());
        privileges.close();
    }
    
    /**
     * @tests java.sql.DatabaseMetaData #getUDTs(java.lang.String,
     *        java.lang.String, java.lang.String, int[])
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getUDTs",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, int[].class}
    )
    public void test_getUDTsLjava_lang_StringLjava_lang_StringLjava_lang_String$I()
            throws SQLException {
        // NOT_FEASIBLE: JDBC does not implement this functionality
    }
   
    /**
     * @tests java.sql.DatabaseMetaData #getVersionColumns(java.lang.String,
     *        java.lang.String, java.lang.String)
     *  
     *  NOT_FEASIBLE trigger is not supported       
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. Received result wasn't checked.Triggers not supported",
        method = "getVersionColumns",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    @KnownFailure("Not supported ops applied")
    public void test_getVersionColumnsLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        DatabaseMetaDataTest.insertNewRecord();

        String triggerName = "updateTrigger";
        String triggerQuery = "CREATE TRIGGER " + triggerName
                + " AFTER UPDATE ON " + DatabaseCreator.TEST_TABLE1
                + " FOR EACH ROW BEGIN INSERT INTO "
                + DatabaseCreator.TEST_TABLE3 + " SET fk = 10; END;";
        statementForward.execute(triggerQuery);

        String updateQuery = "UPDATE " + DatabaseCreator.TEST_TABLE1
                + " SET field1='fffff' WHERE id=1";
        statementForward.execute(updateQuery);

        ResultSet rs = meta.getVersionColumns(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1);
        assertTrue("Result set is empty", rs.next());
        rs.close();
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#isCatalogAtStart()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isCatalogAtStart",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_isCatalogAtStart() throws SQLException {
        assertFalse(
                "catalog doesn't appear at the start of a fully qualified table name",
                meta.isCatalogAtStart());
        
      //Exception checking
        conn.close();
         
         try {
             meta.isCatalogAtStart();
             fail("SQLException not thrown");
         } catch (SQLException e) {
             //ok
         }
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#locatorsUpdateCopy()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "locatorsUpdateCopy",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_locatorsUpdateCopy() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.locatorsUpdateCopy());
    }

    /**
     * @tests java.sql.DatabaseMetaData#nullPlusNonNullIsNull()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "nullPlusNonNullIsNull",
        args = {}
    )
    public void test_nullPlusNonNullIsNull() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.nullPlusNonNullIsNull());
    }

    /**
     * @tests java.sql.DatabaseMetaData#nullsAreSortedAtEnd()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "nullsAreSortedAtEnd",
        args = {}
    )
    public void test_nullsAreSortedAtEnd() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.nullsAreSortedAtEnd());
    }

    /**
     * @tests java.sql.DatabaseMetaData#nullsAreSortedAtStart()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "nullsAreSortedAtStart",
        args = {}
    )
    public void test_nullsAreSortedAtStart() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.nullsAreSortedAtStart());
    }

    /**
     * @tests java.sql.DatabaseMetaData#nullsAreSortedHigh()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "nullsAreSortedHigh",
        args = {}
    )
    public void test_nullsAreSortedHigh() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.nullsAreSortedHigh());
    }

    /**
     * @tests java.sql.DatabaseMetaData#nullsAreSortedLow()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "nullsAreSortedLow",
        args = {}
    )
    public void test_nullsAreSortedLow() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.nullsAreSortedLow());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#ownDeletesAreVisible(int)
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Not Fully Supported.",
        method = "ownDeletesAreVisible",
        args = {int.class}
    )
    public void test_ownDeletesAreVisibleI() throws SQLException {
        // NOT_FEASIBLE not supported
//        assertFalse(
//                "result set's own deletes are visible for TYPE_FORWARD_ONLY type",
//                meta.ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
//        assertFalse(
//                "result set's own deletes are visible for TYPE_SCROLL_INSENSITIVE type",
//               meta.ownDeletesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
//        assertFalse(
//                "result set's own deletes are visible for TYPE_SCROLL_SENSITIVE type",
//                meta.ownDeletesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("result set's own deletes are visible for unknown type",
                meta.ownDeletesAreVisible(100));
    }

    /**
     * @tests java.sql.DatabaseMetaData#ownInsertsAreVisible(int)
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "not supported.",
        method = "ownInsertsAreVisible",
        args = {int.class}
    )
    public void test_ownInsertsAreVisibleI() throws SQLException {
//        assertFalse(
//                "result set's own inserts are visible for TYPE_FORWARD_ONLY type",
//                meta.ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
//        assertFalse(
//                "result set's own inserts are visible for TYPE_SCROLL_INSENSITIVE type",
//                meta.ownInsertsAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
//        assertFalse(
//                "result set's own inserts are visible for TYPE_SCROLL_SENSITIVE type",
//                meta.ownInsertsAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("result set's own inserts are visible for unknown type",
                meta.ownInsertsAreVisible(100));
    }

    /**
     * @tests {@link java.sql.DatabaseMetaData#ownUpdatesAreVisible(int)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. Verification with invalid parameters missed.",
        method = "ownUpdatesAreVisible",
        args = {int.class}
    )
    public void test_ownUpdatesAreVisibleI() throws SQLException {
        assertFalse(
                "result set's own updates are visible for TYPE_FORWARD_ONLY type",
                meta.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(
                "result set's own updates are visible for TYPE_SCROLL_INSENSITIVE type",
                meta.ownUpdatesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertFalse(
                "result set's own updates are visible for TYPE_SCROLL_SENSITIVE type",
                meta.ownUpdatesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("result set's own updates are visible for unknown type",
                meta.ownUpdatesAreVisible(100));
    }

    /**
     * @tests java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "storesLowerCaseIdentifiers",
        args = {}
    )
    public void test_storesLowerCaseIdentifiers() throws SQLException {
        assertFalse(meta.storesLowerCaseIdentifiers());
    }

    /**
     * @tests java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "storesLowerCaseQuotedIdentifiers",
        args = {}
    )
    public void test_storesLowerCaseQuotedIdentifiers() throws SQLException {
        assertFalse(meta.storesLowerCaseQuotedIdentifiers());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "storesUpperCaseIdentifiers",
        args = {}
    )
    public void test_storesUpperCaseIdentifiers() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.storesUpperCaseIdentifiers());
    }

    /**
     * @tests java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "storesUpperCaseQuotedIdentifiers",
        args = {}
    )
    public void test_storesUpperCaseQuotedIdentifiers() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.storesUpperCaseQuotedIdentifiers());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsANSI92EntryLevelSQL()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsANSI92EntryLevelSQL",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsANSI92EntryLevelSQL() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsANSI92EntryLevelSQL());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsANSI92FullSQL()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsANSI92FullSQL",
        args = {}
    )
    public void test_supportsANSI92FullSQL() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsANSI92FullSQL());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsANSI92IntermediateSQL()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsANSI92IntermediateSQL",
        args = {}
    )
    public void test_supportsANSI92IntermediateSQL() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsANSI92IntermediateSQL());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsAlterTableWithAddColumn()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsAlterTableWithAddColumn",
        args = {}
    )
    public void test_supportsAlterTableWithAddColumn() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsAlterTableWithAddColumn());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsAlterTableWithDropColumn()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsAlterTableWithDropColumn",
        args = {}
    )
    public void test_supportsAlterTableWithDropColumn() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsAlterTableWithDropColumn());

    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsBatchUpdates()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsBatchUpdates",
        args = {}
    )
    public void test_supportsBatchUpdates() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsBatchUpdates());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCatalogsInDataManipulation()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCatalogsInDataManipulation",
        args = {}
    )
    public void test_supportsCatalogsInDataManipulation() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsCatalogsInDataManipulation());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCatalogsInIndexDefinitions()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCatalogsInIndexDefinitions",
        args = {}
    )
    public void test_supportsCatalogsInIndexDefinitions() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsCatalogsInIndexDefinitions());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCatalogsInPrivilegeDefinitions()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCatalogsInPrivilegeDefinitions",
        args = {}
    )
    public void test_supportsCatalogsInPrivilegeDefinitions()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsCatalogsInPrivilegeDefinitions());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCatalogsInProcedureCalls()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCatalogsInProcedureCalls",
        args = {}
    )
    public void test_supportsCatalogsInProcedureCalls() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsCatalogsInProcedureCalls());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCatalogsInTableDefinitions()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCatalogsInTableDefinitions",
        args = {}
    )
    public void test_supportsCatalogsInTableDefinitions() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsCatalogsInTableDefinitions());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsConvert()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsConvert",
        args = {}
    )
    public void test_supportsConvert() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsConvert());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsConvert(int, int)
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsConvert",
        args = {int.class, int.class}
    )
    public void test_supportsConvertII() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsConvert());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCoreSQLGrammar()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCoreSQLGrammar",
        args = {}
    )
    public void test_supportsCoreSQLGrammar() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsCoreSQLGrammar());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCorrelatedSubqueries()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCorrelatedSubqueries",
        args = {}
    )
    public void test_supportsCorrelatedSubqueries() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsCorrelatedSubqueries());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsDataDefinitionAndDataManipulationTransactions()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsDataDefinitionAndDataManipulationTransactions",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsDataDefinitionAndDataManipulationTransactions()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsDataDefinitionAndDataManipulationTransactions());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsDataManipulationTransactionsOnly()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsDataManipulationTransactionsOnly",
        args = {}
    )
    public void test_supportsDataManipulationTransactionsOnly()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsDataManipulationTransactionsOnly());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsDifferentTableCorrelationNames()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsDifferentTableCorrelationNames",
        args = {}
    )
    public void test_supportsDifferentTableCorrelationNames()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsDifferentTableCorrelationNames());
    }


    /**
     * @tests java.sql.DatabaseMetaData#supportsExtendedSQLGrammar()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsExtendedSQLGrammar",
        args = {}
    )
    public void test_supportsExtendedSQLGrammar() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsExtendedSQLGrammar());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsFullOuterJoins()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsFullOuterJoins",
        args = {}
    )
    public void test_supportsFullOuterJoins() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsFullOuterJoins());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsGetGeneratedKeys",
        args = {}
    )
    public void test_supportsGetGeneratedKeys() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsGetGeneratedKeys());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#supportsGroupByBeyondSelect()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsGroupByBeyondSelect",
        args = {}
    )
    public void test_supportsGroupByBeyondSelect() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsGroupByBeyondSelect());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsIntegrityEnhancementFacility()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsIntegrityEnhancementFacility",
        args = {}
    )
    public void test_supportsIntegrityEnhancementFacility() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsIntegrityEnhancementFacility());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsLikeEscapeClause()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsLikeEscapeClause",
        args = {}
    )
    public void test_supportsLikeEscapeClause() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsLikeEscapeClause());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsLimitedOuterJoins()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsLimitedOuterJoins",
        args = {}
    )
    public void test_supportsLimitedOuterJoins() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsLimitedOuterJoins());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMinimumSQLGrammar()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMinimumSQLGrammar",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsMinimumSQLGrammar() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsMinimumSQLGrammar());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMixedCaseIdentifiers()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMixedCaseIdentifiers",
        args = {}
    )
    public void test_supportsMixedCaseIdentifiers() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsMixedCaseIdentifiers());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMixedCaseQuotedIdentifiers()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMixedCaseQuotedIdentifiers",
        args = {}
    )
    public void test_supportsMixedCaseQuotedIdentifiers() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsMixedCaseQuotedIdentifiers());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMultipleOpenResults()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMultipleOpenResults",
        args = {}
    )
    public void test_supportsMultipleOpenResults() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsMultipleOpenResults());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMultipleResultSets()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMultipleResultSets",
        args = {}
    )
    public void test_supportsMultipleResultSets() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsMultipleResultSets());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMultipleTransactions()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMultipleTransactions",
        args = {}
    )
    public void test_supportsMultipleTransactions() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsMultipleTransactions());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsNamedParameters()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsNamedParameters",
        args = {}
    )
    public void test_supportsNamedParameters() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsNamedParameters());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#supportsOpenCursorsAcrossCommit()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsOpenCursorsAcrossCommit",
        args = {}
    )
    public void test_supportsOpenCursorsAcrossCommit() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsOpenCursorsAcrossCommit());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsOpenCursorsAcrossRollback()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsOpenCursorsAcrossRollback",
        args = {}
    )
    public void test_supportsOpenCursorsAcrossRollback() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsOpenCursorsAcrossRollback());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsOpenStatementsAcrossCommit()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsOpenStatementsAcrossCommit",
        args = {}
    )
    public void test_supportsOpenStatementsAcrossCommit() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsOpenStatementsAcrossCommit());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsOpenStatementsAcrossRollback()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsOpenStatementsAcrossRollback",
        args = {}
    )
    public void test_supportsOpenStatementsAcrossRollback() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsOpenStatementsAcrossRollback());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#supportsOuterJoins()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsOuterJoins",
        args = {}
    )
    public void test_supportsOuterJoins() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsOuterJoins());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsPositionedDelete()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsPositionedDelete",
        args = {}
    )
    public void test_supportsPositionedDelete() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsPositionedDelete());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsPositionedUpdate()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsPositionedUpdate",
        args = {}
    )
    public void test_supportsPositionedUpdate() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsPositionedUpdate());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsResultSetConcurrency(int, int)
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsResultSetConcurrency",
        args = {int.class, int.class}
    )
    public void test_supportsResultSetConcurrencyII() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsResultSetConcurrency(0,0));
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsResultSetHoldability(int)
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsResultSetHoldability",
        args = {int.class}
    )
    public void test_supportsResultSetHoldabilityI() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsResultSetHoldability(0));
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#supportsResultSetType(int)
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. Verification with invalid parameters missed.",
        method = "supportsResultSetType",
        args = {int.class}
    )
    @KnownFailure("not supported")
    public void test_supportsResultSetTypeI() throws SQLException {
        // NOT_FEASIBLE not supported
        assertTrue("database supports TYPE_FORWARD_ONLY type", meta
                .supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse("database doesn't support TYPE_SCROLL_INSENSITIVE type",
                meta.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertFalse("database supports TYPE_SCROLL_SENSITIVE type", meta
                .supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("database supports unknown type", meta
                .supportsResultSetType(100));
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#supportsSavepoints()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSavepoints",
        args = {}
    )
    public void test_supportsSavepoints() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsSavepoints());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSchemasInDataManipulation()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSchemasInDataManipulation",
        args = {}
    )
    public void test_supportsSchemasInDataManipulation() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsSchemasInDataManipulation());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSchemasInIndexDefinitions()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSchemasInIndexDefinitions",
        args = {}
    )
    public void test_supportsSchemasInIndexDefinitions() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsSchemasInIndexDefinitions());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSchemasInPrivilegeDefinitions()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSchemasInPrivilegeDefinitions",
        args = {}
    )
    public void test_supportsSchemasInPrivilegeDefinitions()
            throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSchemasInProcedureCalls()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSchemasInProcedureCalls",
        args = {}
    )
    public void test_supportsSchemasInProcedureCalls() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsSchemasInProcedureCalls());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSchemasInTableDefinitions()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSchemasInTableDefinitions",
        args = {}
    )
    public void test_supportsSchemasInTableDefinitions() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsSchemasInTableDefinitions());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#supportsStatementPooling()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsStatementPooling",
        args = {}
    )
    public void test_supportsStatementPooling() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsStatementPooling());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsStoredProcedures()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsStoredProcedures",
        args = {}
    )
    public void test_supportsStoredProcedures() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsStoredProcedures());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSubqueriesInComparisons()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSubqueriesInComparisons",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsSubqueriesInComparisons() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsSubqueriesInComparisons());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#supportsSubqueriesInIns()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSubqueriesInIns",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsSubqueriesInIns() throws SQLException {
        assertFalse(meta.supportsSubqueriesInIns());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSubqueriesInQuantifieds()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSubqueriesInQuantifieds",
        args = {}
    )
    public void test_supportsSubqueriesInQuantifieds() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsSubqueriesInQuantifieds());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#supportsTransactions()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsTransactions",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsTransactions() throws SQLException {
        assertFalse(meta.supportsTransactions());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsUnion()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsUnion",
        args = {}
    )
    public void test_supportsUnion() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsUnion());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsUnionAll()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsUnionAll",
        args = {}
    )
    public void test_supportsUnionAll() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.supportsUnionAll());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#usesLocalFilePerTable()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "usesLocalFilePerTable",
        args = {}
    )
    public void test_usesLocalFilePerTable() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.usesLocalFilePerTable());
    }

    /**
     * @tests java.sql.DatabaseMetaData#usesLocalFiles()
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "usesLocalFiles",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_usesLocalFiles() throws SQLException {
        // NOT_FEASIBLE: SQLITE does not implement this functionality
        assertFalse(meta.usesLocalFiles());
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#getMaxBinaryLiteralLength()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxBinaryLiteralLength",
        args = {}
    )
    public void test_getMaxBinaryLiteralLength() throws SQLException {
        assertTrue("Incorrect binary literal length", meta
                .getMaxBinaryLiteralLength() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxCatalogNameLength()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxCatalogNameLength",
        args = {}
    )
    public void test_getMaxCatalogNameLength() throws SQLException {
        assertTrue("Incorrect name length", meta.getMaxCatalogNameLength() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxCharLiteralLength()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxCharLiteralLength",
        args = {}
    )
    public void test_getMaxCharLiteralLength() throws SQLException {
        assertTrue("Incorrect char literal length", meta
                .getMaxCharLiteralLength() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnNameLength()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnNameLength",
        args = {}
    )
    public void test_getMaxColumnNameLength() throws SQLException {
        assertTrue("Incorrect column name length", meta
                .getMaxColumnNameLength() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnsInGroupBy()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnsInGroupBy",
        args = {}
    )
    public void test_getMaxColumnsInGroupBy() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInGroupBy() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnsInIndex()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnsInIndex",
        args = {}
    )
    public void test_getMaxColumnsInIndex() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInIndex() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnsInOrderBy()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnsInOrderBy",
        args = {}
    )
    public void test_getMaxColumnsInOrderBy() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInOrderBy() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnsInSelect()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnsInSelect",
        args = {}
    )
    public void test_getMaxColumnsInSelect() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInSelect() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnsInTable()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnsInTable",
        args = {}
    )
    public void test_getMaxColumnsInTable() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInTable() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxConnections()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxConnections",
        args = {}
    )
    public void test_getMaxConnections() throws SQLException {
        assertTrue("Incorrect number of connections",
                meta.getMaxConnections() == 0);
    }
    
    /**
     * @tests java.sql.DatabaseMetaData#getMaxIndexLength()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxIndexLength",
        args = {}
    )
    public void test_getMaxIndexLength() throws SQLException {
        assertTrue("Incorrect length of index", meta.getMaxIndexLength() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxProcedureNameLength()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxProcedureNameLength",
        args = {}
    )
    public void test_getMaxProcedureNameLength() throws SQLException {
        assertTrue("Incorrect length of procedure name", meta
                .getMaxProcedureNameLength() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxRowSize()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxRowSize",
        args = {}
    )
    public void test_getMaxRowSize() throws SQLException {
        assertTrue("Incorrect size of row", meta.getMaxRowSize() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxSchemaNameLength()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxSchemaNameLength",
        args = {}
    )
    public void test_getMaxSchemaNameLength() throws SQLException {
        assertTrue("Incorrect length of schema name", meta
                .getMaxSchemaNameLength() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxStatementLength()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxStatementLength",
        args = {}
    )
    public void test_getMaxStatementLength() throws SQLException {
        assertTrue("Incorrect length of statement", meta
                .getMaxStatementLength() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxStatements()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxStatements",
        args = {}
    )
    public void test_getMaxStatements() throws SQLException {
        assertTrue("Incorrect number of statements",
                meta.getMaxStatements() == 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxTableNameLength()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxTableNameLength",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_getMaxTableNameLength() throws SQLException {
        assertTrue("Now supported", meta
                .getMaxTableNameLength() == 0);
        
      //Exception checking
        conn.close();
         
         try {
             meta.getMaxTableNameLength();
             fail("SQLException not thrown");
         } catch (SQLException e) {
             //ok
         }
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxTablesInSelect()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxTablesInSelect",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_getMaxTablesInSelect() throws SQLException {
        assertTrue("Tables in select is now supported: change test implementation\"",
                meta.getMaxTablesInSelect() == 0);
        
      //Exception checking
        conn.close();
         
         try {
             meta.getMaxTablesInSelect();
             fail("SQLException not thrown");
         } catch (SQLException e) {
             //ok
         }
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxUserNameLength()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "usernames not supported",
        method = "getMaxUserNameLength",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_getMaxUserNameLength() throws SQLException {
        assertTrue("Usernames are now supported: change test implementation",
                meta.getMaxUserNameLength() == 0);
        
      //Excpetion checking
        conn.close();
         
         try {
             meta.getMaxUserNameLength();
             fail("SQLException not thrown");
         } catch (SQLException e) {
             //ok
         }
    }
    

}
