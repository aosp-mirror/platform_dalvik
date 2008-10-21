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

import java.io.Serializable;

/**
 * An exception thrown if a problem occurs during a batch update operation.
 * <p>
 * A BatchUpdateException provides additional information about the problem that
 * occurred, compared with a standard SQLException. It supplies update counts
 * for successful commands that executed within the batch update, but before the
 * exception was encountered.
 * <p>
 * The element order in the array of update counts matches the order that the
 * commands were added to the batch operation.
 * <p>
 * Once a batch update command fails and a BatchUpdateException is thrown, the
 * JDBC driver may continue processing the remaining commands in the batch. If
 * the driver does process more commands after the problem occurs, the array
 * returned by BatchUpdateException.getUpdateCounts has an element for every
 * command in the batch, not only those that executed successfully. In this
 * case, the array element for any command which encountered a problem is set to
 * Statement.EXECUTE_FAILED.
 */
public class BatchUpdateException extends SQLException implements Serializable {

    private static final long serialVersionUID = 5977529877145521757L;

    private int[] updateCounts = null;

    /**
     * Creates a BatchUpdateException with the Reason, SQLState, and Update
     * Counts set to null and a Vendor Code of 0.
     */
    public BatchUpdateException() {
        super();
    }

    /**
     * Creates a BatchUpdateException with the Update Counts set to the supplied
     * value and the Reason, SQLState set to null and a Vendor Code of 0.
     * 
     * @param updateCounts
     *            the array of Update Counts to use in initialization
     */
    public BatchUpdateException(int[] updateCounts) {
        super();
        this.updateCounts = updateCounts;
    }

    /**
     * Creates a BatchUpdateException with the Update Counts set to the supplied
     * value, the Reason set to the supplied value and SQLState set to null and
     * a Vendor Code of 0.
     * 
     * @param reason
     *            the initialization value for Reason
     * @param updateCounts
     *            the array of Update Counts to set
     */
    public BatchUpdateException(String reason, int[] updateCounts) {
        super(reason);
        this.updateCounts = updateCounts;
    }

    /**
     * Creates a BatchUpdateException with the Update Counts set to the supplied
     * value, the Reason set to the supplied value, the SQLState initialized to
     * the supplied value and the Vendor Code initialized to 0.
     * 
     * @param reason
     *            the value to use for the Reason
     * @param SQLState
     *            the X/OPEN value to use for the SQLState
     * @param updateCounts
     *            the array of Update Counts to set
     */
    public BatchUpdateException(String reason, String SQLState,
            int[] updateCounts) {
        super(reason, SQLState);
        this.updateCounts = updateCounts;
    }

    /**
     * Creates a BatchUpdateException with the Update Counts set to the supplied
     * value, the Reason set to the supplied value, the SQLState initialized to
     * the supplied value and the Vendor Code set to the supplied value.
     * 
     * @param reason
     *            the value to use for the Reason
     * @param SQLState
     *            the X/OPEN value to use for the SQLState
     * @param vendorCode
     *            the value to use for the vendor error code
     * @param updateCounts
     *            the array of Update Counts to set
     */
    public BatchUpdateException(String reason, String SQLState, int vendorCode,
            int[] updateCounts) {
        super(reason, SQLState, vendorCode);
        this.updateCounts = updateCounts;
    }

    /**
     * Gets the Update Counts array.
     * <p>
     * If a batch update command fails and a BatchUpdateException is thrown, the
     * JDBC driver may continue processing the remaining commands in the batch.
     * If the driver does process more commands after the problem occurs, the
     * array returned by <code>BatchUpdateException.getUpdateCounts</code> has
     * an element for every command in the batch, not only those that executed
     * successfully. In this case, the array element for any command which
     * encountered a problem is set to Statement.EXECUTE_FAILED.
     * 
     * @return an array that contains the successful update counts, before this
     *         exception. Alternatively, if the driver continues to process
     *         commands following an error, one of these listed items for every
     *         command the batch contains:
     *         <ol>
     *         <li>an count of the updates</li>
     *         <li><code>Statement.SUCCESS_NO_INFO</code> indicating that the
     *         command completed successfully, but the amount of altered rows is
     *         not known.</li>
     *         <li><code>Statement.EXECUTE_FAILED</code> indicating that the
     *         command was unsuccessful.
     *         </ol>
     */
    public int[] getUpdateCounts() {
        return updateCounts;
    }
}
