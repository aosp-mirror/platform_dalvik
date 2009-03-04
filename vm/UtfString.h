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
 * UTF-8 and Unicode string manipulation functions, plus convenience
 * functions for working with java/lang/String.
 */
#ifndef _DALVIK_STRING
#define _DALVIK_STRING

/*
 * Hash function for modified UTF-8 strings.
 */
u4 dvmComputeUtf8Hash(const char* str);

/*
 * Hash function for string objects.
 */
u4 dvmComputeStringHash(StringObject* strObj);

/*
 * Create a java/lang/String from a C string.
 *
 * The caller must call dvmReleaseTrackedAlloc() on the return value or
 * use a non-default value for "allocFlags".  It is never appropriate
 * to use ALLOC_DONT_TRACK with this function.
 *
 * Returns NULL and throws an exception on failure.
 */
StringObject* dvmCreateStringFromCstr(const char* utf8Str, int allocFlags);

/*
 * Create a java/lang/String from a C string, given its UTF-16 length
 * (number of UTF-16 code points).
 *
 * The caller must call dvmReleaseTrackedAlloc() on the return value or
 * use a non-default value for "allocFlags".  It is never appropriate
 * to use ALLOC_DONT_TRACK with this function.
 *
 * Returns NULL and throws an exception on failure.
 */
StringObject* dvmCreateStringFromCstrAndLength(const char* utf8Str,
    u4 utf16Length, int allocFlags);

/*
 * Compute the number of characters in a "modified UTF-8" string.  This will
 * match the result from strlen() so long as there are no multi-byte chars.
 */
int dvmUtf8Len(const char* utf8Str);

/*
 * Convert a UTF-8 string to UTF-16.  "utf16Str" must have enough room
 * to hold the output.
 */
void dvmConvertUtf8ToUtf16(u2* utf16Str, const char* utf8Str);

/*
 * Create a java/lang/String from a Unicode string.
 *
 * The caller must call dvmReleaseTrackedAlloc() on the return value.
 */
StringObject* dvmCreateStringFromUnicode(const u2* unichars, int len);

/*
 * Create a UTF-8 C string from a java/lang/String.  Caller must free
 * the result.
 *
 * Returns NULL if "jstr" is NULL.
 */
char* dvmCreateCstrFromString(StringObject* jstr);

/*
 * Create a UTF-8 C string from a region of a java/lang/String.  (Used by
 * the JNI GetStringUTFRegion call.)
 */
void dvmCreateCstrFromStringRegion(StringObject* jstr, int start, int len,
    char* buf);

/*
 * Compute the length in bytes of the modified UTF-8 representation of a
 * string.
 */
int dvmStringUtf8ByteLen(StringObject* jstr);

/*
 * Get the length in Unicode characters of a string.
 */
int dvmStringLen(StringObject* jstr);

/*
 * Get a pointer to the Unicode data.
 */
const u2* dvmStringChars(StringObject* jstr);

/*
 * Compare two string objects.  (This is a dvmHashTableLookup() callback.)
 */
int dvmHashcmpStrings(const void* vstrObj1, const void* vstrObj2);

#endif /*_DALVIK_STRING*/
