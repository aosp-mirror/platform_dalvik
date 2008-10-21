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

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.PriviAction;

/**
 * Instances of ObjectStreamClass are used to describe classes of objects used
 * by serialization. When objects are saved, information about all its
 * superclasses is also saved by the use of descriptors, instances of
 * ObjectStreamClass.
 * 
 * These descriptors carry information about the class they represent, such as -
 * The name of the class - SUID of the class - Field names and types
 * 
 * @see ObjectOutputStream
 * @see ObjectInputStream
 * @see java.lang.Class
 */
public class ObjectStreamClass implements Serializable {

    // No need to compute the SUID for ObjectStreamClass, just use the value
    // below
    private static final long serialVersionUID = -6120832682080437368L;

    // Name of the field that contains the SUID value (if present)
    private static final String UID_FIELD_NAME = "serialVersionUID"; //$NON-NLS-1$

    private static final int CLASS_MODIFIERS_MASK;

    private static final int FIELD_MODIFIERS_MASK;

    private static final int METHOD_MODIFIERS_MASK;

    private static final Class<?>[] READ_PARAM_TYPES;

    private static final Class<?>[] WRITE_PARAM_TYPES;

    static final Class<?>[] EMPTY_CONSTRUCTOR_PARAM_TYPES;

    private static final Class<Void> VOID_CLASS;

    static final Class<?>[] UNSHARED_PARAM_TYPES;

    private static native void oneTimeInitialization();

    static {
        oneTimeInitialization();

        CLASS_MODIFIERS_MASK = Modifier.PUBLIC | Modifier.FINAL
                | Modifier.INTERFACE | Modifier.ABSTRACT;
        FIELD_MODIFIERS_MASK = Modifier.PUBLIC | Modifier.PRIVATE
                | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
                | Modifier.VOLATILE | Modifier.TRANSIENT;
        METHOD_MODIFIERS_MASK = Modifier.PUBLIC | Modifier.PRIVATE
                | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
                | Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.ABSTRACT
                | Modifier.STRICT;

        READ_PARAM_TYPES = new Class[1];
        WRITE_PARAM_TYPES = new Class[1];
        READ_PARAM_TYPES[0] = ObjectInputStream.class;
        WRITE_PARAM_TYPES[0] = ObjectOutputStream.class;
        EMPTY_CONSTRUCTOR_PARAM_TYPES = new Class[0];
        VOID_CLASS = Void.TYPE;
        UNSHARED_PARAM_TYPES = new Class[1];
        UNSHARED_PARAM_TYPES[0] = Object.class;
    }

    /**
     * A value that indicates the class has no Serializable fields
     */
    public static final ObjectStreamField[] NO_FIELDS = new ObjectStreamField[0];

    /*
     * used to fetch field serialPersistentFields and checking its type
     */
    static final Class<?> ARRAY_OF_FIELDS;

    static {
        try {
            ARRAY_OF_FIELDS = Class.forName("[Ljava.io.ObjectStreamField;"); //$NON-NLS-1$
        } catch (ClassNotFoundException e) {
            // This should not happen
            throw new AssertionError(e);
        }
    }

    private static final String CLINIT_NAME = "<clinit>"; //$NON-NLS-1$

    private static final int CLINIT_MODIFIERS = Modifier.STATIC;

    private static final String CLINIT_SIGNATURE = "()V"; //$NON-NLS-1$

    // Used to determine if an object is Serializable or Externalizable
    private static final Class<Serializable> SERIALIZABLE = Serializable.class;

    private static final Class<Externalizable> EXTERNALIZABLE = Externalizable.class;

    // Used to test if the object is a String or a class.
    static final Class<String> STRINGCLASS = String.class;

    static final Class<?> CLASSCLASS = Class.class;

    static final Class<ObjectStreamClass> OBJECTSTREAMCLASSCLASS = ObjectStreamClass.class;

    // Table mapping instances of java.lang.Class to to corresponding instances
    // of ObjectStreamClass
    private static final WeakHashMap<Class<?>, ObjectStreamClass> classesAndDescriptors = new WeakHashMap<Class<?>, ObjectStreamClass>();

    // ClassDesc //

    // Name of the class this descriptor represents
    private transient String className;

    // Corresponding loaded class with the name above
    private transient WeakReference<Class<?>> resolvedClass;

