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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A logical unit of code shared between Apache Harmony and Dalvik.
 */
class Module {

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
