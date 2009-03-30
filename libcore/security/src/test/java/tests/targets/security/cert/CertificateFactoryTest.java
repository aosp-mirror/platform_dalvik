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
package tests.targets.security.cert;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public abstract class CertificateFactoryTest extends TestCase {

    private final String algorithmName;
    private final byte[] certificateData;


    public CertificateFactoryTest(String algorithmName, byte[] certificateData) {
        this.algorithmName = algorithmName;
        this.certificateData = certificateData;
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    @TestTargets({
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="getInstance",
                args={String.class}
        ),
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="generateCertificate",
                args={InputStream.class}
        ),
        @TestTargetNew(
                level=TestLevel.COMPLETE,
                method="method",
                args={}
        )
    })
    public void testCertificateFactory() {
        CertificateFactory certificateFactory = null;
        try {
            certificateFactory = CertificateFactory.getInstance(algorithmName);
        } catch (CertificateException e) {
            fail(e.getMessage());
        }

        Certificate certificate = null;
        try {
            certificate = certificateFactory
                    .generateCertificate(new ByteArrayInputStream(
                            certificateData));
        } catch (CertificateException e) {
            fail(e.getMessage());
        }

        assertNotNull(certificate);
    }
}
