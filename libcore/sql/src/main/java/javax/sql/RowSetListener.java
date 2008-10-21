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

import java.util.EventListener;

/**
 * An interface used to send notification of events occurring in a RowSet. To
 * receive the notification events, an object must implement the RowSetListener
 * interface and then register itself with the RowSet of interest using the
 * <code>RowSet.addRowSetListener</code> method.
 */
public interface RowSetListener extends EventListener {

    /**
     * Notifies the listener that one of the RowSet's rows has changed.
     * 
     * @param theEvent
     *            a RowSetEvent that contains information about the RowSet
     *            involved. This information can be used to retrieve information
     *            about the change, such as the new cursor position.
     */
    public void cursorMoved(RowSetEvent theEvent);

    /**
     * Notifies the listener that the RowSet's cursor has moved.
     * 
     * @param theEvent
     *            theEvent a RowSetEvent that contains information about the
     *            RowSet involved. This information can be used to retrieve
     *            information about the change, such as the updated data values.
     */
    public void rowChanged(RowSetEvent theEvent);

    /**
     * Notifies the listener that the RowSet's entire contents have been updated
     * (an example is the execution of a command which retrieves new data from
     * the database).
     * 
     * @param theEvent
     *            theEvent a RowSetEvent that contains information about the
     *            RowSet involved. This information can be used to retrieve
     *            information about the change, such as the updated rows of
     *            data.
     */
    public void rowSetChanged(RowSetEvent theEvent);
}
