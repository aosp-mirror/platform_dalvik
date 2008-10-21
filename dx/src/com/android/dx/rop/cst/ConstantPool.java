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

package com.android.dx.rop.cst;

/**
 * Interface for constant pools, which are, more or less, just lists of
 * {@link Constant} objects.
 */
public interface ConstantPool {
    /**
     * Get the "size" of the constant pool. This corresponds to the
     * class file field <code>constant_pool_count</code>, and is in fact
     * always at least one more than the actual size of the constant pool,
     * as element <code>0</code> is always invalid.
     *
     * @return <code>&gt;= 1</code>; the size
     */
    public int size();

    /**
     * Get the <code>n</code>th entry in the constant pool, which must
     * be valid.
     *
     * @param n <code>n &gt;= 0, n &lt; size()</code>; the constant pool index
     * @return non-null; the corresponding entry
     * @throws IllegalArgumentException thrown if <code>n</code> is
     * in-range but invalid
     */
    public Constant get(int n);

    /**
     * Get the <code>n</code>th entry in the constant pool, which must
     * be valid unless <code>n == 0</code>, in which case <code>null</code>
     * is returned.
     *
     * @param n <code>n &gt;= 0, n &lt; size()</code>; the constant pool index
     * @return null-ok; the corresponding entry, if <code>n != 0</code>
     * @throws IllegalArgumentException thrown if <code>n</code> is
     * in-range and non-zero but invalid
     */
    public Constant get0Ok(int n);

    /**
     * Get the <code>n</code>th entry in the constant pool, or
     * <code>null</code> if the index is in-range but invalid. In
     * particular, <code>null</code> is returned for index <code>0</code>
     * as well as the index after any entry which is defined to take up
     * two slots (that is, <code>Long</code> and <code>Double</code>
     * entries).
     *
     * @param n <code>n &gt;= 0, n &lt; size()</code>; the constant pool index
     * @return null-ok; the corresponding entry, or <code>null</code> if
     * the index is in-range but invalid
     */
    public Constant getOrNull(int n);
}
