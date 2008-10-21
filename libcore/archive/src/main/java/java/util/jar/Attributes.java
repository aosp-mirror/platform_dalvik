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

package java.util.jar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.harmony.archive.util.Util;

/**
 * The Attributes class is used to store values for Manifest entries. Attributes
 * keys are generally instances of Attributes.Name. Values associated with
 * Attributes keys are of type String.
 */
public class Attributes implements Cloneable, Map<Object, Object> {

    protected Map<Object, Object> map;

    public static class Name {
        private final String name;

        private int hashCode;

        public static final Name CLASS_PATH = new Name("Class-Path"); //$NON-NLS-1$

        public static final Name MANIFEST_VERSION = new Name(
                "Manifest-Version"); //$NON-NLS-1$

        public static final Name MAIN_CLASS = new Name("Main-Class"); //$NON-NLS-1$

        public static final Name SIGNATURE_VERSION = new Name(
                "Signature-Version"); //$NON-NLS-1$

        public static final Name CONTENT_TYPE = new Name("Content-Type"); //$NON-NLS-1$

        public static final Name SEALED = new Name("Sealed"); //$NON-NLS-1$

        public static final Name IMPLEMENTATION_TITLE = new Name(
                "Implementation-Title"); //$NON-NLS-1$

        public static final Name IMPLEMENTATION_VERSION = new Name(
                "Implementation-Version"); //$NON-NLS-1$

        public static final Name IMPLEMENTATION_VENDOR = new Name(
                "Implementation-Vendor"); //$NON-NLS-1$

        public static final Name SPECIFICATION_TITLE = new Name(
                "Specification-Title"); //$NON-NLS-1$

        public static final Name SPECIFICATION_VERSION = new Name(
                "Specification-Version"); //$NON-NLS-1$

        public static final Name SPECIFICATION_VENDOR = new Name(
                "Specification-Vendor"); //$NON-NLS-1$

        public static final Name EXTENSION_LIST = new Name("Extension-List"); //$NON-NLS-1$

        public static final Name EXTENSION_NAME = new Name("Extension-Name"); //$NON-NLS-1$

        public static final Name EXTENSION_INSTALLATION = new Name(
                "Extension-Installation"); //$NON-NLS-1$

        public static final Name IMPLEMENTATION_VENDOR_ID = new Name(
                "Implementation-Vendor-Id"); //$NON-NLS-1$

        public static final Name IMPLEMENTATION_URL = new Name(
                "Implementation-URL"); //$NON-NLS-1$

