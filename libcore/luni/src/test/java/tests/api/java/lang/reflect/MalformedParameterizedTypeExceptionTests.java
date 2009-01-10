package tests.api.java.lang.reflect;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Modifier;

@TestTargetClass(MalformedParameterizedTypeException.class)
public class MalformedParameterizedTypeExceptionTests  extends junit.framework.TestCase {
    
    /**
     * @tests java.lang.reflect.MalformedParameterizedTypeException#MalformedParameterizedTypeException()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Since this constructor is never invoked, this test only verifies its existence.",
        method = "MalformedParameterizedTypeException",
        args = {}
    )
    public void test_Constructor() throws Exception {
        Constructor<MalformedParameterizedTypeException> ctor = MalformedParameterizedTypeException.class
                .getDeclaredConstructor();
        assertNotNull("Parameterless constructor does not exist.", ctor);
        assertTrue("Constructor is not protected", Modifier.isPublic(ctor
                .getModifiers()));
        assertNotNull(ctor.newInstance());
    }

}
