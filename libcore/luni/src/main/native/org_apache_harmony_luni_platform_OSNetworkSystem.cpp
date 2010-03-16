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

// BEGIN android-changed
//
// This file has been substantially reworked in order to provide more IPv6
// support and to move functionality from Java to native code where it made
// sense (e.g. when converting between IP addresses, socket structures, and
// strings, for which there exist fast and robust native implementations).

#define LOG_TAG "OSNetworkSystem"

#include "AndroidSystemNatives.h"
#include "JNIHelp.h"
#include "LocalArray.h"
#include "jni.h"

#include <arpa/inet.h>
#include <assert.h>
#include <errno.h>
#include <netdb.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/un.h>
#include <unistd.h>

// Temporary hack to build on systems that don't have up-to-date libc headers.
#ifndef IPV6_TCLASS
#ifdef __linux__
#define IPV6_TCLASS 67 // Linux
#else
#define IPV6_TCLASS -1 // BSD(-like); TODO: Something better than this!
#endif
#endif

/*
 * TODO: The multicast code is highly platform-dependent, and for now
 * we just punt on anything but Linux.
 */
#ifdef __linux__
#define ENABLE_MULTICAST
#endif

/**
 * @name Socket Errors
 * Error codes for socket operations
 *
 * @internal SOCKERR* range from -200 to -299 avoid overlap
 */
#define SOCKERR_NOTINITIALIZED     -201 /* socket library uninitialized */
#define SOCKERR_BADAF              -202 /* bad address family */
#define SOCKERR_BADPROTO           -203 /* bad protocol */
#define SOCKERR_BADTYPE            -204 /* bad type */
#define SOCKERR_SYSTEMBUSY         -205 /* system busy handling requests */
#define SOCKERR_SYSTEMFULL         -206 /* too many sockets */
#define SOCKERR_NOTCONNECTED       -207 /* socket is not connected */
#define SOCKERR_INTERRUPTED        -208 /* the call was cancelled */
#define SOCKERR_TIMEOUT            -209 /* the operation timed out */
#define SOCKERR_CONNRESET          -210 /* the connection was reset */
#define SOCKERR_WOULDBLOCK         -211 /* the socket is marked as nonblocking operation would block */
#define SOCKERR_ADDRNOTAVAIL       -212 /* address not available */
#define SOCKERR_ADDRINUSE          -213 /* address already in use */
#define SOCKERR_NOTBOUND           -214 /* the socket is not bound */
#define SOCKERR_INVALIDTIMEOUT     -216 /* the specified timeout is invalid */
#define SOCKERR_FDSETFULL          -217 /* Unable to create an FDSET */
#define SOCKERR_TIMEVALFULL        -218 /* Unable to create a TIMEVAL */
#define SOCKERR_REMSOCKSHUTDOWN    -219 /* The remote socket has shutdown gracefully */
#define SOCKERR_NOTLISTENING       -220 /* listen() was not invoked prior to accept() */
#define SOCKERR_NOTSTREAMSOCK      -221 /* The socket does not support connection-oriented service */
#define SOCKERR_ALREADYBOUND       -222 /* The socket is already bound to an address */
#define SOCKERR_NBWITHLINGER       -223 /* The socket is marked non-blocking & SO_LINGER is non-zero */
#define SOCKERR_ISCONNECTED        -224 /* The socket is already connected */
#define SOCKERR_NOBUFFERS          -225 /* No buffer space is available */
#define SOCKERR_HOSTNOTFOUND       -226 /* Authoritative Answer Host not found */
#define SOCKERR_NODATA             -227 /* Valid name, no data record of requested type */
#define SOCKERR_BOUNDORCONN        -228 /* The socket has not been bound or is already connected */
#define SOCKERR_OPNOTSUPP          -229 /* The socket does not support the operation */
#define SOCKERR_OPTUNSUPP          -230 /* The socket option is not supported */
#define SOCKERR_OPTARGSINVALID     -231 /* The socket option arguments are invalid */
#define SOCKERR_SOCKLEVELINVALID   -232 /* The socket level is invalid */
#define SOCKERR_TIMEOUTFAILURE     -233
#define SOCKERR_SOCKADDRALLOCFAIL  -234 /* Unable to allocate the sockaddr structure */
#define SOCKERR_FDSET_SIZEBAD      -235 /* The calculated maximum size of the file descriptor set is bad */
#define SOCKERR_UNKNOWNFLAG        -236 /* The flag is unknown */
#define SOCKERR_MSGSIZE            -237 /* The datagram was too big to fit the specified buffer & was truncated. */
#define SOCKERR_NORECOVERY         -238 /* The operation failed with no recovery possible */
#define SOCKERR_ARGSINVALID        -239 /* The arguments are invalid */
#define SOCKERR_BADDESC            -240 /* The socket argument is not a valid file descriptor */
#define SOCKERR_NOTSOCK            -241 /* The socket argument is not a socket */
#define SOCKERR_HOSTENTALLOCFAIL   -242 /* Unable to allocate the hostent structure */
#define SOCKERR_TIMEVALALLOCFAIL   -243 /* Unable to allocate the timeval structure */
#define SOCKERR_LINGERALLOCFAIL    -244 /* Unable to allocate the linger structure */
#define SOCKERR_IPMREQALLOCFAIL    -245 /* Unable to allocate the ipmreq structure */
#define SOCKERR_FDSETALLOCFAIL     -246 /* Unable to allocate the fdset structure */
#define SOCKERR_OPFAILED           -247 /* Operation failed */
#define SOCKERR_VALUE_NULL         -248 /* The value indexed was NULL */
#define SOCKERR_CONNECTION_REFUSED -249 /* connection was refused */
#define SOCKERR_ENETUNREACH        -250 /* network is not reachable */
#define SOCKERR_EACCES             -251 /* permissions do not allow action on socket */
#define SOCKERR_EHOSTUNREACH       -252 /* no route to host */
#define SOCKERR_EPIPE              -253 /* broken pipe */

#define JAVASOCKOPT_TCP_NODELAY 1
#define JAVASOCKOPT_IP_TOS 3
#define JAVASOCKOPT_SO_REUSEADDR 4
#define JAVASOCKOPT_SO_KEEPALIVE 8
#define JAVASOCKOPT_IP_MULTICAST_IF 16
#define JAVASOCKOPT_MCAST_TTL 17
#define JAVASOCKOPT_IP_MULTICAST_LOOP 18
#define JAVASOCKOPT_MCAST_ADD_MEMBERSHIP 19
#define JAVASOCKOPT_MCAST_DROP_MEMBERSHIP 20
#define JAVASOCKOPT_IP_MULTICAST_IF2 31
#define JAVASOCKOPT_SO_BROADCAST 32
#define JAVASOCKOPT_SO_LINGER 128
#define JAVASOCKOPT_REUSEADDR_AND_REUSEPORT  10001
#define JAVASOCKOPT_SO_SNDBUF 4097
#define JAVASOCKOPT_SO_RCVBUF 4098
#define JAVASOCKOPT_SO_RCVTIMEOUT  4102
#define JAVASOCKOPT_SO_OOBINLINE  4099

/* constants for calling multi-call functions */
#define SOCKET_STEP_START 10
#define SOCKET_STEP_CHECK 20
#define SOCKET_STEP_DONE 30

#define BROKEN_MULTICAST_IF 1
#define BROKEN_MULTICAST_TTL 2
#define BROKEN_TCP_NODELAY 4

#define SOCKET_CONNECT_STEP_START 0
#define SOCKET_CONNECT_STEP_CHECK 1

#define SOCKET_OP_NONE 0
#define SOCKET_OP_READ 1
#define SOCKET_OP_WRITE 2

#define SOCKET_NOFLAGS 0

// Local constants for getOrSetSocketOption
#define SOCKOPT_GET 1
#define SOCKOPT_SET 2

struct CachedFields {
    jfieldID fd_descriptor;
    jclass iaddr_class;
    jmethodID iaddr_getbyaddress;
    jclass i4addr_class;
    jmethodID i4addr_class_init;
    jfieldID iaddr_ipaddress;
    jclass genericipmreq_class;
    jclass integer_class;
    jmethodID integer_class_init;
    jfieldID integer_class_value;
    jclass boolean_class;
    jmethodID boolean_class_init;
    jfieldID boolean_class_value;
    jclass byte_class;
    jmethodID byte_class_init;
    jfieldID byte_class_value;
    jclass socketimpl_class;
    jfieldID socketimpl_address;
    jfieldID socketimpl_port;
    jclass dpack_class;
    jfieldID dpack_address;
    jfieldID dpack_port;
    jfieldID dpack_length;
} gCachedFields;

/* needed for connecting with timeout */
struct selectFDSet {
  int nfds;
  int sock;
  fd_set writeSet;
  fd_set readSet;
  fd_set exceptionSet;
};

static const char * netLookupErrorString(int anErrorNum);

/**
 * Throws an SocketException with the message affiliated with the errorCode.
 * 
 * @deprecated: 'errorCode' is one of the bogus SOCKERR_ values, *not* errno.
 * jniThrowSocketException is the better choice.
 */
static void throwSocketException(JNIEnv *env, int errorCode) {
    jniThrowException(env, "java/net/SocketException",
        netLookupErrorString(errorCode));
}

// TODO(enh): move to JNIHelp.h
static void jniThrowExceptionWithErrno(JNIEnv* env,
        const char* exceptionClassName, int error) {
    char buf[BUFSIZ];
    jniThrowException(env, exceptionClassName,
            jniStrError(error, buf, sizeof(buf)));
}

static void jniThrowBindException(JNIEnv* env, int error) {
    jniThrowExceptionWithErrno(env, "java/net/BindException", error);
}

static void jniThrowSocketException(JNIEnv* env, int error) {
    jniThrowExceptionWithErrno(env, "java/net/SocketException", error);
}

static void jniThrowSocketTimeoutException(JNIEnv* env, int error) {
    jniThrowExceptionWithErrno(env, "java/net/SocketTimeoutException", error);
}

// Used by functions that shouldn't throw SocketException. (These functions
// aren't meant to see bad addresses, so seeing one really does imply an
// internal error.)
// TODO: fix the code (native and Java) so we don't paint ourselves into this corner.
static void jniThrowBadAddressFamily(JNIEnv* env) {
    jniThrowException(env, "java/lang/IllegalArgumentException", "Bad address family");
}

static bool jniGetFd(JNIEnv* env, jobject fileDescriptor, int& fd) {
    fd = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (fd == -1) {
        jniThrowSocketException(env, EBADF);
        return false;
    }
    return true;
}

/**
 * Converts a native address structure to a Java byte array.
 */
static jbyteArray socketAddressToByteArray(JNIEnv *env,
        struct sockaddr_storage *address) {

    void *rawAddress;
    size_t addressLength;
    if (address->ss_family == AF_INET) {
        struct sockaddr_in *sin = (struct sockaddr_in *) address;
        rawAddress = &sin->sin_addr.s_addr;
        addressLength = 4;
    } else if (address->ss_family == AF_INET6) {
        struct sockaddr_in6 *sin6 = (struct sockaddr_in6 *) address;
        rawAddress = &sin6->sin6_addr.s6_addr;
        addressLength = 16;
    } else {
        jniThrowBadAddressFamily(env);
        return NULL;
    }

    jbyteArray byteArray = env->NewByteArray(addressLength);
    if (byteArray == NULL) {
        return NULL;
    }
    env->SetByteArrayRegion(byteArray, 0, addressLength, (jbyte *) rawAddress);

    return byteArray;
}

/**
 * Returns the port number in a sockaddr_storage structure.
 *
 * @param address the sockaddr_storage structure to get the port from
 *
 * @return the port number, or -1 if the address family is unknown.
 */
static int getSocketAddressPort(struct sockaddr_storage *address) {
    switch (address->ss_family) {
        case AF_INET:
            return ntohs(((struct sockaddr_in *) address)->sin_port);
        case AF_INET6:
            return ntohs(((struct sockaddr_in6 *) address)->sin6_port);
        default:
            return -1;
    }
}

/**
 * Obtain the socket address family from an existing socket.
 * 
 * @param socket the file descriptor of the socket to examine
 * @return an integer, the address family of the socket
 */
static int getSocketAddressFamily(int socket) {
    sockaddr_storage ss;
    socklen_t namelen = sizeof(ss);
    int ret = getsockname(socket, (sockaddr*) &ss, &namelen);
    if (ret != 0) {
        return AF_UNSPEC;
    } else {
        return ss.ss_family;
    }
}

jobject byteArrayToInetAddress(JNIEnv* env, jbyteArray byteArray) {
    if (byteArray == NULL) {
        return NULL;
    }
    return env->CallStaticObjectMethod(gCachedFields.iaddr_class,
            gCachedFields.iaddr_getbyaddress, byteArray);
}

/**
 * Converts a native address structure to an InetAddress object.
 * Throws a NullPointerException or an IOException in case of
 * error.
 *
 * @param sockAddress the sockaddr_storage structure to convert
 *
 * @return a jobject representing an InetAddress
 */
