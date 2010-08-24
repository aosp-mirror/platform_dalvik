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
 * Handle Dalvik Debug Monitor requests and events.
 *
 * Remember that all DDM traffic is big-endian since it travels over the
 * JDWP connection.
 */
#include "Dalvik.h"

#include <fcntl.h>
#include <errno.h>

/*
 * "buf" contains a full JDWP packet, possibly with multiple chunks.  We
 * need to process each, accumulate the replies, and ship the whole thing
 * back.
 *
 * Returns "true" if we have a reply.  The reply buffer is newly allocated,
 * and includes the chunk type/length, followed by the data.
 *
 * TODO: we currently assume that the request and reply include a single
 * chunk.  If this becomes inconvenient we will need to adapt.
 */
bool dvmDdmHandlePacket(const u1* buf, int dataLen, u1** pReplyBuf,
    int* pReplyLen)
{
    Thread* self = dvmThreadSelf();
    const int kChunkHdrLen = 8;
    ArrayObject* dataArray = NULL;
    bool result = false;

    assert(dataLen >= 0);

    /*
     * Prep DdmServer.  We could throw this in gDvm.
     */
    ClassObject* ddmServerClass;
    Method* dispatch;

    ddmServerClass =
        dvmFindClass("Lorg/apache/harmony/dalvik/ddmc/DdmServer;", NULL);
    if (ddmServerClass == NULL) {
        LOGW("Unable to find org.apache.harmony.dalvik.ddmc.DdmServer\n");
        goto bail;
    }
    dispatch = dvmFindDirectMethodByDescriptor(ddmServerClass, "dispatch",
                    "(I[BII)Lorg/apache/harmony/dalvik/ddmc/Chunk;");
    if (dispatch == NULL) {
        LOGW("Unable to find DdmServer.dispatch\n");
        goto bail;
    }

    /*
     * Prep Chunk.
     */
    int chunkTypeOff, chunkDataOff, chunkOffsetOff, chunkLengthOff;
    ClassObject* chunkClass;
    chunkClass = dvmFindClass("Lorg/apache/harmony/dalvik/ddmc/Chunk;", NULL);
    if (chunkClass == NULL) {
        LOGW("Unable to find org.apache.harmony.dalvik.ddmc.Chunk\n");
        goto bail;
    }
    chunkTypeOff = dvmFindFieldOffset(chunkClass, "type", "I");
    chunkDataOff = dvmFindFieldOffset(chunkClass, "data", "[B");
    chunkOffsetOff = dvmFindFieldOffset(chunkClass, "offset", "I");
    chunkLengthOff = dvmFindFieldOffset(chunkClass, "length", "I");
    if (chunkTypeOff < 0 || chunkDataOff < 0 ||
        chunkOffsetOff < 0 || chunkLengthOff < 0)
    {
        LOGW("Unable to find all chunk fields\n");
        goto bail;
    }

    /*
     * The chunk handlers are written in the Java programming language, so
     * we need to convert the buffer to a byte array.
     */
    dataArray = dvmAllocPrimitiveArray('B', dataLen, ALLOC_DEFAULT);
    if (dataArray == NULL) {
        LOGW("array alloc failed (%d)\n", dataLen);
        dvmClearException(self);
        goto bail;
    }
    memcpy(dataArray->contents, buf, dataLen);

    /*
     * Run through and find all chunks.  [Currently just find the first.]
     */
    unsigned int offset, length, type;
    type = get4BE((u1*)dataArray->contents + 0);
    length = get4BE((u1*)dataArray->contents + 4);
    offset = kChunkHdrLen;
    if (offset+length > (unsigned int) dataLen) {
        LOGW("WARNING: bad chunk found (len=%u pktLen=%d)\n", length, dataLen);
        goto bail;
    }

    /*
     * Call the handler.
     */
    JValue callRes;
    dvmCallMethod(self, dispatch, NULL, &callRes, type, dataArray, offset,
        length);
    if (dvmCheckException(self)) {
        LOGI("Exception thrown by dispatcher for 0x%08x\n", type);
        dvmLogExceptionStackTrace();
        dvmClearException(self);
        goto bail;
    }

    Object* chunk;
    ArrayObject* replyData;
    chunk = (Object*) callRes.l;
    if (chunk == NULL)
        goto bail;

    /*
     * Pull the pieces out of the chunk.  We copy the results into a
     * newly-allocated buffer that the caller can free.  We don't want to
     * continue using the Chunk object because nothing has a reference to it.
     * (If we do an alloc in here, we need to dvmAddTrackedAlloc it.)
     *
     * We could avoid this by returning type/data/offset/length and having
     * the caller be aware of the object lifetime issues, but that
     * integrates the JDWP code more tightly into the VM, and doesn't work
     * if we have responses for multiple chunks.
     *
     * So we're pretty much stuck with copying data around multiple times.
     */
    type = dvmGetFieldInt(chunk, chunkTypeOff);
    replyData = (ArrayObject*) dvmGetFieldObject(chunk, chunkDataOff);
    offset = dvmGetFieldInt(chunk, chunkOffsetOff);
    length = dvmGetFieldInt(chunk, chunkLengthOff);

    LOGV("DDM reply: type=0x%08x data=%p offset=%d length=%d\n",
        type, replyData, offset, length);

    if (length == 0 || replyData == NULL)
        goto bail;
    if (offset + length > replyData->length) {
        LOGW("WARNING: chunk off=%d len=%d exceeds reply array len %d\n",
            offset, length, replyData->length);
        goto bail;
    }

    u1* reply;
    reply = (u1*) malloc(length + kChunkHdrLen);
    if (reply == NULL) {
        LOGW("malloc %d failed\n", length+kChunkHdrLen);
        goto bail;
    }
    set4BE(reply + 0, type);
    set4BE(reply + 4, length);
    memcpy(reply+kChunkHdrLen, (const u1*)replyData->contents + offset, length);

    *pReplyBuf = reply;
    *pReplyLen = length + kChunkHdrLen;
    result = true;

    LOGV("dvmHandleDdm returning type=%.4s buf=%p len=%d\n",
        (char*) reply, reply, length);

bail:
    dvmReleaseTrackedAlloc((Object*) dataArray, NULL);
    return result;
}

