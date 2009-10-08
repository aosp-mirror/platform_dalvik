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

import java.util.Collection;
import java.util.List;

/**
 * Factory for filesystem commands.
 */
class Filesystem {

    public void move(String source, String target) {
        new Command("mv", source, target).execute();
    }

    /**
     * Moves all of the files in {@code source} to {@code target}, one at a
     * time. Unlike {@code move}, this approach works even if the target
     * directory is nonempty.
     */
    public int moveContents(String source, String target) {
        return copyContents(true, source, target);
    }

    /**
     * Copies all of the files in {@code source} to {@code target}, one at a
     * time. Unlike {@code move}, this approach works even if the target
     * directory is nonempty.
     */
    public int copyContents(String source, String target) {
        return copyContents(false, source, target);
    }

    private int copyContents(boolean move, String source, String target) {
        List<String> files = new Command("find", source, "-type", "f") .execute();
        for (String file : files) {
            String targetFile = target + "/" + file.substring(source.length());
            mkdir(parent(targetFile));
            if (move) {
                new Command("mv", "-i", file, targetFile).execute();
            } else {
                new Command("cp", file, targetFile).execute();
            }
        }
        return files.size();
    }

    private String parent(String file) {
        return file.substring(0, file.lastIndexOf('/'));
    }

    public void mkdir(String dir) {
        new Command("mkdir", "-p", dir).execute();
    }

    public List<String> find(String where, String name) {
        return new Command("find", where, "-name", name).execute();
    }

    public void rm(Collection<String> files) {
        new Command.Builder().args("rm", "-r").args(files).execute();
    }

    public void rm(String file) {
        new Command("rm", "-r", file).execute();
    }
}