    // Serial version UID of the class the descriptor represents
    private transient long svUID;

    // ClassDescInfo //

    // Any combination of SC_WRITE_METHOD, SC_SERIALIZABLE and SC_EXTERNALIZABLE
    // (see ObjectStreamConstants)
    private transient byte flags;

    // Descriptor for the superclass of the class associated with this
    // descriptor
    private transient ObjectStreamClass superclass;

    // Array of ObjectStreamField (see below) describing the fields of this
    // class
    private transient ObjectStreamField[] fields;

    // Array of ObjectStreamField describing the serialized fields of this class
    private transient ObjectStreamField[] loadFields;

    /*
     * If an ObjectStreamClass describes an Externalizable class, it (the
     * descriptor) should not have field descriptors (ObjectStreamField) at all.
     * The ObjectStreamClass that gets saved should simply have no field info.
     * This is a footnote in page 1511 (class Serializable) of "The Java Class
     * Libraries, Second Edition, Vol. I".
     */

    /**
     * Constructs a new instance of this class.
     */
    ObjectStreamClass() {
        super();
    }

    /**
     * Add an extra entry mapping a given class <code>cl</code> to its class
     * descriptor, which will be computed (an ObjectStreamClass). If
     * <code>computeSUID</code> is true, this method will compute the SUID for
     * this class.
     * 
     * @param cl
     *            a java.langClass for which to compute the corresponding
     *            descriptor
     * @param computeSUID
     *            a boolean indicating if SUID should be computed or not.
     * @return the computer class descriptor
     */
    private static ObjectStreamClass addToCache(Class<?> cl, boolean computeSUID) {

        ObjectStreamClass result = new ObjectStreamClass();

        // Now we fill in the values
        result.setName(cl.getName());
        result.setClass(cl);
        Class<?> superclass = cl.getSuperclass();
        if (superclass != null) {
            result.setSuperclass(lookup(superclass));
        }

        Field[] declaredFields = null;
        if (computeSUID) {
            // Lazy computation, to save speed & space
            declaredFields = cl.getDeclaredFields();
            result.setSerialVersionUID((cl.isEnum() || (cl == Enum.class)) ? 0
                    : computeSerialVersionUID(cl, declaredFields));
        }

        boolean serializable = isSerializable(cl);
        // Serializables need field descriptors
        if (serializable && !cl.isArray()) {
            if (declaredFields == null) {

                declaredFields = cl.getDeclaredFields();
            }
            result.buildFieldDescriptors(declaredFields);
        } else {
            // Externalizables or arrays do not need FieldDesc info
            result.setFields(new ObjectStreamField[0]);
        }

        byte flags = 0;
        boolean externalizable = isExternalizable(cl);
        if (externalizable) {
            flags |= ObjectStreamConstants.SC_EXTERNALIZABLE;
        } else if (serializable) {
            flags |= ObjectStreamConstants.SC_SERIALIZABLE;
        }
        if (getPrivateWriteObjectMethod(cl) != null) {
            flags |= ObjectStreamConstants.SC_WRITE_METHOD;
        }
        result.setFlags(flags);
        classesAndDescriptors.put(cl, result);

        return result;
    }

