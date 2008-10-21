/*parsingContext->env
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

#define LOG_TAG "ExpatParser"

#include "jni.h"
#include "utils/Log.h"
#include "JNIHelp.h"

#include <string.h>
#include <utils/misc.h>
#include <expat.h>
#include <cutils/jstring.h>

#define BUCKET_COUNT 128

/**
 * Wrapper around an interned string.
 */
typedef struct {

    /** The interned string itself. */
    jstring interned;

    /** UTF-8 equivalent of the interned string. */
    const char* bytes;

    /** Hash code of the interned string. */
    int hash;    
} InternedString;

/**
 * Keeps track of strings between start and end events.
 */
typedef struct {

    jstring* array;
    int capacity;
    int size;
} StringStack;

/**
 * Data passed to parser handler method by the parser.
 */
typedef struct {

    /**
     * The JNI environment for the current thread. This should only be used
     * to keep a reference to the env for use in Expat callbacks.
     */
    JNIEnv* env;

    /** The Java parser object. */
    jobject object;

    /** Buffer for text events. */
    jcharArray buffer;

    /** The size of our buffer in jchars. */
    int bufferSize;

    /** Current attributes. */
    const char** attributes;

    /** Number of attributes. */
    int attributeCount;

    /** True if namespace support is enabled. */
    bool processNamespaces;

    /** Keep track of names. */
    StringStack stringStack;

    /** Cache of interned strings. */
    InternedString** internedStrings[BUCKET_COUNT];
} ParsingContext;

static jmethodID startElementMethod;
static jmethodID endElementMethod;
static jmethodID textMethod;
static jmethodID commentMethod;
static jmethodID startCdataMethod;
static jmethodID endCdataMethod;
static jmethodID startDtdMethod;
static jmethodID endDtdMethod;
static jmethodID startNamespaceMethod;
static jmethodID endNamespaceMethod;
static jmethodID processingInstructionMethod;
static jmethodID handleExternalEntityMethod;
static jmethodID internMethod;
static jclass stringClass;
static jstring emptyString;

/**
 * Throws a NullPointerException.
 *
 * @param msg exception message
 */
static void throw_NullPointerException(JNIEnv* env, const char* msg) {
    jniThrowException(env, "java/lang/NullPointerException", msg);
}

/**
 * Throw a NullPointerException.
 */
static void throw_OutOfMemoryError(JNIEnv* env) {
    jniThrowException(env, "java/lang/OutOfMemoryError", "Out of memory.");
}

/**
 * Calculates a hash code for a null-terminated string. This is *not* equivalent
 * to Java's String.hashCode(). This hashes the bytes while String.hashCode()
 * hashes UTF-16 chars.
 *
 * @param s null-terminated string to hash
 * @returns hash code
 */
static int hashString(const char* s) {
    int hash = 0;
    if (s) {
        while (*s) {
            hash = hash * 31 + *s++;
        }
    }
    return hash;
}

/**
 * Creates a new interned string wrapper. Looks up the interned string
 * representing the given UTF-8 bytes.
 *
 * @param bytes null-terminated string to intern
 * @param hash of bytes
 * @returns wrapper of interned Java string
 */
static InternedString* newInternedString(JNIEnv* env,
        ParsingContext* parsingContext, const char* bytes, int hash) {
    // Allocate a new wrapper.
    InternedString* wrapper
        = (InternedString* ) malloc(sizeof(InternedString));
    if (wrapper == NULL) {
        throw_OutOfMemoryError(env);
        return NULL;
    }

    // Create a copy of the UTF-8 bytes.
    // TODO: sometimes we already know the length. Reuse it if so.
    char* copy = strdup(bytes);
    wrapper->bytes = copy;
    if (wrapper->bytes == NULL) {
        throw_OutOfMemoryError(env);
        free(wrapper);
        return NULL;
    }

    // Save the hash.
    wrapper->hash = hash;

    // To intern a string, we must first create a new string and then call
    // intern() on it. We then keep a global reference to the interned string.
    jstring newString = env->NewStringUTF(bytes);
    if (env->ExceptionCheck()) {
        free(copy);
        free(wrapper);
        return NULL;
    }

    // Call intern().
    jstring interned =
        (jstring) env->CallObjectMethod(newString, internMethod);
    if (env->ExceptionCheck()) {
        free(copy);
        free(wrapper);
        return NULL;
    }

    // Create a global reference to the interned string.
    wrapper->interned = (jstring) env->NewGlobalRef(interned);
    if (env->ExceptionCheck()) {
        free(copy);
        free(wrapper);
        return NULL;
    }

    env->DeleteLocalRef(interned);
    env->DeleteLocalRef(newString);

    return wrapper;
}

/**
 * Allocates a new bucket with one entry.
 *
 * @param entry to store in the bucket
 * @returns a reference to the bucket
 */
static InternedString** newInternedStringBucket(InternedString* entry) {
    InternedString** bucket
        = (InternedString**) malloc(sizeof(InternedString*) * 2);
    if (bucket == NULL) return NULL;

    bucket[0] = entry;
    bucket[1] = NULL;
    return bucket;
}

/**
 * Expands an interned string bucket and adds the given entry. Frees the
 * provided bucket and returns a new one.
 *
 * @param existingBucket the bucket to replace
 * @param entry to add to the bucket
 * @returns a reference to the newly-allocated bucket containing the given entry
 */
static InternedString** expandInternedStringBucket(
        InternedString** existingBucket, InternedString* entry) {
    // Determine the size of the existing bucket.
    int size = 0;
    while (existingBucket[size]) size++;

    // Allocate the new bucket with enough space for one more entry and
    // a null terminator.
    InternedString** newBucket = (InternedString**) realloc(existingBucket,
            sizeof(InternedString*) * (size + 2));
    if (newBucket == NULL) return NULL;

    newBucket[size] = entry;
    newBucket[size + 1] = NULL;

    return newBucket;
}

