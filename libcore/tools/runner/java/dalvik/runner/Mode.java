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

package dalvik.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A Mode for running tests. Examples including running in a virtual
 * machine either on the host or a device or within a specific context
 * such as within an Activity.
 */
abstract class Mode {

    private static final Pattern JAVA_TEST_PATTERN = Pattern.compile("\\/(\\w)+\\.java$");

    private static final Logger logger = Logger.getLogger(Mode.class.getName());

    protected final Environment environment;
    protected final long timeoutSeconds;
    protected final File sdkJar;
    protected final List<String> javacArgs;
    protected final PrintStream tee;

    /**
     * Set of Java files needed to built to tun the currently selected
     * set of tests. We build a subset rather than all the files all
     * the time to reduce dex packaging costs in the activity mode
     * case.
     */
    protected final Set<File> testRunnerJava = new HashSet<File>();

    /**
     * Classpath of testRunner on the host side including any
     * supporting libraries for testRunnerJava. Useful for compiling
     * testRunnerJava as well as executing it on the host. Execution
     * on the device requires further packaging typically done by
     * postCompileTestRunner.
     */
    protected final Classpath testRunnerClasspath = new Classpath();

    // TODO: this should be an immutable collection.
    protected final Classpath testClasspath = Classpath.of(
            new File("dalvik/libcore/tools/runner/lib/jsr305.jar"),
            new File("dalvik/libcore/tools/runner/lib/guava.jar"),
            new File("dalvik/libcore/tools/runner/lib/caliper.jar"),
            // TODO: we should be able to work with a shipping SDK, not depend on out/...
            // dalvik/libcore/**/test/ for junit
            // TODO: jar up just the junit classes and drop the jar in our lib/ directory.
            new File("out/target/common/obj/JAVA_LIBRARIES/core-tests-luni_intermediates/classes.jar").getAbsoluteFile());

    Mode(Environment environment, long timeoutSeconds, File sdkJar, List<String> javacArgs, PrintStream tee) {
        this.environment = environment;
        this.timeoutSeconds = timeoutSeconds;
        this.sdkJar = sdkJar;
        this.javacArgs = javacArgs;
        this.tee = tee;
    }

    /**
     * Initializes the temporary directories and test harness necessary to run
     * tests.
     */
    protected void prepare(Set<File> testRunnerJava, Classpath testRunnerClasspath) {
        this.testRunnerJava.add(new File(DalvikRunner.HOME_JAVA, "dalvik/runner/TestRunner.java"));
        this.testRunnerJava.addAll(dalvikAnnotationSourceFiles());
        this.testRunnerJava.addAll(testRunnerJava);
        this.testRunnerClasspath.addAll(testRunnerClasspath);
        environment.prepare();
        compileTestRunner();
    }

