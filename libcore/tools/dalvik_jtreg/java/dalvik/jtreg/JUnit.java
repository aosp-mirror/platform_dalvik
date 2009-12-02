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

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Create {@link TestRun}s for {@code .java} files with JUnit tests in them.
 */
class JUnit {

    private static final Logger logger = Logger.getLogger(JUnit.class.getName());

    public Set<TestRun> findTests(File testDirectory) {
        Set<TestRun> result = new LinkedHashSet<TestRun>();
        findTestsRecursive(result, testDirectory);
        return result;
    }

    private void findTestsRecursive(Set<TestRun> sink, File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                findTestsRecursive(sink, child);
            }
        } else if (file.getName().endsWith(".java")) {
            String className = fileToClass(file);
            File testDirectory = file.getParentFile();
            String testName = "junit"; // TODO: try to get names for each method?
            String testDescription = null;
            sink.add(new TestRun(testDirectory, file, className, className,
                    testName, className, testDescription, JUnitRunner.class));
        } else {
            logger.fine("skipping " + file);
        }
    }

    /**
     * Returns the Java classname for the given file. For example, given the
     * input {@code luni/src/test/java/org/apache/harmony/luni/tests/java/util/ArrayListTest.java},
     * this returns {@code org.apache.harmony.luni.tests.java.util.ArrayListTest}.
     */
    private String fileToClass(File file) {
        String filePath = file.getPath();
        if (!filePath.endsWith(".java")) {
            throw new IllegalArgumentException("Not a .java file: " + file);
        }

        String fqClass = filePath.replaceAll(".*/test/java/", "");
        return fqClass.replace('/', '.').substring(0, fqClass.length() - 5);
    }
}