/**
 * Returns an interned string for the given UTF-8 string.
 *
 * @param bucket to search for s
 * @param s null-terminated string to find
 * @param hash of s
 * @returns interned Java string equivalent of s or null if not found
 */
static jstring findInternedString(InternedString** bucket, const char* s,
        int hash) {
    InternedString* current;
    while ((current = *(bucket++)) != NULL) {
        if (current->hash != hash) continue;
        if (!strcmp(s, current->bytes)) return current->interned;
    }
    return NULL;
}

/**
 * Returns an interned string for the given UTF-8 string.
 *
 * @param s null-terminated string to intern
 * @returns interned Java string equivelent of s or NULL if s is null
 */
static jstring internString(JNIEnv* env, ParsingContext* parsingContext,
        const char* s) {
    if (s == NULL) return NULL;

    int hash = hashString(s);
    int bucketIndex = hash & (BUCKET_COUNT - 1);

    InternedString*** buckets = parsingContext->internedStrings;
    InternedString** bucket = buckets[bucketIndex];
    InternedString* internedString;

    if (bucket) {
        // We have a bucket already. Look for the given string.
        jstring found = findInternedString(bucket, s, hash);
        if (found) {
            // We found it!
            return found;
        }

        // We didn't find it. :(
        // Create a new entry.
        internedString = newInternedString(env, parsingContext, s, hash);
        if (internedString == NULL) return NULL;

        // Expand the bucket.
        bucket = expandInternedStringBucket(bucket, internedString);
        if (bucket == NULL) {
            throw_OutOfMemoryError(env);
            return NULL;
        }

        buckets[bucketIndex] = bucket;

        return internedString->interned;
    } else {
        // We don't even have a bucket yet. Create an entry.
        internedString = newInternedString(env, parsingContext, s, hash);
        if (internedString == NULL) return NULL;

        // Create a new bucket with one entry.
        bucket = newInternedStringBucket(internedString);
        if (bucket == NULL) {
            throw_OutOfMemoryError(env);
            return NULL;
        }

        buckets[bucketIndex] = bucket;

        return internedString->interned;
    }
}

/**
 * Returns an interned string for the given UTF-8 string.
 *
 * @param s string to intern
 * @param length of s
 * @returns interned Java string equivelent of s
 */
static jstring internStringOfLength(JNIEnv* env, ParsingContext* parsingContext,
        const char* s, int length) {
    // Create a null-terminated version.
    char nullTerminated[length + 1];
    memcpy(nullTerminated, s, length);
    nullTerminated[length] = 0;

    return internString(env, parsingContext, nullTerminated);
}

/**
 * Throw an assertion error.
 *
 * @param message to show
 */
static void fail(JNIEnv* env, const char* message) {
    jclass clazz;
    clazz = env->FindClass("java/lang/AssertionError");
    env->ThrowNew(clazz, message);
}

/**
 * Allocates a new parsing context.
 *
 * @param jobject the Java ExpatParser instance
 * @returns a newly-allocated ParsingContext
 */
ParsingContext* newParsingContext(JNIEnv* env, jobject object) {
    ParsingContext* result = (ParsingContext*) malloc(sizeof(ParsingContext));
    if (result == NULL) {
        throw_OutOfMemoryError(env);
        return NULL;
    }

    result->env = NULL;
    result->buffer = NULL;
    result->bufferSize = -1;
    result->object = object;

    int stackSize = 10;
    result->stringStack.capacity = stackSize;
    result->stringStack.size = 0;
    result->stringStack.array
            = (jstring*) malloc(stackSize * sizeof(jstring));

    for (int i = 0; i < BUCKET_COUNT; i++) {
        result->internedStrings[i] = NULL;
    }

    return result;
}

/**
 * Frees the char[] buffer if it exists.
 */
static void freeBuffer(JNIEnv* env, ParsingContext* parsingContext) {
    if (parsingContext->buffer != NULL) {
        env->DeleteGlobalRef(parsingContext->buffer);
        parsingContext->buffer = NULL;
        parsingContext->bufferSize = -1;
    }
}

/**
 * Ensures our buffer is big enough.
 *
 * @param length in jchars
 * @returns a reference to the buffer
 */
static jcharArray ensureCapacity(ParsingContext* parsingContext, int length) {
    if (parsingContext->bufferSize < length) {
        JNIEnv* env = parsingContext->env;

        // Free the existing char[].
        freeBuffer(env, parsingContext);

        // Allocate a new char[].
        jcharArray javaBuffer = env->NewCharArray(length);
        if (javaBuffer == NULL) return NULL;

        // Create a global reference.
        javaBuffer = (jcharArray) env->NewGlobalRef(javaBuffer);
        if (javaBuffer == NULL) return NULL;

        parsingContext->buffer = javaBuffer;
        parsingContext->bufferSize = length;
    }

    return parsingContext->buffer;
}

/**
 * Copies UTF-8 characters into the buffer. Returns the number of Java chars
 * which were buffered.
 *
 * @param characters to copy into the buffer
 * @param length of characters to copy (in bytes)
 * @returns number of UTF-16 characters which were copied
 */
static size_t fillBuffer(ParsingContext* parsingContext, const char* characters,
        int length) {
    JNIEnv* env = parsingContext->env;

    // Grow buffer if necessary.
    jcharArray buffer = ensureCapacity(parsingContext, length);
    if (buffer == NULL) return -1;

    // Get a native reference to our buffer.
    jchar* nativeBuffer = env->GetCharArrayElements(buffer, NULL);

    // Decode UTF-8 characters into our buffer.
    size_t utf16length;
    strcpylen8to16((char16_t*) nativeBuffer, characters, length, &utf16length);

    // Release our native reference.
    env->ReleaseCharArrayElements(buffer, nativeBuffer, JNI_ABORT);

    return utf16length;
}

