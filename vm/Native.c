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
 * Native method resolution.
 *
 * Currently the "Dalvik native" methods are only used for internal methods.
 * Someday we may want to export the interface as a faster but riskier
 * alternative to JNI.
 */
#include "Dalvik.h"

#include <stdlib.h>
#include <dlfcn.h>

static void freeSharedLibEntry(void* ptr);
static void* lookupSharedLibMethod(const Method* method);


/*
 * Initialize the native code loader.
 */
bool dvmNativeStartup(void)
{
    gDvm.nativeLibs = dvmHashTableCreate(4, freeSharedLibEntry);
    if (gDvm.nativeLibs == NULL)
        return false;

    return true;
}

/*
 * Free up our tables.
 */
void dvmNativeShutdown(void)
{
    dvmHashTableFree(gDvm.nativeLibs);
    gDvm.nativeLibs = NULL;
}


/*
 * Resolve a native method and invoke it.
 *
 * This is executed as if it were a native bridge or function.  If the
 * resolution succeeds, method->insns is replaced, and we don't go through
 * here again.
 *
 * Initializes method's class if necessary.
 *
 * An exception is thrown on resolution failure.
 *
 * (This should not be taking "const Method*", because it modifies the
 * structure, but the declaration needs to match the DalvikBridgeFunc
 * type definition.)
 */
void dvmResolveNativeMethod(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    ClassObject* clazz = method->clazz;
    void* func;

    /*
     * If this is a static method, it could be called before the class
     * has been initialized.
     */
    if (dvmIsStaticMethod(method)) {
        if (!dvmIsClassInitialized(clazz) && !dvmInitClass(clazz)) {
            assert(dvmCheckException(dvmThreadSelf()));
            return;
        }
    } else {
        assert(dvmIsClassInitialized(clazz) ||
               dvmIsClassInitializing(clazz));
    }

    /* start with our internal-native methods */
    func = dvmLookupInternalNativeMethod(method);
    if (func != NULL) {
        /* resolution always gets the same answer, so no race here */
        IF_LOGVV() {
            char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
            LOGVV("+++ resolved native %s.%s %s, invoking\n",
                clazz->descriptor, method->name, desc);
            free(desc);
        }
        if (dvmIsSynchronizedMethod(method)) {
            LOGE("ERROR: internal-native can't be declared 'synchronized'\n");
            LOGE("Failing on %s.%s\n", method->clazz->descriptor, method->name);
            dvmAbort();     // harsh, but this is VM-internal problem
        }
        DalvikBridgeFunc dfunc = (DalvikBridgeFunc) func;
        dvmSetNativeFunc(method, dfunc, NULL);
        assert(method->insns == NULL);
        dfunc(args, pResult, method, self);
        return;
    }

    /* now scan any DLLs we have loaded for JNI signatures */
    func = lookupSharedLibMethod(method);
    if (func != NULL) {
        /* found it, point it at the JNI bridge and then call it */
        dvmUseJNIBridge((Method*) method, func);
        dvmCallJNIMethod(args, pResult, method, self);
        return;
    }

    IF_LOGW() {
        char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
        LOGW("No implementation found for native %s.%s %s\n",
            clazz->descriptor, method->name, desc);
        free(desc);
    }

    dvmThrowException("Ljava/lang/UnsatisfiedLinkError;", method->name);
}


/*
 * ===========================================================================
 *      Native shared library support
 * ===========================================================================
 */

// TODO? if a ClassLoader is unloaded, we need to unload all DLLs that
// are associated with it.  (Or not -- can't determine if native code
// is still using parts of it.)

/*
 * We add one of these to the hash table for every library we load.  The
 * hash is on the "pathName" field.
 */
typedef struct SharedLib {
    char*       pathName;       /* absolute path to library */
    void*       handle;         /* from dlopen */
    Object*     classLoader;    /* ClassLoader we are associated with */
} SharedLib;

/*
 * (This is a dvmHashTableLookup callback.)
 *
 * Find an entry that matches the string.
 */
