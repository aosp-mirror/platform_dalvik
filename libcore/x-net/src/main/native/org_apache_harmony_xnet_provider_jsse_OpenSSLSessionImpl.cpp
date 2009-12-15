/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "OpenSSLSessionImpl"

#include "AndroidSystemNatives.h"
#include "JNIHelp.h"

#include <jni.h>

#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>

#include <openssl/err.h>
#include <openssl/rand.h>
#include <openssl/ssl.h>

#include "org_apache_harmony_xnet_provider_jsse_common.h"

static jfieldID field_session;

static SSL_SESSION* getSslSessionPointer(JNIEnv* env, jobject object) {
    return reinterpret_cast<SSL_SESSION*>(env->GetIntField(object, field_session));
}

// Fills a byte[][] with the peer certificates in the chain.
static jobjectArray OpenSSLSessionImpl_getPeerCertificatesImpl(JNIEnv* env,
        jobject object, jint jssl)
{
    SSL_SESSION* ssl_session = getSslSessionPointer(env, object);
    SSL_CTX* ssl_ctx = SSL_CTX_new(SSLv23_client_method());
    SSL* ssl = SSL_new(ssl_ctx);

    SSL_set_session(ssl, ssl_session);

    STACK_OF(X509)* chain = SSL_get_peer_cert_chain(ssl);
    jobjectArray objectArray = getcertificatebytes(env, chain);

    SSL_free(ssl);
    SSL_CTX_free(ssl_ctx);
    return objectArray;
}

/**
 * Serializes the native state of the session (ID, cipher, and keys but
 * not certificates). Returns a byte[] containing the DER-encoded state.
 * See apache mod_ssl.
 */
static jbyteArray OpenSSLSessionImpl_getEncoded(JNIEnv* env, jobject object) {
    SSL_SESSION* ssl_session = getSslSessionPointer(env, object);
    if (ssl_session == NULL) {
        return NULL;
    }

    // Compute the size of the DER data
    int size = i2d_SSL_SESSION(ssl_session, NULL);
    if (size == 0) {
        return NULL;
    }

    jbyteArray bytes = env->NewByteArray(size);
    if (bytes != NULL) {
        jbyte* tmp = env->GetByteArrayElements(bytes, NULL);
        unsigned char* ucp = reinterpret_cast<unsigned char*>(tmp);
        i2d_SSL_SESSION(ssl_session, &ucp);
        env->ReleaseByteArrayElements(bytes, tmp, 0);
    }

    return bytes;
}

/**
 * Deserialize the session.
 */
static jint OpenSSLSessionImpl_initializeNativeImpl(JNIEnv* env, jobject object, jbyteArray bytes, jint size) {
    if (bytes == NULL) {
        return 0;
    }

    jbyte* tmp = env->GetByteArrayElements(bytes, NULL);
    const unsigned char* ucp = reinterpret_cast<const unsigned char*>(tmp);
    SSL_SESSION* ssl_session = d2i_SSL_SESSION(NULL, &ucp, size);
    env->ReleaseByteArrayElements(bytes, tmp, 0);

    return static_cast<jint>(reinterpret_cast<uintptr_t>(ssl_session));
}

/**
 * Gets and returns in a byte array the ID of the actual SSL session.
 */
static jbyteArray OpenSSLSessionImpl_getId(JNIEnv* env, jobject object) {
    SSL_SESSION* ssl_session = getSslSessionPointer(env, object);

    jbyteArray result = env->NewByteArray(ssl_session->session_id_length);
    if (result != NULL) {
        jbyte* src = reinterpret_cast<jbyte*>(ssl_session->session_id);
        env->SetByteArrayRegion(result, 0, ssl_session->session_id_length, src);
    }

    return result;
}

/**
 * Gets and returns in a long integer the creation's time of the
 * actual SSL session.
 */
static jlong OpenSSLSessionImpl_getCreationTime(JNIEnv* env, jobject object) {
    SSL_SESSION* ssl_session = getSslSessionPointer(env, object);
    jlong result = SSL_SESSION_get_time(ssl_session);
    result *= 1000; // OpenSSL uses seconds, Java uses milliseconds.
    return result;
}

/**
 * Gets and returns in a string the version of the SSL protocol. If it
 * returns the string "unknown" it means that no connection is established.
 */
static jstring OpenSSLSessionImpl_getProtocol(JNIEnv* env, jobject object) {
    SSL_SESSION* ssl_session = getSslSessionPointer(env, object);
    SSL_CTX* ssl_ctx = SSL_CTX_new(SSLv23_client_method());
    SSL* ssl = SSL_new(ssl_ctx);

    SSL_set_session(ssl, ssl_session);

    const char* protocol = SSL_get_version(ssl);
    jstring result = env->NewStringUTF(protocol);

    SSL_free(ssl);
    SSL_CTX_free(ssl_ctx);
    return result;
}

/**
 * Gets and returns in a string the set of ciphers the actual SSL session uses.
 */
static jstring OpenSSLSessionImpl_getCipherSuite(JNIEnv* env, jobject object) {
    SSL_SESSION* ssl_session = getSslSessionPointer(env, object);
    SSL_CTX* ssl_ctx = SSL_CTX_new(SSLv23_client_method());
    SSL* ssl = SSL_new(ssl_ctx);

    SSL_set_session(ssl, ssl_session);

    SSL_CIPHER* cipher = SSL_get_current_cipher(ssl);
    jstring result = env->NewStringUTF(SSL_CIPHER_get_name(cipher));

    SSL_free(ssl);
    SSL_CTX_free(ssl_ctx);
    return result;
}

/**
 * Frees the SSL session.
 */
static void OpenSSLSessionImpl_freeImpl(JNIEnv* env, jobject object, jint session) {
    LOGD("Freeing OpenSSL session");
    SSL_SESSION* ssl_session = reinterpret_cast<SSL_SESSION*>(session);
    SSL_SESSION_free(ssl_session);
}

static JNINativeMethod sMethods[] = {
    { "freeImpl", "(I)V", (void*) OpenSSLSessionImpl_freeImpl },
    { "getCipherSuite", "()Ljava/lang/String;", (void*) OpenSSLSessionImpl_getCipherSuite },
    { "getCreationTime", "()J", (void*) OpenSSLSessionImpl_getCreationTime },
    { "getEncoded", "()[B", (void*) OpenSSLSessionImpl_getEncoded },
    { "getId", "()[B", (void*) OpenSSLSessionImpl_getId },
    { "getPeerCertificatesImpl", "()[[B", (void*) OpenSSLSessionImpl_getPeerCertificatesImpl },
    { "getProtocol", "()Ljava/lang/String;", (void*) OpenSSLSessionImpl_getProtocol },
    { "initializeNativeImpl", "([BI)I", (void*) OpenSSLSessionImpl_initializeNativeImpl }
};

int register_org_apache_harmony_xnet_provider_jsse_OpenSSLSessionImpl(JNIEnv* env) {
    jclass clazz = env->FindClass("org/apache/harmony/xnet/provider/jsse/OpenSSLSessionImpl");
    if (clazz == NULL) {
        return -1;
    }

    field_session = env->GetFieldID(clazz, "session", "I");

    return jniRegisterNativeMethods(env, "org/apache/harmony/xnet/provider/jsse/OpenSSLSessionImpl",
            sMethods, NELEM(sMethods));
}