jobject socketAddressToInetAddress(JNIEnv* env, sockaddr_storage* sockAddress) {
    jbyteArray byteArray = socketAddressToByteArray(env, sockAddress);
    return byteArrayToInetAddress(env, byteArray);
}

// Handles translating between IPv4 and IPv6 addresses so -- where possible --
// we can use either class of address with either an IPv4 or IPv6 socket.
class CompatibleSocketAddress {
public:
    // Constructs an address corresponding to 'ss' that's compatible with 'fd'.
    CompatibleSocketAddress(int fd, const sockaddr_storage& ss, bool mapUnspecified) {
        const int desiredFamily = getSocketAddressFamily(fd);
        if (ss.ss_family == AF_INET6) {
            if (desiredFamily == AF_INET6) {
                // Nothing to do.
                mCompatibleAddress = reinterpret_cast<const sockaddr*>(&ss);
            } else {
                sockaddr_in* sin = reinterpret_cast<sockaddr_in*>(&mTmp);
                const sockaddr_in6* sin6 = reinterpret_cast<const sockaddr_in6*>(&ss);
                memset(sin, 0, sizeof(*sin));
                sin->sin_family = AF_INET;
                sin->sin_port = sin6->sin6_port;
                if (IN6_IS_ADDR_V4COMPAT(&sin6->sin6_addr)) {
                    // We have an IPv6-mapped IPv4 address, but need plain old IPv4.
                    // Unmap the mapped address in ss into an IPv6 address in mTmp.
                    memcpy(&sin->sin_addr.s_addr, &sin6->sin6_addr.s6_addr[12], 4);
                    mCompatibleAddress = reinterpret_cast<const sockaddr*>(&mTmp);
                } else if (IN6_IS_ADDR_LOOPBACK(&sin6->sin6_addr)) {
                    // Translate the IPv6 loopback address to the IPv4 one.
                    sin->sin_addr.s_addr = htonl(INADDR_LOOPBACK);
                    mCompatibleAddress = reinterpret_cast<const sockaddr*>(&mTmp);
                } else {
                    // We can't help you. We return what you gave us, and assume you'll
                    // get a sensible error when you use the address.
                    mCompatibleAddress = reinterpret_cast<const sockaddr*>(&ss);
                }
            }
        } else /* ss.ss_family == AF_INET */ {
            if (desiredFamily == AF_INET) {
                // Nothing to do.
                mCompatibleAddress = reinterpret_cast<const sockaddr*>(&ss);
            } else {
                // We have IPv4 and need IPv6.
                // Map the IPv4 address in ss into an IPv6 address in mTmp.
                const sockaddr_in* sin = reinterpret_cast<const sockaddr_in*>(&ss);
                sockaddr_in6* sin6 = reinterpret_cast<sockaddr_in6*>(&mTmp);
                memset(sin6, 0, sizeof(*sin6));
                sin6->sin6_family = AF_INET6;
                sin6->sin6_port = sin->sin_port;
                // TODO: mapUnspecified was introduced because kernels < 2.6.31 don't allow
                // you to bind to ::ffff:0.0.0.0. When we move to something >= 2.6.31, we
                // should make the code behave as if mapUnspecified were always true, and
                // remove the parameter.
                if (sin->sin_addr.s_addr != 0 || mapUnspecified) {
                    memset(&(sin6->sin6_addr.s6_addr[10]), 0xff, 2);
                }
                memcpy(&sin6->sin6_addr.s6_addr[12], &sin->sin_addr.s_addr, 4);
                mCompatibleAddress = reinterpret_cast<const sockaddr*>(&mTmp);
            }
        }
    }
    // Returns a pointer to an address compatible with the socket.
    const sockaddr* get() const {
        return mCompatibleAddress;
    }
private:
    const sockaddr* mCompatibleAddress;
    sockaddr_storage mTmp;
};

/**
 * Converts an InetAddress object and port number to a native address structure.
 * Throws a NullPointerException or a SocketException in case of
 * error.
 */
static bool byteArrayToSocketAddress(JNIEnv *env,
        jbyteArray addressBytes, int port, sockaddr_storage *sockaddress) {
    if (addressBytes == NULL) {
        jniThrowNullPointerException(env, NULL);
        return false;
    }

    // Convert the IP address bytes to the proper IP address type.
    size_t addressLength = env->GetArrayLength(addressBytes);
    memset(sockaddress, 0, sizeof(*sockaddress));
    if (addressLength == 4) {
        // IPv4 address.
        sockaddr_in *sin = reinterpret_cast<sockaddr_in*>(sockaddress);
        sin->sin_family = AF_INET;
        sin->sin_port = htons(port);
        jbyte* dst = reinterpret_cast<jbyte*>(&sin->sin_addr.s_addr);
        env->GetByteArrayRegion(addressBytes, 0, 4, dst);
    } else if (addressLength == 16) {
        // IPv6 address.
        sockaddr_in6 *sin6 = reinterpret_cast<sockaddr_in6*>(sockaddress);
        sin6->sin6_family = AF_INET6;
        sin6->sin6_port = htons(port);
        jbyte* dst = reinterpret_cast<jbyte*>(&sin6->sin6_addr.s6_addr);
        env->GetByteArrayRegion(addressBytes, 0, 16, dst);
    } else {
        jniThrowBadAddressFamily(env);
        return false;
    }
    return true;
}

/**
 * Converts an InetAddress object and port number to a native address structure.
 */
static bool inetAddressToSocketAddress(JNIEnv *env, jobject inetaddress,
        int port, sockaddr_storage *sockaddress) {
    // Get the byte array that stores the IP address bytes in the InetAddress.
    if (inetaddress == NULL) {
        jniThrowNullPointerException(env, NULL);
        return false;
    }
    jbyteArray addressBytes =
        reinterpret_cast<jbyteArray>(env->GetObjectField(inetaddress,
            gCachedFields.iaddr_ipaddress));

    return byteArrayToSocketAddress(env, addressBytes, port, sockaddress);
}

/**
 * Convert a Java byte array representing an IP address to a Java string.
 *
 * @param addressByteArray the byte array to convert.
 *
 * @return a string with the textual representation of the address.
 */
static jstring osNetworkSystem_byteArrayToIpString(JNIEnv* env, jobject,
        jbyteArray byteArray) {
    if (byteArray == NULL) {
        jniThrowNullPointerException(env, NULL);
        return NULL;
    }
    sockaddr_storage ss;
    if (!byteArrayToSocketAddress(env, byteArray, 0, &ss)) {
        return NULL;
    }
    // TODO: getnameinfo seems to want its length parameter to be exactly
    // sizeof(sockaddr_in) for an IPv4 address and sizeof (sockaddr_in6) for an
    // IPv6 address. Fix getnameinfo so it accepts sizeof(sockaddr_storage), and
    // then remove this hack.
    int sa_size;
    if (ss.ss_family == AF_INET) {
        sa_size = sizeof(sockaddr_in);
    } else if (ss.ss_family == AF_INET6) {
        sa_size = sizeof(sockaddr_in6);
    } else {
        jniThrowBadAddressFamily(env);
        return NULL;
    }
    char ipString[INET6_ADDRSTRLEN];
    int rc = getnameinfo(reinterpret_cast<sockaddr*>(&ss), sa_size,
            ipString, sizeof(ipString), NULL, 0, NI_NUMERICHOST);
    if (rc != 0) {
        jniThrowException(env, "java/net/UnknownHostException", gai_strerror(rc));
        return NULL;
    }
    return env->NewStringUTF(ipString);
}

/**
 * Convert a Java string representing an IP address to a Java byte array.
 * The formats accepted are:
 * - IPv4:
 *   - 1.2.3.4
 *   - 1.2.4
 *   - 1.4
 *   - 4
 * - IPv6
 *   - Compressed form (2001:db8::1)
 *   - Uncompressed form (2001:db8:0:0:0:0:0:1)
 *   - IPv4-compatible (::192.0.2.0)
 *   - With an embedded IPv4 address (2001:db8::192.0.2.0).
 * IPv6 addresses may appear in square brackets.
 *
 * @param addressByteArray the byte array to convert.
 *
 * @return a string with the textual representation of the address.
 *
 * @throws UnknownHostException the IP address was invalid.
 */
static jbyteArray osNetworkSystem_ipStringToByteArray(JNIEnv* env, jobject,
        jstring javaString) {
    if (javaString == NULL) {
        jniThrowNullPointerException(env, NULL);
        return NULL;
    }

    // Convert the String to UTF bytes.
    size_t byteCount = env->GetStringUTFLength(javaString);
    LocalArray<INET6_ADDRSTRLEN> bytes(byteCount + 1);
    char* ipString = &bytes[0];
    env->GetStringUTFRegion(javaString, 0, env->GetStringLength(javaString), ipString);

    // Accept IPv6 addresses (only) in square brackets for compatibility.
    if (ipString[0] == '[' && ipString[byteCount - 1] == ']' &&
            index(ipString, ':') != NULL) {
        memmove(ipString, ipString + 1, byteCount - 2);
        ipString[byteCount - 2] = '\0';
    }

    jbyteArray result = NULL;
    addrinfo hints;
    memset(&hints, 0, sizeof(hints));
    hints.ai_flags = AI_NUMERICHOST;

    sockaddr_storage ss;
    memset(&ss, 0, sizeof(ss));
    
    addrinfo* res = NULL;
    int ret = getaddrinfo(ipString, NULL, &hints, &res);
    if (ret == 0 && res) {
        // Convert IPv4-mapped addresses to IPv4 addresses.
        // The RI states "Java will never return an IPv4-mapped address".
        sockaddr_in6* sin6 = reinterpret_cast<sockaddr_in6*>(res->ai_addr);
        if (res->ai_family == AF_INET6 && IN6_IS_ADDR_V4MAPPED(&sin6->sin6_addr)) {
            sockaddr_in* sin = reinterpret_cast<sockaddr_in*>(&ss);
            sin->sin_family = AF_INET;
            sin->sin_port = sin6->sin6_port;
            memcpy(&sin->sin_addr.s_addr, &sin6->sin6_addr.s6_addr[12], 4);
            result = socketAddressToByteArray(env, &ss);
        } else {
            result = socketAddressToByteArray(env, reinterpret_cast<sockaddr_storage*>(res->ai_addr));
        }
    } else {
        // For backwards compatibility, deal with address formats that
        // getaddrinfo does not support. For example, 1.2.3, 1.3, and even 3 are
        // valid IPv4 addresses according to the Java API. If getaddrinfo fails,
        // try to use inet_aton.
        sockaddr_in* sin = reinterpret_cast<sockaddr_in*>(&ss);
        if (inet_aton(ipString, &sin->sin_addr)) {
            sin->sin_family = AF_INET;
            sin->sin_port = 0;
            result = socketAddressToByteArray(env, &ss);
        }
    }

    if (res) {
        freeaddrinfo(res);
    }

    if (! result) {
        env->ExceptionClear();
        jniThrowException(env, "java/net/UnknownHostException",
                gai_strerror(ret));
    }

    return result;
}

/**
 * Answer a new java.lang.Boolean object.
 *
 * @param env   pointer to the JNI library
 * @param anInt the Boolean constructor argument
 *
 * @return  the new Boolean
 */
static jobject newJavaLangBoolean(JNIEnv * env, jint anInt) {
    jclass tempClass;
    jmethodID tempMethod;

    tempClass = gCachedFields.boolean_class;
    tempMethod = gCachedFields.boolean_class_init;
    return env->NewObject(tempClass, tempMethod, (jboolean) (anInt != 0));
}

/**
 * Answer a new java.lang.Byte object.
 *
 * @param env   pointer to the JNI library
 * @param anInt the Byte constructor argument
 *
 * @return  the new Byte
 */
static jobject newJavaLangByte(JNIEnv * env, jbyte val) {
    jclass tempClass;
    jmethodID tempMethod;

    tempClass = gCachedFields.byte_class;
    tempMethod = gCachedFields.byte_class_init;
    return env->NewObject(tempClass, tempMethod, val);
}

/**
 * Answer a new java.lang.Integer object.
 *
 * @param env   pointer to the JNI library
 * @param anInt the Integer constructor argument
 *
 * @return  the new Integer
 */
static jobject newJavaLangInteger(JNIEnv* env, jint anInt) {
    return env->NewObject(gCachedFields.integer_class, gCachedFields.integer_class_init, anInt);
}

// Converts a number of milliseconds to a timeval.
static timeval toTimeval(long ms) {
    timeval tv;
    tv.tv_sec = ms / 1000;
    tv.tv_usec = (ms - tv.tv_sec*1000) * 1000;
    return tv;
}

