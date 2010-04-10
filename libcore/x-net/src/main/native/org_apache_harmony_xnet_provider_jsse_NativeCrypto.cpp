/*
 * Copyright (C) 2007-2008 The Android Open Source Project
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

/**
 * Native glue for Java class org.apache.harmony.xnet.provider.jsse.NativeCrypto
 */

#define LOG_TAG "NativeCrypto"

#include <fcntl.h>
#include <sys/socket.h>
#include <unistd.h>

#include <jni.h>

#include <JNIHelp.h>
#include <LocalArray.h>

#include <openssl/dsa.h>
#include <openssl/err.h>
#include <openssl/evp.h>
#include <openssl/rand.h>
#include <openssl/rsa.h>
#include <openssl/ssl.h>

/**
 * Structure to hold JNI state for openssl callback
 */
struct jsse_ssl_app_data_t {
    JNIEnv* env;
    jobject object;
};

/**
 * Frees the SSL error state.
 *
 * OpenSSL keeps an "error stack" per thread, and given that this code
 * can be called from arbitrary threads that we don't keep track of,
 * we err on the side of freeing the error state promptly (instead of,
 * say, at thread death).
 */
static void freeSslErrorState(void) {
    ERR_clear_error();
    ERR_remove_state(0);
}

/*
 * Checks this thread's OpenSSL error queue and throws a RuntimeException if
 * necessary.
 *
 * @return 1 if an exception was thrown, 0 if not.
 */
static int throwExceptionIfNecessary(JNIEnv* env) {
    int error = ERR_get_error();
    int result = 0;

    if (error != 0) {
        char message[50];
        ERR_error_string_n(error, message, sizeof(message));
        LOGD("OpenSSL error %d: %s", error, message);
        jniThrowRuntimeException(env, message);
        result = 1;
    }

    freeSslErrorState();
    return result;
}


/**
 * Throws an SocketTimeoutException with the given string as a message.
 */
static void throwSocketTimeoutException(JNIEnv* env, const char* message) {
    if (jniThrowException(env, "java/net/SocketTimeoutException", message)) {
        LOGE("Unable to throw");
    }
}

/**
 * Throws a java.io.IOException with the given string as a message.
 */
static void throwIOExceptionStr(JNIEnv* env, const char* message) {
    if (jniThrowException(env, "java/io/IOException", message)) {
        LOGE("Unable to throw");
    }
}

/**
 * Throws an IOException with a message constructed from the current
 * SSL errors. This will also log the errors.
 *
 * @param env the JNI environment
 * @param sslReturnCode return code from failing SSL function
 * @param sslErrorCode error code returned from SSL_get_error()
 * @param message null-ok; general error message
 */
static void throwIOExceptionWithSslErrors(JNIEnv* env, int sslReturnCode,
        int sslErrorCode, const char* message) {
    const char* messageStr = NULL;
    char* str;
    int ret;

    // First consult the SSL error code for the general message.
    switch (sslErrorCode) {
        case SSL_ERROR_NONE:
            messageStr = "Ok";
            break;
        case SSL_ERROR_SSL:
            messageStr = "Failure in SSL library, usually a protocol error";
            break;
        case SSL_ERROR_WANT_READ:
            messageStr = "SSL_ERROR_WANT_READ occured. You should never see this.";
            break;
        case SSL_ERROR_WANT_WRITE:
            messageStr = "SSL_ERROR_WANT_WRITE occured. You should never see this.";
            break;
        case SSL_ERROR_WANT_X509_LOOKUP:
            messageStr = "SSL_ERROR_WANT_X509_LOOKUP occured. You should never see this.";
            break;
        case SSL_ERROR_SYSCALL:
            messageStr = "I/O error during system call";
            break;
        case SSL_ERROR_ZERO_RETURN:
            messageStr = "SSL_ERROR_ZERO_RETURN occured. You should never see this.";
            break;
        case SSL_ERROR_WANT_CONNECT:
            messageStr = "SSL_ERROR_WANT_CONNECT occured. You should never see this.";
            break;
        case SSL_ERROR_WANT_ACCEPT:
            messageStr = "SSL_ERROR_WANT_ACCEPT occured. You should never see this.";
            break;
        default:
            messageStr = "Unknown SSL error";
    }

    // Prepend either our explicit message or a default one.
    if (asprintf(&str, "%s: %s",
            (message != NULL) ? message : "SSL error", messageStr) == 0) {
        throwIOExceptionStr(env, messageStr);
        LOGV("%s", messageStr);
        freeSslErrorState();
        return;
    }

    char* allocStr = str;

    // For SSL protocol errors, SSL might have more information.
    if (sslErrorCode == SSL_ERROR_SSL) {
        // Append each error as an additional line to the message.
        for (;;) {
            char errStr[256];
            const char* file;
            int line;
            const char* data;
            int flags;
            unsigned long err =
                ERR_get_error_line_data(&file, &line, &data, &flags);
            if (err == 0) {
                break;
            }

            ERR_error_string_n(err, errStr, sizeof(errStr));

            ret = asprintf(&str, "%s\n%s (%s:%d %p:0x%08x)",
                    (allocStr == NULL) ? "" : allocStr,
                    errStr,
                    file,
                    line,
                    data,
                    flags);

            if (ret < 0) {
                break;
            }

            free(allocStr);
            allocStr = str;
        }
    // For errors during system calls, errno might be our friend.
    } else if (sslErrorCode == SSL_ERROR_SYSCALL) {
        if (asprintf(&str, "%s, %s", allocStr, strerror(errno)) >= 0) {
            free(allocStr);
            allocStr = str;
        }
    // If the error code is invalid, print it.
    } else if (sslErrorCode > SSL_ERROR_WANT_ACCEPT) {
        if (asprintf(&str, ", error code is %d", sslErrorCode) >= 0) {
            free(allocStr);
            allocStr = str;
        }
    }

    throwIOExceptionStr(env, allocStr);

    LOGV("%s", allocStr);
    free(allocStr);
    freeSslErrorState();
}

/**
 * Helper function that grabs the casts an ssl pointer and then checks for nullness.
 * If this function returns NULL and <code>throwIfNull</code> is
 * passed as <code>true</code>, then this function will call
 * <code>throwIOExceptionStr</code> before returning, so in this case of
 * NULL, a caller of this function should simply return and allow JNI
 * to do its thing.
 *
 * @param env the JNI environment
 * @param ssl_address; the ssl_address pointer as an integer
 * @param throwIfNull whether to throw if the SSL pointer is NULL
 * @returns the pointer, which may be NULL
 */
static SSL* getSslPointer(JNIEnv* env, int ssl_address, bool throwIfNull) {
    SSL* ssl = reinterpret_cast<SSL*>(static_cast<uintptr_t>(ssl_address));
    if ((ssl == NULL) && throwIfNull) {
        throwIOExceptionStr(env, "null SSL pointer");
    }

    return ssl;
}

/**
 * Converts a Java byte[] to an OpenSSL BIGNUM, allocating the BIGNUM on the
 * fly.
 */
static BIGNUM* arrayToBignum(JNIEnv* env, jbyteArray source) {
    // LOGD("Entering arrayToBignum()");

    jbyte* sourceBytes = env->GetByteArrayElements(source, NULL);
    int sourceLength = env->GetArrayLength(source);
    BIGNUM* bignum = BN_bin2bn((unsigned char*) sourceBytes, sourceLength, NULL);
    env->ReleaseByteArrayElements(source, sourceBytes, JNI_ABORT);
    return bignum;
}

/**
 * OpenSSL locking support. Taken from the O'Reilly book by Viega et al., but I
 * suppose there are not many other ways to do this on a Linux system (modulo
 * isomorphism).
 */
#define MUTEX_TYPE pthread_mutex_t
#define MUTEX_SETUP(x) pthread_mutex_init(&(x), NULL)
#define MUTEX_CLEANUP(x) pthread_mutex_destroy(&(x))
#define MUTEX_LOCK(x) pthread_mutex_lock(&(x))
#define MUTEX_UNLOCK(x) pthread_mutex_unlock(&(x))
#define THREAD_ID pthread_self()
#define THROW_EXCEPTION (-2)
#define THROW_SOCKETTIMEOUTEXCEPTION (-3)

static MUTEX_TYPE *mutex_buf = NULL;

static void locking_function(int mode, int n, const char * file, int line) {
    if (mode & CRYPTO_LOCK) {
        MUTEX_LOCK(mutex_buf[n]);
    } else {
        MUTEX_UNLOCK(mutex_buf[n]);
    }
}

static unsigned long id_function(void) {
    return ((unsigned long)THREAD_ID);
}

int THREAD_setup(void) {
    int i;

    mutex_buf = (MUTEX_TYPE *)malloc(CRYPTO_num_locks( ) * sizeof(MUTEX_TYPE));

    if(!mutex_buf) {
        return 0;
    }

    for (i = 0; i < CRYPTO_num_locks( ); i++) {
        MUTEX_SETUP(mutex_buf[i]);
    }

    CRYPTO_set_id_callback(id_function);
    CRYPTO_set_locking_callback(locking_function);

    return 1;
}

int THREAD_cleanup(void) {
    int i;

    if (!mutex_buf) {
      return 0;
    }

    CRYPTO_set_id_callback(NULL);
    CRYPTO_set_locking_callback(NULL);

    for (i = 0; i < CRYPTO_num_locks( ); i++) {
        MUTEX_CLEANUP(mutex_buf[i]);
    }

    free(mutex_buf);
    mutex_buf = NULL;

    return 1;
}

/**
 * Initialization phase for every OpenSSL job: Loads the Error strings, the
 * crypto algorithms and reset the OpenSSL library
 */
static void NativeCrypto_clinit(JNIEnv* env, jclass)
{
    SSL_load_error_strings();
    ERR_load_crypto_strings();
    SSL_library_init();
    OpenSSL_add_all_algorithms();
    THREAD_setup();
}

/**
 * public static native int EVP_PKEY_new_DSA(byte[] p, byte[] q, byte[] g, byte[] pub_key, byte[] priv_key);
 */
