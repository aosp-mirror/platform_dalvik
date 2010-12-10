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

/*
 * Miscellaneous utility functions.
 */
#ifndef _DALVIK_BITVECTOR
#define _DALVIK_BITVECTOR

/*
 * Expanding bitmap, used for tracking resources.  Bits are numbered starting
 * from zero.
 *
 * All operations on a BitVector are unsynchronized.
 */
typedef struct BitVector {
    bool    expandable;     /* expand bitmap if we run out? */
    int     storageSize;    /* current size, in 32-bit words */
    u4*     storage;
} BitVector;

/* allocate a bit vector with enough space to hold "startBits" bits */
BitVector* dvmAllocBitVector(int startBits, bool expandable);
void dvmFreeBitVector(BitVector* pBits);

/*
 * dvmAllocBit always allocates the first possible bit.  If we run out of
 * space in the bitmap, and it's not marked expandable, dvmAllocBit
 * returns -1.
 *
 * dvmSetBit sets the specified bit, expanding the vector if necessary
 * (and possible).
 *
 * dvmIsBitSet returns "true" if the bit is set.
 */
int dvmAllocBit(BitVector* pBits);
bool dvmSetBit(BitVector* pBits, int num);
void dvmClearBit(BitVector* pBits, int num);
void dvmClearAllBits(BitVector* pBits);
bool dvmIsBitSet(const BitVector* pBits, int num);

/* count the number of bits that have been set */
int dvmCountSetBits(const BitVector* pBits);

/* copy one vector to the other compatible one */
bool dvmCopyBitVector(BitVector *dest, const BitVector *src);

/*
 * Intersect two bit vectores and merge the result on top of the pre-existing
 * value in the dest vector.
 */
bool dvmIntersectBitVectors(BitVector *dest, const BitVector *src1,
                            const BitVector *src2);


#endif /*_DALVIK_BITVECTOR*/
