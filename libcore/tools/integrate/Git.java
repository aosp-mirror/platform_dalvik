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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for git commands.
 */
class Git {

    private static final Pattern STATUS_DELETED
            = Pattern.compile("#\\tdeleted:    (.*)");

    public void branch(String newBranch) {
        branch(newBranch, "HEAD");
    }

    /**
     * @param base another branch, or a revision identifier like {@code HEAD}.
     */
    public void branch(String newBranch, String base) {
        // -b is used by git to branch from another checkout
        new Command("git", "checkout", "-b", newBranch, base).execute();
    }

    public void commit(String message) {
        new Command("git", "commit", "-m", message).execute();
    }

    public void add(String path) {
        new Command("git", "add", path).execute();
    }

    public void remove(Collection<String> paths) {
        new Command.Builder().args("git", "rm").args(paths).execute();
    }

    public List<String> merge(String otherBranch) {
        return new Command.Builder()
                .args("git", "merge", "-s", "recursive", otherBranch)
                .permitNonZeroExitStatus()
                .execute();
    }

    /**
     * Returns the files that have been deleted from the filesystem, but that
     * don't exist in the active git change.
     */
    public List<String> listDeleted() {
        List<String> statusLines = new Command.Builder()
                .args("git", "status")
                .permitNonZeroExitStatus()
                .execute();

        List<String> deletedFiles = new ArrayList<String>();
        Matcher matcher = STATUS_DELETED.matcher("");
        for (String line : statusLines) {
            matcher.reset(line);
            if (matcher.matches()) {
                deletedFiles.add(matcher.group(1));
            }
        }
        return deletedFiles;
    }

    public void rm(List<String> files) {
        new Command.Builder()
                .args("git", "rm").args(files)
                .permitNonZeroExitStatus()
                .build()
                .execute();
    }
}
