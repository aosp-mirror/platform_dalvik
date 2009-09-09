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

#define LOG_TAG "SystemThread"

/*
 * System thread support.
 */
#include "Dalvik.h"
#include "native/SystemThread.h"

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>

struct SystemThread {
    /*
     * /proc/PID/task/TID/stat. -1 if not opened yet. -2 indicates an error
     * occurred while opening the file.
     */
    int statFile;

    /* Offset of state char in stat file, last we checked. */
    int stateOffset;
};

void dvmDetachSystemThread(Thread* thread) {
    if (thread->systemThread != NULL) {
        if (thread->systemThread->statFile > -1) {
            close(thread->systemThread->statFile);
        }
        free(thread->systemThread);
        thread->systemThread = NULL;
    }
}

/* Converts a Linux thread state to a ThreadStatus. */
static ThreadStatus stateToStatus(char state) {
    switch (state) {
        case 'R': return THREAD_RUNNING;    // running
        case 'S': return THREAD_WAIT;       // sleeping in interruptible wait
        case 'D': return THREAD_WAIT;       // uninterruptible disk sleep
        case 'Z': return THREAD_ZOMBIE;     // zombie
        case 'T': return THREAD_WAIT;       // traced or stopped on a signal
        case 'W': return THREAD_WAIT;  // paging memory
        default:
            LOGE("Unexpected state: %c", state);
            return THREAD_NATIVE;
    }
}

/* Reads the state char starting from the beginning of the file. */
static char readStateFromBeginning(SystemThread* thread) {
    char buffer[256];
    int size = read(thread->statFile, buffer, sizeof(buffer) - 1);
    if (size <= 0) {
        LOGE("read() returned %d: %s", size, strerror(errno));
        return 0;
    }
    char* endOfName = (char*) memchr(buffer, ')', size);
    if (endOfName == NULL) {
        LOGE("End of executable name not found.");
        return 0;
    }
    char* state = endOfName + 2;
    if ((state - buffer) + 1 > size) {
        LOGE("Unexpected EOF while trying to read stat file.");
        return 0;
    }
    thread->stateOffset = state - buffer;
    return *state;
}

/*
 * Looks for the state char at the last place we found it. Read from the
 * beginning if necessary.
 */
static char readStateRelatively(SystemThread* thread) {
    char buffer[3];
    // Position file offset at end of executable name.
    int result = lseek(thread->statFile, thread->stateOffset - 2, SEEK_SET);
    if (result < 0) {
        LOGE("lseek() error.");
        return 0;
    }
    int size = read(thread->statFile, buffer, sizeof(buffer));
    if (size < (int) sizeof(buffer)) {
        LOGE("Unexpected EOF while trying to read stat file.");
        return 0;
    }
    if (buffer[0] != ')') {
        // The executable name must have changed.
        result = lseek(thread->statFile, 0, SEEK_SET);
        if (result < 0) {
            LOGE("lseek() error.");
            return 0;
        }
        return readStateFromBeginning(thread);
    }
    return buffer[2];
}

ThreadStatus dvmGetSystemThreadStatus(Thread* thread) {
    ThreadStatus status = thread->status;
    if (status != THREAD_NATIVE) {
        // Return cached status so we don't accidentally return THREAD_NATIVE.
        return status;
    }

    if (thread->systemThread == NULL) {
        thread->systemThread = (SystemThread*) malloc(sizeof(SystemThread));
        if (thread->systemThread == NULL) {
            LOGE("Couldn't allocate a SystemThread.");
            return THREAD_NATIVE;
        }
        thread->systemThread->statFile = -1;
    }

    SystemThread* systemThread = thread->systemThread;
    if (systemThread->statFile == -2) {
        // We tried and failed to open the file earlier. Return current status.
        return thread->status;
    }

    // Note: see "man proc" for the format of stat.
    // The format is "PID (EXECUTABLE NAME) STATE_CHAR ...".
    // Example: "15 (/foo/bar) R ..."
    char state;
    if (systemThread->statFile == -1) {
        // We haven't tried to open the file yet. Do so.
        char fileName[256];
        sprintf(fileName, "/proc/self/task/%d/stat", thread->systemTid);
        systemThread->statFile = open(fileName, O_RDONLY);
        if (systemThread->statFile == -1) {
            LOGE("Error opening %s: %s", fileName, strerror(errno));
            systemThread->statFile = -2;
            return thread->status;
        }
        state = readStateFromBeginning(systemThread);
    } else {
        state = readStateRelatively(systemThread);
    }

    if (state == 0) {
        close(systemThread->statFile);
        systemThread->statFile = -2;
        return thread->status;
    }
    ThreadStatus nativeStatus = stateToStatus(state);

    // The thread status could have changed from NATIVE.
    status = thread->status;
    return status == THREAD_NATIVE ? nativeStatus : status;
}
