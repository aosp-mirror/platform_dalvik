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

#define LOG_TAG "OSNetworkSystem"

#include "JNIHelp.h"
#include "jni.h"
#include "errno.h"

#include <unistd.h>
#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <netdb.h>
#include <sys/time.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <sys/un.h>

#include <cutils/properties.h>
#include <cutils/adb_networking.h>
#include "AndroidSystemNatives.h"

// Temporary hack to build on systems that don't have up-to-date libc headers.
#ifndef IPV6_TCLASS
#define IPV6_TCLASS 67
#endif

/**
 * @name Socket Errors
 * Error codes for socket operations
 *
 * @internal SOCKERR* range from -200 to -299 avoid overlap
 */
#define SOCKERR_BADSOCKET          -200 /* generic error */
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
#define SOCKERR_UNKNOWNSOCKET      -215 /* resolution of fileDescriptor to socket failed */
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
#define JAVASOCKOPT_MCAST_TIME_TO_LIVE 10 /* Currently unused */
#define JAVASOCKOPT_SO_BINDADDR 15
#define JAVASOCKOPT_MCAST_INTERFACE 16
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
#define SOCKET_READ_WRITE 3

#define SOCKET_MSG_PEEK 1
#define SOCKET_MSG_OOB 2

#define SOCKET_NOFLAGS 0

#undef BUFFERSIZE
#define BUFFERSIZE 2048

// wait for 500000 usec = 0.5 second
#define SEND_RETRY_TIME 500000

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
    jclass string_class;
    jmethodID string_class_init;
    jclass socketimpl_class;
    jfieldID socketimpl_address;
    jfieldID socketimpl_port;
    jclass dpack_class;
    jfieldID dpack_address;
    jfieldID dpack_port;
    jfieldID dpack_length;
} gCachedFields;

static int useAdbNetworking = 0;

/* needed for connecting with timeout */
typedef struct selectFDSet {
  int nfds;
  int sock;
  fd_set writeSet;
  fd_set readSet;
  fd_set exceptionSet;
} selectFDSet;

static const char * netLookupErrorString(int anErrorNum);

/**
 * Throws an SocketException with the message affiliated with the errorCode.
 */
static void throwSocketException(JNIEnv *env, int errorCode) {
    jniThrowException(env, "java/net/SocketException",
        netLookupErrorString(errorCode));
}

/**
 * Throws an IOException with the given message.
 */
static void throwIOExceptionStr(JNIEnv *env, const char *message) {
    jniThrowException(env, "java/io/IOException", message);
}

/**
 * Throws a NullPointerException.
 */
static void throwNullPointerException(JNIEnv *env) {
    jniThrowException(env, "java/lang/NullPointerException", NULL);
}

/**
 * Converts a 4-byte array to a native address structure. Throws a
 * NullPointerException or an IOException in case of error. This is
 * signaled by a return value of -1. The normal return value is 0.
 */
static int javaAddressToStructIn(
        JNIEnv *env, jbyteArray java_address, struct in_addr *address) {
    if (java_address == NULL) {
        return -1;
    }

    if (env->GetArrayLength(java_address) != sizeof(address->s_addr)) {
        return -1;
    }

    memset(address, 0, sizeof(address));

    jbyte* dst = reinterpret_cast<jbyte*>(&(address->s_addr));
    env->GetByteArrayRegion(java_address, 0, sizeof(address->s_addr), dst);
    return 0;
}

/**
 * Converts a native address structure to a Java byte array. Throws a
 * NullPointerException or an IOException in case of error. This is
 * signaled by a return value of -1. The normal return value is 0.
 *
 * @param address the sockaddr_storage structure to convert
 *
 * @exception SocketException the address family is unknown, or out of memory
 *
 */
