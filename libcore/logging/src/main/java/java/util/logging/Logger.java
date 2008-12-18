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

package java.util.logging;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.harmony.logging.internal.nls.Messages;

/**
 * Loggers are used to log records to certain outputs, including file, console,
 * etc. They use various handlers to actually do the output-dependent
 * operations.
 * <p>
 * Client applications can get named loggers by calling the {@code getLogger}
 * methods. They can also get anonymous loggers by calling the
 * {@code getAnonymousLogger} methods. Named loggers are organized in a
 * namespace hierarchy managed by a log manager. The naming convention is
 * usually the same as java package's naming convention, that is using
 * dot-separated strings. Anonymous loggers do not belong to any namespace.
 * </p>
 * <p>
 * Loggers "inherit" log level setting from their parent if their own level is
 * set to {@code null}. This is also true for the resource bundle. The logger's
 * resource bundle is used to localize the log messages if no resource bundle
 * name is given when a log method is called. If {@code getUseParentHandlers()}
 * returns {@code true}, loggers also inherit their parent's handlers. In this
 * context, "inherit" only means that "behavior" is inherited. The internal
 * field values will not change, for example, {@code getLevel()} still returns
 * {@code null}.
 * </p>
 * <p>
 * When loading a given resource bundle, the logger first tries to use the
 * context classloader. If that fails, it tries the system classloader. And if
 * that still fails, it searches up the class stack and uses each class's
 * classloader to try to locate the resource bundle.
 * </p>
 * <p>
 * Some log methods accept log requests that do not specify the source class and
 * source method. In these cases, the logging framework will automatically infer
 * the calling class and method, but this is not guaranteed to be accurate.
 * </p>
 * <p>
 * Once a {@code LogRecord} object has been passed into the logging framework,
 * it is owned by the logging framework and the client applications should not
 * use it any longer.
 * </p>
 * <p>
 * All methods of this class are thread-safe.
 * </p>
 * 
 * @see LogManager
 * @since Android 1.0
 */
public class Logger {

    /**
     * The global logger is provided as convenience for casual use.
     * 
     * @since Android 1.0
     */
    public final static Logger global = new Logger("global", null); //$NON-NLS-1$

    // the name of this logger
    private volatile String name;

    // the parent logger of this logger
    private Logger parent;

    // the logging level of this logger
    private volatile Level levelObjVal;

    // the logging level as int of this logger
    private volatile int levelIntVal;

    // the filter
    private Filter filter;

    // the name of the resource bundle used to localize logging messages
    private String resBundleName;

    // the loaded resource bundle according to the specified name
    private ResourceBundle resBundle;

    // the handlers attached to this logger
    private List<Handler> handlers;

    /*
     * flag indicating whether to notify parent's handlers on receiving a log
     * request
     */
    private boolean notifyParentHandlers;

    // flag indicating whether this logger is named or anonymous
    private boolean isNamed;

    private List<Logger> childs;
    
    private LogManager manager;

    // BEGIN android-changed
    private volatile boolean handlerInited;
    // END android-changed


    /*
     * -------------------------------------------------------------------
     * Constructors
     * -------------------------------------------------------------------
     */

    /**
     * Constructs a {@code Logger} object with the supplied name and resource
     * bundle name; {@code notifiyParentHandlers} is set to {@code true}.
     * <p>
     * Notice : Loggers use a naming hierarchy. Thus "z.x.y" is a child of "z.x". 
     * </p>
     * 
     * @param name
     *            the name of this logger, may be {@code null} for anonymous
     *            loggers.
     * @param resourceBundleName
     *            the name of the resource bundle used to localize logging
     *            messages, may be {@code null}.
     * @throws MissingResourceException
     *             if the specified resource bundle can not be loaded.
     * @since Android 1.0
     */
    protected Logger(String name, String resourceBundleName) {
        // try to load the specified resource bundle first
        if (null == resourceBundleName) {
            this.resBundleName = null;
            this.resBundle = null;
        } else {
            this.resBundle = loadResourceBundle(resourceBundleName);
            this.resBundleName = resourceBundleName;
        }
        this.name = name;
        this.parent = null;
        this.filter = null;
        this.childs = new ArrayList<Logger>();
        this.notifyParentHandlers = true;
        // any logger is not anonymous by default
        this.isNamed = true;

        //-- 'null' means that level will be inherited from parent (see getLevel)
        //-- Level.INFO is default level if we don't set it. It will be
        //-- changed to parent level or to configLevel after adding to the
        //-- family tree. As of this, actually, setting to Level.INFO is
        //-- not needed here.
        this.levelObjVal = null;
        this.levelIntVal = Level.INFO.intValue();
    }

