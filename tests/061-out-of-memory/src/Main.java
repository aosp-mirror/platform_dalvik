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

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Exercise the construction and throwing of OutOfMemoryError.
 */
public class Main {
    public static void main(String args[]) {
        System.out.println("tests beginning");
        testHugeArray();
        testOomeLarge();
        testOomeSmall();
        System.out.println("tests succeeded");
    }

    private static void testHugeArray() {
        try {
            final int COUNT = 32768*32768 + 4;
            int[] tooBig = new int[COUNT];

            Arrays.fill(tooBig, 0xdd);
        } catch (OutOfMemoryError oom) {
            System.out.println("Got expected huge-array OOM");
        }
    }

    private static void testOomeLarge() {
        System.out.println("testOomeLarge beginning");

        /* Just shy of the typical max heap size so that it will actually
         * try to allocate it instead of short-circuiting.
         *
         * TODO: stop assuming the VM defaults to 16MB max
         */
        final int SIXTEEN_MB = (16 * 1024 * 1024 - 32);

        Boolean sawEx = false;
        byte a[];

        try {
            a = new byte[SIXTEEN_MB];
        } catch (OutOfMemoryError oom) {
            //Log.i(TAG, "HeapTest/OomeLarge caught " + oom);
            sawEx = true;
        }

        if (!sawEx) {
            throw new RuntimeException("Test failed: " +
                    "OutOfMemoryError not thrown");
        }

        System.out.println("testOomeLarge succeeded");
    }

    /* Do this in another method so that the GC has a chance of freeing the
     * list afterwards.  Even if we null out list when we're done, the conservative
     * GC may see a stale pointer to it in a register.
     *
     * TODO: stop assuming the VM defaults to 16MB max
     */
    private static boolean testOomeSmallInternal() {
        final int SIXTEEN_MB = (16 * 1024 * 1024);
        final int LINK_SIZE = 6 * 4; // estimated size of a LinkedList's node

        LinkedList<Object> list = new LinkedList<Object>();

        /* Allocate progressively smaller objects to fill up the entire heap.
         */
        int objSize = 1 * 1024 * 1024;
        while (objSize >= LINK_SIZE) {
            boolean sawEx = false;
            try {
                for (int i = 0; i < SIXTEEN_MB / objSize; i++) {
                    list.add((Object)new byte[objSize]);
                }
            } catch (OutOfMemoryError oom) {
                sawEx = true;
            }

            if (!sawEx) {
                return false;
            }

            objSize = (objSize * 4) / 5;
        }

        return true;
    }

    private static void testOomeSmall() {
        System.out.println("testOomeSmall beginning");
        if (!testOomeSmallInternal()) {
            /* Can't reliably throw this from inside the internal function, because
             * we may not be able to allocate the RuntimeException.
             */
            throw new RuntimeException("Test failed: " +
                    "OutOfMemoryError not thrown while filling heap");
        }
        System.out.println("testOomeSmall succeeded");
    }
}
