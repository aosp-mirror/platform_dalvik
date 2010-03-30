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

package vogar.commands;

import vogar.Classpath;
import vogar.Md5Cache;
import vogar.Strings;

import java.io.File;
import java.util.logging.Logger;

/**
 * A dx command.
 */
public final class Dx {
    private static final Logger logger = Logger.getLogger(Dx.class.getName());
    private static final Md5Cache DEX_CACHE = new Md5Cache("dex");

    /**
     * Converts all the .class files on 'classpath' into a dex file written to 'output'.
     */
    public void dex(File output, Classpath classpath) {
        File key = DEX_CACHE.makeKey(classpath);
        if (key != null && key.exists()) {
            logger.fine("dex cache hit for " + classpath);
            new Command.Builder().args("cp", key, output).execute();
            return;
        }
        /*
         * We pass --core-library so that we can write tests in the
         * same package they're testing, even when that's a core
         * library package. If you're actually just using this tool to
         * execute arbitrary code, this has the unfortunate
         * side-effect of preventing "dx" from protecting you from
         * yourself.
         *
         * Memory options pulled from build/core/definitions.mk to
         * handle large dx input when building dex for APK.
         */
        new Command.Builder()
                .args("dx")
                .args("-JXms16M")
                .args("-JXmx1536M")
                .args("--dex")
                .args("--output=" + output)
                .args("--core-library")
                .args(Strings.objectsToStrings(classpath.getElements()))
                .execute();
        DEX_CACHE.insert(key, output);
    }
}
