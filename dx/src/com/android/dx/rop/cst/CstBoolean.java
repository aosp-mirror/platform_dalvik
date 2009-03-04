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

package com.android.dx.rop.cst;

import com.android.dx.rop.type.Type;

/**
 * Constants of type <code>boolean</code>.
 */
public final class CstBoolean
        extends CstLiteral32 {
    /** non-null; instance representing <code>false</code> */
    public static final CstBoolean VALUE_FALSE = new CstBoolean(false);

    /** non-null; instance representing <code>true</code> */
    public static final CstBoolean VALUE_TRUE = new CstBoolean(true);

    /**
     * Makes an instance for the given value. This will return an
     * already-allocated instance.
     * 
     * @param value the <code>boolean</code> value
     * @return non-null; the appropriate instance
     */
    public static CstBoolean make(boolean value) {
        return value ? VALUE_TRUE : VALUE_FALSE;
    }

    /**
     * Makes an instance for the given <code>int</code> value. This
     * will return an already-allocated instance.
     * 
     * @param value must be either <code>0</code> or <code>1</code>
     * @return non-null; the appropriate instance
     */
    public static CstBoolean make(int value) {
        if (value == 0) {
            return VALUE_FALSE;
        } else if (value == 1) {
            return VALUE_TRUE;
        } else {
            throw new IllegalArgumentException("bogus value: " + value);
        }
    }

    /**
     * Constructs an instance. This constructor is private; use {@link #make}.
     * 
     * @param value the <code>boolean</code> value
     */
    private CstBoolean(boolean value) {
        super(value ? 1 : 0);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getValue() ? "boolean{true}" : "boolean{false}";
    }

    /** {@inheritDoc} */
    public Type getType() {
        return Type.BOOLEAN;
    }

    /** {@inheritDoc} */
    @Override
    public String typeName() {
        return "boolean";
    }

    /** {@inheritDoc} */
    public String toHuman() {
        return getValue() ? "true" : "false";
    }

    /**
     * Gets the <code>boolean</code> value.
     * 
     * @return the value
     */
    public boolean getValue() {
        return (getIntBits() == 0) ? false : true;
    }
}
