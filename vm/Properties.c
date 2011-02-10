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
    if (mangle == NULL) {
        return false;
    }
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
 * Find the "System.setProperty" method.
 *
 * Returns NULL and throws an exception if not found.
 */
static Method* findSetProperty(ClassObject* clazz)
{
    Method* put = dvmFindVirtualMethodHierByDescriptor(clazz, "setProperty",
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
 * Fills the passed-in java.util.Properties with stuff only the VM knows, such
 * as the VM's exact version and properties set on the command-line with -D.
 */
void dvmInitVmSystemProperties(Object* propObj)
{
    Method* put = findSetProperty(propObj->clazz);
    int i;
    struct utsname info;
    char tmpBuf[64];
    char path[PATH_MAX];

    if (put == NULL)
        return;

    /*
     * TODO: these are currently awkward to do in Java so we sneak them in
     * here. Only java.vm.version really needs to be in Dalvik.
     */
    setProperty(propObj, put, "java.boot.class.path", gDvm.bootClassPathStr);
    setProperty(propObj, put, "java.class.path", gDvm.classPathStr);
    sprintf(tmpBuf, "%d.%d.%d",
            DALVIK_MAJOR_VERSION, DALVIK_MINOR_VERSION, DALVIK_BUG_VERSION);
    setProperty(propObj, put, "java.vm.version", tmpBuf);
    uname(&info);
    setProperty(propObj, put, "os.arch", info.machine);
    setProperty(propObj, put, "os.name", info.sysname);
    setProperty(propObj, put, "os.version", info.release);
    setProperty(propObj, put, "user.dir", getcwd(path, sizeof(path)));

    /*
     * Properties set on the command-line with -D.
     */
    for (i = 0; i < gDvm.numProps; i++) {
        const char* value;

        /* value starts after the end of the key string */
        for (value = gDvm.propList[i]; *value != '\0'; value++)
            ;
        setProperty(propObj, put, gDvm.propList[i], value+1);
    }
}
