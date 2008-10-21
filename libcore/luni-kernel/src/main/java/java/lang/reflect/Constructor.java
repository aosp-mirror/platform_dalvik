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
//END android-added

import java.lang.annotation.Annotation;

/**
 * This class models a constructor. Information about the constructor can be
 * accessed, and the constructor can be invoked dynamically.
 * 
 */
public final class Constructor<T> extends AccessibleObject implements GenericDeclaration,
        Member {
   
    Class<T> declaringClass;
    
    Class<?>[] parameterTypes;
    
    Class<?>[] exceptionTypes;
    
    // BEGIN android-added
    ListOfTypes genericExceptionTypes;
    ListOfTypes genericParameterTypes;
    TypeVariable<Constructor<T>>[] formalTypeParameters;
    private volatile boolean genericTypesAreInitialized = false;

    private synchronized void initGenericTypes() {
        if (!genericTypesAreInitialized) {
            String signatureAttribute = getSignatureAttribute();
            GenericSignatureParser parser = new GenericSignatureParser();
            parser.parseForConstructor(this, signatureAttribute);
            formalTypeParameters = parser.formalTypeParameters;
            genericParameterTypes = parser.parameterTypes;
            genericExceptionTypes = parser.exceptionTypes;
            genericTypesAreInitialized = true;
        }
    }
    // END android-added

    int slot;

    /**
     * Prevent this class from being instantiated
     */
    private Constructor(){
        //do nothing
    }

    /**
     * Creates an instance of the class. Only called from native code, thus
     * private.
     * 
     * @param declaringClass The class this constructor object belongs to.
     * @param ptypes The parameter types of the constructor.
     * @param extypes The exception types of the constructor.
     * @param slot The slot of the constructor inside the VM class structure.
     */
    private Constructor (Class<T> declaringClass, Class<?>[] ptypes, Class<?>[] extypes, int slot)
    {
        this.declaringClass = declaringClass;
        this.parameterTypes = ptypes;
        this.exceptionTypes = extypes;          // may be null
        this.slot = slot;
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
     * Get the Signature annotation for this constructor.  Returns null if not
     * found.
     */
    native private Object[] getSignatureAnnotation(Class declaringClass,
            int slot);

    // END android-changed

    /**
     * Returns an array of generic type variables used in this constructor.
     * 
     * @return The array of type parameters.
     */
    public TypeVariable<Constructor<T>>[] getTypeParameters() {
        // BEGIN android-changed
        initGenericTypes();
        return formalTypeParameters.clone();
        // END android-changed
    }

    /**
     * <p>
     * Returns the String representation of the constructor's declaration,
     * including the type parameters.
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
            sb.append(Modifier.toString(modifier & ~Modifier.VARARGS)).append(' ');
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
        // append constructor name
        appendArrayType(sb, getDeclaringClass());
        // append parameters
        sb.append('(');
        appendArrayGenericType(sb, 
                Types.getClonedTypeArray(genericParameterTypes));
        sb.append(')');
        // append exeptions if any
        Type[] genericEceptionTypeArray = 
                Types.getClonedTypeArray(genericExceptionTypes);
        if (genericEceptionTypeArray.length > 0) {
            sb.append(" throws ");
            appendArrayGenericType(sb, genericEceptionTypeArray);
        }
        return sb.toString();
        // END android-changed
    }

    /**
     * <p>
     * Gets the parameter types as an array of {@link Type} instances, in
     * declaration order. If the constructor has no parameters, then an empty
     * array is returned.
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
     * constructor has no declared exceptions, then an empty array is returned.
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

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getDeclaredAnnotations(declaringClass, slot);
    }
    native private Annotation[] getDeclaredAnnotations(Class declaringClass,
        int slot);

    /**
     * <p>
     * Gets an array of arrays that represent the annotations of the formal
     * parameters of this constructor. If there are no parameters on this
     * constructor, then an empty array is returned. If there are no annotations
     * set, then and array of empty arrays is returned.
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
            return Method.noAnnotations(parameterTypes.length);
        }
// END android-changed
        return parameterAnnotations;
    }
    native private Annotation[][] getParameterAnnotations(Class declaringClass,
        int slot);

    /**
     * <p>
     * Indicates whether or not this constructor takes a variable number
     * argument.
     * </p>
     * 
     * @return A value of <code>true</code> if a vararg is declare, otherwise
     *         <code>false</code>.
     * @since 1.5
     */
    public boolean isVarArgs() {
        int mods = getConstructorModifiers(declaringClass, slot);
        return (mods & Modifier.VARARGS) != 0;
    }

    /**
     * <p>
     * Indicates whether or not this constructor is synthetic.
     * </p>
     * 
     * @return A value of <code>true</code> if it is synthetic, or
     *         <code>false</code> otherwise.
     *         
     * @since 1.5
     */
    public boolean isSynthetic() {
        int mods = getConstructorModifiers(declaringClass, slot);
        return (mods & Modifier.SYNTHETIC) != 0;
    }

    /**
     * Compares the specified object to this Constructor and answer if they are
     * equal. The object must be an instance of Constructor with the same
     * defining class and parameter types.
     * 
     * @param object the object to compare
     * @return true if the specified object is equal to this Constructor, false
     *         otherwise
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        return object instanceof Constructor && toString().equals(object.toString());
    }

    /**
     * Return the {@link Class} associated with the class that defined this
     * constructor.
     * 
     * @return the declaring class
     */
    public Class<T> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Return an array of the {@link Class} objects associated with the
     * exceptions declared to be thrown by this constructor. If the constructor
     * was not declared to throw any exceptions, the array returned will be
     * empty.
     * 
     * @return the declared exception classes
     */
    public Class<?>[] getExceptionTypes() {
        if (exceptionTypes == null)
            return new Class[0];
        return exceptionTypes;
    }

    /**
     * Return the modifiers for the modeled constructor. The Modifier class
     * should be used to decode the result.
     * 
     * @return the modifiers
     * @see java.lang.reflect.Modifier
     */
    public int getModifiers() {
        return getConstructorModifiers(declaringClass, slot);
    }

    private native int getConstructorModifiers(Class<T> declaringClass, int slot);
    
    /**
     * Return the name of the modeled constructor. This is the name of the
     * declaring class.
     * 
     * @return the name
     */
    public String getName() {
        return declaringClass.getName();
    }

    /**
     * Return an array of the {@link Class} objects associated with the
     * parameter types of this constructor. If the constructor was declared with
     * no parameters, the array returned will be empty.
     * 
     * @return the parameter types
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
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
        result.append(")V");
        
        return result.toString();
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method. The hash code for a Constructor is
     * the hash code of the declaring class' name.
     * 
     * @return the receiver's hash
     * @see #equals
     */
    @Override
    public int hashCode() {
        return declaringClass.getName().hashCode();
    }

    /**
     * Return a new instance of the declaring class, initialized by dynamically
     * invoking the modeled constructor. This reproduces the effect of
     * <code>new declaringClass(arg1, arg2, ... , argN)</code> This method
     * performs the following:
     * <ul>
     * <li>A new instance of the declaring class is created. If the declaring
     * class cannot be instantiated (i.e. abstract class, an interface, an array
     * type, or a base type) then an InstantiationException is thrown.</li>
     * <li>If this Constructor object is enforcing access control (see
     * AccessibleObject) and the modeled constructor is not accessible from the
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
     * <li>The modeled constructor is then invoked. If an exception is thrown
     * during the invocation, it is caught and wrapped in an
     * InvocationTargetException. This exception is then thrown. If the
     * invocation completes normally, the newly initialized object is returned.
     * </ul>
     * 
     * @param args the arguments to the constructor
     * @return the new, initialized, object
     * @exception java.lang.InstantiationException if the class cannot be
     *            instantiated
     * @exception java.lang.IllegalAccessException if the modeled constructor
     *            is not accessible
     * @exception java.lang.IllegalArgumentException if an incorrect number of
     *            arguments are passed, or an argument could not be converted by
     *            a widening conversion
     * @exception java.lang.reflect.InvocationTargetException if an exception
     *            was thrown by the invoked constructor
     * @see java.lang.reflect.AccessibleObject
     */
    public T newInstance(Object... args) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        return constructNative (args, declaringClass, parameterTypes, slot, flag);
    }

    private native T constructNative(Object[] args, Class<T> declaringClass,
            Class<?>[] parameterTypes, int slot,
            boolean noAccessCheck) throws InstantiationException, IllegalAccessException,
            InvocationTargetException;
    
    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver. The format of the string is modifiers (if any) declaring class
     * name '(' parameter types, separated by ',' ')' If the constructor throws
     * exceptions, ' throws ' exception types, separated by ',' For example:
     * <code>public String(byte[],String) throws UnsupportedEncodingException</code>
     * 
     * @return a printable representation for the receiver
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(Modifier.toString(getModifiers()));

        if (result.length() != 0)       // android-changed
            result.append(' ');
        result.append(declaringClass.getName());
        result.append("(");
        result.append(toString(parameterTypes));
        result.append(")");
        if (exceptionTypes != null && exceptionTypes.length != 0) {
            result.append(" throws ");
            result.append(toString(exceptionTypes));
        }
        
        return result.toString();
    }
}
