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
import java.util.logging.Logger;

/**
 * Execute tests on a Dalvik VM using an Android device or emulator.
 */
final class DeviceDalvikVm extends Vm {

    // TODO: Don't assume we can put files in /system/framework,
    // so we can run on production devices.
    private static final Classpath RUNTIME_SUPPORT_CLASSPATH = Classpath.of(
            new File("/system/framework/core-tests.jar"),
            new File("/system/framework/caliper.jar"),
            new File("/system/framework/guava.jar"),
            new File("/system/framework/jsr305.jar"));

    private static final Logger logger = Logger.getLogger(DeviceDalvikVm.class.getName());

    DeviceDalvikVm(Integer debugPort, long timeoutSeconds, File sdkJar,
            File localTemp, List<String> additionalVmArgs,
            boolean cleanBefore, boolean cleanAfter, File runnerDir) {
        super(new EnvironmentDevice(cleanBefore, cleanAfter, debugPort, localTemp, runnerDir),
                timeoutSeconds, sdkJar, additionalVmArgs);
    }

    private EnvironmentDevice getEnvironmentDevice() {
        return (EnvironmentDevice) environment;
    }

    @Override protected void postCompileTestRunner() {
        postCompile("testrunner", environment.testRunnerClassesDir());
    }

    @Override protected Classpath postCompileTest(TestRun testRun) {
        return postCompile(testRun.getQualifiedName(), environment.testClassesDir(testRun));
    }

    private Classpath postCompile(String name, File dir) {
        logger.fine("dex and push " + name);

        // make the local dex (inside a jar)
        File localDex = new File(dir.getPath() + ".jar");
        new Dx().dex(localDex, Classpath.of(dir));

        // post the local dex to the device
        File deviceDex = new File(getEnvironmentDevice().runnerDir, name + ".jar");
        getEnvironmentDevice().adb.push(localDex, deviceDex);

        return Classpath.of(deviceDex);
    }

    @Override protected VmCommandBuilder newVmCommandBuilder(
            File workingDirectory) {
        // ignore the working directory; it's device-local and we can't easily
        // set the working directory for commands run via adb shell.
        return new VmCommandBuilder()
                .vmCommand("adb", "shell", "dalvikvm")
                .vmArgs("-Duser.name=root")
                .vmArgs("-Duser.language=en")
                .vmArgs("-Duser.region=US")
                .vmArgs("-Djavax.net.ssl.trustStore=/system/etc/security/cacerts.bks")
                .temp(getEnvironmentDevice().testTemp);
    }

    @Override protected Classpath getRuntimeSupportClasspath() {
        return RUNTIME_SUPPORT_CLASSPATH;
    }
}
