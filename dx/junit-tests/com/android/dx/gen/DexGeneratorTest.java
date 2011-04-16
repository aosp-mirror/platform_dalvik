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

import static com.android.dx.rop.code.AccessFlags.ACC_CONSTRUCTOR;
import static com.android.dx.rop.code.AccessFlags.ACC_FINAL;
import static com.android.dx.rop.code.AccessFlags.ACC_PRIVATE;
import static com.android.dx.rop.code.AccessFlags.ACC_PROTECTED;
import static com.android.dx.rop.code.AccessFlags.ACC_PUBLIC;
import static com.android.dx.rop.code.AccessFlags.ACC_STATIC;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import junit.framework.TestCase;

/**
 * This generates a class named 'Generated' with one or more generated methods
 * and fields. In loads the generated class into the current VM and uses
 * reflection to invoke its methods.
 *
 * <p>This test must run on a Dalvik VM.
 */
public final class DexGeneratorTest extends TestCase {
    private DexGenerator generator;
    private Type<Void> voidType;
    private Type<Integer> intType;
    private Type<Long> longType;
    private Type<Boolean> booleanType;
    private Type<Float> floatType;
    private Type<Double> doubleType;
    private Type<Object> objectType;
    private Type<String> stringType;
    private Type<boolean[]> booleanArrayType;
    private Type<int[]> intArrayType;
    private Type<long[]> longArrayType;
    private Type<long[][]> long2dArrayType;
    private Type<Object[]> objectArrayType;
    private Type<DexGeneratorTest> dexGeneratorTestType;
    private Type<?> generatedType;
    private Type<Callable> callableType;
    private Method<Callable, Object> call;

    @Override protected void setUp() throws Exception {
        super.setUp();
        reset();
    }

    /**
     * The generator is mutable. Calling reset creates a new empty generator.
     * This is necessary to generate multiple classes in the same test method.
     */
    private void reset() {
        generator = new DexGenerator();
        voidType = generator.getType(void.class);
        intType = generator.getType(int.class);
        longType = generator.getType(long.class);
        booleanType = generator.getType(boolean.class);
        floatType = generator.getType(float.class);
        doubleType = generator.getType(double.class);
        objectType = generator.getType(Object.class);
        stringType = generator.getType(String.class);
        booleanArrayType = generator.getType(boolean[].class);
        intArrayType = generator.getType(int[].class);
        longArrayType = generator.getType(long[].class);
        long2dArrayType = generator.getType(long[][].class);
        objectArrayType = generator.getType(Object[].class);
        dexGeneratorTestType = generator.getType(DexGeneratorTest.class);
        generatedType = generator.getType("LGenerated;");
        callableType = generator.getType(Callable.class);
        call = callableType.getMethod(objectType, "call");
        generatedType.declare("Generated.java", ACC_PUBLIC, objectType);
    }

