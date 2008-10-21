/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.luni.internal.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashSet;

public final class ProxyClassFile implements ProxyConstants {

    private static final int INITIAL_CONTENTS_SIZE = 1000;

    private static final int INITIAL_HEADER_SIZE = 500;

    private static final int INCREMENT_SIZE = 250;

    private static Method ObjectEqualsMethod;

    private static Method ObjectHashCodeMethod;

    private static Method ObjectToStringMethod;

    private static Method ClassForNameMethod;

    private static Method ClassGetMethod;

    private static Method HandlerInvokeMethod;

    private static Constructor<?> ProxyConstructor;

    private static Constructor<?> UndeclaredThrowableExceptionConstructor;

    private static Field ProxyHandlerField;

    public static byte[] generateBytes(String typeName, Class[] interfaces) {
        ProxyClassFile classFile = new ProxyClassFile(typeName, interfaces);
        classFile.addFields();
        classFile.findMethods(interfaces);
        classFile.addMethods();
        classFile.addAttributes();
        return classFile.getBytes();
    }

    static char[] getConstantPoolName(Class<?> c) {
        if (c.isArray()) {
            // Array classes are named/ with their signature
            return c.getName().replace('.', '/').toCharArray();
        }

        if (c.isPrimitive()) {
            // Special cases for each base type.
            if (c == void.class) {
                return new char[] { 'V' };
            }
            if (c == int.class) {
                return new char[] { 'I' };
            }
            if (c == boolean.class) {
                return new char[] { 'Z' };
            }
            if (c == byte.class) {
                return new char[] { 'B' };
            }
            if (c == char.class) {
                return new char[] { 'C' };
            }
            if (c == short.class) {
                return new char[] { 'S' };
            }
            if (c == long.class) {
                return new char[] { 'J' };
            }
            if (c == float.class) {
                return new char[] { 'F' };
            }
            if (c == double.class) {
                return new char[] { 'D' };
            }
        }
        return ("L" + c.getName().replace('.', '/') + ";").toCharArray();
    }

    static char[] getConstantPoolName(Constructor<?> method) /* (ILjava/lang/Thread;)V */{
        Class[] parameters = method.getParameterTypes();
        StringBuffer buffer = new StringBuffer(parameters.length + 1 * 20);
        buffer.append('(');
        for (Class<?> element : parameters) {
            buffer.append(getConstantPoolName(element));
        }
        buffer.append(')');
        buffer.append(getConstantPoolName(void.class));
        return buffer.toString().toCharArray();
    }

    static char[] getConstantPoolName(Method method) /* (ILjava/lang/Thread;)Ljava/lang/Object; */{
        Class[] parameters = method.getParameterTypes();
        StringBuffer buffer = new StringBuffer(parameters.length + 1 * 20);
        buffer.append('(');
        for (Class<?> element : parameters) {
            buffer.append(getConstantPoolName(element));
        }
        buffer.append(')');
        buffer.append(getConstantPoolName(method.getReturnType()));
        return buffer.toString().toCharArray();
    }

    private ProxyConstantPool constantPool;

    // the header contains all the bytes till the end of the constant pool
    byte[] header;

    int headerOffset;

    // that collection contains all the remaining bytes of the .class file
    private byte[] contents;

    private int contentsOffset;

    private int constantPoolOffset;

    private ProxyMethod[] proxyMethods;