// Converts a timeval to a number of milliseconds.
static long toMs(const timeval& tv) {
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

/**
 * Query OS for timestamp.
 * Retrieve the current value of system clock and convert to milliseconds.
 *
 * @param[in] portLibrary The port library.
 *
 * @return 0 on failure, time value in milliseconds on success.
 * @deprecated Use @ref time_hires_clock and @ref time_hires_delta
 *
 * technically, this should return I_64 since both timeval.tv_sec and
 * timeval.tv_usec are long
 */

static int time_msec_clock() {
    timeval tp;
    struct timezone tzp;
    gettimeofday(&tp, &tzp);
    return toMs(tp);
}

/**
 * Answer the errorString corresponding to the errorNumber, if available.
 * This function will answer a default error string, if the errorNumber is not
 * recognized.
 *
 * This function will have to be reworked to handle internationalization
 * properly, removing the explicit strings.
 *
 * @param anErrorNum    the error code to resolve to a human readable string
 *
 * @return  a human readable error string
 */

static const char * netLookupErrorString(int anErrorNum) {
    switch (anErrorNum) {
        case SOCKERR_NOTINITIALIZED:
            return "Socket library uninitialized";
        case SOCKERR_BADAF:
            return "Bad address family";
        case SOCKERR_BADPROTO:
            return "Bad protocol";
        case SOCKERR_BADTYPE:
            return "Bad type";
        case SOCKERR_SYSTEMBUSY:
            return "System busy handling requests";
        case SOCKERR_SYSTEMFULL:
            return "Too many sockets allocated";
        case SOCKERR_NOTCONNECTED:
            return "Socket is not connected";
        case SOCKERR_INTERRUPTED:
            return "The system call was cancelled";
        case SOCKERR_TIMEOUT:
            return "The operation timed out";
        case SOCKERR_CONNRESET:
            return "The connection was reset";
        case SOCKERR_WOULDBLOCK:
            return "The nonblocking operation would block";
        case SOCKERR_ADDRNOTAVAIL:
            return "The address is not available";
        case SOCKERR_ADDRINUSE:
            return "The address is already in use";
        case SOCKERR_NOTBOUND:
            return "The socket is not bound";
        case SOCKERR_INVALIDTIMEOUT:
            return "The specified timeout is invalid";
        case SOCKERR_FDSETFULL:
            return "Unable to create an FDSET";
        case SOCKERR_TIMEVALFULL:
            return "Unable to create a TIMEVAL";
        case SOCKERR_REMSOCKSHUTDOWN:
            return "The remote socket has shutdown gracefully";
        case SOCKERR_NOTLISTENING:
            return "Listen() was not invoked prior to accept()";
        case SOCKERR_NOTSTREAMSOCK:
            return "The socket does not support connection-oriented service";
        case SOCKERR_ALREADYBOUND:
            return "The socket is already bound to an address";
        case SOCKERR_NBWITHLINGER:
            return "The socket is marked non-blocking & SO_LINGER is non-zero";
        case SOCKERR_ISCONNECTED:
            return "The socket is already connected";
        case SOCKERR_NOBUFFERS:
            return "No buffer space is available";
        case SOCKERR_HOSTNOTFOUND:
            return "Authoritative Answer Host not found";
        case SOCKERR_NODATA:
            return "Valid name, no data record of requested type";
        case SOCKERR_BOUNDORCONN:
            return "The socket has not been bound or is already connected";
        case SOCKERR_OPNOTSUPP:
            return "The socket does not support the operation";
        case SOCKERR_OPTUNSUPP:
            return "The socket option is not supported";
        case SOCKERR_OPTARGSINVALID:
            return "The socket option arguments are invalid";
        case SOCKERR_SOCKLEVELINVALID:
            return "The socket level is invalid";
        case SOCKERR_TIMEOUTFAILURE:
            return "The timeout operation failed";
        case SOCKERR_SOCKADDRALLOCFAIL:
            return "Failed to allocate address structure";
        case SOCKERR_FDSET_SIZEBAD:
            return "The calculated maximum size of the file descriptor set is bad";
        case SOCKERR_UNKNOWNFLAG:
            return "The flag is unknown";
        case SOCKERR_MSGSIZE:
            return "The datagram was too big to fit the specified buffer, so truncated";
        case SOCKERR_NORECOVERY:
            return "The operation failed with no recovery possible";
        case SOCKERR_ARGSINVALID:
            return "The arguments are invalid";
        case SOCKERR_BADDESC:
            return "The socket argument is not a valid file descriptor";
        case SOCKERR_NOTSOCK:
            return "The socket argument is not a socket";
        case SOCKERR_HOSTENTALLOCFAIL:
            return "Unable to allocate the hostent structure";
        case SOCKERR_TIMEVALALLOCFAIL:
            return "Unable to allocate the timeval structure";
        case SOCKERR_LINGERALLOCFAIL:
            return "Unable to allocate the linger structure";
        case SOCKERR_IPMREQALLOCFAIL:
            return "Unable to allocate the ipmreq structure";
        case SOCKERR_FDSETALLOCFAIL:
            return "Unable to allocate the fdset structure";
        case SOCKERR_OPFAILED:
            return "Operation failed";
        case SOCKERR_CONNECTION_REFUSED:
            return "Connection refused";
        case SOCKERR_ENETUNREACH:
            return "Network unreachable";
        case SOCKERR_EHOSTUNREACH:
            return "No route to host";
        case SOCKERR_EPIPE:
            return "Broken pipe";
        case SOCKERR_EACCES:
            return "Permission denied (maybe missing INTERNET permission)";

        default:
            LOGE("unknown socket error %d", anErrorNum);
            return "unknown error";
    }
}

static int convertError(int errorCode) {
    switch (errorCode) {
        case EBADF:
            return SOCKERR_BADDESC;
        case ENOBUFS:
            return SOCKERR_NOBUFFERS;
        case EOPNOTSUPP:
            return SOCKERR_OPNOTSUPP;
        case ENOPROTOOPT:
            return SOCKERR_OPTUNSUPP;
        case EINVAL:
            return SOCKERR_SOCKLEVELINVALID;
        case ENOTSOCK:
            return SOCKERR_NOTSOCK;
        case EINTR:
            return SOCKERR_INTERRUPTED;
        case ENOTCONN:
            return SOCKERR_NOTCONNECTED;
        case EAFNOSUPPORT:
            return SOCKERR_BADAF;
            /* note: CONNRESET not included because it has the same
             * value as ECONNRESET and they both map to SOCKERR_CONNRESET */
        case ECONNRESET:
            return SOCKERR_CONNRESET;
        case EAGAIN:
            return SOCKERR_WOULDBLOCK;
        case EPROTONOSUPPORT:
            return SOCKERR_BADPROTO;
        case EFAULT:
            return SOCKERR_ARGSINVALID;
        case ETIMEDOUT:
            return SOCKERR_TIMEOUT;
        case ECONNREFUSED:
            return SOCKERR_CONNECTION_REFUSED;
        case ENETUNREACH:
            return SOCKERR_ENETUNREACH;
        case EACCES:
            return SOCKERR_EACCES;
        case EPIPE:
            return SOCKERR_EPIPE;
        case EHOSTUNREACH:
            return SOCKERR_EHOSTUNREACH;
        case EADDRINUSE:
            return SOCKERR_ADDRINUSE;
        case EADDRNOTAVAIL:
            return SOCKERR_ADDRNOTAVAIL;
        case EMSGSIZE:
            return SOCKERR_MSGSIZE;
        default:
            LOGE("unclassified errno %d (%s)", errorCode, strerror(errorCode));
            return SOCKERR_OPFAILED;
    }
}

static int selectWait(int fd, int uSecTime) {
    timeval tv;
    timeval* tvp;
    if (uSecTime >= 0) {
        /* Use a timeout if uSecTime >= 0 */
        memset(&tv, 0, sizeof(tv));
        tv.tv_usec = uSecTime;
        tvp = &tv;
    } else {
        /* Infinite timeout if uSecTime < 0 */
        tvp = NULL;
    }

    fd_set readFds;
    FD_ZERO(&readFds);
    FD_SET(fd, &readFds);
    int result = select(fd + 1, &readFds, NULL, NULL, tvp);
    if (result == -1) {
        if (errno == EINTR) {
            result = SOCKERR_INTERRUPTED;
        } else {
            result = SOCKERR_OPFAILED;
        }
    } else if (result == 0) {
        result = SOCKERR_TIMEOUT;
    }
    return result;
}

// Returns 0 on success, not obviously meaningful negative values on error.
static int pollSelectWait(JNIEnv *env, jobject fileDescriptor, int timeout) {
    /* now try reading the socket for the timeout.
     * if timeout is 0 try forever until the sockets gets ready or until an
     * exception occurs.
     */
    int pollTimeoutUSec = 100000, pollMsec = 100;
    int finishTime = 0;
    int timeLeft = timeout;
    int hasTimeout = timeout > 0 ? 1 : 0;
    int result = 0;
    int handle;

    if (hasTimeout) {
        finishTime = time_msec_clock() + timeout;
    }

    int poll = 1;

    while (poll) { /* begin polling loop */

        /*
         * Fetch the handle every time in case the socket is closed.
         */
        handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
        if (handle == -1) {
            jniThrowSocketException(env, EINTR);
            return -1;
        }

        if (hasTimeout) {

            if (timeLeft - 10 < pollMsec) {
                pollTimeoutUSec = timeLeft <= 0 ? 0 : (timeLeft * 1000);
            }

            result = selectWait(handle, pollTimeoutUSec);

            /*
             * because we are polling at a time smaller than timeout
             * (presumably) lets treat an interrupt and timeout the same - go
             * see if we're done timewise, and then just try again if not.
             */
            if (SOCKERR_TIMEOUT == result ||
                SOCKERR_INTERRUPTED == result) {

                timeLeft = finishTime - time_msec_clock();

                if (timeLeft <= 0) {
                    /*
                     * Always throw the "timeout" message because that is
                     * effectively what has happened, even if we happen to
                     * have been interrupted.
                     */
                    jniThrowSocketTimeoutException(env, ETIMEDOUT);
                } else {
                    continue; // try again
                }

            } else if (0 > result) {
                throwSocketException(env, result);
            }
            poll = 0;

        } else { /* polling with no timeout (why would you do this?)*/

            result = selectWait(handle, pollTimeoutUSec);

            /*
             *  if interrupted (or a timeout) just retry
             */
            if (SOCKERR_TIMEOUT == result ||
               SOCKERR_INTERRUPTED == result) {

                continue; // try again
            } else if (0 > result) {
                throwSocketException(env, result);
            }
            poll = 0;
        }
    } /* end polling loop */

    return result;
}

/**
 * Wrapper for connect() that converts IPv4 addresses to IPv4-mapped IPv6
 * addresses if necessary.
 *
 * @param socket the file descriptor of the socket to connect
 * @param socketAddress the address to connect to
 */
static int doConnect(int fd, const sockaddr_storage* socketAddress) {
    const CompatibleSocketAddress compatibleAddress(fd, *socketAddress, true);
    return TEMP_FAILURE_RETRY(connect(fd, compatibleAddress.get(), sizeof(sockaddr_storage)));
}

/**
 * Establish a connection to a peer with a timeout.  This function is called
 * repeatedly in order to carry out the connect and to allow other tasks to
 * proceed on certain platforms. The caller must first call with
 * step = SOCKET_STEP_START, if the result is SOCKERR_NOTCONNECTED it will then
 * call it with step = CHECK until either another error or 0 is returned to
 * indicate the connect is complete.  Each time the function should sleep for no
 * more than timeout milliseconds.  If the connect succeeds or an error occurs,
 * the caller must always end the process by calling the function with
 * step = SOCKET_STEP_DONE
 *
 * @param[in] portLibrary The port library.
 * @param[in] sock pointer to the unconnected local socket.
 * @param[in] addr pointer to the sockaddr, specifying remote host/port.
 * @param[in] timeout the timeout in milliseconds. If timeout is negative,
 *         perform a block operation.
 * @param[in,out] pointer to context pointer. Filled in on first call and then
 *         to be passed into each subsequent call.
 *
 * @return 0, if no errors occurred, otherwise the (negative) error code.
 */
// TODO: do we really want to pass 'addr' by value?
static int sockConnectWithTimeout(int handle, sockaddr_storage addr,
                                  int timeout, unsigned int step, jbyte *ctxt) {
    int rc = 0;
    int errorVal;
    socklen_t errorValLen = sizeof(int);
    selectFDSet* context = reinterpret_cast<selectFDSet*>(ctxt);

    if (SOCKET_STEP_START == step) {
        context->sock = handle;
        context->nfds = handle + 1;

        /* set the socket to non-blocking */
        int block = JNI_TRUE;
        rc = ioctl(handle, FIONBIO, &block);
        if (rc != 0) {
            return convertError(rc);
        }
        
        // LOGD("+connect to address 0x%08x (via normal) on handle %d",
        //         addr.sin_addr.s_addr, handle);
        rc = doConnect(handle, &addr);
        // LOGD("-connect to address 0x%08x (via normal) returned %d",
        //         addr.sin_addr.s_addr, (int) rc);

        if (rc == -1) {
            rc = errno;
            switch (rc) {
                case EINTR:
                    return SOCKERR_ALREADYBOUND;
                case EAGAIN:
                case EINPROGRESS:
                    return SOCKERR_NOTCONNECTED;
                default:
                    return convertError(rc);
            }
        }

        /* we connected right off the bat so just return */
        return rc;

    } else if (SOCKET_STEP_CHECK == step) {
        /* now check if we have connected yet */

        /*
         * set the timeout value to be used. Because on some unix platforms we
         * don't get notified when a socket is closed we only sleep for 100ms
         * at a time
         * 
         * TODO: is this relevant for Android?
         */
        if (timeout > 100) {
            timeout = 100;
        }
        timeval passedTimeout(toTimeval(timeout));

        /* initialize the FD sets for the select */
        FD_ZERO(&(context->exceptionSet));
        FD_ZERO(&(context->writeSet));
        FD_ZERO(&(context->readSet));
        FD_SET(context->sock, &(context->writeSet));
        FD_SET(context->sock, &(context->readSet));
        FD_SET(context->sock, &(context->exceptionSet));

        rc = select(context->nfds,
                   &(context->readSet),
                   &(context->writeSet),
                   &(context->exceptionSet),
                   timeout >= 0 ? &passedTimeout : NULL);

        /* if there is at least one descriptor ready to be checked */
        if (0 < rc) {
            /* if the descriptor is in the write set we connected or failed */
            if (FD_ISSET(context->sock, &(context->writeSet))) {

                if (!FD_ISSET(context->sock, &(context->readSet))) {
                    /* ok we have connected ok */
                    return 0;
                } else {
                    /* ok we have more work to do to figure it out */
                    if (getsockopt(context->sock, SOL_SOCKET, SO_ERROR,
                            &errorVal, &errorValLen) >= 0) {
                        return errorVal ? convertError(errorVal) : 0;
                    } else {
                        return convertError(errno);
                    }
                }
            }

            /* if the descriptor is in the exception set the connect failed */
            if (FD_ISSET(context->sock, &(context->exceptionSet))) {
                if (getsockopt(context->sock, SOL_SOCKET, SO_ERROR, &errorVal,
                        &errorValLen) >= 0) {
                    return errorVal ? convertError(errorVal) : 0;
                }
                rc = errno;
                return convertError(rc);
            }

        } else if (rc < 0) {
            /* something went wrong with the select call */
            rc = errno;

            /* if it was EINTR we can just try again. Return not connected */
            if (EINTR == rc) {
                return SOCKERR_NOTCONNECTED;
            }

            /* some other error occured so look it up and return */
            return convertError(rc);
        }

        /*
         * if we get here the timeout expired or the connect had not yet
         * completed just indicate that the connect is not yet complete
         */
        return SOCKERR_NOTCONNECTED;
    } else if (SOCKET_STEP_DONE == step) {
        /* we are done the connect or an error occured so clean up  */
        if (handle != -1) {
            int block = JNI_FALSE;
            ioctl(handle, FIONBIO, &block);
        }
        return 0;
    }
    return SOCKERR_ARGSINVALID;
}


#if LOG_SOCKOPT
/**
 * Helper method to log getsockopt/getsockopt calls.
 */
static const char *sockoptLevelToString(int level) {
    switch(level) {
        case SOL_SOCKET:
            return "SOL_SOCKET";
        case IPPROTO_IP:
            return "IPPROTO_IP";
        case IPPROTO_IPV6:
            return "IPPROTO_IPV6";
        default:
            return "SOL_???";
    }
}
#endif

/**
 * Helper method to get or set socket options
 *
 * @param action SOCKOPT_GET to get an option, SOCKOPT_SET to set it
 * @param socket the file descriptor of the socket to use
 * @param ipv4Option the option value to use for an IPv4 socket
 * @param ipv6Option the option value to use for an IPv6 socket
 * @param optionValue the value of the socket option to get or set
 * @param optionLength the length of the socket option to get or set
 *
 * @return the value of the socket call, or -1 on failure inside this function
 *
 * @note on internal failure, the errno variable will be set appropriately
 */
static int getOrSetSocketOption(int action, int socket, int ipv4Option,
        int ipv6Option, void *optionValue, socklen_t *optionLength) {
    int option;
    int protocol;
    int family = getSocketAddressFamily(socket);
    switch (family) {
        case AF_INET:
            option = ipv4Option;
            protocol = IPPROTO_IP;
            break;
        case AF_INET6:
            option = ipv6Option;
            protocol = IPPROTO_IPV6;
            break;
        default:
            // TODO(enh): throw Java exceptions from this method instead of just
            // returning error codes.
            errno = EAFNOSUPPORT;
            return -1;
    }

    int ret;
    if (action == SOCKOPT_GET) {
        ret = getsockopt(socket, protocol, option, optionValue, optionLength);
#if LOG_SOCKOPT
        LOGI("getsockopt(%d, %s, %d, %p, [%d]) = %d %s",
                socket, sockoptLevelToString(protocol), option, optionValue,
                *optionLength, ret, (ret == -1) ? strerror(errno) : "");
#endif
    } else if (action == SOCKOPT_SET) {
        ret = setsockopt(socket, protocol, option, optionValue, *optionLength);
#if LOG_SOCKOPT
        LOGI("setsockopt(%d, %s, %d, [%d], %d) = %d %s",
                socket, sockoptLevelToString(protocol), option,
                // Note: this only works for integer options.
                // TODO: Use dvmPrintHexDump() to log non-integer options.
                *(int *)optionValue, *optionLength, ret,
                (ret == -1) ? strerror(errno) : "");
#endif
    } else {
        errno = EINVAL;
        ret = -1;
    }
    return ret;
}

#ifdef ENABLE_MULTICAST
/*
 * Find the interface index that was set for this socket by the IP_MULTICAST_IF
 * or IPV6_MULTICAST_IF socket option.
 *
 * @param socket the socket to examine
 *
 * @return the interface index, or -1 on failure
 *
 * @note on internal failure, the errno variable will be set appropriately
 */
static int interfaceIndexFromMulticastSocket(int socket) {
    int family = getSocketAddressFamily(socket);
    int interfaceIndex;
    int result;
    if (family == AF_INET) {
        // IP_MULTICAST_IF returns a pointer to a struct ip_mreqn.
        struct ip_mreqn tempRequest;
        socklen_t requestLength = sizeof(tempRequest);
        result = getsockopt(socket, IPPROTO_IP, IP_MULTICAST_IF, &tempRequest,
            &requestLength);
        interfaceIndex = tempRequest.imr_ifindex;
    } else if (family == AF_INET6) {
        // IPV6_MULTICAST_IF returns a pointer to an integer.
        socklen_t requestLength = sizeof(interfaceIndex);
        result = getsockopt(socket, IPPROTO_IPV6, IPV6_MULTICAST_IF,
                &interfaceIndex, &requestLength);
    } else {
        errno = EAFNOSUPPORT;
        return -1;
    }

    if (result == 0)
        return interfaceIndex;
    else
        return -1;
}

/**
 * Join/Leave the nominated multicast group on the specified socket.
 * Implemented by setting the multicast 'add membership'/'drop membership'
 * option at the HY_IPPROTO_IP level on the socket.
 *
 * Implementation note for multicast sockets in general:
 *
 * - This code is untested, because at the time of this writing multicast can't
 * be properly tested on Android due to GSM routing restrictions. So it might
 * or might not work.
 *
 * - The REUSEPORT socket option that Harmony employs is not supported on Linux
 * and thus also not supported on Android. It's is not needed for multicast
 * to work anyway (REUSEADDR should suffice).
 *
 * @param env pointer to the JNI library.
 * @param socketP pointer to the hysocket to join/leave on.
 * @param optVal pointer to the InetAddress, the multicast group to join/drop.
 *
 * @exception SocketException if an error occurs during the call
 */
static void mcastAddDropMembership(JNIEnv *env, int handle, jobject optVal,
        int ignoreIF, int setSockOptVal) {
    struct sockaddr_storage sockaddrP;
    int result;
    // By default, let the system decide which interface to use.
    int interfaceIndex = 0;

    /*
     * Check whether we are getting an InetAddress or an Generic IPMreq. For now
     * we support both so that we will not break the tests. If an InetAddress
     * is passed in, only support IPv4 as obtaining an interface from an
     * InetAddress is complex and should be done by the Java caller.
     */
    if (env->IsInstanceOf (optVal, gCachedFields.iaddr_class)) {
        /*
         * optVal is an InetAddress. Construct a multicast request structure
         * from this address. Support IPv4 only.
         */
        struct ip_mreqn multicastRequest;
        socklen_t length = sizeof(multicastRequest);
        memset(&multicastRequest, 0, length);

        // If ignoreIF is false, determine the index of the interface to use.
        if (!ignoreIF) {
            interfaceIndex = interfaceIndexFromMulticastSocket(handle);
            multicastRequest.imr_ifindex = interfaceIndex;
            if (interfaceIndex == -1) {
                jniThrowSocketException(env, errno);
                return;
            }
        }

        // Convert the inetAddress to an IPv4 address structure.
        if (!inetAddressToSocketAddress(env, optVal, 0, &sockaddrP)) {
            return;
        }
        if (sockaddrP.ss_family != AF_INET) {
            jniThrowSocketException(env, EAFNOSUPPORT);
            return;
        }
        struct sockaddr_in *sin = (struct sockaddr_in *) &sockaddrP;
        multicastRequest.imr_multiaddr = sin->sin_addr;

        result = setsockopt(handle, IPPROTO_IP, setSockOptVal,
                            &multicastRequest, length);
        if (0 != result) {
            jniThrowSocketException(env, errno);
            return;
        }
    } else {
        /*
         * optVal is a GenericIPMreq object. Extract the relevant fields from
         * it and construct a multicast request structure from these. Support
         * both IPv4 and IPv6.
         */
        jclass cls;
        jfieldID multiaddrID;
        jfieldID interfaceIdxID;
        jobject multiaddr;

        // Get the multicast address to join or leave.
        cls = env->GetObjectClass(optVal);
        multiaddrID = env->GetFieldID(cls, "multiaddr", "Ljava/net/InetAddress;");
        multiaddr = env->GetObjectField(optVal, multiaddrID);

        // Get the interface index to use.
        if (! ignoreIF) {
            interfaceIdxID = env->GetFieldID(cls, "interfaceIdx", "I");
            interfaceIndex = env->GetIntField(optVal, interfaceIdxID);
        }
        LOGI("mcastAddDropMembership interfaceIndex=%i", interfaceIndex);

        if (!inetAddressToSocketAddress(env, multiaddr, 0, &sockaddrP)) {
            return;
        }

        int family = getSocketAddressFamily(handle);

        // Handle IPv4 multicast on an IPv6 socket.
        if (family == AF_INET6 && sockaddrP.ss_family == AF_INET) {
            family = AF_INET;
        }

        struct ip_mreqn ipv4Request;
        struct ipv6_mreq ipv6Request;
        void *multicastRequest;
        socklen_t requestLength;
        int level;
        switch (family) {
            case AF_INET:
                requestLength = sizeof(ipv4Request);
                memset(&ipv4Request, 0, requestLength);
                ipv4Request.imr_multiaddr =
                        ((struct sockaddr_in *) &sockaddrP)->sin_addr;
                ipv4Request.imr_ifindex = interfaceIndex;
                multicastRequest = &ipv4Request;
                level = IPPROTO_IP;
                break;
            case AF_INET6:
                // setSockOptVal is passed in by the caller and may be IPv4-only
                if (setSockOptVal == IP_ADD_MEMBERSHIP) {
                    setSockOptVal = IPV6_ADD_MEMBERSHIP;
                }
                if (setSockOptVal == IP_DROP_MEMBERSHIP) {
                    setSockOptVal == IPV6_DROP_MEMBERSHIP;
                }
                requestLength = sizeof(ipv6Request);
                memset(&ipv6Request, 0, requestLength);
                ipv6Request.ipv6mr_multiaddr =
                        ((struct sockaddr_in6 *) &sockaddrP)->sin6_addr;
                ipv6Request.ipv6mr_interface = interfaceIndex;
                multicastRequest = &ipv6Request;
                level = IPPROTO_IPV6;
                break;
           default:
                jniThrowSocketException(env, EAFNOSUPPORT);
                return;
        }

        /* join/drop the multicast address */
        result = setsockopt(handle, level, setSockOptVal, multicastRequest,
                            requestLength);
        if (0 != result) {
            jniThrowSocketException(env, errno);
            return;
        }
    }
}
#endif // def ENABLE_MULTICAST

static bool initCachedFields(JNIEnv* env) {
    memset(&gCachedFields, 0, sizeof(gCachedFields));
    struct CachedFields *c = &gCachedFields;

    struct classInfo {
        jclass *clazz;
        const char *name;
    } classes[] = {
        {&c->iaddr_class, "java/net/InetAddress"},
        {&c->i4addr_class, "java/net/Inet4Address"},
        {&c->genericipmreq_class, "org/apache/harmony/luni/net/GenericIPMreq"},
        {&c->integer_class, "java/lang/Integer"},
        {&c->boolean_class, "java/lang/Boolean"},
        {&c->byte_class, "java/lang/Byte"},
        {&c->socketimpl_class, "java/net/SocketImpl"},
        {&c->dpack_class, "java/net/DatagramPacket"}
    };
    for (unsigned i = 0; i < sizeof(classes) / sizeof(classes[0]); i++) {
        classInfo c = classes[i];
        jclass tempClass = env->FindClass(c.name);
        if (tempClass == NULL) return false;
        *c.clazz = (jclass) env->NewGlobalRef(tempClass);
    }

    struct methodInfo {
        jmethodID *method;
        jclass clazz;
        const char *name;
        const char *signature;
        bool isStatic;
    } methods[] = {
        {&c->i4addr_class_init, c->i4addr_class, "<init>", "([B)V", false},
        {&c->integer_class_init, c->integer_class, "<init>", "(I)V", false},
        {&c->boolean_class_init, c->boolean_class, "<init>", "(Z)V", false},
        {&c->byte_class_init, c->byte_class, "<init>", "(B)V", false},
        {&c->iaddr_getbyaddress, c->iaddr_class, "getByAddress",
                    "([B)Ljava/net/InetAddress;", true}
    };
    for (unsigned i = 0; i < sizeof(methods) / sizeof(methods[0]); i++) {
        methodInfo m = methods[i];
        if (m.isStatic) {
            *m.method = env->GetStaticMethodID(m.clazz, m.name, m.signature);
        } else {
            *m.method = env->GetMethodID(m.clazz, m.name, m.signature);
        }
        if (*m.method == NULL) return false;
    }

    struct fieldInfo {
        jfieldID *field;
        jclass clazz;
        const char *name;
        const char *type;
    } fields[] = {
        {&c->iaddr_ipaddress, c->iaddr_class, "ipaddress", "[B"},
        {&c->integer_class_value, c->integer_class, "value", "I"},
        {&c->boolean_class_value, c->boolean_class, "value", "Z"},
        {&c->byte_class_value, c->byte_class, "value", "B"},
        {&c->socketimpl_port, c->socketimpl_class, "port", "I"},
        {&c->socketimpl_address, c->socketimpl_class, "address",
                "Ljava/net/InetAddress;"},
        {&c->dpack_address, c->dpack_class, "address",
                "Ljava/net/InetAddress;"},
        {&c->dpack_port, c->dpack_class, "port", "I"},
        {&c->dpack_length, c->dpack_class, "length", "I"}
    };
    for (unsigned i = 0; i < sizeof(fields) / sizeof(fields[0]); i++) {
        fieldInfo f = fields[i];
        *f.field = env->GetFieldID(f.clazz, f.name, f.type);
        if (*f.field == NULL) return false;
    }
    return true;
}

/**
 * Helper function to create a socket of the specified type and bind it to a
 * Java file descriptor.
 *
 * @param fileDescriptor the file descriptor to bind the socket to
 * @param type the socket type to create, e.g., SOCK_STREAM
 * @throws SocketException an error occurred when creating the socket
 *
 * @return the socket file descriptor. On failure, an exception is thrown and
 *         a negative value is returned.
 *
 */
static int createSocketFileDescriptor(JNIEnv* env, jobject fileDescriptor,
                                      int type) {
    if (fileDescriptor == NULL) {
        jniThrowNullPointerException(env, NULL);
        errno = EBADF;
        return -1;
    }

    // Try IPv6 but fall back to IPv4...
    int sock = socket(PF_INET6, type, 0);
    if (sock == -1 && errno == EAFNOSUPPORT) {
        sock = socket(PF_INET, type, 0);
    }
    if (sock == -1) {
        jniThrowSocketException(env, errno);
        return sock;
    }
    jniSetFileDescriptorOfFD(env, fileDescriptor, sock);
    return sock;
}

static void osNetworkSystem_createStreamSocket(JNIEnv* env, jobject, jobject fileDescriptor, jboolean) {
    createSocketFileDescriptor(env, fileDescriptor, SOCK_STREAM);
}

static void osNetworkSystem_createDatagramSocket(JNIEnv* env, jobject, jobject fd, jboolean) {
    createSocketFileDescriptor(env, fd, SOCK_DGRAM);
}

static jint osNetworkSystem_readDirect(JNIEnv* env, jobject,
        jobject fileDescriptor, jint address, jint count, jint timeout) {
    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return 0;
    }

    if (timeout != 0) {
        int result = selectWait(fd, timeout * 1000);
        if (result < 0) {
            return 0;
        }
    }

    jbyte* dst = reinterpret_cast<jbyte*>(static_cast<uintptr_t>(address));
    ssize_t bytesReceived =
            TEMP_FAILURE_RETRY(recv(fd, dst, count, SOCKET_NOFLAGS));
    if (bytesReceived == 0) {
        return -1;
    } else if (bytesReceived == -1) {
        if (errno == EAGAIN || errno == EWOULDBLOCK) {
            // We were asked to read a non-blocking socket with no data
            // available, so report "no bytes read".
            return 0;
        } else {
            jniThrowSocketException(env, errno);
            return 0;
        }
    }
    return bytesReceived;
}

