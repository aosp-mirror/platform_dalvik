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
 * Exception handling.
 */
#ifndef _DALVIK_EXCEPTION
#define _DALVIK_EXCEPTION

/* initialization */
bool dvmExceptionStartup(void);
void dvmExceptionShutdown(void);

/*
 * Throw an exception in the current thread, by class descriptor.
 */
void dvmThrowChainedException(const char* exceptionDescriptor, const char* msg,
    Object* cause);
INLINE void dvmThrowException(const char* exceptionDescriptor,
    const char* msg)
{
    dvmThrowChainedException(exceptionDescriptor, msg, NULL);
}

/*
 * Like dvmThrowChainedException, but takes printf-style args for the message.
 */
void dvmThrowExceptionFmtV(const char* exceptionDescriptor, const char* fmt,
    va_list args);
void dvmThrowExceptionFmt(const char* exceptionDescriptor, const char* fmt, ...)
#if defined(__GNUC__)
    __attribute__ ((format(printf, 2, 3)))
#endif
    ;
INLINE void dvmThrowExceptionFmt(const char* exceptionDescriptor,
    const char* fmt, ...)
{
    va_list args;
    va_start(args, fmt);
    dvmThrowExceptionFmtV(exceptionDescriptor, fmt, args);
    va_end(args);
}

/*
 * Throw an exception in the current thread, by class object.
 */
void dvmThrowChainedExceptionByClass(ClassObject* exceptionClass,
    const char* msg, Object* cause);
INLINE void dvmThrowExceptionByClass(ClassObject* exceptionClass,
    const char* msg)
{
    dvmThrowChainedExceptionByClass(exceptionClass, msg, NULL);
}

/*
 * Throw the named exception using the name of a class as the exception
 * message.
 */
void dvmThrowChainedExceptionWithClassMessage(const char* exceptionDescriptor,
    const char* messageDescriptor, Object* cause);
INLINE void dvmThrowExceptionWithClassMessage(const char* exceptionDescriptor,
    const char* messageDescriptor)
{
    dvmThrowChainedExceptionWithClassMessage(exceptionDescriptor,
        messageDescriptor, NULL);
}

/*
 * Like dvmThrowExceptionWithMessageFromDescriptor, but take a
 * class object instead of a name.
 */
void dvmThrowExceptionByClassWithClassMessage(ClassObject* exceptionClass,
    const char* messageDescriptor);

/*
 * Return the exception being thrown in the current thread, or NULL if
 * no exception is pending.
 */
INLINE Object* dvmGetException(Thread* self) {
    return self->exception;
}

/*
 * Set the exception being thrown in the current thread.
 */
INLINE void dvmSetException(Thread* self, Object* exception)
{
    assert(exception != NULL);
    self->exception = exception;
}

/*
 * Clear the pending exception.
 *
 * (We use this rather than "set(null)" because we may need to have special
 * fixups here for StackOverflowError stuff.  Calling "clear" in the code
 * makes it obvious.)
 */
INLINE void dvmClearException(Thread* self) {
    self->exception = NULL;
}

/*
 * Clear the pending exception.  Used by the optimization and verification
 * code, which has to run with "initializing" set to avoid going into a
 * death-spin if the "class not found" exception can't be found.
 */
void dvmClearOptException(Thread* self);

/*
 * Returns "true" if an exception is pending.  Use this if you have a
 * "self" pointer.
 */
INLINE bool dvmCheckException(Thread* self) {
    return (self->exception != NULL);
}

/*
 * Returns "true" if this is a "checked" exception, i.e. it's a subclass
 * of Throwable (assumed) but not a subclass of RuntimeException or Error.
 */
bool dvmIsCheckedException(const Object* exception);

/*
 * Wrap the now-pending exception in a different exception.
 *
 * If something fails, an (unchecked) exception related to that failure
 * will be pending instead.
 */
void dvmWrapException(const char* newExcepStr);

/*
 * Get the "cause" field from an exception.
 *
 * Returns NULL if the field is null or uninitialized.
 */
Object* dvmGetExceptionCause(const Object* exception);

/*
 * Print the exception stack trace on stderr.  Calls the exception's
 * print function.
 */
void dvmPrintExceptionStackTrace(void);

/*
 * Print the exception stack trace to the log file.  The exception stack
 * trace is computed within the VM.
 */
void dvmLogExceptionStackTrace(void);

/*
 * Search for a catch block that matches "exception".
 *
 * "*newFrame" gets a copy of the new frame pointer.
 *
 * If "doUnroll" is set, we unroll "thread"s stack as we go (and update
 * self->curFrame with the same value as in *newFrame).
 *
 * Returns the offset to the catch code on success, or -1 if we couldn't
 * find a catcher.
 */
int dvmFindCatchBlock(Thread* self, int relPc, Object* exception,
    bool doUnroll, void** newFrame);

/*
 * Support for saving exception stack traces and converting them to
 * usable form.  Use the "FillIn" function to generate a compact array
 * that represents the stack frames, then "GetStackTrace" to convert it
 * to an array of StackTraceElement objects.
 *
 * Don't call the "Internal" form of the function directly.
 */
void* dvmFillInStackTraceInternal(Thread* thread, bool wantObject, int* pCount);
/* return an [I for use by interpreted code */
INLINE Object* dvmFillInStackTrace(Thread* thread) {
    return (Object*) dvmFillInStackTraceInternal(thread, true, NULL);
}
ArrayObject* dvmGetStackTrace(const Object* stackState);
/* return an int* and array count; caller must free() the return value */
INLINE int* dvmFillInStackTraceRaw(Thread* thread, int* pCount) {
    return (int*) dvmFillInStackTraceInternal(thread, false, pCount);
}
ArrayObject* dvmGetStackTraceRaw(const int* intVals, int stackDepth);

/*
 * Print a formatted version of a raw stack trace to the log file.
 */
void dvmLogRawStackTrace(const int* intVals, int stackDepth);

#endif /*_DALVIK_EXCEPTION*/
