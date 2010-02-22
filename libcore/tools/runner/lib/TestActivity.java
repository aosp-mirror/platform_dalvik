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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Runs a user-supplied {@code main(String[] args)} method
 * in the context of an Android activity. The result of the method
 * (success or exception) is reported to a file where Dalvik
 * Runner can pick it up.
 */
public class TestActivity extends Activity {

    private final static String TAG = "TestActivity";

    private TextView view;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.view = new TextView(this);
        log("TestActivity starting...");
        setContentView(view);
        ActivityRunner activityRunner = new ActivityRunner();
        activityRunner.run();
    }

    private void log(String message, Throwable ex) {
        log(message + "\n" + Log.getStackTraceString(ex));
    }
    private void log(String message) {
        Log.i(TAG, message);
        view.append(message + "\n");
    }

    class ActivityRunner extends TestRunner {

        private final File runnerDir;
        private final Thread shutdownHook = new Thread(new ShutdownHook());

        ActivityRunner() {
            runnerDir = new File(properties.getProperty(TestProperties.DEVICE_RUNNER_DIR));
        }

        @Override public boolean run() {
            log("Using " + runnerClass + " to run " + qualifiedName);
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            boolean success = super.run();
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            writeResultFile(success);
            return success;
        }

        private void writeResultFile (boolean success) {
            String result = TestProperties.result(success);
            File resultDir = new File(runnerDir, qualifiedName);
            File resultTemp = new File(resultDir, TestProperties.RESULT_FILE + ".temp");
            File resultFile = new File(resultDir, TestProperties.RESULT_FILE);
            log("TestActivity " + result + " " + resultFile);
            try {
                FileOutputStream resultOut = new FileOutputStream(resultTemp);
                resultOut.write(result.getBytes("UTF-8"));
                resultOut.close();
                // atomically rename since DalvikRunner will be polling for this
                resultTemp.renameTo(resultFile);
            } catch (IOException e) {
                log("TestActivity could not create result file", e);
            }
        }

        /**
         * Used to trap tests that try to exit on the their own. We
         * treat this as a failure since they usually are calling
         * System.exit with a non-zero value.
         */
        class ShutdownHook implements Runnable {
            public void run() {
                writeResultFile(false);
            }
        }
    }
}
