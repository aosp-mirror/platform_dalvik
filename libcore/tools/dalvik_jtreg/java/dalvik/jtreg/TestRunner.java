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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Runs a jtreg test.
 */
public abstract class TestRunner {

    /**
     * The name of the test properties file within the {@code .jar} file.
     */
    static final String TEST_PROPERTIES_FILE = "test.properties";

    /**
     * Property identifier for the test's main class name. This class should
     * have a {@code public static void main(String[] args)} method.
     */
    static final String CLASS_NAME = "className";

    /**
     * Property identifier for the test's name, such as {@code
     * java.math.BigDecimal.PowTests}.
     */
    static final String QUALIFIED_NAME = "qualifiedName";

    protected String className;
    protected String qualifiedName;

    protected Properties loadProperties() {
        Properties properties = new Properties();
        try {
            InputStream propertiesStream = TestRunner.class.getResourceAsStream(
                    "/" + TEST_PROPERTIES_FILE);
            if (propertiesStream == null) {
                throw new RuntimeException(TEST_PROPERTIES_FILE + " missing!");
            }

            properties.load(propertiesStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        className = properties.getProperty(CLASS_NAME);
        qualifiedName = properties.getProperty(QUALIFIED_NAME);
        return properties;
    }

    public void prepareTest() {}

    public abstract boolean test();

    public void run() {
        loadProperties();
        prepareTest();

        System.out.println("Executing " + qualifiedName);
        boolean success = test();
        System.out.println(success ? "SUCCESS" : "FAILURE");
    }
}
