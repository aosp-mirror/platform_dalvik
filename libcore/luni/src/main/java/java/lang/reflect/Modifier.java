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

package java.lang.reflect;

/**
 * This class provides methods to decode class and member modifiers.
 * 
 * @see Class#getModifiers()
 * @see Member#getModifiers()
 */
public class Modifier {

    public static final int PUBLIC = 0x1;

    public static final int PRIVATE = 0x2;

    public static final int PROTECTED = 0x4;

    public static final int STATIC = 0x8;

    public static final int FINAL = 0x10;

    public static final int SYNCHRONIZED = 0x20;

    public static final int VOLATILE = 0x40;

    public static final int TRANSIENT = 0x80;

    public static final int NATIVE = 0x100;

    public static final int INTERFACE = 0x200;

    public static final int ABSTRACT = 0x400;

    public static final int STRICT = 0x800;

    // Non-public types required by Java 5 update to class file format
    static final int BRIDGE = 0x40;

    static final int VARARGS = 0x80;

    static final int SYNTHETIC = 0x1000;

    static final int ANNOTATION = 0x2000;

    static final int ENUM = 0x4000;

    public Modifier() {
    }

    /**
     * Return true if the specified modifiers contain the <code>abstract</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the abstract modifier
     */
    public static boolean isAbstract(int modifiers) {
        return ((modifiers & ABSTRACT) != 0);
    }

    /**
     * Return true if the specified modifiers contain the <code>final</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the final modifier
     */
    public static boolean isFinal(int modifiers) {
        return ((modifiers & FINAL) != 0);
    }

    /**
     * Return true if the specified modifiers contain the <code>interface</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the interface modifier
     */
    public static boolean isInterface(int modifiers) {
        return ((modifiers & INTERFACE) != 0);
    }

    /**
     * Return true if the specified modifiers contain the <code>native</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the native modifier
     */
    public static boolean isNative(int modifiers) {
        return ((modifiers & NATIVE) != 0);
    }

    /**
     * Return true if the specified modifiers contain the <code>private</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the private modifier
     */
    public static boolean isPrivate(int modifiers) {
        return ((modifiers & PRIVATE) != 0);
    }

    /**
     * Return true if the specified modifiers contain the <code>protected</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the protected modifier
     */
    public static boolean isProtected(int modifiers) {
        return ((modifiers & PROTECTED) != 0);
    }

    /**
     * Return true if the specified modifiers contain the <code>public</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the abstract modifier
     */
    public static boolean isPublic(int modifiers) {
        return ((modifiers & PUBLIC) != 0);
    }

    /**
     * Return true if the specified modifiers contain the <code>static</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the static modifier
     */
    public static boolean isStatic(int modifiers) {
        return ((modifiers & STATIC) != 0);
    }

    /**
     * Return true if the specified modifiers contain the <code>strict</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the strict modifier
     */
    public static boolean isStrict(int modifiers) {
        return ((modifiers & STRICT) != 0);
    }

    /**
     * Return true if the specified modifiers contain the
     * <code>synchronized</code> modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the synchronized modifier
     */
    public static boolean isSynchronized(int modifiers) {
        return ((modifiers & SYNCHRONIZED) != 0);
    }

    /**
     * Return true if the specified modifiers contain the <code>transient</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the transient modifier
     */
    public static boolean isTransient(int modifiers) {
        return ((modifiers & TRANSIENT) != 0);
    }

    /**
     * Return true if the specified modifiers contain the <code>volatile</code>
     * modifier, false otherwise.
     * 
     * @param modifiers
     *            the modifiers to test
     * @return if the modifiers contain the volatile modifier
     */
    public static boolean isVolatile(int modifiers) {
        return ((modifiers & VOLATILE) != 0);
    }

    /**
     * Returns a string containing the string representation of all modifiers
     * present in the specified modifiers.
     * 
     * Modifiers appear in the order specified by the Java Language
     * Specification:
     * <code>public private protected abstract static final transient volatile native synchronized interface strict</code>
     * 
     * @param modifiers
     *            the modifiers to print
     * @return a printable representation of the modifiers
     */
    @SuppressWarnings("nls")
    public static java.lang.String toString(int modifiers) {
        StringBuffer buf;

        buf = new StringBuffer();

        if (isPublic(modifiers)) {
            buf.append("public ");
        }
        if (isProtected(modifiers)) {
            buf.append("protected ");
        }
        if (isPrivate(modifiers)) {
            buf.append("private ");
        }
        if (isAbstract(modifiers)) {
            buf.append("abstract ");
        }
        if (isStatic(modifiers)) {
            buf.append("static ");
        }
        if (isFinal(modifiers)) {
            buf.append("final ");
        }
        if (isTransient(modifiers)) {
            buf.append("transient ");
        }
        if (isVolatile(modifiers)) {
            buf.append("volatile ");
        }
        if (isSynchronized(modifiers)) {
            buf.append("synchronized ");
        }
        if (isNative(modifiers)) {
            buf.append("native ");
        }
        if (isStrict(modifiers)) {
            buf.append("strictfp ");
        }
        if (isInterface(modifiers)) {
            buf.append("interface ");
        }
        if (buf.length() == 0) {
            return "";
        }
        buf.setLength(buf.length() - 1);
        return buf.toString();
    }
}
