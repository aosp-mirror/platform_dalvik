/*
 * Copyright (C) 2010 The Android Open Source Project
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

#include "Dalvik.h"
#include "alloc/HeapBitmap.h"
#include "alloc/Verify.h"
#include "alloc/Visit.h"

/*
 * Checks that the given reference points to a valid object.
 */
static void verifyReference(void *addr, void *arg)
{
    const Object *obj;
    bool isValid;

    assert(addr != NULL);
    obj = *(const Object **)addr;
    if (obj == NULL) {
        isValid = true;
    } else {
        isValid = dvmIsValidObject(obj);
    }
    if (!isValid) {
        LOGE("Verify of object %p @ %p failed", obj, addr);
        dvmAbort();
    }
}

static void visitorCallback(void *addr, void *arg)
{
    verifyReference(addr, arg);
}

/*
 * Verifies an object reference.
 */
void dvmVerifyObject(const Object *obj)
{
    dvmVisitObject(visitorCallback, (Object *)obj, NULL);
}

/*
 * Helper function to call dvmVerifyObject from a bitmap walker.
 */
static void verifyBitmapCallback(size_t numPtrs, void **ptrs,
                                 const void *finger, void *arg)
{
    size_t i;

    for (i = 0; i < numPtrs; i++) {
        dvmVerifyObject(ptrs[i]);
    }
}

/*
 * Verifies the object references in a heap bitmap. Assumes the VM is
 * suspended.
 */
void dvmVerifyBitmap(const HeapBitmap *bitmap)
{
    /* TODO: check that locks are held and the VM is suspended. */
    dvmHeapBitmapWalk(bitmap, verifyBitmapCallback, NULL);
}

/*
 * Verifies references in the roots.
 */
void dvmVerifyRoots(void)
{
    dvmVisitRoots(verifyReference, NULL);
}
