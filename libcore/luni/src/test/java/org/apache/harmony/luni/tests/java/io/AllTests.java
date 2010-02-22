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

package org.apache.harmony.luni.tests.java.io;

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
        TestSuite suite = new TestSuite("Tests for java.io");

        suite.addTestSuite(BufferedReaderTest.class);
        suite.addTestSuite(FilePermissionTest.class);
        suite.addTestSuite(FileTest.class);
        suite.addTestSuite(InputStreamReaderTest.class);
        suite.addTestSuite(ObjectInputStreamTest.class);
        suite.addTestSuite(ObjectStreamConstantsTest.class);
        suite.addTestSuite(OutputStreamWriterTest.class);
        suite.addTestSuite(PushBackInputStreamTest.class);
        suite.addTestSuite(RandomAccessFileTest.class);
        suite.addTestSuite(ReaderTest.class);
        suite.addTestSuite(WriterTest.class);

        return suite;
    }
}
