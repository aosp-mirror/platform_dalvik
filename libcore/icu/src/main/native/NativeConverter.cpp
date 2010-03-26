/**
*******************************************************************************
* Copyright (C) 1996-2006, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*
*******************************************************************************
*/
/*
 *  @(#) icujniinterface.c	1.2 00/10/11
 *
 * (C) Copyright IBM Corp. 2000 - All Rights Reserved
 *  A JNI wrapper to ICU native converter Interface
 * @author: Ram Viswanadha
 */

#include "JNIHelp.h"
#include "AndroidSystemNatives.h"
#include "ScopedUtfChars.h"
#include "unicode/utypes.h"   /* Basic ICU data types */
#include "unicode/ucnv.h"     /* C   Converter API    */
#include "unicode/ustring.h"  /* some more string functions*/
#include "unicode/ucnv_cb.h"  /* for callback functions */
#include "unicode/uset.h"     /* for contains function */
#include "ErrorCode.h"
#include <stdlib.h>
#include <string.h>

#define com_ibm_icu4jni_converters_NativeConverter_STOP_CALLBACK 0L
#define com_ibm_icu4jni_converters_NativeConverter_SKIP_CALLBACK 1L
#define com_ibm_icu4jni_converters_NativeConverter_SUBSTITUTE_CALLBACK 2L

/* Prototype of callback for substituting user settable sub chars */
static void JNI_TO_U_CALLBACK_SUBSTITUTE
        (const void *,UConverterToUnicodeArgs *,const char* ,int32_t ,UConverterCallbackReason ,UErrorCode * );

static jlong openConverter(JNIEnv* env, jclass, jstring converterName) {
    ScopedUtfChars converterNameChars(env, converterName);
    if (!converterNameChars.data()) {
        return 0;
    }
    UErrorCode errorCode = U_ZERO_ERROR;
    UConverter* conv = ucnv_open(converterNameChars.data(), &errorCode);
    icu4jni_error(env, errorCode);
    return (jlong) conv;
}

static void closeConverter(JNIEnv* env, jclass, jlong handle) {
    UConverter* cnv = (UConverter*)(long)handle;
    if (cnv) {
        // BEGIN android-added
        // Free up any contexts created in setCallback[Encode|Decode]()
        UConverterToUCallback toAction;
        UConverterFromUCallback fromAction;
        void* context1 = NULL;
        void* context2 = NULL;
        // TODO: ICU API bug?
        // The documentation clearly states that the caller owns the returned
        // pointers: http://icu-project.org/apiref/icu4c/ucnv_8h.html
        ucnv_getToUCallBack(cnv, &toAction, const_cast<const void**>(&context1));
        ucnv_getFromUCallBack(cnv, &fromAction, const_cast<const void**>(&context2));
        // END android-added
        ucnv_close(cnv);
        // BEGIN android-added
        if (context1 != NULL) {
            free(context1);
        }
        if (context2 != NULL) {
            free(context2);
        }
        // END android-added
    }
}

/**
 * Sets the substution mode for from Unicode conversion. Currently only 
 * two modes are supported: substitute or report
 * @param env environment handle for JNI 
 * @param jClass handle for the class
 * @param handle address of ICU converter
 * @param mode the mode to set 
 */
static jint setSubstitutionModeCharToByte (JNIEnv *env, jclass, jlong handle, jboolean mode) {
    
    UConverter* conv = (UConverter*)(long)handle;
    UErrorCode errorCode =U_ZERO_ERROR;

    if(conv) {
        
        UConverterFromUCallback fromUOldAction ;
        void* fromUOldContext;
        void* fromUNewContext=NULL;
        if(mode) {

            ucnv_setFromUCallBack(conv,
               UCNV_FROM_U_CALLBACK_SUBSTITUTE,
               fromUNewContext,
               &fromUOldAction,
               (const void**)&fromUOldContext,
               &errorCode);

        }
        else{

            ucnv_setFromUCallBack(conv,
               UCNV_FROM_U_CALLBACK_STOP,
               fromUNewContext,
               &fromUOldAction,
               (const void**)&fromUOldContext,
               &errorCode);
         
        }
        return errorCode;
    }
    errorCode = U_ILLEGAL_ARGUMENT_ERROR;
    return errorCode;
}
/**
 * Sets the substution mode for to Unicode conversion. Currently only 
 * two modes are supported: substitute or report
 * @param env environment handle for JNI 
 * @param jClass handle for the class
 * @param handle address of ICU converter
 * @param mode the mode to set 
 */
static jint setSubstitutionModeByteToChar (JNIEnv *env, jclass, jlong handle, jboolean mode) {
    
    UConverter* conv = (UConverter*)handle;
    UErrorCode errorCode =U_ZERO_ERROR;

    if(conv) {
        
        UConverterToUCallback toUOldAction ;
        void* toUOldContext;
        void* toUNewContext=NULL;
        if(mode) {

            ucnv_setToUCallBack(conv,
               UCNV_TO_U_CALLBACK_SUBSTITUTE,
               toUNewContext,
               &toUOldAction,
               (const void**)&toUOldContext,
               &errorCode);

        }
        else{

            ucnv_setToUCallBack(conv,
               UCNV_TO_U_CALLBACK_STOP,
               toUNewContext,
               &toUOldAction,
               (const void**)&toUOldContext,
               &errorCode);
         
        }
        return errorCode;
    }
    errorCode = U_ILLEGAL_ARGUMENT_ERROR;
    return errorCode;
}
/**
 * Converts a buffer of Unicode code units to target encoding 
 * @param env environment handle for JNI 
 * @param jClass handle for the class
 * @param handle address of ICU converter
 * @param source buffer of Unicode chars to convert 
 * @param sourceEnd limit of the source buffer
 * @param target buffer to recieve the converted bytes
 * @param targetEnd the limit of the target buffer
 * @param data buffer to recieve state of the current conversion
 * @param flush boolean that specifies end of source input
 */
