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
 * Perform some simple bytecode optimizations, chiefly "quickening" of
 * opcodes.
 */
#include "Dalvik.h"
#include "libdex/InstrUtils.h"

#include <zlib.h>

#include <stdlib.h>

/*
 * Virtual/direct calls to "method" are replaced with an execute-inline
 * instruction with index "idx".
 */
typedef struct InlineSub {
    Method* method;
    int     inlineIdx;
} InlineSub;


/* fwd */
static bool loadAllClasses(DvmDex* pDvmDex);
static void optimizeLoadedClasses(DexFile* pDexFile);
static void optimizeClass(ClassObject* clazz, const InlineSub* inlineSubs);
static bool optimizeMethod(Method* method, const InlineSub* inlineSubs);
static void rewriteInstField(Method* method, u2* insns, OpCode newOpc);
static bool rewriteVirtualInvoke(Method* method, u2* insns, OpCode newOpc);
static bool rewriteEmptyDirectInvoke(Method* method, u2* insns);
static bool rewriteExecuteInline(Method* method, u2* insns,
    MethodType methodType, const InlineSub* inlineSubs);
static bool rewriteExecuteInlineRange(Method* method, u2* insns,
    MethodType methodType, const InlineSub* inlineSubs);


/*
 * Perform in-place rewrites on a memory-mapped DEX file.
 *
 * This happens in a short-lived child process, so we can go nutty with
 * loading classes and allocating memory.
 */
bool dvmRewriteDex(u1* addr, int len, bool doVerify, bool doOpt,
    u4* pHeaderFlags, DexClassLookup** ppClassLookup)
{
    u8 prepWhen, loadWhen, verifyWhen, optWhen;
    DvmDex* pDvmDex = NULL;
    bool result = false;

    *pHeaderFlags = 0;

    LOGV("+++ swapping bytes\n");
    if (dexFixByteOrdering(addr, len) != 0)
        goto bail;
#if __BYTE_ORDER != __LITTLE_ENDIAN
    *pHeaderFlags |= DEX_OPT_FLAG_BIG;
#endif

    /*
     * Now that the DEX file can be read directly, create a DexFile for it.
     */
    if (dvmDexFileOpenPartial(addr, len, &pDvmDex) != 0) {
        LOGE("Unable to create DexFile\n");
        goto bail;
    }

    /*
     * Create the class lookup table.
     */
    //startWhen = dvmGetRelativeTimeUsec();
    *ppClassLookup = dexCreateClassLookup(pDvmDex->pDexFile);
    if (*ppClassLookup == NULL)
        goto bail;

    /*
     * Bail out early if they don't want The Works.  The current implementation
     * doesn't fork a new process if this flag isn't set, so we really don't
     * want to continue on with the crazy class loading.
     */
    if (!doVerify && !doOpt) {
        result = true;
        goto bail;
    }

    /* this is needed for the next part */
    pDvmDex->pDexFile->pClassLookup = *ppClassLookup;

    prepWhen = dvmGetRelativeTimeUsec();

    /*
     * Load all classes found in this DEX file.  If they fail to load for
     * some reason, they won't get verified (which is as it should be).
     */
    if (!loadAllClasses(pDvmDex))
        goto bail;
    loadWhen = dvmGetRelativeTimeUsec();

    /*
     * Verify all classes in the DEX file.  Export the "is verified" flag
     * to the DEX file we're creating.
     */
    if (doVerify) {
        dvmVerifyAllClasses(pDvmDex->pDexFile);
        *pHeaderFlags |= DEX_FLAG_VERIFIED;
    }
    verifyWhen = dvmGetRelativeTimeUsec();

    /*
     * Optimize the classes we successfully loaded.  If the opt mode is
     * OPTIMIZE_MODE_VERIFIED, each class must have been successfully
     * verified or we'll skip it.
     */
#ifndef PROFILE_FIELD_ACCESS
    if (doOpt) {
        optimizeLoadedClasses(pDvmDex->pDexFile);
        *pHeaderFlags |= DEX_OPT_FLAG_FIELDS | DEX_OPT_FLAG_INVOCATIONS;
    }
#endif
    optWhen = dvmGetRelativeTimeUsec();

    LOGD("DexOpt: load %dms, verify %dms, opt %dms\n",
        (int) (loadWhen - prepWhen) / 1000,
        (int) (verifyWhen - loadWhen) / 1000,
        (int) (optWhen - verifyWhen) / 1000);

    result = true;

bail:
    /* free up storage */
    dvmDexFileFree(pDvmDex);

    return result;
}

/*
 * Try to load all classes in the specified DEX.  If they have some sort
 * of broken dependency, e.g. their superclass lives in a different DEX
 * that wasn't previously loaded into the bootstrap class path, loading
 * will fail.  This is the desired behavior.
 *
 * We have no notion of class loader at this point, so we load all of
 * the classes with the bootstrap class loader.  It turns out this has
 * exactly the behavior we want, and has no ill side effects because we're
 * running in a separate process and anything we load here will be forgotten.
 *
 * We set the CLASS_MULTIPLE_DEFS flag here if we see multiple definitions.
 * This works because we only call here as part of optimization / pre-verify,
 * not during verification as part of loading a class into a running VM.
 *
 * This returns "false" if the world is too screwed up to do anything
 * useful at all.
 */
