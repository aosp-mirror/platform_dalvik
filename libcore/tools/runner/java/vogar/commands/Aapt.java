/*
 * Copyright (C) 2010 The Android Open Source Project
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

package vogar.commands;

import java.io.File;

/**
 * An aapt (Android Asset Packaging Tool) command.
 */
public final class Aapt {

    public void apk(File apk, File manifest) {

        // TODO: we should be able to work with a shipping SDK, not depend on out/...
        new Command.Builder()
                .args("aapt")
                .args("package")
                .args("-F")
                .args(apk)
                .args("-M")
                .args(manifest)
                .args("-I")
                .args("out/target/common/obj/APPS/framework-res_intermediates/package-export.apk")
                .execute();
    }
    public void add(File apk, File dex) {
        new Command.Builder()
                .args("aapt")
                .args("add")
                .args("-k")
                .args(apk)
                .args(dex)
                .execute();
    }
}