static jint convertCharToByte(JNIEnv *env, jclass, jlong handle,  jcharArray source,  jint sourceEnd, jbyteArray target, jint targetEnd, jintArray data, jboolean flush) {

    UErrorCode errorCode =U_ZERO_ERROR;
    UConverter* cnv = (UConverter*)handle;
    if(cnv) {
        jint* myData = (jint*) env->GetPrimitiveArrayCritical(data,NULL);
        if(myData) {
            jint* sourceOffset = &myData[0];
            jint* targetOffset = &myData[1];
            const jchar* uSource =(jchar*) env->GetPrimitiveArrayCritical(source, NULL);
            if(uSource) {
                jbyte* uTarget=(jbyte*) env->GetPrimitiveArrayCritical(target,NULL);
                if(uTarget) {
                    const jchar* mySource = uSource+ *sourceOffset;
                    const UChar* mySourceLimit= uSource+sourceEnd;
                    char* cTarget = reinterpret_cast<char*>(uTarget+ *targetOffset);
                    const char* cTargetLimit = reinterpret_cast<const char*>(uTarget+targetEnd);
                    
                    ucnv_fromUnicode( cnv , &cTarget, cTargetLimit,&mySource,
                                    mySourceLimit,NULL,(UBool) flush, &errorCode);

                    *sourceOffset = (jint) (mySource - uSource)-*sourceOffset;
                    *targetOffset = (jint) ((jbyte*)cTarget - uTarget)- *targetOffset;
                    if(U_FAILURE(errorCode)) {
                        env->ReleasePrimitiveArrayCritical(target,uTarget,0);
                        env->ReleasePrimitiveArrayCritical(source,(jchar*)uSource,0);
                        env->ReleasePrimitiveArrayCritical(data,(jint*)myData,0);
                        return errorCode;
                    }
                }else{
                    errorCode = U_ILLEGAL_ARGUMENT_ERROR;
                }
                env->ReleasePrimitiveArrayCritical(target,uTarget,0);
            }else{
                errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            }
            env->ReleasePrimitiveArrayCritical(source,(jchar*)uSource,0);
        }else{
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        }
        env->ReleasePrimitiveArrayCritical(data,(jint*)myData,0);
        return errorCode;
    }
    errorCode = U_ILLEGAL_ARGUMENT_ERROR;
    return errorCode;
}

static jint encode(JNIEnv *env, jclass, jlong handle, jcharArray source, jint sourceEnd, jbyteArray target, jint targetEnd, jintArray data, jboolean flush) {
   
    UErrorCode ec = UErrorCode(convertCharToByte(env, NULL,handle,source,sourceEnd, target,targetEnd,data,flush));
    UConverter* cnv = (UConverter*)handle;
    jint* myData = (jint*) env->GetPrimitiveArrayCritical(data,NULL);

    if(cnv && myData) {
        
       UErrorCode errorCode = U_ZERO_ERROR;
       myData[3] = ucnv_fromUCountPending(cnv, &errorCode);

       if(ec == U_ILLEGAL_CHAR_FOUND || ec == U_INVALID_CHAR_FOUND) {
            int8_t count =32;
            UChar invalidUChars[32];
            ucnv_getInvalidUChars(cnv,invalidUChars,&count,&errorCode);

            if(U_SUCCESS(errorCode)) {
                myData[2] = count;
            }
        }
    }
    env->ReleasePrimitiveArrayCritical(data,(jint*)myData,0);
    return ec;
}

/**
 * Converts a buffer of encoded bytes to Unicode code units
 * @param env environment handle for JNI 
 * @param jClass handle for the class
 * @param handle address of ICU converter
 * @param source buffer of Unicode chars to convert 
 * @param sourceEnd limit of the source buffer
 * @param target buffer to recieve the converted bytes
 * @param targetEnd the limit of the target buffer
 * @param data buffer to recieve state of the current conversion
 * @param flush boolean that specifies end of source input
 */
static jint convertByteToChar(JNIEnv *env, jclass, jlong handle, jbyteArray source, jint sourceEnd, jcharArray target, jint targetEnd, jintArray data, jboolean flush) {

    UErrorCode errorCode =U_ZERO_ERROR;
    UConverter* cnv = (UConverter*)handle;
    if(cnv) {
        jint* myData = (jint*) env->GetPrimitiveArrayCritical(data,NULL);
        if(myData) {
            jint* sourceOffset = &myData[0];
            jint* targetOffset = &myData[1];

            const jbyte* uSource =(jbyte*) env->GetPrimitiveArrayCritical(source, NULL);
            if(uSource) {
                jchar* uTarget=(jchar*) env->GetPrimitiveArrayCritical(target,NULL);
                if(uTarget) {
                    const jbyte* mySource = uSource+ *sourceOffset;
                    const char* mySourceLimit = reinterpret_cast<const char*>(uSource+sourceEnd);
                    UChar* cTarget=uTarget+ *targetOffset;
                    const UChar* cTargetLimit=uTarget+targetEnd;
                    
                    ucnv_toUnicode( cnv , &cTarget, cTargetLimit,(const char**)&mySource,
                                   mySourceLimit,NULL,(UBool) flush, &errorCode);
                
                    *sourceOffset = mySource - uSource - *sourceOffset  ;
                    *targetOffset = cTarget - uTarget - *targetOffset;
                    if(U_FAILURE(errorCode)) {
                        env->ReleasePrimitiveArrayCritical(target,uTarget,0);
                        env->ReleasePrimitiveArrayCritical(source,(jchar*)uSource,0);
                        env->ReleasePrimitiveArrayCritical(data,(jint*)myData,0);
                        return errorCode;
                    }
                }else{
                    errorCode = U_ILLEGAL_ARGUMENT_ERROR;
                }
                env->ReleasePrimitiveArrayCritical(target,uTarget,0);
            }else{
                errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            }
            env->ReleasePrimitiveArrayCritical(source,(jchar*)uSource,0);
        }else{
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        }
        env->ReleasePrimitiveArrayCritical(data,(jint*)myData,0);
        return errorCode;
    }
    errorCode = U_ILLEGAL_ARGUMENT_ERROR;
    return errorCode;
}

