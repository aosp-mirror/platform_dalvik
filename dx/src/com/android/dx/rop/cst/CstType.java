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

import java.util.HashMap;

/**
 * Constants that represent an arbitrary type (reference or primitive).
 */
public final class CstType extends TypedConstant {
    /** non-null; map of interned types */
    private static final HashMap<Type, CstType> interns =
        new HashMap<Type, CstType>(100);

    /** non-null; instance corresponding to the class <code>Object</code> */
    public static final CstType OBJECT = intern(Type.OBJECT);

    /** non-null; instance corresponding to the class <code>Boolean</code> */
    public static final CstType BOOLEAN = intern(Type.BOOLEAN_CLASS);

    /** non-null; instance corresponding to the class <code>Byte</code> */
    public static final CstType BYTE = intern(Type.BYTE_CLASS);

    /** non-null; instance corresponding to the class <code>Character</code> */
    public static final CstType CHARACTER = intern(Type.CHARACTER_CLASS);

    /** non-null; instance corresponding to the class <code>Double</code> */
    public static final CstType DOUBLE = intern(Type.DOUBLE_CLASS);

    /** non-null; instance corresponding to the class <code>Float</code> */
    public static final CstType FLOAT = intern(Type.FLOAT_CLASS);

    /** non-null; instance corresponding to the class <code>Long</code> */
    public static final CstType LONG = intern(Type.LONG_CLASS);

    /** non-null; instance corresponding to the class <code>Integer</code> */
    public static final CstType INTEGER = intern(Type.INTEGER_CLASS);

    /** non-null; instance corresponding to the class <code>Short</code> */
    public static final CstType SHORT = intern(Type.SHORT_CLASS);

    /** non-null; instance corresponding to the class <code>Void</code> */
    public static final CstType VOID = intern(Type.VOID_CLASS);

    /** non-null; instance corresponding to the type <code>boolean[]</code> */
    public static final CstType BOOLEAN_ARRAY = intern(Type.BOOLEAN_ARRAY);

    /** non-null; instance corresponding to the type <code>byte[]</code> */
    public static final CstType BYTE_ARRAY = intern(Type.BYTE_ARRAY);

    /** non-null; instance corresponding to the type <code>char[]</code> */
    public static final CstType CHAR_ARRAY = intern(Type.CHAR_ARRAY);

    /** non-null; instance corresponding to the type <code>double[]</code> */
    public static final CstType DOUBLE_ARRAY = intern(Type.DOUBLE_ARRAY);

    /** non-null; instance corresponding to the type <code>float[]</code> */
    public static final CstType FLOAT_ARRAY = intern(Type.FLOAT_ARRAY);

    /** non-null; instance corresponding to the type <code>long[]</code> */
    public static final CstType LONG_ARRAY = intern(Type.LONG_ARRAY);

    /** non-null; instance corresponding to the type <code>int[]</code> */
    public static final CstType INT_ARRAY = intern(Type.INT_ARRAY);

    /** non-null; instance corresponding to the type <code>short[]</code> */
    public static final CstType SHORT_ARRAY = intern(Type.SHORT_ARRAY);

    /** non-null; the underlying type */
    private final Type type;

    /**
     * null-ok; the type descriptor corresponding to this instance, if
     * calculated
     */
    private CstUtf8 descriptor;

    /**
     * Returns an instance of this class that represents the wrapper
     * class corresponding to a given primitive type. For example, if
     * given {@link Type#INT}, this method returns the class reference
     * <code>java.lang.Integer</code>.
     * 
     * @param primitiveType non-null; the primitive type
     * @return non-null; the corresponding wrapper class
     */
    public static CstType forBoxedPrimitiveType(Type primitiveType) {
        switch (primitiveType.getBasicType()) {
            case Type.BT_BOOLEAN: return BOOLEAN;
            case Type.BT_BYTE:    return BYTE;
            case Type.BT_CHAR:    return CHARACTER;
            case Type.BT_DOUBLE:  return DOUBLE;
            case Type.BT_FLOAT:   return FLOAT;
            case Type.BT_INT:     return INTEGER;
            case Type.BT_LONG:    return LONG;
            case Type.BT_SHORT:   return SHORT;
            case Type.BT_VOID:    return VOID;
        }

        throw new IllegalArgumentException("not primitive: " + primitiveType);
    }

    /**
     * Returns an interned instance of this class for the given type.
     * 
     * @param type non-null; the underlying type
     * @return non-null; an appropriately-constructed instance
     */
    public static CstType intern(Type type) {
        CstType cst = interns.get(type);

        if (cst == null) {
            cst = new CstType(type);
            interns.put(type, cst);
        }

        return cst;
    }

    /**
     * Constructs an instance.
     * 
     * @param type non-null; the underlying type
     */
    public CstType(Type type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }

        if (type == type.KNOWN_NULL) {
            throw new UnsupportedOperationException(
                    "KNOWN_NULL is not representable");
        }

        this.type = type;
        this.descriptor = null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CstType)) {
            return false;
        }

        return type == ((CstType) other).type;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return type.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    protected int compareTo0(Constant other) {
        String thisDescriptor = type.getDescriptor();
        String otherDescriptor = ((CstType) other).type.getDescriptor();
        return thisDescriptor.compareTo(otherDescriptor);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "type{" + toHuman() + '}';
    }

    /** {@inheritDoc} */
    public Type getType() {
        return Type.CLASS;
    }

    /** {@inheritDoc} */
    @Override
    public String typeName() {
        return "type";
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCategory2() {
        return false;
    }

    /** {@inheritDoc} */
    public String toHuman() {
        return type.toHuman();
    }

    /**
     * Gets the underlying type (as opposed to the type corresponding
     * to this instance as a constant, which is always
     * <code>Class</code>).
     * 
     * @return non-null; the type corresponding to the name
     */
    public Type getClassType() {
        return type;
    }

    /**
     * Gets the type descriptor for this instance.
     * 
     * @return non-null; the descriptor
     */
    public CstUtf8 getDescriptor() {
        if (descriptor == null) {
            descriptor = new CstUtf8(type.getDescriptor());
        }
        
        return descriptor;
    }
}