    private List<File> dalvikAnnotationSourceFiles() {
        // Hopefully one day we'll strip the dalvik annotations out, but until then we need to make
        // them available to javac(1).
        File sourceDir = new File("dalvik/libcore/dalvik/src/main/java/dalvik/annotation");
        File[] javaSourceFiles = sourceDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".java");
            }
        });
        return Arrays.asList(javaSourceFiles);
    }

    private void compileTestRunner() {
        logger.fine("build testrunner");

        Classpath classpath = new Classpath();
        classpath.addAll(testClasspath);
        classpath.addAll(testRunnerClasspath);

        File base = environment.testRunnerClassesDir();
        new Mkdir().mkdirs(base);
        new Javac()
                .bootClasspath(sdkJar)
                .classpath(classpath)
                .sourcepath(DalvikRunner.HOME_JAVA)
                .destination(base)
                .extra(javacArgs)
                .compile(testRunnerJava);
        postCompileTestRunner();
    }

    /**
     * Hook method called after TestRunner compilation.
     */
    abstract protected void postCompileTestRunner();

    /**
     * Compiles classes for the given test and makes them ready for execution.
     * If the test could not be compiled successfully, it will be updated with
     * the appropriate test result.
     */
    public void buildAndInstall(TestRun testRun) {
        logger.fine("build " + testRun.getQualifiedName());

        boolean testCompiled;
        try {
            testCompiled = compileTest(testRun);
            if (!testCompiled) {
                testRun.setResult(Result.UNSUPPORTED, Collections.<String>emptyList());
                return;
            }
        } catch (CommandFailedException e) {
            testRun.setResult(Result.COMPILE_FAILED, e.getOutputLines());
            return;
        } catch (IOException e) {
            testRun.setResult(Result.ERROR, e);
            return;
        }
        testRun.setTestCompiled(testCompiled);
        environment.prepareUserDir(testRun);
    }

    /**
     * Compiles the classes for the described test.
     *
     * @return the path to the compiled classes (directory or jar), or {@code
     *      null} if the test could not be compiled.
     * @throws CommandFailedException if javac fails
     */
    private boolean compileTest(TestRun testRun) throws IOException {
        if (!JAVA_TEST_PATTERN.matcher(testRun.getTestJava().toString()).find()) {
            return false;
        }

        String qualifiedName = testRun.getQualifiedName();
        File testClassesDir = environment.testClassesDir(testRun);
        new Mkdir().mkdirs(testClassesDir);
        FileOutputStream propertiesOut = new FileOutputStream(
                new File(testClassesDir, TestProperties.FILE));
        Properties properties = new Properties();
        fillInProperties(properties, testRun);
        properties.store(propertiesOut, "generated by " + Mode.class.getName());
        propertiesOut.close();

        Classpath classpath = new Classpath();
        classpath.addAll(testClasspath);
        classpath.addAll(testRun.getRunnerClasspath());

        Set<File> sourceFiles = new HashSet<File>();
        sourceFiles.add(testRun.getTestJava());
        sourceFiles.addAll(dalvikAnnotationSourceFiles());

        // compile the test case
        new Javac()
                .bootClasspath(sdkJar)
                .classpath(classpath)
                .sourcepath(testRun.getTestDirectory())
                .destination(testClassesDir)
                .extra(javacArgs)
                .compile(sourceFiles);
        postCompileTest(testRun);
        return true;
    }

    /**
     * Hook method called after test compilation.
     *
     * @param testRun The test being compiled
     */
    abstract protected void postCompileTest(TestRun testRun);


    /**
     * Fill in properties for running in this mode
     */
    protected void fillInProperties(Properties properties, TestRun testRun) {
        properties.setProperty(TestProperties.TEST_CLASS, testRun.getTestClass());
        properties.setProperty(TestProperties.QUALIFIED_NAME, testRun.getQualifiedName());
        properties.setProperty(TestProperties.RUNNER_CLASS, testRun.getRunnerClass().getName());
    }

    /**
     * Runs the test, and updates its test result.
     */
    void runTest(TestRun testRun) {
        if (!testRun.isRunnable()) {
            throw new IllegalArgumentException();
        }

        List<String> output;
        try {
            output = runTestCommand(testRun);
        } catch (TimeoutException e) {
            testRun.setResult(Result.EXEC_TIMEOUT,
                Collections.singletonList("Exceeded timeout! (" + timeoutSeconds + "s)"));
            return;
        } catch (Exception e) {
            testRun.setResult(Result.ERROR, e);
            return;
        }
        // we only look at the output of the last command
        if (output.isEmpty()) {
            testRun.setResult(Result.ERROR,
                    Collections.singletonList("No output returned!"));
            return;
        }

        Result result = TestProperties.RESULT_SUCCESS.equals(output.get(output.size() - 1))
                ? Result.SUCCESS
                : Result.EXEC_FAILED;
        testRun.setResult(result, output.subList(0, output.size() - 1));
    }

    /**
     * Run the actual test to gather output
     */
    protected abstract List<String> runTestCommand(TestRun testRun)
        throws TimeoutException;

    /**
     * Deletes files and releases any resources required for the execution of
     * the given test.
     */
    void cleanup(TestRun testRun) {
        environment.cleanup(testRun);
    }

    /**
     * Cleans up after all test runs have completed.
     */
    void shutdown() {
        environment.shutdown();
    }
}
