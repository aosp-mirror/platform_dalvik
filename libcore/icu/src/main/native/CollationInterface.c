/**
*******************************************************************************
* Copyright (C) 1996-2005, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/

#include "JNIHelp.h"
#include "AndroidSystemNatives.h"
#include "ErrorCode.h"
#include "unicode/ucol.h"
#include "unicode/ucoleitr.h"
#include "ucol_imp.h"


/**
* Closing a C UCollator with the argument locale rules.
* Note determining if a collator currently exist for the caller is to be handled
* by the caller. Hence if the caller has a existing collator, it is his 
* responsibility to delete first before calling this method.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address of the C UCollator
*/
static void closeCollator(JNIEnv *env, jclass obj,
        jint address) { 

  UCollator *collator = (UCollator *)(int)address;
  ucol_close(collator);
}


/**
* Close a C collation element iterator.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address of C collation element iterator to close.
*/
static void closeElements(JNIEnv *env, jclass obj,
        jint address) {

  UCollationElements *iterator = (UCollationElements *)(int)address;
  ucol_closeElements(iterator);
}

/**
* Compare two strings.
* The strings will be compared using the normalization mode and options
* specified in openCollator or openCollatorFromRules
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address address of the c collator
* @param source The source string.
* @param target The target string.
* @return result of the comparison, UCOL_EQUAL, UCOL_GREATER or UCOL_LESS
*/
static jint compare(JNIEnv *env, jclass obj, jint address,
        jstring source, jstring target) {

    const UCollator *collator  = (const UCollator *)(int)address;
    jint result = -2;
    if(collator){
        jsize       srclength = (*env)->GetStringLength(env, source);
        const UChar *srcstr   = (const UChar *)(*env)->GetStringCritical(env,source,0);
        if(srcstr){
            jsize       tgtlength = (*env)->GetStringLength(env, target);
            const UChar *tgtstr    = (const UChar *)(*env)->GetStringCritical(env,target,0);
            if(tgtstr){ 
                  result = ucol_strcoll(collator, srcstr, srclength, tgtstr, tgtlength);
                  (*env)->ReleaseStringCritical(env, source, srcstr);
                  (*env)->ReleaseStringCritical(env, target, tgtstr);
                  return result;
            }else{
                icu4jni_error(env,U_ILLEGAL_ARGUMENT_ERROR);
            }
        }else{
            icu4jni_error(env,U_ILLEGAL_ARGUMENT_ERROR);
        }
    }else{
        icu4jni_error(env,U_ILLEGAL_ARGUMENT_ERROR);
    }
    return result;
}

/**
* Universal attribute getter
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address address of the C collator
* @param type type of attribute to be set
* @return attribute value
* @exception thrown when error occurs while getting attribute value
*/
static jint getAttribute(JNIEnv *env, jclass obj, jint address,
        jint type) {

    const UCollator *collator = (const UCollator *)(int)address;
    UErrorCode status = U_ZERO_ERROR;
    if(collator){
        jint result = (jint)ucol_getAttribute(collator, (UColAttribute)type, 
                                            &status);
        if (icu4jni_error(env, status) != FALSE){
            return (jint)UCOL_DEFAULT;
        }
        return result;
    }else{
        icu4jni_error(env,U_ILLEGAL_ARGUMENT_ERROR);
    }
    return (jint)UCOL_DEFAULT;
}

/** 
* Create a CollationElementIterator object that will iterator over the elements 
* in a string, using the collation rules defined in this RuleBasedCollatorJNI
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address address of C collator
* @param source string to iterate over
* @return address of C collationelement
*/
static jint getCollationElementIterator(JNIEnv *env,
        jclass obj, jint address, jstring source) {

    UErrorCode status    = U_ZERO_ERROR;
    UCollator *collator  = (UCollator *)(int)address;
    jint       result=0;
    if(collator){
        jsize srclength     = (*env)->GetStringLength(env, source);
        const UChar *srcstr = (const UChar *)(*env)->GetStringCritical(env,source,0);
        if(srcstr){
            result = (jint)(ucol_openElements(collator, srcstr, srclength, &status));

            (*env)->ReleaseStringCritical(env, source, srcstr);
            icu4jni_error(env, status);
        }else{
            icu4jni_error(env, U_ILLEGAL_ARGUMENT_ERROR);
        }
    }else{
        icu4jni_error(env, U_ILLEGAL_ARGUMENT_ERROR);
    }
    return result;
}

