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

import com.android.dx.dex.DexFormat;
import com.android.dx.dex.file.DexFile;
import com.android.dx.rop.cst.CstBoolean;
import com.android.dx.rop.cst.CstByte;
import com.android.dx.rop.cst.CstChar;
import com.android.dx.rop.cst.CstDouble;
import com.android.dx.rop.cst.CstFloat;
import com.android.dx.rop.cst.CstInteger;
import com.android.dx.rop.cst.CstKnownNull;
import com.android.dx.rop.cst.CstLong;
import com.android.dx.rop.cst.CstShort;
import com.android.dx.rop.cst.CstString;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.cst.TypedConstant;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Define types, fields and methods.
 */
public final class DexGenerator {
    private final Canonicalizer<Type<?>> types = new Canonicalizer<Type<?>>();

    /**
     * @param name a descriptor like "(Ljava/lang/Class;[I)Ljava/lang/Object;".
     */
    public Type<?> getType(String name) {
        return types.get(new Type<Object>(this, name));
    }

    public <T> Type<T> getType(Class<T> type) {
        return types.get(new Type<T>(this, type));
    }

    public Code newCode() {
        return new Code(this);
    }

    /**
     * Returns a .dex formatted file.
     */
    public byte[] generate() {
        DexFile outputDex = new DexFile();

        for (Type<?> type : types) {
            if (type.isDeclared()) {
                outputDex.add(type.toClassDefItem());
            } else {
                for (Method<?, ?> m : type.getMethods()) {
                    if (m.isDeclared()) {
                        throw new IllegalStateException(
                                "Undeclared type " + type + " declares " + m);
                    }
                }
                for (Field<?, ?> f : type.getFields()) {
                    if (f.isDeclared()) {
                        throw new IllegalStateException(
                                "Undeclared type " + type + " declares " + f);
                    }
                }
            }
        }

        try {
            return outputDex.toDex(null, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the generated types into the current dalvikvm process.
     */
    public ClassLoader load(ClassLoader parent) throws IOException {
        byte[] dex = generate();

        /*
         * This implementation currently dumps the dex to the filesystem. It
         * jars the emitted .dex for the benefit of Gingerbread and earlier
         * devices, which can't load .dex files directly.
         *
         * TODO: load the dex from memory where supported.
         */
        File result = File.createTempFile("Generated", ".jar");
        result.deleteOnExit();
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(result));
        jarOut.putNextEntry(new JarEntry(DexFormat.DEX_IN_JAR_NAME));
        jarOut.write(dex);
        jarOut.closeEntry();
        jarOut.close();
        try {
            Class<?> pathClassLoader = Class.forName("dalvik.system.PathClassLoader");
            return (ClassLoader) pathClassLoader.getConstructor(String.class, ClassLoader.class)
                    .newInstance(result.getPath(), parent);
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("load() requires a Dalvik VM", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (InstantiationException e) {
            throw new AssertionError();
        } catch (NoSuchMethodException e) {
            throw new AssertionError();
        } catch (IllegalAccessException e) {
            throw new AssertionError();
        }
    }
}