/**
 * Buffers the given text and passes it to the given method.
 *
 * @param method to pass the characters and length to with signature
 *  (char[], int)
 * @param data parsing context
 * @param text to copy into the buffer
 * @param length of text to copy (in bytes)
 */
static void bufferAndInvoke(jmethodID method, void* data, const char* text,
        size_t length) {
    ParsingContext* parsingContext = (ParsingContext*) data;
    JNIEnv* env = parsingContext->env;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) return;

    // Buffer the element name.
    size_t utf16length = fillBuffer(parsingContext, text, length);

    // Invoke given method.
    jobject javaParser = parsingContext->object;
    jcharArray buffer = parsingContext->buffer;
    env->CallVoidMethod(javaParser, method, buffer, utf16length);
}

/**
 * Finds the '|' in a null-terminated string. Returns -1 if none is found.
 *
 * @param s string to search
 * @returns index of the separator
 */
static int findSeparator(const char* s) {
    char* pointer = strchr(s, '|');
    return pointer == NULL ? -1 : pointer - s;
}

/**
 * Parses the URI out of an Expat element name and turns it into a Java string.
 * Expat element names follow the format: "uri|localName".
 *
 * @param s string to parse from
 * @param separatorIndex index of separator in s
 * @returns interned Java string containing the URI
 */
static jstring parseUri(JNIEnv* env, ParsingContext* parsingContext, const char* s,
        int separatorIndex) {
    return separatorIndex == -1 ? emptyString
        : internStringOfLength(env, parsingContext, s, separatorIndex);
}

/**
 * Parses the local name out of an Expat element name and turns it into a Java
 * string. Expat element names follow the format: "uri|localName".
 *
 * @param s string to parse from
 * @param separatorIndex index of separator in s
 * @returns interned Java string containing the local name
 */
static jstring parseLocalName(JNIEnv* env, ParsingContext* parsingContext,
        const char* s, int separatorIndex) {
    return internString(env, parsingContext, s + separatorIndex + 1);
}

/**
 * Pushes a string onto the stack.
 */
static void stringStackPush(ParsingContext* parsingContext, jstring s) {
    StringStack* stringStack = &parsingContext->stringStack;

    // Expand if necessary.
    if (stringStack->size == stringStack->capacity) {
        int newCapacity = stringStack->capacity * 2;
        stringStack->array = (jstring*) realloc(stringStack->array,
            newCapacity * sizeof(jstring));

        if (stringStack->array == NULL) {
            throw_OutOfMemoryError(parsingContext->env);
            return;
        }

        stringStack->capacity = newCapacity;
    }

    stringStack->array[stringStack->size++] = s;
}

/**
 * Pops a string off the stack.
 */
static jstring stringStackPop(ParsingContext* parsingContext) {
    StringStack* stringStack = &parsingContext->stringStack;

    if (stringStack->size == 0) {
        return NULL;
    }

    return stringStack->array[--stringStack->size];
}

/**
 * Called by Expat at the start of an element. Delegates to the same method
 * on the Java parser.
 *
 * @param data parsing context
 * @param elementName "uri|localName" or "localName" for the current element
 * @param attributes alternating attribute names and values. Like element
 * names, attribute names follow the format "uri|localName" or "localName".
 */
static void startElement(void* data, const char* elementName,
        const char** attributes) {
    ParsingContext* parsingContext = (ParsingContext*) data;
    JNIEnv* env = parsingContext->env;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) return;

    // Count the number of attributes.
    int count = 0;
    while (attributes[count << 1]) count++;

    // Make the attributes available for the duration of this call.
    parsingContext->attributes = attributes;
    parsingContext->attributeCount = count;

    jobject javaParser = parsingContext->object;

    if (parsingContext->processNamespaces) {
        // Break down Expat's element name into two Java strings.
        int separatorIndex = findSeparator(elementName);
        jstring uri = parseUri(
                env, parsingContext, elementName, separatorIndex);
        jstring localName = parseLocalName(
                env, parsingContext, elementName, separatorIndex);

        stringStackPush(parsingContext, uri);
        stringStackPush(parsingContext, localName);

        env->CallVoidMethod(javaParser, startElementMethod, uri, localName,
                emptyString, attributes, count);
    } else {
        jstring qName = internString(env, parsingContext, elementName);

        stringStackPush(parsingContext, qName);

        env->CallVoidMethod(javaParser, startElementMethod,
                emptyString, emptyString, qName, attributes, count);
    }

    parsingContext->attributes = NULL;
    parsingContext->attributeCount = -1;
}

/**
 * Called by Expat at the end of an element. Delegates to the same method
 * on the Java parser.
 *
 * @param data parsing context
 * @param elementName "uri|localName" or "localName" for the current element
 */
static void endElement(void* data, const char* elementName) {
    ParsingContext* parsingContext = (ParsingContext*) data;
    JNIEnv* env = parsingContext->env;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) return;

    jobject javaParser = parsingContext->object;

    if (parsingContext->processNamespaces) {
        jstring localName = stringStackPop(parsingContext);
        jstring uri = stringStackPop(parsingContext);

        env->CallVoidMethod(javaParser, endElementMethod, uri, localName,
                emptyString);
    } else {
        jstring qName = stringStackPop(parsingContext);

        env->CallVoidMethod(javaParser, endElementMethod,
                emptyString, emptyString, qName);
    }
}

/**
 * Called by Expat when it encounters text. Delegates to the same method
 * on the Java parser. This may be called mutiple times with incremental pieces
 * of the same contiguous block of text.
 *
 * @param data parsing context
 * @param characters buffer containing encountered text
 * @param length number of characters in the buffer
 */
