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
 * Inlined native functions.  These definitions replace interpreted or
 * native implementations at runtime; "intrinsic" might be a better word.
 */
#include "Dalvik.h"

#include <math.h>

#ifdef HAVE__MEMCMP16
/* hand-coded assembly implementation, available on some platforms */
//#warning "trying memcmp16"
//#define CHECK_MEMCMP16
/* "count" is in 16-bit units */
extern u4 __memcmp16(const u2* s0, const u2* s1, size_t count);
#endif

/*
 * Some notes on "inline" functions.
 *
 * These are NOT simply native implementations.  A full method definition
 * must still be provided.  Depending on the flags passed into the VM
 * at runtime, the original or inline version may be selected by the
 * DEX optimizer.
 *
 * PLEASE DO NOT use this as the default location for native methods.
 * The difference between this and an "internal native" static method
 * call on a 200MHz ARM 9 is roughly 370ns vs. 700ns.  The code here
 * "secretly replaces" the other method, so you can't avoid having two
 * implementations.  Since the DEX optimizer mode can't be known ahead
 * of time, both implementations must be correct and complete.
 *
 * The only stuff that really needs to be here are methods that
 * are high-volume or must be low-overhead, e.g. certain String/Math
 * methods and some java.util.concurrent.atomic operations.
 *
 * Normally, a class is loaded and initialized the first time a static
 * method is invoked.  This property is NOT preserved here.  If you need
 * to access a static field in a class, you must ensure initialization
 * yourself (cheap/easy way is to check the resolved-methods table, and
 * resolve the method if it hasn't been).
 *
 * DO NOT replace "synchronized" methods.  We do not support method
 * synchronization here.
 *
 * Remember that these functions are executing while the thread is in
 * the "RUNNING" state, not the "NATIVE" state.  If you perform a blocking
 * operation you can stall the entire VM if the GC or debugger wants to
 * suspend the thread.  Since these are arguably native implementations
 * rather than VM internals, prefer NATIVE to VMWAIT if you want to change
 * the thread state.
 *
 * Always write results to 32-bit or 64-bit fields in "pResult", e.g. do
 * not write boolean results to pResult->z.  The interpreter expects
 * 32 or 64 bits to be set.
 *
 * Inline op methods return "false" if an exception was thrown, "true" if
 * everything went well.
 *
 * DO NOT provide implementations of methods that can be overridden by a
 * subclass, as polymorphism does not work correctly.  For safety you should
 * only provide inline functions for classes/methods declared "final".
 *
 * It's best to avoid inlining the overridden version of a method.  For
 * example, String.hashCode() is inherited from Object.hashCode().  Code
 * calling String.hashCode() through an Object reference will run the
 * "slow" version, while calling it through a String reference gets
 * the inlined version.  It's best to have just one version unless there
 * are clear performance gains.
 *
 * Because the actual method is not called, debugger breakpoints on these
 * methods will not happen.  (TODO: have the code here find the original
 * method and call it when the debugger is active.)  Additional steps have
 * been taken to allow method profiling to produce correct results.
 */


/*
 * ===========================================================================
 *      org.apache.harmony.dalvik.NativeTestTarget
 * ===========================================================================
 */

/*
 * public static void emptyInlineMethod
 *
 * This exists only for benchmarks.
 */
static bool org_apache_harmony_dalvik_NativeTestTarget_emptyInlineMethod(
    u4 arg0, u4 arg1, u4 arg2, u4 arg3, JValue* pResult)
{
    // do nothing
    return true;
}


/*
 * ===========================================================================
 *      java.lang.String
 * ===========================================================================
 */

/*
 * public char charAt(int index)
 */
static bool javaLangString_charAt(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    int count, offset;
    ArrayObject* chars;

    /* null reference check on "this" */
    if (!dvmValidateObject((Object*) arg0))
        return false;

    //LOGI("String.charAt this=0x%08x index=%d\n", arg0, arg1);
    count = dvmGetFieldInt((Object*) arg0, gDvm.offJavaLangString_count);
    if ((s4) arg1 < 0 || (s4) arg1 >= count) {
        dvmThrowException("Ljava/lang/StringIndexOutOfBoundsException;", NULL);
        return false;
    } else {
        offset = dvmGetFieldInt((Object*) arg0, gDvm.offJavaLangString_offset);
        chars = (ArrayObject*)
            dvmGetFieldObject((Object*) arg0, gDvm.offJavaLangString_value);

        pResult->i = ((const u2*) chars->contents)[arg1 + offset];
        return true;
    }
}