    //-- should be called under the lm lock
    private void setLevelImpl(Level newLevel) {
        // update levels for the whole hierarchy
        int oldVal = levelIntVal;
        levelObjVal = newLevel;
        if (null == newLevel) {
            levelIntVal = null != parent
                    ? parent.levelIntVal
                    : Level.INFO.intValue();
        } else {
            levelIntVal = newLevel.intValue();
        }
        if (oldVal != levelIntVal) {
            forceChildsToInherit();
        }
    }

    //-- should be called under the lm lock
    private void forceChildsToInherit() {
        for (Logger child : childs) {
            if (null == child.levelObjVal) { // should inherit
                child.setLevelImpl(null);
            }
        }
    }

    /*
     * -------------------------------------------------------------------
     * Methods
     * -------------------------------------------------------------------
     */

    /**
     * Load the specified resource bundle, use privileged code.
     * 
     * @param resourceBundleName
     *            the name of the resource bundle to load, cannot be {@code null}.
     * @return the loaded resource bundle.
     * @throws MissingResourceException
     *             if the specified resource bundle can not be loaded.
     */
    static ResourceBundle loadResourceBundle(String resourceBundleName) {
        // try context class loader to load the resource
        ClassLoader cl = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
        if (null != cl) {
            try {
                return ResourceBundle.getBundle(resourceBundleName, Locale
                        .getDefault(), cl);
            } catch (MissingResourceException e) {
                // Failed to load using context classloader, ignore
            }
        }
        // try system class loader to load the resource
        cl = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return ClassLoader.getSystemClassLoader();
                    }
                });
        if (null != cl) {
            try {
                return ResourceBundle.getBundle(resourceBundleName, Locale
                        .getDefault(), cl);
            } catch (MissingResourceException e) {
                // Failed to load using system classloader, ignore
            }
        }
        // try all class loaders up the class stack
        final Class<?>[] classes = AccessController
                .doPrivileged(new PrivilegedAction<Class<?>[]>() {
                    public Class<?>[] run() {
                        return (new PrivateSecurityManager())
                                .privateGetClassContext();
                    }
                });
        // the first class, which is PrivateSecurityManager, is skipped
        for (int i = 1; i < classes.length; i++) {
            final int index = i;
            try {
                cl = AccessController.doPrivileged(
                        new PrivilegedAction<ClassLoader>() {
                            public ClassLoader run() {
                                return classes[index].getClassLoader();
                            }
                        });
                if (null == cl) {
                    continue;
                }
                return ResourceBundle.getBundle(resourceBundleName, Locale
                        .getDefault(), cl);
            } catch (MissingResourceException e) {
                // Failed to load using the current class's classloader, ignore
            }
        }
        // logging.8=Failed to load the specified resource bundle "{0}".
        throw new MissingResourceException(Messages.getString("logging.8", //$NON-NLS-1$
                resourceBundleName), resourceBundleName, null);  
    }

    /**
     * Gets an anonymous logger to use internally in a thread. Anonymous loggers
     * are not registered in the log manager's namespace. No security checks
     * will be performed when updating an anonymous logger's control settings.
     * <p>
     * The anonymous loggers' parent is set to be the root logger. This way it
     * inherits the default logging level and handlers from the root logger.
     * </p>
     * 
     * @return a new instance of anonymous logger.
     * @since Android 1.0
     */
    public static Logger getAnonymousLogger() {
        return getAnonymousLogger(null);
    }

    /**
     * Gets an anonymous logger to use internally in a thread. Anonymous loggers
     * are not registered in the log manager's namespace. No security checks
     * will be performed when updating an anonymous logger's control settings.
     * <p>
     * The anonymous loggers' parent is set to be the root logger. This way it
     * inherits default logging level and handlers from the root logger.
     * </p>
     * 
     * @param resourceBundleName
     *            the name of the resource bundle used to localize log messages.
     * @return a new instance of anonymous logger.
     * @throws MissingResourceException
     *             if the specified resource bundle can not be loaded.
     * @since Android 1.0
     */
    public static Logger getAnonymousLogger(String resourceBundleName) {
        final Logger l = new Logger(null, resourceBundleName);
        l.isNamed = false;
        l.internalSetParent(LogManager.getLogManager().getLogger("")); //$NON-NLS-1$
        return l;
    }

    /*
     * Check whether the same resource bundle has been specified.
     * Synchronize to ensure the consistency between resource bundle
     * and its name.
     */
    private static void updateResourceBundle(Logger l, String resourceBundleName) {
        synchronized (l) {
            if (null == l.getResourceBundleName()) {
                if(null == resourceBundleName){
                    return;
                }
                /*
                 * load the resource bundle if none is specified
                 * before
                 */
                l.resBundle = loadResourceBundle(resourceBundleName);
                l.resBundleName = resourceBundleName;
            } else if (!l.getResourceBundleName().equals(resourceBundleName)) {
                /*
                 * throw exception if the specified resource bundles
                 * are inconsistent with each other, i.e., different
                 * names
                 */
                // logging.9=The specified resource bundle name "{0}" is 
                // inconsistent with the existing one "{1}".
                throw new IllegalArgumentException(Messages.getString(
                        "logging.9", //$NON-NLS-1$
                        resourceBundleName, l.getResourceBundleName())); 
            }
        }
    }

    /*
     * Gets a named logger associated with the supplied resource bundle. This
     * method accepts null resource bundle name. The method body is synchronized
     * on the instance of the LogManager to insure the consistency of the whole
     * operation.
     */
    private static Logger getLoggerWithRes(String name,
            String resourceBundleName, boolean hasResourceName) {
        LogManager man = LogManager.getLogManager();
        Logger l = null;
        synchronized (man) {
            // Try to find an existing logger with the specified name
            l = man.getLogger(name);
            // If no existing logger with the same name, create a new one
            if (null == l) {
                l = new Logger(name, resourceBundleName);
                man.addLogger(l);
                return l;
            }
        }
        if (hasResourceName) {
            updateResourceBundle(l, resourceBundleName);
        }
        return l;
    }

    /**
     * Gets a named logger. The returned logger may already exist or may be
     * newly created. In the latter case, its level will be set to the
     * configured level according to the {@code LogManager}'s properties.
     * 
     * @param name
     *            the name of the logger to get, cannot be {@code null}.
     * @return a named logger.
     * @since Android 1.0
     */
    public static Logger getLogger(String name) {
        return getLoggerWithRes(name, null, false);
    }

    /**
     * Gets a named logger associated with the supplied resource bundle. The
     * resource bundle will be used to localize logging messages.
     * 
     * @param name
     *            the name of the logger to get, cannot be {@code null}.
     * @param resourceBundleName
     *            the name of the resource bundle, may be {@code null}.
     * @throws IllegalArgumentException
     *             if the logger identified by {@code name} is associated with a
     *             resource bundle and its name is not equal to
     *             {@code resourceBundleName}.
     * @throws MissingResourceException
     *             if the name of the resource bundle cannot be found.
     * @return a named logger.
     * @since Android 1.0
     */
    public static Logger getLogger(String name, String resourceBundleName) {
        return getLoggerWithRes(name, resourceBundleName, true);
    }

    /**
     * Adds a handler to this logger. The {@code name} will be fed with log
     * records received by this logger.
     * 
     * @param handler
     *            the handler object to add, cannot be {@code null}.
     * @throws SecurityException
     *             if a security manager determines that the caller does not
     *             have the required permission.
     * @since Android 1.0
     */
    public void addHandler(Handler handler) {
        if (null == handler) {
            // logging.A=The 'handler' parameter is null.
            throw new NullPointerException(Messages.getString("logging.A")); //$NON-NLS-1$
        }
        // Anonymous loggers can always add handlers
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        initHandler();
        synchronized(this){
            this.handlers.add(handler);
        }
    }
    
    /*
     * Be cautious to avoid deadlock when using this method, it gets lock on manager 
     * at first, and then gets lock on this Logger, so any methods should not hold 
     * lock on this Logger when invoking this method. 
     */
    private void initHandler() {
        if(!handlerInited){
            synchronized (this) {
                if (!handlerInited) {
                    // BEGIN android-added
                    /*
                     * Force LogManager to be initialized, since its
                     * class init code performs necessary one-time setup.
                     */ 
                    LogManager.getLogManager();
                    // END android-added
                    if (handlers == null) {
                        handlers = new ArrayList<Handler>();
                    }
                    if (manager == null) {
                        return;
                    }

                    String handlerStr = manager
                            .getProperty("".equals(name) ? "handlers" : name + ".handlers"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (null == handlerStr) {
                        return;
                    }
                    StringTokenizer st = new StringTokenizer(handlerStr, " "); //$NON-NLS-1$
                    while (st.hasMoreTokens()) {
                        String handlerName = st.nextToken();
                        // BEGIN android-changed
                        // deal with non-existing handler
                        try {
                            Handler handler = (Handler) LogManager
                                    .getInstanceByClass(handlerName);
                            handlers.add(handler);
                            String level = manager.getProperty(handlerName
                                    + ".level"); //$NON-NLS-1$
                            if (null != level) {
                                handler.setLevel(Level.parse(level));
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        // END android-changed
                    }
                    handlerInited = true;
                }
            }
        }
    }

    /**
     * Gets all the handlers associated with this logger.
     * 
     * @return an array of all the handlers associated with this logger.
     * @since Android 1.0
     */
    public Handler[] getHandlers() {
        initHandler();
        synchronized(this){
            return handlers.toArray(new Handler[handlers.size()]);
        }
    }

    /**
     * Removes a handler from this logger. If the specified handler does not
     * exist then this method has no effect.
     * 
     * @param handler
     *            the handler to be removed.
     * @throws SecurityException
     *             if a security manager determines that the caller does not
     *             have the required permission.
     * @since Android 1.0
     */
    public void removeHandler(Handler handler) {
        // Anonymous loggers can always remove handlers
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        if (null == handler) {
            return;
        }
        initHandler();
        synchronized(this){
            this.handlers.remove(handler);
        }
    }

    /**
     * Gets the filter used by this logger.
     * 
     * @return the filter used by this logger, may be {@code null}.
     * @since Android 1.0
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * Sets the filter used by this logger.
     * 
     * @param newFilter
     *            the filter to set, may be {@code null}.
     * @throws SecurityException
     *             if a security manager determines that the caller does not
     *             have the required permission.
     * @since Android 1.0
     */
    public void setFilter(Filter newFilter) {
        // Anonymous loggers can always set the filter
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        filter = newFilter;
    }

    /**
     * Gets the logging level of this logger. A {@code null} level indicates
     * that this logger inherits its parent's level.
     * 
     * @return the logging level of this logger.
     * @since Android 1.0
     */
    public Level getLevel() {
        return levelObjVal;
    }

    /**
     * Sets the logging level for this logger. A {@code null} level indicates
     * that this logger will inherit its parent's level.
     * 
     * @param newLevel
     *            the logging level to set.
     * @throws SecurityException
     *             if a security manager determines that the caller does not
     *             have the required permission.
     * @since Android 1.0
     */
    public void setLevel(Level newLevel) {
        // Anonymous loggers can always set the level
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        synchronized (LogManager.getLogManager()) {
            setLevelImpl(newLevel);
        }
    }

    /**
     * Gets the flag which indicates whether to use the handlers of this
     * logger's parent to publish incoming log records, potentially recursively
     * up the namespace.
     * 
     * @return {@code true} if set to use parent's handlers, {@code false}
     *         otherwise.
     * @since Android 1.0
     */
    public boolean getUseParentHandlers() {
        return this.notifyParentHandlers;
    }

    /**
     * Sets the flag which indicates whether to use the handlers of this
     * logger's parent, potentially recursively up the namespace.
     * 
     * @param notifyParentHandlers
     *            the new flag indicating whether to use the parent's handlers.
     * @throws SecurityException
     *             if a security manager determines that the caller does not
     *             have the required permission.
     * @since Android 1.0
     */
    public void setUseParentHandlers(boolean notifyParentHandlers) {
        // Anonymous loggers can always set the useParentHandlers flag
        if (this.isNamed) {
            LogManager.getLogManager().checkAccess();
        }
        this.notifyParentHandlers = notifyParentHandlers;
    }

    /**
     * Gets the nearest parent of this logger in the namespace, a {@code null}
     * value will be returned if called on the root logger.
     * 
     * @return the parent of this logger in the namespace.
     * @since Android 1.0
     */
    public Logger getParent() {
        return parent;
    }

    /**
     * Sets the parent of this logger in the namespace. This method should
     * usually be used by the {@code LogManager} object only. This method does
     * not check security.
     * 
     * @param newParent
     *            the parent logger to set.
     * @since Android 1.0
     */
    void internalSetParent(Logger newParent) {
        //All hierarchy related modifications should get LogManager lock at first
        synchronized(LogManager.getLogManager()){
            parent = newParent;
            // -- update level after setting a parent.
            // -- if level == null we should inherit the parent's level
            if (null == levelObjVal) {
                setLevelImpl(levelObjVal);
            }
            newParent.addChild(this);
        }
    }

    /**
     * Sets the parent of this logger in the namespace. This method should be
     * used by the {@code LogManager} object only.
     * 
     * @param parent
     *            the parent logger to set.
     * @throws SecurityException
     *             if a security manager determines that the caller does not
     *             have the required permission.
     * @since Android 1.0            
     */
    public void setParent(Logger parent) {
        if (null == parent) {
            // logging.B=The 'parent' parameter is null.
            throw new NullPointerException(Messages.getString("logging.B")); //$NON-NLS-1$
        }
        // even anonymous loggers are checked
        LogManager.getLogManager().checkAccess();
        internalSetParent(parent);
    }

    final void addChild(Logger logger) {
        childs.add(logger);
    }

    final void removeChild(Logger child) {
        childs.remove(child);
    }


    /**
     * Gets the name of this logger, {@code null} for anonymous loggers.
     * 
     * @return the name of this logger.
     * @since Android 1.0
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the loaded resource bundle used by this logger to localize logging
     * messages. If the value is {@code null}, the parent's resource bundle will be
     * inherited.
     * 
     * @return the loaded resource bundle used by this logger.
     * @since Android 1.0
     */
    public ResourceBundle getResourceBundle() {
        return this.resBundle;
    }

    /**
     * Gets the name of the loaded resource bundle used by this logger to
     * localize logging messages. If the value is {@code null}, the parent's resource
     * bundle name will be inherited.
     * 
     * @return the name of the loaded resource bundle used by this logger.
     * @since Android 1.0
     */
    public String getResourceBundleName() {
        return this.resBundleName;
    }

    /**
     * This method is for compatibility. Tests written to the reference
     * implementation API imply that the isLoggable() method is not called
     * directly. This behavior is important because subclass may override
     * isLoggable() method, so that affect the result of log methods.
     */
    private boolean internalIsLoggable(Level l) {
        int effectiveLevel = levelIntVal;
        if (effectiveLevel == Level.OFF.intValue()) {
            // always return false if the effective level is off
            return false;
        }
        return l.intValue() >= effectiveLevel;
    }

    /**
     * Determines whether this logger will actually log messages of the
     * specified level. The effective level used to do the determination may be
     * inherited from its parent. The default level is {@code Level.INFO}.
     * 
     * @param l
     *            the level to check.
     * @return {@code true} if this logger will actually log this level,
     *         otherwise {@code false}.
     * @since Android 1.0
     */
    public boolean isLoggable(Level l) {
        return internalIsLoggable(l);
    }

    /*
     * Sets the resource bundle and its name for a supplied LogRecord object.
     * This method first tries to use this logger's resource bundle if any,
     * otherwise try to inherit from this logger's parent, recursively up the
     * namespace. Synchronize to ensure the consistency between resource bundle
     * and its name.
     */
    private void setResourceBundle(LogRecord record) {
        if (null != this.resBundleName) {
            record.setResourceBundle(this.resBundle);
            record.setResourceBundleName(this.resBundleName);
        } else {
            Logger anyParent = this.parent;
            // no need to synchronize here, because if resBundleName
            // is not null, there is no chance to modify it
            while (null != anyParent) {
                if (null != anyParent.resBundleName) {
                    record.setResourceBundle(anyParent.resBundle);
                    record.setResourceBundleName(anyParent.resBundleName);
                    return;
                }
                anyParent = anyParent.parent;
            }
        }
    }

    /**
     * Logs a message indicating that a method has been entered. A log record
     * with log level {@code Level.FINER}, log message "ENTRY", the specified
     * source class name and source method name is submitted for logging.
     * 
     * @param sourceClass
     *            the calling class name.
     * @param sourceMethod
     *            the method name.
     * @since Android 1.0
     */
    public void entering(String sourceClass, String sourceMethod) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, "ENTRY"); //$NON-NLS-1$
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message indicating that a method has been entered. A log record
     * with log level {@code Level.FINER}, log message "ENTRY", the specified
     * source class name, source method name and one parameter is submitted for
     * logging.
     * 
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param param
     *            the parameter for the method call.
     * @since Android 1.0
     */
    public void entering(String sourceClass, String sourceMethod, Object param) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, "ENTRY" + " {0}"); //$NON-NLS-1$ //$NON-NLS-2$
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(new Object[] { param });
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message indicating that a method has been entered. A log record
     * with log level {@code Level.FINER}, log message "ENTRY", the specified
     * source class name, source method name and array of parameters is
     * submitted for logging.
     * 
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param params
     *            an array of parameters for the method call.
     * @since Android 1.0
     */
    public void entering(String sourceClass, String sourceMethod,
            Object[] params) {
        if (internalIsLoggable(Level.FINER)) {
            String msg = "ENTRY"; //$NON-NLS-1$
            if (null != params) {
                StringBuilder msgBuffer = new StringBuilder("ENTRY"); //$NON-NLS-1$
                for (int i = 0; i < params.length; i++) {
                    msgBuffer.append(" {" + i + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                msg = msgBuffer.toString();
            }
            LogRecord record = new LogRecord(Level.FINER, msg);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(params);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message indicating that a method is exited. A log record with log
     * level {@code Level.FINER}, log message "RETURN", the specified source
     * class name and source method name is submitted for logging.
     * 
     * @param sourceClass
     *            the calling class name.
     * @param sourceMethod
     *            the method name.
     * @since Android 1.0
     */
    public void exiting(String sourceClass, String sourceMethod) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, "RETURN"); //$NON-NLS-1$
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message indicating that a method is exited. A log record with log
     * level {@code Level.FINER}, log message "RETURN", the specified source
     * class name, source method name and return value is submitted for logging.
     * 
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param result
     *            the return value of the method call.
     * @since Android 1.0
     */
    public void exiting(String sourceClass, String sourceMethod, Object result) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, "RETURN" + " {0}"); //$NON-NLS-1$ //$NON-NLS-2$
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(new Object[] { result });
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message indicating that an exception is thrown. A log record with
     * log level {@code Level.FINER}, log message "THROW", the specified source
     * class name, source method name and the {@code Throwable} object is
     * submitted for logging.
     * 
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param thrown
     *            the {@code Throwable} object.
     * @since Android 1.0
     */
    public void throwing(String sourceClass, String sourceMethod,
            Throwable thrown) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, "THROW"); //$NON-NLS-1$
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setThrown(thrown);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level {@code Level.SEVERE}; the message is transmitted
     * to all subscribed handlers.
     * 
     * @param msg
     *            the message to log.
     * @since Android 1.0
     */
    public void severe(String msg) {
        if (internalIsLoggable(Level.SEVERE)) {
            LogRecord record = new LogRecord(Level.SEVERE, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level {@code Level.WARNING}; the message is
     * transmitted to all subscribed handlers.
     * 
     * @param msg
     *            the message to log.
     * @since Android 1.0           
     */
    public void warning(String msg) {
        if (internalIsLoggable(Level.WARNING)) {
            LogRecord record = new LogRecord(Level.WARNING, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level {@code Level.INFO}; the message is transmitted
     * to all subscribed handlers.
     * 
     * @param msg
     *            the message to log.
     * @since Android 1.0           
     */
    public void info(String msg) {
        if (internalIsLoggable(Level.INFO)) {
            LogRecord record = new LogRecord(Level.INFO, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level {@code Level.CONFIG}; the message is transmitted
     * to all subscribed handlers.
     * 
     * @param msg
     *            the message to log.
     * @since Android 1.0           
     */
    public void config(String msg) {
        if (internalIsLoggable(Level.CONFIG)) {
            LogRecord record = new LogRecord(Level.CONFIG, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level {@code Level.FINE}; the message is transmitted
     * to all subscribed handlers.
     * 
     * @param msg
     *            the message to log.
     * @since Android 1.0           
     */
    public void fine(String msg) {
        if (internalIsLoggable(Level.FINE)) {
            LogRecord record = new LogRecord(Level.FINE, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level {@code Level.FINER}; the message is transmitted
     * to all subscribed handlers.
     * 
     * @param msg
     *            the message to log.
     * @since Android 1.0           
     */
    public void finer(String msg) {
        if (internalIsLoggable(Level.FINER)) {
            LogRecord record = new LogRecord(Level.FINER, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of level {@code Level.FINEST}; the message is transmitted
     * to all subscribed handlers.
     * 
     * @param msg
     *            the message to log.
     * @since Android 1.0
     */
    public void finest(String msg) {
        if (internalIsLoggable(Level.FINEST)) {
            LogRecord record = new LogRecord(Level.FINEST, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the specified level. The message is transmitted to all
     * subscribed handlers.
     * 
     * @param logLevel
     *            the level of the specified message.
     * @param msg
     *            the message to log.
     * @since Android 1.0
     */
    public void log(Level logLevel, String msg) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the specified level with the supplied parameter. The
     * message is then transmitted to all subscribed handlers.
     * 
     * @param logLevel
     *            the level of the given message.
     * @param msg
     *            the message to log.
     * @param param
     *            the parameter associated with the event that is logged.
     * @since Android 1.0
     */
    public void log(Level logLevel, String msg, Object param) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setParameters(new Object[] { param });
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the specified level with the supplied parameter array.
     * The message is then transmitted to all subscribed handlers.
     * 
     * @param logLevel
     *            the level of the given message
     * @param msg
     *            the message to log.
     * @param params
     *            the parameter array associated with the event that is logged.
     * @since Android 1.0
     */
    public void log(Level logLevel, String msg, Object[] params) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setParameters(params);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the specified level with the supplied {@code Throwable}
     * object. The message is then transmitted to all subscribed handlers.
     * 
     * @param logLevel
     *            the level of the given message.
     * @param msg
     *            the message to log.
     * @param thrown
     *            the {@code Throwable} object associated with the event that is
     *            logged.
     * @since Android 1.0
     */
    public void log(Level logLevel, String msg, Throwable thrown) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setThrown(thrown);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a given log record. Only records with a logging level that is equal
     * or greater than this logger's level will be submitted to this logger's
     * handlers for logging. If {@code getUseParentHandlers()} returns {@code
     * true}, the log record will also be submitted to the handlers of this
     * logger's parent, potentially recursively up the namespace.
     * <p>
     * Since all other log methods call this method to actually perform the
     * logging action, subclasses of this class can override this method to
     * catch all logging activities.
     * </p>
     * 
     * @param record
     *            the log record to be logged.
     * @since Android 1.0
     */
    public void log(LogRecord record) {
        if (internalIsLoggable(record.getLevel())) {
            // apply the filter if any
            Filter f = filter;
            if (null != f && !f.isLoggable(record)) {
                return;
            }
            initHandler();
            /*
             * call the handlers of this logger, throw any exception that
             * occurs
             */
            Handler[] allHandlers = getHandlers();
            for (Handler element : allHandlers) {
                element.publish(record);
            }
            // call the parent's handlers if set useParentHandlers
            Logger temp = this;
            Logger theParent = temp.parent;
            while (theParent != null && temp.getUseParentHandlers()) {
                Handler[] ha = theParent.getHandlers();
                for (Handler element : ha) {
                    element.publish(record);
                }
                temp = theParent;
                theParent = temp.parent;
            }
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name.
     * 
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param msg
     *            the message to be logged.
     * @since Android 1.0
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and parameter.
     * 
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param msg
     *            the message to be logged
     * @param param
     *            the parameter associated with the event that is logged.
     * @since Android 1.0
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg, Object param) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(new Object[] { param });
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and parameter array.
     * 
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param msg
     *            the message to be logged.
     * @param params
     *            the parameter array associated with the event that is logged.
     * @since Android 1.0
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg, Object[] params) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(params);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and {@code Throwable} object.
     * 
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param msg
     *            the message to be logged.
     * @param thrown
     *            the {@code Throwable} object.
     * @since Android 1.0
     */
    public void logp(Level logLevel, String sourceClass, String sourceMethod,
            String msg, Throwable thrown) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setThrown(thrown);
            setResourceBundle(record);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name
     * and source method name, using the given resource bundle to localize the
     * message. If {@code bundleName} is null, the empty string or not valid then
     * the message is not localized.
     * 
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param bundleName
     *            the name of the resource bundle used to localize the message.
     * @param msg
     *            the message to be logged.
     * @since Android 1.0
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            if (null != bundleName) {
                try {
                    record.setResourceBundle(loadResourceBundle(bundleName));
                } catch (MissingResourceException e) {
                    // ignore
                }
                record.setResourceBundleName(bundleName);
            }
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and parameter, using the given resource bundle to
     * localize the message. If {@code bundleName} is null, the empty string
     * or not valid then the message is not localized.
     * 
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param bundleName
     *            the name of the resource bundle used to localize the message.
     * @param msg
     *            the message to be logged.
     * @param param
     *            the parameter associated with the event that is logged.
     * @since Android 1.0
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg, Object param) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            if (null != bundleName) {
                try {
                    record.setResourceBundle(loadResourceBundle(bundleName));
                } catch (MissingResourceException e) {
                    // ignore
                }
                record.setResourceBundleName(bundleName);
            }
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(new Object[] { param });
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and parameter array, using the given resource bundle
     * to localize the message. If {@code bundleName} is null, the empty string
     * or not valid then the message is not localized.
     * 
     * @param logLevel
     *            the level of the given message.
     * @param sourceClass
     *            the source class name.
     * @param sourceMethod
     *            the source method name.
     * @param bundleName
     *            the name of the resource bundle used to localize the message.
     * @param msg
     *            the message to be logged.
     * @param params
     *            the parameter array associated with the event that is logged.
     * @since Android 1.0
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg, Object[] params) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            if (null != bundleName) {
                try {
                    record.setResourceBundle(loadResourceBundle(bundleName));
                } catch (MissingResourceException e) {
                    // ignore
                }
                record.setResourceBundleName(bundleName);
            }
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setParameters(params);
            log(record);
        }
    }

    /**
     * Logs a message of the given level with the specified source class name,
     * source method name and {@code Throwable} object, using the given resource
     * bundle to localize the message. If {@code bundleName} is null, the empty
     * string or not valid then the message is not localized.
     * 
     * @param logLevel
     *            the level of the given message
     * @param sourceClass
     *            the source class name
     * @param sourceMethod
     *            the source method name
     * @param bundleName
     *            the name of the resource bundle used to localize the message.
     * @param msg
     *            the message to be logged.
     * @param thrown
     *            the {@code Throwable} object.
     * @since Android 1.0
     */
    public void logrb(Level logLevel, String sourceClass, String sourceMethod,
            String bundleName, String msg, Throwable thrown) {
        if (internalIsLoggable(logLevel)) {
            LogRecord record = new LogRecord(logLevel, msg);
            if (null != bundleName) {
                try {
                    record.setResourceBundle(loadResourceBundle(bundleName));
                } catch (MissingResourceException e) {
                    // ignore
                }
                record.setResourceBundleName(bundleName);
            }
            record.setLoggerName(this.name);
            record.setSourceClassName(sourceClass);
            record.setSourceMethodName(sourceMethod);
            record.setThrown(thrown);
            log(record);
        }
    }

    /*
     * This security manager is used to access the class context.
     */
    static class PrivateSecurityManager extends SecurityManager {
        public Class<?>[] privateGetClassContext() {
            return super.getClassContext();
        }
    }

    void setManager(LogManager manager) {
        if(this.manager != manager){
            this.manager = manager;
            handlerInited  = false;
        }
        //init level here, but let handlers be for lazy loading
        String configedLevel = manager.getProperty(name+ ".level"); //$NON-NLS-1$
        if (null != configedLevel) {
            try {
                setLevel(Level.parse(configedLevel));
            } catch (IllegalArgumentException e) {
                //ignore
            }
        }        
    }

    synchronized void reset() {
        levelObjVal = null;
        levelIntVal = Level.INFO.intValue();
        if(handlers != null){
            for (Handler element : handlers) {
                // close all handlers, when unknown exceptions happen,
                // ignore them and go on
                try {
                    element.close();
                } catch (Exception e) {
                    // Ignored.
                }
            }
            handlers.clear();
        }
        handlerInited = false;
    }
}

