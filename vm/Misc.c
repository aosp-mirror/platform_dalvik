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
#include "Dalvik.h"

#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <strings.h>
#include <ctype.h>
#include <time.h>
#include <sys/time.h>
#include <fcntl.h>
#include <cutils/ashmem.h>
#include <sys/mman.h>

#define ALIGN_UP_TO_PAGE_SIZE(p) \
    (((size_t)(p) + (SYSTEM_PAGE_SIZE - 1)) & ~(SYSTEM_PAGE_SIZE - 1))

/*
 * Print a hex dump in this format:
 *
01234567: 00 11 22 33 44 55 66 77 88 99 aa bb cc dd ee ff  0123456789abcdef\n
 *
 * If "mode" is kHexDumpLocal, we start at offset zero, and show a full
 * 16 bytes on the first line.  If it's kHexDumpMem, we make this look
 * like a memory dump, using the actual address, outputting a partial line
 * if "vaddr" isn't aligned on a 16-byte boundary.
 *
 * "priority" and "tag" determine the values passed to the log calls.
 *
 * Does not use printf() or other string-formatting calls.
 */
void dvmPrintHexDumpEx(int priority, const char* tag, const void* vaddr,
    size_t length, HexDumpMode mode)
{
    static const char gHexDigit[] = "0123456789abcdef";
    const unsigned char* addr = vaddr;
    char out[77];           /* exact fit */
    unsigned int offset;    /* offset to show while printing */
    char* hex;
    char* asc;
    int gap;
    //int trickle = 0;

    if (mode == kHexDumpLocal)
        offset = 0;
    else
        offset = (int) addr;

    memset(out, ' ', sizeof(out)-1);
    out[8] = ':';
    out[sizeof(out)-2] = '\n';
    out[sizeof(out)-1] = '\0';

    gap = (int) offset & 0x0f;
    while (length) {
        unsigned int lineOffset = offset & ~0x0f;
        int i, count;

        hex = out;
        asc = out + 59;

        for (i = 0; i < 8; i++) {
            *hex++ = gHexDigit[lineOffset >> 28];
            lineOffset <<= 4;
        }
        hex++;
        hex++;

        count = ((int)length > 16-gap) ? 16-gap : (int)length; /* cap length */
        assert(count != 0);
        assert(count+gap <= 16);

        if (gap) {
            /* only on first line */
            hex += gap * 3;
            asc += gap;
        }

        for (i = gap ; i < count+gap; i++) {
            *hex++ = gHexDigit[*addr >> 4];
            *hex++ = gHexDigit[*addr & 0x0f];
            hex++;
            if (*addr >= 0x20 && *addr < 0x7f /*isprint(*addr)*/)
                *asc++ = *addr;
            else
                *asc++ = '.';
            addr++;
        }
        for ( ; i < 16; i++) {
            /* erase extra stuff; only happens on last line */
            *hex++ = ' ';
            *hex++ = ' ';
            hex++;
            *asc++ = ' ';
        }

        LOG_PRI(priority, tag, "%s", out);
#if 0 //def HAVE_ANDROID_OS
        /*
         * We can overrun logcat easily by writing at full speed.  On the
         * other hand, we can make Eclipse time out if we're showing
         * packet dumps while debugging JDWP.
         */
        {
            if (trickle++ == 8) {
                trickle = 0;
                usleep(20000);
            }
        }
#endif

        gap = 0;
        length -= count;
        offset += count;
    }
}


/*
 * Fill out a DebugOutputTarget, suitable for printing to the log.
 */
void dvmCreateLogOutputTarget(DebugOutputTarget* target, int priority,
    const char* tag)
{
    assert(target != NULL);
    assert(tag != NULL);

    target->which = kDebugTargetLog;
    target->data.log.priority = priority;
    target->data.log.tag = tag;
}

/*
 * Fill out a DebugOutputTarget suitable for printing to a file pointer.
 */
