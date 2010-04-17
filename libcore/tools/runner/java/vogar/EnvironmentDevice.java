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

package vogar;

import java.io.File;
import vogar.commands.Adb;

class EnvironmentDevice extends Environment {
    final Adb adb = new Adb();
    final File runnerDir;
    final File vogarTemp;
    final int monitorPort;

    EnvironmentDevice (boolean cleanBefore, boolean cleanAfter,
            Integer debugPort, int monitorPort, File localTemp, File runnerDir) {
        super(cleanBefore, cleanAfter, debugPort, localTemp);
        this.runnerDir = runnerDir;
        this.vogarTemp = new File(runnerDir, "/vogar.tmp");
        this.monitorPort = monitorPort;
    }

    @Override void prepare() {
        adb.waitForDevice();
        adb.waitForNonEmptyDirectory(runnerDir.getParentFile(), 5 * 60);
        if (cleanBefore) {
            adb.rm(runnerDir);
        }
        adb.mkdir(runnerDir);
        adb.mkdir(vogarTemp);
        adb.mkdir(new File("/sdcard/dalvik-cache")); // TODO: only necessary on production devices.
        adb.forwardTcp(monitorPort, monitorPort);
        if (debugPort != null) {
            adb.forwardTcp(debugPort, debugPort);
        }
    }

    @Override protected void prepareUserDir(Action action) {
        File actionClassesDirOnDevice = actionClassesDirOnDevice(action);
        adb.mkdir(actionClassesDirOnDevice);
        File resourcesDirectory = action.getResourcesDirectory();
        if (resourcesDirectory != null) {
            adb.push(resourcesDirectory, actionClassesDirOnDevice);
        }
        action.setUserDir(actionClassesDirOnDevice);
    }

    private File actionClassesDirOnDevice(Action action) {
        return new File(runnerDir, action.getName());
    }

    @Override void cleanup(Action action) {
        super.cleanup(action);
        if (cleanAfter) {
            adb.rm(actionClassesDirOnDevice(action));
        }
    }

    @Override void shutdown() {
        super.shutdown();
        if (cleanAfter) {
            adb.rm(runnerDir);
        }
    }
}
