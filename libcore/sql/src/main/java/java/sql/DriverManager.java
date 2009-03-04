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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Vector;
import org.apache.harmony.sql.internal.nls.Messages;
// BEGIN android-changed
import dalvik.system.VMStack;
// END android-changed

/**
 * Provides facilities for managing JDBC drivers.
 * <p>
 * The {@code DriverManager} class loads JDBC drivers during its initialization,
 * from the list of drivers referenced by the system property {@code
 * "jdbc.drivers"}.
 * </p>
 *  
 * @since Android 1.0
 */
public class DriverManager {

    /*
     * Facilities for logging. The Print Stream is deprecated but is maintained
     * here for compatibility.
     */
    private static PrintStream thePrintStream;

    private static PrintWriter thePrintWriter;

    // Login timeout value - by default set to 0 -> "wait forever"
    private static int loginTimeout = 0;

    /*
     * Set to hold Registered Drivers - initial capacity 10 drivers (will expand
     * automatically if necessary.
     */
    private static final Set<Driver> theDriverSet = new HashSet<Driver>(10);

    // Permission for setting log
    private static final SQLPermission logPermission = new SQLPermission("setLog"); //$NON-NLS-1$

    /*
     * Load drivers on initialization
     */
    static {
        loadInitialDrivers();
    }

    /*
     * Loads the set of JDBC drivers defined by the Property "jdbc.drivers" if
     * it is defined.
     */
    private static void loadInitialDrivers() {
        String theDriverList = System.getProperty("jdbc.drivers", null); //$NON-NLS-1$
        if (theDriverList == null) {
            return;
        }

        /*
         * Get the names of the drivers as an array of Strings from the system
         * property by splitting the property at the separator character ':'
         */
        String[] theDriverNames = theDriverList.split(":"); //$NON-NLS-1$

        for (String element : theDriverNames) {
            try {
                // Load the driver class
                Class
                        .forName(element, true, ClassLoader
                                .getSystemClassLoader());
            } catch (Throwable t) {
                // Ignored
            }
        }
    }

    /*
     * A private constructor to prevent allocation
     */
    private DriverManager() {
        super();
    }

