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
 * System utilities.
 */
#ifndef _LIBDEX_SYSUTIL
#define _LIBDEX_SYSUTIL

#include <sys/types.h>

/*
 * Use this to keep track of mapped segments.
 */
typedef struct MemMapping {
    void*   addr;           /* start of data */
    size_t  length;         /* length of data */

    void*   baseAddr;       /* page-aligned base address */
    size_t  baseLength;     /* length of mapping */
} MemMapping;

/*
 * Copy a map.
 */
void sysCopyMap(MemMapping* dst, const MemMapping* src);

/*
 * Load a file into a new shared memory segment.  All data from the current
 * offset to the end of the file is pulled in.
 *
 * The segment is read-write, allowing VM fixups.  (It should be modified
 * to support .gz/.zip compressed data.)
 *
 * On success, "pMap" is filled in, and zero is returned.
 */
int sysLoadFileInShmem(int fd, MemMapping* pMap);

/*
 * Map a file (from fd's current offset) into a shared,
 * read-only memory segment.
 *
 * On success, "pMap" is filled in, and zero is returned.
 */
int sysMapFileInShmem(int fd, MemMapping* pMap);

/*
 * Like sysMapFileInShmem, but on only part of a file.
 */
int sysMapFileSegmentInShmem(int fd, off_t start, long length,
    MemMapping* pMap);

/*
 * Create a private anonymous mapping, useful for large allocations.
 *
 * On success, "pMap" is filled in, and zero is returned.
 */
int sysCreatePrivateMap(size_t length, MemMapping* pMap);

/*
 * Release the pages associated with a shared memory segment.
 *
 * This does not free "pMap"; it just releases the memory.
 */
void sysReleaseShmem(MemMapping* pMap);

#endif /*_DALVIK_SYSUTIL*/
