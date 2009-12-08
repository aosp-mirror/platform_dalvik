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

package org.apache.harmony.luni.tests.java.util;

import com.google.caliper.Benchmark;
import com.google.caliper.DefaultBenchmarkSuite;

/**
 * This class exists only to force a dependency from our libraries on Caliper,
 * our micro benchmarking framework. 
 */
public class NullBenchmarkSuite extends DefaultBenchmarkSuite {

    class NullBenchmark extends Benchmark {
        @Override public Object run(int trials) throws Exception {
            for (int i = 0; i < trials; i++) {
                // code under test goes here!
            }
            return null;
        }
    }
}
