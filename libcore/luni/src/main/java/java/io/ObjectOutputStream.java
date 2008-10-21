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

package java.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.util.IdentityHashMap;

import org.apache.harmony.luni.util.PriviAction;

/**
 * An ObjectOutputStream can be used to save Java objects into a stream where
 * the objects can be loaded later with an ObjectInputStream. Primitive data
 * (ints, bytes, chars, etc) can also be saved.
 * 
 * @see ObjectInputStream
 * @see ObjectOutput
 * @see Serializable
 * @see Externalizable
 */
public class ObjectOutputStream extends OutputStream implements ObjectOutput,
        ObjectStreamConstants {

    /*
     * How many nested levels to writeObject. We may not need this.
     */
    private int nestedLevels;

    /*
     * Where we write to
     */
    private DataOutputStream output;

    /*
     * If object replacement is enabled or not
     */
    private boolean enableReplace;

    /*
     * Where we write primitive types to
     */
    private DataOutputStream primitiveTypes;

    /*
     * Where the write primitive types are actually written to
     */
    private ByteArrayOutputStream primitiveTypesBuffer;

    /*
     * Table mapping Object -> Integer (handle)
     */
    private IdentityHashMap<Object, Integer> objectsWritten;

    /*
     * All objects are assigned an ID (integer handle)
     */
    private int currentHandle;

    /*
     * Used by defaultWriteObject
     */
    private Object currentObject;

    /*
     * Used by defaultWriteObject
     */
    private ObjectStreamClass currentClass;

    /*
     * Either ObjectStreamConstants.PROTOCOL_VERSION_1 or
     * ObjectStreamConstants.PROTOCOL_VERSION_2
     */
    private int protocolVersion;

    /*
     * Used to detect nested exception when saving an exception due to an error
     */
    private StreamCorruptedException nestedException;

    /*
     * Used to keep track of the PutField object for the class/object being
     * written
     */
    private EmulatedFieldsForDumping currentPutField;

    /*
     * Allows the receiver to decide if it needs to call writeObjectOverride
     */
    private boolean subclassOverridingImplementation;

    /*
     * cache for writeReplace methods
     */
    private IdentityHashMap<Class<?>, Object> writeReplaceCache;

    /**
     * Inner class to provide access to serializable fields
     */
    public static abstract class PutField {
        public abstract void put(String name, boolean value);

        public abstract void put(String name, char value);

        public abstract void put(String name, byte value);

        public abstract void put(String name, short value);

        public abstract void put(String name, int value);

        public abstract void put(String name, long value);

        public abstract void put(String name, float value);

        public abstract void put(String name, double value);

        public abstract void put(String name, Object value);

        /**
         * @deprecated This method is unsafe and may corrupt the output stream.
         *             Use ObjectOutputStream#writeFields() instead.
         */
        @Deprecated
        public abstract void write(ObjectOutput out) throws IOException;
    }

    /**
     * Constructs a new <code>ObjectOutputStream</code>. The representation
     * and proper initialization is in the hands of subclasses.
     * 
     * @throws IOException
     * @throws SecurityException
     *             if subclassing this is not allowed
     * 
     * @see SecurityManager#checkPermission(java.security.Permission)
     */
    protected ObjectOutputStream() throws IOException, SecurityException {
        super();
        SecurityManager currentManager = System.getSecurityManager();
        if (currentManager != null) {
            currentManager.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        /*
         * WARNING - we should throw IOException if not called from a subclass
         * according to the JavaDoc. Add the test.
         */
        this.subclassOverridingImplementation = true;
    }

    /**
     * Constructs a new ObjectOutputStream on the OutputStream
     * <code>output</code>. All writes are now filtered through this stream.
     * 
     * @param output
     *            The non-null OutputStream to filter writes on.
     * 
     * @throws IOException
     *             If an IO exception happened when writing the object stream
     *             header
     */
    public ObjectOutputStream(OutputStream output) throws IOException {
        Class<?> implementationClass = getClass();
        Class<?> thisClass = ObjectOutputStream.class;
        if (implementationClass != thisClass) {
            boolean mustCheck = false;
            try {
                Method method = implementationClass.getMethod("putFields", //$NON-NLS-1$
                        ObjectStreamClass.EMPTY_CONSTRUCTOR_PARAM_TYPES);
                mustCheck = method.getDeclaringClass() != thisClass;
            } catch (NoSuchMethodException e) {
            }
            if (!mustCheck) {
                try {
                    Method method = implementationClass.getMethod(
                            "writeUnshared", //$NON-NLS-1$
                            ObjectStreamClass.UNSHARED_PARAM_TYPES);
                    mustCheck = method.getDeclaringClass() != thisClass;
                } catch (NoSuchMethodException e) {
                }
            }
            if (mustCheck) {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm
                            .checkPermission(ObjectStreamConstants.SUBCLASS_IMPLEMENTATION_PERMISSION);
                }
            }
        }
        this.output = (output instanceof DataOutputStream) ? (DataOutputStream) output
                : new DataOutputStream(output);
        this.enableReplace = false;
        this.protocolVersion = PROTOCOL_VERSION_2;
        this.subclassOverridingImplementation = false;
        this.writeReplaceCache = new IdentityHashMap<Class<?>, Object>();

        resetState();
        this.nestedException = new StreamCorruptedException();
        // So write...() methods can be used by
        // subclasses during writeStreamHeader()
        primitiveTypes = this.output;
        // Has to be done here according to the specification
        writeStreamHeader();
        primitiveTypes = null;
    }

    /**
     * Writes optional information for class <code>aClass</code> into the
     * stream represented by the receiver. This optional data can be read when
     * deserializing the class descriptor (ObjectStreamClass) for this class
     * from the input stream. By default no extra data is saved.
     * 
     * @param aClass
     *            The class to annotate
     * 
     * @throws IOException
     *             If an IO exception happened when annotating the class.
     * 
     * @see ObjectInputStream#resolveClass
     */
    protected void annotateClass(Class<?> aClass) throws IOException {
        // By default no extra info is saved. Subclasses can override
    }

    /**
     * Writes optional information for a proxy class into the stream represented
     * by the receiver. This optional data can be read when deserializing the
     * proxy class from the input stream. By default no extra data is saved.
     * 
     * @param aClass
     *            The proxy class to annotate
     * 
     * @throws IOException
     *             If an IO exception happened when annotating the class.
     * 
     * @see ObjectInputStream#resolveProxyClass
     */
    protected void annotateProxyClass(Class<?> aClass) throws IOException {
        // By default no extra info is saved. Subclasses can override
    }

    /**
     * Do the necessary work to see if the receiver can be used to write
     * primitive types like int, char, etc.
     */
    private void checkWritePrimitiveTypes() {
        if (primitiveTypes == null) {
            // If we got here we have no Stream previously created
            // WARNING - if the stream does not grow, this code is wrong
            primitiveTypesBuffer = new ByteArrayOutputStream(128);
            primitiveTypes = new DataOutputStream(primitiveTypesBuffer);
        }
    }

    /**
     * Close this ObjectOutputStream. Any buffered data is flushed. This
     * implementation closes the target stream.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this stream.
     */
    @Override
    public void close() throws IOException {
        // First flush what is needed (primitive data, etc)
        flush();
        output.close();
    }

    /**
     * Computes the collection of emulated fields that users can manipulate to
     * store a representation different than the one declared by the class of
     * the object being dumped.
     * 
     * @see #writeFields
     * @see #writeFieldValues(EmulatedFieldsForDumping)
     */
    private void computePutField() {
        currentPutField = new EmulatedFieldsForDumping(currentClass);
    }

    /**
     * Default method to write objects into the receiver. Fields defined in the
     * object's class and superclasses (which are Serializable) will be saved.
     * 
     * @throws IOException
     *             If an IO error occurs attempting to write the object data
     * 
     * @see ObjectInputStream#defaultReadObject
     */
    public void defaultWriteObject() throws IOException {
        // We can't be called from just anywhere. There are rules.
        if (currentObject == null) {
            throw new NotActiveException();
        }
        writeFieldValues(currentObject, currentClass);
    }

    /**
     * Flushes buffered primitive data into the receiver.
     * 
     * @throws IOException
     *             If an error occurs attempting to drain the data
     */
    protected void drain() throws IOException {
        if (primitiveTypes == null) {
            return;
        }

        // If we got here we have a Stream previously created
        int offset = 0;
        byte[] written = primitiveTypesBuffer.toByteArray();
        // Normalize the primitive data
        while (offset < written.length) {
            int toWrite = written.length - offset > 1024 ? 1024
                    : written.length - offset;
            if (toWrite < 256) {
                output.writeByte(TC_BLOCKDATA);
                output.writeByte((byte) toWrite);
            } else {
                output.writeByte(TC_BLOCKDATALONG);
                output.writeInt(toWrite);
            }

            // write primitive types we had and the marker of end-of-buffer
            output.write(written, offset, toWrite);
            offset += toWrite;
        }

        // and now we're clean to a state where we can write an object
        primitiveTypes = null;
        primitiveTypesBuffer = null;
    }

    /**
     * Dumps the parameter <code>obj</code> only if it is <code>null</code>
     * or an object that has already been dumped previously.
     * 
     * @param obj
     *            Object to check if an instance previously dumped by this
     *            stream.
     * @return null if it is an instance which has not been dumped yet (and this
     *         method does nothing). Integer, if <code>obj</code> is an
     *         instance which has been dumped already. In this case this method
     *         saves the cyclic reference.
     * 
     * @throws IOException
     *             If an error occurs attempting to save <code>null</code> or
     *             a cyclic reference.
     */
    private Integer dumpCycle(Object obj) throws IOException {
        // If the object has been saved already, save its handle only
        Integer handle = registeredObjectHandleFor(obj);
        if (handle != null) {
            writeCyclicReference(handle);
            return handle;
        }
        return null;
    }

    /**
     * Enables/disables object replacement for the receiver. By default this is
     * not enabled. Only trusted subclasses (loaded with system class loader)
     * can override this behavior.
     * 
     * @param enable
     *            if true, enables replacement. If false, disables replacement.
     * @return boolean the previous configuration (if it was enabled or
     *         disabled)
     * 
     * @throws SecurityException
     *             If the class of the receiver is not trusted
     * 
     * @see #replaceObject
     * @see ObjectInputStream#enableResolveObject
     */
    protected boolean enableReplaceObject(boolean enable)
            throws SecurityException {
        if (enable) {
            // The Stream has to be trusted for this feature to be enabled.
            // trusted means the stream's classloader has to be null
            SecurityManager currentManager = System.getSecurityManager();
            if (currentManager != null) {
                currentManager.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        boolean originalValue = enableReplace;
        enableReplace = enable;
        return originalValue;
    }

    /**
     * Flush this ObjectOutputStream. Any pending writes to the underlying
     * stream are written out when this method is invoked.
     * 
     * @throws IOException
     *             If an error occurs attempting to flush this
     *             ObjectOutputStream.
     */
    @Override
    public void flush() throws IOException {
        drain();
        output.flush();
    }

    /**
     * Get the value of field named
     * <code>fieldName<code> of object <code>instance</code>. The
     * field is declared by class <code>declaringClass</code>. The field is supposed to be
     * a boolean.
     *
     * This method could be implemented non-natively on top of java.lang.reflect implementations
     * that support the <code>setAccessible</code> API, at the expense of extra object creation
     * (java.lang.reflect.Field). Otherwise Serialization could not fetch private fields, except
     * by the use of a native method like this one.
     *
     * @param instance Object whose field value we want to fetch
     * @param declaringClass The class that declares the field
     * @param fieldName Name of the field we want to fetch
     * @return the value of the field
     *
     * @throws NoSuchFieldError If the field does not exist.
     */
    private static native boolean getFieldBool(Object instance,
            Class<?> declaringClass, String fieldName);

    /**
     * Get the value of field named
     * <code>fieldName<code> of object <code>instance</code>. The
     * field is declared by class <code>declaringClass</code>. The field is supposed to be
     * a byte
     *
     * This method could be implemented non-natively on top of java.lang.reflect implementations
     * that support the <code>setAccessible</code> API, at the expense of extra object creation
     * (java.lang.reflect.Field). Otherwise Serialization could not fetch private fields, except
     * by the use of a native method like this one.
     *
     * @param instance Object whose field value we want to fetch
     * @param declaringClass The class that declares the field
     * @param fieldName Name of the field we want to fetch
     * @return the value of the field
     *
     * @throws NoSuchFieldError If the field does not exist.
     */
    private static native byte getFieldByte(Object instance,
            Class<?> declaringClass, String fieldName);

    /**
     * Get the value of field named
     * <code>fieldName<code> of object <code>instance</code>. The
     * field is declared by class <code>declaringClass</code>. The field is supposed to be
     * a char.
     *
     * This method could be implemented non-natively on top of java.lang.reflect implementations
     * that support the <code>setAccessible</code> API, at the expense of extra object creation
     * (java.lang.reflect.Field). Otherwise Serialization could not fetch private fields, except
     * by the use of a native method like this one.
     *
     * @param instance Object whose field value we want to fetch
     * @param declaringClass The class that declares the field
     * @param fieldName Name of the field we want to fetch
     * @return the value of the field
     *
     * @throws NoSuchFieldError If the field does not exist.
     */
    private static native char getFieldChar(Object instance,
            Class<?> declaringClass, String fieldName);

    /**
     * Get the value of field named
     * <code>fieldName<code> of object <code>instance</code>. The
     * field is declared by class <code>declaringClass</code>. The field is supposed to be
     * a double.
     *
     * This method could be implemented non-natively on top of java.lang.reflect implementations
     * that support the <code>setAccessible</code> API, at the expense of extra object creation
     * (java.lang.reflect.Field). Otherwise Serialization could not fetch private fields, except
     * by the use of a native method like this one.
     *
     * @param instance Object whose field value we want to fetch
     * @param declaringClass The class that declares the field
     * @param fieldName Name of the field we want to fetch
     * @return the value of the field
     *
     * @throws NoSuchFieldError If the field does not exist.
     */
    private static native double getFieldDouble(Object instance,
            Class<?> declaringClass, String fieldName);

    /**
     * Get the value of field named
     * <code>fieldName<code> of object <code>instance</code>. The
     * field is declared by class <code>declaringClass</code>. The field is supposed to be
     * a float.
     *
     * This method could be implemented non-natively on top of java.lang.reflect implementations
     * that support the <code>setAccessible</code> API, at the expense of extra object creation
     * (java.lang.reflect.Field). Otherwise Serialization could not fetch private fields, except
     * by the use of a native method like this one.
     *
     * @param instance Object whose field value we want to fetch
     * @param declaringClass The class that declares the field
     * @param fieldName Name of the field we want to fetch
     * @return the value of the field
     *
     * @throws NoSuchFieldError If the field does not exist.
     */
    private static native float getFieldFloat(Object instance,
            Class<?> declaringClass, String fieldName);

    /**
     * Get the value of field named
     * <code>fieldName<code> of object <code>instance</code>. The
     * field is declared by class <code>declaringClass</code>. The field is supposed to be
     * an int.
     *
     * This method could be implemented non-natively on top of java.lang.reflect implementations
     * that support the <code>setAccessible</code> API, at the expense of extra object creation
     * (java.lang.reflect.Field). Otherwise Serialization could not fetch private fields, except
     * by the use of a native method like this one.
     *
     * @param instance Object whose field value we want to fetch
     * @param declaringClass The class that declares the field
     * @param fieldName Name of the field we want to fetch
     * @return the value of the field
     *
     * @throws NoSuchFieldError If the field does not exist.
     */
    private static native int getFieldInt(Object instance,
            Class<?> declaringClass, String fieldName);

    /**
     * Get the value of field named
     * <code>fieldName<code> of object <code>instance</code>. The
     * field is declared by class <code>declaringClass</code>. The field is supposed to be
     * a long.
     *
     * This method could be implemented non-natively on top of java.lang.reflect implementations
     * that support the <code>setAccessible</code> API, at the expense of extra object creation
     * (java.lang.reflect.Field). Otherwise Serialization could not fetch private fields, except
     * by the use of a native method like this one.
     *
     * @param instance Object whose field value we want to fetch
     * @param declaringClass The class that declares the field
     * @param fieldName Name of the field we want to fetch
     * @return the value of the field
     *
     * @throws NoSuchFieldError If the field does not exist.
     */
    private static native long getFieldLong(Object instance,
            Class<?> declaringClass, String fieldName);

    /**
     * Get the value of field named
     * <code>fieldName<code> of object <code>instance</code>. The
     * field is declared by class <code>declaringClass</code>. The field is supposed to be
     * an Object type whose name is <code>fieldTypeName</code>.
     *
     * This method could be implemented non-natively on top of java.lang.reflect implementations
     * that support the <code>setAccessible</code> API, at the expense of extra object creation
     * (java.lang.reflect.Field). Otherwise Serialization could not fetch private fields, except
     * by the use of a native method like this one.
     *
     * @param instance Object whose field value we want to fetch
     * @param declaringClass The class that declares the field
     * @param fieldName Name of the field we want to fetch
     * @param fieldTypeName Name of the class that defines the type of this field
     * @return the value of the field
     *
     * @throws NoSuchFieldError If the field does not exist.
     */
    private static native Object getFieldObj(Object instance,
            Class<?> declaringClass, String fieldName, String fieldTypeName);

    /**
     * Get the value of field named
     * <code>fieldName<code> of object <code>instance</code>. The
     * field is declared by class <code>declaringClass</code>. The field is supposed to be
     * a short.
     *
     * This method could be implemented non-natively on top of java.lang.reflect implementations
     * that support the <code>setAccessible</code> API, at the expense of extra object creation
     * (java.lang.reflect.Field). Otherwise Serialization could not fetch private fields, except
     * by the use of a native method like this one.
     *
     * @param instance Object whose field value we want to fetch
     * @param declaringClass The class that declares the field
     * @param fieldName Name of the field we want to fetch
     * @return the value of the field
     *
     * @throws NoSuchFieldError If the field does not exist.
     */
    private static native short getFieldShort(Object instance,
            Class<?> declaringClass, String fieldName);

    /**
     * Return the next <code>int</code> handle to be used to indicate cyclic
     * references being saved to the stream.
     * 
     * @return int, the next handle to represent the next cyclic reference
     */
    private int nextHandle() {
        return this.currentHandle++;
    }

    /**
     * Return the <code>PutField</code> object for the receiver. This allows
     * users to transfer values from actual object fields in the object being
     * dumped to the emulated fields represented by the <code>PutField</code>
     * returned by this method.
     * 
     * @return the PutFieldObject for the receiver
     * 
     * @throws IOException
     *             If an IO error occurs
     * @throws NotActiveException
     *             If this method is not called from writeObject()
     * 
     * @see ObjectInputStream#defaultReadObject
     */
    public PutField putFields() throws IOException {
        // We can't be called from just anywhere. There are rules.
        if (currentObject == null) {
            throw new NotActiveException();
        }
        if (currentPutField == null) {
            computePutField();
        }
        return currentPutField;
    }

    /**
     * Return the <code>Integer</code> handle used to tag object
     * <code>obj</code> as an instance that has been dumped already. Return
     * <code>null</code> if object <code>obj</code> has not been saved yet.
     * 
     * @param obj
     *            the object
     * @return null if object <code>obj</code> has not been saved yet. Integer
     *         The handle that this object was assigned when it was saved.
     */
    private Integer registeredObjectHandleFor(Object obj) {
        return objectsWritten.get(obj);
    }

    /**
     * Assume object <code>obj</code> has not been dumped yet, and assign a
     * handle to it
     * 
     * @param obj
     *            Non-null object being dumped.
     * @return the handle that this object is being assigned.
     * 
     * @see #nextHandle
     */
    private Integer registerObjectWritten(Object obj) {
        Integer handle = Integer.valueOf(nextHandle());
        registerObjectWritten(obj, handle);
        return handle;
    }

    /**
     * Remove the unshared object from the table, and restore any previous
     * handle.
     * 
     * @param obj
     *            Non-null object being dumped.
     * @param previousHandle
     *            The handle of the previous identical object dumped
     */
    private void removeUnsharedReference(Object obj, Integer previousHandle) {
        if (previousHandle != null) {
            registerObjectWritten(obj, previousHandle);
        } else {
            objectsWritten.remove(obj);
        }
    }

    /**
     * Assume object <code>obj</code> has not been dumped yet, and assign a
     * handle to it, <code>handle</code>.
     * 
     * @param obj
     *            Non-null object being dumped.
     * @param handle
     *            An Integer, the handle to this object
     * 
     * @see #nextHandle
     */
    private void registerObjectWritten(Object obj, Integer handle) {
        objectsWritten.put(obj, handle);
    }

    /**
     * If <code>enableReplaceObject()</code> was activated, computes the
     * replacement object for the original object <code>object</code> and
     * returns the replacement. Otherwise returns <code>object</code>.
     * 
     * @param object
     *            Original object for which a replacement may be defined
     * @return a possibly new, replacement object for <code>object</code>
     * 
     * @throws IOException
     *             If any IO problem occurred when trying to resolve the object.
     * 
     * @see #enableReplaceObject
     * @see ObjectInputStream#enableResolveObject
     * @see ObjectInputStream#resolveObject
     */
    protected Object replaceObject(Object object) throws IOException {
        // By default no object replacement. Subclasses can override
        return object;
    }

    /**
     * Reset the receiver. A marker is written to the stream, so that
     * deserialization will also perform a rest at the same point. Objects
     * previously written are no longer remembered, so they will be written
     * again (instead of a cyclical reference) if found in the object graph.
     * 
     * @throws IOException
     *             If any IO problem occurred when trying to reset the receiver
     */
    public void reset() throws IOException {
        // First we flush what we have
        drain();
        /*
         * And dump a reset marker, so that the ObjectInputStream can reset
         * itself at the same point
         */
        output.writeByte(TC_RESET);
        // Now we reset ourselves
        resetState();
    }

    /**
     * Reset the collection of objects already dumped by the receiver. If the
     * objects are found again in the object graph, the receiver will dump them
     * again, instead of a handle (cyclic reference).
     * 
     */
    private void resetSeenObjects() {
        objectsWritten = new IdentityHashMap<Object, Integer>();
        currentHandle = baseWireHandle;
    }

    /**
     * Reset the receiver. The collection of objects already dumped by the
     * receiver is reset, and internal structures are also reset so that the
     * receiver knows it is in a fresh clean state.
     * 
     */
    private void resetState() {
        resetSeenObjects();
        nestedLevels = 0;
    }

    /**
     * Set the receiver to use the given protocol version.
     * 
     * @param version
     *            protocol version to be used
     * 
     * @throws IOException
     *             If an IO error occurs
     */
    public void useProtocolVersion(int version) throws IOException {
        if (version != ObjectStreamConstants.PROTOCOL_VERSION_1
                && version != ObjectStreamConstants.PROTOCOL_VERSION_2) {
            throw new IllegalArgumentException(org.apache.harmony.luni.util.Msg
                    .getString("K00b3", version)); //$NON-NLS-1$
        }
        protocolVersion = version;
    }

    /**
     * Writes the entire contents of the byte array <code>buffer</code> to
     * this ObjectOutputStream.
     * 
     * @param buffer
     *            the buffer to be written
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this
     *             ObjectOutputStream.
     */
    @Override
    public void write(byte[] buffer) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.write(buffer);
    }

    /**
     * Writes <code>length</code> <code>bytes</code> from the byte array
     * <code>buffer</code> starting at offset <code>offset</code> to the
     * ObjectOutputStream.
     * 
     * @param buffer
     *            the buffer to be written
     * @param offset
     *            offset in buffer to get bytes
     * @param length
     *            number of bytes in buffer to write
     * 
     * @throws IOException
     *             If an error occurs attempting to write to this OutputStream.
     */
    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.write(buffer, offset, length);
    }

    /**
     * Write one byte (<code>value</code>) into the receiver's underlying
     * stream.
     * 
     * @param value
     *            The primitive data to write. Only the lower byte is written.
     * 
     * @throws IOException
     *             If an IO exception happened when writing the byte.
     */
    @Override
    public void write(int value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.write(value);
    }

    /**
     * Write primitive data of type boolean (<code>value</code>)into the
     * receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeBoolean(boolean value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeBoolean(value);
    }

    /**
     * Write primitive data of type byte (<code>value</code>)into the
     * receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeByte(int value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeByte(value);
    }

    /**
     * Write a String as a sequence of bytes (only lower-order 8 bits of each
     * char are written), as primitive data (<code>value</code>) into the
     * receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeBytes(String value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeBytes(value);
    }

    /**
     * Write primitive data of type char (<code>value</code>)into the
     * receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeChar(int value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeChar(value);
    }

    /**
     * Write a String as a sequence of char, as primitive data (<code>value</code>)
     * into the receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeChars(String value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeChars(value);
    }

    /**
     * Write a class descriptor <code>classDesc</code> (an
     * <code>ObjectStreamClass</code>) to the stream.
     * 
     * @param classDesc
     *            The class descriptor (an <code>ObjectStreamClass</code>) to
     *            be dumped
     * @param unshared
     *            Write the object unshared
     * @return the handle assigned to the class descriptor
     * 
     * @throws IOException
     *             If an IO exception happened when writing the class
     *             descriptor.
     */
    private Integer writeClassDesc(ObjectStreamClass classDesc, boolean unshared)
            throws IOException {
        if (classDesc == null) {
            writeNull();
            return null;
        }
        Integer handle = null;
        if (!unshared) {
            handle = dumpCycle(classDesc);
        }
        if (handle == null) {
            Class<?> classToWrite = classDesc.forClass();
            Integer previousHandle = objectsWritten.get(classDesc);
            // If we got here, it is a new (non-null) classDesc that will have
            // to be registered as well
            handle = registerObjectWritten(classDesc);

            if (Proxy.isProxyClass(classToWrite)) {
                output.writeByte(TC_PROXYCLASSDESC);
                Class<?>[] interfaces = classToWrite.getInterfaces();
                output.writeInt(interfaces.length);
                for (int i = 0; i < interfaces.length; i++) {
                    output.writeUTF(interfaces[i].getName());
                }
                annotateProxyClass(classToWrite);
                output.writeByte(TC_ENDBLOCKDATA);
                writeClassDescForClass(Proxy.class);
                if (unshared) {
                    // remove reference to unshared object
                    removeUnsharedReference(classDesc, previousHandle);
                }
                return handle;
            }

            output.writeByte(TC_CLASSDESC);
            if (protocolVersion == PROTOCOL_VERSION_1) {
                writeNewClassDesc(classDesc);
            } else {
                // So write...() methods can be used by
                // subclasses during writeClassDescriptor()
                primitiveTypes = output;
                writeClassDescriptor(classDesc);
                primitiveTypes = null;
            }
            // Extra class info (optional)
            annotateClass(classToWrite);
            drain(); // flush primitive types in the annotation
            output.writeByte(TC_ENDBLOCKDATA);
            writeClassDesc(classDesc.getSuperclass(), unshared);
            if (unshared) {
                // remove reference to unshared object
                removeUnsharedReference(classDesc, previousHandle);
            }
        }
        return handle;
    }

    /**
     * Writes a class descriptor (an <code>ObjectStreamClass</code>) that
     * corresponds to the <code>java.lang.Class objClass</code> to the stream.
     * 
     * @param objClass
     *            The class for which a class descriptor (an
     *            <code>ObjectStreamClass</code>) will be dumped.
     * @return the handle assigned to the class descriptor
     * 
     * @throws IOException
     *             If an IO exception happened when writing the class
     *             descriptor.
     * 
     */
    private Integer writeClassDescForClass(Class<?> objClass)
            throws IOException {
        return writeClassDesc(ObjectStreamClass.lookup(objClass), false);
    }

    /**
     * Writes a handle representing a cyclic reference (object previously
     * dumped).
     * 
     * @param handle
     *            The Integer handle that represents an object previously seen
     * 
     * @throws IOException
     *             If an IO exception happened when writing the cyclic
     *             reference.
     */
    private void writeCyclicReference(Integer handle) throws IOException {
        output.writeByte(TC_REFERENCE);
        output.writeInt(handle.intValue());
    }

    /**
     * Write primitive data of type double (<code>value</code>)into the
     * receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeDouble(double value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeDouble(value);
    }

    /**
     * Writes a collection of field descriptors (name, type name, etc) for the
     * class descriptor <code>classDesc</code> (an
     * <code>ObjectStreamClass</code>)
     * 
     * @param classDesc
     *            The class descriptor (an <code>ObjectStreamClass</code>)
     *            for which to write field information
     * @param externalizable
     *            true if the descriptors are externalizable
     * 
     * @throws IOException
     *             If an IO exception happened when writing the field
     *             descriptors.
     * 
     * @see #writeObject(Object)
     */
    private void writeFieldDescriptors(ObjectStreamClass classDesc,
            boolean externalizable) throws IOException {
        Class<?> loadedClass = classDesc.forClass();
        ObjectStreamField[] fields = null;
        int fieldCount = 0;

        // The fields of String are not needed since Strings are treated as
        // primitive types
        if (!externalizable && loadedClass != ObjectStreamClass.STRINGCLASS) {
            fields = classDesc.fields();
            fieldCount = fields.length;
        }

        // Field count
        output.writeShort(fieldCount);
        // Field names
        for (int i = 0; i < fieldCount; i++) {
            ObjectStreamField f = fields[i];
            output.writeByte(f.getTypeCode());
            output.writeUTF(f.getName());
            if (!f.isPrimitive()) {
                writeObject(f.getTypeString());
            }
        }
    }

    /**
     * Write the fields of the object being dumped. The stream will use the
     * currently active <code>PutField</code> object, allowing users to dump
     * emulated fields, for cross-loading compatibility when a class definition
     * changes.
     * 
     * @throws IOException
     *             If an IO error occurs
     * 
     * @see #putFields
     */
    public void writeFields() throws IOException {
        // Has to have fields to write
        if (currentPutField == null) {
            throw new NotActiveException();
        }
        writeFieldValues(currentPutField);
    }

    /**
     * Writes a collection of field values for the emulated fields
     * <code>emulatedFields</code>
     * 
     * @param emulatedFields
     *            an <code>EmulatedFieldsForDumping</code>, concrete subclass
     *            of <code>PutField</code>
     * 
     * @throws IOException
     *             If an IO exception happened when writing the field values.
     * 
     * @see #writeFields
     * @see #writeObject(Object)
     */
    private void writeFieldValues(EmulatedFieldsForDumping emulatedFields)
            throws IOException {
        EmulatedFields accessibleSimulatedFields = emulatedFields
                .emulatedFields(); // Access internal fields which we can
        // set/get. Users can't do this.
        EmulatedFields.ObjectSlot[] slots = accessibleSimulatedFields.slots();
        for (int i = 0; i < slots.length; i++) {
            EmulatedFields.ObjectSlot slot = slots[i];
            Object fieldValue = slot.getFieldValue();
            Class<?> type = slot.getField().getType();
            // WARNING - default values exist for each primitive type
            if (type == Integer.TYPE) {
                output.writeInt(fieldValue != null ? ((Integer) fieldValue)
                        .intValue() : 0);
            } else if (type == Byte.TYPE) {
                output.writeByte(fieldValue != null ? ((Byte) fieldValue)
                        .byteValue() : (byte) 0);
            } else if (type == Character.TYPE) {
                output.writeChar(fieldValue != null ? ((Character) fieldValue)
                        .charValue() : (char) 0);
            } else if (type == Short.TYPE) {
                output.writeShort(fieldValue != null ? ((Short) fieldValue)
                        .shortValue() : (short) 0);
            } else if (type == Boolean.TYPE) {
                output.writeBoolean(fieldValue != null ? ((Boolean) fieldValue)
                        .booleanValue() : false);
            } else if (type == Long.TYPE) {
                output.writeLong(fieldValue != null ? ((Long) fieldValue)
                        .longValue() : (long) 0);
            } else if (type == Float.TYPE) {
                output.writeFloat(fieldValue != null ? ((Float) fieldValue)
                        .floatValue() : (float) 0);
            } else if (type == Double.TYPE) {
                output.writeDouble(fieldValue != null ? ((Double) fieldValue)
                        .doubleValue() : (double) 0);
            } else {
                // Either array or Object
                writeObject(fieldValue);
            }
        }
    }

    /**
     * Writes a collection of field values for the fields described by class
     * descriptor <code>classDesc</code> (an <code>ObjectStreamClass</code>).
     * This is the default mechanism, when emulated fields (an
     * <code>PutField</code>) are not used. Actual values to dump are fetched
     * directly from object <code>obj</code>.
     * 
     * @param obj
     *            Instance from which to fetch field values to dump.
     * @param classDesc
     *            A class descriptor (an <code>ObjectStreamClass</code>)
     *            defining which fields should be dumped.
     * 
     * @throws IOException
     *             If an IO exception happened when writing the field values.
     * 
     * @see #writeObject(Object)
     */
    private void writeFieldValues(Object obj, ObjectStreamClass classDesc)
            throws IOException {
        ObjectStreamField[] fields = classDesc.fields();
        Class<?> declaringClass = classDesc.forClass();
        for (int i = 0; i < fields.length; i++) {
            try {
                // Code duplication starts, just because Java is typed
                ObjectStreamField fieldDesc = fields[i];
                if (fieldDesc.isPrimitive()) {
                    switch (fieldDesc.getTypeCode()) {
                        case 'B':
                            output.writeByte(getFieldByte(obj, declaringClass,
                                    fieldDesc.getName()));
                            break;
                        case 'C':
                            output.writeChar(getFieldChar(obj, declaringClass,
                                    fieldDesc.getName()));
                            break;
                        case 'D':
                            output.writeDouble(getFieldDouble(obj,
                                    declaringClass, fieldDesc.getName()));
                            break;
                        case 'F':
                            output.writeFloat(getFieldFloat(obj,
                                    declaringClass, fieldDesc.getName()));
                            break;
                        case 'I':
                            output.writeInt(getFieldInt(obj, declaringClass,
                                    fieldDesc.getName()));
                            break;
                        case 'J':
                            output.writeLong(getFieldLong(obj, declaringClass,
                                    fieldDesc.getName()));
                            break;
                        case 'S':
                            output.writeShort(getFieldShort(obj,
                                    declaringClass, fieldDesc.getName()));
                            break;
                        case 'Z':
                            output.writeBoolean(getFieldBool(obj,
                                    declaringClass, fieldDesc.getName()));
                            break;
                        default:
                            throw new IOException(
                                    org.apache.harmony.luni.util.Msg.getString(
                                            "K00d5", fieldDesc.getTypeCode())); //$NON-NLS-1$
                    }
                } else {
                    // Object type (array included).
                    Object field = getFieldObj(obj, declaringClass, fieldDesc
                            .getName(), fieldDesc.getTypeString());
                    if (fieldDesc.isUnshared()) {
                        writeUnshared(field);
                    } else {
                        writeObject(field);
                    }
                }
            } catch (NoSuchFieldError nsf) {
                // The user defined serialPersistentFields but did not provide
                // the glue to transfer values,
                // (in writeObject) so we end up using the default mechanism and
                // fail to set the emulated field
                throw new InvalidClassException(classDesc.getName());
            }
        }
    }

    /**
     * Write primitive data of type float (<code>value</code>)into the
     * receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeFloat(float value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeFloat(value);
    }

    /**
     * Walks the hierarchy of classes described by class descriptor
     * <code>classDesc</code> and writes the field values corresponding to
     * fields declared by the corresponding class descriptor. The instance to
     * fetch field values from is <code>object</code>. If the class
     * (corresponding to class descriptor <code>classDesc</code>) defines
     * private instance method <code>writeObject</code> it will be used to
     * dump field values.
     * 
     * @param object
     *            Instance from which to fetch field values to dump.
     * @param classDesc
     *            A class descriptor (an <code>ObjectStreamClass</code>)
     *            defining which fields should be dumped.
     * 
     * @throws IOException
     *             If an IO exception happened when writing the field values in
     *             the hierarchy.
     * @throws NotActiveException
     *             If the given object is not active
     * 
     * @see #defaultWriteObject
     * @see #writeObject(Object)
     */
    private void writeHierarchy(Object object, ObjectStreamClass classDesc)
            throws IOException, NotActiveException {
        // We can't be called from just anywhere. There are rules.
        if (object == null) {
            throw new NotActiveException();
        }

        // Fields are written from class closest to Object to leaf class
        // (down the chain)
        if (classDesc.getSuperclass() != null) {
            // first
            writeHierarchy(object, classDesc.getSuperclass());
        }

        // Have to do this before calling defaultWriteObject or anything
        // that calls defaultWriteObject
        currentObject = object;
        currentClass = classDesc;

        // See if the object has a writeObject method. If so, run it
        boolean executed = false;
        Class<?> targetClass = classDesc.forClass();
        try {
            final Method method = ObjectStreamClass
                    .getPrivateWriteObjectMethod(targetClass);
            if (method != null) {
                // We have to be able to fetch its value, even if it is
                // private
                AccessController.doPrivileged(new PriviAction<Object>(method));
                try {
                    method.invoke(object, new Object[] { this });
                    executed = true;
                } catch (InvocationTargetException e) {
                    Throwable ex = e.getTargetException();
                    if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    } else if (ex instanceof Error) {
                        throw (Error) ex;
                    }
                    throw (IOException) ex;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.toString());
                }
            }

            if (executed) {
                drain();
                output.writeByte(TC_ENDBLOCKDATA);
            } else {
                // If the object did not have a writeMethod, call
                // defaultWriteObject
                defaultWriteObject();
            }
        } finally {
            // Cleanup, needs to run always so that we can later detect
            // invalid calls to defaultWriteObject
            currentObject = null;
            currentClass = null;
            currentPutField = null;
        }
    }

    /**
     * Write primitive data of type int (<code>value</code>)into the
     * receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeInt(int value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeInt(value);
    }

    /**
     * Write primitive data of type long (<code>value</code>)into the
     * receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeLong(long value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeLong(value);
    }

    /**
     * Write array <code>array</code> of class <code>arrayClass</code> with
     * component type <code>componentType</code> into the receiver. It is
     * assumed the array has not been dumped yet. Return an <code>Integer</code>
     * that represents the handle for this object (array) which is dumped here.
     * 
     * @param array
     *            The array object to dump
     * @param arrayClass
     *            A <code>java.lang.Class</code> representing the class of the
     *            array
     * @param componentType
     *            A <code>java.lang.Class</code> representing the array
     *            component type
     * @return the handle assigned to the array
     * 
     * @throws IOException
     *             If an IO exception happened when writing the array.
     */
    private Integer writeNewArray(Object array, Class<?> arrayClass,
            Class<?> componentType, boolean unshared) throws IOException {
        output.writeByte(TC_ARRAY);
        writeClassDescForClass(arrayClass);

        Integer previousHandle = objectsWritten.get(array);
        Integer handle = registerObjectWritten(array);
        if (unshared) {
            // remove reference to unshared object
            removeUnsharedReference(array, previousHandle);
        }

        // Now we have code duplication just because Java is typed. We have to
        // write N elements and assign to array positions, but we must typecast
        // the array first, and also call different methods depending on the
        // elements.

        if (componentType.isPrimitive()) {
            if (componentType == Integer.TYPE) {
                int[] intArray = (int[]) array;
                output.writeInt(intArray.length);
                for (int i = 0; i < intArray.length; i++) {
                    output.writeInt(intArray[i]);
                }
            } else if (componentType == Byte.TYPE) {
                byte[] byteArray = (byte[]) array;
                output.writeInt(byteArray.length);
                output.write(byteArray, 0, byteArray.length);
            } else if (componentType == Character.TYPE) {
                char[] charArray = (char[]) array;
                output.writeInt(charArray.length);
                for (int i = 0; i < charArray.length; i++) {
                    output.writeChar(charArray[i]);
                }
            } else if (componentType == Short.TYPE) {
                short[] shortArray = (short[]) array;
                output.writeInt(shortArray.length);
                for (int i = 0; i < shortArray.length; i++) {
                    output.writeShort(shortArray[i]);
                }
            } else if (componentType == Boolean.TYPE) {
                boolean[] booleanArray = (boolean[]) array;
                output.writeInt(booleanArray.length);
                for (int i = 0; i < booleanArray.length; i++) {
                    output.writeBoolean(booleanArray[i]);
                }
            } else if (componentType == Long.TYPE) {
                long[] longArray = (long[]) array;
                output.writeInt(longArray.length);
                for (int i = 0; i < longArray.length; i++) {
                    output.writeLong(longArray[i]);
                }
            } else if (componentType == Float.TYPE) {
                float[] floatArray = (float[]) array;
                output.writeInt(floatArray.length);
                for (int i = 0; i < floatArray.length; i++) {
                    output.writeFloat(floatArray[i]);
                }
            } else if (componentType == Double.TYPE) {
                double[] doubleArray = (double[]) array;
                output.writeInt(doubleArray.length);
                for (int i = 0; i < doubleArray.length; i++) {
                    output.writeDouble(doubleArray[i]);
                }
            } else {
                throw new InvalidClassException(
                        org.apache.harmony.luni.util.Msg.getString(
                                "K00d7", arrayClass.getName())); //$NON-NLS-1$
            }
        } else {
            // Array of Objects
            Object[] objectArray = (Object[]) array;
            output.writeInt(objectArray.length);
            for (int i = 0; i < objectArray.length; i++) {
                writeObject(objectArray[i]);
            }
        }
        return handle;
    }

    /**
     * Write class <code>object</code> into the receiver. It is assumed the
     * class has not been dumped yet. Classes are not really dumped, but a class
     * descriptor (<code>ObjectStreamClass</code>) that corresponds to them.
     * Return an <code>Integer</code> that represents the handle for this
     * object (class) which is dumped here.
     * 
     * @param object
     *            The <code>java.lang.Class</code> object to dump
     * @return the handle assigned to the class being dumped
     * 
     * @throws IOException
     *             If an IO exception happened when writing the class.
     */
    private Integer writeNewClass(Class<?> object, boolean unshared)
            throws IOException {
        output.writeByte(TC_CLASS);

        // Instances of java.lang.Class are always Serializable, even if their
        // instances aren't (e.g. java.lang.Object.class).
        // We cannot call lookup because it returns null if the parameter
        // represents instances that cannot be serialized, and that is not what
        // we want.

        // The handle for the classDesc is NOT the handle for the class object
        // being dumped. We must allocate a new handle and return it.
        if (object.isEnum()) {
            writeEnumDesc(object, unshared);
        } else {
            writeClassDesc(ObjectStreamClass.lookupStreamClass(object),
                    unshared);
        }

        Integer previousHandle = objectsWritten.get(object);
        Integer handle = registerObjectWritten(object);
        if (unshared) {
            // remove reference to unshared object
            removeUnsharedReference(object, previousHandle);
        }

        return handle;
    }

    /**
     * Write class descriptor <code>classDesc</code> into the receiver. It is
     * assumed the class descriptor has not been dumped yet. The class
     * descriptors for the superclass chain will be dumped as well. Return an
     * <code>Integer</code> that represents the handle for this object (class
     * descriptor) which is dumped here.
     * 
     * @param classDesc
     *            The <code>ObjectStreamClass</code> object to dump
     * 
     * @throws IOException
     *             If an IO exception happened when writing the class
     *             descriptor.
     */
    private void writeNewClassDesc(ObjectStreamClass classDesc)
            throws IOException {
        output.writeUTF(classDesc.getName());
        output.writeLong(classDesc.getSerialVersionUID());
        byte flags = classDesc.getFlags();
        boolean externalizable = false;
        externalizable = ObjectStreamClass.isExternalizable(classDesc
                .forClass());
        if (protocolVersion != PROTOCOL_VERSION_1) {
            // Change for 1.2. Objects can be saved in old format
            // (PROTOCOL_VERSION_1) or in the 1.2 format (PROTOCOL_VERSION_2).
            // Nested "if" check to optimize checking. Second check is more
            // expensive.
            if (externalizable) {
                flags |= SC_BLOCK_DATA;
            }
        }
        output.writeByte(flags);
        if ((SC_ENUM | SC_SERIALIZABLE) != classDesc.getFlags()) {
            writeFieldDescriptors(classDesc, externalizable);
        } else {
            // enum write no fields
            output.writeShort(0);
        }
    }

    /**
     * Write class descriptor <code>classDesc</code> into the receiver.
     * 
     * @param classDesc
     *            The <code>ObjectStreamClass</code> object to dump
     * 
     * @throws IOException
     *             If an IO exception happened when writing the class
     *             descriptor.
     */
    protected void writeClassDescriptor(ObjectStreamClass classDesc)
            throws IOException {
        writeNewClassDesc(classDesc);
    }

    /**
     * Write exception <code>ex</code> into the receiver. It is assumed the
     * exception has not been dumped yet. Return an <code>Integer</code> that
     * represents the handle for this object (exception) which is dumped here.
     * This is used to dump the exception instance that happened (if any) when
     * dumping the original object graph. The set of seen objects will be reset
     * just before and just after dumping this exception object.
     * 
     * When exceptions are found normally in the object graph, they are dumped
     * as a regular object, and not by this method. In that case, the set of
     * "known objects" is not reset.
     * 
     * @param ex
     *            Exception object to dump
     * 
     * @throws IOException
     *             If an IO exception happened when writing the exception
     *             object.
     */
    private void writeNewException(Exception ex) throws IOException {
        output.writeByte(TC_EXCEPTION);
        resetSeenObjects();
        writeObjectInternal(ex, false, false, false); // No replacements
        resetSeenObjects();
    }

    /**
     * Write object <code>object</code> of class <code>theClass</code> into
     * the receiver. It is assumed the object has not been dumped yet. Return an
     * <code>Integer</code> that represents the handle for this object which
     * is dumped here.
     * 
     * If the object implements <code>Externalizable</code> its
     * <code>writeExternal</code> is called. Otherwise, all fields described
     * by the class hierarchy is dumped. Each class can define how its declared
     * instance fields are dumped by defining a private method
     * <code>writeObject</code>
     * 
     * @param object
     *            The object to dump
     * @param theClass
     *            A <code>java.lang.Class</code> representing the class of the
     *            object
     * @param unshared
     *            Write the object unshared
     * @return the handle assigned to the object
     * 
     * @throws IOException
     *             If an IO exception happened when writing the object.
     */
    private Integer writeNewObject(Object object, Class<?> theClass,
            boolean unshared) throws IOException {
        // Not String, not null, not array, not cyclic reference

        EmulatedFieldsForDumping originalCurrentPutField = currentPutField; // save
        currentPutField = null; // null it, to make sure one will be computed if
        // needed

        boolean externalizable = ObjectStreamClass.isExternalizable(theClass);
        boolean serializable = ObjectStreamClass.isSerializable(theClass);
        if (!externalizable && !serializable) {
            // Object is neither externalizable nor serializable. Error
            throw new NotSerializableException(theClass.getName());
        }

        // Either serializable or externalizable, now we can save info
        output.writeByte(TC_OBJECT);
        writeClassDescForClass(theClass);
        Integer previousHandle = objectsWritten.get(object);
        Integer handle = registerObjectWritten(object);

        // This is how we know what to do in defaultWriteObject. And it is also
        // used by defaultWriteObject to check if it was called from an invalid
        // place.
        // It allows writeExternal to call defaultWriteObject and have it work.
        currentObject = object;
        currentClass = ObjectStreamClass.lookup(theClass);
        try {
            if (externalizable) {
                boolean noBlockData = protocolVersion == PROTOCOL_VERSION_1;
                if (noBlockData) {
                    primitiveTypes = output;
                }
                // Object is externalizable, just call its own method
                ((Externalizable) object).writeExternal(this);
                if (noBlockData) {
                    primitiveTypes = null;
                } else {
                    // Similar to the code in writeHierarchy when object
                    // implements writeObject.
                    // Any primitive data has to be flushed and a tag must be
                    // written
                    drain();
                    output.writeByte(TC_ENDBLOCKDATA);
                }
            } else { // If it got here, it has to be Serializable
                // Object is serializable. Walk the class chain writing the
                // fields
                writeHierarchy(object, currentClass);
            }
        } finally {
            // Cleanup, needs to run always so that we can later detect invalid
            // calls to defaultWriteObject
            if (unshared) {
                // remove reference to unshared object
                removeUnsharedReference(object, previousHandle);
            }
            currentObject = null;
            currentClass = null;
            currentPutField = originalCurrentPutField;
        }

        return handle;
    }

    /**
     * Write String <code>object</code> into the receiver. It is assumed the
     * String has not been dumped yet. Return an <code>Integer</code> that
     * represents the handle for this object (String) which is dumped here.
     * Strings are saved in UTF format.
     * 
     * @param object
     *            The <code>java.lang.String</code> object to dump
     * @return the handle assigned to the String being dumped
     * 
     * @throws IOException
     *             If an IO exception happened when writing the String.
     */
    private Integer writeNewString(String object, boolean unshared)
            throws IOException {
        long count = output.countUTFBytes(object);
        if (count <= 0xffff) {
            output.writeByte(TC_STRING);
            output.writeShort((short) count);
        } else {
            output.writeByte(TC_LONGSTRING);
            output.writeLong(count);
        }
        output.writeUTFBytes(object, count);

        Integer previousHandle = objectsWritten.get(object);
        Integer handle = registerObjectWritten(object);
        if (unshared) {
            // remove reference to unshared object
            removeUnsharedReference(object, previousHandle);
        }
        return handle;
    }

    /**
     * Write a special tag that indicates the value <code>null</code> into the
     * receiver.
     * 
     * @throws IOException
     *             If an IO exception happened when writing the tag for
     *             <code>null</code>.
     */
    private void writeNull() throws IOException {
        output.writeByte(TC_NULL);
    }

    /**
     * Write object <code>object</code> into the receiver's underlying stream.
     * 
     * @param object
     *            The object to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the object
     * 
     * @see ObjectInputStream#readObject()
     */
    public final void writeObject(Object object) throws IOException {
        writeObject(object, false);
    }

    /**
     * Write object <code>object</code> into the receiver's underlying stream
     * unshared with previously written identical objects.
     * 
     * @param object
     *            The object to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the object
     * 
     * @see ObjectInputStream#readObject()
     */
    public void writeUnshared(Object object) throws IOException {
        writeObject(object, true);
    }

    private void writeObject(Object object, boolean unshared)
            throws IOException {
        boolean setOutput = (primitiveTypes == output);
        if (setOutput) {
            primitiveTypes = null;
        }
        // This is the spec'ed behavior in JDK 1.2. Very bizarre way to allow
        // behavior overriding.
        if (subclassOverridingImplementation && !unshared) {
            writeObjectOverride(object);
        } else {

            try {
                // First we need to flush primitive types if they were written
                drain();
                // Actual work, and class-based replacement should be computed
                // if needed.
                writeObjectInternal(object, unshared, true, true);
                if (setOutput) {
                    primitiveTypes = output;
                }
            } catch (IOException ioEx1) {
                // This will make it pass through until the top caller. It also
                // lets it pass through the nested exception.
                if (nestedLevels == 0 && ioEx1 != nestedException) {
                    try {
                        writeNewException(ioEx1);
                    } catch (IOException ioEx2) {
                        nestedException.fillInStackTrace();
                        throw nestedException;
                    }
                }
                throw ioEx1; // and then we propagate the original exception
            }
        }
    }

    /**
     * Write object <code>object</code> into the receiver's underlying stream.
     * 
     * @param object
     *            The object to write
     * @param unshared
     *            Write the object unshared
     * @param computeClassBasedReplacement
     *            A boolean indicating if class-based replacement should be
     *            computed (if supported) for the object.
     * @param computeStreamReplacement
     *            A boolean indicating if stream-based replacement should be
     *            computed (if supported) for the object.
     * @return the handle assigned to the final object being dumped
     * 
     * @throws IOException
     *             If an IO exception happened when writing the object
     * 
     * @see ObjectInputStream#readObject()
     */
    private Integer writeObjectInternal(Object object, boolean unshared,
            boolean computeClassBasedReplacement,
            boolean computeStreamReplacement) throws IOException {

        if (object == null) {
            writeNull();
            return null;
        }
        Integer handle = null;
        if (!unshared) {
            handle = dumpCycle(object);
            if (handle != null) {
                return handle; // cyclic reference
            }
        }

        // Non-null object, first time seen...
        Class<?> objClass = object.getClass();
        nestedLevels++;
        try {

            if (!(enableReplace && computeStreamReplacement)) {
                // Is it a Class ?
                if (objClass == ObjectStreamClass.CLASSCLASS) {
                    return writeNewClass((Class<?>) object, unshared);
                }
                // Is it an ObjectStreamClass ?
                if (objClass == ObjectStreamClass.OBJECTSTREAMCLASSCLASS) {
                    return writeClassDesc((ObjectStreamClass) object, unshared);
                }
            }

            if (ObjectStreamClass.isSerializable(object.getClass())
                    && computeClassBasedReplacement) {
                Object writeReplaceMethod = writeReplaceCache.get(objClass);
                if (writeReplaceMethod != this) {
                    if (writeReplaceMethod == null) {
                        final Method writeReplace = ObjectStreamClass
                                .methodWriteReplace(objClass);
                        if (writeReplace == null) {
                            writeReplaceCache.put(objClass, this);
                            writeReplaceMethod = null;
                        } else {
                            // Has replacement method
                            AccessController
                                    .doPrivileged(new PriviAction<Object>(
                                            writeReplace));
                            writeReplaceCache.put(objClass, writeReplace);
                            writeReplaceMethod = writeReplace;
                        }
                    }
                    if (writeReplaceMethod != null) {
                        Object classBasedReplacement;
                        try {
                            classBasedReplacement = ((Method) writeReplaceMethod)
                                    .invoke(object, (Object[]) null);
                        } catch (IllegalAccessException iae) {
                            classBasedReplacement = object;
                        } catch (InvocationTargetException ite) {
                            // WARNING - Not sure this is the right thing to do
                            // if we can't run the method
                            Throwable target = ite.getTargetException();
                            if (target instanceof ObjectStreamException) {
                                throw (ObjectStreamException) target;
                            } else if (target instanceof Error) {
                                throw (Error) target;
                            } else {
                                throw (RuntimeException) target;
                            }
                        }
                        if (classBasedReplacement != object) {
                            // All over, class-based replacement off this time.
                            Integer replacementHandle = writeObjectInternal(
                                    classBasedReplacement, false, false,
                                    computeStreamReplacement);
                            // Make the original object also map to the same
                            // handle.
                            if (replacementHandle != null) {
                                registerObjectWritten(object, replacementHandle);
                            }
                            return replacementHandle;
                        }
                    }
                }
            }

            // We get here either if class-based replacement was not needed or
            // if it was needed but produced the same object or if it could not
            // be computed.
            if (enableReplace && computeStreamReplacement) {
                // Now we compute the stream-defined replacement.
                Object streamReplacement = replaceObject(object);
                if (streamReplacement != object) {
                    // All over, class-based replacement off this time.
                    Integer replacementHandle = writeObjectInternal(
                            streamReplacement, false,
                            computeClassBasedReplacement, false);
                    // Make the original object also map to the same handle.
                    if (replacementHandle != null) {
                        registerObjectWritten(object, replacementHandle);
                    }
                    return replacementHandle;
                }
            }

            // We get here if stream-based replacement produced the same object

            // Is it a Class ?
            if (objClass == ObjectStreamClass.CLASSCLASS) {
                return writeNewClass((Class<?>) object, unshared);
            }

            // Is it an ObjectStreamClass ?
            if (objClass == ObjectStreamClass.OBJECTSTREAMCLASSCLASS) {
                return writeClassDesc((ObjectStreamClass) object, unshared);
            }

            // Is it a String ? (instanceof, but == is faster)
            if (objClass == ObjectStreamClass.STRINGCLASS) {
                return writeNewString((String) object, unshared);
            }

            // Is it an Array ?
            if (objClass.isArray()) {
                return writeNewArray(object, objClass, objClass
                        .getComponentType(), unshared);
            }

            if (object instanceof Enum) {
                return writeNewEnum(object, objClass, unshared);
            }

            // Not a String or Class or Array. Default procedure.
            return writeNewObject(object, objClass, unshared);
        } finally {
            nestedLevels--;
        }
    }

    // write for Enum Class Desc only, which is different from other classes
    private ObjectStreamClass writeEnumDesc(Class<?> theClass, boolean unshared)
            throws IOException {
        // write classDesc, classDesc for enum is different
        ObjectStreamClass classDesc = ObjectStreamClass.lookup(theClass);
        // set flag for enum, the flag is (SC_SERIALIZABLE | SC_ENUM)
        classDesc.setFlags((byte) (SC_SERIALIZABLE | SC_ENUM));
        Integer previousHandle = objectsWritten.get(classDesc);
        Integer handle = null;
        if (!unshared) {
            handle = dumpCycle(classDesc);
        }
        if (handle == null) {
            Class<?> classToWrite = classDesc.forClass();
            // If we got here, it is a new (non-null) classDesc that will have
            // to be registered as well
            registerObjectWritten(classDesc);

            output.writeByte(TC_CLASSDESC);
            if (protocolVersion == PROTOCOL_VERSION_1) {
                writeNewClassDesc(classDesc);
            } else {
                // So write...() methods can be used by
                // subclasses during writeClassDescriptor()
                primitiveTypes = output;
                writeClassDescriptor(classDesc);
                primitiveTypes = null;
            }
            // Extra class info (optional)
            annotateClass(classToWrite);
            drain(); // flush primitive types in the annotation
            output.writeByte(TC_ENDBLOCKDATA);
            // write super class
            ObjectStreamClass superClass = classDesc.getSuperclass();
            if (null != superClass) {
                // super class is also enum
                superClass.setFlags((byte) (SC_SERIALIZABLE | SC_ENUM));
                writeEnumDesc(superClass.forClass(), unshared);
            } else {
                output.writeByte(TC_NULL);
            }
            if (unshared) {
                // remove reference to unshared object
                removeUnsharedReference(classDesc, previousHandle);
            }
        }
        return classDesc;
    }

    private Integer writeNewEnum(Object object, Class<?> theClass,
            boolean unshared) throws IOException {
        // write new Enum
        EmulatedFieldsForDumping originalCurrentPutField = currentPutField; // save
        // null it, to make sure one will be computed if needed
        currentPutField = null;

        output.writeByte(TC_ENUM);
        while (theClass != null && !theClass.isEnum()) {
            // write enum only
            theClass = theClass.getSuperclass();
        }
        ObjectStreamClass classDesc = writeEnumDesc(theClass, unshared);

        Integer previousHandle = objectsWritten.get(object);
        Integer handle = registerObjectWritten(object);

        ObjectStreamField[] fields = classDesc.getSuperclass().fields();
        Class<?> declaringClass = classDesc.getSuperclass().forClass();
        // Only write field "name" for enum class, which is the second field of
        // enum, that is fields[1]. Ignore all non-fields and fields.length < 2
        if (null != fields && fields.length > 1) {
            String str = (String) getFieldObj(object, declaringClass, fields[1]
                    .getName(), fields[1].getTypeString());
            Integer strhandle = null;
            if (!unshared) {
                strhandle = dumpCycle(str);
            }
            if (null == strhandle) {
                writeNewString(str, unshared);
            }
        }

        if (unshared) {
            // remove reference to unshared object
            removeUnsharedReference(object, previousHandle);
        }
        currentPutField = originalCurrentPutField;
        return handle;
    }

    /**
     * Method to be overridden by subclasses to write <code>object</code> into
     * the receiver's underlying stream.
     * 
     * @param object
     *            the object
     * 
     * @throws IOException
     *             If an IO exception happened when writing the object
     */
    protected void writeObjectOverride(Object object) throws IOException {
        if (!subclassOverridingImplementation) {
            // Subclasses must override.
            throw new IOException();
        }
    }

    /**
     * Write primitive data of type short (<code>value</code>)into the
     * receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeShort(int value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeShort(value);
    }

    /**
     * Writes the ObjectOutputStream header into the underlying stream.
     * 
     * @throws IOException
     *             If an IO exception happened when writing the stream header.
     */
    protected void writeStreamHeader() throws IOException {
        output.writeShort(STREAM_MAGIC);
        output.writeShort(STREAM_VERSION);
    }

    /**
     * Write primitive data of type String (<code>value</code>) in UTF
     * format into the receiver's underlying stream.
     * 
     * @param value
     *            The primitive data to write
     * 
     * @throws IOException
     *             If an IO exception happened when writing the primitive data.
     */
    public void writeUTF(String value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeUTF(value);
    }
}
