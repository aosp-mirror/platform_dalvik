/**
*******************************************************************************
* Copyright (C) 1996-2005, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/

#include "ErrorCode.h"
#include "JNIHelp.h"

/**
* Checks if an error has occurred, throwing a suitable exception if so.
* @param env JNI environment
* @param errorCode code to determine if it is an error
* @return 0 if errorCode is not an error, 1 if errorCode is an error, but the
*         creation of the exception to be thrown fails
 * @exception thrown if errorCode represents an error
*/
UBool icu4jni_error(JNIEnv *env, UErrorCode errorCode)
{
    const char* message = u_errorName(errorCode);
    if (errorCode <= U_ZERO_ERROR || errorCode >= U_ERROR_LIMIT) {
        return 0;
    }
    
    switch (errorCode) {
    case U_ILLEGAL_ARGUMENT_ERROR:
        return jniThrowException(env, "java/lang/IllegalArgumentException", message);
    case U_INDEX_OUTOFBOUNDS_ERROR:
    case U_BUFFER_OVERFLOW_ERROR:
        return jniThrowException(env, "java/lang/ArrayIndexOutOfBoundsException", message);
    case U_UNSUPPORTED_ERROR:
        return jniThrowException(env, "java/lang/UnsupportedOperationException", message);
    default:
        return jniThrowException(env, "java/lang/RuntimeException", message);
    }
}
