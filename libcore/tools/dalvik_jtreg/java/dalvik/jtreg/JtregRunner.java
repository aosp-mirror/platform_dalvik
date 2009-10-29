// Copyright 2009 Google Inc. All Rights Reserved.

package dalvik.jtreg;

import com.sun.javatest.TestDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Runs a directory's worth of jtreg tests on a device.
 */
public final class JtregRunner {

    private final File localTemp = new File("/tmp/" + UUID.randomUUID());
    private final File deviceTemp = new File("/data/jtreg" + UUID.randomUUID());

    private final Adb adb = new Adb();
    private final File directoryToScan;
    private final TestToDex testToDex;

    private File deviceTestRunner;

    public JtregRunner(File sdkJar, File directoryToScan) {
        this.directoryToScan = directoryToScan;
        this.testToDex = new TestToDex(sdkJar, localTemp);
    }

    public void buildAndRunAllTests() throws Exception {
        localTemp.mkdirs();

        prepareDevice();
        List<TestDescription> tests = testToDex.findTests(directoryToScan);

        // TODO: investigate why tests don't work when run in parallel on device
        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<Run>> futures = new ArrayList<Future<Run>>();
        for (final TestDescription testDescription : tests) {
            futures.add(executor.submit(new Callable<Run>() {
                public Run call() throws Exception {
                    return buildAndRunTest(testDescription);
                }
            }));
        }

        for (Future<Run> future : futures) {
            try {
                System.out.println(future.get());
                System.out.println();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown();
    }

    /**
     * Initializes the temporary directories and test harness necessary to run
     * tests on a device.
     */
    private void prepareDevice() {
        System.out.print("Preparing device...");
        adb.mkdir(deviceTemp);
        File testRunnerJar = testToDex.writeTestRunnerJar();
        adb.push(testRunnerJar, deviceTemp);
        deviceTestRunner = new File(deviceTemp, testRunnerJar.getName());
        System.out.println("done.");
    }

    /**
     * Creates a dex file for the given test, pushes it out to the device, and
     * runs it, returning the test's result.
     */
    private Run buildAndRunTest(TestDescription testDescription)
            throws IOException {
        String qualifiedName = TestDescriptions.qualifiedName(testDescription);
        File base = new File(deviceTemp, qualifiedName);
        adb.mkdir(base);

        File dex;
        try {
            dex = testToDex.dexify(testDescription);
            if (dex == null) {
                return new Run(testDescription, Run.Result.SKIPPED, Collections.<String>emptyList());
            }
        } catch (CommandFailedException e) {
            return new Run(testDescription, Run.Result.COMPILE_FAILED, e.getOutputLines());
        } catch (IOException e) {
            return new Run(testDescription, Run.Result.ERROR, e);
        }

        adb.push(testDescription.getDir(), base);
        adb.push(dex, deviceTemp);
        File deviceDex = new File(deviceTemp, dex.getName());

        return runTest(testDescription, base, deviceDex);
    }

    /**
     * Runs the specified test on the device.
     *
     * @param base the test's base directory, from which local files can be
     *      read by the test.
     * @param dex the jar file containing the test code.
     * @return the result of executing the test.
     */
    private Run runTest(TestDescription testDescription, File base, File dex) {
        List<String> output = new Dalvikvm()
                .classpath(dex, deviceTestRunner)
                .args("-Duser.dir=" + base)
                .exec("dalvik.jtreg.TestRunner");

        if (output.isEmpty()) {
            return new Run(testDescription, Run.Result.ERROR,
                    Collections.singletonList("No output returned!"));
        }

        Run.Result result = "SUCCESS".equals(output.get(output.size() - 1))
                ? Run.Result.SUCCESS
                : Run.Result.EXEC_FAILED;
        return new Run(testDescription, result, output.subList(0, output.size() - 1));
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: JTRegRunner <android_jar> <directoryWithTests>");
            System.out.println();
            System.out.println("  android_jar: the API jar file to compile against. Usually");
            System.out.println("      this is <SDK>/platforms/android-<X.X>/android.jar where");
            System.out.println("      <SDK> is the path to an Android SDK path and <X.X> is a");
            System.out.println("      release version like 1.5.");
            System.out.println();
            System.out.println("  directoryWithTests: a directory to scan for test cases;");
            System.out.println("      typically this is 'platform_v6/jdk/test' if 'platform_v6'");
            System.out.println("      contains the sources of a platform implementation.");
            System.out.println();
            return;
        }

        File sdkJar = new File(args[0]);
        if (!sdkJar.exists()) {
            throw new RuntimeException("No such file: " + sdkJar);
        }

        File directoryToScan = new File(args[1]);
        if (!directoryToScan.exists()) {
            throw new RuntimeException("No such directory: " + directoryToScan);
        }

        new JtregRunner(sdkJar, directoryToScan).buildAndRunAllTests();
    }
}
