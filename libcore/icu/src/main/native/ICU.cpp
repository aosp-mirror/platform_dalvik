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

#define LOG_TAG "ICU"

#include "JNIHelp.h"
#include "ScopedUtfChars.h"
#include "UniquePtr.h"
#include "cutils/log.h"
#include "unicode/numfmt.h"
#include "unicode/locid.h"
#include "unicode/ubrk.h"
#include "unicode/ucal.h"
#include "unicode/ucol.h"
#include "unicode/udat.h"
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

    // Disallow copy and assignment.
    ScopedResourceBundle(const ScopedResourceBundle&);
    void operator=(const ScopedResourceBundle&);
};

static Locale getLocale(JNIEnv* env, jstring localeName) {
    return Locale::createFromName(ScopedUtfChars(env, localeName).data());
}

static jint getCurrencyFractionDigitsNative(JNIEnv* env, jclass, jstring currencyCode) {
    UErrorCode status = U_ZERO_ERROR;
    UniquePtr<NumberFormat> fmt(NumberFormat::createCurrencyInstance(status));
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
    return fmt->getMinimumFractionDigits(); 
}

static jstring getCurrencyCodeNative(JNIEnv* env, jclass, jstring key) {
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

    // check if there is a 'to' date. If there is, the currency isn't used anymore.
    ScopedResourceBundle currencyTo(ures_getByKey(currencyElem.get(), "to", NULL, &status));
    if (!U_FAILURE(status)) {
        // return and let the caller throw an exception
        return NULL;
    }
    // We need to reset 'status'. It works like errno in that ICU doesn't set it
    // to U_ZERO_ERROR on success: it only touches it on error, and the test
    // above means it now holds a failure code.
    status = U_ZERO_ERROR;

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

static jstring getCurrencySymbolNative(JNIEnv* env, jclass, jstring locale, jstring currencyCode) {
    // LOGI("ENTER getCurrencySymbolNative");

    const char* locName = env->GetStringUTFChars(locale, NULL);
    UErrorCode status = U_ZERO_ERROR;
    ScopedResourceBundle root(ures_open(NULL, locName, &status));
    env->ReleaseStringUTFChars(locale, locName);
    if (U_FAILURE(status)) {
        return NULL;
    }

    ScopedResourceBundle currencies(ures_getByKey(root.get(), "Currencies", NULL, &status));
    if (U_FAILURE(status)) {
        return NULL;
    }

    const char* currName = env->GetStringUTFChars(currencyCode, NULL);
    ScopedResourceBundle currencyElems(ures_getByKey(currencies.get(), currName, NULL, &status));
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

static jstring getDisplayCountryNative(JNIEnv* env, jclass, jstring targetLocale, jstring locale) {

    Locale loc = getLocale(env, locale);
    Locale targetLoc = getLocale(env, targetLocale);

    UnicodeString str;
    targetLoc.getDisplayCountry(loc, str);
    return env->NewString(str.getBuffer(), str.length());
}

static jstring getDisplayLanguageNative(JNIEnv* env, jclass, jstring targetLocale, jstring locale) {

    Locale loc = getLocale(env, locale);
    Locale targetLoc = getLocale(env, targetLocale);

    UnicodeString str;
    targetLoc.getDisplayLanguage(loc, str);
    return env->NewString(str.getBuffer(), str.length());
}

static jstring getDisplayVariantNative(JNIEnv* env, jclass, jstring targetLocale, jstring locale) {
    Locale loc = getLocale(env, locale);
    Locale targetLoc = getLocale(env, targetLocale);
    UnicodeString str;
    targetLoc.getDisplayVariant(loc, str);
    return env->NewString(str.getBuffer(), str.length());
}

static jstring getISO3CountryNative(JNIEnv* env, jclass, jstring locale) {
    Locale loc = getLocale(env, locale);
    return env->NewStringUTF(loc.getISO3Country());
}

static jstring getISO3LanguageNative(JNIEnv* env, jclass, jstring locale) {
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

static jobjectArray getISOCountriesNative(JNIEnv* env, jclass) {
    return toStringArray(env, Locale::getISOCountries());
}

static jobjectArray getISOLanguagesNative(JNIEnv* env, jclass) {
    return toStringArray(env, Locale::getISOLanguages());
}

template <typename Counter, typename Getter>
static jobjectArray getAvailableLocales(JNIEnv* env, Counter* counter, Getter* getter) {
    size_t count = (*counter)();
    jobjectArray result = env->NewObjectArray(count, string_class, NULL);
    for (size_t i = 0; i < count; ++i) {
        jstring s = env->NewStringUTF((*getter)(i));
        env->SetObjectArrayElement(result, i, s);
        env->DeleteLocalRef(s);
    }
    return result;
}

static jobjectArray getAvailableLocalesNative(JNIEnv* env, jclass) {
    return getAvailableLocales(env, uloc_countAvailable, uloc_getAvailable);
}

static jobjectArray getAvailableBreakIteratorLocalesNative(JNIEnv* env, jclass) {
    return getAvailableLocales(env, ubrk_countAvailable, ubrk_getAvailable);
}

static jobjectArray getAvailableCalendarLocalesNative(JNIEnv* env, jclass) {
    return getAvailableLocales(env, ucal_countAvailable, ucal_getAvailable);
}

static jobjectArray getAvailableCollatorLocalesNative(JNIEnv* env, jclass) {
    return getAvailableLocales(env, ucol_countAvailable, ucol_getAvailable);
}

static jobjectArray getAvailableDateFormatLocalesNative(JNIEnv* env, jclass) {
    return getAvailableLocales(env, udat_countAvailable, udat_getAvailable);
}

static jobjectArray getAvailableNumberFormatLocalesNative(JNIEnv* env, jclass) {
    return getAvailableLocales(env, unum_countAvailable, unum_getAvailable);
}

static TimeZone* timeZoneFromId(JNIEnv* env, jstring id) {
    const jchar* chars = env->GetStringChars(id, NULL);
    const UnicodeString zoneID(reinterpret_cast<const UChar*>(chars), env->GetStringLength(id));
    env->ReleaseStringChars(id, chars);
    return TimeZone::createTimeZone(zoneID);
}

static jstring formatDate(JNIEnv* env, const SimpleDateFormat& fmt, const UDate& when) {
    UnicodeString str;
    fmt.format(when, str);
    return env->NewString(str.getBuffer(), str.length());
}

static void getTimeZonesNative(JNIEnv* env, jclass, jobjectArray outerArray, jstring locale) {
    // get all timezone objects
    jobjectArray zoneIdArray = (jobjectArray) env->GetObjectArrayElement(outerArray, 0);
    int count = env->GetArrayLength(zoneIdArray);
    TimeZone* zones[count];
    for(int i = 0; i < count; i++) {
        jstring id = (jstring) env->GetObjectArrayElement(zoneIdArray, i);
        zones[i] = timeZoneFromId(env, id);
        env->DeleteLocalRef(id);
    }

    Locale loc = getLocale(env, locale);

    UErrorCode status = U_ZERO_ERROR;
    UnicodeString longPattern("zzzz","");
    SimpleDateFormat longFormat(longPattern, loc, status);
    UnicodeString shortPattern("z","");
    SimpleDateFormat shortFormat(shortPattern, loc, status);

    jobjectArray longStdTimeArray = (jobjectArray) env->GetObjectArrayElement(outerArray, 1);
    jobjectArray shortStdTimeArray = (jobjectArray) env->GetObjectArrayElement(outerArray, 2);
    jobjectArray longDlTimeArray = (jobjectArray) env->GetObjectArrayElement(outerArray, 3);
    jobjectArray shortDlTimeArray = (jobjectArray) env->GetObjectArrayElement(outerArray, 4);

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

        jstring content = formatDate(env, shortFormat, daylightSavingDate);
        env->SetObjectArrayElement(shortDlTimeArray, i, content);
        env->DeleteLocalRef(content);

        content = formatDate(env, shortFormat, standardDate);
        env->SetObjectArrayElement(shortStdTimeArray, i, content);
        env->DeleteLocalRef(content);

        content = formatDate(env, longFormat, daylightSavingDate);
        env->SetObjectArrayElement(longDlTimeArray, i, content);
        env->DeleteLocalRef(content);

        content = formatDate(env, longFormat, standardDate);
        env->SetObjectArrayElement(longStdTimeArray, i, content);
        env->DeleteLocalRef(content);

        delete tz;
    }
}

static jstring getDisplayTimeZoneNative(JNIEnv* env, jclass, jstring zoneId, jboolean isDST, jint style, jstring localeId) {
    UniquePtr<TimeZone> zone(timeZoneFromId(env, zoneId));
    Locale locale = getLocale(env, localeId);
    // Try to get the display name of the TimeZone according to the Locale
    UnicodeString displayName;
    zone->getDisplayName((UBool)isDST, (style == 0 ? TimeZone::SHORT : TimeZone::LONG), locale, displayName);
    return env->NewString(displayName.getBuffer(), displayName.length());
}

static bool getDayIntVector(JNIEnv* env, UResourceBundle* gregorian, int* values) {
    // get the First day of week and the minimal days in first week numbers
    UErrorCode status = U_ZERO_ERROR;
    ScopedResourceBundle gregorianElems(ures_getByKey(gregorian, "DateTimeElements", NULL, &status));
    if (U_FAILURE(status)) {
        return false;
    }

    int intVectSize;
    const int* result = ures_getIntVector(gregorianElems.get(), &intVectSize, &status);
    if (U_FAILURE(status) || intVectSize != 2) {
        return false;
    }

    values[0] = result[0];
    values[1] = result[1];
    return true;
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

static jstring getIntCurrencyCode(JNIEnv* env, jstring locale) {
    ScopedUtfChars localeChars(env, locale);

    // Extract the 2-character country name.
    if (strlen(localeChars.data()) < 5) {
        return NULL;
    }
    if (localeChars[3] < 'A' || localeChars[3] > 'Z' || localeChars[4] < 'A' || localeChars[4] > 'Z') {
        return NULL;
    }

    char country[3] = { localeChars[3], localeChars[4], 0 };
    return getCurrencyCodeNative(env, NULL, env->NewStringUTF(country));
}

static void setIntegerField(JNIEnv* env, jobject obj, const char* fieldName, int value) {
    // Convert our int to a java.lang.Integer.
    // TODO: switch to Integer.valueOf, add error checking.
    jclass integerClass = env->FindClass("java/lang/Integer");
    jmethodID constructor = env->GetMethodID(integerClass, "<init>", "(I)V");
    jobject integerValue = env->NewObject(integerClass, constructor, value);
    // Set the field.
    jclass localeDataClass = env->FindClass("com/ibm/icu4jni/util/LocaleData");
    jfieldID fid = env->GetFieldID(localeDataClass, fieldName, "Ljava/lang/Integer;");
    env->SetObjectField(obj, fid, integerValue);
}

static void setStringField(JNIEnv* env, jobject obj, const char* fieldName, jstring value) {
    jclass localeDataClass = env->FindClass("com/ibm/icu4jni/util/LocaleData");
    jfieldID fid = env->GetFieldID(localeDataClass, fieldName, "Ljava/lang/String;");
    env->SetObjectField(obj, fid, value);
}

static void setStringArrayField(JNIEnv* env, jobject obj, const char* fieldName, jobjectArray value) {
    jclass localeDataClass = env->FindClass("com/ibm/icu4jni/util/LocaleData");
    jfieldID fid = env->GetFieldID(localeDataClass, fieldName, "[Ljava/lang/String;");
    env->SetObjectField(obj, fid, value);
}

static void setStringField(JNIEnv* env, jobject obj, const char* fieldName, UResourceBundle* bundle, int index) {
    UErrorCode status = U_ZERO_ERROR;
    int charCount;
    const UChar* chars = ures_getStringByIndex(bundle, index, &charCount, &status);
    if (U_SUCCESS(status)) {
        setStringField(env, obj, fieldName, env->NewString(chars, charCount));
    } else {
        LOGE("Error setting String field %s from ICU resource: %s", fieldName, u_errorName(status));
    }
}

static void setCharField(JNIEnv* env, jobject obj, const char* fieldName, UResourceBundle* bundle, int index) {
    UErrorCode status = U_ZERO_ERROR;
    int charCount;
    const UChar* chars = ures_getStringByIndex(bundle, index, &charCount, &status);
    if (U_SUCCESS(status)) {
        jclass localeDataClass = env->FindClass("com/ibm/icu4jni/util/LocaleData");
        jfieldID fid = env->GetFieldID(localeDataClass, fieldName, "C");
        env->SetCharField(obj, fid, chars[0]);
    } else {
        LOGE("Error setting char field %s from ICU resource: %s", fieldName, u_errorName(status));
    }
}

static jboolean initLocaleDataImpl(JNIEnv* env, jclass, jstring locale, jobject localeData) {
    const char* loc = env->GetStringUTFChars(locale, NULL);
    UErrorCode status = U_ZERO_ERROR;
    ScopedResourceBundle root(ures_openU(NULL, loc, &status));
    env->ReleaseStringUTFChars(locale, loc);
    if (U_FAILURE(status)) {
        LOGE("Error getting ICU resource bundle: %s", u_errorName(status));
        status = U_ZERO_ERROR;
        return JNI_FALSE;
    }

    ScopedResourceBundle calendar(ures_getByKey(root.get(), "calendar", NULL, &status));
    if (U_FAILURE(status)) {
        LOGE("Error getting ICU calendar resource bundle: %s", u_errorName(status));
        return JNI_FALSE;
    }

    ScopedResourceBundle gregorian(ures_getByKey(calendar.get(), "gregorian", NULL, &status));
    if (U_FAILURE(status)) {
        LOGE("Error getting ICU gregorian resource bundle: %s", u_errorName(status));
        return JNI_FALSE;
    }

    int firstDayVals[2];
    if (getDayIntVector(env, gregorian.get(), firstDayVals)) {
        setIntegerField(env, localeData, "firstDayOfWeek", firstDayVals[0]);
        setIntegerField(env, localeData, "minimalDaysInFirstWeek", firstDayVals[1]);
    }

    setStringArrayField(env, localeData, "amPm", getAmPmMarkers(env, gregorian.get()));
    setStringArrayField(env, localeData, "eras", getEras(env, gregorian.get()));

    setStringArrayField(env, localeData, "longMonthNames", getLongMonthNames(env, gregorian.get()));
    setStringArrayField(env, localeData, "shortMonthNames", getShortMonthNames(env, gregorian.get()));
    setStringArrayField(env, localeData, "longWeekdayNames", getLongWeekdayNames(env, gregorian.get()));
    setStringArrayField(env, localeData, "shortWeekdayNames", getShortWeekdayNames(env, gregorian.get()));

    ScopedResourceBundle gregorianElems(ures_getByKey(gregorian.get(), "DateTimePatterns", NULL, &status));
    if (U_SUCCESS(status)) {
        setStringField(env, localeData, "fullTimeFormat", gregorianElems.get(), 0);
        setStringField(env, localeData, "longTimeFormat", gregorianElems.get(), 1);
        setStringField(env, localeData, "mediumTimeFormat", gregorianElems.get(), 2);
        setStringField(env, localeData, "shortTimeFormat", gregorianElems.get(), 3);
        setStringField(env, localeData, "fullDateFormat", gregorianElems.get(), 4);
        setStringField(env, localeData, "longDateFormat", gregorianElems.get(), 5);
        setStringField(env, localeData, "mediumDateFormat", gregorianElems.get(), 6);
        setStringField(env, localeData, "shortDateFormat", gregorianElems.get(), 7);
    }
    status = U_ZERO_ERROR;

    ScopedResourceBundle numberElements(ures_getByKey(root.get(), "NumberElements", NULL, &status));
    if (U_SUCCESS(status) && ures_getSize(numberElements.get()) >= 11) {
        setCharField(env, localeData, "zeroDigit", numberElements.get(), 4);
        setCharField(env, localeData, "digit", numberElements.get(), 5);
        setCharField(env, localeData, "decimalSeparator", numberElements.get(), 0);
        setCharField(env, localeData, "groupingSeparator", numberElements.get(), 1);
        setCharField(env, localeData, "patternSeparator", numberElements.get(), 2);
        setCharField(env, localeData, "percent", numberElements.get(), 3);
        setCharField(env, localeData, "perMill", numberElements.get(), 8);
        setCharField(env, localeData, "monetarySeparator", numberElements.get(), 0);
        setCharField(env, localeData, "minusSign", numberElements.get(), 6);
        setStringField(env, localeData, "exponentSeparator", numberElements.get(), 7);
        setStringField(env, localeData, "infinity", numberElements.get(), 9);
        setStringField(env, localeData, "NaN", numberElements.get(), 10);
    }
    status = U_ZERO_ERROR;

    jstring internationalCurrencySymbol = getIntCurrencyCode(env, locale);
    jstring currencySymbol = NULL;
    if (internationalCurrencySymbol != NULL) {
        currencySymbol = getCurrencySymbolNative(env, NULL, locale, internationalCurrencySymbol);
    } else {
        internationalCurrencySymbol = env->NewStringUTF("XXX");
    }
    if (currencySymbol == NULL) {
        // This is the UTF-8 encoding of U+00A4 (CURRENCY SIGN).
        currencySymbol = env->NewStringUTF("\xc2\xa4");
    }
    setStringField(env, localeData, "currencySymbol", currencySymbol);
    setStringField(env, localeData, "internationalCurrencySymbol", internationalCurrencySymbol);

    ScopedResourceBundle numberPatterns(ures_getByKey(root.get(), "NumberPatterns", NULL, &status));
    if (U_SUCCESS(status) && ures_getSize(numberPatterns.get()) >= 3) {
        setStringField(env, localeData, "numberPattern", numberPatterns.get(), 0);
        setStringField(env, localeData, "currencyPattern", numberPatterns.get(), 1);
        setStringField(env, localeData, "percentPattern", numberPatterns.get(), 2);
    }

    return JNI_TRUE;
}

static JNINativeMethod gMethods[] = {
    {"getAvailableBreakIteratorLocalesNative", "()[Ljava/lang/String;", (void*) getAvailableBreakIteratorLocalesNative},
    {"getAvailableCalendarLocalesNative", "()[Ljava/lang/String;", (void*) getAvailableCalendarLocalesNative},
    {"getAvailableCollatorLocalesNative", "()[Ljava/lang/String;", (void*) getAvailableCollatorLocalesNative},
    {"getAvailableDateFormatLocalesNative", "()[Ljava/lang/String;", (void*) getAvailableDateFormatLocalesNative},
    {"getAvailableLocalesNative", "()[Ljava/lang/String;", (void*) getAvailableLocalesNative},
    {"getAvailableNumberFormatLocalesNative", "()[Ljava/lang/String;", (void*) getAvailableNumberFormatLocalesNative},
    {"getCurrencyCodeNative", "(Ljava/lang/String;)Ljava/lang/String;", (void*) getCurrencyCodeNative},
    {"getCurrencyFractionDigitsNative", "(Ljava/lang/String;)I", (void*) getCurrencyFractionDigitsNative},
    {"getCurrencySymbolNative", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", (void*) getCurrencySymbolNative},
    {"getDisplayCountryNative", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", (void*) getDisplayCountryNative},
    {"getDisplayLanguageNative", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", (void*) getDisplayLanguageNative},
    {"getDisplayTimeZoneNative", "(Ljava/lang/String;ZILjava/lang/String;)Ljava/lang/String;", (void*) getDisplayTimeZoneNative},
    {"getDisplayVariantNative", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", (void*) getDisplayVariantNative},
    {"getISO3CountryNative", "(Ljava/lang/String;)Ljava/lang/String;", (void*) getISO3CountryNative},
    {"getISO3LanguageNative", "(Ljava/lang/String;)Ljava/lang/String;", (void*) getISO3LanguageNative},
    {"getISOCountriesNative", "()[Ljava/lang/String;", (void*) getISOCountriesNative},
    {"getISOLanguagesNative", "()[Ljava/lang/String;", (void*) getISOLanguagesNative},
    {"getTimeZonesNative", "([[Ljava/lang/String;Ljava/lang/String;)V", (void*) getTimeZonesNative},
    {"initLocaleDataImpl", "(Ljava/lang/String;Lcom/ibm/icu4jni/util/LocaleData;)Z", (void*) initLocaleDataImpl},
};
int register_com_ibm_icu4jni_util_Resources(JNIEnv* env) {
    jclass stringclass = env->FindClass("java/lang/String");
    if (stringclass == NULL) {
        return -1;
    }
    string_class = (jclass) env->NewGlobalRef(stringclass);

    return jniRegisterNativeMethods(env, "com/ibm/icu4jni/util/ICU", gMethods, NELEM(gMethods));
}