/* defined in org.apache.harmony.dalvik.ddmc.DdmServer */
#define CONNECTED       1
#define DISCONNECTED    2

/*
 * Broadcast an event to all handlers.
 */
static void broadcast(int event)
{
    ClassObject* ddmServerClass;
    Method* bcast;

    ddmServerClass =
        dvmFindClass("Lorg/apache/harmony/dalvik/ddmc/DdmServer;", NULL);
    if (ddmServerClass == NULL) {
        LOGW("Unable to find org.apache.harmony.dalvik.ddmc.DdmServer\n");
        goto bail;
    }
    bcast = dvmFindDirectMethodByDescriptor(ddmServerClass, "broadcast", "(I)V");
    if (bcast == NULL) {
        LOGW("Unable to find DdmServer.broadcast\n");
        goto bail;
    }

    Thread* self = dvmThreadSelf();

    if (self->status != THREAD_RUNNING) {
        LOGE("ERROR: DDM broadcast with thread status=%d\n", self->status);
        /* try anyway? */
    }

    JValue unused;
    dvmCallMethod(self, bcast, NULL, &unused, event);
    if (dvmCheckException(self)) {
        LOGI("Exception thrown by broadcast(%d)\n", event);
        dvmLogExceptionStackTrace();
        dvmClearException(self);
        goto bail;
    }

bail:
    ;
}

/*
 * First DDM packet has arrived over JDWP.  Notify the press.
 *
 * We can do some initialization here too.
 */
void dvmDdmConnected(void)
{
    // TODO: any init

    LOGV("Broadcasting DDM connect\n");
    broadcast(CONNECTED);
}

/*
 * JDWP connection has dropped.
 *
 * Do some cleanup.
 */
void dvmDdmDisconnected(void)
{
    LOGV("Broadcasting DDM disconnect\n");
    broadcast(DISCONNECTED);

    gDvm.ddmThreadNotification = false;
}


/*
 * Turn thread notification on or off.
 */
void dvmDdmSetThreadNotification(bool enable)
{
    /*
     * We lock the thread list to avoid sending duplicate events or missing
     * a thread change.  We should be okay holding this lock while sending
     * the messages out.  (We have to hold it while accessing a live thread.)
     */
    dvmLockThreadList(NULL);
    gDvm.ddmThreadNotification = enable;

    if (enable) {
        Thread* thread;
        for (thread = gDvm.threadList; thread != NULL; thread = thread->next) {
            //LOGW("notify %d\n", thread->threadId);
            dvmDdmSendThreadNotification(thread, true);
        }
    }

    dvmUnlockThreadList();
}

/*
 * Send a notification when a thread starts or stops.
 *
 * Because we broadcast the full set of threads when the notifications are
 * first enabled, it's possible for "thread" to be actively executing.
 */
