package tests.api.java.io;

import junit.framework.TestCase;

import java.io.ObjectInputValidation;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass; 

@TestTargetClass(
        value = ObjectInputValidation.class,
        untestedMethods = {
            @TestTargetNew(
                    method = "validateObject",
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "There are no classes in the current core " +
                            "libraries that implement this method."
            )
        }
)
public class ObjectInputValidationTest extends TestCase {

}