        public Name(String s) {
            int i = s.length();
            if (i == 0 || i > 70) {
                throw new IllegalArgumentException();
            }
            for (; --i >= 0;) {
                char ch = s.charAt(i);
                if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                        || ch == '_' || ch == '-' || (ch >= '0' && ch <= '9'))) {
                    throw new IllegalArgumentException(s);
                }
            }
            name = s;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object an) {
            if (an == null) {
                return false;
            }
            return an.getClass() == this.getClass()
                    && name.equalsIgnoreCase(((Name) an).name);
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = Util.toASCIILowerCase("name").hashCode();
            }
            return hashCode;
        }
    }

    /**
     * Constructs an Attributes instance
     */
    public Attributes() {
        map = new HashMap<Object, Object>();
    }

    /**
     * Constructs an Attributes instance obtaining keys and values from the
     * parameter Attributes, attrib
     * 
     * @param attrib
     *            The Attributes to obtain entries from.
     */
    @SuppressWarnings("unchecked")
    public Attributes(Attributes attrib) {
        map = (Map<Object, Object>)((HashMap) attrib.map).clone();
    }

    /**
     * Constructs an Attributes instance with initial capacity of size size
     * 
     * @param size
     *            Initial size of this Attributes instance.
     */
    public Attributes(int size) {
        map = new HashMap<Object, Object>(size);
    }

    /**
     * Removes all key/value pairs from this Attributes.
     * 
     */
    public void clear() {
        map.clear();
    }

    /**
     * Determines whether this Attributes contains the specified key
     * 
     * 
     * @param key
     *            The key to search for.
     * @return true if the key is found, false otherwise
     */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * Determines whether this Attributes contains the specified value
     * 
     * @param value
     *            The value to search for.
     * @return true if the value is found, false otherwise
     */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * Returns a set containing MapEntry's for each of the key/value pairs
     * contained in this Attributes.
     * 
     * @return a set of MapEntry's
     */
    public Set<Map.Entry<Object, Object>> entrySet() {
        return map.entrySet();
    }

    /**
     * Returns the value associated with the parameter key
     * 
     * @param key
     *            The key to search for.
     * @return Object associated with key, or null if key does not exist.
     */
    public Object get(Object key) {
        return map.get(key);
    }

    /**
     * Determines whether this Attributes contains any keys
     * 
     * @return true if one or more keys exist, false otherwise
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns a Set containing all the keys found in this Attributes.
     * 
     * @return a Set of all keys
     */
    public Set<Object> keySet() {
        return map.keySet();
    }

    /**
     * Store value in this Attributes and associate it with key.
     * 
     * @param key
     *            The key to associate with value.
     * @param value
     *            The value to store in this Attributes
     * @return The value being stored
     * 
     * @exception ClassCastException
     *                when key is not an Attributes.Name or value is not a
     *                String
     */
    @SuppressWarnings("cast") // Require cast to force ClassCastException
    public Object put(Object key, Object value) {
        return map.put((Name)key, (String)value);
    }

    /**
     * Store all the key.value pairs in the argument in this Attributes.
     * 
     * @param attrib
     *            the associations to store (must be of type Attributes).
     */
    public void putAll(Map<?, ?> attrib) {
        if (attrib == null || !(attrib instanceof Attributes)) {
            throw new ClassCastException();
        }
        this.map.putAll(attrib);
    }

    /**
     * Deletes the key/value pair with key key from this Attributes.
     * 
     * @param key
     *            The key to remove
     * @return the values associated with the removed key, null if not present.
     */
    public Object remove(Object key) {
        return map.remove(key);
    }

    /**
     * Returns the number of key.value pairs associated with this Attributes.
     * 
     * @return the size of this Attributes
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns a Collection of all the values present in this Attributes.
     * 
     * @return a Collection of all values present
     */
    public Collection<Object> values() {
        return map.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        Attributes clone;
        try {
            clone = (Attributes) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
        clone.map = (Map<Object, Object>) ((HashMap) map).clone();
        return clone;
    }

    /**
     * Returns the hashCode of this Attributes
     * 
     * @return the hashCode of this Object.
     */
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * Determines if this Attributes and the parameter Attributes are equal. Two
     * Attributes instances are equal if they contain the same keys and values.
     * 
     * @return true if the Attributes are equals, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Attributes) {
            return map.equals(((Attributes) obj).map);
        }
        return false;
    }

    /**
     * Returns the value associated with the parameter Attributes.Name key.
     * 
     * @param name
     *            The key to obtain the value for.
     * @return the String associated with name, or null if name is not a valid
     *         key
     */
    public String getValue(Attributes.Name name) {
        return (String) map.get(name);
    }

    /**
     * Returns the String associated with the parameter name.
     * 
     * @param name
     *            The key to obtain the value for.
     * @return the String associated with name, or null if name is not a valid
     *         key
     */
    public String getValue(String name) {
        return (String) map.get(new Attributes.Name(name));
    }

    /**
     * Stores value val against key name in this Attributes
     * 
     * @param name
     *            The key to store against.
     * @param val
     *            The value to store in this Attributes
     * @return the Value being stored
     */
    public String putValue(String name, String val) {
        return (String) map.put(new Attributes.Name(name), val);
    }
}
