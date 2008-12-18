package tests.javax.sql;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.PrintWriter;

import javax.sql.ConnectionPoolDataSource;


@TestTargetClass(ConnectionPoolDataSource.class)
public class ConnectionPoolDataSourceTest extends TestCase {

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#getLoginTimeout()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getLoginTimeout",
                                     methodArgs = {ConnectionPoolDataSource.class})
            }
    )
    public void testGetLoginTimeout() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#getLogWriter()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getLogWriter",
                                     methodArgs = {ConnectionPoolDataSource.class})
            }
    )
    public void testGetLogWriter() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#getPooledConnection()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getPooledConnection",
                                     methodArgs = {ConnectionPoolDataSource.class})
            }
    )
    public void testGetPooledConnection() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#getPooledConnection(String, String)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getPooledConnection",
                                     methodArgs = {ConnectionPoolDataSource.class})
            }
    )
    public void testGetPooledConnectionStringString() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#setLoginTimeout(int)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "setLoginTimeout",
                                     methodArgs = {int.class})
            }
    )
    public void testSetLoginTimeout() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @tests {@link javax.sql.ConnectionPoolDataSource#setLogWriter(java.io.PrintWriter)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "setLogWriter",
                                     methodArgs = {PrintWriter.class})
            }
    )
    public void testSetLogWriter() {
        fail("Not yet implemented"); // TODO
    }

}
