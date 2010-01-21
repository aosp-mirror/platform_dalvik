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
 * A local Java virtual machine like Harmony or the RI.
 */
final class JavaVm extends Vm {

    private final String javaHome;

    JavaVm(Integer debugPort, long timeoutSeconds, File sdkJar,
            File localTemp, String javaHome, boolean clean) {
        super(debugPort, timeoutSeconds, sdkJar, localTemp, clean);
        this.javaHome = javaHome;
    }

    @Override protected VmCommandBuilder newVmCommandBuilder(
            File workingDirectory) {
        return new VmCommandBuilder()
                .vmCommand(javaHome + "/bin/java")
                .workingDir(workingDirectory);
    }
}