static bool loadAllClasses(DvmDex* pDvmDex)
{
    u4 count = pDvmDex->pDexFile->pHeader->classDefsSize;
    u4 idx;
    int loaded = 0;

    LOGV("DexOpt: +++ trying to load %d classes\n", count);

    dvmSetBootPathExtraDex(pDvmDex);

    /*
     * We have some circularity issues with Class and Object that are most
     * easily avoided by ensuring that Object is never the first thing we
     * try to find.  Take care of that here.  (We only need to do this when
     * loading classes from the DEX file that contains Object, and only
     * when Object comes first in the list, but it costs very little to
     * do it in all cases.)
     */
    if (dvmFindSystemClass("Ljava/lang/Class;") == NULL) {
        LOGE("ERROR: java.lang.Class does not exist!\n");
        return false;
    }

    for (idx = 0; idx < count; idx++) {
        const DexClassDef* pClassDef;
        const char* classDescriptor;
        ClassObject* newClass;

        pClassDef = dexGetClassDef(pDvmDex->pDexFile, idx);
        classDescriptor =
            dexStringByTypeIdx(pDvmDex->pDexFile, pClassDef->classIdx);

        LOGV("+++  loading '%s'", classDescriptor);
        //newClass = dvmDefineClass(pDexFile, classDescriptor,
        //        NULL);
        newClass = dvmFindSystemClassNoInit(classDescriptor);
        if (newClass == NULL) {
            LOGV("DexOpt: failed loading '%s'\n", classDescriptor);
            dvmClearOptException(dvmThreadSelf());
        } else if (newClass->pDvmDex != pDvmDex) {
            /*
             * We don't load the new one, and we tag the first one found
             * with the "multiple def" flag so the resolver doesn't try
             * to make it available.
             */
            LOGD("DexOpt: '%s' has an earlier definition; blocking out\n",
                classDescriptor);
            SET_CLASS_FLAG(newClass, CLASS_MULTIPLE_DEFS);
        } else {
            loaded++;
        }
    }
    LOGV("DexOpt: +++ successfully loaded %d classes\n", loaded);

    dvmSetBootPathExtraDex(NULL);
    return true;
}


/*
 * Create a table of inline substitutions.
 *
 * TODO: this is currently just a linear array.  We will want to put this
 * into a hash table as the list size increases.
 */
static InlineSub* createInlineSubsTable(void)
{
    const InlineOperation* ops = dvmGetInlineOpsTable();
    const int count = dvmGetInlineOpsTableLength();
    InlineSub* table;
    Method* method;
    ClassObject* clazz;
    int i, tableIndex;

    /*
     * Allocate for optimism: one slot per entry, plus an end-of-list marker.
     */
    table = malloc(sizeof(InlineSub) * (count+1));

    tableIndex = 0;
    for (i = 0; i < count; i++) {
        clazz = dvmFindClassNoInit(ops[i].classDescriptor, NULL);
        if (clazz == NULL) {
            LOGV("DexOpt: can't inline for class '%s': not found\n",
                ops[i].classDescriptor);
            dvmClearOptException(dvmThreadSelf());
        } else {
            /*
             * Method could be virtual or direct.  Try both.  Don't use
             * the "hier" versions.
             */
            method = dvmFindDirectMethodByDescriptor(clazz, ops[i].methodName,
                        ops[i].methodSignature);
            if (method == NULL)
                method = dvmFindVirtualMethodByDescriptor(clazz, ops[i].methodName,
                        ops[i].methodSignature);
            if (method == NULL) {
                LOGW("DexOpt: can't inline %s.%s %s: method not found\n",
                    ops[i].classDescriptor, ops[i].methodName,
                    ops[i].methodSignature);
            } else {
                if (!dvmIsFinalClass(clazz) && !dvmIsFinalMethod(method)) {
                    LOGW("DexOpt: WARNING: inline op on non-final class/method "
                         "%s.%s\n",
                        clazz->descriptor, method->name);
                    /* fail? */
                }
                if (dvmIsSynchronizedMethod(method) ||
                    dvmIsDeclaredSynchronizedMethod(method))
                {
                    LOGW("DexOpt: WARNING: inline op on synchronized method "
                         "%s.%s\n",
                        clazz->descriptor, method->name);
                    /* fail? */
                }

                table[tableIndex].method = method;
                table[tableIndex].inlineIdx = i;
                tableIndex++;

                LOGV("DexOpt: will inline %d: %s.%s %s\n", i,
                    ops[i].classDescriptor, ops[i].methodName,
                    ops[i].methodSignature);
            }
        }
    }

    /* mark end of table */
    table[tableIndex].method = NULL;
    LOGV("DexOpt: inline table has %d entries\n", tableIndex);

    return table;
}

/*
 * Run through all classes that were successfully loaded from this DEX
 * file and optimize their code sections.
 */
