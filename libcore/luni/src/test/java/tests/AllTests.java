/*
 * Copyright (C) 2007 The Android Open Source Project
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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Listing of all the tests that are to be run.
 */
public class AllTests
{
    public static void run() {
        TestRunner.main(new String[] { AllTests.class.getName() });
    }

    public static final Test suite() {
        TestSuite suite = tests.TestSuiteFactory.createTestSuite();
        
        suite.addTest(tests.annotation.AllTests.suite());
        suite.addTest(tests.archive.AllTests.suite());
        suite.addTest(tests.concurrent.AllTests.suite());
        suite.addTest(tests.crypto.AllTests.suite());
        suite.addTest(tests.dom.AllTests.suite());
        suite.addTest(tests.logging.AllTests.suite());
        suite.addTest(tests.luni.AllTestsIo.suite());
        suite.addTest(tests.luni.AllTestsLang.suite());
        suite.addTest(tests.luni.AllTestsNet.suite());
        suite.addTest(tests.luni.AllTestsUtil.suite());
        suite.addTest(tests.math.AllTests.suite());
        suite.addTest(tests.nio.AllTests.suite());
        suite.addTest(tests.nio_char.AllTests.suite());
        suite.addTest(tests.prefs.AllTests.suite());
        suite.addTest(tests.regex.AllTests.suite());
        suite.addTest(tests.security.AllTests.suite());
        suite.addTest(tests.sql.AllTests.suite());
        suite.addTest(tests.suncompat.AllTests.suite());
        suite.addTest(tests.text.AllTests.suite());
        suite.addTest(tests.xml.AllTests.suite());
        suite.addTest(tests.xnet.AllTests.suite());

        suite.addTest(org.apache.harmony.luni.platform.AllTests.suite());
        
        return suite;
    }
}