static EVP_PKEY* NativeCrypto_EVP_PKEY_new_DSA(JNIEnv* env, jclass clazz, jbyteArray p, jbyteArray q, jbyteArray g, jbyteArray pub_key, jbyteArray priv_key) {
    // LOGD("Entering EVP_PKEY_new_DSA()");

    DSA* dsa = DSA_new();

    dsa->p = arrayToBignum(env, p);
    dsa->q = arrayToBignum(env, q);
    dsa->g = arrayToBignum(env, g);
    dsa->pub_key = arrayToBignum(env, pub_key);

    if (priv_key != NULL) {
        dsa->priv_key = arrayToBignum(env, priv_key);
    }

    if (dsa->p == NULL || dsa->q == NULL || dsa->g == NULL || dsa->pub_key == NULL) {
        DSA_free(dsa);
        jniThrowRuntimeException(env, "Unable to convert BigInteger to BIGNUM");
        return NULL;
    }

    EVP_PKEY* pkey = EVP_PKEY_new();
    EVP_PKEY_assign_DSA(pkey, dsa);

    return pkey;
}

/**
 * private static native int EVP_PKEY_new_RSA(byte[] n, byte[] e, byte[] d, byte[] p, byte[] q);
 */
static EVP_PKEY* NativeCrypto_EVP_PKEY_new_RSA(JNIEnv* env, jclass clazz, jbyteArray n, jbyteArray e, jbyteArray d, jbyteArray p, jbyteArray q) {
    // LOGD("Entering EVP_PKEY_new_RSA()");

    RSA* rsa = RSA_new();

    rsa->n = arrayToBignum(env, n);
    rsa->e = arrayToBignum(env, e);

    if (d != NULL) {
        rsa->d = arrayToBignum(env, d);
    }

    if (p != NULL) {
        rsa->p = arrayToBignum(env, p);
    }

    if (q != NULL) {
        rsa->q = arrayToBignum(env, q);
    }

    // int check = RSA_check_key(rsa);
    // LOGI("RSA_check_key returns %d", check);

    if (rsa->n == NULL || rsa->e == NULL) {
        RSA_free(rsa);
        jniThrowRuntimeException(env, "Unable to convert BigInteger to BIGNUM");
        return NULL;
    }

    EVP_PKEY* pkey = EVP_PKEY_new();
    EVP_PKEY_assign_RSA(pkey, rsa);

    return pkey;
}

/**
 * private static native void EVP_PKEY_free(int pkey);
 */
static void NativeCrypto_EVP_PKEY_free(JNIEnv* env, jclass clazz, EVP_PKEY* pkey) {
    // LOGD("Entering EVP_PKEY_free()");

    if (pkey != NULL) {
        EVP_PKEY_free(pkey);
    }
}

/*
 * public static native int EVP_new()
 */
static jint NativeCrypto_EVP_new(JNIEnv* env, jclass clazz) {
    // LOGI("NativeCrypto_EVP_DigestNew");

    return (jint)EVP_MD_CTX_create();
}

/*
 * public static native void EVP_free(int)
 */
static void NativeCrypto_EVP_free(JNIEnv* env, jclass clazz, EVP_MD_CTX* ctx) {
    // LOGI("NativeCrypto_EVP_DigestFree");

    if (ctx != NULL) {
        EVP_MD_CTX_destroy(ctx);
    }
}

/*
 * public static native int EVP_DigestFinal(int, byte[], int)
 */
static jint NativeCrypto_EVP_DigestFinal(JNIEnv* env, jclass clazz, EVP_MD_CTX* ctx, jbyteArray hash, jint offset) {
    // LOGI("NativeCrypto_EVP_DigestFinal%x, %x, %d, %d", ctx, hash, offset);

    if (ctx == NULL || hash == NULL) {
        jniThrowNullPointerException(env, NULL);
        return -1;
    }

    int result = -1;

    jbyte* hashBytes = env->GetByteArrayElements(hash, NULL);
    EVP_DigestFinal(ctx, (unsigned char*) (hashBytes + offset), (unsigned int*)&result);
    env->ReleaseByteArrayElements(hash, hashBytes, 0);

    throwExceptionIfNecessary(env);

    return result;
}

/*
 * public static native void EVP_DigestInit(int, java.lang.String)
 */
static void NativeCrypto_EVP_DigestInit(JNIEnv* env, jclass clazz, EVP_MD_CTX* ctx, jstring algorithm) {
    // LOGI("NativeCrypto_EVP_DigestInit");

    if (ctx == NULL || algorithm == NULL) {
        jniThrowNullPointerException(env, NULL);
        return;
    }

    const char* algorithmChars = env->GetStringUTFChars(algorithm, NULL);

    const EVP_MD *digest = EVP_get_digestbynid(OBJ_txt2nid(algorithmChars));
    env->ReleaseStringUTFChars(algorithm, algorithmChars);

    if (digest == NULL) {
        jniThrowRuntimeException(env, "Hash algorithm not found");
        return;
    }

    EVP_DigestInit(ctx, digest);

    throwExceptionIfNecessary(env);
}

/*
 * public static native void EVP_DigestSize(int)
 */
static jint NativeCrypto_EVP_DigestSize(JNIEnv* env, jclass clazz, EVP_MD_CTX* ctx) {
    // LOGI("NativeCrypto_EVP_DigestSize");

    if (ctx == NULL) {
        jniThrowNullPointerException(env, NULL);
        return -1;
    }

    int result = EVP_MD_CTX_size(ctx);

    throwExceptionIfNecessary(env);

    return result;
}

/*
 * public static native void EVP_DigestBlockSize(int)
 */
static jint NativeCrypto_EVP_DigestBlockSize(JNIEnv* env, jclass clazz, EVP_MD_CTX* ctx) {
    // LOGI("NativeCrypto_EVP_DigestBlockSize");

    if (ctx == NULL) {
        jniThrowNullPointerException(env, NULL);
        return -1;
    }

    int result = EVP_MD_CTX_block_size(ctx);

    throwExceptionIfNecessary(env);

    return result;
}

/*
 * public static native void EVP_DigestUpdate(int, byte[], int, int)
 */
static void NativeCrypto_EVP_DigestUpdate(JNIEnv* env, jclass clazz, EVP_MD_CTX* ctx, jbyteArray buffer, jint offset, jint length) {
    // LOGI("NativeCrypto_EVP_DigestUpdate %x, %x, %d, %d", ctx, buffer, offset, length);

    if (ctx == NULL || buffer == NULL) {
        jniThrowNullPointerException(env, NULL);
        return;
    }

    jbyte* bufferBytes = env->GetByteArrayElements(buffer, NULL);
    EVP_DigestUpdate(ctx, (unsigned char*) (bufferBytes + offset), length);
    env->ReleaseByteArrayElements(buffer, bufferBytes, JNI_ABORT);

    throwExceptionIfNecessary(env);
}

/*
 * public static native void EVP_VerifyInit(int, java.lang.String)
 */
static void NativeCrypto_EVP_VerifyInit(JNIEnv* env, jclass clazz, EVP_MD_CTX* ctx, jstring algorithm) {
    // LOGI("NativeCrypto_EVP_VerifyInit");

    if (ctx == NULL || algorithm == NULL) {
        jniThrowNullPointerException(env, NULL);
        return;
    }

    const char* algorithmChars = env->GetStringUTFChars(algorithm, NULL);

    const EVP_MD *digest = EVP_get_digestbynid(OBJ_txt2nid(algorithmChars));
    env->ReleaseStringUTFChars(algorithm, algorithmChars);

    if (digest == NULL) {
        jniThrowRuntimeException(env, "Hash algorithm not found");
        return;
    }

    EVP_VerifyInit(ctx, digest);

    throwExceptionIfNecessary(env);
}

/*
 * public static native void EVP_VerifyUpdate(int, byte[], int, int)
 */
static void NativeCrypto_EVP_VerifyUpdate(JNIEnv* env, jclass clazz, EVP_MD_CTX* ctx, jbyteArray buffer, jint offset, jint length) {
    // LOGI("NativeCrypto_EVP_VerifyUpdate %x, %x, %d, %d", ctx, buffer, offset, length);

    if (ctx == NULL || buffer == NULL) {
        jniThrowNullPointerException(env, NULL);
        return;
    }

    jbyte* bufferBytes = env->GetByteArrayElements(buffer, NULL);
    EVP_VerifyUpdate(ctx, (unsigned char*) (bufferBytes + offset), length);
    env->ReleaseByteArrayElements(buffer, bufferBytes, JNI_ABORT);

    throwExceptionIfNecessary(env);
}

/*
 * public static native void EVP_VerifyFinal(int, byte[], int, int, int)
 */
static int NativeCrypto_EVP_VerifyFinal(JNIEnv* env, jclass clazz, EVP_MD_CTX* ctx, jbyteArray buffer, jint offset, jint length, EVP_PKEY* pkey) {
    // LOGI("NativeCrypto_EVP_VerifyFinal %x, %x, %d, %d %x", ctx, buffer, offset, length, pkey);

    if (ctx == NULL || buffer == NULL || pkey == NULL) {
        jniThrowNullPointerException(env, NULL);
        return -1;
    }

    jbyte* bufferBytes = env->GetByteArrayElements(buffer, NULL);
    int result = EVP_VerifyFinal(ctx, (unsigned char*) (bufferBytes + offset), length, pkey);
    env->ReleaseByteArrayElements(buffer, bufferBytes, JNI_ABORT);

    throwExceptionIfNecessary(env);

    return result;
}

/**
 * Convert ssl version constant to string. Based on SSL_get_version
 */
static const char* get_ssl_version(int ssl_version) {
    switch (ssl_version) {
        // newest to oldest
        case TLS1_VERSION: {
          return SSL_TXT_TLSV1;
        }
        case SSL3_VERSION: {
          return SSL_TXT_SSLV3;
        }
        case SSL2_VERSION: {
          return SSL_TXT_SSLV2;
        }
        default: {
          return "unknown";
        }
    }
}

/**
 * Convert content type constant to string.
 */
static const char* get_content_type(int content_type) {
    switch (content_type) {
        case SSL3_RT_CHANGE_CIPHER_SPEC: {
            return "SSL3_RT_CHANGE_CIPHER_SPEC";
        }
        case SSL3_RT_ALERT: {
            return "SSL3_RT_ALERT";
        }
        case SSL3_RT_HANDSHAKE: {
            return "SSL3_RT_HANDSHAKE";
        }
        case SSL3_RT_APPLICATION_DATA: {
            return "SSL3_RT_APPLICATION_DATA";
        }
        default: {
            LOGD("Unknown TLS/SSL content type %d", content_type);
            return "<unknown>";
        }
    }
}

