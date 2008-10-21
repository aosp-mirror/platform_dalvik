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

package com.android.dx.rop.type;

import com.android.dx.util.Hex;

import java.util.HashMap;

/**
 * Representation of a value type, such as may appear in a field, in a
 * local, on a stack, or in a method descriptor. Instances of this
 * class are generally interned and may be usefully compared with each
 * other using <code>==</code>.
 */
public final class Type implements TypeBearer, Comparable<Type> {
    /** non-null; intern table mapping string descriptors to instances */
    private static final HashMap<String, Type> internTable = 
        new HashMap<String, Type>(500);

    /** basic type constant for <code>void</code> */
    public static final int BT_VOID = 0;

    /** basic type constant for <code>boolean</code> */
    public static final int BT_BOOLEAN = 1;

    /** basic type constant for <code>byte</code> */
    public static final int BT_BYTE = 2;

    /** basic type constant for <code>char</code> */
    public static final int BT_CHAR = 3;

    /** basic type constant for <code>double</code> */
    public static final int BT_DOUBLE = 4;

    /** basic type constant for <code>float</code> */
    public static final int BT_FLOAT = 5;

    /** basic type constant for <code>int</code> */
    public static final int BT_INT = 6;

    /** basic type constant for <code>long</code> */
    public static final int BT_LONG = 7;

    /** basic type constant for <code>short</code> */
    public static final int BT_SHORT = 8;

    /** basic type constant for <code>Object</code> */
    public static final int BT_OBJECT = 9;

    /** basic type constant for a return address */
    public static final int BT_ADDR = 10;

    /** count of basic type constants */
    public static final int BT_COUNT = 11;

    /** non-null; instance representing <code>boolean</code> */
    public static final Type BOOLEAN = new Type("Z", BT_BOOLEAN);

    /** non-null; instance representing <code>byte</code> */
    public static final Type BYTE = new Type("B", BT_BYTE);

    /** non-null; instance representing <code>char</code> */
    public static final Type CHAR = new Type("C", BT_CHAR);

    /** non-null; instance representing <code>double</code> */
    public static final Type DOUBLE = new Type("D", BT_DOUBLE);

    /** non-null; instance representing <code>float</code> */
    public static final Type FLOAT = new Type("F", BT_FLOAT);

    /** non-null; instance representing <code>int</code> */
    public static final Type INT = new Type("I", BT_INT);

    /** non-null; instance representing <code>long</code> */
    public static final Type LONG = new Type("J", BT_LONG);

    /** non-null; instance representing <code>short</code> */
    public static final Type SHORT = new Type("S", BT_SHORT);

    /** non-null; instance representing <code>void</code> */
    public static final Type VOID = new Type("V", BT_VOID);

    /** non-null; instance representing a known-<code>null</code> */
    public static final Type KNOWN_NULL = new Type("<null>", BT_OBJECT);

    /** non-null; instance representing a subroutine return address */
    public static final Type RETURN_ADDRESS = new Type("<addr>", BT_ADDR);

    static {
        /*
         * Put all the primitive types into the intern table. This needs
         * to happen before the array types below get interned.
         */
        putIntern(BOOLEAN);
        putIntern(BYTE);
        putIntern(CHAR);
        putIntern(DOUBLE);
        putIntern(FLOAT);
        putIntern(INT);
        putIntern(LONG);
        putIntern(SHORT);
        /*
         * Note: VOID isn't put in the intern table, since it's special and
         * shouldn't be found by a normal call to intern().
         */
    }

    /**
     * non-null; instance representing
     * <code>java.lang.annotation.Annotation</code>
     */
    public static final Type ANNOTATION =
        intern("Ljava/lang/annotation/Annotation;");

    /** non-null; instance representing <code>java.lang.Class</code> */
    public static final Type CLASS = intern("Ljava/lang/Class;");

    /** non-null; instance representing <code>java.lang.Cloneable</code> */
    public static final Type CLONEABLE = intern("Ljava/lang/Cloneable;");

    /** non-null; instance representing <code>java.lang.Object</code> */
    public static final Type OBJECT = intern("Ljava/lang/Object;");

    /** non-null; instance representing <code>java.io.Serializable</code> */
    public static final Type SERIALIZABLE = intern("Ljava/io/Serializable;");

    /** non-null; instance representing <code>java.lang.String</code> */
    public static final Type STRING = intern("Ljava/lang/String;");

    /** non-null; instance representing <code>java.lang.Throwable</code> */
    public static final Type THROWABLE = intern("Ljava/lang/Throwable;");