#ifdef CHECK_MEMCMP16
/*
 * Utility function when we're evaluating alternative implementations.
 */
static void badMatch(StringObject* thisStrObj, StringObject* compStrObj,
    int expectResult, int newResult, const char* compareType)
{
    ArrayObject* thisArray;
    ArrayObject* compArray;
    const char* thisStr;
    const char* compStr;
    int thisOffset, compOffset, thisCount, compCount;

    thisCount =
        dvmGetFieldInt((Object*) thisStrObj, gDvm.offJavaLangString_count);
    compCount =
        dvmGetFieldInt((Object*) compStrObj, gDvm.offJavaLangString_count);
    thisOffset =
        dvmGetFieldInt((Object*) thisStrObj, gDvm.offJavaLangString_offset);
    compOffset =
        dvmGetFieldInt((Object*) compStrObj, gDvm.offJavaLangString_offset);
    thisArray = (ArrayObject*)
        dvmGetFieldObject((Object*) thisStrObj, gDvm.offJavaLangString_value);
    compArray = (ArrayObject*)
        dvmGetFieldObject((Object*) compStrObj, gDvm.offJavaLangString_value);

    thisStr = dvmCreateCstrFromString(thisStrObj);
    compStr = dvmCreateCstrFromString(compStrObj);

    LOGE("%s expected %d got %d\n", compareType, expectResult, newResult);
    LOGE(" this (o=%d l=%d) '%s'\n", thisOffset, thisCount, thisStr);
    LOGE(" comp (o=%d l=%d) '%s'\n", compOffset, compCount, compStr);
    dvmPrintHexDumpEx(ANDROID_LOG_INFO, LOG_TAG,
        ((const u2*) thisArray->contents) + thisOffset, thisCount*2,
        kHexDumpLocal);
    dvmPrintHexDumpEx(ANDROID_LOG_INFO, LOG_TAG,
        ((const u2*) compArray->contents) + compOffset, compCount*2,
        kHexDumpLocal);
    dvmAbort();
}
#endif

/*
 * public int compareTo(String s)
 */
static bool javaLangString_compareTo(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    /*
     * Null reference check on "this".  Normally this is performed during
     * the setup of the virtual method call.  We need to do it before
     * anything else.  While we're at it, check out the other string,
     * which must also be non-null.
     */
    if (!dvmValidateObject((Object*) arg0) ||
        !dvmValidateObject((Object*) arg1))
    {
        return false;
    }

    /* quick test for comparison with itself */
    if (arg0 == arg1) {
        pResult->i = 0;
        return true;
    }

    /*
     * This would be simpler and faster if we promoted StringObject to
     * a full representation, lining up the C structure fields with the
     * actual object fields.
     */
    int thisCount, thisOffset, compCount, compOffset;
    ArrayObject* thisArray;
    ArrayObject* compArray;
    const u2* thisChars;
    const u2* compChars;
    int i, minCount, countDiff;

    thisCount = dvmGetFieldInt((Object*) arg0, gDvm.offJavaLangString_count);
    compCount = dvmGetFieldInt((Object*) arg1, gDvm.offJavaLangString_count);
    countDiff = thisCount - compCount;
    minCount = (countDiff < 0) ? thisCount : compCount;
    thisOffset = dvmGetFieldInt((Object*) arg0, gDvm.offJavaLangString_offset);
    compOffset = dvmGetFieldInt((Object*) arg1, gDvm.offJavaLangString_offset);
    thisArray = (ArrayObject*)
        dvmGetFieldObject((Object*) arg0, gDvm.offJavaLangString_value);
    compArray = (ArrayObject*)
        dvmGetFieldObject((Object*) arg1, gDvm.offJavaLangString_value);
    thisChars = ((const u2*) thisArray->contents) + thisOffset;
    compChars = ((const u2*) compArray->contents) + compOffset;

#ifdef HAVE__MEMCMP16
    /*
     * Use assembly version, which returns the difference between the
     * characters.  The annoying part here is that 0x00e9 - 0xffff != 0x00ea,
     * because the interpreter converts the characters to 32-bit integers
     * *without* sign extension before it subtracts them (which makes some
     * sense since "char" is unsigned).  So what we get is the result of
     * 0x000000e9 - 0x0000ffff, which is 0xffff00ea.
     */
    int otherRes = __memcmp16(thisChars, compChars, minCount);
# ifdef CHECK_MEMCMP16
    for (i = 0; i < minCount; i++) {
        if (thisChars[i] != compChars[i]) {
            pResult->i = (s4) thisChars[i] - (s4) compChars[i];
            if (pResult->i != otherRes) {
                badMatch((StringObject*) arg0, (StringObject*) arg1,
                    pResult->i, otherRes, "compareTo");
            }
            return true;
        }
    }
# endif
    if (otherRes != 0) {
        pResult->i = otherRes;
        return true;
    }

#else
    /*
     * Straightforward implementation, examining 16 bits at a time.  Compare
     * the characters that overlap, and if they're all the same then return
     * the difference in lengths.
     */
    for (i = 0; i < minCount; i++) {
        if (thisChars[i] != compChars[i]) {
            pResult->i = (s4) thisChars[i] - (s4) compChars[i];
            return true;
        }
    }
#endif

    pResult->i = countDiff;
    return true;
}

