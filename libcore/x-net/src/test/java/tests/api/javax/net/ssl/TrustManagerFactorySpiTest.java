/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.api.javax.net.ssl;

import dalvik.annotation.TestTargetClass; 
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

import junit.framework.TestCase;
import org.apache.harmony.xnet.tests.support.TrustManagerFactorySpiImpl;
import org.apache.harmony.xnet.tests.support.MyTrustManagerFactorySpi.Parameters;

@TestTargetClass(TrustManagerFactorySpi.class) 
public class TrustManagerFactorySpiTest extends TestCase {

    /**
     * @tests javax.net.ssl.TrustManagerFactorySpi#TrustManagerFactorySpi()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TrustManagerFactorySpi",
        args = {}
    )
    public void test_Constructor() {
        try {
            TrustManagerFactorySpiImpl tmf = new TrustManagerFactorySpiImpl();
            assertTrue(tmf instanceof TrustManagerFactorySpi);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    
    /**
     * @tests javax.net.ssl.TrustManagerFactorySpi#engineInit(KeyStore ks)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineInit",
        args = {java.security.KeyStore.class}
    )
    public void test_engineInit_01() {
        TrustManagerFactorySpiImpl tmf = new TrustManagerFactorySpiImpl();
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            tmf.engineInit(ks);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
        try {
            KeyStore ks = null;
            tmf.engineInit(ks);
            fail("KeyStoreException wasn't thrown");
        } catch (KeyStoreException kse) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.TrustManagerFactorySpi#engineInit(ManagerFactoryParameters spec)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineInit",
        args = {javax.net.ssl.ManagerFactoryParameters.class}
    )
    public void test_engineInit_02() {
        TrustManagerFactorySpiImpl tmf = new TrustManagerFactorySpiImpl();
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            Parameters pr = new Parameters(ks);
            tmf.engineInit(pr);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
        try {
            ManagerFactoryParameters mfp = null;
            tmf.engineInit(mfp);
            fail("InvalidAlgorithmParameterException wasn't thrown");
        } catch (InvalidAlgorithmParameterException kse) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.TrustManagerFactorySpi#engineGetTrustManagers()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineGetTrustManagers",
        args = {}
    )
    public void test_engineGetTrustManagers() {
        TrustManagerFactorySpiImpl tmf = new TrustManagerFactorySpiImpl();
        try {
            TrustManager[] tm = tmf.engineGetTrustManagers();
            fail("IllegalStateException wasn't thrown");
        } catch (IllegalStateException ise) {
            //expected
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalStateException");
        }
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            tmf.engineInit(ks);
            TrustManager[] tm = tmf.engineGetTrustManagers();
            assertNull("Object is not NULL", tm);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
}