    ProxyClassFile(String typeName, Class[] interfaces) {
        super();
        header = new byte[INITIAL_HEADER_SIZE];
        // generate the magic numbers inside the header
        header[headerOffset++] = (byte) (0xCAFEBABEL >> 24);
        header[headerOffset++] = (byte) (0xCAFEBABEL >> 16);
        header[headerOffset++] = (byte) (0xCAFEBABEL >> 8);
        header[headerOffset++] = (byte) (0xCAFEBABEL >> 0);
        // Compatible with JDK 1.2
        header[headerOffset++] = 0;
        header[headerOffset++] = 0;
        header[headerOffset++] = 0;
        header[headerOffset++] = 46;
        constantPoolOffset = headerOffset;
        headerOffset += 2;
        constantPool = new ProxyConstantPool(this);
        contents = new byte[INITIAL_CONTENTS_SIZE];
        // now we continue to generate the bytes inside the contents array
        int accessFlags = AccPublic | AccFinal | AccSuper;
        contents[contentsOffset++] = (byte) (accessFlags >> 8);
        contents[contentsOffset++] = (byte) accessFlags;
        int classNameIndex = constantPool.typeIndex(typeName);
        contents[contentsOffset++] = (byte) (classNameIndex >> 8);
        contents[contentsOffset++] = (byte) classNameIndex;
        int superclassNameIndex = constantPool
                .typeIndex("java/lang/reflect/Proxy");
        contents[contentsOffset++] = (byte) (superclassNameIndex >> 8);
        contents[contentsOffset++] = (byte) superclassNameIndex;
        int interfacesCount = interfaces.length;
        contents[contentsOffset++] = (byte) (interfacesCount >> 8);
        contents[contentsOffset++] = (byte) interfacesCount;
        for (int i = 0; i < interfacesCount; i++) {
            int interfaceIndex = constantPool
                    .typeIndex(interfaces[i].getName());
            contents[contentsOffset++] = (byte) (interfaceIndex >> 8);
            contents[contentsOffset++] = (byte) interfaceIndex;
        }
    }

    private void addAttributes() {
        writeUnsignedShort(0); // classFile does not have attributes of its own

        // resynchronize all offsets of the classfile
        header = constantPool.poolContent;
        headerOffset = constantPool.currentOffset;
        int constantPoolCount = constantPool.currentIndex;
        header[constantPoolOffset++] = (byte) (constantPoolCount >> 8);
        header[constantPoolOffset] = (byte) constantPoolCount;
    }

    private void addFields() {
        writeUnsignedShort(0); // we have no fields
    }

    private void addMethods() {
        int methodCount = proxyMethods.length;
        writeUnsignedShort(methodCount + 1);

        // save constructor
        writeUnsignedShort(AccPublic);
        writeUnsignedShort(constantPool.literalIndex(Init));
        if (ProxyConstructor == null) {
            try {
                ProxyConstructor = Proxy.class
                        .getDeclaredConstructor(new Class[] { InvocationHandler.class });
            } catch (NoSuchMethodException e) {
                throw new InternalError();
            }
        }
        writeUnsignedShort(constantPool
                .literalIndex(getConstantPoolName(ProxyConstructor)));
        writeUnsignedShort(1); // store just the code attribute
        writeUnsignedShort(constantPool.literalIndex(CodeName));
        // save attribute_length(4), max_stack(2), max_locals(2), code_length(4)
        int codeLength = 6;
        writeUnsignedWord(12 + codeLength); // max_stack(2), max_locals(2),
        // code_length(4), 2 zero shorts
        writeUnsignedShort(2);
        writeUnsignedShort(2);
        writeUnsignedWord(codeLength);
        writeUnsignedByte(OPC_aload_0);
        writeUnsignedByte(OPC_aload_1);
        writeUnsignedByte(OPC_invokespecial);
        writeUnsignedShort(constantPool.literalIndex(ProxyConstructor));
        writeUnsignedByte(OPC_return);
        writeUnsignedShort(0); // no exceptions table
        writeUnsignedShort(0); // there are no attributes for the code
        // attribute

        for (int i = 0; i < methodCount; i++) {
            ProxyMethod pMethod = proxyMethods[i];
            Method method = pMethod.method;
            writeUnsignedShort(AccPublic | AccFinal);
            writeUnsignedShort(constantPool.literalIndex(method.getName()
                    .toCharArray()));
            writeUnsignedShort(constantPool
                    .literalIndex(getConstantPoolName(method)));
            Class[] thrownsExceptions = pMethod.commonExceptions;
            int eLength = thrownsExceptions.length;
            if (eLength > 0) {
                writeUnsignedShort(2); // store the exception & code attributes
                // The method has a throw clause. So we need to add an exception
                // attribute
                writeUnsignedShort(constantPool.literalIndex(ExceptionsName));
                // The attribute length = length * 2 + 2 in case of a exception
                // attribute
                writeUnsignedWord(eLength * 2 + 2);
                writeUnsignedShort(eLength);
                for (int e = 0; e < eLength; e++) {
                    writeUnsignedShort(constantPool
                            .typeIndex(thrownsExceptions[e].getName()));
                }
            } else {
                writeUnsignedShort(1); // store just the code attribute
            }
            generateCodeAttribute(pMethod);
        }
    }