    /**
     * Builds the collection of field descriptors for the receiver
     * 
     * @param declaredFields
     *            collection of java.lang.reflect.Field for which to compute
     *            field descriptors
     */
    void buildFieldDescriptors(Field[] declaredFields) {
        // We could find the field ourselves in the collection, but calling
        // reflect is easier. Optimize if needed.
        final Field f = ObjectStreamClass.fieldSerialPersistentFields(this
                .forClass());
        // If we could not find the emulated fields, we'll have to compute
        // dumpable fields from reflect fields
        boolean useReflectFields = f == null; // Assume we will compute the
        // fields to dump based on the
        // reflect fields

        ObjectStreamField[] _fields = null;
        if (!useReflectFields) {
            // The user declared a collection of emulated fields. Use them.
            // We have to be able to fetch its value, even if it is private
            AccessController.doPrivileged(new PriviAction<Object>(f));
            try {
                // static field, pass null
                _fields = (ObjectStreamField[]) f.get(null);
            } catch (IllegalAccessException ex) {
                // WARNING - what should we do if we have no access ? This
                // should not happen.
                throw new RuntimeException(ex);
            }
        } else {
            // Compute collection of dumpable fields based on reflect fields
            List<ObjectStreamField> serializableFields = new ArrayList<ObjectStreamField>(
                    declaredFields.length);
            // Filter, we are only interested in fields that are serializable
            for (int i = 0; i < declaredFields.length; i++) {
                Field declaredField = declaredFields[i];
                int modifiers = declaredField.getModifiers();
                boolean shouldBeSerialized = !(Modifier.isStatic(modifiers) || Modifier
                        .isTransient(modifiers));
                if (shouldBeSerialized) {
                    ObjectStreamField field = new ObjectStreamField(
                            declaredField.getName(), declaredField.getType());
                    serializableFields.add(field);
                }
            }

            if (serializableFields.size() == 0) {
                _fields = NO_FIELDS; // If no serializable fields, share the
                // special value so that users can test
            } else {
                // Now convert from Vector to array
                _fields = new ObjectStreamField[serializableFields.size()];
                _fields = serializableFields.toArray(_fields);
            }
        }
        ObjectStreamField.sortFields(_fields);
        // assign offsets
        int primOffset = 0, objectOffset = 0;
        for (int i = 0; i < _fields.length; i++) {
            Class<?> type = _fields[i].getType();
            if (type.isPrimitive()) {
                _fields[i].offset = primOffset;
                primOffset += primitiveSize(type);
            } else {
                _fields[i].offset = objectOffset++;
            }
        }
        fields = _fields;
    }
    
