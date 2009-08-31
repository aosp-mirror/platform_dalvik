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

#define DBUG_MSG    LOGV

/*
 * Basic add/get/delete tests in an unsegmented table.
 */
static bool basicTest(void)
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

    if (!dvmInitIndirectRefTable(&irt, kTableMax/2, kTableMax,
            kIndirectKindGlobal))
    {
        return false;
    }

    iref0 = (IndirectRef) 0x11110;
    if (dvmRemoveFromIndirectRefTable(&irt, cookie, iref0)) {
        LOGE("unexpectedly successful removal\n");
        goto bail;
    }

    /*
     * Add three, check, remove in the order in which they were added.
     */
    DBUG_MSG("+++ START fifo\n");
    iref0 = dvmAddToIndirectRefTable(&irt, cookie, obj0);
    iref1 = dvmAddToIndirectRefTable(&irt, cookie, obj1);
    iref2 = dvmAddToIndirectRefTable(&irt, cookie, obj2);
    if (iref0 == NULL || iref1 == NULL || iref2 == NULL) {
        LOGE("trivial add1 failed\n");
        goto bail;
    }

    if (dvmGetFromIndirectRefTable(&irt, iref0) != obj0 ||
        dvmGetFromIndirectRefTable(&irt, iref1) != obj1 ||
        dvmGetFromIndirectRefTable(&irt, iref2) != obj2)
    {
        LOGE("objects don't match expected values %p %p %p vs. %p %p %p\n",
            dvmGetFromIndirectRefTable(&irt, iref0),
            dvmGetFromIndirectRefTable(&irt, iref1),
            dvmGetFromIndirectRefTable(&irt, iref2),
            obj0, obj1, obj2);
        goto bail;
    } else {
        DBUG_MSG("+++ obj1=%p --> iref1=%p\n", obj1, iref1);
    }

    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref0) ||
        !dvmRemoveFromIndirectRefTable(&irt, cookie, iref1) ||
        !dvmRemoveFromIndirectRefTable(&irt, cookie, iref2))
    {
        LOGE("fifo deletion failed\n");
        goto bail;
    }

    /* table should be empty now */
    if (dvmIndirectRefTableEntries(&irt) != 0) {
        LOGE("fifo del not empty\n");
        goto bail;
    }

    /* get invalid entry (off the end of the list) */
    if (dvmGetFromIndirectRefTable(&irt, iref0) != NULL) {
        LOGE("stale entry get succeeded unexpectedly\n");
        goto bail;
    }

    /*
     * Add three, remove in the opposite order.
     */
    DBUG_MSG("+++ START lifo\n");
    iref0 = dvmAddToIndirectRefTable(&irt, cookie, obj0);
    iref1 = dvmAddToIndirectRefTable(&irt, cookie, obj1);
    iref2 = dvmAddToIndirectRefTable(&irt, cookie, obj2);
    if (iref0 == NULL || iref1 == NULL || iref2 == NULL) {
        LOGE("trivial add2 failed\n");
        goto bail;
    }

    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref2) ||
        !dvmRemoveFromIndirectRefTable(&irt, cookie, iref1) ||
        !dvmRemoveFromIndirectRefTable(&irt, cookie, iref0))
    {
        LOGE("lifo deletion failed\n");
        goto bail;
    }

    /* table should be empty now */
    if (dvmIndirectRefTableEntries(&irt) != 0) {
        LOGE("lifo del not empty\n");
        goto bail;
    }

    /*
     * Add three, remove middle / middle / bottom / top.  (Second attempt
     * to remove middle should fail.)
     */
    DBUG_MSG("+++ START unorder\n");
    iref0 = dvmAddToIndirectRefTable(&irt, cookie, obj0);
    iref1 = dvmAddToIndirectRefTable(&irt, cookie, obj1);
    iref2 = dvmAddToIndirectRefTable(&irt, cookie, obj2);
    if (iref0 == NULL || iref1 == NULL || iref2 == NULL) {
        LOGE("trivial add3 failed\n");
        goto bail;
    }

    if (dvmIndirectRefTableEntries(&irt) != 3) {
        LOGE("expected 3 entries, found %d\n",
            dvmIndirectRefTableEntries(&irt));
        goto bail;
    }

    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref1) ||
        dvmRemoveFromIndirectRefTable(&irt, cookie, iref1))
    {
        LOGE("unorder deletion1 failed\n");
        goto bail;
    }

    /* get invalid entry (from hole) */
    if (dvmGetFromIndirectRefTable(&irt, iref1) != NULL) {
        LOGE("hole get succeeded unexpectedly\n");
        goto bail;
    }

    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref2) ||
        !dvmRemoveFromIndirectRefTable(&irt, cookie, iref0))
    {
        LOGE("unorder deletion2 failed\n");
        goto bail;
    }

    /* table should be empty now */
    if (dvmIndirectRefTableEntries(&irt) != 0) {
        LOGE("unorder del not empty\n");
        goto bail;
    }

    /*
     * Add four entries.  Remove #1, add new entry, verify that table size
     * is still 4 (i.e. holes are getting filled).  Remove #1 and #3, verify
     * that we delete one and don't hole-compact the other.
     */
    DBUG_MSG("+++ START hole fill\n");
    iref0 = dvmAddToIndirectRefTable(&irt, cookie, obj0);
    iref1 = dvmAddToIndirectRefTable(&irt, cookie, obj1);
    iref2 = dvmAddToIndirectRefTable(&irt, cookie, obj2);
    iref3 = dvmAddToIndirectRefTable(&irt, cookie, obj3);
    if (iref0 == NULL || iref1 == NULL || iref2 == NULL || iref3 == NULL) {
        LOGE("trivial add4 failed\n");
        goto bail;
    }
    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref1)) {
        LOGE("remove 1 of 4 failed\n");
        goto bail;
    }
    iref1 = dvmAddToIndirectRefTable(&irt, cookie, obj1);
    if (dvmIndirectRefTableEntries(&irt) != 4) {
        LOGE("hole not filled\n");
        goto bail;
    }
    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref1) ||
        !dvmRemoveFromIndirectRefTable(&irt, cookie, iref3))
    {
        LOGE("remove 1/3 failed\n");
        goto bail;
    }
    if (dvmIndirectRefTableEntries(&irt) != 3) {
        LOGE("should be 3 after two deletions\n");
        goto bail;
    }
    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref2) ||
        !dvmRemoveFromIndirectRefTable(&irt, cookie, iref0))
    {
        LOGE("remove 2/0 failed\n");
        goto bail;
    }
    if (dvmIndirectRefTableEntries(&irt) != 0) {
        LOGE("not empty after split remove\n");
        goto bail;
    }

    /*
     * Add an entry, remove it, add a new entry, and try to use the original
     * iref.  They have the same slot number but are for different objects.
     * With the extended checks in place, this should fail.
     */
    DBUG_MSG("+++ START switched\n");
    iref0 = dvmAddToIndirectRefTable(&irt, cookie, obj0);
    dvmRemoveFromIndirectRefTable(&irt, cookie, iref0);
    iref1 = dvmAddToIndirectRefTable(&irt, cookie, obj1);
    if (dvmRemoveFromIndirectRefTable(&irt, cookie, iref0)) {
        LOGE("mismatched del succeeded (%p vs %p)\n", iref0, iref1);
        goto bail;
    }
    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref1)) {
        LOGE("switched del failed\n");
        goto bail;
    }
    if (dvmIndirectRefTableEntries(&irt) != 0) {
        LOGE("switching del not empty\n");
        goto bail;
    }

    /*
     * Same as above, but with the same object.  A more rigorous checker
     * (e.g. with slot serialization) will catch this.
     */
    iref0 = dvmAddToIndirectRefTable(&irt, cookie, obj0);
    dvmRemoveFromIndirectRefTable(&irt, cookie, iref0);
    iref1 = dvmAddToIndirectRefTable(&irt, cookie, obj0);
    if (iref0 != iref1) {
        /* try 0, should not work */
        if (dvmRemoveFromIndirectRefTable(&irt, cookie, iref0)) {
            LOGE("temporal del succeeded (%p vs %p)\n", iref0, iref1);
            goto bail;
        }
    }
    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref1)) {
        LOGE("temporal cleanup failed\n");
        goto bail;
    }
    if (dvmIndirectRefTableEntries(&irt) != 0) {
        LOGE("temporal del not empty\n");
        goto bail;
    }

    /*
     * Test table overflow.
     */
    DBUG_MSG("+++ START overflow\n");
    int i;
    for (i = 0; i < kTableMax; i++) {
        manyRefs[i] = dvmAddToIndirectRefTable(&irt, cookie, obj0);
        if (manyRefs[i] == NULL) {
            LOGE("Failed adding %d of %d\n", i, kTableMax);
            goto bail;
        }
    }
    if (dvmAddToIndirectRefTable(&irt, cookie, obj0) != NULL) {
        LOGE("Table overflow succeeded\n");
        goto bail;
    }
    if (dvmIndirectRefTableEntries(&irt) != (size_t)kTableMax) {
        LOGE("Expected %d entries, found %d\n",
            kTableMax, dvmIndirectRefTableEntries(&irt));
        goto bail;
    }
    for (i = 0; i < kTableMax-1; i++) {
        if (!dvmRemoveFromIndirectRefTable(&irt, cookie, manyRefs[i])) {
            LOGE("multi-remove failed at %d\n", i);
            goto bail;
        }
    }
    /* because of removal order, should have 20 entries, 19 of them holes */
    if (dvmIndirectRefTableEntries(&irt) != (size_t)kTableMax) {
        LOGE("Expected %d entries (with holes), found %d\n",
            kTableMax, dvmIndirectRefTableEntries(&irt));
        goto bail;
    }
    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, manyRefs[kTableMax-1])) {
        LOGE("multi-remove final failed\n");
        goto bail;
    }
    if (dvmIndirectRefTableEntries(&irt) != 0) {
        LOGE("multi-del not empty\n");
        goto bail;
    }

    DBUG_MSG("+++ basic test complete\n");
    result = true;

