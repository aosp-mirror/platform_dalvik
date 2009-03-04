/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.google.coretests;

import junit.textui.TestRunner;
import tests.AllTests;

/**
 * Main class to run the core tests.
 */
public class Main
{
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Running all tests...");
            TestRunner.run(AllTests.suite());
        } else if ("--stats".equals(args[0])) {
            // Delegate to new stats test runner
            String[] args2 = new String[args.length - 1];
            System.arraycopy(args, 1, args2, 0, args2.length);
            
            if (args2.length == 0) {
                System.out.println("Running all tests with stats...");
                StatTestRunner.run(AllTests.suite());
            } else {
                System.out.println("Running selected tests with stats...");
                StatTestRunner.main(args2);
            }
        } else {
            System.out.println("Running selected tests...");
            TestRunner.main(args);
        }
        
        Runtime.getRuntime().halt(0);
    }
}