    private void findMethods(Class[] interfaces) {
        /*
         * find all methods defined by the interfaces (including inherited
         * interfaces) plus hashCode, equals & toString from Object build an
         * array with the methods... no duplicates - check up to the array size
         * when the interface's first method was added
         */
        if (ObjectEqualsMethod == null) {
            try {
                ObjectEqualsMethod = Object.class.getMethod("equals",
                        new Class[] { Object.class });
                ObjectHashCodeMethod = Object.class.getMethod("hashCode",
                        new Class[0]);
                ObjectToStringMethod = Object.class.getMethod("toString",
                        new Class[0]);
            } catch (NoSuchMethodException ex) {
                throw new InternalError();
            }
        }

        ArrayList<ProxyMethod> allMethods = new ArrayList<ProxyMethod>(25);
        allMethods.add(new ProxyMethod(ObjectEqualsMethod));
        allMethods.add(new ProxyMethod(ObjectHashCodeMethod));
        allMethods.add(new ProxyMethod(ObjectToStringMethod));

        HashSet<Class<?>> interfacesSeen = new HashSet<Class<?>>();
        for (Class<?> element : interfaces) {
            findMethods(element, allMethods, interfacesSeen);
        }

        proxyMethods = new ProxyMethod[allMethods.size()];
        allMethods.toArray(proxyMethods);
    }

    private void findMethods(Class<?> nextInterface,
            ArrayList<ProxyMethod> allMethods, HashSet<Class<?>> interfacesSeen) {
        /*
         * add the nextInterface's methods to allMethods if an equivalent method
         * already exists then return types must be identical... don't replace
         * it
         */
        if (interfacesSeen.contains(nextInterface)) {
            return; // already walked it
        }
        interfacesSeen.add(nextInterface);

        int existingMethodCount = allMethods.size();
        Method[] methods = nextInterface.getMethods();
        nextMethod: for (Method method : methods) {
            for (int j = 0; j < existingMethodCount; j++) {
                if (allMethods.get(j).matchMethod(method)) {
                    continue nextMethod;
                }
            }
            allMethods.add(new ProxyMethod(method));
        }

        Class<?>[] superInterfaces = nextInterface.getInterfaces();
        for (Class<?> element : superInterfaces) {
            // recursion should be minimal
            findMethods(element, allMethods, interfacesSeen);
        }
    }

