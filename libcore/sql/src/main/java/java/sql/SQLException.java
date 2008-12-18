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
 * An {@code Exception} class that is used in conjunction with JDBC operations.
 * It provides information about problems encountered with database access and
 * other problems related to JDBC
 * <p>
 * The {@code SQLException} class provides the following information:
 * <ul>
 * <li>A standard Java exception message, as a {@code String}</li>
 * <li>An {@code SQLState} string. This is an error description string which
 * follows either the SQL 99 conventions or the X/OPEN {@code SQLstate}
 * conventions. The potential values of the {@code SQLState} string are
 * described in each of the specifications. Which of the conventions is being
 * used by the {@code SQLState} string can be discovered by using the {@code
 * getSQLStateType} method of the {@code DatabaseMetaData} interface.</li>
 * <li>An error code, an an integer. The error code is specific to each database
 * vendor and is typically the error code returned by the database itself.</li>
 * <li>A chain to a next {@code Exception}, if relevant, which can give access
 * to additional error information.</li>
 * </ul>
 * </p>
 * 
 * @see DatabaseMetaData
 * 
 * @since Android 1.0
 */
public class SQLException extends Exception implements Serializable {

    private static final long serialVersionUID = 2135244094396331484L;

    private String SQLState = null;

    private int vendorCode = 0;

    private SQLException next = null;

    /**
     * Creates an {@code SQLException} object. The reason string is set to
     * {@code null}, the {@code SQLState} string is set to {@code null} and the
     * error code is set to 0.
     */
    public SQLException() {
        super();
    }

    /**
     * Creates an {@code SQLException} object. The reason string is set to the given
     * reason string, the {@code SQLState} string is set to {@code null} and the error code is
     * set to 0.
     * 
     * @param theReason
     *            the string to use as the Reason string
     */
    public SQLException(String theReason) {
        this(theReason, null, 0);
    }

    /**
     * Creates an {@code SQLException} object. The reason string is set to the
     * given reason string, the {@code SQLState} string is set to the given
     * {@code SQLState} string and the error code is set to 0.
     * 
     * @param theReason
     *            the string to use as the reason string.
     * @param theSQLState
     *            the string to use as the {@code SQLState} string.
     * @since Android 1.0
     */
    public SQLException(String theReason, String theSQLState) {
        this(theReason, theSQLState, 0);
    }

    /**
     * Creates an {@code SQLException} object. The reason string is set to the
     * given reason string, the {@code SQLState} string is set to the given
     * {@code SQLState} string and the error code is set to the given error code
     * value.
     * 
     * @param theReason
     *            the string to use as the reason string.
     * @param theSQLState
     *            the string to use as the {@code SQLState} string.
     * @param theErrorCode
     *            the integer value for the error code.
     * @since Android 1.0
     */
    public SQLException(String theReason, String theSQLState, int theErrorCode) {
        super(theReason);
        SQLState = theSQLState;
        vendorCode = theErrorCode;
    }

    /**
     * Returns the integer error code for this {@code SQLException}.
     * 
     * @return The integer error code for this {@code SQLException}. The meaning
     *         of the code is specific to the vendor of the database.
     * @since Android 1.0
     */
    public int getErrorCode() {
        return vendorCode;
    }

    /**
     * Retrieves the {@code SQLException} chained to this {@code SQLException},
     * if any.
     * 
     * @return The {@code SQLException} chained to this {@code SQLException}.
     *         {@code null} if there is no {@code SQLException} chained to this
     *         {@code SQLException}.
     */
    public SQLException getNextException() {
        return next;
    }

    /**
     * Retrieves the {@code SQLState} description string for this {@code
     * SQLException} object.
     * 
     * @return The {@code SQLState} string for this {@code SQLException} object.
     *         This is an error description string which follows either the SQL
     *         99 conventions or the X/OPEN {@code SQLstate} conventions. The
     *         potential values of the {@code SQLState} string are described in
     *         each of the specifications. Which of the conventions is being
     *         used by the {@code SQLState} string can be discovered by using
     *         the {@code getSQLStateType} method of the {@code
     *         DatabaseMetaData} interface.
     */
    public String getSQLState() {
        return SQLState;
    }

    /**
     * Adds the SQLException to the end of this {@code SQLException} chain.
     * 
     * @param ex
     *            the new {@code SQLException} to be added to the end of the
     *            chain.
     * @since Android 1.0
     */
    public void setNextException(SQLException ex) {    
        if (next != null) {
            next.setNextException(ex);
        } else {
            next = ex;
        }
    }
}
