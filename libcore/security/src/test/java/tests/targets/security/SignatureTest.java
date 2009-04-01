/*
 * Copyright (C) 2009 The Android Open Source Project
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
package tests.targets.security;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
public abstract class SignatureTest extends TestCase {

    private final String algorithmName;
    private final String keyAlgorithmName;
    private static final String signData = "some data to sign an"; //d verify";
    KeyPairGenerator generator;
    KeyPair keyPair;

    public SignatureTest(String algorithmName, String keyAlgorithmName) {
        this.algorithmName = algorithmName;
        this.keyAlgorithmName = keyAlgorithmName;
    }

    protected void setUp() throws Exception {
        super.setUp();
        generator = getGenerator();
        keyPair = getKeyPair();
    }

    private KeyPair getKeyPair() {
        return generator.generateKeyPair();
    }

    private KeyPairGenerator getGenerator() {
        try {
            return KeyPairGenerator.getInstance(keyAlgorithmName);
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
        return null;
    }

    @TestTargets({
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "getInstance",
                args = {String.class}
        ),
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "initSign",
                args = {PrivateKey.class}
        ),
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "update",
                args = {byte[].class}
        ),
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "sign",
                args = {}
        ),
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "initVerify",
                args = {PublicKey.class}
        ),
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "verify",
                args = {byte[].class}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "method",
                args = {}
        )
    })
    public void testSignature() {
        Signature signature = null;
        try {
            signature = Signature.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }

        try {
            signature.initSign(keyPair.getPrivate());
        } catch (InvalidKeyException e) {
            fail(e.getMessage());
        }

        try {
            signature.update(signData.getBytes());
        } catch (SignatureException e) {
            fail(e.getMessage());
        }

        byte[] sign = null;
        try {
            sign = signature.sign();
        } catch (SignatureException e) {
            fail(e.getMessage());
        }

        try {
            signature.initVerify(keyPair.getPublic());
        } catch (InvalidKeyException e) {
            fail(e.getMessage());
        }

        try {
            signature.update(signData.getBytes());
        } catch (SignatureException e) {
            fail(e.getMessage());
        }

        try {
            assertTrue(signature.verify(sign));
        } catch (SignatureException e) {
            fail(e.getMessage());
        }
    }
}
