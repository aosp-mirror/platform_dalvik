/*
 * Copyright (C) 2009 The Android Open Source Project
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

/**
 * Native support for dalvik.system.SamplingProfiler
 */

#define LOG_TAG "SamplingProfiler"

#include <cutils/log.h>

#include "Dalvik.h"
#include "native/InternalNativePriv.h"
#include "native/SystemThread.h"

// ~20k
#define INITIAL_CAPACITY 1024

// ~80k
#define MAX_CAPACITY 4096

typedef enum {
    /** The "event thread". */
    EVENT_THREAD,
    /** Not the "event thread". */
    OTHER_THREAD
} ThreadType;

#define THREAD_TYPE_SIZE (OTHER_THREAD + 1)

typedef enum {
    /** Executing bytecode. */
    RUNNING_THREAD,
    /** Waiting on a lock or VM resource. */
    SUSPENDED_THREAD
} ThreadState;

#define THREAD_STATE_SIZE (SUSPENDED_THREAD + 1)

typedef enum {
    /** This method is in the call stack. */
    CALLING_METHOD,
    /** VM is in this method. */
    LEAF_METHOD
} MethodState;

#define METHOD_STATE_SIZE (LEAF_METHOD + 1)

/** SampleSet entry. */
typedef struct {
    /** Entry key. */
    const Method* method; // 4 bytes
    /** Sample counts for method divided by thread type and state. */
    u2 counts[THREAD_TYPE_SIZE][THREAD_STATE_SIZE][METHOD_STATE_SIZE]; // 16B
} MethodCount;

/**
 * Set of MethodCount entries.
 *
 * Note: If we ever support class unloading, we'll need to make this a GC root
 * so the methods don't get reclaimed.
 */
typedef struct {
    /** Hash collisions. */
    int collisions;
    /** Number of entries in set. */
    int size;
    /** Number of slots. */
    int capacity;
    /** Maximum number of entries this set can hold. 3/4 capacity. */
    int maxSize;
    /** Used to convert a hash to an entry index. */
    int mask;
    /** Entry table. */
    MethodCount* entries;
    /** The event thread. */
    Thread* eventThread;
} SampleSet;

/**
 * Initializes an empty set with the given capacity (which must be a power of
 * two). Allocates memory for the entry array which must be freed.
 */
static SampleSet newSampleSet(int capacity) {
    SampleSet set;
    set.collisions = 0;
    set.size = 0;
    set.capacity = capacity;
    set.maxSize = (capacity >> 2) * 3; // 3/4 capacity
    set.mask = capacity - 1;
    set.entries = (MethodCount*) calloc(sizeof(MethodCount), capacity);
    set.eventThread = NULL;
    return set;
}

/** Hashes the given pointer. */
static u4 hash(const void* p) {
    u4 h = (u4) p;

    // This function treats its argument as seed for a Marsaglia
    // xorshift random number generator, and produces the next
    // value. The particular xorshift parameters used here tend to
    // spread bits downward, to better cope with keys that differ
    // only in upper bits, which otherwise excessively collide in
    // small tables.
    h ^= h >> 11;
    h ^= h << 7;
    return h ^ (h >> 16);
}

/** Doubles capacity of SampleSet. */
static void expand(SampleSet* oldSet) {
    // TODO: Handle newSet.entries == NULL
    SampleSet newSet = newSampleSet(oldSet->capacity << 1);
    LOGI("Expanding sample set capacity to %d.", newSet.capacity);
    int oldIndex;
    MethodCount* oldEntries = oldSet->entries;
    for (oldIndex = 0; oldIndex < oldSet->size; oldIndex++) {
        MethodCount oldEntry = oldEntries[oldIndex];
        if (oldEntry.method != NULL) {
            // Find the first empty slot.
            int start = hash(oldEntry.method) & newSet.mask;
            int i = start;
            while (newSet.entries[i].method != NULL) {
                i = (i + 1) & newSet.mask;
            }

            // Copy the entry into the empty slot.
            newSet.entries[i] = oldEntry;
            newSet.collisions += (i != start);
        }
    }
    free(oldEntries);
    newSet.size = oldSet->size;
    newSet.eventThread = oldSet->eventThread;
    *oldSet = newSet;
}

