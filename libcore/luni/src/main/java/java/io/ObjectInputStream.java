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

import java.io.EmulatedFields.ObjectSlot;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;

// BEGIN android-added
import dalvik.system.VMStack;
// END android-added

import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.PriviAction;

/**
 * An ObjectInputStream can be used to load Java objects from a stream where the
 * objects were saved using an ObjectOutputStream. Primitive data (ints, bytes,
 * chars, etc) can also be loaded if the data was saved as primitive types as
 * well. It is invalid to attempt to read an object as primitive data.
 * 
 * @see ObjectOutputStream
 * @see ObjectInput
 * @see Serializable
 * @see Externalizable
 */
public class ObjectInputStream extends InputStream implements ObjectInput,
        ObjectStreamConstants {

    private static InputStream emptyStream = new ByteArrayInputStream(
            new byte[0]);

    // To put into objectsRead when reading unsharedObject
    private static final Object UNSHARED_OBJ = new Object(); // $NON-LOCK-1$

    // If the receiver has already read & not consumed a TC code
    private boolean hasPushbackTC;

    // Push back TC code if the variable above is true
    private byte pushbackTC;

    // How many nested levels to readObject. When we reach 0 we have to validate
    // the graph then reset it
    private int nestedLevels;

    // All objects are assigned an ID (integer handle)
    private int currentHandle;

    // Where we read from
    private DataInputStream input;

    // Where we read primitive types from
    private DataInputStream primitiveTypes;

    // Where we keep primitive type data
    private InputStream primitiveData = emptyStream;

    // Resolve object is a mechanism for replacement
    private boolean enableResolve;

    // Table mapping Integer (handle) -> Object
    private Hashtable<Integer, Object> objectsRead;

    // Used by defaultReadObject
    private Object currentObject;

    // Used by defaultReadObject
    private ObjectStreamClass currentClass;

    // All validations to be executed when the complete graph is read. See inner
    // type below.
    private InputValidationDesc[] validations;

    // Allows the receiver to decide if it needs to call readObjectOverride
    private boolean subclassOverridingImplementation;

    // Original caller's class loader, used to perform class lookups
    private ClassLoader callerClassLoader;

    // false when reading missing fields
    private boolean mustResolve = true;

    // Handle for the current class descriptor
    private Integer descriptorHandle;

    // cache for readResolve methods
    private IdentityHashMap<Class<?>, Object> readResolveCache;

    private static final Hashtable<String, Class<?>> PRIMITIVE_CLASSES = new Hashtable<String, Class<?>>();

    static {
        PRIMITIVE_CLASSES.put("byte", byte.class); //$NON-NLS-1$
        PRIMITIVE_CLASSES.put("short", short.class); //$NON-NLS-1$
        PRIMITIVE_CLASSES.put("int", int.class); //$NON-NLS-1$
        PRIMITIVE_CLASSES.put("long", long.class); //$NON-NLS-1$
        PRIMITIVE_CLASSES.put("boolean", boolean.class); //$NON-NLS-1$
        PRIMITIVE_CLASSES.put("char", char.class); //$NON-NLS-1$
        PRIMITIVE_CLASSES.put("float", float.class); //$NON-NLS-1$
        PRIMITIVE_CLASSES.put("double", double.class); //$NON-NLS-1$
    }

    // Internal type used to keep track of validators & corresponding priority
    static class InputValidationDesc {
        ObjectInputValidation validator;

        int priority;
    }

    /**
     * Inner class to provide access to serializable fields
     */
    public abstract static class GetField {
        /**
         * @return ObjectStreamClass
         */
        public abstract ObjectStreamClass getObjectStreamClass();

        /**
         * @param name
         * @return <code>true</code> if the default value is set,
         *         <code>false</code> otherwise
         * 
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public abstract boolean defaulted(String name) throws IOException,
                IllegalArgumentException;

        /**
         * @param name
         * @param defaultValue
         * @return the value
         * 
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public abstract boolean get(String name, boolean defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * @param name
         * @param defaultValue
         * @return the value
         * 
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public abstract char get(String name, char defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * @param name
         * @param defaultValue
         * @return the value
         * 
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public abstract byte get(String name, byte defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * @param name
         * @param defaultValue
         * @return the value
         * 
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public abstract short get(String name, short defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * @param name
         * @param defaultValue
         * @return the value
         * 
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public abstract int get(String name, int defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * @param name
         * @param defaultValue
         * @return the value
         * 
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public abstract long get(String name, long defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * @param name
         * @param defaultValue
         * @return the value
         * 
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public abstract float get(String name, float defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * @param name
         * @param defaultValue
         * @return the value
         * 
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public abstract double get(String name, double defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * @param name
         * @param defaultValue
         * @return the value
         * 
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public abstract Object get(String name, Object defaultValue)
                throws IOException, IllegalArgumentException;
    }

    /**
     * Constructs a new ObjectInputStream. The representation and proper
     * initialization is on the hands of subclasses.
     * 
     * @throws IOException
     *             If not called from a subclass
     * @throws SecurityException
     *             If subclasses are not allowed
     * 
     * @see SecurityManager#checkPermission(java.security.Permission)
     */
    protected ObjectInputStream() throws IOException, SecurityException {
        super();
        SecurityManager currentManager = System.getSecurityManager();
        if (currentManager != null) {
            currentManager.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        // WARNING - we should throw IOException if not called from a subclass
        // according to the JavaDoc. Add the test.
        this.subclassOverridingImplementation = true;
    }

    /**
     * Constructs a new ObjectInputStream on the InputStream <code>input</code>.
     * All reads are now filtered through this stream.
     * 
     * @param input
     *            The non-null InputStream to filter reads on.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the stream header.
     * @throws StreamCorruptedException
     *             If the underlying stream does not contain serialized objects
     *             that can be read.
     */
    public ObjectInputStream(InputStream input)
            throws StreamCorruptedException, IOException {
        final Class<?> implementationClass = getClass();
        final Class<?> thisClass = ObjectInputStream.class;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null && implementationClass != thisClass) {
            boolean mustCheck = (AccessController
                    .doPrivileged(new PrivilegedAction<Boolean>() {
                        public Boolean run() {
                            try {
                                Method method = implementationClass
                                        .getMethod(
                                                "readFields", //$NON-NLS-1$
                                                ObjectStreamClass.EMPTY_CONSTRUCTOR_PARAM_TYPES);
                                if (method.getDeclaringClass() != thisClass) {
                                    return Boolean.TRUE;
                                }
                            } catch (NoSuchMethodException e) {
                            }
                            try {
                                Method method = implementationClass
                                        .getMethod(
                                                "readUnshared", //$NON-NLS-1$
                                                ObjectStreamClass.EMPTY_CONSTRUCTOR_PARAM_TYPES);
                                if (method.getDeclaringClass() != thisClass) {
                                    return Boolean.TRUE;
                                }
                            } catch (NoSuchMethodException e) {
                            }
                            return Boolean.FALSE;
                        }
                    })).booleanValue();
            if (mustCheck) {
                sm
                        .checkPermission(ObjectStreamConstants.SUBCLASS_IMPLEMENTATION_PERMISSION);
            }
        }
        this.input = (input instanceof DataInputStream) ? (DataInputStream) input
                : new DataInputStream(input);
        primitiveTypes = new DataInputStream(this);
        enableResolve = false;
        this.subclassOverridingImplementation = false;
        this.readResolveCache = new IdentityHashMap<Class<?>, Object>();
        resetState();
        nestedLevels = 0;
        // So read...() methods can be used by
        // subclasses during readStreamHeader()
        primitiveData = this.input;
        // Has to be done here according to the specification
        readStreamHeader();
        primitiveData = emptyStream;
    }

    /**
     * Returns the number of bytes of primitive data available from the
     * receiver. It should not be used at any arbitrary position; just when
     * reading primitive data types (ints, chars, etc).
     * 
     * @return the number of available primitive data bytes
     * 
     * @throws IOException
     *             If any IO problem occurred when trying to compute the bytes
     *             available.
     */
    @Override
    public int available() throws IOException {
        // returns 0 if next data is an object, or N if reading primitive types
        checkReadPrimitiveTypes();
        return primitiveData.available();
    }

    /**
     * Checks to see if it is ok to read primitive types at this point from the
     * receiver. One is not supposed to read primitive types when about to read
     * an object, for example, so an exception has to be thrown.
     * 
     * @throws IOException
     *             If any IO problem occurred when trying to read primitive type
     *             or if it is illegal to read primitive types
     */
    private void checkReadPrimitiveTypes() throws IOException {
        // If we still have primitive data, it is ok to read primitive data
        if (primitiveData == input || primitiveData.available() > 0) {
            return;
        }

        // If we got here either we had no Stream previously created or
        // we no longer have data in that one, so get more bytes
        do {
            int next = 0;
            if (hasPushbackTC) {
                hasPushbackTC = false;
            } else {
                next = input.read();
                pushbackTC = (byte) next;
            }
            switch (pushbackTC) {
                case TC_BLOCKDATA:
                    primitiveData = new ByteArrayInputStream(readBlockData());
                    return;
                case TC_BLOCKDATALONG:
                    primitiveData = new ByteArrayInputStream(
                            readBlockDataLong());
                    return;
                case TC_RESET:
                    resetState();
                    break;
                default:
                    if (next != -1) {
                        pushbackTC();
                    }
                    return;
            }
            // Only TC_RESET falls through
        } while (true);
    }

    /**
     * Close this ObjectInputStream. This implementation closes the target
     * stream.
     * 
     * @throws IOException
     *             If an error occurs attempting to close this stream.
     */
    @Override
    public void close() throws IOException {
        input.close();
    }

    /**
     * Default method to read objects from the receiver. Fields defined in the
     * object's class and super classes (which are Serializable) will be read.
     * 
     * @throws IOException
     *             If an IO error occurs attempting to read the object data
     * @throws ClassNotFoundException
     *             If the class of the object cannot be found
     * @throws NotActiveException
     *             If this method is not called from readObject()
     * 
     * @see ObjectOutputStream#defaultWriteObject
     */
    public void defaultReadObject() throws IOException, ClassNotFoundException,
            NotActiveException {
        // We can't be called from just anywhere. There are rules.
        if (currentObject != null || !mustResolve) {
            readFieldValues(currentObject, currentClass);
        } else {
            throw new NotActiveException();
        }
    }

    /**
     * Enables/disables object replacement for the receiver. By default this is
     * not enabled. Only trusted subclasses (loaded with system class loader)
     * can override this behavior.
     * 
     * @param enable
     *            if true, enables replacement. If false, disables replacement.
     * @return the previous configuration (if it was enabled or disabled)
     * 
     * @throws SecurityException
     *             If the class of the receiver is not trusted
     * 
     * @see #resolveObject
     * @see ObjectOutputStream#enableReplaceObject
     */
    protected boolean enableResolveObject(boolean enable)
            throws SecurityException {
        if (enable) {
            // The Stream has to be trusted for this feature to be enabled.
            // trusted means the stream's classloader has to be null
            SecurityManager currentManager = System.getSecurityManager();
            if (currentManager != null) {
                currentManager.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        boolean originalValue = enableResolve;
        enableResolve = enable;
        return originalValue;
    }

    /**
     * Checks if two classes belong to the same package and returns true in the
     * positive case. Return false otherwise.
     * 
     * @param c1
     *            one of the classes to test
     * @param c2
     *            the other class to test
     * @return <code>true</code> if the two classes belong to the same
     *         package, <code>false</code> otherwise
     */
    private boolean inSamePackage(Class<?> c1, Class<?> c2) {
        String nameC1 = c1.getName();
        String nameC2 = c2.getName();
        int indexDotC1 = nameC1.lastIndexOf('.');
        int indexDotC2 = nameC2.lastIndexOf('.');
        if (indexDotC1 != indexDotC2) {
            return false; // cannot be in the same package if indices are not
        }
        // the same
        if (indexDotC1 < 0) {
            return true; // both of them are in default package
        }
        return nameC1.substring(0, indexDotC1).equals(
                nameC2.substring(0, indexDotC2));
    }

    /**
     * Create and return a new instance of class <code>instantiationClass</code>
     * but running the constructor defined in class
     * <code>constructorClass</code> (same as <code>instantiationClass</code>
     * or a superclass).
     * 
     * Has to be native to avoid visibility rules and to be able to have
     * <code>instantiationClass</code> not the same as
     * <code>constructorClass</code> (no such API in java.lang.reflect).
     * 
     * @param instantiationClass
     *            The new object will be an instance of this class
     * @param constructorClass
     *            The empty constructor to run will be in this class
     * @return the object created from <code>instantiationClass</code>
     */
    private static native Object newInstance(Class<?> instantiationClass,
            Class<?> constructorClass);

    /**
     * Return the next <code>int</code> handle to be used to indicate cyclic
     * references being loaded from the stream.
     * 
     * @return the next handle to represent the next cyclic reference
     */
    private int nextHandle() {
        return this.currentHandle++;
    }

    /**
     * Return the next token code (TC) from the receiver, which indicates what
     * kind of object follows
     * 
     * @return the next TC from the receiver
     * 
     * @throws IOException
     *             If an IO error occurs
     * 
     * @see ObjectStreamConstants
     */
    private byte nextTC() throws IOException {
        if (hasPushbackTC) {
            hasPushbackTC = false; // We are consuming it
        } else {
            // Just in case a later call decides to really push it back,
            // we don't require the caller to pass it as parameter
            pushbackTC = input.readByte();
        }
        return pushbackTC;
    }

    /**
     * Pushes back the last TC code read
     */
    private void pushbackTC() {
        hasPushbackTC = true;
    }

    /**
     * Reads a single byte from the receiver and returns the result as an int.
     * The low-order byte is returned or -1 of the end of stream was
     * encountered.
     * 
     * @return The byte read or -1 if end of stream.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    @Override
    public int read() throws IOException {
        checkReadPrimitiveTypes();
        return primitiveData.read();
    }

    /**
     * Reads at most <code>length</code> bytes from the receiver and stores
     * them in byte array <code>buffer</code> starting at offset
     * <code>offset</code>. Answer the number of bytes actually read or -1 if
     * no bytes were read and end of stream was encountered.
     * 
     * @param buffer
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param length
     *            the maximum number of bytes to store in <code>buffer</code>.
     * @return The number of bytes actually read or -1 if end of stream.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (buffer == null) {
            throw new NullPointerException();
        }
        // avoid int overflow
        if (offset < 0 || offset > buffer.length || length < 0
                || length > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }
        checkReadPrimitiveTypes();
        return primitiveData.read(buffer, offset, length);
    }

    /**
     * Reads and returns an array of raw bytes with primitive data. The array
     * will have up to 255 bytes. The primitive data will be in the format
     * described by <code>DataOutputStream</code>.
     * 
     * @return The primitive data read, as raw bytes
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    private byte[] readBlockData() throws IOException {
        byte[] result = new byte[input.readByte() & 0xff];
        input.readFully(result);
        return result;
    }

    /**
     * Reads and returns an array of raw bytes with primitive data. The array
     * will have more than 255 bytes. The primitive data will be in the format
     * described by <code>DataOutputStream</code>.
     * 
     * @return The primitive data read, as raw bytes
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    private byte[] readBlockDataLong() throws IOException {
        byte[] result = new byte[input.readInt()];
        input.readFully(result);
        return result;
    }

    /**
     * Reads and returns primitive data of type boolean read from the receiver
     * 
     * @return A boolean saved as primitive data using
     *         <code>ObjectOutputStream.writeBoolean()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public boolean readBoolean() throws IOException {
        return primitiveTypes.readBoolean();
    }

    /**
     * Reads and returns primitive data of type byte read from the receiver
     * 
     * @return A byte saved as primitive data using
     *         <code>ObjectOutputStream.writeByte()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public byte readByte() throws IOException {
        return primitiveTypes.readByte();
    }

    /**
     * Reads and returns primitive data of type char read from the receiver
     * 
     * @return A char saved as primitive data using
     *         <code>ObjectOutputStream.writeChar()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public char readChar() throws IOException {
        return primitiveTypes.readChar();
    }

    /**
     * Reads and discards block data and objects until TC_ENDBLOCKDATA is found.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the optional class
     *             annotation.
     * @throws ClassNotFoundException
     *             If the class corresponding to the class descriptor could not
     *             be found.
     */
    private void discardData() throws ClassNotFoundException, IOException {
        primitiveData = emptyStream;
        boolean resolve = mustResolve;
        mustResolve = false;
        do {
            byte tc = nextTC();
            if (tc == TC_ENDBLOCKDATA) {
                mustResolve = resolve;
                return; // End of annotation
            }
            readContent(tc);
        } while (true);
    }

    /**
     * Reads a class descriptor (an <code>ObjectStreamClass</code>) from the
     * stream.
     * 
     * @return the class descriptor read from the stream
     * 
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If the class corresponding to the class descriptor could not
     *             be found.
     */
    private ObjectStreamClass readClassDesc() throws ClassNotFoundException,
            IOException {
        byte tc = nextTC();
        switch (tc) {
            case TC_CLASSDESC:
                return readNewClassDesc(false);
            case TC_PROXYCLASSDESC:
                Class<?> proxyClass = readNewProxyClassDesc();
                ObjectStreamClass streamClass = ObjectStreamClass
                        .lookup(proxyClass);
                streamClass.setLoadFields(new ObjectStreamField[0]);
                registerObjectRead(streamClass, Integer.valueOf(nextHandle()),
                        false);
                checkedSetSuperClassDesc(streamClass, readClassDesc());
                return streamClass;
            case TC_REFERENCE:
                return (ObjectStreamClass) readCyclicReference();
            case TC_NULL:
                return null;
            default:
                throw new StreamCorruptedException(Msg.getString(
                        "K00d2", Integer.toHexString(tc & 0xff))); //$NON-NLS-1$
        }
    }

    /**
     * Reads the content of the receiver based on the previously read token
     * <code>tc</code>.
     * 
     * @param tc
     *            The token code for the next item in the stream
     * @return the object read from the stream
     * 
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If the class corresponding to the object being read could not
     *             be found.
     */
    private Object readContent(byte tc) throws ClassNotFoundException,
            IOException {
        switch (tc) {
            case TC_BLOCKDATA:
                return readBlockData();
            case TC_BLOCKDATALONG:
                return readBlockDataLong();
            case TC_CLASS:
                return readNewClass(false);
            case TC_CLASSDESC:
                return readNewClassDesc(false);
            case TC_ARRAY:
                return readNewArray(false);
            case TC_OBJECT:
                return readNewObject(false);
            case TC_STRING:
                return readNewString(false);
            case TC_LONGSTRING:
                return readNewLongString(false);
            case TC_REFERENCE:
                return readCyclicReference();
            case TC_NULL:
                return null;
            case TC_EXCEPTION:
                Exception exc = readException();
                throw new WriteAbortedException(Msg.getString("K00d3"), exc); //$NON-NLS-1$
            case TC_RESET:
                resetState();
                return null;
            default:
                throw new StreamCorruptedException(Msg.getString(
                        "K00d2", Integer.toHexString(tc & 0xff))); //$NON-NLS-1$
        }
    }

    /**
     * Reads the content of the receiver based on the previously read token
     * <code>tc</code>. Primitive data content is considered an error.
     * 
     * @param unshared
     *            read the object unshared
     * @return the object read from the stream
     * 
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If the class corresponding to the object being read could not
     *             be found.
     */
    private Object readNonPrimitiveContent(boolean unshared)
            throws ClassNotFoundException, IOException {
        checkReadPrimitiveTypes();
        if (primitiveData.available() > 0) {
            OptionalDataException e = new OptionalDataException();
            e.length = primitiveData.available();
            throw e;
        }

        do {
            byte tc = nextTC();
            switch (tc) {
                case TC_CLASS:
                    return readNewClass(unshared);
                case TC_CLASSDESC:
                    return readNewClassDesc(unshared);
                case TC_ARRAY:
                    return readNewArray(unshared);
                case TC_OBJECT:
                    return readNewObject(unshared);
                case TC_STRING:
                    return readNewString(unshared);
                case TC_LONGSTRING:
                    return readNewLongString(unshared);
                case TC_ENUM:
                    return readEnum(unshared);
                case TC_REFERENCE:
                    if (unshared) {
                        readNewHandle();
                        throw new InvalidObjectException(Msg.getString("KA002")); //$NON-NLS-1$
                    }
                    return readCyclicReference();
                case TC_NULL:
                    return null;
                case TC_EXCEPTION:
                    Exception exc = readException();
                    throw new WriteAbortedException(Msg.getString("K00d3"), exc); //$NON-NLS-1$
                case TC_RESET:
                    resetState();
                    break;
                case TC_ENDBLOCKDATA: // Can occur reading class annotation
                    pushbackTC();
                    OptionalDataException e = new OptionalDataException();
                    e.eof = true;
                    throw e;
                default:
                    throw new StreamCorruptedException(Msg.getString(
                            "K00d2", Integer.toHexString(tc & 0xff))); //$NON-NLS-1$
            }
            // Only TC_RESET falls through
        } while (true);
    }

    /**
     * Reads the next item from the stream assuming it is a cyclic reference to
     * an object previously read. Return the actual object previously read.
     * 
     * @return the object previously read from the stream
     * 
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws InvalidObjectException
     *             If the cyclic reference is not valid.
     */
    private Object readCyclicReference() throws InvalidObjectException,
            IOException {
        return registeredObjectRead(readNewHandle());
    }

    /**
     * Reads and returns primitive data of type double read from the receiver
     * 
     * @return A double saved as primitive data using
     *         <code>ObjectOutputStream.writeDouble()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public double readDouble() throws IOException {
        return primitiveTypes.readDouble();
    }

    /**
     * Read the next item assuming it is an exception. The exception is not a
     * regular instance in the object graph, but the exception instance that
     * happened (if any) when dumping the original object graph. The set of seen
     * objects will be reset just before and just after loading this exception
     * object.
     * <p>
     * When exceptions are found normally in the object graph, they are loaded
     * as a regular object, and not by this method. In that case, the set of
     * "known objects" is not reset.
     * 
     * @return the exception read
     * 
     * @throws IOException
     *             If an IO exception happened when reading the exception
     *             object.
     * @throws ClassNotFoundException
     *             If a class could not be found when reading the object graph
     *             for the exception
     * @throws OptionalDataException
     *             If optional data could not be found when reading the
     *             exception graph
     * @throws WriteAbortedException
     *             If another exception was caused when dumping this exception
     */
    private Exception readException() throws WriteAbortedException,
            OptionalDataException, ClassNotFoundException, IOException {

        resetSeenObjects();

        // Now we read the Throwable object that was saved
        // WARNING - the grammar says it is a Throwable, but the
        // WriteAbortedException constructor takes an Exception. So, we read an
        // Exception from the stream
        Exception exc = (Exception) readObject();

        // We reset the receiver's state (the grammar has "reset" in normal
        // font)
        resetSeenObjects();
        return exc;
    }

    /**
     * Reads a collection of field descriptors (name, type name, etc) for the
     * class descriptor <code>cDesc</code> (an <code>ObjectStreamClass</code>)
     * 
     * @param cDesc
     *            The class descriptor (an <code>ObjectStreamClass</code>)
     *            for which to write field information
     * 
     * @throws IOException
     *             If an IO exception happened when reading the field
     *             descriptors.
     * @throws ClassNotFoundException
     *             If a class for one of the field types could not be found
     * 
     * @see #readObject()
     */
    private void readFieldDescriptors(ObjectStreamClass cDesc)
            throws ClassNotFoundException, IOException {
        short numFields = input.readShort();
        ObjectStreamField[] fields = new ObjectStreamField[numFields];

        // We set it now, but each element will be inserted in the array further
        // down
        cDesc.setLoadFields(fields);

        // Check ObjectOutputStream.writeFieldDescriptors
        for (short i = 0; i < numFields; i++) {
            char typecode = (char) input.readByte();
            String fieldName = input.readUTF();
            boolean isPrimType = ObjectStreamClass.isPrimitiveType(typecode);
            String classSig;
            if (isPrimType) {
                classSig = String.valueOf(typecode);
            } else {
                // The spec says it is a UTF, but experience shows they dump
                // this String using writeObject (unlike the field name, which
                // is saved with writeUTF).
                // And if resolveObject is enabled, the classSig may be modified
                // so that the original class descriptor cannot be read
                // properly, so it is disabled.
                boolean old = enableResolve;
                try {
                    enableResolve = false;
                    classSig = (String) readObject();
                } finally {
                    enableResolve = old;
                }
            }
            ObjectStreamField f = new ObjectStreamField(classSig, fieldName);
            fields[i] = f;
        }
    }

    /**
     * Reads the fields of the object being read from the stream. The stream
     * will use the currently active <code>getField</code> object, allowing
     * users to load emulated fields, for cross-loading compatibility when a
     * class definition changes.
     * 
     * @return the fields being read
     * 
     * @throws IOException
     *             If an IO exception happened
     * @throws ClassNotFoundException
     *             If a class of an object being de-serialized can not be found
     * @throws NotActiveException
     *             If there is no object currently being loaded (invalid to call
     *             this method)
     */
    public GetField readFields() throws IOException, ClassNotFoundException,
            NotActiveException {
        // We can't be called from just anywhere. There are rules.
        if (currentObject == null) {
            throw new NotActiveException();
        }
        EmulatedFieldsForLoading result = new EmulatedFieldsForLoading(
                currentClass);
        readFieldValues(result);
        return result;
    }

    /**
     * Reads a collection of field values for the emulated fields
     * <code>emulatedFields</code>
     * 
     * @param emulatedFields
     *            an <code>EmulatedFieldsForLoading</code>, concrete subclass
     *            of <code>GetField</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the field values.
     * @throws InvalidClassException
     *             If an incompatible type is being assigned to an emulated
     *             field.
     * @throws OptionalDataException
     *             If optional data could not be found when reading the
     *             exception graph
     * 
     * @see #readFields
     * @see #readObject()
     */
    private void readFieldValues(EmulatedFieldsForLoading emulatedFields)
            throws OptionalDataException, InvalidClassException, IOException {
        EmulatedFields.ObjectSlot[] slots = emulatedFields.emulatedFields()
                .slots();
        for (ObjectSlot element : slots) {
            element.defaulted = false;
            Class<?> type = element.field.getType();
            if (type == Integer.TYPE) {
                element.fieldValue = Integer.valueOf(input.readInt());
            } else if (type == Byte.TYPE) {
                element.fieldValue = Byte.valueOf(input.readByte());
            } else if (type == Character.TYPE) {
                element.fieldValue = Character.valueOf(input.readChar());
            } else if (type == Short.TYPE) {
                element.fieldValue = Short.valueOf(input.readShort());
            } else if (type == Boolean.TYPE) {
                element.fieldValue = Boolean.valueOf(input.readBoolean());
            } else if (type == Long.TYPE) {
                element.fieldValue = Long.valueOf(input.readLong());
            } else if (type == Float.TYPE) {
                element.fieldValue = Float.valueOf(input.readFloat());
            } else if (type == Double.TYPE) {
                element.fieldValue = Double.valueOf(input.readDouble());
            } else {
                // Either array or Object
                try {
                    element.fieldValue = readObject();
                } catch (ClassNotFoundException cnf) {
                    // WARNING- Not sure this is the right thing to do. Write
                    // test case.
                    throw new InvalidClassException(cnf.toString());
                }
            }
        }
    }

    /**
     * Reads a collection of field values for the class descriptor
     * <code>classDesc</code> (an <code>ObjectStreamClass</code>). The
     * values will be used to set instance fields in object <code>obj</code>.
     * This is the default mechanism, when emulated fields (an
     * <code>GetField</code>) are not used. Actual values to load are stored
     * directly into the object <code>obj</code>.
     * 
     * @param obj
     *            Instance in which the fields will be set.
     * @param classDesc
     *            A class descriptor (an <code>ObjectStreamClass</code>)
     *            defining which fields should be loaded.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the field values.
     * @throws InvalidClassException
     *             If an incompatible type is being assigned to an emulated
     *             field.
     * @throws OptionalDataException
     *             If optional data could not be found when reading the
     *             exception graph
     * @throws ClassNotFoundException
     *             If a class of an object being de-serialized can not be found
     * 
     * @see #readFields
     * @see #readObject()
     */
    private void readFieldValues(Object obj, ObjectStreamClass classDesc)
            throws OptionalDataException, ClassNotFoundException, IOException {
        // Now we must read all fields and assign them to the receiver
        ObjectStreamField[] fields = classDesc.getLoadFields();
        fields = (null == fields ? new ObjectStreamField[] {} : fields);
        Class<?> declaringClass = classDesc.forClass();
        if (declaringClass == null && mustResolve) {
            throw new ClassNotFoundException(classDesc.getName());
        }

        for (ObjectStreamField fieldDesc : fields) {
            // Code duplication starts, just because Java is typed
            if (fieldDesc.isPrimitive()) {
                try {
                    switch (fieldDesc.getTypeCode()) {
                        case 'B':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readByte());
                            break;
                        case 'C':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readChar());
                            break;
                        case 'D':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readDouble());
                            break;
                        case 'F':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readFloat());
                            break;
                        case 'I':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readInt());
                            break;
                        case 'J':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readLong());
                            break;
                        case 'S':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readShort());
                            break;
                        case 'Z':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readBoolean());
                            break;
                        default:
                            throw new StreamCorruptedException(Msg.getString(
                                    "K00d5", fieldDesc.getTypeCode())); //$NON-NLS-1$
                    }
                } catch (NoSuchFieldError err) {
                }
            } else {
                // Object type (array included).
                String fieldName = fieldDesc.getName();
                boolean setBack = false;
                ObjectStreamField field = classDesc.getField(fieldName);
                if (mustResolve && field == null) {
                    setBack = true;
                    mustResolve = false;
                }
                Object toSet;
                if (field != null && field.isUnshared()) {
                    toSet = readUnshared();
                } else {
                    toSet = readObject();
                }
                if (setBack) {
                    mustResolve = true;
                }
                if (field != null) {
                    if (toSet != null) {
                        // BEGIN android-removed
                        // Class<?> fieldType = field.getType();
                        // END android-removed
                        // BEGIN android-added
                        // Originally getTypeInternal() was called getType().
                        // After the semantics of getType() changed inside
                        // Harmony, the check below wasn't adjusted and didn't
                        // work anymore.
                        Class<?> fieldType = field.getTypeInternal();
                        // END android-added                        
                        Class<?> valueType = toSet.getClass();
                        if (!fieldType.isAssignableFrom(valueType)) {
                            throw new ClassCastException(Msg.getString(
                                    "K00d4", new String[] { //$NON-NLS-1$
                                    fieldType.toString(), valueType.toString(),
                                            classDesc.getName() + "." //$NON-NLS-1$
                                                    + fieldName }));
                        }
                        try {
                            objSetField(obj, declaringClass, fieldName, field
                                    .getTypeString(), toSet);
                        } catch (NoSuchFieldError e) {
                            // Ignored
                        }
                    }
                }
            }
        }
    }

    /**
     * Reads and returns primitive data of type float read from the receiver
     * 
     * @return A float saved as primitive data using
     *         <code>ObjectOutputStream.writeFloat()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public float readFloat() throws IOException {
        return primitiveTypes.readFloat();
    }

    /**
     * Reads bytes from the receiver into the byte array <code>buffer</code>.
     * This method will block until <code>buffer.length</code> number of bytes
     * have been read.
     * 
     * @param buffer
     *            the buffer to read bytes into
     * 
     * @throws IOException
     *             if a problem occurs reading from this stream.
     */
    public void readFully(byte[] buffer) throws IOException {
        primitiveTypes.readFully(buffer);
    }

    /**
     * Reads bytes from the receiver into the byte array <code>buffer</code>.
     * This method will block until <code>length</code> number of bytes have
     * been read.
     * 
     * @param buffer
     *            the byte array in which to store the read bytes.
     * @param offset
     *            the offset in <code>buffer</code> to store the read bytes.
     * @param length
     *            the maximum number of bytes to store in <code>buffer</code>.
     * 
     * @throws IOException
     *             if a problem occurs reading from this stream.
     */
    public void readFully(byte[] buffer, int offset, int length)
            throws IOException {
        primitiveTypes.readFully(buffer, offset, length);
    }

    /**
     * Walks the hierarchy of classes described by class descriptor
     * <code>classDesc</code> and reads the field values corresponding to
     * fields declared by the corresponding class descriptor. The instance to
     * store field values into is <code>object</code>. If the class
     * (corresponding to class descriptor <code>classDesc</code>) defines
     * private instance method <code>readObject</code> it will be used to load
     * field values.
     * 
     * @param object
     *            Instance into which stored field values loaded.
     * @param classDesc
     *            A class descriptor (an <code>ObjectStreamClass</code>)
     *            defining which fields should be loaded.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the field values in
     *             the hierarchy.
     * @throws ClassNotFoundException
     *             If a class for one of the field types could not be found
     * @throws NotActiveException
     *             If <code>defaultReadObject</code> is called from the wrong
     *             context.
     * 
     * @see #defaultReadObject
     * @see #readObject()
     */
    private void readHierarchy(Object object, ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException, NotActiveException {
        // We can't be called from just anywhere. There are rules.
        if (object == null && mustResolve) {
            throw new NotActiveException();
        }

        ArrayList<ObjectStreamClass> streamClassList = new ArrayList<ObjectStreamClass>(
                32);
        ObjectStreamClass nextStreamClass = classDesc;
        while (nextStreamClass != null) {
            streamClassList.add(0, nextStreamClass);
            nextStreamClass = nextStreamClass.getSuperclass();
        }
        if (object == null) {
            Iterator<ObjectStreamClass> streamIt = streamClassList.iterator();
            while (streamIt.hasNext()) {
                ObjectStreamClass streamClass = streamIt.next();
                readObjectForClass(null, streamClass);
            }
        } else {
            ArrayList<Class<?>> classList = new ArrayList<Class<?>>(32);
            Class<?> nextClass = object.getClass();
            while (nextClass != null) {
                Class<?> testClass = nextClass.getSuperclass();
                if (testClass != null) {
                    classList.add(0, nextClass);
                }
                nextClass = testClass;
            }
            int lastIndex = 0;
            for (int i = 0; i < classList.size(); i++) {
                Class<?> superclass = classList.get(i);
                int index = findStreamSuperclass(superclass, streamClassList,
                        lastIndex);
                if (index == -1) {
                    readObjectNoData(object, superclass);
                } else {
                    for (int j = lastIndex; j <= index; j++) {
                        readObjectForClass(object, streamClassList.get(j));
                    }
                    lastIndex = index + 1;
                }
            }
        }
    }

    private int findStreamSuperclass(Class<?> cl,
            ArrayList<ObjectStreamClass> classList, int lastIndex) {
        ObjectStreamClass objCl;
        String forName;

        for (int i = lastIndex; i < classList.size(); i++) {
            objCl = classList.get(i);
            forName = objCl.forClass().getName();

            if (objCl.getName().equals(forName)) {
                if (cl.getName().equals(objCl.getName())) {
                    return i;
                }
            } else {
                // there was a class replacement
                if (cl.getName().equals(forName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void readObjectNoData(Object object, Class<?> cl)
            throws ObjectStreamException {
        if (!ObjectStreamClass.isSerializable(cl)) {
            return;
        }

        final Method readMethod = ObjectStreamClass
                .getPrivateReadObjectNoDataMethod(cl);
        if (readMethod != null) {
            AccessController.doPrivileged(new PriviAction<Object>(readMethod));
            try {
                readMethod.invoke(object, new Object[0]);
            } catch (InvocationTargetException e) {
                Throwable ex = e.getTargetException();
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                } else if (ex instanceof Error) {
                    throw (Error) ex;
                }
                throw (ObjectStreamException) ex;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.toString());
            }
        }
    }

    private void readObjectForClass(Object object, ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException, NotActiveException {
        // Have to do this before calling defaultReadObject or anything that
        // calls defaultReadObject
        currentObject = object;
        currentClass = classDesc;

        boolean hadWriteMethod = (classDesc.getFlags() & SC_WRITE_METHOD) > 0;
        Class<?> targetClass = classDesc.forClass();
        final Method readMethod;
        if (targetClass == null || !mustResolve) {
            readMethod = null;
        } else {
            readMethod = ObjectStreamClass
                    .getPrivateReadObjectMethod(targetClass);
        }
        try {
            if (readMethod != null) {
                // We have to be able to fetch its value, even if it is private
                AccessController.doPrivileged(new PriviAction<Object>(
                        readMethod));
                try {
                    readMethod.invoke(object, new Object[] { this });
                } catch (InvocationTargetException e) {
                    Throwable ex = e.getTargetException();
                    if (ex instanceof ClassNotFoundException) {
                        throw (ClassNotFoundException) ex;
                    } else if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    } else if (ex instanceof Error) {
                        throw (Error) ex;
                    }
                    throw (IOException) ex;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.toString());
                }
            } else {
                defaultReadObject();
            }
            if (hadWriteMethod) {
                discardData();
            }
        } finally {
            // Cleanup, needs to run always so that we can later detect invalid
            // calls to defaultReadObject
            currentObject = null; // We did not set this, so we do not need to
            // clean it
            currentClass = null;
        }
    }

    /**
     * Reads and returns primitive data of type int read from the receiver
     * 
     * @return an int saved as primitive data using
     *         <code>ObjectOutputStream.writeInt()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public int readInt() throws IOException {
        return primitiveTypes.readInt();
    }

    /**
     * Reads and returns the next line (primitive data of type String) read from
     * the receiver
     * 
     * @return a String saved as primitive data using
     *         <code>ObjectOutputStream.writeLine()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     * 
     * @deprecated Use {@link BufferedReader}
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public String readLine() throws IOException {
        return primitiveTypes.readLine();
    }

    /**
     * Reads and returns primitive data of type long read from the receiver
     * 
     * @return a long saved as primitive data using
     *         <code>ObjectOutputStream.writeLong()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public long readLong() throws IOException {
        return primitiveTypes.readLong();
    }

    /**
     * Read a new array from the receiver. It is assumed the array has not been
     * read yet (not a cyclic reference). Return the array read.
     * 
     * @param unshared
     *            read the object unshared
     * @return the array read
     * 
     * @throws IOException
     *             If an IO exception happened when reading the array.
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     * @throws OptionalDataException
     *             If optional data could not be found when reading the array.
     */
    private Object readNewArray(boolean unshared) throws OptionalDataException,
            ClassNotFoundException, IOException {
        ObjectStreamClass classDesc = readClassDesc();

        if (classDesc == null) {
            throw new InvalidClassException(Msg.getString("K00d1")); //$NON-NLS-1$
        }

        Integer newHandle = Integer.valueOf(nextHandle());

        // Array size
        int size = input.readInt();
        Class<?> arrayClass = classDesc.forClass();
        Class<?> componentType = arrayClass.getComponentType();
        Object result = Array.newInstance(componentType, size);

        registerObjectRead(result, newHandle, unshared);

        // Now we have code duplication just because Java is typed. We have to
        // read N elements and assign to array positions, but we must typecast
        // the array first, and also call different methods depending on the
        // elements.
        if (componentType.isPrimitive()) {
            if (componentType == Integer.TYPE) {
                int[] intArray = (int[]) result;
                for (int i = 0; i < size; i++) {
                    intArray[i] = input.readInt();
                }
            } else if (componentType == Byte.TYPE) {
                byte[] byteArray = (byte[]) result;
                input.readFully(byteArray, 0, size);
            } else if (componentType == Character.TYPE) {
                char[] charArray = (char[]) result;
                for (int i = 0; i < size; i++) {
                    charArray[i] = input.readChar();
                }
            } else if (componentType == Short.TYPE) {
                short[] shortArray = (short[]) result;
                for (int i = 0; i < size; i++) {
                    shortArray[i] = input.readShort();
                }
            } else if (componentType == Boolean.TYPE) {
                boolean[] booleanArray = (boolean[]) result;
                for (int i = 0; i < size; i++) {
                    booleanArray[i] = input.readBoolean();
                }
            } else if (componentType == Long.TYPE) {
                long[] longArray = (long[]) result;
                for (int i = 0; i < size; i++) {
                    longArray[i] = input.readLong();
                }
            } else if (componentType == Float.TYPE) {
                float[] floatArray = (float[]) result;
                for (int i = 0; i < size; i++) {
                    floatArray[i] = input.readFloat();
                }
            } else if (componentType == Double.TYPE) {
                double[] doubleArray = (double[]) result;
                for (int i = 0; i < size; i++) {
                    doubleArray[i] = input.readDouble();
                }
            } else {
                throw new ClassNotFoundException(Msg.getString(
                        "K00d7", classDesc.getName())); //$NON-NLS-1$
            }
        } else {
            // Array of Objects
            Object[] objectArray = (Object[]) result;
            for (int i = 0; i < size; i++) {
                objectArray[i] = readObject();
            }
        }
        if (enableResolve) {
            result = resolveObject(result);
            registerObjectRead(result, newHandle, false);
        }
        return result;
    }

    /**
     * Reads a new class from the receiver. It is assumed the class has not been
     * read yet (not a cyclic reference). Return the class read.
     * 
     * @param unshared
     *            read the object unshared
     * @return The <code>java.lang.Class</code> read from the stream.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the class.
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     */
    private Class<?> readNewClass(boolean unshared)
            throws ClassNotFoundException, IOException {
        ObjectStreamClass classDesc = readClassDesc();

        if (classDesc != null) {
            Integer newHandle = Integer.valueOf(nextHandle());
            Class<?> localClass = classDesc.forClass();
            if (localClass != null) {
                registerObjectRead(localClass, newHandle, unshared);
            }
            return localClass;
        }
        throw new InvalidClassException(Msg.getString("K00d1")); //$NON-NLS-1$
    }

    /*
     * read class type for Enum, note there's difference between enum and normal
     * classes
     */
    private ObjectStreamClass readEnumDesc() throws IOException,
            ClassNotFoundException {
        byte tc = nextTC();
        switch (tc) {
            case TC_CLASSDESC:
                return readEnumDescInternal();
            case TC_REFERENCE:
                return (ObjectStreamClass) readCyclicReference();
            case TC_NULL:
                return null;
            default:
                throw new StreamCorruptedException(Msg.getString(
                        "K00d2", Integer.toHexString(tc & 0xff))); //$NON-NLS-1$
        }
    }

    private ObjectStreamClass readEnumDescInternal() throws IOException,
            ClassNotFoundException {
        ObjectStreamClass classDesc;
        primitiveData = input;
        Integer oldHandle = descriptorHandle;
        descriptorHandle = Integer.valueOf(nextHandle());
        classDesc = readClassDescriptor();
        if (descriptorHandle != null) {
            registerObjectRead(classDesc, descriptorHandle, false);
        }
        descriptorHandle = oldHandle;
        primitiveData = emptyStream;
        classDesc.setClass(resolveClass(classDesc));
        // Consume unread class annotation data and TC_ENDBLOCKDATA
        discardData();
        ObjectStreamClass superClass = readClassDesc();
        checkedSetSuperClassDesc(classDesc, superClass);
        // Check SUIDs, note all SUID for Enum is 0L
        if (0L != classDesc.getSerialVersionUID()
                || 0L != superClass.getSerialVersionUID()) {
            throw new InvalidClassException(superClass.getName(), Msg
                    .getString("K00da", superClass, //$NON-NLS-1$
                            superClass));
        }
        byte tc = nextTC();
        // discard TC_ENDBLOCKDATA after classDesc if any
        if (tc == TC_ENDBLOCKDATA) {
            // read next parent class. For enum, it may be null
            superClass.setSuperclass(readClassDesc());
        } else {
            // not TC_ENDBLOCKDATA, push back for next read
            pushbackTC();
        }
        return classDesc;
    }

    @SuppressWarnings("unchecked")// For the Enum.valueOf call
    private Object readEnum(boolean unshared) throws OptionalDataException,
            ClassNotFoundException, IOException {
        // read classdesc for Enum first
        ObjectStreamClass classDesc = readEnumDesc();
        Integer newHandle = Integer.valueOf(nextHandle());
        // read name after class desc
        String name;
        byte tc = nextTC();
        switch (tc) {
            case TC_REFERENCE:
                if (unshared) {
                    readNewHandle();
                    throw new InvalidObjectException(Msg.getString("KA002")); //$NON-NLS-1$
                }
                name = (String) readCyclicReference();
                break;
            case TC_STRING:
                name = (String) readNewString(unshared);
                break;
            default:
                throw new StreamCorruptedException(Msg.getString("K00d2"));//$NON-NLS-1$
        }

        Enum<?> result = Enum.valueOf((Class) classDesc.forClass(), name);
        registerObjectRead(result, newHandle, unshared);

        return result;
    }

    /**
     * Reads a new class descriptor from the receiver. It is assumed the class
     * descriptor has not been read yet (not a cyclic reference). Return the
     * class descriptor read.
     * 
     * @param unshared
     *            read the object unshared
     * @return The <code>ObjectStreamClass</code> read from the stream.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     */
    private ObjectStreamClass readNewClassDesc(boolean unshared)
            throws ClassNotFoundException, IOException {
        // So read...() methods can be used by
        // subclasses during readClassDescriptor()
        primitiveData = input;
        Integer oldHandle = descriptorHandle;
        descriptorHandle = Integer.valueOf(nextHandle());
        ObjectStreamClass newClassDesc = readClassDescriptor();
        if (descriptorHandle != null) {
            registerObjectRead(newClassDesc, descriptorHandle, unshared);
        }
        descriptorHandle = oldHandle;
        primitiveData = emptyStream;

        // We need to map classDesc to class.
        try {
            newClassDesc.setClass(resolveClass(newClassDesc));
            // Check SUIDs
            verifySUID(newClassDesc);
            // Check base name of the class
            verifyBaseName(newClassDesc);
        } catch (ClassNotFoundException e) {
            if (mustResolve) {
                throw e;
                // Just continue, the class may not be required
            }
        }

        // Resolve the field signatures using the class loader of the
        // resolved class
        ObjectStreamField[] fields = newClassDesc.getLoadFields();
        fields = (null == fields ? new ObjectStreamField[] {} : fields);
        ClassLoader loader = newClassDesc.forClass() == null ? callerClassLoader
                : newClassDesc.forClass().getClassLoader();
        for (ObjectStreamField element : fields) {
            element.resolve(loader);
        }

        // Consume unread class annotation data and TC_ENDBLOCKDATA
        discardData();
        checkedSetSuperClassDesc(newClassDesc, readClassDesc());
        return newClassDesc;
    }

    /**
     * Reads a new proxy class descriptor from the receiver. It is assumed the
     * proxy class descriptor has not been read yet (not a cyclic reference).
     * Return the proxy class descriptor read.
     * 
     * @return The <code>Class</code> read from the stream.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     */
    private Class<?> readNewProxyClassDesc() throws ClassNotFoundException,
            IOException {
        int count = input.readInt();
        String[] interfaceNames = new String[count];
        for (int i = 0; i < count; i++) {
            interfaceNames[i] = input.readUTF();
        }
        Class<?> proxy = resolveProxyClass(interfaceNames);
        // Consume unread class annotation data and TC_ENDBLOCKDATA
        discardData();
        return proxy;
    }

    /**
     * Reads a new class descriptor from the receiver. Return the class
     * descriptor read.
     * 
     * @return The <code>ObjectStreamClass</code> read from the stream.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     */
    protected ObjectStreamClass readClassDescriptor() throws IOException,
            ClassNotFoundException {

        ObjectStreamClass newClassDesc = new ObjectStreamClass();
        String name = input.readUTF();
        if ("".equals(name)) {
            throw new IOException("The stream is corrupted.");
        }
        newClassDesc.setName(name);
        newClassDesc.setSerialVersionUID(input.readLong());
        newClassDesc.setFlags(input.readByte());

        // We must register the class descriptor before reading field
        // descriptors.
        // if called outside of readObject, the descriptorHandle might be null
        descriptorHandle = (null == descriptorHandle ? Integer
                .valueOf(nextHandle()) : descriptorHandle);
        registerObjectRead(newClassDesc, descriptorHandle, false);
        descriptorHandle = null;

        readFieldDescriptors(newClassDesc);
        return newClassDesc;
    }

    /**
     * Retrieves the proxy class corresponding to the interface names.
     * 
     * @param interfaceNames
     *            The interfaces used to create the proxy class
     * @return A proxy class
     * 
     * @throws IOException
     *             If any IO problem occurred when trying to load the class.
     * @throws ClassNotFoundException
     *             If the proxy class cannot be created
     */
    protected Class<?> resolveProxyClass(String[] interfaceNames)
            throws IOException, ClassNotFoundException {
        // BEGIN android-removed
        // ClassLoader loader = VM.getNonBootstrapClassLoader();
        // END android-removed
        // BEGIN android-added
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        // END android-added
        Class<?>[] interfaces = new Class<?>[interfaceNames.length];
        for (int i = 0; i < interfaceNames.length; i++) {
            interfaces[i] = Class.forName(interfaceNames[i], false, loader);
        }
        try {
            return Proxy.getProxyClass(loader, interfaces);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(e.toString(), e);
        }
    }

    /**
     * Write a new handle describing a cyclic reference from the stream.
     * 
     * @return the handle read
     * 
     * @throws IOException
     *             If an IO exception happened when reading the handle
     */
    private Integer readNewHandle() throws IOException {
        return Integer.valueOf(input.readInt());
    }

    /**
     * Read a new object from the stream. It is assumed the object has not been
     * loaded yet (not a cyclic reference). Return the object read.
     * 
     * If the object implements <code>Externalizable</code> its
     * <code>readExternal</code> is called. Otherwise, all fields described by
     * the class hierarchy are loaded. Each class can define how its declared
     * instance fields are loaded by defining a private method
     * <code>readObject</code>
     * 
     * @param unshared
     *            read the object unshared
     * @return the object read
     * 
     * @throws IOException
     *             If an IO exception happened when reading the object.
     * @throws OptionalDataException
     *             If optional data could not be found when reading the object
     *             graph
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     */
    private Object readNewObject(boolean unshared)
            throws OptionalDataException, ClassNotFoundException, IOException {
        ObjectStreamClass classDesc = readClassDesc();

        if (classDesc == null) {
            throw new InvalidClassException(Msg.getString("K00d1")); //$NON-NLS-1$
        }

        Integer newHandle = Integer.valueOf(nextHandle());

        // Note that these values come from the Stream, and in fact it could be
        // that the classes have been changed so that the info below now
        // conflicts with the newer class
        boolean wasExternalizable = (classDesc.getFlags() & SC_EXTERNALIZABLE) > 0;
        boolean wasSerializable = (classDesc.getFlags() & SC_SERIALIZABLE) > 0;

        // Maybe we should cache the values above in classDesc ? It may be the
        // case that when reading classDesc we may need to read more stuff
        // depending on the values above
        Class<?> objectClass = classDesc.forClass();

        Object result, registeredResult = null;
        if (objectClass != null) {
            // The class of the instance may not be the same as the class of the
            // constructor to run
            // This is the constructor to run if Externalizable
            Class<?> constructorClass = objectClass;

            // WARNING - What if the object is serializable and externalizable ?
            // Is that possible ?
            if (wasSerializable) {
                // Now we must run the constructor of the class just above the
                // one that implements Serializable so that slots that were not
                // dumped can be initialized properly
                while (constructorClass != null
                        && ObjectStreamClass.isSerializable(constructorClass)) {
                    constructorClass = constructorClass.getSuperclass();
                }
            }

            // Fetch the empty constructor, or null if none.
            Constructor<?> constructor = null;
            if (constructorClass != null) {
                try {
                    constructor = constructorClass
                            .getDeclaredConstructor(ObjectStreamClass.EMPTY_CONSTRUCTOR_PARAM_TYPES);
                } catch (NoSuchMethodException nsmEx) {
                    // Ignored
                }
            }

            // Has to have an empty constructor
            if (constructor == null) {
                throw new InvalidClassException(constructorClass.getName(), Msg
                        .getString("K00dc")); //$NON-NLS-1$
            }

            int constructorModifiers = constructor.getModifiers();

            // Now we must check if the empty constructor is visible to the
            // instantiation class
            if (Modifier.isPrivate(constructorModifiers)
                    || (wasExternalizable && !Modifier
                            .isPublic(constructorModifiers))) {
                throw new InvalidClassException(constructorClass.getName(), Msg
                        .getString("K00dc")); //$NON-NLS-1$
            }

            // We know we are testing from a subclass, so the only other case
            // where the visibility is not allowed is when the constructor has
            // default visibility and the instantiation class is in a different
            // package than the constructor class
            if (!Modifier.isPublic(constructorModifiers)
                    && !Modifier.isProtected(constructorModifiers)) {
                // Not public, not private and not protected...means default
                // visibility. Check if same package
                if (!inSamePackage(constructorClass, objectClass)) {
                    throw new InvalidClassException(constructorClass.getName(),
                            Msg.getString("K00dc")); //$NON-NLS-1$
                }
            }

            // Now we know which class to instantiate and which constructor to
            // run. We are allowed to run the constructor.
            result = newInstance(objectClass, constructorClass);
            registerObjectRead(result, newHandle, unshared);

            registeredResult = result;
        } else {
            result = null;
        }

        try {
            // This is how we know what to do in defaultReadObject. And it is
            // also used by defaultReadObject to check if it was called from an
            // invalid place. It also allows readExternal to call
            // defaultReadObject and have it work.
            currentObject = result;
            currentClass = classDesc;

            // If Externalizable, just let the object read itself
            if (wasExternalizable) {
                boolean blockData = (classDesc.getFlags() & SC_BLOCK_DATA) > 0;
                if (!blockData) {
                    primitiveData = input;
                }
                if (mustResolve) {
                    Externalizable extern = (Externalizable) result;
                    extern.readExternal(this);
                }
                if (blockData) {
                    // Similar to readHierarchy. Anything not read by
                    // readExternal has to be consumed here
                    discardData();
                } else {
                    primitiveData = emptyStream;
                }
            } else {
                // If we got here, it is Serializable but not Externalizable.
                // Walk the hierarchy reading each class' slots
                readHierarchy(result, classDesc);
            }
        } finally {
            // Cleanup, needs to run always so that we can later detect invalid
            // calls to defaultReadObject
            currentObject = null;
            currentClass = null;
        }

        if (objectClass != null) {
            Object readResolveMethod = readResolveCache.get(objectClass);
            if (readResolveMethod != this) {
                if (readResolveMethod == null) {
                    final Method readResolve = ObjectStreamClass
                            .methodReadResolve(objectClass);
                    if (readResolve == null) {
                        readResolveCache.put(objectClass, this);
                        readResolveMethod = null;
                    } else {
                        // Has replacement method
                        AccessController.doPrivileged(new PriviAction<Object>(
                                readResolve));
                        readResolveCache.put(objectClass, readResolve);
                        readResolveMethod = readResolve;
                    }
                }
                if (readResolveMethod != null) {
                    try {
                        result = ((Method) readResolveMethod).invoke(result,
                                (Object[]) null);
                    } catch (IllegalAccessException iae) {
                    } catch (InvocationTargetException ite) {
                        Throwable target = ite.getTargetException();
                        if (target instanceof ObjectStreamException) {
                            throw (ObjectStreamException) target;
                        } else if (target instanceof Error) {
                            throw (Error) target;
                        } else {
                            throw (RuntimeException) target;
                        }
                    }
                }
            }
        }
        // We get here either if class-based replacement was not needed or if it
        // was needed but produced the same object or if it could not be
        // computed.

        // The object to return is the one we instantiated or a replacement for
        // it
        if (result != null && enableResolve) {
            result = resolveObject(result);
        }
        if (registeredResult != result) {
            registerObjectRead(result, newHandle, unshared);
        }
        return result;
    }

    /**
     * Read a new String in UTF format from the receiver. Return the string
     * read.
     * 
     * @param unshared
     *            read the object unshared
     * @return the string just read.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the String.
     */
    private Object readNewString(boolean unshared) throws IOException {
        Object result = input.readUTF();
        if (enableResolve) {
            result = resolveObject(result);
        }
        int newHandle = nextHandle();
        registerObjectRead(result, Integer.valueOf(newHandle), unshared);

        return result;
    }

    /**
     * Read a new String in UTF format from the receiver. Return the string
     * read.
     * 
     * @param unshared
     *            read the object unshared
     * @return the string just read.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the String.
     */
    private Object readNewLongString(boolean unshared) throws IOException {
        long length = input.readLong();
        Object result = input.decodeUTF((int) length);
        if (enableResolve) {
            result = resolveObject(result);
        }
        int newHandle = nextHandle();
        registerObjectRead(result, Integer.valueOf(newHandle), unshared);

        return result;
    }

    /**
     * Read the next object from the receiver's underlying stream.
     * 
     * @return the new object read.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the object
     * @throws ClassNotFoundException
     *             If the class of one of the objects in the object graph could
     *             not be found
     * @throws OptionalDataException
     *             If primitive data types were found instead of an object.
     * 
     * @see ObjectOutputStream#writeObject(Object)
     */
    public final Object readObject() throws OptionalDataException,
            ClassNotFoundException, IOException {
        return readObject(false);
    }

    /**
     * Read the next unshared object from the receiver's underlying stream.
     * 
     * @return the new object read.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the object
     * @throws ClassNotFoundException
     *             If the class of one of the objects in the object graph could
     *             not be found
     * 
     * @see ObjectOutputStream#writeUnshared
     */
    public Object readUnshared() throws IOException, ClassNotFoundException {
        return readObject(true);
    }

    private Object readObject(boolean unshared) throws OptionalDataException,
            ClassNotFoundException, IOException {
        boolean restoreInput = (primitiveData == input);
        if (restoreInput) {
            primitiveData = emptyStream;
        }

        // This is the spec'ed behavior in JDK 1.2. Very bizarre way to allow
        // behavior overriding.
        if (subclassOverridingImplementation && !unshared) {
            return readObjectOverride();
        }

        // If we still had primitive types to read, should we discard them
        // (reset the primitiveTypes stream) or leave as is, so that attempts to
        // read primitive types won't read 'past data' ???
        Object result;
        try {
            // We need this so we can tell when we are returning to the
            // original/outside caller
            if (++nestedLevels == 1) {
                // Remember the caller's class loader
                // BEGIN android-changed
                callerClassLoader = getClosestUserClassLoader();
                // END android-changed
            }

            result = readNonPrimitiveContent(unshared);
            if (restoreInput) {
                primitiveData = input;
            }
        } finally {
            // We need this so we can tell when we are returning to the
            // original/outside caller
            if (--nestedLevels == 0) {
                // We are going to return to the original caller, perform
                // cleanups.
                // No more need to remember the caller's class loader
                callerClassLoader = null;
            }
        }

        // Done reading this object. Is it time to return to the original
        // caller? If so we need to perform validations first.
        if (nestedLevels == 0 && validations != null) {
            // We are going to return to the original caller. If validation is
            // enabled we need to run them now and then cleanup the validation
            // collection
            try {
                for (InputValidationDesc element : validations) {
                    element.validator.validateObject();
                }
            } finally {
                // Validations have to be renewed, since they are only called
                // from readObject
                validations = null;
            }
        }
        return result;
    }

    // BEGIN android-added
    private static final ClassLoader bootstrapLoader
            = Object.class.getClassLoader();
    private static final ClassLoader systemLoader
            = ClassLoader.getSystemClassLoader();

    /**
     * Searches up the call stack to find the closest user-defined class loader.
     *
     * @return a user-defined class loader or null if one isn't found
     */
    private static ClassLoader getClosestUserClassLoader() {
        Class<?>[] stackClasses = VMStack.getClasses(-1, false);
        for (Class<?> stackClass : stackClasses) {
            ClassLoader loader = stackClass.getClassLoader();
            if (loader != null && loader != bootstrapLoader
                    && loader != systemLoader) {
                return loader;
            }
        }
        return null;
    }
    // END android-added

    /**
     * Method to be overriden by subclasses to read the next object from the
     * receiver's underlying stream.
     * 
     * @return the new object read.
     * 
     * @throws IOException
     *             If an IO exception happened when reading the object
     * @throws ClassNotFoundException
     *             If the class of one of the objects in the object graph could
     *             not be found
     * @throws OptionalDataException
     *             If primitive data types were found instead of an object.
     * 
     * @see ObjectOutputStream#writeObjectOverride
     */
    protected Object readObjectOverride() throws OptionalDataException,
            ClassNotFoundException, IOException {
        if (input == null) {
            return null;
        }
        // Subclasses must override.
        throw new IOException();
    }

    /**
     * Reads and returns primitive data of type short from the receiver
     * 
     * @return a short saved as primitive data using
     *         <code>ObjectOutputStream.writeShort()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public short readShort() throws IOException {
        return primitiveTypes.readShort();
    }

    /**
     * Reads and validates the ObjectInputStream header from the receiver
     * 
     * @throws IOException
     *             If an IO exception happened when reading the stream header.
     * @throws StreamCorruptedException
     *             If the underlying stream does not contain serialized objects
     *             that can be read.
     */
    protected void readStreamHeader() throws IOException,
            StreamCorruptedException {
        if (input.readShort() == STREAM_MAGIC
                && input.readShort() == STREAM_VERSION) {
            return;
        }
        throw new StreamCorruptedException();
    }

    /**
     * Reads and returns primitive data of type byte (unsigned) from the
     * receiver
     * 
     * @return a byte saved as primitive data using
     *         <code>ObjectOutputStream.writeUnsignedByte()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public int readUnsignedByte() throws IOException {
        return primitiveTypes.readUnsignedByte();
    }

    /**
     * Reads and returns primitive data of type short (unsigned) from the
     * receiver
     * 
     * @return a short saved as primitive data using
     *         <code>ObjectOutputStream.writeUnsignedShort()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public int readUnsignedShort() throws IOException {
        return primitiveTypes.readUnsignedShort();
    }

    /**
     * Reads and returns primitive data of type String read in UTF format from
     * the receiver
     * 
     * @return a String saved as primitive data using
     *         <code>ObjectOutputStream.writeUTF()</code>
     * 
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    public String readUTF() throws IOException {
        return primitiveTypes.readUTF();
    }

    /**
     * Return the object previously read tagged with handle <code>handle</code>.
     * 
     * @param handle
     *            The handle that this object was assigned when it was read.
     * @return the object previously read.
     * 
     * @throws InvalidObjectException
     *             If there is no previously read object with this handle
     */
    private Object registeredObjectRead(Integer handle)
            throws InvalidObjectException {
        Object res = objectsRead.get(handle);

        if (res == UNSHARED_OBJ) {
            throw new InvalidObjectException(Msg.getString("KA010")); //$NON-NLS-1$
        }

        return res;
    }

    /**
     * Assume object <code>obj</code> has been read, and assign a handle to
     * it, <code>handle</code>.
     * 
     * @param obj
     *            Non-null object being loaded.
     * @param handle
     *            An Integer, the handle to this object
     * @param unshared
     *            Boolean, indicates that caller is reading in unshared mode
     * 
     * @see #nextHandle
     */
    private void registerObjectRead(Object obj, Integer handle, boolean unshared) {
        objectsRead.put(handle, unshared ? UNSHARED_OBJ : obj);
    }

    /**
     * Register object validator <code>object</code> to be executed to perform
     * validation of objects loaded from the receiver. Validations will be run
     * in order of decreasing priority, defined by <code>priority</code>.
     * 
     * @param object
     *            An ObjectInputValidation to validate objects loaded.
     * @param priority
     *            validator priority
     * 
     * @throws NotActiveException
     *             If this method is not called from <code>readObject()</code>
     * @throws InvalidObjectException
     *             If <code>object</code> is null.
     */
    public synchronized void registerValidation(ObjectInputValidation object,
            int priority) throws NotActiveException, InvalidObjectException {
        // Validation can only be registered when inside readObject calls
        Object instanceBeingRead = this.currentObject;

        // We can't be called from just anywhere. There are rules.
        if (instanceBeingRead == null) {
            throw new NotActiveException();
        }
        if (object == null) {
            throw new InvalidObjectException(Msg.getString("K00d9")); //$NON-NLS-1$
        }
        // From now on it is just insertion in a SortedCollection. Since
        // the Java class libraries don't provide that, we have to
        // implement it from scratch here.
        InputValidationDesc desc = new InputValidationDesc();
        desc.validator = object;
        desc.priority = priority;
        // No need for this, validateObject does not take a parameter
        // desc.toValidate = instanceBeingRead;
        if (validations == null) {
            validations = new InputValidationDesc[1];
            validations[0] = desc;
        } else {
            int i = 0;
            for (; i < validations.length; i++) {
                InputValidationDesc validation = validations[i];
                // Sorted, higher priority first.
                if (priority >= validation.priority) {
                    break; // Found the index where to insert
                }
            }
            InputValidationDesc[] oldValidations = validations;
            int currentSize = oldValidations.length;
            validations = new InputValidationDesc[currentSize + 1];
            System.arraycopy(oldValidations, 0, validations, 0, i);
            System.arraycopy(oldValidations, i, validations, i + 1, currentSize
                    - i);
            validations[i] = desc;
        }
    }

    /**
     * Reset the collection of objects already loaded by the receiver.
     */
    private void resetSeenObjects() {
        objectsRead = new Hashtable<Integer, Object>();
        currentHandle = baseWireHandle;
        primitiveData = emptyStream;
    }

    /**
     * Reset the receiver. The collection of objects already read by the
     * receiver is reset, and internal structures are also reset so that the
     * receiver knows it is in a fresh clean state.
     */
    private void resetState() {
        resetSeenObjects();
        hasPushbackTC = false;
        pushbackTC = 0;
        // nestedLevels = 0;
    }

    /**
     * Loads the Java class corresponding to the class descriptor
     * <code>osClass</code>(ObjectStreamClass) just read from the receiver.
     * 
     * @param osClass
     *            An ObjectStreamClass read from the receiver.
     * @return a Class corresponding to the descriptor loaded.
     * 
     * @throws IOException
     *             If any IO problem occurred when trying to load the class.
     * @throws ClassNotFoundException
     *             If the corresponding class cannot be found.
     */
    protected Class<?> resolveClass(ObjectStreamClass osClass)
            throws IOException, ClassNotFoundException {
        String className = osClass.getName();
        // if it is primitive class, for example, long.class
        Class<?> cls = PRIMITIVE_CLASSES.get(className);
        if (null == cls) {
            // not primitive class
            // Use the first non-null ClassLoader on the stack. If null, use the
            // system class loader
            return Class.forName(className, true, callerClassLoader);
        }
        return cls;
    }

    /**
     * If <code>enableResolveObject()</code> was activated, computes the
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
     * @see #enableResolveObject
     * @see ObjectOutputStream#enableReplaceObject
     * @see ObjectOutputStream#replaceObject
     */
    protected Object resolveObject(Object object) throws IOException {
        // By default no object replacement. Subclasses can override
        return object;
    }

    /**
     * Set a given declared field named <code>fieldName</code> of
     * <code>instance</code> to the new <code>byte</code> value
     * <code>value</code>.
     * 
     * This method could be implemented non-natively on top of java.lang.reflect
     * implementations that support the <code>setAccessible</code> API, at the
     * expense of extra object creation (java.lang.reflect.Field). Otherwise
     * Serialization could not set private fields, except by the use of a native
     * method like this one.
     * 
     * @param instance
     *            Object whose field to set
     * @param declaringClass
     *            <code>instance</code>'s declaring class
     * @param fieldName
     *            Name of the field to set
     * @param value
     *            New value for the field
     * 
     * @throws NoSuchFieldError
     *             If the field does not exist.
     */
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, byte value)
            throws NoSuchFieldError;

    /**
     * Set a given declared field named <code>fieldName</code> of
     * <code>instance</code> to the new <code>char</code> value
     * <code>value</code>.
     * 
     * This method could be implemented non-natively on top of java.lang.reflect
     * implementations that support the <code>setAccessible</code> API, at the
     * expense of extra object creation (java.lang.reflect.Field). Otherwise
     * Serialization could not set private fields, except by the use of a native
     * method like this one.
     * 
     * @param instance
     *            Object whose field to set
     * @param declaringClass
     *            <code>instance</code>'s declaring class
     * @param fieldName
     *            Name of the field to set
     * @param value
     *            New value for the field
     * 
     * @throws NoSuchFieldError
     *             If the field does not exist.
     */
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, char value)
            throws NoSuchFieldError;

    /**
     * Set a given declared field named <code>fieldName</code> of
     * <code>instance</code> to the new <code>double</code> value
     * <code>value</code>.
     * 
     * This method could be implemented non-natively on top of java.lang.reflect
     * implementations that support the <code>setAccessible</code> API, at the
     * expense of extra object creation (java.lang.reflect.Field). Otherwise
     * Serialization could not set private fields, except by the use of a native
     * method like this one.
     * 
     * @param instance
     *            Object whose field to set
     * @param declaringClass
     *            <code>instance</code>'s declaring class
     * @param fieldName
     *            Name of the field to set
     * @param value
     *            New value for the field
     * 
     * @throws NoSuchFieldError
     *             If the field does not exist.
     */
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, double value)
            throws NoSuchFieldError;

    /**
     * Set a given declared field named <code>fieldName</code> of
     * <code>instance</code> to the new <code>float</code> value
     * <code>value</code>.
     * 
     * This method could be implemented non-natively on top of java.lang.reflect
     * implementations that support the <code>setAccessible</code> API, at the
     * expense of extra object creation (java.lang.reflect.Field). Otherwise
     * Serialization could not set private fields, except by the use of a native
     * method like this one.
     * 
     * @param instance
     *            Object whose field to set
     * @param declaringClass
     *            <code>instance</code>'s declaring class
     * @param fieldName
     *            Name of the field to set
     * @param value
     *            New value for the field
     * 
     * @throws NoSuchFieldError
     *             If the field does not exist.
     */
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, float value)
            throws NoSuchFieldError;

    /**
     * Set a given declared field named <code>fieldName</code> of
     * <code>instance</code> to the new <code>int</code> value
     * <code>value</code>.
     * 
     * This method could be implemented non-natively on top of java.lang.reflect
     * implementations that support the <code>setAccessible</code> API, at the
     * expense of extra object creation (java.lang.reflect.Field). Otherwise
     * Serialization could not set private fields, except by the use of a native
     * method like this one.
     * 
     * @param instance
     *            Object whose field to set
     * @param declaringClass
     *            <code>instance</code>'s declaring class
     * @param fieldName
     *            Name of the field to set
     * @param value
     *            New value for the field
     * 
     * @throws NoSuchFieldError
     *             If the field does not exist.
     */
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, int value)
            throws NoSuchFieldError;

    /**
     * Set a given declared field named <code>fieldName</code> of
     * <code>instance</code> to the new <code>long</code> value
     * <code>value</code>.
     * 
     * This method could be implemented non-natively on top of java.lang.reflect
     * implementations that support the <code>setAccessible</code> API, at the
     * expense of extra object creation (java.lang.reflect.Field). Otherwise
     * Serialization could not set private fields, except by the use of a native
     * method like this one.
     * 
     * @param instance
     *            Object whose field to set
     * @param declaringClass
     *            <code>instance</code>'s declaring class
     * @param fieldName
     *            Name of the field to set
     * @param value
     *            New value for the field
     * 
     * @throws NoSuchFieldError
     *             If the field does not exist.
     */
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, long value)
            throws NoSuchFieldError;

    /**
     * Set a given declared field named <code>fieldName</code> of
     * <code>instance</code> to the new value <code>value</code>.
     * 
     * This method could be implemented non-natively on top of java.lang.reflect
     * implementations that support the <code>setAccessible</code> API, at the
     * expense of extra object creation (java.lang.reflect.Field). Otherwise
     * Serialization could not set private fields, except by the use of a native
     * method like this one.
     * 
     * @param instance
     *            Object whose field to set
     * @param declaringClass
     *            Class which declares the field
     * @param fieldName
     *            Name of the field to set
     * @param fieldTypeName
     *            Name of the class defining the type of the field
     * @param value
     *            New value for the field
     * 
     * @throws NoSuchFieldError
     *             If the field does not exist.
     */
    private static native void objSetField(Object instance,
            Class<?> declaringClass, String fieldName, String fieldTypeName,
            Object value) throws NoSuchFieldError;

    /**
     * Set a given declared field named <code>fieldName</code> of
     * <code>instance</code> to the new <code>short</code> value
     * <code>value</code>.
     * 
     * This method could be implemented non-natively on top of java.lang.reflect
     * implementations that support the <code>setAccessible</code> API, at the
     * expense of extra object creation (java.lang.reflect.Field). Otherwise
     * Serialization could not set private fields, except by the use of a native
     * method like this one.
     * 
     * @param instance
     *            Object whose field to set
     * @param declaringClass
     *            <code>instance</code>'s declaring class
     * @param fieldName
     *            Name of the field to set
     * @param value
     *            New value for the field
     * 
     * @throws NoSuchFieldError
     *             If the field does not exist.
     */
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, short value)
            throws NoSuchFieldError;

    /**
     * Set a given declared field named <code>fieldName</code> of
     * <code>instance</code> to the new <code>boolean</code> value
     * <code>value</code>.
     * 
     * This method could be implemented non-natively on top of java.lang.reflect
     * implementations that support the <code>setAccessible</code> API, at the
     * expense of extra object creation (java.lang.reflect.Field). Otherwise
     * Serialization could not set private fields, except by the use of a native
     * method like this one.
     * 
     * @param instance
     *            Object whose field to set
     * @param declaringClass
     *            <code>instance</code>'s declaring class
     * @param fieldName
     *            Name of the field to set
     * @param value
     *            New value for the field
     * 
     * @throws NoSuchFieldError
     *             If the field does not exist.
     */
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, boolean value)
            throws NoSuchFieldError;

    /**
     * Skips <code>length</code> bytes of primitive data from the receiver. It
     * should not be used to skip bytes at any arbitrary position; just when
     * reading primitive data types (ints, chars, etc).
     * 
     * 
     * @param length
     *            How many bytes to skip
     * @return number of bytes skipped
     * 
     * @throws IOException
     *             If any IO problem occurred when trying to skip the bytes.
     */
    public int skipBytes(int length) throws IOException {
        // To be used with available. Ok to call if reading primitive buffer
        if (input == null) {
            throw new NullPointerException();
        }

        int offset = 0;
        while (offset < length) {
            checkReadPrimitiveTypes();
            long skipped = primitiveData.skip(length - offset);
            if (skipped == 0) {
                return offset;
            }
            offset += (int) skipped;
        }
        return length;
    }

    /**
     * Verify if the SUID for descriptor <code>loadedStreamClass</code>matches
     * the SUID of the corresponding loaded class.
     * 
     * @param loadedStreamClass
     *            An ObjectStreamClass that was loaded from the stream.
     * 
     * @throws InvalidClassException
     *             If the SUID of the stream class does not match the VM class
     */
    private void verifySUID(ObjectStreamClass loadedStreamClass)
            throws InvalidClassException {
        Class<?> localClass = loadedStreamClass.forClass();
        // Instances of java.lang.Class are always Serializable, even if their
        // instances aren't (e.g. java.lang.Object.class). We cannot call lookup
        // because it returns null if the parameter represents instances that
        // cannot be serialized, and that is not what we want. If we are loading
        // an instance of java.lang.Class, we better have the corresponding
        // ObjectStreamClass.
        ObjectStreamClass localStreamClass = ObjectStreamClass
                .lookupStreamClass(localClass);
        if (loadedStreamClass.getSerialVersionUID() != localStreamClass
                .getSerialVersionUID()) {
            throw new InvalidClassException(loadedStreamClass.getName(), Msg
                    .getString("K00da", loadedStreamClass, //$NON-NLS-1$
                            localStreamClass));
        }
    }

    /**
     * Verify if the base name for descriptor <code>loadedStreamClass</code>
     * matches the base name of the corresponding loaded class.
     * 
     * @param loadedStreamClass
     *            An ObjectStreamClass that was loaded from the stream.
     * 
     * @throws InvalidClassException
     *             If the base name of the stream class does not match the VM
     *             class
     */
    private void verifyBaseName(ObjectStreamClass loadedStreamClass)
            throws InvalidClassException {
        Class<?> localClass = loadedStreamClass.forClass();
        ObjectStreamClass localStreamClass = ObjectStreamClass
                .lookupStreamClass(localClass);
        String loadedClassBaseName = getBaseName(loadedStreamClass.getName());
        String localClassBaseName = getBaseName(localStreamClass.getName());

        if (!loadedClassBaseName.equals(localClassBaseName)) {
            throw new InvalidClassException(loadedStreamClass.getName(), Msg
                    .getString("KA015", loadedClassBaseName, //$NON-NLS-1$
                            localClassBaseName));
        }
    }

    private static String getBaseName(String fullName) {
        int k = fullName.lastIndexOf("."); //$NON-NLS-1$

        if (k == -1 || k == (fullName.length() - 1)) {
            return fullName;
        }
        return fullName.substring(k + 1);
    }

    // Avoid recursive defining.
    private static void checkedSetSuperClassDesc(ObjectStreamClass desc,
            ObjectStreamClass superDesc) throws StreamCorruptedException {
        if (desc.equals(superDesc)) {
            throw new StreamCorruptedException();
        }
        desc.setSuperclass(superDesc);
    }
}
