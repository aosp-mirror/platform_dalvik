/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.archive.tests.java.util.jar;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import junit.framework.TestCase;
import tests.support.resource.Support_Resources;

@TestTargetClass(Manifest.class)
public class ManifestTest extends TestCase {

    private final String jarName = "hyts_patch.jar";

    private final String attJarName = "hyts_att.jar";

    private Manifest m;

    private Manifest m2;

    private File resources;

    @Override
    protected void setUp() {
        resources = Support_Resources.createTempFolder();
        try {
            Support_Resources.copyFile(resources, null, jarName);
            JarFile jarFile = new JarFile(new File(resources, jarName));
            m = jarFile.getManifest();
            jarFile.close();
            Support_Resources.copyFile(resources, null, attJarName);
            jarFile = new JarFile(new File(resources, attJarName));
            m2 = jarFile.getManifest();
            jarFile.close();
        } catch (Exception e) {
            fail("Exception during setup: " + e.toString());
        }
    }

    /**
     * @tests java.util.jar.Manifest#Manifest()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Manifest",
        args = {}
    )
    public void test_Constructor() {
        // Test for method java.util.jar.Manifest()
        Manifest emptyManifest = new Manifest();
        assertTrue("Should have no entries", emptyManifest.getEntries()
                .isEmpty());
        assertTrue("Should have no main attributes", emptyManifest
                .getMainAttributes().isEmpty());
    }

    /**
     * @tests java.util.jar.Manifest#Manifest(java.io.InputStream)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "IOException checking missed.",
        method = "Manifest",
        args = {java.io.InputStream.class}
    )
    public void test_ConstructorLjava_io_InputStream() {
        // Test for method java.util.jar.Manifest(java.io.InputStream)
        /*
         * ByteArrayOutputStream baos = new ByteArrayOutputStream();
         * m2.write(baos); InputSteam is = new ByteArrayInputStream
         * (baos.toByteArray()); Manifest myManifest = new Manifest (is);
         * assertTrue("Manifests should be equal", myManifest.equals(m2));
         */

