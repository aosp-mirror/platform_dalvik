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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import junit.framework.TestCase;

public class ConnectionTest extends TestCase {

    /*
     * Public statics test
     */
    public void testPublicStatics() {

        HashMap<String, Integer> thePublicStatics = new HashMap<String, Integer>();
        thePublicStatics.put("TRANSACTION_SERIALIZABLE", new Integer(8));
        thePublicStatics.put("TRANSACTION_REPEATABLE_READ", new Integer(4));
        thePublicStatics.put("TRANSACTION_READ_COMMITTED", new Integer(2));
        thePublicStatics.put("TRANSACTION_READ_UNCOMMITTED", new Integer(1));
        thePublicStatics.put("TRANSACTION_NONE", new Integer(0));

        /*
         * System.out.println( "TRANSACTION_SERIALIZABLE: " +
         * Connection.TRANSACTION_SERIALIZABLE ); System.out.println(
         * "TRANSACTION_REPEATABLE_READ: " +
         * Connection.TRANSACTION_REPEATABLE_READ ); System.out.println(
         * "TRANSACTION_READ_COMMITTED: " +
         * Connection.TRANSACTION_READ_COMMITTED ); System.out.println(
         * "TRANSACTION_READ_UNCOMMITTED: " +
         * Connection.TRANSACTION_READ_UNCOMMITTED ); System.out.println(
         * "TRANSACTION_NONE: " + Connection.TRANSACTION_NONE );
         */

        Class<?> connectionClass;
        try {
            connectionClass = Class.forName("java.sql.Connection");
        } catch (ClassNotFoundException e) {
            fail("java.sql.Connection class not found!");
            return;
        } // end try

        Field[] theFields = connectionClass.getDeclaredFields();
        int requiredModifier = Modifier.PUBLIC + Modifier.STATIC
                + Modifier.FINAL;

        int countPublicStatics = 0;
        for (Field element : theFields) {
            String fieldName = element.getName();
            int theMods = element.getModifiers();
            if (Modifier.isPublic(theMods) && Modifier.isStatic(theMods)) {
                try {
                    Object fieldValue = element.get(null);
                    Object expectedValue = thePublicStatics.get(fieldName);
                    if (expectedValue == null) {
                        fail("Field " + fieldName + " missing!");
                    } // end
                    assertEquals("Field " + fieldName + " value mismatch: ",
                            expectedValue, fieldValue);
                    assertEquals("Field " + fieldName + " modifier mismatch: ",
                            requiredModifier, theMods);
                    countPublicStatics++;
                } catch (IllegalAccessException e) {
                    fail("Illegal access to Field " + fieldName);
                } // end try
            } // end if
        } // end for

    } // end method testPublicStatics

} // end class ConnectionTest

