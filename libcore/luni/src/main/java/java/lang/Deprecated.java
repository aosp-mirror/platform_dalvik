/*
 * Copyright (C) 2007 The Android Open Source Project
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

package java.lang;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

/**
 * Annotation type used to mark program elements that should no longer be used
 * by programmers. Compilers produce a warning if a deprecated program element
 * is used.
 * 
 * @since Android 1.0
 */
@Documented
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Deprecated
{

}
