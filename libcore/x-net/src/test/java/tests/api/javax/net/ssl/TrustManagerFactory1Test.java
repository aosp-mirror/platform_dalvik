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

package tests.api.javax.net.ssl;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;

import org.apache.harmony.security.tests.support.SpiEngUtils;
import org.apache.harmony.xnet.tests.support.MyTrustManagerFactorySpi;
import junit.framework.TestCase;

/**
 * Tests for <code>TrustManagerFactory</code> class constructors and methods.
 * 
 */

public class TrustManagerFactory1Test extends TestCase {
  
    private static final String srvTrustManagerFactory = "TrustManagerFactory";

    private static String defaultAlgorithm = null;

    private static String defaultProviderName = null;

    private static Provider defaultProvider = null;

    private static boolean DEFSupported = false;

    private static final String NotSupportedMsg = "There is no suitable provider for TrustManagerFactory";

    private static final String[] invalidValues = SpiEngUtils.invalidValues;

    private static String[] validValues = new String[3];
    static {
        defaultAlgorithm = Security
                .getProperty("ssl.TrustManagerFactory.algorithm");
        if (defaultAlgorithm != null) {
            defaultProvider = SpiEngUtils.isSupport(defaultAlgorithm,
                    srvTrustManagerFactory);
            DEFSupported = (defaultProvider != null);
            defaultProviderName = (DEFSupported ? defaultProvider.getName()
                    : null);
            validValues[0] = defaultAlgorithm;
            validValues[1] = defaultAlgorithm.toUpperCase();
            validValues[2] = defaultAlgorithm.toLowerCase();
        }
    }