static jbyteArray socketAddressToAddressBytes(JNIEnv *env,
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
        throwSocketException(env, SOCKERR_BADAF);
        return NULL;
    }

    jbyteArray byteArray = env->NewByteArray(addressLength);
    if (byteArray == NULL) {
        throwSocketException(env, SOCKERR_NOBUFFERS);
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
 * Converts a native address structure to an InetAddress object.
 * Throws a NullPointerException or an IOException in case of
 * error. This is signaled by a return value of -1. The normal
 * return value is 0.
 *
 * @param sockaddress the sockaddr_storage structure to convert
 *
 * @return a jobject representing an InetAddress
 */
static jobject socketAddressToInetAddress(JNIEnv *env,
        struct sockaddr_storage *sockaddress) {

    jbyteArray byteArray = socketAddressToAddressBytes(env, sockaddress);
    if (byteArray == NULL)  // Exception has already been thrown.
        return NULL;

    return env->CallStaticObjectMethod(gCachedFields.iaddr_class,
            gCachedFields.iaddr_getbyaddress, byteArray);
}

/**
 * Converts an InetAddress object and port number to a native address structure.
 * Throws a NullPointerException or a SocketException in case of
 * error. This is signaled by a return value of -1. The normal
 * return value is 0.
 *
 * @param inetaddress the InetAddress object to convert
 * @param port the port number
 * @param sockaddress the sockaddr_storage structure to write to
 *
 * @return 0 on success, -1 on failure
 *
 * @exception SocketError if the address family is unknown
 */
static int inetAddressToSocketAddress(JNIEnv *env, jobject inetaddress,
        int port, struct sockaddr_storage *sockaddress) {
    // Get the byte array that stores the IP address bytes in the InetAddress.
    if (inetaddress == NULL) {
        throwNullPointerException(env);
        return -1;
    }
    jbyteArray addressBytes =
        reinterpret_cast<jbyteArray>(env->GetObjectField(inetaddress,
            gCachedFields.iaddr_ipaddress));
    if (addressBytes == NULL) {
        throwNullPointerException(env);
        return -1;
    }

    // Convert the IP address bytes to the proper IP address type.
    size_t addressLength = env->GetArrayLength(addressBytes);
    if (addressLength == 4) {
        // IPv4 address.
        struct sockaddr_in *sin = reinterpret_cast<sockaddr_in*>(sockaddress);
        memset(sin, 0, sizeof(struct sockaddr_in));
        sin->sin_family = AF_INET;
        sin->sin_port = htons(port);
        jbyte* dst = reinterpret_cast<jbyte*>(&sin->sin_addr.s_addr);
        env->GetByteArrayRegion(addressBytes, 0, 4, dst);
    } else if (addressLength == 16) {
        // IPv6 address.
        struct sockaddr_in6 *sin6 = reinterpret_cast<sockaddr_in6*>(sockaddress);
        memset(sin6, 0, sizeof(struct sockaddr_in6));
        sin6->sin6_family = AF_INET6;
        sin6->sin6_port = htons(port);
        jbyte* dst = reinterpret_cast<jbyte*>(&sin6->sin6_addr.s6_addr);
        env->GetByteArrayRegion(addressBytes, 0, 16, dst);
    } else {
        // Unknown address family.
        throwSocketException(env, SOCKERR_BADAF);
        return -1;
    }
    return 0;
}

/**
 * Answer a new java.lang.Boolean object.
 *
 * @param env   pointer to the JNI library
 * @param anInt the Boolean constructor argument
 *
 * @return  the new Boolean
 */

jobject newJavaLangBoolean(JNIEnv * env, jint anInt) {
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

jobject newJavaLangByte(JNIEnv * env, jbyte val) {
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

jobject newJavaLangInteger(JNIEnv * env, jint anInt) {
    jclass tempClass;
    jmethodID tempMethod;

    tempClass = gCachedFields.integer_class;
    tempMethod = gCachedFields.integer_class_init;
    return env->NewObject(tempClass, tempMethod, anInt);
}

/**
 * Answer a new java.lang.String object.
 *
 * @param env   pointer to the JNI library
 * @param anInt the byte[] constructor argument
 *
 * @return  the new String
 */

jobject newJavaLangString(JNIEnv * env, jbyteArray bytes) {
    jclass tempClass;
    jmethodID tempMethod;

    tempClass = gCachedFields.string_class;
    tempMethod = gCachedFields.string_class_init;
    return env->NewObject(tempClass, tempMethod, (jbyteArray) bytes);
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
    struct timeval tp;
    struct timezone tzp;

    gettimeofday(&tp, &tzp);
    return (tp.tv_sec * 1000) + (tp.tv_usec / 1000);
}

/**
 * Check if the passed sockaddr_storage struct contains a localhost address
 *
 * @param address address pointer to the address to check
 *
 * @return 0 if the passed address isn't a localhost address
 */
static int isLocalHost(struct sockaddr_storage *address) {
    if (address->ss_family == AF_INET) {
        struct sockaddr_in *sin = (struct sockaddr_in *) address;
        return (sin->sin_addr.s_addr == htonl(INADDR_LOOPBACK));
    } else if (address->ss_family == AF_INET6) {
        struct sockaddr_in6 *sin6 = (struct sockaddr_in6 *) address;
        return IN6_IS_ADDR_LOOPBACK(&sin6->sin6_addr);
    } else {
        return 0;
    }
}

/**
 * Decide whether to use ADB networking for the given socket address.
 *
 * @param address pointer to sockaddr_storage structure to check
 *
 * @return true if ADB networking should be used, false otherwise.
 */
static bool useAdbNetworkingForAddress(struct sockaddr_storage *address) {
    return useAdbNetworking && !isLocalHost(address) &&
           address->ss_family == AF_INET;
}

/**
 * Convert a sockaddr_storage structure to a string for logging purposes.
 *
 * @param address pointer to sockaddr_storage structure to print
 *
 * @return a string with the textual representation of the address.
 *
 * @note Returns a statically allocated buffer, so is not thread-safe.
 */
static char *socketAddressToString(struct sockaddr_storage *address) {
    static char invalidString[] = "<invalid>";
    static char ipString[INET6_ADDRSTRLEN + sizeof("[]:65535")];

    char tmp[INET6_ADDRSTRLEN];
    int port;
    // TODO: getnameinfo seems to want its length parameter to be exactly
    // sizeof(sockaddr_in) for an IPv4 address and sizeof (sockaddr_in6) for an
    // IPv6 address. Fix getnameinfo so it accepts sizeof(sockaddr_storage), and
    // then remove this hack.
    int size = (address->ss_family == AF_INET) ?
            sizeof(sockaddr_in) : sizeof(sockaddr_in6);
    int result = getnameinfo((struct sockaddr *)address,
            size, tmp, sizeof(tmp), NULL, 0,
            NI_NUMERICHOST);

    if (result != 0)
        return invalidString;

    if (address->ss_family == AF_INET6) {
        struct sockaddr_in6 *sin6 = (struct sockaddr_in6 *) address;
        port = ntohs(sin6->sin6_port);
        sprintf(ipString, "[%s]:%d", tmp, port);
        return ipString;
    } else if (address->ss_family == AF_INET) {
        struct sockaddr_in *sin = (struct sockaddr_in *) address;
        port = ntohs(sin->sin_port);
        sprintf(ipString, "%s:%d", tmp, port);
        return ipString;
    } else {
        return invalidString;
    }
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
        case SOCKERR_BADSOCKET:
            return "Bad socket";
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
        case SOCKERR_UNKNOWNSOCKET:
            return "Resolution of the FileDescriptor to socket failed";
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

static int sockSelect(int nfds, fd_set *readfds, fd_set *writefds,
        fd_set *exceptfds, struct timeval *timeout) {

    int result = select(nfds, readfds, writefds, exceptfds, timeout);

    if (result < 0) {
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

#define SELECT_READ_TYPE 0
#define SELECT_WRITE_TYPE 1

static int selectWait(int handle, int uSecTime, int type) {
    fd_set fdset;
    struct timeval time, *timePtr;
    int result = 0;
    int size = handle + 1;

    FD_ZERO(&fdset);
    FD_SET(handle, &fdset);

    if (0 <= uSecTime) {
        /* Use a timeout if uSecTime >= 0 */
        memset(&time, 0, sizeof(time));
        time.tv_usec = uSecTime;
        timePtr = &time;
    } else {
        /* Infinite timeout if uSecTime < 0 */
        timePtr = NULL;
    }

    if (type == SELECT_READ_TYPE) {
        result = sockSelect(size, &fdset, NULL, NULL, timePtr);
    } else {
        result = sockSelect(size, NULL, &fdset, NULL, timePtr);
    }
    return result;
}

static int pollSelectWait(JNIEnv *env, jobject fileDescriptor, int timeout, int type) {
    /* now try reading the socket for the timespan timeout.
     * if timeout is 0 try forever until the soclets gets ready or until an
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

        if (handle == 0 || handle == -1) {
            throwSocketException(env, SOCKERR_INTERRUPTED);
            return -1;
        }

        if (hasTimeout) {

            if (timeLeft - 10 < pollMsec) {
                pollTimeoutUSec = timeLeft <= 0 ? 0 : (timeLeft * 1000);
            }

            result = selectWait(handle, pollTimeoutUSec, type);

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
                    jniThrowException(env, "java/net/SocketTimeoutException",
                            netLookupErrorString(SOCKERR_TIMEOUT));
                } else {
                    continue; // try again
                }

            } else if (0 > result) {
                throwSocketException(env, result);
            }
            poll = 0;

        } else { /* polling with no timeout (why would you do this?)*/

            result = selectWait(handle, pollTimeoutUSec, type);

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
 * Obtain the socket address family from an existing socket.
 *
 * @param socket the filedescriptor of the socket to examine
 *
 * @return an integer, the address family of the socket
 */
static int getSocketAddressFamily(int socket) {
  struct sockaddr_storage ss;
  socklen_t namelen = sizeof(ss);
  int ret = getsockname(socket, (struct sockaddr*) &ss, &namelen);
  if (ret != 0) {
    return AF_UNSPEC;
  } else {
    return ss.ss_family;
  }
}

/**
 * A helper method, to set the connect context to a Long object.
 *
 * @param env  pointer to the JNI library
 * @param longclass Java Long Object
 */
void setConnectContext(JNIEnv *env,jobject longclass,jbyte * context) {
    jclass descriptorCLS;
    jfieldID descriptorFID;
    descriptorCLS = env->FindClass("java/lang/Long");
    descriptorFID = env->GetFieldID(descriptorCLS, "value", "J");
    env->SetLongField(longclass, descriptorFID, (jlong)((jint)context));
};

/**
 * A helper method, to get the connect context.
 *
 * @param env  pointer to the JNI library
 * @param longclass Java Long Object
 */
jbyte *getConnectContext(JNIEnv *env, jobject longclass) {
    jclass descriptorCLS;
    jfieldID descriptorFID;
    descriptorCLS = env->FindClass("java/lang/Long");
    descriptorFID = env->GetFieldID(descriptorCLS, "value", "J");
    return (jbyte*) ((jint)env->GetLongField(longclass, descriptorFID));
};

// typical ip checksum
unsigned short ip_checksum(unsigned short* buffer, int size) {
    register unsigned short * buf = buffer;
    register int bufleft = size;
    register unsigned long sum = 0;

    while (bufleft > 1) {
        sum = sum + (*buf++);
        bufleft = bufleft - sizeof(unsigned short );
    }
    if (bufleft) {
        sum = sum + (*(unsigned char*)buf);
    }
    sum = (sum >> 16) + (sum & 0xffff);
    sum += (sum >> 16);

    return (unsigned short )(~sum);
}

/**
 * Converts an IPv4 address to an IPv4-mapped IPv6 address. Performs no error
 * checking.
 *
 * @param address the address to convert. Must contain an IPv4 address.
 * @param outputAddress the converted address. Will contain an IPv6 address.
 * @param mapUnspecified if true, convert 0.0.0.0 to ::ffff:0:0; if false, to ::
 */
static void ipv4ToMappedAddress(struct sockaddr_storage *address,
        struct sockaddr_storage *outputAddress, bool mapUnspecified) {
  memset(outputAddress, 0, sizeof(struct sockaddr_storage));
  const struct sockaddr_in *sin = ((struct sockaddr_in *) address);
  struct sockaddr_in6 *sin6 = ((struct sockaddr_in6 *) outputAddress);
  sin6->sin6_family = AF_INET6;
  sin6->sin6_addr.s6_addr32[3] = sin->sin_addr.s_addr;
  if (sin->sin_addr.s_addr != 0  || mapUnspecified) {
      sin6->sin6_addr.s6_addr32[2] = htonl(0xffff);
  }
  sin6->sin6_port = sin->sin_port;
}

/**
 * Wrapper for connect() that converts IPv4 addresses to IPv4-mapped IPv6
 * addresses if necessary.
 *
 * @param socket the filedescriptor of the socket to connect
 * @param socketAddress the address to connect to
 */
static int doConnect(int socket, struct sockaddr_storage *socketAddress) {
    struct sockaddr_storage mappedAddress;
    struct sockaddr_storage *realAddress;
    if (socketAddress->ss_family == AF_INET &&
        getSocketAddressFamily(socket) == AF_INET6) {
        ipv4ToMappedAddress(socketAddress, &mappedAddress, true);
        realAddress = &mappedAddress;
    } else {
        realAddress = socketAddress;
    }
    int ret;
    do {
        ret = connect(socket, (struct sockaddr *) realAddress,
                sizeof(struct sockaddr_storage));
    } while (ret < 0 && errno == EINTR);
    return ret;
}

/**
 * Wrapper for bind() that converts IPv4 addresses to IPv4-mapped IPv6
 * addresses if necessary.
 *
 * @param socket the filedescriptor of the socket to connect
 * @param socketAddress the address to connect to
 */
static int doBind(int socket, struct sockaddr_storage *socketAddress) {
    struct sockaddr_storage mappedAddress;
    struct sockaddr_storage *realAddress;
    if (socketAddress->ss_family == AF_INET &&
        getSocketAddressFamily(socket) == AF_INET6) {
        ipv4ToMappedAddress(socketAddress, &mappedAddress, false);
        realAddress = &mappedAddress;
    } else {
        realAddress = socketAddress;
    }
    int ret;
    do {
        ret = bind(socket, (struct sockaddr *) realAddress,
                sizeof(struct sockaddr_storage));
    } while (ret < 0 && errno == EINTR);
    return ret;
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
static int sockConnectWithTimeout(int handle, struct sockaddr_storage addr,
        unsigned int timeout, unsigned int step, jbyte *ctxt) {
    int rc = 0;
    struct timeval passedTimeout;
    int errorVal;
    socklen_t errorValLen = sizeof(int);
    struct selectFDSet *context = NULL;

    if (SOCKET_STEP_START == step) {

        context = (struct selectFDSet *) ctxt;

        context->sock = handle;
        context->nfds = handle + 1;

        if (useAdbNetworkingForAddress(&addr)) {

            // LOGD("+connect to address 0x%08x (via adb)",
            //         addr.sin_addr.s_addr);
            rc = adb_networking_connect_fd(handle, (struct sockaddr_in *) &addr);
            // LOGD("-connect ret %d errno %d (via adb)", rc, errno);

        } else {
            /* set the socket to non-blocking */
            int block = JNI_TRUE;
            rc = ioctl(handle, FIONBIO, &block);
            if (0 != rc) {
                return convertError(rc);
            }

            // LOGD("+connect to address 0x%08x (via normal) on handle %d",
            //         addr.sin_addr.s_addr, handle);
            rc = doConnect(handle, &addr);
            // LOGD("-connect to address 0x%08x (via normal) returned %d",
            //         addr.sin_addr.s_addr, (int) rc);

        }

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

        context = (struct selectFDSet *) ctxt;

        /*
         * set the timeout value to be used. Because on some unix platforms we
         * don't get notified when a socket is closed we only sleep for 100ms
         * at a time
         */
        passedTimeout.tv_sec = 0;
        if (timeout > 100) {
            passedTimeout.tv_usec = 100 * 1000;
        } else if ((int)timeout >= 0) {
          passedTimeout.tv_usec = timeout * 1000;
        }

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
                   (int)timeout >= 0 ? &passedTimeout : NULL);

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
            errno = EAFNOSUPPORT;
            return -1;
    }
    if (action == SOCKOPT_GET) {
        return getsockopt(socket, protocol, option, &optionValue, optionLength);
    } else if (action == SOCKOPT_SET) {
        return setsockopt(socket, protocol, option, &optionValue,
                          *optionLength);
    } else {
        errno = EINVAL;
        return -1;
    }
}

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
    socklen_t requestLength;
    int interfaceIndex;
    int result;
    if (family == AF_INET) {
        // IP_MULTICAST_IF returns a pointer to a struct ip_mreqn.
        struct ip_mreqn tempRequest;
        requestLength = sizeof(tempRequest);
        result = getsockopt(socket, IPPROTO_IP, IP_MULTICAST_IF, &tempRequest,
            &requestLength);
        interfaceIndex = tempRequest.imr_ifindex;
    } else if (family == AF_INET6) {
        // IPV6_MULTICAST_IF returns a pointer to an integer.
        requestLength = sizeof(interfaceIndex);
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
static void mcastAddDropMembership (JNIEnv * env, int handle, jobject optVal,
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
                throwSocketException(env, convertError(errno));
                return;
            }
        }

        // Convert the inetAddress to an IPv4 address structure.
        result = inetAddressToSocketAddress(env, optVal, 0, &sockaddrP);
        if (result < 0)  // Exception has already been thrown.
            return;
        if (sockaddrP.ss_family != AF_INET) {
            throwSocketException(env, SOCKERR_BADAF);
            return;
        }
        struct sockaddr_in *sin = (struct sockaddr_in *) &sockaddrP;
        multicastRequest.imr_multiaddr = sin->sin_addr;

        result = setsockopt(handle, IPPROTO_IP, setSockOptVal,
                            &multicastRequest, length);
        if (0 != result) {
            throwSocketException (env, convertError(errno));
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

        result = inetAddressToSocketAddress(env, multiaddr, 0, &sockaddrP);
        if (result < 0)  // Exception has already been thrown.
            return;

        struct ip_mreqn ipv4Request;
        struct ipv6_mreq ipv6Request;
        void *multicastRequest;
        socklen_t requestLength;
        int level;
        int family = getSocketAddressFamily(handle);
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
                requestLength = sizeof(ipv6Request);
                memset(&ipv6Request, 0, requestLength);
                ipv6Request.ipv6mr_multiaddr =
                        ((struct sockaddr_in6 *) &sockaddrP)->sin6_addr;
                ipv6Request.ipv6mr_interface = interfaceIndex;
                multicastRequest = &ipv6Request;
                level = IPPROTO_IPV6;
                break;
           default:
               throwSocketException (env, SOCKERR_BADAF);
               return;
        }

        /* join/drop the multicast address */
        result = setsockopt(handle, level, setSockOptVal, multicastRequest,
                            requestLength);
        if (0 != result) {
            throwSocketException (env, convertError(errno));
            return;
        }
    }
}

static void osNetworkSystem_oneTimeInitializationImpl(JNIEnv* env, jobject obj,
        jboolean jcl_supports_ipv6) {
    // LOGD("ENTER oneTimeInitializationImpl of OSNetworkSystem");

    char useAdbNetworkingProperty[PROPERTY_VALUE_MAX];
    char adbConnectedProperty[PROPERTY_VALUE_MAX];

    property_get("android.net.use-adb-networking", useAdbNetworkingProperty, "");
    property_get("adb.connected", adbConnectedProperty, "");

    if (strlen((char *)useAdbNetworkingProperty) > 0
            && strlen((char *)adbConnectedProperty) > 0) {
        useAdbNetworking = 1;
    }

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
        {&c->string_class, "java/lang/String"},
        {&c->socketimpl_class, "java/net/SocketImpl"},
        {&c->dpack_class, "java/net/DatagramPacket"}
    };
    for (unsigned i = 0; i < sizeof(classes) / sizeof(classes[0]); i++) {
        classInfo c = classes[i];
        jclass tempClass = env->FindClass(c.name);
        if (tempClass == NULL) return;
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
        {&c->string_class_init, c->string_class, "<init>", "([B)V", false},
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
        if (*m.method == NULL) return;
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
        if (*f.field == NULL) return;
    }
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
        throwNullPointerException(env);
        errno = EBADF;
        return -1;
    }

    int sock;
    sock = socket(PF_INET6, type, 0);
    if (sock < 0 && errno == EAFNOSUPPORT) {
        sock = socket(PF_INET, type, 0);
    }
    if (sock < 0) {
        int err = convertError(errno);
        throwSocketException(env, err);
        return sock;
    }
    jniSetFileDescriptorOfFD(env, fileDescriptor, sock);
    return sock;
}


static void osNetworkSystem_createStreamSocketImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jboolean preferIPv4Stack) {
    // LOGD("ENTER createSocketImpl");
    createSocketFileDescriptor(env, fileDescriptor, SOCK_STREAM);
}

static void osNetworkSystem_createDatagramSocketImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jboolean preferIPv4Stack) {
    // LOGD("ENTER createDatagramSocketImpl");
    createSocketFileDescriptor(env, fileDescriptor, SOCK_DGRAM);
}

static jint osNetworkSystem_readSocketDirectImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jint address, jint offset, jint count,
        jint timeout) {
    // LOGD("ENTER readSocketDirectImpl");

    int handle;
    jbyte *message = (jbyte *)address + offset;
    int result, ret, localCount;

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return 0;
    }

    result = selectWait(handle, timeout, SELECT_READ_TYPE);

    if (0 > result) {
        return 0;
    }

    localCount = (count < 65536) ? count : 65536;

    do {
        ret = recv(handle, (jbyte *) message, localCount, SOCKET_NOFLAGS);
    } while (ret < 0 && errno == EINTR);

    if (0 == ret) {
        return -1;
    } else if (ret == -1) {
        int err = convertError(errno);
        throwSocketException(env, err);
        return 0;
    }
    return ret;
}

