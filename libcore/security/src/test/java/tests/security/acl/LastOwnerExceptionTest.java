/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Aleksei Y. Semenov
* @version $Revision$
*/

package tests.security.acl;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.security.acl.LastOwnerException;

/**
 * Unit test for LastOwnerException.
 * 
 */
@TestTargetClass(LastOwnerException.class)
public class LastOwnerExceptionTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(LastOwnerExceptionTest.class);
    }
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "LastOwnerException",
          methodArgs = {}
        )
    })
    public void testLastOwnerException() {
        assertNotNull(new LastOwnerException());
        assertNull(new LastOwnerException().getMessage());
        assertNull(new LastOwnerException().getCause());
    }

}