/**
 * Simple logging call back to show hand shake messages
 */
static void ssl_msg_callback_LOG(int write_p, int ssl_version, int content_type,
                                 const void *buf, size_t len, SSL* ssl, void* arg) {
    LOGD("SSL %p %s %s %s %p %d %p",
         ssl,
         (write_p) ? "send" : "recv",
         get_ssl_version(ssl_version),
         get_content_type(content_type),
         buf,
         len,
         arg);
}

/*
 * public static native int SSL_CTX_new();
 */
static int NativeCrypto_SSL_CTX_new(JNIEnv* env, jclass clazz) {
    SSL_CTX* sslCtx = SSL_CTX_new(SSLv23_method());
    // Note: We explicitly do not allow SSLv2 to be used.
    SSL_CTX_set_options(sslCtx, SSL_OP_ALL | SSL_OP_NO_SSLv2);

    int mode = SSL_CTX_get_mode(sslCtx);
    /*
     * Turn on "partial write" mode. This means that SSL_write() will
     * behave like Posix write() and possibly return after only
     * writing a partial buffer. Note: The alternative, perhaps
     * surprisingly, is not that SSL_write() always does full writes
     * but that it will force you to retry write calls having
     * preserved the full state of the original call. (This is icky
     * and undesirable.)
     */
    mode |= SSL_MODE_ENABLE_PARTIAL_WRITE;
#if defined(SSL_MODE_SMALL_BUFFERS) /* not all SSL versions have this */
    mode |= SSL_MODE_SMALL_BUFFERS;  /* lazily allocate record buffers; usually saves
                                      * 44k over the default */
#endif
#if defined(SSL_MODE_HANDSHAKE_CUTTHROUGH) /* not all SSL versions have this */
    mode |= SSL_MODE_HANDSHAKE_CUTTHROUGH;  /* enable sending of client data as soon as
                                             * ClientCCS and ClientFinished are sent */
#endif
    SSL_CTX_set_mode(sslCtx, mode);

    // SSL_CTX_set_msg_callback(sslCtx, ssl_msg_callback_LOG); /* enable for handshake debug */
    return (jint) sslCtx;
}

static jobjectArray makeCipherList(JNIEnv* env, STACK_OF(SSL_CIPHER)* cipher_list) {
    // Create a String[].
    jclass stringClass = env->FindClass("java/lang/String");
    if (stringClass == NULL) {
        return NULL;
    }
    int cipherCount = sk_SSL_CIPHER_num(cipher_list);
    jobjectArray array = env->NewObjectArray(cipherCount, stringClass, NULL);
    if (array == NULL) {
        return NULL;
    }

    // Fill in the cipher names.
    for (int i = 0; i < cipherCount; ++i) {
        const char* c = sk_SSL_CIPHER_value(cipher_list, i)->name;
        env->SetObjectArrayElement(array, i, env->NewStringUTF(c));
    }
    return array;
}

/**
 * Loads the ciphers suites that are supported by an SSL_CTX
 * and returns them in a string array.
 */
static jobjectArray NativeCrypto_SSL_CTX_get_ciphers(JNIEnv* env,
        jclass, jint ssl_ctx_address)
{
    SSL_CTX* ssl_ctx = reinterpret_cast<SSL_CTX*>(static_cast<uintptr_t>(ssl_ctx_address));
    if (ssl_ctx == NULL) {
        jniThrowNullPointerException(env, "SSL_CTX is null");
        return NULL;
    }
    return makeCipherList(env, ssl_ctx->cipher_list);
}

/**
 * public static native void SSL_CTX_free(int ssl_ctx)
 */
static void NativeCrypto_SSL_CTX_free(JNIEnv* env,
        jclass, jint ssl_ctx_address)
{
    SSL_CTX* ssl_ctx = reinterpret_cast<SSL_CTX*>(static_cast<uintptr_t>(ssl_ctx_address));
    if (ssl_ctx == NULL) {
        jniThrowNullPointerException(env, "SSL_CTX is null");
        return;
    }
    SSL_CTX_free(ssl_ctx);
}

/**
 * Gets the chars of a String object as a '\0'-terminated UTF-8 string,
 * stored in a freshly-allocated BIO memory buffer.
 */
static BIO *stringToMemBuf(JNIEnv* env, jstring string) {
    jsize byteCount = env->GetStringUTFLength(string);
    LocalArray<1024> buf(byteCount + 1);
    env->GetStringUTFRegion(string, 0, env->GetStringLength(string), &buf[0]);

    BIO* result = BIO_new(BIO_s_mem());
    BIO_puts(result, &buf[0]);
    return result;
}

/**
 * public static native int SSL_new(int ssl_ctx, String privatekey, String certificate, byte[] seed) throws IOException;
 */
static jint NativeCrypto_SSL_new(JNIEnv* env, jclass,
        jint ssl_ctx_address, jstring privatekey, jstring certificates, jbyteArray seed)
{
    SSL_CTX* ssl_ctx = reinterpret_cast<SSL_CTX*>(static_cast<uintptr_t>(ssl_ctx_address));
    if (ssl_ctx == NULL) {
        jniThrowNullPointerException(env, "SSL_CTX is null");
        return 0;
    }

    // 'seed == null' when no SecureRandom Object is set
    // in the SSLContext.
    if (seed != NULL) {
        jbyte* randseed = env->GetByteArrayElements(seed, NULL);
        RAND_seed((unsigned char*) randseed, 1024);
        env->ReleaseByteArrayElements(seed, randseed, 0);
    } else {
        RAND_load_file("/dev/urandom", 1024);
    }

    SSL* ssl = SSL_new(ssl_ctx);
    if (ssl == NULL) {
        throwIOExceptionWithSslErrors(env, 0, 0,
                "Unable to create SSL structure");
        return NULL;
    }

    /* Java code in class OpenSSLSocketImpl does the verification. Meaning of
     * SSL_VERIFY_NONE flag in client mode: if not using an anonymous cipher
     * (by default disabled), the server will send a certificate which will
     * be checked. The result of the certificate verification process can be
     * checked after the TLS/SSL handshake using the SSL_get_verify_result(3)
     * function. The handshake will be continued regardless of the
     * verification result.
     */
    SSL_set_verify(ssl, SSL_VERIFY_NONE, NULL);

    if (privatekey != NULL) {
        BIO* privatekeybio = stringToMemBuf(env, (jstring) privatekey);
        EVP_PKEY* privatekeyevp =
          PEM_read_bio_PrivateKey(privatekeybio, NULL, 0, NULL);
        BIO_free(privatekeybio);

        if (privatekeyevp == NULL) {
            LOGE(ERR_error_string(ERR_get_error(), NULL));
            throwIOExceptionWithSslErrors(env, 0, 0,
                    "Error parsing the private key");
            SSL_free(ssl);
            return NULL;
        }

        BIO* certificatesbio = stringToMemBuf(env, (jstring) certificates);
        X509* certificatesx509 =
          PEM_read_bio_X509(certificatesbio, NULL, 0, NULL);
        BIO_free(certificatesbio);

        if (certificatesx509 == NULL) {
            LOGE(ERR_error_string(ERR_get_error(), NULL));
            throwIOExceptionWithSslErrors(env, 0, 0,
                    "Error parsing the certificates");
            EVP_PKEY_free(privatekeyevp);
            SSL_free(ssl);
            return NULL;
        }

        int ret = SSL_use_certificate(ssl, certificatesx509);
        if (ret != 1) {
            LOGE(ERR_error_string(ERR_get_error(), NULL));
            throwIOExceptionWithSslErrors(env, ret, 0,
                    "Error setting the certificates");
            X509_free(certificatesx509);
            EVP_PKEY_free(privatekeyevp);
            SSL_free(ssl);
            return NULL;
        }

        ret = SSL_use_PrivateKey(ssl, privatekeyevp);
        if (ret != 1) {
            LOGE(ERR_error_string(ERR_get_error(), NULL));
            throwIOExceptionWithSslErrors(env, ret, 0,
                    "Error setting the private key");
            X509_free(certificatesx509);
            EVP_PKEY_free(privatekeyevp);
            SSL_free(ssl);
            return NULL;
        }

        ret = SSL_check_private_key(ssl);
        if (ret != 1) {
            throwIOExceptionWithSslErrors(env, ret, 0,
                    "Error checking the private key");
            X509_free(certificatesx509);
            EVP_PKEY_free(privatekeyevp);
            SSL_free(ssl);
            return NULL;
        }
    }
    return (jint)ssl;
}

/**
 * public static native long SSL_get_options(int ssl);
 */
static jlong NativeCrypto_SSL_get_options(JNIEnv* env, jclass,
        jint ssl_address) {
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
      return 0;
    }
    return SSL_get_options(ssl);
}

/**
 * public static native long SSL_set_options(int ssl, long options);
 */
static jlong NativeCrypto_SSL_set_options(JNIEnv* env, jclass,
        jint ssl_address, jlong options) {
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
      return 0 ;
    }
    return SSL_set_options(ssl, options);
}

/**
 * Loads the ciphers suites that are enabled in the SSL
 * and returns them in a string array.
 */
static jobjectArray NativeCrypto_SSL_get_ciphers(JNIEnv* env,
        jclass, jint ssl_address)
{
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
      return NULL;
    }
    return makeCipherList(env, SSL_get_ciphers(ssl));
}

/**
 * Sets the ciphers suites that are enabled in the SSL
 */
static void NativeCrypto_SSL_set_cipher_list(JNIEnv* env, jclass,
        jint ssl_address, jstring controlString)
{
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
      return;
    }
    const char* str = env->GetStringUTFChars(controlString, NULL);
    int rc = SSL_set_cipher_list(ssl, str);
    env->ReleaseStringUTFChars(controlString, str);
    if (rc == 0) {
        freeSslErrorState();
        jniThrowException(env, "java/lang/IllegalArgumentException",
                          "Illegal cipher suite strings.");
    }
}

