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
import org.apache.harmony.luni.lang.reflect.GenericSignatureParser;
import org.apache.harmony.luni.lang.reflect.Types;
import org.apache.harmony.kernel.vm.StringUtils;
// END android-added

import java.lang.annotation.Annotation;


/**
 * This class must be implemented by the VM vendor. This class models a field.
 * Information about the field can be accessed, and the field's value can be
 * accessed dynamically.
 * 
 */
public final class Field extends AccessibleObject implements Member {
    
    private Class<?> declaringClass;
    
    private Class<?> type;
    
    // BEGIN android-added
    private Type genericType;
    private volatile boolean genericTypesAreInitialized = false;
    // END android-added
    
    private String name;
    
    private int slot;
    
    private static final int TYPE_BOOLEAN = 1;
    private static final int TYPE_BYTE = 2;
    private static final int TYPE_CHAR = 3;
    private static final int TYPE_SHORT = 4;
    private static final int TYPE_INTEGER = 5;
    private static final int TYPE_FLOAT = 6;
    private static final int TYPE_LONG = 7;
    private static final int TYPE_DOUBLE = 8;
    
    /**
     * Prevent this class from being instantiated
     */
    private Field(){
        //do nothing
    }

    private Field(Class<?> declaringClass, Class<?> type, String name, int slot) {
        this.declaringClass = declaringClass;
        this.type = type;
        this.name = name;
        this.slot = slot;
    }

    // BEGIN android-added
    private synchronized void initGenericType() {
        if (!genericTypesAreInitialized) {
            String signatureAttribute = getSignatureAttribute();
            GenericSignatureParser parser = new GenericSignatureParser();
            parser.parseForField(this.declaringClass, signatureAttribute);
            genericType = parser.fieldType;
            if (genericType == null) {
                genericType = getType();
            }
            genericTypesAreInitialized = true;
        }
    }
    // END android-added
    
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
     * Get the Signature annotation for this field.  Returns null if not found.
     */
    native private Object[] getSignatureAnnotation(Class declaringClass,
            int slot);

    // END android-changed

    /**
     * <p>
     * Indicates whether or not this field is synthetic.
     * </p>
     * 
     * @return A value of <code>true</code> if this field is synthetic,
     *         otherwise <code>false</code>.
     * @since 1.5
     */
    public boolean isSynthetic() {
        int flags = getFieldModifiers(declaringClass, slot);
        return (flags & Modifier.SYNTHETIC) != 0;
    }

    /**
     * <p>
     * Returns the String representation of the field's declaration, including
     * the type parameters.
     * </p>
     * 
     * @return An instance of String.
     * @since 1.5
     */
    public String toGenericString() {
        // BEGIN android-changed
        StringBuilder sb = new StringBuilder(80);
        // append modifiers if any
        int modifier = getModifiers();
        if (modifier != 0) {
            sb.append(Modifier.toString(modifier)).append(' ');
        }
        // append generic type
        appendGenericType(sb, getGenericType());
        sb.append(' ');
        // append full field name
        sb.append(getDeclaringClass().getName()).append('.').append(getName());
        return sb.toString();
        // END android-changed
    }

    /**
     * <p>
     * Indicates whether or not this field is an enumeration constant.
     * </p>
     * 
     * @return A value of <code>true</code> if this field is an enumeration
     *         constant, otherwise <code>false</code>.
     * @since 1.5
     */
    public boolean isEnumConstant() {
        int flags = getFieldModifiers(declaringClass, slot);
        return (flags & Modifier.ENUM) != 0;
    }

