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
import org.apache.harmony.kernel.vm.VM;

/**
 * Provides facilities for managing JDBC Drivers.
 * <p>
 * The DriverManager class will load JDBC drivers during its initialization,
 * from the list of drivers referenced by the System Property "jdbc.drivers".
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
     * Removes a driver from the DriverManager's registered driver list. This
     * will only succeed where the caller's classloader loaded the driver that
     * is to be removed. If the driver was loaded by a different classloader,
     * the removal of the driver will fail silently.
     * <p>
     * If the removal succeeds, the DriverManager will not in future use this
     * driver when asked to get a Connection.
     * 
     * @param driver
     * @throws SQLException
     *             if there is an exception accessing the database.
     */
    public static void deregisterDriver(Driver driver) throws SQLException {
        if (driver == null) {
            return;
        }
//        ClassLoader callerClassLoader = VM.callerClassLoader(); //???SQL VM.callerClassLoader not implemented -> null
//
//        if (!DriverManager.isClassFromClassLoader(driver, callerClassLoader)) { 
//            // sql.1=DriverManager: calling class not authorized to deregister JDBC driver
//            throw new SecurityException(Messages.getString("sql.1")); //$NON-NLS-1$
//        } // end if
        synchronized (theDriverSet) {
            theDriverSet.remove(driver);
        }
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * 
     * @param url
     *            a URL string representing the database target to connect with
     * @return a Connection to the database identified by the URL. null if no
     *         connection can be made.
     * @throws SQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL
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
     *            a set of Properties to use as arguments to set up the
     *            connection. Properties are arbitrary string/value pairs.
     *            Normally, at least the properties "user" and "password" should
     *            be passed, with appropriate settings for the userid and its
     *            corresponding password to get access to the database
     *            concerned.
     * @return a Connection to the database identified by the URL. null if no
     *         connection can be made.
     * @throws SQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL
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
     *            a URL string representing the database target to connect with
     * @param user
     *            a userid used to login to the database
     * @param password
     *            a password for the userid to login to the database
     * @return a Connection to the database identified by the URL. null if no
     *         connection can be made.
     * @throws SQLException
     *             if there is an error while attempting to connect to the
     *             database identified by the URL
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
     *            the URL of a database
     * @return a Driver that can understand the given URL. null if no Driver
     *         understands the URL
     * @throws SQLException
     *             if there is any kind of Database Access problem
     */
    public static Driver getDriver(String url) throws SQLException {
//        ClassLoader callerClassLoader = VM.callerClassLoader(); //???SQL VM.callerClassLoader not implemented -> null

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
//                        && DriverManager.isClassFromClassLoader(theDriver,
//                                callerClassLoader)) { //???SQL VM.callerClassLoader not implemented -> null
                        ){
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
     * Returns an Enumeration that contains all of the loaded JDBC drivers that
     * the current caller can access.
     * 
     * @return An Enumeration containing all the currently loaded JDBC Drivers
     */
    public static Enumeration<Driver> getDrivers() {
//        ClassLoader callerClassLoader = VM.callerClassLoader(); //???SQL VM.callerClassLoader not implemented -> null
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
//                if (DriverManager.isClassFromClassLoader(theDriver,  //???SQL VM.callerClassLoader not implemented -> null
//                        callerClassLoader)) {
                    theVector.add(theDriver);
//                }
            }
            return theVector.elements();
        }
    }

    /**
     * Returns the login timeout when connecting to a database, in seconds.
     * 
     * @return the login timeout in seconds
     */
    public static int getLoginTimeout() {
        return loginTimeout;
    }

    /**
     * @deprecated Gets the log PrintStream used by the DriverManager and all
     *             the JDBC Drivers.
     * @return the PrintStream used for logging activity
     */
    @Deprecated
    public static PrintStream getLogStream() {
        return thePrintStream;
    }

    /**
     * Retrieves the log writer.
     * 
     * @return A PrintWriter object used as the log writer. null if no log
     *         writer is set.
     */
    public static PrintWriter getLogWriter() {
        return thePrintWriter;
    }

    /**
     * Prints a message to the current JDBC log stream. This is either the
     * PrintWriter or (deprecated) the PrintStream, if set.
     * 
     * @param message
     *            the message to print to the JDBC log stream
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
     * Registers a given JDBC driver with the DriverManager.
     * <p>
     * A newly loaded JDBC driver class should register itself with the
     * DriverManager by calling this method.
     * 
     * @param driver
     *            the Driver to register with the DriverManager
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
     * Set the login timeout when connecting to a database, in seconds.
     * 
     * @param seconds
     *            seconds until timeout. 0 indicates wait forever.
     */
    public static void setLoginTimeout(int seconds) {
        loginTimeout = seconds;
        return;
    }

    /**
     * @deprecated Sets the Print Stream to use for logging data from the
     *             DriverManager and the JDBC drivers.
     *             <p>
     *             Use {@link #setLogWriter} instead.
     * @param out
     *            the PrintStream to use for logging.
     */
    @Deprecated
    public static void setLogStream(PrintStream out) {
        checkLogSecurity();
        thePrintStream = out;
    }

    /**
     * Sets the PrintWriter that will be used by all loaded drivers, and also
     * the DriverManager.
     * 
     * @param out
     *            the PrintWriter to be used
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
     * Finds if a supplied Object belongs to the given ClassLoader.
     * 
     * @param theObject
     *            the object to check
     * @param theClassLoader
     *            the ClassLoader
     * @return true if the Object does belong to the ClassLoader, false
     *         otherwise
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