static void optimizeLoadedClasses(DexFile* pDexFile)
{
    u4 count = pDexFile->pHeader->classDefsSize;
    u4 idx;
    InlineSub* inlineSubs = NULL;

    LOGV("DexOpt: +++ optimizing up to %d classes\n", count);
    assert(gDvm.dexOptMode != OPTIMIZE_MODE_NONE);

    inlineSubs = createInlineSubsTable();

    for (idx = 0; idx < count; idx++) {
        const DexClassDef* pClassDef;
        const char* classDescriptor;
        ClassObject* clazz;

        pClassDef = dexGetClassDef(pDexFile, idx);
        classDescriptor = dexStringByTypeIdx(pDexFile, pClassDef->classIdx);

        /* all classes are loaded into the bootstrap class loader */
        clazz = dvmLookupClass(classDescriptor, NULL, false);
        if (clazz != NULL) {
            if ((pClassDef->accessFlags & CLASS_ISPREVERIFIED) == 0 &&
                gDvm.dexOptMode == OPTIMIZE_MODE_VERIFIED)
            {
                LOGV("DexOpt: not optimizing '%s': not verified\n",
                    classDescriptor);
            } else if (clazz->pDvmDex->pDexFile != pDexFile) {
                /* shouldn't be here -- verifier should have caught */
                LOGD("DexOpt: not optimizing '%s': multiple definitions\n",
                    classDescriptor);
            } else {
                optimizeClass(clazz, inlineSubs);

                /* set the flag whether or not we actually did anything */
                ((DexClassDef*)pClassDef)->accessFlags |=
                    CLASS_ISOPTIMIZED;
            }
        } else {
            LOGV("DexOpt: not optimizing unavailable class '%s'\n",
                classDescriptor);
        }
    }

    free(inlineSubs);
}

/*
 * Optimize the specified class.
 */
static void optimizeClass(ClassObject* clazz, const InlineSub* inlineSubs)
{
    int i;

    for (i = 0; i < clazz->directMethodCount; i++) {
        if (!optimizeMethod(&clazz->directMethods[i], inlineSubs))
            goto fail;
    }
    for (i = 0; i < clazz->virtualMethodCount; i++) {
        if (!optimizeMethod(&clazz->virtualMethods[i], inlineSubs))
            goto fail;
    }

    return;

fail:
    LOGV("DexOpt: ceasing optimization attempts on %s\n", clazz->descriptor);
}

/*
 * Optimize instructions in a method.
 *
 * Returns "true" if all went well, "false" if we bailed out early when
 * something failed.
 */
static bool optimizeMethod(Method* method, const InlineSub* inlineSubs)
{
    u4 insnsSize;
    u2* insns;
    u2 inst;

    if (dvmIsNativeMethod(method) || dvmIsAbstractMethod(method))
        return true;

    insns = (u2*) method->insns;
    assert(insns != NULL);
    insnsSize = dvmGetMethodInsnsSize(method);

    while (insnsSize > 0) {
        int width;

        inst = *insns & 0xff;

        switch (inst) {
        case OP_IGET:
        case OP_IGET_BOOLEAN:
        case OP_IGET_BYTE:
        case OP_IGET_CHAR:
        case OP_IGET_SHORT:
            rewriteInstField(method, insns, OP_IGET_QUICK);
            break;
        case OP_IGET_WIDE:
            rewriteInstField(method, insns, OP_IGET_WIDE_QUICK);
            break;
        case OP_IGET_OBJECT:
            rewriteInstField(method, insns, OP_IGET_OBJECT_QUICK);
            break;
        case OP_IPUT:
        case OP_IPUT_BOOLEAN:
        case OP_IPUT_BYTE:
        case OP_IPUT_CHAR:
        case OP_IPUT_SHORT:
            rewriteInstField(method, insns, OP_IPUT_QUICK);
            break;
        case OP_IPUT_WIDE:
            rewriteInstField(method, insns, OP_IPUT_WIDE_QUICK);
            break;
        case OP_IPUT_OBJECT:
            rewriteInstField(method, insns, OP_IPUT_OBJECT_QUICK);
            break;

        case OP_INVOKE_VIRTUAL:
            if (!rewriteExecuteInline(method, insns, METHOD_VIRTUAL,inlineSubs))
            {
                if (!rewriteVirtualInvoke(method, insns, OP_INVOKE_VIRTUAL_QUICK))
                    return false;
            }
            break;
        case OP_INVOKE_VIRTUAL_RANGE:
            if (!rewriteExecuteInlineRange(method, insns, METHOD_VIRTUAL,
                    inlineSubs))
            {
                if (!rewriteVirtualInvoke(method, insns,
                        OP_INVOKE_VIRTUAL_QUICK_RANGE))
                {
                    return false;
                }
            }
            break;
        case OP_INVOKE_SUPER:
            if (!rewriteVirtualInvoke(method, insns, OP_INVOKE_SUPER_QUICK))
                return false;
            break;
        case OP_INVOKE_SUPER_RANGE:
            if (!rewriteVirtualInvoke(method, insns, OP_INVOKE_SUPER_QUICK_RANGE))
                return false;
            break;

        case OP_INVOKE_DIRECT:
            if (!rewriteExecuteInline(method, insns, METHOD_DIRECT, inlineSubs))
            {
                if (!rewriteEmptyDirectInvoke(method, insns))
                    return false;
            }
            break;
        case OP_INVOKE_DIRECT_RANGE:
            rewriteExecuteInlineRange(method, insns, METHOD_DIRECT, inlineSubs);
            break;

        case OP_INVOKE_STATIC:
            rewriteExecuteInline(method, insns, METHOD_STATIC, inlineSubs);
            break;
        case OP_INVOKE_STATIC_RANGE:
            rewriteExecuteInlineRange(method, insns, METHOD_STATIC, inlineSubs);
            break;

        default:
            // ignore this instruction
            ;
        }

        if (*insns == kPackedSwitchSignature) {
            width = 4 + insns[1] * 2;
        } else if (*insns == kSparseSwitchSignature) {
            width = 2 + insns[1] * 4;
        } else if (*insns == kArrayDataSignature) {
            u2 elemWidth = insns[1];
            u4 len = insns[2] | (((u4)insns[3]) << 16);
            width = 4 + (elemWidth * len + 1) / 2;
        } else {
            width = dexGetInstrWidthAbs(gDvm.instrWidth, inst);
        }
        assert(width > 0);

        insns += width;
        insnsSize -= width;
    }

    assert(insnsSize == 0);
    return true;
}


