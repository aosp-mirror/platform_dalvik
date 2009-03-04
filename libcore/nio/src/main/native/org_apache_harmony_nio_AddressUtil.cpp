/*
 * Copyright (C) 2007 The Android Open Source Project
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

#include "JNIHelp.h"
#include "AndroidSystemNatives.h"

#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>

#define SOCKET_CAST(x) ((struct hysocket_struct*)  x)->sock

typedef jint OSSOCKET; 

typedef struct hysocket_struct
{
    OSSOCKET sock;
    unsigned short family;
} hysocket_struct;

typedef struct hysocket_struct *hysocket_t;

/*
 * Internal helper function.
 *
 * Get the file descriptor.
 */
static jint getFd(JNIEnv* env, jclass clazz, jobject fd)
{
    jclass descriptorCLS;
    jfieldID descriptorFID;
    hysocket_t* hysocketP;

    descriptorCLS = env->FindClass("java/io/FileDescriptor");
    if (NULL == descriptorCLS){
        return 0;
    }
    descriptorFID = env->GetFieldID(descriptorCLS, "descriptor", "I");
    if (NULL == descriptorFID){
        return 0;
    }
    jint result = env->GetIntField(fd, descriptorFID);
    hysocketP = (hysocket_t*) (result);
    return SOCKET_CAST(hysocketP);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getFDAddress", "(Ljava/io/FileDescriptor;)I", (void*) getFd }
};

int register_org_apache_harmony_nio_AddressUtil(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "org/apache/harmony/nio/AddressUtil",
        gMethods, NELEM(gMethods));
}
