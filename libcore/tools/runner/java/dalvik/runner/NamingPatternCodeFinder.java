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

package dalvik.runner;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A code finder that traverses through the directory tree looking for matching
 * naming patterns.
 */
abstract class NamingPatternCodeFinder implements CodeFinder {

    private final String PACKAGE_PATTERN = "(?m)^\\s*package\\s+(\\S+)\\s*;";

    private final String TYPE_DECLARATION_PATTERN
            = "(?m)\\b(?:public|private)\\s+(?:final\\s+)?(?:interface|class|enum)\\b";

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
                testName, className, testDescription,
                getRunnerClass(), getRunnerJava(), getRunnerClasspath()));
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

        // We can get the unqualified class name from the path.
        // It's the last element minus the trailing ".java".
        String filename = file.getName();
        String className = filename.substring(0, filename.length() - 5);

        // For the package, the only foolproof way is to look for the package
        // declaration inside the file.
        try {
            String content = Strings.readFile(file);
            Pattern packagePattern = Pattern.compile(PACKAGE_PATTERN);
            Matcher packageMatcher = packagePattern.matcher(content);
            if (!packageMatcher.find()) {
                // if it doesn't have a package, make sure there's at least a
                // type declaration otherwise we're probably reading the wrong
                // kind of file.
                if (Pattern.compile(TYPE_DECLARATION_PATTERN).matcher(content).find()) {
                    return className;
                }
                throw new IllegalArgumentException("Not a .java file: '" + file + "'\n" + content);
            }
            String packageName = packageMatcher.group(1);
            return packageName + "." + className;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Couldn't read '" + file + "': " + ex.getMessage());
        }
    }
}