static int hashcmpNameStr(const void* ventry, const void* vname)
{
    const SharedLib* pLib = (const SharedLib*) ventry;
    const char* name = (const char*) vname;

    return strcmp(pLib->pathName, name);
}

/*
 * (This is a dvmHashTableLookup callback.)
 *
 * Find an entry that matches the new entry.
 */
static int hashcmpSharedLib(const void* ventry, const void* vnewEntry)
{
    const SharedLib* pLib = (const SharedLib*) ventry;
    const SharedLib* pNewLib = (const SharedLib*) vnewEntry;

    LOGD("--- comparing %p '%s' %p '%s'\n",
        pLib, pLib->pathName, pNewLib, pNewLib->pathName);
    return strcmp(pLib->pathName, pNewLib->pathName);
}

/*
 * Check to see if an entry with the same pathname already exists.
 */
static const SharedLib* findSharedLibEntry(const char* pathName)
{
    u4 hash = dvmComputeUtf8Hash(pathName);
    void* ent;

    ent = dvmHashTableLookup(gDvm.nativeLibs, hash, (void*)pathName,
                hashcmpNameStr, false);
    return ent;
}

/*
 * Add the new entry to the table.
 *
 * Returns "true" on success, "false" if the entry already exists.
 */
static bool addSharedLibEntry(SharedLib* pLib)
{
    u4 hash = dvmComputeUtf8Hash(pLib->pathName);
    void* ent;

    /*
     * Do the lookup with the "add" flag set.  If we add it, we will get
     * our own pointer back.  If somebody beat us to the punch, we'll get
     * their pointer back instead.
     */
    ent = dvmHashTableLookup(gDvm.nativeLibs, hash, pLib, hashcmpSharedLib,
                true);
    return (ent == pLib);
}

/*
 * Free up an entry.  (This is a dvmHashTableFree callback.)
 */
static void freeSharedLibEntry(void* ptr)
{
    SharedLib* pLib = (SharedLib*) ptr;

    /*
     * Calling dlclose() here is somewhat dangerous, because it's possible
     * that a thread outside the VM is still accessing the code we loaded.
     */
    if (false)
        dlclose(pLib->handle);
    free(pLib->pathName);
    free(pLib);
}

/*
 * Convert library name to system-dependent form, e.g. "jpeg" becomes
 * "libjpeg.so".
 *
 * (Should we have this take buffer+len and avoid the alloc?  It gets
 * called very rarely.)
 */
char* dvmCreateSystemLibraryName(char* libName)
{
    char buf[256];
    int len;

    len = snprintf(buf, sizeof(buf), OS_SHARED_LIB_FORMAT_STR, libName);
    if (len >= (int) sizeof(buf))
        return NULL;
    else
        return strdup(buf);
}


#if 0
/*
 * Find a library, given the lib's system-dependent name (e.g. "libjpeg.so").
 *
 * We need to search through the path defined by the java.library.path
 * property.
 *
 * Returns NULL if the library was not found.
 */
static char* findLibrary(const char* libSysName)
{
    char* javaLibraryPath = NULL;
    char* testName = NULL;
    char* start;
    char* cp;
    bool done;

    javaLibraryPath = dvmGetProperty("java.library.path");
    if (javaLibraryPath == NULL)
        goto bail;

    LOGVV("+++ path is '%s'\n", javaLibraryPath);

    start = cp = javaLibraryPath;
    while (cp != NULL) {
        char pathBuf[256];
        int len;

        cp = strchr(start, ':');
        if (cp != NULL)
            *cp = '\0';

        len = snprintf(pathBuf, sizeof(pathBuf), "%s/%s", start, libSysName);
        if (len >= (int) sizeof(pathBuf)) {
            LOGW("Path overflowed %d bytes: '%s' / '%s'\n",
                len, start, libSysName);
            /* keep going, next one might fit */
        } else {
            LOGVV("+++  trying '%s'\n", pathBuf);
            if (access(pathBuf, R_OK) == 0) {
                testName = strdup(pathBuf);
                break;
            }
        }

        start = cp +1;
    }

bail:
    free(javaLibraryPath);
    return testName;
}

/*
 * Load a native shared library, given the system-independent piece of
 * the library name.
 *
 * Throws an exception on failure.
 */
