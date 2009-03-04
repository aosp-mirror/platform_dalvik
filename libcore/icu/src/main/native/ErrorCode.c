/**
*******************************************************************************
* Copyright (C) 1996-2005, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/

#include "ErrorCode.h"

/* private data members ----------------------------------------------------*/

/**
* Name of the java runtime exception classes
*/
#define ILLEGALARGUMENTEXCEPTION_       "java/lang/IllegalArgumentException"
#define ARRAYINDEXOUTOFBOUNDSEXCEPTION_ "java/lang/ArrayIndexOutOfBoundsException"
#define UNSUPPORTEDOPERATIONEXCEPTION_  "java/lang/UnsupportedOperationException"
#define RUNTIMEEXCEPTION_               "java/lang/RuntimeException"

/* public methods ---------------------------------------------------------*/

/**
* Checks if an error has occured. 
* Throws a generic Java RuntimeException if an error has occured.
* @param env JNI environment variable
* @param errorcode code to determine if it is an erro
* @return 0 if errorcode is not an error, 1 if errorcode is an error, but the 
*         creation of the exception to be thrown fails
* @exception thrown if errorcode represents an error
*/
UBool icu4jni_error(JNIEnv *env, UErrorCode errorcode)
{
  const char   *emsg      = u_errorName(errorcode);
        jclass  exception;

  if (errorcode > U_ZERO_ERROR && errorcode < U_ERROR_LIMIT) {
    switch (errorcode) {
      case U_ILLEGAL_ARGUMENT_ERROR :
        exception = (*env)->FindClass(env, ILLEGALARGUMENTEXCEPTION_);
        break;
      case U_INDEX_OUTOFBOUNDS_ERROR :
      case U_BUFFER_OVERFLOW_ERROR :
        exception = (*env)->FindClass(env, ARRAYINDEXOUTOFBOUNDSEXCEPTION_);
        break;
      case U_UNSUPPORTED_ERROR :
        exception = (*env)->FindClass(env, UNSUPPORTEDOPERATIONEXCEPTION_);
        break;
      default :
        exception = (*env)->FindClass(env, RUNTIMEEXCEPTION_);
    }

    return ((*env)->ThrowNew(env, exception, emsg) != 0);
  }
  return 0;
}
