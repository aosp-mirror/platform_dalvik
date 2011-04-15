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

import com.android.dx.rop.code.Rop;
import com.android.dx.rop.code.Rops;
import com.android.dx.rop.type.TypeList;

/**
 * A binary operation on two values of the same type.
 */
public enum BinaryOp {
    ADD() {
        @Override Rop rop(TypeList types) {
            return Rops.opAdd(types);
        }
    },
    SUBTRACT() {
        @Override Rop rop(TypeList types) {
            return Rops.opSub(types);
        }
    },
    MULTIPLY() {
        @Override Rop rop(TypeList types) {
            return Rops.opMul(types);
        }
    },
    DIVIDE() {
        @Override Rop rop(TypeList types) {
            return Rops.opDiv(types);
        }
    },
    REMAINDER() {
        @Override Rop rop(TypeList types) {
            return Rops.opRem(types);
        }
    },
    AND() {
        @Override Rop rop(TypeList types) {
            return Rops.opAnd(types);
        }
    },
    OR() {
        @Override Rop rop(TypeList types) {
            return Rops.opOr(types);
        }
    },
    XOR() {
        @Override Rop rop(TypeList types) {
            return Rops.opXor(types);
        }
    },
    SHIFT_LEFT() {
        @Override Rop rop(TypeList types) {
            return Rops.opShl(types);
        }
    },
    SHIFT_RIGHT() {
        @Override Rop rop(TypeList types) {
            return Rops.opShr(types);
        }
    },
    UNSIGNED_SHIFT_RIGHT() {
        @Override Rop rop(TypeList types) {
            return Rops.opUshr(types);
        }
    };

    abstract Rop rop(com.android.dx.rop.type.TypeList types);
}