static void text(void* data, const char* characters, int length) {
    bufferAndInvoke(textMethod, data, characters, length);
}

/**
 * Called by Expat when it encounters a comment. Delegates to the same method
 * on the Java parser.

 * @param data parsing context
 * @param comment 0-terminated
 */
static void comment(void* data, const char* comment) {
    bufferAndInvoke(commentMethod, data, comment, strlen(comment));
}

/**
 * Called by Expat at the beginning of a namespace mapping.
 *
 * @param data parsing context
 * @param prefix null-terminated namespace prefix used in the XML
 * @param uri of the namespace
 */
static void startNamespace(void* data, const char* prefix, const char* uri) {
    ParsingContext* parsingContext = (ParsingContext*) data;
    JNIEnv* env = parsingContext->env;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) return;

    jstring internedPrefix = emptyString;
    if (prefix != NULL) {
        internedPrefix = internString(env, parsingContext, prefix);
        if (env->ExceptionCheck()) return;
    }

    jstring internedUri = emptyString;
    if (uri != NULL) {
        internedUri = internString(env, parsingContext, uri);
        if (env->ExceptionCheck()) return;
    }

    stringStackPush(parsingContext, internedPrefix);

    jobject javaParser = parsingContext->object;
    env->CallVoidMethod(javaParser, startNamespaceMethod, internedPrefix,
        internedUri);
}

/**
 * Called by Expat at the end of a namespace mapping.
 *
 * @param data parsing context
 * @param prefix null-terminated namespace prefix used in the XML
 */
static void endNamespace(void* data, const char* prefix) {
    ParsingContext* parsingContext = (ParsingContext*) data;
    JNIEnv* env = parsingContext->env;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) return;

    jstring internedPrefix = stringStackPop(parsingContext);

    jobject javaParser = parsingContext->object;
    env->CallVoidMethod(javaParser, endNamespaceMethod, internedPrefix);
}

/**
 * Called by Expat at the beginning of a CDATA section.
 *
 * @param data parsing context
 */
static void startCdata(void* data) {
    ParsingContext* parsingContext = (ParsingContext*) data;
    JNIEnv* env = parsingContext->env;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) return;

    jobject javaParser = parsingContext->object;
    env->CallVoidMethod(javaParser, startCdataMethod);
}

/**
 * Called by Expat at the end of a CDATA section.
 *
 * @param data parsing context
 */
static void endCdata(void* data) {
    ParsingContext* parsingContext = (ParsingContext*) data;
    JNIEnv* env = parsingContext->env;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) return;

    jobject javaParser = parsingContext->object;
    env->CallVoidMethod(javaParser, endCdataMethod);
}

/**
 * Called by Expat at the beginning of a DOCTYPE section.
 *
 * @param data parsing context
 */
static void startDtd(void* data, const char* name,
        const char* systemId, const char* publicId, int hasInternalSubset) {
    ParsingContext* parsingContext = (ParsingContext*) data;
    JNIEnv* env = parsingContext->env;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) return;

    jstring javaName = internString(env, parsingContext, name);
    if (env->ExceptionCheck()) return;

    jstring javaPublicId = internString(env, parsingContext, publicId);
    if (env->ExceptionCheck()) return;

    jstring javaSystemId = internString(env, parsingContext, systemId);
    if (env->ExceptionCheck()) return;

    jobject javaParser = parsingContext->object;
    env->CallVoidMethod(javaParser, startDtdMethod, javaName, javaPublicId,
        javaSystemId);
}

/**
 * Called by Expat at the end of a DOCTYPE section.
 *
 * @param data parsing context
 */
static void endDtd(void* data) {
    ParsingContext* parsingContext = (ParsingContext*) data;
    JNIEnv* env = parsingContext->env;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) return;

    jobject javaParser = parsingContext->object;
    env->CallVoidMethod(javaParser, endDtdMethod);
}

/**
 * Called by Expat when it encounters processing instructions.
 *
 * @param data parsing context
 * @param target of the instruction
 * @param instructionData
 */
static void processingInstruction(void* data, const char* target,
        const char* instructionData) {
    ParsingContext* parsingContext = (ParsingContext*) data;
    JNIEnv* env = parsingContext->env;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) return;

    jstring javaTarget = internString(env, parsingContext, target);
    if (env->ExceptionCheck()) return;

    jstring javaInstructionData = env->NewStringUTF(instructionData);
    if (env->ExceptionCheck()) return;

    jobject javaParser = parsingContext->object;
    env->CallVoidMethod(javaParser, processingInstructionMethod, javaTarget,
        javaInstructionData);

    // We have to temporarily clear an exception before we can release local
    // references.
    jthrowable exception = env->ExceptionOccurred();
    if (exception != NULL) {
        env->ExceptionClear();
    }

    env->DeleteLocalRef(javaInstructionData);

    if (exception != NULL) {
        env->Throw(exception);
    }
}

/**
 * Creates a new entity parser.
 *
 * @param object the Java ExpatParser instance
 * @param parentParser pointer
 * @param javaEncoding the character encoding name
 * @param javaContext that was provided to handleExternalEntity
 * @returns the pointer to the C Expat entity parser
 */
static jint createEntityParser(JNIEnv* env, jobject object, jint parentParser,
        jstring javaEncoding, jstring javaContext) {
    const char* encoding = env->GetStringUTFChars(javaEncoding, NULL);
    if (encoding == NULL) {
        return 0;
    }

    const char* context = env->GetStringUTFChars(javaContext, NULL);
    if (context == NULL) {
        env->ReleaseStringUTFChars(javaEncoding, encoding);
        return 0;
    }

    XML_Parser parent = (XML_Parser) parentParser;
    XML_Parser entityParser
            = XML_ExternalEntityParserCreate(parent, context, NULL);
    env->ReleaseStringUTFChars(javaEncoding, encoding);
    env->ReleaseStringUTFChars(javaContext, context);

    if (entityParser == NULL) {
        throw_OutOfMemoryError(env);
    }

    return (jint) entityParser;
}

