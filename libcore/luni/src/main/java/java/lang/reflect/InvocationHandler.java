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
 * Implementors of this interface decode and dispatch methods sent to proxy
 * instances.
 * 
 * @see Proxy
 */
public interface InvocationHandler {

    /**
     * Return the result of decoding and dispatching the method which was
     * originally sent to the proxy instance.
     * 
     * @param proxy
     *            the proxy instance which was the receiver of the method.
     * @param method
     *            the Method invoked on the proxy instance.
     * @param args
     *            an array of objects containing the parameters passed to the
     *            method, or null if no arguments are expected. primitive types
     *            are wrapped in the appropriate class.
     * @return the result of executing the method
     * 
     * @throws Throwable
     *             if an exception was thrown by the invoked method. The
     *             exception must match one of the declared exception types for
     *             the invoked method or any unchecked exception type. If not
     *             then an UndeclaredThrowableException is thrown.
     */
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable;
}