static jint osNetworkSystem_readSocketImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jbyteArray data, jint offset, jint count,
        jint timeout) {
    // LOGD("ENTER readSocketImpl");

    jbyte *message;
    int result, localCount;

    jbyte internalBuffer[BUFFERSIZE];

    localCount = (count < 65536) ? count : 65536;

    if (localCount > BUFFERSIZE) {
        message = (jbyte*)malloc(localCount * sizeof(jbyte));
        if (message == NULL) {
            jniThrowException(env, "java/lang/OutOfMemoryError",
                    "couldn't allocate enough memory for readSocket");
            return 0;
        }
    } else {
        message = (jbyte *)internalBuffer;
    }

    result = osNetworkSystem_readSocketDirectImpl(env, clazz, fileDescriptor,
            (jint) message, 0, localCount, timeout);

    if (result > 0) {
        env->SetByteArrayRegion(data, offset, result, (jbyte *)message);
    }

    if (((jbyte *)message) != internalBuffer) {
        free(( jbyte *)message);
    }

    return result;
}

static jint osNetworkSystem_writeSocketDirectImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jint address, jint offset, jint count) {
    // LOGD("ENTER writeSocketDirectImpl");

    int handle;
    jbyte *message = (jbyte *)address + offset;
    int result = 0, sent = 0;

    if (count <= 0) {
        return 0;
    }

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return 0;
    }

    result = send(handle, (jbyte *) message, (int) count, SOCKET_NOFLAGS);
    if (result < 0) {
        int err = convertError(errno);

        if (SOCKERR_WOULDBLOCK == err){
            jclass socketExClass,errorCodeExClass;
            jmethodID errorCodeExConstructor, socketExConstructor,socketExCauseMethod;
            jobject errorCodeEx, socketEx;
            const char* errorMessage = netLookupErrorString(err);
            jstring errorMessageString = env->NewStringUTF(errorMessage);

            errorCodeExClass = env->FindClass("org/apache/harmony/luni/util/ErrorCodeException");
            if (!errorCodeExClass){
                return 0;
            }
            errorCodeExConstructor = env->GetMethodID(errorCodeExClass,"<init>","(I)V");
            if (!errorCodeExConstructor){
                return 0;
            }
            errorCodeEx = env->NewObject(errorCodeExClass,errorCodeExConstructor,err);

            socketExClass = env->FindClass("java/net/SocketException");
            if (!socketExClass) {
                return 0;
            }
            socketExConstructor = env->GetMethodID(socketExClass,"<init>","(Ljava/lang/String;)V");
            if (!socketExConstructor) {
                return 0;
            }
            socketEx = env->NewObject(socketExClass, socketExConstructor, errorMessageString);
            socketExCauseMethod = env->GetMethodID(socketExClass,"initCause","(Ljava/lang/Throwable;)Ljava/lang/Throwable;");
            env->CallObjectMethod(socketEx,socketExCauseMethod,errorCodeEx);
            env->Throw((jthrowable)socketEx);
            return 0;
        }
        throwSocketException(env, err);
        return 0;
    }

    return result;
}

static jint osNetworkSystem_writeSocketImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jbyteArray data, jint offset, jint count) {
    // LOGD("ENTER writeSocketImpl");

    jbyte *message;
    int sent = 0;
    jint result = 0;

/* TODO: ARRAY PINNING */
#define INTERNAL_SEND_BUFFER_MAX 512
    jbyte internalBuffer[INTERNAL_SEND_BUFFER_MAX];

    if (count > INTERNAL_SEND_BUFFER_MAX) {
        message = (jbyte*)malloc(count * sizeof( jbyte));
        if (message == NULL) {
            jniThrowException(env, "java/lang/OutOfMemoryError",
                    "couldn't allocate enough memory for writeSocket");
            return 0;
        }
    } else {
        message = (jbyte *)internalBuffer;
    }

    env->GetByteArrayRegion(data, offset, count, message);

    result = osNetworkSystem_writeSocketDirectImpl(env, clazz, fileDescriptor,
            (jint) message, 0, count);

    if (( jbyte *)message != internalBuffer) {
        free(( jbyte *)message);
    }
#undef INTERNAL_SEND_BUFFER_MAX
    return result;
}

static void osNetworkSystem_setNonBlockingImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jboolean nonblocking) {
    // LOGD("ENTER setNonBlockingImpl");

    int handle;
    int result;

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return;
    }

    int block = nonblocking;

    result = ioctl(handle, FIONBIO, &block);

    if (result == -1) {
        throwSocketException(env, convertError(errno));
    }
}

static jint osNetworkSystem_connectSocketImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jint trafficClass, jobject inetAddr, jint port);

static jint osNetworkSystem_connectWithTimeoutSocketImpl(JNIEnv* env,
        jclass clazz, jobject fileDescriptor, jint timeout, jint trafficClass,
        jobject inetAddr, jint port, jint step, jbyteArray passContext) {
    // LOGD("ENTER connectWithTimeoutSocketImpl");

    int handle;
    int result = 0;
    struct sockaddr_storage address;
    jbyte *context = NULL;

    result = inetAddressToSocketAddress(env, inetAddr, port, &address);
    if (result < 0)
        return result;

    // Check if we're using adb networking and redirect in case it is used.
    if (useAdbNetworkingForAddress(&address)) {
        return osNetworkSystem_connectSocketImpl(env, clazz, fileDescriptor,
                trafficClass, inetAddr, port);
    }

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADDESC);
        return -1;
    }

    context = (jbyte *)env->GetPrimitiveArrayCritical(passContext, NULL);

    switch (step) {
        case SOCKET_CONNECT_STEP_START:
            result = sockConnectWithTimeout(handle, address, 0,
                    SOCKET_STEP_START, context);
            break;
        case SOCKET_CONNECT_STEP_CHECK:
            result = sockConnectWithTimeout(handle, address, timeout,
                    SOCKET_STEP_CHECK, context);
            break;
    }

    env->ReleasePrimitiveArrayCritical(passContext, context, JNI_ABORT);

    if (0 == result) {
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

static void osNetworkSystem_connectStreamWithTimeoutSocketImpl(JNIEnv* env,
        jclass clazz, jobject fileDescriptor, jint remotePort, jint timeout,
        jint trafficClass, jobject inetAddr) {
    // LOGD("ENTER connectStreamWithTimeoutSocketImpl");

    int result = 0;
    int handle;
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

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADDESC);
        return;
    }

    result = inetAddressToSocketAddress(env, inetAddr, remotePort, &address);
    if (result < 0)  // Exception has already been thrown.
        return;

    // Check if we're using adb networking and redirect in case it is used.
    if (useAdbNetworkingForAddress(&address)) {
        int retVal = osNetworkSystem_connectSocketImpl(env, clazz,
                fileDescriptor, trafficClass, inetAddr, remotePort);
        if (retVal != 0) {
            throwSocketException(env, SOCKERR_BADSOCKET);
        }
        return;
    }

    /*
     * we will be looping checking for when we are connected so allocate
     * the descriptor sets that we will use
     */
    context =(jbyte *) malloc(sizeof(struct selectFDSet));
    if (NULL == context) {
        throwSocketException(env, SOCKERR_NOBUFFERS);
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
        if (handle == 0 || handle == -1) {
            sockConnectWithTimeout(handle, address, 0,
                    SOCKET_STEP_DONE, context);
            throwSocketException(env, SOCKERR_BADSOCKET);
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
                    jniThrowException(env,
                            "java/net/SocketTimeoutException",
                            netLookupErrorString(result));
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

static jint osNetworkSystem_connectSocketImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jint trafficClass, jobject inetAddr, jint port) {
    //LOGD("ENTER direct-call connectSocketImpl\n");

    struct sockaddr_storage address;
    int ret;
    int handle;

    ret = inetAddressToSocketAddress(env, inetAddr, port, &address);
    if (ret < 0)
        return ret;

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADDESC);
        return -1;
    }

    if (useAdbNetworkingForAddress(&address)) {

        // LOGD("+connect to address 0x%08x port %d (via adb)",
        //         address.sin_addr.s_addr, (int) port);
        ret = adb_networking_connect_fd(handle, (struct sockaddr_in *) &address);
        // LOGD("-connect ret %d errno %d (via adb)", ret, errno);

    } else {

        // call this method with a timeout of zero
        osNetworkSystem_connectStreamWithTimeoutSocketImpl(env, clazz,
                fileDescriptor, port, 0, trafficClass, inetAddr);
        if (env->ExceptionOccurred() != 0) {
            return -1;
        } else {
            return 0;
        }

    }

    if (ret < 0) {
        jniThrowException(env, "java/net/ConnectException",
                netLookupErrorString(convertError(errno)));
        return ret;
    }

    return ret;
}

