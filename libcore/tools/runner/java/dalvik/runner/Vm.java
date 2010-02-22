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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A Java-like virtual machine for compiling and running tests.
 */
public abstract class Vm extends Mode {

    private static final Logger logger = Logger.getLogger(Vm.class.getName());

    protected final List<String> additionalVmArgs;

    Vm(Environment environment, long timeoutSeconds, File sdkJar,
            List<String> additionalVmArgs) {
        super(environment, timeoutSeconds, sdkJar);
        this.additionalVmArgs = additionalVmArgs;
    }

    /**
     * Returns a VM for test execution.
     */
    @Override protected List<Command> buildCommands(TestRun testRun) {
        return Collections.singletonList(newVmCommandBuilder(testRun.getUserDir())
                .classpath(getRuntimeSupportClasspath(testRun))
                .userDir(testRun.getUserDir())
                .debugPort(environment.debugPort)
                .vmArgs(additionalVmArgs)
                .mainClass(TestRunner.class.getName())
                .build());
    }

    /**
     * Returns a VM for test execution.
     */
    protected abstract VmCommandBuilder newVmCommandBuilder(File workingDirectory);

    /**
     * Returns the classpath containing JUnit and the dalvik annotations
     * required for test execution.
     */
    protected abstract Classpath getRuntimeSupportClasspath(TestRun testRun);

    /**
     * Builds a virtual machine command.
     */
    public static class VmCommandBuilder {
        private File temp;
        private Classpath classpath = new Classpath();
        private File workingDir;
        private File userDir;
        private Integer debugPort;
        private String mainClass;
        private List<String> vmCommand = Collections.singletonList("java");
        private List<String> vmArgs = new ArrayList<String>();

        public VmCommandBuilder vmCommand(String... vmCommand) {
            this.vmCommand = Arrays.asList(vmCommand.clone());
            return this;
        }

        public VmCommandBuilder temp(File temp) {
            this.temp = temp;
            return this;
        }

        public VmCommandBuilder classpath(Classpath classpath) {
            this.classpath.addAll(classpath);
            return this;
        }

        public VmCommandBuilder workingDir(File workingDir) {
            this.workingDir = workingDir;
            return this;
        }

        public VmCommandBuilder userDir(File userDir) {
            this.userDir = userDir;
            return this;
        }

        public VmCommandBuilder debugPort(Integer debugPort) {
            this.debugPort = debugPort;
            return this;
        }

        public VmCommandBuilder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        public VmCommandBuilder vmArgs(String... vmArgs) {
            return vmArgs(Arrays.asList(vmArgs));
        }

        public VmCommandBuilder vmArgs(Collection<String> vmArgs) {
            this.vmArgs.addAll(vmArgs);
            return this;
        }

        public Command build() {
            Command.Builder builder = new Command.Builder();
            builder.args(vmCommand);
            builder.args("-classpath", classpath.toString());
            builder.args("-Duser.dir=" + userDir);
            if (workingDir != null) {
                builder.workingDirectory(workingDir);
            }

            if (temp != null) {
                builder.args("-Djava.io.tmpdir=" + temp);
            }

            if (debugPort != null) {
                builder.args("-Xrunjdwp:transport=dt_socket,address="
                        + debugPort + ",server=y,suspend=y");
            }

            builder.args(vmArgs);
            builder.args(mainClass);

            return builder.build();
        }
    }
}
