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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import vogar.commands.Command;
import vogar.commands.CommandFailedException;
import vogar.commands.Mkdir;

/**
 * A Mode for running actions. Examples including running in a virtual machine
 * either on the host or a device or within a specific context such as within an
 * Activity.
 */
abstract class Mode {

    private static final Pattern JAVA_SOURCE_PATTERN = Pattern.compile("\\/(\\w)+\\.java$");

    private static final Logger logger = Logger.getLogger(Mode.class.getName());

    protected final Environment environment;
    protected final File sdkJar;
    protected final List<String> javacArgs;
    protected final int monitorPort;

    /**
     * Set of Java files needed to built to tun the currently selected set of
     * actions. We build a subset rather than all the files all the time to
     * reduce dex packaging costs in the activity mode case.
     */
    protected final Set<File> runnerJava = new HashSet<File>();

    /**
     * User classes that need to be included in the classpath for both
     * compilation and execution. Also includes dependencies of all active
     * runners.
     */
    protected final Classpath classpath = new Classpath();

    Mode(Environment environment, File sdkJar, List<String> javacArgs,
            int monitorPort, Classpath classpath) {
        this.environment = environment;
        this.sdkJar = sdkJar;
        this.javacArgs = javacArgs;
        this.monitorPort = monitorPort;
        this.classpath.addAll(classpath);
    }

    /**
     * Initializes the temporary directories and harness necessary to run
     * actions.
     */
    protected void prepare(Set<RunnerSpec> runners) {
        for (RunnerSpec runnerSpec : runners) {
            runnerJava.add(runnerSpec.getSource());
            classpath.addAll(runnerSpec.getClasspath());
        }
        runnerJava.add(new File(Vogar.HOME_JAVA, "vogar/target/TestRunner.java"));
        environment.prepare();
        classpath.addAll(compileRunner());
        installRunner();
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

    /**
     * Returns a .jar file containing the compiled runner .java files.
     */
    private File compileRunner() {
        logger.fine("build runner");
        File classes = environment.file("runner", "classes");
        File jar = environment.hostJar("runner");
        new Mkdir().mkdirs(classes);
        new Javac()
                .bootClasspath(sdkJar)
                .classpath(classpath)
                .sourcepath(Vogar.HOME_JAVA)
                .destination(classes)
                .extra(javacArgs)
                .compile(runnerJava);
        new Command("jar", "cvfM", jar.getPath(),
                 "-C", classes.getPath(), "./").execute();
        return jar;
    }

    /**
     * Compiles classes for the given action and makes them ready for execution.
     *
     * @return null if the compilation succeeded, or an outcome describing the
     *      failure otherwise.
     */
    public Outcome buildAndInstall(Action action) {
        logger.fine("build " + action.getName());

        try {
            File jar = compile(action);
            postCompile(action, jar);
        } catch (CommandFailedException e) {
            return new Outcome(action.getName(), action.getName(),
                    Result.COMPILE_FAILED, e.getOutputLines());
        } catch (IOException e) {
            return new Outcome(action.getName(), Result.ERROR, e);
        }
        environment.prepareUserDir(action);
        return null;
    }

    /**
     * Returns the .jar file containing the action's compiled classes.
     *
     * @throws CommandFailedException if javac fails
     */
    private File compile(Action action) throws IOException {
        File classesDir = environment.file(action, "classes");
        new Mkdir().mkdirs(classesDir);
        FileOutputStream propertiesOut = new FileOutputStream(
                new File(classesDir, TestProperties.FILE));
        Properties properties = new Properties();
        fillInProperties(properties, action);
        properties.store(propertiesOut, "generated by " + Mode.class.getName());
        propertiesOut.close();

        Javac javac = new Javac();

        Set<File> sourceFiles = new HashSet<File>();
        sourceFiles.addAll(dalvikAnnotationSourceFiles());

        File javaFile = action.getJavaFile();
        if (javaFile != null) {
            if (!JAVA_SOURCE_PATTERN.matcher(javaFile.toString()).find()) {
                throw new CommandFailedException(Collections.<String>emptyList(),
                        Collections.singletonList("Cannot compile: " + javaFile));
            }
            sourceFiles.add(javaFile);
            javac.sourcepath(javaFile.getParentFile());
        }

        javac.bootClasspath(sdkJar)
                .classpath(classpath)
                .destination(classesDir)
                .extra(javacArgs)
                .compile(sourceFiles);

        File jar = environment.hostJar(action);
        new Command("jar", "cvfM", jar.getPath(),
                "-C", classesDir.getPath(), "./").execute();
        return jar;
    }

    /**
     * Fill in properties for running in this mode
     */
    protected void fillInProperties(Properties properties, Action action) {
        properties.setProperty(TestProperties.TEST_CLASS, action.getTargetClass());
        properties.setProperty(TestProperties.QUALIFIED_NAME, action.getName());
        properties.setProperty(TestProperties.RUNNER_CLASS, action.getRunnerSpec().getRunnerClass().getName());
        properties.setProperty(TestProperties.MONITOR_PORT, String.valueOf(monitorPort));
    }

    /**
     * Hook method called after runner compilation.
     */
    protected void installRunner() {}

    /**
     * Hook method called after action compilation.
     */
    protected void postCompile(Action action, File jar) {}

    /**
     * Create the command that executes the action.
     */
    protected abstract Command createActionCommand(Action action);

    /**
     * Deletes files and releases any resources required for the execution of
     * the given action.
     */
    void cleanup(Action action) {
        environment.cleanup(action);
    }

    /**
     * Cleans up after all actions have completed.
     */
    void shutdown() {
        environment.shutdown();
    }
}
