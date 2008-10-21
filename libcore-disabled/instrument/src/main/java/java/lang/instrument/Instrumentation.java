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

/**
 * Instances of this interface may be used by Java instrumentation agent code
 * for support in carrying out the runtime instrumentation of classes. Using
 * such an approach, classes may be enhanced with services such as profiling,
 * logging or tracing which were not included in the source of the original
 * class.
 * <p>
 * A concrete instance of this interface is made available as an input argument
 * to all Java instrumentation agents'
 * <code>premain(String agentArgs, Instrumentation inst)</code> method.
 * </p>
 * 
 */
public interface Instrumentation {

    /**
     * Registers the supplied <code>transformer</code> argument with the VM.
     * Any classes that are to be defined or re-defined (if supported) in the VM
     * will then be offered to the transformer for it to carry out any byte code
     * modifications. The exception to this scheme is if the class to be defined /
     * re-defined is a dependency of the transformer.
     * <p>
     * This operation can be carried out multiple times on a concrete
     * <code>Instrumentation</code>. The order of registration is important
     * as it defines the order in which the transformers' transformation
     * operation gets called.
     * <p>
     * <p>
     * It is possible for any given instance of
     * <code>ClassFileTransformer</code> to be registered more than once with
     * this operation.
     * 
     * @param transformer
     *            a class file transformer
     * @throws NullPointerException
     *             if <code>transformer</code> is <code>null</code>.
     */
    public void addTransformer(ClassFileTransformer transformer);

    /**
     * Returns an array of all of the classes that have been loaded into the VM.
     * 
     * @return an array of <code>Class</code> objects with each element
     *         identifying a class that has been loaded into the VM.
     */
    public Class[] getAllLoadedClasses();

    /**
     * Returns an array of all of the classes for which <code>loader</code> is
     * the <i>initiating</i> class loader.
     * 
     * @param loader
     *            a class loader. In order to obtain the array of classes
     *            initiated by the bootstrap class loader this argument should
     *            be <code>null</code>.
     * @return an array of <code>Class</code> objects with each element
     *         identifying a class that has been initiated by the specified
     *         class loader.
     */
    public Class[] getInitiatedClasses(ClassLoader loader);

    /**
     * Returns the number of bytes in memory required by this VM for the
     * supplied object <code>objectToSize</code>. The returned value should
     * be taken as an estimation only which is susceptible to change between
     * separate launches of the VM.
     * 
     * @param objectToSize
     *            any object
     * @return an approximation of the number of bytes in memory taken up by
     *         <code>objectToSize</code>.
     * @throws NullPointerException
     *             if the given object is null.
     */
    public long getObjectSize(Object objectToSize);

    /**
     * Returns a boolean indication of whether or not this VM supports the
     * on-the-fly redefining of classes that have been already loaded.
     * 
     * @return <code>true</code> if class redefining is supported, otherwise
     *         <code>false</code>.
     */
    public boolean isRedefineClassesSupported();

    /**
     * Receives an array of {@link ClassDefinition} instances and attempts to
     * carry out on-the-fly redefining on each of the associated classes.
     * Redefining in this manner may be used to update the following parts of an
     * already loaded class:
     * <ul>
     * <li>attributes
     * <li>constant pool
     * <li>method implementations
     * </ul>
     * If any invocations of a redefined method are already active in the VM
     * when this call is made then they will run to completion and be unaffected
     * by the outcome of this method. Provided the method redefinition is
     * successful, all subsequent calls on the method will run the new version.
     * <br>
     * Redefining a class may <em>not</em> be used to make changes to any
     * other aspects of a previously loaded class such as its inheritance
     * hierarchy, the names or signatures of any of its methods, the names of
     * any fields, the values of any static variables etc.
     * <p>
     * If a class associated with a <code>ClassDefinition</code> is
     * successfully redefined then there will be no resulting re-run of any of
     * its initialization code. Similarly, any instances of the class that were
     * created before the redefining will not be changed in any way. That is,
     * they will remain in the VM as instances of the previous version of the
     * class.
     * </p>
     * <p>
     * Note that before the requested redefinitions are attempted, each
     * {@link ClassFileTransformer} registered with the VM will be given the
     * opportunity to carry out their own custom transformations of the new
     * version of the class.
     * </p>
     * 
     * @param definitions
     *            an array of <code>ClassDefinition</code> objects wrapping
     *            the details of the classes to be redefined. A zero-length
     *            array value will not cause an error but, instead, will
     *            silently do nothing.
     * @throws ClassNotFoundException
     *             if any of the classes specified in the contents of
     *             <code>definitions</code> cannot be located.
     * @throws UnmodifiableClassException
     *             if any of the classes specified in the contents of
     *             <code>definitions</code> cannot be modified.
     * @throws UnsupportedOperationException
     *             if this method is not supported in by the VM. May be checked
     *             in advance by calling {@link #isRedefineClassesSupported()}.
     * @throws ClassFormatError
     *             if any of the <code>definitions</code> elements has been
     *             created with a <code>byte</code> array containing a badly
     *             formed class file.
     * @throws NoClassDefFoundError
     *             if there is disagreement between the name of a class to be
     *             redefined and the name of the class from the corresponding
     *             class file format byte array.
     * @throws UnsupportedClassVersionError
     *             if the version of any of the classes to be redefined is not
     *             supported by the VM.
     * @throws ClassCircularityError
     *             if a circular dependency is detected among the classes to be
     *             redefined.
     * @throws LinkageError
     *             if a linkage error situation is detected such that there is
     *             an incompatability between dependent classes.
     * @throws NullPointerException
     *             if <code>definitions</code> or any of its elements are
     *             found to be <code>null</code>.
     * @see #isRedefineClassesSupported()
     */
    public void redefineClasses(ClassDefinition[] definitions)
            throws ClassNotFoundException, UnmodifiableClassException;

    /**
     * Removes <i>the most recently added instance of</i> the
     * <code>ClassFileTransformer</code> object from the VM's list of
     * registered transformers. After this call completes, the specified
     * <code>ClassFileTransformer</code> object will no longer have its
     * <code>transform()<code> method automatically invoked when class definitions or
     * redefinitions are attempted. 
     * 
     * @param transformer
     *            a previously registered <code>ClassFileTransformer</code>.
     * @return <code>true</code> if <code>transformer</code> was located in
     *         the list of registered transformers and successfully removed.
     *         Otherwise, <code>false</code>.
     * @throws NullPointerException
     *            if <code>transformer</code> is <code>null</code>.
     */
    public boolean removeTransformer(ClassFileTransformer transformer);
}