    private void generateCodeAttribute(ProxyMethod pMethod) {
        int codeAttributeOffset = contentsOffset;
        int contentsLength = contents.length;
        if (contentsOffset + 20 + 100 >= contentsLength) {
            System.arraycopy(contents, 0, (contents = new byte[contentsLength
                    + INCREMENT_SIZE]), 0, contentsLength);
        }
        writeUnsignedShort(constantPool.literalIndex(CodeName));
        // leave space for attribute_length(4), max_stack(2), max_locals(2),
        // code_length(4)
        contentsOffset += 12;

        /*
         * to push the args for the call to invoke push the receiver field h 0
         * aload0 1 getfield 33 java.lang.reflect.Proxy.h
         * Ljava.lang.reflect.InvocationHandler; push the receiver as the first
         * arg 4 aload0 push the method push the array of args call invoke 89
         * invokeinterface 67
         * java.lang.reflect.InvocationHandler.invoke(Ljava.lang.Object;Ljava.lang.reflect.Method;[Ljava.lang.Object;)Ljava.lang.Object;
         * cast return result catch & convert exceptions if necessary
         */

        int codeStartOffset = contentsOffset;
        writeUnsignedByte(OPC_aload_0);
        writeUnsignedByte(OPC_getfield);
        if (ProxyHandlerField == null) {
            try {
                ProxyHandlerField = Proxy.class.getDeclaredField("h");
            } catch (NoSuchFieldException e) {
                throw new InternalError();
            }
        }
        writeUnsignedShort(constantPool.literalIndex(ProxyHandlerField));
        writeUnsignedByte(OPC_aload_0);
        Method method = pMethod.method;
        Class[] argTypes = method.getParameterTypes();
        genCallGetMethod(method.getDeclaringClass(), method.getName(), argTypes);
        int maxLocals = genInvokeArgs(argTypes);
        writeUnsignedByte(OPC_invokeinterface);
        if (HandlerInvokeMethod == null) {
            try {
                HandlerInvokeMethod = InvocationHandler.class.getMethod(
                        "invoke", new Class[] { Object.class, Method.class,
                                Object[].class });
            } catch (NoSuchMethodException e) {
                throw new InternalError();
            }
        }
        writeUnsignedShort(constantPool.literalIndex(HandlerInvokeMethod));
        writeUnsignedByte(4); // invoke has 3 args
        writeUnsignedByte(0); // 4th operand must be 0
        genCastReturnType(method.getReturnType());
        int codeLength = contentsOffset - codeStartOffset;

        Class[] checkedExceptions = pMethod.getCheckedExceptions();
        int checkedLength = checkedExceptions.length;
        if (checkedLength > 0) {
            int codeEndIndex = contentsOffset - codeStartOffset;
            writeUnsignedByte(OPC_athrow); // re-throw the caught exception

            genStoreArg(maxLocals);
            writeUnsignedByte(OPC_new);
            writeUnsignedShort(constantPool
                    .typeIndex("java/lang/reflect/UndeclaredThrowableException"));
            writeUnsignedByte(OPC_dup);
            genLoadArg(maxLocals);
            maxLocals++; // now expecting the exception
            writeUnsignedByte(OPC_invokespecial);
            if (UndeclaredThrowableExceptionConstructor == null) {
                try {
                    UndeclaredThrowableExceptionConstructor = UndeclaredThrowableException.class
                            .getConstructor(new Class[] { Throwable.class });
                } catch (NoSuchMethodException e) {
                    throw new InternalError();
                }
            }
            writeUnsignedShort(constantPool
                    .literalIndex(UndeclaredThrowableExceptionConstructor));
            writeUnsignedByte(OPC_athrow);

            codeLength = contentsOffset - codeStartOffset;

            // write the exception table
            writeUnsignedShort(checkedLength + 1);
            for (int i = 0; i < checkedLength; i++) {
                writeUnsignedShort(0);
                writeUnsignedShort(codeEndIndex);
                writeUnsignedShort(codeEndIndex);
                writeUnsignedShort(constantPool.typeIndex(checkedExceptions[i]
                        .getName()));
            }
            writeUnsignedShort(0);
            writeUnsignedShort(codeEndIndex);
            writeUnsignedShort(codeEndIndex + 1); // starts after the first
            // throw
            writeUnsignedShort(constantPool.typeIndex("java/lang/Throwable"));
        } else {
            writeUnsignedShort(0); // no exceptions table
        }
        // there are no attributes for the code attribute
        writeUnsignedShort(0);

        /*
         * Complete the creation of the code attribute by setting the
         * attribute_length, max_stack max_locals, code_length & exception table
         * codeAttributeOffset is the position inside contents byte array before
         * we started to write That means that to write the attribute_length you
         * need to offset by 2 the value of codeAttributeOffset to get the right
         * position, 6 for the max_stack etc...
         */
        int codeAttributeLength = contentsOffset - (codeAttributeOffset + 6);
        contents[codeAttributeOffset + 2] = (byte) (codeAttributeLength >> 24);
        contents[codeAttributeOffset + 3] = (byte) (codeAttributeLength >> 16);
        contents[codeAttributeOffset + 4] = (byte) (codeAttributeLength >> 8);
        contents[codeAttributeOffset + 5] = (byte) codeAttributeLength;

        int maxStack = maxLocals + 10; // larger than the exact amount
        contents[codeAttributeOffset + 6] = (byte) (maxStack >> 8);
        contents[codeAttributeOffset + 7] = (byte) maxStack;
        contents[codeAttributeOffset + 8] = (byte) (maxLocals >> 8);
        contents[codeAttributeOffset + 9] = (byte) maxLocals;
        contents[codeAttributeOffset + 10] = (byte) (codeLength >> 24);
        contents[codeAttributeOffset + 11] = (byte) (codeLength >> 16);
        contents[codeAttributeOffset + 12] = (byte) (codeLength >> 8);
        contents[codeAttributeOffset + 13] = (byte) codeLength;
    }