static jint decode(JNIEnv *env, jclass, jlong handle, jbyteArray source, jint sourceEnd, jcharArray target, jint targetEnd, jintArray data, jboolean flush) {

    jint ec = convertByteToChar(env, NULL,handle,source,sourceEnd, target,targetEnd,data,flush);

    jint* myData = (jint*) env->GetPrimitiveArrayCritical(data,NULL);
    UConverter* cnv = (UConverter*)handle;

    if(myData && cnv) {
        UErrorCode errorCode = U_ZERO_ERROR;
        myData[3] = ucnv_toUCountPending(cnv, &errorCode);

        if(ec == U_ILLEGAL_CHAR_FOUND || ec == U_INVALID_CHAR_FOUND ) {
            char invalidChars[32] = {'\0'};
            int8_t len = 32;
            ucnv_getInvalidChars(cnv,invalidChars,&len,&errorCode);
            
            if(U_SUCCESS(errorCode)) {
                myData[2] = len;
            }	  
        }
    }
    env->ReleasePrimitiveArrayCritical(data,(jint*)myData,0);
    return ec;
}

static void resetByteToChar(JNIEnv* env, jclass, jlong handle) {
    UConverter* cnv = (UConverter*)handle;
    if (cnv) {
        ucnv_resetToUnicode(cnv);
    }
}

static void resetCharToByte(JNIEnv* env, jclass, jlong handle) {
    UConverter* cnv = (UConverter*)handle;
    if (cnv) {
        ucnv_resetFromUnicode(cnv);
    }
}

static jint countInvalidBytes (JNIEnv *env, jclass, jlong handle, jintArray length) {
    UConverter* cnv = (UConverter*)handle;
    if (!cnv) {
        return U_ILLEGAL_ARGUMENT_ERROR;
    }

    UErrorCode errorCode = U_ZERO_ERROR;
    jint* len = (jint*) env->GetPrimitiveArrayCritical(length, NULL);
    if (len) {
        char invalidChars[32];
        ucnv_getInvalidChars(cnv,invalidChars,(int8_t*)len,&errorCode);
    }
    env->ReleasePrimitiveArrayCritical(length,(jint*)len,0);
    return errorCode;
}

static jint countInvalidChars(JNIEnv *env, jclass, jlong handle, jintArray length) {
    UConverter* cnv = (UConverter*)handle;
    if (!cnv) {
        return U_ILLEGAL_ARGUMENT_ERROR;
    }

    UErrorCode errorCode =U_ZERO_ERROR;
    jint* len = (jint*) env->GetPrimitiveArrayCritical(length, NULL);
    if (len) {
        UChar invalidUChars[32];
        ucnv_getInvalidUChars(cnv,invalidUChars,(int8_t*)len,&errorCode);
    }
    env->ReleasePrimitiveArrayCritical(length,(jint*)len,0);
    return errorCode;
}

static jint getMaxBytesPerChar(JNIEnv *env, jclass, jlong handle) {
    UConverter* cnv = (UConverter*)handle;
    return (cnv != NULL) ? ucnv_getMaxCharSize(cnv) : -1;
}

static jint getMinBytesPerChar(JNIEnv *env, jclass, jlong handle) {
    UConverter* cnv = (UConverter*)handle;
    return (cnv != NULL) ? ucnv_getMinCharSize(cnv) : -1;
}

static jfloat getAveBytesPerChar(JNIEnv *env, jclass, jlong handle) {
    UConverter* cnv = (UConverter*)handle;
    if (cnv) {
         jfloat max = (jfloat)ucnv_getMaxCharSize(cnv);
         jfloat min = (jfloat)ucnv_getMinCharSize(cnv);
         return (jfloat) ( (max+min)/2 );
    }
    return -1;
}

static jint flushByteToChar(JNIEnv *env, jclass,jlong handle, jcharArray target, jint targetEnd, jintArray data) {

    UErrorCode errorCode =U_ZERO_ERROR;
    UConverter* cnv = (UConverter*)handle;
    if(cnv) {
        jbyte source ='\0';
        jint* myData = (jint*) env->GetPrimitiveArrayCritical(data,NULL);
        if(myData) {
            jint* targetOffset = &myData[1];
            jchar* uTarget=(jchar*) env->GetPrimitiveArrayCritical(target,NULL);
            if(uTarget) {
                const jbyte* mySource = &source;
                const char* mySourceLimit = reinterpret_cast<char*>(&source);
                UChar* cTarget=uTarget+ *targetOffset;
                const UChar* cTargetLimit=uTarget+targetEnd;

                ucnv_toUnicode( cnv , &cTarget, cTargetLimit,(const char**)&mySource,
                               mySourceLimit,NULL,TRUE, &errorCode);


                *targetOffset = (jint) ((jchar*)cTarget - uTarget)- *targetOffset;
                if(U_FAILURE(errorCode)) {
                    env->ReleasePrimitiveArrayCritical(target,uTarget,0);
                    env->ReleasePrimitiveArrayCritical(data,(jint*)myData,0);
                    return errorCode;
                }
            }else{
                errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            }
            env->ReleasePrimitiveArrayCritical(target,uTarget,0);

        }else{
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        }
        env->ReleasePrimitiveArrayCritical(data,(jint*)myData,0);
        return errorCode;
    }
    errorCode = U_ILLEGAL_ARGUMENT_ERROR;
    return errorCode;
}

static jint flushCharToByte (JNIEnv *env, jclass, jlong handle, jbyteArray target, jint targetEnd, jintArray data) {
          
    UErrorCode errorCode =U_ZERO_ERROR;
    UConverter* cnv = (UConverter*)handle;
    jchar source = '\0';
    if(cnv) {
        jint* myData = (jint*) env->GetPrimitiveArrayCritical(data,NULL);
        if(myData) {
            jint* targetOffset = &myData[1];
            jbyte* uTarget=(jbyte*) env->GetPrimitiveArrayCritical(target,NULL);
            if(uTarget) {
                const jchar* mySource = &source;
                const UChar* mySourceLimit= &source;
                char* cTarget = reinterpret_cast<char*>(uTarget+ *targetOffset);
                const char* cTargetLimit = reinterpret_cast<char*>(uTarget+targetEnd);

                ucnv_fromUnicode( cnv , &cTarget, cTargetLimit,&mySource,
                                  mySourceLimit,NULL,TRUE, &errorCode);
            

                *targetOffset = (jint) ((jbyte*)cTarget - uTarget)- *targetOffset;
                if(U_FAILURE(errorCode)) {
                    env->ReleasePrimitiveArrayCritical(target,uTarget,0);
                
                    env->ReleasePrimitiveArrayCritical(data,(jint*)myData,0);
                    return errorCode;
                }
            }else{
                errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            }
            env->ReleasePrimitiveArrayCritical(target,uTarget,0);
        }else{
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        }
        env->ReleasePrimitiveArrayCritical(data,(jint*)myData,0);
        return errorCode;
    }
    errorCode = U_ILLEGAL_ARGUMENT_ERROR;
    return errorCode;
}

