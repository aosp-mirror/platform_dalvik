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

/**
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package javax.net.ssl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.AccessController;
import java.security.Security;

import javax.net.SocketFactory;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public abstract class SSLSocketFactory extends SocketFactory {
    // FIXME EXPORT CONTROL

    // The default SSL socket factory
    private static SocketFactory defaultSocketFactory;

    private static String defaultName;

    public SSLSocketFactory() {
        super();
    }

    public static SocketFactory getDefault() {
        if (defaultSocketFactory != null) {
            log("SSLSocketFactory", "Using factory " + defaultSocketFactory);
            return defaultSocketFactory;
        }
        if (defaultName == null) {
            AccessController.doPrivileged(new java.security.PrivilegedAction(){
                public Object run() {
                    defaultName = Security.getProperty("ssl.SocketFactory.provider");
                    if (defaultName != null) {    
                        ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        if (cl == null) {
                            cl = ClassLoader.getSystemClassLoader();
                        }
                        try {
                            defaultSocketFactory = (SocketFactory) Class.forName(
                                    defaultName, true, cl).newInstance();
                         } catch (Exception e) {
                            return e;
                        }
                    }
                    return null;
                }
            });
        }

        if (defaultSocketFactory == null) {
            // Try to find in providers
            SSLContext context = DefaultSSLContext.getContext();
            if (context != null) {
                defaultSocketFactory = context.getSocketFactory();
            }
        }
        if (defaultSocketFactory == null) {
            defaultSocketFactory = new DefaultSSLSocketFactory("No SSLSocketFactory installed");
        }

        log("SSLSocketFactory", "Using factory " + defaultSocketFactory);
        return defaultSocketFactory;
    }

    @SuppressWarnings("unchecked")
    private static void log(String tag, String msg) {
        try {
            Class clazz = Class.forName("android.util.Log");
            Method method = clazz.getMethod("d", new Class[] { String.class, String.class });
            method.invoke(null, new Object[] { tag, msg });
        } catch (Exception ex) {
            // Silently ignore.
        }
    }
    
    public abstract String[] getDefaultCipherSuites();

    public abstract String[] getSupportedCipherSuites();

    public abstract Socket createSocket(Socket s, String host, int port,
            boolean autoClose) throws IOException;

}
