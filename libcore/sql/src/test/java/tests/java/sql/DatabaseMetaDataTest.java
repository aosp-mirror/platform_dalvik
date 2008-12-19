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

package tests.java.sql;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;

import tests.support.DatabaseCreator;
import tests.support.Support_SQL;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

@TestTargetClass(DatabaseMetaData.class)
public class DatabaseMetaDataTest extends TestCase {
    private static String VIEW_NAME = "myView";

    private static String CREATE_VIEW_QUERY = "CREATE VIEW " + VIEW_NAME
            + " AS SELECT * FROM " + DatabaseCreator.TEST_TABLE1;

    private static String DROP_VIEW_QUERY = "DROP VIEW " + VIEW_NAME;

    private static Connection conn;

    private static DatabaseMetaData meta;

    private static Statement statement;

    private static Statement statementForward;

    private static int id = 1;

    public static Test suite() {
        TestSetup setup = new TestSetup(new TestSuite(
                DatabaseMetaDataTest.class)) {
            protected void setUp() throws Exception {
                Support_SQL.loadDriver();
                try {
                    conn = Support_SQL.getConnection();
                    statement = conn.createStatement();
                    statementForward = conn.createStatement(
                            ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_UPDATABLE);
                    meta = conn.getMetaData();
                    createTestTables();
                } catch (SQLException e) {
                    fail("Unexpected SQLException " + e.toString());
                }
            }

            protected void tearDown() throws Exception {
                deleteTestTables();
                statement.close();
                statementForward.close();
                conn.close();
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
                }
            }
        };
        return setup;
    }

    /**
     * @tests java.sql.DatabaseMetaData#allProceduresAreCallable()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "allProceduresAreCallable",
          methodArgs = {}
        )
    })
    public void test_allProceduresAreCallable() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#allTablesAreSelectable()
     * 
     * // TODO GRANT and REVOKE are not supported
     */
/*    public void test_allTablesAreSelectable() throws SQLException {
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
*/
    /**
     * @tests java.sql.DatabaseMetaData#dataDefinitionCausesTransactionCommit()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "dataDefinitionCausesTransactionCommit",
          methodArgs = {}
        )
    })
    public void test_dataDefinitionCausesTransactionCommit()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#dataDefinitionIgnoredInTransactions()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "dataDefinitionIgnoredInTransactions",
          methodArgs = {}
        )
    })
    public void test_dataDefinitionIgnoredInTransactions() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#deletesAreDetected(int)
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "deletesAreDetected",
          methodArgs = {int.class}
        )
    })
    public void test_deletesAreDetectedI() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#doesMaxRowSizeIncludeBlobs()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "doesMaxRowSizeIncludeBlobs",
          methodArgs = {}
        )
    })
    public void test_doesMaxRowSizeIncludeBlobs() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData #getAttributes(java.lang.String,
     *        java.lang.String, java.lang.String, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "getAttributes",
          methodArgs = {String.class, String.class, String.class, String.class}
        )
    })
    public void test_getAttributesLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData #getBestRowIdentifier(java.lang.String,
     *        java.lang.String, java.lang.String, int, boolean)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "All variants of parameters were not tested. " +
                  "SQLException checking missed.",
      targets = {
        @TestTarget(
          methodName = "getBestRowIdentifier",
          methodArgs = {String.class, String.class, String.class, int.class, boolean.class}
        )
    })
    public void test_getBestRowIdentifierLjava_lang_StringLjava_lang_StringLjava_lang_StringIZ()
            throws SQLException {
        ResultSet result = statementForward.executeQuery("SELECT * FROM "
                + DatabaseCreator.TEST_TABLE1);
        
        // TODO not supported
//        try {
//            result.moveToInsertRow();
//            result.updateInt("id", 1234567);
//            result.updateString("field1", "test1");
//            result.insertRow();
//        } catch (SQLException e) {
//            fail("Unexpected SQLException " + e.toString());
//        }

        result.close();

        ResultSet rs = meta.getBestRowIdentifier(null, null,
                DatabaseCreator.TEST_TABLE1, DatabaseMetaData.bestRowSession,
                true);
        ResultSetMetaData rsmd = rs.getMetaData();
        assertTrue("Rows do not obtained", rs.next());
        int col = rsmd.getColumnCount();
        assertEquals("Incorrect number of columns", 8, col);
        String[] columnNames = { "SCOPE", "COLUMN_NAME", "DATA_TYPE",
                "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS",
                "PSEUDO_COLUMN" };
        for (int c = 1; c <= col; ++c) {
            assertEquals("Incorrect column name", columnNames[c - 1], rsmd
                    .getColumnName(c));
        }
        assertEquals("Incorrect scope", DatabaseMetaData.bestRowSession, rs
                .getShort("SCOPE"));
//        assertEquals("Incorrect column name", "id", rs.getString("COLUMN_NAME"));
        assertEquals("Incorrect data type", java.sql.Types.INTEGER, rs
                .getInt("DATA_TYPE"));
        assertEquals("Incorrect type name", "INTEGER", rs.getString("TYPE_NAME"));
        rs.close();
    }

    /**
     * @tests java.sql.DatabaseMetaData#getCatalogSeparator()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getCatalogSeparator",
          methodArgs = {}
        )
    })
    public void test_getCatalogSeparator() throws SQLException {
        assertTrue("Incorrect catalog separator", "".equals(meta
                .getCatalogSeparator().trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData#getCatalogTerm()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getCatalogTerm",
          methodArgs = {}
        )
    })
    public void test_getCatalogTerm() throws SQLException {
        assertTrue("Incorrect catalog term", "".equals(meta
                .getCatalogSeparator().trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData#getCatalogs()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                  "Received result wasn't checked.",
      targets = {
        @TestTarget(
          methodName = "getCatalogs",
          methodArgs = {}
        )
    })
    public void test_getCatalogs() throws SQLException {
        ResultSet rs = meta.getCatalogs();
        // TODO getCatalog is not supported
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
     * @tests java.sql.DatabaseMetaData #getColumnPrivileges(java.lang.String,
     *        java.lang.String, java.lang.String, java.lang.String)
     *        
     *  TODO GRANT is not supported      
     */
