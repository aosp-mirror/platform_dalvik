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

public class DatabaseMetaDataTest extends TestCase {

    /*
     * Public statics test
     */
    public void testPublicStatics() {

        HashMap<String, Number> thePublicStatics = new HashMap<String, Number>();
        thePublicStatics.put("sqlStateSQL99", new Integer(2));
        thePublicStatics.put("sqlStateXOpen", new Integer(1));
        thePublicStatics.put("attributeNullableUnknown", new Short((short) 2));
        thePublicStatics.put("attributeNullable", new Short((short) 1));
        thePublicStatics.put("attributeNoNulls", new Short((short) 0));
        thePublicStatics.put("tableIndexOther", new Short((short) 3));
        thePublicStatics.put("tableIndexHashed", new Short((short) 2));
        thePublicStatics.put("tableIndexClustered", new Short((short) 1));
        thePublicStatics.put("tableIndexStatistic", new Short((short) 0));
        thePublicStatics.put("typeSearchable", new Integer(3));
        thePublicStatics.put("typePredBasic", new Integer(2));
        thePublicStatics.put("typePredChar", new Integer(1));
        thePublicStatics.put("typePredNone", new Integer(0));
        thePublicStatics.put("typeNullableUnknown", new Integer(2));
        thePublicStatics.put("typeNullable", new Integer(1));
        thePublicStatics.put("typeNoNulls", new Integer(0));
        thePublicStatics.put("importedKeyNotDeferrable", new Integer(7));
        thePublicStatics.put("importedKeyInitiallyImmediate", new Integer(6));
        thePublicStatics.put("importedKeyInitiallyDeferred", new Integer(5));
        thePublicStatics.put("importedKeySetDefault", new Integer(4));
        thePublicStatics.put("importedKeyNoAction", new Integer(3));
        thePublicStatics.put("importedKeySetNull", new Integer(2));
        thePublicStatics.put("importedKeyRestrict", new Integer(1));
        thePublicStatics.put("importedKeyCascade", new Integer(0));
        thePublicStatics.put("versionColumnPseudo", new Integer(2));
        thePublicStatics.put("versionColumnNotPseudo", new Integer(1));
        thePublicStatics.put("versionColumnUnknown", new Integer(0));
        thePublicStatics.put("bestRowPseudo", new Integer(2));
        thePublicStatics.put("bestRowNotPseudo", new Integer(1));
        thePublicStatics.put("bestRowUnknown", new Integer(0));
        thePublicStatics.put("bestRowSession", new Integer(2));
        thePublicStatics.put("bestRowTransaction", new Integer(1));
        thePublicStatics.put("bestRowTemporary", new Integer(0));
        thePublicStatics.put("columnNullableUnknown", new Integer(2));
        thePublicStatics.put("columnNullable", new Integer(1));
        thePublicStatics.put("columnNoNulls", new Integer(0));
        thePublicStatics.put("procedureNullableUnknown", new Integer(2));
        thePublicStatics.put("procedureNullable", new Integer(1));
        thePublicStatics.put("procedureNoNulls", new Integer(0));
        thePublicStatics.put("procedureColumnResult", new Integer(3));
        thePublicStatics.put("procedureColumnReturn", new Integer(5));
        thePublicStatics.put("procedureColumnOut", new Integer(4));
        thePublicStatics.put("procedureColumnInOut", new Integer(2));
        thePublicStatics.put("procedureColumnIn", new Integer(1));
        thePublicStatics.put("procedureColumnUnknown", new Integer(0));
        thePublicStatics.put("procedureReturnsResult", new Integer(2));
        thePublicStatics.put("procedureNoResult", new Integer(1));
        thePublicStatics.put("procedureResultUnknown", new Integer(0));

        Class<?> databaseMetaDataClass;
        try {
            databaseMetaDataClass = Class.forName("java.sql.DatabaseMetaData");
        } catch (ClassNotFoundException e) {
            fail("java.sql.DatabaseMetaData class not found!");
            return;
        } // end try

        Field[] theFields = databaseMetaDataClass.getDeclaredFields();
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

} // end class DatabaseMetaDataTest