/**
* Get the maximum length of any expansion sequences that end with the specified 
* comparison order.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address of the C collation element iterator containing the text.
* @param order collation order returned by previous or next.
* @return maximum length of any expansion sequences ending with the specified 
*         order or 1 if collation order does not occur at the end of any 
*         expansion sequence.
*/
static jint getMaxExpansion(JNIEnv *env, jclass obj,
        jint address, jint order) {

  UCollationElements *iterator = (UCollationElements *)(int)address;
  return ucol_getMaxExpansion(iterator, order);
}

/**
* Get the normalization mode for this object.
* The normalization mode influences how strings are compared.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address of C collator
* @return normalization mode; one of the values from NormalizerEnum
*/
static jint getNormalization(JNIEnv *env, jclass obj,
        jint address) {

  UErrorCode status = U_ZERO_ERROR;
  const UCollator *collator = (const UCollator *)(int)address;
  if(U_FAILURE(status)){
       icu4jni_error(env, status);
  }
  return (jint)ucol_getAttribute(collator,UCOL_NORMALIZATION_MODE,&status);

}

/**
* Set the normalization mode for this object.
* The normalization mode influences how strings are compared.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address of C collator
* @param mode the normalization mode
*/
static void setNormalization(JNIEnv *env, jclass obj, jint address, 
        jint mode) {

    UErrorCode status = U_ZERO_ERROR;
    const UCollator *collator = (const UCollator *)(int)address;
    if(U_FAILURE(status)){
        icu4jni_error(env, status);
    }
    ucol_setAttribute(collator,UCOL_NORMALIZATION_MODE,mode,&status);
}


/**
* Get the offset of the current source character.
* This is an offset into the text of the character containing the current
* collation elements.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param addresss of the C collation elements iterator to query.
* @return offset of the current source character.
*/
static jint getOffset(JNIEnv *env, jclass obj, jint address) {

  UCollationElements *iterator = (UCollationElements *)(int)address;
  return ucol_getOffset(iterator);
}

/**
* Get the collation rules from a UCollator.
* The rules will follow the rule syntax.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address the address of the C collator
* @return collation rules.
*/
static jstring getRules(JNIEnv *env, jclass obj,
        jint address) {

  const UCollator *collator = (const UCollator *)(int)address;
  int32_t length=0;
  const UChar *rules = ucol_getRules(collator, &length);
  return (*env)->NewString(env, rules, length);
}

/**
* Get a sort key for the argument string
* Sort keys may be compared using java.util.Arrays.equals
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address address of the C collator
* @param source string for key to be generated
* @return sort key
*/
static jbyteArray getSortKey(JNIEnv *env, jclass obj,
        jint address, jstring source) {

  const UCollator *collator  = (const UCollator *)(int)address;
  jbyteArray result;
  if(collator && source){
      // BEGIN android-added
      if(!source) {
          return NULL;
      }
      // END android-added
      jsize srclength            = (*env)->GetStringLength(env, source);
      const UChar *srcstr        = (const UChar *)(*env)->GetStringCritical(env,source, 0);
      if(srcstr){
// BEGIN android-changed
          uint8_t bytearray[UCOL_MAX_BUFFER * 2];
          uint8_t *largerbytearray = NULL;
          uint8_t *usedbytearray = bytearray;
  
          jint bytearraysize = ucol_getSortKey(collator, srcstr, srclength, bytearray, 
                                               sizeof(bytearray) - 1);
 
          if (bytearraysize > sizeof(bytearray) - 1) {
            // didn't fit, try again with a larger buffer.
            largerbytearray = malloc(bytearraysize + 1);
            usedbytearray = largerbytearray;
            bytearraysize = ucol_getSortKey(collator, srcstr, srclength, largerbytearray, 
                                               bytearraysize);
          }
 
          (*env)->ReleaseStringCritical(env, source, srcstr);

          if (bytearraysize == 0) {
            free(largerbytearray);
            return NULL;
          }
  
          /* no problem converting uint8_t to int8_t, gives back the correct value
           * tried and tested
           */
          result = (*env)->NewByteArray(env, bytearraysize);
          (*env)->SetByteArrayRegion(env, result, 0, bytearraysize, usedbytearray);
          free(largerbytearray);
// END android-changed
      }else{
          icu4jni_error(env,U_ILLEGAL_ARGUMENT_ERROR);
      }
  }else{
    icu4jni_error(env,U_ILLEGAL_ARGUMENT_ERROR);
  }
  return result;
}

/**
* Returns a hash of this collation object
* Note this method is not complete, it only returns 0 at the moment.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address address of C collator
* @return hash of this collation object
*/
static jint hashCode(JNIEnv *env, jclass obj, jint address) {
	
  UCollator *collator = (UCollator *)(int)address;
  int32_t length=0;
  const UChar *rules = ucol_getRules(collator, &length);
  /* temporary commented out
   * return uhash_hashUCharsN(rules, length);
   */
  return 0;
}

