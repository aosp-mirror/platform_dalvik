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
/*
 * Set up values for System.getProperties().
 */
#include "Dalvik.h"

#include <stdlib.h>
#include <sys/utsname.h>
#include <limits.h>
#include <unistd.h>

/*
 * Create some storage for properties read from the command line.
 */
bool dvmPropertiesStartup(int maxProps)
{
    gDvm.maxProps = maxProps;
    if (maxProps > 0) {
        gDvm.propList = (char**) malloc(maxProps * sizeof(char*));
        if (gDvm.propList == NULL)
            return false;
    }
    gDvm.numProps = 0;

    return true;
}

/*
 * Clean up.
 */
void dvmPropertiesShutdown(void)
{
    int i;

    for (i = 0; i < gDvm.numProps; i++)
        free(gDvm.propList[i]);
    free(gDvm.propList);
    gDvm.propList = NULL;
}

/*
 * Add a property specified on the command line.  "argStr" has the form
 * "name=value".  "name" must have nonzero length.
 *
 * Returns "true" if argStr appears valid.
 */
bool dvmAddCommandLineProperty(const char* argStr)
{
    char* mangle;
    char* equals;

    mangle = strdup(argStr);
    equals = strchr(mangle, '=');
    if (equals == NULL || equals == mangle) {
        free(mangle);
        return false;
    }
    *equals = '\0';

    assert(gDvm.numProps < gDvm.maxProps);
    gDvm.propList[gDvm.numProps++] = mangle;

    return true;
}


/*
 * Find the "put" method for this class.
 *
 * Returns NULL and throws an exception if not found.
 */
