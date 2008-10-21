/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.text.tests.java.text;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllTests.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Suite org.apache.harmony.text.tests.java.text");
        //$JUnit-BEGIN$
        suite.addTestSuite(AnnotationTest.class);
        suite.addTestSuite(AttributedCharacterIteratorAttributeTest.class);
        suite.addTestSuite(AttributedCharacterIteratorTest.class);
        suite.addTestSuite(AttributedStringTest.class);
        suite.addTestSuite(BidiTest.class);
        suite.addTestSuite(BreakIteratorTest.class);
        suite.addTestSuite(ChoiceFormatTest.class);
        suite.addTestSuite(CollationElementIteratorTest.class);
        suite.addTestSuite(CollationKeyTest.class);
        suite.addTestSuite(CollatorTest.class);
        suite.addTestSuite(DataFormatFieldTest.class);
        suite.addTestSuite(DateFormatSymbolsTest.class);
        suite.addTestSuite(DateFormatTest.class);
        suite.addTestSuite(DecimalFormatSymbolsTest.class);
        suite.addTestSuite(DecimalFormatTest.class);
        suite.addTestSuite(FieldPositionTest.class);
        suite.addTestSuite(FormatFieldTest.class);
        suite.addTestSuite(FormatTest.class);
        suite.addTestSuite(MessageFormatFieldTest.class);
        suite.addTestSuite(MessageFormatTest.class);
        suite.addTestSuite(NumberFormatFieldTest.class);
        suite.addTestSuite(NumberFormatTest.class);
        suite.addTestSuite(ParseExceptionTest.class);
        suite.addTestSuite(ParsePositionTest.class);
        suite.addTestSuite(RuleBasedCollatorTest.class);
        suite.addTestSuite(SimpleDateFormatTest.class);
        suite.addTestSuite(StringCharacterIteratorTest.class);
        //$JUnit-END$
        return suite;
    }
}