    /**
     * non-null; instance representing <code>java.lang.Boolean</code>; the
     * suffix on the name helps disambiguate this from the instance
     * representing a primitive type 
     */
    public static final Type BOOLEAN_CLASS = intern("Ljava/lang/Boolean;");

    /**
     * non-null; instance representing <code>java.lang.Byte</code>; the
     * suffix on the name helps disambiguate this from the instance
     * representing a primitive type 
     */
    public static final Type BYTE_CLASS = intern("Ljava/lang/Byte;");

    /**
     * non-null; instance representing <code>java.lang.Character</code>; the
     * suffix on the name helps disambiguate this from the instance
     * representing a primitive type 
     */
    public static final Type CHARACTER_CLASS = intern("Ljava/lang/Character;");

    /**
     * non-null; instance representing <code>java.lang.Double</code>; the
     * suffix on the name helps disambiguate this from the instance
     * representing a primitive type 
     */
    public static final Type DOUBLE_CLASS = intern("Ljava/lang/Double;");

    /**
     * non-null; instance representing <code>java.lang.Float</code>; the
     * suffix on the name helps disambiguate this from the instance
     * representing a primitive type 
     */
    public static final Type FLOAT_CLASS = intern("Ljava/lang/Float;");

    /**
     * non-null; instance representing <code>java.lang.Integer</code>; the
     * suffix on the name helps disambiguate this from the instance
     * representing a primitive type 
     */
    public static final Type INTEGER_CLASS = intern("Ljava/lang/Integer;");

    /**
     * non-null; instance representing <code>java.lang.Long</code>; the
     * suffix on the name helps disambiguate this from the instance
     * representing a primitive type 
     */
    public static final Type LONG_CLASS = intern("Ljava/lang/Long;");

    /**
     * non-null; instance representing <code>java.lang.Short</code>; the
     * suffix on the name helps disambiguate this from the instance
     * representing a primitive type 
     */
    public static final Type SHORT_CLASS = intern("Ljava/lang/Short;");

    /**
     * non-null; instance representing <code>java.lang.Void</code>; the
     * suffix on the name helps disambiguate this from the instance
     * representing a primitive type 
     */
    public static final Type VOID_CLASS = intern("Ljava/lang/Void;");

    /** non-null; instance representing <code>boolean[]</code> */
    public static final Type BOOLEAN_ARRAY = BOOLEAN.getArrayType();

    /** non-null; instance representing <code>byte[]</code> */
    public static final Type BYTE_ARRAY = BYTE.getArrayType();

    /** non-null; instance representing <code>char[]</code> */
    public static final Type CHAR_ARRAY = CHAR.getArrayType();

    /** non-null; instance representing <code>double[]</code> */
    public static final Type DOUBLE_ARRAY = DOUBLE.getArrayType();

    /** non-null; instance representing <code>float[]</code> */
    public static final Type FLOAT_ARRAY = FLOAT.getArrayType();

    /** non-null; instance representing <code>int[]</code> */
    public static final Type INT_ARRAY = INT.getArrayType();

    /** non-null; instance representing <code>long[]</code> */
    public static final Type LONG_ARRAY = LONG.getArrayType();

    /** non-null; instance representing <code>Object[]</code> */
    public static final Type OBJECT_ARRAY = OBJECT.getArrayType();

    /** non-null; instance representing <code>short[]</code> */
    public static final Type SHORT_ARRAY = SHORT.getArrayType();

    /** non-null; field descriptor for the type */
    private final String descriptor;

    /**
     * basic type corresponding to this type; one of the
     * <code>BT_*</code> constants 
     */
    private final int basicType;

    /**
     * &gt;= -1; for an uninitialized type, bytecode index that this
     * instance was allocated at; <code>Integer.MAX_VALUE</code> if it
     * was an incoming uninitialized instance; <code>-1</code> if this
     * is an <i>inititialized</i> instance 
     */
    private final int newAt;

    /**
     * null-ok; the internal-form class name corresponding to this type, if
     * calculated; only valid if <code>this</code> is a reference type and
     * additionally not a return address 
     */
    private String className;

    /**
     * null-ok; the type corresponding to an array of this type, if
     * calculated 
     */
    private Type arrayType;

    /**
     * null-ok; the type corresponding to elements of this type, if
     * calculated; only valid if <code>this</code> is an array type 
     */
    private Type componentType;

    /**
     * null-ok; the type corresponding to the initialized version of
     * this type, if this instance is in fact an uninitialized type 
     */
    private Type initializedType;

