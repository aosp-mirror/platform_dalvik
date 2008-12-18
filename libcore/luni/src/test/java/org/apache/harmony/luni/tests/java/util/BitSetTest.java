/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.luni.tests.java.util;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.util.BitSet;

@TestTargetClass(BitSet.class) 
public class BitSetTest extends TestCase {

    /**
     * @tests java.util.BitSet#clear(int, int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "IndexOutOfBoundsException is not verified.",
      targets = {
        @TestTarget(
          methodName = "clear",
          methodArgs = {int.class, int.class}
        )
    })
    public void test_clearII() {
        // Regression for HARMONY-98
        BitSet bitset = new BitSet();
        for (int i = 0; i < 20; i++) {
            bitset.set(i);
        }
        bitset.clear(10, 10);
    }

    /**
     * @tests java.util.BitSet#flip(int, int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "IndexOutOfBoundsException is not verified.",
      targets = {
        @TestTarget(
          methodName = "flip",
          methodArgs = {int.class, int.class}
        )
    })
    public void test_flipII() {
        BitSet bitset = new BitSet();
        for (int i = 0; i < 20; i++) {
            bitset.set(i);
        }
        bitset.flip(10, 10);
    }

    /**
     * @tests java.util.BitSet#get(int, int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "IndexOutOfBoundsException is not verified.",
      targets = {
        @TestTarget(
          methodName = "get",
          methodArgs = {int.class, int.class}
        )
    })
    public void test_getII() {
        BitSet bitset = new BitSet(30);
        bitset.get(3, 3);
    }

    /**
     * @tests java.util.BitSet#set(int, int)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "IndexOutOfBoundsException is not verified.",
      targets = {
        @TestTarget(
          methodName = "set",
          methodArgs = {int.class, int.class}
        )
    })
    public void test_setII() {
        BitSet bitset = new BitSet(30);
        bitset.set(29, 29);
    }
}
