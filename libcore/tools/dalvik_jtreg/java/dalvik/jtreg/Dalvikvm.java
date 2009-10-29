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

package dalvik.jtreg;

import java.io.File;
import java.util.List;

/**
 * A dalvikvm command.
 */
final class Dalvikvm {

    private final Command.Builder builder = new Command.Builder();

    public Dalvikvm() {
        builder.args("adb", "shell", "dalvikvm");
    }

    Dalvikvm classpath(File... files) {
        builder.args("-classpath");
        builder.args(Command.path(files));
        return this;
    }

    List<String> exec(String classname, String... args) {
        builder.args(classname);
        builder.args(args);
        return builder.execute();
    }

    public Dalvikvm args(String... args) {
        builder.args(args);
        return this;
    }
}