    /**
     * Perform call to Class.getMethod(String, Class[]) receiver 13 ldc 37
     * (java.lang.String) "java.lang.Object" 15 invokestatic 43
     * java.lang.Class.forName(Ljava.lang.String;)Ljava.lang.Class; selector 37
     * ldc 55 (java.lang.String) "equals" plus method args 39 iconst0 40
     * anewarray 39 java.lang.Class or 39 iconst1 40 anewarray 39
     * java.lang.Class 43 dup 44 iconst0 53 ldc 37 (java.lang.String)
     * "java.lang.Object" 55 invokestatic 43
     * java.lang.Class.forName(Ljava.lang.String;)Ljava.lang.Class; 77 aastore
     * or 39 iconst2 40 anewarray 39 java.lang.Class 43 dup 44 iconst0 45
     * getstatic 102 java.lang.Integer.TYPE Ljava.lang.Class; 48 aastore 49 dup
     * 50 iconst1 51 getstatic 104 java.lang.Boolean.TYPE Ljava.lang.Class; 54
     * aastore then 78 invokevirtual 59
     * java.lang.Class.getMethod(Ljava.lang.String;[Ljava.lang.Class;)Ljava.lang.reflect.Method;
     */
    private void genCallGetMethod(Class<?> receiverType, String selector,
            Class[] argTypes) {
        genCallClassForName(receiverType.getName());
        writeLdc(selector);
        int length = argTypes.length;
        writeIntConstant(length);
        writeUnsignedByte(OPC_anewarray);
        writeUnsignedShort(constantPool.typeIndex("java/lang/Class"));
        for (int i = 0; i < length; i++) {
            writeUnsignedByte(OPC_dup);
            writeIntConstant(i);
            Class<?> type = argTypes[i];
            if (type.isPrimitive()) {
                writeUnsignedByte(OPC_getstatic);
                writeUnsignedShort(constantPool.literalIndex(typeField(type)));
            } else {
                genCallClassForName(type.getName());
            }
            writeUnsignedByte(OPC_aastore);
        }
        writeUnsignedByte(OPC_invokevirtual);
        if (ClassGetMethod == null) {
            try {
                ClassGetMethod = Class.class.getMethod("getMethod",
                        new Class[] { String.class, Class[].class });
            } catch (NoSuchMethodException e) {
                throw new InternalError();
            }
        }
        writeUnsignedShort(constantPool.literalIndex(ClassGetMethod));
    }

    /**
     * Add argument array for call to InvocationHandler.invoke
     * 
     * 46 aconstnull or 81 iconst1 82 anewarray 61 java.lang.Object 85 dup 86
     * iconst0 87 aload1 88 aastore or 58 iconst2 59 anewarray 61
     * java.lang.Object 62 dup 63 iconst0 64 new 84 java.lang.Integer 67 dup 68
     * iload1 69 invokespecial 107 java.lang.Integer.<init>(I)V 72 aastore 73
     * dup 74 iconst1 75 new 69 java.lang.Boolean 78 dup 79 iload2 80
     * invokespecial 110 java.lang.Boolean.<init>(Z)V 83 aastore
     */
    private int genInvokeArgs(Class[] argTypes) {
        int argByteOffset = 1; // remember h is at position 0
        int length = argTypes.length;
        if (length == 0) {
            writeUnsignedByte(OPC_aconst_null);
        } else {
            writeIntConstant(length);
            writeUnsignedByte(OPC_anewarray);
            writeUnsignedShort(constantPool.typeIndex("java/lang/Object"));
            for (int i = 0; i < length; i++) {
                writeUnsignedByte(OPC_dup);
                writeIntConstant(i);
                argByteOffset = genInvokeArg(argTypes[i], argByteOffset);
                writeUnsignedByte(OPC_aastore);
            }
        }
        return argByteOffset;
    }

