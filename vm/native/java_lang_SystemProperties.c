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
 * java.lang.SystemProperties
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"


/*
 * Expected call sequence:
 *  (1) call SystemProperties.preInit() to get VM defaults
 *  (2) set any higher-level defaults
 *  (3) call SystemProperties.postInit() to get command-line overrides
 * This currently happens the first time somebody tries to access a property.
 *
 * SystemProperties is a Dalvik-specific package-scope class.
 */

/*
 * void preInit()
 *
 * Tells the VM to populate the properties table with VM defaults.
 */
static void Dalvik_java_lang_SystemProperties_preInit(const u4* args,
    JValue* pResult)
{
    dvmCreateDefaultProperties((Object*) args[0]);
    RETURN_VOID();
}

/*
 * void postInit()
 *
 * Tells the VM to update properties with values from the command line.
 */
static void Dalvik_java_lang_SystemProperties_postInit(const u4* args,
    JValue* pResult)
{
    dvmSetCommandLineProperties((Object*) args[0]);
    RETURN_VOID();
}

const DalvikNativeMethod dvm_java_lang_SystemProperties[] = {
    { "preInit",            "()V",
        Dalvik_java_lang_SystemProperties_preInit },
    { "postInit",           "()V",
        Dalvik_java_lang_SystemProperties_postInit },
    { NULL, NULL, NULL },
};