void dvmDdmSendThreadNotification(Thread* thread, bool started)
{
    if (!gDvm.ddmThreadNotification)
        return;

    StringObject* nameObj = NULL;
    Object* threadObj = thread->threadObj;

    if (threadObj != NULL) {
        nameObj = (StringObject*)
            dvmGetFieldObject(threadObj, gDvm.offJavaLangThread_name);
    }

    int type, len;
    u1 buf[256];

    if (started) {
        const u2* chars;
        u2* outChars;
        size_t stringLen;

        type = CHUNK_TYPE("THCR");

        if (nameObj != NULL) {
            stringLen = dvmStringLen(nameObj);
            chars = dvmStringChars(nameObj);
        } else {
            stringLen = 0;
            chars = NULL;
        }

        /* leave room for the two integer fields */
        if (stringLen > (sizeof(buf) - sizeof(u4)*2) / 2)
            stringLen = (sizeof(buf) - sizeof(u4)*2) / 2;
        len = stringLen*2 + sizeof(u4)*2;

        set4BE(&buf[0x00], thread->threadId);
        set4BE(&buf[0x04], stringLen);

        /* copy the UTF-16 string, transforming to big-endian */
        outChars = (u2*) &buf[0x08];
        while (stringLen--)
            set2BE((u1*) (outChars++), *chars++);
    } else {
        type = CHUNK_TYPE("THDE");

        len = 4;

        set4BE(&buf[0x00], thread->threadId);
    }

    dvmDbgDdmSendChunk(type, len, buf);
}

/*
 * Send a notification when a thread's name changes.
 */
void dvmDdmSendThreadNameChange(int threadId, StringObject* newName)
{
    if (!gDvm.ddmThreadNotification)
        return;

    size_t stringLen = dvmStringLen(newName);
    const u2* chars = dvmStringChars(newName);

    /*
     * Output format:
     *  (4b) thread ID
     *  (4b) stringLen
     *  (xb) string chars
     */
    int bufLen = 4 + 4 + (stringLen * 2);
    u1 buf[bufLen];

    set4BE(&buf[0x00], threadId);
    set4BE(&buf[0x04], stringLen);
    u2* outChars = (u2*) &buf[0x08];
    while (stringLen--)
        set2BE((u1*) (outChars++), *chars++);

    dvmDbgDdmSendChunk(CHUNK_TYPE("THNM"), bufLen, buf);
}

/*
 * Get some per-thread stats.
 *
 * This is currently generated by opening the appropriate "stat" file
 * in /proc and reading the pile of stuff that comes out.
 */
static bool getThreadStats(pid_t pid, pid_t tid, unsigned long* pUtime,
    unsigned long* pStime)
{
    /*
    int pid;
    char comm[128];
    char state;
    int ppid, pgrp, session, tty_nr, tpgid;
    unsigned long flags, minflt, cminflt, majflt, cmajflt, utime, stime;
    long cutime, cstime, priority, nice, zero, itrealvalue;
    unsigned long starttime, vsize;
    long rss;
    unsigned long rlim, startcode, endcode, startstack, kstkesp, kstkeip;
    unsigned long signal, blocked, sigignore, sigcatch, wchan, nswap, cnswap;
    int exit_signal, processor;
    unsigned long rt_priority, policy;

    scanf("%d %s %c %d %d %d %d %d %lu %lu %lu %lu %lu %lu %lu %ld %ld %ld "
          "%ld %ld %ld %lu %lu %ld %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu "
          "%lu %lu %lu %d %d %lu %lu",
        &pid, comm, &state, &ppid, &pgrp, &session, &tty_nr, &tpgid,
        &flags, &minflt, &cminflt, &majflt, &cmajflt, &utime, &stime,
        &cutime, &cstime, &priority, &nice, &zero, &itrealvalue,
        &starttime, &vsize, &rss, &rlim, &startcode, &endcode,
        &startstack, &kstkesp, &kstkeip, &signal, &blocked, &sigignore,
        &sigcatch, &wchan, &nswap, &cnswap, &exit_signal, &processor,
        &rt_priority, &policy);
    */

    char nameBuf[64];
    int i, fd;

    /*
     * Open and read the appropriate file.  This is expected to work on
     * Linux but will fail on other platforms (e.g. Mac sim).
     */
    sprintf(nameBuf, "/proc/%d/task/%d/stat", (int) pid, (int) tid);
    fd = open(nameBuf, O_RDONLY);
    if (fd < 0) {
        LOGV("Unable to open '%s': %s\n", nameBuf, strerror(errno));
        return false;
    }

    char lineBuf[512];      // > 2x typical
    int cc;
    cc = read(fd, lineBuf, sizeof(lineBuf)-1);
    if (cc <= 0) {
        const char* msg = (cc == 0) ? "unexpected EOF" : strerror(errno);
        LOGI("Unable to read '%s': %s\n", nameBuf, msg);
        close(fd);
        return false;
    }
    lineBuf[cc] = '\0';

    /*
     * Skip whitespace-separated tokens.
     */
    static const char* kWhitespace = " ";
    char* cp = lineBuf;
    for (i = 0; i < 13; i++) {
        cp += strcspn(cp, kWhitespace);     // skip token
        cp += strspn(cp, kWhitespace);      // skip whitespace
    }

    /*
     * Grab the values we want.
     */
    char* endp;
    *pUtime = strtoul(cp, &endp, 10);
    if (endp == cp)
        LOGI("Warning: strtoul failed on utime ('%.30s...')\n", cp);

    cp += strcspn(cp, kWhitespace);
    cp += strspn(cp, kWhitespace);

    *pStime = strtoul(cp, &endp, 10);
    if (endp == cp)
        LOGI("Warning: strtoul failed on stime ('%.30s...')\n", cp);

    close(fd);
    return true;
}

