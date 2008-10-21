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

/**
 * An EmulatedFields is an object that represents a set of emulated fields for
 * an object being dumped or loaded. It allows objects to be dumped with a shape
 * different than the fields they were declared to have.
 * 
 * @see ObjectInputStream.GetField
 * @see ObjectOutputStream.PutField
 * @see EmulatedFieldsForLoading
 * @see EmulatedFieldsForDumping
 */
class EmulatedFields {

    // A slot is a field plus its value
    static class ObjectSlot {

        // Field descriptor
        ObjectStreamField field;

        // Actual value this emulated field holds
        Object fieldValue;

        // If this field has a default value (true) or something has been
        // assigned (false)
        boolean defaulted = true;

        /**
         * Returns the descriptor for this emulated field.
         * 
         * @return the field descriptor
         */
        public ObjectStreamField getField() {
            return field;
        }

        /**
         * Returns the value held by this emulated field.
         * 
         * @return the field value
         */
        public Object getFieldValue() {
            return fieldValue;
        }
    }

    // The collection of slots the receiver represents
    private ObjectSlot[] slotsToSerialize;

    private ObjectStreamField[] declaredFields;

    /**
     * Constructs a new instance of EmulatedFields.
     * 
     * @param fields
     *            an array of ObjectStreamFields, which describe the fields to
     *            be emulated (names, types, etc).
     * @param declared
     *            an array of ObjectStreamFields, which describe the declared
     *            fields.
     */
    public EmulatedFields(ObjectStreamField[] fields,
            ObjectStreamField[] declared) {
        super();
        // We assume the slots are already sorted in the right shape for dumping
        buildSlots(fields);
        declaredFields = declared;
    }

    /**
     * Build emulated slots that correspond to emulated fields. A slot is a
     * field descriptor (ObjectStreamField) plus the actual value it holds.
     * 
     * @param fields
     *            an array of ObjectStreamField, which describe the fields to be
     *            emulated (names, types, etc).
     */
    private void buildSlots(ObjectStreamField[] fields) {
        slotsToSerialize = new ObjectSlot[fields.length];
        for (int i = 0; i < fields.length; i++) {
            ObjectSlot s = new ObjectSlot();
            slotsToSerialize[i] = s;
            s.field = fields[i];
        }
        // We assume the slots are already sorted in the right shape for dumping
    }

