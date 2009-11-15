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

/*
 * Load DriverManager class and initialize the class with SecurityManager
 * Regression for HARMONY-4303
 */
public class TestMainForDriver {
    public static void main(String[] args) throws Throwable {
        // Install SecurityManager
        System.setSecurityManager(new SecurityManager());
        // Load java.sql.DriverManager and it will invoke its <clinit> method
        try {
            Class.forName("java.sql.DriverManager");
        } catch (ExceptionInInitializerError e) {
            // ExceptionInInitializerError is caused by AccessControlException
            throw e.getException();
        }
    }
}
