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

package java.lang.reflect;

/**
 * Represents a parameterized type.
 * 
 * @since 1.5
 */
public interface ParameterizedType extends Type {

    /**
     * Gets the type arguments for this type.
     * 
     * @return An array of {@link Type}, which may be empty.
     * @throws TypeNotPresentException
     *             if one of the type arguments can't be found.
     * @throws MalformedParameterizedTypeException
     *             if one of the type arguments can't be instantiated for some
     *             reason.
     */
    Type[] getActualTypeArguments();

    /**
     * Gets the parent/owner type, if this type is an inner type, otherwise
     * <code>null</code> is returned if this is a top-level type.
     * 
     * @return An instance of {@link Type} or <code>null</code>.
     * @throws TypeNotPresentException
     *             if one of the type arguments can't be found.
     * @throws MalformedParameterizedTypeException
     *             if one of the type arguments can't be instantiated for some
     *             reason.
     */
    Type getOwnerType();

    /**
     * Gets the raw type of this type.
     * 
     * @return An instance of {@link Type}.
     */
    Type getRawType();
}
