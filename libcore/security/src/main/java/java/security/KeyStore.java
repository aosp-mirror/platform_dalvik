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

package java.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.security.auth.callback.CallbackHandler;

import org.apache.harmony.security.fortress.Engine;
import org.apache.harmony.security.internal.nls.Messages;


public class KeyStore {

    // Store KeyStore SERVICE name
    private static final String SERVICE = "KeyStore"; //$NON-NLS-1$

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    //  Store KeyStore property name
    private static final String PROPERTYNAME = "keystore.type"; //$NON-NLS-1$

    //  Store default KeyStore type
    private static final String DEFAULT_KEYSTORE_TYPE = "jks"; //$NON-NLS-1$

    // Message to report about non-initialized key store object
    // BEGIN android-changed
    private static String NOTINITKEYSTORE;
    // END android-changed

    // Store KeyStore state (initialized or not)
    private boolean isInit;

    // Store used KeyStoreSpi
    private final KeyStoreSpi implSpi;

    // Store used provider
    private final Provider provider;

    // Store used type
    private final String type;

    protected KeyStore(KeyStoreSpi keyStoreSpi, Provider provider, String type) {
        this.type = type;
        this.provider = provider;
        this.implSpi = keyStoreSpi;
        isInit = false;
    }

    // BEGIN android-added
    /**
     * Throws the standard "keystore not initialized" exception.
     */
    private static void throwNotInitialized() throws KeyStoreException {
        if (NOTINITKEYSTORE == null) {
            NOTINITKEYSTORE = Messages.getString("security.4F"); //$NON-NLS-1$
        }
        throw new KeyStoreException(NOTINITKEYSTORE);
    }
    // END android-added
    
