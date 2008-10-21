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

#include "hy2sie.h"

#include "zlib.h"
#include <memory.h>
#define jclmem_allocate_memory(env, byteCount) sieb_malloc(env, byteCount)
#define jclmem_free_memory(env, pointer) sieb_free(env, pointer)

#include <fcntl.h>


void zfree PROTOTYPE ((void *opaque, void *address));
void *zalloc PROTOTYPE ((void *opaque, U_32 items, U_32 size));


static struct {
    jfieldID inRead;
    jfieldID finished;
    jfieldID needsDictionary;
} gCachedFields;


// Contents from Harmony's inflater.h was put here:
//
typedef struct JCLZipStream
{
  U_8 *inaddr;
  int inCap;
  U_8 *dict;
  z_stream *stream;
} JCLZipStream;



/**
  * Throw java.util.zip.DataFormatException
  */
void
throwNewDataFormatException (JNIEnv * env, const char *message)
{
  jniThrowException(env, "java/util/zip/DataFormatException", message);
}


/* Create a new stream . This stream cannot be used until it has been properly initialized. */
JNIEXPORT jlong JNICALL
Java_java_util_zip_Inflater_createStream (JNIEnv * env, jobject recv,
                                          jboolean noHeader)
{
  PORT_ACCESS_FROM_ENV (env);

  JCLZipStream *jstream;
  z_stream *stream;
  int err = 0;
  int wbits = 15;               /*Use MAX for fastest */

  /*Allocate mem for wrapped struct */
  jstream = jclmem_allocate_memory (env, sizeof (JCLZipStream));
  if (jstream == NULL)
    {
      throwNewOutOfMemoryError (env, "");
      return -1;
    }

  /*Allocate the z_stream */
  stream = jclmem_allocate_memory (env, sizeof (z_stream));
  if (stream == NULL)
    {
      jclmem_free_memory (env, jstream);
      throwNewOutOfMemoryError (env, "");
      return -1;
    }
  stream->opaque = (void *) privatePortLibrary;
  stream->zalloc = zalloc;
  stream->zfree = zfree;
  stream->adler = 1;
  jstream->stream = stream;
  jstream->dict = NULL;
  jstream->inaddr = NULL;
  jstream->inCap = 0;

  /*Unable to find official doc that this is the way to avoid zlib header use. However doc in zipsup.c claims it is so. */
  if (noHeader)
    wbits = wbits / -1;
  err = inflateInit2 (stream, wbits);   /*Window bits to use. 15 is fastest but consumes the most memory */

  if (err != Z_OK)
    {
      jclmem_free_memory (env, stream);
      jclmem_free_memory (env, jstream);
      throwNewIllegalArgumentException (env, "");
      return -1;
    }

  return (jlong) ((IDATA) jstream);
}

JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_setInputImpl (JNIEnv * env, jobject recv,
                                          jbyteArray buf, jint off, jint len,
                                          jlong handle)
{
  PORT_ACCESS_FROM_ENV (env);

  jbyte *in;
  U_8 *baseAddr;
  JCLZipStream *stream = (JCLZipStream *) ((IDATA) handle);

  if (stream->inaddr != NULL)   /*Input has already been provided, free the old buffer */
    jclmem_free_memory (env, stream->inaddr);
  baseAddr = jclmem_allocate_memory (env, len);
  if (baseAddr == NULL)
    {
      throwNewOutOfMemoryError (env, "");
      return;
    }
  stream->inaddr = baseAddr;
  stream->stream->next_in = (Bytef *) baseAddr;
  stream->stream->avail_in = len;
  in = ((*env)->GetPrimitiveArrayCritical (env, buf, 0));
  if (in == NULL)
    return;
  memcpy (baseAddr, (in + off), len);
  ((*env)->ReleasePrimitiveArrayCritical (env, buf, in, JNI_ABORT));
  return;
}

JNIEXPORT jint JNICALL
Java_java_util_zip_Inflater_setFileInputImpl (JNIEnv * env, jobject recv,
        jobject javaFileDescriptor, jlong off, jint len, jlong handle)
{
    PORT_ACCESS_FROM_ENV (env);

    U_8 * baseAddr;
    JCLZipStream * stream = (JCLZipStream *) ((IDATA) handle);

    if (stream->inCap < len) {
        // No input buffer as yet (or one that is too small).
        jclmem_free_memory(env, stream->inaddr);
        baseAddr = jclmem_allocate_memory(env, len);
        if (baseAddr == NULL)
        {
            throwNewOutOfMemoryError(env, "");
            return -1;
        }
        stream->inaddr = baseAddr;
    }
    stream->stream->next_in = (Bytef *) stream->inaddr;
    stream->stream->avail_in = len;

    int fd = jniGetFDFromFileDescriptor(env, javaFileDescriptor);
    lseek(fd, off, SEEK_SET);
    int cnt = read(fd, stream->inaddr, len);

    return cnt;
}

