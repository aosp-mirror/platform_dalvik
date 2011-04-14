/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dx.gen;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

final class Canonicalizer<T> extends AbstractSet<T> implements Set<T> {
    private final Map<T, T> map = new HashMap<T, T>();

    public <S extends T> S get(S s) {
        @SuppressWarnings("unchecked") // equals() guarantees that the types match
        S result = (S) map.get(s);
        if (result != null) {
            return result;
        }
        map.put(s, s);
        return s;
    }

    @Override public int size() {
        return map.size();
    }

    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }
}
