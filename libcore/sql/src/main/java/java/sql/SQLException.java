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
 * An Exception class that is used in conjunction with JDBC operations. It
 * provides information about problems encountered with Database access and
 * other problems related to JDBC
 * <p>
 * The SQLException class provides the following information:
 * <ul>
 * <li>A standard Java exception message, as a String
 * <li>An SQLState string. This is an error description string which follows
 * either the SQL 99 conventions or the XOPEN SQLstate conventions. The
 * potential values of the SQLState string are described in each of the
 * specifications. Which of the conventions is being used by the SQLState string
 * can be discovered by using the getSQLStateType method of the DatabaseMetaData
 * interface.
 * <li>An Error Code, an an integer. The error code is specific to each
 * database vendor and is typically the error code returned by the database
 * itself.
 * <li>A chain to a next Exception, if relevant, which can give access to
 * additional error information.
 * </ul>
 */
public class SQLException extends Exception implements Serializable {

    private static final long serialVersionUID = 2135244094396331484L;

    private String SQLState = null;

    private int vendorCode = 0;

    private SQLException next = null;

    /**
     * Creates an SQLException object. The Reason string is set to null, the
     * SQLState string is set to null and the Error Code is set to 0.
     */
    public SQLException() {
        super();
    }

    /**
     * Creates an SQLException object. The Reason string is set to the given
     * reason string, the SQLState string is set to null and the Error Code is
     * set to 0.
     * 
     * @param theReason
     *            the string to use as the Reason string
     */
    public SQLException(String theReason) {
        this(theReason, null, 0);
    }

    /**
     * Creates an SQLException object. The Reason string is set to the given
     * reason string, the SQLState string is set to the given SQLState string
     * and the Error Code is set to 0.
     * 
     * @param theReason
     *            the string to use as the Reason string
     * @param theSQLState
     *            the string to use as the SQLState string
     */
    public SQLException(String theReason, String theSQLState) {
        this(theReason, theSQLState, 0);
    }

    /**
     * Creates an SQLException object. The Reason string is set to the given
     * reason string, the SQLState string is set to the given SQLState string
     * and the Error Code is set to the given error code value.
     * 
     * @param theReason
     *            the string to use as the Reason string
     * @param theSQLState
     *            the string to use as the SQLState string
     * @param theErrorCode
     *            the integer value for the error code
     */
    public SQLException(String theReason, String theSQLState, int theErrorCode) {
        super(theReason);
        SQLState = theSQLState;
        vendorCode = theErrorCode;
    }

    /**
     * Returns the integer error code for this SQLException
     * 
     * @return The integer error code for this SQLException. The meaning of the
     *         code is specific to the vendor of the database.
     */
    public int getErrorCode() {
        return vendorCode;
    }

    /**
     * Retrieves the SQLException chained to this SQLException, if any.
     * 
     * @return The SQLException chained to this SQLException. null if there is
     *         no SQLException chained to this SQLException.
     */
    public SQLException getNextException() {
        return next;
    }

    /**
     * Retrieves the SQLState description string for this SQLException object
     * 
     * @return The SQLState string for this SQLException object. This is an
     *         error description string which follows either the SQL 99
     *         conventions or the XOPEN SQLstate conventions. The potential
     *         values of the SQLState string are described in each of the
     *         specifications. Which of the conventions is being used by the
     *         SQLState string can be discovered by using the getSQLStateType
     *         method of the DatabaseMetaData interface.
     */
    public String getSQLState() {
        return SQLState;
    }

    /**
     * Adds the SQLException to the end of this SQLException chain.
     * 
     * @param ex
     *            the new SQLException to be added to the end of the chain
     */
    public void setNextException(SQLException ex) {    
        if (next != null) {
            next.setNextException(ex);
        } else {
            next = ex;
        }
    }
}
