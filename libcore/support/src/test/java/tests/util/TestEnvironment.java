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

package tests.util;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Prepares system properties and preferences to hygienic values for testing:
 * <ul>
 *   <li>System properties are set to reasonable defaults. Referenced
 *       directories such as the user and Java home directories will be
 *       writable.
 *   <li>System and user preferences are cleared. Once the preferences classes
 *       have been initialized, the path where their data is stored is fixed.
 *       For this reason, every test that reads or writes preferences should
 *       first reset the system configuration with this API.
 * </ul>
 *
 * <p>Use this class to clean up before and/or after your test. Sample usage:
 * <pre>
 * public void MyTest extends TestCase {
 *
 *     protected void setUp() throws Exception {
 *         super.setUp();
 *         TestEnvironment().reset();
 *     }
 *
 *     protected void tearDown() throws Exception {
 *         TestEnvironment().reset();
 *         super.tearDown();
 *     }
 *
 *     ...
 * }</pre>
 */
public final class TestEnvironment {
    private TestEnvironment() {}

    public static synchronized void reset() {
        resetSystemProperties();
        resetPreferences();
        resetDefaultLocale();
        resetDefaultTimeZone();
        // TODO: SecurityManager?
    }

    private static void resetDefaultLocale() {
        // This is hard-coded in resetSystemProperties, so no need to be clever here.
        Locale.setDefault(Locale.US);
    }

    private static void resetDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    private static void resetSystemProperties() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir == null) {
            throw new IllegalStateException("Test execution requires the"
                    + " system property java.io.tmpdir to be set.");
        }

        Properties p = new Properties();

        // runtime properties that we never want to clobber
        copyProperty(p, "android.vm.dexfile");
        copyProperty(p, "java.boot.class.path");
        copyProperty(p, "java.class.path");
        copyProperty(p, "java.io.tmpdir");
        copyProperty(p, "java.library.path");
        copyProperty(p, "os.arch");
        copyProperty(p, "os.name");
        copyProperty(p, "os.version");

        // paths with writable values for testing
        String userHome = tmpDir + "/user.home";
        String javaHome = tmpDir + "/java.home";
        String userDir = tmpDir + "/user.dir";
        makeDirectory(new File(userHome));
        makeDirectory(new File(javaHome));
        makeDirectory(new File(userDir));
        p.put("java.home", javaHome);
        p.put("user.dir", userDir);
        p.put("user.home", userHome);

        // hardcoded properties
        p.put("file.encoding", "UTF-8");
        p.put("file.separator", "/");
        p.put("java.class.version", "46.0");
        p.put("java.compiler", "");
        p.put("java.ext.dirs", "");
        p.put("java.net.preferIPv6Addresses", "true");
        p.put("java.runtime.name", "Android Runtime");
        p.put("java.runtime.version", "0.9");
        p.put("java.specification.name", "Dalvik Core Library");
        p.put("java.specification.vendor", "The Android Project");
        p.put("java.specification.version", "0.9");
        p.put("java.vendor", "The Android Project");
        p.put("java.vendor.url", "http://www.android.com/");
        p.put("java.version", "0");
        p.put("java.vm.name", "Dalvik");
        p.put("java.vm.specification.name", "Dalvik Virtual Machine Specification");
        p.put("java.vm.specification.vendor", "The Android Project");
        p.put("java.vm.specification.version", "0.9");
        p.put("java.vm.vendor", "The Android Project");
        p.put("java.vm.vendor.url", "http://www.android.com/");
        p.put("java.vm.version", "1.2.0");
        p.put("javax.net.ssl.trustStore", "/etc/security/cacerts.bks");
        p.put("line.separator", "\n");
        p.put("path.separator", ":");
        p.put("user.language", "en");
        p.put("user.name", "");
        p.put("user.region", "US");

        System.setProperties(p);
    }

    private static void copyProperty(Properties p, String key) {
        p.put(key, System.getProperty(key));
    }

    private static void makeDirectory(File path) {
        boolean success;
        if (!path.exists()) {
            success = path.mkdirs();
        } else if (!path.isDirectory()) {
            success = path.delete() && path.mkdirs();
        } else {
            success = true;
        }

        if (!success) {
            throw new RuntimeException("Failed to make directory " + path);
        }
    }

    private static void resetPreferences() {
        try {
            for (Preferences root : Arrays.asList(
                    Preferences.systemRoot(), Preferences.userRoot())) {
                for (String child : root.childrenNames()) {
                    root.node(child).removeNode();
                }
                root.clear();
                root.flush();
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