/**
 * Our additional application data needed for getting synchronization right.
 * This maybe warrants a bit of lengthy prose:
 *
 * (1) We use a flag to reflect whether we consider the SSL connection alive.
 * Any read or write attempt loops will be cancelled once this flag becomes 0.
 *
 * (2) We use an int to count the number of threads that are blocked by the
 * underlying socket. This may be at most two (one reader and one writer), since
 * the Java layer ensures that no more threads will enter the native code at the
 * same time.
 *
 * (3) The pipe is used primarily as a means of cancelling a blocking select()
 * when we want to close the connection (aka "emergency button"). It is also
 * necessary for dealing with a possible race condition situation: There might
 * be cases where both threads see an SSL_ERROR_WANT_READ or
 * SSL_ERROR_WANT_WRITE. Both will enter a select() with the proper argument.
 * If one leaves the select() successfully before the other enters it, the
 * "success" event is already consumed and the second thread will be blocked,
 * possibly forever (depending on network conditions).
 *
 * The idea for solving the problem looks like this: Whenever a thread is
 * successful in moving around data on the network, and it knows there is
 * another thread stuck in a select(), it will write a byte to the pipe, waking
 * up the other thread. A thread that returned from select(), on the other hand,
 * knows whether it's been woken up by the pipe. If so, it will consume the
 * byte, and the original state of affairs has been restored.
 *
 * The pipe may seem like a bit of overhead, but it fits in nicely with the
 * other file descriptors of the select(), so there's only one condition to wait
 * for.
 *
 * (4) Finally, a mutex is needed to make sure that at most one thread is in
 * either SSL_read() or SSL_write() at any given time. This is an OpenSSL
 * requirement. We use the same mutex to guard the field for counting the
 * waiting threads.
 *
 * Note: The current implementation assumes that we don't have to deal with
 * problems induced by multiple cores or processors and their respective
 * memory caches. One possible problem is that of inconsistent views on the
 * "aliveAndKicking" field. This could be worked around by also enclosing all
 * accesses to that field inside a lock/unlock sequence of our mutex, but
 * currently this seems a bit like overkill.
 */
typedef struct app_data {
    volatile int aliveAndKicking;
    int waitingThreads;
    int fdsEmergency[2];
    MUTEX_TYPE mutex;
} APP_DATA;

/**
 * Creates our application data and attaches it to a given SSL connection.
 *
 * @param ssl The SSL connection to attach the data to.
 * @return 0 on success, -1 on failure.
 */
static int sslCreateAppData(SSL* ssl) {
    APP_DATA* data = (APP_DATA*) malloc(sizeof(APP_DATA));

    memset(data, 0, sizeof(APP_DATA));

    data->aliveAndKicking = 1;
    data->waitingThreads = 0;
    data->fdsEmergency[0] = -1;
    data->fdsEmergency[1] = -1;

    if (pipe(data->fdsEmergency) == -1) {
        free(data);
        return -1;
    }

    if (MUTEX_SETUP(data->mutex) == -1) {
        free(data);
        return -1;
    }

    SSL_set_app_data(ssl, (char*) data);

    return 0;
}

/**
 * Destroys our application data, cleaning up everything in the process.
 *
 * @param ssl The SSL connection to take the data from.
 */
static void sslDestroyAppData(SSL* ssl) {
    APP_DATA* data = (APP_DATA*) SSL_get_app_data(ssl);

    if (data != NULL) {
        SSL_set_app_data(ssl, NULL);

        data->aliveAndKicking = 0;

        if (data->fdsEmergency[0] != -1) {
            close(data->fdsEmergency[0]);
        }

        if (data->fdsEmergency[1] != -1) {
            close(data->fdsEmergency[1]);
        }

        MUTEX_CLEANUP(data->mutex);

        free(data);
    }
}

/**
 * public static native void SSL_free(int ssl);
 */
static void NativeCrypto_SSL_free(JNIEnv* env, jclass, jint ssl_address)
{
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
      return;
    }
    sslDestroyAppData(ssl);
    SSL_free(ssl);
}

/*
 * Defines the mapping from Java methods and their signatures
 * to native functions. Order is (1) Java name, (2) signature,
 * (3) pointer to C function.
 */
static JNINativeMethod sNativeCryptoMethods[] = {
    { "clinit",              "()V",           (void*)NativeCrypto_clinit},
    { "EVP_PKEY_new_DSA",    "([B[B[B[B[B)I", (void*)NativeCrypto_EVP_PKEY_new_DSA },
    { "EVP_PKEY_new_RSA",    "([B[B[B[B[B)I", (void*)NativeCrypto_EVP_PKEY_new_RSA },
    { "EVP_PKEY_free",       "(I)V",          (void*)NativeCrypto_EVP_PKEY_free },
    { "EVP_new",             "()I",           (void*)NativeCrypto_EVP_new },
    { "EVP_free",            "(I)V",          (void*)NativeCrypto_EVP_free },
    { "EVP_DigestFinal",     "(I[BI)I",       (void*)NativeCrypto_EVP_DigestFinal },
    { "EVP_DigestInit",      "(ILjava/lang/String;)V", (void*)NativeCrypto_EVP_DigestInit },
    { "EVP_DigestBlockSize", "(I)I",          (void*)NativeCrypto_EVP_DigestBlockSize },
    { "EVP_DigestSize",      "(I)I",          (void*)NativeCrypto_EVP_DigestSize },
    { "EVP_DigestUpdate",    "(I[BII)V",      (void*)NativeCrypto_EVP_DigestUpdate },
    { "EVP_VerifyInit",      "(ILjava/lang/String;)V", (void*)NativeCrypto_EVP_VerifyInit },
    { "EVP_VerifyUpdate",    "(I[BII)V",      (void*)NativeCrypto_EVP_VerifyUpdate },
    { "EVP_VerifyFinal",     "(I[BIII)I",     (void*)NativeCrypto_EVP_VerifyFinal },
    { "SSL_CTX_new",         "()I",           (void*)NativeCrypto_SSL_CTX_new },
    { "SSL_CTX_get_ciphers", "(I)[Ljava/lang/String;", (void*)NativeCrypto_SSL_CTX_get_ciphers},
    { "SSL_CTX_free",        "(I)V",          (void*)NativeCrypto_SSL_CTX_free },
    { "SSL_new",             "(ILjava/lang/String;Ljava/lang/String;[B)I", (void*)NativeCrypto_SSL_new},
    { "SSL_get_options",     "(I)J",          (void*)NativeCrypto_SSL_get_options },
    { "SSL_set_options",     "(IJ)J",         (void*)NativeCrypto_SSL_set_options },
    { "SSL_get_ciphers",     "(I)[Ljava/lang/String;", (void*)NativeCrypto_SSL_get_ciphers},
    { "SSL_set_cipher_list", "(ILjava/lang/String;)V", (void*)NativeCrypto_SSL_set_cipher_list},
    { "SSL_free",            "(I)V",          (void*)NativeCrypto_SSL_free},
};

/**
 * Module scope variables initialized during JNI registration.
 */
static jfieldID field_Socket_mImpl;
static jfieldID field_Socket_mFD;

// ============================================================================
// === OpenSSL-related helper stuff begins here. ==============================
// ============================================================================

int get_socket_timeout(int type, int sd) {
    struct timeval tv;
    socklen_t len = sizeof(tv);
    if (getsockopt(sd, SOL_SOCKET, type, &tv, &len) < 0) {
         LOGE("getsockopt(%d, SOL_SOCKET): %s (%d)",
              sd,
              strerror(errno),
              errno);
        return 0;
    }
    // LOGI("Current socket timeout (%d(s), %d(us))!",
    //      (int)tv.tv_sec, (int)tv.tv_usec);
    int timeout = tv.tv_sec * 1000 + tv.tv_usec / 1000;
    return timeout;
}

#ifdef TIMEOUT_DEBUG_SSL

void print_socket_timeout(const char* name, int type, int sd) {
    struct timeval tv;
    int len = sizeof(tv);
    if (getsockopt(sd, SOL_SOCKET, type, &tv, &len) < 0) {
         LOGE("getsockopt(%d, SOL_SOCKET, %s): %s (%d)",
              sd,
              name,
              strerror(errno),
              errno);
    }
    LOGI("Current socket %s is (%d(s), %d(us))!",
          name, (int)tv.tv_sec, (int)tv.tv_usec);
}

void print_timeout(const char* method, SSL* ssl) {
    LOGI("SSL_get_default_timeout %d in %s", SSL_get_default_timeout(ssl), method);
    int fd = SSL_get_fd(ssl);
    print_socket_timeout("SO_RCVTIMEO", SO_RCVTIMEO, fd);
    print_socket_timeout("SO_SNDTIMEO", SO_SNDTIMEO, fd);
}

#endif

/**
 * Dark magic helper function that checks, for a given SSL session, whether it
 * can SSL_read() or SSL_write() without blocking. Takes into account any
 * concurrent attempts to close the SSL session from the Java side. This is
 * needed to get rid of the hangs that occur when thread #1 closes the SSLSocket
 * while thread #2 is sitting in a blocking read or write. The type argument
 * specifies whether we are waiting for readability or writability. It expects
 * to be passed either SSL_ERROR_WANT_READ or SSL_ERROR_WANT_WRITE, since we
 * only need to wait in case one of these problems occurs.
 *
 * @param type Either SSL_ERROR_WANT_READ or SSL_ERROR_WANT_WRITE
 * @param fd The file descriptor to wait for (the underlying socket)
 * @param data The application data structure with mutex info etc.
 * @param timeout The timeout value for select call, with the special value
 *                0 meaning no timeout at all (wait indefinitely). Note: This is
 *                the Java semantics of the timeout value, not the usual
 *                select() semantics.
 * @return The result of the inner select() call, -1 on additional errors
 */
static int sslSelect(int type, int fd, APP_DATA *data, int timeout) {
    fd_set rfds;
    fd_set wfds;

    FD_ZERO(&rfds);
    FD_ZERO(&wfds);

    if (type == SSL_ERROR_WANT_READ) {
        FD_SET(fd, &rfds);
    } else {
        FD_SET(fd, &wfds);
    }

    FD_SET(data->fdsEmergency[0], &rfds);

    int max = fd > data->fdsEmergency[0] ? fd : data->fdsEmergency[0];

    // Build a struct for the timeout data if we actually want a timeout.
    struct timeval tv;
    struct timeval *ptv;
    if (timeout > 0) {
        tv.tv_sec = timeout / 1000;
        tv.tv_usec = 0;
        ptv = &tv;
    } else {
        ptv = NULL;
    }

    // LOGD("Doing select() for SSL_ERROR_WANT_%s...", type == SSL_ERROR_WANT_READ ? "READ" : "WRITE");
    int result = select(max + 1, &rfds, &wfds, NULL, ptv);
    // LOGD("Returned from select(), result is %d", result);

    // Lock
    if (MUTEX_LOCK(data->mutex) == -1) {
        return -1;
    }

    // If we have been woken up by the emergency pipe, there must be a token in
    // it. Thus we can safely read it (even in a blocking way).
    if (FD_ISSET(data->fdsEmergency[0], &rfds)) {
        char token;
        do {
            read(data->fdsEmergency[0], &token, 1);
        } while (errno == EINTR);
    }

    // Tell the world that there is now one thread less waiting for the
    // underlying network.
    data->waitingThreads--;

    // Unlock
    MUTEX_UNLOCK(data->mutex);
    // LOGD("leave sslSelect");
    return result;
}