/**
 * Handles external entities. We ignore the "base" URI and keep track of it
 * ourselves.
 */
static int handleExternalEntity(XML_Parser parser, const char* context,
        const char* ignored, const char* systemId, const char* publicId) {
    ParsingContext* parsingContext = (ParsingContext*) XML_GetUserData(parser);
    jobject javaParser = parsingContext->object;
    JNIEnv* env = parsingContext->env;
    jobject object = parsingContext->object;

    // Bail out if a previously called handler threw an exception.
    if (env->ExceptionCheck()) {
        return XML_STATUS_ERROR;
    }

    jstring javaSystemId = env->NewStringUTF(systemId);
    if (env->ExceptionCheck()) {
        return XML_STATUS_ERROR;
    }
    jstring javaPublicId = env->NewStringUTF(publicId);
    if (env->ExceptionCheck()) {
        return XML_STATUS_ERROR;
    }
    jstring javaContext = env->NewStringUTF(context);
    if (env->ExceptionCheck()) {
        return XML_STATUS_ERROR;
    }

    // Pass the wrapped parser and both strings to java.
    env->CallVoidMethod(javaParser, handleExternalEntityMethod, javaContext,
        javaPublicId, javaSystemId);

    /*
     * Parsing the external entity leaves parsingContext->env and object set to
     * NULL, so we need to restore both.
     *
     * TODO: consider restoring the original env and object instead of setting
     * them to NULL in the append() functions.
     */
    parsingContext->env = env;
    parsingContext->object = object;

    env->DeleteLocalRef(javaSystemId);
    env->DeleteLocalRef(javaPublicId);
    env->DeleteLocalRef(javaContext);

    return env->ExceptionCheck() ? XML_STATUS_ERROR : XML_STATUS_OK;
}

/**
 * Releases the parsing context.
 */
static void releaseParsingContext(JNIEnv* env, ParsingContext* context) {
    free(context->stringStack.array);

    freeBuffer(env, context);

    // Free interned string cache.
    for (int i = 0; i < BUCKET_COUNT; i++) {
        if (context->internedStrings[i]) {
            InternedString** bucket = context->internedStrings[i];
            InternedString* current;
            while ((current = *(bucket++)) != NULL) {
                // Free the UTF-8 bytes.
                free((void*) (current->bytes));

                // Free the interned string reference.
                env->DeleteGlobalRef(current->interned);

                // Free the bucket.
                free(current);
            }

            // Free the buckets.
            free(context->internedStrings[i]);
        }
    }

    free(context);
}

/**
 * Creates a new Expat parser. Called from the Java ExpatParser contructor.
 *
 * @param object the Java ExpatParser instance
 * @param javaEncoding the character encoding name
 * @param processNamespaces true if the parser should handle namespaces
 * @returns the pointer to the C Expat parser
 */
static jint initialize(JNIEnv* env, jobject object, jstring javaEncoding,
        jboolean processNamespaces) {
    // Allocate parsing context.
    ParsingContext* context = newParsingContext(env, object);
    if (context == NULL) {
        return 0;
    }

    context->processNamespaces = (bool) processNamespaces;

    // Create a parser.
    XML_Parser parser;
    const char* encoding = env->GetStringUTFChars(javaEncoding, NULL);
    if (processNamespaces) {
        // Use '|' to separate URIs from local names.
        parser = XML_ParserCreateNS(encoding, '|');
    } else {
        parser = XML_ParserCreate(encoding);
    }
    env->ReleaseStringUTFChars(javaEncoding, encoding);

    if (parser != NULL) {
        if (processNamespaces) {
            XML_SetNamespaceDeclHandler(parser, startNamespace, endNamespace);
        }

        XML_SetCommentHandler(parser, comment);
        XML_SetCdataSectionHandler(parser, startCdata, endCdata);

        XML_SetElementHandler(parser, startElement, endElement);
        XML_SetCharacterDataHandler(parser, text);
        XML_SetDoctypeDeclHandler(parser, startDtd, endDtd);
        XML_SetProcessingInstructionHandler(parser, processingInstruction);
        XML_SetExternalEntityRefHandler(parser, handleExternalEntity);

        XML_SetUserData(parser, context);
    } else {
        releaseParsingContext(env, context);
        throw_OutOfMemoryError(env);
        return 0;
    }

    return (jint) parser;
}

/**
 * Passes some XML to the parser.
 *
 * @param object the Java ExpatParser instance
 * @param pointer to the C expat parser
 * @param xml Java string containing an XML snippet
 * @param isFinal whether or not this is the last snippet; enables more error
 *  checking, i.e. is the document complete?
 */
static void appendString(JNIEnv* env, jobject object, jint pointer, jstring xml,
        jboolean isFinal) {
    XML_Parser parser = (XML_Parser) pointer;
    ParsingContext* context = (ParsingContext*) XML_GetUserData(parser);
    context->env = env;
    context->object = object;

    int length = env->GetStringLength(xml) << 1; // in bytes
    const jchar* characters = env->GetStringChars(xml, NULL);

    if (!XML_Parse(parser, (char*) characters, length, isFinal)
            && !env->ExceptionCheck()) {
        jclass clazz = env->FindClass("org/apache/harmony/xml/ExpatException");
        const char* errorMessage = XML_ErrorString(XML_GetErrorCode(parser));
        env->ThrowNew(clazz, errorMessage);
    }

    // We have to temporarily clear an exception before we can release local
    // references.
    jthrowable exception = env->ExceptionOccurred();
    if (exception) {
        env->ExceptionClear();
        env->ReleaseStringChars(xml, characters);
        env->Throw(exception);
    } else {
        env->ReleaseStringChars(xml, characters);
    }

    context->object = NULL;
    context->env = NULL;
}