    public void testNewInstance() throws Exception {
        /*
         * public static Constructable call(long a, boolean b) {
         *   Constructable result = new Constructable(a, b);
         *   return result;
         * }
         */
        Type<Constructable> constructable = generator.getType(Constructable.class);
        Code code = generatedType.getMethod(constructable, "call", longType, booleanType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Long> localA = code.getParameter(0, longType);
        Local<Boolean> localB = code.getParameter(1, booleanType);
        Method<Constructable, Void> constructor
                = constructable.getConstructor(longType, booleanType);
        Local<Constructable> localResult = code.newLocal(constructable);
        code.newInstance(localResult, constructor, localA, localB);
        code.returnValue(localResult);

        Constructable constructed = (Constructable) getMethod().invoke(null, 5L, false);
        assertEquals(5L, constructed.a);
        assertEquals(false, constructed.b);
    }

    public static class Constructable {
        private final long a;
        private final boolean b;
        public Constructable(long a, boolean b) {
            this.a = a;
            this.b = b;
        }
    }

    public void testInvokeStatic() throws Exception {
        /*
         * public static int call(int a) {
         *   int result = DexGeneratorTest.staticMethod(a);
         *   return result;
         * }
         */
        Code code = generatedType.getMethod(intType, "call", intType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Integer> localA = code.getParameter(0, intType);
        Local<Integer> localResult = code.newLocal(intType);
        Method<?, Integer> staticMethod
                = dexGeneratorTestType.getMethod(intType, "staticMethod", intType);
        code.invokeStatic(staticMethod, localResult, localA);
        code.returnValue(localResult);

        assertEquals(10, getMethod().invoke(null, 4));
    }

    @SuppressWarnings("unused") // called by generated code
    public static int staticMethod(int a) {
        return a + 6;
    }

    public void testInvokeVirtual() throws Exception {
        /*
         * public static int call(DexGeneratorTest test, int a) {
         *   int result = test.virtualMethod(a);
         *   return result;
         * }
         */
        Code code = generatedType.getMethod(intType, "call", dexGeneratorTestType, intType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<DexGeneratorTest> localInstance = code.getParameter(0, dexGeneratorTestType);
        Local<Integer> localA = code.getParameter(1, intType);
        Local<Integer> localResult = code.newLocal(intType);
        Method<DexGeneratorTest, Integer> virtualMethod
                = dexGeneratorTestType.getMethod(intType, "virtualMethod", intType);
        code.invokeVirtual(virtualMethod, localResult, localInstance, localA);
        code.returnValue(localResult);

        assertEquals(9, getMethod().invoke(null, this, 4));
    }

    @SuppressWarnings("unused") // called by generated code
    public int virtualMethod(int a) {
        return a + 5;
    }

    public <G> void testInvokeDirect() throws Exception {
        /*
         * private int directMethod() {
         *   int a = 5;
         *   return a;
         * }
         *
         * public static int call(Generated g) {
         *   int b = g.directMethod();
         *   return b;
         * }
         */
        Type<G> generated = generator.getType("LGenerated;");
        Method<G, Integer> directMethod = generated.getMethod(intType, "directMethod");
        Code directCode = directMethod.declare(ACC_PRIVATE);
        directCode.getThis(generated); // 'this' is unused
        Local<Integer> localA = directCode.newLocal(intType);
        directCode.loadConstant(localA, 5);
        directCode.returnValue(localA);

        Method<G, Integer> method = generated.getMethod(intType, "call", generated);
        Code code = method.declare(ACC_PUBLIC | ACC_STATIC);
        Local<Integer> localB = code.newLocal(intType);
        Local<G> localG = code.getParameter(0, generated);
        code.invokeDirect(directMethod, localB, localG);
        code.returnValue(localB);

        addDefaultConstructor();

        Class<?> generatedClass = loadAndGenerate();
        Object instance = generatedClass.newInstance();
        java.lang.reflect.Method m = generatedClass.getMethod("call", generatedClass);
        assertEquals(5, m.invoke(null, instance));
    }

    public <G> void testInvokeSuper() throws Exception {
        /*
         * public int superHashCode() {
         *   int result = super.hashCode();
         *   return result;
         * }
         * public int hashCode() {
         *   return 0;
         * }
         */
        Type<G> generated = generator.getType("LGenerated;");
        Method<Object, Integer> objectHashCode = objectType.getMethod(intType, "hashCode");
        Code superHashCode = generated.getMethod(intType, "superHashCode").declare(ACC_PUBLIC);
        Local<Integer> localResult = superHashCode.newLocal(intType);
        Local<G> localThis = superHashCode.getThis(generated);
        superHashCode.invokeSuper(objectHashCode, localResult, localThis);
        superHashCode.returnValue(localResult);

        Code generatedHashCode = generated.getMethod(intType, "hashCode").declare(ACC_PUBLIC);
        Local<Integer> localZero = generatedHashCode.newLocal(intType);
        generatedHashCode.loadConstant(localZero, 0);
        generatedHashCode.returnValue(localZero);

        addDefaultConstructor();

        Class<?> generatedClass = loadAndGenerate();
        Object instance = generatedClass.newInstance();
        java.lang.reflect.Method m = generatedClass.getMethod("superHashCode");
        assertEquals(System.identityHashCode(instance), m.invoke(instance));
    }

    @SuppressWarnings("unused") // called by generated code
    public int superMethod(int a) {
        return a + 4;
    }

    public void testInvokeInterface() throws Exception {
        /*
         * public static Object call(Callable c) {
         *   Object result = c.call();
         *   return result;
         * }
         */
        Code code = generatedType.getMethod(objectType, "call", callableType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Callable> localC = code.getParameter(0, callableType);
        Local<Object> localResult = code.newLocal(objectType);
        code.invokeInterface(call, localResult, localC);
        code.returnValue(localResult);

        Callable<Object> callable = new Callable<Object>() {
            public Object call() throws Exception {
                return "abc";
            }
        };
        assertEquals("abc", getMethod().invoke(null, callable));
    }

    public void testParameterMismatch() throws Exception {
        Type<?>[] argTypes = {
                generator.getType(Integer.class), // should fail because the code specifies int
                objectType,
        };
        Method<?, Integer> method = generatedType.getMethod(intType, "call", argTypes);
        Code code = method.declare(ACC_PUBLIC | ACC_STATIC);
        try {
            code.getParameter(0, intType);
        } catch (IllegalArgumentException e) {
        }
        try {
            code.getParameter(2, intType);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void testInvokeTypeSafety() throws Exception {
        /*
         * public static boolean call(DexGeneratorTest test) {
         *   CharSequence cs = test.toString();
         *   boolean result = cs.equals(test);
         *   return result;
         * }
         */
        Code code = generatedType.getMethod(booleanType, "call", dexGeneratorTestType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<DexGeneratorTest> localTest = code.getParameter(0, dexGeneratorTestType);
        Type<CharSequence> charSequenceType = generator.getType(CharSequence.class);
        Method<Object, String> objectToString = objectType.getMethod(stringType, "toString");
        Method<Object, Boolean> objectEquals
                = objectType.getMethod(booleanType, "equals", objectType);
        Local<CharSequence> localCs = code.newLocal(charSequenceType);
        Local<Boolean> localResult = code.newLocal(booleanType);
        code.invokeVirtual(objectToString, localCs, localTest);
        code.invokeVirtual(objectEquals, localResult, localCs, localTest);
        code.returnValue(localResult);

        assertEquals(false, getMethod().invoke(null, this));
    }

    public void testReturnTypeMismatch() {
        Code code = generatedType.getMethod(stringType, "call")
                .declare(ACC_PUBLIC | ACC_STATIC);
        try {
            code.returnValue(code.newLocal(booleanType));
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            code.returnVoid();
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testDeclareStaticFields() throws Exception {
        /*
         * class Generated {
         *   public static int a;
         *   protected static Object b;
         * }
         */
        generatedType.getField(intType, "a").declare(ACC_PUBLIC | ACC_STATIC, 3);
        generatedType.getField(objectType, "b").declare(ACC_PROTECTED | ACC_STATIC, null);
        Class<?> generatedClass = loadAndGenerate();

        java.lang.reflect.Field a = generatedClass.getField("a");
        assertEquals(int.class, a.getType());
        assertEquals(3, a.get(null));

        java.lang.reflect.Field b = generatedClass.getDeclaredField("b");
        assertEquals(Object.class, b.getType());
        b.setAccessible(true);
        assertEquals(null, b.get(null));
    }

    public void testDeclareInstanceFields() throws Exception {
        /*
         * class Generated {
         *   public int a;
         *   protected Object b;
         * }
         */
        generatedType.getField(intType, "a").declare(ACC_PUBLIC, null);
        generatedType.getField(objectType, "b").declare(ACC_PROTECTED, null);

        addDefaultConstructor();

        Class<?> generatedClass = loadAndGenerate();
        Object instance = generatedClass.newInstance();

        java.lang.reflect.Field a = generatedClass.getField("a");
        assertEquals(int.class, a.getType());
        assertEquals(0, a.get(instance));

        java.lang.reflect.Field b = generatedClass.getDeclaredField("b");
        assertEquals(Object.class, b.getType());
        b.setAccessible(true);
        assertEquals(null, b.get(instance));
    }

    /**
     * Declare a constructor that takes an int parameter and assigns it to a
     * field.
     */
    public <G> void testDeclareConstructor() throws Exception {
        /*
         * class Generated {
         *   public final int a;
         *   public Generated(int a) {
         *     this.a = a;
         *   }
         * }
         */
        Type<G> generated = generator.getType("LGenerated;");
        Field<G, Integer> field = generated.getField(intType, "a");
        field.declare(ACC_PUBLIC | ACC_FINAL, null);
        Code code = generatedType.getConstructor(intType).declare(ACC_PUBLIC | ACC_CONSTRUCTOR);
        Local<G> thisRef = code.getThis(generated);
        Local<Integer> parameter = code.getParameter(0, intType);
        code.invokeDirect(objectType.getConstructor(), null, thisRef);
        code.iput(field, thisRef, parameter);
        code.returnVoid();

        Class<?> generatedClass = loadAndGenerate();
        java.lang.reflect.Field a = generatedClass.getField("a");
        Object instance = generatedClass.getConstructor(int.class).newInstance(0xabcd);
        assertEquals(0xabcd, a.get(instance));
    }

    public void testReturnBoolean() throws Exception {
        testReturnType(boolean.class, true);
        testReturnType(byte.class, (byte) 5);
        testReturnType(char.class, 'E');
        testReturnType(double.class, 5.0);
        testReturnType(float.class, 5.0f);
        testReturnType(int.class, 5);
        testReturnType(long.class, 5L);
        testReturnType(short.class, (short) 5);
        testReturnType(void.class, null);
        testReturnType(String.class, "foo");
        testReturnType(Class.class, List.class);
    }

    private <T> void testReturnType(Class<T> javaType, T value) throws Exception {
        /*
         * public int call() {
         *   int a = 5;
         *   return a;
         * }
         */
        reset();
        Type<T> returnType = generator.getType(javaType);
        Code code = generatedType.getMethod(returnType, "call")
                .declare(ACC_PUBLIC | ACC_STATIC);
        if (value != null) {
            Local<T> i = code.newLocal(returnType);
            code.loadConstant(i, value);
            code.returnValue(i);
        } else {
            code.returnVoid();
        }

        Class<?> generatedClass = loadAndGenerate();
        java.lang.reflect.Method method = generatedClass.getMethod("call");
        assertEquals(javaType, method.getReturnType());
        assertEquals(value, method.invoke(null));
    }

    public void testBranching() throws Exception {
        java.lang.reflect.Method lt = branchingMethod(Comparison.LT);
        assertEquals(Boolean.TRUE, lt.invoke(null, 1, 2));
        assertEquals(Boolean.FALSE, lt.invoke(null, 1, 1));
        assertEquals(Boolean.FALSE, lt.invoke(null, 2, 1));

        java.lang.reflect.Method le = branchingMethod(Comparison.LE);
        assertEquals(Boolean.TRUE, le.invoke(null, 1, 2));
        assertEquals(Boolean.TRUE, le.invoke(null, 1, 1));
        assertEquals(Boolean.FALSE, le.invoke(null, 2, 1));

        java.lang.reflect.Method eq = branchingMethod(Comparison.EQ);
        assertEquals(Boolean.FALSE, eq.invoke(null, 1, 2));
        assertEquals(Boolean.TRUE, eq.invoke(null, 1, 1));
        assertEquals(Boolean.FALSE, eq.invoke(null, 2, 1));

        java.lang.reflect.Method ge = branchingMethod(Comparison.GE);
        assertEquals(Boolean.FALSE, ge.invoke(null, 1, 2));
        assertEquals(Boolean.TRUE, ge.invoke(null, 1, 1));
        assertEquals(Boolean.TRUE, ge.invoke(null, 2, 1));

        java.lang.reflect.Method gt = branchingMethod(Comparison.GT);
        assertEquals(Boolean.FALSE, gt.invoke(null, 1, 2));
        assertEquals(Boolean.FALSE, gt.invoke(null, 1, 1));
        assertEquals(Boolean.TRUE, gt.invoke(null, 2, 1));

        java.lang.reflect.Method ne = branchingMethod(Comparison.NE);
        assertEquals(Boolean.TRUE, ne.invoke(null, 1, 2));
        assertEquals(Boolean.FALSE, ne.invoke(null, 1, 1));
        assertEquals(Boolean.TRUE, ne.invoke(null, 2, 1));
    }

    private java.lang.reflect.Method branchingMethod(Comparison comparison) throws Exception {
        /*
         * public static boolean call(int localA, int localB) {
         *   if (a comparison b) {
         *     return true;
         *   }
         *   return false;
         * }
         */
        reset();
        Code code = generatedType.getMethod(booleanType, "call", intType, intType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Integer> localA = code.getParameter(0, intType);
        Local<Integer> localB = code.getParameter(1, intType);
        Local<Boolean> result = code.newLocal(generator.getType(boolean.class));
        Label afterIf = code.newLabel();
        Label ifBody = code.newLabel();
        code.compare(comparison, localA, localB, ifBody);
        code.jump(afterIf);

        code.mark(ifBody);
        code.loadConstant(result, true);
        code.returnValue(result);

        code.mark(afterIf);
        code.loadConstant(result, false);
        code.returnValue(result);
        return getMethod();
    }

    public void testCastIntegerToInteger() throws Exception {
        java.lang.reflect.Method intToLong = numericCastingMethod(int.class, long.class);
        assertEquals(0x0000000000000000L, intToLong.invoke(null, 0x00000000));
        assertEquals(0x000000007fffffffL, intToLong.invoke(null, 0x7fffffff));
        assertEquals(0xffffffff80000000L, intToLong.invoke(null, 0x80000000));
        assertEquals(0xffffffffffffffffL, intToLong.invoke(null, 0xffffffff));

        java.lang.reflect.Method longToInt = numericCastingMethod(long.class, int.class);
        assertEquals(0x1234abcd, longToInt.invoke(null, 0x000000001234abcdL));
        assertEquals(0x1234abcd, longToInt.invoke(null, 0x123456781234abcdL));
        assertEquals(0x1234abcd, longToInt.invoke(null, 0xffffffff1234abcdL));

        java.lang.reflect.Method intToShort = numericCastingMethod(int.class, short.class);
        assertEquals((short) 0x1234, intToShort.invoke(null, 0x00001234));
        assertEquals((short) 0x1234, intToShort.invoke(null, 0xabcd1234));
        assertEquals((short) 0x1234, intToShort.invoke(null, 0xffff1234));

        java.lang.reflect.Method intToChar = numericCastingMethod(int.class, char.class);
        assertEquals((char) 0x1234, intToChar.invoke(null, 0x00001234));
        assertEquals((char) 0x1234, intToChar.invoke(null, 0xabcd1234));
        assertEquals((char) 0x1234, intToChar.invoke(null, 0xffff1234));

        java.lang.reflect.Method intToByte = numericCastingMethod(int.class, byte.class);
        assertEquals((byte) 0x34, intToByte.invoke(null, 0x00000034));
        assertEquals((byte) 0x34, intToByte.invoke(null, 0xabcd1234));
        assertEquals((byte) 0x34, intToByte.invoke(null, 0xffffff34));
    }

    public void testCastIntegerToFloatingPoint() throws Exception {
        java.lang.reflect.Method intToFloat = numericCastingMethod(int.class, float.class);
        assertEquals(0.0f, intToFloat.invoke(null, 0));
        assertEquals(-1.0f, intToFloat.invoke(null, -1));
        assertEquals(16777216f, intToFloat.invoke(null, 16777216));
        assertEquals(16777216f, intToFloat.invoke(null, 16777217)); // precision

        java.lang.reflect.Method intToDouble = numericCastingMethod(int.class, double.class);
        assertEquals(0.0, intToDouble.invoke(null, 0));
        assertEquals(-1.0, intToDouble.invoke(null, -1));
        assertEquals(16777216.0, intToDouble.invoke(null, 16777216));
        assertEquals(16777217.0, intToDouble.invoke(null, 16777217));

        java.lang.reflect.Method longToFloat = numericCastingMethod(long.class, float.class);
        assertEquals(0.0f, longToFloat.invoke(null, 0L));
        assertEquals(-1.0f, longToFloat.invoke(null, -1L));
        assertEquals(16777216f, longToFloat.invoke(null, 16777216L));
        assertEquals(16777216f, longToFloat.invoke(null, 16777217L));

        java.lang.reflect.Method longToDouble = numericCastingMethod(long.class, double.class);
        assertEquals(0.0, longToDouble.invoke(null, 0L));
        assertEquals(-1.0, longToDouble.invoke(null, -1L));
        assertEquals(9007199254740992.0, longToDouble.invoke(null, 9007199254740992L));
        assertEquals(9007199254740992.0, longToDouble.invoke(null, 9007199254740993L)); // precision
    }

    public void testCastFloatingPointToInteger() throws Exception {
        java.lang.reflect.Method floatToInt = numericCastingMethod(float.class, int.class);
        assertEquals(0, floatToInt.invoke(null, 0.0f));
        assertEquals(-1, floatToInt.invoke(null, -1.0f));
        assertEquals(Integer.MAX_VALUE, floatToInt.invoke(null, 10e15f));
        assertEquals(0, floatToInt.invoke(null, 0.5f));
        assertEquals(Integer.MIN_VALUE, floatToInt.invoke(null, Float.NEGATIVE_INFINITY));
        assertEquals(0, floatToInt.invoke(null, Float.NaN));

        java.lang.reflect.Method floatToLong = numericCastingMethod(float.class, long.class);
        assertEquals(0L, floatToLong.invoke(null, 0.0f));
        assertEquals(-1L, floatToLong.invoke(null, -1.0f));
        assertEquals(10000000272564224L, floatToLong.invoke(null, 10e15f));
        assertEquals(0L, floatToLong.invoke(null, 0.5f));
        assertEquals(Long.MIN_VALUE, floatToLong.invoke(null, Float.NEGATIVE_INFINITY));
        assertEquals(0L, floatToLong.invoke(null, Float.NaN));

        java.lang.reflect.Method doubleToInt = numericCastingMethod(double.class, int.class);
        assertEquals(0, doubleToInt.invoke(null, 0.0));
        assertEquals(-1, doubleToInt.invoke(null, -1.0));
        assertEquals(Integer.MAX_VALUE, doubleToInt.invoke(null, 10e15));
        assertEquals(0, doubleToInt.invoke(null, 0.5));
        assertEquals(Integer.MIN_VALUE, doubleToInt.invoke(null, Double.NEGATIVE_INFINITY));
        assertEquals(0, doubleToInt.invoke(null, Double.NaN));

        java.lang.reflect.Method doubleToLong = numericCastingMethod(double.class, long.class);
        assertEquals(0L, doubleToLong.invoke(null, 0.0));
        assertEquals(-1L, doubleToLong.invoke(null, -1.0));
        assertEquals(10000000000000000L, doubleToLong.invoke(null, 10e15));
        assertEquals(0L, doubleToLong.invoke(null, 0.5));
        assertEquals(Long.MIN_VALUE, doubleToLong.invoke(null, Double.NEGATIVE_INFINITY));
        assertEquals(0L, doubleToLong.invoke(null, Double.NaN));
    }

    public void testCastFloatingPointToFloatingPoint() throws Exception {
        java.lang.reflect.Method floatToDouble = numericCastingMethod(float.class, double.class);
        assertEquals(0.0, floatToDouble.invoke(null, 0.0f));
        assertEquals(-1.0, floatToDouble.invoke(null, -1.0f));
        assertEquals(0.5, floatToDouble.invoke(null, 0.5f));
        assertEquals(Double.NEGATIVE_INFINITY, floatToDouble.invoke(null, Float.NEGATIVE_INFINITY));
        assertEquals(Double.NaN, floatToDouble.invoke(null, Float.NaN));

        java.lang.reflect.Method doubleToFloat = numericCastingMethod(double.class, float.class);
        assertEquals(0.0f, doubleToFloat.invoke(null, 0.0));
        assertEquals(-1.0f, doubleToFloat.invoke(null, -1.0));
        assertEquals(0.5f, doubleToFloat.invoke(null, 0.5));
        assertEquals(Float.NEGATIVE_INFINITY, doubleToFloat.invoke(null, Double.NEGATIVE_INFINITY));
        assertEquals(Float.NaN, doubleToFloat.invoke(null, Double.NaN));
    }

    private java.lang.reflect.Method numericCastingMethod(Class<?> source, Class<?> target)
            throws Exception {
        /*
         * public static short call(int source) {
         *   short casted = (short) source;
         *   return casted;
         * }
         */
        reset();
        Type<?> sourceType = generator.getType(source);
        Type<?> targetType = generator.getType(target);
        Code code = generatedType.getMethod(targetType, "call", sourceType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<?> localSource = code.getParameter(0, sourceType);
        Local<?> localCasted = code.newLocal(targetType);
        code.numericCast(localSource, localCasted);
        code.returnValue(localCasted);
        return getMethod();
    }

    public void testNot() throws Exception {
        java.lang.reflect.Method notInteger = notMethod(int.class);
        assertEquals(0xffffffff, notInteger.invoke(null, 0x00000000));
        assertEquals(0x00000000, notInteger.invoke(null, 0xffffffff));
        assertEquals(0xedcba987, notInteger.invoke(null, 0x12345678));

        java.lang.reflect.Method notLong = notMethod(long.class);
        assertEquals(0xffffffffffffffffL, notLong.invoke(null, 0x0000000000000000L));
        assertEquals(0x0000000000000000L, notLong.invoke(null, 0xffffffffffffffffL));
        assertEquals(0x98765432edcba987L, notLong.invoke(null, 0x6789abcd12345678L));
    }

    private <T> java.lang.reflect.Method notMethod(Class<T> source) throws Exception {
        /*
         * public static short call(int source) {
         *   source = ~source;
         *   return not;
         * }
         */
        reset();
        Type<T> valueType = generator.getType(source);
        Code code = generatedType.getMethod(valueType, "call", valueType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<T> localSource = code.getParameter(0, valueType);
        code.not(localSource, localSource);
        code.returnValue(localSource);
        return getMethod();
    }

    public void testNegate() throws Exception {
        java.lang.reflect.Method negateInteger = negateMethod(int.class);
        assertEquals(0, negateInteger.invoke(null, 0));
        assertEquals(-1, negateInteger.invoke(null, 1));
        assertEquals(Integer.MIN_VALUE, negateInteger.invoke(null, Integer.MIN_VALUE));

        java.lang.reflect.Method negateLong = negateMethod(long.class);
        assertEquals(0L, negateLong.invoke(null, 0));
        assertEquals(-1L, negateLong.invoke(null, 1));
        assertEquals(Long.MIN_VALUE, negateLong.invoke(null, Long.MIN_VALUE));

        java.lang.reflect.Method negateFloat = negateMethod(float.class);
        assertEquals(-0.0f, negateFloat.invoke(null, 0.0f));
        assertEquals(-1.0f, negateFloat.invoke(null, 1.0f));
        assertEquals(Float.NaN, negateFloat.invoke(null, Float.NaN));
        assertEquals(Float.POSITIVE_INFINITY, negateFloat.invoke(null, Float.NEGATIVE_INFINITY));

        java.lang.reflect.Method negateDouble = negateMethod(double.class);
        assertEquals(-0.0, negateDouble.invoke(null, 0.0));
        assertEquals(-1.0, negateDouble.invoke(null, 1.0));
        assertEquals(Double.NaN, negateDouble.invoke(null, Double.NaN));
        assertEquals(Double.POSITIVE_INFINITY, negateDouble.invoke(null, Double.NEGATIVE_INFINITY));
    }

    private <T> java.lang.reflect.Method negateMethod(Class<T> source) throws Exception {
        /*
         * public static short call(int source) {
         *   source = -source;
         *   return not;
         * }
         */
        reset();
        Type<T> valueType = generator.getType(source);
        Code code = generatedType.getMethod(valueType, "call", valueType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<T> localSource = code.getParameter(0, valueType);
        code.negate(localSource, localSource);
        code.returnValue(localSource);
        return getMethod();
    }

    public void testIntBinaryOps() throws Exception {
        java.lang.reflect.Method add = binaryOpMethod(int.class, BinaryOp.ADD);
        assertEquals(79, add.invoke(null, 75, 4));

        java.lang.reflect.Method subtract = binaryOpMethod(int.class, BinaryOp.SUBTRACT);
        assertEquals(71, subtract.invoke(null, 75, 4));

        java.lang.reflect.Method multiply = binaryOpMethod(int.class, BinaryOp.MULTIPLY);
        assertEquals(300, multiply.invoke(null, 75, 4));

        java.lang.reflect.Method divide = binaryOpMethod(int.class, BinaryOp.DIVIDE);
        assertEquals(18, divide.invoke(null, 75, 4));
        try {
            divide.invoke(null, 75, 0);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(ArithmeticException.class, expected.getCause().getClass());
        }

        java.lang.reflect.Method remainder = binaryOpMethod(int.class, BinaryOp.REMAINDER);
        assertEquals(3, remainder.invoke(null, 75, 4));
        try {
            remainder.invoke(null, 75, 0);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(ArithmeticException.class, expected.getCause().getClass());
        }

        java.lang.reflect.Method and = binaryOpMethod(int.class, BinaryOp.AND);
        assertEquals(0xff000000, and.invoke(null, 0xff00ff00, 0xffff0000));

        java.lang.reflect.Method or = binaryOpMethod(int.class, BinaryOp.OR);
        assertEquals(0xffffff00, or.invoke(null, 0xff00ff00, 0xffff0000));

        java.lang.reflect.Method xor = binaryOpMethod(int.class, BinaryOp.XOR);
        assertEquals(0x00ffff00, xor.invoke(null, 0xff00ff00, 0xffff0000));

        java.lang.reflect.Method shiftLeft = binaryOpMethod(int.class, BinaryOp.SHIFT_LEFT);
        assertEquals(0xcd123400, shiftLeft.invoke(null, 0xabcd1234, 8));

        java.lang.reflect.Method shiftRight = binaryOpMethod(int.class, BinaryOp.SHIFT_RIGHT);
        assertEquals(0xffabcd12, shiftRight.invoke(null, 0xabcd1234, 8));

        java.lang.reflect.Method unsignedShiftRight = binaryOpMethod(int.class,
                BinaryOp.UNSIGNED_SHIFT_RIGHT);
        assertEquals(0x00abcd12, unsignedShiftRight.invoke(null, 0xabcd1234, 8));
    }

    public void testLongBinaryOps() throws Exception {
        java.lang.reflect.Method add = binaryOpMethod(long.class, BinaryOp.ADD);
        assertEquals(79L, add.invoke(null, 75L, 4L));

        java.lang.reflect.Method subtract = binaryOpMethod(long.class, BinaryOp.SUBTRACT);
        assertEquals(71L, subtract.invoke(null, 75L, 4L));

        java.lang.reflect.Method multiply = binaryOpMethod(long.class, BinaryOp.MULTIPLY);
        assertEquals(300L, multiply.invoke(null, 75L, 4L));

        java.lang.reflect.Method divide = binaryOpMethod(long.class, BinaryOp.DIVIDE);
        assertEquals(18L, divide.invoke(null, 75L, 4L));
        try {
            divide.invoke(null, 75L, 0L);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(ArithmeticException.class, expected.getCause().getClass());
        }

        java.lang.reflect.Method remainder = binaryOpMethod(long.class, BinaryOp.REMAINDER);
        assertEquals(3L, remainder.invoke(null, 75L, 4L));
        try {
            remainder.invoke(null, 75L, 0L);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(ArithmeticException.class, expected.getCause().getClass());
        }

        java.lang.reflect.Method and = binaryOpMethod(long.class, BinaryOp.AND);
        assertEquals(0xff00ff0000000000L,
                and.invoke(null, 0xff00ff00ff00ff00L, 0xffffffff00000000L));

        java.lang.reflect.Method or = binaryOpMethod(long.class, BinaryOp.OR);
        assertEquals(0xffffffffff00ff00L, or.invoke(null, 0xff00ff00ff00ff00L, 0xffffffff00000000L));

        java.lang.reflect.Method xor = binaryOpMethod(long.class, BinaryOp.XOR);
        assertEquals(0x00ff00ffff00ff00L,
                xor.invoke(null, 0xff00ff00ff00ff00L, 0xffffffff00000000L));

        java.lang.reflect.Method shiftLeft = binaryOpMethod(long.class, BinaryOp.SHIFT_LEFT);
        assertEquals(0xcdef012345678900L, shiftLeft.invoke(null, 0xabcdef0123456789L, 8L));

        java.lang.reflect.Method shiftRight = binaryOpMethod(long.class, BinaryOp.SHIFT_RIGHT);
        assertEquals(0xffabcdef01234567L, shiftRight.invoke(null, 0xabcdef0123456789L, 8L));

        java.lang.reflect.Method unsignedShiftRight = binaryOpMethod(long.class,
                BinaryOp.UNSIGNED_SHIFT_RIGHT);
        assertEquals(0x00abcdef01234567L, unsignedShiftRight.invoke(null, 0xabcdef0123456789L, 8L));
    }

    public void testFloatBinaryOps() throws Exception {
        java.lang.reflect.Method add = binaryOpMethod(float.class, BinaryOp.ADD);
        assertEquals(6.75f, add.invoke(null, 5.5f, 1.25f));

        java.lang.reflect.Method subtract = binaryOpMethod(float.class, BinaryOp.SUBTRACT);
        assertEquals(4.25f, subtract.invoke(null, 5.5f, 1.25f));

        java.lang.reflect.Method multiply = binaryOpMethod(float.class, BinaryOp.MULTIPLY);
        assertEquals(6.875f, multiply.invoke(null, 5.5f, 1.25f));

        java.lang.reflect.Method divide = binaryOpMethod(float.class, BinaryOp.DIVIDE);
        assertEquals(4.4f, divide.invoke(null, 5.5f, 1.25f));
        assertEquals(Float.POSITIVE_INFINITY, divide.invoke(null, 5.5f, 0.0f));

        java.lang.reflect.Method remainder = binaryOpMethod(float.class, BinaryOp.REMAINDER);
        assertEquals(0.5f, remainder.invoke(null, 5.5f, 1.25f));
        assertEquals(Float.NaN, remainder.invoke(null, 5.5f, 0.0f));
    }

    public void testDoubleBinaryOps() throws Exception {
        java.lang.reflect.Method add = binaryOpMethod(double.class, BinaryOp.ADD);
        assertEquals(6.75, add.invoke(null, 5.5, 1.25));

        java.lang.reflect.Method subtract = binaryOpMethod(double.class, BinaryOp.SUBTRACT);
        assertEquals(4.25, subtract.invoke(null, 5.5, 1.25));

        java.lang.reflect.Method multiply = binaryOpMethod(double.class, BinaryOp.MULTIPLY);
        assertEquals(6.875, multiply.invoke(null, 5.5, 1.25));

        java.lang.reflect.Method divide = binaryOpMethod(double.class, BinaryOp.DIVIDE);
        assertEquals(4.4, divide.invoke(null, 5.5, 1.25));
        assertEquals(Double.POSITIVE_INFINITY, divide.invoke(null, 5.5, 0.0));

        java.lang.reflect.Method remainder = binaryOpMethod(double.class, BinaryOp.REMAINDER);
        assertEquals(0.5, remainder.invoke(null, 5.5, 1.25));
        assertEquals(Double.NaN, remainder.invoke(null, 5.5, 0.0));
    }

    private <T> java.lang.reflect.Method binaryOpMethod(Class<T> valueClass, BinaryOp op)
            throws Exception {
        /*
         * public static int binaryOp(int a, int b) {
         *   int result = a + b;
         *   return result;
         * }
         */
        reset();
        Type<T> valueType = generator.getType(valueClass);
        Code code = generatedType.getMethod(valueType, "call", valueType,valueType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<T> localA = code.getParameter(0, valueType);
        Local<T> localB = code.getParameter(1, valueType);
        Local<T> localResult = code.newLocal(valueType);
        code.op(op, localResult, localA, localB);
        code.returnValue(localResult);
        return getMethod();
    }

    public void testReadAndWriteInstanceFields() throws Exception {
        Instance instance = new Instance();

        java.lang.reflect.Method intSwap = instanceSwapMethod(int.class, "intValue");
        instance.intValue = 5;
        assertEquals(5, intSwap.invoke(null, instance, 10));
        assertEquals(10, instance.intValue);

        java.lang.reflect.Method longSwap = instanceSwapMethod(long.class, "longValue");
        instance.longValue = 500L;
        assertEquals(500L, longSwap.invoke(null, instance, 1234L));
        assertEquals(1234L, instance.longValue);

        java.lang.reflect.Method booleanSwap = instanceSwapMethod(boolean.class, "booleanValue");
        instance.booleanValue = false;
        assertEquals(false, booleanSwap.invoke(null, instance, true));
        assertEquals(true, instance.booleanValue);

        java.lang.reflect.Method floatSwap = instanceSwapMethod(float.class, "floatValue");
        instance.floatValue = 1.5f;
        assertEquals(1.5f, floatSwap.invoke(null, instance, 0.5f));
        assertEquals(0.5f, instance.floatValue);

        java.lang.reflect.Method doubleSwap = instanceSwapMethod(double.class, "doubleValue");
        instance.doubleValue = 155.5;
        assertEquals(155.5, doubleSwap.invoke(null, instance, 266.6));
        assertEquals(266.6, instance.doubleValue);

        java.lang.reflect.Method objectSwap = instanceSwapMethod(Object.class, "objectValue");
        instance.objectValue = "before";
        assertEquals("before", objectSwap.invoke(null, instance, "after"));
        assertEquals("after", instance.objectValue);

        java.lang.reflect.Method byteSwap = instanceSwapMethod(byte.class, "byteValue");
        instance.byteValue = 0x35;
        assertEquals((byte) 0x35, byteSwap.invoke(null, instance, (byte) 0x64));
        assertEquals((byte) 0x64, instance.byteValue);

        java.lang.reflect.Method charSwap = instanceSwapMethod(char.class, "charValue");
        instance.charValue = 'A';
        assertEquals('A', charSwap.invoke(null, instance, 'B'));
        assertEquals('B', instance.charValue);

        java.lang.reflect.Method shortSwap = instanceSwapMethod(short.class, "shortValue");
        instance.shortValue = (short) 0xabcd;
        assertEquals((short) 0xabcd, shortSwap.invoke(null, instance, (short) 0x1234));
        assertEquals((short) 0x1234, instance.shortValue);
    }

    public class Instance {
        public int intValue;
        public long longValue;
        public float floatValue;
        public double doubleValue;
        public Object objectValue;
        public boolean booleanValue;
        public byte byteValue;
        public char charValue;
        public short shortValue;
    }

    private <V> java.lang.reflect.Method instanceSwapMethod(
            Class<V> valueClass, String fieldName) throws Exception {
        /*
         * public static int call(Instance instance, int newValue) {
         *   int oldValue = instance.intValue;
         *   instance.intValue = newValue;
         *   return oldValue;
         * }
         */
        reset();
        Type<V> valueType = generator.getType(valueClass);
        Type<Instance> objectType = generator.getType(Instance.class);
        Field<Instance, V> field = objectType.getField(valueType, fieldName);
        Code code = generatedType.getMethod(valueType, "call", objectType, valueType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Instance> localInstance = code.getParameter(0, objectType);
        Local<V> localNewValue = code.getParameter(1, valueType);
        Local<V> localOldValue = code.newLocal(valueType);
        code.iget(field, localInstance, localOldValue);
        code.iput(field, localInstance, localNewValue);
        code.returnValue(localOldValue);
        return getMethod();
    }

    public void testReadAndWriteStaticFields() throws Exception {
        java.lang.reflect.Method intSwap = staticSwapMethod(int.class, "intValue");
        Static.intValue = 5;
        assertEquals(5, intSwap.invoke(null, 10));
        assertEquals(10, Static.intValue);

        java.lang.reflect.Method longSwap = staticSwapMethod(long.class, "longValue");
        Static.longValue = 500L;
        assertEquals(500L, longSwap.invoke(null, 1234L));
        assertEquals(1234L, Static.longValue);

        java.lang.reflect.Method booleanSwap = staticSwapMethod(boolean.class, "booleanValue");
        Static.booleanValue = false;
        assertEquals(false, booleanSwap.invoke(null, true));
        assertEquals(true, Static.booleanValue);

        java.lang.reflect.Method floatSwap = staticSwapMethod(float.class, "floatValue");
        Static.floatValue = 1.5f;
        assertEquals(1.5f, floatSwap.invoke(null, 0.5f));
        assertEquals(0.5f, Static.floatValue);

        java.lang.reflect.Method doubleSwap = staticSwapMethod(double.class, "doubleValue");
        Static.doubleValue = 155.5;
        assertEquals(155.5, doubleSwap.invoke(null, 266.6));
        assertEquals(266.6, Static.doubleValue);

        java.lang.reflect.Method objectSwap = staticSwapMethod(Object.class, "objectValue");
        Static.objectValue = "before";
        assertEquals("before", objectSwap.invoke(null, "after"));
        assertEquals("after", Static.objectValue);

        java.lang.reflect.Method byteSwap = staticSwapMethod(byte.class, "byteValue");
        Static.byteValue = 0x35;
        assertEquals((byte) 0x35, byteSwap.invoke(null, (byte) 0x64));
        assertEquals((byte) 0x64, Static.byteValue);

        java.lang.reflect.Method charSwap = staticSwapMethod(char.class, "charValue");
        Static.charValue = 'A';
        assertEquals('A', charSwap.invoke(null, 'B'));
        assertEquals('B', Static.charValue);

        java.lang.reflect.Method shortSwap = staticSwapMethod(short.class, "shortValue");
        Static.shortValue = (short) 0xabcd;
        assertEquals((short) 0xabcd, shortSwap.invoke(null, (short) 0x1234));
        assertEquals((short) 0x1234, Static.shortValue);
    }

    public static class Static {
        public static int intValue;
        public static long longValue;
        public static float floatValue;
        public static double doubleValue;
        public static Object objectValue;
        public static boolean booleanValue;
        public static byte byteValue;
        public static char charValue;
        public static short shortValue;
    }

    private <V> java.lang.reflect.Method staticSwapMethod(Class<V> valueClass, String fieldName)
            throws Exception {
        /*
         * public static int call(int newValue) {
         *   int oldValue = Static.intValue;
         *   Static.intValue = newValue;
         *   return oldValue;
         * }
         */
        reset();
        Type<V> valueType = generator.getType(valueClass);
        Type<Static> objectType = generator.getType(Static.class);
        Field<Static, V> field = objectType.getField(valueType, fieldName);
        Code code = generatedType.getMethod(valueType, "call", valueType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<V> localNewValue = code.getParameter(0, valueType);
        Local<V> localOldValue = code.newLocal(valueType);
        code.sget(field, localOldValue);
        code.sput(field, localNewValue);
        code.returnValue(localOldValue);
        return getMethod();
    }

    public void testTypeCast() throws Exception {
        /*
         * public static String call(Object o) {
         *   String s = (String) o;
         * }
         */
        Code code = generatedType.getMethod(stringType, "call", objectType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Object> localObject = code.getParameter(0, objectType);
        Local<String> localString = code.newLocal(stringType);
        code.typeCast(localObject, localString);
        code.returnValue(localString);

        java.lang.reflect.Method method = getMethod();
        assertEquals("s", method.invoke(null, "s"));
        assertEquals(null, method.invoke(null, (String) null));
        try {
            method.invoke(null, 5);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(ClassCastException.class, expected.getCause().getClass());
        }
    }

    public void testInstanceOf() throws Exception {
        /*
         * public static boolean call(Object o) {
         *   boolean result = o instanceof String;
         *   return result;
         * }
         */
        Code code = generatedType.getMethod(booleanType, "call", objectType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Object> localObject = code.getParameter(0, objectType);
        Local<Boolean> localResult = code.newLocal(booleanType);
        code.instanceOfType(localResult, localObject, stringType);
        code.returnValue(localResult);

        java.lang.reflect.Method method = getMethod();
        assertEquals(true, method.invoke(null, "s"));
        assertEquals(false, method.invoke(null, (String) null));
        assertEquals(false, method.invoke(null, 5));
    }

    /**
     * Tests that we can construct a for loop.
     */
    public void testForLoop() throws Exception {
        /*
         * public static int call(int count) {
         *   int result = 1;
         *   for (int i = 0; i < count; i += 1) {
         *     result = result * 2;
         *   }
         *   return result;
         * }
         */
        Code code = generatedType.getMethod(intType, "call", intType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Integer> localCount = code.getParameter(0, intType);
        Local<Integer> localResult = code.newLocal(intType);
        Local<Integer> localI = code.newLocal(intType);
        Local<Integer> local1 = code.newLocal(intType);
        Local<Integer> local2 = code.newLocal(intType);
        code.loadConstant(local1, 1);
        code.loadConstant(local2, 2);
        code.loadConstant(localResult, 1);
        code.loadConstant(localI, 0);
        Label loopCondition = code.newLabel();
        Label loopBody = code.newLabel();
        Label afterLoop = code.newLabel();
        code.mark(loopCondition);
        code.compare(Comparison.LT, localI, localCount, loopBody);
        code.jump(afterLoop);
        code.mark(loopBody);
        code.op(BinaryOp.MULTIPLY, localResult, localResult, local2);
        code.op(BinaryOp.ADD, localI, localI, local1);
        code.jump(loopCondition);
        code.mark(afterLoop);
        code.returnValue(localResult);

        java.lang.reflect.Method pow2 = getMethod();
        assertEquals(1, pow2.invoke(null, 0));
        assertEquals(2, pow2.invoke(null, 1));
        assertEquals(4, pow2.invoke(null, 2));
        assertEquals(8, pow2.invoke(null, 3));
        assertEquals(16, pow2.invoke(null, 4));
    }

    /**
     * Tests that we can construct a while loop.
     */
    public void testWhileLoop() throws Exception {
        /*
         * public static int call(int max) {
         *   int result = 1;
         *   while (result < max) {
         *     result = result * 2;
         *   }
         *   return result;
         * }
         */
        Code code = generatedType.getMethod(intType, "call", intType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Integer> localMax = code.getParameter(0, intType);
        Local<Integer> localResult = code.newLocal(intType);
        Local<Integer> local2 = code.newLocal(intType);
        code.loadConstant(localResult, 1);
        code.loadConstant(local2, 2);
        Label loopCondition = code.newLabel();
        Label loopBody = code.newLabel();
        Label afterLoop = code.newLabel();
        code.mark(loopCondition);
        code.compare(Comparison.LT, localResult, localMax, loopBody);
        code.jump(afterLoop);
        code.mark(loopBody);
        code.op(BinaryOp.MULTIPLY, localResult, localResult, local2);
        code.jump(loopCondition);
        code.mark(afterLoop);
        code.returnValue(localResult);

        java.lang.reflect.Method ceilPow2 = getMethod();
        assertEquals(1, ceilPow2.invoke(null, 1));
        assertEquals(2, ceilPow2.invoke(null, 2));
        assertEquals(4, ceilPow2.invoke(null, 3));
        assertEquals(16, ceilPow2.invoke(null, 10));
        assertEquals(128, ceilPow2.invoke(null, 100));
        assertEquals(1024, ceilPow2.invoke(null, 1000));
    }

    public void testIfElseBlock() throws Exception {
        /*
         * public static int call(int a, int b, int c) {
         *   if (a < b) {
         *     if (a < c) {
         *       return a;
         *     } else {
         *       return c;
         *     }
         *   } else if (b < c) {
         *     return b;
         *   } else {
         *     return c;
         *   }
         * }
         */
        Code code = generatedType.getMethod(intType, "call", intType, intType, intType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Integer> localA = code.getParameter(0, intType);
        Local<Integer> localB = code.getParameter(1, intType);
        Local<Integer> localC = code.getParameter(2, intType);
        Label aLessThanB = code.newLabel();
        Label aLessThanC = code.newLabel();
        Label bLessThanC = code.newLabel();
        code.compare(Comparison.LT, localA, localB, aLessThanB);
        code.compare(Comparison.LT, localB, localC, bLessThanC);
        code.returnValue(localC);
        // (a < b)
        code.mark(aLessThanB);
        code.compare(Comparison.LT, localA, localC, aLessThanC);
        code.returnValue(localC);
        // (a < c)
        code.mark(aLessThanC);
        code.returnValue(localA);
        // (b < c)
        code.mark(bLessThanC);
        code.returnValue(localB);

        java.lang.reflect.Method min = getMethod();
        assertEquals(1, min.invoke(null, 1, 2, 3));
        assertEquals(1, min.invoke(null, 2, 3, 1));
        assertEquals(1, min.invoke(null, 2, 1, 3));
        assertEquals(1, min.invoke(null, 3, 2, 1));
    }

    public void testRecursion() throws Exception {
        /*
         * public static int call(int a) {
         *   if (a < 2) {
         *     return a;
         *   }
         *   a -= 1;
         *   int x = call(a)
         *   a -= 1;
         *   int y = call(a);
         *   int result = x + y;
         *   return result;
         * }
         */
        Method<?, Integer> c = generatedType.getMethod(intType, "call", intType);
        Code code = c.declare(ACC_PUBLIC | ACC_STATIC);
        Local<Integer> localA = code.getParameter(0, intType);
        Local<Integer> local1 = code.newLocal(intType);
        Local<Integer> local2 = code.newLocal(intType);
        Local<Integer> localX = code.newLocal(intType);
        Local<Integer> localY = code.newLocal(intType);
        Local<Integer> localResult = code.newLocal(intType);
        Label baseCase = code.newLabel();
        code.loadConstant(local1, 1);
        code.loadConstant(local2, 2);
        code.compare(Comparison.LT, localA, local2, baseCase);
        code.op(BinaryOp.SUBTRACT, localA, localA, local1);
        code.invokeStatic(c, localX, localA);
        code.op(BinaryOp.SUBTRACT, localA, localA, local1);
        code.invokeStatic(c, localY, localA);
        code.op(BinaryOp.ADD, localResult, localX, localY);
        code.returnValue(localResult);
        code.mark(baseCase);
        code.returnValue(localA);

        java.lang.reflect.Method fib = getMethod();
        assertEquals(0, fib.invoke(null, 0));
        assertEquals(1, fib.invoke(null, 1));
        assertEquals(1, fib.invoke(null, 2));
        assertEquals(2, fib.invoke(null, 3));
        assertEquals(3, fib.invoke(null, 4));
        assertEquals(5, fib.invoke(null, 5));
        assertEquals(8, fib.invoke(null, 6));
    }

    public void testCatchExceptions() throws Exception {
        /*
         * public static String call(int i) {
         *   try {
         *     DexGeneratorTest.thrower(i);
         *     return "NONE";
         *   } catch (IllegalArgumentException e) {
         *     return "IAE";
         *   } catch (IllegalStateException e) {
         *     return "ISE";
         *   } catch (RuntimeException e) {
         *     return "RE";
         *   }
         */
        Code code = generatedType.getMethod(stringType, "call", intType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Integer> localI = code.getParameter(0, intType);
        Local<String> result = code.newLocal(stringType);
        Label catchIae = code.newLabel();
        Label catchIse = code.newLabel();
        Label catchRe = code.newLabel();

        code.addCatchClause(generator.getType(IllegalArgumentException.class), catchIae);
        code.addCatchClause(generator.getType(IllegalStateException.class), catchIse);
        code.addCatchClause(generator.getType(RuntimeException.class), catchRe);
        Method<?, ?> thrower = dexGeneratorTestType.getMethod(voidType, "thrower", intType);
        code.invokeStatic(thrower, null, localI);
        code.loadConstant(result, "NONE");
        code.returnValue(result);

        code.mark(catchIae);
        code.loadConstant(result, "IAE");
        code.returnValue(result);

        code.mark(catchIse);
        code.loadConstant(result, "ISE");
        code.returnValue(result);

        code.mark(catchRe);
        code.loadConstant(result, "RE");
        code.returnValue(result);

        java.lang.reflect.Method method = getMethod();
        assertEquals("NONE", method.invoke(null, 0));
        assertEquals("IAE", method.invoke(null, 1));
        assertEquals("ISE", method.invoke(null, 2));
        assertEquals("RE", method.invoke(null, 3));
        try {
            method.invoke(null, 4);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(IOException.class, expected.getCause().getClass());
        }
    }

    @SuppressWarnings("unused") // called by generated code
    public static void thrower(int a) throws Exception {
        switch (a) {
        case 0:
            return;
        case 1:
            throw new IllegalArgumentException();
        case 2:
            throw new IllegalStateException();
        case 3:
            throw new UnsupportedOperationException();
        case 4:
            throw new IOException();
        default:
            throw new AssertionError();
        }
    }

    public void testNestedCatchClauses() throws Exception {
        /*
         * public static String call(int a, int b, int c) {
         *   try {
         *     DexGeneratorTest.thrower(a);
         *     try {
         *       DexGeneratorTest.thrower(b);
         *     } catch (IllegalArgumentException) {
         *       return "INNER";
         *     }
         *     DexGeneratorTest.thrower(c);
         *     return "NONE";
         *   } catch (IllegalArgumentException e) {
         *     return "OUTER";
         *   }
         */
        Code code = generatedType.getMethod(stringType, "call", intType, intType, intType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Integer> localA = code.getParameter(0, intType);
        Local<Integer> localB = code.getParameter(1, intType);
        Local<Integer> localC = code.getParameter(2, intType);
        Local<String> localResult = code.newLocal(stringType);
        Label catchInner = code.newLabel();
        Label catchOuter = code.newLabel();

        Type<IllegalArgumentException> iaeType = generator.getType(IllegalArgumentException.class);
        code.addCatchClause(iaeType, catchOuter);

        Method<?, ?> thrower = dexGeneratorTestType.getMethod(voidType, "thrower", intType);
        code.invokeStatic(thrower, null, localA);

        // for the inner catch clause, we stash the old label and put it back afterwards.
        Label previousLabel = code.removeCatchClause(iaeType);
        code.addCatchClause(iaeType, catchInner);
        code.invokeStatic(thrower, null, localB);
        code.removeCatchClause(iaeType);
        code.addCatchClause(iaeType, previousLabel);
        code.invokeStatic(thrower, null, localC);
        code.loadConstant(localResult, "NONE");
        code.returnValue(localResult);

        code.mark(catchInner);
        code.loadConstant(localResult, "INNER");
        code.returnValue(localResult);

        code.mark(catchOuter);
        code.loadConstant(localResult, "OUTER");
        code.returnValue(localResult);

        java.lang.reflect.Method method = getMethod();
        assertEquals("OUTER", method.invoke(null, 1, 0, 0));
        assertEquals("INNER", method.invoke(null, 0, 1, 0));
        assertEquals("OUTER", method.invoke(null, 0, 0, 1));
        assertEquals("NONE", method.invoke(null, 0, 0, 0));
    }

    public void testThrow() throws Exception {
        /*
         * public static void call() {
         *   throw new IllegalStateException();
         * }
         */
        Code code = generatedType.getMethod(voidType, "call")
                .declare(ACC_PUBLIC | ACC_STATIC);
        Type<IllegalStateException> iseType = generator.getType(IllegalStateException.class);
        Method<IllegalStateException, Void> iseConstructor = iseType.getConstructor();
        Local<IllegalStateException> localIse = code.newLocal(iseType);
        code.newInstance(localIse, iseConstructor);
        code.throwValue(localIse);

        try {
            getMethod().invoke(null);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(IllegalStateException.class, expected.getCause().getClass());
        }
    }

    public void testUnusedParameters() throws Exception {
        /*
         * public static void call(int unused1, long unused2, long unused3) {}
         */
        Code code = generatedType.getMethod(voidType, "call", intType, longType, longType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        code.returnVoid();
        getMethod().invoke(null, 1, 2, 3);
    }

    public void testFloatingPointCompare() throws Exception {
        java.lang.reflect.Method floatG = floatingPointCompareMethod(floatType, 1);
        assertEquals(-1, floatG.invoke(null, 1.0f, Float.POSITIVE_INFINITY));
        assertEquals(-1, floatG.invoke(null, 1.0f, 2.0f));
        assertEquals(0, floatG.invoke(null, 1.0f, 1.0f));
        assertEquals(1, floatG.invoke(null, 2.0f, 1.0f));
        assertEquals(1, floatG.invoke(null, 1.0f, Float.NaN));
        assertEquals(1, floatG.invoke(null, Float.NaN, 1.0f));
        assertEquals(1, floatG.invoke(null, Float.NaN, Float.NaN));
        assertEquals(1, floatG.invoke(null, Float.NaN, Float.POSITIVE_INFINITY));

        java.lang.reflect.Method floatL = floatingPointCompareMethod(floatType, -1);
        assertEquals(-1, floatG.invoke(null, 1.0f, Float.POSITIVE_INFINITY));
        assertEquals(-1, floatL.invoke(null, 1.0f, 2.0f));
        assertEquals(0, floatL.invoke(null, 1.0f, 1.0f));
        assertEquals(1, floatL.invoke(null, 2.0f, 1.0f));
        assertEquals(-1, floatL.invoke(null, 1.0f, Float.NaN));
        assertEquals(-1, floatL.invoke(null, Float.NaN, 1.0f));
        assertEquals(-1, floatL.invoke(null, Float.NaN, Float.NaN));
        assertEquals(-1, floatL.invoke(null, Float.NaN, Float.POSITIVE_INFINITY));

        java.lang.reflect.Method doubleG = floatingPointCompareMethod(doubleType, 1);
        assertEquals(-1, doubleG.invoke(null, 1.0, Double.POSITIVE_INFINITY));
        assertEquals(-1, doubleG.invoke(null, 1.0, 2.0));
        assertEquals(0, doubleG.invoke(null, 1.0, 1.0));
        assertEquals(1, doubleG.invoke(null, 2.0, 1.0));
        assertEquals(1, doubleG.invoke(null, 1.0, Double.NaN));
        assertEquals(1, doubleG.invoke(null, Double.NaN, 1.0));
        assertEquals(1, doubleG.invoke(null, Double.NaN, Double.NaN));
        assertEquals(1, doubleG.invoke(null, Double.NaN, Double.POSITIVE_INFINITY));

        java.lang.reflect.Method doubleL = floatingPointCompareMethod(doubleType, -1);
        assertEquals(-1, doubleL.invoke(null, 1.0, Double.POSITIVE_INFINITY));
        assertEquals(-1, doubleL.invoke(null, 1.0, 2.0));
        assertEquals(0, doubleL.invoke(null, 1.0, 1.0));
        assertEquals(1, doubleL.invoke(null, 2.0, 1.0));
        assertEquals(-1, doubleL.invoke(null, 1.0, Double.NaN));
        assertEquals(-1, doubleL.invoke(null, Double.NaN, 1.0));
        assertEquals(-1, doubleL.invoke(null, Double.NaN, Double.NaN));
        assertEquals(-1, doubleL.invoke(null, Double.NaN, Double.POSITIVE_INFINITY));
    }

    private <T extends Number> java.lang.reflect.Method floatingPointCompareMethod(
            Type<T> valueType, int nanValue) throws Exception {
        /*
         * public static int call(float a, float b) {
         *     int result = a <=> b;
         *     return result;
         * }
         */
        reset();
        Code code = generatedType.getMethod(intType, "call", valueType, valueType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<T> localA = code.getParameter(0, valueType);
        Local<T> localB = code.getParameter(1, valueType);
        Local<Integer> localResult = code.newLocal(intType);
        code.compare(localA, localB, localResult, nanValue);
        code.returnValue(localResult);
        return getMethod();
    }

    public void testLongCompare() throws Exception {
        /*
         * public static int call(long a, long b) {
         *   int result = a <=> b;
         *   return result;
         * }
         */
        Code code = generatedType.getMethod(intType, "call", longType, longType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Long> localA = code.getParameter(0, longType);
        Local<Long> localB = code.getParameter(1, longType);
        Local<Integer> localResult = code.newLocal(intType);
        code.compare(localA, localB, localResult);
        code.returnValue(localResult);

        java.lang.reflect.Method method = getMethod();
        assertEquals(0, method.invoke(null, Long.MIN_VALUE, Long.MIN_VALUE));
        assertEquals(-1, method.invoke(null, Long.MIN_VALUE, 0));
        assertEquals(-1, method.invoke(null, Long.MIN_VALUE, Long.MAX_VALUE));
        assertEquals(1, method.invoke(null, 0, Long.MIN_VALUE));
        assertEquals(0, method.invoke(null, 0, 0));
        assertEquals(-1, method.invoke(null, 0, Long.MAX_VALUE));
        assertEquals(1, method.invoke(null, Long.MAX_VALUE, Long.MIN_VALUE));
        assertEquals(1, method.invoke(null, Long.MAX_VALUE, 0));
        assertEquals(0, method.invoke(null, Long.MAX_VALUE, Long.MAX_VALUE));
    }

    public void testArrayLength() throws Exception {
        java.lang.reflect.Method booleanArrayLength = arrayLengthMethod(booleanArrayType);
        assertEquals(0, booleanArrayLength.invoke(null, new Object[] { new boolean[0] }));
        assertEquals(5, booleanArrayLength.invoke(null, new Object[] { new boolean[5] }));

        java.lang.reflect.Method intArrayLength = arrayLengthMethod(intArrayType);
        assertEquals(0, intArrayLength.invoke(null, new Object[] { new int[0] }));
        assertEquals(5, intArrayLength.invoke(null, new Object[] { new int[5] }));

        java.lang.reflect.Method longArrayLength = arrayLengthMethod(longArrayType);
        assertEquals(0, longArrayLength.invoke(null, new Object[] { new long[0] }));
        assertEquals(5, longArrayLength.invoke(null, new Object[] { new long[5] }));

        java.lang.reflect.Method objectArrayLength = arrayLengthMethod(objectArrayType);
        assertEquals(0, objectArrayLength.invoke(null, new Object[] { new Object[0] }));
        assertEquals(5, objectArrayLength.invoke(null, new Object[] { new Object[5] }));

        java.lang.reflect.Method long2dArrayLength = arrayLengthMethod(long2dArrayType);
        assertEquals(0, long2dArrayLength.invoke(null, new Object[] { new long[0][0] }));
        assertEquals(5, long2dArrayLength.invoke(null, new Object[] { new long[5][10] }));
    }

    private <T> java.lang.reflect.Method arrayLengthMethod(Type<T> valueType) throws Exception {
        /*
         * public static int call(long[] array) {
         *   int result = array.length;
         *   return result;
         * }
         */
        reset();
        Code code = generatedType.getMethod(intType, "call", valueType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<T> localArray = code.getParameter(0, valueType);
        Local<Integer> localResult = code.newLocal(intType);
        code.arrayLength(localArray, localResult);
        code.returnValue(localResult);
        return getMethod();
    }

    public void testNewArray() throws Exception {
        java.lang.reflect.Method newBooleanArray = newArrayMethod(booleanArrayType);
        assertEquals("[]", Arrays.toString((boolean[]) newBooleanArray.invoke(null, 0)));
        assertEquals("[false, false, false]",
                Arrays.toString((boolean[]) newBooleanArray.invoke(null, 3)));

        java.lang.reflect.Method newIntArray = newArrayMethod(intArrayType);
        assertEquals("[]", Arrays.toString((int[]) newIntArray.invoke(null, 0)));
        assertEquals("[0, 0, 0]", Arrays.toString((int[]) newIntArray.invoke(null, 3)));

        java.lang.reflect.Method newLongArray = newArrayMethod(longArrayType);
        assertEquals("[]", Arrays.toString((long[]) newLongArray.invoke(null, 0)));
        assertEquals("[0, 0, 0]", Arrays.toString((long[]) newLongArray.invoke(null, 3)));

        java.lang.reflect.Method newObjectArray = newArrayMethod(objectArrayType);
        assertEquals("[]", Arrays.toString((Object[]) newObjectArray.invoke(null, 0)));
        assertEquals("[null, null, null]",
                Arrays.toString((Object[]) newObjectArray.invoke(null, 3)));

        java.lang.reflect.Method new2dLongArray = newArrayMethod(long2dArrayType);
        assertEquals("[]", Arrays.deepToString((long[][]) new2dLongArray.invoke(null, 0)));
        assertEquals("[null, null, null]",
                Arrays.deepToString((long[][]) new2dLongArray.invoke(null, 3)));
    }

    private <T> java.lang.reflect.Method newArrayMethod(Type<T> valueType) throws Exception {
        /*
         * public static long[] call(int length) {
         *   long[] result = new long[length];
         *   return result;
         * }
         */
        reset();
        Code code = generatedType.getMethod(valueType, "call", intType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<Integer> localLength = code.getParameter(0, intType);
        Local<T> localResult = code.newLocal(valueType);
        code.newArray(localLength, localResult);
        code.returnValue(localResult);
        return getMethod();
    }

    public void testReadAndWriteArray() throws Exception {
        java.lang.reflect.Method swapBooleanArray = arraySwapMethod(booleanArrayType, booleanType);
        boolean[] booleans = new boolean[3];
        assertEquals(false, swapBooleanArray.invoke(null, booleans, 1, true));
        assertEquals("[false, true, false]", Arrays.toString(booleans));

        java.lang.reflect.Method swapIntArray = arraySwapMethod(intArrayType, intType);
        int[] ints = new int[3];
        assertEquals(0, swapIntArray.invoke(null, ints, 1, 5));
        assertEquals("[0, 5, 0]", Arrays.toString(ints));

        java.lang.reflect.Method swapLongArray = arraySwapMethod(longArrayType, longType);
        long[] longs = new long[3];
        assertEquals(0L, swapLongArray.invoke(null, longs, 1, 6L));
        assertEquals("[0, 6, 0]", Arrays.toString(longs));

        java.lang.reflect.Method swapObjectArray = arraySwapMethod(objectArrayType, objectType);
        Object[] objects = new Object[3];
        assertEquals(null, swapObjectArray.invoke(null, objects, 1, "X"));
        assertEquals("[null, X, null]", Arrays.toString(objects));

        java.lang.reflect.Method swapLong2dArray = arraySwapMethod(long2dArrayType, longArrayType);
        long[][] longs2d = new long[3][];
        assertEquals(null, swapLong2dArray.invoke(null, longs2d, 1, new long[] { 7 }));
        assertEquals("[null, [7], null]", Arrays.deepToString(longs2d));
    }

    private <A, T> java.lang.reflect.Method arraySwapMethod(Type<A> arrayType, Type<T> singleType)
            throws Exception {
        /*
         * public static long swap(long[] array, int index, long newValue) {
         *   long result = array[index];
         *   array[index] = newValue;
         *   return result;
         * }
         */
        reset();
        Code code = generatedType.getMethod(singleType, "call", arrayType, intType, singleType)
                .declare(ACC_PUBLIC | ACC_STATIC);
        Local<A> localArray = code.getParameter(0, arrayType);
        Local<Integer> localIndex = code.getParameter(1, intType);
        Local<T> localNewValue = code.getParameter(2, singleType);
        Local<T> localResult = code.newLocal(singleType);
        code.aget(localArray, localIndex, localResult);
        code.aput(localArray, localIndex, localNewValue);
        code.returnValue(localResult);
        return getMethod();
    }

    // TODO: fail if a label is unreachable (never navigated to)

    // TODO: more strict type parameters: Integer on methods

    // TODO: don't generate multiple times (?)

    private void addDefaultConstructor() {
        Code code = generatedType.getConstructor().declare(ACC_PUBLIC | ACC_CONSTRUCTOR);
        Local<?> thisRef = code.getThis(generatedType);
        code.invokeDirect(objectType.getConstructor(), null, thisRef);
        code.returnVoid();
    }

    /**
     * Returns the generated method.
     */
    private java.lang.reflect.Method getMethod() throws Exception {
        Class<?> generated = loadAndGenerate();
        for (java.lang.reflect.Method method : generated.getMethods()) {
            if (method.getName().equals("call")) {
                return method;
            }
        }
        throw new IllegalStateException("no call() method");
    }

    private Class<?> loadAndGenerate() throws IOException, ClassNotFoundException {
        return generator.load(DexGeneratorTest.class.getClassLoader()).loadClass("Generated");
    }
}