/*
 * public boolean equals(Object anObject)
 */
static bool javaLangString_equals(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    /*
     * Null reference check on "this".
     */
    if (!dvmValidateObject((Object*) arg0))
        return false;

    /* quick test for comparison with itself */
    if (arg0 == arg1) {
        pResult->i = true;
        return true;
    }

    /*
     * See if the other object is also a String.
     *
     * str.equals(null) is expected to return false, presumably based on
     * the results of the instanceof test.
     */
    if (arg1 == 0 || ((Object*) arg0)->clazz != ((Object*) arg1)->clazz) {
        pResult->i = false;
        return true;
    }

    /*
     * This would be simpler and faster if we promoted StringObject to
     * a full representation, lining up the C structure fields with the
     * actual object fields.
     */
    int thisCount, thisOffset, compCount, compOffset;
    ArrayObject* thisArray;
    ArrayObject* compArray;
    const u2* thisChars;
    const u2* compChars;
    int i;

    /* quick length check */
    thisCount = dvmGetFieldInt((Object*) arg0, gDvm.offJavaLangString_count);
    compCount = dvmGetFieldInt((Object*) arg1, gDvm.offJavaLangString_count);
    if (thisCount != compCount) {
        pResult->i = false;
        return true;
    }

    thisOffset = dvmGetFieldInt((Object*) arg0, gDvm.offJavaLangString_offset);
    compOffset = dvmGetFieldInt((Object*) arg1, gDvm.offJavaLangString_offset);
    thisArray = (ArrayObject*)
        dvmGetFieldObject((Object*) arg0, gDvm.offJavaLangString_value);
    compArray = (ArrayObject*)
        dvmGetFieldObject((Object*) arg1, gDvm.offJavaLangString_value);
    thisChars = ((const u2*) thisArray->contents) + thisOffset;
    compChars = ((const u2*) compArray->contents) + compOffset;

#ifdef HAVE__MEMCMP16
    pResult->i = (__memcmp16(thisChars, compChars, thisCount) == 0);
# ifdef CHECK_MEMCMP16
    int otherRes = (memcmp(thisChars, compChars, thisCount * 2) == 0);
    if (pResult->i != otherRes) {
        badMatch((StringObject*) arg0, (StringObject*) arg1,
            otherRes, pResult->i, "equals-1");
    }
# endif
#else
    /*
     * Straightforward implementation, examining 16 bits at a time.  The
     * direction of the loop doesn't matter, and starting at the end may
     * give us an advantage when comparing certain types of strings (e.g.
     * class names).
     *
     * We want to go forward for benchmarks against __memcmp16 so we get a
     * meaningful comparison when the strings don't match (could also test
     * with palindromes).
     */
    //for (i = 0; i < thisCount; i++)
    for (i = thisCount-1; i >= 0; --i)
    {
        if (thisChars[i] != compChars[i]) {
            pResult->i = false;
            return true;
        }
    }
    pResult->i = true;
#endif

    return true;
}

/*
 * public int length()
 */
