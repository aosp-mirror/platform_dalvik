/* Licensed to the Apache Software Foundation (ASF) under one or more
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
package java.util;

import org.apache.harmony.kernel.vm.LangAccess;

import java.io.Serializable;

public abstract class EnumSet<E extends Enum<E>> extends AbstractSet<E>
        implements Cloneable, Serializable {
    // BEGIN android-added
    /**
     * null-ok; package access to <code>java.lang</code>, set during
     * first need. This shouldn't be used directly. Instead, use {@link
     * SpecialAccess#LANG}, which is guaranteed to be initialized.
     */
    static /*package*/ LangAccess LANG_BOOTSTRAP = null;
    // END android-added

    private static final long serialVersionUID = 4782406773684236311L;

    final Class<E> elementClass;

    EnumSet(Class<E> cls) {
        elementClass = cls;
    }

    /**
     * Creates an empty enum set. The permitted elements are of type Class<E>.
     * 
     * @param elementType
     *            the class object for the elements contained
     * @return an empty enum set, with permitted elements of the specified
     *         elementType
     * @throws NullPointerException
     *             if the specified elementType is null
     */
    public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
        if (!elementType.isEnum()) {
            throw new ClassCastException();
        }
        // BEGIN android-changed
        E[] enums = SpecialAccess.LANG.getEnumValuesInOrder(elementType);
        if (enums.length <= 64) {
            return new MiniEnumSet<E>(elementType, enums);
        }
        return new HugeEnumSet<E>(elementType, enums);
        // END android-changed
    }

    /**
     * Creates an enum set. Element is contained in this enum set if and only if
     * it is a member of the specified element type.
     * 
     * @param elementType
     *            the class object for the elements contained
     * @return an enum set with elements solely from the specified element type
     * @throws NullPointerException
     *             if the specified elementType is null
     */
    public static <E extends Enum<E>> EnumSet<E> allOf(Class<E> elementType) {
        EnumSet<E> set = noneOf(elementType);
        set.complement();
        return set;
    }

    /**
     * Creates an enum set. All the contained elements are of type Class<E>,
     * and the contained elements are the same as those contained in s.
     * 
     * @param s
     *            the enum set from which to copy
     * @return an enum set with all the elements from the specified enum set
     * @throws NullPointerException
     *             if the specified enum set is null
     */
    public static <E extends Enum<E>> EnumSet<E> copyOf(EnumSet<E> s) {
        EnumSet<E> set = EnumSet.noneOf(s.elementClass);
        set.addAll(s);
        return set;
    }

    /**
     * Creates an enum set. The contained elements are the same as those
     * contained in collection c. If c is an enum set, invoking this method is
     * the same as invoking {@link #copyOf(EnumSet)}.
     * 
     * @param c
     *            the collection from which to copy
     * @return an enum set with all the elements from the specified collection
     * @throws IllegalArgumentException
     *             if c is not an enum set and contains no elements at all
     * @throws NullPointerException
     *             if the specified collection is null
     */
    public static <E extends Enum<E>> EnumSet<E> copyOf(Collection<E> c) {
        if (c instanceof EnumSet) {
            return copyOf((EnumSet<E>) c);
        }
        if (0 == c.size()) {
            throw new IllegalArgumentException();
        }
        Iterator<E> iterator = c.iterator();
        E element = iterator.next();
        EnumSet<E> set = EnumSet.noneOf(element.getDeclaringClass());
        set.add(element);
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }

    /**
     * Creates an enum set. All the contained elements complement those from the
     * specified enum set.
     * 
     * @param s
     *            the specified enum set
     * @return an enum set with all the elements complement those from the
     *         specified enum set
     * @throws NullPointerException
     *             if the specified enum set is null
     */
    public static <E extends Enum<E>> EnumSet<E> complementOf(EnumSet<E> s) {
        EnumSet<E> set = EnumSet.noneOf(s.elementClass);
        set.addAll(s);
        set.complement();
        return set;
    }

    abstract void complement();

    /**
     * Creates a new enum set, containing only the specified element. There are
     * six overloadings of the method. They accept from one to five elements
     * respectively. The sixth one receives arbitrary number of elements, and
     * runs slower than those only receive fixed number of elements.
     * 
     * @param e
     *            the initially contained element
     * @return an enum set containing the specified element
     * @throws NullPointerException
     *             if the specified element is null
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e) {
        EnumSet<E> set = EnumSet.noneOf(e.getDeclaringClass());
        set.add(e);
        return set;
    }

    /**
     * Creates a new enum set, containing only the specified elements. There are
     * six overloadings of the method. They accept from one to five elements
     * respectively. The sixth one receives arbitrary number of elements, and
     * runs slower than those only receive fixed number of elements.
     * 
     * @param e1
     *            the initially contained element
     * @param e2
     *            another initially contained element
     * @return an enum set containing the specified elements
     * @throws NullPointerException
     *             if any of the specified elements is null
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) {
        EnumSet<E> set = of(e1);
        set.add(e2);
        return set;
    }

    /**
     * Creates a new enum set, containing only the specified elements. There are
     * six overloadings of the method. They accept from one to five elements
     * respectively. The sixth one receives arbitrary number of elements, and
     * runs slower than those only receive fixed number of elements.
     * 
     * @param e1
     *            the initially contained element
     * @param e2
     *            another initially contained element
     * @param e3
     *            another initially contained element
     * @return an enum set containing the specified elements
     * @throws NullPointerException
     *             if any of the specified elements is null
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3) {
        EnumSet<E> set = of(e1, e2);
        set.add(e3);
        return set;
    }

    /**
     * Creates a new enum set, containing only the specified elements. There are
     * six overloadings of the method. They accept from one to five elements
     * respectively. The sixth one receives arbitrary number of elements, and
     * runs slower than those only receive fixed number of elements.
     * 
     * @param e1
     *            the initially contained element
     * @param e2
     *            another initially contained element
     * @param e3
     *            another initially contained element
     * @param e4
     *            another initially contained element
     * @return an enum set containing the specified elements
     * @throws NullPointerException
     *             if any of the specified elements is null
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4) {
        EnumSet<E> set = of(e1, e2, e3);
        set.add(e4);
        return set;
    }

    /**
     * Creates a new enum set, containing only the specified elements. There are
     * six overloadings of the method. They accept from one to five elements
     * respectively. The sixth one receives arbitrary number of elements, and
     * runs slower than those only receive fixed number of elements.
     * 
     * @param e1
     *            the initially contained element
     * @param e2
     *            another initially contained element
     * @param e3
     *            another initially contained element
     * @param e4
     *            another initially contained element
     * @param e5
     *            another initially contained element
     * @return an enum set containing the specified elements
     * @throws NullPointerException
     *             if any of the specified elements is null
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4, E e5) {
        EnumSet<E> set = of(e1, e2, e3, e4);
        set.add(e5);
        return set;
    }

    /**
     * Creates a new enum set, containing only the specified elements. It
     * receives arbitrary number of elements, and runs slower than those only
     * receive fixed number of elements.
     * 
     * @param start
     *            the first initially contained element
     * @param others
     *            the other initially contained elements
     * @return an enum set containing the specified elements
     * @throws NullPointerException
     *             if any of the specified elements is null
     */
    public static <E extends Enum<E>> EnumSet<E> of(E start, E... others) {
        EnumSet<E> set = of(start);
        for (E e : others) {
            set.add(e);
        }
        return set;
    }

    /**
     * Creates an enum set containing all the elements within the range defined
     * by start and end (inclusive). All the elements must be in order.
     * 
     * @param start
     *            the element used to define the beginning of the range
     * @param end
     *            the element used to define the end of the range
     * @return an enum set with elements in the range from start to end
     * @throws NullPointerException
     *             if any one of start or end is null
     * @throws IllegalArgumentException
     *             if start is behind end
     */
    public static <E extends Enum<E>> EnumSet<E> range(E start, E end) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException();
        }
        EnumSet<E> set = EnumSet.noneOf(start.getDeclaringClass());
        set.setRange(start, end);
        return set;
    }

    abstract void setRange(E start, E end);

    /**
     * Creates a new enum set with the same elements as those contained in this
     * enum set.
     * 
     * @return a new enum set with the same elements as those contained in this
     *         enum set
     */
    @SuppressWarnings("unchecked")
    @Override
    public EnumSet<E> clone() {
        try {
            Object set = super.clone();
            return (EnumSet<E>) set;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    boolean isValidType(Class cls) {
        return cls == elementClass || cls.getSuperclass() == elementClass;
    }

    private static class SerializationProxy<E extends Enum<E>> implements
            Serializable {

        private static final long serialVersionUID = 362491234563181265L;

        private Class<E> elementType;

        private E[] elements;

        private Object readResolve() {
            EnumSet<E> set = EnumSet.noneOf(elementType);
            for (E e : elements) {
                set.add(e);
            }
            return set;
        }
    }

    @SuppressWarnings("unchecked")
    Object writeReplace() {
        SerializationProxy proxy = new SerializationProxy();
        proxy.elements = toArray(new Enum[0]);
        proxy.elementType = elementClass;
        return proxy;
    }
}
