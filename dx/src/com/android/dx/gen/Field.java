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

import com.android.dx.dex.file.EncodedField;
import com.android.dx.rop.code.AccessFlags;
import com.android.dx.rop.cst.CstFieldRef;
import com.android.dx.rop.cst.CstNat;
import com.android.dx.rop.cst.CstUtf8;

/**
 * A field.
 */
public final class Field<T, R> {
    final Type<T> declaringType;
    final Type<R> type;
    final String name;

    /** cached converted state */
    final CstNat nat;
    final CstFieldRef constant;

    /** declared state */
    private boolean declared;
    private int accessFlags;
    private Object staticValue;

    Field(Type<T> declaringType, Type<R> type, String name) {
        if (declaringType == null || type == null || name == null) {
            throw new NullPointerException();
        }
        this.declaringType = declaringType;
        this.type = type;
        this.name = name;
        this.nat = new CstNat(new CstUtf8(name), new CstUtf8(type.name));
        this.constant = new CstFieldRef(declaringType.constant, nat);
    }

    public Type<T> getDeclaringType() {
        return declaringType;
    }

    public Type<R> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    /**
     * @param accessFlags any flags masked by {@link AccessFlags#FIELD_FLAGS}.
     */
    public void declare(int accessFlags, Object staticValue) {
        if (declared) {
            throw new IllegalStateException("already declared: " + this);
        }
        if ((accessFlags & (AccessFlags.ACC_STATIC)) == 0 && staticValue != null) {
            throw new IllegalArgumentException("instance fields may not have a value");
        }
        this.declared = true;
        this.accessFlags = accessFlags;
        this.staticValue = staticValue;
    }

    public Object getStaticValue() {
        return staticValue;
    }

    boolean isDeclared() {
        return declared;
    }

    public boolean isStatic() {
        if (!declared) {
            throw new IllegalStateException();
        }
        return (accessFlags & (AccessFlags.ACC_STATIC)) != 0;
    }

    EncodedField toEncodedField() {
        if (!declared) {
            throw new IllegalStateException();
        }
        return new EncodedField(constant, accessFlags);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Field
                && ((Field) o).declaringType.equals(declaringType)
                && ((Field) o).name.equals(name);
    }

    @Override public int hashCode() {
        return declaringType.hashCode() + 37 * name.hashCode();
    }

    @Override public String toString() {
        return declaringType + "." + name;
    }
}
