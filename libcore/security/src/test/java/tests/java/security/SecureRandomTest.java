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

package tests.java.security;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import org.apache.harmony.security.tests.support.RandomImpl;

import junit.framework.TestCase;
@TestTargetClass(SecureRandom.class)
/**
 * Tests for <code>SecureRandom</code> constructor and methods
 * 
 */
public class SecureRandomTest extends TestCase {

    /**
     * SRProvider
     */
    Provider p;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        p = new SRProvider();
        Security.insertProviderAt(p, 1);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(p.getName());
    }

    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification of negative and boundary parameters missed",
      targets = {
        @TestTarget(
          methodName = "next",
          methodArgs = {int.class}
        )
    })
    public final void testNext() {
        MySecureRandom sr = new MySecureRandom();
        if (sr.nextElement(1) != 1 || sr.nextElement(2) != 3 || sr.nextElement(3) != 7) {
            fail("next failed");            
        }
    }

    /*
     * Class under test for void setSeed(long)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification of boundary parameter missed",
      targets = {
        @TestTarget(
          methodName = "setSeed",
          methodArgs = {long.class}
        )
    })
    public final void testSetSeedlong() {
        SecureRandom sr = new SecureRandom();
        sr.setSeed(12345);
        if (!RandomImpl.runEngineSetSeed) {
            fail("setSeed failed");
        }    
    }

    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Null parameter verification missed",
      targets = {
        @TestTarget(
          methodName = "nextBytes",
          methodArgs = {byte[].class}
        )
    })
    public final void testNextBytes() {
        byte[] b = new byte[5];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(b);
        for (int i = 0; i < b.length; i++) {
            if (b[i] != (byte)(i + 0xF1)) {
                fail("nextBytes failed");
            }
        }
    }

    /*
     * Class under test for void SecureRandom()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "SecureRandom",
          methodArgs = {}
        )
    })
    public final void testSecureRandom() {
        SecureRandom sr = new SecureRandom();
        if (!sr.getAlgorithm().equals("someRandom")  ||
                sr.getProvider()!= p) {
            fail("incorrect SecureRandom implementation" + p.getName());
        }    
    }

    /*
     * Class under test for void SecureRandom(byte[])
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Null parameter checking missed",
      targets = {
        @TestTarget(
          methodName = "SecureRandom",
          methodArgs = {byte[].class}
        )
    })
    public final void testSecureRandombyteArray() {
        byte[] b = {1,2,3};
        new SecureRandom(b);
        
        if (!RandomImpl.runEngineSetSeed) {
            fail("No setSeed");
        }
    }

    /*
     * Class under test for SecureRandom getInstance(String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "NoSuchAlgorithmException checking missed",
      targets = {
        @TestTarget(
          methodName = "getInstance",
          methodArgs = {String.class}
        )
    })
    public final void testGetInstanceString() {
        SecureRandom sr = null;
        try {
            sr = SecureRandom.getInstance("someRandom");    
        } catch (NoSuchAlgorithmException e) {
            fail(e.toString());
        }
        if (sr.getProvider() != p || !"someRandom".equals(sr.getAlgorithm())) {
            fail("getInstance failed");
        }    
    }

    /*
     * Class under test for SecureRandom getInstance(String, String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "NoSuchAlgorithmException, NoSuchProviderException, IllegalArgumentException checking missed",
      targets = {
        @TestTarget(
          methodName = "getInstance",
          methodArgs = {String.class, String.class}
        )
    })
    public final void testGetInstanceStringString() throws Exception {
        SecureRandom sr = SecureRandom.getInstance("someRandom", "SRProvider");    
        if (sr.getProvider() != p || !"someRandom".equals(sr.getAlgorithm())) {
            fail("getInstance failed");
        }    
    }

    /*
     * Class under test for SecureRandom getInstance(String, Provider)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "NoSuchAlgorithmException, IllegalArgumentException checking missed",
      targets = {
        @TestTarget(
          methodName = "getInstance",
          methodArgs = {String.class, Provider.class}
        )
    })
    public final void testGetInstanceStringProvider() throws Exception {
        Provider p = new SRProvider();
        SecureRandom sr = SecureRandom.getInstance("someRandom", p);
        if (sr.getProvider() != p || !"someRandom".equals(sr.getAlgorithm())) {
            fail("getInstance failed");
        }    
    }

    /*
     * Class under test for void setSeed(byte[])
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification with null parameter missed",
      targets = {
        @TestTarget(
          methodName = "setSeed",
          methodArgs = {byte[].class}
        )
    })
    public final void testSetSeedbyteArray() {
        byte[] b = {1,2,3};
        SecureRandom sr = new SecureRandom();
        sr.setSeed(b);
        if (!RandomImpl.runEngineSetSeed) {
            fail("setSeed failed");
        }
    }

    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification with invalid parameter missed",
      targets = {
        @TestTarget(
          methodName = "getSeed",
          methodArgs = {int.class}
        )
    })
    public final void testGetSeed() {
        byte[] b = SecureRandom.getSeed(4);
        if( b.length != 4) {
            fail("getSeed failed");
        }
    }

    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verification with invalid parameter missed",
      targets = {
        @TestTarget(
          methodName = "generateSeed",
          methodArgs = {int.class}
        )
    })
    public final void testGenerateSeed() {
        SecureRandom sr = new SecureRandom();
        byte[] b = sr.generateSeed(4);
        for (int i = 0; i < b.length; i++) {
            if (b[i] != (byte)i) {
                fail("generateSeed failed");
            }
        }
    }
    
    
    
    public class SRProvider extends Provider {
        public SRProvider() {
            super("SRProvider", 1.0, "SRProvider for testing");
            put("SecureRandom.someRandom",
                    "org.apache.harmony.security.tests.support.RandomImpl");
        }
    }
    
    class MySecureRandom extends SecureRandom {
        public MySecureRandom(){
            super();
        }
        
        public int nextElement(int numBits) {
            return super.next(numBits);
        }
    }
}