/**
* Get the ordering priority of the next collation element in the text.
* A single character may contain more than one collation element.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address if C collation elements containing the text.
* @return next collation elements ordering, otherwise returns NULLORDER if an 
*         error has occured or if the end of string has been reached
*/
static jint next(JNIEnv *env, jclass obj, jint address) {
  UCollationElements *iterator = (UCollationElements *)(int)address;
  UErrorCode status = U_ZERO_ERROR;
  jint result = ucol_next(iterator, &status);

   icu4jni_error(env, status);
  return result;
}

/**
* Opening a new C UCollator with the default locale.
* Note determining if a collator currently exist for the caller is to be handled
* by the caller. Hence if the caller has a existing collator, it is his 
* responsibility to delete first before calling this method.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @return address of the new C UCollator
* @exception thrown if creation of the UCollator fails
*/
static jint openCollator__(JNIEnv *env, jclass obj) {
  jint result;
  UErrorCode status = U_ZERO_ERROR;

  result = (jint)ucol_open(NULL, &status);
  if ( icu4jni_error(env, status) != FALSE)
    return 0;
 
  return result;
}


/**
* Opening a new C UCollator with the argument locale rules.
* Note determining if a collator currently exist for the caller is to be handled
* by the caller. Hence if the caller has a existing collator, it is his 
* responsibility to delete first before calling this method.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param locale name
* @return address of the new C UCollator
* @exception thrown if creation of the UCollator fails
*/
static jint openCollator__Ljava_lang_String_2(JNIEnv *env,
        jclass obj, jstring locale) {

  /* this will be null terminated */
  const char *localestr = (*env)->GetStringUTFChars(env, locale, 0);
  jint result=0;
  UErrorCode status = U_ZERO_ERROR;

  if(localestr){
      result = (jint)ucol_open(localestr, &status);
      (*env)->ReleaseStringUTFChars(env, locale, localestr);
      icu4jni_error(env, status);
  }else{
      icu4jni_error(env,U_ILLEGAL_ARGUMENT_ERROR);
  }
  return result;
}

/**
* Opening a new C UCollator with the argument locale rules.
* Note determining if a collator currently exist for the caller is to be 
* handled by the caller. Hence if the caller has a existing collator, it is his 
* responsibility to delete first before calling this method.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param rules set of collation rules
* @param normalizationmode normalization mode
* @param strength collation strength
* @return address of the new C UCollator
* @exception thrown if creation of the UCollator fails
*/
static jint openCollatorFromRules(JNIEnv *env, jclass obj,
        jstring rules, jint normalizationmode, jint strength) {

  jsize  ruleslength    = (*env)->GetStringLength(env, rules);
  const UChar *rulestr  = (const UChar *)(*env)->GetStringCritical(env,rules, 0);
  UErrorCode status     = U_ZERO_ERROR;
  jint   result        = 0;
  if(rulestr){
      result = (jint)ucol_openRules(rulestr, ruleslength, 
                                   (UColAttributeValue)normalizationmode,
                                   (UCollationStrength)strength, NULL, &status);

      (*env)->ReleaseStringCritical(env, rules, rulestr);
      icu4jni_error(env, status);
  }else{
      icu4jni_error(env,U_ILLEGAL_ARGUMENT_ERROR);
  }

  return result;
}

/**
* Get the ordering priority of the previous collation element in the text.
* A single character may contain more than one collation element.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address of the C collation element iterator containing the text.
* @return previous collation element ordering, otherwise returns NULLORDER if 
*         an error has occured or if the start of string has been reached
* @exception thrown when retrieval of previous collation element fails.
*/
static jint previous(JNIEnv *env, jclass obj, jint address) {

  UCollationElements *iterator = (UCollationElements *)(int)address;
  UErrorCode status = U_ZERO_ERROR;
  jint result = ucol_previous(iterator, &status);

   icu4jni_error(env, status);
  return result;
}


/**
* Reset the collation elements to their initial state.
* This will move the 'cursor' to the beginning of the text.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address of C collation element iterator to reset.
*/
static void reset(JNIEnv *env, jclass obj, jint address) {

  UCollationElements *iterator = (UCollationElements *)(int)address;
  ucol_reset(iterator);
}

