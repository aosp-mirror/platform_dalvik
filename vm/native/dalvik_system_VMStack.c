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
 * dalvik.system.VMStack
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"


/*
 * public static ClassLoader getCallingClassLoader()
 *
 * Return the defining class loader of the caller's caller.
 */
static void Dalvik_dalvik_system_VMStack_getCallingClassLoader(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = dvmGetCaller2Class(dvmThreadSelf()->curFrame);

    UNUSED_PARAMETER(args);

    if (clazz == NULL)
        RETURN_PTR(NULL);
    RETURN_PTR(clazz->classLoader);
}

/*
 * public static ClassLoader getCallingClassLoader2()
 *
 * Return the defining class loader of the caller's caller's caller.
 */
static void Dalvik_dalvik_system_VMStack_getCallingClassLoader2(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = dvmGetCaller3Class(dvmThreadSelf()->curFrame);

    UNUSED_PARAMETER(args);

    if (clazz == NULL)
        RETURN_PTR(NULL);
    RETURN_PTR(clazz->classLoader);
}

/*
 * public static Class<?> getStackClass2()
 *
 * Returns the class of the caller's caller's caller.
 */
static void Dalvik_dalvik_system_VMStack_getStackClass2(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = dvmGetCaller3Class(dvmThreadSelf()->curFrame);

    UNUSED_PARAMETER(args);

    RETURN_PTR(clazz);
}

/*
 * public static Class<?>[] getClasses(int maxDepth, boolean stopAtPrivileged)
 *
 * Create an array of classes for the methods on the stack, skipping the
 * first two and all reflection methods.  If "stopAtPrivileged" is set,
 * stop shortly after we encounter a privileged class.
 */
static void Dalvik_dalvik_system_VMStack_getClasses(const u4* args,
    JValue* pResult)
{
    /* note "maxSize" is unsigned, so -1 turns into a very large value */
    unsigned int maxSize = args[0];
    bool stopAtPrivileged = args[1];
    unsigned int size = 0;
    const unsigned int kSkip = 2;
    const Method** methods = NULL;
    int methodCount;

    /*
     * Get an array with the stack trace in it.
     */
    if (!dvmCreateStackTraceArray(dvmThreadSelf()->curFrame, &methods,
            &methodCount))
    {
        LOGE("Failed to create stack trace array\n");
        dvmThrowException("Ljava/lang/InternalError;", NULL);
        RETURN_VOID();
    }

    //int i;
    //LOGI("dvmCreateStackTraceArray results:\n");
    //for (i = 0; i < methodCount; i++) {
    //    LOGI(" %2d: %s.%s\n",
    //        i, methods[i]->clazz->descriptor, methods[i]->name);
    //}

    /*
     * Run through the array and count up how many elements there are.
     */
    unsigned int idx;
    for (idx = kSkip; (int) idx < methodCount && size < maxSize; idx++) {
        const Method* meth = methods[idx];

        if (dvmIsReflectionMethod(meth))
            continue;

        if (stopAtPrivileged && dvmIsPrivilegedMethod(meth)) {
            /*
             * We want the last element of the array to be the caller of
             * the privileged method, so we want to include the privileged
             * method and the next one.
             */
            if (maxSize > size + 2)
                maxSize = size + 2;
        }

        size++;
    }

    /*
     * Create an array object to hold the classes.
     * TODO: can use gDvm.classJavaLangClassArray here?
     */
    ClassObject* classArrayClass = NULL;
    ArrayObject* classes = NULL;
    classArrayClass = dvmFindArrayClass("[Ljava/lang/Class;", NULL);
    if (classArrayClass == NULL) {
        LOGW("Unable to find java.lang.Class array class\n");
        goto bail;
    }
    classes = dvmAllocArray(classArrayClass, size, kObjectArrayRefWidth,
                ALLOC_DEFAULT);
    if (classes == NULL) {
        LOGW("Unable to allocate class array (%d elems)\n", size);
        goto bail;
    }

    /*
     * Fill in the array.
     */
    unsigned int objCount = 0;
    for (idx = kSkip; (int) idx < methodCount; idx++) {
        if (dvmIsReflectionMethod(methods[idx])) {
            continue;
        }
        dvmSetObjectArrayElement(classes, objCount,
                                 (Object *)methods[idx]->clazz);
        objCount++;
    }
    assert(objCount == classes->length);

bail:
    free(methods);
    dvmReleaseTrackedAlloc((Object*) classes, NULL);
    RETURN_PTR(classes);
}

/*
 * public static StackTraceElement[] getThreadStackTrace(Thread t)
 *
 * Retrieve the stack trace of the specified thread and return it as an
 * array of StackTraceElement.  Returns NULL on failure.
 */
static void Dalvik_dalvik_system_VMStack_getThreadStackTrace(const u4* args,
    JValue* pResult)
{
    Object* targetThreadObj = (Object*) args[0];
    Thread* self = dvmThreadSelf();
    Thread* thread;
    int* traceBuf;

    assert(targetThreadObj != NULL);

    dvmLockThreadList(self);

    /*
     * Make sure the thread is still alive and in the list.
     */
    for (thread = gDvm.threadList; thread != NULL; thread = thread->next) {
        if (thread->threadObj == targetThreadObj)
            break;
    }
    if (thread == NULL) {
        LOGI("VMStack.getThreadStackTrace: threadObj %p not active\n",
            targetThreadObj);
        dvmUnlockThreadList();
        RETURN_PTR(NULL);
    }

    /*
     * Suspend the thread, pull out the stack trace, then resume the thread
     * and release the thread list lock.  If we're being asked to examine
     * our own stack trace, skip the suspend/resume.
     */
    int stackDepth = -1;
    if (thread != self)
        dvmSuspendThread(thread);
    traceBuf = dvmFillInStackTraceRaw(thread, &stackDepth);
    if (thread != self)
        dvmResumeThread(thread);
    dvmUnlockThreadList();

    /*
     * Convert the raw buffer into an array of StackTraceElement.
     */
    ArrayObject* trace = dvmGetStackTraceRaw(traceBuf, stackDepth);
    free(traceBuf);
    RETURN_PTR(trace);
}

const DalvikNativeMethod dvm_dalvik_system_VMStack[] = {
    { "getCallingClassLoader",  "()Ljava/lang/ClassLoader;",
        Dalvik_dalvik_system_VMStack_getCallingClassLoader },
    { "getCallingClassLoader2", "()Ljava/lang/ClassLoader;",
        Dalvik_dalvik_system_VMStack_getCallingClassLoader2 },
    { "getStackClass2", "()Ljava/lang/Class;",
        Dalvik_dalvik_system_VMStack_getStackClass2 },
    { "getClasses",             "(IZ)[Ljava/lang/Class;",
        Dalvik_dalvik_system_VMStack_getClasses },
    { "getThreadStackTrace",    "(Ljava/lang/Thread;)[Ljava/lang/StackTraceElement;",
        Dalvik_dalvik_system_VMStack_getThreadStackTrace },
    { NULL, NULL, NULL },
};
