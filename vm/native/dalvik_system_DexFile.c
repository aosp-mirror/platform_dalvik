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
 * dalvik.system.DexFile
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"


/*
 * Internal struct for managing DexFile.
 */
typedef struct DexOrJar {
    char*       fileName;
    bool        isDex;
    bool        okayToFree;
    RawDexFile* pRawDexFile;
    JarFile*    pJarFile;
} DexOrJar;

/*
 * (This is a dvmHashTableFree callback.)
 */
void dvmFreeDexOrJar(void* vptr)
{
    DexOrJar* pDexOrJar = (DexOrJar*) vptr;

    LOGV("Freeing DexOrJar '%s'\n", pDexOrJar->fileName);

    if (pDexOrJar->isDex)
        dvmRawDexFileFree(pDexOrJar->pRawDexFile);
    else
        dvmJarFileFree(pDexOrJar->pJarFile);
    free(pDexOrJar->fileName);
    free(pDexOrJar);
}

/*
 * (This is a dvmHashTableLookup compare func.)
 *
 * Args are DexOrJar*.
 */
static int hashcmpDexOrJar(const void* tableVal, const void* newVal)
{
    return (int) newVal - (int) tableVal;
}

/*
 * Verify that the "cookie" is a DEX file we opened.
 *
 * Expects that the hash table will be *unlocked* here.
 *
 * If the cookie is invalid, we throw an exception and return "false".
 */
static bool validateCookie(int cookie)
{
    DexOrJar* pDexOrJar = (DexOrJar*) cookie;

    LOGVV("+++ dex verifying cookie %p\n", pDexOrJar);

    if (pDexOrJar == NULL)
        return false;

    u4 hash = dvmComputeUtf8Hash(pDexOrJar->fileName);
    dvmHashTableLock(gDvm.userDexFiles);
    void* result = dvmHashTableLookup(gDvm.userDexFiles, hash, pDexOrJar,
                hashcmpDexOrJar, false);
    dvmHashTableUnlock(gDvm.userDexFiles);
    if (result == NULL) {
        dvmThrowException("Ljava/lang/RuntimeException;",
            "invalid DexFile cookie");
        return false;
    }

    return true;
}

/*
 * private static int openDexFile(String sourceName, String outputName,
 *     int flags) throws IOException
 *
 * Open a DEX file, returning a pointer to our internal data structure.
 *
 * "sourceName" should point to the "source" jar or DEX file.
 *
 * If "outputName" is NULL, the DEX code will automatically find the
 * "optimized" version in the cache directory, creating it if necessary.
 * If it's non-NULL, the specified file will be used instead.
 *
 * TODO: at present we will happily open the same file more than once.
 * To optimize this away we could search for existing entries in the hash
 * table and refCount them.  Requires atomic ops or adding "synchronized"
 * to the non-native code that calls here.
 */