static void osNetworkSystem_socketBindImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jint port, jobject inetAddress) {
    // LOGD("ENTER socketBindImpl");

    struct sockaddr_storage sockaddress;
    int ret;
    int handle;

    ret = inetAddressToSocketAddress(env, inetAddress, port, &sockaddress);
    if (ret < 0)
        return;

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADDESC);
        return;
    }

    ret = doBind(handle, &sockaddress);
    if (ret < 0) {
        jniThrowException(env, "java/net/BindException",
                netLookupErrorString(convertError(errno)));
        return;
    }
}

static void osNetworkSystem_listenStreamSocketImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jint backlog) {
    // LOGD("ENTER listenStreamSocketImpl");

    int ret;
    int handle;

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return;
    }

    ret = listen(handle, backlog);

    if (ret < 0) {
        int err = convertError(errno);
        throwSocketException(env, err);
        return;
    }
}

static jint osNetworkSystem_availableStreamImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor) {
    // LOGD("ENTER availableStreamImpl");

    int handle;
    char message[BUFFERSIZE];

    int result;

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADDESC);
        return 0;
    }

    do {
        result = selectWait(handle, 1, SELECT_READ_TYPE);

        if (SOCKERR_TIMEOUT == result) {
            // The read operation timed out, so answer 0 bytes available
            return 0;
        } else if (SOCKERR_INTERRUPTED == result) {
            continue;
        } else if (0 > result) {
            throwSocketException(env, result);
            return 0;
        }
    } while (SOCKERR_INTERRUPTED == result);

    result = recv(handle, (jbyte *) message, BUFFERSIZE, MSG_PEEK);

    if (0 > result) {
        int err = convertError(errno);
        throwSocketException(env, err);
        return 0;
    }
    return result;
}

static void osNetworkSystem_acceptSocketImpl(JNIEnv* env, jclass clazz,
        jobject fdServer, jobject newSocket, jobject fdnewSocket, jint timeout) {
    // LOGD("ENTER acceptSocketImpl");

    struct sockaddr_storage sa;

    int ret;
    int retFD;
    int result;
    int handle;
    socklen_t addrlen;

    if (newSocket == NULL) {
        throwNullPointerException(env);
        return;
    }

    result = pollSelectWait(env, fdServer, timeout, SELECT_READ_TYPE);
    if (0 > result) {
        return;
    }

    handle = jniGetFDFromFileDescriptor(env, fdServer);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADDESC);
        return;
    }

    do {
        addrlen = sizeof(sa);
        ret = accept(handle, (struct sockaddr *) &sa, &addrlen);
    } while (ret < 0 && errno == EINTR);

    if (ret < 0) {
        int err = convertError(errno);
        throwSocketException(env, err);
        return;
    }

    retFD = ret;

    /*
     * For network sockets, put the peer address and port in instance variables.
     * We don't bother to do this for UNIX domain sockets, since most peers are
     * anonymous anyway.
     */
    if (sa.ss_family == AF_INET || sa.ss_family == AF_INET6) {
        jobject inetAddress =  socketAddressToInetAddress(env, &sa);
        if (ret == -1) {
            close(retFD);
            newSocket = NULL;  // Exception has already been thrown.
            return;
        }

        env->SetObjectField(newSocket,
                gCachedFields.socketimpl_address, inetAddress);

        int port = getSocketAddressPort(&sa);
        env->SetIntField(newSocket, gCachedFields.socketimpl_port, port);
    }

    jniSetFileDescriptorOfFD(env, fdnewSocket, retFD);
}

static jboolean osNetworkSystem_supportsUrgentDataImpl(JNIEnv* env,
        jclass clazz, jobject fileDescriptor) {
    // LOGD("ENTER supportsUrgentDataImpl");

    int handle;

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (handle == 0 || handle == -1) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static void osNetworkSystem_sendUrgentDataImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jbyte value) {
    // LOGD("ENTER sendUrgentDataImpl");

    int handle;
    int result;

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return;
    }

    result = send(handle, (jbyte *) &value, 1, MSG_OOB);
    if (result < 0) {
        int err = convertError(errno);
        throwSocketException(env, err);
    }
}

static void osNetworkSystem_connectDatagramImpl2(JNIEnv* env, jclass clazz,
        jobject fd, jint port, jint trafficClass, jobject inetAddress) {
    // LOGD("ENTER connectDatagramImpl2");

    int handle = jniGetFDFromFileDescriptor(env, fd);

    struct sockaddr_storage sockAddr;
    int ret;

    ret = inetAddressToSocketAddress(env, inetAddress, port, &sockAddr);
    if (ret < 0)  // Exception has already been thrown.
        return;

    ret = doConnect(handle, &sockAddr);
    if (ret < 0) {
        int err = convertError(errno);
        throwSocketException(env, err);
    }
}

static void osNetworkSystem_disconnectDatagramImpl(JNIEnv* env, jclass clazz,
        jobject fd) {
    // LOGD("ENTER disconnectDatagramImpl");

    int handle = jniGetFDFromFileDescriptor(env, fd);

    struct sockaddr_storage sockAddr;
    memset(&sockAddr, 0, sizeof(sockAddr));
    sockAddr.ss_family = AF_UNSPEC;

    int result = doConnect(handle, &sockAddr);
    if (result < 0) {
        int err = convertError(errno);
        throwSocketException(env, err);
    }
}

static jint osNetworkSystem_peekDatagramImpl(JNIEnv* env, jclass clazz,
        jobject fd, jobject sender, jint receiveTimeout) {
    // LOGD("ENTER peekDatagramImpl");

    int port = -1;

    int result = pollSelectWait (env, fd, receiveTimeout, SELECT_READ_TYPE);
    if (0> result) {
        return (jint) 0;
    }

    int handle = jniGetFDFromFileDescriptor(env, fd);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADDESC);
        return 0;
    }

    struct sockaddr_storage sockAddr;
    socklen_t sockAddrLen = sizeof(sockAddr);
    ssize_t length;
    do {
        length = recvfrom(handle, NULL, 0, MSG_PEEK,
                (struct sockaddr *)&sockAddr, &sockAddrLen);
    } while (length < 0 && errno == EINTR);
    if (length < 0) {
        int err = convertError(errno);
        throwSocketException(env, err);
        return 0;
    }

    sender = socketAddressToInetAddress(env, &sockAddr);
    if (sender == NULL)  // Exception has already been thrown.
        return -1;

    port = getSocketAddressPort(&sockAddr);
    return port;
}

static jint osNetworkSystem_receiveDatagramDirectImpl(JNIEnv* env, jclass clazz,
        jobject fd, jobject packet, jint address, jint offset, jint length,
        jint receiveTimeout, jboolean peek) {
    // LOGD("ENTER receiveDatagramDirectImpl");

    int result = pollSelectWait (env, fd, receiveTimeout, SELECT_READ_TYPE);
    if (0 > result) {
        return (jint) 0;
    }

    int handle = jniGetFDFromFileDescriptor(env, fd);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADDESC);
        return 0;
    }

    struct sockaddr_storage sockAddr;
    socklen_t sockAddrLen = sizeof(sockAddr);

    int mode = peek ? MSG_PEEK : 0;

    ssize_t actualLength;
    do {
        actualLength = recvfrom(handle, (char*)(address + offset), length, mode,
                (struct sockaddr *)&sockAddr, &sockAddrLen);
    } while (actualLength < 0 && errno == EINTR);
    if (actualLength < 0) {
        int err = convertError(errno);
        throwSocketException(env, err);
        return 0;
    }

    if (packet != NULL) {
        jbyteArray addr = socketAddressToAddressBytes(env, &sockAddr);
        if (addr == NULL)  // Exception has already been thrown.
            return 0;
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

static jint osNetworkSystem_receiveDatagramImpl(JNIEnv* env, jclass clazz,
        jobject fd, jobject packet, jbyteArray data, jint offset, jint length,
        jint receiveTimeout, jboolean peek) {
    // LOGD("ENTER receiveDatagramImpl");

    int localLength = (length < 65536) ? length : 65536;
    jbyte *bytes = (jbyte*) malloc(localLength);
    if (bytes == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError",
                "couldn't allocate enough memory for receiveDatagram");
        return 0;
    }

    int actualLength = osNetworkSystem_receiveDatagramDirectImpl(env, clazz, fd,
            packet, (jint)bytes, 0, localLength, receiveTimeout, peek);

    if (actualLength > 0) {
        env->SetByteArrayRegion(data, offset, actualLength, bytes);
    }
    free(bytes);

    return actualLength;
}

static jint osNetworkSystem_recvConnectedDatagramDirectImpl(JNIEnv* env,
        jclass clazz, jobject fd, jobject packet, jint address, jint offset,
        jint length, jint receiveTimeout, jboolean peek) {
    // LOGD("ENTER receiveConnectedDatagramDirectImpl");

    int result = pollSelectWait (env, fd, receiveTimeout, SELECT_READ_TYPE);

    if (0 > result) {
        return 0;
    }

    int handle = jniGetFDFromFileDescriptor(env, fd);

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return 0;
    }

    int mode = peek ? MSG_PEEK : 0;

    int actualLength = recvfrom(handle,
            (char*)(address + offset), length, mode, NULL, NULL);

    if (actualLength < 0) {
        jniThrowException(env, "java/net/PortUnreachableException", "");
        return 0;
    }

    if ( packet != NULL) {
        env->SetIntField(packet, gCachedFields.dpack_length, actualLength);
    }
    return actualLength;
}