static jint osNetworkSystem_readSocketImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jbyteArray byteArray, jint offset, jint count,
        jint timeout) {
    // LOGD("ENTER readSocketImpl");

    jbyte* bytes = env->GetByteArrayElements(byteArray, NULL);
    if (bytes == NULL) {
        return -1;
    }
    jint address =
            static_cast<jint>(reinterpret_cast<uintptr_t>(bytes + offset));
    int result = osNetworkSystem_readDirect(env, NULL,
            fileDescriptor, address, count, timeout);
    env->ReleaseByteArrayElements(byteArray, bytes, 0);
    return result;
}

static jint osNetworkSystem_writeDirect(JNIEnv* env, jobject,
        jobject fileDescriptor, jint address, jint offset, jint count) {
    if (count <= 0) {
        return 0;
    }

    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return 0;
    }

    jbyte* message = reinterpret_cast<jbyte*>(static_cast<uintptr_t>(address + offset));
    int bytesSent = send(fd, message, count, SOCKET_NOFLAGS);
    if (bytesSent == -1) {
        if (errno == EAGAIN || errno == EWOULDBLOCK) {
            // We were asked to write to a non-blocking socket, but were told
            // it would block, so report "no bytes written".
            return 0;
        } else {
            jniThrowSocketException(env, errno);
            return 0;
        }
    }
    return bytesSent;
}

