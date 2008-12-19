/*
 * Copyright (C) 2007 The Android Open Source Project
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

package dalvik.system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Induces optimization/verification of a set of DEX files.
 *
 * TODO: This class is public, so SystemServer can access it.  This is NOT
 * the correct long-term solution; once we have a real installer and/or
 * dalvik-cache manager, this class should be removed.
 * 
 * @cts See to-do about removing this class...
 * 
 * @since Android 1.0
 */
public class TouchDex {

    /**
     * Forks a process, makes sure the DEX files are prepared, and returns
     * when everything is finished.
     * <p>
     * The filenames must be the same as will be used when the files are
     * actually opened, because the dalvik-cache filename is based upon
     * this filename.  (The absolute path to the JAR/ZIP/APK should work.)
     *
     * @param dexFiles a colon-separated list of DEX files.
     * @return zero on success
     * 
     * @cts What about error cases?
     */
    public static int start(String dexFiles) {
        return trampoline(dexFiles, System.getProperty("java.boot.class.path"));
    }

    /**
     * This calls fork() and then, in the child, calls cont(dexFiles).
     *
     * @param dexFiles Colon-separated list of DEX files.
     * @return zero on success
     */
    native private static int trampoline(String dexFiles, String bcp);

    /**
     * The entry point for the child process. args[0] can be a colon-separated
     * path list, or "-" to read from stdin.
     * <p>
     * Alternatively, if we're invoked directly from the command line we
     * just start here (skipping the fork/exec stuff).
     *
     * @param args command line args
     */
    public static void main(String[] args) {

        if ("-".equals(args[0])) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(System.in), 256);

            String line;
            try {
                while ((line = in.readLine()) != null) {
                    prepFiles(line);
                }
            } catch (IOException ex) {
                throw new RuntimeException ("Error processing stdin");
            }
        } else {
            prepFiles(args[0]);
        }

        System.out.println(" Prep complete");
    }


    private static String expandDirectories(String dexPath) {
        String[] parts = dexPath.split(":");
        StringBuilder outPath = new StringBuilder(dexPath.length());

        // A filename filter accepting *.jar and *.apk
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar") || name.endsWith(".apk");
            }
        };

        for (String part: parts) {
            File f = new File(part);

            if (f.isFile()) {
                outPath.append(part);
                outPath.append(':');
            } else if (f.isDirectory()) {
                String[] filenames = f.list(filter);

                if (filenames == null) {
                    System.err.println("I/O error with directory: " + part);
                    continue;
                }

                for (String filename: filenames) {
                    outPath.append(part);
                    outPath.append(File.separatorChar);
                    outPath.append(filename);
                    outPath.append(':');
                }
            } else {
                System.err.println("File not found: " + part);
            }
        }


        return outPath.toString();
    }

    private static void prepFiles(String dexPath) {

        System.out.println(" Prepping: " + dexPath);

        TouchDexLoader loader
                = new TouchDexLoader(expandDirectories(dexPath), null);

        try {
            /* By looking for a nonexistent class, we'll trick TouchDexLoader
             * into trying to load something from every file on dexPath,
             * optimizing all of them as a side-effect.
             *
             * The optimization happens implicitly in the VM the first time
             * someone tries to load a class from an unoptimized dex file.
             */
            loader.loadClass("com.google.NonexistentClassNeverFound");
            throw new RuntimeException("nonexistent class loaded?!");
        } catch (ClassNotFoundException cnfe) {
            //System.out.println("got expected dnfe");
        }
    }
}

