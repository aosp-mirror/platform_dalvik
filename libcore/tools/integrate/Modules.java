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

/**
 * Constants that define modules shared by Harmony and Dalvik.
 */
public class Modules {

    private static final String SVN_ROOT
            = "http://svn.apache.org/repos/asf/harmony/enhanced/classlib/trunk/modules";

    public static final Module ARCHIVE = new Module.Builder(SVN_ROOT, "archive")
            .mapDirectory("archive/src/main/native/archive/shared",
                    "archive/src/main/native")
            .mapDirectory("archive/src/main/native/zip/shared",
                    "archive/src/main/native")
            .build();

    public static final Module CRYPTO = new Module.Builder(SVN_ROOT, "crypto")
            .mapDirectory("crypto/src/test/api/java.injected/javax",
                    "crypto/src/test/java/org/apache/harmony/crypto/tests/javax")
            .mapDirectory("crypto/src/test/api/java",
                    "crypto/src/test/java")
            .mapDirectory("crypto/src/test/resources/serialization",
                    "crypto/src/test/java/serialization")
            .mapDirectory("crypto/src/test/support/common/java",
                    "crypto/src/test/java")
            .build();

    public static final Module REGEX
            = new Module.Builder(SVN_ROOT, "regex").build();

    public static final Module SECURITY = new Module.Builder(SVN_ROOT, "security")
            .mapDirectory("security/src/main/java/common",
                    "security/src/main/java")
            .mapDirectory("security/src/main/java/unix/org",
                    "security/src/main/java/org")
            .mapDirectory("security/src/test/api/java",
                    "security/src/test/java")
            .build();

    public static final Module TEXT
            = new Module.Builder(SVN_ROOT, "text").build();

    public static final Module X_NET
            = new Module.Builder(SVN_ROOT, "x-net").build();

    // TODO: add the other modules
}
