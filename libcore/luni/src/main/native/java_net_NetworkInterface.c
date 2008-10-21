/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include "JNIHelp.h"
#include "jni.h"
#include "errno.h"

#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/socket.h>
#include <net/if.h>
#include <netinet/in.h>
#include <sys/ioctl.h>

//--------------------------------------------------------------------
// TODO copied from OSNetworkSystem. Might get into a separate .h file
/**
 * Throws an IOException with the given message.
 */
static void throwSocketException(JNIEnv *env, const char *message) {
    jclass exClass = (*env)->FindClass(env, "java/net/SocketException");

    if(exClass == NULL) {
        LOGE("Unable to find class java/net/SocketException");
    } else {
        (*env)->ThrowNew(env, exClass, message);
    }
}


/**
 * Throws a NullPointerException.
 */
static void throwNullPointerException(JNIEnv *env) {
    jclass exClass = (*env)->FindClass(env, "java/lang/NullPointerException");

    if(exClass == NULL) {
        LOGE("Unable to find class java/lang/NullPointerException");
    } else {
        (*env)->ThrowNew(env, exClass, NULL);
    }
}

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
#define SOCKERR_OPFAILED           -247
#define SOCKERR_VALUE_NULL         -248 /* The value indexed was NULL */
#define SOCKERR_CONNECTION_REFUSED -249 /* connection was refused */
#define SOCKERR_ENETUNREACH        -250 /* network is not reachable */
#define SOCKERR_EACCES             -251 /* permissions do not allow action on socket */

/**
 * Answer the errorString corresponding to the errorNumber, if available.
 * This function will answer a default error string, if the errorNumber is not
 * recognized.
 *
 * This function will have to be reworked to handle internationalization properly, removing
 * the explicit strings.
 *
 * @param anErrorNum    the error code to resolve to a human readable string
 *
 * @return  a human readable error string
 */

static char * netLookupErrorString(int anErrorNum) {
    switch(anErrorNum) {
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
            return "The call was cancelled";
        case SOCKERR_TIMEOUT:
            return "The operation timed out";
        case SOCKERR_CONNRESET:
            return "The connection was reset";
        case SOCKERR_WOULDBLOCK:
            return "The socket is marked as nonblocking operation would block";
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
        case SOCKERR_CONNECTION_REFUSED:
            return "Connection refused";

        default:
            return "unkown error";
    }
}

/**
 * Converts a native address structure to a 4-byte array. Throws a
 * NullPointerException or an IOException in case of error. This is
 * signaled by a return value of -1. The normal return value is 0.
 */
static int structInToJavaAddress(
        JNIEnv *env, struct in_addr *address, jbyteArray java_address) {

    if(java_address == NULL) {
        throwNullPointerException(env);
        return -1;
    }

    if((*env)->GetArrayLength(env, java_address) != sizeof(address->s_addr)) {
        jniThrowIOException(env, errno);
        return -1;
    }

    jbyte *java_address_bytes;

    java_address_bytes = (*env)->GetByteArrayElements(env, java_address, NULL);

    memcpy(java_address_bytes, &(address->s_addr), sizeof(address->s_addr));

    (*env)->ReleaseByteArrayElements(env, java_address, java_address_bytes, 0);

    return 0;
}

static jobject structInToInetAddress(JNIEnv *env, struct in_addr *address) {
    jbyteArray bytes;
    int success;

    bytes = (*env)->NewByteArray(env, 4);

    if(bytes == NULL) {
        return NULL;
    }

    success = structInToJavaAddress(env, address, bytes);

    if(success < 0) {
        return NULL;
    }

    jclass iaddrclass = (*env)->FindClass(env, "java/net/InetAddress");

    if(iaddrclass == NULL) {
        LOGE("Can't find java/net/InetAddress");
        jniThrowException(env, "java/lang/ClassNotFoundException", "java.net.InetAddress");
        return NULL;
    }

    jmethodID iaddrgetbyaddress = (*env)->GetStaticMethodID(env, iaddrclass, "getByAddress", "([B)Ljava/net/InetAddress;");

    if(iaddrgetbyaddress == NULL) {
        LOGE("Can't find method InetAddress.getByAddress(byte[] val)");
        jniThrowException(env, "java/lang/NoSuchMethodError", "InetAddress.getByAddress(byte[] val)");
        return NULL;
    }

    return (*env)->CallStaticObjectMethod(env, iaddrclass, iaddrgetbyaddress, bytes);
}
//--------------------------------------------------------------------






















