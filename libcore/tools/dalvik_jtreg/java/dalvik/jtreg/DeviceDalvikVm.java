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

package dalvik.jtreg;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Execute tests on a Dalvik VM using an Android device or emulator.
 */
final class DeviceDalvikVm extends Vm {

    private static final Classpath RUNTIME_SUPPORT_CLASSPATH = Classpath.of(
            new File("/system/framework/core-tests.jar"),
            new File("/system/framework/caliper.jar"),
            new File("/system/framework/guava.jar"),
            new File("/system/framework/jsr305.jar"));

    private static final Logger logger = Logger.getLogger(DeviceDalvikVm.class.getName());
    private final File deviceTemp = new File("/data/jtreg" + UUID.randomUUID());

    private final Adb adb = new Adb();
    private final File testTemp;

    DeviceDalvikVm(Integer debugPort, long timeoutSeconds, File sdkJar, File localTemp) {
        super(debugPort, timeoutSeconds, sdkJar, localTemp);
        this.testTemp = new File(deviceTemp, "/tests.tmp");
    }

    @Override public void prepare() {
        adb.mkdir(deviceTemp);
        adb.mkdir(testTemp);
        if (debugPort != null) {
            adb.forwardTcp(debugPort, debugPort);
        }
        super.prepare();
    }

    @Override protected Classpath postCompile(String name, Classpath targetClasses) {
        logger.fine("dex and push " + name);

        // make the local dex
        File localDex = new File(localTemp, name + ".jar");
        new Dx().dex(localDex.toString(), targetClasses);

        // post the local dex to the device
        File deviceDex = new File(deviceTemp, localDex.getName());
        adb.push(localDex, deviceDex);

        return Classpath.of(deviceDex);
    }

    @Override public void shutdown() {
        super.shutdown();
        adb.rm(deviceTemp);
    }

    @Override public void buildAndInstall(TestRun testRun) {
        super.buildAndInstall(testRun);

        File base = new File(deviceTemp, testRun.getQualifiedName());
        adb.mkdir(base);
        adb.push(testRun.getTestDirectory(), base);
        testRun.setUserDir(base);
    }

    @Override protected VmCommandBuilder newVmCommandBuilder() {
        return new VmCommandBuilder()
                .vmCommand("adb", "shell", "dalvikvm")
                .vmArgs("-Duser.name=root")
                .vmArgs("-Duser.language=en")
                .vmArgs("-Duser.region=US")
                .vmArgs("-Djavax.net.ssl.trustStore=/system/etc/security/cacerts.bks")
                .temp(testTemp);
    }

    @Override protected Classpath getRuntimeSupportClasspath() {
        return RUNTIME_SUPPORT_CLASSPATH;
    }
}