/**
 * Passes some XML to the parser.
 *
 * @param object the Java ExpatParser instance
 * @param pointer to the C expat parser
 * @param xml Java char[] containing XML
 * @param offset into the xml buffer
 * @param length of text in the xml buffer
 */
static void appendCharacters(JNIEnv* env, jobject object, jint pointer,
        jcharArray xml, jint offset, jint length) {
    XML_Parser parser = (XML_Parser) pointer;
    ParsingContext* context = (ParsingContext*) XML_GetUserData(parser);
    context->env = env;
    context->object = object;

    jchar* characters = env->GetCharArrayElements(xml, NULL);

    if (!XML_Parse(parser, ((char*) characters) + (offset << 1),
            length << 1, XML_FALSE) && !env->ExceptionCheck()) {
        jclass clazz = env->FindClass("org/apache/harmony/xml/ExpatException");
        const char* errorMessage = XML_ErrorString(XML_GetErrorCode(parser));
        env->ThrowNew(clazz, errorMessage);
    }

    // We have to temporarily clear an exception before we can release local
    // references.
    jthrowable exception = env->ExceptionOccurred();
    if (exception) {
        env->ExceptionClear();
        env->ReleaseCharArrayElements(xml, characters, JNI_ABORT);
        env->Throw(exception);
    } else {
        env->ReleaseCharArrayElements(xml, characters, JNI_ABORT);
    }

    context->object = NULL;
    context->env = NULL;
}

/**
 * Passes some XML to the parser.
 *
 * @param object the Java ExpatParser instance
 * @param pointer to the C expat parser
 * @param xml Java byte[] containing XML
 * @param offset into the xml buffer
 * @param length of text in the xml buffer
 */
static void appendBytes(JNIEnv* env, jobject object, jint pointer,
        jbyteArray xml, jint offset, jint length) {
    XML_Parser parser = (XML_Parser) pointer;
    ParsingContext* context = (ParsingContext*) XML_GetUserData(parser);
    context->env = env;
    context->object = object;

    jbyte* bytes = env->GetByteArrayElements(xml, NULL);

    if (!XML_Parse(parser, ((char*) bytes) + offset, length, XML_FALSE)
            && !env->ExceptionCheck()) {
        jclass clazz = env->FindClass("org/apache/harmony/xml/ExpatException");
        const char* errorMessage = XML_ErrorString(XML_GetErrorCode(parser));
        env->ThrowNew(clazz, errorMessage);
    }

    // We have to temporarily clear an exception before we can release local
    // references.
    jthrowable exception = env->ExceptionOccurred();
    if (exception) {
        env->ExceptionClear();
        env->ReleaseByteArrayElements(xml, bytes, JNI_ABORT);
        env->Throw(exception);
    } else {
        env->ReleaseByteArrayElements(xml, bytes, JNI_ABORT);
    }

    context->object = NULL;
    context->env = NULL;
}

/**
 * Releases parser only.
 *
 * @param object the Java ExpatParser instance
 * @param i pointer to the C expat parser
 */
static void releaseParser(JNIEnv* env, jobject object, jint i) {
    XML_Parser parser = (XML_Parser) i;
    XML_ParserFree(parser);
}

/**
 * Cleans up after the parser. Called at garbage collection time.
 *
 * @param object the Java ExpatParser instance
 * @param i pointer to the C expat parser
 */
static void release(JNIEnv* env, jobject object, jint i) {
    XML_Parser parser = (XML_Parser) i;

    ParsingContext* context = (ParsingContext*) XML_GetUserData(parser);
    releaseParsingContext(env, context);

    XML_ParserFree(parser);
}

/**
 * Gets the current line.
 *
 * @param object the Java ExpatParser instance
 * @param pointer to the C expat parser
 * @returns current line number
 */
static int line(JNIEnv* env, jobject clazz, jint pointer) {
    XML_Parser parser = (XML_Parser) pointer;
    return XML_GetCurrentLineNumber(parser);
}

/**
 * Gets the current column.
 *
 * @param object the Java ExpatParser instance
 * @param pointer to the C expat parser
 * @returns current column number
 */
static int column(JNIEnv* env, jobject clazz, jint pointer) {
    XML_Parser parser = (XML_Parser) pointer;
    return XML_GetCurrentColumnNumber(parser);
}

/**
 * Gets the URI of the attribute at the given index.
 *
 * @param object Java ExpatParser instance
 * @param pointer to the C expat parser
 * @param attributePointer to the attribute array
 * @param index of the attribute
 * @returns interned Java string containing attribute's URI
 */
static jstring getAttributeURI(JNIEnv* env, jobject clazz, jint pointer,
        jint attributePointer, jint index) {
    XML_Parser parser = (XML_Parser) pointer;
    ParsingContext* context = (ParsingContext*) XML_GetUserData(parser);

    if (!context->processNamespaces) {
        return emptyString;
    }

    const char** attributes = (const char**) attributePointer;

    const char* name = attributes[index << 1];
    int separatorIndex = findSeparator(name);
    return parseUri(env, context, name, separatorIndex);
}

/**
 * Gets the local name of the attribute at the given index.
 *
 * @param object Java ExpatParser instance
 * @param pointer to the C expat parser
 * @param attributePointer to the attribute array
 * @param index of the attribute
 * @returns interned Java string containing attribute's local name
 */
