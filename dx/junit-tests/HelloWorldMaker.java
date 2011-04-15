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

import com.android.dx.gen.BinaryOp;
import com.android.dx.gen.Code;
import com.android.dx.gen.DexGenerator;
import com.android.dx.gen.Field;
import com.android.dx.gen.Local;
import com.android.dx.gen.Method;
import com.android.dx.gen.Type;
import com.android.dx.rop.code.AccessFlags;
import java.io.PrintStream;

public class HelloWorldMaker {

    public static void main(String[] args) throws Exception {

        /*
         * This code generates Dalvik bytecode equivalent to the following
         * program.
         *
         *  public class HelloWorld {
         *      public static void hello() {
         *          int a = 0xabcd;
         *          int b = 0xaaaa;
         *          int c = a - b;
         *          String s = Integer.toHexString(c);
         *          System.out.println(s);
         *      }
         *  }
         */

        DexGenerator generator = new DexGenerator();

        // lookup the symbols of interest
        Type<Object> object = generator.getType(Object.class);
        Type<Integer> integer = generator.getType(Integer.class);
        Type<Integer> intType = generator.getType(int.class);
        Type<String> string = generator.getType(String.class);
        Type<Void> voidType = generator.getType(void.class);
        Type<System> system = generator.getType(System.class);
        Type<PrintStream> printStream = generator.getType(PrintStream.class);
        Type<?> helloWorld = generator.getType("LHelloWorld;");
        Field<System, PrintStream> systemOutField = system.getField(printStream, "out");
        Method<Integer, String> toHexString = integer.getMethod(string, "toHexString", intType);
        Method<PrintStream, Void> println = printStream.getMethod(voidType, "println", string);

        // create some registers
        //    (I'd like a better syntax for this)
        Code code = generator.newCode();
        Local<Integer> a = code.newLocal(intType);
        Local<Integer> b = code.newLocal(intType);
        Local<Integer> c = code.newLocal(intType);
        Local<String> s = code.newLocal(string);
        Local<PrintStream> localSystemOut = code.newLocal(printStream);

        // specify the code instruction-by-instruction (approximately)
        code.loadConstant(a, 0xabcd);
        code.loadConstant(b, 0xaaaa);
        code.op(BinaryOp.SUBTRACT, c, a, b);
        code.invokeStatic(toHexString, s, c);
        code.sget(systemOutField, localSystemOut);
        code.invokeVirtual(println, null, localSystemOut, s);
        code.returnVoid();

        // wrap it up by building the HelloWorld class and hello() method
        Method hello = helloWorld.getMethod(voidType, "hello");
        hello.declare(AccessFlags.ACC_STATIC | AccessFlags.ACC_PUBLIC, code);

        // TODO: create the constructor

        helloWorld.declare("Generated.java", AccessFlags.ACC_PUBLIC, object);

        // load the dex
        ClassLoader loader = generator.load(HelloWorldMaker.class.getClassLoader());
        Class<?> helloWorldClass = loader.loadClass("HelloWorld");
        helloWorldClass.getMethod("hello").invoke(null);
    }
}