/**
 * Helper function that wakes up a thread blocked in select(), in case there is
 * one. Is being called by sslRead() and sslWrite() as well as by JNI glue
 * before closing the connection.
 *
 * @param data The application data structure with mutex info etc.
 */
static void sslNotify(APP_DATA *data) {
    // Write a byte to the emergency pipe, so a concurrent select() can return.
    // Note we have to restore the errno of the original system call, since the
    // caller relies on it for generating error messages.
    int errnoBackup = errno;
    char token = '*';
    do {
        errno = 0;
        write(data->fdsEmergency[1], &token, 1);
    } while (errno == EINTR);
    errno = errnoBackup;
}

/**
 * Helper function which does the actual reading. The Java layer guarantees that
 * at most one thread will enter this function at any given time.
 *
 * @param ssl non-null; the SSL context
 * @param buf non-null; buffer to read into
 * @param len length of the buffer, in bytes
 * @param sslReturnCode original SSL return code
 * @param sslErrorCode filled in with the SSL error code in case of error
 * @return number of bytes read on success, -1 if the connection was
 * cleanly shut down, or THROW_EXCEPTION if an exception should be thrown.
 */
static int sslRead(SSL* ssl, char* buf, jint len, int* sslReturnCode,
        int* sslErrorCode, int timeout) {

    // LOGD("Entering sslRead, caller requests to read %d bytes...", len);

    if (len == 0) {
        // Don't bother doing anything in this case.
        return 0;
    }

    int fd = SSL_get_fd(ssl);
    BIO *bio = SSL_get_rbio(ssl);

    APP_DATA* data = (APP_DATA*) SSL_get_app_data(ssl);

    while (data->aliveAndKicking) {
        errno = 0;

        // Lock
        if (MUTEX_LOCK(data->mutex) == -1) {
            return -1;
        }

        unsigned int bytesMoved = BIO_number_read(bio) + BIO_number_written(bio);

        // LOGD("Doing SSL_Read()");
        int result = SSL_read(ssl, buf, len);
        int error = SSL_ERROR_NONE;
        if (result <= 0) {
            error = SSL_get_error(ssl, result);
            freeSslErrorState();
        }
        // LOGD("Returned from SSL_Read() with result %d, error code %d", result, error);

        // If we have been successful in moving data around, check whether it
        // might make sense to wake up other blocked threads, so they can give
        // it a try, too.
        if (BIO_number_read(bio) + BIO_number_written(bio) != bytesMoved && data->waitingThreads > 0) {
            sslNotify(data);
        }

        // If we are blocked by the underlying socket, tell the world that
        // there will be one more waiting thread now.
        if (error == SSL_ERROR_WANT_READ || error == SSL_ERROR_WANT_WRITE) {
            data->waitingThreads++;
        }

        // Unlock
        MUTEX_UNLOCK(data->mutex);

        switch (error) {
             // Sucessfully read at least one byte.
            case SSL_ERROR_NONE: {
                return result;
            }

            // Read zero bytes. End of stream reached.
            case SSL_ERROR_ZERO_RETURN: {
                return -1;
            }

            // Need to wait for availability of underlying layer, then retry.
            case SSL_ERROR_WANT_READ:
            case SSL_ERROR_WANT_WRITE: {
                int selectResult = sslSelect(error, fd, data, timeout);
                if (selectResult == -1) {
                    *sslReturnCode = -1;
                    *sslErrorCode = error;
                    return THROW_EXCEPTION;
                } else if (selectResult == 0) {
                    return THROW_SOCKETTIMEOUTEXCEPTION;
                }

                break;
            }

            // A problem occured during a system call, but this is not
            // necessarily an error.
            case SSL_ERROR_SYSCALL: {
                // Connection closed without proper shutdown. Tell caller we
                // have reached end-of-stream.
                if (result == 0) {
                    return -1;
                }

                // System call has been interrupted. Simply retry.
                if (errno == EINTR) {
                    break;
                }

                // Note that for all other system call errors we fall through
                // to the default case, which results in an Exception.
            }

            // Everything else is basically an error.
            default: {
                *sslReturnCode = result;
                *sslErrorCode = error;
                return THROW_EXCEPTION;
            }
        }
    }

    return -1;
}

/**
 * Helper function which does the actual writing. The Java layer guarantees that
 * at most one thread will enter this function at any given time.
 *
 * @param ssl non-null; the SSL context
 * @param buf non-null; buffer to write
 * @param len length of the buffer, in bytes
 * @param sslReturnCode original SSL return code
 * @param sslErrorCode filled in with the SSL error code in case of error
 * @return number of bytes read on success, -1 if the connection was
 * cleanly shut down, or THROW_EXCEPTION if an exception should be thrown.
 */
static int sslWrite(SSL* ssl, const char* buf, jint len, int* sslReturnCode,
        int* sslErrorCode) {

    // LOGD("Entering sslWrite(), caller requests to write %d bytes...", len);

    if (len == 0) {
        // Don't bother doing anything in this case.
        return 0;
    }

    int fd = SSL_get_fd(ssl);
    BIO *bio = SSL_get_wbio(ssl);

    APP_DATA* data = (APP_DATA*) SSL_get_app_data(ssl);

    int count = len;

    while (data->aliveAndKicking && len > 0) {
        errno = 0;
        if (MUTEX_LOCK(data->mutex) == -1) {
            return -1;
        }

        unsigned int bytesMoved = BIO_number_read(bio) + BIO_number_written(bio);

        // LOGD("Doing SSL_write() with %d bytes to go", len);
        int result = SSL_write(ssl, buf, len);
        int error = SSL_ERROR_NONE;
        if (result <= 0) {
            error = SSL_get_error(ssl, result);
            freeSslErrorState();
        }
        // LOGD("Returned from SSL_write() with result %d, error code %d", result, error);

        // If we have been successful in moving data around, check whether it
        // might make sense to wake up other blocked threads, so they can give
        // it a try, too.
        if (BIO_number_read(bio) + BIO_number_written(bio) != bytesMoved && data->waitingThreads > 0) {
            sslNotify(data);
        }

        // If we are blocked by the underlying socket, tell the world that
        // there will be one more waiting thread now.
        if (error == SSL_ERROR_WANT_READ || error == SSL_ERROR_WANT_WRITE) {
            data->waitingThreads++;
        }

        MUTEX_UNLOCK(data->mutex);

        switch (error) {
             // Sucessfully write at least one byte.
            case SSL_ERROR_NONE: {
                buf += result;
                len -= result;
                break;
            }

            // Wrote zero bytes. End of stream reached.
            case SSL_ERROR_ZERO_RETURN: {
                return -1;
            }

            // Need to wait for availability of underlying layer, then retry.
            // The concept of a write timeout doesn't really make sense, and
            // it's also not standard Java behavior, so we wait forever here.
            case SSL_ERROR_WANT_READ:
            case SSL_ERROR_WANT_WRITE: {
                int selectResult = sslSelect(error, fd, data, 0);
                if (selectResult == -1) {
                    *sslReturnCode = -1;
                    *sslErrorCode = error;
                    return THROW_EXCEPTION;
                } else if (selectResult == 0) {
                    return THROW_SOCKETTIMEOUTEXCEPTION;
                }

                break;
            }

            // An problem occured during a system call, but this is not
            // necessarily an error.
            case SSL_ERROR_SYSCALL: {
                // Connection closed without proper shutdown. Tell caller we
                // have reached end-of-stream.
                if (result == 0) {
                    return -1;
                }

                // System call has been interrupted. Simply retry.
                if (errno == EINTR) {
                    break;
                }

                // Note that for all other system call errors we fall through
                // to the default case, which results in an Exception.
            }

            // Everything else is basically an error.
            default: {
                *sslReturnCode = result;
                *sslErrorCode = error;
                return THROW_EXCEPTION;
            }
        }
    }
    // LOGD("Successfully wrote %d bytes", count);

    return count;
}

/**
 * Helper function that creates an RSA public key from two buffers containing
 * the big-endian bit representation of the modulus and the public exponent.
 *
 * @param mod The data of the modulus
 * @param modLen The length of the modulus data
 * @param exp The data of the exponent
 * @param expLen The length of the exponent data
 *
 * @return A pointer to the new RSA structure, or NULL on error
 */
static RSA* rsaCreateKey(unsigned char* mod, int modLen, unsigned char* exp, int expLen) {
    // LOGD("Entering rsaCreateKey()");

    RSA* rsa = RSA_new();

    rsa->n = BN_bin2bn((unsigned char*) mod, modLen, NULL);
    rsa->e = BN_bin2bn((unsigned char*) exp, expLen, NULL);

    if (rsa->n == NULL || rsa->e == NULL) {
        RSA_free(rsa);
        return NULL;
    }

    return rsa;
}

/**
 * Helper function that frees an RSA key. Just calls the corresponding OpenSSL
 * function.
 *
 * @param rsa The pointer to the new RSA structure to free.
 */
static void rsaFreeKey(RSA* rsa) {
    // LOGD("Entering rsaFreeKey()");

    if (rsa != NULL) {
        RSA_free(rsa);
    }
}

/**
 * Helper function that verifies a given RSA signature for a given message.
 *
 * @param msg The message to verify
 * @param msgLen The length of the message
 * @param sig The signature to verify
 * @param sigLen The length of the signature
 * @param algorithm The name of the hash/sign algorithm to use, e.g. "RSA-SHA1"
 * @param rsa The RSA public key to use
 *
 * @return 1 on success, 0 on failure, -1 on error (check SSL errors then)
 *
 */
