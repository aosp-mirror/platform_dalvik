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

package javax.sql;

import java.sql.SQLException;

/**
 * An interface which provides functionality for a disconnected RowSet to put
 * data updates back to the data source from which the RowSet was originally
 * populated. An object implementing this interface is called a Writer.
 * <p>
 * The Writer must establish a connection to the RowSet's data source before
 * writing the data. The RowSet calling this interface must implement the
 * RowSetInternal interface.
 * <p>
 * The Writer may encounter a situation where the updated data being written
 * back to the data source has already been updated in the data source. How a
 * conflict of this kind is handled is determined by the implementation of the
 * Writer.
 */
public interface RowSetWriter {

    /**
     * Writes changes in the RowSet associated with this RowSetWriter back to
     * its data source.
     * 
     * @param theRowSet
     *            the RowSet object. This RowSet must a) Implement the
     *            RowSetInternal interface and b) have have this RowSetWriter
     *            registered with it and c) must call this method internally
     * @return true if the modified data was written, false otherwise (which
     *         typically implies some form of conflict)
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public boolean writeData(RowSetInternal theRowSet) throws SQLException;
}
