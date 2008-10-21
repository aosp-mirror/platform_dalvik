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

package java.lang;


/**
 * This interface should be implemented by all classes which wish to define a
 * <em>natural ordering</em> of their instances. The ordering rule must be
 * transitive and invertable (i.e. the sign of the result of x.compareTo(y) must
 * equal the negation of the sign of the result of y.compareTo(x) for all x and
 * y).
 * <p>
 * In addition, it is desireable (but not required) that when the result of
 * x.compareTo(y) is zero (and only then) the result of x.equals(y) should be
 * true.
 * 
 */
public interface Comparable<T> {
    
    /**
     * Returns an integer indicating the relative positions of the receiver and
     * the argument in the natural order of elements of the receiver's class.
     * 
     * 
     * @return int which should be <0 if the receiver should sort before the
     *         argument, 0 if the receiver should sort in the same position as
     *         the argument, and >0 if the receiver should sort after the
     *         argument.
     * @param another
     *            Object an object to compare the receiver to
     * @throws ClassCastException
     *             if the argument can not be converted into something
     *             comparable with the receiver.
     */
    int compareTo(T another);
}