static jint osNetworkSystem_write(JNIEnv* env, jobject,
        jobject fileDescriptor, jbyteArray byteArray, jint offset, jint count) {
    jbyte* bytes = env->GetByteArrayElements(byteArray, NULL);
    if (bytes == NULL) {
        return -1;
    }
    jint address = static_cast<jint>(reinterpret_cast<uintptr_t>(bytes));
    int result = osNetworkSystem_writeDirect(env, NULL,
            fileDescriptor, address, offset, count);
    env->ReleaseByteArrayElements(byteArray, bytes, 0);
    return result;
}

static void osNetworkSystem_setNonBlocking(JNIEnv* env, jobject,
        jobject fileDescriptor, jboolean nonblocking) {
    int handle;
    if (!jniGetFd(env, fileDescriptor, handle)) {
        return;
    }

    int block = nonblocking;
    int rc = ioctl(handle, FIONBIO, &block);
    if (rc == -1) {
        jniThrowSocketException(env, errno);
    }
}

static jint osNetworkSystem_connectWithTimeout(JNIEnv* env,
        jobject, jobject fileDescriptor, jint timeout, jint trafficClass,
        jobject inetAddr, jint port, jint step, jbyteArray passContext) {
    sockaddr_storage address;
    if (!inetAddressToSocketAddress(env, inetAddr, port, &address)) {
        return -1;
    }

    int handle;
    if (!jniGetFd(env, fileDescriptor, handle)) {
        return -1;
    }

    jbyte* context = env->GetByteArrayElements(passContext, NULL);
    int result = 0;
    switch (step) {
    case SOCKET_CONNECT_STEP_START:
        result = sockConnectWithTimeout(handle, address, 0,
                SOCKET_STEP_START, context);
        break;
    case SOCKET_CONNECT_STEP_CHECK:
        result = sockConnectWithTimeout(handle, address, timeout,
                SOCKET_STEP_CHECK, context);
        break;
    default:
        assert(false);
    }
    env->ReleaseByteArrayElements(passContext, context, 0);

    if (result == 0) {
        /* connected , so stop here */
        sockConnectWithTimeout(handle, address, 0, SOCKET_STEP_DONE, NULL);
    } else if (result != SOCKERR_NOTCONNECTED) {
        /* can not connect... */
        sockConnectWithTimeout(handle, address, 0, SOCKET_STEP_DONE, NULL);
        if (result == SOCKERR_EACCES) {
            jniThrowException(env, "java/lang/SecurityException",
                              netLookupErrorString(result));
        } else {
            jniThrowException(env, "java/net/ConnectException",
                              netLookupErrorString(result));
        }
    }

    return result;
}

static void osNetworkSystem_connectStreamWithTimeoutSocket(JNIEnv* env,
        jobject, jobject fileDescriptor, jint remotePort, jint timeout,
        jint trafficClass, jobject inetAddr) {
    int result = 0;
    struct sockaddr_storage address;
    jbyte *context = NULL;
    int remainingTimeout = timeout;
    int passedTimeout = 0;
    int finishTime = 0;
    int blocking = 0;
    char hasTimeout = timeout > 0;

    /* if a timeout was specified calculate the finish time value */
    if (hasTimeout)  {
        finishTime = time_msec_clock() + (int) timeout;
    }

    int handle;
    if (!jniGetFd(env, fileDescriptor, handle)) {
        return;
    }

    if (!inetAddressToSocketAddress(env, inetAddr, remotePort, &address)) {
        return;
    }

    /*
     * we will be looping checking for when we are connected so allocate
     * the descriptor sets that we will use
     */
    context =(jbyte *) malloc(sizeof(struct selectFDSet));
    if (context == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError", "native heap");
        return;
    }

    result = sockConnectWithTimeout(handle, address, 0, SOCKET_STEP_START, context);
    if (0 == result) {
        /* ok we connected right away so we are done */
        sockConnectWithTimeout(handle, address, 0, SOCKET_STEP_DONE, context);
        goto bail;
    } else if (result != SOCKERR_NOTCONNECTED) {
        sockConnectWithTimeout(handle, address, 0, SOCKET_STEP_DONE,
                               context);
        /* we got an error other than NOTCONNECTED so we cannot continue */
        if (SOCKERR_EACCES == result) {
            jniThrowException(env, "java/lang/SecurityException",
                              netLookupErrorString(result));
        } else {
            throwSocketException(env, result);
        }
        goto bail;
    }

    while (SOCKERR_NOTCONNECTED == result) {
        passedTimeout = remainingTimeout;

        /*
         * ok now try and connect. Depending on the platform this may sleep
         * for up to passedTimeout milliseconds
         */
        result = sockConnectWithTimeout(handle, address, passedTimeout,
                SOCKET_STEP_CHECK, context);

        /*
         * now check if the socket is still connected.
         * Do it here as some platforms seem to think they
         * are connected if the socket is closed on them.
         */
        handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
        if (handle == -1) {
            sockConnectWithTimeout(handle, address, 0,
                    SOCKET_STEP_DONE, context);
            jniThrowSocketException(env, EBADF);
            goto bail;
        }

        /*
         * check if we are now connected,
         * if so we can finish the process and return
         */
        if (0 == result) {
            sockConnectWithTimeout(handle, address, 0,
                    SOCKET_STEP_DONE, context);
            goto bail;
        }

        /*
         * if the error is SOCKERR_NOTCONNECTED then we have not yet
         * connected and we may not be done yet
         */
        if (SOCKERR_NOTCONNECTED == result) {
            /* check if the timeout has expired */
            if (hasTimeout) {
                remainingTimeout = finishTime - time_msec_clock();
                if (remainingTimeout <= 0) {
                    sockConnectWithTimeout(handle, address, 0,
                            SOCKET_STEP_DONE, context);
                    jniThrowSocketTimeoutException(env, ENOTCONN);
                    goto bail;
                }
            } else {
                remainingTimeout = 100;
            }
        } else {
            sockConnectWithTimeout(handle, address, remainingTimeout,
                                   SOCKET_STEP_DONE, context);
            if ((SOCKERR_CONNRESET == result) ||
                (SOCKERR_CONNECTION_REFUSED == result) ||
                (SOCKERR_ADDRNOTAVAIL == result) ||
                (SOCKERR_ADDRINUSE == result) ||
                (SOCKERR_ENETUNREACH == result)) {
                jniThrowException(env, "java/net/ConnectException",
                                  netLookupErrorString(result));
            } else if (SOCKERR_EACCES == result) {
                jniThrowException(env, "java/lang/SecurityException",
                                  netLookupErrorString(result));
            } else {
                throwSocketException(env, result);
            }
            goto bail;
        }
    }

bail:

    /* free the memory for the FD set */
    if (context != NULL)  {
        free(context);
    }
}

