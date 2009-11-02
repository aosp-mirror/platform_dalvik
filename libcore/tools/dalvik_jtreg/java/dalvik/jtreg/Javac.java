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
 * A javac command.
 */
final class Javac {

    private final Command.Builder builder = new Command.Builder();

    Javac() {
        builder.args("javac", "-Xmaxerrs", "1");
    }

    public Javac bootClasspath(File... path) {
        builder.args("-bootclasspath", Command.path(path));
        return this;
    }

    public Javac classpath(File... path) {
        builder.args("-classpath", Command.path(path));
        return this;
    }

    public Javac sourcepath(File... path) {
        builder.args("-sourcepath", Command.path(path));
        return this;
    }

    public Javac destination(File directory) {
        builder.args("-d", directory.toString());
        return this;
    }

    public List<String> compile(File... files) {
        return builder.args(Strings.objectsToStrings(files))
                .execute();
    }
}
