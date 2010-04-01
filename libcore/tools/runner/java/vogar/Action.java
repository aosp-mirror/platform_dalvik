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

import vogar.target.Runner;

import java.io.File;

/**
 * A named job such as a test or benchmark run. This class tracks the resource
 * files and classes for compiling and running a Java source file.
 */
public final class Action {

    private final String name;
    private final String actionClass;
    private final File actionDirectory;
    private final File actionJava;
    private final String description;
    private final Class<? extends Runner> runnerClass;
    private final File runnerJava;
    private final Classpath runnerClasspath;
    private File userDir = new File(System.getProperty("user.dir"));

    public Action(String name, String actionClass, File actionDirectory,
            File actionJava, String description, Class<? extends Runner> runnerClass,
            File runnerJava, Classpath runnerClasspath) {
        this.name = name;
        this.actionClass = actionClass;
        this.actionDirectory = actionDirectory;
        this.actionJava = actionJava;
        this.description = description;
        this.runnerClass = runnerClass;
        this.runnerJava = runnerJava;
        this.runnerClasspath = runnerClasspath;
    }

    /**
     * Returns the local directory containing this action's java file.
     */
    public File getJavaDirectory() {
        return actionDirectory;
    }

    public File getJavaFile() {
        return actionJava;
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

    /**
     * Returns an English description of this action, or null if no such
     * description is known.
     */
    public String getDescription() {
        return description;
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

    public Class<? extends Runner> getRunnerClass() {
        return runnerClass;
    }

    public File getRunnerJava() {
        return runnerJava;
    }

    public Classpath getRunnerClasspath() {
        return runnerClasspath;
    }

    @Override public String toString() {
        return name;
    }
}
