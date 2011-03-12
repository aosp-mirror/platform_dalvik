/*
 * Copyright (C) 2010 The Android Open Source Project
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

import other.Mutant;

/*
 * Entry point and tests that are expected to succeed.
 */
public class Main {

    /**
     * Drives tests.
     */
    public static void main(String[] args) {

        // Test static put/get
        testStaticInt();
        testStaticVolatileInt();
        testStaticWide();
        testStaticVolatileWide();
        testStaticObject();
        testStaticVolatileObject();
        testStaticBoolean();
        testStaticByte();
        testStaticChar();
        testStaticShort();

        // Test field put/get
        JumboField fieldTest = new JumboField();
        testFieldInt(fieldTest);
        testFieldVolatileInt(fieldTest);
        testFieldWide(fieldTest);
        testFieldVolatileWide(fieldTest);
        testFieldObject(fieldTest);
        testFieldVolatileObject(fieldTest);
        testFieldBoolean(fieldTest);
        testFieldByte(fieldTest);
        testFieldChar(fieldTest);
        testFieldShort(fieldTest);

        // Test method invokes
        JumboMethod methodTest = new JumboMethod();
        methodTest.testMethods();

        // Test remaining jumbo instructions
        // const-class/jumbo, check-cast/jumbo, instance-of/jumbo,
        // new-instance/jumbo, new-array/jumbo, filled-new-array/jumbo
        // throw-verification-error/jumbo
        JumboRegister registerTest = new JumboRegister();
        registerTest.testRegisters();
    }

    // Test sput/jumbo & sget/jumbo
    public static void testStaticInt() {
        int putInt = 0x12345678;
        JumboStatic.testInt = putInt;
        int getInt = JumboStatic.testInt;
        if (putInt != getInt) {
            System.out.println("Static put int: " + putInt +
                " does not match static get int: " + getInt);
        }
    }

    // Test sput-wide/jumbo & sget-wide/jumbo
    public static void testStaticWide() {
        long putWide = 0xfedcba9876543210l;
        JumboStatic.testWide = putWide;
        long getWide = JumboStatic.testWide;
        if (putWide != getWide) {
            System.out.println("Static put wide: " + putWide +
                " does not match static get wide: " + getWide);
        }
    }

    // Test sput-object/jumbo & sget-object/jumbo
    public static void testStaticObject() {
        Object putObject = new Object();
        JumboStatic.testObject = putObject;
        Object getObject = JumboStatic.testObject;
        if (putObject != getObject) {
            System.out.println("Static put object: " + putObject +
                " does not match static get object: " + getObject);
        }
    }

    // Test sput-volatile/jumbo & sget-volatile/jumbo
    public static void testStaticVolatileInt() {
        int putInt = 0x12345678;
        JumboStatic.testVolatileInt = putInt;
        int getInt = JumboStatic.testVolatileInt;
        if (putInt != getInt) {
            System.out.println("Static put int: " + putInt +
                " does not match static get int: " + getInt);
        }
    }

    // Test sput-wide-volatile/jumbo & sget-wide-volatile/jumbo
    public static void testStaticVolatileWide() {
        long putWide = 0xfedcba9876543210l;
        JumboStatic.testVolatileWide = putWide;
        long getWide = JumboStatic.testVolatileWide;
        if (putWide != getWide) {
            System.out.println("Static put wide: " + putWide +
                " does not match static get wide: " + getWide);
        }
    }

    // Test sput-object-volatile/jumbo & sget-object-volatile/jumbo
    public static void testStaticVolatileObject() {
        Object putObject = new Object();
        JumboStatic.testVolatileObject = putObject;
        Object getObject = JumboStatic.testVolatileObject;
        if (putObject != getObject) {
            System.out.println("Static put object: " + putObject +
                " does not match static get object: " + getObject);
        }
    }

