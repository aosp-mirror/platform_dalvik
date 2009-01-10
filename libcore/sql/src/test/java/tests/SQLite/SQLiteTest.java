/*
 * Copyright (C) 2008 The Android Open Source Project
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

package tests.SQLite;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteTest extends TestCase {
    public static Connection conn;
    public static File dbFile = null;
    
    public void setup() {
        String tmp = System.getProperty("ctsdir");
        File tmpDir = new File(tmp);
        try {
            if (tmpDir.isDirectory()) {
                dbFile = File.createTempFile("sqliteTest", ".db", tmpDir);
                dbFile.deleteOnExit();
            } else {
                System.out.println("ctsdir does not exist");
            }
            
            Class.forName("SQLite.JDBCDriver").newInstance();

            if (dbFile.exists()) {
                System.out.println("SQLTest.getSQLiteConnection()File:"
                        + dbFile.getName());
            }
            System.out.println("DB File created and Driver instantiated");
            System.out.println("Path "+ dbFile.getPath());
            conn = DriverManager.getConnection("jdbc:sqlite:/"
                    + dbFile.getPath());
            assertNotNull("Connection created ",conn);

        } catch (IOException e) {
            System.out.println("Problem creating test file in " + tmp);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            fail("Exception: " + e.toString());
        } catch (java.lang.Exception e) {
            fail("Exception: " + e.toString());
        }
        
    }
    
    public void tearDown() {
        try {
            if (!conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            fail("Couldn't close Connection: " + e.getMessage());
        }

    }

}
