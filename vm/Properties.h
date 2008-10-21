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
 * Support for System.getProperties().
 */
#ifndef _DALVIK_PROPERTIES
#define _DALVIK_PROPERTIES

/*
 * Initialization.
 */
bool dvmPropertiesStartup(int maxProps);
void dvmPropertiesShutdown(void);

/* add "-D" option to list */
bool dvmAddCommandLineProperty(const char* argStr);

/* called during property initialization */
void dvmCreateDefaultProperties(Object* propObj);
void dvmSetCommandLineProperties(Object* propObj);

char* dvmGetProperty(const char* key);

#endif /*_DALVIK_PROPERTIES*/
