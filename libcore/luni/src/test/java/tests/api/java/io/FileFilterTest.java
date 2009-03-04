package tests.api.java.io;

import java.io.File;
import java.io.FileFilter;

import junit.framework.TestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(
        value = FileFilter.class,
        untestedMethods = {
            @TestTargetNew(
                    method = "accept",
                    args = {File.class},
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "There are no classes in the current core " +
                            "libraries that implement this method."
            )
        }
)
public class FileFilterTest extends TestCase {

}
