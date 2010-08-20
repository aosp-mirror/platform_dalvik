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
 * UTF-8 and Unicode string manipulation, plus java/lang/String convenience
 * functions.
 *
 * In most cases we populate the fields in the String object directly,
 * rather than going through an instance field lookup.
 */
#include "Dalvik.h"
#include <stdlib.h>

/*
 * Initialize string globals.
 *
 * This isn't part of the VM init sequence because it's hard to get the
 * timing right -- we need it to happen after java/lang/String has been
 * loaded, but before anybody wants to use a string.  It's easiest to
 * just initialize it on first use.
 *
 * In some unusual circumstances (e.g. trying to throw an exception because
 * String implements java/lang/CharSequence, but CharSequence doesn't exist)
 * we can try to create an exception string internally before anything has
 * really tried to use String.  In that case we basically self-destruct.
 *
 * We're expecting to be essentially single-threaded at this point.
 * We employ atomics to ensure everything is observed correctly, and also
 * to guarantee that we do detect a problem if our assumption is wrong.
 */
static bool stringStartup()
{
    if (gDvm.javaLangStringReady < 0) {
        LOGE("ERROR: reentrant string initialization\n");
        assert(false);
        return false;
    }

    if (android_atomic_acquire_cas(0, -1, &gDvm.javaLangStringReady) != 0) {
        LOGE("ERROR: initial string-ready state not 0 (%d)\n",
            gDvm.javaLangStringReady);
        return false;
    }

    if (gDvm.classJavaLangString == NULL)
        gDvm.classJavaLangString =
            dvmFindSystemClassNoInit("Ljava/lang/String;");

    gDvm.offJavaLangString_value =
        dvmFindFieldOffset(gDvm.classJavaLangString, "value", "[C");
    gDvm.offJavaLangString_count =
        dvmFindFieldOffset(gDvm.classJavaLangString, "count", "I");
    gDvm.offJavaLangString_offset =
        dvmFindFieldOffset(gDvm.classJavaLangString, "offset", "I");
    gDvm.offJavaLangString_hashCode =
        dvmFindFieldOffset(gDvm.classJavaLangString, "hashCode", "I");

    if (gDvm.offJavaLangString_value < 0 ||
        gDvm.offJavaLangString_count < 0 ||
        gDvm.offJavaLangString_offset < 0 ||
        gDvm.offJavaLangString_hashCode < 0)
    {
        LOGE("VM-required field missing from java/lang/String\n");
        return false;
    }

    bool badValue = false;
    if (gDvm.offJavaLangString_value != STRING_FIELDOFF_VALUE) {
        LOGE("InlineNative: String.value offset = %d, expected %d\n",
            gDvm.offJavaLangString_value, STRING_FIELDOFF_VALUE);
        badValue = true;
    }
    if (gDvm.offJavaLangString_count != STRING_FIELDOFF_COUNT) {
        LOGE("InlineNative: String.count offset = %d, expected %d\n",
            gDvm.offJavaLangString_count, STRING_FIELDOFF_COUNT);
        badValue = true;
    }
    if (gDvm.offJavaLangString_offset != STRING_FIELDOFF_OFFSET) {
        LOGE("InlineNative: String.offset offset = %d, expected %d\n",
            gDvm.offJavaLangString_offset, STRING_FIELDOFF_OFFSET);
        badValue = true;
    }
    if (gDvm.offJavaLangString_hashCode != STRING_FIELDOFF_HASHCODE) {
        LOGE("InlineNative: String.hashCode offset = %d, expected %d\n",
            gDvm.offJavaLangString_hashCode, STRING_FIELDOFF_HASHCODE);
        badValue = true;
    }
    if (badValue)
        return false;

    android_atomic_release_store(1, &gDvm.javaLangStringReady);

    return true;
}

/*
 * Discard heap-allocated storage.
 */
void dvmStringShutdown()
{
    // currently unused
}

/*
 * Compute a hash code on a UTF-8 string, for use with internal hash tables.
 *
 * This may or may not yield the same results as the java/lang/String
 * computeHashCode() function.  (To make sure this doesn't get abused,
 * I'm initializing the hash code to 1 so they *don't* match up.)
 *
 * It would be more correct to invoke dexGetUtf16FromUtf8() here and compute
 * the hash with the result.  That way, if something encoded the same
 * character in two different ways, the hash value would be the same.  For
 * our purposes that isn't necessary.
 */
