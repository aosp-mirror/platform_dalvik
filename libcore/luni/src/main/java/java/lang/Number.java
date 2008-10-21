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

package java.lang;


/**
 * Number is the abstract superclass of the classes which represent numeric base
 * types (i.e. all but Character, Boolean, and Void).
 */
public abstract class Number implements java.io.Serializable {

    private static final long serialVersionUID = -8742448824652078965L;

    /**
     * Number constructor. Included for spec compatability.
     */
    public Number() {
    }

    /**
     * Returns the byte value which the receiver represents
     * 
     * @return byte the value of the receiver.
     */
    public byte byteValue() {
        return (byte) intValue();
    }

    /**
     * Returns the double value which the receiver represents
     * 
     * @return double the value of the receiver.
     */
    public abstract double doubleValue();

    /**
     * Returns the float value which the receiver represents
     * 
     * @return float the value of the receiver.
     */
    public abstract float floatValue();

    /**
     * Returns the int value which the receiver represents
     * 
     * @return int the value of the receiver.
     */
    public abstract int intValue();

    /**
     * Returns the long value which the receiver represents
     * 
     * @return long the value of the receiver.
     */
    public abstract long longValue();

    /**
     * Returns the short value which the receiver represents
     * 
     * @return short the value of the receiver.
     */
    public short shortValue() {
        return (short) intValue();
    }
}
