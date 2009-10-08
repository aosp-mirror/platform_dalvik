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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A logical unit of code shared between Apache Harmony and Dalvik.
 */
class Module {

    static final Map<String, Module> VALUES;
    static {
        Map<String, Module> valuesMutable = new LinkedHashMap<String, Module>();

        String svnRoot = "http://svn.apache.org/repos/asf/harmony/enhanced/classlib/trunk/modules";
        valuesMutable.put("archive", new Module.Builder(svnRoot, "archive")
                .mapDirectory("archive/src/main/native/archive/shared",
                        "archive/src/main/native")
                .mapDirectory("archive/src/main/native/zip/shared",
                        "archive/src/main/native")
                .build());

        valuesMutable.put("crypto", new Module.Builder(svnRoot, "crypto")
                .mapDirectory("crypto/src/test/api/java.injected/javax",
                        "crypto/src/test/java/org/apache/harmony/crypto/tests/javax")
                .mapDirectory("crypto/src/test/api/java",
                        "crypto/src/test/java")
                .mapDirectory("crypto/src/test/resources/serialization",
                        "crypto/src/test/java/serialization")
                .mapDirectory("crypto/src/test/support/common/java",
                        "crypto/src/test/java")
                .build());

        valuesMutable.put("regex", new Module.Builder(svnRoot, "regex").build());

        valuesMutable.put("security", new Module.Builder(svnRoot, "security")
                .mapDirectory("security/src/main/java/common",
                        "security/src/main/java")
                .mapDirectory("security/src/main/java/unix/org",
                        "security/src/main/java/org")
                .mapDirectory("security/src/test/api/java",
                        "security/src/test/java")
                .build());

        valuesMutable.put("text", new Module.Builder(svnRoot, "text").build());

        valuesMutable.put("x-net", new Module.Builder(svnRoot, "x-net").build());

        VALUES = Collections.unmodifiableMap(valuesMutable);
    }

    private final String svnBaseUrl;
    private final String path;
    private final Set<MappedDirectory> mappedDirectories;

    public String getSvnBaseUrl() {
        return svnBaseUrl;
    }

    public String path() {
        return path;
    }

    public Set<MappedDirectory> getMappedDirectories() {
        return mappedDirectories;
    }

    private Module(Builder builder) {
        this.svnBaseUrl = builder.svnBaseUrl;
        this.path = builder.path;
        this.mappedDirectories = new LinkedHashSet<MappedDirectory>(builder.mappedDirectories);
    }

    public static class Builder {
        private final String svnBaseUrl;
        private final String path;
        private final Set<MappedDirectory> mappedDirectories
                = new LinkedHashSet<MappedDirectory>();

        public Builder(String svnBaseUrl, String path) {
            this.svnBaseUrl = svnBaseUrl;
            this.path = path;
        }

        public Builder mapDirectory(String svnPath, String gitPath) {
            mappedDirectories.add(new MappedDirectory(svnPath, gitPath));
            return this;
        }

        public Module build() {
            return new Module(this);
        }
    }
}