u4 dvmComputeUtf8Hash(const char* utf8Str)
{
    u4 hash = 1;

    while (*utf8Str != '\0')
        hash = hash * 31 + *utf8Str++;

    return hash;
}

/*
 * Like "strlen", but for strings encoded with "modified" UTF-8.
 *
 * The value returned is the number of characters, which may or may not
 * be the same as the number of bytes.
 *
 * (If this needs optimizing, try: mask against 0xa0, shift right 5,
 * get increment {1-3} from table of 8 values.)
 */
int dvmUtf8Len(const char* utf8Str)
{
    int ic, len = 0;

    while ((ic = *utf8Str++) != '\0') {
        len++;
        if ((ic & 0x80) != 0) {
            /* two- or three-byte encoding */
            utf8Str++;
            if ((ic & 0x20) != 0) {
                /* three-byte encoding */
                utf8Str++;
            }
        }
    }

    return len;
}

/*
 * Convert a "modified" UTF-8 string to UTF-16.
 */
void dvmConvertUtf8ToUtf16(u2* utf16Str, const char* utf8Str)
{
    while (*utf8Str != '\0')
        *utf16Str++ = dexGetUtf16FromUtf8(&utf8Str);
}

/*
 * Given a UTF-16 string, compute the length of the corresponding UTF-8
 * string in bytes.
 */
static int utf16_utf8ByteLen(const u2* utf16Str, int len)
{
    int utf8Len = 0;

    while (len--) {
        unsigned int uic = *utf16Str++;

        /*
         * The most common case is (uic > 0 && uic <= 0x7f).
         */
        if (uic == 0 || uic > 0x7f) {
            if (uic > 0x07ff)
                utf8Len += 3;
            else /*(uic > 0x7f || uic == 0) */
                utf8Len += 2;
        } else
            utf8Len++;
    }
    return utf8Len;
}

/*
 * Convert a UTF-16 string to UTF-8.
 *
 * Make sure you allocate "utf8Str" with the result of utf16_utf8ByteLen(),
 * not just "len".
 */
static void convertUtf16ToUtf8(char* utf8Str, const u2* utf16Str, int len)
{
    assert(len >= 0);

    while (len--) {
        unsigned int uic = *utf16Str++;

        /*
         * The most common case is (uic > 0 && uic <= 0x7f).
         */
        if (uic == 0 || uic > 0x7f) {
            if (uic > 0x07ff) {
                *utf8Str++ = (uic >> 12) | 0xe0;
                *utf8Str++ = ((uic >> 6) & 0x3f) | 0x80;
                *utf8Str++ = (uic & 0x3f) | 0x80;
            } else /*(uic > 0x7f || uic == 0)*/ {
                *utf8Str++ = (uic >> 6) | 0xc0;
                *utf8Str++ = (uic & 0x3f) | 0x80;
            }
        } else {
            *utf8Str++ = uic;
        }
    }

    *utf8Str = '\0';
}

/*
 * Use the java/lang/String.computeHashCode() algorithm.
 */
static inline u4 dvmComputeUtf16Hash(const u2* utf16Str, int len)
{
    u4 hash = 0;

    while (len--)
        hash = hash * 31 + *utf16Str++;

    return hash;
}
u4 dvmComputeStringHash(const StringObject* strObj) {
    ArrayObject* chars = (ArrayObject*) dvmGetFieldObject((Object*) strObj,
                                STRING_FIELDOFF_VALUE);
    int offset, len;

    len = dvmGetFieldInt((Object*) strObj, STRING_FIELDOFF_COUNT);
    offset = dvmGetFieldInt((Object*) strObj, STRING_FIELDOFF_OFFSET);

    return dvmComputeUtf16Hash((u2*) chars->contents + offset, len);
}

/*
 * Create a new java/lang/String object, using the string data in "utf8Str".
 *
 * The caller must call dvmReleaseTrackedAlloc() on the return value.
 *
 * Returns NULL and throws an exception on failure.
 */
StringObject* dvmCreateStringFromCstr(const char* utf8Str)
{
    assert(utf8Str != NULL);
    return dvmCreateStringFromCstrAndLength(utf8Str, dvmUtf8Len(utf8Str));
}

/*
 * Create a java/lang/String from a C string, given its UTF-16 length
 * (number of UTF-16 code points).
 *
 * The caller must call dvmReleaseTrackedAlloc() on the return value.
 *
 * Returns NULL and throws an exception on failure.
 */