    /**
     * Returns the unique instance corresponding to the type with the
     * given descriptor. See vmspec-2 sec4.3.2 for details on the
     * field descriptor syntax. This method does <i>not</i> allow
     * <code>"V"</code> (that is, type <code>void</code>) as a valid
     * descriptor.
     * 
     * @param descriptor non-null; the descriptor
     * @return non-null; the corresponding instance
     * @throws IllegalArgumentException thrown if the descriptor has
     * invalid syntax
     */
    public static Type intern(String descriptor) {
        Type result = internTable.get(descriptor);
        if (result != null) {
            return result;
        }

        char firstChar;
        try {
            firstChar = descriptor.charAt(0);
        } catch (IndexOutOfBoundsException ex) {
            // Translate the exception.
            throw new IllegalArgumentException("descriptor is empty");
        } catch (NullPointerException ex) {
            // Elucidate the exception.
            throw new NullPointerException("descriptor == null");
        }

        if (firstChar == '[') {
            /*
             * Recursively strip away array markers to get at the underlying
             * type, and build back on to form the result.
             */
            result = intern(descriptor.substring(1));
            return result.getArrayType();
        }

        /*
         * If the first character isn't '[' and it wasn't found in the
         * intern cache, then it had better be the descriptor for a class.
         */

        int length = descriptor.length();
        if ((firstChar != 'L') ||
            (descriptor.charAt(length - 1) != ';')) {
            throw new IllegalArgumentException("bad descriptor");
        }

        /*
         * Validate the characters of the class name itself. Note that
         * vmspec-2 does not have a coherent definition for valid
         * internal-form class names, and the definition here is fairly
         * liberal: A name is considered valid as long as it doesn't
         * contain any of '[' ';' '.' '(' ')', and it has no more than one
         * '/' in a row, and no '/' at either end.
         */

        int limit = (length - 1); // Skip the final ';'.
        for (int i = 1; i < limit; i++) {
            char c = descriptor.charAt(i);
            switch (c) {
                case '[':
                case ';': 
                case '.': 
                case '(':
                case ')': {
                    throw new IllegalArgumentException("bad descriptor");
                }
                case '/': {
                    if ((i == 1) ||
                        (i == (length - 1)) ||
                        (descriptor.charAt(i - 1) == '/')) {
                        throw new IllegalArgumentException("bad descriptor");
                    }
                    break;
                }
            }
        }

        result = new Type(descriptor, BT_OBJECT);
        return putIntern(result);
    }

    /**
     * Returns the unique instance corresponding to the type with the
     * given descriptor, allowing <code>"V"</code> to return the type
     * for <code>void</code>. Other than that one caveat, this method
     * is identical to {@link #intern}.
     * 
     * @param descriptor non-null; the descriptor
     * @return non-null; the corresponding instance
     * @throws IllegalArgumentException thrown if the descriptor has
     * invalid syntax
     */
    public static Type internReturnType(String descriptor) {
        try {
            if (descriptor.equals("V")) {
                // This is the one special case where void may be returned.
                return VOID;
            }
        } catch (NullPointerException ex) {
            // Elucidate the exception.
            throw new NullPointerException("descriptor == null");
        }

        return intern(descriptor);
    }

    /**
     * Returns the unique instance corresponding to the type of the
     * class with the given name. Calling this method is equivalent to
     * calling <code>intern(name)</code> if <code>name</code> begins
     * with <code>"["</code> and calling <code>intern("L" + name + ";")</code>
     * in all other cases.
     * 
     * @param name non-null; the name of the class whose type is desired
     * @return non-null; the corresponding type
     * @throws IllegalArgumentException thrown if the name has
     * invalid syntax
     */
    public static Type internClassName(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }

        if (name.startsWith("[")) {
            return intern(name);
        }

