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

package dalvik.runner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Runs a test.
 */
public abstract class TestRunner {

    /**
     * A static field that allows TestActivity to access the
     * underlying test result without depending on reading
     * System.out. This is necessary because TestRunner subclasses are
     * invoked via a tradtional static main method with a void return
     * type.
     */
    public static boolean success;

    protected final Properties properties;

    protected final String testClass;
    protected final String qualifiedName;

    protected TestRunner () {
        properties = loadProperties();
        testClass = properties.getProperty(TestProperties.TEST_CLASS);
        qualifiedName = properties.getProperty(TestProperties.QUALIFIED_NAME);
    }

    protected static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            InputStream propertiesStream = TestRunner.class.getResourceAsStream(
                    "/" + TestProperties.FILE);
            if (propertiesStream == null) {
                throw new RuntimeException(TestProperties.FILE + " missing!");
            }
            properties.load(propertiesStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void prepareTest() {}

    public abstract boolean test();

    public void run() {
        prepareTest();

        System.out.println("Executing " + qualifiedName);
        success = test();
        System.out.println(TestProperties.result(success));
    }
}
