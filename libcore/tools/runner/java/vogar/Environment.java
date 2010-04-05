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

import java.io.File;
import java.util.logging.Logger;
import vogar.commands.Rm;

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
     * Initializes the temporary directories and harness necessary to run
     * actions.
     */
    abstract void prepare();

    /**
     * Prepares the directory from which the action will be executed. Some
     * actions expect to read data files from the current working directory;
     * this step should ensure such files are available.
     */
    abstract void prepareUserDir(Action action);

    /**
     * Deletes files and releases any resources required for the execution of
     * the given action.
     */
    void cleanup(Action action) {
        if (cleanAfter) {
            logger.fine("clean " + action.getName());
            new Rm().directoryTree(actionCompilationDir(action));
            new Rm().directoryTree(actionUserDir(action));
        }
    }

    final File actionDir(String name) {
        return new File(localTemp, name);
    }

    final File runnerDir(String name) {
        return new File(actionDir("testrunner"), name);
    }

    final File runnerClassesDir() {
        return runnerDir("classes");
    }

    final File actionCompilationDir(Action action) {
        return new File(localTemp, action.getName());
    }

    final File classesDir(Action action) {
        return new File(actionCompilationDir(action), "classes");
    }

    final File actionUserDir(Action action) {
        File testTemp = new File(localTemp, "userDir");
        return new File(testTemp, action.getName());
    }

    abstract void shutdown();
}
