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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A Java-like virtual machine for compiling and running tests.
 */
public abstract class Vm {

    static final String DALVIK_RUNNER_HOME = "dalvik/libcore/tools/runner";

    static final Set<File> TEST_RUNNER_JAVA_FILES = new HashSet<File>(Arrays.asList(
            new File(DALVIK_RUNNER_HOME + "/java/dalvik/runner/CaliperRunner.java"),
            new File(DALVIK_RUNNER_HOME + "/java/dalvik/runner/JUnitRunner.java"),
            new File(DALVIK_RUNNER_HOME + "/java/dalvik/runner/JtregRunner.java"),
            new File(DALVIK_RUNNER_HOME + "/java/dalvik/runner/MainRunner.java"),
            new File(DALVIK_RUNNER_HOME + "/java/dalvik/runner/TestRunner.java")));

    private final Pattern JAVA_TEST_PATTERN = Pattern.compile("\\/(\\w)+\\.java$");
    static final Classpath COMPILATION_CLASSPATH = Classpath.of(
            new File("out/target/common/obj/JAVA_LIBRARIES/core_intermediates/classes.jar").getAbsoluteFile(),
            new File("out/target/common/obj/JAVA_LIBRARIES/core-tests_intermediates/classes.jar").getAbsoluteFile(),
            new File("out/target/common/obj/JAVA_LIBRARIES/jsr305_intermediates/classes.jar").getAbsoluteFile(),
            new File("out/target/common/obj/JAVA_LIBRARIES/guava_intermediates/classes.jar").getAbsoluteFile(),
            new File("out/target/common/obj/JAVA_LIBRARIES/caliper_intermediates/classes.jar").getAbsoluteFile());

    private static final Logger logger = Logger.getLogger(Vm.class.getName());

    protected final ExecutorService outputReaders
            = Executors.newFixedThreadPool(1, Threads.daemonThreadFactory());

    protected final Integer debugPort;
    protected final List<String> additionalVmArgs;
    protected final long timeoutSeconds;
    protected final File sdkJar;
    protected final File localTemp;
    protected final boolean clean;

    /** The path of the test runner's compiled classes */
    private Classpath testRunnerClasses;

    Vm(Integer debugPort, long timeoutSeconds, File sdkJar, File localTemp,
            List<String> additionalVmArgs, boolean clean) {
        this.debugPort = debugPort;
        this.timeoutSeconds = timeoutSeconds;
        this.sdkJar = sdkJar;
        this.localTemp = localTemp;
        this.additionalVmArgs = additionalVmArgs;
        this.clean = clean;
    }

    /**
     * Initializes the temporary directories and test harness necessary to run
     * tests.
     */
    public void prepare() {
        testRunnerClasses = compileTestRunner();
    }

    private Classpath compileTestRunner() {
        logger.fine("build testrunner");

        File base = new File(localTemp, "testrunner");
        base.mkdirs();
        new Javac()
                .classpath(COMPILATION_CLASSPATH)
                .destination(base)
                .compile(TEST_RUNNER_JAVA_FILES);

        return postCompile("testrunner", Classpath.of(base));
    }

    /**
     * Cleans up after all test runs have completed.
     */
    public void shutdown() {
        outputReaders.shutdown();
    }

