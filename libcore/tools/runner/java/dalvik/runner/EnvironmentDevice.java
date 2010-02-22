/*
 * Copyright (C) 2010 The Android Open Source Project
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
import java.util.logging.Logger;

class EnvironmentDevice extends Environment {
    private static final Logger logger = Logger.getLogger(EnvironmentDevice.class.getName());

    final Adb adb = new Adb();
    final File runnerDir;
    final File testTemp;

    EnvironmentDevice (boolean cleanBefore, boolean cleanAfter,
            Integer debugPort, File localTemp, File runnerDir) {
        super(cleanBefore, cleanAfter, debugPort, localTemp);
        this.runnerDir = runnerDir;
        this.testTemp = new File(runnerDir, "/tests.tmp");
    }

    @Override void prepare() {
        adb.waitForDevice();
        adb.waitForNonEmptyDirectory(runnerDir.getParentFile(), 5 * 60);
        if (cleanBefore) {
            adb.rm(runnerDir);
        }
        adb.mkdir(runnerDir);
        adb.mkdir(testTemp);
        adb.mkdir(new File("/sdcard/dalvik-cache")); // TODO: only necessary on production devices.
        if (debugPort != null) {
            adb.forwardTcp(debugPort, debugPort);
        }
    }

    @Override protected void prepareUserDir(TestRun testRun) {
        File testClassesDirOnDevice = testClassesDirOnDevice(testRun);
        adb.mkdir(testClassesDirOnDevice);
        adb.push(testRun.getTestDirectory(), testClassesDirOnDevice);
        testRun.setUserDir(testClassesDirOnDevice);
    }

    private File testClassesDirOnDevice(TestRun testRun) {
        return new File(runnerDir, testRun.getQualifiedName());
    }

    @Override void cleanup(TestRun testRun) {
        super.cleanup(testRun);
        if (cleanAfter) {
            adb.rm(testClassesDirOnDevice(testRun));
        }
    }

    @Override void shutdown() {
        if (cleanAfter) {
            adb.rm(runnerDir);
        }
    }
}
