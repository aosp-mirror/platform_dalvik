package tests.javax.sql;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.DataSource;

@TestTargetClass(DataSource.class)
public class DataSourceTest extends TestCase {

    /**
     * @tests {@link javax.sql.DataSource#getConnection()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getConnetion",
                                     methodArgs = {})
            }
    )
    public void testGetConnection() {
        fail("Not yet implemented");
    }
    
    /**
     * @tests {@link javax.sql.DataSource#getConnection(String, String)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getConnection",
                                     methodArgs = {String.class,String.class})
            }
    )
    public void testGetConnectionStringString() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.DataSource#getLoginTimeout()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getLoginTimeout",
                                     methodArgs = {})
            }
    )
    public void testGetLoginTimeout() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.DataSource#getLogWriter()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getLogWriter",
                                     methodArgs = {})
            }
    )
    public void testGetLogWriter() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.DataSource#setLoginTimeout(int)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "setLoginTimeout",
                                     methodArgs = {int.class})
            }
    )
    public void testSetLoginTimeout() {
        fail("Not yet implemented");
    }

    
    /**
     * @tests {@link javax.sql.DataSource#setLogWriter(java.io.PrintWriter)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "setLoginTimeout",
                                     methodArgs = {int.class})
            }
    )
    public void testSetLogWriter() {
        fail("Not yet implemented");
    }

}