static void osNetworkSystem_bind(JNIEnv* env, jobject, jobject fileDescriptor,
        jobject inetAddress, jint port) {
    sockaddr_storage socketAddress;
    if (!inetAddressToSocketAddress(env, inetAddress, port, &socketAddress)) {
        return;
    }

    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return;
    }

    const CompatibleSocketAddress compatibleAddress(fd, socketAddress, false);
    int rc = TEMP_FAILURE_RETRY(bind(fd, compatibleAddress.get(), sizeof(sockaddr_storage)));
    if (rc == -1) {
        jniThrowBindException(env, errno);
    }
}

static void osNetworkSystem_listenStreamSocket(JNIEnv* env, jobject,
        jobject fileDescriptor, jint backlog) {
    int handle;
    if (!jniGetFd(env, fileDescriptor, handle)) {
        return;
    }

    int rc = listen(handle, backlog);
    if (rc == -1) {
        jniThrowSocketException(env, errno);
        return;
    }
}

static void osNetworkSystem_accept(JNIEnv* env, jobject,
        jobject serverFileDescriptor,
        jobject newSocket, jobject clientFileDescriptor, jint timeout) {
    // LOGD("ENTER acceptSocketImpl");

    if (newSocket == NULL) {
        jniThrowNullPointerException(env, NULL);
        return;
    }

    int rc = pollSelectWait(env, serverFileDescriptor, timeout);
    if (rc < 0) {
        return;
    }

    int serverFd;
    if (!jniGetFd(env, serverFileDescriptor, serverFd)) {
        return;
    }

    sockaddr_storage sa;
    socklen_t addrlen = sizeof(sa);
    int clientFd = TEMP_FAILURE_RETRY(accept(serverFd,
            reinterpret_cast<sockaddr*>(&sa), &addrlen));
    if (clientFd == -1) {
        jniThrowSocketException(env, errno);
        return;
    }

    /*
     * For network sockets, put the peer address and port in instance variables.
     * We don't bother to do this for UNIX domain sockets, since most peers are
     * anonymous anyway.
     */
    if (sa.ss_family == AF_INET || sa.ss_family == AF_INET6) {
        jobject inetAddress = socketAddressToInetAddress(env, &sa);
        if (inetAddress == NULL) {
            close(clientFd);
            return;
        }

        env->SetObjectField(newSocket,
                gCachedFields.socketimpl_address, inetAddress);

        int port = getSocketAddressPort(&sa);
        env->SetIntField(newSocket, gCachedFields.socketimpl_port, port);
    }

    jniSetFileDescriptorOfFD(env, clientFileDescriptor, clientFd);
}

static jboolean osNetworkSystem_supportsUrgentData(JNIEnv* env,
        jobject, jobject fileDescriptor) {
    // TODO(enh): do we really need to exclude the invalid file descriptor case?
    int fd = jniGetFDFromFileDescriptor(env, fileDescriptor);
    return (fd == -1) ? JNI_FALSE : JNI_TRUE;
}

static void osNetworkSystem_sendUrgentData(JNIEnv* env, jobject,
        jobject fileDescriptor, jbyte value) {
    int handle;
    if (!jniGetFd(env, fileDescriptor, handle)) {
        return;
    }

    int rc = send(handle, &value, 1, MSG_OOB);
    if (rc == -1) {
        jniThrowSocketException(env, errno);
    }
}

static void osNetworkSystem_connectDatagram(JNIEnv* env, jobject,
        jobject fileDescriptor, jint port, jint trafficClass, jobject inetAddress) {
    sockaddr_storage sockAddr;
    if (!inetAddressToSocketAddress(env, inetAddress, port, &sockAddr)) {
        return;
    }

    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return;
    }

    int ret = doConnect(fd, &sockAddr);
    if (ret < 0) {
        jniThrowSocketException(env, errno);
    }
}

static void osNetworkSystem_disconnectDatagram(JNIEnv* env, jobject,
        jobject fileDescriptor) {
    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return;
    }

    // To disconnect a datagram socket, we connect to a bogus address with
    // the family AF_UNSPEC.
    sockaddr_storage ss;
    memset(&ss, 0, sizeof(ss));
    ss.ss_family = AF_UNSPEC;
    const sockaddr* sa = reinterpret_cast<const sockaddr*>(&ss);
    int rc = TEMP_FAILURE_RETRY(connect(fd, sa, sizeof(ss)));
    if (rc == -1) {
        jniThrowSocketException(env, errno);
    }
}

static void osNetworkSystem_setInetAddress(JNIEnv* env, jobject,
        jobject sender, jbyteArray address) {
    env->SetObjectField(sender, gCachedFields.iaddr_ipaddress, address);
}

static jint osNetworkSystem_peekDatagram(JNIEnv* env, jobject,
        jobject fileDescriptor, jobject sender, jint receiveTimeout) {
    int result = pollSelectWait(env, fileDescriptor, receiveTimeout);
    if (result < 0) {
        return 0;
    }

    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return 0;
    }
    
    sockaddr_storage sockAddr;
    socklen_t sockAddrLen = sizeof(sockAddr);
    ssize_t length = TEMP_FAILURE_RETRY(recvfrom(fd, NULL, 0, MSG_PEEK,
            reinterpret_cast<sockaddr*>(&sockAddr), &sockAddrLen));
    if (length == -1) {
        jniThrowSocketException(env, errno);
        return 0;
    }

    // We update the byte[] in the 'sender' InetAddress, and return the port.
    // This awful API is public in the RI, so there's no point returning
    // InetSocketAddress here instead.
    jbyteArray senderAddressArray = socketAddressToByteArray(env, &sockAddr);
    if (sender == NULL) {
        return -1;
    }
    osNetworkSystem_setInetAddress(env, NULL, sender, senderAddressArray);
    return getSocketAddressPort(&sockAddr);
}

static jint osNetworkSystem_receiveDatagramDirect(JNIEnv* env, jobject,
        jobject fileDescriptor, jobject packet, jint address, jint offset,
        jint length, jint receiveTimeout, jboolean peek) {
    int result = pollSelectWait(env, fileDescriptor, receiveTimeout);
    if (result < 0) {
        return 0;
    }

    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return 0;
    }

    char* buf =
            reinterpret_cast<char*>(static_cast<uintptr_t>(address + offset));
    const int mode = peek ? MSG_PEEK : 0;
    sockaddr_storage sockAddr;
    socklen_t sockAddrLen = sizeof(sockAddr);
    ssize_t actualLength = TEMP_FAILURE_RETRY(recvfrom(fd, buf, length, mode,
            reinterpret_cast<sockaddr*>(&sockAddr), &sockAddrLen));
    if (actualLength == -1) {
        jniThrowSocketException(env, errno);
        return 0;
    }

    if (packet != NULL) {
        jbyteArray addr = socketAddressToByteArray(env, &sockAddr);
        if (addr == NULL) {
            return 0;
        }
        int port = getSocketAddressPort(&sockAddr);
        jobject sender = env->CallStaticObjectMethod(
                gCachedFields.iaddr_class, gCachedFields.iaddr_getbyaddress,
                addr);
        env->SetObjectField(packet, gCachedFields.dpack_address, sender);
        env->SetIntField(packet, gCachedFields.dpack_port, port);
        env->SetIntField(packet, gCachedFields.dpack_length,
                (jint) actualLength);
    }
    return (jint) actualLength;
}

static jint osNetworkSystem_receiveDatagram(JNIEnv* env, jobject,
        jobject fd, jobject packet, jbyteArray data, jint offset, jint length,
        jint receiveTimeout, jboolean peek) {
    int localLength = (length < 65536) ? length : 65536;
    jbyte *bytes = (jbyte*) malloc(localLength);
    if (bytes == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError",
                "couldn't allocate enough memory for receiveDatagram");
        return 0;
    }

    int actualLength = osNetworkSystem_receiveDatagramDirect(env, NULL, fd,
            packet, (jint)bytes, 0, localLength, receiveTimeout, peek);

    if (actualLength > 0) {
        env->SetByteArrayRegion(data, offset, actualLength, bytes);
    }
    free(bytes);

    return actualLength;
}

static jint osNetworkSystem_recvConnectedDatagramDirect(JNIEnv* env,
        jobject, jobject fileDescriptor, jobject packet,
        jint address, jint offset, jint length,
        jint receiveTimeout, jboolean peek) {

    int result = pollSelectWait(env, fileDescriptor, receiveTimeout);
    if (result < 0) {
        return 0;
    }

    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return 0;
    }
    
    char* buf = reinterpret_cast<char*>(static_cast<uintptr_t>(address + offset));
    int mode = peek ? MSG_PEEK : 0;
    int actualLength = recvfrom(fd, buf, length, mode, NULL, NULL);
    if (actualLength < 0) {
        jniThrowException(env, "java/net/PortUnreachableException", "");
        return 0;
    }

    if (packet != NULL) {
        env->SetIntField(packet, gCachedFields.dpack_length, actualLength);
    }
    return actualLength;
}

static jint osNetworkSystem_recvConnectedDatagram(JNIEnv* env, jobject,
        jobject fd, jobject packet, jbyteArray data, jint offset, jint length,
        jint receiveTimeout, jboolean peek) {
    int localLength = (length < 65536) ? length : 65536;
    jbyte *bytes = (jbyte*) malloc(localLength);
    if (bytes == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError",
                "couldn't allocate enough memory for recvConnectedDatagram");
        return 0;
    }

    int actualLength = osNetworkSystem_recvConnectedDatagramDirect(env,
            NULL, fd, packet, (jint)bytes, 0, localLength,
            receiveTimeout, peek);

    if (actualLength > 0) {
        env->SetByteArrayRegion(data, offset, actualLength, bytes);
    }
    free(bytes);

    return actualLength;
}

static jint osNetworkSystem_sendDatagramDirect(JNIEnv* env, jobject,
        jobject fileDescriptor, jint address, jint offset, jint length,
        jint port,
        jboolean bindToDevice, jint trafficClass, jobject inetAddress) {
    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return -1;
    }

    sockaddr_storage receiver;
    if (!inetAddressToSocketAddress(env, inetAddress, port, &receiver)) {
        return -1;
    }

    char* buf =
            reinterpret_cast<char*>(static_cast<uintptr_t>(address + offset));
    ssize_t bytesSent = TEMP_FAILURE_RETRY(sendto(fd, buf, length,
            SOCKET_NOFLAGS,
            reinterpret_cast<sockaddr*>(&receiver), sizeof(receiver)));
    if (bytesSent == -1) {
        if (errno == ECONNRESET || errno == ECONNREFUSED) {
            return 0;
        } else {
            jniThrowSocketException(env, errno);
        }
    }
    return bytesSent;
}

static jint osNetworkSystem_sendDatagram(JNIEnv* env, jobject,
        jobject fd, jbyteArray data, jint offset, jint length, jint port,
        jboolean bindToDevice, jint trafficClass, jobject inetAddress) {
    jbyte *bytes = env->GetByteArrayElements(data, NULL);
    int actualLength = osNetworkSystem_sendDatagramDirect(env, NULL, fd,
            (jint)bytes, offset, length, port, bindToDevice, trafficClass,
            inetAddress);
    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
    return actualLength;
}

static jint osNetworkSystem_sendConnectedDatagramDirect(JNIEnv* env,
        jobject, jobject fileDescriptor,
        jint address, jint offset, jint length,
        jboolean bindToDevice) {
    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return 0;
    }

    char* buf =
            reinterpret_cast<char*>(static_cast<uintptr_t>(address + offset));
    ssize_t bytesSent = TEMP_FAILURE_RETRY(send(fd, buf, length, 0));
    if (bytesSent == -1) {
        if (errno == ECONNRESET || errno == ECONNREFUSED) {
            return 0;
        } else {
            jniThrowSocketException(env, errno);
        }
    }
    return bytesSent;
}

static jint osNetworkSystem_sendConnectedDatagram(JNIEnv* env, jobject,
        jobject fd, jbyteArray data, jint offset, jint length,
        jboolean bindToDevice) {
    jbyte *bytes = env->GetByteArrayElements(data, NULL);
    int actualLength = osNetworkSystem_sendConnectedDatagramDirect(env,
            NULL, fd, (jint)bytes, offset, length, bindToDevice);
    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);

    return actualLength;
}

static void osNetworkSystem_createServerStreamSocket(JNIEnv* env, jobject,
        jobject fileDescriptor, jboolean) {
    int fd = createSocketFileDescriptor(env, fileDescriptor, SOCK_STREAM);
    if (fd != -1) {
        // TODO: we could actually do this in Java. (and check for errors!)
        int value = 1;
        setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, &value, sizeof(int));
    }
}

static void doShutdown(JNIEnv* env, jobject fileDescriptor, int how) {
    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return;
    }
    int rc = shutdown(fd, how);
    if (rc == -1) {
        jniThrowSocketException(env, errno);
    }
}

static void osNetworkSystem_shutdownInput(JNIEnv* env, jobject, jobject fd) {
    doShutdown(env, fd, SHUT_RD);
}

static void osNetworkSystem_shutdownOutput(JNIEnv* env, jobject, jobject fd) {
    doShutdown(env, fd, SHUT_WR);
}

