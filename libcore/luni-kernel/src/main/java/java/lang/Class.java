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

/*
 * Copyright (C) 2006-2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.lang;

// BEGIN android-added
import dalvik.system.VMStack;

import org.apache.harmony.kernel.vm.StringUtils;
import org.apache.harmony.luni.lang.reflect.GenericSignatureParser;
import org.apache.harmony.luni.lang.reflect.Types;
// END android-added

import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.ref.SoftReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.net.URL;
import java.security.ProtectionDomain;

import static java.lang.ClassCache.REFLECT;
import static java.lang.ClassCache.compareClassLists;
import static java.lang.ClassCache.getMatchingMethod;

/**
 * This class must be implemented by the VM vendor. The documented natives must
 * be implemented to support other provided class implementations in this
 * package. An instance of class Class is the in-image representation of a Java
 * class. There are three basic types of Classes
 * <dl>
 * <dt><em>Classes representing object types (classes or interfaces)</em>
 * </dt>
 * <dd>These are Classes which represent the class of a simple instance as
 * found in the class hierarchy. The name of one of these Classes is simply the
 * fully qualified class name of the class or interface that it represents. Its
 * <em>signature</em> is the letter "L", followed by its name, followed by a
 * semi-colon (";").</dd>
 * <dt><em>Classes representing base types</em></dt>
 * <dd>These Classes represent the standard Java base types. Although it is not
 * possible to create new instances of these Classes, they are still useful for
 * providing reflection information, and as the component type of array classes.
 * There is one of these Classes for each base type, and their signatures are:
 * <ul>
 * <li><code>B</code> representing the <code>byte</code> base type</li>
 * <li><code>S</code> representing the <code>short</code> base type</li>
 * <li><code>I</code> representing the <code>int</code> base type</li>
 * <li><code>J</code> representing the <code>long</code> base type</li>
 * <li><code>F</code> representing the <code>float</code> base type</li>
 * <li><code>D</code> representing the <code>double</code> base type</li>
 * <li><code>C</code> representing the <code>char</code> base type</li>
 * <li><code>Z</code> representing the <code>boolean</code> base type</li>
 * <li><code>V</code> representing void function return values</li>
 * </ul>
 * The name of a Class representing a base type is the keyword which is used to
 * represent the type in Java source code (i.e. "int" for the <code>int</code>
 * base type.</dd>
 * <dt><em>Classes representing array classes</em></dt>
 * <dd>These are Classes which represent the classes of Java arrays. There is
 * one such Class for all array instances of a given arity (number of
 * dimensions) and leaf component type. In this case, the name of the class is
 * one or more left square brackets (one per dimension in the array) followed by
 * the signature of the class representing the leaf component type, which can
 * be either an object type or a base type. The signature of a Class
 * representing an array type is the same as its name.</dd>
 * </dl>
 * 
 * @since 1.0
 */
public final class Class<T> implements Serializable, AnnotatedElement, GenericDeclaration, Type {

    private static final long serialVersionUID = 3206093459760846163L;

    // TODO How is this field being initialized? What's it being used for?
    private ProtectionDomain pd;

    /**
     * null-ok; cache of reflective information, wrapped in a soft
     * reference
     */
    private volatile SoftReference<ClassCache<T>> cacheRef;
    
    private Class() {
        // Prevent this class to be instantiated, instance
        // should be created by JVM only
    }

    /**
     * Get the Signature attribute for this class.  Returns null if not found.
     */
    private String getSignatureAttribute() {
        Object[] annotation = getSignatureAnnotation();

        if (annotation == null) {
            return null;
        }

        return StringUtils.combineStrings(annotation);
    }

    /**
     * Get the Signature annotation for this class.  Returns null if not found.
     */
    native private Object[] getSignatureAnnotation();

    /**
     * Returns a Class object which represents the class named by the argument.
     * The name should be the name of a class as described in the class
     * definition of {@link Class}, however Classes representing base types can
     * not be found using this method.
     * 
     * @param className The name of the non-base type class to find
     * @return the named Class
     * @throws ClassNotFoundException If the class could not be found
     * @see Class
     */
    public static Class<?> forName(String className) throws ClassNotFoundException {
        return forName(className, true, VMStack.getCallingClassLoader());
    }

    /**
     * Returns a Class object which represents the class named by the argument.
     * The name should be the name of a class as described in the class
     * definition of {@link Class}, however Classes representing base types can
     * not be found using this method. Security rules will be obeyed.
     * 
     * @param className The name of the non-base type class to find
     * @param initializeBoolean A boolean indicating whether the class should be
     *        initialized
     * @param classLoader The class loader to use to load the class
     * @return the named class.
     * @throws ClassNotFoundException If the class could not be found
     * @see Class
     */
    public static Class<?> forName(String className, boolean initializeBoolean,
            ClassLoader classLoader) throws ClassNotFoundException {
        
        if (classLoader == null) {
            SecurityManager smgr = System.getSecurityManager();
            if (smgr != null) {
                ClassLoader calling = VMStack.getCallingClassLoader();
                if (calling != null) {
                    smgr.checkPermission(new RuntimePermission("getClassLoader"));
                }
            }
            
            classLoader = ClassLoader.getSystemClassLoader();
        }
        
        return classForName(className, initializeBoolean, classLoader);
    }

