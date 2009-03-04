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

#ifndef _DALVIK_FLOAT12_H
#define _DALVIK_FLOAT12_H

/* Encodes a 32-bit number in 12 bits with +/-1.5% error,
 * though the majority (80%) are within +/-0.25%.
 *
 * The encoding looks like:
 *
 *     EEEMMMMM MMMMMMMM MMMMMMMM
 *     76543210 76543210 76543210
 *
 * where EEE is a base-16 exponent and MMMM is the mantissa.
 * The output value is (MMMM * 16^EEE), or (MMMM << (EEE * 4)).
 *
 * TODO: do this in a less brain-dead way.  I'm sure we can do
 *       it without all of these loops.
 */
inline unsigned short intToFloat12(unsigned int val)
{
    int oval = val;
    int shift = 0;

    /* Shift off the precision we don't care about.
     * Don't round here; it biases the values too high
     * (such that the encoded value is always greater
     * than the actual value)
     */
    unsigned int pval = val;
    while (val > 0x1ff) {
        pval = val;
        val >>= 1;
        shift++;
    }
    if (shift > 0 && (pval & 1)) {
        /* Round based on the last bit we shifted off.
         */
        val++;
        if (val > 0x1ff) {
            val = (val + 1) >> 1;
            shift++;
        }
    }

    /* Shift off enough bits to create a valid exponent.
     * Since we care about the bits we're losing, be sure
     * to round them.
     */
    while (shift % 4 != 0) {
        val = (val + 1) >> 1;
        shift++;
    }

    /* In the end, only round by the most-significant lost bit.
     * This centers the values around the closest match.
     * All of the rounding we did above guarantees that this
     * round won't overflow past 0x1ff.
     */
    if (shift > 0) {
        val = ((oval >> (shift - 1)) + 1) >> 1;
    }

    val |= (shift / 4) << 9;
    return val;
}

inline unsigned int float12ToInt(unsigned short f12)
{
    return (f12 & 0x1ff) << ((f12 >> 9) * 4);
}

#if 0   // testing

#include <stdio.h>
int main(int argc, char *argv[])
{
    if (argc != 3) {
        fprintf(stderr, "usage: %s <min> <max>\n", argv[0]);
        return 1;
    }

    unsigned int min = atoi(argv[1]);
    unsigned int max = atoi(argv[2]);
    if (min > max) {
        int t = min;
        max = min;
        min = t;
    } else if (min == max) {
        max++;
    }

    while (min < max) {
        unsigned int out;
        unsigned short sf;

        sf = intToFloat12(min);
        out = float12ToInt(sf);
//        printf("%d 0x%03x / 0x%03x %d (%d)\n", min, min, sf, out, (int)min - (int)out);
        printf("%6.6f %d %d\n", ((float)(int)(min - out)) / (float)(int)min, min, out);
        if (min <= 8192) {
            min++;
        } else if (min < 10000) {
            min += 10;
        } else if (min < 100000) {
            min += 1000;
        } else {
            min += 10000;
        }
    }
    return 0;
}

#endif  // testing

#endif  // _DALVIK_FLOAT12_H
