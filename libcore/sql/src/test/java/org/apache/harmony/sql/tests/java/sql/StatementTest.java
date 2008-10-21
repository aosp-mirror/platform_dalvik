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

public class StatementTest extends TestCase {

    /*
     * Public statics test
     */
    public void testPublicStatics() {

        HashMap<String, Integer> thePublicStatics = new HashMap<String, Integer>();
        thePublicStatics.put("NO_GENERATED_KEYS", new Integer(2));
        thePublicStatics.put("RETURN_GENERATED_KEYS", new Integer(1));
        thePublicStatics.put("EXECUTE_FAILED", new Integer(-3));
        thePublicStatics.put("SUCCESS_NO_INFO", new Integer(-2));
        thePublicStatics.put("CLOSE_ALL_RESULTS", new Integer(3));
        thePublicStatics.put("KEEP_CURRENT_RESULT", new Integer(2));
        thePublicStatics.put("CLOSE_CURRENT_RESULT", new Integer(1));

        /*
         * System.out.println( "NO_GENERATED_KEYS: " +
         * Statement.NO_GENERATED_KEYS ); System.out.println(
         * "RETURN_GENERATED_KEYS: " + Statement.RETURN_GENERATED_KEYS );
         * System.out.println( "EXECUTE_FAILED: " + Statement.EXECUTE_FAILED );
         * System.out.println( "SUCCESS_NO_INFO: " + Statement.SUCCESS_NO_INFO );
         * System.out.println( "CLOSE_ALL_RESULTS: " +
         * Statement.CLOSE_ALL_RESULTS ); System.out.println(
         * "KEEP_CURRENT_RESULT: " + Statement.KEEP_CURRENT_RESULT );
         * System.out.println( "CLOSE_CURRENT_RESULT: " +
         * Statement.CLOSE_CURRENT_RESULT );
         */

        Class<?> statementClass;
        try {
            statementClass = Class.forName("java.sql.Statement");
        } catch (ClassNotFoundException e) {
            fail("java.sql.Statement class not found!");
            return;
        } // end try

        Field[] theFields = statementClass.getDeclaredFields();
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

} // end class StatementTest

