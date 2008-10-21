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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FilePermission;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.net.InetAddress;
import java.net.SocketPermission;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permission;
import java.security.Security;
import java.security.SecurityPermission;
import java.util.PropertyPermission;
import java.util.StringTokenizer;

import org.apache.harmony.luni.util.PriviAction;

/**
 * SecurityManager is the abstract superclass of the classes which can provide
 * security verification for a running program.
 */
public class SecurityManager {

    private static final PropertyPermission READ_WRITE_ALL_PROPERTIES_PERMISSION = new PropertyPermission(
            "*", "read,write"); //$NON-NLS-1$ //$NON-NLS-2$

    private static final String PKG_ACC_KEY = "package.access"; //$NON-NLS-1$

    private static final String PKG_DEF_KEY = "package.definition"; //$NON-NLS-1$

    /**
     * Flag to indicate whether a security check is in progress.
     * 
     * @deprecated Use {@link #checkPermission}
     */
    @Deprecated
    protected boolean inCheck;

    /**
     * Constructs a new instance of this class.
     */
    public SecurityManager() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security
                    .checkPermission(RuntimePermission.permissionToCreateSecurityManager);
        }
        Class<?> type = Security.class; // initialize Security properties
        if (type == null) {
            throw new AssertionError();
        }
    }

    /**
     * Checks whether the running program is allowed to accept socket
     * connections.
     * 
     * @param host
     *            the address of the host which is attempting to connect
     * @param port
     *            the port number to check
     */
    public void checkAccept(String host, int port) {
        if (host == null) {
            throw new NullPointerException();
        }
        checkPermission(new SocketPermission(host + ':' + port, "accept")); //$NON-NLS-1$
    }

    /**
     * Checks whether the running program is allowed to modify the thread.
     * 
     * @param thread
     *            the thread we are attempting to modify
     */
    public void checkAccess(Thread thread) {
        // Only worry about system threads. Dead threads have a null group.
        ThreadGroup group = thread.getThreadGroup();
        if ((group != null) && (group.parent == null)) {
            checkPermission(RuntimePermission.permissionToModifyThread);
        }
    }

    /**
     * Checks whether the running program is allowed to modify the thread group.
     * 
     * 
     * @param group
     *            the thread group we are attempting to modify
     */
    public void checkAccess(ThreadGroup group) {
        // Only worry about system threads.
        if (group == null) {
            throw new NullPointerException();
        }
        if (group.parent == null) {
            checkPermission(RuntimePermission.permissionToModifyThreadGroup);
        }
    }

    /**
     * Checks whether the running program is allowed to establish socket
     * connections. A -1 port indicates the caller is trying to resolve the
     * hostname.
     * 
     * @param host
     *            String the address of the host to connect to.
     * @param port
     *            int the port number to check, or -1 for resolve.
     */
    public void checkConnect(String host, int port) {
        if (host == null) {
            throw new NullPointerException();
        }
        if (port > 0) {
            checkPermission(new SocketPermission(host + ':' + port, "connect")); //$NON-NLS-1$
        } else {
            checkPermission(new SocketPermission(host, "resolve")); //$NON-NLS-1$
        }
    }

    /**
     * Checks whether the given security context is allowed to establish socket
     * connections. A -1 port indicates the caller is trying to resolve the
     * hostname.
     * 
     * @param host
     *            String the address of the host to connect to.
     * @param port
     *            int the port number to check, or -1 for resolve.
     * @param context
     *            Object the security context to use for the check.
     */
    public void checkConnect(String host, int port, Object context) {
        if (port > 0) {
            checkPermission(new SocketPermission(host + ':' + port, "connect"), //$NON-NLS-1$
                    context);
        } else {
            checkPermission(new SocketPermission(host, "resolve"), context); //$NON-NLS-1$
        }
    }

    /**
     * Checks whether the running program is allowed to create a class loader.
     */
    public void checkCreateClassLoader() {
        checkPermission(RuntimePermission.permissionToCreateClassLoader);
    }

    /**
     * Checks whether the running program is allowed to delete the file named by
     * the argument, which should be passed in canonical form.
     * 
     * @param file
     *            the name of the file to check
     *            
     * @throws java.lang.SecurityException if the caller is not allowed to
     *         delete the given file.
     */
    public void checkDelete(String file) {
        checkPermission(new FilePermission(file, "delete")); //$NON-NLS-1$
    }

    /**
     * Checks whether the running program is allowed to execute the specified
     * platform specific command.
     * 
     * @param cmd
     *            the command line
     */
    public void checkExec(String cmd) {
        checkPermission(new FilePermission(new File(cmd).isAbsolute() ? cmd
                : "<<ALL FILES>>", "execute")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Checks whether the running program is allowed to terminate itself.
     * 
     * @param status
     *            the status to return from the exit.
     */
    public void checkExit(int status) {
        checkPermission(RuntimePermission.permissionToExitVM);
    }

    /**
     * Checks whether the running program is allowed to load the specified
     * native library.
     * 
     * @param libName
     *            the name of the library to load
     */
    public void checkLink(String libName) {
        if (libName == null) {
            throw new NullPointerException();
        }
        checkPermission(new RuntimePermission("loadLibrary." + libName)); //$NON-NLS-1$
    }

    /**
     * Checks whether the running program is allowed to listen on the specified
     * port.
     * 
     * @param port
     *            int the port number to check
     */
    public void checkListen(int port) {
        if (port == 0) {
            checkPermission(new SocketPermission("localhost:1024-", "listen")); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            checkPermission(new SocketPermission("localhost:" + port, "listen")); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    /**
     * Checks whether the running program is allowed to access members. The
     * default is to allow access to public members (i.e.
     * java.lang.reflect.PUBLIC) and to classes loaded by the same loader as the
     * original caller (i.e. the method that called the reflect API).
     * 
     * Due to the nature of the check, overriding implementations cannot call
     * super.checkMemberAccess() since the stack would no longer be of the
     * expected shape.
     * 
     * @param cls ?
     * @param type
     *            Either java.lang.reflect.Member.PUBLIC or DECLARED
     */
    public void checkMemberAccess(Class<?> cls, int type) {
        if (cls == null) {
            throw new NullPointerException();
        }
        if (type == Member.PUBLIC) {
            return;
        }
        //
        // Need to compare the classloaders.
        // Stack shape is
        // <user code> <- want this class
        // Class.getDeclared*();
        // Class.checkMemberAccess();
        // SecurityManager.checkMemberAccess(); <- current frame
        //
        // Use getClassLoaderImpl() since getClassLoader()
        // returns null for the bootstrap class loader.
        if (ClassLoader.getStackClassLoader(3) == cls.getClassLoaderImpl()) {
            return;
        }

        // Forward off to the permission mechanism.
        checkPermission(new RuntimePermission("accessDeclaredMembers")); //$NON-NLS-1$
    }

    /**
     * Checks whether the running program is allowed to join, leave or send to a
     * multicast address.
     */
    public void checkMulticast(InetAddress maddr) {
        checkPermission(new SocketPermission(maddr.getHostAddress(),
                "accept,connect")); //$NON-NLS-1$
    }

    /**
     * Checks whether the running program is allowed to join, leave or send to a
     * multicast address.
     * 
     * @deprecated use {@link #checkMulticast(java.net.InetAddress)}
     */
    @Deprecated
    public void checkMulticast(InetAddress maddr, byte ttl) {
        checkPermission(new SocketPermission(maddr.getHostAddress(),
                "accept,connect")); //$NON-NLS-1$
    }

    /**
     * Checks whether the running program is allowed to access the specified
     * package.
     * 
     * @param packageName
     *            the name of the package to be accessed.
     */
    public void checkPackageAccess(String packageName) {
        if (packageName == null) {
            throw new NullPointerException();
        }
        if (checkPackageProperty(PKG_ACC_KEY, packageName)) {
            checkPermission(new RuntimePermission("accessClassInPackage." //$NON-NLS-1$
                    + packageName));
        }
    }

    /**
     * Checks whether the running program is allowed to define new classes in
     * the specified package.
     * 
     * @param packageName
     *            the name of the package to add a class to.
     */
    public void checkPackageDefinition(String packageName) {
        if (packageName == null) {
            throw new NullPointerException();
        }
        if (checkPackageProperty(PKG_DEF_KEY, packageName)) {
            checkPermission(new RuntimePermission("defineClassInPackage." //$NON-NLS-1$
                    + packageName));
        }
    }

    /**
     * Returns true if the package name is restricted by the specified security
     * property.
     */
    private static boolean checkPackageProperty(final String property,
            final String pkg) {
        String list = AccessController.doPrivileged(PriviAction
                .getSecurityProperty(property));
        if (list != null) {
            int plen = pkg.length();
            StringTokenizer tokenizer = new StringTokenizer(list, ", "); //$NON-NLS-1$
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                int tlen = token.length();
                if (plen > tlen
                        && pkg.startsWith(token)
                        && (token.charAt(tlen - 1) == '.' || pkg.charAt(tlen) == '.')) {
                    return true;
                } else if (plen == tlen && token.startsWith(pkg)) {
                    return true;
                } else if (plen + 1 == tlen && token.startsWith(pkg)
                        && token.charAt(tlen - 1) == '.') {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks whether the running program is allowed to access the system
     * properties.
     */
    public void checkPropertiesAccess() {
        checkPermission(READ_WRITE_ALL_PROPERTIES_PERMISSION);
    }

    /**
     * Checks whether the running program is allowed to access a particular
     * system property.
     * 
     * @param key
     *            the name of the property to be accessed.
     */
    public void checkPropertyAccess(String key) {
        checkPermission(new PropertyPermission(key, "read")); //$NON-NLS-1$
    }

    /**
     * Checks whether the running program is allowed to read from the file whose
     * descriptor is the argument.
     * 
     * @param fd
     *            the file descriptor of the file to check
     */
    public void checkRead(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException();
        }
        checkPermission(RuntimePermission.permissionToReadFileDescriptor);
    }

    /**
     * Checks whether the running program is allowed to read from the file named
     * by the argument, which should be passed in canonical form.
     * 
     * @param file
     *            String the name of the file or directory to check.
     */
    public void checkRead(String file) {
        checkPermission(new FilePermission(file, "read")); //$NON-NLS-1$
    }

    /**
     * Checks whether the given security context is allowed to read from the
     * file named by the argument, which should be passed in canonical form.
     * 
     * @param file
     *            String the name of the file or directory to check.
     * @param context
     *            Object the security context to use for the check.
     */
    public void checkRead(String file, Object context) {
        checkPermission(new FilePermission(file, "read"), context); //$NON-NLS-1$
    }

    /**
     * Checks whether the running program is allowed to perform the security
     * operation named by the target.
     * 
     * @param target
     *            String the name of the operation to perform.
     */
    public void checkSecurityAccess(String target) {
        checkPermission(new SecurityPermission(target));
    }

    /**
     * Checks whether the running program is allowed to set the net object
     * factories.
     */
    public void checkSetFactory() {
        checkPermission(RuntimePermission.permissionToSetFactory);
    }

    /**
     * Checks whether the running program is allowed to create a top level
     * window.
     * 
     * @param window
     *            The non-null window for which to check access
     */
    public boolean checkTopLevelWindow(Object window) {
        if (window == null) {
            throw new NullPointerException();
        }
        try {
            Class<?> awtPermission = Class.forName("java.awt.AWTPermission"); //$NON-NLS-1$
            Constructor<?> constructor = awtPermission
                    .getConstructor(String.class);
            Object perm = constructor
                    .newInstance("showWindowWithoutWarningBanner"); //$NON-NLS-1$
            checkPermission((Permission) perm);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the running program is allowed to access the system
     * clipboard.
     */
    public void checkSystemClipboardAccess() {
        try {
            Class<?> awtPermission = Class.forName("java.awt.AWTPermission"); //$NON-NLS-1$
            Constructor<?> constructor = awtPermission
                    .getConstructor(String.class);
            Object perm = constructor.newInstance("accessClipboard"); //$NON-NLS-1$
            checkPermission((Permission) perm);
            return;
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        throw new SecurityException();
    }

    /**
     * Checks whether the running program is allowed to access the AWT Event
     * queue. Since we don't support AWT, the answer is no.
     */
    public void checkAwtEventQueueAccess() {
        try {
            Class<?> awtPermission = Class.forName("java.awt.AWTPermission"); //$NON-NLS-1$
            Constructor<?> constructor = awtPermission
                    .getConstructor(String.class);
            Object perm = constructor.newInstance("accessEventQueue"); //$NON-NLS-1$
            checkPermission((Permission) perm);
            return;
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        throw new SecurityException();
    }

    /**
     * Checks whether the running program is allowed to start a new print job.
     */
    public void checkPrintJobAccess() {
        checkPermission(RuntimePermission.permissionToQueuePrintJob);
    }

    /**
     * Checks whether the running program is allowed to read from the file whose
     * descriptor is the argument.
     * 
     * @param fd
     *            the file descriptor of the file to check
     */
    public void checkWrite(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException();
        }
        checkPermission(RuntimePermission.permissionToWriteFileDescriptor);
    }

    /**
     * Checks whether the running program is allowed to write to the file named
     * by the argument, which should be passed in canonical form.
     * 
     * @param file
     *            the name of the file to check
     */
    public void checkWrite(String file) {
        checkPermission(new FilePermission(file, "write")); //$NON-NLS-1$
    }

    /**
     * Returns true if the security manager is currently checking something.
     * 
     * @return boolean true if we are are in a security check method.
     * 
     * @deprecated Use {@link #checkPermission}
     */
    @Deprecated
    public boolean getInCheck() {
        return inCheck;
    }

    /**
     * Returns an array containing one entry for each method in the stack. Each
     * entry is the java.lang.Class which represents the class in which the
     * method is defined.
     * 
     * @return Class[] all of the classes in the stack.
     */
    @SuppressWarnings("unchecked")
    protected Class[] getClassContext() {
        return Class.getStackClasses(-1, false);
    }

    /**
     * Returns the class loader of the first class in the stack whose class
     * loader is not a system class loader.
     * 
     * @return ClassLoader the most recent non-system class loader.
     * 
     * @deprecated Use {@link #checkPermission}
     */
    @Deprecated
    protected ClassLoader currentClassLoader() {

        /*
         * First, check if AllPermission is allowed. If so, then we are
         * effectively running in an unsafe environment, so just answer null
         * (==> everything is a system class).
         */
        try {
            checkPermission(new AllPermission());
            return null;
        } catch (SecurityException ex) {
        }

        /*
         * Now, check if there are any non-system class loaders in the stack up
         * to the first privileged method (or the end of the stack.
         */
        Class<?>[] classes = Class.getStackClasses(-1, true);
        for (int i = 0; i < classes.length; i++) {
            ClassLoader cl = classes[i].getClassLoaderImpl();
            if (!cl.isSystemClassLoader()) {
                return cl;
            }
        }
        return null;
    }

    /**
     * Returns the index in the stack of three first class whose class loader is
     * not a system class loader.
     * 
     * @return int the frame index of the first method whose class was loaded by
     *         a non-system class loader.
     * 
     * @deprecated Use {@link #checkPermission}
     */
    @Deprecated
    protected int classLoaderDepth() {
        /*
         * First, check if AllPermission is allowed. If so, then we are
         * effectively running in an unsafe environment, so just answer -1 (==>
         * everything is a system class).
         */
        try {
            checkPermission(new AllPermission());
            return -1;
        } catch (SecurityException ex) {
        }

        /*
         * Now, check if there are any non-system class loaders in the stack up
         * to the first privileged method (or the end of the stack.
         */
        Class<?>[] classes = Class.getStackClasses(-1, true);
        for (int i = 0; i < classes.length; i++) {
            ClassLoader cl = classes[i].getClassLoaderImpl();
            if (!cl.isSystemClassLoader()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the first class in the stack which was loaded by a class loader
     * which is not a system class loader.
     * 
     * @return Class the most recent class loaded by a non-system class loader.
     * 
     * @deprecated Use {@link #checkPermission}
     */
    @Deprecated
    protected Class<?> currentLoadedClass() {
        /*
         * First, check if AllPermission is allowed. If so, then we are
         * effectively running in an unsafe environment, so just answer null
         * (==> everything is a system class).
         */
        try {
            checkPermission(new AllPermission());
            return null;
        } catch (SecurityException ex) {
        }

        /*
         * Now, check if there are any non-system class loaders in the stack up
         * to the first privileged method (or the end of the stack.
         */
        Class<?>[] classes = Class.getStackClasses(-1, true);
        for (int i = 0; i < classes.length; i++) {
            ClassLoader cl = classes[i].getClassLoaderImpl();
            if (!cl.isSystemClassLoader()) {
                return classes[i];
            }
        }
        return null;
    }

    /**
     * Returns the index in the stack of the first method which is contained in
     * a class called <code>name</code>. If no methods from this class are in
     * the stack, return -1.
     * 
     * @param name
     *            String the name of the class to look for.
     * @return int the depth in the stack of a the first method found.
     * 
     * @deprecated Use {@link #checkPermission}
     */
    @Deprecated
    protected int classDepth(String name) {
        Class<?>[] classes = Class.getStackClasses(-1, false);
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if there is a method on the stack from the specified class,
     * and false otherwise.
     * 
     * @param name
     *            String the name of the class to look for.
     * @return boolean true if we are running a method from the specified class.
     * 
     * @deprecated Use {@link #checkPermission}
     */
    @Deprecated
    protected boolean inClass(String name) {
        return classDepth(name) != -1;
    }

    /**
     * Returns true if there is a method on the stack from a class which was
     * defined by a non-system classloader.
     * 
     * @return boolean
     * 
     * @deprecated Use {@link #checkPermission}
     */
    @Deprecated
    protected boolean inClassLoader() {
        return currentClassLoader() != null;
    }

    /**
     * Returns the thread group which should be used to instantiate new threads.
     * By default, this is the same as the thread group of the thread running
     * this method.
     * 
     * @return ThreadGroup The thread group to create new threads in.
     */
    public ThreadGroup getThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }

    /**
     * Returns an object which encapsulates the security state of the current
     * point in the execution. In our case, this is an AccessControlContext.
     */
    public Object getSecurityContext() {
        return AccessController.getContext();
    }

    /**
     * Checks whether the running program is allowed to access the resource
     * being guarded by the given Permission argument.
     * 
     * @param permission
     *            the permission to check
     */
    public void checkPermission(Permission permission) {
        try {
            inCheck = true;
            AccessController.checkPermission(permission);
        } finally {
            inCheck = false;
        }
    }

    /**
     * Checks whether the running program is allowed to access the resource
     * being guarded by the given Permission argument.
     * 
     * @param permission
     *            the permission to check
     */
    public void checkPermission(Permission permission, Object context) {
        try {
            inCheck = true;
            // Must be an AccessControlContext. If we don't check
            // this, then applications could pass in an arbitrary
            // object which circumvents the security check.
            if (context instanceof AccessControlContext) {
                ((AccessControlContext) context).checkPermission(permission);
            } else {
                throw new SecurityException();
            }
        } finally {
            inCheck = false;
        }
    }
}