        Manifest manifest = null;
        InputStream is = null;
        try {
            is = new URL(Support_Resources.getURL("manifest/hyts_MANIFEST.MF"))
                    .openStream();
        } catch (MalformedURLException e1) {
            fail("Failed to create InputStream object");
        } catch (IOException e1) {
            fail("Failed to create InputStream object");
        }
        try {
            manifest = new Manifest(is);
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        } catch (IOException e) {
            fail("IOException");
        }
        Attributes main = manifest.getMainAttributes();
        assertEquals("Bundle-Name not correct", "ClientSupport", main
                .getValue("Bundle-Name"));
        assertEquals(
                "Bundle-Description not correct",

                "Provides SessionService, AuthenticationService. Extends RegistryService.",
                main.getValue("Bundle-Description"));
        assertEquals("Bundle-Activator not correct",
                "com.ibm.ive.eccomm.client.support.ClientSupportActivator",
                main.getValue("Bundle-Activator"));
        assertEquals(
                "Import-Package not correct",

                "com.ibm.ive.eccomm.client.services.log,com.ibm.ive.eccomm.client.services.registry,com.ibm.ive.eccomm.service.registry; specification-version=1.0.0,com.ibm.ive.eccomm.service.session; specification-version=1.0.0,com.ibm.ive.eccomm.service.framework; specification-version=1.2.0,org.osgi.framework; specification-version=1.0.0,org.osgi.service.log; specification-version=1.0.0,com.ibm.ive.eccomm.flash; specification-version=1.2.0,com.ibm.ive.eccomm.client.xml,com.ibm.ive.eccomm.client.http.common,com.ibm.ive.eccomm.client.http.client",
                main.getValue("Import-Package"));
        assertEquals(
                "Import-Service not correct",

                "org.osgi.service.log.LogReaderServiceorg.osgi.service.log.LogService,com.ibm.ive.eccomm.service.registry.RegistryService",
                main.getValue("Import-Service"));
        assertEquals(
                "Export-Package not correct",

                "com.ibm.ive.eccomm.client.services.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.service.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.common; specification-version=1.0.0,com.ibm.ive.eccomm.client.services.registry.store; specification-version=1.0.0",
                main.getValue("Export-Package"));
        assertEquals(
                "Export-Service not correct",

                "com.ibm.ive.eccomm.service.authentication.AuthenticationService,com.ibm.ive.eccomm.service.session.SessionService",
                main.getValue("Export-Service"));
        assertEquals("Bundle-Vendor not correct", "IBM", main
                .getValue("Bundle-Vendor"));
        assertEquals("Bundle-Version not correct", "1.2.0", main
                .getValue("Bundle-Version"));
        try {
            is.close();
        } catch (IOException e1) {
            fail("Failed to close InputStream object");
        }
        try {
            manifest = new Manifest(is);
            fail("IOException expected");
        } catch (MalformedURLException e) {
            fail("IOException expected");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests java.util.jar.Manifest#clear()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
    public void test_clear() {
        // Test for method void java.util.jar.Manifest.clear()
        m2.clear();
        assertTrue("Should have no entries", m2.getEntries().isEmpty());
        assertTrue("Should have no main attributes", m2.getMainAttributes()
                .isEmpty());
    }

    /**
     * @tests java.util.jar.Manifest#getAttributes(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAttributes",
        args = {java.lang.String.class}
    )
    public void test_getAttributesLjava_lang_String() {
        // Test for method java.util.jar.Attributes
        // java.util.jar.Manifest.getAttributes(java.lang.String)
        assertNull("Should not exist", m2.getAttributes("Doesn't Exist"));
        assertEquals("Should exist", "OK", m2
                .getAttributes("HasAttributes.txt").get(
                        new Attributes.Name("MyAttribute")));
    }

    /**
     * @tests java.util.jar.Manifest#getEntries()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEntries",
        args = {}
    )
    public void test_getEntries() {
        // Test for method java.util.Map java.util.jar.Manifest.getEntries()
        Map<String, Attributes> myMap = m2.getEntries();
        assertNull("Shouldn't exist", myMap.get("Doesn't exist"));
        assertEquals("Should exist", "OK", myMap.get("HasAttributes.txt").get(
                new Attributes.Name("MyAttribute")));

    }

    /**
     * @tests java.util.jar.Manifest#getMainAttributes()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMainAttributes",
        args = {}
    )
    public void test_getMainAttributes() {
        // Test for method java.util.jar.Attributes
        // java.util.jar.Manifest.getMainAttributes()
        Attributes a = m.getMainAttributes();
        assertEquals("Manifest_Version should return 1.0", "1.0", a
                .get(Attributes.Name.MANIFEST_VERSION));
    }

    /**
     * @tests {@link java.util.jar.Manifest#read(java.io.InputStream)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {java.io.InputStream.class}
    )
    public void test_readLjava_io_InputStream() {
        // Regression for HARMONY-89
        InputStream is = new InputStreamImpl();
        try {
            new Manifest().read(is);
            fail("Assert 0: Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }

        Manifest manifest = new Manifest();
        try {
            manifest.read(new URL(Support_Resources
                    .getURL("manifest/hyts_MANIFEST.MF")).openStream());
        } catch (MalformedURLException e) {
            fail("Can nor read manifest");
        } catch (IOException e) {
            fail("Can nor read manifest");
        }
        Attributes main = manifest.getMainAttributes();
        assertEquals("Bundle-Name not correct", "ClientSupport", main
                .getValue("Bundle-Name"));
        assertEquals(
                "Bundle-Description not correct",

                "Provides SessionService, AuthenticationService. Extends RegistryService.",
                main.getValue("Bundle-Description"));
        assertEquals("Bundle-Activator not correct",
                "com.ibm.ive.eccomm.client.support.ClientSupportActivator",
                main.getValue("Bundle-Activator"));
        assertEquals(
                "Import-Package not correct",

                "com.ibm.ive.eccomm.client.services.log,com.ibm.ive.eccomm.client.services.registry,com.ibm.ive.eccomm.service.registry; specification-version=1.0.0,com.ibm.ive.eccomm.service.session; specification-version=1.0.0,com.ibm.ive.eccomm.service.framework; specification-version=1.2.0,org.osgi.framework; specification-version=1.0.0,org.osgi.service.log; specification-version=1.0.0,com.ibm.ive.eccomm.flash; specification-version=1.2.0,com.ibm.ive.eccomm.client.xml,com.ibm.ive.eccomm.client.http.common,com.ibm.ive.eccomm.client.http.client",
                main.getValue("Import-Package"));
        assertEquals(
                "Import-Service not correct",

                "org.osgi.service.log.LogReaderServiceorg.osgi.service.log.LogService,com.ibm.ive.eccomm.service.registry.RegistryService",
                main.getValue("Import-Service"));
        assertEquals(
                "Export-Package not correct",

                "com.ibm.ive.eccomm.client.services.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.service.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.common; specification-version=1.0.0,com.ibm.ive.eccomm.client.services.registry.store; specification-version=1.0.0",
                main.getValue("Export-Package"));
        assertEquals(
                "Export-Service not correct",

                "com.ibm.ive.eccomm.service.authentication.AuthenticationService,com.ibm.ive.eccomm.service.session.SessionService",
                main.getValue("Export-Service"));
        assertEquals("Bundle-Vendor not correct", "IBM", main
                .getValue("Bundle-Vendor"));
        assertEquals("Bundle-Version not correct", "1.2.0", main
                .getValue("Bundle-Version"));
    }

    // helper class
    class InputStreamImpl extends InputStream {
        public InputStreamImpl() {
            super();
        }

        @Override
        public int read() {
            return 0;
        }
    }

    /**
     * @tests java.util.jar.Manifest#Manifest(Manifest)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Manifest",
        args = {java.util.jar.Manifest.class}
    )
    public void test_ConstructorLjava_util_jar_Manifest() {
        // Test for method java.util.jar.Manifest()
        Manifest emptyManifest = new Manifest();
        Manifest emptyClone = new Manifest(emptyManifest);
        assertTrue("Should have no entries", emptyClone.getEntries().isEmpty());
        assertTrue("Should have no main attributes", emptyClone
                .getMainAttributes().isEmpty());
        assertEquals(emptyClone, emptyManifest);
        assertEquals(emptyClone, emptyManifest.clone());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        Manifest emptyManifest = new Manifest();
        Manifest emptyClone = (Manifest) emptyManifest.clone();
        assertTrue("Should have no entries", emptyClone.getEntries().isEmpty());
        assertTrue("Should have no main attributes", emptyClone
                .getMainAttributes().isEmpty());
        assertEquals(emptyClone, emptyManifest);
        assertEquals(emptyManifest.clone().getClass().getName(),
                "java.util.jar.Manifest");

        Manifest manifest = null;
        try {
            manifest = new Manifest(new URL(Support_Resources
                    .getURL("manifest/hyts_MANIFEST.MF")).openStream());
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        } catch (IOException e) {
            fail("IOException");
        }
        Manifest manifestClone = (Manifest) manifest.clone();
        Attributes main = manifestClone.getMainAttributes();
        assertEquals("Bundle-Name not correct", "ClientSupport", main
                .getValue("Bundle-Name"));
        assertEquals(
                "Bundle-Description not correct",

                "Provides SessionService, AuthenticationService. Extends RegistryService.",
                main.getValue("Bundle-Description"));
        assertEquals("Bundle-Activator not correct",
                "com.ibm.ive.eccomm.client.support.ClientSupportActivator",
                main.getValue("Bundle-Activator"));
        assertEquals(
                "Import-Package not correct",

                "com.ibm.ive.eccomm.client.services.log,com.ibm.ive.eccomm.client.services.registry,com.ibm.ive.eccomm.service.registry; specification-version=1.0.0,com.ibm.ive.eccomm.service.session; specification-version=1.0.0,com.ibm.ive.eccomm.service.framework; specification-version=1.2.0,org.osgi.framework; specification-version=1.0.0,org.osgi.service.log; specification-version=1.0.0,com.ibm.ive.eccomm.flash; specification-version=1.2.0,com.ibm.ive.eccomm.client.xml,com.ibm.ive.eccomm.client.http.common,com.ibm.ive.eccomm.client.http.client",
                main.getValue("Import-Package"));
        assertEquals(
                "Import-Service not correct",

                "org.osgi.service.log.LogReaderServiceorg.osgi.service.log.LogService,com.ibm.ive.eccomm.service.registry.RegistryService",
                main.getValue("Import-Service"));
        assertEquals(
                "Export-Package not correct",

                "com.ibm.ive.eccomm.client.services.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.service.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.common; specification-version=1.0.0,com.ibm.ive.eccomm.client.services.registry.store; specification-version=1.0.0",
                main.getValue("Export-Package"));
        assertEquals(
                "Export-Service not correct",

                "com.ibm.ive.eccomm.service.authentication.AuthenticationService,com.ibm.ive.eccomm.service.session.SessionService",
                main.getValue("Export-Service"));
        assertEquals("Bundle-Vendor not correct", "IBM", main
                .getValue("Bundle-Vendor"));
        assertEquals("Bundle-Version not correct", "1.2.0", main
                .getValue("Bundle-Version"));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equals() {
        Manifest manifest1 = null;
        Manifest manifest2 = null;
        Manifest manifest3 = new Manifest();
        InputStream is = null;
        try {
            is = new URL(Support_Resources.getURL("manifest/hyts_MANIFEST.MF"))
                    .openStream();
        } catch (MalformedURLException e1) {
            fail("Failed to create InputStream object");
        } catch (IOException e1) {
            fail("Failed to create InputStream object");
        }
        try {
            manifest1 = new Manifest(is);
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        } catch (IOException e) {
            fail("IOException");
        }

        try {
            manifest2 = new Manifest(new URL(Support_Resources
                    .getURL("manifest/hyts_MANIFEST.MF")).openStream());
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        } catch (IOException e) {
            fail("IOException");
        }

        assertTrue(manifest1.equals(manifest1));
        assertTrue(manifest1.equals(manifest2));
        assertFalse(manifest1.equals(manifest3));
        assertFalse(manifest1.equals(this));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        Manifest manifest1 = null;
        Manifest manifest2 = new Manifest();
        InputStream is = null;
        try {
            manifest1 = new Manifest(new URL(Support_Resources
                    .getURL("manifest/hyts_MANIFEST.MF")).openStream());
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        } catch (IOException e) {
            fail("IOException");
        }
        assertEquals(manifest1.hashCode(), manifest1.hashCode());
        assertNotSame(manifest1.hashCode(), manifest2.hashCode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {java.io.OutputStream.class}
    )
    public void test_writeLjava_io_OutputStream() throws IOException {
        byte b[] = null;
        Manifest manifest1 = null;
        Manifest manifest2 = null;
        InputStream is = null;
        try {
            manifest1 = new Manifest(new URL(Support_Resources
                    .getURL("manifest/hyts_MANIFEST.MF")).openStream());
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        manifest1.write(baos);

        b = baos.toByteArray();

        File f = File.createTempFile("111", "111");
        FileOutputStream fos = new FileOutputStream(f);
        fos.close();
        try {
            manifest1.write(fos);
            fail("IOException expected");
        } catch (IOException e) {
            // expected
        }
        f.delete();

        ByteArrayInputStream bais = new ByteArrayInputStream(b);

        try {
            manifest2 = new Manifest(bais);
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        }

        assertTrue(manifest1.equals(manifest2));
    }
}