static void toChars(const UChar* us, char* cs, int32_t length) {
    UChar u;
    while(length>0) {
        u=*us++;
        *cs++=(char)u;
        --length;
    }
}
static jint setSubstitutionBytes(JNIEnv *env, jclass, jlong handle, jbyteArray subChars, jint length) {

    UConverter* cnv = (UConverter*) handle;
    UErrorCode errorCode = U_ZERO_ERROR;
    if(cnv) {
        jbyte* u_subChars = reinterpret_cast<jbyte*>(env->GetPrimitiveArrayCritical(subChars, NULL));
        if(u_subChars) {
            char* mySubChars = new char[length];
             toChars((UChar*)u_subChars,&mySubChars[0],length);
             ucnv_setSubstChars(cnv,mySubChars, (char)length,&errorCode);
             if(U_FAILURE(errorCode)) {
                env->ReleasePrimitiveArrayCritical(subChars,mySubChars,0);
                return errorCode;
             }
             delete[] mySubChars;
        }
        else{   
           errorCode =  U_ILLEGAL_ARGUMENT_ERROR;
        }
        env->ReleasePrimitiveArrayCritical(subChars,u_subChars,0);
        return errorCode;
    }
    errorCode = U_ILLEGAL_ARGUMENT_ERROR;
    return errorCode;
}


#define VALUE_STRING_LENGTH 32

struct SubCharStruct {
    int length;
    UChar subChars[256];
    UBool stopOnIllegal;
};


static UErrorCode 
setToUCallbackSubs(UConverter* cnv,UChar* subChars, int32_t length,UBool stopOnIllegal ) {
    SubCharStruct* substitutionCharS = (SubCharStruct*) malloc(sizeof(SubCharStruct));
    UErrorCode errorCode = U_ZERO_ERROR;
    if(substitutionCharS) {
       UConverterToUCallback toUOldAction;
       void* toUOldContext=NULL;
       void* toUNewContext=NULL ;
       if(subChars) {
            u_strncpy(substitutionCharS->subChars,subChars,length);
       }else{
           substitutionCharS->subChars[length++] =0xFFFD;
       }
       substitutionCharS->subChars[length]=0;
       substitutionCharS->length = length;
       substitutionCharS->stopOnIllegal = stopOnIllegal;
       toUNewContext = substitutionCharS;

       ucnv_setToUCallBack(cnv,
           JNI_TO_U_CALLBACK_SUBSTITUTE,
           toUNewContext,
           &toUOldAction,
           (const void**)&toUOldContext,
           &errorCode);

       if(toUOldContext) {
           SubCharStruct* temp = (SubCharStruct*) toUOldContext;
           free(temp);
       }

       return errorCode;
    }
    return U_MEMORY_ALLOCATION_ERROR;
}
static jint setSubstitutionChars(JNIEnv *env, jclass, jlong handle, jcharArray subChars, jint length) {

    UErrorCode errorCode = U_ZERO_ERROR;
    UConverter* cnv = (UConverter*) handle;
    jchar* u_subChars=NULL;
    if(cnv) {
        if(subChars) {
            int len = env->GetArrayLength(subChars);
            u_subChars = reinterpret_cast<jchar*>(env->GetPrimitiveArrayCritical(subChars,NULL));
            if(u_subChars) {
               errorCode =  setToUCallbackSubs(cnv,u_subChars,len,FALSE);
            }else{
                errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            }
            env->ReleasePrimitiveArrayCritical(subChars,u_subChars,0);
            return errorCode;
        }
    }
    return U_ILLEGAL_ARGUMENT_ERROR;
}


static void JNI_TO_U_CALLBACK_SUBSTITUTE( const void *context, UConverterToUnicodeArgs *toArgs, const char* codeUnits, int32_t length, UConverterCallbackReason reason, UErrorCode * err) {

    if(context) {
        SubCharStruct* temp = (SubCharStruct*)context;
        if( temp) {
            if(temp->stopOnIllegal==FALSE) {
                if (reason > UCNV_IRREGULAR) {
                    return;
                }
                /* reset the error */
                *err = U_ZERO_ERROR;
                ucnv_cbToUWriteUChars(toArgs,temp->subChars ,temp->length , 0, err);
            }else{
                if(reason != UCNV_UNASSIGNED) {
                    /* the caller must have set 
                     * the error code accordingly
                     */
                    return;
                }else{
                    *err = U_ZERO_ERROR;
                    ucnv_cbToUWriteUChars(toArgs,temp->subChars ,temp->length , 0, err);
                    return;
                }
            }
        }
    }
    return;
}

static jboolean canEncode(JNIEnv *env, jclass, jlong handle, jint codeUnit) {
    
    UErrorCode errorCode =U_ZERO_ERROR;
    UConverter* cnv = (UConverter*)handle;
    if(cnv) {
        UChar source[3];
        UChar *mySource=source;
        const UChar* sourceLimit = (codeUnit<0x010000) ? &source[1] : &source[2];
        char target[5];
        char *myTarget = target;
        const char* targetLimit = &target[4];
        int i=0;
        UTF_APPEND_CHAR(&source[0],i,2,codeUnit);

        ucnv_fromUnicode(cnv,&myTarget,targetLimit, 
                         (const UChar**)&mySource, 
                         sourceLimit,NULL, TRUE,&errorCode);

        if(U_SUCCESS(errorCode)) {
            return (jboolean)TRUE;
        }
    }
    return (jboolean)FALSE;
}