void dvmCreateFileOutputTarget(DebugOutputTarget* target, FILE* fp)
{
    assert(target != NULL);
    assert(fp != NULL);

    target->which = kDebugTargetFile;
    target->data.file.fp = fp;
}

/*
 * Free "target" and any associated data.
 */
void dvmFreeOutputTarget(DebugOutputTarget* target)
{
    free(target);
}

/*
 * Print a debug message, to either a file or the log.
 */
void dvmPrintDebugMessage(const DebugOutputTarget* target, const char* format,
    ...)
{
    va_list args;

    va_start(args, format);

    switch (target->which) {
    case kDebugTargetLog:
        LOG_PRI_VA(target->data.log.priority, target->data.log.tag,
            format, args);
        break;
    case kDebugTargetFile:
        vfprintf(target->data.file.fp, format, args);
        break;
    default:
        LOGE("unexpected 'which' %d\n", target->which);
        break;
    }

    va_end(args);
}


/*
 * Allocate a bit vector with enough space to hold at least the specified
 * number of bits.
 */
BitVector* dvmAllocBitVector(int startBits, bool expandable)
{
    BitVector* bv;
    int count;

    assert(sizeof(bv->storage[0]) == 4);        /* assuming 32-bit units */
    assert(startBits >= 0);

    bv = (BitVector*) malloc(sizeof(BitVector));

    count = (startBits + 31) >> 5;

    bv->storageSize = count;
    bv->expandable = expandable;
    bv->storage = (u4*) malloc(count * sizeof(u4));
    memset(bv->storage, 0x00, count * sizeof(u4));
    return bv;
}

/*
 * Free a BitVector.
 */
void dvmFreeBitVector(BitVector* pBits)
{
    if (pBits == NULL)
        return;

    free(pBits->storage);
    free(pBits);
}

/*
 * "Allocate" the first-available bit in the bitmap.
 *
 * This is not synchronized.  The caller is expected to hold some sort of
 * lock that prevents multiple threads from executing simultaneously in
 * dvmAllocBit/dvmFreeBit.
 */
int dvmAllocBit(BitVector* pBits)
{
    int word, bit;

retry:
    for (word = 0; word < pBits->storageSize; word++) {
        if (pBits->storage[word] != 0xffffffff) {
            /*
             * There are unallocated bits in this word.  Return the first.
             */
            bit = ffs(~(pBits->storage[word])) -1;
            assert(bit >= 0 && bit < 32);
            pBits->storage[word] |= 1 << bit;
            return (word << 5) | bit;
        }
    }

    /*
     * Ran out of space, allocate more if we're allowed to.
     */
    if (!pBits->expandable)
        return -1;

    pBits->storage = realloc(pBits->storage,
                    (pBits->storageSize + kBitVectorGrowth) * sizeof(u4));
    memset(&pBits->storage[pBits->storageSize], 0x00,
        kBitVectorGrowth * sizeof(u4));
    pBits->storageSize += kBitVectorGrowth;
    goto retry;
}

/*
 * Mark the specified bit as "set".
 *
 * Returns "false" if the bit is outside the range of the vector and we're
 * not allowed to expand.
 */
bool dvmSetBit(BitVector* pBits, int num)
{
    assert(num >= 0);
    if (num >= pBits->storageSize * (int)sizeof(u4) * 8) {
        if (!pBits->expandable)
            return false;

        int newSize = (num + 31) >> 5;
        assert(newSize > pBits->storageSize);
        pBits->storage = realloc(pBits->storage, newSize * sizeof(u4));
        memset(&pBits->storage[pBits->storageSize], 0x00,
            (newSize - pBits->storageSize) * sizeof(u4));
        pBits->storageSize = newSize;
    }

    pBits->storage[num >> 5] |= 1 << (num & 0x1f);
    return true;
}

/*
 * Mark the specified bit as "clear".
 */
void dvmClearBit(BitVector* pBits, int num)
{
    assert(num >= 0 && num < (int) pBits->storageSize * (int)sizeof(u4) * 8);

    pBits->storage[num >> 5] &= ~(1 << (num & 0x1f));
}

