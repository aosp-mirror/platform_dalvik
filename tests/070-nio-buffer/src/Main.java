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

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class Main {
    public static void main(String[] args) {
         intFloatTest();
         basicShortTest();
    }

    /*
     * Create a buffer and fiddle with it.
     */
    public static void basicShortTest() {
        ByteBuffer directBuf = ByteBuffer.allocateDirect(64);
        //ByteBuffer directBuf = ByteBuffer.allocateDirect(65);

        ShortBuffer shortBuf = directBuf.asShortBuffer();

        short[] myShorts = {
            1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007,
            1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015,
            1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023,
            1024, 1025, 1026, 1027, 1028, 1029, 1030, 1031
        };

        shortBuf.position(0);
        shortBuf.put(myShorts, 0, 32);      // should work
        shortBuf.position(0);
        shortBuf.put(myShorts, 16, 16);     // should work
        shortBuf.put(myShorts, 16, 16);     // advance to end

        try {
            shortBuf.put(myShorts, 0, 1);     // should fail
            System.err.println("ERROR: out-of-bounds put succeeded\n");
        } catch (BufferOverflowException boe) {
            System.out.println("Got expected buffer overflow exception");
        }

        try {
            shortBuf.position(0);
            shortBuf.put(myShorts, 0, 33);     // should fail
            System.err.println("ERROR: out-of-bounds put succeeded\n");
        } catch (IndexOutOfBoundsException ioobe) {
            System.out.println("Got expected out-of-bounds exception");
        }

        try {
            shortBuf.position(16);
            shortBuf.put(myShorts, 0, 17);     // should fail
            System.err.println("ERROR: out-of-bounds put succeeded\n");
        } catch (BufferOverflowException boe) {
            System.out.println("Got expected buffer overflow exception");
        }
    }

    /*
     * Try this with either floats or ints; ints fail with
     * BufferOverflowException, floats work.
     *
     * From http://code.google.com/p/android/issues/detail?id=1585 .
     */
    public static void intFloatTest() {
        ByteBuffer direct = ByteBuffer.allocateDirect(100);
        direct.order(ByteOrder.nativeOrder());
        IntBuffer int1 = direct.asIntBuffer();
        int data[] = new int[25];
        //FloatBuffer int1 = direct.asFloatBuffer();
        //float data[] = new float[25];
        int1.clear ();
        int1.put (data);
        int1.position (0);

        int1.clear ();
        int1.put (data);
        int1.position (0);
    }
}
