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

package org.apache.harmony.kernel.vm;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * Bridge from <code>java.lang</code> to <code>java.lang.reflect</code>.
 * The package <code>java.lang</code> gets seeded with an instance of
 * this interface, to allow it to call into what would otherwise be
 * package-scope functionality in <code>java.lang.reflect</code>.
 */
public interface ReflectionAccess {
    /**
     * Gets a clone of the given method.
     * 
     * @param method non-null; the method to clone
     * @return non-null; the clone
     */
    public Method clone(Method method);

    /**
     * Gets a clone of the given method, where the clone has
     * its "accessible" flag set to <code>true</code>
     * 
     * @param method non-null; the method to clone
     * @return non-null; the accessible clone
     */
    public Method accessibleClone(Method method);

    /**
     * Sets the accessible flag on a given {@link AccessibleObject}
     * without doing any checks.
     * 
     * @param ao non-null; the instance in question
     * @param flag the new value for the accessible flag
     */
    public void setAccessibleNoCheck(AccessibleObject ao, boolean flag);
}
