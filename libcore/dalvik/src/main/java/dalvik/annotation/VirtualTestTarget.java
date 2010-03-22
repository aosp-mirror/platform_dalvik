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

package dalvik.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for "virtual" implementation classes. These are classes that have
 * the following attributes:
 * <ul>
 * <li>they implement a public interface or are a concrete implementation of a
 * public abstract class,</li>
 * <li>they are not public,</li>
 * <li>instances can only be retrieved through some kind of factory method.</li>
 * </ul>
 * <p>
 * Example: {@code MessageDigest} is an abstract class. Concrete implementations
 * of message digest algorithms such as MD5 and SHA-1 can only be retrieved
 * through one of the static {@code getInstance} methods of
 * {@code MessageDigest}, which accept the desired algorithm as a string
 * parameter and return an implementation accordingly.
 * </p>
 * <p>
 * Even though the concrete implementation class for a message digest algorithm
 * is not known, we need to be able to indicate that such a class exists and
 * that it must be tested. This is done by defining corresponding classes and
 * annotating them with {@code @VirtualTestTarget}. This class can then be
 * used in the {@code @TestTargetClass} annotation with which we annotate 
 * {@code TestCase} subclasses.
 * @hide
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface VirtualTestTarget {
    
    /**
     * Field for comments.
     */
    String value() default "";
}
