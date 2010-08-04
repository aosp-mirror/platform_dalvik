/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.util.ArrayList;

/**
 * The matrix of tests includes the A-E axis for loop body contents and
 * the 0-5 axis for iterator style.
 *
 * <ul>
 * <li>A: empty body</li>
 * <li>B: array element access and update</li>
 * <li>C: instance field access and update</li>
 * <li>D: method call to empty method</li>
 * <li>E: synch and then method call to empty method</li>
 * <li>F: 5 method calls to empty method</li>
 * <li>G: one small object allocation (empty constructor)</li>
 * <li>H: copy 8k of bytes from one array to another</li>
 * </ul>
 *
 * <ul>
 * <li>0: for() loop backward to 0</li>
 * <li>1: for() loop forward to local variable</li>
 * <li>2: for() loop forward to array length</li>
 * <li>3: for(:) loop over array</li>
 * <li>4: for() loop forward to instance variable</li>
 * <li>5: for() loop forward to trivial method call</li>
 * <li>6: for(:) loop over ArrayList</li>
 * </ul>
 */
public class Main {
    static public final int BODIES = 8;
    static public final int LOOPS = 7;

    static public void main(String[] args) throws Exception {
        boolean timing = (args.length >= 1) && args[0].equals("--timing");

        int iters = 100;
        double probeSec;

        for (;;) {
            long t0 = System.nanoTime();
            runAllTests(iters, false);
            long t1 = System.nanoTime();

            probeSec = (t1 - t0) / 1000000000.0;
            if (probeSec > 0.25) {
                break;
            }

            iters *= 2;
        }

        // Attempt to arrange for the real test to take 20 seconds.
        iters = (int) ((iters / probeSec) * 20);

        if (timing) {
            System.out.println("iters = " + iters);
        }

        run(timing, iters);
    }

    static private enum Normalization {
        NONE, PER_COLUMN, TOP_LEFT;
    }

    static public void printTimings(double[][] timings, Normalization norm) {
        System.out.println();
        System.out.printf("%-7s   A        B        C        D        E" +
                "        F        G        H\n",
                (norm == Normalization.NONE) ? "(usec)" : "(ratio)");
        System.out.println("      -------- -------- -------- -------- " +
                "-------- -------- -------- --------");

        double bases[] = new double[BODIES];
        for (int i = 0; i < BODIES; i++) {
            double n;
            switch (norm) {
                case PER_COLUMN:  n = timings[i][0]; break;
                case TOP_LEFT:    n = timings[0][0]; break;
                default /*NONE*/: n = 1.0;           break;
            }
            bases[i] = n;
        }

        for (int i = 0; i < LOOPS; i++) {
            System.out.printf("%4d: %8.3g %8.3g %8.3g %8.3g %8.3g %8.3g " +
                    "%8.3g %8.3g\n",
                    i,
                    timings[0][i] / bases[0],
                    timings[1][i] / bases[1],
                    timings[2][i] / bases[2],
                    timings[3][i] / bases[3],
                    timings[4][i] / bases[4],
                    timings[5][i] / bases[5],
                    timings[6][i] / bases[6],
                    timings[7][i] / bases[7]);
        }
    }

    static public void run(boolean timing, int iters) {
        double[][] timings = null; // assign to avoid apparent javac bug

        // Try up to 5 times to get good times.
        for (int i = 0; i < 5; i++) {
            double[][] newTimings = runAllTests(iters, timing || (i == 0));

            if (timings == null) {
                timings = newTimings;
            } else {
                combineTimings(timings, newTimings, i);
            }

            if (checkTimes(timings, timing)) {
                break;
            }
        }

        System.out.println("Done with runs.");

        boolean goodTimes = checkTimes(timings, true);

        if (! goodTimes) {
            timing = true;
        }

        if (timing) {
            printTimings(timings, Normalization.NONE);
            printTimings(timings, Normalization.TOP_LEFT);
            printTimings(timings, Normalization.PER_COLUMN);
        } else {
            System.out.println("\nAll times are within the expected ranges.");
        }
    }

