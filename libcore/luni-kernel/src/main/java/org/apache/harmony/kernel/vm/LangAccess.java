/*
 * Copyright (C) 2008 The Android Open Source Project
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

package org.apache.harmony.kernel.vm;

import dalvik.system.VMStack;

/**
 * Bridge into <code>java.lang</code> from other trusted parts of the
 * core library. Trusted packages either get seeded with an instance
 * of this class directly or may call {@link #getInstance} on this
 * class, to allow them to call into what would otherwise be
 * package-scope functionality in <code>java.lang</code>.
 */
public abstract class LangAccess {
    /** unique instance of this class */
    private static LangAccess theInstance = null;

    /**
     * Sets the unique instance of this class. This may only be done once.
     *
     * @param instance non-null; the instance
     */
    public static void setInstance(LangAccess instance) {
        if (theInstance != null) {
            throw new UnsupportedOperationException("already initialized");
        }

        theInstance = instance;
    }

    /**
     * Gets the unique instance of this class. This is only allowed in
     * very limited situations.
     */
    public static LangAccess getInstance() {
        /*
         * Only code on the bootclasspath is allowed to get at the
         * instance.
         */
        ClassLoader calling = VMStack.getCallingClassLoader2();
        ClassLoader current = LangAccess.class.getClassLoader();

        if ((calling != null) && (calling != current)) {
            throw new SecurityException("LangAccess access denied");
        }

        if (theInstance == null) {
            throw new UnsupportedOperationException("not yet initialized");
        }
        
        return theInstance;
    }
    
    /**
     * Gets a shared array of the enum constants of a given class in
     * declaration (ordinal) order. It is not safe to hand out this
     * array to any user code.
     * 
     * @param clazz non-null; the class in question
     * @return null-ok; the class's list of enumerated constants in
     * declaration order or <code>null</code> if the given class is
     * not an enumeration
     */
    public abstract <T> T[] getEnumValuesInOrder(Class<T> clazz);

    /**
     * Unparks the given thread. This unblocks the thread it if it was 
     * previously parked, or indicates that the thread is "preemptively
     * unparked" if it wasn't already parked. The latter means that the
     * next time the thread is told to park, it will merely clear its
     * latent park bit and carry on without blocking.
     * 
     * <p>See {@link java.util.concurrent.locks.LockSupport} for more
     * in-depth information of the behavior of this method.</p>
     * 
     * @param thread non-null; the thread to unpark
     */
    public abstract void unpark(Thread thread);

    /**
     * Parks the current thread for a particular number of nanoseconds, or
     * indefinitely. If not indefinitely, this method unparks the thread
     * after the given number of nanoseconds if no other thread unparks it
     * first. If the thread has been "preemptively unparked," this method
     * cancels that unparking and returns immediately. This method may
     * also return spuriously (that is, without the thread being told to
     * unpark and without the indicated amount of time elapsing).
     * 
     * <p>See {@link java.util.concurrent.locks.LockSupport} for more
     * in-depth information of the behavior of this method.</p>
     * 
     * @param nanos number of nanoseconds to park for or <code>0</code>
     * to park indefinitely
     * @throws IllegalArgumentException thrown if <code>nanos &lt; 0</code>
     */
    public abstract void parkFor(long nanos);

    /**
     * Parks the current thread until the specified system time. This
     * method attempts to unpark the current thread immediately after
     * <code>System.currentTimeMillis()</code> reaches the specified
     * value, if no other thread unparks it first. If the thread has
     * been "preemptively unparked," this method cancels that
     * unparking and returns immediately. This method may also return
     * spuriously (that is, without the thread being told to unpark
     * and without the indicated amount of time elapsing).
     *
     * <p>See {@link java.util.concurrent.locks.LockSupport} for more
     * in-depth information of the behavior of this method.</p>
     * 
     * @param time the time after which the thread should be unparked,
     * in absolute milliseconds-since-the-epoch
     */
    public abstract void parkUntil(long time);
}
