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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.util.List;
import java.util.Map;

@TestTargetClass(CacheResponse.class) 
public class CacheResponseTest extends TestCase {

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBody",
        args = {}
    )
    public void test_getBody() throws IOException {
        MockCacheResponse mcr = new MockCacheResponse();
        assertNull(mcr.getBody());
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getHeaders",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "CacheResponse",
            args = {}
        )
    })
    public void test_getHeaders() throws IOException {
        MockCacheResponse mcr = new MockCacheResponse();
        assertNull(mcr.getHeaders());
    }
    
    class MockCacheResponse extends CacheResponse {

        MockCacheResponse() {
            super();
        }
        
        @Override
        public Map<String,List<String>> getHeaders() throws IOException {
            return null;
        }

        @Override
        public InputStream getBody() throws IOException {
            return null;
        }
    }    
}