    /**
     * Compute and return the Serial Version UID of the class <code>cl</code>.
     * The value is computed based on the class name, superclass chain, field
     * names, method names, modifiers, etc.
     * 
     * @param cl
     *            a java.lang.Class for which to compute the SUID
     * @param fields
     *            cl.getDeclaredFields(), pre-computed by the caller
     * @return the value of SUID of this class
     */
    private static long computeSerialVersionUID(Class<?> cl, Field[] fields) {
        /*
         * First we should try to fetch the static slot 'static final long
         * serialVersionUID'. If it is defined, return it. If not defined, we
         * really need to compute SUID using SHAOutputStream
         */
        for (int i = 0; i < fields.length; i++) {
            final Field field = fields[i];
            if (Long.TYPE == field.getType()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    if (UID_FIELD_NAME.equals(field.getName())) {
                        /*
                         * We need to be able to see it even if we have no
                         * visibility. That is why we set accessible first (new
                         * API in reflect 1.2)
                         */
                        AccessController.doPrivileged(new PriviAction<Object>(
                                field));
                        try {
                            // Static field, parameter is ignored
                            return field.getLong(null);
                        } catch (IllegalAccessException iae) {
                            throw new RuntimeException(Msg.getString(
                                    "K0071", iae)); //$NON-NLS-1$
                        }
                    }
                }
            }
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA"); //$NON-NLS-1$
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
        ByteArrayOutputStream sha = new ByteArrayOutputStream();
        try {
            DataOutputStream output = new DataOutputStream(sha);
            output.writeUTF(cl.getName());
            int classModifiers = CLASS_MODIFIERS_MASK & cl.getModifiers();
            /*
             * Workaround for 1F9LOQO. Arrays are ABSTRACT in JDK, but that is
             * not in the specification. Since we want to be compatible for
             * X-loading, we have to pretend we have the same shape
             */
            boolean isArray = cl.isArray();
            if (isArray) {
                classModifiers |= Modifier.ABSTRACT;
            }
            // Required for JDK UID compatibility
            if (cl.isInterface() && !Modifier.isPublic(classModifiers)) {
                classModifiers &= ~Modifier.ABSTRACT;
            }
            output.writeInt(classModifiers);

            /*
             * In JDK1.2 arrays implement Cloneable and Serializable but not in
             * JDK 1.1.7. So, JDK 1.2 "pretends" arrays have no interfaces when
             * computing SHA-1 to be compatible.
             */
            if (!isArray) {
                // Interface information
                Class<?>[] interfaces = cl.getInterfaces();
                if (interfaces.length > 1) {
                    // Only attempt to sort if really needed (saves object
                    // creation, etc)
                    Comparator<Class<?>> interfaceComparator = new Comparator<Class<?>>() {
                        public int compare(Class<?> itf1, Class<?> itf2) {
                            return itf1.getName().compareTo(itf2.getName());
                        }
                    };
                    Arrays.sort(interfaces, interfaceComparator);
                }

                // Dump them
                for (int i = 0; i < interfaces.length; i++) {
                    output.writeUTF(interfaces[i].getName());
                }
            }

            // Field information
            if (fields.length > 1) {
                // Only attempt to sort if really needed (saves object creation,
                // etc)
                Comparator<Field> fieldComparator = new Comparator<Field>() {
                    public int compare(Field field1, Field field2) {
                        return field1.getName().compareTo(field2.getName());
                    }
                };
                Arrays.sort(fields, fieldComparator);
            }

            // Dump them
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                int modifiers = field.getModifiers() & FIELD_MODIFIERS_MASK;

                boolean skip = Modifier.isPrivate(modifiers)
                        && (Modifier.isTransient(modifiers) || Modifier
                                .isStatic(modifiers));
                if (!skip) {
                    // write name, modifier & "descriptor" of all but private
                    // static and private transient
                    output.writeUTF(field.getName());
                    output.writeInt(modifiers);
                    output
                            .writeUTF(descriptorForFieldSignature(getFieldSignature(field)));
                }
            }

            /*
             * Normally constructors come before methods (because <init> <
             * anyMethodName). However, <clinit> is an exception. Besides,
             * reflect will not let us get to it.
             */
            if (hasClinit(cl)) {
                // write name, modifier & "descriptor"
                output.writeUTF(CLINIT_NAME);
                output.writeInt(CLINIT_MODIFIERS);
                output.writeUTF(CLINIT_SIGNATURE);
            }

            // Constructor information
            Constructor<?>[] constructors = cl.getDeclaredConstructors();
            if (constructors.length > 1) {
                // Only attempt to sort if really needed (saves object creation,
                // etc)
                Comparator<Constructor<?>> constructorComparator = new Comparator<Constructor<?>>() {
                    public int compare(Constructor<?> ctr1, Constructor<?> ctr2) {
                        // All constructors have same name, so we sort based on
                        // signature
                        return (getConstructorSignature(ctr1)
                                .compareTo(getConstructorSignature(ctr2)));
                    }
                };
                Arrays.sort(constructors, constructorComparator);
            }

            // Dump them
            for (int i = 0; i < constructors.length; i++) {
                Constructor<?> constructor = constructors[i];
                int modifiers = constructor.getModifiers()
                        & METHOD_MODIFIERS_MASK;
                boolean isPrivate = Modifier.isPrivate(modifiers);
                if (!isPrivate) {
                    /*
                     * write name, modifier & "descriptor" of all but private
                     * ones
                     * 
                     * constructor.getName() returns the constructor name as
                     * typed, not the VM name
                     */
                    output.writeUTF("<init>"); //$NON-NLS-1$
                    output.writeInt(modifiers);
                    output.writeUTF(descriptorForSignature(
                            getConstructorSignature(constructor)).replace('/',
                            '.'));
                }
            }

            // Method information
            Method[] methods = cl.getDeclaredMethods();
            if (methods.length > 1) {
                Comparator<Method> methodComparator = new Comparator<Method>() {
                    public int compare(Method m1, Method m2) {
                        int result = m1.getName().compareTo(m2.getName());
                        if (result == 0) {
                            // same name, signature will tell which one comes
                            // first
                            return getMethodSignature(m1).compareTo(
                                    getMethodSignature(m2));
                        }
                        return result;
                    }
                };
                Arrays.sort(methods, methodComparator);
            }

            // Dump them
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                int modifiers = method.getModifiers() & METHOD_MODIFIERS_MASK;
                boolean isPrivate = Modifier.isPrivate(modifiers);
                if (!isPrivate) {
                    // write name, modifier & "descriptor" of all but private
                    // ones
                    output.writeUTF(method.getName());
                    output.writeInt(modifiers);
                    output.writeUTF(descriptorForSignature(
                            getMethodSignature(method)).replace('/', '.'));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(Msg.getString("K0072", e));//$NON-NLS-1$
        }

        // now compute the UID based on the SHA
        byte[] hash = digest.digest(sha.toByteArray());

        return littleEndianLongAt(hash, 0);
    }

