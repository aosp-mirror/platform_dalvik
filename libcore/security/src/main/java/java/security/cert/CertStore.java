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

package java.security.cert;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Collection;

import org.apache.harmony.security.fortress.Engine;
import org.apache.harmony.security.internal.nls.Messages;


/**
 * @com.intel.drl.spec_ref
 * 
 */

public class CertStore {

    // Store spi implementation service name
    private static final String SERVICE = "CertStore"; //$NON-NLS-1$

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    // Store default property name
    private static final String PROPERTYNAME = "certstore.type"; //$NON-NLS-1$

    // Default value of CertStore type. It returns if certpathbuild.type
    // property is not defined in java.security file
    private static final String DEFAULTPROPERTY = "LDAP"; //$NON-NLS-1$

    // Store used provider
    private final Provider provider;

    // Store CertStoreSpi implementation
    private final CertStoreSpi spiImpl;

    // Store used type
    private final String type;

    // Store used parameters
    private final CertStoreParameters certStoreParams;

    /**
     * @com.intel.drl.spec_ref
     */
    protected CertStore(CertStoreSpi storeSpi, Provider provider, String type,
            CertStoreParameters params) {
        this.provider = provider;
        this.type = type;
        this.spiImpl = storeSpi;
        this.certStoreParams = params;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if type is null (instead of
     * NoSuchAlgorithmException as in 1.4 release)
     */
    public static CertStore getInstance(String type, CertStoreParameters params)
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.07")); //$NON-NLS-1$
        }
        try {
            synchronized (engine) {
                engine.getInstance(type, params);
                return new CertStore((CertStoreSpi) engine.spi, engine.provider,
                        type, params);
            }
        } catch (NoSuchAlgorithmException e) {
            Throwable th = e.getCause();
            if (th == null) {
                throw e;
            } else {
                throw new InvalidAlgorithmParameterException(e.getMessage(), th);
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if type is null (instead of
     * NoSuchAlgorithmException as in 1.4 release)
     * 
     * FIXME: IllegalArgumentException when provider is empty
     */
    public static CertStore getInstance(String type,
            CertStoreParameters params, String provider)
            throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.02")); //$NON-NLS-1$
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(type, params, impProvider);
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if type is null (instead of
     * NoSuchAlgorithmException as in 1.4 release)
     */
    public static CertStore getInstance(String type,
            CertStoreParameters params, Provider provider)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); //$NON-NLS-1$
        }
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.07")); //$NON-NLS-1$
        }
        try {
            synchronized (engine) {
                engine.getInstance(type, provider, params);
                return new CertStore((CertStoreSpi) engine.spi, provider, type,
                        params);
            }
        } catch (NoSuchAlgorithmException e) {
            Throwable th = e.getCause();
            if (th == null) {
                throw e;
            } else {
                throw new InvalidAlgorithmParameterException(e.getMessage(), th);
            }
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final String getType() {
        return type;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final CertStoreParameters getCertStoreParameters() {
        if (certStoreParams == null) {
            return null;
        } else {
            return (CertStoreParameters) certStoreParams.clone();
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final Collection<? extends Certificate> getCertificates(CertSelector selector)
            throws CertStoreException {
        return spiImpl.engineGetCertificates(selector);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final Collection<? extends CRL> getCRLs(CRLSelector selector)
            throws CertStoreException {
        return spiImpl.engineGetCRLs(selector);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static final String getDefaultType() {
        String defaultType = AccessController
                .doPrivileged(new java.security.PrivilegedAction<String>() {
                    public String run() {
                        return Security.getProperty(PROPERTYNAME);
                    }
                });
        return (defaultType == null ? DEFAULTPROPERTY : defaultType);
    }
}
