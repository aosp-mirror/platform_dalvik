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
 * A Java representation of the SQL ARRAY type.
 */
public interface Array {

    /**
     * Retrieves the contents of the SQL ARRAY value as a Java array object.
     * 
     * @return A Java array containing the elements of this Array
     * @throws SQLException
     */
    public Object getArray() throws SQLException;

    /**
     * Returns part of the SQL ARRAY associated with this Array, starting at a
     * particular index and comprising up to count successive elements of the
     * SQL array.
     * 
     * @param index
     * @param count
     * @return A Java array containing the subportion of elements of this Array
     * @throws SQLException
     */
    public Object getArray(long index, int count) throws SQLException;

    /**
     * Returns part of the SQL ARRAY associated with this Array, starting at a
     * particular index and comprising up to count successive elements of the
     * SQL array.
     * 
     * @param index
     * @param count
     * @param map
     * @return A Java array containing the subportion of elements of this Array
     * @throws SQLException
     */
    public Object getArray(long index, int count, Map<String, Class<?>> map)
            throws SQLException;

    /**
     * Returns the SQL ARRAY associated with this Array.
     * 
     * @param map
     * @return A Java array containing the elements of this Array
     * @throws SQLException
     */
    public Object getArray(Map<String, Class<?>> map) throws SQLException;

    /**
     * Returns the JDBC type of the entries in this Array's associated array.
     * 
     * @return An integer constant from the java.sql.Types class
     * @throws SQLException
     */
    public int getBaseType() throws SQLException;

    /**
     * Returns the SQL type name of the entries in the array associated with
     * this Array.
     * 
     * @return The database specific name or a fully-qualified SQL type name.
     * @throws SQLException
     */
    public String getBaseTypeName() throws SQLException;

    /**
     * Returns a ResultSet object which holds the entries of the SQL ARRAY
     * associated with this Array.
     * 
     * @return the ResultSet
     * @throws SQLException
     */
    public ResultSet getResultSet() throws SQLException;

    /**
     * Returns a ResultSet object that holds the entries of a subarray,
     * beginning at a particular index and comprising up to count successive
     * entries.
     * 
     * @param index
     * @param count
     * @return the ResultSet
     * @throws SQLException
     */
    public ResultSet getResultSet(long index, int count) throws SQLException;

    /**
     * Returns a ResultSet object that holds the entries of a subarray,
     * beginning at a particular index and comprising up to count successive
     * entries.
     * 
     * @param index
     * @param count
     * @param map
     * @return the ResultSet
     * @throws SQLException
     */
    public ResultSet getResultSet(long index, int count,
            Map<String, Class<?>> map) throws SQLException;

    /**
     * Returns a ResultSet object which holds the entries of the SQL ARRAY
     * associated with this Array.
     * 
     * @param map
     * @return the ResultSet
     * @throws SQLException
     */
    public ResultSet getResultSet(Map<String, Class<?>> map)
            throws SQLException;

}