static jboolean canDecode(JNIEnv *env, jclass, jlong handle, jbyteArray source) {
    
    UErrorCode errorCode =U_ZERO_ERROR;
    UConverter* cnv = (UConverter*)handle;
    if(cnv) {
        jint len = env->GetArrayLength(source);
        jbyte* cSource =(jbyte*) env->GetPrimitiveArrayCritical(source, NULL);
        if(cSource) {
            const char* cSourceLimit = reinterpret_cast<const char*>(cSource+len);

            /* Assume that we need at most twice the length of source */
            UChar* target = (UChar*) malloc(sizeof(UChar)* (len<<1));
            UChar* targetLimit = target + (len<<1);
            if(target) {
                ucnv_toUnicode(cnv,&target,targetLimit, (const char**)&cSource,
                        cSourceLimit,NULL, TRUE,&errorCode);

                if(U_SUCCESS(errorCode)) {
                    free(target);
                    env->ReleasePrimitiveArrayCritical(source,cSource,0);
                    return (jboolean)TRUE;
                }
            }
            free(target);
        }
        env->ReleasePrimitiveArrayCritical(source,cSource,0);
    }
    return (jboolean)FALSE;
}

static int32_t copyString(char* dest, int32_t destCapacity, int32_t startIndex,
           const char* src, UErrorCode* status) {
    int32_t srcLen = 0, i=0;
    if(U_FAILURE(*status)) {
        return 0;
    }
    if(dest == NULL || src == NULL || destCapacity < startIndex) { 
        *status = U_ILLEGAL_ARGUMENT_ERROR;
        return 0;
    }
    srcLen = strlen(src);
    if(srcLen >= destCapacity) {
        *status = U_BUFFER_OVERFLOW_ERROR;
        return 0;
    }
    for(i=0; i < srcLen; i++) {
        dest[startIndex++] = src[i];
    }
    /* null terminate the buffer */
    dest[startIndex] = 0; /* no bounds check already made sure that we have enough room */
    return startIndex;
}

static int32_t getJavaCanonicalName1(const char* icuCanonicalName,
                     char* canonicalName, int32_t capacity, 
                     UErrorCode* status) {
    int32_t retLen = 0;
    const char* cName = NULL;
    /* find out the alias with MIME tag */
    if((cName =ucnv_getStandardName(icuCanonicalName, "MIME", status)) !=  NULL) {
        retLen = copyString(canonicalName, capacity, 0, cName, status);
        /* find out the alias with IANA tag */
    }else if((cName =ucnv_getStandardName(icuCanonicalName, "IANA", status)) !=  NULL) {
        retLen = copyString(canonicalName, capacity, 0, cName, status);
    }else {
        /*  
            check to see if an alias already exists with x- prefix, if yes then 
            make that the canonical name
        */
        int32_t aliasCount = ucnv_countAliases(icuCanonicalName,status);
        int32_t i=0;
        const char* name;
        for(i=0;i<aliasCount;i++) {
            name = ucnv_getAlias(icuCanonicalName,(uint16_t)i, status);
            if(name != NULL && name[0]=='x' && name[1]=='-') {
                retLen = copyString(canonicalName, capacity, 0, name, status);
                break;
            }
        }
        /* last resort just append x- to any of the alias and 
            make it the canonical name */
        if(retLen == 0 && U_SUCCESS(*status)) {
            name = ucnv_getStandardName(icuCanonicalName, "UTR22", status);
            if(name == NULL && strchr(icuCanonicalName, ',')!= NULL) {
                name = ucnv_getAlias(icuCanonicalName, 1, status);
                if(*status == U_INDEX_OUTOFBOUNDS_ERROR) {
                    *status = U_ZERO_ERROR;
                }
            }
            /* if there is no UTR22 canonical name .. then just return itself*/
            if(name == NULL) {                
                name = icuCanonicalName;
            }
            if(capacity >= 2) {
                strcpy(canonicalName,"x-");
            }
            retLen = copyString(canonicalName, capacity, 2, name, status);
        }
    }
    return retLen;
}

static jstring getJavaCanonicalName(JNIEnv *env, const char* icuCanonicalName) {
    /*
     * If a charset listed in the IANA Charset Registry is supported by an implementation
     * of the Java platform then its canonical name must be the name listed in the registry.
     * Many charsets are given more than one name in the registry, in which case the registry
     * identifies one of the names as MIME-preferred. If a charset has more than one registry
     * name then its canonical name must be the MIME-preferred name and the other names in
     * the registry must be valid aliases. If a supported charset is not listed in the IANA
     * registry then its canonical name must begin with one of the strings "X-" or "x-".
     */
    UErrorCode error = U_ZERO_ERROR;
    char cName[UCNV_MAX_CONVERTER_NAME_LENGTH] = {0};
    if (icuCanonicalName[0] != 0) {
        getJavaCanonicalName1(icuCanonicalName, cName, UCNV_MAX_CONVERTER_NAME_LENGTH, &error);
    }
    return env->NewStringUTF(cName);
}

static jobjectArray getAvailable(JNIEnv *env, jclass) {
    int32_t num = ucnv_countAvailable();
    jobjectArray result = env->NewObjectArray(num, env->FindClass("java/lang/String"), NULL);
    for (int i = 0; i < num; ++i) {
        const char* name = ucnv_getAvailableName(i);
        jstring javaCanonicalName = getJavaCanonicalName(env, name);
        env->SetObjectArrayElement(result, i, javaCanonicalName);
        env->DeleteLocalRef(javaCanonicalName);
    }
    return result;
}

static jint countAliases(JNIEnv *env, jclass, jstring enc) {
    ScopedUtfChars encChars(env, enc);
    UErrorCode error = U_ZERO_ERROR;
    return encChars.data() ? ucnv_countAliases(encChars.data(), &error) : 0;
}

static jobjectArray getAliases(JNIEnv* env, const char* icuCanonicalName) {
    // Get an upper bound on the number of aliases...
    const char* myEncName = icuCanonicalName;
    UErrorCode error = U_ZERO_ERROR;
    int32_t aliasCount = ucnv_countAliases(myEncName, &error);
    if (aliasCount == 0 && myEncName[0] == 'x' && myEncName[1] == '-') {
        myEncName = myEncName + 2;
        aliasCount = ucnv_countAliases(myEncName, &error);
    }
    if (!U_SUCCESS(error)) {
        return NULL;
    }

    // Collect the aliases we want...
    const char* aliasArray[aliasCount];
    int actualAliasCount = 0;
    for(int i = 0; i < aliasCount; ++i) {
        const char* name = ucnv_getAlias(myEncName, (uint16_t) i, &error);
        if (!U_SUCCESS(error)) {
            return NULL;
        }
        // TODO: why do we ignore these ones?
        if (strchr(name, '+') == 0 && strchr(name, ',') == 0) {
            aliasArray[actualAliasCount++]= name;
        }
    }

    // Convert our C++ char*[] into a Java String[]...
    jobjectArray result = env->NewObjectArray(actualAliasCount, env->FindClass("java/lang/String"), NULL);
    for (int i = 0; i < actualAliasCount; ++i) {
        jstring alias = env->NewStringUTF(aliasArray[i]);
        env->SetObjectArrayElement(result, i, alias);
        env->DeleteLocalRef(alias);
    }
    return result;
}