/*    public void test_getColumnPrivilegesLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
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
*/
    /**
     * @tests java.sql.DatabaseMetaData#getConnection()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getConnection",
          methodArgs = {}
        )
    })
    public void test_getConnection() throws SQLException {
        assertEquals("Incorrect connection value", conn, meta.getConnection());
    }

    /**
     * @tests java.sql.DatabaseMetaData #getCrossReference(java.lang.String,
     *        java.lang.String, java.lang.String, java.lang.String,
     *        java.lang.String, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                  "All variants of parameters were not tested.",
      targets = {
        @TestTarget(
          methodName = "getCrossReference",
          methodArgs = {String.class, String.class, String.class, String.class, String.class, String.class}
        )
    })
    public void test_getCrossReferenceLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        ResultSet rs = meta.getCrossReference(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE3, conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1);
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
//      TODO getCatalog is not supported 
//        assertEquals("Incorrect primary key table catalog", conn.getCatalog(),
//                rs.getString("PKTABLE_CAT"));
        assertEquals("Incorrect primary key table schema", "", rs
                .getString("PKTABLE_SCHEM"));
        assertEquals("Incorrect primary key table name",
                DatabaseCreator.TEST_TABLE3, rs.getString("PKTABLE_NAME"));
        assertEquals("Incorrect primary key column name", "fkey", rs
                .getString("PKCOLUMN_NAME"));
        // TODO getCatalog is not supported
//        assertEquals("Incorrect foreign key table catalog", conn.getCatalog(),
//                rs.getString("FKTABLE_CAT"));
        assertEquals("Incorrect foreign key table schema", "", rs
                .getString("FKTABLE_SCHEM"));
        assertEquals("Incorrect foreign key table name",
                DatabaseCreator.TEST_TABLE1, rs.getString("FKTABLE_NAME"));
        assertEquals("Incorrect foreign key column name", "fk", rs
                .getString("FKCOLUMN_NAME"));
        assertEquals("Incorrect sequence number within foreign key", 1, rs
                .getShort("KEY_SEQ"));
        assertEquals("Incorrect update rule value",
                DatabaseMetaData.importedKeyNoAction, rs
                        .getShort("UPDATE_RULE"));
        assertEquals("Incorrect delete rule value",
                DatabaseMetaData.importedKeyNoAction, rs
                        .getShort("DELETE_RULE"));
        assertNull("Incorrect foreign key name", rs.getString("FK_NAME"));
        assertNull("Incorrect primary key name", rs.getString("PK_NAME"));
        assertEquals("Incorrect deferrability",
                DatabaseMetaData.importedKeyNotDeferrable, rs
                        .getShort("DEFERRABILITY"));
        rs.close();
    }

    /**
     * @tests java.sql.DatabaseMetaData#getDatabaseMajorVersion()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getDatabaseMajorVersion",
          methodArgs = {}
        )
    })
    public void test_getDatabaseMajorVersion() throws SQLException {
        assertTrue("Incorrdct database major version", meta
                .getDatabaseMajorVersion() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getDatabaseMinorVersion()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getDatabaseMinorVersion",
          methodArgs = {}
        )
    })
    public void test_getDatabaseMinorVersion() throws SQLException {
        assertTrue("Incorrect database minor version", meta
                .getDatabaseMinorVersion() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getDatabaseProductName()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getDatabaseProductName",
          methodArgs = {}
        )
    })
    public void test_getDatabaseProductName() throws SQLException {
        assertTrue("Incorrect database product name", !"".equals(meta
                .getDatabaseProductName().trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData#getDatabaseProductVersion()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getDatabaseProductVersion",
          methodArgs = {}
        )
    })
    public void test_getDatabaseProductVersion() throws SQLException {
        assertTrue("Incorrect database product version", !"".equals(meta
                .getDatabaseProductVersion().trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData#getDefaultTransactionIsolation()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getDefaultTransactionIsolation",
          methodArgs = {}
        )
    })
    public void test_getDefaultTransactionIsolation() throws SQLException {
        int defaultLevel = meta.getDefaultTransactionIsolation();
        switch (defaultLevel) {
        case Connection.TRANSACTION_NONE:
        case Connection.TRANSACTION_READ_COMMITTED:
        case Connection.TRANSACTION_READ_UNCOMMITTED:
        case Connection.TRANSACTION_REPEATABLE_READ:
        case Connection.TRANSACTION_SERIALIZABLE:
            // these levels are OK
            break;
        default:
            fail("Incorrect value of default transaction isolation level");
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#getDriverMajorVersion()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getDriverMajorVersion",
          methodArgs = {}
        )
    })
    public void test_getDriverMajorVersion() {
        assertTrue("Incorrect driver major version", meta
                .getDriverMajorVersion() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getDriverMinorVersion()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getDriverMinorVersion",
          methodArgs = {}
        )
    })
    public void test_getDriverMinorVersion() {
        assertTrue("Incorrect driver minor version", meta
                .getDriverMinorVersion() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getDriverName()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getDriverName",
          methodArgs = {}
        )
    })
    public void test_getDriverName() throws SQLException {
        assertTrue("Incorrect driver name", !"".equals(meta.getDriverName()
                .trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData#getDriverVersion()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getDriverVersion",
          methodArgs = {}
        )
    })
    public void test_getDriverVersion() throws SQLException {
        assertTrue("Incorrect driver version", !"".equals(meta
                .getDriverVersion().trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData #getExportedKeys(java.lang.String,
     *        java.lang.String, java.lang.String)
     *        
     * TODO getCatalog is not supported       
     */
/*    public void test_getExportedKeysLjava_lang_StringLjava_lang_StringLjava_lang_String()
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
*/
    /**
     * @tests java.sql.DatabaseMetaData#getExtraNameCharacters()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getExtraNameCharacters",
          methodArgs = {}
        )
    })
    public void test_getExtraNameCharacters() throws SQLException {
        assertNotNull("Incorrect extra name characters", meta
                .getExtraNameCharacters());
    }

    /**
     * @tests java.sql.DatabaseMetaData#getIdentifierQuoteString()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getIdentifierQuoteString",
          methodArgs = {}
        )
    })
    public void test_getIdentifierQuoteString() throws SQLException {
        assertTrue("Incorrect identifier of quoted string", !"".equals(meta
                .getIdentifierQuoteString().trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData #getImportedKeys(java.lang.String,
     *        java.lang.String, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getImportedKeys",
          methodArgs = {String.class, String.class, String.class}
        )
    })
    public void test_getImportedKeysLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        ResultSet rs = meta.getImportedKeys(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1);
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
//      TODO getCatalog is not supported
//        assertEquals("Incorrect primary key table catalog", conn.getCatalog(),
//                rs.getString("PKTABLE_CAT"));
        assertEquals("Incorrect primary key table schema", "", rs
                .getString("PKTABLE_SCHEM"));
        assertEquals("Incorrect primary key table name",
                DatabaseCreator.TEST_TABLE3, rs.getString("PKTABLE_NAME"));
        assertEquals("Incorrect primary key column name", "fkey", rs
                .getString("PKCOLUMN_NAME"));
//        assertEquals("Incorrect foreign key table catalog", conn.getCatalog(),
//                rs.getString("FKTABLE_CAT"));
        assertEquals("Incorrect foreign key table schema", "", rs
                .getString("FKTABLE_SCHEM"));
        assertEquals("Incorrect foreign key table name",
                DatabaseCreator.TEST_TABLE1, rs.getString("FKTABLE_NAME"));
        assertEquals("Incorrect foreign key column name", "fk", rs
                .getString("FKCOLUMN_NAME"));
        assertEquals("Incorrect sequence number within foreign key", 1, rs
                .getShort("KEY_SEQ"));
        assertEquals("Incorrect update rule value",
                DatabaseMetaData.importedKeyNoAction, rs
                        .getShort("UPDATE_RULE"));
        assertEquals("Incorrect delete rule value",
                DatabaseMetaData.importedKeyNoAction, rs
                        .getShort("DELETE_RULE"));
 //       assertNotNull("Incorrect foreign key name", rs.getString("FK_NAME"));
        assertEquals("Incorrect primary key name", null, rs
                .getString("PK_NAME"));
        assertEquals("Incorrect deferrability",
                DatabaseMetaData.importedKeyNotDeferrable, rs
                        .getShort("DEFERRABILITY"));
        rs.close();
    }

    /**
     * @tests java.sql.DatabaseMetaData #getIndexInfo(java.lang.String,
     *        java.lang.String, java.lang.String, boolean, boolean)
     *        
     *  TODO getCatalog is not supported      
     */
