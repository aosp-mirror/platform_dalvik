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

import com.sun.javatest.TestDescription;
import com.sun.javatest.TestResult;
import com.sun.javatest.TestResultTable;
import com.sun.javatest.TestSuite;
import com.sun.javatest.WorkDirectory;
import com.sun.javatest.regtest.RegressionTestSuite;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Create {@link TestRun}s for {@code .java} files with jtreg tests in them.
 */
class JtregFinder implements CodeFinder {

    // TODO: add support for the  @library directive, as seen in
    //   test/com/sun/crypto/provider/Cipher/AES/TestKATForECB_VT.java

    private static final Logger logger = Logger.getLogger(JtregFinder.class.getName());

    /**
     * The subpath of a platform implementation under which tests live. Used to
     * derive relative test paths like {@code /java/io/Reader} from an absolute
     * path like {@code /home/jessewilson/platform_v6/test/java/io/Reader}.
     */
    static final String TEST_ROOT = "/test/";

    private final File localTemp;

    JtregFinder(File localTemp) {
        this.localTemp = localTemp;
    }

    /**
     * Returns the tests in {@code directoryToScan}.
     */
    public Set<TestRun> findTests(File directoryToScan) {
        // for now, jtreg doesn't know how to scan anything but directories
        if (!directoryToScan.isDirectory()) {
            return Collections.emptySet();
        }

        try {
            logger.fine("scanning " + directoryToScan + " for jtreg tests");
            File workDirectory = new File(localTemp, "JTwork");
            new Mkdir().mkdirs(workDirectory);

            /*
             * This code is capable of extracting test descriptions using jtreg 4.0
             * and its bundled copy of jtharness. As a command line tool, jtreg's
             * API wasn't intended for this style of use. As a consequence, this
             * code is fragile and may be incompatible with newer versions of jtreg.
             */
            TestSuite testSuite = new RegressionTestSuite(directoryToScan);
            WorkDirectory wd = WorkDirectory.convert(workDirectory, testSuite);
            TestResultTable resultTable = wd.getTestResultTable();

            Set<TestRun> result = new LinkedHashSet<TestRun>();
            for (Iterator i = resultTable.getIterator(); i.hasNext(); ) {
                TestResult testResult = (TestResult) i.next();
                TestDescription description = testResult.getDescription();
                String qualifiedName = qualifiedName(description);
                String suiteName = suiteName(description);
                String testName = description.getName();
                String testClass = description.getName();
                result.add(new TestRun(description.getDir(), description.getFile(),
                        testClass, suiteName, testName, qualifiedName,
                        description.getTitle(),
                        getRunnerClass(), getRunnerJava(), getRunnerClasspath()));
            }
            return result;
        } catch (Exception jtregFailure) {
            // jtreg shouldn't fail in practice
            throw new RuntimeException(jtregFailure);
        }
    }

    /**
     * Returns a fully qualified name of the form {@code
     * java.lang.Math.PowTests} from the given test description. The returned
     * name is appropriate for use in a filename.
     */
    String qualifiedName(TestDescription testDescription) {
        return suiteName(testDescription) + "." + escape(testDescription.getName());
    }

    /**
     * Returns the name of the class under test, such as {@code java.lang.Math}.
     */
    String suiteName(TestDescription testDescription) {
        String dir = testDescription.getDir().toString();
        int separatorIndex = dir.indexOf(TEST_ROOT);
        return separatorIndex != -1
                ? escape(dir.substring(separatorIndex + TEST_ROOT.length()))
                : escape(dir);
    }

    /**
     * Returns a similar string with filename-unsafe characters replaced by
     * filename-safe ones.
     */
    private String escape(String s) {
        return s.replace('/', '.');
    }

    public Class<? extends Runner> getRunnerClass() {
        return JtregRunner.class;
    }

    public File getRunnerJava() {
        return new File(DalvikRunner.HOME_JAVA, "dalvik/runner/JtregRunner.java");
    }

    public Classpath getRunnerClasspath() {
        return new Classpath();
    }
}
