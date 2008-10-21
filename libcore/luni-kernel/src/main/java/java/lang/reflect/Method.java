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

package java.lang.reflect;

// BEGIN android-added
import org.apache.harmony.kernel.vm.StringUtils;
import org.apache.harmony.luni.lang.reflect.GenericSignatureParser;
import org.apache.harmony.luni.lang.reflect.ListOfTypes;
import org.apache.harmony.luni.lang.reflect.Types;
// END android-added

import java.lang.annotation.Annotation;


/**
 * This class models a method. Information about the method can be accessed, and
 * the method can be invoked dynamically.
 */
public final class Method extends AccessibleObject implements GenericDeclaration, Member {
    
    private int slot;
    
    private Class<?> declaringClass;
    
    private String name;
    
    private Class<?>[] parameterTypes;
    
    private Class<?>[] exceptionTypes;
    
    private Class<?> returnType;

    // BEGIN android-added
    private ListOfTypes genericExceptionTypes;
    private ListOfTypes genericParameterTypes;
    private Type genericReturnType;
    private TypeVariable<Method>[] formalTypeParameters;
    private volatile boolean genericTypesAreInitialized = false;

    private synchronized void initGenericTypes() {
        if (!genericTypesAreInitialized) {
            String signatureAttribute = getSignatureAttribute();
            GenericSignatureParser parser = new GenericSignatureParser();
            parser.parseForMethod(this, signatureAttribute);
            formalTypeParameters = parser.formalTypeParameters;
            genericParameterTypes = parser.parameterTypes;
            genericExceptionTypes = parser.exceptionTypes;
            genericReturnType = parser.returnType;
            genericTypesAreInitialized = true;
        }
    }
    // END android-added
    
    // BEGIN android-removed
    /**
     * Prevent this class from being instantiated
     */
    //private Method(){
        //do nothing
    //}
    // END android-removed

    // BEGIN android-added
    /**
     * Construct a clone of the given instance.
     * 
     * @param orig non-null; the original instance to clone
     */
    /*package*/ Method(Method orig) {
        this(orig.declaringClass, orig.parameterTypes, orig.exceptionTypes,
                orig.returnType, orig.name, orig.slot);

        // Copy the accessible flag.
        if (orig.flag) {
            this.flag = true;
        }
    }
    // END android-added

    private Method(Class<?> declaring, Class<?>[] paramTypes, Class<?>[] exceptTypes, Class<?> returnType, String name, int slot)
    {
        this.declaringClass = declaring;
        this.name = name;
        this.slot = slot;
        this.parameterTypes = paramTypes;
        this.exceptionTypes = exceptTypes;      // may be null
        this.returnType = returnType;
    }

    public TypeVariable<Method>[] getTypeParameters() {
        // BEGIN android-changed
        initGenericTypes();
        return formalTypeParameters.clone();
        // END android-changed
    }

    // BEGIN android-changed

    /** {@inheritDoc} */
    @Override /*package*/ String getSignatureAttribute() {
        Object[] annotation = getSignatureAnnotation(declaringClass, slot);

        if (annotation == null) {
            return null;
        }

        return StringUtils.combineStrings(annotation);
    }
    
    /**
     * Gets the Signature annotation for this method. Returns null if
     * not found.
     */
    native private Object[] getSignatureAnnotation(Class declaringClass,
            int slot);

    // END android-changed

