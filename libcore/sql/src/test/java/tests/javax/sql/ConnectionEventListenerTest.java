package tests.javax.sql;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

@TestTargetClass(ConnectionEventListener.class)
public class ConnectionEventListenerTest extends TestCase {

    /**
     * @tests {@link javax.sql.ConnectionEventListener#connectionClosed(javax.sql.ConnectionEvent)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "connectionClosed",
        args = {javax.sql.ConnectionEvent.class}
    )
    public void testConnectionClosed() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * @test {@link javax.sql.ConnectionEventListener#connectionErrorOccurred(ConnectionEvent)}
     */
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "connectionErrorOccurred",
        args = {javax.sql.ConnectionEvent.class}
    )
    public void testConnectionErrorOccurred() {
        fail("Not yet implemented"); // TODO
    }

}