    private int genInvokeArg(Class<?> type, int argByteOffset) {
        // offset represents maxLocals in bytes
        if (type.isPrimitive()) {
            writeUnsignedByte(OPC_new);
            writeUnsignedShort(constantPool.typeIndex(typeWrapperName(type)));
            writeUnsignedByte(OPC_dup);
            if (argByteOffset > 255) {
                writeUnsignedByte(OPC_wide);
            }
            if (type == long.class) {
                switch (argByteOffset) {
                    case 0:
                        writeUnsignedByte(OPC_lload_0);
                        break;
                    case 1:
                        writeUnsignedByte(OPC_lload_1);
                        break;
                    case 2:
                        writeUnsignedByte(OPC_lload_2);
                        break;
                    case 3:
                        writeUnsignedByte(OPC_lload_3);
                        break;
                    default:
                        writeUnsignedByte(OPC_lload);
                        if (argByteOffset > 255) {
                            writeUnsignedShort(argByteOffset);
                        } else {
                            writeUnsignedByte(argByteOffset);
                        }
                }
                argByteOffset += 2;
            } else if (type == float.class) {
                switch (argByteOffset) {
                    case 0:
                        writeUnsignedByte(OPC_fload_0);
                        break;
                    case 1:
                        writeUnsignedByte(OPC_fload_1);
                        break;
                    case 2:
                        writeUnsignedByte(OPC_fload_2);
                        break;
                    case 3:
                        writeUnsignedByte(OPC_fload_3);
                        break;
                    default:
                        writeUnsignedByte(OPC_fload);
                        if (argByteOffset > 255) {
                            writeUnsignedShort(argByteOffset);
                        } else {
                            writeUnsignedByte(argByteOffset);
                        }
                }
                argByteOffset++;
            } else if (type == double.class) {
                switch (argByteOffset) {
                    case 0:
                        writeUnsignedByte(OPC_dload_0);
                        break;
                    case 1:
                        writeUnsignedByte(OPC_dload_1);
                        break;
                    case 2:
                        writeUnsignedByte(OPC_dload_2);
                        break;
                    case 3:
                        writeUnsignedByte(OPC_dload_3);
                        break;
                    default:
                        writeUnsignedByte(OPC_dload);
                        if (argByteOffset > 255) {
                            writeUnsignedShort(argByteOffset);
                        } else {
                            writeUnsignedByte(argByteOffset);
                        }
                }
                argByteOffset += 2;
            } else { // handles int, short, byte, boolean & char
                switch (argByteOffset) {
                    case 0:
                        writeUnsignedByte(OPC_iload_0);
                        break;
                    case 1:
                        writeUnsignedByte(OPC_iload_1);
                        break;
                    case 2:
                        writeUnsignedByte(OPC_iload_2);
                        break;
                    case 3:
                        writeUnsignedByte(OPC_iload_3);
                        break;
                    default:
                        writeUnsignedByte(OPC_iload);
                        if (argByteOffset > 255) {
                            writeUnsignedShort(argByteOffset);
                        } else {
                            writeUnsignedByte(argByteOffset);
                        }
                }
                argByteOffset++;
            }
            writeUnsignedByte(OPC_invokespecial);
            writeUnsignedShort(constantPool.literalIndex(typeInitMethod(type)));
        } else {
            genLoadArg(argByteOffset);
            argByteOffset++;
        }
        return argByteOffset;
    }

