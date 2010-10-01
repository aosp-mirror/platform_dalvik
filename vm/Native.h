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
 * Dalvik's native call interface.
 *
 * You should follow the JNI function naming conventions, but prefix with
 * "Dalvik_" instead of "Java_".
 */
#ifndef _DALVIK_NATIVE
#define _DALVIK_NATIVE

/*
 * Method description; equivalent to a JNI struct.
 */
typedef struct DalvikNativeMethod {
    const char* name;
    const char* signature;
    DalvikNativeFunc  fnPtr;
} DalvikNativeMethod;

/*
 * All methods for one class.  The last "methodInfo" has a NULL "name".
 */
typedef struct DalvikNativeClass {
    const char* classDescriptor;
    const DalvikNativeMethod* methodInfo;
    u4          classDescriptorHash;          /* initialized at runtime */
} DalvikNativeClass;


/* init/shutdown */
bool dvmNativeStartup(void);
void dvmNativeShutdown(void);


/*
 * Convert argc/argv into a function call.  This is platform-specific.
 */
void dvmPlatformInvoke(void* pEnv, ClassObject* clazz, int argInfo, int argc,
    const u4* argv, const char* signature, void* func, JValue* pResult);

/*
 * Generate hints to speed native calls.  This is platform specific.
 */
u4 dvmPlatformInvokeHints(const DexProto* proto);

/*
 * Convert a short library name ("jpeg") to a system-dependent name
 * ("libjpeg.so").  Returns a newly-allocated string.
 */
char* dvmCreateSystemLibraryName(char* libName);
//void dvmLoadNativeLibrary(StringObject* libNameObj, Object* classLoader);
bool dvmLoadNativeCode(const char* fileName, Object* classLoader,
        char** detail);


/*
 * Resolve a native method.  This uses the same prototype as a
 * DalvikBridgeFunc, because it takes the place of the actual function
 * until the first time that it's invoked.
 *
 * Causes the method's class to be initialized.
 *
 * Throws an exception and returns NULL on failure.
 */
void dvmResolveNativeMethod(const u4* args, JValue* pResult,
    const Method* method, struct Thread* self);

/*
 * Unregister all JNI native methods associated with a class.
 */
void dvmUnregisterJNINativeMethods(ClassObject* clazz);

//#define GET_ARG_LONG(_args, _elem)          (*(s8*)(&(_args)[_elem]))
#define GET_ARG_LONG(_args, _elem)          dvmGetArgLong(_args, _elem)

/*
 * Helpful function for accessing long integers in "u4* args".
 *
 * We can't just return *(s8*)(&args[elem]), because that breaks if our
 * architecture requires 64-bit alignment of 64-bit values.
 *
 * Big/little endian shouldn't matter here -- ordering of words within a
 * long seems consistent across our supported platforms.
 */
INLINE s8 dvmGetArgLong(const u4* args, int elem)
{
#if 0
    union { u4 parts[2]; s8 whole; } conv;
    conv.parts[0] = args[elem];
    conv.parts[1] = args[elem+1];
    return conv.whole;
#else
    /* with gcc's optimizer, memcpy() turns into simpler assignments */
    s8 val;
    memcpy(&val, &args[elem], 8);
    return val;
#endif
}

/*
 * Used to implement -Xjnitrace.
 */
struct Thread;
void dvmLogNativeMethodEntry(const Method* method, const u4* newFp);
void dvmLogNativeMethodExit(const Method* method, struct Thread* self,
        const JValue retval);

#endif /*_DALVIK_NATIVE*/