static void Dalvik_dalvik_system_DexFile_openDexFile(const u4* args,
    JValue* pResult)
{
    StringObject* sourceNameObj = (StringObject*) args[0];
    StringObject* outputNameObj = (StringObject*) args[1];
    DexOrJar* pDexOrJar = NULL;
    JarFile* pJarFile;
    RawDexFile* pRawDexFile;
    char* sourceName;
    char* outputName;

    if (sourceNameObj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        RETURN_VOID();
    }

    sourceName = dvmCreateCstrFromString(sourceNameObj);
    if (outputNameObj != NULL)
        outputName = dvmCreateCstrFromString(outputNameObj);
    else
        outputName = NULL;

    /*
     * We have to deal with the possibility that somebody might try to
     * open one of our bootstrap class DEX files.  The set of dependencies
     * will be different, and hence the results of optimization might be
     * different, which means we'd actually need to have two versions of
     * the optimized DEX: one that only knows about part of the boot class
     * path, and one that knows about everything in it.  The latter might
     * optimize field/method accesses based on a class that appeared later
     * in the class path.
     *
     * We can't let the user-defined class loader open it and start using
     * the classes, since the optimized form of the code skips some of
     * the method and field resolution that we would ordinarily do, and
     * we'd have the wrong semantics.
     *
     * We have to reject attempts to manually open a DEX file from the boot
     * class path.  The easiest way to do this is by filename, which works
     * out because variations in name (e.g. "/system/framework/./ext.jar")
     * result in us hitting a different dalvik-cache entry.  It's also fine
     * if the caller specifies their own output file.
     */
    if (dvmClassPathContains(gDvm.bootClassPath, sourceName)) {
        LOGW("Refusing to reopen boot DEX '%s'\n", sourceName);
        dvmThrowException("Ljava/io/IOException;",
            "Re-opening BOOTCLASSPATH DEX files is not allowed");
        free(sourceName);
        RETURN_VOID();
    }

    /*
     * Try to open it directly as a DEX.  If that fails, try it as a Zip
     * with a "classes.dex" inside.
     */
    if (dvmRawDexFileOpen(sourceName, outputName, &pRawDexFile, false) == 0) {
        LOGV("Opening DEX file '%s' (DEX)\n", sourceName);

        pDexOrJar = (DexOrJar*) malloc(sizeof(DexOrJar));
        pDexOrJar->isDex = true;
        pDexOrJar->pRawDexFile = pRawDexFile;
    } else if (dvmJarFileOpen(sourceName, outputName, &pJarFile, false) == 0) {
        LOGV("Opening DEX file '%s' (Jar)\n", sourceName);

        pDexOrJar = (DexOrJar*) malloc(sizeof(DexOrJar));
        pDexOrJar->isDex = false;
        pDexOrJar->pJarFile = pJarFile;
    } else {
        LOGV("Unable to open DEX file '%s'\n", sourceName);
        dvmThrowException("Ljava/io/IOException;", "unable to open DEX file");
    }

    if (pDexOrJar != NULL) {
        pDexOrJar->fileName = sourceName;

        /* add to hash table */
        u4 hash = dvmComputeUtf8Hash(sourceName);
        void* result;
        dvmHashTableLock(gDvm.userDexFiles);
        result = dvmHashTableLookup(gDvm.userDexFiles, hash, pDexOrJar,
                    hashcmpDexOrJar, true);
        dvmHashTableUnlock(gDvm.userDexFiles);
        if (result != pDexOrJar) {
            LOGE("Pointer has already been added?\n");
            dvmAbort();
        }

        pDexOrJar->okayToFree = true;
    } else
        free(sourceName);

    RETURN_PTR(pDexOrJar);
}

/*
 * private static void closeDexFile(int cookie)
 *
 * Release resources associated with a user-loaded DEX file.
 */
static void Dalvik_dalvik_system_DexFile_closeDexFile(const u4* args,
    JValue* pResult)
{
    int cookie = args[0];
    DexOrJar* pDexOrJar = (DexOrJar*) cookie;

    if (pDexOrJar == NULL)
        RETURN_VOID();

    LOGV("Closing DEX file %p (%s)\n", pDexOrJar, pDexOrJar->fileName);

    if (!validateCookie(cookie))
        RETURN_VOID();

    /*
     * We can't just free arbitrary DEX files because they have bits and
     * pieces of loaded classes.  The only exception to this rule is if
     * they were never used to load classes.
     *
     * If we can't free them here, dvmInternalNativeShutdown() will free
     * them when the VM shuts down.
     */
    if (pDexOrJar->okayToFree) {
        u4 hash = dvmComputeUtf8Hash(pDexOrJar->fileName);
        dvmHashTableLock(gDvm.userDexFiles);
        if (!dvmHashTableRemove(gDvm.userDexFiles, hash, pDexOrJar)) {
            LOGW("WARNING: could not remove '%s' from DEX hash table\n",
                pDexOrJar->fileName);
        }
        dvmHashTableUnlock(gDvm.userDexFiles);
        LOGV("+++ freeing DexFile '%s' resources\n", pDexOrJar->fileName);
        dvmFreeDexOrJar(pDexOrJar);
    } else {
        LOGV("+++ NOT freeing DexFile '%s' resources\n", pDexOrJar->fileName);
    }

    RETURN_VOID();
}

