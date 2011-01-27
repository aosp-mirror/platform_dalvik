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
 * java.lang.Class
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"

/*
 * Call the appropriate copy function given the circumstances.
 */
static void copy(void *dest, const void *src, size_t n, bool sameArray,
        size_t elemSize)
{
    if (sameArray) {
        /* Might overlap. */
        if (elemSize == sizeof(Object*)) {
            /*
             * In addition to handling overlap properly, bcopy()
             * guarantees atomic treatment of words. This is needed so
             * that concurrent threads never see half-formed pointers
             * or ints. The former is required for proper gc behavior,
             * and the latter is also required for proper high-level
             * language support.
             *
             * Note: bcopy()'s argument order is different than memcpy().
             */
            bcopy(src, dest, n);
        } else {
            memmove(dest, src, n);
        }
    } else {
        memcpy(dest, src, n); /* Can't overlap; use faster function. */
    }
}

/*
 * public static void arraycopy(Object src, int srcPos, Object dest,
 *      int destPos, int length)
 *
 * The description of this function is long, and describes a multitude
 * of checks and exceptions.
 */
static void Dalvik_java_lang_System_arraycopy(const u4* args, JValue* pResult)
{
    ArrayObject* srcArray;
    ArrayObject* dstArray;
    ClassObject* srcClass;
    ClassObject* dstClass;
    int srcPos, dstPos, length;
    char srcType, dstType;
    bool srcPrim, dstPrim;
    bool sameArray;

    srcArray = (ArrayObject*) args[0];
    srcPos = args[1];
    dstArray = (ArrayObject*) args[2];
    dstPos = args[3];
    length = args[4];

    sameArray = (srcArray == dstArray);

    /* check for null or bad pointer */
    if (!dvmValidateObject((Object*)srcArray) ||
        !dvmValidateObject((Object*)dstArray))
    {
        assert(dvmCheckException(dvmThreadSelf()));
        RETURN_VOID();
    }
    /* make sure it's an array */
    if (!dvmIsArray(srcArray) || !dvmIsArray(dstArray)) {
        dvmThrowExceptionFmt("Ljava/lang/ArrayStoreException;",
            "source and destination must be arrays, but were %s and %s",
            ((Object*)srcArray)->clazz->descriptor,
            ((Object*)dstArray)->clazz->descriptor);
        RETURN_VOID();
    }

    // avoid int overflow
    if (srcPos < 0 || dstPos < 0 || length < 0 ||
        srcPos > (int) srcArray->length - length ||
        dstPos > (int) dstArray->length - length)
    {
        dvmThrowExceptionFmt("Ljava/lang/ArrayIndexOutOfBoundsException;",
            "src.length=%d srcPos=%d dst.length=%d dstPos=%d length=%d",
            srcArray->length, srcPos, dstArray->length, dstPos, length);
        RETURN_VOID();
    }

    srcClass = srcArray->obj.clazz;
    dstClass = dstArray->obj.clazz;
    srcType = srcClass->descriptor[1];
    dstType = dstClass->descriptor[1];

    /*
     * If one of the arrays holds a primitive type, the other array must
     * hold the same type.
     */
    srcPrim = (srcType != '[' && srcType != 'L');
    dstPrim = (dstType != '[' && dstType != 'L');
    if (srcPrim || dstPrim) {
        int width;

        if (srcPrim != dstPrim || srcType != dstType) {
            dvmThrowExceptionFmt("Ljava/lang/ArrayStoreException;",
                "source and destination arrays are incompatible: %s and %s",
                srcClass->descriptor, dstClass->descriptor);
            RETURN_VOID();
        }

        switch (srcClass->descriptor[1]) {
        case 'B':
        case 'Z':
            width = 1;
            break;
        case 'C':
        case 'S':
            width = 2;
            break;
        case 'F':
        case 'I':
            width = 4;
            break;
        case 'D':
        case 'J':
            width = 8;
            break;
        default:        /* 'V' or something weird */
            LOGE("Weird array type '%s'\n", srcClass->descriptor);
            assert(false);
            width = 0;
            break;
        }

        if (false) LOGVV("arraycopy prim dst=%p %d src=%p %d len=%d\n",
                dstArray->contents, dstPos * width,
                srcArray->contents, srcPos * width,
                length * width);
        copy((u1*)dstArray->contents + dstPos * width,
                (const u1*)srcArray->contents + srcPos * width,
                length * width,
                sameArray, width);
    } else {
        /*
         * Neither class is primitive.  See if elements in "src" are instances
         * of elements in "dst" (e.g. copy String to String or String to
         * Object).
         */
        int width = sizeof(Object*);

        if (srcClass->arrayDim == dstClass->arrayDim &&
            dvmInstanceof(srcClass, dstClass))
        {
            /*
             * "dst" can hold "src"; copy the whole thing.
             */
            if (false) LOGVV("arraycopy ref dst=%p %d src=%p %d len=%d\n",
                dstArray->contents, dstPos * width,
                srcArray->contents, srcPos * width,
                length * width);
            copy((u1*)dstArray->contents + dstPos * width,
                    (const u1*)srcArray->contents + srcPos * width,
                    length * width,
                    sameArray, width);
            dvmWriteBarrierArray(dstArray, dstPos, dstPos+length);
        } else {
            /*
             * The arrays are not fundamentally compatible.  However, we may
             * still be able to do this if the destination object is compatible
             * (e.g. copy Object to String, but the Object being copied is
             * actually a String).  We need to copy elements one by one until
             * something goes wrong.
             *
             * Because of overlapping moves, what we really want to do is
             * compare the types and count up how many we can move, then call
             * memmove() to shift the actual data.  If we just start from the
             * front we could do a smear rather than a move.
             */
            Object** srcObj;
            Object** dstObj;
            int copyCount;
            ClassObject*   clazz = NULL;

            srcObj = ((Object**) srcArray->contents) + srcPos;
            dstObj = ((Object**) dstArray->contents) + dstPos;

            if (length > 0 && srcObj[0] != NULL)
            {
                clazz = srcObj[0]->clazz;
                if (!dvmCanPutArrayElement(clazz, dstClass))
                    clazz = NULL;
            }

            for (copyCount = 0; copyCount < length; copyCount++)
            {
                if (srcObj[copyCount] != NULL &&
                    srcObj[copyCount]->clazz != clazz &&
                    !dvmCanPutArrayElement(srcObj[copyCount]->clazz, dstClass))
                {
                    /* can't put this element into the array */
                    break;
                }
            }

            if (false) LOGVV("arraycopy iref dst=%p %d src=%p %d count=%d of %d\n",
                dstArray->contents, dstPos * width,
                srcArray->contents, srcPos * width,
                copyCount, length);
            copy((u1*)dstArray->contents + dstPos * width,
                    (const u1*)srcArray->contents + srcPos * width,
                    copyCount * width,
                    sameArray, width);
            dvmWriteBarrierArray(dstArray, 0, copyCount);
            if (copyCount != length) {
                dvmThrowExceptionFmt("Ljava/lang/ArrayStoreException;",
                    "source[%d] of type %s cannot be stored in destination array of type %s",
                    copyCount, srcObj[copyCount]->clazz->descriptor,
                    dstClass->descriptor);
                RETURN_VOID();
            }
        }
    }

    RETURN_VOID();
}