static jint osNetworkSystem_sendDatagram2(JNIEnv* env, jobject,
        jobject fileDescriptor, jbyteArray data, jint offset, jint length,
        jint port, jobject inetAddress) {
    sockaddr_storage sockAddr;
    if (inetAddress != NULL) {
        if (!inetAddressToSocketAddress(env, inetAddress, port, &sockAddr)) {
            return -1;
        }
    }

    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return 0;
    }

    jbyte* message = (jbyte*) malloc(length * sizeof(jbyte));
    if (message == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError",
                "couldn't allocate enough memory for readSocket");
        return 0;
    }

    env->GetByteArrayRegion(data, offset, length, message);

    int totalBytesSent = 0;
    while (totalBytesSent < length) {
        ssize_t bytesSent = TEMP_FAILURE_RETRY(sendto(fd,
                message + totalBytesSent, length - totalBytesSent,
                SOCKET_NOFLAGS,
                reinterpret_cast<sockaddr*>(&sockAddr), sizeof(sockAddr)));
        if (bytesSent == -1) {
            jniThrowSocketException(env, errno);
            free(message);
            return 0;
        }

        totalBytesSent += bytesSent;
    }

    free(message);
    return totalBytesSent;
}

static bool initFdSet(JNIEnv* env, jobjectArray fdArray, jint count, fd_set* fdSet, int* maxFd) {
    for (int i = 0; i < count; ++i) {
        jobject fileDescriptor = env->GetObjectArrayElement(fdArray, i);
        if (fileDescriptor == NULL) {
            return false;
        }
        
        const int fd = jniGetFDFromFileDescriptor(env, fileDescriptor);
        if (fd < 0 || fd > 1024) {
            LOGE("selectImpl: ignoring invalid fd %i", fd);
            continue;
        }
        
        FD_SET(fd, fdSet);
        
        if (fd > *maxFd) {
            *maxFd = fd;
        }
    }
    return true;
}

/*
 * Note: fdSet has to be non-const because although on Linux FD_ISSET() is sane
 * and takes a const fd_set*, it takes fd_set* on Mac OS. POSIX is not on our
 * side here:
 *   http://www.opengroup.org/onlinepubs/000095399/functions/select.html
 */
static bool translateFdSet(JNIEnv* env, jobjectArray fdArray, jint count, fd_set& fdSet, jint* flagArray, size_t offset, jint op) {
    for (int i = 0; i < count; ++i) {
        jobject fileDescriptor = env->GetObjectArrayElement(fdArray, i);
        if (fileDescriptor == NULL) {
            return false;
        }
        
        const int fd = jniGetFDFromFileDescriptor(env, fileDescriptor);
        const bool valid = fd >= 0 && fd < 1024;

        if (valid && FD_ISSET(fd, &fdSet)) {
            flagArray[i + offset] = op;
        } else {
            flagArray[i + offset] = SOCKET_OP_NONE;
        }
    }
    return true;
}

static jboolean osNetworkSystem_selectImpl(JNIEnv* env, jclass,
        jobjectArray readFDArray, jobjectArray writeFDArray, jint countReadC,
        jint countWriteC, jintArray outFlags, jlong timeoutMs) {
    // LOGD("ENTER selectImpl");
    
    // Initialize the fd_sets.
    int maxFd = -1;
    fd_set readFds;
    fd_set writeFds;
    FD_ZERO(&readFds);
    FD_ZERO(&writeFds);
    bool initialized = initFdSet(env, readFDArray, countReadC, &readFds, &maxFd) &&
                       initFdSet(env, writeFDArray, countWriteC, &writeFds, &maxFd);
    if (!initialized) {
        return -1;
    }
    
    // Initialize the timeout, if any.
    timeval tv;
    timeval* tvp = NULL;
    if (timeoutMs >= 0) {
        tv = toTimeval(timeoutMs);
        tvp = &tv;
    }
    
    // Perform the select.
    int result = select(maxFd + 1, &readFds, &writeFds, NULL, tvp);
    if (result == 0) {
        // Timeout.
        return JNI_FALSE;
    } else if (result == -1) {
        // Error.
        if (errno == EINTR) {
            return JNI_FALSE;
        } else {
            jniThrowSocketException(env, errno);
            return JNI_FALSE;
        }
    }
    
    // Translate the result into the int[] we're supposed to fill in.
    jint* flagArray = env->GetIntArrayElements(outFlags, NULL);
    if (flagArray == NULL) {
        return JNI_FALSE;
    }
    bool okay = translateFdSet(env, readFDArray, countReadC, readFds, flagArray, 0, SOCKET_OP_READ) &&
                translateFdSet(env, writeFDArray, countWriteC, writeFds, flagArray, countReadC, SOCKET_OP_WRITE);
    env->ReleaseIntArrayElements(outFlags, flagArray, 0);
    return okay;
}

static jobject osNetworkSystem_getSocketLocalAddress(JNIEnv* env,
        jobject, jobject fileDescriptor) {
    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return NULL;
    }

    sockaddr_storage addr;
    socklen_t addrLen = sizeof(addr);
    memset(&addr, 0, addrLen);
    int rc = getsockname(fd, (sockaddr*) &addr, &addrLen);
    if (rc == -1) {
        // TODO: the public API doesn't allow failure, so this whole method
        // represents a broken design. In practice, though, getsockname can't
        // fail unless we give it invalid arguments.
        LOGE("getsockname failed: %s (errno=%i)", strerror(errno), errno);
        return NULL;
    }
    return socketAddressToInetAddress(env, &addr);
}

static jint osNetworkSystem_getSocketLocalPort(JNIEnv* env, jobject,
        jobject fileDescriptor) {
    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return 0;
    }

    sockaddr_storage addr;
    socklen_t addrLen = sizeof(addr);
    memset(&addr, 0, addrLen);
    int rc = getsockname(fd, (sockaddr*) &addr, &addrLen);
    if (rc == -1) {
        // TODO: the public API doesn't allow failure, so this whole method
        // represents a broken design. In practice, though, getsockname can't
        // fail unless we give it invalid arguments.
        LOGE("getsockname failed: %s (errno=%i)", strerror(errno), errno);
        return 0;
    }
    return getSocketAddressPort(&addr);
}

