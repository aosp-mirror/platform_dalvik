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

package org.apache.harmony.luni.util;

import java.util.Arrays;

/**
 * Helper class with methods for sorting arrays.
 * @deprecated Use {@link Comparator} and {@link Arrays.sort()}
 */
@Deprecated
public final class Sorter {
    public interface Comparator<T> extends java.util.Comparator<T> {
    }

    /**
     * Sorts the array of objects using the default sorting algorithm.
     * 
     * @param objs
     *            array of objects to be sorted
     * @param comp
     *            A Comparator to be used to sort the elements
     * 
     */
    public static <T> void sort(T[] objs, Comparator<T> comp) {
        Arrays.sort(objs, comp);
    }

}
