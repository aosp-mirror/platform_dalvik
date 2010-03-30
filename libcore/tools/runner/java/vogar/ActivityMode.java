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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Runs a test in the context of an android.app.Activity on a device
 */
final class ActivityMode extends Mode {

    private static final Logger logger = Logger.getLogger(ActivityMode.class.getName());

    private static final String TEST_ACTIVITY_CLASS   = "vogar.target.TestActivity";

    ActivityMode(Integer debugPort, long timeoutSeconds, File sdkJar, List<String> javacArgs,
                 PrintStream tee, File localTemp, boolean cleanBefore, boolean cleanAfter,
                 File deviceRunnerDir) {
        super(new EnvironmentDevice(cleanBefore, cleanAfter,
                debugPort, localTemp, deviceRunnerDir),
                timeoutSeconds, sdkJar, javacArgs, tee);
    }

    private EnvironmentDevice getEnvironmentDevice() {
        return (EnvironmentDevice) environment;
    }

    @Override protected void prepare(Set<File> testRunnerJava, Classpath testRunnerClasspath) {
        testRunnerJava.add(new File("dalvik/libcore/tools/runner/lib/TestActivity.java"));
        super.prepare(testRunnerJava, testRunnerClasspath);
    }

    @Override protected void postCompileTestRunner() {
    }

    @Override protected void postCompileTest(TestRun testRun) {
        logger.fine("aapt and push " + testRun.getQualifiedName());

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
        File packagingDir = makePackagingDirectory(testRun);
        addTestRunnerClasses(packagingDir);
        List<File> found = new ArrayList<File>();
        File originalTestJar = findOriginalTestJar(testRun);
        if (originalTestJar != null) {
            found.add(originalTestJar);
        }
        found.addAll(testRun.getRunnerClasspath().getElements());
        extractJars(packagingDir, found);
        addTestClasses(testRun, packagingDir);
        File dex = createDex(testRun, packagingDir);
        File apkUnsigned = createApk(testRun, dex);
        File apkSigned = signApk(testRun, apkUnsigned);
        installApk(testRun, apkSigned);
    }

    private File makePackagingDirectory(TestRun testRun) {
        File packagingDir = new File(environment.testCompilationDir(testRun), "packaging");
        new Rm().directoryTree(packagingDir);
        new Mkdir().mkdirs(packagingDir);
        return packagingDir;
    }

    private void addTestRunnerClasses(File packagingDir) {
        new Command("rsync", "-a",
                    environment.testRunnerClassesDir() + "/",
                    packagingDir + "/").execute();
    }

    private File findOriginalTestJar(TestRun testRun) {
        String testClass = testRun.getTestClass();
        String testFile = testClass.replace('.', '/') + ".class";
        for (File element : testClasspath.getElements()) {
            try {
                JarFile jar = new JarFile(element);
                JarEntry jarEntry = jar.getJarEntry(testFile);
                if (jarEntry != null) {
                    return element;
                }
            } catch (IOException e) {
                throw new RuntimeException(
                        "Could not find element " + element +
                        " of test class path " + testClasspath, e);
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

    private void addTestClasses(TestRun testRun, File packagingDir) {
        File testClassesDir = environment.testClassesDir(testRun);
        new Command("rsync", "-a",
                    testClassesDir + "/",
                    packagingDir + "/").execute();
    }
    private File createDex (TestRun testRun, File packagingDir) {
        File testClassesDir = environment.testClassesDir(testRun);
        File dex = new File(testClassesDir + ".dex");
        new Dx().dex(dex, Classpath.of(packagingDir));
        return dex;
    }

    /**
     * According to android.content.pm.PackageParser, package name
     * "must have at least one '.' separator" Since the qualified name
     * may not contain a dot, we prefix containing one to ensure we
     * are compliant.
     */
    private static String packageName(TestRun testRun) {
        return "vogar.test." + testRun.getQualifiedName();
    }

    private File createApk (TestRun testRun, File dex) {
        String androidManifest =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "      package=\"" + packageName(testRun) + "\">\n" +
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
                new File(environment.testCompilationDir(testRun),
                        "AndroidManifest.xml");
        try {
            FileOutputStream androidManifestOut =
                    new FileOutputStream(androidManifestFile);
            androidManifestOut.write(androidManifest.getBytes("UTF-8"));
            androidManifestOut.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem writing " + androidManifestFile, e);
        }

        File testClassesDir = environment.testClassesDir(testRun);
        File apkUnsigned = new File(testClassesDir + ".apk.unsigned");
        new Aapt().apk(apkUnsigned, androidManifestFile);
        new Aapt().add(apkUnsigned, dex);
        new Aapt().add(apkUnsigned, new File(testClassesDir, TestProperties.FILE));
        return apkUnsigned;
    }

    private File signApk(TestRun testRun, File apkUnsigned) {
        File testClassesDir = environment.testClassesDir(testRun);
        File apkSigned = new File(testClassesDir, testRun.getQualifiedName() + ".apk");
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

    private void installApk(TestRun testRun, File apkSigned) {
        // install the local apk ona the device
        getEnvironmentDevice().adb.uninstall(packageName(testRun));
        getEnvironmentDevice().adb.install(apkSigned);
    }

    @Override protected void fillInProperties(Properties properties, TestRun testRun) {
        super.fillInProperties(properties, testRun);
        properties.setProperty(TestProperties.DEVICE_RUNNER_DIR, getEnvironmentDevice().runnerDir.getPath());
    }

    @Override protected List<String> runTestCommand(TestRun testRun)
            throws TimeoutException {
        new Command(
            "adb", "shell", "am", "start",
            "-a","android.intent.action.MAIN",
            "-n", (packageName(testRun) + "/" + TEST_ACTIVITY_CLASS)).executeWithTimeout(timeoutSeconds);

        File resultDir = new File(getEnvironmentDevice().runnerDir, testRun.getQualifiedName());
        File resultFile = new File(resultDir, TestProperties.RESULT_FILE);
        getEnvironmentDevice().adb.waitForFile(resultFile, timeoutSeconds);
        return new Command.Builder()
            .args("adb", "shell", "cat", resultFile.getPath())
            .tee(tee)
            .build().executeWithTimeout(timeoutSeconds);
    }

    @Override void cleanup(TestRun testRun) {
        super.cleanup(testRun);
        if (environment.cleanAfter) {
            getEnvironmentDevice().adb.uninstall(testRun.getQualifiedName());
        }
    }
}