static jstring getAttributeLocalName(JNIEnv* env, jobject clazz, jint pointer,
        jint attributePointer, jint index) {
    XML_Parser parser = (XML_Parser) pointer;
    ParsingContext* context = (ParsingContext*) XML_GetUserData(parser);

    if (!context->processNamespaces) {
        return emptyString;
    }

    const char** attributes = (const char**) attributePointer;
    const char* name = attributes[index << 1];

    int separatorIndex = findSeparator(name);
    return parseLocalName(env, context, name, separatorIndex);
}

/**
 * Gets the qualified name of the attribute at the given index.
 *
 * @param object Java ExpatParser instance
 * @param pointer to the C expat parser
 * @param attributePointer to the attribute array
 * @param index of the attribute
 * @returns interned Java string containing attribute's local name
 */
static jstring getAttributeQName(JNIEnv* env, jobject clazz, jint pointer,
        jint attributePointer, jint index) {
    XML_Parser parser = (XML_Parser) pointer;
    ParsingContext* context = (ParsingContext*) XML_GetUserData(parser);

    if (context->processNamespaces) {
        return emptyString;
    }

    const char** attributes = (const char**) attributePointer;

    const char* name = attributes[index << 1];
    return internString(env, context, name);
}

/**
 * Gets the value of the attribute at the given index.
 *
 * @param object Java ExpatParser instance
 * @param attributePointer to the attribute array
 * @param index of the attribute
 * @returns Java string containing attribute's value
 */
static jstring getAttributeValueByIndex(JNIEnv* env, jobject clazz,
        jint attributePointer, jint index) {
    const char** attributes = (const char**) attributePointer;
    const char* value = attributes[(index << 1) + 1];
    return env->NewStringUTF(value);
}

/**
 * Searches the attributes for the given name and returns the attribute's
 * index or -1 if not found.
 *
 * @param attributes alternating name/value pairs
 * @param name of attribute to look for
 * @returns index of found attribute or -1 if not found
 */
static jint findAttributeByName(const char** attributes,
        const char* name) {
    int index;
    for (index = 0; attributes[index]; index += 2) {
        if (strcmp(attributes[index], name) == 0)
            return index >> 1;
    }

    // Not found.
    return -1;
}

/**
 * Gets the index of the attribute with the given qualified name.
 *
 * @param attributePointer to the attribute array
 * @param qName to look for
 * @returns index of attribute with the given uri and local name or -1 if not
 *  found
 */
static jint getAttributeIndexForQName(JNIEnv* env, jobject clazz,
        jint attributePointer, jstring qName) {
    const char** attributes = (const char**) attributePointer;
    int length = env->GetStringUTFLength(qName);

    const char* qNameBytes = env->GetStringUTFChars(qName, NULL);
    if (qNameBytes == NULL) return -1;
    int found = findAttributeByName(attributes, qNameBytes);
    env->ReleaseStringUTFChars(qName, qNameBytes);
    return found;
}

/**
 * Gets the index of the attribute with the given URI and name.
 *
 * @param attributePointer to the attribute array
 * @param uri to look for
 * @param localName to look for
 * @returns index of attribute with the given uri and local name or -1 if not
 *  found
 */
static jint getAttributeIndex(JNIEnv* env, jobject clazz,
        jint attributePointer, jstring uri, jstring localName) {
    const char** attributes = (const char**) attributePointer;
    int uriLength = env->GetStringUTFLength(uri);

    if (uriLength == 0) {
        // If there's no URI, then a local name works just like a qName.
        return getAttributeIndexForQName(
                env, clazz, attributePointer, localName);
    }

    int localNameLength = env->GetStringUTFLength(localName);

    // Create string in the same format used by Expat: "uri|localName"
    char concatenated[uriLength + localNameLength + 2];

    // Append uri.
    const char* uriBytes = env->GetStringUTFChars(uri, NULL);
    if (uriBytes == NULL) return -1;
    strcpy(concatenated, uriBytes);
    env->ReleaseStringUTFChars(uri, uriBytes);

    // Separarator.
    concatenated[uriLength] = '|';

    // Append local name.
    const char* localNameBytes = env->GetStringUTFChars(localName, NULL);
    if (localNameBytes == NULL) return -1;
    strcpy(concatenated + uriLength + 1, localNameBytes);
    env->ReleaseStringUTFChars(localName, localNameBytes);

    return findAttributeByName(attributes, concatenated);
}

/**
 * Gets the value of the attribute with the given qualified name.
 *
 * @param attributePointer to the attribute array
 * @param uri to look for
 * @param localName to look for
 * @returns value of attribute with the given uri and local name or NULL if not
 *  found
 */
static jstring getAttributeValueForQName(JNIEnv* env, jobject clazz,
        jint attributePointer, jstring qName) {
    jint index = getAttributeIndexForQName(
            env, clazz, attributePointer, qName);
    return index == -1 ? NULL
        : getAttributeValueByIndex(env, clazz, attributePointer, index);
}

/**
 * Gets the value of the attribute with the given URI and name.
 *
 * @param attributePointer to the attribute array
 * @param uri to look for
 * @param localName to look for
 * @returns value of attribute with the given uri and local name or NULL if not
 *  found
 */
static jstring getAttributeValue(JNIEnv* env, jobject clazz,
        jint attributePointer, jstring uri, jstring localName) {
    jint index = getAttributeIndex(
            env, clazz, attributePointer, uri, localName);
    return index == -1 ? NULL
        : getAttributeValueByIndex(env, clazz, attributePointer, index);
}

/**
 * Clones an array of strings. Uses one contiguous block of memory so as to
 * maximize performance.
 */
