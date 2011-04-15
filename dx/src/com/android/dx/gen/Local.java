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

import com.android.dx.rop.code.RegisterSpec;
import static com.android.dx.rop.type.Type.BT_DOUBLE;
import static com.android.dx.rop.type.Type.BT_LONG;

/**
 * A temporary variable that holds a single value.
 */
public final class Local<T> {
    private final Code code;
    final Type type;
    final InitialValue initialValue;
    private int reg = -1;
    private RegisterSpec spec;

    Local(Code code, Type type, InitialValue initialValue) {
        this.code = code;
        this.type = type;
        this.initialValue = initialValue;
    }

    /**
     * Assigns registers to this local.
     *
     * @return the number of registers required.
     */
    int initialize(int reg) {
        this.reg = reg;
        this.spec = RegisterSpec.make(reg, type.getRopType());

        switch (type.ropType.getBasicType()) {
        case BT_LONG:
        case BT_DOUBLE:
            return 2;
        default:
            return 1;
        }
    }

    RegisterSpec spec() {
        if (spec == null) {
            code.initializeLocals();
            if (spec == null) {
                throw new AssertionError();
            }
        }
        return spec;
    }

    public Type getType() {
        return type;
    }

    @Override public String toString() {
        return "v" + reg + "(" + type + ")";
    }

    enum InitialValue {
        NONE, THIS, PARAMETER
    }
}