static Method* getPut(ClassObject* clazz)
{
    Method* put;

    put = dvmFindVirtualMethodHierByDescriptor(clazz, "setProperty",
            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
    if (put == NULL) {
        dvmThrowException("Ljava/lang/RuntimeException;",
            "could not find setProperty(String,String) in Properties");
        /* fall through to return */
    }
    return put;
}

/*
 * Set the value of the property.
 */
static void setProperty(Object* propObj, Method* put, const char* key,
    const char* value)
{
    StringObject* keyStr;
    StringObject* valueStr;

    if (value == NULL) {
        /* unclear what to do; probably want to create prop w/ empty string */
        value = "";
    }

    keyStr = dvmCreateStringFromCstr(key);
    valueStr = dvmCreateStringFromCstr(value);
    if (keyStr == NULL || valueStr == NULL) {
        LOGW("setProperty string creation failed\n");
        goto bail;
    }

    JValue unused;
    dvmCallMethod(dvmThreadSelf(), put, propObj, &unused, keyStr, valueStr);

bail:
    dvmReleaseTrackedAlloc((Object*) keyStr, NULL);
    dvmReleaseTrackedAlloc((Object*) valueStr, NULL);
}

/*
 * Create the VM-default system properties.
 *
 * We can do them here, or do them in interpreted code with lots of native
 * methods to get bits and pieces.  This is a bit smaller.
 */
void dvmCreateDefaultProperties(Object* propObj)
{
    Method* put = getPut(propObj->clazz);

    if (put == NULL)
        return;

    struct utsname info;
    uname(&info);

    /* constant strings that are used multiple times below */
    const char *projectUrl = "http://www.android.com/";
    const char *projectName = "The Android Project";

    /*
     * These are listed in the docs.
     */

    setProperty(propObj, put, "java.boot.class.path", gDvm.bootClassPathStr);
    setProperty(propObj, put, "java.class.path", gDvm.classPathStr);
    setProperty(propObj, put, "java.class.version", "46.0");
    setProperty(propObj, put, "java.compiler", "");
    setProperty(propObj, put, "java.ext.dirs", "");

    if (getenv("JAVA_HOME") != NULL) {
        setProperty(propObj, put, "java.home", getenv("JAVA_HOME"));
    } else {
        setProperty(propObj, put, "java.home", "/system");
    }

    setProperty(propObj, put, "java.io.tmpdir", "/tmp");
    setProperty(propObj, put, "java.library.path", getenv("LD_LIBRARY_PATH"));

    setProperty(propObj, put, "java.net.preferIPv6Addresses", "true");

    setProperty(propObj, put, "java.vendor", projectName);
    setProperty(propObj, put, "java.vendor.url", projectUrl);
    setProperty(propObj, put, "java.version", "0");
    setProperty(propObj, put, "java.vm.name", "Dalvik");
    setProperty(propObj, put, "java.vm.specification.name",
            "Dalvik Virtual Machine Specification");
    setProperty(propObj, put, "java.vm.specification.vendor", projectName);
    setProperty(propObj, put, "java.vm.specification.version", "0.9");
    setProperty(propObj, put, "java.vm.vendor", projectName);

    char tmpBuf[64];
    sprintf(tmpBuf, "%d.%d.%d",
        DALVIK_MAJOR_VERSION, DALVIK_MINOR_VERSION, DALVIK_BUG_VERSION);
    setProperty(propObj, put, "java.vm.version", tmpBuf);

    setProperty(propObj, put, "java.specification.name",
            "Dalvik Core Library");
    setProperty(propObj, put, "java.specification.vendor", projectName);
    setProperty(propObj, put, "java.specification.version", "0.9");

    setProperty(propObj, put, "os.arch", info.machine);
    setProperty(propObj, put, "os.name", info.sysname);
    setProperty(propObj, put, "os.version", info.release);
    setProperty(propObj, put, "user.home", getenv("HOME"));
    setProperty(propObj, put, "user.name", getenv("USER"));

    char path[PATH_MAX];
    setProperty(propObj, put, "user.dir", getcwd(path, sizeof(path)));

    setProperty(propObj, put, "file.separator", "/");
    setProperty(propObj, put, "line.separator", "\n");
    setProperty(propObj, put, "path.separator", ":");

    /*
     * These show up elsewhere, so do them here too.
     */
    setProperty(propObj, put, "java.runtime.name", "Android Runtime");
    setProperty(propObj, put, "java.runtime.version", "0.9");
    setProperty(propObj, put, "java.vm.vendor.url", projectUrl);

    setProperty(propObj, put, "file.encoding", "UTF-8");
    setProperty(propObj, put, "user.language", "en");
    setProperty(propObj, put, "user.region", "US");

    /*
     * These are unique to Android/Dalvik.
     */
    setProperty(propObj, put, "android.vm.dexfile", "true");
}

/*
 * Add anything specified on the command line.
 */
void dvmSetCommandLineProperties(Object* propObj)
{
    Method* put = getPut(propObj->clazz);
    int i;

    if (put == NULL)
        return;

    for (i = 0; i < gDvm.numProps; i++) {
        const char* value;

        /* value starts after the end of the key string */
        for (value = gDvm.propList[i]; *value != '\0'; value++)
            ;
        setProperty(propObj, put, gDvm.propList[i], value+1);
    }
}

/*
 * Get a property by calling System.getProperty(key).
 *
 * Returns a newly-allocated string, or NULL on failure or key not found.
 * (Unexpected failures will also raise an exception.)
 */
char* dvmGetProperty(const char* key)
{
    ClassObject* system;
    Method* getProp;
    StringObject* keyObj = NULL;
    StringObject* valueObj;
    char* result = NULL;

    assert(key != NULL);

    system = dvmFindSystemClass("Ljava/lang/System;");
    if (system == NULL)
        goto bail;

    getProp = dvmFindDirectMethodByDescriptor(system, "getProperty",
        "(Ljava/lang/String;)Ljava/lang/String;");
    if (getProp == NULL) {
        LOGW("Could not find getProperty(String) in java.lang.System\n");
        goto bail;
    }

    keyObj = dvmCreateStringFromCstr(key);
    if (keyObj == NULL)
        goto bail;

    JValue val;
    dvmCallMethod(dvmThreadSelf(), getProp, NULL, &val, keyObj);
    valueObj = (StringObject*) val.l;
    if (valueObj == NULL)
        goto bail;

    result = dvmCreateCstrFromString(valueObj);
    /* fall through with result */

bail:
    dvmReleaseTrackedAlloc((Object*)keyObj, NULL);
    return result;
}