/**
* Thread safe cloning operation
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address address of C collator to be cloned
* @return address of the new clone
* @exception thrown when error occurs while cloning
*/
static jint safeClone(JNIEnv *env, jclass obj, jint address) {

  const UCollator *collator = (const UCollator *)(int)address;
  UErrorCode status = U_ZERO_ERROR;
  jint result;
  jint buffersize = U_COL_SAFECLONE_BUFFERSIZE;

  result = (jint)ucol_safeClone(collator, NULL, &buffersize, &status);

  if ( icu4jni_error(env, status) != FALSE) {
    return 0;
  }
 
  return result;
}

/**
* Universal attribute setter.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address address of the C collator
* @param type type of attribute to be set
* @param value attribute value
* @exception thrown when error occurs while setting attribute value
*/
static void setAttribute(JNIEnv *env, jclass obj, jint address,
        jint type, jint value) {

  UCollator *collator = (UCollator *)(int)address;
  UErrorCode status = U_ZERO_ERROR;
  ucol_setAttribute(collator, (UColAttribute)type, (UColAttributeValue)value, 
                    &status);
   icu4jni_error(env, status);
}

/**
* Set the offset of the current source character.
* This is an offset into the text of the character to be processed.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address of the C collation element iterator to set.
* @param offset The desired character offset.
* @exception thrown when offset setting fails
*/
static void setOffset(JNIEnv *env, jclass obj, jint address,
        jint offset) {

  UCollationElements *iterator = (UCollationElements *)(int)address;
  UErrorCode status = U_ZERO_ERROR;

  ucol_setOffset(iterator, offset, &status);
   icu4jni_error(env, status);
}

/**
* Set the text containing the collation elements.
* @param env JNI environment
* @param obj RuleBasedCollatorJNI object
* @param address of the C collation element iterator to be set
* @param source text containing the collation elements.
* @exception thrown when error occurs while setting offset
*/
static void setText(JNIEnv *env, jclass obj, jint address,
        jstring source) {

  UCollationElements *iterator = (UCollationElements *)(int)address;
  UErrorCode status = U_ZERO_ERROR;
  int strlength = (*env)->GetStringLength(env, source);
  const UChar *str = (const UChar *)(*env)->GetStringCritical(env, source, 0);
  
  ucol_setText(iterator, str, strlength, &status);
  (*env)->ReleaseStringCritical(env, source, str);

   icu4jni_error(env, status);
}

// BEGIN android-added
static jstring getAvailableLocalesImpl(JNIEnv *env, jclass clazz, jint index) {

    const char * locale = ucol_getAvailable(index);

    return (*env)->NewStringUTF(env, locale);

}

static jint getAvailableLocalesCountImpl(JNIEnv *env, jclass clazz) {
    return ucol_countAvailable();
}
// END android-added

/*
 * JNI registratio
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    // BEGIN android-added
    { "getAvailableLocalesImpl", "(I)Ljava/lang/String;", (void*) getAvailableLocalesImpl },
    { "getAvailableLocalesCountImpl", "()I", (void*) getAvailableLocalesCountImpl },
    // END android-added
    { "openCollator", "()I", (void*) openCollator__ },
    { "openCollator", "(Ljava/lang/String;)I", (void*) openCollator__Ljava_lang_String_2 },
    { "openCollatorFromRules", "(Ljava/lang/String;II)I", (void*) openCollatorFromRules },
    { "closeCollator", "(I)V", (void*) closeCollator },
    { "compare", "(ILjava/lang/String;Ljava/lang/String;)I", (void*) compare },
    { "getNormalization", "(I)I", (void*) getNormalization },
    { "setNormalization", "(II)V", (void*) setNormalization },
    { "getRules", "(I)Ljava/lang/String;", (void*) getRules },
    { "getSortKey", "(ILjava/lang/String;)[B", (void*) getSortKey },
    { "setAttribute", "(III)V", (void*) setAttribute },
    { "getAttribute", "(II)I", (void*) getAttribute },
    { "safeClone", "(I)I", (void*) safeClone },
    { "getCollationElementIterator", "(ILjava/lang/String;)I", (void*) getCollationElementIterator },
    { "hashCode", "(I)I", (void*) hashCode },
    { "closeElements", "(I)V", (void*) closeElements },
    { "reset", "(I)V", (void*) reset },
    { "next", "(I)I", (void*) next },
    { "previous", "(I)I", (void*) previous },
    { "getMaxExpansion", "(II)I", (void*) getMaxExpansion },
    { "setText", "(ILjava/lang/String;)V", (void*) setText },
    { "getOffset", "(I)I", (void*) getOffset },
    { "setOffset", "(II)V", (void*) setOffset }
};

int register_com_ibm_icu4jni_text_NativeCollator(JNIEnv *_env) { 
    return jniRegisterNativeMethods(_env, "com/ibm/icu4jni/text/NativeCollation",
                gMethods, NELEM(gMethods));
}

