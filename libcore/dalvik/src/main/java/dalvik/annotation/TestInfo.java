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
 * Defines an annotation for test methods that allow, among other things, to
 * link the test to the method that is being tested.
 * 
 * {@hide}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface TestInfo {
    
    /**
     * Specifies the current status of the test, as determined by a reviewer.
     */
    TestStatus status() default TestStatus.TBR;

    /**
     * Specifies noteworthy plain-text information about the test, like whether
     * it is testing a specific parameter combination or something.
     */
    String notes() default "";
    
    /**
     * Specifies an array of target methods.
     */
    TestTarget[] targets();
    
}
