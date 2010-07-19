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
 * Dalvik-specific side of debugger support.  (The JDWP code is intended to
 * be relatively generic.)
 */
#ifndef _DALVIK_DEBUGGER
#define _DALVIK_DEBUGGER

#include "Common.h"
#include "Misc.h"
#include "jdwp/Jdwp.h"
#include <pthread.h>

/* fwd decl */
struct Object;
struct ClassObject;
struct Method;
struct Thread;

/*
 * Used by StepControl to track a set of addresses associated with
 * a single line.
 */
typedef struct AddressSet {
    u4 setSize;
    u1 set[1];
} AddressSet;

INLINE void dvmAddressSetSet(AddressSet *pSet, u4 toSet)
{
    if (toSet < pSet->setSize) {
        pSet->set[toSet/8] |= 1 << (toSet % 8);
    }
}

INLINE bool dvmAddressSetGet(const AddressSet *pSet, u4 toGet)
{
    if (toGet < pSet->setSize) {
        return (pSet->set[toGet/8] & (1 << (toGet % 8))) != 0;
    } else {
        return false;
    }
}

/*
 * Single-step management.
 */
typedef struct StepControl {
    /* request */
    enum JdwpStepSize   size;
    enum JdwpStepDepth  depth;
    struct Thread*      thread;         /* don't deref; for comparison only */

    /* current state */
    bool                active;
    const struct Method* method;
    int                 line;           /* line #; could be -1 */
    const AddressSet*   pAddressSet;    /* if non-null, address set for line */
    int                 frameDepth;
} StepControl;

/*
 * Invoke-during-breakpoint support.
 */
typedef struct DebugInvokeReq {
    /* boolean; only set when we're in the tail end of an event handler */
    bool ready;

    /* boolean; set if the JDWP thread wants this thread to do work */
    bool invokeNeeded;

    /* request */
    struct Object*      obj;        /* not used for ClassType.InvokeMethod */
    struct Object*      thread;
    struct ClassObject* clazz;
    struct Method*      method;
    u4                  numArgs;
    u8*                 argArray;   /* will be NULL if numArgs==0 */
    u4                  options;

    /* result */
    JdwpError           err;
    u1                  resultTag;
    JValue              resultValue;
    ObjectId            exceptObj;

    /* condition variable to wait on while the method executes */
    pthread_mutex_t     lock;
    pthread_cond_t      cv;
} DebugInvokeReq;

/* system init/shutdown */
bool dvmDebuggerStartup(void);
void dvmDebuggerShutdown(void);

void dvmDbgInitMutex(pthread_mutex_t* pMutex);
void dvmDbgLockMutex(pthread_mutex_t* pMutex);
void dvmDbgUnlockMutex(pthread_mutex_t* pMutex);
void dvmDbgInitCond(pthread_cond_t* pCond);
void dvmDbgCondWait(pthread_cond_t* pCond, pthread_mutex_t* pMutex);
void dvmDbgCondSignal(pthread_cond_t* pCond);
void dvmDbgCondBroadcast(pthread_cond_t* pCond);

/*
 * Return the DebugInvokeReq for the current thread.
 */
DebugInvokeReq* dvmDbgGetInvokeReq(void);

/*
 * Enable/disable breakpoints and step modes.  Used to provide a heads-up
 * when the debugger attaches.
 */
void dvmDbgConnected(void);
void dvmDbgActive(void);
void dvmDbgDisconnected(void);

/*
 * Returns "true" if a debugger is connected.  Returns "false" if it's
 * just DDM.
 */
bool dvmDbgIsDebuggerConnected(void);

/*
 * Time, in milliseconds, since the last debugger activity.  Does not
 * include DDMS activity.  Returns -1 if there has been no activity.
 * Returns 0 if we're in the middle of handling a debugger request.
 */
s8 dvmDbgLastDebuggerActivity(void);

/*
 * Block/allow GC depending on what we're doing.  These return the old
 * status, which can be fed to dvmDbgThreadGoing() to restore the previous
 * mode.
 */
int dvmDbgThreadRunning(void);
int dvmDbgThreadWaiting(void);
int dvmDbgThreadContinuing(int status);

