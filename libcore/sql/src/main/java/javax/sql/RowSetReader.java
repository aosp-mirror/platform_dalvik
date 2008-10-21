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
 * An interface which provides functionality for a disconnected RowSet to get
 * data from a data source into its rows. The RowSet calls the RowSetReader
 * interface when the RowSet's execute method is invoked - a RowSetReader must
 * first be registered with the RowSet for this to work.
 */
public interface RowSetReader {

    /**
     * Reads new data into the RowSet. The calling RowSet object must itself
     * implement the RowSetInternal interface and the RowSetReader must be
     * registered as a Reader on the RowSet.
     * <p>
     * This method adds rows into the calling RowSet. The Reader may invoke any
     * of the RowSet's methods except for the <code>execute</code> method
     * (calling execute will cause an SQLException to be thrown). However, when
     * the Reader calls the RowSet's methods, no events are sent to listeners -
     * any listeners are informed by the calling RowSet's execute method once
     * the Reader returns from the readData method.
     * 
     * @param theCaller
     *            must be the calling RowSet object, which must have implemented
     *            the RowSetInternal interface.
     * @throws SQLException
     *             if a problem occurs accessing the database or if the Reader
     *             calls the RowSet.execute method.
     */
    public void readData(RowSetInternal theCaller) throws SQLException;
}
