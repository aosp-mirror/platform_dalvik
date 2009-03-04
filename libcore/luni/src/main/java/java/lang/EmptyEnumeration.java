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

import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Empty enumeration class.  Call getInstance() to get the singleton.
 *
 * This is not part of the Java spec, so must be package-scope only.
 */
/*package*/ final class EmptyEnumeration implements Enumeration<URL>, Serializable {

    private static final EmptyEnumeration mInst = new EmptyEnumeration();

    /**
     * One instance per VM.
     */
    private EmptyEnumeration() {}

    /**
     * Return instance.
     */
    public static EmptyEnumeration getInstance() {
        return mInst;
    }

    /**
     * Enumeration implementation.
     */
    public boolean hasMoreElements() {
        return false;
    }
    
    public URL nextElement() {
        throw new NoSuchElementException();
    }
}

