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
 * System thread support.
 */
#ifndef _DALVIK_SYSTEM_THREAD
#define _DALVIK_SYSTEM_THREAD

#include "Thread.h"

/*
 * Queries the system thread status for the given thread. If the thread is in
 * native code, this function queries the system thread state and converts it
 * to an equivalent ThreadStatus. If the system thread state is "paging",
 * this function returns THREAD_WAIT. If the native status can't be read,
 * this function returns THREAD_NATIVE. The thread list lock should be held
 * when calling this function.
 */
ThreadStatus dvmGetSystemThreadStatus(Thread* thread);

/*
 * Frees system thread-specific state in the given thread. The thread list
 * lock is *not* held when this function is invoked.
 */
void dvmDetachSystemThread(Thread* thread);

#endif /*_DALVIK_SYSTEM_THREAD*/