/*
 * private static Class defineClass(String name, ClassLoader loader,
 *      int cookie, ProtectionDomain pd)
 *
 * Load a class from a DEX file.  This is roughly equivalent to defineClass()
 * in a regular VM -- it's invoked by the class loader to cause the
 * creation of a specific class.  The difference is that the search for and
 * reading of the bytes is done within the VM.
 *
 * The class name is a "binary name", e.g. "java.lang.String".
 *
 * Returns a null pointer with no exception if the class was not found.
 * Throws an exception on other failures.
 */
static void Dalvik_dalvik_system_DexFile_defineClass(const u4* args,
    JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    Object* loader = (Object*) args[1];
    int cookie = args[2];
    Object* pd = (Object*) args[3];
    ClassObject* clazz = NULL;
    DexOrJar* pDexOrJar = (DexOrJar*) cookie;
    DvmDex* pDvmDex;
    char* name;
    char* descriptor;

    name = dvmCreateCstrFromString(nameObj);
    descriptor = dvmDotToDescriptor(name);
    LOGV("--- Explicit class load '%s' 0x%08x\n", descriptor, cookie);
    free(name);

    if (!validateCookie(cookie))
        RETURN_VOID();

    if (pDexOrJar->isDex)
        pDvmDex = dvmGetRawDexFileDex(pDexOrJar->pRawDexFile);
    else
        pDvmDex = dvmGetJarFileDex(pDexOrJar->pJarFile);

    /* once we load something, we can't unmap the storage */
    pDexOrJar->okayToFree = false;

    clazz = dvmDefineClass(pDvmDex, descriptor, loader);
    Thread* self = dvmThreadSelf();
    if (dvmCheckException(self)) {
        /*
         * If we threw a "class not found" exception, stifle it, since the
         * contract in the higher method says we simply return null if
         * the class is not found.
         */
        Object* excep = dvmGetException(self);
        if (strcmp(excep->clazz->descriptor,
                   "Ljava/lang/ClassNotFoundException;") == 0 ||
            strcmp(excep->clazz->descriptor,
                   "Ljava/lang/NoClassDefFoundError;") == 0)
        {
            dvmClearException(self);
        }
        clazz = NULL;
    }

    /*
     * Set the ProtectionDomain -- do we need this to happen before we
     * link the class and make it available? If so, we need to pass it
     * through dvmDefineClass (and figure out some other
     * stuff, like where it comes from for bootstrap classes).
     */
    if (clazz != NULL) {
        //LOGI("SETTING pd '%s' to %p\n", clazz->descriptor, pd);
        dvmSetFieldObject((Object*) clazz, gDvm.offJavaLangClass_pd, pd);
    }

    free(descriptor);
    RETURN_PTR(clazz);
}

/*
 * private static String[] getClassNameList(int cookie)
 *
 * Returns a String array that holds the names of all classes in the
 * specified DEX file.
 */
