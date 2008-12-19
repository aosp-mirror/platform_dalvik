/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

//TODO :
/*
 * 1. Don't forget to write tests for org.apache.harmony.logging.internal.nls/Messages.java this file is in logging/src/main/java folder
 * 2. inrteface filter / LoggingMXBean tests machen
 * 3. XMLFormatter.java should be finish for Monday (not a lot to do) but as I beginn want to finish.  
 * 3. In my case
 *    I didn't use the PARTIAL_OK, so I believe that 98% of COMPLETE are PARTIAL_OK
 *    COMPLETE = Tests finish and should be working. If error check the test before to make a ticket.
 *    PARTIAL = Tests finish, but need special reviewing
 *    TODO = A test to do (or not). Mostly a test to complete
 * 4. For questions christian.wiederseiner
 */

package org.apache.harmony.logging.tests.java.util.logging;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import junit.framework.TestCase;

/**
 * This testcase verifies the signature of the interface Filter.
 * 
 */
@TestTargetClass(Filter.class) 
public class FilterTest extends TestCase {
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "Verifies interface.",
      targets = {
        @TestTarget(
          methodName = "isLoggable",
          methodArgs = {java.util.logging.LogRecord.class}
        )
    })
    public void testFilter() {
        MockFilter f = new MockFilter();
        assertFalse(f.isLoggable(null));
    }

    /*
     * This inner class implements the interface Filter to verify the signature.
     */
    private class MockFilter implements Filter {

        public boolean isLoggable(LogRecord record) {
            return false;
        }
    }
}
