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

/**
 * Defines an enumeration of possible states a test case can be in.
 *
 * {@hide}
 *
 */
public enum TestStatus {
    
    /**
     * Status is "to be reviewed", which is the initial state when a test method
     * has been annotated.
     */    
    TBR,
    
    /**
     * Status is "to do", meaning a reviewer has determined that additional work
     * is needed.
     */
    TODO, 
    
    /**
     * Status is "looks good to me", meaning the test is okay.
     */
    LGTM  
    
}
