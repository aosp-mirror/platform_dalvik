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
 * java.lang.Class native methods
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"

/*
 * The VM makes guarantees about the atomicity of accesses to primitive
 * variables.  These guarantees also apply to elements of arrays.
 * In particular, 8-bit, 16-bit, and 32-bit accesses must be atomic and
 * must not cause "word tearing".  Accesses to 64-bit array elements must
 * either be atomic or treated as two 32-bit operations.  References are
 * always read and written atomically, regardless of the number of bits
 * used to represent them.
 *
 * We can't rely on standard libc functions like memcpy() and memmove()
 * in our implementation of System.arraycopy(), because they may copy
 * byte-by-byte (either for the full run or for "unaligned" parts at the
 * start or end).  We need to use functions that guarantee 16-bit or 32-bit
 * atomicity as appropriate.
 *
 * System.arraycopy() is heavily used, so having an efficient implementation
 * is important.  The bionic libc provides a platform-optimized memory move
 * function that should be used when possible.  If it's not available,
 * the trivial "reference implementation" versions below can be used until
 * a proper version can be written.
 *
 * For these functions, The caller must guarantee that dest/src are aligned
 * appropriately for the element type, and that n is a multiple of the
 * element size.
 */
#ifdef __BIONIC__
/* always present in bionic libc */
#define HAVE_MEMMOVE_WORDS
#endif

#ifdef HAVE_MEMMOVE_WORDS
extern "C" void _memmove_words(void* dest, const void* src, size_t n);
#define move16 _memmove_words
#define move32 _memmove_words
#else
static void move16(void* dest, const void* src, size_t n)
{
    assert((((uintptr_t) dest | (uintptr_t) src | n) & 0x01) == 0);

    uint16_t* d = (uint16_t*) dest;
    const uint16_t* s = (uint16_t*) src;

    n /= sizeof(uint16_t);

    if (d < s) {
        /* copy forward */
        while (n--) {
            *d++ = *s++;
        }
    } else {
        /* copy backward */
        d += n;
        s += n;
        while (n--) {
            *--d = *--s;
        }
    }
}

static void move32(void* dest, const void* src, size_t n)
{
    assert((((uintptr_t) dest | (uintptr_t) src | n) & 0x03) == 0);

    uint32_t* d = (uint32_t*) dest;
    const uint32_t* s = (uint32_t*) src;

    n /= sizeof(uint32_t);

    if (d < s) {
        /* copy forward */
        while (n--) {
            *d++ = *s++;
        }
    } else {
        /* copy backward */
        d += n;
        s += n;
        while (n--) {
            *--d = *--s;
        }
    }
}
#endif /*HAVE_MEMMOVE_WORDS*/

/*
 * public static void arraycopy(Object src, int srcPos, Object dest,
 *      int destPos, int length)
 *
 * The description of this function is long, and describes a multitude
 * of checks and exceptions.
 */
