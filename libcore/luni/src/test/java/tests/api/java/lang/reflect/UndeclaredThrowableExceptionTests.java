package tests.api.java.lang.reflect;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.EOFException;
import java.lang.reflect.UndeclaredThrowableException;

@TestTargetClass(UndeclaredThrowableException.class)
public class UndeclaredThrowableExceptionTests extends TestCase {
    
    private static EOFException throwable = new EOFException();
    private static String msg = "TEST_MSG";
    /**
     * @tests java.lang.reflect.UndeclaredThrowableException#getCause()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCause",
        args = {}
    )
    public void test_getCause() throws Exception {
        UndeclaredThrowableException ute = new UndeclaredThrowableException(
                throwable);
        assertSame("Wrong cause returned", throwable, ute.getCause());
    }
    
    /**
     * @tests java.lang.reflect.UndeclaredThrowableException#getUndeclaredThrowable()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getUndeclaredThrowable",
        args = {}
    )
    public void test_getUndeclaredThrowable() throws Exception {
        UndeclaredThrowableException ute = new UndeclaredThrowableException(
                throwable);
        assertSame("Wrong undeclared throwable returned", throwable, ute
                .getUndeclaredThrowable());
    }
    
    /**
     * @tests java.lang.reflect.UndeclaredThrowableException#UndeclaredThrowableException(java.lang.Throwable)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UndeclaredThrowableException",
        args = {java.lang.Throwable.class}
    )
    public void test_Constructor_Throwable() throws Exception {
        UndeclaredThrowableException e = new UndeclaredThrowableException(
                throwable);
        assertEquals("Wrong cause returned", throwable, e.getCause());
        assertEquals("Wrong throwable returned", throwable, e
                .getUndeclaredThrowable());
    }
    
    /**
     * @tests java.lang.reflect.UndeclaredThrowableException#UndeclaredThrowableException(java.lang.Throwable, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UndeclaredThrowableException",
        args = {java.lang.Throwable.class, java.lang.String.class}
    )
    public void test_Constructor_Throwable_String() throws Exception {
       UndeclaredThrowableException e = new UndeclaredThrowableException(
                throwable, msg);
        assertEquals("Wrong cause returned", throwable, e.getCause());
        assertEquals("Wrong throwable returned", throwable, e
                .getUndeclaredThrowable());
        assertEquals("Wrong message returned", msg, e.getMessage());
    }
}
