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

package com.android.dx.rop.code;

import com.android.dx.rop.type.StdTypeList;
import com.android.dx.rop.type.Type;

/**
 * Common exception types.
 */
public final class Exceptions {
    /** non-null; the type <code>java.lang.ArithmeticException</code> */
    public static final Type TYPE_ArithmeticException =
        Type.intern("Ljava/lang/ArithmeticException;");

    /**
     * non-null; the type
     * <code>java.lang.ArrayIndexOutOfBoundsException</code> 
     */
    public static final Type TYPE_ArrayIndexOutOfBoundsException =
        Type.intern("Ljava/lang/ArrayIndexOutOfBoundsException;");

    /** non-null; the type <code>java.lang.ArrayStoreException</code> */
    public static final Type TYPE_ArrayStoreException =
        Type.intern("Ljava/lang/ArrayStoreException;");

    /** non-null; the type <code>java.lang.ClassCastException</code> */
    public static final Type TYPE_ClassCastException =
        Type.intern("Ljava/lang/ClassCastException;");

    /** non-null; the type <code>java.lang.Error</code> */
    public static final Type TYPE_Error = Type.intern("Ljava/lang/Error;");

    /**
     * non-null; the type
     * <code>java.lang.IllegalMonitorStateException</code> 
     */
    public static final Type TYPE_IllegalMonitorStateException =
        Type.intern("Ljava/lang/IllegalMonitorStateException;");

    /** non-null; the type <code>java.lang.NegativeArraySizeException</code> */
    public static final Type TYPE_NegativeArraySizeException =
        Type.intern("Ljava/lang/NegativeArraySizeException;");

    /** non-null; the type <code>java.lang.NullPointerException</code> */
    public static final Type TYPE_NullPointerException =
        Type.intern("Ljava/lang/NullPointerException;");

    /** non-null; the list <code>[java.lang.Error]</code> */
    public static final StdTypeList LIST_Error = StdTypeList.make(TYPE_Error);

    /**
     * non-null; the list <code>[java.lang.Error,
     * java.lang.ArithmeticException]</code> 
     */
    public static final StdTypeList LIST_Error_ArithmeticException =
        StdTypeList.make(TYPE_Error, TYPE_ArithmeticException);

    /**
     * non-null; the list <code>[java.lang.Error,
     * java.lang.ClassCastException]</code> 
     */
    public static final StdTypeList LIST_Error_ClassCastException =
        StdTypeList.make(TYPE_Error, TYPE_ClassCastException);

    /**
     * non-null; the list <code>[java.lang.Error,
     * java.lang.NegativeArraySizeException]</code> 
     */
    public static final StdTypeList LIST_Error_NegativeArraySizeException =
        StdTypeList.make(TYPE_Error, TYPE_NegativeArraySizeException);

    /**
     * non-null; the list <code>[java.lang.Error,
     * java.lang.NullPointerException]</code> 
     */
    public static final StdTypeList LIST_Error_NullPointerException =
        StdTypeList.make(TYPE_Error, TYPE_NullPointerException);

    /**
     * non-null; the list <code>[java.lang.Error,
     * java.lang.NullPointerException,
     * java.lang.ArrayIndexOutOfBoundsException]</code> 
     */
    public static final StdTypeList LIST_Error_Null_ArrayIndexOutOfBounds =
        StdTypeList.make(TYPE_Error,
                      TYPE_NullPointerException,
                      TYPE_ArrayIndexOutOfBoundsException);

    /**
     * non-null; the list <code>[java.lang.Error,
     * java.lang.NullPointerException,
     * java.lang.ArrayIndexOutOfBoundsException,
     * java.lang.ArrayStoreException]</code> 
     */
    public static final StdTypeList LIST_Error_Null_ArrayIndex_ArrayStore =
        StdTypeList.make(TYPE_Error,
                      TYPE_NullPointerException,
                      TYPE_ArrayIndexOutOfBoundsException,
                      TYPE_ArrayStoreException);

    /**
     * non-null; the list <code>[java.lang.Error,
     * java.lang.NullPointerException,
     * java.lang.IllegalMonitorStateException]</code> 
     */
    public static final StdTypeList
        LIST_Error_Null_IllegalMonitorStateException =
        StdTypeList.make(TYPE_Error,
                      TYPE_NullPointerException,
                      TYPE_IllegalMonitorStateException);

    /**
     * This class is uninstantiable.
     */
    private Exceptions() {
        // This space intentionally left blank.
    }
}
