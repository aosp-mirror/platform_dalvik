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

package vogar;

/**
 * TestProperties is a common class of constants shared between the
 * Vogar on the host and TestRunner classes potentially running
 * on other devices.
 */
final public class TestProperties {

    /**
     * The name of the test properties file within the {@code .jar} file.
     */
    public static final String FILE = "test.properties";

    /**
     * Name of the property giving the test's main class name. This class should
     * have a {@code public static void main(String[] args)} method.
     */
    public static final String TEST_CLASS = "testClass";

    /**
     * Name of the property giving the test's name, such as {@code
     * java.math.BigDecimal.PowTests}.
     */
    public static final String QUALIFIED_NAME = "qualifiedName";

    /**
     * Name of the property used by TestRunner to determine which
     * class to use as the Runner name. This class should implement
     * Runner.
     */
    public static final String RUNNER_CLASS = "runnerClass";

    /**
     * Name of the property used by TestActivity to the test directory.
     */
    public static final String DEVICE_RUNNER_DIR = "deviceRunnerDir";

    /**
     * Port to accept monitor connections on.
     */
    public static final String MONITOR_PORT = "monitorPort";

    /**
     * Result value for successful test
     */
    public static final String RESULT_SUCCESS = "SUCCESS";

    /**
     * Result value for failed test
     */
    public static final String RESULT_FAILURE = "FAILURE";

    public static String result(boolean success) {
        return success ? RESULT_SUCCESS : RESULT_FAILURE;
    }

    /**
     * This class should not be instantiated
     */
    private TestProperties() {}
}