static jint osNetworkSystem_recvConnectedDatagramImpl(JNIEnv* env, jclass clazz,
        jobject fd, jobject packet, jbyteArray data, jint offset, jint length,
        jint receiveTimeout, jboolean peek) {
    // LOGD("ENTER receiveConnectedDatagramImpl");

    int localLength = (length < 65536) ? length : 65536;
    jbyte *bytes = (jbyte*) malloc(localLength);
    if (bytes == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError",
                "couldn't allocate enough memory for recvConnectedDatagram");
        return 0;
    }

    int actualLength = osNetworkSystem_recvConnectedDatagramDirectImpl(env,
            clazz, fd, packet, (jint)bytes, 0, localLength,
            receiveTimeout, peek);

    if (actualLength > 0) {
        env->SetByteArrayRegion(data, offset, actualLength, bytes);
    }
    free(bytes);

    return actualLength;
}

static jint osNetworkSystem_sendDatagramDirectImpl(JNIEnv* env, jclass clazz,
        jobject fd, jint address, jint offset, jint length, jint port,
        jboolean bindToDevice, jint trafficClass, jobject inetAddress) {
    // LOGD("ENTER sendDatagramDirectImpl");

    int handle = jniGetFDFromFileDescriptor(env, fd);

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return 0;
    }

    struct sockaddr_storage receiver;
    if (inetAddressToSocketAddress(env, inetAddress, port, &receiver) < 0) {
        // Exception has already been thrown.
        return 0;
    }

    ssize_t result = 0;
    do {
        result = sendto(handle, (char*)(address + offset), length,
                SOCKET_NOFLAGS, (struct sockaddr*)&receiver, sizeof(receiver));
    } while (result < 0 && errno == EINTR);
    if (result < 0) {
        int err = convertError(errno);
        if ((SOCKERR_CONNRESET == err)
                || (SOCKERR_CONNECTION_REFUSED == err)) {
            return 0;
        } else {
            throwSocketException(env, err);
            return 0;
        }
    }
    return (jint) result;
}

static jint osNetworkSystem_sendDatagramImpl(JNIEnv* env, jclass clazz,
        jobject fd, jbyteArray data, jint offset, jint length, jint port,
        jboolean bindToDevice, jint trafficClass, jobject inetAddress) {
    // LOGD("ENTER sendDatagramImpl");

    jbyte *bytes = env->GetByteArrayElements(data, NULL);
    int actualLength = osNetworkSystem_sendDatagramDirectImpl(env, clazz, fd,
            (jint)bytes, offset, length, port, bindToDevice, trafficClass,
            inetAddress);
    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);

    return actualLength;
}

static jint osNetworkSystem_sendConnectedDatagramDirectImpl(JNIEnv* env,
        jclass clazz, jobject fd, jint address, jint offset, jint length,
        jboolean bindToDevice) {
    // LOGD("ENTER sendConnectedDatagramDirectImpl");

    int handle = jniGetFDFromFileDescriptor(env, fd);

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return 0;
    }

    int result = send(handle, (char*)(address + offset), length, 0);

    if (result < 0) {
        int err = convertError(errno);
        if ((SOCKERR_CONNRESET == err) || (SOCKERR_CONNECTION_REFUSED == err)) {
            return 0;
        } else {
            throwSocketException(env, err);
            return 0;
        }
    }
    return result;
}

static jint osNetworkSystem_sendConnectedDatagramImpl(JNIEnv* env, jclass clazz,
        jobject fd, jbyteArray data, jint offset, jint length,
        jboolean bindToDevice) {
    // LOGD("ENTER sendConnectedDatagramImpl");

    jbyte *bytes = env->GetByteArrayElements(data, NULL);
    int actualLength = osNetworkSystem_sendConnectedDatagramDirectImpl(env,
            clazz, fd, (jint)bytes, offset, length, bindToDevice);
    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);

    return actualLength;
}

static void osNetworkSystem_createServerStreamSocketImpl(JNIEnv* env,
        jclass clazz, jobject fileDescriptor, jboolean preferIPv4Stack) {
    // LOGD("ENTER createServerStreamSocketImpl");

    int handle = createSocketFileDescriptor(env, fileDescriptor, SOCK_STREAM);
    if (handle < 0)
        return;

    int value = 1;
    setsockopt(handle, SOL_SOCKET, SO_REUSEADDR, &value, sizeof(int));
}

/*
 * @param timeout in milliseconds.  If zero, block until data received
 */
static jint osNetworkSystem_receiveStreamImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jbyteArray data, jint offset, jint count,
        jint timeout) {
    // LOGD("ENTER receiveStreamImpl");

    int result;
    int handle = jniGetFDFromFileDescriptor(env, fileDescriptor);

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return 0;
    }

    // Cap read length to available buf size
    int spaceAvailable = env->GetArrayLength(data) - offset;
    int localCount = count < spaceAvailable? count : spaceAvailable;

    jbyte* body = env->GetByteArrayElements(data, NULL);

    // set timeout
    struct timeval tv;
    tv.tv_sec = timeout / 1000;
    tv.tv_usec = (timeout % 1000) * 1000;
    setsockopt(handle, SOL_SOCKET, SO_RCVTIMEO, (struct timeval *)&tv,
               sizeof(struct timeval));

    do {
        result = recv(handle, body + offset, localCount, SOCKET_NOFLAGS);
    } while (result < 0 && errno == EINTR);

    env->ReleaseByteArrayElements(data, body, 0);

    /*
     * If no bytes are read, return -1 to signal 'endOfFile'
     * to the Java input stream
     */
    if (0 < result) {
        return result;
    } else if (0 == result) {
        return -1;
    } else {
        // If EAGAIN or EWOULDBLOCK, read timed out
        if (errno == EAGAIN || errno == EWOULDBLOCK) {
            jniThrowException(env, "java/net/SocketTimeoutException",
                              netLookupErrorString(SOCKERR_TIMEOUT));
        } else {
            int err = convertError(errno);
            throwSocketException(env, err);
        }
        return 0;
    }
}

static jint osNetworkSystem_sendStreamImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jbyteArray data, jint offset, jint count) {
    // LOGD("ENTER sendStreamImpl");

    int handle = 0;
    int result = 0, sent = 0;

    jbyte *message = env->GetByteArrayElements(data, NULL);

    // Cap write length to available buf size
    int spaceAvailable = env->GetArrayLength(data) - offset;
    if (count > spaceAvailable) count = spaceAvailable;

    while (sent < count) {

        handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
        if (handle == 0 || handle == -1) {
            throwSocketException(env,
                    sent == 0 ? SOCKERR_BADSOCKET : SOCKERR_INTERRUPTED);
            env->ReleaseByteArrayElements(data, message, 0);
            return 0;
        }

        // LOGD("before select %d", count);
        selectWait(handle, SEND_RETRY_TIME, SELECT_WRITE_TYPE);
        result = send(handle, (jbyte *)message + offset + sent,
                (int) count - sent, SOCKET_NOFLAGS);

        if (result < 0) {
            result = errno;
            if (result == EAGAIN ||result == EWOULDBLOCK) {
                // LOGD("write blocked %d", sent);
                continue;
            }
            env->ReleaseByteArrayElements(data, message, 0);
            int err = convertError(result);
            throwSocketException(env, err);
            return 0;
        }
        sent += result;
    }

    env->ReleaseByteArrayElements(data, message, 0);
    return sent;
}

static void osNetworkSystem_shutdownInputImpl(JNIEnv* env, jobject obj,
        jobject fileDescriptor) {
    // LOGD("ENTER shutdownInputImpl");

    int ret;
    int handle;

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return;
    }

    ret = shutdown(handle, SHUT_RD);

    if (ret < 0) {
        int err = convertError(errno);
        throwSocketException(env, err);
        return;
    }
}

static void osNetworkSystem_shutdownOutputImpl(JNIEnv* env, jobject obj,
        jobject fileDescriptor) {
    // LOGD("ENTER shutdownOutputImpl");

    int ret;
    int handle;

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);

    if (handle == 0 || handle == -1) {
        return;
    }

    ret = shutdown(handle, SHUT_WR);

    if (ret < 0) {
        int err = convertError(errno);
        throwSocketException(env, err);
        return;
    }
}

static jint osNetworkSystem_sendDatagramImpl2(JNIEnv* env, jclass clazz,
        jobject fd, jbyteArray data, jint offset, jint length, jint port,
        jobject inetAddress) {
    // LOGD("ENTER sendDatagramImpl2");

    jbyte *message;
    unsigned short nPort;
    int ret = 0, sent = 0;
    int handle = 0;
    struct sockaddr_storage sockaddrP;

    if (inetAddress != NULL) {
        ret = inetAddressToSocketAddress(env, inetAddress, port, &sockaddrP);
        if (ret < 0)  // Exception has already been thrown.
            return 0;

        handle = jniGetFDFromFileDescriptor(env, fd);
        if (handle == 0 || handle == -1) {
            throwSocketException(env, SOCKERR_BADDESC);
            return 0;
        }
    }

    message = (jbyte*) malloc(length * sizeof(jbyte));
    if (message == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError",
                "couldn't allocate enough memory for readSocket");
        return 0;
    }

    env->GetByteArrayRegion(data, offset, length, message);

    while (sent < length) {
        handle = jniGetFDFromFileDescriptor(env, fd);

        if (handle == 0 || handle == -1) {
            throwSocketException(env,
                    sent == 0 ? SOCKERR_BADDESC : SOCKERR_INTERRUPTED);
            free(message);
            return 0;
        }

        ssize_t result;
        do {
            result = sendto(handle, (char *) (message + sent),
                    (int) (length - sent), SOCKET_NOFLAGS,
                    (struct sockaddr *) &sockaddrP, sizeof(sockaddrP));
        } while (result < 0 && errno == EINTR);
        if (result < 0) {
            int err = convertError(errno);
            throwSocketException(env, err);
            free(message);
            return 0;
        }

        sent += result;
    }

    free(message);
    return sent;
}

