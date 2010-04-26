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

package org.apache.harmony.archive.tests.java.util.jar;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.jar.Pack200;

public class Pack200Test extends TestCase {

    public void testNewPacker() {
        Method[] methodsInt = Pack200.Packer.class.getDeclaredMethods();
        Method[] methodsImpl = Pack200.newPacker().getClass()
                .getDeclaredMethods();
        Field[] fieldsInt = Pack200.Packer.class.getFields();
        Field[] fieldsImpl = Pack200.newPacker().getClass().getFields();
        int i, k;
        boolean flag;
        for (i = 0; i < methodsInt.length; i++) {
            flag = false;
            for (k = 0; k < methodsImpl.length; k++) {
                if (methodsInt[i].getName().equals(methodsImpl[k].getName())) {
                    flag = true;
                    break;
                }
            }
            assertTrue("Not all methods were implemented", flag);
        }

        for (i = 0; i < fieldsInt.length; i++) {
            flag = false;
            for (k = 0; k < fieldsImpl.length; k++) {
                if (fieldsInt[i].getName().equals(fieldsImpl[k].getName())) {
                    flag = true;
                    break;
                }
            }
            assertTrue("Not all fields were existed", flag);
        }
    }

    public void testNewUnpacker() {
        assertNotNull(Pack200.newUnpacker().getClass());
        Method[] methodsInt = Pack200.Unpacker.class.getDeclaredMethods();
        Method[] methodsImpl = Pack200.newUnpacker().getClass()
                .getDeclaredMethods();
        Field[] fieldsInt = Pack200.Unpacker.class.getFields();
        Field[] fieldsImpl = Pack200.newUnpacker().getClass().getFields();
        int i, k;
        boolean flag;
        for (i = 0; i < methodsInt.length; i++) {
            flag = false;
            for (k = 0; k < methodsImpl.length; k++) {
                if (methodsInt[i].getName().equals(methodsImpl[k].getName())) {
                    flag = true;
                    break;
                }
            }
            assertTrue("Not all methods were implemented", flag);
        }

        for (i = 0; i < fieldsInt.length; i++) {
            flag = false;
            for (k = 0; k < fieldsImpl.length; k++) {
                if (fieldsInt[i].getName().equals(fieldsImpl[k].getName())) {
                    flag = true;
                    break;
                }
            }
            assertTrue("Not all fields were existed", flag);
        }
    }

}