    /**
     * Removes a driver from the {@code DriverManager}'s registered driver list.
     * This will only succeed when the caller's class loader loaded the driver
     * that is to be removed. If the driver was loaded by a different class
     * loader, the removal of the driver fails silently.
     * <p>
     * If the removal succeeds, the {@code DriverManager} will not use this
     * driver in the future when asked to get a {@code Connection}.
     * </p>
     * 
     * @param driver
     *            the JDBC driver to remove.
     * @throws SQLException
     *             if there is a problem interfering with accessing the
     *             database.
     * @since Android 1.0
     */
    public static void deregisterDriver(Driver driver) throws SQLException {
        if (driver == null) {
            return;
        }
        // BEGIN android-changed
        ClassLoader callerClassLoader = VMStack.getCallingClassLoader();
        // END android-changed

        if (!DriverManager.isClassFromClassLoader(driver, callerClassLoader)) {
            // sql.1=DriverManager: calling class not authorized to deregister JDBC driver
            throw new SecurityException(Messages.getString("sql.1")); //$NON-NLS-1$
        } // end if
        synchronized (theDriverSet) {
            theDriverSet.remove(driver);
        }
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * 
     * @param url
     *            a URL string representing the database target to connect with.
     * @return a {@code Connection} to the database identified by the URL.
     *         {@code null} if no connection can be established.
     * @throws SQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL.
     * @since Android 1.0
     */
    public static Connection getConnection(String url) throws SQLException {
        return getConnection(url, new Properties());
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * 
     * @param url
     *            a URL string representing the database target to connect with
     * @param info
     *            a set of properties to use as arguments to set up the
     *            connection. Properties are arbitrary string/value pairs.
     *            Normally, at least the properties {@code "user"} and {@code
     *            "password"} should be passed, with appropriate settings for
     *            the user ID and its corresponding password to get access to
     *            the corresponding database.
     * @return a {@code Connection} to the database identified by the URL.
     *         {@code null} if no connection can be established.
     * @throws SQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL.
     * @since Android 1.0
     */
    public static Connection getConnection(String url, Properties info)
            throws SQLException {
        // 08 - connection exception
        // 001 - SQL-client unable to establish SQL-connection
        String sqlState = "08001"; //$NON-NLS-1$
        if (url == null) {
            // sql.5=The url cannot be null
            throw new SQLException(Messages.getString("sql.5"), sqlState); //$NON-NLS-1$
        }
        synchronized (theDriverSet) {
            /*
             * Loop over the drivers in the DriverSet checking to see if one can
             * open a connection to the supplied URL - return the first
             * connection which is returned
             */
            for (Driver theDriver : theDriverSet) {
                Connection theConnection = theDriver.connect(url, info);
                if (theConnection != null) {
                    return theConnection;
                }
            }
        }
        // If we get here, none of the drivers are able to resolve the URL
        // sql.6=No suitable driver
        throw new SQLException(Messages.getString("sql.6"), sqlState); //$NON-NLS-1$ 
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * 
     * @param url
     *            a URL string representing the database target to connect with.
     * @param user
     *            a user ID used to login to the database.
     * @param password
     *            a password for the user ID to login to the database.
     * @return a {@code Connection} to the database identified by the URL.
     *         {@code null} if no connection can be established.
     * @throws SQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL.
     * @since Android 1.0
     */
    public static Connection getConnection(String url, String user,
            String password) throws SQLException {
        Properties theProperties = new Properties();
        if(null != user){
            theProperties.setProperty("user", user); //$NON-NLS-1$
        }
        if(null != password){
            theProperties.setProperty("password", password); //$NON-NLS-1$
        }
        return getConnection(url, theProperties);
    }

    /**
     * Tries to find a driver that can interpret the supplied URL.
     * 
     * @param url
     *            the URL of a database.
     * @return a {@code Driver} that matches the provided URL. {@code null} if
     *         no {@code Driver} understands the URL
     * @throws SQLException
     *             if there is any kind of problem accessing the database.
     */
    public static Driver getDriver(String url) throws SQLException {
        // BEGIN android-changed
        ClassLoader callerClassLoader = VMStack.getCallingClassLoader();
        // END android-changed

        synchronized (theDriverSet) {
            /*
             * Loop over the drivers in the DriverSet checking to see if one
             * does understand the supplied URL - return the first driver which
             * does understand the URL
             */
            Iterator<Driver> theIterator = theDriverSet.iterator();
            while (theIterator.hasNext()) {
                Driver theDriver = theIterator.next();
                if (theDriver.acceptsURL(url)
                        && DriverManager.isClassFromClassLoader(theDriver,
                                callerClassLoader)) {
                    return theDriver;
                }
            }
        }
        // If no drivers understand the URL, throw an SQLException
        // sql.6=No suitable driver
        //SQLState: 08 - connection exception
        //001 - SQL-client unable to establish SQL-connection
        throw new SQLException(Messages.getString("sql.6"), "08001"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns an {@code Enumeration} that contains all of the loaded JDBC
     * drivers that the current caller can access.
     * 
     * @return An {@code Enumeration} containing all the currently loaded JDBC
     *         {@code Drivers}.
     * @since Android 1.0
     */
    public static Enumeration<Driver> getDrivers() {
        // BEGIN android-changed
        ClassLoader callerClassLoader = VMStack.getCallingClassLoader();
        // END android-changed
        /*
         * Synchronize to avoid clashes with additions and removals of drivers
         * in the DriverSet
         */
        synchronized (theDriverSet) {
            /*
             * Create the Enumeration by building a Vector from the elements of
             * the DriverSet
             */
            Vector<Driver> theVector = new Vector<Driver>();
            Iterator<Driver> theIterator = theDriverSet.iterator();
            while (theIterator.hasNext()) {
                Driver theDriver = theIterator.next();
                if (DriverManager.isClassFromClassLoader(theDriver,
                        callerClassLoader)) {
                    theVector.add(theDriver);
                }
            }
            return theVector.elements();
        }
    }

    /**
     * Returns the login timeout when connecting to a database in seconds.
     * 
     * @return the login timeout in seconds.
     * @since Android 1.0
     */
    public static int getLoginTimeout() {
        return loginTimeout;
    }

    /**
     * Gets the log {@code PrintStream} used by the {@code DriverManager} and
     * all the JDBC Drivers.
     * 
     * @deprecated use {@link #getLogWriter()} instead.
     * @return the {@code PrintStream} used for logging activities.
     * @since Android 1.0
     */
    @Deprecated
    public static PrintStream getLogStream() {
        return thePrintStream;
    }

    /**
     * Retrieves the log writer.
     * 
     * @return A {@code PrintWriter} object used as the log writer. {@code null}
     *         if no log writer is set.
     * @since Android 1.0
     */
    public static PrintWriter getLogWriter() {
        return thePrintWriter;
    }

    /**
     * Prints a message to the current JDBC log stream. This is either the
     * {@code PrintWriter} or (deprecated) the {@code PrintStream}, if set.
     * 
     * @param message
     *            the message to print to the JDBC log stream.
     * @since Android 1.0
     */
    public static void println(String message) {
        if (thePrintWriter != null) {
            thePrintWriter.println(message);
            thePrintWriter.flush();
        } else if (thePrintStream != null) {
            thePrintStream.println(message);
            thePrintStream.flush();
        }
        /*
         * If neither the PrintWriter not the PrintStream are set, then silently
         * do nothing the message is not recorded and no exception is generated.
         */
        return;
    }

    /**
     * Registers a given JDBC driver with the {@code DriverManager}.
     * <p>
     * A newly loaded JDBC driver class should register itself with the
     * {@code DriverManager} by calling this method.
     * </p>
     * 
     * @param driver
     *            the {@code Driver} to register with the {@code DriverManager}.
     * @throws SQLException
     *             if a database access error occurs.
     */
    public static void registerDriver(Driver driver) throws SQLException {
        if (driver == null) {
            throw new NullPointerException();
        }
        synchronized (theDriverSet) {
            theDriverSet.add(driver);
        }
    }

    /**
     * Sets the login timeout when connecting to a database in seconds.
     * 
     * @param seconds
     *            seconds until timeout. 0 indicates wait forever.
     * @since Android 1.0
     */
    public static void setLoginTimeout(int seconds) {
        loginTimeout = seconds;
        return;
    }

    /**
     * Sets the print stream to use for logging data from the {@code
     * DriverManager} and the JDBC drivers.
     * 
     * @deprecated Use {@link #setLogWriter} instead.
     * @param out
     *            the {@code PrintStream} to use for logging.
     * @since Android 1.0
     */
    @Deprecated
    public static void setLogStream(PrintStream out) {
        checkLogSecurity();
        thePrintStream = out;
    }

    /**
     * Sets the {@code PrintWriter} that is used by all loaded drivers, and also
     * the {@code DriverManager}.
     * 
     * @param out
     *            the {@code PrintWriter} to be used.
     * @since Android 1.0
     */
    public static void setLogWriter(PrintWriter out) {
        checkLogSecurity();
        thePrintWriter = out;
    }

    /*
     * Method which checks to see if setting a logging stream is allowed by the
     * Security manager
     */
    private static void checkLogSecurity() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            // Throws a SecurityException if setting the log is not permitted
            securityManager.checkPermission(logPermission);
        }
    }

    /**
     * Determines whether the supplied object was loaded by the given {@code ClassLoader}.
     * 
     * @param theObject
     *            the object to check.
     * @param theClassLoader
     *            the {@code ClassLoader}.
     * @return {@code true} if the Object does belong to the {@code ClassLoader}
     *         , {@code false} otherwise
     */
    private static boolean isClassFromClassLoader(Object theObject,
            ClassLoader theClassLoader) {
    
        if ((theObject == null) || (theClassLoader == null)) {
            return false;
        }
    
        Class<?> objectClass = theObject.getClass();
    
        try {
            Class<?> checkClass = Class.forName(objectClass.getName(), true,
                    theClassLoader);
            if (checkClass == objectClass) {
                return true;
            }
        } catch (Throwable t) {
            // Empty
        }
        return false;
    }
}
