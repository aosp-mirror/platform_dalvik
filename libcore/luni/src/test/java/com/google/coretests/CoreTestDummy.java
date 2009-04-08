/*
 * Copyright (C) 2009 The Android Open Source Project
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
package com.google.coretests;

import junit.framework.TestCase;
import dalvik.annotation.AndroidOnly;
import dalvik.annotation.BrokenTest;
import dalvik.annotation.KnownFailure;
import dalvik.annotation.SideEffect;

/**
 * A dummy test for testing our CoreTestRunner.
 */
public class CoreTestDummy extends TestCase {

    @AndroidOnly("")
    public void testAndroidOnlyPass() {
    }

    @AndroidOnly("")
    public void testAndroidOnlyFail() {
        fail("Oops!");
    }
    
    @BrokenTest("")
    public void testBrokenTestPass() {
    }

    @BrokenTest("")
    public void testBrokenTestFail() {
        fail("Oops!");
    }
    
    @KnownFailure("")
    public void testKnownFailurePass() {
    }

    @KnownFailure("")
    public void testKnownFailureFail() {
        fail("Oops!");
    }
    
    @SideEffect("")
    public void testSideEffectPass() {
    }

    @SideEffect("")
    public void testSideEffectFail() {
        fail("Oops!");
    }

    public void testNormalPass() {
    }

    public void testNormalFail() {
        fail("Oops!");
    }

}
