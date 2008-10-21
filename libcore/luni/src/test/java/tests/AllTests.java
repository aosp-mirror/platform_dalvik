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
        TestSuite   suite = new TestSuite();
        
        suite.addTestSuite(
                tests.api.org.apache.harmony.kernel.dalvik.ThreadsTest.class);
        
        suite.addTestSuite(tests.api.java.lang.ref.PhantomReferenceTest.class);
        suite.addTestSuite(tests.api.java.lang.ref.ReferenceQueueTest.class);
        suite.addTestSuite(tests.api.java.lang.ref.ReferenceTest.class);
        suite.addTestSuite(tests.api.java.lang.ref.SoftReferenceTest.class);
        suite.addTestSuite(tests.api.java.lang.ref.WeakReferenceTest.class);

        // Add some existing Android tests for the luni module.
        suite.addTest(tests.luni.AllTests.suite());

        // Add the luni tests from Harmony. Not included in above. 
        suite.addTest(tests.luni.AllTestsLang.suite());
        suite.addTest(tests.luni.AllTestsUtil.suite());
        suite.addTest(tests.luni.AllTestsNet.suite());
        suite.addTest(tests.luni.AllTestsIo.suite());

        // Add the tests for some packages that are basically part
        // of luni, but did get their own module for some reason. 
        suite.addTest(tests.math.AllTests.suite());
        suite.addTest(tests.text.AllTests.suite());
        suite.addTest(tests.regex.AllTests.suite());

        // Add the tests for the nio and nio-char modules.
        suite.addTest(tests.nio.AllTests.suite());
        suite.addTest(tests.nio_char.AllTests.suite());

        // Add the tests for some security-related modules.
        // TODO Takes ages, commenting out for the moment.
        // suite.addTest(tests.security.AllTests.suite());
        suite.addTest(tests.net.ssl.AllTests.suite());

        // Add the tests for miscellaneous other modules.
        suite.addTest(tests.logging.AllTests.suite());
        suite.addTest(tests.prefs.AllTests.suite());
        suite.addTest(tests.xml.AllTests.suite());
        suite.addTest(tests.sql.AllTests.suite());
        
        // TODO added these suites so they won't be forgotten.
        // Not sure if they have any problems running through.
        // suite.addTest(tests.archive.AllTests.suite());
        // suite.addTest(tests.annotation.AllTests.suite());

        return suite;
    }
}
