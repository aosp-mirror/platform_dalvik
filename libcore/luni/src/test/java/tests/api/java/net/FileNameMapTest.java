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

import java.net.FileNameMap;
import java.net.URLConnection;

@TestTargetClass(FileNameMap.class) 
public class FileNameMapTest extends TestCase {

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getContentTypeFor",
        args = {java.lang.String.class}
    )
    public void test_getContentTypeFor() {
        String [] files = {"text", "txt", "htm", "html"}; 
        
        String [] mimeTypes = {"text/plain", "text/plain", 
                "text/html", "text/html"}; 
        
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        
        for(int i = 0; i < files.length; i++) {
            String mimeType = fileNameMap.getContentTypeFor("test." + files[i]);
            assertEquals("getContentTypeFor returns incorrect MIME type for " +
                    files[i], mimeTypes[i], mimeType);
        }
    } 
}

