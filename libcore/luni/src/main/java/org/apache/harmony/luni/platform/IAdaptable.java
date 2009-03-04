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
 * The interface to extensible objects.
 * <p>
 * Classes can implement this interface (a single method) to provide interfaces
 * that are not API -- each interface type has to be handled in the
 * implementation of <code>getAdapter(Class)</code>. This is a good way to
 * extend the class without breaking existing API.
 * </p>
 * <p>
 * In addition, classes can be augmented by interfaces that are defined by other
 * classes (which requires the <code>getAdapter(Class)</code> to be
 * implemented by a factory.
 * 
 */
public interface IAdaptable {

    /**
     * Returns the adapter corresponding to the given class.
     * <p>
     * The adapter is typically obtained using the class literal, like this:
     * 
     * <pre>
     *    ...
     *    IAdaptable = (IAdaptable) foo;
     *    IMyInterface bar = (IMyInterface)foo.getAdapter(IMyInterface.class);
     *    bar.doMyThing();
     *    ...
     * </pre>
     * 
     * @param adapter
     *            the type of adapter requested
     * @return the adapter
     */
    public Object getAdapter(Class adapter);
}