    // Test sput-boolean/jumbo & sget-boolean/jumbo
    public static void testStaticBoolean() {
        boolean putBoolean = true;
        JumboStatic.testBoolean = putBoolean;
        boolean getBoolean = JumboStatic.testBoolean;
        if (putBoolean != getBoolean) {
            System.out.println("Static put boolean: " + putBoolean +
                " does not match static get boolean: " + getBoolean);
        }
    }

    // Test sput-byte/jumbo & sget-byte/jumbo
    public static void testStaticByte() {
        byte putByte = 0x6D;
        JumboStatic.testByte = putByte;
        byte getByte = JumboStatic.testByte;
        if (putByte != getByte) {
            System.out.println("Static put byte: " + putByte +
                " does not match static get byte: " + getByte);
        }
    }

    // Test sput-char/jumbo & sget-char/jumbo
    public static void testStaticChar() {
        char putChar = 0xE5;
        JumboStatic.testChar = putChar;
        char getChar = JumboStatic.testChar;
        if (putChar != getChar) {
            System.out.println("Static put char: " + putChar +
                " does not match static get char: " + getChar);
        }
    }

    // Test sput-short/jumbo & sget-short/jumbo
    public static void testStaticShort() {
        short putShort = 0x7A3B;
        JumboStatic.testShort = putShort;
        short getShort = JumboStatic.testShort;
        if (putShort != getShort) {
            System.out.println("Static put short: " + putShort +
                " does not match static get short: " + getShort);
        }
    }

    // Test iput/jumbo & iget/jumbo
    public static void testFieldInt(JumboField fieldTest) {
        int putInt = 0x12345678;
        fieldTest.testInt = putInt;
        int getInt = fieldTest.testInt;
        if (putInt != getInt) {
            System.out.println("Field put int: " + putInt +
                " does not match field get int: " + getInt);
        }
    }

    // Test iput-wide/jumbo & iget-wide/jumbo
    public static void testFieldWide(JumboField fieldTest) {
        long putWide = 0xfedcba9876543210l;
        fieldTest.testWide = putWide;
        long getWide = fieldTest.testWide;
        if (putWide != getWide) {
            System.out.println("Field put wide: " + putWide +
                " does not match field get wide: " + getWide);
        }
    }

    // Test iput-object/jumbo & iget-object/jumbo
    public static void testFieldObject(JumboField fieldTest) {
        Object putObject = new Object();
        fieldTest.testObject = putObject;
        Object getObject = fieldTest.testObject;
        if (putObject != getObject) {
            System.out.println("Field put object: " + putObject +
                " does not match field get object: " + getObject);
        }
    }

    // Test iput-volatile/jumbo & iget-volatile/jumbo
    public static void testFieldVolatileInt(JumboField fieldTest) {
        int putInt = 0x12345678;
        fieldTest.testVolatileInt = putInt;
        int getInt = fieldTest.testVolatileInt;
        if (putInt != getInt) {
            System.out.println("Field put int: " + putInt +
                " does not match field get int: " + getInt);
        }
    }

    // Test iput-wide-volatile/jumbo & iget-wide-volatile/jumbo
    public static void testFieldVolatileWide(JumboField fieldTest) {
        long putWide = 0xfedcba9876543210l;
        fieldTest.testVolatileWide = putWide;
        long getWide = fieldTest.testVolatileWide;
        if (putWide != getWide) {
            System.out.println("Field put wide: " + putWide +
                " does not match field get wide: " + getWide);
        }
    }

    // Test iput-object-volatile/jumbo & iget-object-volatile/jumbo
    public static void testFieldVolatileObject(JumboField fieldTest) {
        Object putObject = new Object();
        fieldTest.testVolatileObject = putObject;
        Object getObject = fieldTest.testVolatileObject;
        if (putObject != getObject) {
            System.out.println("Field put object: " + putObject +
                " does not match field get object: " + getObject);
        }
    }

    // Test iput-boolean/jumbo & iget-boolean/jumbo
    public static void testFieldBoolean(JumboField fieldTest) {
        boolean putBoolean = true;
        fieldTest.testBoolean = putBoolean;
        boolean getBoolean = fieldTest.testBoolean;
        if (putBoolean != getBoolean) {
            System.out.println("Field put boolean: " + putBoolean +
                " does not match field get boolean: " + getBoolean);
        }
    }