void dvmLoadNativeLibrary(StringObject* libNameObj, Object* classLoader)
{
    char* libName = NULL;
    char* libSysName = NULL;
    char* libPath = NULL;

    /*
     * If "classLoader" isn't NULL, call the class loader's "findLibrary"
     * method with the lib name.  If it returns a non-NULL result, we use
     * that as the pathname.
     */
    if (classLoader != NULL) {
        Method* findLibrary;
        Object* findLibResult;

        findLibrary = dvmFindVirtualMethodByDescriptor(classLoader->clazz,
            "findLibrary", "(Ljava/lang/String;)Ljava/lang/String;");
        if (findLibrary == NULL) {
            LOGW("Could not find findLibrary() in %s\n",
                classLoader->clazz->name);
            dvmThrowException("Ljava/lang/UnsatisfiedLinkError;",
                "findLibrary");
            goto bail;
        }

        findLibResult = (Object*)(u4) dvmCallMethod(findLibrary, classLoader,
                                            libNameObj);
        if (dvmCheckException()) {
            LOGV("returning early on exception\n");
            goto bail;
        }
        if (findLibResult != NULL) {
            /* success! */
            libPath = dvmCreateCstrFromString(libNameObj);
            LOGI("Found library through CL: '%s'\n", libPath);
            dvmLoadNativeCode(libPath, classLoader);
            goto bail;
        }
    }

    libName = dvmCreateCstrFromString(libNameObj);
    if (libName == NULL)
        goto bail;
    libSysName = dvmCreateSystemLibraryName(libName);
    if (libSysName == NULL)
        goto bail;

    libPath = findLibrary(libSysName);
    if (libPath != NULL) {
        LOGD("Found library through path: '%s'\n", libPath);
        dvmLoadNativeCode(libPath, classLoader);
    } else {
        LOGW("Unable to locate shared lib matching '%s'\n", libSysName);
        dvmThrowException("Ljava/lang/UnsatisfiedLinkError;", libName);
    }

bail:
    free(libName);
    free(libSysName);
    free(libPath);
}
#endif

typedef int (*OnLoadFunc)(JavaVM*, void*);

/*
 * Load native code from the specified absolute pathname.  Per the spec,
 * if we've already loaded a library with the specified pathname, we
 * return without doing anything.
 *
 * TODO? for better results we should absolutify the pathname.  For fully
 * correct results we should stat to get the inode and compare that.  The
 * existing implementation is fine so long as everybody is using
 * System.loadLibrary.
 *
 * The library will be associated with the specified class loader.  The JNI
 * spec says we can't load the same library into more than one class loader.
 *
 * Returns "true" on success.
 */
