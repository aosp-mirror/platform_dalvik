/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "BidiWrapperInterface.h"
#include "ErrorCode.h"
#include "unicode/ubidi.h"
#include <stdlib.h>
#include <string.h>

typedef struct {
    UBiDi *pBiDi;
    void *embeddingLevels;
} BiDiData;

JNIEXPORT jlong JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1open
  (JNIEnv * env, jclass clazz)
{
  BiDiData *data = (BiDiData *)malloc(sizeof(BiDiData)); 
  (*data).pBiDi = ubidi_open ();
  (*data).embeddingLevels = NULL;
  return (jlong) (data);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1close
  (JNIEnv * env, jclass clazz, jlong pBiDi)
{
  BiDiData *data = (BiDiData *)pBiDi;

  ubidi_close ((*data).pBiDi);
  
  free((*data).embeddingLevels);
  free(data);
}

JNIEXPORT void JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1setPara
  (JNIEnv * env, jclass clazz, jlong pBiDi, jcharArray text, jint length,
   jbyte paraLevel, jbyteArray newEmbeddingLevels)
{
  BiDiData *data = (BiDiData *)pBiDi;
  void *oldEmbeddingLevels = (*data).embeddingLevels;

  // Copy the new embedding levels from the Java heap to the native heap.
  if (newEmbeddingLevels != NULL) {
    (*data).embeddingLevels = malloc(length);
    (*env)->GetByteArrayRegion(env, newEmbeddingLevels, 0, length,
                               (*data).embeddingLevels);
  } else {
    (*data).embeddingLevels = NULL;
  }

  UErrorCode err = 0;
  jchar* _text = (*env)->GetCharArrayElements(env, text, NULL);
  ubidi_setPara ((*data).pBiDi, _text, length, paraLevel,
                 ((*data).embeddingLevels), &err);
  (*env)->ReleaseCharArrayElements (env, text, _text, 0);
  free(oldEmbeddingLevels);

  icu4jni_error(env, err);
}

JNIEXPORT jlong JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1setLine
  (JNIEnv * env, jclass clazz, jlong pBiDi, jint start, jint limit)
{
    UErrorCode err = 0;
    BiDiData *data = (BiDiData *)pBiDi;
    BiDiData *lineData = (BiDiData *) malloc(sizeof(BiDiData));
    (*lineData).embeddingLevels = NULL;

    (*lineData).pBiDi = ubidi_openSized (limit - start, 0, &err);
    if (icu4jni_error(env, err)) {
        return 0;
    }

    ubidi_setLine ((*data).pBiDi, start, limit, (*lineData).pBiDi, &err);
    icu4jni_error(env, err);
    return (jlong) lineData;
}

JNIEXPORT jint JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1getDirection
  (JNIEnv * env, jclass clazz, jlong pBiDi)
{
  BiDiData *data = (BiDiData *)pBiDi;
  return ubidi_getDirection ((*data).pBiDi);
}

JNIEXPORT jint JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1getLength
  (JNIEnv * env, jclass clazz, jlong pBiDi)
{
  BiDiData *data = (BiDiData *)pBiDi;
  return ubidi_getLength ((*data).pBiDi);
}

JNIEXPORT jbyte JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1getParaLevel
  (JNIEnv * env, jclass clazz, jlong pBiDi)
{
  BiDiData *data = (BiDiData *)pBiDi;
  return ubidi_getParaLevel ((*data).pBiDi);
}

JNIEXPORT jbyteArray JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1getLevels
  (JNIEnv * env, jclass clazz, jlong pBiDi)
{
    BiDiData *data = (BiDiData *)pBiDi;

    UErrorCode err = 0;
    const UBiDiLevel* levels = ubidi_getLevels((*data).pBiDi, &err);
    if (icu4jni_error(env, err)) {
        return NULL;
    }

    int len = ubidi_getLength((*data).pBiDi);
    jbyteArray result = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion(env, result, 0, len, (jbyte*) levels);

    return result;
}

JNIEXPORT jint JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1countRuns
  (JNIEnv * env, jclass clazz, jlong pBiDi)
{
    BiDiData *data = (BiDiData *)pBiDi;

    UErrorCode err = 0;
    int count = ubidi_countRuns ((*data).pBiDi, &err);
    icu4jni_error(env, err);
    return count;
}

JNIEXPORT jobjectArray JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1getRuns
  (JNIEnv * env, jclass clz, jlong pBiDi)
{
    BiDiData* data = (BiDiData*) pBiDi;

    UErrorCode err = 0;
    int runCount = ubidi_countRuns((*data).pBiDi, &err);
    if (icu4jni_error(env, err)) {
        return 0;
    }

    jclass bidiRunClass = (*env)->FindClass(env, "org/apache/harmony/text/BidiRun");
    jmethodID bidiRunConstructor = (*env)->GetMethodID(env, bidiRunClass, "<init>", "(III)V");
    jobjectArray runs = (*env)->NewObjectArray(env, runCount, bidiRunClass, NULL);
    UBiDiLevel level = 0;
    int start = 0;
    int limit = 0;
    int i = 0; // TODO: switch this file to C++!
    for (i = 0; i < runCount; ++i) {
        ubidi_getLogicalRun((*data).pBiDi, start, &limit, &level);
        jobject run = (*env)->NewObject(env, bidiRunClass, bidiRunConstructor, start, limit, level);
        (*env)->SetObjectArrayElement(env, runs, i, run);
        start = limit;
    }
    return runs;
}

JNIEXPORT jintArray JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1reorderVisual
  (JNIEnv * env, jclass clazz, jbyteArray levels, jint length)
{
  UBiDiLevel *local_levels = 0;
  int *local_indexMap = 0;
  jintArray result = 0;

  local_indexMap = (int *) malloc(sizeof (int) * length);
  local_levels = (*env)->GetByteArrayElements (env, levels, NULL);

  ubidi_reorderVisual (local_levels, length, local_indexMap);

  result = (*env)->NewIntArray (env, length);
  (*env)->SetIntArrayRegion (env, result, 0, length, (jint *) local_indexMap);

  free(local_indexMap);
  (*env)->ReleaseByteArrayElements (env, levels, local_levels, 0);

  return result;
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* NAME                , SIGNATURE              ,       FUNCPTR */
    { "ubidi_open"        , "()J"                                  ,
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1open          },
    { "ubidi_close"        , "(J)V"                                 ,
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1close         },
    { "ubidi_setPara"      , "(J[CIB[B)V"                           ,
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1setPara       },
    { "ubidi_setLine"      , "(JII)J"                               ,
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1setLine       },
    { "ubidi_getDirection" , "(J)I"                                 ,
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1getDirection  },
    { "ubidi_getLength"    , "(J)I"                                 ,
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1getLength     },
    { "ubidi_getParaLevel" , "(J)B"                                 ,
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1getParaLevel  },
    { "ubidi_getLevels"    , "(J)[B"                                ,
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1getLevels     },
    { "ubidi_countRuns"    , "(J)I"                                 ,
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1countRuns     },
    { "ubidi_getRuns"      , "(J)[Lorg/apache/harmony/text/BidiRun;",
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1getRuns       },
    { "ubidi_reorderVisual", "([BI)[I"                              ,
        Java_org_apache_harmony_text_BidiWrapper_ubidi_1reorderVisual },
};
int register_org_apache_harmony_text_BidiWrapper(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, "org/apache/harmony/text/BidiWrapper",
                gMethods, NELEM(gMethods));
}
