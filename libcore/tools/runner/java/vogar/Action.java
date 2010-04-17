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

/**
 * A named job such as a test or benchmark run. This class tracks the resource
 * files and classes for compiling and running a Java source file.
 */
public final class Action {

    private final String name;
    private final String actionClass;
    private final File resourcesDirectory;
    private final File javaFile;
    private final RunnerSpec runnerSpec;
    private File userDir = new File(System.getProperty("user.dir"));

    public Action(String name, String actionClass, File resourcesDirectory,
            File javaFile, RunnerSpec runnerSpec) {
        this.name = name;
        this.actionClass = actionClass;
        this.resourcesDirectory = resourcesDirectory;
        this.javaFile = javaFile;
        this.runnerSpec = runnerSpec;
    }

    /**
     * Returns the local directory containing this action's required resource
     * files, or {@code null} if this action is standalone.
     */
    public File getResourcesDirectory() {
        return resourcesDirectory;
    }

    /**
     * Returns this action's java file, or {@code null} if this file wasn't
     * built from source.
     */
    public File getJavaFile() {
        return javaFile;
    }

    /**
     * Returns the executable classname, such as java.lang.IntegerTest
     * or BitTwiddle.
     */
    public String getTargetClass() {
        return actionClass;
    }

    /**
     * Returns a unique identifier for this action.
     */
    public String getName() {
        return name;
    }

    public RunnerSpec getRunnerSpec() {
        return runnerSpec;
    }

    /**
     * Initializes the directory from which local files can be read by the
     * action.
     */
    public void setUserDir(File base) {
        this.userDir = base;
    }

    public File getUserDir() {
        return userDir;
    }

    @Override public String toString() {
        return name;
    }
}
