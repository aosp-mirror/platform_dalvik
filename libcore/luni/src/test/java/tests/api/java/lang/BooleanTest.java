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

package tests.api.java.lang;

import junit.framework.TestCase;

/**
 * Tests for the class {@link Boolean}.
 */
public class BooleanTest
    extends TestCase
{
    public void test_TRUE() {
        assertTrue(Boolean.TRUE.booleanValue() == true);
    }

    public void test_FALSE() {
        assertTrue(Boolean.FALSE.booleanValue() == false);
    }

    // TODO(danfuzz): Add tests for the rest of Boolean here.
}
