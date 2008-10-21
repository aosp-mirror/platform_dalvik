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

package SQLite;

import tests.sql.AbstractSqlTest;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;



/**
 * Tests the SQLite.JDBCDriver.
 */
public class JDBCDriverTest extends AbstractSqlTest {



    /**
     * The SQLite db file.
     */
    private final File dbFile = new File("sqliteTest.db");
    
    private final String connectionURL = "jdbc:sqlite:/" + dbFile.getName();

    /**
     * Creates a new instance of this class.
     */
    public JDBCDriverTest(String testName) {
        super(testName);
    }

    /**
     * Sets up an unit test by loading the SQLite.JDBCDriver, getting two
     * connections and calling the setUp method of the super class.
     */
    @Override
    protected void setUp() throws ClassNotFoundException, SQLException,
            java.lang.Exception { // the Exception class needs to be fully
        // qualified since there is an Exception
        // class in the SQLite package.

        super.setUp();
    }

    /**
     * Tears down an unit test by calling the tearDown method of the super class
     * and deleting the SQLite test db file.
     */
    @Override
    protected void tearDown() throws SQLException {
        super.tearDown();
        dbFile.delete();
    }


    @Override
    protected String getConnectionURL() {
        return  connectionURL;
    }
    
    @Override
    protected String getDriverClassName() {
        return "SQLite.JDBCDriver";
    }

    @Override
    protected int getTransactionIsolation() {
        return Connection.TRANSACTION_SERIALIZABLE;
    }
}