/*
 * If "referrer" and "resClass" don't come from the same DEX file, and
 * the DEX we're working on is not destined for the bootstrap class path,
 * tweak the class loader so package-access checks work correctly.
 *
 * Only do this if we're doing pre-verification or optimization.
 */
static void tweakLoader(ClassObject* referrer, ClassObject* resClass)
{
    if (!gDvm.optimizing)
        return;
    assert(referrer->classLoader == NULL);
    assert(resClass->classLoader == NULL);

    if (!gDvm.optimizingBootstrapClass) {
        /* class loader for an array class comes from element type */
        if (dvmIsArrayClass(resClass))
            resClass = resClass->elementClass;
        if (referrer->pDvmDex != resClass->pDvmDex)
            resClass->classLoader = (Object*) 0xdead3333;
    }
}

/*
 * Undo the effects of tweakLoader.
 */
static void untweakLoader(ClassObject* referrer, ClassObject* resClass)
{
    if (!gDvm.optimizing || gDvm.optimizingBootstrapClass)
        return;

    if (dvmIsArrayClass(resClass))
        resClass = resClass->elementClass;
    resClass->classLoader = NULL;
}


/*
 * Alternate version of dvmResolveClass for use with verification and
 * optimization.  Performs access checks on every resolve, and refuses
 * to acknowledge the existence of classes defined in more than one DEX
 * file.
 *
 * Exceptions caused by failures are cleared before returning.
 *
 * On failure, returns NULL, and sets *pFailure if pFailure is not NULL.
 */
ClassObject* dvmOptResolveClass(ClassObject* referrer, u4 classIdx,
    VerifyError* pFailure)
{
    DvmDex* pDvmDex = referrer->pDvmDex;
    ClassObject* resClass;

    /*
     * Check the table first.  If not there, do the lookup by name.
     */
    resClass = dvmDexGetResolvedClass(pDvmDex, classIdx);
    if (resClass == NULL) {
        const char* className = dexStringByTypeIdx(pDvmDex->pDexFile, classIdx);
        if (className[0] != '\0' && className[1] == '\0') {
            /* primitive type */
            resClass = dvmFindPrimitiveClass(className[0]);
        } else {
            resClass = dvmFindClassNoInit(className, referrer->classLoader);
        }
        if (resClass == NULL) {
            /* not found, exception should be raised */
            LOGV("DexOpt: class %d (%s) not found\n",
                classIdx,
                dexStringByTypeIdx(pDvmDex->pDexFile, classIdx));
            if (pFailure != NULL) {
                /* dig through the wrappers to find the original failure */
                Object* excep = dvmGetException(dvmThreadSelf());
                while (true) {
                    Object* cause = dvmGetExceptionCause(excep);
                    if (cause == NULL)
                        break;
                    excep = cause;
                }
                if (strcmp(excep->clazz->descriptor,
                    "Ljava/lang/IncompatibleClassChangeError;") == 0)
                {
                    *pFailure = VERIFY_ERROR_CLASS_CHANGE;
                } else {
                    *pFailure = VERIFY_ERROR_NO_CLASS;
                }
            }
            dvmClearOptException(dvmThreadSelf());
            return NULL;
        }

        /*
         * Add it to the resolved table so we're faster on the next lookup.
         */
        dvmDexSetResolvedClass(pDvmDex, classIdx, resClass);
    }

    /* multiple definitions? */
    if (IS_CLASS_FLAG_SET(resClass, CLASS_MULTIPLE_DEFS)) {
        LOGI("DexOpt: not resolving ambiguous class '%s'\n",
            resClass->descriptor);
        if (pFailure != NULL)
            *pFailure = VERIFY_ERROR_NO_CLASS;
        return NULL;
    }

    /* access allowed? */
    tweakLoader(referrer, resClass);
    bool allowed = dvmCheckClassAccess(referrer, resClass);
    untweakLoader(referrer, resClass);
    if (!allowed) {
        LOGW("DexOpt: resolve class illegal access: %s -> %s\n",
            referrer->descriptor, resClass->descriptor);
        if (pFailure != NULL)
            *pFailure = VERIFY_ERROR_ACCESS_CLASS;
        return NULL;
    }

    return resClass;
}

/*
 * Alternate version of dvmResolveInstField().
 *
 * On failure, returns NULL, and sets *pFailure if pFailure is not NULL.
 */