/*    public void test_getIndexInfoLjava_lang_StringLjava_lang_StringLjava_lang_StringZZ()
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
*/
    /**
     * @tests java.sql.DatabaseMetaData#getJDBCMajorVersion()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getJDBCMajorVersion",
          methodArgs = {}
        )
    })
    public void test_getJDBCMajorVersion() throws SQLException {
        assertTrue("Incorrect JDBC major version",
                meta.getJDBCMajorVersion() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getJDBCMinorVersion()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getJDBCMinorVersion",
          methodArgs = {}
        )
    })
    public void test_getJDBCMinorVersion() throws SQLException {
        assertTrue("Incorrect JDBC minor version",
                meta.getJDBCMinorVersion() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxBinaryLiteralLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxBinaryLiteralLength",
          methodArgs = {}
        )
    })
    public void test_getMaxBinaryLiteralLength() throws SQLException {
        assertTrue("Incorrect binary literal length", meta
                .getMaxBinaryLiteralLength() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxCatalogNameLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxCatalogNameLength",
          methodArgs = {}
        )
    })
    public void test_getMaxCatalogNameLength() throws SQLException {
        assertTrue("Incorrect name length", meta.getMaxCatalogNameLength() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxCharLiteralLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxCharLiteralLength",
          methodArgs = {}
        )
    })
    public void test_getMaxCharLiteralLength() throws SQLException {
        assertTrue("Incorrect char literal length", meta
                .getMaxCharLiteralLength() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnNameLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxColumnNameLength",
          methodArgs = {}
        )
    })
    public void test_getMaxColumnNameLength() throws SQLException {
        assertTrue("Incorrect column name length", meta
                .getMaxColumnNameLength() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnsInGroupBy()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxColumnsInGroupBy",
          methodArgs = {}
        )
    })
    public void test_getMaxColumnsInGroupBy() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInGroupBy() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnsInIndex()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxColumnsInIndex",
          methodArgs = {}
        )
    })
    public void test_getMaxColumnsInIndex() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInIndex() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnsInOrderBy()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxColumnsInOrderBy",
          methodArgs = {}
        )
    })
    public void test_getMaxColumnsInOrderBy() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInOrderBy() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnsInSelect()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxColumnsInSelect",
          methodArgs = {}
        )
    })
    public void test_getMaxColumnsInSelect() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInSelect() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxColumnsInTable()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxColumnsInTable",
          methodArgs = {}
        )
    })
    public void test_getMaxColumnsInTable() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInTable() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxConnections()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxConnections",
          methodArgs = {}
        )
    })
    public void test_getMaxConnections() throws SQLException {
        assertTrue("Incorrect number of connections",
                meta.getMaxConnections() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxCursorNameLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxCursorNameLength",
          methodArgs = {}
        )
    })
    public void test_getMaxCursorNameLength() throws SQLException {
        int nameLength = meta.getMaxCursorNameLength();
        if (nameLength > 0) {
            try {
                statement.setCursorName(new String(new byte[nameLength + 1]));
                fail("Expected SQLException was not thrown");
            } catch (SQLException e) {
                // expected
            }
        } else if (nameLength < 0) {
            fail("Incorrect length of cursor name");
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxIndexLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxIndexLength",
          methodArgs = {}
        )
    })
    public void test_getMaxIndexLength() throws SQLException {
        assertTrue("Incorrect length of index", meta.getMaxIndexLength() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxProcedureNameLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxProcedureNameLength",
          methodArgs = {}
        )
    })
    public void test_getMaxProcedureNameLength() throws SQLException {
        assertTrue("Incorrect length of procedure name", meta
                .getMaxProcedureNameLength() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxRowSize()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxRowSize",
          methodArgs = {}
        )
    })
    public void test_getMaxRowSize() throws SQLException {
        assertTrue("Incorrect size of row", meta.getMaxRowSize() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxSchemaNameLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxSchemaNameLength",
          methodArgs = {}
        )
    })
    public void test_getMaxSchemaNameLength() throws SQLException {
        assertTrue("Incorrect length of schema name", meta
                .getMaxSchemaNameLength() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxStatementLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxStatementLength",
          methodArgs = {}
        )
    })
    public void test_getMaxStatementLength() throws SQLException {
        assertTrue("Incorrect length of statement", meta
                .getMaxStatementLength() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxStatements()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxStatements",
          methodArgs = {}
        )
    })
    public void test_getMaxStatements() throws SQLException {
        assertTrue("Incorrect number of statements",
                meta.getMaxStatements() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxTableNameLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxTableNameLength",
          methodArgs = {}
        )
    })
    public void test_getMaxTableNameLength() throws SQLException {
        assertTrue("Incorrect length of table name", meta
                .getMaxTableNameLength() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxTablesInSelect()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxTablesInSelect",
          methodArgs = {}
        )
    })
    public void test_getMaxTablesInSelect() throws SQLException {
        assertTrue("Incorrect number of tables",
                meta.getMaxTablesInSelect() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getMaxUserNameLength()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getMaxUserNameLength",
          methodArgs = {}
        )
    })
    public void test_getMaxUserNameLength() throws SQLException {
        assertTrue("Incorrect length of user name",
                meta.getMaxUserNameLength() >= 0);
    }

    /**
     * @tests java.sql.DatabaseMetaData#getNumericFunctions()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getNumericFunctions",
          methodArgs = {}
        )
    })
    public void test_getNumericFunctions() throws SQLException {
        assertTrue("Incorrect list of math functions", "".equals(meta
                .getNumericFunctions().trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData #getPrimaryKeys(java.lang.String,
     *        java.lang.String, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getPrimaryKeys",
          methodArgs = {String.class, String.class, String.class}
        )
    })
    public void test_getPrimaryKeysLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        ResultSet rs = meta.getPrimaryKeys(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1);
        ResultSetMetaData rsmd = rs.getMetaData();
        assertTrue("Rows do not obtained", rs.next());
        int col = rsmd.getColumnCount();
        assertEquals("Incorrect number of columns", 6, col);
        String[] columnNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
                "COLUMN_NAME", "KEY_SEQ", "PK_NAME" };
        for (int c = 1; c <= col; ++c) {
            assertEquals("Incorrect column name", columnNames[c - 1], rsmd
                    .getColumnName(c));
        }
//        assertEquals("Incorrect table catalogue", conn.getCatalog(), rs
//                .getString("TABLE_CAT").toLowerCase());
        assertEquals("Incorrect table schema", "", rs
                .getString("TABLE_SCHEM"));
        assertEquals("Incorrect table name", DatabaseCreator.TEST_TABLE1, rs
                .getString("TABLE_NAME").toLowerCase());
        assertEquals("Incorrect column name", "id", rs.getString("COLUMN_NAME")
                .toLowerCase());
        assertEquals("Incorrect sequence number", 1, rs.getShort("KEY_SEQ"));
 //       assertEquals("Incorrect primary key name", "primary", rs.getString(
 //               "PK_NAME").toLowerCase());
        rs.close();
    }

    /**
     * @tests java.sql.DatabaseMetaData #getProcedureColumns(java.lang.String,
     *        java.lang.String, java.lang.String, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "getProcedureColumns",
          methodArgs = {String.class, String.class, String.class, String.class}
        )
    })
    public void test_getProcedureColumnsLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#getProcedureTerm()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getProcedureTerm",
          methodArgs = {}
        )
    })
    public void test_getProcedureTerm() throws SQLException {
        assertTrue("Incorrect procedure term", "".equals(meta
                .getProcedureTerm().trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData #getProcedures(java.lang.String,
     *        java.lang.String, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "getProcedures",
          methodArgs = {String.class, String.class, String.class}
        )
    })
    public void test_getProceduresLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#getResultSetHoldability()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getResultSetHoldability",
          methodArgs = {}
        )
    })
    public void test_getResultSetHoldability() throws SQLException {
        int hdb = meta.getResultSetHoldability();
        switch (hdb) {
        case ResultSet.HOLD_CURSORS_OVER_COMMIT:
        case ResultSet.CLOSE_CURSORS_AT_COMMIT:
            // these holdabilities are OK
            break;
        default:
            fail("Incorrect value of holdability");
        }
        assertFalse("Incorrect result set holdability", meta
                .supportsResultSetHoldability(hdb));
    }

    /**
     * @tests java.sql.DatabaseMetaData#getSQLKeywords()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getSQLKeywords",
          methodArgs = {}
        )
    })
    public void test_getSQLKeywords() throws SQLException {
        assertTrue("Incorrect SQL keywords", !"".equals(meta.getSQLKeywords()
                .trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData#getSQLStateType()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getSQLStateType",
          methodArgs = {}
        )
    })
    public void test_getSQLStateType() throws SQLException {
        int type = meta.getSQLStateType();
        switch (type) {
        case DatabaseMetaData.sqlStateSQL99:
        case DatabaseMetaData.sqlStateXOpen:
            // these types are OK
            break;
        default:
            fail("Incorrect SQL state types");
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#getSchemaTerm()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getSchemaTerm",
          methodArgs = {}
        )
    })
    public void test_getSchemaTerm() throws SQLException {
        assertNotNull("Incorrect schema term", meta.getSchemaTerm());
    }

    /**
     * @tests java.sql.DatabaseMetaData#getSchemas()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getSchemas",
          methodArgs = {}
        )
    })
    public void test_getSchemas() throws SQLException {
        ResultSet rs = meta.getSchemas();
        ResultSetMetaData rsmd = rs.getMetaData();
        assertTrue("Rows do not obtained", rs.next());
        int col = rsmd.getColumnCount();
        assertEquals("Incorrect number of columns", 1, col);
        String[] columnNames = { "TABLE_SCHEM", "TABLE_CATALOG" };
        for (int c = 1; c <= col; ++c) {
            assertEquals("Incorrect column name", columnNames[c - 1], rsmd
                    .getColumnName(c));
        }
        rs.close();
    }

    /**
     * @tests java.sql.DatabaseMetaData#getSearchStringEscape()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getSearchStringEscape",
          methodArgs = {}
        )
    })
    public void test_getSearchStringEscape() throws SQLException {
        assertTrue("Incorrect search string escape", !"".equals(meta
                .getSearchStringEscape().trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData#getStringFunctions()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getStringFunctions",
          methodArgs = {}
        )
    })
    public void test_getStringFunctions() throws SQLException {
        assertTrue("Incorrect string functions", "".equals(meta
                .getStringFunctions().trim()));
    }

    /**
     * @tests java.sql.DatabaseMetaData #getSuperTables(java.lang.String,
     *        java.lang.String, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "getSuperTables",
          methodArgs = {String.class, String.class, String.class}
        )
    })
    public void test_getSuperTablesLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData #getSuperTypes(java.lang.String,
     *        java.lang.String, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "getSuperTypes",
          methodArgs = {String.class, String.class, String.class}
        )
    })
    public void test_getSuperTypesLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#getSystemFunctions()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getSystemFunctions",
          methodArgs = {}
        )
    })
    public void test_getSystemFunctions() throws SQLException {
        assertTrue("No system function exist", meta.getSystemFunctions()
                .trim().equals(""));
    }

    /**
     * @tests java.sql.DatabaseMetaData #getTablePrivileges(java.lang.String,
     *        java.lang.String, java.lang.String)
     *        
     *  TODO GRANT is not supported      
     */