/*
 * Generate the contents of a THST chunk.  The data encompasses all known
 * threads.
 *
 * Response has:
 *  (1b) header len
 *  (1b) bytes per entry
 *  (2b) thread count
 * Then, for each thread:
 *  (4b) threadId
 *  (1b) thread status
 *  (4b) tid
 *  (4b) utime
 *  (4b) stime
 *  (1b) is daemon?
 *
 * The length fields exist in anticipation of adding additional fields
 * without wanting to break ddms or bump the full protocol version.  I don't
 * think it warrants full versioning.  They might be extraneous and could
 * be removed from a future version.
 *
 * Returns a new byte[] with the data inside, or NULL on failure.  The
 * caller must call dvmReleaseTrackedAlloc() on the array.
 */
ArrayObject* dvmDdmGenerateThreadStats(void)
{
    const int kHeaderLen = 4;
    const int kBytesPerEntry = 18;

    dvmLockThreadList(NULL);

    Thread* thread;
    int threadCount = 0;
    for (thread = gDvm.threadList; thread != NULL; thread = thread->next)
        threadCount++;

    /*
     * Create a temporary buffer.  We can't perform heap allocation with
     * the thread list lock held (could cause a GC).  The output is small
     * enough to sit on the stack.
     */
    int bufLen = kHeaderLen + threadCount * kBytesPerEntry;
    u1 tmpBuf[bufLen];
    u1* buf = tmpBuf;

    set1(buf+0, kHeaderLen);
    set1(buf+1, kBytesPerEntry);
    set2BE(buf+2, (u2) threadCount);
    buf += kHeaderLen;

    pid_t pid = getpid();
    for (thread = gDvm.threadList; thread != NULL; thread = thread->next) {
        unsigned long utime, stime;
        bool isDaemon = false;

        if (!getThreadStats(pid, thread->systemTid, &utime, &stime)) {
            // failed; drop in empty values
            utime = stime = 0;
        }

        Object* threadObj = thread->threadObj;
        if (threadObj != NULL) {
            isDaemon = dvmGetFieldBoolean(threadObj,
                            gDvm.offJavaLangThread_daemon);
        }

        set4BE(buf+0, thread->threadId);
        set1(buf+4, thread->status);
        set4BE(buf+5, thread->systemTid);
        set4BE(buf+9, utime);
        set4BE(buf+13, stime);
        set1(buf+17, isDaemon);

        buf += kBytesPerEntry;
    }
    dvmUnlockThreadList();


    /*
     * Create a byte array to hold the data.
     */
    ArrayObject* arrayObj = dvmAllocPrimitiveArray('B', bufLen, ALLOC_DEFAULT);
    if (arrayObj != NULL)
        memcpy(arrayObj->contents, tmpBuf, bufLen);
    return arrayObj;
}


/*
 * Find the specified thread and return its stack trace as an array of
 * StackTraceElement objects.
 */
ArrayObject* dvmDdmGetStackTraceById(u4 threadId)
{
    Thread* self = dvmThreadSelf();
    Thread* thread;
    int* traceBuf;

    dvmLockThreadList(self);

    for (thread = gDvm.threadList; thread != NULL; thread = thread->next) {
        if (thread->threadId == threadId)
            break;
    }
    if (thread == NULL) {
        LOGI("dvmDdmGetStackTraceById: threadid=%d not found\n", threadId);
        dvmUnlockThreadList();
        return NULL;
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
    return trace;
}

/*
 * Gather up the allocation data and copy it into a byte[].
 *
 * Returns NULL on failure with an exception raised.
 */
ArrayObject* dvmDdmGetRecentAllocations(void)
{
    u1* data;
    size_t len;

    if (!dvmGenerateTrackedAllocationReport(&data, &len)) {
        /* assume OOM */
        dvmThrowException("Ljava/lang/OutOfMemoryError;","recent alloc native");
        return NULL;
    }

    ArrayObject* arrayObj = dvmAllocPrimitiveArray('B', len, ALLOC_DEFAULT);
    if (arrayObj != NULL)
        memcpy(arrayObj->contents, data, len);
    return arrayObj;
}
