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

import com.android.dx.dex.DexOptions;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.EncodedField;
import com.android.dx.dex.file.EncodedMethod;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.cst.CstUtf8;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An interface or class.
 */
public final class Type<T> {
    private static final Map<Class<?>, String> PRIMITIVE_TO_TYPE_NAME
            = new HashMap<Class<?>, String>();
    static {
        PRIMITIVE_TO_TYPE_NAME.put(boolean.class, "Z");
        PRIMITIVE_TO_TYPE_NAME.put(byte.class, "B");
        PRIMITIVE_TO_TYPE_NAME.put(char.class, "C");
        PRIMITIVE_TO_TYPE_NAME.put(double.class, "D");
        PRIMITIVE_TO_TYPE_NAME.put(float.class, "F");
        PRIMITIVE_TO_TYPE_NAME.put(int.class, "I");
        PRIMITIVE_TO_TYPE_NAME.put(long.class, "J");
        PRIMITIVE_TO_TYPE_NAME.put(short.class, "S");
        PRIMITIVE_TO_TYPE_NAME.put(void.class, "V");
    }

    final DexGenerator generator;
    final String name;

    /** cached converted values */
    final com.android.dx.rop.type.Type ropType;
    final CstType constant;

    /** declared state */
    private boolean declared;
    private int flags;
    private Type<?> supertype;
    private String sourceFile;
    private TypeList interfaces;

    /** declared members */
    private final Canonicalizer<Field<T, ?>> fields = new Canonicalizer<Field<T, ?>>();
    private final Canonicalizer<Method<T, ?>> methods = new Canonicalizer<Method<T, ?>>();

    Type(DexGenerator generator, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.generator = generator;
        this.name = name;
        this.ropType = com.android.dx.rop.type.Type.internReturnType(name);
        this.constant = CstType.intern(ropType);
    }

    Type(DexGenerator generator, Class<T> type) {
        this(generator, getTypeName(type));
    }

    public String getName() {
        return name;
    }

    public <R> Field<T, R> getField(Type<R> type, String name) {
        return fields.get(new Field<T, R>(this, type, name));
    }

    public <R> Method<T, R> getMethod(Type<R> returnType, String name, Type... parameters) {
        return methods.get(new Method<T, R>(this, returnType, name, new TypeList(parameters)));
    }

    public Method<T, Void> getConstructor(Type... parameters) {
        return getMethod(generator.getType(void.class), "<init>", parameters);
    }

    Set<Field<T, ?>> getFields() {
        return fields;
    }

    Set<Method<T, ?>> getMethods() {
        return methods;
    }

    /**
     * @param flags any flags masked by {@link com.android.dx.rop.code.AccessFlags#METHOD_FLAGS}.
     */
    public void declare(String sourceFile, int flags, Type<?> supertype, Type<?>... interfaces) {
        if (declared) {
            throw new IllegalStateException();
        }
        this.declared = true;
        this.flags = flags;
        this.supertype = supertype;
        this.sourceFile = sourceFile;
        this.interfaces = new TypeList(interfaces);
    }

    boolean isDeclared() {
        return declared;
    }

    ClassDefItem toClassDefItem() {
        if (!declared) {
            throw new IllegalStateException();
        }

        DexOptions dexOptions = new DexOptions();
        dexOptions.enableExtendedOpcodes = false;

        CstType thisType = constant;

        ClassDefItem out = new ClassDefItem(thisType, flags, supertype.constant,
                interfaces.ropTypes, new CstUtf8(sourceFile));

        for (Method<T, ?> method : methods) {
            EncodedMethod encoded = method.toEncodedMethod(dexOptions);
            if (method.isDirect()) {
                out.addDirectMethod(encoded);
            } else {
                out.addVirtualMethod(encoded);
            }
        }
        for (Field<T, ?> field : fields) {
            EncodedField encoded = field.toEncodedField();
            if (field.isStatic()) {
                out.addStaticField(encoded, Constants.getConstant(field.getStaticValue()));
            } else {
                out.addInstanceField(encoded);
            }
        }

        return out;
    }

    com.android.dx.rop.type.Type getRopType() {
        return com.android.dx.rop.type.Type.internReturnType(name);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Type
                && ((Type) o).name.equals(name);
    }

    @Override public int hashCode() {
        return name.hashCode();
    }

    @Override public String toString() {
        return name;
    }

    /**
     * Returns a type name like "Ljava/lang/Integer;".
     */
    static String getTypeName(Class<?> type) {
        if (type.isPrimitive()) {
            return PRIMITIVE_TO_TYPE_NAME.get(type);
        }
        String name = type.getName().replace('.', '/');
        return type.isArray() ? name : 'L' + name + ';';
    }
}
