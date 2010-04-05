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

package vogar.target;

import java.lang.reflect.Method;
import vogar.Result;

/**
 * Runs a Java class with a main method.
 */
public final class MainRunner implements Runner {

    private TargetMonitor monitor;
    private Method main;

    public void init(TargetMonitor monitor, String actionName,
            Class<?> testClass) {
        this.monitor = monitor;
        try {
            main = testClass.getMethod("main", String[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void run(String actionName, Class<?> testClass) {
        monitor.outcomeStarted(actionName, actionName);
        try {
            main.invoke(null, new Object[] { new String[0] });
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        monitor.outcomeFinished(Result.SUCCESS);
    }
}
