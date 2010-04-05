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

package vogar;

import java.io.File;
import java.util.Set;
import vogar.target.Runner;

/**
 * A strategy for finding runnable things in a directory.
 */
public interface CodeFinder {

    /**
     * Returns all actions in the given file or directory. If the returned set
     * is empty, no executable code of this kind were found.
     */
    public Set<Action> findActions(File file);

    /**
     * Return the class for the TestRunner
     */
    public Class<? extends Runner> getRunnerClass();

    /**
     * Return the Java file for the TestRunner
     */
    public File getRunnerJava();

    /**
     * Return the compile classpath for the TestRunner
     */
    public Classpath getRunnerClasspath();
}
