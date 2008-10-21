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

package org.apache.harmony.luni.platform;


/**
 * IAdapterFactory
 * 
 */
public interface IAdapterFactory {

    /**
     * Answer the adapter of the given type for the given object.
     * 
     * @param adaptableObject
     *            the object that implements IAdaptable.
     * @param adapterType
     *            the type of adapter to return.
     * @return the adapter of the requested type.
     */
    public Object getAdapter(IAdaptable adaptableObject, Class adapterType);

    /**
     * Returns the adapters that this factory provides.
     * 
     * @return the list of adapters as an array of classes.
     */
    public Class[] getAdapterList();
}
