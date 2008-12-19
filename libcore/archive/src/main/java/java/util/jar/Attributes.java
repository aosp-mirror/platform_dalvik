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
 * The {@code Attributes} class is used to store values for manifest entries.
 * Attribute keys are generally instances of {@code Attributes.Name}. Values
 * associated with attribute keys are of type {@code String}.
 * @since Android 1.0
 */
public class Attributes implements Cloneable, Map<Object, Object> {

    /**
     * The {@code Attributes} as name/value pairs. Maps the attribute names (as
     * {@link Attributes.Name}) of a JAR file manifest to arbitrary values. The
     * attribute names thus are obtained from the {@link Manifest} for
     * convenience.
     * 
     * @since Android 1.0
     */
    protected Map<Object, Object> map;

    /**
     * The name part of the name/value pairs constituting an attribute as
     * defined by the specification of the JAR manifest. May be composed of the
     * following ASCII signs as defined in the EBNF below:
     * 
     * <pre>
     * name       = alphanum *headerchar
     * headerchar = alphanum | - | _
     * alphanum   = {A-Z} | {a-z} | {0-9} 
     * </pre>
     * 
     * @since Android 1.0
     */
    public static class Name {
        private final String name;

        private int hashCode;

        /**
         * The class path (a main attribute).
         * 
         * @since Android 1.0
         */
        public static final Name CLASS_PATH = new Name("Class-Path"); //$NON-NLS-1$

        /**
         * The version of the manifest file (a main attribute).
         * 
         * @since Android 1.0
         */
        public static final Name MANIFEST_VERSION = new Name(
                "Manifest-Version"); //$NON-NLS-1$

        /**
         * The main class's name (for stand-alone applications).
         * 
         * @since Android 1.0
         */
        public static final Name MAIN_CLASS = new Name("Main-Class"); //$NON-NLS-1$

        /**
         * Defines the signature version of the JAR file.
         * 
         * @since Android 1.0
         */
        public static final Name SIGNATURE_VERSION = new Name(
                "Signature-Version"); //$NON-NLS-1$

        /**
         * The {@code Content-Type} manifest attribute.
         */
        public static final Name CONTENT_TYPE = new Name("Content-Type"); //$NON-NLS-1$

        /**
         * The {@code Sealed} manifest attribute which may have the value
         * {@code true} for sealed archives.
         * 
         * @since Android 1.0
         */
        public static final Name SEALED = new Name("Sealed"); //$NON-NLS-1$

        /**
         * The {@code Implementation-Title} attribute whose value is a string
         * that defines the title of the extension implementation.
         * 
         * @since Android 1.0
         */
        public static final Name IMPLEMENTATION_TITLE = new Name(
                "Implementation-Title"); //$NON-NLS-1$

        /**
         * The {@code Implementation-Version} attribute defining the version of
         * the extension implementation.
         * 
         * @since Android 1.0
         */
        public static final Name IMPLEMENTATION_VERSION = new Name(
                "Implementation-Version"); //$NON-NLS-1$

        /**
         * The {@code Implementation-Vendor} attribute defining the organization
         * that maintains the extension implementation.
         * 
         * @since Android 1.0
         */
        public static final Name IMPLEMENTATION_VENDOR = new Name(
                "Implementation-Vendor"); //$NON-NLS-1$

        /**
         * The {@code Specification-Title} attribute defining the title of the
         * extension specification.
         * 
         * @since Android 1.0
         */
        public static final Name SPECIFICATION_TITLE = new Name(
                "Specification-Title"); //$NON-NLS-1$

        /**
         * The {@code Specification-Version} attribute defining the version of
         * the extension specification.
         * 
         * @since Android 1.0
         */
        public static final Name SPECIFICATION_VERSION = new Name(
                "Specification-Version"); //$NON-NLS-1$

        /**
         * The {@code Specification-Vendor} attribute defining the organization
         * that maintains the extension specification.
         * 
         * @since Android 1.0
         */
        public static final Name SPECIFICATION_VENDOR = new Name(
                "Specification-Vendor"); //$NON-NLS-1$