static bool javaLangString_length(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    //LOGI("String.length this=0x%08x pResult=%p\n", arg0, pResult);

    /* null reference check on "this" */
    if (!dvmValidateObject((Object*) arg0))
        return false;

    pResult->i = dvmGetFieldInt((Object*) arg0, gDvm.offJavaLangString_count);
    return true;
}


/*
 * ===========================================================================
 *      java.lang.Math
 * ===========================================================================
 */

/*
 * public static int abs(int)
 */
static bool javaLangMath_abs_int(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    s4 val = (s4) arg0;
    pResult->i = (val >= 0) ? val : -val;
    return true;
}

/*
 * public static long abs(long)
 */
static bool javaLangMath_abs_long(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    union {
        u4 arg[2];
        s8 ll;
    } convert;

    convert.arg[0] = arg0;
    convert.arg[1] = arg1;
    s8 val = convert.ll;
    pResult->j = (val >= 0) ? val : -val;
    return true;
}

/*
 * public static float abs(float)
 */
static bool javaLangMath_abs_float(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    union {
        u4 arg;
        float ff;
    } convert;

    /* clear the sign bit; assumes a fairly common fp representation */
    convert.arg = arg0 & 0x7fffffff;
    pResult->f = convert.ff;
    return true;
}

/*
 * public static double abs(double)
 */
static bool javaLangMath_abs_double(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    union {
        u4 arg[2];
        s8 ll;
        double dd;
    } convert;

    /* clear the sign bit in the (endian-dependent) high word */
    convert.arg[0] = arg0;
    convert.arg[1] = arg1;
    convert.ll &= 0x7fffffffffffffffULL;
    pResult->d = convert.dd;
    return true;
}

/*
 * public static int min(int)
 */
static bool javaLangMath_min_int(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    pResult->i = ((s4) arg0 < (s4) arg1) ? arg0 : arg1;
    return true;
}

/*
 * public static int max(int)
 */
static bool javaLangMath_max_int(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    pResult->i = ((s4) arg0 > (s4) arg1) ? arg0 : arg1;
    return true;
}

/*
 * public static double sqrt(double)
 *
 * With ARM VFP enabled, gcc turns this into an fsqrtd instruction, followed
 * by an fcmpd of the result against itself.  If it doesn't match (i.e.
 * it's NaN), the libm sqrt() is invoked.
 */
static bool javaLangMath_sqrt(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    union {
        u4 arg[2];
        double dd;
    } convert;

    convert.arg[0] = arg0;
    convert.arg[1] = arg1;
    pResult->d = sqrt(convert.dd);
    return true;
}

/*
 * public static double cos(double)
 */
static bool javaLangMath_cos(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    union {
        u4 arg[2];
        double dd;
    } convert;

    convert.arg[0] = arg0;
    convert.arg[1] = arg1;
    pResult->d = cos(convert.dd);
    return true;
}

/*
 * public static double sin(double)
 */
static bool javaLangMath_sin(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult)
{
    union {
        u4 arg[2];
        double dd;
    } convert;

    convert.arg[0] = arg0;
    convert.arg[1] = arg1;
    pResult->d = sin(convert.dd);
    return true;
}


/*
 * ===========================================================================
 *      Infrastructure
 * ===========================================================================
 */

/*
 * Table of methods.
 *
 * The DEX optimizer uses the class/method/signature string fields to decide
 * which calls it can trample.  The interpreter just uses the function
 * pointer field.
 *
 * IMPORTANT: you must update DALVIK_VM_BUILD in DalvikVersion.h if you make
 * changes to this table.  Must also be kept in sync with NativeInlineOps
 * enum in InlineNative.h.
 */
