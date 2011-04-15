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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final Map<Class<?>, Class<?>> BOXED_TO_PRIMITIVE
            = new HashMap<Class<?>, Class<?>>();
    static {
        BOXED_TO_PRIMITIVE.put(Boolean.class, boolean.class);
        BOXED_TO_PRIMITIVE.put(Byte.class, byte.class);
        BOXED_TO_PRIMITIVE.put(Character.class, char.class);
        BOXED_TO_PRIMITIVE.put(Double.class, double.class);
        BOXED_TO_PRIMITIVE.put(Float.class, float.class);
        BOXED_TO_PRIMITIVE.put(Integer.class, int.class);
        BOXED_TO_PRIMITIVE.put(Long.class, long.class);
        BOXED_TO_PRIMITIVE.put(Short.class, short.class);
        BOXED_TO_PRIMITIVE.put(Void.class, void.class);
    }

    private DexGenerator generator;
    private Type<Integer> intType;
    private Type<Long> longType;
    private Type<Boolean> booleanType;
    private Type<Object> objectType;
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
        intType = generator.getType(int.class);
        longType = generator.getType(long.class);
        booleanType = generator.getType(boolean.class);
        objectType = generator.getType(Object.class);
        dexGeneratorTestType = generator.getType(DexGeneratorTest.class);
        generatedType = generator.getType("LGenerated;");
        callableType = generator.getType(Callable.class);
        call = callableType.getMethod(objectType, "call");
    }

    public void testNewInstance() throws Exception {
        /*
         * public static Constructable generatedMethod(long a, boolean b) {
         *   Constructable result = new Constructable(a, b);
         *   return result;
         * }
         */

        Code code = generator.newCode();
        Local<Long> localA = code.newParameter(longType);
        Local<Boolean> localB = code.newParameter(booleanType);
        Type<Constructable> constructable = generator.getType(Constructable.class);
        Method<Constructable, Void> constructor
                = constructable.getConstructor(longType, booleanType);
        Local<Constructable> localResult = code.newLocal(constructable);
        code.newInstance(localResult, constructor, localA, localB);
        code.returnValue(localResult);

        java.lang.reflect.Method method
                = newMethod(Constructable.class, code, long.class, boolean.class);
        Constructable constructed = (Constructable) method.invoke(null, 5L, false);
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
         * public static int generatedMethod(int a) {
         *   int result = DexGeneratorTest.staticMethod(a);
         *   return result;
         * }
         */

        Code code = generator.newCode();
        Local<Integer> localA = code.newParameter(intType);
        Local<Integer> localResult = code.newLocal(intType);
        Method<DexGeneratorTest, Integer> staticMethod
                = dexGeneratorTestType.getMethod(intType, "staticMethod", intType);
        code.invokeStatic(staticMethod, localResult, localA);
        code.returnValue(localResult);

        java.lang.reflect.Method method = newMethod(int.class, code, int.class);
        assertEquals(10, method.invoke(null, 4));
    }

    @SuppressWarnings("unused") // called by generated code
    public static int staticMethod(int a) {
        return a + 6;
    }

    public void testInvokeVirtual() throws Exception {
        /*
         * public static int generatedMethod(DexGeneratorTest test, int a) {
         *   int result = test.virtualMethod(a);
         *   return result;
         * }
         */
        Code code = generator.newCode();
        Local<DexGeneratorTest> localInstance = code.newParameter(dexGeneratorTestType);
        Local<Integer> localA = code.newParameter(intType);
        Local<Integer> localResult = code.newLocal(intType);
        Method<DexGeneratorTest, Integer> virtualMethod
                = dexGeneratorTestType.getMethod(intType, "virtualMethod", intType);
        code.invokeVirtual(virtualMethod, localResult, localInstance, localA);
        code.returnValue(localResult);

        java.lang.reflect.Method method = newMethod(
                int.class, code, DexGeneratorTest.class, int.class);
        assertEquals(9, method.invoke(null, this, 4));
    }

    @SuppressWarnings("unused") // called by generated code
    public int virtualMethod(int a) {
        return a + 5;
    }

    public void testInvokeDirect() throws Exception {
        /*
         * private int directMethod() {
         *   int a = 5;
         *   return a;
         * }
         *
         * public static int generatedMethod(Generated g) {
         *   int b = g.directMethod();
         *   return b;
         * }
         */
        Method<?, Integer> directMethod = generatedType.getMethod(intType, "directMethod");
        Code directCode = generator.newCode();
        directCode.newThisLocal(generatedType); // 'this' is unused
        Local<Integer> localA = directCode.newLocal(intType);
        directCode.loadConstant(localA, 5);
        directCode.returnValue(localA);
        directMethod.declare(ACC_PRIVATE, directCode);

        Method<?, ?> method = generatedType.getMethod(intType, "generatedMethod", generatedType);
        Code code = generator.newCode();
        Local<Integer> localB = code.newLocal(intType);
        Local<?> localG = code.newParameter(generatedType);
        code.invokeDirect(directMethod, localB, localG);
        code.returnValue(localB);
        method.declare(ACC_PUBLIC | ACC_STATIC, code);

        addDefaultConstructor();

        generatedType.declare("Generated.java", ACC_PUBLIC, objectType);
        Class<?> generatedClass = loadAndGenerate();
        Object instance = generatedClass.newInstance();
        java.lang.reflect.Method m = generatedClass.getMethod("generatedMethod", generatedClass);
        assertEquals(5, m.invoke(null, instance));
    }

    public void testInvokeSuper() throws Exception {
        /*
         * public int superHashCode() {
         *   int result = super.hashCode();
         *   return result;
         * }
         * public int hashCode() {
         *   return 0;
         * }
         */
        Method<?, Integer> objectHashCode = objectType.getMethod(intType, "hashCode");
        Code superHashCode = generator.newCode();
        Local<Integer> localResult = superHashCode.newLocal(intType);
        Local<?> localThis = superHashCode.newThisLocal(generatedType);
        superHashCode.invokeSuper(objectHashCode, localResult, localThis);
        superHashCode.returnValue(localResult);
        generatedType.getMethod(intType, "superHashCode").declare(ACC_PUBLIC, superHashCode);

        Code generatedHashCode = generator.newCode();
        generatedHashCode.newThisLocal(generatedType);
        Local<Integer> localZero = generatedHashCode.newLocal(intType);
        generatedHashCode.loadConstant(localZero, 0);
        generatedHashCode.returnValue(localZero);
        generatedType.getMethod(intType, "hashCode").declare(ACC_PUBLIC, generatedHashCode);

        addDefaultConstructor();

        generatedType.declare("Generated.java", ACC_PUBLIC, objectType);
        Class<?> generatedClass = loadAndGenerate();
        Object instance = generatedClass.newInstance();
        java.lang.reflect.Method m = generatedClass.getMethod("superHashCode");
        assertEquals(System.identityHashCode(instance), m.invoke(instance));
    }

    @SuppressWarnings("unused") // called by generated code
    public final int superMethod(int a) {
        return a + 4;
    }

    public void testInvokeInterface() throws Exception {
        /*
         * public static Object generatedMethod(Callable c) {
         *   Object result = c.call();
         *   return result;
         * }
         */
        Code code = generator.newCode();
        Local<Callable> localC = code.newParameter(callableType);
        Local<Object> localResult = code.newLocal(objectType);
        code.invokeInterface(call, localResult, localC);
        code.returnValue(localResult);

        Callable<Object> callable = new Callable<Object>() {
            public Object call() throws Exception {
                return "abc";
            }
        };
        java.lang.reflect.Method method = newMethod(Object.class, code, Callable.class);
        assertEquals("abc", method.invoke(null, callable));
    }

    public void testParameterMismatch() throws Exception {
        Code code = generator.newCode();
        code.newParameter(intType);
        code.newParameter(objectType);

        Type<?>[] argTypes = {
                generator.getType(Integer.class), // should fail because the code specifies int
                objectType,
        };

        Method<?, Integer> method = generatedType.getMethod(intType, "generatedMethod", argTypes);
        try {
            method.declare(ACC_PUBLIC | ACC_STATIC, code);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testReturnTypeMismatch() {
        fail("TODO");
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
        generatedType.declare("Generated.java", ACC_PUBLIC, objectType);

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
        generatedType.declare("Generated.java", ACC_PUBLIC, objectType);
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
    public void testDeclareConstructor() throws Exception {
        /*
         * class Generated {
         *   public final int a;
         *   public Generated(int a) {
         *     this.a = a;
         *   }
         * }
         */
        Field<?, Integer> field = generatedType.getField(intType, "a");
        field.declare(ACC_PUBLIC | ACC_FINAL, null);

        Code constructor = generator.newCode();
        Local<?> thisRef = constructor.newThisLocal(generatedType);
        Local<Integer> parameter = constructor.newParameter(intType);
        constructor.invokeDirect(objectType.getConstructor(), null, thisRef);
        constructor.iput(field, (Local) thisRef, parameter); // TODO: type safety hurts us here
        constructor.returnVoid();
        generatedType.getConstructor(intType).declare(ACC_PUBLIC | ACC_CONSTRUCTOR, constructor);
        generatedType.declare("Generated.java", ACC_PUBLIC, objectType);

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
         * public int generatedMethod() {
         *   int a = 5;
         *   return a;
         * }
         */
        reset();
        Type<T> returnType = generator.getType(javaType);
        Code code = generator.newCode();
        if (value != null) {
            Local<T> i = code.newLocal(returnType);
            code.loadConstant(i, value);
            code.returnValue(i);
        } else {
            code.returnVoid();
        }
        generatedType.getMethod(returnType, "generatedMethod")
                .declare(ACC_PUBLIC | ACC_STATIC, code);
        generatedType.declare("Generated.java", ACC_PUBLIC, objectType);

        Class<?> generatedClass = loadAndGenerate();
        java.lang.reflect.Method method = generatedClass.getMethod("generatedMethod");
        assertEquals(javaType, method.getReturnType());
        assertEquals(value, method.invoke(null));
    }

    public void testBranching() throws Exception {
        java.lang.reflect.Method lt = newBranchingMethod(Comparison.LT);
        assertEquals(Boolean.TRUE, lt.invoke(null, 1, 2));
        assertEquals(Boolean.FALSE, lt.invoke(null, 1, 1));
        assertEquals(Boolean.FALSE, lt.invoke(null, 2, 1));

        java.lang.reflect.Method le = newBranchingMethod(Comparison.LE);
        assertEquals(Boolean.TRUE, le.invoke(null, 1, 2));
        assertEquals(Boolean.TRUE, le.invoke(null, 1, 1));
        assertEquals(Boolean.FALSE, le.invoke(null, 2, 1));

        java.lang.reflect.Method eq = newBranchingMethod(Comparison.EQ);
        assertEquals(Boolean.FALSE, eq.invoke(null, 1, 2));
        assertEquals(Boolean.TRUE, eq.invoke(null, 1, 1));
        assertEquals(Boolean.FALSE, eq.invoke(null, 2, 1));

        java.lang.reflect.Method ge = newBranchingMethod(Comparison.GE);
        assertEquals(Boolean.FALSE, ge.invoke(null, 1, 2));
        assertEquals(Boolean.TRUE, ge.invoke(null, 1, 1));
        assertEquals(Boolean.TRUE, ge.invoke(null, 2, 1));

        java.lang.reflect.Method gt = newBranchingMethod(Comparison.GT);
        assertEquals(Boolean.FALSE, gt.invoke(null, 1, 2));
        assertEquals(Boolean.FALSE, gt.invoke(null, 1, 1));
        assertEquals(Boolean.TRUE, gt.invoke(null, 2, 1));

        java.lang.reflect.Method ne = newBranchingMethod(Comparison.NE);
        assertEquals(Boolean.TRUE, ne.invoke(null, 1, 2));
        assertEquals(Boolean.FALSE, ne.invoke(null, 1, 1));
        assertEquals(Boolean.TRUE, ne.invoke(null, 2, 1));
    }

    private java.lang.reflect.Method newBranchingMethod(Comparison comparison) throws Exception {
        /*
         * public static int generatedMethod(int localA, int localB) {
         *   if (a comparison b) {
         *     return true;
         *   }
         *   return false;
         * }
         */
        reset();
        Code code = generator.newCode();
        Local<Integer> localA = code.newParameter(intType);
        Local<Integer> localB = code.newParameter(intType);
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
        return newMethod(boolean.class, code, int.class, int.class);
    }

    public void testCastIntegerToInteger() throws Exception {
        java.lang.reflect.Method intToLong = newCastingMethod(int.class, long.class);
        assertEquals(0x0000000000000000L, intToLong.invoke(null, 0x00000000));
        assertEquals(0x000000007fffffffL, intToLong.invoke(null, 0x7fffffff));
        assertEquals(0xffffffff80000000L, intToLong.invoke(null, 0x80000000));
        assertEquals(0xffffffffffffffffL, intToLong.invoke(null, 0xffffffff));

        java.lang.reflect.Method longToInt = newCastingMethod(long.class, int.class);
        assertEquals(0x1234abcd, longToInt.invoke(null, 0x000000001234abcdL));
        assertEquals(0x1234abcd, longToInt.invoke(null, 0x123456781234abcdL));
        assertEquals(0x1234abcd, longToInt.invoke(null, 0xffffffff1234abcdL));

        java.lang.reflect.Method intToShort = newCastingMethod(int.class, short.class);
        assertEquals((short) 0x1234, intToShort.invoke(null, 0x00001234));
        assertEquals((short) 0x1234, intToShort.invoke(null, 0xabcd1234));
        assertEquals((short) 0x1234, intToShort.invoke(null, 0xffff1234));

        java.lang.reflect.Method intToChar = newCastingMethod(int.class, char.class);
        assertEquals((char) 0x1234, intToChar.invoke(null, 0x00001234));
        assertEquals((char) 0x1234, intToChar.invoke(null, 0xabcd1234));
        assertEquals((char) 0x1234, intToChar.invoke(null, 0xffff1234));

        java.lang.reflect.Method intToByte = newCastingMethod(int.class, byte.class);
        assertEquals((byte) 0x34, intToByte.invoke(null, 0x00000034));
        assertEquals((byte) 0x34, intToByte.invoke(null, 0xabcd1234));
        assertEquals((byte) 0x34, intToByte.invoke(null, 0xffffff34));
    }

    public void testCastIntegerToFloatingPoint() throws Exception {
        java.lang.reflect.Method intToFloat = newCastingMethod(int.class, float.class);
        assertEquals(0.0f, intToFloat.invoke(null, 0));
        assertEquals(-1.0f, intToFloat.invoke(null, -1));
        assertEquals(16777216f, intToFloat.invoke(null, 16777216));
        assertEquals(16777216f, intToFloat.invoke(null, 16777217)); // precision

        java.lang.reflect.Method intToDouble = newCastingMethod(int.class, double.class);
        assertEquals(0.0, intToDouble.invoke(null, 0));
        assertEquals(-1.0, intToDouble.invoke(null, -1));
        assertEquals(16777216.0, intToDouble.invoke(null, 16777216));
        assertEquals(16777217.0, intToDouble.invoke(null, 16777217));

        java.lang.reflect.Method longToFloat = newCastingMethod(long.class, float.class);
        assertEquals(0.0f, longToFloat.invoke(null, 0L));
        assertEquals(-1.0f, longToFloat.invoke(null, -1L));
        assertEquals(16777216f, longToFloat.invoke(null, 16777216L));
        assertEquals(16777216f, longToFloat.invoke(null, 16777217L));

        java.lang.reflect.Method longToDouble = newCastingMethod(long.class, double.class);
        assertEquals(0.0, longToDouble.invoke(null, 0L));
        assertEquals(-1.0, longToDouble.invoke(null, -1L));
        assertEquals(9007199254740992.0, longToDouble.invoke(null, 9007199254740992L));
        assertEquals(9007199254740992.0, longToDouble.invoke(null, 9007199254740993L)); // precision
    }

    public void testCastFloatingPointToInteger() throws Exception {
        java.lang.reflect.Method floatToInt = newCastingMethod(float.class, int.class);
        assertEquals(0, floatToInt.invoke(null, 0.0f));
        assertEquals(-1, floatToInt.invoke(null, -1.0f));
        assertEquals(Integer.MAX_VALUE, floatToInt.invoke(null, 10e15f));
        assertEquals(0, floatToInt.invoke(null, 0.5f));
        assertEquals(Integer.MIN_VALUE, floatToInt.invoke(null, Float.NEGATIVE_INFINITY));
        assertEquals(0, floatToInt.invoke(null, Float.NaN));

        java.lang.reflect.Method floatToLong = newCastingMethod(float.class, long.class);
        assertEquals(0L, floatToLong.invoke(null, 0.0f));
        assertEquals(-1L, floatToLong.invoke(null, -1.0f));
        assertEquals(10000000272564224L, floatToLong.invoke(null, 10e15f));
        assertEquals(0L, floatToLong.invoke(null, 0.5f));
        assertEquals(Long.MIN_VALUE, floatToLong.invoke(null, Float.NEGATIVE_INFINITY));
        assertEquals(0L, floatToLong.invoke(null, Float.NaN));

        java.lang.reflect.Method doubleToInt = newCastingMethod(double.class, int.class);
        assertEquals(0, doubleToInt.invoke(null, 0.0));
        assertEquals(-1, doubleToInt.invoke(null, -1.0));
        assertEquals(Integer.MAX_VALUE, doubleToInt.invoke(null, 10e15));
        assertEquals(0, doubleToInt.invoke(null, 0.5));
        assertEquals(Integer.MIN_VALUE, doubleToInt.invoke(null, Double.NEGATIVE_INFINITY));
        assertEquals(0, doubleToInt.invoke(null, Double.NaN));

        java.lang.reflect.Method doubleToLong = newCastingMethod(double.class, long.class);
        assertEquals(0L, doubleToLong.invoke(null, 0.0));
        assertEquals(-1L, doubleToLong.invoke(null, -1.0));
        assertEquals(10000000000000000L, doubleToLong.invoke(null, 10e15));
        assertEquals(0L, doubleToLong.invoke(null, 0.5));
        assertEquals(Long.MIN_VALUE, doubleToLong.invoke(null, Double.NEGATIVE_INFINITY));
        assertEquals(0L, doubleToLong.invoke(null, Double.NaN));
    }

    public void testCastFloatingPointToFloatingPoint() throws Exception {
        java.lang.reflect.Method floatToDouble = newCastingMethod(float.class, double.class);
        assertEquals(0.0, floatToDouble.invoke(null, 0.0f));
        assertEquals(-1.0, floatToDouble.invoke(null, -1.0f));
        assertEquals(0.5, floatToDouble.invoke(null, 0.5f));
        assertEquals(Double.NEGATIVE_INFINITY, floatToDouble.invoke(null, Float.NEGATIVE_INFINITY));
        assertEquals(Double.NaN, floatToDouble.invoke(null, Float.NaN));

        java.lang.reflect.Method doubleToFloat = newCastingMethod(double.class, float.class);
        assertEquals(0.0f, doubleToFloat.invoke(null, 0.0));
        assertEquals(-1.0f, doubleToFloat.invoke(null, -1.0));
        assertEquals(0.5f, doubleToFloat.invoke(null, 0.5));
        assertEquals(Float.NEGATIVE_INFINITY, doubleToFloat.invoke(null, Double.NEGATIVE_INFINITY));
        assertEquals(Float.NaN, doubleToFloat.invoke(null, Double.NaN));
    }

    private java.lang.reflect.Method newCastingMethod(Class<?> source, Class<?> target)
            throws Exception {
        /*
         * public static short generatedMethod(int source) {
         *   short casted = (short) source;
         *   return casted;
         * }
         */
        reset();
        Type<?> sourceType = generator.getType(source);
        Type<?> targetType = generator.getType(target);
        Code code = generator.newCode();
        Local<?> localSource = code.newParameter(sourceType);
        Local<?> localCasted = code.newLocal(targetType);
        code.cast(localSource, localCasted);
        code.returnValue(localCasted);
        return newMethod(target, code, source);
    }

    public void testNot() throws Exception {
        java.lang.reflect.Method notInteger = newNotMethod(int.class);
        assertEquals(0xffffffff, notInteger.invoke(null, 0x00000000));
        assertEquals(0x00000000, notInteger.invoke(null, 0xffffffff));
        assertEquals(0xedcba987, notInteger.invoke(null, 0x12345678));

        java.lang.reflect.Method notLong = newNotMethod(long.class);
        assertEquals(0xffffffffffffffffL, notLong.invoke(null, 0x0000000000000000L));
        assertEquals(0x0000000000000000L, notLong.invoke(null, 0xffffffffffffffffL));
        assertEquals(0x98765432edcba987L, notLong.invoke(null, 0x6789abcd12345678L));
    }

    private java.lang.reflect.Method newNotMethod(Class<?> source) throws Exception {
        /*
         * public static short generatedMethod(int source) {
         *   source = ~source;
         *   return not;
         * }
         */
        reset();
        Type<?> sourceType = generator.getType(source);
        Code code = generator.newCode();
        Local<?> localSource = code.newParameter(sourceType);
        code.not((Local) localSource, localSource); // TODO: type safety
        code.returnValue(localSource);
        return newMethod(source, code, source);
    }

    public void testNegate() throws Exception {
        java.lang.reflect.Method negateInteger = newNegateMethod(int.class);
        assertEquals(0, negateInteger.invoke(null, 0));
        assertEquals(-1, negateInteger.invoke(null, 1));
        assertEquals(Integer.MIN_VALUE, negateInteger.invoke(null, Integer.MIN_VALUE));

        java.lang.reflect.Method negateLong = newNegateMethod(long.class);
        assertEquals(0L, negateLong.invoke(null, 0));
        assertEquals(-1L, negateLong.invoke(null, 1));
        assertEquals(Long.MIN_VALUE, negateLong.invoke(null, Long.MIN_VALUE));

        java.lang.reflect.Method negateFloat = newNegateMethod(float.class);
        assertEquals(-0.0f, negateFloat.invoke(null, 0.0f));
        assertEquals(-1.0f, negateFloat.invoke(null, 1.0f));
        assertEquals(Float.NaN, negateFloat.invoke(null, Float.NaN));
        assertEquals(Float.POSITIVE_INFINITY, negateFloat.invoke(null, Float.NEGATIVE_INFINITY));

        java.lang.reflect.Method negateDouble = newNegateMethod(double.class);
        assertEquals(-0.0, negateDouble.invoke(null, 0.0));
        assertEquals(-1.0, negateDouble.invoke(null, 1.0));
        assertEquals(Double.NaN, negateDouble.invoke(null, Double.NaN));
        assertEquals(Double.POSITIVE_INFINITY, negateDouble.invoke(null, Double.NEGATIVE_INFINITY));
    }

    private java.lang.reflect.Method newNegateMethod(Class<?> source) throws Exception {
        /*
         * public static short generatedMethod(int source) {
         *   source = -source;
         *   return not;
         * }
         */
        reset();
        Type<?> sourceType = generator.getType(source);
        Code code = generator.newCode();
        Local<?> localSource = code.newParameter(sourceType);
        code.negate((Local) localSource, localSource); // TODO: type safety
        code.returnValue(localSource);
        return newMethod(source, code, source);
    }

    public void testIntBinaryOps() throws Exception {
        java.lang.reflect.Method add = newBinaryOpMethod(int.class, BinaryOp.ADD);
        assertEquals(79, add.invoke(null, 75, 4));

        java.lang.reflect.Method subtract = newBinaryOpMethod(int.class, BinaryOp.SUBTRACT);
        assertEquals(71, subtract.invoke(null, 75, 4));

        java.lang.reflect.Method multiply = newBinaryOpMethod(int.class, BinaryOp.MULTIPLY);
        assertEquals(300, multiply.invoke(null, 75, 4));

        java.lang.reflect.Method divide = newBinaryOpMethod(int.class, BinaryOp.DIVIDE);
        assertEquals(18, divide.invoke(null, 75, 4));
        try {
            divide.invoke(null, 75, 0);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(ArithmeticException.class, expected.getCause().getClass());
        }

        java.lang.reflect.Method remainder = newBinaryOpMethod(int.class, BinaryOp.REMAINDER);
        assertEquals(3, remainder.invoke(null, 75, 4));
        try {
            remainder.invoke(null, 75, 0);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(ArithmeticException.class, expected.getCause().getClass());
        }

        java.lang.reflect.Method and = newBinaryOpMethod(int.class, BinaryOp.AND);
        assertEquals(0xff000000, and.invoke(null, 0xff00ff00, 0xffff0000));

        java.lang.reflect.Method or = newBinaryOpMethod(int.class, BinaryOp.OR);
        assertEquals(0xffffff00, or.invoke(null, 0xff00ff00, 0xffff0000));

        java.lang.reflect.Method xor = newBinaryOpMethod(int.class, BinaryOp.XOR);
        assertEquals(0x00ffff00, xor.invoke(null, 0xff00ff00, 0xffff0000));

        java.lang.reflect.Method shiftLeft = newBinaryOpMethod(int.class, BinaryOp.SHIFT_LEFT);
        assertEquals(0xcd123400, shiftLeft.invoke(null, 0xabcd1234, 8));

        java.lang.reflect.Method shiftRight = newBinaryOpMethod(int.class, BinaryOp.SHIFT_RIGHT);
        assertEquals(0xffabcd12, shiftRight.invoke(null, 0xabcd1234, 8));

        java.lang.reflect.Method unsignedShiftRight = newBinaryOpMethod(int.class,
                BinaryOp.UNSIGNED_SHIFT_RIGHT);
        assertEquals(0x00abcd12, unsignedShiftRight.invoke(null, 0xabcd1234, 8));
    }

    public void testLongBinaryOps() throws Exception {
        java.lang.reflect.Method add = newBinaryOpMethod(long.class, BinaryOp.ADD);
        assertEquals(79L, add.invoke(null, 75L, 4L));

        java.lang.reflect.Method subtract = newBinaryOpMethod(long.class, BinaryOp.SUBTRACT);
        assertEquals(71L, subtract.invoke(null, 75L, 4L));

        java.lang.reflect.Method multiply = newBinaryOpMethod(long.class, BinaryOp.MULTIPLY);
        assertEquals(300L, multiply.invoke(null, 75L, 4L));

        java.lang.reflect.Method divide = newBinaryOpMethod(long.class, BinaryOp.DIVIDE);
        assertEquals(18L, divide.invoke(null, 75L, 4L));
        try {
            divide.invoke(null, 75L, 0L);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(ArithmeticException.class, expected.getCause().getClass());
        }

        java.lang.reflect.Method remainder = newBinaryOpMethod(long.class, BinaryOp.REMAINDER);
        assertEquals(3L, remainder.invoke(null, 75L, 4L));
        try {
            remainder.invoke(null, 75L, 0L);
            fail();
        } catch (InvocationTargetException expected) {
            assertEquals(ArithmeticException.class, expected.getCause().getClass());
        }

        java.lang.reflect.Method and = newBinaryOpMethod(long.class, BinaryOp.AND);
        assertEquals(0xff00ff0000000000L,
                and.invoke(null, 0xff00ff00ff00ff00L, 0xffffffff00000000L));

        java.lang.reflect.Method or = newBinaryOpMethod(long.class, BinaryOp.OR);
        assertEquals(0xffffffffff00ff00L, or.invoke(null, 0xff00ff00ff00ff00L, 0xffffffff00000000L));

        java.lang.reflect.Method xor = newBinaryOpMethod(long.class, BinaryOp.XOR);
        assertEquals(0x00ff00ffff00ff00L,
                xor.invoke(null, 0xff00ff00ff00ff00L, 0xffffffff00000000L));

        java.lang.reflect.Method shiftLeft = newBinaryOpMethod(long.class, BinaryOp.SHIFT_LEFT);
        assertEquals(0xcdef012345678900L, shiftLeft.invoke(null, 0xabcdef0123456789L, 8L));

        java.lang.reflect.Method shiftRight = newBinaryOpMethod(long.class, BinaryOp.SHIFT_RIGHT);
        assertEquals(0xffabcdef01234567L, shiftRight.invoke(null, 0xabcdef0123456789L, 8L));

        java.lang.reflect.Method unsignedShiftRight = newBinaryOpMethod(long.class,
                BinaryOp.UNSIGNED_SHIFT_RIGHT);
        assertEquals(0x00abcdef01234567L, unsignedShiftRight.invoke(null, 0xabcdef0123456789L, 8L));
    }

    public void testFloatBinaryOps() throws Exception {
        java.lang.reflect.Method add = newBinaryOpMethod(float.class, BinaryOp.ADD);
        assertEquals(6.75f, add.invoke(null, 5.5f, 1.25f));

        java.lang.reflect.Method subtract = newBinaryOpMethod(float.class, BinaryOp.SUBTRACT);
        assertEquals(4.25f, subtract.invoke(null, 5.5f, 1.25f));

        java.lang.reflect.Method multiply = newBinaryOpMethod(float.class, BinaryOp.MULTIPLY);
        assertEquals(6.875f, multiply.invoke(null, 5.5f, 1.25f));

        java.lang.reflect.Method divide = newBinaryOpMethod(float.class, BinaryOp.DIVIDE);
        assertEquals(4.4f, divide.invoke(null, 5.5f, 1.25f));
        assertEquals(Float.POSITIVE_INFINITY, divide.invoke(null, 5.5f, 0.0f));

        java.lang.reflect.Method remainder = newBinaryOpMethod(float.class, BinaryOp.REMAINDER);
        assertEquals(0.5f, remainder.invoke(null, 5.5f, 1.25f));
        assertEquals(Float.NaN, remainder.invoke(null, 5.5f, 0.0f));
    }

    public void testDoubleBinaryOps() throws Exception {
        java.lang.reflect.Method add = newBinaryOpMethod(double.class, BinaryOp.ADD);
        assertEquals(6.75, add.invoke(null, 5.5, 1.25));

        java.lang.reflect.Method subtract = newBinaryOpMethod(double.class, BinaryOp.SUBTRACT);
        assertEquals(4.25, subtract.invoke(null, 5.5, 1.25));

        java.lang.reflect.Method multiply = newBinaryOpMethod(double.class, BinaryOp.MULTIPLY);
        assertEquals(6.875, multiply.invoke(null, 5.5, 1.25));

        java.lang.reflect.Method divide = newBinaryOpMethod(double.class, BinaryOp.DIVIDE);
        assertEquals(4.4, divide.invoke(null, 5.5, 1.25));
        assertEquals(Double.POSITIVE_INFINITY, divide.invoke(null, 5.5, 0.0));

        java.lang.reflect.Method remainder = newBinaryOpMethod(double.class, BinaryOp.REMAINDER);
        assertEquals(0.5, remainder.invoke(null, 5.5, 1.25));
        assertEquals(Double.NaN, remainder.invoke(null, 5.5, 0.0));
    }

    private java.lang.reflect.Method newBinaryOpMethod(Class<?> valueClass, BinaryOp op)
            throws Exception {
        /*
         * public static int binaryOp(int a, int b) {
         *   int result = a + b;
         *   return result;
         * }
         */
        reset();
        Type<?> valueType = generator.getType(valueClass);
        Code code = generator.newCode();
        Local<?> localA = code.newParameter(valueType);
        Local<?> localB = code.newParameter(valueType);
        Local<?> localResult = code.newLocal(valueType);
        code.op(op, (Local) localResult, (Local) localA, (Local) localB); // TODO: type safety
        code.returnValue(localResult);
        return newMethod(valueClass, code, valueClass, valueClass);
    }

    public void testReadAndWriteInstanceFields() throws Exception {
        Instance instance = new Instance();

        java.lang.reflect.Method intSwap = newInstanceSwapMethod(int.class, "intValue");
        instance.intValue = 5;
        assertEquals(5, intSwap.invoke(null, instance, 10));
        assertEquals(10, instance.intValue);

        java.lang.reflect.Method longSwap = newInstanceSwapMethod(long.class, "longValue");
        instance.longValue = 500L;
        assertEquals(500L, longSwap.invoke(null, instance, 1234L));
        assertEquals(1234L, instance.longValue);

        java.lang.reflect.Method booleanSwap = newInstanceSwapMethod(boolean.class, "booleanValue");
        instance.booleanValue = false;
        assertEquals(false, booleanSwap.invoke(null, instance, true));
        assertEquals(true, instance.booleanValue);

        java.lang.reflect.Method floatSwap = newInstanceSwapMethod(float.class, "floatValue");
        instance.floatValue = 1.5f;
        assertEquals(1.5f, floatSwap.invoke(null, instance, 0.5f));
        assertEquals(0.5f, instance.floatValue);

        java.lang.reflect.Method doubleSwap = newInstanceSwapMethod(double.class, "doubleValue");
        instance.doubleValue = 155.5;
        assertEquals(155.5, doubleSwap.invoke(null, instance, 266.6));
        assertEquals(266.6, instance.doubleValue);

        java.lang.reflect.Method objectSwap = newInstanceSwapMethod(Object.class, "objectValue");
        instance.objectValue = "before";
        assertEquals("before", objectSwap.invoke(null, instance, "after"));
        assertEquals("after", instance.objectValue);

        java.lang.reflect.Method byteSwap = newInstanceSwapMethod(byte.class, "byteValue");
        instance.byteValue = 0x35;
        assertEquals((byte) 0x35, byteSwap.invoke(null, instance, (byte) 0x64));
        assertEquals((byte) 0x64, instance.byteValue);

        java.lang.reflect.Method charSwap = newInstanceSwapMethod(char.class, "charValue");
        instance.charValue = 'A';
        assertEquals('A', charSwap.invoke(null, instance, 'B'));
        assertEquals('B', instance.charValue);

        java.lang.reflect.Method shortSwap = newInstanceSwapMethod(short.class, "shortValue");
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

    private <V> java.lang.reflect.Method newInstanceSwapMethod(
            Class<V> valueClass, String fieldName) throws Exception {
        /*
         * public static int generatedMethod(Instance instance, int newValue) {
         *   int oldValue = instance.intValue;
         *   instance.intValue = newValue;
         *   return oldValue;
         * }
         */
        reset();
        Type<V> valueType = generator.getType(valueClass);
        Type<Instance> objectType = generator.getType(Instance.class);
        Field<Instance, V> field = objectType.getField(valueType, fieldName);
        Code code = generator.newCode();
        Local<Instance> localInstance = code.newParameter(objectType);
        Local<V> localNewValue = code.newParameter(valueType);
        Local<V> localOldValue = code.newLocal(valueType);
        code.iget(field, localInstance, localOldValue);
        code.iput(field, localInstance, localNewValue);
        code.returnValue(localOldValue);
        return newMethod(valueClass, code, Instance.class, valueClass);
    }

    public void testReadAndWriteStaticFields() throws Exception {
        java.lang.reflect.Method intSwap = newStaticSwapMethod(int.class, "intValue");
        Static.intValue = 5;
        assertEquals(5, intSwap.invoke(null, 10));
        assertEquals(10, Static.intValue);

        java.lang.reflect.Method longSwap = newStaticSwapMethod(long.class, "longValue");
        Static.longValue = 500L;
        assertEquals(500L, longSwap.invoke(null, 1234L));
        assertEquals(1234L, Static.longValue);

        java.lang.reflect.Method booleanSwap = newStaticSwapMethod(boolean.class, "booleanValue");
        Static.booleanValue = false;
        assertEquals(false, booleanSwap.invoke(null, true));
        assertEquals(true, Static.booleanValue);

        java.lang.reflect.Method floatSwap = newStaticSwapMethod(float.class, "floatValue");
        Static.floatValue = 1.5f;
        assertEquals(1.5f, floatSwap.invoke(null, 0.5f));
        assertEquals(0.5f, Static.floatValue);

        java.lang.reflect.Method doubleSwap = newStaticSwapMethod(double.class, "doubleValue");
        Static.doubleValue = 155.5;
        assertEquals(155.5, doubleSwap.invoke(null, 266.6));
        assertEquals(266.6, Static.doubleValue);

        java.lang.reflect.Method objectSwap = newStaticSwapMethod(Object.class, "objectValue");
        Static.objectValue = "before";
        assertEquals("before", objectSwap.invoke(null, "after"));
        assertEquals("after", Static.objectValue);

        java.lang.reflect.Method byteSwap = newStaticSwapMethod(byte.class, "byteValue");
        Static.byteValue = 0x35;
        assertEquals((byte) 0x35, byteSwap.invoke(null, (byte) 0x64));
        assertEquals((byte) 0x64, Static.byteValue);

        java.lang.reflect.Method charSwap = newStaticSwapMethod(char.class, "charValue");
        Static.charValue = 'A';
        assertEquals('A', charSwap.invoke(null, 'B'));
        assertEquals('B', Static.charValue);

        java.lang.reflect.Method shortSwap = newStaticSwapMethod(short.class, "shortValue");
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

    private <V> java.lang.reflect.Method newStaticSwapMethod(Class<V> valueClass, String fieldName)
            throws Exception {
        /*
         * public static int generatedMethod(int newValue) {
         *   int oldValue = Static.intValue;
         *   Static.intValue = newValue;
         *   return oldValue;
         * }
         */
        reset();
        Type<V> valueType = generator.getType(valueClass);
        Type<Static> objectType = generator.getType(Static.class);
        Field<Static, V> field = objectType.getField(valueType, fieldName);
        Code code = generator.newCode();
        Local<V> localNewValue = code.newParameter(valueType);
        Local<V> localOldValue = code.newLocal(valueType);
        code.sget(field, localOldValue);
        code.sput(field, localNewValue);
        code.returnValue(localOldValue);
        return newMethod(valueClass, code, valueClass);
    }

    /**
     * Tests that we can construct a for loop.
     */
    public void testForLoop() throws Exception {
        /*
         * public static int generatedMethod(int count) {
         *   int result = 1;
         *   for (int i = 0; i < count; i += 1) {
         *     result = result * 2;
         *   }
         *   return result;
         * }
         */
        Code code = generator.newCode();
        Local<Integer> localCount = code.newParameter(intType);
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

        java.lang.reflect.Method pow2 = newMethod(int.class, code, int.class);
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
         * public static int generatedMethod(int max) {
         *   int result = 1;
         *   while (result < max) {
         *     result = result * 2;
         *   }
         *   return result;
         * }
         */
        Code code = generator.newCode();
        Local<Integer> localMax = code.newParameter(intType);
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

        java.lang.reflect.Method ceilPow2 = newMethod(int.class, code, int.class);
        assertEquals(1, ceilPow2.invoke(null, 1));
        assertEquals(2, ceilPow2.invoke(null, 2));
        assertEquals(4, ceilPow2.invoke(null, 3));
        assertEquals(16, ceilPow2.invoke(null, 10));
        assertEquals(128, ceilPow2.invoke(null, 100));
        assertEquals(1024, ceilPow2.invoke(null, 1000));
    }

    public void testIfElseBlock() throws Exception {
        /*
         * public static int generatedMethod(int a, int b, int c) {
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
        Code code = generator.newCode();
        Local<Integer> localA = code.newParameter(intType);
        Local<Integer> localB = code.newParameter(intType);
        Local<Integer> localC = code.newParameter(intType);
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

        java.lang.reflect.Method min = newMethod(int.class, code, int.class, int.class, int.class);
        assertEquals(1, min.invoke(null, 1, 2, 3));
        assertEquals(1, min.invoke(null, 2, 3, 1));
        assertEquals(1, min.invoke(null, 2, 1, 3));
        assertEquals(1, min.invoke(null, 3, 2, 1));
    }

    // TODO: cast

    // TODO: instanceof

    // TODO: array ops including new array, aget, etc.

    // TODO: throw + catch

    // TODO: cmp float

    // TODO: fail if a label is unreachable (never navigated to)

    private void addDefaultConstructor() {
        Code code = generator.newCode();
        Local<?> thisRef = code.newThisLocal(generatedType);
        code.invokeDirect(objectType.getConstructor(), null, thisRef);
        code.returnVoid();
        generatedType.getConstructor().declare(ACC_PUBLIC | ACC_CONSTRUCTOR, code);
    }

    /**
     * Returns a method containing the body of {@code code}, accepting the
     * {@code argClasses}, and returning {@code argClasses}.
     */
    private java.lang.reflect.Method newMethod(
            Class<?> returnClass, Code code, Class<?>... argClasses) throws Exception {
        Type<?> returnType = generator.getType(returnClass);
        Type<?>[] argTypes = new Type<?>[argClasses.length];
        for (int i = 0; i < argClasses.length; i++) {
            argTypes[i] = generator.getType(argClasses[i]);
        }

        Method<?, ?> method = generatedType.getMethod(returnType, "generatedMethod", argTypes);
        generatedType.declare("Generated.java", ACC_PUBLIC, objectType);
        method.declare(ACC_PUBLIC | ACC_STATIC, code);
        return loadAndGenerate().getMethod("generatedMethod", argClasses);
    }

    private Class<?> loadAndGenerate() throws IOException, ClassNotFoundException {
        return generator.load(DexGeneratorTest.class.getClassLoader()).loadClass("Generated");
    }

    private static Class<?> unbox(Class<?> boxed) {
        Class<?> unboxed = BOXED_TO_PRIMITIVE.get(boxed);
        return unboxed != null ? unboxed : boxed;
    }
}
