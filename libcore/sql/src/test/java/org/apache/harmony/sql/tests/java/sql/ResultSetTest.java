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

public class ResultSetTest extends TestCase {

    /*
     * Public statics test
     */
    public void testPublicStatics() {

        HashMap<String, Integer> thePublicStatics = new HashMap<String, Integer>();
        thePublicStatics.put("CLOSE_CURSORS_AT_COMMIT",
                new java.lang.Integer(2));
        thePublicStatics.put("HOLD_CURSORS_OVER_COMMIT", new java.lang.Integer(
                1));
        thePublicStatics.put("CONCUR_UPDATABLE", new java.lang.Integer(1008));
        thePublicStatics.put("CONCUR_READ_ONLY", new java.lang.Integer(1007));
        thePublicStatics.put("TYPE_SCROLL_SENSITIVE", new java.lang.Integer(
                1005));
        thePublicStatics.put("TYPE_SCROLL_INSENSITIVE", new java.lang.Integer(
                1004));
        thePublicStatics.put("TYPE_FORWARD_ONLY", new java.lang.Integer(1003));
        thePublicStatics.put("FETCH_UNKNOWN", new java.lang.Integer(1002));
        thePublicStatics.put("FETCH_REVERSE", new java.lang.Integer(1001));
        thePublicStatics.put("FETCH_FORWARD", new java.lang.Integer(1000));

        /*
         * System.out.println( "CLOSE_CURSORS_AT_COMMIT: " +
         * ResultSet.CLOSE_CURSORS_AT_COMMIT ); System.out.println(
         * "HOLD_CURSORS_OVER_COMMIT: " + ResultSet.HOLD_CURSORS_OVER_COMMIT );
         * System.out.println( "CONCUR_UPDATABLE: " + ResultSet.CONCUR_UPDATABLE );
         * System.out.println( "CONCUR_READ_ONLY: " + ResultSet.CONCUR_READ_ONLY );
         * System.out.println( "TYPE_SCROLL_SENSITIVE: " +
         * ResultSet.TYPE_SCROLL_SENSITIVE ); System.out.println(
         * "TYPE_SCROLL_INSENSITIVE: " + ResultSet.TYPE_SCROLL_INSENSITIVE );
         * System.out.println( "TYPE_FORWARD_ONLY: " +
         * ResultSet.TYPE_FORWARD_ONLY ); System.out.println( "FETCH_UNKNOWN: " +
         * ResultSet.FETCH_UNKNOWN ); System.out.println( "FETCH_REVERSE: " +
         * ResultSet.FETCH_REVERSE ); System.out.println( "FETCH_FORWARD: " +
         * ResultSet.FETCH_FORWARD );
         */

        Class<?> resultSetClass;
        try {
            resultSetClass = Class.forName("java.sql.ResultSet");
        } catch (ClassNotFoundException e) {
            fail("java.sql.ResultSet class not found!");
            return;
        } // end try

        Field[] theFields = resultSetClass.getDeclaredFields();
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

} // end class ResultSetTest
