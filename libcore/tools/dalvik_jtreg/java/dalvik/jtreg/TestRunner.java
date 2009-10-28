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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Runs a jtreg test that was prepared with {@link TestToDex}.
 */
public class TestRunner {

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

    /**
     * Property identifier for the test's title, such as {@code Some exponent
     * over/undeflow tests for the pow method}.
     */
    static final String TITLE = "title";

    /**
     * Property identifier for the comma-separated list of keywords, such as
     * { "bug4916097" }.
     */
    static final String KEYWORDS = "keywords";

    /**
     * Property identifier for the test's source directory, such as {@code
     * platform_v6/jdk/test/java/math/BigDecimal}.
     */
    static final String DIR = "dir";

    /**
     * The comma-separated source files, such as { "PowTests.java" }.
     */
    static final String SOURCES = "sources";

    private String className;
    private String qualifiedName;
    private String title;
    private String keywords;
    private String dir;

    private Method main;

    public void test(String[] args)
            throws InvocationTargetException, IllegalAccessException {
        System.err.println("Executing " + qualifiedName);
        try {
            main.invoke(null, new Object[] { args });
            System.err.println("SUCCESS.");
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.err.println("FAILURE!");
            System.err.println("  " + title);
            System.err.println("  " + keywords);
            System.err.println("  " + dir);
        }
    }

    private void loadProperties() {
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
        title = properties.getProperty(TITLE);
        keywords = properties.getProperty(KEYWORDS);
        dir = properties.getProperty(DIR);

        if (className == null || qualifiedName == null) {
            throw new RuntimeException(TEST_PROPERTIES_FILE + " missing required values!");
        }
    }

    private void prepareTest() {
        try {
            Class<?> testClass = Class.forName(className);
            main = testClass.getMethod("main", String[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        // Usage: TestRunner [test args]

        TestRunner testRunner = new TestRunner();
        testRunner.loadProperties();
        testRunner.prepareTest();
        testRunner.test(args);

        // TODO: report the results out as an XML file in JUnit format
    }
}