/* structure for returning either and IPV4 or IPV6 ip address */
typedef struct ipAddress_struct {
    union {
        char bytes[sizeof(struct in_addr)];
        struct in_addr inAddr;
    } addr;
    unsigned int length;
    unsigned int  scope;
} ipAddress_struct;

/* structure for returning network interface information */
typedef struct NetworkInterface_struct {
    char *name;
    char *displayName;
    unsigned int  numberAddresses;
    unsigned int  index;
    struct ipAddress_struct *addresses;
} NetworkInterface_struct;

/* array of network interface structures */
typedef struct NetworkInterfaceArray_struct {
    unsigned int  length;
    struct NetworkInterface_struct *elements;
} NetworkInterfaceArray_struct;



























/**
 * Frees the memory allocated for the hyNetworkInterface_struct array passed in
 *
 * @param[in] portLibrary The port library.
 * @param[in] handle Pointer to array of network interface structures to be freed
 *
 * @return 0 on success
*/
int sock_free_network_interface_struct (struct NetworkInterfaceArray_struct *array) {
    unsigned int i = 0;

    if((array != NULL) && (array->elements != NULL)) {

        /* free the allocated memory in each of the structures */
        for(i = 0; i < array->length; i++) {

            /* free the name, displayName and addresses */
            if(array->elements[i].name != NULL) {
                free(array->elements[i].name);
            }

            if(array->elements[i].displayName != NULL) {
                free(array->elements[i].displayName);
            }

            if(array->elements[i].addresses != NULL) {
                free(array->elements[i].addresses);
            }
        }

        /* now free the array itself */
        free(array->elements);
    }

    return 0;
}





























