/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "JNIHelp.h"
#include "AndroidSystemNatives.h"
#include "unicode/numfmt.h"
#include "unicode/locid.h"
#include "unicode/ucal.h"
#include "unicode/gregocal.h"
#include "unicode/ucurr.h"
#include "unicode/calendar.h"
#include "unicode/datefmt.h"
#include "unicode/dtfmtsym.h"
#include "unicode/decimfmt.h"
#include "unicode/dcfmtsym.h"
#include "unicode/uclean.h"
#include "unicode/smpdtfmt.h"
#include "unicode/strenum.h"
#include "unicode/ustring.h"
#include "unicode/timezone.h"
#include "ErrorCode.h"
#include <cutils/log.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys/time.h>

static jclass string_class;

class ScopedResourceBundle {
public:
    ScopedResourceBundle(UResourceBundle* bundle) : mBundle(bundle) {
    }

    ~ScopedResourceBundle() {
        if (mBundle != NULL) {
            ures_close(mBundle);
        }
    }

    UResourceBundle* get() {
        return mBundle;
    }

private:
    UResourceBundle* mBundle;
};

static Locale getLocale(JNIEnv* env, jstring locale) {
    const char* name = env->GetStringUTFChars(locale, NULL);
    Locale result = Locale::createFromName(name);
    env->ReleaseStringUTFChars(locale, name);
    return result;
}

static void addObject(JNIEnv* env, jobjectArray result, const char* keyStr, jobject elem, int index) {
    jclass objArray_class = env->FindClass("java/lang/Object");
    jobjectArray element = env->NewObjectArray(2, objArray_class, NULL);
    jstring key = env->NewStringUTF(keyStr);
    env->SetObjectArrayElement(element, 0, key);
    env->SetObjectArrayElement(element, 1, elem);
    env->SetObjectArrayElement(result, index, element);
    env->DeleteLocalRef(key);
    env->DeleteLocalRef(element);
} 

static jint getCurrencyFractionDigitsNative(JNIEnv* env, jclass clazz, jstring currencyCode) {
    UErrorCode status = U_ZERO_ERROR;
    
    NumberFormat* fmt = NumberFormat::createCurrencyInstance(status);
    if (U_FAILURE(status)) {
        return -1;
    }

    const jchar* cCode = env->GetStringChars(currencyCode, NULL);
    fmt->setCurrency(cCode, status);
    env->ReleaseStringChars(currencyCode, cCode);
    if (U_FAILURE(status)) {
        return -1;
    }
    
    // for CurrencyFormats the minimum and maximum fraction digits are the same.
    int result = fmt->getMinimumFractionDigits(); 
    delete fmt;
    return result;
}