    static public void combineTimings(double[][] target, double[][] newTimes,
            int oldWeight) {
        for (int i = 0; i < target.length; i++) {
            for (int j = 0; j < target[i].length; j++) {
                target[i][j] =
                    ((target[i][j] * oldWeight) + newTimes[i][j])
                    / (oldWeight + 1);
            }
        }
    }

    static public boolean checkTimes(double[][] timings, boolean print) {
        // expected increase over A1
        double[][] expected = {
            {  1.0,  2.3,  2.4,  3.3,  6.5, 12.0, 57.0,  94.0 },
            {  1.2,  2.4,  2.5,  3.4,  6.6, 12.2, 60.0,  95.0 },
            {  1.5,  2.6,  2.9,  3.5,  6.7, 12.4, 63.0,  96.0 },
            {  1.6,  2.8,  2.9,  3.6,  6.8, 12.6, 63.5,  97.0 },
            {  1.7,  3.0,  2.9,  3.7,  6.9, 12.8, 64.0,  98.0 },
            {  6.0,  6.0,  6.0,  7.0, 10.0, 15.0, 64.5, 105.0 },
            { 31.0, 31.2, 31.5, 34.0, 41.0, 43.0, 91.0, 135.0 },
        };

        boolean good = true;

        for (int x = 0; x < BODIES; x++) {
            for (int y = 0; y < LOOPS; y++) {
                double ratio = timings[x][y] / timings[0][0];
                if (ratio > expected[y][x]) {
                    if (print) {
                        System.out.printf("%c%d is too slow: %.3g vs. %.3g\n",
                                (char) (x + 'A'), y, ratio, expected[y][x]);
                    }
                    good = false;
                }
            }
        }

        return good;
    }

