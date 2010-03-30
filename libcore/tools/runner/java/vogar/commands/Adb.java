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

package vogar.commands;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * An adb command.
 */
public final class Adb {

    public void mkdir(File name) {
        new Command("adb", "shell", "mkdir", name.getPath()).execute();
    }

    public void rm(File name) {
        new Command("adb", "shell", "rm", "-r", name.getPath()).execute();
    }

    public void push(File local, File remote) {
        new Command("adb", "push", local.getPath(), remote.getPath())
                .execute();
    }

    public void install(File apk) {
        new Command("adb", "install", "-r", apk.getPath())
                .execute();
    }

    public void uninstall(String packageName) {
        new Command("adb", "uninstall", packageName)
                .execute();
    }

    public void forwardTcp(int localPort, int devicePort) {
        new Command("adb", "forward", "tcp:" + localPort, "tcp:" + devicePort)
                .execute();
    }

    public void waitForDevice() {
        new Command("adb", "wait-for-device").execute();
    }

    /**
     * Loop until we see a file on the device. For example, wait
     * result.txt appears.
     */
    public void waitForFile(File file, long timeoutSeconds) {
        waitFor(true, file, timeoutSeconds);
    }

    /**
     * Loop until we see a non-empty directory on the device. For
     * example, wait until /sdcard is mounted.
     */
    public void waitForNonEmptyDirectory(File path, long timeoutSeconds) {
        waitFor(false, path, timeoutSeconds);
    }

    private void waitFor(boolean file, File path, long timeoutSeconds) {
        final int millisPerSecond = 1000;
        final long start = System.currentTimeMillis();
        final long deadline = start + (millisPerSecond * timeoutSeconds);

        while (true) {
            final long remainingSeconds = ((deadline - System.currentTimeMillis())
                                           / millisPerSecond);
            String pathArgument = path.getPath();
            if (!file) {
                pathArgument += "/";
            }
            Command command = new Command("adb", "shell", "ls", pathArgument);
            List<String> output;
            try {
                output = command.executeWithTimeout(remainingSeconds);
            } catch (TimeoutException e) {
                throw new RuntimeException("Timed out after " + timeoutSeconds +
                                           " seconds waiting for file " + path, e);
            }
            try {
                Thread.sleep(millisPerSecond);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (file) {
                // for files, we expect one line of output that matches the filename
                if (output.size() == 1 && output.get(0).equals(path.getPath())) {
                    return;
                }
            } else {
                // for a non empty directory, we just want any output
                if (!output.isEmpty()) {
                    return;
                }
            }
        }
    }
}
