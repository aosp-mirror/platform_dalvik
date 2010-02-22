/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.luni;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Listing of all the tests that are to be run.
 */
public class AllTestsIo
{

    public static void run() {
        TestRunner.main(new String[] { AllTestsIo.class.getName() });
    }

    public static final Test suite() {
        TestSuite suite = new TestSuite("Tests for java.io");

        suite.addTest(tests.api.java.io.AllTests.suite());

        suite.addTest(org.apache.harmony.luni.tests.java.io.AllTests.suite());

        return suite;
    }
}
