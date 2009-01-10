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

package org.apache.harmony.luni.tests.java.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Listing of all the tests that are to be run.
 */
public class AllTests {

    public static void run() {
        TestRunner.main(new String[] { AllTests.class.getName() });
    }

    public static final Test suite() {
        TestSuite suite = tests.TestSuiteFactory.createTestSuite("Tests for java.util");

        suite.addTestSuite(AbstractCollectionTest.class);
        suite.addTestSuite(AbstractMapTest.class);
        suite.addTestSuite(AbstractSetTest.class);
        suite.addTestSuite(ArraysTest.class);
        suite.addTestSuite(BitSetTest.class);
        suite.addTestSuite(CollectionsTest.class);
        suite.addTestSuite(DateTest.class);
        suite.addTestSuite(DictionaryTest.class);
        suite.addTestSuite(DuplicateFormatFlagsExceptionTest.class);
        suite.addTestSuite(EventListenerProxyTest.class);
        suite.addTestSuite(FormatFlagsConversionMismatchExceptionTest.class);
        suite.addTestSuite(FormattableTest.class);
        suite.addTestSuite(FormatterClosedExceptionTest.class);
        suite.addTestSuite(FormatterTest.class);
        suite.addTestSuite(HashMapTest.class);
        suite.addTestSuite(IdentityHashMapTest.class);
        suite.addTestSuite(IllegalFormatCodePointExceptionTest.class);
        suite.addTestSuite(IllegalFormatConversionExceptionTest.class);
        suite.addTestSuite(IllegalFormatFlagsExceptionTest.class);
        suite.addTestSuite(IllegalFormatPrecisionExceptionTest.class);
        suite.addTestSuite(IllegalFormatWidthExceptionTest.class);
        suite.addTestSuite(InputMismatchExceptionTest.class);
        suite.addTestSuite(InvalidPropertiesFormatExceptionTest.class);
        suite.addTestSuite(LinkedHashMapTest.class);
        suite.addTestSuite(ListIteratorTest.class);
        suite.addTestSuite(LocaleTest.class);
        suite.addTestSuite(MapEntryTest.class);
        suite.addTestSuite(MissingFormatArgumentExceptionTest.class);
        suite.addTestSuite(MissingFormatWidthExceptionTest.class);
        suite.addTestSuite(ObserverTest.class);
        suite.addTestSuite(UnknownFormatConversionExceptionTest.class);
        suite.addTestSuite(UnknownFormatFlagsExceptionTest.class);
        suite.addTestSuite(UUIDTest.class);
        suite.addTestSuite(VectorTest.class);
        suite.addTestSuite(WeakHashMapTest.class);

        return suite;
    }
}
