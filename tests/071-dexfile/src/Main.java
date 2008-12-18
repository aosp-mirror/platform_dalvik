/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.io.File;
import java.lang.reflect.Constructor;

/**
 * DexFile tests (Dalvik-specific).
 */
public class Main {
    private static final String CLASS_PATH = "test-ex.jar";
    private static final String ODEX_DIR = "/sdcard";
    //private static final String ODEX_DIR = ".";
    private static final String ODEX_ALT = "/tmp";
    private static final String LIB_DIR = "/nowhere/nothing/";

    /**
     * Create a class loader, explicitly specifying the source DEX and
     * the location for the optimized DEX.
     */
    public static void main(String[] args) {
        ClassLoader dexClassLoader = getDexClassLoader();

        Class anotherClass;
        try {
            anotherClass = dexClassLoader.loadClass("Another");
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Another?");
        }

        Object another;
        try {
            another = anotherClass.newInstance();
        } catch (IllegalAccessException ie) {
            throw new RuntimeException("new another", ie);
        } catch (InstantiationException ie) {
            throw new RuntimeException("new another", ie);
        }

        /* not expected to work; just exercises the call */
        dexClassLoader.getResource("nonexistent");

        System.out.println("done");
    }

    /*
     * Create an instance of DexClassLoader.  The test harness doesn't
     * have visibility into dalvik.system.*, so we do this through
     * reflection.
     */
    private static ClassLoader getDexClassLoader() {
        String odexDir;

        /*
        String androidData = System.getenv("ANDROID_DATA");
        if (androidData == null)
            androidData = "";
        odexDir = androidData + "/" + ODEX_DIR;
        */

        File test = new File(ODEX_DIR);
        if (test.isDirectory())
            odexDir = ODEX_DIR;
        else
            odexDir = ODEX_ALT;
        //System.out.println("Output dir is " + odexDir);

        ClassLoader myLoader = Main.class.getClassLoader();
        Class dclClass;
        try {
            dclClass = myLoader.loadClass("dalvik.system.DexClassLoader");
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("dalvik.system.DexClassLoader not found");
        }

        Constructor ctor;
        try {
            ctor = dclClass.getConstructor(String.class, String.class,
                String.class, ClassLoader.class);
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException("DCL ctor", nsme);
        }

        Object dclObj;
        try {
            dclObj = ctor.newInstance(CLASS_PATH, odexDir, LIB_DIR, myLoader);
        } catch (Exception ex) {
            throw new RuntimeException("DCL newInstance", ex);
        }

        return (ClassLoader) dclObj;
    }
}