StringObject* dvmCreateStringFromCstrAndLength(const char* utf8Str,
    u4 utf16Length)
{
    StringObject* newObj;
    ArrayObject* chars;
    u4 hashCode = 0;

    //LOGV("Creating String from '%s'\n", utf8Str);
    assert(utf8Str != NULL);

    if (gDvm.javaLangStringReady <= 0) {
        if (!stringStartup())
            return NULL;
    }

    /* init before alloc */
    if (!dvmIsClassInitialized(gDvm.classJavaLangString) &&
        !dvmInitClass(gDvm.classJavaLangString))
    {
        return NULL;
    }

    newObj = (StringObject*) dvmAllocObject(gDvm.classJavaLangString,
                ALLOC_DEFAULT);
    if (newObj == NULL)
        return NULL;

    chars = dvmAllocPrimitiveArray('C', utf16Length, ALLOC_DEFAULT);
    if (chars == NULL) {
        dvmReleaseTrackedAlloc((Object*) newObj, NULL);
        return NULL;
    }
    dvmConvertUtf8ToUtf16((u2*)chars->contents, utf8Str);
    hashCode = dvmComputeUtf16Hash((u2*) chars->contents, utf16Length);

    dvmSetFieldObject((Object*)newObj, STRING_FIELDOFF_VALUE,
        (Object*)chars);
    dvmReleaseTrackedAlloc((Object*) chars, NULL);
    dvmSetFieldInt((Object*)newObj, STRING_FIELDOFF_COUNT, utf16Length);
    dvmSetFieldInt((Object*)newObj, STRING_FIELDOFF_HASHCODE, hashCode);
    /* leave offset set to zero */

    /* debugging stuff */
    //dvmDumpObject((Object*)newObj);
    //printHexDumpEx(ANDROID_LOG_DEBUG, chars->contents, utf16Length * 2,
    //    kHexDumpMem);

    /* caller may need to dvmReleaseTrackedAlloc(newObj) */
    return newObj;
}

/*
 * Create a new java/lang/String object, using the Unicode data.
 */
StringObject* dvmCreateStringFromUnicode(const u2* unichars, int len)
{
    StringObject* newObj;
    ArrayObject* chars;
    u4 hashCode = 0;

    /* we allow a null pointer if the length is zero */
    assert(len == 0 || unichars != NULL);

    if (gDvm.javaLangStringReady <= 0) {
        if (!stringStartup())
            return NULL;
    }

    /* init before alloc */
    if (!dvmIsClassInitialized(gDvm.classJavaLangString) &&
        !dvmInitClass(gDvm.classJavaLangString))
    {
        return NULL;
    }

    newObj = (StringObject*) dvmAllocObject(gDvm.classJavaLangString,
        ALLOC_DEFAULT);
    if (newObj == NULL)
        return NULL;

    chars = dvmAllocPrimitiveArray('C', len, ALLOC_DEFAULT);
    if (chars == NULL) {
        dvmReleaseTrackedAlloc((Object*) newObj, NULL);
        return NULL;
    }
    if (len > 0)
        memcpy(chars->contents, unichars, len * sizeof(u2));
    hashCode = dvmComputeUtf16Hash((u2*) chars->contents, len);

    dvmSetFieldObject((Object*)newObj, STRING_FIELDOFF_VALUE,
        (Object*)chars);
    dvmReleaseTrackedAlloc((Object*) chars, NULL);
    dvmSetFieldInt((Object*)newObj, STRING_FIELDOFF_COUNT, len);
    dvmSetFieldInt((Object*)newObj, STRING_FIELDOFF_HASHCODE, hashCode);
    /* leave offset set to zero */

    /* debugging stuff */
    //dvmDumpObject((Object*)newObj);
    //printHexDumpEx(ANDROID_LOG_DEBUG, chars->contents, len*2, kHexDumpMem);

    /* caller must dvmReleaseTrackedAlloc(newObj) */
    return newObj;
}

/*
 * Create a new C string from a java/lang/String object.
 *
 * Returns NULL if the object is NULL.
 */