static int rsaVerify(unsigned char* msg, unsigned int msgLen, unsigned char* sig,
                     unsigned int sigLen, char* algorithm, RSA* rsa) {

    // LOGD("Entering rsaVerify(%x, %d, %x, %d, %s, %x)", msg, msgLen, sig, sigLen, algorithm, rsa);

    int result = -1;

    EVP_PKEY* key = EVP_PKEY_new();
    EVP_PKEY_set1_RSA(key, rsa);

    const EVP_MD *type = EVP_get_digestbyname(algorithm);
    if (type == NULL) {
        goto cleanup;
    }

    EVP_MD_CTX ctx;

    EVP_MD_CTX_init(&ctx);
    if (EVP_VerifyInit_ex(&ctx, type, NULL) == 0) {
        goto cleanup;
    }

    EVP_VerifyUpdate(&ctx, msg, msgLen);
    result = EVP_VerifyFinal(&ctx, sig, sigLen, key);
    EVP_MD_CTX_cleanup(&ctx);

    cleanup:

    if (key != NULL) {
        EVP_PKEY_free(key);
    }

    return result;
}

// ============================================================================
// === OpenSSL-related helper stuff ends here. JNI glue follows. ==============
// ============================================================================

/**
 * A connection within an OpenSSL context is established. (1) A new socket is
 * constructed, (2) the TLS/SSL handshake with a server is initiated.
 */
static jboolean org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_connect(JNIEnv* env, jclass,
        jint ssl_address, jobject socketObject, jint timeout, jboolean client_mode, jint ssl_session_address)
{
    // LOGD("ENTER connect");
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
      return (jboolean) false;
    }
    SSL_SESSION* ssl_session = reinterpret_cast<SSL_SESSION*>(static_cast<uintptr_t>(ssl_session_address));

    jobject socketImplObject = env->GetObjectField(socketObject, field_Socket_mImpl);
    if (socketImplObject == NULL) {
        throwIOExceptionStr(env,
            "couldn't get the socket impl from the socket");
        return (jboolean) false;
    }

    jobject fdObject = env->GetObjectField(socketImplObject, field_Socket_mFD);
    if (fdObject == NULL) {
        throwIOExceptionStr(env,
            "couldn't get the file descriptor from the socket impl");
        return (jboolean) false;
    }

    int fd = jniGetFDFromFileDescriptor(env, fdObject);

    int ret = SSL_set_fd(ssl, fd);

    if (ret != 1) {
        throwIOExceptionWithSslErrors(env, ret, 0,
                "Error setting the file descriptor");
        SSL_clear(ssl);
        return (jboolean) false;
    }

    if (ssl_session != NULL) {
        // LOGD("Trying to reuse session %p", ssl_session);
        ret = SSL_set_session(ssl, ssl_session);
        if (ret != 1) {
            /*
             * Translate the error, and throw if it turns out to be a real
             * problem.
             */
            int sslErrorCode = SSL_get_error(ssl, ret);
            if (sslErrorCode != SSL_ERROR_ZERO_RETURN) {
                throwIOExceptionWithSslErrors(env, ret, sslErrorCode,
                        "SSL session set");
                SSL_clear(ssl);
                return (jboolean) false;
            }
        }
    }

    /*
     * Make socket non-blocking, so SSL_connect SSL_read() and SSL_write() don't hang
     * forever and we can use select() to find out if the socket is ready.
     */
    int mode = fcntl(fd, F_GETFL);
    if (mode == -1 || fcntl(fd, F_SETFL, mode | O_NONBLOCK) == -1) {
        throwIOExceptionStr(env, "Unable to make socket non blocking");
        SSL_clear(ssl);
        return (jboolean) false;
    }

    /*
     * Create our special application data.
     */
    if (sslCreateAppData(ssl) == -1) {
        throwIOExceptionStr(env, "Unable to create application data");
        SSL_clear(ssl);
        // TODO
        return (jboolean) false;
    }

    APP_DATA* data = (APP_DATA*) SSL_get_app_data(ssl);

    while (data->aliveAndKicking) {
        errno = 0;
        ret = SSL_connect(ssl);
        if (ret == 1) {
            break;
        } else if (errno == EINTR) {
            continue;
        } else {
            // LOGD("SSL_connect: result %d, errno %d, timeout %d", ret, errno, timeout);
            int error = SSL_get_error(ssl, ret);

            /*
             * If SSL_connect doesn't succeed due to the socket being
             * either unreadable or unwritable, we use sslSelect to
             * wait for it to become ready. If that doesn't happen
             * before the specified timeout or an error occurs, we
             * cancel the handshake. Otherwise we try the SSL_connect
             * again.
             */
            if (error == SSL_ERROR_WANT_READ || error == SSL_ERROR_WANT_WRITE) {
                data->waitingThreads++;
                int selectResult = sslSelect(error, fd, data, timeout);

                if (selectResult == -1) {
                    throwIOExceptionWithSslErrors(env, -1, error,
                        "Connect error");
                    SSL_clear(ssl);
                    return (jboolean) false;
                } else if (selectResult == 0) {
                    throwSocketTimeoutException(env, "SSL handshake timed out");
                    SSL_clear(ssl);
                    freeSslErrorState();
                    return (jboolean) false;
                }
            } else {
                LOGE("Unknown error %d during connect", error);
                break;
            }
        }
    }

    if (ret != 1) {
        /*
         * Translate the error, and throw if it turns out to be a real
         * problem.
         */
        int sslErrorCode = SSL_get_error(ssl, ret);
        if (sslErrorCode != SSL_ERROR_ZERO_RETURN) {
            throwIOExceptionWithSslErrors(env, ret, sslErrorCode,
                    "SSL handshake failure");
            SSL_clear(ssl);
            return (jboolean) false;
        }
    }

    if (ssl_session != NULL) {
        ret = SSL_session_reused(ssl);
        // if (ret == 1) LOGD("Session %p was reused", ssl_session);
        // else LOGD("Session %p was not reused, using new session %p", ssl_session, SSL_get_session(ssl));
        return (jboolean) ret;
    } else {
        // LOGD("New session %p was negotiated", SSL_get_session(ssl));
        return (jboolean) 0;
    }
    // LOGD("LEAVE connect");
}

static jint org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_getsslsession(JNIEnv* env, jclass,
        jint jssl)
{
    return (jint) SSL_get1_session((SSL*) jssl);
}

static jint org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_accept(JNIEnv* env, jclass,
        jint ssl_address, jobject socketObject)
{
    SSL* serverSocketSsl = reinterpret_cast<SSL*>(static_cast<uintptr_t>(ssl_address));
    if (serverSocketSsl == NULL) {
        throwIOExceptionWithSslErrors(env, 0, 0,
                "Unusable SSL structure");
        return NULL;
    }

    SSL* ssl = SSL_dup(serverSocketSsl);
    if (ssl == NULL) {
        throwIOExceptionWithSslErrors(env, 0, 0,
                "Unable to create SSL structure");
        return NULL;
    }

    jobject socketImplObject = env->GetObjectField(socketObject, field_Socket_mImpl);
    if (socketImplObject == NULL) {
        throwIOExceptionStr(env, "couldn't get the socket impl from the socket");
        return NULL;
    }

    jobject fdObject = env->GetObjectField(socketImplObject, field_Socket_mFD);
    if (fdObject == NULL) {
        throwIOExceptionStr(env, "couldn't get the file descriptor from the socket impl");
        return NULL;
    }


    int sd = jniGetFDFromFileDescriptor(env, fdObject);

    BIO* bio = BIO_new_socket(sd, BIO_NOCLOSE);
    SSL_set_bio(ssl, bio, bio);

    /*
     * Fill in the stack allocated appdata structure needed for the
     * certificate callback and store this in the SSL application data
     * slot.
     */
    jsse_ssl_app_data_t appdata;
    appdata.env = env;
    appdata.object = socketObject;
    SSL_set_app_data(ssl, &appdata);

    /*
     * Do the actual SSL_accept(). It is possible this code is insufficient.
     * Maybe we need to deal with all the special SSL error cases (WANT_*),
     * just like we do for SSL_connect(). But currently it is looking ok.
     */
    int ret = SSL_accept(ssl);

    /*
     * Clear the SSL application data slot again, so we can safely use it for
     * our ordinary synchronization structure afterwards. Also, we don't want
     * sslDestroyAppData() to think that there is something that needs to be
     * freed right now (in case of an error).
     */
    SSL_set_app_data(ssl, NULL);

    if (ret == 0) {
        /*
         * The other side closed the socket before the handshake could be
         * completed, but everything is within the bounds of the TLS protocol.
         * We still might want to find out the real reason of the failure.
         */
        int sslErrorCode = SSL_get_error(ssl, ret);
        if (sslErrorCode == SSL_ERROR_NONE ||
            (sslErrorCode == SSL_ERROR_SYSCALL && errno == 0)) {
          throwIOExceptionStr(env, "Connection closed by peer");
        } else {
          throwIOExceptionWithSslErrors(env, ret, sslErrorCode,
              "Trouble accepting connection");
    	}
        SSL_clear(ssl);
        return NULL;
    } else if (ret < 0) {
        /*
         * Translate the error and throw exception. We are sure it is an error
         * at this point.
         */
        int sslErrorCode = SSL_get_error(ssl, ret);
        throwIOExceptionWithSslErrors(env, ret, sslErrorCode,
                "Trouble accepting connection");
        SSL_clear(ssl);
        return NULL;
    }

    /*
     * Make socket non-blocking, so SSL_read() and SSL_write() don't hang
     * forever and we can use select() to find out if the socket is ready.
     */
    int fd = SSL_get_fd(ssl);
    int mode = fcntl(fd, F_GETFL);
    if (mode == -1 || fcntl(fd, F_SETFL, mode | O_NONBLOCK) == -1) {
        throwIOExceptionStr(env, "Unable to make socket non blocking");
        SSL_clear(ssl);
        return NULL;
    }

    /*
     * Create our special application data.
     */
    if (sslCreateAppData(ssl) == -1) {
        throwIOExceptionStr(env, "Unable to create application data");
        SSL_clear(ssl);
        return NULL;
    }

    return (jint) ssl;
}

#define SSL_AUTH_MASK           0x00007F00L
#define SSL_aRSA                0x00000100L /* Authenticate with RSA */
#define SSL_aDSS                0x00000200L /* Authenticate with DSS */
#define SSL_DSS                 SSL_aDSS
#define SSL_aFZA                0x00000400L
#define SSL_aNULL               0x00000800L /* no Authenticate, ADH */
#define SSL_aDH                 0x00001000L /* no Authenticate, ADH */
#define SSL_aKRB5               0x00002000L /* Authenticate with KRB5 */
#define SSL_aECDSA              0x00004000L /* Authenticate with ECDSA */