/*
 * Mark all bits bit as "clear".
 */
void dvmClearAllBits(BitVector* pBits)
{
    int count = pBits->storageSize;
    memset(pBits->storage, 0, count * sizeof(u4));
}

/*
 * Determine whether or not the specified bit is set.
 */
bool dvmIsBitSet(const BitVector* pBits, int num)
{
    assert(num >= 0 && num < (int) pBits->storageSize * (int)sizeof(u4) * 8);

    int val = pBits->storage[num >> 5] & (1 << (num & 0x1f));
    return (val != 0);
}

/*
 * Count the number of bits that are set.
 */
int dvmCountSetBits(const BitVector* pBits)
{
    int word;
    int count = 0;

    for (word = 0; word < pBits->storageSize; word++) {
        u4 val = pBits->storage[word];

        if (val != 0) {
            if (val == 0xffffffff) {
                count += 32;
            } else {
                /* count the number of '1' bits */
                while (val != 0) {
                    val &= val - 1;
                    count++;
                }
            }
        }
    }

    return count;
}

/*
 * Copy a whole vector to the other. Only do that when the both vectors have
 * the same size and attribute.
 */
bool dvmCopyBitVector(BitVector *dest, const BitVector *src)
{
    if (dest->storageSize != src->storageSize ||
        dest->expandable != src->expandable)
        return false;
    memcpy(dest->storage, src->storage, sizeof(u4) * dest->storageSize);
    return true;
}

/*
 * Intersect two bit vectores and merge the result on top of the pre-existing
 * value in the dest vector.
 */
bool dvmIntersectBitVectors(BitVector *dest, const BitVector *src1,
                            const BitVector *src2)
{
    if (dest->storageSize != src1->storageSize ||
        dest->storageSize != src2->storageSize ||
        dest->expandable != src1->expandable ||
        dest->expandable != src2->expandable)
        return false;

    int i;
    for (i = 0; i < dest->storageSize; i++) {
        dest->storage[i] |= src1->storage[i] & src2->storage[i];
    }
    return true;
}

/*
 * Return a newly-allocated string in which all occurrences of '.' have
 * been changed to '/'.  If we find a '/' in the original string, NULL
 * is returned to avoid ambiguity.
 */
char* dvmDotToSlash(const char* str)
{
    char* newStr = strdup(str);
    char* cp = newStr;

    if (newStr == NULL)
        return NULL;

    while (*cp != '\0') {
        if (*cp == '/') {
            assert(false);
            return NULL;
        }
        if (*cp == '.')
            *cp = '/';
        cp++;
    }

    return newStr;
}

/*
 * Return a newly-allocated string for the "dot version" of the class
 * name for the given type descriptor. That is, The initial "L" and
 * final ";" (if any) have been removed and all occurrences of '/'
 * have been changed to '.'.
 */
char* dvmDescriptorToDot(const char* str)
{
    size_t at = strlen(str);
    char* newStr;

    if ((at >= 2) && (str[0] == 'L') && (str[at - 1] == ';')) {
        at -= 2; /* Two fewer chars to copy. */
        str++; /* Skip the 'L'. */
    }

    newStr = malloc(at + 1); /* Add one for the '\0'. */
    if (newStr == NULL)
        return NULL;

    newStr[at] = '\0';

    while (at > 0) {
        at--;
        newStr[at] = (str[at] == '/') ? '.' : str[at];
    }

    return newStr;
}

/*
 * Return a newly-allocated string for the type descriptor
 * corresponding to the "dot version" of the given class name. That
 * is, non-array names are surrounded by "L" and ";", and all
 * occurrences of '.' are changed to '/'.
 */
