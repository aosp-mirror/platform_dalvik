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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang.reflect;

/**
 * Implementors of this interface model a class member.
 * 
 * @see Field
 * @see Constructor
 * @see Method
 */
public interface Member {

    public static final int PUBLIC = 0;

    public static final int DECLARED = 1;

    /**
     * Return the {@link Class} associated with the class that defined this
     * member.
     * 
     * @return the declaring class
     */
    @SuppressWarnings("unchecked")
    Class getDeclaringClass();

    /**
     * Return the modifiers for the member. The Modifier class should be used to
     * decode the result.
     * 
     * @return the modifiers
     * @see java.lang.reflect.Modifier
     */
    int getModifiers();

    /**
     * Return the name of the member.
     * 
     * @return the name
     */
    String getName();

    /**
     * Indicates whether or not this member is synthetic (artificially
     * introduced by the compiler).
     * 
     * @return A value of <code>true</code> if synthetic, otherwise
     *         <code>false</code>.
     */
    boolean isSynthetic();
}
