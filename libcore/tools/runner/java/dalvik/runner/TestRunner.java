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
public class TestRunner {

    protected final Properties properties;

    protected final String qualifiedName;
    protected final Class<?> testClass;
    protected final Class<?> runnerClass;

    protected TestRunner () {
        properties = loadProperties();
        qualifiedName = properties.getProperty(TestProperties.QUALIFIED_NAME);
        testClass = classProperty(TestProperties.TEST_CLASS, Object.class);
        runnerClass = classProperty(TestProperties.RUNNER_CLASS, Runner.class);
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

    private Class<?> classProperty(String propertyName, Class<?> superClass) {
        String className = properties.getProperty(propertyName);
        if (className == null) {
            throw new IllegalArgumentException("Could not find property for " +
                                               propertyName);
        }
        try {
            Class<?> klass = Class.forName(className);
            if (!superClass.isAssignableFrom(klass)) {
                throw new IllegalArgumentException(
                        className + " can not be assigned to " + Runner.class);
            }
            return klass;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean run() {
        Runner runner;
        try {
            runner = (Runner) runnerClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        runner.prepareTest(testClass);
        return runner.test(testClass);
    }

    public static void main(String[] args) {
        if (args.length != 0) {
            throw new RuntimeException("TestRunner doesn't take arguments");
        }
        System.out.println(TestProperties.result(new TestRunner().run()));
    }
}
