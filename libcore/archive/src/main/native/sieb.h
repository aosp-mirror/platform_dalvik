#if !defined(sieb_h)
#define sieb_h


#include "JNIHelp.h"
#include "jni.h"



void throwNewOutOfMemoryError (JNIEnv * env, const char *message);
void throwNewIllegalArgumentException (JNIEnv * env, const char *message);
void throwNewIllegalStateException (JNIEnv * env, const char *message);


void * sieb_malloc (JNIEnv * env, size_t byteCnt);
void sieb_free (JNIEnv * env, void * adr);

void sieb_convertToPlatform (char *path);



#endif /* sieb_h */