        /**
         * The {@code Extension-List} attribute defining the extensions that are
         * needed by the applet.
         * 
         * @since Android 1.0
         */
        public static final Name EXTENSION_LIST = new Name("Extension-List"); //$NON-NLS-1$

        /**
         * The {@code Extension-Name} attribute which defines the unique name of
         * the extension.
         * 
         * @since Android 1.0
         */
        public static final Name EXTENSION_NAME = new Name("Extension-Name"); //$NON-NLS-1$

        /**
         * The {@code Extension-Installation} attribute.
         * 
         * @since Android 1.0
         */
        public static final Name EXTENSION_INSTALLATION = new Name(
                "Extension-Installation"); //$NON-NLS-1$

        /**
         * The {@code Implementation-Vendor-Id} attribute specifies the vendor
         * of an extension implementation if the applet requires an
         * implementation from a specific vendor.
         * 
         * @since Android 1.0
         */
        public static final Name IMPLEMENTATION_VENDOR_ID = new Name(
                "Implementation-Vendor-Id"); //$NON-NLS-1$

        /**
         * The {@code Implementation-URL} attribute specifying a URL that can be
         * used to obtain the most recent version of the extension if the
         * required version is not already installed.
         * 
         * @since Android 1.0
         */
        public static final Name IMPLEMENTATION_URL = new Name(
                "Implementation-URL"); //$NON-NLS-1$

        /**
         * A String which must satisfy the following EBNF grammar to specify an
         * additional attribute:
         * 
         * <pre>
         * name       = alphanum *headerchar
         * headerchar = alphanum | - | _
         * alphanum   = {A-Z} | {a-z} | {0-9}
         * </pre>
         * 
         * @param s
         *            The Attribute string.
         * @exception IllegalArgumentException
         *                if the string does not satisfy the EBNF grammar.
         * @since Android 1.0
         */
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

        /**
         * Returns this attribute name.
         * 
         * @return the attribute name.
         * @since Android 1.0
         */
        @Override
        public String toString() {
            return name;
        }

        /**
         * returns whether the argument provided is the same as the attribute
         * name.
         * 
         * @return if the attribute names correspond.
         * @param an
         *            An attribute name to be compared with this name.
         * @since Android 1.0
         */
        @Override
        public boolean equals(Object an) {
            if (an == null) {
                return false;
            }
            return an.getClass() == this.getClass()
                    && name.equalsIgnoreCase(((Name) an).name);
        }

