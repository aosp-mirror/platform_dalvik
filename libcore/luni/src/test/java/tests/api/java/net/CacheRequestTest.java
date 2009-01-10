/*
 * Copyright (C) 2008 The Android Open Source Project
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

package tests.api.java.net;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.OutputStream;
import java.net.CacheRequest;

@TestTargetClass(CacheRequest.class) 
public class CacheRequestTest extends TestCase {
    
       
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "abort",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CacheRequest",
            args = {}
        )
    })
    public void test_abort() {
        MockCacheRequest mcr = new MockCacheRequest();
        mcr.abort();
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getBody",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CacheRequest",
            args = {}
        )
    })
    public void test_getBody() throws IOException {
        MockCacheRequest mcr = new MockCacheRequest();
        assertNull(mcr.getBody());
    }
    
    class MockCacheRequest extends CacheRequest {
        
        MockCacheRequest() {
            super();
        }

        @Override
        public void abort() {
        }

        @Override
        public OutputStream getBody() throws IOException {
            return null;
        }
    }
}
