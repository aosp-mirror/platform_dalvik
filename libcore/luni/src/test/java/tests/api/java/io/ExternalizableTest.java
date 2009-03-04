package tests.api.java.io;

import junit.framework.TestCase;

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass; 

@TestTargetClass(
        value = Externalizable.class,
        untestedMethods = {
            @TestTargetNew(
                    method = "readExternal",
                    args = {ObjectInput.class},
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "There are no classes in the current core " +
                            "libraries that implement this method."
            ),
            @TestTargetNew(
                    method = "writeExternal",
                    args = {ObjectOutput.class},
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "There are no classes in the current core " +
                            "libraries that implement this method."
            )
        }
)
public class ExternalizableTest extends TestCase {

}
