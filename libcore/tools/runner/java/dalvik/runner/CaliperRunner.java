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

import com.google.caliper.Benchmark;
import com.google.caliper.Runner;

/**
 * Runs a <a href="http://code.google.com/p/caliper/">Caliper</a> benchmark.
 */
public final class CaliperRunner implements dalvik.runner.Runner {

    public void prepareTest(Class<?> testClass) {}

    public boolean test(Class<?> testClass) {
        try {
            Runner.main(testClass.asSubclass(Benchmark.class), new String[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false; // always print benchmarking results
    }
}
