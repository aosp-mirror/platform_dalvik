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

import java.util.List;
import java.util.UUID;

/**
 * Download two versions of Apache Harmony from their SVN version, and use it
 * to perform a three-way merge with Dalvik.
 */
public class PullHarmonyCode {

    private final int currentVersion;
    private final int targetVersion;

    public PullHarmonyCode(int currentVersion, int targetVersion) {
        this.currentVersion = currentVersion;
        this.targetVersion = targetVersion;
    }

    public void pull(Module module) {
        String path = module.path();
        String svnOldBranch = path + "_" + currentVersion;
        String svnNewBranch = path + "_" + targetVersion;
        String dalvikBranch = path + "_dalvik";

        Git git = new Git();
        Filesystem filesystem = new Filesystem();
        Svn svn = new Svn();

        // Assume we're starting with the current Dalvik code. Tuck this away
        // somewhere while we rewrite history.
        String temp = "/tmp/" + UUID.randomUUID();
        filesystem.mkdir(temp);

        // To prepare a three-way-merge, we need a common starting point: the
        // time at which Dalvik and Harmony were most the same. We'll use the
        // previous Harmony SVN code as this starting point. We grab the old
        // code from their repository, and commit it as a git branch.
        System.out.print("Creating branch " + svnOldBranch + "...");
        git.branch(svnOldBranch);
        filesystem.move(path, temp + "/" + path);
        svn.checkOut(currentVersion, module.getSvnBaseUrl() + "/" + path);
        filesystem.rm(filesystem.find(path, ".svn"));
        for (MappedDirectory mappedDirectory : module.getMappedDirectories()) {
            filesystem.moveContents(mappedDirectory.svnPath(), mappedDirectory.gitPath());
        }
        git.rm(git.listDeleted());
        git.add(path);
        git.commit(svnOldBranch);
        System.out.println("done");

        // Create a branch that's derived from the starting point. It will
        // contain all of the changes Harmony has made from then until now.
        System.out.print("Creating branch " + svnNewBranch + "...");
        git.branch(svnNewBranch, svnOldBranch);
        filesystem.rm(path);
        svn.checkOut(targetVersion, module.getSvnBaseUrl() + "/" + path);
        filesystem.rm(filesystem.find(path, ".svn"));
        for (MappedDirectory mappedDirectory : module.getMappedDirectories()) {
            filesystem.moveContents(mappedDirectory.svnPath(), mappedDirectory.gitPath());
        }
        git.rm(git.listDeleted());
        git.add(path);
        git.commit(svnNewBranch);
        System.out.println("done");

        // Create another branch that's derived from the starting point. It will
        // contain all of the changes Dalvik has made from then until now.
        System.out.print("Creating branch " + dalvikBranch + "...");
        git.branch(dalvikBranch, svnOldBranch);
        filesystem.rm(path);
        filesystem.move(temp + "/" + path, path);
        git.rm(git.listDeleted());
        git.add(path);
        git.commit(dalvikBranch);
        System.out.println("done");

        // Merge the two sets of changes together: Harmony's and Dalvik's. By
        // initializing a common starting point, git can make better decisions
        // when the two new versions differ. For example, if today's Dalvik has
        // a method that today's Harmony does not, it may be because Dalvik
        // added it, or because Harmony deleted it!
        System.out.println("Merging " + svnNewBranch + " into " + dalvikBranch + ":");
        List<String> mergeResults = git.merge(svnNewBranch);
        for (String mergeResult : mergeResults) {
            System.out.print("  ");
            System.out.println(mergeResult);
        }
    }

    public static void main(String[] args) {
//        new PullHarmonyCode(527399, 802921).pull(Modules.CRYPTO);
        new PullHarmonyCode(772995, 802921).pull(Modules.ARCHIVE);
    }
}