    /*
     * Returns a class by name without any security checks.
     *
     * @param className The name of the non-base type class to find
     * @param initializeBoolean A boolean indicating whether the class should be
     *        initialized
     * @param classLoader The class loader to use to load the class
     * @return the named class.
     * @throws ClassNotFoundException If the class could not be found
     */
    static native Class<?> classForName(String className, boolean initializeBoolean,
            ClassLoader classLoader) throws ClassNotFoundException;
    
    /**
     * Returns an array containing all public class members of the class which
     * the receiver represents and its super classes and interfaces
     * 
     * @return the class' public class members
     * @throws SecurityException If member access is not allowed
     * @see Class
     */
    public Class<?>[] getClasses() {
        checkPublicMemberAccess();
        return getFullListOfClasses(true);
    }

    /**
     * Returns the annotation of the given type. If there is no annotation the
     * method returns <code>null</code>.
     * 
     * @param annotationClass the annotation type.
     * @return the annotation of the given type, or <code>null</code> if none.
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        Annotation[] list = getAnnotations();
        for (int i = 0; i < list.length; i++) {
            if (annotationClass.isInstance(list[i])) {
                return (A)list[i];
            }
        }
        
        return null;
    }

    /**
     * Returns all the annotations of the receiver. If there are no annotations
     * then returns an empty array.
     * 
     * @return a copy of the array containing the receiver's annotations.
     */
    public Annotation[] getAnnotations() {
        /*
         * We need to get the annotations declared on this class, plus the
         * annotations from superclasses that have the "@Inherited" annotation
         * set.  We create a temporary map to use while we accumulate the
         * annotations and convert it to an array at the end.
         *
         * It's possible to have duplicates when annotations are inherited.
         * We use a Map to filter those out.
         *
         * HashMap might be overkill here.
         */
        HashMap<Class, Annotation> map = new HashMap<Class, Annotation>();
        Annotation[] annos = getDeclaredAnnotations();

        for (int i = annos.length-1; i >= 0; --i)
            map.put(annos[i].annotationType(), annos[i]);

        for (Class sup = getSuperclass(); sup != null;
                sup = sup.getSuperclass()) {
            annos = sup.getDeclaredAnnotations();
            for (int i = annos.length-1; i >= 0; --i) {
                Class clazz = annos[i].annotationType();
                if (!map.containsKey(clazz) &&
                        clazz.isAnnotationPresent(Inherited.class)) {
                    map.put(clazz, annos[i]);
                }
            }
        }

        /* convert annotation values from HashMap to array */
        Collection<Annotation> coll = map.values();
        return coll.toArray(new Annotation[coll.size()]);
    }

    /**
     * Returns the canonical name of the receiver. If the receiver does not have
     * a canonical name, as defined in the Java Language Specification, then the
     * method returns <code>null</code>.
     * 
     * @return the receiver canonical name, or <code>null</code>.
     */
    public String getCanonicalName() {
        if (isLocalClass() || isAnonymousClass())
            return null;

        if (isArray()) {
            /*
             * The canonical name of an array type depends on the (existence of)
             * the component type's canonical name. 
             */
            String name = getComponentType().getCanonicalName();
            if (name != null) {
                return name + "[]";
            } 
        } else if (isMemberClass()) {
            /*
             * The canonical name of an inner class depends on the (existence
             * of) the declaring class' canonical name. 
             */
            String name = getDeclaringClass().getCanonicalName();
            if (name != null) {
                return name + "." + getSimpleName();
            }
        } else {
            /*
             * The canonical name of a top-level class or primitive type is
             * equal to the fully qualified name. 
             */
            
            // TODO Check if this works for the primitive types.
            return getName();
        }
        
        /*
         * Other classes don't have a canonical name.
         */
        return null;
    }

    /**
     * Returns the class loader which was used to load the class represented by
     * the receiver. Returns null if the class was loaded by the bootstrap class
     * loader
     * 
     * @return the receiver's class loader or nil
     * @see ClassLoader
     */
    public ClassLoader getClassLoader() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            ClassLoader calling = VMStack.getCallingClassLoader();
            ClassLoader current = getClassLoaderImpl();