    // Test iput-byte/jumbo & iget-byte/jumbo
    public static void testFieldByte(JumboField fieldTest) {
        byte putByte = 0x6D;
        fieldTest.testByte = putByte;
        byte getByte = fieldTest.testByte;
        if (putByte != getByte) {
            System.out.println("Field put byte: " + putByte +
                " does not match field get byte: " + getByte);
        }
    }

    // Test iput-char/jumbo & iget-char/jumbo
    public static void testFieldChar(JumboField fieldTest) {
        char putChar = 0xE5;
        fieldTest.testChar = putChar;
        char getChar = fieldTest.testChar;
        if (putChar != getChar) {
            System.out.println("Field put char: " + putChar +
                " does not match field get char: " + getChar);
        }
    }

    // Test iput-short/jumbo & iget-short/jumbo
    public static void testFieldShort(JumboField fieldTest) {
        short putShort = 0x7A3B;
        fieldTest.testShort = putShort;
        short getShort = fieldTest.testShort;
        if (putShort != getShort) {
            System.out.println("Field put short: " + putShort +
                " does not match field get short: " + getShort);
        }
    }
}

class JumboStatic {
    static int staticInt1;
    static int staticInt2;
    static int staticInt3;
    static int staticInt4;
    static int staticInt5;
    static int staticInt6;
    static int staticInt7;
    static int staticInt8;
    static int staticInt9;
    static int staticInt10;
    static int staticInt11;
    static int staticInt12;
    static int staticInt13;
    static int staticInt14;
    static int staticInt15;
    static int staticInt16;
    static int staticInt17;
    static int staticInt18;
    static int staticInt19;
    static int staticInt20;
    static int staticInt21;
    static int staticInt22;
    static int staticInt23;
    static int staticInt24;
    static int staticInt25;
    static int staticInt26;
    static int staticInt27;
    static int staticInt28;
    static int staticInt29;
    static int staticInt30;
    static int staticInt31;
    static int staticInt32;
    static int staticInt33;
    static int staticInt34;
    static int staticInt35;
    static int staticInt36;
    static int staticInt37;
    static int staticInt38;
    static int staticInt39;
    static int staticInt40;
    static int staticInt41;
    static int staticInt42;
    static int staticInt43;
    static int staticInt44;
    static int staticInt45;
    static int staticInt46;
    static int staticInt47;
    static int staticInt48;
    static int staticInt49;
    static int staticInt50;

    static int     testInt;
    static long    testWide;
    static Object  testObject;
    static boolean testBoolean;
    static byte    testByte;
    static char    testChar;
    static short   testShort;
    static volatile int     testVolatileInt;
    static volatile long    testVolatileWide;
    static volatile Object  testVolatileObject;
}

class JumboField {
    int fieldInt1;
    int fieldInt2;
    int fieldInt3;
    int fieldInt4;
    int fieldInt5;
    int fieldInt6;
    int fieldInt7;
    int fieldInt8;
    int fieldInt9;
    int fieldInt10;
    int fieldInt11;
    int fieldInt12;
    int fieldInt13;
    int fieldInt14;
    int fieldInt15;
    int fieldInt16;
    int fieldInt17;
    int fieldInt18;
    int fieldInt19;
    int fieldInt20;
    int fieldInt21;
    int fieldInt22;
    int fieldInt23;
    int fieldInt24;
    int fieldInt25;
    int fieldInt26;
    int fieldInt27;
    int fieldInt28;
    int fieldInt29;
    int fieldInt30;
    int fieldInt31;
    int fieldInt32;
    int fieldInt33;
    int fieldInt34;
    int fieldInt35;
    int fieldInt36;
    int fieldInt37;
    int fieldInt38;
    int fieldInt39;
    int fieldInt40;
    int fieldInt41;
    int fieldInt42;
    int fieldInt43;
    int fieldInt44;
    int fieldInt45;
    int fieldInt46;
    int fieldInt47;
    int fieldInt48;
    int fieldInt49;
    int fieldInt50;

