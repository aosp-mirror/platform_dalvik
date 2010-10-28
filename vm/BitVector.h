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

/* Handy iterator to walk through the bit positions set to 1 */
typedef struct BitVectorIterator {
    BitVector *pBits;
    u4 idx;
    u4 bitSize;
} BitVectorIterator;

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
 * dvmSetInitialBits sets all bits in [0..numBits-1]. Won't expand the vector.
 *
 * dvmIsBitSet returns "true" if the bit is set.
 */
int dvmAllocBit(BitVector* pBits);
bool dvmSetBit(BitVector* pBits, int num);
void dvmClearBit(BitVector* pBits, int num);
void dvmClearAllBits(BitVector* pBits);
void dvmSetInitialBits(BitVector* pBits, int numBits);
bool dvmIsBitSet(const BitVector* pBits, int num);

/* count the number of bits that have been set */
int dvmCountSetBits(const BitVector* pBits);

/* copy one vector to the other compatible one */
bool dvmCopyBitVector(BitVector *dest, const BitVector *src);

/*
 * Intersect two bit vectors and store the result to the dest vector.
 */
bool dvmIntersectBitVectors(BitVector *dest, const BitVector *src1,
                            const BitVector *src2);

/*
 * Unify two bit vectors and store the result to the dest vector.
 */
bool dvmUnifyBitVectors(BitVector *dest, const BitVector *src1,
                        const BitVector *src2);

/*
 * Compare two bit vectors and return true if difference is seen.
 */
bool dvmCompareBitVectors(const BitVector *src1, const BitVector *src2);

/* Initialize the iterator structure */
void dvmBitVectorIteratorInit(BitVector* pBits, BitVectorIterator* iterator);

/* Return the next position set to 1. -1 means end-of-vector reached */
int dvmBitVectorIteratorNext(BitVectorIterator* iterator);

#endif /*_DALVIK_BITVECTOR*/
