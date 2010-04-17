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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import vogar.commands.Aapt;
import vogar.commands.Command;
import vogar.commands.Dx;
import vogar.commands.Rm;

/**
 * Runs an action in the context of an android.app.Activity on a device
 */
final class ActivityMode extends Mode {

    private static final Logger logger = Logger.getLogger(ActivityMode.class.getName());

    private static final String TEST_ACTIVITY_CLASS   = "vogar.target.TestActivity";

    ActivityMode(Integer debugPort, File sdkJar, List<String> javacArgs,
            int monitorPort, File localTemp, boolean cleanBefore, boolean cleanAfter,
            File deviceRunnerDir, Classpath classpath) {
        super(new EnvironmentDevice(cleanBefore, cleanAfter,
                debugPort, monitorPort, localTemp, deviceRunnerDir),
                sdkJar, javacArgs, monitorPort, classpath);
    }

    private EnvironmentDevice getEnvironmentDevice() {
        return (EnvironmentDevice) environment;
    }

    @Override protected void prepare(Set<RunnerSpec> runners) {
        runnerJava.add(new File("dalvik/libcore/tools/runner/lib/TestActivity.java"));
        super.prepare(runners);
    }

    @Override protected void postCompile(Action action, File jar) {
        logger.fine("aapt and push " + action.getName());

        // We can't put multiple dex files in one apk.
        // We can't just give dex multiple jars with conflicting class names

        // With that in mind, the APK packaging strategy is as follows:
        // 1. dx to create a dex
        // 2. aapt the dex to create apk
        // 3. sign the apk
        // 4. install the apk
        File dex = createDex(action, jar);
        File apkUnsigned = createApk(action, dex);
        File apkSigned = signApk(action, apkUnsigned);
        installApk(action, apkSigned);
    }

    /**
     * Returns a single dexfile containing {@code action}'s classes and all
     * dependencies.
     */
    private File createDex(Action action, File actionJar) {
        File dex = environment.file(action, "classes.dex");
        Classpath classesToDex = Classpath.of(actionJar);
        classesToDex.addAll(this.classpath);
        new Dx().dex(dex, classesToDex);
        return dex;
    }

    /**
     * According to android.content.pm.PackageParser, package name
     * "must have at least one '.' separator" Since the qualified name
     * may not contain a dot, we prefix containing one to ensure we
     * are compliant.
     */
    private static String packageName(Action action) {
        return "vogar.test." + action.getName();
    }

    private File createApk (Action action, File dex) {
        String androidManifest =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "      package=\"" + packageName(action) + "\">\n" +
            "    <uses-permission android:name=\"android.permission.INTERNET\" />\n" +
            "    <application>\n" +
            "        <activity android:name=\"" + TEST_ACTIVITY_CLASS + "\">\n" +
            "            <intent-filter>\n" +
            "                <action android:name=\"android.intent.action.MAIN\" />\n" +
            "                <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
            "            </intent-filter>\n" +
            "        </activity>\n" +
            "    </application>\n" +
            "</manifest>\n";
        File androidManifestFile = environment.file(action, "classes", "AndroidManifest.xml");
        try {
            FileOutputStream androidManifestOut =
                    new FileOutputStream(androidManifestFile);
            androidManifestOut.write(androidManifest.getBytes("UTF-8"));
            androidManifestOut.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem writing " + androidManifestFile, e);
        }

        File apkUnsigned = environment.file(action, action + ".apk.unsigned");
        new Aapt().apk(apkUnsigned, androidManifestFile);
        new Aapt().add(apkUnsigned, dex);
        new Aapt().add(apkUnsigned, environment.file(action, "classes", TestProperties.FILE));
        return apkUnsigned;
    }

    private File signApk(Action action, File apkUnsigned) {
        File apkSigned = environment.file(action, action + ".apk");
        // TODO: we should be able to work with a shipping SDK, not depend on out/...
        // TODO: we should be able to work without hardwired keys, not depend on build/...
        new Command.Builder()
                .args("java")
                .args("-jar")
                .args("out/host/linux-x86/framework/signapk.jar")
                .args("build/target/product/security/testkey.x509.pem")
                .args("build/target/product/security/testkey.pk8")
                .args(apkUnsigned)
                .args(apkSigned).execute();
        new Rm().file(apkUnsigned);
        return apkSigned;
    }

    private void installApk(Action action, File apkSigned) {
        // install the local apk ona the device
        getEnvironmentDevice().adb.uninstall(packageName(action));
        getEnvironmentDevice().adb.install(apkSigned);
    }

    @Override protected void fillInProperties(Properties properties, Action action) {
        super.fillInProperties(properties, action);
        properties.setProperty(TestProperties.DEVICE_RUNNER_DIR, getEnvironmentDevice().runnerDir.getPath());
    }

    @Override protected Command createActionCommand(Action action) {
        return new Command(
                "adb", "shell", "am", "start", "-W",
                "-a", "android.intent.action.MAIN",
                "-n", (packageName(action) + "/" + TEST_ACTIVITY_CLASS));
    }

    @Override void cleanup(Action action) {
        super.cleanup(action);
        if (environment.cleanAfter) {
            getEnvironmentDevice().adb.uninstall(action.getName());
        }
    }
}