    /**
     * 94 checkcast 69 java.lang.Boolean 97 invokevirtual 73
     * java.lang.Boolean.booleanValue()Z 100 ireturn or 52 checkcast 91
     * java.lang.String 55 areturn
     */
    private void genCastReturnType(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == void.class) {
                writeUnsignedByte(OPC_pop);
                writeUnsignedByte(OPC_return);
            } else {
                writeUnsignedByte(OPC_checkcast);
                writeUnsignedShort(constantPool
                        .typeIndex(typeWrapperName(type)));
                writeUnsignedByte(OPC_invokevirtual);
                writeUnsignedShort(constantPool
                        .literalIndex(typeAccessMethod(type)));
                if (type == long.class) {
                    writeUnsignedByte(OPC_lreturn);
                } else if (type == float.class) {
                    writeUnsignedByte(OPC_freturn);
                } else if (type == double.class) {
                    writeUnsignedByte(OPC_dreturn);
                } else { // handles int, short, byte, boolean & char
                    writeUnsignedByte(OPC_ireturn);
                }
            }
        } else {
            writeUnsignedByte(OPC_checkcast);
            writeUnsignedShort(constantPool.typeIndex(type.getName()));
            writeUnsignedByte(OPC_areturn);
        }
    }

    private void genCallClassForName(String typeName) {
        writeLdc(typeName);
        writeUnsignedByte(OPC_invokestatic);
        if (ClassForNameMethod == null) {
            try {
                ClassForNameMethod = Class.class.getMethod("forName",
                        new Class[] { String.class });
            } catch (NoSuchMethodException e) {
                throw new InternalError();
            }
        }
        writeUnsignedShort(constantPool.literalIndex(ClassForNameMethod));
    }

    private void genLoadArg(int argByteOffset) {
        if (argByteOffset > 255) {
            writeUnsignedByte(OPC_wide);
            writeUnsignedByte(OPC_aload);
            writeUnsignedShort(argByteOffset);
        } else {
            switch (argByteOffset) {
                case 0:
                    writeUnsignedByte(OPC_aload_0);
                    break;
                case 1:
                    writeUnsignedByte(OPC_aload_1);
                    break;
                case 2:
                    writeUnsignedByte(OPC_aload_2);
                    break;
                case 3:
                    writeUnsignedByte(OPC_aload_3);
                    break;
                default:
                    writeUnsignedByte(OPC_aload);
                    writeUnsignedByte(argByteOffset);
            }
        }
    }

    private void genStoreArg(int argByteOffset) {
        if (argByteOffset > 255) {
            writeUnsignedByte(OPC_wide);
            writeUnsignedByte(OPC_astore);
            writeUnsignedShort(argByteOffset);
        } else {
            switch (argByteOffset) {
                case 0:
                    writeUnsignedByte(OPC_astore_0);
                    break;
                case 1:
                    writeUnsignedByte(OPC_astore_1);
                    break;
                case 2:
                    writeUnsignedByte(OPC_astore_2);
                    break;
                case 3:
                    writeUnsignedByte(OPC_astore_3);
                    break;
                default:
                    writeUnsignedByte(OPC_astore);
                    writeUnsignedByte(argByteOffset);
            }
        }
    }

    private byte[] getBytes() {
        byte[] fullContents = new byte[headerOffset + contentsOffset];
        System.arraycopy(header, 0, fullContents, 0, headerOffset);
        System.arraycopy(contents, 0, fullContents, headerOffset,
                contentsOffset);
        return fullContents;
    }

    private Method typeAccessMethod(Class<?> baseType) {
        try {
            if (baseType == int.class) {
                return Integer.class.getMethod("intValue", (Class[]) null);
            }
            if (baseType == short.class) {
                return Short.class.getMethod("shortValue", (Class[]) null);
            }
            if (baseType == byte.class) {
                return Byte.class.getMethod("byteValue", (Class[]) null);
            }
            if (baseType == boolean.class) {
                return Boolean.class.getMethod("booleanValue", (Class[]) null);
            }
            if (baseType == char.class) {
                return Character.class.getMethod("charValue", (Class[]) null);
            }
            if (baseType == long.class) {
                return Long.class.getMethod("longValue", (Class[]) null);
            }
            if (baseType == float.class) {
                return Float.class.getMethod("floatValue", (Class[]) null);
            }
            if (baseType == double.class) {
                return Double.class.getMethod("doubleValue", (Class[]) null);
            }
        } catch (NoSuchMethodException e) {
            throw new InternalError();
        }
        return null;
    }

    private Field typeField(Class<?> baseType) {
        try {
            if (baseType == int.class) {
                return Integer.class.getField("TYPE");
            }
            if (baseType == short.class) {
                return Short.class.getField("TYPE");
            }
            if (baseType == byte.class) {
                return Byte.class.getField("TYPE");
            }
            if (baseType == boolean.class) {
                return Boolean.class.getField("TYPE");
            }
            if (baseType == char.class) {
                return Character.class.getField("TYPE");
            }
            if (baseType == long.class) {
                return Long.class.getField("TYPE");
            }
            if (baseType == float.class) {
                return Float.class.getField("TYPE");
            }
            if (baseType == double.class) {
                return Double.class.getField("TYPE");
            }
        } catch (NoSuchFieldException e) {
            throw new InternalError();
        }
        return null;
    }

    private Constructor<?> typeInitMethod(Class<?> baseType) {
        try {
            if (baseType == int.class) {
                return Integer.class.getConstructor(new Class[] { int.class });
            }
            if (baseType == short.class) {
                return Short.class.getConstructor(new Class[] { short.class });
            }
            if (baseType == byte.class) {
                return Byte.class.getConstructor(new Class[] { byte.class });
            }
            if (baseType == boolean.class) {
                return Boolean.class
                        .getConstructor(new Class[] { boolean.class });
            }
            if (baseType == char.class) {
                return Character.class
                        .getConstructor(new Class[] { char.class });
            }
            if (baseType == long.class) {
                return Long.class.getConstructor(new Class[] { long.class });
            }
            if (baseType == float.class) {
                return Float.class.getConstructor(new Class[] { float.class });
            }
            if (baseType == double.class) {
                return Double.class
                        .getConstructor(new Class[] { double.class });
            }
        } catch (NoSuchMethodException e) {
            throw new InternalError();
        }
        return null;
    }

    private String typeWrapperName(Class<?> baseType) {
        if (baseType == int.class) {
            return "java/lang/Integer";
        }
        if (baseType == short.class) {
            return "java/lang/Short";
        }
        if (baseType == byte.class) {
            return "java/lang/Byte";
        }
        if (baseType == boolean.class) {
            return "java/lang/Boolean";
        }
        if (baseType == char.class) {
            return "java/lang/Character";
        }
        if (baseType == long.class) {
            return "java/lang/Long";
        }
        if (baseType == float.class) {
            return "java/lang/Float";
        }
        if (baseType == double.class) {
            return "java/lang/Double";
        }
        return null;
    }

    private void writeIntConstant(int b) {
        switch (b) {
            case 0:
                writeUnsignedByte(OPC_iconst_0);
                break;
            case 1:
                writeUnsignedByte(OPC_iconst_1);
                break;
            case 2:
                writeUnsignedByte(OPC_iconst_2);
                break;
            case 3:
                writeUnsignedByte(OPC_iconst_3);
                break;
            case 4:
                writeUnsignedByte(OPC_iconst_4);
                break;
            case 5:
                writeUnsignedByte(OPC_iconst_5);
                break;
            default:
                writeUnsignedByte(OPC_bipush);
                writeUnsignedByte(b);
        }
    }

    private void writeLdc(String constant) {
        int index = constantPool.literalIndexForLdc(constant.toCharArray());
        if (index <= 0) {
            throw new InternalError();
        }
        if (index > 255) {
            writeUnsignedByte(OPC_ldc_w);
            writeUnsignedShort(index);
        } else {
            writeUnsignedByte(OPC_ldc);
            writeUnsignedByte(index);
        }
    }

    private void writeUnsignedByte(int b) {
        try {
            contents[contentsOffset++] = (byte) b;
        } catch (IndexOutOfBoundsException e) {
            int actualLength = contents.length;
            System.arraycopy(contents, 0, (contents = new byte[actualLength
                    + INCREMENT_SIZE]), 0, actualLength);
            contents[contentsOffset - 1] = (byte) b;
        }
    }

    private void writeUnsignedShort(int b) {
        writeUnsignedByte(b >>> 8);
        writeUnsignedByte(b);
    }

    private void writeUnsignedWord(int b) {
        writeUnsignedByte(b >>> 24);
        writeUnsignedByte(b >>> 16);
        writeUnsignedByte(b >>> 8);
        writeUnsignedByte(b);
    }
}