static const char* getICUCanonicalName(const char* name) {
    UErrorCode error = U_ZERO_ERROR;
    const char* canonicalName = NULL;
    if ((canonicalName = ucnv_getCanonicalName(name, "MIME", &error)) != NULL) {
        return canonicalName;
    } else if((canonicalName = ucnv_getCanonicalName(name, "IANA", &error)) != NULL) {
        return canonicalName;
    } else if((canonicalName = ucnv_getCanonicalName(name, "", &error)) != NULL) {
        return canonicalName;
    } else if((canonicalName =  ucnv_getAlias(name, 0, &error)) != NULL) {
        /* we have some aliases in the form x-blah .. match those first */
        return canonicalName;
    } else if (strstr(name, "x-") == name) {
        /* check if the converter can be opened with the name given */
        error = U_ZERO_ERROR;
        UConverter* conv = ucnv_open(name + 2, &error);
        if (conv != NULL) {
            ucnv_close(conv);
            return name + 2;
        }
    }
    return NULL;
}

#define SUBS_ARRAY_CAPACITY 256
struct EncoderCallbackContext {
    int length;
    char subChars[SUBS_ARRAY_CAPACITY];
    UConverterFromUCallback onUnmappableInput;
    UConverterFromUCallback onMalformedInput;
};

static void CHARSET_ENCODER_CALLBACK(const void *context,
                  UConverterFromUnicodeArgs *fromArgs,
                  const UChar* codeUnits,
                  int32_t length,
                  UChar32 codePoint,
                  UConverterCallbackReason reason,
                  UErrorCode * status) {   
    if(context) {
        EncoderCallbackContext* ctx = (EncoderCallbackContext*)context;
        
        if(ctx) {
            UConverterFromUCallback realCB = NULL;
            switch(reason) {
                case UCNV_UNASSIGNED:
                    realCB = ctx->onUnmappableInput;
                    break;
                case UCNV_ILLEGAL:/*malformed input*/
                case UCNV_IRREGULAR:/*malformed input*/
                    realCB = ctx->onMalformedInput;
                    break;
                /*
                case UCNV_RESET:
                    ucnv_resetToUnicode(args->converter);
                    break;
                case UCNV_CLOSE:
                    ucnv_close(args->converter);
                    break;
                case UCNV_CLONE:
                    ucnv_clone(args->clone);
               */
                default:
                    *status = U_ILLEGAL_ARGUMENT_ERROR;
                    return;
            }
            if(realCB==NULL) {
                *status = U_INTERNAL_PROGRAM_ERROR;
            }
            realCB(context, fromArgs, codeUnits, length, codePoint, reason, status);
        }
    }      
}

static void JNI_FROM_U_CALLBACK_SUBSTITUTE_ENCODER(const void *context,
                                        UConverterFromUnicodeArgs *fromArgs,
                                        const UChar* codeUnits,
                                        int32_t length,
                                        UChar32 codePoint,
                                        UConverterCallbackReason reason,
                                        UErrorCode * err) {
    if(context) {
        EncoderCallbackContext* temp = (EncoderCallbackContext*)context;
        *err = U_ZERO_ERROR;
        ucnv_cbFromUWriteBytes(fromArgs,temp->subChars ,temp->length , 0, err);
    }
    return;
}

static UConverterFromUCallback getFromUCallback(int32_t mode) {
    switch(mode) {
        default: /* falls through */
        case com_ibm_icu4jni_converters_NativeConverter_STOP_CALLBACK:
            return UCNV_FROM_U_CALLBACK_STOP;
        case com_ibm_icu4jni_converters_NativeConverter_SKIP_CALLBACK:
            return UCNV_FROM_U_CALLBACK_SKIP ;
        case com_ibm_icu4jni_converters_NativeConverter_SUBSTITUTE_CALLBACK:
            return JNI_FROM_U_CALLBACK_SUBSTITUTE_ENCODER;
    }
}

static jint setCallbackEncode(JNIEnv *env, jclass, jlong handle, jint onMalformedInput, jint onUnmappableInput, jbyteArray subChars, jint length) {

    UConverter* conv = (UConverter*)handle;
    UErrorCode errorCode =U_ZERO_ERROR;

    if(conv) {
        
        UConverterFromUCallback fromUOldAction = NULL;
        void* fromUOldContext = NULL;
        EncoderCallbackContext* fromUNewContext=NULL;
        UConverterFromUCallback fromUNewAction=NULL;
        jbyte* sub = (jbyte*) env->GetPrimitiveArrayCritical(subChars, NULL);
        ucnv_getFromUCallBack(conv, &fromUOldAction, const_cast<const void**>(&fromUOldContext));

        /* fromUOldContext can only be DecodeCallbackContext since
           the converter created is private data for the decoder
           and callbacks can only be set via this method!
        */
        if(fromUOldContext==NULL) {
            fromUNewContext = (EncoderCallbackContext*) malloc(sizeof(EncoderCallbackContext));
            fromUNewAction = CHARSET_ENCODER_CALLBACK;
        }else{
            fromUNewContext = (EncoderCallbackContext*) fromUOldContext;
            fromUNewAction = fromUOldAction;
            fromUOldAction = NULL;
            fromUOldContext = NULL;
        }
        fromUNewContext->onMalformedInput = getFromUCallback(onMalformedInput);
        fromUNewContext->onUnmappableInput = getFromUCallback(onUnmappableInput);
        // BEGIN android-changed
        if(sub!=NULL) {
            fromUNewContext->length = length;
            const char* src = const_cast<const char*>(reinterpret_cast<char*>(sub));
            strncpy(fromUNewContext->subChars, src, length);
            env->ReleasePrimitiveArrayCritical(subChars, sub, 0);
        }else{
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        }
        // END android-changed

        ucnv_setFromUCallBack(conv,
           fromUNewAction,
           fromUNewContext,
           &fromUOldAction,
           (const void**)&fromUOldContext,
           &errorCode);


        return errorCode;
    }
    return U_ILLEGAL_ARGUMENT_ERROR;
}
                                                                  
