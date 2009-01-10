package org.apache.harmony.xnet.tests.support;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;

public class TrustManagerFactorySpiImpl extends MyTrustManagerFactorySpi {
    
    private boolean isInitialized = false;
    
    public void engineInit(KeyStore ks) throws KeyStoreException {
        if (ks == null) {
            throw new KeyStoreException("Not supported operation for null KeyStore");
        }
        isInitialized = true;
    }

    public void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        if (spec == null) {
            throw new InvalidAlgorithmParameterException("Null parameter");
        }
        if (spec instanceof Parameters) {
            try {
                engineInit(((Parameters)spec).getKeyStore());
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new InvalidAlgorithmParameterException("Invalid parameter");
        }
        isInitialized = true;
    }

    public TrustManager[] engineGetTrustManagers() {
        if(!isInitialized)
            throw new IllegalStateException("TrustManagerFactorySpi is not initialized");
        else
            return null;
    }

}