InstField* dvmOptResolveInstField(ClassObject* referrer, u4 ifieldIdx,
    VerifyError* pFailure)
{
    DvmDex* pDvmDex = referrer->pDvmDex;
    InstField* resField;

    resField = (InstField*) dvmDexGetResolvedField(pDvmDex, ifieldIdx);
    if (resField == NULL) {
        const DexFieldId* pFieldId;
        ClassObject* resClass;

        pFieldId = dexGetFieldId(pDvmDex->pDexFile, ifieldIdx);

        /*
         * Find the field's class.
         */
        resClass = dvmOptResolveClass(referrer, pFieldId->classIdx, pFailure);
        if (resClass == NULL) {
            //dvmClearOptException(dvmThreadSelf());
            assert(!dvmCheckException(dvmThreadSelf()));
            if (pFailure != NULL) { assert(!VERIFY_OK(*pFailure)); }
            return NULL;
        }

        resField = (InstField*)dvmFindFieldHier(resClass,
            dexStringById(pDvmDex->pDexFile, pFieldId->nameIdx),
            dexStringByTypeIdx(pDvmDex->pDexFile, pFieldId->typeIdx));
        if (resField == NULL) {
            LOGD("DexOpt: couldn't find field %s.%s\n",
                resClass->descriptor,
                dexStringById(pDvmDex->pDexFile, pFieldId->nameIdx));
            if (pFailure != NULL)
                *pFailure = VERIFY_ERROR_NO_FIELD;
            return NULL;
        }
        if (dvmIsStaticField(&resField->field)) {
            LOGD("DexOpt: wanted instance, got static for field %s.%s\n",
                resClass->descriptor,
                dexStringById(pDvmDex->pDexFile, pFieldId->nameIdx));
            if (pFailure != NULL)
                *pFailure = VERIFY_ERROR_CLASS_CHANGE;
            return NULL;
        }

        /*
         * Add it to the resolved table so we're faster on the next lookup.
         */
        dvmDexSetResolvedField(pDvmDex, ifieldIdx, (Field*) resField);
    }

    /* access allowed? */
    tweakLoader(referrer, resField->field.clazz);
    bool allowed = dvmCheckFieldAccess(referrer, (Field*)resField);
    untweakLoader(referrer, resField->field.clazz);
    if (!allowed) {
        LOGI("DexOpt: access denied from %s to field %s.%s\n",
            referrer->descriptor, resField->field.clazz->descriptor,
            resField->field.name);
        if (pFailure != NULL)
            *pFailure = VERIFY_ERROR_ACCESS_FIELD;
        return NULL;
    }

    return resField;
}

/*
 * Alternate version of dvmResolveStaticField().
 *
 * Does not force initialization of the resolved field's class.
 *
 * On failure, returns NULL, and sets *pFailure if pFailure is not NULL.
 */
StaticField* dvmOptResolveStaticField(ClassObject* referrer, u4 sfieldIdx,
    VerifyError* pFailure)
{
    DvmDex* pDvmDex = referrer->pDvmDex;
    StaticField* resField;

    resField = (StaticField*)dvmDexGetResolvedField(pDvmDex, sfieldIdx);
    if (resField == NULL) {
        const DexFieldId* pFieldId;
        ClassObject* resClass;

        pFieldId = dexGetFieldId(pDvmDex->pDexFile, sfieldIdx);

        /*
         * Find the field's class.
         */
        resClass = dvmOptResolveClass(referrer, pFieldId->classIdx, pFailure);
        if (resClass == NULL) {
            //dvmClearOptException(dvmThreadSelf());
            assert(!dvmCheckException(dvmThreadSelf()));
            if (pFailure != NULL) { assert(!VERIFY_OK(*pFailure)); }
            return NULL;
        }

        resField = (StaticField*)dvmFindFieldHier(resClass,
                    dexStringById(pDvmDex->pDexFile, pFieldId->nameIdx),
                    dexStringByTypeIdx(pDvmDex->pDexFile, pFieldId->typeIdx));
        if (resField == NULL) {
            LOGD("DexOpt: couldn't find static field\n");
            if (pFailure != NULL)
                *pFailure = VERIFY_ERROR_NO_FIELD;
            return NULL;
        }
        if (!dvmIsStaticField(&resField->field)) {
            LOGD("DexOpt: wanted static, got instance for field %s.%s\n",
                resClass->descriptor,
                dexStringById(pDvmDex->pDexFile, pFieldId->nameIdx));
            if (pFailure != NULL)
                *pFailure = VERIFY_ERROR_CLASS_CHANGE;
            return NULL;
        }

        /*
         * Add it to the resolved table so we're faster on the next lookup.
         *
         * We can only do this if we're in "dexopt", because the presence
         * of a valid value in the resolution table implies that the class
         * containing the static field has been initialized.
         */
        if (gDvm.optimizing)
            dvmDexSetResolvedField(pDvmDex, sfieldIdx, (Field*) resField);
    }

    /* access allowed? */
    tweakLoader(referrer, resField->field.clazz);
    bool allowed = dvmCheckFieldAccess(referrer, (Field*)resField);
    untweakLoader(referrer, resField->field.clazz);
    if (!allowed) {
        LOGI("DexOpt: access denied from %s to field %s.%s\n",
            referrer->descriptor, resField->field.clazz->descriptor,
            resField->field.name);
        if (pFailure != NULL)
            *pFailure = VERIFY_ERROR_ACCESS_FIELD;
        return NULL;
    }

    return resField;
}


/*
 * Rewrite an iget/iput instruction.  These all have the form:
 *   op vA, vB, field@CCCC
 *
 * Where vA holds the value, vB holds the object reference, and CCCC is
 * the field reference constant pool offset.  We want to replace CCCC
 * with the byte offset from the start of the object.
 *
 * "clazz" is the referring class.  We need this because we verify
 * access rights here.
 */
