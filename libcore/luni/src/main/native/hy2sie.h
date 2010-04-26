#if !defined(hy2sie_h)
#define hy2sie_h


#include "JNIHelp.h"
#include "jni.h"
#include "sieb.h"


typedef int BOOLEAN;
#define TRUE 1
#define FALSE 0


// mc: Stuff adopted from hyport.h:

/** HyMaxPath was chosen from unix MAXPATHLEN.  Override in platform
  * specific hyfile implementations if needed.
  */
#define HyMaxPath   1024



// Following definitions from hycomp.h:

/**
 * Define common types:
 * <ul>
 * <li><code>U_32 / I_32</code>  - unsigned/signed 32 bits</li>
 * <li><code>U_16 / I_16</code>  - unsigned/signed 16 bits</li>
 * <li><code>U_8 / I_8</code>    - unsigned/signed 8 bits (bytes -- not to be
 *                                 confused with char)</li>
 * </ul>
 */

typedef int I_32;
typedef short I_16;
typedef signed char I_8; /* chars can be unsigned */
typedef unsigned int U_32;
typedef unsigned short U_16;
typedef unsigned char U_8;

typedef long long I_64;
typedef unsigned long long U_64;

/**
 * Define platform specific types:
 * <ul>
 * <li><code>UDATA</code>        - unsigned data, can be used as an integer or
 *                                 pointer storage</li>
 * <li><code>IDATA</code>        - signed data, can be used as an integer or
 *                                 pointer storage</li>
 * </ul>
 */
/* FIXME: POINTER64 */

typedef I_32 IDATA;
typedef U_32 UDATA;


// Further required definitions from Harmony:

#define HYCONST64(x) x##L


#define HY_CFUNC
#define HY_CDATA
#define PROTOTYPE(x) x
#define VMCALL
#define PVMCALL *
#define NORETURN

#define GLOBAL_DATA(symbol) ((void*)&(symbol))


// Following definitions substitute the HyPortLibrary simply with the JNIEnv

typedef JNIEnv HyPortLibrary;

#define PORT_ACCESS_FROM_ENV(env) HyPortLibrary *privatePortLibrary = env
#define PORT_ACCESS_FROM_PORT(portLibrary) HyPortLibrary *privatePortLibrary = portLibrary
#define PORTLIB privatePortLibrary


// Following defintion is used to avoide quite a few signedness warnings:
#define mcSignednessBull void *


// Following the substitution of hyfile:

#include <fcntl.h>

#define HyOpenRead    O_RDONLY
#define hyfile_open(a, b, c) open(a, b, c)

#define HySeekEnd SEEK_END
#define HySeekSet SEEK_SET
#define HySeekCur SEEK_CUR
#define hyfile_seek(a, b, c) lseek(a, b, c)

#define hyfile_read(a, b, c) read(a, b, c)

#define hyfile_close(a) close(a)


// And further substitutions:

#define hymem_allocate_memory(byteCount) sieb_malloc(privatePortLibrary, byteCount)
#define hymem_free_memory(pointer) sieb_free(privatePortLibrary, pointer)

#define ioh_convertToPlatform(path) sieb_convertToPlatform (path)


#endif /* hy2sie_h */
