/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package tests.java.sql;

import SQLite.Database;
import SQLite.Function;
import SQLite.FunctionContext;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import tests.support.Support_SQL;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;


/**
 * Functional test for the Statement.setQueryTimeout() method. Adopted from
 * Apache Derby project (Apache License 2.0).
 * 
 * TODO Test requires transaction isolation to be supported. => Ticket 69
 * 
 * This test consists of four parts: 1. Executes a SELECT
 * query in 4 different threads concurrently. The query calls a user-defined,
 * server-side function which delays the execution, so that it takes several
 * seconds even though the data volume is really low. The fetch operations take
 * longer time than the timeout value set. Hence, this part tests getting
 * timeouts from calls to ResultSet.next(). Two connections are used, two
 * threads execute their statement in the context of one connection, the other
 * two threads in the context of the other connection. Of the 4 threads, only
 * one executes its statement with a timeout value. This way, the test ensures
 * that the correct statement is affected by setQueryTimeout(), regardless of
 * what connection/transaction it and other statements are executed in the
 * context of. 
 * 
 * 2. Executes an INSERT query in multiple threads. This part tests
 * getting timeouts from calls to Statement.execute(). Each thread executes the
 * query in the context of a separate connection. There is no point in executing
 * multiple statements on the same connection; since only one statement per
 * connection executes at a time, there will be no interleaving of execution
 * between them (contrary to the first part of this test, where calls to
 * ResultSet.next() may be interleaved between the different threads). Half of
 * the threads execute their statement with a timeout value set, this is to
 * verify that the correct statements are affected by the timeout, while the
 * other statements execute to completion. 
 * 3. Sets an invalid (negative)
 * timeout. Verifies that the correct exception is thrown. 
 * 4. Tests that the query timeout value is not forgotten after the execution of a statement.
 */
@TestTargetClass(Statement.class)
public class QueryTimeoutTest extends TestCase {

    private static Statement statement;

    private static final int TIMEOUT = 1; // In seconds
    private static final int CONNECTIONS = 100;

    private static Connection[] connections = new Connection[CONNECTIONS];

    private static void printSQLException(SQLException e) {
        while (e != null) {
            e.printStackTrace();
            e = e.getNextException();
        }
    }

    /**
     * This Exception class is used for getting fail-fast behaviour in this
     * test. There is no point in wasting cycles running a test to the end when
     * we know that it has failed. In order to enable chaining of exceptions in
     * J2ME, this class defines its own "cause", duplicating existing
     * functionality in J2SE.
     */
    private static class TestFailedException extends Exception {
        private Throwable cause;

        public TestFailedException(Throwable t) {
            super();
            cause = t;
        }

        public TestFailedException(String message) {
            super(message);
            cause = null;
        }

        public TestFailedException(String message, Throwable t) {
            super(message);
            cause = t;
        }

        public String toString() {
            if (cause != null) {
                return super.toString() + ": " + cause.toString();
            } else {
                return super.toString();
            }
        }

        public void printStackTrace() {
            super.printStackTrace();
            if (cause != null) {
                if (cause instanceof SQLException) {
                    QueryTimeoutTest.printSQLException((SQLException) cause);
                } else {
                    cause.printStackTrace();
                }
            }
        }
    }