/*
 * static long currentTimeMillis()
 *
 * Current time, in miliseconds.  This doesn't need to be internal to the
 * VM, but we're already handling java.lang.System here.
 */
static void Dalvik_java_lang_System_currentTimeMillis(const u4* args,
    JValue* pResult)
{
    struct timeval tv;

    UNUSED_PARAMETER(args);

    gettimeofday(&tv, (struct timezone *) NULL);
    long long when = tv.tv_sec * 1000LL + tv.tv_usec / 1000;

    RETURN_LONG(when);
}

/*
 * static long nanoTime()
 *
 * Current monotonically-increasing time, in nanoseconds.  This doesn't
 * need to be internal to the VM, but we're already handling
 * java.lang.System here.
 */
static void Dalvik_java_lang_System_nanoTime(const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);

    u8 when = dvmGetRelativeTimeNsec();
    RETURN_LONG(when);
}

/*
 * static int identityHashCode(Object x)
 *
 * Returns that hash code that the default hashCode()
 * method would return for "x", even if "x"s class
 * overrides hashCode().
 */
static void Dalvik_java_lang_System_identityHashCode(const u4* args,
    JValue* pResult)
{
    Object* thisPtr = (Object*) args[0];
    RETURN_INT(dvmIdentityHashCode(thisPtr));
}

/*
 * public static String mapLibraryName(String libname)
 */
static void Dalvik_java_lang_System_mapLibraryName(const u4* args,
    JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    StringObject* result = NULL;
    char* name;
    char* mappedName;

    if (nameObj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        RETURN_VOID();
    }

    name = dvmCreateCstrFromString(nameObj);
    mappedName = dvmCreateSystemLibraryName(name);
    if (mappedName != NULL) {
        result = dvmCreateStringFromCstr(mappedName);
        dvmReleaseTrackedAlloc((Object*) result, NULL);
    }

    free(name);
    free(mappedName);
    RETURN_PTR(result);
}

const DalvikNativeMethod dvm_java_lang_System[] = {
    { "arraycopy",          "(Ljava/lang/Object;ILjava/lang/Object;II)V",
        Dalvik_java_lang_System_arraycopy },
    { "currentTimeMillis",  "()J",
        Dalvik_java_lang_System_currentTimeMillis },
    { "nanoTime",  "()J",
        Dalvik_java_lang_System_nanoTime },
    { "identityHashCode",  "(Ljava/lang/Object;)I",
        Dalvik_java_lang_System_identityHashCode },
    { "mapLibraryName",     "(Ljava/lang/String;)Ljava/lang/String;",
        Dalvik_java_lang_System_mapLibraryName },
    { NULL, NULL, NULL },
};
