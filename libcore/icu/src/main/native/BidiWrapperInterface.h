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

#include <JNIHelp.h>
/* Header for class org_apache_harmony_text_BidiWrapper */

#if !defined(_Included_org_apache_harmony_text_BidiWrapper)
#define _Included_org_apache_harmony_text_BidiWrapper
#if defined(__cplusplus)
extern "C"
{
#endif
#undef org_apache_harmony_text_BidiWrapper_UBIDI_DEFAULT_LTR
#define org_apache_harmony_text_BidiWrapper_UBIDI_DEFAULT_LTR 254L
#undef org_apache_harmony_text_BidiWrapper_UBIDI_DEFAULT_RTL
#define org_apache_harmony_text_BidiWrapper_UBIDI_DEFAULT_RTL 255L
#undef org_apache_harmony_text_BidiWrapper_UBIDI_MAX_EXPLICIT_LEVEL
#define org_apache_harmony_text_BidiWrapper_UBIDI_MAX_EXPLICIT_LEVEL 61L
#undef org_apache_harmony_text_BidiWrapper_UBIDI_LEVEL_OVERRIDE
#define org_apache_harmony_text_BidiWrapper_UBIDI_LEVEL_OVERRIDE 128L
#undef org_apache_harmony_text_BidiWrapper_UBIDI_KEEP_BASE_COMBINING
#define org_apache_harmony_text_BidiWrapper_UBIDI_KEEP_BASE_COMBINING 1L
#undef org_apache_harmony_text_BidiWrapper_UBIDI_DO_MIRRORING
#define org_apache_harmony_text_BidiWrapper_UBIDI_DO_MIRRORING 2L
#undef org_apache_harmony_text_BidiWrapper_UBIDI_INSERT_LRM_FOR_NUMERIC
#define org_apache_harmony_text_BidiWrapper_UBIDI_INSERT_LRM_FOR_NUMERIC 4L
#undef org_apache_harmony_text_BidiWrapper_UBIDI_REMOVE_BIDI_CONTROLS
#define org_apache_harmony_text_BidiWrapper_UBIDI_REMOVE_BIDI_CONTROLS 8L
#undef org_apache_harmony_text_BidiWrapper_UBIDI_OUTPUT_REVERSE
#define org_apache_harmony_text_BidiWrapper_UBIDI_OUTPUT_REVERSE 16L
#undef org_apache_harmony_text_BidiWrapper_UBiDiDirection_UBIDI_LTR
#define org_apache_harmony_text_BidiWrapper_UBiDiDirection_UBIDI_LTR 0L
#undef org_apache_harmony_text_BidiWrapper_UBiDiDirection_UBIDI_RTL
#define org_apache_harmony_text_BidiWrapper_UBiDiDirection_UBIDI_RTL 1L
#undef org_apache_harmony_text_BidiWrapper_UBiDiDirection_UBIDI_MIXED
#define org_apache_harmony_text_BidiWrapper_UBiDiDirection_UBIDI_MIXED 2L
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_open
 * Signature: ()J
 */
  JNIEXPORT jlong JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1open
    (JNIEnv *, jclass);
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_close
 * Signature: (J)V
 */
  JNIEXPORT void JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1close
    (JNIEnv *, jclass, jlong);
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_setPara
 * Signature: (J[CIB[B)V
 */
  JNIEXPORT void JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1setPara
    (JNIEnv *, jclass, jlong, jcharArray, jint, jbyte, jbyteArray);
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_setLine
 * Signature: (JII)J
 */
  JNIEXPORT jlong JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1setLine
    (JNIEnv *, jclass, jlong, jint, jint);
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_getDirection
 * Signature: (J)I
 */
  JNIEXPORT jint JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1getDirection
    (JNIEnv *, jclass, jlong);
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_getLength
 * Signature: (J)I
 */
  JNIEXPORT jint JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1getLength
    (JNIEnv *, jclass, jlong);
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_getParaLevel
 * Signature: (J)B
 */
  JNIEXPORT jbyte JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1getParaLevel
    (JNIEnv *, jclass, jlong);
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_getLevels
 * Signature: (J)[B
 */
  JNIEXPORT jbyteArray JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1getLevels
    (JNIEnv *, jclass, jlong);
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_countRuns
 * Signature: (J)I
 */
  JNIEXPORT jint JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1countRuns
    (JNIEnv *, jclass, jlong);
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_getRuns
 * Signature: (J)[Lorg/apache/harmony/text/BidiRun;
 */
JNIEXPORT jobjectArray JNICALL Java_org_apache_harmony_text_BidiWrapper_ubidi_1getRuns
  (JNIEnv *, jclass, jlong);
/*
 * Class:     org_apache_harmony_text_BidiWrapper
 * Method:    ubidi_reorderVisual
 * Signature: ([BI)[I
 */
  JNIEXPORT jintArray JNICALL
    Java_org_apache_harmony_text_BidiWrapper_ubidi_1reorderVisual (JNIEnv *, jclass,
                                                        jbyteArray, jint);
#if defined(__cplusplus)
}
#endif
#endif
