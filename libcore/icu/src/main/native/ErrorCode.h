/**
*******************************************************************************
* Copyright (C) 1996-2005, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*******************************************************************************
*/

#ifndef ERRORCODE_H
#define ERRORCODE_H

#include <jni.h>
#include "unicode/utypes.h"
#include "unicode/putil.h"

/**
* Checks if an error has occured. 
* Throws a generic Java RuntimeException if an error has occured.
* @param env JNI environment variable
* @param errorcode code to determine if it is an erro
* @return 0 if errorcode is not an error, 1 if errorcode is an error, but the 
*         creation of the exception to be thrown fails
* @exception thrown if errorcode represents an error
*/
UBool icu4jni_error(JNIEnv *env, UErrorCode errorcode);

#endif
