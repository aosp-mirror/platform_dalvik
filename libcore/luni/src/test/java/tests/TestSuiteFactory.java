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

package tests;

import junit.framework.TestSuite;

/**
 * This is about to go away...
 */
public class TestSuiteFactory {
    
    public static TestSuite createTestSuite(String name) {
        return new TestSuite(name);
    }

    public static TestSuite createTestSuite() {
        return new TestSuite();    
    }
    
}
