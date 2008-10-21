/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.lang.instrument;

import java.security.ProtectionDomain;

/**
 * This interface must be implemented by types used to instrument classes as
 * they are loaded by a running VM. Implementations are registered by agents in
 * the {@link java.lang.instrument.Instrumentation#addTransformer} operation.
 * Once registered, a <code>ClassFileTransformer</code> has the opportunity to
 * instrument every class that is loaded or redefined by the VM provided that
 * the transformer does not have a dependency on that class.
 * <p>
 * Transformations of classes takes place just prior to them being defined by
 * the VM.
 * </p>
 * 
 */
public interface ClassFileTransformer {

    /**
     * Receives a <code>byte</code> array containing the raw contents of a
     * class for <i>possible</i> transformation into a new <code>byte</code>
     * array which gets returned to the caller. It is left up to the
     * implementation to decide what, if any, transformations are carried out
     * and returned.
     * <p>
     * Requests for class transformations can occur in two situations.
     * <ul>
     * <li>the attempted defining of a class using
     * {@link ClassLoader#defineClass(java.lang.String, byte[], int, int)}
     * <li>the attempted re-defining of a previously defined class using
     * {@link Instrumentation#redefineClasses(ClassDefinition[])}
     * </ul>
     * In both cases this operation will be called before the verification of
     * the specified bytes in the <code>Class</code> file format. Each
     * registered <code>ClassFileTransformer</code> instance will have this
     * operation called on it. The order of the invocations matches the order in
     * which the transformers were registered using the method
     * {@link Instrumentation#addTransformer(ClassFileTransformer)}.
     * </p>
     * <p>
     * Provided that the implementation of this method wishes to carry out a
     * transformation, the return is a newly allocated <code>byte</code> array
     * which contains <i>a copy of</i> the <code>classfileBuffer</code>
     * argument plus the transformations to the array specific to the method
     * implementation. If the transformer is written so as to pass on the
     * opportunity to modify a given input then the return value should be
     * <code>null</code>.
     * </p>
     * 
     * @param loader
     *            the <i>defining</i> <code>ClassLoader</code> for the
     *            candidate class to be transformed.
     * @param className
     *            the fully qualified name of the candidate class to be
     *            transformed in the <i>fully/qualified/Name</i> format.
     * @param classBeingRedefined
     *            if a class redefinition is in process then this argument will
     *            be the <code>Class</code> object for the class. Otherwise,
     *            if a class definition is in process, a <code>null</code>.
     * @param protectionDomain
     *            the security protection domain for the class being defined or
     *            redefined.
     * @param classfileBuffer
     *            a <code>byte</code> array containing the class to be
     *            transformed in <code>Class</code> file format.
     *            <em>This argument
     *            must not be modified</em>.
     * @return if transformation occurs, a newly allocated <code>byte</code>
     *         array containing the modified version of
     *         <code>classfileBuffer</code>, otherwise <code>null</code>.
     * @throws IllegalClassFormatException
     *             if the <code>classfileBuffer</code> does not contain a
     *             well-formed representation of a class in the
     *             <code>Class</code> file format. Note that if an invocation
     *             of this operation ends on an exception throw then (a) the
     *             remaining transformers in the &quot;chain&quot; will still
     *             have this method called, and (b) the class definition or
     *             redefinition that was the catalyst for the transformation
     *             opportunities will still be attempted.
     */
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException;
}