/** Increments counter for method in set. */
static void countMethod(SampleSet* set, const Method* method,
        ThreadType threadType, ThreadState threadState,
        MethodState methodState) {
    MethodCount* entries = set->entries;
    int start = hash(method) & set->mask;
    int i;
    for (i = start;; i = (i + 1) & set->mask) {
        MethodCount* entry = &entries[i];

        if (entry->method == method) {
            // We found an existing entry.
            entry->counts[threadType][threadState][methodState]++;
            return;
        }

        if (entry->method == NULL) {
            // Add a new entry.
            if (set->size < set->maxSize) {
                entry->method = method;
                entry->counts[threadType][threadState][methodState] = 1;
                set->collisions += (i != start);
                set->size++;
            } else {
                if (set->capacity < MAX_CAPACITY) {
                    // The set is 3/4 full. Expand it, and then add the entry.
                    expand(set);
                    countMethod(set, method, threadType, threadState,
                            methodState);
                } else {
                    // Don't add any more entries.
                    // TODO: Should we replace the LRU entry?
                }
            }
            return;
        }
    }
}

/** Clears all entries from sample set. */
static void clearSampleSet(SampleSet* set) {
    set->collisions = 0;
    set->size = 0;
    memset(set->entries, 0, set->capacity * sizeof(MethodCount));
}

/**
 * Collects a sample from a single, possibly running thread.
 */
static void sample(SampleSet* set, Thread* thread) {
    ThreadType threadType = thread == set->eventThread
        ? EVENT_THREAD : OTHER_THREAD;

    ThreadState threadState;
    switch (dvmGetSystemThreadStatus(thread)) {
        case THREAD_RUNNING: threadState = RUNNING_THREAD; break;
        case THREAD_NATIVE: return; // Something went wrong. Skip this thread.
        default: threadState = SUSPENDED_THREAD; // includes PAGING
    }

    /*
     * This code reads the stack concurrently, so it needs to defend against
     * garbage data that will certainly result from the stack changing out
     * from under us.
     */

    // Top of the stack.
    void* stackTop = thread->interpStackStart;

    void* currentFrame = thread->curFrame;
    if (currentFrame == NULL) {
        return;
    }

    MethodState methodState = LEAF_METHOD;
    while (true) {
        StackSaveArea* saveArea = SAVEAREA_FROM_FP(currentFrame);

        const Method* method = saveArea->method;
        // Count the method now. We'll validate later that it's a real Method*.
        if (method != NULL) {
            countMethod(set, method, threadType, threadState, methodState);
            methodState = CALLING_METHOD;
        }

        void* callerFrame = saveArea->prevFrame;
        if (callerFrame == NULL // No more callers.
                || callerFrame > stackTop // Stack underflow!
                || callerFrame < currentFrame // Wrong way!
            ) {
            break;
        }

        currentFrame = callerFrame;
    }
}

/**
 * Collects samples.
 */
static void Dalvik_dalvik_system_SamplingProfiler_sample(const u4* args,
        JValue* pResult) {
    SampleSet* set = (SampleSet*) args[0];
    dvmLockThreadList(dvmThreadSelf());
    Thread* thread = gDvm.threadList;
    int sampledThreads = 0;
    Thread* self = dvmThreadSelf();
    while (thread != NULL) {
        if (thread != self) {
            sample(set, thread);
            sampledThreads++;
        }
        thread = thread->next;
    }
    dvmUnlockThreadList();
    RETURN_INT(sampledThreads);
}

/**
 * Gets the number of methods in the sample set.
 */
static void Dalvik_dalvik_system_SamplingProfiler_size(const u4* args,
        JValue* pResult) {
    SampleSet* set = (SampleSet*) args[0];
    RETURN_INT(set->size);
}

/**
 * Gets the number of collisions in the sample set.
 */
static void Dalvik_dalvik_system_SamplingProfiler_collisions(const u4* args,
        JValue* pResult) {
    SampleSet* set = (SampleSet*) args[0];
    RETURN_INT(set->collisions);
}