    static public double[][] runAllTests(int iters, boolean print) {
        // diters is used to get usec, not nanosec; hence the extra 1000.
        double diters = (double) iters * INNER_COUNT * 1000;

        double[][] timings = new double[BODIES][LOOPS];
        long t0, t1, t2, t3, t4, t5, t6, t7;

        // Column A

        if (print) {
            System.out.println("Running A...");
        }

        t0 = System.nanoTime();
        testA0(iters);
        t1 = System.nanoTime();
        testA1(iters);
        t2 = System.nanoTime();
        testA2(iters);
        t3 = System.nanoTime();
        testA3(iters);
        t4 = System.nanoTime();
        testA4(iters);
        t5 = System.nanoTime();
        testA5(iters);
        t6 = System.nanoTime();
        testA6(iters);
        t7 = System.nanoTime();

        timings[0][0] = (t1 - t0) / diters;
        timings[0][1] = (t2 - t1) / diters;
        timings[0][2] = (t3 - t2) / diters;
        timings[0][3] = (t4 - t3) / diters;
        timings[0][4] = (t5 - t4) / diters;
        timings[0][5] = (t6 - t5) / diters;
        timings[0][6] = (t7 - t6) / diters;

        // Column B

        if (print) {
            System.out.println("Running B...");
        }

        t0 = System.nanoTime();
        testB0(iters);
        t1 = System.nanoTime();
        testB1(iters);
        t2 = System.nanoTime();
        testB2(iters);
        t3 = System.nanoTime();
        testB3(iters);
        t4 = System.nanoTime();
        testB4(iters);
        t5 = System.nanoTime();
        testB5(iters);
        t6 = System.nanoTime();
        testB6(iters);
        t7 = System.nanoTime();

        timings[1][0] = (t1 - t0) / diters;
        timings[1][1] = (t2 - t1) / diters;
        timings[1][2] = (t3 - t2) / diters;
        timings[1][3] = (t4 - t3) / diters;
        timings[1][4] = (t5 - t4) / diters;
        timings[1][5] = (t6 - t5) / diters;
        timings[1][6] = (t7 - t6) / diters;

        // Column C

        if (print) {
            System.out.println("Running C...");
        }

        t0 = System.nanoTime();
        testC0(iters);
        t1 = System.nanoTime();
        testC1(iters);
        t2 = System.nanoTime();
        testC2(iters);
        t3 = System.nanoTime();
        testC3(iters);
        t4 = System.nanoTime();
        testC4(iters);
        t5 = System.nanoTime();
        testC5(iters);
        t6 = System.nanoTime();
        testC6(iters);
        t7 = System.nanoTime();

        timings[2][0] = (t1 - t0) / diters;
        timings[2][1] = (t2 - t1) / diters;
        timings[2][2] = (t3 - t2) / diters;
        timings[2][3] = (t4 - t3) / diters;
        timings[2][4] = (t5 - t4) / diters;
        timings[2][5] = (t6 - t5) / diters;
        timings[2][6] = (t7 - t6) / diters;

        // Column D

        if (print) {
            System.out.println("Running D...");
        }

        t0 = System.nanoTime();
        testD0(iters);
        t1 = System.nanoTime();
        testD1(iters);
        t2 = System.nanoTime();
        testD2(iters);
        t3 = System.nanoTime();
        testD3(iters);
        t4 = System.nanoTime();
        testD4(iters);
        t5 = System.nanoTime();
        testD5(iters);
        t6 = System.nanoTime();
        testD6(iters);
        t7 = System.nanoTime();

        timings[3][0] = (t1 - t0) / diters;
        timings[3][1] = (t2 - t1) / diters;
        timings[3][2] = (t3 - t2) / diters;
        timings[3][3] = (t4 - t3) / diters;
        timings[3][4] = (t5 - t4) / diters;
        timings[3][5] = (t6 - t5) / diters;
        timings[3][6] = (t7 - t6) / diters;

        // Column E

        if (print) {
            System.out.println("Running E...");
        }

        t0 = System.nanoTime();
        testE0(iters);
        t1 = System.nanoTime();
        testE1(iters);
        t2 = System.nanoTime();
        testE2(iters);
        t3 = System.nanoTime();
        testE3(iters);
        t4 = System.nanoTime();
        testE4(iters);
        t5 = System.nanoTime();
        testE5(iters);
        t6 = System.nanoTime();
        testE6(iters);
        t7 = System.nanoTime();

        timings[4][0] = (t1 - t0) / diters;
        timings[4][1] = (t2 - t1) / diters;
        timings[4][2] = (t3 - t2) / diters;
        timings[4][3] = (t4 - t3) / diters;
        timings[4][4] = (t5 - t4) / diters;
        timings[4][5] = (t6 - t5) / diters;
        timings[4][6] = (t7 - t6) / diters;

        // Column F

        if (print) {
            System.out.println("Running F...");
        }

        t0 = System.nanoTime();
        testF0(iters);
        t1 = System.nanoTime();
        testF1(iters);
        t2 = System.nanoTime();
        testF2(iters);
        t3 = System.nanoTime();
        testF3(iters);
        t4 = System.nanoTime();
        testF4(iters);
        t5 = System.nanoTime();
        testF5(iters);
        t6 = System.nanoTime();
        testF6(iters);
        t7 = System.nanoTime();

        timings[5][0] = (t1 - t0) / diters;
        timings[5][1] = (t2 - t1) / diters;
        timings[5][2] = (t3 - t2) / diters;
        timings[5][3] = (t4 - t3) / diters;
        timings[5][4] = (t5 - t4) / diters;
        timings[5][5] = (t6 - t5) / diters;
        timings[5][6] = (t7 - t6) / diters;

        // Reduce the iters for the last two, since they're much slower.

        iters /= 5;
        diters /= 5;

        // Column G

        if (print) {
            System.out.println("Running G...");
        }

        t0 = System.nanoTime();
        testG0(iters);
        t1 = System.nanoTime();
        testG1(iters);
        t2 = System.nanoTime();
        testG2(iters);
        t3 = System.nanoTime();
        testG3(iters);
        t4 = System.nanoTime();
        testG4(iters);
        t5 = System.nanoTime();
        testG5(iters);
        t6 = System.nanoTime();
        testG6(iters);
        t7 = System.nanoTime();

        timings[6][0] = (t1 - t0) / diters;
        timings[6][1] = (t2 - t1) / diters;
        timings[6][2] = (t3 - t2) / diters;
        timings[6][3] = (t4 - t3) / diters;
        timings[6][4] = (t5 - t4) / diters;
        timings[6][5] = (t6 - t5) / diters;
        timings[6][6] = (t7 - t6) / diters;

        // Column H

        if (print) {
            System.out.println("Running H...");
        }

        t0 = System.nanoTime();
        testH0(iters);
        t1 = System.nanoTime();
        testH1(iters);
        t2 = System.nanoTime();
        testH2(iters);
        t3 = System.nanoTime();
        testH3(iters);
        t4 = System.nanoTime();
        testH4(iters);
        t5 = System.nanoTime();
        testH5(iters);
        t6 = System.nanoTime();
        testH6(iters);
        t7 = System.nanoTime();

        timings[7][0] = (t1 - t0) / diters;
        timings[7][1] = (t2 - t1) / diters;
        timings[7][2] = (t3 - t2) / diters;
        timings[7][3] = (t4 - t3) / diters;
        timings[7][4] = (t5 - t4) / diters;
        timings[7][5] = (t6 - t5) / diters;
        timings[7][6] = (t7 - t6) / diters;

        return timings;
    }

