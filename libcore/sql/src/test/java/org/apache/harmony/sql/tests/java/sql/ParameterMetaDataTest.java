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

public class ParameterMetaDataTest extends TestCase {

    /*
     * Public statics test
     */
    public void testPublicStatics() {

        HashMap<String, Integer> thePublicStatics = new HashMap<String, Integer>();
        thePublicStatics.put("parameterModeOut", new Integer(4));
        thePublicStatics.put("parameterModeInOut", new Integer(2));
        thePublicStatics.put("parameterModeIn", new Integer(1));
        thePublicStatics.put("parameterModeUnknown", new Integer(0));
        thePublicStatics.put("parameterNullableUnknown", new Integer(2));
        thePublicStatics.put("parameterNullable", new Integer(1));
        thePublicStatics.put("parameterNoNulls", new Integer(0));

        /*
         * System.out.println( "parameterModeOut: " +
         * ParameterMetaData.parameterModeOut ); System.out.println(
         * "parameterModeInOut: " + ParameterMetaData.parameterModeInOut );
         * System.out.println( "parameterModeIn: " +
         * ParameterMetaData.parameterModeIn ); System.out.println(
         * "parameterModeUnknown: " + ParameterMetaData.parameterModeUnknown );
         * System.out.println( "parameterNullableUnknown: " +
         * ParameterMetaData.parameterNullableUnknown ); System.out.println(
         * "parameterNullable: " + ParameterMetaData.parameterNullable );
         * System.out.println( "parameterNoNulls: " +
         * ParameterMetaData.parameterNoNulls );
         */

        Class<?> parameterMetaDataClass;
        try {
            parameterMetaDataClass = Class
                    .forName("java.sql.ParameterMetaData");
        } catch (ClassNotFoundException e) {
            fail("java.sql.ParameterMetaData class not found!");
            return;
        } // end try

        Field[] theFields = parameterMetaDataClass.getDeclaredFields();
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

} // end class ParameterMetaDataTest

