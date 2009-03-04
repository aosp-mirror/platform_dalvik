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

package java.lang;

import org.apache.harmony.kernel.vm.LangAccess;

/**
 * Implementation of bridge into <code>java.lang</code>.
 */
/*package*/ final class LangAccessImpl extends LangAccess {
    /** non-null; unique instance of this class */
    /*package*/ static final LangAccessImpl THE_ONE = new LangAccessImpl();

    /** 
     * This class is not publicly instantiable. Use {@link #THE_ONE}.
     */
    private LangAccessImpl() {
        // This space intentionally left blank.
    }

    /** {@inheritDoc} */
    public <T> T[] getEnumValuesInOrder(Class<T> clazz) {
        ClassCache<T> cache = clazz.getClassCache();
        return cache.getEnumValuesInOrder();
    }

    /** {@inheritDoc} */
    public void unpark(Thread thread) {
        thread.unpark();
    }

    /** {@inheritDoc} */
    public void parkFor(long nanos) {
        Thread.currentThread().parkFor(nanos);
    }

    /** {@inheritDoc} */
    public void parkUntil(long time) {
        Thread.currentThread().parkUntil(time);
    }
}