static void Dalvik_dalvik_system_DexFile_getClassNameList(const u4* args,
    JValue* pResult)
{
    int cookie = args[0];
    DexOrJar* pDexOrJar = (DexOrJar*) cookie;
    DvmDex* pDvmDex;
    DexFile* pDexFile;
    ArrayObject* stringArray;
    Thread* self = dvmThreadSelf();

    if (!validateCookie(cookie))
        RETURN_VOID();

    if (pDexOrJar->isDex)
        pDvmDex = dvmGetRawDexFileDex(pDexOrJar->pRawDexFile);
    else
        pDvmDex = dvmGetJarFileDex(pDexOrJar->pJarFile);
    assert(pDvmDex != NULL);
    pDexFile = pDvmDex->pDexFile;

    int count = pDexFile->pHeader->classDefsSize;
    stringArray = dvmAllocObjectArray(gDvm.classJavaLangString, count,
                    ALLOC_DEFAULT);
    if (stringArray == NULL) {
        /* probably OOM */
        LOGD("Failed allocating array of %d strings\n", count);
        assert(dvmCheckException(self));
        RETURN_VOID();
    }

    int i;
    for (i = 0; i < count; i++) {
        const DexClassDef* pClassDef = dexGetClassDef(pDexFile, i);
        const char* descriptor =
            dexStringByTypeIdx(pDexFile, pClassDef->classIdx);

        char* className = dvmDescriptorToDot(descriptor);
        StringObject* str = dvmCreateStringFromCstr(className);
        dvmSetObjectArrayElement(stringArray, i, (Object *)str);
        dvmReleaseTrackedAlloc((Object *)str, self);
        free(className);
    }

    dvmReleaseTrackedAlloc((Object*)stringArray, self);
    RETURN_PTR(stringArray);
}

/*
 * public static boolean isDexOptNeeded(String apkName)
 *         throws FileNotFoundException, IOException
 *
 * Returns true if the VM believes that the apk/jar file is out of date
 * and should be passed through "dexopt" again.
 *
 * @param fileName the absolute path to the apk/jar file to examine.
 * @return true if dexopt should be called on the file, false otherwise.
 * @throws java.io.FileNotFoundException if fileName is not readable,
 *         not a file, or not present.
 * @throws java.io.IOException if fileName is not a valid apk/jar file or
 *         if problems occur while parsing it.
 * @throws java.lang.NullPointerException if fileName is null.
 * @throws dalvik.system.StaleDexCacheError if the optimized dex file
 *         is stale but exists on a read-only partition.
 */
static void Dalvik_dalvik_system_DexFile_isDexOptNeeded(const u4* args,
    JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    char* name;
    DexCacheStatus status;
    int result;

    name = dvmCreateCstrFromString(nameObj);
    if (name == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        RETURN_VOID();
    }
    if (access(name, R_OK) != 0) {
        dvmThrowException("Ljava/io/FileNotFoundException;", name);
        free(name);
        RETURN_VOID();
    }
    status = dvmDexCacheStatus(name);
    LOGV("dvmDexCacheStatus(%s) returned %d\n", name, status);

    result = true;
    switch (status) {
    default: //FALLTHROUGH
    case DEX_CACHE_BAD_ARCHIVE:
        dvmThrowException("Ljava/io/IOException;", name);
        result = -1;
        break;
    case DEX_CACHE_OK:
        result = false;
        break;
    case DEX_CACHE_STALE:
        result = true;
        break;
    case DEX_CACHE_STALE_ODEX:
        dvmThrowException("Ldalvik/system/StaleDexCacheError;", name);
        result = -1;
        break;
    }
    free(name);

    if (result >= 0) {
        RETURN_BOOLEAN(result);
    } else {
        RETURN_VOID();
    }
}

const DalvikNativeMethod dvm_dalvik_system_DexFile[] = {
    { "openDexFile",        "(Ljava/lang/String;Ljava/lang/String;I)I",
        Dalvik_dalvik_system_DexFile_openDexFile },
    { "closeDexFile",       "(I)V",
        Dalvik_dalvik_system_DexFile_closeDexFile },
    { "defineClass",        "(Ljava/lang/String;Ljava/lang/ClassLoader;ILjava/security/ProtectionDomain;)Ljava/lang/Class;",
        Dalvik_dalvik_system_DexFile_defineClass },
    { "getClassNameList",   "(I)[Ljava/lang/String;",
        Dalvik_dalvik_system_DexFile_getClassNameList },
    { "isDexOptNeeded",     "(Ljava/lang/String;)Z",
        Dalvik_dalvik_system_DexFile_isDexOptNeeded },
    { NULL, NULL, NULL },
};
