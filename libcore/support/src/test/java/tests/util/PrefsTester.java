/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.util;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.Arrays;

/**
 * Prepares the shared preferences store for a test by wiping preference data
 * before and after the test. Sample usage:
 * <pre>
 * public void MyPreferencesTest extends TestCase {
 *     private final PrefsTester prefsTester = new PrefsTester();
 *
 *     public void setUp() throws BackingStoreException {
 *         super.setUp();
 *         prefsTester.setUp();
 *     }
 *
 *     public void tearDown() throws BackingStoreException {
 *         prefsTester.tearDown();
 *         super.tearDown();
 *     }
 *
 *     ...
 * }</pre>
 *
 * <p>Once the preferences classes have been initialized, the path where their
 * data is stored is fixed. For that reason, every test that reads or writes
 * preferences must first prepare preferences for testing by using this class.
 */
public final class PrefsTester {

    static {
        String tmp = System.getProperty("java.io.tmpdir");
        System.setProperty("user.home", tmp);
        System.setProperty("java.home", tmp);
    }

    public void setUp() throws BackingStoreException {
        clear();
    }

    public void tearDown() throws BackingStoreException {
        clear();
    }

    private void clear() throws BackingStoreException {
        for (Preferences root : Arrays .asList(
                Preferences.systemRoot(), Preferences.userRoot())) {
            for (String child : root.childrenNames()) {
                root.node(child).removeNode();
            }
            root.clear();
            root.flush();
        }
    }
}
