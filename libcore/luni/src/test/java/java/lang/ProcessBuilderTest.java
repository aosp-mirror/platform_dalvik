/*
 * Copyright (C) 2009 The Android Open Source Project
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

package java.lang;

import static tests.support.Support_Exec.execAndCheckOutput;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.InputStream;
import java.util.Arrays;

public class ProcessBuilderTest extends junit.framework.TestCase {
    private static String shell() {
        return "Dalvik".equals(System.getProperty("java.vm.name")) ? "/system/bin/sh" : "/bin/sh";
    }

    public void testRedirectErrorStream(boolean doRedirect,
            String expectedOut, String expectedErr) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(shell(), "-c", "echo out; echo err 1>&2");
        pb.redirectErrorStream(doRedirect);
        execAndCheckOutput(pb, expectedOut, expectedErr);
    }

    public void test_redirectErrorStream_true() throws Exception {
        testRedirectErrorStream(true, "out\nerr\n", "");
    }

    public void test_redirectErrorStream_false() throws Exception {
        testRedirectErrorStream(false, "out\n", "err\n");
    }
}