static void rewriteInstField(Method* method, u2* insns, OpCode newOpc)
{
    ClassObject* clazz = method->clazz;
    u2 fieldIdx = insns[1];
    InstField* field;
    int byteOffset;

    field = dvmOptResolveInstField(clazz, fieldIdx, NULL);
    if (field == NULL) {
        LOGI("DexOpt: unable to optimize field ref 0x%04x at 0x%02x in %s.%s\n",
            fieldIdx, (int) (insns - method->insns), clazz->descriptor,
            method->name);
        return;
    }

    if (field->byteOffset >= 65536) {
        LOGI("DexOpt: field offset exceeds 64K (%d)\n", field->byteOffset);
        return;
    }

    insns[0] = (insns[0] & 0xff00) | (u2) newOpc;
    insns[1] = (u2) field->byteOffset;
    LOGVV("DexOpt: rewrote access to %s.%s --> %d\n",
        field->field.clazz->descriptor, field->field.name,
        field->byteOffset);
}

/*
 * Alternate version of dvmResolveMethod().
 *
 * Doesn't throw exceptions, and checks access on every lookup.
 *
 * On failure, returns NULL, and sets *pFailure if pFailure is not NULL.
 */
Method* dvmOptResolveMethod(ClassObject* referrer, u4 methodIdx,
    MethodType methodType, VerifyError* pFailure)
{
    DvmDex* pDvmDex = referrer->pDvmDex;
    Method* resMethod;

    assert(methodType == METHOD_DIRECT ||
           methodType == METHOD_VIRTUAL ||
           methodType == METHOD_STATIC);

    LOGVV("--- resolving method %u (referrer=%s)\n", methodIdx,
        referrer->descriptor);

    resMethod = dvmDexGetResolvedMethod(pDvmDex, methodIdx);
    if (resMethod == NULL) {
        const DexMethodId* pMethodId;
        ClassObject* resClass;

        pMethodId = dexGetMethodId(pDvmDex->pDexFile, methodIdx);

        resClass = dvmOptResolveClass(referrer, pMethodId->classIdx, pFailure);
        if (resClass == NULL) {
            /*
             * Can't find the class that the method is a part of, or don't
             * have permission to access the class.
             */
            LOGV("DexOpt: can't find called method's class (?.%s)\n",
                dexStringById(pDvmDex->pDexFile, pMethodId->nameIdx));
            if (pFailure != NULL) { assert(!VERIFY_OK(*pFailure)); }
            return NULL;
        }
        if (dvmIsInterfaceClass(resClass)) {
            /* method is part of an interface; this is wrong method for that */
            LOGW("DexOpt: method is in an interface\n");
            if (pFailure != NULL)
                *pFailure = VERIFY_ERROR_GENERIC;
            return NULL;
        }

        /*
         * We need to chase up the class hierarchy to find methods defined
         * in super-classes.  (We only want to check the current class
         * if we're looking for a constructor.)
         */
        DexProto proto;
        dexProtoSetFromMethodId(&proto, pDvmDex->pDexFile, pMethodId);

        if (methodType == METHOD_DIRECT) {
            resMethod = dvmFindDirectMethod(resClass,
                dexStringById(pDvmDex->pDexFile, pMethodId->nameIdx), &proto);
        } else {
            /* METHOD_STATIC or METHOD_VIRTUAL */
            resMethod = dvmFindMethodHier(resClass,
                dexStringById(pDvmDex->pDexFile, pMethodId->nameIdx), &proto);
        }

        if (resMethod == NULL) {
            LOGV("DexOpt: couldn't find method '%s'\n",
                dexStringById(pDvmDex->pDexFile, pMethodId->nameIdx));
            if (pFailure != NULL)
                *pFailure = VERIFY_ERROR_NO_METHOD;
            return NULL;
        }
        if (methodType == METHOD_STATIC) {
            if (!dvmIsStaticMethod(resMethod)) {
                LOGD("DexOpt: wanted static, got instance for method %s.%s\n",
                    resClass->descriptor, resMethod->name);
                if (pFailure != NULL)
                    *pFailure = VERIFY_ERROR_CLASS_CHANGE;
                return NULL;
            }
        } else if (methodType == METHOD_VIRTUAL) {
            if (dvmIsStaticMethod(resMethod)) {
                LOGD("DexOpt: wanted instance, got static for method %s.%s\n",
                    resClass->descriptor, resMethod->name);
                if (pFailure != NULL)
                    *pFailure = VERIFY_ERROR_CLASS_CHANGE;
                return NULL;
            }
        }

        /* see if this is a pure-abstract method */
        if (dvmIsAbstractMethod(resMethod) && !dvmIsAbstractClass(resClass)) {
            LOGW("DexOpt: pure-abstract method '%s' in %s\n",
                dexStringById(pDvmDex->pDexFile, pMethodId->nameIdx),
                resClass->descriptor);
            if (pFailure != NULL)
                *pFailure = VERIFY_ERROR_GENERIC;
            return NULL;
        }

        /*
         * Add it to the resolved table so we're faster on the next lookup.
         *
         * We can only do this for static methods if we're not in "dexopt",
         * because the presence of a valid value in the resolution table
         * implies that the class containing the static field has been
         * initialized.
         */
        if (methodType != METHOD_STATIC || gDvm.optimizing)
            dvmDexSetResolvedMethod(pDvmDex, methodIdx, resMethod);
    }

    LOGVV("--- found method %d (%s.%s)\n",
        methodIdx, resMethod->clazz->descriptor, resMethod->name);

    /* access allowed? */
    tweakLoader(referrer, resMethod->clazz);
    bool allowed = dvmCheckMethodAccess(referrer, resMethod);
    untweakLoader(referrer, resMethod->clazz);
    if (!allowed) {
        IF_LOGI() {
            char* desc = dexProtoCopyMethodDescriptor(&resMethod->prototype);
            LOGI("DexOpt: illegal method access (call %s.%s %s from %s)\n",
                resMethod->clazz->descriptor, resMethod->name, desc,
                referrer->descriptor);
            free(desc);
        }
        if (pFailure != NULL)
            *pFailure = VERIFY_ERROR_ACCESS_METHOD;
        return NULL;
    }

    return resMethod;
}

