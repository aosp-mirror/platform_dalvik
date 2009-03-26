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

package com.android.dexdeps;

/**
 * Generate fancy output.
 */
public class Output {
    public static void generate(DexData dexData, String format) {
        FieldRef[] externFieldRefs;
        MethodRef[] externMethodRefs;

        externFieldRefs = dexData.getExternalFieldReferences();
        externMethodRefs = dexData.getExternalMethodReferences();

        if (format.equals("brief")) {
            printFieldRefs(externFieldRefs);
            printMethodRefs(externMethodRefs);
        } else if (format.equals("xml")) {
            // ...
        } else {
            /* should've been trapped in arg handler */
            throw new RuntimeException("unknown output format");
        }
    }

    static void printFieldRefs(FieldRef[] fields) {
        System.out.println("Fields:");
        for (int i = 0; i < fields.length; i++) {
            FieldRef ref = fields[i];

            System.out.println(ref.getDeclClassName() + "." +
                ref.getName() + " : " + ref.getTypeName());
        }
    }

    static void printMethodRefs(MethodRef[] methods) {
        System.out.println("Methods:");
        for (int i = 0; i < methods.length; i++) {
            MethodRef ref = methods[i];

            System.out.println(ref.getDeclClassName() + "." +
                ref.getName() + " : " + ref.getDescriptor());
        }
    }
}