/*    public void test_getTablePrivilegesLjava_lang_StringLjava_lang_StringLjava_lang_String()
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
*/
    /**
     * @tests java.sql.DatabaseMetaData#getTableTypes()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getTableTypes",
          methodArgs = {}
        )
    })
    public void test_getTableTypes() throws SQLException {
        String[] tableTypes = { "LOCAL TEMPORARY", "TABLE", "VIEW" };
        ResultSet rs = meta.getTableTypes();

        while (rs.next()) {
            assertTrue("Wrong table type", Arrays.binarySearch(tableTypes, rs
                    .getString("TABLE_TYPE")) > -1);
        }
        rs.close();
    }

    /**
     * @tests java.sql.DatabaseMetaData #getTables(java.lang.String,
     *        java.lang.String, java.lang.String, java.lang.String[])
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getTables",
          methodArgs = {String.class, String.class, String.class, String[].class}
        )
    })
    public void test_getTablesLjava_lang_StringLjava_lang_StringLjava_lang_String$Ljava_lang_String()
            throws SQLException {
        String[] tablesName = { VIEW_NAME, DatabaseCreator.TEST_TABLE1,
                DatabaseCreator.TEST_TABLE3 };
        String[] tablesType = { "TABLE", "VIEW" };

        // case 1. get all tables. There are two tables and one view in the
        // database
        ResultSet rs = meta.getTables(null, null, null, null);

        while (rs.next()) {
            assertTrue("Wrong table name", Arrays.binarySearch(tablesName, rs
                    .getString("TABLE_NAME")) > -1);
 //           assertNull("Wrong table schema", rs.getString("TABLE_SCHEM"));
            assertTrue("Wrong table type", Arrays.binarySearch(tablesType, rs
                    .getString("TABLE_TYPE")) > -1);
            assertEquals("Wrong parameter REMARKS", "", rs.getString("REMARKS"));
        }
        rs.close();

        // case 2. get tables with specified types. There are no tables of such
        // types
        rs = meta.getTables(conn.getCatalog(), null, null, new String[] {
                "SYSTEM TABLE", "LOCAL TEMPORARY" });
        assertFalse("Some tables exist", rs.next());
        rs.close();

        // case 3. get tables with specified types. There is a table of such
        // types
        rs = meta.getTables(conn.getCatalog(), null, null, new String[] {
                "VIEW", "LOCAL TEMPORARY" });

        assertTrue("No tables exist", rs.next());
        assertEquals("Wrong table name", VIEW_NAME, rs.getString("TABLE_NAME"));
//        assertNull("Wrong table schema", rs.getString("TABLE_SCHEM"));
        assertEquals("Wrong table type", "VIEW", rs.getString("TABLE_TYPE"));
        assertEquals("Wrong parameter REMARKS", "", rs.getString("REMARKS"));
        assertFalse("Wrong size of result set", rs.next());
        assertFalse("Some tables exist", rs.next());
        rs.close();

        // case 4. get all tables using tables pattern.
        // There are two tables and one view in the database
        rs = meta.getTables(null, null, "%", null);

        while (rs.next()) {
            assertTrue("Wrong table name", Arrays.binarySearch(tablesName, rs
                    .getString("TABLE_NAME")) > -1);
//            assertNull("Wrong table schema", rs.getString("TABLE_SCHEM"));
            assertTrue("Wrong table type", Arrays.binarySearch(tablesType, rs
                    .getString("TABLE_TYPE")) > -1);
            assertEquals("Wrong parameter REMARKS", "", rs.getString("REMARKS"));
        }
        rs.close();
    }

    /**
     * @tests java.sql.DatabaseMetaData#getTimeDateFunctions()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getTimeDateFunctions",
          methodArgs = {}
        )
    })
    public void test_getTimeDateFunctions() throws SQLException {
        assertFalse("No time and data functions exist", !meta
                .getTimeDateFunctions().trim().equals(""));
    }

    /**
     * @tests java.sql.DatabaseMetaData#getTypeInfo()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getTypeInfo",
          methodArgs = {}
        )
    })
    public void test_getTypeInfo() throws SQLException {
        insertNewRecord();

        ResultSet rs = meta.getTypeInfo();

        final String[] names = { "TYPE_NAME", "DATA_TYPE", "PRECISION",
                "LITERAL_PREFIX", "LITERAL_SUFFIX", "CREATE_PARAMS",
                "NULLABLE", "CASE_SENSITIVE", "SEARCHABLE",
                "UNSIGNED_ATTRIBUTE", "FIXED_PREC_SCALE", "AUTO_INCREMENT",
                "LOCAL_TYPE_NAME", "MINIMUM_SCALE", "MAXIMUM_SCALE",
                "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "NUM_PREC_RADIX" };
        Arrays.sort(names);

        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
            assertTrue("wrong column was return", Arrays.binarySearch(names, rs
                    .getMetaData().getColumnName(i + 1)) > -1);
        }

        int[] types = { Types.ARRAY, Types.BIGINT, Types.BINARY, Types.BIT,
                Types.BLOB, Types.BOOLEAN, Types.CHAR, Types.CLOB,
                Types.DATALINK, Types.DATE, Types.DECIMAL, Types.DISTINCT,
                Types.DOUBLE, Types.FLOAT, Types.INTEGER, Types.JAVA_OBJECT,
                Types.LONGVARBINARY, Types.LONGVARCHAR, Types.NULL,
                Types.NUMERIC, Types.OTHER, Types.REAL, Types.REF,
                Types.SMALLINT, Types.STRUCT, Types.TIME, Types.TIMESTAMP,
                Types.TINYINT, Types.VARBINARY, Types.VARCHAR };
        Arrays.sort(types);

        while (rs.next()) {
            assertTrue("wrong type was return ", Arrays.binarySearch(types, rs
                    .getInt("DATA_TYPE")) > -1);
        }
        rs.close();
    }

    /**
     * @tests java.sql.DatabaseMetaData #getUDTs(java.lang.String,
     *        java.lang.String, java.lang.String, int[])
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "getUDTs",
          methodArgs = {String.class, String.class, String.class, int[].class}
        )
    })
    public void test_getUDTsLjava_lang_StringLjava_lang_StringLjava_lang_String$I()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#getURL()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "getURL",
          methodArgs = {}
        )
    })
    public void test_getURL() throws SQLException {
        assertEquals("Wrong url", Support_SQL.sqlUrl, meta.getURL());
    }

    /**
     * @tests java.sql.DatabaseMetaData#getUserName()
     * 
     *  TODO not supported
     */
