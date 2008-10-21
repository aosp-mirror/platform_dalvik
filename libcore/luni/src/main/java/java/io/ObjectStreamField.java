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
import java.util.Arrays;
import java.util.Comparator;

/**
 * This class represents object fields that are saved to the stream, by
 * serialization. Classes can define the collection of fields to be dumped,
 * which can differ from the actual object's declared fields.
 * 
 * @see ObjectOutputStream#writeFields()
 * @see ObjectInputStream#readFields()
 */
public class ObjectStreamField implements Comparable<Object> {

    // Declared name of the field
    private String name;

    // Declared type of the field
    private Object type;

    // offset of this field in the object
    int offset;

    // Cached version of intern'ed type String
    private String typeString;

    private boolean unshared;

    private boolean isDeserialized;

    /**
     * Constructs an ObjectStreamField with the given name and the given type
     * 
     * @param name
     *            a String, the name of the field
     * @param cl
     *            A Class object representing the type of the field
     */
    public ObjectStreamField(String name, Class<?> cl) {
        if (name == null || cl == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.type = new WeakReference<Class<?>>(cl);
    }

    /**
     * Constructs an ObjectStreamField with the given name and the given type
     * 
     * @param name
     *            a String, the name of the field
     * @param cl
     *            A Class object representing the type of the field
     * @param unshared
     *            write and read the field unshared
     */
    public ObjectStreamField(String name, Class<?> cl, boolean unshared) {
        if (name == null || cl == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.type = (cl.getClassLoader() == null) ? cl
                : new WeakReference<Class<?>>(cl);
        this.unshared = unshared;
    }

    /**
     * Constructs an ObjectStreamField with the given name and the given type.
     * The type may be null.
     * 
     * @param signature
     *            A String representing the type of the field
     * @param name
     *            a String, the name of the field, or null
     */
    ObjectStreamField(String signature, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.typeString = signature.replace('.', '/').intern();
        this.isDeserialized = true;
    }

    /**
     * Comparing the receiver to the parameter, according to the Comparable
     * interface.
     * 
     * @param o
     *            The object to compare against
     * 
     * @return -1 if the receiver is "smaller" than the parameter. 0 if the
     *         receiver is "equal" to the parameter. 1 if the receiver is
     *         "greater" than the parameter.
     */
    public int compareTo(Object o) {
        ObjectStreamField f = (ObjectStreamField) o;
        boolean thisPrimitive = this.isPrimitive();
        boolean fPrimitive = f.isPrimitive();

        // If one is primitive and the other isn't, we have enough info to
        // compare
        if (thisPrimitive != fPrimitive) {
            return thisPrimitive ? -1 : 1;
        }

        // Either both primitives or both not primitives. Compare based on name.
        return this.getName().compareTo(f.getName());
    }

    @Override
    public boolean equals(Object arg0) {
        return compareTo(arg0) == 0;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Return the name of the field the receiver represents
     * 
     * @return a String, the name of the field
     */
    public String getName() {
        return name;
    }

    /**
     * Return the offset of this field in the object
     * 
     * @return an int, the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Return the type of the field the receiver represents, this is an internal
     * method
     * 
     * @return A Class object representing the type of the field
     */
    // BEGIN android-note
    // Changed from private to default visibility for usage in ObjectStreamClass
    // END android-note
    /* package */ Class<?> getTypeInternal() {
        if (type instanceof WeakReference) {
            return (Class<?>) ((WeakReference<?>) type).get();
        }
        return (Class<?>) type;
    }

    /**
     * Return the type of the field the receiver represents
     * 
     * @return A Class object representing the type of the field
     */
    public Class<?> getType() {
        Class<?> cl = getTypeInternal();
        if (isDeserialized && !cl.isPrimitive()) {
            return Object.class;
        }
        return cl;
    }

    /**
     * Return the type code that corresponds to the class the receiver
     * represents
     * 
     * @return A char, the typecode of the class
     */
    public char getTypeCode() {
        Class<?> t = getTypeInternal();
        if (t == Integer.TYPE) {
            return 'I';
        }
        if (t == Byte.TYPE) {
            return 'B';
        }
        if (t == Character.TYPE) {
            return 'C';
        }
        if (t == Short.TYPE) {
            return 'S';
        }
        if (t == Boolean.TYPE) {
            return 'Z';
        }
        if (t == Long.TYPE) {
            return 'J';
        }
        if (t == Float.TYPE) {
            return 'F';
        }
        if (t == Double.TYPE) {
            return 'D';
        }
        if (t.isArray()) {
            return '[';
        }
        return 'L';
    }

    /**
     * Return the type signature used by the VM to represent the type for this
     * field.
     * 
     * @return A String, the signature for the class of this field.
     */
    public String getTypeString() {
        if (isPrimitive()) {
            return null;
        }
        if (typeString == null) {
            Class<?> t = getTypeInternal();
            String typeName = t.getName().replace('.', '/');
            String str = (t.isArray()) ? typeName : ("L" + typeName + ';'); //$NON-NLS-1$
            typeString = str.intern();
        }
        return typeString;
    }

    /**
     * Return a boolean indicating whether the class of this field is a
     * primitive type or not
     * 
     * @return true if the type of this field is a primitive type false if the
     *         type of this field is a regular class.
     */
    public boolean isPrimitive() {
        Class<?> t = getTypeInternal();
        return t != null && t.isPrimitive();
    }

    /**
     * Set the offset this field represents in the object
     * 
     * @param newValue
     *            an int, the offset
     */
    protected void setOffset(int newValue) {
        this.offset = newValue;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a printable representation for the receiver.
     */
    @Override
    public String toString() {
        return this.getClass().getName() + '(' + getName() + ':'
                + getTypeInternal() + ')';
    }

    /**
     * Sorts the fields for dumping. Primitive types come first, then regular
     * types.
     * 
     * @param fields
     *            ObjectStreamField[] fields to be sorted
     */
    static void sortFields(ObjectStreamField[] fields) {
        // Sort if necessary
        if (fields.length > 1) {
            Comparator<ObjectStreamField> fieldDescComparator = new Comparator<ObjectStreamField>() {
                public int compare(ObjectStreamField f1, ObjectStreamField f2) {
                    return f1.compareTo(f2);
                }
            };
            Arrays.sort(fields, fieldDescComparator);
        }
    }

    void resolve(ClassLoader loader) {
        if (typeString.length() == 1) {
            switch (typeString.charAt(0)) {
                case 'I':
                    type = Integer.TYPE;
                    return;
                case 'B':
                    type = Byte.TYPE;
                    return;
                case 'C':
                    type = Character.TYPE;
                    return;
                case 'S':
                    type = Short.TYPE;
                    return;
                case 'Z':
                    type = Boolean.TYPE;
                    return;
                case 'J':
                    type = Long.TYPE;
                    return;
                case 'F':
                    type = Float.TYPE;
                    return;
                case 'D':
                    type = Double.TYPE;
                    return;
            }
        }
        String className = typeString.replace('/', '.');
        if (className.charAt(0) == 'L') {
            // remove L and ;
            className = className.substring(1, className.length() - 1);
        }
        try {
            Class<?> cl = Class.forName(className, false, loader);
            type = (cl.getClassLoader() == null) ? cl
                    : new WeakReference<Class<?>>(cl);
        } catch (ClassNotFoundException e) {
            // Ignored
        }
    }

    /**
     * Returns whether this serialized field is unshared.
     * 
     * @return true if the field is unshared, false otherwise.
     */
    public boolean isUnshared() {
        return unshared;
    }
    
    void setUnshared(boolean unshared) {
        this.unshared = unshared;
    }
}
