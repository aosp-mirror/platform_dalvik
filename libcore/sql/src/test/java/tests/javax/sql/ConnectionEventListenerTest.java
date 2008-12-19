package tests.javax.sql;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

@TestTargetClass(ConnectionEventListener.class)
public class ConnectionEventListenerTest extends TestCase {

    /**
     * @tests {@link javax.sql.ConnectionEventListener#connectionClosed(javax.sql.ConnectionEvent)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "Verifies that the listener is notified when a connection closed regularly.",
            targets = { @TestTarget(methodName = "testConnectionEventPooledConnection", 
                                    methodArgs = {ConnectionEvent.class})                         
            }
    ) 
    
    public void testConnectionClosed() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @test {@link javax.sql.ConnectionEventListener#connectionErrorOccurred(ConnectionEvent)}
     */
    @TestInfo(
            level = TestLevel.TODO,
            purpose = "Verifies that the listener is notified when a connection is unavailable because an error has occurred.",
            targets = { @TestTarget(methodName = "testConnectionEventPooledConnection", 
                                    methodArgs = {ConnectionEvent.class})                         
            }
    )    
    public void testConnectionErrorOccurred() {
        fail("Not yet implemented"); // TODO
    }

}