/**
 * Queries and returns the information for the network interfaces that are currently active within the system.
 * Applications are responsible for freeing the memory returned via the handle.
 *
 * @param[in] portLibrary The port library.
 * @param[in,out] array Pointer to structure with array of network interface entries
 * @param[in] boolean which indicates if we should prefer the IPv4 stack or not
 *
 * @return The number of elements in handle on success, negatvie portable error code on failure.
                               -WSANO_RECOVERY if system calls required to get the info fail, -WSAENOBUFS if memory allocation fails
 * @note A return value of 0 indicates no interfaces exist
*/
int sockGetNetworkInterfaces(struct NetworkInterfaceArray_struct * array) {

    struct NetworkInterface_struct *interfaces = NULL;
    unsigned int nameLength = 0;
    unsigned int currentAdapterIndex = 0;
    unsigned int counter = 0;
    unsigned int result = 0;
    unsigned int numAddresses = 0;
    unsigned int currentIPAddressIndex = 0;
    unsigned int numAdapters = 0;
    int err = 0;

    struct ifconf ifc;
    int len = 32 * sizeof(struct ifreq);
    int socketP = 0;
    unsigned int totalInterfaces = 0;
    struct ifreq reqCopy;
    unsigned int counter2 = 0;
    char *lastName = NULL;

    int ifconfCommand = SIOCGIFCONF;

    /* this method is not guarranteed to return the IPV6 addresses.  Code is include so that if the platform returns IPV6 addresses
       in reply to the SIOCGIFCONF they will be included.  Howerver, it is not guarranteed or even expected that many platforms will
       include the IPV6 addresses.  For this reason there are other specific implementations that will return the IPV6 addresses */
    /* first get the list of interfaces.  We do not know how long the buffer needs to be so we try with one that allows for
       32 interfaces.  If this turns out not to be big enough then we expand the buffer to be able to support another
       32 interfaces and try again.  We do this until the result indicates that the result fit into the buffer provided */
    /* we need  socket to do the ioctl so create one */
    socketP = socket(PF_INET, SOCK_DGRAM, 0);
    if(socketP < 0) {
        return socketP;
    }
    for(;;) {
        char *data = (char *)malloc(len * sizeof(char));
        if(data == NULL) {
          close(socketP);
          return SOCKERR_NOBUFFERS;
        }
        ifc.ifc_len = len;
        ifc.ifc_buf = data;
        errno = 0;
        if(ioctl(socketP, ifconfCommand, &ifc) != 0) {
          err = errno;
          free(ifc.ifc_buf);
          close(socketP);
          return SOCKERR_NORECOVERY;
        }
        if(ifc.ifc_len < len)
        break;
        /* the returned data was likely truncated, expand the buffer and try again */
        free(ifc.ifc_buf);
        len += 32 * sizeof(struct ifreq);
    }

    /* get the number of distinct interfaces */
    if(ifc.ifc_len != 0) {
        totalInterfaces = ifc.ifc_len / sizeof(struct ifreq);
    }
    lastName = NULL;
    for(counter = 0; counter < totalInterfaces; counter++) {
        if((NULL == lastName) || (strncmp(lastName, ifc.ifc_req[counter].ifr_name, IFNAMSIZ) != 0)) {
            /* make sure the interface is up */
            reqCopy = ifc.ifc_req[counter];
            ioctl(socketP, SIOCGIFFLAGS, &reqCopy);
            if((reqCopy.ifr_flags) & (IFF_UP == IFF_UP)) {
                numAdapters++;
            }
        }
        lastName = ifc.ifc_req[counter].ifr_name;
    }

    /* now allocate the space for the hyNetworkInterface structs and fill it in */
    interfaces = malloc(numAdapters * sizeof(NetworkInterface_struct));
    if(NULL == interfaces) {
        free(ifc.ifc_buf);
        close(socketP);
        return SOCKERR_NOBUFFERS;
    }

    /* initialize the structure so that we can free allocated if a failure occurs */
    for(counter = 0; counter < numAdapters; counter++) {
        interfaces[counter].name = NULL;
        interfaces[counter].displayName = NULL;
        interfaces[counter].addresses = NULL;
    }

    /* set up the return stucture */
    array->elements = interfaces;
    array->length = numAdapters;
    lastName = NULL;
    for(counter = 0; counter < totalInterfaces; counter++) {
        /* make sure the interface is still up */
        reqCopy = ifc.ifc_req[counter];
        ioctl(socketP, SIOCGIFFLAGS, &reqCopy);
        if((reqCopy.ifr_flags) & (IFF_UP == IFF_UP)) {
            /* since this function can return multiple entries for the same name, only do it for the first one with any given name */
            if((NULL == lastName) || (strncmp(lastName, ifc.ifc_req[counter].ifr_name, IFNAMSIZ) != 0)) {

                /* get the index for the interface.  This is only truely necessary on platforms that support IPV6 */
                interfaces[currentAdapterIndex].index = 0;
                /* get the name and display name for the adapter */
                /* there only seems to be one name so use it for both the name and the display name */
                nameLength = strlen(ifc.ifc_req[counter].ifr_name);
                interfaces[currentAdapterIndex].name = malloc(nameLength + 1);

                if(NULL == interfaces[currentAdapterIndex].name) {
                    free(ifc.ifc_buf);
                    sock_free_network_interface_struct(array);
                    close(socketP);
                    return SOCKERR_NOBUFFERS;
                }
                strncpy(interfaces[currentAdapterIndex].name, ifc.ifc_req[counter].ifr_name, nameLength);
                interfaces[currentAdapterIndex].name[nameLength] = 0;
                nameLength = strlen(ifc.ifc_req[counter].ifr_name);
                interfaces[currentAdapterIndex].displayName = malloc(nameLength + 1);
                if(NULL == interfaces[currentAdapterIndex].displayName) {
                    free(ifc.ifc_buf);
                    sock_free_network_interface_struct(array);
                    close(socketP);
                    return SOCKERR_NOBUFFERS;
                }
                strncpy(interfaces[currentAdapterIndex].displayName, ifc.ifc_req[counter].ifr_name, nameLength);
                interfaces[currentAdapterIndex].displayName[nameLength] = 0;

                /* check how many addresses/aliases this adapter has.  aliases show up as adaptors with the same name */
                numAddresses = 0;
                for(counter2 = counter; counter2 < totalInterfaces; counter2++) {
                    if(strncmp(ifc.ifc_req[counter].ifr_name, ifc.ifc_req[counter2].ifr_name, IFNAMSIZ) == 0) {
                        if(ifc.ifc_req[counter2].ifr_addr.sa_family == AF_INET) {
                            numAddresses++;
                        }
                    } else {
                      break;
                    }
                }

                /* allocate space for the addresses */
                interfaces[currentAdapterIndex].numberAddresses = numAddresses;
                interfaces[currentAdapterIndex].addresses = malloc(numAddresses * sizeof(ipAddress_struct));
                if(NULL == interfaces[currentAdapterIndex].addresses) {
                    free(ifc.ifc_buf);
                    sock_free_network_interface_struct(array);
                    close(socketP);
                    return SOCKERR_NOBUFFERS;
                }

                /* now get the addresses */
                currentIPAddressIndex = 0;
                lastName = ifc.ifc_req[counter].ifr_name;

                for(;;) {
                    if(ifc.ifc_req[counter].ifr_addr.sa_family == AF_INET) {
                        interfaces[currentAdapterIndex].addresses[currentIPAddressIndex].addr.inAddr.s_addr = ((struct sockaddr_in *) (&ifc.ifc_req[counter].ifr_addr))->sin_addr.s_addr;
                        interfaces[currentAdapterIndex].addresses[currentIPAddressIndex].length = sizeof(struct in_addr);
                        interfaces[currentAdapterIndex].addresses[currentIPAddressIndex].scope = 0;
                        currentIPAddressIndex++;
                    }

                    /* we mean to increment the outside counter here as we want to skip the next entry as it is for the same interface
                                          as we are currently working on */
                    if((counter + 1 < totalInterfaces) && (strncmp(ifc.ifc_req[counter + 1].ifr_name, lastName, IFNAMSIZ) == 0)) {
                        counter++;
                    } else {
                        break;
                    }

                }
                currentAdapterIndex++;
            }
        }
    }          /* for over all interfaces */
    /* now an interface might have been taken down since we first counted them */
    array->length = currentAdapterIndex;
    /* free the memory now that we are done with it */
    free(ifc.ifc_buf);
    close(socketP);

    return 0;
}























