    int     testInt;
    long    testWide;
    Object  testObject;
    boolean testBoolean;
    byte    testByte;
    char    testChar;
    short   testShort;
    volatile int     testVolatileInt;
    volatile long    testVolatileWide;
    volatile Object  testVolatileObject;
}

class JumboMethodSuper {
    void testSuper() {
        System.out.println("Invoked super");
    }
}

interface JumboMethodInterface {
    void testInterface();
}

class JumboMethod extends JumboMethodSuper implements JumboMethodInterface {
    void meth1() { }
    void meth2() { }
    void meth3() { }
    void meth4() { }
    void meth5() { }
    void meth6() { }
    void meth7() { }
    void meth8() { }
    void meth9() { }
    void meth10() { }
    void meth11() { }
    void meth12() { }
    void meth13() { }
    void meth14() { }
    void meth15() { }
    void meth16() { }
    void meth17() { }
    void meth18() { }
    void meth19() { }
    void meth20() { }
    void meth21() { }
    void meth22() { }
    void meth23() { }
    void meth24() { }
    void meth25() { }
    void meth26() { }
    void meth27() { }
    void meth28() { }
    void meth29() { }
    void meth30() { }
    void meth31() { }
    void meth32() { }
    void meth33() { }
    void meth34() { }
    void meth35() { }
    void meth36() { }
    void meth37() { }
    void meth38() { }
    void meth39() { }
    void meth40() { }
    void meth41() { }
    void meth42() { }
    void meth43() { }
    void meth44() { }
    void meth45() { }
    void meth46() { }
    void meth47() { }
    void meth48() { }
    void meth49() { }
    void meth50() { }

    void testMethods() {
        testVirtual();
        super.testSuper();
        testDirect();
        testStatic();
        ((JumboMethodInterface) this).testInterface();
    }

    void testVirtual() {
        System.out.println("Invoked virtual");
    }

    void testSuper() {
        System.out.println("Invoked base");
    }

    private void testDirect() {
        System.out.println("Invoked direct");
    }

    static void testStatic() {
        System.out.println("Invoked static");
    }

    public void testInterface() {
        System.out.println("Invoked interface");
    }
}