        return intern('L' + name + ';');
    }

    /**
     * Constructs an instance corresponding to an "uninitialized type."
     * This is a private constructor; use one of the public static
     * methods to get instances.
     * 
     * @param descriptor non-null; the field descriptor for the type
     * @param basicType basic type corresponding to this type; one of the
     * <code>BT_*</code> constants
     * @param newAt &gt;= -1 allocation bytecode index
     */
    private Type(String descriptor, int basicType, int newAt) {
        if (descriptor == null) {
            throw new NullPointerException("descriptor == null");
        }

        if ((basicType < 0) || (basicType >= BT_COUNT)) {
            throw new IllegalArgumentException("bad basicType");
        }

        if (newAt < -1) {
            throw new IllegalArgumentException("newAt < -1");
        }

        this.descriptor = descriptor;
        this.basicType = basicType;
        this.newAt = newAt;
        this.arrayType = null;
        this.componentType = null;
        this.initializedType = null;
    }

    /**
     * Constructs an instance corresponding to an "initialized type."
     * This is a private constructor; use one of the public static
     * methods to get instances.
     * 
     * @param descriptor non-null; the field descriptor for the type
     * @param basicType basic type corresponding to this type; one of the
     * <code>BT_*</code> constants
     */
    private Type(String descriptor, int basicType) {
        this(descriptor, basicType, -1);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            /*
             * Since externally-visible types are interned, this check
             * helps weed out some easy cases.
             */
            return true;
        }

        if (!(other instanceof Type)) {
            return false;
        }

        return descriptor.equals(((Type) other).descriptor);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }

    /** {@inheritDoc} */
    public int compareTo(Type other) {
        return descriptor.compareTo(other.descriptor);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return descriptor;
    }

    /** {@inheritDoc} */
    public String toHuman() {
        switch (basicType) {
            case BT_VOID:    return "void";
            case BT_BOOLEAN: return "boolean";
            case BT_BYTE:    return "byte";
            case BT_CHAR:    return "char";
            case BT_DOUBLE:  return "double";
            case BT_FLOAT:   return "float";
            case BT_INT:     return "int";
            case BT_LONG:    return "long";
            case BT_SHORT:   return "short";
            case BT_OBJECT:  break;
            default:         return descriptor;
        }

        if (isArray()) {
            return getComponentType().toHuman() + "[]";
        }

        // Remove the "L...;" around the type and convert "/" to ".".
        return getClassName().replace("/", ".");
    }

    /** {@inheritDoc} */
    public Type getType() {
        return this;
    }

    /** {@inheritDoc} */
    public Type getFrameType() {
        switch (basicType) {
            case BT_BOOLEAN:
            case BT_BYTE:
            case BT_CHAR:
            case BT_INT:
            case BT_SHORT: {
                return INT;
            }
        }

        return this;
    }

    /** {@inheritDoc} */
    public int getBasicType() {
        return basicType;
    }

    /** {@inheritDoc} */
    public int getBasicFrameType() {
        switch (basicType) {
            case BT_BOOLEAN:
            case BT_BYTE:
            case BT_CHAR:
            case BT_INT:
            case BT_SHORT: {
                return BT_INT;
            }
        }

        return basicType;
    }

    /** {@inheritDoc} */
    public boolean isConstant() {
        return false;
    }

    /**
     * Gets the descriptor.
     * 
     * @return non-null; the descriptor
     */
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * Gets the name of the class this type corresponds to, in internal
     * form. This method is only valid if this instance is for a
     * normal reference type (that is, a reference type and
     * additionally not a return address).
     * 
     * @return non-null; the internal-form class name
     */
    public String getClassName() {
        if (className == null) {
            if (!isReference()) {
                throw new IllegalArgumentException("not an object type: " +
                                                   descriptor);
            }

            if (descriptor.charAt(0) == '[') {
                className = descriptor;
            } else {
                className = descriptor.substring(1, descriptor.length() - 1);
            }
        }

        return className;
    }

    /**
     * Gets the category. Most instances are category 1. <code>long</code>
     * and <code>double</code> are the only category 2 types.
     * 
     * @see #isCategory1
     * @see #isCategory2
     * @return the category
     */
    public int getCategory() {
        switch (basicType) {
            case BT_LONG:
            case BT_DOUBLE: {
                return 2;
            }
        }

        return 1;
    }

    /**
     * Returns whether or not this is a category 1 type.
     * 
     * @see #getCategory
     * @see #isCategory2
     * @return whether or not this is a category 1 type
     */
    public boolean isCategory1() {
        switch (basicType) {
            case BT_LONG:
            case BT_DOUBLE: {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns whether or not this is a category 2 type.
     * 
     * @see #getCategory
     * @see #isCategory1
     * @return whether or not this is a category 2 type
     */
    public boolean isCategory2() {
        switch (basicType) {
            case BT_LONG:
            case BT_DOUBLE: {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets whether this type is "intlike." An intlike type is one which, when
     * placed on a stack or in a local, is automatically converted to an
     * <code>int</code>.
     * 
     * @return whether this type is "intlike"
     */
    public boolean isIntlike() {
        switch (basicType) {
            case BT_BOOLEAN:
            case BT_BYTE:
            case BT_CHAR:
            case BT_INT:
            case BT_SHORT: {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets whether this type is a primitive type. All types are either
     * primitive or reference types.
     * 
     * @return whether this type is primitive
     */
    public boolean isPrimitive() {
        switch (basicType) {
            case BT_BOOLEAN:
            case BT_BYTE:
            case BT_CHAR:
            case BT_DOUBLE:
            case BT_FLOAT:
            case BT_INT:
            case BT_LONG:
            case BT_SHORT:
            case BT_VOID: {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets whether this type is a normal reference type. A normal
     * reference type is a reference type that is not a return
     * address. This method is just convenient shorthand for
     * <code>getBasicType() == Type.BT_OBJECT</code>.
     * 
     * @return whether this type is a normal reference type
     */
    public boolean isReference() {
        return (basicType == BT_OBJECT);
    }

    /**
     * Gets whether this type is an array type. If this method returns
     * <code>true</code>, then it is safe to use {@link #getComponentType}
     * to determine the component type.
     * 
     * @return whether this type is an array type
     */
    public boolean isArray() {
        return (descriptor.charAt(0) == '[');
    }

    /**
     * Gets whether this type is an array type or is a known-null, and
     * hence is compatible with array types.
     * 
     * @return whether this type is an array type
     */
    public boolean isArrayOrKnownNull() {
        return isArray() || equals(KNOWN_NULL);
    }

    /**
     * Gets whether this type represents an uninitialized instance. An
     * uninitialized instance is what one gets back from the <code>new</code>
     * opcode, and remains uninitialized until a valid constructor is
     * invoked on it.
     * 
     * @return whether this type is "uninitialized"
     */
    public boolean isUninitialized() {
        return (newAt >= 0);
    }

    /**
     * Gets the bytecode index at which this uninitialized type was
     * allocated.  This returns <code>Integer.MAX_VALUE</code> if this
     * type is an uninitialized incoming parameter (i.e., the
     * <code>this</code> of an <code>&lt;init&gt;</code> method) or
     * <code>-1</code> if this type is in fact <i>initialized</i>.
     * 
     * @return &gt;= -1; the allocation bytecode index
     */
    public int getNewAt() {
        return newAt;
    }

    /**
     * Gets the initialized type corresponding to this instance, but only
     * if this instance is in fact an uninitialized object type.
     * 
     * @return non-null; the initialized type
     */
    public Type getInitializedType() {
        if (initializedType == null) {
            throw new IllegalArgumentException("initialized type: " +
                                               descriptor);
        }

        return initializedType;
    }

    /**
     * Gets the type corresponding to an array of this type.
     * 
     * @return non-null; the array type
     */
    public Type getArrayType() {
        if (arrayType == null) {
            arrayType = putIntern(new Type('[' + descriptor, BT_OBJECT));
        }

        return arrayType;
    }

    /**
     * Gets the component type of this type. This method is only valid on
     * array types.
     * 
     * @return non-null; the component type
     */
    public Type getComponentType() {
        if (componentType == null) {
            if (descriptor.charAt(0) != '[') {
                throw new IllegalArgumentException("not an array type: " +
                                                   descriptor);
            }
            componentType = intern(descriptor.substring(1));
        }

        return componentType;
    }

    /**
     * Returns a new interned instance which is identical to this one, except
     * it is indicated as uninitialized and allocated at the given bytecode
     * index. This instance must be an initialized object type.
     * 
     * @param newAt &gt;= 0; the allocation bytecode index
     * @return non-null; an appropriately-constructed instance
     */
    public Type asUninitialized(int newAt) {
        if (newAt < 0) {
            throw new IllegalArgumentException("newAt < 0");
        }

        if (!isReference()) {
            throw new IllegalArgumentException("not a reference type: " +
                                               descriptor);
        }

        if (isUninitialized()) {
            /*
             * Dealing with uninitialized types as a starting point is
             * a pain, and it's not clear that it'd ever be used, so
             * just disallow it.
             */
            throw new IllegalArgumentException("already uninitialized: " +
                                               descriptor);
        }

        /*
         * Create a new descriptor that is unique and shouldn't conflict
         * with "normal" type descriptors
         */
        String newDesc = 'N' + Hex.u2(newAt) + descriptor;
        Type result = new Type(newDesc, BT_OBJECT, newAt);
        result.initializedType = this;
        return putIntern(result);
    }

    /**
     * Puts the given instance in the intern table if it's not already
     * there. If a conflicting value is already in the table, then leave it.
     * Return the interned value.
     * 
     * @param type non-null; instance to make interned
     * @return non-null; the actual interned object
     */
    private static Type putIntern(Type type) {
        synchronized (internTable) {
            String descriptor = type.getDescriptor();
            Type already = internTable.get(descriptor);
            if (already != null) {
                return already;
            }
            internTable.put(descriptor, type);
            return type;
        }
    }
}
