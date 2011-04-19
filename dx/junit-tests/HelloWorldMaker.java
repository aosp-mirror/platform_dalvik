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
import com.android.dx.gen.FieldId;
import com.android.dx.gen.Local;
import com.android.dx.gen.MethodId;
import com.android.dx.gen.Type;
import com.android.dx.rop.code.AccessFlags;
import java.io.PrintStream;

public class HelloWorldMaker {
    private static final Type<PrintStream> PRINT_STREAM = Type.get(PrintStream.class);
    private static final FieldId<System, PrintStream> SYSTEM_OUT
            = Type.get(System.class).getField(PRINT_STREAM, "out");
    private static final MethodId<Integer, String> TO_HEX_STRING
            = Type.get(Integer.class).getMethod(Type.STRING, "toHexString", Type.INT);
    private static final MethodId<PrintStream, Void> PRINTLN
            = PRINT_STREAM.getMethod(Type.VOID, "println", Type.STRING);

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
        Type<?> helloWorld = Type.get("LHelloWorld;");
        MethodId hello = helloWorld.getMethod(Type.VOID, "hello");

        // create some registers
        //    (I'd like a better syntax for this)
        Code code = generator.declare(hello, AccessFlags.ACC_STATIC | AccessFlags.ACC_PUBLIC);
        Local<Integer> a = code.newLocal(Type.INT);
        Local<Integer> b = code.newLocal(Type.INT);
        Local<Integer> c = code.newLocal(Type.INT);
        Local<String> s = code.newLocal(Type.STRING);
        Local<PrintStream> localSystemOut = code.newLocal(PRINT_STREAM);

        // specify the code instruction-by-instruction (approximately)
        code.loadConstant(a, 0xabcd);
        code.loadConstant(b, 0xaaaa);
        code.op(BinaryOp.SUBTRACT, c, a, b);
        code.invokeStatic(TO_HEX_STRING, s, c);
        code.sget(SYSTEM_OUT, localSystemOut);
        code.invokeVirtual(PRINTLN, null, localSystemOut, s);
        code.returnVoid();

        // TODO: create the constructor

        generator.declare(helloWorld, "Generated.java", AccessFlags.ACC_PUBLIC, Type.OBJECT);

        // load the dex
        ClassLoader loader = generator.load(HelloWorldMaker.class.getClassLoader());
        Class<?> helloWorldClass = loader.loadClass("HelloWorld");
        helloWorldClass.getMethod("hello").invoke(null);
    }
}
