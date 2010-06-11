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
 * java.security.AccessController
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"


/*
 * private static ProtectionDomain[] getStackDomains()
 *
 * Return an array of ProtectionDomain objects from the classes of the
 * methods on the stack.  Ignore reflection frames.  Stop at the first
 * privileged frame we see.
 */
static void Dalvik_java_security_AccessController_getStackDomains(
    const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);

    const Method** methods = NULL;
    int length;

    /*
     * Get an array with the stack trace in it.
     */
    if (!dvmCreateStackTraceArray(dvmThreadSelf()->curFrame, &methods, &length))
    {
        LOGE("Failed to create stack trace array\n");
        dvmThrowException("Ljava/lang/InternalError;", NULL);
        RETURN_VOID();
    }

    //int i;
    //LOGI("dvmCreateStackTraceArray results:\n");
    //for (i = 0; i < length; i++)
    //    LOGI(" %2d: %s.%s\n", i, methods[i]->clazz->name, methods[i]->name);

    /*
     * Generate a list of ProtectionDomain objects from the frames that
     * we're interested in.  Skip the first two methods (this method, and
     * the one that called us), and ignore reflection frames.  Stop on the
     * frame *after* the first privileged frame we see as we walk up.
     *
     * We create a new array, probably over-allocated, and fill in the
     * stuff we want.  We could also just run the list twice, but the
     * costs of the per-frame tests could be more expensive than the
     * second alloc.  (We could also allocate it on the stack using C99
     * array creation, but it's not guaranteed to fit.)
     *
     * The array we return doesn't include null ProtectionDomain objects,
     * so we skip those here.
     */
    Object** subSet = (Object**) malloc((length-2) * sizeof(Object*));
    if (subSet == NULL) {
        LOGE("Failed to allocate subSet (length=%d)\n", length);
        free(methods);
        dvmThrowException("Ljava/lang/InternalError;", NULL);
        RETURN_VOID();
    }
    int idx, subIdx = 0;
    for (idx = 2; idx < length; idx++) {
        const Method* meth = methods[idx];
        Object* pd;

        if (dvmIsReflectionMethod(meth))
            continue;

        if (dvmIsPrivilegedMethod(meth)) {
            /* find nearest non-reflection frame; note we skip priv frame */
            //LOGI("GSD priv frame at %s.%s\n", meth->clazz->name, meth->name);
            while (++idx < length && dvmIsReflectionMethod(methods[idx]))
                ;
            length = idx;       // stomp length to end loop
            meth = methods[idx];
        }

        /* get the pd object from the method's class */
        assert(gDvm.offJavaLangClass_pd != 0);
        pd = dvmGetFieldObject((Object*) meth->clazz,
                gDvm.offJavaLangClass_pd);
        //LOGI("FOUND '%s' pd=%p\n", meth->clazz->name, pd);
        if (pd != NULL)
            subSet[subIdx++] = pd;
    }

    //LOGI("subSet:\n");
    //for (i = 0; i < subIdx; i++)
    //    LOGI("  %2d: %s\n", i, subSet[i]->clazz->name);

    /*
     * Create an array object to contain "subSet".
     */
    ClassObject* pdArrayClass = NULL;
    ArrayObject* domains = NULL;
    pdArrayClass = dvmFindArrayClass("[Ljava/security/ProtectionDomain;", NULL);
    if (pdArrayClass == NULL) {
        LOGW("Unable to find ProtectionDomain class for array\n");
        goto bail;
    }
    domains = dvmAllocArray(pdArrayClass, subIdx, kObjectArrayRefWidth,
                ALLOC_DEFAULT);
    if (domains == NULL) {
        LOGW("Unable to allocate pd array (%d elems)\n", subIdx);
        goto bail;
    }

    /* copy the ProtectionDomain objects out */
    memcpy(domains->contents, subSet, subIdx * sizeof(Object *));
    dvmWriteBarrierArray(domains, 0, subIdx);

bail:
    free(subSet);
    free(methods);
    dvmReleaseTrackedAlloc((Object*) domains, NULL);
    RETURN_PTR(domains);
}

const DalvikNativeMethod dvm_java_security_AccessController[] = {
    { "getStackDomains",    "()[Ljava/security/ProtectionDomain;",
        Dalvik_java_security_AccessController_getStackDomains },
    { NULL, NULL, NULL },
};