    // Helper bits and pieces

    static private final int INNER_COUNT = 100;
    static private final int[] INNER_ARRAY = new int[INNER_COUNT];
    static private final ArrayList<Object> INNER_LIST =
        new ArrayList<Object>(INNER_COUNT);
    static private final Target TARGET = new Target();
    static private final int ARRAY_BYTES = 8192;
    static private final byte[] BYTES_1 = new byte[ARRAY_BYTES];
    static private final byte[] BYTES_2 = new byte[ARRAY_BYTES];

    static {
        for (int i = 0; i < INNER_COUNT; i++) {
            INNER_LIST.add(null);
        }
    }

    public static class Target {
        public int value;
        public int size = INNER_COUNT;

        public void simple() {
            // empty
        }

        public int size() {
            return size;
        }
    }

    // The tests themselves

    static public void testA0(int iters) {
        for (int outer = iters; outer > 0; outer--) {
            for (int i = INNER_COUNT; i > 0; i--) {
                // empty
            }
        }
    }

    static public void testA1(int iters) {
        int count = INNER_COUNT;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < count; i++) {
                // empty
            }
        }
    }

    static public void testA2(int iters) {
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < array.length; i++) {
                // empty
            }
        }
    }

    static public void testA3(int iters) {
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i : array) {
                // empty
            }
        }
    }

    static public void testA4(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size; i++) {
                // empty
            }
        }
    }

    static public void testA5(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size(); i++) {
                // empty
            }
        }
    }

    static public void testA6(int iters) {
        ArrayList<Object> list = INNER_LIST;

        for (int outer = iters; outer > 0; outer--) {
            for (Object o : list) {
                // empty
            }
        }
    }

    static public void testB0(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = INNER_COUNT; i > 0; i--) {
                target.value++;
            }
        }
    }

    static public void testB1(int iters) {
        Target target = TARGET;
        int count = INNER_COUNT;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < count; i++) {
                target.value++;
            }
        }
    }

    static public void testB2(int iters) {
        Target target = TARGET;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < array.length; i++) {
                target.value++;
            }
        }
    }

    static public void testB3(int iters) {
        Target target = TARGET;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i : array) {
                target.value++;
            }
        }
    }

    static public void testB4(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size; i++) {
                target.value++;
            }
        }
    }

    static public void testB5(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size(); i++) {
                target.value++;
            }
        }
    }

    static public void testB6(int iters) {
        Target target = TARGET;
        ArrayList<Object> list = INNER_LIST;

        for (int outer = iters; outer > 0; outer--) {
            for (Object o : list) {
                target.value++;
            }
        }
    }

    static public void testC0(int iters) {
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = INNER_COUNT - 1; i >= 0; i--) {
                array[i]++;
            }
        }
    }

    static public void testC1(int iters) {
        int[] array = INNER_ARRAY;
        int count = INNER_COUNT;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < count; i++) {
                array[i]++;
            }
        }
    }

    static public void testC2(int iters) {
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < array.length; i++) {
                array[i]++;
            }
        }
    }

    static public void testC3(int iters) {
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i : array) {
                array[0] = i + 1;
            }
        }
    }

    static public void testC4(int iters) {
        Target target = TARGET;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size; i++) {
                array[i]++;
            }
        }
    }

    static public void testC5(int iters) {
        int[] array = INNER_ARRAY;
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size(); i++) {
                array[i]++;
            }
        }
    }

    static public void testC6(int iters) {
        int[] array = INNER_ARRAY;
        ArrayList<Object> list = INNER_LIST;

        for (int outer = iters; outer > 0; outer--) {
            for (Object o : list) {
                array[0]++;
            }
        }
    }

    static public void testD0(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = INNER_COUNT; i > 0; i--) {
                target.simple();
            }
        }
    }

    static public void testD1(int iters) {
        Target target = TARGET;
        int count = INNER_COUNT;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < count; i++) {
                target.simple();
            }
        }
    }

    static public void testD2(int iters) {
        Target target = TARGET;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < array.length; i++) {
                target.simple();
            }
        }
    }

    static public void testD3(int iters) {
        Target target = TARGET;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i : array) {
                target.simple();
            }
        }
    }

    static public void testD4(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size; i++) {
                target.simple();
            }
        }
    }

    static public void testD5(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size(); i++) {
                target.simple();
            }
        }
    }

    static public void testD6(int iters) {
        Target target = TARGET;
        ArrayList<Object> list = INNER_LIST;

        for (int outer = iters; outer > 0; outer--) {
            for (Object o : list) {
                target.simple();
            }
        }
    }

    static public void testE0(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = INNER_COUNT; i > 0; i--) {
                synchronized (target) {
                    target.simple();
                }
            }
        }
    }

    static public void testE1(int iters) {
        Target target = TARGET;
        int count = INNER_COUNT;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < count; i++) {
                synchronized (target) {
                    target.simple();
                }
            }
        }
    }

    static public void testE2(int iters) {
        Target target = TARGET;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < array.length; i++) {
                synchronized (target) {
                    target.simple();
                }
            }
        }
    }

    static public void testE3(int iters) {
        Target target = TARGET;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i : array) {
                synchronized (target) {
                    target.simple();
                }
            }
        }
    }

    static public void testE4(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size; i++) {
                synchronized (target) {
                    target.simple();
                }
            }
        }
    }

    static public void testE5(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size(); i++) {
                synchronized (target) {
                    target.simple();
                }
            }
        }
    }

    static public void testE6(int iters) {
        Target target = TARGET;
        ArrayList<Object> list = INNER_LIST;

        for (int outer = iters; outer > 0; outer--) {
            for (Object o : list) {
                synchronized (target) {
                    target.simple();
                }
            }
        }
    }

    static public void testF0(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = INNER_COUNT; i > 0; i--) {
                target.simple();
                target.simple();
                target.simple();
                target.simple();
                target.simple();
            }
        }
    }

    static public void testF1(int iters) {
        Target target = TARGET;
        int count = INNER_COUNT;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < count; i++) {
                target.simple();
                target.simple();
                target.simple();
                target.simple();
                target.simple();
            }
        }
    }

    static public void testF2(int iters) {
        Target target = TARGET;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < array.length; i++) {
                target.simple();
                target.simple();
                target.simple();
                target.simple();
                target.simple();
            }
        }
    }

    static public void testF3(int iters) {
        Target target = TARGET;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i : array) {
                target.simple();
                target.simple();
                target.simple();
                target.simple();
                target.simple();
            }
        }
    }

    static public void testF4(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size; i++) {
                target.simple();
                target.simple();
                target.simple();
                target.simple();
                target.simple();
            }
        }
    }

    static public void testF5(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size(); i++) {
                target.simple();
                target.simple();
                target.simple();
                target.simple();
                target.simple();
            }
        }
    }

    static public void testF6(int iters) {
        Target target = TARGET;
        ArrayList<Object> list = INNER_LIST;

        for (int outer = iters; outer > 0; outer--) {
            for (Object o : list) {
                target.simple();
                target.simple();
                target.simple();
                target.simple();
                target.simple();
            }
        }
    }

    static public void testG0(int iters) {
        for (int outer = iters; outer > 0; outer--) {
            for (int i = INNER_COUNT; i > 0; i--) {
                new Target();
            }
        }
    }

    static public void testG1(int iters) {
        int count = INNER_COUNT;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < count; i++) {
                new Target();
            }
        }
    }

    static public void testG2(int iters) {
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < array.length; i++) {
                new Target();
            }
        }
    }

    static public void testG3(int iters) {
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i : array) {
                new Target();
            }
        }
    }

    static public void testG4(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size; i++) {
                new Target();
            }
        }
    }

    static public void testG5(int iters) {
        Target target = TARGET;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size(); i++) {
                new Target();
            }
        }
    }

    static public void testG6(int iters) {
        ArrayList<Object> list = INNER_LIST;

        for (int outer = iters; outer > 0; outer--) {
            for (Object o : list) {
                new Target();
            }
        }
    }

    static public void testH0(int iters) {
        byte[] b1 = BYTES_1;
        byte[] b2 = BYTES_2;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = INNER_COUNT; i > 0; i--) {
                System.arraycopy(b1, 0, b2, 0, ARRAY_BYTES);
            }
        }
    }

    static public void testH1(int iters) {
        byte[] b1 = BYTES_1;
        byte[] b2 = BYTES_2;
        int count = INNER_COUNT;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < count; i++) {
                System.arraycopy(b1, 0, b2, 0, ARRAY_BYTES);
            }
        }
    }

    static public void testH2(int iters) {
        byte[] b1 = BYTES_1;
        byte[] b2 = BYTES_2;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < array.length; i++) {
                System.arraycopy(b1, 0, b2, 0, ARRAY_BYTES);
            }
        }
    }

    static public void testH3(int iters) {
        byte[] b1 = BYTES_1;
        byte[] b2 = BYTES_2;
        int[] array = INNER_ARRAY;

        for (int outer = iters; outer > 0; outer--) {
            for (int i : array) {
                System.arraycopy(b1, 0, b2, 0, ARRAY_BYTES);
            }
        }
    }

    static public void testH4(int iters) {
        Target target = TARGET;
        byte[] b1 = BYTES_1;
        byte[] b2 = BYTES_2;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size; i++) {
                System.arraycopy(b1, 0, b2, 0, ARRAY_BYTES);
            }
        }
    }

    static public void testH5(int iters) {
        Target target = TARGET;
        byte[] b1 = BYTES_1;
        byte[] b2 = BYTES_2;

        for (int outer = iters; outer > 0; outer--) {
            for (int i = 0; i < target.size(); i++) {
                System.arraycopy(b1, 0, b2, 0, ARRAY_BYTES);
            }
        }
    }

    static public void testH6(int iters) {
        byte[] b1 = BYTES_1;
        byte[] b2 = BYTES_2;
        ArrayList<Object> list = INNER_LIST;

        for (int outer = iters; outer > 0; outer--) {
            for (Object o : list) {
                System.arraycopy(b1, 0, b2, 0, ARRAY_BYTES);
            }
        }
    }
}