struct DecoderCallbackContext {
    int length;
    UChar subUChars[256];
    UConverterToUCallback onUnmappableInput;
    UConverterToUCallback onMalformedInput;
};

static void JNI_TO_U_CALLBACK_SUBSTITUTE_DECODER(const void *context,
                                    UConverterToUnicodeArgs *toArgs,
                                    const char* codeUnits,
                                    int32_t length,
                                    UConverterCallbackReason reason,
                                    UErrorCode * err) {
    if(context) {
        DecoderCallbackContext* temp = (DecoderCallbackContext*)context;
        *err = U_ZERO_ERROR;
        ucnv_cbToUWriteUChars(toArgs,temp->subUChars ,temp->length , 0, err);
    }
    return;
}

static UConverterToUCallback getToUCallback(int32_t mode) {
    switch(mode) {
        default: /* falls through */
        case com_ibm_icu4jni_converters_NativeConverter_STOP_CALLBACK:
            return UCNV_TO_U_CALLBACK_STOP;
        case com_ibm_icu4jni_converters_NativeConverter_SKIP_CALLBACK:
            return UCNV_TO_U_CALLBACK_SKIP ;
        case com_ibm_icu4jni_converters_NativeConverter_SUBSTITUTE_CALLBACK:
            return JNI_TO_U_CALLBACK_SUBSTITUTE_DECODER;
    }
}

static void CHARSET_DECODER_CALLBACK(const void *context,
                               UConverterToUnicodeArgs *args, 
                               const char* codeUnits, 
                               int32_t length,
                               UConverterCallbackReason reason,
                               UErrorCode *status ) {
   
    if(context) {
        DecoderCallbackContext* ctx = (DecoderCallbackContext*)context;
        
        if(ctx) {
            UConverterToUCallback realCB = NULL;
            switch(reason) {
                case UCNV_UNASSIGNED:
                    realCB = ctx->onUnmappableInput;
                    break;
                case UCNV_ILLEGAL:/*malformed input*/
                case UCNV_IRREGULAR:/*malformed input*/
                    realCB = ctx->onMalformedInput;
                    break;
                /*
                case UCNV_RESET:
                    ucnv_resetToUnicode(args->converter);
                    break;
                case UCNV_CLOSE:
                    ucnv_close(args->converter);
                    break;
                case UCNV_CLONE:
                    ucnv_clone(args->clone);
               */
                default:
                    *status = U_ILLEGAL_ARGUMENT_ERROR;
                    return;
            }
            if(realCB==NULL) {
                *status = U_INTERNAL_PROGRAM_ERROR;
            }
            realCB(context, args, codeUnits, length, reason, status);
        }
    }      
}

static jint setCallbackDecode(JNIEnv *env, jclass, jlong handle, jint onMalformedInput, jint onUnmappableInput, jcharArray subChars, jint length) {
    
    UConverter* conv = (UConverter*)handle;
    UErrorCode errorCode =U_ZERO_ERROR;
    if(conv) {
        
        UConverterToUCallback toUOldAction ;
        void* toUOldContext;
        DecoderCallbackContext* toUNewContext = NULL;
        UConverterToUCallback toUNewAction = NULL;
        jchar* sub = (jchar*) env->GetPrimitiveArrayCritical(subChars, NULL);
    
        ucnv_getToUCallBack(conv, &toUOldAction, const_cast<const void**>(&toUOldContext));

        /* toUOldContext can only be DecodeCallbackContext since
           the converter created is private data for the decoder
           and callbacks can only be set via this method!
        */
        if(toUOldContext==NULL) {
            toUNewContext = (DecoderCallbackContext*) malloc(sizeof(DecoderCallbackContext));
            toUNewAction = CHARSET_DECODER_CALLBACK;
        }else{
            toUNewContext = reinterpret_cast<DecoderCallbackContext*>(toUOldContext);
            toUNewAction = toUOldAction;
            toUOldAction = NULL;
            toUOldContext = NULL;
        }
        toUNewContext->onMalformedInput = getToUCallback(onMalformedInput);
        toUNewContext->onUnmappableInput = getToUCallback(onUnmappableInput);
        // BEGIN android-changed
        if(sub!=NULL) {
            toUNewContext->length = length;
            u_strncpy(toUNewContext->subUChars, sub, length);
            env->ReleasePrimitiveArrayCritical(subChars, sub, 0);
        }else{
            errorCode =  U_ILLEGAL_ARGUMENT_ERROR;
        }
        // END android-changed
        ucnv_setToUCallBack(conv,
           toUNewAction,
           toUNewContext,
           &toUOldAction,
           (const void**)&toUOldContext,
           &errorCode);

        return errorCode;
    }
    return U_ILLEGAL_ARGUMENT_ERROR;
}

static jlong safeClone(JNIEnv *env, jclass, jlong address) {
    UConverter* source = reinterpret_cast<UConverter*>(static_cast<uintptr_t>(address));
    if (!source) {
        return NULL;
    }
    UErrorCode status = U_ZERO_ERROR;
    jint bufferSize = U_CNV_SAFECLONE_BUFFERSIZE;
    UConverter* conv = ucnv_safeClone(source, NULL, &bufferSize, &status);
    icu4jni_error(env, status);
    return reinterpret_cast<uintptr_t>(conv);
}

static jint getMaxCharsPerByte(JNIEnv *env, jclass, jlong handle) {
    /*
     * currently we know that max number of chars per byte is 2
     */
    return 2;
}

static jfloat getAveCharsPerByte(JNIEnv *env, jclass, jlong handle) {
    return (1 / (jfloat) getMaxBytesPerChar(env, NULL, handle));
}

