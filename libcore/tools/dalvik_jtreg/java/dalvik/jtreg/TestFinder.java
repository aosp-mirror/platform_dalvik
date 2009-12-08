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

/**
 * A pluggable strategy for converting files into test runs.
 */
abstract class TestFinder {

    public Set<TestRun> findTests(File testDirectory) {
        Set<TestRun> result = new LinkedHashSet<TestRun>();
        findTestsRecursive(result, testDirectory);
        return result;
    }

    /**
     * Returns true if {@code file} contains a test class of this type.
     */
    protected boolean matches(File file) {
        return file.getName().endsWith(".java");
    }

    protected abstract String testName(File file);

    protected abstract Class<? extends TestRunner> runnerClass();

    private void findTestsRecursive(Set<TestRun> sink, File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                findTestsRecursive(sink, child);
            }
            return;
        }

        if (!matches(file)) {
            return;
        }

        String className = fileToClass(file);
        File testDirectory = file.getParentFile();
        String testName = testName(file);
        String testDescription = null;
        sink.add(new TestRun(testDirectory, file, className, className,
                testName, className, testDescription, runnerClass()));
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
