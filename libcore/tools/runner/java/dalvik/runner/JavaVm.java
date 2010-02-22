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
import java.util.List;
import java.util.Set;

/**
 * A local Java virtual machine like Harmony or the RI.
 */
final class JavaVm extends Vm {

    private final File javaHome;

    JavaVm(Integer debugPort, long timeoutSeconds, File sdkJar, File localTemp,
            File javaHome, List<String> additionalVmArgs,
            boolean cleanBefore, boolean cleanAfter) {
        super(new EnvironmentHost(cleanBefore, cleanAfter, debugPort, localTemp),
                timeoutSeconds, sdkJar, additionalVmArgs);
        this.javaHome = javaHome;
    }

    @Override protected void postCompileTestRunner() {
    }

    @Override protected void postCompileTest(TestRun testRun) {
    }

    @Override protected VmCommandBuilder newVmCommandBuilder(
            File workingDirectory) {
        String java = javaHome == null ? "java" : new File(javaHome, "bin/java").getPath();
        return new VmCommandBuilder()
                .vmCommand(java)
                .workingDir(workingDirectory);
    }
    @Override protected Classpath getRuntimeSupportClasspath(TestRun testRun) {
        Classpath classpath = new Classpath();
        classpath.addAll(environment.testClassesDir(testRun));
        classpath.addAll(testClasspath);
        classpath.addAll(environment.testRunnerClassesDir());
        classpath.addAll(testRunnerClasspath);
        return classpath;
    }
}