/**
 * Answer an array of NetworkInterface objects.  One for each network interface within the system
 *
 * @param      env     pointer to the JNI library
 * @param      clazz   the class of the object invoking the JNI function
 *
 * @return                     an array of NetworkInterface objects of length 0 or more
 */

static jobjectArray getNetworkInterfacesImpl(JNIEnv * env, jclass clazz) {

    /* variables to store network interfac edata returned by call to port library */
    struct NetworkInterfaceArray_struct networkInterfaceArray;
    int result = 0;

    /* variables for class and method objects needed to create bridge to java */
    jclass networkInterfaceClass = NULL;
    jclass inetAddressClass = NULL;
    jclass utilClass = NULL;
    jmethodID methodID = NULL;
    jmethodID utilMid = NULL;

    /* JNI objects used to return values from native call */
    jstring name = NULL;
    jstring displayName = NULL;
    jobjectArray addresses = NULL;
    jobjectArray networkInterfaces = NULL;
    jbyteArray bytearray = NULL;

    /* jobjects used to build the object arrays returned */
    jobject currentInterface = NULL;
    jobject element = NULL;

    /* misc variables needed for looping and determining inetAddress info */
    unsigned int i = 0;
    unsigned int j = 0;
    unsigned int nameLength = 0;

    /* get the classes and methods that we need for later calls */
    networkInterfaceClass = (*env)->FindClass(env, "java/net/NetworkInterface");
    if(networkInterfaceClass == NULL) {
        throwSocketException(env, netLookupErrorString(SOCKERR_NORECOVERY));
        return NULL;
    }

    inetAddressClass = (*env)->FindClass(env, "java/net/InetAddress");
    if(inetAddressClass == NULL) {
        throwSocketException(env, netLookupErrorString(SOCKERR_NORECOVERY));
        return NULL;
    }

    methodID = (*env)->GetMethodID(env, networkInterfaceClass, "<init>",
            "(Ljava/lang/String;Ljava/lang/String;[Ljava/net/InetAddress;I)V");
    if(methodID == NULL) {
        throwSocketException(env, netLookupErrorString(SOCKERR_NORECOVERY));
        return NULL;
    }

    utilClass = (*env)->FindClass(env, "org/apache/harmony/luni/util/Util");
    if(!utilClass) {
        return NULL;
    }

    utilMid = ((*env)->GetStaticMethodID(env, utilClass, "toString",
            "([BII)Ljava/lang/String;"));
    if(!utilMid) {
        return NULL;
    }

    result = sockGetNetworkInterfaces(&networkInterfaceArray);

    if(result < 0) {
        /* this means an error occured.  The value returned is the socket error that should be returned */
        throwSocketException(env, netLookupErrorString(result));
        return NULL;
    }

    /* now loop through the interfaces and extract the information to be returned */
    for(j = 0; j < networkInterfaceArray.length; j++) {
        /* set the name and display name and reset the addresses object array */
        addresses = NULL;
        name = NULL;
        displayName = NULL;

        if(networkInterfaceArray.elements[j].name != NULL) {
            nameLength = strlen(networkInterfaceArray.elements[j].name);
            bytearray = (*env)->NewByteArray(env, nameLength);
            if(bytearray == NULL) {
                /* NewByteArray should have thrown an exception */
                return NULL;
            }
            (*env)->SetByteArrayRegion(env, bytearray, (jint) 0, nameLength,
                    (jbyte *)networkInterfaceArray.elements[j].name);
            name = (*env)->CallStaticObjectMethod(env, utilClass, utilMid,
                    bytearray, (jint) 0, nameLength);
            if((*env)->ExceptionCheck(env)) {
                return NULL;
            }
        }

        if(networkInterfaceArray.elements[j].displayName != NULL) {
            nameLength = strlen(networkInterfaceArray.elements[j].displayName);
            bytearray = (*env)->NewByteArray(env, nameLength);
            if(bytearray == NULL) {
                /* NewByteArray should have thrown an exception */
                return NULL;
            }
            (*env)->SetByteArrayRegion(env, bytearray, (jint) 0, nameLength,
                    (jbyte *)networkInterfaceArray.elements[j].displayName);
            displayName = (*env)->CallStaticObjectMethod(env, utilClass, utilMid,
                    bytearray, (jint) 0, nameLength);
            if((*env)->ExceptionCheck(env)) {
                return NULL;
            }
        }

        /* generate the object with the inet addresses for the itnerface       */
        for(i = 0; i < networkInterfaceArray.elements[j].numberAddresses; i++) {
            element = structInToInetAddress(env, (struct in_addr *) &(networkInterfaceArray.elements[j].addresses[i].addr.inAddr));
            if(i == 0) {
                addresses = (*env)->NewObjectArray(env,
                        networkInterfaceArray.elements[j].numberAddresses,
                        inetAddressClass, element);
            } else {
                (*env)->SetObjectArrayElement(env, addresses, i, element);
            }
        }

        /* now  create the NetworkInterface object for this interface and then add it it ot the arrary that will be returned */
        currentInterface = (*env)->NewObject(env, networkInterfaceClass,
                methodID, name, displayName, addresses,
                networkInterfaceArray.elements[j].index);

        if(j == 0) {
            networkInterfaces = (*env)->NewObjectArray(env,
                    networkInterfaceArray.length, networkInterfaceClass,
                    currentInterface);
        } else {
            (*env)->SetObjectArrayElement(env, networkInterfaces, j, currentInterface);
        }
    }

    /* free the memory for the interfaces struct and return the new NetworkInterface List */
    sock_free_network_interface_struct(&networkInterfaceArray);
    return networkInterfaces;
}


/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getNetworkInterfacesImpl", "()[Ljava/net/NetworkInterface;", getNetworkInterfacesImpl }
};
int register_java_net_NetworkInterface(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "java/net/NetworkInterface",
        gMethods, NELEM(gMethods));

}