/**
 * Sets  the client's crypto algorithms and authentication methods.
 */
static jstring org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_cipherauthenticationmethod(JNIEnv* env,
        jclass, jint ssl_address)
{
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
        return NULL;
    }

    SSL_CIPHER* cipher = SSL_get_current_cipher(ssl);

    unsigned long alg = cipher->algorithms;

    const char *au;
    switch (alg&SSL_AUTH_MASK) {
        case SSL_aRSA:
            au="RSA";
            break;
        case SSL_aDSS:
            au="DSS";
            break;
        case SSL_aDH:
            au="DH";
            break;
        case SSL_aFZA:
            au = "FZA";
            break;
        case SSL_aNULL:
            au="None";
            break;
        case SSL_aECDSA:
            au="ECDSA";
            break;
        default:
            au="unknown";
            break;
    }

    jstring ret = env->NewStringUTF(au);

    return ret;
}

/**
 * OpenSSL read function (1): only one chunk is read (returned as jint).
 */
static jint org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_read(JNIEnv* env, jclass, jint ssl_address, jint timeout)
{
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
        return 0;
    }

    unsigned char byteRead;
    int returnCode = 0;
    int errorCode = 0;

    int ret = sslRead(ssl, (char *) &byteRead, 1, &returnCode, &errorCode, timeout);

    switch (ret) {
        case THROW_EXCEPTION:
            // See sslRead() regarding improper failure to handle normal cases.
            throwIOExceptionWithSslErrors(env, returnCode, errorCode,
                    "Read error");
            return -1;
        case THROW_SOCKETTIMEOUTEXCEPTION:
            throwSocketTimeoutException(env, "Read timed out");
            return -1;
        case -1:
            // Propagate EOF upwards.
            return -1;
        default:
            // Return the actual char read, make sure it stays 8 bits wide.
            return ((jint) byteRead) & 0xFF;
    }
}

/**
 * OpenSSL read function (2): read into buffer at offset n chunks.
 * Returns 1 (success) or value <= 0 (failure).
 */
static jint org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_readba(JNIEnv* env, jclass, jint ssl_address, jbyteArray dest, jint offset, jint len, jint timeout)
{
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
        return 0;
    }

    jbyte* bytes = env->GetByteArrayElements(dest, NULL);
    int returnCode = 0;
    int errorCode = 0;

    int ret =
        sslRead(ssl, (char*) (bytes + offset), len, &returnCode, &errorCode, timeout);

    env->ReleaseByteArrayElements(dest, bytes, 0);

    if (ret == THROW_EXCEPTION) {
        // See sslRead() regarding improper failure to handle normal cases.
        throwIOExceptionWithSslErrors(env, returnCode, errorCode,
                "Read error");
        return -1;
    } else if(ret == THROW_SOCKETTIMEOUTEXCEPTION) {
        throwSocketTimeoutException(env, "Read timed out");
        return -1;
    }

    return ret;
}

/**
 * OpenSSL write function (1): only one chunk is written.
 */
static void org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_write(JNIEnv* env, jclass, jint ssl_address, jint b)
{
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
        return;
    }

    int returnCode = 0;
    int errorCode = 0;
    char buf[1] = { (char) b };
    int ret = sslWrite(ssl, buf, 1, &returnCode, &errorCode);

    if (ret == THROW_EXCEPTION) {
        // See sslWrite() regarding improper failure to handle normal cases.
        throwIOExceptionWithSslErrors(env, returnCode, errorCode,
                "Write error");
    } else if(ret == THROW_SOCKETTIMEOUTEXCEPTION) {
        throwSocketTimeoutException(env, "Write timed out");
    }
}

/**
 * OpenSSL write function (2): write into buffer at offset n chunks.
 */
static void org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_writeba(JNIEnv* env, jclass,
        jint ssl_address, jbyteArray dest, jint offset, jint len)
{
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
        return;
    }

    jbyte* bytes = env->GetByteArrayElements(dest, NULL);
    int returnCode = 0;
    int errorCode = 0;
    int ret = sslWrite(ssl, (const char *) (bytes + offset), len,
            &returnCode, &errorCode);

    env->ReleaseByteArrayElements(dest, bytes, 0);

    if (ret == THROW_EXCEPTION) {
        // See sslWrite() regarding improper failure to handle normal cases.
        throwIOExceptionWithSslErrors(env, returnCode, errorCode,
                "Write error");
    } else if(ret == THROW_SOCKETTIMEOUTEXCEPTION) {
        throwSocketTimeoutException(env, "Write timed out");
    }
}

/**
 * Interrupt any pending IO before closing the socket.
 */
static void org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_interrupt(
        JNIEnv* env, jclass, jint ssl_address) {
    SSL* ssl = getSslPointer(env, ssl_address, false);
    if (ssl == NULL) {
        return;
    }

    /*
     * Mark the connection as quasi-dead, then send something to the emergency
     * file descriptor, so any blocking select() calls are woken up.
     */
    APP_DATA* data = (APP_DATA*) SSL_get_app_data(ssl);
    if (data != NULL) {
        data->aliveAndKicking = 0;

        // At most two threads can be waiting.
        sslNotify(data);
        sslNotify(data);
    }
}

/**
 * OpenSSL close SSL socket function.
 */
static void org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_close(
        JNIEnv* env, jclass, jint ssl_address) {
    SSL* ssl = getSslPointer(env, ssl_address, false);
    if (ssl == NULL) {
        return;
    }

    /*
     * Try to make socket blocking again. OpenSSL literature recommends this.
     */
    int fd = SSL_get_fd(ssl);
    if (fd != -1) {
        int mode = fcntl(fd, F_GETFL);
        if (mode == -1 || fcntl(fd, F_SETFL, mode & ~O_NONBLOCK) == -1) {
//            throwIOExceptionStr(env, "Unable to make socket blocking again");
//            LOGW("Unable to make socket blocking again");
        }
    }

    int ret = SSL_shutdown(ssl);
    switch (ret) {
        case 0:
            /*
             * Shutdown was not successful (yet), but there also
             * is no error. Since we can't know whether the remote
             * server is actually still there, and we don't want to
             * get stuck forever in a second SSL_shutdown() call, we
             * simply return. This is not security a problem as long
             * as we close the underlying socket, which we actually
             * do, because that's where we are just coming from.
             */
            break;
        case 1:
            /*
             * Shutdown was sucessful. We can safely return. Hooray!
             */
            break;
        default:
            /*
             * Everything else is a real error condition. We should
             * let the Java layer know about this by throwing an
             * exception.
             */
            int sslErrorCode = SSL_get_error(ssl, ret);
            throwIOExceptionWithSslErrors(env, ret, sslErrorCode, "SSL shutdown failed");
            break;
    }

    SSL_clear(ssl);
    freeSslErrorState();
}

/**
 * Verifies an RSA signature.
 */