/*    public void test_getUserName() throws SQLException {
        assertEquals("Wrong user name", Support_SQL.sqlLogin + "@"
                + Support_SQL.sqlHost, meta.getUserName());
    }
*/
    /**
     * @tests java.sql.DatabaseMetaData #getVersionColumns(java.lang.String,
     *        java.lang.String, java.lang.String)
     *  
     *  TODO trigger is not supported       
     */
/*    public void test_getVersionColumnsLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        insertNewRecord();

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
*/
    /**
     * @tests java.sql.DatabaseMetaData#insertsAreDetected(int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                  "Verification with invalid parameters missed.",
      targets = {
        @TestTarget(
          methodName = "insertsAreDetected",
          methodArgs = {int.class}
        )
    })
    public void test_insertsAreDetectedI() throws SQLException {
        assertFalse(
                "visible row insert can be detected for TYPE_FORWARD_ONLY type",
                meta.insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(
                "visible row insert can be detected for TYPE_SCROLL_INSENSITIVE type",
                meta.insertsAreDetected(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertFalse(
                "visible row insert can be detected for TYPE_SCROLL_SENSITIVE type",
                meta.insertsAreDetected(ResultSet.TYPE_SCROLL_SENSITIVE));
    }

    /**
     * @tests java.sql.DatabaseMetaData#isCatalogAtStart()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "isCatalogAtStart",
          methodArgs = {}
        )
    })
    public void test_isCatalogAtStart() throws SQLException {
        assertFalse(
                "catalog doesn't appear at the start of a fully qualified table name",
                meta.isCatalogAtStart());
    }

    /**
     * @tests java.sql.DatabaseMetaData#isReadOnly()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "isReadOnly",
          methodArgs = {}
        )
    })
    public void test_isReadOnly() throws SQLException {
        assertFalse("database is not read-only", meta.isReadOnly());
    }

    /**
     * @tests java.sql.DatabaseMetaData#locatorsUpdateCopy()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "locatorsUpdateCopy",
          methodArgs = {}
        )
    })
    public void test_locatorsUpdateCopy() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#nullPlusNonNullIsNull()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "nullPlusNonNullIsNull",
          methodArgs = {}
        )
    })
    public void test_nullPlusNonNullIsNull() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#nullsAreSortedAtEnd()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "nullsAreSortedAtEnd",
          methodArgs = {}
        )
    })
    public void test_nullsAreSortedAtEnd() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#nullsAreSortedAtStart()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "nullsAreSortedAtStart",
          methodArgs = {}
        )
    })
    public void test_nullsAreSortedAtStart() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#nullsAreSortedHigh()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "nullsAreSortedHigh",
          methodArgs = {}
        )
    })
    public void test_nullsAreSortedHigh() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#nullsAreSortedLow()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "nullsAreSortedLow",
          methodArgs = {}
        )
    })
    public void test_nullsAreSortedLow() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#othersDeletesAreVisible(int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                  "Verification with invalid parameters missed.",
      targets = {
        @TestTarget(
          methodName = "othersDeletesAreVisible",
          methodArgs = {int.class}
        )
    })
    public void test_othersDeletesAreVisibleI() throws SQLException {
        assertFalse(
                "deletes made by others are visible for TYPE_FORWARD_ONLY type",
                meta.othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(
                "deletes made by others are visible for TYPE_SCROLL_INSENSITIVE type",
                meta.othersDeletesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertFalse(
                "deletes made by others are visible for TYPE_SCROLL_SENSITIVE type",
                meta.othersDeletesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("deletes made by others are visible for unknown type", meta
                .othersDeletesAreVisible(100));
    }

    /**
     * @tests java.sql.DatabaseMetaData#othersInsertsAreVisible(int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                  "Verification with invalid parameters missed.",
      targets = {
        @TestTarget(
          methodName = "othersInsertsAreVisible",
          methodArgs = {int.class}
        )
    })
    public void test_othersInsertsAreVisibleI() throws SQLException {
        assertFalse(
                "inserts made by others are visible for TYPE_FORWARD_ONLY type",
                meta.othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(
                "inserts made by others are visible for TYPE_SCROLL_INSENSITIVE type",
                meta.othersInsertsAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertFalse(
                "inserts made by others are visible for TYPE_SCROLL_SENSITIVE type",
                meta.othersInsertsAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("inserts made by others are visible for unknown type", meta
                .othersInsertsAreVisible(100));
    }

    /**
     * @tests java.sql.DatabaseMetaData#othersUpdatesAreVisible(int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                  "Verification with invalid parameters missed.",
      targets = {
        @TestTarget(
          methodName = "othersUpdatesAreVisible",
          methodArgs = {int.class}
        )
    })
    public void test_othersUpdatesAreVisibleI() throws SQLException {
        assertFalse(
                "updates made by others are visible for TYPE_FORWARD_ONLY type",
                meta.othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(
                "updates made by others are visible for TYPE_SCROLL_INSENSITIVE type",
                meta.othersUpdatesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertFalse(
                "updates made by others are visible for TYPE_SCROLL_SENSITIVE type",
                meta.othersUpdatesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("updates made by others are visible for unknown type", meta
                .othersUpdatesAreVisible(100));
    }

    /**
     * @tests java.sql.DatabaseMetaData#ownDeletesAreVisible(int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                "Verification with invalid parameters missed.",
      targets = {
        @TestTarget(
          methodName = "ownDeletesAreVisible",
          methodArgs = {int.class}
        )
    })
    public void test_ownDeletesAreVisibleI() throws SQLException {
        // TODO not supported
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
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                "Verification with invalid parameters missed.",
      targets = {
        @TestTarget(
          methodName = "ownInsertsAreVisible",
          methodArgs = {int.class}
        )
    })
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
     * @tests java.sql.DatabaseMetaData#ownUpdatesAreVisible(int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                "Verification with invalid parameters missed.",
      targets = {
        @TestTarget(
          methodName = "ownUpdatesAreVisible",
          methodArgs = {int.class}
        )
    })
    public void test_ownUpdatesAreVisibleI() throws SQLException {
        // TODO not supported
//        assertFalse(
//                "result set's own updates are visible for TYPE_FORWARD_ONLY type",
//                meta.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
//        assertFalse(
//                "result set's own updates are visible for TYPE_SCROLL_INSENSITIVE type",
//                meta.ownUpdatesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
//        assertFalse(
//                "result set's own updates are visible for TYPE_SCROLL_SENSITIVE type",
//                meta.ownUpdatesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("result set's own updates are visible for unknown type",
                meta.ownUpdatesAreVisible(100));
    }

    /**
     * @tests java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "storesLowerCaseIdentifiers",
          methodArgs = {}
        )
    })
    public void test_storesLowerCaseIdentifiers() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "storesLowerCaseQuotedIdentifiers",
          methodArgs = {}
        )
    })
    public void test_storesLowerCaseQuotedIdentifiers() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#storesMixedCaseIdentifiers()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Method storesMixedCaseQuotedIdentifiers was invoked instead of storesMixedCaseIdentifiers",
      targets = {
        @TestTarget(
          methodName = "storesMixedCaseIdentifiers",
          methodArgs = {}
        )
    })
    public void test_storesMixedCaseIdentifiers() throws SQLException {
        insertNewRecord();

        String selectQuery = "SELECT fieLD1 FROM "
                + DatabaseCreator.TEST_TABLE1;

        try {
            statement.executeQuery(selectQuery);
            if (!meta.storesMixedCaseQuotedIdentifiers()) {
                fail("mixed case are supported");
            }
        } catch (SQLException e) {
            if (meta.storesMixedCaseQuotedIdentifiers()) {
                fail("mixed case are not supported");
            }
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#storesMixedCaseQuotedIdentifiers()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "storesMixedCaseQuotedIdentifiers",
          methodArgs = {}
        )
    })
    public void test_storesMixedCaseQuotedIdentifiers() throws SQLException {
        String quote = meta.getIdentifierQuoteString();

        insertNewRecord();

        String selectQuery = "SELECT " + quote + "fieLD1" + quote + " FROM "
                + DatabaseCreator.TEST_TABLE1;

        try {
            statement.executeQuery(selectQuery);
            if (!meta.storesMixedCaseQuotedIdentifiers()) {
                fail("mixed case is supported");
            }
        } catch (SQLException e) {
            if (meta.storesMixedCaseQuotedIdentifiers()) {
                fail("mixed case is not supported");
            }
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "storesUpperCaseIdentifiers",
          methodArgs = {}
        )
    })
    public void test_storesUpperCaseIdentifiers() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "storesUpperCaseQuotedIdentifiers",
          methodArgs = {}
        )
    })
    public void test_storesUpperCaseQuotedIdentifiers() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsANSI92EntryLevelSQL()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsANSI92EntryLevelSQL",
          methodArgs = {}
        )
    })
    public void test_supportsANSI92EntryLevelSQL() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsANSI92FullSQL()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsANSI92FullSQL",
          methodArgs = {}
        )
    })
    public void test_supportsANSI92FullSQL() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsANSI92IntermediateSQL()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsANSI92IntermediateSQL",
          methodArgs = {}
        )
    })
    public void test_supportsANSI92IntermediateSQL() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsAlterTableWithAddColumn()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsAlterTableWithAddColumn",
          methodArgs = {}
        )
    })
    public void test_supportsAlterTableWithAddColumn() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsAlterTableWithDropColumn()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsAlterTableWithDropColumn",
          methodArgs = {}
        )
    })
    public void test_supportsAlterTableWithDropColumn() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsBatchUpdates()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsBatchUpdates",
          methodArgs = {}
        )
    })
    public void test_supportsBatchUpdates() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCatalogsInDataManipulation()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsCatalogsInDataManipulation",
          methodArgs = {}
        )
    })
    public void test_supportsCatalogsInDataManipulation() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCatalogsInIndexDefinitions()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsCatalogsInIndexDefinitions",
          methodArgs = {}
        )
    })
    public void test_supportsCatalogsInIndexDefinitions() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCatalogsInPrivilegeDefinitions()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsCatalogsInPrivilegeDefinitions",
          methodArgs = {}
        )
    })
    public void test_supportsCatalogsInPrivilegeDefinitions()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCatalogsInProcedureCalls()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsCatalogsInProcedureCalls",
          methodArgs = {}
        )
    })
    public void test_supportsCatalogsInProcedureCalls() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCatalogsInTableDefinitions()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsCatalogsInTableDefinitions",
          methodArgs = {}
        )
    })
    public void test_supportsCatalogsInTableDefinitions() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsColumnAliasing()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "supportsColumnAliasing",
          methodArgs = {}
        )
    })
    public void test_supportsColumnAliasing() throws SQLException {
        insertNewRecord();

        String alias = "FIELD3";
        String selectQuery = "SELECT field1 AS " + alias + " FROM "
                + DatabaseCreator.TEST_TABLE1;
        ResultSet rs = statement.executeQuery(selectQuery);
        ResultSetMetaData rsmd = rs.getMetaData();

        if (meta.supportsColumnAliasing()) {
            // supports aliasing
            assertEquals("Wrong count of columns", 1, rsmd.getColumnCount());
            assertEquals("Aliasing is not supported", alias, rsmd
                    .getColumnLabel(1));
        } else {
            // doesn't support aliasing
            assertEquals("Aliasing is supported", 0, rsmd.getColumnCount());
        }
        rs.close();
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsConvert()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsConvert",
          methodArgs = {}
        )
    })
    public void test_supportsConvert() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsConvert(int, int)
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsConvert",
          methodArgs = {int.class, int.class}
        )
    })
    public void test_supportsConvertII() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCoreSQLGrammar()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsCoreSQLGrammar",
          methodArgs = {}
        )
    })
    public void test_supportsCoreSQLGrammar() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsCorrelatedSubqueries()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsCorrelatedSubqueries",
          methodArgs = {}
        )
    })
    public void test_supportsCorrelatedSubqueries() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsDataDefinitionAndDataManipulationTransactions()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsDataDefinitionAndDataManipulationTransactions",
          methodArgs = {}
        )
    })
    public void test_supportsDataDefinitionAndDataManipulationTransactions()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsDataManipulationTransactionsOnly()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsDataManipulationTransactionsOnly",
          methodArgs = {}
        )
    })
    public void test_supportsDataManipulationTransactionsOnly()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsDifferentTableCorrelationNames()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsDifferentTableCorrelationNames",
          methodArgs = {}
        )
    })
    public void test_supportsDifferentTableCorrelationNames()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsExpressionsInOrderBy()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "supportsExpressionsInOrderBy",
          methodArgs = {}
        )
    })
    public void test_supportsExpressionsInOrderBy() throws SQLException {
        insertNewRecord();

        String selectQuery = "SELECT * FROM " + DatabaseCreator.TEST_TABLE1
                + " ORDER BY id + field3";

        try {
            statement.executeQuery(selectQuery);
            if (!meta.supportsExpressionsInOrderBy()) {
                fail("Expressions in order by are supported");
            }
        } catch (SQLException e) {
            if (meta.supportsExpressionsInOrderBy()) {
                fail("Expressions in order by are not supported");
            }
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsExtendedSQLGrammar()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsExtendedSQLGrammar",
          methodArgs = {}
        )
    })
    public void test_supportsExtendedSQLGrammar() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsFullOuterJoins()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsFullOuterJoins",
          methodArgs = {}
        )
    })
    public void test_supportsFullOuterJoins() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsGetGeneratedKeys",
          methodArgs = {}
        )
    })
    public void test_supportsGetGeneratedKeys() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsGroupBy()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "supportsGroupBy",
          methodArgs = {}
        )
    })
    public void test_supportsGroupBy() throws SQLException {
        insertNewRecord();

        String selectQuery = "SELECT * FROM " + DatabaseCreator.TEST_TABLE1
                + " GROUP BY field3";

        try {
            statement.executeQuery(selectQuery);
            if (!meta.supportsGroupBy()) {
                fail("group by are supported");
            }
        } catch (SQLException e) {
            if (meta.supportsGroupBy()) {
                fail("group by are not supported");
            }
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsGroupByBeyondSelect()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsGroupByBeyondSelect",
          methodArgs = {}
        )
    })
    public void test_supportsGroupByBeyondSelect() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsGroupByUnrelated()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "supportsGroupByUnrelated",
          methodArgs = {}
        )
    })
    public void test_supportsGroupByUnrelated() throws SQLException {
        insertNewRecord();

        String selectQuery = "SELECT field1, field2 FROM "
                + DatabaseCreator.TEST_TABLE1 + " GROUP BY field3";

        try {
            statement.executeQuery(selectQuery);
            if (!meta.supportsGroupByUnrelated()) {
                fail("unrelated columns in group by are supported");
            }
        } catch (SQLException e) {
            if (meta.supportsGroupByUnrelated()) {
                fail("unrelated columns in group by are not supported");
            }
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsIntegrityEnhancementFacility()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsIntegrityEnhancementFacility",
          methodArgs = {}
        )
    })
    public void test_supportsIntegrityEnhancementFacility() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsLikeEscapeClause()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsLikeEscapeClause",
          methodArgs = {}
        )
    })
    public void test_supportsLikeEscapeClause() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsLimitedOuterJoins()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsLimitedOuterJoins",
          methodArgs = {}
        )
    })
    public void test_supportsLimitedOuterJoins() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMinimumSQLGrammar()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsMinimumSQLGrammar",
          methodArgs = {}
        )
    })
    public void test_supportsMinimumSQLGrammar() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMixedCaseIdentifiers()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsMixedCaseIdentifiers",
          methodArgs = {}
        )
    })
    public void test_supportsMixedCaseIdentifiers() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMixedCaseQuotedIdentifiers()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsMixedCaseQuotedIdentifiers",
          methodArgs = {}
        )
    })
    public void test_supportsMixedCaseQuotedIdentifiers() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMultipleOpenResults()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsMultipleOpenResults",
          methodArgs = {}
        )
    })
    public void test_supportsMultipleOpenResults() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMultipleResultSets()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsMultipleResultSets",
          methodArgs = {}
        )
    })
    public void test_supportsMultipleResultSets() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsMultipleTransactions()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsMultipleTransactions",
          methodArgs = {}
        )
    })
    public void test_supportsMultipleTransactions() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsNamedParameters()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsNamedParameters",
          methodArgs = {}
        )
    })
    public void test_supportsNamedParameters() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsNonNullableColumns()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "supportsNonNullableColumns",
          methodArgs = {}
        )
    })
    public void test_supportsNonNullableColumns() throws SQLException {
        assertTrue(
                "columns in this database may not be defined as non-nullable",
                meta.supportsNonNullableColumns());
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsOpenCursorsAcrossCommit()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsOpenCursorsAcrossCommit",
          methodArgs = {}
        )
    })
    public void test_supportsOpenCursorsAcrossCommit() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsOpenCursorsAcrossRollback()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsOpenCursorsAcrossRollback",
          methodArgs = {}
        )
    })
    public void test_supportsOpenCursorsAcrossRollback() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsOpenStatementsAcrossCommit()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsOpenStatementsAcrossCommit",
          methodArgs = {}
        )
    })
    public void test_supportsOpenStatementsAcrossCommit() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsOpenStatementsAcrossRollback()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsOpenStatementsAcrossRollback",
          methodArgs = {}
        )
    })
    public void test_supportsOpenStatementsAcrossRollback() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsOrderByUnrelated()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "supportsOrderByUnrelated",
          methodArgs = {}
        )
    })
    public void test_supportsOrderByUnrelated() throws SQLException {
        insertNewRecord();

        String selectQuery = "SELECT field1, field2 FROM "
                + DatabaseCreator.TEST_TABLE1 + " ORDER BY id + field3";

        try {
            statement.executeQuery(selectQuery);
            if (!meta.supportsOrderByUnrelated()) {
                fail("unrelated columns in order by are supported");
            }
        } catch (SQLException e) {
            if (meta.supportsOrderByUnrelated()) {
                fail("unrelated columns in order by are not supported");
            }
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsOuterJoins()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsOuterJoins",
          methodArgs = {}
        )
    })
    public void test_supportsOuterJoins() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsPositionedDelete()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsPositionedDelete",
          methodArgs = {}
        )
    })
    public void test_supportsPositionedDelete() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsPositionedUpdate()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsPositionedUpdate",
          methodArgs = {}
        )
    })
    public void test_supportsPositionedUpdate() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsResultSetConcurrency(int, int)
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsResultSetConcurrency",
          methodArgs = {int.class, int.class}
        )
    })
    public void test_supportsResultSetConcurrencyII() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsResultSetHoldability(int)
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsResultSetHoldability",
          methodArgs = {int.class}
        )
    })
    public void test_supportsResultSetHoldabilityI() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsResultSetType(int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                  "Verification with invalid parameters missed.",
      targets = {
        @TestTarget(
          methodName = "supportsResultSetType",
          methodArgs = {int.class}
        )
    })
    public void test_supportsResultSetTypeI() throws SQLException {
        // TODO not supported
//        assertFalse("database supports TYPE_FORWARD_ONLY type", meta
//                .supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
//        assertTrue("database doesn't support TYPE_SCROLL_INSENSITIVE type",
//                meta.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
//        assertFalse("database supports TYPE_SCROLL_SENSITIVE type", meta
//                .supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("database supports unknown type", meta
                .supportsResultSetType(100));
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSavepoints()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsSavepoints",
          methodArgs = {}
        )
    })
    public void test_supportsSavepoints() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSchemasInDataManipulation()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsSchemasInDataManipulation",
          methodArgs = {}
        )
    })
    public void test_supportsSchemasInDataManipulation() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSchemasInIndexDefinitions()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsSchemasInIndexDefinitions",
          methodArgs = {}
        )
    })
    public void test_supportsSchemasInIndexDefinitions() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSchemasInPrivilegeDefinitions()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsSchemasInPrivilegeDefinitions",
          methodArgs = {}
        )
    })
    public void test_supportsSchemasInPrivilegeDefinitions()
            throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSchemasInProcedureCalls()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsSchemasInProcedureCalls",
          methodArgs = {}
        )
    })
    public void test_supportsSchemasInProcedureCalls() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSchemasInTableDefinitions()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsSchemasInTableDefinitions",
          methodArgs = {}
        )
    })
    public void test_supportsSchemasInTableDefinitions() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSelectForUpdate()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "supportsSelectForUpdate",
          methodArgs = {}
        )
    })
    public void test_supportsSelectForUpdate() throws SQLException {
        insertNewRecord();

        String selectQuery = "SELECT field1 FROM "
                + DatabaseCreator.TEST_TABLE1 + " FOR UPDATE";

        try {
            statement.executeQuery(selectQuery);
            if (!meta.supportsSelectForUpdate()) {
                fail("select for update are supported");
            }
        } catch (SQLException e) {
            if (!meta.supportsSelectForUpdate()) {
                fail("select for update are not supported");
            }
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsStatementPooling()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsStatementPooling",
          methodArgs = {}
        )
    })
    public void test_supportsStatementPooling() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsStoredProcedures()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsStoredProcedures",
          methodArgs = {}
        )
    })
    public void test_supportsStoredProcedures() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSubqueriesInComparisons()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsSubqueriesInComparisons",
          methodArgs = {}
        )
    })
    public void test_supportsSubqueriesInComparisons() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSubqueriesInExists()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "supportsSubqueriesInExists",
          methodArgs = {}
        )
    })
    public void test_supportsSubqueriesInExists() throws SQLException {
        insertNewRecord();

        String selectQuery = "SELECT field1 FROM "
                + DatabaseCreator.TEST_TABLE1
                + " WHERE EXISTS(SELECT field2 FROM "
                + DatabaseCreator.TEST_TABLE1 + ")";

        try {
            statement.executeQuery(selectQuery);
            if (!meta.supportsSubqueriesInExists()) {
                fail("Subqueries in exists are supported");
            }
        } catch (SQLException e) {
            if (meta.supportsSubqueriesInExists()) {
                fail("Subqueries in exists are not supported");
            }
        }
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSubqueriesInIns()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsSubqueriesInIns",
          methodArgs = {}
        )
    })
    public void test_supportsSubqueriesInIns() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsSubqueriesInQuantifieds()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsSubqueriesInQuantifieds",
          methodArgs = {}
        )
    })
    public void test_supportsSubqueriesInQuantifieds() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsTableCorrelationNames()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed",
      targets = {
        @TestTarget(
          methodName = "supportsTableCorrelationNames",
          methodArgs = {}
        )
    })
    public void test_supportsTableCorrelationNames() throws SQLException {
        insertNewRecord();

        String corelationName = "TABLE_NAME";
        String selectQuery = "SELECT * FROM " + DatabaseCreator.TEST_TABLE1
                + " AS " + corelationName;
        ResultSet rs = statement.executeQuery(selectQuery);
        ResultSetMetaData rsmd = rs.getMetaData();
        int numOfColumn = rsmd.getColumnCount();

        for (int i = 0; i < numOfColumn; i++) {
            if (meta.supportsTableCorrelationNames()) {
                assertEquals("Corelation names is not supported",
                        corelationName, rsmd.getTableName(i + 1));
            } else {
                assertEquals("Corelation names is supported",
                        DatabaseCreator.TEST_TABLE1, rsmd.getTableName(i + 1));
            }
        }
        rs.close();
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsTransactionIsolationLevel(int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                  "Verification with invalid parameters missed.",
      targets = {
        @TestTarget(
          methodName = "supportsTransactionIsolationLevel",
          methodArgs = {int.class}
        )
    })
    public void test_supportsTransactionIsolationLevelI() throws SQLException {
        assertFalse("database supports TRANSACTION_NONE isolation level", meta
                .supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE));
        // TODO only Connection.TRANSACTION_SERIALIZABLE is supported
//        assertTrue(
//                "database doesn't supports TRANSACTION_READ_COMMITTED isolation level",
//                meta
//                        .supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED));
//        assertTrue(
//                "database doesn't supports TRANSACTION_READ_UNCOMMITTED isolation level",
//                meta
//                        .supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED));
//        assertTrue(
//               "database doesn't supports TRANSACTION_REPEATABLE_READ isolation level",
//                meta
//                        .supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ));
        assertTrue(
                "database doesn't supports TRANSACTION_SERIALIZABLE isolation level",
                meta
                        .supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE));
        assertFalse("database supports unknown isolation level", meta
                .supportsTransactionIsolationLevel(100));
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsTransactions()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsTransactions",
          methodArgs = {}
        )
    })
    public void test_supportsTransactions() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsUnion()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsUnion",
          methodArgs = {}
        )
    })
    public void test_supportsUnion() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#supportsUnionAll()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "supportsUnionAll",
          methodArgs = {}
        )
    })
    public void test_supportsUnionAll() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#updatesAreDetected(int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "SQLException checking missed. " +
                  "Verification with invalid parameters missed.",
      targets = {
        @TestTarget(
          methodName = "updatesAreDetected",
          methodArgs = {int.class}
        )
    })
    public void test_updatesAreDetectedI() throws SQLException {
        assertFalse(
                "visible row update can be detected for TYPE_FORWARD_ONLY type",
                meta.updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(
                "visible row update can be detected for TYPE_SCROLL_INSENSITIVE type",
                meta.updatesAreDetected(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertFalse(
                "visible row update can be detected for TYPE_SCROLL_SENSITIVE type",
                meta.updatesAreDetected(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("visible row update can be detected for unknown type", meta
                .updatesAreDetected(100));
    }

    /**
     * @tests java.sql.DatabaseMetaData#usesLocalFilePerTable()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "usesLocalFilePerTable",
          methodArgs = {}
        )
    })
    public void test_usesLocalFilePerTable() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    /**
     * @tests java.sql.DatabaseMetaData#usesLocalFiles()
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test",
      targets = {
        @TestTarget(
          methodName = "usesLocalFiles",
          methodArgs = {}
        )
    })
    public void test_usesLocalFiles() throws SQLException {
        // TODO: JDBC does not implement this functionality
    }

    private void insertNewRecord() throws SQLException {
        String insertQuery = "INSERT INTO " + DatabaseCreator.TEST_TABLE1
                + " (id, field1, field2, field3) VALUES(" + id + ", '"
                + "value" + id + "', " + id + ", " + id + ")";
        id++;
        statement.execute(insertQuery);
    }
}
