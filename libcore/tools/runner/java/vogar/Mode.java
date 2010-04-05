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
     * Classpath of runner on the host side including any supporting libraries
     * for runnerJava. Useful for compiling runnerJava as well as executing it
     * on the host. Execution on the device requires further packaging typically
     * done by postCompile.
     */
    protected final Classpath runnerClasspath = new Classpath();

    // TODO: this should be an immutable collection.
    protected final Classpath classpath = Classpath.of(
            new File("dalvik/libcore/tools/runner/lib/jsr305.jar"),
            new File("dalvik/libcore/tools/runner/lib/guava.jar"),
            new File("dalvik/libcore/tools/runner/lib/caliper.jar"),
            // TODO: we should be able to work with a shipping SDK, not depend on out/...
            // dalvik/libcore/**/test/ for junit
            // TODO: jar up just the junit classes and drop the jar in our lib/ directory.
            new File("out/host/common/obj/JAVA_LIBRARIES/kxml2-2.3.0_intermediates/javalib.jar").getAbsoluteFile(),
            new File("out/target/common/obj/JAVA_LIBRARIES/core-tests-luni_intermediates/classes.jar").getAbsoluteFile());

    Mode(Environment environment, File sdkJar, List<String> javacArgs, int monitorPort) {
        this.environment = environment;
        this.sdkJar = sdkJar;
        this.javacArgs = javacArgs;
        this.monitorPort = monitorPort;
    }

    /**
     * Initializes the temporary directories and harness necessary to run
     * actions.
     */
    protected void prepare(Set<File> runnerJava, Classpath runnerClasspath) {
        this.runnerJava.add(new File(Vogar.HOME_JAVA, "vogar/target/TestRunner.java"));
        this.runnerJava.addAll(dalvikAnnotationSourceFiles());
        this.runnerJava.addAll(runnerJava);
        this.runnerClasspath.addAll(runnerClasspath);
        environment.prepare();
        compileRunner();
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

    private void compileRunner() {
        logger.fine("build runner");

        Classpath classpath = new Classpath();
        classpath.addAll(this.classpath);
        classpath.addAll(runnerClasspath);

        File base = environment.runnerClassesDir();
        new Mkdir().mkdirs(base);
        new Javac()
                .bootClasspath(sdkJar)
                .classpath(classpath)
                .sourcepath(Vogar.HOME_JAVA)
                .destination(base)
                .extra(javacArgs)
                .compile(runnerJava);
        postCompileRunner();
    }

    /**
     * Hook method called after runner compilation.
     */
    abstract protected void postCompileRunner();

    /**
     * Compiles classes for the given action and makes them ready for execution.
     *
     * @return null if the compilation succeeded, or an outcome describing the
     *      failure otherwise.
     */
    public Outcome buildAndInstall(Action action) {
        logger.fine("build " + action.getName());

        try {
            compile(action);
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
     * Compiles the classes for the described action.
     *
     * @throws CommandFailedException if javac fails
     */
    private void compile(Action action) throws IOException {
        if (!JAVA_SOURCE_PATTERN.matcher(action.getJavaFile().toString()).find()) {
            throw new CommandFailedException(Collections.<String>emptyList(),
                    Collections.singletonList("Cannot compile: " + action.getJavaFile()));
        }

        File classesDir = environment.classesDir(action);
        new Mkdir().mkdirs(classesDir);
        FileOutputStream propertiesOut = new FileOutputStream(
                new File(classesDir, TestProperties.FILE));
        Properties properties = new Properties();
        fillInProperties(properties, action);
        properties.store(propertiesOut, "generated by " + Mode.class.getName());
        propertiesOut.close();

        Classpath classpath = new Classpath();
        classpath.addAll(this.classpath);
        classpath.addAll(action.getRunnerClasspath());

        Set<File> sourceFiles = new HashSet<File>();
        sourceFiles.add(action.getJavaFile());
        sourceFiles.addAll(dalvikAnnotationSourceFiles());

        // compile the action case
        new Javac()
                .bootClasspath(sdkJar)
                .classpath(classpath)
                .sourcepath(action.getJavaDirectory())
                .destination(classesDir)
                .extra(javacArgs)
                .compile(sourceFiles);
        postCompile(action);
    }

    /**
     * Hook method called after action compilation.
     */
    abstract protected void postCompile(Action action);


    /**
     * Fill in properties for running in this mode
     */
    protected void fillInProperties(Properties properties, Action action) {
        properties.setProperty(TestProperties.TEST_CLASS, action.getTargetClass());
        properties.setProperty(TestProperties.QUALIFIED_NAME, action.getName());
        properties.setProperty(TestProperties.RUNNER_CLASS, action.getRunnerClass().getName());
        properties.setProperty(TestProperties.MONITOR_PORT, String.valueOf(monitorPort));
    }

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