char* dvmDotToDescriptor(const char* str)
{
    size_t length = strlen(str);
    int wrapElSemi = 0;
    char* newStr;
    char* at;

    if (str[0] != '[') {
        length += 2; /* for "L" and ";" */
        wrapElSemi = 1;
    }

    newStr = at = malloc(length + 1); /* + 1 for the '\0' */

    if (newStr == NULL) {
        return NULL;
    }

    if (wrapElSemi) {
        *(at++) = 'L';
    }

    while (*str) {
        char c = *(str++);
        if (c == '.') {
            c = '/';
        }
        *(at++) = c;
    }

    if (wrapElSemi) {
        *(at++) = ';';
    }

    *at = '\0';
    return newStr;
}

/*
 * Return a newly-allocated string for the internal-form class name for
 * the given type descriptor. That is, the initial "L" and final ";" (if
 * any) have been removed.
 */
char* dvmDescriptorToName(const char* str)
{
    if (str[0] == 'L') {
        size_t length = strlen(str) - 1;
        char* newStr = malloc(length);

        if (newStr == NULL) {
            return NULL;
        }

        strlcpy(newStr, str + 1, length);
        return newStr;
    }

    return strdup(str);
}

/*
 * Return a newly-allocated string for the type descriptor for the given
 * internal-form class name. That is, a non-array class name will get
 * surrounded by "L" and ";", while array names are left as-is.
 */
char* dvmNameToDescriptor(const char* str)
{
    if (str[0] != '[') {
        size_t length = strlen(str);
        char* descriptor = malloc(length + 3);

        if (descriptor == NULL) {
            return NULL;
        }

        descriptor[0] = 'L';
        strcpy(descriptor + 1, str);
        descriptor[length + 1] = ';';
        descriptor[length + 2] = '\0';

        return descriptor;
    }

    return strdup(str);
}

/*
 * Get a notion of the current time, in nanoseconds.  This is meant for
 * computing durations (e.g. "operation X took 52nsec"), so the result
 * should not be used to get the current date/time.
 */
u8 dvmGetRelativeTimeNsec(void)
{
#ifdef HAVE_POSIX_CLOCKS
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return (u8)now.tv_sec*1000000000LL + now.tv_nsec;
#else
    struct timeval now;
    gettimeofday(&now, NULL);
    return (u8)now.tv_sec*1000000000LL + now.tv_usec * 1000LL;
#endif
}

/*
 * Get the per-thread CPU time, in nanoseconds.
 *
 * Only useful for time deltas.
 */
u8 dvmGetThreadCpuTimeNsec(void)
{
#ifdef HAVE_POSIX_CLOCKS
    struct timespec now;
    clock_gettime(CLOCK_THREAD_CPUTIME_ID, &now);
    return (u8)now.tv_sec*1000000000LL + now.tv_nsec;
#else
    return (u8) -1;
#endif
}

/*
 * Get the per-thread CPU time, in nanoseconds, for the specified thread.
 */
u8 dvmGetOtherThreadCpuTimeNsec(pthread_t thread)
{
#if 0 /*def HAVE_POSIX_CLOCKS*/
    int clockId;

    if (pthread_getcpuclockid(thread, &clockId) != 0)
        return (u8) -1;

    struct timespec now;
    clock_gettime(clockId, &now);
    return (u8)now.tv_sec*1000000000LL + now.tv_nsec;
#else
    return (u8) -1;
#endif
}


/*
 * Call this repeatedly, with successively higher values for "iteration",
 * to sleep for a period of time not to exceed "maxTotalSleep".
 *
 * For example, when called with iteration==0 we will sleep for a very
 * brief time.  On the next call we will sleep for a longer time.  When
 * the sum total of all sleeps reaches "maxTotalSleep", this returns false.
 *
 * The initial start time value for "relStartTime" MUST come from the
 * dvmGetRelativeTimeUsec call.  On the device this must come from the
 * monotonic clock source, not the wall clock.
 *
 * This should be used wherever you might be tempted to call sched_yield()
 * in a loop.  The problem with sched_yield is that, for a high-priority
 * thread, the kernel might not actually transfer control elsewhere.
 *
 * Returns "false" if we were unable to sleep because our time was up.
 */