/*
 * The debugger wants the VM to exit.
 */
void dvmDbgExit(int status);

/*
 * Class, Object, Array
 */
const char* dvmDbgGetClassDescriptor(RefTypeId id);
ObjectId dvmDbgGetClassObject(RefTypeId id);
RefTypeId dvmDbgGetSuperclass(RefTypeId id);
ObjectId dvmDbgGetClassLoader(RefTypeId id);
u4 dvmDbgGetAccessFlags(RefTypeId id);
bool dvmDbgIsInterface(RefTypeId id);
void dvmDbgGetClassList(u4* pNumClasses, RefTypeId** pClassRefBuf);
void dvmDbgGetVisibleClassList(ObjectId classLoaderId, u4* pNumClasses,
        RefTypeId** pClassRefBuf);
void dvmDbgGetClassInfo(RefTypeId classId, u1* pTypeTag, u4* pStatus,
    char** pSignature);
bool dvmDbgFindLoadedClassBySignature(const char* classDescriptor,
        RefTypeId* pRefTypeId);
void dvmDbgGetObjectType(ObjectId objectId, u1* pRefTypeTag,
    RefTypeId* pRefTypeId);
u1 dvmDbgGetClassObjectType(RefTypeId refTypeId);
char* dvmDbgGetSignature(RefTypeId refTypeId);
const char* dvmDbgGetSourceFile(RefTypeId refTypeId);
char* dvmDbgGetObjectTypeName(ObjectId objectId);
int dvmDbgGetSignatureTag(const char* signature);
int dvmDbgGetObjectTag(ObjectId objectId, const char* type);
int dvmDbgGetTagWidth(int tag);

int dvmDbgGetArrayLength(ObjectId arrayId);
int dvmDbgGetArrayElementTag(ObjectId arrayId);
bool dvmDbgOutputArray(ObjectId arrayId, int firstIndex, int count,
    ExpandBuf* pReply);
bool dvmDbgSetArrayElements(ObjectId arrayId, int firstIndex, int count,
    const u1* buf);

ObjectId dvmDbgCreateString(const char* str);
ObjectId dvmDbgCreateObject(RefTypeId classId);
ObjectId dvmDbgCreateArrayObject(RefTypeId arrayTypeId, u4 length);

bool dvmDbgMatchType(RefTypeId instClassId, RefTypeId classId);

/*
 * Method and Field
 */
const char* dvmDbgGetMethodName(RefTypeId refTypeId, MethodId id);
void dvmDbgOutputAllFields(RefTypeId refTypeId, bool withGeneric,
    ExpandBuf* pReply);
void dvmDbgOutputAllMethods(RefTypeId refTypeId, bool withGeneric,
    ExpandBuf* pReply);
void dvmDbgOutputAllInterfaces(RefTypeId refTypeId, ExpandBuf* pReply);
void dvmDbgOutputLineTable(RefTypeId refTypeId, MethodId methodId,
    ExpandBuf* pReply);
void dvmDbgOutputVariableTable(RefTypeId refTypeId, MethodId id,
    bool withGeneric, ExpandBuf* pReply);

int dvmDbgGetFieldTag(ObjectId objId, FieldId fieldId);
int dvmDbgGetStaticFieldTag(RefTypeId refTypeId, FieldId fieldId);
void dvmDbgGetFieldValue(ObjectId objId, FieldId fieldId, u1* ptr, int width);
void dvmDbgSetFieldValue(ObjectId objectId, FieldId fieldId, u8 value,
    int width);
void dvmDbgGetStaticFieldValue(RefTypeId refTypeId, FieldId fieldId, u1* ptr,
    int width);
void dvmDbgSetStaticFieldValue(RefTypeId refTypeId, FieldId fieldId,
    u8 rawValue, int width);

char* dvmDbgStringToUtf8(ObjectId strId);

/*
 * Thread, ThreadGroup, Frame
 */
char* dvmDbgGetThreadName(ObjectId threadId);
ObjectId dvmDbgGetThreadGroup(ObjectId threadId);
char* dvmDbgGetThreadGroupName(ObjectId threadGroupId);
ObjectId dvmDbgGetThreadGroupParent(ObjectId threadGroupId);
ObjectId dvmDbgGetSystemThreadGroupId(void);
ObjectId dvmDbgGetMainThreadGroupId(void);

