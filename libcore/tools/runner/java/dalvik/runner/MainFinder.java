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

/**
 * Create {@link TestRun}s for {@code .java} files with main methods in them.
 */
class MainFinder extends NamingPatternCodeFinder {

    @Override protected boolean matches(File file) {
        return file.getName().endsWith(".java");
    }

    @Override protected String testName(File file) {
        return "main";
    }

    public Class<? extends TestRunner> getRunnerClass() {
        return MainRunner.class;
    }

    public File getRunnerJava() {
        return new File(DalvikRunner.HOME_JAVA, "dalvik/runner/MainRunner.java");
    }

    public Classpath getRunnerClasspath() {
        return new Classpath();
    }
}
