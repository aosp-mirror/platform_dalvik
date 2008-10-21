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

package java.nio.channels.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;

import org.apache.harmony.luni.platform.Platform;
import org.apache.harmony.nio.internal.SelectorProviderImpl;

/**
 * Provider for nio selector and selectable channel.
 * <p>
 * The provider can be got by system property or the configuration file in a jar
 * file, if not, the system default provider will return. The main function of
 * this class is to return the instance of implementation class of
 * <code>DatagramChannel</code>, <code>Pipe</code>, <code>Selector</code> ,
 * <code>ServerSocketChannel</code>, and <code>SocketChannel</code>. All
 * the methods of this class are multi-thread safe.
 * </p>
 * 
 */
public abstract class SelectorProvider extends Object {

    private static final String SYMBOL_COMMENT = "#"; //$NON-NLS-1$

    private static final String PROVIDER_IN_SYSTEM_PROPERTY = "java.nio.channels.spi.SelectorProvider"; //$NON-NLS-1$

    private static final String PROVIDER_IN_JAR_RESOURCE = "META-INF/services/java.nio.channels.spi.SelectorProvider"; //$NON-NLS-1$

    private static SelectorProvider provider = null;
    
    private static Channel inheritedChannel; 

    /**
     * Constructor for this class.
     * 
     * @throws SecurityException
     *             If there is a security manager, and it denies
     *             RuntimePermission("selectorProvider").
     */
    protected SelectorProvider() {
        super();
        if (null != System.getSecurityManager()) {
            System.getSecurityManager().checkPermission(
                    new RuntimePermission("selectorProvider")); //$NON-NLS-1$
        }
    }

    /**
     * Get the provider by following steps in the first calling.
     * <p>
     * <ul>
     * <li> If the system property "java.nio.channels.spi.SelectorProvider" is
     * set, the value of this property is the class name of the return provider.
     * </li>
     * <li>If there is a provider-configuration file named
     * "java.nio.channels.spi.SelectorProvider" in META-INF/services of some jar
     * file valid in the system class loader, the first class name is the return
     * provider's class name. </li>
     * <li> Otherwise, a system default provider will be returned. </li>
     * </ul>
     * </p>
     * 
     * @return The provider.
     */
    synchronized public static SelectorProvider provider() {
        if (null == provider) {
            provider = loadProviderByProperty();
            if (null == provider) {
                provider = loadProviderByJar();
            }
            if (null == provider) {
                provider = AccessController
                        .doPrivileged(new PrivilegedAction<SelectorProvider>() {
                            public SelectorProvider run() {
                                return new SelectorProviderImpl();
                            }
                        });
            }
        }
        return provider;
    }

    /*
     * load the provider in the jar file of class path.
     */
    static SelectorProvider loadProviderByJar() {
        Enumeration<URL> enumeration = null;

        ClassLoader classLoader = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return ClassLoader.getSystemClassLoader();
                    }
                });
        try {
            enumeration = classLoader.getResources(PROVIDER_IN_JAR_RESOURCE);
        } catch (IOException e) {
            throw new Error(e);
        }
        if (null == enumeration) {
            return null;
        }
        // for every jar, read until we find the provider name.
        while (enumeration.hasMoreElements()) {
            BufferedReader br = null;
            String className = null;
            try {
                // BEGIN android-modified
                br = new BufferedReader(
                        new InputStreamReader(
                                (enumeration.nextElement()).openStream()),
                        8192);
                // END android-modified
            } catch (Exception e) {
                continue;
            }
            try {
                // only the first class is loaded ,as spec says, not the same as
                // we do before.
                while ((className = br.readLine()) != null) {
                    className = className.trim();
                    int siteComment = className.indexOf(SYMBOL_COMMENT);
                    className = (-1 == siteComment) ? className : className
                            .substring(0, siteComment);
                    if (0 < className.length()) {
                        return (SelectorProvider) classLoader.loadClass(
                                className).newInstance();                  
                    }
                }
            } catch (Exception e) {
                throw new Error(e);
            // BEGIN android-added
            // copied from a newer version of harmony
            } finally {
                try {
                    br.close();
                } catch (IOException ioe) {
                    // Ignore
                }
            // END android-added
            }
        }
        return null;
    }

    /*
     * load by system property.
     */
    static SelectorProvider loadProviderByProperty() {
        return AccessController.doPrivileged(
                new PrivilegedAction<SelectorProvider>() {
                    public SelectorProvider run() {
                        try {
                            final String className =
                                System.getProperty(PROVIDER_IN_SYSTEM_PROPERTY);
                            if (null != className) {
                                Class<?> spClass = ClassLoader
                                        .getSystemClassLoader().loadClass(
                                                className);
                                return (SelectorProvider)spClass.newInstance();
                            }
                            return null;
                        } catch (Exception e) {
                            throw new Error(e);
                        }
                    }
                });
    }

    /**
     * Create a new open <code>DatagramChannel</code>.
     * 
     * @return The channel.
     * @throws IOException
     *             If some I/O exception occurred.
     */
    public abstract DatagramChannel openDatagramChannel() throws IOException;

    /**
     * Create a new <code>Pipe</code>.
     * 
     * @return The pipe.
     * @throws IOException
     *             If some I/O exception occurred.
     */
    public abstract Pipe openPipe() throws IOException;

    /**
     * Create a new selector.
     * 
     * @return The selector.
     * @throws IOException
     *             If some I/O exception occurred.
     */
    public abstract AbstractSelector openSelector() throws IOException;

    /**
     * Create a new open <code>ServerSocketChannel</code>.
     * 
     * @return The channel.
     * @throws IOException
     *             If some I/O exception occurred.
     */
    public abstract ServerSocketChannel openServerSocketChannel()
            throws IOException;

    /**
     * Create a new open <code>SocketChannel</code>.
     * 
     * @return The channel.
     * @throws IOException
     *             If some I/O exception occurred.
     */
    public abstract SocketChannel openSocketChannel() throws IOException;

    /**
     * Answer the channel inherited from the instance which created this JVM.
     * 
     * @return The channel.
     * @throws IOException
     *             If some I/O exception occurred.
     * @throws SecurityException
     *             If there is a security manager, and it denies
     *             RuntimePermission("selectorProvider").
     */
    public Channel inheritedChannel() throws IOException {
        if (null == inheritedChannel) {
            inheritedChannel = Platform.getNetworkSystem().inheritedChannel();
        }
        return inheritedChannel;
    }
}