    /**
     * Compiles classes for the given test and makes them ready for execution.
     * If the test could not be compiled successfully, it will be updated with
     * the appropriate test result.
     */
    public void buildAndInstall(TestRun testRun) {
        logger.fine("build " + testRun.getQualifiedName());

        Classpath testClasses;
        try {
            testClasses = compileTest(testRun);
            if (testClasses == null) {
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
        testRun.setTestClasspath(testClasses);
        prepareUserDir(testRun);
    }

    /**
     * Prepares the directory from which the test will be executed. Some tests
     * expect to read data files from the current working directory; this step
     * should ensure such files are available.
     */
    protected void prepareUserDir(TestRun testRun) {
        File testUserDir = testUserDir(testRun);

        // if the user dir exists, cp would copy the files to the wrong place
        if (testUserDir.exists()) {
            throw new IllegalStateException();
        }

        testUserDir.getParentFile().mkdirs();
        new Command("cp", "-r", testRun.getTestDirectory().toString(),
                testUserDir.toString()).execute();
        testRun.setUserDir(testUserDir);
    }

    /**
     * Deletes files and releases any resources required for the execution of
     * the given test.
     */
    public void cleanup(TestRun testRun) {
        if (clean) {
            logger.fine("clean " + testRun.getQualifiedName());

            new Command.Builder().args("rm", "-rf", testClassesDir(testRun).getPath())
                    .execute();
            new Command.Builder().args("rm", "-rf", testUserDir(testRun).getPath())
                    .execute();
        }
    }

    /**
     * Compiles the classes for the described test.
     *
     * @return the path to the compiled classes (directory or jar), or {@code
     *      null} if the test could not be compiled.
     * @throws CommandFailedException if javac fails
     */
    private Classpath compileTest(TestRun testRun) throws IOException {
        if (!JAVA_TEST_PATTERN.matcher(testRun.getJavaFile().toString()).find()) {
            return null;
        }

        String qualifiedName = testRun.getQualifiedName();
        File testClassesDir = testClassesDir(testRun);
        testClassesDir.mkdirs();

        FileOutputStream propertiesOut = new FileOutputStream(
                new File(testClassesDir, TestRunner.TEST_PROPERTIES_FILE));
        toProperties(testRun).store(propertiesOut, "generated by " + getClass().getName());
        propertiesOut.close();

        // write a test descriptor
        new Javac()
                .bootClasspath(sdkJar)
                .classpath(COMPILATION_CLASSPATH)
                .sourcepath(testRun.getTestDirectory())
                .destination(testClassesDir)
                .compile(testRun.getJavaFile());
        return postCompile(qualifiedName, Classpath.of(testClassesDir));
    }

    private File testClassesDir(TestRun testRun) {
        return new File(localTemp, testRun.getQualifiedName());
    }

    private File testUserDir(TestRun testRun) {
        File testTemp = new File(localTemp, "userDir");
        return new File(testTemp, testRun.getQualifiedName());
    }

    /**
     * Returns a properties object for the given test description.
     */
    static Properties toProperties(TestRun testRun) {
        Properties result = new Properties();
        result.setProperty(TestRunner.CLASS_NAME, testRun.getTestClass());
        result.setProperty(TestRunner.QUALIFIED_NAME, testRun.getQualifiedName());
        return result;
    }

    /**
     * Runs the test, and updates its test result.
     */
    public void runTest(TestRun testRun) {
        if (!testRun.isRunnable()) {
            throw new IllegalArgumentException();
        }

        final Command command = newVmCommandBuilder(testRun.getUserDir())
                .classpath(testRun.getTestClasspath())
                .classpath(testRunnerClasses)
                .classpath(getRuntimeSupportClasspath())
                .userDir(testRun.getUserDir())
                .debugPort(debugPort)
                .vmArgs(additionalVmArgs)
                .mainClass(testRun.getTestRunner().getName())
                .build();

        logger.fine("executing " + command.getArgs());

        try {
            command.start();

            // run on a different thread to allow a timeout
            List<String> output = outputReaders.submit(new Callable<List<String>>() {
                public List<String> call() throws Exception {
                    return command.gatherOutput();
                }
            }).get(timeoutSeconds, TimeUnit.SECONDS);

            if (output.isEmpty()) {
                testRun.setResult(Result.ERROR,
                        Collections.singletonList("No output returned!"));
                return;
            }

            Result result = "SUCCESS".equals(output.get(output.size() - 1))
                    ? Result.SUCCESS
                    : Result.EXEC_FAILED;
            testRun.setResult(result, output.subList(0, output.size() - 1));
        } catch (TimeoutException e) {
            testRun.setResult(Result.EXEC_TIMEOUT,
                    Collections.singletonList("Exceeded timeout! (" + timeoutSeconds + "s)"));
        } catch (Exception e) {
            testRun.setResult(Result.ERROR, e);
        } finally {
            if (command.isStarted()) {
                command.getProcess().destroy(); // to release the output reader
            }
        }
    }

    /**
     * Returns a VM for test execution.
     */
    protected abstract VmCommandBuilder newVmCommandBuilder(File workingDirectory);

    /**
     * Returns the classpath containing JUnit and the dalvik annotations
     * required for test execution.
     */
    protected Classpath getRuntimeSupportClasspath() {
        return COMPILATION_CLASSPATH;
    }

    /**
     * Hook method called after each compilation.
     *
     * @param name the name of this compilation unit. Usually a qualified test
     *        name like java.lang.Math.PowTests.
     * @param targetClasses the full set of classes that make up this target.
     *        This will include the newly compiled classes, plus optional other
     *        classes that complete the target (such as library jars).
     * @return the new result file.
     */
    protected Classpath postCompile(String name, Classpath targetClasses) {
        return targetClasses;
    }

    /**
     * Builds a virtual machine command.
     */
    public static class VmCommandBuilder {
        private File temp;
        private Classpath classpath = new Classpath();
        private File workingDir;
        private File userDir;
        private Integer debugPort;
        private String mainClass;
        private List<String> vmCommand = Collections.singletonList("java");
        private List<String> vmArgs = new ArrayList<String>();

        public VmCommandBuilder vmCommand(String... vmCommand) {
            this.vmCommand = Arrays.asList(vmCommand.clone());
            return this;
        }

        public VmCommandBuilder temp(File temp) {
            this.temp = temp;
            return this;
        }

        public VmCommandBuilder classpath(Classpath classpath) {
            this.classpath.addAll(classpath);
            return this;
        }

        public VmCommandBuilder workingDir(File workingDir) {
            this.workingDir = workingDir;
            return this;
        }

        public VmCommandBuilder userDir(File userDir) {
            this.userDir = userDir;
            return this;
        }

        public VmCommandBuilder debugPort(Integer debugPort) {
            this.debugPort = debugPort;
            return this;
        }

        public VmCommandBuilder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        public VmCommandBuilder vmArgs(String... vmArgs) {
            return vmArgs(Arrays.asList(vmArgs));
        }

        public VmCommandBuilder vmArgs(Collection<String> vmArgs) {
            this.vmArgs.addAll(vmArgs);
            return this;
        }

        public Command build() {
            Command.Builder builder = new Command.Builder();
            builder.args(vmCommand);
            builder.args("-classpath", classpath.toString());
            builder.args("-Duser.dir=" + userDir);
            if (workingDir != null) {
                builder.workingDirectory(workingDir);
            }

            if (temp != null) {
                builder.args("-Djava.io.tmpdir=" + temp);
            }

            if (debugPort != null) {
                builder.args("-Xrunjdwp:transport=dt_socket,address="
                        + debugPort + ",server=y,suspend=y");
            }

            builder.args(vmArgs);
            builder.args(mainClass);

            return builder.build();
        }
    }
}
