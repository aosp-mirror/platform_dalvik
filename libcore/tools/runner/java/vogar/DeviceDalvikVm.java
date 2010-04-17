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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import vogar.commands.Dx;

/**
 * Execute actions on a Dalvik VM using an Android device or emulator.
 */
final class DeviceDalvikVm extends Vm {
    private static final Logger logger = Logger.getLogger(DeviceDalvikVm.class.getName());

    /** A list of generic names that we avoid when naming generated files. */
    private static final Set<String> BANNED_NAMES = new HashSet<String>();
    {
        BANNED_NAMES.add("classes");
        BANNED_NAMES.add("javalib");
    }

    DeviceDalvikVm(Integer debugPort, File sdkJar, List<String> javacArgs,
            int monitorPort, File localTemp, List<String> additionalVmArgs,
            List<String> targetArgs, boolean cleanBefore, boolean cleanAfter,
            File runnerDir, Classpath classpath) {
        super(new EnvironmentDevice(cleanBefore, cleanAfter, debugPort, monitorPort, localTemp,
                runnerDir), sdkJar, javacArgs, additionalVmArgs, targetArgs, monitorPort, classpath);
    }

    private EnvironmentDevice getEnvironmentDevice() {
        return (EnvironmentDevice) environment;
    }

    @Override protected void installRunner() {
        // dex everything on the classpath and push it to the device.
        for (File classpathElement : classpath.getElements()) {
            dexAndPush(basenameOfJar(classpathElement), classpathElement);
        }
    }

    private String basenameOfJar(File file) {
        String name = file.getName().replaceAll("\\.jar$", "");
        while (BANNED_NAMES.contains(name)) {
            file = file.getParentFile();
            name = file.getName();
        }
        return name;
    }

    @Override protected void postCompile(Action action, File jar) {
        dexAndPush(action.getName(), jar);
    }

    private void dexAndPush(String name, File jar) {
        logger.fine("dex and push " + name);

        // make the local dex (inside a jar)
        File localDex = environment.file(name, name + ".dx.jar");
        new Dx().dex(localDex, Classpath.of(jar));

        // post the local dex to the device
        getEnvironmentDevice().adb.push(localDex, deviceDexFile(name));
    }

    private File deviceDexFile(String name) {
        return new File(getEnvironmentDevice().runnerDir, name + ".jar");
    }

    @Override protected VmCommandBuilder newVmCommandBuilder(
            File workingDirectory) {
        // ignore the working directory; it's device-local and we can't easily
        // set the working directory for commands run via adb shell.
        // TODO: we only *need* to set ANDROID_DATA on production devices.
        // We set "user.home" to /sdcard because code might reasonably assume it can write to
        // that directory.
        return new VmCommandBuilder()
                .vmCommand("adb", "shell", "ANDROID_DATA=/sdcard", "dalvikvm")
                .vmArgs("-Duser.home=/sdcard")
                .vmArgs("-Duser.name=root")
                .vmArgs("-Duser.language=en")
                .vmArgs("-Duser.region=US")
                .vmArgs("-Djavax.net.ssl.trustStore=/system/etc/security/cacerts.bks")
                .temp(getEnvironmentDevice().vogarTemp);
    }

    @Override protected Classpath getRuntimeClasspath(Action action) {
        Classpath result = new Classpath();
        result.addAll(deviceDexFile(action.getName()));
        for (File classpathElement : classpath.getElements()) {
            result.addAll(deviceDexFile(basenameOfJar(classpathElement)));
        }
        return result;
    }
}