JNIEXPORT jint JNICALL
Java_java_util_zip_Inflater_inflateImpl (JNIEnv * env, jobject recv,
                                         jbyteArray buf, int off, int len,
                                         jlong handle)
{
  PORT_ACCESS_FROM_ENV (env);

  jbyte *out;
  JCLZipStream *stream = (JCLZipStream *) ((IDATA) handle);
  jint err = 0;
  jfieldID fid = 0, fid2 = 0;
  jint sin, sout, inBytes = 0;

  /* We need to get the number of bytes already read */
  fid = gCachedFields.inRead;
  inBytes = ((*env)->GetIntField (env, recv, fid));

  stream->stream->avail_out = len;
  sin = stream->stream->total_in;
  sout = stream->stream->total_out;
  out = ((*env)->GetPrimitiveArrayCritical (env, buf, 0));

  if (out == NULL)
    return -1;
  stream->stream->next_out = (Bytef *) out + off;
  err = inflate (stream->stream, Z_SYNC_FLUSH);
  ((*env)->ReleasePrimitiveArrayCritical (env, buf, out, 0));

  if (err != Z_OK)
    {
      if(err == Z_STREAM_ERROR) {
          return 0;
      }
      if (err == Z_STREAM_END || err == Z_NEED_DICT)
        {
          ((*env)->SetIntField (env, recv, fid, (jint) stream->stream->total_in - sin + inBytes));      /* Update inRead */
          if (err == Z_STREAM_END)
            fid2 = gCachedFields.finished;
          else
            fid2 = gCachedFields.needsDictionary;

          ((*env)->SetBooleanField (env, recv, fid2, JNI_TRUE));
          return stream->stream->total_out - sout;
        }
      else
        {
          throwNewDataFormatException (env, "");
          return -1;
        }
    }

  /* Need to update the number of input bytes read. Is there a better way
   * (Maybe global the fid then delete when end is called)?
   */
  ((*env)->
   SetIntField (env, recv, fid,
                (jint) stream->stream->total_in - sin + inBytes));

  return stream->stream->total_out - sout;
}

JNIEXPORT jint JNICALL
Java_java_util_zip_Inflater_getAdlerImpl (JNIEnv * env, jobject recv,
                                          jlong handle)
{
  JCLZipStream *stream;

  stream = (JCLZipStream *) ((IDATA) handle);

  return stream->stream->adler;
}

JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_endImpl (JNIEnv * env, jobject recv, jlong handle)
{
  PORT_ACCESS_FROM_ENV (env);
  JCLZipStream *stream;

  stream = (JCLZipStream *) ((IDATA) handle);
  inflateEnd (stream->stream);
  if (stream->inaddr != NULL)   /*Input has been provided, free the buffer */
    jclmem_free_memory (env, stream->inaddr);
  if (stream->dict != NULL)
    jclmem_free_memory (env, stream->dict);
  jclmem_free_memory (env, stream->stream);
  jclmem_free_memory (env, stream);
}

JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_setDictionaryImpl (JNIEnv * env, jobject recv,
                                               jbyteArray dict, int off,
                                               int len, jlong handle)
{
  PORT_ACCESS_FROM_ENV (env);
  int err = 0;
  U_8 *dBytes;
  JCLZipStream *stream = (JCLZipStream *) ((IDATA) handle);

  dBytes = jclmem_allocate_memory (env, len);
  if (dBytes == NULL)
    {
      throwNewOutOfMemoryError (env, "");
      return;
    }
  (*env)->GetByteArrayRegion (env, dict, off, len, (mcSignednessBull)dBytes);
  err = inflateSetDictionary (stream->stream, (Bytef *) dBytes, len);
  if (err != Z_OK)
    {
      jclmem_free_memory (env, dBytes);
      throwNewIllegalArgumentException (env, "");
      return;
    }
  stream->dict = dBytes;
}

JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_resetImpl (JNIEnv * env, jobject recv,
                                       jlong handle)
{
  JCLZipStream *stream;
  int err = 0;
  stream = (JCLZipStream *) ((IDATA) handle);

  err = inflateReset (stream->stream);
  if (err != Z_OK)
    {
      throwNewIllegalArgumentException (env, "");
      return;
    }
}


JNIEXPORT jlong JNICALL
Java_java_util_zip_Inflater_getTotalOutImpl (JNIEnv * env, jobject recv,
                                             jlong handle)
{
  JCLZipStream *stream;

  stream = (JCLZipStream *) ((IDATA) handle);
  return stream->stream->total_out;
}

JNIEXPORT jlong JNICALL
Java_java_util_zip_Inflater_getTotalInImpl (JNIEnv * env, jobject recv,
                                            jlong handle)
{
  JCLZipStream *stream;

  stream = (JCLZipStream *) ((IDATA) handle);
  return stream->stream->total_in;
}

JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_oneTimeInitialization (JNIEnv * env, jclass clazz)
{
    memset(&gCachedFields, 0, sizeof(gCachedFields));
    gCachedFields.inRead = (*env)->GetFieldID (env, clazz, "inRead", "I");
    gCachedFields.finished = (*env)->GetFieldID (env, clazz, "finished", "Z");
    gCachedFields.needsDictionary = (*env)->GetFieldID (env, clazz, "needsDictionary", "Z");
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "createStream", "(Z)J",     Java_java_util_zip_Inflater_createStream },
    { "setInputImpl", "([BIIJ)V",     Java_java_util_zip_Inflater_setInputImpl },
    { "setFileInputImpl", "(Ljava/io/FileDescriptor;JIJ)I",     Java_java_util_zip_Inflater_setFileInputImpl },
    { "inflateImpl", "([BIIJ)I",     Java_java_util_zip_Inflater_inflateImpl },
    { "getAdlerImpl", "(J)I",     Java_java_util_zip_Inflater_getAdlerImpl },
    { "endImpl", "(J)V",     Java_java_util_zip_Inflater_endImpl },
    { "setDictionaryImpl", "([BIIJ)V",     Java_java_util_zip_Inflater_setDictionaryImpl },
    { "resetImpl", "(J)V",     Java_java_util_zip_Inflater_resetImpl },
    { "getTotalOutImpl", "(J)J",     Java_java_util_zip_Inflater_getTotalOutImpl },
    { "getTotalInImpl", "(J)J",     Java_java_util_zip_Inflater_getTotalInImpl },
    { "oneTimeInitialization", "()V",     Java_java_util_zip_Inflater_oneTimeInitialization },
};
int register_java_util_zip_Inflater(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "java/util/zip/Inflater",
                gMethods, NELEM(gMethods));
}
