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

package tests.xml;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(DeclarationTest.class);
        suite.addTestSuite(DomTest.class);
        suite.addTestSuite(SimpleParserTest.class);
        suite.addTestSuite(SimpleBuilderTest.class);
        suite.addTestSuite(NodeTest.class);
        suite.addTestSuite(NormalizeTest.class);
        suite.addTestSuite(SaxTest.class);

        //suite.addTest(tests.org.w3c.dom.AllTests.suite());
        suite.addTest(tests.api.javax.xml.parsers.AllTests.suite());

        suite.addTest(tests.api.org.xml.sax.AllTests.suite());
        // BEGIN android-changed: this is in the dom module!
        // suite.addTest(tests.api.org.w3c.dom.AllTests.suite());
        // END android-changed
        suite.addTest(tests.org.w3c.dom.AllTests.suite());
        suite.addTest(org.apache.harmony.xml.AllTests.suite());
        suite.addTest(org.kxml2.io.AllTests.suite());

        return suite;
    }

}