    /**
     * Return a boolean indicating if the field named <code>name</code> has
     * been assigned a value explicitly (false) or if it still holds a default
     * value for the type (true) because it hasn't been assigned to yet.
     * 
     * @param name
     *            a String, the name of the field to test
     * @return <code>true</code> if <code>name</code> still holds its
     *         default value, <code>false</code> otherwise
     * 
     * @throws IllegalArgumentException
     *             If <code>name</code> is null
     */
    public boolean defaulted(String name) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, null);
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        return slot.defaulted;
    }

    /**
     * Find and return an ObjectSlot that corresponds to a field named
     * <code>fieldName</code> and type <code>fieldType</code>. If the field
     * type <code>fieldType</code> corresponds to a primitive type, the field
     * type has to match exactly or <code>null</code> is returned. If the
     * field type <code>fieldType</code> corresponds to an object type, the
     * field type has to be compatible in terms of assignment, or null is
     * returned. If <code>fieldType</code> is <code>null</code>, no such
     * compatibility checking is performed and the slot is returned.
     * 
     * @param fieldName
     *            A String, the name of the field to find
     * @param fieldType
     *            A Class, the type of the field. This will be used to test
     *            compatibility. If null, no testing is done, the corresponding
     *            slot is returned.
     * @return If there is no field with that name, or no compatible field
     *         (relative to <code>fieldType</code>)
     */
    private ObjectSlot findSlot(String fieldName, Class<?> fieldType) {
        boolean isPrimitive = fieldType != null && fieldType.isPrimitive();

        for (int i = 0; i < slotsToSerialize.length; i++) {
            ObjectSlot slot = slotsToSerialize[i];
            if (slot.field.getName().equals(fieldName)) {
                if (isPrimitive) {
                    // Looking for a primitive type field. Types must match
                    // *exactly*
                    if (slot.field.getType() == fieldType) {
                        return slot;
                    }
                } else {
                    // Looking for a non-primitive type field.
                    if (fieldType == null) {
                        return slot; // Null means we take anything
                    }
                    // Types must be compatible (assignment)
                    if (slot.field.getType().isAssignableFrom(fieldType)) {
                        return slot;
                    }
                }
            }
        }

        if (declaredFields != null) {
            for (int i = 0; i < declaredFields.length; i++) {
                ObjectStreamField field = declaredFields[i];
                if (field.getName().equals(fieldName)) {
                    if (isPrimitive ? field.getType() == fieldType
                            : fieldType == null
                                    || field.getType().isAssignableFrom(
                                            fieldType)) {
                        ObjectSlot slot = new ObjectSlot();
                        slot.field = field;
                        slot.defaulted = true;
                        return slot;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find and return the byte value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     * 
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, the default
     *         value otherwise
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public byte get(String name, byte defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Byte.TYPE);
        // if not initialized yet, we give the default value
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        return slot.defaulted ? defaultValue : ((Byte) slot.fieldValue)
                .byteValue();
    }

    /**
     * Find and return the char value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     * 
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, the default
     *         value otherwise
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public char get(String name, char defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Character.TYPE);
        // if not initialized yet, we give the default value
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        return slot.defaulted ? defaultValue : ((Character) slot.fieldValue)
                .charValue();
    }

    /**
     * Find and return the double value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     * 
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, the default
     *         value otherwise
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public double get(String name, double defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Double.TYPE);
        // if not initialized yet, we give the default value
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        return slot.defaulted ? defaultValue : ((Double) slot.fieldValue)
                .doubleValue();
    }

    /**
     * Find and return the float value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     * 
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, the default
     *         value otherwise
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public float get(String name, float defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Float.TYPE);
        // if not initialized yet, we give the default value
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        return slot.defaulted ? defaultValue : ((Float) slot.fieldValue)
                .floatValue();
    }

    /**
     * Find and return the int value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     * 
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, the default
     *         value otherwise
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public int get(String name, int defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Integer.TYPE);
        // if not initialized yet, we give the default value
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        return slot.defaulted ? defaultValue : ((Integer) slot.fieldValue)
                .intValue();
    }

    /**
     * Find and return the long value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     * 
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, the default
     *         value otherwise
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public long get(String name, long defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Long.TYPE);
        // if not initialized yet, we give the default value
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        return slot.defaulted ? defaultValue : ((Long) slot.fieldValue)
                .longValue();
    }

    /**
     * Find and return the Object value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     * 
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, the default
     *         value otherwise
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public Object get(String name, Object defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, null);
        // if not initialized yet, we give the default value
        if (slot == null || slot.field.getType().isPrimitive()) {
            throw new IllegalArgumentException();
        }
        return slot.defaulted ? defaultValue : slot.fieldValue;
    }

    /**
     * Find and return the short value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     * 
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, the default
     *         value otherwise
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public short get(String name, short defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Short.TYPE);
        // if not initialized yet, we give the default value
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        return slot.defaulted ? defaultValue : ((Short) slot.fieldValue)
                .shortValue();
    }

    /**
     * Find and return the boolean value of a given field named
     * <code>name</code> in the receiver. If the field has not been assigned
     * any value yet, the default value <code>defaultValue</code> is returned
     * instead.
     * 
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, the default
     *         value otherwise
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public boolean get(String name, boolean defaultValue)
            throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Boolean.TYPE);
        // if not initialized yet, we give the default value
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        return slot.defaulted ? defaultValue : ((Boolean) slot.fieldValue)
                .booleanValue();
    }

    /**
     * Find and set the byte value of a given field named <code>name</code> in
     * the receiver.
     * 
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public void put(String name, byte value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Byte.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        slot.fieldValue = Byte.valueOf(value);
        slot.defaulted = false; // No longer default value
    }

    /**
     * Find and set the char value of a given field named <code>name</code> in
     * the receiver.
     * 
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public void put(String name, char value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Character.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        slot.fieldValue = Character.valueOf(value);
        slot.defaulted = false; // No longer default value
    }

    /**
     * Find and set the double value of a given field named <code>name</code>
     * in the receiver.
     * 
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public void put(String name, double value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Double.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        slot.fieldValue = Double.valueOf(value);
        slot.defaulted = false; // No longer default value
    }

    /**
     * Find and set the float value of a given field named <code>name</code>
     * in the receiver.
     * 
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public void put(String name, float value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Float.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        slot.fieldValue = Float.valueOf(value);
        slot.defaulted = false; // No longer default value
    }

    /**
     * Find and set the int value of a given field named <code>name</code> in
     * the receiver.
     * 
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public void put(String name, int value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Integer.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        slot.fieldValue = Integer.valueOf(value);
        slot.defaulted = false; // No longer default value
    }

    /**
     * Find and set the long value of a given field named <code>name</code> in
     * the receiver.
     * 
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public void put(String name, long value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Long.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        slot.fieldValue = Long.valueOf(value);
        slot.defaulted = false; // No longer default value
    }

    /**
     * Find and set the Object value of a given field named <code>name</code>
     * in the receiver.
     * 
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public void put(String name, Object value) throws IllegalArgumentException {
        Class<?> valueClass = null;
        if (value != null) {
            valueClass = value.getClass();
        }
        ObjectSlot slot = findSlot(name, valueClass);
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        slot.fieldValue = value;
        slot.defaulted = false; // No longer default value
    }

    /**
     * Find and set the short value of a given field named <code>name</code>
     * in the receiver.
     * 
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public void put(String name, short value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Short.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        slot.fieldValue = Short.valueOf(value);
        slot.defaulted = false; // No longer default value
    }

    /**
     * Find and set the boolean value of a given field named <code>name</code>
     * in the receiver.
     * 
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     * 
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    public void put(String name, boolean value) throws IllegalArgumentException {
        ObjectSlot slot = findSlot(name, Boolean.TYPE);
        if (slot == null) {
            throw new IllegalArgumentException();
        }
        slot.fieldValue = Boolean.valueOf(value);
        slot.defaulted = false; // No longer default value
    }

    /**
     * Return the array of ObjectSlot the receiver represents.
     * 
     * @return array of ObjectSlot the receiver represents.
     */
    public ObjectSlot[] slots() {
        return slotsToSerialize;
    }
}
