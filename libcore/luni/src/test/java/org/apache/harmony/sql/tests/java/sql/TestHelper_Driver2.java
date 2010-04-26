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

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Basic JDBC driver implementation to help with tests
 * 
 */
public class TestHelper_Driver2 extends TestHelper_Driver1 {

    static {
        Driver theDriver = new TestHelper_Driver2();
        /*
         * System.out.println("Driver2 classloader: " +
         * theDriver.getClass().getClassLoader() ); System.out.println("Driver2
         * object is: " + theDriver );
         */
        try {
            DriverManager.registerDriver(theDriver);
        } catch (SQLException e) {
            System.out.println("Failed to register driver!");
        }
    } // end static block initializer

    protected TestHelper_Driver2() {
        super();
        baseURL = "jdbc:mikes2";
    } // end constructor TestHelper_Driver1()

} // end class TestHelper_Driver2

