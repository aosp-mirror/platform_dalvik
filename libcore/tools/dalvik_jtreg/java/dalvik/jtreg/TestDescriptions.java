/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dalvik.jtreg;

import com.sun.javatest.TestDescription;

import java.util.Properties;

/**
 * Utility methods for manipulating {@link TestDescription} instances.
 */
class TestDescriptions {

    /**
     * The subpath of a platform implementation under which tests live. Used to
     * derive relative test paths like {@code /java/io/Reader} from an absolute
     * path like {@code /home/jessewilson/platform_v6/test/java/io/Reader}.
     */
    static final String TEST_ROOT = "/test/";

    /**
     * Returns a properties object for the given test description.
     */
    static Properties toProperties(TestDescription testDescription) {
        Properties result = new Properties();
        result.setProperty(TestRunner.CLASS_NAME, testDescription.getName());
        result.setProperty(TestRunner.QUALIFIED_NAME, qualifiedName(testDescription));
        return result;
    }

    /**
     * Returns a fully qualified name of the form {@code
     * java.lang.Math.PowTests} from the given test description. The returned
     * name is appropriate for use in a filename.
     */
    static String qualifiedName(TestDescription testDescription) {
        return className(testDescription) + "." + escape(testDescription.getName());
    }

    /**
     * Returns the name of the class under test, such as {@code java.lang.Math}.
     */
    static String className(TestDescription testDescription) {
        String dir = testDescription.getDir().toString();
        int separatorIndex = dir.indexOf(TEST_ROOT);
        return separatorIndex != -1
                ? escape(dir.substring(separatorIndex + TEST_ROOT.length()))
                : escape(dir);
    }

    /**
     * Returns a similar string with filename-unsafe characters replaced by
     * filename-safe ones.
     */
    private static String escape(String s) {
        return s.replace('/', '.');
    }

    private TestDescriptions() {}
}
