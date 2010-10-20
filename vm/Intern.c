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

#include <stddef.h>

/*
 * Prep string interning.
 */
bool dvmStringInternStartup(void)
{
    dvmInitMutex(&gDvm.internLock);
    gDvm.internedStrings = dvmHashTableCreate(256, NULL);
    if (gDvm.internedStrings == NULL)
        return false;
    gDvm.literalStrings = dvmHashTableCreate(256, NULL);
    if (gDvm.literalStrings == NULL)
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
    if (gDvm.internedStrings != NULL || gDvm.literalStrings != NULL) {
        dvmDestroyMutex(&gDvm.internLock);
    }
    dvmHashTableFree(gDvm.internedStrings);
    gDvm.internedStrings = NULL;
    dvmHashTableFree(gDvm.literalStrings);
    gDvm.literalStrings = NULL;
}

static StringObject* lookupInternedString(StringObject* strObj, bool isLiteral)
{
    StringObject* found;
    u4 hash;

    assert(strObj != NULL);
    hash = dvmComputeStringHash(strObj);
    dvmLockMutex(&gDvm.internLock);
    if (isLiteral) {
        /*
         * Check the literal table for a match.
         */
        StringObject* literal = dvmHashTableLookup(gDvm.literalStrings,
                                                   hash, strObj,
                                                   dvmHashcmpStrings,
                                                   false);
        if (literal != NULL) {
            /*
             * A match was found in the literal table, the easy case.
             */
            found = literal;
        } else {
            /*
             * There is no match in the literal table, check the
             * interned string table.
             */
            StringObject* interned = dvmHashTableLookup(gDvm.internedStrings,
                                                        hash, strObj,
                                                        dvmHashcmpStrings,
                                                        false);
            if (interned != NULL) {
                /*
                 * A match was found in the interned table.  Move the
                 * matching string to the literal table.
                 */
                dvmHashTableRemove(gDvm.internedStrings, hash, interned);
                found = dvmHashTableLookup(gDvm.literalStrings,
                                           hash, interned,
                                           dvmHashcmpStrings,
                                           true);
                assert(found == interned);
            } else {
                /*
                 * No match in the literal table or the interned
                 * table.  Insert into the literal table.
                 */
                found = dvmHashTableLookup(gDvm.literalStrings,
                                           hash, strObj,
                                           dvmHashcmpStrings,
                                           true);
                assert(found == strObj);
            }
        }
    } else {
        /*
         * Check the literal table for a match.
         */
        found = dvmHashTableLookup(gDvm.literalStrings,
                                   hash, strObj,
                                   dvmHashcmpStrings,
                                   false);
        if (found == NULL) {
            /*
             * No match was found in the literal table.  Insert into
             * the intern table.
             */
            found = dvmHashTableLookup(gDvm.internedStrings,
                                       hash, strObj,
                                       dvmHashcmpStrings,
                                       true);
        }
    }
    assert(found != NULL);
    dvmUnlockMutex(&gDvm.internLock);
    return found;
}

/*
 * Find an entry in the interned string table.
 *
 * If the string doesn't already exist, the StringObject is added to
 * the table.  Otherwise, the existing entry is returned.
 */
StringObject* dvmLookupInternedString(StringObject* strObj)
{
    return lookupInternedString(strObj, false);
}

/*
 * Same as dvmLookupInternedString(), but guarantees that the
 * returned string is a literal.
 */
StringObject* dvmLookupImmortalInternedString(StringObject* strObj)
{
    return lookupInternedString(strObj, true);
}

/*
 * Returns true if the object is a weak interned string.  Any string
 * interned by the user is weak.
 */
bool dvmIsWeakInternedString(const StringObject* strObj)
{
    StringObject* found;
    u4 hash;

    assert(strObj != NULL);
    if (gDvm.internedStrings == NULL) {
        return false;
    }
    dvmLockMutex(&gDvm.internLock);
    hash = dvmComputeStringHash(strObj);
    found = dvmHashTableLookup(gDvm.internedStrings, hash, (void*)strObj,
                               dvmHashcmpStrings, false);
    dvmUnlockMutex(&gDvm.internLock);
    return found == strObj;
}

static int markStringObject(void* strObj, void* arg)
{
    UNUSED_PARAMETER(arg);
    dvmMarkObjectNonNull(strObj);
    return 0;
}

/*
 * Blacken string references from the literal string table.  The
 * literal table is a root.
 */
void dvmGcScanInternedStrings()
{
    /* It's possible for a GC to happen before dvmStringInternStartup()
     * is called.
     */
    if (gDvm.literalStrings != NULL) {
        dvmHashForeach(gDvm.literalStrings, markStringObject, NULL);
    }
}

/*
 * Clear white references from the intern table.
 */
void dvmGcDetachDeadInternedStrings(int (*isUnmarkedObject)(void *))
{
    /* It's possible for a GC to happen before dvmStringInternStartup()
     * is called.
     */
    if (gDvm.internedStrings != NULL) {
        dvmLockMutex(&gDvm.internLock);
        dvmHashForeachRemove(gDvm.internedStrings, isUnmarkedObject);
        dvmUnlockMutex(&gDvm.internLock);
    }
}