bail:
    dvmClearIndirectRefTable(&irt);
    return result;
}

/*
 * Test operations on a segmented table.
 */
static bool segmentTest(void)
{
    static const int kTableMax = 20;
    IndirectRefTable irt;
    IndirectRef iref0, iref1, iref2, iref3;
    ClassObject* clazz = dvmFindClass("Ljava/lang/Object;", NULL);
    Object* obj0 = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
    Object* obj1 = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
    Object* obj2 = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
    Object* obj3 = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
    u4 cookie;
    u4 segmentState[4];
    bool result = false;

    if (!dvmInitIndirectRefTable(&irt, kTableMax, kTableMax,
            kIndirectKindLocal))
    {
        return false;
    }
    cookie = segmentState[0] = IRT_FIRST_SEGMENT;
    DBUG_MSG("+++ objs %p %p %p %p\n", obj0, obj1, obj2, obj3);

    /*
     * Push two, create new segment, push two more, try to get all four,
     * try to delete all 4.  All four should be accessible, but only the
     * last two should be deletable.
     */
    DBUG_MSG("+++ START basic segment\n");
    iref0 = dvmAddToIndirectRefTable(&irt, cookie, obj0);
    iref1 = dvmAddToIndirectRefTable(&irt, cookie, obj1);
    cookie = segmentState[1] = dvmPushIndirectRefTableSegment(&irt);
    DBUG_MSG("+++ pushed, cookie is 0x%08x\n", cookie);
    iref2 = dvmAddToIndirectRefTable(&irt, cookie, obj2);
    iref3 = dvmAddToIndirectRefTable(&irt, cookie, obj3);

    if (dvmRemoveFromIndirectRefTable(&irt, cookie, iref0) ||
        dvmRemoveFromIndirectRefTable(&irt, cookie, iref1))
    {
        LOGE("removed values from earlier segment\n");
        goto bail;
    }
    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref2) ||
        !dvmRemoveFromIndirectRefTable(&irt, cookie, iref3))
    {
        LOGE("unable to remove values from current segment\n");
        goto bail;
    }
    if (dvmIndirectRefTableEntries(&irt) != 2) {
        LOGE("wrong total entries\n");
        goto bail;
    }
    dvmPopIndirectRefTableSegment(&irt, segmentState[1]);
    cookie = segmentState[0];
    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref0) ||
        !dvmRemoveFromIndirectRefTable(&irt, cookie, iref1))
    {
        LOGE("unable to remove values from first segment\n");
        goto bail;
    }
    if (dvmIndirectRefTableEntries(&irt) != 0) {
        LOGE("basic push/pop not empty\n");
        goto bail;
    }

    /*
     * Push two, delete first, segment, push two more, pop segment, verify
     * the last two are no longer present and hole count is right.  The
     * adds after the segment pop should not be filling in the hole.
     */
    DBUG_MSG("+++ START segment pop\n");
    iref0 = dvmAddToIndirectRefTable(&irt, cookie, obj0);
    iref1 = dvmAddToIndirectRefTable(&irt, cookie, obj1);
    dvmRemoveFromIndirectRefTable(&irt, cookie, iref0);
    cookie = segmentState[1] = dvmPushIndirectRefTableSegment(&irt);
    iref2 = dvmAddToIndirectRefTable(&irt, cookie, obj2);
    iref3 = dvmAddToIndirectRefTable(&irt, cookie, obj3);
    dvmPopIndirectRefTableSegment(&irt, segmentState[1]);
    cookie = segmentState[0];
    if (dvmIndirectRefTableEntries(&irt) != 2) {
        LOGE("wrong total entries after pop\n");
        goto bail;
    }
    dvmRemoveFromIndirectRefTable(&irt, cookie, iref1);
    if (dvmIndirectRefTableEntries(&irt) != 0) {
        LOGE("not back to zero after pop + del\n");
        goto bail;
    }

    /*
     * Multiple segments, some empty.
     */
    DBUG_MSG("+++ START multiseg\n");
    iref0 = dvmAppendToIndirectRefTable(&irt, cookie, obj0);
    iref1 = dvmAppendToIndirectRefTable(&irt, cookie, obj1);
    cookie = segmentState[1] = dvmPushIndirectRefTableSegment(&irt);
    cookie = segmentState[2] = dvmPushIndirectRefTableSegment(&irt);
    iref3 = dvmAppendToIndirectRefTable(&irt, cookie, obj3);
    iref2 = dvmAppendToIndirectRefTable(&irt, cookie, obj2);
    dvmRemoveFromIndirectRefTable(&irt, cookie, iref3);
    cookie = segmentState[3] = dvmPushIndirectRefTableSegment(&irt);
    iref3 = dvmAppendToIndirectRefTable(&irt, cookie, obj3);

    if (dvmGetFromIndirectRefTable(&irt, iref0) != obj0 ||
        dvmGetFromIndirectRefTable(&irt, iref1) != obj1 ||
        dvmGetFromIndirectRefTable(&irt, iref2) != obj2 ||
        dvmGetFromIndirectRefTable(&irt, iref3) != obj3)
    {
        LOGE("Unable to retrieve all multiseg objects\n");
        goto bail;
    }

    dvmDumpIndirectRefTable(&irt, "test");

    //int i;
    //for (i = 0; i < sizeof(segmentState) / sizeof(segmentState[0]); i++) {
    //    DBUG_MSG("+++  segment %d = 0x%08x\n", i, segmentState[i]);
    //}

    dvmRemoveFromIndirectRefTable(&irt, cookie, iref3);
    if (dvmRemoveFromIndirectRefTable(&irt, cookie, iref2)) {
        LOGE("multiseg del2 worked\n");
        goto bail;
    }
    dvmPopIndirectRefTableSegment(&irt, segmentState[3]);
    cookie = segmentState[2];
    if (!dvmRemoveFromIndirectRefTable(&irt, cookie, iref2)) {
        LOGE("multiseg del2b failed (cookie=0x%08x ref=%p)\n", cookie, iref2);
        goto bail;
    }
    iref2 = dvmAddToIndirectRefTable(&irt, cookie, obj2);

    /* pop two off at once */
    dvmPopIndirectRefTableSegment(&irt, segmentState[1]);
    cookie = segmentState[0];

    if (dvmIndirectRefTableEntries(&irt) != 2) {
        LOGE("Unexpected entry count in multiseg\n");
        goto bail;
    }
    dvmRemoveFromIndirectRefTable(&irt, cookie, iref0);
    dvmRemoveFromIndirectRefTable(&irt, cookie, iref1);
    if (dvmIndirectRefTableEntries(&irt) != 0) {
        LOGE("Unexpected entry count at multiseg end\n");
        goto bail;
    }

    DBUG_MSG("+++ segment test complete\n");
    result = true;

bail:
    dvmClearIndirectRefTable(&irt);
    return result;
}


/*
 * Some quick tests.
 */
bool dvmTestIndirectRefTable(void)
{
    if (!basicTest()) {
        LOGE("IRT basic test failed\n");
        return false;
    }
    if (!segmentTest()) {
        LOGE("IRT segment test failed\n");
        return false;
    }

    return true;
}

#endif /*NDEBUG*/
