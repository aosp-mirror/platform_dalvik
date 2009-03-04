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

package org.apache.harmony.luni.lang.reflect;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ListOfTypes {
    static final ListOfTypes empty = new ListOfTypes(0);

    ArrayList<Type> list;
    private Type[] resolvedTypes;

    void add(Type elem) {
        if (elem == null) {
            throw new RuntimeException("Adding null type is not allowed!");
        }
        list.add(elem);
    }

    ListOfTypes(int capacity) {
        list = new ArrayList<Type>(capacity);
    }

    ListOfTypes(Type[] types) {
        list = new ArrayList<Type>();
        for(Type t : types) {
            list.add(t);
        }
    }

    int length() {
        return list.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Type t : list) {
            if (i != 0) { sb.append(", "); }
            sb.append(t.toString());
        }
        return sb.toString();
    }

    // Returns not null, but maybe an array of length 0.
    public Type[] getResolvedTypes() {
        if (resolvedTypes == null) {
            resolvedTypes = new Type[list.size()];
            int i = 0;
            for (Type t : list) {
                try { 
                    resolvedTypes[i] = ((ImplForType)t).getResolvedType();
                } catch (ClassCastException e) { 
                    resolvedTypes[i] = t; 
                }
                i++;
            }
            list = null;
        }
        return resolvedTypes;
    }
}
