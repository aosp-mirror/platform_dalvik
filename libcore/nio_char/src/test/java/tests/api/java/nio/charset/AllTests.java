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

package tests.api.java.nio.charset;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllTests.suite());
    }

    public static Test suite() {
        TestSuite suite = tests.TestSuiteFactory.createTestSuite("All tests for package tests.api.java.nio.charset;");

        suite.addTestSuite(CharsetProviderTest.class);
        suite.addTestSuite(CharsetTest.class);
        suite.addTestSuite(CharsetDecoderTest.class);
        suite.addTestSuite(CharsetEncoderTest.class);
        suite.addTestSuite(CoderResultTest.class);
        suite.addTestSuite(CodingErrorActionTest.class);

        suite.addTestSuite(ASCCharsetDecoderTest.class);
        suite.addTestSuite(ASCCharsetTest.class);
// GBCharset not supported
//        suite.addTestSuite(GBCharsetDecoderTest.class);
//        suite.addTestSuite(GBCharsetEncoderTest.class);
        suite.addTestSuite(ISOCharsetDecoderTest.class);
        suite.addTestSuite(ISOCharsetEncoderTest.class);
        suite.addTestSuite(ISOCharsetTest.class);
        suite.addTestSuite(UTF16BECharsetDecoderTest.class);
        suite.addTestSuite(UTF16BECharsetEncoderTest.class);
        suite.addTestSuite(UTF16BECharsetTest.class);
        suite.addTestSuite(UTF16CharsetDecoderTest.class);
        suite.addTestSuite(UTF16CharsetEncoderTest.class);
        suite.addTestSuite(UTF16CharsetTest.class);
        suite.addTestSuite(UTF16LECharsetDecoderTest.class);
        suite.addTestSuite(UTF16LECharsetEncoderTest.class);
        suite.addTestSuite(UTF16LECharsetTest.class);
        suite.addTestSuite(UTF8CharsetTest.class);
        suite.addTestSuite(UTFCharsetDecoderTest.class);
        suite.addTestSuite(UTFCharsetEncoderTest.class);


        suite.addTestSuite(Charset_MultiByte_EUC_KR.class);
        suite.addTestSuite(Charset_MultiByte_UTF_8.class);
        suite.addTestSuite(Charset_MultiByte_UTF_16BE.class);
        suite.addTestSuite(Charset_MultiByte_UTF_16LE.class);

//      suite.addTestSuite(Charset_MultiByte_UTF_16.class);
        suite.addTestSuite(Charset_MultiByte_UTF_16_Android.class);
//      suite.addTestSuite(Charset_MultiByte_EUC_JP.class);
        suite.addTestSuite(Charset_MultiByte_EUC_JP_Android.class);
        suite.addTestSuite(Charset_MultiByte_ISO_2022_JP.class);  // IS HIDDENLY MAPPED TO ASCII OR WHAT?!?
//        suite.addTestSuite(Charset_MultiByte_ISO_2022_JP_Android.class);  // IS HIDDENLY MAPPED TO ASCII OR WHAT?!?

//      suite.addTestSuite(Charset_MultiByte_Big5.class);
        suite.addTestSuite(Charset_MultiByte_Big5_Android.class);
        suite.addTestSuite(Charset_MultiByte_x_windows_950.class);  // IS MAPPED TO Big5!!!
//        suite.addTestSuite(Charset_MultiByte_x_windows_950_Android.class);  // IS MAPPED TO Big5!!!

//      suite.addTestSuite(Charset_MultiByte_GBK.class);
        suite.addTestSuite(Charset_MultiByte_GBK_Android.class);
        suite.addTestSuite(Charset_MultiByte_GB2312.class);  // IS HIDDENLY MAPPED TO ASCII OR WHAT?!?
//        suite.addTestSuite(Charset_MultiByte_GB2312_Android.class);  // IS MAPPED TO GBK!!!

        suite.addTestSuite(Charset_SingleByte_US_ASCII.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_1.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_2.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_3.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_4.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_5.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_6.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_7.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_8.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_9.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_11.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_13.class);
        suite.addTestSuite(Charset_SingleByte_ISO_8859_15.class);
        suite.addTestSuite(Charset_SingleByte_IBM864.class);
        suite.addTestSuite(Charset_SingleByte_x_IBM874.class);
        suite.addTestSuite(Charset_SingleByte_windows_1250.class);
        suite.addTestSuite(Charset_SingleByte_windows_1251.class);
        suite.addTestSuite(Charset_SingleByte_windows_1252.class);
        suite.addTestSuite(Charset_SingleByte_windows_1253.class);
        suite.addTestSuite(Charset_SingleByte_windows_1254.class);
        suite.addTestSuite(Charset_SingleByte_windows_1255.class);
        suite.addTestSuite(Charset_SingleByte_windows_1256.class);
        suite.addTestSuite(Charset_SingleByte_windows_1257.class);
        suite.addTestSuite(Charset_SingleByte_windows_1258.class);
        suite.addTestSuite(Charset_SingleByte_KOI8_R.class);

        // NOT SUPPORTED BY RI:
        suite.addTestSuite(Charset_ISO_8859_10.class);
        suite.addTestSuite(Charset_ISO_8859_14.class);
        suite.addTestSuite(Charset_ISO_8859_16.class);
        suite.addTestSuite(Charset_macintosh.class);
        suite.addTestSuite(Charset_GSM0338.class);

        return suite;
    }
}