/*
 * Rewrite invoke-virtual, invoke-virtual/range, invoke-super, and
 * invoke-super/range.  These all have the form:
 *   op vAA, meth@BBBB, reg stuff @CCCC
 *
 * We want to replace the method constant pool index BBBB with the
 * vtable index.
 */
static bool rewriteVirtualInvoke(Method* method, u2* insns, OpCode newOpc)
{
    ClassObject* clazz = method->clazz;
    Method* baseMethod;
    u2 methodIdx = insns[1];

    baseMethod = dvmOptResolveMethod(clazz, methodIdx, METHOD_VIRTUAL, NULL);
    if (baseMethod == NULL) {
        LOGD("DexOpt: unable to optimize virt call 0x%04x at 0x%02x in %s.%s\n",
            methodIdx,
            (int) (insns - method->insns), clazz->descriptor,
            method->name);
        return false;
    }

    assert((insns[0] & 0xff) == OP_INVOKE_VIRTUAL ||
           (insns[0] & 0xff) == OP_INVOKE_VIRTUAL_RANGE ||
           (insns[0] & 0xff) == OP_INVOKE_SUPER ||
           (insns[0] & 0xff) == OP_INVOKE_SUPER_RANGE);

    /*
     * Note: Method->methodIndex is a u2 and is range checked during the
     * initial load.
     */
    insns[0] = (insns[0] & 0xff00) | (u2) newOpc;
    insns[1] = baseMethod->methodIndex;

    //LOGI("DexOpt: rewrote call to %s.%s --> %s.%s\n",
    //    method->clazz->descriptor, method->name,
    //    baseMethod->clazz->descriptor, baseMethod->name);

    return true;
}

/*
 * Rewrite invoke-direct, which has the form:
 *   op vAA, meth@BBBB, reg stuff @CCCC
 *
 * There isn't a lot we can do to make this faster, but in some situations
 * we can make it go away entirely.
 *
 * This must only be used when the invoked method does nothing and has
 * no return value (the latter being very important for verification).
 */
static bool rewriteEmptyDirectInvoke(Method* method, u2* insns)
{
    ClassObject* clazz = method->clazz;
    Method* calledMethod;
    u2 methodIdx = insns[1];

    calledMethod = dvmOptResolveMethod(clazz, methodIdx, METHOD_DIRECT, NULL);
    if (calledMethod == NULL) {
        LOGD("DexOpt: unable to opt direct call 0x%04x at 0x%02x in %s.%s\n",
            methodIdx,
            (int) (insns - method->insns), clazz->descriptor,
            method->name);
        return false;
    }

    /* TODO: verify that java.lang.Object() is actually empty! */
    if (calledMethod->clazz == gDvm.classJavaLangObject &&
        dvmCompareNameDescriptorAndMethod("<init>", "()V", calledMethod) == 0)
    {
        /*
         * Replace with "empty" instruction.  DO NOT disturb anything
         * else about it, as we want it to function the same as
         * OP_INVOKE_DIRECT when debugging is enabled.
         */
        assert((insns[0] & 0xff) == OP_INVOKE_DIRECT);
        insns[0] = (insns[0] & 0xff00) | (u2) OP_INVOKE_DIRECT_EMPTY;

        //LOGI("DexOpt: marked-empty call to %s.%s --> %s.%s\n",
        //    method->clazz->descriptor, method->name,
        //    calledMethod->clazz->descriptor, calledMethod->name);
    }

    return true;
}

/*
 * Resolve an interface method reference.
 *
 * No method access check here -- interface methods are always public.
 *
 * Returns NULL if the method was not found.  Does not throw an exception.
 */
