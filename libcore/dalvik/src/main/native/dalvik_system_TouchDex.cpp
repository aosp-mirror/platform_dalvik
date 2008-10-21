/*
 * Copyright (C) 2007 The Android Open Source Project
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
 * Bit of code to wrap DEX force-updating with a fork() call.
 */

#define LOG_TAG "TouchDex"
#include "JNIHelp.h"

#include "cutils/properties.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <assert.h>
#include <errno.h>

#define JAVA_PACKAGE "dalvik/system"

#ifndef HAVE_ANDROID_OS
# define BASE_DIR "/work/device/out/linux-x86-debug-sim"
#else
# define BASE_DIR ""
#endif

namespace android {

// fwd
static void logProcStatus(pid_t pid);


/*
 * private static int trampoline(String dexFiles, String bcp)
 */
static jint dalvik_system_TouchDex_trampoline(JNIEnv* env,
    jclass clazz, jstring dexFilesStr, jstring bcpStr)
{
#ifndef HAVE_ANDROID_OS
    /* don't do this on simulator -- gdb goes "funny" in goobuntu */
    return 0;
#endif

    const int kMinTimeout = 900;        // 90 seconds
    const char* bcp;
    const char* dexFiles;
    static const char* kExecFile = BASE_DIR "/system/bin/dalvikvm";
    //static const char* kDebugArg =
    //        "-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n";
    static const char* kBcpArgName = "-Xbootclasspath:";
    static const char* kClassName = "dalvik.system.TouchDex";
    static const char* kExecMode = "-Xint";
    static const int argc = 7;
    char* bcpArg;
    const char* argv[argc+1];
    const char* kVerifyArg;
    const char* kDexOptArg;
    int timeoutMult;
    pid_t pid;
    struct timeval startWhen, endWhen;
    char propBuf[PROPERTY_VALUE_MAX];
    char execModeBuf[PROPERTY_VALUE_MAX + sizeof("-X")];
    bool verifyJava = true;

    property_get("dalvik.vm.verify-bytecode", propBuf, "");
    if (strcmp(propBuf, "true") == 0) {
        verifyJava = true;
    } else if (strcmp(propBuf, "false") == 0) {
        verifyJava = false;
    } else {
        /* bad value or not defined; use default */
    }

    if (verifyJava) {
        kVerifyArg = "-Xverify:all";
        kDexOptArg = "-Xdexopt:verified";
        timeoutMult = 11;
    } else {
        kVerifyArg = "-Xverify:none";
        //kDexOptArg = "-Xdexopt:all";
        kDexOptArg = "-Xdexopt:verified";
        timeoutMult = 7;
    }

    property_get("dalvik.vm.execution-mode", propBuf, "");
    if (strncmp(propBuf, "int:", 4) == 0) {
        strcpy(execModeBuf, "-X");
        strcat(execModeBuf, propBuf);
        kExecMode = execModeBuf;
    }

    LOGV("TouchDex trampoline forking\n");
    gettimeofday(&startWhen, NULL);

    /*
     * Retrieve strings.  Note we want to do this *before* the fork() -- bad
     * idea to perform Java operations in the child process (not all threads
     * get carried over to the new process).
     */
    bcp = env->GetStringUTFChars(bcpStr, NULL);
    dexFiles = env->GetStringUTFChars(dexFilesStr, NULL);
    if (bcp == NULL || dexFiles == NULL) {
        LOGE("Bad values for bcp=%p dexFiles=%p\n", bcp, dexFiles);
        abort();
    }

    pid = fork();
    if (pid < 0) {
        LOGE("fork failed: %s", strerror(errno));
        return -1;
    }

    if (pid == 0) {
        /* child */
        char* bcpArg;

        LOGV("TouchDex trampoline in child\n");

        bcpArg = (char*) malloc(strlen(bcp) + strlen(kBcpArgName) +1);
        strcpy(bcpArg, kBcpArgName);
        strcat(bcpArg, bcp);

        argv[0] = kExecFile;
        argv[1] = bcpArg;
        argv[2] = kVerifyArg;
        argv[3] = kDexOptArg;
        argv[4] = kExecMode;
        argv[5] = kClassName;
        argv[6] = dexFiles;
        argv[7] = NULL;

        //LOGI("Calling execv with args:\n");
        //for (int i = 0; i < argc; i++)
        //    LOGI(" %d: '%s'\n", i, argv[i]);

        execv(kExecFile, (char* const*) argv);
        free(bcpArg);

        LOGE("execv '%s' failed: %s\n", kExecFile, strerror(errno));
        exit(1);
    } else {
        int cc, count, dexCount, timeout;
        int result = -1;
        const char* cp;

        /*
         * Adjust the timeout based on how many DEX files we have to
         * process.  Larger DEX files take longer, so this is a crude
         * approximation at best.
         *
         * We need this for http://b/issue?id=836771, which can leave us
         * stuck waiting for a long time even if there is no work to be done.
         *
         * This is currently being (ab)used to convert single files, which
         * sort of spoils the timeout calculation.  We establish a minimum
         * timeout for single apps.
         *
         * The timeout calculation doesn't work at all right when a
         * directory is specified.  So the minimum is now a minute.  At
         * this point it's probably safe to just remove the timeout.
         *
         * The timeout is in 1/10ths of a second.
         */
        dexCount = 1;
        cp = dexFiles;
        while (*++cp != '\0') {
            if (*cp == ':')
                dexCount++;
        }
        timeout = timeoutMult * dexCount;
        if (timeout < kMinTimeout)
            timeout = kMinTimeout;

        env->ReleaseStringUTFChars(bcpStr, bcp);
        env->ReleaseStringUTFChars(dexFilesStr, dexFiles);


        LOGD("TouchDex parent waiting for pid=%d (timeout=%.1fs)\n",
            (int) pid, timeout / 10.0);
        for (count = 0; count < timeout; count++) {
            /* waitpid doesn't take a timeout, so poll and sleep */
            cc = waitpid(pid, &result, WNOHANG);
            if (cc < 0) {
                LOGE("waitpid(%d) failed: %s", (int) pid, strerror(errno));
                return -1;
            } else if (cc == 0) {
                usleep(100000);     /* 0.1 sec */
            } else {
                /* success! */
                break;
            }
        }

        if (count == timeout) {
            /* note kill(0) returns 0 if the pid is a zombie */
            LOGE("timed out waiting for %d; kill(0) returns %d\n",
                (int) pid, kill(pid, 0));
            logProcStatus(pid);
        } else {
            LOGV("TouchDex done after %d iterations (kill(0) returns %d)\n",
                count, kill(pid, 0));
        }

        gettimeofday(&endWhen, NULL);
        long long start = startWhen.tv_sec * 1000000 + startWhen.tv_usec;
        long long end = endWhen.tv_sec * 1000000 + endWhen.tv_usec;

        LOGI("Dalvik-cache prep: status=0x%04x, finished in %dms\n",
            result, (int) ((end - start) / 1000));

        if (WIFEXITED(result))
            return WEXITSTATUS(result);
        else
            return result;
    }
}

/*
 * Dump the contents of /proc/<pid>/status to the log file.
 */
static void logProcStatus(pid_t pid)
{
    char localBuf[256];
    FILE* fp;

    sprintf(localBuf, "/proc/%d/status", (int) pid);
    fp = fopen(localBuf, "r");
    if (fp == NULL) {
        LOGI("Unable to open '%s'\n", localBuf);
        return;
    }

    LOGI("Contents of %s:\n", localBuf);
    while (true) {
        fgets(localBuf, sizeof(localBuf), fp);
        if (ferror(fp) || feof(fp))
            break;
        LOGI("  %s", localBuf);
    }

    fclose(fp);
}

/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "trampoline", "(Ljava/lang/String;Ljava/lang/String;)I",
        (void*) dalvik_system_TouchDex_trampoline },
};

extern "C" int register_dalvik_system_TouchDex(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, JAVA_PACKAGE "/TouchDex",
        gMethods, NELEM(gMethods));
}

}; // namespace android