            if (calling != null && !calling.getClass().isInstance(current)) {
                smgr.checkPermission(new RuntimePermission("getClassLoader"));
            }
        }
        
        ClassLoader loader = getClassLoaderImpl();
        if (loader == null) {
            loader = BootClassLoader.getInstance();
        }
        
        return loader;
    }

    /**
     * This must be provided by the VM vendor, as it is used by other provided
     * class implementations in this package. Outside of this class, it is used
     * by SecurityManager.checkMemberAccess(), classLoaderDepth(),
     * currentClassLoader() and currentLoadedClass(). Return the ClassLoader for
     * this Class without doing any security checks. The bootstrap ClassLoader
     * is returned, unlike getClassLoader() which returns null in place of the
     * bootstrap ClassLoader.
     * 
     * @return the ClassLoader
     * @see ClassLoader#isSystemClassLoader()
     */
    ClassLoader getClassLoaderImpl() {
        return getClassLoader(this);
    }

    /*
     * Returns the defining class loader for the given class.
     * 
     * @param clazz the class the class loader of which we want
     * @return the class loader
     */
    private static native ClassLoader getClassLoader(Class<?> clazz);
    
    /**
     * Returns a Class object which represents the receiver's component type if
     * the receiver represents an array type. Otherwise returns nil. The
     * component type of an array type is the type of the elements of the array.
     * 
     * @return the component type of the receiver.
     * @see Class
     */
    public native Class<?> getComponentType();

    /**
     * Returns a public Constructor object which represents the constructor
     * described by the arguments.
     * 
     * @param parameterTypes the types of the arguments.
     * @return the constructor described by the arguments.
     * @throws NoSuchMethodException if the constructor could not be found.
     * @throws SecurityException if member access is not allowed
     * @see #getConstructors
     */
    @SuppressWarnings("unchecked")
    public Constructor<T> getConstructor(Class... parameterTypes) throws NoSuchMethodException,
            SecurityException {
        checkPublicMemberAccess();
        return getMatchingConstructor(getDeclaredConstructors(this, true), parameterTypes);
    }

    /**
     * Returns an array containing Constructor objects describing all
     * constructors which are visible from the current execution context.
     * 
     * @return all visible constructors starting from the receiver.
     * @throws SecurityException if member access is not allowed
     * @see #getMethods
     */
    public Constructor<T>[] getConstructors() throws SecurityException {
        checkPublicMemberAccess();
        return getDeclaredConstructors(this, true);
    }

    /**
     * Returns the annotations that are directly defined on this type.
     * Annotations that are inherited are not included in the result. If there
     * are no annotations, returns an empty array.
     * 
     * @return a copy of the array containing the receiver's defined
     *         annotations.
     * @since 1.5
     */
    native public Annotation[] getDeclaredAnnotations();

    /**
     * Returns an array containing all class members of the class which the
     * receiver represents. Note that some of the fields which are returned may
     * not be visible in the current execution context.
     * 
     * @return the class' class members
     * @throws SecurityException if member access is not allowed
     * @see Class
     */
    public Class<?>[] getDeclaredClasses() throws SecurityException {
        checkDeclaredMemberAccess();
        return getDeclaredClasses(this, false);
    }

    /*
     * Returns the list of member classes without performing any security checks
     * first. This includes the member classes inherited from superclasses. If no
     * member classes exist at all, an empty array is returned.
     * 
     * @param publicOnly reflects whether we want only public members or all of them
     * @return the list of classes
     */
    private Class<?>[] getFullListOfClasses(boolean publicOnly) {
        Class<?>[] result = getDeclaredClasses(this, publicOnly);
        
        // Traverse all superclasses
        Class<?> clazz = this.getSuperclass();
        while (clazz != null) {
            Class<?>[] temp = getDeclaredClasses(clazz, publicOnly);
            if (temp.length != 0) {
                result = arraycopy(new Class[result.length + temp.length], result, temp);
            }
            
            clazz = clazz.getSuperclass();
        }
        
        return result;
    }

    /*
     * Returns the list of member classes of the given class. No security checks
     * are performed. If no members exist, an empty array is returned.
     * 
     * @param clazz the class the members of which we want
     * @param publicOnly reflects whether we want only public member or all of them
     * @return the class' class members
     */
    native private static Class<?>[] getDeclaredClasses(Class<?> clazz,
        boolean publicOnly);
    
    /**
     * Returns a Constructor object which represents the constructor described
     * by the arguments.
     * 
     * @param parameterTypes the types of the arguments.
     * @return the constructor described by the arguments.
     * @throws NoSuchMethodException if the constructor could not be found.
     * @throws SecurityException if member access is not allowed
     * @see #getConstructors
     */
    @SuppressWarnings("unchecked")
    public Constructor<T> getDeclaredConstructor(Class... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        checkDeclaredMemberAccess();
        return getMatchingConstructor(getDeclaredConstructors(this, false), parameterTypes);
    }

    /**
     * Returns an array containing Constructor objects describing all
     * constructor which are defined by the receiver. Note that some of the
     * fields which are returned may not be visible in the current execution
     * context.
     * 
     * @return the receiver's constructors.
     * @throws SecurityException if member access is not allowed
     * @see #getMethods
     */
    public Constructor<T>[] getDeclaredConstructors() throws SecurityException {
        checkDeclaredMemberAccess();
        return getDeclaredConstructors(this, false);
    }

    /*
     * Returns the list of constructors without performing any security checks
     * first. If no constructors exist, an empty array is returned.
     *
     * @param clazz the class of interest
     * @param publicOnly reflects whether we want only public constructors or all of them
     * @return the list of constructors
     */
    private static native <T> Constructor<T>[] getDeclaredConstructors(Class<T> clazz, boolean publicOnly);

    /*
     * Finds a constructor with a given signature.
     * 
     * @param list the list of constructors to search through
     * @param parameterTypes the formal parameter list
     * @return the matching constructor
     * @throws NoSuchMethodException if the constructor does not exist.
     */
    private Constructor<T> getMatchingConstructor(
            Constructor<T>[] list, Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        for (int i = 0; i < list.length; i++) {
            if (compareClassLists(list[i].getParameterTypes(), parameterTypes)) {
                return list[i];
            }
        }
        
        throw new NoSuchMethodException(getSimpleName());
    }
    
    /**
     * Returns a Field object describing the field in the receiver named by the
     * argument. Note that the Constructor may not be visible from the current
     * execution context.
     * 
     * @param name The name of the field to look for.
     * @return the field in the receiver named by the argument.
     * @throws NoSuchFieldException if the requested field could not be found
     * @throws SecurityException if member access is not allowed
     * @see #getDeclaredFields()
     */
    public Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException {
        checkDeclaredMemberAccess();
        return getMatchingField(getDeclaredFields(this, false), name);
    }

    /**
     * Returns an array containing Field objects describing all fields which are
     * defined by the receiver. Note that some of the fields which are returned
     * may not be visible in the current execution context.
     * 
     * @return the receiver's fields.
     * @throws SecurityException If member access is not allowed
     * @see #getFields
     */
    public Field[] getDeclaredFields() throws SecurityException {
        checkDeclaredMemberAccess();
        return getDeclaredFields(this, false);
    }

    /*
     * Returns the list of fields without performing any security checks
     * first. This includes the fields inherited from superclasses. If no
     * fields exist at all, an empty array is returned.
     * 
     * @param publicOnly reflects whether we want only public fields or all of them
     * @return the list of fields 
     */
    private Field[] getFullListOfFields(boolean publicOnly) {
        Field[] result = getDeclaredFields(this, publicOnly);
        
        // Traverse all superclasses
        Class<?> clazz = this.getSuperclass();
        while (clazz != null) {
            Field[] temp = getDeclaredFields(clazz, publicOnly);
            if (temp.length != 0) {
                result = arraycopy(new Field[result.length + temp.length], result, temp);
            }
            
            clazz = clazz.getSuperclass();
        }
        
        return result;
    }
    
    /*
     * Returns the list of fields without performing any security checks
     * first. If no fields exist at all, an empty array is returned.
     * 
     * @param clazz the class of interest
     * @param publicOnly reflects whether we want only public fields or all of them
     * @return the list of fields 
     */
    static native Field[] getDeclaredFields(Class<?> clazz, boolean publicOnly);
    
    /*
     * Finds a field with a given name in a list of fields.
     * 
     * @param fields the list of fields to search through
     * @name the name of the field
     * @return the field
     * @throws NoSuchFieldException if the field does not exist.
     */
    private static Field getMatchingField(Field[] fields, String name)
            throws NoSuchFieldException {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(name))
                return fields[i];
        }

        throw new NoSuchFieldException(name);
    }

    // BEGIN android-changed
    // Changed to raw type to be closer to the RI
    /**
     * Returns a Method object which represents the method described by the
     * arguments. Note that the associated method may not be visible from the
     * current execution context.
     * 
     * @param name the name of the method
     * @param parameterTypes the types of the arguments.
     * @return the method described by the arguments.
     * @throws NoSuchMethodException if the method could not be found.
     * @throws SecurityException If member access is not allowed
     * @throws NullPointerException if the name parameter is <code>null</code>.
     * @see #getMethods
     */
    public Method getDeclaredMethod(String name, Class... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        checkDeclaredMemberAccess();

        Method[] methods = getClassCache().getDeclaredMethods();
        Method method = getMatchingMethod(methods, name, parameterTypes);

        /*
         * Make a copy of the private (to the package) object, so that
         * setAccessible() won't alter the private instance.
         */
        return REFLECT.clone(method);
    }
    // END android-changed
    
    /**
     * Returns an array containing Method objects describing all methods which
     * are defined by the receiver. Note that some of the methods which are
     * returned may not be visible in the current execution context.
     * 
     * @return the receiver's methods.
     * @throws SecurityException if member access is not allowed
     * @see #getMethods
     */
    public Method[] getDeclaredMethods() throws SecurityException {
        checkDeclaredMemberAccess();

        // Return a copy of the private (to the package) array.
        Method[] methods = getClassCache().getDeclaredMethods();
        return ClassCache.deepCopy(methods);
    }
    
    /**
     * Returns the list of methods without performing any security checks
     * first. If no methods exist, an empty array is returned.
     */
    static native Method[] getDeclaredMethods(Class<?> clazz, boolean publicOnly);

    /**
     * Gets the {@link ClassCache} for this instance.
     *
     * @return non-null; the cache object
     */
    /*package*/ ClassCache<T> getClassCache() {
        /*
         * Note: It is innocuous if two threads try to simultaneously
         * create the cache, so we don't bother protecting against that.
         */
        ClassCache<T> cache = null;

        if (cacheRef != null) {
            cache = cacheRef.get();
        }

        if (cache == null) {
            cache = new ClassCache<T>(this);
            cacheRef = new SoftReference<ClassCache<T>>(cache);
        }

        return cache;
    }

    /**
     * Returns the class which declared the class represented by the receiver.
     * This will return null if the receiver is a member of another class.
     * 
     * @return the declaring class of the receiver.
     */
    native public Class<?> getDeclaringClass();

    /**
     * Returns the class that directly encloses the receiver. If there is no
     * enclosing class the method returns <code>null</code>.
     * 
     * @return the enclosing class or <code>null</code>.
     */
    native public Class<?> getEnclosingClass();

    /**
     * Gets the {@link Constructor}, which encloses the declaration of this
     * class, if it is an anonymous or local/automatic class, otherwise
     * <code>null</code>.
     * 
     * @return A {@link Constructor} instance or <code>null</code>.
     * @since 1.5
     */
    native public Constructor<?> getEnclosingConstructor();

    /**
     * Gets the {@link Method}, which encloses the declaration of this class,
     * if it is an anonymous or local/automatic class, otherwise
     * <code>null</code>.
     * 
     * @return A {@link Method} instance or <code>null</code>.
     * @since 1.5
     */
    native public Method getEnclosingMethod();

    /**
     * Gets the <code>enum</code> constants/fields associated with this class
     * if it is an {@linkplain #isEnum() enum}, otherwise <code>null</code>.
     * 
     * @return An array of the <code>enum</code> constants for this class or
     *         <code>null</code>.
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public T[] getEnumConstants() {
        if (isEnum()) {
            checkPublicMemberAccess();
            T[] values = getClassCache().getEnumValuesInOrder();

            // Copy the private (to the package) array.
            return (T[]) values.clone();
        }
        
        return null;
    }
        
    /**
     * Returns a Field object describing the field in the receiver named by the
     * argument which must be visible from the current execution context.
     * 
     * @param name The name of the field to look for.
     * @return the field in the receiver named by the argument.
     * @throws NoSuchFieldException If the given field does not exist
     * @throws SecurityException If access is denied
     * @see #getDeclaredFields()
     */
    public Field getField(String name) throws NoSuchFieldException, SecurityException {
        checkPublicMemberAccess();
        return getMatchingField(getFullListOfFields(true), name);
    }

    /**
     * Returns an array containing Field objects describing all fields which are
     * visible from the current execution context.
     * 
     * @return all visible fields starting from the receiver.
     * @throws SecurityException If member access is not allowed
     * @see #getDeclaredFields()
     */
    public Field[] getFields() throws SecurityException {
        checkPublicMemberAccess();
        return getFullListOfFields(true);
    }

    /**
     * Gets the {@link Type types} of the interface that this class directly
     * implements.
     * 
     * @return An array of {@link Type} instances.
     * @since 1.5
     */
    public Type[] getGenericInterfaces() {
        // BEGIN android-changed
        GenericSignatureParser parser = new GenericSignatureParser();
        parser.parseForClass(this, getSignatureAttribute());
        return Types.getClonedTypeArray(parser.interfaceTypes);
        // END android-changed
    }
    
    /**
     * Gets the {@link Type} that represents the super class of this class.
     * 
     * @return An instance of {@link Type}
     * @since 1.5
     */
    public Type getGenericSuperclass() {
        // BEGIN android-changed
        GenericSignatureParser parser = new GenericSignatureParser();
        parser.parseForClass(this, getSignatureAttribute());
        return Types.getType(parser.superclassType);
        // END android-changed
    }

    /**
     * Returns an array of Class objects which match the interfaces specified in
     * the receiver classes <code>implements</code> declaration. The order of
     * entries equals the order in the original class declaration. If the
     * class doesn't implement any interfaces, an empty array is returned.
     * 
     * @return Class[] the interfaces the receiver claims to implement.
     */
    public native Class<?>[] getInterfaces();

    // BEGIN android-changed
    // Changed to raw type to be closer to the RI
    /**
     * Returns a Method object which represents the method described by the
     * arguments.
     * 
     * @param name String the name of the method
     * @param parameterTypes Class[] the types of the arguments.
     * @return Method the method described by the arguments.
     * @throws NoSuchMethodException if the method could not be found.
     * @throws SecurityException if member access is not allowed
     * @see #getMethods
     */
    public Method getMethod(String name, Class... parameterTypes) throws NoSuchMethodException, 
            SecurityException {
        checkPublicMemberAccess();

        Method[] methods = getClassCache().getAllPublicMethods();
        Method method = getMatchingMethod(methods, name, parameterTypes);

        /*
         * Make a copy of the private (to the package) object, so that
         * setAccessible() won't alter the private instance.
         */
        return REFLECT.clone(method);
    }
    // END android-changed

    /**
     * Returns an array containing Method objects describing all methods which
     * are visible from the current execution context.
     * 
     * @return Method[] all visible methods starting from the receiver.
     * @throws SecurityException if member access is not allowed
     * @see #getDeclaredMethods()
     */
    public Method[] getMethods() throws SecurityException {
        checkPublicMemberAccess();

        // Return a copy of the private (to the package) array.
        Method[] methods = getClassCache().getAllPublicMethods();
        return ClassCache.deepCopy(methods);
    }
    
    /**
     * Performs the security checks regarding the access of a public
     * member of this class.
     * 
     * <p><b>Note:</b> Because of the <code>getCallingClassLoader2()</code>
     * check, this method must be called exactly one level deep into a
     * public method on this instance.</p>
     */
    /*package*/ void checkPublicMemberAccess() {
        SecurityManager smgr = System.getSecurityManager();

        if (smgr != null) {
            smgr.checkMemberAccess(this, Member.PUBLIC);

            ClassLoader calling = VMStack.getCallingClassLoader2();
            ClassLoader current = getClassLoader();
            
            if (calling != null && !calling.getClass().isInstance(current)) {
                smgr.checkPackageAccess(this.getPackage().getName());
            }
        }
    }

    /**
     * Performs the security checks regarding the access of a declared
     * member of this class.
     * 
     * <p><b>Note:</b> Because of the <code>getCallingClassLoader2()</code>
     * check, this method must be called exactly one level deep into a
     * public method on this instance.</p>
     */
    private void checkDeclaredMemberAccess() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkMemberAccess(this, Member.DECLARED);
            
            ClassLoader calling = VMStack.getCallingClassLoader2();
            ClassLoader current = getClassLoader();

            if (calling != null && !calling.getClass().isInstance(current)) {
                smgr.checkPackageAccess(this.getPackage().getName());
            }
        }
    }
    
    /**
     * Returns an integer which which is the receiver's modifiers. Note that the
     * constants which describe the bits which are returned are implemented in
     * class {@link Modifier} which may not be available on the target.
     * 
     * @return the receiver's modifiers
     */
    public int getModifiers() {
        return getModifiers(this, false);
    }

    /*
     * Return the modifiers for the given class.
     * 
     * @param clazz the class of interest
     * @ignoreInnerClassesAttrib determines whether we look for and use the
     *     flags from an "inner class" attribute
     */
    private static native int getModifiers(Class<?> clazz, boolean ignoreInnerClassesAttrib);
    
    /**
     * Returns the name of the class which the receiver represents. For a
     * description of the format which is used, see the class definition of
     * {@link Class}.
     * 
     * @return the receiver's name.
     * @see Class
     */
    public native String getName();

    /**
     * Returns the simple name of the receiver as defined in the source code. If
     * there is no name (the class is anonymous) returns an empty string, and if
     * the receiver is an array returns the name of the underlying type with
     * square braces appended (e.g. <code>&quot;Integer[]&quot;</code>).
     * 
     * @return the simple name of the receiver.
     */
    public String getSimpleName() {
        if (isArray()) {
            return getComponentType().getSimpleName() + "[]";
        }
        
        String name = getName();
        
        if (isAnonymousClass()) {
            return "";
        }

        if (isMemberClass() || isLocalClass()) {
            return getInnerClassName();
        }
        
        int dot = name.lastIndexOf('.');
        if (dot != -1) {
            return name.substring(dot + 1);
        }
        
        return name;
    }

    /*
     * Returns the simple name of a member or local class, or null otherwise. 
     * 
     * @return The name.
     */
    private native String getInnerClassName();
    
    /**
     * Returns the ProtectionDomain of the receiver.
     * <p>
     * Note: In order to conserve space in embedded targets, we allow this
     * method to answer null for classes in the system protection domain (i.e.
     * for system classes). System classes are always given full permissions
     * (i.e. AllPermission). This is not changeable via the
     * java.security.Policy.
     * 
     * @return ProtectionDomain the receiver's ProtectionDomain.
     * @see Class
     */
    public ProtectionDomain getProtectionDomain() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            ClassLoader calling = VMStack.getCallingClassLoader();
            ClassLoader current = getClassLoaderImpl();
            
            if (calling != null && !calling.getClass().isInstance(current)) {
                smgr.checkPermission(new RuntimePermission("getProtectionDomain"));
            }
        }
        
        return pd;
    }

    /**
     * Returns a read-only stream on the contents of the resource specified by
     * resName. The mapping between the resource name and the stream is managed
     * by the class' class loader.
     * 
     * @param resName the name of the resource.
     * @return a stream on the resource.
     * @see ClassLoader
     */
    public URL getResource(String resName) {
        // Get absolute resource name, but without the leading slash
        if (resName.startsWith("/")) {
            resName = resName.substring(1);
        } else {
            String pkg = getName();
            int dot = pkg.lastIndexOf('.');
            if (dot != -1) {
                pkg = pkg.substring(0, dot).replace('.', '/');
            } else {
                pkg = "";
            }
            
            resName = pkg + "/" + resName;
        }
        
        // Delegate to proper class loader
        ClassLoader loader = getClassLoader();
        if (loader != null) {
            return loader.getResource(resName);
        } else {
            return ClassLoader.getSystemResource(resName);
        }
    }

    /**
     * Returns a read-only stream on the contents of the resource specified by
     * resName. The mapping between the resource name and the stream is managed
     * by the class' class loader.
     * 
     * @param resName the name of the resource.
     * @return a stream on the resource.
     * @see ClassLoader
     */
    public InputStream getResourceAsStream(String resName) {
        // Get absolute resource name, but without the leading slash
        if (resName.startsWith("/")) {
            resName = resName.substring(1);
        } else {
            String pkg = getName();
            int dot = pkg.lastIndexOf('.');
            if (dot != -1) {
                pkg = pkg.substring(0, dot).replace('.', '/');
            } else {
                pkg = "";
            }
            
            resName = pkg + "/" + resName;
        }
        
        // Delegate to proper class loader
        ClassLoader loader = getClassLoader();
        if (loader != null) {
            return loader.getResourceAsStream(resName);
        } else {
            return ClassLoader.getSystemResourceAsStream(resName);
        }
    }

    /**
     * Returns the signers for the class represented by the receiver, or null if
     * there are no signers.
     * 
     * @return the signers of the receiver.
     * @see #getMethods
     */
    public Object[] getSigners() {
        // TODO Delegate this to class loader somehow? What are these signers?
        return null;
    }

    /**
     * Returns the Class which represents the receiver's superclass. For Classes
     * which represent base types, interfaces, and for {@link Object} the method
     * returns null.
     * 
     * @return the receiver's superclass.
     */
    public native Class<? super T> getSuperclass();

    /**
     * Gets the type variables associated with this class. Returns an empty
     * array if the class is not generic or does not make use of type
     * variables otherwise.
     * 
     * @return An array of {@link TypeVariable} instances.
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public synchronized TypeVariable<Class<T>>[] getTypeParameters() {
        // BEGIN android-changed
        GenericSignatureParser parser = new GenericSignatureParser();
        parser.parseForClass(this, getSignatureAttribute());
        return parser.formalTypeParameters.clone();
        // END android-changed
    }

    /**
     * Indicates whether or not this class is an annotation.
     * 
     * @return A value of <code>true</code> if this class is an annotation,
     *         otherwise <code>false</code>.
     * @since 1.5
     */
    public boolean isAnnotation() {
        final int ACC_ANNOTATION = 0x2000;  // not public in reflect.Modifiers
        int mod = getModifiers(this, true);
        return (mod & ACC_ANNOTATION) != 0;
    }

    /**
     * Indicates whether or not the given annotation is present for this class.
     * 
     * @param annotationClass The annotation to look for in this class.
     * @return A value of <code>true</code> if the annotation is present,
     *         otherwise <code>false</code>.
     * @since 1.5
     */
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    /**
     * Indicates whether or not this class was anonymously declared.
     * 
     * @return A value of <code>true</code> if this class is anonymous,
     *         otherwise <code>false</code>.
     * @since 1.5
     */
    native public boolean isAnonymousClass();

    /**
     * Returns true if the receiver represents an array class.
     * 
     * @return <code>true</code> if the receiver represents an array class
     *         <code>false</code> if it does not represent an array class
     */
    public boolean isArray() {
        return getComponentType() != null;
    }

    /**
     * Returns true if the type represented by the argument can be converted via
     * an identity conversion or a widening reference conversion (i.e. if either
     * the receiver or the argument represent primitive types, only the identity
     * conversion applies).
     * 
     * @return <code>true</code> the argument can be assigned into the
     *         receiver <code>false</code> the argument cannot be assigned
     *         into the receiver
     * @param cls Class the class to test
     * @throws NullPointerException if the parameter is null
     */
    public native boolean isAssignableFrom(Class<?> cls);

    /**
     * Indicates whether or not this class is an <code>enum</code>.
     * 
     * @return A value of <code>true</code> if this class is an {@link Enum},
     *         otherwise <code>false</code>.
     * @since 1.5
     */
    public boolean isEnum() {
        return ((getModifiers() & 0x4000) != 0) && (getSuperclass() == Enum.class);
    }

    /**
     * Returns true if the argument is non-null and can be cast to the type of
     * the receiver. This is the runtime version of the <code>instanceof</code>
     * operator.
     * 
     * @return <code>true</code> the argument can be cast to the type of the
     *         receiver <code>false</code> the argument is null or cannot be
     *         cast to the type of the receiver
     * @param object Object the object to test
     */
    public native boolean isInstance(Object object);

    /**
     * Returns true if the receiver represents an interface.
     * 
     * @return <code>true</code> if the receiver represents an interface
     *         <code>false</code> if it does not represent an interface
     */
    public native boolean isInterface();

    /**
     * Returns whether the receiver is defined locally.
     * 
     * @return <code>true</code> if the class is local, otherwise
     *         <code>false</code>.
     */
    public boolean isLocalClass() {
        boolean enclosed = (getEnclosingMethod() != null ||
                         getEnclosingConstructor() != null);
        return enclosed && !isAnonymousClass();
    }

    /**
     * Returns whether the receiver is a member class.
     * 
     * @return <code>true</code> if the class is a member class, otherwise
     *         <code>false</code>.
     */
    public boolean isMemberClass() {
        return getDeclaringClass() != null;
    }

    /**
     * Returns true if the receiver represents a base type.
     * 
     * @return <code>true</code> if the receiver represents a base type
     *         <code>false</code> if it does not represent a base type
     */
    public native boolean isPrimitive();

    /**
     * Returns whether the receiver is a synthetic type.
     * 
     * @return <code>true</code> if the receiver is a synthetic type and
     *         <code>false</code> otherwise.
     */
    public boolean isSynthetic() {
        final int ACC_SYNTHETIC = 0x1000;   // not public in reflect.Modifiers
        int mod = getModifiers(this, true);
        return (mod & ACC_SYNTHETIC) != 0;
    }

    /**
     * Returns a new instance of the class represented by the receiver, created
     * by invoking the default (i.e. zero-argument) constructor. If there is no
     * such constructor, or if the creation fails (either because of a lack of
     * available memory or because an exception is thrown by the constructor),
     * an InstantiationException is thrown. If the default constructor exists,
     * but is not accessible from the context where this message is sent, an
     * IllegalAccessException is thrown.
     * 
     * @return a new instance of the class represented by the receiver.
     * @throws IllegalAccessException if the constructor is not visible to the
     *         sender.
     * @throws InstantiationException if the instance could not be created.
     */
    public native T newInstance() throws IllegalAccessException, InstantiationException;

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a printable representation for the receiver.
     */
    @Override
    public String toString() {
        if (isPrimitive()) {
            return getSimpleName().toLowerCase();
        } else {
            return (isInterface() ? "interface " : "class ") + getName();
        }
    }

    /**
     * Returns the Package of which this class is a member. A class has a
     * Package if it was loaded from a SecureClassLoader
     * 
     * @return Package the Package of which this class is a member or null
     */
    public Package getPackage() {
        // TODO This might be a hack, but the VM doesn't have the necessary info.
        ClassLoader loader = getClassLoader();
        if (loader != null) {
            String name = getName();
            int dot = name.lastIndexOf('.');
            return (dot != -1 ? ClassLoader.getPackage(loader, name.substring(0, dot)) : null);
        }
        
        return null;
    }

    /**
     * Returns the assertion status for this class. Assertion is
     * enabled/disabled based on class loader default, package or class default
     * at runtime
     * 
     * @return the assertion status for this class
     */
    public native boolean desiredAssertionStatus();

    /**
     * Casts the receiver to a subclass of the given class.  If successful
     * returns the receiver, otherwise if the cast cannot be made throws a
     * <code>ClassCastException</code>.
     * 
     * @param clazz the required type.
     * @return this class cast as a subclass of the given type.
     * @throws ClassCastException if the class cannot be cast to the given type.
     */
    @SuppressWarnings("unchecked")
    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        return (Class<? extends U>)this;
    }

    /**
     * Cast the given object to the type <code>T</code>.
     * If the object is <code>null</code> the result is also
     * <code>null</code>.
     * 
     * @param obj the object to cast
     * @return The object that has been cast.
     * @throws ClassCastException if the object cannot be cast to the given type.
     */
    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        return (T)obj;
    }

    /**
     * Set the "accessible" flag of the given object, without doing any
     * access checks.
     * 
     * <p><b>Note:</b> This method is implemented in native code, and,
     * as such, is less efficient than using {@link ClassCache#REFLECT}
     * to achieve the same goal. This method exists solely to help
     * bootstrap the reflection bridge.</p>
     * 
     * @param ao non-null; the object to modify
     * @param flag the new value for the accessible flag
     */
    /*package*/ static native void setAccessibleNoCheck(AccessibleObject ao,
            boolean flag);
    
    /**
     * Copies two arrays into one. Assumes that the destination array is large
     * enough. 
     * 
     * @param result the destination array
     * @param head the first source array
     * @param tail the second source array
     * @return the destination array, that is, result
     */
    private static <T extends Object> T[] arraycopy(T[] result, T[] head, T[] tail) {
        System.arraycopy(head, 0, result, 0, head.length);
        System.arraycopy(tail, 0, result, head.length, tail.length);
        return result;
    }
    
    /**
     * This must be provided by the vm vendor, as it is used by other provided
     * class implementations in this package. This method is used by
     * SecurityManager.classDepth(), and getClassContext() which use the
     * parameters (-1, false) and SecurityManager.classLoaderDepth(),
     * currentClassLoader(), and currentLoadedClass() which use the parameters
     * (-1, true). Walk the stack and answer an array containing the maxDepth
     * most recent classes on the stack of the calling thread. Starting with the
     * caller of the caller of getStackClasses(), return an array of not more
     * than maxDepth Classes representing the classes of running methods on the
     * stack (including native methods). Frames representing the VM
     * implementation of java.lang.reflect are not included in the list. If
     * stopAtPrivileged is true, the walk will terminate at any frame running
     * one of the following methods: <code><ul>
     * <li>java/security/AccessController.doPrivileged(Ljava/security/PrivilegedAction;)Ljava/lang/Object;</li>
     * <li>java/security/AccessController.doPrivileged(Ljava/security/PrivilegedExceptionAction;)Ljava/lang/Object;</li>
     * <li>java/security/AccessController.doPrivileged(Ljava/security/PrivilegedAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;</li>
     * <li>java/security/AccessController.doPrivileged(Ljava/security/PrivilegedExceptionAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;</li>
     * </ul></code> If one of the doPrivileged methods is found, the walk terminate
     * and that frame is NOT included in the returned array. Notes:
     * <ul>
     * <li>This method operates on the defining classes of methods on stack.
     * NOT the classes of receivers.</li>
     * <li>The item at index zero in the result array describes the caller of
     * the caller of this method.</li>
     * </ul>
     * 
     * @param maxDepth
     *            maximum depth to walk the stack, -1 for the entire stack
     * @param stopAtPrivileged
     *            stop at privileged classes
     * @return the array of the most recent classes on the stack
     */
    static final Class<?>[] getStackClasses(int maxDepth, boolean stopAtPrivileged) {
        return VMStack.getClasses(maxDepth, stopAtPrivileged);
    }
    
}

/**
 * TODO Open issues
 * - Check whether contracts of all (native) methods are ok. Some return null,
 *   others return empty arrays. Same throw exceptions, other don't.
 * - Check whether reflection methods for lists of fields etc. are ok. Some take
 *   superclasses into account, others don't.
 * - Check whether handling of naming for primitive classes is ok.
 * - Searching for specific members could be implemented more efficently.
 * - Test getInterfaces()
 * - Test getEnumConstants()
 * - Check various TODOs in code.
 * - Complete ticket
 */
