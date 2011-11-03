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

/*
 * Test the indirect reference table implementation.
 */
#include "Dalvik.h"

#include <stdlib.h>

#ifndef NDEBUG

#define DBUG_MSG    LOGI

/*
 * Basic add/get/delete tests in an unsegmented table.
 */
static bool basicTest()
{
    static const int kTableMax = 20;
    IndirectRefTable irt;
    IndirectRef iref0, iref1, iref2, iref3;
    IndirectRef manyRefs[kTableMax];
    ClassObject* clazz = dvmFindClass("Ljava/lang/Object;", NULL);
    Object* obj0 = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
    Object* obj1 = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
    Object* obj2 = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
    Object* obj3 = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
    const u4 cookie = IRT_FIRST_SEGMENT;
    bool result = false;

    if (!irt.init(kTableMax/2, kTableMax, kIndirectKindGlobal)) {
        return false;
    }

    iref0 = (IndirectRef) 0x11110;
    if (irt.remove(cookie, iref0)) {
        LOGE("unexpectedly successful removal");
        goto bail;
    }

    /*
     * Add three, check, remove in the order in which they were added.
     */
    DBUG_MSG("+++ START fifo\n");
    iref0 = irt.add(cookie, obj0);
    iref1 = irt.add(cookie, obj1);
    iref2 = irt.add(cookie, obj2);
    if (iref0 == NULL || iref1 == NULL || iref2 == NULL) {
        LOGE("trivial add1 failed");
        goto bail;
    }

    if (irt.get(iref0) != obj0 ||
            irt.get(iref1) != obj1 ||
            irt.get(iref2) != obj2) {
        LOGE("objects don't match expected values %p %p %p vs. %p %p %p",
                irt.get(iref0), irt.get(iref1), irt.get(iref2),
                obj0, obj1, obj2);
        goto bail;
    } else {
        DBUG_MSG("+++ obj1=%p --> iref1=%p\n", obj1, iref1);
    }

    if (!irt.remove(cookie, iref0) ||
            !irt.remove(cookie, iref1) ||
            !irt.remove(cookie, iref2))
    {
        LOGE("fifo deletion failed");
        goto bail;
    }

    /* table should be empty now */
    if (irt.capacity() != 0) {
        LOGE("fifo del not empty");
        goto bail;
    }

    /* get invalid entry (off the end of the list) */
    if (irt.get(iref0) != kInvalidIndirectRefObject) {
        LOGE("stale entry get succeeded unexpectedly");
        goto bail;
    }

    /*
     * Add three, remove in the opposite order.
     */
    DBUG_MSG("+++ START lifo\n");
    iref0 = irt.add(cookie, obj0);
    iref1 = irt.add(cookie, obj1);
    iref2 = irt.add(cookie, obj2);
    if (iref0 == NULL || iref1 == NULL || iref2 == NULL) {
        LOGE("trivial add2 failed");
        goto bail;
    }

    if (!irt.remove(cookie, iref2) ||
            !irt.remove(cookie, iref1) ||
            !irt.remove(cookie, iref0))
    {
        LOGE("lifo deletion failed");
        goto bail;
    }

    /* table should be empty now */
    if (irt.capacity() != 0) {
        LOGE("lifo del not empty");
        goto bail;
    }

    /*
     * Add three, remove middle / middle / bottom / top.  (Second attempt
     * to remove middle should fail.)
     */
    DBUG_MSG("+++ START unorder\n");
    iref0 = irt.add(cookie, obj0);
    iref1 = irt.add(cookie, obj1);
    iref2 = irt.add(cookie, obj2);
    if (iref0 == NULL || iref1 == NULL || iref2 == NULL) {
        LOGE("trivial add3 failed");
        goto bail;
    }

    if (irt.capacity() != 3) {
        LOGE("expected 3 entries, found %d", irt.capacity());
        goto bail;
    }

    if (!irt.remove(cookie, iref1) || irt.remove(cookie, iref1)) {
        LOGE("unorder deletion1 failed");
        goto bail;
    }

    /* get invalid entry (from hole) */
    if (irt.get(iref1) != kInvalidIndirectRefObject) {
        LOGE("hole get succeeded unexpectedly");
        goto bail;
    }

    if (!irt.remove(cookie, iref2) || !irt.remove(cookie, iref0)) {
        LOGE("unorder deletion2 failed");
        goto bail;
    }

    /* table should be empty now */
    if (irt.capacity() != 0) {
        LOGE("unorder del not empty");
        goto bail;
    }

    /*
     * Add four entries.  Remove #1, add new entry, verify that table size
     * is still 4 (i.e. holes are getting filled).  Remove #1 and #3, verify
     * that we delete one and don't hole-compact the other.
     */
    DBUG_MSG("+++ START hole fill\n");
    iref0 = irt.add(cookie, obj0);
    iref1 = irt.add(cookie, obj1);
    iref2 = irt.add(cookie, obj2);
    iref3 = irt.add(cookie, obj3);
    if (iref0 == NULL || iref1 == NULL || iref2 == NULL || iref3 == NULL) {
        LOGE("trivial add4 failed");
        goto bail;
    }
    if (!irt.remove(cookie, iref1)) {
        LOGE("remove 1 of 4 failed");
        goto bail;
    }
    iref1 = irt.add(cookie, obj1);
    if (irt.capacity() != 4) {
        LOGE("hole not filled");
        goto bail;
    }
    if (!irt.remove(cookie, iref1) || !irt.remove(cookie, iref3)) {
        LOGE("remove 1/3 failed");
        goto bail;
    }
    if (irt.capacity() != 3) {
        LOGE("should be 3 after two deletions");
        goto bail;
    }
    if (!irt.remove(cookie, iref2) || !irt.remove(cookie, iref0)) {
        LOGE("remove 2/0 failed");
        goto bail;
    }
    if (irt.capacity() != 0) {
        LOGE("not empty after split remove");
        goto bail;
    }

    /*
     * Add an entry, remove it, add a new entry, and try to use the original
     * iref.  They have the same slot number but are for different objects.
     * With the extended checks in place, this should fail.
     */
    DBUG_MSG("+++ START switched\n");
    iref0 = irt.add(cookie, obj0);
    irt.remove(cookie, iref0);
    iref1 = irt.add(cookie, obj1);
    if (irt.remove(cookie, iref0)) {
        LOGE("mismatched del succeeded (%p vs %p)", iref0, iref1);
        goto bail;
    }
    if (!irt.remove(cookie, iref1)) {
        LOGE("switched del failed");
        goto bail;
    }
    if (irt.capacity() != 0) {
        LOGE("switching del not empty");
        goto bail;
    }

    /*
     * Same as above, but with the same object.  A more rigorous checker
     * (e.g. with slot serialization) will catch this.
     */
    DBUG_MSG("+++ START switched same object\n");
    iref0 = irt.add(cookie, obj0);
    irt.remove(cookie, iref0);
    iref1 = irt.add(cookie, obj0);
    if (iref0 != iref1) {
        /* try 0, should not work */
        if (irt.remove(cookie, iref0)) {
            LOGE("temporal del succeeded (%p vs %p)", iref0, iref1);
            goto bail;
        }
    }
    if (!irt.remove(cookie, iref1)) {
        LOGE("temporal cleanup failed");
        goto bail;
    }
    if (irt.capacity() != 0) {
        LOGE("temporal del not empty");
        goto bail;
    }

    DBUG_MSG("+++ START null lookup\n");
    if (irt.get(NULL) != kInvalidIndirectRefObject) {
        LOGE("null lookup succeeded");
        goto bail;
    }

    DBUG_MSG("+++ START stale lookup\n");
    iref0 = irt.add(cookie, obj0);
    irt.remove(cookie, iref0);
    if (irt.get(iref0) != kInvalidIndirectRefObject) {
        LOGE("stale lookup succeeded");
        goto bail;
    }

    /*
     * Test table overflow.
     */
    DBUG_MSG("+++ START overflow\n");
    int i;
    for (i = 0; i < kTableMax; i++) {
        manyRefs[i] = irt.add(cookie, obj0);
        if (manyRefs[i] == NULL) {
            LOGE("Failed adding %d of %d", i, kTableMax);
            goto bail;
        }
    }
    if (irt.add(cookie, obj0) != NULL) {
        LOGE("Table overflow succeeded");
        goto bail;
    }
    if (irt.capacity() != (size_t)kTableMax) {
        LOGE("Expected %d entries, found %d", kTableMax, irt.capacity());
        goto bail;
    }
    irt.dump("table with 20 entries, all filled");
    for (i = 0; i < kTableMax-1; i++) {
        if (!irt.remove(cookie, manyRefs[i])) {
            LOGE("multi-remove failed at %d", i);
            goto bail;
        }
    }
    irt.dump("table with 20 entries, 19 of them holes");
    /* because of removal order, should have 20 entries, 19 of them holes */
    if (irt.capacity() != (size_t)kTableMax) {
        LOGE("Expected %d entries (with holes), found %d",
                kTableMax, irt.capacity());
        goto bail;
    }
    if (!irt.remove(cookie, manyRefs[kTableMax-1])) {
        LOGE("multi-remove final failed");
        goto bail;
    }
    if (irt.capacity() != 0) {
        LOGE("multi-del not empty");
        goto bail;
    }

    /* Done */
    DBUG_MSG("+++ basic test complete\n");
    result = true;

bail:
    irt.destroy();
    return result;
}

/*
 * Some quick tests.
 */
bool dvmTestIndirectRefTable()
{
    if (!basicTest()) {
        LOGE("IRT basic test failed");
        return false;
    }

    return true;
}

#endif /*NDEBUG*/
