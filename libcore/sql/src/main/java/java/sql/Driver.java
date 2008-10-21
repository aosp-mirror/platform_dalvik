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

import java.util.Properties;

/**
 * An Interface to a JDBC Driver.
 * <p>
 * The JDBC Driver uses URLs to specify the location of specific data. URL
 * format typically takes the form "xxxx:yyyy:SpecificData", where "xxxx:yyyy"
 * is termed the subprotocol and is normally the same for all uses of a
 * particular driver. "SpecificData" is a string which identifies the particular
 * data source that the driver should use.
 * 
 */
public interface Driver {

    /**
     * Returns whether the driver thinks that it can open a connection to the
     * given URL.
     * 
     * @param url
     *            the URL to connect to.
     * @return true if the driver thinks that is can open a connection to the
     *         supplied URL, false otherwise. Typically, the driver will respond
     *         true if it thinks that it can handle the subprotocol specified by
     *         the driver.
     * @throws SQLException
     */
    public boolean acceptsURL(String url) throws SQLException;

    /**
     * Attempts to make a database connection to a datasource specified by a
     * supplied URL.
     * 
     * @param url
     *            the url to connect.
     * @param info
     *            some properties that should be used in establishing the
     *            connection. The properties consist of name/value pairs of
     *            Strings. Normally, a connection to a database requires at
     *            least two properties - for "user" and "password" in order to
     *            pass authentication to the database.
     * @return a Connection object representing the connection to the database.
     * @throws SQLException
     *             if a database error occurs
     */
    public Connection connect(String url, Properties info) throws SQLException;

    /**
     * Gets the driver's major version number.
     * 
     * @return the major version number of the Driver - typically starts at 1.
     */
    public int getMajorVersion();

    /**
     * Gets the driver's minor version number.
     * 
     * @return the minor version number of the Driver - typically starts at 0.
     */
    public int getMinorVersion();

    /**
     * Gets information about possible properties for this driver.
     * <p>
     * This method is intended to provide a listing of possible properties that
     * the user of the driver may need to supply in order to correct connect to
     * a database. Note that the returned array of Properties may change
     * depending on the supplied list of property values.
     * 
     * @param url
     *            the url of the database. A using program may call this method
     *            iteratively as the property list is built up - for example,
     *            when displaying a dialog to an end-user as part of the
     *            database login process.
     * @param info
     * @return an array of DriverPropertyInfo records which provide detail on
     *         each property that the driver will accept.
     * @throws SQLException
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException;

    /**
     * Reports whether this driver is a genuine JDBC CompliantTM driver. The
     * driver may only return true from this method if it passes all the JDBC
     * Compliance tests.
     * <p>
     * A driver may not be fully compliant if the underlying database has
     * limited functionality.
     * 
     * @return true if the driver is fully JDBC compliant, false otherwise.
     */
    public boolean jdbcCompliant();

}
