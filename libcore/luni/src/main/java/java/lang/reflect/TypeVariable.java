/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Represents a type variable.
 * 
 * @since 1.5
 */
public interface TypeVariable<D extends GenericDeclaration> extends Type {

    /**
     * Returns the upper bounds of the type variable.
     * 
     * @return array of type variable's upper bounds.
     * @throws TypeNotPresentException
     *             if the component type points to a missing type.
     * @throws MalformedParameterizedTypeException
     *             if the component type points to a type that can't be
     *             instantiated for some reason.
     */
    Type[] getBounds();

    /**
     * Returns a GenericDeclaration object for this type variable.
     * 
     * @return the generic declaration spec
     */
    D getGenericDeclaration();

    /**
     * Returns the type variable's name from source.
     * 
     * @return the variable's name from the source code.
     */
    String getName();
}
