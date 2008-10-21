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

package java.util;

/**
 * Comparator is used to compare two objects to determine their ordering in
 * respect to each other.
 * 
 * @since 1.2
 */
public interface Comparator<T> {
    /**
     * Compare the two objects to determine the relative ordering.
     * 
     * @param object1
     *            an Object to compare
     * @param object2
     *            an Object to compare
     * @return an int < 0 if object1 is less than object2, 0 if they are equal,
     *         and > 0 if object1 is greater
     * 
     * @exception ClassCastException
     *                when objects are not the correct type
     */
    public int compare(T object1, T object2);

    /**
     * Compares the argument to the receiver, and returns true if they represent
     * the <em>same</em> object using a class specific comparison.
     * 
     * @param object
     *            Object the object to compare with this object.
     * @return boolean <code>true</code> if the object is the same as this
     *         object <code>false</code> if it is different from this object.
     * @see Object#hashCode
     */
    public boolean equals(Object object);
}