static void Dalvik_java_lang_System_arraycopy(const u4* args, JValue* pResult)
{
    ArrayObject* srcArray = (ArrayObject*) args[0];
    int srcPos = args[1];
    ArrayObject* dstArray = (ArrayObject*) args[2];
    int dstPos = args[3];
    int length = args[4];

    /* Check for null pointers. */
    if (srcArray == NULL) {
        dvmThrowNullPointerException("src == null");
        RETURN_VOID();
    }
    if (dstArray == NULL) {
        dvmThrowNullPointerException("dst == null");
        RETURN_VOID();
    }

    /* Make sure source and destination are arrays. */
    if (!dvmIsArray(srcArray)) {
        dvmThrowArrayStoreExceptionNotArray(((Object*)srcArray)->clazz, "source");
        RETURN_VOID();
    }
    if (!dvmIsArray(dstArray)) {
        dvmThrowArrayStoreExceptionNotArray(((Object*)dstArray)->clazz, "destination");
        RETURN_VOID();
    }

    /* avoid int overflow */
    if (srcPos < 0 || dstPos < 0 || length < 0 ||
        srcPos > (int) srcArray->length - length ||
        dstPos > (int) dstArray->length - length)
    {
        dvmThrowExceptionFmt(gDvm.exArrayIndexOutOfBoundsException,
            "src.length=%d srcPos=%d dst.length=%d dstPos=%d length=%d",
            srcArray->length, srcPos, dstArray->length, dstPos, length);
        RETURN_VOID();
    }

    ClassObject* srcClass = srcArray->clazz;
    ClassObject* dstClass = dstArray->clazz;
    char srcType = srcClass->descriptor[1];
    char dstType = dstClass->descriptor[1];

    /*
     * If one of the arrays holds a primitive type, the other array must
     * hold the same type.
     */
    bool srcPrim = (srcType != '[' && srcType != 'L');
    bool dstPrim = (dstType != '[' && dstType != 'L');
    if (srcPrim || dstPrim) {
        if (srcPrim != dstPrim || srcType != dstType) {
            dvmThrowArrayStoreExceptionIncompatibleArrays(srcClass, dstClass);
            RETURN_VOID();
        }

        if (false) LOGD("arraycopy prim[%c] dst=%p %d src=%p %d len=%d",
            srcType, dstArray->contents, dstPos,
            srcArray->contents, srcPos, length);

        switch (srcType) {
        case 'B':
        case 'Z':
            /* 1 byte per element */
            memmove((u1*) dstArray->contents + dstPos,
                (const u1*) srcArray->contents + srcPos,
                length);
            break;
        case 'C':
        case 'S':
            /* 2 bytes per element */
            move16((u1*) dstArray->contents + dstPos * 2,
                (const u1*) srcArray->contents + srcPos * 2,
                length * 2);
            break;
        case 'F':
        case 'I':
            /* 4 bytes per element */
            move32((u1*) dstArray->contents + dstPos * 4,
                (const u1*) srcArray->contents + srcPos * 4,
                length * 4);
            break;
        case 'D':
        case 'J':
            /*
             * 8 bytes per element.  We don't need to guarantee atomicity
             * of the entire 64-bit word, so we can use the 32-bit copier.
             */
            move32((u1*) dstArray->contents + dstPos * 8,
                (const u1*) srcArray->contents + srcPos * 8,
                length * 8);
            break;
        default:        /* illegal array type */
            LOGE("Weird array type '%s'", srcClass->descriptor);
            dvmAbort();
        }
    } else {
        /*
         * Neither class is primitive.  See if elements in "src" are instances
         * of elements in "dst" (e.g. copy String to String or String to
         * Object).
         */
        const int width = sizeof(Object*);

        if (srcClass->arrayDim == dstClass->arrayDim &&
            dvmInstanceof(srcClass, dstClass))
        {
            /*
             * "dst" can hold "src"; copy the whole thing.
             */
            if (false) LOGD("arraycopy ref dst=%p %d src=%p %d len=%d",
                dstArray->contents, dstPos * width,
                srcArray->contents, srcPos * width,
                length * width);
            move32((u1*)dstArray->contents + dstPos * width,
                (const u1*)srcArray->contents + srcPos * width,
                length * width);
            dvmWriteBarrierArray(dstArray, dstPos, dstPos+length);
        } else {
            /*
             * The arrays are not fundamentally compatible.  However, we
             * may still be able to do this if the destination object is
             * compatible (e.g. copy Object[] to String[], but the Object
             * being copied is actually a String).  We need to copy elements
             * one by one until something goes wrong.
             *
             * Because of overlapping moves, what we really want to do
             * is compare the types and count up how many we can move,
             * then call move32() to shift the actual data.  If we just
             * start from the front we could do a smear rather than a move.
             */
            Object** srcObj;
            int copyCount;
            ClassObject*   clazz = NULL;

            srcObj = ((Object**)(void*)srcArray->contents) + srcPos;

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

            if (false) LOGD("arraycopy iref dst=%p %d src=%p %d count=%d of %d",
                dstArray->contents, dstPos * width,
                srcArray->contents, srcPos * width,
                copyCount, length);
            move32((u1*)dstArray->contents + dstPos * width,
                (const u1*)srcArray->contents + srcPos * width,
                copyCount * width);
            dvmWriteBarrierArray(dstArray, 0, copyCount);
            if (copyCount != length) {
                dvmThrowArrayStoreExceptionIncompatibleArrayElement(srcPos + copyCount,
                        srcObj[copyCount]->clazz, dstClass);
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

static void Dalvik_java_lang_System_mapLibraryName(const u4* args,
    JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    StringObject* result = NULL;
    char* name;
    char* mappedName;

    if (nameObj == NULL) {
        dvmThrowNullPointerException("userLibName == null");
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
    { "identityHashCode",  "(Ljava/lang/Object;)I",
        Dalvik_java_lang_System_identityHashCode },
    { "mapLibraryName",     "(Ljava/lang/String;)Ljava/lang/String;",
        Dalvik_java_lang_System_mapLibraryName },
    { "nanoTime",  "()J",
        Dalvik_java_lang_System_nanoTime },
    { NULL, NULL, NULL },
};
