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

package tests.api.java.nio.charset;

import org.apache.harmony.nio_char.tests.java.nio.charset.ASCIICharsetEncoderTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for java.nio.charset package.
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for tests.api.java.nio.charset");
        // $JUnit-BEGIN$
        suite.addTestSuite(CodingErrorActionTest.class);
        suite.addTestSuite(CoderResultTest.class);
        suite.addTestSuite(CharsetTest.class);
        suite.addTestSuite(ASCCharsetTest.class);
        suite.addTestSuite(ISOCharsetTest.class);
        suite.addTestSuite(UTF8CharsetTest.class);
        suite.addTestSuite(UTF16CharsetTest.class);
        suite.addTestSuite(UTF16BECharsetTest.class);
        suite.addTestSuite(UTF16LECharsetTest.class);
        suite.addTestSuite(CharsetEncoderTest.class);
        suite.addTestSuite(ISOCharsetEncoderTest.class);
        suite.addTestSuite(UTFCharsetEncoderTest.class);
        // GBCharset not supported
        // suite.addTestSuite(GBCharsetEncoderTest.class);
        suite.addTestSuite(ASCIICharsetEncoderTest.class);
        suite.addTestSuite(UTF16CharsetEncoderTest.class);
        suite.addTestSuite(UTF16LECharsetEncoderTest.class);
        suite.addTestSuite(UTF16BECharsetEncoderTest.class);
        suite.addTestSuite(CharsetDecoderTest.class);
        suite.addTestSuite(ISOCharsetDecoderTest.class);
        suite.addTestSuite(UTFCharsetDecoderTest.class);
        // GBCharset not supported
        // suite.addTestSuite(GBCharsetDecoderTest.class);
        suite.addTestSuite(ASCCharsetDecoderTest.class);
        suite.addTestSuite(UTF16CharsetDecoderTest.class);
        suite.addTestSuite(UTF16LECharsetDecoderTest.class);
        suite.addTestSuite(UTF16BECharsetDecoderTest.class);
        // $JUnit-END$
        return suite;
    }
}