const InlineOperation gDvmInlineOpsTable[] = {
    { org_apache_harmony_dalvik_NativeTestTarget_emptyInlineMethod,
        "Lorg/apache/harmony/dalvik/NativeTestTarget;",
        "emptyInlineMethod", "()V" },

    { javaLangString_charAt,
        "Ljava/lang/String;", "charAt", "(I)C" },
    { javaLangString_compareTo,
        "Ljava/lang/String;", "compareTo", "(Ljava/lang/String;)I" },
    { javaLangString_equals,
        "Ljava/lang/String;", "equals", "(Ljava/lang/Object;)Z" },
    { javaLangString_length,
        "Ljava/lang/String;", "length", "()I" },

    { javaLangMath_abs_int,
        "Ljava/lang/Math;", "abs", "(I)I" },
    { javaLangMath_abs_long,
        "Ljava/lang/Math;", "abs", "(J)J" },
    { javaLangMath_abs_float,
        "Ljava/lang/Math;", "abs", "(F)F" },
    { javaLangMath_abs_double,
        "Ljava/lang/Math;", "abs", "(D)D" },
    { javaLangMath_min_int,
        "Ljava/lang/Math;", "min", "(II)I" },
    { javaLangMath_max_int,
        "Ljava/lang/Math;", "max", "(II)I" },
    { javaLangMath_sqrt,
        "Ljava/lang/Math;", "sqrt", "(D)D" },
    { javaLangMath_cos,
        "Ljava/lang/Math;", "cos", "(D)D" },
    { javaLangMath_sin,
        "Ljava/lang/Math;", "sin", "(D)D" },
};


/*
 * Allocate some tables.
 */
bool dvmInlineNativeStartup(void)
{
#ifdef WITH_PROFILER
    gDvm.inlinedMethods =
        (Method**) calloc(NELEM(gDvmInlineOpsTable), sizeof(Method*));
    if (gDvm.inlinedMethods == NULL)
        return false;
#endif

    return true;
}

/*
 * Free generated tables.
 */
void dvmInlineNativeShutdown(void)
{
#ifdef WITH_PROFILER
    free(gDvm.inlinedMethods);
#endif
}


/*
 * Get a pointer to the inlineops table.
 */
const InlineOperation* dvmGetInlineOpsTable(void)
{
    return gDvmInlineOpsTable;
}

/*
 * Get the number of entries in the inlineops table.
 */
int dvmGetInlineOpsTableLength(void)
{
    return NELEM(gDvmInlineOpsTable);
}

/*
 * Make an inline call for the "debug" interpreter, used when the debugger
 * or profiler is active.
 */
bool dvmPerformInlineOp4Dbg(u4 arg0, u4 arg1, u4 arg2, u4 arg3,
    JValue* pResult, int opIndex)
{
    Thread* self = dvmThreadSelf();
    bool result;

    assert(opIndex >= 0 && opIndex < NELEM(gDvmInlineOpsTable));

#ifdef WITH_PROFILER
    /*
     * Populate the methods table on first use.  It's possible the class
     * hasn't been resolved yet, so we need to do the full "calling the
     * method for the first time" routine.  (It's probably okay to skip
     * the access checks.)
     *
     * Currently assuming that we're only inlining stuff loaded by the
     * bootstrap class loader.  This is a safe assumption for many reasons.
     */
    Method* method = gDvm.inlinedMethods[opIndex];
    if (method == NULL) {
        ClassObject* clazz;
        
        clazz = dvmFindClassNoInit(
                gDvmInlineOpsTable[opIndex].classDescriptor, NULL);
        if (clazz == NULL) {
            LOGW("Warning: can't find class '%s'\n", clazz->descriptor);
            goto skip_prof;
        }
        method = dvmFindDirectMethodByDescriptor(clazz,
                    gDvmInlineOpsTable[opIndex].methodName,
                    gDvmInlineOpsTable[opIndex].methodSignature);
        if (method == NULL)
            method = dvmFindVirtualMethodByDescriptor(clazz,
                        gDvmInlineOpsTable[opIndex].methodName,
                        gDvmInlineOpsTable[opIndex].methodSignature);
        if (method == NULL) {
            LOGW("Warning: can't find method %s.%s %s\n",
                clazz->descriptor,
                gDvmInlineOpsTable[opIndex].methodName,
                gDvmInlineOpsTable[opIndex].methodSignature);
            goto skip_prof;
        }

        gDvm.inlinedMethods[opIndex] = method;
        IF_LOGV() {
            char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
            LOGV("Registered for profile: %s.%s %s\n",
                method->clazz->descriptor, method->name, desc);
            free(desc);
        }
    }

    TRACE_METHOD_ENTER(self, method);
    result = (*gDvmInlineOpsTable[opIndex].func)(arg0, arg1, arg2, arg3,
                pResult);
    TRACE_METHOD_EXIT(self, method);
    return result;

skip_prof:
#endif
    return (*gDvmInlineOpsTable[opIndex].func)(arg0, arg1, arg2, arg3, pResult);
}