    /**
     * <p>
     * Gets the declared type of this field.
     * </p>
     * 
     * @return An instance of {@link Type}.
     * @throws GenericSignatureFormatError if the generic method signature is
     *         invalid.
     * @throws TypeNotPresentException if the component type points to a missing
     *         type.
     * @throws MalformedParameterizedTypeException if the component type points
     *         to a type that can't be instantiated for some reason.
     * @since 1.5
     */
    public Type getGenericType() {
        // BEGIN android-changed
        initGenericType();
        return Types.getType(genericType);
        // END android-changed
    }
    
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getDeclaredAnnotations(declaringClass, slot);
    }
    native private Annotation[] getDeclaredAnnotations(Class declaringClass,
        int slot);

    /**
     * Compares the specified object to this Field and answer if they are equal.
     * The object must be an instance of Field with the same defining class and
     * name.
     * 
     * @param object the object to compare
     * @return true if the specified object is equal to this Field, false
     *         otherwise
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        return object instanceof Field && toString().equals(object.toString());
    }

    /**
     * Return the value of the field in the specified object. This reproduces
     * the effect of <code>object.fieldName</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * The value of the field is returned. If the type of this field is a base
     * type, the field value is automatically wrapped.
     * 
     * @param object
     *            the object to access
     * @return the field value, possibly wrapped
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public Object get(Object object) throws IllegalAccessException,
            IllegalArgumentException {
        return getField(object, declaringClass, type, slot, flag);
    }

    /**
     * Return the value of the field in the specified object as a boolean. This
     * reproduces the effect of <code>object.fieldName</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * 
     * @param object
     *            the object to access
     * @return the field value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public boolean getBoolean(Object object)
            throws IllegalAccessException, IllegalArgumentException {
        return getZField(object, declaringClass, type, slot, flag, TYPE_BOOLEAN);
    }

    /**
     * Return the value of the field in the specified object as a byte. This
     * reproduces the effect of <code>object.fieldName</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * 
     * @param object
     *            the object to access
     * @return the field value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public byte getByte(Object object) throws IllegalAccessException,
            IllegalArgumentException {
        return getBField(object, declaringClass, type, slot, flag, TYPE_BYTE);
    }

    /**
     * Return the value of the field in the specified object as a char. This
     * reproduces the effect of <code>object.fieldName</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * 
     * @param object
     *            the object to access
     * @return the field value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public char getChar(Object object) throws IllegalAccessException,
            IllegalArgumentException {
        return getCField(object, declaringClass, type, slot, flag, TYPE_CHAR);
    }

    /**
     * Return the {@link Class} associated with the class that defined this
     * field.
     * 
     * @return the declaring class
     */
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Return the value of the field in the specified object as a double. This
     * reproduces the effect of <code>object.fieldName</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * 
     * @param object
     *            the object to access
     * @return the field value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public double getDouble(Object object)
            throws IllegalAccessException, IllegalArgumentException {
        return getDField(object, declaringClass, type, slot, flag, TYPE_DOUBLE);
    }

    /**
     * Return the value of the field in the specified object as a float. This
     * reproduces the effect of <code>object.fieldName</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * 
     * @param object
     *            the object to access
     * @return the field value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public float getFloat(Object object) throws IllegalAccessException,
            IllegalArgumentException {
        return getFField(object, declaringClass, type, slot, flag, TYPE_FLOAT);
    }

    /**
     * Return the value of the field in the specified object as an int. This
     * reproduces the effect of <code>object.fieldName</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * 
     * @param object
     *            the object to access
     * @return the field value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public int getInt(Object object) throws IllegalAccessException,
            IllegalArgumentException {
        return getIField(object, declaringClass, type, slot, flag, TYPE_INTEGER);
    }

    /**
     * Return the value of the field in the specified object as a long. This
     * reproduces the effect of <code>object.fieldName</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * 
     * @param object
     *            the object to access
     * @return the field value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public long getLong(Object object) throws IllegalAccessException,
            IllegalArgumentException {
        return getJField(object, declaringClass, type, slot, flag, TYPE_LONG);
    }

    /**
     * Return the modifiers for the modeled field. The Modifier class should be
     * used to decode the result.
     * 
     * @return the modifiers
     * @see java.lang.reflect.Modifier
     */
    public int getModifiers() {
        return getFieldModifiers(declaringClass, slot);
    }

    private native int getFieldModifiers(Class<?> declaringClass, int slot);

    /**
     * Return the name of the modeled field.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the value of the field in the specified object as a short. This
     * reproduces the effect of <code>object.fieldName</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * 
     * @param object
     *            the object to access
     * @return the field value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public short getShort(Object object) throws IllegalAccessException,
            IllegalArgumentException {
        return getSField(object, declaringClass, type, slot, flag, TYPE_SHORT);
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
        return getSignature(type);
    }

    /**
     * Return the {@link Class} associated with the type of this field.
     * 
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * <p>
     * The hash code for a Field is the hash code of the field's name.
     * 
     * @return the receiver's hash
     * @see #equals
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Set the value of the field in the specified object to the boolean value.
     * This reproduces the effect of <code>object.fieldName = value</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * If the field type is a base type, the value is automatically unwrapped.
     * If the unwrap fails, an IllegalArgumentException is thrown. If the value
     * cannot be converted to the field type via a widening conversion, an
     * IllegalArgumentException is thrown.
     * 
     * @param object
     *            the object to access
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public void set(Object object, Object value)
            throws IllegalAccessException, IllegalArgumentException {
        setField(object, declaringClass, type, slot, flag, value);
    }
    
    /**
     * Set the value of the field in the specified object to the boolean value.
     * This reproduces the effect of <code>object.fieldName = value</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * If the value cannot be converted to the field type via a widening
     * conversion, an IllegalArgumentException is thrown.
     * 
     * @param object
     *            the object to access
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public void setBoolean(Object object, boolean value)
            throws IllegalAccessException, IllegalArgumentException {
        setZField(object, declaringClass, type, slot, flag, TYPE_BOOLEAN, value);
    }

    /**
     * Set the value of the field in the specified object to the byte value.
     * This reproduces the effect of <code>object.fieldName = value</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * If the value cannot be converted to the field type via a widening
     * conversion, an IllegalArgumentException is thrown.
     * 
     * @param object
     *            the object to access
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public void setByte(Object object, byte value)
            throws IllegalAccessException, IllegalArgumentException {
        setBField(object, declaringClass, type, slot, flag, TYPE_BYTE, value);
    }

    /**
     * Set the value of the field in the specified object to the char value.
     * This reproduces the effect of <code>object.fieldName = value</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * If the value cannot be converted to the field type via a widening
     * conversion, an IllegalArgumentException is thrown.
     * 
     * @param object
     *            the object to access
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public void setChar(Object object, char value)
            throws IllegalAccessException, IllegalArgumentException {
        setCField(object, declaringClass, type, slot, flag, TYPE_CHAR, value);
    }

    /**
     * Set the value of the field in the specified object to the double value.
     * This reproduces the effect of <code>object.fieldName = value</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * If the value cannot be converted to the field type via a widening
     * conversion, an IllegalArgumentException is thrown.
     * 
     * @param object
     *            the object to access
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public void setDouble(Object object, double value)
            throws IllegalAccessException, IllegalArgumentException {
        setDField(object, declaringClass, type, slot, flag, TYPE_DOUBLE, value);
    }

    /**
     * Set the value of the field in the specified object to the float value.
     * This reproduces the effect of <code>object.fieldName = value</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * If the value cannot be converted to the field type via a widening
     * conversion, an IllegalArgumentException is thrown.
     * 
     * @param object
     *            the object to access
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public void setFloat(Object object, float value)
            throws IllegalAccessException, IllegalArgumentException {
        setFField(object, declaringClass, type, slot, flag, TYPE_FLOAT, value);
    }

    /**
     * Set the value of the field in the specified object to the int value. This
     * reproduces the effect of <code>object.fieldName = value</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * If the value cannot be converted to the field type via a widening
     * conversion, an IllegalArgumentException is thrown.
     * 
     * @param object
     *            the object to access
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public void setInt(Object object, int value)
            throws IllegalAccessException, IllegalArgumentException {
        setIField(object, declaringClass, type, slot, flag, TYPE_INTEGER, value);
    }

    /**
     * Set the value of the field in the specified object to the long value.
     * This reproduces the effect of <code>object.fieldName = value</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * If the value cannot be converted to the field type via a widening
     * conversion, an IllegalArgumentException is thrown.
     * 
     * @param object
     *            the object to access
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public void setLong(Object object, long value)
            throws IllegalAccessException, IllegalArgumentException {
        setJField(object, declaringClass, type, slot, flag, TYPE_LONG, value);
    }

    /**
     * Set the value of the field in the specified object to the short value.
     * This reproduces the effect of <code>object.fieldName = value</code>
     * <p>
     * If the modeled field is static, the object argument is ignored.
     * Otherwise, if the object is null, a NullPointerException is thrown. If
     * the object is not an instance of the declaring class of the method, an
     * IllegalArgumentException is thrown.
     * <p>
     * If this Field object is enforcing access control (see AccessibleObject)
     * and the modeled field is not accessible from the current context, an
     * IllegalAccessException is thrown.
     * <p>
     * If the value cannot be converted to the field type via a widening
     * conversion, an IllegalArgumentException is thrown.
     * 
     * @param object
     *            the object to access
     * @param value
     *            the new value
     * @throws NullPointerException
     *             if the object is null and the field is non-static
     * @throws IllegalArgumentException
     *             if the object is not compatible with the declaring class
     * @throws IllegalAccessException
     *             if modeled field is not accessible
     */
    public void setShort(Object object, short value)
            throws IllegalAccessException, IllegalArgumentException {
        setSField(object, declaringClass, type, slot, flag, TYPE_SHORT, value);
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * <p>
     * The format of the string is:
     * <ul>
     * <li>modifiers (if any)
     * <li>return type
     * <li>declaring class name
     * <li>'.'
     * <li>field name
     * </ul>
     * <p>
     * For example:
     * <code>public static java.io.InputStream java.lang.System.in</code>
     * 
     * @return a printable representation for the receiver
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(Modifier.toString(getModifiers()));

        if (result.length() != 0)       // android-changed
            result.append(' ');
        result.append(type.getName());
        result.append(' ');
        result.append(declaringClass.getName());
        result.append('.');
        result.append(name);
        
        return result.toString();
    }
    
    private native Object getField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck) throws IllegalAccessException;

    private native double getDField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no)
            throws IllegalAccessException;

    private native int getIField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no)
            throws IllegalAccessException;

    private native long getJField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no)
            throws IllegalAccessException;

    private native boolean getZField(Object o, Class<?> declaringClass,
            Class<?> type, int slot, boolean noAccessCheck, int type_no)
            throws IllegalAccessException;

    private native float getFField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no)
            throws IllegalAccessException;

    private native char getCField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no)
            throws IllegalAccessException;

    private native short getSField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no)
            throws IllegalAccessException;

    private native byte getBField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no)
            throws IllegalAccessException;

    private native void setField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, Object value)
            throws IllegalAccessException;

    private native void setDField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no, double v)
            throws IllegalAccessException;

    private native void setIField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no, int i)
            throws IllegalAccessException;

    private native void setJField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no, long j)
            throws IllegalAccessException;

    private native void setZField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no, boolean z)
            throws IllegalAccessException;

    private native void setFField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no, float f)
            throws IllegalAccessException;

    private native void setCField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no, char c)
            throws IllegalAccessException;

    private native void setSField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no, short s)
            throws IllegalAccessException;

    private native void setBField(Object o, Class<?> declaringClass, Class<?> type,
            int slot, boolean noAccessCheck, int type_no, byte b)
            throws IllegalAccessException;
    
}