static jstring getCurrencyCodeNative(JNIEnv* env, jclass clazz, jstring key) {
    UErrorCode status = U_ZERO_ERROR;
    ScopedResourceBundle supplData(ures_openDirect(NULL, "supplementalData", &status));
    if (U_FAILURE(status)) {
        return NULL;
    }

    ScopedResourceBundle currencyMap(ures_getByKey(supplData.get(), "CurrencyMap", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }

    const char* keyChars = env->GetStringUTFChars(key, NULL);
    ScopedResourceBundle currency(ures_getByKey(currencyMap.get(), keyChars, NULL, &status));
    env->ReleaseStringUTFChars(key, keyChars);
    if (U_FAILURE(status)) {
        return NULL;
    }

    ScopedResourceBundle currencyElem(ures_getByIndex(currency.get(), 0, NULL, &status));
    if (U_FAILURE(status)) {
        return env->NewStringUTF("None");
    }

    // check if there is a to date. If there is, the currency isn't used anymore.
    {
        ScopedResourceBundle currencyTo(ures_getByKey(currencyElem.get(), "to", NULL, &status));
        if (!U_FAILURE(status)) {
            // return and let the ResourceBundle throw an exception
            return NULL;
        }
        status = U_ZERO_ERROR;
    }

    ScopedResourceBundle currencyId(ures_getByKey(currencyElem.get(), "id", NULL, &status));
    if (U_FAILURE(status)) {
        // No id defined for this country
        return env->NewStringUTF("None");
    }

    int length;
    const jchar* id = ures_getString(currencyId.get(), &length, &status);
    if (U_FAILURE(status) || length == 0) {
        return env->NewStringUTF("None");
    }
    return env->NewString(id, length);
}

static jstring getCurrencySymbolNative(JNIEnv* env, jclass clazz, 
        jstring locale, jstring currencyCode) {
    // LOGI("ENTER getCurrencySymbolNative");

    const char* locName = env->GetStringUTFChars(locale, NULL);
    UErrorCode status = U_ZERO_ERROR;
    ScopedResourceBundle root(ures_open(NULL, locName, &status));
    env->ReleaseStringUTFChars(locale, locName);
    if (U_FAILURE(status)) {
        return NULL;
    }

    ScopedResourceBundle rootElems(ures_getByKey(root.get(), "Currencies", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }

    const char* currName = env->GetStringUTFChars(currencyCode, NULL);
    ScopedResourceBundle currencyElems(ures_getByKey(rootElems.get(), currName, NULL, &status));
    env->ReleaseStringUTFChars(currencyCode, currName);
    if (U_FAILURE(status)) {
        return NULL;
    }

    int currSymbL;
    const jchar* currSymbU = ures_getStringByIndex(currencyElems.get(), 0, &currSymbL, &status);
    if (U_FAILURE(status)) {
        return NULL;
    }

    return (currSymbL == 0) ? NULL : env->NewString(currSymbU, currSymbL);
}

static jstring getDisplayCountryNative(JNIEnv* env, jclass clazz, 
        jstring targetLocale, jstring locale) {

    Locale loc = getLocale(env, locale);
    Locale targetLoc = getLocale(env, targetLocale);

    UnicodeString str;
    targetLoc.getDisplayCountry(loc, str);
    return env->NewString(str.getBuffer(), str.length());
}

static jstring getDisplayLanguageNative(JNIEnv* env, jclass clazz, 
        jstring targetLocale, jstring locale) {

    Locale loc = getLocale(env, locale);
    Locale targetLoc = getLocale(env, targetLocale);

    UnicodeString str;
    targetLoc.getDisplayLanguage(loc, str);
    return env->NewString(str.getBuffer(), str.length());
}

static jstring getDisplayVariantNative(JNIEnv* env, jclass clazz, 
        jstring targetLocale, jstring locale) {

    Locale loc = getLocale(env, locale);
    Locale targetLoc = getLocale(env, targetLocale);

    UnicodeString str;
    targetLoc.getDisplayVariant(loc, str);
    return env->NewString(str.getBuffer(), str.length());
}

static jstring getISO3CountryNative(JNIEnv* env, jclass clazz, jstring locale) {
    Locale loc = getLocale(env, locale);
    return env->NewStringUTF(loc.getISO3Country());
}

static jstring getISO3LanguageNative(JNIEnv* env, jclass clazz, jstring locale) {
    Locale loc = getLocale(env, locale);
    return env->NewStringUTF(loc.getISO3Language());
}

static jobjectArray toStringArray(JNIEnv* env, const char* const* strings) {
    size_t count = 0;
    while (strings[count] != NULL) {
        ++count;
    }
    jobjectArray result = env->NewObjectArray(count, string_class, NULL);
    for (size_t i = 0; i < count; ++i) {
        jstring s = env->NewStringUTF(strings[i]);
        env->SetObjectArrayElement(result, i, s);
        env->DeleteLocalRef(s);
    }
    return result;
}

static jobjectArray getISOCountriesNative(JNIEnv* env, jclass clazz) {
    return toStringArray(env, Locale::getISOCountries());
}

static jobjectArray getISOLanguagesNative(JNIEnv* env, jclass clazz) {
    return toStringArray(env, Locale::getISOLanguages());
}

static jobjectArray getAvailableLocalesNative(JNIEnv* env, jclass clazz) {
    size_t count = uloc_countAvailable();
    jobjectArray result = env->NewObjectArray(count, string_class, NULL);
    for (size_t i = 0; i < count; ++i) {
        jstring s = env->NewStringUTF(uloc_getAvailable(i));
        env->SetObjectArrayElement(result, i, s);
        env->DeleteLocalRef(s);
    }
    return result;
}

static void getTimeZonesNative(JNIEnv* env, jclass clazz,
        jobjectArray outerArray, jstring locale) {
    // LOGI("ENTER getTimeZonesNative");
    
    UErrorCode status = U_ZERO_ERROR;

    jobjectArray zoneIdArray;
    jobjectArray longStdTimeArray;
    jobjectArray shortStdTimeArray;
    jobjectArray longDlTimeArray;
    jobjectArray shortDlTimeArray;

    jstring content;
    jstring strObj;
    const jchar* res;
    UnicodeString resU;
    jint length;
    const UnicodeString* zoneID;
    DateFormat* df;

    UnicodeString longPattern("zzzz","");
    UnicodeString shortPattern("z","");
      
    Locale loc = getLocale(env, locale);

    SimpleDateFormat longFormat(longPattern, loc, status);
    SimpleDateFormat shortFormat(shortPattern, loc, status);


    zoneIdArray = (jobjectArray) env->GetObjectArrayElement(outerArray, 0);
    longStdTimeArray = (jobjectArray) env->GetObjectArrayElement(outerArray, 1);
    shortStdTimeArray = (jobjectArray) env->GetObjectArrayElement(outerArray, 2);
    longDlTimeArray = (jobjectArray) env->GetObjectArrayElement(outerArray, 3);
    shortDlTimeArray = (jobjectArray) env->GetObjectArrayElement(outerArray, 4);

    // get all timezone objects
    int count = env->GetArrayLength(zoneIdArray);
    TimeZone* zones[count];
    for(int i = 0; i < count; i++) {
        strObj = (jstring) env->GetObjectArrayElement(zoneIdArray, i);
        length = env->GetStringLength(strObj);
        res = env->GetStringChars(strObj, NULL);
        const UnicodeString zoneID((UChar*)res, length);
        env->ReleaseStringChars(strObj, res);
        zones[i] = TimeZone::createTimeZone(zoneID);
        env->DeleteLocalRef(strObj);
    }

    // 15th January 2008
    UDate date1 = 1203105600000.0;
    // 15th July 2008
    UDate date2 = 1218826800000.0;

    for (int i = 0; i < count; ++i) {
        TimeZone* tz = zones[i];
        longFormat.setTimeZone(*tz);
        shortFormat.setTimeZone(*tz);

        int32_t daylightOffset;
        int32_t rawOffset;
        tz->getOffset(date1, false, rawOffset, daylightOffset, status);
        UDate standardDate;
        UDate daylightSavingDate;
        if (daylightOffset != 0) {
            // The Timezone is reporting that we are in daylight time
            // for the winter date.  The dates are for the wrong hemisphere,
            // swap them.
            standardDate = date2;
            daylightSavingDate = date1;
        } else {
            standardDate = date1;
            daylightSavingDate = date2;
        }

        UnicodeString str;
        shortFormat.format(daylightSavingDate, str);
        content = env->NewString(str.getBuffer(), str.length());
        env->SetObjectArrayElement(shortDlTimeArray, i, content);
        env->DeleteLocalRef(content);

        shortFormat.format(standardDate, str);
        content = env->NewString(str.getBuffer(), str.length());
        env->SetObjectArrayElement(shortStdTimeArray, i, content);
        env->DeleteLocalRef(content);

        longFormat.format(daylightSavingDate, str);
        content = env->NewString(str.getBuffer(), str.length());
        env->SetObjectArrayElement(longDlTimeArray, i, content);
        env->DeleteLocalRef(content);

        longFormat.format(standardDate, str);
        content = env->NewString(str.getBuffer(), str.length());
        env->SetObjectArrayElement(longStdTimeArray, i, content);
        env->DeleteLocalRef(content);

        delete tz;
    }
}

static jstring getDisplayTimeZoneNative(JNIEnv* env, jclass clazz,
        jstring zoneID, jboolean isDST, jint style, jstring localeID) {

    // Build TimeZone object
    const jchar* idChars = env->GetStringChars(zoneID, NULL);
    jint idLength = env->GetStringLength(zoneID);
    UnicodeString idString((UChar*)idChars, idLength);
    TimeZone* zone = TimeZone::createTimeZone(idString);
    env->ReleaseStringChars(zoneID, idChars);

    Locale locale = getLocale(env, localeID);

    // Try to get the display name of the TimeZone according to the Locale
    UnicodeString displayName;
    zone->getDisplayName((UBool)isDST, (style == 0 ? TimeZone::SHORT : TimeZone::LONG), locale, displayName);
    jstring result = env->NewString(displayName.getBuffer(), displayName.length());
    delete zone;
    return result;
}

static void getDayIntVector(JNIEnv* env, UResourceBundle* gregorian, int* values) {

    // get the First day of week and the minimal days in first week numbers
    UErrorCode status = U_ZERO_ERROR;
    ScopedResourceBundle gregorianElems(ures_getByKey(gregorian, "DateTimeElements", NULL, &status));
    if (U_FAILURE(status)) {
        return;
    }

    int intVectSize;
    const int* result = ures_getIntVector(gregorianElems.get(), &intVectSize, &status);
    if (U_FAILURE(status) || intVectSize != 2) {
        return;
    }
    values[0] = result[0];
    values[1] = result[1];
}

static jobjectArray getAmPmMarkers(JNIEnv* env, UResourceBundle* gregorian) {
    UErrorCode status = U_ZERO_ERROR;
    ScopedResourceBundle gregorianElems(ures_getByKey(gregorian, "AmPmMarkers", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }

    ures_resetIterator(gregorianElems.get());

    int lengthAm, lengthPm;
    const jchar* am = ures_getStringByIndex(gregorianElems.get(), 0, &lengthAm, &status);
    const jchar* pm = ures_getStringByIndex(gregorianElems.get(), 1, &lengthPm, &status);

    if (U_FAILURE(status)) {
        return NULL;
    }
    
    jobjectArray amPmMarkers = env->NewObjectArray(2, string_class, NULL);
    jstring amU = env->NewString(am, lengthAm);
    env->SetObjectArrayElement(amPmMarkers, 0, amU);
    env->DeleteLocalRef(amU);
    jstring pmU = env->NewString(pm, lengthPm);
    env->SetObjectArrayElement(amPmMarkers, 1, pmU);
    env->DeleteLocalRef(pmU);

    return amPmMarkers;
}

static jobjectArray getEras(JNIEnv* env, UResourceBundle* gregorian) {
    UErrorCode status = U_ZERO_ERROR;
    ScopedResourceBundle gregorianElems(ures_getByKey(gregorian, "eras", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }

    ScopedResourceBundle eraElems(ures_getByKey(gregorianElems.get(), "abbreviated", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }

    int eraCount = ures_getSize(eraElems.get());
    jobjectArray eras = env->NewObjectArray(eraCount, string_class, NULL);

    ures_resetIterator(eraElems.get());
    for (int i = 0; i < eraCount; ++i) {
        int eraLength;
        const jchar* era = ures_getStringByIndex(eraElems.get(), i, &eraLength, &status);
        if (U_FAILURE(status)) {
            return NULL;
        }
        jstring eraU = env->NewString(era, eraLength);
        env->SetObjectArrayElement(eras, i, eraU);
        env->DeleteLocalRef(eraU);
    }
    return eras;
}

static jobjectArray getMonthNames(JNIEnv* env, UResourceBundle* gregorian, bool longNames) {
    UErrorCode status = U_ZERO_ERROR;
    ScopedResourceBundle gregorianElems(ures_getByKey(gregorian, "monthNames", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }
    
    ScopedResourceBundle monthNameElems(ures_getByKey(gregorianElems.get(), "format", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }
    
    ScopedResourceBundle monthNameElemsFormat(ures_getByKey(monthNameElems.get(), longNames ? "wide" : "abbreviated", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }
    
    ures_resetIterator(monthNameElemsFormat.get());
    int monthCount = ures_getSize(monthNameElemsFormat.get());
    // the array length is +1 because the harmony locales had an empty string at the end of their month name array
    jobjectArray months = env->NewObjectArray(monthCount + 1, string_class, NULL);
    for (int i = 0; i < monthCount; ++i) {
        int monthNameLength;
        const jchar* month = ures_getStringByIndex(monthNameElemsFormat.get(), i, &monthNameLength, &status);
        if (U_FAILURE(status)) {
            return NULL;
        }
        jstring monthU = env->NewString(month, monthNameLength);
        env->SetObjectArrayElement(months, i, monthU);
        env->DeleteLocalRef(monthU);
    }
    
    jstring monthU = env->NewStringUTF("");
    env->SetObjectArrayElement(months, monthCount, monthU);
    env->DeleteLocalRef(monthU);
    
    return months;
}

static jobjectArray getLongMonthNames(JNIEnv* env, UResourceBundle* gregorian) {
    return getMonthNames(env, gregorian, true);
}

static jobjectArray getShortMonthNames(JNIEnv* env, UResourceBundle* gregorian) {
    return getMonthNames(env, gregorian, false);
}

static jobjectArray getWeekdayNames(JNIEnv* env, UResourceBundle* gregorian, bool longNames) {
    UErrorCode status = U_ZERO_ERROR;
    ScopedResourceBundle gregorianElems(ures_getByKey(gregorian, "dayNames", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }

    ScopedResourceBundle dayNameElems(ures_getByKey(gregorianElems.get(), "format", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }

    ScopedResourceBundle dayNameElemsFormat(ures_getByKey(dayNameElems.get(), longNames ? "wide" : "abbreviated", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }

    ures_resetIterator(dayNameElemsFormat.get());
    int dayCount = ures_getSize(dayNameElemsFormat.get());
    jobjectArray weekdays = env->NewObjectArray(dayCount + 1, string_class, NULL);
    // first entry in the weekdays array is an empty string
    env->SetObjectArrayElement(weekdays, 0, env->NewStringUTF(""));
    for(int i = 0; i < dayCount; i++) {
        int dayNameLength;
        const jchar* day = ures_getStringByIndex(dayNameElemsFormat.get(), i, &dayNameLength, &status);
        if(U_FAILURE(status)) {
            return NULL;
        }
        jstring dayU = env->NewString(day, dayNameLength);
        env->SetObjectArrayElement(weekdays, i + 1, dayU);
        env->DeleteLocalRef(dayU);
    }
    return weekdays;
}

static jobjectArray getLongWeekdayNames(JNIEnv* env, UResourceBundle* gregorian) {
    return getWeekdayNames(env, gregorian, true);
}

static jobjectArray getShortWeekdayNames(JNIEnv* env, UResourceBundle* gregorian) {
    return getWeekdayNames(env, gregorian, false);
}

static jstring getDecimalPatternChars(JNIEnv* env, UResourceBundle* rootElems) {
    UErrorCode status = U_ZERO_ERROR;

    int zeroL, digitL, decSepL, groupL, listL, percentL, permillL, expL, currSepL, minusL;

    const jchar* zero = ures_getStringByIndex(rootElems, 4, &zeroL, &status);
    const jchar* digit = ures_getStringByIndex(rootElems, 5, &digitL, &status);
    const jchar* decSep = ures_getStringByIndex(rootElems, 0, &decSepL, &status);
    const jchar* group = ures_getStringByIndex(rootElems, 1, &groupL, &status);
    const jchar* list = ures_getStringByIndex(rootElems, 2, &listL, &status);
    const jchar* percent = ures_getStringByIndex(rootElems, 3, &percentL, &status);
    const jchar* permill = ures_getStringByIndex(rootElems, 8, &permillL, &status);
    const jchar* exp = ures_getStringByIndex(rootElems, 7, &expL, &status);
    const jchar* currSep = ures_getStringByIndex(rootElems, 0, &currSepL, &status);
    const jchar* minus = ures_getStringByIndex(rootElems, 6, &minusL, &status);

    if (U_FAILURE(status)) {
        return NULL;
    }

    jchar patternChars[11];
    patternChars[0] = 0;

    u_strncat(patternChars, zero, 1);
    u_strncat(patternChars, digit, 1);
    u_strncat(patternChars, decSep, 1);
    u_strncat(patternChars, group, 1);
    u_strncat(patternChars, list, 1);
    u_strncat(patternChars, percent, 1);
    u_strncat(patternChars, permill, 1);
    u_strncat(patternChars, exp, 1);
    u_strncat(patternChars, currSep, 1);
    u_strncat(patternChars, minus, 1);

    return env->NewString(patternChars, 10);
}

static jstring getIntCurrencyCode(JNIEnv* env, jclass clazz, jstring locale) {
    const char* locStr = env->GetStringUTFChars(locale, NULL);
    char country[3] = {0,0,0};

    // getting the 2 character country name
    if(strlen(locStr) < 5) {
        env->ReleaseStringUTFChars(locale, locStr);
        return NULL;
    }
    if(locStr[3] < 'A' || locStr[3] > 'Z' || locStr[4] < 'A' || locStr[4] > 'Z') {
        env->ReleaseStringUTFChars(locale, locStr);
        return NULL;
    }
    country[0] = locStr[3];
    country[1] = locStr[4];

    env->ReleaseStringUTFChars(locale, locStr);

    return getCurrencyCodeNative(env, clazz, env->NewStringUTF(country));
}

static jobjectArray getContentImpl(JNIEnv* env, jclass clazz, jstring locale) {
    const char* loc = env->GetStringUTFChars(locale, NULL);
    UErrorCode status = U_ZERO_ERROR;
    UResourceBundle* root = ures_openU(NULL, loc, &status);
    env->ReleaseStringUTFChars(locale, loc);
    if (U_FAILURE(status)) {
        LOGE("Error getting resources: %s", u_errorName(status));
        status = U_ZERO_ERROR;
        return NULL;
    }

    jclass obj_class = env->FindClass("[Ljava/lang/Object;");
    jclass integer_class = env->FindClass("java/lang/Integer");
    jmethodID integerInit = env->GetMethodID(integer_class, "<init>", "(I)V");
    jobjectArray result;

    jobject firstDayOfWeek = NULL;
    jobject minimalDaysInFirstWeek = NULL;
    jobjectArray amPmMarkers = NULL;
    jobjectArray eras = NULL;
    jstring localPatternChars = NULL;
    jobjectArray weekdays = NULL;
    jobjectArray shortWeekdays = NULL;
    jobjectArray months = NULL;
    jobjectArray shortMonths = NULL;
    jstring time_SHORT = NULL;
    jstring time_MEDIUM = NULL;
    jstring time_LONG = NULL;
    jstring time_FULL = NULL;
    jstring date_SHORT = NULL;
    jstring date_MEDIUM = NULL;
    jstring date_LONG = NULL;
    jstring date_FULL = NULL;
    jstring decimalPatternChars = NULL;
    jstring naN = NULL;
    jstring infinity = NULL;
    jstring currencySymbol = NULL;
    jstring intCurrencySymbol = NULL;
    jstring numberPattern = NULL;
    jstring integerPattern = NULL;
    jstring currencyPattern = NULL;
    jstring percentPattern = NULL;
    jobjectArray zones = NULL;

    int counter = 0;


    const jchar* nan = NULL;
    const jchar* inf = NULL;
    int nanL, infL;


    UResourceBundle* gregorian;
    UResourceBundle* gregorianElems;
    UResourceBundle* rootElems;




    // get the resources needed
    rootElems = ures_getByKey(root, "calendar", NULL, &status);
    if(U_FAILURE(status)) {
        return NULL;
    }

    gregorian = ures_getByKey(rootElems, "gregorian", NULL, &status);
    if(U_FAILURE(status)) {
        ures_close(rootElems);
        return NULL;
    }



    // adding the first day of week and minimal days in first week values
    int firstDayVals[2] = {-1, -1};
    getDayIntVector(env, gregorian, firstDayVals);
    if((firstDayVals[0] != -1) && (firstDayVals[1] != -1)) {
        firstDayOfWeek = env->NewObject(integer_class, integerInit, firstDayVals[0]);
        minimalDaysInFirstWeek = env->NewObject(integer_class, integerInit, firstDayVals[1]);
        // adding First_Day and Minimal_Days integer to the result
        counter += 2;
    }


    // adding ampm string array to the result");
    amPmMarkers = getAmPmMarkers(env, gregorian);
    if(amPmMarkers != NULL) {
        counter++;
    }


    // adding eras string array to the result
    eras = getEras(env, gregorian);
    if(eras != NULL) {
        counter++;
    }


    // local pattern chars are initially always the same
    localPatternChars = env->NewStringUTF("GyMdkHmsSEDFwWahKzZ");
    // adding local pattern chars string to the result
    counter++;


    // adding month names string array to the result
    months = getLongMonthNames(env, gregorian);
    if(months != NULL) {
        counter++;
    }


    // adding short month names string array to the result
    shortMonths = getShortMonthNames(env, gregorian);
    if(shortMonths != NULL) {
        counter++;
    }


    // adding day names string array to the result
    weekdays = getLongWeekdayNames(env, gregorian);
    if(weekdays != NULL) {
        counter++;
    }


    // adding short day names string array to the result
    shortWeekdays = getShortWeekdayNames(env, gregorian);
    if(shortWeekdays != NULL) {
        counter++;
    }

    const UChar* pattern;
    jchar check[2] = {0, 0};
    u_uastrcpy(check, "v");
    jchar replacement[2] = {0, 0};
    u_uastrcpy(replacement, "z");
    jchar* pos;
    jchar* patternCopy;
    int patternLength;

    // adding date and time format patterns to the result
    gregorianElems = ures_getByKey(gregorian, "DateTimePatterns", NULL, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        goto endOfCalendar;
    }

    pattern = ures_getStringByIndex(gregorianElems, 0, &patternLength, &status);
    // there are some patterns in icu that use the pattern character 'v'
    // java doesn't accept this, so it gets replaced by 'z' which has
    // about the same result as 'v', the timezone name. 
    // 'v' -> "PT", 'z' -> "PST", v is the generic timezone and z the standard tz
    // "vvvv" -> "Pacific Time", "zzzz" -> "Pacific Standard Time"
    patternCopy = new jchar[patternLength + 1];
    u_strcpy(patternCopy, pattern);
    if(U_FAILURE(status)) {
        delete[] patternCopy;
        status = U_ZERO_ERROR;
        goto endOfCalendar;
    }
    while((pos = u_strchr(patternCopy, check[0])) != NULL) {
        u_memset(pos, replacement[0], 1);
    }
    time_FULL = env->NewString(patternCopy, patternLength);
    delete[] patternCopy;
    counter++;

    pattern = ures_getStringByIndex(gregorianElems, 1, &patternLength, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        goto endOfCalendar;
    }
    time_LONG = env->NewString(pattern, patternLength);
    counter++;

    pattern = ures_getStringByIndex(gregorianElems, 2, &patternLength, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        goto endOfCalendar;
    }
    time_MEDIUM = env->NewString(pattern, patternLength);
    counter++;

    pattern = ures_getStringByIndex(gregorianElems, 3, &patternLength, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        goto endOfCalendar;
    }
    time_SHORT = env->NewString(pattern, patternLength);
    counter++;

    pattern = ures_getStringByIndex(gregorianElems, 4, &patternLength, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        goto endOfCalendar;
    }
    date_FULL = env->NewString(pattern, patternLength);
    counter++;

    pattern = ures_getStringByIndex(gregorianElems, 5, &patternLength, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        goto endOfCalendar;
    }
    date_LONG = env->NewString(pattern, patternLength);
    counter++;

    pattern = ures_getStringByIndex(gregorianElems, 6, &patternLength, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        goto endOfCalendar;
    }
    date_MEDIUM = env->NewString(pattern, patternLength);
    counter++;

    pattern = ures_getStringByIndex(gregorianElems, 7, &patternLength, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        goto endOfCalendar;
    }
    date_SHORT = env->NewString(pattern, patternLength);
    counter++;


endOfCalendar:

    if(gregorianElems != NULL) {
        ures_close(gregorianElems);
    }
    ures_close(gregorian);
    ures_close(rootElems);


    rootElems = ures_getByKey(root, "NumberElements", NULL, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
    }

    if(ures_getSize(rootElems) >= 11) {

        // adding decimal pattern chars to the result
        decimalPatternChars = getDecimalPatternChars(env, rootElems);
        if(decimalPatternChars != NULL) {
            counter++;
        }

        // adding NaN pattern char to the result
        nan = ures_getStringByIndex(rootElems, 10, &nanL, &status);
        if(U_SUCCESS(status)) {
            naN = env->NewString(nan, nanL);
            counter++;
        }
        status = U_ZERO_ERROR;

        // adding infinity pattern char to the result
        inf = ures_getStringByIndex(rootElems, 9, &infL, &status);
        if(U_SUCCESS(status)) {
            infinity = env->NewString(inf, infL);
            counter++;
        }
        status = U_ZERO_ERROR;
    }

    ures_close(rootElems);


    // adding intl currency code to result
    intCurrencySymbol = getIntCurrencyCode(env, clazz, locale);
    if(intCurrencySymbol != NULL) {
        // adding currency symbol to result
        currencySymbol = getCurrencySymbolNative(env, clazz, locale, intCurrencySymbol);
        // TODO: this is broken; the two will never be identical *unless*
        // they're NULL. Given that string equality is hard here, and this
        // code has always been broken, does this matter?
        if (currencySymbol == intCurrencySymbol) {
            currencySymbol = NULL;
        }
    } else {
        intCurrencySymbol = env->NewStringUTF("XXX");
    }
    if(currencySymbol == NULL) {
        // creating a new string explicitly with the UTF-8 encoding of "\u00a4"
        currencySymbol = env->NewStringUTF("\xc2\xa4");
    }
    counter += 2;


    // adding number format patterns to the result
    int numOfEntries;
    int decSepOffset;
    NumberFormat* nf;
    jchar* tmpPattern;

    rootElems = ures_getByKey(root, "NumberPatterns", NULL, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        goto zones;
    }

    numOfEntries = ures_getSize(rootElems);
    if(numOfEntries < 3) {
        ures_close(rootElems);
        goto zones;
    }

    // number pattern
    pattern = ures_getStringByIndex(rootElems, 0, &patternLength, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        ures_close(rootElems);
        goto zones;
    }
    numberPattern = env->NewString(pattern, patternLength);
    counter++;

    // integer pattern derived from number pattern
    // We need to convert a C string literal to a UChar string for u_strcspn.
    static const char c_decSep[] = ".";
    UChar decSep[sizeof(c_decSep)];
    u_charsToUChars(c_decSep, decSep, sizeof(c_decSep));
    decSepOffset = u_strcspn(pattern, decSep);
    tmpPattern = new jchar[decSepOffset + 1];
    u_strncpy(tmpPattern, pattern, decSepOffset);
    integerPattern = env->NewString(tmpPattern, decSepOffset);
    delete[] tmpPattern;
    counter++;

    // currency pattern
    pattern = ures_getStringByIndex(rootElems, 1, &patternLength, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        ures_close(rootElems);
        goto zones;
    }
    currencyPattern = env->NewString(pattern, patternLength);
    counter++;

    // percent pattern
    pattern = ures_getStringByIndex(rootElems, 2, &patternLength, &status);
    if(U_FAILURE(status)) {
        status = U_ZERO_ERROR;
        ures_close(rootElems);
        goto zones;
    }
    percentPattern = env->NewString(pattern, patternLength);
    counter++;

    ures_close(rootElems);

zones:

    ures_close(root);


    // collect all content and put it into an array
    result = env->NewObjectArray(counter, obj_class, NULL);

    int index = 0;
    if(firstDayOfWeek != NULL && index < counter) {
        addObject(env, result, "First_Day", firstDayOfWeek, index++);
    }
    if(minimalDaysInFirstWeek != NULL && index < counter) {
        addObject(env, result, "Minimal_Days", minimalDaysInFirstWeek, index++);
    }
    if(amPmMarkers != NULL && index < counter) {
        addObject(env, result, "ampm", amPmMarkers, index++);
    }
    if(eras != NULL && index < counter) {
        addObject(env, result, "eras", eras, index++);
    }
    if(localPatternChars != NULL && index < counter) {
        addObject(env, result, "LocalPatternChars", localPatternChars, index++);
    }
    if(weekdays != NULL && index < counter) {
        addObject(env, result, "weekdays", weekdays, index++);
    }
    if(shortWeekdays != NULL && index < counter) {
        addObject(env, result, "shortWeekdays", shortWeekdays, index++);
    }
    if(months != NULL && index < counter) {
        addObject(env, result, "months", months, index++);
    }
    if(shortMonths != NULL && index < counter) {
        addObject(env, result, "shortMonths", shortMonths, index++);
    }
    if(time_SHORT != NULL && index < counter) {
        addObject(env, result, "Time_SHORT", time_SHORT, index++);
    }
    if(time_MEDIUM != NULL && index < counter) {
        addObject(env, result, "Time_MEDIUM", time_MEDIUM, index++);
    }
    if(time_LONG != NULL && index < counter) {
        addObject(env, result, "Time_LONG", time_LONG, index++);
    }
    if(time_FULL != NULL && index < counter) {
        addObject(env, result, "Time_FULL", time_FULL, index++);
    }
    if(date_SHORT != NULL && index < counter) {
        addObject(env, result, "Date_SHORT", date_SHORT, index++);
    }
    if(date_MEDIUM != NULL && index < counter) {
        addObject(env, result, "Date_MEDIUM", date_MEDIUM, index++);
    }
    if(date_LONG != NULL && index < counter) {
        addObject(env, result, "Date_LONG", date_LONG, index++);
    }
    if(date_FULL != NULL && index < counter) {
        addObject(env, result, "Date_FULL", date_FULL, index++);
    }
    if(decimalPatternChars != NULL && index < counter) {
        addObject(env, result, "DecimalPatternChars", decimalPatternChars, index++);
    }
    if(naN != NULL && index < counter) {
        addObject(env, result, "NaN", naN, index++);
    }
    if(infinity != NULL && index < counter) {
        addObject(env, result, "Infinity", infinity, index++);
    }
    if(currencySymbol != NULL && index < counter) {
        addObject(env, result, "CurrencySymbol", currencySymbol, index++);
    }
    if(intCurrencySymbol != NULL && index < counter) {
        addObject(env, result, "IntCurrencySymbol", intCurrencySymbol, index++);
    }
    if(numberPattern != NULL && index < counter) {
        addObject(env, result, "Number", numberPattern, index++);
    }
    if(integerPattern != NULL && index < counter) {
        addObject(env, result, "Integer", integerPattern, index++);
    }
    if(currencyPattern != NULL && index < counter) {
        addObject(env, result, "Currency", currencyPattern, index++);
    }
    if(percentPattern != NULL && index < counter) {
        addObject(env, result, "Percent", percentPattern, index++);
    }

    return result;

}

static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    {"getCurrencyFractionDigitsNative", "(Ljava/lang/String;)I",
            (void*) getCurrencyFractionDigitsNative},
    {"getCurrencyCodeNative", "(Ljava/lang/String;)Ljava/lang/String;",
            (void*) getCurrencyCodeNative},
    {"getCurrencySymbolNative", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            (void*) getCurrencySymbolNative},
    {"getDisplayCountryNative",
            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            (void*) getDisplayCountryNative},
    {"getDisplayLanguageNative",
            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            (void*) getDisplayLanguageNative},
    {"getDisplayVariantNative",
            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            (void*) getDisplayVariantNative},
    {"getISO3CountryNative",
            "(Ljava/lang/String;)Ljava/lang/String;",
            (void*) getISO3CountryNative},
    {"getISO3LanguageNative",
            "(Ljava/lang/String;)Ljava/lang/String;",
            (void*) getISO3LanguageNative},
    {"getISOCountriesNative", "()[Ljava/lang/String;",
            (void*) getISOCountriesNative},
    {"getISOLanguagesNative", "()[Ljava/lang/String;",
            (void*) getISOLanguagesNative},
    {"getAvailableLocalesNative", "()[Ljava/lang/String;",
            (void*) getAvailableLocalesNative},
    {"getTimeZonesNative",
            "([[Ljava/lang/String;Ljava/lang/String;)V",
            (void*) getTimeZonesNative},
    {"getDisplayTimeZoneNative",
            "(Ljava/lang/String;ZILjava/lang/String;)Ljava/lang/String;",
            (void*) getDisplayTimeZoneNative},
    {"getContentImpl",
            "(Ljava/lang/String;)[[Ljava/lang/Object;",
            (void*) getContentImpl},
};

int register_com_ibm_icu4jni_util_Resources(JNIEnv* env) {
    jclass stringclass = env->FindClass("java/lang/String");
    if (stringclass == NULL) {
        return -1;
    }
    string_class = (jclass) env->NewGlobalRef(stringclass);

    return jniRegisterNativeMethods(env, "com/ibm/icu4jni/util/Resources",
            gMethods, NELEM(gMethods));
}