class JumboRegister {
    void testRegisters() {
        // Create a bunch of registers
        Class c1 = Thread.class;
        Class c2 = Thread.class;
        Class c3 = Thread.class;
        Class c4 = Thread.class;
        Class c5 = Thread.class;
        Class c6 = Thread.class;
        Class c7 = Thread.class;
        Class c8 = Thread.class;
        Class c9 = Thread.class;
        Class c10 = Thread.class;
        Class c11 = Thread.class;
        Class c12 = Thread.class;
        Class c13 = Thread.class;
        Class c14 = Thread.class;
        Class c15 = Thread.class;
        Class c16 = Thread.class;
        Class c17 = Thread.class;
        Class c18 = Thread.class;
        Class c19 = Thread.class;
        Class c20 = Thread.class;
        Class c21 = Thread.class;
        Class c22 = Thread.class;
        Class c23 = Thread.class;
        Class c24 = Thread.class;
        Class c25 = Thread.class;
        Class c26 = Thread.class;
        Class c27 = Thread.class;
        Class c28 = Thread.class;
        Class c29 = Thread.class;
        Class c30 = Thread.class;
        Class c31 = Thread.class;
        Class c32 = Thread.class;
        Class c33 = Thread.class;
        Class c34 = Thread.class;
        Class c35 = Thread.class;
        Class c36 = Thread.class;
        Class c37 = Thread.class;
        Class c38 = Thread.class;
        Class c39 = Thread.class;
        Class c40 = Thread.class;
        Class c41 = Thread.class;
        Class c42 = Thread.class;
        Class c43 = Thread.class;
        Class c44 = Thread.class;
        Class c45 = Thread.class;
        Class c46 = Thread.class;
        Class c47 = Thread.class;
        Class c48 = Thread.class;
        Class c49 = Thread.class;
        Class c50 = Thread.class;
        Class c51 = Thread.class;
        Class c52 = Thread.class;
        Class c53 = Thread.class;
        Class c54 = Thread.class;
        Class c55 = Thread.class;
        Class c56 = Thread.class;
        Class c57 = Thread.class;
        Class c58 = Thread.class;
        Class c59 = Thread.class;
        Class c60 = Thread.class;
        Class c61 = Thread.class;
        Class c62 = Thread.class;
        Class c63 = Thread.class;
        Class c64 = Thread.class;
        Class c65 = Thread.class;
        Class c66 = Thread.class;
        Class c67 = Thread.class;
        Class c68 = Thread.class;
        Class c69 = Thread.class;
        Class c70 = Thread.class;
        Class c71 = Thread.class;
        Class c72 = Thread.class;
        Class c73 = Thread.class;
        Class c74 = Thread.class;
        Class c75 = Thread.class;
        Class c76 = Thread.class;
        Class c77 = Thread.class;
        Class c78 = Thread.class;
        Class c79 = Thread.class;
        Class c80 = Thread.class;
        Class c81 = Thread.class;
        Class c82 = Thread.class;
        Class c83 = Thread.class;
        Class c84 = Thread.class;
        Class c85 = Thread.class;
        Class c86 = Thread.class;
        Class c87 = Thread.class;
        Class c88 = Thread.class;
        Class c89 = Thread.class;
        Class c90 = Thread.class;
        Class c91 = Thread.class;
        Class c92 = Thread.class;
        Class c93 = Thread.class;
        Class c94 = Thread.class;
        Class c95 = Thread.class;
        Class c96 = Thread.class;
        Class c97 = Thread.class;
        Class c98 = Thread.class;
        Class c99 = Thread.class;
        Class c100 = Thread.class;
        Class c101 = Thread.class;
        Class c102 = Thread.class;
        Class c103 = Thread.class;
        Class c104 = Thread.class;
        Class c105 = Thread.class;
        Class c106 = Thread.class;
        Class c107 = Thread.class;
        Class c108 = Thread.class;
        Class c109 = Thread.class;
        Class c110 = Thread.class;
        Class c111 = Thread.class;
        Class c112 = Thread.class;
        Class c113 = Thread.class;
        Class c114 = Thread.class;
        Class c115 = Thread.class;
        Class c116 = Thread.class;
        Class c117 = Thread.class;
        Class c118 = Thread.class;
        Class c119 = Thread.class;
        Class c120 = Thread.class;
        Class c121 = Thread.class;
        Class c122 = Thread.class;
        Class c123 = Thread.class;
        Class c124 = Thread.class;
        Class c125 = Thread.class;
        Class c126 = Thread.class;
        Class c127 = Thread.class;
        Class c128 = Thread.class;
        Class c129 = Thread.class;
        Class c130 = Thread.class;
        Class c131 = Thread.class;
        Class c132 = Thread.class;
        Class c133 = Thread.class;
        Class c134 = Thread.class;
        Class c135 = Thread.class;
        Class c136 = Thread.class;
        Class c137 = Thread.class;
        Class c138 = Thread.class;
        Class c139 = Thread.class;
        Class c140 = Thread.class;
        Class c141 = Thread.class;
        Class c142 = Thread.class;
        Class c143 = Thread.class;
        Class c144 = Thread.class;
        Class c145 = Thread.class;
        Class c146 = Thread.class;
        Class c147 = Thread.class;
        Class c148 = Thread.class;
        Class c149 = Thread.class;
        Class c150 = Thread.class;
        Class c151 = Thread.class;
        Class c152 = Thread.class;
        Class c153 = Thread.class;
        Class c154 = Thread.class;
        Class c155 = Thread.class;
        Class c156 = Thread.class;
        Class c157 = Thread.class;
        Class c158 = Thread.class;
        Class c159 = Thread.class;
        Class c160 = Thread.class;
        Class c161 = Thread.class;
        Class c162 = Thread.class;
        Class c163 = Thread.class;
        Class c164 = Thread.class;
        Class c165 = Thread.class;
        Class c166 = Thread.class;
        Class c167 = Thread.class;
        Class c168 = Thread.class;
        Class c169 = Thread.class;
        Class c170 = Thread.class;
        Class c171 = Thread.class;
        Class c172 = Thread.class;
        Class c173 = Thread.class;
        Class c174 = Thread.class;
        Class c175 = Thread.class;
        Class c176 = Thread.class;
        Class c177 = Thread.class;
        Class c178 = Thread.class;
        Class c179 = Thread.class;
        Class c180 = Thread.class;
        Class c181 = Thread.class;
        Class c182 = Thread.class;
        Class c183 = Thread.class;
        Class c184 = Thread.class;
        Class c185 = Thread.class;
        Class c186 = Thread.class;
        Class c187 = Thread.class;
        Class c188 = Thread.class;
        Class c189 = Thread.class;
        Class c190 = Thread.class;
        Class c191 = Thread.class;
        Class c192 = Thread.class;
        Class c193 = Thread.class;
        Class c194 = Thread.class;
        Class c195 = Thread.class;
        Class c196 = Thread.class;
        Class c197 = Thread.class;
        Class c198 = Thread.class;
        Class c199 = Thread.class;
        Class c200 = Thread.class;
        Class c201 = Thread.class;
        Class c202 = Thread.class;
        Class c203 = Thread.class;
        Class c204 = Thread.class;
        Class c205 = Thread.class;
        Class c206 = Thread.class;
        Class c207 = Thread.class;
        Class c208 = Thread.class;
        Class c209 = Thread.class;
        Class c210 = Thread.class;
        Class c211 = Thread.class;
        Class c212 = Thread.class;
        Class c213 = Thread.class;
        Class c214 = Thread.class;
        Class c215 = Thread.class;
        Class c216 = Thread.class;
        Class c217 = Thread.class;
        Class c218 = Thread.class;
        Class c219 = Thread.class;
        Class c220 = Thread.class;
        Class c221 = Thread.class;
        Class c222 = Thread.class;
        Class c223 = Thread.class;
        Class c224 = Thread.class;
        Class c225 = Thread.class;
        Class c226 = Thread.class;
        Class c227 = Thread.class;
        Class c228 = Thread.class;
        Class c229 = Thread.class;
        Class c230 = Thread.class;
        Class c231 = Thread.class;
        Class c232 = Thread.class;
        Class c233 = Thread.class;
        Class c234 = Thread.class;
        Class c235 = Thread.class;
        Class c236 = Thread.class;
        Class c237 = Thread.class;
        Class c238 = Thread.class;
        Class c239 = Thread.class;
        Class c240 = Thread.class;
        Class c241 = Thread.class;
        Class c242 = Thread.class;
        Class c243 = Thread.class;
        Class c244 = Thread.class;
        Class c245 = Thread.class;
        Class c246 = Thread.class;
        Class c247 = Thread.class;
        Class c248 = Thread.class;
        Class c249 = Thread.class;
        Class c250 = Thread.class;
        Class c251 = Thread.class;
        Class c252 = Thread.class;
        Class c253 = Thread.class;
        Class c254 = Thread.class;
        Class c255 = Thread.class;

        // Test const-class/jumbo
        Class c256 = Thread.class;

        // Test check-cast/jumbo

        // Test instance-of/jumbo
        boolean b1 = c1 instanceof Object;
        if (!b1) System.out.println("instance-of/jumbo returned wrong result");

        // Test new-instance/jumbo
        Object o1 = new Object();

        // Test new-array/jumbo
        int[] a1 = new int[10];
        a1[0] = 1;
        a1[1] = 2;
        a1[2] = 3;
        a1[3] = 4;
        a1[4] = 5;
        a1[5] = 6;
        a1[6] = 7;
        a1[7] = 8;
        a1[8] = 9;
        a1[9] = 10;

        // Test filled-new-array/jumbo

        // Test throw-verification-error/jumbo
        try {
            MaybeAbstract ma = new MaybeAbstract();
            System.err.println("ERROR: MaybeAbstract succeeded unexpectedly");
        } catch (InstantiationError ie) {
            System.out.println("Got expected InstantationError");
        } catch (Exception ex) {
            System.err.println("Got unexpected MaybeAbstract failure");
        }
        testMissingStuff();

        // Do something with those registers to force other ops to be jumbo
        useRegs(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10);
        useRegs(c11, c12, c13, c14, c15, c16, c17, c18, c19, c20);
        useRegs(c21, c22, c23, c24, c25, c26, c27, c28, c29, c30);
        useRegs(c31, c32, c33, c34, c35, c36, c37, c38, c39, c40);
        useRegs(c41, c42, c43, c44, c45, c46, c47, c48, c49, c50);
        useRegs(c51, c52, c53, c54, c55, c56, c57, c58, c59, c60);
        useRegs(c61, c62, c63, c64, c65, c66, c67, c68, c69, c70);
        useRegs(c71, c72, c73, c74, c75, c76, c77, c78, c79, c80);
        useRegs(c81, c82, c83, c84, c85, c86, c87, c88, c89, c90);
        useRegs(c91, c92, c93, c94, c95, c96, c97, c98, c99, c100);
        useRegs(c101, c102, c103, c104, c105, c106, c107, c108, c109, c110);
        useRegs(c111, c112, c113, c114, c115, c116, c117, c118, c119, c120);
        useRegs(c121, c122, c123, c124, c125, c126, c127, c128, c129, c130);
        useRegs(c131, c132, c133, c134, c135, c136, c137, c138, c139, c140);
        useRegs(c141, c142, c143, c144, c145, c146, c147, c148, c149, c150);
        useRegs(c151, c152, c153, c154, c155, c156, c157, c158, c159, c160);
        useRegs(c161, c162, c163, c164, c165, c166, c167, c168, c169, c170);
        useRegs(c171, c172, c173, c174, c175, c176, c177, c178, c179, c180);
        useRegs(c181, c182, c183, c184, c185, c186, c187, c188, c189, c190);
        useRegs(c191, c192, c193, c194, c195, c196, c197, c198, c199, c200);
        useRegs(c201, c202, c203, c204, c205, c206, c207, c208, c209, c210);
        useRegs(c211, c212, c213, c214, c215, c216, c217, c218, c219, c220);
        useRegs(c221, c222, c223, c224, c225, c226, c227, c228, c229, c230);
        useRegs(c231, c232, c233, c234, c235, c236, c237, c238, c239, c240);
        useRegs(c241, c242, c243, c244, c245, c246, c247, c248, c249, c250);
        useRegs(c251, c252, c253, c254, c255, c256, c256, c256, c256, c256);

        useRegs(b1);
        useRegs(o1);
        useRegs(a1);
    }

    // Trigger more jumbo verification errors
    static void testMissingStuff() {
        Mutant mutant = new Mutant();

        try {
            int x = mutant.disappearingField;
        } catch (NoSuchFieldError nsfe) {
            System.out.println("Got expected NoSuchFieldError");
        }

        try {
            int y = Mutant.disappearingStaticField;
        } catch (NoSuchFieldError nsfe) {
            System.out.println("Got expected NoSuchFieldError");
        }

        try {
            mutant.disappearingMethod();
        } catch (NoSuchMethodError nsme) {
            System.out.println("Got expected NoSuchMethodError");
        }

        try {
            Mutant.disappearingStaticMethod();
        } catch (NoSuchMethodError nsme) {
            System.out.println("Got expected NoSuchMethodError");
        }
    }

    void useRegs(Object o1, Object o2, Object o3, Object o4, Object o5,
        Object o6, Object o7, Object o8, Object o9, Object o10) {
    }

    void useRegs(Object o1) { }
    void useRegs(boolean b1) { }
}