bool dvmLoadNativeCode(const char* pathName, Object* classLoader)
{
    const SharedLib* pEntry;
    void* handle;

    LOGD("Trying to load lib %s %p\n", pathName, classLoader);

    /*
     * See if we've already loaded it.  If we have, and the class loader
     * matches, return successfully without doing anything.
     */
    pEntry = findSharedLibEntry(pathName);
    if (pEntry != NULL) {
        if (pEntry->classLoader != classLoader) {
            LOGW("Shared lib '%s' already opened by CL %p; can't open in %p\n",
                pathName, pEntry->classLoader, classLoader);
            return false;
        }
        LOGD("Shared lib '%s' already loaded in same CL %p\n",
            pathName, classLoader);
        return true;
    }

    /*
     * Open the shared library.  Because we're using a full path, the system
     * doesn't have to search through LD_LIBRARY_PATH.  (It may do so to
     * resolve this library's dependencies though.)
     *
     * Failures here are expected when java.library.path has several entries
     * and we have to hunt for the lib.
     *
     * The current android-arm dynamic linker implementation tends to
     * return "Cannot find library" from dlerror() regardless of the actual
     * problem.  A more useful diagnostic may be sent to stdout/stderr if
     * linker diagnostics are enabled, but that's not usually visible in
     * Android apps.  Some things to try:
     *   - make sure the library exists on the device
     *   - verify that the right path is being opened (the debug log message
     *     above can help with that)
     *   - check to see if the library is valid (e.g. not zero bytes long)
     *   - check config/prelink-linux-arm.map to ensure that the library
     *     is listed and is not being overrun by the previous entry (if
     *     loading suddenly stops working on a prelinked library, this is
     *     a good one to check)
     *   - write a trivial app that calls sleep() then dlopen(), attach
     *     to it with "strace -p <pid>" while it sleeps, and watch for
     *     attempts to open nonexistent dependent shared libs
     */
    handle = dlopen(pathName, RTLD_LAZY);
    if (handle == NULL) {
        LOGI("Unable to dlopen(%s): %s\n", pathName, dlerror());
        return false;
    }

    SharedLib* pNewEntry;
    pNewEntry = (SharedLib*) malloc(sizeof(SharedLib));
    pNewEntry->pathName = strdup(pathName);
    pNewEntry->handle = handle;
    pNewEntry->classLoader = classLoader;
    if (!addSharedLibEntry(pNewEntry)) {
        LOGI("WOW: we lost a race to add a shared lib (%s %p)\n",
            pathName, classLoader);
        /* free up our entry, and just return happy that one exists */
        freeSharedLibEntry(pNewEntry);
    } else {
        LOGD("Added shared lib %s %p\n", pathName, classLoader);

        void* vonLoad;
        int version;

        vonLoad = dlsym(handle, "JNI_OnLoad");
        if (vonLoad == NULL) {
            LOGD("No JNI_OnLoad found in %s %p\n", pathName, classLoader);
        } else {
            /*
             * Call JNI_OnLoad.  We have to override the current class
             * loader, which will always be "null" since the stuff at the
             * top of the stack is around Runtime.loadLibrary().
             */
            OnLoadFunc func = vonLoad;
            Thread* self = dvmThreadSelf();
            Object* prevOverride = self->classLoaderOverride;

            self->classLoaderOverride = classLoader;
            dvmChangeStatus(NULL, THREAD_NATIVE);
            version = (*func)(gDvm.vmList, NULL);
            dvmChangeStatus(NULL, THREAD_RUNNING);
            self->classLoaderOverride = prevOverride;

            if (version != JNI_VERSION_1_2 && version != JNI_VERSION_1_4 &&
                version != JNI_VERSION_1_6)
            {
                LOGW("JNI_OnLoad returned bad version (%d) in %s %p\n",
                    version, pathName, classLoader);
                // TODO: dlclose, remove hash table entry
                return false;
            }
        }
    }

    return true;
}


/*
 * ===========================================================================
 *      Signature-based method lookup
 * ===========================================================================
 */

/*
 * Create the pre-mangled form of the class+method string.
 *
 * Returns a newly-allocated string, and sets "*pLen" to the length.
 */
static char* createJniNameString(const char* classDescriptor,
    const char* methodName, int* pLen)
{
    char* result;
    size_t descriptorLength = strlen(classDescriptor);

    *pLen = 4 + descriptorLength + strlen(methodName);

    result = malloc(*pLen +1);
    if (result == NULL)
        return NULL;

    /*
     * Add one to classDescriptor to skip the "L", and then replace
     * the final ";" with a "/" after the sprintf() call.
     */
    sprintf(result, "Java/%s%s", classDescriptor + 1, methodName);
    result[5 + (descriptorLength - 2)] = '/';

    return result;
}

/*
 * Returns a newly-allocated, mangled copy of "str".
 *
 * "str" is a "modified UTF-8" string.  We convert it to UTF-16 first to
 * make life simpler.
 */