char* dvmCreateCstrFromString(StringObject* jstr)
{
    char* newStr;
    ArrayObject* chars;
    int len, byteLen, offset;
    const u2* data;

    assert(gDvm.javaLangStringReady > 0);

    if (jstr == NULL)
        return NULL;

    len = dvmGetFieldInt((Object*) jstr, STRING_FIELDOFF_COUNT);
    offset = dvmGetFieldInt((Object*) jstr, STRING_FIELDOFF_OFFSET);
    chars = (ArrayObject*) dvmGetFieldObject((Object*) jstr,
                                STRING_FIELDOFF_VALUE);
    data = (const u2*) chars->contents + offset;
    assert(offset + len <= (int) chars->length);

    byteLen = utf16_utf8ByteLen(data, len);
    newStr = (char*) malloc(byteLen+1);
    if (newStr == NULL)
        return NULL;
    convertUtf16ToUtf8(newStr, data, len);

    return newStr;
}

/*
 * Create a UTF-8 C string from a region of a java/lang/String.  (Used by
 * the JNI GetStringUTFRegion call.)
 */
void dvmCreateCstrFromStringRegion(StringObject* jstr, int start, int len,
    char* buf)
{
    const u2* data;

    data = dvmStringChars(jstr) + start;
    convertUtf16ToUtf8(buf, data, len);
}

/*
 * Compute the length, in modified UTF-8, of a java/lang/String object.
 *
 * Does not include the terminating null byte.
 */
int dvmStringUtf8ByteLen(StringObject* jstr)
{
    ArrayObject* chars;
    int len, offset;
    const u2* data;

    assert(gDvm.javaLangStringReady > 0);

    if (jstr == NULL)
        return 0;       // should we throw something?  assert?

    len = dvmGetFieldInt((Object*) jstr, STRING_FIELDOFF_COUNT);
    offset = dvmGetFieldInt((Object*) jstr, STRING_FIELDOFF_OFFSET);
    chars = (ArrayObject*) dvmGetFieldObject((Object*) jstr,
                                STRING_FIELDOFF_VALUE);
    data = (const u2*) chars->contents + offset;
    assert(offset + len <= (int) chars->length);

    return utf16_utf8ByteLen(data, len);
}

/*
 * Get the string's length.
 */
int dvmStringLen(StringObject* jstr)
{
    return dvmGetFieldInt((Object*) jstr, STRING_FIELDOFF_COUNT);
}

/*
 * Get the char[] object from the String.
 */
ArrayObject* dvmStringCharArray(StringObject* jstr)
{
    return (ArrayObject*) dvmGetFieldObject((Object*) jstr,
                                STRING_FIELDOFF_VALUE);
}

/*
 * Get the string's data.
 */
const u2* dvmStringChars(StringObject* jstr)
{
    ArrayObject* chars;
    int offset;

    offset = dvmGetFieldInt((Object*) jstr, STRING_FIELDOFF_OFFSET);
    chars = (ArrayObject*) dvmGetFieldObject((Object*) jstr,
                                STRING_FIELDOFF_VALUE);
    return (const u2*) chars->contents + offset;
}


/*
 * Compare two String objects.
 *
 * This is a dvmHashTableLookup() callback.  The function has already
 * compared their hash values; we need to do a full compare to ensure
 * that the strings really match.
 */
int dvmHashcmpStrings(const void* vstrObj1, const void* vstrObj2)
{
    const StringObject* strObj1 = (const StringObject*) vstrObj1;
    const StringObject* strObj2 = (const StringObject*) vstrObj2;
    ArrayObject* chars1;
    ArrayObject* chars2;
    int len1, len2, offset1, offset2;

    assert(gDvm.javaLangStringReady > 0);

    /* get offset and length into char array; all values are in 16-bit units */
    len1 = dvmGetFieldInt((Object*) strObj1, STRING_FIELDOFF_COUNT);
    offset1 = dvmGetFieldInt((Object*) strObj1, STRING_FIELDOFF_OFFSET);
    len2 = dvmGetFieldInt((Object*) strObj2, STRING_FIELDOFF_COUNT);
    offset2 = dvmGetFieldInt((Object*) strObj2, STRING_FIELDOFF_OFFSET);
    if (len1 != len2)
        return len1 - len2;

    chars1 = (ArrayObject*) dvmGetFieldObject((Object*) strObj1,
                                STRING_FIELDOFF_VALUE);
    chars2 = (ArrayObject*) dvmGetFieldObject((Object*) strObj2,
                                STRING_FIELDOFF_VALUE);

    /* damage here actually indicates a broken java/lang/String */
    assert(offset1 + len1 <= (int) chars1->length);
    assert(offset2 + len2 <= (int) chars2->length);

    return memcmp((const u2*) chars1->contents + offset1,
                  (const u2*) chars2->contents + offset2,
                  len1 * sizeof(u2));
}
