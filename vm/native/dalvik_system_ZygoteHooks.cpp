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
 * dalvik.system.Zygote
 */
#include "Dalvik.h"
#include "Thread.h"
#include "native/InternalNativePriv.h"

#include <sys/resource.h>

#if defined(HAVE_PRCTL)
# include <sys/prctl.h>
#endif

#define ZYGOTE_LOG_TAG "Zygote"

/* must match values in com.android.internal.os.Zygote */
enum {
    DEBUG_ENABLE_DEBUGGER           = 1,
    DEBUG_ENABLE_CHECKJNI           = 1 << 1,
    DEBUG_ENABLE_ASSERT             = 1 << 2,
    DEBUG_ENABLE_SAFEMODE           = 1 << 3,
    DEBUG_ENABLE_JNI_LOGGING        = 1 << 4,
};

/*
 * Enable/disable debug features requested by the caller.
 *
 * debugger
 *   If set, enable debugging; if not set, disable debugging.  This is
 *   easy to handle, because the JDWP thread isn't started until we call
 *   dvmInitAfterZygote().
 * checkjni
 *   If set, make sure "check JNI" is enabled.
 * assert
 *   If set, make sure assertions are enabled.  This gets fairly weird,
 *   because it affects the result of a method called by class initializers,
 *   and hence can't affect pre-loaded/initialized classes.
 * safemode
 *   If set, operates the VM in the safe mode. The definition of "safe mode" is
 *   implementation dependent and currently only the JIT compiler is disabled.
 *   This is easy to handle because the compiler thread and associated resources
 *   are not requested until we call dvmInitAfterZygote().
 */
static void enableDebugFeatures(u4 debugFlags)
{
    gDvm.jdwpAllowed = ((debugFlags & DEBUG_ENABLE_DEBUGGER) != 0);

    if ((debugFlags & DEBUG_ENABLE_CHECKJNI) != 0) {
        /* turn it on if it's not already enabled */
        dvmLateEnableCheckedJni();
    }

    if ((debugFlags & DEBUG_ENABLE_JNI_LOGGING) != 0) {
        gDvmJni.logThirdPartyJni = true;
    }

    if ((debugFlags & DEBUG_ENABLE_ASSERT) != 0) {
        /* turn it on if it's not already enabled */
        dvmLateEnableAssertions();
    }

    if ((debugFlags & DEBUG_ENABLE_SAFEMODE) != 0) {
#if defined(WITH_JIT)
        /* turn off the jit if it is explicitly requested by the app */
        if (gDvm.executionMode == kExecutionModeJit)
            gDvm.executionMode = kExecutionModeInterpFast;
#endif
    }

#ifdef HAVE_ANDROID_OS
    if ((debugFlags & DEBUG_ENABLE_DEBUGGER) != 0) {
        /* To let a non-privileged gdbserver attach to this
         * process, we must set its dumpable bit flag. However
         * we are not interested in generating a coredump in
         * case of a crash, so also set the coredump size to 0
         * to disable that
         */
        if (prctl(PR_SET_DUMPABLE, 1, 0, 0, 0) < 0) {
            ALOGE("could not set dumpable bit flag for pid %d: %s",
                 getpid(), strerror(errno));
        } else {
            struct rlimit rl;
            rl.rlim_cur = 0;
            rl.rlim_max = RLIM_INFINITY;
            if (setrlimit(RLIMIT_CORE, &rl) < 0) {
                ALOGE("could not disable core file generation for pid %d: %s",
                    getpid(), strerror(errno));
            }
        }
    }
#endif
}

/*
 * native public static long nativePreFork()
 */
static void Dalvik_dalvik_system_ZygoteHooks_preFork(const u4* args,
    JValue* pResult)
{
    dvmDumpLoaderStats("zygote");

    if (!gDvm.zygote) {
        dvmThrowIllegalStateException(
            "VM instance not started with -Xzygote");

        RETURN_LONG(-1L);
    }

    if (!dvmGcPreZygoteFork()) {
        ALOGE("pre-fork heap failed");
        dvmAbort();
    }

    RETURN_LONG(0L);
}

/*
 * native public static int nativePostForkChild(long token, int debug_flags),
 */
static void Dalvik_dalvik_system_ZygoteHooks_postForkChild(
        const u4* args, JValue* pResult)
{
    /*
     * Our system thread ID has changed.  Get the new one.
     */
    Thread* thread = dvmThreadSelf();
    thread->systemTid = dvmGetSysThreadId();

    /* configure additional debug options */
    enableDebugFeatures(args[2]);

    gDvm.zygote = false;
    if (!dvmInitAfterZygote()) {
        ALOGE("error in post-zygote initialization");
        dvmAbort();
    }

    RETURN_VOID();
}

const DalvikNativeMethod dvm_dalvik_system_ZygoteHooks[] = {
    { "nativePreFork", "()J",
      Dalvik_dalvik_system_ZygoteHooks_preFork },
    { "nativePostForkChild", "(JI)V",
      Dalvik_dalvik_system_ZygoteHooks_postForkChild },
    { NULL, NULL, NULL },
};
