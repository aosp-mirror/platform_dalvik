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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package javax.net.ssl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import org.apache.harmony.security.fortress.Engine;


/**
 * @com.intel.drl.spec_ref
 * 
 */

public class SSLContext {
    // StoreSSLContext service name
    private static final String SERVICE = "SSLContext";

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    // Storeused provider
    private final Provider provider;

    // Storeused SSLContextSpi implementation
    private final SSLContextSpi spiImpl;

    // Storeused protocol
    private final String protocol;

    /*
     * @com.intel.drl.spec_ref
     *  
     */
    protected SSLContext(SSLContextSpi contextSpi, Provider provider,
            String protocol) {
        this.provider = provider;
        this.protocol = protocol;
        this.spiImpl = contextSpi;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if protocol is null (instead of
     * NoSuchAlgorithmException as in 1.4 release)
     */
    public static SSLContext getInstance(String protocol)
            throws NoSuchAlgorithmException {
        if (protocol == null) {
            throw new NullPointerException("protocol is null");
        }
        synchronized (engine) {
            engine.getInstance(protocol, null);
            return new SSLContext((SSLContextSpi) engine.spi, engine.provider,
                    protocol);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if protocol is null (instead of
     * NoSuchAlgorithmException as in 1.4 release)
     */
    public static SSLContext getInstance(String protocol, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (provider.length() == 0) {
            throw new IllegalArgumentException("Provider is empty");
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(protocol, impProvider);
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if protocol is null (instead of
     * NoSuchAlgorithmException as in 1.4 release)
     */
    public static SSLContext getInstance(String protocol, Provider provider)
            throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("provider is null");
        }
        if (protocol == null) {
            throw new NullPointerException("protocol is null");
        }
        synchronized (engine) {
            engine.getInstance(protocol, provider, null);
            return new SSLContext((SSLContextSpi) engine.spi, provider, protocol);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final String getProtocol() {
        return protocol;
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * FIXME: check what exception will be thrown when parameters are null
     */
    public final void init(KeyManager[] km, TrustManager[] tm, SecureRandom sr)
            throws KeyManagementException {
        spiImpl.engineInit(km, tm, sr);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final SSLSocketFactory getSocketFactory() {
        return spiImpl.engineGetSocketFactory();
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final SSLServerSocketFactory getServerSocketFactory() {
        return spiImpl.engineGetServerSocketFactory();
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final SSLEngine createSSLEngine() {
        return spiImpl.engineCreateSSLEngine();
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final SSLEngine createSSLEngine(String peerHost, int peerPort) {
        return spiImpl.engineCreateSSLEngine(peerHost, peerPort);
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final SSLSessionContext getServerSessionContext() {
        return spiImpl.engineGetServerSessionContext();
    }

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    public final SSLSessionContext getClientSessionContext() {
        return spiImpl.engineGetClientSessionContext();
    }
}