static jobject osNetworkSystem_getSocketOption(JNIEnv* env, jobject,
        jobject fileDescriptor, jint anOption) {
    int intValue = 0;
    socklen_t intSize = sizeof(int);
    int result;
    struct sockaddr_storage sockVal;
    socklen_t sockSize = sizeof(sockVal);

    int handle;
    if (!jniGetFd(env, fileDescriptor, handle)) {
        return 0;
    }

    switch ((int) anOption & 0xffff) {
        case JAVASOCKOPT_SO_LINGER: {
            struct linger lingr;
            socklen_t size = sizeof(struct linger);
            result = getsockopt(handle, SOL_SOCKET, SO_LINGER, &lingr, &size);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            if (!lingr.l_onoff) {
                intValue = -1;
            } else {
                intValue = lingr.l_linger;
            }
            return newJavaLangInteger(env, intValue);
        }

        case JAVASOCKOPT_TCP_NODELAY: {
            if ((anOption >> 16) & BROKEN_TCP_NODELAY) {
                return NULL;
            }
            result = getsockopt(handle, IPPROTO_TCP, TCP_NODELAY, &intValue, &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }

        case JAVASOCKOPT_SO_SNDBUF: {
            result = getsockopt(handle, SOL_SOCKET, SO_SNDBUF, &intValue, &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangInteger(env, intValue);
        }

        case JAVASOCKOPT_SO_RCVBUF: {
            result = getsockopt(handle, SOL_SOCKET, SO_RCVBUF, &intValue, &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangInteger(env, intValue);
        }

        case JAVASOCKOPT_SO_BROADCAST: {
            result = getsockopt(handle, SOL_SOCKET, SO_BROADCAST, &intValue, &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }

        case JAVASOCKOPT_SO_REUSEADDR: {
            result = getsockopt(handle, SOL_SOCKET, SO_REUSEADDR, &intValue, &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }

        case JAVASOCKOPT_SO_KEEPALIVE: {
            result = getsockopt(handle, SOL_SOCKET, SO_KEEPALIVE, &intValue, &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }

        case JAVASOCKOPT_SO_OOBINLINE: {
            result = getsockopt(handle, SOL_SOCKET, SO_OOBINLINE, &intValue, &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }

        case JAVASOCKOPT_IP_TOS: {
            result = getOrSetSocketOption(SOCKOPT_GET, handle, IP_TOS,
                                          IPV6_TCLASS, &intValue, &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangInteger(env, intValue);
        }

        case JAVASOCKOPT_SO_RCVTIMEOUT: {
            struct timeval timeout;
            socklen_t size = sizeof(timeout);
            result = getsockopt(handle, SOL_SOCKET, SO_RCVTIMEO, &timeout, &size);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangInteger(env, toMs(timeout));
        }

#ifdef ENABLE_MULTICAST
        case JAVASOCKOPT_MCAST_TTL: {
            if ((anOption >> 16) & BROKEN_MULTICAST_TTL) {
                return newJavaLangByte(env, 0);
            }
            // Java uses a byte to store the TTL, but the kernel uses an int.
            result = getOrSetSocketOption(SOCKOPT_GET, handle, IP_MULTICAST_TTL,
                                          IPV6_MULTICAST_HOPS, &intValue,
                                          &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangByte(env, (jbyte)(intValue & 0xFF));
        }

        case JAVASOCKOPT_IP_MULTICAST_IF: {
            if ((anOption >> 16) & BROKEN_MULTICAST_IF) {
                return NULL;
            }
            result = getsockopt(handle, IPPROTO_IP, IP_MULTICAST_IF,
                &sockVal, &sockSize);
            if (result == -1) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            if (sockVal.ss_family != AF_INET) {
                // Java expects an AF_INET INADDR_ANY, but Linux just returns AF_UNSPEC.
                jbyteArray inAddrAny = env->NewByteArray(4); // { 0, 0, 0, 0 }
                return byteArrayToInetAddress(env, inAddrAny);
            }
            return socketAddressToInetAddress(env, &sockVal);
        }

        case JAVASOCKOPT_IP_MULTICAST_IF2: {
            if ((anOption >> 16) & BROKEN_MULTICAST_IF) {
                return NULL;
            }
            struct ip_mreqn multicastRequest;
            int interfaceIndex = 0;
            socklen_t optionLength;
            int addressFamily = getSocketAddressFamily(handle);
            switch (addressFamily) {
                case AF_INET:
                    optionLength = sizeof(multicastRequest);
                    result = getsockopt(handle, IPPROTO_IP, IP_MULTICAST_IF,
                                        &multicastRequest, &optionLength);
                    if (result == 0)
                        interfaceIndex = multicastRequest.imr_ifindex;
                    break;
                case AF_INET6:
                    optionLength = sizeof(interfaceIndex);
                    result = getsockopt(handle, IPPROTO_IPV6, IPV6_MULTICAST_IF,
                                        &interfaceIndex, &optionLength);
                    break;
                default:
                    jniThrowSocketException(env, EAFNOSUPPORT);
                    return NULL;
            }

            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangInteger(env, interfaceIndex);
        }

        case JAVASOCKOPT_IP_MULTICAST_LOOP: {
            result = getOrSetSocketOption(SOCKOPT_GET, handle,
                                          IP_MULTICAST_LOOP,
                                          IPV6_MULTICAST_LOOP, &intValue,
                                          &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }
#else
        case JAVASOCKOPT_MCAST_TTL:
        case JAVASOCKOPT_IP_MULTICAST_IF:
        case JAVASOCKOPT_IP_MULTICAST_IF2:
        case JAVASOCKOPT_IP_MULTICAST_LOOP: {
            jniThrowException(env, "java/lang/UnsupportedOperationException", NULL);
            return NULL;
        }
#endif // def ENABLE_MULTICAST

        default: {
            jniThrowSocketException(env, ENOPROTOOPT);
            return NULL;
        }
    }

}

static void osNetworkSystem_setSocketOption(JNIEnv* env, jobject,
        jobject fileDescriptor, jint anOption, jobject optVal) {
    int result;
    int intVal;
    socklen_t intSize = sizeof(int);
    struct sockaddr_storage sockVal;
    int sockSize = sizeof(sockVal);

    if (env->IsInstanceOf(optVal, gCachedFields.integer_class)) {
        intVal = (int) env->GetIntField(optVal, gCachedFields.integer_class_value);
    } else if (env->IsInstanceOf(optVal, gCachedFields.boolean_class)) {
        intVal = (int) env->GetBooleanField(optVal, gCachedFields.boolean_class_value);
    } else if (env->IsInstanceOf(optVal, gCachedFields.byte_class)) {
        // TTL uses a byte in Java, but the kernel still wants an int.
        intVal = (int) env->GetByteField(optVal, gCachedFields.byte_class_value);
    } else if (env->IsInstanceOf(optVal, gCachedFields.iaddr_class)) {
        if (!inetAddressToSocketAddress(env, optVal, 0, &sockVal)) {
            return;
        }
    } else if (env->IsInstanceOf(optVal, gCachedFields.genericipmreq_class)) {
        // we'll use optVal directly
    } else {
        jniThrowSocketException(env, ENOPROTOOPT);
        return;
    }

    int handle;
    if (!jniGetFd(env, fileDescriptor, handle)) {
        return;
    }

    switch ((int) anOption & 0xffff) {
        case JAVASOCKOPT_SO_LINGER: {
            struct linger lingr;
            lingr.l_onoff = intVal > 0 ? 1 : 0;
            lingr.l_linger = intVal;
            result = setsockopt(handle, SOL_SOCKET, SO_LINGER, &lingr,
                    sizeof(struct linger));
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_TCP_NODELAY: {
            if ((anOption >> 16) & BROKEN_TCP_NODELAY) {
                return;
            }
            result = setsockopt(handle, IPPROTO_TCP, TCP_NODELAY, &intVal, intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_SNDBUF: {
            result = setsockopt(handle, SOL_SOCKET, SO_SNDBUF, &intVal, intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_RCVBUF: {
            result = setsockopt(handle, SOL_SOCKET, SO_RCVBUF, &intVal, intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_BROADCAST: {
            result = setsockopt(handle, SOL_SOCKET, SO_BROADCAST, &intVal, intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_REUSEADDR: {
            result = setsockopt(handle, SOL_SOCKET, SO_REUSEADDR, &intVal, intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }
        case JAVASOCKOPT_SO_KEEPALIVE: {
            result = setsockopt(handle, SOL_SOCKET, SO_KEEPALIVE, &intVal, intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_OOBINLINE: {
            result = setsockopt(handle, SOL_SOCKET, SO_OOBINLINE, &intVal, intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_IP_TOS: {
            result = getOrSetSocketOption(SOCKOPT_SET, handle, IP_TOS,
                                          IPV6_TCLASS, &intVal, &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_REUSEADDR_AND_REUSEPORT: {
            // SO_REUSEPORT doesn't need to get set on this System
            result = setsockopt(handle, SOL_SOCKET, SO_REUSEADDR, &intVal, intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_RCVTIMEOUT: {
            timeval timeout(toTimeval(intVal));
            result = setsockopt(handle, SOL_SOCKET, SO_RCVTIMEO, &timeout,
                    sizeof(struct timeval));
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

#ifdef ENABLE_MULTICAST
        case JAVASOCKOPT_MCAST_TTL: {
            if ((anOption >> 16) & BROKEN_MULTICAST_TTL) {
                return;
            }
            result = getOrSetSocketOption(SOCKOPT_SET, handle, IP_MULTICAST_TTL,
                                          IPV6_MULTICAST_HOPS, &intVal,
                                          &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_MCAST_ADD_MEMBERSHIP: {
            mcastAddDropMembership(env, handle, optVal,
                    (anOption >> 16) & BROKEN_MULTICAST_IF, IP_ADD_MEMBERSHIP);
            break;
        }

        case JAVASOCKOPT_MCAST_DROP_MEMBERSHIP: {
            mcastAddDropMembership(env, handle, optVal,
                    (anOption >> 16) & BROKEN_MULTICAST_IF, IP_DROP_MEMBERSHIP);
            break;
        }

        case JAVASOCKOPT_IP_MULTICAST_IF: {
            if ((anOption >> 16) & BROKEN_MULTICAST_IF) {
                return;
            }
            // This call is IPv4 only. The socket may be IPv6, but the address
            // that identifies the interface to join must be an IPv4 address.
            if (sockVal.ss_family != AF_INET) {
                jniThrowSocketException(env, EAFNOSUPPORT);
                return;
            }
            struct ip_mreqn mcast_req;
            memset(&mcast_req, 0, sizeof(mcast_req));
            struct sockaddr_in *sin = (struct sockaddr_in *) &sockVal;
            mcast_req.imr_address = sin->sin_addr;
            result = setsockopt(handle, IPPROTO_IP, IP_MULTICAST_IF,
                                &mcast_req, sizeof(mcast_req));
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_IP_MULTICAST_IF2: {
            if ((anOption >> 16) & BROKEN_MULTICAST_IF) {
                return;
            }
            int addressFamily = getSocketAddressFamily(handle);
            int interfaceIndex = intVal;
            void *optionValue;
            socklen_t optionLength;
            struct ip_mreqn multicastRequest;
            switch (addressFamily) {
                case AF_INET:
                    // IP_MULTICAST_IF expects a pointer to a struct ip_mreqn.
                    memset(&multicastRequest, 0, sizeof(multicastRequest));
                    multicastRequest.imr_ifindex = interfaceIndex;
                    optionValue = &multicastRequest;
                    optionLength = sizeof(multicastRequest);
                    break;
                case AF_INET6:
                    // IPV6_MULTICAST_IF expects a pointer to an integer.
                    optionValue = &interfaceIndex;
                    optionLength = sizeof(interfaceIndex);
                    break;
                default:
                    jniThrowSocketException(env, EAFNOSUPPORT);
                    return;
            }
            result = getOrSetSocketOption(SOCKOPT_SET, handle,
                    IP_MULTICAST_IF, IPV6_MULTICAST_IF, optionValue,
                    &optionLength);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }

        case JAVASOCKOPT_IP_MULTICAST_LOOP: {
            result = getOrSetSocketOption(SOCKOPT_SET, handle,
                                          IP_MULTICAST_LOOP,
                                          IPV6_MULTICAST_LOOP, &intVal,
                                          &intSize);
            if (0 != result) {
                jniThrowSocketException(env, errno);
                return;
            }
            break;
        }
#else
        case JAVASOCKOPT_MCAST_TTL:
        case JAVASOCKOPT_MCAST_ADD_MEMBERSHIP:
        case JAVASOCKOPT_MCAST_DROP_MEMBERSHIP:
        case JAVASOCKOPT_IP_MULTICAST_IF:
        case JAVASOCKOPT_IP_MULTICAST_IF2:
        case JAVASOCKOPT_IP_MULTICAST_LOOP: {
            jniThrowException(env, "java/lang/UnsupportedOperationException", NULL);
            return;
        }
#endif // def ENABLE_MULTICAST

        default: {
            jniThrowSocketException(env, ENOPROTOOPT);
        }
    }
}

static void osNetworkSystem_socketClose(JNIEnv* env, jobject, jobject fileDescriptor) {
    int fd;
    if (!jniGetFd(env, fileDescriptor, fd)) {
        return;
    }

    jniSetFileDescriptorOfFD(env, fileDescriptor, -1);

    close(fd);
}

/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "accept",                            "(Ljava/io/FileDescriptor;Ljava/net/SocketImpl;Ljava/io/FileDescriptor;I)V",(void*) osNetworkSystem_accept },
    { "bind",                              "(Ljava/io/FileDescriptor;Ljava/net/InetAddress;I)V",                       (void*) osNetworkSystem_bind },
    { "byteArrayToIpString",               "([B)Ljava/lang/String;",                                                   (void*) osNetworkSystem_byteArrayToIpString },
    { "connectDatagram",                   "(Ljava/io/FileDescriptor;IILjava/net/InetAddress;)V",                      (void*) osNetworkSystem_connectDatagram },
    { "connectStreamWithTimeoutSocket",    "(Ljava/io/FileDescriptor;IIILjava/net/InetAddress;)V",                     (void*) osNetworkSystem_connectStreamWithTimeoutSocket },
    { "connectWithTimeout",                "(Ljava/io/FileDescriptor;IILjava/net/InetAddress;II[B)I",                  (void*) osNetworkSystem_connectWithTimeout },
    { "createDatagramSocket",              "(Ljava/io/FileDescriptor;Z)V",                                             (void*) osNetworkSystem_createDatagramSocket },
    { "createServerStreamSocket",          "(Ljava/io/FileDescriptor;Z)V",                                             (void*) osNetworkSystem_createServerStreamSocket },
    { "createStreamSocket",                "(Ljava/io/FileDescriptor;Z)V",                                             (void*) osNetworkSystem_createStreamSocket },
    { "disconnectDatagram",                "(Ljava/io/FileDescriptor;)V",                                              (void*) osNetworkSystem_disconnectDatagram },
    { "getSocketLocalAddress",             "(Ljava/io/FileDescriptor;)Ljava/net/InetAddress;",                         (void*) osNetworkSystem_getSocketLocalAddress },
    { "getSocketLocalPort",                "(Ljava/io/FileDescriptor;)I",                                              (void*) osNetworkSystem_getSocketLocalPort },
    { "getSocketOption",                   "(Ljava/io/FileDescriptor;I)Ljava/lang/Object;",                            (void*) osNetworkSystem_getSocketOption },
    { "ipStringToByteArray",               "(Ljava/lang/String;)[B",                                                   (void*) osNetworkSystem_ipStringToByteArray },
    { "listenStreamSocket",                "(Ljava/io/FileDescriptor;I)V",                                             (void*) osNetworkSystem_listenStreamSocket },
    { "peekDatagram",                      "(Ljava/io/FileDescriptor;Ljava/net/InetAddress;I)I",                       (void*) osNetworkSystem_peekDatagram },
    { "readDirect",                        "(Ljava/io/FileDescriptor;III)I",                                           (void*) osNetworkSystem_readDirect },
    { "readSocketImpl",                    "(Ljava/io/FileDescriptor;[BIII)I",                                         (void*) osNetworkSystem_readSocketImpl },
    { "receiveDatagramDirect",             "(Ljava/io/FileDescriptor;Ljava/net/DatagramPacket;IIIIZ)I",                (void*) osNetworkSystem_receiveDatagramDirect },
    { "receiveDatagram",                   "(Ljava/io/FileDescriptor;Ljava/net/DatagramPacket;[BIIIZ)I",               (void*) osNetworkSystem_receiveDatagram },
    { "recvConnectedDatagramDirect",       "(Ljava/io/FileDescriptor;Ljava/net/DatagramPacket;IIIIZ)I",                (void*) osNetworkSystem_recvConnectedDatagramDirect },
    { "recvConnectedDatagram",             "(Ljava/io/FileDescriptor;Ljava/net/DatagramPacket;[BIIIZ)I",               (void*) osNetworkSystem_recvConnectedDatagram },
    { "selectImpl",                        "([Ljava/io/FileDescriptor;[Ljava/io/FileDescriptor;II[IJ)Z",               (void*) osNetworkSystem_selectImpl },
    { "sendConnectedDatagramDirect",       "(Ljava/io/FileDescriptor;IIIZ)I",                                          (void*) osNetworkSystem_sendConnectedDatagramDirect },
    { "sendConnectedDatagram",             "(Ljava/io/FileDescriptor;[BIIZ)I",                                         (void*) osNetworkSystem_sendConnectedDatagram },
    { "sendDatagramDirect",                "(Ljava/io/FileDescriptor;IIIIZILjava/net/InetAddress;)I",                  (void*) osNetworkSystem_sendDatagramDirect },
    { "sendDatagram",                      "(Ljava/io/FileDescriptor;[BIIIZILjava/net/InetAddress;)I",                 (void*) osNetworkSystem_sendDatagram },
    { "sendDatagram2",                     "(Ljava/io/FileDescriptor;[BIIILjava/net/InetAddress;)I",                   (void*) osNetworkSystem_sendDatagram2 },
    { "sendUrgentData",                    "(Ljava/io/FileDescriptor;B)V",                                             (void*) osNetworkSystem_sendUrgentData },
    { "setInetAddress",                    "(Ljava/net/InetAddress;[B)V",                                              (void*) osNetworkSystem_setInetAddress },
    { "setNonBlocking",                    "(Ljava/io/FileDescriptor;Z)V",                                             (void*) osNetworkSystem_setNonBlocking },
    { "setSocketOption",                   "(Ljava/io/FileDescriptor;ILjava/lang/Object;)V",                           (void*) osNetworkSystem_setSocketOption },
    { "shutdownInput",                     "(Ljava/io/FileDescriptor;)V",                                              (void*) osNetworkSystem_shutdownInput },
    { "shutdownOutput",                    "(Ljava/io/FileDescriptor;)V",                                              (void*) osNetworkSystem_shutdownOutput },
    { "socketClose",                       "(Ljava/io/FileDescriptor;)V",                                              (void*) osNetworkSystem_socketClose },
    { "supportsUrgentData",                "(Ljava/io/FileDescriptor;)Z",                                              (void*) osNetworkSystem_supportsUrgentData },
    { "writeDirect",                       "(Ljava/io/FileDescriptor;III)I",                                           (void*) osNetworkSystem_writeDirect },
    { "write",                             "(Ljava/io/FileDescriptor;[BII)I",                                          (void*) osNetworkSystem_write },
};

int register_org_apache_harmony_luni_platform_OSNetworkSystem(JNIEnv* env) {
    return initCachedFields(env) && jniRegisterNativeMethods(env,
            "org/apache/harmony/luni/platform/OSNetworkSystem",
            gMethods,
            NELEM(gMethods));
}
// END android-changed