/**
 * Returns true if the method is in the given table.
 */
static bool inTable(const Method* method, const Method* table,
        int tableLength) {
    if (tableLength < 1) {
        return false;
    }

    const Method* last = table + (tableLength - 1);

    // Cast to char* to handle misaligned pointers.
    return (char*) method >= (char*) table
        && (char*) method <= (char*) last;
}

/** Entry in a hash of method counts by class. */
typedef struct mcw {
    /** Decorated method count. */
    MethodCount* methodCount;

    /** Shortcut to methodCount->method->clazz. */
    ClassObject* clazz;
    /** Pointer to class name that enables us to chop off the first char. */
    const char* className;
    /** Cached string lengths. */
    u2 classNameLength;
    u2 methodNameLength;

    /** Next method in the same class. */
    struct mcw* next;
} MethodCountWrapper;

/** Returns true if we can trim the first and last chars in the class name. */
static bool isNormalClassName(const char* clazzName, int length) {
    return (length >= 2) && (clazzName[0] == 'L')
        && (clazzName[length - 1] == ';');
}

/**
 * Heurtistically guesses whether or not 'method' actually points to a Method
 * struct.
 */
static bool isValidMethod(const Method* method) {
    if (!dvmLinearAllocContains(method, sizeof(Method))) {
        LOGW("Method* is not in linear allocation table.");
        return false;
    }
    ClassObject* clazz = method->clazz;
    if (!dvmIsValidObject((Object*) clazz)) {
        LOGW("method->clazz doesn't point to an object at all.");
        return false;
    }
    if (clazz->obj.clazz != gDvm.classJavaLangClass) {
        LOGW("method->clazz doesn't point to a ClassObject.");
        return false;
    }

    // No need to validate the tables because we don't actually read them.
    if (!inTable(method, clazz->directMethods, clazz->directMethodCount)
            && !inTable(method, clazz->virtualMethods,
                    clazz->virtualMethodCount)) {
        LOGW("Method not found in associated ClassObject.");
        return false;
    }

    // We're pretty sure at this point that we're looking at a real Method*.
    // The only alternative is that 'method' points to the middle of a Method
    // struct and whatever ->clazz resolves to relative to that random
    // address happens to point to the right ClassObject*. We could mod
    // the address to ensure that the Method* is aligned as expected, but it's
    // probably not worth the overhead.
    return true;
}

/** Converts slashes to dots in the given class name. */
static void slashesToDots(char* s, int length) {
    int i;
    for (i = 0; i < length; i++) {
        if (s[i] == '/') {
            s[i] = '.';
        }
    }
}

/**
 * Compares class pointers from two method count wrappers. Used in the by-class
 * hash table.
 */
static int compareMethodCountClasses(const void* tableItem,
        const void* looseItem) {
    const MethodCountWrapper* a = (MethodCountWrapper*) tableItem;
    const MethodCountWrapper* b = (MethodCountWrapper*) looseItem;
    u4 serialA = a->clazz->serialNumber;
    u4 serialB = b->clazz->serialNumber;
    return serialA == serialB ? 0 : (serialA < serialB ? -1 : 1);
}

/**
 * Calculates amount of memory needed for the given class in the final
 * snapshot and adds the result to arg.
 */
static int calculateSnapshotEntrySize(void* data, void* arg) {
    MethodCountWrapper* wrapper = (MethodCountWrapper*) data;

    const char* className = wrapper->clazz->descriptor;
    wrapper->classNameLength = strlen(className);
    if (isNormalClassName(className, wrapper->classNameLength)) {
        // Trim first & last chars.
        wrapper->className = className + 1;
        wrapper->classNameLength -= 2;
    } else {
        wrapper->className = className;
    }

    // Size of this class entry.
    int size = 2; // class name size
    size += wrapper->classNameLength;
    size += 2; // number of methods in this class
    do {
        wrapper->methodNameLength
                = strlen(wrapper->methodCount->method->name);

        size += 2; // method name size
        size += wrapper->methodNameLength;
        // sample counts
        size += THREAD_TYPE_SIZE * THREAD_STATE_SIZE * METHOD_STATE_SIZE * 2;
        wrapper = wrapper->next;
    } while (wrapper != NULL);

    int* total = (int*) arg;
    *total += size;

    return 0;
}