Method* dvmOptResolveInterfaceMethod(ClassObject* referrer, u4 methodIdx)
{
    DvmDex* pDvmDex = referrer->pDvmDex;
    Method* resMethod;
    int i;

    LOGVV("--- resolving interface method %d (referrer=%s)\n",
        methodIdx, referrer->descriptor);

    resMethod = dvmDexGetResolvedMethod(pDvmDex, methodIdx);
    if (resMethod == NULL) {
        const DexMethodId* pMethodId;
        ClassObject* resClass;

        pMethodId = dexGetMethodId(pDvmDex->pDexFile, methodIdx);

        resClass = dvmOptResolveClass(referrer, pMethodId->classIdx, NULL);
        if (resClass == NULL) {
            /* can't find the class that the method is a part of */
            dvmClearOptException(dvmThreadSelf());
            return NULL;
        }
        if (!dvmIsInterfaceClass(resClass)) {
            /* whoops */
            LOGI("Interface method not part of interface class\n");
            return NULL;
        }

        const char* methodName =
            dexStringById(pDvmDex->pDexFile, pMethodId->nameIdx);
        DexProto proto;
        dexProtoSetFromMethodId(&proto, pDvmDex->pDexFile, pMethodId);

        LOGVV("+++ looking for '%s' '%s' in resClass='%s'\n",
            methodName, methodSig, resClass->descriptor);
        resMethod = dvmFindVirtualMethod(resClass, methodName, &proto);
        if (resMethod == NULL) {
            /* scan superinterfaces and superclass interfaces */
            LOGVV("+++ did not resolve immediately\n");
            for (i = 0; i < resClass->iftableCount; i++) {
                resMethod = dvmFindVirtualMethod(resClass->iftable[i].clazz,
                                methodName, &proto);
                if (resMethod != NULL)
                    break;
            }

            if (resMethod == NULL) {
                LOGVV("+++ unable to resolve method %s\n", methodName);
                return NULL;
            }
        } else {
            LOGVV("+++ resolved immediately: %s (%s %d)\n", resMethod->name,
                resMethod->clazz->descriptor, (u4) resMethod->methodIndex);
        }

        /* we're expecting this to be abstract */
        if (!dvmIsAbstractMethod(resMethod)) {
            char* desc = dexProtoCopyMethodDescriptor(&resMethod->prototype);
            LOGW("Found non-abstract interface method %s.%s %s\n",
                resMethod->clazz->descriptor, resMethod->name, desc);
            free(desc);
            return NULL;
        }

        /*
         * Add it to the resolved table so we're faster on the next lookup.
         */
        dvmDexSetResolvedMethod(pDvmDex, methodIdx, resMethod);
    }

    LOGVV("--- found interface method %d (%s.%s)\n",
        methodIdx, resMethod->clazz->descriptor, resMethod->name);

    /* interface methods are always public; no need to check access */

    return resMethod;
}

/*
 * See if the method being called can be rewritten as an inline operation.
 * Works for invoke-virtual, invoke-direct, and invoke-static.
 *
 * Returns "true" if we replace it.
 */
static bool rewriteExecuteInline(Method* method, u2* insns,
    MethodType methodType, const InlineSub* inlineSubs)
{
    ClassObject* clazz = method->clazz;
    Method* calledMethod;
    u2 methodIdx = insns[1];

    //return false;

    calledMethod = dvmOptResolveMethod(clazz, methodIdx, methodType, NULL);
    if (calledMethod == NULL) {
        LOGV("+++ DexOpt inline: can't find %d\n", methodIdx);
        return false;
    }

    while (inlineSubs->method != NULL) {
        /*
        if (extra) {
            LOGI("comparing %p vs %p %s.%s %s\n",
                inlineSubs->method, calledMethod,
                inlineSubs->method->clazz->descriptor,
                inlineSubs->method->name,
                inlineSubs->method->signature);
        }
        */
        if (inlineSubs->method == calledMethod) {
            assert((insns[0] & 0xff) == OP_INVOKE_DIRECT ||
                   (insns[0] & 0xff) == OP_INVOKE_STATIC ||
                   (insns[0] & 0xff) == OP_INVOKE_VIRTUAL);
            insns[0] = (insns[0] & 0xff00) | (u2) OP_EXECUTE_INLINE;
            insns[1] = (u2) inlineSubs->inlineIdx;

            //LOGI("DexOpt: execute-inline %s.%s --> %s.%s\n",
            //    method->clazz->descriptor, method->name,
            //    calledMethod->clazz->descriptor, calledMethod->name);
            return true;
        }

        inlineSubs++;
    }

    return false;
}

/*
 * See if the method being called can be rewritten as an inline operation.
 * Works for invoke-virtual/range, invoke-direct/range, and invoke-static/range.
 *
 * Returns "true" if we replace it.
 */
static bool rewriteExecuteInlineRange(Method* method, u2* insns,
    MethodType methodType, const InlineSub* inlineSubs)
{
    ClassObject* clazz = method->clazz;
    Method* calledMethod;
    u2 methodIdx = insns[1];

    calledMethod = dvmOptResolveMethod(clazz, methodIdx, methodType, NULL);
    if (calledMethod == NULL) {
        LOGV("+++ DexOpt inline/range: can't find %d\n", methodIdx);
        return false;
    }

    while (inlineSubs->method != NULL) {
        if (inlineSubs->method == calledMethod) {
            assert((insns[0] & 0xff) == OP_INVOKE_DIRECT_RANGE ||
                   (insns[0] & 0xff) == OP_INVOKE_STATIC_RANGE ||
                   (insns[0] & 0xff) == OP_INVOKE_VIRTUAL_RANGE);
            insns[0] = (insns[0] & 0xff00) | (u2) OP_EXECUTE_INLINE_RANGE;
            insns[1] = (u2) inlineSubs->inlineIdx;

            //LOGI("DexOpt: execute-inline/range %s.%s --> %s.%s\n",
            //    method->clazz->descriptor, method->name,
            //    calledMethod->clazz->descriptor, calledMethod->name);
            return true;
        }

        inlineSubs++;
    }

    return false;
}

