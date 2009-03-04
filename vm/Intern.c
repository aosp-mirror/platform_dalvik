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
 * String interning.
 */
#include "Dalvik.h"

#include <stdlib.h>

#define INTERN_STRING_IMMORTAL_BIT (1<<0)
#define SET_IMMORTAL_BIT(strObj) \
            ((uintptr_t)(strObj) | INTERN_STRING_IMMORTAL_BIT)
#define STRIP_IMMORTAL_BIT(strObj) \
            ((uintptr_t)(strObj) & ~INTERN_STRING_IMMORTAL_BIT)
#define IS_IMMORTAL(strObj) \
            ((uintptr_t)(strObj) & INTERN_STRING_IMMORTAL_BIT)


/*
 * Prep string interning.
 */
bool dvmStringInternStartup(void)
{
    gDvm.internedStrings = dvmHashTableCreate(256, NULL);
    if (gDvm.internedStrings == NULL)
        return false;

    return true;
}

/*
 * Chuck the intern list.
 *
 * The contents of the list are StringObjects that live on the GC heap.
 */
void dvmStringInternShutdown(void)
{
    dvmHashTableFree(gDvm.internedStrings);
    gDvm.internedStrings = NULL;
}


/*
 * Compare two string objects that may have INTERN_STRING_IMMORTAL_BIT
 * set in their pointer values.
 */
static int hashcmpImmortalStrings(const void* vstrObj1, const void* vstrObj2)
{
    return dvmHashcmpStrings((const void*) STRIP_IMMORTAL_BIT(vstrObj1),
                             (const void*) STRIP_IMMORTAL_BIT(vstrObj2));
}

static StringObject* lookupInternedString(StringObject* strObj, bool immortal)
{
    StringObject* found;
    u4 hash;

    assert(strObj != NULL);
    hash = dvmComputeStringHash(strObj);

    if (false) {
        char* debugStr = dvmCreateCstrFromString(strObj);
        LOGV("+++ dvmLookupInternedString searching for '%s'\n", debugStr);
        free(debugStr);
    }

    if (immortal) {
        strObj = (StringObject*) SET_IMMORTAL_BIT(strObj);
    }

    dvmHashTableLock(gDvm.internedStrings);

    found = (StringObject*) dvmHashTableLookup(gDvm.internedStrings,
                                hash, strObj, hashcmpImmortalStrings, true);
    if (immortal && !IS_IMMORTAL(found)) {
        /* Make this entry immortal.  We have to use the existing object
         * because, as an interned string, it's not allowed to change.
         *
         * There's no way to get a pointer to the actual hash table entry,
         * so the only way to modify the existing entry is to remove,
         * modify, and re-add it.
         */
        dvmHashTableRemove(gDvm.internedStrings, hash, found);
        found = (StringObject*) SET_IMMORTAL_BIT(found);
        found = (StringObject*) dvmHashTableLookup(gDvm.internedStrings,
                                    hash, found, hashcmpImmortalStrings, true);
        assert(IS_IMMORTAL(found));
    }

    dvmHashTableUnlock(gDvm.internedStrings);

    //if (found == strObj)
    //    LOGVV("+++  added string\n");
    return (StringObject*) STRIP_IMMORTAL_BIT(found);
}

/*
 * Find an entry in the interned string list.
 *
 * If the string doesn't already exist, the StringObject is added to
 * the list.  Otherwise, the existing entry is returned.
 */
StringObject* dvmLookupInternedString(StringObject* strObj)
{
    return lookupInternedString(strObj, false);
}

/*
 * Same as dvmLookupInternedString(), but guarantees that the
 * returned string is immortal.
 */
StringObject* dvmLookupImmortalInternedString(StringObject* strObj)
{
    return lookupInternedString(strObj, true);
}

/*
 * Mark all immortal interned string objects so that they don't
 * get collected by the GC.  Non-immortal strings may or may not
 * get marked by other references.
 */
static int markStringObject(void* strObj, void* arg)
{
    UNUSED_PARAMETER(arg);

    if (IS_IMMORTAL(strObj)) {
        dvmMarkObjectNonNull((Object*) STRIP_IMMORTAL_BIT(strObj));
    }
    return 0;
}

void dvmGcScanInternedStrings()
{
    /* It's possible for a GC to happen before dvmStringInternStartup()
     * is called.
     */
    if (gDvm.internedStrings != NULL) {
        dvmHashTableLock(gDvm.internedStrings);
        dvmHashForeach(gDvm.internedStrings, markStringObject, NULL);
        dvmHashTableUnlock(gDvm.internedStrings);
    }
}

/*
 * Called by the GC after all reachable objects have been
 * marked.  isUnmarkedObject is a function suitable for passing
 * to dvmHashForeachRemove();  it must strip the low bits from
 * its pointer argument to deal with the immortal bit, though.
 */
void dvmGcDetachDeadInternedStrings(int (*isUnmarkedObject)(void *))
{
    /* It's possible for a GC to happen before dvmStringInternStartup()
     * is called.
     */
    if (gDvm.internedStrings != NULL) {
        dvmHashTableLock(gDvm.internedStrings);
        dvmHashForeachRemove(gDvm.internedStrings, isUnmarkedObject);
        dvmHashTableUnlock(gDvm.internedStrings);
    }
}