bool dvmIterativeSleep(int iteration, int maxTotalSleep, u8 relStartTime)
{
    const int minSleep = 10000;
    u8 curTime;
    int curDelay;

    /*
     * Get current time, and see if we've already exceeded the limit.
     */
    curTime = dvmGetRelativeTimeUsec();
    if (curTime >= relStartTime + maxTotalSleep) {
        LOGVV("exsl: sleep exceeded (start=%llu max=%d now=%llu)\n",
            relStartTime, maxTotalSleep, curTime);
        return false;
    }

    /*
     * Compute current delay.  We're bounded by "maxTotalSleep", so no
     * real risk of overflow assuming "usleep" isn't returning early.
     * (Besides, 2^30 usec is about 18 minutes by itself.)
     *
     * For iteration==0 we just call sched_yield(), so the first sleep
     * at iteration==1 is actually (minSleep * 2).
     */
    curDelay = minSleep;
    while (iteration-- > 0)
        curDelay *= 2;
    assert(curDelay > 0);

    if (curTime + curDelay >= relStartTime + maxTotalSleep) {
        LOGVV("exsl: reduced delay from %d to %d\n",
            curDelay, (int) ((relStartTime + maxTotalSleep) - curTime));
        curDelay = (int) ((relStartTime + maxTotalSleep) - curTime);
    }

    if (iteration == 0) {
        LOGVV("exsl: yield\n");
        sched_yield();
    } else {
        LOGVV("exsl: sleep for %d\n", curDelay);
        usleep(curDelay);
    }
    return true;
}


/*
 * Set the "close on exec" flag so we don't expose our file descriptors
 * to processes launched by us.
 */
bool dvmSetCloseOnExec(int fd)
{
    int flags;

    /*
     * There's presently only one flag defined, so getting the previous
     * value of the fd flags is probably unnecessary.
     */
    flags = fcntl(fd, F_GETFD);
    if (flags < 0) {
        LOGW("Unable to get fd flags for fd %d\n", fd);
        return false;
    }
    if (fcntl(fd, F_SETFD, flags | FD_CLOEXEC) < 0) {
        LOGW("Unable to set close-on-exec for fd %d\n", fd);
        return false;
    }
    return true;
}

#if (!HAVE_STRLCPY)
/* Implementation of strlcpy() for platforms that don't already have it. */
size_t strlcpy(char *dst, const char *src, size_t size) {
    size_t srcLength = strlen(src);
    size_t copyLength = srcLength;

    if (srcLength > (size - 1)) {
        copyLength = size - 1;
    }

    if (size != 0) {
        strncpy(dst, src, copyLength);
        dst[copyLength] = '\0';
    }

    return srcLength;
}
#endif

/*
 *  Allocates a memory region using ashmem and mmap, initialized to
 *  zero.  Actual allocation rounded up to page multiple.  Returns
 *  NULL on failure.
 */
void *dvmAllocRegion(size_t size, int prot, const char *name) {
    void *base;
    int fd, ret;

    size = ALIGN_UP_TO_PAGE_SIZE(size);
    fd = ashmem_create_region(name, size);
    if (fd == -1) {
        return NULL;
    }
    base = mmap(NULL, size, prot, MAP_PRIVATE, fd, 0);
    ret = close(fd);
    if (base == MAP_FAILED) {
        return NULL;
    }
    if (ret == -1) {
        return NULL;
    }
    return base;
}

/* documented in header file */
const char* dvmPathToAbsolutePortion(const char* path) {
    if (path == NULL) {
        return NULL;
    }

    if (path[0] == '/') {
        /* It's a regular absolute path. Return it. */
        return path;
    }

    const char* sentinel = strstr(path, "/./");

    if (sentinel != NULL) {
        /* It's got the sentinel. Return a pointer to the second slash. */
        return sentinel + 2;
    }

    return NULL;
}
