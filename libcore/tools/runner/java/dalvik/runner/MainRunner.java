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

package dalvik.runner;

import java.lang.reflect.Method;

/**
 * Runs a Java class with a main method.
 */
public final class MainRunner extends TestRunner {

    @Override public boolean test() {
        try {
            Method mainMethod = Class.forName(className)
                    .getDeclaredMethod("main", String[].class);
            mainMethod.invoke(null, new Object[] { new String[0] });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false; // always print main method output
    }

    public static void main(String[] args) throws Exception {
        new MainRunner().run();
    }
}
