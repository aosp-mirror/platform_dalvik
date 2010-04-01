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

import vogar.commands.Aapt;
import vogar.commands.Command;
import vogar.commands.Dx;
import vogar.commands.Mkdir;
import vogar.commands.Rm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Runs an action in the context of an android.app.Activity on a device
 */
final class ActivityMode extends Mode {

    private static final Logger logger = Logger.getLogger(ActivityMode.class.getName());

    private static final String TEST_ACTIVITY_CLASS   = "vogar.target.TestActivity";

    ActivityMode(Integer debugPort, File sdkJar, List<String> javacArgs,
            int monitorPort, File localTemp, boolean cleanBefore, boolean cleanAfter,
            File deviceRunnerDir) {
        super(new EnvironmentDevice(cleanBefore, cleanAfter,
                debugPort, monitorPort, localTemp, deviceRunnerDir),
                sdkJar, javacArgs, monitorPort);
    }

    private EnvironmentDevice getEnvironmentDevice() {
        return (EnvironmentDevice) environment;
    }

    @Override protected void prepare(Set<File> testRunnerJava, Classpath testRunnerClasspath) {
        testRunnerJava.add(new File("dalvik/libcore/tools/runner/lib/TestActivity.java"));
        super.prepare(testRunnerJava, testRunnerClasspath);
    }

    @Override protected void postCompileRunner() {
    }

    @Override protected void postCompile(Action action) {
        logger.fine("aapt and push " + action.getName());

        // Some things of note:
        // 1. we can't put multiple dex files in one apk
        // 2. we can't just give dex multiple jars with conflicting class names
        // 3. dex is slow if we give it too much to chew on
        // 4. dex can run out of memory if given too much to chew on

        // With that in mind, the APK packaging strategy is as follows:
        // 1. make an empty classes temporary directory
        // 2. add test runner classes
        // 3. find original jar test came from, add contents to classes
        // 4. add supported runner classes specified by finder
        // 5. add latest test classes to output
        // 6. dx to create a dex
        // 7. aapt the dex to create apk
        // 8. sign the apk
        // 9. install the apk
        File packagingDir = makePackagingDirectory(action);
        addRunnerClasses(packagingDir);
        List<File> found = new ArrayList<File>();
        File originalJar = findOriginalJar(action);
        if (originalJar != null) {
            found.add(originalJar);
        }
        found.addAll(action.getRunnerClasspath().getElements());
        extractJars(packagingDir, found);
        addActionClasses(action, packagingDir);
        File dex = createDex(action, packagingDir);
        File apkUnsigned = createApk(action, dex);
        File apkSigned = signApk(action, apkUnsigned);
        installApk(action, apkSigned);
    }

    private File makePackagingDirectory(Action action) {
        File packagingDir = new File(environment.actionCompilationDir(action), "packaging");
        new Rm().directoryTree(packagingDir);
        new Mkdir().mkdirs(packagingDir);
        return packagingDir;
    }

    private void addRunnerClasses(File packagingDir) {
        new Command("rsync", "-a",
                    environment.runnerClassesDir() + "/",
                    packagingDir + "/").execute();
    }

    private File findOriginalJar(Action action) {
        String targetClass = action.getTargetClass();
        String targetClassFile = targetClass.replace('.', '/') + ".class";
        for (File element : classpath.getElements()) {
            try {
                JarFile jar = new JarFile(element);
                JarEntry jarEntry = jar.getJarEntry(targetClassFile);
                if (jarEntry != null) {
                    return element;
                }
            } catch (IOException e) {
                throw new RuntimeException(
                        "Could not find element " + element +
                        " of class path " + classpath, e);
            }
        }
        return null;
    }

    private static void extractJars(File packagingDir, List<File> jars) {
        for (File jar : jars) {
            new Command.Builder()
                    .args("unzip")
                    .args("-q")
                    .args("-o")
                    .args(jar)
                    .args("-d")
                    .args(packagingDir).execute();
        }
        new Rm().directoryTree(new File(packagingDir, "META-INF"));
    }

    private void addActionClasses(Action action, File packagingDir) {
        File classesDir = environment.classesDir(action);
        new Command("rsync", "-a",
                    classesDir + "/",
                    packagingDir + "/").execute();
    }
    private File createDex(Action action, File packagingDir) {
        File classesDir = environment.classesDir(action);
        File dex = new File(classesDir + ".dex");
        new Dx().dex(dex, Classpath.of(packagingDir));
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
        File androidManifestFile =
                new File(environment.actionCompilationDir(action),
                        "AndroidManifest.xml");
        try {
            FileOutputStream androidManifestOut =
                    new FileOutputStream(androidManifestFile);
            androidManifestOut.write(androidManifest.getBytes("UTF-8"));
            androidManifestOut.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem writing " + androidManifestFile, e);
        }

        File classesDir = environment.classesDir(action);
        File apkUnsigned = new File(classesDir  + ".apk.unsigned");
        new Aapt().apk(apkUnsigned, androidManifestFile);
        new Aapt().add(apkUnsigned, dex);
        new Aapt().add(apkUnsigned, new File(classesDir , TestProperties.FILE));
        return apkUnsigned;
    }

    private File signApk(Action action, File apkUnsigned) {
        File classesDir = environment.classesDir(action);
        File apkSigned = new File(classesDir, action.getName() + ".apk");
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
