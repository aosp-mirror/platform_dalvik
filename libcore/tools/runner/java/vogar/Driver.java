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

package vogar;

import vogar.commands.Mkdir;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Compiles, installs, runs and reports on actions.
 */
final class Driver {

    private static final Logger logger = Logger.getLogger(Driver.class.getName());

    private final File localTemp;
    private final ExpectationStore expectationStore;
    private final List<CodeFinder> codeFinders;
    private final Mode mode;
    private final String indent;
    private final XmlReportPrinter reportPrinter;

    private final Map<String, Action> actions = Collections.synchronizedMap(
            new LinkedHashMap<String, Action>());
    private final Map<String, Outcome> outcomes = Collections.synchronizedMap(
            new LinkedHashMap<String, Outcome>());

    /**
     * The number of tests that weren't run because they aren't supported by
     * this runner.
     */
    private int unsupportedActions = 0;

    public Driver(File localTemp, Mode mode, ExpectationStore expectationStore,
            String indent, List<CodeFinder> codeFinders, XmlReportPrinter reportPrinter) {
        this.localTemp = localTemp;
        this.expectationStore = expectationStore;
        this.mode = mode;
        this.indent = indent;
        this.codeFinders = codeFinders;
        this.reportPrinter = reportPrinter;
    }

    /**
     * Builds and executes all tests in the test directory.
     */
    public void buildAndRunAllActions(Collection<File> files) {
        if (!actions.isEmpty()) {
            throw new IllegalStateException("Drivers are not reusable");
        }

        new Mkdir().mkdirs(localTemp);
        for (File file : files) {
            Set<Action> actionsForFile = Collections.emptySet();

            for (CodeFinder codeFinder : codeFinders) {
                actionsForFile = codeFinder.findActions(file);

                // break as soon as we find any match. We don't need multiple
                // matches for the same file, since that would run it twice.
                if (!actionsForFile.isEmpty()) {
                    break;
                }
            }

            for (Action action : actionsForFile) {
                actions.put(action.getName(), action);
            }
        }

        // compute TestRunner java and classpath to pass to mode.prepare
        Set<File> runnerJava = new HashSet<File>();
        Classpath runnerClasspath = new Classpath();
        for (final Action action : actions.values()) {
            runnerJava.add(action.getRunnerJava());
            runnerClasspath.addAll(action.getRunnerClasspath());
        }

        // mode.prepare before mode.buildAndInstall to ensure the runner is
        // built. packaging of activity APK files needs the runner along with
        // the action-specific files.
        mode.prepare(runnerJava, runnerClasspath);

        logger.info("Running " + actions.size() + " actions.");

        // build and install actions in a background thread. Using lots of
        // threads helps for packages that contain many unsupported actions
        final BlockingQueue<Action> readyToRun = new ArrayBlockingQueue<Action>(4);

        ExecutorService builders = Threads.threadPerCpuExecutor();
        int t = 0;

        for (final Action action : actions.values()) {
            final String name = action.getName();
            final int runIndex = t++;
            builders.submit(new Runnable() {
                public void run() {
                    try {
                        logger.fine("installing action " + runIndex + "; "
                                + readyToRun.size() + " are runnable");

                        if (expectationStore.get(name).getResult() == Result.UNSUPPORTED) {
                            outcomes.put(name, new Outcome(name, Result.UNSUPPORTED,
                                    "Unsupported according to expectations file"));

                        } else {
                            Outcome outcome = mode.buildAndInstall(action);
                            if (outcome != null) {
                                outcomes.put(name, outcome);
                            }
                        }

                        readyToRun.put(action);
                    } catch (InterruptedException e) {
                        outcomes.put(name, new Outcome(name, Result.ERROR, e));
                    }
                }
            });
        }
        builders.shutdown();

        for (int i = 0; i < actions.size(); i++) {
            logger.fine("executing action " + i + "; "
                    + readyToRun.size() + " are ready to run");

            // if it takes 5 minutes for build and install, something is broken
            Action action;
            try {
                action = readyToRun.poll(5 * 60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unexpected interruption waiting for build and install", e);
            }

            if (action == null) {
                throw new IllegalStateException("Expected " + actions.size()
                        + " actions but found only " + i);
            }

            execute(action);
            mode.cleanup(action);
        }

        if (unsupportedActions > 0) {
            logger.info("Skipped " + unsupportedActions + " unsupported actions.");
        }

        if (reportPrinter != null) {
            logger.info("Printing XML Reports... ");
            int numFiles = reportPrinter.generateReports(outcomes.values());
            logger.info(numFiles + " XML files written.");
        }
    }

    /**
     * Executes a single action and then prints the result.
     */
    private void execute(Action action) {
        Outcome earlyFailure = outcomes.get(action.getName());
        if (earlyFailure != null) {
            if (earlyFailure.getResult() == Result.UNSUPPORTED) {
                logger.fine("skipping " + action.getName());
                unsupportedActions++;
            } else {
                printResult(earlyFailure);
            }
            return;
        }

        Set<Outcome> outcomes = mode.run(action);
        for (Outcome outcome : outcomes) {
            printResult(outcome);
        }
    }

    private void printResult(Outcome outcome) {
        Expectation expected = expectationStore.get(outcome.getName());
        Action action = actions.get(outcome.getActionName());

        if (expected.matches(outcome)) {
            logger.info("OK " + outcome.getName() + " (" + outcome.getResult() + ")");
            // In --verbose mode, show the output even on success.
            logger.fine(indent + expected.getFailureMessage(outcome).replace("\n", "\n" + indent));
            return;
        }

        logger.info("FAIL " + outcome.getName() + " (" + outcome.getResult() + ")");
        String description = action.getDescription();
        if (description != null) {
            logger.info(indent + "\"" + description + "\"");
        }

        // Don't mess with compiler error output for tools (such as
        // Emacs) that are trying to parse it with regexps
        logger.info(indent + expected.getFailureMessage(outcome).replace("\n", "\n" + indent));
    }
}
