/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.math;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite that includes all tests for the Math project.
 */
public class AllTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllTests.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("All Math test suites");

        suite.addTest(tests.api.java.math.AllTests.suite());

        suite.addTestSuite(org.apache.harmony.tests.java.math.BigDecimalArithmeticTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigDecimalCompareTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigDecimalConstructorsTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigDecimalConvertTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigDecimalScaleOperationsTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerAddTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerAndTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerCompareTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerConstructorsTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerConvertTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerDivideTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerHashCodeTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerModPowTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerMultiplyTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerNotTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerOperateBitsTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerOrTest.class);
        // suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerProbablePrimeTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerSubtractTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerToStringTest.class);
        suite.addTestSuite(org.apache.harmony.tests.java.math.BigIntegerXorTest.class);
        // suite.addTestSuite(org.apache.harmony.tests.java.math.MathContextConstructorsTest.class);
        // suite.addTestSuite(org.apache.harmony.tests.java.math.MathContextMethodsTest.class);
        return suite;
    }
}
