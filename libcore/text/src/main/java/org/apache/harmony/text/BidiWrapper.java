/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.text;

/**
 * TODO: type description
 */

public final class BidiWrapper {

    public static final int UBIDI_DEFAULT_LTR = 0xfe;

    public static final int UBIDI_DEFAULT_RTL = 0xff;

    public static final int UBIDI_MAX_EXPLICIT_LEVEL = 61;

    public static final int UBIDI_LEVEL_OVERRIDE = 0x80;

    public static final int UBIDI_KEEP_BASE_COMBINING = 1;

    public static final int UBIDI_DO_MIRRORING = 2;

    public static final int UBIDI_INSERT_LRM_FOR_NUMERIC = 4;

    public static final int UBIDI_REMOVE_BIDI_CONTROLS = 8;

    public static final int UBIDI_OUTPUT_REVERSE = 16;

    public static final int UBiDiDirection_UBIDI_LTR = 0;

    public static final int UBiDiDirection_UBIDI_RTL = 1;

    public static final int UBiDiDirection_UBIDI_MIXED = 2;

    // Allocate a UBiDi structure.
    public static native long ubidi_open();

    // ubidi_close() must be called to free the memory associated with a
    // UBiDi object.
    public static native void ubidi_close(long pBiDi);

    // Perform the Unicode BiDi algorithm.
    public static native void ubidi_setPara(long pBiDi, char[] text,
            int length, byte paraLevel, byte[] embeddingLevels);

    // ubidi_setLine() sets a UBiDi to contain the reordering information,
    // especially the resolved levels, for all the characters in a line of
    // text.
    public static native long ubidi_setLine(final long pParaBiDi, int start,
            int limit);

    // Get the directionality of the text.
    public static native int ubidi_getDirection(final long pBiDi);

    // Get the length of the text.
    public static native int ubidi_getLength(final long pBiDi);

    // Get the paragraph level of the text.
    public static native byte ubidi_getParaLevel(final long pBiDi);

    // Get an array of levels for each character.
    public static native byte[] ubidi_getLevels(long pBiDi);

    // Get the number of runs.
    public static native int ubidi_countRuns(long pBiDi);

    // Get the BidiRuns
    public static native BidiRun[] ubidi_getRuns(long pBidi);

    // This is a convenience function that does not use a UBiDi object
    public static native int[] ubidi_reorderVisual(byte[] levels, int length);
}