        /**
         * Computes a hash code of the name.
         * 
         * @return the hash value computed from the name.
         * @since Android 1.0
         */
        @Override
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = Util.toASCIILowerCase("name").hashCode();
            }
            return hashCode;
        }
    }

    /**
     * Constructs an {@code Attributes} instance.
     * 
     * @since Android 1.0
     */
    public Attributes() {
        map = new HashMap<Object, Object>();
    }

    /**
     * Constructs an {@code Attributes} instance obtaining keys and values from
     * the parameter {@code attrib}.
     * 
     * @param attrib
     *            The attributes to obtain entries from.
     * @since Android 1.0
     */
    @SuppressWarnings("unchecked")
    public Attributes(Attributes attrib) {
        map = (Map<Object, Object>)((HashMap) attrib.map).clone();
    }

    /**
     * Constructs an {@code Attributes} instance with initial capacity of size
     * {@code size}.
     * 
     * @param size
     *            Initial size of this {@code Attributes} instance.
     * @since Android 1.0
     */
    public Attributes(int size) {
        map = new HashMap<Object, Object>(size);
    }

    /**
     * Removes all key/value pairs from this {@code Attributes}.
     * 
     * @since Android 1.0
     */
    public void clear() {
        map.clear();
    }

    /**
     * Determines whether this {@code Attributes} contains the specified key.
     * 
     * @param key
     *            The key to search for.
     * @return {@code true} if the key is found, {@code false} otherwise.
     * @since Android 1.0
     */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * Determines whether this {@code Attributes} contains the specified value.
     * 
     * @param value
     *            the value to search for.
     * @return {@code true} if the value is found, {@code false} otherwise.
     * @since Android 1.0
     */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * Returns a set containing map entries for each of the key/value pair
     * contained in this {@code Attributes}.
     * 
     * @return a set of Map.Entry's
     * @since Android 1.0
     */
    public Set<Map.Entry<Object, Object>> entrySet() {
        return map.entrySet();
    }

    /**
     * Returns the value associated with the parameter key.
     * 
     * @param key
     *            the key to search for.
     * @return Object associated with key, or {@code null} if key does not
     *         exist.
     * @since Android 1.0
     */
    public Object get(Object key) {
        return map.get(key);
    }

    /**
     * Determines whether this {@code Attributes} contains any keys.
     * 
     * @return {@code true} if one or more keys exist, {@code false} otherwise.
     * @since Android 1.0
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns a {@code Set} containing all the keys found in this {@code
     * Attributes}.
     * 
     * @return a {@code Set} of all keys.
     * @since Android 1.0
     */
    public Set<Object> keySet() {
        return map.keySet();
    }

    /**
     * Stores key/value pairs in this {@code Attributes}.
     * 
     * @param key
     *            the key to associate with value.
     * @param value
     *            the value to store in this {@code Attributes}.
     * @return the value being stored.
     * @exception ClassCastException
     *                when key is not an {@code Attributes.Name} or value is not
     *                a {@code String}.
     *@since Android 1.0
     */
    @SuppressWarnings("cast") // Require cast to force ClassCastException
    public Object put(Object key, Object value) {
        return map.put((Name)key, (String)value);
    }

    /**
     * Stores all the key/value pairs in the argument in this {@code Attributes}
     * .
     * 
     * @param attrib
     *            the associations to store (must be of type {@code Attributes}
     *            ).
     * @since Android 1.0
     */
    public void putAll(Map<?, ?> attrib) {
        if (attrib == null || !(attrib instanceof Attributes)) {
            throw new ClassCastException();
        }
        this.map.putAll(attrib);
    }

    /**
     * Deletes the key/value pair with key {@code key} from this {@code
     * Attributes}.
     * 
     * @param key
     *            the key to remove.
     * @return the values associated with the removed key, {@code null} if not
     *         present.
     * @since Android 1.0
     */
    public Object remove(Object key) {
        return map.remove(key);
    }

    /**
     * Returns the number of key/value pairs associated with this {@code
     * Attributes}.
     * 
     * @return the size of this {@code Attributes}.
     * @since Android 1.0
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns a collection of all the values present in this {@code Attributes}
     * .
     * 
     * @return a collection of all values present.
     * @since Android 1.0
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
     * Returns the hash code of this {@code Attributes}.
     * 
     * @return the hash code of this object.
     * @since Android 1.0
     */
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * Determines if this {@code Attributes} and the parameter {@code
     * Attributes} are equal. Two {@code Attributes} instances are equal if they
     * contain the same keys and values.
     * 
     * @param obj
     *            the object with which this {@code Attributes} is compared.
     * @return {@code true} if the {@code Attributes} are equal, {@code false}
     *         otherwise.
     * @since Android 1.0
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
     * Returns the value associated with the parameter {@code Attributes.Name}
     * key.
     * 
     * @param name
     *            the key to obtain the value for.
     * @return the {@code String} associated with name, or {@code null} if name
     *         is not a valid key.
     * @since Android 1.0
     */
    public String getValue(Attributes.Name name) {
        return (String) map.get(name);
    }

    /**
     * Returns the string associated with the parameter name.
     * 
     * @param name
     *            the key to obtain the value for.
     * @return the string associated with name, or {@code null} if name is not a
     *         valid key.
     * @since Android 1.0
     */
    public String getValue(String name) {
        return (String) map.get(new Attributes.Name(name));
    }

    /**
     * Stores the value {@code val} associated with the key {@code name} in this
     * {@code Attributes}.
     * 
     * @param name
     *            the key to store.
     * @param val
     *            the value to store in this {@code Attributes}.
     * @return the value being stored.
     * @since Android 1.0
     */
    public String putValue(String name, String val) {
        return (String) map.put(new Attributes.Name(name), val);
    }
}
