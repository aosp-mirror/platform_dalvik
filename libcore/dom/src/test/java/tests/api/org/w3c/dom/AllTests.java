/*
 * Copyright (C) 2008 Google Inc.
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

package tests.api.org.w3c.dom;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllTests {
    /*public static void run() {
        TestRunner.main(new String[] {AllTests.class.getName()});
    }*/

    public static final Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(AllTests_Level1.suite());
        suite.addTest(AllTests_Level2.suite());
        return suite;
    }
}
