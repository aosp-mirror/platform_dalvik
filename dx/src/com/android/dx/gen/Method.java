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
import com.android.dx.dex.code.DalvCode;
import com.android.dx.dex.code.PositionList;
import com.android.dx.dex.code.RopTranslator;
import com.android.dx.dex.file.EncodedMethod;
import com.android.dx.rop.code.AccessFlags;
import static com.android.dx.rop.code.AccessFlags.ACC_CONSTRUCTOR;
import static com.android.dx.rop.code.AccessFlags.ACC_PRIVATE;
import static com.android.dx.rop.code.AccessFlags.ACC_STATIC;
import com.android.dx.rop.code.LocalVariableInfo;
import com.android.dx.rop.code.RopMethod;
import com.android.dx.rop.cst.CstMethodRef;
import com.android.dx.rop.cst.CstNat;
import com.android.dx.rop.cst.CstUtf8;
import com.android.dx.rop.type.Prototype;
import com.android.dx.rop.type.StdTypeList;
import java.util.List;

/**
 * A method or constructor.
 */
public final class Method<T, R> {
    final Type<T> declaringType;
    final Type<R> returnType;
    final String name;
    final TypeList parameters;

    /** cached converted state */
    final CstNat nat;
    final CstMethodRef constant;

    /** declared state */
    private boolean declared;
    private int accessFlags;
    private Code code;

    Method(Type<T> declaringType, Type<R> returnType, String name, TypeList parameters) {
        if (declaringType == null || returnType == null || name == null || parameters == null) {
            throw new NullPointerException();
        }
        this.declaringType = declaringType;
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.nat = new CstNat(new CstUtf8(name), new CstUtf8(descriptor(false)));
        this.constant = new CstMethodRef(declaringType.constant, nat);
    }

    public Type<T> getDeclaringType() {
        return declaringType;
    }

    public Type<R> getReturnType() {
        return returnType;
    }

    public String getName() {
        return name;
    }

    public List<Type<?>> getParameters() {
        return parameters.asList();
    }

    /**
     * Returns a descriptor like "(Ljava/lang/Class;[I)Ljava/lang/Object;".
     */
    String descriptor(boolean includeThis) {
        StringBuilder result = new StringBuilder();
        result.append("(");
        if (includeThis) {
            result.append(declaringType.name);
        }
        for (Type t : parameters.types) {
            result.append(t.name);
        }
        result.append(")");
        result.append(returnType.name);
        return result.toString();
    }

    /**
     * @param accessFlags any flags masked by {@link AccessFlags#METHOD_FLAGS}.
     */
    public void declare(int accessFlags, Code code) {
        if (declared) {
            throw new IllegalStateException();
        }
        if (code == null) {
            throw new NullPointerException(); // TODO: permit methods without code
        }
        if (!parameters.equals(code.parameters())) {
            throw new IllegalArgumentException("Parameters mismatch. Expected (" + parameters
                    + ") but was (" + code.parameters() + ")");
        }
        boolean isStatic = (accessFlags & (ACC_STATIC)) != 0;
        if (isStatic != (code.thisLocal() == null)) {
            throw new IllegalArgumentException("Static mismatch. Declared static=" + isStatic
                    + " this local=" + code.thisLocal());
        }
        this.declared = true;
        this.accessFlags = accessFlags;
        this.code = code;
    }

    boolean isDeclared() {
        return declared;
    }

    boolean isDirect() {
        if (!declared) {
            throw new IllegalStateException();
        }
        return (accessFlags & (ACC_STATIC | ACC_PRIVATE | ACC_CONSTRUCTOR)) != 0;
    }

    Prototype prototype(boolean includeThis) {
        return Prototype.intern(descriptor(includeThis));
    }

    EncodedMethod toEncodedMethod(DexOptions dexOptions) {
        if (!declared) {
            throw new IllegalStateException();
        }
        RopMethod ropMethod = new RopMethod(code.toBasicBlocks(), 0);
        int paramSize = -1;
        LocalVariableInfo locals = null;
        int positionInfo = PositionList.NONE;
        DalvCode code = RopTranslator.translate(
                ropMethod, positionInfo, locals, paramSize, dexOptions);
        return new EncodedMethod(constant, accessFlags, code, StdTypeList.EMPTY);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Method
                && ((Method) o).declaringType.equals(declaringType)
                && ((Method) o).name.equals(name)
                && ((Method) o).parameters.equals(parameters)
                && ((Method) o).returnType.equals(returnType);
    }

    @Override public int hashCode() {
        int result = 17;
        result = 31 * result + declaringType.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + parameters.hashCode();
        result = 31 * result + returnType.hashCode();
        return result;
    }

    @Override public String toString() {
        return declaringType + "." + name + "(" + parameters + ")";
    }
}