    /**
     * <p>
     * Returns the String representation of the method's declaration, including
     * the type parameters.
     * </p>
     * 
     * @return An instance of String.
     * @since 1.5
     */
    public String toGenericString() {
        // BEGIN android-changed
        StringBuilder sb = new StringBuilder(80);
        
        initGenericTypes();

        // append modifiers if any
        int modifier = getModifiers();
        if (modifier != 0) {
            sb.append(Modifier.toString(modifier & ~(Modifier.BRIDGE + 
                    Modifier.VARARGS))).append(' ');
        }
        // append type parameters
        if (formalTypeParameters != null && formalTypeParameters.length > 0) {
            sb.append('<');
            for (int i = 0; i < formalTypeParameters.length; i++) {
                appendGenericType(sb, formalTypeParameters[i]);
                if (i < formalTypeParameters.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("> ");
        }
        // append return type
        appendGenericType(sb, Types.getType(genericReturnType));
        sb.append(' ');
        // append method name
        appendArrayType(sb, getDeclaringClass());
        sb.append("."+getName());
        // append parameters
        sb.append('(');
        appendArrayGenericType(sb, 
                Types.getClonedTypeArray(genericParameterTypes));
        sb.append(')');
        // append exeptions if any
        Type[] genericExceptionTypeArray = Types.getClonedTypeArray(
                genericExceptionTypes);
        if (genericExceptionTypeArray.length > 0) {
            sb.append(" throws ");
            appendArrayGenericType(sb, genericExceptionTypeArray);
        }
        return sb.toString();
        // END android-changed
    }

    /**
     * <p>
     * Gets the parameter types as an array of {@link Type} instances, in
     * declaration order. If the method has no parameters, then an empty array
     * is returned.
     * </p>
     * 
     * @return An array of {@link Type} instances.
     * @throws GenericSignatureFormatError if the generic method signature is
     *         invalid.
     * @throws TypeNotPresentException if the component type points to a missing
     *         type.
     * @throws MalformedParameterizedTypeException if the component type points
     *         to a type that can't be instantiated for some reason.
     * @since 1.5
     */
    public Type[] getGenericParameterTypes() {
        // BEGIN android-changed
        initGenericTypes();
        return Types.getClonedTypeArray(genericParameterTypes);
        // END android-changed
    }

    /**
     * <p>
     * Gets the exception types as an array of {@link Type} instances. If the
     * method has no declared exceptions, then an empty array is returned.
     * </p>
     * 
     * @return An array of {@link Type} instances.
     * @throws GenericSignatureFormatError if the generic method signature is
     *         invalid.
     * @throws TypeNotPresentException if the component type points to a missing
     *         type.
     * @throws MalformedParameterizedTypeException if the component type points
     *         to a type that can't be instantiated for some reason.
     * @since 1.5
     */
    public Type[] getGenericExceptionTypes() {
        // BEGIN android-changed
        initGenericTypes();
        return Types.getClonedTypeArray(genericExceptionTypes);
        // END android-changed
    }

    /**
     * <p>
     * Gets the return type as a {@link Type} instance.
     * </p>
     * 
     * @return A {@link Type} instance.
     * @throws GenericSignatureFormatError if the generic method signature is
     *         invalid.
     * @throws TypeNotPresentException if the component type points to a missing
     *         type.
     * @throws MalformedParameterizedTypeException if the component type points
     *         to a type that can't be instantiated for some reason.
     * @since 1.5
     */
    public Type getGenericReturnType() {
        // BEGIN android-changed
        initGenericTypes();
        return Types.getType(genericReturnType);
        // END android-changed
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getDeclaredAnnotations(declaringClass, slot);
    }
    native private Annotation[] getDeclaredAnnotations(Class declaringClass,
        int slot);

// BEGIN android-added
    private static final Annotation[] NO_ANNOTATIONS = new Annotation[0];

    /**
     * Creates an array of empty Annotation arrays.
     */
    /*package*/ static Annotation[][] noAnnotations(int size) {
        Annotation[][] annotations = new Annotation[size][];
        for (int i = 0; i < size; i++) {
            annotations[i] = NO_ANNOTATIONS;
        }
        return annotations;
    }
// END android-added

    /**
     * <p>
     * Gets an array of arrays that represent the annotations of the formal
     * parameters of this method. If there are no parameters on this method,
     * then an empty array is returned. If there are no annotations set, then
     * and array of empty arrays is returned.
     * </p>
     * 
     * @return An array of arrays of {@link Annotation} instances.
     * @since 1.5
     */
    public Annotation[][] getParameterAnnotations() {
// BEGIN android-changed
        Annotation[][] parameterAnnotations
                = getParameterAnnotations(declaringClass, slot);
        if (parameterAnnotations.length == 0) {
            return noAnnotations(parameterTypes.length);
        }
// END android-changed
        return parameterAnnotations;
    }

    native private Annotation[][] getParameterAnnotations(Class declaringClass,
        int slot);

    /**
     * <p>
     * Indicates whether or not this method takes a variable number argument.
     * </p>
     * 
     * @return A value of <code>true</code> if a vararg is declare, otherwise
     *         <code>false</code>.
     * @since 1.5
     */
    public boolean isVarArgs() {
        int modifiers = getMethodModifiers(declaringClass, slot);
        return (modifiers & Modifier.VARARGS) != 0;
    }

    /**
     * <p>
     * Indicates whether or not this method is a bridge.
     * </p>
     * 
     * @return A value of <code>true</code> if this method's a bridge,
     *         otherwise <code>false</code>.
     * @since 1.5
     */
    public boolean isBridge() {
        int modifiers = getMethodModifiers(declaringClass, slot);
        return (modifiers & Modifier.BRIDGE) != 0;
    }

    /**
     * <p>
     * Indicates whether or not this method is synthetic.
     * </p>
     * 
     * @return A value of <code>true</code> if this method is synthetic,
     *         otherwise <code>false</code>.
     * @since 1.5
     */
    public boolean isSynthetic() {
        int modifiers = getMethodModifiers(declaringClass, slot);
        return (modifiers & Modifier.SYNTHETIC) != 0;
    }
    
    /**
     * <p>Gets the default value for the annotation member represented by
     * this method.</p>
     * @return The default value or <code>null</code> if none.
     * @throws TypeNotPresentException if the annotation is of type {@link Class}
     * and no definition can be found.
     * @since 1.5
     */
    public Object getDefaultValue() {
        return getDefaultValue(declaringClass, slot);
    }
    native private Object getDefaultValue(Class declaringClass, int slot);

    /**
     * Compares the specified object to this Method and determines if they are
     * equal. The object must be an instance of Method with the same defining
     * class and parameter types.
     * 
     * @param object
     *            the object to compare
     * @return true if the specified object is equal to this Method, false
     *         otherwise
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        return object instanceof Method && toString().equals(object.toString());
    }

    /**
     * Return the {@link Class} associated with the class that defined this
     * method.
     * 
     * @return the declaring class
     */
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Return an array of the {@link Class} objects associated with the
     * exceptions declared to be thrown by this method. If the method was not
     * declared to throw any exceptions, the array returned will be empty.
     * 
     * @return the declared exception classes
     */
    public Class<?>[] getExceptionTypes() {
        if (exceptionTypes == null) {
            return new Class[0];
        }

        return exceptionTypes;
    }

    /**
     * Return the modifiers for the modeled method. The Modifier class
     * should be used to decode the result.
     * 
     * @return the modifiers
     * @see java.lang.reflect.Modifier
     */
    public int getModifiers() {
        return getMethodModifiers(declaringClass, slot);
    }

    private native int getMethodModifiers(Class<?> decl_class, int slot);
    
    /**
     * Return the name of the modeled method.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Return an array of the {@link Class} objects associated with the
     * parameter types of this method. If the method was declared with no
     * parameters, the array returned will be empty.
     * 
     * @return the parameter types
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Return the {@link Class} associated with the return type of this
     * method.
     * 
     * @return the return type
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * return the same value for this method. The hash code for a Method is the
     * hash code of the method's name.
     * 
     * @return the receiver's hash
     * @see #equals
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Return the result of dynamically invoking the modeled method. This
     * reproduces the effect of
     * <code>receiver.methodName(arg1, arg2, ... , argN)</code> This method
     * performs the following:
     * <ul>
     * <li>If the modeled method is static, the receiver argument is ignored.
     * </li>
     * <li>Otherwise, if the receiver is null, a NullPointerException is
     * thrown.</li>
     * If the receiver is not an instance of the declaring class of the method,
     * an IllegalArgumentException is thrown.
     * <li>If this Method object is enforcing access control (see
     * AccessibleObject) and the modeled method is not accessible from the
     * current context, an IllegalAccessException is thrown.</li>
     * <li>If the number of arguments passed and the number of parameters do
     * not match, an IllegalArgumentException is thrown.</li>
     * <li>For each argument passed:
     * <ul>
     * <li>If the corresponding parameter type is a base type, the argument is
     * unwrapped. If the unwrapping fails, an IllegalArgumentException is
     * thrown.</li>
     * <li>If the resulting argument cannot be converted to the parameter type
     * via a widening conversion, an IllegalArgumentException is thrown.</li>
     * </ul>
     * <li>If the modeled method is static, it is invoked directly. If it is
     * non-static, the modeled method and the receiver are then used to perform
     * a standard dynamic method lookup. The resulting method is then invoked.
     * </li>
     * <li>If an exception is thrown during the invocation it is caught and
     * wrapped in an InvocationTargetException. This exception is then thrown.
     * </li>
     * <li>If the invocation completes normally, the return value is itself
     * returned. If the method is declared to return a base type, the return
     * value is first wrapped. If the return type is void, null is returned.
     * </li>
     * </ul>
     * 
     * @param receiver
     *            The object on which to call the modeled method
     * @param args
     *            the arguments to the method
     * @return the new, initialized, object
     * @throws java.lang.NullPointerException
     *                if the receiver is null for a non-static method
     * @throws java.lang.IllegalAccessException
     *                if the modeled method is not accessible
     * @throws java.lang.IllegalArgumentException
     *                if an incorrect number of arguments are passed, the
     *                receiver is incompatible with the declaring class, or an
     *                argument could not be converted by a widening conversion
     * @throws java.lang.reflect.InvocationTargetException
     *                if an exception was thrown by the invoked method
     * @see java.lang.reflect.AccessibleObject
     */
    public Object invoke(Object receiver, Object... args)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        if (args == null) {
            args = new Object[0];
        }

        return invokeNative (receiver, args, declaringClass, parameterTypes, returnType, slot, flag);
    }

    private native Object invokeNative(Object obj, Object[] args, Class<?> declaringClass, Class<?>[] parameterTYpes, Class<?> returnType, int slot, boolean noAccessCheck)
    throws IllegalAccessException,
             IllegalArgumentException,
             InvocationTargetException;
    
    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver. The format of the string is modifiers (if any) return type
     * declaring class name '.' method name '(' parameter types, separated by
     * ',' ')' If the method throws exceptions, ' throws ' exception types,
     * separated by ',' For example:
     * <code>public native Object java.lang.Method.invoke(Object,Object) throws IllegalAccessException,IllegalArgumentException,InvocationTargetException</code>
     * 
     * @return a printable representation for the receiver
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(Modifier.toString(getModifiers()));

        if (result.length() != 0)       // android-changed
            result.append(' ');
        result.append(returnType.getName());
        result.append(' ');
        result.append(declaringClass.getName());
        result.append('.');
        result.append(name);
        result.append("(");
        result.append(toString(parameterTypes));
        result.append(")");
        if (exceptionTypes != null && exceptionTypes.length != 0) {
            result.append(" throws ");
            result.append(toString(exceptionTypes));
        }
        
        return result.toString();
    }
    
    /**
     * Returns the constructor's signature in non-printable form. This is called
     * (only) from IO native code and needed for deriving the serialVersionUID
     * of the class
     * 
     * @return The constructor's signature.
     */
    @SuppressWarnings("unused")
    private String getSignature() {
        StringBuilder result = new StringBuilder();
        
        result.append('(');
        for(int i = 0; i < parameterTypes.length; i++) {            
            result.append(getSignature(parameterTypes[i]));
        }
        result.append(')');
        result.append(getSignature(returnType));
        
        return result.toString();
    }
    
}
