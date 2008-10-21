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

package com.android.dx.cf.iface;

import com.android.dx.rop.cst.ConstantPool;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.cst.CstUtf8;
import com.android.dx.rop.type.TypeList;

/**
 * Interface for things which purport to be class files or reasonable
 * facsimiles thereof.
 *
 * <p><b>Note:</b> The fields referred to in this documentation are of the
 * <code>ClassFile</code> structure defined in vmspec-2 sec4.1.
 */
public interface ClassFile {
    /**
     * Gets the field <code>magic</code>.
     *
     * @return the value in question
     */
    public int getMagic();

    /**
     * Gets the field <code>minor_version</code>.
     *
     * @return the value in question
     */
    public int getMinorVersion();

    /**
     * Gets the field <code>major_version</code>.
     *
     * @return the value in question
     */
    public int getMajorVersion();

    /**
     * Gets the field <code>access_flags</code>.
     *
     * @return the value in question
     */
    public int getAccessFlags();

    /**
     * Gets the field <code>this_class</code>, interpreted as a type constant.
     *
     * @return non-null; the value in question
     */
    public CstType getThisClass();

    /**
     * Gets the field <code>super_class</code>, interpreted as a type constant
     * if non-zero.
     *
     * @return null-ok; the value in question
     */
    public CstType getSuperclass();

    /**
     * Gets the field <code>constant_pool</code> (along with
     * <code>constant_pool_count</code>).
     *
     * @return non-null; the constant pool
     */
    public ConstantPool getConstantPool();

    /**
     * Gets the field <code>interfaces<code> (along with 
     * interfaces_count</code>).
     *
     * @return non-null; the list of interfaces
     */
    public TypeList getInterfaces();

    /**
     * Gets the field <code>fields</code> (along with
     * <code>fields_count</code>).
     *
     * @return non-null; the list of fields
     */
    public FieldList getFields();

    /**
     * Gets the field <code>methods</code> (along with
     * <code>methods_count</code>).
     *
     * @return non-null; the list of fields
     */
    public MethodList getMethods();

    /**
     * Gets the field <code>attributes</code> (along with
     * <code>attributes_count</code>).
     *
     * @return non-null; the list of attributes
     */
    public AttributeList getAttributes();

    /**
     * Gets the name out of the <code>SourceFile</code> attribute of this
     * file, if any. This is a convenient shorthand for scrounging around
     * the class's attributes.
     *
     * @return non-null; the constant pool
     */
    public CstUtf8 getSourceFile();
}