static char** cloneStrings(const char** source, int count) {
    // Figure out how big the buffer needs to be.
    int arraySize = (count + 1) * sizeof(char*);
    int totalSize = arraySize;
    int stringLengths[count];
    for (int i = 0; i < count; i++) {
        int length = strlen(source[i]);
        stringLengths[i] = length;
        totalSize += length + 1;
    }

    char* buffer = (char*) malloc(totalSize);
    if (buffer == NULL) {
        return NULL;
    }

    // Array is at the beginning of the buffer.
    char** clonedArray = (char**) buffer;
    clonedArray[count] = NULL; // null terminate

    // First string is immediately after.
    char* destinationString = buffer + arraySize;

    for (int i = 0; i < count; i++) {
        const char* sourceString = source[i];
        int stringLength = stringLengths[i];
        memcpy(destinationString, sourceString, stringLength + 1);
        clonedArray[i] = destinationString;
        destinationString += stringLength + 1;
    }

    return clonedArray;
}

/**
 * Clones attributes.
 *
 * @param pointer to char** to clone
 * @param count number of attributes
 */
static jint cloneAttributes(JNIEnv* env, jobject clazz,
        jint pointer, jint count) {
    return (int) cloneStrings((const char**) pointer, count << 1);
}

/**
 * Frees cloned attributes.
 */
static void freeAttributes(JNIEnv* env, jobject clazz, jint pointer) {
    free((void*) pointer);
}

/**
 * Called when we initialize our Java parser class.
 *
 * @param clazz Java ExpatParser class
 */
static void staticInitialize(JNIEnv* env, jobject clazz, jstring empty) {
    startElementMethod = env->GetMethodID((jclass) clazz, "startElement",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)V");
    if (startElementMethod == NULL) return;
    
    endElementMethod = env->GetMethodID((jclass) clazz, "endElement",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (endElementMethod == NULL) return;

    textMethod = env->GetMethodID((jclass) clazz, "text", "([CI)V");
    if (textMethod == NULL) return;

    commentMethod = env->GetMethodID((jclass) clazz, "comment", "([CI)V");
    if (commentMethod == NULL) return;

    startCdataMethod = env->GetMethodID((jclass) clazz, "startCdata", "()V");
    if (startCdataMethod == NULL) return;

    endCdataMethod = env->GetMethodID((jclass) clazz, "endCdata", "()V");
    if (endCdataMethod == NULL) return;

    startDtdMethod = env->GetMethodID((jclass) clazz, "startDtd",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (startDtdMethod == NULL) return;

    endDtdMethod = env->GetMethodID((jclass) clazz, "endDtd", "()V");
    if (endDtdMethod == NULL) return;

    startNamespaceMethod = env->GetMethodID((jclass) clazz, "startNamespace",
        "(Ljava/lang/String;Ljava/lang/String;)V");
    if (startNamespaceMethod == NULL) return;

    endNamespaceMethod = env->GetMethodID((jclass) clazz, "endNamespace",
        "(Ljava/lang/String;)V");
    if (endNamespaceMethod == NULL) return;

    processingInstructionMethod = env->GetMethodID((jclass) clazz,
        "processingInstruction", "(Ljava/lang/String;Ljava/lang/String;)V");
    if (processingInstructionMethod == NULL) return;

    handleExternalEntityMethod = env->GetMethodID((jclass) clazz,
        "handleExternalEntity",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (handleExternalEntityMethod == NULL) return;

    // Look up String class.
    stringClass = env->FindClass("java/lang/String");

    internMethod = env->GetMethodID(stringClass, "intern",
        "()Ljava/lang/String;");
    if (internMethod == NULL) return;

    // Reference to "".
    emptyString = (jstring) env->NewGlobalRef(empty);
}

static JNINativeMethod parserMethods[] = {
    { "line", "(I)I", (void*) line },
    { "column", "(I)I", (void*) column },
    { "release", "(I)V", (void*) release },
    { "releaseParser", "(I)V", (void*) releaseParser },
    { "append", "(ILjava/lang/String;Z)V", (void*) appendString },
    { "append", "(I[CII)V", (void*) appendCharacters },
    { "append", "(I[BII)V", (void*) appendBytes },
    { "initialize", "(Ljava/lang/String;Z)I",
        (void*) initialize},
    { "createEntityParser", "(ILjava/lang/String;Ljava/lang/String;)I",
        (void*) createEntityParser},
    { "staticInitialize", "(Ljava/lang/String;)V", (void*) staticInitialize},
    { "cloneAttributes", "(II)I", (void*) cloneAttributes },
};

static JNINativeMethod attributeMethods[] = {
    { "getURI", "(III)Ljava/lang/String;", (void*) getAttributeURI },
    { "getLocalName", "(III)Ljava/lang/String;", (void*) getAttributeLocalName },
    { "getQName", "(III)Ljava/lang/String;", (void*) getAttributeQName },
    { "getValue", "(II)Ljava/lang/String;", (void*) getAttributeValueByIndex },
    { "getIndex", "(ILjava/lang/String;Ljava/lang/String;)I",
        (void*) getAttributeIndex },
    { "getIndex", "(ILjava/lang/String;)I",
        (void*) getAttributeIndexForQName },
    { "getValue", "(ILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
        (void*) getAttributeValue },
    { "getValue", "(ILjava/lang/String;)Ljava/lang/String;",
        (void*) getAttributeValueForQName },
    { "freeAttributes", "(I)V", (void*) freeAttributes },
};

/**
 * Called from Register.c.
 */
extern "C" int register_org_apache_harmony_xml_ExpatParser(JNIEnv* env) {
    int result = jniRegisterNativeMethods(env, "org/apache/harmony/xml/ExpatParser",
        parserMethods, NELEM(parserMethods));
    if (result != 0) {
        return result;
    }

    return jniRegisterNativeMethods(env, "org/apache/harmony/xml/ExpatAttributes",
        attributeMethods, NELEM(attributeMethods));
}
