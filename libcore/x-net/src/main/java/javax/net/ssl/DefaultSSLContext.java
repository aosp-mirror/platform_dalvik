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

import java.io.FileInputStream;
import java.security.AccessController;
import java.security.Provider;
import java.security.Security;
import java.security.KeyStore;
import java.util.Iterator;

import org.apache.harmony.security.fortress.Engine;
import org.apache.harmony.security.fortress.Services;


/**
 * Support class for this package.
 *  
 */

class DefaultSSLContext {
    private static SSLContext defaultSSLContext;

    public static SSLContext getContext() {
        if (defaultSSLContext == null) {
            defaultSSLContext = AccessController
                    .doPrivileged(new java.security.PrivilegedAction<SSLContext>() {
                        public SSLContext run() {
                            return findDefault();
                        }
                    });
        }
        return defaultSSLContext;
    }

    private static SSLContext findDefault() {
        // FIXME EXPORT CONTROL
        Provider.Service service;
        for (Iterator it1 = Services.getProvidersList().iterator(); it1
                .hasNext();) {
            service = Engine.door.getService((Provider) it1.next(),
                    "SSLContext");
            if (service != null) {
                try {
                    SSLContext con = new ContextImpl(
                            (SSLContextSpi) service.newInstance(null),
                            service.getProvider(), 
                            service.getAlgorithm());

 //TODO javax.net.ssl.keyStoreProvider, javax.net.ssl.trustStoreProvider system property
                    // find KeyStore, KeyManagers
                    KeyManager[] keyManagers = null;
                    KeyStore ks = KeyStore.getInstance(KeyStore
                            .getDefaultType());
                    String keystore = System
                            .getProperty("javax.net.ssl.keyStore");
                    String keystorepwd = System
                            .getProperty("javax.net.ssl.keyStorePassword");
                    char[] pwd = null;
                    if (keystorepwd != null) {
                        pwd = keystorepwd.toCharArray();
                    }
                    if (keystore != null) {
                        FileInputStream fis = new java.io.FileInputStream(
                                keystore);
                        ks.load(fis, pwd);
                        fis.close();

                        KeyManagerFactory kmf;
                        String kmfAlg = Security
                                .getProperty("ssl.KeyManagerFactory.algorithm");
                        if (kmfAlg == null) {
                            kmfAlg = "SunX509";
                        }
                        kmf = KeyManagerFactory.getInstance(kmfAlg);
                        kmf.init(ks, pwd);
                        keyManagers = kmf.getKeyManagers();
                    }

                    // find TrustStore, TrustManagers
                    TrustManager[] trustManagers = null;
                    keystore = System.getProperty("javax.net.ssl.trustStore");
                    keystorepwd = System
                            .getProperty("javax.net.ssl.trustStorePassword");
                    pwd = null;
                    if (keystorepwd != null) {
                        pwd = keystorepwd.toCharArray();
                    }
                    //TODO Defaults: jssecacerts; cacerts
                    if (keystore != null) {
                        FileInputStream fis = new java.io.FileInputStream(
                                keystore);
                        ks.load(fis, pwd);
                        fis.close();
                        TrustManagerFactory tmf;
                        String tmfAlg = Security
                                .getProperty("ssl.TrustManagerFactory.algorithm");
                        if (tmfAlg == null) {
                            tmfAlg = "PKIX";
                        }
                        tmf = TrustManagerFactory.getInstance(tmfAlg);
                        tmf.init(ks);
                        trustManagers = tmf.getTrustManagers();
                    }

                    con.init(keyManagers, trustManagers, null);
                    return con;
                } catch (Exception e) {
                    // e.printStackTrace();
                    // ignore and try another
                }
            }
        }
        return null;
    }
}