bool dvmDbgGetThreadStatus(ObjectId threadId, u4* threadStatus,
    u4* suspendStatus);
u4 dvmDbgGetThreadSuspendCount(ObjectId threadId);
bool dvmDbgThreadExists(ObjectId threadId);
bool dvmDbgIsSuspended(ObjectId threadId);
//void dvmDbgWaitForSuspend(ObjectId threadId);
void dvmDbgGetThreadGroupThreads(ObjectId threadGroupId,
    ObjectId** ppThreadIds, u4* pThreadCount);
void dvmDbgGetAllThreads(ObjectId** ppThreadIds, u4* pThreadCount);
int dvmDbgGetThreadFrameCount(ObjectId threadId);
bool dvmDbgGetThreadFrame(ObjectId threadId, int num, FrameId* pFrameId,
    JdwpLocation* pLoc);

ObjectId dvmDbgGetThreadSelfId(void);
void dvmDbgSuspendVM(bool isEvent);
void dvmDbgResumeVM(void);
void dvmDbgSuspendThread(ObjectId threadId);
void dvmDbgResumeThread(ObjectId threadId);
void dvmDbgSuspendSelf(void);

bool dvmDbgGetThisObject(ObjectId threadId, FrameId frameId, ObjectId* pThisId);
void dvmDbgGetLocalValue(ObjectId threadId, FrameId frameId, int slot,
    u1 tag, u1* buf, int expectedLen);
void dvmDbgSetLocalValue(ObjectId threadId, FrameId frameId, int slot,
    u1 tag, u8 value, int width);


/*
 * Debugger notification
 */
void dvmDbgPostLocationEvent(const struct Method* method, int pcOffset,
    struct Object* thisPtr, int eventFlags);
void dvmDbgPostException(void* throwFp, int throwRelPc, void* catchFp,
    int catchRelPc, struct Object* exception);
void dvmDbgPostThreadStart(struct Thread* thread);
void dvmDbgPostThreadDeath(struct Thread* thread);
void dvmDbgPostClassPrepare(struct ClassObject* clazz);
// FieldAccess, FieldModification

/* for "eventFlags" */
enum {
    DBG_BREAKPOINT      = 0x01,
    DBG_SINGLE_STEP     = 0x02,
    DBG_METHOD_ENTRY    = 0x04,
    DBG_METHOD_EXIT     = 0x08,
};

bool dvmDbgWatchLocation(const JdwpLocation* pLoc);
void dvmDbgUnwatchLocation(const JdwpLocation* pLoc);
bool dvmDbgConfigureStep(ObjectId threadId, enum JdwpStepSize size,
    enum JdwpStepDepth depth);
void dvmDbgUnconfigureStep(ObjectId threadId);

JdwpError dvmDbgInvokeMethod(ObjectId threadId, ObjectId objectId,
    RefTypeId classId, MethodId methodId, u4 numArgs, u8* argArray,
    u4 options, u1* pResultTag, u8* pResultValue, ObjectId* pExceptObj);
void dvmDbgExecuteMethod(DebugInvokeReq* pReq);

/* Make an AddressSet for a line, for single stepping */
const AddressSet *dvmAddressSetForLine(const struct Method* method, int line);

/* perform "late registration" of an object ID */
void dvmDbgRegisterObjectId(ObjectId id);

/*
 * DDM support.
 */
bool dvmDbgDdmHandlePacket(const u1* buf, int dataLen, u1** pReplyBuf,
    int* pReplyLen);
void dvmDbgDdmConnected(void);
void dvmDbgDdmDisconnected(void);
void dvmDbgDdmSendChunk(int type, size_t len, const u1* buf);
void dvmDbgDdmSendChunkV(int type, const struct iovec* iov, int iovcnt);

#define CHUNK_TYPE(_name) \
    ((_name)[0] << 24 | (_name)[1] << 16 | (_name)[2] << 8 | (_name)[3])

#endif /*_DALVIK_DEBUGGER*/