static char* mangleString(const char* str, int len)
{
    u2* utf16 = NULL;
    char* mangle = NULL;
    int charLen;

    //LOGI("mangling '%s' %d\n", str, len);

    assert(str[len] == '\0');

    charLen = dvmUtf8Len(str);
    utf16 = (u2*) malloc(sizeof(u2) * charLen);
    if (utf16 == NULL)
        goto bail;

    dvmConvertUtf8ToUtf16(utf16, str);

    /*
     * Compute the length of the mangled string.
     */
    int i, mangleLen = 0;

    for (i = 0; i < charLen; i++) {
        u2 ch = utf16[i];

        if (ch > 127) {
            mangleLen += 6;
        } else {
            switch (ch) {
            case '_':
            case ';':
            case '[':
                mangleLen += 2;
                break;
            default:
                mangleLen++;
                break;
            }
        }
    }

    char* cp;

    mangle = (char*) malloc(mangleLen +1);
    if (mangle == NULL)
        goto bail;

    for (i = 0, cp = mangle; i < charLen; i++) {
        u2 ch = utf16[i];

        if (ch > 127) {
            sprintf(cp, "_0%04x", ch);
            cp += 6;
        } else {
            switch (ch) {
            case '_':
                *cp++ = '_';
                *cp++ = '1';
                break;
            case ';':
                *cp++ = '_';
                *cp++ = '2';
                break;
            case '[':
                *cp++ = '_';
                *cp++ = '3';
                break;
            case '/':
                *cp++ = '_';
                break;
            default:
                *cp++ = (char) ch;
                break;
            }
        }
    }

    *cp = '\0';

bail:
    free(utf16);
    return mangle;
}

/*
 * Create the mangled form of the parameter types.
 */
static char* createMangledSignature(const DexProto* proto)
{
    DexStringCache sigCache;
    const char* interim;
    char* result;

    dexStringCacheInit(&sigCache);
    interim = dexProtoGetParameterDescriptors(proto, &sigCache);
    result = mangleString(interim, strlen(interim));
    dexStringCacheRelease(&sigCache);

    return result;
}

/*
 * (This is a dvmHashForeach callback.)
 *
 * Search for a matching method in this shared library.
 */
static int findMethodInLib(void* vlib, void* vmethod)
{
    const SharedLib* pLib = (const SharedLib*) vlib;
    const Method* meth = (const Method*) vmethod;
    char* preMangleCM = NULL;
    char* mangleCM = NULL;
    char* mangleSig = NULL;
    char* mangleCMSig = NULL;
    void* func = NULL;
    int len;

    if (meth->clazz->classLoader != pLib->classLoader) {
        LOGD("+++ not scanning '%s' for '%s' (wrong CL)\n",
            pLib->pathName, meth->name);
        return 0;
    } else
        LOGV("+++ scanning '%s' for '%s'\n", pLib->pathName, meth->name);

    /*
     * First, we try it without the signature.
     */
    preMangleCM =
        createJniNameString(meth->clazz->descriptor, meth->name, &len);
    if (preMangleCM == NULL)
        goto bail;

    mangleCM = mangleString(preMangleCM, len);
    if (mangleCM == NULL)
        goto bail;

    LOGV("+++ calling dlsym(%s)\n", mangleCM);
    func = dlsym(pLib->handle, mangleCM);
    if (func == NULL) {
        mangleSig =
            createMangledSignature(&meth->prototype);
        if (mangleSig == NULL)
            goto bail;

        mangleCMSig = (char*) malloc(strlen(mangleCM) + strlen(mangleSig) +3);
        if (mangleCMSig == NULL)
            goto bail;

        sprintf(mangleCMSig, "%s__%s", mangleCM, mangleSig);

        LOGV("+++ calling dlsym(%s)\n", mangleCMSig);
        func = dlsym(pLib->handle, mangleCMSig);
        if (func != NULL) {
            LOGV("Found '%s' with dlsym\n", mangleCMSig);
        }
    } else {
        LOGV("Found '%s' with dlsym\n", mangleCM);
    }

bail:
    free(preMangleCM);
    free(mangleCM);
    free(mangleSig);
    free(mangleCMSig);
    return (int) func;
}

/*
 * See if the requested method lives in any of the currently-loaded
 * shared libraries.  We do this by checking each of them for the expected
 * method signature.
 */
static void* lookupSharedLibMethod(const Method* method)
{
    if (gDvm.nativeLibs == NULL) {
        LOGE("Unexpected init state: nativeLibs not ready\n");
        dvmAbort();
    }
    return (void*) dvmHashForeach(gDvm.nativeLibs, findMethodInLib,
        (void*) method);
}