    /**
     * Return what the serializaton specification calls "descriptor" given a
     * field signature. signature.
     * 
     * @param signature
     *            a field signature
     * @return containing the descriptor
     */
    private static String descriptorForFieldSignature(String signature) {
        return signature.replace('.', '/');
    }

    /**
     * Return what the serializaton specification calls "descriptor" given a
     * method/constructor signature.
     * 
     * @param signature
     *            a method or constructor signature
     * @return containing the descriptor
     */
    private static String descriptorForSignature(String signature) {
        return signature.substring(signature.indexOf("(")); //$NON-NLS-1$
    }

    /**
     * Return the java.lang.reflect.Field <code>serialPersistentFields</code>
     * if class <code>cl</code> implements it. Return null otherwise.
     * 
     * @param cl
     *            a java.lang.Class which to test
     * @return <code>java.lang.reflect.Field</code> if the class has
     *         serialPersistentFields <code>null</code> if the class does not
     *         have serialPersistentFields
     */
    static Field fieldSerialPersistentFields(Class<?> cl) {
        try {
            Field f = cl.getDeclaredField("serialPersistentFields"); //$NON-NLS-1$
            int modifiers = f.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPrivate(modifiers)
                    && Modifier.isFinal(modifiers)) {
                if (f.getType() == ARRAY_OF_FIELDS) {
                    return f;
                }
            }
        } catch (NoSuchFieldException nsm) {
            // Ignored
        }
        return null;
    }

    /**
     * Return the class (java.lang.Class) that the receiver represents
     * 
     * @return <code>null</code> if there is no corresponding class for the
     *         receiver <code>Class</code> The loaded class corresponding to
     *         the receiver
     */
    public Class<?> forClass() {
        if (resolvedClass != null) {
            return resolvedClass.get();
        }
        return null;
    }

    /**
     * Return a String representing the signature for a Constructor
     * <code>c</code>.
     * 
     * @param c
     *            a java.lang.reflect.Constructor for which to compute the
     *            signature
     * @return the constructor's signature
     * 
     */
    static native String getConstructorSignature(Constructor<?> c);

    /**
     * Returns a given field by name.
     * 
     * @param name
     *            name of the desired field.
     * @return a given field by name.
     */
    public ObjectStreamField getField(String name) {
        ObjectStreamField[] allFields = getFields();
        for (int i = 0; i < allFields.length; i++) {
            ObjectStreamField f = allFields[i];
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    /**
     * Returns the collection of field descriptors for the fields of the
     * corresponding class
     * 
     * @return the receiver's collection of declared fields for the class it
     *         represents
     */
    ObjectStreamField[] fields() {
        if (fields == null) {
            synchronized(this){ 
                Class<?> forCl = forClass();
                if (forCl != null && isSerializable(forCl) && !forCl.isArray()) {
                    buildFieldDescriptors(forCl.getDeclaredFields());
                } else {
                    // Externalizables or arrays do not need FieldDesc info
                    setFields(new ObjectStreamField[0]);
                }
            }
        }
        return fields;
    }

    /**
     * Returns the collection of field descriptors for the fields of the
     * corresponding class
     * 
     * @return the receiver's collection of declared fields for the class it
     *         represents
     */
    public ObjectStreamField[] getFields() {
        copyFieldAttributes();
        return loadFields == null ? fields().clone() : loadFields.clone();
    }

    /**
     * If a Class uses "serialPersistentFields" to define the serialized fields, 
     * this.loadFields cannot get the "unshared" information when deserializing
     * fields using current implementation of ObjectInputStream. This method 
     * provides a way to copy the "unshared" attribute from this.fields.
     *
     */
    private void copyFieldAttributes() {
        if ((loadFields == null) || fields == null) {
            return;
        }
        
        for (int i = 0; i < loadFields.length; i++) {
            ObjectStreamField loadField = loadFields[i];
            String name = loadField.getName();
            for (int j = 0; j < fields.length; j++) {
                ObjectStreamField field = fields[j];
                if (name.equals(field.getName())) {
                    loadField.setUnshared(field.isUnshared());
                    loadField.setOffset(field.getOffset());
                    break;
                }
            }
        }
    }

    /**
     * Returns the collection of field descriptors for the input fields of the
     * corresponding class
     * 
     * @return the receiver's collection of input fields for the class it
     *         represents
     */
    ObjectStreamField[] getLoadFields() {
        return loadFields;
    }

    /**
     * Return a String representing the signature for a field <code>f</code>.
     * 
     * @param f
     *            a java.lang.reflect.Field for which to compute the signature
     * @return the field's signature
     */
    private static native String getFieldSignature(Field f);

    /**
     * Returns the flags for this descriptor, where possible combined values are
     * 
     * ObjectStreamConstants.SC_WRITE_METHOD
     * ObjectStreamConstants.SC_SERIALIZABLE
     * ObjectStreamConstants.SC_EXTERNALIZABLE
     * 
     * @return byte the receiver's flags for the class it represents
     */
    byte getFlags() {
        return flags;
    }

    /**
     * Return a String representing the signature for a method <code>m</code>.
     * 
     * @param m
     *            a java.lang.reflect.Method for which to compute the signature
     * @return the method's signature
     */
    static native String getMethodSignature(Method m);

    /**
     * Returns the name of the class represented by the receiver
     * 
     * @return fully qualified name of the class the receiver represents
     */
    public String getName() {
        return className;
    }

    /**
     * Returns the Serial Version User ID of the class represented by the
     * receiver
     * 
     * @return SUID for the class represented by the receiver
     */
    public long getSerialVersionUID() {
        return svUID;
    }

    /**
     * Returns the descriptor (ObjectStreamClass) of the superclass of the class
     * represented by the receiver.
     * 
     * @return an ObjectStreamClass representing the superclass of the class
     *         represented by the receiver.
     */
    ObjectStreamClass getSuperclass() {
        return superclass;
    }

    /**
     * Return true if the given class <code>cl</code> has the
     * compiler-generated method <code>clinit</code>. Even though it is
     * compiler-generated, it is used by the serialization code to compute SUID.
     * This is unfortunate, since it may depend on compiler optimizations in
     * some cases.
     * 
     * @param cl
     *            a java.lang.Class which to test
     * @return <code>true</code> if the class has <clinit> <code>false</code>
     *         if the class does not have <clinit>
     */
    private static native boolean hasClinit(Class<?> cl);

    /**
     * Return true if the given class <code>cl</code> implements private
     * method <code>readObject()</code>.
     * 
     * @param cl
     *            a java.lang.Class which to test
     * @return <code>true</code> if the class implements readObject
     *         <code>false</code> if the class does not implement readObject
     */
    static Method getPrivateReadObjectMethod(Class<?> cl) {
        try {
            Method method = cl
                    .getDeclaredMethod("readObject", READ_PARAM_TYPES); //$NON-NLS-1$
            if (Modifier.isPrivate(method.getModifiers())
                    && method.getReturnType() == VOID_CLASS) {
                return method;
            }
        } catch (NoSuchMethodException nsm) {
            // Ignored
        }
        return null;
    }

    /**
     * Return true if the given class <code>cl</code> implements private
     * method <code>readObject()</code>.
     * 
     * @param cl
     *            a java.lang.Class which to test
     * @return <code>true</code> if the class implements readObject
     *         <code>false</code> if the class does not implement readObject
     */
    static Method getPrivateReadObjectNoDataMethod(Class<?> cl) {
        try {
            Method method = cl.getDeclaredMethod("readObjectNoData", //$NON-NLS-1$
                    EMPTY_CONSTRUCTOR_PARAM_TYPES);
            if (Modifier.isPrivate(method.getModifiers())
                    && method.getReturnType() == VOID_CLASS) {
                return method;
            }
        } catch (NoSuchMethodException nsm) {
            // Ignored
        }
        return null;
    }

    /**
     * Return true if the given class <code>cl</code> implements private
     * method <code>writeObject()</code>.
     * 
     * @param cl
     *            a java.lang.Class which to test
     * @return <code>true</code> if the class implements writeObject
     *         <code>false</code> if the class does not implement writeObject
     */
    static Method getPrivateWriteObjectMethod(Class<?> cl) {
        try {
            Method method = cl.getDeclaredMethod("writeObject", //$NON-NLS-1$
                    WRITE_PARAM_TYPES);
            if (Modifier.isPrivate(method.getModifiers())
                    && method.getReturnType() == VOID_CLASS) {
                return method;
            }
        } catch (NoSuchMethodException nsm) {
            // Ignored
        }
        return null;
    }

    /**
     * Return true if instances of class <code>cl</code> are Externalizable,
     * false otherwise.
     * 
     * @param cl
     *            a java.lang.Class which to test
     * @return <code>true</code> if instances of the class are Externalizable
     *         <code>false</code> if instances of the class are not
     *         Externalizable
     * 
     * @see Object#hashCode
     */
    static boolean isExternalizable(Class<?> cl) {
        return EXTERNALIZABLE.isAssignableFrom(cl);
    }

    /**
     * Return true if the type code
     * <code>typecode<code> describes a primitive type
     *
     * @param typecode a char describing the typecode
     * @return <code>true</code> if the typecode represents a primitive type 
     * <code>false</code> if the typecode represents an Object type (including arrays)
     *
     * @see Object#hashCode
     */
    static boolean isPrimitiveType(char typecode) {
        return !(typecode == '[' || typecode == 'L');
    }

    /**
     * Return true if instances of class <code>cl</code> are Serializable,
     * false otherwise.
     * 
     * @param cl
     *            a java.lang.Class which to test
     * @return <code>true</code> if instances of the class are Serializable
     *         <code>false</code> if instances of the class are not
     *         Serializable
     * 
     * @see Object#hashCode
     */
    static boolean isSerializable(Class<?> cl) {
        return SERIALIZABLE.isAssignableFrom(cl);
    }

    /**
     * Return a little endian long stored in a given position of the buffer
     * 
     * @param buffer
     *            a byte array with the byte representation of the number
     * @param position
     *            index where the number starts in the byte array
     * @return the number that was stored in little endian format
     */
    private static long littleEndianLongAt(byte[] buffer, int position) {
        long result = 0;
        for (int i = position + 7; i >= position; i--) {
            result = (result << 8) + (buffer[i] & 0xff);
        }
        return result;
    }

    /**
     * Return the descriptor (ObjectStreamClass) corresponding to the class
     * <code>cl</code>. If the class is not Serializable or Externalizable,
     * null is returned.
     * 
     * @param cl
     *            a java.langClass for which to obtain the corresponding
     *            descriptor
     * @return <code>null</code> if instances of the class <code>cl</code>
     *         are not Serializable or Externalizable
     *         <code>ObjectStreamClass</code> The corresponding descriptor if
     *         the class <code>cl</code> is Serializable or Externalizable
     */
    public static ObjectStreamClass lookup(Class<?> cl) {
        boolean serializable = isSerializable(cl);
        boolean externalizable = isExternalizable(cl);

        // Has to be either Serializable or Externalizable
        if (!serializable && !externalizable) {
            return null;
        }

        return lookupStreamClass(cl, true);
    }

    /**
     * Return the descriptor (ObjectStreamClass) corresponding to the class
     * <code>cl</code>. Returns an ObjectStreamClass even if instances of the
     * class cannot be serialized
     * 
     * @param cl
     *            a java.langClass for which to obtain the corresponding
     *            descriptor
     * @return the corresponding descriptor
     */
    static ObjectStreamClass lookupStreamClass(Class<?> cl) {
        return lookupStreamClass(cl, isSerializable(cl) || isExternalizable(cl));
    }

    /**
     * Return the descriptor (ObjectStreamClass) corresponding to the class
     * <code>cl</code>. Returns an ObjectStreamClass even if instances of the
     * class cannot be serialized
     * 
     * @param cl
     *            a <code>java.langClass</code> for which to obtain the
     *            corresponding descriptor
     * @param computeSUID
     *            a boolean indicating if SUID should be computed or not.
     * @return the corresponding descriptor
     */
    private static synchronized ObjectStreamClass lookupStreamClass(
            Class<?> cl, boolean computeSUID) {
        // Synchronized because of the lookup table 'classesAndDescriptors'
        ObjectStreamClass cachedValue = classesAndDescriptors.get(cl);
        if (cachedValue != null) {
            return cachedValue;
        }
        return addToCache(cl, computeSUID);
    }

    /**
     * Return the java.lang.reflect.Method <code>readResolve</code> if class
     * <code>cl</code> implements it. Return null otherwise.
     * 
     * @param cl
     *            a java.lang.Class which to test
     * @return <code>java.lang.reflect.Method</code> if the class implements
     *         readResolve <code>null</code> if the class does not implement
     *         readResolve
     */
    static Method methodReadResolve(Class<?> cl) {
        Class<?> search = cl;
        while (search != null) {
            try {
                Method method = search.getDeclaredMethod(
                        "readResolve", (Class[]) null); //$NON-NLS-1$
                if (search == cl
                        || (method.getModifiers() & Modifier.PRIVATE) == 0) {
                    return method;
                }
                return null;
            } catch (NoSuchMethodException nsm) {
            }
            search = search.getSuperclass();
        }
        return null;
    }

    /**
     * Return the java.lang.reflect.Method <code>writeReplace</code> if class
     * <code>cl</code> implements it. Return null otherwise.
     * 
     * @param cl
     *            a java.lang.Class which to test
     * @return <code>java.lang.reflect.Method</code> if the class implements
     *         writeReplace <code>null</code> if the class does not implement
     *         writeReplace
     */
    static Method methodWriteReplace(Class<?> cl) {
        Class<?> search = cl;
        while (search != null) {
            try {
                Method method = search.getDeclaredMethod(
                        "writeReplace", (Class[]) null); //$NON-NLS-1$
                if (search == cl
                        || (method.getModifiers() & Modifier.PRIVATE) == 0) {
                    return method;
                }
                return null;
            } catch (NoSuchMethodException nsm) {
                // Ignored
            }
            search = search.getSuperclass();
        }
        return null;
    }

    /**
     * Set the class (java.lang.Class) that the receiver represents
     * 
     * @param c
     *            aClass, the new class that the receiver describes
     */
    void setClass(Class<?> c) {
        resolvedClass = new WeakReference<Class<?>>(c);
    }

    /**
     * Set the collection of field descriptors for the fields of the
     * corresponding class
     * 
     * @param f
     *            ObjectStreamField[], the receiver's new collection of declared
     *            fields for the class it represents
     */
    void setFields(ObjectStreamField[] f) {
        fields = f;
    }

    /**
     * Set the collection of field descriptors for the input fields of the
     * corresponding class
     * 
     * @param f
     *            ObjectStreamField[], the receiver's new collection of input
     *            fields for the class it represents
     */
    void setLoadFields(ObjectStreamField[] f) {
        loadFields = f;
    }

    /**
     * Set the flags for this descriptor, where possible combined values are
     * 
     * ObjectStreamConstants.SC_WRITE_METHOD
     * ObjectStreamConstants.SC_SERIALIZABLE
     * ObjectStreamConstants.SC_EXTERNALIZABLE
     * 
     * @param b
     *            byte, the receiver's new flags for the class it represents
     */
    void setFlags(byte b) {
        flags = b;
    }

    /**
     * Set the name of the class represented by the receiver
     * 
     * @param newName
     *            a String, the new fully qualified name of the class the
     *            receiver represents
     */
    void setName(String newName) {
        className = newName;
    }

    /**
     * Set the Serial Version User ID of the class represented by the receiver
     * 
     * @param l
     *            a long, the new SUID for the class represented by the receiver
     */
    void setSerialVersionUID(long l) {
        svUID = l;
    }

    /**
     * Set the descriptor for the superclass of the class described by the
     * receiver
     * 
     * @param c
     *            an ObjectStreamClass, the new ObjectStreamClass for the
     *            superclass of the class represented by the receiver
     */
    void setSuperclass(ObjectStreamClass c) {
        superclass = c;
    }

    private int primitiveSize(Class<?> type) {
        if (type == Byte.TYPE || type == Boolean.TYPE) {
            return 1;
        }
        if (type == Short.TYPE || type == Character.TYPE) {
            return 2;
        }
        if (type == Integer.TYPE || type == Float.TYPE) {
            return 4;
        }
        if (type == Long.TYPE || type == Double.TYPE) {
            return 8;
        }
        return 0;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a printable representation for the receiver.
     */
    @Override
    public String toString() {
        return getName() + ": static final long serialVersionUID =" //$NON-NLS-1$
                + getSerialVersionUID() + "L;"; //$NON-NLS-1$
    }
}
