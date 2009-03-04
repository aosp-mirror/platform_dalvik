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

package java.lang.reflect;

import org.apache.harmony.kernel.vm.ReflectionAccess;

/**
 * Implementation of bridge from {@code java.lang} to
 * {@code java.lang.reflect}.
 */
/*package*/ final class ReflectionAccessImpl implements ReflectionAccess {
    /** non-null; unique instance of this class */
    /*package*/ static final ReflectionAccessImpl THE_ONE =
        new ReflectionAccessImpl();

    /**
     * This class is not publicly instantiable. Use {@link #THE_ONE}.
     */
    private ReflectionAccessImpl() {
        // This space intentionally left blank.
    }

    public Method clone(Method method) {
        return new Method(method);
    }

    public Field clone(Field field) {
        return new Field(field);
    }

    public Method accessibleClone(Method method) {
        Method result = new Method(method);
        result.setAccessibleNoCheck(true);
        return result;
    }

    public void setAccessibleNoCheck(AccessibleObject ao, boolean accessible) {
        ao.setAccessibleNoCheck(accessible);
    }
}
