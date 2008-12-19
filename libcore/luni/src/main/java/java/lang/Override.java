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

import java.lang.annotation.Target;
import java.lang.annotation.Retention;

/**
 * Annotation type used to mark methods that override a method declaration in a superclass.
 * Compilers produce an error if a method annotated with @Override does not actually override
 * a method in a superclass.
 * 
 * @since Android 1.0
 */
@Retention(value=java.lang.annotation.RetentionPolicy.SOURCE)
@Target(value=java.lang.annotation.ElementType.METHOD)
public @interface Override
{

}
