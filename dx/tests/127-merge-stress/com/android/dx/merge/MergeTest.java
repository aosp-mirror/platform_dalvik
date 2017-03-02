/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.dx.merge;

import com.android.dex.Dex;
import com.android.dex.DexIndexOverflowException;
import com.android.dx.command.dexer.DxContext;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

/**
 * This test tries to merge given dex files at random, a first pass at 2 by 2, followed by
 * a second pass doing multi-way merges.
 */
public class MergeTest {

  private static final int NUMBER_OF_TRIES = 1000;

  public static void main(String[] args) throws Throwable {
    Random random = new Random();
    for (int pass = 0; pass < 2; pass++) {
      for (int i = 0; i < NUMBER_OF_TRIES; i++) {
        // On the first pass only do 2-way merges, then do from 3 to 10 way merges
        // but not more to avoid dex index overflow.
        int numDex = pass == 0 ? 2 : random.nextInt(8) + 3;

        String[] fileNames = new String[numDex]; // only for the error message
        try {
          Dex[] dexesToMerge = new Dex[numDex];
          for (int j = 0; j < numDex; j++) {
            String fileName = args[random.nextInt(args.length)];
            fileNames[j] = fileName;
            dexesToMerge[j] = new Dex(new File(fileName));
          }
          new DexMerger(dexesToMerge, CollisionPolicy.KEEP_FIRST, new DxContext()).merge();
        } catch (DexIndexOverflowException e) {
          // ignore index overflow
        } catch (Throwable t) {
          System.err.println(
                  "Problem merging those dexes: " + Arrays.toString(fileNames));
          throw t;
        }
      }
    }
  }
}