/** Writes 2 bytes and increments dest pointer. */
#define writeShort(dest, value)     \
do {                                \
    u2 _value = (value);            \
    *dest++ = (char) (_value >> 8); \
    *dest++ = (char) _value;        \
} while (0);

/** Writes length in 2 bytes and then string, increments dest. */
#define writeString(dest, s, length)    \
do {                                    \
    u2 _length = (length);              \
    writeShort(dest, _length);          \
    memcpy(dest, s, _length);           \
    dest += _length;                    \
} while (0);

/**
 * Writes the entry data and advances the pointer (in arg).
 */
static int writeSnapshotEntry(void* data, void* arg) {
    MethodCountWrapper* wrapper = (MethodCountWrapper*) data;

    // We'll copy offset back into offsetPointer at the end.
    char** offsetPointer = (char**) arg;
    char* offset = *offsetPointer;

    // Class name.
    writeString(offset, wrapper->className, wrapper->classNameLength);
    slashesToDots(offset - wrapper->classNameLength, wrapper->classNameLength);

    // Method count.
    char* methodCountPointer = offset;
    u2 methodCount = 0;
    offset += 2;

    // Method entries.
    do {
        // Method name.
        writeString(offset, wrapper->methodCount->method->name,
                wrapper->methodNameLength);

        // Sample counts.
        u2 (*counts)[THREAD_STATE_SIZE][METHOD_STATE_SIZE]
                = wrapper->methodCount->counts;
        int type, threadState, methodState;
        for (type = 0; type < THREAD_TYPE_SIZE; type++)
            for (threadState = 0; threadState < THREAD_STATE_SIZE;
                    threadState++)
                for (methodState = 0; methodState < METHOD_STATE_SIZE;
                        methodState++)
                    writeShort(offset, counts[type][threadState][methodState]);

        methodCount++;
        wrapper = wrapper->next;
    } while (wrapper != NULL);

    // Go back and write method count.
    writeShort(methodCountPointer, methodCount);

    // Increment original pointer.
    *offsetPointer = offset;
    return 0;
}

/**
 * Captures the collected samples and clears the sample set.
 */