static jint osNetworkSystem_selectImpl(JNIEnv* env, jclass clazz,
        jobjectArray readFDArray, jobjectArray writeFDArray, jint countReadC,
        jint countWriteC, jintArray outFlags, jlong timeout) {
    // LOGD("ENTER selectImpl");

    struct timeval timeP;
    int result = 0;
    int size = 0;
    jobject gotFD;
    fd_set *fdset_read,*fdset_write;
    int handle;
    jint *flagArray;
    int val;
    unsigned int time_sec = (unsigned int)timeout/1000;
    unsigned int time_msec = (unsigned int)(timeout%1000);

    fdset_read = (fd_set *)malloc(sizeof(fd_set));
    fdset_write = (fd_set *)malloc(sizeof(fd_set));

    FD_ZERO(fdset_read);
    FD_ZERO(fdset_write);

    for (val = 0; val<countReadC; val++) {

        gotFD = env->GetObjectArrayElement(readFDArray,val);

        handle = jniGetFDFromFileDescriptor(env, gotFD);

        FD_SET(handle, fdset_read);

        if (0 > (size - handle)) {
            size = handle;
        }
    }

    for (val = 0; val<countWriteC; val++) {

        gotFD = env->GetObjectArrayElement(writeFDArray,val);

        handle = jniGetFDFromFileDescriptor(env, gotFD);

        FD_SET(handle, fdset_write);

        if (0 > (size - handle)) {
            size = handle;
        }
    }

    /* the size is the max_fd + 1 */
    size =size + 1;

    if (0 > size) {
        result = SOCKERR_FDSET_SIZEBAD;
    } else {
      /* only set when timeout >= 0 (non-block)*/
        if (0 <= timeout) {

            timeP.tv_sec = time_sec;
            timeP.tv_usec = time_msec*1000;

            result = sockSelect(size, fdset_read, fdset_write, NULL, &timeP);

        } else {
            result = sockSelect(size, fdset_read, fdset_write, NULL, NULL);
        }
    }

    if (0 < result) {
        /*output the result to a int array*/
        flagArray = env->GetIntArrayElements(outFlags, NULL);

        for (val=0; val<countReadC; val++) {
            gotFD = env->GetObjectArrayElement(readFDArray,val);

            handle = jniGetFDFromFileDescriptor(env, gotFD);

            if (FD_ISSET(handle,fdset_read)) {
                flagArray[val] = SOCKET_OP_READ;
            } else {
                flagArray[val] = SOCKET_OP_NONE;
            }
        }

        for (val=0; val<countWriteC; val++) {

            gotFD = env->GetObjectArrayElement(writeFDArray,val);

            handle = jniGetFDFromFileDescriptor(env, gotFD);

            if (FD_ISSET(handle,fdset_write)) {
                flagArray[val+countReadC] = SOCKET_OP_WRITE;
            } else {
                flagArray[val+countReadC] = SOCKET_OP_NONE;
            }
        }

        env->ReleaseIntArrayElements(outFlags, flagArray, 0);
    }

    free(fdset_write);
    free(fdset_read);

    /* return both correct and error result, let java handle the exception*/
    return result;
}

static jobject osNetworkSystem_getSocketLocalAddressImpl(JNIEnv* env,
        jclass clazz, jobject fileDescriptor, jboolean preferIPv6Addresses) {
    // LOGD("ENTER getSocketLocalAddressImpl");

    struct sockaddr_storage addr;
    socklen_t addrLen = sizeof(addr);

    memset(&addr, 0, addrLen);


    int handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_UNKNOWNSOCKET);
        return NULL;
    }

    int result;
    result = getsockname(handle, (struct sockaddr *)&addr, &addrLen);

    // Spec says ignore all errors
    return socketAddressToInetAddress(env, &addr);
}

static jint osNetworkSystem_getSocketLocalPortImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jboolean preferIPv6Addresses) {
    // LOGD("ENTER getSocketLocalPortImpl");

    struct sockaddr_storage addr;
    socklen_t addrLen = sizeof(addr);

    int handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    int result;

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_UNKNOWNSOCKET);
        return 0;
    }

    result = getsockname(handle, (struct sockaddr *)&addr, &addrLen);

    if (0 != result) {
        // The java spec does not indicate any exceptions on this call
        return 0;
    } else {
        return getSocketAddressPort(&addr);
    }
}

