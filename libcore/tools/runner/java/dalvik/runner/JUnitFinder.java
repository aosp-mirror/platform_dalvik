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
 * Create {@link TestRun}s for {@code .java} files with JUnit tests in them.
 */
class JUnitFinder extends NamingPatternCodeFinder {

    @Override protected boolean matches(File file) {
        return file.getName().endsWith("Test.java");
    }

    // TODO: try to get names for each method?
    @Override protected String testName(File file) {
        return "junit";
    }

    public Class<? extends TestRunner> getRunnerClass() {
        return JUnitRunner.class;
    }

    public File getRunnerJava() {
        return new File(DalvikRunner.HOME_JAVA, "dalvik/runner/JUnitRunner.java");
    }

    public Classpath getRunnerClasspath() {
        // TODO: we should be able to work with a shipping SDK, not depend on out/...
        return Classpath.of(new File("out/host/common/obj/JAVA_LIBRARIES/junit_intermediates/javalib.jar").getAbsoluteFile());
    }
}