static void Dalvik_dalvik_system_SamplingProfiler_snapshot(const u4* args,
        JValue* pResult) {
    /*
     * Format:
     *   version # (2 bytes)
     *   # of class entries (2 bytes)
     *   ClassEntry...
     *
     * ClassEntry:
     *   class name length (2 bytes)
     *   UTF-8 class name
     *   # of method entries (2 bytes)
     *   MethodEntry...
     *
     *  MethodEntry:
     *    method name length (2 bytes)
     *    UTF-8 method name
     *    CountsByThreadState (for event thread)
     *    CountsByThreadState (for other threads)
     *
     *  CountsByThreadState:
     *    CountsByMethodState (for running threads)
     *    CountsByMethodState (for suspended threads)
     *
     *  CountsByMethodState:
     *    as calling method (2 bytes)
     *    as leaf method (2 bytes)
     */

    SampleSet* set = (SampleSet*) args[0];
    if (set->size == 0) {
        // No data has been captured.
        RETURN_PTR(NULL);
    }

    MethodCountWrapper* wrappers = (MethodCountWrapper*) calloc(set->size,
            sizeof(MethodCountWrapper));
    if (wrappers == NULL) {
        LOGW("Out of memory.");
        RETURN_PTR(NULL);
    }

    // Method count wrappers by class.
    HashTable* byClass = dvmHashTableCreate(set->size, NULL);
    if (byClass == NULL) {
        free(wrappers);
        LOGW("Out of memory.");
        RETURN_PTR(NULL);
    }

    // Validate method pointers and index by class.
    int setIndex;
    int wrapperIndex;
    for (setIndex = set->capacity - 1, wrapperIndex = 0;
            setIndex >= 0 && wrapperIndex < set->size;
            setIndex--) {
        MethodCount* mc = &set->entries[setIndex];
        const Method* method = mc->method;
        if (method != NULL && isValidMethod(method)) {
            MethodCountWrapper* wrapper = &wrappers[wrapperIndex];
            wrapper->methodCount = mc;
            wrapper->clazz = mc->method->clazz;
            u4 h = hash(wrapper->clazz);
            MethodCountWrapper* fromTable = dvmHashTableLookup(byClass, h,
                    wrapper, compareMethodCountClasses, true);
            if (fromTable != wrapper) {
                // We already have an entry for this class. Link the new entry.
                wrapper->next = fromTable->next;
                fromTable->next = wrapper;
            }
            wrapperIndex++;
        }
    }

    // Calculate size of snapshot in bytes.
    int totalSize = 4; // version, # of classes
    dvmHashForeach(byClass, calculateSnapshotEntrySize, &totalSize);

    // Write snapshot.
    ArrayObject* snapshot
            = dvmAllocPrimitiveArray('B', totalSize, ALLOC_DEFAULT);
    if (snapshot == NULL) {
        // Not enough memory to hold snapshot.
        // TODO: Still clear the set or leave it to try again later?
        LOGW("Out of memory.");
        free(wrappers);
        dvmHashTableFree(byClass);
        RETURN_PTR(NULL);
    }

    char* offset = (char*) snapshot->contents;
    writeShort(offset, 1); // version
    writeShort(offset, dvmHashTableNumEntries(byClass)); // class count
    dvmHashForeach(byClass, writeSnapshotEntry, &offset);

    // Verify that our size calculation was correct.
    int actualSize = offset - (char*) snapshot->contents;
    if (actualSize != totalSize) {
        LOGE("expected: %d, actual: %d", totalSize, actualSize);
        abort();
    }

    dvmHashTableFree(byClass);
    free(wrappers);

    clearSampleSet(set);

    dvmReleaseTrackedAlloc((Object*) snapshot, NULL);
    RETURN_PTR(snapshot);
}

/**
 * Allocates native memory.
 */
static void Dalvik_dalvik_system_SamplingProfiler_allocate(const u4* args,
        JValue* pResult) {
    SampleSet* set = (SampleSet*) malloc(sizeof(SampleSet));
    *set = newSampleSet(INITIAL_CAPACITY);
    RETURN_INT((jint) set);
}

/**
 * Frees native memory.
 */
static void Dalvik_dalvik_system_SamplingProfiler_free(const u4* args,
        JValue* pResult) {
    SampleSet* set = (SampleSet*) args[0];
    free(set->entries);
    free(set);
    RETURN_VOID();
}

/**
 * Identifies the event thread.
 */
static void Dalvik_dalvik_system_SamplingProfiler_setEventThread(const u4* args,
        JValue* pResult) {
    SampleSet* set = (SampleSet*) args[0];
    Object* eventThread = (Object*) args[1];  // java.lang.Thread
    Object* vmThread = dvmGetFieldObject(eventThread,
            gDvm.offJavaLangThread_vmThread); // java.lang.VMThread
    set->eventThread = dvmGetThreadFromThreadObject(vmThread);
    RETURN_VOID();
}

const DalvikNativeMethod dvm_dalvik_system_SamplingProfiler[] = {
    { "collisions", "(I)I", Dalvik_dalvik_system_SamplingProfiler_collisions },
    { "size", "(I)I", Dalvik_dalvik_system_SamplingProfiler_size },
    { "sample", "(I)I", Dalvik_dalvik_system_SamplingProfiler_sample },
    { "snapshot", "(I)[B", Dalvik_dalvik_system_SamplingProfiler_snapshot },
    { "free", "(I)V", Dalvik_dalvik_system_SamplingProfiler_free },
    { "allocate", "()I", Dalvik_dalvik_system_SamplingProfiler_allocate },
    { "setEventThread", "(ILjava/lang/Thread;)V",
            Dalvik_dalvik_system_SamplingProfiler_setEventThread },
    { NULL, NULL, NULL },
};
