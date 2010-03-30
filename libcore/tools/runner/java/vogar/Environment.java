/*
 * Copyright (C) 2010 The Android Open Source Project
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

package vogar;

import vogar.commands.Rm;

import java.io.File;
import java.util.logging.Logger;

/**
 * A target runtime environment such as a remote device or the local host
 */
abstract class Environment {
    private static final Logger logger = Logger.getLogger(Environment.class.getName());

    final boolean cleanBefore;
    final boolean cleanAfter;
    final Integer debugPort;
    private final File localTemp;

    Environment (boolean cleanBefore, boolean cleanAfter, Integer debugPort, File localTemp) {
        this.cleanBefore = cleanBefore;
        this.cleanAfter = cleanAfter;
        this.debugPort = debugPort;
        this.localTemp = localTemp;
    }

    /**
     * Initializes the temporary directories and test harness necessary to run
     * tests.
     */
    abstract void prepare();

    /**
     * Prepares the directory from which the test will be executed. Some tests
     * expect to read data files from the current working directory; this step
     * should ensure such files are available.
     */
    abstract void prepareUserDir(TestRun testRun);

    /**
     * Deletes files and releases any resources required for the execution of
     * the given test.
     */
    void cleanup(TestRun testRun) {
        if (cleanAfter) {
            logger.fine("clean " + testRun.getQualifiedName());
            new Rm().directoryTree(testCompilationDir(testRun));
            new Rm().directoryTree(testUserDir(testRun));
        }
    }

    final File testDir(String name) {
        return new File(localTemp, name);
    }

    final File testRunnerDir(String name) {
        return new File(testDir("testrunner"), name);
    }

    final File testRunnerClassesDir() {
        return testRunnerDir("classes");
    }

    final File testCompilationDir(TestRun testRun) {
        return new File(localTemp, testRun.getQualifiedName());
    }

    final File testClassesDir(TestRun testRun) {
        return new File(testCompilationDir(testRun), "classes");
    }

    final File testUserDir(TestRun testRun) {
        File testTemp = new File(localTemp, "userDir");
        return new File(testTemp, testRun.getQualifiedName());
    }

    abstract void shutdown();
}