    protected TrustManagerFactory[] createTMFac() {
        if (!DEFSupported) {
            fail(defaultAlgorithm + " algorithm is not supported");
            return null;
        }
        TrustManagerFactory[] tMF = new TrustManagerFactory[3];
        try {
            tMF[0] = TrustManagerFactory.getInstance(defaultAlgorithm);
            tMF[1] = TrustManagerFactory.getInstance(defaultAlgorithm,
                    defaultProvider);
            tMF[2] = TrustManagerFactory.getInstance(defaultAlgorithm,
                    defaultProviderName);
            return tMF;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Test for
     * <code>TrustManagerFactory(TrustManagerFactorySpi impl, Provider prov, String algoriyjm) </code>
     * constructor
     * Assertion: created new TrustManagerFactory object
     */
    public void test_ConstructorLjavax_net_ssl_TrustManagerFactorySpiLjava_security_ProviderLjava_lang_String()
        throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        TrustManagerFactorySpi spi = new MyTrustManagerFactorySpi(); 
        TrustManagerFactory tmF = new myTrustManagerFactory(spi, defaultProvider,
                defaultAlgorithm);
        assertTrue("Not CertStore object", tmF instanceof TrustManagerFactory);
        assertEquals("Incorrect algorithm", tmF.getAlgorithm(),
                defaultAlgorithm);
        assertEquals("Incorrect provider", tmF.getProvider(), defaultProvider);
        assertNull("Incorrect result", tmF.getTrustManagers());
        
        tmF = new myTrustManagerFactory(null, null, null);
        assertTrue("Not CertStore object", tmF instanceof TrustManagerFactory);
        assertNull("Provider must be null", tmF.getProvider());
        assertNull("Algorithm must be null", tmF.getAlgorithm());
        try {
            tmF.getTrustManagers();
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
    }

    /**
     *  Test for <code>getAlgorithm()</code> method
     * Assertion: returns the algorithm name of this object
     * @throws NoSuchAlgorithmException 
     * @throws NoSuchProviderException 
     */
    public void test_getAlgorithm()
        throws NoSuchAlgorithmException, NoSuchProviderException {
        if (!DEFSupported) fail(NotSupportedMsg);
        assertEquals("Incorrect algorithm",
        		defaultAlgorithm,
        		TrustManagerFactory
        		.getInstance(defaultAlgorithm).getAlgorithm());
        assertEquals("Incorrect algorithm",
        		defaultAlgorithm,
        		TrustManagerFactory
        		.getInstance(defaultAlgorithm, defaultProviderName)
        		.getAlgorithm());
        assertEquals("Incorrect algorithm",
        		defaultAlgorithm,
        		TrustManagerFactory.getInstance(defaultAlgorithm, defaultProvider)
        		.getAlgorithm());
    }

    /**
     *  Test for <code>getDefaultAlgorithm()</code> method
     * Assertion: returns value which is specifoed in security property
     */
    public void test_getDefaultAlgorithm() {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        String def = TrustManagerFactory.getDefaultAlgorithm();
        if (defaultAlgorithm == null) {
            assertNull("DefaultAlgorithm must be null", def);
        } else {
            assertEquals("Invalid default algorithm", def, defaultAlgorithm);
        }
        String defA = "Proba.trustmanagerfactory.defaul.type";
        Security.setProperty("ssl.TrustManagerFactory.algorithm", defA);
        assertEquals("Incorrect defaultAlgorithm", 
                TrustManagerFactory.getDefaultAlgorithm(), defA);
        if (def == null) {
            def = "";
        }
        Security.setProperty("ssl.TrustManagerFactory.algorithm", def); 
        assertEquals("Incorrect defaultAlgorithm", 
                TrustManagerFactory.getDefaultAlgorithm(), def);        
    }

    /**
     * Test for <code>getInstance(String algorithm)</code> method 
     * Assertions: returns security property "ssl.TrustManagerFactory.algorithm"; 
     * returns instance of TrustManagerFactory
     */
    public void test_getInstanceLjava_lang_String01() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        TrustManagerFactory trustMF;
        for (int i = 0; i < validValues.length; i++) {
            trustMF = TrustManagerFactory.getInstance(validValues[i]);
            assertTrue("Not TrustManagerFactory object",
                    trustMF instanceof TrustManagerFactory);
            assertEquals("Invalid algorithm", trustMF.getAlgorithm(),
                    validValues[i]);
        }
    }

    /**
     * Test for <code>getInstance(String algorithm)</code> method 
     * Assertion:
     * throws NullPointerException when algorithm is null;
     * throws NoSuchAlgorithmException when algorithm is not correct;
     */
    public void test_getInstanceLjava_lang_String02() {
        try {
            TrustManagerFactory.getInstance(null);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                TrustManagerFactory.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException was not thrown as expected for algorithm: "
                        .concat(invalidValues[i]));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, String provider)</code>
     * method
     * Assertion: throws IllegalArgumentException when provider is null
     * or empty
     */
    public void test_getInstanceLjava_lang_StringLjava_lang_String01() throws NoSuchProviderException,
            NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        String provider = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                TrustManagerFactory.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown when provider is null");
            } catch (IllegalArgumentException e) {
            }
            try {
                TrustManagerFactory.getInstance(validValues[i], "");
                fail("IllegalArgumentException must be thrown when provider is empty");
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, String provider)</code>
     * method
     * Assertion: 
     * throws NullPointerException when algorithm is null;
     * throws NoSuchAlgorithmException when algorithm is not correct;
     */
    public void test_getInstanceLjava_lang_StringLjava_lang_String02() throws NoSuchProviderException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        try {
            TrustManagerFactory.getInstance(null, defaultProviderName);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                TrustManagerFactory.getInstance(invalidValues[i],
                        defaultProviderName);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, String provider)</code>
     * method
     * Assertion: throws NoSuchProviderException when provider has
     * invalid value
     */
    public void test_getInstanceLjava_lang_StringLjava_lang_String03() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        for (int i = 1; i < invalidValues.length; i++) {
            for (int j = 0; j < validValues.length; j++) {
                try {
                    TrustManagerFactory.getInstance(validValues[j],
                            invalidValues[i]);
                    fail("NuSuchProviderException must be thrown (algorithm: "
                            .concat(validValues[j]).concat(" provider: ")
                            .concat(invalidValues[i]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, String provider)</code>
     * method
     * Assertion: returns instance of TrustManagerFactory
     */
    public void test_getInstanceLjava_lang_StringLjava_lang_String04() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        TrustManagerFactory trustMF;
        for (int i = 0; i < validValues.length; i++) {
            trustMF = TrustManagerFactory.getInstance(validValues[i],
                    defaultProviderName);
            assertTrue("Not TrustManagerFactory object",
                    trustMF instanceof TrustManagerFactory);
            assertEquals("Invalid algorithm", trustMF.getAlgorithm(),
                    validValues[i]);
            assertEquals("Invalid provider", trustMF.getProvider(),
                    defaultProvider);
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, Provider provider)</code>
     * method
     * Assertion: throws IllegalArgumentException when provider is null
     */
    public void test_getInstanceLjava_lang_StringLjava_security_Provider01() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        Provider provider = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                TrustManagerFactory.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown  when provider is null");
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, Provider provider)</code>
     * method
     * Assertion: 
     * throws NullPointerException when algorithm is null;
     * throws NoSuchAlgorithmException when algorithm is not correct;
     */
    public void test_getInstanceLjava_lang_StringLjava_security_Provider02() {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        try {
            TrustManagerFactory.getInstance(null, defaultProvider);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                TrustManagerFactory.getInstance(invalidValues[i],
                        defaultProvider);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, Provider provider)</code>
     * method
     * Assertion: returns instance of TrustManagerFactory
     */
    public void test_getInstanceLjava_lang_StringLjava_security_Provider03() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        TrustManagerFactory trustMF;
        for (int i = 0; i < validValues.length; i++) {
            trustMF = TrustManagerFactory.getInstance(validValues[i],
                    defaultProvider);
            assertTrue("Not TrustManagerFactory object",
                    trustMF instanceof TrustManagerFactory);
            assertEquals("Invalid algorithm", trustMF.getAlgorithm(),
                    validValues[i]);
            assertEquals("Invalid provider", trustMF.getProvider(),
                    defaultProvider);
        }
    }

    /**
     * Test for <code>getProvider()</code>
     * @throws NoSuchAlgorithmException 
     * @throws NoSuchProviderException 
     */
    public void test_getProvider()
        throws NoSuchAlgorithmException, NoSuchProviderException {
    	if (!DEFSupported) fail(NotSupportedMsg);
        assertEquals("Incorrect provider",
        		defaultProvider,
        		TrustManagerFactory
        		.getInstance(defaultAlgorithm).getProvider());
        assertEquals("Incorrect provider",
        		defaultProvider,
        		TrustManagerFactory
        		.getInstance(defaultAlgorithm, defaultProviderName)
        		.getProvider());
        assertEquals("Incorrect provider",
        		defaultProvider,
        		TrustManagerFactory.getInstance(defaultAlgorithm, defaultProvider)
        		.getProvider());
    }
    
    /**
     * Test for <code>geTrustManagers()</code>
     * @throws KeyStoreException 
     * @throws IOException 
     * @throws CertificateException 
     * @throws NoSuchAlgorithmException 
     */
    public void test_getTrustManagers()
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);            
        TrustManager[] tm;
        TrustManagerFactory[] trustMF = createTMFac();
        assertNotNull("TrustManagerFactory objects were not created", trustMF);
        for (int i = 0; i < trustMF.length; i++) {
            try {
                trustMF[i].init((KeyStore)null);
            } catch (KeyStoreException e) {
            	fail("Unexpected exception " + e.toString());
            }
            trustMF[i].init(ks);
            tm = trustMF[i].getTrustManagers();
            assertNotNull("Result has not be null", tm);
            assertTrue("Length of result TrustManager array should not be 0",
                    (tm.length > 0));
        }
    }

    /**
     * Test for <code>init(KeyStore keyStore)</code>
     * Assertion: returns not empty TrustManager array
     */
    public void test_initLjava_security_KeyStore() throws NoSuchAlgorithmException,
            KeyStoreException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        KeyStore ks;
        KeyStore ksNull = null;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);            
        } catch (KeyStoreException e) {
            fail(e.toString() + "default KeyStore type is not supported");
            return;
        } catch (Exception e) {
            fail("Unexpected: " + e.toString());
            return;
        }
    }

    /**
     * Test for <code>init(ManagerFactoryParameters params)</code>
     * Assertion:
     * throws InvalidAlgorithmParameterException when params is null
     */
    public void test_initLjavax_net_ssl_ManagerFactoryParameters()
        throws NoSuchAlgorithmException,
        KeyStoreException, InvalidAlgorithmParameterException  {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        ManagerFactoryParameters par = null;
        ManagerFactoryParameters par1 = new myManagerFactoryParam();
        TrustManagerFactory[] trustMF = createTMFac();
        assertNotNull("TrustManagerFactory objects were not created", trustMF);
        for (int i = 0; i < trustMF.length; i++) {
            try {
                trustMF[i].init(par);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch (InvalidAlgorithmParameterException e) {
            }
            try {
                trustMF[i].init(par1);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch (InvalidAlgorithmParameterException e) {
            }
        }        
    }

}

/**
 * Addifional class to verify TrustManagerFactory constructor
 */

class myTrustManagerFactory extends TrustManagerFactory {
    public myTrustManagerFactory(TrustManagerFactorySpi spi, Provider prov,
            String alg) {
        super(spi, prov, alg);
    }
}

class myManagerFactoryParam implements ManagerFactoryParameters {
    
}