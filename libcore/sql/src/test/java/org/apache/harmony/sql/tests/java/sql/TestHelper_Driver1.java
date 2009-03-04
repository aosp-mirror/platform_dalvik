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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/**
 * A simple implementation of a class implementing a JDBC Driver, for use in the
 * testing of the java.sql.DriverManager class
 * 
 */
public class TestHelper_Driver1 implements Driver {
    int majorVersion = 1;

    int minorVersion = 0;

    String baseURL = "jdbc:mikes1";

    String[] dataSources = { "data1", "data2", "data3" };

    static Driver theDriver;
    static {
        theDriver = new TestHelper_Driver1();
        try {
            DriverManager.registerDriver(theDriver);
        } catch (SQLException e) {
            System.out.println("Failed to register driver!");
        }
    } // end static block initializer

    protected TestHelper_Driver1() {
        super();
    } // end constructor TestHelper_Driver1()

    public boolean acceptsURL(String url) throws SQLException {
        // Check on the supplied String...
        if (url == null) {
            return false;
        }
        // Everything's fine if the quoted url starts with the base url for this
        // driver
        if (url.startsWith(baseURL)) {
            return true;
        }
        return false;
    } // end method acceptsURL

    static String validuser = "theuser";

    static String validpassword = "thepassword";

    static String userProperty = "user";

    static String passwordProperty = "password";

    public Connection connect(String url, Properties info) throws SQLException {
        // Does the URL have the right form?
        if (this.acceptsURL(url)) {
            // The datasource name is the remainder of the url after the ":"
            String datasource = url.substring(baseURL.length() + 1);
            for (String element : dataSources) {
                if (datasource.equals(element)) {
                    /*
                     * Check for user and password, except for datasource =
                     * data1 which is set up not to require a user/password
                     * combination
                     */
                    // It all checks out - so return a connection
                    Connection connection = new TestHelper_Connection1();
                    return connection;
                } // end if
            } // end for
        } // end if
        return null;
    } // end method connect(String, Properties)

    public int getMajorVersion() {
        return majorVersion;
    } // end method getMajorVersion()

    public int getMinorVersion() {
        return minorVersion;
    } // end method getMinorVersion()

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {
        DriverPropertyInfo[] theInfos = { new DriverPropertyInfo(userProperty, "*"),
                new DriverPropertyInfo(passwordProperty, "*"), };
        return theInfos;
    }

    public boolean jdbcCompliant() {
        // Basic version here returns false
        return false;
    }
}
