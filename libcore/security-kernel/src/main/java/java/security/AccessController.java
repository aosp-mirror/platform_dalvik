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

package java.security;

import java.util.ArrayList;
import java.util.WeakHashMap;

import org.apache.harmony.security.fortress.SecurityUtils;

/**
 * @com.intel.drl.spec_ref
 */
public final class AccessController {

    private AccessController() {
        throw new Error("statics only.");
    };

    /**
     * A map used to store a mapping between a given Thread and
     * AccessControllerContext-s used in successive calls of doPrivileged(). A
     * WeakHashMap is used to allow automagical wiping of the dead threads from
     * the map. The thread (normally Thread.currentThread()) is used as a key
     * for the map, and a value is ArrayList where all AccessControlContext-s are
     * stored. ((ArrayList)contexts.get(Thread.currentThread())).lastElement() - 
     * is reference to the latest context passed to the doPrivileged() call.
     */
    private static final WeakHashMap<Thread, ArrayList<AccessControlContext>> 
    contexts = new WeakHashMap<Thread, ArrayList<AccessControlContext>>();
    
    /**
     * @com.intel.drl.spec_ref 
     */
    public static <T> T doPrivileged(PrivilegedAction<T> action) {
        if (action == null) {
            throw new NullPointerException("action can not be null");
        }
        return doPrivilegedImpl(action, null);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public static <T> T doPrivileged(PrivilegedAction<T> action,
            AccessControlContext context) {
        if (action == null) {
            throw new NullPointerException("action can not be null");
        }
        return doPrivilegedImpl(action, context);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action)
            throws PrivilegedActionException {
        if (action == null) {
            throw new NullPointerException("action can not be null");
        }
        return doPrivilegedImpl(action, null);
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action,
            AccessControlContext context) throws PrivilegedActionException {
        if (action == null) {
            throw new NullPointerException("action can not be null");
        }
        return doPrivilegedImpl(action, context);
    }

    /**
     * The real implementation of doPrivileged() method.<br>
     * It pushes the passed context into this thread's contexts stack,
     * and then invokes <code>action.run()</code>.<br>
     * The pushed context is then investigated in the {@link getContext()}
     * which is called in the {@link checkPermission}.
     */
    private static <T> T doPrivilegedImpl(PrivilegedExceptionAction<T> action,
            AccessControlContext context) throws PrivilegedActionException {

        Thread currThread = Thread.currentThread();

        ArrayList<AccessControlContext> a = null;
        try {
            // currThread==null means that VM warm up is in progress
            if (currThread != null && contexts != null) {
                synchronized (contexts) {
                    a = contexts.get(currThread);
                    if (a == null) {
                        a = new ArrayList<AccessControlContext>();
                        contexts.put(currThread, a);
                    }
                }
                a.add(context);
            }
            return action.run();

        } catch (Exception ex) {
            // Errors automagically go throught - they are not catched by this
            // block

            // Unchecked exceptions must pass through without modification
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }

            // All other (==checked) exceptions get wrapped
            throw new PrivilegedActionException(ex);
        } finally {
            if (currThread != null) {
                // No need to sync() here, as each given 'a' will be accessed 
                // only from one Thread. 'contexts' still need sync() however,
                // as it's accessed from different threads simultaneously
                if (a != null) {
                    // it seems I will never have here [v.size() == 0]
                    a.remove(a.size() - 1);
                }
            }
        }
    }

    /**
     * The real implementation of appropriate doPrivileged() method.<br>
     * It pushes the passed context into this thread's stack of contexts and 
     * then invokes <code>action.run()</code>.<br>
     * The pushed context is then investigated in the {@link getContext()} 
     * which is called in the {@link checkPermission}.  
     */
    private static <T> T doPrivilegedImpl(PrivilegedAction<T> action,
            AccessControlContext context) {

        Thread currThread = Thread.currentThread();

        if (currThread == null || contexts == null) {
            // Big boom time - VM is starting... No need to check permissions:
            // 1st, I do believe there is no malicious code available here for 
            // this moment
            // 2d, I cant use currentThread() as a key anyway - when it will 
            // turn into the real Thread, I'll be unable to retrieve the value 
            // stored with 'currThread==null' as a key.
            return action.run();
        }

        ArrayList<AccessControlContext> a = null;
        try {
            synchronized (contexts) {
                a = contexts.get(currThread);
                if (a == null) {
                    a = new ArrayList<AccessControlContext>();
                    contexts.put(currThread, a);
                }
            }
            a.add(context);

            return action.run();

        } finally {
            // No need to sync() here, as each given 'a' will be accessed 
            // only from one Thread. 'contexts' still need sync() however,
            // as it's accessed from different threads simultaneously
            if (a != null) {
                a.remove(a.size() - 1);
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public static void checkPermission(Permission perm)
            throws AccessControlException {
        if (perm == null) {
            throw new NullPointerException("permission can not be null");
        }

        getContext().checkPermission(perm);
    }

    /**
     * Returns array of ProtectionDomains from the classes residing 
     * on the stack of the current thread, up to and including the caller
     * of the nearest privileged frame.  Reflection frames are skipped.
     * The returned array is never null and never contains null elements,
     * meaning that bootstrap classes are effectively ignored.
     */
    private static native ProtectionDomain[] getStackDomains();
    
    /**
     * @com.intel.drl.spec_ref 
     */
    public static AccessControlContext getContext() {

        // duplicates (if any) will be removed in ACC constructor
        ProtectionDomain[] stack = getStackDomains();
        
        Thread currThread = Thread.currentThread();
        if (currThread == null || contexts == null) {
            // Big boo time. No need to check anything ? 
            return new AccessControlContext(stack);
        }

        ArrayList<AccessControlContext> threadContexts;
        synchronized (contexts) {
            threadContexts = contexts.get(currThread);
        }
        
        AccessControlContext that;
        if ((threadContexts == null) || (threadContexts.size() == 0)) {
            // We were not in doPrivileged method, so
            // have inherited context here
            that = SecurityUtils.getContext(currThread);
        } else {
            // We were in doPrivileged method, so
            // Use context passed to the doPrivileged()
            that = threadContexts.get(threadContexts.size() - 1);
        }

        if (that != null && that.combiner != null) {
            ProtectionDomain[] assigned = null;
            if (that.context != null && that.context.length != 0) {
                    assigned = new ProtectionDomain[that.context.length];
                    System.arraycopy(that.context, 0, assigned, 0,
                            assigned.length);
            }
            ProtectionDomain[] allpds = that.combiner.combine(stack, assigned);
            if (allpds == null) {
                allpds = new ProtectionDomain[0];
            }
            return new AccessControlContext(allpds, that.combiner);
        }

        return new AccessControlContext(stack, that);
    }

    /*
     * TEMPORARY, used for testing implementation.
     */
    //public static ProtectionDomain[] testStackDomains() {
    //    return getStackDomains();
    //}
}