static jbyteArray getSubstitutionBytes(JNIEnv *env, jclass, jlong handle) {
    const UConverter * cnv = (const UConverter *) handle;
    if (cnv) {
        UErrorCode status = U_ZERO_ERROR;
        char subBytes[10];
        int8_t len =(char)10;
        ucnv_getSubstChars(cnv,subBytes,&len,&status);
        if(U_SUCCESS(status)) {
            jbyteArray arr = env->NewByteArray(len);
            if (arr) {
                env->SetByteArrayRegion(arr,0,len,(jbyte*)subBytes);
            }
            return arr;
        }
    }
    return env->NewByteArray(0);
}

static jboolean contains(JNIEnv* env, jclass, jlong handle1, jlong handle2) {
    UErrorCode status = U_ZERO_ERROR;
    const UConverter * cnv1 = (const UConverter *) handle1;
    const UConverter * cnv2 = (const UConverter *) handle2;
    UBool bRet = 0;
    
    if(cnv1 != NULL && cnv2 != NULL) {
        /* open charset 1 */
        USet* set1 = uset_open(1, 2);
        ucnv_getUnicodeSet(cnv1, set1, UCNV_ROUNDTRIP_SET, &status);

        if(U_SUCCESS(status)) {
            /* open charset 2 */
            status = U_ZERO_ERROR;
            USet* set2 = uset_open(1, 2);
            ucnv_getUnicodeSet(cnv2, set2, UCNV_ROUNDTRIP_SET, &status);

            /* contains?      */
            if(U_SUCCESS(status)) {
                bRet = uset_containsAll(set1, set2);
                uset_close(set2);
            }
            uset_close(set1);
        }
    }
    return bRet;
}

static jobject charsetForName(JNIEnv* env, jclass, jstring charsetName) {
    ScopedUtfChars charsetNameChars(env, charsetName);
    if (!charsetNameChars.data()) {
        return NULL;
    }
    // Get ICU's canonical name for this charset.
    const char* icuCanonicalName = getICUCanonicalName(charsetNameChars.data());
    if (icuCanonicalName == NULL) {
        return NULL;
    }
    // Get Java's canonical name for this charset.
    jstring javaCanonicalName = getJavaCanonicalName(env, icuCanonicalName);
    if (env->ExceptionOccurred()) {
        return NULL;
    }

    // Check that this charset is supported.
    UErrorCode errorCode = U_ZERO_ERROR;
    UConverter* conv = ucnv_open(icuCanonicalName, &errorCode);
    icu4jni_error(env, errorCode);
    closeConverter(env, NULL, (jlong) conv);
    if (env->ExceptionOccurred()) {
        return NULL;
    }

    // Get the aliases for this charset.
    jobjectArray aliases = getAliases(env, icuCanonicalName);
    if (env->ExceptionOccurred()) {
        return NULL;
    }

    // Construct the CharsetICU object.
    jclass charsetClass = env->FindClass("com/ibm/icu4jni/charset/CharsetICU");
    if (env->ExceptionOccurred()) {
        return NULL;
    }
    jmethodID charsetConstructor = env->GetMethodID(charsetClass, "<init>",
            "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V");
    if (env->ExceptionOccurred()) {
        return NULL;
    }
    return env->NewObject(charsetClass, charsetConstructor,
            javaCanonicalName, env->NewStringUTF(icuCanonicalName), aliases);
}

/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "charsetForName", "(Ljava/lang/String;)Ljava/nio/charset/Charset;", (void*) charsetForName },
    { "convertByteToChar", "(J[BI[CI[IZ)I", (void*) convertByteToChar },
    { "decode", "(J[BI[CI[IZ)I", (void*) decode },
    { "convertCharToByte", "(J[CI[BI[IZ)I", (void*) convertCharToByte },
    { "encode", "(J[CI[BI[IZ)I", (void*) encode },
    { "flushCharToByte", "(J[BI[I)I", (void*) flushCharToByte },
    { "flushByteToChar", "(J[CI[I)I", (void*) flushByteToChar },
    { "openConverter", "(Ljava/lang/String;)J", (void*) openConverter },
    { "resetByteToChar", "(J)V", (void*) resetByteToChar },
    { "resetCharToByte", "(J)V", (void*) resetCharToByte },
    { "closeConverter", "(J)V", (void*) closeConverter },
    { "setSubstitutionChars", "(J[CI)I", (void*) setSubstitutionChars },
    { "setSubstitutionBytes", "(J[BI)I", (void*) setSubstitutionBytes },
    { "setSubstitutionModeCharToByte", "(JZ)I", (void*) setSubstitutionModeCharToByte },
    { "setSubstitutionModeByteToChar", "(JZ)I", (void*) setSubstitutionModeByteToChar },
    { "countInvalidBytes", "(J[I)I", (void*) countInvalidBytes },
    { "countInvalidChars", "(J[I)I", (void*) countInvalidChars },
    { "getMaxBytesPerChar", "(J)I", (void*) getMaxBytesPerChar },
    { "getMinBytesPerChar", "(J)I", (void*) getMinBytesPerChar },
    { "getAveBytesPerChar", "(J)F", (void*) getAveBytesPerChar },
    { "getMaxCharsPerByte", "(J)I", (void*) getMaxCharsPerByte },
    { "getAveCharsPerByte", "(J)F", (void*) getAveCharsPerByte },
    { "contains", "(JJ)Z", (void*) contains },
    { "getSubstitutionBytes", "(J)[B", (void*) getSubstitutionBytes },
    { "canEncode", "(JI)Z", (void*) canEncode },
    { "canDecode", "(J[B)Z", (void*) canDecode },
    { "getAvailable", "()[Ljava/lang/String;", (void*) getAvailable },
    { "countAliases", "(Ljava/lang/String;)I", (void*) countAliases },
    { "setCallbackDecode", "(JII[CI)I", (void*) setCallbackDecode },
    { "setCallbackEncode", "(JII[BI)I", (void*) setCallbackEncode },
    { "safeClone", "(J)J", (void*) safeClone }
};

int register_com_ibm_icu4jni_converters_NativeConverter(JNIEnv *_env) {
    return jniRegisterNativeMethods(_env, "com/ibm/icu4jni/charset/NativeConverter",
                gMethods, NELEM(gMethods));
}
