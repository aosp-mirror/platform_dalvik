package tests.javax.sql;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.ConnectionEventListener;

@TestTargetClass(PooledConnectionTest.class)
public class PooledConnectionTest extends TestCase {

    /**
     * @tests {@link javax.sql.PooledConnection#addConnectionEventListener(javax.sql.ConnectionEventListener)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "addConnectionEventListener",
                                   methodArgs = {ConnectionEventListener.class})
            }
    )
    public void testAddConnectionEventListener() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.PooledConnection#close()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "close",
                                   methodArgs = {})
            }
    )
    public void testClose() {
        fail("Not yet implemented");
    }

    /**
     * @tests {@link javax.sql.PooledConnection#getConnection()}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getConnection",
                                   methodArgs = {})
            }
    )
    public void testGetConnection() {
        fail("Not yet implemented");
    }

    
    /**
     * @tests {@link javax.sql.PooledConnection#removeConnectionEventListener(ConnectionEventListener)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "",
            targets = {@TestTarget(methodName = "getConnection",
                                   methodArgs = {ConnectionEventListener.class})
            }
    )
    public void testRemoveConnectionEventListener() {
        fail("Not yet implemented");
    }

}