static jobject osNetworkSystem_getSocketOptionImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jint anOption) {
    // LOGD("ENTER getSocketOptionImpl");

    int handle;
    int intValue = 0;
    socklen_t intSize = sizeof(int);
    unsigned char byteValue = 0;
    socklen_t byteSize = sizeof(unsigned char);
    int result;
    struct sockaddr_storage sockVal;
    socklen_t sockSize = sizeof(sockVal);

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADDESC);
        return NULL;
    }

    switch ((int) anOption & 0xffff) {
        case JAVASOCKOPT_SO_LINGER: {
            struct linger lingr;
            socklen_t size = sizeof(struct linger);
            result = getsockopt(handle, SOL_SOCKET, SO_LINGER, &lingr, &size);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
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
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }
        case JAVASOCKOPT_MCAST_TTL: {
            if ((anOption >> 16) & BROKEN_MULTICAST_TTL) {
                return newJavaLangByte(env, 0);
            }
            result = getOrSetSocketOption(SOCKOPT_GET, handle, IP_MULTICAST_TTL,
                                          IPV6_MULTICAST_HOPS, &byteValue,
                                          &byteSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangByte(env, (jbyte)(byteValue & 0xFF));
        }
        case JAVASOCKOPT_MCAST_INTERFACE: {
            if ((anOption >> 16) & BROKEN_MULTICAST_IF) {
                return NULL;
            }
            result = getsockopt(handle, IPPROTO_IP, IP_MULTICAST_IF, &sockVal, &sockSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            // This option is IPv4-only.
            sockVal.ss_family = AF_INET;
            return socketAddressToInetAddress(env, &sockVal);
        }
        case JAVASOCKOPT_IP_MULTICAST_IF2: {
            if ((anOption >> 16) & BROKEN_MULTICAST_IF) {
                return NULL;
            }
            struct ip_mreqn multicastRequest;
            int interfaceIndex;
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
                    throwSocketException(env, SOCKERR_BADAF);
                    return NULL;
            }

            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }

            return newJavaLangInteger(env, interfaceIndex);
        }
        case JAVASOCKOPT_SO_SNDBUF: {
            result = getsockopt(handle, SOL_SOCKET, SO_SNDBUF, &intValue, &intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangInteger(env, intValue);
        }
        case JAVASOCKOPT_SO_RCVBUF: {
            result = getsockopt(handle, SOL_SOCKET, SO_RCVBUF, &intValue, &intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangInteger(env, intValue);
        }
        case JAVASOCKOPT_SO_BROADCAST: {
            result = getsockopt(handle, SOL_SOCKET, SO_BROADCAST, &intValue, &intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }
        case JAVASOCKOPT_SO_REUSEADDR: {
            result = getsockopt(handle, SOL_SOCKET, SO_REUSEADDR, &intValue, &intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }
        case JAVASOCKOPT_SO_KEEPALIVE: {
            result = getsockopt(handle, SOL_SOCKET, SO_KEEPALIVE, &intValue, &intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }
        case JAVASOCKOPT_SO_OOBINLINE: {
            result = getsockopt(handle, SOL_SOCKET, SO_OOBINLINE, &intValue, &intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }
        case JAVASOCKOPT_IP_MULTICAST_LOOP: {
            result = getOrSetSocketOption(SOCKOPT_GET, handle,
                                          IP_MULTICAST_LOOP,
                                          IPV6_MULTICAST_LOOP, &intValue,
                                          &intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangBoolean(env, intValue);
        }
        case JAVASOCKOPT_IP_TOS: {
            result = getOrSetSocketOption(SOCKOPT_GET, handle, IP_TOS,
                                          IPV6_TCLASS, &intValue, &intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangInteger(env, intValue);
        }
        case JAVASOCKOPT_SO_RCVTIMEOUT: {
            struct timeval timeout;
            socklen_t size = sizeof(timeout);
            result = getsockopt(handle, SOL_SOCKET, SO_RCVTIMEO, &timeout, &size);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return NULL;
            }
            return newJavaLangInteger(env, timeout.tv_sec * 1000 + timeout.tv_usec/1000);
        }
        default: {
            throwSocketException(env, SOCKERR_OPTUNSUPP);
            return NULL;
        }
    }

}

static void osNetworkSystem_setSocketOptionImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor, jint anOption, jobject optVal) {
    // LOGD("ENTER setSocketOptionImpl");

    int handle, result;
    int intVal;
    socklen_t intSize = sizeof(int);
    unsigned char byteVal;
    socklen_t byteSize = sizeof(unsigned char);
    struct sockaddr_storage sockVal;
    int sockSize = sizeof(sockVal);

    if (env->IsInstanceOf(optVal, gCachedFields.integer_class)) {
        intVal = (int) env->GetIntField(optVal, gCachedFields.integer_class_value);
    } else if (env->IsInstanceOf(optVal, gCachedFields.boolean_class)) {
        intVal = (int) env->GetBooleanField(optVal, gCachedFields.boolean_class_value);
    } else if (env->IsInstanceOf(optVal, gCachedFields.byte_class)) {
        byteVal = (int) env->GetByteField(optVal, gCachedFields.byte_class_value);
    } else if (env->IsInstanceOf(optVal, gCachedFields.iaddr_class)) {
        if (inetAddressToSocketAddress(env, optVal, 0, &sockVal) < 0) {
            // Exception has already been thrown.
            return;
        }
    } else if (env->IsInstanceOf(optVal, gCachedFields.genericipmreq_class)) {
        // we'll use optVal directly
    } else {
        throwSocketException(env, SOCKERR_OPTUNSUPP);
        return;
    }

    handle = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADDESC);
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
                throwSocketException(env, convertError(errno));
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
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }

        case JAVASOCKOPT_MCAST_TTL: {
            if ((anOption >> 16) & BROKEN_MULTICAST_TTL) {
                return;
            }
            result = getOrSetSocketOption(SOCKOPT_SET, handle, IP_MULTICAST_TTL,
                                          IPV6_MULTICAST_HOPS, &byteVal,
                                          &byteSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
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

        case JAVASOCKOPT_MCAST_INTERFACE: {
            if ((anOption >> 16) & BROKEN_MULTICAST_IF) {
                return;
            }
            // This call is IPv4 only.
            if (getSocketAddressFamily(handle) != AF_INET) {
                throwSocketException(env, SOCKERR_BADAF);
                return;
            }
            struct ip_mreqn mcast_req;
            memset(&mcast_req, 0, sizeof(mcast_req));
            struct sockaddr_in *sin = (struct sockaddr_in *) &sockVal;
            mcast_req.imr_address = sin->sin_addr;
            result = setsockopt(handle, IPPROTO_IP, IP_MULTICAST_IF,
                                &mcast_req, sizeof(mcast_req));
            if (0 != result) {
                throwSocketException(env, convertError(errno));
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
                    throwSocketException(env, SOCKERR_BADAF);
                    return;
            }
            result = getOrSetSocketOption(SOCKOPT_SET, handle,
                    IP_MULTICAST_IF, IPV6_MULTICAST_IF, optionValue,
                    &optionLength);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_SNDBUF: {
            result = setsockopt(handle, SOL_SOCKET, SO_SNDBUF, &intVal, intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_RCVBUF: {
            result = setsockopt(handle, SOL_SOCKET, SO_RCVBUF, &intVal, intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_BROADCAST: {
            result = setsockopt(handle, SOL_SOCKET, SO_BROADCAST, &intVal, intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_REUSEADDR: {
            result = setsockopt(handle, SOL_SOCKET, SO_REUSEADDR, &intVal, intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }
        case JAVASOCKOPT_SO_KEEPALIVE: {
            result = setsockopt(handle, SOL_SOCKET, SO_KEEPALIVE, &intVal, intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_OOBINLINE: {
            result = setsockopt(handle, SOL_SOCKET, SO_OOBINLINE, &intVal, intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
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
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }

        case JAVASOCKOPT_IP_TOS: {
            result = getOrSetSocketOption(SOCKOPT_SET, handle, IP_TOS,
                                          IPV6_TCLASS, &intVal, &intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }

        case JAVASOCKOPT_REUSEADDR_AND_REUSEPORT: {
            // SO_REUSEPORT doesn't need to get set on this System
            result = setsockopt(handle, SOL_SOCKET, SO_REUSEADDR, &intVal, intSize);
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }

        case JAVASOCKOPT_SO_RCVTIMEOUT: {
            struct timeval timeout;
            timeout.tv_sec = intVal / 1000;
            timeout.tv_usec = (intVal % 1000) * 1000;
            result = setsockopt(handle, SOL_SOCKET, SO_RCVTIMEO, &timeout,
                    sizeof(struct timeval));
            if (0 != result) {
                throwSocketException(env, convertError(errno));
                return;
            }
            break;
        }

        default: {
            throwSocketException(env, SOCKERR_OPTUNSUPP);
        }
    }
}

static jint osNetworkSystem_getSocketFlagsImpl(JNIEnv* env, jclass clazz) {
    // LOGD("ENTER getSocketFlagsImpl");

    // Not implemented by harmony
    return 0;
}

static void osNetworkSystem_socketCloseImpl(JNIEnv* env, jclass clazz,
        jobject fileDescriptor) {
    // LOGD("ENTER socketCloseImpl");

    int handle = jniGetFDFromFileDescriptor(env, fileDescriptor);

    if (handle == 0 || handle == -1) {
        throwSocketException(env, SOCKERR_BADSOCKET);
        return;
    }

    jniSetFileDescriptorOfFD(env, fileDescriptor, -1);

    close(handle);
}

static jobject osNetworkSystem_getHostByAddrImpl(JNIEnv* env, jclass clazz,
        jbyteArray addrStr) {
    // LOGD("ENTER getHostByAddrImpl");

    if (addrStr == NULL) {
        throwNullPointerException(env);
        return JNI_FALSE;
    }

    jstring address = (jstring)newJavaLangString(env, addrStr);
    jstring result;
    const char* addr = env->GetStringUTFChars(address, NULL);

    struct hostent* ent = gethostbyaddr(addr, strlen(addr), AF_INET);

    if (ent != NULL  && ent->h_name != NULL) {
        result = env->NewStringUTF(ent->h_name);
    } else {
        result = NULL;
    }

    env->ReleaseStringUTFChars(address, addr);

    return result;
}

static jobject osNetworkSystem_getHostByNameImpl(JNIEnv* env, jclass clazz,
        jstring nameStr, jboolean preferIPv6Addresses) {
    // LOGD("ENTER getHostByNameImpl");

    if (nameStr == NULL) {
        throwNullPointerException(env);
        return NULL;
    }

    const char* name = env->GetStringUTFChars(nameStr, NULL);

    if (useAdbNetworking) {

        union {
            struct in_addr a;
            jbyte j[4];
        } outaddr;

        // LOGD("ADB networking: +gethostbyname '%s'", name);
        int err;
        err = adb_networking_gethostbyname(name, &(outaddr.a));

        env->ReleaseStringUTFChars(nameStr, name);
#if 0
        LOGD("ADB networking: -gethostbyname err %d addr 0x%08x %u.%u.%u.%u",
                err, (unsigned int)outaddr.a.s_addr,
                outaddr.j[0],outaddr.j[1],
                outaddr.j[2],outaddr.j[3]);
#endif

        if (err < 0) {
            return NULL;
        } else {
            jbyteArray addr = env->NewByteArray(4);
            env->SetByteArrayRegion(addr, 0, 4, outaddr.j);
            return addr;
        }
    } else {

        // normal case...no adb networking
        struct hostent* ent = gethostbyname(name);

        env->ReleaseStringUTFChars(nameStr, name);

        if (ent != NULL  && ent->h_length > 0) {
            jbyteArray addr = env->NewByteArray(4);
            jbyte v[4];
            memcpy(v, ent->h_addr, 4);
            env->SetByteArrayRegion(addr, 0, 4, v);
            return addr;
        } else {
            return NULL;
        }
    }
}

static void osNetworkSystem_setInetAddressImpl(JNIEnv* env, jobject obj,
        jobject sender, jbyteArray address) {
    // LOGD("ENTER setInetAddressImpl");

    env->SetObjectField(sender, gCachedFields.iaddr_ipaddress, address);
}

// TODO: rewrite this method in Java and make it support IPv6.
static jobject osNetworkSystem_inheritedChannelImpl(JNIEnv* env, jobject obj) {
    // LOGD("ENTER inheritedChannelImpl");

    int socket = 0;
    int opt;
    socklen_t length = sizeof(opt);
    int socket_type;
    struct sockaddr_in local_addr;
    struct sockaddr_in remote_addr;
    jclass channel_class, socketaddr_class, serverSocket_class, socketImpl_class;
    jobject channel_object = NULL, socketaddr_object, serverSocket_object;
    jobject fd_object, addr_object, localAddr_object, socketImpl_object;
    jfieldID port_field, socketaddr_field, bound_field, fd_field;
    jfieldID serverSocket_field, socketImpl_field, addr_field, localAddr_field;
    jmethodID channel_new;
    jbyteArray addr_array;
    struct sockaddr_in *sock;
    jbyte * address;
    jbyte * localAddr;
    jboolean jtrue = JNI_TRUE;

    if (0 != getsockopt(socket, SOL_SOCKET, SO_TYPE, &opt, &length)) {
        return NULL;
    }
    if (SOCK_STREAM !=opt && SOCK_DGRAM !=opt) {
        return NULL;
    }
    socket_type = opt;

    length  = sizeof(struct sockaddr);
    if (0 != getsockname(socket, (struct sockaddr *)&local_addr, &length)) {
        return NULL;
    } else {
        if (AF_INET != local_addr.sin_family || length != sizeof(struct sockaddr)) {
            return NULL;
        }
        localAddr = (jbyte*) malloc(sizeof(jbyte)*4);
        if (NULL == localAddr) {
            return NULL;
        }
        memcpy (localAddr, &(local_addr.sin_addr.s_addr), 4);
    }
    if (0 != getpeername(socket, (struct sockaddr *)&remote_addr, &length)) {
        remote_addr.sin_port = 0;
        remote_addr.sin_addr.s_addr = 0;
        address = (jbyte*) malloc(sizeof(jbyte)*4);
        bzero(address, sizeof(jbyte)*4);
    } else {
        if (AF_INET != remote_addr.sin_family
                || length != sizeof(struct sockaddr)) {
            return NULL;
        }
        address = (jbyte*) malloc(sizeof(jbyte)*4);
        memcpy (address, &(remote_addr.sin_addr.s_addr), 4);
    }

    // analysis end, begin pack to java
    if (SOCK_STREAM == opt) {
        if (remote_addr.sin_port!=0) {
            //socket
            channel_class = env->FindClass(
                    "org/apache/harmony/nio/internal/SocketChannelImpl");
            if (NULL == channel_class) {
                goto clean;
            }

            channel_new = env->GetMethodID(channel_class, "<init>", "()V");
            if (NULL == channel_new) {
                goto clean;
            }
            channel_object = env->NewObject(channel_class, channel_new);
            if (NULL == channel_object) {
                goto clean;
            }
            // new and set FileDescript

            fd_field = env->GetFieldID(channel_class, "fd",
                    "java/io/FielDescriptor");
            fd_object = env->GetObjectField(channel_object, fd_field);
            if (NULL == fd_object) {
                goto clean;
            }

            jniSetFileDescriptorOfFD(env, fd_object, socket);

            // local port
            port_field = env->GetFieldID(channel_class, "localPort", "I");
            env->SetIntField(channel_object, port_field,
                    ntohs(local_addr.sin_port));

            // new and set remote addr
            addr_array = env->NewByteArray((jsize)4);
            env->SetByteArrayRegion(addr_array, (jsize)0, (jsize)4, address);
            addr_object = env->NewObject(gCachedFields.i4addr_class,
                    gCachedFields.i4addr_class_init, addr_array);
            if (NULL == addr_object) {
                goto clean;
            }
            socketaddr_class = env->FindClass("java/net/InetSocketAddress");
            socketaddr_field = env->GetFieldID(channel_class, "connectAddress",
                    "Ljava/net/InetSocketAddress;");
            socketaddr_object = env->GetObjectField(channel_object,
                    socketaddr_field);
            if (NULL == socketaddr_object) {
                goto clean;
            }

            // localAddr
            socketaddr_class = env->FindClass("java/net/InetSocketAddress");
            socketaddr_field = env->GetFieldID(channel_class, "connectAddress",
                     "Ljava/net/InetSocketAddress;");
            socketaddr_object = env->GetObjectField(channel_object,
                     socketaddr_field);

            localAddr_field = env->GetFieldID(channel_class, "localAddress",
                     "Ljava/net/Inet4Address;");
            addr_array = env->NewByteArray((jsize)4);
            env->SetByteArrayRegion(addr_array, (jsize)0, (jsize)4, localAddr);
            localAddr_object = env->NewObject(gCachedFields.i4addr_class,
                     gCachedFields.i4addr_class_init, addr_array);
            jfieldID socketaddr_field = env->GetFieldID(channel_class,
                     "connectAddress", "Ljava/net/InetSocketAddress;");
            jobject socketaddr_object = env->GetObjectField(channel_object,
                     socketaddr_field);
            env->SetObjectField(socketaddr_object, localAddr_field,
                     localAddr_object);
            if (NULL == localAddr_object) {
                goto clean;
            }


            // set port
            port_field = env->GetFieldID(socketaddr_class, "port", "I");
            env->SetIntField(socketaddr_object, port_field,
                    ntohs(remote_addr.sin_port));

            // set bound
            if (0 != local_addr.sin_port) {
                bound_field = env->GetFieldID(channel_class, "isBound", "Z");
                env->SetBooleanField(channel_object, bound_field, jtrue);
            }

        } else {
            //serverSocket
            channel_class = env->FindClass(
                    "org/apache/harmony/nio/internal/ServerSocketChannelImpl");
            if (NULL == channel_class) {
                goto clean;
            }

            channel_new = env->GetMethodID(channel_class, "<init>", "()V");
            if (NULL == channel_new) {
                goto clean;
            }
            channel_object = env->NewObject(channel_class, channel_new);
            if (NULL == channel_object) {
                goto clean;
            }

            serverSocket_field = env->GetFieldID(channel_class, "socket",
                    "Ljava/net/ServerSocket;");
            serverSocket_class = env->FindClass("Ljava/net/ServerSocket;");
            serverSocket_object = env->GetObjectField(channel_object,
                    serverSocket_field);
            // set bound
            if (0 != local_addr.sin_port) {
                bound_field = env->GetFieldID(channel_class, "isBound", "Z");
                env->SetBooleanField(channel_object, bound_field, jtrue);
                bound_field = env->GetFieldID(serverSocket_class, "isBound", "Z");
                env->SetBooleanField(serverSocket_object, bound_field, jtrue);
            }
            // localAddr
            socketImpl_class = env->FindClass("java/net/SocketImpl");
            socketImpl_field = env->GetFieldID(channel_class, "impl",
                    "Ljava/net/SocketImpl;");
            socketImpl_object =  env->GetObjectField(channel_object,
                    socketImpl_field);
            if (NULL == socketImpl_object) {
                 goto clean;
            }

            addr_array = env->NewByteArray((jsize)4);
            localAddr_field = env->GetFieldID(channel_class, "localAddress",
                    "Ljava/net/InetAddress;");
            memset(address, 0, 4);
            env->SetByteArrayRegion(addr_array, (jsize)0, (jsize)4, address);
            localAddr_object = env->NewObject(gCachedFields.i4addr_class,
                    gCachedFields.i4addr_class_init, addr_array);
            if (NULL == localAddr_object) {
                 goto clean;
            }
            env->SetObjectField(socketImpl_object, localAddr_field,
                    localAddr_object);
            addr_array = env->NewByteArray((jsize)4);
            env->SetByteArrayRegion(addr_array, (jsize)0, (jsize)4, localAddr);
            env->SetObjectField(localAddr_object,
                    gCachedFields.iaddr_ipaddress, addr_array);

            // set port
            port_field = env->GetFieldID(socketImpl_class, "localport", "I");
            env->SetIntField(socketImpl_object, port_field,
                    ntohs(local_addr.sin_port));
        }
    } else {
        //Datagram Socket
        // new DatagramChannel
        channel_class = env->FindClass(
                "org/apache/harmony/nio/internal/DatagramChannelImpl");
        if (NULL == channel_class) {
            goto clean;
        }

        channel_new = env->GetMethodID(channel_class, "<init>", "()V");
        if (NULL == channel_new) {
            goto clean;
        }
        channel_object = env->NewObject(channel_class, channel_new);
        if (NULL == channel_object) {
            goto clean;
        }

        // new and set FileDescript
        fd_field = env->GetFieldID(channel_class, "fd", "java/io/FileDescriptor");
        fd_object = env->GetObjectField(channel_object, fd_field);
        if (NULL == fd_object) {
            goto clean;
        }

        jniSetFileDescriptorOfFD(env, fd_object, socket);

        port_field = env->GetFieldID(channel_class, "localPort", "I");
        env->SetIntField(channel_object, port_field, ntohs(local_addr.sin_port));

        // new and set remote addr
        addr_array = env->NewByteArray((jsize)4);
        env->SetByteArrayRegion(addr_array, (jsize)0, (jsize)4, address);
        addr_object = env->NewObject(gCachedFields.iaddr_class,
                gCachedFields.i4addr_class_init, addr_array);
        if (NULL == addr_object) {
            goto clean;
        }
        socketaddr_class = env->FindClass("java/net/InetSocketAddress");
        socketaddr_field = env->GetFieldID(channel_class, "connectAddress",
                "Ljava/net/InetSocketAddress;");
        socketaddr_object = env->GetObjectField(channel_object, socketaddr_field);
        if (NULL == socketaddr_object) {
            goto clean;
        }

        // set bound
        if (0 != local_addr.sin_port) {
            bound_field = env->GetFieldID(channel_class, "isBound", "Z");
            env->SetBooleanField(channel_object, bound_field, jtrue);
        }
    }
clean:
    free(address);
    free(localAddr);
    return channel_object;
}

/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "oneTimeInitializationImpl",         "(Z)V",                                                                     (void*) osNetworkSystem_oneTimeInitializationImpl          },
    { "createStreamSocketImpl",            "(Ljava/io/FileDescriptor;Z)V",                                             (void*) osNetworkSystem_createStreamSocketImpl             },
    { "createDatagramSocketImpl",          "(Ljava/io/FileDescriptor;Z)V",                                             (void*) osNetworkSystem_createDatagramSocketImpl           },
    { "readSocketImpl",                    "(Ljava/io/FileDescriptor;[BIII)I",                                         (void*) osNetworkSystem_readSocketImpl                     },
    { "readSocketDirectImpl",              "(Ljava/io/FileDescriptor;IIII)I",                                          (void*) osNetworkSystem_readSocketDirectImpl               },
    { "writeSocketImpl",                   "(Ljava/io/FileDescriptor;[BII)I",                                          (void*) osNetworkSystem_writeSocketImpl                    },
    { "writeSocketDirectImpl",             "(Ljava/io/FileDescriptor;III)I",                                           (void*) osNetworkSystem_writeSocketDirectImpl              },
    { "setNonBlockingImpl",                "(Ljava/io/FileDescriptor;Z)V",                                             (void*) osNetworkSystem_setNonBlockingImpl                 },
    { "connectSocketImpl",                 "(Ljava/io/FileDescriptor;ILjava/net/InetAddress;I)I",                      (void*) osNetworkSystem_connectSocketImpl                  },
    { "connectWithTimeoutSocketImpl",      "(Ljava/io/FileDescriptor;IILjava/net/InetAddress;II[B)I",                  (void*) osNetworkSystem_connectWithTimeoutSocketImpl       },
    { "connectStreamWithTimeoutSocketImpl","(Ljava/io/FileDescriptor;IIILjava/net/InetAddress;)V",                     (void*) osNetworkSystem_connectStreamWithTimeoutSocketImpl },
    { "socketBindImpl",                    "(Ljava/io/FileDescriptor;ILjava/net/InetAddress;)V",                       (void*) osNetworkSystem_socketBindImpl                     },
    { "listenStreamSocketImpl",            "(Ljava/io/FileDescriptor;I)V",                                             (void*) osNetworkSystem_listenStreamSocketImpl             },
    { "availableStreamImpl",               "(Ljava/io/FileDescriptor;)I",                                              (void*) osNetworkSystem_availableStreamImpl                },
    { "acceptSocketImpl",                  "(Ljava/io/FileDescriptor;Ljava/net/SocketImpl;Ljava/io/FileDescriptor;I)V",(void*) osNetworkSystem_acceptSocketImpl                   },
    { "supportsUrgentDataImpl",            "(Ljava/io/FileDescriptor;)Z",                                              (void*) osNetworkSystem_supportsUrgentDataImpl             },
    { "sendUrgentDataImpl",                "(Ljava/io/FileDescriptor;B)V",                                             (void*) osNetworkSystem_sendUrgentDataImpl                 },
    { "connectDatagramImpl2",              "(Ljava/io/FileDescriptor;IILjava/net/InetAddress;)V",                      (void*) osNetworkSystem_connectDatagramImpl2               },
    { "disconnectDatagramImpl",            "(Ljava/io/FileDescriptor;)V",                                              (void*) osNetworkSystem_disconnectDatagramImpl             },
    { "peekDatagramImpl",                  "(Ljava/io/FileDescriptor;Ljava/net/InetAddress;I)I",                       (void*) osNetworkSystem_peekDatagramImpl                   },
    { "receiveDatagramImpl",               "(Ljava/io/FileDescriptor;Ljava/net/DatagramPacket;[BIIIZ)I",               (void*) osNetworkSystem_receiveDatagramImpl                },
    { "receiveDatagramDirectImpl",         "(Ljava/io/FileDescriptor;Ljava/net/DatagramPacket;IIIIZ)I",                (void*) osNetworkSystem_receiveDatagramDirectImpl          },
    { "recvConnectedDatagramImpl",         "(Ljava/io/FileDescriptor;Ljava/net/DatagramPacket;[BIIIZ)I",               (void*) osNetworkSystem_recvConnectedDatagramImpl          },
    { "recvConnectedDatagramDirectImpl",   "(Ljava/io/FileDescriptor;Ljava/net/DatagramPacket;IIIIZ)I",                (void*) osNetworkSystem_recvConnectedDatagramDirectImpl    },
    { "sendDatagramImpl",                  "(Ljava/io/FileDescriptor;[BIIIZILjava/net/InetAddress;)I",                 (void*) osNetworkSystem_sendDatagramImpl                   },
    { "sendDatagramDirectImpl",            "(Ljava/io/FileDescriptor;IIIIZILjava/net/InetAddress;)I",                  (void*) osNetworkSystem_sendDatagramDirectImpl             },
    { "sendConnectedDatagramImpl",         "(Ljava/io/FileDescriptor;[BIIZ)I",                                         (void*) osNetworkSystem_sendConnectedDatagramImpl          },
    { "sendConnectedDatagramDirectImpl",   "(Ljava/io/FileDescriptor;IIIZ)I",                                          (void*) osNetworkSystem_sendConnectedDatagramDirectImpl    },
    { "createServerStreamSocketImpl",      "(Ljava/io/FileDescriptor;Z)V",                                             (void*) osNetworkSystem_createServerStreamSocketImpl       },
    { "receiveStreamImpl",                 "(Ljava/io/FileDescriptor;[BIII)I",                                         (void*) osNetworkSystem_receiveStreamImpl                  },
    { "sendStreamImpl",                    "(Ljava/io/FileDescriptor;[BII)I",                                          (void*) osNetworkSystem_sendStreamImpl                     },
    { "shutdownInputImpl",                 "(Ljava/io/FileDescriptor;)V",                                              (void*) osNetworkSystem_shutdownInputImpl                  },
    { "shutdownOutputImpl",                "(Ljava/io/FileDescriptor;)V",                                              (void*) osNetworkSystem_shutdownOutputImpl                 },
    { "sendDatagramImpl2",                 "(Ljava/io/FileDescriptor;[BIIILjava/net/InetAddress;)I",                   (void*) osNetworkSystem_sendDatagramImpl2                  },
    { "selectImpl",                        "([Ljava/io/FileDescriptor;[Ljava/io/FileDescriptor;II[IJ)I",               (void*) osNetworkSystem_selectImpl                         },
    { "getSocketLocalAddressImpl",         "(Ljava/io/FileDescriptor;Z)Ljava/net/InetAddress;",                        (void*) osNetworkSystem_getSocketLocalAddressImpl          },
    { "getSocketLocalPortImpl",            "(Ljava/io/FileDescriptor;Z)I",                                             (void*) osNetworkSystem_getSocketLocalPortImpl             },
    { "getSocketOptionImpl",               "(Ljava/io/FileDescriptor;I)Ljava/lang/Object;",                            (void*) osNetworkSystem_getSocketOptionImpl                },
    { "setSocketOptionImpl",               "(Ljava/io/FileDescriptor;ILjava/lang/Object;)V",                           (void*) osNetworkSystem_setSocketOptionImpl                },
    { "getSocketFlagsImpl",                "()I",                                                                      (void*) osNetworkSystem_getSocketFlagsImpl                 },
    { "socketCloseImpl",                   "(Ljava/io/FileDescriptor;)V",                                              (void*) osNetworkSystem_socketCloseImpl                    },
    { "getHostByAddrImpl",                 "([B)Ljava/net/InetAddress;",                                               (void*) osNetworkSystem_getHostByAddrImpl                  },
    { "getHostByNameImpl",                 "(Ljava/lang/String;Z)Ljava/net/InetAddress;",                              (void*) osNetworkSystem_getHostByNameImpl                  },
    { "setInetAddressImpl",                "(Ljava/net/InetAddress;[B)V",                                              (void*) osNetworkSystem_setInetAddressImpl                 },
    { "inheritedChannelImpl",              "()Ljava/nio/channels/Channel;",                                            (void*) osNetworkSystem_inheritedChannelImpl               },
};

int register_org_apache_harmony_luni_platform_OSNetworkSystem(JNIEnv* env) {
    return jniRegisterNativeMethods(env,
            "org/apache/harmony/luni/platform/OSNetworkSystem",
            gMethods,
            NELEM(gMethods));
}
