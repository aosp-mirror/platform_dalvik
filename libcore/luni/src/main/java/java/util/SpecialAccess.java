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

package java.util;

import org.apache.harmony.kernel.vm.LangAccess;

/**
 * Holder for special cross-package access objects.
 */
/*package*/ final class SpecialAccess {
    /** non-null; package access to <code>java.lang</code> */
    static /*package*/ final LangAccess LANG;

    static {
        /*
         * Force ClassCache to be initialized, which should set
         * EnumSet.LANG_BOOTSTRAP.
         */
        try {
            Runnable.class.getMethod("run", (Class[]) null);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }

        // This can only be assigned after the above bootstrap.
        LANG = EnumSet.LANG_BOOTSTRAP;
        
        if (LANG == null) {
            throw new AssertionError();
        }
    }
}