    /**
     * Used for executing the SQL statements for setting up this test (the
     * preparation phase). The queries testing setQueryTimeout() are run by the
     * StatementExecutor class.
     */
    private static void exec(Connection connection, String queryString,
            Collection ignoreExceptions) throws TestFailedException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            System.out.println(" Executing "+queryString);
            statement.execute(queryString);
        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            if (!ignoreExceptions.contains(sqlState)) {
                throw new TestFailedException(e); // See finally block below
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ee) {
                    // This will discard an exception possibly thrown above :-(
                    // But we don't worry too much about this, since:
                    // 1. This is just a test
                    // 2. We don't expect close() to throw
                    // 3. If it does, this will be inspected by a developer
                    throw new TestFailedException(ee);
                }
            }
        }
    }

    // Convenience method
    private static void exec(Connection connection, String queryString)
            throws TestFailedException {
        exec(connection, queryString, Collections.EMPTY_SET);
    }

    private static void dropTables(Connection conn, String tablePrefix)
            throws TestFailedException {
        Collection ignore = new HashSet();
        //ignore.add("42Y55");

        exec(conn, "drop table if exists " + tablePrefix + "_orig;", ignore);
        exec(conn, "drop table if exists " + tablePrefix + "_copy;", ignore);
    }

    private static void prepareTables(Connection conn, String tablePrefix)
            throws TestFailedException {
        System.out.println("Initializing tables with prefix " + tablePrefix);

        dropTables(conn, tablePrefix);

        exec(conn, "create table " + tablePrefix + "_orig (a int)");

        exec(conn, "create table " + tablePrefix + "_copy (a int)");
        
        for (int i = 0; i < 7; i++) {
        exec(conn, "insert into " + tablePrefix + "_orig"
                + " values ("+i+");");
        }
    }

    private static String getFetchQuery(String tablePrefix) {
        /**
         * The reason for using the mod function here is to force at least one
         * invocation of ResultSet.next() to read more than one row from the
         * table before returning. This is necessary since timeout is checked
         * only when reading rows from base tables, and when the first row is
         * read, the query still has not exceeded the timeout.
         */
        return "select a from " + tablePrefix
                + "_orig where mod(DELAY(1,a),3)=0";
    }

    private static String getExecQuery(String tablePrefix) {
        return "insert into " + tablePrefix + "_copy select a from "
                + tablePrefix + "_orig where DELAY(1,1)=1";
    }

    private static class StatementExecutor extends Thread {
        private PreparedStatement statement;
        private boolean doFetch;
        private int timeout;
        private SQLException sqlException;
        private String name;
        private long highestRunTime;

        public StatementExecutor(PreparedStatement statement, boolean doFetch,
                int timeout) {
            this.statement = statement;
            this.doFetch = doFetch;
            this.timeout = timeout;
            highestRunTime = 0;
            sqlException = null;
            if (timeout > 0) {
                try {
                    statement.setQueryTimeout(timeout);
                } catch (SQLException e) {
                    sqlException = e;
                }
            }
        }

        private void setHighestRunTime(long runTime) {
            synchronized (this) {
                highestRunTime = runTime;
            }
        }

        public long getHighestRunTime() {
            synchronized (this) {
                return highestRunTime;
            }
        }

        private boolean fetchRow(ResultSet resultSet) throws SQLException {
            long startTime = System.currentTimeMillis();
            boolean hasNext = resultSet.next();
            long endTime = System.currentTimeMillis();
            long runTime = endTime - startTime;
            if (runTime > highestRunTime) setHighestRunTime(runTime);
            return hasNext;
        }

        public void run() {
            if (sqlException != null) return;

            ResultSet resultSet = null;

            try {
                if (doFetch) {
                    long startTime = System.currentTimeMillis();
                    resultSet = statement.executeQuery();
                    long endTime = System.currentTimeMillis();
                    setHighestRunTime(endTime - startTime);
                    while (fetchRow(resultSet)) {
                        yield();
                    }
                } else {
                    long startTime = System.currentTimeMillis();
                    statement.execute();
                    long endTime = System.currentTimeMillis();
                    setHighestRunTime(endTime - startTime);
                }
            } catch (SQLException e) {
                synchronized (this) {
                    sqlException = e;
                }
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException ex) {
                        if (sqlException != null) {
                            System.err.println("Discarding previous exception");
                            sqlException.printStackTrace();
                        }
                        sqlException = ex;
                    }
                }
            }
        }

        public SQLException getSQLException() {
            synchronized (this) {
                return sqlException;
            }
        }
    }

    /**
     * This method compares a thrown SQLException's SQLState value to an
     * expected SQLState. If they do not match, a TestFailedException is thrown
     * with the given message string.
     */
    private static void expectException(String expectSqlState,
            SQLException sqlException, String failMsg)
            throws TestFailedException {
        if (sqlException == null) {
            throw new TestFailedException(failMsg);
        } else {
            String sqlState = sqlException.getSQLState();
            if (!expectSqlState.equals(sqlState)) {
                throw new TestFailedException(sqlException);
            }
        }
    }

    // A convenience method which wraps a SQLException
    private static PreparedStatement prepare(Connection conn, String query)
            throws TestFailedException {
        try {
            return conn.prepareStatement(query);
        } catch (SQLException e) {
            throw new TestFailedException(e);
        }
    }

    /**
     * Part 1 of this test.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Testing timeout with fetch operations",
        method = "setQueryTimeout",
        args = {int.class}
    )
    public static void testTimeoutWithFetch() throws TestFailedException {
        System.out.println("Testing timeout with fetch operations");

        Connection conn1 = connections[0];
        Connection conn2 = connections[1];

        try {
            conn1.setAutoCommit(false);
            conn2.setAutoCommit(false);
        } catch (SQLException e) {
            throw new TestFailedException("Unexpected Exception", e);
        }

        // The idea with these 4 statements is as follows:
        // A - should time out
        // B - different stmt on the same connection; should NOT time out
        // C - different stmt on different connection; should NOT time out
        // D - here just to create equal contention on conn1 and conn2

        PreparedStatement statementA = prepare(conn1, getFetchQuery("t"));
        PreparedStatement statementB = prepare(conn1, getFetchQuery("t"));
        PreparedStatement statementC = prepare(conn2, getFetchQuery("t"));
        PreparedStatement statementD = prepare(conn2, getFetchQuery("t"));

        StatementExecutor[] statementExecutor = new StatementExecutor[4];
        statementExecutor[0] = new StatementExecutor(statementA, true, TIMEOUT);
        statementExecutor[1] = new StatementExecutor(statementB, true, 0);
        statementExecutor[2] = new StatementExecutor(statementC, true, 0);
        statementExecutor[3] = new StatementExecutor(statementD, true, 0);

        for (int i = 3; i >= 0; --i) {
            statementExecutor[i].start();
        }

        for (int i = 0; i < 4; ++i) {
            try {
                statementExecutor[i].join();
            } catch (InterruptedException e) {
                throw new TestFailedException("Should never happen", e);
            }
        }

        /**
         * Actually, there is no guarantee that setting a query timeout for a
         * statement will actually cause a timeout, even if execution of the
         * statement takes longer than the specified timeout. However, these
         * queries execute significantly longer than the specified query
         * timeout. Also, the cancellation mechanism implemented should be quite
         * responsive. In sum, we expect the statement to always time out. If it
         * does not time out, however, we print the highest execution time for
         * the query, as an assistance in determining why it failed. Compare the
         * number to the TIMEOUT constant in this class (note that the TIMEOUT
         * constant is in seconds, while the execution time is in milliseconds).
         */
        expectException("XCL52", statementExecutor[0].getSQLException(),
                "fetch did not time out. Highest execution time: "
                        + statementExecutor[0].getHighestRunTime() + " ms");

        System.out.println("Statement 0 timed out");

        for (int i = 1; i < 4; ++i) {
            SQLException sqlException = statementExecutor[i].getSQLException();
            if (sqlException != null) {
                throw new TestFailedException("Unexpected exception in " + i,
                        sqlException);
            }
            System.out.println("Statement " + i + " completed");
        }

        try {
            statementA.close();
            statementB.close();
            statementC.close();
            statementD.close();
            conn1.commit();
            conn2.commit();
        } catch (SQLException e) {
            throw new TestFailedException(e);
        }
    }

    /**
     * 
     * @test {@link java.sql.Statement#setQueryTimeout(int) }
     * 
     * Part two of this test.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "test timeout with st.exec()",
        method = "setQueryTimeout",
        args = {int.class}
    )
    public static void testTimeoutWithExec()
            throws TestFailedException {
        System.out.println("Testing timeout with an execute operation");

        for (int i = 0; i < connections.length; ++i) {
            try {
                connections[i].setAutoCommit(true);
            } catch (SQLException e) {
                throw new TestFailedException("Unexpected Exception", e);
            }
        }

        PreparedStatement statements[] = new PreparedStatement[connections.length];
        for (int i = 0; i < statements.length; ++i) {
            statements[i] = prepare(connections[i], getExecQuery("t"));
        }

        StatementExecutor[] executors = new StatementExecutor[statements.length];
        for (int i = 0; i < executors.length; ++i) {
            int timeout = (i % 2 == 0) ? TIMEOUT : 0;
            executors[i] = new StatementExecutor(statements[i], false, timeout);
        }

        for (int i = 0; i < executors.length; ++i) {
            executors[i].start();
        }

        for (int i = 0; i < executors.length; ++i) {
            try {
                executors[i].join();
            } catch (InterruptedException e) {
                throw new TestFailedException("Should never happen", e);
            }
        }

        /**
         * Actually, there is no guarantee that setting a query timeout for a
         * statement will actually cause a timeout, even if execution of the
         * statement takes longer than the specified timeout. However, these
         * queries execute significantly longer than the specified query
         * timeout. Also, the cancellation mechanism implemented should be quite
         * responsive. In sum, we expect the statement to always time out. If it
         * does not time out, however, we print the highest execution time for
         * the query, as an assistance in determining why it failed. Compare the
         * number to the TIMEOUT constant in this class (note that the TIMEOUT
         * constant is in seconds, while the execution time is in milliseconds).
         */
        for (int i = 0; i < executors.length; ++i) {
            int timeout = (i % 2 == 0) ? TIMEOUT : 0;
            if (timeout > 0) {
                expectException("XCL52", executors[i].getSQLException(),
                        "exec did not time out. Execution time: "
                                + executors[i].getHighestRunTime() + " ms");
            } else {
                SQLException sqlException = executors[i].getSQLException();
                if (sqlException != null) {
                    throw new TestFailedException(sqlException);
                }
            }
        }

        System.out
                .println("Statements that should time out timed out, and statements that should complete completed");

        for (int i = 0; i < statements.length; ++i) {
            try {
                statements[i].close();
            } catch (SQLException e) {
                throw new TestFailedException(e);
            }
        }
    }

    
    /**
     * 
     * @test {@link java.sql.Statement#setQueryTimeout(int) }
     * 
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Testing setting a negative timeout value",
        method = "setQueryTimeout",
        args = {int.class}
    )
    public static void testInvalidTimeoutValue(Connection conn)
            throws TestFailedException {
        
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TestFailedException("Unexpected Exception", e);
        }

        // Create statement
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("select * from sys.systables");
        } catch (SQLException e) {
            throw new TestFailedException("Unexpected Exception", e);
        }

        // Set (invalid) timeout value - expect exception
        try {
            stmt.setQueryTimeout(-1);
        } catch (SQLException e) {
            expectException("XJ074", e,
                    "negative timeout value should give exception");
        }

        System.out
                .println("Negative timeout value caused exception, as expected");

        // Execute the statement and fetch result
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
            System.out.println("Execute returned a ResultSet");
            rs.close();
        } catch (SQLException e) {
            throw new TestFailedException("Unexpected Exception", e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                // This will discard an exception possibly thrown above :-(
                // But we don't worry too much about this, since:
                // 1. This is just a test
                // 2. We don't expect close() to throw
                // 3. If it does, this will be inspected by a developer
                throw new TestFailedException("close should not throw", e);
            }
        }
    }
    
    /**
     * 
     * @test {@link java.sql.Statement#setQueryTimeout(int) }
     * 
     * Part two of this test.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "timeout with executeUpdate call",
        method = "setQueryTimeout",
        args = {int.class}
    )
    public static void testTimeoutWithExecuteUpdate()
            throws TestFailedException {
        System.out.println("Testing timeout with executeUpdate call.");
        try {
            Statement stmt = connections[0].createStatement();
            stmt.setQueryTimeout(TIMEOUT);
            stmt.executeUpdate(getExecQuery("t"));
        } catch (SQLException sqle) {
            expectException("XCL52", sqle, "Should have timed out.");
        }
    }

    /** Test for DERBY-1692. */
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Testing that Statement considers timeout.",
            method = "setQueryTimeout",
            args = {int.class}
        )
    public static void testRememberTimeoutValue()
            throws TestFailedException {
        String sql = getFetchQuery("t");
        try {
            Statement stmt = connections[0].createStatement();
            statementRemembersTimeout(stmt);
            PreparedStatement ps = connections[0].prepareStatement(sql);
            statementRemembersTimeout(ps);
            CallableStatement cs = connections[0].prepareCall(sql);
            statementRemembersTimeout(cs);
        } catch (SQLException sqle) {
            throw new TestFailedException("Unexpected Exception", sqle);
        }
    }

    public static void statementRemembersTimeout(Statement stmt)
            throws SQLException, TestFailedException {
        System.out.println("Testing that Statement remembers timeout.");
        stmt.setQueryTimeout(1);
        for (int i = 0; i < 3; i++) {
            try {
                ResultSet rs = stmt.executeQuery(getFetchQuery("t"));
                while (rs.next()) {
                    // do nothing
                }
                throw new TestFailedException("Should have timed out.");
            } catch (SQLException sqle) {
                expectException("XCL52", sqle, "Should have timed out.");
            }
        }
        stmt.close();
    }

    private static void statementRemembersTimeout(PreparedStatement ps)
            throws SQLException, TestFailedException {
        String name = (ps instanceof CallableStatement) ? "CallableStatement"
                : "PreparedStatement";
        System.out.println("Testing that " + name + " remembers timeout.");
        ps.setQueryTimeout(1);
        for (int i = 0; i < 3; i++) {
            try {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    // do nothing
                }
                throw new TestFailedException("Should have timed out.");
            } catch (SQLException sqle) {
                expectException("XCL52", sqle, "Should have timed out.");
            }
        }
        ps.close();
    }

    /**
     * A function 
     * arg0 : int seconds
     *
     */
    static class Delay implements SQLite.Function {

        public void function(FunctionContext fc, String[] args) {
            int seconds = new Integer(args[0]).intValue();
            int value = new Integer(args[1]).intValue();
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                // Ignore
            }
            fc.set_result(value);

        }

        public void last_step(FunctionContext fc) {
            // TODO Auto-generated method stub

        }

        public void step(FunctionContext fc, String[] args) {
            // TODO Auto-generated method stub

        }

    }

    /**
     * The actual main bulk of this test. Sets up the environment, prepares
     * tables, runs the tests, and shuts down.
     */
    public static Test suite() {
        
        TestSetup setup = new TestSetup( new TestSuite (QueryTimeoutTest.class)) {
            public void setUp() {
                
                // Establish connections
                Support_SQL.loadDriver();
                try {

                    for (int i = 0; i < connections.length; ++i) {
                        connections[i] = Support_SQL.getConnection();
                    }

                    for (int i = 0; i < connections.length; ++i) {
                        connections[i]
                                .setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                    }
                    
                    // setup Delay function
                    prepare();

                } catch (Throwable e) {
                    fail("Unexpected SQLException " + e.toString());
                }

                System.out.println("Connections set up");
                
            }

            public void tearDown() {
                for (int i = connections.length - 1; i >= 0; --i) {
                    if (connections[i] != null) {
                        try {
                            connections[i].close();
                        } catch (SQLException ex) {
                            printSQLException(ex);
                        }
                    }
                }
                System.out.println("Closed connections");
            }
            
            public void prepare() throws TestFailedException {
                System.out.println("Preparing for testing queries with timeout");
                Database db = new Database();

                Connection conn = connections[0];


                try {
                    db.open(Support_SQL.getFilename(), 1);
                    conn.setAutoCommit(true);
                } catch (Exception e) {
                    throw new TestFailedException("Unexpected Exception", e);
                }

                Function delayFc = new Delay();
                db.create_function("DELAY", 2, delayFc);

                prepareTables(conn, "t");
            }
        };
       
        TestSuite ts = new TestSuite();
        ts.addTestSuite(QueryTimeoutTest.class);

        return setup;
    }

}
