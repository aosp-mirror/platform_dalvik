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

package java.sql;

import java.util.Map;

/**
 * A manifestation of the SQL REF type - a reference to an SQL type contained in
 * the database.
 * <p>
 * The SQL REF's are held in a table along with SQL structured types. Every REF
 * has an individual identifier for each single instance. The SQL REF is used
 * instead of the structured type it references.
 * <p>
 * A Ref object is stored into the database using the PreparedStatement.setRef
 * method.
 */
public interface Ref {

    /**
     * Gets the fully-qualified SQL name of the SQL structured type that this
     * Ref references.
     * 
     * @return the fully qualified name of the SQL structured type
     * @throws SQLException
     *             if there is a database error
     */
    public String getBaseTypeName() throws SQLException;

    /**
     * Gets the SQL structured type instance referenced by this Ref.
     * 
     * @return a Java object whose type is defined by the mapping for the SQL
     *         structured type.
     * @throws SQLException
     *             if there is a database error
     */
    public Object getObject() throws SQLException;

    /**
     * Returns the associated object and uses the relevant mapping to convert it
     * to a Java type.
     * 
     * @param map
     *            a java.util.Map which contains the mapping to use
     * @return a Java object whose type is defined by the mapping for the SQL
     *         structured type.
     * @throws SQLException
     *             if there is a database error
     */
    public Object getObject(Map<String, Class<?>> map) throws SQLException;

    /**
     * Sets the value of the structured typethat this Ref references to a
     * supplied Object.
     * 
     * @param value
     *            the Object representing the new SQL structured type that this
     *            Ref will reference.
     * @throws SQLException
     *             if there is a database error
     */
    public void setObject(Object value) throws SQLException;
}