    /**
     * @throws NullPointerException if type is null
     */
    public static KeyStore getInstance(String type) throws KeyStoreException {
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.07")); //$NON-NLS-1$
        }
        synchronized (engine) {
            try {
                engine.getInstance(type, null);
                return new KeyStore((KeyStoreSpi) engine.spi, engine.provider, type);
            } catch (NoSuchAlgorithmException e) {
                throw new KeyStoreException(e.getMessage());
            }
        }
    }

    /**
     * 
     * 
     * @throws NullPointerException if type is null (instead of
     * NoSuchAlgorithmException) as in 1.4 release
     */
    public static KeyStore getInstance(String type, String provider)
            throws KeyStoreException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.02")); //$NON-NLS-1$
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        try {
            return getInstance(type, impProvider);
        } catch (Exception e) {
            throw new KeyStoreException(e.getMessage(), e);
        }
    }

    /**
     * 
     * 
     * throws NullPointerException if type is null (instead of
     * NoSuchAlgorithmException) as in 1.4 release
     */
    public static KeyStore getInstance(String type, Provider provider)
            throws KeyStoreException {
        // check parameters
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); //$NON-NLS-1$
        }
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.07")); //$NON-NLS-1$
        }
        // return KeyStore instance
        synchronized (engine) {
            try {
                engine.getInstance(type, provider, null);
                return new KeyStore((KeyStoreSpi) engine.spi, provider, type);
            } catch (Exception e) {
            // override exception
                throw new KeyStoreException(e.getMessage());
            }
        }
    }

    /**
     * 
     *  
     */
    public static final String getDefaultType() {
        String dt = AccessController.doPrivileged(
                new PrivilegedAction<String>() {
                    public String run() {
                        return Security.getProperty(PROPERTYNAME);
                    }
                }
            );
        return (dt == null ? DEFAULT_KEYSTORE_TYPE : dt);
    }

    /**
     * 
     *  
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * 
     *  
     */
    public final String getType() {
        return type;
    }

    /**
     * 
     *  
     */
    public final Key getKey(String alias, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        return implSpi.engineGetKey(alias, password);
    }

    /**
     * 
     *  
     */
    public final Certificate[] getCertificateChain(String alias)
            throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        return implSpi.engineGetCertificateChain(alias);
    }

    /**
     * 
     *  
     */
    public final Certificate getCertificate(String alias)
            throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        return implSpi.engineGetCertificate(alias);
    }

    /**
     * 
     *  
     */
    public final Date getCreationDate(String alias) throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        return implSpi.engineGetCreationDate(alias);
    }

    /**
     * 
     * 
     * 1.4.2 and 1.5 releases throw unspecified NullPointerException -
     * when alias is null IllegalArgumentException - when password is null
     * IllegalArgumentException - when key is instance of PrivateKey and chain
     * is null or empty
     */
    public final void setKeyEntry(String alias, Key key, char[] password,
            Certificate[] chain) throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }

        // Certificate chain is required for PrivateKey
        if (null != key && key instanceof PrivateKey
                && (chain == null || chain.length == 0)) {
            throw new IllegalArgumentException(Messages
                    .getString("security.52")); //$NON-NLS-1$
        }
        implSpi.engineSetKeyEntry(alias, key, password, chain);
    }

    /**
     * 
     *  
     */
    public final void setKeyEntry(String alias, byte[] key, Certificate[] chain)
            throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        implSpi.engineSetKeyEntry(alias, key, chain);
    }

    /**
     * 
     * 
     * 1.4.2 and 1.5 releases throw unspecified NullPointerException
     * when alias is null
     */
    public final void setCertificateEntry(String alias, Certificate cert)
            throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        implSpi.engineSetCertificateEntry(alias, cert);
    }

    /**
     * 
     * 
     * 1.4.2 and 1.5 releases throw NullPointerException when alias is null
     */
    public final void deleteEntry(String alias) throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        if (alias == null) {
            throw new NullPointerException(Messages.getString("security.3F")); //$NON-NLS-1$
        }
        implSpi.engineDeleteEntry(alias);
    }

    /**
     * 
     */
    public final Enumeration<String> aliases() throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        return implSpi.engineAliases();
    }

    /**
     * 
     * 
     * 1.4.2 and 1.5 releases throw unspecified NullPointerException when
     * alias is null
     */
    public final boolean containsAlias(String alias) throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        if (alias == null) {
            throw new NullPointerException(Messages.getString("security.3F")); //$NON-NLS-1$
        }
        return implSpi.engineContainsAlias(alias);
    }

    /**
     * 
     *  
     */
    public final int size() throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        return implSpi.engineSize();
    }

    /**
     * 
     * 
     * jdk1.4.2 and 1.5 releases throw unspecified NullPointerException
     * when alias is null
     */
    public final boolean isKeyEntry(String alias) throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        if (alias == null) {
            throw new NullPointerException(Messages.getString("security.3F")); //$NON-NLS-1$
        }
        return implSpi.engineIsKeyEntry(alias);
    }

    /**
     * 
     * 
     * jdk1.4.2 and 1.5 releases throw unspecified NullPointerException
     * when alias is null
     */
    public final boolean isCertificateEntry(String alias)
            throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        if (alias == null) {
            throw new NullPointerException(Messages.getString("security.3F")); //$NON-NLS-1$
        }
        return implSpi.engineIsCertificateEntry(alias);
    }

    /**
     * 
     *  
     */
    public final String getCertificateAlias(Certificate cert)
            throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        return implSpi.engineGetCertificateAlias(cert);
    }

    /**
     * 
     * 
     * throws IOException when stream or password is null
     */
    public final void store(OutputStream stream, char[] password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        if (stream == null) {
            throw new IOException(Messages.getString("security.51")); //$NON-NLS-1$
        }
        if (password == null) {
            throw new IOException(Messages.getString("security.50")); //$NON-NLS-1$
        }
        implSpi.engineStore(stream, password);
    }

    /**
     * 
     *  
     */
    public final void store(LoadStoreParameter param) throws KeyStoreException,
            IOException, NoSuchAlgorithmException, CertificateException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        implSpi.engineStore(param);
    }

    /**
     * 
     *  
     */
    public final void load(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        implSpi.engineLoad(stream, password);
        isInit = true;
    }

    /**
     * 
     *  
     */
    public final void load(LoadStoreParameter param) throws IOException,
            NoSuchAlgorithmException, CertificateException {
        implSpi.engineLoad(param);
        isInit = true;
    }

    /**
     * 
     *  
     */
    public final Entry getEntry(String alias, ProtectionParameter param)
            throws NoSuchAlgorithmException, UnrecoverableEntryException,
            KeyStoreException {
        if (alias == null) {
            throw new NullPointerException(Messages.getString("security.3F")); //$NON-NLS-1$
        }
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        return implSpi.engineGetEntry(alias, param);
    }

    /**
     * 
     * 
     * 1.5 release throws unspecified NullPointerException when alias or
     * entry is null
     */
    public final void setEntry(String alias, Entry entry,
            ProtectionParameter param) throws KeyStoreException {
        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        if (alias == null) {
            throw new NullPointerException(Messages.getString("security.3F")); //$NON-NLS-1$
        }
        if (entry == null) {
            throw new NullPointerException(Messages.getString("security.39")); //$NON-NLS-1$
        }
        implSpi.engineSetEntry(alias, entry, param);
    }

    /**
     * 
     */
    public final boolean entryInstanceOf(String alias, 
            Class<? extends KeyStore.Entry> entryClass)
            throws KeyStoreException {
        if (alias == null) {
            throw new NullPointerException(Messages.getString("security.3F")); //$NON-NLS-1$
        }
        if (entryClass == null) {
            throw new NullPointerException(Messages.getString("security.40")); //$NON-NLS-1$
        }

        if (!isInit) {
            // BEGIN android-changed
            throwNotInitialized();
            // END android-changed
        }
        return implSpi.engineEntryInstanceOf(alias, entryClass);
    }

    /**
     * 
     * 
     * 
     */
    public abstract static class Builder {
        /**
         * 
         *  
         */
        protected Builder() {
        }

        /**
         * 
         *  
         */
        public abstract KeyStore getKeyStore() throws KeyStoreException;

        /**
         * 
         *  
         */
        public abstract ProtectionParameter getProtectionParameter(String alise)
                throws KeyStoreException;

        /**
         * 
         *  
         */
        public static Builder newInstance(KeyStore keyStore,
                ProtectionParameter protectionParameter) {
            if (keyStore == null) {
                throw new NullPointerException(Messages.getString("security.41")); //$NON-NLS-1$
            }
            if (protectionParameter == null) {
                throw new NullPointerException(Messages.getString("security.42")); //$NON-NLS-1$
            }

            if (!keyStore.isInit) {
                throw new IllegalArgumentException(NOTINITKEYSTORE);
            }
            return new BuilderImpl(keyStore, protectionParameter,
                    null, null, null, null);
        }

        /**
         * 
         *  
         */
        public static Builder newInstance(String type, Provider provider,
                File file, ProtectionParameter protectionParameter) {
            // check null parameters
            if (type == null) {
                throw new NullPointerException(Messages.getString("security.07")); //$NON-NLS-1$
            }
            if (protectionParameter == null) {
                throw new NullPointerException(Messages.getString("security.42")); //$NON-NLS-1$
            }
            if (file == null) {
                throw new NullPointerException(Messages.getString("security.43")); //$NON-NLS-1$
            }
            // protection parameter should be PasswordProtection or
            // CallbackHandlerProtection
            if (!(protectionParameter instanceof PasswordProtection)
                    && !(protectionParameter instanceof CallbackHandlerProtection)) {
                throw new IllegalArgumentException(Messages.getString("security.35")); //$NON-NLS-1$
            }
            // check file parameter
            if (!file.exists()) {
                throw new IllegalArgumentException(Messages.getString("security.44", file.getName())); //$NON-NLS-1$
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException(Messages.getString("security.45", file.getName())); //$NON-NLS-1$
            }
            // create new instance
            return new BuilderImpl(null, protectionParameter, file,
                    type, provider, AccessController.getContext());
        }

        /**
         * 
         *  
         */
        public static Builder newInstance(String type, Provider provider,
                ProtectionParameter protectionParameter) {
            if (type == null) {
                throw new NullPointerException(Messages.getString("security.07")); //$NON-NLS-1$
            }
            if (protectionParameter == null) {
                throw new NullPointerException(Messages.getString("security.42")); //$NON-NLS-1$
            }
            return new BuilderImpl(null, protectionParameter, null,
                    type, provider, AccessController.getContext());
        }

        /*
         * This class is implementation of abstract class KeyStore.Builder
         * 
         * @author Vera Petrashkova
         * 
         */
        private static class BuilderImpl extends Builder {
            // Store used KeyStore
            private KeyStore keyStore;

            // Store used ProtectionParameter
            private ProtectionParameter protParameter;

            // Store used KeyStore type
            private final String typeForKeyStore;

            // Store used KeyStore provider
            private final Provider providerForKeyStore;

            // Store used file for KeyStore loading
            private final File fileForLoad;

            // Store getKeyStore method was invoked or not for KeyStoreBuilder
            private boolean isGetKeyStore = false;

            // Store last Exception in getKeyStore()
            private KeyStoreException lastException;

            // Store AccessControlContext which is used in getKeyStore() method
            private final AccessControlContext accControlContext;

            //
            // Constructor BuilderImpl initializes private fields: keyStore,
            // protParameter, typeForKeyStore providerForKeyStore fileForLoad,
            // isGetKeyStore
            //
            BuilderImpl(KeyStore ks, ProtectionParameter pp, File file,
                    String type, Provider provider, AccessControlContext context) {
                super();
                keyStore = ks;
                protParameter = pp;
                fileForLoad = file;
                typeForKeyStore = type;
                providerForKeyStore = provider;
                isGetKeyStore = false;
                lastException = null;
                accControlContext = context;
            }

            //
            // Implementation of abstract getKeyStore() method If
            // KeyStoreBuilder encapsulates KeyStore object then this object is
            // returned
            // 
            // If KeyStoreBuilder encapsulates KeyStore type and provider then
            // KeyStore is created using these parameters. If KeyStoreBuilder
            // encapsulates file and ProtectionParameter then KeyStore data are
            // loaded from FileInputStream that is created on file. If file is
            // not defined then KeyStore object is initialized with null
            // InputStream and null password.
            // 
            // Result KeyStore object is returned.
            //
            public synchronized KeyStore getKeyStore() throws KeyStoreException {
                // If KeyStore was created but in final block some exception was
                // thrown
                // then it was stored in lastException variable and will be
                // thrown
                // all subsequent calls of this method.
                if (lastException != null) {
                    throw lastException;
                }
                if (keyStore != null) {
                    isGetKeyStore = true;
                    return keyStore;
                }

                try {
                    final KeyStore ks;
                    final char[] passwd;

                    // get KeyStore instance using type or type and provider
                    ks = (providerForKeyStore == null ? KeyStore
                            .getInstance(typeForKeyStore) : KeyStore
                            .getInstance(typeForKeyStore, providerForKeyStore));
                    // protection parameter should be PasswordProtection
                    // or CallbackHandlerProtection
                    if (protParameter instanceof PasswordProtection) {
                        passwd = ((PasswordProtection) protParameter)
                                .getPassword();
                    } else if (protParameter instanceof CallbackHandlerProtection) {
                        passwd = KeyStoreSpi
                                .getPasswordFromCallBack(protParameter);
                    } else {
                        throw new KeyStoreException(Messages.getString("security.35")); //$NON-NLS-1$
                    }

                    // load KeyStore from file
                    AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Object>() {
                                public Object run() throws Exception {
                                    if (fileForLoad != null) {
                                        FileInputStream fis = null;
                                        try {
                                            fis = new FileInputStream(fileForLoad);
                                            ks.load(fis, passwd);
                                        } finally {
                                            // close file input stream
                                            if( fis != null ) {
                                                fis.close();   
                                            }
                                        }
                                    } else {
                                        ks.load(new TmpLSParameter(
                                                protParameter));
                                    }
                                    return null;
                                }
                            }, accControlContext);

                    
                    isGetKeyStore = true;
                    keyStore = ks;
                    return keyStore;
                } catch (KeyStoreException e) {
                    // Store exception
                    throw lastException = e;
                } catch (Exception e) {
                    // Override exception
                    throw lastException = new KeyStoreException(e);
                }
            }

            //
            // This is implementation of abstract method
            // getProtectionParameter(String alias)
            // 
            // Return: ProtectionParameter to get Entry which was saved in
            // KeyStore with defined alias
            //
            public synchronized ProtectionParameter getProtectionParameter(
                    String alias) throws KeyStoreException {
                if (alias == null) {
                    throw new NullPointerException(Messages.getString("security.3F")); //$NON-NLS-1$
                }
                if (!isGetKeyStore) {
                    throw new IllegalStateException(Messages.getString("security.46")); //$NON-NLS-1$
                }
                return protParameter;
            }
        }

        // BEGIN android-note
        // Added "static" to the class declaration below.
        // END android-note
        /*
         * Implementation of LoadStoreParameter interface
         * 
         * @author Vera Petrashkova
         */
        private static class TmpLSParameter implements LoadStoreParameter {

            // Store used protection parameter
            private final ProtectionParameter protPar;

            /**
             * Creates TmpLoadStoreParameter object
             */
            public TmpLSParameter(ProtectionParameter protPar) {
                this.protPar = protPar;
            }

            /**
             * This method returns protection parameter
             */
            public ProtectionParameter getProtectionParameter() {
                return protPar;
            }
        }
    }

    /**
     * 
     * 
     * 
     */
    public static class CallbackHandlerProtection implements
            ProtectionParameter {
        // Store CallbackHandler
        private final CallbackHandler callbackHandler;

        /**
         * 
         *  
         */
        public CallbackHandlerProtection(CallbackHandler handler) {
            if (handler == null) {
                throw new NullPointerException(Messages.getString("security.47")); //$NON-NLS-1$
            }
            this.callbackHandler = handler;
        }

        /**
         * 
         *  
         */
        public CallbackHandler getCallbackHandler() {
            return callbackHandler;
        }
    }

    /**
     * 
     * 
     * 
     */
    public static interface Entry {
    }

    /**
     * 
     * 
     * 
     */
    public static interface LoadStoreParameter {
        /**
         * 
         *  
         */
        public ProtectionParameter getProtectionParameter();
    }

    /**
     * 
     * 
     * 
     */
    public static class PasswordProtection implements ProtectionParameter,
            Destroyable {

        // Store password
        private char[] password;

        private boolean isDestroyed = false;

        /**
         * 
         *  
         */
        public PasswordProtection(char[] password) {
            this.password = password;
        }

        /**
         * 
         *  
         */
        public synchronized char[] getPassword() {
            if (isDestroyed) {
                throw new IllegalStateException(Messages.getString("security.36")); //$NON-NLS-1$
            }
            return password;
        }

        /**
         * 
         *  
         */
        public synchronized void destroy() throws DestroyFailedException {
            isDestroyed = true;
            if (password != null) {
                Arrays.fill(password, '\u0000');
                password = null;
            }
        }

        /**
         * 
         *  
         */
        public synchronized boolean isDestroyed() {
            return isDestroyed;
        }
    }

    /**
     * 
     * 
     * 
     */
    public static interface ProtectionParameter {
    }

    /**
     * 
     * 
     * 
     */
    public static final class PrivateKeyEntry implements Entry {
        // Store Certificate chain
        private Certificate[] chain;

        // Store PrivateKey
        private PrivateKey privateKey;

        /**
         * 
         *  
         */
        public PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain) {
            if (privateKey == null) {
                throw new NullPointerException(Messages.getString("security.48")); //$NON-NLS-1$
            }
            if (chain == null) {
                throw new NullPointerException(Messages.getString("security.49")); //$NON-NLS-1$
            }

            if (chain.length == 0) {
                throw new IllegalArgumentException(Messages.getString("security.4A")); //$NON-NLS-1$
            }
            // Match algorithm of private key and algorithm of public key from
            // the end certificate
            String s = chain[0].getType();
            if (!(chain[0].getPublicKey().getAlgorithm()).equals(privateKey
                    .getAlgorithm())) {
                throw new IllegalArgumentException(Messages.getString("security.4B")); //$NON-NLS-1$
            }
            // Match certificate types
            for (int i = 1; i < chain.length; i++) {
                if (!s.equals(chain[i].getType())) {
                    throw new IllegalArgumentException(
                            Messages.getString("security.4C")); //$NON-NLS-1$
                }
            }
            // clone chain - this.chain = (Certificate[])chain.clone();
            this.chain = new Certificate[chain.length];
            System.arraycopy(chain, 0, this.chain, 0, chain.length);
            this.privateKey = privateKey;
        }

        /**
         * 
         *  
         */
        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        /**
         * 
         *  
         */
        public Certificate[] getCertificateChain() {
            return chain;
        }

        /**
         * 
         *  
         */
        public Certificate getCertificate() {
            return chain[0];
        }

        /**
         * 
         *  
         */
        public String toString() {
            StringBuffer sb = new StringBuffer(
                    "PrivateKeyEntry: number of elements in certificate chain is "); //$NON-NLS-1$
            sb.append(Integer.toString(chain.length));
            sb.append("\n"); //$NON-NLS-1$
            for (int i = 0; i < chain.length; i++) {
                sb.append(chain[i].toString());
                sb.append("\n"); //$NON-NLS-1$
            }
            return sb.toString();
        }
    }

    /**
     * 
     * 
     * 
     */
    public static final class SecretKeyEntry implements Entry {

        // Store SecretKey
        private final SecretKey secretKey;

        /**
         * 
         *  
         */
        public SecretKeyEntry(SecretKey secretKey) {
            if (secretKey == null) {
                throw new NullPointerException(Messages.getString("security.4D")); //$NON-NLS-1$
            }
            this.secretKey = secretKey;
        }

        /**
         * 
         *  
         */
        public SecretKey getSecretKey() {
            return secretKey;
        }

        /**
         * 
         *  
         */
        public String toString() {
            StringBuffer sb = new StringBuffer("SecretKeyEntry: algorithm - "); //$NON-NLS-1$
            sb.append(secretKey.getAlgorithm());
            return sb.toString();
        }
    }

    /**
     * 
     * 
     * 
     */
    public static final class TrustedCertificateEntry implements Entry {

        // Store trusted Certificate
        private final Certificate trustCertificate;

        /**
         * 
         *  
         */
        public TrustedCertificateEntry(Certificate trustCertificate) {
            if (trustCertificate == null) {
                throw new NullPointerException(Messages.getString("security.4E")); //$NON-NLS-1$
            }
            this.trustCertificate = trustCertificate;
        }

        /**
         * 
         *  
         */
        public Certificate getTrustedCertificate() {
            return trustCertificate;
        }

        /**
         * 
         *  
         */
        public String toString() {
            return "Trusted certificate entry:\n" + trustCertificate; //$NON-NLS-1$
        }
    }
}
