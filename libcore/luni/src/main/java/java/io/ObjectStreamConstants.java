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
 * Helper interface with constants used by the serialization implementation.
 */
public abstract interface ObjectStreamConstants {

    /**
     * Used for the stream header
     */
    public static final short STREAM_MAGIC = (short) 0xaced;

    /**
     * Used for the stream header
     */
    public static final short STREAM_VERSION = 5;

    // These are tags to indicate the stream contents
    public static final byte TC_BASE = 0x70;

    public static final byte TC_NULL = (byte) 0x70;

    public static final byte TC_REFERENCE = (byte) 0x71;

    public static final byte TC_CLASSDESC = (byte) 0x72;

    public static final byte TC_OBJECT = (byte) 0x73;

    public static final byte TC_STRING = (byte) 0x74;

    public static final byte TC_ARRAY = (byte) 0x75;

    public static final byte TC_CLASS = (byte) 0x76;

    public static final byte TC_BLOCKDATA = (byte) 0x77;

    public static final byte TC_ENDBLOCKDATA = (byte) 0x78;

    public static final byte TC_RESET = (byte) 0x79;

    public static final byte TC_BLOCKDATALONG = (byte) 0x7A;

    public static final byte TC_EXCEPTION = (byte) 0x7B;

    public static final byte TC_LONGSTRING = (byte) 0x7C;

    public static final byte TC_PROXYCLASSDESC = (byte) 0x7D;

    public static final byte TC_MAX = 0x7E;

    /**
     * The first object dumped gets assigned this handle/ID
     */
    public static final int baseWireHandle = 0x007e0000;

    public static final int PROTOCOL_VERSION_1 = 1;

    public static final int PROTOCOL_VERSION_2 = 2;

    public static final SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION = new SerializablePermission(
            "enableSubclassImplementation"); //$NON-NLS-1$

    public static final SerializablePermission SUBSTITUTION_PERMISSION = new SerializablePermission(
            "enableSubstitution"); //$NON-NLS-1$

    // Flags that indicate if the object was serializable, externalizable
    // and had a writeObject method when dumped.
    public static final byte SC_WRITE_METHOD = 0x01; // If SC_SERIALIZABLE

    public static final byte SC_SERIALIZABLE = 0x02;

    public static final byte SC_EXTERNALIZABLE = 0x04;

    public static final byte SC_BLOCK_DATA = 0x08; // If SC_EXTERNALIZABLE

    /**
     * constant for new enum
     */
    public static final byte TC_ENUM = 0x7E;

    /**
     * the bitmask denoting that the object is a enum
     */
    public static final byte SC_ENUM = 0x10;
}