static int org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_verifysignature(JNIEnv* env, jclass clazz,
        jbyteArray msg, jbyteArray sig, jstring algorithm, jbyteArray mod, jbyteArray exp) {

    // LOGD("Entering verifysignature()");

    if (msg == NULL || sig == NULL || algorithm == NULL || mod == NULL || exp == NULL) {
        jniThrowNullPointerException(env, NULL);
        return -1;
    }

    int result = -1;

    jbyte* msgBytes = env->GetByteArrayElements(msg, NULL);
    jint msgLength = env->GetArrayLength(msg);

    jbyte* sigBytes = env->GetByteArrayElements(sig, NULL);
    jint sigLength = env->GetArrayLength(sig);

    jbyte* modBytes = env->GetByteArrayElements(mod, NULL);
    jint modLength = env->GetArrayLength(mod);

    jbyte* expBytes = env->GetByteArrayElements(exp, NULL);
    jint expLength = env->GetArrayLength(exp);

    const char* algorithmChars = env->GetStringUTFChars(algorithm, NULL);

    RSA* rsa = rsaCreateKey((unsigned char*) modBytes, modLength, (unsigned char*) expBytes, expLength);
    if (rsa != NULL) {
        result = rsaVerify((unsigned char*) msgBytes, msgLength, (unsigned char*) sigBytes, sigLength,
                (char*) algorithmChars, rsa);
        rsaFreeKey(rsa);
    }

    env->ReleaseStringUTFChars(algorithm, algorithmChars);

    env->ReleaseByteArrayElements(exp, expBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(mod, modBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(sig, sigBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(msg, msgBytes, JNI_ABORT);

    if (result == -1) {
        int error = ERR_get_error();
        if (error != 0) {
            char message[50];
            ERR_error_string_n(error, message, sizeof(message));
            jniThrowRuntimeException(env, message);
        } else {
            jniThrowRuntimeException(env, "Internal error during verification");
        }
        freeSslErrorState();
    }

    return result;
}

static JNINativeMethod sSocketImplMethods[] =
{
    {"nativeconnect", "(ILjava/net/Socket;IZI)Z", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_connect},
    {"nativegetsslsession", "(I)I", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_getsslsession},
    {"nativeread", "(II)I", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_read},
    {"nativeread", "(I[BIII)I", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_readba},
    {"nativewrite", "(II)V", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_write},
    {"nativewrite", "(I[BII)V", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_writeba},
    {"nativeaccept", "(ILjava/net/Socket;)I", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_accept},
    {"nativecipherauthenticationmethod", "(I)Ljava/lang/String;", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_cipherauthenticationmethod},
    {"nativeinterrupt", "(I)V", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_interrupt},
    {"nativeclose", "(I)V", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_close},
    {"nativeverifysignature", "([B[BLjava/lang/String;[B[B)I", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLSocketImpl_verifysignature},
};

/**
 * Gives an array back containing all the X509 certificate's bytes.
 */
static jobjectArray getcertificatebytes(JNIEnv* env,
        const STACK_OF(X509) *chain)
{
    BUF_MEM *bptr;
    int count, i;
    jbyteArray bytes;
    jobjectArray joa;

    if (chain == NULL) {
        // Chain can be NULL if the associated cipher doesn't do certs.
        return NULL;
    }

    count = sk_X509_num(chain);

    if (count > 0) {
        joa = env->NewObjectArray(count, env->FindClass("[B"), NULL);

        if (joa == NULL) {
            return NULL;
        }

        BIO *bio = BIO_new(BIO_s_mem());

        // LOGD("Start fetching the certificates");
        for (i = 0; i < count; i++) {
            X509 *cert = sk_X509_value(chain, i);

            BIO_reset(bio);
            PEM_write_bio_X509(bio, cert);

            BIO_get_mem_ptr(bio, &bptr);
            bytes = env->NewByteArray(bptr->length);

            if (bytes == NULL) {
                /*
                 * Indicate an error by resetting joa to NULL. It will
                 * eventually get gc'ed.
                 */
                joa = NULL;
                break;
            } else {
                jbyte* src = reinterpret_cast<jbyte*>(bptr->data);
                env->SetByteArrayRegion(bytes, 0, bptr->length, src);
                env->SetObjectArrayElement(joa, i, bytes);
            }
        }

        // LOGD("Certificate fetching complete");
        BIO_free(bio);
        return joa;
    } else {
        return NULL;
    }
}

/**
 * Verify the X509 certificate.
 */
static int verify_callback(int preverify_ok, X509_STORE_CTX *x509_store_ctx)
{
    /* Get the correct index to the SSLobject stored into X509_STORE_CTX. */
    SSL* ssl = (SSL*)X509_STORE_CTX_get_ex_data(x509_store_ctx, SSL_get_ex_data_X509_STORE_CTX_idx());

    jsse_ssl_app_data_t* appdata = (jsse_ssl_app_data_t*)SSL_get_app_data(ssl);

    jclass cls = appdata->env->GetObjectClass(appdata->object);

    jmethodID methodID = appdata->env->GetMethodID(cls, "verifyCertificateChain", "([[B)Z");

    jobjectArray objectArray = getcertificatebytes(appdata->env, x509_store_ctx->untrusted);

    jboolean verified = appdata->env->CallBooleanMethod(appdata->object, methodID, objectArray);

    return (verified) ? 1 : 0;
}

/**
 * Sets  the client's credentials and the depth of theirs verification.
 */
static void org_apache_harmony_xnet_provider_jsse_OpenSSLServerSocketImpl_nativesetclientauth(JNIEnv* env,
        jclass, jint ssl_address, jint value)
{
    SSL* ssl = getSslPointer(env, ssl_address, true);
    if (ssl == NULL) {
      return;
    }
    SSL_set_verify(ssl, (int)value, verify_callback);
}

static JNINativeMethod sServerSocketImplMethods[] =
{
    {"nativesetclientauth", "(II)V", (void*)org_apache_harmony_xnet_provider_jsse_OpenSSLServerSocketImpl_nativesetclientauth},
};

/**
 * Our implementation of what might be considered
 * SSL_SESSION_get_peer_cert_chain
 */
static STACK_OF(X509)* SSL_SESSION_get_peer_cert_chain(SSL_CTX* ssl_ctx, SSL_SESSION* ssl_session) {
    SSL* ssl = SSL_new(ssl_ctx);
    SSL_set_session(ssl, ssl_session);
    STACK_OF(X509)* chain = SSL_get_peer_cert_chain(ssl);
    SSL_free(ssl);
    return chain;
}

// Fills a byte[][] with the peer certificates in the chain.
static jobjectArray OpenSSLSessionImpl_getPeerCertificatesImpl(JNIEnv* env,
        jclass, jint ssl_ctx_address, jint ssl_session_address)
{
    SSL_CTX* ssl_ctx = reinterpret_cast<SSL_CTX*>(static_cast<uintptr_t>(ssl_ctx_address));
    if (ssl_ctx == NULL) {
        jniThrowNullPointerException(env, "SSL_CTX is null");
        return NULL;
    }
    SSL_SESSION* ssl_session = reinterpret_cast<SSL_SESSION*>(static_cast<uintptr_t>(ssl_session_address));
    STACK_OF(X509)* chain = SSL_SESSION_get_peer_cert_chain(ssl_ctx, ssl_session);
    jobjectArray objectArray = getcertificatebytes(env, chain);
    return objectArray;
}

/**
 * Serializes the native state of the session (ID, cipher, and keys but
 * not certificates). Returns a byte[] containing the DER-encoded state.
 * See apache mod_ssl.
 */
static jbyteArray OpenSSLSessionImpl_getEncoded(JNIEnv* env, jclass, jint ssl_session_address) {
    SSL_SESSION* ssl_session = reinterpret_cast<SSL_SESSION*>(static_cast<uintptr_t>(ssl_session_address));
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
static jint OpenSSLSessionImpl_initializeNativeImpl(JNIEnv* env, jclass, jbyteArray bytes, jint size) {
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
static jbyteArray OpenSSLSessionImpl_getId(JNIEnv* env, jclass, jint ssl_session_address) {
    SSL_SESSION* ssl_session = reinterpret_cast<SSL_SESSION*>(static_cast<uintptr_t>(ssl_session_address));

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
static jlong OpenSSLSessionImpl_getCreationTime(JNIEnv* env, jclass, jint ssl_session_address) {
    SSL_SESSION* ssl_session = reinterpret_cast<SSL_SESSION*>(static_cast<uintptr_t>(ssl_session_address));
    jlong result = SSL_SESSION_get_time(ssl_session);
    result *= 1000; // OpenSSL uses seconds, Java uses milliseconds.
    return result;
}

/**
 * Our implementation of what might be considered
 * SSL_SESSION_get_version, based on SSL_get_version.
 * See get_ssl_version above.
 */
static const char* SSL_SESSION_get_version(SSL_SESSION* ssl_session) {
  return get_ssl_version(ssl_session->ssl_version);
}

/**
 * Gets and returns in a string the version of the SSL protocol. If it
 * returns the string "unknown" it means that no connection is established.
 */
static jstring OpenSSLSessionImpl_getProtocol(JNIEnv* env, jclass, jint ssl_session_address) {
    SSL_SESSION* ssl_session = reinterpret_cast<SSL_SESSION*>(static_cast<uintptr_t>(ssl_session_address));
    const char* protocol = SSL_SESSION_get_version(ssl_session);
    jstring result = env->NewStringUTF(protocol);
    return result;
}

/**
 * Gets and returns in a string the set of ciphers the actual SSL session uses.
 */
static jstring OpenSSLSessionImpl_getCipherSuite(JNIEnv* env, jclass, jint ssl_session_address) {
    SSL_SESSION* ssl_session = reinterpret_cast<SSL_SESSION*>(static_cast<uintptr_t>(ssl_session_address));
    SSL_CIPHER* cipher = ssl_session->cipher;
    jstring result = env->NewStringUTF(SSL_CIPHER_get_name(cipher));
    return result;
}

/**
 * Frees the SSL session.
 */
static void OpenSSLSessionImpl_freeImpl(JNIEnv* env, jclass, jint session) {
    SSL_SESSION* ssl_session = reinterpret_cast<SSL_SESSION*>(session);
    // LOGD("Freeing OpenSSL session %p", session);
    SSL_SESSION_free(ssl_session);
}

static JNINativeMethod sSessionImplMethods[] = {
    { "freeImpl", "(I)V", (void*) OpenSSLSessionImpl_freeImpl },
    { "getCipherSuite", "(I)Ljava/lang/String;", (void*) OpenSSLSessionImpl_getCipherSuite },
    { "getCreationTime", "(I)J", (void*) OpenSSLSessionImpl_getCreationTime },
    { "getEncoded", "(I)[B", (void*) OpenSSLSessionImpl_getEncoded },
    { "getId", "(I)[B", (void*) OpenSSLSessionImpl_getId },
    { "getPeerCertificatesImpl", "(II)[[B", (void*) OpenSSLSessionImpl_getPeerCertificatesImpl },
    { "getProtocol", "(I)Ljava/lang/String;", (void*) OpenSSLSessionImpl_getProtocol },
    { "initializeNativeImpl", "([BI)I", (void*) OpenSSLSessionImpl_initializeNativeImpl },
};

typedef struct {
    const char*            name;
    const JNINativeMethod* methods;
    jint                   nMethods;
} JNINativeClass;

static JNINativeClass sClasses[] = {
    { "org/apache/harmony/xnet/provider/jsse/NativeCrypto", sNativeCryptoMethods, NELEM(sNativeCryptoMethods) },
    { "org/apache/harmony/xnet/provider/jsse/OpenSSLSocketImpl", sSocketImplMethods, NELEM(sSocketImplMethods) },
    { "org/apache/harmony/xnet/provider/jsse/OpenSSLServerSocketImpl", sServerSocketImplMethods, NELEM(sServerSocketImplMethods) },
    { "org/apache/harmony/xnet/provider/jsse/OpenSSLSessionImpl", sSessionImplMethods, NELEM(sSessionImplMethods) },
};

/*
 * Peforms the actual registration of the native methods.
 * Also looks up the fields that belong to the class (if
 * any) and stores the field IDs. Simply remove what you
 * don't need.
 */
extern "C" int register_org_apache_harmony_xnet_provider_jsse_NativeCrypto(JNIEnv* env) {

    // Register org.apache.harmony.xnet.provider.jsse.* methods
    for (int i = 0; i < NELEM(sClasses); i++) {
        int result = jniRegisterNativeMethods(env,
                                              sClasses[i].name,
                                              sClasses[i].methods,
                                              sClasses[i].nMethods);
        if (result == -1) {
          return -1;
        }
    }

    // java.net.Socket
    jclass socket = env->FindClass("java/net/Socket");
    if (socket == NULL) {
        LOGE("Can't find class java.net.Socket");
        return -1;
    }
    field_Socket_mImpl = env->GetFieldID(socket, "impl", "Ljava/net/SocketImpl;");
    if (field_Socket_mImpl == NULL) {
        LOGE("Can't find field impl in class java.net.Socket");
        return -1;
    }

    // java.net.SocketImpl
    jclass socketImplClass = env->FindClass("java/net/SocketImpl");
    if (socketImplClass == NULL) {
        LOGE("Can't find class java.net.SocketImpl");
        return -1;
    }
    field_Socket_mFD = env->GetFieldID(socketImplClass, "fd", "Ljava/io/FileDescriptor;");
    if (field_Socket_mFD == NULL) {
        LOGE("Can't find field fd in java.net.SocketImpl");
        return -1;
    }

    return 0;
}
